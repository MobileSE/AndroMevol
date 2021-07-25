package android.filterfw.core;

import android.filterfw.core.GraphRunner;
import android.os.ConditionVariable;
import android.util.Log;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SyncRunner extends GraphRunner {
    private static final String TAG = "SyncRunner";
    private GraphRunner.OnRunnerDoneListener mDoneListener = null;
    private final boolean mLogVerbose = Log.isLoggable(TAG, 2);
    private Scheduler mScheduler = null;
    private StopWatchMap mTimer = null;
    private ConditionVariable mWakeCondition = new ConditionVariable();
    private ScheduledThreadPoolExecutor mWakeExecutor = new ScheduledThreadPoolExecutor(1);

    public SyncRunner(FilterContext context, FilterGraph graph, Class schedulerClass) {
        super(context);
        if (this.mLogVerbose) {
            Log.v(TAG, "Initializing SyncRunner");
        }
        if (Scheduler.class.isAssignableFrom(schedulerClass)) {
            try {
                this.mScheduler = (Scheduler) schedulerClass.getConstructor(FilterGraph.class).newInstance(graph);
                this.mFilterContext = context;
                this.mFilterContext.addGraph(graph);
                this.mTimer = new StopWatchMap();
                if (this.mLogVerbose) {
                    Log.v(TAG, "Setting up filters");
                }
                graph.setupFilters();
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Scheduler does not have constructor <init>(FilterGraph)!", e);
            } catch (InstantiationException e2) {
                throw new RuntimeException("Could not instantiate the Scheduler instance!", e2);
            } catch (IllegalAccessException e3) {
                throw new RuntimeException("Cannot access Scheduler constructor!", e3);
            } catch (InvocationTargetException e4) {
                throw new RuntimeException("Scheduler constructor threw an exception", e4);
            } catch (Exception e5) {
                throw new RuntimeException("Could not instantiate Scheduler", e5);
            }
        } else {
            throw new IllegalArgumentException("Class provided is not a Scheduler subclass!");
        }
    }

    @Override // android.filterfw.core.GraphRunner
    public FilterGraph getGraph() {
        if (this.mScheduler != null) {
            return this.mScheduler.getGraph();
        }
        return null;
    }

    public int step() {
        assertReadyToStep();
        if (!getGraph().isReady()) {
            throw new RuntimeException("Trying to process graph that is not open!");
        } else if (performStep()) {
            return 1;
        } else {
            return determinePostRunState();
        }
    }

    public void beginProcessing() {
        this.mScheduler.reset();
        getGraph().beginProcessing();
    }

    @Override // android.filterfw.core.GraphRunner
    public void close() {
        if (this.mLogVerbose) {
            Log.v(TAG, "Closing graph.");
        }
        getGraph().closeFilters(this.mFilterContext);
        this.mScheduler.reset();
    }

    @Override // android.filterfw.core.GraphRunner
    public void run() {
        if (this.mLogVerbose) {
            Log.v(TAG, "Beginning run.");
        }
        assertReadyToStep();
        beginProcessing();
        boolean glActivated = activateGlContext();
        boolean keepRunning = true;
        while (keepRunning) {
            keepRunning = performStep();
        }
        if (glActivated) {
            deactivateGlContext();
        }
        if (this.mDoneListener != null) {
            if (this.mLogVerbose) {
                Log.v(TAG, "Calling completion listener.");
            }
            this.mDoneListener.onRunnerDone(determinePostRunState());
        }
        if (this.mLogVerbose) {
            Log.v(TAG, "Run complete");
        }
    }

    @Override // android.filterfw.core.GraphRunner
    public boolean isRunning() {
        return false;
    }

    @Override // android.filterfw.core.GraphRunner
    public void setDoneCallback(GraphRunner.OnRunnerDoneListener listener) {
        this.mDoneListener = listener;
    }

    @Override // android.filterfw.core.GraphRunner
    public void stop() {
        throw new RuntimeException("SyncRunner does not support stopping a graph!");
    }

    @Override // android.filterfw.core.GraphRunner
    public synchronized Exception getError() {
        return null;
    }

    /* access modifiers changed from: protected */
    public void waitUntilWake() {
        this.mWakeCondition.block();
    }

    /* access modifiers changed from: protected */
    public void processFilterNode(Filter filter) {
        if (this.mLogVerbose) {
            Log.v(TAG, "Processing filter node");
        }
        filter.performProcess(this.mFilterContext);
        if (filter.getStatus() == 6) {
            throw new RuntimeException("There was an error executing " + filter + "!");
        } else if (filter.getStatus() == 4) {
            if (this.mLogVerbose) {
                Log.v(TAG, "Scheduling filter wakeup");
            }
            scheduleFilterWake(filter, filter.getSleepDelay());
        }
    }

    /* access modifiers changed from: protected */
    public void scheduleFilterWake(final Filter filter, int delay) {
        this.mWakeCondition.close();
        final ConditionVariable conditionToWake = this.mWakeCondition;
        this.mWakeExecutor.schedule(new Runnable() {
            /* class android.filterfw.core.SyncRunner.AnonymousClass1 */

            public void run() {
                filter.unsetStatus(4);
                conditionToWake.open();
            }
        }, (long) delay, TimeUnit.MILLISECONDS);
    }

    /* access modifiers changed from: protected */
    public int determinePostRunState() {
        for (Filter filter : this.mScheduler.getGraph().getFilters()) {
            if (filter.isOpen()) {
                if (filter.getStatus() == 4) {
                    return 3;
                }
                return 4;
            }
        }
        return 2;
    }

    /* access modifiers changed from: package-private */
    public boolean performStep() {
        if (this.mLogVerbose) {
            Log.v(TAG, "Performing one step.");
        }
        Filter filter = this.mScheduler.scheduleNextNode();
        if (filter == null) {
            return false;
        }
        this.mTimer.start(filter.getName());
        processFilterNode(filter);
        this.mTimer.stop(filter.getName());
        return true;
    }

    /* access modifiers changed from: package-private */
    public void assertReadyToStep() {
        if (this.mScheduler == null) {
            throw new RuntimeException("Attempting to run schedule with no scheduler in place!");
        } else if (getGraph() == null) {
            throw new RuntimeException("Calling step on scheduler with no graph in place!");
        }
    }
}

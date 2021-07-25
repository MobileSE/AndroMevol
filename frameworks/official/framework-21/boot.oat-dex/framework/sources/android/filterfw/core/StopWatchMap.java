package android.filterfw.core;

import java.util.HashMap;

public class StopWatchMap {
    public boolean LOG_MFF_RUNNING_TIMES;
    private HashMap<String, StopWatch> mStopWatches;

    public StopWatchMap() {
        this.LOG_MFF_RUNNING_TIMES = false;
        this.mStopWatches = null;
        this.mStopWatches = new HashMap<>();
    }

    public void start(String stopWatchName) {
        if (this.LOG_MFF_RUNNING_TIMES) {
            if (!this.mStopWatches.containsKey(stopWatchName)) {
                this.mStopWatches.put(stopWatchName, new StopWatch(stopWatchName));
            }
            this.mStopWatches.get(stopWatchName).start();
        }
    }

    public void stop(String stopWatchName) {
        if (this.LOG_MFF_RUNNING_TIMES) {
            if (!this.mStopWatches.containsKey(stopWatchName)) {
                throw new RuntimeException("Calling stop with unknown stopWatchName: " + stopWatchName);
            }
            this.mStopWatches.get(stopWatchName).stop();
        }
    }
}

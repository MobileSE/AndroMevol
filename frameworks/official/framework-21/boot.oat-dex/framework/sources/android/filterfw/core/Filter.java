package android.filterfw.core;

import android.filterfw.format.ObjectFormat;
import android.filterfw.io.GraphIOException;
import android.filterfw.io.TextGraphReader;
import android.util.Log;
import android.view.KeyEvent;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public abstract class Filter {
    static final int STATUS_ERROR = 6;
    static final int STATUS_FINISHED = 5;
    static final int STATUS_PREINIT = 0;
    static final int STATUS_PREPARED = 2;
    static final int STATUS_PROCESSING = 3;
    static final int STATUS_RELEASED = 7;
    static final int STATUS_SLEEPING = 4;
    static final int STATUS_UNPREPARED = 1;
    private static final String TAG = "Filter";
    private long mCurrentTimestamp;
    private HashSet<Frame> mFramesToRelease;
    private HashMap<String, Frame> mFramesToSet;
    private int mInputCount = -1;
    private HashMap<String, InputPort> mInputPorts;
    private boolean mIsOpen = false;
    private boolean mLogVerbose;
    private String mName;
    private int mOutputCount = -1;
    private HashMap<String, OutputPort> mOutputPorts;
    private int mSleepDelay;
    private int mStatus = 0;

    public abstract void process(FilterContext filterContext);

    public abstract void setupPorts();

    public Filter(String name) {
        this.mName = name;
        this.mFramesToRelease = new HashSet<>();
        this.mFramesToSet = new HashMap<>();
        this.mStatus = 0;
        this.mLogVerbose = Log.isLoggable(TAG, 2);
    }

    public static final boolean isAvailable(String filterName) {
        try {
            try {
                Thread.currentThread().getContextClassLoader().loadClass(filterName).asSubclass(Filter.class);
                return true;
            } catch (ClassCastException e) {
                return false;
            }
        } catch (ClassNotFoundException e2) {
            return false;
        }
    }

    public final void initWithValueMap(KeyValueMap valueMap) {
        initFinalPorts(valueMap);
        initRemainingPorts(valueMap);
        this.mStatus = 1;
    }

    public final void initWithAssignmentString(String assignments) {
        try {
            initWithValueMap(new TextGraphReader().readKeyValueAssignments(assignments));
        } catch (GraphIOException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public final void initWithAssignmentList(Object... keyValues) {
        KeyValueMap valueMap = new KeyValueMap();
        valueMap.setKeyValues(keyValues);
        initWithValueMap(valueMap);
    }

    public final void init() throws ProtocolException {
        initWithValueMap(new KeyValueMap());
    }

    public String getFilterClassName() {
        return getClass().getSimpleName();
    }

    public final String getName() {
        return this.mName;
    }

    public boolean isOpen() {
        return this.mIsOpen;
    }

    public void setInputFrame(String inputName, Frame frame) {
        FilterPort port = getInputPort(inputName);
        if (!port.isOpen()) {
            port.open();
        }
        port.setFrame(frame);
    }

    public final void setInputValue(String inputName, Object value) {
        setInputFrame(inputName, wrapInputValue(inputName, value));
    }

    /* access modifiers changed from: protected */
    public void prepare(FilterContext context) {
    }

    /* access modifiers changed from: protected */
    public void parametersUpdated(Set<String> set) {
    }

    /* access modifiers changed from: protected */
    public void delayNextProcess(int millisecs) {
        this.mSleepDelay = millisecs;
        this.mStatus = 4;
    }

    public FrameFormat getOutputFormat(String portName, FrameFormat inputFormat) {
        return null;
    }

    public final FrameFormat getInputFormat(String portName) {
        return getInputPort(portName).getSourceFormat();
    }

    public void open(FilterContext context) {
    }

    public final int getSleepDelay() {
        return KeyEvent.KEYCODE_TV_INPUT_COMPONENT_2;
    }

    public void close(FilterContext context) {
    }

    public void tearDown(FilterContext context) {
    }

    public final int getNumberOfConnectedInputs() {
        int c = 0;
        for (InputPort inputPort : this.mInputPorts.values()) {
            if (inputPort.isConnected()) {
                c++;
            }
        }
        return c;
    }

    public final int getNumberOfConnectedOutputs() {
        int c = 0;
        for (OutputPort outputPort : this.mOutputPorts.values()) {
            if (outputPort.isConnected()) {
                c++;
            }
        }
        return c;
    }

    public final int getNumberOfInputs() {
        if (this.mOutputPorts == null) {
            return 0;
        }
        return this.mInputPorts.size();
    }

    public final int getNumberOfOutputs() {
        if (this.mInputPorts == null) {
            return 0;
        }
        return this.mOutputPorts.size();
    }

    public final InputPort getInputPort(String portName) {
        if (this.mInputPorts == null) {
            throw new NullPointerException("Attempting to access input port '" + portName + "' of " + this + " before Filter has been initialized!");
        }
        InputPort result = this.mInputPorts.get(portName);
        if (result != null) {
            return result;
        }
        throw new IllegalArgumentException("Unknown input port '" + portName + "' on filter " + this + "!");
    }

    public final OutputPort getOutputPort(String portName) {
        if (this.mInputPorts == null) {
            throw new NullPointerException("Attempting to access output port '" + portName + "' of " + this + " before Filter has been initialized!");
        }
        OutputPort result = this.mOutputPorts.get(portName);
        if (result != null) {
            return result;
        }
        throw new IllegalArgumentException("Unknown output port '" + portName + "' on filter " + this + "!");
    }

    /* access modifiers changed from: protected */
    public final void pushOutput(String name, Frame frame) {
        if (frame.getTimestamp() == -2) {
            if (this.mLogVerbose) {
                Log.v(TAG, "Default-setting output Frame timestamp on port " + name + " to " + this.mCurrentTimestamp);
            }
            frame.setTimestamp(this.mCurrentTimestamp);
        }
        getOutputPort(name).pushFrame(frame);
    }

    /* access modifiers changed from: protected */
    public final Frame pullInput(String name) {
        Frame result = getInputPort(name).pullFrame();
        if (this.mCurrentTimestamp == -1) {
            this.mCurrentTimestamp = result.getTimestamp();
            if (this.mLogVerbose) {
                Log.v(TAG, "Default-setting current timestamp from input port " + name + " to " + this.mCurrentTimestamp);
            }
        }
        this.mFramesToRelease.add(result);
        return result;
    }

    public void fieldPortValueUpdated(String name, FilterContext context) {
    }

    /* access modifiers changed from: protected */
    public void transferInputPortFrame(String name, FilterContext context) {
        getInputPort(name).transfer(context);
    }

    /* access modifiers changed from: protected */
    public void initProgramInputs(Program program, FilterContext context) {
        if (program != null) {
            for (InputPort inputPort : this.mInputPorts.values()) {
                if (inputPort.getTarget() == program) {
                    inputPort.transfer(context);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void addInputPort(String name) {
        addMaskedInputPort(name, null);
    }

    /* access modifiers changed from: protected */
    public void addMaskedInputPort(String name, FrameFormat formatMask) {
        InputPort port = new StreamPort(this, name);
        if (this.mLogVerbose) {
            Log.v(TAG, "Filter " + this + " adding " + port);
        }
        this.mInputPorts.put(name, port);
        port.setPortFormat(formatMask);
    }

    /* access modifiers changed from: protected */
    public void addOutputPort(String name, FrameFormat format) {
        OutputPort port = new OutputPort(this, name);
        if (this.mLogVerbose) {
            Log.v(TAG, "Filter " + this + " adding " + port);
        }
        port.setPortFormat(format);
        this.mOutputPorts.put(name, port);
    }

    /* access modifiers changed from: protected */
    public void addOutputBasedOnInput(String outputName, String inputName) {
        OutputPort port = new OutputPort(this, outputName);
        if (this.mLogVerbose) {
            Log.v(TAG, "Filter " + this + " adding " + port);
        }
        port.setBasePort(getInputPort(inputName));
        this.mOutputPorts.put(outputName, port);
    }

    /* access modifiers changed from: protected */
    public void addFieldPort(String name, Field field, boolean hasDefault, boolean isFinal) {
        field.setAccessible(true);
        InputPort fieldPort = isFinal ? new FinalPort(this, name, field, hasDefault) : new FieldPort(this, name, field, hasDefault);
        if (this.mLogVerbose) {
            Log.v(TAG, "Filter " + this + " adding " + fieldPort);
        }
        fieldPort.setPortFormat(ObjectFormat.fromClass(field.getType(), 1));
        this.mInputPorts.put(name, fieldPort);
    }

    /* access modifiers changed from: protected */
    public void addProgramPort(String name, String varName, Field field, Class varType, boolean hasDefault) {
        field.setAccessible(true);
        InputPort programPort = new ProgramPort(this, name, varName, field, hasDefault);
        if (this.mLogVerbose) {
            Log.v(TAG, "Filter " + this + " adding " + programPort);
        }
        programPort.setPortFormat(ObjectFormat.fromClass(varType, 1));
        this.mInputPorts.put(name, programPort);
    }

    /* access modifiers changed from: protected */
    public void closeOutputPort(String name) {
        getOutputPort(name).close();
    }

    /* access modifiers changed from: protected */
    public void setWaitsOnInputPort(String portName, boolean waits) {
        getInputPort(portName).setBlocking(waits);
    }

    /* access modifiers changed from: protected */
    public void setWaitsOnOutputPort(String portName, boolean waits) {
        getOutputPort(portName).setBlocking(waits);
    }

    public String toString() {
        return "'" + getName() + "' (" + getFilterClassName() + ")";
    }

    /* access modifiers changed from: package-private */
    public final Collection<InputPort> getInputPorts() {
        return this.mInputPorts.values();
    }

    /* access modifiers changed from: package-private */
    public final Collection<OutputPort> getOutputPorts() {
        return this.mOutputPorts.values();
    }

    /* access modifiers changed from: package-private */
    public final synchronized int getStatus() {
        return this.mStatus;
    }

    /* access modifiers changed from: package-private */
    public final synchronized void unsetStatus(int flag) {
        this.mStatus &= flag ^ -1;
    }

    /* access modifiers changed from: package-private */
    public final synchronized void performOpen(FilterContext context) {
        if (!this.mIsOpen) {
            if (this.mStatus == 1) {
                if (this.mLogVerbose) {
                    Log.v(TAG, "Preparing " + this);
                }
                prepare(context);
                this.mStatus = 2;
            }
            if (this.mStatus == 2) {
                if (this.mLogVerbose) {
                    Log.v(TAG, "Opening " + this);
                }
                open(context);
                this.mStatus = 3;
            }
            if (this.mStatus != 3) {
                throw new RuntimeException("Filter " + this + " was brought into invalid state during " + "opening (state: " + this.mStatus + ")!");
            }
            this.mIsOpen = true;
        }
    }

    /* access modifiers changed from: package-private */
    public final synchronized void performProcess(FilterContext context) {
        if (this.mStatus == 7) {
            throw new RuntimeException("Filter " + this + " is already torn down!");
        }
        transferInputFrames(context);
        if (this.mStatus < 3) {
            performOpen(context);
        }
        if (this.mLogVerbose) {
            Log.v(TAG, "Processing " + this);
        }
        this.mCurrentTimestamp = -1;
        process(context);
        releasePulledFrames(context);
        if (filterMustClose()) {
            performClose(context);
        }
    }

    /* access modifiers changed from: package-private */
    public final synchronized void performClose(FilterContext context) {
        if (this.mIsOpen) {
            if (this.mLogVerbose) {
                Log.v(TAG, "Closing " + this);
            }
            this.mIsOpen = false;
            this.mStatus = 2;
            close(context);
            closePorts();
        }
    }

    /* access modifiers changed from: package-private */
    public final synchronized void performTearDown(FilterContext context) {
        performClose(context);
        if (this.mStatus != 7) {
            tearDown(context);
            this.mStatus = 7;
        }
    }

    /* access modifiers changed from: package-private */
    public final synchronized boolean canProcess() {
        boolean z = false;
        synchronized (this) {
            if (this.mLogVerbose) {
                Log.v(TAG, "Checking if can process: " + this + " (" + this.mStatus + ").");
            }
            if (this.mStatus <= 3 && inputConditionsMet() && outputConditionsMet()) {
                z = true;
            }
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public final void openOutputs() {
        if (this.mLogVerbose) {
            Log.v(TAG, "Opening all output ports on " + this + "!");
        }
        for (OutputPort outputPort : this.mOutputPorts.values()) {
            if (!outputPort.isOpen()) {
                outputPort.open();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final void clearInputs() {
        for (InputPort inputPort : this.mInputPorts.values()) {
            inputPort.clear();
        }
    }

    /* access modifiers changed from: package-private */
    public final void clearOutputs() {
        for (OutputPort outputPort : this.mOutputPorts.values()) {
            outputPort.clear();
        }
    }

    /* access modifiers changed from: package-private */
    public final void notifyFieldPortValueUpdated(String name, FilterContext context) {
        if (this.mStatus == 3 || this.mStatus == 2) {
            fieldPortValueUpdated(name, context);
        }
    }

    /* access modifiers changed from: package-private */
    public final synchronized void pushInputFrame(String inputName, Frame frame) {
        FilterPort port = getInputPort(inputName);
        if (!port.isOpen()) {
            port.open();
        }
        port.pushFrame(frame);
    }

    /* access modifiers changed from: package-private */
    public final synchronized void pushInputValue(String inputName, Object value) {
        pushInputFrame(inputName, wrapInputValue(inputName, value));
    }

    private final void initFinalPorts(KeyValueMap values) {
        this.mInputPorts = new HashMap<>();
        this.mOutputPorts = new HashMap<>();
        addAndSetFinalPorts(values);
    }

    private final void initRemainingPorts(KeyValueMap values) {
        addAnnotatedPorts();
        setupPorts();
        setInitialInputValues(values);
    }

    private final void addAndSetFinalPorts(KeyValueMap values) {
        Field[] arr$ = getClass().getDeclaredFields();
        for (Field field : arr$) {
            Annotation annotation = field.getAnnotation(GenerateFinalPort.class);
            if (annotation != null) {
                GenerateFinalPort generator = (GenerateFinalPort) annotation;
                String name = generator.name().isEmpty() ? field.getName() : generator.name();
                addFieldPort(name, field, generator.hasDefault(), true);
                if (values.containsKey(name)) {
                    setImmediateInputValue(name, values.get(name));
                    values.remove(name);
                } else if (!generator.hasDefault()) {
                    throw new RuntimeException("No value specified for final input port '" + name + "' of filter " + this + "!");
                }
            }
        }
    }

    private final void addAnnotatedPorts() {
        Field[] arr$ = getClass().getDeclaredFields();
        for (Field field : arr$) {
            Annotation annotation = field.getAnnotation(GenerateFieldPort.class);
            if (annotation != null) {
                addFieldGenerator((GenerateFieldPort) annotation, field);
            } else {
                Annotation annotation2 = field.getAnnotation(GenerateProgramPort.class);
                if (annotation2 != null) {
                    addProgramGenerator((GenerateProgramPort) annotation2, field);
                } else {
                    Annotation annotation3 = field.getAnnotation(GenerateProgramPorts.class);
                    if (annotation3 != null) {
                        for (GenerateProgramPort generator : ((GenerateProgramPorts) annotation3).value()) {
                            addProgramGenerator(generator, field);
                        }
                    }
                }
            }
        }
    }

    private final void addFieldGenerator(GenerateFieldPort generator, Field field) {
        addFieldPort(generator.name().isEmpty() ? field.getName() : generator.name(), field, generator.hasDefault(), false);
    }

    private final void addProgramGenerator(GenerateProgramPort generator, Field field) {
        String name = generator.name();
        addProgramPort(name, generator.variableName().isEmpty() ? name : generator.variableName(), field, generator.type(), generator.hasDefault());
    }

    private final void setInitialInputValues(KeyValueMap values) {
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            setInputValue(entry.getKey(), entry.getValue());
        }
    }

    private final void setImmediateInputValue(String name, Object value) {
        if (this.mLogVerbose) {
            Log.v(TAG, "Setting immediate value " + value + " for port " + name + "!");
        }
        FilterPort port = getInputPort(name);
        port.open();
        port.setFrame(SimpleFrame.wrapObject(value, null));
    }

    private final void transferInputFrames(FilterContext context) {
        for (InputPort inputPort : this.mInputPorts.values()) {
            inputPort.transfer(context);
        }
    }

    private final Frame wrapInputValue(String inputName, Object value) {
        boolean shouldSerialize = true;
        MutableFrameFormat inputFormat = ObjectFormat.fromObject(value, 1);
        if (value == null) {
            FrameFormat portFormat = getInputPort(inputName).getPortFormat();
            inputFormat.setObjectClass(portFormat == null ? null : portFormat.getObjectClass());
        }
        if ((value instanceof Number) || (value instanceof Boolean) || (value instanceof String) || !(value instanceof Serializable)) {
            shouldSerialize = false;
        }
        Frame frame = shouldSerialize ? new SerializedFrame(inputFormat, null) : new SimpleFrame(inputFormat, null);
        frame.setObjectValue(value);
        return frame;
    }

    private final void releasePulledFrames(FilterContext context) {
        Iterator i$ = this.mFramesToRelease.iterator();
        while (i$.hasNext()) {
            context.getFrameManager().releaseFrame(i$.next());
        }
        this.mFramesToRelease.clear();
    }

    private final boolean inputConditionsMet() {
        for (InputPort port : this.mInputPorts.values()) {
            if (!port.isReady()) {
                if (this.mLogVerbose) {
                    Log.v(TAG, "Input condition not met: " + port + "!");
                }
                return false;
            }
        }
        return true;
    }

    private final boolean outputConditionsMet() {
        for (OutputPort port : this.mOutputPorts.values()) {
            if (!port.isReady()) {
                if (this.mLogVerbose) {
                    Log.v(TAG, "Output condition not met: " + port + "!");
                }
                return false;
            }
        }
        return true;
    }

    private final void closePorts() {
        if (this.mLogVerbose) {
            Log.v(TAG, "Closing all ports on " + this + "!");
        }
        for (InputPort inputPort : this.mInputPorts.values()) {
            inputPort.close();
        }
        for (OutputPort outputPort : this.mOutputPorts.values()) {
            outputPort.close();
        }
    }

    private final boolean filterMustClose() {
        for (InputPort inputPort : this.mInputPorts.values()) {
            if (inputPort.filterMustClose()) {
                if (!this.mLogVerbose) {
                    return true;
                }
                Log.v(TAG, "Filter " + this + " must close due to port " + inputPort);
                return true;
            }
        }
        for (OutputPort outputPort : this.mOutputPorts.values()) {
            if (outputPort.filterMustClose()) {
                if (!this.mLogVerbose) {
                    return true;
                }
                Log.v(TAG, "Filter " + this + " must close due to port " + outputPort);
                return true;
            }
        }
        return false;
    }
}

package android.hardware;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.MessageQueue;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import dalvik.system.CloseGuard;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SystemSensorManager extends SensorManager {
    private static final ArrayList<Sensor> sFullSensorsList = new ArrayList<>();
    private static final SparseArray<Sensor> sHandleToSensor = new SparseArray<>();
    private static boolean sSensorModuleInitialized = false;
    private static final Object sSensorModuleLock = new Object();
    private final Looper mMainLooper;
    private final HashMap<SensorEventListener, SensorEventQueue> mSensorListeners = new HashMap<>();
    private final int mTargetSdkLevel;
    private final HashMap<TriggerEventListener, TriggerEventQueue> mTriggerListeners = new HashMap<>();

    private static native void nativeClassInit();

    private static native int nativeGetNextSensor(Sensor sensor, int i);

    public SystemSensorManager(Context context, Looper mainLooper) {
        this.mMainLooper = mainLooper;
        this.mTargetSdkLevel = context.getApplicationInfo().targetSdkVersion;
        synchronized (sSensorModuleLock) {
            if (!sSensorModuleInitialized) {
                sSensorModuleInitialized = true;
                nativeClassInit();
                ArrayList<Sensor> fullList = sFullSensorsList;
                int i = 0;
                do {
                    Sensor sensor = new Sensor();
                    i = nativeGetNextSensor(sensor, i);
                    if (i >= 0) {
                        fullList.add(sensor);
                        sHandleToSensor.append(sensor.getHandle(), sensor);
                        continue;
                    }
                } while (i > 0);
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.hardware.SensorManager
    public List<Sensor> getFullSensorList() {
        return sFullSensorsList;
    }

    /* access modifiers changed from: protected */
    @Override // android.hardware.SensorManager
    public boolean registerListenerImpl(SensorEventListener listener, Sensor sensor, int delayUs, Handler handler, int maxBatchReportLatencyUs, int reservedFlags) {
        boolean z = false;
        if (listener == null || sensor == null) {
            Log.e("SensorManager", "sensor or listener is null");
        } else if (sensor.getReportingMode() == 2) {
            Log.e("SensorManager", "Trigger Sensors should use the requestTriggerSensor.");
        } else if (maxBatchReportLatencyUs < 0 || delayUs < 0) {
            Log.e("SensorManager", "maxBatchReportLatencyUs and delayUs should be non-negative");
        } else {
            synchronized (this.mSensorListeners) {
                SensorEventQueue queue = this.mSensorListeners.get(listener);
                if (queue == null) {
                    SensorEventQueue queue2 = new SensorEventQueue(listener, handler != null ? handler.getLooper() : this.mMainLooper, this);
                    if (!queue2.addSensor(sensor, delayUs, maxBatchReportLatencyUs, reservedFlags)) {
                        queue2.dispose();
                    } else {
                        this.mSensorListeners.put(listener, queue2);
                        z = true;
                    }
                } else {
                    z = queue.addSensor(sensor, delayUs, maxBatchReportLatencyUs, reservedFlags);
                }
            }
        }
        return z;
    }

    /* access modifiers changed from: protected */
    @Override // android.hardware.SensorManager
    public void unregisterListenerImpl(SensorEventListener listener, Sensor sensor) {
        boolean result;
        if (sensor == null || sensor.getReportingMode() != 2) {
            synchronized (this.mSensorListeners) {
                SensorEventQueue queue = this.mSensorListeners.get(listener);
                if (queue != null) {
                    if (sensor == null) {
                        result = queue.removeAllSensors();
                    } else {
                        result = queue.removeSensor(sensor, true);
                    }
                    if (result && !queue.hasSensors()) {
                        this.mSensorListeners.remove(listener);
                        queue.dispose();
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.hardware.SensorManager
    public boolean requestTriggerSensorImpl(TriggerEventListener listener, Sensor sensor) {
        boolean z = false;
        if (sensor == null) {
            throw new IllegalArgumentException("sensor cannot be null");
        }
        if (sensor.getReportingMode() == 2) {
            synchronized (this.mTriggerListeners) {
                TriggerEventQueue queue = this.mTriggerListeners.get(listener);
                if (queue == null) {
                    TriggerEventQueue queue2 = new TriggerEventQueue(listener, this.mMainLooper, this);
                    if (!queue2.addSensor(sensor, 0, 0, 0)) {
                        queue2.dispose();
                    } else {
                        this.mTriggerListeners.put(listener, queue2);
                        z = true;
                    }
                } else {
                    z = queue.addSensor(sensor, 0, 0, 0);
                }
            }
        }
        return z;
    }

    /* access modifiers changed from: protected */
    @Override // android.hardware.SensorManager
    public boolean cancelTriggerSensorImpl(TriggerEventListener listener, Sensor sensor, boolean disable) {
        boolean result = false;
        if (sensor == null || sensor.getReportingMode() == 2) {
            synchronized (this.mTriggerListeners) {
                TriggerEventQueue queue = this.mTriggerListeners.get(listener);
                if (queue != null) {
                    if (sensor == null) {
                        result = queue.removeAllSensors();
                    } else {
                        result = queue.removeSensor(sensor, disable);
                    }
                    if (result && !queue.hasSensors()) {
                        this.mTriggerListeners.remove(listener);
                        queue.dispose();
                    }
                }
            }
        }
        return result;
    }

    /* access modifiers changed from: protected */
    @Override // android.hardware.SensorManager
    public boolean flushImpl(SensorEventListener listener) {
        boolean z = false;
        if (listener == null) {
            throw new IllegalArgumentException("listener cannot be null");
        }
        synchronized (this.mSensorListeners) {
            SensorEventQueue queue = this.mSensorListeners.get(listener);
            if (queue != null) {
                if (queue.flush() == 0) {
                    z = true;
                }
            }
        }
        return z;
    }

    private static abstract class BaseEventQueue {
        private final SparseBooleanArray mActiveSensors = new SparseBooleanArray();
        private final CloseGuard mCloseGuard = CloseGuard.get();
        protected final SparseBooleanArray mFirstEvent = new SparseBooleanArray();
        protected final SystemSensorManager mManager;
        private final float[] mScratch = new float[16];
        protected final SparseIntArray mSensorAccuracies = new SparseIntArray();
        private long nSensorEventQueue;

        private static native void nativeDestroySensorEventQueue(long j);

        private static native int nativeDisableSensor(long j, int i);

        private static native int nativeEnableSensor(long j, int i, int i2, int i3, int i4);

        private static native int nativeFlushSensor(long j);

        private native long nativeInitBaseEventQueue(BaseEventQueue baseEventQueue, MessageQueue messageQueue, float[] fArr);

        /* access modifiers changed from: protected */
        public abstract void addSensorEvent(Sensor sensor);

        /* access modifiers changed from: protected */
        public abstract void dispatchFlushCompleteEvent(int i);

        /* access modifiers changed from: protected */
        public abstract void dispatchSensorEvent(int i, float[] fArr, int i2, long j);

        /* access modifiers changed from: protected */
        public abstract void removeSensorEvent(Sensor sensor);

        BaseEventQueue(Looper looper, SystemSensorManager manager) {
            this.nSensorEventQueue = nativeInitBaseEventQueue(this, looper.getQueue(), this.mScratch);
            this.mCloseGuard.open("dispose");
            this.mManager = manager;
        }

        public void dispose() {
            dispose(false);
        }

        public boolean addSensor(Sensor sensor, int delayUs, int maxBatchReportLatencyUs, int reservedFlags) {
            int handle = sensor.getHandle();
            if (this.mActiveSensors.get(handle)) {
                return false;
            }
            this.mActiveSensors.put(handle, true);
            addSensorEvent(sensor);
            if (enableSensor(sensor, delayUs, maxBatchReportLatencyUs, reservedFlags) == 0 || (maxBatchReportLatencyUs != 0 && (maxBatchReportLatencyUs <= 0 || enableSensor(sensor, delayUs, 0, 0) == 0))) {
                return true;
            }
            removeSensor(sensor, false);
            return false;
        }

        public boolean removeAllSensors() {
            for (int i = 0; i < this.mActiveSensors.size(); i++) {
                if (this.mActiveSensors.valueAt(i)) {
                    int handle = this.mActiveSensors.keyAt(i);
                    Sensor sensor = (Sensor) SystemSensorManager.sHandleToSensor.get(handle);
                    if (sensor != null) {
                        disableSensor(sensor);
                        this.mActiveSensors.put(handle, false);
                        removeSensorEvent(sensor);
                    }
                }
            }
            return true;
        }

        public boolean removeSensor(Sensor sensor, boolean disable) {
            if (!this.mActiveSensors.get(sensor.getHandle())) {
                return false;
            }
            if (disable) {
                disableSensor(sensor);
            }
            this.mActiveSensors.put(sensor.getHandle(), false);
            removeSensorEvent(sensor);
            return true;
        }

        public int flush() {
            if (this.nSensorEventQueue != 0) {
                return nativeFlushSensor(this.nSensorEventQueue);
            }
            throw new NullPointerException();
        }

        public boolean hasSensors() {
            return this.mActiveSensors.indexOfValue(true) >= 0;
        }

        /* access modifiers changed from: protected */
        public void finalize() throws Throwable {
            try {
                dispose(true);
            } finally {
                super.finalize();
            }
        }

        private void dispose(boolean finalized) {
            if (this.mCloseGuard != null) {
                if (finalized) {
                    this.mCloseGuard.warnIfOpen();
                }
                this.mCloseGuard.close();
            }
            if (this.nSensorEventQueue != 0) {
                nativeDestroySensorEventQueue(this.nSensorEventQueue);
                this.nSensorEventQueue = 0;
            }
        }

        private int enableSensor(Sensor sensor, int rateUs, int maxBatchReportLatencyUs, int reservedFlags) {
            if (this.nSensorEventQueue == 0) {
                throw new NullPointerException();
            } else if (sensor != null) {
                return nativeEnableSensor(this.nSensorEventQueue, sensor.getHandle(), rateUs, maxBatchReportLatencyUs, reservedFlags);
            } else {
                throw new NullPointerException();
            }
        }

        private int disableSensor(Sensor sensor) {
            if (this.nSensorEventQueue == 0) {
                throw new NullPointerException();
            } else if (sensor != null) {
                return nativeDisableSensor(this.nSensorEventQueue, sensor.getHandle());
            } else {
                throw new NullPointerException();
            }
        }
    }

    static final class SensorEventQueue extends BaseEventQueue {
        private final SensorEventListener mListener;
        private final SparseArray<SensorEvent> mSensorsEvents = new SparseArray<>();

        public SensorEventQueue(SensorEventListener listener, Looper looper, SystemSensorManager manager) {
            super(looper, manager);
            this.mListener = listener;
        }

        @Override // android.hardware.SystemSensorManager.BaseEventQueue
        public void addSensorEvent(Sensor sensor) {
            SensorEvent t = new SensorEvent(Sensor.getMaxLengthValuesArray(sensor, this.mManager.mTargetSdkLevel));
            synchronized (this.mSensorsEvents) {
                this.mSensorsEvents.put(sensor.getHandle(), t);
            }
        }

        @Override // android.hardware.SystemSensorManager.BaseEventQueue
        public void removeSensorEvent(Sensor sensor) {
            synchronized (this.mSensorsEvents) {
                this.mSensorsEvents.delete(sensor.getHandle());
            }
        }

        /* access modifiers changed from: protected */
        @Override // android.hardware.SystemSensorManager.BaseEventQueue
        public void dispatchSensorEvent(int handle, float[] values, int inAccuracy, long timestamp) {
            SensorEvent t;
            Sensor sensor = (Sensor) SystemSensorManager.sHandleToSensor.get(handle);
            synchronized (this.mSensorsEvents) {
                t = this.mSensorsEvents.get(handle);
            }
            if (t != null) {
                System.arraycopy(values, 0, t.values, 0, t.values.length);
                t.timestamp = timestamp;
                t.accuracy = inAccuracy;
                t.sensor = sensor;
                int accuracy = this.mSensorAccuracies.get(handle);
                if (t.accuracy >= 0 && accuracy != t.accuracy) {
                    this.mSensorAccuracies.put(handle, t.accuracy);
                    this.mListener.onAccuracyChanged(t.sensor, t.accuracy);
                }
                this.mListener.onSensorChanged(t);
            }
        }

        /* access modifiers changed from: protected */
        @Override // android.hardware.SystemSensorManager.BaseEventQueue
        public void dispatchFlushCompleteEvent(int handle) {
            if (this.mListener instanceof SensorEventListener2) {
                ((SensorEventListener2) this.mListener).onFlushCompleted((Sensor) SystemSensorManager.sHandleToSensor.get(handle));
            }
        }
    }

    /* access modifiers changed from: package-private */
    public static final class TriggerEventQueue extends BaseEventQueue {
        private final TriggerEventListener mListener;
        private final SparseArray<TriggerEvent> mTriggerEvents = new SparseArray<>();

        public TriggerEventQueue(TriggerEventListener listener, Looper looper, SystemSensorManager manager) {
            super(looper, manager);
            this.mListener = listener;
        }

        @Override // android.hardware.SystemSensorManager.BaseEventQueue
        public void addSensorEvent(Sensor sensor) {
            TriggerEvent t = new TriggerEvent(Sensor.getMaxLengthValuesArray(sensor, this.mManager.mTargetSdkLevel));
            synchronized (this.mTriggerEvents) {
                this.mTriggerEvents.put(sensor.getHandle(), t);
            }
        }

        @Override // android.hardware.SystemSensorManager.BaseEventQueue
        public void removeSensorEvent(Sensor sensor) {
            synchronized (this.mTriggerEvents) {
                this.mTriggerEvents.delete(sensor.getHandle());
            }
        }

        /* access modifiers changed from: protected */
        @Override // android.hardware.SystemSensorManager.BaseEventQueue
        public void dispatchSensorEvent(int handle, float[] values, int accuracy, long timestamp) {
            TriggerEvent t;
            Sensor sensor = (Sensor) SystemSensorManager.sHandleToSensor.get(handle);
            synchronized (this.mTriggerEvents) {
                t = this.mTriggerEvents.get(handle);
            }
            if (t == null) {
                Log.e("SensorManager", "Error: Trigger Event is null for Sensor: " + sensor);
                return;
            }
            System.arraycopy(values, 0, t.values, 0, t.values.length);
            t.timestamp = timestamp;
            t.sensor = sensor;
            this.mManager.cancelTriggerSensorImpl(this.mListener, sensor, false);
            this.mListener.onTrigger(t);
        }

        /* access modifiers changed from: protected */
        @Override // android.hardware.SystemSensorManager.BaseEventQueue
        public void dispatchFlushCompleteEvent(int handle) {
        }
    }
}

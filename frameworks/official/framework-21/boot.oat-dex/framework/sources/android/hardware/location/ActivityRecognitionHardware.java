package android.hardware.location;

import android.content.Context;
import android.hardware.location.IActivityRecognitionHardware;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

public class ActivityRecognitionHardware extends IActivityRecognitionHardware.Stub {
    private static final String HARDWARE_PERMISSION = "android.permission.LOCATION_HARDWARE";
    private static final int INVALID_ACTIVITY_TYPE = -1;
    private static final int NATIVE_SUCCESS_RESULT = 0;
    private static final String TAG = "ActivityRecognitionHardware";
    private static ActivityRecognitionHardware sSingletonInstance = null;
    private static final Object sSingletonInstanceLock = new Object();
    private final Context mContext;
    private final RemoteCallbackList<IActivityRecognitionHardwareSink> mSinks = new RemoteCallbackList<>();
    private final String[] mSupportedActivities;

    private static native void nativeClassInit();

    private native int nativeDisableActivityEvent(int i, int i2);

    private native int nativeEnableActivityEvent(int i, int i2, long j);

    private native int nativeFlush();

    private native String[] nativeGetSupportedActivities();

    private native void nativeInitialize();

    private static native boolean nativeIsSupported();

    private native void nativeRelease();

    static {
        nativeClassInit();
    }

    private static class Event {
        public int activity;
        public long timestamp;
        public int type;

        private Event() {
        }
    }

    private ActivityRecognitionHardware(Context context) {
        nativeInitialize();
        this.mContext = context;
        this.mSupportedActivities = fetchSupportedActivities();
    }

    public static ActivityRecognitionHardware getInstance(Context context) {
        ActivityRecognitionHardware activityRecognitionHardware;
        synchronized (sSingletonInstanceLock) {
            if (sSingletonInstance == null) {
                sSingletonInstance = new ActivityRecognitionHardware(context);
            }
            activityRecognitionHardware = sSingletonInstance;
        }
        return activityRecognitionHardware;
    }

    public static boolean isSupported() {
        return nativeIsSupported();
    }

    @Override // android.hardware.location.IActivityRecognitionHardware
    public String[] getSupportedActivities() {
        checkPermissions();
        return this.mSupportedActivities;
    }

    @Override // android.hardware.location.IActivityRecognitionHardware
    public boolean isActivitySupported(String activity) {
        checkPermissions();
        return getActivityType(activity) != -1;
    }

    @Override // android.hardware.location.IActivityRecognitionHardware
    public boolean registerSink(IActivityRecognitionHardwareSink sink) {
        checkPermissions();
        return this.mSinks.register(sink);
    }

    @Override // android.hardware.location.IActivityRecognitionHardware
    public boolean unregisterSink(IActivityRecognitionHardwareSink sink) {
        checkPermissions();
        return this.mSinks.unregister(sink);
    }

    @Override // android.hardware.location.IActivityRecognitionHardware
    public boolean enableActivityEvent(String activity, int eventType, long reportLatencyNs) {
        checkPermissions();
        int activityType = getActivityType(activity);
        if (activityType != -1 && nativeEnableActivityEvent(activityType, eventType, reportLatencyNs) == 0) {
            return true;
        }
        return false;
    }

    @Override // android.hardware.location.IActivityRecognitionHardware
    public boolean disableActivityEvent(String activity, int eventType) {
        checkPermissions();
        int activityType = getActivityType(activity);
        if (activityType != -1 && nativeDisableActivityEvent(activityType, eventType) == 0) {
            return true;
        }
        return false;
    }

    @Override // android.hardware.location.IActivityRecognitionHardware
    public boolean flush() {
        checkPermissions();
        return nativeFlush() == 0;
    }

    private void onActivityChanged(Event[] events) {
        if (events == null || events.length == 0) {
            Log.d(TAG, "No events to broadcast for onActivityChanged.");
            return;
        }
        int eventsLength = events.length;
        ActivityRecognitionEvent[] activityRecognitionEventArray = new ActivityRecognitionEvent[eventsLength];
        for (int i = 0; i < eventsLength; i++) {
            Event event = events[i];
            activityRecognitionEventArray[i] = new ActivityRecognitionEvent(getActivityName(event.activity), event.type, event.timestamp);
        }
        ActivityChangedEvent activityChangedEvent = new ActivityChangedEvent(activityRecognitionEventArray);
        int size = this.mSinks.beginBroadcast();
        for (int i2 = 0; i2 < size; i2++) {
            try {
                this.mSinks.getBroadcastItem(i2).onActivityChanged(activityChangedEvent);
            } catch (RemoteException e) {
                Log.e(TAG, "Error delivering activity changed event.", e);
            }
        }
        this.mSinks.finishBroadcast();
    }

    private String getActivityName(int activityType) {
        if (activityType >= 0 && activityType < this.mSupportedActivities.length) {
            return this.mSupportedActivities[activityType];
        }
        Log.e(TAG, String.format("Invalid ActivityType: %d, SupportedActivities: %d", Integer.valueOf(activityType), Integer.valueOf(this.mSupportedActivities.length)));
        return null;
    }

    private int getActivityType(String activity) {
        if (TextUtils.isEmpty(activity)) {
            return -1;
        }
        int supportedActivitiesLength = this.mSupportedActivities.length;
        for (int i = 0; i < supportedActivitiesLength; i++) {
            if (activity.equals(this.mSupportedActivities[i])) {
                return i;
            }
        }
        return -1;
    }

    private void checkPermissions() {
        this.mContext.enforceCallingPermission("android.permission.LOCATION_HARDWARE", String.format("Permission '%s' not granted to access ActivityRecognitionHardware", "android.permission.LOCATION_HARDWARE"));
    }

    private String[] fetchSupportedActivities() {
        String[] supportedActivities = nativeGetSupportedActivities();
        return supportedActivities != null ? supportedActivities : new String[0];
    }
}

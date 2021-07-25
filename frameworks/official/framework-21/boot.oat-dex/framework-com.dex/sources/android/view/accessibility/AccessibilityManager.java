package android.view.accessibility;

import android.Manifest;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.util.Log;
import android.view.IWindow;
import android.view.accessibility.IAccessibilityManager;
import android.view.accessibility.IAccessibilityManagerClient;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class AccessibilityManager {
    public static final int DALTONIZER_CORRECT_DEUTERANOMALY = 12;
    public static final int DALTONIZER_DISABLED = -1;
    public static final int DALTONIZER_SIMULATE_MONOCHROMACY = 0;
    private static final boolean DEBUG = false;
    private static final String LOG_TAG = "AccessibilityManager";
    public static final int STATE_FLAG_ACCESSIBILITY_ENABLED = 1;
    public static final int STATE_FLAG_HIGH_TEXT_CONTRAST_ENABLED = 4;
    public static final int STATE_FLAG_TOUCH_EXPLORATION_ENABLED = 2;
    private static AccessibilityManager sInstance;
    static final Object sInstanceSync = new Object();
    private final CopyOnWriteArrayList<AccessibilityStateChangeListener> mAccessibilityStateChangeListeners = new CopyOnWriteArrayList<>();
    private final IAccessibilityManagerClient.Stub mClient = new IAccessibilityManagerClient.Stub() {
        /* class android.view.accessibility.AccessibilityManager.AnonymousClass1 */

        @Override // android.view.accessibility.IAccessibilityManagerClient
        public void setState(int state) {
            AccessibilityManager.this.mHandler.obtainMessage(4, state, 0).sendToTarget();
        }
    };
    final Handler mHandler;
    private final CopyOnWriteArrayList<HighTextContrastChangeListener> mHighTextContrastStateChangeListeners = new CopyOnWriteArrayList<>();
    boolean mIsEnabled;
    boolean mIsHighTextContrastEnabled;
    boolean mIsTouchExplorationEnabled;
    private final Object mLock = new Object();
    private IAccessibilityManager mService;
    private final CopyOnWriteArrayList<TouchExplorationStateChangeListener> mTouchExplorationStateChangeListeners = new CopyOnWriteArrayList<>();
    final int mUserId;

    public interface AccessibilityStateChangeListener {
        void onAccessibilityStateChanged(boolean z);
    }

    public interface HighTextContrastChangeListener {
        void onHighTextContrastStateChanged(boolean z);
    }

    public interface TouchExplorationStateChangeListener {
        void onTouchExplorationStateChanged(boolean z);
    }

    public static AccessibilityManager getInstance(Context context) {
        int userId;
        synchronized (sInstanceSync) {
            if (sInstance == null) {
                if (Binder.getCallingUid() == 1000 || context.checkCallingOrSelfPermission(Manifest.permission.INTERACT_ACROSS_USERS) == 0 || context.checkCallingOrSelfPermission(Manifest.permission.INTERACT_ACROSS_USERS_FULL) == 0) {
                    userId = -2;
                } else {
                    userId = UserHandle.myUserId();
                }
                IBinder iBinder = ServiceManager.getService(Context.ACCESSIBILITY_SERVICE);
                sInstance = new AccessibilityManager(context, iBinder == null ? null : IAccessibilityManager.Stub.asInterface(iBinder), userId);
            }
        }
        return sInstance;
    }

    public AccessibilityManager(Context context, IAccessibilityManager service, int userId) {
        this.mHandler = new MyHandler(context.getMainLooper());
        this.mService = service;
        this.mUserId = userId;
        synchronized (this.mLock) {
            tryConnectToServiceLocked();
        }
    }

    public IAccessibilityManagerClient getClient() {
        return this.mClient;
    }

    public boolean isEnabled() {
        boolean z;
        synchronized (this.mLock) {
            if (getServiceLocked() == null) {
                z = false;
            } else {
                z = this.mIsEnabled;
            }
        }
        return z;
    }

    public boolean isTouchExplorationEnabled() {
        boolean z;
        synchronized (this.mLock) {
            if (getServiceLocked() == null) {
                z = false;
            } else {
                z = this.mIsTouchExplorationEnabled;
            }
        }
        return z;
    }

    public boolean isHighTextContrastEnabled() {
        boolean z;
        synchronized (this.mLock) {
            if (getServiceLocked() == null) {
                z = false;
            } else {
                z = this.mIsHighTextContrastEnabled;
            }
        }
        return z;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x001d, code lost:
        r0 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:?, code lost:
        r10.setEventTime(android.os.SystemClock.uptimeMillis());
        r2 = android.os.Binder.clearCallingIdentity();
        r0 = r4.sendAccessibilityEvent(r10, r5);
        android.os.Binder.restoreCallingIdentity(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0030, code lost:
        if (r0 == false) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0032, code lost:
        r10.recycle();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0036, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:?, code lost:
        android.util.Log.e(android.view.accessibility.AccessibilityManager.LOG_TAG, "Error during sending " + r10 + " ", r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0055, code lost:
        if (r0 != false) goto L_0x0057;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0057, code lost:
        r10.recycle();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x005b, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x005c, code lost:
        if (r0 != false) goto L_0x005e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x005e, code lost:
        r10.recycle();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0061, code lost:
        throw r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:?, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void sendAccessibilityEvent(android.view.accessibility.AccessibilityEvent r10) {
        /*
            r9 = this;
            java.lang.Object r7 = r9.mLock
            monitor-enter(r7)
            android.view.accessibility.IAccessibilityManager r4 = r9.getServiceLocked()     // Catch:{ all -> 0x0017 }
            if (r4 != 0) goto L_0x000b
            monitor-exit(r7)     // Catch:{ all -> 0x0017 }
        L_0x000a:
            return
        L_0x000b:
            boolean r6 = r9.mIsEnabled     // Catch:{ all -> 0x0017 }
            if (r6 != 0) goto L_0x001a
            java.lang.IllegalStateException r6 = new java.lang.IllegalStateException     // Catch:{ all -> 0x0017 }
            java.lang.String r8 = "Accessibility off. Did you forget to check that?"
            r6.<init>(r8)     // Catch:{ all -> 0x0017 }
            throw r6     // Catch:{ all -> 0x0017 }
        L_0x0017:
            r6 = move-exception
            monitor-exit(r7)     // Catch:{ all -> 0x0017 }
            throw r6
        L_0x001a:
            int r5 = r9.mUserId
            monitor-exit(r7)
            r0 = 0
            long r6 = android.os.SystemClock.uptimeMillis()     // Catch:{ RemoteException -> 0x0036 }
            r10.setEventTime(r6)     // Catch:{ RemoteException -> 0x0036 }
            long r2 = android.os.Binder.clearCallingIdentity()     // Catch:{ RemoteException -> 0x0036 }
            boolean r0 = r4.sendAccessibilityEvent(r10, r5)     // Catch:{ RemoteException -> 0x0036 }
            android.os.Binder.restoreCallingIdentity(r2)     // Catch:{ RemoteException -> 0x0036 }
            if (r0 == 0) goto L_0x000a
            r10.recycle()
            goto L_0x000a
        L_0x0036:
            r1 = move-exception
            java.lang.String r6 = "AccessibilityManager"
            java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ all -> 0x005b }
            r7.<init>()     // Catch:{ all -> 0x005b }
            java.lang.String r8 = "Error during sending "
            java.lang.StringBuilder r7 = r7.append(r8)     // Catch:{ all -> 0x005b }
            java.lang.StringBuilder r7 = r7.append(r10)     // Catch:{ all -> 0x005b }
            java.lang.String r8 = " "
            java.lang.StringBuilder r7 = r7.append(r8)     // Catch:{ all -> 0x005b }
            java.lang.String r7 = r7.toString()     // Catch:{ all -> 0x005b }
            android.util.Log.e(r6, r7, r1)     // Catch:{ all -> 0x005b }
            if (r0 == 0) goto L_0x000a
            r10.recycle()
            goto L_0x000a
        L_0x005b:
            r6 = move-exception
            if (r0 == 0) goto L_0x0061
            r10.recycle()
        L_0x0061:
            throw r6
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.accessibility.AccessibilityManager.sendAccessibilityEvent(android.view.accessibility.AccessibilityEvent):void");
    }

    public void interrupt() {
        synchronized (this.mLock) {
            IAccessibilityManager service = getServiceLocked();
            if (service != null) {
                if (!this.mIsEnabled) {
                    throw new IllegalStateException("Accessibility off. Did you forget to check that?");
                }
                int userId = this.mUserId;
                try {
                    service.interrupt(userId);
                } catch (RemoteException re) {
                    Log.e(LOG_TAG, "Error while requesting interrupt from all services. ", re);
                }
            }
        }
    }

    @Deprecated
    public List<ServiceInfo> getAccessibilityServiceList() {
        List<AccessibilityServiceInfo> infos = getInstalledAccessibilityServiceList();
        List<ServiceInfo> services = new ArrayList<>();
        int infoCount = infos.size();
        for (int i = 0; i < infoCount; i++) {
            services.add(infos.get(i).getResolveInfo().serviceInfo);
        }
        return Collections.unmodifiableList(services);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:?, code lost:
        r2 = r1.getInstalledAccessibilityServiceList(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0021, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0022, code lost:
        android.util.Log.e(android.view.accessibility.AccessibilityManager.LOG_TAG, "Error while obtaining the installed AccessibilityServices. ", r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0012, code lost:
        r2 = null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.util.List<android.accessibilityservice.AccessibilityServiceInfo> getInstalledAccessibilityServiceList() {
        /*
            r6 = this;
            java.lang.Object r5 = r6.mLock
            monitor-enter(r5)
            android.view.accessibility.IAccessibilityManager r1 = r6.getServiceLocked()     // Catch:{ all -> 0x001e }
            if (r1 != 0) goto L_0x000f
            java.util.List r4 = java.util.Collections.emptyList()     // Catch:{ all -> 0x001e }
            monitor-exit(r5)     // Catch:{ all -> 0x001e }
        L_0x000e:
            return r4
        L_0x000f:
            int r3 = r6.mUserId     // Catch:{ all -> 0x001e }
            monitor-exit(r5)     // Catch:{ all -> 0x001e }
            r2 = 0
            java.util.List r2 = r1.getInstalledAccessibilityServiceList(r3)     // Catch:{ RemoteException -> 0x0021 }
        L_0x0017:
            if (r2 == 0) goto L_0x002a
            java.util.List r4 = java.util.Collections.unmodifiableList(r2)
            goto L_0x000e
        L_0x001e:
            r4 = move-exception
            monitor-exit(r5)
            throw r4
        L_0x0021:
            r0 = move-exception
            java.lang.String r4 = "AccessibilityManager"
            java.lang.String r5 = "Error while obtaining the installed AccessibilityServices. "
            android.util.Log.e(r4, r5, r0)
            goto L_0x0017
        L_0x002a:
            java.util.List r4 = java.util.Collections.emptyList()
            goto L_0x000e
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.accessibility.AccessibilityManager.getInstalledAccessibilityServiceList():java.util.List");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:?, code lost:
        r2 = r1.getEnabledAccessibilityServiceList(r7, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0021, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0022, code lost:
        android.util.Log.e(android.view.accessibility.AccessibilityManager.LOG_TAG, "Error while obtaining the installed AccessibilityServices. ", r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0012, code lost:
        r2 = null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.util.List<android.accessibilityservice.AccessibilityServiceInfo> getEnabledAccessibilityServiceList(int r7) {
        /*
            r6 = this;
            java.lang.Object r5 = r6.mLock
            monitor-enter(r5)
            android.view.accessibility.IAccessibilityManager r1 = r6.getServiceLocked()     // Catch:{ all -> 0x001e }
            if (r1 != 0) goto L_0x000f
            java.util.List r4 = java.util.Collections.emptyList()     // Catch:{ all -> 0x001e }
            monitor-exit(r5)     // Catch:{ all -> 0x001e }
        L_0x000e:
            return r4
        L_0x000f:
            int r3 = r6.mUserId     // Catch:{ all -> 0x001e }
            monitor-exit(r5)     // Catch:{ all -> 0x001e }
            r2 = 0
            java.util.List r2 = r1.getEnabledAccessibilityServiceList(r7, r3)     // Catch:{ RemoteException -> 0x0021 }
        L_0x0017:
            if (r2 == 0) goto L_0x002a
            java.util.List r4 = java.util.Collections.unmodifiableList(r2)
            goto L_0x000e
        L_0x001e:
            r4 = move-exception
            monitor-exit(r5)
            throw r4
        L_0x0021:
            r0 = move-exception
            java.lang.String r4 = "AccessibilityManager"
            java.lang.String r5 = "Error while obtaining the installed AccessibilityServices. "
            android.util.Log.e(r4, r5, r0)
            goto L_0x0017
        L_0x002a:
            java.util.List r4 = java.util.Collections.emptyList()
            goto L_0x000e
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.accessibility.AccessibilityManager.getEnabledAccessibilityServiceList(int):java.util.List");
    }

    public boolean addAccessibilityStateChangeListener(AccessibilityStateChangeListener listener) {
        return this.mAccessibilityStateChangeListeners.add(listener);
    }

    public boolean removeAccessibilityStateChangeListener(AccessibilityStateChangeListener listener) {
        return this.mAccessibilityStateChangeListeners.remove(listener);
    }

    public boolean addTouchExplorationStateChangeListener(TouchExplorationStateChangeListener listener) {
        return this.mTouchExplorationStateChangeListeners.add(listener);
    }

    public boolean removeTouchExplorationStateChangeListener(TouchExplorationStateChangeListener listener) {
        return this.mTouchExplorationStateChangeListeners.remove(listener);
    }

    public boolean addHighTextContrastStateChangeListener(HighTextContrastChangeListener listener) {
        return this.mHighTextContrastStateChangeListeners.add(listener);
    }

    public boolean removeHighTextContrastStateChangeListener(HighTextContrastChangeListener listener) {
        return this.mHighTextContrastStateChangeListeners.remove(listener);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setStateLocked(int stateFlags) {
        boolean enabled;
        boolean touchExplorationEnabled;
        boolean highTextContrastEnabled = false;
        if ((stateFlags & 1) != 0) {
            enabled = true;
        } else {
            enabled = false;
        }
        if ((stateFlags & 2) != 0) {
            touchExplorationEnabled = true;
        } else {
            touchExplorationEnabled = false;
        }
        if ((stateFlags & 4) != 0) {
            highTextContrastEnabled = true;
        }
        boolean wasEnabled = this.mIsEnabled;
        boolean wasTouchExplorationEnabled = this.mIsTouchExplorationEnabled;
        boolean wasHighTextContrastEnabled = this.mIsHighTextContrastEnabled;
        this.mIsEnabled = enabled;
        this.mIsTouchExplorationEnabled = touchExplorationEnabled;
        this.mIsHighTextContrastEnabled = highTextContrastEnabled;
        if (wasEnabled != enabled) {
            this.mHandler.sendEmptyMessage(1);
        }
        if (wasTouchExplorationEnabled != touchExplorationEnabled) {
            this.mHandler.sendEmptyMessage(2);
        }
        if (wasHighTextContrastEnabled != highTextContrastEnabled) {
            this.mHandler.sendEmptyMessage(3);
        }
    }

    public int addAccessibilityInteractionConnection(IWindow windowToken, IAccessibilityInteractionConnection connection) {
        synchronized (this.mLock) {
            IAccessibilityManager service = getServiceLocked();
            if (service == null) {
                return -1;
            }
            int userId = this.mUserId;
            try {
                return service.addAccessibilityInteractionConnection(windowToken, connection, userId);
            } catch (RemoteException re) {
                Log.e(LOG_TAG, "Error while adding an accessibility interaction connection. ", re);
                return -1;
            }
        }
    }

    public void removeAccessibilityInteractionConnection(IWindow windowToken) {
        synchronized (this.mLock) {
            IAccessibilityManager service = getServiceLocked();
            if (service != null) {
                try {
                    service.removeAccessibilityInteractionConnection(windowToken);
                } catch (RemoteException re) {
                    Log.e(LOG_TAG, "Error while removing an accessibility interaction connection. ", re);
                }
            }
        }
    }

    private IAccessibilityManager getServiceLocked() {
        if (this.mService == null) {
            tryConnectToServiceLocked();
        }
        return this.mService;
    }

    private void tryConnectToServiceLocked() {
        IBinder iBinder = ServiceManager.getService(Context.ACCESSIBILITY_SERVICE);
        if (iBinder != null) {
            IAccessibilityManager service = IAccessibilityManager.Stub.asInterface(iBinder);
            try {
                setStateLocked(service.addClient(this.mClient, this.mUserId));
                this.mService = service;
            } catch (RemoteException re) {
                Log.e(LOG_TAG, "AccessibilityManagerService is dead", re);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleNotifyAccessibilityStateChanged() {
        boolean isEnabled;
        synchronized (this.mLock) {
            isEnabled = this.mIsEnabled;
        }
        int listenerCount = this.mAccessibilityStateChangeListeners.size();
        for (int i = 0; i < listenerCount; i++) {
            this.mAccessibilityStateChangeListeners.get(i).onAccessibilityStateChanged(isEnabled);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleNotifyTouchExplorationStateChanged() {
        boolean isTouchExplorationEnabled;
        synchronized (this.mLock) {
            isTouchExplorationEnabled = this.mIsTouchExplorationEnabled;
        }
        int listenerCount = this.mTouchExplorationStateChangeListeners.size();
        for (int i = 0; i < listenerCount; i++) {
            this.mTouchExplorationStateChangeListeners.get(i).onTouchExplorationStateChanged(isTouchExplorationEnabled);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleNotifyHighTextContrastStateChanged() {
        boolean isHighTextContrastEnabled;
        synchronized (this.mLock) {
            isHighTextContrastEnabled = this.mIsHighTextContrastEnabled;
        }
        int listenerCount = this.mHighTextContrastStateChangeListeners.size();
        for (int i = 0; i < listenerCount; i++) {
            this.mHighTextContrastStateChangeListeners.get(i).onHighTextContrastStateChanged(isHighTextContrastEnabled);
        }
    }

    private final class MyHandler extends Handler {
        public static final int MSG_NOTIFY_ACCESSIBILITY_STATE_CHANGED = 1;
        public static final int MSG_NOTIFY_EXPLORATION_STATE_CHANGED = 2;
        public static final int MSG_NOTIFY_HIGH_TEXT_CONTRAST_STATE_CHANGED = 3;
        public static final int MSG_SET_STATE = 4;

        public MyHandler(Looper looper) {
            super(looper, null, false);
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    AccessibilityManager.this.handleNotifyAccessibilityStateChanged();
                    return;
                case 2:
                    AccessibilityManager.this.handleNotifyTouchExplorationStateChanged();
                    return;
                case 3:
                    AccessibilityManager.this.handleNotifyHighTextContrastStateChanged();
                    return;
                case 4:
                    int state = message.arg1;
                    synchronized (AccessibilityManager.this.mLock) {
                        AccessibilityManager.this.setStateLocked(state);
                    }
                    return;
                default:
                    return;
            }
        }
    }
}

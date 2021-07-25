package android.app;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.IAccessibilityServiceClient;
import android.accessibilityservice.IAccessibilityServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.hardware.display.DisplayManagerGlobal;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import android.view.Display;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.WindowAnimationFrameStats;
import android.view.WindowContentFrameStats;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityInteractionClient;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import libcore.io.IoUtils;

public final class UiAutomation {
    private static final int CONNECTION_ID_UNDEFINED = -1;
    private static final long CONNECT_TIMEOUT_MILLIS = 5000;
    private static final boolean DEBUG = false;
    private static final String LOG_TAG = UiAutomation.class.getSimpleName();
    public static final int ROTATION_FREEZE_0 = 0;
    public static final int ROTATION_FREEZE_180 = 2;
    public static final int ROTATION_FREEZE_270 = 3;
    public static final int ROTATION_FREEZE_90 = 1;
    public static final int ROTATION_FREEZE_CURRENT = -1;
    public static final int ROTATION_UNFREEZE = -2;
    private final IAccessibilityServiceClient mClient;
    private int mConnectionId = -1;
    private final ArrayList<AccessibilityEvent> mEventQueue = new ArrayList<>();
    private boolean mIsConnecting;
    private long mLastEventTimeMillis;
    private final Object mLock = new Object();
    private OnAccessibilityEventListener mOnAccessibilityEventListener;
    private final IUiAutomationConnection mUiAutomationConnection;
    private boolean mWaitingForEventDelivery;

    public interface AccessibilityEventFilter {
        boolean accept(AccessibilityEvent accessibilityEvent);
    }

    public interface OnAccessibilityEventListener {
        void onAccessibilityEvent(AccessibilityEvent accessibilityEvent);
    }

    public UiAutomation(Looper looper, IUiAutomationConnection connection) {
        if (looper == null) {
            throw new IllegalArgumentException("Looper cannot be null!");
        } else if (connection == null) {
            throw new IllegalArgumentException("Connection cannot be null!");
        } else {
            this.mUiAutomationConnection = connection;
            this.mClient = new IAccessibilityServiceClientImpl(looper);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0017, code lost:
        r8 = r12.mLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0019, code lost:
        monitor-enter(r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:?, code lost:
        r6 = android.os.SystemClock.uptimeMillis();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0022, code lost:
        if (isConnectedLocked() == false) goto L_0x0038;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0027, code lost:
        monitor-exit(r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x002f, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0037, code lost:
        throw new java.lang.RuntimeException("Error while connecting UiAutomation", r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0038, code lost:
        r4 = 5000 - (android.os.SystemClock.uptimeMillis() - r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0046, code lost:
        if (r4 > 0) goto L_0x0055;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x004f, code lost:
        throw new java.lang.RuntimeException("Error while connecting UiAutomation");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0050, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0051, code lost:
        r12.mIsConnecting = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0054, code lost:
        throw r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:?, code lost:
        r12.mLock.wait(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:?, code lost:
        r12.mUiAutomationConnection.connect(r12.mClient);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void connect() {
        /*
            r12 = this;
            java.lang.Object r8 = r12.mLock
            monitor-enter(r8)
            r12.throwIfConnectedLocked()     // Catch:{ all -> 0x002c }
            boolean r3 = r12.mIsConnecting     // Catch:{ all -> 0x002c }
            if (r3 == 0) goto L_0x000c
            monitor-exit(r8)     // Catch:{ all -> 0x002c }
        L_0x000b:
            return
        L_0x000c:
            r3 = 1
            r12.mIsConnecting = r3     // Catch:{ all -> 0x002c }
            monitor-exit(r8)     // Catch:{ all -> 0x002c }
            android.app.IUiAutomationConnection r3 = r12.mUiAutomationConnection     // Catch:{ RemoteException -> 0x002f }
            android.accessibilityservice.IAccessibilityServiceClient r8 = r12.mClient     // Catch:{ RemoteException -> 0x002f }
            r3.connect(r8)     // Catch:{ RemoteException -> 0x002f }
            java.lang.Object r8 = r12.mLock
            monitor-enter(r8)
            long r6 = android.os.SystemClock.uptimeMillis()     // Catch:{ all -> 0x0029 }
        L_0x001e:
            boolean r3 = r12.isConnectedLocked()     // Catch:{ all -> 0x0050 }
            if (r3 == 0) goto L_0x0038
            r3 = 0
            r12.mIsConnecting = r3
            monitor-exit(r8)
            goto L_0x000b
        L_0x0029:
            r3 = move-exception
            monitor-exit(r8)
            throw r3
        L_0x002c:
            r3 = move-exception
            monitor-exit(r8)
            throw r3
        L_0x002f:
            r2 = move-exception
            java.lang.RuntimeException r3 = new java.lang.RuntimeException
            java.lang.String r8 = "Error while connecting UiAutomation"
            r3.<init>(r8, r2)
            throw r3
        L_0x0038:
            long r10 = android.os.SystemClock.uptimeMillis()
            long r0 = r10 - r6
            r10 = 5000(0x1388, double:2.4703E-320)
            long r4 = r10 - r0
            r10 = 0
            int r3 = (r4 > r10 ? 1 : (r4 == r10 ? 0 : -1))
            if (r3 > 0) goto L_0x0055
            java.lang.RuntimeException r3 = new java.lang.RuntimeException
            java.lang.String r9 = "Error while connecting UiAutomation"
            r3.<init>(r9)
            throw r3
        L_0x0050:
            r3 = move-exception
            r9 = 0
            r12.mIsConnecting = r9
            throw r3
        L_0x0055:
            java.lang.Object r3 = r12.mLock     // Catch:{ InterruptedException -> 0x005b }
            r3.wait(r4)     // Catch:{ InterruptedException -> 0x005b }
            goto L_0x001e
        L_0x005b:
            r3 = move-exception
            goto L_0x001e
        */
        throw new UnsupportedOperationException("Method not decompiled: android.app.UiAutomation.connect():void");
    }

    public void disconnect() {
        synchronized (this.mLock) {
            if (this.mIsConnecting) {
                throw new IllegalStateException("Cannot call disconnect() while connecting!");
            }
            throwIfNotConnectedLocked();
            this.mConnectionId = -1;
        }
        try {
            this.mUiAutomationConnection.disconnect();
        } catch (RemoteException re) {
            throw new RuntimeException("Error while disconnecting UiAutomation", re);
        }
    }

    public int getConnectionId() {
        int i;
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
            i = this.mConnectionId;
        }
        return i;
    }

    public void setOnAccessibilityEventListener(OnAccessibilityEventListener listener) {
        synchronized (this.mLock) {
            this.mOnAccessibilityEventListener = listener;
        }
    }

    public final boolean performGlobalAction(int action) {
        IAccessibilityServiceConnection connection;
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
            connection = AccessibilityInteractionClient.getInstance().getConnection(this.mConnectionId);
        }
        if (connection != null) {
            try {
                return connection.performGlobalAction(action);
            } catch (RemoteException re) {
                Log.w(LOG_TAG, "Error while calling performGlobalAction", re);
            }
        }
        return false;
    }

    public AccessibilityNodeInfo findFocus(int focus) {
        return AccessibilityInteractionClient.getInstance().findFocus(this.mConnectionId, -2, AccessibilityNodeInfo.ROOT_NODE_ID, focus);
    }

    public final AccessibilityServiceInfo getServiceInfo() {
        IAccessibilityServiceConnection connection;
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
            connection = AccessibilityInteractionClient.getInstance().getConnection(this.mConnectionId);
        }
        if (connection != null) {
            try {
                return connection.getServiceInfo();
            } catch (RemoteException re) {
                Log.w(LOG_TAG, "Error while getting AccessibilityServiceInfo", re);
            }
        }
        return null;
    }

    public final void setServiceInfo(AccessibilityServiceInfo info) {
        IAccessibilityServiceConnection connection;
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
            AccessibilityInteractionClient.getInstance().clearCache();
            connection = AccessibilityInteractionClient.getInstance().getConnection(this.mConnectionId);
        }
        if (connection != null) {
            try {
                connection.setServiceInfo(info);
            } catch (RemoteException re) {
                Log.w(LOG_TAG, "Error while setting AccessibilityServiceInfo", re);
            }
        }
    }

    public List<AccessibilityWindowInfo> getWindows() {
        int connectionId;
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
            connectionId = this.mConnectionId;
        }
        return AccessibilityInteractionClient.getInstance().getWindows(connectionId);
    }

    public AccessibilityNodeInfo getRootInActiveWindow() {
        int connectionId;
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
            connectionId = this.mConnectionId;
        }
        return AccessibilityInteractionClient.getInstance().getRootInActiveWindow(connectionId);
    }

    public boolean injectInputEvent(InputEvent event, boolean sync) {
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
        }
        try {
            return this.mUiAutomationConnection.injectInputEvent(event, sync);
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "Error while injecting input event!", re);
            return false;
        }
    }

    public boolean setRotation(int rotation) {
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
        }
        switch (rotation) {
            case -2:
            case -1:
            case 0:
            case 1:
            case 2:
            case 3:
                try {
                    this.mUiAutomationConnection.setRotation(rotation);
                    return true;
                } catch (RemoteException re) {
                    Log.e(LOG_TAG, "Error while setting rotation!", re);
                    return false;
                }
            default:
                throw new IllegalArgumentException("Invalid rotation.");
        }
    }

    public AccessibilityEvent executeAndWaitForEvent(Runnable command, AccessibilityEventFilter filter, long timeoutMillis) throws TimeoutException {
        AccessibilityEvent event;
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
            this.mEventQueue.clear();
            this.mWaitingForEventDelivery = true;
        }
        long executionStartTimeMillis = SystemClock.uptimeMillis();
        command.run();
        synchronized (this.mLock) {
            try {
                long startTimeMillis = SystemClock.uptimeMillis();
                while (true) {
                    if (!this.mEventQueue.isEmpty()) {
                        event = this.mEventQueue.remove(0);
                        if (event.getEventTime() < executionStartTimeMillis) {
                            continue;
                        } else if (filter.accept(event)) {
                            this.mWaitingForEventDelivery = false;
                            this.mEventQueue.clear();
                            this.mLock.notifyAll();
                        } else {
                            event.recycle();
                        }
                    } else {
                        long remainingTimeMillis = timeoutMillis - (SystemClock.uptimeMillis() - startTimeMillis);
                        if (remainingTimeMillis <= 0) {
                            throw new TimeoutException("Expected event not received within: " + timeoutMillis + " ms.");
                        }
                        try {
                            this.mLock.wait(remainingTimeMillis);
                        } catch (InterruptedException e) {
                        }
                    }
                }
            } catch (Throwable th) {
                this.mWaitingForEventDelivery = false;
                this.mEventQueue.clear();
                this.mLock.notifyAll();
                throw th;
            }
        }
        return event;
    }

    public void waitForIdle(long idleTimeoutMillis, long globalTimeoutMillis) throws TimeoutException {
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
            long startTimeMillis = SystemClock.uptimeMillis();
            if (this.mLastEventTimeMillis <= 0) {
                this.mLastEventTimeMillis = startTimeMillis;
            }
            while (true) {
                long currentTimeMillis = SystemClock.uptimeMillis();
                if (globalTimeoutMillis - (currentTimeMillis - startTimeMillis) <= 0) {
                    throw new TimeoutException("No idle state with idle timeout: " + idleTimeoutMillis + " within global timeout: " + globalTimeoutMillis);
                }
                long remainingIdleTimeMillis = idleTimeoutMillis - (currentTimeMillis - this.mLastEventTimeMillis);
                if (remainingIdleTimeMillis > 0) {
                    try {
                        this.mLock.wait(remainingIdleTimeMillis);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    }

    public Bitmap takeScreenshot() {
        float screenshotWidth;
        float screenshotHeight;
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
        }
        Display display = DisplayManagerGlobal.getInstance().getRealDisplay(0);
        Point displaySize = new Point();
        display.getRealSize(displaySize);
        int displayWidth = displaySize.x;
        int displayHeight = displaySize.y;
        int rotation = display.getRotation();
        switch (rotation) {
            case 0:
                screenshotWidth = (float) displayWidth;
                screenshotHeight = (float) displayHeight;
                break;
            case 1:
                screenshotWidth = (float) displayHeight;
                screenshotHeight = (float) displayWidth;
                break;
            case 2:
                screenshotWidth = (float) displayWidth;
                screenshotHeight = (float) displayHeight;
                break;
            case 3:
                screenshotWidth = (float) displayHeight;
                screenshotHeight = (float) displayWidth;
                break;
            default:
                throw new IllegalArgumentException("Invalid rotation: " + rotation);
        }
        try {
            Bitmap screenShot = this.mUiAutomationConnection.takeScreenshot((int) screenshotWidth, (int) screenshotHeight);
            if (screenShot == null) {
                return null;
            }
            if (rotation != 0) {
                Bitmap unrotatedScreenShot = Bitmap.createBitmap(displayWidth, displayHeight, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(unrotatedScreenShot);
                canvas.translate((float) (unrotatedScreenShot.getWidth() / 2), (float) (unrotatedScreenShot.getHeight() / 2));
                canvas.rotate(getDegreesForRotation(rotation));
                canvas.translate((-screenshotWidth) / 2.0f, (-screenshotHeight) / 2.0f);
                canvas.drawBitmap(screenShot, 0.0f, 0.0f, (Paint) null);
                canvas.setBitmap(null);
                screenShot.recycle();
                screenShot = unrotatedScreenShot;
            }
            screenShot.setHasAlpha(false);
            return screenShot;
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "Error while taking screnshot!", re);
            return null;
        }
    }

    public void setRunAsMonkey(boolean enable) {
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
        }
        try {
            ActivityManagerNative.getDefault().setUserIsMonkey(enable);
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "Error while setting run as monkey!", re);
        }
    }

    public boolean clearWindowContentFrameStats(int windowId) {
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
        }
        try {
            return this.mUiAutomationConnection.clearWindowContentFrameStats(windowId);
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "Error clearing window content frame stats!", re);
            return false;
        }
    }

    public WindowContentFrameStats getWindowContentFrameStats(int windowId) {
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
        }
        try {
            return this.mUiAutomationConnection.getWindowContentFrameStats(windowId);
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "Error getting window content frame stats!", re);
            return null;
        }
    }

    public void clearWindowAnimationFrameStats() {
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
        }
        try {
            this.mUiAutomationConnection.clearWindowAnimationFrameStats();
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "Error clearing window animation frame stats!", re);
        }
    }

    public WindowAnimationFrameStats getWindowAnimationFrameStats() {
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
        }
        try {
            return this.mUiAutomationConnection.getWindowAnimationFrameStats();
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "Error getting window animation frame stats!", re);
            return null;
        }
    }

    public ParcelFileDescriptor executeShellCommand(String command) {
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
        }
        ParcelFileDescriptor source = null;
        ParcelFileDescriptor sink = null;
        try {
            ParcelFileDescriptor[] pipe = ParcelFileDescriptor.createPipe();
            source = pipe[0];
            sink = pipe[1];
            this.mUiAutomationConnection.executeShellCommand(command, sink);
        } catch (IOException ioe) {
            Log.e(LOG_TAG, "Error executing shell command!", ioe);
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "Error executing shell command!", re);
        } finally {
            IoUtils.closeQuietly(sink);
        }
        return source;
    }

    private static float getDegreesForRotation(int value) {
        switch (value) {
            case 1:
                return 270.0f;
            case 2:
                return 180.0f;
            case 3:
                return 90.0f;
            default:
                return 0.0f;
        }
    }

    private boolean isConnectedLocked() {
        return this.mConnectionId != -1;
    }

    private void throwIfConnectedLocked() {
        if (this.mConnectionId != -1) {
            throw new IllegalStateException("UiAutomation not connected!");
        }
    }

    private void throwIfNotConnectedLocked() {
        if (!isConnectedLocked()) {
            throw new IllegalStateException("UiAutomation not connected!");
        }
    }

    private class IAccessibilityServiceClientImpl extends AccessibilityService.IAccessibilityServiceClientWrapper {
        public IAccessibilityServiceClientImpl(Looper looper) {
            super(null, looper, new AccessibilityService.Callbacks() {
                /* class android.app.UiAutomation.IAccessibilityServiceClientImpl.AnonymousClass1 */

                @Override // android.accessibilityservice.AccessibilityService.Callbacks
                public void onSetConnectionId(int connectionId) {
                    synchronized (UiAutomation.this.mLock) {
                        UiAutomation.this.mConnectionId = connectionId;
                        UiAutomation.this.mLock.notifyAll();
                    }
                }

                @Override // android.accessibilityservice.AccessibilityService.Callbacks
                public void onServiceConnected() {
                }

                @Override // android.accessibilityservice.AccessibilityService.Callbacks
                public void onInterrupt() {
                }

                @Override // android.accessibilityservice.AccessibilityService.Callbacks
                public boolean onGesture(int gestureId) {
                    return false;
                }

                @Override // android.accessibilityservice.AccessibilityService.Callbacks
                public void onAccessibilityEvent(AccessibilityEvent event) {
                    synchronized (UiAutomation.this.mLock) {
                        UiAutomation.this.mLastEventTimeMillis = event.getEventTime();
                        if (UiAutomation.this.mWaitingForEventDelivery) {
                            UiAutomation.this.mEventQueue.add(AccessibilityEvent.obtain(event));
                        }
                        UiAutomation.this.mLock.notifyAll();
                    }
                    OnAccessibilityEventListener listener = UiAutomation.this.mOnAccessibilityEventListener;
                    if (listener != null) {
                        listener.onAccessibilityEvent(AccessibilityEvent.obtain(event));
                    }
                }

                @Override // android.accessibilityservice.AccessibilityService.Callbacks
                public boolean onKeyEvent(KeyEvent event) {
                    return false;
                }
            });
        }
    }
}

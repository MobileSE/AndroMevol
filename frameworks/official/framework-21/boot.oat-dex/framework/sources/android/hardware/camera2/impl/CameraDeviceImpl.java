package android.hardware.camera2.impl;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.ICameraDeviceCallbacks;
import android.hardware.camera2.ICameraDeviceUser;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.utils.CameraBinderDecorator;
import android.hardware.camera2.utils.CameraRuntimeException;
import android.hardware.camera2.utils.LongParcelable;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.util.SparseArray;
import android.view.Surface;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

public class CameraDeviceImpl extends CameraDevice {
    private static final int REQUEST_ID_NONE = -1;
    private final boolean DEBUG;
    private final String TAG;
    private final Runnable mCallOnActive = new Runnable() {
        /* class android.hardware.camera2.impl.CameraDeviceImpl.AnonymousClass3 */

        /* JADX WARNING: Code restructure failed: missing block: B:14:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:15:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:8:0x0017, code lost:
            if (r0 == null) goto L_?;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:9:0x0019, code lost:
            r0.onActive(r3.this$0);
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            /*
                r3 = this;
                r0 = 0
                android.hardware.camera2.impl.CameraDeviceImpl r1 = android.hardware.camera2.impl.CameraDeviceImpl.this
                java.lang.Object r2 = r1.mInterfaceLock
                monitor-enter(r2)
                android.hardware.camera2.impl.CameraDeviceImpl r1 = android.hardware.camera2.impl.CameraDeviceImpl.this     // Catch:{ all -> 0x001f }
                android.hardware.camera2.ICameraDeviceUser r1 = android.hardware.camera2.impl.CameraDeviceImpl.access$000(r1)     // Catch:{ all -> 0x001f }
                if (r1 != 0) goto L_0x0010
                monitor-exit(r2)     // Catch:{ all -> 0x001f }
            L_0x000f:
                return
            L_0x0010:
                android.hardware.camera2.impl.CameraDeviceImpl r1 = android.hardware.camera2.impl.CameraDeviceImpl.this     // Catch:{ all -> 0x001f }
                android.hardware.camera2.impl.CameraDeviceImpl$StateCallbackKK r0 = android.hardware.camera2.impl.CameraDeviceImpl.access$100(r1)     // Catch:{ all -> 0x001f }
                monitor-exit(r2)     // Catch:{ all -> 0x001f }
                if (r0 == 0) goto L_0x000f
                android.hardware.camera2.impl.CameraDeviceImpl r1 = android.hardware.camera2.impl.CameraDeviceImpl.this
                r0.onActive(r1)
                goto L_0x000f
            L_0x001f:
                r1 = move-exception
                monitor-exit(r2)
                throw r1
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.impl.CameraDeviceImpl.AnonymousClass3.run():void");
        }
    };
    private final Runnable mCallOnBusy = new Runnable() {
        /* class android.hardware.camera2.impl.CameraDeviceImpl.AnonymousClass4 */

        /* JADX WARNING: Code restructure failed: missing block: B:14:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:15:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:8:0x0017, code lost:
            if (r0 == null) goto L_?;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:9:0x0019, code lost:
            r0.onBusy(r3.this$0);
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            /*
                r3 = this;
                r0 = 0
                android.hardware.camera2.impl.CameraDeviceImpl r1 = android.hardware.camera2.impl.CameraDeviceImpl.this
                java.lang.Object r2 = r1.mInterfaceLock
                monitor-enter(r2)
                android.hardware.camera2.impl.CameraDeviceImpl r1 = android.hardware.camera2.impl.CameraDeviceImpl.this     // Catch:{ all -> 0x001f }
                android.hardware.camera2.ICameraDeviceUser r1 = android.hardware.camera2.impl.CameraDeviceImpl.access$000(r1)     // Catch:{ all -> 0x001f }
                if (r1 != 0) goto L_0x0010
                monitor-exit(r2)     // Catch:{ all -> 0x001f }
            L_0x000f:
                return
            L_0x0010:
                android.hardware.camera2.impl.CameraDeviceImpl r1 = android.hardware.camera2.impl.CameraDeviceImpl.this     // Catch:{ all -> 0x001f }
                android.hardware.camera2.impl.CameraDeviceImpl$StateCallbackKK r0 = android.hardware.camera2.impl.CameraDeviceImpl.access$100(r1)     // Catch:{ all -> 0x001f }
                monitor-exit(r2)     // Catch:{ all -> 0x001f }
                if (r0 == 0) goto L_0x000f
                android.hardware.camera2.impl.CameraDeviceImpl r1 = android.hardware.camera2.impl.CameraDeviceImpl.this
                r0.onBusy(r1)
                goto L_0x000f
            L_0x001f:
                r1 = move-exception
                monitor-exit(r2)
                throw r1
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.impl.CameraDeviceImpl.AnonymousClass4.run():void");
        }
    };
    private final Runnable mCallOnClosed = new Runnable() {
        /* class android.hardware.camera2.impl.CameraDeviceImpl.AnonymousClass5 */
        private boolean mClosedOnce = false;

        public void run() {
            StateCallbackKK sessionCallback;
            if (this.mClosedOnce) {
                throw new AssertionError("Don't post #onClosed more than once");
            }
            synchronized (CameraDeviceImpl.this.mInterfaceLock) {
                sessionCallback = CameraDeviceImpl.this.mSessionStateCallback;
            }
            if (sessionCallback != null) {
                sessionCallback.onClosed(CameraDeviceImpl.this);
            }
            CameraDeviceImpl.this.mDeviceCallback.onClosed(CameraDeviceImpl.this);
            this.mClosedOnce = true;
        }
    };
    private final Runnable mCallOnDisconnected = new Runnable() {
        /* class android.hardware.camera2.impl.CameraDeviceImpl.AnonymousClass7 */

        /* JADX WARNING: Code restructure failed: missing block: B:10:0x001e, code lost:
            r3.this$0.mDeviceCallback.onDisconnected(r3.this$0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:15:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:8:0x0017, code lost:
            if (r0 == null) goto L_0x001e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:9:0x0019, code lost:
            r0.onDisconnected(r3.this$0);
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            /*
                r3 = this;
                r0 = 0
                android.hardware.camera2.impl.CameraDeviceImpl r1 = android.hardware.camera2.impl.CameraDeviceImpl.this
                java.lang.Object r2 = r1.mInterfaceLock
                monitor-enter(r2)
                android.hardware.camera2.impl.CameraDeviceImpl r1 = android.hardware.camera2.impl.CameraDeviceImpl.this     // Catch:{ all -> 0x002a }
                android.hardware.camera2.ICameraDeviceUser r1 = android.hardware.camera2.impl.CameraDeviceImpl.access$000(r1)     // Catch:{ all -> 0x002a }
                if (r1 != 0) goto L_0x0010
                monitor-exit(r2)     // Catch:{ all -> 0x002a }
            L_0x000f:
                return
            L_0x0010:
                android.hardware.camera2.impl.CameraDeviceImpl r1 = android.hardware.camera2.impl.CameraDeviceImpl.this     // Catch:{ all -> 0x002a }
                android.hardware.camera2.impl.CameraDeviceImpl$StateCallbackKK r0 = android.hardware.camera2.impl.CameraDeviceImpl.access$100(r1)     // Catch:{ all -> 0x002a }
                monitor-exit(r2)     // Catch:{ all -> 0x002a }
                if (r0 == 0) goto L_0x001e
                android.hardware.camera2.impl.CameraDeviceImpl r1 = android.hardware.camera2.impl.CameraDeviceImpl.this
                r0.onDisconnected(r1)
            L_0x001e:
                android.hardware.camera2.impl.CameraDeviceImpl r1 = android.hardware.camera2.impl.CameraDeviceImpl.this
                android.hardware.camera2.CameraDevice$StateCallback r1 = android.hardware.camera2.impl.CameraDeviceImpl.access$200(r1)
                android.hardware.camera2.impl.CameraDeviceImpl r2 = android.hardware.camera2.impl.CameraDeviceImpl.this
                r1.onDisconnected(r2)
                goto L_0x000f
            L_0x002a:
                r1 = move-exception
                monitor-exit(r2)
                throw r1
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.impl.CameraDeviceImpl.AnonymousClass7.run():void");
        }
    };
    private final Runnable mCallOnIdle = new Runnable() {
        /* class android.hardware.camera2.impl.CameraDeviceImpl.AnonymousClass6 */

        /* JADX WARNING: Code restructure failed: missing block: B:14:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:15:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:8:0x0017, code lost:
            if (r0 == null) goto L_?;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:9:0x0019, code lost:
            r0.onIdle(r3.this$0);
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            /*
                r3 = this;
                r0 = 0
                android.hardware.camera2.impl.CameraDeviceImpl r1 = android.hardware.camera2.impl.CameraDeviceImpl.this
                java.lang.Object r2 = r1.mInterfaceLock
                monitor-enter(r2)
                android.hardware.camera2.impl.CameraDeviceImpl r1 = android.hardware.camera2.impl.CameraDeviceImpl.this     // Catch:{ all -> 0x001f }
                android.hardware.camera2.ICameraDeviceUser r1 = android.hardware.camera2.impl.CameraDeviceImpl.access$000(r1)     // Catch:{ all -> 0x001f }
                if (r1 != 0) goto L_0x0010
                monitor-exit(r2)     // Catch:{ all -> 0x001f }
            L_0x000f:
                return
            L_0x0010:
                android.hardware.camera2.impl.CameraDeviceImpl r1 = android.hardware.camera2.impl.CameraDeviceImpl.this     // Catch:{ all -> 0x001f }
                android.hardware.camera2.impl.CameraDeviceImpl$StateCallbackKK r0 = android.hardware.camera2.impl.CameraDeviceImpl.access$100(r1)     // Catch:{ all -> 0x001f }
                monitor-exit(r2)     // Catch:{ all -> 0x001f }
                if (r0 == 0) goto L_0x000f
                android.hardware.camera2.impl.CameraDeviceImpl r1 = android.hardware.camera2.impl.CameraDeviceImpl.this
                r0.onIdle(r1)
                goto L_0x000f
            L_0x001f:
                r1 = move-exception
                monitor-exit(r2)
                throw r1
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.impl.CameraDeviceImpl.AnonymousClass6.run():void");
        }
    };
    private final Runnable mCallOnOpened = new Runnable() {
        /* class android.hardware.camera2.impl.CameraDeviceImpl.AnonymousClass1 */

        /* JADX WARNING: Code restructure failed: missing block: B:10:0x001e, code lost:
            r3.this$0.mDeviceCallback.onOpened(r3.this$0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:15:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:8:0x0017, code lost:
            if (r0 == null) goto L_0x001e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:9:0x0019, code lost:
            r0.onOpened(r3.this$0);
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            /*
                r3 = this;
                r0 = 0
                android.hardware.camera2.impl.CameraDeviceImpl r1 = android.hardware.camera2.impl.CameraDeviceImpl.this
                java.lang.Object r2 = r1.mInterfaceLock
                monitor-enter(r2)
                android.hardware.camera2.impl.CameraDeviceImpl r1 = android.hardware.camera2.impl.CameraDeviceImpl.this     // Catch:{ all -> 0x002a }
                android.hardware.camera2.ICameraDeviceUser r1 = android.hardware.camera2.impl.CameraDeviceImpl.access$000(r1)     // Catch:{ all -> 0x002a }
                if (r1 != 0) goto L_0x0010
                monitor-exit(r2)     // Catch:{ all -> 0x002a }
            L_0x000f:
                return
            L_0x0010:
                android.hardware.camera2.impl.CameraDeviceImpl r1 = android.hardware.camera2.impl.CameraDeviceImpl.this     // Catch:{ all -> 0x002a }
                android.hardware.camera2.impl.CameraDeviceImpl$StateCallbackKK r0 = android.hardware.camera2.impl.CameraDeviceImpl.access$100(r1)     // Catch:{ all -> 0x002a }
                monitor-exit(r2)     // Catch:{ all -> 0x002a }
                if (r0 == 0) goto L_0x001e
                android.hardware.camera2.impl.CameraDeviceImpl r1 = android.hardware.camera2.impl.CameraDeviceImpl.this
                r0.onOpened(r1)
            L_0x001e:
                android.hardware.camera2.impl.CameraDeviceImpl r1 = android.hardware.camera2.impl.CameraDeviceImpl.this
                android.hardware.camera2.CameraDevice$StateCallback r1 = android.hardware.camera2.impl.CameraDeviceImpl.access$200(r1)
                android.hardware.camera2.impl.CameraDeviceImpl r2 = android.hardware.camera2.impl.CameraDeviceImpl.this
                r1.onOpened(r2)
                goto L_0x000f
            L_0x002a:
                r1 = move-exception
                monitor-exit(r2)
                throw r1
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.impl.CameraDeviceImpl.AnonymousClass1.run():void");
        }
    };
    private final Runnable mCallOnUnconfigured = new Runnable() {
        /* class android.hardware.camera2.impl.CameraDeviceImpl.AnonymousClass2 */

        /* JADX WARNING: Code restructure failed: missing block: B:14:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:15:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:8:0x0017, code lost:
            if (r0 == null) goto L_?;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:9:0x0019, code lost:
            r0.onUnconfigured(r3.this$0);
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            /*
                r3 = this;
                r0 = 0
                android.hardware.camera2.impl.CameraDeviceImpl r1 = android.hardware.camera2.impl.CameraDeviceImpl.this
                java.lang.Object r2 = r1.mInterfaceLock
                monitor-enter(r2)
                android.hardware.camera2.impl.CameraDeviceImpl r1 = android.hardware.camera2.impl.CameraDeviceImpl.this     // Catch:{ all -> 0x001f }
                android.hardware.camera2.ICameraDeviceUser r1 = android.hardware.camera2.impl.CameraDeviceImpl.access$000(r1)     // Catch:{ all -> 0x001f }
                if (r1 != 0) goto L_0x0010
                monitor-exit(r2)     // Catch:{ all -> 0x001f }
            L_0x000f:
                return
            L_0x0010:
                android.hardware.camera2.impl.CameraDeviceImpl r1 = android.hardware.camera2.impl.CameraDeviceImpl.this     // Catch:{ all -> 0x001f }
                android.hardware.camera2.impl.CameraDeviceImpl$StateCallbackKK r0 = android.hardware.camera2.impl.CameraDeviceImpl.access$100(r1)     // Catch:{ all -> 0x001f }
                monitor-exit(r2)     // Catch:{ all -> 0x001f }
                if (r0 == 0) goto L_0x000f
                android.hardware.camera2.impl.CameraDeviceImpl r1 = android.hardware.camera2.impl.CameraDeviceImpl.this
                r0.onUnconfigured(r1)
                goto L_0x000f
            L_0x001f:
                r1 = move-exception
                monitor-exit(r2)
                throw r1
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.impl.CameraDeviceImpl.AnonymousClass2.run():void");
        }
    };
    private final CameraDeviceCallbacks mCallbacks = new CameraDeviceCallbacks();
    private final String mCameraId;
    private final SparseArray<CaptureCallbackHolder> mCaptureCallbackMap = new SparseArray<>();
    private final CameraCharacteristics mCharacteristics;
    private volatile boolean mClosing = false;
    private final SparseArray<Surface> mConfiguredOutputs = new SparseArray<>();
    private CameraCaptureSessionImpl mCurrentSession;
    private final CameraDevice.StateCallback mDeviceCallback;
    private final Handler mDeviceHandler;
    private final List<AbstractMap.SimpleEntry<Long, Integer>> mFrameNumberRequestPairs = new ArrayList();
    private final FrameNumberTracker mFrameNumberTracker = new FrameNumberTracker();
    private boolean mIdle = true;
    private boolean mInError = false;
    final Object mInterfaceLock = new Object();
    private int mNextSessionId = 0;
    private ICameraDeviceUser mRemoteDevice;
    private int mRepeatingRequestId = -1;
    private final ArrayList<Integer> mRepeatingRequestIdDeletedList = new ArrayList<>();
    private volatile StateCallbackKK mSessionStateCallback;
    private final int mTotalPartialCount;

    public CameraDeviceImpl(String cameraId, CameraDevice.StateCallback callback, Handler handler, CameraCharacteristics characteristics) {
        if (cameraId == null || callback == null || handler == null || characteristics == null) {
            throw new IllegalArgumentException("Null argument given");
        }
        this.mCameraId = cameraId;
        this.mDeviceCallback = callback;
        this.mDeviceHandler = handler;
        this.mCharacteristics = characteristics;
        String tag = String.format("CameraDevice-JV-%s", this.mCameraId);
        this.TAG = tag.length() > 23 ? tag.substring(0, 23) : tag;
        this.DEBUG = Log.isLoggable(this.TAG, 3);
        Integer partialCount = (Integer) this.mCharacteristics.get(CameraCharacteristics.REQUEST_PARTIAL_RESULT_COUNT);
        if (partialCount == null) {
            this.mTotalPartialCount = 1;
        } else {
            this.mTotalPartialCount = partialCount.intValue();
        }
    }

    public CameraDeviceCallbacks getCallbacks() {
        return this.mCallbacks;
    }

    public void setRemoteDevice(ICameraDeviceUser remoteDevice) {
        synchronized (this.mInterfaceLock) {
            if (!this.mInError) {
                this.mRemoteDevice = (ICameraDeviceUser) CameraBinderDecorator.newInstance(remoteDevice);
                this.mDeviceHandler.post(this.mCallOnOpened);
                this.mDeviceHandler.post(this.mCallOnUnconfigured);
            }
        }
    }

    public void setRemoteFailure(CameraRuntimeException failure) {
        final int failureCode = 4;
        final boolean failureIsError = true;
        switch (failure.getReason()) {
            case 1:
                failureCode = 3;
                break;
            case 2:
                failureIsError = false;
                break;
            case 3:
                failureCode = 4;
                break;
            case 4:
                failureCode = 1;
                break;
            case 5:
                failureCode = 2;
                break;
            default:
                Log.wtf(this.TAG, "Unknown failure in opening camera device: " + failure.getReason());
                break;
        }
        synchronized (this.mInterfaceLock) {
            this.mInError = true;
            this.mDeviceHandler.post(new Runnable() {
                /* class android.hardware.camera2.impl.CameraDeviceImpl.AnonymousClass8 */

                public void run() {
                    if (failureIsError) {
                        CameraDeviceImpl.this.mDeviceCallback.onError(CameraDeviceImpl.this, failureCode);
                    } else {
                        CameraDeviceImpl.this.mDeviceCallback.onDisconnected(CameraDeviceImpl.this);
                    }
                }
            });
        }
    }

    @Override // android.hardware.camera2.CameraDevice
    public String getId() {
        return this.mCameraId;
    }

    public void configureOutputs(List<Surface> outputs) throws CameraAccessException {
        configureOutputsChecked(outputs);
    }

    public boolean configureOutputsChecked(List<Surface> outputs) throws CameraAccessException {
        if (outputs == null) {
            outputs = new ArrayList<>();
        }
        synchronized (this.mInterfaceLock) {
            checkIfCameraClosedOrInError();
            HashSet<Surface> addSet = new HashSet<>(outputs);
            List<Integer> deleteList = new ArrayList<>();
            for (int i = 0; i < this.mConfiguredOutputs.size(); i++) {
                int streamId = this.mConfiguredOutputs.keyAt(i);
                Surface s = this.mConfiguredOutputs.valueAt(i);
                if (!outputs.contains(s)) {
                    deleteList.add(Integer.valueOf(streamId));
                } else {
                    addSet.remove(s);
                }
            }
            this.mDeviceHandler.post(this.mCallOnBusy);
            stopRepeating();
            try {
                waitUntilIdle();
                this.mRemoteDevice.beginConfigure();
                for (Integer streamId2 : deleteList) {
                    this.mRemoteDevice.deleteStream(streamId2.intValue());
                    this.mConfiguredOutputs.delete(streamId2.intValue());
                }
                Iterator i$ = addSet.iterator();
                while (i$.hasNext()) {
                    Surface s2 = i$.next();
                    this.mConfiguredOutputs.put(this.mRemoteDevice.createStream(0, 0, 0, s2), s2);
                }
                try {
                    this.mRemoteDevice.endConfigure();
                    if (1 == 0 || outputs.size() <= 0) {
                        this.mDeviceHandler.post(this.mCallOnUnconfigured);
                    } else {
                        this.mDeviceHandler.post(this.mCallOnIdle);
                    }
                } catch (IllegalArgumentException e) {
                    Log.w(this.TAG, "Stream configuration failed");
                    if (0 == 0 || outputs.size() <= 0) {
                        this.mDeviceHandler.post(this.mCallOnUnconfigured);
                    } else {
                        this.mDeviceHandler.post(this.mCallOnIdle);
                    }
                    return false;
                }
            } catch (CameraRuntimeException e2) {
                if (e2.getReason() == 4) {
                    throw new IllegalStateException("The camera is currently busy. You must wait until the previous operation completes.");
                }
                throw e2.asChecked();
            } catch (RemoteException e3) {
                if (0 == 0 || outputs.size() <= 0) {
                    this.mDeviceHandler.post(this.mCallOnUnconfigured);
                } else {
                    this.mDeviceHandler.post(this.mCallOnIdle);
                }
                return false;
            } catch (Throwable th) {
                if (0 == 0 || outputs.size() <= 0) {
                    this.mDeviceHandler.post(this.mCallOnUnconfigured);
                } else {
                    this.mDeviceHandler.post(this.mCallOnIdle);
                }
                throw th;
            }
        }
        return true;
    }

    @Override // android.hardware.camera2.CameraDevice
    public void createCaptureSession(List<Surface> outputs, CameraCaptureSession.StateCallback callback, Handler handler) throws CameraAccessException {
        boolean configureSuccess;
        synchronized (this.mInterfaceLock) {
            if (this.DEBUG) {
                Log.d(this.TAG, "createCaptureSession");
            }
            checkIfCameraClosedOrInError();
            if (this.mCurrentSession != null) {
                this.mCurrentSession.replaceSessionClose();
            }
            CameraAccessException pendingException = null;
            try {
                configureSuccess = configureOutputsChecked(outputs);
            } catch (CameraAccessException e) {
                configureSuccess = false;
                pendingException = e;
                if (this.DEBUG) {
                    Log.v(this.TAG, "createCaptureSession - failed with exception ", e);
                }
            }
            int i = this.mNextSessionId;
            this.mNextSessionId = i + 1;
            this.mCurrentSession = new CameraCaptureSessionImpl(i, outputs, callback, handler, this, this.mDeviceHandler, configureSuccess);
            if (pendingException != null) {
                throw pendingException;
            }
            this.mSessionStateCallback = this.mCurrentSession.getDeviceStateCallback();
        }
    }

    public void setSessionListener(StateCallbackKK sessionCallback) {
        synchronized (this.mInterfaceLock) {
            this.mSessionStateCallback = sessionCallback;
        }
    }

    @Override // android.hardware.camera2.CameraDevice
    public CaptureRequest.Builder createCaptureRequest(int templateType) throws CameraAccessException {
        CaptureRequest.Builder builder;
        synchronized (this.mInterfaceLock) {
            checkIfCameraClosedOrInError();
            CameraMetadataNative templatedRequest = new CameraMetadataNative();
            try {
                this.mRemoteDevice.createDefaultRequest(templateType, templatedRequest);
                builder = new CaptureRequest.Builder(templatedRequest);
            } catch (CameraRuntimeException e) {
                throw e.asChecked();
            } catch (RemoteException e2) {
                builder = null;
            }
        }
        return builder;
    }

    public int capture(CaptureRequest request, CaptureCallback callback, Handler handler) throws CameraAccessException {
        if (this.DEBUG) {
            Log.d(this.TAG, "calling capture");
        }
        List<CaptureRequest> requestList = new ArrayList<>();
        requestList.add(request);
        return submitCaptureRequest(requestList, callback, handler, false);
    }

    public int captureBurst(List<CaptureRequest> requests, CaptureCallback callback, Handler handler) throws CameraAccessException {
        if (requests != null && !requests.isEmpty()) {
            return submitCaptureRequest(requests, callback, handler, false);
        }
        throw new IllegalArgumentException("At least one request must be given");
    }

    private void checkEarlyTriggerSequenceComplete(final int requestId, final long lastFrameNumber) {
        if (lastFrameNumber == -1) {
            int index = this.mCaptureCallbackMap.indexOfKey(requestId);
            final CaptureCallbackHolder holder = index >= 0 ? this.mCaptureCallbackMap.valueAt(index) : null;
            if (holder != null) {
                this.mCaptureCallbackMap.removeAt(index);
                if (this.DEBUG) {
                    Log.v(this.TAG, String.format("remove holder for requestId %d, because lastFrame is %d.", Integer.valueOf(requestId), Long.valueOf(lastFrameNumber)));
                }
            }
            if (holder != null) {
                if (this.DEBUG) {
                    Log.v(this.TAG, "immediately trigger onCaptureSequenceAborted because request did not reach HAL");
                }
                holder.getHandler().post(new Runnable() {
                    /* class android.hardware.camera2.impl.CameraDeviceImpl.AnonymousClass9 */

                    public void run() {
                        if (!CameraDeviceImpl.this.isClosed()) {
                            if (CameraDeviceImpl.this.DEBUG) {
                                Log.d(CameraDeviceImpl.this.TAG, String.format("early trigger sequence complete for request %d", Integer.valueOf(requestId)));
                            }
                            if (lastFrameNumber < -2147483648L || lastFrameNumber > 2147483647L) {
                                throw new AssertionError(lastFrameNumber + " cannot be cast to int");
                            }
                            holder.getCallback().onCaptureSequenceAborted(CameraDeviceImpl.this, requestId);
                        }
                    }
                });
                return;
            }
            Log.w(this.TAG, String.format("did not register callback to request %d", Integer.valueOf(requestId)));
            return;
        }
        this.mFrameNumberRequestPairs.add(new AbstractMap.SimpleEntry<>(Long.valueOf(lastFrameNumber), Integer.valueOf(requestId)));
        checkAndFireSequenceComplete();
    }

    private int submitCaptureRequest(List<CaptureRequest> requestList, CaptureCallback callback, Handler handler, boolean repeating) throws CameraAccessException {
        int requestId;
        Handler handler2 = checkHandler(handler, callback);
        for (CaptureRequest request : requestList) {
            if (request.getTargets().isEmpty()) {
                throw new IllegalArgumentException("Each request must have at least one Surface target");
            }
            Iterator i$ = request.getTargets().iterator();
            while (true) {
                if (i$.hasNext()) {
                    if (i$.next() == null) {
                        throw new IllegalArgumentException("Null Surface targets are not allowed");
                    }
                }
            }
        }
        synchronized (this.mInterfaceLock) {
            checkIfCameraClosedOrInError();
            if (repeating) {
                stopRepeating();
            }
            LongParcelable lastFrameNumberRef = new LongParcelable();
            try {
                requestId = this.mRemoteDevice.submitRequestList(requestList, repeating, lastFrameNumberRef);
                if (this.DEBUG) {
                    Log.v(this.TAG, "last frame number " + lastFrameNumberRef.getNumber());
                }
                if (callback != null) {
                    this.mCaptureCallbackMap.put(requestId, new CaptureCallbackHolder(callback, requestList, handler2, repeating));
                } else if (this.DEBUG) {
                    Log.d(this.TAG, "Listen for request " + requestId + " is null");
                }
                long lastFrameNumber = lastFrameNumberRef.getNumber();
                if (repeating) {
                    if (this.mRepeatingRequestId != -1) {
                        checkEarlyTriggerSequenceComplete(this.mRepeatingRequestId, lastFrameNumber);
                    }
                    this.mRepeatingRequestId = requestId;
                } else {
                    this.mFrameNumberRequestPairs.add(new AbstractMap.SimpleEntry<>(Long.valueOf(lastFrameNumber), Integer.valueOf(requestId)));
                }
                if (this.mIdle) {
                    this.mDeviceHandler.post(this.mCallOnActive);
                }
                this.mIdle = false;
            } catch (CameraRuntimeException e) {
                throw e.asChecked();
            } catch (RemoteException e2) {
                requestId = -1;
            }
        }
        return requestId;
    }

    public int setRepeatingRequest(CaptureRequest request, CaptureCallback callback, Handler handler) throws CameraAccessException {
        List<CaptureRequest> requestList = new ArrayList<>();
        requestList.add(request);
        return submitCaptureRequest(requestList, callback, handler, true);
    }

    public int setRepeatingBurst(List<CaptureRequest> requests, CaptureCallback callback, Handler handler) throws CameraAccessException {
        if (requests != null && !requests.isEmpty()) {
            return submitCaptureRequest(requests, callback, handler, true);
        }
        throw new IllegalArgumentException("At least one request must be given");
    }

    public void stopRepeating() throws CameraAccessException {
        synchronized (this.mInterfaceLock) {
            checkIfCameraClosedOrInError();
            if (this.mRepeatingRequestId != -1) {
                int requestId = this.mRepeatingRequestId;
                this.mRepeatingRequestId = -1;
                if (this.mCaptureCallbackMap.get(requestId) != null) {
                    this.mRepeatingRequestIdDeletedList.add(Integer.valueOf(requestId));
                }
                try {
                    LongParcelable lastFrameNumberRef = new LongParcelable();
                    this.mRemoteDevice.cancelRequest(requestId, lastFrameNumberRef);
                    checkEarlyTriggerSequenceComplete(requestId, lastFrameNumberRef.getNumber());
                } catch (CameraRuntimeException e) {
                    throw e.asChecked();
                } catch (RemoteException e2) {
                }
            }
        }
    }

    private void waitUntilIdle() throws CameraAccessException {
        synchronized (this.mInterfaceLock) {
            checkIfCameraClosedOrInError();
            if (this.mRepeatingRequestId != -1) {
                throw new IllegalStateException("Active repeating request ongoing");
            }
            try {
                this.mRemoteDevice.waitUntilIdle();
            } catch (CameraRuntimeException e) {
                throw e.asChecked();
            } catch (RemoteException e2) {
            }
        }
    }

    public void flush() throws CameraAccessException {
        synchronized (this.mInterfaceLock) {
            checkIfCameraClosedOrInError();
            this.mDeviceHandler.post(this.mCallOnBusy);
            if (this.mIdle) {
                this.mDeviceHandler.post(this.mCallOnIdle);
                return;
            }
            try {
                LongParcelable lastFrameNumberRef = new LongParcelable();
                this.mRemoteDevice.flush(lastFrameNumberRef);
                if (this.mRepeatingRequestId != -1) {
                    checkEarlyTriggerSequenceComplete(this.mRepeatingRequestId, lastFrameNumberRef.getNumber());
                    this.mRepeatingRequestId = -1;
                }
            } catch (CameraRuntimeException e) {
                throw e.asChecked();
            } catch (RemoteException e2) {
            }
        }
    }

    @Override // android.hardware.camera2.CameraDevice, java.lang.AutoCloseable
    public void close() {
        synchronized (this.mInterfaceLock) {
            try {
                if (this.mRemoteDevice != null) {
                    this.mRemoteDevice.disconnect();
                }
            } catch (CameraRuntimeException e) {
                Log.e(this.TAG, "Exception while closing: ", e.asChecked());
            } catch (RemoteException e2) {
            }
            if (this.mRemoteDevice != null || this.mInError) {
                this.mDeviceHandler.post(this.mCallOnClosed);
            }
            this.mRemoteDevice = null;
            this.mInError = false;
        }
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }

    public static abstract class CaptureCallback {
        public static final int NO_FRAMES_CAPTURED = -1;

        public void onCaptureStarted(CameraDevice camera, CaptureRequest request, long timestamp, long frameNumber) {
        }

        public void onCapturePartial(CameraDevice camera, CaptureRequest request, CaptureResult result) {
        }

        public void onCaptureProgressed(CameraDevice camera, CaptureRequest request, CaptureResult partialResult) {
        }

        public void onCaptureCompleted(CameraDevice camera, CaptureRequest request, TotalCaptureResult result) {
        }

        public void onCaptureFailed(CameraDevice camera, CaptureRequest request, CaptureFailure failure) {
        }

        public void onCaptureSequenceCompleted(CameraDevice camera, int sequenceId, long frameNumber) {
        }

        public void onCaptureSequenceAborted(CameraDevice camera, int sequenceId) {
        }
    }

    public static abstract class StateCallbackKK extends CameraDevice.StateCallback {
        public void onUnconfigured(CameraDevice camera) {
        }

        public void onActive(CameraDevice camera) {
        }

        public void onBusy(CameraDevice camera) {
        }

        public void onIdle(CameraDevice camera) {
        }
    }

    /* access modifiers changed from: package-private */
    public static class CaptureCallbackHolder {
        private final CaptureCallback mCallback;
        private final Handler mHandler;
        private final boolean mRepeating;
        private final List<CaptureRequest> mRequestList;

        CaptureCallbackHolder(CaptureCallback callback, List<CaptureRequest> requestList, Handler handler, boolean repeating) {
            if (callback == null || handler == null) {
                throw new UnsupportedOperationException("Must have a valid handler and a valid callback");
            }
            this.mRepeating = repeating;
            this.mHandler = handler;
            this.mRequestList = new ArrayList(requestList);
            this.mCallback = callback;
        }

        public boolean isRepeating() {
            return this.mRepeating;
        }

        public CaptureCallback getCallback() {
            return this.mCallback;
        }

        public CaptureRequest getRequest(int subsequenceId) {
            if (subsequenceId >= this.mRequestList.size()) {
                throw new IllegalArgumentException(String.format("Requested subsequenceId %d is larger than request list size %d.", Integer.valueOf(subsequenceId), Integer.valueOf(this.mRequestList.size())));
            } else if (subsequenceId >= 0) {
                return this.mRequestList.get(subsequenceId);
            } else {
                throw new IllegalArgumentException(String.format("Requested subsequenceId %d is negative", Integer.valueOf(subsequenceId)));
            }
        }

        public CaptureRequest getRequest() {
            return getRequest(0);
        }

        public Handler getHandler() {
            return this.mHandler;
        }
    }

    public class FrameNumberTracker {
        private long mCompletedFrameNumber = -1;
        private final TreeSet<Long> mFutureErrorSet = new TreeSet<>();
        private final HashMap<Long, List<CaptureResult>> mPartialResults = new HashMap<>();

        public FrameNumberTracker() {
        }

        private void update() {
            Iterator<Long> iter = this.mFutureErrorSet.iterator();
            while (iter.hasNext() && iter.next().longValue() == this.mCompletedFrameNumber + 1) {
                this.mCompletedFrameNumber++;
                iter.remove();
            }
        }

        public void updateTracker(long frameNumber, boolean isError) {
            if (isError) {
                this.mFutureErrorSet.add(Long.valueOf(frameNumber));
            } else {
                if (frameNumber != this.mCompletedFrameNumber + 1) {
                    Log.e(CameraDeviceImpl.this.TAG, String.format("result frame number %d comes out of order, should be %d + 1", Long.valueOf(frameNumber), Long.valueOf(this.mCompletedFrameNumber)));
                }
                this.mCompletedFrameNumber = frameNumber;
            }
            update();
        }

        public void updateTracker(long frameNumber, CaptureResult result, boolean partial) {
            if (!partial) {
                updateTracker(frameNumber, false);
            } else if (result != null) {
                List<CaptureResult> partials = this.mPartialResults.get(Long.valueOf(frameNumber));
                if (partials == null) {
                    partials = new ArrayList<>();
                    this.mPartialResults.put(Long.valueOf(frameNumber), partials);
                }
                partials.add(result);
            }
        }

        public List<CaptureResult> popPartialResults(long frameNumber) {
            return this.mPartialResults.remove(Long.valueOf(frameNumber));
        }

        public long getCompletedFrameNumber() {
            return this.mCompletedFrameNumber;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0081, code lost:
        r5.remove();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0084, code lost:
        if (r3 == null) goto L_0x000c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0086, code lost:
        r3.getHandler().post(new android.hardware.camera2.impl.CameraDeviceImpl.AnonymousClass10(r14));
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void checkAndFireSequenceComplete() {
        /*
        // Method dump skipped, instructions count: 153
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.impl.CameraDeviceImpl.checkAndFireSequenceComplete():void");
    }

    public class CameraDeviceCallbacks extends ICameraDeviceCallbacks.Stub {
        public static final int ERROR_CAMERA_BUFFER = 5;
        public static final int ERROR_CAMERA_DEVICE = 1;
        public static final int ERROR_CAMERA_DISCONNECTED = 0;
        public static final int ERROR_CAMERA_REQUEST = 3;
        public static final int ERROR_CAMERA_RESULT = 4;
        public static final int ERROR_CAMERA_SERVICE = 2;

        public CameraDeviceCallbacks() {
        }

        @Override // android.os.IInterface, android.hardware.camera2.ICameraDeviceCallbacks.Stub
        public IBinder asBinder() {
            return this;
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        @Override // android.hardware.camera2.ICameraDeviceCallbacks
        public void onDeviceError(final int errorCode, CaptureResultExtras resultExtras) {
            if (CameraDeviceImpl.this.DEBUG) {
                Log.d(CameraDeviceImpl.this.TAG, String.format("Device error received, code %d, frame number %d, request ID %d, subseq ID %d", Integer.valueOf(errorCode), Long.valueOf(resultExtras.getFrameNumber()), Integer.valueOf(resultExtras.getRequestId()), Integer.valueOf(resultExtras.getSubsequenceId())));
            }
            synchronized (CameraDeviceImpl.this.mInterfaceLock) {
                if (CameraDeviceImpl.this.mRemoteDevice != null) {
                    switch (errorCode) {
                        case 0:
                            CameraDeviceImpl.this.mDeviceHandler.post(CameraDeviceImpl.this.mCallOnDisconnected);
                            break;
                        case 1:
                        case 2:
                            CameraDeviceImpl.this.mInError = true;
                            CameraDeviceImpl.this.mDeviceHandler.post(new Runnable() {
                                /* class android.hardware.camera2.impl.CameraDeviceImpl.CameraDeviceCallbacks.AnonymousClass1 */

                                public void run() {
                                    if (!CameraDeviceImpl.this.isClosed()) {
                                        CameraDeviceImpl.this.mDeviceCallback.onError(CameraDeviceImpl.this, errorCode);
                                    }
                                }
                            });
                            break;
                        case 3:
                        case 4:
                        case 5:
                            onCaptureErrorLocked(errorCode, resultExtras);
                            break;
                        default:
                            Log.e(CameraDeviceImpl.this.TAG, "Unknown error from camera device: " + errorCode);
                            CameraDeviceImpl.this.mInError = true;
                            CameraDeviceImpl.this.mDeviceHandler.post(new Runnable() {
                                /* class android.hardware.camera2.impl.CameraDeviceImpl.CameraDeviceCallbacks.AnonymousClass1 */

                                public void run() {
                                    if (!CameraDeviceImpl.this.isClosed()) {
                                        CameraDeviceImpl.this.mDeviceCallback.onError(CameraDeviceImpl.this, errorCode);
                                    }
                                }
                            });
                            break;
                    }
                }
            }
        }

        @Override // android.hardware.camera2.ICameraDeviceCallbacks
        public void onDeviceIdle() {
            if (CameraDeviceImpl.this.DEBUG) {
                Log.d(CameraDeviceImpl.this.TAG, "Camera now idle");
            }
            synchronized (CameraDeviceImpl.this.mInterfaceLock) {
                if (CameraDeviceImpl.this.mRemoteDevice != null) {
                    if (!CameraDeviceImpl.this.mIdle) {
                        CameraDeviceImpl.this.mDeviceHandler.post(CameraDeviceImpl.this.mCallOnIdle);
                    }
                    CameraDeviceImpl.this.mIdle = true;
                }
            }
        }

        @Override // android.hardware.camera2.ICameraDeviceCallbacks
        public void onCaptureStarted(final CaptureResultExtras resultExtras, final long timestamp) {
            int requestId = resultExtras.getRequestId();
            final long frameNumber = resultExtras.getFrameNumber();
            if (CameraDeviceImpl.this.DEBUG) {
                Log.d(CameraDeviceImpl.this.TAG, "Capture started for id " + requestId + " frame number " + frameNumber);
            }
            synchronized (CameraDeviceImpl.this.mInterfaceLock) {
                if (CameraDeviceImpl.this.mRemoteDevice != null) {
                    final CaptureCallbackHolder holder = (CaptureCallbackHolder) CameraDeviceImpl.this.mCaptureCallbackMap.get(requestId);
                    if (holder != null) {
                        if (!CameraDeviceImpl.this.isClosed()) {
                            holder.getHandler().post(new Runnable() {
                                /* class android.hardware.camera2.impl.CameraDeviceImpl.CameraDeviceCallbacks.AnonymousClass2 */

                                public void run() {
                                    if (!CameraDeviceImpl.this.isClosed()) {
                                        holder.getCallback().onCaptureStarted(CameraDeviceImpl.this, holder.getRequest(resultExtras.getSubsequenceId()), timestamp, frameNumber);
                                    }
                                }
                            });
                        }
                    }
                }
            }
        }

        @Override // android.hardware.camera2.ICameraDeviceCallbacks
        public void onResultReceived(CameraMetadataNative result, CaptureResultExtras resultExtras) throws RemoteException {
            Runnable resultDispatch;
            CaptureResult finalResult;
            int requestId = resultExtras.getRequestId();
            long frameNumber = resultExtras.getFrameNumber();
            if (CameraDeviceImpl.this.DEBUG) {
                Log.v(CameraDeviceImpl.this.TAG, "Received result frame " + frameNumber + " for id " + requestId);
            }
            synchronized (CameraDeviceImpl.this.mInterfaceLock) {
                if (CameraDeviceImpl.this.mRemoteDevice != null) {
                    result.set(CameraCharacteristics.LENS_INFO_SHADING_MAP_SIZE, CameraDeviceImpl.this.getCharacteristics().get(CameraCharacteristics.LENS_INFO_SHADING_MAP_SIZE));
                    final CaptureCallbackHolder holder = (CaptureCallbackHolder) CameraDeviceImpl.this.mCaptureCallbackMap.get(requestId);
                    boolean isPartialResult = resultExtras.getPartialResultCount() < CameraDeviceImpl.this.mTotalPartialCount;
                    if (holder == null) {
                        if (CameraDeviceImpl.this.DEBUG) {
                            Log.d(CameraDeviceImpl.this.TAG, "holder is null, early return at frame " + frameNumber);
                        }
                        CameraDeviceImpl.this.mFrameNumberTracker.updateTracker(frameNumber, null, isPartialResult);
                    } else if (CameraDeviceImpl.this.isClosed()) {
                        if (CameraDeviceImpl.this.DEBUG) {
                            Log.d(CameraDeviceImpl.this.TAG, "camera is closed, early return at frame " + frameNumber);
                        }
                        CameraDeviceImpl.this.mFrameNumberTracker.updateTracker(frameNumber, null, isPartialResult);
                    } else {
                        final CaptureRequest request = holder.getRequest(resultExtras.getSubsequenceId());
                        if (isPartialResult) {
                            final CaptureResult resultAsCapture = new CaptureResult(result, request, resultExtras);
                            resultDispatch = new Runnable() {
                                /* class android.hardware.camera2.impl.CameraDeviceImpl.CameraDeviceCallbacks.AnonymousClass3 */

                                public void run() {
                                    if (!CameraDeviceImpl.this.isClosed()) {
                                        holder.getCallback().onCaptureProgressed(CameraDeviceImpl.this, request, resultAsCapture);
                                    }
                                }
                            };
                            finalResult = resultAsCapture;
                        } else {
                            final TotalCaptureResult resultAsCapture2 = new TotalCaptureResult(result, request, resultExtras, CameraDeviceImpl.this.mFrameNumberTracker.popPartialResults(frameNumber));
                            resultDispatch = new Runnable() {
                                /* class android.hardware.camera2.impl.CameraDeviceImpl.CameraDeviceCallbacks.AnonymousClass4 */

                                public void run() {
                                    if (!CameraDeviceImpl.this.isClosed()) {
                                        holder.getCallback().onCaptureCompleted(CameraDeviceImpl.this, request, resultAsCapture2);
                                    }
                                }
                            };
                            finalResult = resultAsCapture2;
                        }
                        holder.getHandler().post(resultDispatch);
                        CameraDeviceImpl.this.mFrameNumberTracker.updateTracker(frameNumber, finalResult, isPartialResult);
                        if (!isPartialResult) {
                            CameraDeviceImpl.this.checkAndFireSequenceComplete();
                        }
                    }
                }
            }
        }

        private void onCaptureErrorLocked(int errorCode, CaptureResultExtras resultExtras) {
            int requestId = resultExtras.getRequestId();
            int subsequenceId = resultExtras.getSubsequenceId();
            long frameNumber = resultExtras.getFrameNumber();
            final CaptureCallbackHolder holder = (CaptureCallbackHolder) CameraDeviceImpl.this.mCaptureCallbackMap.get(requestId);
            final CaptureRequest request = holder.getRequest(subsequenceId);
            if (errorCode == 5) {
                Log.e(CameraDeviceImpl.this.TAG, String.format("Lost output buffer reported for frame %d", Long.valueOf(frameNumber)));
                return;
            }
            final CaptureFailure failure = new CaptureFailure(request, (CameraDeviceImpl.this.mCurrentSession == null || !CameraDeviceImpl.this.mCurrentSession.isAborting()) ? 0 : 1, errorCode == 4, requestId, frameNumber);
            holder.getHandler().post(new Runnable() {
                /* class android.hardware.camera2.impl.CameraDeviceImpl.CameraDeviceCallbacks.AnonymousClass5 */

                public void run() {
                    if (!CameraDeviceImpl.this.isClosed()) {
                        holder.getCallback().onCaptureFailed(CameraDeviceImpl.this, request, failure);
                    }
                }
            });
            if (CameraDeviceImpl.this.DEBUG) {
                Log.v(CameraDeviceImpl.this.TAG, String.format("got error frame %d", Long.valueOf(frameNumber)));
            }
            CameraDeviceImpl.this.mFrameNumberTracker.updateTracker(frameNumber, true);
            CameraDeviceImpl.this.checkAndFireSequenceComplete();
        }
    }

    static Handler checkHandler(Handler handler) {
        if (handler != null) {
            return handler;
        }
        Looper looper = Looper.myLooper();
        if (looper != null) {
            return new Handler(looper);
        }
        throw new IllegalArgumentException("No handler given, and current thread has no looper!");
    }

    static <T> Handler checkHandler(Handler handler, T callback) {
        if (callback != null) {
            return checkHandler(handler);
        }
        return handler;
    }

    private void checkIfCameraClosedOrInError() throws CameraAccessException {
        if (this.mInError) {
            throw new CameraAccessException(3, "The camera device has encountered a serious error");
        } else if (this.mRemoteDevice == null) {
            throw new IllegalStateException("CameraDevice was already closed");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isClosed() {
        return this.mClosing;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private CameraCharacteristics getCharacteristics() {
        return this.mCharacteristics;
    }
}

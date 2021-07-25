package android.hardware.camera2.legacy;

import android.hardware.Camera;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.ICameraDeviceCallbacks;
import android.hardware.camera2.ICameraDeviceUser;
import android.hardware.camera2.impl.CameraMetadataNative;
import android.hardware.camera2.impl.CaptureResultExtras;
import android.hardware.camera2.utils.CameraBinderDecorator;
import android.hardware.camera2.utils.CameraRuntimeException;
import android.hardware.camera2.utils.LongParcelable;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.util.SparseArray;
import android.view.Surface;
import android.widget.ExpandableListView;
import java.util.ArrayList;
import java.util.List;

public class CameraDeviceUserShim implements ICameraDeviceUser {
    private static final boolean DEBUG = Log.isLoggable(LegacyCameraDevice.DEBUG_PROP, 3);
    private static final int OPEN_CAMERA_TIMEOUT_MS = 5000;
    private static final String TAG = "CameraDeviceUserShim";
    private final CameraCallbackThread mCameraCallbacks;
    private final CameraCharacteristics mCameraCharacteristics;
    private final CameraLooper mCameraInit;
    private final Object mConfigureLock = new Object();
    private boolean mConfiguring;
    private final LegacyCameraDevice mLegacyDevice;
    private int mSurfaceIdCounter;
    private final SparseArray<Surface> mSurfaces;

    protected CameraDeviceUserShim(int cameraId, LegacyCameraDevice legacyCamera, CameraCharacteristics characteristics, CameraLooper cameraInit, CameraCallbackThread cameraCallbacks) {
        this.mLegacyDevice = legacyCamera;
        this.mConfiguring = false;
        this.mSurfaces = new SparseArray<>();
        this.mCameraCharacteristics = characteristics;
        this.mCameraInit = cameraInit;
        this.mCameraCallbacks = cameraCallbacks;
        this.mSurfaceIdCounter = 0;
    }

    /* access modifiers changed from: private */
    public static int translateErrorsFromCamera1(int errorCode) {
        switch (errorCode) {
            case -13:
                return -1;
            default:
                return errorCode;
        }
    }

    /* access modifiers changed from: private */
    public static class CameraLooper implements Runnable, AutoCloseable {
        private final Camera mCamera = Camera.openUninitialized();
        private final int mCameraId;
        private volatile int mInitErrors;
        private Looper mLooper;
        private final ConditionVariable mStartDone = new ConditionVariable();
        private final Thread mThread;

        public CameraLooper(int cameraId) {
            this.mCameraId = cameraId;
            this.mThread = new Thread(this);
            this.mThread.start();
        }

        public Camera getCamera() {
            return this.mCamera;
        }

        public void run() {
            Looper.prepare();
            this.mLooper = Looper.myLooper();
            this.mInitErrors = CameraDeviceUserShim.translateErrorsFromCamera1(this.mCamera.cameraInitUnspecified(this.mCameraId));
            this.mStartDone.open();
            Looper.loop();
        }

        @Override // java.lang.AutoCloseable
        public void close() {
            if (this.mLooper != null) {
                this.mLooper.quitSafely();
                try {
                    this.mThread.join();
                    this.mLooper = null;
                } catch (InterruptedException e) {
                    throw new AssertionError(e);
                }
            }
        }

        public int waitForOpen(int timeoutMs) {
            if (this.mStartDone.block((long) timeoutMs)) {
                return this.mInitErrors;
            }
            Log.e(CameraDeviceUserShim.TAG, "waitForOpen - Camera failed to open after timeout of 5000 ms");
            try {
                this.mCamera.release();
            } catch (RuntimeException e) {
                Log.e(CameraDeviceUserShim.TAG, "connectBinderShim - Failed to release camera after timeout ", e);
            }
            throw new CameraRuntimeException(3);
        }
    }

    /* access modifiers changed from: private */
    public static class CameraCallbackThread implements ICameraDeviceCallbacks {
        private static final int CAMERA_ERROR = 0;
        private static final int CAMERA_IDLE = 1;
        private static final int CAPTURE_STARTED = 2;
        private static final int RESULT_RECEIVED = 3;
        private final ICameraDeviceCallbacks mCallbacks;
        private Handler mHandler;
        private final HandlerThread mHandlerThread = new HandlerThread("LegacyCameraCallback");

        public CameraCallbackThread(ICameraDeviceCallbacks callbacks) {
            this.mCallbacks = callbacks;
            this.mHandlerThread.start();
        }

        public void close() {
            this.mHandlerThread.quitSafely();
        }

        @Override // android.hardware.camera2.ICameraDeviceCallbacks
        public void onDeviceError(int errorCode, CaptureResultExtras resultExtras) {
            getHandler().sendMessage(getHandler().obtainMessage(0, errorCode, 0, resultExtras));
        }

        @Override // android.hardware.camera2.ICameraDeviceCallbacks
        public void onDeviceIdle() {
            getHandler().sendMessage(getHandler().obtainMessage(1));
        }

        @Override // android.hardware.camera2.ICameraDeviceCallbacks
        public void onCaptureStarted(CaptureResultExtras resultExtras, long timestamp) {
            getHandler().sendMessage(getHandler().obtainMessage(2, (int) (timestamp & ExpandableListView.PACKED_POSITION_VALUE_NULL), (int) ((timestamp >> 32) & ExpandableListView.PACKED_POSITION_VALUE_NULL), resultExtras));
        }

        @Override // android.hardware.camera2.ICameraDeviceCallbacks
        public void onResultReceived(CameraMetadataNative result, CaptureResultExtras resultExtras) {
            getHandler().sendMessage(getHandler().obtainMessage(3, new Object[]{result, resultExtras}));
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }

        private Handler getHandler() {
            if (this.mHandler == null) {
                this.mHandler = new CallbackHandler(this.mHandlerThread.getLooper());
            }
            return this.mHandler;
        }

        /* access modifiers changed from: private */
        public class CallbackHandler extends Handler {
            public CallbackHandler(Looper l) {
                super(l);
            }

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                try {
                    switch (msg.what) {
                        case 0:
                            CameraCallbackThread.this.mCallbacks.onDeviceError(msg.arg1, (CaptureResultExtras) msg.obj);
                            return;
                        case 1:
                            CameraCallbackThread.this.mCallbacks.onDeviceIdle();
                            return;
                        case 2:
                            CaptureResultExtras resultExtras = (CaptureResultExtras) msg.obj;
                            CameraCallbackThread.this.mCallbacks.onCaptureStarted(resultExtras, ((((long) msg.arg2) & ExpandableListView.PACKED_POSITION_VALUE_NULL) << 32) | (((long) msg.arg1) & ExpandableListView.PACKED_POSITION_VALUE_NULL));
                            return;
                        case 3:
                            Object[] resultArray = (Object[]) msg.obj;
                            CameraCallbackThread.this.mCallbacks.onResultReceived((CameraMetadataNative) resultArray[0], (CaptureResultExtras) resultArray[1]);
                            return;
                        default:
                            throw new IllegalArgumentException("Unknown callback message " + msg.what);
                    }
                } catch (RemoteException e) {
                    throw new IllegalStateException("Received remote exception during camera callback " + msg.what, e);
                }
            }
        }
    }

    public static CameraDeviceUserShim connectBinderShim(ICameraDeviceCallbacks callbacks, int cameraId) {
        if (DEBUG) {
            Log.d(TAG, "Opening shim Camera device");
        }
        CameraLooper init = new CameraLooper(cameraId);
        CameraCallbackThread threadCallbacks = new CameraCallbackThread(callbacks);
        int initErrors = init.waitForOpen(5000);
        Camera legacyCamera = init.getCamera();
        CameraBinderDecorator.throwOnError(initErrors);
        legacyCamera.disableShutterSound();
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        try {
            CameraCharacteristics characteristics = LegacyMetadataMapper.createCharacteristics(legacyCamera.getParameters(), info);
            return new CameraDeviceUserShim(cameraId, new LegacyCameraDevice(cameraId, legacyCamera, characteristics, threadCallbacks), characteristics, init, threadCallbacks);
        } catch (RuntimeException e) {
            throw new CameraRuntimeException(3, "Unable to get initial parameters", e);
        }
    }

    @Override // android.hardware.camera2.ICameraDeviceUser
    public void disconnect() {
        if (DEBUG) {
            Log.d(TAG, "disconnect called.");
        }
        if (this.mLegacyDevice.isClosed()) {
            Log.w(TAG, "Cannot disconnect, device has already been closed.");
        }
        try {
            this.mLegacyDevice.close();
        } finally {
            this.mCameraInit.close();
            this.mCameraCallbacks.close();
        }
    }

    @Override // android.hardware.camera2.ICameraDeviceUser
    public int submitRequest(CaptureRequest request, boolean streaming, LongParcelable lastFrameNumber) {
        if (DEBUG) {
            Log.d(TAG, "submitRequest called.");
        }
        if (this.mLegacyDevice.isClosed()) {
            Log.e(TAG, "Cannot submit request, device has been closed.");
            return -19;
        }
        synchronized (this.mConfigureLock) {
            if (!this.mConfiguring) {
                return this.mLegacyDevice.submitRequest(request, streaming, lastFrameNumber);
            }
            Log.e(TAG, "Cannot submit request, configuration change in progress.");
            return -38;
        }
    }

    @Override // android.hardware.camera2.ICameraDeviceUser
    public int submitRequestList(List<CaptureRequest> request, boolean streaming, LongParcelable lastFrameNumber) {
        if (DEBUG) {
            Log.d(TAG, "submitRequestList called.");
        }
        if (this.mLegacyDevice.isClosed()) {
            Log.e(TAG, "Cannot submit request list, device has been closed.");
            return -19;
        }
        synchronized (this.mConfigureLock) {
            if (!this.mConfiguring) {
                return this.mLegacyDevice.submitRequestList(request, streaming, lastFrameNumber);
            }
            Log.e(TAG, "Cannot submit request, configuration change in progress.");
            return -38;
        }
    }

    @Override // android.hardware.camera2.ICameraDeviceUser
    public int cancelRequest(int requestId, LongParcelable lastFrameNumber) {
        if (DEBUG) {
            Log.d(TAG, "cancelRequest called.");
        }
        if (this.mLegacyDevice.isClosed()) {
            Log.e(TAG, "Cannot cancel request, device has been closed.");
            return -19;
        }
        synchronized (this.mConfigureLock) {
            if (this.mConfiguring) {
                Log.e(TAG, "Cannot cancel request, configuration change in progress.");
                return -38;
            }
            lastFrameNumber.setNumber(this.mLegacyDevice.cancelRequest(requestId));
            return 0;
        }
    }

    @Override // android.hardware.camera2.ICameraDeviceUser
    public int beginConfigure() {
        if (DEBUG) {
            Log.d(TAG, "beginConfigure called.");
        }
        if (this.mLegacyDevice.isClosed()) {
            Log.e(TAG, "Cannot begin configure, device has been closed.");
            return -19;
        }
        synchronized (this.mConfigureLock) {
            if (this.mConfiguring) {
                Log.e(TAG, "Cannot begin configure, configuration change already in progress.");
                return -38;
            }
            this.mConfiguring = true;
            return 0;
        }
    }

    @Override // android.hardware.camera2.ICameraDeviceUser
    public int endConfigure() {
        Throwable th;
        if (DEBUG) {
            Log.d(TAG, "endConfigure called.");
        }
        if (this.mLegacyDevice.isClosed()) {
            Log.e(TAG, "Cannot end configure, device has been closed.");
            return -19;
        }
        ArrayList<Surface> surfaces = null;
        synchronized (this.mConfigureLock) {
            try {
                if (!this.mConfiguring) {
                    Log.e(TAG, "Cannot end configure, no configuration change in progress.");
                    return -38;
                }
                int numSurfaces = this.mSurfaces.size();
                if (numSurfaces > 0) {
                    ArrayList<Surface> surfaces2 = new ArrayList<>();
                    for (int i = 0; i < numSurfaces; i++) {
                        try {
                            surfaces2.add(this.mSurfaces.valueAt(i));
                        } catch (Throwable th2) {
                            th = th2;
                            throw th;
                        }
                    }
                    surfaces = surfaces2;
                }
                this.mConfiguring = false;
                return this.mLegacyDevice.configureOutputs(surfaces);
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
    }

    @Override // android.hardware.camera2.ICameraDeviceUser
    public int deleteStream(int streamId) {
        if (DEBUG) {
            Log.d(TAG, "deleteStream called.");
        }
        if (this.mLegacyDevice.isClosed()) {
            Log.e(TAG, "Cannot delete stream, device has been closed.");
            return -19;
        }
        synchronized (this.mConfigureLock) {
            if (!this.mConfiguring) {
                Log.e(TAG, "Cannot delete stream, beginConfigure hasn't been called yet.");
                return -38;
            }
            int index = this.mSurfaces.indexOfKey(streamId);
            if (index < 0) {
                Log.e(TAG, "Cannot delete stream, stream id " + streamId + " doesn't exist.");
                return -22;
            }
            this.mSurfaces.removeAt(index);
            return 0;
        }
    }

    @Override // android.hardware.camera2.ICameraDeviceUser
    public int createStream(int width, int height, int format, Surface surface) {
        if (DEBUG) {
            Log.d(TAG, "createStream called.");
        }
        if (this.mLegacyDevice.isClosed()) {
            Log.e(TAG, "Cannot create stream, device has been closed.");
            return -19;
        }
        synchronized (this.mConfigureLock) {
            if (!this.mConfiguring) {
                Log.e(TAG, "Cannot create stream, beginConfigure hasn't been called yet.");
                return -38;
            }
            int id = this.mSurfaceIdCounter + 1;
            this.mSurfaceIdCounter = id;
            this.mSurfaces.put(id, surface);
            return id;
        }
    }

    @Override // android.hardware.camera2.ICameraDeviceUser
    public int createDefaultRequest(int templateId, CameraMetadataNative request) {
        if (DEBUG) {
            Log.d(TAG, "createDefaultRequest called.");
        }
        if (this.mLegacyDevice.isClosed()) {
            Log.e(TAG, "Cannot create default request, device has been closed.");
            return -19;
        }
        try {
            request.swap(LegacyMetadataMapper.createRequestTemplate(this.mCameraCharacteristics, templateId));
            return 0;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "createDefaultRequest - invalid templateId specified");
            return -22;
        }
    }

    @Override // android.hardware.camera2.ICameraDeviceUser
    public int getCameraInfo(CameraMetadataNative info) {
        if (DEBUG) {
            Log.d(TAG, "getCameraInfo called.");
        }
        Log.e(TAG, "getCameraInfo unimplemented.");
        return 0;
    }

    @Override // android.hardware.camera2.ICameraDeviceUser
    public int waitUntilIdle() throws RemoteException {
        if (DEBUG) {
            Log.d(TAG, "waitUntilIdle called.");
        }
        if (this.mLegacyDevice.isClosed()) {
            Log.e(TAG, "Cannot wait until idle, device has been closed.");
            return -19;
        }
        synchronized (this.mConfigureLock) {
            if (this.mConfiguring) {
                Log.e(TAG, "Cannot wait until idle, configuration change in progress.");
                return -38;
            }
            this.mLegacyDevice.waitUntilIdle();
            return 0;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0033, code lost:
        r0 = r5.mLegacyDevice.flush();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0039, code lost:
        if (r6 == null) goto L_0x003e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x003b, code lost:
        r6.setNumber(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x003e, code lost:
        return 0;
     */
    @Override // android.hardware.camera2.ICameraDeviceUser
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int flush(android.hardware.camera2.utils.LongParcelable r6) {
        /*
            r5 = this;
            boolean r2 = android.hardware.camera2.legacy.CameraDeviceUserShim.DEBUG
            if (r2 == 0) goto L_0x000b
            java.lang.String r2 = "CameraDeviceUserShim"
            java.lang.String r3 = "flush called."
            android.util.Log.d(r2, r3)
        L_0x000b:
            android.hardware.camera2.legacy.LegacyCameraDevice r2 = r5.mLegacyDevice
            boolean r2 = r2.isClosed()
            if (r2 == 0) goto L_0x001d
            java.lang.String r2 = "CameraDeviceUserShim"
            java.lang.String r3 = "Cannot flush, device has been closed."
            android.util.Log.e(r2, r3)
            r2 = -19
        L_0x001c:
            return r2
        L_0x001d:
            java.lang.Object r3 = r5.mConfigureLock
            monitor-enter(r3)
            boolean r2 = r5.mConfiguring     // Catch:{ all -> 0x002f }
            if (r2 == 0) goto L_0x0032
            java.lang.String r2 = "CameraDeviceUserShim"
            java.lang.String r4 = "Cannot flush, configuration change in progress."
            android.util.Log.e(r2, r4)     // Catch:{ all -> 0x002f }
            r2 = -38
            monitor-exit(r3)     // Catch:{ all -> 0x002f }
            goto L_0x001c
        L_0x002f:
            r2 = move-exception
            monitor-exit(r3)     // Catch:{ all -> 0x002f }
            throw r2
        L_0x0032:
            monitor-exit(r3)
            android.hardware.camera2.legacy.LegacyCameraDevice r2 = r5.mLegacyDevice
            long r0 = r2.flush()
            if (r6 == 0) goto L_0x003e
            r6.setNumber(r0)
        L_0x003e:
            r2 = 0
            goto L_0x001c
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.legacy.CameraDeviceUserShim.flush(android.hardware.camera2.utils.LongParcelable):int");
    }

    @Override // android.os.IInterface
    public IBinder asBinder() {
        return null;
    }
}

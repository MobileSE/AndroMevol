package android.hardware.camera2.legacy;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.impl.CameraMetadataNative;
import android.hardware.camera2.legacy.LegacyExceptionUtils;
import android.hardware.camera2.utils.LongParcelable;
import android.hardware.camera2.utils.SizeAreaComparator;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.MutableLong;
import android.util.Pair;
import android.util.Size;
import android.view.Surface;
import com.android.internal.util.Preconditions;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RequestThreadManager {
    private static final float ASPECT_RATIO_TOLERANCE = 0.01f;
    private static final boolean DEBUG = Log.isLoggable(LegacyCameraDevice.DEBUG_PROP, 3);
    private static final int JPEG_FRAME_TIMEOUT = 3000;
    private static final int MAX_IN_FLIGHT_REQUESTS = 2;
    private static final int MSG_CLEANUP = 3;
    private static final int MSG_CONFIGURE_OUTPUTS = 1;
    private static final int MSG_SUBMIT_CAPTURE_REQUEST = 2;
    private static final int PREVIEW_FRAME_TIMEOUT = 1000;
    private static final int REQUEST_COMPLETE_TIMEOUT = 3000;
    private static final boolean USE_BLOB_FORMAT_OVERRIDE = true;
    private static final boolean VERBOSE = Log.isLoggable(LegacyCameraDevice.DEBUG_PROP, 2);
    private final String TAG;
    private final List<Surface> mCallbackOutputs = new ArrayList();
    private final Camera mCamera;
    private final int mCameraId;
    private final CaptureCollector mCaptureCollector;
    private final CameraCharacteristics mCharacteristics;
    private final CameraDeviceState mDeviceState;
    private Surface mDummySurface;
    private SurfaceTexture mDummyTexture;
    private final Camera.ErrorCallback mErrorCallback = new Camera.ErrorCallback() {
        /* class android.hardware.camera2.legacy.RequestThreadManager.AnonymousClass1 */

        @Override // android.hardware.Camera.ErrorCallback
        public void onError(int i, Camera camera) {
            Log.e(RequestThreadManager.this.TAG, "Received error " + i + " from the Camera1 ErrorCallback");
            RequestThreadManager.this.mDeviceState.setError(1);
        }
    };
    private final LegacyFaceDetectMapper mFaceDetectMapper;
    private final LegacyFocusStateMapper mFocusStateMapper;
    private GLThreadManager mGLThreadManager;
    private final Object mIdleLock = new Object();
    private Size mIntermediateBufferSize;
    private final Camera.PictureCallback mJpegCallback = new Camera.PictureCallback() {
        /* class android.hardware.camera2.legacy.RequestThreadManager.AnonymousClass2 */

        @Override // android.hardware.Camera.PictureCallback
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.i(RequestThreadManager.this.TAG, "Received jpeg.");
            Pair<RequestHolder, Long> captureInfo = RequestThreadManager.this.mCaptureCollector.jpegProduced();
            if (captureInfo == null || captureInfo.first == null) {
                Log.e(RequestThreadManager.this.TAG, "Dropping jpeg frame.");
                return;
            }
            long timestamp = captureInfo.second.longValue();
            for (Surface s : captureInfo.first.getHolderTargets()) {
                try {
                    if (LegacyCameraDevice.containsSurfaceId(s, RequestThreadManager.this.mJpegSurfaceIds)) {
                        Log.i(RequestThreadManager.this.TAG, "Producing jpeg buffer...");
                        LegacyCameraDevice.setNextTimestamp(s, timestamp);
                        LegacyCameraDevice.setSurfaceFormat(s, 1);
                        int dimen = (((int) Math.ceil(Math.sqrt((double) ((data.length + LegacyCameraDevice.nativeGetJpegFooterSize() + 3) & -4)))) + 15) & -16;
                        LegacyCameraDevice.setSurfaceDimens(s, dimen, dimen);
                        LegacyCameraDevice.produceFrame(s, data, dimen, dimen, 33);
                    }
                } catch (LegacyExceptionUtils.BufferQueueAbandonedException e) {
                    Log.w(RequestThreadManager.this.TAG, "Surface abandoned, dropping frame. ", e);
                }
            }
            RequestThreadManager.this.mReceivedJpeg.open();
        }
    };
    private final Camera.ShutterCallback mJpegShutterCallback = new Camera.ShutterCallback() {
        /* class android.hardware.camera2.legacy.RequestThreadManager.AnonymousClass3 */

        @Override // android.hardware.Camera.ShutterCallback
        public void onShutter() {
            RequestThreadManager.this.mCaptureCollector.jpegCaptured(SystemClock.elapsedRealtimeNanos());
        }
    };
    private final List<Long> mJpegSurfaceIds = new ArrayList();
    private LegacyRequest mLastRequest = null;
    private Camera.Parameters mParams;
    private final FpsCounter mPrevCounter = new FpsCounter("Incoming Preview");
    private final SurfaceTexture.OnFrameAvailableListener mPreviewCallback = new SurfaceTexture.OnFrameAvailableListener() {
        /* class android.hardware.camera2.legacy.RequestThreadManager.AnonymousClass4 */

        @Override // android.graphics.SurfaceTexture.OnFrameAvailableListener
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            if (RequestThreadManager.DEBUG) {
                RequestThreadManager.this.mPrevCounter.countAndLog();
            }
            RequestThreadManager.this.mGLThreadManager.queueNewFrame();
        }
    };
    private final List<Surface> mPreviewOutputs = new ArrayList();
    private boolean mPreviewRunning = false;
    private SurfaceTexture mPreviewTexture;
    private final ConditionVariable mReceivedJpeg = new ConditionVariable(false);
    private final FpsCounter mRequestCounter = new FpsCounter("Incoming Requests");
    private final Handler.Callback mRequestHandlerCb = new Handler.Callback() {
        /* class android.hardware.camera2.legacy.RequestThreadManager.AnonymousClass5 */
        private boolean mCleanup = false;
        private final LegacyResultMapper mMapper = new LegacyResultMapper();

        @Override // android.os.Handler.Callback
        public boolean handleMessage(Message msg) {
            if (this.mCleanup) {
                return true;
            }
            if (RequestThreadManager.DEBUG) {
                Log.d(RequestThreadManager.this.TAG, "Request thread handling message:" + msg.what);
            }
            long startTime = 0;
            if (RequestThreadManager.DEBUG) {
                startTime = SystemClock.elapsedRealtimeNanos();
            }
            switch (msg.what) {
                case -1:
                    break;
                case 0:
                default:
                    throw new AssertionError("Unhandled message " + msg.what + " on RequestThread.");
                case 1:
                    ConfigureHolder config = (ConfigureHolder) msg.obj;
                    Log.i(RequestThreadManager.this.TAG, "Configure outputs: " + (config.surfaces != null ? config.surfaces.size() : 0) + " surfaces configured.");
                    try {
                        if (!RequestThreadManager.this.mCaptureCollector.waitForEmpty(3000, TimeUnit.MILLISECONDS)) {
                            Log.e(RequestThreadManager.this.TAG, "Timed out while queueing configure request.");
                            RequestThreadManager.this.mCaptureCollector.failAll();
                        }
                        RequestThreadManager.this.configureOutputs(config.surfaces);
                        config.condition.open();
                        if (RequestThreadManager.DEBUG) {
                            Log.d(RequestThreadManager.this.TAG, "Configure took " + (SystemClock.elapsedRealtimeNanos() - startTime) + " ns");
                            break;
                        }
                    } catch (InterruptedException e) {
                        Log.e(RequestThreadManager.this.TAG, "Interrupted while waiting for requests to complete.");
                        RequestThreadManager.this.mDeviceState.setError(1);
                        break;
                    }
                    break;
                case 2:
                    Handler handler = RequestThreadManager.this.mRequestThread.getHandler();
                    Pair<BurstHolder, Long> nextBurst = RequestThreadManager.this.mRequestQueue.getNext();
                    if (nextBurst == null) {
                        try {
                            if (!RequestThreadManager.this.mCaptureCollector.waitForEmpty(3000, TimeUnit.MILLISECONDS)) {
                                Log.e(RequestThreadManager.this.TAG, "Timed out while waiting for prior requests to complete.");
                                RequestThreadManager.this.mCaptureCollector.failAll();
                            }
                            synchronized (RequestThreadManager.this.mIdleLock) {
                                nextBurst = RequestThreadManager.this.mRequestQueue.getNext();
                                if (nextBurst == null) {
                                    RequestThreadManager.this.mDeviceState.setIdle();
                                    break;
                                }
                            }
                        } catch (InterruptedException e2) {
                            Log.e(RequestThreadManager.this.TAG, "Interrupted while waiting for requests to complete: ", e2);
                            RequestThreadManager.this.mDeviceState.setError(1);
                            break;
                        }
                    }
                    if (nextBurst != null) {
                        handler.sendEmptyMessage(2);
                    }
                    for (RequestHolder holder : nextBurst.first.produceRequestHolders(nextBurst.second.longValue())) {
                        CaptureRequest request = holder.getRequest();
                        boolean paramsChanged = false;
                        if (RequestThreadManager.this.mLastRequest == null || RequestThreadManager.this.mLastRequest.captureRequest != request) {
                            LegacyRequest legacyRequest = new LegacyRequest(RequestThreadManager.this.mCharacteristics, request, ParameterUtils.convertSize(RequestThreadManager.this.mParams.getPreviewSize()), RequestThreadManager.this.mParams);
                            LegacyMetadataMapper.convertRequestMetadata(legacyRequest);
                            if (!RequestThreadManager.this.mParams.same(legacyRequest.parameters)) {
                                try {
                                    RequestThreadManager.this.mCamera.setParameters(legacyRequest.parameters);
                                    paramsChanged = true;
                                    RequestThreadManager.this.mParams = legacyRequest.parameters;
                                } catch (RuntimeException e3) {
                                    Log.e(RequestThreadManager.this.TAG, "Exception while setting camera parameters: ", e3);
                                    holder.failRequest();
                                    RequestThreadManager.this.mDeviceState.setCaptureStart(holder, 0, 3);
                                }
                            }
                            RequestThreadManager.this.mLastRequest = legacyRequest;
                        }
                        try {
                            if (!RequestThreadManager.this.mCaptureCollector.queueRequest(holder, RequestThreadManager.this.mLastRequest, 3000, TimeUnit.MILLISECONDS)) {
                                Log.e(RequestThreadManager.this.TAG, "Timed out while queueing capture request.");
                                holder.failRequest();
                                RequestThreadManager.this.mDeviceState.setCaptureStart(holder, 0, 3);
                            } else {
                                if (holder.hasPreviewTargets()) {
                                    RequestThreadManager.this.doPreviewCapture(holder);
                                }
                                if (holder.hasJpegTargets()) {
                                    while (!RequestThreadManager.this.mCaptureCollector.waitForPreviewsEmpty(1000, TimeUnit.MILLISECONDS)) {
                                        Log.e(RequestThreadManager.this.TAG, "Timed out while waiting for preview requests to complete.");
                                        RequestThreadManager.this.mCaptureCollector.failNextPreview();
                                    }
                                    RequestThreadManager.this.mReceivedJpeg.close();
                                    RequestThreadManager.this.doJpegCapturePrepare(holder);
                                }
                                RequestThreadManager.this.mFaceDetectMapper.processFaceDetectMode(request, RequestThreadManager.this.mParams);
                                RequestThreadManager.this.mFocusStateMapper.processRequestTriggers(request, RequestThreadManager.this.mParams);
                                if (holder.hasJpegTargets()) {
                                    RequestThreadManager.this.doJpegCapture(holder);
                                    if (!RequestThreadManager.this.mReceivedJpeg.block(3000)) {
                                        Log.e(RequestThreadManager.this.TAG, "Hit timeout for jpeg callback!");
                                        RequestThreadManager.this.mCaptureCollector.failNextJpeg();
                                    }
                                }
                                if (paramsChanged) {
                                    if (RequestThreadManager.DEBUG) {
                                        Log.d(RequestThreadManager.this.TAG, "Params changed -- getting new Parameters from HAL.");
                                    }
                                    try {
                                        RequestThreadManager.this.mParams = RequestThreadManager.this.mCamera.getParameters();
                                        RequestThreadManager.this.mLastRequest.setParameters(RequestThreadManager.this.mParams);
                                    } catch (RuntimeException e4) {
                                        Log.e(RequestThreadManager.this.TAG, "Received device exception: ", e4);
                                        RequestThreadManager.this.mDeviceState.setError(1);
                                    }
                                }
                                MutableLong timestampMutable = new MutableLong(0);
                                try {
                                    if (!RequestThreadManager.this.mCaptureCollector.waitForRequestCompleted(holder, 3000, TimeUnit.MILLISECONDS, timestampMutable)) {
                                        Log.e(RequestThreadManager.this.TAG, "Timed out while waiting for request to complete.");
                                        RequestThreadManager.this.mCaptureCollector.failAll();
                                    }
                                    CameraMetadataNative result = this.mMapper.cachedConvertResultMetadata(RequestThreadManager.this.mLastRequest, timestampMutable.value);
                                    RequestThreadManager.this.mFocusStateMapper.mapResultTriggers(result);
                                    RequestThreadManager.this.mFaceDetectMapper.mapResultFaces(result, RequestThreadManager.this.mLastRequest);
                                    if (!holder.requestFailed()) {
                                        RequestThreadManager.this.mDeviceState.setCaptureResult(holder, result, -1);
                                    }
                                } catch (InterruptedException e5) {
                                    Log.e(RequestThreadManager.this.TAG, "Interrupted waiting for request completion: ", e5);
                                    RequestThreadManager.this.mDeviceState.setError(1);
                                }
                            }
                        } catch (IOException e6) {
                            Log.e(RequestThreadManager.this.TAG, "Received device exception: ", e6);
                            RequestThreadManager.this.mDeviceState.setError(1);
                        } catch (InterruptedException e7) {
                            Log.e(RequestThreadManager.this.TAG, "Interrupted during capture: ", e7);
                            RequestThreadManager.this.mDeviceState.setError(1);
                        }
                    }
                    if (RequestThreadManager.DEBUG) {
                        Log.d(RequestThreadManager.this.TAG, "Capture request took " + (SystemClock.elapsedRealtimeNanos() - startTime) + " ns");
                        RequestThreadManager.this.mRequestCounter.countAndLog();
                        break;
                    }
                    break;
                case 3:
                    this.mCleanup = true;
                    try {
                        if (!RequestThreadManager.this.mCaptureCollector.waitForEmpty(3000, TimeUnit.MILLISECONDS)) {
                            Log.e(RequestThreadManager.this.TAG, "Timed out while queueing cleanup request.");
                            RequestThreadManager.this.mCaptureCollector.failAll();
                        }
                    } catch (InterruptedException e8) {
                        Log.e(RequestThreadManager.this.TAG, "Interrupted while waiting for requests to complete: ", e8);
                        RequestThreadManager.this.mDeviceState.setError(1);
                    }
                    if (RequestThreadManager.this.mGLThreadManager != null) {
                        RequestThreadManager.this.mGLThreadManager.quit();
                    }
                    if (RequestThreadManager.this.mCamera != null) {
                        RequestThreadManager.this.mCamera.release();
                    }
                    RequestThreadManager.this.resetJpegSurfaceFormats(RequestThreadManager.this.mCallbackOutputs);
                    break;
            }
            return true;
        }
    };
    private final RequestQueue mRequestQueue = new RequestQueue(this.mJpegSurfaceIds);
    private final RequestHandlerThread mRequestThread;

    /* access modifiers changed from: private */
    public static class ConfigureHolder {
        public final ConditionVariable condition;
        public final Collection<Surface> surfaces;

        public ConfigureHolder(ConditionVariable condition2, Collection<Surface> surfaces2) {
            this.condition = condition2;
            this.surfaces = surfaces2;
        }
    }

    public static class FpsCounter {
        private static final long NANO_PER_SECOND = 1000000000;
        private static final String TAG = "FpsCounter";
        private int mFrameCount = 0;
        private double mLastFps = 0.0d;
        private long mLastPrintTime = 0;
        private long mLastTime = 0;
        private final String mStreamType;

        public FpsCounter(String streamType) {
            this.mStreamType = streamType;
        }

        public synchronized void countFrame() {
            this.mFrameCount++;
            long nextTime = SystemClock.elapsedRealtimeNanos();
            if (this.mLastTime == 0) {
                this.mLastTime = nextTime;
            }
            if (nextTime > this.mLastTime + NANO_PER_SECOND) {
                this.mLastFps = ((double) this.mFrameCount) * (1.0E9d / ((double) (nextTime - this.mLastTime)));
                this.mFrameCount = 0;
                this.mLastTime = nextTime;
            }
        }

        public synchronized double checkFps() {
            return this.mLastFps;
        }

        public synchronized void staggeredLog() {
            if (this.mLastTime > this.mLastPrintTime + 5000000000L) {
                this.mLastPrintTime = this.mLastTime;
                Log.d(TAG, "FPS for " + this.mStreamType + " stream: " + this.mLastFps);
            }
        }

        public synchronized void countAndLog() {
            countFrame();
            staggeredLog();
        }
    }

    private void createDummySurface() {
        if (this.mDummyTexture == null || this.mDummySurface == null) {
            this.mDummyTexture = new SurfaceTexture(0);
            this.mDummyTexture.setDefaultBufferSize(DisplayMetrics.DENSITY_XXXHIGH, DisplayMetrics.DENSITY_XXHIGH);
            this.mDummySurface = new Surface(this.mDummyTexture);
        }
    }

    private void stopPreview() {
        if (VERBOSE) {
            Log.v(this.TAG, "stopPreview - preview running? " + this.mPreviewRunning);
        }
        if (this.mPreviewRunning) {
            this.mCamera.stopPreview();
            this.mPreviewRunning = false;
        }
    }

    private void startPreview() {
        if (VERBOSE) {
            Log.v(this.TAG, "startPreview - preview running? " + this.mPreviewRunning);
        }
        if (!this.mPreviewRunning) {
            this.mCamera.startPreview();
            this.mPreviewRunning = true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doJpegCapturePrepare(RequestHolder request) throws IOException {
        if (DEBUG) {
            Log.d(this.TAG, "doJpegCapturePrepare - preview running? " + this.mPreviewRunning);
        }
        if (!this.mPreviewRunning) {
            if (DEBUG) {
                Log.d(this.TAG, "doJpegCapture - create fake surface");
            }
            createDummySurface();
            this.mCamera.setPreviewTexture(this.mDummyTexture);
            startPreview();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doJpegCapture(RequestHolder request) {
        if (DEBUG) {
            Log.d(this.TAG, "doJpegCapturePrepare");
        }
        this.mCamera.takePicture(this.mJpegShutterCallback, null, this.mJpegCallback);
        this.mPreviewRunning = false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doPreviewCapture(RequestHolder request) throws IOException {
        if (VERBOSE) {
            Log.v(this.TAG, "doPreviewCapture - preview running? " + this.mPreviewRunning);
        }
        if (!this.mPreviewRunning) {
            if (this.mPreviewTexture == null) {
                throw new IllegalStateException("Preview capture called with no preview surfaces configured.");
            }
            this.mPreviewTexture.setDefaultBufferSize(this.mIntermediateBufferSize.getWidth(), this.mIntermediateBufferSize.getHeight());
            this.mCamera.setPreviewTexture(this.mPreviewTexture);
            startPreview();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void configureOutputs(Collection<Surface> outputs) {
        if (DEBUG) {
            Log.d(this.TAG, "configureOutputs with " + (outputs == null ? "null" : outputs.size() + " surfaces"));
        }
        stopPreview();
        try {
            this.mCamera.setPreviewTexture(null);
        } catch (IOException e) {
            Log.w(this.TAG, "Failed to clear prior SurfaceTexture, may cause GL deadlock: ", e);
        }
        if (this.mGLThreadManager != null) {
            this.mGLThreadManager.waitUntilStarted();
            this.mGLThreadManager.ignoreNewFrames();
            this.mGLThreadManager.waitUntilIdle();
        }
        resetJpegSurfaceFormats(this.mCallbackOutputs);
        this.mPreviewOutputs.clear();
        this.mCallbackOutputs.clear();
        this.mJpegSurfaceIds.clear();
        this.mPreviewTexture = null;
        int facing = ((Integer) this.mCharacteristics.get(CameraCharacteristics.LENS_FACING)).intValue();
        int orientation = ((Integer) this.mCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)).intValue();
        if (outputs != null) {
            for (Surface s : outputs) {
                try {
                    int format = LegacyCameraDevice.detectSurfaceType(s);
                    LegacyCameraDevice.setSurfaceOrientation(s, facing, orientation);
                    switch (format) {
                        case 33:
                            LegacyCameraDevice.setSurfaceFormat(s, 1);
                            this.mJpegSurfaceIds.add(Long.valueOf(LegacyCameraDevice.getSurfaceId(s)));
                            this.mCallbackOutputs.add(s);
                            continue;
                        default:
                            this.mPreviewOutputs.add(s);
                            continue;
                    }
                } catch (LegacyExceptionUtils.BufferQueueAbandonedException e2) {
                    Log.w(this.TAG, "Surface abandoned, skipping...", e2);
                }
            }
        }
        try {
            this.mParams = this.mCamera.getParameters();
            int[] bestRange = getPhotoPreviewFpsRange(this.mParams.getSupportedPreviewFpsRange());
            if (DEBUG) {
                Log.d(this.TAG, "doPreviewCapture - Selected range [" + bestRange[0] + "," + bestRange[1] + "]");
            }
            this.mParams.setPreviewFpsRange(bestRange[0], bestRange[1]);
            if (this.mPreviewOutputs.size() > 0) {
                List<Size> outputSizes = new ArrayList<>(outputs.size());
                for (Surface s2 : this.mPreviewOutputs) {
                    try {
                        outputSizes.add(LegacyCameraDevice.getSurfaceSize(s2));
                    } catch (LegacyExceptionUtils.BufferQueueAbandonedException e3) {
                        Log.w(this.TAG, "Surface abandoned, skipping...", e3);
                    }
                }
                Size largestOutput = SizeAreaComparator.findLargestByArea(outputSizes);
                Size largestJpegDimen = ParameterUtils.getLargestSupportedJpegSizeByArea(this.mParams);
                List<Size> supportedPreviewSizes = ParameterUtils.convertSizeList(this.mParams.getSupportedPreviewSizes());
                long largestOutputArea = ((long) largestOutput.getHeight()) * ((long) largestOutput.getWidth());
                Size bestPreviewDimen = SizeAreaComparator.findLargestByArea(supportedPreviewSizes);
                for (Size s3 : supportedPreviewSizes) {
                    long currArea = (long) (s3.getWidth() * s3.getHeight());
                    long bestArea = (long) (bestPreviewDimen.getWidth() * bestPreviewDimen.getHeight());
                    if (checkAspectRatiosMatch(largestJpegDimen, s3) && currArea < bestArea && currArea >= largestOutputArea) {
                        bestPreviewDimen = s3;
                    }
                }
                this.mIntermediateBufferSize = bestPreviewDimen;
                this.mParams.setPreviewSize(this.mIntermediateBufferSize.getWidth(), this.mIntermediateBufferSize.getHeight());
                if (DEBUG) {
                    Log.d(this.TAG, "Intermediate buffer selected with dimens: " + bestPreviewDimen.toString());
                }
            } else {
                this.mIntermediateBufferSize = null;
                if (DEBUG) {
                    Log.d(this.TAG, "No Intermediate buffer selected, no preview outputs were configured");
                }
            }
            Size smallestSupportedJpegSize = calculatePictureSize(this.mCallbackOutputs, this.mParams);
            if (smallestSupportedJpegSize != null) {
                Log.i(this.TAG, "configureOutputs - set take picture size to " + smallestSupportedJpegSize);
                this.mParams.setPictureSize(smallestSupportedJpegSize.getWidth(), smallestSupportedJpegSize.getHeight());
            }
            if (this.mGLThreadManager == null) {
                this.mGLThreadManager = new GLThreadManager(this.mCameraId, facing, this.mDeviceState);
                this.mGLThreadManager.start();
            }
            this.mGLThreadManager.waitUntilStarted();
            this.mGLThreadManager.setConfigurationAndWait(this.mPreviewOutputs, this.mCaptureCollector);
            this.mGLThreadManager.allowNewFrames();
            this.mPreviewTexture = this.mGLThreadManager.getCurrentSurfaceTexture();
            if (this.mPreviewTexture != null) {
                this.mPreviewTexture.setOnFrameAvailableListener(this.mPreviewCallback);
            }
            this.mCamera.setParameters(this.mParams);
        } catch (RuntimeException e4) {
            Log.e(this.TAG, "Received device exception: ", e4);
            this.mDeviceState.setError(1);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resetJpegSurfaceFormats(Collection<Surface> surfaces) {
        if (surfaces != null) {
            for (Surface s : surfaces) {
                try {
                    LegacyCameraDevice.setSurfaceFormat(s, 33);
                } catch (LegacyExceptionUtils.BufferQueueAbandonedException e) {
                    Log.w(this.TAG, "Surface abandoned, skipping...", e);
                }
            }
        }
    }

    private Size calculatePictureSize(Collection<Surface> callbackOutputs, Camera.Parameters params) {
        List<Size> configuredJpegSizes = new ArrayList<>();
        for (Surface callbackSurface : callbackOutputs) {
            try {
                if (LegacyCameraDevice.containsSurfaceId(callbackSurface, this.mJpegSurfaceIds)) {
                    configuredJpegSizes.add(LegacyCameraDevice.getSurfaceSize(callbackSurface));
                }
            } catch (LegacyExceptionUtils.BufferQueueAbandonedException e) {
                Log.w(this.TAG, "Surface abandoned, skipping...", e);
            }
        }
        if (configuredJpegSizes.isEmpty()) {
            return null;
        }
        int maxConfiguredJpegWidth = -1;
        int maxConfiguredJpegHeight = -1;
        for (Size jpegSize : configuredJpegSizes) {
            if (jpegSize.getWidth() > maxConfiguredJpegWidth) {
                maxConfiguredJpegWidth = jpegSize.getWidth();
            }
            if (jpegSize.getHeight() > maxConfiguredJpegHeight) {
                maxConfiguredJpegHeight = jpegSize.getHeight();
            }
        }
        Size smallestBoundJpegSize = new Size(maxConfiguredJpegWidth, maxConfiguredJpegHeight);
        List<Size> supportedJpegSizes = ParameterUtils.convertSizeList(params.getSupportedPictureSizes());
        List<Size> candidateSupportedJpegSizes = new ArrayList<>();
        for (Size supportedJpegSize : supportedJpegSizes) {
            if (supportedJpegSize.getWidth() >= maxConfiguredJpegWidth && supportedJpegSize.getHeight() >= maxConfiguredJpegHeight) {
                candidateSupportedJpegSizes.add(supportedJpegSize);
            }
        }
        if (candidateSupportedJpegSizes.isEmpty()) {
            throw new AssertionError("Could not find any supported JPEG sizes large enough to fit " + smallestBoundJpegSize);
        }
        Size smallestSupportedJpegSize = (Size) Collections.min(candidateSupportedJpegSizes, new SizeAreaComparator());
        if (smallestSupportedJpegSize.equals(smallestBoundJpegSize)) {
            return smallestSupportedJpegSize;
        }
        Log.w(this.TAG, String.format("configureOutputs - Will need to crop picture %s into smallest bound size %s", smallestSupportedJpegSize, smallestBoundJpegSize));
        return smallestSupportedJpegSize;
    }

    private static boolean checkAspectRatiosMatch(Size a, Size b) {
        return Math.abs((((float) a.getWidth()) / ((float) a.getHeight())) - (((float) b.getWidth()) / ((float) b.getHeight()))) < ASPECT_RATIO_TOLERANCE;
    }

    private int[] getPhotoPreviewFpsRange(List<int[]> frameRates) {
        if (frameRates.size() == 0) {
            Log.e(this.TAG, "No supported frame rates returned!");
            return null;
        }
        int bestMin = 0;
        int bestMax = 0;
        int bestIndex = 0;
        int index = 0;
        for (int[] rate : frameRates) {
            int minFps = rate[0];
            int maxFps = rate[1];
            if (maxFps > bestMax || (maxFps == bestMax && minFps > bestMin)) {
                bestMin = minFps;
                bestMax = maxFps;
                bestIndex = index;
            }
            index++;
        }
        return frameRates.get(bestIndex);
    }

    public RequestThreadManager(int cameraId, Camera camera, CameraCharacteristics characteristics, CameraDeviceState deviceState) {
        this.mCamera = (Camera) Preconditions.checkNotNull(camera, "camera must not be null");
        this.mCameraId = cameraId;
        this.mCharacteristics = (CameraCharacteristics) Preconditions.checkNotNull(characteristics, "characteristics must not be null");
        String name = String.format("RequestThread-%d", Integer.valueOf(cameraId));
        this.TAG = name;
        this.mDeviceState = (CameraDeviceState) Preconditions.checkNotNull(deviceState, "deviceState must not be null");
        this.mFocusStateMapper = new LegacyFocusStateMapper(this.mCamera);
        this.mFaceDetectMapper = new LegacyFaceDetectMapper(this.mCamera, this.mCharacteristics);
        this.mCaptureCollector = new CaptureCollector(2, this.mDeviceState);
        this.mRequestThread = new RequestHandlerThread(name, this.mRequestHandlerCb);
        this.mCamera.setErrorCallback(this.mErrorCallback);
    }

    public void start() {
        this.mRequestThread.start();
    }

    public long flush() {
        Log.i(this.TAG, "Flushing all pending requests.");
        long lastFrame = this.mRequestQueue.stopRepeating();
        this.mCaptureCollector.failAll();
        return lastFrame;
    }

    public void quit() {
        Handler handler = this.mRequestThread.waitAndGetHandler();
        handler.sendMessageAtFrontOfQueue(handler.obtainMessage(3));
        this.mRequestThread.quitSafely();
        try {
            this.mRequestThread.join();
        } catch (InterruptedException e) {
            Log.e(this.TAG, String.format("Thread %s (%d) interrupted while quitting.", this.mRequestThread.getName(), Long.valueOf(this.mRequestThread.getId())));
        }
    }

    public int submitCaptureRequests(List<CaptureRequest> requests, boolean repeating, LongParcelable frameNumber) {
        int ret;
        Handler handler = this.mRequestThread.waitAndGetHandler();
        synchronized (this.mIdleLock) {
            ret = this.mRequestQueue.submit(requests, repeating, frameNumber);
            handler.sendEmptyMessage(2);
        }
        return ret;
    }

    public long cancelRepeating(int requestId) {
        return this.mRequestQueue.stopRepeating(requestId);
    }

    public void configure(Collection<Surface> outputs) {
        Handler handler = this.mRequestThread.waitAndGetHandler();
        ConditionVariable condition = new ConditionVariable(false);
        handler.sendMessage(handler.obtainMessage(1, 0, 0, new ConfigureHolder(condition, outputs)));
        condition.block();
    }
}

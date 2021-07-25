package android.graphics;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Surface;
import java.lang.ref.WeakReference;

public class SurfaceTexture {
    private final Looper mCreatorLooper;
    private long mFrameAvailableListener;
    private Handler mOnFrameAvailableHandler;
    private long mProducer;
    private long mSurfaceTexture;

    public interface OnFrameAvailableListener {
        void onFrameAvailable(SurfaceTexture surfaceTexture);
    }

    private native int nativeAttachToGLContext(int i);

    private static native void nativeClassInit();

    private native int nativeDetachFromGLContext();

    private native void nativeFinalize();

    private native int nativeGetQueuedCount();

    private native long nativeGetTimestamp();

    private native void nativeGetTransformMatrix(float[] fArr);

    private native void nativeInit(boolean z, int i, boolean z2, WeakReference<SurfaceTexture> weakReference) throws Surface.OutOfResourcesException;

    private native void nativeRelease();

    private native void nativeReleaseTexImage();

    private native void nativeSetDefaultBufferSize(int i, int i2);

    private native void nativeUpdateTexImage();

    @Deprecated
    public static class OutOfResourcesException extends Exception {
        public OutOfResourcesException() {
        }

        public OutOfResourcesException(String name) {
            super(name);
        }
    }

    public SurfaceTexture(int texName) {
        this(texName, false);
    }

    public SurfaceTexture(int texName, boolean singleBufferMode) {
        this.mCreatorLooper = Looper.myLooper();
        nativeInit(false, texName, singleBufferMode, new WeakReference<>(this));
    }

    public SurfaceTexture(boolean singleBufferMode) {
        this.mCreatorLooper = Looper.myLooper();
        nativeInit(true, 0, singleBufferMode, new WeakReference<>(this));
    }

    public void setOnFrameAvailableListener(OnFrameAvailableListener listener) {
        setOnFrameAvailableListener(listener, null);
    }

    public void setOnFrameAvailableListener(final OnFrameAvailableListener listener, Handler handler) {
        if (listener != null) {
            this.mOnFrameAvailableHandler = new Handler(handler != null ? handler.getLooper() : this.mCreatorLooper != null ? this.mCreatorLooper : Looper.getMainLooper(), null, true) {
                /* class android.graphics.SurfaceTexture.AnonymousClass1 */

                @Override // android.os.Handler
                public void handleMessage(Message msg) {
                    listener.onFrameAvailable(SurfaceTexture.this);
                }
            };
        } else {
            this.mOnFrameAvailableHandler = null;
        }
    }

    public void setDefaultBufferSize(int width, int height) {
        nativeSetDefaultBufferSize(width, height);
    }

    public void updateTexImage() {
        nativeUpdateTexImage();
    }

    public void releaseTexImage() {
        nativeReleaseTexImage();
    }

    public void detachFromGLContext() {
        if (nativeDetachFromGLContext() != 0) {
            throw new RuntimeException("Error during detachFromGLContext (see logcat for details)");
        }
    }

    public void attachToGLContext(int texName) {
        if (nativeAttachToGLContext(texName) != 0) {
            throw new RuntimeException("Error during attachToGLContext (see logcat for details)");
        }
    }

    public void getTransformMatrix(float[] mtx) {
        if (mtx.length != 16) {
            throw new IllegalArgumentException();
        }
        nativeGetTransformMatrix(mtx);
    }

    public long getTimestamp() {
        return nativeGetTimestamp();
    }

    public void release() {
        nativeRelease();
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            nativeFinalize();
        } finally {
            super.finalize();
        }
    }

    private static void postEventFromNative(WeakReference<SurfaceTexture> weakSelf) {
        Handler handler;
        SurfaceTexture st = weakSelf.get();
        if (st != null && (handler = st.mOnFrameAvailableHandler) != null) {
            handler.sendEmptyMessage(0);
        }
    }

    static {
        nativeClassInit();
    }
}

package android.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.util.Log;

public class TextureView extends View {
    private static final String LOG_TAG = "TextureView";
    private Canvas mCanvas;
    private boolean mHadSurface;
    private HardwareLayer mLayer;
    private SurfaceTextureListener mListener;
    private final Object[] mLock;
    private final Matrix mMatrix;
    private boolean mMatrixChanged;
    private long mNativeWindow;
    private final Object[] mNativeWindowLock;
    private boolean mOpaque;
    private int mSaveCount;
    private SurfaceTexture mSurface;
    private boolean mUpdateLayer;
    private final SurfaceTexture.OnFrameAvailableListener mUpdateListener;
    private boolean mUpdateSurface;

    public interface SurfaceTextureListener {
        void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i2);

        boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture);

        void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i2);

        void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture);
    }

    private native void nCreateNativeWindow(SurfaceTexture surfaceTexture);

    private native void nDestroyNativeWindow();

    private static native boolean nLockCanvas(long j, Canvas canvas, Rect rect);

    private static native void nUnlockCanvasAndPost(long j, Canvas canvas);

    public TextureView(Context context) {
        super(context);
        this.mOpaque = true;
        this.mMatrix = new Matrix();
        this.mLock = new Object[0];
        this.mNativeWindowLock = new Object[0];
        this.mUpdateListener = new SurfaceTexture.OnFrameAvailableListener() {
            /* class android.view.TextureView.AnonymousClass1 */

            @Override // android.graphics.SurfaceTexture.OnFrameAvailableListener
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                TextureView.this.updateLayer();
                TextureView.this.invalidate();
            }
        };
        init();
    }

    public TextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mOpaque = true;
        this.mMatrix = new Matrix();
        this.mLock = new Object[0];
        this.mNativeWindowLock = new Object[0];
        this.mUpdateListener = new SurfaceTexture.OnFrameAvailableListener() {
            /* class android.view.TextureView.AnonymousClass1 */

            @Override // android.graphics.SurfaceTexture.OnFrameAvailableListener
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                TextureView.this.updateLayer();
                TextureView.this.invalidate();
            }
        };
        init();
    }

    public TextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mOpaque = true;
        this.mMatrix = new Matrix();
        this.mLock = new Object[0];
        this.mNativeWindowLock = new Object[0];
        this.mUpdateListener = new SurfaceTexture.OnFrameAvailableListener() {
            /* class android.view.TextureView.AnonymousClass1 */

            @Override // android.graphics.SurfaceTexture.OnFrameAvailableListener
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                TextureView.this.updateLayer();
                TextureView.this.invalidate();
            }
        };
        init();
    }

    public TextureView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mOpaque = true;
        this.mMatrix = new Matrix();
        this.mLock = new Object[0];
        this.mNativeWindowLock = new Object[0];
        this.mUpdateListener = new SurfaceTexture.OnFrameAvailableListener() {
            /* class android.view.TextureView.AnonymousClass1 */

            @Override // android.graphics.SurfaceTexture.OnFrameAvailableListener
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                TextureView.this.updateLayer();
                TextureView.this.invalidate();
            }
        };
        init();
    }

    private void init() {
        this.mLayerPaint = new Paint();
    }

    @Override // android.view.View
    public boolean isOpaque() {
        return this.mOpaque;
    }

    public void setOpaque(boolean opaque) {
        if (opaque != this.mOpaque) {
            this.mOpaque = opaque;
            if (this.mLayer != null) {
                updateLayerAndInvalidate();
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isHardwareAccelerated()) {
            Log.w(LOG_TAG, "A TextureView or a subclass can only be used with hardware acceleration enabled.");
        }
        if (this.mHadSurface) {
            invalidate(true);
            this.mHadSurface = false;
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onDetachedFromWindowInternal() {
        destroySurface();
        super.onDetachedFromWindowInternal();
    }

    private void destroySurface() {
        if (this.mLayer != null) {
            this.mLayer.detachSurfaceTexture();
            boolean shouldRelease = true;
            if (this.mListener != null) {
                shouldRelease = this.mListener.onSurfaceTextureDestroyed(this.mSurface);
            }
            synchronized (this.mNativeWindowLock) {
                nDestroyNativeWindow();
            }
            this.mLayer.destroy();
            if (shouldRelease) {
                this.mSurface.release();
            }
            this.mSurface = null;
            this.mLayer = null;
            this.mHadSurface = true;
        }
    }

    @Override // android.view.View
    public void setLayerType(int layerType, Paint paint) {
        if (paint != this.mLayerPaint) {
            if (paint == null) {
                paint = new Paint();
            }
            this.mLayerPaint = paint;
            invalidate();
        }
    }

    @Override // android.view.View
    public void setLayerPaint(Paint paint) {
        setLayerType(0, paint);
    }

    @Override // android.view.View
    public int getLayerType() {
        return 2;
    }

    /* access modifiers changed from: package-private */
    @Override // android.view.View
    public boolean hasStaticLayer() {
        return true;
    }

    @Override // android.view.View
    public void buildLayer() {
    }

    @Override // android.view.View
    public final void draw(Canvas canvas) {
        this.mPrivateFlags = (this.mPrivateFlags & -6291457) | 32;
        applyUpdate();
        applyTransformMatrix();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public final void onDraw(Canvas canvas) {
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (this.mSurface != null) {
            this.mSurface.setDefaultBufferSize(getWidth(), getHeight());
            updateLayer();
            if (this.mListener != null) {
                this.mListener.onSurfaceTextureSizeChanged(this.mSurface, getWidth(), getHeight());
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void destroyHardwareResources() {
        super.destroyHardwareResources();
        destroySurface();
        invalidateParentCaches();
        invalidate(true);
    }

    /* access modifiers changed from: package-private */
    @Override // android.view.View
    public HardwareLayer getHardwareLayer() {
        this.mPrivateFlags |= 32800;
        this.mPrivateFlags &= -6291457;
        if (this.mLayer == null) {
            if (this.mAttachInfo == null || this.mAttachInfo.mHardwareRenderer == null) {
                return null;
            }
            this.mLayer = this.mAttachInfo.mHardwareRenderer.createTextureLayer();
            if (!this.mUpdateSurface) {
                this.mSurface = new SurfaceTexture(false);
                this.mLayer.setSurfaceTexture(this.mSurface);
            }
            this.mSurface.setDefaultBufferSize(getWidth(), getHeight());
            nCreateNativeWindow(this.mSurface);
            this.mSurface.setOnFrameAvailableListener(this.mUpdateListener, this.mAttachInfo.mHandler);
            if (this.mListener != null && !this.mUpdateSurface) {
                this.mListener.onSurfaceTextureAvailable(this.mSurface, getWidth(), getHeight());
            }
            this.mLayer.setLayerPaint(this.mLayerPaint);
        }
        if (this.mUpdateSurface) {
            this.mUpdateSurface = false;
            updateLayer();
            this.mMatrixChanged = true;
            this.mLayer.setSurfaceTexture(this.mSurface);
            this.mSurface.setDefaultBufferSize(getWidth(), getHeight());
        }
        applyUpdate();
        applyTransformMatrix();
        return this.mLayer;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (this.mSurface == null) {
            return;
        }
        if (visibility == 0) {
            if (this.mLayer != null) {
                this.mSurface.setOnFrameAvailableListener(this.mUpdateListener, this.mAttachInfo.mHandler);
            }
            updateLayerAndInvalidate();
            return;
        }
        this.mSurface.setOnFrameAvailableListener(null);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateLayer() {
        synchronized (this.mLock) {
            this.mUpdateLayer = true;
        }
    }

    private void updateLayerAndInvalidate() {
        synchronized (this.mLock) {
            this.mUpdateLayer = true;
        }
        invalidate();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0026, code lost:
        if (r4.mListener == null) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0028, code lost:
        r4.mListener.onSurfaceTextureUpdated(r4.mSurface);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0010, code lost:
        r4.mLayer.prepare(getWidth(), getHeight(), r4.mOpaque);
        r4.mLayer.updateSurfaceTexture();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void applyUpdate() {
        /*
            r4 = this;
            android.view.HardwareLayer r0 = r4.mLayer
            if (r0 != 0) goto L_0x0005
        L_0x0004:
            return
        L_0x0005:
            java.lang.Object[] r1 = r4.mLock
            monitor-enter(r1)
            boolean r0 = r4.mUpdateLayer     // Catch:{ all -> 0x0032 }
            if (r0 == 0) goto L_0x0030
            r0 = 0
            r4.mUpdateLayer = r0     // Catch:{ all -> 0x0032 }
            monitor-exit(r1)     // Catch:{ all -> 0x0032 }
            android.view.HardwareLayer r0 = r4.mLayer
            int r1 = r4.getWidth()
            int r2 = r4.getHeight()
            boolean r3 = r4.mOpaque
            r0.prepare(r1, r2, r3)
            android.view.HardwareLayer r0 = r4.mLayer
            r0.updateSurfaceTexture()
            android.view.TextureView$SurfaceTextureListener r0 = r4.mListener
            if (r0 == 0) goto L_0x0004
            android.view.TextureView$SurfaceTextureListener r0 = r4.mListener
            android.graphics.SurfaceTexture r1 = r4.mSurface
            r0.onSurfaceTextureUpdated(r1)
            goto L_0x0004
        L_0x0030:
            monitor-exit(r1)
            goto L_0x0004
        L_0x0032:
            r0 = move-exception
            monitor-exit(r1)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.TextureView.applyUpdate():void");
    }

    public void setTransform(Matrix transform) {
        this.mMatrix.set(transform);
        this.mMatrixChanged = true;
        invalidateParentIfNeeded();
    }

    public Matrix getTransform(Matrix transform) {
        if (transform == null) {
            transform = new Matrix();
        }
        transform.set(this.mMatrix);
        return transform;
    }

    private void applyTransformMatrix() {
        if (this.mMatrixChanged && this.mLayer != null) {
            this.mLayer.setTransform(this.mMatrix);
            this.mMatrixChanged = false;
        }
    }

    public Bitmap getBitmap() {
        return getBitmap(getWidth(), getHeight());
    }

    public Bitmap getBitmap(int width, int height) {
        if (!isAvailable() || width <= 0 || height <= 0) {
            return null;
        }
        return getBitmap(Bitmap.createBitmap(getResources().getDisplayMetrics(), width, height, Bitmap.Config.ARGB_8888));
    }

    public Bitmap getBitmap(Bitmap bitmap) {
        if (bitmap != null && isAvailable()) {
            applyUpdate();
            applyTransformMatrix();
            if (this.mLayer == null && this.mUpdateSurface) {
                getHardwareLayer();
            }
            if (this.mLayer != null) {
                this.mLayer.copyInto(bitmap);
            }
        }
        return bitmap;
    }

    public boolean isAvailable() {
        return this.mSurface != null;
    }

    public Canvas lockCanvas() {
        return lockCanvas(null);
    }

    public Canvas lockCanvas(Rect dirty) {
        if (!isAvailable()) {
            return null;
        }
        if (this.mCanvas == null) {
            this.mCanvas = new Canvas();
        }
        synchronized (this.mNativeWindowLock) {
            if (!nLockCanvas(this.mNativeWindow, this.mCanvas, dirty)) {
                return null;
            }
            this.mSaveCount = this.mCanvas.save();
            return this.mCanvas;
        }
    }

    public void unlockCanvasAndPost(Canvas canvas) {
        if (this.mCanvas != null && canvas == this.mCanvas) {
            canvas.restoreToCount(this.mSaveCount);
            this.mSaveCount = 0;
            synchronized (this.mNativeWindowLock) {
                nUnlockCanvasAndPost(this.mNativeWindow, this.mCanvas);
            }
        }
    }

    public SurfaceTexture getSurfaceTexture() {
        return this.mSurface;
    }

    public void setSurfaceTexture(SurfaceTexture surfaceTexture) {
        if (surfaceTexture == null) {
            throw new NullPointerException("surfaceTexture must not be null");
        }
        if (this.mSurface != null) {
            this.mSurface.release();
        }
        this.mSurface = surfaceTexture;
        this.mUpdateSurface = true;
        invalidateParentIfNeeded();
    }

    public SurfaceTextureListener getSurfaceTextureListener() {
        return this.mListener;
    }

    public void setSurfaceTextureListener(SurfaceTextureListener listener) {
        this.mListener = listener;
    }
}

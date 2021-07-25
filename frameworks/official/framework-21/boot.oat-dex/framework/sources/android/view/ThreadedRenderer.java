package android.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.Trace;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.TimeUtils;
import android.view.HardwareRenderer;
import android.view.IAssetAtlas;
import android.view.Surface;
import android.view.View;
import com.android.internal.R;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.HashSet;

public class ThreadedRenderer extends HardwareRenderer {
    private static final String LOGTAG = "ThreadedRenderer";
    private static final int SYNC_INVALIDATE_REQUIRED = 1;
    private static final int SYNC_OK = 0;
    private static final String[] VISUALIZERS = {HardwareRenderer.PROFILE_PROPERTY_VISUALIZE_BARS};
    private final int mAmbientShadowAlpha;
    private Choreographer mChoreographer;
    private boolean mHasInsets;
    private int mHeight;
    private boolean mInitialized = false;
    private int mInsetLeft;
    private int mInsetTop;
    private final float mLightRadius;
    private final float mLightY;
    private final float mLightZ;
    private long mNativeProxy;
    private boolean mProfilingEnabled;
    private RenderNode mRootNode;
    private boolean mRootNodeNeedsUpdate;
    private final int mSpotShadowAlpha;
    private int mSurfaceHeight;
    private int mSurfaceWidth;
    private int mWidth;

    private static native void nBuildLayer(long j, long j2);

    private static native void nCancelLayerUpdate(long j, long j2);

    private static native boolean nCopyLayerInto(long j, long j2, long j3);

    private static native long nCreateProxy(boolean z, long j);

    private static native long nCreateRootRenderNode();

    private static native long nCreateTextureLayer(long j);

    private static native void nDeleteProxy(long j);

    private static native void nDestroy(long j);

    private static native void nDestroyHardwareResources(long j);

    private static native void nDetachSurfaceTexture(long j, long j2);

    private static native void nDumpProfileInfo(long j, FileDescriptor fileDescriptor);

    private static native void nFence(long j);

    private static native boolean nInitialize(long j, Surface surface);

    private static native void nInvokeFunctor(long j, boolean z);

    private static native boolean nLoadSystemProperties(long j);

    private static native void nNotifyFramePending(long j);

    private static native void nPauseSurface(long j, Surface surface);

    private static native void nPushLayerUpdate(long j, long j2);

    private static native void nRegisterAnimatingRenderNode(long j, long j2);

    /* access modifiers changed from: private */
    public static native void nSetAtlas(long j, GraphicBuffer graphicBuffer, long[] jArr);

    private static native void nSetFrameInterval(long j, long j2);

    private static native void nSetOpaque(long j, boolean z);

    private static native void nSetup(long j, int i, int i2, float f, float f2, float f3, float f4, int i3, int i4);

    private static native void nStopDrawing(long j);

    private static native int nSyncAndDrawFrame(long j, long j2, long j3, float f);

    private static native void nTrimMemory(int i);

    private static native void nUpdateSurface(long j, Surface surface);

    static native void setupShadersDiskCache(String str);

    ThreadedRenderer(Context context, boolean translucent) {
        TypedArray a = context.obtainStyledAttributes(null, R.styleable.Lighting, 0, 0);
        this.mLightY = a.getDimension(2, 0.0f);
        this.mLightZ = a.getDimension(3, 0.0f);
        this.mLightRadius = a.getDimension(4, 0.0f);
        this.mAmbientShadowAlpha = (int) ((a.getFloat(0, 0.0f) * 255.0f) + 0.5f);
        this.mSpotShadowAlpha = (int) ((a.getFloat(1, 0.0f) * 255.0f) + 0.5f);
        a.recycle();
        long rootNodePtr = nCreateRootRenderNode();
        this.mRootNode = RenderNode.adopt(rootNodePtr);
        this.mRootNode.setClipToBounds(false);
        this.mNativeProxy = nCreateProxy(translucent, rootNodePtr);
        AtlasInitializer.sInstance.init(context, this.mNativeProxy);
        this.mChoreographer = Choreographer.getInstance();
        nSetFrameInterval(this.mNativeProxy, this.mChoreographer.getFrameIntervalNanos());
        loadSystemProperties();
    }

    /* access modifiers changed from: package-private */
    @Override // android.view.HardwareRenderer
    public void destroy() {
        this.mInitialized = false;
        updateEnabledState(null);
        nDestroy(this.mNativeProxy);
    }

    private void updateEnabledState(Surface surface) {
        if (surface == null || !surface.isValid()) {
            setEnabled(false);
        } else {
            setEnabled(this.mInitialized);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // android.view.HardwareRenderer
    public boolean initialize(Surface surface) throws Surface.OutOfResourcesException {
        this.mInitialized = true;
        updateEnabledState(surface);
        boolean status = nInitialize(this.mNativeProxy, surface);
        surface.allocateBuffers();
        return status;
    }

    /* access modifiers changed from: package-private */
    @Override // android.view.HardwareRenderer
    public void updateSurface(Surface surface) throws Surface.OutOfResourcesException {
        updateEnabledState(surface);
        nUpdateSurface(this.mNativeProxy, surface);
    }

    /* access modifiers changed from: package-private */
    @Override // android.view.HardwareRenderer
    public void pauseSurface(Surface surface) {
        nPauseSurface(this.mNativeProxy, surface);
    }

    /* access modifiers changed from: package-private */
    @Override // android.view.HardwareRenderer
    public void destroyHardwareResources(View view) {
        destroyResources(view);
        nDestroyHardwareResources(this.mNativeProxy);
    }

    private static void destroyResources(View view) {
        view.destroyHardwareResources();
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            int count = group.getChildCount();
            for (int i = 0; i < count; i++) {
                destroyResources(group.getChildAt(i));
            }
        }
    }

    /* access modifiers changed from: package-private */
    @Override // android.view.HardwareRenderer
    public void invalidate(Surface surface) {
        updateSurface(surface);
    }

    /* access modifiers changed from: package-private */
    @Override // android.view.HardwareRenderer
    public void detachSurfaceTexture(long hardwareLayer) {
        nDetachSurfaceTexture(this.mNativeProxy, hardwareLayer);
    }

    /* access modifiers changed from: package-private */
    @Override // android.view.HardwareRenderer
    public void setup(int width, int height, Rect surfaceInsets) {
        float lightX = ((float) width) / 2.0f;
        this.mWidth = width;
        this.mHeight = height;
        if (surfaceInsets == null || surfaceInsets.isEmpty()) {
            this.mHasInsets = false;
            this.mInsetLeft = 0;
            this.mInsetTop = 0;
            this.mSurfaceWidth = width;
            this.mSurfaceHeight = height;
        } else {
            this.mHasInsets = true;
            this.mInsetLeft = surfaceInsets.left;
            this.mInsetTop = surfaceInsets.top;
            this.mSurfaceWidth = this.mInsetLeft + width + surfaceInsets.right;
            this.mSurfaceHeight = this.mInsetTop + height + surfaceInsets.bottom;
            setOpaque(false);
        }
        this.mRootNode.setLeftTopRightBottom(-this.mInsetLeft, -this.mInsetTop, this.mSurfaceWidth, this.mSurfaceHeight);
        nSetup(this.mNativeProxy, this.mSurfaceWidth, this.mSurfaceHeight, lightX, this.mLightY, this.mLightZ, this.mLightRadius, this.mAmbientShadowAlpha, this.mSpotShadowAlpha);
    }

    /* access modifiers changed from: package-private */
    @Override // android.view.HardwareRenderer
    public void setOpaque(boolean opaque) {
        nSetOpaque(this.mNativeProxy, opaque && !this.mHasInsets);
    }

    /* access modifiers changed from: package-private */
    @Override // android.view.HardwareRenderer
    public int getWidth() {
        return this.mWidth;
    }

    /* access modifiers changed from: package-private */
    @Override // android.view.HardwareRenderer
    public int getHeight() {
        return this.mHeight;
    }

    /* access modifiers changed from: package-private */
    @Override // android.view.HardwareRenderer
    public void dumpGfxInfo(PrintWriter pw, FileDescriptor fd) {
        pw.flush();
        nDumpProfileInfo(this.mNativeProxy, fd);
    }

    private static int search(String[] values, String value) {
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(value)) {
                return i;
            }
        }
        return -1;
    }

    private static boolean checkIfProfilingRequested() {
        String profiling = SystemProperties.get(HardwareRenderer.PROFILE_PROPERTY);
        return search(VISUALIZERS, profiling) >= 0 || Boolean.parseBoolean(profiling);
    }

    /* access modifiers changed from: package-private */
    @Override // android.view.HardwareRenderer
    public boolean loadSystemProperties() {
        boolean changed = nLoadSystemProperties(this.mNativeProxy);
        boolean wantProfiling = checkIfProfilingRequested();
        if (wantProfiling == this.mProfilingEnabled) {
            return changed;
        }
        this.mProfilingEnabled = wantProfiling;
        return true;
    }

    private void updateViewTreeDisplayList(View view) {
        view.mPrivateFlags |= 32;
        view.mRecreateDisplayList = (view.mPrivateFlags & Integer.MIN_VALUE) == Integer.MIN_VALUE;
        view.mPrivateFlags &= Integer.MAX_VALUE;
        view.getDisplayList();
        view.mRecreateDisplayList = false;
    }

    private void updateRootDisplayList(View view, HardwareRenderer.HardwareDrawCallbacks callbacks) {
        Trace.traceBegin(8, "getDisplayList");
        updateViewTreeDisplayList(view);
        if (this.mRootNodeNeedsUpdate || !this.mRootNode.isValid()) {
            HardwareCanvas canvas = this.mRootNode.start(this.mSurfaceWidth, this.mSurfaceHeight);
            try {
                int saveCount = canvas.save();
                canvas.translate((float) this.mInsetLeft, (float) this.mInsetTop);
                callbacks.onHardwarePreDraw(canvas);
                canvas.insertReorderBarrier();
                canvas.drawRenderNode(view.getDisplayList());
                canvas.insertInorderBarrier();
                callbacks.onHardwarePostDraw(canvas);
                canvas.restoreToCount(saveCount);
                this.mRootNodeNeedsUpdate = false;
            } finally {
                this.mRootNode.end(canvas);
            }
        }
        Trace.traceEnd(8);
    }

    /* access modifiers changed from: package-private */
    @Override // android.view.HardwareRenderer
    public void invalidateRoot() {
        this.mRootNodeNeedsUpdate = true;
    }

    /* access modifiers changed from: package-private */
    @Override // android.view.HardwareRenderer
    public void draw(View view, View.AttachInfo attachInfo, HardwareRenderer.HardwareDrawCallbacks callbacks) {
        attachInfo.mIgnoreDirtyState = true;
        long frameTimeNanos = this.mChoreographer.getFrameTimeNanos();
        attachInfo.mDrawingTime = frameTimeNanos / TimeUtils.NANOS_PER_MS;
        long recordDuration = 0;
        if (this.mProfilingEnabled) {
            recordDuration = System.nanoTime();
        }
        updateRootDisplayList(view, callbacks);
        if (this.mProfilingEnabled) {
            recordDuration = System.nanoTime() - recordDuration;
        }
        attachInfo.mIgnoreDirtyState = false;
        if (attachInfo.mPendingAnimatingRenderNodes != null) {
            int count = attachInfo.mPendingAnimatingRenderNodes.size();
            for (int i = 0; i < count; i++) {
                registerAnimatingRenderNode(attachInfo.mPendingAnimatingRenderNodes.get(i));
            }
            attachInfo.mPendingAnimatingRenderNodes.clear();
            attachInfo.mPendingAnimatingRenderNodes = null;
        }
        if ((nSyncAndDrawFrame(this.mNativeProxy, frameTimeNanos, recordDuration, view.getResources().getDisplayMetrics().density) & 1) != 0) {
            attachInfo.mViewRootImpl.invalidate();
        }
    }

    static void invokeFunctor(long functor, boolean waitForCompletion) {
        nInvokeFunctor(functor, waitForCompletion);
    }

    /* access modifiers changed from: package-private */
    @Override // android.view.HardwareRenderer
    public HardwareLayer createTextureLayer() {
        return HardwareLayer.adoptTextureLayer(this, nCreateTextureLayer(this.mNativeProxy));
    }

    /* access modifiers changed from: package-private */
    @Override // android.view.HardwareRenderer
    public void buildLayer(RenderNode node) {
        nBuildLayer(this.mNativeProxy, node.getNativeDisplayList());
    }

    /* access modifiers changed from: package-private */
    @Override // android.view.HardwareRenderer
    public boolean copyLayerInto(HardwareLayer layer, Bitmap bitmap) {
        return nCopyLayerInto(this.mNativeProxy, layer.getDeferredLayerUpdater(), bitmap.mNativeBitmap);
    }

    /* access modifiers changed from: package-private */
    @Override // android.view.HardwareRenderer
    public void pushLayerUpdate(HardwareLayer layer) {
        nPushLayerUpdate(this.mNativeProxy, layer.getDeferredLayerUpdater());
    }

    /* access modifiers changed from: package-private */
    @Override // android.view.HardwareRenderer
    public void onLayerDestroyed(HardwareLayer layer) {
        nCancelLayerUpdate(this.mNativeProxy, layer.getDeferredLayerUpdater());
    }

    /* access modifiers changed from: package-private */
    @Override // android.view.HardwareRenderer
    public void setName(String name) {
    }

    /* access modifiers changed from: package-private */
    @Override // android.view.HardwareRenderer
    public void fence() {
        nFence(this.mNativeProxy);
    }

    /* access modifiers changed from: package-private */
    @Override // android.view.HardwareRenderer
    public void stopDrawing() {
        nStopDrawing(this.mNativeProxy);
    }

    @Override // android.view.HardwareRenderer
    public void notifyFramePending() {
        nNotifyFramePending(this.mNativeProxy);
    }

    /* access modifiers changed from: package-private */
    @Override // android.view.HardwareRenderer
    public void registerAnimatingRenderNode(RenderNode animator) {
        nRegisterAnimatingRenderNode(this.mRootNode.mNativeRenderNode, animator.mNativeRenderNode);
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            nDeleteProxy(this.mNativeProxy);
            this.mNativeProxy = 0;
        } finally {
            super.finalize();
        }
    }

    static void trimMemory(int level) {
        nTrimMemory(level);
    }

    private static class AtlasInitializer {
        static AtlasInitializer sInstance = new AtlasInitializer();
        private boolean mInitialized = false;

        private AtlasInitializer() {
        }

        /* access modifiers changed from: package-private */
        public synchronized void init(Context context, long renderProxy) {
            IBinder binder;
            GraphicBuffer buffer;
            if (!this.mInitialized && (binder = ServiceManager.getService("assetatlas")) != null) {
                IAssetAtlas atlas = IAssetAtlas.Stub.asInterface(binder);
                try {
                    if (atlas.isCompatible(Process.myPpid()) && (buffer = atlas.getBuffer()) != null) {
                        long[] map = atlas.getMap();
                        if (map != null) {
                            validateMap(context, map);
                            ThreadedRenderer.nSetAtlas(renderProxy, buffer, map);
                            this.mInitialized = true;
                        }
                        if (atlas.getClass() != binder.getClass()) {
                            buffer.destroy();
                        }
                    }
                } catch (RemoteException e) {
                    Log.w("HardwareRenderer", "Could not acquire atlas", e);
                }
            }
        }

        private static void validateMap(Context context, long[] map) {
            Log.d("Atlas", "Validating map...");
            HashSet<Long> preloadedPointers = new HashSet<>();
            LongSparseArray<Drawable.ConstantState> drawables = context.getResources().getPreloadedDrawables();
            int count = drawables.size();
            for (int i = 0; i < count; i++) {
                Bitmap bitmap = drawables.valueAt(i).getBitmap();
                if (bitmap != null && bitmap.getConfig() == Bitmap.Config.ARGB_8888) {
                    preloadedPointers.add(Long.valueOf(bitmap.mNativeBitmap));
                }
            }
            for (int i2 = 0; i2 < map.length; i2 += 4) {
                if (!preloadedPointers.contains(Long.valueOf(map[i2]))) {
                    Log.w("Atlas", String.format("Pointer 0x%X, not in getPreloadedDrawables?", Long.valueOf(map[i2])));
                    map[i2] = 0;
                }
            }
        }
    }
}

package android.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.view.Surface;
import android.view.View;
import java.io.File;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public abstract class HardwareRenderer {
    private static final String CACHE_PATH_SHADERS = "com.android.opengl.shaders_cache";
    public static final String DEBUG_DIRTY_REGIONS_PROPERTY = "debug.hwui.show_dirty_regions";
    public static final String DEBUG_OVERDRAW_PROPERTY = "debug.hwui.overdraw";
    public static final String DEBUG_SHOW_LAYERS_UPDATES_PROPERTY = "debug.hwui.show_layers_updates";
    public static final String DEBUG_SHOW_NON_RECTANGULAR_CLIP_PROPERTY = "debug.hwui.show_non_rect_clip";
    static final String LOG_TAG = "HardwareRenderer";
    public static final String OVERDRAW_PROPERTY_SHOW = "show";
    static final String PRINT_CONFIG_PROPERTY = "debug.hwui.print_config";
    static final String PROFILE_MAXFRAMES_PROPERTY = "debug.hwui.profile.maxframes";
    public static final String PROFILE_PROPERTY = "debug.hwui.profile";
    public static final String PROFILE_PROPERTY_VISUALIZE_BARS = "visual_bars";
    static final String RENDER_DIRTY_REGIONS_PROPERTY = "debug.hwui.render_dirty_regions";
    public static boolean sRendererDisabled = false;
    public static boolean sSystemRendererDisabled = false;
    public static boolean sTrimForeground = false;
    private boolean mEnabled;
    private boolean mRequested = true;

    /* access modifiers changed from: package-private */
    public interface HardwareDrawCallbacks {
        void onHardwarePostDraw(HardwareCanvas hardwareCanvas);

        void onHardwarePreDraw(HardwareCanvas hardwareCanvas);
    }

    /* access modifiers changed from: package-private */
    public abstract void buildLayer(RenderNode renderNode);

    /* access modifiers changed from: package-private */
    public abstract boolean copyLayerInto(HardwareLayer hardwareLayer, Bitmap bitmap);

    /* access modifiers changed from: package-private */
    public abstract HardwareLayer createTextureLayer();

    /* access modifiers changed from: package-private */
    public abstract void destroy();

    /* access modifiers changed from: package-private */
    public abstract void destroyHardwareResources(View view);

    /* access modifiers changed from: package-private */
    public abstract void detachSurfaceTexture(long j);

    /* access modifiers changed from: package-private */
    public abstract void draw(View view, View.AttachInfo attachInfo, HardwareDrawCallbacks hardwareDrawCallbacks);

    /* access modifiers changed from: package-private */
    public abstract void dumpGfxInfo(PrintWriter printWriter, FileDescriptor fileDescriptor);

    /* access modifiers changed from: package-private */
    public abstract void fence();

    /* access modifiers changed from: package-private */
    public abstract int getHeight();

    /* access modifiers changed from: package-private */
    public abstract int getWidth();

    /* access modifiers changed from: package-private */
    public abstract boolean initialize(Surface surface) throws Surface.OutOfResourcesException;

    /* access modifiers changed from: package-private */
    public abstract void invalidate(Surface surface);

    /* access modifiers changed from: package-private */
    public abstract void invalidateRoot();

    /* access modifiers changed from: package-private */
    public abstract boolean loadSystemProperties();

    /* access modifiers changed from: package-private */
    public abstract void notifyFramePending();

    /* access modifiers changed from: package-private */
    public abstract void onLayerDestroyed(HardwareLayer hardwareLayer);

    /* access modifiers changed from: package-private */
    public abstract void pauseSurface(Surface surface);

    /* access modifiers changed from: package-private */
    public abstract void pushLayerUpdate(HardwareLayer hardwareLayer);

    /* access modifiers changed from: package-private */
    public abstract void registerAnimatingRenderNode(RenderNode renderNode);

    /* access modifiers changed from: package-private */
    public abstract void setName(String str);

    /* access modifiers changed from: package-private */
    public abstract void setOpaque(boolean z);

    /* access modifiers changed from: package-private */
    public abstract void setup(int i, int i2, Rect rect);

    /* access modifiers changed from: package-private */
    public abstract void stopDrawing();

    /* access modifiers changed from: package-private */
    public abstract void updateSurface(Surface surface) throws Surface.OutOfResourcesException;

    public static void disable(boolean system) {
        sRendererDisabled = true;
        if (system) {
            sSystemRendererDisabled = true;
        }
    }

    public static void enableForegroundTrimming() {
        sTrimForeground = true;
    }

    public static boolean isAvailable() {
        return GLES20Canvas.isAvailable();
    }

    public static void setupDiskCache(File cacheDir) {
        ThreadedRenderer.setupShadersDiskCache(new File(cacheDir, CACHE_PATH_SHADERS).getAbsolutePath());
    }

    /* access modifiers changed from: package-private */
    public boolean initializeIfNeeded(int width, int height, Surface surface, Rect surfaceInsets) throws Surface.OutOfResourcesException {
        if (!isRequested() || isEnabled() || !initialize(surface)) {
            return false;
        }
        setup(width, height, surfaceInsets);
        return true;
    }

    static HardwareRenderer create(Context context, boolean translucent) {
        if (GLES20Canvas.isAvailable()) {
            return new ThreadedRenderer(context, translucent);
        }
        return null;
    }

    static void trimMemory(int level) {
        ThreadedRenderer.trimMemory(level);
    }

    /* access modifiers changed from: package-private */
    public boolean isEnabled() {
        return this.mEnabled;
    }

    /* access modifiers changed from: package-private */
    public void setEnabled(boolean enabled) {
        this.mEnabled = enabled;
    }

    /* access modifiers changed from: package-private */
    public boolean isRequested() {
        return this.mRequested;
    }

    /* access modifiers changed from: package-private */
    public void setRequested(boolean requested) {
        this.mRequested = requested;
    }
}

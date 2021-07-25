package android.webkit;

import android.content.Context;
import android.net.ProxyInfo;

public abstract class WebSettings {
    public static final int LOAD_CACHE_ELSE_NETWORK = 1;
    public static final int LOAD_CACHE_ONLY = 3;
    public static final int LOAD_DEFAULT = -1;
    @Deprecated
    public static final int LOAD_NORMAL = 0;
    public static final int LOAD_NO_CACHE = 2;
    public static final int MIXED_CONTENT_ALWAYS_ALLOW = 0;
    public static final int MIXED_CONTENT_COMPATIBILITY_MODE = 2;
    public static final int MIXED_CONTENT_NEVER_ALLOW = 1;

    public enum LayoutAlgorithm {
        NORMAL,
        SINGLE_COLUMN,
        NARROW_COLUMNS,
        TEXT_AUTOSIZING
    }

    public enum PluginState {
        ON,
        ON_DEMAND,
        OFF
    }

    public enum RenderPriority {
        NORMAL,
        HIGH,
        LOW
    }

    public abstract boolean getAllowFileAccessFromFileURLs();

    public abstract boolean getAllowUniversalAccessFromFileURLs();

    public abstract int getMixedContentMode();

    public abstract boolean getVideoOverlayForEmbeddedEncryptedVideoEnabled();

    public abstract void setAllowFileAccessFromFileURLs(boolean z);

    public abstract void setAllowUniversalAccessFromFileURLs(boolean z);

    public abstract void setMixedContentMode(int i);

    public abstract void setVideoOverlayForEmbeddedEncryptedVideoEnabled(boolean z);

    public enum TextSize {
        SMALLEST(50),
        SMALLER(75),
        NORMAL(100),
        LARGER(150),
        LARGEST(200);
        
        int value;

        private TextSize(int size) {
            this.value = size;
        }
    }

    public enum ZoomDensity {
        FAR(150),
        MEDIUM(100),
        CLOSE(75);
        
        int value;

        private ZoomDensity(int size) {
            this.value = size;
        }

        public int getValue() {
            return this.value;
        }
    }

    protected WebSettings() {
    }

    @Deprecated
    public void setNavDump(boolean enabled) {
        throw new MustOverrideException();
    }

    @Deprecated
    public boolean getNavDump() {
        throw new MustOverrideException();
    }

    public void setSupportZoom(boolean support) {
        throw new MustOverrideException();
    }

    public boolean supportZoom() {
        throw new MustOverrideException();
    }

    public void setMediaPlaybackRequiresUserGesture(boolean require) {
        throw new MustOverrideException();
    }

    public boolean getMediaPlaybackRequiresUserGesture() {
        throw new MustOverrideException();
    }

    public void setBuiltInZoomControls(boolean enabled) {
        throw new MustOverrideException();
    }

    public boolean getBuiltInZoomControls() {
        throw new MustOverrideException();
    }

    public void setDisplayZoomControls(boolean enabled) {
        throw new MustOverrideException();
    }

    public boolean getDisplayZoomControls() {
        throw new MustOverrideException();
    }

    public void setAllowFileAccess(boolean allow) {
        throw new MustOverrideException();
    }

    public boolean getAllowFileAccess() {
        throw new MustOverrideException();
    }

    public void setAllowContentAccess(boolean allow) {
        throw new MustOverrideException();
    }

    public boolean getAllowContentAccess() {
        throw new MustOverrideException();
    }

    public void setLoadWithOverviewMode(boolean overview) {
        throw new MustOverrideException();
    }

    public boolean getLoadWithOverviewMode() {
        throw new MustOverrideException();
    }

    @Deprecated
    public void setEnableSmoothTransition(boolean enable) {
        throw new MustOverrideException();
    }

    @Deprecated
    public boolean enableSmoothTransition() {
        throw new MustOverrideException();
    }

    @Deprecated
    public void setUseWebViewBackgroundForOverscrollBackground(boolean view) {
        throw new MustOverrideException();
    }

    @Deprecated
    public boolean getUseWebViewBackgroundForOverscrollBackground() {
        throw new MustOverrideException();
    }

    public void setSaveFormData(boolean save) {
        throw new MustOverrideException();
    }

    public boolean getSaveFormData() {
        throw new MustOverrideException();
    }

    @Deprecated
    public void setSavePassword(boolean save) {
        throw new MustOverrideException();
    }

    @Deprecated
    public boolean getSavePassword() {
        throw new MustOverrideException();
    }

    public synchronized void setTextZoom(int textZoom) {
        throw new MustOverrideException();
    }

    public synchronized int getTextZoom() {
        throw new MustOverrideException();
    }

    public void setAcceptThirdPartyCookies(boolean accept) {
        throw new MustOverrideException();
    }

    public boolean getAcceptThirdPartyCookies() {
        throw new MustOverrideException();
    }

    public synchronized void setTextSize(TextSize t) {
        setTextZoom(t.value);
    }

    public synchronized TextSize getTextSize() {
        TextSize size;
        TextSize closestSize = null;
        int smallestDelta = Integer.MAX_VALUE;
        int textSize = getTextZoom();
        TextSize[] arr$ = TextSize.values();
        int len$ = arr$.length;
        int i$ = 0;
        while (true) {
            if (i$ < len$) {
                size = arr$[i$];
                int delta = Math.abs(textSize - size.value);
                if (delta == 0) {
                    break;
                }
                if (delta < smallestDelta) {
                    smallestDelta = delta;
                    closestSize = size;
                }
                i$++;
            } else {
                if (closestSize == null) {
                    closestSize = TextSize.NORMAL;
                }
                size = closestSize;
            }
        }
        return size;
    }

    @Deprecated
    public void setDefaultZoom(ZoomDensity zoom) {
        throw new MustOverrideException();
    }

    public ZoomDensity getDefaultZoom() {
        throw new MustOverrideException();
    }

    @Deprecated
    public void setLightTouchEnabled(boolean enabled) {
        throw new MustOverrideException();
    }

    @Deprecated
    public boolean getLightTouchEnabled() {
        throw new MustOverrideException();
    }

    @Deprecated
    public synchronized void setUseDoubleTree(boolean use) {
    }

    @Deprecated
    public synchronized boolean getUseDoubleTree() {
        return false;
    }

    @Deprecated
    public synchronized void setUserAgent(int ua) {
        throw new MustOverrideException();
    }

    @Deprecated
    public synchronized int getUserAgent() {
        throw new MustOverrideException();
    }

    public synchronized void setUseWideViewPort(boolean use) {
        throw new MustOverrideException();
    }

    public synchronized boolean getUseWideViewPort() {
        throw new MustOverrideException();
    }

    public synchronized void setSupportMultipleWindows(boolean support) {
        throw new MustOverrideException();
    }

    public synchronized boolean supportMultipleWindows() {
        throw new MustOverrideException();
    }

    public synchronized void setLayoutAlgorithm(LayoutAlgorithm l) {
        throw new MustOverrideException();
    }

    public synchronized LayoutAlgorithm getLayoutAlgorithm() {
        throw new MustOverrideException();
    }

    public synchronized void setStandardFontFamily(String font) {
        throw new MustOverrideException();
    }

    public synchronized String getStandardFontFamily() {
        throw new MustOverrideException();
    }

    public synchronized void setFixedFontFamily(String font) {
        throw new MustOverrideException();
    }

    public synchronized String getFixedFontFamily() {
        throw new MustOverrideException();
    }

    public synchronized void setSansSerifFontFamily(String font) {
        throw new MustOverrideException();
    }

    public synchronized String getSansSerifFontFamily() {
        throw new MustOverrideException();
    }

    public synchronized void setSerifFontFamily(String font) {
        throw new MustOverrideException();
    }

    public synchronized String getSerifFontFamily() {
        throw new MustOverrideException();
    }

    public synchronized void setCursiveFontFamily(String font) {
        throw new MustOverrideException();
    }

    public synchronized String getCursiveFontFamily() {
        throw new MustOverrideException();
    }

    public synchronized void setFantasyFontFamily(String font) {
        throw new MustOverrideException();
    }

    public synchronized String getFantasyFontFamily() {
        throw new MustOverrideException();
    }

    public synchronized void setMinimumFontSize(int size) {
        throw new MustOverrideException();
    }

    public synchronized int getMinimumFontSize() {
        throw new MustOverrideException();
    }

    public synchronized void setMinimumLogicalFontSize(int size) {
        throw new MustOverrideException();
    }

    public synchronized int getMinimumLogicalFontSize() {
        throw new MustOverrideException();
    }

    public synchronized void setDefaultFontSize(int size) {
        throw new MustOverrideException();
    }

    public synchronized int getDefaultFontSize() {
        throw new MustOverrideException();
    }

    public synchronized void setDefaultFixedFontSize(int size) {
        throw new MustOverrideException();
    }

    public synchronized int getDefaultFixedFontSize() {
        throw new MustOverrideException();
    }

    public synchronized void setLoadsImagesAutomatically(boolean flag) {
        throw new MustOverrideException();
    }

    public synchronized boolean getLoadsImagesAutomatically() {
        throw new MustOverrideException();
    }

    public synchronized void setBlockNetworkImage(boolean flag) {
        throw new MustOverrideException();
    }

    public synchronized boolean getBlockNetworkImage() {
        throw new MustOverrideException();
    }

    public synchronized void setBlockNetworkLoads(boolean flag) {
        throw new MustOverrideException();
    }

    public synchronized boolean getBlockNetworkLoads() {
        throw new MustOverrideException();
    }

    public synchronized void setJavaScriptEnabled(boolean flag) {
        throw new MustOverrideException();
    }

    @Deprecated
    public synchronized void setPluginsEnabled(boolean flag) {
        throw new MustOverrideException();
    }

    @Deprecated
    public synchronized void setPluginState(PluginState state) {
        throw new MustOverrideException();
    }

    @Deprecated
    public synchronized void setPluginsPath(String pluginsPath) {
    }

    @Deprecated
    public synchronized void setDatabasePath(String databasePath) {
        throw new MustOverrideException();
    }

    public synchronized void setGeolocationDatabasePath(String databasePath) {
        throw new MustOverrideException();
    }

    public synchronized void setAppCacheEnabled(boolean flag) {
        throw new MustOverrideException();
    }

    public synchronized void setAppCachePath(String appCachePath) {
        throw new MustOverrideException();
    }

    @Deprecated
    public synchronized void setAppCacheMaxSize(long appCacheMaxSize) {
        throw new MustOverrideException();
    }

    public synchronized void setDatabaseEnabled(boolean flag) {
        throw new MustOverrideException();
    }

    public synchronized void setDomStorageEnabled(boolean flag) {
        throw new MustOverrideException();
    }

    public synchronized boolean getDomStorageEnabled() {
        throw new MustOverrideException();
    }

    @Deprecated
    public synchronized String getDatabasePath() {
        throw new MustOverrideException();
    }

    public synchronized boolean getDatabaseEnabled() {
        throw new MustOverrideException();
    }

    public synchronized void setGeolocationEnabled(boolean flag) {
        throw new MustOverrideException();
    }

    public synchronized boolean getJavaScriptEnabled() {
        throw new MustOverrideException();
    }

    @Deprecated
    public synchronized boolean getPluginsEnabled() {
        throw new MustOverrideException();
    }

    @Deprecated
    public synchronized PluginState getPluginState() {
        throw new MustOverrideException();
    }

    @Deprecated
    public synchronized String getPluginsPath() {
        return ProxyInfo.LOCAL_EXCL_LIST;
    }

    public synchronized void setJavaScriptCanOpenWindowsAutomatically(boolean flag) {
        throw new MustOverrideException();
    }

    public synchronized boolean getJavaScriptCanOpenWindowsAutomatically() {
        throw new MustOverrideException();
    }

    public synchronized void setDefaultTextEncodingName(String encoding) {
        throw new MustOverrideException();
    }

    public synchronized String getDefaultTextEncodingName() {
        throw new MustOverrideException();
    }

    public synchronized void setUserAgentString(String ua) {
        throw new MustOverrideException();
    }

    public synchronized String getUserAgentString() {
        throw new MustOverrideException();
    }

    public static String getDefaultUserAgent(Context context) {
        return WebViewFactory.getProvider().getStatics().getDefaultUserAgent(context);
    }

    public void setNeedInitialFocus(boolean flag) {
        throw new MustOverrideException();
    }

    @Deprecated
    public synchronized void setRenderPriority(RenderPriority priority) {
        throw new MustOverrideException();
    }

    public void setCacheMode(int mode) {
        throw new MustOverrideException();
    }

    public int getCacheMode() {
        throw new MustOverrideException();
    }
}

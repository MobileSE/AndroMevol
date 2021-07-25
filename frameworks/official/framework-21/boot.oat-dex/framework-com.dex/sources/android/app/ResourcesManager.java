package android.app;

import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.ResourcesKey;
import android.hardware.display.DisplayManagerGlobal;
import android.util.ArrayMap;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.DisplayAdjustments;
import java.lang.ref.WeakReference;
import java.util.Locale;

public class ResourcesManager {
    static final boolean DEBUG_CACHE = false;
    static final boolean DEBUG_STATS = true;
    static final String TAG = "ResourcesManager";
    private static ResourcesManager sResourcesManager;
    final ArrayMap<ResourcesKey, WeakReference<Resources>> mActiveResources = new ArrayMap<>();
    final ArrayMap<DisplayAdjustments, DisplayMetrics> mDefaultDisplayMetrics = new ArrayMap<>();
    CompatibilityInfo mResCompatibilityInfo;
    Configuration mResConfiguration;
    final Configuration mTmpConfig = new Configuration();

    public static ResourcesManager getInstance() {
        ResourcesManager resourcesManager;
        synchronized (ResourcesManager.class) {
            if (sResourcesManager == null) {
                sResourcesManager = new ResourcesManager();
            }
            resourcesManager = sResourcesManager;
        }
        return resourcesManager;
    }

    public Configuration getConfiguration() {
        return this.mResConfiguration;
    }

    public void flushDisplayMetricsLocked() {
        this.mDefaultDisplayMetrics.clear();
    }

    public DisplayMetrics getDisplayMetricsLocked(int displayId) {
        return getDisplayMetricsLocked(displayId, DisplayAdjustments.DEFAULT_DISPLAY_ADJUSTMENTS);
    }

    public DisplayMetrics getDisplayMetricsLocked(int displayId, DisplayAdjustments daj) {
        boolean isDefaultDisplay = displayId == 0;
        DisplayMetrics dm = isDefaultDisplay ? this.mDefaultDisplayMetrics.get(daj) : null;
        if (dm != null) {
            return dm;
        }
        DisplayMetrics dm2 = new DisplayMetrics();
        DisplayManagerGlobal displayManager = DisplayManagerGlobal.getInstance();
        if (displayManager == null) {
            dm2.setToDefaults();
            return dm2;
        }
        if (isDefaultDisplay) {
            this.mDefaultDisplayMetrics.put(daj, dm2);
        }
        Display d = displayManager.getCompatibleDisplay(displayId, daj);
        if (d != null) {
            d.getMetrics(dm2);
        } else {
            dm2.setToDefaults();
        }
        return dm2;
    }

    /* access modifiers changed from: package-private */
    public final void applyNonDefaultDisplayMetricsToConfigurationLocked(DisplayMetrics dm, Configuration config) {
        config.touchscreen = 1;
        config.densityDpi = dm.densityDpi;
        config.screenWidthDp = (int) (((float) dm.widthPixels) / dm.density);
        config.screenHeightDp = (int) (((float) dm.heightPixels) / dm.density);
        int sl = Configuration.resetScreenLayout(config.screenLayout);
        if (dm.widthPixels > dm.heightPixels) {
            config.orientation = 2;
            config.screenLayout = Configuration.reduceScreenLayout(sl, config.screenWidthDp, config.screenHeightDp);
        } else {
            config.orientation = 1;
            config.screenLayout = Configuration.reduceScreenLayout(sl, config.screenHeightDp, config.screenWidthDp);
        }
        config.smallestScreenWidthDp = config.screenWidthDp;
        config.compatScreenWidthDp = config.screenWidthDp;
        config.compatScreenHeightDp = config.screenHeightDp;
        config.compatSmallestScreenWidthDp = config.smallestScreenWidthDp;
    }

    public boolean applyCompatConfiguration(int displayDensity, Configuration compatConfiguration) {
        if (this.mResCompatibilityInfo == null || this.mResCompatibilityInfo.supportsScreen()) {
            return false;
        }
        this.mResCompatibilityInfo.applyToConfiguration(displayDensity, compatConfiguration);
        return true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0037, code lost:
        r8 = new android.content.res.AssetManager();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x003c, code lost:
        if (r24 == null) goto L_0x004b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0044, code lost:
        if (r8.addAssetPath(r24) != 0) goto L_0x004b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0046, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x004b, code lost:
        if (r25 == null) goto L_0x0069;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x004d, code lost:
        r0 = r25.length;
        r16 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0058, code lost:
        if (r16 >= r0) goto L_0x0069;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0062, code lost:
        if (r8.addAssetPath(r25[r16]) != 0) goto L_0x0066;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0064, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0066, code lost:
        r16 = r16 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0069, code lost:
        if (r26 == null) goto L_0x0082;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x006b, code lost:
        r0 = r26.length;
        r16 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0076, code lost:
        if (r16 >= r0) goto L_0x0082;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0078, code lost:
        r8.addOverlayPath(r26[r16]);
        r16 = r16 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0082, code lost:
        if (r27 == null) goto L_0x00be;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0084, code lost:
        r0 = r27.length;
        r16 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x008f, code lost:
        if (r16 >= r0) goto L_0x00be;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0091, code lost:
        r20 = r27[r16];
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x0099, code lost:
        if (r8.addAssetPath(r20) != 0) goto L_0x00bb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x009b, code lost:
        android.util.Slog.w(android.app.ResourcesManager.TAG, "Asset path '" + r20 + "' does not exist or contains no resources.");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00bb, code lost:
        r16 = r16 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00be, code lost:
        r9 = getDisplayMetricsLocked(r28);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00c6, code lost:
        if (r28 != 0) goto L_0x011f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00c8, code lost:
        r18 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00ca, code lost:
        r15 = r2.hasOverrideConfiguration();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00ce, code lost:
        if (r18 == false) goto L_0x00d2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x00d0, code lost:
        if (r15 == false) goto L_0x0122;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x00d2, code lost:
        r10 = new android.content.res.Configuration(getConfiguration());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00db, code lost:
        if (r18 != false) goto L_0x00e2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x00dd, code lost:
        applyNonDefaultDisplayMetricsToConfigurationLocked(r9, r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x00e2, code lost:
        if (r15 == false) goto L_0x00e9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00e4, code lost:
        r10.updateFrom(r2.mOverrideConfiguration);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00e9, code lost:
        r7 = new android.content.res.Resources(r8, r9, r10, r30, r31);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00f2, code lost:
        monitor-enter(r23);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:?, code lost:
        r22 = r23.mActiveResources.get(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x00fd, code lost:
        if (r22 == null) goto L_0x0127;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x00ff, code lost:
        r14 = r22.get();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x0106, code lost:
        if (r14 == null) goto L_0x0129;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x0110, code lost:
        if (r14.getAssets().isUpToDate() == false) goto L_0x0129;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x0112, code lost:
        r7.getAssets().close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x0119, code lost:
        monitor-exit(r23);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x011f, code lost:
        r18 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x0122, code lost:
        r10 = getConfiguration();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x0127, code lost:
        r14 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:0x0129, code lost:
        r23.mActiveResources.put(r2, new java.lang.ref.WeakReference<>(r7));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:74:0x0135, code lost:
        monitor-exit(r23);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:83:?, code lost:
        return r14;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:84:?, code lost:
        return r7;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public android.content.res.Resources getTopLevelResources(java.lang.String r24, java.lang.String[] r25, java.lang.String[] r26, java.lang.String[] r27, int r28, android.content.res.Configuration r29, android.content.res.CompatibilityInfo r30, android.os.IBinder r31) {
        /*
        // Method dump skipped, instructions count: 313
        */
        throw new UnsupportedOperationException("Method not decompiled: android.app.ResourcesManager.getTopLevelResources(java.lang.String, java.lang.String[], java.lang.String[], java.lang.String[], int, android.content.res.Configuration, android.content.res.CompatibilityInfo, android.os.IBinder):android.content.res.Resources");
    }

    public final boolean applyConfigurationToResourcesLocked(Configuration config, CompatibilityInfo compat) {
        boolean z = true;
        if (this.mResConfiguration == null) {
            this.mResConfiguration = new Configuration();
        }
        if (!this.mResConfiguration.isOtherSeqNewer(config) && compat == null) {
            return false;
        }
        int changes = this.mResConfiguration.updateFrom(config);
        flushDisplayMetricsLocked();
        DisplayMetrics defaultDisplayMetrics = getDisplayMetricsLocked(0);
        if (compat != null && (this.mResCompatibilityInfo == null || !this.mResCompatibilityInfo.equals(compat))) {
            this.mResCompatibilityInfo = compat;
            changes |= 3328;
        }
        if (config.locale != null) {
            Locale.setDefault(config.locale);
        }
        Resources.updateSystemConfiguration(config, defaultDisplayMetrics, compat);
        ApplicationPackageManager.configurationChanged();
        Configuration tmpConfig = null;
        for (int i = this.mActiveResources.size() - 1; i >= 0; i--) {
            ResourcesKey key = this.mActiveResources.keyAt(i);
            Resources r = this.mActiveResources.valueAt(i).get();
            if (r != null) {
                int displayId = key.mDisplayId;
                boolean isDefaultDisplay = displayId == 0;
                DisplayMetrics dm = defaultDisplayMetrics;
                boolean hasOverrideConfiguration = key.hasOverrideConfiguration();
                if (!isDefaultDisplay || hasOverrideConfiguration) {
                    if (tmpConfig == null) {
                        tmpConfig = new Configuration();
                    }
                    tmpConfig.setTo(config);
                    if (!isDefaultDisplay) {
                        dm = getDisplayMetricsLocked(displayId);
                        applyNonDefaultDisplayMetricsToConfigurationLocked(dm, tmpConfig);
                    }
                    if (hasOverrideConfiguration) {
                        tmpConfig.updateFrom(key.mOverrideConfiguration);
                    }
                    r.updateConfiguration(tmpConfig, dm, compat);
                } else {
                    r.updateConfiguration(config, dm, compat);
                }
            } else {
                this.mActiveResources.removeAt(i);
            }
        }
        if (changes == 0) {
            z = false;
        }
        return z;
    }
}

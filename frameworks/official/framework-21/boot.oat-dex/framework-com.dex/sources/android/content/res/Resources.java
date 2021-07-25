package android.content.res;

import android.content.pm.ActivityInfo;
import android.content.res.XmlBlock;
import android.graphics.Movie;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.TtmlUtils;
import android.net.ProxyInfo;
import android.nfc.cardemulation.CardEmulation;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Trace;
import android.util.ArrayMap;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.Pools;
import android.util.TypedValue;
import android.view.ViewDebug;
import com.android.internal.R;
import com.android.internal.util.XmlUtils;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Locale;
import libcore.icu.NativePluralRules;
import org.xmlpull.v1.XmlPullParserException;

public class Resources {
    private static final boolean DEBUG_CONFIG = false;
    private static final boolean DEBUG_LOAD = false;
    private static final int ID_OTHER = 16777220;
    private static final int LAYOUT_DIR_CONFIG = ActivityInfo.activityInfoConfigToNative(8192);
    static final String TAG = "Resources";
    private static final boolean TRACE_FOR_MISS_PRELOAD = false;
    private static final boolean TRACE_FOR_PRELOAD = false;
    static Resources mSystem = null;
    private static boolean sPreloaded;
    private static final LongSparseArray<Drawable.ConstantState> sPreloadedColorDrawables = new LongSparseArray<>();
    private static final LongSparseArray<ColorStateList> sPreloadedColorStateLists = new LongSparseArray<>();
    private static int sPreloadedDensity;
    private static final LongSparseArray<Drawable.ConstantState>[] sPreloadedDrawables = new LongSparseArray[2];
    private static final Object sSync = new Object();
    private final Object mAccessLock;
    final AssetManager mAssets;
    private TypedArray mCachedStyledAttributes;
    private final int[] mCachedXmlBlockIds;
    private final XmlBlock[] mCachedXmlBlocks;
    private final ArrayMap<String, LongSparseArray<WeakReference<Drawable.ConstantState>>> mColorDrawableCache;
    private final LongSparseArray<WeakReference<ColorStateList>> mColorStateListCache;
    private CompatibilityInfo mCompatibilityInfo;
    private final Configuration mConfiguration;
    private final ArrayMap<String, LongSparseArray<WeakReference<Drawable.ConstantState>>> mDrawableCache;
    private int mLastCachedXmlBlockIndex;
    final DisplayMetrics mMetrics;
    private NativePluralRules mPluralRule;
    private boolean mPreloading;
    private final Configuration mTmpConfig;
    private TypedValue mTmpValue;
    private WeakReference<IBinder> mToken;
    final Pools.SynchronizedPool<TypedArray> mTypedArrayPool;

    static {
        sPreloadedDrawables[0] = new LongSparseArray<>();
        sPreloadedDrawables[1] = new LongSparseArray<>();
    }

    public static int selectDefaultTheme(int curTheme, int targetSdkVersion) {
        return selectSystemTheme(curTheme, targetSdkVersion, 16973829, 16973931, 16974120, 16974143);
    }

    public static int selectSystemTheme(int curTheme, int targetSdkVersion, int orig, int holo, int dark, int deviceDefault) {
        if (curTheme != 0) {
            return curTheme;
        }
        if (targetSdkVersion < 11) {
            return orig;
        }
        if (targetSdkVersion < 14) {
            return holo;
        }
        return targetSdkVersion < 10000 ? dark : deviceDefault;
    }

    public static class NotFoundException extends RuntimeException {
        public NotFoundException() {
        }

        public NotFoundException(String name) {
            super(name);
        }
    }

    public Resources(AssetManager assets, DisplayMetrics metrics, Configuration config) {
        this(assets, metrics, config, CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO, null);
    }

    public Resources(AssetManager assets, DisplayMetrics metrics, Configuration config, CompatibilityInfo compatInfo, IBinder token) {
        this.mTypedArrayPool = new Pools.SynchronizedPool<>(5);
        this.mAccessLock = new Object();
        this.mTmpConfig = new Configuration();
        this.mDrawableCache = new ArrayMap<>();
        this.mColorDrawableCache = new ArrayMap<>();
        this.mColorStateListCache = new LongSparseArray<>();
        this.mTmpValue = new TypedValue();
        this.mCachedStyledAttributes = null;
        this.mLastCachedXmlBlockIndex = -1;
        this.mCachedXmlBlockIds = new int[]{0, 0, 0, 0};
        this.mCachedXmlBlocks = new XmlBlock[4];
        this.mMetrics = new DisplayMetrics();
        this.mConfiguration = new Configuration();
        this.mCompatibilityInfo = CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO;
        this.mAssets = assets;
        this.mMetrics.setToDefaults();
        if (compatInfo != null) {
            this.mCompatibilityInfo = compatInfo;
        }
        this.mToken = new WeakReference<>(token);
        updateConfiguration(config, metrics);
        assets.ensureStringBlocks();
    }

    public static Resources getSystem() {
        Resources ret;
        synchronized (sSync) {
            ret = mSystem;
            if (ret == null) {
                ret = new Resources();
                mSystem = ret;
            }
        }
        return ret;
    }

    public CharSequence getText(int id) throws NotFoundException {
        CharSequence res = this.mAssets.getResourceText(id);
        if (res != null) {
            return res;
        }
        throw new NotFoundException("String resource ID #0x" + Integer.toHexString(id));
    }

    public CharSequence getQuantityText(int id, int quantity) throws NotFoundException {
        NativePluralRules rule = getPluralRule();
        CharSequence res = this.mAssets.getResourceBagText(id, attrForQuantityCode(rule.quantityForInt(quantity)));
        if (res != null) {
            return res;
        }
        CharSequence res2 = this.mAssets.getResourceBagText(id, ID_OTHER);
        if (res2 != null) {
            return res2;
        }
        throw new NotFoundException("Plural resource ID #0x" + Integer.toHexString(id) + " quantity=" + quantity + " item=" + stringForQuantityCode(rule.quantityForInt(quantity)));
    }

    private NativePluralRules getPluralRule() {
        NativePluralRules nativePluralRules;
        synchronized (sSync) {
            if (this.mPluralRule == null) {
                this.mPluralRule = NativePluralRules.forLocale(this.mConfiguration.locale);
            }
            nativePluralRules = this.mPluralRule;
        }
        return nativePluralRules;
    }

    private static int attrForQuantityCode(int quantityCode) {
        switch (quantityCode) {
            case 0:
                return 16777221;
            case 1:
                return 16777222;
            case 2:
                return 16777223;
            case 3:
                return 16777224;
            case 4:
                return 16777225;
            default:
                return ID_OTHER;
        }
    }

    private static String stringForQuantityCode(int quantityCode) {
        switch (quantityCode) {
            case 0:
                return "zero";
            case 1:
                return "one";
            case 2:
                return "two";
            case 3:
                return "few";
            case 4:
                return "many";
            default:
                return CardEmulation.CATEGORY_OTHER;
        }
    }

    public String getString(int id) throws NotFoundException {
        CharSequence res = getText(id);
        if (res != null) {
            return res.toString();
        }
        throw new NotFoundException("String resource ID #0x" + Integer.toHexString(id));
    }

    public String getString(int id, Object... formatArgs) throws NotFoundException {
        return String.format(this.mConfiguration.locale, getString(id), formatArgs);
    }

    public String getQuantityString(int id, int quantity, Object... formatArgs) throws NotFoundException {
        return String.format(this.mConfiguration.locale, getQuantityText(id, quantity).toString(), formatArgs);
    }

    public String getQuantityString(int id, int quantity) throws NotFoundException {
        return getQuantityText(id, quantity).toString();
    }

    public CharSequence getText(int id, CharSequence def) {
        CharSequence res = id != 0 ? this.mAssets.getResourceText(id) : null;
        return res != null ? res : def;
    }

    public CharSequence[] getTextArray(int id) throws NotFoundException {
        CharSequence[] res = this.mAssets.getResourceTextArray(id);
        if (res != null) {
            return res;
        }
        throw new NotFoundException("Text array resource ID #0x" + Integer.toHexString(id));
    }

    public String[] getStringArray(int id) throws NotFoundException {
        String[] res = this.mAssets.getResourceStringArray(id);
        if (res != null) {
            return res;
        }
        throw new NotFoundException("String array resource ID #0x" + Integer.toHexString(id));
    }

    public int[] getIntArray(int id) throws NotFoundException {
        int[] res = this.mAssets.getArrayIntResource(id);
        if (res != null) {
            return res;
        }
        throw new NotFoundException("Int array resource ID #0x" + Integer.toHexString(id));
    }

    public TypedArray obtainTypedArray(int id) throws NotFoundException {
        int len = this.mAssets.getArraySize(id);
        if (len < 0) {
            throw new NotFoundException("Array resource ID #0x" + Integer.toHexString(id));
        }
        TypedArray array = TypedArray.obtain(this, len);
        array.mLength = this.mAssets.retrieveArray(id, array.mData);
        array.mIndices[0] = 0;
        return array;
    }

    public float getDimension(int id) throws NotFoundException {
        float complexToDimension;
        synchronized (this.mAccessLock) {
            TypedValue value = this.mTmpValue;
            if (value == null) {
                value = new TypedValue();
                this.mTmpValue = value;
            }
            getValue(id, value, true);
            if (value.type == 5) {
                complexToDimension = TypedValue.complexToDimension(value.data, this.mMetrics);
            } else {
                throw new NotFoundException("Resource ID #0x" + Integer.toHexString(id) + " type #0x" + Integer.toHexString(value.type) + " is not valid");
            }
        }
        return complexToDimension;
    }

    public int getDimensionPixelOffset(int id) throws NotFoundException {
        int complexToDimensionPixelOffset;
        synchronized (this.mAccessLock) {
            TypedValue value = this.mTmpValue;
            if (value == null) {
                value = new TypedValue();
                this.mTmpValue = value;
            }
            getValue(id, value, true);
            if (value.type == 5) {
                complexToDimensionPixelOffset = TypedValue.complexToDimensionPixelOffset(value.data, this.mMetrics);
            } else {
                throw new NotFoundException("Resource ID #0x" + Integer.toHexString(id) + " type #0x" + Integer.toHexString(value.type) + " is not valid");
            }
        }
        return complexToDimensionPixelOffset;
    }

    public int getDimensionPixelSize(int id) throws NotFoundException {
        int complexToDimensionPixelSize;
        synchronized (this.mAccessLock) {
            TypedValue value = this.mTmpValue;
            if (value == null) {
                value = new TypedValue();
                this.mTmpValue = value;
            }
            getValue(id, value, true);
            if (value.type == 5) {
                complexToDimensionPixelSize = TypedValue.complexToDimensionPixelSize(value.data, this.mMetrics);
            } else {
                throw new NotFoundException("Resource ID #0x" + Integer.toHexString(id) + " type #0x" + Integer.toHexString(value.type) + " is not valid");
            }
        }
        return complexToDimensionPixelSize;
    }

    public float getFraction(int id, int base, int pbase) {
        float complexToFraction;
        synchronized (this.mAccessLock) {
            TypedValue value = this.mTmpValue;
            if (value == null) {
                value = new TypedValue();
                this.mTmpValue = value;
            }
            getValue(id, value, true);
            if (value.type == 6) {
                complexToFraction = TypedValue.complexToFraction(value.data, (float) base, (float) pbase);
            } else {
                throw new NotFoundException("Resource ID #0x" + Integer.toHexString(id) + " type #0x" + Integer.toHexString(value.type) + " is not valid");
            }
        }
        return complexToFraction;
    }

    public Drawable getDrawable(int id) throws NotFoundException {
        Drawable d = getDrawable(id, null);
        if (d.canApplyTheme()) {
            Log.w(TAG, "Drawable " + getResourceName(id) + " has unresolved theme " + "attributes! Consider using Resources.getDrawable(int, Theme) or " + "Context.getDrawable(int).", new RuntimeException());
        }
        return d;
    }

    public Drawable getDrawable(int id, Theme theme) throws NotFoundException {
        TypedValue value;
        synchronized (this.mAccessLock) {
            value = this.mTmpValue;
            if (value == null) {
                value = new TypedValue();
            } else {
                this.mTmpValue = null;
            }
            getValue(id, value, true);
        }
        Drawable res = loadDrawable(value, id, theme);
        synchronized (this.mAccessLock) {
            if (this.mTmpValue == null) {
                this.mTmpValue = value;
            }
        }
        return res;
    }

    public Drawable getDrawableForDensity(int id, int density) throws NotFoundException {
        return getDrawableForDensity(id, density, null);
    }

    public Drawable getDrawableForDensity(int id, int density, Theme theme) {
        TypedValue value;
        synchronized (this.mAccessLock) {
            value = this.mTmpValue;
            if (value == null) {
                value = new TypedValue();
            } else {
                this.mTmpValue = null;
            }
            getValueForDensity(id, density, value, true);
            if (value.density > 0 && value.density != 65535) {
                if (value.density == density) {
                    value.density = this.mMetrics.densityDpi;
                } else {
                    value.density = (value.density * this.mMetrics.densityDpi) / density;
                }
            }
        }
        Drawable res = loadDrawable(value, id, theme);
        synchronized (this.mAccessLock) {
            if (this.mTmpValue == null) {
                this.mTmpValue = value;
            }
        }
        return res;
    }

    public Movie getMovie(int id) throws NotFoundException {
        InputStream is = openRawResource(id);
        Movie movie = Movie.decodeStream(is);
        try {
            is.close();
        } catch (IOException e) {
        }
        return movie;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0061, code lost:
        r0 = loadColorStateList(r1, r7);
        r3 = r6.mAccessLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0067, code lost:
        monitor-enter(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x006a, code lost:
        if (r6.mTmpValue != null) goto L_0x006e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x006c, code lost:
        r6.mTmpValue = r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x006e, code lost:
        monitor-exit(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:?, code lost:
        return r0.getDefaultColor();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getColor(int r7) throws android.content.res.Resources.NotFoundException {
        /*
        // Method dump skipped, instructions count: 119
        */
        throw new UnsupportedOperationException("Method not decompiled: android.content.res.Resources.getColor(int):int");
    }

    public ColorStateList getColorStateList(int id) throws NotFoundException {
        TypedValue value;
        synchronized (this.mAccessLock) {
            value = this.mTmpValue;
            if (value == null) {
                value = new TypedValue();
            } else {
                this.mTmpValue = null;
            }
            getValue(id, value, true);
        }
        ColorStateList res = loadColorStateList(value, id);
        synchronized (this.mAccessLock) {
            if (this.mTmpValue == null) {
                this.mTmpValue = value;
            }
        }
        return res;
    }

    public boolean getBoolean(int id) throws NotFoundException {
        boolean z = true;
        synchronized (this.mAccessLock) {
            TypedValue value = this.mTmpValue;
            if (value == null) {
                value = new TypedValue();
                this.mTmpValue = value;
            }
            getValue(id, value, true);
            if (value.type < 16 || value.type > 31) {
                throw new NotFoundException("Resource ID #0x" + Integer.toHexString(id) + " type #0x" + Integer.toHexString(value.type) + " is not valid");
            } else if (value.data == 0) {
                z = false;
            }
        }
        return z;
    }

    public int getInteger(int id) throws NotFoundException {
        int i;
        synchronized (this.mAccessLock) {
            TypedValue value = this.mTmpValue;
            if (value == null) {
                value = new TypedValue();
                this.mTmpValue = value;
            }
            getValue(id, value, true);
            if (value.type < 16 || value.type > 31) {
                throw new NotFoundException("Resource ID #0x" + Integer.toHexString(id) + " type #0x" + Integer.toHexString(value.type) + " is not valid");
            }
            i = value.data;
        }
        return i;
    }

    public float getFloat(int id) {
        float f;
        synchronized (this.mAccessLock) {
            TypedValue value = this.mTmpValue;
            if (value == null) {
                value = new TypedValue();
                this.mTmpValue = value;
            }
            getValue(id, value, true);
            if (value.type == 4) {
                f = value.getFloat();
            } else {
                throw new NotFoundException("Resource ID #0x" + Integer.toHexString(id) + " type #0x" + Integer.toHexString(value.type) + " is not valid");
            }
        }
        return f;
    }

    public XmlResourceParser getLayout(int id) throws NotFoundException {
        return loadXmlResourceParser(id, TtmlUtils.TAG_LAYOUT);
    }

    public XmlResourceParser getAnimation(int id) throws NotFoundException {
        return loadXmlResourceParser(id, "anim");
    }

    public XmlResourceParser getXml(int id) throws NotFoundException {
        return loadXmlResourceParser(id, "xml");
    }

    public InputStream openRawResource(int id) throws NotFoundException {
        TypedValue value;
        synchronized (this.mAccessLock) {
            value = this.mTmpValue;
            if (value == null) {
                value = new TypedValue();
            } else {
                this.mTmpValue = null;
            }
        }
        InputStream res = openRawResource(id, value);
        synchronized (this.mAccessLock) {
            if (this.mTmpValue == null) {
                this.mTmpValue = value;
            }
        }
        return res;
    }

    public InputStream openRawResource(int id, TypedValue value) throws NotFoundException {
        getValue(id, value, true);
        try {
            return this.mAssets.openNonAsset(value.assetCookie, value.string.toString(), 2);
        } catch (Exception e) {
            NotFoundException rnf = new NotFoundException("File " + value.string.toString() + " from drawable resource ID #0x" + Integer.toHexString(id));
            rnf.initCause(e);
            throw rnf;
        }
    }

    public AssetFileDescriptor openRawResourceFd(int id) throws NotFoundException {
        TypedValue value;
        synchronized (this.mAccessLock) {
            value = this.mTmpValue;
            if (value == null) {
                value = new TypedValue();
            } else {
                this.mTmpValue = null;
            }
            getValue(id, value, true);
        }
        try {
            AssetFileDescriptor openNonAssetFd = this.mAssets.openNonAssetFd(value.assetCookie, value.string.toString());
            synchronized (this.mAccessLock) {
                if (this.mTmpValue == null) {
                    this.mTmpValue = value;
                }
            }
            return openNonAssetFd;
        } catch (Exception e) {
            NotFoundException rnf = new NotFoundException("File " + value.string.toString() + " from drawable resource ID #0x" + Integer.toHexString(id));
            rnf.initCause(e);
            throw rnf;
        } catch (Throwable th) {
            synchronized (this.mAccessLock) {
                if (this.mTmpValue == null) {
                    this.mTmpValue = value;
                }
                throw th;
            }
        }
    }

    public void getValue(int id, TypedValue outValue, boolean resolveRefs) throws NotFoundException {
        if (!this.mAssets.getResourceValue(id, 0, outValue, resolveRefs)) {
            throw new NotFoundException("Resource ID #0x" + Integer.toHexString(id));
        }
    }

    public void getValueForDensity(int id, int density, TypedValue outValue, boolean resolveRefs) throws NotFoundException {
        if (!this.mAssets.getResourceValue(id, density, outValue, resolveRefs)) {
            throw new NotFoundException("Resource ID #0x" + Integer.toHexString(id));
        }
    }

    public void getValue(String name, TypedValue outValue, boolean resolveRefs) throws NotFoundException {
        int id = getIdentifier(name, "string", null);
        if (id != 0) {
            getValue(id, outValue, resolveRefs);
            return;
        }
        throw new NotFoundException("String resource name " + name);
    }

    public final class Theme {
        private final AssetManager mAssets;
        private String mKey = ProxyInfo.LOCAL_EXCL_LIST;
        private final long mTheme;
        private int mThemeResId = 0;

        public void applyStyle(int resId, boolean force) {
            AssetManager.applyThemeStyle(this.mTheme, resId, force);
            this.mThemeResId = resId;
            this.mKey += Integer.toHexString(resId) + (force ? "! " : " ");
        }

        public void setTo(Theme other) {
            AssetManager.copyTheme(this.mTheme, other.mTheme);
            this.mThemeResId = other.mThemeResId;
            this.mKey = other.mKey;
        }

        public TypedArray obtainStyledAttributes(int[] attrs) {
            TypedArray array = TypedArray.obtain(Resources.this, attrs.length);
            array.mTheme = this;
            AssetManager.applyStyle(this.mTheme, 0, 0, 0, attrs, array.mData, array.mIndices);
            return array;
        }

        public TypedArray obtainStyledAttributes(int resid, int[] attrs) throws NotFoundException {
            TypedArray array = TypedArray.obtain(Resources.this, attrs.length);
            array.mTheme = this;
            AssetManager.applyStyle(this.mTheme, 0, resid, 0, attrs, array.mData, array.mIndices);
            return array;
        }

        public TypedArray obtainStyledAttributes(AttributeSet set, int[] attrs, int defStyleAttr, int defStyleRes) {
            TypedArray array = TypedArray.obtain(Resources.this, attrs.length);
            XmlBlock.Parser parser = (XmlBlock.Parser) set;
            AssetManager.applyStyle(this.mTheme, defStyleAttr, defStyleRes, parser != null ? parser.mParseState : 0, attrs, array.mData, array.mIndices);
            array.mTheme = this;
            array.mXml = parser;
            return array;
        }

        public TypedArray resolveAttributes(int[] values, int[] attrs) {
            int len = attrs.length;
            if (values == null || len == values.length) {
                TypedArray array = TypedArray.obtain(Resources.this, len);
                AssetManager.resolveAttrs(this.mTheme, 0, 0, values, attrs, array.mData, array.mIndices);
                array.mTheme = this;
                array.mXml = null;
                return array;
            }
            throw new IllegalArgumentException("Base attribute values must be null or the same length as attrs");
        }

        public boolean resolveAttribute(int resid, TypedValue outValue, boolean resolveRefs) {
            return this.mAssets.getThemeValue(this.mTheme, resid, outValue, resolveRefs);
        }

        public int[] getAllAttributes() {
            return this.mAssets.getStyleAttributes(getAppliedStyleResId());
        }

        public Resources getResources() {
            return Resources.this;
        }

        public Drawable getDrawable(int id) throws NotFoundException {
            return Resources.this.getDrawable(id, this);
        }

        public void dump(int priority, String tag, String prefix) {
            AssetManager.dumpTheme(this.mTheme, priority, tag, prefix);
        }

        /* access modifiers changed from: protected */
        public void finalize() throws Throwable {
            super.finalize();
            this.mAssets.releaseTheme(this.mTheme);
        }

        Theme() {
            this.mAssets = Resources.this.mAssets;
            this.mTheme = this.mAssets.createTheme();
        }

        /* access modifiers changed from: package-private */
        public long getNativeTheme() {
            return this.mTheme;
        }

        /* access modifiers changed from: package-private */
        public int getAppliedStyleResId() {
            return this.mThemeResId;
        }

        /* access modifiers changed from: package-private */
        public String getKey() {
            return this.mKey;
        }

        private String getResourceNameFromHexString(String hexString) {
            return Resources.this.getResourceName(Integer.parseInt(hexString, 16));
        }

        @ViewDebug.ExportedProperty(category = "theme", hasAdjacentMapping = true)
        public String[] getTheme() {
            String str;
            String[] themeData = this.mKey.split(" ");
            String[] themes = new String[(themeData.length * 2)];
            int i = 0;
            int j = themeData.length - 1;
            while (i < themes.length) {
                String theme = themeData[j];
                boolean forced = theme.endsWith("!");
                themes[i] = forced ? getResourceNameFromHexString(theme.substring(0, theme.length() - 1)) : getResourceNameFromHexString(theme);
                int i2 = i + 1;
                if (forced) {
                    str = "forced";
                } else {
                    str = "not forced";
                }
                themes[i2] = str;
                i += 2;
                j--;
            }
            return themes;
        }
    }

    public final Theme newTheme() {
        return new Theme();
    }

    public TypedArray obtainAttributes(AttributeSet set, int[] attrs) {
        TypedArray array = TypedArray.obtain(this, attrs.length);
        XmlBlock.Parser parser = (XmlBlock.Parser) set;
        this.mAssets.retrieveAttributes(parser.mParseState, attrs, array.mData, array.mIndices);
        array.mXml = parser;
        return array;
    }

    public void updateConfiguration(Configuration config, DisplayMetrics metrics) {
        updateConfiguration(config, metrics, null);
    }

    public void updateConfiguration(Configuration config, DisplayMetrics metrics, CompatibilityInfo compat) {
        int width;
        int height;
        synchronized (this.mAccessLock) {
            if (compat != null) {
                this.mCompatibilityInfo = compat;
            }
            if (metrics != null) {
                this.mMetrics.setTo(metrics);
            }
            this.mCompatibilityInfo.applyToDisplayMetrics(this.mMetrics);
            int configChanges = 268435455;
            if (config != null) {
                this.mTmpConfig.setTo(config);
                int density = config.densityDpi;
                if (density == 0) {
                    density = this.mMetrics.noncompatDensityDpi;
                }
                this.mCompatibilityInfo.applyToConfiguration(density, this.mTmpConfig);
                if (this.mTmpConfig.locale == null) {
                    this.mTmpConfig.locale = Locale.getDefault();
                    this.mTmpConfig.setLayoutDirection(this.mTmpConfig.locale);
                }
                configChanges = ActivityInfo.activityInfoConfigToNative(this.mConfiguration.updateFrom(this.mTmpConfig));
            }
            if (this.mConfiguration.locale == null) {
                this.mConfiguration.locale = Locale.getDefault();
                this.mConfiguration.setLayoutDirection(this.mConfiguration.locale);
            }
            if (this.mConfiguration.densityDpi != 0) {
                this.mMetrics.densityDpi = this.mConfiguration.densityDpi;
                this.mMetrics.density = ((float) this.mConfiguration.densityDpi) * 0.00625f;
            }
            this.mMetrics.scaledDensity = this.mMetrics.density * this.mConfiguration.fontScale;
            String locale = null;
            if (this.mConfiguration.locale != null) {
                locale = adjustLanguageTag(this.mConfiguration.locale.toLanguageTag());
            }
            if (this.mMetrics.widthPixels >= this.mMetrics.heightPixels) {
                width = this.mMetrics.widthPixels;
                height = this.mMetrics.heightPixels;
            } else {
                width = this.mMetrics.heightPixels;
                height = this.mMetrics.widthPixels;
            }
            int keyboardHidden = this.mConfiguration.keyboardHidden;
            if (keyboardHidden == 1 && this.mConfiguration.hardKeyboardHidden == 2) {
                keyboardHidden = 3;
            }
            this.mAssets.setConfiguration(this.mConfiguration.mcc, this.mConfiguration.mnc, locale, this.mConfiguration.orientation, this.mConfiguration.touchscreen, this.mConfiguration.densityDpi, this.mConfiguration.keyboard, keyboardHidden, this.mConfiguration.navigation, width, height, this.mConfiguration.smallestScreenWidthDp, this.mConfiguration.screenWidthDp, this.mConfiguration.screenHeightDp, this.mConfiguration.screenLayout, this.mConfiguration.uiMode, Build.VERSION.RESOURCES_SDK_INT);
            clearDrawableCachesLocked(this.mDrawableCache, configChanges);
            clearDrawableCachesLocked(this.mColorDrawableCache, configChanges);
            this.mColorStateListCache.clear();
            flushLayoutCache();
        }
        synchronized (sSync) {
            if (this.mPluralRule != null) {
                this.mPluralRule = NativePluralRules.forLocale(config.locale);
            }
        }
    }

    private void clearDrawableCachesLocked(ArrayMap<String, LongSparseArray<WeakReference<Drawable.ConstantState>>> caches, int configChanges) {
        int N = caches.size();
        for (int i = 0; i < N; i++) {
            clearDrawableCacheLocked(caches.valueAt(i), configChanges);
        }
    }

    private void clearDrawableCacheLocked(LongSparseArray<WeakReference<Drawable.ConstantState>> cache, int configChanges) {
        Drawable.ConstantState cs;
        int N = cache.size();
        for (int i = 0; i < N; i++) {
            WeakReference<Drawable.ConstantState> ref = cache.valueAt(i);
            if (!(ref == null || (cs = ref.get()) == null || !Configuration.needNewResources(configChanges, cs.getChangingConfigurations()))) {
                cache.setValueAt(i, null);
            }
        }
    }

    private static String adjustLanguageTag(String languageTag) {
        String language;
        String remainder;
        int separator = languageTag.indexOf(45);
        if (separator == -1) {
            language = languageTag;
            remainder = ProxyInfo.LOCAL_EXCL_LIST;
        } else {
            language = languageTag.substring(0, separator);
            remainder = languageTag.substring(separator);
        }
        return Locale.adjustLanguageCode(language) + remainder;
    }

    public static void updateSystemConfiguration(Configuration config, DisplayMetrics metrics, CompatibilityInfo compat) {
        if (mSystem != null) {
            mSystem.updateConfiguration(config, metrics, compat);
        }
    }

    public DisplayMetrics getDisplayMetrics() {
        return this.mMetrics;
    }

    public Configuration getConfiguration() {
        return this.mConfiguration;
    }

    public CompatibilityInfo getCompatibilityInfo() {
        return this.mCompatibilityInfo;
    }

    public void setCompatibilityInfo(CompatibilityInfo ci) {
        if (ci != null) {
            this.mCompatibilityInfo = ci;
            updateConfiguration(this.mConfiguration, this.mMetrics);
        }
    }

    public int getIdentifier(String name, String defType, String defPackage) {
        if (name == null) {
            throw new NullPointerException("name is null");
        }
        try {
            return Integer.parseInt(name);
        } catch (Exception e) {
            return this.mAssets.getResourceIdentifier(name, defType, defPackage);
        }
    }

    public static boolean resourceHasPackage(int resid) {
        return (resid >>> 24) != 0;
    }

    public String getResourceName(int resid) throws NotFoundException {
        String str = this.mAssets.getResourceName(resid);
        if (str != null) {
            return str;
        }
        throw new NotFoundException("Unable to find resource ID #0x" + Integer.toHexString(resid));
    }

    public String getResourcePackageName(int resid) throws NotFoundException {
        String str = this.mAssets.getResourcePackageName(resid);
        if (str != null) {
            return str;
        }
        throw new NotFoundException("Unable to find resource ID #0x" + Integer.toHexString(resid));
    }

    public String getResourceTypeName(int resid) throws NotFoundException {
        String str = this.mAssets.getResourceTypeName(resid);
        if (str != null) {
            return str;
        }
        throw new NotFoundException("Unable to find resource ID #0x" + Integer.toHexString(resid));
    }

    public String getResourceEntryName(int resid) throws NotFoundException {
        String str = this.mAssets.getResourceEntryName(resid);
        if (str != null) {
            return str;
        }
        throw new NotFoundException("Unable to find resource ID #0x" + Integer.toHexString(resid));
    }

    public void parseBundleExtras(XmlResourceParser parser, Bundle outBundle) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4)) {
                if (parser.getName().equals("extra")) {
                    parseBundleExtra("extra", parser, outBundle);
                    XmlUtils.skipCurrentTag(parser);
                } else {
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
    }

    public void parseBundleExtra(String tagName, AttributeSet attrs, Bundle outBundle) throws XmlPullParserException {
        boolean z = true;
        TypedArray sa = obtainAttributes(attrs, R.styleable.Extra);
        String name = sa.getString(0);
        if (name == null) {
            sa.recycle();
            throw new XmlPullParserException("<" + tagName + "> requires an android:name attribute at " + attrs.getPositionDescription());
        }
        TypedValue v = sa.peekValue(1);
        if (v != null) {
            if (v.type == 3) {
                outBundle.putCharSequence(name, v.coerceToString());
            } else if (v.type == 18) {
                if (v.data == 0) {
                    z = false;
                }
                outBundle.putBoolean(name, z);
            } else if (v.type >= 16 && v.type <= 31) {
                outBundle.putInt(name, v.data);
            } else if (v.type == 4) {
                outBundle.putFloat(name, v.getFloat());
            } else {
                sa.recycle();
                throw new XmlPullParserException("<" + tagName + "> only supports string, integer, float, color, and boolean at " + attrs.getPositionDescription());
            }
            sa.recycle();
            return;
        }
        sa.recycle();
        throw new XmlPullParserException("<" + tagName + "> requires an android:value or android:resource attribute at " + attrs.getPositionDescription());
    }

    public final AssetManager getAssets() {
        return this.mAssets;
    }

    public final void flushLayoutCache() {
        synchronized (this.mCachedXmlBlockIds) {
            int num = this.mCachedXmlBlockIds.length;
            for (int i = 0; i < num; i++) {
                this.mCachedXmlBlockIds[i] = 0;
                XmlBlock oldBlock = this.mCachedXmlBlocks[i];
                if (oldBlock != null) {
                    oldBlock.close();
                }
                this.mCachedXmlBlocks[i] = null;
            }
        }
    }

    public final void startPreloading() {
        synchronized (sSync) {
            if (sPreloaded) {
                throw new IllegalStateException("Resources already preloaded");
            }
            sPreloaded = true;
            this.mPreloading = true;
            sPreloadedDensity = DisplayMetrics.DENSITY_DEVICE;
            this.mConfiguration.densityDpi = sPreloadedDensity;
            updateConfiguration(null, null);
        }
    }

    public final void finishPreloading() {
        if (this.mPreloading) {
            this.mPreloading = false;
            flushLayoutCache();
        }
    }

    public LongSparseArray<Drawable.ConstantState> getPreloadedDrawables() {
        return sPreloadedDrawables[0];
    }

    private boolean verifyPreloadConfig(int changingConfigurations, int allowVarying, int resourceId, String name) {
        String resName;
        if ((-1073745921 & changingConfigurations & (allowVarying ^ -1)) == 0) {
            return true;
        }
        try {
            resName = getResourceName(resourceId);
        } catch (NotFoundException e) {
            resName = "?";
        }
        Log.w(TAG, "Preloaded " + name + " resource #0x" + Integer.toHexString(resourceId) + " (" + resName + ") that varies with configuration!!");
        return false;
    }

    /* access modifiers changed from: package-private */
    public Drawable loadDrawable(TypedValue value, int id, Theme theme) throws NotFoundException {
        boolean isColorDrawable;
        ArrayMap<String, LongSparseArray<WeakReference<Drawable.ConstantState>>> caches;
        long key;
        Drawable.ConstantState cs;
        Drawable dr;
        Drawable cachedDrawable;
        if (value.type < 28 || value.type > 31) {
            isColorDrawable = false;
            caches = this.mDrawableCache;
            key = (((long) value.assetCookie) << 32) | ((long) value.data);
        } else {
            isColorDrawable = true;
            caches = this.mColorDrawableCache;
            key = (long) value.data;
        }
        if (!this.mPreloading && (cachedDrawable = getCachedDrawable(caches, key, theme)) != null) {
            return cachedDrawable;
        }
        if (isColorDrawable) {
            cs = sPreloadedColorDrawables.get(key);
        } else {
            cs = sPreloadedDrawables[this.mConfiguration.getLayoutDirection()].get(key);
        }
        if (cs != null) {
            dr = cs.newDrawable(this, theme);
        } else if (isColorDrawable) {
            dr = new ColorDrawable(value.data);
        } else {
            dr = loadDrawableForCookie(value, id, theme);
        }
        if (dr != null) {
            dr.setChangingConfigurations(value.changingConfigurations);
            cacheDrawable(value, theme, isColorDrawable, caches, key, dr);
        }
        return dr;
    }

    private void cacheDrawable(TypedValue value, Theme theme, boolean isColorDrawable, ArrayMap<String, LongSparseArray<WeakReference<Drawable.ConstantState>>> caches, long key, Drawable dr) {
        Drawable.ConstantState cs = dr.getConstantState();
        if (cs != null) {
            if (this.mPreloading) {
                int changingConfigs = cs.getChangingConfigurations();
                if (isColorDrawable) {
                    if (verifyPreloadConfig(changingConfigs, 0, value.resourceId, "drawable")) {
                        sPreloadedColorDrawables.put(key, cs);
                    }
                } else if (!verifyPreloadConfig(changingConfigs, LAYOUT_DIR_CONFIG, value.resourceId, "drawable")) {
                } else {
                    if ((LAYOUT_DIR_CONFIG & changingConfigs) == 0) {
                        sPreloadedDrawables[0].put(key, cs);
                        sPreloadedDrawables[1].put(key, cs);
                        return;
                    }
                    sPreloadedDrawables[this.mConfiguration.getLayoutDirection()].put(key, cs);
                }
            } else {
                synchronized (this.mAccessLock) {
                    String themeKey = theme == null ? ProxyInfo.LOCAL_EXCL_LIST : theme.mKey;
                    LongSparseArray<WeakReference<Drawable.ConstantState>> themedCache = caches.get(themeKey);
                    if (themedCache == null) {
                        themedCache = new LongSparseArray<>(1);
                        caches.put(themeKey, themedCache);
                    }
                    themedCache.put(key, new WeakReference<>(cs));
                }
            }
        }
    }

    private Drawable loadDrawableForCookie(TypedValue value, int id, Theme theme) {
        Drawable dr;
        if (value.string == null) {
            throw new NotFoundException("Resource \"" + getResourceName(id) + "\" (" + Integer.toHexString(id) + ")  is not a Drawable (color or path): " + value);
        }
        String file = value.string.toString();
        Trace.traceBegin(Trace.TRACE_TAG_RESOURCES, file);
        try {
            if (file.endsWith(".xml")) {
                XmlResourceParser rp = loadXmlResourceParser(file, id, value.assetCookie, "drawable");
                dr = Drawable.createFromXml(this, rp, theme);
                rp.close();
            } else {
                InputStream is = this.mAssets.openNonAsset(value.assetCookie, file, 2);
                dr = Drawable.createFromResourceStream(this, value, is, file, null);
                is.close();
            }
            Trace.traceEnd(Trace.TRACE_TAG_RESOURCES);
            return dr;
        } catch (Exception e) {
            Trace.traceEnd(Trace.TRACE_TAG_RESOURCES);
            NotFoundException rnf = new NotFoundException("File " + file + " from drawable resource ID #0x" + Integer.toHexString(id));
            rnf.initCause(e);
            throw rnf;
        }
    }

    private Drawable getCachedDrawable(ArrayMap<String, LongSparseArray<WeakReference<Drawable.ConstantState>>> caches, long key, Theme theme) {
        Drawable themedDrawable;
        synchronized (this.mAccessLock) {
            LongSparseArray<WeakReference<Drawable.ConstantState>> themedCache = caches.get(theme != null ? theme.mKey : ProxyInfo.LOCAL_EXCL_LIST);
            if (themedCache == null || (themedDrawable = getCachedDrawableLocked(themedCache, key)) == null) {
                themedDrawable = null;
            }
        }
        return themedDrawable;
    }

    private Drawable.ConstantState getConstantStateLocked(LongSparseArray<WeakReference<Drawable.ConstantState>> drawableCache, long key) {
        WeakReference<Drawable.ConstantState> wr = drawableCache.get(key);
        if (wr != null) {
            Drawable.ConstantState entry = wr.get();
            if (entry != null) {
                return entry;
            }
            drawableCache.delete(key);
        }
        return null;
    }

    private Drawable getCachedDrawableLocked(LongSparseArray<WeakReference<Drawable.ConstantState>> drawableCache, long key) {
        Drawable.ConstantState entry = getConstantStateLocked(drawableCache, key);
        if (entry != null) {
            return entry.newDrawable(this);
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public ColorStateList loadColorStateList(TypedValue value, int id) throws NotFoundException {
        long key = (((long) value.assetCookie) << 32) | ((long) value.data);
        if (value.type < 28 || value.type > 31) {
            ColorStateList csl = getCachedColorStateList(key);
            if (csl != null) {
                return csl;
            }
            ColorStateList csl2 = sPreloadedColorStateLists.get(key);
            if (csl2 != null) {
                return csl2;
            }
            if (value.string == null) {
                throw new NotFoundException("Resource is not a ColorStateList (color or path): " + value);
            }
            String file = value.string.toString();
            if (file.endsWith(".xml")) {
                Trace.traceBegin(Trace.TRACE_TAG_RESOURCES, file);
                try {
                    XmlResourceParser rp = loadXmlResourceParser(file, id, value.assetCookie, "colorstatelist");
                    ColorStateList csl3 = ColorStateList.createFromXml(this, rp);
                    rp.close();
                    Trace.traceEnd(Trace.TRACE_TAG_RESOURCES);
                    if (csl3 != null) {
                        if (!this.mPreloading) {
                            synchronized (this.mAccessLock) {
                                this.mColorStateListCache.put(key, new WeakReference<>(csl3));
                            }
                        } else if (verifyPreloadConfig(value.changingConfigurations, 0, value.resourceId, "color")) {
                            sPreloadedColorStateLists.put(key, csl3);
                        }
                    }
                    return csl3;
                } catch (Exception e) {
                    Trace.traceEnd(Trace.TRACE_TAG_RESOURCES);
                    NotFoundException rnf = new NotFoundException("File " + file + " from color state list resource ID #0x" + Integer.toHexString(id));
                    rnf.initCause(e);
                    throw rnf;
                }
            } else {
                throw new NotFoundException("File " + file + " from drawable resource ID #0x" + Integer.toHexString(id) + ": .xml extension required");
            }
        } else {
            ColorStateList csl4 = sPreloadedColorStateLists.get(key);
            if (csl4 != null) {
                return csl4;
            }
            ColorStateList csl5 = ColorStateList.valueOf(value.data);
            if (this.mPreloading && verifyPreloadConfig(value.changingConfigurations, 0, value.resourceId, "color")) {
                sPreloadedColorStateLists.put(key, csl5);
            }
            return csl5;
        }
    }

    private ColorStateList getCachedColorStateList(long key) {
        synchronized (this.mAccessLock) {
            WeakReference<ColorStateList> wr = this.mColorStateListCache.get(key);
            if (wr != null) {
                ColorStateList entry = wr.get();
                if (entry != null) {
                    return entry;
                }
                this.mColorStateListCache.delete(key);
            }
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public XmlResourceParser loadXmlResourceParser(int id, String type) throws NotFoundException {
        XmlResourceParser loadXmlResourceParser;
        synchronized (this.mAccessLock) {
            TypedValue value = this.mTmpValue;
            if (value == null) {
                value = new TypedValue();
                this.mTmpValue = value;
            }
            getValue(id, value, true);
            if (value.type == 3) {
                loadXmlResourceParser = loadXmlResourceParser(value.string.toString(), id, value.assetCookie, type);
            } else {
                throw new NotFoundException("Resource ID #0x" + Integer.toHexString(id) + " type #0x" + Integer.toHexString(value.type) + " is not valid");
            }
        }
        return loadXmlResourceParser;
    }

    /* access modifiers changed from: package-private */
    public XmlResourceParser loadXmlResourceParser(String file, int id, int assetCookie, String type) throws NotFoundException {
        XmlResourceParser newParser;
        if (id != 0) {
            try {
                synchronized (this.mCachedXmlBlockIds) {
                    int num = this.mCachedXmlBlockIds.length;
                    int i = 0;
                    while (true) {
                        if (i >= num) {
                            XmlBlock block = this.mAssets.openXmlBlockAsset(assetCookie, file);
                            if (block != null) {
                                int pos = this.mLastCachedXmlBlockIndex + 1;
                                if (pos >= num) {
                                    pos = 0;
                                }
                                this.mLastCachedXmlBlockIndex = pos;
                                XmlBlock oldBlock = this.mCachedXmlBlocks[pos];
                                if (oldBlock != null) {
                                    oldBlock.close();
                                }
                                this.mCachedXmlBlockIds[pos] = id;
                                this.mCachedXmlBlocks[pos] = block;
                                newParser = block.newParser();
                            }
                        } else if (this.mCachedXmlBlockIds[i] == id) {
                            newParser = this.mCachedXmlBlocks[i].newParser();
                        } else {
                            i++;
                        }
                    }
                    return newParser;
                }
            } catch (Exception e) {
                NotFoundException rnf = new NotFoundException("File " + file + " from xml type " + type + " resource ID #0x" + Integer.toHexString(id));
                rnf.initCause(e);
                throw rnf;
            }
        }
        throw new NotFoundException("File " + file + " from xml type " + type + " resource ID #0x" + Integer.toHexString(id));
    }

    /* access modifiers changed from: package-private */
    public void recycleCachedStyledAttributes(TypedArray attrs) {
        synchronized (this.mAccessLock) {
            TypedArray cached = this.mCachedStyledAttributes;
            if (cached == null || cached.mData.length < attrs.mData.length) {
                this.mCachedStyledAttributes = attrs;
            }
        }
    }

    private Resources() {
        this.mTypedArrayPool = new Pools.SynchronizedPool<>(5);
        this.mAccessLock = new Object();
        this.mTmpConfig = new Configuration();
        this.mDrawableCache = new ArrayMap<>();
        this.mColorDrawableCache = new ArrayMap<>();
        this.mColorStateListCache = new LongSparseArray<>();
        this.mTmpValue = new TypedValue();
        this.mCachedStyledAttributes = null;
        this.mLastCachedXmlBlockIndex = -1;
        this.mCachedXmlBlockIds = new int[]{0, 0, 0, 0};
        this.mCachedXmlBlocks = new XmlBlock[4];
        this.mMetrics = new DisplayMetrics();
        this.mConfiguration = new Configuration();
        this.mCompatibilityInfo = CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO;
        this.mAssets = AssetManager.getSystem();
        this.mConfiguration.setToDefaults();
        this.mMetrics.setToDefaults();
        updateConfiguration(null, null);
        this.mAssets.ensureStringBlocks();
    }
}

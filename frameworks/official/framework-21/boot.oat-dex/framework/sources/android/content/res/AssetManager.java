package android.content.res;

import android.os.ParcelFileDescriptor;
import android.util.SparseArray;
import android.util.TypedValue;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public final class AssetManager implements AutoCloseable {
    public static final int ACCESS_BUFFER = 3;
    public static final int ACCESS_RANDOM = 1;
    public static final int ACCESS_STREAMING = 2;
    public static final int ACCESS_UNKNOWN = 0;
    private static final boolean DEBUG_REFS = false;
    static final int STYLE_ASSET_COOKIE = 2;
    static final int STYLE_CHANGING_CONFIGURATIONS = 4;
    static final int STYLE_DATA = 1;
    static final int STYLE_DENSITY = 5;
    static final int STYLE_NUM_ENTRIES = 6;
    static final int STYLE_RESOURCE_ID = 3;
    static final int STYLE_TYPE = 0;
    private static final String TAG = "AssetManager";
    private static final boolean localLOGV = false;
    private static final Object sSync = new Object();
    static AssetManager sSystem = null;
    private int mNumRefs;
    private long mObject;
    private final long[] mOffsets;
    private boolean mOpen;
    private HashMap<Long, RuntimeException> mRefStacks;
    private StringBlock[] mStringBlocks;
    private final TypedValue mValue;

    private final native int addAssetPathNative(String str);

    static final native boolean applyStyle(long j, int i, int i2, long j2, int[] iArr, int[] iArr2, int[] iArr3);

    static final native void applyThemeStyle(long j, int i, boolean z);

    static final native void copyTheme(long j, long j2);

    private final native void deleteTheme(long j);

    private final native void destroy();

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final native void destroyAsset(long j);

    static final native void dumpTheme(long j, int i, String str, String str2);

    private final native int[] getArrayStringInfo(int i);

    private final native String[] getArrayStringResource(int i);

    public static final native String getAssetAllocations();

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final native long getAssetLength(long j);

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final native long getAssetRemainingLength(long j);

    public static final native int getGlobalAssetCount();

    public static final native int getGlobalAssetManagerCount();

    private final native long getNativeStringBlock(int i);

    private final native int getStringBlockCount();

    private final native void init(boolean z);

    private final native int loadResourceBagValue(int i, int i2, TypedValue typedValue, boolean z);

    private final native int loadResourceValue(int i, short s, TypedValue typedValue, boolean z);

    static final native int loadThemeAttributeValue(long j, int i, TypedValue typedValue, boolean z);

    private final native long newTheme();

    private final native long openAsset(String str, int i);

    private final native ParcelFileDescriptor openAssetFd(String str, long[] jArr) throws IOException;

    private native ParcelFileDescriptor openNonAssetFdNative(int i, String str, long[] jArr) throws IOException;

    private final native long openNonAssetNative(int i, String str, int i2);

    private final native long openXmlAssetNative(int i, String str);

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final native int readAsset(long j, byte[] bArr, int i, int i2);

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final native int readAssetChar(long j);

    static final native boolean resolveAttrs(long j, int i, int i2, int[] iArr, int[] iArr2, int[] iArr3, int[] iArr4);

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final native long seekAsset(long j, long j2, int i);

    public final native int addOverlayPath(String str);

    /* access modifiers changed from: package-private */
    public final native int[] getArrayIntResource(int i);

    /* access modifiers changed from: package-private */
    public final native int getArraySize(int i);

    public final native SparseArray<String> getAssignedPackageIdentifiers();

    public final native String getCookieName(int i);

    public final native String[] getLocales();

    /* access modifiers changed from: package-private */
    public final native String getResourceEntryName(int i);

    /* access modifiers changed from: package-private */
    public final native int getResourceIdentifier(String str, String str2, String str3);

    /* access modifiers changed from: package-private */
    public final native String getResourceName(int i);

    /* access modifiers changed from: package-private */
    public final native String getResourcePackageName(int i);

    /* access modifiers changed from: package-private */
    public final native String getResourceTypeName(int i);

    /* access modifiers changed from: package-private */
    public final native int[] getStyleAttributes(int i);

    public final native boolean isUpToDate();

    public final native String[] list(String str) throws IOException;

    /* access modifiers changed from: package-private */
    public final native int retrieveArray(int i, int[] iArr);

    /* access modifiers changed from: package-private */
    public final native boolean retrieveAttributes(long j, int[] iArr, int[] iArr2, int[] iArr3);

    public final native void setConfiguration(int i, int i2, String str, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i10, int i11, int i12, int i13, int i14, int i15, int i16);

    public final native void setLocale(String str);

    public AssetManager() {
        this.mValue = new TypedValue();
        this.mOffsets = new long[2];
        this.mStringBlocks = null;
        this.mNumRefs = 1;
        this.mOpen = true;
        synchronized (this) {
            init(false);
            ensureSystemAssets();
        }
    }

    private static void ensureSystemAssets() {
        synchronized (sSync) {
            if (sSystem == null) {
                AssetManager system = new AssetManager(true);
                system.makeStringBlocks(null);
                sSystem = system;
            }
        }
    }

    private AssetManager(boolean isSystem) {
        this.mValue = new TypedValue();
        this.mOffsets = new long[2];
        this.mStringBlocks = null;
        this.mNumRefs = 1;
        this.mOpen = true;
        init(true);
    }

    public static AssetManager getSystem() {
        ensureSystemAssets();
        return sSystem;
    }

    @Override // java.lang.AutoCloseable
    public void close() {
        synchronized (this) {
            if (this.mOpen) {
                this.mOpen = false;
                decRefsLocked((long) hashCode());
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final CharSequence getResourceText(int ident) {
        synchronized (this) {
            TypedValue tmpValue = this.mValue;
            int block = loadResourceValue(ident, 0, tmpValue, true);
            if (block < 0) {
                return null;
            }
            if (tmpValue.type == 3) {
                return this.mStringBlocks[block].get(tmpValue.data);
            }
            return tmpValue.coerceToString();
        }
    }

    /* access modifiers changed from: package-private */
    public final CharSequence getResourceBagText(int ident, int bagEntryId) {
        synchronized (this) {
            TypedValue tmpValue = this.mValue;
            int block = loadResourceBagValue(ident, bagEntryId, tmpValue, true);
            if (block < 0) {
                return null;
            }
            if (tmpValue.type == 3) {
                return this.mStringBlocks[block].get(tmpValue.data);
            }
            return tmpValue.coerceToString();
        }
    }

    /* access modifiers changed from: package-private */
    public final String[] getResourceStringArray(int id) {
        return getArrayStringResource(id);
    }

    /* access modifiers changed from: package-private */
    public final boolean getResourceValue(int ident, int density, TypedValue outValue, boolean resolveRefs) {
        int block = loadResourceValue(ident, (short) density, outValue, resolveRefs);
        if (block < 0) {
            return false;
        }
        if (outValue.type != 3) {
            return true;
        }
        outValue.string = this.mStringBlocks[block].get(outValue.data);
        return true;
    }

    /* access modifiers changed from: package-private */
    public final CharSequence[] getResourceTextArray(int id) {
        int[] rawInfoArray = getArrayStringInfo(id);
        int rawInfoArrayLen = rawInfoArray.length;
        CharSequence[] retArray = new CharSequence[(rawInfoArrayLen / 2)];
        int i = 0;
        int j = 0;
        while (i < rawInfoArrayLen) {
            int block = rawInfoArray[i];
            int index = rawInfoArray[i + 1];
            retArray[j] = index >= 0 ? this.mStringBlocks[block].get(index) : null;
            i += 2;
            j++;
        }
        return retArray;
    }

    /* access modifiers changed from: package-private */
    public final boolean getThemeValue(long theme, int ident, TypedValue outValue, boolean resolveRefs) {
        int block = loadThemeAttributeValue(theme, ident, outValue, resolveRefs);
        if (block < 0) {
            return false;
        }
        if (outValue.type != 3) {
            return true;
        }
        StringBlock[] blocks = this.mStringBlocks;
        if (blocks == null) {
            ensureStringBlocks();
            blocks = this.mStringBlocks;
        }
        outValue.string = blocks[block].get(outValue.data);
        return true;
    }

    /* access modifiers changed from: package-private */
    public final void ensureStringBlocks() {
        if (this.mStringBlocks == null) {
            synchronized (this) {
                if (this.mStringBlocks == null) {
                    makeStringBlocks(sSystem.mStringBlocks);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final void makeStringBlocks(StringBlock[] seed) {
        int seedNum = seed != null ? seed.length : 0;
        int num = getStringBlockCount();
        this.mStringBlocks = new StringBlock[num];
        for (int i = 0; i < num; i++) {
            if (i < seedNum) {
                this.mStringBlocks[i] = seed[i];
            } else {
                this.mStringBlocks[i] = new StringBlock(getNativeStringBlock(i), true);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final CharSequence getPooledStringForCookie(int cookie, int id) {
        return this.mStringBlocks[cookie - 1].get(id);
    }

    public final InputStream open(String fileName) throws IOException {
        return open(fileName, 2);
    }

    public final InputStream open(String fileName, int accessMode) throws IOException {
        synchronized (this) {
            if (!this.mOpen) {
                throw new RuntimeException("Assetmanager has been closed");
            }
            long asset = openAsset(fileName, accessMode);
            if (asset != 0) {
                AssetInputStream res = new AssetInputStream(asset);
                incRefsLocked((long) res.hashCode());
                return res;
            }
            throw new FileNotFoundException("Asset file: " + fileName);
        }
    }

    public final AssetFileDescriptor openFd(String fileName) throws IOException {
        synchronized (this) {
            if (!this.mOpen) {
                throw new RuntimeException("Assetmanager has been closed");
            }
            ParcelFileDescriptor pfd = openAssetFd(fileName, this.mOffsets);
            if (pfd != null) {
                return new AssetFileDescriptor(pfd, this.mOffsets[0], this.mOffsets[1]);
            }
            throw new FileNotFoundException("Asset file: " + fileName);
        }
    }

    public final InputStream openNonAsset(String fileName) throws IOException {
        return openNonAsset(0, fileName, 2);
    }

    public final InputStream openNonAsset(String fileName, int accessMode) throws IOException {
        return openNonAsset(0, fileName, accessMode);
    }

    public final InputStream openNonAsset(int cookie, String fileName) throws IOException {
        return openNonAsset(cookie, fileName, 2);
    }

    public final InputStream openNonAsset(int cookie, String fileName, int accessMode) throws IOException {
        synchronized (this) {
            if (!this.mOpen) {
                throw new RuntimeException("Assetmanager has been closed");
            }
            long asset = openNonAssetNative(cookie, fileName, accessMode);
            if (asset != 0) {
                AssetInputStream res = new AssetInputStream(asset);
                incRefsLocked((long) res.hashCode());
                return res;
            }
            throw new FileNotFoundException("Asset absolute file: " + fileName);
        }
    }

    public final AssetFileDescriptor openNonAssetFd(String fileName) throws IOException {
        return openNonAssetFd(0, fileName);
    }

    public final AssetFileDescriptor openNonAssetFd(int cookie, String fileName) throws IOException {
        synchronized (this) {
            if (!this.mOpen) {
                throw new RuntimeException("Assetmanager has been closed");
            }
            ParcelFileDescriptor pfd = openNonAssetFdNative(cookie, fileName, this.mOffsets);
            if (pfd != null) {
                return new AssetFileDescriptor(pfd, this.mOffsets[0], this.mOffsets[1]);
            }
            throw new FileNotFoundException("Asset absolute file: " + fileName);
        }
    }

    public final XmlResourceParser openXmlResourceParser(String fileName) throws IOException {
        return openXmlResourceParser(0, fileName);
    }

    public final XmlResourceParser openXmlResourceParser(int cookie, String fileName) throws IOException {
        XmlBlock block = openXmlBlockAsset(cookie, fileName);
        XmlResourceParser rp = block.newParser();
        block.close();
        return rp;
    }

    /* access modifiers changed from: package-private */
    public final XmlBlock openXmlBlockAsset(String fileName) throws IOException {
        return openXmlBlockAsset(0, fileName);
    }

    /* access modifiers changed from: package-private */
    public final XmlBlock openXmlBlockAsset(int cookie, String fileName) throws IOException {
        synchronized (this) {
            if (!this.mOpen) {
                throw new RuntimeException("Assetmanager has been closed");
            }
            long xmlBlock = openXmlAssetNative(cookie, fileName);
            if (xmlBlock != 0) {
                XmlBlock res = new XmlBlock(this, xmlBlock);
                incRefsLocked((long) res.hashCode());
                return res;
            }
            throw new FileNotFoundException("Asset XML file: " + fileName);
        }
    }

    /* access modifiers changed from: package-private */
    public void xmlBlockGone(int id) {
        synchronized (this) {
            decRefsLocked((long) id);
        }
    }

    /* access modifiers changed from: package-private */
    public final long createTheme() {
        long res;
        synchronized (this) {
            if (!this.mOpen) {
                throw new RuntimeException("Assetmanager has been closed");
            }
            res = newTheme();
            incRefsLocked(res);
        }
        return res;
    }

    /* access modifiers changed from: package-private */
    public final void releaseTheme(long theme) {
        synchronized (this) {
            deleteTheme(theme);
            decRefsLocked(theme);
        }
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public void finalize() throws Throwable {
        try {
            destroy();
        } finally {
            super.finalize();
        }
    }

    public final class AssetInputStream extends InputStream {
        private long mAsset;
        private long mLength;
        private long mMarkPos;

        public final int getAssetInt() {
            throw new UnsupportedOperationException();
        }

        public final long getNativeAsset() {
            return this.mAsset;
        }

        private AssetInputStream(long asset) {
            this.mAsset = asset;
            this.mLength = AssetManager.this.getAssetLength(asset);
        }

        @Override // java.io.InputStream
        public final int read() throws IOException {
            return AssetManager.this.readAssetChar(this.mAsset);
        }

        public final boolean markSupported() {
            return true;
        }

        @Override // java.io.InputStream
        public final int available() throws IOException {
            long len = AssetManager.this.getAssetRemainingLength(this.mAsset);
            if (len > 2147483647L) {
                return Integer.MAX_VALUE;
            }
            return (int) len;
        }

        @Override // java.io.Closeable, java.lang.AutoCloseable, java.io.InputStream
        public final void close() throws IOException {
            synchronized (AssetManager.this) {
                if (this.mAsset != 0) {
                    AssetManager.this.destroyAsset(this.mAsset);
                    this.mAsset = 0;
                    AssetManager.this.decRefsLocked((long) hashCode());
                }
            }
        }

        public final void mark(int readlimit) {
            this.mMarkPos = AssetManager.this.seekAsset(this.mAsset, 0, 0);
        }

        @Override // java.io.InputStream
        public final void reset() throws IOException {
            AssetManager.this.seekAsset(this.mAsset, this.mMarkPos, -1);
        }

        @Override // java.io.InputStream
        public final int read(byte[] b) throws IOException {
            return AssetManager.this.readAsset(this.mAsset, b, 0, b.length);
        }

        @Override // java.io.InputStream
        public final int read(byte[] b, int off, int len) throws IOException {
            return AssetManager.this.readAsset(this.mAsset, b, off, len);
        }

        @Override // java.io.InputStream
        public final long skip(long n) throws IOException {
            long pos = AssetManager.this.seekAsset(this.mAsset, 0, 0);
            if (pos + n > this.mLength) {
                n = this.mLength - pos;
            }
            if (n > 0) {
                AssetManager.this.seekAsset(this.mAsset, n, 0);
            }
            return n;
        }

        /* access modifiers changed from: protected */
        @Override // java.lang.Object
        public void finalize() throws Throwable {
            close();
        }
    }

    public final int addAssetPath(String path) {
        int res;
        synchronized (this) {
            res = addAssetPathNative(path);
            makeStringBlocks(this.mStringBlocks);
        }
        return res;
    }

    public final int[] addAssetPaths(String[] paths) {
        if (paths == null) {
            return null;
        }
        int[] cookies = new int[paths.length];
        for (int i = 0; i < paths.length; i++) {
            cookies[i] = addAssetPath(paths[i]);
        }
        return cookies;
    }

    private final void incRefsLocked(long id) {
        this.mNumRefs++;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void decRefsLocked(long id) {
        this.mNumRefs--;
        if (this.mNumRefs == 0) {
            destroy();
        }
    }
}

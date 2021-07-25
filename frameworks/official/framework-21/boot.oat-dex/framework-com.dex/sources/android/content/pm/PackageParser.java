package android.content.pm;

import android.Manifest;
import android.accounts.GrantCredentialsPermissionActivity;
import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.os.Build;
import android.os.Bundle;
import android.os.PatternMatcher;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.util.Slog;
import android.util.TypedValue;
import android.view.HapticFeedbackConstants;
import com.android.internal.R;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.XmlUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.jar.StrictJarFile;
import java.util.zip.ZipEntry;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class PackageParser {
    private static final String ANDROID_MANIFEST_FILENAME = "AndroidManifest.xml";
    private static final String ANDROID_RESOURCES = "http://schemas.android.com/apk/res/android";
    private static final boolean DEBUG_BACKUP = false;
    private static final boolean DEBUG_JAR = false;
    private static final boolean DEBUG_PARSER = false;
    public static final NewPermissionInfo[] NEW_PERMISSIONS = {new NewPermissionInfo(Manifest.permission.WRITE_EXTERNAL_STORAGE, 4, 0), new NewPermissionInfo(Manifest.permission.READ_PHONE_STATE, 4, 0)};
    public static final int PARSE_CHATTY = 2;
    public static final int PARSE_COLLECT_CERTIFICATES = 256;
    private static final int PARSE_DEFAULT_INSTALL_LOCATION = -1;
    public static final int PARSE_FORWARD_LOCK = 16;
    public static final int PARSE_IGNORE_PROCESSES = 8;
    public static final int PARSE_IS_PRIVILEGED = 128;
    public static final int PARSE_IS_SYSTEM = 1;
    public static final int PARSE_IS_SYSTEM_DIR = 64;
    public static final int PARSE_MUST_BE_APK = 4;
    public static final int PARSE_ON_SDCARD = 32;
    public static final int PARSE_TRUSTED_OVERLAY = 512;
    private static final boolean RIGID_PARSER = false;
    private static final String[] SDK_CODENAMES = Build.VERSION.ACTIVE_CODENAMES;
    private static final int SDK_VERSION = Build.VERSION.SDK_INT;
    public static final SplitPermissionInfo[] SPLIT_PERMISSIONS = {new SplitPermissionInfo(Manifest.permission.WRITE_EXTERNAL_STORAGE, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, HapticFeedbackConstants.SAFE_MODE_ENABLED), new SplitPermissionInfo(Manifest.permission.READ_CONTACTS, new String[]{Manifest.permission.READ_CALL_LOG}, 16), new SplitPermissionInfo(Manifest.permission.WRITE_CONTACTS, new String[]{Manifest.permission.WRITE_CALL_LOG}, 16)};
    private static final String TAG = "PackageParser";
    private static AtomicReference<byte[]> sBuffer = new AtomicReference<>();
    private static boolean sCompatibilityModeEnabled = true;
    private static final Comparator<String> sSplitNameComparator = new SplitNameComparator();
    @Deprecated
    private String mArchiveSourcePath;
    private DisplayMetrics mMetrics = new DisplayMetrics();
    private boolean mOnlyCoreApps;
    private ParseComponentArgs mParseActivityAliasArgs;
    private ParseComponentArgs mParseActivityArgs;
    private int mParseError = 1;
    private ParsePackageItemArgs mParseInstrumentationArgs;
    private ParseComponentArgs mParseProviderArgs;
    private ParseComponentArgs mParseServiceArgs;
    private String[] mSeparateProcesses;

    public static class IntentInfo extends IntentFilter {
        public int banner;
        public boolean hasDefault;
        public int icon;
        public int labelRes;
        public int logo;
        public CharSequence nonLocalizedLabel;
        public int preferred;
    }

    public static class NewPermissionInfo {
        public final int fileVersion;
        public final String name;
        public final int sdkVersion;

        public NewPermissionInfo(String name2, int sdkVersion2, int fileVersion2) {
            this.name = name2;
            this.sdkVersion = sdkVersion2;
            this.fileVersion = fileVersion2;
        }
    }

    public static class SplitPermissionInfo {
        public final String[] newPerms;
        public final String rootPerm;
        public final int targetSdk;

        public SplitPermissionInfo(String rootPerm2, String[] newPerms2, int targetSdk2) {
            this.rootPerm = rootPerm2;
            this.newPerms = newPerms2;
            this.targetSdk = targetSdk2;
        }
    }

    /* access modifiers changed from: package-private */
    public static class ParsePackageItemArgs {
        final int bannerRes;
        final int iconRes;
        final int labelRes;
        final int logoRes;
        final int nameRes;
        final String[] outError;
        final Package owner;
        TypedArray sa;
        String tag;

        ParsePackageItemArgs(Package _owner, String[] _outError, int _nameRes, int _labelRes, int _iconRes, int _logoRes, int _bannerRes) {
            this.owner = _owner;
            this.outError = _outError;
            this.nameRes = _nameRes;
            this.labelRes = _labelRes;
            this.iconRes = _iconRes;
            this.logoRes = _logoRes;
            this.bannerRes = _bannerRes;
        }
    }

    /* access modifiers changed from: package-private */
    public static class ParseComponentArgs extends ParsePackageItemArgs {
        final int descriptionRes;
        final int enabledRes;
        int flags;
        final int processRes;
        final String[] sepProcesses;

        ParseComponentArgs(Package _owner, String[] _outError, int _nameRes, int _labelRes, int _iconRes, int _logoRes, int _bannerRes, String[] _sepProcesses, int _processRes, int _descriptionRes, int _enabledRes) {
            super(_owner, _outError, _nameRes, _labelRes, _iconRes, _logoRes, _bannerRes);
            this.sepProcesses = _sepProcesses;
            this.processRes = _processRes;
            this.descriptionRes = _descriptionRes;
            this.enabledRes = _enabledRes;
        }
    }

    public static class PackageLite {
        public final String baseCodePath;
        public final String codePath;
        public final boolean coreApp;
        public final int installLocation;
        public final boolean multiArch;
        public final String packageName;
        public final String[] splitCodePaths;
        public final String[] splitNames;
        public final VerifierInfo[] verifiers;
        public final int versionCode;

        public PackageLite(String codePath2, ApkLite baseApk, String[] splitNames2, String[] splitCodePaths2) {
            this.packageName = baseApk.packageName;
            this.versionCode = baseApk.versionCode;
            this.installLocation = baseApk.installLocation;
            this.verifiers = baseApk.verifiers;
            this.splitNames = splitNames2;
            this.codePath = codePath2;
            this.baseCodePath = baseApk.codePath;
            this.splitCodePaths = splitCodePaths2;
            this.coreApp = baseApk.coreApp;
            this.multiArch = baseApk.multiArch;
        }

        public List<String> getAllCodePaths() {
            ArrayList<String> paths = new ArrayList<>();
            paths.add(this.baseCodePath);
            if (!ArrayUtils.isEmpty(this.splitCodePaths)) {
                Collections.addAll(paths, this.splitCodePaths);
            }
            return paths;
        }
    }

    public static class ApkLite {
        public final String codePath;
        public final boolean coreApp;
        public final int installLocation;
        public final boolean multiArch;
        public final String packageName;
        public final Signature[] signatures;
        public final String splitName;
        public final VerifierInfo[] verifiers;
        public final int versionCode;

        public ApkLite(String codePath2, String packageName2, String splitName2, int versionCode2, int installLocation2, List<VerifierInfo> verifiers2, Signature[] signatures2, boolean coreApp2, boolean multiArch2) {
            this.codePath = codePath2;
            this.packageName = packageName2;
            this.splitName = splitName2;
            this.versionCode = versionCode2;
            this.installLocation = installLocation2;
            this.verifiers = (VerifierInfo[]) verifiers2.toArray(new VerifierInfo[verifiers2.size()]);
            this.signatures = signatures2;
            this.coreApp = coreApp2;
            this.multiArch = multiArch2;
        }
    }

    public PackageParser() {
        this.mMetrics.setToDefaults();
    }

    public void setSeparateProcesses(String[] procs) {
        this.mSeparateProcesses = procs;
    }

    public void setOnlyCoreApps(boolean onlyCoreApps) {
        this.mOnlyCoreApps = onlyCoreApps;
    }

    public void setDisplayMetrics(DisplayMetrics metrics) {
        this.mMetrics = metrics;
    }

    public static final boolean isApkFile(File file) {
        return isApkPath(file.getName());
    }

    private static boolean isApkPath(String path) {
        return path.endsWith(".apk");
    }

    public static PackageInfo generatePackageInfo(Package p, int[] gids, int flags, long firstInstallTime, long lastUpdateTime, HashSet<String> grantedPermissions, PackageUserState state) {
        return generatePackageInfo(p, gids, flags, firstInstallTime, lastUpdateTime, grantedPermissions, state, UserHandle.getCallingUserId());
    }

    private static boolean checkUseInstalledOrHidden(int flags, PackageUserState state) {
        return (state.installed && !state.hidden) || (flags & 8192) != 0;
    }

    public static boolean isAvailable(PackageUserState state) {
        return checkUseInstalledOrHidden(0, state);
    }

    public static PackageInfo generatePackageInfo(Package p, int[] gids, int flags, long firstInstallTime, long lastUpdateTime, HashSet<String> grantedPermissions, PackageUserState state, int userId) {
        int N;
        int N2;
        int j;
        int N3;
        int j2;
        int N4;
        int j3;
        int N5;
        int j4;
        if (!checkUseInstalledOrHidden(flags, state)) {
            return null;
        }
        PackageInfo pi = new PackageInfo();
        pi.packageName = p.packageName;
        pi.splitNames = p.splitNames;
        pi.versionCode = p.mVersionCode;
        pi.versionName = p.mVersionName;
        pi.sharedUserId = p.mSharedUserId;
        pi.sharedUserLabel = p.mSharedUserLabel;
        pi.applicationInfo = generateApplicationInfo(p, flags, state, userId);
        pi.installLocation = p.installLocation;
        pi.coreApp = p.coreApp;
        if (!((pi.applicationInfo.flags & 1) == 0 && (pi.applicationInfo.flags & 128) == 0)) {
            pi.requiredForAllUsers = p.mRequiredForAllUsers;
        }
        pi.restrictedAccountType = p.mRestrictedAccountType;
        pi.requiredAccountType = p.mRequiredAccountType;
        pi.overlayTarget = p.mOverlayTarget;
        pi.firstInstallTime = firstInstallTime;
        pi.lastUpdateTime = lastUpdateTime;
        if ((flags & 256) != 0) {
            pi.gids = gids;
        }
        if ((flags & 16384) != 0) {
            int N6 = p.configPreferences != null ? p.configPreferences.size() : 0;
            if (N6 > 0) {
                pi.configPreferences = new ConfigurationInfo[N6];
                p.configPreferences.toArray(pi.configPreferences);
            }
            int N7 = p.reqFeatures != null ? p.reqFeatures.size() : 0;
            if (N7 > 0) {
                pi.reqFeatures = new FeatureInfo[N7];
                p.reqFeatures.toArray(pi.reqFeatures);
            }
            int N8 = p.featureGroups != null ? p.featureGroups.size() : 0;
            if (N8 > 0) {
                pi.featureGroups = new FeatureGroupInfo[N8];
                p.featureGroups.toArray(pi.featureGroups);
            }
        }
        if ((flags & 1) != 0 && (N5 = p.activities.size()) > 0) {
            if ((flags & 512) != 0) {
                pi.activities = new ActivityInfo[N5];
            } else {
                int num = 0;
                for (int i = 0; i < N5; i++) {
                    if (p.activities.get(i).info.enabled) {
                        num++;
                    }
                }
                pi.activities = new ActivityInfo[num];
            }
            int i2 = 0;
            int j5 = 0;
            while (i2 < N5) {
                if (p.activities.get(i2).info.enabled || (flags & 512) != 0) {
                    j4 = j5 + 1;
                    pi.activities[j5] = generateActivityInfo(p.activities.get(i2), flags, state, userId);
                } else {
                    j4 = j5;
                }
                i2++;
                j5 = j4;
            }
        }
        if ((flags & 2) != 0 && (N4 = p.receivers.size()) > 0) {
            if ((flags & 512) != 0) {
                pi.receivers = new ActivityInfo[N4];
            } else {
                int num2 = 0;
                for (int i3 = 0; i3 < N4; i3++) {
                    if (p.receivers.get(i3).info.enabled) {
                        num2++;
                    }
                }
                pi.receivers = new ActivityInfo[num2];
            }
            int i4 = 0;
            int j6 = 0;
            while (i4 < N4) {
                if (p.receivers.get(i4).info.enabled || (flags & 512) != 0) {
                    j3 = j6 + 1;
                    pi.receivers[j6] = generateActivityInfo(p.receivers.get(i4), flags, state, userId);
                } else {
                    j3 = j6;
                }
                i4++;
                j6 = j3;
            }
        }
        if ((flags & 4) != 0 && (N3 = p.services.size()) > 0) {
            if ((flags & 512) != 0) {
                pi.services = new ServiceInfo[N3];
            } else {
                int num3 = 0;
                for (int i5 = 0; i5 < N3; i5++) {
                    if (p.services.get(i5).info.enabled) {
                        num3++;
                    }
                }
                pi.services = new ServiceInfo[num3];
            }
            int i6 = 0;
            int j7 = 0;
            while (i6 < N3) {
                if (p.services.get(i6).info.enabled || (flags & 512) != 0) {
                    j2 = j7 + 1;
                    pi.services[j7] = generateServiceInfo(p.services.get(i6), flags, state, userId);
                } else {
                    j2 = j7;
                }
                i6++;
                j7 = j2;
            }
        }
        if ((flags & 8) != 0 && (N2 = p.providers.size()) > 0) {
            if ((flags & 512) != 0) {
                pi.providers = new ProviderInfo[N2];
            } else {
                int num4 = 0;
                for (int i7 = 0; i7 < N2; i7++) {
                    if (p.providers.get(i7).info.enabled) {
                        num4++;
                    }
                }
                pi.providers = new ProviderInfo[num4];
            }
            int i8 = 0;
            int j8 = 0;
            while (i8 < N2) {
                if (p.providers.get(i8).info.enabled || (flags & 512) != 0) {
                    j = j8 + 1;
                    pi.providers[j8] = generateProviderInfo(p.providers.get(i8), flags, state, userId);
                } else {
                    j = j8;
                }
                i8++;
                j8 = j;
            }
        }
        if ((flags & 16) != 0 && (N = p.instrumentation.size()) > 0) {
            pi.instrumentation = new InstrumentationInfo[N];
            for (int i9 = 0; i9 < N; i9++) {
                pi.instrumentation[i9] = generateInstrumentationInfo(p.instrumentation.get(i9), flags);
            }
        }
        if ((flags & 4096) != 0) {
            int N9 = p.permissions.size();
            if (N9 > 0) {
                pi.permissions = new PermissionInfo[N9];
                for (int i10 = 0; i10 < N9; i10++) {
                    pi.permissions[i10] = generatePermissionInfo(p.permissions.get(i10), flags);
                }
            }
            int N10 = p.requestedPermissions.size();
            if (N10 > 0) {
                pi.requestedPermissions = new String[N10];
                pi.requestedPermissionsFlags = new int[N10];
                for (int i11 = 0; i11 < N10; i11++) {
                    String perm = p.requestedPermissions.get(i11);
                    pi.requestedPermissions[i11] = perm;
                    if (p.requestedPermissionsRequired.get(i11).booleanValue()) {
                        int[] iArr = pi.requestedPermissionsFlags;
                        iArr[i11] = iArr[i11] | 1;
                    }
                    if (grantedPermissions != null && grantedPermissions.contains(perm)) {
                        int[] iArr2 = pi.requestedPermissionsFlags;
                        iArr2[i11] = iArr2[i11] | 2;
                    }
                }
            }
        }
        if ((flags & 64) == 0) {
            return pi;
        }
        int N11 = p.mSignatures != null ? p.mSignatures.length : 0;
        if (N11 <= 0) {
            return pi;
        }
        pi.signatures = new Signature[N11];
        System.arraycopy(p.mSignatures, 0, pi.signatures, 0, N11);
        return pi;
    }

    private static Certificate[][] loadCertificates(StrictJarFile jarFile, ZipEntry entry) throws PackageParserException {
        Exception e;
        InputStream is = null;
        try {
            is = jarFile.getInputStream(entry);
            readFullyIgnoringContents(is);
            Certificate[][] certificateChains = jarFile.getCertificateChains(entry);
            IoUtils.closeQuietly(is);
            return certificateChains;
        } catch (IOException e2) {
            e = e2;
            try {
                throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION, "Failed reading " + entry.getName() + " in " + jarFile, e);
            } catch (Throwable th) {
                IoUtils.closeQuietly(is);
                throw th;
            }
        } catch (RuntimeException e3) {
            e = e3;
            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION, "Failed reading " + entry.getName() + " in " + jarFile, e);
        }
    }

    private static class SplitNameComparator implements Comparator<String> {
        private SplitNameComparator() {
        }

        public int compare(String lhs, String rhs) {
            if (lhs == null) {
                return -1;
            }
            if (rhs == null) {
                return 1;
            }
            return lhs.compareTo(rhs);
        }
    }

    public static PackageLite parsePackageLite(File packageFile, int flags) throws PackageParserException {
        if (packageFile.isDirectory()) {
            return parseClusterPackageLite(packageFile, flags);
        }
        return parseMonolithicPackageLite(packageFile, flags);
    }

    private static PackageLite parseMonolithicPackageLite(File packageFile, int flags) throws PackageParserException {
        return new PackageLite(packageFile.getAbsolutePath(), parseApkLite(packageFile, flags), null, null);
    }

    private static PackageLite parseClusterPackageLite(File packageDir, int flags) throws PackageParserException {
        File[] files = packageDir.listFiles();
        if (ArrayUtils.isEmpty(files)) {
            throw new PackageParserException(-100, "No packages found in split");
        }
        String packageName = null;
        int versionCode = 0;
        ArrayMap<String, ApkLite> apks = new ArrayMap<>();
        for (File file : files) {
            if (isApkFile(file)) {
                ApkLite lite = parseApkLite(file, flags);
                if (packageName == null) {
                    packageName = lite.packageName;
                    versionCode = lite.versionCode;
                } else if (!packageName.equals(lite.packageName)) {
                    throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_BAD_MANIFEST, "Inconsistent package " + lite.packageName + " in " + file + "; expected " + packageName);
                } else if (versionCode != lite.versionCode) {
                    throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_BAD_MANIFEST, "Inconsistent version " + lite.versionCode + " in " + file + "; expected " + versionCode);
                }
                if (apks.put(lite.splitName, lite) != null) {
                    throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_BAD_MANIFEST, "Split name " + lite.splitName + " defined more than once; most recent was " + file);
                }
            }
        }
        ApkLite baseApk = apks.remove(null);
        if (baseApk == null) {
            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_BAD_MANIFEST, "Missing base APK in " + packageDir);
        }
        int size = apks.size();
        String[] splitNames = null;
        String[] splitCodePaths = null;
        if (size > 0) {
            splitCodePaths = new String[size];
            splitNames = (String[]) apks.keySet().toArray(new String[size]);
            Arrays.sort(splitNames, sSplitNameComparator);
            for (int i = 0; i < size; i++) {
                splitCodePaths[i] = apks.get(splitNames[i]).codePath;
            }
        }
        return new PackageLite(packageDir.getAbsolutePath(), baseApk, splitNames, splitCodePaths);
    }

    public Package parsePackage(File packageFile, int flags) throws PackageParserException {
        if (packageFile.isDirectory()) {
            return parseClusterPackage(packageFile, flags);
        }
        return parseMonolithicPackage(packageFile, flags);
    }

    private Package parseClusterPackage(File packageDir, int flags) throws PackageParserException {
        PackageLite lite = parseClusterPackageLite(packageDir, 0);
        if (!this.mOnlyCoreApps || lite.coreApp) {
            AssetManager assets = new AssetManager();
            try {
                loadApkIntoAssetManager(assets, lite.baseCodePath, flags);
                if (!ArrayUtils.isEmpty(lite.splitCodePaths)) {
                    for (String path : lite.splitCodePaths) {
                        loadApkIntoAssetManager(assets, path, flags);
                    }
                }
                File baseApk = new File(lite.baseCodePath);
                Package pkg = parseBaseApk(baseApk, assets, flags);
                if (pkg == null) {
                    throw new PackageParserException(-100, "Failed to parse base APK: " + baseApk);
                }
                if (!ArrayUtils.isEmpty(lite.splitNames)) {
                    int num = lite.splitNames.length;
                    pkg.splitNames = lite.splitNames;
                    pkg.splitCodePaths = lite.splitCodePaths;
                    pkg.splitFlags = new int[num];
                    for (int i = 0; i < num; i++) {
                        parseSplitApk(pkg, i, assets, flags);
                    }
                }
                pkg.codePath = packageDir.getAbsolutePath();
                return pkg;
            } finally {
                IoUtils.closeQuietly(assets);
            }
        } else {
            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED, "Not a coreApp: " + packageDir);
        }
    }

    @Deprecated
    public Package parseMonolithicPackage(File apkFile, int flags) throws PackageParserException {
        if (!this.mOnlyCoreApps || parseMonolithicPackageLite(apkFile, flags).coreApp) {
            AssetManager assets = new AssetManager();
            try {
                Package pkg = parseBaseApk(apkFile, assets, flags);
                pkg.codePath = apkFile.getAbsolutePath();
                return pkg;
            } finally {
                IoUtils.closeQuietly(assets);
            }
        } else {
            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED, "Not a coreApp: " + apkFile);
        }
    }

    private static int loadApkIntoAssetManager(AssetManager assets, String apkPath, int flags) throws PackageParserException {
        if ((flags & 4) == 0 || isApkPath(apkPath)) {
            int cookie = assets.addAssetPath(apkPath);
            if (cookie != 0) {
                return cookie;
            }
            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_BAD_MANIFEST, "Failed adding asset path: " + apkPath);
        }
        throw new PackageParserException(-100, "Invalid package file: " + apkPath);
    }

    private Package parseBaseApk(File apkFile, AssetManager assets, int flags) throws PackageParserException {
        Exception e;
        Throwable th;
        String apkPath = apkFile.getAbsolutePath();
        this.mParseError = 1;
        this.mArchiveSourcePath = apkFile.getAbsolutePath();
        int cookie = loadApkIntoAssetManager(assets, apkPath, flags);
        try {
            Resources res = new Resources(assets, this.mMetrics, null);
            try {
                assets.setConfiguration(0, 0, null, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, Build.VERSION.RESOURCES_SDK_INT);
                XmlResourceParser parser = assets.openXmlResourceParser(cookie, ANDROID_MANIFEST_FILENAME);
                String[] outError = new String[1];
                Package pkg = parseBaseApk(res, parser, flags, outError);
                if (pkg == null) {
                    throw new PackageParserException(this.mParseError, apkPath + " (at " + parser.getPositionDescription() + "): " + outError[0]);
                }
                pkg.baseCodePath = apkPath;
                pkg.mSignatures = null;
                IoUtils.closeQuietly(parser);
                return pkg;
            } catch (PackageParserException e2) {
                e = e2;
                try {
                    throw e;
                } catch (Throwable th2) {
                    th = th2;
                }
            } catch (Exception e3) {
                e = e3;
                throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION, "Failed to read manifest from " + apkPath, e);
            } catch (Throwable th3) {
                th = th3;
                IoUtils.closeQuietly((AutoCloseable) null);
                throw th;
            }
        } catch (PackageParserException e4) {
            e = e4;
            throw e;
        } catch (Exception e5) {
            e = e5;
            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION, "Failed to read manifest from " + apkPath, e);
        }
    }

    private void parseSplitApk(Package pkg, int splitIndex, AssetManager assets, int flags) throws PackageParserException {
        XmlResourceParser parser;
        Exception e;
        String apkPath = pkg.splitCodePaths[splitIndex];
        new File(apkPath);
        this.mParseError = 1;
        this.mArchiveSourcePath = apkPath;
        int cookie = loadApkIntoAssetManager(assets, apkPath, flags);
        try {
            Resources res = new Resources(assets, this.mMetrics, null);
            try {
                assets.setConfiguration(0, 0, null, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, Build.VERSION.RESOURCES_SDK_INT);
                parser = assets.openXmlResourceParser(cookie, ANDROID_MANIFEST_FILENAME);
            } catch (PackageParserException e2) {
                e = e2;
                parser = null;
                try {
                    throw e;
                } catch (Throwable th) {
                    th = th;
                }
            } catch (Exception e3) {
                e = e3;
                throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION, "Failed to read manifest from " + apkPath, e);
            } catch (Throwable th2) {
                th = th2;
                parser = null;
                IoUtils.closeQuietly(parser);
                throw th;
            }
            try {
                String[] outError = new String[1];
                if (parseSplitApk(pkg, res, parser, flags, splitIndex, outError) == null) {
                    throw new PackageParserException(this.mParseError, apkPath + " (at " + parser.getPositionDescription() + "): " + outError[0]);
                }
                IoUtils.closeQuietly(parser);
            } catch (PackageParserException e4) {
                e = e4;
                throw e;
            } catch (Exception e5) {
                e = e5;
                throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION, "Failed to read manifest from " + apkPath, e);
            } catch (Throwable th3) {
                th = th3;
                IoUtils.closeQuietly(parser);
                throw th;
            }
        } catch (PackageParserException e6) {
            e = e6;
            parser = null;
            throw e;
        } catch (Exception e7) {
            e = e7;
            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION, "Failed to read manifest from " + apkPath, e);
        } catch (Throwable th4) {
            th = th4;
            parser = null;
            IoUtils.closeQuietly(parser);
            throw th;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x009a  */
    /* JADX WARNING: Removed duplicated region for block: B:30:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private android.content.pm.PackageParser.Package parseSplitApk(android.content.pm.PackageParser.Package r15, android.content.res.Resources r16, android.content.res.XmlResourceParser r17, int r18, int r19, java.lang.String[] r20) throws org.xmlpull.v1.XmlPullParserException, java.io.IOException, android.content.pm.PackageParser.PackageParserException {
        /*
        // Method dump skipped, instructions count: 164
        */
        throw new UnsupportedOperationException("Method not decompiled: android.content.pm.PackageParser.parseSplitApk(android.content.pm.PackageParser$Package, android.content.res.Resources, android.content.res.XmlResourceParser, int, int, java.lang.String[]):android.content.pm.PackageParser$Package");
    }

    public void collectManifestDigest(Package pkg) throws PackageParserException {
        pkg.manifestDigest = null;
        try {
            StrictJarFile jarFile = new StrictJarFile(pkg.baseCodePath);
            try {
                ZipEntry je = jarFile.findEntry(ANDROID_MANIFEST_FILENAME);
                if (je != null) {
                    pkg.manifestDigest = ManifestDigest.fromInputStream(jarFile.getInputStream(je));
                }
            } finally {
                jarFile.close();
            }
        } catch (IOException | RuntimeException e) {
            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED, "Failed to collect manifest digest");
        }
    }

    public void collectCertificates(Package pkg, int flags) throws PackageParserException {
        pkg.mCertificates = null;
        pkg.mSignatures = null;
        pkg.mSigningKeys = null;
        collectCertificates(pkg, new File(pkg.baseCodePath), flags);
        if (!ArrayUtils.isEmpty(pkg.splitCodePaths)) {
            for (String splitCodePath : pkg.splitCodePaths) {
                collectCertificates(pkg, new File(splitCodePath), flags);
            }
        }
    }

    private static void collectCertificates(Package pkg, File apkFile, int flags) throws PackageParserException {
        Exception e;
        Throwable th;
        String apkPath = apkFile.getAbsolutePath();
        StrictJarFile jarFile = null;
        try {
            StrictJarFile jarFile2 = new StrictJarFile(apkPath);
            try {
                ZipEntry manifestEntry = jarFile2.findEntry(ANDROID_MANIFEST_FILENAME);
                if (manifestEntry == null) {
                    throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_BAD_MANIFEST, "Package " + apkPath + " has no manifest");
                }
                List<ZipEntry> toVerify = new ArrayList<>();
                toVerify.add(manifestEntry);
                if ((flags & 1) == 0) {
                    Iterator<ZipEntry> i = jarFile2.iterator();
                    while (i.hasNext()) {
                        ZipEntry entry = i.next();
                        if (!entry.isDirectory() && !entry.getName().startsWith("META-INF/") && !entry.getName().equals(ANDROID_MANIFEST_FILENAME)) {
                            toVerify.add(entry);
                        }
                    }
                }
                for (ZipEntry entry2 : toVerify) {
                    Certificate[][] entryCerts = loadCertificates(jarFile2, entry2);
                    if (ArrayUtils.isEmpty(entryCerts)) {
                        throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_NO_CERTIFICATES, "Package " + apkPath + " has no certificates at entry " + entry2.getName());
                    }
                    Signature[] entrySignatures = convertToSignatures(entryCerts);
                    if (pkg.mCertificates == null) {
                        pkg.mCertificates = entryCerts;
                        pkg.mSignatures = entrySignatures;
                        pkg.mSigningKeys = new ArraySet<>();
                        for (Certificate[] certificateArr : entryCerts) {
                            pkg.mSigningKeys.add(certificateArr[0].getPublicKey());
                        }
                    } else if (!Signature.areExactMatch(pkg.mSignatures, entrySignatures)) {
                        throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES, "Package " + apkPath + " has mismatched certificates at entry " + entry2.getName());
                    }
                }
                closeQuietly(jarFile2);
            } catch (GeneralSecurityException e2) {
                e = e2;
                jarFile = jarFile2;
                try {
                    throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_CERTIFICATE_ENCODING, "Failed to collect certificates from " + apkPath, e);
                } catch (Throwable th2) {
                    th = th2;
                    closeQuietly(jarFile);
                    throw th;
                }
            } catch (IOException e3) {
                e = e3;
                e = e;
                throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_NO_CERTIFICATES, "Failed to collect certificates from " + apkPath, e);
            } catch (RuntimeException e4) {
                e = e4;
                e = e;
                throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_NO_CERTIFICATES, "Failed to collect certificates from " + apkPath, e);
            } catch (Throwable th3) {
                th = th3;
                jarFile = jarFile2;
                closeQuietly(jarFile);
                throw th;
            }
        } catch (GeneralSecurityException e5) {
            e = e5;
            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_CERTIFICATE_ENCODING, "Failed to collect certificates from " + apkPath, e);
        } catch (IOException e6) {
            e = e6;
            e = e;
            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_NO_CERTIFICATES, "Failed to collect certificates from " + apkPath, e);
        } catch (RuntimeException e7) {
            e = e7;
            e = e;
            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_NO_CERTIFICATES, "Failed to collect certificates from " + apkPath, e);
        }
    }

    private static Signature[] convertToSignatures(Certificate[][] certs) throws CertificateEncodingException {
        Signature[] res = new Signature[certs.length];
        for (int i = 0; i < certs.length; i++) {
            res[i] = new Signature(certs[i]);
        }
        return res;
    }

    public static ApkLite parseApkLite(File apkFile, int flags) throws PackageParserException {
        XmlResourceParser parser;
        AssetManager assets;
        Exception e;
        Exception e2;
        Exception e3;
        Signature[] signatures;
        String apkPath = apkFile.getAbsolutePath();
        try {
            assets = new AssetManager();
            try {
                assets.setConfiguration(0, 0, null, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, Build.VERSION.RESOURCES_SDK_INT);
                int cookie = assets.addAssetPath(apkPath);
                if (cookie == 0) {
                    throw new PackageParserException(-100, "Failed to parse " + apkPath);
                }
                DisplayMetrics metrics = new DisplayMetrics();
                metrics.setToDefaults();
                Resources res = new Resources(assets, metrics, null);
                parser = assets.openXmlResourceParser(cookie, ANDROID_MANIFEST_FILENAME);
                if ((flags & 256) != 0) {
                    try {
                        Package tempPkg = new Package(null);
                        collectCertificates(tempPkg, apkFile, 0);
                        signatures = tempPkg.mSignatures;
                    } catch (XmlPullParserException e4) {
                        e = e4;
                        e2 = e;
                        try {
                            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION, "Failed to parse " + apkPath, e2);
                        } catch (Throwable th) {
                            th = th;
                            IoUtils.closeQuietly(parser);
                            IoUtils.closeQuietly(assets);
                            throw th;
                        }
                    } catch (IOException e5) {
                        e3 = e5;
                        e2 = e3;
                        throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION, "Failed to parse " + apkPath, e2);
                    } catch (RuntimeException e6) {
                        e = e6;
                        e2 = e;
                        throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION, "Failed to parse " + apkPath, e2);
                    }
                } else {
                    signatures = null;
                }
                ApkLite parseApkLite = parseApkLite(apkPath, res, parser, parser, flags, signatures);
                IoUtils.closeQuietly(parser);
                IoUtils.closeQuietly(assets);
                return parseApkLite;
            } catch (XmlPullParserException e7) {
                e = e7;
                parser = null;
                e2 = e;
                throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION, "Failed to parse " + apkPath, e2);
            } catch (IOException e8) {
                e3 = e8;
                parser = null;
                e2 = e3;
                throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION, "Failed to parse " + apkPath, e2);
            } catch (RuntimeException e9) {
                e = e9;
                parser = null;
                e2 = e;
                throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION, "Failed to parse " + apkPath, e2);
            } catch (Throwable th2) {
                th = th2;
                parser = null;
                IoUtils.closeQuietly(parser);
                IoUtils.closeQuietly(assets);
                throw th;
            }
        } catch (XmlPullParserException e10) {
            e = e10;
            parser = null;
            assets = null;
            e2 = e;
            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION, "Failed to parse " + apkPath, e2);
        } catch (IOException e11) {
            e3 = e11;
            parser = null;
            assets = null;
            e2 = e3;
            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION, "Failed to parse " + apkPath, e2);
        } catch (RuntimeException e12) {
            e = e12;
            parser = null;
            assets = null;
            e2 = e;
            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION, "Failed to parse " + apkPath, e2);
        } catch (Throwable th3) {
            th = th3;
            parser = null;
            assets = null;
            IoUtils.closeQuietly(parser);
            IoUtils.closeQuietly(assets);
            throw th;
        }
    }

    private static String validateName(String name, boolean requiresSeparator) {
        int N = name.length();
        boolean hasSep = false;
        boolean front = true;
        for (int i = 0; i < N; i++) {
            char c = name.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                front = false;
            } else if (front || ((c < '0' || c > '9') && c != '_')) {
                if (c != '.') {
                    return "bad character '" + c + "'";
                }
                hasSep = true;
                front = true;
            }
        }
        if (hasSep || !requiresSeparator) {
            return null;
        }
        return "must have at least one '.' separator";
    }

    private static Pair<String, String> parsePackageSplitNames(XmlPullParser parser, AttributeSet attrs, int flags) throws IOException, XmlPullParserException, PackageParserException {
        int type;
        String error;
        do {
            type = parser.next();
            if (type == 2) {
                break;
            }
        } while (type != 1);
        if (type != 2) {
            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED, "No start tag found");
        } else if (!parser.getName().equals("manifest")) {
            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED, "No <manifest> tag");
        } else {
            String packageName = attrs.getAttributeValue(null, "package");
            if ("android".equals(packageName) || (error = validateName(packageName, true)) == null) {
                String splitName = attrs.getAttributeValue(null, "split");
                if (splitName != null) {
                    if (splitName.length() == 0) {
                        splitName = null;
                    } else {
                        String error2 = validateName(splitName, false);
                        if (error2 != null) {
                            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME, "Invalid manifest split: " + error2);
                        }
                    }
                }
                String intern = packageName.intern();
                if (splitName != null) {
                    splitName = splitName.intern();
                }
                return Pair.create(intern, splitName);
            }
            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME, "Invalid manifest package: " + error);
        }
    }

    private static ApkLite parseApkLite(String codePath, Resources res, XmlPullParser parser, AttributeSet attrs, int flags, Signature[] signatures) throws IOException, XmlPullParserException, PackageParserException {
        VerifierInfo verifier;
        Pair<String, String> packageSplit = parsePackageSplitNames(parser, attrs, flags);
        int installLocation = -1;
        int versionCode = 0;
        boolean coreApp = false;
        boolean multiArch = false;
        int numFound = 0;
        for (int i = 0; i < attrs.getAttributeCount(); i++) {
            String attr = attrs.getAttributeName(i);
            if (attr.equals("installLocation")) {
                installLocation = attrs.getAttributeIntValue(i, -1);
                numFound++;
            } else if (attr.equals("versionCode")) {
                versionCode = attrs.getAttributeIntValue(i, 0);
                numFound++;
            } else if (attr.equals("coreApp")) {
                coreApp = attrs.getAttributeBooleanValue(i, false);
                numFound++;
            }
            if (numFound >= 3) {
                break;
            }
        }
        int searchDepth = parser.getDepth() + 1;
        List<VerifierInfo> verifiers = new ArrayList<>();
        while (true) {
            int type = parser.next();
            if (type != 1 && (type != 3 || parser.getDepth() >= searchDepth)) {
                if (type != 3 && type != 4) {
                    if (parser.getDepth() == searchDepth && "package-verifier".equals(parser.getName()) && (verifier = parseVerifier(res, parser, attrs, flags)) != null) {
                        verifiers.add(verifier);
                    }
                    if (parser.getDepth() == searchDepth && GrantCredentialsPermissionActivity.EXTRAS_PACKAGES.equals(parser.getName())) {
                        int i2 = 0;
                        while (true) {
                            if (i2 >= attrs.getAttributeCount()) {
                                break;
                            } else if ("multiArch".equals(attrs.getAttributeName(i2))) {
                                multiArch = attrs.getAttributeBooleanValue(i2, false);
                                break;
                            } else {
                                i2++;
                            }
                        }
                    }
                }
            }
        }
        return new ApkLite(codePath, packageSplit.first, packageSplit.second, versionCode, installLocation, verifiers, signatures, coreApp, multiArch);
    }

    public static Signature stringToSignature(String str) {
        int N = str.length();
        byte[] sig = new byte[N];
        for (int i = 0; i < N; i++) {
            sig[i] = (byte) str.charAt(i);
        }
        return new Signature(sig);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:232:0x0795, code lost:
        r14 = android.content.pm.PackageParser.NEW_PERMISSIONS.length;
        r29 = null;
        r33 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:234:0x079e, code lost:
        if (r33 >= r14) goto L_0x07ae;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:235:0x07a0, code lost:
        r40 = android.content.pm.PackageParser.NEW_PERMISSIONS[r33];
     */
    /* JADX WARNING: Code restructure failed: missing block: B:236:0x07ac, code lost:
        if (r3.applicationInfo.targetSdkVersion < r40.sdkVersion) goto L_0x07df;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:237:0x07ae, code lost:
        if (r29 == null) goto L_0x07b9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:238:0x07b0, code lost:
        android.util.Slog.i(android.content.pm.PackageParser.TAG, r29.toString());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:239:0x07b9, code lost:
        r15 = android.content.pm.PackageParser.SPLIT_PERMISSIONS.length;
        r34 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:241:0x07c0, code lost:
        if (r34 >= r15) goto L_0x0855;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:242:0x07c2, code lost:
        r48 = android.content.pm.PackageParser.SPLIT_PERMISSIONS[r34];
     */
    /* JADX WARNING: Code restructure failed: missing block: B:243:0x07ce, code lost:
        if (r3.applicationInfo.targetSdkVersion >= r48.targetSdk) goto L_0x07dc;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:245:0x07da, code lost:
        if (r3.requestedPermissions.contains(r48.rootPerm) != false) goto L_0x0829;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:246:0x07dc, code lost:
        r34 = r34 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:248:0x07e9, code lost:
        if (r3.requestedPermissions.contains(r40.name) != false) goto L_0x081d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:249:0x07eb, code lost:
        if (r29 != null) goto L_0x0821;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:250:0x07ed, code lost:
        r29 = new java.lang.StringBuilder(128);
        r29.append(r3.packageName);
        r29.append(": compat added ");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:251:0x0804, code lost:
        r29.append(r40.name);
        r3.requestedPermissions.add(r40.name);
        r3.requestedPermissionsRequired.add(java.lang.Boolean.TRUE);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:252:0x081d, code lost:
        r33 = r33 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:253:0x0821, code lost:
        r29.append(' ');
     */
    /* JADX WARNING: Code restructure failed: missing block: B:254:0x0829, code lost:
        r30 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:256:0x0832, code lost:
        if (r30 >= r48.newPerms.length) goto L_0x07dc;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:257:0x0834, code lost:
        r44 = r48.newPerms[r30];
     */
    /* JADX WARNING: Code restructure failed: missing block: B:258:0x0842, code lost:
        if (r3.requestedPermissions.contains(r44) != false) goto L_0x0852;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:259:0x0844, code lost:
        r3.requestedPermissions.add(r44);
        r3.requestedPermissionsRequired.add(java.lang.Boolean.TRUE);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:260:0x0852, code lost:
        r30 = r30 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:261:0x0855, code lost:
        if (r53 < 0) goto L_0x0860;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:262:0x0857, code lost:
        if (r53 <= 0) goto L_0x0868;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:264:0x085e, code lost:
        if (r3.applicationInfo.targetSdkVersion < 4) goto L_0x0868;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:265:0x0860, code lost:
        r3.applicationInfo.flags |= 512;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:266:0x0868, code lost:
        if (r52 == 0) goto L_0x0872;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:267:0x086a, code lost:
        r3.applicationInfo.flags |= 1024;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:268:0x0872, code lost:
        if (r51 < 0) goto L_0x087d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:269:0x0874, code lost:
        if (r51 <= 0) goto L_0x0885;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:271:0x087b, code lost:
        if (r3.applicationInfo.targetSdkVersion < 4) goto L_0x0885;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:272:0x087d, code lost:
        r3.applicationInfo.flags |= 2048;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:273:0x0885, code lost:
        if (r54 < 0) goto L_0x0891;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:274:0x0887, code lost:
        if (r54 <= 0) goto L_0x089a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:276:0x088f, code lost:
        if (r3.applicationInfo.targetSdkVersion < 9) goto L_0x089a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:277:0x0891, code lost:
        r3.applicationInfo.flags |= 524288;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:278:0x089a, code lost:
        if (r46 < 0) goto L_0x08a5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:279:0x089c, code lost:
        if (r46 <= 0) goto L_0x08ad;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:281:0x08a3, code lost:
        if (r3.applicationInfo.targetSdkVersion < 4) goto L_0x08ad;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:282:0x08a5, code lost:
        r3.applicationInfo.flags |= 4096;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:283:0x08ad, code lost:
        if (r17 < 0) goto L_0x08b8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:284:0x08af, code lost:
        if (r17 <= 0) goto L_0x08c0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:286:0x08b6, code lost:
        if (r3.applicationInfo.targetSdkVersion < 4) goto L_0x08c0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:287:0x08b8, code lost:
        r3.applicationInfo.flags |= 8192;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:289:0x08c6, code lost:
        if (r3.applicationInfo.targetSdkVersion >= 18) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:290:0x08c8, code lost:
        r27 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:292:0x08d2, code lost:
        if (r27 >= r3.requestedPermissionsRequired.size()) goto L_0x005b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:293:0x08d4, code lost:
        r3.requestedPermissionsRequired.set(r27, java.lang.Boolean.TRUE);
        r27 = r27 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:363:?, code lost:
        return r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:364:?, code lost:
        return r3;
     */
    /* JADX WARNING: Removed duplicated region for block: B:119:0x03cf  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private android.content.pm.PackageParser.Package parseBaseApk(android.content.res.Resources r62, android.content.res.XmlResourceParser r63, int r64, java.lang.String[] r65) throws org.xmlpull.v1.XmlPullParserException, java.io.IOException {
        /*
        // Method dump skipped, instructions count: 2272
        */
        throw new UnsupportedOperationException("Method not decompiled: android.content.pm.PackageParser.parseBaseApk(android.content.res.Resources, android.content.res.XmlResourceParser, int, java.lang.String[]):android.content.pm.PackageParser$Package");
    }

    private FeatureInfo parseUsesFeature(Resources res, AttributeSet attrs) throws XmlPullParserException, IOException {
        FeatureInfo fi = new FeatureInfo();
        TypedArray sa = res.obtainAttributes(attrs, R.styleable.AndroidManifestUsesFeature);
        fi.name = sa.getNonResourceString(0);
        if (fi.name == null) {
            fi.reqGlEsVersion = sa.getInt(1, 0);
        }
        if (sa.getBoolean(2, true)) {
            fi.flags |= 1;
        }
        sa.recycle();
        return fi;
    }

    private boolean parseUsesPermission(Package pkg, Resources res, XmlResourceParser parser, AttributeSet attrs, String[] outError) throws XmlPullParserException, IOException {
        Boolean bool;
        TypedArray sa = res.obtainAttributes(attrs, R.styleable.AndroidManifestUsesPermission);
        String name = sa.getNonResourceString(0);
        int maxSdkVersion = 0;
        TypedValue val = sa.peekValue(1);
        if (val != null && val.type >= 16 && val.type <= 31) {
            maxSdkVersion = val.data;
        }
        sa.recycle();
        if ((maxSdkVersion == 0 || maxSdkVersion >= Build.VERSION.RESOURCES_SDK_INT) && name != null) {
            int index = pkg.requestedPermissions.indexOf(name);
            if (index == -1) {
                pkg.requestedPermissions.add(name.intern());
                ArrayList<Boolean> arrayList = pkg.requestedPermissionsRequired;
                if (1 != 0) {
                    bool = Boolean.TRUE;
                } else {
                    bool = Boolean.FALSE;
                }
                arrayList.add(bool);
            } else if (!pkg.requestedPermissionsRequired.get(index).booleanValue()) {
                outError[0] = "conflicting <uses-permission> entries";
                this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                return false;
            }
        }
        XmlUtils.skipCurrentTag(parser);
        return true;
    }

    /* access modifiers changed from: private */
    public static String buildClassName(String pkg, CharSequence clsSeq, String[] outError) {
        if (clsSeq == null || clsSeq.length() <= 0) {
            outError[0] = "Empty class name in package " + pkg;
            return null;
        }
        String cls = clsSeq.toString();
        char c = cls.charAt(0);
        if (c == '.') {
            return (pkg + cls).intern();
        }
        if (cls.indexOf(46) < 0) {
            return (pkg + '.' + cls).intern();
        } else if (c >= 'a' && c <= 'z') {
            return cls.intern();
        } else {
            outError[0] = "Bad class name " + cls + " in package " + pkg;
            return null;
        }
    }

    private static String buildCompoundName(String pkg, CharSequence procSeq, String type, String[] outError) {
        String proc = procSeq.toString();
        char c = proc.charAt(0);
        if (pkg == null || c != ':') {
            String nameError = validateName(proc, true);
            if (nameError == null || "system".equals(proc)) {
                return proc.intern();
            }
            outError[0] = "Invalid " + type + " name " + proc + " in package " + pkg + ": " + nameError;
            return null;
        } else if (proc.length() < 2) {
            outError[0] = "Bad " + type + " name " + proc + " in package " + pkg + ": must be at least two characters";
            return null;
        } else {
            String nameError2 = validateName(proc.substring(1), false);
            if (nameError2 == null) {
                return (pkg + proc).intern();
            }
            outError[0] = "Invalid " + type + " name " + proc + " in package " + pkg + ": " + nameError2;
            return null;
        }
    }

    /* access modifiers changed from: private */
    public static String buildProcessName(String pkg, String defProc, CharSequence procSeq, int flags, String[] separateProcesses, String[] outError) {
        if (!((flags & 8) == 0 || "system".equals(procSeq))) {
            return defProc != null ? defProc : pkg;
        }
        if (separateProcesses != null) {
            for (int i = separateProcesses.length - 1; i >= 0; i--) {
                String sp = separateProcesses[i];
                if (sp.equals(pkg) || sp.equals(defProc) || sp.equals(procSeq)) {
                    return pkg;
                }
            }
        }
        return (procSeq == null || procSeq.length() <= 0) ? defProc : buildCompoundName(pkg, procSeq, "process", outError);
    }

    private static String buildTaskAffinityName(String pkg, String defProc, CharSequence procSeq, String[] outError) {
        if (procSeq == null) {
            return defProc;
        }
        if (procSeq.length() <= 0) {
            return null;
        }
        return buildCompoundName(pkg, procSeq, "taskAffinity", outError);
    }

    private boolean parseKeySets(Package owner, Resources res, XmlPullParser parser, AttributeSet attrs, String[] outError) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        int currentKeySetDepth = -1;
        String currentKeySet = null;
        ArrayMap<String, PublicKey> publicKeys = new ArrayMap<>();
        ArraySet<String> upgradeKeySets = new ArraySet<>();
        ArrayMap<String, ArraySet<String>> definedKeySets = new ArrayMap<>();
        ArraySet<String> improperKeySets = new ArraySet<>();
        while (true) {
            int type = parser.next();
            if (type != 1 && (type != 3 || parser.getDepth() > outerDepth)) {
                if (type != 3) {
                    String tagName = parser.getName();
                    if (tagName.equals("key-set")) {
                        if (currentKeySet != null) {
                            Slog.w(TAG, "Improperly nested 'key-set' tag at " + parser.getPositionDescription());
                            return false;
                        }
                        TypedArray sa = res.obtainAttributes(attrs, R.styleable.AndroidManifestKeySet);
                        String keysetName = sa.getNonResourceString(0);
                        definedKeySets.put(keysetName, new ArraySet<>());
                        currentKeySet = keysetName;
                        currentKeySetDepth = parser.getDepth();
                        sa.recycle();
                    } else if (tagName.equals("public-key")) {
                        if (currentKeySet == null) {
                            Slog.w(TAG, "Improperly nested 'public-key' tag at " + parser.getPositionDescription());
                            return false;
                        }
                        TypedArray sa2 = res.obtainAttributes(attrs, R.styleable.AndroidManifestPublicKey);
                        String publicKeyName = sa2.getNonResourceString(0);
                        String encodedKey = sa2.getNonResourceString(1);
                        if (encodedKey == null && publicKeys.get(publicKeyName) == null) {
                            Slog.w(TAG, "'public-key' " + publicKeyName + " must define a public-key value" + " on first use at " + parser.getPositionDescription());
                            sa2.recycle();
                            return false;
                        }
                        if (encodedKey != null) {
                            PublicKey currentKey = parsePublicKey(encodedKey);
                            if (currentKey == null) {
                                Slog.w(TAG, "No recognized valid key in 'public-key' tag at " + parser.getPositionDescription() + " key-set " + currentKeySet + " will not be added to the package's defined key-sets.");
                                sa2.recycle();
                                improperKeySets.add(currentKeySet);
                                XmlUtils.skipCurrentTag(parser);
                            } else if (publicKeys.get(publicKeyName) == null || publicKeys.get(publicKeyName).equals(currentKey)) {
                                publicKeys.put(publicKeyName, currentKey);
                            } else {
                                Slog.w(TAG, "Value of 'public-key' " + publicKeyName + " conflicts with previously defined value at " + parser.getPositionDescription());
                                sa2.recycle();
                                return false;
                            }
                        }
                        definedKeySets.get(currentKeySet).add(publicKeyName);
                        sa2.recycle();
                        XmlUtils.skipCurrentTag(parser);
                    } else if (tagName.equals("upgrade-key-set")) {
                        TypedArray sa3 = res.obtainAttributes(attrs, R.styleable.AndroidManifestUpgradeKeySet);
                        upgradeKeySets.add(sa3.getNonResourceString(0));
                        sa3.recycle();
                        XmlUtils.skipCurrentTag(parser);
                    } else {
                        Slog.w(TAG, "Unknown element under <key-sets>: " + parser.getName() + " at " + this.mArchiveSourcePath + " " + parser.getPositionDescription());
                        XmlUtils.skipCurrentTag(parser);
                    }
                } else if (parser.getDepth() == currentKeySetDepth) {
                    currentKeySet = null;
                    currentKeySetDepth = -1;
                }
            }
        }
        if (publicKeys.keySet().removeAll(definedKeySets.keySet())) {
            Slog.w(TAG, "Package" + owner.packageName + " AndroidManifext.xml " + "'key-set' and 'public-key' names must be distinct.");
            return false;
        }
        owner.mKeySetMapping = new ArrayMap<>();
        for (Map.Entry<String, ArraySet<String>> e : definedKeySets.entrySet()) {
            String keySetName = e.getKey();
            if (e.getValue().size() == 0) {
                Slog.w(TAG, "Package" + owner.packageName + " AndroidManifext.xml " + "'key-set' " + keySetName + " has no valid associated 'public-key'." + " Not including in package's defined key-sets.");
            } else if (improperKeySets.contains(keySetName)) {
                Slog.w(TAG, "Package" + owner.packageName + " AndroidManifext.xml " + "'key-set' " + keySetName + " contained improper 'public-key'" + " tags. Not including in package's defined key-sets.");
            } else {
                owner.mKeySetMapping.put(keySetName, new ArraySet<>());
                Iterator i$ = e.getValue().iterator();
                while (i$.hasNext()) {
                    owner.mKeySetMapping.get(keySetName).add(publicKeys.get(i$.next()));
                }
            }
        }
        if (owner.mKeySetMapping.keySet().containsAll(upgradeKeySets)) {
            owner.mUpgradeKeySets = upgradeKeySets;
            return true;
        }
        Slog.w(TAG, "Package" + owner.packageName + " AndroidManifext.xml " + "does not define all 'upgrade-key-set's .");
        return false;
    }

    private PermissionGroup parsePermissionGroup(Package owner, int flags, Resources res, XmlPullParser parser, AttributeSet attrs, String[] outError) throws XmlPullParserException, IOException {
        PermissionGroup perm = new PermissionGroup(owner);
        TypedArray sa = res.obtainAttributes(attrs, R.styleable.AndroidManifestPermissionGroup);
        if (!parsePackageItemInfo(owner, perm.info, outError, "<permission-group>", sa, 2, 0, 1, 5, 7)) {
            sa.recycle();
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            return null;
        }
        perm.info.descriptionRes = sa.getResourceId(4, 0);
        perm.info.flags = sa.getInt(6, 0);
        perm.info.priority = sa.getInt(3, 0);
        if (perm.info.priority > 0 && (flags & 1) == 0) {
            perm.info.priority = 0;
        }
        sa.recycle();
        if (!parseAllMetaData(res, parser, attrs, "<permission-group>", perm, outError)) {
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            return null;
        }
        owner.permissionGroups.add(perm);
        return perm;
    }

    private Permission parsePermission(Package owner, Resources res, XmlPullParser parser, AttributeSet attrs, String[] outError) throws XmlPullParserException, IOException {
        Permission perm = new Permission(owner);
        TypedArray sa = res.obtainAttributes(attrs, R.styleable.AndroidManifestPermission);
        if (!parsePackageItemInfo(owner, perm.info, outError, "<permission>", sa, 2, 0, 1, 6, 8)) {
            sa.recycle();
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            return null;
        }
        perm.info.group = sa.getNonResourceString(4);
        if (perm.info.group != null) {
            perm.info.group = perm.info.group.intern();
        }
        perm.info.descriptionRes = sa.getResourceId(5, 0);
        perm.info.protectionLevel = sa.getInt(3, 0);
        perm.info.flags = sa.getInt(7, 0);
        sa.recycle();
        if (perm.info.protectionLevel == -1) {
            outError[0] = "<permission> does not specify protectionLevel";
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            return null;
        }
        perm.info.protectionLevel = PermissionInfo.fixProtectionLevel(perm.info.protectionLevel);
        if ((perm.info.protectionLevel & 240) != 0 && (perm.info.protectionLevel & 15) != 2) {
            outError[0] = "<permission>  protectionLevel specifies a flag but is not based on signature type";
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            return null;
        } else if (!parseAllMetaData(res, parser, attrs, "<permission>", perm, outError)) {
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            return null;
        } else {
            owner.permissions.add(perm);
            return perm;
        }
    }

    private Permission parsePermissionTree(Package owner, Resources res, XmlPullParser parser, AttributeSet attrs, String[] outError) throws XmlPullParserException, IOException {
        Permission perm = new Permission(owner);
        TypedArray sa = res.obtainAttributes(attrs, R.styleable.AndroidManifestPermissionTree);
        if (!parsePackageItemInfo(owner, perm.info, outError, "<permission-tree>", sa, 2, 0, 1, 3, 4)) {
            sa.recycle();
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            return null;
        }
        sa.recycle();
        int index = perm.info.name.indexOf(46);
        if (index > 0) {
            index = perm.info.name.indexOf(46, index + 1);
        }
        if (index < 0) {
            outError[0] = "<permission-tree> name has less than three segments: " + perm.info.name;
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            return null;
        }
        perm.info.descriptionRes = 0;
        perm.info.protectionLevel = 0;
        perm.tree = true;
        if (!parseAllMetaData(res, parser, attrs, "<permission-tree>", perm, outError)) {
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            return null;
        }
        owner.permissions.add(perm);
        return perm;
    }

    private Instrumentation parseInstrumentation(Package owner, Resources res, XmlPullParser parser, AttributeSet attrs, String[] outError) throws XmlPullParserException, IOException {
        TypedArray sa = res.obtainAttributes(attrs, R.styleable.AndroidManifestInstrumentation);
        if (this.mParseInstrumentationArgs == null) {
            this.mParseInstrumentationArgs = new ParsePackageItemArgs(owner, outError, 2, 0, 1, 6, 7);
            this.mParseInstrumentationArgs.tag = "<instrumentation>";
        }
        this.mParseInstrumentationArgs.sa = sa;
        Instrumentation a = new Instrumentation(this.mParseInstrumentationArgs, new InstrumentationInfo());
        if (outError[0] != null) {
            sa.recycle();
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            return null;
        }
        String str = sa.getNonResourceString(3);
        a.info.targetPackage = str != null ? str.intern() : null;
        a.info.handleProfiling = sa.getBoolean(4, false);
        a.info.functionalTest = sa.getBoolean(5, false);
        sa.recycle();
        if (a.info.targetPackage == null) {
            outError[0] = "<instrumentation> does not specify targetPackage";
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            return null;
        } else if (!parseAllMetaData(res, parser, attrs, "<instrumentation>", a, outError)) {
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            return null;
        } else {
            owner.instrumentation.add(a);
            return a;
        }
    }

    private boolean parseBaseApplication(Package owner, Resources res, XmlPullParser parser, AttributeSet attrs, int flags, String[] outError) throws XmlPullParserException, IOException {
        String str;
        CharSequence pname;
        ApplicationInfo ai = owner.applicationInfo;
        String pkgName = owner.applicationInfo.packageName;
        TypedArray sa = res.obtainAttributes(attrs, R.styleable.AndroidManifestApplication);
        String name = sa.getNonConfigurationString(3, 0);
        if (name != null) {
            ai.className = buildClassName(pkgName, name, outError);
            if (ai.className == null) {
                sa.recycle();
                this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                return false;
            }
        }
        String manageSpaceActivity = sa.getNonConfigurationString(4, 1024);
        if (manageSpaceActivity != null) {
            ai.manageSpaceActivityName = buildClassName(pkgName, manageSpaceActivity, outError);
        }
        if (sa.getBoolean(17, true)) {
            ai.flags |= 32768;
            String backupAgent = sa.getNonConfigurationString(16, 1024);
            if (backupAgent != null) {
                ai.backupAgentName = buildClassName(pkgName, backupAgent, outError);
                if (sa.getBoolean(18, true)) {
                    ai.flags |= 65536;
                }
                if (sa.getBoolean(21, false)) {
                    ai.flags |= 131072;
                }
                if (sa.getBoolean(32, false)) {
                    ai.flags |= 67108864;
                }
            }
        }
        TypedValue v = sa.peekValue(1);
        if (v != null) {
            int i = v.resourceId;
            ai.labelRes = i;
            if (i == 0) {
                ai.nonLocalizedLabel = v.coerceToString();
            }
        }
        ai.icon = sa.getResourceId(2, 0);
        ai.logo = sa.getResourceId(22, 0);
        ai.banner = sa.getResourceId(30, 0);
        ai.theme = sa.getResourceId(0, 0);
        ai.descriptionRes = sa.getResourceId(13, 0);
        if ((flags & 1) != 0 && sa.getBoolean(8, false)) {
            ai.flags |= 8;
        }
        if (sa.getBoolean(27, false)) {
            owner.mRequiredForAllUsers = true;
        }
        String restrictedAccountType = sa.getString(28);
        if (restrictedAccountType != null && restrictedAccountType.length() > 0) {
            owner.mRestrictedAccountType = restrictedAccountType;
        }
        String requiredAccountType = sa.getString(29);
        if (requiredAccountType != null && requiredAccountType.length() > 0) {
            owner.mRequiredAccountType = requiredAccountType;
        }
        if (sa.getBoolean(10, false)) {
            ai.flags |= 2;
        }
        if (sa.getBoolean(20, false)) {
            ai.flags |= 16384;
        }
        owner.baseHardwareAccelerated = sa.getBoolean(23, owner.applicationInfo.targetSdkVersion >= 14);
        if (sa.getBoolean(7, true)) {
            ai.flags |= 4;
        }
        if (sa.getBoolean(14, false)) {
            ai.flags |= 32;
        }
        if (sa.getBoolean(5, true)) {
            ai.flags |= 64;
        }
        if (sa.getBoolean(15, false)) {
            ai.flags |= 256;
        }
        if (sa.getBoolean(24, false)) {
            ai.flags |= 1048576;
        }
        if (sa.getBoolean(26, false)) {
            ai.flags |= 4194304;
        }
        if (sa.getBoolean(33, false)) {
            ai.flags |= Integer.MIN_VALUE;
        }
        String str2 = sa.getNonConfigurationString(6, 0);
        ai.permission = (str2 == null || str2.length() <= 0) ? null : str2.intern();
        if (owner.applicationInfo.targetSdkVersion >= 8) {
            str = sa.getNonConfigurationString(12, 1024);
        } else {
            str = sa.getNonResourceString(12);
        }
        ai.taskAffinity = buildTaskAffinityName(ai.packageName, ai.packageName, str, outError);
        if (outError[0] == null) {
            if (owner.applicationInfo.targetSdkVersion >= 8) {
                pname = sa.getNonConfigurationString(11, 1024);
            } else {
                pname = sa.getNonResourceString(11);
            }
            ai.processName = buildProcessName(ai.packageName, null, pname, flags, this.mSeparateProcesses, outError);
            ai.enabled = sa.getBoolean(9, true);
            if (sa.getBoolean(31, false)) {
                ai.flags |= 33554432;
            }
        }
        ai.uiOptions = sa.getInt(25, 0);
        sa.recycle();
        if (outError[0] != null) {
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            return false;
        }
        int innerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1 || (type == 3 && parser.getDepth() <= innerDepth)) {
                return true;
            }
            if (!(type == 3 || type == 4)) {
                String tagName = parser.getName();
                if (tagName.equals(Context.ACTIVITY_SERVICE)) {
                    Activity a = parseActivity(owner, res, parser, attrs, flags, outError, false, owner.baseHardwareAccelerated);
                    if (a == null) {
                        this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                        return false;
                    }
                    owner.activities.add(a);
                } else if (tagName.equals("receiver")) {
                    Activity a2 = parseActivity(owner, res, parser, attrs, flags, outError, true, false);
                    if (a2 == null) {
                        this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                        return false;
                    }
                    owner.receivers.add(a2);
                } else if (tagName.equals(Notification.CATEGORY_SERVICE)) {
                    Service s = parseService(owner, res, parser, attrs, flags, outError);
                    if (s == null) {
                        this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                        return false;
                    }
                    owner.services.add(s);
                } else if (tagName.equals("provider")) {
                    Provider p = parseProvider(owner, res, parser, attrs, flags, outError);
                    if (p == null) {
                        this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                        return false;
                    }
                    owner.providers.add(p);
                } else if (tagName.equals("activity-alias")) {
                    Activity a3 = parseActivityAlias(owner, res, parser, attrs, flags, outError);
                    if (a3 == null) {
                        this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                        return false;
                    }
                    owner.activities.add(a3);
                } else if (parser.getName().equals("meta-data")) {
                    Bundle parseMetaData = parseMetaData(res, parser, attrs, owner.mAppMetaData, outError);
                    owner.mAppMetaData = parseMetaData;
                    if (parseMetaData == null) {
                        this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                        return false;
                    }
                } else if (tagName.equals("library")) {
                    TypedArray sa2 = res.obtainAttributes(attrs, R.styleable.AndroidManifestLibrary);
                    String lname = sa2.getNonResourceString(0);
                    sa2.recycle();
                    if (lname != null) {
                        String lname2 = lname.intern();
                        if (!ArrayUtils.contains(owner.libraryNames, lname2)) {
                            owner.libraryNames = ArrayUtils.add(owner.libraryNames, lname2);
                        }
                    }
                    XmlUtils.skipCurrentTag(parser);
                } else if (tagName.equals("uses-library")) {
                    TypedArray sa3 = res.obtainAttributes(attrs, R.styleable.AndroidManifestUsesLibrary);
                    String lname3 = sa3.getNonResourceString(0);
                    boolean req = sa3.getBoolean(1, true);
                    sa3.recycle();
                    if (lname3 != null) {
                        String lname4 = lname3.intern();
                        if (req) {
                            owner.usesLibraries = ArrayUtils.add(owner.usesLibraries, lname4);
                        } else {
                            owner.usesOptionalLibraries = ArrayUtils.add(owner.usesOptionalLibraries, lname4);
                        }
                    }
                    XmlUtils.skipCurrentTag(parser);
                } else if (tagName.equals("uses-package")) {
                    XmlUtils.skipCurrentTag(parser);
                } else {
                    Slog.w(TAG, "Unknown element under <application>: " + tagName + " at " + this.mArchiveSourcePath + " " + parser.getPositionDescription());
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
        return true;
    }

    private boolean parseSplitApplication(Package owner, Resources res, XmlPullParser parser, AttributeSet attrs, int flags, int splitIndex, String[] outError) throws XmlPullParserException, IOException {
        if (res.obtainAttributes(attrs, R.styleable.AndroidManifestApplication).getBoolean(7, true)) {
            int[] iArr = owner.splitFlags;
            iArr[splitIndex] = iArr[splitIndex] | 4;
        }
        int innerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1 || (type == 3 && parser.getDepth() <= innerDepth)) {
                return true;
            }
            if (!(type == 3 || type == 4)) {
                String tagName = parser.getName();
                if (tagName.equals(Context.ACTIVITY_SERVICE)) {
                    Activity a = parseActivity(owner, res, parser, attrs, flags, outError, false, owner.baseHardwareAccelerated);
                    if (a == null) {
                        this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                        return false;
                    }
                    owner.activities.add(a);
                } else if (tagName.equals("receiver")) {
                    Activity a2 = parseActivity(owner, res, parser, attrs, flags, outError, true, false);
                    if (a2 == null) {
                        this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                        return false;
                    }
                    owner.receivers.add(a2);
                } else if (tagName.equals(Notification.CATEGORY_SERVICE)) {
                    Service s = parseService(owner, res, parser, attrs, flags, outError);
                    if (s == null) {
                        this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                        return false;
                    }
                    owner.services.add(s);
                } else if (tagName.equals("provider")) {
                    Provider p = parseProvider(owner, res, parser, attrs, flags, outError);
                    if (p == null) {
                        this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                        return false;
                    }
                    owner.providers.add(p);
                } else if (tagName.equals("activity-alias")) {
                    Activity a3 = parseActivityAlias(owner, res, parser, attrs, flags, outError);
                    if (a3 == null) {
                        this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                        return false;
                    }
                    owner.activities.add(a3);
                } else if (parser.getName().equals("meta-data")) {
                    Bundle parseMetaData = parseMetaData(res, parser, attrs, owner.mAppMetaData, outError);
                    owner.mAppMetaData = parseMetaData;
                    if (parseMetaData == null) {
                        this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                        return false;
                    }
                } else if (tagName.equals("uses-library")) {
                    TypedArray sa = res.obtainAttributes(attrs, R.styleable.AndroidManifestUsesLibrary);
                    String lname = sa.getNonResourceString(0);
                    boolean req = sa.getBoolean(1, true);
                    sa.recycle();
                    if (lname != null) {
                        String lname2 = lname.intern();
                        if (req) {
                            owner.usesLibraries = ArrayUtils.add(owner.usesLibraries, lname2);
                            owner.usesOptionalLibraries = ArrayUtils.remove(owner.usesOptionalLibraries, lname2);
                        } else if (!ArrayUtils.contains(owner.usesLibraries, lname2)) {
                            owner.usesOptionalLibraries = ArrayUtils.add(owner.usesOptionalLibraries, lname2);
                        }
                    }
                    XmlUtils.skipCurrentTag(parser);
                } else if (tagName.equals("uses-package")) {
                    XmlUtils.skipCurrentTag(parser);
                } else {
                    Slog.w(TAG, "Unknown element under <application>: " + tagName + " at " + this.mArchiveSourcePath + " " + parser.getPositionDescription());
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
        return true;
    }

    private boolean parsePackageItemInfo(Package owner, PackageItemInfo outInfo, String[] outError, String tag, TypedArray sa, int nameRes, int labelRes, int iconRes, int logoRes, int bannerRes) {
        String name = sa.getNonConfigurationString(nameRes, 0);
        if (name == null) {
            outError[0] = tag + " does not specify android:name";
            return false;
        }
        outInfo.name = buildClassName(owner.applicationInfo.packageName, name, outError);
        if (outInfo.name == null) {
            return false;
        }
        int iconVal = sa.getResourceId(iconRes, 0);
        if (iconVal != 0) {
            outInfo.icon = iconVal;
            outInfo.nonLocalizedLabel = null;
        }
        int logoVal = sa.getResourceId(logoRes, 0);
        if (logoVal != 0) {
            outInfo.logo = logoVal;
        }
        int bannerVal = sa.getResourceId(bannerRes, 0);
        if (bannerVal != 0) {
            outInfo.banner = bannerVal;
        }
        TypedValue v = sa.peekValue(labelRes);
        if (v != null) {
            int i = v.resourceId;
            outInfo.labelRes = i;
            if (i == 0) {
                outInfo.nonLocalizedLabel = v.coerceToString();
            }
        }
        outInfo.packageName = owner.packageName;
        return true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:146:0x052b  */
    /* JADX WARNING: Removed duplicated region for block: B:166:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private android.content.pm.PackageParser.Activity parseActivity(android.content.pm.PackageParser.Package r23, android.content.res.Resources r24, org.xmlpull.v1.XmlPullParser r25, android.util.AttributeSet r26, int r27, java.lang.String[] r28, boolean r29, boolean r30) throws org.xmlpull.v1.XmlPullParserException, java.io.IOException {
        /*
        // Method dump skipped, instructions count: 1340
        */
        throw new UnsupportedOperationException("Method not decompiled: android.content.pm.PackageParser.parseActivity(android.content.pm.PackageParser$Package, android.content.res.Resources, org.xmlpull.v1.XmlPullParser, android.util.AttributeSet, int, java.lang.String[], boolean, boolean):android.content.pm.PackageParser$Activity");
    }

    /* JADX WARNING: Removed duplicated region for block: B:70:0x0304  */
    /* JADX WARNING: Removed duplicated region for block: B:92:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private android.content.pm.PackageParser.Activity parseActivityAlias(android.content.pm.PackageParser.Package r29, android.content.res.Resources r30, org.xmlpull.v1.XmlPullParser r31, android.util.AttributeSet r32, int r33, java.lang.String[] r34) throws org.xmlpull.v1.XmlPullParserException, java.io.IOException {
        /*
        // Method dump skipped, instructions count: 789
        */
        throw new UnsupportedOperationException("Method not decompiled: android.content.pm.PackageParser.parseActivityAlias(android.content.pm.PackageParser$Package, android.content.res.Resources, org.xmlpull.v1.XmlPullParser, android.util.AttributeSet, int, java.lang.String[]):android.content.pm.PackageParser$Activity");
    }

    private Provider parseProvider(Package owner, Resources res, XmlPullParser parser, AttributeSet attrs, int flags, String[] outError) throws XmlPullParserException, IOException {
        TypedArray sa = res.obtainAttributes(attrs, R.styleable.AndroidManifestProvider);
        if (this.mParseProviderArgs == null) {
            this.mParseProviderArgs = new ParseComponentArgs(owner, outError, 2, 0, 1, 15, 17, this.mSeparateProcesses, 8, 14, 6);
            this.mParseProviderArgs.tag = "<provider>";
        }
        this.mParseProviderArgs.sa = sa;
        this.mParseProviderArgs.flags = flags;
        Provider p = new Provider(this.mParseProviderArgs, new ProviderInfo());
        if (outError[0] != null) {
            sa.recycle();
            return null;
        }
        boolean providerExportedDefault = false;
        if (owner.applicationInfo.targetSdkVersion < 17) {
            providerExportedDefault = true;
        }
        p.info.exported = sa.getBoolean(7, providerExportedDefault);
        String cpname = sa.getNonConfigurationString(10, 0);
        p.info.isSyncable = sa.getBoolean(11, false);
        String permission = sa.getNonConfigurationString(3, 0);
        String str = sa.getNonConfigurationString(4, 0);
        if (str == null) {
            str = permission;
        }
        if (str == null) {
            p.info.readPermission = owner.applicationInfo.permission;
        } else {
            p.info.readPermission = str.length() > 0 ? str.toString().intern() : null;
        }
        String str2 = sa.getNonConfigurationString(5, 0);
        if (str2 == null) {
            str2 = permission;
        }
        if (str2 == null) {
            p.info.writePermission = owner.applicationInfo.permission;
        } else {
            p.info.writePermission = str2.length() > 0 ? str2.toString().intern() : null;
        }
        p.info.grantUriPermissions = sa.getBoolean(13, false);
        p.info.multiprocess = sa.getBoolean(9, false);
        p.info.initOrder = sa.getInt(12, 0);
        p.info.flags = 0;
        if (sa.getBoolean(16, false)) {
            p.info.flags |= 1073741824;
            if (p.info.exported && (flags & 128) == 0) {
                Slog.w(TAG, "Provider exported request ignored due to singleUser: " + p.className + " at " + this.mArchiveSourcePath + " " + parser.getPositionDescription());
                p.info.exported = false;
            }
        }
        sa.recycle();
        if ((owner.applicationInfo.flags & 268435456) != 0 && p.info.processName == owner.packageName) {
            outError[0] = "Heavy-weight applications can not have providers in main process";
            return null;
        } else if (cpname == null) {
            outError[0] = "<provider> does not include authorities attribute";
            return null;
        } else {
            p.info.authority = cpname.intern();
            if (!parseProviderTags(res, parser, attrs, p, outError)) {
                return null;
            }
            return p;
        }
    }

    private boolean parseProviderTags(Resources res, XmlPullParser parser, AttributeSet attrs, Provider outInfo, String[] outError) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                return true;
            }
            if (!(type == 3 || type == 4)) {
                if (parser.getName().equals("intent-filter")) {
                    ProviderIntentInfo intent = new ProviderIntentInfo(outInfo);
                    if (!parseIntent(res, parser, attrs, true, intent, outError)) {
                        return false;
                    }
                    outInfo.intents.add(intent);
                } else if (parser.getName().equals("meta-data")) {
                    Bundle parseMetaData = parseMetaData(res, parser, attrs, outInfo.metaData, outError);
                    outInfo.metaData = parseMetaData;
                    if (parseMetaData == null) {
                        return false;
                    }
                } else if (parser.getName().equals("grant-uri-permission")) {
                    TypedArray sa = res.obtainAttributes(attrs, R.styleable.AndroidManifestGrantUriPermission);
                    PatternMatcher pa = null;
                    String str = sa.getNonConfigurationString(0, 0);
                    if (str != null) {
                        pa = new PatternMatcher(str, 0);
                    }
                    String str2 = sa.getNonConfigurationString(1, 0);
                    if (str2 != null) {
                        pa = new PatternMatcher(str2, 1);
                    }
                    String str3 = sa.getNonConfigurationString(2, 0);
                    if (str3 != null) {
                        pa = new PatternMatcher(str3, 2);
                    }
                    sa.recycle();
                    if (pa != null) {
                        if (outInfo.info.uriPermissionPatterns == null) {
                            outInfo.info.uriPermissionPatterns = new PatternMatcher[1];
                            outInfo.info.uriPermissionPatterns[0] = pa;
                        } else {
                            int N = outInfo.info.uriPermissionPatterns.length;
                            PatternMatcher[] newp = new PatternMatcher[(N + 1)];
                            System.arraycopy(outInfo.info.uriPermissionPatterns, 0, newp, 0, N);
                            newp[N] = pa;
                            outInfo.info.uriPermissionPatterns = newp;
                        }
                        outInfo.info.grantUriPermissions = true;
                        XmlUtils.skipCurrentTag(parser);
                    } else {
                        Slog.w(TAG, "Unknown element under <path-permission>: " + parser.getName() + " at " + this.mArchiveSourcePath + " " + parser.getPositionDescription());
                        XmlUtils.skipCurrentTag(parser);
                    }
                } else if (parser.getName().equals("path-permission")) {
                    TypedArray sa2 = res.obtainAttributes(attrs, R.styleable.AndroidManifestPathPermission);
                    PathPermission pa2 = null;
                    String permission = sa2.getNonConfigurationString(0, 0);
                    String readPermission = sa2.getNonConfigurationString(1, 0);
                    if (readPermission == null) {
                        readPermission = permission;
                    }
                    String writePermission = sa2.getNonConfigurationString(2, 0);
                    if (writePermission == null) {
                        writePermission = permission;
                    }
                    boolean havePerm = false;
                    if (readPermission != null) {
                        readPermission = readPermission.intern();
                        havePerm = true;
                    }
                    if (writePermission != null) {
                        writePermission = writePermission.intern();
                        havePerm = true;
                    }
                    if (!havePerm) {
                        Slog.w(TAG, "No readPermission or writePermssion for <path-permission>: " + parser.getName() + " at " + this.mArchiveSourcePath + " " + parser.getPositionDescription());
                        XmlUtils.skipCurrentTag(parser);
                    } else {
                        String path = sa2.getNonConfigurationString(3, 0);
                        if (path != null) {
                            pa2 = new PathPermission(path, 0, readPermission, writePermission);
                        }
                        String path2 = sa2.getNonConfigurationString(4, 0);
                        if (path2 != null) {
                            pa2 = new PathPermission(path2, 1, readPermission, writePermission);
                        }
                        String path3 = sa2.getNonConfigurationString(5, 0);
                        if (path3 != null) {
                            pa2 = new PathPermission(path3, 2, readPermission, writePermission);
                        }
                        sa2.recycle();
                        if (pa2 != null) {
                            if (outInfo.info.pathPermissions == null) {
                                outInfo.info.pathPermissions = new PathPermission[1];
                                outInfo.info.pathPermissions[0] = pa2;
                            } else {
                                int N2 = outInfo.info.pathPermissions.length;
                                PathPermission[] newp2 = new PathPermission[(N2 + 1)];
                                System.arraycopy(outInfo.info.pathPermissions, 0, newp2, 0, N2);
                                newp2[N2] = pa2;
                                outInfo.info.pathPermissions = newp2;
                            }
                            XmlUtils.skipCurrentTag(parser);
                        } else {
                            Slog.w(TAG, "No path, pathPrefix, or pathPattern for <path-permission>: " + parser.getName() + " at " + this.mArchiveSourcePath + " " + parser.getPositionDescription());
                            XmlUtils.skipCurrentTag(parser);
                        }
                    }
                } else {
                    Slog.w(TAG, "Unknown element under <provider>: " + parser.getName() + " at " + this.mArchiveSourcePath + " " + parser.getPositionDescription());
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
        return true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:59:0x01f2  */
    /* JADX WARNING: Removed duplicated region for block: B:76:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private android.content.pm.PackageParser.Service parseService(android.content.pm.PackageParser.Package r21, android.content.res.Resources r22, org.xmlpull.v1.XmlPullParser r23, android.util.AttributeSet r24, int r25, java.lang.String[] r26) throws org.xmlpull.v1.XmlPullParserException, java.io.IOException {
        /*
        // Method dump skipped, instructions count: 515
        */
        throw new UnsupportedOperationException("Method not decompiled: android.content.pm.PackageParser.parseService(android.content.pm.PackageParser$Package, android.content.res.Resources, org.xmlpull.v1.XmlPullParser, android.util.AttributeSet, int, java.lang.String[]):android.content.pm.PackageParser$Service");
    }

    private boolean parseAllMetaData(Resources res, XmlPullParser parser, AttributeSet attrs, String tag, Component outInfo, String[] outError) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                return true;
            }
            if (!(type == 3 || type == 4)) {
                if (parser.getName().equals("meta-data")) {
                    Bundle parseMetaData = parseMetaData(res, parser, attrs, outInfo.metaData, outError);
                    outInfo.metaData = parseMetaData;
                    if (parseMetaData == null) {
                        return false;
                    }
                } else {
                    Slog.w(TAG, "Unknown element under " + tag + ": " + parser.getName() + " at " + this.mArchiveSourcePath + " " + parser.getPositionDescription());
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
        return true;
    }

    private Bundle parseMetaData(Resources res, XmlPullParser parser, AttributeSet attrs, Bundle data, String[] outError) throws XmlPullParserException, IOException {
        String str = null;
        TypedArray sa = res.obtainAttributes(attrs, R.styleable.AndroidManifestMetaData);
        if (data == null) {
            data = new Bundle();
        }
        String name = sa.getNonConfigurationString(0, 0);
        if (name == null) {
            outError[0] = "<meta-data> requires an android:name attribute";
            sa.recycle();
            return null;
        }
        String name2 = name.intern();
        TypedValue v = sa.peekValue(2);
        if (v == null || v.resourceId == 0) {
            TypedValue v2 = sa.peekValue(1);
            if (v2 == null) {
                outError[0] = "<meta-data> requires an android:value or android:resource attribute";
                data = null;
            } else if (v2.type == 3) {
                CharSequence cs = v2.coerceToString();
                if (cs != null) {
                    str = cs.toString().intern();
                }
                data.putString(name2, str);
            } else if (v2.type == 18) {
                data.putBoolean(name2, v2.data != 0);
            } else if (v2.type >= 16 && v2.type <= 31) {
                data.putInt(name2, v2.data);
            } else if (v2.type == 4) {
                data.putFloat(name2, v2.getFloat());
            } else {
                Slog.w(TAG, "<meta-data> only supports string, integer, float, color, boolean, and resource reference types: " + parser.getName() + " at " + this.mArchiveSourcePath + " " + parser.getPositionDescription());
            }
        } else {
            data.putInt(name2, v.resourceId);
        }
        sa.recycle();
        XmlUtils.skipCurrentTag(parser);
        return data;
    }

    private static VerifierInfo parseVerifier(Resources res, XmlPullParser parser, AttributeSet attrs, int flags) {
        TypedArray sa = res.obtainAttributes(attrs, R.styleable.AndroidManifestPackageVerifier);
        String packageName = sa.getNonResourceString(0);
        String encodedPublicKey = sa.getNonResourceString(1);
        sa.recycle();
        if (packageName == null || packageName.length() == 0) {
            Slog.i(TAG, "verifier package name was null; skipping");
            return null;
        }
        PublicKey publicKey = parsePublicKey(encodedPublicKey);
        if (publicKey != null) {
            return new VerifierInfo(packageName, publicKey);
        }
        Slog.i(TAG, "Unable to parse verifier public key for " + packageName);
        return null;
    }

    public static final PublicKey parsePublicKey(String encodedPublicKey) {
        if (encodedPublicKey == null) {
            Slog.i(TAG, "Could not parse null public key");
            return null;
        }
        try {
            EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.decode(encodedPublicKey, 0));
            try {
                return KeyFactory.getInstance("RSA").generatePublic(keySpec);
            } catch (NoSuchAlgorithmException e) {
                Log.wtf(TAG, "Could not parse public key because RSA isn't included in build");
                return null;
            } catch (InvalidKeySpecException e2) {
                try {
                    return KeyFactory.getInstance("DSA").generatePublic(keySpec);
                } catch (NoSuchAlgorithmException e3) {
                    Log.wtf(TAG, "Could not parse public key because DSA isn't included in build");
                    return null;
                } catch (InvalidKeySpecException e4) {
                    return null;
                }
            }
        } catch (IllegalArgumentException e5) {
            Slog.i(TAG, "Could not parse verifier public key; invalid Base64");
            return null;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:88:?, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:92:?, code lost:
        return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean parseIntent(android.content.res.Resources r17, org.xmlpull.v1.XmlPullParser r18, android.util.AttributeSet r19, boolean r20, android.content.pm.PackageParser.IntentInfo r21, java.lang.String[] r22) throws org.xmlpull.v1.XmlPullParserException, java.io.IOException {
        /*
        // Method dump skipped, instructions count: 458
        */
        throw new UnsupportedOperationException("Method not decompiled: android.content.pm.PackageParser.parseIntent(android.content.res.Resources, org.xmlpull.v1.XmlPullParser, android.util.AttributeSet, boolean, android.content.pm.PackageParser$IntentInfo, java.lang.String[]):boolean");
    }

    public static final class Package {
        public final ArrayList<Activity> activities = new ArrayList<>(0);
        public final ApplicationInfo applicationInfo = new ApplicationInfo();
        public String baseCodePath;
        public boolean baseHardwareAccelerated;
        public String codePath;
        public ArrayList<ConfigurationInfo> configPreferences = null;
        public boolean coreApp;
        public String cpuAbiOverride;
        public ArrayList<FeatureGroupInfo> featureGroups = null;
        public int installLocation;
        public final ArrayList<Instrumentation> instrumentation = new ArrayList<>(0);
        public ArrayList<String> libraryNames = null;
        public ArrayList<String> mAdoptPermissions = null;
        public Bundle mAppMetaData = null;
        public Certificate[][] mCertificates;
        public final ArraySet<String> mDexOptPerformed = new ArraySet<>(4);
        public Object mExtras;
        public ArrayMap<String, ArraySet<PublicKey>> mKeySetMapping;
        public long mLastPackageUsageTimeInMills;
        public boolean mOperationPending;
        public ArrayList<String> mOriginalPackages = null;
        public int mOverlayPriority;
        public String mOverlayTarget;
        public int mPreferredOrder = 0;
        public String mRealPackage = null;
        public String mRequiredAccountType;
        public boolean mRequiredForAllUsers;
        public String mRestrictedAccountType;
        public String mSharedUserId;
        public int mSharedUserLabel;
        public Signature[] mSignatures;
        public ArraySet<PublicKey> mSigningKeys;
        public boolean mTrustedOverlay;
        public ArraySet<String> mUpgradeKeySets;
        public int mVersionCode;
        public String mVersionName;
        public ManifestDigest manifestDigest;
        public String packageName;
        public final ArrayList<PermissionGroup> permissionGroups = new ArrayList<>(0);
        public final ArrayList<Permission> permissions = new ArrayList<>(0);
        public ArrayList<ActivityIntentInfo> preferredActivityFilters = null;
        public ArrayList<String> protectedBroadcasts;
        public final ArrayList<Provider> providers = new ArrayList<>(0);
        public final ArrayList<Activity> receivers = new ArrayList<>(0);
        public ArrayList<FeatureInfo> reqFeatures = null;
        public final ArrayList<String> requestedPermissions = new ArrayList<>();
        public final ArrayList<Boolean> requestedPermissionsRequired = new ArrayList<>();
        public final ArrayList<Service> services = new ArrayList<>(0);
        public String[] splitCodePaths;
        public int[] splitFlags;
        public String[] splitNames;
        public ArrayList<String> usesLibraries = null;
        public String[] usesLibraryFiles = null;
        public ArrayList<String> usesOptionalLibraries = null;

        public Package(String packageName2) {
            this.packageName = packageName2;
            this.applicationInfo.packageName = packageName2;
            this.applicationInfo.uid = -1;
        }

        public List<String> getAllCodePaths() {
            ArrayList<String> paths = new ArrayList<>();
            paths.add(this.baseCodePath);
            if (!ArrayUtils.isEmpty(this.splitCodePaths)) {
                Collections.addAll(paths, this.splitCodePaths);
            }
            return paths;
        }

        public List<String> getAllCodePathsExcludingResourceOnly() {
            ArrayList<String> paths = new ArrayList<>();
            if ((this.applicationInfo.flags & 4) != 0) {
                paths.add(this.baseCodePath);
            }
            if (!ArrayUtils.isEmpty(this.splitCodePaths)) {
                for (int i = 0; i < this.splitCodePaths.length; i++) {
                    if ((this.splitFlags[i] & 4) != 0) {
                        paths.add(this.splitCodePaths[i]);
                    }
                }
            }
            return paths;
        }

        public void setPackageName(String newName) {
            this.packageName = newName;
            this.applicationInfo.packageName = newName;
            for (int i = this.permissions.size() - 1; i >= 0; i--) {
                this.permissions.get(i).setPackageName(newName);
            }
            for (int i2 = this.permissionGroups.size() - 1; i2 >= 0; i2--) {
                this.permissionGroups.get(i2).setPackageName(newName);
            }
            for (int i3 = this.activities.size() - 1; i3 >= 0; i3--) {
                this.activities.get(i3).setPackageName(newName);
            }
            for (int i4 = this.receivers.size() - 1; i4 >= 0; i4--) {
                this.receivers.get(i4).setPackageName(newName);
            }
            for (int i5 = this.providers.size() - 1; i5 >= 0; i5--) {
                this.providers.get(i5).setPackageName(newName);
            }
            for (int i6 = this.services.size() - 1; i6 >= 0; i6--) {
                this.services.get(i6).setPackageName(newName);
            }
            for (int i7 = this.instrumentation.size() - 1; i7 >= 0; i7--) {
                this.instrumentation.get(i7).setPackageName(newName);
            }
        }

        public boolean hasComponentClassName(String name) {
            for (int i = this.activities.size() - 1; i >= 0; i--) {
                if (name.equals(this.activities.get(i).className)) {
                    return true;
                }
            }
            for (int i2 = this.receivers.size() - 1; i2 >= 0; i2--) {
                if (name.equals(this.receivers.get(i2).className)) {
                    return true;
                }
            }
            for (int i3 = this.providers.size() - 1; i3 >= 0; i3--) {
                if (name.equals(this.providers.get(i3).className)) {
                    return true;
                }
            }
            for (int i4 = this.services.size() - 1; i4 >= 0; i4--) {
                if (name.equals(this.services.get(i4).className)) {
                    return true;
                }
            }
            for (int i5 = this.instrumentation.size() - 1; i5 >= 0; i5--) {
                if (name.equals(this.instrumentation.get(i5).className)) {
                    return true;
                }
            }
            return false;
        }

        public String toString() {
            return "Package{" + Integer.toHexString(System.identityHashCode(this)) + " " + this.packageName + "}";
        }
    }

    public static class Component<II extends IntentInfo> {
        public final String className;
        ComponentName componentName;
        String componentShortName;
        public final ArrayList<II> intents;
        public Bundle metaData;
        public final Package owner;

        public Component(Package _owner) {
            this.owner = _owner;
            this.intents = null;
            this.className = null;
        }

        public Component(ParsePackageItemArgs args, PackageItemInfo outInfo) {
            this.owner = args.owner;
            this.intents = new ArrayList<>(0);
            String name = args.sa.getNonConfigurationString(args.nameRes, 0);
            if (name == null) {
                this.className = null;
                args.outError[0] = args.tag + " does not specify android:name";
                return;
            }
            outInfo.name = PackageParser.buildClassName(this.owner.applicationInfo.packageName, name, args.outError);
            if (outInfo.name == null) {
                this.className = null;
                args.outError[0] = args.tag + " does not have valid android:name";
                return;
            }
            this.className = outInfo.name;
            int iconVal = args.sa.getResourceId(args.iconRes, 0);
            if (iconVal != 0) {
                outInfo.icon = iconVal;
                outInfo.nonLocalizedLabel = null;
            }
            int logoVal = args.sa.getResourceId(args.logoRes, 0);
            if (logoVal != 0) {
                outInfo.logo = logoVal;
            }
            int bannerVal = args.sa.getResourceId(args.bannerRes, 0);
            if (bannerVal != 0) {
                outInfo.banner = bannerVal;
            }
            TypedValue v = args.sa.peekValue(args.labelRes);
            if (v != null) {
                int i = v.resourceId;
                outInfo.labelRes = i;
                if (i == 0) {
                    outInfo.nonLocalizedLabel = v.coerceToString();
                }
            }
            outInfo.packageName = this.owner.packageName;
        }

        public Component(ParseComponentArgs args, ComponentInfo outInfo) {
            this((ParsePackageItemArgs) args, (PackageItemInfo) outInfo);
            CharSequence pname;
            if (args.outError[0] == null) {
                if (args.processRes != 0) {
                    if (this.owner.applicationInfo.targetSdkVersion >= 8) {
                        pname = args.sa.getNonConfigurationString(args.processRes, 1024);
                    } else {
                        pname = args.sa.getNonResourceString(args.processRes);
                    }
                    outInfo.processName = PackageParser.buildProcessName(this.owner.applicationInfo.packageName, this.owner.applicationInfo.processName, pname, args.flags, args.sepProcesses, args.outError);
                }
                if (args.descriptionRes != 0) {
                    outInfo.descriptionRes = args.sa.getResourceId(args.descriptionRes, 0);
                }
                outInfo.enabled = args.sa.getBoolean(args.enabledRes, true);
            }
        }

        public Component(Component<II> clone) {
            this.owner = clone.owner;
            this.intents = clone.intents;
            this.className = clone.className;
            this.componentName = clone.componentName;
            this.componentShortName = clone.componentShortName;
        }

        public ComponentName getComponentName() {
            if (this.componentName != null) {
                return this.componentName;
            }
            if (this.className != null) {
                this.componentName = new ComponentName(this.owner.applicationInfo.packageName, this.className);
            }
            return this.componentName;
        }

        public void appendComponentShortName(StringBuilder sb) {
            ComponentName.appendShortString(sb, this.owner.applicationInfo.packageName, this.className);
        }

        public void printComponentShortName(PrintWriter pw) {
            ComponentName.printShortString(pw, this.owner.applicationInfo.packageName, this.className);
        }

        public void setPackageName(String packageName) {
            this.componentName = null;
            this.componentShortName = null;
        }
    }

    public static final class Permission extends Component<IntentInfo> {
        public PermissionGroup group;
        public final PermissionInfo info;
        public boolean tree;

        public Permission(Package _owner) {
            super(_owner);
            this.info = new PermissionInfo();
        }

        public Permission(Package _owner, PermissionInfo _info) {
            super(_owner);
            this.info = _info;
        }

        @Override // android.content.pm.PackageParser.Component
        public void setPackageName(String packageName) {
            super.setPackageName(packageName);
            this.info.packageName = packageName;
        }

        public String toString() {
            return "Permission{" + Integer.toHexString(System.identityHashCode(this)) + " " + this.info.name + "}";
        }
    }

    public static final class PermissionGroup extends Component<IntentInfo> {
        public final PermissionGroupInfo info;

        public PermissionGroup(Package _owner) {
            super(_owner);
            this.info = new PermissionGroupInfo();
        }

        public PermissionGroup(Package _owner, PermissionGroupInfo _info) {
            super(_owner);
            this.info = _info;
        }

        @Override // android.content.pm.PackageParser.Component
        public void setPackageName(String packageName) {
            super.setPackageName(packageName);
            this.info.packageName = packageName;
        }

        public String toString() {
            return "PermissionGroup{" + Integer.toHexString(System.identityHashCode(this)) + " " + this.info.name + "}";
        }
    }

    private static boolean copyNeeded(int flags, Package p, PackageUserState state, Bundle metaData, int userId) {
        if (userId != 0) {
            return true;
        }
        if (state.enabled != 0) {
            if (p.applicationInfo.enabled != (state.enabled == 1)) {
                return true;
            }
        }
        if (!state.installed || state.hidden || state.stopped) {
            return true;
        }
        if ((flags & 128) == 0 || (metaData == null && p.mAppMetaData == null)) {
            return ((flags & 1024) == 0 || p.usesLibraryFiles == null) ? false : true;
        }
        return true;
    }

    public static ApplicationInfo generateApplicationInfo(Package p, int flags, PackageUserState state) {
        return generateApplicationInfo(p, flags, state, UserHandle.getCallingUserId());
    }

    private static void updateApplicationInfo(ApplicationInfo ai, int flags, PackageUserState state) {
        boolean z = true;
        if (!sCompatibilityModeEnabled) {
            ai.disableCompatibilityMode();
        }
        if (state.installed) {
            ai.flags |= 8388608;
        } else {
            ai.flags &= -8388609;
        }
        if (state.hidden) {
            ai.flags |= 134217728;
        } else {
            ai.flags &= -134217729;
        }
        if (state.enabled == 1) {
            ai.enabled = true;
        } else if (state.enabled == 4) {
            if ((32768 & flags) == 0) {
                z = false;
            }
            ai.enabled = z;
        } else if (state.enabled == 2 || state.enabled == 3) {
            ai.enabled = false;
        }
        ai.enabledSetting = state.enabled;
    }

    public static ApplicationInfo generateApplicationInfo(Package p, int flags, PackageUserState state, int userId) {
        if (p == null || !checkUseInstalledOrHidden(flags, state)) {
            return null;
        }
        if (copyNeeded(flags, p, state, null, userId) || ((32768 & flags) != 0 && state.enabled == 4)) {
            ApplicationInfo ai = new ApplicationInfo(p.applicationInfo);
            if (userId != 0) {
                ai.uid = UserHandle.getUid(userId, ai.uid);
                ai.dataDir = PackageManager.getDataDirForUser(userId, ai.packageName);
            }
            if ((flags & 128) != 0) {
                ai.metaData = p.mAppMetaData;
            }
            if ((flags & 1024) != 0) {
                ai.sharedLibraryFiles = p.usesLibraryFiles;
            }
            if (state.stopped) {
                ai.flags |= 2097152;
            } else {
                ai.flags &= -2097153;
            }
            updateApplicationInfo(ai, flags, state);
            return ai;
        }
        updateApplicationInfo(p.applicationInfo, flags, state);
        return p.applicationInfo;
    }

    public static ApplicationInfo generateApplicationInfo(ApplicationInfo ai, int flags, PackageUserState state, int userId) {
        ApplicationInfo ai2 = null;
        if (ai != null && checkUseInstalledOrHidden(flags, state)) {
            ai2 = new ApplicationInfo(ai);
            if (userId != 0) {
                ai2.uid = UserHandle.getUid(userId, ai2.uid);
                ai2.dataDir = PackageManager.getDataDirForUser(userId, ai2.packageName);
            }
            if (state.stopped) {
                ai2.flags |= 2097152;
            } else {
                ai2.flags &= -2097153;
            }
            updateApplicationInfo(ai2, flags, state);
        }
        return ai2;
    }

    public static final PermissionInfo generatePermissionInfo(Permission p, int flags) {
        if (p == null) {
            return null;
        }
        if ((flags & 128) == 0) {
            return p.info;
        }
        PermissionInfo pi = new PermissionInfo(p.info);
        pi.metaData = p.metaData;
        return pi;
    }

    public static final PermissionGroupInfo generatePermissionGroupInfo(PermissionGroup pg, int flags) {
        if (pg == null) {
            return null;
        }
        if ((flags & 128) == 0) {
            return pg.info;
        }
        PermissionGroupInfo pgi = new PermissionGroupInfo(pg.info);
        pgi.metaData = pg.metaData;
        return pgi;
    }

    public static final class Activity extends Component<ActivityIntentInfo> {
        public final ActivityInfo info;

        public Activity(ParseComponentArgs args, ActivityInfo _info) {
            super(args, (ComponentInfo) _info);
            this.info = _info;
            this.info.applicationInfo = args.owner.applicationInfo;
        }

        @Override // android.content.pm.PackageParser.Component
        public void setPackageName(String packageName) {
            super.setPackageName(packageName);
            this.info.packageName = packageName;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(128);
            sb.append("Activity{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(' ');
            appendComponentShortName(sb);
            sb.append('}');
            return sb.toString();
        }
    }

    public static final ActivityInfo generateActivityInfo(Activity a, int flags, PackageUserState state, int userId) {
        if (a == null || !checkUseInstalledOrHidden(flags, state)) {
            return null;
        }
        if (!copyNeeded(flags, a.owner, state, a.metaData, userId)) {
            return a.info;
        }
        ActivityInfo ai = new ActivityInfo(a.info);
        ai.metaData = a.metaData;
        ai.applicationInfo = generateApplicationInfo(a.owner, flags, state, userId);
        return ai;
    }

    public static final ActivityInfo generateActivityInfo(ActivityInfo ai, int flags, PackageUserState state, int userId) {
        if (ai == null || !checkUseInstalledOrHidden(flags, state)) {
            return null;
        }
        ActivityInfo ai2 = new ActivityInfo(ai);
        ai2.applicationInfo = generateApplicationInfo(ai2.applicationInfo, flags, state, userId);
        return ai2;
    }

    public static final class Service extends Component<ServiceIntentInfo> {
        public final ServiceInfo info;

        public Service(ParseComponentArgs args, ServiceInfo _info) {
            super(args, (ComponentInfo) _info);
            this.info = _info;
            this.info.applicationInfo = args.owner.applicationInfo;
        }

        @Override // android.content.pm.PackageParser.Component
        public void setPackageName(String packageName) {
            super.setPackageName(packageName);
            this.info.packageName = packageName;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(128);
            sb.append("Service{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(' ');
            appendComponentShortName(sb);
            sb.append('}');
            return sb.toString();
        }
    }

    public static final ServiceInfo generateServiceInfo(Service s, int flags, PackageUserState state, int userId) {
        if (s == null || !checkUseInstalledOrHidden(flags, state)) {
            return null;
        }
        if (!copyNeeded(flags, s.owner, state, s.metaData, userId)) {
            return s.info;
        }
        ServiceInfo si = new ServiceInfo(s.info);
        si.metaData = s.metaData;
        si.applicationInfo = generateApplicationInfo(s.owner, flags, state, userId);
        return si;
    }

    public static final class Provider extends Component<ProviderIntentInfo> {
        public final ProviderInfo info;
        public boolean syncable;

        public Provider(ParseComponentArgs args, ProviderInfo _info) {
            super(args, (ComponentInfo) _info);
            this.info = _info;
            this.info.applicationInfo = args.owner.applicationInfo;
            this.syncable = false;
        }

        public Provider(Provider existingProvider) {
            super(existingProvider);
            this.info = existingProvider.info;
            this.syncable = existingProvider.syncable;
        }

        @Override // android.content.pm.PackageParser.Component
        public void setPackageName(String packageName) {
            super.setPackageName(packageName);
            this.info.packageName = packageName;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(128);
            sb.append("Provider{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(' ');
            appendComponentShortName(sb);
            sb.append('}');
            return sb.toString();
        }
    }

    public static final ProviderInfo generateProviderInfo(Provider p, int flags, PackageUserState state, int userId) {
        if (p == null) {
            return null;
        }
        if (!checkUseInstalledOrHidden(flags, state)) {
            return null;
        }
        if (!copyNeeded(flags, p.owner, state, p.metaData, userId) && ((flags & 2048) != 0 || p.info.uriPermissionPatterns == null)) {
            return p.info;
        }
        ProviderInfo pi = new ProviderInfo(p.info);
        pi.metaData = p.metaData;
        if ((flags & 2048) == 0) {
            pi.uriPermissionPatterns = null;
        }
        pi.applicationInfo = generateApplicationInfo(p.owner, flags, state, userId);
        return pi;
    }

    public static final class Instrumentation extends Component {
        public final InstrumentationInfo info;

        public Instrumentation(ParsePackageItemArgs args, InstrumentationInfo _info) {
            super(args, _info);
            this.info = _info;
        }

        @Override // android.content.pm.PackageParser.Component
        public void setPackageName(String packageName) {
            super.setPackageName(packageName);
            this.info.packageName = packageName;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(128);
            sb.append("Instrumentation{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(' ');
            appendComponentShortName(sb);
            sb.append('}');
            return sb.toString();
        }
    }

    public static final InstrumentationInfo generateInstrumentationInfo(Instrumentation i, int flags) {
        if (i == null) {
            return null;
        }
        if ((flags & 128) == 0) {
            return i.info;
        }
        InstrumentationInfo ii = new InstrumentationInfo(i.info);
        ii.metaData = i.metaData;
        return ii;
    }

    public static final class ActivityIntentInfo extends IntentInfo {
        public final Activity activity;

        public ActivityIntentInfo(Activity _activity) {
            this.activity = _activity;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(128);
            sb.append("ActivityIntentInfo{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(' ');
            this.activity.appendComponentShortName(sb);
            sb.append('}');
            return sb.toString();
        }
    }

    public static final class ServiceIntentInfo extends IntentInfo {
        public final Service service;

        public ServiceIntentInfo(Service _service) {
            this.service = _service;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(128);
            sb.append("ServiceIntentInfo{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(' ');
            this.service.appendComponentShortName(sb);
            sb.append('}');
            return sb.toString();
        }
    }

    public static final class ProviderIntentInfo extends IntentInfo {
        public final Provider provider;

        public ProviderIntentInfo(Provider provider2) {
            this.provider = provider2;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(128);
            sb.append("ProviderIntentInfo{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(' ');
            this.provider.appendComponentShortName(sb);
            sb.append('}');
            return sb.toString();
        }
    }

    public static void setCompatibilityModeEnabled(boolean compatibilityModeEnabled) {
        sCompatibilityModeEnabled = compatibilityModeEnabled;
    }

    public static long readFullyIgnoringContents(InputStream in) throws IOException {
        byte[] buffer = sBuffer.getAndSet(null);
        if (buffer == null) {
            buffer = new byte[4096];
        }
        int count = 0;
        while (true) {
            int n = in.read(buffer, 0, buffer.length);
            if (n != -1) {
                count += n;
            } else {
                sBuffer.set(buffer);
                return (long) count;
            }
        }
    }

    public static void closeQuietly(StrictJarFile jarFile) {
        if (jarFile != null) {
            try {
                jarFile.close();
            } catch (Exception e) {
            }
        }
    }

    public static class PackageParserException extends Exception {
        public final int error;

        public PackageParserException(int error2, String detailMessage) {
            super(detailMessage);
            this.error = error2;
        }

        public PackageParserException(int error2, String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
            this.error = error2;
        }
    }
}

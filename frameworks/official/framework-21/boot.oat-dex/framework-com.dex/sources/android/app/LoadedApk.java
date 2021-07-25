package android.app;

import android.app.IServiceConnection;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.CompatibilityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.os.StrictMode;
import android.os.Trace;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.view.DisplayAdjustments;
import dalvik.system.VMRuntime;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Objects;

public final class LoadedApk {
    static final /* synthetic */ boolean $assertionsDisabled = (!LoadedApk.class.desiredAssertionStatus());
    private static final String TAG = "LoadedApk";
    private final ActivityThread mActivityThread;
    private final String mAppDir;
    private Application mApplication;
    private ApplicationInfo mApplicationInfo;
    private final ClassLoader mBaseClassLoader;
    private ClassLoader mClassLoader;
    int mClientCount = 0;
    private final String mDataDir;
    private final File mDataDirFile;
    private final DisplayAdjustments mDisplayAdjustments = new DisplayAdjustments();
    private final boolean mIncludeCode;
    private final String mLibDir;
    private final String[] mOverlayDirs;
    final String mPackageName;
    private final ArrayMap<Context, ArrayMap<BroadcastReceiver, ReceiverDispatcher>> mReceivers = new ArrayMap<>();
    private final boolean mRegisterPackage;
    private final String mResDir;
    Resources mResources;
    private final boolean mSecurityViolation;
    private final ArrayMap<Context, ArrayMap<ServiceConnection, ServiceDispatcher>> mServices = new ArrayMap<>();
    private final String[] mSharedLibraries;
    private final String[] mSplitAppDirs;
    private final String[] mSplitResDirs;
    private final ArrayMap<Context, ArrayMap<ServiceConnection, ServiceDispatcher>> mUnboundServices = new ArrayMap<>();
    private final ArrayMap<Context, ArrayMap<BroadcastReceiver, ReceiverDispatcher>> mUnregisteredReceivers = new ArrayMap<>();

    /* access modifiers changed from: package-private */
    public Application getApplication() {
        return this.mApplication;
    }

    public LoadedApk(ActivityThread activityThread, ApplicationInfo aInfo, CompatibilityInfo compatInfo, ClassLoader baseLoader, boolean securityViolation, boolean includeCode, boolean registerPackage) {
        int myUid = Process.myUid();
        ApplicationInfo aInfo2 = adjustNativeLibraryPaths(aInfo);
        this.mActivityThread = activityThread;
        this.mApplicationInfo = aInfo2;
        this.mPackageName = aInfo2.packageName;
        this.mAppDir = aInfo2.sourceDir;
        this.mResDir = aInfo2.uid == myUid ? aInfo2.sourceDir : aInfo2.publicSourceDir;
        this.mSplitAppDirs = aInfo2.splitSourceDirs;
        this.mSplitResDirs = aInfo2.uid == myUid ? aInfo2.splitSourceDirs : aInfo2.splitPublicSourceDirs;
        this.mOverlayDirs = aInfo2.resourceDirs;
        if (!UserHandle.isSameUser(aInfo2.uid, myUid) && !Process.isIsolated()) {
            aInfo2.dataDir = PackageManager.getDataDirForUser(UserHandle.getUserId(myUid), this.mPackageName);
        }
        this.mSharedLibraries = aInfo2.sharedLibraryFiles;
        this.mDataDir = aInfo2.dataDir;
        this.mDataDirFile = this.mDataDir != null ? new File(this.mDataDir) : null;
        this.mLibDir = aInfo2.nativeLibraryDir;
        this.mBaseClassLoader = baseLoader;
        this.mSecurityViolation = securityViolation;
        this.mIncludeCode = includeCode;
        this.mRegisterPackage = registerPackage;
        this.mDisplayAdjustments.setCompatibilityInfo(compatInfo);
    }

    private static ApplicationInfo adjustNativeLibraryPaths(ApplicationInfo info) {
        if (info.primaryCpuAbi == null || info.secondaryCpuAbi == null || !VMRuntime.getRuntime().vmInstructionSet().equals(VMRuntime.getInstructionSet(info.secondaryCpuAbi))) {
            return info;
        }
        ApplicationInfo modified = new ApplicationInfo(info);
        modified.nativeLibraryDir = modified.secondaryNativeLibraryDir;
        return modified;
    }

    LoadedApk(ActivityThread activityThread) {
        this.mActivityThread = activityThread;
        this.mApplicationInfo = new ApplicationInfo();
        this.mApplicationInfo.packageName = "android";
        this.mPackageName = "android";
        this.mAppDir = null;
        this.mResDir = null;
        this.mSplitAppDirs = null;
        this.mSplitResDirs = null;
        this.mOverlayDirs = null;
        this.mSharedLibraries = null;
        this.mDataDir = null;
        this.mDataDirFile = null;
        this.mLibDir = null;
        this.mBaseClassLoader = null;
        this.mSecurityViolation = false;
        this.mIncludeCode = true;
        this.mRegisterPackage = false;
        this.mClassLoader = ClassLoader.getSystemClassLoader();
        this.mResources = Resources.getSystem();
    }

    /* access modifiers changed from: package-private */
    public void installSystemApplicationInfo(ApplicationInfo info, ClassLoader classLoader) {
        if ($assertionsDisabled || info.packageName.equals("android")) {
            this.mApplicationInfo = info;
            this.mClassLoader = classLoader;
            return;
        }
        throw new AssertionError();
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public ApplicationInfo getApplicationInfo() {
        return this.mApplicationInfo;
    }

    public boolean isSecurityViolation() {
        return this.mSecurityViolation;
    }

    public CompatibilityInfo getCompatibilityInfo() {
        return this.mDisplayAdjustments.getCompatibilityInfo();
    }

    public void setCompatibilityInfo(CompatibilityInfo compatInfo) {
        this.mDisplayAdjustments.setCompatibilityInfo(compatInfo);
    }

    private static String[] getLibrariesFor(String packageName) {
        try {
            ApplicationInfo ai = ActivityThread.getPackageManager().getApplicationInfo(packageName, 1024, UserHandle.myUserId());
            if (ai == null) {
                return null;
            }
            return ai.sharedLibraryFiles;
        } catch (RemoteException e) {
            throw new AssertionError(e);
        }
    }

    public ClassLoader getClassLoader() {
        ClassLoader classLoader;
        synchronized (this) {
            if (this.mClassLoader != null) {
                classLoader = this.mClassLoader;
            } else {
                if (this.mIncludeCode && !this.mPackageName.equals("android")) {
                    if (!Objects.equals(this.mPackageName, ActivityThread.currentPackageName())) {
                        try {
                            ActivityThread.getPackageManager().performDexOptIfNeeded(this.mPackageName, VMRuntime.getRuntime().vmInstructionSet());
                        } catch (RemoteException e) {
                        }
                    }
                    ArrayList<String> zipPaths = new ArrayList<>();
                    ArrayList<String> libPaths = new ArrayList<>();
                    if (this.mRegisterPackage) {
                        try {
                            ActivityManagerNative.getDefault().addPackageDependency(this.mPackageName);
                        } catch (RemoteException e2) {
                        }
                    }
                    zipPaths.add(this.mAppDir);
                    if (this.mSplitAppDirs != null) {
                        Collections.addAll(zipPaths, this.mSplitAppDirs);
                    }
                    libPaths.add(this.mLibDir);
                    String instrumentationPackageName = this.mActivityThread.mInstrumentationPackageName;
                    String instrumentationAppDir = this.mActivityThread.mInstrumentationAppDir;
                    String[] instrumentationSplitAppDirs = this.mActivityThread.mInstrumentationSplitAppDirs;
                    String instrumentationLibDir = this.mActivityThread.mInstrumentationLibDir;
                    String instrumentedAppDir = this.mActivityThread.mInstrumentedAppDir;
                    String[] instrumentedSplitAppDirs = this.mActivityThread.mInstrumentedSplitAppDirs;
                    String instrumentedLibDir = this.mActivityThread.mInstrumentedLibDir;
                    String[] instrumentationLibs = null;
                    if (this.mAppDir.equals(instrumentationAppDir) || this.mAppDir.equals(instrumentedAppDir)) {
                        zipPaths.clear();
                        zipPaths.add(instrumentationAppDir);
                        if (instrumentationSplitAppDirs != null) {
                            Collections.addAll(zipPaths, instrumentationSplitAppDirs);
                        }
                        zipPaths.add(instrumentedAppDir);
                        if (instrumentedSplitAppDirs != null) {
                            Collections.addAll(zipPaths, instrumentedSplitAppDirs);
                        }
                        libPaths.clear();
                        libPaths.add(instrumentationLibDir);
                        libPaths.add(instrumentedLibDir);
                        if (!instrumentedAppDir.equals(instrumentationAppDir)) {
                            instrumentationLibs = getLibrariesFor(instrumentationPackageName);
                        }
                    }
                    if (this.mSharedLibraries != null) {
                        String[] arr$ = this.mSharedLibraries;
                        for (String lib : arr$) {
                            if (!zipPaths.contains(lib)) {
                                zipPaths.add(0, lib);
                            }
                        }
                    }
                    if (instrumentationLibs != null) {
                        for (String lib2 : instrumentationLibs) {
                            if (!zipPaths.contains(lib2)) {
                                zipPaths.add(0, lib2);
                            }
                        }
                    }
                    String zip = TextUtils.join(File.pathSeparator, zipPaths);
                    String lib3 = TextUtils.join(File.pathSeparator, libPaths);
                    StrictMode.ThreadPolicy oldPolicy = StrictMode.allowThreadDiskReads();
                    this.mClassLoader = ApplicationLoaders.getDefault().getClassLoader(zip, lib3, this.mBaseClassLoader);
                    StrictMode.setThreadPolicy(oldPolicy);
                } else if (this.mBaseClassLoader == null) {
                    this.mClassLoader = ClassLoader.getSystemClassLoader();
                } else {
                    this.mClassLoader = this.mBaseClassLoader;
                }
                classLoader = this.mClassLoader;
            }
        }
        return classLoader;
    }

    private void initializeJavaContextClassLoader() {
        boolean sharedUserIdSet;
        boolean processNameNotDefault;
        boolean sharable;
        try {
            PackageInfo pi = ActivityThread.getPackageManager().getPackageInfo(this.mPackageName, 0, UserHandle.myUserId());
            if (pi == null) {
                throw new IllegalStateException("Unable to get package info for " + this.mPackageName + "; is package not installed?");
            }
            if (pi.sharedUserId != null) {
                sharedUserIdSet = true;
            } else {
                sharedUserIdSet = false;
            }
            if (pi.applicationInfo == null || this.mPackageName.equals(pi.applicationInfo.processName)) {
                processNameNotDefault = false;
            } else {
                processNameNotDefault = true;
            }
            if (sharedUserIdSet || processNameNotDefault) {
                sharable = true;
            } else {
                sharable = false;
            }
            Thread.currentThread().setContextClassLoader(sharable ? new WarningContextClassLoader() : this.mClassLoader);
        } catch (RemoteException e) {
            throw new IllegalStateException("Unable to get package info for " + this.mPackageName + "; is system dying?", e);
        }
    }

    /* access modifiers changed from: private */
    public static class WarningContextClassLoader extends ClassLoader {
        private static boolean warned = false;

        private WarningContextClassLoader() {
        }

        private void warn(String methodName) {
            if (!warned) {
                warned = true;
                Thread.currentThread().setContextClassLoader(getParent());
                Slog.w(ActivityThread.TAG, "ClassLoader." + methodName + ": " + "The class loader returned by " + "Thread.getContextClassLoader() may fail for processes " + "that host multiple applications. You should explicitly " + "specify a context class loader. For example: " + "Thread.setContextClassLoader(getClass().getClassLoader());");
            }
        }

        public URL getResource(String resName) {
            warn("getResource");
            return getParent().getResource(resName);
        }

        @Override // java.lang.ClassLoader
        public Enumeration<URL> getResources(String resName) throws IOException {
            warn("getResources");
            return getParent().getResources(resName);
        }

        public InputStream getResourceAsStream(String resName) {
            warn("getResourceAsStream");
            return getParent().getResourceAsStream(resName);
        }

        @Override // java.lang.ClassLoader
        public Class<?> loadClass(String className) throws ClassNotFoundException {
            warn("loadClass");
            return getParent().loadClass(className);
        }

        public void setClassAssertionStatus(String cname, boolean enable) {
            warn("setClassAssertionStatus");
            getParent().setClassAssertionStatus(cname, enable);
        }

        public void setPackageAssertionStatus(String pname, boolean enable) {
            warn("setPackageAssertionStatus");
            getParent().setPackageAssertionStatus(pname, enable);
        }

        public void setDefaultAssertionStatus(boolean enable) {
            warn("setDefaultAssertionStatus");
            getParent().setDefaultAssertionStatus(enable);
        }

        public void clearAssertionStatus() {
            warn("clearAssertionStatus");
            getParent().clearAssertionStatus();
        }
    }

    public String getAppDir() {
        return this.mAppDir;
    }

    public String getLibDir() {
        return this.mLibDir;
    }

    public String getResDir() {
        return this.mResDir;
    }

    public String[] getSplitAppDirs() {
        return this.mSplitAppDirs;
    }

    public String[] getSplitResDirs() {
        return this.mSplitResDirs;
    }

    public String[] getOverlayDirs() {
        return this.mOverlayDirs;
    }

    public String getDataDir() {
        return this.mDataDir;
    }

    public File getDataDirFile() {
        return this.mDataDirFile;
    }

    public AssetManager getAssets(ActivityThread mainThread) {
        return getResources(mainThread).getAssets();
    }

    public Resources getResources(ActivityThread mainThread) {
        if (this.mResources == null) {
            this.mResources = mainThread.getTopLevelResources(this.mResDir, this.mSplitResDirs, this.mOverlayDirs, this.mApplicationInfo.sharedLibraryFiles, 0, null, this);
        }
        return this.mResources;
    }

    public Application makeApplication(boolean forceDefaultAppClass, Instrumentation instrumentation) {
        if (this.mApplication != null) {
            return this.mApplication;
        }
        Application app = null;
        String appClass = this.mApplicationInfo.className;
        if (forceDefaultAppClass || appClass == null) {
            appClass = "android.app.Application";
        }
        try {
            ClassLoader cl = getClassLoader();
            if (!this.mPackageName.equals("android")) {
                initializeJavaContextClassLoader();
            }
            ContextImpl appContext = ContextImpl.createAppContext(this.mActivityThread, this);
            app = this.mActivityThread.mInstrumentation.newApplication(cl, appClass, appContext);
            appContext.setOuterContext(app);
        } catch (Exception e) {
            if (!this.mActivityThread.mInstrumentation.onException(null, e)) {
                throw new RuntimeException("Unable to instantiate application " + appClass + ": " + e.toString(), e);
            }
        }
        this.mActivityThread.mAllApplications.add(app);
        this.mApplication = app;
        if (instrumentation != null) {
            try {
                instrumentation.callApplicationOnCreate(app);
            } catch (Exception e2) {
                if (!instrumentation.onException(app, e2)) {
                    throw new RuntimeException("Unable to create application " + app.getClass().getName() + ": " + e2.toString(), e2);
                }
            }
        }
        SparseArray<String> packageIdentifiers = getAssets(this.mActivityThread).getAssignedPackageIdentifiers();
        int N = packageIdentifiers.size();
        for (int i = 0; i < N; i++) {
            int id = packageIdentifiers.keyAt(i);
            if (!(id == 1 || id == 127)) {
                rewriteRValues(getClassLoader(), packageIdentifiers.valueAt(i), id);
            }
        }
        return app;
    }

    private void rewriteRValues(ClassLoader cl, String packageName, int id) {
        Throwable cause;
        try {
            Class<?> rClazz = cl.loadClass(packageName + ".R");
            try {
                Method callback = rClazz.getMethod("onResourcesLoaded", Integer.TYPE);
                try {
                    callback.invoke(null, Integer.valueOf(id));
                    return;
                } catch (IllegalAccessException e) {
                    cause = e;
                } catch (InvocationTargetException e2) {
                    cause = e2.getCause();
                }
                throw new RuntimeException("Failed to rewrite resource references for " + packageName, cause);
            } catch (NoSuchMethodException e3) {
            }
        } catch (ClassNotFoundException e4) {
            Log.i(TAG, "No resource references to update in package " + packageName);
        }
    }

    public void removeContextRegistrations(Context context, String who, String what) {
        boolean reportRegistrationLeaks = StrictMode.vmRegistrationLeaksEnabled();
        ArrayMap<BroadcastReceiver, ReceiverDispatcher> rmap = this.mReceivers.remove(context);
        if (rmap != null) {
            for (int i = 0; i < rmap.size(); i++) {
                ReceiverDispatcher rd = rmap.valueAt(i);
                IntentReceiverLeaked leak = new IntentReceiverLeaked(what + " " + who + " has leaked IntentReceiver " + rd.getIntentReceiver() + " that was " + "originally registered here. Are you missing a " + "call to unregisterReceiver()?");
                leak.setStackTrace(rd.getLocation().getStackTrace());
                Slog.e(ActivityThread.TAG, leak.getMessage(), leak);
                if (reportRegistrationLeaks) {
                    StrictMode.onIntentReceiverLeaked(leak);
                }
                try {
                    ActivityManagerNative.getDefault().unregisterReceiver(rd.getIIntentReceiver());
                } catch (RemoteException e) {
                }
            }
        }
        this.mUnregisteredReceivers.remove(context);
        ArrayMap<ServiceConnection, ServiceDispatcher> smap = this.mServices.remove(context);
        if (smap != null) {
            for (int i2 = 0; i2 < smap.size(); i2++) {
                ServiceDispatcher sd = smap.valueAt(i2);
                ServiceConnectionLeaked leak2 = new ServiceConnectionLeaked(what + " " + who + " has leaked ServiceConnection " + sd.getServiceConnection() + " that was originally bound here");
                leak2.setStackTrace(sd.getLocation().getStackTrace());
                Slog.e(ActivityThread.TAG, leak2.getMessage(), leak2);
                if (reportRegistrationLeaks) {
                    StrictMode.onServiceConnectionLeaked(leak2);
                }
                try {
                    ActivityManagerNative.getDefault().unbindService(sd.getIServiceConnection());
                } catch (RemoteException e2) {
                }
                sd.doForget();
            }
        }
        this.mUnboundServices.remove(context);
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:0x001f A[SYNTHETIC, Splitter:B:10:0x001f] */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0044  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public android.content.IIntentReceiver getReceiverDispatcher(android.content.BroadcastReceiver r12, android.content.Context r13, android.os.Handler r14, android.app.Instrumentation r15, boolean r16) {
        /*
            r11 = this;
            android.util.ArrayMap<android.content.Context, android.util.ArrayMap<android.content.BroadcastReceiver, android.app.LoadedApk$ReceiverDispatcher>> r10 = r11.mReceivers
            monitor-enter(r10)
            r1 = 0
            r7 = 0
            if (r16 == 0) goto L_0x0058
            android.util.ArrayMap<android.content.Context, android.util.ArrayMap<android.content.BroadcastReceiver, android.app.LoadedApk$ReceiverDispatcher>> r2 = r11.mReceivers     // Catch:{ all -> 0x004a }
            java.lang.Object r2 = r2.get(r13)     // Catch:{ all -> 0x004a }
            r0 = r2
            android.util.ArrayMap r0 = (android.util.ArrayMap) r0     // Catch:{ all -> 0x004a }
            r7 = r0
            if (r7 == 0) goto L_0x0058
            java.lang.Object r2 = r7.get(r12)     // Catch:{ all -> 0x004a }
            r0 = r2
            android.app.LoadedApk$ReceiverDispatcher r0 = (android.app.LoadedApk.ReceiverDispatcher) r0     // Catch:{ all -> 0x004a }
            r1 = r0
            r8 = r7
            r9 = r1
        L_0x001d:
            if (r9 != 0) goto L_0x0044
            android.app.LoadedApk$ReceiverDispatcher r1 = new android.app.LoadedApk$ReceiverDispatcher     // Catch:{ all -> 0x004d }
            r2 = r12
            r3 = r13
            r4 = r14
            r5 = r15
            r6 = r16
            r1.<init>(r2, r3, r4, r5, r6)     // Catch:{ all -> 0x004d }
            if (r16 == 0) goto L_0x0056
            if (r8 != 0) goto L_0x0054
            android.util.ArrayMap r7 = new android.util.ArrayMap     // Catch:{ all -> 0x0051 }
            r7.<init>()     // Catch:{ all -> 0x0051 }
            android.util.ArrayMap<android.content.Context, android.util.ArrayMap<android.content.BroadcastReceiver, android.app.LoadedApk$ReceiverDispatcher>> r2 = r11.mReceivers
            r2.put(r13, r7)
        L_0x0038:
            r7.put(r12, r1)
        L_0x003b:
            r2 = 0
            r1.mForgotten = r2
            android.content.IIntentReceiver r2 = r1.getIIntentReceiver()
            monitor-exit(r10)
            return r2
        L_0x0044:
            r9.validate(r13, r14)
            r7 = r8
            r1 = r9
            goto L_0x003b
        L_0x004a:
            r2 = move-exception
        L_0x004b:
            monitor-exit(r10)
            throw r2
        L_0x004d:
            r2 = move-exception
            r7 = r8
            r1 = r9
            goto L_0x004b
        L_0x0051:
            r2 = move-exception
            r7 = r8
            goto L_0x004b
        L_0x0054:
            r7 = r8
            goto L_0x0038
        L_0x0056:
            r7 = r8
            goto L_0x003b
        L_0x0058:
            r8 = r7
            r9 = r1
            goto L_0x001d
        */
        throw new UnsupportedOperationException("Method not decompiled: android.app.LoadedApk.getReceiverDispatcher(android.content.BroadcastReceiver, android.content.Context, android.os.Handler, android.app.Instrumentation, boolean):android.content.IIntentReceiver");
    }

    public IIntentReceiver forgetReceiverDispatcher(Context context, BroadcastReceiver r) {
        ReceiverDispatcher rd;
        ReceiverDispatcher rd2;
        IIntentReceiver iIntentReceiver;
        synchronized (this.mReceivers) {
            ArrayMap<BroadcastReceiver, ReceiverDispatcher> map = this.mReceivers.get(context);
            if (map == null || (rd2 = map.get(r)) == null) {
                ArrayMap<BroadcastReceiver, ReceiverDispatcher> holder = this.mUnregisteredReceivers.get(context);
                if (holder != null && (rd = holder.get(r)) != null) {
                    throw new IllegalArgumentException("Unregistering Receiver " + r + " that was already unregistered", rd.getUnregisterLocation());
                } else if (context == null) {
                    throw new IllegalStateException("Unbinding Receiver " + r + " from Context that is no longer in use: " + context);
                } else {
                    throw new IllegalArgumentException("Receiver not registered: " + r);
                }
            } else {
                map.remove(r);
                if (map.size() == 0) {
                    this.mReceivers.remove(context);
                }
                if (r.getDebugUnregister()) {
                    ArrayMap<BroadcastReceiver, ReceiverDispatcher> holder2 = this.mUnregisteredReceivers.get(context);
                    if (holder2 == null) {
                        holder2 = new ArrayMap<>();
                        this.mUnregisteredReceivers.put(context, holder2);
                    }
                    RuntimeException ex = new IllegalArgumentException("Originally unregistered here:");
                    ex.fillInStackTrace();
                    rd2.setUnregisterLocation(ex);
                    holder2.put(r, rd2);
                }
                rd2.mForgotten = true;
                iIntentReceiver = rd2.getIIntentReceiver();
            }
        }
        return iIntentReceiver;
    }

    /* access modifiers changed from: package-private */
    public static final class ReceiverDispatcher {
        final Handler mActivityThread;
        final Context mContext;
        boolean mForgotten;
        final IIntentReceiver.Stub mIIntentReceiver;
        final Instrumentation mInstrumentation;
        final IntentReceiverLeaked mLocation;
        final BroadcastReceiver mReceiver;
        final boolean mRegistered;
        RuntimeException mUnregisterLocation;

        static final class InnerReceiver extends IIntentReceiver.Stub {
            final WeakReference<ReceiverDispatcher> mDispatcher;
            final ReceiverDispatcher mStrongRef;

            InnerReceiver(ReceiverDispatcher rd, boolean strong) {
                this.mDispatcher = new WeakReference<>(rd);
                this.mStrongRef = !strong ? null : rd;
            }

            @Override // android.content.IIntentReceiver
            public void performReceive(Intent intent, int resultCode, String data, Bundle extras, boolean ordered, boolean sticky, int sendingUser) {
                ReceiverDispatcher rd = this.mDispatcher.get();
                if (rd != null) {
                    rd.performReceive(intent, resultCode, data, extras, ordered, sticky, sendingUser);
                    return;
                }
                IActivityManager mgr = ActivityManagerNative.getDefault();
                if (extras != null) {
                    try {
                        extras.setAllowFds(false);
                    } catch (RemoteException e) {
                        Slog.w(ActivityThread.TAG, "Couldn't finish broadcast to unregistered receiver");
                        return;
                    }
                }
                mgr.finishReceiver(this, resultCode, data, extras, false);
            }
        }

        /* access modifiers changed from: package-private */
        public final class Args extends BroadcastReceiver.PendingResult implements Runnable {
            private Intent mCurIntent;
            private final boolean mOrdered;

            /* JADX INFO: super call moved to the top of the method (can break code semantics) */
            public Args(Intent intent, int resultCode, String resultData, Bundle resultExtras, boolean ordered, boolean sticky, int sendingUser) {
                super(resultCode, resultData, resultExtras, ReceiverDispatcher.this.mRegistered ? 1 : 2, ordered, sticky, ReceiverDispatcher.this.mIIntentReceiver.asBinder(), sendingUser);
                this.mCurIntent = intent;
                this.mOrdered = ordered;
            }

            public void run() {
                BroadcastReceiver receiver = ReceiverDispatcher.this.mReceiver;
                boolean ordered = this.mOrdered;
                IActivityManager mgr = ActivityManagerNative.getDefault();
                Intent intent = this.mCurIntent;
                this.mCurIntent = null;
                if (receiver != null && !ReceiverDispatcher.this.mForgotten) {
                    Trace.traceBegin(64, "broadcastReceiveReg");
                    try {
                        ClassLoader cl = ReceiverDispatcher.this.mReceiver.getClass().getClassLoader();
                        intent.setExtrasClassLoader(cl);
                        setExtrasClassLoader(cl);
                        receiver.setPendingResult(this);
                        receiver.onReceive(ReceiverDispatcher.this.mContext, intent);
                    } catch (Exception e) {
                        if (ReceiverDispatcher.this.mRegistered && ordered) {
                            sendFinished(mgr);
                        }
                        if (ReceiverDispatcher.this.mInstrumentation == null || !ReceiverDispatcher.this.mInstrumentation.onException(ReceiverDispatcher.this.mReceiver, e)) {
                            Trace.traceEnd(64);
                            throw new RuntimeException("Error receiving broadcast " + intent + " in " + ReceiverDispatcher.this.mReceiver, e);
                        }
                    }
                    if (receiver.getPendingResult() != null) {
                        finish();
                    }
                    Trace.traceEnd(64);
                } else if (ReceiverDispatcher.this.mRegistered && ordered) {
                    sendFinished(mgr);
                }
            }
        }

        ReceiverDispatcher(BroadcastReceiver receiver, Context context, Handler activityThread, Instrumentation instrumentation, boolean registered) {
            if (activityThread == null) {
                throw new NullPointerException("Handler must not be null");
            }
            this.mIIntentReceiver = new InnerReceiver(this, !registered);
            this.mReceiver = receiver;
            this.mContext = context;
            this.mActivityThread = activityThread;
            this.mInstrumentation = instrumentation;
            this.mRegistered = registered;
            this.mLocation = new IntentReceiverLeaked(null);
            this.mLocation.fillInStackTrace();
        }

        /* access modifiers changed from: package-private */
        public void validate(Context context, Handler activityThread) {
            if (this.mContext != context) {
                throw new IllegalStateException("Receiver " + this.mReceiver + " registered with differing Context (was " + this.mContext + " now " + context + ")");
            } else if (this.mActivityThread != activityThread) {
                throw new IllegalStateException("Receiver " + this.mReceiver + " registered with differing handler (was " + this.mActivityThread + " now " + activityThread + ")");
            }
        }

        /* access modifiers changed from: package-private */
        public IntentReceiverLeaked getLocation() {
            return this.mLocation;
        }

        /* access modifiers changed from: package-private */
        public BroadcastReceiver getIntentReceiver() {
            return this.mReceiver;
        }

        /* access modifiers changed from: package-private */
        public IIntentReceiver getIIntentReceiver() {
            return this.mIIntentReceiver;
        }

        /* access modifiers changed from: package-private */
        public void setUnregisterLocation(RuntimeException ex) {
            this.mUnregisterLocation = ex;
        }

        /* access modifiers changed from: package-private */
        public RuntimeException getUnregisterLocation() {
            return this.mUnregisterLocation;
        }

        public void performReceive(Intent intent, int resultCode, String data, Bundle extras, boolean ordered, boolean sticky, int sendingUser) {
            Args args = new Args(intent, resultCode, data, extras, ordered, sticky, sendingUser);
            if (!this.mActivityThread.post(args) && this.mRegistered && ordered) {
                args.sendFinished(ActivityManagerNative.getDefault());
            }
        }
    }

    public final IServiceConnection getServiceDispatcher(ServiceConnection c, Context context, Handler handler, int flags) {
        Throwable th;
        ServiceDispatcher sd;
        ServiceDispatcher sd2;
        synchronized (this.mServices) {
            try {
                ArrayMap<ServiceConnection, ServiceDispatcher> map = this.mServices.get(context);
                if (map != null) {
                    sd = map.get(c);
                } else {
                    sd = null;
                }
                if (sd == null) {
                    try {
                        sd2 = new ServiceDispatcher(c, context, handler, flags);
                        if (map == null) {
                            map = new ArrayMap<>();
                            this.mServices.put(context, map);
                        }
                        map.put(c, sd2);
                    } catch (Throwable th2) {
                        th = th2;
                        throw th;
                    }
                } else {
                    sd.validate(context, handler);
                    sd2 = sd;
                }
                return sd2.getIServiceConnection();
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
    }

    public final IServiceConnection forgetServiceDispatcher(Context context, ServiceConnection c) {
        ServiceDispatcher sd;
        ServiceDispatcher sd2;
        IServiceConnection iServiceConnection;
        synchronized (this.mServices) {
            ArrayMap<ServiceConnection, ServiceDispatcher> map = this.mServices.get(context);
            if (map == null || (sd2 = map.get(c)) == null) {
                ArrayMap<ServiceConnection, ServiceDispatcher> holder = this.mUnboundServices.get(context);
                if (holder != null && (sd = holder.get(c)) != null) {
                    throw new IllegalArgumentException("Unbinding Service " + c + " that was already unbound", sd.getUnbindLocation());
                } else if (context == null) {
                    throw new IllegalStateException("Unbinding Service " + c + " from Context that is no longer in use: " + context);
                } else {
                    throw new IllegalArgumentException("Service not registered: " + c);
                }
            } else {
                map.remove(c);
                sd2.doForget();
                if (map.size() == 0) {
                    this.mServices.remove(context);
                }
                if ((sd2.getFlags() & 2) != 0) {
                    ArrayMap<ServiceConnection, ServiceDispatcher> holder2 = this.mUnboundServices.get(context);
                    if (holder2 == null) {
                        holder2 = new ArrayMap<>();
                        this.mUnboundServices.put(context, holder2);
                    }
                    RuntimeException ex = new IllegalArgumentException("Originally unbound here:");
                    ex.fillInStackTrace();
                    sd2.setUnbindLocation(ex);
                    holder2.put(c, sd2);
                }
                iServiceConnection = sd2.getIServiceConnection();
            }
        }
        return iServiceConnection;
    }

    /* access modifiers changed from: package-private */
    public static final class ServiceDispatcher {
        private final ArrayMap<ComponentName, ConnectionInfo> mActiveConnections = new ArrayMap<>();
        private final Handler mActivityThread;
        private final ServiceConnection mConnection;
        private final Context mContext;
        private boolean mDied;
        private final int mFlags;
        private boolean mForgotten;
        private final InnerConnection mIServiceConnection = new InnerConnection(this);
        private final ServiceConnectionLeaked mLocation;
        private RuntimeException mUnbindLocation;

        /* access modifiers changed from: private */
        public static class ConnectionInfo {
            IBinder binder;
            IBinder.DeathRecipient deathMonitor;

            private ConnectionInfo() {
            }
        }

        /* access modifiers changed from: private */
        public static class InnerConnection extends IServiceConnection.Stub {
            final WeakReference<ServiceDispatcher> mDispatcher;

            InnerConnection(ServiceDispatcher sd) {
                this.mDispatcher = new WeakReference<>(sd);
            }

            @Override // android.app.IServiceConnection
            public void connected(ComponentName name, IBinder service) throws RemoteException {
                ServiceDispatcher sd = this.mDispatcher.get();
                if (sd != null) {
                    sd.connected(name, service);
                }
            }
        }

        ServiceDispatcher(ServiceConnection conn, Context context, Handler activityThread, int flags) {
            this.mConnection = conn;
            this.mContext = context;
            this.mActivityThread = activityThread;
            this.mLocation = new ServiceConnectionLeaked(null);
            this.mLocation.fillInStackTrace();
            this.mFlags = flags;
        }

        /* access modifiers changed from: package-private */
        public void validate(Context context, Handler activityThread) {
            if (this.mContext != context) {
                throw new RuntimeException("ServiceConnection " + this.mConnection + " registered with differing Context (was " + this.mContext + " now " + context + ")");
            } else if (this.mActivityThread != activityThread) {
                throw new RuntimeException("ServiceConnection " + this.mConnection + " registered with differing handler (was " + this.mActivityThread + " now " + activityThread + ")");
            }
        }

        /* access modifiers changed from: package-private */
        public void doForget() {
            synchronized (this) {
                for (int i = 0; i < this.mActiveConnections.size(); i++) {
                    ConnectionInfo ci = this.mActiveConnections.valueAt(i);
                    ci.binder.unlinkToDeath(ci.deathMonitor, 0);
                }
                this.mActiveConnections.clear();
                this.mForgotten = true;
            }
        }

        /* access modifiers changed from: package-private */
        public ServiceConnectionLeaked getLocation() {
            return this.mLocation;
        }

        /* access modifiers changed from: package-private */
        public ServiceConnection getServiceConnection() {
            return this.mConnection;
        }

        /* access modifiers changed from: package-private */
        public IServiceConnection getIServiceConnection() {
            return this.mIServiceConnection;
        }

        /* access modifiers changed from: package-private */
        public int getFlags() {
            return this.mFlags;
        }

        /* access modifiers changed from: package-private */
        public void setUnbindLocation(RuntimeException ex) {
            this.mUnbindLocation = ex;
        }

        /* access modifiers changed from: package-private */
        public RuntimeException getUnbindLocation() {
            return this.mUnbindLocation;
        }

        public void connected(ComponentName name, IBinder service) {
            if (this.mActivityThread != null) {
                this.mActivityThread.post(new RunConnection(name, service, 0));
            } else {
                doConnected(name, service);
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:12:0x0020, code lost:
            if (r5.mActivityThread == null) goto L_0x0030;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:13:0x0022, code lost:
            r5.mActivityThread.post(new android.app.LoadedApk.ServiceDispatcher.RunConnection(r5, r6, r7, 1));
         */
        /* JADX WARNING: Code restructure failed: missing block: B:17:0x0030, code lost:
            doDeath(r6, r7);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:20:?, code lost:
            return;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void death(android.content.ComponentName r6, android.os.IBinder r7) {
            /*
                r5 = this;
                r4 = 1
                monitor-enter(r5)
                r1 = 1
                r5.mDied = r1     // Catch:{ all -> 0x002d }
                android.util.ArrayMap<android.content.ComponentName, android.app.LoadedApk$ServiceDispatcher$ConnectionInfo> r1 = r5.mActiveConnections     // Catch:{ all -> 0x002d }
                java.lang.Object r0 = r1.remove(r6)     // Catch:{ all -> 0x002d }
                android.app.LoadedApk$ServiceDispatcher$ConnectionInfo r0 = (android.app.LoadedApk.ServiceDispatcher.ConnectionInfo) r0     // Catch:{ all -> 0x002d }
                if (r0 == 0) goto L_0x0013
                android.os.IBinder r1 = r0.binder     // Catch:{ all -> 0x002d }
                if (r1 == r7) goto L_0x0015
            L_0x0013:
                monitor-exit(r5)     // Catch:{ all -> 0x002d }
            L_0x0014:
                return
            L_0x0015:
                android.os.IBinder r1 = r0.binder     // Catch:{ all -> 0x002d }
                android.os.IBinder$DeathRecipient r2 = r0.deathMonitor     // Catch:{ all -> 0x002d }
                r3 = 0
                r1.unlinkToDeath(r2, r3)     // Catch:{ all -> 0x002d }
                monitor-exit(r5)     // Catch:{ all -> 0x002d }
                android.os.Handler r1 = r5.mActivityThread
                if (r1 == 0) goto L_0x0030
                android.os.Handler r1 = r5.mActivityThread
                android.app.LoadedApk$ServiceDispatcher$RunConnection r2 = new android.app.LoadedApk$ServiceDispatcher$RunConnection
                r2.<init>(r6, r7, r4)
                r1.post(r2)
                goto L_0x0014
            L_0x002d:
                r1 = move-exception
                monitor-exit(r5)
                throw r1
            L_0x0030:
                r5.doDeath(r6, r7)
                goto L_0x0014
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.LoadedApk.ServiceDispatcher.death(android.content.ComponentName, android.os.IBinder):void");
        }

        /* JADX WARNING: Code restructure failed: missing block: B:21:0x0044, code lost:
            if (r2 == null) goto L_0x004b;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:22:0x0046, code lost:
            r6.mConnection.onServiceDisconnected(r7);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:23:0x004b, code lost:
            if (r8 == null) goto L_?;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:24:0x004d, code lost:
            r6.mConnection.onServiceConnected(r7, r8);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:31:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:32:?, code lost:
            return;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void doConnected(android.content.ComponentName r7, android.os.IBinder r8) {
            /*
                r6 = this;
                monitor-enter(r6)
                boolean r3 = r6.mForgotten     // Catch:{ all -> 0x0017 }
                if (r3 == 0) goto L_0x0007
                monitor-exit(r6)     // Catch:{ all -> 0x0017 }
            L_0x0006:
                return
            L_0x0007:
                android.util.ArrayMap<android.content.ComponentName, android.app.LoadedApk$ServiceDispatcher$ConnectionInfo> r3 = r6.mActiveConnections     // Catch:{ all -> 0x0017 }
                java.lang.Object r2 = r3.get(r7)     // Catch:{ all -> 0x0017 }
                android.app.LoadedApk$ServiceDispatcher$ConnectionInfo r2 = (android.app.LoadedApk.ServiceDispatcher.ConnectionInfo) r2     // Catch:{ all -> 0x0017 }
                if (r2 == 0) goto L_0x001a
                android.os.IBinder r3 = r2.binder     // Catch:{ all -> 0x0017 }
                if (r3 != r8) goto L_0x001a
                monitor-exit(r6)     // Catch:{ all -> 0x0017 }
                goto L_0x0006
            L_0x0017:
                r3 = move-exception
                monitor-exit(r6)     // Catch:{ all -> 0x0017 }
                throw r3
            L_0x001a:
                if (r8 == 0) goto L_0x005b
                r3 = 0
                r6.mDied = r3
                android.app.LoadedApk$ServiceDispatcher$ConnectionInfo r1 = new android.app.LoadedApk$ServiceDispatcher$ConnectionInfo
                r3 = 0
                r1.<init>()
                r1.binder = r8
                android.app.LoadedApk$ServiceDispatcher$DeathMonitor r3 = new android.app.LoadedApk$ServiceDispatcher$DeathMonitor
                r3.<init>(r7, r8)
                r1.deathMonitor = r3
                android.os.IBinder$DeathRecipient r3 = r1.deathMonitor     // Catch:{ RemoteException -> 0x0053 }
                r4 = 0
                r8.linkToDeath(r3, r4)     // Catch:{ RemoteException -> 0x0053 }
                android.util.ArrayMap<android.content.ComponentName, android.app.LoadedApk$ServiceDispatcher$ConnectionInfo> r3 = r6.mActiveConnections     // Catch:{ RemoteException -> 0x0053 }
                r3.put(r7, r1)     // Catch:{ RemoteException -> 0x0053 }
            L_0x0039:
                if (r2 == 0) goto L_0x0043
                android.os.IBinder r3 = r2.binder
                android.os.IBinder$DeathRecipient r4 = r2.deathMonitor
                r5 = 0
                r3.unlinkToDeath(r4, r5)
            L_0x0043:
                monitor-exit(r6)
                if (r2 == 0) goto L_0x004b
                android.content.ServiceConnection r3 = r6.mConnection
                r3.onServiceDisconnected(r7)
            L_0x004b:
                if (r8 == 0) goto L_0x0006
                android.content.ServiceConnection r3 = r6.mConnection
                r3.onServiceConnected(r7, r8)
                goto L_0x0006
            L_0x0053:
                r0 = move-exception
                android.util.ArrayMap<android.content.ComponentName, android.app.LoadedApk$ServiceDispatcher$ConnectionInfo> r3 = r6.mActiveConnections
                r3.remove(r7)
                monitor-exit(r6)
                goto L_0x0006
            L_0x005b:
                android.util.ArrayMap<android.content.ComponentName, android.app.LoadedApk$ServiceDispatcher$ConnectionInfo> r3 = r6.mActiveConnections
                r3.remove(r7)
                goto L_0x0039
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.LoadedApk.ServiceDispatcher.doConnected(android.content.ComponentName, android.os.IBinder):void");
        }

        public void doDeath(ComponentName name, IBinder service) {
            this.mConnection.onServiceDisconnected(name);
        }

        /* access modifiers changed from: private */
        public final class RunConnection implements Runnable {
            final int mCommand;
            final ComponentName mName;
            final IBinder mService;

            RunConnection(ComponentName name, IBinder service, int command) {
                this.mName = name;
                this.mService = service;
                this.mCommand = command;
            }

            public void run() {
                if (this.mCommand == 0) {
                    ServiceDispatcher.this.doConnected(this.mName, this.mService);
                } else if (this.mCommand == 1) {
                    ServiceDispatcher.this.doDeath(this.mName, this.mService);
                }
            }
        }

        /* access modifiers changed from: private */
        public final class DeathMonitor implements IBinder.DeathRecipient {
            final ComponentName mName;
            final IBinder mService;

            DeathMonitor(ComponentName name, IBinder service) {
                this.mName = name;
                this.mService = service;
            }

            @Override // android.os.IBinder.DeathRecipient
            public void binderDied() {
                ServiceDispatcher.this.death(this.mName, this.mService);
            }
        }
    }
}

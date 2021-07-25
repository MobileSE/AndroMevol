package android.os;

import android.animation.ValueAnimator;
import android.app.ActivityManagerNative;
import android.app.ActivityThread;
import android.app.ApplicationErrorReport;
import android.app.IActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.MessageQueue;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Printer;
import android.util.Singleton;
import android.util.Slog;
import android.view.IWindowManager;
import com.android.internal.os.RuntimeInit;
import com.android.internal.util.FastPrintWriter;
import dalvik.system.BlockGuard;
import dalvik.system.CloseGuard;
import dalvik.system.VMDebug;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public final class StrictMode {
    private static final int ALL_THREAD_DETECT_BITS = 15;
    private static final int ALL_VM_DETECT_BITS = 32256;
    public static final int DETECT_CUSTOM = 8;
    public static final int DETECT_DISK_READ = 2;
    public static final int DETECT_DISK_WRITE = 1;
    public static final int DETECT_NETWORK = 4;
    public static final int DETECT_VM_ACTIVITY_LEAKS = 2048;
    public static final int DETECT_VM_CLOSABLE_LEAKS = 1024;
    public static final int DETECT_VM_CURSOR_LEAKS = 512;
    private static final int DETECT_VM_FILE_URI_EXPOSURE = 16384;
    private static final int DETECT_VM_INSTANCE_LEAKS = 4096;
    public static final int DETECT_VM_REGISTRATION_LEAKS = 8192;
    public static final String DISABLE_PROPERTY = "persist.sys.strictmode.disable";
    private static final HashMap<Class, Integer> EMPTY_CLASS_LIMIT_MAP = new HashMap<>();
    private static final boolean IS_ENG_BUILD = "eng".equals(Build.TYPE);
    private static final boolean IS_USER_BUILD = Context.USER_SERVICE.equals(Build.TYPE);
    private static final boolean LOG_V = Log.isLoggable(TAG, 2);
    private static final int MAX_OFFENSES_PER_LOOP = 10;
    private static final int MAX_SPAN_TAGS = 20;
    private static final long MIN_DIALOG_INTERVAL_MS = 30000;
    private static final long MIN_LOG_INTERVAL_MS = 1000;
    private static final Span NO_OP_SPAN = new Span() {
        /* class android.os.StrictMode.AnonymousClass7 */

        @Override // android.os.StrictMode.Span
        public void finish() {
        }
    };
    public static final int PENALTY_DEATH = 64;
    public static final int PENALTY_DEATH_ON_NETWORK = 512;
    public static final int PENALTY_DIALOG = 32;
    public static final int PENALTY_DROPBOX = 128;
    public static final int PENALTY_FLASH = 2048;
    public static final int PENALTY_GATHER = 256;
    public static final int PENALTY_LOG = 16;
    private static final String TAG = "StrictMode";
    private static final int THREAD_PENALTY_MASK = 3056;
    public static final String VISUAL_PROPERTY = "persist.sys.strictmode.visual";
    private static final int VM_PENALTY_MASK = 208;
    private static final ThreadLocal<ArrayList<ViolationInfo>> gatheredViolations = new ThreadLocal<ArrayList<ViolationInfo>>() {
        /* class android.os.StrictMode.AnonymousClass1 */

        /* access modifiers changed from: protected */
        @Override // java.lang.ThreadLocal
        public ArrayList<ViolationInfo> initialValue() {
            return null;
        }
    };
    private static final AtomicInteger sDropboxCallsInFlight = new AtomicInteger(0);
    private static final HashMap<Class, Integer> sExpectedActivityInstanceCount = new HashMap<>();
    private static boolean sIsIdlerRegistered = false;
    private static long sLastInstanceCountCheckMillis = 0;
    private static final HashMap<Integer, Long> sLastVmViolationTime = new HashMap<>();
    private static final MessageQueue.IdleHandler sProcessIdleHandler = new MessageQueue.IdleHandler() {
        /* class android.os.StrictMode.AnonymousClass6 */

        @Override // android.os.MessageQueue.IdleHandler
        public boolean queueIdle() {
            long now = SystemClock.uptimeMillis();
            if (now - StrictMode.sLastInstanceCountCheckMillis <= 30000) {
                return true;
            }
            long unused = StrictMode.sLastInstanceCountCheckMillis = now;
            StrictMode.conditionallyCheckInstanceCounts();
            return true;
        }
    };
    private static final ThreadLocal<ThreadSpanState> sThisThreadSpanState = new ThreadLocal<ThreadSpanState>() {
        /* class android.os.StrictMode.AnonymousClass8 */

        /* access modifiers changed from: protected */
        @Override // java.lang.ThreadLocal
        public ThreadSpanState initialValue() {
            return new ThreadSpanState();
        }
    };
    private static volatile VmPolicy sVmPolicy = VmPolicy.LAX;
    private static volatile int sVmPolicyMask = 0;
    private static Singleton<IWindowManager> sWindowManager = new Singleton<IWindowManager>() {
        /* class android.os.StrictMode.AnonymousClass9 */

        /* access modifiers changed from: protected */
        @Override // android.util.Singleton
        public IWindowManager create() {
            return IWindowManager.Stub.asInterface(ServiceManager.getService(Context.WINDOW_SERVICE));
        }
    };
    private static final ThreadLocal<AndroidBlockGuardPolicy> threadAndroidPolicy = new ThreadLocal<AndroidBlockGuardPolicy>() {
        /* class android.os.StrictMode.AnonymousClass4 */

        /* access modifiers changed from: protected */
        @Override // java.lang.ThreadLocal
        public AndroidBlockGuardPolicy initialValue() {
            return new AndroidBlockGuardPolicy(0);
        }
    };
    private static final ThreadLocal<Handler> threadHandler = new ThreadLocal<Handler>() {
        /* class android.os.StrictMode.AnonymousClass3 */

        /* access modifiers changed from: protected */
        @Override // java.lang.ThreadLocal
        public Handler initialValue() {
            return new Handler();
        }
    };
    private static final ThreadLocal<ArrayList<ViolationInfo>> violationsBeingTimed = new ThreadLocal<ArrayList<ViolationInfo>>() {
        /* class android.os.StrictMode.AnonymousClass2 */

        /* access modifiers changed from: protected */
        @Override // java.lang.ThreadLocal
        public ArrayList<ViolationInfo> initialValue() {
            return new ArrayList<>();
        }
    };

    private StrictMode() {
    }

    public static final class ThreadPolicy {
        public static final ThreadPolicy LAX = new ThreadPolicy(0);
        final int mask;

        private ThreadPolicy(int mask2) {
            this.mask = mask2;
        }

        public String toString() {
            return "[StrictMode.ThreadPolicy; mask=" + this.mask + "]";
        }

        public static final class Builder {
            private int mMask;

            public Builder() {
                this.mMask = 0;
                this.mMask = 0;
            }

            public Builder(ThreadPolicy policy) {
                this.mMask = 0;
                this.mMask = policy.mask;
            }

            public Builder detectAll() {
                return enable(15);
            }

            public Builder permitAll() {
                return disable(15);
            }

            public Builder detectNetwork() {
                return enable(4);
            }

            public Builder permitNetwork() {
                return disable(4);
            }

            public Builder detectDiskReads() {
                return enable(2);
            }

            public Builder permitDiskReads() {
                return disable(2);
            }

            public Builder detectCustomSlowCalls() {
                return enable(8);
            }

            public Builder permitCustomSlowCalls() {
                return disable(8);
            }

            public Builder detectDiskWrites() {
                return enable(1);
            }

            public Builder permitDiskWrites() {
                return disable(1);
            }

            public Builder penaltyDialog() {
                return enable(32);
            }

            public Builder penaltyDeath() {
                return enable(64);
            }

            public Builder penaltyDeathOnNetwork() {
                return enable(512);
            }

            public Builder penaltyFlashScreen() {
                return enable(2048);
            }

            public Builder penaltyLog() {
                return enable(16);
            }

            public Builder penaltyDropBox() {
                return enable(128);
            }

            private Builder enable(int bit) {
                this.mMask |= bit;
                return this;
            }

            private Builder disable(int bit) {
                this.mMask &= bit ^ -1;
                return this;
            }

            public ThreadPolicy build() {
                if (this.mMask != 0 && (this.mMask & 240) == 0) {
                    penaltyLog();
                }
                return new ThreadPolicy(this.mMask);
            }
        }
    }

    public static final class VmPolicy {
        public static final VmPolicy LAX = new VmPolicy(0, StrictMode.EMPTY_CLASS_LIMIT_MAP);
        final HashMap<Class, Integer> classInstanceLimit;
        final int mask;

        private VmPolicy(int mask2, HashMap<Class, Integer> classInstanceLimit2) {
            if (classInstanceLimit2 == null) {
                throw new NullPointerException("classInstanceLimit == null");
            }
            this.mask = mask2;
            this.classInstanceLimit = classInstanceLimit2;
        }

        public String toString() {
            return "[StrictMode.VmPolicy; mask=" + this.mask + "]";
        }

        public static final class Builder {
            private HashMap<Class, Integer> mClassInstanceLimit;
            private boolean mClassInstanceLimitNeedCow;
            private int mMask;

            public Builder() {
                this.mClassInstanceLimitNeedCow = false;
                this.mMask = 0;
            }

            public Builder(VmPolicy base) {
                this.mClassInstanceLimitNeedCow = false;
                this.mMask = base.mask;
                this.mClassInstanceLimitNeedCow = true;
                this.mClassInstanceLimit = base.classInstanceLimit;
            }

            public Builder setClassInstanceLimit(Class klass, int instanceLimit) {
                if (klass == null) {
                    throw new NullPointerException("klass == null");
                }
                if (!this.mClassInstanceLimitNeedCow) {
                    if (this.mClassInstanceLimit == null) {
                        this.mClassInstanceLimit = new HashMap<>();
                    }
                    this.mMask |= 4096;
                    this.mClassInstanceLimit.put(klass, Integer.valueOf(instanceLimit));
                } else if (!this.mClassInstanceLimit.containsKey(klass) || this.mClassInstanceLimit.get(klass).intValue() != instanceLimit) {
                    this.mClassInstanceLimitNeedCow = false;
                    this.mClassInstanceLimit = (HashMap) this.mClassInstanceLimit.clone();
                    this.mMask |= 4096;
                    this.mClassInstanceLimit.put(klass, Integer.valueOf(instanceLimit));
                }
                return this;
            }

            public Builder detectActivityLeaks() {
                return enable(2048);
            }

            public Builder detectAll() {
                return enable(28160);
            }

            public Builder detectLeakedSqlLiteObjects() {
                return enable(512);
            }

            public Builder detectLeakedClosableObjects() {
                return enable(1024);
            }

            public Builder detectLeakedRegistrationObjects() {
                return enable(8192);
            }

            public Builder detectFileUriExposure() {
                return enable(16384);
            }

            public Builder penaltyDeath() {
                return enable(64);
            }

            public Builder penaltyLog() {
                return enable(16);
            }

            public Builder penaltyDropBox() {
                return enable(128);
            }

            private Builder enable(int bit) {
                this.mMask |= bit;
                return this;
            }

            public VmPolicy build() {
                if (this.mMask != 0 && (this.mMask & 240) == 0) {
                    penaltyLog();
                }
                return new VmPolicy(this.mMask, this.mClassInstanceLimit != null ? this.mClassInstanceLimit : StrictMode.EMPTY_CLASS_LIMIT_MAP);
            }
        }
    }

    public static void setThreadPolicy(ThreadPolicy policy) {
        setThreadPolicyMask(policy.mask);
    }

    /* access modifiers changed from: private */
    public static void setThreadPolicyMask(int policyMask) {
        setBlockGuardPolicy(policyMask);
        Binder.setThreadStrictModePolicy(policyMask);
    }

    private static void setBlockGuardPolicy(int policyMask) {
        AndroidBlockGuardPolicy androidPolicy;
        if (policyMask == 0) {
            BlockGuard.setThreadPolicy(BlockGuard.LAX_POLICY);
            return;
        }
        BlockGuard.Policy policy = BlockGuard.getThreadPolicy();
        if (policy instanceof AndroidBlockGuardPolicy) {
            androidPolicy = (AndroidBlockGuardPolicy) policy;
        } else {
            androidPolicy = threadAndroidPolicy.get();
            BlockGuard.setThreadPolicy(androidPolicy);
        }
        androidPolicy.setPolicyMask(policyMask);
    }

    private static void setCloseGuardEnabled(boolean enabled) {
        if (!(CloseGuard.getReporter() instanceof AndroidCloseGuardReporter)) {
            CloseGuard.setReporter(new AndroidCloseGuardReporter());
        }
        CloseGuard.setEnabled(enabled);
    }

    public static class StrictModeViolation extends BlockGuard.BlockGuardPolicyException {
        public StrictModeViolation(int policyState, int policyViolated, String message) {
            super(policyState, policyViolated, message);
        }
    }

    public static class StrictModeNetworkViolation extends StrictModeViolation {
        public StrictModeNetworkViolation(int policyMask) {
            super(policyMask, 4, null);
        }
    }

    /* access modifiers changed from: private */
    public static class StrictModeDiskReadViolation extends StrictModeViolation {
        public StrictModeDiskReadViolation(int policyMask) {
            super(policyMask, 2, null);
        }
    }

    /* access modifiers changed from: private */
    public static class StrictModeDiskWriteViolation extends StrictModeViolation {
        public StrictModeDiskWriteViolation(int policyMask) {
            super(policyMask, 1, null);
        }
    }

    /* access modifiers changed from: private */
    public static class StrictModeCustomViolation extends StrictModeViolation {
        public StrictModeCustomViolation(int policyMask, String name) {
            super(policyMask, 8, name);
        }
    }

    public static int getThreadPolicyMask() {
        return BlockGuard.getThreadPolicy().getPolicyMask();
    }

    public static ThreadPolicy getThreadPolicy() {
        return new ThreadPolicy(getThreadPolicyMask());
    }

    public static ThreadPolicy allowThreadDiskWrites() {
        int oldPolicyMask = getThreadPolicyMask();
        int newPolicyMask = oldPolicyMask & -4;
        if (newPolicyMask != oldPolicyMask) {
            setThreadPolicyMask(newPolicyMask);
        }
        return new ThreadPolicy(oldPolicyMask);
    }

    public static ThreadPolicy allowThreadDiskReads() {
        int oldPolicyMask = getThreadPolicyMask();
        int newPolicyMask = oldPolicyMask & -3;
        if (newPolicyMask != oldPolicyMask) {
            setThreadPolicyMask(newPolicyMask);
        }
        return new ThreadPolicy(oldPolicyMask);
    }

    private static boolean amTheSystemServerProcess() {
        if (Process.myUid() != 1000) {
            return false;
        }
        Throwable stack = new Throwable();
        stack.fillInStackTrace();
        for (StackTraceElement ste : stack.getStackTrace()) {
            String clsName = ste.getClassName();
            if (clsName != null && clsName.startsWith("com.android.server.")) {
                return true;
            }
        }
        return false;
    }

    public static boolean conditionallyEnableDebugLogging() {
        boolean doFlashes;
        if (!SystemProperties.getBoolean(VISUAL_PROPERTY, false) || amTheSystemServerProcess()) {
            doFlashes = false;
        } else {
            doFlashes = true;
        }
        boolean suppress = SystemProperties.getBoolean(DISABLE_PROPERTY, false);
        if (doFlashes || (!IS_USER_BUILD && !suppress)) {
            if (IS_ENG_BUILD) {
                doFlashes = true;
            }
            int threadPolicyMask = 7;
            if (!IS_USER_BUILD) {
                threadPolicyMask = 7 | 128;
            }
            if (doFlashes) {
                threadPolicyMask |= 2048;
            }
            setThreadPolicyMask(threadPolicyMask);
            if (IS_USER_BUILD) {
                setCloseGuardEnabled(false);
            } else {
                VmPolicy.Builder policyBuilder = new VmPolicy.Builder().detectAll().penaltyDropBox();
                if (IS_ENG_BUILD) {
                    policyBuilder.penaltyLog();
                }
                setVmPolicy(policyBuilder.build());
                setCloseGuardEnabled(vmClosableObjectLeaksEnabled());
            }
            return true;
        }
        setCloseGuardEnabled(false);
        return false;
    }

    public static void enableDeathOnNetwork() {
        setThreadPolicyMask(getThreadPolicyMask() | 4 | 512);
    }

    private static int parsePolicyFromMessage(String message) {
        int spaceIndex;
        if (message == null || !message.startsWith("policy=") || (spaceIndex = message.indexOf(32)) == -1) {
            return 0;
        }
        try {
            return Integer.valueOf(message.substring(7, spaceIndex)).intValue();
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /* access modifiers changed from: private */
    public static int parseViolationFromMessage(String message) {
        int violationIndex;
        if (message == null || (violationIndex = message.indexOf("violation=")) == -1) {
            return 0;
        }
        int numberStartIndex = violationIndex + "violation=".length();
        int numberEndIndex = message.indexOf(32, numberStartIndex);
        if (numberEndIndex == -1) {
            numberEndIndex = message.length();
        }
        try {
            return Integer.valueOf(message.substring(numberStartIndex, numberEndIndex)).intValue();
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /* access modifiers changed from: private */
    public static boolean tooManyViolationsThisLoop() {
        return violationsBeingTimed.get().size() >= 10;
    }

    /* access modifiers changed from: private */
    public static class AndroidBlockGuardPolicy implements BlockGuard.Policy {
        private ArrayMap<Integer, Long> mLastViolationTime;
        private int mPolicyMask;

        public AndroidBlockGuardPolicy(int policyMask) {
            this.mPolicyMask = policyMask;
        }

        public String toString() {
            return "AndroidBlockGuardPolicy; mPolicyMask=" + this.mPolicyMask;
        }

        public int getPolicyMask() {
            return this.mPolicyMask;
        }

        public void onWriteToDisk() {
            if ((this.mPolicyMask & 1) != 0 && !StrictMode.tooManyViolationsThisLoop()) {
                BlockGuard.BlockGuardPolicyException e = new StrictModeDiskWriteViolation(this.mPolicyMask);
                e.fillInStackTrace();
                startHandlingViolationException(e);
            }
        }

        /* access modifiers changed from: package-private */
        public void onCustomSlowCall(String name) {
            if ((this.mPolicyMask & 8) != 0 && !StrictMode.tooManyViolationsThisLoop()) {
                BlockGuard.BlockGuardPolicyException e = new StrictModeCustomViolation(this.mPolicyMask, name);
                e.fillInStackTrace();
                startHandlingViolationException(e);
            }
        }

        public void onReadFromDisk() {
            if ((this.mPolicyMask & 2) != 0 && !StrictMode.tooManyViolationsThisLoop()) {
                BlockGuard.BlockGuardPolicyException e = new StrictModeDiskReadViolation(this.mPolicyMask);
                e.fillInStackTrace();
                startHandlingViolationException(e);
            }
        }

        public void onNetwork() {
            if ((this.mPolicyMask & 4) != 0) {
                if ((this.mPolicyMask & 512) != 0) {
                    throw new NetworkOnMainThreadException();
                } else if (!StrictMode.tooManyViolationsThisLoop()) {
                    BlockGuard.BlockGuardPolicyException e = new StrictModeNetworkViolation(this.mPolicyMask);
                    e.fillInStackTrace();
                    startHandlingViolationException(e);
                }
            }
        }

        public void setPolicyMask(int policyMask) {
            this.mPolicyMask = policyMask;
        }

        /* access modifiers changed from: package-private */
        public void startHandlingViolationException(BlockGuard.BlockGuardPolicyException e) {
            ViolationInfo info = new ViolationInfo((Throwable) e, e.getPolicy());
            info.violationUptimeMillis = SystemClock.uptimeMillis();
            handleViolationWithTimingAttempt(info);
        }

        /* access modifiers changed from: package-private */
        public void handleViolationWithTimingAttempt(ViolationInfo info) {
            if (Looper.myLooper() == null || (info.policy & 3056) == 64) {
                info.durationMillis = -1;
                handleViolation(info);
                return;
            }
            final ArrayList<ViolationInfo> records = (ArrayList) StrictMode.violationsBeingTimed.get();
            if (records.size() < 10) {
                records.add(info);
                if (records.size() <= 1) {
                    final IWindowManager windowManager = (info.policy & 2048) != 0 ? (IWindowManager) StrictMode.sWindowManager.get() : null;
                    if (windowManager != null) {
                        try {
                            windowManager.showStrictModeViolation(true);
                        } catch (RemoteException e) {
                        }
                    }
                    ((Handler) StrictMode.threadHandler.get()).postAtFrontOfQueue(new Runnable() {
                        /* class android.os.StrictMode.AndroidBlockGuardPolicy.AnonymousClass1 */

                        public void run() {
                            long loopFinishTime = SystemClock.uptimeMillis();
                            if (windowManager != null) {
                                try {
                                    windowManager.showStrictModeViolation(false);
                                } catch (RemoteException e) {
                                }
                            }
                            for (int n = 0; n < records.size(); n++) {
                                ViolationInfo v = (ViolationInfo) records.get(n);
                                v.violationNumThisLoop = n + 1;
                                v.durationMillis = (int) (loopFinishTime - v.violationUptimeMillis);
                                AndroidBlockGuardPolicy.this.handleViolation(v);
                            }
                            records.clear();
                        }
                    });
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void handleViolation(ViolationInfo info) {
            if (info == null || info.crashInfo == null || info.crashInfo.stackTrace == null) {
                Log.wtf(StrictMode.TAG, "unexpected null stacktrace");
                return;
            }
            if (StrictMode.LOG_V) {
                Log.d(StrictMode.TAG, "handleViolation; policy=" + info.policy);
            }
            if ((info.policy & 256) != 0) {
                ArrayList<ViolationInfo> violations = (ArrayList) StrictMode.gatheredViolations.get();
                if (violations == null) {
                    violations = new ArrayList<>(1);
                    StrictMode.gatheredViolations.set(violations);
                } else if (violations.size() >= 5) {
                    return;
                }
                Iterator i$ = violations.iterator();
                while (i$.hasNext()) {
                    if (info.crashInfo.stackTrace.equals(i$.next().crashInfo.stackTrace)) {
                        return;
                    }
                }
                violations.add(info);
                return;
            }
            Integer crashFingerprint = Integer.valueOf(info.hashCode());
            long lastViolationTime = 0;
            if (this.mLastViolationTime != null) {
                Long vtime = this.mLastViolationTime.get(crashFingerprint);
                if (vtime != null) {
                    lastViolationTime = vtime.longValue();
                }
            } else {
                this.mLastViolationTime = new ArrayMap<>(1);
            }
            long now = SystemClock.uptimeMillis();
            this.mLastViolationTime.put(crashFingerprint, Long.valueOf(now));
            long timeSinceLastViolationMillis = lastViolationTime == 0 ? Long.MAX_VALUE : now - lastViolationTime;
            if ((info.policy & 16) != 0 && timeSinceLastViolationMillis > 1000) {
                if (info.durationMillis != -1) {
                    Log.d(StrictMode.TAG, "StrictMode policy violation; ~duration=" + info.durationMillis + " ms: " + info.crashInfo.stackTrace);
                } else {
                    Log.d(StrictMode.TAG, "StrictMode policy violation: " + info.crashInfo.stackTrace);
                }
            }
            int violationMaskSubset = 0;
            if ((info.policy & 32) != 0 && timeSinceLastViolationMillis > 30000) {
                violationMaskSubset = 0 | 32;
            }
            if ((info.policy & 128) != 0 && lastViolationTime == 0) {
                violationMaskSubset |= 128;
            }
            if (violationMaskSubset != 0) {
                int violationMaskSubset2 = violationMaskSubset | StrictMode.parseViolationFromMessage(info.crashInfo.exceptionMessage);
                int savedPolicyMask = StrictMode.getThreadPolicyMask();
                if ((info.policy & 3056) == 128) {
                    StrictMode.dropboxViolationAsync(violationMaskSubset2, info);
                    return;
                }
                try {
                    StrictMode.setThreadPolicyMask(0);
                    ActivityManagerNative.getDefault().handleApplicationStrictModeViolation(RuntimeInit.getApplicationObject(), violationMaskSubset2, info);
                } catch (RemoteException e) {
                    Log.e(StrictMode.TAG, "RemoteException trying to handle StrictMode violation", e);
                } finally {
                    StrictMode.setThreadPolicyMask(savedPolicyMask);
                }
            }
            if ((info.policy & 64) != 0) {
                StrictMode.executeDeathPenalty(info);
            }
        }
    }

    /* JADX WARN: Type inference failed for: r1v2, types: [java.lang.Throwable, android.os.StrictMode$StrictModeViolation] */
    /* access modifiers changed from: private */
    /* JADX WARNING: Unknown variable types count: 1 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void executeDeathPenalty(android.os.StrictMode.ViolationInfo r4) {
        /*
            android.app.ApplicationErrorReport$CrashInfo r1 = r4.crashInfo
            java.lang.String r1 = r1.exceptionMessage
            int r0 = parseViolationFromMessage(r1)
            android.os.StrictMode$StrictModeViolation r1 = new android.os.StrictMode$StrictModeViolation
            int r2 = r4.policy
            r3 = 0
            r1.<init>(r2, r0, r3)
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: android.os.StrictMode.executeDeathPenalty(android.os.StrictMode$ViolationInfo):void");
    }

    /* access modifiers changed from: private */
    public static void dropboxViolationAsync(final int violationMaskSubset, final ViolationInfo info) {
        int outstanding = sDropboxCallsInFlight.incrementAndGet();
        if (outstanding > 20) {
            sDropboxCallsInFlight.decrementAndGet();
            return;
        }
        if (LOG_V) {
            Log.d(TAG, "Dropboxing async; in-flight=" + outstanding);
        }
        new Thread("callActivityManagerForStrictModeDropbox") {
            /* class android.os.StrictMode.AnonymousClass5 */

            public void run() {
                Process.setThreadPriority(10);
                try {
                    IActivityManager am = ActivityManagerNative.getDefault();
                    if (am == null) {
                        Log.d(StrictMode.TAG, "No activity manager; failed to Dropbox violation.");
                    } else {
                        am.handleApplicationStrictModeViolation(RuntimeInit.getApplicationObject(), violationMaskSubset, info);
                    }
                } catch (RemoteException e) {
                    Log.e(StrictMode.TAG, "RemoteException handling StrictMode violation", e);
                }
                int outstanding = StrictMode.sDropboxCallsInFlight.decrementAndGet();
                if (StrictMode.LOG_V) {
                    Log.d(StrictMode.TAG, "Dropbox complete; in-flight=" + outstanding);
                }
            }
        }.start();
    }

    /* access modifiers changed from: private */
    public static class AndroidCloseGuardReporter implements CloseGuard.Reporter {
        private AndroidCloseGuardReporter() {
        }

        public void report(String message, Throwable allocationSite) {
            StrictMode.onVmPolicyViolation(message, allocationSite);
        }
    }

    static boolean hasGatheredViolations() {
        return gatheredViolations.get() != null;
    }

    static void clearGatheredViolations() {
        gatheredViolations.set(null);
    }

    public static void conditionallyCheckInstanceCounts() {
        VmPolicy policy = getVmPolicy();
        if (policy.classInstanceLimit.size() != 0) {
            System.gc();
            System.runFinalization();
            System.gc();
            for (Map.Entry<Class, Integer> entry : policy.classInstanceLimit.entrySet()) {
                Class klass = entry.getKey();
                int limit = entry.getValue().intValue();
                long instances = VMDebug.countInstancesOfClass(klass, false);
                if (instances > ((long) limit)) {
                    Throwable tr = new InstanceCountViolation(klass, instances, limit);
                    onVmPolicyViolation(tr.getMessage(), tr);
                }
            }
        }
    }

    public static void setVmPolicy(VmPolicy policy) {
        synchronized (StrictMode.class) {
            sVmPolicy = policy;
            sVmPolicyMask = policy.mask;
            setCloseGuardEnabled(vmClosableObjectLeaksEnabled());
            Looper looper = Looper.getMainLooper();
            if (looper != null) {
                MessageQueue mq = looper.mQueue;
                if (policy.classInstanceLimit.size() == 0 || (sVmPolicyMask & 208) == 0) {
                    mq.removeIdleHandler(sProcessIdleHandler);
                    sIsIdlerRegistered = false;
                } else if (!sIsIdlerRegistered) {
                    mq.addIdleHandler(sProcessIdleHandler);
                    sIsIdlerRegistered = true;
                }
            }
        }
    }

    public static VmPolicy getVmPolicy() {
        VmPolicy vmPolicy;
        synchronized (StrictMode.class) {
            vmPolicy = sVmPolicy;
        }
        return vmPolicy;
    }

    public static void enableDefaults() {
        setThreadPolicy(new ThreadPolicy.Builder().detectAll().penaltyLog().build());
        setVmPolicy(new VmPolicy.Builder().detectAll().penaltyLog().build());
    }

    public static boolean vmSqliteObjectLeaksEnabled() {
        return (sVmPolicyMask & 512) != 0;
    }

    public static boolean vmClosableObjectLeaksEnabled() {
        return (sVmPolicyMask & 1024) != 0;
    }

    public static boolean vmRegistrationLeaksEnabled() {
        return (sVmPolicyMask & 8192) != 0;
    }

    public static boolean vmFileUriExposureEnabled() {
        return (sVmPolicyMask & 16384) != 0;
    }

    public static void onSqliteObjectLeaked(String message, Throwable originStack) {
        onVmPolicyViolation(message, originStack);
    }

    public static void onWebViewMethodCalledOnWrongThread(Throwable originStack) {
        onVmPolicyViolation(null, originStack);
    }

    public static void onIntentReceiverLeaked(Throwable originStack) {
        onVmPolicyViolation(null, originStack);
    }

    public static void onServiceConnectionLeaked(Throwable originStack) {
        onVmPolicyViolation(null, originStack);
    }

    public static void onFileUriExposed(String location) {
        String message = "file:// Uri exposed through " + location;
        onVmPolicyViolation(message, new Throwable(message));
    }

    public static void onVmPolicyViolation(String message, Throwable originStack) {
        boolean penaltyDropbox = (sVmPolicyMask & 128) != 0;
        boolean penaltyDeath = (sVmPolicyMask & 64) != 0;
        boolean penaltyLog = (sVmPolicyMask & 16) != 0;
        ViolationInfo info = new ViolationInfo(originStack, sVmPolicyMask);
        info.numAnimationsRunning = 0;
        info.tags = null;
        info.broadcastIntentAction = null;
        Integer fingerprint = Integer.valueOf(info.hashCode());
        long now = SystemClock.uptimeMillis();
        long lastViolationTime = 0;
        long timeSinceLastViolationMillis = Long.MAX_VALUE;
        synchronized (sLastVmViolationTime) {
            if (sLastVmViolationTime.containsKey(fingerprint)) {
                lastViolationTime = sLastVmViolationTime.get(fingerprint).longValue();
                timeSinceLastViolationMillis = now - lastViolationTime;
            }
            if (timeSinceLastViolationMillis > 1000) {
                sLastVmViolationTime.put(fingerprint, Long.valueOf(now));
            }
        }
        if (penaltyLog && timeSinceLastViolationMillis > 1000) {
            Log.e(TAG, message, originStack);
        }
        int violationMaskSubset = (sVmPolicyMask & ALL_VM_DETECT_BITS) | 128;
        if (!penaltyDropbox || penaltyDeath) {
            if (penaltyDropbox && lastViolationTime == 0) {
                int savedPolicyMask = getThreadPolicyMask();
                try {
                    setThreadPolicyMask(0);
                    ActivityManagerNative.getDefault().handleApplicationStrictModeViolation(RuntimeInit.getApplicationObject(), violationMaskSubset, info);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException trying to handle StrictMode violation", e);
                } finally {
                    setThreadPolicyMask(savedPolicyMask);
                }
            }
            if (penaltyDeath) {
                System.err.println("StrictMode VmPolicy violation with POLICY_DEATH; shutting down.");
                Process.killProcess(Process.myPid());
                System.exit(10);
                return;
            }
            return;
        }
        dropboxViolationAsync(violationMaskSubset, info);
    }

    static void writeGatheredViolationsToParcel(Parcel p) {
        ArrayList<ViolationInfo> violations = gatheredViolations.get();
        if (violations == null) {
            p.writeInt(0);
        } else {
            p.writeInt(violations.size());
            for (int i = 0; i < violations.size(); i++) {
                int start = p.dataPosition();
                violations.get(i).writeToParcel(p, 0);
                if (p.dataPosition() - start > 10240) {
                    Slog.d(TAG, "Wrote violation #" + i + " of " + violations.size() + ": " + (p.dataPosition() - start) + " bytes");
                }
            }
            if (LOG_V) {
                Log.d(TAG, "wrote violations to response parcel; num=" + violations.size());
            }
            violations.clear();
        }
        gatheredViolations.set(null);
    }

    /* access modifiers changed from: private */
    public static class LogStackTrace extends Exception {
        private LogStackTrace() {
        }
    }

    static void readAndHandleBinderCallViolations(Parcel p) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new FastPrintWriter(sw, false, 256);
        new LogStackTrace().printStackTrace(pw);
        pw.flush();
        String ourStack = sw.toString();
        int policyMask = getThreadPolicyMask();
        boolean currentlyGathering = (policyMask & 256) != 0;
        int numViolations = p.readInt();
        int i = 0;
        while (i < numViolations) {
            if (LOG_V) {
                Log.d(TAG, "strict mode violation stacks read from binder call.  i=" + i);
            }
            ViolationInfo info = new ViolationInfo(p, !currentlyGathering);
            if (info.crashInfo.stackTrace == null || info.crashInfo.stackTrace.length() <= 10000) {
                StringBuilder sb = new StringBuilder();
                ApplicationErrorReport.CrashInfo crashInfo = info.crashInfo;
                crashInfo.stackTrace = sb.append(crashInfo.stackTrace).append("# via Binder call with stack:\n").append(ourStack).toString();
                BlockGuard.Policy policy = BlockGuard.getThreadPolicy();
                if (policy instanceof AndroidBlockGuardPolicy) {
                    ((AndroidBlockGuardPolicy) policy).handleViolationWithTimingAttempt(info);
                }
                i++;
            } else {
                String front = info.crashInfo.stackTrace.substring(256);
                while (i < numViolations) {
                    new ViolationInfo(p, !currentlyGathering);
                    i++;
                }
                clearGatheredViolations();
                Slog.wtfStack(TAG, "Stack is too large: numViolations=" + numViolations + " policy=#" + Integer.toHexString(policyMask) + " front=" + front);
                return;
            }
        }
    }

    private static void onBinderStrictModePolicyChange(int newPolicy) {
        setBlockGuardPolicy(newPolicy);
    }

    public static class Span {
        private final ThreadSpanState mContainerState;
        private long mCreateMillis;
        private String mName;
        private Span mNext;
        private Span mPrev;

        Span(ThreadSpanState threadState) {
            this.mContainerState = threadState;
        }

        protected Span() {
            this.mContainerState = null;
        }

        public void finish() {
            ThreadSpanState state = this.mContainerState;
            synchronized (state) {
                if (this.mName != null) {
                    if (this.mPrev != null) {
                        this.mPrev.mNext = this.mNext;
                    }
                    if (this.mNext != null) {
                        this.mNext.mPrev = this.mPrev;
                    }
                    if (state.mActiveHead == this) {
                        state.mActiveHead = this.mNext;
                    }
                    state.mActiveSize--;
                    if (StrictMode.LOG_V) {
                        Log.d(StrictMode.TAG, "Span finished=" + this.mName + "; size=" + state.mActiveSize);
                    }
                    this.mCreateMillis = -1;
                    this.mName = null;
                    this.mPrev = null;
                    this.mNext = null;
                    if (state.mFreeListSize < 5) {
                        this.mNext = state.mFreeListHead;
                        state.mFreeListHead = this;
                        state.mFreeListSize++;
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static class ThreadSpanState {
        public Span mActiveHead;
        public int mActiveSize;
        public Span mFreeListHead;
        public int mFreeListSize;

        private ThreadSpanState() {
        }
    }

    public static Span enterCriticalSpan(String name) {
        Span span;
        if (IS_USER_BUILD) {
            return NO_OP_SPAN;
        }
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("name must be non-null and non-empty");
        }
        ThreadSpanState state = sThisThreadSpanState.get();
        synchronized (state) {
            if (state.mFreeListHead != null) {
                span = state.mFreeListHead;
                state.mFreeListHead = span.mNext;
                state.mFreeListSize--;
            } else {
                span = new Span(state);
            }
            span.mName = name;
            span.mCreateMillis = SystemClock.uptimeMillis();
            span.mNext = state.mActiveHead;
            span.mPrev = null;
            state.mActiveHead = span;
            state.mActiveSize++;
            if (span.mNext != null) {
                span.mNext.mPrev = span;
            }
            if (LOG_V) {
                Log.d(TAG, "Span enter=" + name + "; size=" + state.mActiveSize);
            }
        }
        return span;
    }

    public static void noteSlowCall(String name) {
        BlockGuard.Policy policy = BlockGuard.getThreadPolicy();
        if (policy instanceof AndroidBlockGuardPolicy) {
            ((AndroidBlockGuardPolicy) policy).onCustomSlowCall(name);
        }
    }

    public static void noteDiskRead() {
        BlockGuard.Policy policy = BlockGuard.getThreadPolicy();
        if (policy instanceof AndroidBlockGuardPolicy) {
            ((AndroidBlockGuardPolicy) policy).onReadFromDisk();
        }
    }

    public static void noteDiskWrite() {
        BlockGuard.Policy policy = BlockGuard.getThreadPolicy();
        if (policy instanceof AndroidBlockGuardPolicy) {
            ((AndroidBlockGuardPolicy) policy).onWriteToDisk();
        }
    }

    public static Object trackActivity(Object instance) {
        return new InstanceTracker(instance);
    }

    public static void incrementExpectedActivityCount(Class klass) {
        if (klass != null) {
            synchronized (StrictMode.class) {
                if ((sVmPolicy.mask & 2048) != 0) {
                    Integer expected = sExpectedActivityInstanceCount.get(klass);
                    sExpectedActivityInstanceCount.put(klass, Integer.valueOf(expected == null ? 1 : expected.intValue() + 1));
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0033, code lost:
        if (android.os.StrictMode.InstanceTracker.getInstanceCount(r11) <= r4) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0035, code lost:
        java.lang.System.gc();
        java.lang.System.runFinalization();
        java.lang.System.gc();
        r2 = dalvik.system.VMDebug.countInstancesOfClass(r11, false);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0045, code lost:
        if (r2 <= ((long) r4)) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0047, code lost:
        r6 = new android.os.StrictMode.InstanceCountViolation(r11, r2, r4);
        onVmPolicyViolation(r6.getMessage(), r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:?, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void decrementExpectedActivityCount(java.lang.Class r11) {
        /*
        // Method dump skipped, instructions count: 101
        */
        throw new UnsupportedOperationException("Method not decompiled: android.os.StrictMode.decrementExpectedActivityCount(java.lang.Class):void");
    }

    public static class ViolationInfo {
        public String broadcastIntentAction;
        public final ApplicationErrorReport.CrashInfo crashInfo;
        public int durationMillis;
        public int numAnimationsRunning;
        public long numInstances;
        public final int policy;
        public String[] tags;
        public int violationNumThisLoop;
        public long violationUptimeMillis;

        public ViolationInfo() {
            this.durationMillis = -1;
            this.numAnimationsRunning = 0;
            this.numInstances = -1;
            this.crashInfo = null;
            this.policy = 0;
        }

        public ViolationInfo(Throwable tr, int policy2) {
            this.durationMillis = -1;
            this.numAnimationsRunning = 0;
            this.numInstances = -1;
            this.crashInfo = new ApplicationErrorReport.CrashInfo(tr);
            this.violationUptimeMillis = SystemClock.uptimeMillis();
            this.policy = policy2;
            this.numAnimationsRunning = ValueAnimator.getCurrentAnimationsCount();
            Intent broadcastIntent = ActivityThread.getIntentBeingBroadcast();
            if (broadcastIntent != null) {
                this.broadcastIntentAction = broadcastIntent.getAction();
            }
            ThreadSpanState state = (ThreadSpanState) StrictMode.sThisThreadSpanState.get();
            if (tr instanceof InstanceCountViolation) {
                this.numInstances = ((InstanceCountViolation) tr).mInstances;
            }
            synchronized (state) {
                int spanActiveCount = state.mActiveSize;
                spanActiveCount = spanActiveCount > 20 ? 20 : spanActiveCount;
                if (spanActiveCount != 0) {
                    this.tags = new String[spanActiveCount];
                    int index = 0;
                    for (Span iter = state.mActiveHead; iter != null && index < spanActiveCount; iter = iter.mNext) {
                        this.tags[index] = iter.mName;
                        index++;
                    }
                }
            }
        }

        public int hashCode() {
            int result = this.crashInfo.stackTrace.hashCode() + 629;
            if (this.numAnimationsRunning != 0) {
                result *= 37;
            }
            if (this.broadcastIntentAction != null) {
                result = (result * 37) + this.broadcastIntentAction.hashCode();
            }
            if (this.tags != null) {
                for (String tag : this.tags) {
                    result = (result * 37) + tag.hashCode();
                }
            }
            return result;
        }

        public ViolationInfo(Parcel in) {
            this(in, false);
        }

        public ViolationInfo(Parcel in, boolean unsetGatheringBit) {
            this.durationMillis = -1;
            this.numAnimationsRunning = 0;
            this.numInstances = -1;
            this.crashInfo = new ApplicationErrorReport.CrashInfo(in);
            int rawPolicy = in.readInt();
            if (unsetGatheringBit) {
                this.policy = rawPolicy & -257;
            } else {
                this.policy = rawPolicy;
            }
            this.durationMillis = in.readInt();
            this.violationNumThisLoop = in.readInt();
            this.numAnimationsRunning = in.readInt();
            this.violationUptimeMillis = in.readLong();
            this.numInstances = in.readLong();
            this.broadcastIntentAction = in.readString();
            this.tags = in.readStringArray();
        }

        public void writeToParcel(Parcel dest, int flags) {
            this.crashInfo.writeToParcel(dest, flags);
            int start = dest.dataPosition();
            dest.writeInt(this.policy);
            dest.writeInt(this.durationMillis);
            dest.writeInt(this.violationNumThisLoop);
            dest.writeInt(this.numAnimationsRunning);
            dest.writeLong(this.violationUptimeMillis);
            dest.writeLong(this.numInstances);
            dest.writeString(this.broadcastIntentAction);
            dest.writeStringArray(this.tags);
            if (dest.dataPosition() - start > 10240) {
                Slog.d(StrictMode.TAG, "VIO: policy=" + this.policy + " dur=" + this.durationMillis + " numLoop=" + this.violationNumThisLoop + " anim=" + this.numAnimationsRunning + " uptime=" + this.violationUptimeMillis + " numInst=" + this.numInstances);
                Slog.d(StrictMode.TAG, "VIO: action=" + this.broadcastIntentAction);
                Slog.d(StrictMode.TAG, "VIO: tags=" + Arrays.toString(this.tags));
                Slog.d(StrictMode.TAG, "VIO: TOTAL BYTES WRITTEN: " + (dest.dataPosition() - start));
            }
        }

        public void dump(Printer pw, String prefix) {
            this.crashInfo.dump(pw, prefix);
            pw.println(prefix + "policy: " + this.policy);
            if (this.durationMillis != -1) {
                pw.println(prefix + "durationMillis: " + this.durationMillis);
            }
            if (this.numInstances != -1) {
                pw.println(prefix + "numInstances: " + this.numInstances);
            }
            if (this.violationNumThisLoop != 0) {
                pw.println(prefix + "violationNumThisLoop: " + this.violationNumThisLoop);
            }
            if (this.numAnimationsRunning != 0) {
                pw.println(prefix + "numAnimationsRunning: " + this.numAnimationsRunning);
            }
            pw.println(prefix + "violationUptimeMillis: " + this.violationUptimeMillis);
            if (this.broadcastIntentAction != null) {
                pw.println(prefix + "broadcastIntentAction: " + this.broadcastIntentAction);
            }
            if (this.tags != null) {
                String[] arr$ = this.tags;
                int len$ = arr$.length;
                int i$ = 0;
                int index = 0;
                while (i$ < len$) {
                    pw.println(prefix + "tag[" + index + "]: " + arr$[i$]);
                    i$++;
                    index++;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static class InstanceCountViolation extends Throwable {
        private static final StackTraceElement[] FAKE_STACK = {new StackTraceElement("android.os.StrictMode", "setClassInstanceLimit", "StrictMode.java", 1)};
        final Class mClass;
        final long mInstances;
        final int mLimit;

        public InstanceCountViolation(Class klass, long instances, int limit) {
            super(klass.toString() + "; instances=" + instances + "; limit=" + limit);
            setStackTrace(FAKE_STACK);
            this.mClass = klass;
            this.mInstances = instances;
            this.mLimit = limit;
        }
    }

    /* access modifiers changed from: private */
    public static final class InstanceTracker {
        private static final HashMap<Class<?>, Integer> sInstanceCounts = new HashMap<>();
        private final Class<?> mKlass;

        public InstanceTracker(Object instance) {
            this.mKlass = instance.getClass();
            synchronized (sInstanceCounts) {
                Integer value = sInstanceCounts.get(this.mKlass);
                sInstanceCounts.put(this.mKlass, Integer.valueOf(value != null ? value.intValue() + 1 : 1));
            }
        }

        /* access modifiers changed from: protected */
        public void finalize() throws Throwable {
            try {
                synchronized (sInstanceCounts) {
                    Integer value = sInstanceCounts.get(this.mKlass);
                    if (value != null) {
                        int newValue = value.intValue() - 1;
                        if (newValue > 0) {
                            sInstanceCounts.put(this.mKlass, Integer.valueOf(newValue));
                        } else {
                            sInstanceCounts.remove(this.mKlass);
                        }
                    }
                }
            } finally {
                super.finalize();
            }
        }

        public static int getInstanceCount(Class<?> klass) {
            int intValue;
            synchronized (sInstanceCounts) {
                Integer value = sInstanceCounts.get(klass);
                intValue = value != null ? value.intValue() : 0;
            }
            return intValue;
        }
    }
}

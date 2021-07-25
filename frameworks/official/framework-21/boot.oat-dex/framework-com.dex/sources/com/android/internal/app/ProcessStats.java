package com.android.internal.app;

import android.accounts.GrantCredentialsPermissionActivity;
import android.app.Notification;
import android.app.backup.FullBackup;
import android.content.Context;
import android.hardware.Camera;
import android.media.TtmlUtils;
import android.net.ProxyInfo;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.CalendarContract;
import android.text.format.DateFormat;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.util.TimeUtils;
import com.android.internal.content.NativeLibraryHelper;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.util.GrowingArrayUtils;
import dalvik.system.VMRuntime;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;
import libcore.util.EmptyArray;

public final class ProcessStats implements Parcelable {
    public static final int ADJ_COUNT = 8;
    public static final int ADJ_MEM_FACTOR_COUNT = 4;
    public static final int ADJ_MEM_FACTOR_CRITICAL = 3;
    public static final int ADJ_MEM_FACTOR_LOW = 2;
    public static final int ADJ_MEM_FACTOR_MODERATE = 1;
    public static final int ADJ_MEM_FACTOR_NORMAL = 0;
    public static final String[] ADJ_MEM_NAMES_CSV = {"norm", "mod", "low", "crit"};
    static final String[] ADJ_MEM_TAGS = {"n", "m", "l", FullBackup.CACHE_TREE_TOKEN};
    public static final int ADJ_NOTHING = -1;
    public static final int ADJ_SCREEN_MOD = 4;
    public static final String[] ADJ_SCREEN_NAMES_CSV = {"off", Camera.Parameters.FLASH_MODE_ON};
    public static final int ADJ_SCREEN_OFF = 0;
    public static final int ADJ_SCREEN_ON = 4;
    static final String[] ADJ_SCREEN_TAGS = {WifiEnterpriseConfig.ENGINE_DISABLE, WifiEnterpriseConfig.ENGINE_ENABLE};
    public static final int[] ALL_MEM_ADJ = {0, 1, 2, 3};
    public static final int[] ALL_PROC_STATES = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13};
    public static final int[] ALL_SCREEN_ADJ = {0, 4};
    public static final int[] BACKGROUND_PROC_STATES = {2, 3, 4, 5, 6, 7, 8};
    static final int[] BAD_TABLE = new int[0];
    public static long COMMIT_PERIOD = 10800000;
    public static long COMMIT_UPTIME_PERIOD = 3600000;
    public static final Parcelable.Creator<ProcessStats> CREATOR = new Parcelable.Creator<ProcessStats>() {
        /* class com.android.internal.app.ProcessStats.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ProcessStats createFromParcel(Parcel in) {
            return new ProcessStats(in);
        }

        @Override // android.os.Parcelable.Creator
        public ProcessStats[] newArray(int size) {
            return new ProcessStats[size];
        }
    };
    static final String CSV_SEP = "\t";
    static final boolean DEBUG = false;
    static final boolean DEBUG_PARCEL = false;
    public static final int FLAG_COMPLETE = 1;
    public static final int FLAG_SHUTDOWN = 2;
    public static final int FLAG_SYSPROPS = 4;
    static final int LONGS_SIZE = 4096;
    private static final int MAGIC = 1347638355;
    public static final int[] NON_CACHED_PROC_STATES = {0, 1, 2, 3, 4, 5, 6, 7, 8};
    static int OFFSET_ARRAY_MASK = 255;
    static int OFFSET_ARRAY_SHIFT = 8;
    static int OFFSET_INDEX_MASK = 65535;
    static int OFFSET_INDEX_SHIFT = 16;
    static int OFFSET_TYPE_MASK = 255;
    static int OFFSET_TYPE_SHIFT = 0;
    private static final int PARCEL_VERSION = 18;
    static final int[] PROCESS_STATE_TO_STATE = {0, 0, 1, 2, 3, 4, 5, 6, 8, 9, 10, 11, 12, 13};
    public static final int PSS_AVERAGE = 2;
    public static final int PSS_COUNT = 7;
    public static final int PSS_MAXIMUM = 3;
    public static final int PSS_MINIMUM = 1;
    public static final int PSS_SAMPLE_COUNT = 0;
    public static final int PSS_USS_AVERAGE = 5;
    public static final int PSS_USS_MAXIMUM = 6;
    public static final int PSS_USS_MINIMUM = 4;
    public static final String SERVICE_NAME = "procstats";
    public static final int STATE_BACKUP = 4;
    public static final int STATE_CACHED_ACTIVITY = 11;
    public static final int STATE_CACHED_ACTIVITY_CLIENT = 12;
    public static final int STATE_CACHED_EMPTY = 13;
    public static final int STATE_COUNT = 14;
    public static final int STATE_HEAVY_WEIGHT = 5;
    public static final int STATE_HOME = 9;
    public static final int STATE_IMPORTANT_BACKGROUND = 3;
    public static final int STATE_IMPORTANT_FOREGROUND = 2;
    public static final int STATE_LAST_ACTIVITY = 10;
    static final String[] STATE_NAMES = {"Persist", "Top    ", "ImpFg  ", "ImpBg  ", "Backup ", "HeavyWt", "Service", "ServRst", "Receivr", "Home   ", "LastAct", "CchAct ", "CchCAct", "CchEmty"};
    public static final String[] STATE_NAMES_CSV = {"pers", "top", "impfg", "impbg", Context.BACKUP_SERVICE, "heavy", Notification.CATEGORY_SERVICE, "service-rs", "receiver", CalendarContract.CalendarCache.TIMEZONE_TYPE_HOME, "lastact", "cch-activity", "cch-aclient", "cch-empty"};
    public static final int STATE_NOTHING = -1;
    public static final int STATE_PERSISTENT = 0;
    public static final int STATE_RECEIVER = 8;
    public static final int STATE_SERVICE = 6;
    public static final int STATE_SERVICE_RESTARTING = 7;
    static final String[] STATE_TAGS = {TtmlUtils.TAG_P, "t", FullBackup.DATA_TREE_TOKEN, "b", "u", "w", "s", "x", FullBackup.ROOT_TREE_TOKEN, "h", "l", FullBackup.APK_TREE_TOKEN, FullBackup.CACHE_TREE_TOKEN, "e"};
    public static final int STATE_TOP = 1;
    public static final int SYS_MEM_USAGE_CACHED_AVERAGE = 2;
    public static final int SYS_MEM_USAGE_CACHED_MAXIMUM = 3;
    public static final int SYS_MEM_USAGE_CACHED_MINIMUM = 1;
    public static final int SYS_MEM_USAGE_COUNT = 16;
    public static final int SYS_MEM_USAGE_FREE_AVERAGE = 5;
    public static final int SYS_MEM_USAGE_FREE_MAXIMUM = 6;
    public static final int SYS_MEM_USAGE_FREE_MINIMUM = 4;
    public static final int SYS_MEM_USAGE_KERNEL_AVERAGE = 11;
    public static final int SYS_MEM_USAGE_KERNEL_MAXIMUM = 12;
    public static final int SYS_MEM_USAGE_KERNEL_MINIMUM = 10;
    public static final int SYS_MEM_USAGE_NATIVE_AVERAGE = 14;
    public static final int SYS_MEM_USAGE_NATIVE_MAXIMUM = 15;
    public static final int SYS_MEM_USAGE_NATIVE_MINIMUM = 13;
    public static final int SYS_MEM_USAGE_SAMPLE_COUNT = 0;
    public static final int SYS_MEM_USAGE_ZRAM_AVERAGE = 8;
    public static final int SYS_MEM_USAGE_ZRAM_MAXIMUM = 9;
    public static final int SYS_MEM_USAGE_ZRAM_MINIMUM = 7;
    static final String TAG = "ProcessStats";
    int[] mAddLongTable;
    int mAddLongTableSize;
    ArrayMap<String, Integer> mCommonStringToIndex;
    public int mFlags;
    ArrayList<String> mIndexToCommonString;
    final ArrayList<long[]> mLongs;
    public int mMemFactor;
    public final long[] mMemFactorDurations;
    int mNextLong;
    public final ProcessMap<SparseArray<PackageState>> mPackages;
    public final ProcessMap<ProcessState> mProcesses;
    public String mReadError;
    boolean mRunning;
    String mRuntime;
    public long mStartTime;
    public final long[] mSysMemUsageArgs;
    public int[] mSysMemUsageTable;
    public int mSysMemUsageTableSize;
    public long mTimePeriodEndRealtime;
    public long mTimePeriodEndUptime;
    public long mTimePeriodStartClock;
    public String mTimePeriodStartClockStr;
    public long mTimePeriodStartRealtime;
    public long mTimePeriodStartUptime;

    public ProcessStats(boolean running) {
        this.mPackages = new ProcessMap<>();
        this.mProcesses = new ProcessMap<>();
        this.mMemFactorDurations = new long[8];
        this.mMemFactor = -1;
        this.mSysMemUsageTable = null;
        this.mSysMemUsageTableSize = 0;
        this.mSysMemUsageArgs = new long[16];
        this.mLongs = new ArrayList<>();
        this.mRunning = running;
        reset();
    }

    public ProcessStats(Parcel in) {
        this.mPackages = new ProcessMap<>();
        this.mProcesses = new ProcessMap<>();
        this.mMemFactorDurations = new long[8];
        this.mMemFactor = -1;
        this.mSysMemUsageTable = null;
        this.mSysMemUsageTableSize = 0;
        this.mSysMemUsageArgs = new long[16];
        this.mLongs = new ArrayList<>();
        reset();
        readFromParcel(in);
    }

    public void add(ProcessStats other) {
        ArrayMap<String, SparseArray<SparseArray<PackageState>>> pkgMap = other.mPackages.getMap();
        for (int ip = 0; ip < pkgMap.size(); ip++) {
            String pkgName = pkgMap.keyAt(ip);
            SparseArray<SparseArray<PackageState>> uids = pkgMap.valueAt(ip);
            for (int iu = 0; iu < uids.size(); iu++) {
                int uid = uids.keyAt(iu);
                SparseArray<PackageState> versions = uids.valueAt(iu);
                for (int iv = 0; iv < versions.size(); iv++) {
                    int vers = versions.keyAt(iv);
                    PackageState otherState = versions.valueAt(iv);
                    int NPROCS = otherState.mProcesses.size();
                    int NSRVS = otherState.mServices.size();
                    for (int iproc = 0; iproc < NPROCS; iproc++) {
                        ProcessState otherProc = otherState.mProcesses.valueAt(iproc);
                        if (otherProc.mCommonProcess != otherProc) {
                            ProcessState thisProc = getProcessStateLocked(pkgName, uid, vers, otherProc.mName);
                            if (thisProc.mCommonProcess == thisProc) {
                                thisProc.mMultiPackage = true;
                                long now = SystemClock.uptimeMillis();
                                PackageState pkgState = getPackageStateLocked(pkgName, uid, vers);
                                thisProc = thisProc.clone(thisProc.mPackage, now);
                                pkgState.mProcesses.put(thisProc.mName, thisProc);
                            }
                            thisProc.add(otherProc);
                        }
                    }
                    for (int isvc = 0; isvc < NSRVS; isvc++) {
                        ServiceState otherSvc = otherState.mServices.valueAt(isvc);
                        getServiceStateLocked(pkgName, uid, vers, otherSvc.mProcessName, otherSvc.mName).add(otherSvc);
                    }
                }
            }
        }
        ArrayMap<String, SparseArray<ProcessState>> procMap = other.mProcesses.getMap();
        for (int ip2 = 0; ip2 < procMap.size(); ip2++) {
            SparseArray<ProcessState> uids2 = procMap.valueAt(ip2);
            for (int iu2 = 0; iu2 < uids2.size(); iu2++) {
                int uid2 = uids2.keyAt(iu2);
                ProcessState otherProc2 = uids2.valueAt(iu2);
                ProcessState thisProc2 = this.mProcesses.get(otherProc2.mName, uid2);
                if (thisProc2 == null) {
                    thisProc2 = new ProcessState(this, otherProc2.mPackage, uid2, otherProc2.mVersion, otherProc2.mName);
                    this.mProcesses.put(otherProc2.mName, uid2, thisProc2);
                    PackageState thisState = getPackageStateLocked(otherProc2.mPackage, uid2, otherProc2.mVersion);
                    if (!thisState.mProcesses.containsKey(otherProc2.mName)) {
                        thisState.mProcesses.put(otherProc2.mName, thisProc2);
                    }
                }
                thisProc2.add(otherProc2);
            }
        }
        for (int i = 0; i < 8; i++) {
            long[] jArr = this.mMemFactorDurations;
            jArr[i] = jArr[i] + other.mMemFactorDurations[i];
        }
        for (int i2 = 0; i2 < other.mSysMemUsageTableSize; i2++) {
            int ent = other.mSysMemUsageTable[i2];
            addSysMemUsage((ent >> OFFSET_TYPE_SHIFT) & OFFSET_TYPE_MASK, other.mLongs.get((ent >> OFFSET_ARRAY_SHIFT) & OFFSET_ARRAY_MASK), (ent >> OFFSET_INDEX_SHIFT) & OFFSET_INDEX_MASK);
        }
        if (other.mTimePeriodStartClock < this.mTimePeriodStartClock) {
            this.mTimePeriodStartClock = other.mTimePeriodStartClock;
            this.mTimePeriodStartClockStr = other.mTimePeriodStartClockStr;
        }
        this.mTimePeriodEndRealtime += other.mTimePeriodEndRealtime - other.mTimePeriodStartRealtime;
        this.mTimePeriodEndUptime += other.mTimePeriodEndUptime - other.mTimePeriodStartUptime;
    }

    public void addSysMemUsage(long cachedMem, long freeMem, long zramMem, long kernelMem, long nativeMem) {
        if (this.mMemFactor != -1) {
            int state = this.mMemFactor * 14;
            this.mSysMemUsageArgs[0] = 1;
            for (int i = 0; i < 3; i++) {
                this.mSysMemUsageArgs[i + 1] = cachedMem;
                this.mSysMemUsageArgs[i + 4] = freeMem;
                this.mSysMemUsageArgs[i + 7] = zramMem;
                this.mSysMemUsageArgs[i + 10] = kernelMem;
                this.mSysMemUsageArgs[i + 13] = nativeMem;
            }
            addSysMemUsage(state, this.mSysMemUsageArgs, 0);
        }
    }

    /* access modifiers changed from: package-private */
    public void addSysMemUsage(int state, long[] data, int dataOff) {
        int off;
        int idx = binarySearch(this.mSysMemUsageTable, this.mSysMemUsageTableSize, state);
        if (idx >= 0) {
            off = this.mSysMemUsageTable[idx];
        } else {
            this.mAddLongTable = this.mSysMemUsageTable;
            this.mAddLongTableSize = this.mSysMemUsageTableSize;
            off = addLongData(idx ^ -1, state, 16);
            this.mSysMemUsageTable = this.mAddLongTable;
            this.mSysMemUsageTableSize = this.mAddLongTableSize;
        }
        addSysMemUsage(this.mLongs.get((off >> OFFSET_ARRAY_SHIFT) & OFFSET_ARRAY_MASK), (off >> OFFSET_INDEX_SHIFT) & OFFSET_INDEX_MASK, data, dataOff);
    }

    static void addSysMemUsage(long[] dstData, int dstOff, long[] addData, int addOff) {
        long dstCount = dstData[dstOff + 0];
        long addCount = addData[addOff + 0];
        if (dstCount == 0) {
            dstData[dstOff + 0] = addCount;
            for (int i = 1; i < 16; i++) {
                dstData[dstOff + i] = addData[addOff + i];
            }
        } else if (addCount > 0) {
            dstData[dstOff + 0] = dstCount + addCount;
            for (int i2 = 1; i2 < 16; i2 += 3) {
                if (dstData[dstOff + i2] > addData[addOff + i2]) {
                    dstData[dstOff + i2] = addData[addOff + i2];
                }
                dstData[dstOff + i2 + 1] = (long) (((((double) dstData[(dstOff + i2) + 1]) * ((double) dstCount)) + (((double) addData[(addOff + i2) + 1]) * ((double) addCount))) / ((double) (dstCount + addCount)));
                if (dstData[dstOff + i2 + 2] < addData[addOff + i2 + 2]) {
                    dstData[dstOff + i2 + 2] = addData[addOff + i2 + 2];
                }
            }
        }
    }

    private static void printScreenLabel(PrintWriter pw, int offset) {
        switch (offset) {
            case -1:
                pw.print("     ");
                return;
            case 0:
                pw.print("SOff/");
                return;
            case 1:
            case 2:
            case 3:
            default:
                pw.print("????/");
                return;
            case 4:
                pw.print("SOn /");
                return;
        }
    }

    public static void printScreenLabelCsv(PrintWriter pw, int offset) {
        switch (offset) {
            case -1:
                return;
            case 0:
                pw.print(ADJ_SCREEN_NAMES_CSV[0]);
                return;
            case 1:
            case 2:
            case 3:
            default:
                pw.print("???");
                return;
            case 4:
                pw.print(ADJ_SCREEN_NAMES_CSV[1]);
                return;
        }
    }

    private static void printMemLabel(PrintWriter pw, int offset, char sep) {
        switch (offset) {
            case -1:
                pw.print("    ");
                if (sep != 0) {
                    pw.print(' ');
                    return;
                }
                return;
            case 0:
                pw.print("Norm");
                if (sep != 0) {
                    pw.print(sep);
                    return;
                }
                return;
            case 1:
                pw.print("Mod ");
                if (sep != 0) {
                    pw.print(sep);
                    return;
                }
                return;
            case 2:
                pw.print("Low ");
                if (sep != 0) {
                    pw.print(sep);
                    return;
                }
                return;
            case 3:
                pw.print("Crit");
                if (sep != 0) {
                    pw.print(sep);
                    return;
                }
                return;
            default:
                pw.print("????");
                if (sep != 0) {
                    pw.print(sep);
                    return;
                }
                return;
        }
    }

    public static void printMemLabelCsv(PrintWriter pw, int offset) {
        if (offset < 0) {
            return;
        }
        if (offset <= 3) {
            pw.print(ADJ_MEM_NAMES_CSV[offset]);
        } else {
            pw.print("???");
        }
    }

    public static long dumpSingleTime(PrintWriter pw, String prefix, long[] durations, int curState, long curStartTime, long now) {
        long totalTime = 0;
        int printedScreen = -1;
        int iscreen = 0;
        while (iscreen < 8) {
            int printedMem = -1;
            int imem = 0;
            while (imem < 4) {
                int state = imem + iscreen;
                long time = durations[state];
                String running = ProxyInfo.LOCAL_EXCL_LIST;
                if (curState == state) {
                    time += now - curStartTime;
                    if (pw != null) {
                        running = " (running)";
                    }
                }
                if (time != 0) {
                    if (pw != null) {
                        pw.print(prefix);
                        printScreenLabel(pw, printedScreen != iscreen ? iscreen : -1);
                        printedScreen = iscreen;
                        printMemLabel(pw, printedMem != imem ? imem : -1, 0);
                        printedMem = imem;
                        pw.print(": ");
                        TimeUtils.formatDuration(time, pw);
                        pw.println(running);
                    }
                    totalTime += time;
                }
                imem++;
            }
            iscreen += 4;
        }
        if (!(totalTime == 0 || pw == null)) {
            pw.print(prefix);
            pw.print("    TOTAL: ");
            TimeUtils.formatDuration(totalTime, pw);
            pw.println();
        }
        return totalTime;
    }

    static void dumpAdjTimesCheckin(PrintWriter pw, String sep, long[] durations, int curState, long curStartTime, long now) {
        for (int iscreen = 0; iscreen < 8; iscreen += 4) {
            for (int imem = 0; imem < 4; imem++) {
                int state = imem + iscreen;
                long time = durations[state];
                if (curState == state) {
                    time += now - curStartTime;
                }
                if (time != 0) {
                    printAdjTagAndValue(pw, state, time);
                }
            }
        }
    }

    static void dumpServiceTimeCheckin(PrintWriter pw, String label, String packageName, int uid, int vers, String serviceName, ServiceState svc, int serviceType, int opCount, int curState, long curStartTime, long now) {
        if (opCount > 0) {
            pw.print(label);
            pw.print(",");
            pw.print(packageName);
            pw.print(",");
            pw.print(uid);
            pw.print(",");
            pw.print(vers);
            pw.print(",");
            pw.print(serviceName);
            pw.print(",");
            pw.print(opCount);
            boolean didCurState = false;
            for (int i = 0; i < svc.mDurationsTableSize; i++) {
                int off = svc.mDurationsTable[i];
                int type = (off >> OFFSET_TYPE_SHIFT) & OFFSET_TYPE_MASK;
                int memFactor = type / 4;
                if (type % 4 == serviceType) {
                    long time = svc.mStats.getLong(off, 0);
                    if (curState == memFactor) {
                        didCurState = true;
                        time += now - curStartTime;
                    }
                    printAdjTagAndValue(pw, memFactor, time);
                }
            }
            if (!didCurState && curState != -1) {
                printAdjTagAndValue(pw, curState, now - curStartTime);
            }
            pw.println();
        }
    }

    public static void computeProcessData(ProcessState proc, ProcessDataCollection data, long now) {
        data.totalTime = 0;
        data.maxUss = 0;
        data.avgUss = 0;
        data.minUss = 0;
        data.maxPss = 0;
        data.avgPss = 0;
        data.minPss = 0;
        data.numPss = 0;
        for (int is = 0; is < data.screenStates.length; is++) {
            for (int im = 0; im < data.memStates.length; im++) {
                for (int ip = 0; ip < data.procStates.length; ip++) {
                    int bucket = ((data.screenStates[is] + data.memStates[im]) * 14) + data.procStates[ip];
                    data.totalTime += proc.getDuration(bucket, now);
                    long samples = proc.getPssSampleCount(bucket);
                    if (samples > 0) {
                        long minPss = proc.getPssMinimum(bucket);
                        long avgPss = proc.getPssAverage(bucket);
                        long maxPss = proc.getPssMaximum(bucket);
                        long minUss = proc.getPssUssMinimum(bucket);
                        long avgUss = proc.getPssUssAverage(bucket);
                        long maxUss = proc.getPssUssMaximum(bucket);
                        if (data.numPss == 0) {
                            data.minPss = minPss;
                            data.avgPss = avgPss;
                            data.maxPss = maxPss;
                            data.minUss = minUss;
                            data.avgUss = avgUss;
                            data.maxUss = maxUss;
                        } else {
                            if (minPss < data.minPss) {
                                data.minPss = minPss;
                            }
                            data.avgPss = (long) (((((double) data.avgPss) * ((double) data.numPss)) + (((double) avgPss) * ((double) samples))) / ((double) (data.numPss + samples)));
                            if (maxPss > data.maxPss) {
                                data.maxPss = maxPss;
                            }
                            if (minUss < data.minUss) {
                                data.minUss = minUss;
                            }
                            data.avgUss = (long) (((((double) data.avgUss) * ((double) data.numPss)) + (((double) avgUss) * ((double) samples))) / ((double) (data.numPss + samples)));
                            if (maxUss > data.maxUss) {
                                data.maxUss = maxUss;
                            }
                        }
                        data.numPss += samples;
                    }
                }
            }
        }
    }

    static long computeProcessTimeLocked(ProcessState proc, int[] screenStates, int[] memStates, int[] procStates, long now) {
        long totalTime = 0;
        for (int is = 0; is < screenStates.length; is++) {
            for (int im = 0; im < memStates.length; im++) {
                for (int i : procStates) {
                    totalTime += proc.getDuration(((screenStates[is] + memStates[im]) * 14) + i, now);
                }
            }
        }
        proc.mTmpTotalTime = totalTime;
        return totalTime;
    }

    /* access modifiers changed from: package-private */
    public static class PssAggr {
        long pss = 0;
        long samples = 0;

        PssAggr() {
        }

        /* access modifiers changed from: package-private */
        public void add(long newPss, long newSamples) {
            this.pss = ((long) ((((double) this.pss) * ((double) this.samples)) + (((double) newPss) * ((double) newSamples)))) / (this.samples + newSamples);
            this.samples += newSamples;
        }
    }

    public void computeTotalMemoryUse(TotalMemoryUseCollection data, long now) {
        long avg;
        data.totalTime = 0;
        for (int i = 0; i < 14; i++) {
            data.processStateWeight[i] = 0.0d;
            data.processStatePss[i] = 0;
            data.processStateTime[i] = 0;
            data.processStateSamples[i] = 0;
        }
        for (int i2 = 0; i2 < 16; i2++) {
            data.sysMemUsage[i2] = 0;
        }
        data.sysMemCachedWeight = 0.0d;
        data.sysMemFreeWeight = 0.0d;
        data.sysMemZRamWeight = 0.0d;
        data.sysMemKernelWeight = 0.0d;
        data.sysMemNativeWeight = 0.0d;
        data.sysMemSamples = 0;
        long[] totalMemUsage = new long[16];
        for (int i3 = 0; i3 < this.mSysMemUsageTableSize; i3++) {
            int ent = this.mSysMemUsageTable[i3];
            addSysMemUsage(totalMemUsage, 0, this.mLongs.get((ent >> OFFSET_ARRAY_SHIFT) & OFFSET_ARRAY_MASK), (ent >> OFFSET_INDEX_SHIFT) & OFFSET_INDEX_MASK);
        }
        for (int is = 0; is < data.screenStates.length; is++) {
            for (int im = 0; im < data.memStates.length; im++) {
                int memBucket = data.screenStates[is] + data.memStates[im];
                int stateBucket = memBucket * 14;
                long memTime = this.mMemFactorDurations[memBucket];
                if (this.mMemFactor == memBucket) {
                    memTime += now - this.mStartTime;
                }
                data.totalTime += memTime;
                int sysIdx = binarySearch(this.mSysMemUsageTable, this.mSysMemUsageTableSize, stateBucket);
                long[] longs = totalMemUsage;
                int idx = 0;
                if (sysIdx >= 0) {
                    int ent2 = this.mSysMemUsageTable[sysIdx];
                    long[] tmpLongs = this.mLongs.get((ent2 >> OFFSET_ARRAY_SHIFT) & OFFSET_ARRAY_MASK);
                    int tmpIdx = (ent2 >> OFFSET_INDEX_SHIFT) & OFFSET_INDEX_MASK;
                    if (tmpLongs[tmpIdx + 0] >= 3) {
                        addSysMemUsage(data.sysMemUsage, 0, longs, 0);
                        longs = tmpLongs;
                        idx = tmpIdx;
                    }
                }
                data.sysMemCachedWeight += ((double) longs[idx + 2]) * ((double) memTime);
                data.sysMemFreeWeight += ((double) longs[idx + 5]) * ((double) memTime);
                data.sysMemZRamWeight += ((double) longs[idx + 8]) * ((double) memTime);
                data.sysMemKernelWeight += ((double) longs[idx + 11]) * ((double) memTime);
                data.sysMemNativeWeight += ((double) longs[idx + 14]) * ((double) memTime);
                data.sysMemSamples = (int) (((long) data.sysMemSamples) + longs[idx + 0]);
            }
        }
        ArrayMap<String, SparseArray<ProcessState>> procMap = this.mProcesses.getMap();
        for (int iproc = 0; iproc < procMap.size(); iproc++) {
            SparseArray<ProcessState> uids = procMap.valueAt(iproc);
            for (int iu = 0; iu < uids.size(); iu++) {
                ProcessState proc = uids.valueAt(iu);
                PssAggr fgPss = new PssAggr();
                PssAggr bgPss = new PssAggr();
                PssAggr cachedPss = new PssAggr();
                boolean havePss = false;
                for (int i4 = 0; i4 < proc.mDurationsTableSize; i4++) {
                    int type = (proc.mDurationsTable[i4] >> OFFSET_TYPE_SHIFT) & OFFSET_TYPE_MASK;
                    int procState = type % 14;
                    long samples = proc.getPssSampleCount(type);
                    if (samples > 0) {
                        long avg2 = proc.getPssAverage(type);
                        havePss = true;
                        if (procState <= 2) {
                            fgPss.add(avg2, samples);
                        } else if (procState <= 8) {
                            bgPss.add(avg2, samples);
                        } else {
                            cachedPss.add(avg2, samples);
                        }
                    }
                }
                if (havePss) {
                    boolean fgHasBg = false;
                    boolean fgHasCached = false;
                    boolean bgHasCached = false;
                    if (fgPss.samples < 3 && bgPss.samples > 0) {
                        fgHasBg = true;
                        fgPss.add(bgPss.pss, bgPss.samples);
                    }
                    if (fgPss.samples < 3 && cachedPss.samples > 0) {
                        fgHasCached = true;
                        fgPss.add(cachedPss.pss, cachedPss.samples);
                    }
                    if (bgPss.samples < 3 && cachedPss.samples > 0) {
                        bgHasCached = true;
                        bgPss.add(cachedPss.pss, cachedPss.samples);
                    }
                    if (bgPss.samples < 3 && !fgHasBg && fgPss.samples > 0) {
                        bgPss.add(fgPss.pss, fgPss.samples);
                    }
                    if (cachedPss.samples < 3 && !bgHasCached && bgPss.samples > 0) {
                        cachedPss.add(bgPss.pss, bgPss.samples);
                    }
                    if (cachedPss.samples < 3 && !fgHasCached && fgPss.samples > 0) {
                        cachedPss.add(fgPss.pss, fgPss.samples);
                    }
                    for (int i5 = 0; i5 < proc.mDurationsTableSize; i5++) {
                        int off = proc.mDurationsTable[i5];
                        int type2 = (off >> OFFSET_TYPE_SHIFT) & OFFSET_TYPE_MASK;
                        long time = getLong(off, 0);
                        if (proc.mCurState == type2) {
                            time += now - proc.mStartTime;
                        }
                        int procState2 = type2 % 14;
                        long[] jArr = data.processStateTime;
                        jArr[procState2] = jArr[procState2] + time;
                        long samples2 = proc.getPssSampleCount(type2);
                        if (samples2 > 0) {
                            avg = proc.getPssAverage(type2);
                        } else if (procState2 <= 2) {
                            samples2 = fgPss.samples;
                            avg = fgPss.pss;
                        } else if (procState2 <= 8) {
                            samples2 = bgPss.samples;
                            avg = bgPss.pss;
                        } else {
                            samples2 = cachedPss.samples;
                            avg = cachedPss.pss;
                        }
                        data.processStatePss[procState2] = (long) (((((double) data.processStatePss[procState2]) * ((double) data.processStateSamples[procState2])) + (((double) avg) * ((double) samples2))) / ((double) (((long) data.processStateSamples[procState2]) + samples2)));
                        int[] iArr = data.processStateSamples;
                        iArr[procState2] = (int) (((long) iArr[procState2]) + samples2);
                        double[] dArr = data.processStateWeight;
                        dArr[procState2] = dArr[procState2] + (((double) avg) * ((double) time));
                    }
                }
            }
        }
    }

    static void dumpProcessState(PrintWriter pw, String prefix, ProcessState proc, int[] screenStates, int[] memStates, int[] procStates, long now) {
        long totalTime = 0;
        int printedScreen = -1;
        for (int is = 0; is < screenStates.length; is++) {
            int printedMem = -1;
            for (int im = 0; im < memStates.length; im++) {
                for (int ip = 0; ip < procStates.length; ip++) {
                    int iscreen = screenStates[is];
                    int imem = memStates[im];
                    int bucket = ((iscreen + imem) * 14) + procStates[ip];
                    long time = proc.getDuration(bucket, now);
                    String running = ProxyInfo.LOCAL_EXCL_LIST;
                    if (proc.mCurState == bucket) {
                        running = " (running)";
                    }
                    if (time != 0) {
                        pw.print(prefix);
                        if (screenStates.length > 1) {
                            printScreenLabel(pw, printedScreen != iscreen ? iscreen : -1);
                            printedScreen = iscreen;
                        }
                        if (memStates.length > 1) {
                            printMemLabel(pw, printedMem != imem ? imem : -1, '/');
                            printedMem = imem;
                        }
                        pw.print(STATE_NAMES[procStates[ip]]);
                        pw.print(": ");
                        TimeUtils.formatDuration(time, pw);
                        pw.println(running);
                        totalTime += time;
                    }
                }
            }
        }
        if (totalTime != 0) {
            pw.print(prefix);
            if (screenStates.length > 1) {
                printScreenLabel(pw, -1);
            }
            if (memStates.length > 1) {
                printMemLabel(pw, -1, '/');
            }
            pw.print("TOTAL  : ");
            TimeUtils.formatDuration(totalTime, pw);
            pw.println();
        }
    }

    static void dumpProcessPss(PrintWriter pw, String prefix, ProcessState proc, int[] screenStates, int[] memStates, int[] procStates) {
        boolean printedHeader = false;
        int printedScreen = -1;
        for (int is = 0; is < screenStates.length; is++) {
            int printedMem = -1;
            for (int im = 0; im < memStates.length; im++) {
                for (int ip = 0; ip < procStates.length; ip++) {
                    int iscreen = screenStates[is];
                    int imem = memStates[im];
                    int bucket = ((iscreen + imem) * 14) + procStates[ip];
                    long count = proc.getPssSampleCount(bucket);
                    if (count > 0) {
                        if (!printedHeader) {
                            pw.print(prefix);
                            pw.print("PSS/USS (");
                            pw.print(proc.mPssTableSize);
                            pw.println(" entries):");
                            printedHeader = true;
                        }
                        pw.print(prefix);
                        pw.print("  ");
                        if (screenStates.length > 1) {
                            printScreenLabel(pw, printedScreen != iscreen ? iscreen : -1);
                            printedScreen = iscreen;
                        }
                        if (memStates.length > 1) {
                            printMemLabel(pw, printedMem != imem ? imem : -1, '/');
                            printedMem = imem;
                        }
                        pw.print(STATE_NAMES[procStates[ip]]);
                        pw.print(": ");
                        pw.print(count);
                        pw.print(" samples ");
                        printSizeValue(pw, proc.getPssMinimum(bucket) * 1024);
                        pw.print(" ");
                        printSizeValue(pw, proc.getPssAverage(bucket) * 1024);
                        pw.print(" ");
                        printSizeValue(pw, proc.getPssMaximum(bucket) * 1024);
                        pw.print(" / ");
                        printSizeValue(pw, proc.getPssUssMinimum(bucket) * 1024);
                        pw.print(" ");
                        printSizeValue(pw, proc.getPssUssAverage(bucket) * 1024);
                        pw.print(" ");
                        printSizeValue(pw, proc.getPssUssMaximum(bucket) * 1024);
                        pw.println();
                    }
                }
            }
        }
        if (proc.mNumExcessiveWake != 0) {
            pw.print(prefix);
            pw.print("Killed for excessive wake locks: ");
            pw.print(proc.mNumExcessiveWake);
            pw.println(" times");
        }
        if (proc.mNumExcessiveCpu != 0) {
            pw.print(prefix);
            pw.print("Killed for excessive CPU use: ");
            pw.print(proc.mNumExcessiveCpu);
            pw.println(" times");
        }
        if (proc.mNumCachedKill != 0) {
            pw.print(prefix);
            pw.print("Killed from cached state: ");
            pw.print(proc.mNumCachedKill);
            pw.print(" times from pss ");
            printSizeValue(pw, proc.mMinCachedKillPss * 1024);
            pw.print(NativeLibraryHelper.CLEAR_ABI_OVERRIDE);
            printSizeValue(pw, proc.mAvgCachedKillPss * 1024);
            pw.print(NativeLibraryHelper.CLEAR_ABI_OVERRIDE);
            printSizeValue(pw, proc.mMaxCachedKillPss * 1024);
            pw.println();
        }
    }

    /* access modifiers changed from: package-private */
    public long getSysMemUsageValue(int state, int index) {
        int idx = binarySearch(this.mSysMemUsageTable, this.mSysMemUsageTableSize, state);
        if (idx >= 0) {
            return getLong(this.mSysMemUsageTable[idx], index);
        }
        return 0;
    }

    /* access modifiers changed from: package-private */
    public void dumpSysMemUsageCategory(PrintWriter pw, String prefix, String label, int bucket, int index) {
        pw.print(prefix);
        pw.print(label);
        pw.print(": ");
        printSizeValue(pw, getSysMemUsageValue(bucket, index) * 1024);
        pw.print(" min, ");
        printSizeValue(pw, getSysMemUsageValue(bucket, index + 1) * 1024);
        pw.print(" avg, ");
        printSizeValue(pw, getSysMemUsageValue(bucket, index + 2) * 1024);
        pw.println(" max");
    }

    /* access modifiers changed from: package-private */
    public void dumpSysMemUsage(PrintWriter pw, String prefix, int[] screenStates, int[] memStates) {
        int printedScreen = -1;
        for (int is = 0; is < screenStates.length; is++) {
            int printedMem = -1;
            for (int im = 0; im < memStates.length; im++) {
                int iscreen = screenStates[is];
                int imem = memStates[im];
                int bucket = (iscreen + imem) * 14;
                long count = getSysMemUsageValue(bucket, 0);
                if (count > 0) {
                    pw.print(prefix);
                    if (screenStates.length > 1) {
                        printScreenLabel(pw, printedScreen != iscreen ? iscreen : -1);
                        printedScreen = iscreen;
                    }
                    if (memStates.length > 1) {
                        printMemLabel(pw, printedMem != imem ? imem : -1, 0);
                        printedMem = imem;
                    }
                    pw.print(": ");
                    pw.print(count);
                    pw.println(" samples:");
                    dumpSysMemUsageCategory(pw, prefix, "  Cached", bucket, 1);
                    dumpSysMemUsageCategory(pw, prefix, "  Free", bucket, 4);
                    dumpSysMemUsageCategory(pw, prefix, "  ZRam", bucket, 7);
                    dumpSysMemUsageCategory(pw, prefix, "  Kernel", bucket, 10);
                    dumpSysMemUsageCategory(pw, prefix, "  Native", bucket, 13);
                }
            }
        }
    }

    static void dumpStateHeadersCsv(PrintWriter pw, String sep, int[] screenStates, int[] memStates, int[] procStates) {
        int NS = screenStates != null ? screenStates.length : 1;
        int NM = memStates != null ? memStates.length : 1;
        int NP = procStates != null ? procStates.length : 1;
        for (int is = 0; is < NS; is++) {
            for (int im = 0; im < NM; im++) {
                for (int ip = 0; ip < NP; ip++) {
                    pw.print(sep);
                    boolean printed = false;
                    if (screenStates != null && screenStates.length > 1) {
                        printScreenLabelCsv(pw, screenStates[is]);
                        printed = true;
                    }
                    if (memStates != null && memStates.length > 1) {
                        if (printed) {
                            pw.print(NativeLibraryHelper.CLEAR_ABI_OVERRIDE);
                        }
                        printMemLabelCsv(pw, memStates[im]);
                        printed = true;
                    }
                    if (procStates != null && procStates.length > 1) {
                        if (printed) {
                            pw.print(NativeLibraryHelper.CLEAR_ABI_OVERRIDE);
                        }
                        pw.print(STATE_NAMES_CSV[procStates[ip]]);
                    }
                }
            }
        }
    }

    static void dumpProcessStateCsv(PrintWriter pw, ProcessState proc, boolean sepScreenStates, int[] screenStates, boolean sepMemStates, int[] memStates, boolean sepProcStates, int[] procStates, long now) {
        int NSS = sepScreenStates ? screenStates.length : 1;
        int NMS = sepMemStates ? memStates.length : 1;
        int NPS = sepProcStates ? procStates.length : 1;
        for (int iss = 0; iss < NSS; iss++) {
            for (int ims = 0; ims < NMS; ims++) {
                for (int ips = 0; ips < NPS; ips++) {
                    int vsscreen = sepScreenStates ? screenStates[iss] : 0;
                    int vsmem = sepMemStates ? memStates[ims] : 0;
                    int vsproc = sepProcStates ? procStates[ips] : 0;
                    int NSA = sepScreenStates ? 1 : screenStates.length;
                    int NMA = sepMemStates ? 1 : memStates.length;
                    int NPA = sepProcStates ? 1 : procStates.length;
                    long totalTime = 0;
                    for (int isa = 0; isa < NSA; isa++) {
                        for (int ima = 0; ima < NMA; ima++) {
                            for (int ipa = 0; ipa < NPA; ipa++) {
                                int vascreen = sepScreenStates ? 0 : screenStates[isa];
                                totalTime += proc.getDuration(((vsscreen + vascreen + vsmem + (sepMemStates ? 0 : memStates[ima])) * 14) + vsproc + (sepProcStates ? 0 : procStates[ipa]), now);
                            }
                        }
                    }
                    pw.print(CSV_SEP);
                    pw.print(totalTime);
                }
            }
        }
    }

    static void dumpProcessList(PrintWriter pw, String prefix, ArrayList<ProcessState> procs, int[] screenStates, int[] memStates, int[] procStates, long now) {
        String innerPrefix = prefix + "  ";
        for (int i = procs.size() - 1; i >= 0; i--) {
            ProcessState proc = procs.get(i);
            pw.print(prefix);
            pw.print(proc.mName);
            pw.print(" / ");
            UserHandle.formatUid(pw, proc.mUid);
            pw.print(" (");
            pw.print(proc.mDurationsTableSize);
            pw.print(" entries)");
            pw.println(":");
            dumpProcessState(pw, innerPrefix, proc, screenStates, memStates, procStates, now);
            if (proc.mPssTableSize > 0) {
                dumpProcessPss(pw, innerPrefix, proc, screenStates, memStates, procStates);
            }
        }
    }

    static void dumpProcessSummaryDetails(PrintWriter pw, ProcessState proc, String prefix, String label, int[] screenStates, int[] memStates, int[] procStates, long now, long totalTime, boolean full) {
        ProcessDataCollection totals = new ProcessDataCollection(screenStates, memStates, procStates);
        computeProcessData(proc, totals, now);
        if (totals.totalTime != 0 || totals.numPss != 0) {
            if (prefix != null) {
                pw.print(prefix);
            }
            if (label != null) {
                pw.print(label);
            }
            totals.print(pw, totalTime, full);
            if (prefix != null) {
                pw.println();
            }
        }
    }

    static void dumpProcessSummaryLocked(PrintWriter pw, String prefix, ArrayList<ProcessState> procs, int[] screenStates, int[] memStates, int[] procStates, boolean inclUidVers, long now, long totalTime) {
        for (int i = procs.size() - 1; i >= 0; i--) {
            ProcessState proc = procs.get(i);
            pw.print(prefix);
            pw.print("* ");
            pw.print(proc.mName);
            pw.print(" / ");
            UserHandle.formatUid(pw, proc.mUid);
            pw.print(" / v");
            pw.print(proc.mVersion);
            pw.println(":");
            dumpProcessSummaryDetails(pw, proc, prefix, "         TOTAL: ", screenStates, memStates, procStates, now, totalTime, true);
            dumpProcessSummaryDetails(pw, proc, prefix, "    Persistent: ", screenStates, memStates, new int[]{0}, now, totalTime, true);
            dumpProcessSummaryDetails(pw, proc, prefix, "           Top: ", screenStates, memStates, new int[]{1}, now, totalTime, true);
            dumpProcessSummaryDetails(pw, proc, prefix, "        Imp Fg: ", screenStates, memStates, new int[]{2}, now, totalTime, true);
            dumpProcessSummaryDetails(pw, proc, prefix, "        Imp Bg: ", screenStates, memStates, new int[]{3}, now, totalTime, true);
            dumpProcessSummaryDetails(pw, proc, prefix, "        Backup: ", screenStates, memStates, new int[]{4}, now, totalTime, true);
            dumpProcessSummaryDetails(pw, proc, prefix, "     Heavy Wgt: ", screenStates, memStates, new int[]{5}, now, totalTime, true);
            dumpProcessSummaryDetails(pw, proc, prefix, "       Service: ", screenStates, memStates, new int[]{6}, now, totalTime, true);
            dumpProcessSummaryDetails(pw, proc, prefix, "    Service Rs: ", screenStates, memStates, new int[]{7}, now, totalTime, true);
            dumpProcessSummaryDetails(pw, proc, prefix, "      Receiver: ", screenStates, memStates, new int[]{8}, now, totalTime, true);
            dumpProcessSummaryDetails(pw, proc, prefix, "        (Home): ", screenStates, memStates, new int[]{9}, now, totalTime, true);
            dumpProcessSummaryDetails(pw, proc, prefix, "    (Last Act): ", screenStates, memStates, new int[]{10}, now, totalTime, true);
            dumpProcessSummaryDetails(pw, proc, prefix, "      (Cached): ", screenStates, memStates, new int[]{11, 12, 13}, now, totalTime, true);
        }
    }

    static void printPercent(PrintWriter pw, double fraction) {
        double fraction2 = fraction * 100.0d;
        if (fraction2 < 1.0d) {
            pw.print(String.format("%.2f", Double.valueOf(fraction2)));
        } else if (fraction2 < 10.0d) {
            pw.print(String.format("%.1f", Double.valueOf(fraction2)));
        } else {
            pw.print(String.format("%.0f", Double.valueOf(fraction2)));
        }
        pw.print("%");
    }

    static void printSizeValue(PrintWriter pw, long number) {
        String value;
        float result = (float) number;
        String suffix = ProxyInfo.LOCAL_EXCL_LIST;
        if (result > 900.0f) {
            suffix = "KB";
            result /= 1024.0f;
        }
        if (result > 900.0f) {
            suffix = "MB";
            result /= 1024.0f;
        }
        if (result > 900.0f) {
            suffix = "GB";
            result /= 1024.0f;
        }
        if (result > 900.0f) {
            suffix = "TB";
            result /= 1024.0f;
        }
        if (result > 900.0f) {
            suffix = "PB";
            result /= 1024.0f;
        }
        if (result < 1.0f) {
            value = String.format("%.2f", Float.valueOf(result));
        } else if (result < 10.0f) {
            value = String.format("%.1f", Float.valueOf(result));
        } else if (result < 100.0f) {
            value = String.format("%.0f", Float.valueOf(result));
        } else {
            value = String.format("%.0f", Float.valueOf(result));
        }
        pw.print(value);
        pw.print(suffix);
    }

    public static void dumpProcessListCsv(PrintWriter pw, ArrayList<ProcessState> procs, boolean sepScreenStates, int[] screenStates, boolean sepMemStates, int[] memStates, boolean sepProcStates, int[] procStates, long now) {
        int[] iArr;
        pw.print("process");
        pw.print(CSV_SEP);
        pw.print(GrantCredentialsPermissionActivity.EXTRAS_REQUESTING_UID);
        pw.print(CSV_SEP);
        pw.print("vers");
        int[] iArr2 = sepScreenStates ? screenStates : null;
        int[] iArr3 = sepMemStates ? memStates : null;
        if (sepProcStates) {
            iArr = procStates;
        } else {
            iArr = null;
        }
        dumpStateHeadersCsv(pw, CSV_SEP, iArr2, iArr3, iArr);
        pw.println();
        for (int i = procs.size() - 1; i >= 0; i--) {
            ProcessState proc = procs.get(i);
            pw.print(proc.mName);
            pw.print(CSV_SEP);
            UserHandle.formatUid(pw, proc.mUid);
            pw.print(CSV_SEP);
            pw.print(proc.mVersion);
            dumpProcessStateCsv(pw, proc, sepScreenStates, screenStates, sepMemStates, memStates, sepProcStates, procStates, now);
            pw.println();
        }
    }

    static int printArrayEntry(PrintWriter pw, String[] array, int value, int mod) {
        int index = value / mod;
        if (index < 0 || index >= array.length) {
            pw.print('?');
        } else {
            pw.print(array[index]);
        }
        return value - (index * mod);
    }

    static void printProcStateTag(PrintWriter pw, int state) {
        printArrayEntry(pw, STATE_TAGS, printArrayEntry(pw, ADJ_MEM_TAGS, printArrayEntry(pw, ADJ_SCREEN_TAGS, state, 56), 14), 1);
    }

    static void printAdjTag(PrintWriter pw, int state) {
        printArrayEntry(pw, ADJ_MEM_TAGS, printArrayEntry(pw, ADJ_SCREEN_TAGS, state, 4), 1);
    }

    static void printProcStateTagAndValue(PrintWriter pw, int state, long value) {
        pw.print(',');
        printProcStateTag(pw, state);
        pw.print(':');
        pw.print(value);
    }

    static void printAdjTagAndValue(PrintWriter pw, int state, long value) {
        pw.print(',');
        printAdjTag(pw, state);
        pw.print(':');
        pw.print(value);
    }

    static void dumpAllProcessStateCheckin(PrintWriter pw, ProcessState proc, long now) {
        boolean didCurState = false;
        for (int i = 0; i < proc.mDurationsTableSize; i++) {
            int off = proc.mDurationsTable[i];
            int type = (off >> OFFSET_TYPE_SHIFT) & OFFSET_TYPE_MASK;
            long time = proc.mStats.getLong(off, 0);
            if (proc.mCurState == type) {
                didCurState = true;
                time += now - proc.mStartTime;
            }
            printProcStateTagAndValue(pw, type, time);
        }
        if (!(didCurState || proc.mCurState == -1)) {
            printProcStateTagAndValue(pw, proc.mCurState, now - proc.mStartTime);
        }
    }

    static void dumpAllProcessPssCheckin(PrintWriter pw, ProcessState proc) {
        for (int i = 0; i < proc.mPssTableSize; i++) {
            int off = proc.mPssTable[i];
            int type = (off >> OFFSET_TYPE_SHIFT) & OFFSET_TYPE_MASK;
            long count = proc.mStats.getLong(off, 0);
            long min = proc.mStats.getLong(off, 1);
            long avg = proc.mStats.getLong(off, 2);
            long max = proc.mStats.getLong(off, 3);
            long umin = proc.mStats.getLong(off, 4);
            long uavg = proc.mStats.getLong(off, 5);
            long umax = proc.mStats.getLong(off, 6);
            pw.print(',');
            printProcStateTag(pw, type);
            pw.print(':');
            pw.print(count);
            pw.print(':');
            pw.print(min);
            pw.print(':');
            pw.print(avg);
            pw.print(':');
            pw.print(max);
            pw.print(':');
            pw.print(umin);
            pw.print(':');
            pw.print(uavg);
            pw.print(':');
            pw.print(umax);
        }
    }

    public void reset() {
        resetCommon();
        this.mPackages.getMap().clear();
        this.mProcesses.getMap().clear();
        this.mMemFactor = -1;
        this.mStartTime = 0;
    }

    public void resetSafely() {
        resetCommon();
        long now = SystemClock.uptimeMillis();
        ArrayMap<String, SparseArray<ProcessState>> procMap = this.mProcesses.getMap();
        for (int ip = procMap.size() - 1; ip >= 0; ip--) {
            SparseArray<ProcessState> uids = procMap.valueAt(ip);
            for (int iu = uids.size() - 1; iu >= 0; iu--) {
                uids.valueAt(iu).mTmpNumInUse = 0;
            }
        }
        ArrayMap<String, SparseArray<SparseArray<PackageState>>> pkgMap = this.mPackages.getMap();
        for (int ip2 = pkgMap.size() - 1; ip2 >= 0; ip2--) {
            SparseArray<SparseArray<PackageState>> uids2 = pkgMap.valueAt(ip2);
            for (int iu2 = uids2.size() - 1; iu2 >= 0; iu2--) {
                SparseArray<PackageState> vpkgs = uids2.valueAt(iu2);
                for (int iv = vpkgs.size() - 1; iv >= 0; iv--) {
                    PackageState pkgState = vpkgs.valueAt(iv);
                    for (int iproc = pkgState.mProcesses.size() - 1; iproc >= 0; iproc--) {
                        ProcessState ps = pkgState.mProcesses.valueAt(iproc);
                        if (ps.isInUse()) {
                            ps.resetSafely(now);
                            ps.mCommonProcess.mTmpNumInUse++;
                            ps.mCommonProcess.mTmpFoundSubProc = ps;
                        } else {
                            pkgState.mProcesses.valueAt(iproc).makeDead();
                            pkgState.mProcesses.removeAt(iproc);
                        }
                    }
                    for (int isvc = pkgState.mServices.size() - 1; isvc >= 0; isvc--) {
                        ServiceState ss = pkgState.mServices.valueAt(isvc);
                        if (ss.isInUse()) {
                            ss.resetSafely(now);
                        } else {
                            pkgState.mServices.removeAt(isvc);
                        }
                    }
                    if (pkgState.mProcesses.size() <= 0 && pkgState.mServices.size() <= 0) {
                        vpkgs.removeAt(iv);
                    }
                }
                if (vpkgs.size() <= 0) {
                    uids2.removeAt(iu2);
                }
            }
            if (uids2.size() <= 0) {
                pkgMap.removeAt(ip2);
            }
        }
        for (int ip3 = procMap.size() - 1; ip3 >= 0; ip3--) {
            SparseArray<ProcessState> uids3 = procMap.valueAt(ip3);
            for (int iu3 = uids3.size() - 1; iu3 >= 0; iu3--) {
                ProcessState ps2 = uids3.valueAt(iu3);
                if (!ps2.isInUse() && ps2.mTmpNumInUse <= 0) {
                    ps2.makeDead();
                    uids3.removeAt(iu3);
                } else if (ps2.mActive || !ps2.mMultiPackage || ps2.mTmpNumInUse != 1) {
                    ps2.resetSafely(now);
                } else {
                    ProcessState ps3 = ps2.mTmpFoundSubProc;
                    ps3.mCommonProcess = ps3;
                    uids3.setValueAt(iu3, ps3);
                }
            }
            if (uids3.size() <= 0) {
                procMap.removeAt(ip3);
            }
        }
        this.mStartTime = now;
    }

    private void resetCommon() {
        this.mTimePeriodStartClock = System.currentTimeMillis();
        buildTimePeriodStartClockStr();
        long elapsedRealtime = SystemClock.elapsedRealtime();
        this.mTimePeriodEndRealtime = elapsedRealtime;
        this.mTimePeriodStartRealtime = elapsedRealtime;
        long uptimeMillis = SystemClock.uptimeMillis();
        this.mTimePeriodEndUptime = uptimeMillis;
        this.mTimePeriodStartUptime = uptimeMillis;
        this.mLongs.clear();
        this.mLongs.add(new long[4096]);
        this.mNextLong = 0;
        Arrays.fill(this.mMemFactorDurations, 0L);
        this.mSysMemUsageTable = null;
        this.mSysMemUsageTableSize = 0;
        this.mStartTime = 0;
        this.mReadError = null;
        this.mFlags = 0;
        evaluateSystemProperties(true);
    }

    public boolean evaluateSystemProperties(boolean update) {
        boolean changed = false;
        String runtime = SystemProperties.get("persist.sys.dalvik.vm.lib.2", VMRuntime.getRuntime().vmLibrary());
        if (!Objects.equals(runtime, this.mRuntime)) {
            changed = true;
            if (update) {
                this.mRuntime = runtime;
            }
        }
        return changed;
    }

    private void buildTimePeriodStartClockStr() {
        this.mTimePeriodStartClockStr = DateFormat.format("yyyy-MM-dd-HH-mm-ss", this.mTimePeriodStartClock).toString();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int[] readTableFromParcel(Parcel in, String name, String what) {
        int size = in.readInt();
        if (size < 0) {
            Slog.w(TAG, "Ignoring existing stats; bad " + what + " table size: " + size);
            return BAD_TABLE;
        } else if (size == 0) {
            return null;
        } else {
            int[] table = new int[size];
            for (int i = 0; i < size; i++) {
                table[i] = in.readInt();
                if (!validateLongOffset(table[i])) {
                    Slog.w(TAG, "Ignoring existing stats; bad " + what + " table entry: " + printLongOffset(table[i]));
                    return null;
                }
            }
            return table;
        }
    }

    private void writeCompactedLongArray(Parcel out, long[] array, int num) {
        for (int i = 0; i < num; i++) {
            long val = array[i];
            if (val < 0) {
                Slog.w(TAG, "Time val negative: " + val);
                val = 0;
            }
            if (val <= 2147483647L) {
                out.writeInt((int) val);
            } else {
                out.writeInt(((int) ((val >> 32) & 2147483647L)) ^ -1);
                out.writeInt((int) (268435455 & val));
            }
        }
    }

    private void readCompactedLongArray(Parcel in, int version, long[] array, int num) {
        if (version <= 10) {
            in.readLongArray(array);
            return;
        }
        int alen = array.length;
        if (num > alen) {
            throw new RuntimeException("bad array lengths: got " + num + " array is " + alen);
        }
        int i = 0;
        while (i < num) {
            int val = in.readInt();
            if (val >= 0) {
                array[i] = (long) val;
            } else {
                array[i] = (((long) (val ^ -1)) << 32) | ((long) in.readInt());
            }
            i++;
        }
        while (i < alen) {
            array[i] = 0;
            i++;
        }
    }

    private void writeCommonString(Parcel out, String name) {
        Integer index = this.mCommonStringToIndex.get(name);
        if (index != null) {
            out.writeInt(index.intValue());
            return;
        }
        Integer index2 = Integer.valueOf(this.mCommonStringToIndex.size());
        this.mCommonStringToIndex.put(name, index2);
        out.writeInt(index2.intValue() ^ -1);
        out.writeString(name);
    }

    private String readCommonString(Parcel in, int version) {
        if (version <= 9) {
            return in.readString();
        }
        int index = in.readInt();
        if (index >= 0) {
            return this.mIndexToCommonString.get(index);
        }
        int index2 = index ^ -1;
        String name = in.readString();
        while (this.mIndexToCommonString.size() <= index2) {
            this.mIndexToCommonString.add(null);
        }
        this.mIndexToCommonString.set(index2, name);
        return name;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        writeToParcel(out, SystemClock.uptimeMillis(), flags);
    }

    public void writeToParcel(Parcel out, long now, int flags) {
        out.writeInt(MAGIC);
        out.writeInt(18);
        out.writeInt(14);
        out.writeInt(8);
        out.writeInt(7);
        out.writeInt(16);
        out.writeInt(4096);
        this.mCommonStringToIndex = new ArrayMap<>(this.mProcesses.mMap.size());
        ArrayMap<String, SparseArray<ProcessState>> procMap = this.mProcesses.getMap();
        int NPROC = procMap.size();
        for (int ip = 0; ip < NPROC; ip++) {
            SparseArray<ProcessState> uids = procMap.valueAt(ip);
            int NUID = uids.size();
            for (int iu = 0; iu < NUID; iu++) {
                uids.valueAt(iu).commitStateTime(now);
            }
        }
        ArrayMap<String, SparseArray<SparseArray<PackageState>>> pkgMap = this.mPackages.getMap();
        int NPKG = pkgMap.size();
        for (int ip2 = 0; ip2 < NPKG; ip2++) {
            SparseArray<SparseArray<PackageState>> uids2 = pkgMap.valueAt(ip2);
            int NUID2 = uids2.size();
            for (int iu2 = 0; iu2 < NUID2; iu2++) {
                SparseArray<PackageState> vpkgs = uids2.valueAt(iu2);
                int NVERS = vpkgs.size();
                for (int iv = 0; iv < NVERS; iv++) {
                    PackageState pkgState = vpkgs.valueAt(iv);
                    int NPROCS = pkgState.mProcesses.size();
                    for (int iproc = 0; iproc < NPROCS; iproc++) {
                        ProcessState proc = pkgState.mProcesses.valueAt(iproc);
                        if (proc.mCommonProcess != proc) {
                            proc.commitStateTime(now);
                        }
                    }
                    int NSRVS = pkgState.mServices.size();
                    for (int isvc = 0; isvc < NSRVS; isvc++) {
                        pkgState.mServices.valueAt(isvc).commitStateTime(now);
                    }
                }
            }
        }
        out.writeLong(this.mTimePeriodStartClock);
        out.writeLong(this.mTimePeriodStartRealtime);
        out.writeLong(this.mTimePeriodEndRealtime);
        out.writeLong(this.mTimePeriodStartUptime);
        out.writeLong(this.mTimePeriodEndUptime);
        out.writeString(this.mRuntime);
        out.writeInt(this.mFlags);
        out.writeInt(this.mLongs.size());
        out.writeInt(this.mNextLong);
        for (int i = 0; i < this.mLongs.size() - 1; i++) {
            long[] array = this.mLongs.get(i);
            writeCompactedLongArray(out, array, array.length);
        }
        writeCompactedLongArray(out, this.mLongs.get(this.mLongs.size() - 1), this.mNextLong);
        if (this.mMemFactor != -1) {
            long[] jArr = this.mMemFactorDurations;
            int i2 = this.mMemFactor;
            jArr[i2] = jArr[i2] + (now - this.mStartTime);
            this.mStartTime = now;
        }
        writeCompactedLongArray(out, this.mMemFactorDurations, this.mMemFactorDurations.length);
        out.writeInt(this.mSysMemUsageTableSize);
        for (int i3 = 0; i3 < this.mSysMemUsageTableSize; i3++) {
            out.writeInt(this.mSysMemUsageTable[i3]);
        }
        out.writeInt(NPROC);
        for (int ip3 = 0; ip3 < NPROC; ip3++) {
            writeCommonString(out, procMap.keyAt(ip3));
            SparseArray<ProcessState> uids3 = procMap.valueAt(ip3);
            int NUID3 = uids3.size();
            out.writeInt(NUID3);
            for (int iu3 = 0; iu3 < NUID3; iu3++) {
                out.writeInt(uids3.keyAt(iu3));
                ProcessState proc2 = uids3.valueAt(iu3);
                writeCommonString(out, proc2.mPackage);
                out.writeInt(proc2.mVersion);
                proc2.writeToParcel(out, now);
            }
        }
        out.writeInt(NPKG);
        for (int ip4 = 0; ip4 < NPKG; ip4++) {
            writeCommonString(out, pkgMap.keyAt(ip4));
            SparseArray<SparseArray<PackageState>> uids4 = pkgMap.valueAt(ip4);
            int NUID4 = uids4.size();
            out.writeInt(NUID4);
            for (int iu4 = 0; iu4 < NUID4; iu4++) {
                out.writeInt(uids4.keyAt(iu4));
                SparseArray<PackageState> vpkgs2 = uids4.valueAt(iu4);
                int NVERS2 = vpkgs2.size();
                out.writeInt(NVERS2);
                for (int iv2 = 0; iv2 < NVERS2; iv2++) {
                    out.writeInt(vpkgs2.keyAt(iv2));
                    PackageState pkgState2 = vpkgs2.valueAt(iv2);
                    int NPROCS2 = pkgState2.mProcesses.size();
                    out.writeInt(NPROCS2);
                    for (int iproc2 = 0; iproc2 < NPROCS2; iproc2++) {
                        writeCommonString(out, pkgState2.mProcesses.keyAt(iproc2));
                        ProcessState proc3 = pkgState2.mProcesses.valueAt(iproc2);
                        if (proc3.mCommonProcess == proc3) {
                            out.writeInt(0);
                        } else {
                            out.writeInt(1);
                            proc3.writeToParcel(out, now);
                        }
                    }
                    int NSRVS2 = pkgState2.mServices.size();
                    out.writeInt(NSRVS2);
                    for (int isvc2 = 0; isvc2 < NSRVS2; isvc2++) {
                        out.writeString(pkgState2.mServices.keyAt(isvc2));
                        ServiceState svc = pkgState2.mServices.valueAt(isvc2);
                        writeCommonString(out, svc.mProcessName);
                        svc.writeToParcel(out, now);
                    }
                }
            }
        }
        this.mCommonStringToIndex = null;
    }

    private boolean readCheckedInt(Parcel in, int val, String what) {
        int got = in.readInt();
        if (got == val) {
            return true;
        }
        this.mReadError = "bad " + what + ": " + got;
        return false;
    }

    static byte[] readFully(InputStream stream, int[] outLen) throws IOException {
        int pos = 0;
        int initialAvail = stream.available();
        byte[] data = new byte[(initialAvail > 0 ? initialAvail + 1 : 16384)];
        while (true) {
            int amt = stream.read(data, pos, data.length - pos);
            if (amt < 0) {
                outLen[0] = pos;
                return data;
            }
            pos += amt;
            if (pos >= data.length) {
                byte[] newData = new byte[(pos + 16384)];
                System.arraycopy(data, 0, newData, 0, pos);
                data = newData;
            }
        }
    }

    public void read(InputStream stream) {
        try {
            int[] len = new int[1];
            byte[] raw = readFully(stream, len);
            Parcel in = Parcel.obtain();
            in.unmarshall(raw, 0, len[0]);
            in.setDataPosition(0);
            stream.close();
            readFromParcel(in);
        } catch (IOException e) {
            this.mReadError = "caught exception: " + e;
        }
    }

    /*  JADX ERROR: JadxRuntimeException in pass: BlockProcessor
        jadx.core.utils.exceptions.JadxRuntimeException: CFG modification limit reached, blocks count: 251
        	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:72)
        	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.visit(BlockProcessor.java:46)
        */
    public void readFromParcel(android.os.Parcel r37) {
        /*
        // Method dump skipped, instructions count: 1093
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.ProcessStats.readFromParcel(android.os.Parcel):void");
    }

    /* access modifiers changed from: package-private */
    public int addLongData(int index, int type, int num) {
        int off = allocLongData(num);
        this.mAddLongTable = GrowingArrayUtils.insert(this.mAddLongTable != null ? this.mAddLongTable : EmptyArray.INT, this.mAddLongTableSize, index, type | off);
        this.mAddLongTableSize++;
        return off;
    }

    /* access modifiers changed from: package-private */
    public int allocLongData(int num) {
        int whichLongs = this.mLongs.size() - 1;
        if (this.mNextLong + num > this.mLongs.get(whichLongs).length) {
            this.mLongs.add(new long[4096]);
            whichLongs++;
            this.mNextLong = 0;
        }
        int off = (whichLongs << OFFSET_ARRAY_SHIFT) | (this.mNextLong << OFFSET_INDEX_SHIFT);
        this.mNextLong += num;
        return off;
    }

    /* access modifiers changed from: package-private */
    public boolean validateLongOffset(int off) {
        if (((off >> OFFSET_ARRAY_SHIFT) & OFFSET_ARRAY_MASK) < this.mLongs.size() && ((off >> OFFSET_INDEX_SHIFT) & OFFSET_INDEX_MASK) < 4096) {
            return true;
        }
        return false;
    }

    static String printLongOffset(int off) {
        StringBuilder sb = new StringBuilder(16);
        sb.append(FullBackup.APK_TREE_TOKEN);
        sb.append((off >> OFFSET_ARRAY_SHIFT) & OFFSET_ARRAY_MASK);
        sb.append("i");
        sb.append((off >> OFFSET_INDEX_SHIFT) & OFFSET_INDEX_MASK);
        sb.append("t");
        sb.append((off >> OFFSET_TYPE_SHIFT) & OFFSET_TYPE_MASK);
        return sb.toString();
    }

    /* access modifiers changed from: package-private */
    public void setLong(int off, int index, long value) {
        this.mLongs.get((off >> OFFSET_ARRAY_SHIFT) & OFFSET_ARRAY_MASK)[((off >> OFFSET_INDEX_SHIFT) & OFFSET_INDEX_MASK) + index] = value;
    }

    /* access modifiers changed from: package-private */
    public long getLong(int off, int index) {
        return this.mLongs.get((off >> OFFSET_ARRAY_SHIFT) & OFFSET_ARRAY_MASK)[((off >> OFFSET_INDEX_SHIFT) & OFFSET_INDEX_MASK) + index];
    }

    static int binarySearch(int[] array, int size, int value) {
        int lo = 0;
        int hi = size - 1;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            int midVal = (array[mid] >> OFFSET_TYPE_SHIFT) & OFFSET_TYPE_MASK;
            if (midVal < value) {
                lo = mid + 1;
            } else if (midVal <= value) {
                return mid;
            } else {
                hi = mid - 1;
            }
        }
        return lo ^ -1;
    }

    public PackageState getPackageStateLocked(String packageName, int uid, int vers) {
        SparseArray<PackageState> vpkg = this.mPackages.get(packageName, uid);
        if (vpkg == null) {
            vpkg = new SparseArray<>();
            this.mPackages.put(packageName, uid, vpkg);
        }
        PackageState as = vpkg.get(vers);
        if (as != null) {
            return as;
        }
        PackageState as2 = new PackageState(packageName, uid);
        vpkg.put(vers, as2);
        return as2;
    }

    public ProcessState getProcessStateLocked(String packageName, int uid, int vers, String processName) {
        ProcessState ps;
        PackageState pkgState = getPackageStateLocked(packageName, uid, vers);
        ProcessState ps2 = pkgState.mProcesses.get(processName);
        if (ps2 != null) {
            return ps2;
        }
        ProcessState commonProc = this.mProcesses.get(processName, uid);
        if (commonProc == null) {
            commonProc = new ProcessState(this, packageName, uid, vers, processName);
            this.mProcesses.put(processName, uid, commonProc);
        }
        if (commonProc.mMultiPackage) {
            ps = new ProcessState(commonProc, packageName, uid, vers, processName, SystemClock.uptimeMillis());
        } else if (!packageName.equals(commonProc.mPackage) || vers != commonProc.mVersion) {
            commonProc.mMultiPackage = true;
            long now = SystemClock.uptimeMillis();
            PackageState commonPkgState = getPackageStateLocked(commonProc.mPackage, uid, commonProc.mVersion);
            if (commonPkgState != null) {
                ProcessState cloned = commonProc.clone(commonProc.mPackage, now);
                commonPkgState.mProcesses.put(commonProc.mName, cloned);
                for (int i = commonPkgState.mServices.size() - 1; i >= 0; i--) {
                    ServiceState ss = commonPkgState.mServices.valueAt(i);
                    if (ss.mProc == commonProc) {
                        ss.mProc = cloned;
                    }
                }
            } else {
                Slog.w(TAG, "Cloning proc state: no package state " + commonProc.mPackage + "/" + uid + " for proc " + commonProc.mName);
            }
            ps = new ProcessState(commonProc, packageName, uid, vers, processName, now);
        } else {
            ps = commonProc;
        }
        pkgState.mProcesses.put(processName, ps);
        return ps;
    }

    public ServiceState getServiceStateLocked(String packageName, int uid, int vers, String processName, String className) {
        PackageState as = getPackageStateLocked(packageName, uid, vers);
        ServiceState ss = as.mServices.get(className);
        if (ss != null) {
            return ss;
        }
        ServiceState ss2 = new ServiceState(this, packageName, className, processName, processName != null ? getProcessStateLocked(packageName, uid, vers, processName) : null);
        as.mServices.put(className, ss2);
        return ss2;
    }

    private void dumpProcessInternalLocked(PrintWriter pw, String prefix, ProcessState proc, boolean dumpAll) {
        if (dumpAll) {
            pw.print(prefix);
            pw.print("myID=");
            pw.print(Integer.toHexString(System.identityHashCode(proc)));
            pw.print(" mCommonProcess=");
            pw.print(Integer.toHexString(System.identityHashCode(proc.mCommonProcess)));
            pw.print(" mPackage=");
            pw.println(proc.mPackage);
            if (proc.mMultiPackage) {
                pw.print(prefix);
                pw.print("mMultiPackage=");
                pw.println(proc.mMultiPackage);
            }
            if (proc != proc.mCommonProcess) {
                pw.print(prefix);
                pw.print("Common Proc: ");
                pw.print(proc.mCommonProcess.mName);
                pw.print("/");
                pw.print(proc.mCommonProcess.mUid);
                pw.print(" pkg=");
                pw.println(proc.mCommonProcess.mPackage);
            }
        }
        if (proc.mActive) {
            pw.print(prefix);
            pw.print("mActive=");
            pw.println(proc.mActive);
        }
        if (proc.mDead) {
            pw.print(prefix);
            pw.print("mDead=");
            pw.println(proc.mDead);
        }
        if (proc.mNumActiveServices != 0 || proc.mNumStartedServices != 0) {
            pw.print(prefix);
            pw.print("mNumActiveServices=");
            pw.print(proc.mNumActiveServices);
            pw.print(" mNumStartedServices=");
            pw.println(proc.mNumStartedServices);
        }
    }

    public void dumpLocked(PrintWriter pw, String reqPackage, long now, boolean dumpSummary, boolean dumpAll, boolean activeOnly) {
        long totalTime = dumpSingleTime(null, null, this.mMemFactorDurations, this.mMemFactor, this.mStartTime, now);
        boolean sepNeeded = false;
        if (this.mSysMemUsageTable != null) {
            pw.println("System memory usage:");
            dumpSysMemUsage(pw, "  ", ALL_SCREEN_ADJ, ALL_MEM_ADJ);
            sepNeeded = true;
        }
        ArrayMap<String, SparseArray<SparseArray<PackageState>>> pkgMap = this.mPackages.getMap();
        boolean printedHeader = false;
        for (int ip = 0; ip < pkgMap.size(); ip++) {
            String pkgName = pkgMap.keyAt(ip);
            SparseArray<SparseArray<PackageState>> uids = pkgMap.valueAt(ip);
            for (int iu = 0; iu < uids.size(); iu++) {
                int uid = uids.keyAt(iu);
                SparseArray<PackageState> vpkgs = uids.valueAt(iu);
                for (int iv = 0; iv < vpkgs.size(); iv++) {
                    int vers = vpkgs.keyAt(iv);
                    PackageState pkgState = vpkgs.valueAt(iv);
                    int NPROCS = pkgState.mProcesses.size();
                    int NSRVS = pkgState.mServices.size();
                    boolean pkgMatch = reqPackage == null || reqPackage.equals(pkgName);
                    if (!pkgMatch) {
                        boolean procMatch = false;
                        int iproc = 0;
                        while (true) {
                            if (iproc >= NPROCS) {
                                break;
                            } else if (reqPackage.equals(pkgState.mProcesses.valueAt(iproc).mName)) {
                                procMatch = true;
                                break;
                            } else {
                                iproc++;
                            }
                        }
                        if (!procMatch) {
                        }
                    }
                    if (NPROCS > 0 || NSRVS > 0) {
                        if (!printedHeader) {
                            if (sepNeeded) {
                                pw.println();
                            }
                            pw.println("Per-Package Stats:");
                            printedHeader = true;
                            sepNeeded = true;
                        }
                        pw.print("  * ");
                        pw.print(pkgName);
                        pw.print(" / ");
                        UserHandle.formatUid(pw, uid);
                        pw.print(" / v");
                        pw.print(vers);
                        pw.println(":");
                    }
                    if (!dumpSummary || dumpAll) {
                        for (int iproc2 = 0; iproc2 < NPROCS; iproc2++) {
                            ProcessState proc = pkgState.mProcesses.valueAt(iproc2);
                            if (pkgMatch || reqPackage.equals(proc.mName)) {
                                if (!activeOnly || proc.isInUse()) {
                                    pw.print("      Process ");
                                    pw.print(pkgState.mProcesses.keyAt(iproc2));
                                    if (proc.mCommonProcess.mMultiPackage) {
                                        pw.print(" (multi, ");
                                    } else {
                                        pw.print(" (unique, ");
                                    }
                                    pw.print(proc.mDurationsTableSize);
                                    pw.print(" entries)");
                                    pw.println(":");
                                    dumpProcessState(pw, "        ", proc, ALL_SCREEN_ADJ, ALL_MEM_ADJ, ALL_PROC_STATES, now);
                                    dumpProcessPss(pw, "        ", proc, ALL_SCREEN_ADJ, ALL_MEM_ADJ, ALL_PROC_STATES);
                                    dumpProcessInternalLocked(pw, "        ", proc, dumpAll);
                                } else {
                                    pw.print("      (Not active: ");
                                    pw.print(pkgState.mProcesses.keyAt(iproc2));
                                    pw.println(")");
                                }
                            }
                        }
                    } else {
                        ArrayList<ProcessState> procs = new ArrayList<>();
                        for (int iproc3 = 0; iproc3 < NPROCS; iproc3++) {
                            ProcessState proc2 = pkgState.mProcesses.valueAt(iproc3);
                            if ((pkgMatch || reqPackage.equals(proc2.mName)) && (!activeOnly || proc2.isInUse())) {
                                procs.add(proc2);
                            }
                        }
                        dumpProcessSummaryLocked(pw, "      ", procs, ALL_SCREEN_ADJ, ALL_MEM_ADJ, NON_CACHED_PROC_STATES, false, now, totalTime);
                    }
                    for (int isvc = 0; isvc < NSRVS; isvc++) {
                        ServiceState svc = pkgState.mServices.valueAt(isvc);
                        if (pkgMatch || reqPackage.equals(svc.mProcessName)) {
                            if (!activeOnly || svc.isInUse()) {
                                if (dumpAll) {
                                    pw.print("      Service ");
                                } else {
                                    pw.print("      * ");
                                }
                                pw.print(pkgState.mServices.keyAt(isvc));
                                pw.println(":");
                                pw.print("        Process: ");
                                pw.println(svc.mProcessName);
                                dumpServiceStats(pw, "        ", "          ", "    ", "Running", svc, svc.mRunCount, 0, svc.mRunState, svc.mRunStartTime, now, totalTime, !dumpSummary || dumpAll);
                                dumpServiceStats(pw, "        ", "          ", "    ", "Started", svc, svc.mStartedCount, 1, svc.mStartedState, svc.mStartedStartTime, now, totalTime, !dumpSummary || dumpAll);
                                dumpServiceStats(pw, "        ", "          ", "      ", "Bound", svc, svc.mBoundCount, 2, svc.mBoundState, svc.mBoundStartTime, now, totalTime, !dumpSummary || dumpAll);
                                dumpServiceStats(pw, "        ", "          ", "  ", "Executing", svc, svc.mExecCount, 3, svc.mExecState, svc.mExecStartTime, now, totalTime, !dumpSummary || dumpAll);
                                if (dumpAll) {
                                    if (svc.mOwner != null) {
                                        pw.print("        mOwner=");
                                        pw.println(svc.mOwner);
                                    }
                                    if (svc.mStarted || svc.mRestarting) {
                                        pw.print("        mStarted=");
                                        pw.print(svc.mStarted);
                                        pw.print(" mRestarting=");
                                        pw.println(svc.mRestarting);
                                    }
                                }
                            } else {
                                pw.print("      (Not active: ");
                                pw.print(pkgState.mServices.keyAt(isvc));
                                pw.println(")");
                            }
                        }
                    }
                }
            }
        }
        ArrayMap<String, SparseArray<ProcessState>> procMap = this.mProcesses.getMap();
        boolean printedHeader2 = false;
        int numShownProcs = 0;
        int numTotalProcs = 0;
        for (int ip2 = 0; ip2 < procMap.size(); ip2++) {
            String procName = procMap.keyAt(ip2);
            SparseArray<ProcessState> uids2 = procMap.valueAt(ip2);
            for (int iu2 = 0; iu2 < uids2.size(); iu2++) {
                int uid2 = uids2.keyAt(iu2);
                numTotalProcs++;
                ProcessState proc3 = uids2.valueAt(iu2);
                if (!(proc3.mDurationsTableSize == 0 && proc3.mCurState == -1 && proc3.mPssTableSize == 0) && proc3.mMultiPackage && (reqPackage == null || reqPackage.equals(procName) || reqPackage.equals(proc3.mPackage))) {
                    numShownProcs++;
                    if (sepNeeded) {
                        pw.println();
                    }
                    sepNeeded = true;
                    if (!printedHeader2) {
                        pw.println("Multi-Package Common Processes:");
                        printedHeader2 = true;
                    }
                    if (!activeOnly || proc3.isInUse()) {
                        pw.print("  * ");
                        pw.print(procName);
                        pw.print(" / ");
                        UserHandle.formatUid(pw, uid2);
                        pw.print(" (");
                        pw.print(proc3.mDurationsTableSize);
                        pw.print(" entries)");
                        pw.println(":");
                        dumpProcessState(pw, "        ", proc3, ALL_SCREEN_ADJ, ALL_MEM_ADJ, ALL_PROC_STATES, now);
                        dumpProcessPss(pw, "        ", proc3, ALL_SCREEN_ADJ, ALL_MEM_ADJ, ALL_PROC_STATES);
                        dumpProcessInternalLocked(pw, "        ", proc3, dumpAll);
                    } else {
                        pw.print("      (Not active: ");
                        pw.print(procName);
                        pw.println(")");
                    }
                }
            }
        }
        if (dumpAll) {
            pw.println();
            pw.print("  Total procs: ");
            pw.print(numShownProcs);
            pw.print(" shown of ");
            pw.print(numTotalProcs);
            pw.println(" total");
        }
        if (sepNeeded) {
            pw.println();
        }
        if (dumpSummary) {
            pw.println("Summary:");
            dumpSummaryLocked(pw, reqPackage, now, activeOnly);
        } else {
            dumpTotalsLocked(pw, now);
        }
        if (dumpAll) {
            pw.println();
            pw.println("Internal state:");
            pw.print("  Num long arrays: ");
            pw.println(this.mLongs.size());
            pw.print("  Next long entry: ");
            pw.println(this.mNextLong);
            pw.print("  mRunning=");
            pw.println(this.mRunning);
        }
    }

    public static long dumpSingleServiceTime(PrintWriter pw, String prefix, ServiceState service, int serviceType, int curState, long curStartTime, long now) {
        long totalTime = 0;
        int printedScreen = -1;
        int iscreen = 0;
        while (iscreen < 8) {
            int printedMem = -1;
            int imem = 0;
            while (imem < 4) {
                int state = imem + iscreen;
                long time = service.getDuration(serviceType, curState, curStartTime, state, now);
                String running = ProxyInfo.LOCAL_EXCL_LIST;
                if (curState == state && pw != null) {
                    running = " (running)";
                }
                if (time != 0) {
                    if (pw != null) {
                        pw.print(prefix);
                        printScreenLabel(pw, printedScreen != iscreen ? iscreen : -1);
                        printedScreen = iscreen;
                        printMemLabel(pw, printedMem != imem ? imem : -1, 0);
                        printedMem = imem;
                        pw.print(": ");
                        TimeUtils.formatDuration(time, pw);
                        pw.println(running);
                    }
                    totalTime += time;
                }
                imem++;
            }
            iscreen += 4;
        }
        if (!(totalTime == 0 || pw == null)) {
            pw.print(prefix);
            pw.print("    TOTAL: ");
            TimeUtils.formatDuration(totalTime, pw);
            pw.println();
        }
        return totalTime;
    }

    /* access modifiers changed from: package-private */
    public void dumpServiceStats(PrintWriter pw, String prefix, String prefixInner, String headerPrefix, String header, ServiceState service, int count, int serviceType, int state, long startTime, long now, long totalTime, boolean dumpAll) {
        if (count == 0) {
            return;
        }
        if (dumpAll) {
            pw.print(prefix);
            pw.print(header);
            pw.print(" op count ");
            pw.print(count);
            pw.println(":");
            dumpSingleServiceTime(pw, prefixInner, service, serviceType, state, startTime, now);
            return;
        }
        long myTime = dumpSingleServiceTime(null, null, service, serviceType, state, startTime, now);
        pw.print(prefix);
        pw.print(headerPrefix);
        pw.print(header);
        pw.print(" count ");
        pw.print(count);
        pw.print(" / time ");
        printPercent(pw, ((double) myTime) / ((double) totalTime));
        pw.println();
    }

    public void dumpSummaryLocked(PrintWriter pw, String reqPackage, long now, boolean activeOnly) {
        dumpFilteredSummaryLocked(pw, null, "  ", ALL_SCREEN_ADJ, ALL_MEM_ADJ, ALL_PROC_STATES, NON_CACHED_PROC_STATES, now, dumpSingleTime(null, null, this.mMemFactorDurations, this.mMemFactor, this.mStartTime, now), reqPackage, activeOnly);
        pw.println();
        dumpTotalsLocked(pw, now);
    }

    /* access modifiers changed from: package-private */
    public long printMemoryCategory(PrintWriter pw, String prefix, String label, double memWeight, long totalTime, long curTotalMem, int samples) {
        if (memWeight == 0.0d) {
            return curTotalMem;
        }
        long mem = (long) ((1024.0d * memWeight) / ((double) totalTime));
        pw.print(prefix);
        pw.print(label);
        pw.print(": ");
        printSizeValue(pw, mem);
        pw.print(" (");
        pw.print(samples);
        pw.print(" samples)");
        pw.println();
        return curTotalMem + mem;
    }

    /* access modifiers changed from: package-private */
    public void dumpTotalsLocked(PrintWriter pw, long now) {
        pw.println("Run time Stats:");
        dumpSingleTime(pw, "  ", this.mMemFactorDurations, this.mMemFactor, this.mStartTime, now);
        pw.println();
        pw.println("Memory usage:");
        TotalMemoryUseCollection totalMem = new TotalMemoryUseCollection(ALL_SCREEN_ADJ, ALL_MEM_ADJ);
        computeTotalMemoryUse(totalMem, now);
        long totalPss = printMemoryCategory(pw, "  ", "Native ", totalMem.sysMemNativeWeight, totalMem.totalTime, printMemoryCategory(pw, "  ", "Kernel ", totalMem.sysMemKernelWeight, totalMem.totalTime, 0, totalMem.sysMemSamples), totalMem.sysMemSamples);
        for (int i = 0; i < 14; i++) {
            if (i != 7) {
                totalPss = printMemoryCategory(pw, "  ", STATE_NAMES[i], totalMem.processStateWeight[i], totalMem.totalTime, totalPss, totalMem.processStateSamples[i]);
            }
        }
        long totalPss2 = printMemoryCategory(pw, "  ", "Z-Ram   ", totalMem.sysMemZRamWeight, totalMem.totalTime, printMemoryCategory(pw, "  ", "Free   ", totalMem.sysMemFreeWeight, totalMem.totalTime, printMemoryCategory(pw, "  ", "Cached ", totalMem.sysMemCachedWeight, totalMem.totalTime, totalPss, totalMem.sysMemSamples), totalMem.sysMemSamples), totalMem.sysMemSamples);
        pw.print("  TOTAL  : ");
        printSizeValue(pw, totalPss2);
        pw.println();
        printMemoryCategory(pw, "  ", STATE_NAMES[7], totalMem.processStateWeight[7], totalMem.totalTime, totalPss2, totalMem.processStateSamples[7]);
        pw.println();
        pw.print("          Start time: ");
        pw.print(DateFormat.format("yyyy-MM-dd HH:mm:ss", this.mTimePeriodStartClock));
        pw.println();
        pw.print("  Total elapsed time: ");
        TimeUtils.formatDuration((this.mRunning ? SystemClock.elapsedRealtime() : this.mTimePeriodEndRealtime) - this.mTimePeriodStartRealtime, pw);
        boolean partial = true;
        if ((this.mFlags & 2) != 0) {
            pw.print(" (shutdown)");
            partial = false;
        }
        if ((this.mFlags & 4) != 0) {
            pw.print(" (sysprops)");
            partial = false;
        }
        if ((this.mFlags & 1) != 0) {
            pw.print(" (complete)");
            partial = false;
        }
        if (partial) {
            pw.print(" (partial)");
        }
        pw.print(' ');
        pw.print(this.mRuntime);
        pw.println();
    }

    /* access modifiers changed from: package-private */
    public void dumpFilteredSummaryLocked(PrintWriter pw, String header, String prefix, int[] screenStates, int[] memStates, int[] procStates, int[] sortProcStates, long now, long totalTime, String reqPackage, boolean activeOnly) {
        ArrayList<ProcessState> procs = collectProcessesLocked(screenStates, memStates, procStates, sortProcStates, now, reqPackage, activeOnly);
        if (procs.size() > 0) {
            if (header != null) {
                pw.println();
                pw.println(header);
            }
            dumpProcessSummaryLocked(pw, prefix, procs, screenStates, memStates, sortProcStates, true, now, totalTime);
        }
    }

    public ArrayList<ProcessState> collectProcessesLocked(int[] screenStates, int[] memStates, int[] procStates, int[] sortProcStates, long now, String reqPackage, boolean activeOnly) {
        ArraySet<ProcessState> foundProcs = new ArraySet<>();
        ArrayMap<String, SparseArray<SparseArray<PackageState>>> pkgMap = this.mPackages.getMap();
        for (int ip = 0; ip < pkgMap.size(); ip++) {
            String pkgName = pkgMap.keyAt(ip);
            SparseArray<SparseArray<PackageState>> procs = pkgMap.valueAt(ip);
            for (int iu = 0; iu < procs.size(); iu++) {
                SparseArray<PackageState> vpkgs = procs.valueAt(iu);
                int NVERS = vpkgs.size();
                for (int iv = 0; iv < NVERS; iv++) {
                    PackageState state = vpkgs.valueAt(iv);
                    int NPROCS = state.mProcesses.size();
                    boolean pkgMatch = reqPackage == null || reqPackage.equals(pkgName);
                    for (int iproc = 0; iproc < NPROCS; iproc++) {
                        ProcessState proc = state.mProcesses.valueAt(iproc);
                        if ((pkgMatch || reqPackage.equals(proc.mName)) && (!activeOnly || proc.isInUse())) {
                            foundProcs.add(proc.mCommonProcess);
                        }
                    }
                }
            }
        }
        ArrayList<ProcessState> outProcs = new ArrayList<>(foundProcs.size());
        for (int i = 0; i < foundProcs.size(); i++) {
            ProcessState proc2 = foundProcs.valueAt(i);
            if (computeProcessTimeLocked(proc2, screenStates, memStates, procStates, now) > 0) {
                outProcs.add(proc2);
                if (procStates != sortProcStates) {
                    computeProcessTimeLocked(proc2, screenStates, memStates, sortProcStates, now);
                }
            }
        }
        Collections.sort(outProcs, new Comparator<ProcessState>() {
            /* class com.android.internal.app.ProcessStats.AnonymousClass2 */

            public int compare(ProcessState lhs, ProcessState rhs) {
                if (lhs.mTmpTotalTime < rhs.mTmpTotalTime) {
                    return -1;
                }
                if (lhs.mTmpTotalTime > rhs.mTmpTotalTime) {
                    return 1;
                }
                return 0;
            }
        });
        return outProcs;
    }

    /* access modifiers changed from: package-private */
    public String collapseString(String pkgName, String itemName) {
        if (!itemName.startsWith(pkgName)) {
            return itemName;
        }
        int ITEMLEN = itemName.length();
        int PKGLEN = pkgName.length();
        if (ITEMLEN == PKGLEN) {
            return ProxyInfo.LOCAL_EXCL_LIST;
        }
        if (ITEMLEN < PKGLEN || itemName.charAt(PKGLEN) != '.') {
            return itemName;
        }
        return itemName.substring(PKGLEN);
    }

    public void dumpCheckinLocked(PrintWriter pw, String reqPackage) {
        long j;
        long now = SystemClock.uptimeMillis();
        ArrayMap<String, SparseArray<SparseArray<PackageState>>> pkgMap = this.mPackages.getMap();
        pw.println("vers,5");
        pw.print("period,");
        pw.print(this.mTimePeriodStartClockStr);
        pw.print(",");
        pw.print(this.mTimePeriodStartRealtime);
        pw.print(",");
        if (this.mRunning) {
            j = SystemClock.elapsedRealtime();
        } else {
            j = this.mTimePeriodEndRealtime;
        }
        pw.print(j);
        boolean partial = true;
        if ((this.mFlags & 2) != 0) {
            pw.print(",shutdown");
            partial = false;
        }
        if ((this.mFlags & 4) != 0) {
            pw.print(",sysprops");
            partial = false;
        }
        if ((this.mFlags & 1) != 0) {
            pw.print(",complete");
            partial = false;
        }
        if (partial) {
            pw.print(",partial");
        }
        pw.println();
        pw.print("config,");
        pw.println(this.mRuntime);
        for (int ip = 0; ip < pkgMap.size(); ip++) {
            String pkgName = pkgMap.keyAt(ip);
            if (reqPackage == null || reqPackage.equals(pkgName)) {
                SparseArray<SparseArray<PackageState>> uids = pkgMap.valueAt(ip);
                for (int iu = 0; iu < uids.size(); iu++) {
                    int uid = uids.keyAt(iu);
                    SparseArray<PackageState> vpkgs = uids.valueAt(iu);
                    for (int iv = 0; iv < vpkgs.size(); iv++) {
                        int vers = vpkgs.keyAt(iv);
                        PackageState pkgState = vpkgs.valueAt(iv);
                        int NPROCS = pkgState.mProcesses.size();
                        int NSRVS = pkgState.mServices.size();
                        for (int iproc = 0; iproc < NPROCS; iproc++) {
                            ProcessState proc = pkgState.mProcesses.valueAt(iproc);
                            pw.print("pkgproc,");
                            pw.print(pkgName);
                            pw.print(",");
                            pw.print(uid);
                            pw.print(",");
                            pw.print(vers);
                            pw.print(",");
                            pw.print(collapseString(pkgName, pkgState.mProcesses.keyAt(iproc)));
                            dumpAllProcessStateCheckin(pw, proc, now);
                            pw.println();
                            if (proc.mPssTableSize > 0) {
                                pw.print("pkgpss,");
                                pw.print(pkgName);
                                pw.print(",");
                                pw.print(uid);
                                pw.print(",");
                                pw.print(vers);
                                pw.print(",");
                                pw.print(collapseString(pkgName, pkgState.mProcesses.keyAt(iproc)));
                                dumpAllProcessPssCheckin(pw, proc);
                                pw.println();
                            }
                            if (proc.mNumExcessiveWake > 0 || proc.mNumExcessiveCpu > 0 || proc.mNumCachedKill > 0) {
                                pw.print("pkgkills,");
                                pw.print(pkgName);
                                pw.print(",");
                                pw.print(uid);
                                pw.print(",");
                                pw.print(vers);
                                pw.print(",");
                                pw.print(collapseString(pkgName, pkgState.mProcesses.keyAt(iproc)));
                                pw.print(",");
                                pw.print(proc.mNumExcessiveWake);
                                pw.print(",");
                                pw.print(proc.mNumExcessiveCpu);
                                pw.print(",");
                                pw.print(proc.mNumCachedKill);
                                pw.print(",");
                                pw.print(proc.mMinCachedKillPss);
                                pw.print(":");
                                pw.print(proc.mAvgCachedKillPss);
                                pw.print(":");
                                pw.print(proc.mMaxCachedKillPss);
                                pw.println();
                            }
                        }
                        for (int isvc = 0; isvc < NSRVS; isvc++) {
                            String serviceName = collapseString(pkgName, pkgState.mServices.keyAt(isvc));
                            ServiceState svc = pkgState.mServices.valueAt(isvc);
                            dumpServiceTimeCheckin(pw, "pkgsvc-run", pkgName, uid, vers, serviceName, svc, 0, svc.mRunCount, svc.mRunState, svc.mRunStartTime, now);
                            dumpServiceTimeCheckin(pw, "pkgsvc-start", pkgName, uid, vers, serviceName, svc, 1, svc.mStartedCount, svc.mStartedState, svc.mStartedStartTime, now);
                            dumpServiceTimeCheckin(pw, "pkgsvc-bound", pkgName, uid, vers, serviceName, svc, 2, svc.mBoundCount, svc.mBoundState, svc.mBoundStartTime, now);
                            dumpServiceTimeCheckin(pw, "pkgsvc-exec", pkgName, uid, vers, serviceName, svc, 3, svc.mExecCount, svc.mExecState, svc.mExecStartTime, now);
                        }
                    }
                }
            }
        }
        ArrayMap<String, SparseArray<ProcessState>> procMap = this.mProcesses.getMap();
        for (int ip2 = 0; ip2 < procMap.size(); ip2++) {
            String procName = procMap.keyAt(ip2);
            SparseArray<ProcessState> uids2 = procMap.valueAt(ip2);
            for (int iu2 = 0; iu2 < uids2.size(); iu2++) {
                int uid2 = uids2.keyAt(iu2);
                ProcessState procState = uids2.valueAt(iu2);
                if (procState.mDurationsTableSize > 0) {
                    pw.print("proc,");
                    pw.print(procName);
                    pw.print(",");
                    pw.print(uid2);
                    dumpAllProcessStateCheckin(pw, procState, now);
                    pw.println();
                }
                if (procState.mPssTableSize > 0) {
                    pw.print("pss,");
                    pw.print(procName);
                    pw.print(",");
                    pw.print(uid2);
                    dumpAllProcessPssCheckin(pw, procState);
                    pw.println();
                }
                if (procState.mNumExcessiveWake > 0 || procState.mNumExcessiveCpu > 0 || procState.mNumCachedKill > 0) {
                    pw.print("kills,");
                    pw.print(procName);
                    pw.print(",");
                    pw.print(uid2);
                    pw.print(",");
                    pw.print(procState.mNumExcessiveWake);
                    pw.print(",");
                    pw.print(procState.mNumExcessiveCpu);
                    pw.print(",");
                    pw.print(procState.mNumCachedKill);
                    pw.print(",");
                    pw.print(procState.mMinCachedKillPss);
                    pw.print(":");
                    pw.print(procState.mAvgCachedKillPss);
                    pw.print(":");
                    pw.print(procState.mMaxCachedKillPss);
                    pw.println();
                }
            }
        }
        pw.print("total");
        dumpAdjTimesCheckin(pw, ",", this.mMemFactorDurations, this.mMemFactor, this.mStartTime, now);
        if (this.mSysMemUsageTable != null) {
            pw.print("sysmemusage");
            for (int i = 0; i < this.mSysMemUsageTableSize; i++) {
                int off = this.mSysMemUsageTable[i];
                pw.print(",");
                printProcStateTag(pw, (off >> OFFSET_TYPE_SHIFT) & OFFSET_TYPE_MASK);
                for (int j2 = 0; j2 < 16; j2++) {
                    if (j2 > 1) {
                        pw.print(":");
                    }
                    pw.print(getLong(off, j2));
                }
            }
        }
        pw.println();
        TotalMemoryUseCollection totalMem = new TotalMemoryUseCollection(ALL_SCREEN_ADJ, ALL_MEM_ADJ);
        computeTotalMemoryUse(totalMem, now);
        pw.print("weights,");
        pw.print(totalMem.totalTime);
        pw.print(",");
        pw.print(totalMem.sysMemCachedWeight);
        pw.print(":");
        pw.print(totalMem.sysMemSamples);
        pw.print(",");
        pw.print(totalMem.sysMemFreeWeight);
        pw.print(":");
        pw.print(totalMem.sysMemSamples);
        pw.print(",");
        pw.print(totalMem.sysMemZRamWeight);
        pw.print(":");
        pw.print(totalMem.sysMemSamples);
        pw.print(",");
        pw.print(totalMem.sysMemKernelWeight);
        pw.print(":");
        pw.print(totalMem.sysMemSamples);
        pw.print(",");
        pw.print(totalMem.sysMemNativeWeight);
        pw.print(":");
        pw.print(totalMem.sysMemSamples);
        for (int i2 = 0; i2 < 14; i2++) {
            pw.print(",");
            pw.print(totalMem.processStateWeight[i2]);
            pw.print(":");
            pw.print(totalMem.processStateSamples[i2]);
        }
        pw.println();
    }

    public static class DurationsTable {
        public int[] mDurationsTable;
        public int mDurationsTableSize;
        public final String mName;
        public final ProcessStats mStats;

        public DurationsTable(ProcessStats stats, String name) {
            this.mStats = stats;
            this.mName = name;
        }

        /* access modifiers changed from: package-private */
        public void copyDurationsTo(DurationsTable other) {
            if (this.mDurationsTable != null) {
                this.mStats.mAddLongTable = new int[this.mDurationsTable.length];
                this.mStats.mAddLongTableSize = 0;
                for (int i = 0; i < this.mDurationsTableSize; i++) {
                    int origEnt = this.mDurationsTable[i];
                    int type = (origEnt >> ProcessStats.OFFSET_TYPE_SHIFT) & ProcessStats.OFFSET_TYPE_MASK;
                    int newOff = this.mStats.addLongData(i, type, 1);
                    this.mStats.mAddLongTable[i] = newOff | type;
                    this.mStats.setLong(newOff, 0, this.mStats.getLong(origEnt, 0));
                }
                other.mDurationsTable = this.mStats.mAddLongTable;
                other.mDurationsTableSize = this.mStats.mAddLongTableSize;
                return;
            }
            other.mDurationsTable = null;
            other.mDurationsTableSize = 0;
        }

        /* access modifiers changed from: package-private */
        public void addDurations(DurationsTable other) {
            for (int i = 0; i < other.mDurationsTableSize; i++) {
                int ent = other.mDurationsTable[i];
                addDuration((ent >> ProcessStats.OFFSET_TYPE_SHIFT) & ProcessStats.OFFSET_TYPE_MASK, other.mStats.getLong(ent, 0));
            }
        }

        /* access modifiers changed from: package-private */
        public void resetDurationsSafely() {
            this.mDurationsTable = null;
            this.mDurationsTableSize = 0;
        }

        /* access modifiers changed from: package-private */
        public void writeDurationsToParcel(Parcel out) {
            out.writeInt(this.mDurationsTableSize);
            for (int i = 0; i < this.mDurationsTableSize; i++) {
                out.writeInt(this.mDurationsTable[i]);
            }
        }

        /* access modifiers changed from: package-private */
        public boolean readDurationsFromParcel(Parcel in) {
            int i = 0;
            this.mDurationsTable = this.mStats.readTableFromParcel(in, this.mName, "durations");
            if (this.mDurationsTable == ProcessStats.BAD_TABLE) {
                return false;
            }
            if (this.mDurationsTable != null) {
                i = this.mDurationsTable.length;
            }
            this.mDurationsTableSize = i;
            return true;
        }

        /* access modifiers changed from: package-private */
        public void addDuration(int state, long dur) {
            int off;
            int idx = ProcessStats.binarySearch(this.mDurationsTable, this.mDurationsTableSize, state);
            if (idx >= 0) {
                off = this.mDurationsTable[idx];
            } else {
                this.mStats.mAddLongTable = this.mDurationsTable;
                this.mStats.mAddLongTableSize = this.mDurationsTableSize;
                off = this.mStats.addLongData(idx ^ -1, state, 1);
                this.mDurationsTable = this.mStats.mAddLongTable;
                this.mDurationsTableSize = this.mStats.mAddLongTableSize;
            }
            long[] longs = this.mStats.mLongs.get((off >> ProcessStats.OFFSET_ARRAY_SHIFT) & ProcessStats.OFFSET_ARRAY_MASK);
            int i = (off >> ProcessStats.OFFSET_INDEX_SHIFT) & ProcessStats.OFFSET_INDEX_MASK;
            longs[i] = longs[i] + dur;
        }

        /* access modifiers changed from: package-private */
        public long getDuration(int state, long now) {
            int idx = ProcessStats.binarySearch(this.mDurationsTable, this.mDurationsTableSize, state);
            if (idx >= 0) {
                return this.mStats.getLong(this.mDurationsTable[idx], 0);
            }
            return 0;
        }
    }

    public static final class ProcessStateHolder {
        public final int appVersion;
        public ProcessState state;

        public ProcessStateHolder(int _appVersion) {
            this.appVersion = _appVersion;
        }
    }

    public static final class ProcessState extends DurationsTable {
        boolean mActive;
        long mAvgCachedKillPss;
        public ProcessState mCommonProcess;
        int mCurState;
        boolean mDead;
        int mLastPssState;
        long mLastPssTime;
        long mMaxCachedKillPss;
        long mMinCachedKillPss;
        boolean mMultiPackage;
        int mNumActiveServices;
        int mNumCachedKill;
        int mNumExcessiveCpu;
        int mNumExcessiveWake;
        int mNumStartedServices;
        public final String mPackage;
        int[] mPssTable;
        int mPssTableSize;
        long mStartTime;
        ProcessState mTmpFoundSubProc;
        int mTmpNumInUse;
        public long mTmpTotalTime;
        public final int mUid;
        public final int mVersion;

        public ProcessState(ProcessStats processStats, String pkg, int uid, int vers, String name) {
            super(processStats, name);
            this.mCurState = -1;
            this.mLastPssState = -1;
            this.mCommonProcess = this;
            this.mPackage = pkg;
            this.mUid = uid;
            this.mVersion = vers;
        }

        public ProcessState(ProcessState commonProcess, String pkg, int uid, int vers, String name, long now) {
            super(commonProcess.mStats, name);
            this.mCurState = -1;
            this.mLastPssState = -1;
            this.mCommonProcess = commonProcess;
            this.mPackage = pkg;
            this.mUid = uid;
            this.mVersion = vers;
            this.mCurState = commonProcess.mCurState;
            this.mStartTime = now;
        }

        /* access modifiers changed from: package-private */
        public ProcessState clone(String pkg, long now) {
            ProcessState pnew = new ProcessState(this, pkg, this.mUid, this.mVersion, this.mName, now);
            copyDurationsTo(pnew);
            if (this.mPssTable != null) {
                this.mStats.mAddLongTable = new int[this.mPssTable.length];
                this.mStats.mAddLongTableSize = 0;
                for (int i = 0; i < this.mPssTableSize; i++) {
                    int origEnt = this.mPssTable[i];
                    int type = (origEnt >> ProcessStats.OFFSET_TYPE_SHIFT) & ProcessStats.OFFSET_TYPE_MASK;
                    int newOff = this.mStats.addLongData(i, type, 7);
                    this.mStats.mAddLongTable[i] = newOff | type;
                    for (int j = 0; j < 7; j++) {
                        this.mStats.setLong(newOff, j, this.mStats.getLong(origEnt, j));
                    }
                }
                pnew.mPssTable = this.mStats.mAddLongTable;
                pnew.mPssTableSize = this.mStats.mAddLongTableSize;
            }
            pnew.mNumExcessiveWake = this.mNumExcessiveWake;
            pnew.mNumExcessiveCpu = this.mNumExcessiveCpu;
            pnew.mNumCachedKill = this.mNumCachedKill;
            pnew.mMinCachedKillPss = this.mMinCachedKillPss;
            pnew.mAvgCachedKillPss = this.mAvgCachedKillPss;
            pnew.mMaxCachedKillPss = this.mMaxCachedKillPss;
            pnew.mActive = this.mActive;
            pnew.mNumActiveServices = this.mNumActiveServices;
            pnew.mNumStartedServices = this.mNumStartedServices;
            return pnew;
        }

        /* access modifiers changed from: package-private */
        public void add(ProcessState other) {
            addDurations(other);
            for (int i = 0; i < other.mPssTableSize; i++) {
                int ent = other.mPssTable[i];
                addPss((ent >> ProcessStats.OFFSET_TYPE_SHIFT) & ProcessStats.OFFSET_TYPE_MASK, (int) other.mStats.getLong(ent, 0), other.mStats.getLong(ent, 1), other.mStats.getLong(ent, 2), other.mStats.getLong(ent, 3), other.mStats.getLong(ent, 4), other.mStats.getLong(ent, 5), other.mStats.getLong(ent, 6));
            }
            this.mNumExcessiveWake += other.mNumExcessiveWake;
            this.mNumExcessiveCpu += other.mNumExcessiveCpu;
            if (other.mNumCachedKill > 0) {
                addCachedKill(other.mNumCachedKill, other.mMinCachedKillPss, other.mAvgCachedKillPss, other.mMaxCachedKillPss);
            }
        }

        /* access modifiers changed from: package-private */
        public void resetSafely(long now) {
            resetDurationsSafely();
            this.mStartTime = now;
            this.mLastPssState = -1;
            this.mLastPssTime = 0;
            this.mPssTable = null;
            this.mPssTableSize = 0;
            this.mNumExcessiveWake = 0;
            this.mNumExcessiveCpu = 0;
            this.mNumCachedKill = 0;
            this.mMaxCachedKillPss = 0;
            this.mAvgCachedKillPss = 0;
            this.mMinCachedKillPss = 0;
        }

        /* access modifiers changed from: package-private */
        public void makeDead() {
            this.mDead = true;
        }

        private void ensureNotDead() {
            if (this.mDead) {
                Slog.wtfStack(ProcessStats.TAG, "ProcessState dead: name=" + this.mName + " pkg=" + this.mPackage + " uid=" + this.mUid + " common.name=" + this.mCommonProcess.mName);
            }
        }

        /* access modifiers changed from: package-private */
        public void writeToParcel(Parcel out, long now) {
            out.writeInt(this.mMultiPackage ? 1 : 0);
            writeDurationsToParcel(out);
            out.writeInt(this.mPssTableSize);
            for (int i = 0; i < this.mPssTableSize; i++) {
                out.writeInt(this.mPssTable[i]);
            }
            out.writeInt(this.mNumExcessiveWake);
            out.writeInt(this.mNumExcessiveCpu);
            out.writeInt(this.mNumCachedKill);
            if (this.mNumCachedKill > 0) {
                out.writeLong(this.mMinCachedKillPss);
                out.writeLong(this.mAvgCachedKillPss);
                out.writeLong(this.mMaxCachedKillPss);
            }
        }

        /* access modifiers changed from: package-private */
        public boolean readFromParcel(Parcel in, boolean fully) {
            boolean multiPackage;
            int i = 0;
            if (in.readInt() != 0) {
                multiPackage = true;
            } else {
                multiPackage = false;
            }
            if (fully) {
                this.mMultiPackage = multiPackage;
            }
            if (!readDurationsFromParcel(in)) {
                return false;
            }
            this.mPssTable = this.mStats.readTableFromParcel(in, this.mName, "pss");
            if (this.mPssTable == ProcessStats.BAD_TABLE) {
                return false;
            }
            if (this.mPssTable != null) {
                i = this.mPssTable.length;
            }
            this.mPssTableSize = i;
            this.mNumExcessiveWake = in.readInt();
            this.mNumExcessiveCpu = in.readInt();
            this.mNumCachedKill = in.readInt();
            if (this.mNumCachedKill > 0) {
                this.mMinCachedKillPss = in.readLong();
                this.mAvgCachedKillPss = in.readLong();
                this.mMaxCachedKillPss = in.readLong();
            } else {
                this.mMaxCachedKillPss = 0;
                this.mAvgCachedKillPss = 0;
                this.mMinCachedKillPss = 0;
            }
            return true;
        }

        public void makeActive() {
            ensureNotDead();
            this.mActive = true;
        }

        public void makeInactive() {
            this.mActive = false;
        }

        public boolean isInUse() {
            return this.mActive || this.mNumActiveServices > 0 || this.mNumStartedServices > 0 || this.mCurState != -1;
        }

        public void setState(int state, int memFactor, long now, ArrayMap<String, ProcessStateHolder> pkgList) {
            int state2;
            if (state < 0) {
                state2 = this.mNumStartedServices > 0 ? (memFactor * 14) + 7 : -1;
            } else {
                state2 = ProcessStats.PROCESS_STATE_TO_STATE[state] + (memFactor * 14);
            }
            this.mCommonProcess.setState(state2, now);
            if (this.mCommonProcess.mMultiPackage && pkgList != null) {
                for (int ip = pkgList.size() - 1; ip >= 0; ip--) {
                    pullFixedProc(pkgList, ip).setState(state2, now);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void setState(int state, long now) {
            ensureNotDead();
            if (this.mCurState != state) {
                commitStateTime(now);
                this.mCurState = state;
            }
        }

        /* access modifiers changed from: package-private */
        public void commitStateTime(long now) {
            if (this.mCurState != -1) {
                long dur = now - this.mStartTime;
                if (dur > 0) {
                    addDuration(this.mCurState, dur);
                }
            }
            this.mStartTime = now;
        }

        /* access modifiers changed from: package-private */
        public void incActiveServices(String serviceName) {
            if (this.mCommonProcess != this) {
                this.mCommonProcess.incActiveServices(serviceName);
            }
            this.mNumActiveServices++;
        }

        /* access modifiers changed from: package-private */
        public void decActiveServices(String serviceName) {
            if (this.mCommonProcess != this) {
                this.mCommonProcess.decActiveServices(serviceName);
            }
            this.mNumActiveServices--;
            if (this.mNumActiveServices < 0) {
                Slog.wtfStack(ProcessStats.TAG, "Proc active services underrun: pkg=" + this.mPackage + " uid=" + this.mUid + " proc=" + this.mName + " service=" + serviceName);
                this.mNumActiveServices = 0;
            }
        }

        /* access modifiers changed from: package-private */
        public void incStartedServices(int memFactor, long now, String serviceName) {
            if (this.mCommonProcess != this) {
                this.mCommonProcess.incStartedServices(memFactor, now, serviceName);
            }
            this.mNumStartedServices++;
            if (this.mNumStartedServices == 1 && this.mCurState == -1) {
                setState((memFactor * 14) + 7, now);
            }
        }

        /* access modifiers changed from: package-private */
        public void decStartedServices(int memFactor, long now, String serviceName) {
            if (this.mCommonProcess != this) {
                this.mCommonProcess.decStartedServices(memFactor, now, serviceName);
            }
            this.mNumStartedServices--;
            if (this.mNumStartedServices == 0 && this.mCurState % 14 == 7) {
                setState(-1, now);
            } else if (this.mNumStartedServices < 0) {
                Slog.wtfStack(ProcessStats.TAG, "Proc started services underrun: pkg=" + this.mPackage + " uid=" + this.mUid + " name=" + this.mName);
                this.mNumStartedServices = 0;
            }
        }

        public void addPss(long pss, long uss, boolean always, ArrayMap<String, ProcessStateHolder> pkgList) {
            ensureNotDead();
            if (always || this.mLastPssState != this.mCurState || SystemClock.uptimeMillis() >= this.mLastPssTime + 30000) {
                this.mLastPssState = this.mCurState;
                this.mLastPssTime = SystemClock.uptimeMillis();
                if (this.mCurState != -1) {
                    this.mCommonProcess.addPss(this.mCurState, 1, pss, pss, pss, uss, uss, uss);
                    if (this.mCommonProcess.mMultiPackage && pkgList != null) {
                        for (int ip = pkgList.size() - 1; ip >= 0; ip--) {
                            pullFixedProc(pkgList, ip).addPss(this.mCurState, 1, pss, pss, pss, uss, uss, uss);
                        }
                    }
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void addPss(int state, int inCount, long minPss, long avgPss, long maxPss, long minUss, long avgUss, long maxUss) {
            int off;
            int idx = ProcessStats.binarySearch(this.mPssTable, this.mPssTableSize, state);
            if (idx >= 0) {
                off = this.mPssTable[idx];
            } else {
                this.mStats.mAddLongTable = this.mPssTable;
                this.mStats.mAddLongTableSize = this.mPssTableSize;
                off = this.mStats.addLongData(idx ^ -1, state, 7);
                this.mPssTable = this.mStats.mAddLongTable;
                this.mPssTableSize = this.mStats.mAddLongTableSize;
            }
            long[] longs = this.mStats.mLongs.get((off >> ProcessStats.OFFSET_ARRAY_SHIFT) & ProcessStats.OFFSET_ARRAY_MASK);
            int idx2 = (off >> ProcessStats.OFFSET_INDEX_SHIFT) & ProcessStats.OFFSET_INDEX_MASK;
            long count = longs[idx2 + 0];
            if (count == 0) {
                longs[idx2 + 0] = (long) inCount;
                longs[idx2 + 1] = minPss;
                longs[idx2 + 2] = avgPss;
                longs[idx2 + 3] = maxPss;
                longs[idx2 + 4] = minUss;
                longs[idx2 + 5] = avgUss;
                longs[idx2 + 6] = maxUss;
                return;
            }
            longs[idx2 + 0] = ((long) inCount) + count;
            if (longs[idx2 + 1] > minPss) {
                longs[idx2 + 1] = minPss;
            }
            longs[idx2 + 2] = (long) (((((double) longs[idx2 + 2]) * ((double) count)) + (((double) avgPss) * ((double) inCount))) / ((double) (((long) inCount) + count)));
            if (longs[idx2 + 3] < maxPss) {
                longs[idx2 + 3] = maxPss;
            }
            if (longs[idx2 + 4] > minUss) {
                longs[idx2 + 4] = minUss;
            }
            longs[idx2 + 5] = (long) (((((double) longs[idx2 + 5]) * ((double) count)) + (((double) avgUss) * ((double) inCount))) / ((double) (((long) inCount) + count)));
            if (longs[idx2 + 6] < maxUss) {
                longs[idx2 + 6] = maxUss;
            }
        }

        public void reportExcessiveWake(ArrayMap<String, ProcessStateHolder> pkgList) {
            ensureNotDead();
            this.mCommonProcess.mNumExcessiveWake++;
            if (this.mCommonProcess.mMultiPackage) {
                for (int ip = pkgList.size() - 1; ip >= 0; ip--) {
                    pullFixedProc(pkgList, ip).mNumExcessiveWake++;
                }
            }
        }

        public void reportExcessiveCpu(ArrayMap<String, ProcessStateHolder> pkgList) {
            ensureNotDead();
            this.mCommonProcess.mNumExcessiveCpu++;
            if (this.mCommonProcess.mMultiPackage) {
                for (int ip = pkgList.size() - 1; ip >= 0; ip--) {
                    pullFixedProc(pkgList, ip).mNumExcessiveCpu++;
                }
            }
        }

        private void addCachedKill(int num, long minPss, long avgPss, long maxPss) {
            if (this.mNumCachedKill <= 0) {
                this.mNumCachedKill = num;
                this.mMinCachedKillPss = minPss;
                this.mAvgCachedKillPss = avgPss;
                this.mMaxCachedKillPss = maxPss;
                return;
            }
            if (minPss < this.mMinCachedKillPss) {
                this.mMinCachedKillPss = minPss;
            }
            if (maxPss > this.mMaxCachedKillPss) {
                this.mMaxCachedKillPss = maxPss;
            }
            this.mAvgCachedKillPss = (long) (((((double) this.mAvgCachedKillPss) * ((double) this.mNumCachedKill)) + ((double) avgPss)) / ((double) (this.mNumCachedKill + num)));
            this.mNumCachedKill += num;
        }

        public void reportCachedKill(ArrayMap<String, ProcessStateHolder> pkgList, long pss) {
            ensureNotDead();
            this.mCommonProcess.addCachedKill(1, pss, pss, pss);
            if (this.mCommonProcess.mMultiPackage) {
                for (int ip = pkgList.size() - 1; ip >= 0; ip--) {
                    pullFixedProc(pkgList, ip).addCachedKill(1, pss, pss, pss);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public ProcessState pullFixedProc(String pkgName) {
            if (!this.mMultiPackage) {
                return this;
            }
            SparseArray<PackageState> vpkg = this.mStats.mPackages.get(pkgName, this.mUid);
            if (vpkg == null) {
                throw new IllegalStateException("Didn't find package " + pkgName + " / " + this.mUid);
            }
            PackageState pkg = vpkg.get(this.mVersion);
            if (pkg == null) {
                throw new IllegalStateException("Didn't find package " + pkgName + " / " + this.mUid + " vers " + this.mVersion);
            }
            ProcessState proc = pkg.mProcesses.get(this.mName);
            if (proc != null) {
                return proc;
            }
            throw new IllegalStateException("Didn't create per-package process " + this.mName + " in pkg " + pkgName + " / " + this.mUid + " vers " + this.mVersion);
        }

        private ProcessState pullFixedProc(ArrayMap<String, ProcessStateHolder> pkgList, int index) {
            ProcessStateHolder holder = pkgList.valueAt(index);
            ProcessState proc = holder.state;
            if (this.mDead && proc.mCommonProcess != proc) {
                Log.wtf(ProcessStats.TAG, "Pulling dead proc: name=" + this.mName + " pkg=" + this.mPackage + " uid=" + this.mUid + " common.name=" + this.mCommonProcess.mName);
                proc = this.mStats.getProcessStateLocked(proc.mPackage, proc.mUid, proc.mVersion, proc.mName);
            }
            if (proc.mMultiPackage) {
                SparseArray<PackageState> vpkg = this.mStats.mPackages.get(pkgList.keyAt(index), proc.mUid);
                if (vpkg == null) {
                    throw new IllegalStateException("No existing package " + pkgList.keyAt(index) + "/" + proc.mUid + " for multi-proc " + proc.mName);
                }
                PackageState pkg = vpkg.get(proc.mVersion);
                if (pkg == null) {
                    throw new IllegalStateException("No existing package " + pkgList.keyAt(index) + "/" + proc.mUid + " for multi-proc " + proc.mName + " version " + proc.mVersion);
                }
                proc = pkg.mProcesses.get(proc.mName);
                if (proc == null) {
                    throw new IllegalStateException("Didn't create per-package process " + proc.mName + " in pkg " + pkg.mPackageName + "/" + pkg.mUid);
                }
                holder.state = proc;
            }
            return proc;
        }

        /* access modifiers changed from: package-private */
        @Override // com.android.internal.app.ProcessStats.DurationsTable
        public long getDuration(int state, long now) {
            long time = super.getDuration(state, now);
            if (this.mCurState == state) {
                return time + (now - this.mStartTime);
            }
            return time;
        }

        /* access modifiers changed from: package-private */
        public long getPssSampleCount(int state) {
            int idx = ProcessStats.binarySearch(this.mPssTable, this.mPssTableSize, state);
            if (idx >= 0) {
                return this.mStats.getLong(this.mPssTable[idx], 0);
            }
            return 0;
        }

        /* access modifiers changed from: package-private */
        public long getPssMinimum(int state) {
            int idx = ProcessStats.binarySearch(this.mPssTable, this.mPssTableSize, state);
            if (idx >= 0) {
                return this.mStats.getLong(this.mPssTable[idx], 1);
            }
            return 0;
        }

        /* access modifiers changed from: package-private */
        public long getPssAverage(int state) {
            int idx = ProcessStats.binarySearch(this.mPssTable, this.mPssTableSize, state);
            if (idx >= 0) {
                return this.mStats.getLong(this.mPssTable[idx], 2);
            }
            return 0;
        }

        /* access modifiers changed from: package-private */
        public long getPssMaximum(int state) {
            int idx = ProcessStats.binarySearch(this.mPssTable, this.mPssTableSize, state);
            if (idx >= 0) {
                return this.mStats.getLong(this.mPssTable[idx], 3);
            }
            return 0;
        }

        /* access modifiers changed from: package-private */
        public long getPssUssMinimum(int state) {
            int idx = ProcessStats.binarySearch(this.mPssTable, this.mPssTableSize, state);
            if (idx >= 0) {
                return this.mStats.getLong(this.mPssTable[idx], 4);
            }
            return 0;
        }

        /* access modifiers changed from: package-private */
        public long getPssUssAverage(int state) {
            int idx = ProcessStats.binarySearch(this.mPssTable, this.mPssTableSize, state);
            if (idx >= 0) {
                return this.mStats.getLong(this.mPssTable[idx], 5);
            }
            return 0;
        }

        /* access modifiers changed from: package-private */
        public long getPssUssMaximum(int state) {
            int idx = ProcessStats.binarySearch(this.mPssTable, this.mPssTableSize, state);
            if (idx >= 0) {
                return this.mStats.getLong(this.mPssTable[idx], 6);
            }
            return 0;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(128);
            sb.append("ProcessState{").append(Integer.toHexString(System.identityHashCode(this))).append(" ").append(this.mName).append("/").append(this.mUid).append(" pkg=").append(this.mPackage);
            if (this.mMultiPackage) {
                sb.append(" (multi)");
            }
            if (this.mCommonProcess != this) {
                sb.append(" (sub)");
            }
            sb.append("}");
            return sb.toString();
        }
    }

    public static final class ServiceState extends DurationsTable {
        public static final int SERVICE_BOUND = 2;
        static final int SERVICE_COUNT = 4;
        public static final int SERVICE_EXEC = 3;
        public static final int SERVICE_RUN = 0;
        public static final int SERVICE_STARTED = 1;
        int mBoundCount;
        long mBoundStartTime;
        public int mBoundState = -1;
        int mExecCount;
        long mExecStartTime;
        public int mExecState = -1;
        Object mOwner;
        public final String mPackage;
        ProcessState mProc;
        public final String mProcessName;
        boolean mRestarting;
        int mRunCount;
        long mRunStartTime;
        public int mRunState = -1;
        boolean mStarted;
        int mStartedCount;
        long mStartedStartTime;
        public int mStartedState = -1;

        public ServiceState(ProcessStats processStats, String pkg, String name, String processName, ProcessState proc) {
            super(processStats, name);
            this.mPackage = pkg;
            this.mProcessName = processName;
            this.mProc = proc;
        }

        public void applyNewOwner(Object newOwner) {
            if (this.mOwner == newOwner) {
                return;
            }
            if (this.mOwner == null) {
                this.mOwner = newOwner;
                this.mProc.incActiveServices(this.mName);
                return;
            }
            this.mOwner = newOwner;
            if (this.mStarted || this.mBoundState != -1 || this.mExecState != -1) {
                long now = SystemClock.uptimeMillis();
                if (this.mStarted) {
                    setStarted(false, 0, now);
                }
                if (this.mBoundState != -1) {
                    setBound(false, 0, now);
                }
                if (this.mExecState != -1) {
                    setExecuting(false, 0, now);
                }
            }
        }

        public void clearCurrentOwner(Object owner, boolean silently) {
            if (this.mOwner == owner) {
                this.mProc.decActiveServices(this.mName);
                if (!(!this.mStarted && this.mBoundState == -1 && this.mExecState == -1)) {
                    long now = SystemClock.uptimeMillis();
                    if (this.mStarted) {
                        if (!silently) {
                            Slog.wtfStack(ProcessStats.TAG, "Service owner " + owner + " cleared while started: pkg=" + this.mPackage + " service=" + this.mName + " proc=" + this.mProc);
                        }
                        setStarted(false, 0, now);
                    }
                    if (this.mBoundState != -1) {
                        if (!silently) {
                            Slog.wtfStack(ProcessStats.TAG, "Service owner " + owner + " cleared while bound: pkg=" + this.mPackage + " service=" + this.mName + " proc=" + this.mProc);
                        }
                        setBound(false, 0, now);
                    }
                    if (this.mExecState != -1) {
                        if (!silently) {
                            Slog.wtfStack(ProcessStats.TAG, "Service owner " + owner + " cleared while exec: pkg=" + this.mPackage + " service=" + this.mName + " proc=" + this.mProc);
                        }
                        setExecuting(false, 0, now);
                    }
                }
                this.mOwner = null;
            }
        }

        public boolean isInUse() {
            return this.mOwner != null || this.mRestarting;
        }

        public boolean isRestarting() {
            return this.mRestarting;
        }

        /* access modifiers changed from: package-private */
        public void add(ServiceState other) {
            addDurations(other);
            this.mRunCount += other.mRunCount;
            this.mStartedCount += other.mStartedCount;
            this.mBoundCount += other.mBoundCount;
            this.mExecCount += other.mExecCount;
        }

        /* access modifiers changed from: package-private */
        public void resetSafely(long now) {
            int i;
            int i2;
            int i3 = 1;
            resetDurationsSafely();
            this.mRunCount = this.mRunState != -1 ? 1 : 0;
            if (this.mStartedState != -1) {
                i = 1;
            } else {
                i = 0;
            }
            this.mStartedCount = i;
            if (this.mBoundState != -1) {
                i2 = 1;
            } else {
                i2 = 0;
            }
            this.mBoundCount = i2;
            if (this.mExecState == -1) {
                i3 = 0;
            }
            this.mExecCount = i3;
            this.mExecStartTime = now;
            this.mBoundStartTime = now;
            this.mStartedStartTime = now;
            this.mRunStartTime = now;
        }

        /* access modifiers changed from: package-private */
        public void writeToParcel(Parcel out, long now) {
            writeDurationsToParcel(out);
            out.writeInt(this.mRunCount);
            out.writeInt(this.mStartedCount);
            out.writeInt(this.mBoundCount);
            out.writeInt(this.mExecCount);
        }

        /* access modifiers changed from: package-private */
        public boolean readFromParcel(Parcel in) {
            if (!readDurationsFromParcel(in)) {
                return false;
            }
            this.mRunCount = in.readInt();
            this.mStartedCount = in.readInt();
            this.mBoundCount = in.readInt();
            this.mExecCount = in.readInt();
            return true;
        }

        /* access modifiers changed from: package-private */
        public void commitStateTime(long now) {
            if (this.mRunState != -1) {
                addDuration((this.mRunState * 4) + 0, now - this.mRunStartTime);
                this.mRunStartTime = now;
            }
            if (this.mStartedState != -1) {
                addDuration((this.mStartedState * 4) + 1, now - this.mStartedStartTime);
                this.mStartedStartTime = now;
            }
            if (this.mBoundState != -1) {
                addDuration((this.mBoundState * 4) + 2, now - this.mBoundStartTime);
                this.mBoundStartTime = now;
            }
            if (this.mExecState != -1) {
                addDuration((this.mExecState * 4) + 3, now - this.mExecStartTime);
                this.mExecStartTime = now;
            }
        }

        private void updateRunning(int memFactor, long now) {
            int state;
            if (this.mStartedState == -1 && this.mBoundState == -1 && this.mExecState == -1) {
                state = -1;
            } else {
                state = memFactor;
            }
            if (this.mRunState != state) {
                if (this.mRunState != -1) {
                    addDuration((this.mRunState * 4) + 0, now - this.mRunStartTime);
                } else if (state != -1) {
                    this.mRunCount++;
                }
                this.mRunState = state;
                this.mRunStartTime = now;
            }
        }

        public void setStarted(boolean started, int memFactor, long now) {
            if (this.mOwner == null) {
                Slog.wtf(ProcessStats.TAG, "Starting service " + this + " without owner");
            }
            this.mStarted = started;
            updateStartedState(memFactor, now);
        }

        public void setRestarting(boolean restarting, int memFactor, long now) {
            this.mRestarting = restarting;
            updateStartedState(memFactor, now);
        }

        /* access modifiers changed from: package-private */
        public void updateStartedState(int memFactor, long now) {
            boolean wasStarted;
            boolean started;
            int state;
            if (this.mStartedState != -1) {
                wasStarted = true;
            } else {
                wasStarted = false;
            }
            if (this.mStarted || this.mRestarting) {
                started = true;
            } else {
                started = false;
            }
            if (started) {
                state = memFactor;
            } else {
                state = -1;
            }
            if (this.mStartedState != state) {
                if (this.mStartedState != -1) {
                    addDuration((this.mStartedState * 4) + 1, now - this.mStartedStartTime);
                } else if (started) {
                    this.mStartedCount++;
                }
                this.mStartedState = state;
                this.mStartedStartTime = now;
                this.mProc = this.mProc.pullFixedProc(this.mPackage);
                if (wasStarted != started) {
                    if (started) {
                        this.mProc.incStartedServices(memFactor, now, this.mName);
                    } else {
                        this.mProc.decStartedServices(memFactor, now, this.mName);
                    }
                }
                updateRunning(memFactor, now);
            }
        }

        public void setBound(boolean bound, int memFactor, long now) {
            int state;
            if (this.mOwner == null) {
                Slog.wtf(ProcessStats.TAG, "Binding service " + this + " without owner");
            }
            if (bound) {
                state = memFactor;
            } else {
                state = -1;
            }
            if (this.mBoundState != state) {
                if (this.mBoundState != -1) {
                    addDuration((this.mBoundState * 4) + 2, now - this.mBoundStartTime);
                } else if (bound) {
                    this.mBoundCount++;
                }
                this.mBoundState = state;
                this.mBoundStartTime = now;
                updateRunning(memFactor, now);
            }
        }

        public void setExecuting(boolean executing, int memFactor, long now) {
            int state;
            if (this.mOwner == null) {
                Slog.wtf(ProcessStats.TAG, "Executing service " + this + " without owner");
            }
            if (executing) {
                state = memFactor;
            } else {
                state = -1;
            }
            if (this.mExecState != state) {
                if (this.mExecState != -1) {
                    addDuration((this.mExecState * 4) + 3, now - this.mExecStartTime);
                } else if (executing) {
                    this.mExecCount++;
                }
                this.mExecState = state;
                this.mExecStartTime = now;
                updateRunning(memFactor, now);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private long getDuration(int opType, int curState, long startTime, int memFactor, long now) {
            long time = getDuration(opType + (memFactor * 4), now);
            if (curState == memFactor) {
                return time + (now - startTime);
            }
            return time;
        }

        public String toString() {
            return "ServiceState{" + Integer.toHexString(System.identityHashCode(this)) + " " + this.mName + " pkg=" + this.mPackage + " proc=" + Integer.toHexString(System.identityHashCode(this)) + "}";
        }
    }

    public static final class PackageState {
        public final String mPackageName;
        public final ArrayMap<String, ProcessState> mProcesses = new ArrayMap<>();
        public final ArrayMap<String, ServiceState> mServices = new ArrayMap<>();
        public final int mUid;

        public PackageState(String packageName, int uid) {
            this.mUid = uid;
            this.mPackageName = packageName;
        }
    }

    public static final class ProcessDataCollection {
        public long avgPss;
        public long avgUss;
        public long maxPss;
        public long maxUss;
        final int[] memStates;
        public long minPss;
        public long minUss;
        public long numPss;
        final int[] procStates;
        final int[] screenStates;
        public long totalTime;

        public ProcessDataCollection(int[] _screenStates, int[] _memStates, int[] _procStates) {
            this.screenStates = _screenStates;
            this.memStates = _memStates;
            this.procStates = _procStates;
        }

        /* access modifiers changed from: package-private */
        public void print(PrintWriter pw, long overallTime, boolean full) {
            if (this.totalTime > overallTime) {
                pw.print(PhoneConstants.APN_TYPE_ALL);
            }
            ProcessStats.printPercent(pw, ((double) this.totalTime) / ((double) overallTime));
            if (this.numPss > 0) {
                pw.print(" (");
                ProcessStats.printSizeValue(pw, this.minPss * 1024);
                pw.print(NativeLibraryHelper.CLEAR_ABI_OVERRIDE);
                ProcessStats.printSizeValue(pw, this.avgPss * 1024);
                pw.print(NativeLibraryHelper.CLEAR_ABI_OVERRIDE);
                ProcessStats.printSizeValue(pw, this.maxPss * 1024);
                pw.print("/");
                ProcessStats.printSizeValue(pw, this.minUss * 1024);
                pw.print(NativeLibraryHelper.CLEAR_ABI_OVERRIDE);
                ProcessStats.printSizeValue(pw, this.avgUss * 1024);
                pw.print(NativeLibraryHelper.CLEAR_ABI_OVERRIDE);
                ProcessStats.printSizeValue(pw, this.maxUss * 1024);
                if (full) {
                    pw.print(" over ");
                    pw.print(this.numPss);
                }
                pw.print(")");
            }
        }
    }

    public static class TotalMemoryUseCollection {
        final int[] memStates;
        public long[] processStatePss = new long[14];
        public int[] processStateSamples = new int[14];
        public long[] processStateTime = new long[14];
        public double[] processStateWeight = new double[14];
        final int[] screenStates;
        public double sysMemCachedWeight;
        public double sysMemFreeWeight;
        public double sysMemKernelWeight;
        public double sysMemNativeWeight;
        public int sysMemSamples;
        public long[] sysMemUsage = new long[16];
        public double sysMemZRamWeight;
        public long totalTime;

        public TotalMemoryUseCollection(int[] _screenStates, int[] _memStates) {
            this.screenStates = _screenStates;
            this.memStates = _memStates;
        }
    }
}

package com.android.internal.os;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.os.BatteryStats;
import android.os.Bundle;
import android.os.MemoryFile;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.app.IBatteryStats;
import com.android.internal.os.BatterySipper;
import com.android.internal.telephony.PhoneConstants;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class BatteryStatsHelper {
    private static final boolean DEBUG = false;
    private static final String TAG = BatteryStatsHelper.class.getSimpleName();
    private static Intent sBatteryBroadcastXfer;
    private static ArrayMap<File, BatteryStats> sFileXfer = new ArrayMap<>();
    private static BatteryStats sStatsXfer;
    private long mAppMobileActive;
    private long mAppWifiRunning;
    private Intent mBatteryBroadcast;
    private IBatteryStats mBatteryInfo;
    long mBatteryRealtime;
    long mBatteryTimeRemaining;
    long mBatteryUptime;
    private double mBluetoothPower;
    private final List<BatterySipper> mBluetoothSippers;
    long mChargeTimeRemaining;
    private final boolean mCollectBatteryBroadcast;
    private double mComputedPower;
    private final Context mContext;
    private double mMaxDrainedPower;
    private double mMaxPower;
    private double mMaxRealPower;
    private double mMinDrainedPower;
    private final List<BatterySipper> mMobilemsppList;
    private PowerProfile mPowerProfile;
    long mRawRealtime;
    long mRawUptime;
    private BatteryStats mStats;
    private long mStatsPeriod;
    private int mStatsType;
    private double mTotalPower;
    long mTypeBatteryRealtime;
    long mTypeBatteryUptime;
    private final List<BatterySipper> mUsageList;
    private final SparseArray<Double> mUserPower;
    private final SparseArray<List<BatterySipper>> mUserSippers;
    private final boolean mWifiOnly;
    private double mWifiPower;
    private final List<BatterySipper> mWifiSippers;

    public BatteryStatsHelper(Context context) {
        this(context, true);
    }

    public BatteryStatsHelper(Context context, boolean collectBatteryBroadcast) {
        this.mUsageList = new ArrayList();
        this.mWifiSippers = new ArrayList();
        this.mBluetoothSippers = new ArrayList();
        this.mUserSippers = new SparseArray<>();
        this.mUserPower = new SparseArray<>();
        this.mMobilemsppList = new ArrayList();
        this.mStatsType = 0;
        this.mStatsPeriod = 0;
        this.mMaxPower = 1.0d;
        this.mMaxRealPower = 1.0d;
        this.mContext = context;
        this.mCollectBatteryBroadcast = collectBatteryBroadcast;
        this.mWifiOnly = checkWifiOnly(context);
    }

    public BatteryStatsHelper(Context context, boolean collectBatteryBroadcast, boolean wifiOnly) {
        this.mUsageList = new ArrayList();
        this.mWifiSippers = new ArrayList();
        this.mBluetoothSippers = new ArrayList();
        this.mUserSippers = new SparseArray<>();
        this.mUserPower = new SparseArray<>();
        this.mMobilemsppList = new ArrayList();
        this.mStatsType = 0;
        this.mStatsPeriod = 0;
        this.mMaxPower = 1.0d;
        this.mMaxRealPower = 1.0d;
        this.mContext = context;
        this.mCollectBatteryBroadcast = collectBatteryBroadcast;
        this.mWifiOnly = wifiOnly;
    }

    public static boolean checkWifiOnly(Context context) {
        if (!((ConnectivityManager) context.getSystemService("connectivity")).isNetworkSupported(0)) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x0040 A[SYNTHETIC, Splitter:B:21:0x0040] */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0049 A[SYNTHETIC, Splitter:B:26:0x0049] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void storeStatsHistoryInFile(java.lang.String r10) {
        /*
            r9 = this;
            android.util.ArrayMap<java.io.File, android.os.BatteryStats> r7 = com.android.internal.os.BatteryStatsHelper.sFileXfer
            monitor-enter(r7)
            android.content.Context r6 = r9.mContext     // Catch:{ all -> 0x004d }
            java.io.File r5 = makeFilePath(r6, r10)     // Catch:{ all -> 0x004d }
            android.util.ArrayMap<java.io.File, android.os.BatteryStats> r6 = com.android.internal.os.BatteryStatsHelper.sFileXfer     // Catch:{ all -> 0x004d }
            android.os.BatteryStats r8 = r9.getStats()     // Catch:{ all -> 0x004d }
            r6.put(r5, r8)     // Catch:{ all -> 0x004d }
            r1 = 0
            java.io.FileOutputStream r2 = new java.io.FileOutputStream     // Catch:{ IOException -> 0x0036 }
            r2.<init>(r5)     // Catch:{ IOException -> 0x0036 }
            android.os.Parcel r3 = android.os.Parcel.obtain()     // Catch:{ IOException -> 0x0055, all -> 0x0052 }
            android.os.BatteryStats r6 = r9.getStats()     // Catch:{ IOException -> 0x0055, all -> 0x0052 }
            r8 = 0
            r6.writeToParcelWithoutUids(r3, r8)     // Catch:{ IOException -> 0x0055, all -> 0x0052 }
            byte[] r4 = r3.marshall()     // Catch:{ IOException -> 0x0055, all -> 0x0052 }
            r2.write(r4)     // Catch:{ IOException -> 0x0055, all -> 0x0052 }
            if (r2 == 0) goto L_0x0058
            r2.close()     // Catch:{ IOException -> 0x0033 }
            r1 = r2
        L_0x0031:
            monitor-exit(r7)
            return
        L_0x0033:
            r6 = move-exception
            r1 = r2
            goto L_0x0031
        L_0x0036:
            r0 = move-exception
        L_0x0037:
            java.lang.String r6 = com.android.internal.os.BatteryStatsHelper.TAG     // Catch:{ all -> 0x0046 }
            java.lang.String r8 = "Unable to write history to file"
            android.util.Log.w(r6, r8, r0)     // Catch:{ all -> 0x0046 }
            if (r1 == 0) goto L_0x0031
            r1.close()     // Catch:{ IOException -> 0x0044 }
            goto L_0x0031
        L_0x0044:
            r6 = move-exception
            goto L_0x0031
        L_0x0046:
            r6 = move-exception
        L_0x0047:
            if (r1 == 0) goto L_0x004c
            r1.close()     // Catch:{ IOException -> 0x0050 }
        L_0x004c:
            throw r6
        L_0x004d:
            r6 = move-exception
            monitor-exit(r7)
            throw r6
        L_0x0050:
            r8 = move-exception
            goto L_0x004c
        L_0x0052:
            r6 = move-exception
            r1 = r2
            goto L_0x0047
        L_0x0055:
            r0 = move-exception
            r1 = r2
            goto L_0x0037
        L_0x0058:
            r1 = r2
            goto L_0x0031
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsHelper.storeStatsHistoryInFile(java.lang.String):void");
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x0044 A[SYNTHETIC, Splitter:B:20:0x0044] */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x005a A[SYNTHETIC, Splitter:B:26:0x005a] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static android.os.BatteryStats statsFromFile(android.content.Context r10, java.lang.String r11) {
        /*
        // Method dump skipped, instructions count: 109
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.BatteryStatsHelper.statsFromFile(android.content.Context, java.lang.String):android.os.BatteryStats");
    }

    public static void dropFile(Context context, String fname) {
        makeFilePath(context, fname).delete();
    }

    private static File makeFilePath(Context context, String fname) {
        return new File(context.getFilesDir(), fname);
    }

    public void clearStats() {
        this.mStats = null;
    }

    public BatteryStats getStats() {
        if (this.mStats == null) {
            load();
        }
        return this.mStats;
    }

    public Intent getBatteryBroadcast() {
        if (this.mBatteryBroadcast == null && this.mCollectBatteryBroadcast) {
            load();
        }
        return this.mBatteryBroadcast;
    }

    public PowerProfile getPowerProfile() {
        return this.mPowerProfile;
    }

    public void create(BatteryStats stats) {
        this.mPowerProfile = new PowerProfile(this.mContext);
        this.mStats = stats;
    }

    public void create(Bundle icicle) {
        if (icicle != null) {
            this.mStats = sStatsXfer;
            this.mBatteryBroadcast = sBatteryBroadcastXfer;
        }
        this.mBatteryInfo = IBatteryStats.Stub.asInterface(ServiceManager.getService("batterystats"));
        this.mPowerProfile = new PowerProfile(this.mContext);
    }

    public void storeState() {
        sStatsXfer = this.mStats;
        sBatteryBroadcastXfer = this.mBatteryBroadcast;
    }

    public static String makemAh(double power) {
        if (power < 1.0E-5d) {
            return String.format("%.8f", Double.valueOf(power));
        } else if (power < 1.0E-4d) {
            return String.format("%.7f", Double.valueOf(power));
        } else if (power < 0.001d) {
            return String.format("%.6f", Double.valueOf(power));
        } else if (power < 0.01d) {
            return String.format("%.5f", Double.valueOf(power));
        } else if (power < 0.1d) {
            return String.format("%.4f", Double.valueOf(power));
        } else if (power < 1.0d) {
            return String.format("%.3f", Double.valueOf(power));
        } else if (power < 10.0d) {
            return String.format("%.2f", Double.valueOf(power));
        } else if (power < 100.0d) {
            return String.format("%.1f", Double.valueOf(power));
        } else {
            return String.format("%.0f", Double.valueOf(power));
        }
    }

    public void refreshStats(int statsType, int asUser) {
        SparseArray<UserHandle> users = new SparseArray<>(1);
        users.put(asUser, new UserHandle(asUser));
        refreshStats(statsType, users);
    }

    public void refreshStats(int statsType, List<UserHandle> asUsers) {
        int n = asUsers.size();
        SparseArray<UserHandle> users = new SparseArray<>(n);
        for (int i = 0; i < n; i++) {
            UserHandle userHandle = asUsers.get(i);
            users.put(userHandle.getIdentifier(), userHandle);
        }
        refreshStats(statsType, users);
    }

    public void refreshStats(int statsType, SparseArray<UserHandle> asUsers) {
        refreshStats(statsType, asUsers, SystemClock.elapsedRealtime() * 1000, SystemClock.uptimeMillis() * 1000);
    }

    public void refreshStats(int statsType, SparseArray<UserHandle> asUsers, long rawRealtimeUs, long rawUptimeUs) {
        getStats();
        this.mMaxPower = 0.0d;
        this.mMaxRealPower = 0.0d;
        this.mComputedPower = 0.0d;
        this.mTotalPower = 0.0d;
        this.mWifiPower = 0.0d;
        this.mBluetoothPower = 0.0d;
        this.mAppMobileActive = 0;
        this.mAppWifiRunning = 0;
        this.mUsageList.clear();
        this.mWifiSippers.clear();
        this.mBluetoothSippers.clear();
        this.mUserSippers.clear();
        this.mUserPower.clear();
        this.mMobilemsppList.clear();
        if (this.mStats != null) {
            this.mStatsType = statsType;
            this.mRawUptime = rawUptimeUs;
            this.mRawRealtime = rawRealtimeUs;
            this.mBatteryUptime = this.mStats.getBatteryUptime(rawUptimeUs);
            this.mBatteryRealtime = this.mStats.getBatteryRealtime(rawRealtimeUs);
            this.mTypeBatteryUptime = this.mStats.computeBatteryUptime(rawUptimeUs, this.mStatsType);
            this.mTypeBatteryRealtime = this.mStats.computeBatteryRealtime(rawRealtimeUs, this.mStatsType);
            this.mBatteryTimeRemaining = this.mStats.computeBatteryTimeRemaining(rawRealtimeUs);
            this.mChargeTimeRemaining = this.mStats.computeChargeTimeRemaining(rawRealtimeUs);
            this.mMinDrainedPower = (((double) this.mStats.getLowDischargeAmountSinceCharge()) * this.mPowerProfile.getBatteryCapacity()) / 100.0d;
            this.mMaxDrainedPower = (((double) this.mStats.getHighDischargeAmountSinceCharge()) * this.mPowerProfile.getBatteryCapacity()) / 100.0d;
            processAppUsage(asUsers);
            for (int i = 0; i < this.mUsageList.size(); i++) {
                BatterySipper bs = this.mUsageList.get(i);
                bs.computeMobilemspp();
                if (bs.mobilemspp != 0.0d) {
                    this.mMobilemsppList.add(bs);
                }
            }
            for (int i2 = 0; i2 < this.mUserSippers.size(); i2++) {
                List<BatterySipper> user = this.mUserSippers.valueAt(i2);
                for (int j = 0; j < user.size(); j++) {
                    BatterySipper bs2 = user.get(j);
                    bs2.computeMobilemspp();
                    if (bs2.mobilemspp != 0.0d) {
                        this.mMobilemsppList.add(bs2);
                    }
                }
            }
            Collections.sort(this.mMobilemsppList, new Comparator<BatterySipper>() {
                /* class com.android.internal.os.BatteryStatsHelper.AnonymousClass1 */

                public int compare(BatterySipper lhs, BatterySipper rhs) {
                    if (lhs.mobilemspp < rhs.mobilemspp) {
                        return 1;
                    }
                    if (lhs.mobilemspp > rhs.mobilemspp) {
                        return -1;
                    }
                    return 0;
                }
            });
            processMiscUsage();
            this.mTotalPower = this.mComputedPower;
            if (this.mStats.getLowDischargeAmountSinceCharge() > 1) {
                if (this.mMinDrainedPower > this.mComputedPower) {
                    double amount = this.mMinDrainedPower - this.mComputedPower;
                    this.mTotalPower = this.mMinDrainedPower;
                    addEntryNoTotal(BatterySipper.DrainType.UNACCOUNTED, 0, amount);
                } else if (this.mMaxDrainedPower < this.mComputedPower) {
                    addEntryNoTotal(BatterySipper.DrainType.OVERCOUNTED, 0, this.mComputedPower - this.mMaxDrainedPower);
                }
            }
            Collections.sort(this.mUsageList);
        }
    }

    private void processAppUsage(SparseArray<UserHandle> asUsers) {
        double p;
        Double userPower;
        boolean forAllUsers = asUsers.get(-1) != null;
        SensorManager sensorManager = (SensorManager) this.mContext.getSystemService("sensor");
        int which = this.mStatsType;
        int speedSteps = this.mPowerProfile.getNumSpeedSteps();
        double[] powerCpuNormal = new double[speedSteps];
        long[] cpuSpeedStepTimes = new long[speedSteps];
        for (int p2 = 0; p2 < speedSteps; p2++) {
            powerCpuNormal[p2] = this.mPowerProfile.getAveragePower(PowerProfile.POWER_CPU_ACTIVE, p2);
        }
        double mobilePowerPerPacket = getMobilePowerPerPacket();
        double mobilePowerPerMs = getMobilePowerPerMs();
        double wifiPowerPerPacket = getWifiPowerPerPacket();
        long appWakelockTimeUs = 0;
        BatterySipper osApp = null;
        this.mStatsPeriod = this.mTypeBatteryRealtime;
        SparseArray<? extends BatteryStats.Uid> uidStats = this.mStats.getUidStats();
        int NU = uidStats.size();
        for (int iu = 0; iu < NU; iu++) {
            BatteryStats.Uid u = (BatteryStats.Uid) uidStats.valueAt(iu);
            double power = 0.0d;
            double highestDrain = 0.0d;
            String packageWithHighestDrain = null;
            Map<String, ? extends BatteryStats.Uid.Proc> processStats = u.getProcessStats();
            long cpuTime = 0;
            long cpuFgTime = 0;
            long wakelockTime = 0;
            long gpsTime = 0;
            if (processStats.size() > 0) {
                for (Map.Entry<String, ? extends BatteryStats.Uid.Proc> ent : processStats.entrySet()) {
                    BatteryStats.Uid.Proc ps = (BatteryStats.Uid.Proc) ent.getValue();
                    long userTime = ps.getUserTime(which);
                    long systemTime = ps.getSystemTime(which);
                    cpuFgTime += 10 * ps.getForegroundTime(which);
                    long tmpCpuTime = (userTime + systemTime) * 10;
                    int totalTimeAtSpeeds = 0;
                    for (int step = 0; step < speedSteps; step++) {
                        cpuSpeedStepTimes[step] = ps.getTimeAtCpuSpeedStep(step, which);
                        totalTimeAtSpeeds = (int) (((long) totalTimeAtSpeeds) + cpuSpeedStepTimes[step]);
                    }
                    if (totalTimeAtSpeeds == 0) {
                        totalTimeAtSpeeds = 1;
                    }
                    double processPower = 0.0d;
                    for (int step2 = 0; step2 < speedSteps; step2++) {
                        processPower += ((double) tmpCpuTime) * (((double) cpuSpeedStepTimes[step2]) / ((double) totalTimeAtSpeeds)) * powerCpuNormal[step2];
                    }
                    cpuTime += tmpCpuTime;
                    power += processPower;
                    if (packageWithHighestDrain == null || packageWithHighestDrain.startsWith(PhoneConstants.APN_TYPE_ALL)) {
                        highestDrain = processPower;
                        packageWithHighestDrain = ent.getKey();
                    } else if (highestDrain < processPower && !ent.getKey().startsWith(PhoneConstants.APN_TYPE_ALL)) {
                        highestDrain = processPower;
                        packageWithHighestDrain = ent.getKey();
                    }
                }
            }
            if (cpuFgTime > cpuTime) {
                cpuTime = cpuFgTime;
            }
            double power2 = power / 3600000.0d;
            for (Map.Entry<String, ? extends BatteryStats.Uid.Wakelock> wakelockEntry : u.getWakelockStats().entrySet()) {
                BatteryStats.Timer timer = ((BatteryStats.Uid.Wakelock) wakelockEntry.getValue()).getWakeTime(0);
                if (timer != null) {
                    wakelockTime += timer.getTotalTimeLocked(this.mRawRealtime, which);
                }
            }
            appWakelockTimeUs += wakelockTime;
            long wakelockTime2 = wakelockTime / 1000;
            double power3 = power2 + ((((double) wakelockTime2) * this.mPowerProfile.getAveragePower(PowerProfile.POWER_CPU_AWAKE)) / 3600000.0d);
            long mobileRx = u.getNetworkActivityPackets(0, this.mStatsType);
            long mobileTx = u.getNetworkActivityPackets(1, this.mStatsType);
            long mobileRxB = u.getNetworkActivityBytes(0, this.mStatsType);
            long mobileTxB = u.getNetworkActivityBytes(1, this.mStatsType);
            long mobileActive = u.getMobileRadioActiveTime(this.mStatsType);
            if (mobileActive > 0) {
                this.mAppMobileActive += mobileActive;
                p = (((double) mobileActive) * mobilePowerPerMs) / 1000.0d;
            } else {
                p = ((double) (mobileRx + mobileTx)) * mobilePowerPerPacket;
            }
            long wifiRx = u.getNetworkActivityPackets(2, this.mStatsType);
            long wifiTx = u.getNetworkActivityPackets(3, this.mStatsType);
            long wifiRxB = u.getNetworkActivityBytes(2, this.mStatsType);
            long wifiTxB = u.getNetworkActivityBytes(3, this.mStatsType);
            long wifiRunningTimeMs = u.getWifiRunningTime(this.mRawRealtime, which) / 1000;
            this.mAppWifiRunning += wifiRunningTimeMs;
            double power4 = power3 + p + (((double) (wifiRx + wifiTx)) * wifiPowerPerPacket) + ((((double) wifiRunningTimeMs) * this.mPowerProfile.getAveragePower(PowerProfile.POWER_WIFI_ON)) / 3600000.0d) + ((((double) (u.getWifiScanTime(this.mRawRealtime, which) / 1000)) * this.mPowerProfile.getAveragePower(PowerProfile.POWER_WIFI_SCAN)) / 3600000.0d);
            for (int bin = 0; bin < 5; bin++) {
                power4 += (((double) (u.getWifiBatchedScanTime(bin, this.mRawRealtime, which) / 1000)) * this.mPowerProfile.getAveragePower(PowerProfile.POWER_WIFI_BATCHED_SCAN, bin)) / 3600000.0d;
            }
            SparseArray<? extends BatteryStats.Uid.Sensor> sensorStats = u.getSensorStats();
            int NSE = sensorStats.size();
            for (int ise = 0; ise < NSE; ise++) {
                int sensorHandle = sensorStats.keyAt(ise);
                long sensorTime = ((BatteryStats.Uid.Sensor) sensorStats.valueAt(ise)).getSensorTime().getTotalTimeLocked(this.mRawRealtime, which) / 1000;
                double multiplier = 0.0d;
                switch (sensorHandle) {
                    case -10000:
                        multiplier = this.mPowerProfile.getAveragePower(PowerProfile.POWER_GPS_ON);
                        gpsTime = sensorTime;
                        break;
                    default:
                        Iterator i$ = sensorManager.getSensorList(-1).iterator();
                        while (true) {
                            if (i$.hasNext()) {
                                Sensor s = i$.next();
                                if (s.getHandle() == sensorHandle) {
                                    multiplier = (double) s.getPower();
                                    break;
                                }
                            } else {
                                break;
                            }
                        }
                }
                power4 += (((double) sensorTime) * multiplier) / 3600000.0d;
            }
            int userId = UserHandle.getUserId(u.getUid());
            if (power4 != 0.0d || u.getUid() == 0) {
                BatterySipper app = new BatterySipper(BatterySipper.DrainType.APP, u, new double[]{power4});
                app.cpuTime = cpuTime;
                app.gpsTime = gpsTime;
                app.wifiRunningTime = wifiRunningTimeMs;
                app.cpuFgTime = cpuFgTime;
                app.wakeLockTime = wakelockTime2;
                app.mobileRxPackets = mobileRx;
                app.mobileTxPackets = mobileTx;
                app.mobileActive = mobileActive / 1000;
                app.mobileActiveCount = u.getMobileRadioActiveCount(this.mStatsType);
                app.wifiRxPackets = wifiRx;
                app.wifiTxPackets = wifiTx;
                app.mobileRxBytes = mobileRxB;
                app.mobileTxBytes = mobileTxB;
                app.wifiRxBytes = wifiRxB;
                app.wifiTxBytes = wifiTxB;
                app.packageWithHighestDrain = packageWithHighestDrain;
                if (u.getUid() == 1010) {
                    this.mWifiSippers.add(app);
                    this.mWifiPower += power4;
                } else if (u.getUid() == 1002) {
                    this.mBluetoothSippers.add(app);
                    this.mBluetoothPower += power4;
                } else if (forAllUsers || asUsers.get(userId) != null || UserHandle.getAppId(u.getUid()) < 10000) {
                    this.mUsageList.add(app);
                    if (power4 > this.mMaxPower) {
                        this.mMaxPower = power4;
                    }
                    if (power4 > this.mMaxRealPower) {
                        this.mMaxRealPower = power4;
                    }
                    this.mComputedPower += power4;
                } else {
                    List<BatterySipper> list = this.mUserSippers.get(userId);
                    if (list == null) {
                        list = new ArrayList<>();
                        this.mUserSippers.put(userId, list);
                    }
                    list.add(app);
                    if (power4 != 0.0d) {
                        Double userPower2 = this.mUserPower.get(userId);
                        if (userPower2 == null) {
                            userPower = Double.valueOf(power4);
                        } else {
                            userPower = Double.valueOf(userPower2.doubleValue() + power4);
                        }
                        this.mUserPower.put(userId, userPower);
                    }
                }
                if (u.getUid() == 0) {
                    osApp = app;
                }
            }
        }
        if (osApp != null) {
            long wakeTimeMillis = (this.mBatteryUptime / 1000) - ((appWakelockTimeUs / 1000) + (this.mStats.getScreenOnTime(this.mRawRealtime, which) / 1000));
            if (wakeTimeMillis > 0) {
                double power5 = (((double) wakeTimeMillis) * this.mPowerProfile.getAveragePower(PowerProfile.POWER_CPU_AWAKE)) / 3600000.0d;
                osApp.wakeLockTime += wakeTimeMillis;
                osApp.value += power5;
                double[] dArr = osApp.values;
                dArr[0] = dArr[0] + power5;
                if (osApp.value > this.mMaxPower) {
                    this.mMaxPower = osApp.value;
                }
                if (osApp.value > this.mMaxRealPower) {
                    this.mMaxRealPower = osApp.value;
                }
                this.mComputedPower += power5;
            }
        }
    }

    private void addPhoneUsage() {
        long phoneOnTimeMs = this.mStats.getPhoneOnTime(this.mRawRealtime, this.mStatsType) / 1000;
        double phoneOnPower = (this.mPowerProfile.getAveragePower(PowerProfile.POWER_RADIO_ACTIVE) * ((double) phoneOnTimeMs)) / 3600000.0d;
        if (phoneOnPower != 0.0d) {
            addEntry(BatterySipper.DrainType.PHONE, phoneOnTimeMs, phoneOnPower);
        }
    }

    private void addScreenUsage() {
        long screenOnTimeMs = this.mStats.getScreenOnTime(this.mRawRealtime, this.mStatsType) / 1000;
        double power = 0.0d + (((double) screenOnTimeMs) * this.mPowerProfile.getAveragePower(PowerProfile.POWER_SCREEN_ON));
        double screenFullPower = this.mPowerProfile.getAveragePower(PowerProfile.POWER_SCREEN_FULL);
        for (int i = 0; i < 5; i++) {
            power += ((((double) (((float) i) + 0.5f)) * screenFullPower) / 5.0d) * ((double) (this.mStats.getScreenBrightnessTime(i, this.mRawRealtime, this.mStatsType) / 1000));
        }
        double power2 = power / 3600000.0d;
        if (power2 != 0.0d) {
            addEntry(BatterySipper.DrainType.SCREEN, screenOnTimeMs, power2);
        }
    }

    private void addRadioUsage() {
        double power = 0.0d;
        long signalTimeMs = 0;
        long noCoverageTimeMs = 0;
        for (int i = 0; i < 5; i++) {
            long strengthTimeMs = this.mStats.getPhoneSignalStrengthTime(i, this.mRawRealtime, this.mStatsType) / 1000;
            power += (((double) strengthTimeMs) * this.mPowerProfile.getAveragePower(PowerProfile.POWER_RADIO_ON, i)) / 3600000.0d;
            signalTimeMs += strengthTimeMs;
            if (i == 0) {
                noCoverageTimeMs = strengthTimeMs;
            }
        }
        double power2 = power + ((((double) (this.mStats.getPhoneSignalScanningTime(this.mRawRealtime, this.mStatsType) / 1000)) * this.mPowerProfile.getAveragePower(PowerProfile.POWER_RADIO_SCANNING)) / 3600000.0d);
        long remainingActiveTime = (this.mStats.getMobileRadioActiveTime(this.mRawRealtime, this.mStatsType) - this.mAppMobileActive) / 1000;
        if (remainingActiveTime > 0) {
            power2 += getMobilePowerPerMs() * ((double) remainingActiveTime);
        }
        if (power2 != 0.0d) {
            BatterySipper bs = addEntry(BatterySipper.DrainType.CELL, signalTimeMs, power2);
            if (signalTimeMs != 0) {
                bs.noCoveragePercent = (((double) noCoverageTimeMs) * 100.0d) / ((double) signalTimeMs);
            }
            bs.mobileActive = remainingActiveTime;
            bs.mobileActiveCount = this.mStats.getMobileRadioActiveUnknownCount(this.mStatsType);
        }
    }

    private void aggregateSippers(BatterySipper bs, List<BatterySipper> from, String tag) {
        for (int i = 0; i < from.size(); i++) {
            BatterySipper wbs = from.get(i);
            bs.cpuTime += wbs.cpuTime;
            bs.gpsTime += wbs.gpsTime;
            bs.wifiRunningTime += wbs.wifiRunningTime;
            bs.cpuFgTime += wbs.cpuFgTime;
            bs.wakeLockTime += wbs.wakeLockTime;
            bs.mobileRxPackets += wbs.mobileRxPackets;
            bs.mobileTxPackets += wbs.mobileTxPackets;
            bs.mobileActive += wbs.mobileActive;
            bs.mobileActiveCount += wbs.mobileActiveCount;
            bs.wifiRxPackets += wbs.wifiRxPackets;
            bs.wifiTxPackets += wbs.wifiTxPackets;
            bs.mobileRxBytes += wbs.mobileRxBytes;
            bs.mobileTxBytes += wbs.mobileTxBytes;
            bs.wifiRxBytes += wbs.wifiRxBytes;
            bs.wifiTxBytes += wbs.wifiTxBytes;
        }
        bs.computeMobilemspp();
    }

    private void addWiFiUsage() {
        long onTimeMs = this.mStats.getWifiOnTime(this.mRawRealtime, this.mStatsType) / 1000;
        long runningTimeMs = (this.mStats.getGlobalWifiRunningTime(this.mRawRealtime, this.mStatsType) / 1000) - this.mAppWifiRunning;
        if (runningTimeMs < 0) {
            runningTimeMs = 0;
        }
        double wifiPower = ((((double) (0 * onTimeMs)) * this.mPowerProfile.getAveragePower(PowerProfile.POWER_WIFI_ON)) + (((double) runningTimeMs) * this.mPowerProfile.getAveragePower(PowerProfile.POWER_WIFI_ON))) / 3600000.0d;
        if (this.mWifiPower + wifiPower != 0.0d) {
            aggregateSippers(addEntry(BatterySipper.DrainType.WIFI, runningTimeMs, this.mWifiPower + wifiPower), this.mWifiSippers, "WIFI");
        }
    }

    private void addIdleUsage() {
        long idleTimeMs = (this.mTypeBatteryRealtime - this.mStats.getScreenOnTime(this.mRawRealtime, this.mStatsType)) / 1000;
        double idlePower = (((double) idleTimeMs) * this.mPowerProfile.getAveragePower(PowerProfile.POWER_CPU_IDLE)) / 3600000.0d;
        if (idlePower != 0.0d) {
            addEntry(BatterySipper.DrainType.IDLE, idleTimeMs, idlePower);
        }
    }

    private void addBluetoothUsage() {
        long btOnTimeMs = this.mStats.getBluetoothOnTime(this.mRawRealtime, this.mStatsType) / 1000;
        double btPower = ((((double) btOnTimeMs) * this.mPowerProfile.getAveragePower(PowerProfile.POWER_BLUETOOTH_ON)) / 3600000.0d) + ((((double) this.mStats.getBluetoothPingCount()) * this.mPowerProfile.getAveragePower(PowerProfile.POWER_BLUETOOTH_AT_CMD)) / 3600000.0d);
        if (this.mBluetoothPower + btPower != 0.0d) {
            aggregateSippers(addEntry(BatterySipper.DrainType.BLUETOOTH, btOnTimeMs, this.mBluetoothPower + btPower), this.mBluetoothSippers, "Bluetooth");
        }
    }

    private void addFlashlightUsage() {
        long flashlightOnTimeMs = this.mStats.getFlashlightOnTime(this.mRawRealtime, this.mStatsType) / 1000;
        double flashlightPower = (((double) flashlightOnTimeMs) * this.mPowerProfile.getAveragePower(PowerProfile.POWER_FLASHLIGHT)) / 3600000.0d;
        if (flashlightPower != 0.0d) {
            addEntry(BatterySipper.DrainType.FLASHLIGHT, flashlightOnTimeMs, flashlightPower);
        }
    }

    private void addUserUsage() {
        for (int i = 0; i < this.mUserSippers.size(); i++) {
            int userId = this.mUserSippers.keyAt(i);
            List<BatterySipper> sippers = this.mUserSippers.valueAt(i);
            Double userPower = this.mUserPower.get(userId);
            BatterySipper bs = addEntry(BatterySipper.DrainType.USER, 0, userPower != null ? userPower.doubleValue() : 0.0d);
            bs.userId = userId;
            aggregateSippers(bs, sippers, "User");
        }
    }

    private double getMobilePowerPerPacket() {
        double MOBILE_POWER = this.mPowerProfile.getAveragePower(PowerProfile.POWER_RADIO_ACTIVE) / 3600.0d;
        long mobileData = this.mStats.getNetworkActivityPackets(0, this.mStatsType) + this.mStats.getNetworkActivityPackets(1, this.mStatsType);
        long radioDataUptimeMs = this.mStats.getMobileRadioActiveTime(this.mRawRealtime, this.mStatsType) / 1000;
        return (MOBILE_POWER / ((mobileData == 0 || radioDataUptimeMs == 0) ? 12.20703125d : ((double) mobileData) / ((double) radioDataUptimeMs))) / 3600.0d;
    }

    private double getMobilePowerPerMs() {
        return this.mPowerProfile.getAveragePower(PowerProfile.POWER_RADIO_ACTIVE) / 3600000.0d;
    }

    private double getWifiPowerPerPacket() {
        return ((this.mPowerProfile.getAveragePower(PowerProfile.POWER_WIFI_ACTIVE) / 3600.0d) / 61.03515625d) / 3600.0d;
    }

    private void processMiscUsage() {
        addUserUsage();
        addPhoneUsage();
        addScreenUsage();
        addFlashlightUsage();
        addWiFiUsage();
        addBluetoothUsage();
        addIdleUsage();
        if (!this.mWifiOnly) {
            addRadioUsage();
        }
    }

    private BatterySipper addEntry(BatterySipper.DrainType drainType, long time, double power) {
        this.mComputedPower += power;
        if (power > this.mMaxRealPower) {
            this.mMaxRealPower = power;
        }
        return addEntryNoTotal(drainType, time, power);
    }

    private BatterySipper addEntryNoTotal(BatterySipper.DrainType drainType, long time, double power) {
        if (power > this.mMaxPower) {
            this.mMaxPower = power;
        }
        BatterySipper bs = new BatterySipper(drainType, null, new double[]{power});
        bs.usageTime = time;
        this.mUsageList.add(bs);
        return bs;
    }

    public List<BatterySipper> getUsageList() {
        return this.mUsageList;
    }

    public List<BatterySipper> getMobilemsppList() {
        return this.mMobilemsppList;
    }

    public long getStatsPeriod() {
        return this.mStatsPeriod;
    }

    public int getStatsType() {
        return this.mStatsType;
    }

    public double getMaxPower() {
        return this.mMaxPower;
    }

    public double getMaxRealPower() {
        return this.mMaxRealPower;
    }

    public double getTotalPower() {
        return this.mTotalPower;
    }

    public double getComputedPower() {
        return this.mComputedPower;
    }

    public double getMinDrainedPower() {
        return this.mMinDrainedPower;
    }

    public double getMaxDrainedPower() {
        return this.mMaxDrainedPower;
    }

    public long getBatteryTimeRemaining() {
        return this.mBatteryTimeRemaining;
    }

    public long getChargeTimeRemaining() {
        return this.mChargeTimeRemaining;
    }

    public static byte[] readFully(FileInputStream stream) throws IOException {
        return readFully(stream, stream.available());
    }

    public static byte[] readFully(FileInputStream stream, int avail) throws IOException {
        int pos = 0;
        byte[] data = new byte[avail];
        while (true) {
            int amt = stream.read(data, pos, data.length - pos);
            if (amt <= 0) {
                return data;
            }
            pos += amt;
            int avail2 = stream.available();
            if (avail2 > data.length - pos) {
                byte[] newData = new byte[(pos + avail2)];
                System.arraycopy(data, 0, newData, 0, pos);
                data = newData;
            }
        }
    }

    private void load() {
        if (this.mBatteryInfo != null) {
            this.mStats = getStats(this.mBatteryInfo);
            if (this.mCollectBatteryBroadcast) {
                this.mBatteryBroadcast = this.mContext.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
            }
        }
    }

    private static BatteryStatsImpl getStats(IBatteryStats service) {
        try {
            ParcelFileDescriptor pfd = service.getStatisticsStream();
            if (pfd != null) {
                try {
                    byte[] data = readFully(new ParcelFileDescriptor.AutoCloseInputStream(pfd), MemoryFile.getSize(pfd.getFileDescriptor()));
                    Parcel parcel = Parcel.obtain();
                    parcel.unmarshall(data, 0, data.length);
                    parcel.setDataPosition(0);
                    BatteryStatsImpl stats = BatteryStatsImpl.CREATOR.createFromParcel(parcel);
                    stats.distributeWorkLocked(0);
                    return stats;
                } catch (IOException e) {
                    Log.w(TAG, "Unable to read statistics stream", e);
                }
            }
        } catch (RemoteException e2) {
            Log.w(TAG, "RemoteException:", e2);
        }
        return new BatteryStatsImpl();
    }
}

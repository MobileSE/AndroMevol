package android.os;

import android.app.Notification;
import android.app.backup.FullBackup;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.location.LocationManager;
import android.net.ProxyInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;
import android.nfc.cardemulation.CardEmulation;
import android.provider.Settings;
import android.telephony.SignalStrength;
import android.text.format.DateFormat;
import android.util.Printer;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.TimeUtils;
import android.view.SurfaceControl;
import com.android.internal.content.NativeLibraryHelper;
import com.android.internal.os.BatterySipper;
import com.android.internal.os.BatteryStatsHelper;
import com.android.internal.telephony.PhoneConstants;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BatteryStats implements Parcelable {
    private static final String APK_DATA = "apk";
    public static final int AUDIO_TURNED_ON = 7;
    private static final String BATTERY_DATA = "bt";
    private static final String BATTERY_DISCHARGE_DATA = "dc";
    private static final String BATTERY_LEVEL_DATA = "lv";
    private static final int BATTERY_STATS_CHECKIN_VERSION = 9;
    private static final String BLUETOOTH_STATE_COUNT_DATA = "bsc";
    public static final int BLUETOOTH_STATE_HIGH = 3;
    public static final int BLUETOOTH_STATE_INACTIVE = 0;
    public static final int BLUETOOTH_STATE_LOW = 1;
    public static final int BLUETOOTH_STATE_MEDIUM = 2;
    static final String[] BLUETOOTH_STATE_NAMES = {"inactive", "low", "med", "high"};
    private static final String BLUETOOTH_STATE_TIME_DATA = "bst";
    private static final long BYTES_PER_GB = 1073741824;
    private static final long BYTES_PER_KB = 1024;
    private static final long BYTES_PER_MB = 1048576;
    private static final String CHARGE_STEP_DATA = "csd";
    private static final String CHARGE_TIME_REMAIN_DATA = "ctr";
    public static final int DATA_CONNECTION_1xRTT = 7;
    public static final int DATA_CONNECTION_CDMA = 4;
    private static final String DATA_CONNECTION_COUNT_DATA = "dcc";
    public static final int DATA_CONNECTION_EDGE = 2;
    public static final int DATA_CONNECTION_EHRPD = 14;
    public static final int DATA_CONNECTION_EVDO_0 = 5;
    public static final int DATA_CONNECTION_EVDO_A = 6;
    public static final int DATA_CONNECTION_EVDO_B = 12;
    public static final int DATA_CONNECTION_GPRS = 1;
    public static final int DATA_CONNECTION_HSDPA = 8;
    public static final int DATA_CONNECTION_HSPA = 10;
    public static final int DATA_CONNECTION_HSPAP = 15;
    public static final int DATA_CONNECTION_HSUPA = 9;
    public static final int DATA_CONNECTION_IDEN = 11;
    public static final int DATA_CONNECTION_LTE = 13;
    static final String[] DATA_CONNECTION_NAMES = {"none", "gprs", "edge", "umts", "cdma", "evdo_0", "evdo_A", "1xrtt", "hsdpa", "hsupa", "hspa", "iden", "evdo_b", "lte", "ehrpd", "hspap", CardEmulation.CATEGORY_OTHER};
    public static final int DATA_CONNECTION_NONE = 0;
    public static final int DATA_CONNECTION_OTHER = 16;
    private static final String DATA_CONNECTION_TIME_DATA = "dct";
    public static final int DATA_CONNECTION_UMTS = 3;
    private static final String DISCHARGE_STEP_DATA = "dsd";
    private static final String DISCHARGE_TIME_REMAIN_DATA = "dtr";
    public static final int DUMP_CHARGED_ONLY = 2;
    public static final int DUMP_DEVICE_WIFI_ONLY = 32;
    public static final int DUMP_HISTORY_ONLY = 4;
    public static final int DUMP_INCLUDE_HISTORY = 8;
    public static final int DUMP_UNPLUGGED_ONLY = 1;
    public static final int DUMP_VERBOSE = 16;
    public static final int FOREGROUND_ACTIVITY = 10;
    private static final String FOREGROUND_DATA = "fg";
    public static final int FULL_WIFI_LOCK = 5;
    private static final String GLOBAL_NETWORK_DATA = "gn";
    private static final String HISTORY_DATA = "h";
    public static final String[] HISTORY_EVENT_CHECKIN_NAMES = {"Enl", "Epr", "Efg", "Etp", "Esy", "Ewl", "Ejb", "Eur", "Euf"};
    public static final String[] HISTORY_EVENT_NAMES = {"null", "proc", FOREGROUND_DATA, "top", "sync", "wake_lock_in", "job", "user", "userfg"};
    public static final BitDescription[] HISTORY_STATE2_DESCRIPTIONS = {new BitDescription(Integer.MIN_VALUE, Settings.Global.LOW_POWER_MODE, "lp"), new BitDescription(1073741824, "video", "v"), new BitDescription(536870912, "wifi_running", "Wr"), new BitDescription(268435456, "wifi", "W"), new BitDescription(134217728, "flashlight", "fl"), new BitDescription(112, 4, "wifi_signal_strength", "Wss", new String[]{WifiEnterpriseConfig.ENGINE_DISABLE, WifiEnterpriseConfig.ENGINE_ENABLE, "2", "3", "4"}, new String[]{WifiEnterpriseConfig.ENGINE_DISABLE, WifiEnterpriseConfig.ENGINE_ENABLE, "2", "3", "4"}), new BitDescription(15, 0, "wifi_suppl", "Wsp", WIFI_SUPPL_STATE_NAMES, WIFI_SUPPL_STATE_SHORT_NAMES)};
    public static final BitDescription[] HISTORY_STATE_DESCRIPTIONS = {new BitDescription(Integer.MIN_VALUE, "running", FullBackup.ROOT_TREE_TOKEN), new BitDescription(1073741824, "wake_lock", "w"), new BitDescription(8388608, Context.SENSOR_SERVICE, "s"), new BitDescription(536870912, LocationManager.GPS_PROVIDER, "g"), new BitDescription(268435456, "wifi_full_lock", "Wl"), new BitDescription(134217728, "wifi_scan", "Ws"), new BitDescription(67108864, "wifi_multicast", "Wm"), new BitDescription(33554432, "mobile_radio", "Pr"), new BitDescription(2097152, "phone_scanning", "Psc"), new BitDescription(4194304, Context.AUDIO_SERVICE, FullBackup.APK_TREE_TOKEN), new BitDescription(1048576, "screen", "S"), new BitDescription(524288, BatteryManager.EXTRA_PLUGGED, "BP"), new BitDescription(262144, "phone_in_call", "Pcl"), new BitDescription(65536, "bluetooth", "b"), new BitDescription(HistoryItem.STATE_DATA_CONNECTION_MASK, 9, "data_conn", "Pcn", DATA_CONNECTION_NAMES, DATA_CONNECTION_NAMES), new BitDescription(448, 6, "phone_state", "Pst", new String[]{"in", "out", PhoneConstants.APN_TYPE_EMERGENCY, "off"}, new String[]{"in", "out", "em", "off"}), new BitDescription(56, 3, "phone_signal_strength", "Pss", SignalStrength.SIGNAL_STRENGTH_NAMES, new String[]{WifiEnterpriseConfig.ENGINE_DISABLE, WifiEnterpriseConfig.ENGINE_ENABLE, "2", "3", "4"}), new BitDescription(7, 0, "brightness", "Sb", SCREEN_BRIGHTNESS_NAMES, SCREEN_BRIGHTNESS_SHORT_NAMES)};
    private static final String HISTORY_STRING_POOL = "hsp";
    public static final int JOB = 14;
    private static final String JOB_DATA = "jb";
    private static final String KERNEL_WAKELOCK_DATA = "kwl";
    private static final boolean LOCAL_LOGV = false;
    private static final String MISC_DATA = "m";
    private static final String NETWORK_DATA = "nt";
    public static final int NETWORK_MOBILE_RX_DATA = 0;
    public static final int NETWORK_MOBILE_TX_DATA = 1;
    public static final int NETWORK_WIFI_RX_DATA = 2;
    public static final int NETWORK_WIFI_TX_DATA = 3;
    public static final int NUM_BLUETOOTH_STATES = 4;
    public static final int NUM_DATA_CONNECTION_TYPES = 17;
    public static final int NUM_NETWORK_ACTIVITY_TYPES = 4;
    public static final int NUM_SCREEN_BRIGHTNESS_BINS = 5;
    public static final int NUM_WIFI_SIGNAL_STRENGTH_BINS = 5;
    public static final int NUM_WIFI_STATES = 8;
    public static final int NUM_WIFI_SUPPL_STATES = 13;
    private static final String POWER_USE_ITEM_DATA = "pwi";
    private static final String POWER_USE_SUMMARY_DATA = "pws";
    private static final String PROCESS_DATA = "pr";
    public static final int PROCESS_STATE = 12;
    public static final int SCREEN_BRIGHTNESS_BRIGHT = 4;
    public static final int SCREEN_BRIGHTNESS_DARK = 0;
    private static final String SCREEN_BRIGHTNESS_DATA = "br";
    public static final int SCREEN_BRIGHTNESS_DIM = 1;
    public static final int SCREEN_BRIGHTNESS_LIGHT = 3;
    public static final int SCREEN_BRIGHTNESS_MEDIUM = 2;
    static final String[] SCREEN_BRIGHTNESS_NAMES = {"dark", "dim", "medium", "light", "bright"};
    static final String[] SCREEN_BRIGHTNESS_SHORT_NAMES = {WifiEnterpriseConfig.ENGINE_DISABLE, WifiEnterpriseConfig.ENGINE_ENABLE, "2", "3", "4"};
    public static final int SENSOR = 3;
    private static final String SENSOR_DATA = "sr";
    public static final String SERVICE_NAME = "batterystats";
    private static final String SIGNAL_SCANNING_TIME_DATA = "sst";
    private static final String SIGNAL_STRENGTH_COUNT_DATA = "sgc";
    private static final String SIGNAL_STRENGTH_TIME_DATA = "sgt";
    private static final String STATE_TIME_DATA = "st";
    public static final int STATS_CURRENT = 1;
    public static final int STATS_SINCE_CHARGED = 0;
    public static final int STATS_SINCE_UNPLUGGED = 2;
    private static final String[] STAT_NAMES = {"l", FullBackup.CACHE_TREE_TOKEN, "u"};
    public static final long STEP_LEVEL_INITIAL_MODE_MASK = 71776119061217280L;
    public static final long STEP_LEVEL_INITIAL_MODE_SHIFT = 48;
    public static final long STEP_LEVEL_LEVEL_MASK = 280375465082880L;
    public static final long STEP_LEVEL_LEVEL_SHIFT = 40;
    public static final int STEP_LEVEL_MODE_POWER_SAVE = 4;
    public static final int STEP_LEVEL_MODE_SCREEN_STATE = 3;
    public static final long STEP_LEVEL_MODIFIED_MODE_MASK = -72057594037927936L;
    public static final long STEP_LEVEL_MODIFIED_MODE_SHIFT = 56;
    public static final long STEP_LEVEL_TIME_MASK = 1099511627775L;
    public static final int SYNC = 13;
    private static final String SYNC_DATA = "sy";
    private static final String UID_DATA = "uid";
    private static final String USER_ACTIVITY_DATA = "ua";
    private static final String VERSION_DATA = "vers";
    private static final String VIBRATOR_DATA = "vib";
    public static final int VIBRATOR_ON = 9;
    public static final int VIDEO_TURNED_ON = 8;
    private static final String WAKELOCK_DATA = "wl";
    private static final String WAKEUP_REASON_DATA = "wr";
    public static final int WAKE_TYPE_FULL = 1;
    public static final int WAKE_TYPE_PARTIAL = 0;
    public static final int WAKE_TYPE_WINDOW = 2;
    public static final int WIFI_BATCHED_SCAN = 11;
    private static final String WIFI_DATA = "wfl";
    public static final int WIFI_MULTICAST_ENABLED = 7;
    public static final int WIFI_RUNNING = 4;
    public static final int WIFI_SCAN = 6;
    private static final String WIFI_SIGNAL_STRENGTH_COUNT_DATA = "wsgc";
    private static final String WIFI_SIGNAL_STRENGTH_TIME_DATA = "wsgt";
    private static final String WIFI_STATE_COUNT_DATA = "wsc";
    static final String[] WIFI_STATE_NAMES = {"off", "scanning", "no_net", "disconn", "sta", "p2p", "sta_p2p", "soft_ap"};
    public static final int WIFI_STATE_OFF = 0;
    public static final int WIFI_STATE_OFF_SCANNING = 1;
    public static final int WIFI_STATE_ON_CONNECTED_P2P = 5;
    public static final int WIFI_STATE_ON_CONNECTED_STA = 4;
    public static final int WIFI_STATE_ON_CONNECTED_STA_P2P = 6;
    public static final int WIFI_STATE_ON_DISCONNECTED = 3;
    public static final int WIFI_STATE_ON_NO_NETWORKS = 2;
    public static final int WIFI_STATE_SOFT_AP = 7;
    private static final String WIFI_STATE_TIME_DATA = "wst";
    public static final int WIFI_SUPPL_STATE_ASSOCIATED = 7;
    public static final int WIFI_SUPPL_STATE_ASSOCIATING = 6;
    public static final int WIFI_SUPPL_STATE_AUTHENTICATING = 5;
    public static final int WIFI_SUPPL_STATE_COMPLETED = 10;
    private static final String WIFI_SUPPL_STATE_COUNT_DATA = "wssc";
    public static final int WIFI_SUPPL_STATE_DISCONNECTED = 1;
    public static final int WIFI_SUPPL_STATE_DORMANT = 11;
    public static final int WIFI_SUPPL_STATE_FOUR_WAY_HANDSHAKE = 8;
    public static final int WIFI_SUPPL_STATE_GROUP_HANDSHAKE = 9;
    public static final int WIFI_SUPPL_STATE_INACTIVE = 3;
    public static final int WIFI_SUPPL_STATE_INTERFACE_DISABLED = 2;
    public static final int WIFI_SUPPL_STATE_INVALID = 0;
    static final String[] WIFI_SUPPL_STATE_NAMES = {"invalid", "disconn", "disabled", "inactive", "scanning", "authenticating", "associating", "associated", "4-way-handshake", "group-handshake", "completed", "dormant", "uninit"};
    public static final int WIFI_SUPPL_STATE_SCANNING = 4;
    static final String[] WIFI_SUPPL_STATE_SHORT_NAMES = {"inv", "dsc", "dis", "inact", "scan", "auth", "ascing", "asced", "4-way", WifiConfiguration.GroupCipher.varName, "compl", "dorm", "uninit"};
    private static final String WIFI_SUPPL_STATE_TIME_DATA = "wsst";
    public static final int WIFI_SUPPL_STATE_UNINITIALIZED = 12;
    private final StringBuilder mFormatBuilder = new StringBuilder(32);
    private final Formatter mFormatter = new Formatter(this.mFormatBuilder);

    public static abstract class Counter {
        public abstract int getCountLocked(int i);

        public abstract void logState(Printer printer, String str);
    }

    public static abstract class LongCounter {
        public abstract long getCountLocked(int i);

        public abstract void logState(Printer printer, String str);
    }

    public static abstract class Timer {
        public abstract int getCountLocked(int i);

        public abstract long getTotalTimeLocked(long j, int i);

        public abstract void logState(Printer printer, String str);
    }

    public abstract void commitCurrentHistoryBatchLocked();

    public abstract long computeBatteryRealtime(long j, int i);

    public abstract long computeBatteryScreenOffRealtime(long j, int i);

    public abstract long computeBatteryScreenOffUptime(long j, int i);

    public abstract long computeBatteryTimeRemaining(long j);

    public abstract long computeBatteryUptime(long j, int i);

    public abstract long computeChargeTimeRemaining(long j);

    public abstract long computeRealtime(long j, int i);

    public abstract long computeUptime(long j, int i);

    public abstract void finishIteratingHistoryLocked();

    public abstract void finishIteratingOldHistoryLocked();

    public abstract long getBatteryRealtime(long j);

    public abstract long getBatteryUptime(long j);

    public abstract long getBluetoothOnTime(long j, int i);

    public abstract int getBluetoothPingCount();

    public abstract int getBluetoothStateCount(int i, int i2);

    public abstract long getBluetoothStateTime(int i, long j, int i2);

    public abstract long[] getChargeStepDurationsArray();

    public abstract int getCpuSpeedSteps();

    public abstract int getDischargeAmount(int i);

    public abstract int getDischargeAmountScreenOff();

    public abstract int getDischargeAmountScreenOffSinceCharge();

    public abstract int getDischargeAmountScreenOn();

    public abstract int getDischargeAmountScreenOnSinceCharge();

    public abstract int getDischargeCurrentLevel();

    public abstract int getDischargeStartLevel();

    public abstract long[] getDischargeStepDurationsArray();

    public abstract String getEndPlatformVersion();

    public abstract long getFlashlightOnCount(int i);

    public abstract long getFlashlightOnTime(long j, int i);

    public abstract long getGlobalWifiRunningTime(long j, int i);

    public abstract int getHighDischargeAmountSinceCharge();

    public abstract long getHistoryBaseTime();

    public abstract int getHistoryStringPoolBytes();

    public abstract int getHistoryStringPoolSize();

    public abstract String getHistoryTagPoolString(int i);

    public abstract int getHistoryTagPoolUid(int i);

    public abstract int getHistoryTotalSize();

    public abstract int getHistoryUsedSize();

    public abstract long getInteractiveTime(long j, int i);

    public abstract boolean getIsOnBattery();

    public abstract Map<String, ? extends Timer> getKernelWakelockStats();

    public abstract int getLowDischargeAmountSinceCharge();

    public abstract int getLowPowerModeEnabledCount(int i);

    public abstract long getLowPowerModeEnabledTime(long j, int i);

    public abstract long getMobileRadioActiveAdjustedTime(int i);

    public abstract int getMobileRadioActiveCount(int i);

    public abstract long getMobileRadioActiveTime(long j, int i);

    public abstract int getMobileRadioActiveUnknownCount(int i);

    public abstract long getMobileRadioActiveUnknownTime(int i);

    public abstract long getNetworkActivityBytes(int i, int i2);

    public abstract long getNetworkActivityPackets(int i, int i2);

    public abstract boolean getNextHistoryLocked(HistoryItem historyItem);

    public abstract boolean getNextOldHistoryLocked(HistoryItem historyItem);

    public abstract int getNumChargeStepDurations();

    public abstract int getNumDischargeStepDurations();

    public abstract int getParcelVersion();

    public abstract int getPhoneDataConnectionCount(int i, int i2);

    public abstract long getPhoneDataConnectionTime(int i, long j, int i2);

    public abstract int getPhoneOnCount(int i);

    public abstract long getPhoneOnTime(long j, int i);

    public abstract long getPhoneSignalScanningTime(long j, int i);

    public abstract int getPhoneSignalStrengthCount(int i, int i2);

    public abstract long getPhoneSignalStrengthTime(int i, long j, int i2);

    public abstract long getScreenBrightnessTime(int i, long j, int i2);

    public abstract int getScreenOnCount(int i);

    public abstract long getScreenOnTime(long j, int i);

    public abstract long getStartClockTime();

    public abstract int getStartCount();

    public abstract String getStartPlatformVersion();

    public abstract SparseArray<? extends Uid> getUidStats();

    public abstract Map<String, ? extends Timer> getWakeupReasonStats();

    public abstract long getWifiOnTime(long j, int i);

    public abstract int getWifiSignalStrengthCount(int i, int i2);

    public abstract long getWifiSignalStrengthTime(int i, long j, int i2);

    public abstract int getWifiStateCount(int i, int i2);

    public abstract long getWifiStateTime(int i, long j, int i2);

    public abstract int getWifiSupplStateCount(int i, int i2);

    public abstract long getWifiSupplStateTime(int i, long j, int i2);

    public abstract boolean startIteratingHistoryLocked();

    public abstract boolean startIteratingOldHistoryLocked();

    public abstract void writeToParcelWithoutUids(Parcel parcel, int i);

    public static abstract class Uid {
        public static final int NUM_PROCESS_STATE = 3;
        public static final int NUM_USER_ACTIVITY_TYPES = 3;
        public static final int NUM_WIFI_BATCHED_SCAN_BINS = 5;
        public static final int PROCESS_STATE_ACTIVE = 1;
        public static final int PROCESS_STATE_FOREGROUND = 0;
        static final String[] PROCESS_STATE_NAMES = {"Foreground", "Active", "Running"};
        public static final int PROCESS_STATE_RUNNING = 2;
        static final String[] USER_ACTIVITY_TYPES = {CardEmulation.CATEGORY_OTHER, "button", "touch"};

        public static abstract class Proc {

            public static class ExcessivePower {
                public static final int TYPE_CPU = 2;
                public static final int TYPE_WAKE = 1;
                public long overTime;
                public int type;
                public long usedTime;
            }

            public abstract int countExcessivePowers();

            public abstract ExcessivePower getExcessivePower(int i);

            public abstract long getForegroundTime(int i);

            public abstract int getStarts(int i);

            public abstract long getSystemTime(int i);

            public abstract long getTimeAtCpuSpeedStep(int i, int i2);

            public abstract long getUserTime(int i);

            public abstract boolean isActive();
        }

        public static abstract class Sensor {
            public static final int GPS = -10000;

            public abstract int getHandle();

            public abstract Timer getSensorTime();
        }

        public static abstract class Wakelock {
            public abstract Timer getWakeTime(int i);
        }

        public abstract long getAudioTurnedOnTime(long j, int i);

        public abstract Timer getForegroundActivityTimer();

        public abstract long getFullWifiLockTime(long j, int i);

        public abstract Map<String, ? extends Timer> getJobStats();

        public abstract int getMobileRadioActiveCount(int i);

        public abstract long getMobileRadioActiveTime(int i);

        public abstract long getNetworkActivityBytes(int i, int i2);

        public abstract long getNetworkActivityPackets(int i, int i2);

        public abstract Map<String, ? extends Pkg> getPackageStats();

        public abstract SparseArray<? extends Pid> getPidStats();

        public abstract long getProcessStateTime(int i, long j, int i2);

        public abstract Map<String, ? extends Proc> getProcessStats();

        public abstract SparseArray<? extends Sensor> getSensorStats();

        public abstract Map<String, ? extends Timer> getSyncStats();

        public abstract int getUid();

        public abstract int getUserActivityCount(int i, int i2);

        public abstract Timer getVibratorOnTimer();

        public abstract long getVideoTurnedOnTime(long j, int i);

        public abstract Map<String, ? extends Wakelock> getWakelockStats();

        public abstract long getWifiBatchedScanTime(int i, long j, int i2);

        public abstract long getWifiMulticastTime(long j, int i);

        public abstract long getWifiRunningTime(long j, int i);

        public abstract long getWifiScanTime(long j, int i);

        public abstract boolean hasNetworkActivity();

        public abstract boolean hasUserActivity();

        public abstract void noteActivityPausedLocked(long j);

        public abstract void noteActivityResumedLocked(long j);

        public abstract void noteFullWifiLockAcquiredLocked(long j);

        public abstract void noteFullWifiLockReleasedLocked(long j);

        public abstract void noteUserActivityLocked(int i);

        public abstract void noteWifiBatchedScanStartedLocked(int i, long j);

        public abstract void noteWifiBatchedScanStoppedLocked(long j);

        public abstract void noteWifiMulticastDisabledLocked(long j);

        public abstract void noteWifiMulticastEnabledLocked(long j);

        public abstract void noteWifiRunningLocked(long j);

        public abstract void noteWifiScanStartedLocked(long j);

        public abstract void noteWifiScanStoppedLocked(long j);

        public abstract void noteWifiStoppedLocked(long j);

        public class Pid {
            public int mWakeNesting;
            public long mWakeStartMs;
            public long mWakeSumMs;

            public Pid() {
            }
        }

        public static abstract class Pkg {
            public abstract Map<String, ? extends Serv> getServiceStats();

            public abstract int getWakeups(int i);

            public abstract class Serv {
                public abstract int getLaunches(int i);

                public abstract long getStartTime(long j, int i);

                public abstract int getStarts(int i);

                public Serv() {
                }
            }
        }
    }

    public static final class HistoryTag {
        public int poolIdx;
        public String string;
        public int uid;

        public void setTo(HistoryTag o) {
            this.string = o.string;
            this.uid = o.uid;
            this.poolIdx = o.poolIdx;
        }

        public void setTo(String _string, int _uid) {
            this.string = _string;
            this.uid = _uid;
            this.poolIdx = -1;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.string);
            dest.writeInt(this.uid);
        }

        public void readFromParcel(Parcel src) {
            this.string = src.readString();
            this.uid = src.readInt();
            this.poolIdx = -1;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            HistoryTag that = (HistoryTag) o;
            if (this.uid != that.uid) {
                return false;
            }
            return this.string.equals(that.string);
        }

        public int hashCode() {
            return (this.string.hashCode() * 31) + this.uid;
        }
    }

    public static final class HistoryItem implements Parcelable {
        public static final byte CMD_CURRENT_TIME = 5;
        public static final byte CMD_NULL = -1;
        public static final byte CMD_OVERFLOW = 6;
        public static final byte CMD_RESET = 7;
        public static final byte CMD_START = 4;
        public static final byte CMD_UPDATE = 0;
        public static final int EVENT_COUNT = 9;
        public static final int EVENT_FLAG_FINISH = 16384;
        public static final int EVENT_FLAG_START = 32768;
        public static final int EVENT_FOREGROUND = 2;
        public static final int EVENT_FOREGROUND_FINISH = 16386;
        public static final int EVENT_FOREGROUND_START = 32770;
        public static final int EVENT_JOB = 6;
        public static final int EVENT_JOB_FINISH = 16390;
        public static final int EVENT_JOB_START = 32774;
        public static final int EVENT_NONE = 0;
        public static final int EVENT_PROC = 1;
        public static final int EVENT_PROC_FINISH = 16385;
        public static final int EVENT_PROC_START = 32769;
        public static final int EVENT_SYNC = 4;
        public static final int EVENT_SYNC_FINISH = 16388;
        public static final int EVENT_SYNC_START = 32772;
        public static final int EVENT_TOP = 3;
        public static final int EVENT_TOP_FINISH = 16387;
        public static final int EVENT_TOP_START = 32771;
        public static final int EVENT_TYPE_MASK = -49153;
        public static final int EVENT_USER_FOREGROUND = 8;
        public static final int EVENT_USER_FOREGROUND_FINISH = 16392;
        public static final int EVENT_USER_FOREGROUND_START = 32776;
        public static final int EVENT_USER_RUNNING = 7;
        public static final int EVENT_USER_RUNNING_FINISH = 16391;
        public static final int EVENT_USER_RUNNING_START = 32775;
        public static final int EVENT_WAKE_LOCK = 5;
        public static final int EVENT_WAKE_LOCK_FINISH = 16389;
        public static final int EVENT_WAKE_LOCK_START = 32773;
        public static final int MOST_INTERESTING_STATES = 1900544;
        public static final int MOST_INTERESTING_STATES2 = -1879048192;
        public static final int STATE2_FLASHLIGHT_FLAG = 134217728;
        public static final int STATE2_LOW_POWER_FLAG = Integer.MIN_VALUE;
        public static final int STATE2_VIDEO_ON_FLAG = 1073741824;
        public static final int STATE2_WIFI_ON_FLAG = 268435456;
        public static final int STATE2_WIFI_RUNNING_FLAG = 536870912;
        public static final int STATE2_WIFI_SIGNAL_STRENGTH_MASK = 112;
        public static final int STATE2_WIFI_SIGNAL_STRENGTH_SHIFT = 4;
        public static final int STATE2_WIFI_SUPPL_STATE_MASK = 15;
        public static final int STATE2_WIFI_SUPPL_STATE_SHIFT = 0;
        public static final int STATE_AUDIO_ON_FLAG = 4194304;
        public static final int STATE_BATTERY_PLUGGED_FLAG = 524288;
        public static final int STATE_BLUETOOTH_ON_FLAG = 65536;
        public static final int STATE_BRIGHTNESS_MASK = 7;
        public static final int STATE_BRIGHTNESS_SHIFT = 0;
        public static final int STATE_CPU_RUNNING_FLAG = Integer.MIN_VALUE;
        public static final int STATE_DATA_CONNECTION_MASK = 15872;
        public static final int STATE_DATA_CONNECTION_SHIFT = 9;
        public static final int STATE_GPS_ON_FLAG = 536870912;
        public static final int STATE_MOBILE_RADIO_ACTIVE_FLAG = 33554432;
        public static final int STATE_PHONE_IN_CALL_FLAG = 262144;
        public static final int STATE_PHONE_SCANNING_FLAG = 2097152;
        public static final int STATE_PHONE_SIGNAL_STRENGTH_MASK = 56;
        public static final int STATE_PHONE_SIGNAL_STRENGTH_SHIFT = 3;
        public static final int STATE_PHONE_STATE_MASK = 448;
        public static final int STATE_PHONE_STATE_SHIFT = 6;
        public static final int STATE_SCREEN_ON_FLAG = 1048576;
        public static final int STATE_SENSOR_ON_FLAG = 8388608;
        public static final int STATE_WAKE_LOCK_FLAG = 1073741824;
        public static final int STATE_WIFI_FULL_LOCK_FLAG = 268435456;
        public static final int STATE_WIFI_MULTICAST_ON_FLAG = 67108864;
        public static final int STATE_WIFI_SCAN_FLAG = 134217728;
        public byte batteryHealth;
        public byte batteryLevel;
        public byte batteryPlugType;
        public byte batteryStatus;
        public short batteryTemperature;
        public char batteryVoltage;
        public byte cmd = -1;
        public long currentTime;
        public int eventCode;
        public HistoryTag eventTag;
        public final HistoryTag localEventTag = new HistoryTag();
        public final HistoryTag localWakeReasonTag = new HistoryTag();
        public final HistoryTag localWakelockTag = new HistoryTag();
        public HistoryItem next;
        public int numReadInts;
        public int states;
        public int states2;
        public long time;
        public HistoryTag wakeReasonTag;
        public HistoryTag wakelockTag;

        public boolean isDeltaData() {
            return this.cmd == 0;
        }

        public HistoryItem() {
        }

        public HistoryItem(long time2, Parcel src) {
            this.time = time2;
            this.numReadInts = 2;
            readFromParcel(src);
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            int i;
            int i2 = 0;
            dest.writeLong(this.time);
            int i3 = ((this.batteryPlugType << 24) & 251658240) | (this.cmd & CMD_NULL) | ((this.batteryLevel << 8) & 65280) | ((this.batteryStatus << 16) & SurfaceControl.FX_SURFACE_MASK) | ((this.batteryHealth << 20) & 15728640);
            if (this.wakelockTag != null) {
                i = 268435456;
            } else {
                i = 0;
            }
            int i4 = (this.wakeReasonTag != null ? 536870912 : 0) | i3 | i;
            if (this.eventCode != 0) {
                i2 = 1073741824;
            }
            dest.writeInt(i4 | i2);
            dest.writeInt((this.batteryTemperature & 65535) | ((this.batteryVoltage << 16) & -65536));
            dest.writeInt(this.states);
            dest.writeInt(this.states2);
            if (this.wakelockTag != null) {
                this.wakelockTag.writeToParcel(dest, flags);
            }
            if (this.wakeReasonTag != null) {
                this.wakeReasonTag.writeToParcel(dest, flags);
            }
            if (this.eventCode != 0) {
                dest.writeInt(this.eventCode);
                this.eventTag.writeToParcel(dest, flags);
            }
            if (this.cmd == 5 || this.cmd == 7) {
                dest.writeLong(this.currentTime);
            }
        }

        public void readFromParcel(Parcel src) {
            int start = src.dataPosition();
            int bat = src.readInt();
            this.cmd = (byte) (bat & 255);
            this.batteryLevel = (byte) ((bat >> 8) & 255);
            this.batteryStatus = (byte) ((bat >> 16) & 15);
            this.batteryHealth = (byte) ((bat >> 20) & 15);
            this.batteryPlugType = (byte) ((bat >> 24) & 15);
            int bat2 = src.readInt();
            this.batteryTemperature = (short) (bat2 & 65535);
            this.batteryVoltage = (char) ((bat2 >> 16) & 65535);
            this.states = src.readInt();
            this.states2 = src.readInt();
            if ((268435456 & bat) != 0) {
                this.wakelockTag = this.localWakelockTag;
                this.wakelockTag.readFromParcel(src);
            } else {
                this.wakelockTag = null;
            }
            if ((536870912 & bat) != 0) {
                this.wakeReasonTag = this.localWakeReasonTag;
                this.wakeReasonTag.readFromParcel(src);
            } else {
                this.wakeReasonTag = null;
            }
            if ((1073741824 & bat) != 0) {
                this.eventCode = src.readInt();
                this.eventTag = this.localEventTag;
                this.eventTag.readFromParcel(src);
            } else {
                this.eventCode = 0;
                this.eventTag = null;
            }
            if (this.cmd == 5 || this.cmd == 7) {
                this.currentTime = src.readLong();
            } else {
                this.currentTime = 0;
            }
            this.numReadInts += (src.dataPosition() - start) / 4;
        }

        public void clear() {
            this.time = 0;
            this.cmd = -1;
            this.batteryLevel = 0;
            this.batteryStatus = 0;
            this.batteryHealth = 0;
            this.batteryPlugType = 0;
            this.batteryTemperature = 0;
            this.batteryVoltage = 0;
            this.states = 0;
            this.states2 = 0;
            this.wakelockTag = null;
            this.wakeReasonTag = null;
            this.eventCode = 0;
            this.eventTag = null;
        }

        public void setTo(HistoryItem o) {
            this.time = o.time;
            this.cmd = o.cmd;
            setToCommon(o);
        }

        public void setTo(long time2, byte cmd2, HistoryItem o) {
            this.time = time2;
            this.cmd = cmd2;
            setToCommon(o);
        }

        private void setToCommon(HistoryItem o) {
            this.batteryLevel = o.batteryLevel;
            this.batteryStatus = o.batteryStatus;
            this.batteryHealth = o.batteryHealth;
            this.batteryPlugType = o.batteryPlugType;
            this.batteryTemperature = o.batteryTemperature;
            this.batteryVoltage = o.batteryVoltage;
            this.states = o.states;
            this.states2 = o.states2;
            if (o.wakelockTag != null) {
                this.wakelockTag = this.localWakelockTag;
                this.wakelockTag.setTo(o.wakelockTag);
            } else {
                this.wakelockTag = null;
            }
            if (o.wakeReasonTag != null) {
                this.wakeReasonTag = this.localWakeReasonTag;
                this.wakeReasonTag.setTo(o.wakeReasonTag);
            } else {
                this.wakeReasonTag = null;
            }
            this.eventCode = o.eventCode;
            if (o.eventTag != null) {
                this.eventTag = this.localEventTag;
                this.eventTag.setTo(o.eventTag);
            } else {
                this.eventTag = null;
            }
            this.currentTime = o.currentTime;
        }

        public boolean sameNonEvent(HistoryItem o) {
            return this.batteryLevel == o.batteryLevel && this.batteryStatus == o.batteryStatus && this.batteryHealth == o.batteryHealth && this.batteryPlugType == o.batteryPlugType && this.batteryTemperature == o.batteryTemperature && this.batteryVoltage == o.batteryVoltage && this.states == o.states && this.states2 == o.states2 && this.currentTime == o.currentTime;
        }

        public boolean same(HistoryItem o) {
            if (!sameNonEvent(o) || this.eventCode != o.eventCode) {
                return false;
            }
            if (this.wakelockTag != o.wakelockTag && (this.wakelockTag == null || o.wakelockTag == null || !this.wakelockTag.equals(o.wakelockTag))) {
                return false;
            }
            if (this.wakeReasonTag != o.wakeReasonTag && (this.wakeReasonTag == null || o.wakeReasonTag == null || !this.wakeReasonTag.equals(o.wakeReasonTag))) {
                return false;
            }
            if (this.eventTag == o.eventTag || (this.eventTag != null && o.eventTag != null && this.eventTag.equals(o.eventTag))) {
                return true;
            }
            return false;
        }
    }

    public static final class HistoryEventTracker {
        private final HashMap<String, SparseIntArray>[] mActiveEvents = new HashMap[9];

        public boolean updateState(int code, String name, int uid, int poolIdx) {
            SparseIntArray uids;
            int idx;
            if ((32768 & code) != 0) {
                int idx2 = code & HistoryItem.EVENT_TYPE_MASK;
                HashMap<String, SparseIntArray> active = this.mActiveEvents[idx2];
                if (active == null) {
                    active = new HashMap<>();
                    this.mActiveEvents[idx2] = active;
                }
                SparseIntArray uids2 = active.get(name);
                if (uids2 == null) {
                    uids2 = new SparseIntArray();
                    active.put(name, uids2);
                }
                if (uids2.indexOfKey(uid) >= 0) {
                    return false;
                }
                uids2.put(uid, poolIdx);
            } else if ((code & 16384) != 0) {
                HashMap<String, SparseIntArray> active2 = this.mActiveEvents[code & HistoryItem.EVENT_TYPE_MASK];
                if (active2 == null || (uids = active2.get(name)) == null || (idx = uids.indexOfKey(uid)) < 0) {
                    return false;
                }
                uids.removeAt(idx);
                if (uids.size() <= 0) {
                    active2.remove(name);
                }
            }
            return true;
        }

        public void removeEvents(int code) {
            this.mActiveEvents[code & HistoryItem.EVENT_TYPE_MASK] = null;
        }

        public HashMap<String, SparseIntArray> getStateForEvent(int code) {
            return this.mActiveEvents[code];
        }
    }

    public static final class BitDescription {
        public final int mask;
        public final String name;
        public final int shift;
        public final String shortName;
        public final String[] shortValues;
        public final String[] values;

        public BitDescription(int mask2, String name2, String shortName2) {
            this.mask = mask2;
            this.shift = -1;
            this.name = name2;
            this.shortName = shortName2;
            this.values = null;
            this.shortValues = null;
        }

        public BitDescription(int mask2, int shift2, String name2, String shortName2, String[] values2, String[] shortValues2) {
            this.mask = mask2;
            this.shift = shift2;
            this.name = name2;
            this.shortName = shortName2;
            this.values = values2;
            this.shortValues = shortValues2;
        }
    }

    private static final void formatTimeRaw(StringBuilder out, long seconds) {
        long days = seconds / 86400;
        if (days != 0) {
            out.append(days);
            out.append("d ");
        }
        long used = 60 * days * 60 * 24;
        long hours = (seconds - used) / 3600;
        if (!(hours == 0 && used == 0)) {
            out.append(hours);
            out.append("h ");
        }
        long used2 = used + (60 * hours * 60);
        long mins = (seconds - used2) / 60;
        if (!(mins == 0 && used2 == 0)) {
            out.append(mins);
            out.append("m ");
        }
        long used3 = used2 + (60 * mins);
        if (seconds != 0 || used3 != 0) {
            out.append(seconds - used3);
            out.append("s ");
        }
    }

    public static final void formatTime(StringBuilder sb, long time) {
        long sec = time / 100;
        formatTimeRaw(sb, sec);
        sb.append((time - (100 * sec)) * 10);
        sb.append("ms ");
    }

    public static final void formatTimeMs(StringBuilder sb, long time) {
        long sec = time / 1000;
        formatTimeRaw(sb, sec);
        sb.append(time - (1000 * sec));
        sb.append("ms ");
    }

    public static final void formatTimeMsNoSpace(StringBuilder sb, long time) {
        long sec = time / 1000;
        formatTimeRaw(sb, sec);
        sb.append(time - (1000 * sec));
        sb.append("ms");
    }

    public final String formatRatioLocked(long num, long den) {
        if (den == 0) {
            return "--%";
        }
        this.mFormatBuilder.setLength(0);
        this.mFormatter.format("%.1f%%", Float.valueOf((((float) num) / ((float) den)) * 100.0f));
        return this.mFormatBuilder.toString();
    }

    /* access modifiers changed from: package-private */
    public final String formatBytesLocked(long bytes) {
        this.mFormatBuilder.setLength(0);
        if (bytes < 1024) {
            return bytes + "B";
        }
        if (bytes < 1048576) {
            this.mFormatter.format("%.2fKB", Double.valueOf(((double) bytes) / 1024.0d));
            return this.mFormatBuilder.toString();
        } else if (bytes < 1073741824) {
            this.mFormatter.format("%.2fMB", Double.valueOf(((double) bytes) / 1048576.0d));
            return this.mFormatBuilder.toString();
        } else {
            this.mFormatter.format("%.2fGB", Double.valueOf(((double) bytes) / 1.073741824E9d));
            return this.mFormatBuilder.toString();
        }
    }

    private static long computeWakeLock(Timer timer, long elapsedRealtimeUs, int which) {
        if (timer != null) {
            return (500 + timer.getTotalTimeLocked(elapsedRealtimeUs, which)) / 1000;
        }
        return 0;
    }

    private static final String printWakeLock(StringBuilder sb, Timer timer, long elapsedRealtimeUs, String name, int which, String linePrefix) {
        if (timer == null) {
            return linePrefix;
        }
        long totalTimeMillis = computeWakeLock(timer, elapsedRealtimeUs, which);
        int count = timer.getCountLocked(which);
        if (totalTimeMillis == 0) {
            return linePrefix;
        }
        sb.append(linePrefix);
        formatTimeMs(sb, totalTimeMillis);
        if (name != null) {
            sb.append(name);
            sb.append(' ');
        }
        sb.append('(');
        sb.append(count);
        sb.append(" times)");
        return ", ";
    }

    private static final String printWakeLockCheckin(StringBuilder sb, Timer timer, long elapsedRealtimeUs, String name, int which, String linePrefix) {
        long totalTimeMicros = 0;
        int count = 0;
        if (timer != null) {
            totalTimeMicros = timer.getTotalTimeLocked(elapsedRealtimeUs, which);
            count = timer.getCountLocked(which);
        }
        sb.append(linePrefix);
        sb.append((500 + totalTimeMicros) / 1000);
        sb.append(',');
        sb.append(name != null ? name + "," : ProxyInfo.LOCAL_EXCL_LIST);
        sb.append(count);
        return ",";
    }

    private static final void dumpLine(PrintWriter pw, int uid, String category, String type, Object... args) {
        pw.print(9);
        pw.print(',');
        pw.print(uid);
        pw.print(',');
        pw.print(category);
        pw.print(',');
        pw.print(type);
        for (Object arg : args) {
            pw.print(',');
            pw.print(arg);
        }
        pw.println();
    }

    public final void dumpCheckinLocked(Context context, PrintWriter pw, int which, int reqUid) {
        dumpCheckinLocked(context, pw, which, reqUid, BatteryStatsHelper.checkWifiOnly(context));
    }

    /*  JADX ERROR: IndexOutOfBoundsException in pass: SSATransform
        java.lang.IndexOutOfBoundsException: bitIndex < 0: -97
        	at java.util.BitSet.get(BitSet.java:623)
        	at jadx.core.dex.visitors.ssa.LiveVarAnalysis.fillBasicBlockInfo(LiveVarAnalysis.java:65)
        	at jadx.core.dex.visitors.ssa.LiveVarAnalysis.runAnalysis(LiveVarAnalysis.java:36)
        	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:50)
        	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:41)
        */
    public final void dumpCheckinLocked(android.content.Context r157, java.io.PrintWriter r158, int r159, int r160, boolean r161) {
        /*
        // Method dump skipped, instructions count: 3326
        */
        throw new UnsupportedOperationException("Method not decompiled: android.os.BatteryStats.dumpCheckinLocked(android.content.Context, java.io.PrintWriter, int, int, boolean):void");
    }

    /* access modifiers changed from: package-private */
    /* renamed from: android.os.BatteryStats$2  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$os$BatterySipper$DrainType = new int[BatterySipper.DrainType.values().length];

        static {
            try {
                $SwitchMap$com$android$internal$os$BatterySipper$DrainType[BatterySipper.DrainType.IDLE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$internal$os$BatterySipper$DrainType[BatterySipper.DrainType.CELL.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$internal$os$BatterySipper$DrainType[BatterySipper.DrainType.PHONE.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$android$internal$os$BatterySipper$DrainType[BatterySipper.DrainType.WIFI.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$android$internal$os$BatterySipper$DrainType[BatterySipper.DrainType.BLUETOOTH.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$android$internal$os$BatterySipper$DrainType[BatterySipper.DrainType.SCREEN.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$android$internal$os$BatterySipper$DrainType[BatterySipper.DrainType.FLASHLIGHT.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$android$internal$os$BatterySipper$DrainType[BatterySipper.DrainType.APP.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$android$internal$os$BatterySipper$DrainType[BatterySipper.DrainType.USER.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$com$android$internal$os$BatterySipper$DrainType[BatterySipper.DrainType.UNACCOUNTED.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$com$android$internal$os$BatterySipper$DrainType[BatterySipper.DrainType.OVERCOUNTED.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public static final class TimerEntry {
        final int mId;
        final String mName;
        final long mTime;
        final Timer mTimer;

        TimerEntry(String name, int id, Timer timer, long time) {
            this.mName = name;
            this.mId = id;
            this.mTimer = timer;
            this.mTime = time;
        }
    }

    private void printmAh(PrintWriter printer, double power) {
        printer.print(BatteryStatsHelper.makemAh(power));
    }

    public final void dumpLocked(Context context, PrintWriter pw, String prefix, int which, int reqUid) {
        dumpLocked(context, pw, prefix, which, reqUid, BatteryStatsHelper.checkWifiOnly(context));
    }

    /*  JADX ERROR: IndexOutOfBoundsException in pass: SSATransform
        java.lang.IndexOutOfBoundsException: bitIndex < 0: -58
        	at java.util.BitSet.get(BitSet.java:623)
        	at jadx.core.dex.visitors.ssa.LiveVarAnalysis.fillBasicBlockInfo(LiveVarAnalysis.java:65)
        	at jadx.core.dex.visitors.ssa.LiveVarAnalysis.runAnalysis(LiveVarAnalysis.java:36)
        	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:50)
        	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:41)
        */
    public final void dumpLocked(android.content.Context r194, java.io.PrintWriter r195, java.lang.String r196, int r197, int r198, boolean r199) {
        /*
        // Method dump skipped, instructions count: 7048
        */
        throw new UnsupportedOperationException("Method not decompiled: android.os.BatteryStats.dumpLocked(android.content.Context, java.io.PrintWriter, java.lang.String, int, int, boolean):void");
    }

    static void printBitDescriptions(PrintWriter pw, int oldval, int newval, HistoryTag wakelockTag, BitDescription[] descriptions, boolean longNames) {
        int diff = oldval ^ newval;
        if (diff != 0) {
            boolean didWake = false;
            for (BitDescription bd : descriptions) {
                if ((bd.mask & diff) != 0) {
                    pw.print(longNames ? " " : ",");
                    if (bd.shift < 0) {
                        pw.print((bd.mask & newval) != 0 ? "+" : NativeLibraryHelper.CLEAR_ABI_OVERRIDE);
                        pw.print(longNames ? bd.name : bd.shortName);
                        if (bd.mask == 1073741824 && wakelockTag != null) {
                            didWake = true;
                            pw.print("=");
                            if (longNames) {
                                UserHandle.formatUid(pw, wakelockTag.uid);
                                pw.print(":\"");
                                pw.print(wakelockTag.string);
                                pw.print("\"");
                            } else {
                                pw.print(wakelockTag.poolIdx);
                            }
                        }
                    } else {
                        pw.print(longNames ? bd.name : bd.shortName);
                        pw.print("=");
                        int val = (bd.mask & newval) >> bd.shift;
                        if (bd.values == null || val < 0 || val >= bd.values.length) {
                            pw.print(val);
                        } else {
                            pw.print(longNames ? bd.values[val] : bd.shortValues[val]);
                        }
                    }
                }
            }
            if (!(didWake || wakelockTag == null)) {
                pw.print(longNames ? " wake_lock=" : ",w=");
                if (longNames) {
                    UserHandle.formatUid(pw, wakelockTag.uid);
                    pw.print(":\"");
                    pw.print(wakelockTag.string);
                    pw.print("\"");
                    return;
                }
                pw.print(wakelockTag.poolIdx);
            }
        }
    }

    public void prepareForDumpLocked() {
    }

    public static class HistoryPrinter {
        long lastTime = -1;
        int oldHealth = -1;
        int oldLevel = -1;
        int oldPlug = -1;
        int oldState = 0;
        int oldState2 = 0;
        int oldStatus = -1;
        int oldTemp = -1;
        int oldVolt = -1;

        /* access modifiers changed from: package-private */
        public void reset() {
            this.oldState2 = 0;
            this.oldState = 0;
            this.oldLevel = -1;
            this.oldStatus = -1;
            this.oldHealth = -1;
            this.oldPlug = -1;
            this.oldTemp = -1;
            this.oldVolt = -1;
        }

        public void printNextItem(PrintWriter pw, HistoryItem rec, long baseTime, boolean checkin, boolean verbose) {
            if (!checkin) {
                pw.print("  ");
                TimeUtils.formatDuration(rec.time - baseTime, pw, 19);
                pw.print(" (");
                pw.print(rec.numReadInts);
                pw.print(") ");
            } else {
                pw.print(9);
                pw.print(',');
                pw.print(BatteryStats.HISTORY_DATA);
                pw.print(',');
                if (this.lastTime < 0) {
                    pw.print(rec.time - baseTime);
                } else {
                    pw.print(rec.time - this.lastTime);
                }
                this.lastTime = rec.time;
            }
            if (rec.cmd == 4) {
                if (checkin) {
                    pw.print(":");
                }
                pw.println("START");
                reset();
            } else if (rec.cmd == 5 || rec.cmd == 7) {
                if (checkin) {
                    pw.print(":");
                }
                if (rec.cmd == 7) {
                    pw.print("RESET:");
                    reset();
                }
                pw.print("TIME:");
                if (checkin) {
                    pw.println(rec.currentTime);
                    return;
                }
                pw.print(" ");
                pw.println(DateFormat.format("yyyy-MM-dd-HH-mm-ss", rec.currentTime).toString());
            } else if (rec.cmd == 6) {
                if (checkin) {
                    pw.print(":");
                }
                pw.println("*OVERFLOW*");
            } else {
                if (!checkin) {
                    if (rec.batteryLevel < 10) {
                        pw.print("00");
                    } else if (rec.batteryLevel < 100) {
                        pw.print(WifiEnterpriseConfig.ENGINE_DISABLE);
                    }
                    pw.print((int) rec.batteryLevel);
                    if (verbose) {
                        pw.print(" ");
                        if (rec.states >= 0) {
                            if (rec.states < 16) {
                                pw.print("0000000");
                            } else if (rec.states < 256) {
                                pw.print("000000");
                            } else if (rec.states < 4096) {
                                pw.print("00000");
                            } else if (rec.states < 65536) {
                                pw.print("0000");
                            } else if (rec.states < 1048576) {
                                pw.print("000");
                            } else if (rec.states < 16777216) {
                                pw.print("00");
                            } else if (rec.states < 268435456) {
                                pw.print(WifiEnterpriseConfig.ENGINE_DISABLE);
                            }
                        }
                        pw.print(Integer.toHexString(rec.states));
                    }
                } else if (this.oldLevel != rec.batteryLevel) {
                    this.oldLevel = rec.batteryLevel;
                    pw.print(",Bl=");
                    pw.print((int) rec.batteryLevel);
                }
                if (this.oldStatus != rec.batteryStatus) {
                    this.oldStatus = rec.batteryStatus;
                    pw.print(checkin ? ",Bs=" : " status=");
                    switch (this.oldStatus) {
                        case 1:
                            pw.print(checkin ? "?" : "unknown");
                            break;
                        case 2:
                            pw.print(checkin ? FullBackup.CACHE_TREE_TOKEN : "charging");
                            break;
                        case 3:
                            pw.print(checkin ? "d" : "discharging");
                            break;
                        case 4:
                            pw.print(checkin ? "n" : "not-charging");
                            break;
                        case 5:
                            pw.print(checkin ? FullBackup.DATA_TREE_TOKEN : "full");
                            break;
                        default:
                            pw.print(this.oldStatus);
                            break;
                    }
                }
                if (this.oldHealth != rec.batteryHealth) {
                    this.oldHealth = rec.batteryHealth;
                    pw.print(checkin ? ",Bh=" : " health=");
                    switch (this.oldHealth) {
                        case 1:
                            pw.print(checkin ? "?" : "unknown");
                            break;
                        case 2:
                            pw.print(checkin ? "g" : "good");
                            break;
                        case 3:
                            pw.print(checkin ? BatteryStats.HISTORY_DATA : "overheat");
                            break;
                        case 4:
                            pw.print(checkin ? "d" : "dead");
                            break;
                        case 5:
                            pw.print(checkin ? "v" : "over-voltage");
                            break;
                        case 6:
                            pw.print(checkin ? FullBackup.DATA_TREE_TOKEN : "failure");
                            break;
                        case 7:
                            pw.print(checkin ? FullBackup.CACHE_TREE_TOKEN : "cold");
                            break;
                        default:
                            pw.print(this.oldHealth);
                            break;
                    }
                }
                if (this.oldPlug != rec.batteryPlugType) {
                    this.oldPlug = rec.batteryPlugType;
                    pw.print(checkin ? ",Bp=" : " plug=");
                    switch (this.oldPlug) {
                        case 0:
                            pw.print(checkin ? "n" : "none");
                            break;
                        case 1:
                            pw.print(checkin ? FullBackup.APK_TREE_TOKEN : "ac");
                            break;
                        case 2:
                            pw.print(checkin ? "u" : Context.USB_SERVICE);
                            break;
                        case 3:
                        default:
                            pw.print(this.oldPlug);
                            break;
                        case 4:
                            pw.print(checkin ? "w" : "wireless");
                            break;
                    }
                }
                if (this.oldTemp != rec.batteryTemperature) {
                    this.oldTemp = rec.batteryTemperature;
                    pw.print(checkin ? ",Bt=" : " temp=");
                    pw.print(this.oldTemp);
                }
                if (this.oldVolt != rec.batteryVoltage) {
                    this.oldVolt = rec.batteryVoltage;
                    pw.print(checkin ? ",Bv=" : " volt=");
                    pw.print(this.oldVolt);
                }
                BatteryStats.printBitDescriptions(pw, this.oldState, rec.states, rec.wakelockTag, BatteryStats.HISTORY_STATE_DESCRIPTIONS, !checkin);
                BatteryStats.printBitDescriptions(pw, this.oldState2, rec.states2, null, BatteryStats.HISTORY_STATE2_DESCRIPTIONS, !checkin);
                if (rec.wakeReasonTag != null) {
                    if (checkin) {
                        pw.print(",wr=");
                        pw.print(rec.wakeReasonTag.poolIdx);
                    } else {
                        pw.print(" wake_reason=");
                        pw.print(rec.wakeReasonTag.uid);
                        pw.print(":\"");
                        pw.print(rec.wakeReasonTag.string);
                        pw.print("\"");
                    }
                }
                if (rec.eventCode != 0) {
                    pw.print(checkin ? "," : " ");
                    if ((rec.eventCode & 32768) != 0) {
                        pw.print("+");
                    } else if ((rec.eventCode & 16384) != 0) {
                        pw.print(NativeLibraryHelper.CLEAR_ABI_OVERRIDE);
                    }
                    String[] eventNames = checkin ? BatteryStats.HISTORY_EVENT_CHECKIN_NAMES : BatteryStats.HISTORY_EVENT_NAMES;
                    int idx = rec.eventCode & HistoryItem.EVENT_TYPE_MASK;
                    if (idx < 0 || idx >= eventNames.length) {
                        pw.print(checkin ? "Ev" : Notification.CATEGORY_EVENT);
                        pw.print(idx);
                    } else {
                        pw.print(eventNames[idx]);
                    }
                    pw.print("=");
                    if (checkin) {
                        pw.print(rec.eventTag.poolIdx);
                    } else {
                        UserHandle.formatUid(pw, rec.eventTag.uid);
                        pw.print(":\"");
                        pw.print(rec.eventTag.string);
                        pw.print("\"");
                    }
                }
                pw.println();
                this.oldState = rec.states;
                this.oldState2 = rec.states2;
            }
        }
    }

    private void printSizeValue(PrintWriter pw, long size) {
        float result = (float) size;
        String suffix = ProxyInfo.LOCAL_EXCL_LIST;
        if (result >= 10240.0f) {
            suffix = "KB";
            result /= 1024.0f;
        }
        if (result >= 10240.0f) {
            suffix = "MB";
            result /= 1024.0f;
        }
        if (result >= 10240.0f) {
            suffix = "GB";
            result /= 1024.0f;
        }
        if (result >= 10240.0f) {
            suffix = "TB";
            result /= 1024.0f;
        }
        if (result >= 10240.0f) {
            suffix = "PB";
            result /= 1024.0f;
        }
        pw.print((int) result);
        pw.print(suffix);
    }

    private static boolean dumpDurationSteps(PrintWriter pw, String header, long[] steps, int count, boolean checkin) {
        if (count <= 0) {
            return false;
        }
        if (!checkin) {
            pw.println(header);
        }
        String[] lineArgs = new String[4];
        for (int i = 0; i < count; i++) {
            long duration = steps[i] & STEP_LEVEL_TIME_MASK;
            int level = (int) ((steps[i] & STEP_LEVEL_LEVEL_MASK) >> 40);
            long initMode = (steps[i] & STEP_LEVEL_INITIAL_MODE_MASK) >> 48;
            long modMode = (steps[i] & STEP_LEVEL_MODIFIED_MODE_MASK) >> 56;
            if (checkin) {
                lineArgs[0] = Long.toString(duration);
                lineArgs[1] = Integer.toString(level);
                if ((3 & modMode) == 0) {
                    switch (((int) (3 & initMode)) + 1) {
                        case 1:
                            lineArgs[2] = "s-";
                            break;
                        case 2:
                            lineArgs[2] = "s+";
                            break;
                        case 3:
                            lineArgs[2] = "sd";
                            break;
                        case 4:
                            lineArgs[2] = "sds";
                            break;
                        default:
                            lineArgs[1] = "?";
                            break;
                    }
                } else {
                    lineArgs[2] = ProxyInfo.LOCAL_EXCL_LIST;
                }
                if ((4 & modMode) == 0) {
                    lineArgs[3] = (4 & initMode) != 0 ? "p+" : "p-";
                } else {
                    lineArgs[3] = ProxyInfo.LOCAL_EXCL_LIST;
                }
                dumpLine(pw, 0, "i", header, lineArgs);
            } else {
                pw.print("  #");
                pw.print(i);
                pw.print(": ");
                TimeUtils.formatDuration(duration, pw);
                pw.print(" to ");
                pw.print(level);
                boolean haveModes = false;
                if ((3 & modMode) == 0) {
                    pw.print(" (");
                    switch (((int) (3 & initMode)) + 1) {
                        case 1:
                            pw.print("screen-off");
                            break;
                        case 2:
                            pw.print("screen-on");
                            break;
                        case 3:
                            pw.print("screen-doze");
                            break;
                        case 4:
                            pw.print("screen-doze-suspend");
                            break;
                        default:
                            lineArgs[1] = "screen-?";
                            break;
                    }
                    haveModes = true;
                }
                if ((4 & modMode) == 0) {
                    pw.print(haveModes ? ", " : " (");
                    pw.print((4 & initMode) != 0 ? "power-save-on" : "power-save-off");
                    haveModes = true;
                }
                if (haveModes) {
                    pw.print(")");
                }
                pw.println();
            }
        }
        return true;
    }

    private void dumpHistoryLocked(PrintWriter pw, int flags, long histStart, boolean checkin) {
        HistoryPrinter hprinter = new HistoryPrinter();
        HistoryItem rec = new HistoryItem();
        long lastTime = -1;
        long baseTime = -1;
        boolean printed = false;
        HistoryEventTracker tracker = null;
        while (getNextHistoryLocked(rec)) {
            lastTime = rec.time;
            if (baseTime < 0) {
                baseTime = lastTime;
            }
            if (rec.time >= histStart) {
                if (histStart >= 0 && !printed) {
                    if (rec.cmd == 5 || rec.cmd == 7 || rec.cmd == 4) {
                        printed = true;
                        hprinter.printNextItem(pw, rec, baseTime, checkin, (flags & 16) != 0);
                        rec.cmd = 0;
                    } else if (rec.currentTime != 0) {
                        printed = true;
                        byte cmd = rec.cmd;
                        rec.cmd = 5;
                        hprinter.printNextItem(pw, rec, baseTime, checkin, (flags & 16) != 0);
                        rec.cmd = cmd;
                    }
                    if (tracker != null) {
                        if (rec.cmd != 0) {
                            hprinter.printNextItem(pw, rec, baseTime, checkin, (flags & 16) != 0);
                            rec.cmd = 0;
                        }
                        int oldEventCode = rec.eventCode;
                        HistoryTag oldEventTag = rec.eventTag;
                        rec.eventTag = new HistoryTag();
                        for (int i = 0; i < 9; i++) {
                            HashMap<String, SparseIntArray> active = tracker.getStateForEvent(i);
                            if (active != null) {
                                for (Map.Entry<String, SparseIntArray> ent : active.entrySet()) {
                                    SparseIntArray uids = ent.getValue();
                                    for (int j = 0; j < uids.size(); j++) {
                                        rec.eventCode = i;
                                        rec.eventTag.string = ent.getKey();
                                        rec.eventTag.uid = uids.keyAt(j);
                                        rec.eventTag.poolIdx = uids.valueAt(j);
                                        hprinter.printNextItem(pw, rec, baseTime, checkin, (flags & 16) != 0);
                                        rec.wakeReasonTag = null;
                                        rec.wakelockTag = null;
                                    }
                                }
                            }
                        }
                        rec.eventCode = oldEventCode;
                        rec.eventTag = oldEventTag;
                        tracker = null;
                    }
                }
                hprinter.printNextItem(pw, rec, baseTime, checkin, (flags & 16) != 0);
            }
        }
        if (histStart >= 0) {
            commitCurrentHistoryBatchLocked();
            pw.print(checkin ? "NEXT: " : "  NEXT: ");
            pw.println(1 + lastTime);
        }
    }

    public void dumpLocked(Context context, PrintWriter pw, int flags, int reqUid, long histStart) {
        boolean z;
        prepareForDumpLocked();
        boolean filtering = (flags & 7) != 0;
        if ((flags & 4) != 0 || !filtering) {
            long historyTotalSize = (long) getHistoryTotalSize();
            long historyUsedSize = (long) getHistoryUsedSize();
            if (startIteratingHistoryLocked()) {
                try {
                    pw.print("Battery History (");
                    pw.print((100 * historyUsedSize) / historyTotalSize);
                    pw.print("% used, ");
                    printSizeValue(pw, historyUsedSize);
                    pw.print(" used of ");
                    printSizeValue(pw, historyTotalSize);
                    pw.print(", ");
                    pw.print(getHistoryStringPoolSize());
                    pw.print(" strings using ");
                    printSizeValue(pw, (long) getHistoryStringPoolBytes());
                    pw.println("):");
                    dumpHistoryLocked(pw, flags, histStart, false);
                    pw.println();
                } finally {
                    finishIteratingHistoryLocked();
                }
            }
            if (startIteratingOldHistoryLocked()) {
                try {
                    HistoryItem rec = new HistoryItem();
                    pw.println("Old battery History:");
                    HistoryPrinter hprinter = new HistoryPrinter();
                    long baseTime = -1;
                    while (getNextOldHistoryLocked(rec)) {
                        if (baseTime < 0) {
                            baseTime = rec.time;
                        }
                        hprinter.printNextItem(pw, rec, baseTime, false, (flags & 16) != 0);
                    }
                    pw.println();
                } finally {
                    finishIteratingOldHistoryLocked();
                }
            }
        }
        if (!filtering || (flags & 3) != 0) {
            if (!filtering) {
                SparseArray<? extends Uid> uidStats = getUidStats();
                int NU = uidStats.size();
                boolean didPid = false;
                long nowRealtime = SystemClock.elapsedRealtime();
                for (int i = 0; i < NU; i++) {
                    SparseArray<? extends Uid.Pid> pids = ((Uid) uidStats.valueAt(i)).getPidStats();
                    if (pids != null) {
                        for (int j = 0; j < pids.size(); j++) {
                            Uid.Pid pid = (Uid.Pid) pids.valueAt(j);
                            if (!didPid) {
                                pw.println("Per-PID Stats:");
                                didPid = true;
                            }
                            long j2 = pid.mWakeSumMs;
                            long j3 = pid.mWakeNesting > 0 ? nowRealtime - pid.mWakeStartMs : 0;
                            pw.print("  PID ");
                            pw.print(pids.keyAt(j));
                            pw.print(" wake time: ");
                            TimeUtils.formatDuration(j2 + j3, pw);
                            pw.println(ProxyInfo.LOCAL_EXCL_LIST);
                        }
                    }
                }
                if (didPid) {
                    pw.println();
                }
            }
            if (!filtering || (flags & 2) != 0) {
                if (dumpDurationSteps(pw, "Discharge step durations:", getDischargeStepDurationsArray(), getNumDischargeStepDurations(), false)) {
                    long timeRemaining = computeBatteryTimeRemaining(SystemClock.elapsedRealtime());
                    if (timeRemaining >= 0) {
                        pw.print("  Estimated discharge time remaining: ");
                        TimeUtils.formatDuration(timeRemaining / 1000, pw);
                        pw.println();
                    }
                    pw.println();
                }
                if (dumpDurationSteps(pw, "Charge step durations:", getChargeStepDurationsArray(), getNumChargeStepDurations(), false)) {
                    long timeRemaining2 = computeChargeTimeRemaining(SystemClock.elapsedRealtime());
                    if (timeRemaining2 >= 0) {
                        pw.print("  Estimated charge time remaining: ");
                        TimeUtils.formatDuration(timeRemaining2 / 1000, pw);
                        pw.println();
                    }
                    pw.println();
                }
                pw.println("Statistics since last charge:");
                pw.println("  System starts: " + getStartCount() + ", currently on battery: " + getIsOnBattery());
                dumpLocked(context, pw, ProxyInfo.LOCAL_EXCL_LIST, 0, reqUid, (flags & 32) != 0);
                pw.println();
            }
            if (!filtering || (flags & 1) != 0) {
                pw.println("Statistics since last unplugged:");
                if ((flags & 32) != 0) {
                    z = true;
                } else {
                    z = false;
                }
                dumpLocked(context, pw, ProxyInfo.LOCAL_EXCL_LIST, 2, reqUid, z);
            }
        }
    }

    public void dumpCheckinLocked(Context context, PrintWriter pw, List<ApplicationInfo> apps, int flags, long histStart) {
        prepareForDumpLocked();
        dumpLine(pw, 0, "i", VERSION_DATA, "11", Integer.valueOf(getParcelVersion()), getStartPlatformVersion(), getEndPlatformVersion());
        long historyBaseTime = getHistoryBaseTime() + SystemClock.elapsedRealtime();
        boolean filtering = (flags & 7) != 0;
        if (!((flags & 8) == 0 && (flags & 4) == 0) && startIteratingHistoryLocked()) {
            for (int i = 0; i < getHistoryStringPoolSize(); i++) {
                try {
                    pw.print(9);
                    pw.print(',');
                    pw.print(HISTORY_STRING_POOL);
                    pw.print(',');
                    pw.print(i);
                    pw.print(",");
                    pw.print(getHistoryTagPoolUid(i));
                    pw.print(",\"");
                    pw.print(getHistoryTagPoolString(i).replace("\\", "\\\\").replace("\"", "\\\""));
                    pw.print("\"");
                    pw.println();
                } finally {
                    finishIteratingHistoryLocked();
                }
            }
            dumpHistoryLocked(pw, flags, histStart, true);
        }
        if (!filtering || (flags & 3) != 0) {
            if (apps != null) {
                SparseArray<ArrayList<String>> uids = new SparseArray<>();
                for (int i2 = 0; i2 < apps.size(); i2++) {
                    ApplicationInfo ai = apps.get(i2);
                    ArrayList<String> pkgs = uids.get(ai.uid);
                    if (pkgs == null) {
                        pkgs = new ArrayList<>();
                        uids.put(ai.uid, pkgs);
                    }
                    pkgs.add(ai.packageName);
                }
                SparseArray<? extends Uid> uidStats = getUidStats();
                int NU = uidStats.size();
                String[] lineArgs = new String[2];
                for (int i3 = 0; i3 < NU; i3++) {
                    int uid = uidStats.keyAt(i3);
                    ArrayList<String> pkgs2 = uids.get(uid);
                    if (pkgs2 != null) {
                        for (int j = 0; j < pkgs2.size(); j++) {
                            lineArgs[0] = Integer.toString(uid);
                            lineArgs[1] = pkgs2.get(j);
                            dumpLine(pw, 0, "i", "uid", lineArgs);
                        }
                    }
                }
            }
            if (!filtering || (flags & 2) != 0) {
                dumpDurationSteps(pw, DISCHARGE_STEP_DATA, getDischargeStepDurationsArray(), getNumDischargeStepDurations(), true);
                String[] lineArgs2 = new String[1];
                long timeRemaining = computeBatteryTimeRemaining(SystemClock.elapsedRealtime());
                if (timeRemaining >= 0) {
                    lineArgs2[0] = Long.toString(timeRemaining);
                    dumpLine(pw, 0, "i", DISCHARGE_TIME_REMAIN_DATA, lineArgs2);
                }
                dumpDurationSteps(pw, CHARGE_STEP_DATA, getChargeStepDurationsArray(), getNumChargeStepDurations(), true);
                long timeRemaining2 = computeChargeTimeRemaining(SystemClock.elapsedRealtime());
                if (timeRemaining2 >= 0) {
                    lineArgs2[0] = Long.toString(timeRemaining2);
                    dumpLine(pw, 0, "i", CHARGE_TIME_REMAIN_DATA, lineArgs2);
                }
                dumpCheckinLocked(context, pw, 0, -1, (flags & 32) != 0);
            }
            if (!filtering || (flags & 1) != 0) {
                dumpCheckinLocked(context, pw, 2, -1, (flags & 32) != 0);
            }
        }
    }
}

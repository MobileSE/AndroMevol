package android.net.wifi;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.util.AsyncChannel;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class WifiScanner {
    private static final int BASE = 159744;
    public static final int CMD_AP_FOUND = 159753;
    public static final int CMD_AP_LOST = 159754;
    public static final int CMD_CONFIGURE_WIFI_CHANGE = 159757;
    public static final int CMD_FULL_SCAN_RESULT = 159764;
    public static final int CMD_GET_SCAN_RESULTS = 159748;
    public static final int CMD_OP_FAILED = 159762;
    public static final int CMD_OP_SUCCEEDED = 159761;
    public static final int CMD_PERIOD_CHANGED = 159763;
    public static final int CMD_RESET_HOTLIST = 159751;
    public static final int CMD_SCAN = 159744;
    public static final int CMD_SCAN_RESULT = 159749;
    public static final int CMD_SET_HOTLIST = 159750;
    public static final int CMD_START_BACKGROUND_SCAN = 159746;
    public static final int CMD_START_TRACKING_CHANGE = 159755;
    public static final int CMD_STOP_BACKGROUND_SCAN = 159747;
    public static final int CMD_STOP_TRACKING_CHANGE = 159756;
    public static final int CMD_WIFI_CHANGES_STABILIZED = 159760;
    public static final int CMD_WIFI_CHANGE_DETECTED = 159759;
    private static final boolean DBG = true;
    public static final String GET_AVAILABLE_CHANNELS_EXTRA = "Channels";
    private static final int INVALID_KEY = 0;
    public static final int MAX_SCAN_PERIOD_MS = 1024000;
    public static final int MIN_SCAN_PERIOD_MS = 1000;
    public static final int REASON_INVALID_LISTENER = -2;
    public static final int REASON_INVALID_REQUEST = -3;
    public static final int REASON_NOT_AUTHORIZED = -4;
    public static final int REASON_SUCCEEDED = 0;
    public static final int REASON_UNSPECIFIED = -1;
    public static final int REPORT_EVENT_AFTER_BUFFER_FULL = 0;
    public static final int REPORT_EVENT_AFTER_EACH_SCAN = 1;
    public static final int REPORT_EVENT_FULL_SCAN_RESULT = 2;
    private static final String TAG = "WifiScanner";
    public static final int WIFI_BAND_24_GHZ = 1;
    public static final int WIFI_BAND_5_GHZ = 2;
    public static final int WIFI_BAND_5_GHZ_DFS_ONLY = 4;
    public static final int WIFI_BAND_5_GHZ_WITH_DFS = 6;
    public static final int WIFI_BAND_BOTH = 3;
    public static final int WIFI_BAND_BOTH_WITH_DFS = 7;
    public static final int WIFI_BAND_UNSPECIFIED = 0;
    private static AsyncChannel sAsyncChannel;
    private static CountDownLatch sConnected;
    private static HandlerThread sHandlerThread;
    private static int sListenerKey = 1;
    private static final SparseArray sListenerMap = new SparseArray();
    private static final Object sListenerMapLock = new Object();
    private static int sThreadRefCount;
    private static final Object sThreadRefLock = new Object();
    private Context mContext;
    private IWifiScanner mService;

    public interface ActionListener {
        void onFailure(int i, String str);

        void onSuccess();
    }

    public static class BssidInfo {
        public String bssid;
        public int frequencyHint;
        public int high;
        public int low;
    }

    public interface BssidListener extends ActionListener {
        void onFound(ScanResult[] scanResultArr);
    }

    public interface ScanListener extends ActionListener {
        void onFullResult(ScanResult scanResult);

        void onPeriodChanged(int i);

        void onResults(ScanResult[] scanResultArr);
    }

    public interface WifiChangeListener extends ActionListener {
        void onChanging(ScanResult[] scanResultArr);

        void onQuiescence(ScanResult[] scanResultArr);
    }

    public List<Integer> getAvailableChannels(int band) {
        try {
            return this.mService.getAvailableChannels(band).getIntegerArrayList(GET_AVAILABLE_CHANNELS_EXTRA);
        } catch (RemoteException e) {
            return null;
        }
    }

    public static class ChannelSpec {
        public int dwellTimeMS = 0;
        public int frequency;
        public boolean passive = false;

        public ChannelSpec(int frequency2) {
            this.frequency = frequency2;
        }
    }

    public static class ScanSettings implements Parcelable {
        public static final Parcelable.Creator<ScanSettings> CREATOR = new Parcelable.Creator<ScanSettings>() {
            /* class android.net.wifi.WifiScanner.ScanSettings.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public ScanSettings createFromParcel(Parcel in) {
                ScanSettings settings = new ScanSettings();
                settings.band = in.readInt();
                settings.periodInMs = in.readInt();
                settings.reportEvents = in.readInt();
                settings.numBssidsPerScan = in.readInt();
                int num_channels = in.readInt();
                settings.channels = new ChannelSpec[num_channels];
                for (int i = 0; i < num_channels; i++) {
                    ChannelSpec spec = new ChannelSpec(in.readInt());
                    spec.dwellTimeMS = in.readInt();
                    spec.passive = in.readInt() == 1;
                    settings.channels[i] = spec;
                }
                return settings;
            }

            @Override // android.os.Parcelable.Creator
            public ScanSettings[] newArray(int size) {
                return new ScanSettings[size];
            }
        };
        public int band;
        public ChannelSpec[] channels;
        public int numBssidsPerScan;
        public int periodInMs;
        public int reportEvents;

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.band);
            dest.writeInt(this.periodInMs);
            dest.writeInt(this.reportEvents);
            dest.writeInt(this.numBssidsPerScan);
            if (this.channels != null) {
                dest.writeInt(this.channels.length);
                for (int i = 0; i < this.channels.length; i++) {
                    dest.writeInt(this.channels[i].frequency);
                    dest.writeInt(this.channels[i].dwellTimeMS);
                    dest.writeInt(this.channels[i].passive ? 1 : 0);
                }
                return;
            }
            dest.writeInt(0);
        }
    }

    public static class ParcelableScanResults implements Parcelable {
        public static final Parcelable.Creator<ParcelableScanResults> CREATOR = new Parcelable.Creator<ParcelableScanResults>() {
            /* class android.net.wifi.WifiScanner.ParcelableScanResults.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public ParcelableScanResults createFromParcel(Parcel in) {
                int n = in.readInt();
                ScanResult[] results = new ScanResult[n];
                for (int i = 0; i < n; i++) {
                    results[i] = ScanResult.CREATOR.createFromParcel(in);
                }
                return new ParcelableScanResults(results);
            }

            @Override // android.os.Parcelable.Creator
            public ParcelableScanResults[] newArray(int size) {
                return new ParcelableScanResults[size];
            }
        };
        public ScanResult[] mResults;

        public ParcelableScanResults(ScanResult[] results) {
            this.mResults = results;
        }

        public ScanResult[] getResults() {
            return this.mResults;
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            if (this.mResults != null) {
                dest.writeInt(this.mResults.length);
                for (int i = 0; i < this.mResults.length; i++) {
                    this.mResults[i].writeToParcel(dest, flags);
                }
                return;
            }
            dest.writeInt(0);
        }
    }

    public void startBackgroundScan(ScanSettings settings, ScanListener listener) {
        validateChannel();
        sAsyncChannel.sendMessage((int) CMD_START_BACKGROUND_SCAN, 0, putListener(listener), settings);
    }

    public void stopBackgroundScan(ScanListener listener) {
        validateChannel();
        sAsyncChannel.sendMessage((int) CMD_STOP_BACKGROUND_SCAN, 0, removeListener(listener));
    }

    public ScanResult[] getScanResults() {
        validateChannel();
        return (ScanResult[]) sAsyncChannel.sendMessageSynchronously((int) CMD_GET_SCAN_RESULTS, 0).obj;
    }

    public static class WifiChangeSettings implements Parcelable {
        public static final Parcelable.Creator<WifiChangeSettings> CREATOR = new Parcelable.Creator<WifiChangeSettings>() {
            /* class android.net.wifi.WifiScanner.WifiChangeSettings.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public WifiChangeSettings createFromParcel(Parcel in) {
                WifiChangeSettings settings = new WifiChangeSettings();
                settings.rssiSampleSize = in.readInt();
                settings.lostApSampleSize = in.readInt();
                settings.unchangedSampleSize = in.readInt();
                settings.minApsBreachingThreshold = in.readInt();
                settings.periodInMs = in.readInt();
                int len = in.readInt();
                settings.bssidInfos = new BssidInfo[len];
                for (int i = 0; i < len; i++) {
                    BssidInfo info = new BssidInfo();
                    info.bssid = in.readString();
                    info.low = in.readInt();
                    info.high = in.readInt();
                    info.frequencyHint = in.readInt();
                    settings.bssidInfos[i] = info;
                }
                return settings;
            }

            @Override // android.os.Parcelable.Creator
            public WifiChangeSettings[] newArray(int size) {
                return new WifiChangeSettings[size];
            }
        };
        public BssidInfo[] bssidInfos;
        public int lostApSampleSize;
        public int minApsBreachingThreshold;
        public int periodInMs;
        public int rssiSampleSize;
        public int unchangedSampleSize;

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.rssiSampleSize);
            dest.writeInt(this.lostApSampleSize);
            dest.writeInt(this.unchangedSampleSize);
            dest.writeInt(this.minApsBreachingThreshold);
            dest.writeInt(this.periodInMs);
            if (this.bssidInfos != null) {
                dest.writeInt(this.bssidInfos.length);
                for (int i = 0; i < this.bssidInfos.length; i++) {
                    BssidInfo info = this.bssidInfos[i];
                    dest.writeString(info.bssid);
                    dest.writeInt(info.low);
                    dest.writeInt(info.high);
                    dest.writeInt(info.frequencyHint);
                }
                return;
            }
            dest.writeInt(0);
        }
    }

    public void configureWifiChange(int rssiSampleSize, int lostApSampleSize, int unchangedSampleSize, int minApsBreachingThreshold, int periodInMs, BssidInfo[] bssidInfos) {
        validateChannel();
        WifiChangeSettings settings = new WifiChangeSettings();
        settings.rssiSampleSize = rssiSampleSize;
        settings.lostApSampleSize = lostApSampleSize;
        settings.unchangedSampleSize = unchangedSampleSize;
        settings.minApsBreachingThreshold = minApsBreachingThreshold;
        settings.periodInMs = periodInMs;
        settings.bssidInfos = bssidInfos;
        configureWifiChange(settings);
    }

    public void startTrackingWifiChange(WifiChangeListener listener) {
        validateChannel();
        sAsyncChannel.sendMessage((int) CMD_START_TRACKING_CHANGE, 0, putListener(listener));
    }

    public void stopTrackingWifiChange(WifiChangeListener listener) {
        validateChannel();
        sAsyncChannel.sendMessage((int) CMD_STOP_TRACKING_CHANGE, 0, removeListener(listener));
    }

    public void configureWifiChange(WifiChangeSettings settings) {
        validateChannel();
        sAsyncChannel.sendMessage((int) CMD_CONFIGURE_WIFI_CHANGE, 0, 0, settings);
    }

    public static class HotlistSettings implements Parcelable {
        public static final Parcelable.Creator<HotlistSettings> CREATOR = new Parcelable.Creator<HotlistSettings>() {
            /* class android.net.wifi.WifiScanner.HotlistSettings.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public HotlistSettings createFromParcel(Parcel in) {
                HotlistSettings settings = new HotlistSettings();
                settings.apLostThreshold = in.readInt();
                int n = in.readInt();
                settings.bssidInfos = new BssidInfo[n];
                for (int i = 0; i < n; i++) {
                    BssidInfo info = new BssidInfo();
                    info.bssid = in.readString();
                    info.low = in.readInt();
                    info.high = in.readInt();
                    info.frequencyHint = in.readInt();
                    settings.bssidInfos[i] = info;
                }
                return settings;
            }

            @Override // android.os.Parcelable.Creator
            public HotlistSettings[] newArray(int size) {
                return new HotlistSettings[size];
            }
        };
        public int apLostThreshold;
        public BssidInfo[] bssidInfos;

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.apLostThreshold);
            if (this.bssidInfos != null) {
                dest.writeInt(this.bssidInfos.length);
                for (int i = 0; i < this.bssidInfos.length; i++) {
                    BssidInfo info = this.bssidInfos[i];
                    dest.writeString(info.bssid);
                    dest.writeInt(info.low);
                    dest.writeInt(info.high);
                    dest.writeInt(info.frequencyHint);
                }
                return;
            }
            dest.writeInt(0);
        }
    }

    public void startTrackingBssids(BssidInfo[] bssidInfos, int apLostThreshold, BssidListener listener) {
        validateChannel();
        HotlistSettings settings = new HotlistSettings();
        settings.bssidInfos = bssidInfos;
        sAsyncChannel.sendMessage((int) CMD_SET_HOTLIST, 0, putListener(listener), settings);
    }

    public void stopTrackingBssids(BssidListener listener) {
        validateChannel();
        sAsyncChannel.sendMessage((int) CMD_RESET_HOTLIST, 0, removeListener(listener));
    }

    public WifiScanner(Context context, IWifiScanner service) {
        this.mContext = context;
        this.mService = service;
        init();
    }

    private void init() {
        synchronized (sThreadRefLock) {
            int i = sThreadRefCount + 1;
            sThreadRefCount = i;
            if (i == 1) {
                Messenger messenger = null;
                try {
                    messenger = this.mService.getMessenger();
                } catch (RemoteException | SecurityException e) {
                }
                if (messenger == null) {
                    sAsyncChannel = null;
                    return;
                }
                sHandlerThread = new HandlerThread(TAG);
                sAsyncChannel = new AsyncChannel();
                sConnected = new CountDownLatch(1);
                sHandlerThread.start();
                sAsyncChannel.connect(this.mContext, new ServiceHandler(sHandlerThread.getLooper()), messenger);
                try {
                    sConnected.await();
                } catch (InterruptedException e2) {
                    Log.e(TAG, "interrupted wait at init");
                }
            }
        }
    }

    private void validateChannel() {
        if (sAsyncChannel == null) {
            throw new IllegalStateException("No permission to access and change wifi or a bad initialization");
        }
    }

    private static int putListener(Object listener) {
        int key;
        if (listener == null) {
            return 0;
        }
        synchronized (sListenerMapLock) {
            do {
                key = sListenerKey;
                sListenerKey = key + 1;
            } while (key == 0);
            sListenerMap.put(key, listener);
        }
        return key;
    }

    /* access modifiers changed from: private */
    public static Object getListener(int key) {
        Object obj;
        if (key == 0) {
            return null;
        }
        synchronized (sListenerMapLock) {
            obj = sListenerMap.get(key);
        }
        return obj;
    }

    private static int getListenerKey(Object listener) {
        int i = 0;
        if (listener != null) {
            synchronized (sListenerMapLock) {
                int index = sListenerMap.indexOfValue(listener);
                if (index != -1) {
                    i = sListenerMap.keyAt(index);
                }
            }
        }
        return i;
    }

    /* access modifiers changed from: private */
    public static Object removeListener(int key) {
        Object obj;
        if (key == 0) {
            return null;
        }
        synchronized (sListenerMapLock) {
            obj = sListenerMap.get(key);
            sListenerMap.remove(key);
        }
        return obj;
    }

    private static int removeListener(Object listener) {
        int key = getListenerKey(listener);
        if (key != 0) {
            synchronized (sListenerMapLock) {
                sListenerMap.remove(key);
            }
        }
        return key;
    }

    public static class OperationResult implements Parcelable {
        public static final Parcelable.Creator<OperationResult> CREATOR = new Parcelable.Creator<OperationResult>() {
            /* class android.net.wifi.WifiScanner.OperationResult.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public OperationResult createFromParcel(Parcel in) {
                return new OperationResult(in.readInt(), in.readString());
            }

            @Override // android.os.Parcelable.Creator
            public OperationResult[] newArray(int size) {
                return new OperationResult[size];
            }
        };
        public String description;
        public int reason;

        public OperationResult(int reason2, String description2) {
            this.reason = reason2;
            this.description = description2;
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.reason);
            dest.writeString(this.description);
        }
    }

    /* access modifiers changed from: private */
    public static class ServiceHandler extends Handler {
        ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 69632:
                    if (msg.arg1 == 0) {
                        WifiScanner.sAsyncChannel.sendMessage(69633);
                    } else {
                        Log.e(WifiScanner.TAG, "Failed to set up channel connection");
                        AsyncChannel unused = WifiScanner.sAsyncChannel = null;
                    }
                    WifiScanner.sConnected.countDown();
                    return;
                case 69633:
                case 69635:
                default:
                    Object listener = WifiScanner.getListener(msg.arg2);
                    if (listener == null) {
                        Log.d(WifiScanner.TAG, "invalid listener key = " + msg.arg2);
                        return;
                    }
                    Log.d(WifiScanner.TAG, "listener key = " + msg.arg2);
                    switch (msg.what) {
                        case WifiScanner.CMD_SCAN_RESULT /*{ENCODED_INT: 159749}*/:
                            ((ScanListener) listener).onResults(((ParcelableScanResults) msg.obj).getResults());
                            return;
                        case WifiScanner.CMD_SET_HOTLIST /*{ENCODED_INT: 159750}*/:
                        case WifiScanner.CMD_RESET_HOTLIST /*{ENCODED_INT: 159751}*/:
                        case 159752:
                        case WifiScanner.CMD_AP_LOST /*{ENCODED_INT: 159754}*/:
                        case WifiScanner.CMD_START_TRACKING_CHANGE /*{ENCODED_INT: 159755}*/:
                        case WifiScanner.CMD_STOP_TRACKING_CHANGE /*{ENCODED_INT: 159756}*/:
                        case WifiScanner.CMD_CONFIGURE_WIFI_CHANGE /*{ENCODED_INT: 159757}*/:
                        case 159758:
                        default:
                            Log.d(WifiScanner.TAG, "Ignoring message " + msg.what);
                            return;
                        case WifiScanner.CMD_AP_FOUND /*{ENCODED_INT: 159753}*/:
                            ((BssidListener) listener).onFound(((ParcelableScanResults) msg.obj).getResults());
                            return;
                        case WifiScanner.CMD_WIFI_CHANGE_DETECTED /*{ENCODED_INT: 159759}*/:
                            ((WifiChangeListener) listener).onChanging(((ParcelableScanResults) msg.obj).getResults());
                            return;
                        case WifiScanner.CMD_WIFI_CHANGES_STABILIZED /*{ENCODED_INT: 159760}*/:
                            ((WifiChangeListener) listener).onQuiescence(((ParcelableScanResults) msg.obj).getResults());
                            return;
                        case WifiScanner.CMD_OP_SUCCEEDED /*{ENCODED_INT: 159761}*/:
                            ((ActionListener) listener).onSuccess();
                            return;
                        case WifiScanner.CMD_OP_FAILED /*{ENCODED_INT: 159762}*/:
                            OperationResult result = (OperationResult) msg.obj;
                            ((ActionListener) listener).onFailure(result.reason, result.description);
                            WifiScanner.removeListener(msg.arg2);
                            return;
                        case WifiScanner.CMD_PERIOD_CHANGED /*{ENCODED_INT: 159763}*/:
                            ((ScanListener) listener).onPeriodChanged(msg.arg1);
                            return;
                        case WifiScanner.CMD_FULL_SCAN_RESULT /*{ENCODED_INT: 159764}*/:
                            ((ScanListener) listener).onFullResult((ScanResult) msg.obj);
                            return;
                    }
                case 69634:
                    return;
                case 69636:
                    Log.e(WifiScanner.TAG, "Channel connection lost");
                    AsyncChannel unused2 = WifiScanner.sAsyncChannel = null;
                    getLooper().quit();
                    return;
            }
        }
    }
}

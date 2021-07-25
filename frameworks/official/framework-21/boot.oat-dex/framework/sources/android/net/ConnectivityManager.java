package android.net;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.INetworkActivityListener;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.ArrayMap;
import android.util.Log;
import com.android.internal.telephony.ITelephony;
import com.android.internal.util.Preconditions;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import libcore.net.event.NetworkEventDispatcher;

public class ConnectivityManager {
    @Deprecated
    public static final String ACTION_BACKGROUND_DATA_SETTING_CHANGED = "android.net.conn.BACKGROUND_DATA_SETTING_CHANGED";
    public static final String ACTION_CAPTIVE_PORTAL_TEST_COMPLETED = "android.net.conn.CAPTIVE_PORTAL_TEST_COMPLETED";
    public static final String ACTION_DATA_ACTIVITY_CHANGE = "android.net.conn.DATA_ACTIVITY_CHANGE";
    public static final String ACTION_TETHER_STATE_CHANGED = "android.net.conn.TETHER_STATE_CHANGED";
    private static final int BASE = 524288;
    public static final int CALLBACK_AVAILABLE = 524290;
    public static final int CALLBACK_CAP_CHANGED = 524294;
    public static final int CALLBACK_EXIT = 524297;
    public static final int CALLBACK_IP_CHANGED = 524295;
    public static final int CALLBACK_LOSING = 524291;
    public static final int CALLBACK_LOST = 524292;
    public static final int CALLBACK_PRECHECK = 524289;
    public static final int CALLBACK_RELEASED = 524296;
    public static final int CALLBACK_UNAVAIL = 524293;
    public static final String CONNECTIVITY_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    public static final String CONNECTIVITY_ACTION_IMMEDIATE = "android.net.conn.CONNECTIVITY_CHANGE_IMMEDIATE";
    public static final int CONNECTIVITY_CHANGE_DELAY_DEFAULT = 3000;
    @Deprecated
    public static final int DEFAULT_NETWORK_PREFERENCE = 1;
    private static final int EXPIRE_LEGACY_REQUEST = 524298;
    public static final String EXTRA_ACTIVE_TETHER = "activeArray";
    public static final String EXTRA_AVAILABLE_TETHER = "availableArray";
    public static final String EXTRA_DEVICE_TYPE = "deviceType";
    public static final String EXTRA_ERRORED_TETHER = "erroredArray";
    public static final String EXTRA_EXTRA_INFO = "extraInfo";
    public static final String EXTRA_INET_CONDITION = "inetCondition";
    public static final String EXTRA_IS_ACTIVE = "isActive";
    public static final String EXTRA_IS_CAPTIVE_PORTAL = "captivePortal";
    public static final String EXTRA_IS_FAILOVER = "isFailover";
    @Deprecated
    public static final String EXTRA_NETWORK_INFO = "networkInfo";
    public static final String EXTRA_NETWORK_REQUEST_NETWORK = "networkRequestNetwork";
    public static final String EXTRA_NETWORK_REQUEST_NETWORK_REQUEST = "networkRequestNetworkRequest";
    public static final String EXTRA_NETWORK_TYPE = "networkType";
    public static final String EXTRA_NO_CONNECTIVITY = "noConnectivity";
    public static final String EXTRA_OTHER_NETWORK_INFO = "otherNetwork";
    public static final String EXTRA_REALTIME_NS = "tsNanos";
    public static final String EXTRA_REASON = "reason";
    public static final String INET_CONDITION_ACTION = "android.net.conn.INET_CONDITION_ACTION";
    private static final boolean LEGACY_DBG = true;
    private static final int LISTEN = 1;
    public static final int MAX_NETWORK_REQUEST_TIMEOUT_MS = 6000000;
    public static final int MAX_NETWORK_TYPE = 17;
    public static final int MAX_RADIO_TYPE = 17;
    public static final int NETID_UNSET = 0;
    private static final int REQUEST = 2;
    public static final int REQUEST_ID_UNSET = 0;
    private static final String TAG = "ConnectivityManager";
    public static final int TETHER_ERROR_DISABLE_NAT_ERROR = 9;
    public static final int TETHER_ERROR_ENABLE_NAT_ERROR = 8;
    public static final int TETHER_ERROR_IFACE_CFG_ERROR = 10;
    public static final int TETHER_ERROR_MASTER_ERROR = 5;
    public static final int TETHER_ERROR_NO_ERROR = 0;
    public static final int TETHER_ERROR_SERVICE_UNAVAIL = 2;
    public static final int TETHER_ERROR_TETHER_IFACE_ERROR = 6;
    public static final int TETHER_ERROR_UNAVAIL_IFACE = 4;
    public static final int TETHER_ERROR_UNKNOWN_IFACE = 1;
    public static final int TETHER_ERROR_UNSUPPORTED = 3;
    public static final int TETHER_ERROR_UNTETHER_IFACE_ERROR = 7;
    public static final int TYPE_BLUETOOTH = 7;
    public static final int TYPE_DUMMY = 8;
    public static final int TYPE_ETHERNET = 9;
    public static final int TYPE_MOBILE = 0;
    public static final int TYPE_MOBILE_CBS = 12;
    public static final int TYPE_MOBILE_DUN = 4;
    public static final int TYPE_MOBILE_EMERGENCY = 15;
    public static final int TYPE_MOBILE_FOTA = 10;
    public static final int TYPE_MOBILE_HIPRI = 5;
    public static final int TYPE_MOBILE_IA = 14;
    public static final int TYPE_MOBILE_IMS = 11;
    public static final int TYPE_MOBILE_MMS = 2;
    public static final int TYPE_MOBILE_SUPL = 3;
    public static final int TYPE_NONE = -1;
    public static final int TYPE_PROXY = 16;
    public static final int TYPE_VPN = 17;
    public static final int TYPE_WIFI = 1;
    public static final int TYPE_WIFI_P2P = 13;
    public static final int TYPE_WIMAX = 6;
    static CallbackHandler sCallbackHandler = null;
    static final AtomicInteger sCallbackRefCount = new AtomicInteger(0);
    private static HashMap<NetworkCapabilities, LegacyRequest> sLegacyRequests = new HashMap<>();
    static final HashMap<NetworkRequest, NetworkCallback> sNetworkCallback = new HashMap<>();
    private INetworkManagementService mNMService;
    private final ArrayMap<OnNetworkActiveListener, INetworkActivityListener> mNetworkActivityListeners = new ArrayMap<>();
    private final IConnectivityManager mService;

    public interface OnNetworkActiveListener {
        void onNetworkActive();
    }

    public static boolean isNetworkTypeValid(int networkType) {
        return networkType >= 0 && networkType <= 17;
    }

    public static String getNetworkTypeName(int type) {
        switch (type) {
            case 0:
                return "MOBILE";
            case 1:
                return "WIFI";
            case 2:
                return "MOBILE_MMS";
            case 3:
                return "MOBILE_SUPL";
            case 4:
                return "MOBILE_DUN";
            case 5:
                return "MOBILE_HIPRI";
            case 6:
                return "WIMAX";
            case 7:
                return "BLUETOOTH";
            case 8:
                return "DUMMY";
            case 9:
                return "ETHERNET";
            case 10:
                return "MOBILE_FOTA";
            case 11:
                return "MOBILE_IMS";
            case 12:
                return "MOBILE_CBS";
            case 13:
                return "WIFI_P2P";
            case 14:
                return "MOBILE_IA";
            case 15:
                return "MOBILE_EMERGENCY";
            case 16:
                return "PROXY";
            default:
                return Integer.toString(type);
        }
    }

    public static boolean isNetworkTypeMobile(int networkType) {
        switch (networkType) {
            case 0:
            case 2:
            case 3:
            case 4:
            case 5:
            case 10:
            case 11:
            case 12:
            case 14:
            case 15:
                return true;
            case 1:
            case 6:
            case 7:
            case 8:
            case 9:
            case 13:
            default:
                return false;
        }
    }

    public static boolean isNetworkTypeWifi(int networkType) {
        switch (networkType) {
            case 1:
            case 13:
                return true;
            default:
                return false;
        }
    }

    public void setNetworkPreference(int preference) {
    }

    public int getNetworkPreference() {
        return -1;
    }

    public NetworkInfo getActiveNetworkInfo() {
        try {
            return this.mService.getActiveNetworkInfo();
        } catch (RemoteException e) {
            return null;
        }
    }

    public NetworkInfo getActiveNetworkInfoForUid(int uid) {
        try {
            return this.mService.getActiveNetworkInfoForUid(uid);
        } catch (RemoteException e) {
            return null;
        }
    }

    public NetworkInfo getNetworkInfo(int networkType) {
        try {
            return this.mService.getNetworkInfo(networkType);
        } catch (RemoteException e) {
            return null;
        }
    }

    public NetworkInfo getNetworkInfo(Network network) {
        try {
            return this.mService.getNetworkInfoForNetwork(network);
        } catch (RemoteException e) {
            return null;
        }
    }

    public NetworkInfo[] getAllNetworkInfo() {
        try {
            return this.mService.getAllNetworkInfo();
        } catch (RemoteException e) {
            return null;
        }
    }

    public Network getNetworkForType(int networkType) {
        try {
            return this.mService.getNetworkForType(networkType);
        } catch (RemoteException e) {
            return null;
        }
    }

    public Network[] getAllNetworks() {
        try {
            return this.mService.getAllNetworks();
        } catch (RemoteException e) {
            return null;
        }
    }

    public NetworkInfo getProvisioningOrActiveNetworkInfo() {
        try {
            return this.mService.getProvisioningOrActiveNetworkInfo();
        } catch (RemoteException e) {
            return null;
        }
    }

    public LinkProperties getActiveLinkProperties() {
        try {
            return this.mService.getActiveLinkProperties();
        } catch (RemoteException e) {
            return null;
        }
    }

    public LinkProperties getLinkProperties(int networkType) {
        try {
            return this.mService.getLinkPropertiesForType(networkType);
        } catch (RemoteException e) {
            return null;
        }
    }

    public LinkProperties getLinkProperties(Network network) {
        try {
            return this.mService.getLinkProperties(network);
        } catch (RemoteException e) {
            return null;
        }
    }

    public NetworkCapabilities getNetworkCapabilities(Network network) {
        try {
            return this.mService.getNetworkCapabilities(network);
        } catch (RemoteException e) {
            return null;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:24:0x00e2, code lost:
        if (r4 == null) goto L_0x0100;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x00e4, code lost:
        android.util.Log.d(android.net.ConnectivityManager.TAG, "starting startUsingNetworkFeature for request " + r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0100, code lost:
        android.util.Log.d(android.net.ConnectivityManager.TAG, " request Failed");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:?, code lost:
        return 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:?, code lost:
        return 3;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int startUsingNetworkFeature(int r12, java.lang.String r13) {
        /*
        // Method dump skipped, instructions count: 265
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.ConnectivityManager.startUsingNetworkFeature(int, java.lang.String):int");
    }

    public int stopUsingNetworkFeature(int networkType, String feature) {
        NetworkCapabilities netCap = networkCapabilitiesForFeature(networkType, feature);
        if (netCap == null) {
            Log.d(TAG, "Can't satisfy stopUsingNetworkFeature for " + networkType + ", " + feature);
            return -1;
        }
        NetworkCallback networkCallback = removeRequestForFeature(netCap);
        if (networkCallback != null) {
            Log.d(TAG, "stopUsingNetworkFeature for " + networkType + ", " + feature);
            unregisterNetworkCallback(networkCallback);
        }
        return 1;
    }

    public static void maybeMarkCapabilitiesRestricted(NetworkCapabilities nc) {
        for (int capability : nc.getCapabilities()) {
            switch (capability) {
                case 2:
                case 3:
                case 4:
                case 5:
                case 7:
                case 8:
                case 9:
                case 10:
                case 13:
                case 6:
                case 11:
                case 12:
                default:
                    return;
            }
        }
        nc.removeCapability(13);
    }

    private NetworkCapabilities networkCapabilitiesForFeature(int networkType, String feature) {
        int cap;
        if (networkType == 0) {
            if ("enableMMS".equals(feature)) {
                cap = 0;
            } else if ("enableSUPL".equals(feature)) {
                cap = 1;
            } else if ("enableDUN".equals(feature) || "enableDUNAlways".equals(feature)) {
                cap = 2;
            } else if ("enableHIPRI".equals(feature)) {
                cap = 12;
            } else if ("enableFOTA".equals(feature)) {
                cap = 3;
            } else if ("enableIMS".equals(feature)) {
                cap = 4;
            } else if (!"enableCBS".equals(feature)) {
                return null;
            } else {
                cap = 5;
            }
            NetworkCapabilities netCap = new NetworkCapabilities();
            netCap.addTransportType(0).addCapability(cap);
            maybeMarkCapabilitiesRestricted(netCap);
            return netCap;
        } else if (networkType != 1 || !"p2p".equals(feature)) {
            return null;
        } else {
            NetworkCapabilities netCap2 = new NetworkCapabilities();
            netCap2.addTransportType(1);
            netCap2.addCapability(6);
            maybeMarkCapabilitiesRestricted(netCap2);
            return netCap2;
        }
    }

    private int inferLegacyTypeForNetworkCapabilities(NetworkCapabilities netCap) {
        if (netCap == null || !netCap.hasTransport(0)) {
            return -1;
        }
        if (netCap.hasCapability(5)) {
            if (netCap.equals(networkCapabilitiesForFeature(0, "enableCBS"))) {
                return 12;
            }
            return -1;
        } else if (netCap.hasCapability(4)) {
            if (netCap.equals(networkCapabilitiesForFeature(0, "enableIMS"))) {
                return 11;
            }
            return -1;
        } else if (netCap.hasCapability(3)) {
            if (netCap.equals(networkCapabilitiesForFeature(0, "enableFOTA"))) {
                return 10;
            }
            return -1;
        } else if (netCap.hasCapability(2)) {
            return netCap.equals(networkCapabilitiesForFeature(0, "enableDUN")) ? 4 : -1;
        } else {
            if (netCap.hasCapability(1)) {
                return netCap.equals(networkCapabilitiesForFeature(0, "enableSUPL")) ? 3 : -1;
            }
            if (netCap.hasCapability(0)) {
                return netCap.equals(networkCapabilitiesForFeature(0, "enableMMS")) ? 2 : -1;
            }
            if (!netCap.hasCapability(12) || !netCap.equals(networkCapabilitiesForFeature(0, "enableHIPRI"))) {
                return -1;
            }
            return 5;
        }
    }

    private int legacyTypeForNetworkCapabilities(NetworkCapabilities netCap) {
        if (netCap == null) {
            return -1;
        }
        if (netCap.hasCapability(5)) {
            return 12;
        }
        if (netCap.hasCapability(4)) {
            return 11;
        }
        if (netCap.hasCapability(3)) {
            return 10;
        }
        if (netCap.hasCapability(2)) {
            return 4;
        }
        if (netCap.hasCapability(1)) {
            return 3;
        }
        if (netCap.hasCapability(0)) {
            return 2;
        }
        if (netCap.hasCapability(12)) {
            return 5;
        }
        if (netCap.hasCapability(6)) {
            return 13;
        }
        return -1;
    }

    /* access modifiers changed from: private */
    public static class LegacyRequest {
        Network currentNetwork;
        int delay;
        int expireSequenceNumber;
        NetworkCallback networkCallback;
        NetworkCapabilities networkCapabilities;
        NetworkRequest networkRequest;

        private LegacyRequest() {
            this.delay = -1;
            this.networkCallback = new NetworkCallback() {
                /* class android.net.ConnectivityManager.LegacyRequest.AnonymousClass1 */

                @Override // android.net.ConnectivityManager.NetworkCallback
                public void onAvailable(Network network) {
                    LegacyRequest.this.currentNetwork = network;
                    Log.d(ConnectivityManager.TAG, "startUsingNetworkFeature got Network:" + network);
                    ConnectivityManager.setProcessDefaultNetworkForHostResolution(network);
                }

                @Override // android.net.ConnectivityManager.NetworkCallback
                public void onLost(Network network) {
                    if (network.equals(LegacyRequest.this.currentNetwork)) {
                        LegacyRequest.this.currentNetwork = null;
                        ConnectivityManager.setProcessDefaultNetworkForHostResolution(null);
                    }
                    Log.d(ConnectivityManager.TAG, "startUsingNetworkFeature lost Network:" + network);
                }
            };
        }
    }

    private NetworkRequest findRequestForFeature(NetworkCapabilities netCap) {
        synchronized (sLegacyRequests) {
            LegacyRequest l = sLegacyRequests.get(netCap);
            if (l == null) {
                return null;
            }
            return l.networkRequest;
        }
    }

    private void renewRequestLocked(LegacyRequest l) {
        l.expireSequenceNumber++;
        Log.d(TAG, "renewing request to seqNum " + l.expireSequenceNumber);
        sendExpireMsgForFeature(l.networkCapabilities, l.expireSequenceNumber, l.delay);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void expireRequest(NetworkCapabilities netCap, int sequenceNum) {
        synchronized (sLegacyRequests) {
            LegacyRequest l = sLegacyRequests.get(netCap);
            if (l != null) {
                int ourSeqNum = l.expireSequenceNumber;
                if (l.expireSequenceNumber == sequenceNum) {
                    unregisterNetworkCallback(l.networkCallback);
                    sLegacyRequests.remove(netCap);
                }
                Log.d(TAG, "expireRequest with " + ourSeqNum + ", " + sequenceNum);
            }
        }
    }

    private NetworkRequest requestNetworkForFeatureLocked(NetworkCapabilities netCap) {
        int delay = -1;
        int type = legacyTypeForNetworkCapabilities(netCap);
        try {
            delay = this.mService.getRestoreDefaultNetworkDelay(type);
        } catch (RemoteException e) {
        }
        LegacyRequest l = new LegacyRequest();
        l.networkCapabilities = netCap;
        l.delay = delay;
        l.expireSequenceNumber = 0;
        l.networkRequest = sendRequestForNetwork(netCap, l.networkCallback, 0, 2, type);
        if (l.networkRequest == null) {
            return null;
        }
        sLegacyRequests.put(netCap, l);
        sendExpireMsgForFeature(netCap, l.expireSequenceNumber, delay);
        return l.networkRequest;
    }

    private void sendExpireMsgForFeature(NetworkCapabilities netCap, int seqNum, int delay) {
        if (delay >= 0) {
            Log.d(TAG, "sending expire msg with seqNum " + seqNum + " and delay " + delay);
            sCallbackHandler.sendMessageDelayed(sCallbackHandler.obtainMessage(EXPIRE_LEGACY_REQUEST, seqNum, 0, netCap), (long) delay);
        }
    }

    private NetworkCallback removeRequestForFeature(NetworkCapabilities netCap) {
        NetworkCallback networkCallback;
        synchronized (sLegacyRequests) {
            LegacyRequest l = sLegacyRequests.remove(netCap);
            if (l == null) {
                networkCallback = null;
            } else {
                networkCallback = l.networkCallback;
            }
        }
        return networkCallback;
    }

    public boolean requestRouteToHost(int networkType, int hostAddress) {
        return requestRouteToHostAddress(networkType, NetworkUtils.intToInetAddress(hostAddress));
    }

    public boolean requestRouteToHostAddress(int networkType, InetAddress hostAddress) {
        try {
            return this.mService.requestRouteToHostAddress(networkType, hostAddress.getAddress());
        } catch (RemoteException e) {
            return false;
        }
    }

    @Deprecated
    public boolean getBackgroundDataSetting() {
        return true;
    }

    @Deprecated
    public void setBackgroundDataSetting(boolean allowBackgroundData) {
    }

    public NetworkQuotaInfo getActiveNetworkQuotaInfo() {
        try {
            return this.mService.getActiveNetworkQuotaInfo();
        } catch (RemoteException e) {
            return null;
        }
    }

    public boolean getMobileDataEnabled() {
        IBinder b = ServiceManager.getService("phone");
        if (b != null) {
            try {
                return ITelephony.Stub.asInterface(b).getDataEnabled();
            } catch (RemoteException e) {
            }
        }
        return false;
    }

    private INetworkManagementService getNetworkManagementService() {
        INetworkManagementService iNetworkManagementService;
        synchronized (this) {
            if (this.mNMService != null) {
                iNetworkManagementService = this.mNMService;
            } else {
                this.mNMService = INetworkManagementService.Stub.asInterface(ServiceManager.getService(Context.NETWORKMANAGEMENT_SERVICE));
                iNetworkManagementService = this.mNMService;
            }
        }
        return iNetworkManagementService;
    }

    public void addDefaultNetworkActiveListener(final OnNetworkActiveListener l) {
        INetworkActivityListener rl = new INetworkActivityListener.Stub() {
            /* class android.net.ConnectivityManager.AnonymousClass1 */

            @Override // android.os.INetworkActivityListener
            public void onNetworkActive() throws RemoteException {
                l.onNetworkActive();
            }
        };
        try {
            getNetworkManagementService().registerNetworkActivityListener(rl);
            this.mNetworkActivityListeners.put(l, rl);
        } catch (RemoteException e) {
        }
    }

    public void removeDefaultNetworkActiveListener(OnNetworkActiveListener l) {
        INetworkActivityListener rl = this.mNetworkActivityListeners.get(l);
        if (rl == null) {
            throw new IllegalArgumentException("Listener not registered: " + l);
        }
        try {
            getNetworkManagementService().unregisterNetworkActivityListener(rl);
        } catch (RemoteException e) {
        }
    }

    public boolean isDefaultNetworkActive() {
        try {
            return getNetworkManagementService().isNetworkActive();
        } catch (RemoteException e) {
            return false;
        }
    }

    public ConnectivityManager(IConnectivityManager service) {
        this.mService = (IConnectivityManager) Preconditions.checkNotNull(service, "missing IConnectivityManager");
    }

    public static ConnectivityManager from(Context context) {
        return (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public static final void enforceTetherChangePermission(Context context) {
        if (context.getResources().getStringArray(17235989).length == 2) {
            context.enforceCallingOrSelfPermission(Manifest.permission.CONNECTIVITY_INTERNAL, "ConnectivityService");
        } else {
            context.enforceCallingOrSelfPermission(Manifest.permission.CHANGE_NETWORK_STATE, "ConnectivityService");
        }
    }

    public String[] getTetherableIfaces() {
        try {
            return this.mService.getTetherableIfaces();
        } catch (RemoteException e) {
            return new String[0];
        }
    }

    public String[] getTetheredIfaces() {
        try {
            return this.mService.getTetheredIfaces();
        } catch (RemoteException e) {
            return new String[0];
        }
    }

    public String[] getTetheringErroredIfaces() {
        try {
            return this.mService.getTetheringErroredIfaces();
        } catch (RemoteException e) {
            return new String[0];
        }
    }

    public String[] getTetheredDhcpRanges() {
        try {
            return this.mService.getTetheredDhcpRanges();
        } catch (RemoteException e) {
            return new String[0];
        }
    }

    public int tether(String iface) {
        try {
            return this.mService.tether(iface);
        } catch (RemoteException e) {
            return 2;
        }
    }

    public int untether(String iface) {
        try {
            return this.mService.untether(iface);
        } catch (RemoteException e) {
            return 2;
        }
    }

    public boolean isTetheringSupported() {
        try {
            return this.mService.isTetheringSupported();
        } catch (RemoteException e) {
            return false;
        }
    }

    public String[] getTetherableUsbRegexs() {
        try {
            return this.mService.getTetherableUsbRegexs();
        } catch (RemoteException e) {
            return new String[0];
        }
    }

    public String[] getTetherableWifiRegexs() {
        try {
            return this.mService.getTetherableWifiRegexs();
        } catch (RemoteException e) {
            return new String[0];
        }
    }

    public String[] getTetherableBluetoothRegexs() {
        try {
            return this.mService.getTetherableBluetoothRegexs();
        } catch (RemoteException e) {
            return new String[0];
        }
    }

    public int setUsbTethering(boolean enable) {
        try {
            return this.mService.setUsbTethering(enable);
        } catch (RemoteException e) {
            return 2;
        }
    }

    public int getLastTetherError(String iface) {
        try {
            return this.mService.getLastTetherError(iface);
        } catch (RemoteException e) {
            return 2;
        }
    }

    public void reportInetCondition(int networkType, int percentage) {
        try {
            this.mService.reportInetCondition(networkType, percentage);
        } catch (RemoteException e) {
        }
    }

    public void reportBadNetwork(Network network) {
        try {
            this.mService.reportBadNetwork(network);
        } catch (RemoteException e) {
        }
    }

    public void setGlobalProxy(ProxyInfo p) {
        try {
            this.mService.setGlobalProxy(p);
        } catch (RemoteException e) {
        }
    }

    public ProxyInfo getGlobalProxy() {
        try {
            return this.mService.getGlobalProxy();
        } catch (RemoteException e) {
            return null;
        }
    }

    public ProxyInfo getProxy() {
        try {
            return this.mService.getProxy();
        } catch (RemoteException e) {
            return null;
        }
    }

    public void setDataDependency(int networkType, boolean met) {
        try {
            this.mService.setDataDependency(networkType, met);
        } catch (RemoteException e) {
        }
    }

    public boolean isNetworkSupported(int networkType) {
        try {
            return this.mService.isNetworkSupported(networkType);
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean isActiveNetworkMetered() {
        try {
            return this.mService.isActiveNetworkMetered();
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean updateLockdownVpn() {
        try {
            return this.mService.updateLockdownVpn();
        } catch (RemoteException e) {
            return false;
        }
    }

    public void captivePortalCheckCompleted(NetworkInfo info, boolean isCaptivePortal) {
        try {
            this.mService.captivePortalCheckCompleted(info, isCaptivePortal);
        } catch (RemoteException e) {
        }
    }

    public void supplyMessenger(int networkType, Messenger messenger) {
        try {
            this.mService.supplyMessenger(networkType, messenger);
        } catch (RemoteException e) {
        }
    }

    public int checkMobileProvisioning(int suggestedTimeOutMs) {
        try {
            return this.mService.checkMobileProvisioning(suggestedTimeOutMs);
        } catch (RemoteException e) {
            return -1;
        }
    }

    public String getMobileProvisioningUrl() {
        try {
            return this.mService.getMobileProvisioningUrl();
        } catch (RemoteException e) {
            return null;
        }
    }

    public String getMobileRedirectedProvisioningUrl() {
        try {
            return this.mService.getMobileRedirectedProvisioningUrl();
        } catch (RemoteException e) {
            return null;
        }
    }

    public LinkQualityInfo getLinkQualityInfo(int networkType) {
        try {
            return this.mService.getLinkQualityInfo(networkType);
        } catch (RemoteException e) {
            return null;
        }
    }

    public LinkQualityInfo getActiveLinkQualityInfo() {
        try {
            return this.mService.getActiveLinkQualityInfo();
        } catch (RemoteException e) {
            return null;
        }
    }

    public LinkQualityInfo[] getAllLinkQualityInfo() {
        try {
            return this.mService.getAllLinkQualityInfo();
        } catch (RemoteException e) {
            return null;
        }
    }

    public void setProvisioningNotificationVisible(boolean visible, int networkType, String action) {
        try {
            this.mService.setProvisioningNotificationVisible(visible, networkType, action);
        } catch (RemoteException e) {
        }
    }

    public void setAirplaneMode(boolean enable) {
        try {
            this.mService.setAirplaneMode(enable);
        } catch (RemoteException e) {
        }
    }

    public void registerNetworkFactory(Messenger messenger, String name) {
        try {
            this.mService.registerNetworkFactory(messenger, name);
        } catch (RemoteException e) {
        }
    }

    public void unregisterNetworkFactory(Messenger messenger) {
        try {
            this.mService.unregisterNetworkFactory(messenger);
        } catch (RemoteException e) {
        }
    }

    public void registerNetworkAgent(Messenger messenger, NetworkInfo ni, LinkProperties lp, NetworkCapabilities nc, int score, NetworkMisc misc) {
        try {
            this.mService.registerNetworkAgent(messenger, ni, lp, nc, score, misc);
        } catch (RemoteException e) {
        }
    }

    public static class NetworkCallback {
        public static final int AVAILABLE = 2;
        public static final int CANCELED = 8;
        public static final int CAP_CHANGED = 6;
        public static final int LOSING = 3;
        public static final int LOST = 4;
        public static final int PRECHECK = 1;
        public static final int PROP_CHANGED = 7;
        public static final int UNAVAIL = 5;
        private NetworkRequest networkRequest;

        public void onPreCheck(Network network) {
        }

        public void onAvailable(Network network) {
        }

        public void onLosing(Network network, int maxMsToLive) {
        }

        public void onLost(Network network) {
        }

        public void onUnavailable() {
        }

        public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
        }

        public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
        }
    }

    /* access modifiers changed from: private */
    public class CallbackHandler extends Handler {
        private static final String TAG = "ConnectivityManager.CallbackHandler";
        private final HashMap<NetworkRequest, NetworkCallback> mCallbackMap;
        private final ConnectivityManager mCm;
        private final AtomicInteger mRefCount;

        CallbackHandler(Looper looper, HashMap<NetworkRequest, NetworkCallback> callbackMap, AtomicInteger refCount, ConnectivityManager cm) {
            super(looper);
            this.mCallbackMap = callbackMap;
            this.mRefCount = refCount;
            this.mCm = cm;
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            NetworkCallback callbacks;
            NetworkCallback callbacks2;
            Log.d(TAG, "CM callback handler got msg " + message.what);
            switch (message.what) {
                case ConnectivityManager.CALLBACK_PRECHECK /*{ENCODED_INT: 524289}*/:
                    NetworkCallback callbacks3 = getCallbacks((NetworkRequest) getObject(message, NetworkRequest.class));
                    if (callbacks3 != null) {
                        callbacks3.onPreCheck((Network) getObject(message, Network.class));
                        return;
                    } else {
                        Log.e(TAG, "callback not found for PRECHECK message");
                        return;
                    }
                case ConnectivityManager.CALLBACK_AVAILABLE /*{ENCODED_INT: 524290}*/:
                    NetworkCallback callbacks4 = getCallbacks((NetworkRequest) getObject(message, NetworkRequest.class));
                    if (callbacks4 != null) {
                        callbacks4.onAvailable((Network) getObject(message, Network.class));
                        return;
                    } else {
                        Log.e(TAG, "callback not found for AVAILABLE message");
                        return;
                    }
                case ConnectivityManager.CALLBACK_LOSING /*{ENCODED_INT: 524291}*/:
                    NetworkCallback callbacks5 = getCallbacks((NetworkRequest) getObject(message, NetworkRequest.class));
                    if (callbacks5 != null) {
                        callbacks5.onLosing((Network) getObject(message, Network.class), message.arg1);
                        return;
                    } else {
                        Log.e(TAG, "callback not found for LOSING message");
                        return;
                    }
                case ConnectivityManager.CALLBACK_LOST /*{ENCODED_INT: 524292}*/:
                    NetworkCallback callbacks6 = getCallbacks((NetworkRequest) getObject(message, NetworkRequest.class));
                    if (callbacks6 != null) {
                        callbacks6.onLost((Network) getObject(message, Network.class));
                        return;
                    } else {
                        Log.e(TAG, "callback not found for LOST message");
                        return;
                    }
                case ConnectivityManager.CALLBACK_UNAVAIL /*{ENCODED_INT: 524293}*/:
                    NetworkRequest request = (NetworkRequest) getObject(message, NetworkRequest.class);
                    synchronized (this.mCallbackMap) {
                        callbacks2 = this.mCallbackMap.get(request);
                    }
                    if (callbacks2 != null) {
                        callbacks2.onUnavailable();
                        return;
                    } else {
                        Log.e(TAG, "callback not found for UNAVAIL message");
                        return;
                    }
                case ConnectivityManager.CALLBACK_CAP_CHANGED /*{ENCODED_INT: 524294}*/:
                    NetworkCallback callbacks7 = getCallbacks((NetworkRequest) getObject(message, NetworkRequest.class));
                    if (callbacks7 != null) {
                        callbacks7.onCapabilitiesChanged((Network) getObject(message, Network.class), (NetworkCapabilities) getObject(message, NetworkCapabilities.class));
                        return;
                    } else {
                        Log.e(TAG, "callback not found for CAP_CHANGED message");
                        return;
                    }
                case ConnectivityManager.CALLBACK_IP_CHANGED /*{ENCODED_INT: 524295}*/:
                    NetworkCallback callbacks8 = getCallbacks((NetworkRequest) getObject(message, NetworkRequest.class));
                    if (callbacks8 != null) {
                        callbacks8.onLinkPropertiesChanged((Network) getObject(message, Network.class), (LinkProperties) getObject(message, LinkProperties.class));
                        return;
                    } else {
                        Log.e(TAG, "callback not found for IP_CHANGED message");
                        return;
                    }
                case ConnectivityManager.CALLBACK_RELEASED /*{ENCODED_INT: 524296}*/:
                    NetworkRequest req = (NetworkRequest) getObject(message, NetworkRequest.class);
                    synchronized (this.mCallbackMap) {
                        callbacks = this.mCallbackMap.remove(req);
                    }
                    if (callbacks != null) {
                        synchronized (this.mRefCount) {
                            if (this.mRefCount.decrementAndGet() == 0) {
                                getLooper().quit();
                            }
                        }
                        return;
                    }
                    Log.e(TAG, "callback not found for CANCELED message");
                    return;
                case ConnectivityManager.CALLBACK_EXIT /*{ENCODED_INT: 524297}*/:
                    Log.d(TAG, "Listener quiting");
                    getLooper().quit();
                    return;
                case ConnectivityManager.EXPIRE_LEGACY_REQUEST /*{ENCODED_INT: 524298}*/:
                    ConnectivityManager.this.expireRequest((NetworkCapabilities) message.obj, message.arg1);
                    return;
                default:
                    return;
            }
        }

        private Object getObject(Message msg, Class c) {
            return msg.getData().getParcelable(c.getSimpleName());
        }

        private NetworkCallback getCallbacks(NetworkRequest req) {
            NetworkCallback networkCallback;
            synchronized (this.mCallbackMap) {
                networkCallback = this.mCallbackMap.get(req);
            }
            return networkCallback;
        }
    }

    private void incCallbackHandlerRefCount() {
        synchronized (sCallbackRefCount) {
            if (sCallbackRefCount.incrementAndGet() == 1) {
                HandlerThread callbackThread = new HandlerThread(TAG);
                callbackThread.start();
                sCallbackHandler = new CallbackHandler(callbackThread.getLooper(), sNetworkCallback, sCallbackRefCount, this);
            }
        }
    }

    private void decCallbackHandlerRefCount() {
        synchronized (sCallbackRefCount) {
            if (sCallbackRefCount.decrementAndGet() == 0) {
                sCallbackHandler.obtainMessage(CALLBACK_EXIT).sendToTarget();
                sCallbackHandler = null;
            }
        }
    }

    private NetworkRequest sendRequestForNetwork(NetworkCapabilities need, NetworkCallback networkCallback, int timeoutSec, int action, int legacyType) {
        if (networkCallback == null) {
            throw new IllegalArgumentException("null NetworkCallback");
        } else if (need == null) {
            throw new IllegalArgumentException("null NetworkCapabilities");
        } else {
            try {
                incCallbackHandlerRefCount();
                synchronized (sNetworkCallback) {
                    if (action == 1) {
                        networkCallback.networkRequest = this.mService.listenForNetwork(need, new Messenger(sCallbackHandler), new Binder());
                    } else {
                        networkCallback.networkRequest = this.mService.requestNetwork(need, new Messenger(sCallbackHandler), timeoutSec, new Binder(), legacyType);
                    }
                    if (networkCallback.networkRequest != null) {
                        sNetworkCallback.put(networkCallback.networkRequest, networkCallback);
                    }
                }
            } catch (RemoteException e) {
            }
            if (networkCallback.networkRequest == null) {
                decCallbackHandlerRefCount();
            }
            return networkCallback.networkRequest;
        }
    }

    public void requestNetwork(NetworkRequest request, NetworkCallback networkCallback) {
        sendRequestForNetwork(request.networkCapabilities, networkCallback, 0, 2, inferLegacyTypeForNetworkCapabilities(request.networkCapabilities));
    }

    public void requestNetwork(NetworkRequest request, NetworkCallback networkCallback, int timeoutMs) {
        sendRequestForNetwork(request.networkCapabilities, networkCallback, timeoutMs, 2, inferLegacyTypeForNetworkCapabilities(request.networkCapabilities));
    }

    public void requestNetwork(NetworkRequest request, PendingIntent operation) {
        try {
            this.mService.pendingRequestForNetwork(request.networkCapabilities, operation);
        } catch (RemoteException e) {
        }
    }

    public void registerNetworkCallback(NetworkRequest request, NetworkCallback networkCallback) {
        sendRequestForNetwork(request.networkCapabilities, networkCallback, 0, 1, -1);
    }

    public void unregisterNetworkCallback(NetworkCallback networkCallback) {
        if (networkCallback == null || networkCallback.networkRequest == null || networkCallback.networkRequest.requestId == 0) {
            throw new IllegalArgumentException("Invalid NetworkCallback");
        }
        try {
            this.mService.releaseNetworkRequest(networkCallback.networkRequest);
        } catch (RemoteException e) {
        }
    }

    public static boolean setProcessDefaultNetwork(Network network) {
        int netId = network == null ? 0 : network.netId;
        if (netId == NetworkUtils.getNetworkBoundToProcess()) {
            return true;
        }
        if (!NetworkUtils.bindProcessToNetwork(netId)) {
            return false;
        }
        InetAddress.clearDnsCache();
        NetworkEventDispatcher.getInstance().onNetworkConfigurationChanged();
        return true;
    }

    public static Network getProcessDefaultNetwork() {
        int netId = NetworkUtils.getNetworkBoundToProcess();
        if (netId == 0) {
            return null;
        }
        return new Network(netId);
    }

    public static boolean setProcessDefaultNetworkForHostResolution(Network network) {
        return NetworkUtils.bindProcessToNetworkForHostResolution(network == null ? 0 : network.netId);
    }
}

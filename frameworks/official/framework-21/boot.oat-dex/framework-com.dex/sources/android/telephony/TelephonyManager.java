package android.telephony;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.ProxyInfo;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import com.android.internal.R;
import com.android.internal.telecom.ITelecomService;
import com.android.internal.telephony.IPhoneSubInfo;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.ITelephonyRegistry;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.TelephonyProperties;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TelephonyManager {
    public static final String ACTION_PHONE_STATE_CHANGED = "android.intent.action.PHONE_STATE";
    public static final String ACTION_PRECISE_CALL_STATE_CHANGED = "android.intent.action.PRECISE_CALL_STATE";
    public static final String ACTION_PRECISE_DATA_CONNECTION_STATE_CHANGED = "android.intent.action.PRECISE_DATA_CONNECTION_STATE_CHANGED";
    public static final String ACTION_RESPOND_VIA_MESSAGE = "android.intent.action.RESPOND_VIA_MESSAGE";
    public static final int CALL_STATE_IDLE = 0;
    public static final int CALL_STATE_OFFHOOK = 2;
    public static final int CALL_STATE_RINGING = 1;
    public static final int CARRIER_PRIVILEGE_STATUS_ERROR_LOADING_RULES = -2;
    public static final int CARRIER_PRIVILEGE_STATUS_HAS_ACCESS = 1;
    public static final int CARRIER_PRIVILEGE_STATUS_NO_ACCESS = 0;
    public static final int CARRIER_PRIVILEGE_STATUS_RULES_NOT_LOADED = -1;
    public static final int DATA_ACTIVITY_DORMANT = 4;
    public static final int DATA_ACTIVITY_IN = 1;
    public static final int DATA_ACTIVITY_INOUT = 3;
    public static final int DATA_ACTIVITY_NONE = 0;
    public static final int DATA_ACTIVITY_OUT = 2;
    public static final int DATA_CONNECTED = 2;
    public static final int DATA_CONNECTING = 1;
    public static final int DATA_DISCONNECTED = 0;
    public static final int DATA_SUSPENDED = 3;
    public static final int DATA_UNKNOWN = -1;
    public static final String EXTRA_BACKGROUND_CALL_STATE = "background_state";
    public static final String EXTRA_DATA_APN = "apn";
    public static final String EXTRA_DATA_APN_TYPE = "apnType";
    public static final String EXTRA_DATA_CHANGE_REASON = "reason";
    public static final String EXTRA_DATA_FAILURE_CAUSE = "failCause";
    public static final String EXTRA_DATA_LINK_PROPERTIES_KEY = "linkProperties";
    public static final String EXTRA_DATA_NETWORK_TYPE = "networkType";
    public static final String EXTRA_DATA_STATE = "state";
    public static final String EXTRA_DISCONNECT_CAUSE = "disconnect_cause";
    public static final String EXTRA_FOREGROUND_CALL_STATE = "foreground_state";
    public static final String EXTRA_INCOMING_NUMBER = "incoming_number";
    public static final String EXTRA_PRECISE_DISCONNECT_CAUSE = "precise_disconnect_cause";
    public static final String EXTRA_RINGING_CALL_STATE = "ringing_state";
    public static final String EXTRA_STATE = "state";
    public static final String EXTRA_STATE_IDLE = PhoneConstants.State.IDLE.toString();
    public static final String EXTRA_STATE_OFFHOOK = PhoneConstants.State.OFFHOOK.toString();
    public static final String EXTRA_STATE_RINGING = PhoneConstants.State.RINGING.toString();
    public static final int NETWORK_CLASS_2_G = 1;
    public static final int NETWORK_CLASS_3_G = 2;
    public static final int NETWORK_CLASS_4_G = 3;
    public static final int NETWORK_CLASS_UNKNOWN = 0;
    public static final int NETWORK_TYPE_1xRTT = 7;
    public static final int NETWORK_TYPE_CDMA = 4;
    public static final int NETWORK_TYPE_EDGE = 2;
    public static final int NETWORK_TYPE_EHRPD = 14;
    public static final int NETWORK_TYPE_EVDO_0 = 5;
    public static final int NETWORK_TYPE_EVDO_A = 6;
    public static final int NETWORK_TYPE_EVDO_B = 12;
    public static final int NETWORK_TYPE_GPRS = 1;
    public static final int NETWORK_TYPE_GSM = 16;
    public static final int NETWORK_TYPE_HSDPA = 8;
    public static final int NETWORK_TYPE_HSPA = 10;
    public static final int NETWORK_TYPE_HSPAP = 15;
    public static final int NETWORK_TYPE_HSUPA = 9;
    public static final int NETWORK_TYPE_IDEN = 11;
    public static final int NETWORK_TYPE_LTE = 13;
    public static final int NETWORK_TYPE_UMTS = 3;
    public static final int NETWORK_TYPE_UNKNOWN = 0;
    public static final int PHONE_TYPE_CDMA = 2;
    public static final int PHONE_TYPE_GSM = 1;
    public static final int PHONE_TYPE_NONE = 0;
    public static final int PHONE_TYPE_SIP = 3;
    public static final int SIM_STATE_ABSENT = 1;
    public static final int SIM_STATE_CARD_IO_ERROR = 6;
    public static final int SIM_STATE_NETWORK_LOCKED = 4;
    public static final int SIM_STATE_PIN_REQUIRED = 2;
    public static final int SIM_STATE_PUK_REQUIRED = 3;
    public static final int SIM_STATE_READY = 5;
    public static final int SIM_STATE_UNKNOWN = 0;
    private static final String TAG = "TelephonyManager";
    private static String multiSimConfig = SystemProperties.get(TelephonyProperties.PROPERTY_MULTI_SIM_CONFIG);
    private static TelephonyManager sInstance = new TelephonyManager();
    private static final String sKernelCmdLine = getProcCmdLine();
    private static final String sLteOnCdmaProductType = SystemProperties.get(TelephonyProperties.PROPERTY_LTE_ON_CDMA_PRODUCT_TYPE, ProxyInfo.LOCAL_EXCL_LIST);
    private static final Pattern sProductTypePattern = Pattern.compile("\\sproduct_type\\s*=\\s*(\\w+)");
    private static ITelephonyRegistry sRegistry;
    private final Context mContext;

    public enum MultiSimVariants {
        DSDS,
        DSDA,
        TSTS,
        UNKNOWN
    }

    public interface WifiCallingChoices {
        public static final int ALWAYS_USE = 0;
        public static final int ASK_EVERY_TIME = 1;
        public static final int NEVER_USE = 2;
    }

    public TelephonyManager(Context context) {
        Context appContext = context.getApplicationContext();
        if (appContext != null) {
            this.mContext = appContext;
        } else {
            this.mContext = context;
        }
        if (sRegistry == null) {
            sRegistry = ITelephonyRegistry.Stub.asInterface(ServiceManager.getService("telephony.registry"));
        }
    }

    private TelephonyManager() {
        this.mContext = null;
    }

    public static TelephonyManager getDefault() {
        return sInstance;
    }

    public MultiSimVariants getMultiSimConfiguration() {
        String mSimConfig = SystemProperties.get(TelephonyProperties.PROPERTY_MULTI_SIM_CONFIG);
        if (mSimConfig.equals("dsds")) {
            return MultiSimVariants.DSDS;
        }
        if (mSimConfig.equals("dsda")) {
            return MultiSimVariants.DSDA;
        }
        if (mSimConfig.equals("tsts")) {
            return MultiSimVariants.TSTS;
        }
        return MultiSimVariants.UNKNOWN;
    }

    /* renamed from: android.telephony.TelephonyManager$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$android$telephony$TelephonyManager$MultiSimVariants = new int[MultiSimVariants.values().length];

        static {
            try {
                $SwitchMap$android$telephony$TelephonyManager$MultiSimVariants[MultiSimVariants.DSDS.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$android$telephony$TelephonyManager$MultiSimVariants[MultiSimVariants.DSDA.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$android$telephony$TelephonyManager$MultiSimVariants[MultiSimVariants.TSTS.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }

    public int getPhoneCount() {
        switch (AnonymousClass1.$SwitchMap$android$telephony$TelephonyManager$MultiSimVariants[getMultiSimConfiguration().ordinal()]) {
            case 1:
            case 2:
                return 2;
            case 3:
                return 3;
            default:
                return 1;
        }
    }

    public static TelephonyManager from(Context context) {
        return (TelephonyManager) context.getSystemService("phone");
    }

    public boolean isMultiSimEnabled() {
        return multiSimConfig.equals("dsds") || multiSimConfig.equals("dsda") || multiSimConfig.equals("tsts");
    }

    public String getDeviceSoftwareVersion() {
        try {
            return getSubscriberInfo().getDeviceSvn();
        } catch (RemoteException | NullPointerException e) {
            return null;
        }
    }

    public String getDeviceId() {
        return getDeviceId(getDefaultSim());
    }

    public String getDeviceId(int slotId) {
        try {
            return getSubscriberInfo().getDeviceIdForSubscriber(SubscriptionManager.getSubId(slotId)[0]);
        } catch (RemoteException | NullPointerException e) {
            return null;
        }
    }

    public String getImei() {
        return getImei(getDefaultSim());
    }

    public String getImei(int slotId) {
        try {
            return getSubscriberInfo().getImeiForSubscriber(SubscriptionManager.getSubId(slotId)[0]);
        } catch (RemoteException | NullPointerException e) {
            return null;
        }
    }

    public CellLocation getCellLocation() {
        try {
            Bundle bundle = getITelephony().getCellLocation();
            if (bundle.isEmpty()) {
                return null;
            }
            CellLocation cl = CellLocation.newFromBundle(bundle);
            if (cl.isEmpty()) {
                return null;
            }
            return cl;
        } catch (RemoteException e) {
            return null;
        } catch (NullPointerException e2) {
            return null;
        }
    }

    public void enableLocationUpdates() {
        enableLocationUpdates(getDefaultSubscription());
    }

    public void enableLocationUpdates(long subId) {
        try {
            getITelephony().enableLocationUpdatesForSubscriber(subId);
        } catch (RemoteException | NullPointerException e) {
        }
    }

    public void disableLocationUpdates() {
        disableLocationUpdates(getDefaultSubscription());
    }

    public void disableLocationUpdates(long subId) {
        try {
            getITelephony().disableLocationUpdatesForSubscriber(subId);
        } catch (RemoteException | NullPointerException e) {
        }
    }

    public List<NeighboringCellInfo> getNeighboringCellInfo() {
        try {
            return getITelephony().getNeighboringCellInfo(this.mContext.getOpPackageName());
        } catch (RemoteException | NullPointerException e) {
            return null;
        }
    }

    public int getCurrentPhoneType() {
        return getCurrentPhoneType(getDefaultSubscription());
    }

    public int getCurrentPhoneType(long subId) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.getActivePhoneTypeForSubscriber(subId);
            }
            return getPhoneTypeFromProperty(subId);
        } catch (RemoteException e) {
            return getPhoneTypeFromProperty(subId);
        } catch (NullPointerException e2) {
            return getPhoneTypeFromProperty(subId);
        }
    }

    public int getPhoneType() {
        if (!isVoiceCapable()) {
            return 0;
        }
        return getCurrentPhoneType();
    }

    private int getPhoneTypeFromProperty() {
        return getPhoneTypeFromProperty(getDefaultSubscription());
    }

    private int getPhoneTypeFromProperty(long subId) {
        String type = getTelephonyProperty(TelephonyProperties.CURRENT_ACTIVE_PHONE, subId, null);
        if (type != null) {
            return Integer.parseInt(type);
        }
        return getPhoneTypeFromNetworkType(subId);
    }

    private int getPhoneTypeFromNetworkType() {
        return getPhoneTypeFromNetworkType(getDefaultSubscription());
    }

    private int getPhoneTypeFromNetworkType(long subId) {
        String mode = getTelephonyProperty("ro.telephony.default_network", subId, null);
        if (mode != null) {
            return getPhoneType(Integer.parseInt(mode));
        }
        return 0;
    }

    public static int getPhoneType(int networkMode) {
        switch (networkMode) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 9:
            case 10:
            case 12:
                return 1;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
                return 2;
            case 11:
                return getLteOnCdmaModeStatic() != 1 ? 1 : 2;
            default:
                return 1;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x0058 A[SYNTHETIC, Splitter:B:22:0x0058] */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0061 A[SYNTHETIC, Splitter:B:27:0x0061] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static java.lang.String getProcCmdLine() {
        /*
        // Method dump skipped, instructions count: 111
        */
        throw new UnsupportedOperationException("Method not decompiled: android.telephony.TelephonyManager.getProcCmdLine():java.lang.String");
    }

    public static int getLteOnCdmaModeStatic() {
        String productType = ProxyInfo.LOCAL_EXCL_LIST;
        int curVal = SystemProperties.getInt(TelephonyProperties.PROPERTY_LTE_ON_CDMA_DEVICE, -1);
        int retVal = curVal;
        if (retVal == -1) {
            Matcher matcher = sProductTypePattern.matcher(sKernelCmdLine);
            if (matcher.find()) {
                productType = matcher.group(1);
                if (sLteOnCdmaProductType.equals(productType)) {
                    retVal = 1;
                } else {
                    retVal = 0;
                }
            } else {
                retVal = 0;
            }
        }
        Rlog.d(TAG, "getLteOnCdmaMode=" + retVal + " curVal=" + curVal + " product_type='" + productType + "' lteOnCdmaProductType='" + sLteOnCdmaProductType + "'");
        return retVal;
    }

    public String getNetworkOperatorName() {
        return getNetworkOperatorName(getDefaultSubscription());
    }

    public String getNetworkOperatorName(long subId) {
        return getTelephonyProperty(TelephonyProperties.PROPERTY_OPERATOR_ALPHA, subId, ProxyInfo.LOCAL_EXCL_LIST);
    }

    public String getNetworkOperator() {
        return getNetworkOperator(getDefaultSubscription());
    }

    public String getNetworkOperator(long subId) {
        return getTelephonyProperty(TelephonyProperties.PROPERTY_OPERATOR_NUMERIC, subId, ProxyInfo.LOCAL_EXCL_LIST);
    }

    public boolean isNetworkRoaming() {
        return isNetworkRoaming(getDefaultSubscription());
    }

    public boolean isNetworkRoaming(long subId) {
        return "true".equals(getTelephonyProperty(TelephonyProperties.PROPERTY_OPERATOR_ISROAMING, subId, null));
    }

    public String getNetworkCountryIso() {
        return getNetworkCountryIso(getDefaultSubscription());
    }

    public String getNetworkCountryIso(long subId) {
        return getTelephonyProperty(TelephonyProperties.PROPERTY_OPERATOR_ISO_COUNTRY, subId, ProxyInfo.LOCAL_EXCL_LIST);
    }

    public int getNetworkType() {
        return getDataNetworkType();
    }

    public int getNetworkType(long subId) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.getNetworkTypeForSubscriber(subId);
            }
            return 0;
        } catch (RemoteException | NullPointerException e) {
            return 0;
        }
    }

    public int getDataNetworkType() {
        return getDataNetworkType(getDefaultSubscription());
    }

    public int getDataNetworkType(long subId) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.getDataNetworkTypeForSubscriber(subId);
            }
            return 0;
        } catch (RemoteException | NullPointerException e) {
            return 0;
        }
    }

    public int getVoiceNetworkType() {
        return getVoiceNetworkType(getDefaultSubscription());
    }

    public int getVoiceNetworkType(long subId) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.getVoiceNetworkTypeForSubscriber(subId);
            }
            return 0;
        } catch (RemoteException | NullPointerException e) {
            return 0;
        }
    }

    public static int getNetworkClass(int networkType) {
        switch (networkType) {
            case 1:
            case 2:
            case 4:
            case 7:
            case 11:
            case 16:
                return 1;
            case 3:
            case 5:
            case 6:
            case 8:
            case 9:
            case 10:
            case 12:
            case 14:
            case 15:
                return 2;
            case 13:
                return 3;
            default:
                return 0;
        }
    }

    public String getNetworkTypeName() {
        return getNetworkTypeName(getNetworkType());
    }

    public static String getNetworkTypeName(int type) {
        switch (type) {
            case 1:
                return "GPRS";
            case 2:
                return "EDGE";
            case 3:
                return "UMTS";
            case 4:
                return "CDMA";
            case 5:
                return "CDMA - EvDo rev. 0";
            case 6:
                return "CDMA - EvDo rev. A";
            case 7:
                return "CDMA - 1xRTT";
            case 8:
                return "HSDPA";
            case 9:
                return "HSUPA";
            case 10:
                return "HSPA";
            case 11:
                return "iDEN";
            case 12:
                return "CDMA - EvDo rev. B";
            case 13:
                return "LTE";
            case 14:
                return "CDMA - eHRPD";
            case 15:
                return "HSPA+";
            case 16:
                return "GSM";
            default:
                return IccCardConstants.INTENT_VALUE_ICC_UNKNOWN;
        }
    }

    public boolean hasIccCard() {
        return hasIccCard((long) getDefaultSim());
    }

    public boolean hasIccCard(long slotId) {
        try {
            return getITelephony().hasIccCardUsingSlotId(slotId);
        } catch (RemoteException | NullPointerException e) {
            return false;
        }
    }

    public int getSimState() {
        return getSimState(getDefaultSim());
    }

    public int getSimState(int slotId) {
        long[] subId = SubscriptionManager.getSubId(slotId);
        if (subId == null) {
            return 1;
        }
        String prop = getTelephonyProperty(TelephonyProperties.PROPERTY_SIM_STATE, subId[0], ProxyInfo.LOCAL_EXCL_LIST);
        if (IccCardConstants.INTENT_VALUE_ICC_ABSENT.equals(prop)) {
            return 1;
        }
        if ("PIN_REQUIRED".equals(prop)) {
            return 2;
        }
        if ("PUK_REQUIRED".equals(prop)) {
            return 3;
        }
        if ("NETWORK_LOCKED".equals(prop)) {
            return 4;
        }
        if (IccCardConstants.INTENT_VALUE_ICC_READY.equals(prop)) {
            return 5;
        }
        if (IccCardConstants.INTENT_VALUE_ICC_CARD_IO_ERROR.equals(prop)) {
            return 6;
        }
        return 0;
    }

    public String getSimOperator() {
        long subId = getDefaultSubscription();
        Rlog.d(TAG, "getSimOperator(): default subId=" + subId);
        return getSimOperator(subId);
    }

    public String getSimOperator(long subId) {
        String operator = getTelephonyProperty(TelephonyProperties.PROPERTY_ICC_OPERATOR_NUMERIC, subId, ProxyInfo.LOCAL_EXCL_LIST);
        Rlog.d(TAG, "getSimOperator: subId=" + subId + " operator=" + operator);
        return operator;
    }

    public String getSimOperatorName() {
        return getSimOperatorName(getDefaultSubscription());
    }

    public String getSimOperatorName(long subId) {
        return getTelephonyProperty(TelephonyProperties.PROPERTY_ICC_OPERATOR_ALPHA, subId, ProxyInfo.LOCAL_EXCL_LIST);
    }

    public String getSimCountryIso() {
        return getSimCountryIso(getDefaultSubscription());
    }

    public String getSimCountryIso(long subId) {
        return getTelephonyProperty(TelephonyProperties.PROPERTY_ICC_OPERATOR_ISO_COUNTRY, subId, ProxyInfo.LOCAL_EXCL_LIST);
    }

    public String getSimSerialNumber() {
        return getSimSerialNumber(getDefaultSubscription());
    }

    public String getSimSerialNumber(long subId) {
        try {
            return getSubscriberInfo().getIccSerialNumberForSubscriber(subId);
        } catch (RemoteException | NullPointerException e) {
            return null;
        }
    }

    public int getLteOnCdmaMode() {
        return getLteOnCdmaMode(getDefaultSubscription());
    }

    public int getLteOnCdmaMode(long subId) {
        try {
            return getITelephony().getLteOnCdmaModeForSubscriber(subId);
        } catch (RemoteException | NullPointerException e) {
            return -1;
        }
    }

    public String getSubscriberId() {
        return getSubscriberId(getDefaultSubscription());
    }

    public String getSubscriberId(long subId) {
        try {
            return getSubscriberInfo().getSubscriberIdForSubscriber(subId);
        } catch (RemoteException | NullPointerException e) {
            return null;
        }
    }

    public String getGroupIdLevel1() {
        try {
            return getSubscriberInfo().getGroupIdLevel1();
        } catch (RemoteException | NullPointerException e) {
            return null;
        }
    }

    public String getGroupIdLevel1(long subId) {
        try {
            return getSubscriberInfo().getGroupIdLevel1ForSubscriber(subId);
        } catch (RemoteException | NullPointerException e) {
            return null;
        }
    }

    public String getLine1Number() {
        return getLine1NumberForSubscriber(getDefaultSubscription());
    }

    public String getLine1NumberForSubscriber(long subId) {
        String number = null;
        try {
            number = getITelephony().getLine1NumberForDisplay(subId);
        } catch (RemoteException | NullPointerException e) {
        }
        if (number != null) {
            return number;
        }
        try {
            return getSubscriberInfo().getLine1NumberForSubscriber(subId);
        } catch (RemoteException e2) {
            return null;
        } catch (NullPointerException e3) {
            return null;
        }
    }

    public void setLine1NumberForDisplay(String alphaTag, String number) {
        setLine1NumberForDisplayForSubscriber(getDefaultSubscription(), alphaTag, number);
    }

    public void setLine1NumberForDisplayForSubscriber(long subId, String alphaTag, String number) {
        try {
            getITelephony().setLine1NumberForDisplayForSubscriber(subId, alphaTag, number);
        } catch (RemoteException | NullPointerException e) {
        }
    }

    public String getLine1AlphaTag() {
        return getLine1AlphaTagForSubscriber(getDefaultSubscription());
    }

    public String getLine1AlphaTagForSubscriber(long subId) {
        String alphaTag = null;
        try {
            alphaTag = getITelephony().getLine1AlphaTagForDisplay(subId);
        } catch (RemoteException | NullPointerException e) {
        }
        if (alphaTag != null) {
            return alphaTag;
        }
        try {
            return getSubscriberInfo().getLine1AlphaTagForSubscriber(subId);
        } catch (RemoteException e2) {
            return null;
        } catch (NullPointerException e3) {
            return null;
        }
    }

    public String getMsisdn() {
        return getMsisdn(getDefaultSubscription());
    }

    public String getMsisdn(long subId) {
        try {
            return getSubscriberInfo().getMsisdnForSubscriber(subId);
        } catch (RemoteException | NullPointerException e) {
            return null;
        }
    }

    public String getVoiceMailNumber() {
        return getVoiceMailNumber(getDefaultSubscription());
    }

    public String getVoiceMailNumber(long subId) {
        try {
            return getSubscriberInfo().getVoiceMailNumberForSubscriber(subId);
        } catch (RemoteException | NullPointerException e) {
            return null;
        }
    }

    public String getCompleteVoiceMailNumber() {
        return getCompleteVoiceMailNumber(getDefaultSubscription());
    }

    public String getCompleteVoiceMailNumber(long subId) {
        try {
            return getSubscriberInfo().getCompleteVoiceMailNumberForSubscriber(subId);
        } catch (RemoteException | NullPointerException e) {
            return null;
        }
    }

    public int getVoiceMessageCount() {
        return getVoiceMessageCount(getDefaultSubscription());
    }

    public int getVoiceMessageCount(long subId) {
        try {
            return getITelephony().getVoiceMessageCountForSubscriber(subId);
        } catch (RemoteException | NullPointerException e) {
            return 0;
        }
    }

    public String getVoiceMailAlphaTag() {
        return getVoiceMailAlphaTag(getDefaultSubscription());
    }

    public String getVoiceMailAlphaTag(long subId) {
        try {
            return getSubscriberInfo().getVoiceMailAlphaTagForSubscriber(subId);
        } catch (RemoteException | NullPointerException e) {
            return null;
        }
    }

    public String getIsimImpi() {
        try {
            return getSubscriberInfo().getIsimImpi();
        } catch (RemoteException | NullPointerException e) {
            return null;
        }
    }

    public String getIsimDomain() {
        try {
            return getSubscriberInfo().getIsimDomain();
        } catch (RemoteException | NullPointerException e) {
            return null;
        }
    }

    public String[] getIsimImpu() {
        try {
            return getSubscriberInfo().getIsimImpu();
        } catch (RemoteException | NullPointerException e) {
            return null;
        }
    }

    private IPhoneSubInfo getSubscriberInfo() {
        return IPhoneSubInfo.Stub.asInterface(ServiceManager.getService("iphonesubinfo"));
    }

    public int getCallState() {
        try {
            return getTelecomService().getCallState();
        } catch (RemoteException | NullPointerException e) {
            return 0;
        }
    }

    public int getCallState(long subId) {
        try {
            return getITelephony().getCallStateForSubscriber(subId);
        } catch (RemoteException | NullPointerException e) {
            return 0;
        }
    }

    public int getDataActivity() {
        try {
            return getITelephony().getDataActivity();
        } catch (RemoteException | NullPointerException e) {
            return 0;
        }
    }

    public int getDataState() {
        try {
            return getITelephony().getDataState();
        } catch (RemoteException | NullPointerException e) {
            return 0;
        }
    }

    private ITelephony getITelephony() {
        return ITelephony.Stub.asInterface(ServiceManager.getService("phone"));
    }

    private ITelecomService getTelecomService() {
        return ITelecomService.Stub.asInterface(ServiceManager.getService(Context.TELECOM_SERVICE));
    }

    public void listen(PhoneStateListener listener, int events) {
        try {
            sRegistry.listenForSubscriber(listener.mSubId, this.mContext != null ? this.mContext.getPackageName() : MediaStore.UNKNOWN_STRING, listener.callback, events, Boolean.valueOf(getITelephony() != null).booleanValue());
        } catch (RemoteException | NullPointerException e) {
        }
    }

    public int getCdmaEriIconIndex() {
        return getCdmaEriIconIndex(getDefaultSubscription());
    }

    public int getCdmaEriIconIndex(long subId) {
        try {
            return getITelephony().getCdmaEriIconIndexForSubscriber(subId);
        } catch (RemoteException | NullPointerException e) {
            return -1;
        }
    }

    public int getCdmaEriIconMode() {
        return getCdmaEriIconMode(getDefaultSubscription());
    }

    public int getCdmaEriIconMode(long subId) {
        try {
            return getITelephony().getCdmaEriIconModeForSubscriber(subId);
        } catch (RemoteException | NullPointerException e) {
            return -1;
        }
    }

    public String getCdmaEriText() {
        return getCdmaEriText(getDefaultSubscription());
    }

    public String getCdmaEriText(long subId) {
        try {
            return getITelephony().getCdmaEriTextForSubscriber(subId);
        } catch (RemoteException | NullPointerException e) {
            return null;
        }
    }

    public boolean isVoiceCapable() {
        if (this.mContext == null) {
            return true;
        }
        return this.mContext.getResources().getBoolean(R.bool.config_voice_capable);
    }

    public boolean isSmsCapable() {
        if (this.mContext == null) {
            return true;
        }
        return this.mContext.getResources().getBoolean(R.bool.config_sms_capable);
    }

    public List<CellInfo> getAllCellInfo() {
        try {
            return getITelephony().getAllCellInfo();
        } catch (RemoteException | NullPointerException e) {
            return null;
        }
    }

    public void setCellInfoListRate(int rateInMillis) {
        try {
            getITelephony().setCellInfoListRate(rateInMillis);
        } catch (RemoteException | NullPointerException e) {
        }
    }

    public String getMmsUserAgent() {
        if (this.mContext == null) {
            return null;
        }
        return this.mContext.getResources().getString(R.string.config_mms_user_agent);
    }

    public String getMmsUAProfUrl() {
        if (this.mContext == null) {
            return null;
        }
        return this.mContext.getResources().getString(R.string.config_mms_user_agent_profile_url);
    }

    public IccOpenLogicalChannelResponse iccOpenLogicalChannel(String AID) {
        try {
            return getITelephony().iccOpenLogicalChannel(AID);
        } catch (RemoteException | NullPointerException e) {
            return null;
        }
    }

    public boolean iccCloseLogicalChannel(int channel) {
        try {
            return getITelephony().iccCloseLogicalChannel(channel);
        } catch (RemoteException | NullPointerException e) {
            return false;
        }
    }

    public String iccTransmitApduLogicalChannel(int channel, int cla, int instruction, int p1, int p2, int p3, String data) {
        try {
            return getITelephony().iccTransmitApduLogicalChannel(channel, cla, instruction, p1, p2, p3, data);
        } catch (RemoteException | NullPointerException e) {
            return ProxyInfo.LOCAL_EXCL_LIST;
        }
    }

    public String iccTransmitApduBasicChannel(int cla, int instruction, int p1, int p2, int p3, String data) {
        try {
            return getITelephony().iccTransmitApduBasicChannel(cla, instruction, p1, p2, p3, data);
        } catch (RemoteException | NullPointerException e) {
            return ProxyInfo.LOCAL_EXCL_LIST;
        }
    }

    public byte[] iccExchangeSimIO(int fileID, int command, int p1, int p2, int p3, String filePath) {
        try {
            return getITelephony().iccExchangeSimIO(fileID, command, p1, p2, p3, filePath);
        } catch (RemoteException | NullPointerException e) {
            return null;
        }
    }

    public String sendEnvelopeWithStatus(String content) {
        try {
            return getITelephony().sendEnvelopeWithStatus(content);
        } catch (RemoteException | NullPointerException e) {
            return ProxyInfo.LOCAL_EXCL_LIST;
        }
    }

    public String nvReadItem(int itemID) {
        try {
            return getITelephony().nvReadItem(itemID);
        } catch (RemoteException ex) {
            Rlog.e(TAG, "nvReadItem RemoteException", ex);
        } catch (NullPointerException ex2) {
            Rlog.e(TAG, "nvReadItem NPE", ex2);
        }
        return ProxyInfo.LOCAL_EXCL_LIST;
    }

    public boolean nvWriteItem(int itemID, String itemValue) {
        try {
            return getITelephony().nvWriteItem(itemID, itemValue);
        } catch (RemoteException ex) {
            Rlog.e(TAG, "nvWriteItem RemoteException", ex);
        } catch (NullPointerException ex2) {
            Rlog.e(TAG, "nvWriteItem NPE", ex2);
        }
        return false;
    }

    public boolean nvWriteCdmaPrl(byte[] preferredRoamingList) {
        try {
            return getITelephony().nvWriteCdmaPrl(preferredRoamingList);
        } catch (RemoteException ex) {
            Rlog.e(TAG, "nvWriteCdmaPrl RemoteException", ex);
        } catch (NullPointerException ex2) {
            Rlog.e(TAG, "nvWriteCdmaPrl NPE", ex2);
        }
        return false;
    }

    public boolean nvResetConfig(int resetType) {
        try {
            return getITelephony().nvResetConfig(resetType);
        } catch (RemoteException ex) {
            Rlog.e(TAG, "nvResetConfig RemoteException", ex);
        } catch (NullPointerException ex2) {
            Rlog.e(TAG, "nvResetConfig NPE", ex2);
        }
        return false;
    }

    private static long getDefaultSubscription() {
        return SubscriptionManager.getDefaultSubId();
    }

    public int getDefaultSim() {
        return 0;
    }

    public static void setTelephonyProperty(String property, long subId, String value) {
        String propVal = ProxyInfo.LOCAL_EXCL_LIST;
        String[] p = null;
        String prop = SystemProperties.get(property);
        int phoneId = SubscriptionManager.getPhoneId(subId);
        if (value == null) {
            value = ProxyInfo.LOCAL_EXCL_LIST;
        }
        if (prop != null) {
            p = prop.split(",");
        }
        if (phoneId >= 0) {
            for (int i = 0; i < phoneId; i++) {
                String str = ProxyInfo.LOCAL_EXCL_LIST;
                if (p != null && i < p.length) {
                    str = p[i];
                }
                propVal = propVal + str + ",";
            }
            String propVal2 = propVal + value;
            if (p != null) {
                for (int i2 = phoneId + 1; i2 < p.length; i2++) {
                    propVal2 = propVal2 + "," + p[i2];
                }
            }
            if (property.length() > 31 || propVal2.length() > 91) {
                Rlog.d(TAG, "setTelephonyProperty length too long:" + property + ", " + propVal2);
                return;
            }
            Rlog.d(TAG, "setTelephonyProperty property=" + property + " propVal=" + propVal2);
            SystemProperties.set(property, propVal2);
        }
    }

    public static int getIntAtIndex(ContentResolver cr, String name, int index) throws Settings.SettingNotFoundException {
        String v = Settings.Global.getString(cr, name);
        if (v != null) {
            String[] valArray = v.split(",");
            if (index >= 0 && index < valArray.length && valArray[index] != null) {
                try {
                    return Integer.parseInt(valArray[index]);
                } catch (NumberFormatException e) {
                }
            }
        }
        throw new Settings.SettingNotFoundException(name);
    }

    public static boolean putIntAtIndex(ContentResolver cr, String name, int index, int value) {
        String data = ProxyInfo.LOCAL_EXCL_LIST;
        String[] valArray = null;
        String v = Settings.Global.getString(cr, name);
        if (v != null) {
            valArray = v.split(",");
        }
        for (int i = 0; i < index; i++) {
            String str = ProxyInfo.LOCAL_EXCL_LIST;
            if (valArray != null && i < valArray.length) {
                str = valArray[i];
            }
            data = data + str + ",";
        }
        String data2 = data + value;
        if (valArray != null) {
            for (int i2 = index + 1; i2 < valArray.length; i2++) {
                data2 = data2 + "," + valArray[i2];
            }
        }
        return Settings.Global.putString(cr, name, data2);
    }

    public static String getTelephonyProperty(String property, long subId, String defaultVal) {
        String propVal = null;
        int phoneId = SubscriptionManager.getPhoneId(subId);
        String prop = SystemProperties.get(property);
        if (prop != null && prop.length() > 0) {
            String[] values = prop.split(",");
            if (phoneId >= 0 && phoneId < values.length && values[phoneId] != null) {
                propVal = values[phoneId];
            }
        }
        return propVal == null ? defaultVal : propVal;
    }

    public int getSimCount() {
        if (isMultiSimEnabled()) {
            return 2;
        }
        return 1;
    }

    public String getIsimIst() {
        try {
            return getSubscriberInfo().getIsimIst();
        } catch (RemoteException | NullPointerException e) {
            return null;
        }
    }

    public String[] getIsimPcscf() {
        try {
            return getSubscriberInfo().getIsimPcscf();
        } catch (RemoteException | NullPointerException e) {
            return null;
        }
    }

    public String getIsimChallengeResponse(String nonce) {
        try {
            return getSubscriberInfo().getIsimChallengeResponse(nonce);
        } catch (RemoteException | NullPointerException e) {
            return null;
        }
    }

    public String getIccSimChallengeResponse(long subId, int appType, String data) {
        try {
            return getSubscriberInfo().getIccSimChallengeResponse(subId, appType, data);
        } catch (RemoteException | NullPointerException e) {
            return null;
        }
    }

    public String getIccSimChallengeResponse(int appType, String data) {
        return getIccSimChallengeResponse(getDefaultSubscription(), appType, data);
    }

    public String[] getPcscfAddress(String apnType) {
        try {
            return getITelephony().getPcscfAddress(apnType);
        } catch (RemoteException e) {
            return new String[0];
        }
    }

    public void setImsRegistrationState(boolean registered) {
        try {
            getITelephony().setImsRegistrationState(registered);
        } catch (RemoteException e) {
        }
    }

    public int getPreferredNetworkType() {
        try {
            return getITelephony().getPreferredNetworkType();
        } catch (RemoteException ex) {
            Rlog.e(TAG, "getPreferredNetworkType RemoteException", ex);
        } catch (NullPointerException ex2) {
            Rlog.e(TAG, "getPreferredNetworkType NPE", ex2);
        }
        return -1;
    }

    public boolean setPreferredNetworkType(int networkType) {
        try {
            return getITelephony().setPreferredNetworkType(networkType);
        } catch (RemoteException ex) {
            Rlog.e(TAG, "setPreferredNetworkType RemoteException", ex);
        } catch (NullPointerException ex2) {
            Rlog.e(TAG, "setPreferredNetworkType NPE", ex2);
        }
        return false;
    }

    public boolean setGlobalPreferredNetworkType() {
        return setPreferredNetworkType(10);
    }

    public int hasCarrierPrivileges() {
        try {
            return getITelephony().hasCarrierPrivileges();
        } catch (RemoteException ex) {
            Rlog.e(TAG, "hasCarrierPrivileges RemoteException", ex);
        } catch (NullPointerException ex2) {
            Rlog.e(TAG, "hasCarrierPrivileges NPE", ex2);
        }
        return 0;
    }

    public boolean setOperatorBrandOverride(String brand) {
        try {
            return getITelephony().setOperatorBrandOverride(brand);
        } catch (RemoteException ex) {
            Rlog.e(TAG, "setOperatorBrandOverride RemoteException", ex);
        } catch (NullPointerException ex2) {
            Rlog.e(TAG, "setOperatorBrandOverride NPE", ex2);
        }
        return false;
    }

    public String getCdmaMdn() {
        return getCdmaMdn(getDefaultSubscription());
    }

    public String getCdmaMdn(long subId) {
        try {
            return getITelephony().getCdmaMdn(subId);
        } catch (RemoteException | NullPointerException e) {
            return null;
        }
    }

    public String getCdmaMin() {
        return getCdmaMin(getDefaultSubscription());
    }

    public String getCdmaMin(long subId) {
        try {
            return getITelephony().getCdmaMin(subId);
        } catch (RemoteException | NullPointerException e) {
            return null;
        }
    }

    public int checkCarrierPrivilegesForPackage(String pkgname) {
        try {
            return getITelephony().checkCarrierPrivilegesForPackage(pkgname);
        } catch (RemoteException ex) {
            Rlog.e(TAG, "hasCarrierPrivileges RemoteException", ex);
        } catch (NullPointerException ex2) {
            Rlog.e(TAG, "hasCarrierPrivileges NPE", ex2);
        }
        return 0;
    }

    public List<String> getCarrierPackageNamesForIntent(Intent intent) {
        try {
            return getITelephony().getCarrierPackageNamesForIntent(intent);
        } catch (RemoteException ex) {
            Rlog.e(TAG, "getCarrierPackageNamesForIntent RemoteException", ex);
        } catch (NullPointerException ex2) {
            Rlog.e(TAG, "getCarrierPackageNamesForIntent NPE", ex2);
        }
        return null;
    }

    public void dial(String number) {
        try {
            getITelephony().dial(number);
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#dial", e);
        }
    }

    public void call(String callingPackage, String number) {
        try {
            getITelephony().call(callingPackage, number);
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#call", e);
        }
    }

    public boolean endCall() {
        try {
            return getITelephony().endCall();
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#endCall", e);
            return false;
        }
    }

    public void answerRingingCall() {
        try {
            getITelephony().answerRingingCall();
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#answerRingingCall", e);
        }
    }

    public void silenceRinger() {
        try {
            getTelecomService().silenceRinger();
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelecomService#silenceRinger", e);
        }
    }

    public boolean isOffhook() {
        try {
            return getITelephony().isOffhook();
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#isOffhook", e);
            return false;
        }
    }

    public boolean isRinging() {
        try {
            return getITelephony().isRinging();
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#isRinging", e);
            return false;
        }
    }

    public boolean isIdle() {
        try {
            return getITelephony().isIdle();
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#isIdle", e);
            return true;
        }
    }

    public boolean isRadioOn() {
        try {
            return getITelephony().isRadioOn();
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#isRadioOn", e);
            return false;
        }
    }

    public boolean isSimPinEnabled() {
        try {
            return getITelephony().isSimPinEnabled();
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#isSimPinEnabled", e);
            return false;
        }
    }

    public boolean supplyPin(String pin) {
        try {
            return getITelephony().supplyPin(pin);
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#supplyPin", e);
            return false;
        }
    }

    public boolean supplyPuk(String puk, String pin) {
        try {
            return getITelephony().supplyPuk(puk, pin);
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#supplyPuk", e);
            return false;
        }
    }

    public int[] supplyPinReportResult(String pin) {
        try {
            return getITelephony().supplyPinReportResult(pin);
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#supplyPinReportResult", e);
            return new int[0];
        }
    }

    public int[] supplyPukReportResult(String puk, String pin) {
        try {
            return getITelephony().supplyPukReportResult(puk, pin);
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#]", e);
            return new int[0];
        }
    }

    public boolean handlePinMmi(String dialString) {
        try {
            return getITelephony().handlePinMmi(dialString);
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#handlePinMmi", e);
            return false;
        }
    }

    public void toggleRadioOnOff() {
        try {
            getITelephony().toggleRadioOnOff();
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#toggleRadioOnOff", e);
        }
    }

    public boolean setRadio(boolean turnOn) {
        try {
            return getITelephony().setRadio(turnOn);
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#setRadio", e);
            return false;
        }
    }

    public boolean setRadioPower(boolean turnOn) {
        try {
            return getITelephony().setRadioPower(turnOn);
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#setRadioPower", e);
            return false;
        }
    }

    public void updateServiceLocation() {
        try {
            getITelephony().updateServiceLocation();
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#updateServiceLocation", e);
        }
    }

    public boolean enableDataConnectivity() {
        try {
            return getITelephony().enableDataConnectivity();
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#enableDataConnectivity", e);
            return false;
        }
    }

    public boolean disableDataConnectivity() {
        try {
            return getITelephony().disableDataConnectivity();
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#disableDataConnectivity", e);
            return false;
        }
    }

    public boolean isDataConnectivityPossible() {
        try {
            return getITelephony().isDataConnectivityPossible();
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#isDataConnectivityPossible", e);
            return false;
        }
    }

    public boolean needsOtaServiceProvisioning() {
        try {
            return getITelephony().needsOtaServiceProvisioning();
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#needsOtaServiceProvisioning", e);
            return false;
        }
    }

    public void setDataEnabled(boolean enable) {
        try {
            getITelephony().setDataEnabled(enable);
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#setDataEnabled", e);
        }
    }

    public boolean getDataEnabled() {
        try {
            return getITelephony().getDataEnabled();
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#getDataEnabled", e);
            return false;
        }
    }

    public void enableSimplifiedNetworkSettings(boolean enable) {
        enableSimplifiedNetworkSettingsForSubscriber(getDefaultSubscription(), enable);
    }

    public void enableSimplifiedNetworkSettingsForSubscriber(long subId, boolean enable) {
        try {
            getITelephony().enableSimplifiedNetworkSettingsForSubscriber(subId, enable);
        } catch (RemoteException | NullPointerException e) {
        }
    }

    public boolean getSimplifiedNetworkSettingsEnabled() {
        return getSimplifiedNetworkSettingsEnabledForSubscriber(getDefaultSubscription());
    }

    public boolean getSimplifiedNetworkSettingsEnabledForSubscriber(long subId) {
        try {
            return getITelephony().getSimplifiedNetworkSettingsEnabledForSubscriber(subId);
        } catch (RemoteException | NullPointerException e) {
            return false;
        }
    }

    public int invokeOemRilRequestRaw(byte[] oemReq, byte[] oemResp) {
        try {
            return getITelephony().invokeOemRilRequestRaw(oemReq, oemResp);
        } catch (RemoteException | NullPointerException e) {
            return -1;
        }
    }
}

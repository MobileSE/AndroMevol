package android.telephony;

import android.content.Intent;
import android.net.Uri;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.BaseColumns;
import com.android.internal.R;
import com.android.internal.telephony.ISub;
import com.android.internal.telephony.PhoneConstants;
import java.util.ArrayList;
import java.util.List;

public class SubscriptionManager implements BaseColumns {
    public static final long ASK_USER_SUB_ID = -1001;
    public static final String COLOR = "color";
    public static final int COLOR_1 = 0;
    public static final int COLOR_2 = 1;
    public static final int COLOR_3 = 2;
    public static final int COLOR_4 = 3;
    public static final int COLOR_DEFAULT = 0;
    public static final Uri CONTENT_URI = Uri.parse("content://telephony/siminfo");
    public static final String DATA_ROAMING = "data_roaming";
    public static final int DATA_ROAMING_DEFAULT = 0;
    public static final int DATA_ROAMING_DISABLE = 0;
    public static final int DATA_ROAMING_ENABLE = 1;
    private static final boolean DBG = true;
    public static final int DEFAULT_INT_VALUE = -100;
    public static final int DEFAULT_NAME_RES = 17039374;
    public static final int DEFAULT_PHONE_ID = Integer.MAX_VALUE;
    public static final int DEFAULT_SLOT_ID = Integer.MAX_VALUE;
    public static final String DEFAULT_STRING_VALUE = "N/A";
    public static final long DEFAULT_SUB_ID = Long.MAX_VALUE;
    public static final int DISLPAY_NUMBER_DEFAULT = 1;
    public static final String DISPLAY_NAME = "display_name";
    public static final int DISPLAY_NUMBER_FIRST = 1;
    public static final String DISPLAY_NUMBER_FORMAT = "display_number_format";
    public static final int DISPLAY_NUMBER_LAST = 2;
    public static final int DISPLAY_NUMBER_NONE = 0;
    public static final int EXTRA_VALUE_NEW_SIM = 1;
    public static final int EXTRA_VALUE_NOCHANGE = 4;
    public static final int EXTRA_VALUE_REMOVE_SIM = 2;
    public static final int EXTRA_VALUE_REPOSITION_SIM = 3;
    public static final String ICC_ID = "icc_id";
    public static final String INTENT_KEY_DETECT_STATUS = "simDetectStatus";
    public static final String INTENT_KEY_NEW_SIM_SLOT = "newSIMSlot";
    public static final String INTENT_KEY_NEW_SIM_STATUS = "newSIMStatus";
    public static final String INTENT_KEY_SIM_COUNT = "simCount";
    public static final int INVALID_PHONE_ID = -1000;
    public static final int INVALID_SLOT_ID = -1000;
    public static final long INVALID_SUB_ID = -1000;
    private static final String LOG_TAG = "SUB";
    public static final String MCC = "mcc";
    public static final String MNC = "mnc";
    public static final String NAME_SOURCE = "name_source";
    public static final int NAME_SOURCE_DEFAULT_SOURCE = 0;
    public static final int NAME_SOURCE_SIM_SOURCE = 1;
    public static final int NAME_SOURCE_UNDEFINDED = -1;
    public static final int NAME_SOURCE_USER_INPUT = 2;
    public static final String NUMBER = "number";
    private static final int RES_TYPE_BACKGROUND_DARK = 0;
    private static final int RES_TYPE_BACKGROUND_LIGHT = 1;
    public static final String SIM_ID = "sim_id";
    public static final int SIM_NOT_INSERTED = -1;
    public static final String SUB_DEFAULT_CHANGED_ACTION = "android.intent.action.SUB_DEFAULT_CHANGED";
    private static final boolean VDBG = false;
    private static final int[] sSimBackgroundDarkRes = setSimResource(0);

    public SubscriptionManager() {
        logd("SubscriptionManager created");
    }

    public static SubInfoRecord getSubInfoForSubscriber(long subId) {
        if (!isValidSubId(subId)) {
            logd("[getSubInfoForSubscriberx]- invalid subId");
            return null;
        }
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                return iSub.getSubInfoForSubscriber(subId);
            }
            return null;
        } catch (RemoteException e) {
            return null;
        }
    }

    public static List<SubInfoRecord> getSubInfoUsingIccId(String iccId) {
        if (iccId == null) {
            logd("[getSubInfoUsingIccId]- null iccid");
            return null;
        }
        List<SubInfoRecord> result = null;
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                result = iSub.getSubInfoUsingIccId(iccId);
            }
        } catch (RemoteException e) {
        }
        if (result == null) {
            return new ArrayList<>();
        }
        return result;
    }

    public static List<SubInfoRecord> getSubInfoUsingSlotId(int slotId) {
        if (!isValidSlotId(slotId)) {
            logd("[getSubInfoUsingSlotId]- invalid slotId");
            return null;
        }
        List<SubInfoRecord> result = null;
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                result = iSub.getSubInfoUsingSlotId(slotId);
            }
        } catch (RemoteException e) {
        }
        if (result == null) {
            return new ArrayList<>();
        }
        return result;
    }

    public static List<SubInfoRecord> getAllSubInfoList() {
        List<SubInfoRecord> result = null;
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                result = iSub.getAllSubInfoList();
            }
        } catch (RemoteException e) {
        }
        if (result == null) {
            return new ArrayList<>();
        }
        return result;
    }

    public static List<SubInfoRecord> getActiveSubInfoList() {
        List<SubInfoRecord> result = null;
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                result = iSub.getActiveSubInfoList();
            }
        } catch (RemoteException e) {
        }
        if (result == null) {
            return new ArrayList<>();
        }
        return result;
    }

    public static int getAllSubInfoCount() {
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                return iSub.getAllSubInfoCount();
            }
            return 0;
        } catch (RemoteException e) {
            return 0;
        }
    }

    public static int getActiveSubInfoCount() {
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                return iSub.getActiveSubInfoCount();
            }
            return 0;
        } catch (RemoteException e) {
            return 0;
        }
    }

    public static Uri addSubInfoRecord(String iccId, int slotId) {
        if (iccId == null) {
            logd("[addSubInfoRecord]- null iccId");
        }
        if (!isValidSlotId(slotId)) {
            logd("[addSubInfoRecord]- invalid slotId");
        }
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub == null) {
                return null;
            }
            iSub.addSubInfoRecord(iccId, slotId);
            return null;
        } catch (RemoteException e) {
            return null;
        }
    }

    public static int setColor(int color, long subId) {
        int size = sSimBackgroundDarkRes.length;
        if (!isValidSubId(subId) || color < 0 || color >= size) {
            logd("[setColor]- fail");
            return -1;
        }
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                return iSub.setColor(color, subId);
            }
            return 0;
        } catch (RemoteException e) {
            return 0;
        }
    }

    public static int setDisplayName(String displayName, long subId) {
        return setDisplayName(displayName, subId, -1);
    }

    public static int setDisplayName(String displayName, long subId, long nameSource) {
        if (!isValidSubId(subId)) {
            logd("[setDisplayName]- fail");
            return -1;
        }
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                return iSub.setDisplayNameUsingSrc(displayName, subId, nameSource);
            }
            return 0;
        } catch (RemoteException e) {
            return 0;
        }
    }

    public static int setDisplayNumber(String number, long subId) {
        if (number == null || !isValidSubId(subId)) {
            logd("[setDisplayNumber]- fail");
            return -1;
        }
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                return iSub.setDisplayNumber(number, subId);
            }
            return 0;
        } catch (RemoteException e) {
            return 0;
        }
    }

    public static int setDisplayNumberFormat(int format, long subId) {
        if (format < 0 || !isValidSubId(subId)) {
            logd("[setDisplayNumberFormat]- fail, return -1");
            return -1;
        }
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                return iSub.setDisplayNumberFormat(format, subId);
            }
            return 0;
        } catch (RemoteException e) {
            return 0;
        }
    }

    public static int setDataRoaming(int roaming, long subId) {
        if (roaming < 0 || !isValidSubId(subId)) {
            logd("[setDataRoaming]- fail");
            return -1;
        }
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                return iSub.setDataRoaming(roaming, subId);
            }
            return 0;
        } catch (RemoteException e) {
            return 0;
        }
    }

    public static int getSlotId(long subId) {
        if (!isValidSubId(subId)) {
            logd("[getSlotId]- fail");
        }
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                return iSub.getSlotId(subId);
            }
            return -1000;
        } catch (RemoteException e) {
            return -1000;
        }
    }

    public static long[] getSubId(int slotId) {
        if (!isValidSlotId(slotId)) {
            logd("[getSubId]- fail");
            return null;
        }
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                return iSub.getSubId(slotId);
            }
            return null;
        } catch (RemoteException e) {
            return null;
        }
    }

    public static int getPhoneId(long subId) {
        if (!isValidSubId(subId)) {
            logd("[getPhoneId]- fail");
            return -1000;
        }
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                return iSub.getPhoneId(subId);
            }
            return -1000;
        } catch (RemoteException e) {
            return -1000;
        }
    }

    private static int[] setSimResource(int type) {
        switch (type) {
            case 0:
                return new int[]{R.drawable.sim_dark_blue, R.drawable.sim_dark_orange, R.drawable.sim_dark_green, R.drawable.sim_dark_purple};
            case 1:
                return new int[]{R.drawable.sim_light_blue, R.drawable.sim_light_orange, R.drawable.sim_light_green, R.drawable.sim_light_purple};
            default:
                return null;
        }
    }

    private static void logd(String msg) {
        Rlog.d(LOG_TAG, "[SubManager] " + msg);
    }

    public static long getDefaultSubId() {
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                return iSub.getDefaultSubId();
            }
            return -1000;
        } catch (RemoteException e) {
            return -1000;
        }
    }

    public static long getDefaultVoiceSubId() {
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                return iSub.getDefaultVoiceSubId();
            }
            return -1000;
        } catch (RemoteException e) {
            return -1000;
        }
    }

    public static void setDefaultVoiceSubId(long subId) {
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                iSub.setDefaultVoiceSubId(subId);
            }
        } catch (RemoteException e) {
        }
    }

    public static SubInfoRecord getDefaultVoiceSubInfo() {
        return getSubInfoForSubscriber(getDefaultVoiceSubId());
    }

    public static int getDefaultVoicePhoneId() {
        return getPhoneId(getDefaultVoiceSubId());
    }

    public static long getDefaultSmsSubId() {
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                return iSub.getDefaultSmsSubId();
            }
            return -1000;
        } catch (RemoteException e) {
            return -1000;
        }
    }

    public static void setDefaultSmsSubId(long subId) {
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                iSub.setDefaultSmsSubId(subId);
            }
        } catch (RemoteException e) {
        }
    }

    public static SubInfoRecord getDefaultSmsSubInfo() {
        return getSubInfoForSubscriber(getDefaultSmsSubId());
    }

    public static int getDefaultSmsPhoneId() {
        return getPhoneId(getDefaultSmsSubId());
    }

    public static long getDefaultDataSubId() {
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                return iSub.getDefaultDataSubId();
            }
            return -1000;
        } catch (RemoteException e) {
            return -1000;
        }
    }

    public static void setDefaultDataSubId(long subId) {
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                iSub.setDefaultDataSubId(subId);
            }
        } catch (RemoteException e) {
        }
    }

    public static SubInfoRecord getDefaultDataSubInfo() {
        return getSubInfoForSubscriber(getDefaultDataSubId());
    }

    public static int getDefaultDataPhoneId() {
        return getPhoneId(getDefaultDataSubId());
    }

    public static void clearSubInfo() {
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                iSub.clearSubInfo();
            }
        } catch (RemoteException e) {
        }
    }

    public static boolean allDefaultsSelected() {
        if (getDefaultDataSubId() == -1000 || getDefaultSmsSubId() == -1000 || getDefaultVoiceSubId() == -1000) {
            return false;
        }
        return true;
    }

    public static void clearDefaultsForInactiveSubIds() {
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                iSub.clearDefaultsForInactiveSubIds();
            }
        } catch (RemoteException e) {
        }
    }

    public static boolean isValidSubId(long subId) {
        return subId > -1000;
    }

    public static boolean isValidSlotId(int slotId) {
        return slotId != -1000 && slotId >= 0 && slotId < TelephonyManager.getDefault().getSimCount();
    }

    public static boolean isValidPhoneId(int phoneId) {
        return phoneId != -1000 && phoneId >= 0 && phoneId < TelephonyManager.getDefault().getPhoneCount();
    }

    public static void putPhoneIdAndSubIdExtra(Intent intent, int phoneId) {
        long[] subIds = getSubId(phoneId);
        if (subIds == null || subIds.length <= 0) {
            logd("putPhoneIdAndSubIdExtra: no valid subs");
        } else {
            putPhoneIdAndSubIdExtra(intent, phoneId, subIds[0]);
        }
    }

    public static void putPhoneIdAndSubIdExtra(Intent intent, int phoneId, long subId) {
        intent.putExtra(PhoneConstants.SUBSCRIPTION_KEY, subId);
        intent.putExtra("phone", phoneId);
        intent.putExtra(PhoneConstants.SLOT_KEY, phoneId);
    }

    public static long[] getActiveSubIdList() {
        long[] subId = null;
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                subId = iSub.getActiveSubIdList();
            }
        } catch (RemoteException e) {
        }
        if (subId == null) {
            return new long[0];
        }
        return subId;
    }
}

package android.media.tv;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.hardware.hdmi.HdmiDeviceInfo;
import android.net.Uri;
import android.os.FileObserver;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.util.Xml;
import com.android.internal.R;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.xmlpull.v1.XmlPullParserException;

public final class TvInputInfo implements Parcelable {
    public static final Parcelable.Creator<TvInputInfo> CREATOR = new Parcelable.Creator<TvInputInfo>() {
        /* class android.media.tv.TvInputInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public TvInputInfo createFromParcel(Parcel in) {
            return new TvInputInfo(in);
        }

        @Override // android.os.Parcelable.Creator
        public TvInputInfo[] newArray(int size) {
            return new TvInputInfo[size];
        }
    };
    private static final boolean DEBUG = false;
    private static final String DELIMITER_INFO_IN_ID = "/";
    public static final String EXTRA_INPUT_ID = "android.media.tv.extra.INPUT_ID";
    private static final int LENGTH_HDMI_DEVICE_ID = 2;
    private static final int LENGTH_HDMI_PHYSICAL_ADDRESS = 4;
    private static final String PREFIX_HARDWARE_DEVICE = "HW";
    private static final String PREFIX_HDMI_DEVICE = "HDMI";
    private static final String TAG = "TvInputInfo";
    public static final int TYPE_COMPONENT = 1004;
    public static final int TYPE_COMPOSITE = 1001;
    public static final int TYPE_DISPLAY_PORT = 1008;
    public static final int TYPE_DVI = 1006;
    public static final int TYPE_HDMI = 1007;
    public static final int TYPE_OTHER = 1000;
    public static final int TYPE_SCART = 1003;
    public static final int TYPE_SVIDEO = 1002;
    public static final int TYPE_TUNER = 0;
    public static final int TYPE_VGA = 1005;
    private static final String XML_START_TAG_NAME = "tv-input";
    private static SparseIntArray sHardwareTypeToTvInputType = new SparseIntArray();
    private HdmiDeviceInfo mHdmiDeviceInfo;
    private Uri mIconUri;
    private final String mId;
    private boolean mIsConnectedToHdmiSwitch;
    private String mLabel;
    private final String mParentId;
    private final ResolveInfo mService;
    private String mSettingsActivity;
    private String mSetupActivity;
    private int mType;

    static {
        sHardwareTypeToTvInputType.put(1, 1000);
        sHardwareTypeToTvInputType.put(2, 0);
        sHardwareTypeToTvInputType.put(3, 1001);
        sHardwareTypeToTvInputType.put(4, 1002);
        sHardwareTypeToTvInputType.put(5, 1003);
        sHardwareTypeToTvInputType.put(6, 1004);
        sHardwareTypeToTvInputType.put(7, 1005);
        sHardwareTypeToTvInputType.put(8, 1006);
        sHardwareTypeToTvInputType.put(9, 1007);
        sHardwareTypeToTvInputType.put(10, 1008);
    }

    public static TvInputInfo createTvInputInfo(Context context, ResolveInfo service) throws XmlPullParserException, IOException {
        return createTvInputInfo(context, service, generateInputIdForComponentName(new ComponentName(service.serviceInfo.packageName, service.serviceInfo.name)), null, 0, null, null, false);
    }

    public static TvInputInfo createTvInputInfo(Context context, ResolveInfo service, HdmiDeviceInfo hdmiDeviceInfo, String parentId, String label, Uri iconUri) throws XmlPullParserException, IOException {
        TvInputInfo input = createTvInputInfo(context, service, generateInputIdForHdmiDevice(new ComponentName(service.serviceInfo.packageName, service.serviceInfo.name), hdmiDeviceInfo), parentId, 1007, label, iconUri, (hdmiDeviceInfo.getPhysicalAddress() & FileObserver.ALL_EVENTS) != 0);
        input.mHdmiDeviceInfo = hdmiDeviceInfo;
        return input;
    }

    public static TvInputInfo createTvInputInfo(Context context, ResolveInfo service, TvInputHardwareInfo hardwareInfo, String label, Uri iconUri) throws XmlPullParserException, IOException {
        return createTvInputInfo(context, service, generateInputIdForHardware(new ComponentName(service.serviceInfo.packageName, service.serviceInfo.name), hardwareInfo), null, sHardwareTypeToTvInputType.get(hardwareInfo.getType(), 0), label, iconUri, false);
    }

    private static TvInputInfo createTvInputInfo(Context context, ResolveInfo service, String id, String parentId, int inputType, String label, Uri iconUri, boolean isConnectedToHdmiSwitch) throws XmlPullParserException, IOException {
        int type;
        ServiceInfo si = service.serviceInfo;
        PackageManager pm = context.getPackageManager();
        XmlResourceParser parser = null;
        try {
            XmlResourceParser parser2 = si.loadXmlMetaData(pm, TvInputService.SERVICE_META_DATA);
            if (parser2 == null) {
                throw new XmlPullParserException("No android.media.tv.input meta-data for " + si.name);
            }
            Resources res = pm.getResourcesForApplication(si.applicationInfo);
            AttributeSet attrs = Xml.asAttributeSet(parser2);
            do {
                type = parser2.next();
                if (type == 1) {
                    break;
                }
            } while (type != 2);
            if (!XML_START_TAG_NAME.equals(parser2.getName())) {
                throw new XmlPullParserException("Meta-data does not start with tv-input-service tag in " + si.name);
            }
            TvInputInfo input = new TvInputInfo(service, id, parentId, inputType);
            TypedArray sa = res.obtainAttributes(attrs, R.styleable.TvInputService);
            input.mSetupActivity = sa.getString(1);
            input.mSettingsActivity = sa.getString(0);
            sa.recycle();
            input.mLabel = label;
            input.mIconUri = iconUri;
            input.mIsConnectedToHdmiSwitch = isConnectedToHdmiSwitch;
            if (parser2 != null) {
                parser2.close();
            }
            return input;
        } catch (PackageManager.NameNotFoundException e) {
            throw new XmlPullParserException("Unable to create context for: " + si.packageName);
        } catch (Throwable th) {
            if (0 != 0) {
                parser.close();
            }
            throw th;
        }
    }

    private TvInputInfo(ResolveInfo service, String id, String parentId, int type) {
        this.mType = 0;
        this.mService = service;
        this.mId = id;
        this.mParentId = parentId;
        this.mType = type;
    }

    public String getId() {
        return this.mId;
    }

    public String getParentId() {
        return this.mParentId;
    }

    public ServiceInfo getServiceInfo() {
        return this.mService.serviceInfo;
    }

    public ComponentName getComponent() {
        return new ComponentName(this.mService.serviceInfo.packageName, this.mService.serviceInfo.name);
    }

    public Intent createSetupIntent() {
        if (TextUtils.isEmpty(this.mSetupActivity)) {
            return null;
        }
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName(this.mService.serviceInfo.packageName, this.mSetupActivity);
        intent.putExtra(EXTRA_INPUT_ID, getId());
        return intent;
    }

    public Intent createSettingsIntent() {
        if (TextUtils.isEmpty(this.mSettingsActivity)) {
            return null;
        }
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName(this.mService.serviceInfo.packageName, this.mSettingsActivity);
        intent.putExtra(EXTRA_INPUT_ID, getId());
        return intent;
    }

    public int getType() {
        return this.mType;
    }

    public HdmiDeviceInfo getHdmiDeviceInfo() {
        if (this.mType == 1007) {
            return this.mHdmiDeviceInfo;
        }
        return null;
    }

    public boolean isPassthroughInput() {
        return this.mType != 0;
    }

    public boolean isConnectedToHdmiSwitch() {
        return this.mIsConnectedToHdmiSwitch;
    }

    public boolean isHidden(Context context) {
        return TvInputSettings.isHidden(context, this.mId, UserHandle.myUserId());
    }

    public CharSequence loadLabel(Context context) {
        if (TextUtils.isEmpty(this.mLabel)) {
            return this.mService.loadLabel(context.getPackageManager());
        }
        return this.mLabel;
    }

    public CharSequence loadCustomLabel(Context context) {
        return TvInputSettings.getCustomLabel(context, this.mId, UserHandle.myUserId());
    }

    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0064, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0065, code lost:
        r5 = r4;
        r4 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0079, code lost:
        r4 = th;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public android.graphics.drawable.Drawable loadIcon(android.content.Context r9) {
        /*
        // Method dump skipped, instructions count: 123
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.tv.TvInputInfo.loadIcon(android.content.Context):android.graphics.drawable.Drawable");
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public int hashCode() {
        return this.mId.hashCode();
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof TvInputInfo)) {
            return false;
        }
        return this.mId.equals(((TvInputInfo) o).mId);
    }

    public String toString() {
        return "TvInputInfo{id=" + this.mId + ", pkg=" + this.mService.serviceInfo.packageName + ", service=" + this.mService.serviceInfo.name + "}";
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mId);
        dest.writeString(this.mParentId);
        this.mService.writeToParcel(dest, flags);
        dest.writeString(this.mSetupActivity);
        dest.writeString(this.mSettingsActivity);
        dest.writeInt(this.mType);
        dest.writeParcelable(this.mHdmiDeviceInfo, flags);
        dest.writeParcelable(this.mIconUri, flags);
        dest.writeString(this.mLabel);
        dest.writeByte(this.mIsConnectedToHdmiSwitch ? (byte) 1 : 0);
    }

    private Drawable loadServiceIcon(Context context) {
        if (this.mService.serviceInfo.icon == 0 && this.mService.serviceInfo.applicationInfo.icon == 0) {
            return null;
        }
        return this.mService.serviceInfo.loadIcon(context.getPackageManager());
    }

    private static final String generateInputIdForComponentName(ComponentName name) {
        return name.flattenToShortString();
    }

    private static final String generateInputIdForHdmiDevice(ComponentName name, HdmiDeviceInfo deviceInfo) {
        String format = String.format("%s%s%%0%sX%%0%sX", DELIMITER_INFO_IN_ID, PREFIX_HDMI_DEVICE, 4, 2);
        return name.flattenToShortString() + String.format(format, Integer.valueOf(deviceInfo.getPhysicalAddress()), Integer.valueOf(deviceInfo.getId()));
    }

    private static final String generateInputIdForHardware(ComponentName name, TvInputHardwareInfo hardwareInfo) {
        return name.flattenToShortString() + String.format("%s%s%d", DELIMITER_INFO_IN_ID, PREFIX_HARDWARE_DEVICE, Integer.valueOf(hardwareInfo.getDeviceId()));
    }

    private TvInputInfo(Parcel in) {
        boolean z;
        this.mType = 0;
        this.mId = in.readString();
        this.mParentId = in.readString();
        this.mService = ResolveInfo.CREATOR.createFromParcel(in);
        this.mSetupActivity = in.readString();
        this.mSettingsActivity = in.readString();
        this.mType = in.readInt();
        this.mHdmiDeviceInfo = (HdmiDeviceInfo) in.readParcelable(null);
        this.mIconUri = (Uri) in.readParcelable(null);
        this.mLabel = in.readString();
        if (in.readByte() == 1) {
            z = true;
        } else {
            z = false;
        }
        this.mIsConnectedToHdmiSwitch = z;
    }

    public static final class TvInputSettings {
        private static final String CUSTOM_NAME_SEPARATOR = ",";
        private static final String TV_INPUT_SEPARATOR = ":";

        private TvInputSettings() {
        }

        /* access modifiers changed from: private */
        public static boolean isHidden(Context context, String inputId, int userId) {
            return getHiddenTvInputIds(context, userId).contains(inputId);
        }

        /* access modifiers changed from: private */
        public static String getCustomLabel(Context context, String inputId, int userId) {
            return getCustomLabels(context, userId).get(inputId);
        }

        public static Set<String> getHiddenTvInputIds(Context context, int userId) {
            String hiddenIdsString = Settings.Secure.getStringForUser(context.getContentResolver(), Settings.Secure.TV_INPUT_HIDDEN_INPUTS, userId);
            Set<String> set = new HashSet<>();
            if (!TextUtils.isEmpty(hiddenIdsString)) {
                for (String id : hiddenIdsString.split(TV_INPUT_SEPARATOR)) {
                    set.add(Uri.decode(id));
                }
            }
            return set;
        }

        public static Map<String, String> getCustomLabels(Context context, int userId) {
            String labelsString = Settings.Secure.getStringForUser(context.getContentResolver(), Settings.Secure.TV_INPUT_CUSTOM_LABELS, userId);
            Map<String, String> map = new HashMap<>();
            if (!TextUtils.isEmpty(labelsString)) {
                for (String pairString : labelsString.split(TV_INPUT_SEPARATOR)) {
                    String[] pair = pairString.split(CUSTOM_NAME_SEPARATOR);
                    map.put(Uri.decode(pair[0]), Uri.decode(pair[1]));
                }
            }
            return map;
        }

        public static void putHiddenTvInputs(Context context, Set<String> hiddenInputIds, int userId) {
            StringBuilder builder = new StringBuilder();
            boolean firstItem = true;
            for (String inputId : hiddenInputIds) {
                ensureValidField(inputId);
                if (firstItem) {
                    firstItem = false;
                } else {
                    builder.append(TV_INPUT_SEPARATOR);
                }
                builder.append(Uri.encode(inputId));
            }
            Settings.Secure.putStringForUser(context.getContentResolver(), Settings.Secure.TV_INPUT_HIDDEN_INPUTS, builder.toString(), userId);
        }

        public static void putCustomLabels(Context context, Map<String, String> customLabels, int userId) {
            StringBuilder builder = new StringBuilder();
            boolean firstItem = true;
            for (Map.Entry<String, String> entry : customLabels.entrySet()) {
                ensureValidField(entry.getKey());
                ensureValidField(entry.getValue());
                if (firstItem) {
                    firstItem = false;
                } else {
                    builder.append(TV_INPUT_SEPARATOR);
                }
                builder.append(Uri.encode(entry.getKey()));
                builder.append(CUSTOM_NAME_SEPARATOR);
                builder.append(Uri.encode(entry.getValue()));
            }
            Settings.Secure.putStringForUser(context.getContentResolver(), Settings.Secure.TV_INPUT_CUSTOM_LABELS, builder.toString(), userId);
        }

        private static void ensureValidField(String value) {
            if (TextUtils.isEmpty(value)) {
                throw new IllegalArgumentException(value + " should not empty ");
            }
        }
    }
}

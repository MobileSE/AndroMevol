package android.service.notification;

import android.content.ComponentName;
import android.content.res.Resources;
import android.net.ProxyInfo;
import android.net.Uri;
import android.nfc.cardemulation.CardEmulation;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.Contacts;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Slog;
import com.android.internal.R;
import com.android.internal.telephony.IccCardConstants;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class ZenModeConfig implements Parcelable {
    private static final String ALLOW_ATT_CALLS = "calls";
    private static final String ALLOW_ATT_EVENTS = "events";
    private static final String ALLOW_ATT_FROM = "from";
    private static final String ALLOW_ATT_MESSAGES = "messages";
    private static final String ALLOW_TAG = "allow";
    public static final int[] ALL_DAYS = {1, 2, 3, 4, 5, 6, 7};
    private static final String CONDITION_ATT_COMPONENT = "component";
    private static final String CONDITION_ATT_FLAGS = "flags";
    private static final String CONDITION_ATT_ICON = "icon";
    private static final String CONDITION_ATT_ID = "id";
    private static final String CONDITION_ATT_LINE1 = "line1";
    private static final String CONDITION_ATT_LINE2 = "line2";
    private static final String CONDITION_ATT_STATE = "state";
    private static final String CONDITION_ATT_SUMMARY = "summary";
    private static final String CONDITION_TAG = "condition";
    private static final String COUNTDOWN_PATH = "countdown";
    public static final Parcelable.Creator<ZenModeConfig> CREATOR = new Parcelable.Creator<ZenModeConfig>() {
        /* class android.service.notification.ZenModeConfig.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ZenModeConfig createFromParcel(Parcel source) {
            return new ZenModeConfig(source);
        }

        @Override // android.os.Parcelable.Creator
        public ZenModeConfig[] newArray(int size) {
            return new ZenModeConfig[size];
        }
    };
    private static final boolean DEFAULT_ALLOW_EVENTS = true;
    private static final String DOWNTIME_PATH = "downtime";
    private static final String EXIT_CONDITION_ATT_COMPONENT = "component";
    private static final String EXIT_CONDITION_TAG = "exitCondition";
    public static final int MAX_SOURCE = 2;
    private static final int MINUTES_MS = 60000;
    public static final int[] MINUTE_BUCKETS = {15, 30, 45, 60, 120, 180, 240, DisplayMetrics.DENSITY_XXHIGH};
    private static final int SECONDS_MS = 1000;
    private static final String SLEEP_ATT_END_HR = "endHour";
    private static final String SLEEP_ATT_END_MIN = "endMin";
    private static final String SLEEP_ATT_MODE = "mode";
    private static final String SLEEP_ATT_START_HR = "startHour";
    private static final String SLEEP_ATT_START_MIN = "startMin";
    public static final String SLEEP_MODE_DAYS_PREFIX = "days:";
    public static final String SLEEP_MODE_NIGHTS = "nights";
    public static final String SLEEP_MODE_WEEKNIGHTS = "weeknights";
    private static final String SLEEP_TAG = "sleep";
    public static final int SOURCE_ANYONE = 0;
    public static final int SOURCE_CONTACT = 1;
    public static final int SOURCE_STAR = 2;
    private static final String SYSTEM_AUTHORITY = "android";
    private static String TAG = "ZenModeConfig";
    public static final int[] WEEKNIGHT_DAYS = {1, 2, 3, 4, 5};
    private static final int XML_VERSION = 1;
    private static final String ZEN_ATT_VERSION = "version";
    private static final String ZEN_TAG = "zen";
    private static final int ZERO_VALUE_MS = 20000;
    public boolean allowCalls;
    public boolean allowEvents = true;
    public int allowFrom = 0;
    public boolean allowMessages;
    public ComponentName[] conditionComponents;
    public Uri[] conditionIds;
    public Condition exitCondition;
    public ComponentName exitConditionComponent;
    public int sleepEndHour;
    public int sleepEndMinute;
    public String sleepMode;
    public int sleepStartHour;
    public int sleepStartMinute;

    public ZenModeConfig() {
    }

    public ZenModeConfig(Parcel source) {
        boolean z;
        boolean z2;
        boolean z3 = false;
        if (source.readInt() == 1) {
            z = true;
        } else {
            z = false;
        }
        this.allowCalls = z;
        if (source.readInt() == 1) {
            z2 = true;
        } else {
            z2 = false;
        }
        this.allowMessages = z2;
        this.allowEvents = source.readInt() == 1 ? true : z3;
        if (source.readInt() == 1) {
            this.sleepMode = source.readString();
        }
        this.sleepStartHour = source.readInt();
        this.sleepStartMinute = source.readInt();
        this.sleepEndHour = source.readInt();
        this.sleepEndMinute = source.readInt();
        int len = source.readInt();
        if (len > 0) {
            this.conditionComponents = new ComponentName[len];
            source.readTypedArray(this.conditionComponents, ComponentName.CREATOR);
        }
        int len2 = source.readInt();
        if (len2 > 0) {
            this.conditionIds = new Uri[len2];
            source.readTypedArray(this.conditionIds, Uri.CREATOR);
        }
        this.allowFrom = source.readInt();
        this.exitCondition = (Condition) source.readParcelable(null);
        this.exitConditionComponent = (ComponentName) source.readParcelable(null);
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        int i;
        int i2;
        dest.writeInt(this.allowCalls ? 1 : 0);
        if (this.allowMessages) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.allowEvents) {
            i2 = 1;
        } else {
            i2 = 0;
        }
        dest.writeInt(i2);
        if (this.sleepMode != null) {
            dest.writeInt(1);
            dest.writeString(this.sleepMode);
        } else {
            dest.writeInt(0);
        }
        dest.writeInt(this.sleepStartHour);
        dest.writeInt(this.sleepStartMinute);
        dest.writeInt(this.sleepEndHour);
        dest.writeInt(this.sleepEndMinute);
        if (this.conditionComponents == null || this.conditionComponents.length <= 0) {
            dest.writeInt(0);
        } else {
            dest.writeInt(this.conditionComponents.length);
            dest.writeTypedArray(this.conditionComponents, 0);
        }
        if (this.conditionIds == null || this.conditionIds.length <= 0) {
            dest.writeInt(0);
        } else {
            dest.writeInt(this.conditionIds.length);
            dest.writeTypedArray(this.conditionIds, 0);
        }
        dest.writeInt(this.allowFrom);
        dest.writeParcelable(this.exitCondition, 0);
        dest.writeParcelable(this.exitConditionComponent, 0);
    }

    public String toString() {
        String str = null;
        StringBuilder append = new StringBuilder(ZenModeConfig.class.getSimpleName()).append('[').append("allowCalls=").append(this.allowCalls).append(",allowMessages=").append(this.allowMessages).append(",allowFrom=").append(sourceToString(this.allowFrom)).append(",allowEvents=").append(this.allowEvents).append(",sleepMode=").append(this.sleepMode).append(",sleepStart=").append(this.sleepStartHour).append('.').append(this.sleepStartMinute).append(",sleepEnd=").append(this.sleepEndHour).append('.').append(this.sleepEndMinute).append(",conditionComponents=").append(this.conditionComponents == null ? null : TextUtils.join(",", this.conditionComponents)).append(",conditionIds=");
        if (this.conditionIds != null) {
            str = TextUtils.join(",", this.conditionIds);
        }
        return append.append(str).append(",exitCondition=").append(this.exitCondition).append(",exitConditionComponent=").append(this.exitConditionComponent).append(']').toString();
    }

    public static String sourceToString(int source) {
        switch (source) {
            case 0:
                return "anyone";
            case 1:
                return Contacts.AUTHORITY;
            case 2:
                return "stars";
            default:
                return IccCardConstants.INTENT_VALUE_ICC_UNKNOWN;
        }
    }

    public boolean equals(Object o) {
        if (!(o instanceof ZenModeConfig)) {
            return false;
        }
        if (o == this) {
            return true;
        }
        ZenModeConfig other = (ZenModeConfig) o;
        return other.allowCalls == this.allowCalls && other.allowMessages == this.allowMessages && other.allowFrom == this.allowFrom && other.allowEvents == this.allowEvents && Objects.equals(other.sleepMode, this.sleepMode) && other.sleepStartHour == this.sleepStartHour && other.sleepStartMinute == this.sleepStartMinute && other.sleepEndHour == this.sleepEndHour && other.sleepEndMinute == this.sleepEndMinute && Objects.deepEquals(other.conditionComponents, this.conditionComponents) && Objects.deepEquals(other.conditionIds, this.conditionIds) && Objects.equals(other.exitCondition, this.exitCondition) && Objects.equals(other.exitConditionComponent, this.exitConditionComponent);
    }

    public int hashCode() {
        return Objects.hash(Boolean.valueOf(this.allowCalls), Boolean.valueOf(this.allowMessages), Integer.valueOf(this.allowFrom), Boolean.valueOf(this.allowEvents), this.sleepMode, Integer.valueOf(this.sleepStartHour), Integer.valueOf(this.sleepStartMinute), Integer.valueOf(this.sleepEndHour), Integer.valueOf(this.sleepEndMinute), Integer.valueOf(Arrays.hashCode(this.conditionComponents)), Integer.valueOf(Arrays.hashCode(this.conditionIds)), this.exitCondition, this.exitConditionComponent);
    }

    public boolean isValid() {
        return isValidHour(this.sleepStartHour) && isValidMinute(this.sleepStartMinute) && isValidHour(this.sleepEndHour) && isValidMinute(this.sleepEndMinute) && isValidSleepMode(this.sleepMode);
    }

    public static boolean isValidSleepMode(String sleepMode2) {
        return sleepMode2 == null || sleepMode2.equals(SLEEP_MODE_NIGHTS) || sleepMode2.equals(SLEEP_MODE_WEEKNIGHTS) || tryParseDays(sleepMode2) != null;
    }

    public static int[] tryParseDays(String sleepMode2) {
        if (sleepMode2 == null) {
            return null;
        }
        String sleepMode3 = sleepMode2.trim();
        if (SLEEP_MODE_NIGHTS.equals(sleepMode3)) {
            return ALL_DAYS;
        }
        if (SLEEP_MODE_WEEKNIGHTS.equals(sleepMode3)) {
            return WEEKNIGHT_DAYS;
        }
        if (!sleepMode3.startsWith(SLEEP_MODE_DAYS_PREFIX)) {
            return null;
        }
        if (sleepMode3.equals(SLEEP_MODE_DAYS_PREFIX)) {
            return null;
        }
        String[] tokens = sleepMode3.substring(SLEEP_MODE_DAYS_PREFIX.length()).split(",");
        if (tokens.length == 0) {
            return null;
        }
        int[] rt = new int[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            int day = tryParseInt(tokens[i], -1);
            if (day == -1) {
                return null;
            }
            rt[i] = day;
        }
        return rt;
    }

    private static int tryParseInt(String value, int defValue) {
        if (TextUtils.isEmpty(value)) {
            return defValue;
        }
        try {
            return Integer.valueOf(value).intValue();
        } catch (NumberFormatException e) {
            return defValue;
        }
    }

    public static ZenModeConfig readXml(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != 2 || !ZEN_TAG.equals(parser.getName())) {
            return null;
        }
        ZenModeConfig rt = new ZenModeConfig();
        safeInt(parser, "version", 1);
        ArrayList<ComponentName> conditionComponents2 = new ArrayList<>();
        ArrayList<Uri> conditionIds2 = new ArrayList<>();
        while (true) {
            int type = parser.next();
            if (type != 1) {
                String tag = parser.getName();
                if (type != 3 || !ZEN_TAG.equals(tag)) {
                    if (type == 2) {
                        if (ALLOW_TAG.equals(tag)) {
                            rt.allowCalls = safeBoolean(parser, ALLOW_ATT_CALLS, false);
                            rt.allowMessages = safeBoolean(parser, ALLOW_ATT_MESSAGES, false);
                            rt.allowEvents = safeBoolean(parser, ALLOW_ATT_EVENTS, true);
                            rt.allowFrom = safeInt(parser, ALLOW_ATT_FROM, 0);
                            if (rt.allowFrom < 0 || rt.allowFrom > 2) {
                            }
                        } else if (SLEEP_TAG.equals(tag)) {
                            String mode = parser.getAttributeValue(null, "mode");
                            if (!isValidSleepMode(mode)) {
                                mode = null;
                            }
                            rt.sleepMode = mode;
                            int startHour = safeInt(parser, SLEEP_ATT_START_HR, 0);
                            int startMinute = safeInt(parser, SLEEP_ATT_START_MIN, 0);
                            int endHour = safeInt(parser, SLEEP_ATT_END_HR, 0);
                            int endMinute = safeInt(parser, SLEEP_ATT_END_MIN, 0);
                            if (!isValidHour(startHour)) {
                                startHour = 0;
                            }
                            rt.sleepStartHour = startHour;
                            if (!isValidMinute(startMinute)) {
                                startMinute = 0;
                            }
                            rt.sleepStartMinute = startMinute;
                            if (!isValidHour(endHour)) {
                                endHour = 0;
                            }
                            rt.sleepEndHour = endHour;
                            if (!isValidMinute(endMinute)) {
                                endMinute = 0;
                            }
                            rt.sleepEndMinute = endMinute;
                        } else if ("condition".equals(tag)) {
                            ComponentName component = safeComponentName(parser, CardEmulation.EXTRA_SERVICE_COMPONENT);
                            Uri conditionId = safeUri(parser, "id");
                            if (!(component == null || conditionId == null)) {
                                conditionComponents2.add(component);
                                conditionIds2.add(conditionId);
                            }
                        } else if (EXIT_CONDITION_TAG.equals(tag)) {
                            rt.exitCondition = readConditionXml(parser);
                            if (rt.exitCondition != null) {
                                rt.exitConditionComponent = safeComponentName(parser, CardEmulation.EXTRA_SERVICE_COMPONENT);
                            }
                        }
                    }
                } else if (conditionComponents2.isEmpty()) {
                    return rt;
                } else {
                    rt.conditionComponents = (ComponentName[]) conditionComponents2.toArray(new ComponentName[conditionComponents2.size()]);
                    rt.conditionIds = (Uri[]) conditionIds2.toArray(new Uri[conditionIds2.size()]);
                    return rt;
                }
            } else {
                throw new IllegalStateException("Failed to reach END_DOCUMENT");
            }
        }
        throw new IndexOutOfBoundsException("bad source in config:" + rt.allowFrom);
    }

    public void writeXml(XmlSerializer out) throws IOException {
        out.startTag(null, ZEN_TAG);
        out.attribute(null, "version", Integer.toString(1));
        out.startTag(null, ALLOW_TAG);
        out.attribute(null, ALLOW_ATT_CALLS, Boolean.toString(this.allowCalls));
        out.attribute(null, ALLOW_ATT_MESSAGES, Boolean.toString(this.allowMessages));
        out.attribute(null, ALLOW_ATT_EVENTS, Boolean.toString(this.allowEvents));
        out.attribute(null, ALLOW_ATT_FROM, Integer.toString(this.allowFrom));
        out.endTag(null, ALLOW_TAG);
        out.startTag(null, SLEEP_TAG);
        if (this.sleepMode != null) {
            out.attribute(null, "mode", this.sleepMode);
        }
        out.attribute(null, SLEEP_ATT_START_HR, Integer.toString(this.sleepStartHour));
        out.attribute(null, SLEEP_ATT_START_MIN, Integer.toString(this.sleepStartMinute));
        out.attribute(null, SLEEP_ATT_END_HR, Integer.toString(this.sleepEndHour));
        out.attribute(null, SLEEP_ATT_END_MIN, Integer.toString(this.sleepEndMinute));
        out.endTag(null, SLEEP_TAG);
        if (!(this.conditionComponents == null || this.conditionIds == null || this.conditionComponents.length != this.conditionIds.length)) {
            for (int i = 0; i < this.conditionComponents.length; i++) {
                out.startTag(null, "condition");
                out.attribute(null, CardEmulation.EXTRA_SERVICE_COMPONENT, this.conditionComponents[i].flattenToString());
                out.attribute(null, "id", this.conditionIds[i].toString());
                out.endTag(null, "condition");
            }
        }
        if (!(this.exitCondition == null || this.exitConditionComponent == null)) {
            out.startTag(null, EXIT_CONDITION_TAG);
            out.attribute(null, CardEmulation.EXTRA_SERVICE_COMPONENT, this.exitConditionComponent.flattenToString());
            writeConditionXml(this.exitCondition, out);
            out.endTag(null, EXIT_CONDITION_TAG);
        }
        out.endTag(null, ZEN_TAG);
    }

    public static Condition readConditionXml(XmlPullParser parser) {
        try {
            return new Condition(safeUri(parser, "id"), parser.getAttributeValue(null, "summary"), parser.getAttributeValue(null, CONDITION_ATT_LINE1), parser.getAttributeValue(null, CONDITION_ATT_LINE2), safeInt(parser, "icon", -1), safeInt(parser, "state", -1), safeInt(parser, "flags", -1));
        } catch (IllegalArgumentException e) {
            Slog.w(TAG, "Unable to read condition xml", e);
            return null;
        }
    }

    public static void writeConditionXml(Condition c, XmlSerializer out) throws IOException {
        out.attribute(null, "id", c.id.toString());
        out.attribute(null, "summary", c.summary);
        out.attribute(null, CONDITION_ATT_LINE1, c.line1);
        out.attribute(null, CONDITION_ATT_LINE2, c.line2);
        out.attribute(null, "icon", Integer.toString(c.icon));
        out.attribute(null, "state", Integer.toString(c.state));
        out.attribute(null, "flags", Integer.toString(c.flags));
    }

    public static boolean isValidHour(int val) {
        return val >= 0 && val < 24;
    }

    public static boolean isValidMinute(int val) {
        return val >= 0 && val < 60;
    }

    private static boolean safeBoolean(XmlPullParser parser, String att, boolean defValue) {
        String val = parser.getAttributeValue(null, att);
        return TextUtils.isEmpty(val) ? defValue : Boolean.valueOf(val).booleanValue();
    }

    private static int safeInt(XmlPullParser parser, String att, int defValue) {
        return tryParseInt(parser.getAttributeValue(null, att), defValue);
    }

    private static ComponentName safeComponentName(XmlPullParser parser, String att) {
        String val = parser.getAttributeValue(null, att);
        if (TextUtils.isEmpty(val)) {
            return null;
        }
        return ComponentName.unflattenFromString(val);
    }

    private static Uri safeUri(XmlPullParser parser, String att) {
        String val = parser.getAttributeValue(null, att);
        if (TextUtils.isEmpty(val)) {
            return null;
        }
        return Uri.parse(val);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public ZenModeConfig copy() {
        Parcel parcel = Parcel.obtain();
        try {
            writeToParcel(parcel, 0);
            parcel.setDataPosition(0);
            return new ZenModeConfig(parcel);
        } finally {
            parcel.recycle();
        }
    }

    public DowntimeInfo toDowntimeInfo() {
        DowntimeInfo downtime = new DowntimeInfo();
        downtime.startHour = this.sleepStartHour;
        downtime.startMinute = this.sleepStartMinute;
        downtime.endHour = this.sleepEndHour;
        downtime.endMinute = this.sleepEndMinute;
        return downtime;
    }

    public static Condition toTimeCondition(int minutesFromNow) {
        return toTimeCondition(System.currentTimeMillis() + (minutesFromNow == 0 ? 20000 : (long) (MINUTES_MS * minutesFromNow)), minutesFromNow);
    }

    public static Condition toTimeCondition(long time, int minutes) {
        int num = minutes < 60 ? minutes : Math.round(((float) minutes) / 60.0f);
        return new Condition(toCountdownConditionId(time), Resources.getSystem().getQuantityString(minutes < 60 ? R.plurals.zen_mode_duration_minutes : R.plurals.zen_mode_duration_hours, num, Integer.valueOf(num)), ProxyInfo.LOCAL_EXCL_LIST, ProxyInfo.LOCAL_EXCL_LIST, 0, 1, 1);
    }

    public static Uri toCountdownConditionId(long time) {
        return new Uri.Builder().scheme("condition").authority(SYSTEM_AUTHORITY).appendPath(COUNTDOWN_PATH).appendPath(Long.toString(time)).build();
    }

    public static long tryParseCountdownConditionId(Uri conditionId) {
        if (!Condition.isValidId(conditionId, SYSTEM_AUTHORITY) || conditionId.getPathSegments().size() != 2 || !COUNTDOWN_PATH.equals(conditionId.getPathSegments().get(0))) {
            return 0;
        }
        try {
            return Long.parseLong(conditionId.getPathSegments().get(1));
        } catch (RuntimeException e) {
            Slog.w(TAG, "Error parsing countdown condition: " + conditionId, e);
            return 0;
        }
    }

    public static boolean isValidCountdownConditionId(Uri conditionId) {
        return tryParseCountdownConditionId(conditionId) != 0;
    }

    public static Uri toDowntimeConditionId(DowntimeInfo downtime) {
        return new Uri.Builder().scheme("condition").authority(SYSTEM_AUTHORITY).appendPath(DOWNTIME_PATH).appendQueryParameter("start", downtime.startHour + "." + downtime.startMinute).appendQueryParameter("end", downtime.endHour + "." + downtime.endMinute).build();
    }

    public static DowntimeInfo tryParseDowntimeConditionId(Uri conditionId) {
        if (!Condition.isValidId(conditionId, SYSTEM_AUTHORITY) || conditionId.getPathSegments().size() != 1 || !DOWNTIME_PATH.equals(conditionId.getPathSegments().get(0))) {
            return null;
        }
        int[] start = tryParseHourAndMinute(conditionId.getQueryParameter("start"));
        int[] end = tryParseHourAndMinute(conditionId.getQueryParameter("end"));
        if (start == null || end == null) {
            return null;
        }
        DowntimeInfo downtime = new DowntimeInfo();
        downtime.startHour = start[0];
        downtime.startMinute = start[1];
        downtime.endHour = end[0];
        downtime.endMinute = end[1];
        return downtime;
    }

    private static int[] tryParseHourAndMinute(String value) {
        int i;
        if (TextUtils.isEmpty(value) || (i = value.indexOf(46)) < 1 || i >= value.length() - 1) {
            return null;
        }
        int hour = tryParseInt(value.substring(0, i), -1);
        int minute = tryParseInt(value.substring(i + 1), -1);
        if (!isValidHour(hour) || !isValidMinute(minute)) {
            return null;
        }
        return new int[]{hour, minute};
    }

    public static boolean isValidDowntimeConditionId(Uri conditionId) {
        return tryParseDowntimeConditionId(conditionId) != null;
    }

    public static class DowntimeInfo {
        public int endHour;
        public int endMinute;
        public int startHour;
        public int startMinute;

        public int hashCode() {
            return 0;
        }

        public boolean equals(Object o) {
            if (!(o instanceof DowntimeInfo)) {
                return false;
            }
            DowntimeInfo other = (DowntimeInfo) o;
            if (this.startHour == other.startHour && this.startMinute == other.startMinute && this.endHour == other.endHour && this.endMinute == other.endMinute) {
                return true;
            }
            return false;
        }
    }
}

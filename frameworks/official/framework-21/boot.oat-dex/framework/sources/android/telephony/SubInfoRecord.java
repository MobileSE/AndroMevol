package android.telephony;

import android.net.ProxyInfo;
import android.os.Parcel;
import android.os.Parcelable;

public class SubInfoRecord implements Parcelable {
    public static final Parcelable.Creator<SubInfoRecord> CREATOR = new Parcelable.Creator<SubInfoRecord>() {
        /* class android.telephony.SubInfoRecord.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public SubInfoRecord createFromParcel(Parcel source) {
            long subId = source.readLong();
            String iccId = source.readString();
            int slotId = source.readInt();
            String displayName = source.readString();
            int nameSource = source.readInt();
            int color = source.readInt();
            String number = source.readString();
            int displayNumberFormat = source.readInt();
            int dataRoaming = source.readInt();
            int[] iconRes = new int[2];
            source.readIntArray(iconRes);
            return new SubInfoRecord(subId, iccId, slotId, displayName, nameSource, color, number, displayNumberFormat, dataRoaming, iconRes, source.readInt(), source.readInt());
        }

        @Override // android.os.Parcelable.Creator
        public SubInfoRecord[] newArray(int size) {
            return new SubInfoRecord[size];
        }
    };
    public int color;
    public int dataRoaming;
    public String displayName;
    public int displayNumberFormat;
    public String iccId;
    public int mcc;
    public int mnc;
    public int nameSource;
    public String number;
    public int[] simIconRes;
    public int slotId;
    public long subId;

    public SubInfoRecord() {
        this.subId = -1000;
        this.iccId = ProxyInfo.LOCAL_EXCL_LIST;
        this.slotId = -1000;
        this.displayName = ProxyInfo.LOCAL_EXCL_LIST;
        this.nameSource = 0;
        this.color = 0;
        this.number = ProxyInfo.LOCAL_EXCL_LIST;
        this.displayNumberFormat = 0;
        this.dataRoaming = 0;
        this.simIconRes = new int[2];
        this.mcc = 0;
        this.mnc = 0;
    }

    public SubInfoRecord(long subId2, String iccId2, int slotId2, String displayName2, int nameSource2, int color2, String number2, int displayFormat, int roaming, int[] iconRes, int mcc2, int mnc2) {
        this.subId = subId2;
        this.iccId = iccId2;
        this.slotId = slotId2;
        this.displayName = displayName2;
        this.nameSource = nameSource2;
        this.color = color2;
        this.number = number2;
        this.displayNumberFormat = displayFormat;
        this.dataRoaming = roaming;
        this.simIconRes = iconRes;
        this.mcc = mcc2;
        this.mnc = mnc2;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.subId);
        dest.writeString(this.iccId);
        dest.writeInt(this.slotId);
        dest.writeString(this.displayName);
        dest.writeInt(this.nameSource);
        dest.writeInt(this.color);
        dest.writeString(this.number);
        dest.writeInt(this.displayNumberFormat);
        dest.writeInt(this.dataRoaming);
        dest.writeIntArray(this.simIconRes);
        dest.writeInt(this.mcc);
        dest.writeInt(this.mnc);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public String toString() {
        return "{mSubId=" + this.subId + ", mIccId=" + this.iccId + " mSlotId=" + this.slotId + " mDisplayName=" + this.displayName + " mNameSource=" + this.nameSource + " mColor=" + this.color + " mNumber=" + this.number + " mDisplayNumberFormat=" + this.displayNumberFormat + " mDataRoaming=" + this.dataRoaming + " mSimIconRes=" + this.simIconRes + " mMcc " + this.mcc + " mMnc " + this.mnc + "}";
    }
}

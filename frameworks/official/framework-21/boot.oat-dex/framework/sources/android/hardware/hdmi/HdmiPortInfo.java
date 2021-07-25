package android.hardware.hdmi;

import android.os.Parcel;
import android.os.Parcelable;

public final class HdmiPortInfo implements Parcelable {
    public static final Parcelable.Creator<HdmiPortInfo> CREATOR = new Parcelable.Creator<HdmiPortInfo>() {
        /* class android.hardware.hdmi.HdmiPortInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public HdmiPortInfo createFromParcel(Parcel source) {
            boolean cec;
            boolean arc;
            boolean mhl;
            int id = source.readInt();
            int type = source.readInt();
            int address = source.readInt();
            if (source.readInt() == 1) {
                cec = true;
            } else {
                cec = false;
            }
            if (source.readInt() == 1) {
                arc = true;
            } else {
                arc = false;
            }
            if (source.readInt() == 1) {
                mhl = true;
            } else {
                mhl = false;
            }
            return new HdmiPortInfo(id, type, address, cec, arc, mhl);
        }

        @Override // android.os.Parcelable.Creator
        public HdmiPortInfo[] newArray(int size) {
            return new HdmiPortInfo[size];
        }
    };
    public static final int PORT_INPUT = 0;
    public static final int PORT_OUTPUT = 1;
    private final int mAddress;
    private final boolean mArcSupported;
    private final boolean mCecSupported;
    private final int mId;
    private final boolean mMhlSupported;
    private final int mType;

    public HdmiPortInfo(int id, int type, int address, boolean cec, boolean mhl, boolean arc) {
        this.mId = id;
        this.mType = type;
        this.mAddress = address;
        this.mCecSupported = cec;
        this.mArcSupported = arc;
        this.mMhlSupported = mhl;
    }

    public int getId() {
        return this.mId;
    }

    public int getType() {
        return this.mType;
    }

    public int getAddress() {
        return this.mAddress;
    }

    public boolean isCecSupported() {
        return this.mCecSupported;
    }

    public boolean isMhlSupported() {
        return this.mMhlSupported;
    }

    public boolean isArcSupported() {
        return this.mArcSupported;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        int i;
        int i2;
        int i3 = 1;
        dest.writeInt(this.mId);
        dest.writeInt(this.mType);
        dest.writeInt(this.mAddress);
        if (this.mCecSupported) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.mArcSupported) {
            i2 = 1;
        } else {
            i2 = 0;
        }
        dest.writeInt(i2);
        if (!this.mMhlSupported) {
            i3 = 0;
        }
        dest.writeInt(i3);
    }

    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append("port_id: ").append(this.mId).append(", ");
        s.append("address: ").append(String.format("0x%04x", Integer.valueOf(this.mAddress))).append(", ");
        s.append("cec: ").append(this.mCecSupported).append(", ");
        s.append("arc: ").append(this.mArcSupported).append(", ");
        s.append("mhl: ").append(this.mMhlSupported);
        return s.toString();
    }
}

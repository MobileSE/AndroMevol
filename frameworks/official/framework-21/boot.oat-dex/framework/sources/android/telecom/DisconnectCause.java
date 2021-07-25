package android.telecom;

import android.net.ProxyInfo;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.CalendarContract;
import android.text.TextUtils;
import java.util.Objects;

public final class DisconnectCause implements Parcelable {
    public static final int BUSY = 7;
    public static final int CANCELED = 4;
    public static final Parcelable.Creator<DisconnectCause> CREATOR = new Parcelable.Creator<DisconnectCause>() {
        /* class android.telecom.DisconnectCause.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DisconnectCause createFromParcel(Parcel source) {
            return new DisconnectCause(source.readInt(), TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source), TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source), source.readString(), source.readInt());
        }

        @Override // android.os.Parcelable.Creator
        public DisconnectCause[] newArray(int size) {
            return new DisconnectCause[size];
        }
    };
    public static final int ERROR = 1;
    public static final int LOCAL = 2;
    public static final int MISSED = 5;
    public static final int OTHER = 9;
    public static final int REJECTED = 6;
    public static final int REMOTE = 3;
    public static final int RESTRICTED = 8;
    public static final int UNKNOWN = 0;
    private int mDisconnectCode;
    private CharSequence mDisconnectDescription;
    private CharSequence mDisconnectLabel;
    private String mDisconnectReason;
    private int mToneToPlay;

    public DisconnectCause(int code) {
        this(code, null, null, null, -1);
    }

    public DisconnectCause(int code, String reason) {
        this(code, null, null, reason, -1);
    }

    public DisconnectCause(int code, CharSequence label, CharSequence description, String reason) {
        this(code, label, description, reason, -1);
    }

    public DisconnectCause(int code, CharSequence label, CharSequence description, String reason, int toneToPlay) {
        this.mDisconnectCode = code;
        this.mDisconnectLabel = label;
        this.mDisconnectDescription = description;
        this.mDisconnectReason = reason;
        this.mToneToPlay = toneToPlay;
    }

    public int getCode() {
        return this.mDisconnectCode;
    }

    public CharSequence getLabel() {
        return this.mDisconnectLabel;
    }

    public CharSequence getDescription() {
        return this.mDisconnectDescription;
    }

    public String getReason() {
        return this.mDisconnectReason;
    }

    public int getTone() {
        return this.mToneToPlay;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel destination, int flags) {
        destination.writeInt(this.mDisconnectCode);
        TextUtils.writeToParcel(this.mDisconnectLabel, destination, flags);
        TextUtils.writeToParcel(this.mDisconnectDescription, destination, flags);
        destination.writeString(this.mDisconnectReason);
        destination.writeInt(this.mToneToPlay);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public int hashCode() {
        return Objects.hashCode(Integer.valueOf(this.mDisconnectCode)) + Objects.hashCode(this.mDisconnectLabel) + Objects.hashCode(this.mDisconnectDescription) + Objects.hashCode(this.mDisconnectReason) + Objects.hashCode(Integer.valueOf(this.mToneToPlay));
    }

    public boolean equals(Object o) {
        if (!(o instanceof DisconnectCause)) {
            return false;
        }
        DisconnectCause d = (DisconnectCause) o;
        if (!Objects.equals(Integer.valueOf(this.mDisconnectCode), Integer.valueOf(d.getCode())) || !Objects.equals(this.mDisconnectLabel, d.getLabel()) || !Objects.equals(this.mDisconnectDescription, d.getDescription()) || !Objects.equals(this.mDisconnectReason, d.getReason()) || !Objects.equals(Integer.valueOf(this.mToneToPlay), Integer.valueOf(d.getTone()))) {
            return false;
        }
        return true;
    }

    public String toString() {
        String code;
        switch (getCode()) {
            case 1:
                code = "ERROR";
                break;
            case 2:
                code = CalendarContract.ACCOUNT_TYPE_LOCAL;
                break;
            case 3:
                code = "REMOTE";
                break;
            case 4:
            default:
                code = "UNKNOWN";
                break;
            case 5:
                code = "MISSED";
                break;
            case 6:
                code = "REJECTED";
                break;
            case 7:
                code = "BUSY";
                break;
            case 8:
                code = "RESTRICTED";
                break;
            case 9:
                code = "OTHER";
                break;
        }
        String label = this.mDisconnectLabel == null ? ProxyInfo.LOCAL_EXCL_LIST : this.mDisconnectLabel.toString();
        return "DisconnectCause [ Code: (" + code + ")" + " Label: (" + label + ")" + " Description: (" + (this.mDisconnectDescription == null ? ProxyInfo.LOCAL_EXCL_LIST : this.mDisconnectDescription.toString()) + ")" + " Reason: (" + (this.mDisconnectReason == null ? ProxyInfo.LOCAL_EXCL_LIST : this.mDisconnectReason) + ")" + " Tone: (" + this.mToneToPlay + ") ]";
    }
}

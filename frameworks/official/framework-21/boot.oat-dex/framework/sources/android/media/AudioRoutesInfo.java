package android.media;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public class AudioRoutesInfo implements Parcelable {
    public static final Parcelable.Creator<AudioRoutesInfo> CREATOR = new Parcelable.Creator<AudioRoutesInfo>() {
        /* class android.media.AudioRoutesInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AudioRoutesInfo createFromParcel(Parcel in) {
            return new AudioRoutesInfo(in);
        }

        @Override // android.os.Parcelable.Creator
        public AudioRoutesInfo[] newArray(int size) {
            return new AudioRoutesInfo[size];
        }
    };
    static final int MAIN_DOCK_SPEAKERS = 4;
    static final int MAIN_HDMI = 8;
    static final int MAIN_HEADPHONES = 2;
    static final int MAIN_HEADSET = 1;
    static final int MAIN_SPEAKER = 0;
    CharSequence mBluetoothName;
    int mMainType = 0;

    public AudioRoutesInfo() {
    }

    public AudioRoutesInfo(AudioRoutesInfo o) {
        this.mBluetoothName = o.mBluetoothName;
        this.mMainType = o.mMainType;
    }

    AudioRoutesInfo(Parcel src) {
        this.mBluetoothName = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(src);
        this.mMainType = src.readInt();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        TextUtils.writeToParcel(this.mBluetoothName, dest, flags);
        dest.writeInt(this.mMainType);
    }
}

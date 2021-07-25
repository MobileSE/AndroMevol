package android.nfc;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public final class BeamShareData implements Parcelable {
    public static final Parcelable.Creator<BeamShareData> CREATOR = new Parcelable.Creator<BeamShareData>() {
        /* class android.nfc.BeamShareData.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public BeamShareData createFromParcel(Parcel source) {
            Uri[] uris = null;
            NdefMessage msg = (NdefMessage) source.readParcelable(NdefMessage.class.getClassLoader());
            int numUris = source.readInt();
            if (numUris > 0) {
                uris = new Uri[numUris];
                source.readTypedArray(uris, Uri.CREATOR);
            }
            return new BeamShareData(msg, uris, source.readInt());
        }

        @Override // android.os.Parcelable.Creator
        public BeamShareData[] newArray(int size) {
            return new BeamShareData[size];
        }
    };
    public final int flags;
    public final NdefMessage ndefMessage;
    public final Uri[] uris;

    public BeamShareData(NdefMessage msg, Uri[] uris2, int flags2) {
        this.ndefMessage = msg;
        this.uris = uris2;
        this.flags = flags2;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags2) {
        int urisLength;
        if (this.uris != null) {
            urisLength = this.uris.length;
        } else {
            urisLength = 0;
        }
        dest.writeParcelable(this.ndefMessage, 0);
        dest.writeInt(urisLength);
        if (urisLength > 0) {
            dest.writeTypedArray(this.uris, 0);
        }
        dest.writeInt(this.flags);
    }
}

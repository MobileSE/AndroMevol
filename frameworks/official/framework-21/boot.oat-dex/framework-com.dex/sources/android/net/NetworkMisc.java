package android.net;

import android.os.Parcel;
import android.os.Parcelable;

public class NetworkMisc implements Parcelable {
    public static final Parcelable.Creator<NetworkMisc> CREATOR = new Parcelable.Creator<NetworkMisc>() {
        /* class android.net.NetworkMisc.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public NetworkMisc createFromParcel(Parcel in) {
            boolean z;
            boolean z2 = true;
            NetworkMisc networkMisc = new NetworkMisc();
            if (in.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            networkMisc.allowBypass = z;
            if (in.readInt() == 0) {
                z2 = false;
            }
            networkMisc.explicitlySelected = z2;
            return networkMisc;
        }

        @Override // android.os.Parcelable.Creator
        public NetworkMisc[] newArray(int size) {
            return new NetworkMisc[size];
        }
    };
    public boolean allowBypass;
    public boolean explicitlySelected;

    public NetworkMisc() {
    }

    public NetworkMisc(NetworkMisc nm) {
        if (nm != null) {
            this.allowBypass = nm.allowBypass;
            this.explicitlySelected = nm.explicitlySelected;
        }
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        int i;
        int i2 = 1;
        if (this.allowBypass) {
            i = 1;
        } else {
            i = 0;
        }
        out.writeInt(i);
        if (!this.explicitlySelected) {
            i2 = 0;
        }
        out.writeInt(i2);
    }
}

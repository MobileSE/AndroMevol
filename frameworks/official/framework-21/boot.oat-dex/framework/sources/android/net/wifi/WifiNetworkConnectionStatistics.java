package android.net.wifi;

import android.os.Parcel;
import android.os.Parcelable;

public class WifiNetworkConnectionStatistics implements Parcelable {
    public static final Parcelable.Creator<WifiNetworkConnectionStatistics> CREATOR = new Parcelable.Creator<WifiNetworkConnectionStatistics>() {
        /* class android.net.wifi.WifiNetworkConnectionStatistics.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public WifiNetworkConnectionStatistics createFromParcel(Parcel in) {
            return new WifiNetworkConnectionStatistics(in.readInt(), in.readInt());
        }

        @Override // android.os.Parcelable.Creator
        public WifiNetworkConnectionStatistics[] newArray(int size) {
            return new WifiNetworkConnectionStatistics[size];
        }
    };
    private static final String TAG = "WifiNetworkConnnectionStatistics";
    public int numConnection;
    public int numUsage;

    public WifiNetworkConnectionStatistics(int connection, int usage) {
        this.numConnection = connection;
        this.numUsage = usage;
    }

    public WifiNetworkConnectionStatistics() {
    }

    public String toString() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("c=").append(this.numConnection);
        sbuf.append(" u=").append(this.numUsage);
        return sbuf.toString();
    }

    public WifiNetworkConnectionStatistics(WifiNetworkConnectionStatistics source) {
        this.numConnection = source.numConnection;
        this.numUsage = source.numUsage;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.numConnection);
        dest.writeInt(this.numUsage);
    }
}

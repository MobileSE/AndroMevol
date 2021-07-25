package android.net.wifi;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import java.util.HashMap;

public class WifiConnectionStatistics implements Parcelable {
    public static final Parcelable.Creator<WifiConnectionStatistics> CREATOR = new Parcelable.Creator<WifiConnectionStatistics>() {
        /* class android.net.wifi.WifiConnectionStatistics.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public WifiConnectionStatistics createFromParcel(Parcel in) {
            WifiConnectionStatistics stats = new WifiConnectionStatistics();
            stats.num24GhzConnected = in.readInt();
            stats.num5GhzConnected = in.readInt();
            stats.numAutoJoinAttempt = in.readInt();
            stats.numAutoRoamAttempt = in.readInt();
            stats.numWifiManagerJoinAttempt = in.readInt();
            int n = in.readInt();
            while (true) {
                n--;
                if (n <= 0) {
                    return stats;
                }
                stats.untrustedNetworkHistory.put(in.readString(), new WifiNetworkConnectionStatistics(in.readInt(), in.readInt()));
            }
        }

        @Override // android.os.Parcelable.Creator
        public WifiConnectionStatistics[] newArray(int size) {
            return new WifiConnectionStatistics[size];
        }
    };
    private static final String TAG = "WifiConnnectionStatistics";
    public int num24GhzConnected;
    public int num5GhzConnected;
    public int numAutoJoinAttempt;
    public int numAutoRoamAttempt;
    public int numWifiManagerJoinAttempt;
    public HashMap<String, WifiNetworkConnectionStatistics> untrustedNetworkHistory = new HashMap<>();

    public WifiConnectionStatistics() {
    }

    public void incrementOrAddUntrusted(String SSID, int connection, int usage) {
        WifiNetworkConnectionStatistics stats;
        if (!TextUtils.isEmpty(SSID)) {
            if (this.untrustedNetworkHistory.containsKey(SSID)) {
                stats = this.untrustedNetworkHistory.get(SSID);
                if (stats != null) {
                    stats.numConnection += connection;
                    stats.numUsage += usage;
                }
            } else {
                stats = new WifiNetworkConnectionStatistics(connection, usage);
            }
            if (stats != null) {
                this.untrustedNetworkHistory.put(SSID, stats);
            }
        }
    }

    public String toString() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("Connected on: 2.4Ghz=").append(this.num24GhzConnected);
        sbuf.append(" 5Ghz=").append(this.num5GhzConnected).append("\n");
        sbuf.append(" join=").append(this.numWifiManagerJoinAttempt);
        sbuf.append("\\").append(this.numAutoJoinAttempt).append("\n");
        sbuf.append(" roam=").append(this.numAutoRoamAttempt).append("\n");
        for (String Key : this.untrustedNetworkHistory.keySet()) {
            WifiNetworkConnectionStatistics stats = this.untrustedNetworkHistory.get(Key);
            if (stats != null) {
                sbuf.append(Key).append(" ").append(stats.toString()).append("\n");
            }
        }
        return sbuf.toString();
    }

    public WifiConnectionStatistics(WifiConnectionStatistics source) {
        if (source != null) {
            this.untrustedNetworkHistory.putAll(source.untrustedNetworkHistory);
        }
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.num24GhzConnected);
        dest.writeInt(this.num5GhzConnected);
        dest.writeInt(this.numAutoJoinAttempt);
        dest.writeInt(this.numAutoRoamAttempt);
        dest.writeInt(this.numWifiManagerJoinAttempt);
        dest.writeInt(this.untrustedNetworkHistory.size());
        for (String Key : this.untrustedNetworkHistory.keySet()) {
            WifiNetworkConnectionStatistics num = this.untrustedNetworkHistory.get(Key);
            dest.writeString(Key);
            dest.writeInt(num.numConnection);
            dest.writeInt(num.numUsage);
        }
    }
}

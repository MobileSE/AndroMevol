package android.net.wifi.p2p.nsd;

import android.os.BatteryStats;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

public class WifiP2pServiceInfo implements Parcelable {
    public static final Parcelable.Creator<WifiP2pServiceInfo> CREATOR = new Parcelable.Creator<WifiP2pServiceInfo>() {
        /* class android.net.wifi.p2p.nsd.WifiP2pServiceInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public WifiP2pServiceInfo createFromParcel(Parcel in) {
            List<String> data = new ArrayList<>();
            in.readStringList(data);
            return new WifiP2pServiceInfo(data);
        }

        @Override // android.os.Parcelable.Creator
        public WifiP2pServiceInfo[] newArray(int size) {
            return new WifiP2pServiceInfo[size];
        }
    };
    public static final int SERVICE_TYPE_ALL = 0;
    public static final int SERVICE_TYPE_BONJOUR = 1;
    public static final int SERVICE_TYPE_UPNP = 2;
    public static final int SERVICE_TYPE_VENDOR_SPECIFIC = 255;
    public static final int SERVICE_TYPE_WS_DISCOVERY = 3;
    private List<String> mQueryList;

    protected WifiP2pServiceInfo(List<String> queryList) {
        if (queryList == null) {
            throw new IllegalArgumentException("query list cannot be null");
        }
        this.mQueryList = queryList;
    }

    public List<String> getSupplicantQueryList() {
        return this.mQueryList;
    }

    static String bin2HexStr(byte[] data) {
        StringBuffer sb = new StringBuffer();
        for (byte b : data) {
            try {
                String s = Integer.toHexString(b & BatteryStats.HistoryItem.CMD_NULL);
                if (s.length() == 1) {
                    sb.append('0');
                }
                sb.append(s);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return sb.toString();
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof WifiP2pServiceInfo)) {
            return false;
        }
        return this.mQueryList.equals(((WifiP2pServiceInfo) o).mQueryList);
    }

    public int hashCode() {
        return (this.mQueryList == null ? 0 : this.mQueryList.hashCode()) + 527;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringList(this.mQueryList);
    }
}

package android.net.wifi;

import android.os.Parcel;
import android.os.Parcelable;

public class ScanResult implements Parcelable {
    public static final int AUTHENTICATION_ERROR = 128;
    public static final int AUTO_JOIN_DISABLED = 32;
    public static final int AUTO_ROAM_DISABLED = 16;
    public static final Parcelable.Creator<ScanResult> CREATOR = new Parcelable.Creator<ScanResult>() {
        /* class android.net.wifi.ScanResult.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ScanResult createFromParcel(Parcel in) {
            WifiSsid wifiSsid = null;
            if (in.readInt() == 1) {
                wifiSsid = WifiSsid.CREATOR.createFromParcel(in);
            }
            ScanResult sr = new ScanResult(wifiSsid, in.readString(), in.readString(), in.readInt(), in.readInt(), in.readLong(), in.readInt(), in.readInt());
            sr.seen = in.readLong();
            sr.autoJoinStatus = in.readInt();
            sr.untrusted = in.readInt() != 0;
            sr.numConnection = in.readInt();
            sr.numUsage = in.readInt();
            sr.numIpConfigFailures = in.readInt();
            sr.isAutoJoinCandidate = in.readInt();
            int n = in.readInt();
            if (n != 0) {
                sr.informationElements = new InformationElement[n];
                for (int i = 0; i < n; i++) {
                    sr.informationElements[i] = new InformationElement();
                    sr.informationElements[i].id = in.readInt();
                    sr.informationElements[i].bytes = new byte[in.readInt()];
                    in.readByteArray(sr.informationElements[i].bytes);
                }
            }
            return sr;
        }

        @Override // android.os.Parcelable.Creator
        public ScanResult[] newArray(int size) {
            return new ScanResult[size];
        }
    };
    public static final int ENABLED = 0;
    public static final int UNSPECIFIED = -1;
    public String BSSID;
    public String SSID;
    public int autoJoinStatus;
    public long blackListTimestamp;
    public String capabilities;
    public int distanceCm;
    public int distanceSdCm;
    public int frequency;
    public InformationElement[] informationElements;
    public int isAutoJoinCandidate;
    public int level;
    public int numConnection;
    public int numIpConfigFailures;
    public int numUsage;
    public long seen;
    public long timestamp;
    public boolean untrusted;
    public WifiSsid wifiSsid;

    public void averageRssi(int previousRssi, long previousSeen, int maxAge) {
        if (this.seen == 0) {
            this.seen = System.currentTimeMillis();
        }
        long age = this.seen - previousSeen;
        if (previousSeen > 0 && age > 0 && age < ((long) (maxAge / 2))) {
            double alpha = 0.5d - (((double) age) / ((double) maxAge));
            this.level = (int) ((((double) this.level) * (1.0d - alpha)) + (((double) previousRssi) * alpha));
        }
    }

    public void setAutoJoinStatus(int status) {
        if (status < 0) {
            status = 0;
        }
        if (status == 0) {
            this.blackListTimestamp = 0;
        } else if (status > this.autoJoinStatus) {
            this.blackListTimestamp = System.currentTimeMillis();
        }
        this.autoJoinStatus = status;
    }

    public boolean is24GHz() {
        return is24GHz(this.frequency);
    }

    public static boolean is24GHz(int freq) {
        return freq > 2400 && freq < 2500;
    }

    public boolean is5GHz() {
        return is5GHz(this.frequency);
    }

    public static boolean is5GHz(int freq) {
        return freq > 4900 && freq < 5900;
    }

    public static class InformationElement {
        public byte[] bytes;
        public int id;

        public InformationElement() {
        }

        public InformationElement(InformationElement rhs) {
            this.id = rhs.id;
            this.bytes = (byte[]) rhs.bytes.clone();
        }
    }

    public ScanResult(WifiSsid wifiSsid2, String BSSID2, String caps, int level2, int frequency2, long tsf) {
        this.wifiSsid = wifiSsid2;
        this.SSID = wifiSsid2 != null ? wifiSsid2.toString() : WifiSsid.NONE;
        this.BSSID = BSSID2;
        this.capabilities = caps;
        this.level = level2;
        this.frequency = frequency2;
        this.timestamp = tsf;
        this.distanceCm = -1;
        this.distanceSdCm = -1;
    }

    public ScanResult(WifiSsid wifiSsid2, String BSSID2, String caps, int level2, int frequency2, long tsf, int distCm, int distSdCm) {
        this.wifiSsid = wifiSsid2;
        this.SSID = wifiSsid2 != null ? wifiSsid2.toString() : WifiSsid.NONE;
        this.BSSID = BSSID2;
        this.capabilities = caps;
        this.level = level2;
        this.frequency = frequency2;
        this.timestamp = tsf;
        this.distanceCm = distCm;
        this.distanceSdCm = distSdCm;
    }

    public ScanResult(ScanResult source) {
        if (source != null) {
            this.wifiSsid = source.wifiSsid;
            this.SSID = source.SSID;
            this.BSSID = source.BSSID;
            this.capabilities = source.capabilities;
            this.level = source.level;
            this.frequency = source.frequency;
            this.timestamp = source.timestamp;
            this.distanceCm = source.distanceCm;
            this.distanceSdCm = source.distanceSdCm;
            this.seen = source.seen;
            this.autoJoinStatus = source.autoJoinStatus;
            this.untrusted = source.untrusted;
            this.numConnection = source.numConnection;
            this.numUsage = source.numUsage;
            this.numIpConfigFailures = source.numIpConfigFailures;
            this.isAutoJoinCandidate = source.isAutoJoinCandidate;
        }
    }

    public ScanResult() {
    }

    public String toString() {
        String str;
        StringBuffer sb = new StringBuffer();
        String none = "<none>";
        StringBuffer append = sb.append("SSID: ").append(this.wifiSsid == null ? WifiSsid.NONE : this.wifiSsid).append(", BSSID: ");
        if (this.BSSID == null) {
            str = none;
        } else {
            str = this.BSSID;
        }
        StringBuffer append2 = append.append(str).append(", capabilities: ");
        if (this.capabilities != null) {
            none = this.capabilities;
        }
        append2.append(none).append(", level: ").append(this.level).append(", frequency: ").append(this.frequency).append(", timestamp: ").append(this.timestamp);
        sb.append(", distance: ").append(this.distanceCm != -1 ? Integer.valueOf(this.distanceCm) : "?").append("(cm)");
        sb.append(", distanceSd: ").append(this.distanceSdCm != -1 ? Integer.valueOf(this.distanceSdCm) : "?").append("(cm)");
        if (this.autoJoinStatus != 0) {
            sb.append(", status: ").append(this.autoJoinStatus);
        }
        return sb.toString();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        int i = 1;
        if (this.wifiSsid != null) {
            dest.writeInt(1);
            this.wifiSsid.writeToParcel(dest, flags);
        } else {
            dest.writeInt(0);
        }
        dest.writeString(this.BSSID);
        dest.writeString(this.capabilities);
        dest.writeInt(this.level);
        dest.writeInt(this.frequency);
        dest.writeLong(this.timestamp);
        dest.writeInt(this.distanceCm);
        dest.writeInt(this.distanceSdCm);
        dest.writeLong(this.seen);
        dest.writeInt(this.autoJoinStatus);
        if (!this.untrusted) {
            i = 0;
        }
        dest.writeInt(i);
        dest.writeInt(this.numConnection);
        dest.writeInt(this.numUsage);
        dest.writeInt(this.numIpConfigFailures);
        dest.writeInt(this.isAutoJoinCandidate);
        if (this.informationElements != null) {
            dest.writeInt(this.informationElements.length);
            for (int i2 = 0; i2 < this.informationElements.length; i2++) {
                dest.writeInt(this.informationElements[i2].id);
                dest.writeInt(this.informationElements[i2].bytes.length);
                dest.writeByteArray(this.informationElements[i2].bytes);
            }
            return;
        }
        dest.writeInt(0);
    }
}

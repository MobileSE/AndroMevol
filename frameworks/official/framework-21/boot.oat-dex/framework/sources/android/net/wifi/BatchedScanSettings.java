package android.net.wifi;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class BatchedScanSettings implements Parcelable {
    public static final Parcelable.Creator<BatchedScanSettings> CREATOR = new Parcelable.Creator<BatchedScanSettings>() {
        /* class android.net.wifi.BatchedScanSettings.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public BatchedScanSettings createFromParcel(Parcel in) {
            BatchedScanSettings settings = new BatchedScanSettings();
            settings.maxScansPerBatch = in.readInt();
            settings.maxApPerScan = in.readInt();
            settings.scanIntervalSec = in.readInt();
            settings.maxApForDistance = in.readInt();
            int channelCount = in.readInt();
            if (channelCount > 0) {
                settings.channelSet = new ArrayList(channelCount);
                while (true) {
                    channelCount--;
                    if (channelCount <= 0) {
                        break;
                    }
                    settings.channelSet.add(in.readString());
                }
            }
            return settings;
        }

        @Override // android.os.Parcelable.Creator
        public BatchedScanSettings[] newArray(int size) {
            return new BatchedScanSettings[size];
        }
    };
    public static final int DEFAULT_AP_FOR_DISTANCE = 0;
    public static final int DEFAULT_AP_PER_SCAN = 16;
    public static final int DEFAULT_INTERVAL_SEC = 30;
    public static final int DEFAULT_SCANS_PER_BATCH = 20;
    public static final int MAX_AP_FOR_DISTANCE = 16;
    public static final int MAX_AP_PER_SCAN = 16;
    public static final int MAX_INTERVAL_SEC = 500;
    public static final int MAX_SCANS_PER_BATCH = 20;
    public static final int MAX_WIFI_CHANNEL = 196;
    public static final int MIN_AP_FOR_DISTANCE = 0;
    public static final int MIN_AP_PER_SCAN = 2;
    public static final int MIN_INTERVAL_SEC = 10;
    public static final int MIN_SCANS_PER_BATCH = 2;
    private static final String TAG = "BatchedScanSettings";
    public static final int UNSPECIFIED = Integer.MAX_VALUE;
    public Collection<String> channelSet;
    public int maxApForDistance;
    public int maxApPerScan;
    public int maxScansPerBatch;
    public int scanIntervalSec;

    public BatchedScanSettings() {
        clear();
    }

    public void clear() {
        this.maxScansPerBatch = Integer.MAX_VALUE;
        this.maxApPerScan = Integer.MAX_VALUE;
        this.channelSet = null;
        this.scanIntervalSec = Integer.MAX_VALUE;
        this.maxApForDistance = Integer.MAX_VALUE;
    }

    public BatchedScanSettings(BatchedScanSettings source) {
        this.maxScansPerBatch = source.maxScansPerBatch;
        this.maxApPerScan = source.maxApPerScan;
        if (source.channelSet != null) {
            this.channelSet = new ArrayList(source.channelSet);
        }
        this.scanIntervalSec = source.scanIntervalSec;
        this.maxApForDistance = source.maxApForDistance;
    }

    private boolean channelSetIsValid() {
        if (this.channelSet == null || this.channelSet.isEmpty()) {
            return true;
        }
        for (String channel : this.channelSet) {
            try {
                int i = Integer.parseInt(channel);
                if (i > 0 && i <= 196) {
                }
            } catch (NumberFormatException e) {
            }
            if (!channel.equals("A") && !channel.equals("B")) {
                return false;
            }
        }
        return true;
    }

    public boolean isInvalid() {
        if (this.maxScansPerBatch != Integer.MAX_VALUE && (this.maxScansPerBatch < 2 || this.maxScansPerBatch > 20)) {
            return true;
        }
        if ((this.maxApPerScan != Integer.MAX_VALUE && (this.maxApPerScan < 2 || this.maxApPerScan > 16)) || !channelSetIsValid()) {
            return true;
        }
        if (this.scanIntervalSec != Integer.MAX_VALUE && (this.scanIntervalSec < 10 || this.scanIntervalSec > 500)) {
            return true;
        }
        if (this.maxApForDistance == Integer.MAX_VALUE || (this.maxApForDistance >= 0 && this.maxApForDistance <= 16)) {
            return false;
        }
        return true;
    }

    public void constrain() {
        if (this.scanIntervalSec == Integer.MAX_VALUE) {
            this.scanIntervalSec = 30;
        } else if (this.scanIntervalSec < 10) {
            this.scanIntervalSec = 10;
        } else if (this.scanIntervalSec > 500) {
            this.scanIntervalSec = 500;
        }
        if (this.maxScansPerBatch == Integer.MAX_VALUE) {
            this.maxScansPerBatch = 20;
        } else if (this.maxScansPerBatch < 2) {
            this.maxScansPerBatch = 2;
        } else if (this.maxScansPerBatch > 20) {
            this.maxScansPerBatch = 20;
        }
        if (this.maxApPerScan == Integer.MAX_VALUE) {
            this.maxApPerScan = 16;
        } else if (this.maxApPerScan < 2) {
            this.maxApPerScan = 2;
        } else if (this.maxApPerScan > 16) {
            this.maxApPerScan = 16;
        }
        if (this.maxApForDistance == Integer.MAX_VALUE) {
            this.maxApForDistance = 0;
        } else if (this.maxApForDistance < 0) {
            this.maxApForDistance = 0;
        } else if (this.maxApForDistance > 16) {
            this.maxApForDistance = 16;
        }
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof BatchedScanSettings)) {
            return false;
        }
        BatchedScanSettings o = (BatchedScanSettings) obj;
        if (this.maxScansPerBatch != o.maxScansPerBatch || this.maxApPerScan != o.maxApPerScan || this.scanIntervalSec != o.scanIntervalSec || this.maxApForDistance != o.maxApForDistance) {
            return false;
        }
        if (this.channelSet != null) {
            return this.channelSet.equals(o.channelSet);
        }
        if (o.channelSet == null) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return this.maxScansPerBatch + (this.maxApPerScan * 3) + (this.scanIntervalSec * 5) + (this.maxApForDistance * 7) + (this.channelSet.hashCode() * 11);
    }

    public String toString() {
        Object valueOf;
        Object valueOf2;
        Object valueOf3;
        StringBuffer sb = new StringBuffer();
        Object obj = "<none>";
        StringBuffer append = sb.append("BatchScanSettings [maxScansPerBatch: ");
        if (this.maxScansPerBatch == Integer.MAX_VALUE) {
            valueOf = obj;
        } else {
            valueOf = Integer.valueOf(this.maxScansPerBatch);
        }
        StringBuffer append2 = append.append(valueOf).append(", maxApPerScan: ");
        if (this.maxApPerScan == Integer.MAX_VALUE) {
            valueOf2 = obj;
        } else {
            valueOf2 = Integer.valueOf(this.maxApPerScan);
        }
        StringBuffer append3 = append2.append(valueOf2).append(", scanIntervalSec: ");
        if (this.scanIntervalSec == Integer.MAX_VALUE) {
            valueOf3 = obj;
        } else {
            valueOf3 = Integer.valueOf(this.scanIntervalSec);
        }
        StringBuffer append4 = append3.append(valueOf3).append(", maxApForDistance: ");
        if (this.maxApForDistance != Integer.MAX_VALUE) {
            obj = Integer.valueOf(this.maxApForDistance);
        }
        append4.append(obj).append(", channelSet: ");
        if (this.channelSet == null) {
            sb.append("ALL");
        } else {
            sb.append("<");
            Iterator i$ = this.channelSet.iterator();
            while (i$.hasNext()) {
                sb.append(" " + i$.next());
            }
            sb.append(">");
        }
        sb.append("]");
        return sb.toString();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.maxScansPerBatch);
        dest.writeInt(this.maxApPerScan);
        dest.writeInt(this.scanIntervalSec);
        dest.writeInt(this.maxApForDistance);
        dest.writeInt(this.channelSet == null ? 0 : this.channelSet.size());
        if (this.channelSet != null) {
            for (String channel : this.channelSet) {
                dest.writeString(channel);
            }
        }
    }
}

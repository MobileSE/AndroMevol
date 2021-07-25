package android.net;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Arrays;
import java.util.Objects;

public class RssiCurve implements Parcelable {
    public static final Parcelable.Creator<RssiCurve> CREATOR = new Parcelable.Creator<RssiCurve>() {
        /* class android.net.RssiCurve.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RssiCurve createFromParcel(Parcel in) {
            return new RssiCurve(in);
        }

        @Override // android.os.Parcelable.Creator
        public RssiCurve[] newArray(int size) {
            return new RssiCurve[size];
        }
    };
    public final int bucketWidth;
    public final byte[] rssiBuckets;
    public final int start;

    public RssiCurve(int start2, int bucketWidth2, byte[] rssiBuckets2) {
        this.start = start2;
        this.bucketWidth = bucketWidth2;
        if (rssiBuckets2 == null || rssiBuckets2.length == 0) {
            throw new IllegalArgumentException("rssiBuckets must be at least one element large.");
        }
        this.rssiBuckets = rssiBuckets2;
    }

    private RssiCurve(Parcel in) {
        this.start = in.readInt();
        this.bucketWidth = in.readInt();
        this.rssiBuckets = new byte[in.readInt()];
        in.readByteArray(this.rssiBuckets);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.start);
        out.writeInt(this.bucketWidth);
        out.writeInt(this.rssiBuckets.length);
        out.writeByteArray(this.rssiBuckets);
    }

    public byte lookupScore(int rssi) {
        int index = (rssi - this.start) / this.bucketWidth;
        if (index < 0) {
            index = 0;
        } else if (index > this.rssiBuckets.length - 1) {
            index = this.rssiBuckets.length - 1;
        }
        return this.rssiBuckets[index];
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RssiCurve rssiCurve = (RssiCurve) o;
        return this.start == rssiCurve.start && this.bucketWidth == rssiCurve.bucketWidth && Arrays.equals(this.rssiBuckets, rssiCurve.rssiBuckets);
    }

    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.start), Integer.valueOf(this.bucketWidth), this.rssiBuckets);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RssiCurve[start=").append(this.start).append(",bucketWidth=").append(this.bucketWidth);
        sb.append(",buckets=");
        for (int i = 0; i < this.rssiBuckets.length; i++) {
            sb.append((int) this.rssiBuckets[i]);
            if (i < this.rssiBuckets.length - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}

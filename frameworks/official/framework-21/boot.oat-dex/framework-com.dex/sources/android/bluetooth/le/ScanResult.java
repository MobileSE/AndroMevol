package android.bluetooth.le;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.Objects;

public final class ScanResult implements Parcelable {
    public static final Parcelable.Creator<ScanResult> CREATOR = new Parcelable.Creator<ScanResult>() {
        /* class android.bluetooth.le.ScanResult.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ScanResult createFromParcel(Parcel source) {
            return new ScanResult(source);
        }

        @Override // android.os.Parcelable.Creator
        public ScanResult[] newArray(int size) {
            return new ScanResult[size];
        }
    };
    private BluetoothDevice mDevice;
    private int mRssi;
    private ScanRecord mScanRecord;
    private long mTimestampNanos;

    public ScanResult(BluetoothDevice device, ScanRecord scanRecord, int rssi, long timestampNanos) {
        this.mDevice = device;
        this.mScanRecord = scanRecord;
        this.mRssi = rssi;
        this.mTimestampNanos = timestampNanos;
    }

    private ScanResult(Parcel in) {
        readFromParcel(in);
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        if (this.mDevice != null) {
            dest.writeInt(1);
            this.mDevice.writeToParcel(dest, flags);
        } else {
            dest.writeInt(0);
        }
        if (this.mScanRecord != null) {
            dest.writeInt(1);
            dest.writeByteArray(this.mScanRecord.getBytes());
        } else {
            dest.writeInt(0);
        }
        dest.writeInt(this.mRssi);
        dest.writeLong(this.mTimestampNanos);
    }

    private void readFromParcel(Parcel in) {
        if (in.readInt() == 1) {
            this.mDevice = BluetoothDevice.CREATOR.createFromParcel(in);
        }
        if (in.readInt() == 1) {
            this.mScanRecord = ScanRecord.parseFromBytes(in.createByteArray());
        }
        this.mRssi = in.readInt();
        this.mTimestampNanos = in.readLong();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public BluetoothDevice getDevice() {
        return this.mDevice;
    }

    public ScanRecord getScanRecord() {
        return this.mScanRecord;
    }

    public int getRssi() {
        return this.mRssi;
    }

    public long getTimestampNanos() {
        return this.mTimestampNanos;
    }

    public int hashCode() {
        return Objects.hash(this.mDevice, Integer.valueOf(this.mRssi), this.mScanRecord, Long.valueOf(this.mTimestampNanos));
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ScanResult other = (ScanResult) obj;
        return Objects.equals(this.mDevice, other.mDevice) && this.mRssi == other.mRssi && Objects.equals(this.mScanRecord, other.mScanRecord) && this.mTimestampNanos == other.mTimestampNanos;
    }

    public String toString() {
        return "ScanResult{mDevice=" + this.mDevice + ", mScanRecord=" + Objects.toString(this.mScanRecord) + ", mRssi=" + this.mRssi + ", mTimestampNanos=" + this.mTimestampNanos + '}';
    }
}

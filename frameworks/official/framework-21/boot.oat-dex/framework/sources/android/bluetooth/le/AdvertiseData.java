package android.bluetooth.le;

import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.util.ArrayMap;
import android.util.SparseArray;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class AdvertiseData implements Parcelable {
    public static final Parcelable.Creator<AdvertiseData> CREATOR = new Parcelable.Creator<AdvertiseData>() {
        /* class android.bluetooth.le.AdvertiseData.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AdvertiseData[] newArray(int size) {
            return new AdvertiseData[size];
        }

        @Override // android.os.Parcelable.Creator
        public AdvertiseData createFromParcel(Parcel in) {
            Builder builder = new Builder();
            List<ParcelUuid> uuids = in.readArrayList(ParcelUuid.class.getClassLoader());
            if (uuids != null) {
                for (ParcelUuid uuid : uuids) {
                    builder.addServiceUuid(uuid);
                }
            }
            int manufacturerSize = in.readInt();
            for (int i = 0; i < manufacturerSize; i++) {
                int manufacturerId = in.readInt();
                if (in.readInt() == 1) {
                    byte[] manufacturerData = new byte[in.readInt()];
                    in.readByteArray(manufacturerData);
                    builder.addManufacturerData(manufacturerId, manufacturerData);
                }
            }
            int serviceDataSize = in.readInt();
            for (int i2 = 0; i2 < serviceDataSize; i2++) {
                ParcelUuid serviceDataUuid = (ParcelUuid) in.readParcelable(ParcelUuid.class.getClassLoader());
                if (in.readInt() == 1) {
                    byte[] serviceData = new byte[in.readInt()];
                    in.readByteArray(serviceData);
                    builder.addServiceData(serviceDataUuid, serviceData);
                }
            }
            builder.setIncludeTxPowerLevel(in.readByte() == 1);
            builder.setIncludeDeviceName(in.readByte() == 1);
            return builder.build();
        }
    };
    private final boolean mIncludeDeviceName;
    private final boolean mIncludeTxPowerLevel;
    private final SparseArray<byte[]> mManufacturerSpecificData;
    private final Map<ParcelUuid, byte[]> mServiceData;
    private final List<ParcelUuid> mServiceUuids;

    private AdvertiseData(List<ParcelUuid> serviceUuids, SparseArray<byte[]> manufacturerData, Map<ParcelUuid, byte[]> serviceData, boolean includeTxPowerLevel, boolean includeDeviceName) {
        this.mServiceUuids = serviceUuids;
        this.mManufacturerSpecificData = manufacturerData;
        this.mServiceData = serviceData;
        this.mIncludeTxPowerLevel = includeTxPowerLevel;
        this.mIncludeDeviceName = includeDeviceName;
    }

    public List<ParcelUuid> getServiceUuids() {
        return this.mServiceUuids;
    }

    public SparseArray<byte[]> getManufacturerSpecificData() {
        return this.mManufacturerSpecificData;
    }

    public Map<ParcelUuid, byte[]> getServiceData() {
        return this.mServiceData;
    }

    public boolean getIncludeTxPowerLevel() {
        return this.mIncludeTxPowerLevel;
    }

    public boolean getIncludeDeviceName() {
        return this.mIncludeDeviceName;
    }

    public int hashCode() {
        return Objects.hash(this.mServiceUuids, this.mManufacturerSpecificData, this.mServiceData, Boolean.valueOf(this.mIncludeDeviceName), Boolean.valueOf(this.mIncludeTxPowerLevel));
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AdvertiseData other = (AdvertiseData) obj;
        return Objects.equals(this.mServiceUuids, other.mServiceUuids) && BluetoothLeUtils.equals(this.mManufacturerSpecificData, other.mManufacturerSpecificData) && BluetoothLeUtils.equals(this.mServiceData, other.mServiceData) && this.mIncludeDeviceName == other.mIncludeDeviceName && this.mIncludeTxPowerLevel == other.mIncludeTxPowerLevel;
    }

    public String toString() {
        return "AdvertiseData [mServiceUuids=" + this.mServiceUuids + ", mManufacturerSpecificData=" + BluetoothLeUtils.toString(this.mManufacturerSpecificData) + ", mServiceData=" + BluetoothLeUtils.toString(this.mServiceData) + ", mIncludeTxPowerLevel=" + this.mIncludeTxPowerLevel + ", mIncludeDeviceName=" + this.mIncludeDeviceName + "]";
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        int i = 1;
        dest.writeList(this.mServiceUuids);
        dest.writeInt(this.mManufacturerSpecificData.size());
        for (int i2 = 0; i2 < this.mManufacturerSpecificData.size(); i2++) {
            dest.writeInt(this.mManufacturerSpecificData.keyAt(i2));
            byte[] data = this.mManufacturerSpecificData.valueAt(i2);
            if (data == null) {
                dest.writeInt(0);
            } else {
                dest.writeInt(1);
                dest.writeInt(data.length);
                dest.writeByteArray(data);
            }
        }
        dest.writeInt(this.mServiceData.size());
        for (ParcelUuid uuid : this.mServiceData.keySet()) {
            dest.writeParcelable(uuid, flags);
            byte[] data2 = this.mServiceData.get(uuid);
            if (data2 == null) {
                dest.writeInt(0);
            } else {
                dest.writeInt(1);
                dest.writeInt(data2.length);
                dest.writeByteArray(data2);
            }
        }
        dest.writeByte((byte) (getIncludeTxPowerLevel() ? 1 : 0));
        if (!getIncludeDeviceName()) {
            i = 0;
        }
        dest.writeByte((byte) i);
    }

    public static final class Builder {
        private boolean mIncludeDeviceName;
        private boolean mIncludeTxPowerLevel;
        private SparseArray<byte[]> mManufacturerSpecificData = new SparseArray<>();
        private Map<ParcelUuid, byte[]> mServiceData = new ArrayMap();
        private List<ParcelUuid> mServiceUuids = new ArrayList();

        public Builder addServiceUuid(ParcelUuid serviceUuid) {
            if (serviceUuid == null) {
                throw new IllegalArgumentException("serivceUuids are null");
            }
            this.mServiceUuids.add(serviceUuid);
            return this;
        }

        public Builder addServiceData(ParcelUuid serviceDataUuid, byte[] serviceData) {
            if (serviceDataUuid == null || serviceData == null) {
                throw new IllegalArgumentException("serviceDataUuid or serviceDataUuid is null");
            }
            this.mServiceData.put(serviceDataUuid, serviceData);
            return this;
        }

        public Builder addManufacturerData(int manufacturerId, byte[] manufacturerSpecificData) {
            if (manufacturerId < 0) {
                throw new IllegalArgumentException("invalid manufacturerId - " + manufacturerId);
            } else if (manufacturerSpecificData == null) {
                throw new IllegalArgumentException("manufacturerSpecificData is null");
            } else {
                this.mManufacturerSpecificData.put(manufacturerId, manufacturerSpecificData);
                return this;
            }
        }

        public Builder setIncludeTxPowerLevel(boolean includeTxPowerLevel) {
            this.mIncludeTxPowerLevel = includeTxPowerLevel;
            return this;
        }

        public Builder setIncludeDeviceName(boolean includeDeviceName) {
            this.mIncludeDeviceName = includeDeviceName;
            return this;
        }

        public AdvertiseData build() {
            return new AdvertiseData(this.mServiceUuids, this.mManufacturerSpecificData, this.mServiceData, this.mIncludeTxPowerLevel, this.mIncludeDeviceName);
        }
    }
}

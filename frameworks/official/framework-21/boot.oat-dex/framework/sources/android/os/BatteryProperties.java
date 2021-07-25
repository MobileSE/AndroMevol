package android.os;

import android.os.Parcelable;

public class BatteryProperties implements Parcelable {
    public static final Parcelable.Creator<BatteryProperties> CREATOR = new Parcelable.Creator<BatteryProperties>() {
        /* class android.os.BatteryProperties.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public BatteryProperties createFromParcel(Parcel p) {
            return new BatteryProperties(p);
        }

        @Override // android.os.Parcelable.Creator
        public BatteryProperties[] newArray(int size) {
            return new BatteryProperties[size];
        }
    };
    public int batteryHealth;
    public int batteryLevel;
    public boolean batteryPresent;
    public int batteryStatus;
    public String batteryTechnology;
    public int batteryTemperature;
    public int batteryVoltage;
    public boolean chargerAcOnline;
    public boolean chargerUsbOnline;
    public boolean chargerWirelessOnline;

    public BatteryProperties() {
    }

    public void set(BatteryProperties other) {
        this.chargerAcOnline = other.chargerAcOnline;
        this.chargerUsbOnline = other.chargerUsbOnline;
        this.chargerWirelessOnline = other.chargerWirelessOnline;
        this.batteryStatus = other.batteryStatus;
        this.batteryHealth = other.batteryHealth;
        this.batteryPresent = other.batteryPresent;
        this.batteryLevel = other.batteryLevel;
        this.batteryVoltage = other.batteryVoltage;
        this.batteryTemperature = other.batteryTemperature;
        this.batteryTechnology = other.batteryTechnology;
    }

    private BatteryProperties(Parcel p) {
        boolean z;
        boolean z2;
        boolean z3 = true;
        this.chargerAcOnline = p.readInt() == 1;
        if (p.readInt() == 1) {
            z = true;
        } else {
            z = false;
        }
        this.chargerUsbOnline = z;
        if (p.readInt() == 1) {
            z2 = true;
        } else {
            z2 = false;
        }
        this.chargerWirelessOnline = z2;
        this.batteryStatus = p.readInt();
        this.batteryHealth = p.readInt();
        this.batteryPresent = p.readInt() != 1 ? false : z3;
        this.batteryLevel = p.readInt();
        this.batteryVoltage = p.readInt();
        this.batteryTemperature = p.readInt();
        this.batteryTechnology = p.readString();
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel p, int flags) {
        int i;
        int i2;
        int i3 = 1;
        p.writeInt(this.chargerAcOnline ? 1 : 0);
        if (this.chargerUsbOnline) {
            i = 1;
        } else {
            i = 0;
        }
        p.writeInt(i);
        if (this.chargerWirelessOnline) {
            i2 = 1;
        } else {
            i2 = 0;
        }
        p.writeInt(i2);
        p.writeInt(this.batteryStatus);
        p.writeInt(this.batteryHealth);
        if (!this.batteryPresent) {
            i3 = 0;
        }
        p.writeInt(i3);
        p.writeInt(this.batteryLevel);
        p.writeInt(this.batteryVoltage);
        p.writeInt(this.batteryTemperature);
        p.writeString(this.batteryTechnology);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }
}

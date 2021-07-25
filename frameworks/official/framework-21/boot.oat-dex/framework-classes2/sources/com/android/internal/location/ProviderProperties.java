package com.android.internal.location;

import android.os.Parcel;
import android.os.Parcelable;

public final class ProviderProperties implements Parcelable {
    public static final Parcelable.Creator<ProviderProperties> CREATOR = new Parcelable.Creator<ProviderProperties>() {
        /* class com.android.internal.location.ProviderProperties.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ProviderProperties createFromParcel(Parcel in) {
            boolean requiresNetwork;
            boolean requiresSatellite;
            boolean requiresCell;
            boolean hasMonetaryCost;
            boolean supportsAltitude;
            boolean supportsSpeed;
            boolean supportsBearing;
            if (in.readInt() == 1) {
                requiresNetwork = true;
            } else {
                requiresNetwork = false;
            }
            if (in.readInt() == 1) {
                requiresSatellite = true;
            } else {
                requiresSatellite = false;
            }
            if (in.readInt() == 1) {
                requiresCell = true;
            } else {
                requiresCell = false;
            }
            if (in.readInt() == 1) {
                hasMonetaryCost = true;
            } else {
                hasMonetaryCost = false;
            }
            if (in.readInt() == 1) {
                supportsAltitude = true;
            } else {
                supportsAltitude = false;
            }
            if (in.readInt() == 1) {
                supportsSpeed = true;
            } else {
                supportsSpeed = false;
            }
            if (in.readInt() == 1) {
                supportsBearing = true;
            } else {
                supportsBearing = false;
            }
            return new ProviderProperties(requiresNetwork, requiresSatellite, requiresCell, hasMonetaryCost, supportsAltitude, supportsSpeed, supportsBearing, in.readInt(), in.readInt());
        }

        @Override // android.os.Parcelable.Creator
        public ProviderProperties[] newArray(int size) {
            return new ProviderProperties[size];
        }
    };
    public final int mAccuracy;
    public final boolean mHasMonetaryCost;
    public final int mPowerRequirement;
    public final boolean mRequiresCell;
    public final boolean mRequiresNetwork;
    public final boolean mRequiresSatellite;
    public final boolean mSupportsAltitude;
    public final boolean mSupportsBearing;
    public final boolean mSupportsSpeed;

    public ProviderProperties(boolean mRequiresNetwork2, boolean mRequiresSatellite2, boolean mRequiresCell2, boolean mHasMonetaryCost2, boolean mSupportsAltitude2, boolean mSupportsSpeed2, boolean mSupportsBearing2, int mPowerRequirement2, int mAccuracy2) {
        this.mRequiresNetwork = mRequiresNetwork2;
        this.mRequiresSatellite = mRequiresSatellite2;
        this.mRequiresCell = mRequiresCell2;
        this.mHasMonetaryCost = mHasMonetaryCost2;
        this.mSupportsAltitude = mSupportsAltitude2;
        this.mSupportsSpeed = mSupportsSpeed2;
        this.mSupportsBearing = mSupportsBearing2;
        this.mPowerRequirement = mPowerRequirement2;
        this.mAccuracy = mAccuracy2;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        int i;
        int i2;
        int i3;
        int i4;
        int i5;
        int i6 = 1;
        parcel.writeInt(this.mRequiresNetwork ? 1 : 0);
        if (this.mRequiresSatellite) {
            i = 1;
        } else {
            i = 0;
        }
        parcel.writeInt(i);
        if (this.mRequiresCell) {
            i2 = 1;
        } else {
            i2 = 0;
        }
        parcel.writeInt(i2);
        if (this.mHasMonetaryCost) {
            i3 = 1;
        } else {
            i3 = 0;
        }
        parcel.writeInt(i3);
        if (this.mSupportsAltitude) {
            i4 = 1;
        } else {
            i4 = 0;
        }
        parcel.writeInt(i4);
        if (this.mSupportsSpeed) {
            i5 = 1;
        } else {
            i5 = 0;
        }
        parcel.writeInt(i5);
        if (!this.mSupportsBearing) {
            i6 = 0;
        }
        parcel.writeInt(i6);
        parcel.writeInt(this.mPowerRequirement);
        parcel.writeInt(this.mAccuracy);
    }
}

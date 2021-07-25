package com.android.internal.location;

import android.location.LocationRequest;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.TimeUtils;
import java.util.ArrayList;
import java.util.List;

public final class ProviderRequest implements Parcelable {
    public static final Parcelable.Creator<ProviderRequest> CREATOR = new Parcelable.Creator<ProviderRequest>() {
        /* class com.android.internal.location.ProviderRequest.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ProviderRequest createFromParcel(Parcel in) {
            boolean z = true;
            ProviderRequest request = new ProviderRequest();
            if (in.readInt() != 1) {
                z = false;
            }
            request.reportLocation = z;
            request.interval = in.readLong();
            int count = in.readInt();
            for (int i = 0; i < count; i++) {
                request.locationRequests.add(LocationRequest.CREATOR.createFromParcel(in));
            }
            return request;
        }

        @Override // android.os.Parcelable.Creator
        public ProviderRequest[] newArray(int size) {
            return new ProviderRequest[size];
        }
    };
    public long interval = Long.MAX_VALUE;
    public List<LocationRequest> locationRequests = new ArrayList();
    public boolean reportLocation = false;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(this.reportLocation ? 1 : 0);
        parcel.writeLong(this.interval);
        parcel.writeInt(this.locationRequests.size());
        for (LocationRequest request : this.locationRequests) {
            request.writeToParcel(parcel, flags);
        }
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("ProviderRequest[");
        if (this.reportLocation) {
            s.append("ON");
            s.append(" interval=");
            TimeUtils.formatDuration(this.interval, s);
        } else {
            s.append("OFF");
        }
        s.append(']');
        return s.toString();
    }
}

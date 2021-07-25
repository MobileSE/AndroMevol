package android.widget;

import android.os.Parcel;
import android.os.Parcelable;

class RemoteViews$3 implements Parcelable.Creator<RemoteViews> {
    RemoteViews$3() {
    }

    @Override // android.os.Parcelable.Creator
    public RemoteViews createFromParcel(Parcel parcel) {
        return new RemoteViews(parcel);
    }

    @Override // android.os.Parcelable.Creator
    public RemoteViews[] newArray(int size) {
        return new RemoteViews[size];
    }
}

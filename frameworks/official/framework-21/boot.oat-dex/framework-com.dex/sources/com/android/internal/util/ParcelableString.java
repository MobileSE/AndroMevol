package com.android.internal.util;

import android.os.Parcel;
import android.os.Parcelable;

public class ParcelableString implements Parcelable {
    public static final Parcelable.Creator<ParcelableString> CREATOR = new Parcelable.Creator<ParcelableString>() {
        /* class com.android.internal.util.ParcelableString.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ParcelableString createFromParcel(Parcel in) {
            ParcelableString ret = new ParcelableString();
            ret.string = in.readString();
            return ret;
        }

        @Override // android.os.Parcelable.Creator
        public ParcelableString[] newArray(int size) {
            return new ParcelableString[size];
        }
    };
    public String string;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.string);
    }
}

package android.hardware.camera2.utils;

import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;

public class BinderHolder implements Parcelable {
    public static final Parcelable.Creator<BinderHolder> CREATOR = new Parcelable.Creator<BinderHolder>() {
        /* class android.hardware.camera2.utils.BinderHolder.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public BinderHolder createFromParcel(Parcel in) {
            return new BinderHolder(in);
        }

        @Override // android.os.Parcelable.Creator
        public BinderHolder[] newArray(int size) {
            return new BinderHolder[size];
        }
    };
    private IBinder mBinder;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStrongBinder(this.mBinder);
    }

    public void readFromParcel(Parcel src) {
        this.mBinder = src.readStrongBinder();
    }

    public IBinder getBinder() {
        return this.mBinder;
    }

    public void setBinder(IBinder binder) {
        this.mBinder = binder;
    }

    public BinderHolder() {
        this.mBinder = null;
    }

    public BinderHolder(IBinder binder) {
        this.mBinder = null;
        this.mBinder = binder;
    }

    private BinderHolder(Parcel in) {
        this.mBinder = null;
        this.mBinder = in.readStrongBinder();
    }
}

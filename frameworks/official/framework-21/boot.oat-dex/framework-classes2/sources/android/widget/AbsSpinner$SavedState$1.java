package android.widget;

import android.os.Parcel;
import android.os.Parcelable;
import android.widget.AbsSpinner;

class AbsSpinner$SavedState$1 implements Parcelable.Creator<AbsSpinner.SavedState> {
    AbsSpinner$SavedState$1() {
    }

    @Override // android.os.Parcelable.Creator
    public AbsSpinner.SavedState createFromParcel(Parcel in) {
        return new AbsSpinner.SavedState(in);
    }

    @Override // android.os.Parcelable.Creator
    public AbsSpinner.SavedState[] newArray(int size) {
        return new AbsSpinner.SavedState[size];
    }
}

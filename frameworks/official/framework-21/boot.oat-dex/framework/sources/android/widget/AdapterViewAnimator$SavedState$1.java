package android.widget;

import android.os.Parcel;
import android.os.Parcelable;
import android.widget.AdapterViewAnimator;

class AdapterViewAnimator$SavedState$1 implements Parcelable.Creator<AdapterViewAnimator.SavedState> {
    AdapterViewAnimator$SavedState$1() {
    }

    @Override // android.os.Parcelable.Creator
    public AdapterViewAnimator.SavedState createFromParcel(Parcel in) {
        return new AdapterViewAnimator.SavedState(in, (AdapterViewAnimator.1) null);
    }

    @Override // android.os.Parcelable.Creator
    public AdapterViewAnimator.SavedState[] newArray(int size) {
        return new AdapterViewAnimator.SavedState[size];
    }
}

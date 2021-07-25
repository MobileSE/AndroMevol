package android.widget;

import android.os.Parcel;
import android.os.Parcelable;
import android.widget.HorizontalScrollView;

class HorizontalScrollView$SavedState$1 implements Parcelable.Creator<HorizontalScrollView.SavedState> {
    HorizontalScrollView$SavedState$1() {
    }

    @Override // android.os.Parcelable.Creator
    public HorizontalScrollView.SavedState createFromParcel(Parcel in) {
        return new HorizontalScrollView.SavedState(in);
    }

    @Override // android.os.Parcelable.Creator
    public HorizontalScrollView.SavedState[] newArray(int size) {
        return new HorizontalScrollView.SavedState[size];
    }
}

package android.widget;

import android.os.Parcel;
import android.os.Parcelable;
import android.widget.TextView;

class TextView$SavedState$1 implements Parcelable.Creator<TextView.SavedState> {
    TextView$SavedState$1() {
    }

    @Override // android.os.Parcelable.Creator
    public TextView.SavedState createFromParcel(Parcel in) {
        return new TextView.SavedState(in, null);
    }

    @Override // android.os.Parcelable.Creator
    public TextView.SavedState[] newArray(int size) {
        return new TextView.SavedState[size];
    }
}

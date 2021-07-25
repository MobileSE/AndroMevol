package android.widget;

import android.os.Parcel;
import android.os.Parcelable;
import android.widget.TimePickerSpinnerDelegate;

class TimePickerSpinnerDelegate$SavedState$1 implements Parcelable.Creator<TimePickerSpinnerDelegate.SavedState> {
    TimePickerSpinnerDelegate$SavedState$1() {
    }

    @Override // android.os.Parcelable.Creator
    public TimePickerSpinnerDelegate.SavedState createFromParcel(Parcel in) {
        return new TimePickerSpinnerDelegate.SavedState(in, (TimePickerSpinnerDelegate.AnonymousClass1) null);
    }

    @Override // android.os.Parcelable.Creator
    public TimePickerSpinnerDelegate.SavedState[] newArray(int size) {
        return new TimePickerSpinnerDelegate.SavedState[size];
    }
}

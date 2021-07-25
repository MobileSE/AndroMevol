package android.widget;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import java.util.ArrayList;

class TimePickerSpinnerDelegate$SavedState extends View.BaseSavedState {
    public static final Parcelable.Creator<TimePickerSpinnerDelegate$SavedState> CREATOR = new 1();
    private final int mCurrentItemShowing;
    private final int mHour;
    private final boolean mInKbMode;
    private final boolean mIs24HourMode;
    private final int mMinute;
    private final ArrayList<Integer> mTypedTimes;

    private TimePickerSpinnerDelegate$SavedState(Parcelable superState, int hour, int minute, boolean is24HourMode, boolean isKbMode, ArrayList<Integer> typedTimes, int currentItemShowing) {
        super(superState);
        this.mHour = hour;
        this.mMinute = minute;
        this.mIs24HourMode = is24HourMode;
        this.mInKbMode = isKbMode;
        this.mTypedTimes = typedTimes;
        this.mCurrentItemShowing = currentItemShowing;
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    private TimePickerSpinnerDelegate$SavedState(Parcel in) {
        super(in);
        boolean z;
        boolean z2 = true;
        this.mHour = in.readInt();
        this.mMinute = in.readInt();
        if (in.readInt() == 1) {
            z = true;
        } else {
            z = false;
        }
        this.mIs24HourMode = z;
        this.mInKbMode = in.readInt() != 1 ? false : z2;
        this.mTypedTimes = in.readArrayList(getClass().getClassLoader());
        this.mCurrentItemShowing = in.readInt();
    }

    public int getHour() {
        return this.mHour;
    }

    public int getMinute() {
        return this.mMinute;
    }

    public boolean is24HourMode() {
        return this.mIs24HourMode;
    }

    public boolean inKbMode() {
        return this.mInKbMode;
    }

    public ArrayList<Integer> getTypesTimes() {
        return this.mTypedTimes;
    }

    public int getCurrentItemShowing() {
        return this.mCurrentItemShowing;
    }

    @Override // android.os.Parcelable, android.view.AbsSavedState
    public void writeToParcel(Parcel dest, int flags) {
        int i;
        int i2 = 1;
        super.writeToParcel(dest, flags);
        dest.writeInt(this.mHour);
        dest.writeInt(this.mMinute);
        if (this.mIs24HourMode) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (!this.mInKbMode) {
            i2 = 0;
        }
        dest.writeInt(i2);
        dest.writeList(this.mTypedTimes);
        dest.writeInt(this.mCurrentItemShowing);
    }
}

package android.widget;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

/* access modifiers changed from: private */
public class DatePickerCalendarDelegate$SavedState extends View.BaseSavedState {
    public static final Parcelable.Creator<DatePickerCalendarDelegate$SavedState> CREATOR = new Parcelable.Creator<DatePickerCalendarDelegate$SavedState>() {
        /* class android.widget.DatePickerCalendarDelegate$SavedState.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DatePickerCalendarDelegate$SavedState createFromParcel(Parcel in) {
            return new DatePickerCalendarDelegate$SavedState(in);
        }

        @Override // android.os.Parcelable.Creator
        public DatePickerCalendarDelegate$SavedState[] newArray(int size) {
            return new DatePickerCalendarDelegate$SavedState[size];
        }
    };
    private final int mCurrentView;
    private final int mListPosition;
    private final int mListPositionOffset;
    private final long mMaxDate;
    private final long mMinDate;
    private final int mSelectedDay;
    private final int mSelectedMonth;
    private final int mSelectedYear;

    private DatePickerCalendarDelegate$SavedState(Parcelable superState, int year, int month, int day, long minDate, long maxDate, int currentView, int listPosition, int listPositionOffset) {
        super(superState);
        this.mSelectedYear = year;
        this.mSelectedMonth = month;
        this.mSelectedDay = day;
        this.mMinDate = minDate;
        this.mMaxDate = maxDate;
        this.mCurrentView = currentView;
        this.mListPosition = listPosition;
        this.mListPositionOffset = listPositionOffset;
    }

    private DatePickerCalendarDelegate$SavedState(Parcel in) {
        super(in);
        this.mSelectedYear = in.readInt();
        this.mSelectedMonth = in.readInt();
        this.mSelectedDay = in.readInt();
        this.mMinDate = in.readLong();
        this.mMaxDate = in.readLong();
        this.mCurrentView = in.readInt();
        this.mListPosition = in.readInt();
        this.mListPositionOffset = in.readInt();
    }

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.mSelectedYear);
        dest.writeInt(this.mSelectedMonth);
        dest.writeInt(this.mSelectedDay);
        dest.writeLong(this.mMinDate);
        dest.writeLong(this.mMaxDate);
        dest.writeInt(this.mCurrentView);
        dest.writeInt(this.mListPosition);
        dest.writeInt(this.mListPositionOffset);
    }

    public int getSelectedDay() {
        return this.mSelectedDay;
    }

    public int getSelectedMonth() {
        return this.mSelectedMonth;
    }

    public int getSelectedYear() {
        return this.mSelectedYear;
    }

    public long getMinDate() {
        return this.mMinDate;
    }

    public long getMaxDate() {
        return this.mMaxDate;
    }

    public int getCurrentView() {
        return this.mCurrentView;
    }

    public int getListPosition() {
        return this.mListPosition;
    }

    public int getListPositionOffset() {
        return this.mListPositionOffset;
    }
}

package android.widget;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

/* access modifiers changed from: private */
public class DatePicker$SavedState extends View.BaseSavedState {
    public static final Parcelable.Creator<DatePicker$SavedState> CREATOR = new Parcelable.Creator<DatePicker$SavedState>() {
        /* class android.widget.DatePicker$SavedState.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DatePicker$SavedState createFromParcel(Parcel in) {
            return new DatePicker$SavedState(in);
        }

        @Override // android.os.Parcelable.Creator
        public DatePicker$SavedState[] newArray(int size) {
            return new DatePicker$SavedState[size];
        }
    };
    private final int mDay;
    private final int mMonth;
    private final int mYear;

    private DatePicker$SavedState(Parcelable superState, int year, int month, int day) {
        super(superState);
        this.mYear = year;
        this.mMonth = month;
        this.mDay = day;
    }

    private DatePicker$SavedState(Parcel in) {
        super(in);
        this.mYear = in.readInt();
        this.mMonth = in.readInt();
        this.mDay = in.readInt();
    }

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.mYear);
        dest.writeInt(this.mMonth);
        dest.writeInt(this.mDay);
    }
}

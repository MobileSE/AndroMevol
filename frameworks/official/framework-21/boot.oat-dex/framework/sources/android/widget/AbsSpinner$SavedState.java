package android.widget;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

class AbsSpinner$SavedState extends View.BaseSavedState {
    public static final Parcelable.Creator<AbsSpinner$SavedState> CREATOR = new 1();
    int position;
    long selectedId;

    AbsSpinner$SavedState(Parcelable superState) {
        super(superState);
    }

    AbsSpinner$SavedState(Parcel in) {
        super(in);
        this.selectedId = in.readLong();
        this.position = in.readInt();
    }

    @Override // android.os.Parcelable, android.view.AbsSavedState
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeLong(this.selectedId);
        out.writeInt(this.position);
    }

    public String toString() {
        return "AbsSpinner.SavedState{" + Integer.toHexString(System.identityHashCode(this)) + " selectedId=" + this.selectedId + " position=" + this.position + "}";
    }
}

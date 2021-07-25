package android.widget;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

class HorizontalScrollView$SavedState extends View.BaseSavedState {
    public static final Parcelable.Creator<HorizontalScrollView$SavedState> CREATOR = new 1();
    public boolean isLayoutRtl;
    public int scrollPosition;

    HorizontalScrollView$SavedState(Parcelable superState) {
        super(superState);
    }

    public HorizontalScrollView$SavedState(Parcel source) {
        super(source);
        this.scrollPosition = source.readInt();
        this.isLayoutRtl = source.readInt() == 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.scrollPosition);
        dest.writeInt(this.isLayoutRtl ? 1 : 0);
    }

    public String toString() {
        return "HorizontalScrollView.SavedState{" + Integer.toHexString(System.identityHashCode(this)) + " scrollPosition=" + this.scrollPosition + " isLayoutRtl=" + this.isLayoutRtl + "}";
    }
}

package android.view;

import android.os.Parcel;
import android.os.Parcelable;

public abstract class AbsSavedState implements Parcelable {
    public static final Parcelable.Creator<AbsSavedState> CREATOR = new Parcelable.Creator<AbsSavedState>() {
        /* class android.view.AbsSavedState.AnonymousClass2 */

        @Override // android.os.Parcelable.Creator
        public AbsSavedState createFromParcel(Parcel in) {
            if (in.readParcelable(null) == null) {
                return AbsSavedState.EMPTY_STATE;
            }
            throw new IllegalStateException("superState must be null");
        }

        @Override // android.os.Parcelable.Creator
        public AbsSavedState[] newArray(int size) {
            return new AbsSavedState[size];
        }
    };
    public static final AbsSavedState EMPTY_STATE = new AbsSavedState() {
        /* class android.view.AbsSavedState.AnonymousClass1 */
    };
    private final Parcelable mSuperState;

    private AbsSavedState() {
        this.mSuperState = null;
    }

    protected AbsSavedState(Parcelable superState) {
        if (superState == null) {
            throw new IllegalArgumentException("superState must not be null");
        }
        this.mSuperState = superState == EMPTY_STATE ? null : superState;
    }

    protected AbsSavedState(Parcel source) {
        Parcelable superState = source.readParcelable(null);
        this.mSuperState = superState == null ? EMPTY_STATE : superState;
    }

    public final Parcelable getSuperState() {
        return this.mSuperState;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mSuperState, flags);
    }
}

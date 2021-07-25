package android.widget;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.View;

public class TextView$SavedState extends View.BaseSavedState {
    public static final Parcelable.Creator<TextView$SavedState> CREATOR = new 1();
    CharSequence error;
    boolean frozenWithFocus;
    int selEnd;
    int selStart;
    CharSequence text;

    TextView$SavedState(Parcelable superState) {
        super(superState);
    }

    @Override // android.os.Parcelable, android.view.AbsSavedState
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeInt(this.selStart);
        out.writeInt(this.selEnd);
        out.writeInt(this.frozenWithFocus ? 1 : 0);
        TextUtils.writeToParcel(this.text, out, flags);
        if (this.error == null) {
            out.writeInt(0);
            return;
        }
        out.writeInt(1);
        TextUtils.writeToParcel(this.error, out, flags);
    }

    public String toString() {
        String str = "TextView.SavedState{" + Integer.toHexString(System.identityHashCode(this)) + " start=" + this.selStart + " end=" + this.selEnd;
        if (this.text != null) {
            str = str + " text=" + ((Object) this.text);
        }
        return str + "}";
    }

    private TextView$SavedState(Parcel in) {
        super(in);
        this.selStart = in.readInt();
        this.selEnd = in.readInt();
        this.frozenWithFocus = in.readInt() != 0;
        this.text = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
        if (in.readInt() != 0) {
            this.error = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
        }
    }
}

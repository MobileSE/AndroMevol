package android.service.notification;

import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.Objects;

public class Condition implements Parcelable {
    public static final Parcelable.Creator<Condition> CREATOR = new Parcelable.Creator<Condition>() {
        /* class android.service.notification.Condition.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public Condition createFromParcel(Parcel source) {
            return new Condition(source);
        }

        @Override // android.os.Parcelable.Creator
        public Condition[] newArray(int size) {
            return new Condition[size];
        }
    };
    public static final int FLAG_RELEVANT_ALWAYS = 2;
    public static final int FLAG_RELEVANT_NOW = 1;
    public static final String SCHEME = "condition";
    public static final int STATE_ERROR = 3;
    public static final int STATE_FALSE = 0;
    public static final int STATE_TRUE = 1;
    public static final int STATE_UNKNOWN = 2;
    public final int flags;
    public final int icon;
    public final Uri id;
    public final String line1;
    public final String line2;
    public final int state;
    public final String summary;

    public Condition(Uri id2, String summary2, String line12, String line22, int icon2, int state2, int flags2) {
        if (id2 == null) {
            throw new IllegalArgumentException("id is required");
        } else if (summary2 == null) {
            throw new IllegalArgumentException("summary is required");
        } else if (line12 == null) {
            throw new IllegalArgumentException("line1 is required");
        } else if (line22 == null) {
            throw new IllegalArgumentException("line2 is required");
        } else if (!isValidState(state2)) {
            throw new IllegalArgumentException("state is invalid: " + state2);
        } else {
            this.id = id2;
            this.summary = summary2;
            this.line1 = line12;
            this.line2 = line22;
            this.icon = icon2;
            this.state = state2;
            this.flags = flags2;
        }
    }

    private Condition(Parcel source) {
        this((Uri) source.readParcelable(Condition.class.getClassLoader()), source.readString(), source.readString(), source.readString(), source.readInt(), source.readInt(), source.readInt());
    }

    private static boolean isValidState(int state2) {
        return state2 >= 0 && state2 <= 3;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags2) {
        dest.writeParcelable(this.id, 0);
        dest.writeString(this.summary);
        dest.writeString(this.line1);
        dest.writeString(this.line2);
        dest.writeInt(this.icon);
        dest.writeInt(this.state);
        dest.writeInt(this.flags);
    }

    public String toString() {
        return Condition.class.getSimpleName() + '[' + "id=" + this.id + ",summary=" + this.summary + ",line1=" + this.line1 + ",line2=" + this.line2 + ",icon=" + this.icon + ",state=" + stateToString(this.state) + ",flags=" + this.flags + ']';
    }

    public static String stateToString(int state2) {
        if (state2 == 0) {
            return "STATE_FALSE";
        }
        if (state2 == 1) {
            return "STATE_TRUE";
        }
        if (state2 == 2) {
            return "STATE_UNKNOWN";
        }
        if (state2 == 3) {
            return "STATE_ERROR";
        }
        throw new IllegalArgumentException("state is invalid: " + state2);
    }

    public static String relevanceToString(int flags2) {
        boolean now;
        boolean always = true;
        if ((flags2 & 1) != 0) {
            now = true;
        } else {
            now = false;
        }
        if ((flags2 & 2) == 0) {
            always = false;
        }
        if (!now && !always) {
            return "NONE";
        }
        if (!now || !always) {
            return now ? "NOW" : "ALWAYS";
        }
        return "NOW, ALWAYS";
    }

    public boolean equals(Object o) {
        if (!(o instanceof Condition)) {
            return false;
        }
        if (o == this) {
            return true;
        }
        Condition other = (Condition) o;
        return Objects.equals(other.id, this.id) && Objects.equals(other.summary, this.summary) && Objects.equals(other.line1, this.line1) && Objects.equals(other.line2, this.line2) && other.icon == this.icon && other.state == this.state && other.flags == this.flags;
    }

    public int hashCode() {
        return Objects.hash(this.id, this.summary, this.line1, this.line2, Integer.valueOf(this.icon), Integer.valueOf(this.state), Integer.valueOf(this.flags));
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public Condition copy() {
        Parcel parcel = Parcel.obtain();
        try {
            writeToParcel(parcel, 0);
            parcel.setDataPosition(0);
            return new Condition(parcel);
        } finally {
            parcel.recycle();
        }
    }

    public static Uri.Builder newId(Context context) {
        return new Uri.Builder().scheme(SCHEME).authority(context.getPackageName());
    }

    public static boolean isValidId(Uri id2, String pkg) {
        return id2 != null && id2.getScheme().equals(SCHEME) && id2.getAuthority().equals(pkg);
    }
}

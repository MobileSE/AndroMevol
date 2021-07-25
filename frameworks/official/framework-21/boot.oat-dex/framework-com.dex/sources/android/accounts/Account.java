package android.accounts;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public class Account implements Parcelable {
    public static final Parcelable.Creator<Account> CREATOR = new Parcelable.Creator<Account>() {
        /* class android.accounts.Account.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public Account createFromParcel(Parcel source) {
            return new Account(source);
        }

        @Override // android.os.Parcelable.Creator
        public Account[] newArray(int size) {
            return new Account[size];
        }
    };
    public final String name;
    public final String type;

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Account)) {
            return false;
        }
        Account other = (Account) o;
        return this.name.equals(other.name) && this.type.equals(other.type);
    }

    public int hashCode() {
        return ((this.name.hashCode() + 527) * 31) + this.type.hashCode();
    }

    public Account(String name2, String type2) {
        if (TextUtils.isEmpty(name2)) {
            throw new IllegalArgumentException("the name must not be empty: " + name2);
        } else if (TextUtils.isEmpty(type2)) {
            throw new IllegalArgumentException("the type must not be empty: " + type2);
        } else {
            this.name = name2;
            this.type = type2;
        }
    }

    public Account(Parcel in) {
        this.name = in.readString();
        this.type = in.readString();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.type);
    }

    public String toString() {
        return "Account {name=" + this.name + ", type=" + this.type + "}";
    }
}

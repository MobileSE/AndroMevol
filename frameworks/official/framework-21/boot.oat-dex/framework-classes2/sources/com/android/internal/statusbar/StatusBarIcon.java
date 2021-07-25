package com.android.internal.statusbar;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.UserHandle;

public class StatusBarIcon implements Parcelable {
    public static final Parcelable.Creator<StatusBarIcon> CREATOR = new Parcelable.Creator<StatusBarIcon>() {
        /* class com.android.internal.statusbar.StatusBarIcon.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public StatusBarIcon createFromParcel(Parcel parcel) {
            return new StatusBarIcon(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public StatusBarIcon[] newArray(int size) {
            return new StatusBarIcon[size];
        }
    };
    public CharSequence contentDescription;
    public int iconId;
    public int iconLevel;
    public String iconPackage;
    public int number;
    public UserHandle user;
    public boolean visible = true;

    public StatusBarIcon(String iconPackage2, UserHandle user2, int iconId2, int iconLevel2, int number2, CharSequence contentDescription2) {
        this.iconPackage = iconPackage2;
        this.user = user2;
        this.iconId = iconId2;
        this.iconLevel = iconLevel2;
        this.number = number2;
        this.contentDescription = contentDescription2;
    }

    public String toString() {
        return "StatusBarIcon(pkg=" + this.iconPackage + "user=" + this.user.getIdentifier() + " id=0x" + Integer.toHexString(this.iconId) + " level=" + this.iconLevel + " visible=" + this.visible + " num=" + this.number + " )";
    }

    @Override // java.lang.Object
    public StatusBarIcon clone() {
        StatusBarIcon that = new StatusBarIcon(this.iconPackage, this.user, this.iconId, this.iconLevel, this.number, this.contentDescription);
        that.visible = this.visible;
        return that;
    }

    public StatusBarIcon(Parcel in) {
        readFromParcel(in);
    }

    public void readFromParcel(Parcel in) {
        this.iconPackage = in.readString();
        this.user = (UserHandle) in.readParcelable(null);
        this.iconId = in.readInt();
        this.iconLevel = in.readInt();
        this.visible = in.readInt() != 0;
        this.number = in.readInt();
        this.contentDescription = in.readCharSequence();
    }

    public void writeToParcel(Parcel out, int flags) {
        int i = 0;
        out.writeString(this.iconPackage);
        out.writeParcelable(this.user, 0);
        out.writeInt(this.iconId);
        out.writeInt(this.iconLevel);
        if (this.visible) {
            i = 1;
        }
        out.writeInt(i);
        out.writeInt(this.number);
        out.writeCharSequence(this.contentDescription);
    }

    public int describeContents() {
        return 0;
    }
}

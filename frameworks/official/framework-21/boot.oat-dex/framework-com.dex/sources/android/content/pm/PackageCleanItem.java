package android.content.pm;

import android.os.Parcel;
import android.os.Parcelable;

public class PackageCleanItem {
    public static final Parcelable.Creator<PackageCleanItem> CREATOR = new Parcelable.Creator<PackageCleanItem>() {
        /* class android.content.pm.PackageCleanItem.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public PackageCleanItem createFromParcel(Parcel source) {
            return new PackageCleanItem(source);
        }

        @Override // android.os.Parcelable.Creator
        public PackageCleanItem[] newArray(int size) {
            return new PackageCleanItem[size];
        }
    };
    public final boolean andCode;
    public final String packageName;
    public final int userId;

    public PackageCleanItem(int userId2, String packageName2, boolean andCode2) {
        this.userId = userId2;
        this.packageName = packageName2;
        this.andCode = andCode2;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj != null) {
            try {
                PackageCleanItem other = (PackageCleanItem) obj;
                return this.userId == other.userId && this.packageName.equals(other.packageName) && this.andCode == other.andCode;
            } catch (ClassCastException e) {
            }
        }
        return false;
    }

    public int hashCode() {
        return ((((this.userId + 527) * 31) + this.packageName.hashCode()) * 31) + (this.andCode ? 1 : 0);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int parcelableFlags) {
        dest.writeInt(this.userId);
        dest.writeString(this.packageName);
        dest.writeInt(this.andCode ? 1 : 0);
    }

    private PackageCleanItem(Parcel source) {
        this.userId = source.readInt();
        this.packageName = source.readString();
        this.andCode = source.readInt() != 0;
    }
}

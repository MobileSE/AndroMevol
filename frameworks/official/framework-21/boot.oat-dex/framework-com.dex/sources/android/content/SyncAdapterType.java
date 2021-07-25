package android.content;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public class SyncAdapterType implements Parcelable {
    public static final Parcelable.Creator<SyncAdapterType> CREATOR = new Parcelable.Creator<SyncAdapterType>() {
        /* class android.content.SyncAdapterType.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public SyncAdapterType createFromParcel(Parcel source) {
            return new SyncAdapterType(source);
        }

        @Override // android.os.Parcelable.Creator
        public SyncAdapterType[] newArray(int size) {
            return new SyncAdapterType[size];
        }
    };
    public final String accountType;
    private final boolean allowParallelSyncs;
    public final String authority;
    private final boolean isAlwaysSyncable;
    public final boolean isKey;
    private final String settingsActivity;
    private final boolean supportsUploading;
    private final boolean userVisible;

    public SyncAdapterType(String authority2, String accountType2, boolean userVisible2, boolean supportsUploading2) {
        if (TextUtils.isEmpty(authority2)) {
            throw new IllegalArgumentException("the authority must not be empty: " + authority2);
        } else if (TextUtils.isEmpty(accountType2)) {
            throw new IllegalArgumentException("the accountType must not be empty: " + accountType2);
        } else {
            this.authority = authority2;
            this.accountType = accountType2;
            this.userVisible = userVisible2;
            this.supportsUploading = supportsUploading2;
            this.isAlwaysSyncable = false;
            this.allowParallelSyncs = false;
            this.settingsActivity = null;
            this.isKey = false;
        }
    }

    public SyncAdapterType(String authority2, String accountType2, boolean userVisible2, boolean supportsUploading2, boolean isAlwaysSyncable2, boolean allowParallelSyncs2, String settingsActivity2) {
        if (TextUtils.isEmpty(authority2)) {
            throw new IllegalArgumentException("the authority must not be empty: " + authority2);
        } else if (TextUtils.isEmpty(accountType2)) {
            throw new IllegalArgumentException("the accountType must not be empty: " + accountType2);
        } else {
            this.authority = authority2;
            this.accountType = accountType2;
            this.userVisible = userVisible2;
            this.supportsUploading = supportsUploading2;
            this.isAlwaysSyncable = isAlwaysSyncable2;
            this.allowParallelSyncs = allowParallelSyncs2;
            this.settingsActivity = settingsActivity2;
            this.isKey = false;
        }
    }

    private SyncAdapterType(String authority2, String accountType2) {
        if (TextUtils.isEmpty(authority2)) {
            throw new IllegalArgumentException("the authority must not be empty: " + authority2);
        } else if (TextUtils.isEmpty(accountType2)) {
            throw new IllegalArgumentException("the accountType must not be empty: " + accountType2);
        } else {
            this.authority = authority2;
            this.accountType = accountType2;
            this.userVisible = true;
            this.supportsUploading = true;
            this.isAlwaysSyncable = false;
            this.allowParallelSyncs = false;
            this.settingsActivity = null;
            this.isKey = true;
        }
    }

    public boolean supportsUploading() {
        if (!this.isKey) {
            return this.supportsUploading;
        }
        throw new IllegalStateException("this method is not allowed to be called when this is a key");
    }

    public boolean isUserVisible() {
        if (!this.isKey) {
            return this.userVisible;
        }
        throw new IllegalStateException("this method is not allowed to be called when this is a key");
    }

    public boolean allowParallelSyncs() {
        if (!this.isKey) {
            return this.allowParallelSyncs;
        }
        throw new IllegalStateException("this method is not allowed to be called when this is a key");
    }

    public boolean isAlwaysSyncable() {
        if (!this.isKey) {
            return this.isAlwaysSyncable;
        }
        throw new IllegalStateException("this method is not allowed to be called when this is a key");
    }

    public String getSettingsActivity() {
        if (!this.isKey) {
            return this.settingsActivity;
        }
        throw new IllegalStateException("this method is not allowed to be called when this is a key");
    }

    public static SyncAdapterType newKey(String authority2, String accountType2) {
        return new SyncAdapterType(authority2, accountType2);
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof SyncAdapterType)) {
            return false;
        }
        SyncAdapterType other = (SyncAdapterType) o;
        return this.authority.equals(other.authority) && this.accountType.equals(other.accountType);
    }

    public int hashCode() {
        return ((this.authority.hashCode() + 527) * 31) + this.accountType.hashCode();
    }

    public String toString() {
        if (this.isKey) {
            return "SyncAdapterType Key {name=" + this.authority + ", type=" + this.accountType + "}";
        }
        return "SyncAdapterType {name=" + this.authority + ", type=" + this.accountType + ", userVisible=" + this.userVisible + ", supportsUploading=" + this.supportsUploading + ", isAlwaysSyncable=" + this.isAlwaysSyncable + ", allowParallelSyncs=" + this.allowParallelSyncs + ", settingsActivity=" + this.settingsActivity + "}";
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        int i;
        int i2;
        int i3 = 1;
        if (this.isKey) {
            throw new IllegalStateException("keys aren't parcelable");
        }
        dest.writeString(this.authority);
        dest.writeString(this.accountType);
        dest.writeInt(this.userVisible ? 1 : 0);
        if (this.supportsUploading) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.isAlwaysSyncable) {
            i2 = 1;
        } else {
            i2 = 0;
        }
        dest.writeInt(i2);
        if (!this.allowParallelSyncs) {
            i3 = 0;
        }
        dest.writeInt(i3);
        dest.writeString(this.settingsActivity);
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public SyncAdapterType(Parcel source) {
        this(source.readString(), source.readString(), source.readInt() != 0, source.readInt() != 0, source.readInt() != 0, source.readInt() != 0, source.readString());
    }
}

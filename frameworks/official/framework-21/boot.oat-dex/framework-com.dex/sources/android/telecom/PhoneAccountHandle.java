package android.telecom;

import android.content.ComponentName;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.Objects;

public class PhoneAccountHandle implements Parcelable {
    public static final Parcelable.Creator<PhoneAccountHandle> CREATOR = new Parcelable.Creator<PhoneAccountHandle>() {
        /* class android.telecom.PhoneAccountHandle.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public PhoneAccountHandle createFromParcel(Parcel in) {
            return new PhoneAccountHandle(in);
        }

        @Override // android.os.Parcelable.Creator
        public PhoneAccountHandle[] newArray(int size) {
            return new PhoneAccountHandle[size];
        }
    };
    private ComponentName mComponentName;
    private String mId;

    public PhoneAccountHandle(ComponentName componentName, String id) {
        this.mComponentName = componentName;
        this.mId = id;
    }

    public ComponentName getComponentName() {
        return this.mComponentName;
    }

    public String getId() {
        return this.mId;
    }

    public int hashCode() {
        return Objects.hashCode(this.mComponentName) + Objects.hashCode(this.mId);
    }

    public String toString() {
        return this.mComponentName + ", " + this.mId;
    }

    public boolean equals(Object other) {
        return other != null && (other instanceof PhoneAccountHandle) && Objects.equals(((PhoneAccountHandle) other).getComponentName(), getComponentName()) && Objects.equals(((PhoneAccountHandle) other).getId(), getId());
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeParcelable(this.mComponentName, flags);
        out.writeString(this.mId);
    }

    private PhoneAccountHandle(Parcel in) {
        this.mComponentName = (ComponentName) in.readParcelable(getClass().getClassLoader());
        this.mId = in.readString();
    }
}

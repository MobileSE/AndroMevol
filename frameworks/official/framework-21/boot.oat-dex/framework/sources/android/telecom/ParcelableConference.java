package android.telecom;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

public final class ParcelableConference implements Parcelable {
    public static final Parcelable.Creator<ParcelableConference> CREATOR = new Parcelable.Creator<ParcelableConference>() {
        /* class android.telecom.ParcelableConference.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ParcelableConference createFromParcel(Parcel source) {
            ClassLoader classLoader = ParcelableConference.class.getClassLoader();
            int state = source.readInt();
            int capabilities = source.readInt();
            List<String> connectionIds = new ArrayList<>(2);
            source.readList(connectionIds, classLoader);
            return new ParcelableConference((PhoneAccountHandle) source.readParcelable(classLoader), state, capabilities, connectionIds);
        }

        @Override // android.os.Parcelable.Creator
        public ParcelableConference[] newArray(int size) {
            return new ParcelableConference[size];
        }
    };
    private int mCapabilities;
    private List<String> mConnectionIds;
    private PhoneAccountHandle mPhoneAccount;
    private int mState;

    public ParcelableConference(PhoneAccountHandle phoneAccount, int state, int capabilities, List<String> connectionIds) {
        this.mPhoneAccount = phoneAccount;
        this.mState = state;
        this.mCapabilities = capabilities;
        this.mConnectionIds = connectionIds;
    }

    public String toString() {
        return new StringBuffer().append("account: ").append(this.mPhoneAccount).append(", state: ").append(Connection.stateToString(this.mState)).append(", capabilities: ").append(PhoneCapabilities.toString(this.mCapabilities)).append(", children: ").append(this.mConnectionIds).toString();
    }

    public PhoneAccountHandle getPhoneAccount() {
        return this.mPhoneAccount;
    }

    public int getState() {
        return this.mState;
    }

    public int getCapabilities() {
        return this.mCapabilities;
    }

    public List<String> getConnectionIds() {
        return this.mConnectionIds;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel destination, int flags) {
        destination.writeParcelable(this.mPhoneAccount, 0);
        destination.writeInt(this.mState);
        destination.writeInt(this.mCapabilities);
        destination.writeList(this.mConnectionIds);
    }
}

package android.location;

import android.os.Parcel;
import android.os.Parcelable;
import java.security.InvalidParameterException;

public class GpsNavigationMessageEvent implements Parcelable {
    public static final Parcelable.Creator<GpsNavigationMessageEvent> CREATOR = new Parcelable.Creator<GpsNavigationMessageEvent>() {
        /* class android.location.GpsNavigationMessageEvent.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public GpsNavigationMessageEvent createFromParcel(Parcel in) {
            return new GpsNavigationMessageEvent((GpsNavigationMessage) in.readParcelable(getClass().getClassLoader()));
        }

        @Override // android.os.Parcelable.Creator
        public GpsNavigationMessageEvent[] newArray(int size) {
            return new GpsNavigationMessageEvent[size];
        }
    };
    private final GpsNavigationMessage mNavigationMessage;

    public interface Listener {
        void onGpsNavigationMessageReceived(GpsNavigationMessageEvent gpsNavigationMessageEvent);
    }

    public GpsNavigationMessageEvent(GpsNavigationMessage message) {
        if (message == null) {
            throw new InvalidParameterException("Parameter 'message' must not be null.");
        }
        this.mNavigationMessage = message;
    }

    public GpsNavigationMessage getNavigationMessage() {
        return this.mNavigationMessage;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeParcelable(this.mNavigationMessage, flags);
    }

    public String toString() {
        return "[ GpsNavigationMessageEvent:\n\n" + this.mNavigationMessage.toString() + "\n]";
    }
}

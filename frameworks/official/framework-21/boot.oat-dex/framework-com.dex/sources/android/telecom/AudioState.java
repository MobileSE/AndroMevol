package android.telecom;

import android.os.Parcel;
import android.os.Parcelable;
import com.android.internal.telephony.IccCardConstants;
import java.util.Locale;

public final class AudioState implements Parcelable {
    public static final Parcelable.Creator<AudioState> CREATOR = new Parcelable.Creator<AudioState>() {
        /* class android.telecom.AudioState.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AudioState createFromParcel(Parcel source) {
            return new AudioState(source.readByte() != 0, source.readInt(), source.readInt());
        }

        @Override // android.os.Parcelable.Creator
        public AudioState[] newArray(int size) {
            return new AudioState[size];
        }
    };
    public static final int ROUTE_ALL = 15;
    public static final int ROUTE_BLUETOOTH = 2;
    public static final int ROUTE_EARPIECE = 1;
    public static final int ROUTE_SPEAKER = 8;
    public static final int ROUTE_WIRED_HEADSET = 4;
    public static final int ROUTE_WIRED_OR_EARPIECE = 5;
    public final boolean isMuted;
    public final int route;
    public final int supportedRouteMask;

    public AudioState(boolean isMuted2, int route2, int supportedRouteMask2) {
        this.isMuted = isMuted2;
        this.route = route2;
        this.supportedRouteMask = supportedRouteMask2;
    }

    public AudioState(AudioState state) {
        this.isMuted = state.isMuted;
        this.route = state.route;
        this.supportedRouteMask = state.supportedRouteMask;
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof AudioState)) {
            return false;
        }
        AudioState state = (AudioState) obj;
        if (this.isMuted == state.isMuted && this.route == state.route && this.supportedRouteMask == state.supportedRouteMask) {
            return true;
        }
        return false;
    }

    public String toString() {
        return String.format(Locale.US, "[AudioState isMuted: %b, route; %s, supportedRouteMask: %s]", Boolean.valueOf(this.isMuted), audioRouteToString(this.route), audioRouteToString(this.supportedRouteMask));
    }

    public static String audioRouteToString(int route2) {
        if (route2 == 0 || (route2 & -16) != 0) {
            return IccCardConstants.INTENT_VALUE_ICC_UNKNOWN;
        }
        StringBuffer buffer = new StringBuffer();
        if ((route2 & 1) == 1) {
            listAppend(buffer, "EARPIECE");
        }
        if ((route2 & 2) == 2) {
            listAppend(buffer, "BLUETOOTH");
        }
        if ((route2 & 4) == 4) {
            listAppend(buffer, "WIRED_HEADSET");
        }
        if ((route2 & 8) == 8) {
            listAppend(buffer, "SPEAKER");
        }
        return buffer.toString();
    }

    private static void listAppend(StringBuffer buffer, String str) {
        if (buffer.length() > 0) {
            buffer.append(", ");
        }
        buffer.append(str);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel destination, int flags) {
        destination.writeByte((byte) (this.isMuted ? 1 : 0));
        destination.writeInt(this.route);
        destination.writeInt(this.supportedRouteMask);
    }
}

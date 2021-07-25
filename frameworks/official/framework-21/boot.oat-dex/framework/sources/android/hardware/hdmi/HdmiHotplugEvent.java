package android.hardware.hdmi;

import android.os.Parcel;
import android.os.Parcelable;

public final class HdmiHotplugEvent implements Parcelable {
    public static final Parcelable.Creator<HdmiHotplugEvent> CREATOR = new Parcelable.Creator<HdmiHotplugEvent>() {
        /* class android.hardware.hdmi.HdmiHotplugEvent.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public HdmiHotplugEvent createFromParcel(Parcel p) {
            boolean connected = true;
            int port = p.readInt();
            if (p.readByte() != 1) {
                connected = false;
            }
            return new HdmiHotplugEvent(port, connected);
        }

        @Override // android.os.Parcelable.Creator
        public HdmiHotplugEvent[] newArray(int size) {
            return new HdmiHotplugEvent[size];
        }
    };
    private final boolean mConnected;
    private final int mPort;

    public HdmiHotplugEvent(int port, boolean connected) {
        this.mPort = port;
        this.mConnected = connected;
    }

    public int getPort() {
        return this.mPort;
    }

    public boolean isConnected() {
        return this.mConnected;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mPort);
        dest.writeByte((byte) (this.mConnected ? 1 : 0));
    }
}

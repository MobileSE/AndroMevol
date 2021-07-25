package android.net;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Pair;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

public final class IpPrefix implements Parcelable {
    public static final Parcelable.Creator<IpPrefix> CREATOR = new Parcelable.Creator<IpPrefix>() {
        /* class android.net.IpPrefix.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public IpPrefix createFromParcel(Parcel in) {
            return new IpPrefix(in.createByteArray(), in.readInt());
        }

        @Override // android.os.Parcelable.Creator
        public IpPrefix[] newArray(int size) {
            return new IpPrefix[size];
        }
    };
    private final byte[] address;
    private final int prefixLength;

    private void checkAndMaskAddressAndPrefixLength() {
        if (this.address.length == 4 || this.address.length == 16) {
            NetworkUtils.maskRawAddress(this.address, this.prefixLength);
            return;
        }
        throw new IllegalArgumentException("IpPrefix has " + this.address.length + " bytes which is neither 4 nor 16");
    }

    public IpPrefix(byte[] address2, int prefixLength2) {
        this.address = (byte[]) address2.clone();
        this.prefixLength = prefixLength2;
        checkAndMaskAddressAndPrefixLength();
    }

    public IpPrefix(InetAddress address2, int prefixLength2) {
        this.address = address2.getAddress();
        this.prefixLength = prefixLength2;
        checkAndMaskAddressAndPrefixLength();
    }

    public IpPrefix(String prefix) {
        Pair<InetAddress, Integer> ipAndMask = NetworkUtils.parseIpAndMask(prefix);
        this.address = ipAndMask.first.getAddress();
        this.prefixLength = ipAndMask.second.intValue();
        checkAndMaskAddressAndPrefixLength();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof IpPrefix)) {
            return false;
        }
        IpPrefix that = (IpPrefix) obj;
        if (!Arrays.equals(this.address, that.address) || this.prefixLength != that.prefixLength) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return Arrays.hashCode(this.address) + (this.prefixLength * 11);
    }

    public InetAddress getAddress() {
        try {
            return InetAddress.getByAddress(this.address);
        } catch (UnknownHostException e) {
            return null;
        }
    }

    public byte[] getRawAddress() {
        return (byte[]) this.address.clone();
    }

    public int getPrefixLength() {
        return this.prefixLength;
    }

    public String toString() {
        try {
            return InetAddress.getByAddress(this.address).getHostAddress() + "/" + this.prefixLength;
        } catch (UnknownHostException e) {
            throw new IllegalStateException("IpPrefix with invalid address! Shouldn't happen.", e);
        }
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByteArray(this.address);
        dest.writeInt(this.prefixLength);
    }
}

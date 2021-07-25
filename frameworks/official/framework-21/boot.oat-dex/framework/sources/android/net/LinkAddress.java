package android.net;

import android.os.Parcel;
import android.os.Parcelable;
import android.system.OsConstants;
import android.util.Pair;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.UnknownHostException;

public class LinkAddress implements Parcelable {
    public static final Parcelable.Creator<LinkAddress> CREATOR = new Parcelable.Creator<LinkAddress>() {
        /* class android.net.LinkAddress.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public LinkAddress createFromParcel(Parcel in) {
            InetAddress address = null;
            try {
                address = InetAddress.getByAddress(in.createByteArray());
            } catch (UnknownHostException e) {
            }
            return new LinkAddress(address, in.readInt(), in.readInt(), in.readInt());
        }

        @Override // android.os.Parcelable.Creator
        public LinkAddress[] newArray(int size) {
            return new LinkAddress[size];
        }
    };
    private InetAddress address;
    private int flags;
    private int prefixLength;
    private int scope;

    static int scopeForUnicastAddress(InetAddress addr) {
        if (addr.isAnyLocalAddress()) {
            return OsConstants.RT_SCOPE_HOST;
        }
        if (addr.isLoopbackAddress() || addr.isLinkLocalAddress()) {
            return OsConstants.RT_SCOPE_LINK;
        }
        if ((addr instanceof Inet4Address) || !addr.isSiteLocalAddress()) {
            return OsConstants.RT_SCOPE_UNIVERSE;
        }
        return OsConstants.RT_SCOPE_SITE;
    }

    private void init(InetAddress address2, int prefixLength2, int flags2, int scope2) {
        if (address2 == null || address2.isMulticastAddress() || prefixLength2 < 0 || (((address2 instanceof Inet4Address) && prefixLength2 > 32) || prefixLength2 > 128)) {
            throw new IllegalArgumentException("Bad LinkAddress params " + address2 + "/" + prefixLength2);
        }
        this.address = address2;
        this.prefixLength = prefixLength2;
        this.flags = flags2;
        this.scope = scope2;
    }

    public LinkAddress(InetAddress address2, int prefixLength2, int flags2, int scope2) {
        init(address2, prefixLength2, flags2, scope2);
    }

    public LinkAddress(InetAddress address2, int prefixLength2) {
        this(address2, prefixLength2, 0, 0);
        this.scope = scopeForUnicastAddress(address2);
    }

    public LinkAddress(InterfaceAddress interfaceAddress) {
        this(interfaceAddress.getAddress(), interfaceAddress.getNetworkPrefixLength());
    }

    public LinkAddress(String address2) {
        this(address2, 0, 0);
        this.scope = scopeForUnicastAddress(this.address);
    }

    public LinkAddress(String address2, int flags2, int scope2) {
        Pair<InetAddress, Integer> ipAndMask = NetworkUtils.parseIpAndMask(address2);
        init(ipAndMask.first, ipAndMask.second.intValue(), flags2, scope2);
    }

    public String toString() {
        return this.address.getHostAddress() + "/" + this.prefixLength;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof LinkAddress)) {
            return false;
        }
        LinkAddress linkAddress = (LinkAddress) obj;
        if (this.address.equals(linkAddress.address) && this.prefixLength == linkAddress.prefixLength && this.flags == linkAddress.flags && this.scope == linkAddress.scope) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return this.address.hashCode() + (this.prefixLength * 11) + (this.flags * 19) + (this.scope * 43);
    }

    public boolean isSameAddressAs(LinkAddress other) {
        return this.address.equals(other.address) && this.prefixLength == other.prefixLength;
    }

    public InetAddress getAddress() {
        return this.address;
    }

    public int getPrefixLength() {
        return this.prefixLength;
    }

    public int getNetworkPrefixLength() {
        return getPrefixLength();
    }

    public int getFlags() {
        return this.flags;
    }

    public int getScope() {
        return this.scope;
    }

    public boolean isGlobalPreferred() {
        return this.scope == OsConstants.RT_SCOPE_UNIVERSE && ((long) (this.flags & ((OsConstants.IFA_F_DADFAILED | OsConstants.IFA_F_DEPRECATED) | OsConstants.IFA_F_TENTATIVE))) == 0;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags2) {
        dest.writeByteArray(this.address.getAddress());
        dest.writeInt(this.prefixLength);
        dest.writeInt(this.flags);
        dest.writeInt(this.scope);
    }
}

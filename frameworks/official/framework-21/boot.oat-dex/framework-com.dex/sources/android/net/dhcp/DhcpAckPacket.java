package android.net.dhcp;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Iterator;

/* access modifiers changed from: package-private */
public class DhcpAckPacket extends DhcpPacket {
    private final InetAddress mSrcIp;

    DhcpAckPacket(int transId, boolean broadcast, InetAddress serverAddress, InetAddress clientIp, byte[] clientMac) {
        super(transId, Inet4Address.ANY, clientIp, serverAddress, Inet4Address.ANY, clientMac, broadcast);
        this.mBroadcast = broadcast;
        this.mSrcIp = serverAddress;
    }

    @Override // android.net.dhcp.DhcpPacket
    public String toString() {
        String s = super.toString();
        String dnsServers = " DNS servers: ";
        Iterator i$ = this.mDnsServers.iterator();
        while (i$.hasNext()) {
            dnsServers = dnsServers + ((InetAddress) i$.next()).toString() + " ";
        }
        return s + " ACK: your new IP " + this.mYourIp + ", netmask " + this.mSubnetMask + ", gateway " + this.mGateway + dnsServers + ", lease time " + this.mLeaseTime;
    }

    @Override // android.net.dhcp.DhcpPacket
    public ByteBuffer buildPacket(int encap, short destUdp, short srcUdp) {
        ByteBuffer result = ByteBuffer.allocate(1500);
        fillInPacket(encap, this.mBroadcast ? Inet4Address.ALL : this.mYourIp, this.mBroadcast ? Inet4Address.ANY : this.mSrcIp, destUdp, srcUdp, result, (byte) 2, this.mBroadcast);
        result.flip();
        return result;
    }

    /* access modifiers changed from: package-private */
    @Override // android.net.dhcp.DhcpPacket
    public void finishPacket(ByteBuffer buffer) {
        addTlv(buffer, (byte) 53, (byte) 5);
        addTlv(buffer, (byte) 54, this.mServerIdentifier);
        addTlv(buffer, (byte) 51, this.mLeaseTime);
        if (this.mLeaseTime != null) {
            addTlv(buffer, (byte) 58, Integer.valueOf(this.mLeaseTime.intValue() / 2));
        }
        addTlv(buffer, (byte) 1, this.mSubnetMask);
        addTlv(buffer, (byte) 3, this.mGateway);
        addTlv(buffer, (byte) 15, this.mDomainName);
        addTlv(buffer, (byte) 28, this.mBroadcastAddress);
        addTlv(buffer, (byte) 6, this.mDnsServers);
        addTlvEnd(buffer);
    }

    private static final int getInt(Integer v) {
        if (v == null) {
            return 0;
        }
        return v.intValue();
    }

    @Override // android.net.dhcp.DhcpPacket
    public void doNextOp(DhcpStateMachine machine) {
        machine.onAckReceived(this.mYourIp, this.mSubnetMask, this.mGateway, this.mDnsServers, this.mServerIdentifier, getInt(this.mLeaseTime));
    }
}

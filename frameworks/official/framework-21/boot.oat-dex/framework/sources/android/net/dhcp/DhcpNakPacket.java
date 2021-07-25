package android.net.dhcp;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.nio.ByteBuffer;

/* access modifiers changed from: package-private */
public class DhcpNakPacket extends DhcpPacket {
    DhcpNakPacket(int transId, InetAddress clientIp, InetAddress yourIp, InetAddress nextIp, InetAddress relayIp, byte[] clientMac) {
        super(transId, Inet4Address.ANY, Inet4Address.ANY, nextIp, relayIp, clientMac, false);
    }

    @Override // android.net.dhcp.DhcpPacket
    public String toString() {
        return super.toString() + " NAK, reason " + (this.mMessage == null ? "(none)" : this.mMessage);
    }

    @Override // android.net.dhcp.DhcpPacket
    public ByteBuffer buildPacket(int encap, short destUdp, short srcUdp) {
        ByteBuffer result = ByteBuffer.allocate(1500);
        fillInPacket(encap, this.mClientIp, this.mYourIp, destUdp, srcUdp, result, (byte) 2, this.mBroadcast);
        result.flip();
        return result;
    }

    /* access modifiers changed from: package-private */
    @Override // android.net.dhcp.DhcpPacket
    public void finishPacket(ByteBuffer buffer) {
        addTlv(buffer, (byte) 53, (byte) 6);
        addTlv(buffer, (byte) 54, this.mServerIdentifier);
        addTlv(buffer, (byte) 56, this.mMessage);
        addTlvEnd(buffer);
    }

    @Override // android.net.dhcp.DhcpPacket
    public void doNextOp(DhcpStateMachine machine) {
        machine.onNakReceived();
    }
}

package android.net.dhcp;

import java.net.InetAddress;
import java.nio.ByteBuffer;

/* access modifiers changed from: package-private */
public class DhcpInformPacket extends DhcpPacket {
    DhcpInformPacket(int transId, InetAddress clientIp, InetAddress yourIp, InetAddress nextIp, InetAddress relayIp, byte[] clientMac) {
        super(transId, clientIp, yourIp, nextIp, relayIp, clientMac, false);
    }

    @Override // android.net.dhcp.DhcpPacket
    public String toString() {
        return super.toString() + " INFORM";
    }

    @Override // android.net.dhcp.DhcpPacket
    public ByteBuffer buildPacket(int encap, short destUdp, short srcUdp) {
        ByteBuffer result = ByteBuffer.allocate(1500);
        fillInPacket(encap, this.mClientIp, this.mYourIp, destUdp, srcUdp, result, (byte) 1, false);
        result.flip();
        return result;
    }

    /* access modifiers changed from: package-private */
    @Override // android.net.dhcp.DhcpPacket
    public void finishPacket(ByteBuffer buffer) {
        byte[] clientId = new byte[7];
        clientId[0] = 1;
        System.arraycopy(this.mClientMac, 0, clientId, 1, 6);
        addTlv(buffer, (byte) 53, (byte) 3);
        addTlv(buffer, (byte) 55, this.mRequestedParams);
        addTlvEnd(buffer);
    }

    @Override // android.net.dhcp.DhcpPacket
    public void doNextOp(DhcpStateMachine machine) {
        machine.onInformReceived(this.mTransId, this.mClientMac, this.mRequestedIp == null ? this.mClientIp : this.mRequestedIp, this.mRequestedParams);
    }
}

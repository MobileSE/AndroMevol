package android.net;

import android.os.Parcel;
import android.os.Parcelable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class StaticIpConfiguration implements Parcelable {
    public static Parcelable.Creator<StaticIpConfiguration> CREATOR = new Parcelable.Creator<StaticIpConfiguration>() {
        /* class android.net.StaticIpConfiguration.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public StaticIpConfiguration createFromParcel(Parcel in) {
            StaticIpConfiguration s = new StaticIpConfiguration();
            StaticIpConfiguration.readFromParcel(s, in);
            return s;
        }

        @Override // android.os.Parcelable.Creator
        public StaticIpConfiguration[] newArray(int size) {
            return new StaticIpConfiguration[size];
        }
    };
    public final ArrayList<InetAddress> dnsServers;
    public String domains;
    public InetAddress gateway;
    public LinkAddress ipAddress;

    public StaticIpConfiguration() {
        this.dnsServers = new ArrayList<>();
    }

    public StaticIpConfiguration(StaticIpConfiguration source) {
        this();
        if (source != null) {
            this.ipAddress = source.ipAddress;
            this.gateway = source.gateway;
            this.dnsServers.addAll(source.dnsServers);
            this.domains = source.domains;
        }
    }

    public void clear() {
        this.ipAddress = null;
        this.gateway = null;
        this.dnsServers.clear();
        this.domains = null;
    }

    public List<RouteInfo> getRoutes(String iface) {
        List<RouteInfo> routes = new ArrayList<>(2);
        if (this.ipAddress != null) {
            routes.add(new RouteInfo(this.ipAddress, (InetAddress) null, iface));
        }
        if (this.gateway != null) {
            routes.add(new RouteInfo((LinkAddress) null, this.gateway, iface));
        }
        return routes;
    }

    public LinkProperties toLinkProperties(String iface) {
        LinkProperties lp = new LinkProperties();
        lp.setInterfaceName(iface);
        if (this.ipAddress != null) {
            lp.addLinkAddress(this.ipAddress);
        }
        for (RouteInfo route : getRoutes(iface)) {
            lp.addRoute(route);
        }
        Iterator i$ = this.dnsServers.iterator();
        while (i$.hasNext()) {
            lp.addDnsServer(i$.next());
        }
        return lp;
    }

    public String toString() {
        StringBuffer str = new StringBuffer();
        str.append("IP address ");
        if (this.ipAddress != null) {
            str.append(this.ipAddress).append(" ");
        }
        str.append("Gateway ");
        if (this.gateway != null) {
            str.append(this.gateway.getHostAddress()).append(" ");
        }
        str.append(" DNS servers: [");
        Iterator i$ = this.dnsServers.iterator();
        while (i$.hasNext()) {
            str.append(" ").append(i$.next().getHostAddress());
        }
        str.append(" ] Domains");
        if (this.domains != null) {
            str.append(this.domains);
        }
        return str.toString();
    }

    public int hashCode() {
        int i = 0;
        int hashCode = ((((this.ipAddress == null ? 0 : this.ipAddress.hashCode()) + 611) * 47) + (this.gateway == null ? 0 : this.gateway.hashCode())) * 47;
        if (this.domains != null) {
            i = this.domains.hashCode();
        }
        return ((hashCode + i) * 47) + this.dnsServers.hashCode();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof StaticIpConfiguration)) {
            return false;
        }
        StaticIpConfiguration other = (StaticIpConfiguration) obj;
        return other != null && Objects.equals(this.ipAddress, other.ipAddress) && Objects.equals(this.gateway, other.gateway) && this.dnsServers.equals(other.dnsServers) && Objects.equals(this.domains, other.domains);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.ipAddress, flags);
        NetworkUtils.parcelInetAddress(dest, this.gateway, flags);
        dest.writeInt(this.dnsServers.size());
        Iterator i$ = this.dnsServers.iterator();
        while (i$.hasNext()) {
            NetworkUtils.parcelInetAddress(dest, i$.next(), flags);
        }
    }

    protected static void readFromParcel(StaticIpConfiguration s, Parcel in) {
        s.ipAddress = (LinkAddress) in.readParcelable(null);
        s.gateway = NetworkUtils.unparcelInetAddress(in);
        s.dnsServers.clear();
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            s.dnsServers.add(NetworkUtils.unparcelInetAddress(in));
        }
    }
}

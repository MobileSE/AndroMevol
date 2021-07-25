package android.net;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public final class LinkProperties implements Parcelable {
    public static final Parcelable.Creator<LinkProperties> CREATOR = new Parcelable.Creator<LinkProperties>() {
        /* class android.net.LinkProperties.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public LinkProperties createFromParcel(Parcel in) {
            LinkProperties netProp = new LinkProperties();
            String iface = in.readString();
            if (iface != null) {
                netProp.setInterfaceName(iface);
            }
            int addressCount = in.readInt();
            for (int i = 0; i < addressCount; i++) {
                netProp.addLinkAddress((LinkAddress) in.readParcelable(null));
            }
            int addressCount2 = in.readInt();
            for (int i2 = 0; i2 < addressCount2; i2++) {
                try {
                    netProp.addDnsServer(InetAddress.getByAddress(in.createByteArray()));
                } catch (UnknownHostException e) {
                }
            }
            netProp.setDomains(in.readString());
            netProp.setMtu(in.readInt());
            netProp.setTcpBufferSizes(in.readString());
            int addressCount3 = in.readInt();
            for (int i3 = 0; i3 < addressCount3; i3++) {
                netProp.addRoute((RouteInfo) in.readParcelable(null));
            }
            if (in.readByte() == 1) {
                netProp.setHttpProxy((ProxyInfo) in.readParcelable(null));
            }
            ArrayList<LinkProperties> stackedLinks = new ArrayList<>();
            in.readList(stackedLinks, LinkProperties.class.getClassLoader());
            Iterator i$ = stackedLinks.iterator();
            while (i$.hasNext()) {
                netProp.addStackedLink(i$.next());
            }
            return netProp;
        }

        @Override // android.os.Parcelable.Creator
        public LinkProperties[] newArray(int size) {
            return new LinkProperties[size];
        }
    };
    private static final int MAX_MTU = 10000;
    private static final int MIN_MTU = 68;
    private static final int MIN_MTU_V6 = 1280;
    private ArrayList<InetAddress> mDnses = new ArrayList<>();
    private String mDomains;
    private ProxyInfo mHttpProxy;
    private String mIfaceName;
    private ArrayList<LinkAddress> mLinkAddresses = new ArrayList<>();
    private int mMtu;
    private ArrayList<RouteInfo> mRoutes = new ArrayList<>();
    private Hashtable<String, LinkProperties> mStackedLinks = new Hashtable<>();
    private String mTcpBufferSizes;

    public static class CompareResult<T> {
        public List<T> added = new ArrayList();
        public List<T> removed = new ArrayList();

        public String toString() {
            String retVal = "removed=[";
            Iterator i$ = this.removed.iterator();
            while (i$.hasNext()) {
                retVal = retVal + i$.next().toString() + ",";
            }
            String retVal2 = retVal + "] added=[";
            Iterator i$2 = this.added.iterator();
            while (i$2.hasNext()) {
                retVal2 = retVal2 + i$2.next().toString() + ",";
            }
            return retVal2 + "]";
        }
    }

    public LinkProperties() {
    }

    public LinkProperties(LinkProperties source) {
        if (source != null) {
            this.mIfaceName = source.getInterfaceName();
            for (LinkAddress l : source.getLinkAddresses()) {
                this.mLinkAddresses.add(l);
            }
            for (InetAddress i : source.getDnsServers()) {
                this.mDnses.add(i);
            }
            this.mDomains = source.getDomains();
            for (RouteInfo r : source.getRoutes()) {
                this.mRoutes.add(r);
            }
            this.mHttpProxy = source.getHttpProxy() == null ? null : new ProxyInfo(source.getHttpProxy());
            for (LinkProperties l2 : source.mStackedLinks.values()) {
                addStackedLink(l2);
            }
            setMtu(source.getMtu());
            this.mTcpBufferSizes = source.mTcpBufferSizes;
        }
    }

    public void setInterfaceName(String iface) {
        this.mIfaceName = iface;
        ArrayList<RouteInfo> newRoutes = new ArrayList<>(this.mRoutes.size());
        Iterator i$ = this.mRoutes.iterator();
        while (i$.hasNext()) {
            newRoutes.add(routeWithInterface(i$.next()));
        }
        this.mRoutes = newRoutes;
    }

    public String getInterfaceName() {
        return this.mIfaceName;
    }

    public List<String> getAllInterfaceNames() {
        List<String> interfaceNames = new ArrayList<>(this.mStackedLinks.size() + 1);
        if (this.mIfaceName != null) {
            interfaceNames.add(new String(this.mIfaceName));
        }
        for (LinkProperties stacked : this.mStackedLinks.values()) {
            interfaceNames.addAll(stacked.getAllInterfaceNames());
        }
        return interfaceNames;
    }

    public List<InetAddress> getAddresses() {
        List<InetAddress> addresses = new ArrayList<>();
        Iterator i$ = this.mLinkAddresses.iterator();
        while (i$.hasNext()) {
            addresses.add(i$.next().getAddress());
        }
        return Collections.unmodifiableList(addresses);
    }

    public List<InetAddress> getAllAddresses() {
        List<InetAddress> addresses = new ArrayList<>();
        Iterator i$ = this.mLinkAddresses.iterator();
        while (i$.hasNext()) {
            addresses.add(i$.next().getAddress());
        }
        for (LinkProperties stacked : this.mStackedLinks.values()) {
            addresses.addAll(stacked.getAllAddresses());
        }
        return addresses;
    }

    private int findLinkAddressIndex(LinkAddress address) {
        for (int i = 0; i < this.mLinkAddresses.size(); i++) {
            if (this.mLinkAddresses.get(i).isSameAddressAs(address)) {
                return i;
            }
        }
        return -1;
    }

    public boolean addLinkAddress(LinkAddress address) {
        if (address == null) {
            return false;
        }
        int i = findLinkAddressIndex(address);
        if (i < 0) {
            this.mLinkAddresses.add(address);
            return true;
        } else if (this.mLinkAddresses.get(i).equals(address)) {
            return false;
        } else {
            this.mLinkAddresses.set(i, address);
            return true;
        }
    }

    public boolean removeLinkAddress(LinkAddress toRemove) {
        int i = findLinkAddressIndex(toRemove);
        if (i < 0) {
            return false;
        }
        this.mLinkAddresses.remove(i);
        return true;
    }

    public List<LinkAddress> getLinkAddresses() {
        return Collections.unmodifiableList(this.mLinkAddresses);
    }

    public List<LinkAddress> getAllLinkAddresses() {
        List<LinkAddress> addresses = new ArrayList<>();
        addresses.addAll(this.mLinkAddresses);
        for (LinkProperties stacked : this.mStackedLinks.values()) {
            addresses.addAll(stacked.getAllLinkAddresses());
        }
        return addresses;
    }

    public void setLinkAddresses(Collection<LinkAddress> addresses) {
        this.mLinkAddresses.clear();
        for (LinkAddress address : addresses) {
            addLinkAddress(address);
        }
    }

    public boolean addDnsServer(InetAddress dnsServer) {
        if (dnsServer == null || this.mDnses.contains(dnsServer)) {
            return false;
        }
        this.mDnses.add(dnsServer);
        return true;
    }

    public void setDnsServers(Collection<InetAddress> dnsServers) {
        this.mDnses.clear();
        for (InetAddress dnsServer : dnsServers) {
            addDnsServer(dnsServer);
        }
    }

    public List<InetAddress> getDnsServers() {
        return Collections.unmodifiableList(this.mDnses);
    }

    public void setDomains(String domains) {
        this.mDomains = domains;
    }

    public String getDomains() {
        return this.mDomains;
    }

    public void setMtu(int mtu) {
        this.mMtu = mtu;
    }

    public int getMtu() {
        return this.mMtu;
    }

    public void setTcpBufferSizes(String tcpBufferSizes) {
        this.mTcpBufferSizes = tcpBufferSizes;
    }

    public String getTcpBufferSizes() {
        return this.mTcpBufferSizes;
    }

    private RouteInfo routeWithInterface(RouteInfo route) {
        return new RouteInfo(route.getDestination(), route.getGateway(), this.mIfaceName, route.getType());
    }

    public boolean addRoute(RouteInfo route) {
        if (route != null) {
            String routeIface = route.getInterface();
            if (routeIface == null || routeIface.equals(this.mIfaceName)) {
                RouteInfo route2 = routeWithInterface(route);
                if (!this.mRoutes.contains(route2)) {
                    this.mRoutes.add(route2);
                    return true;
                }
            } else {
                throw new IllegalArgumentException("Route added with non-matching interface: " + routeIface + " vs. " + this.mIfaceName);
            }
        }
        return false;
    }

    public boolean removeRoute(RouteInfo route) {
        return route != null && Objects.equals(this.mIfaceName, route.getInterface()) && this.mRoutes.remove(route);
    }

    public List<RouteInfo> getRoutes() {
        return Collections.unmodifiableList(this.mRoutes);
    }

    public List<RouteInfo> getAllRoutes() {
        List<RouteInfo> routes = new ArrayList<>();
        routes.addAll(this.mRoutes);
        for (LinkProperties stacked : this.mStackedLinks.values()) {
            routes.addAll(stacked.getAllRoutes());
        }
        return routes;
    }

    public void setHttpProxy(ProxyInfo proxy) {
        this.mHttpProxy = proxy;
    }

    public ProxyInfo getHttpProxy() {
        return this.mHttpProxy;
    }

    public boolean addStackedLink(LinkProperties link) {
        if (link == null || link.getInterfaceName() == null) {
            return false;
        }
        this.mStackedLinks.put(link.getInterfaceName(), link);
        return true;
    }

    public boolean removeStackedLink(LinkProperties link) {
        if (link == null || link.getInterfaceName() == null || this.mStackedLinks.remove(link.getInterfaceName()) == null) {
            return false;
        }
        return true;
    }

    public List<LinkProperties> getStackedLinks() {
        if (this.mStackedLinks.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        List<LinkProperties> stacked = new ArrayList<>();
        for (LinkProperties link : this.mStackedLinks.values()) {
            stacked.add(new LinkProperties(link));
        }
        return Collections.unmodifiableList(stacked);
    }

    public void clear() {
        this.mIfaceName = null;
        this.mLinkAddresses.clear();
        this.mDnses.clear();
        this.mDomains = null;
        this.mRoutes.clear();
        this.mHttpProxy = null;
        this.mStackedLinks.clear();
        this.mMtu = 0;
        this.mTcpBufferSizes = null;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public String toString() {
        String ifaceName = this.mIfaceName == null ? ProxyInfo.LOCAL_EXCL_LIST : "InterfaceName: " + this.mIfaceName + " ";
        String linkAddresses = "LinkAddresses: [";
        Iterator i$ = this.mLinkAddresses.iterator();
        while (i$.hasNext()) {
            linkAddresses = linkAddresses + i$.next().toString() + ",";
        }
        String linkAddresses2 = linkAddresses + "] ";
        String dns = "DnsAddresses: [";
        Iterator i$2 = this.mDnses.iterator();
        while (i$2.hasNext()) {
            dns = dns + i$2.next().getHostAddress() + ",";
        }
        String dns2 = dns + "] ";
        String domainName = "Domains: " + this.mDomains;
        String mtu = " MTU: " + this.mMtu;
        String tcpBuffSizes = ProxyInfo.LOCAL_EXCL_LIST;
        if (this.mTcpBufferSizes != null) {
            tcpBuffSizes = " TcpBufferSizes: " + this.mTcpBufferSizes;
        }
        String routes = " Routes: [";
        Iterator i$3 = this.mRoutes.iterator();
        while (i$3.hasNext()) {
            routes = routes + i$3.next().toString() + ",";
        }
        String routes2 = routes + "] ";
        String proxy = this.mHttpProxy == null ? ProxyInfo.LOCAL_EXCL_LIST : " HttpProxy: " + this.mHttpProxy.toString() + " ";
        String stacked = ProxyInfo.LOCAL_EXCL_LIST;
        if (this.mStackedLinks.values().size() > 0) {
            String stacked2 = stacked + " Stacked: [";
            Iterator i$4 = this.mStackedLinks.values().iterator();
            while (i$4.hasNext()) {
                stacked2 = stacked2 + " [" + i$4.next().toString() + " ],";
            }
            stacked = stacked2 + "] ";
        }
        return "{" + ifaceName + linkAddresses2 + routes2 + dns2 + domainName + mtu + tcpBuffSizes + proxy + stacked + "}";
    }

    public boolean hasIPv4Address() {
        Iterator i$ = this.mLinkAddresses.iterator();
        while (i$.hasNext()) {
            if (i$.next().getAddress() instanceof Inet4Address) {
                return true;
            }
        }
        return false;
    }

    public boolean hasGlobalIPv6Address() {
        Iterator i$ = this.mLinkAddresses.iterator();
        while (i$.hasNext()) {
            LinkAddress address = i$.next();
            if ((address.getAddress() instanceof Inet6Address) && address.isGlobalPreferred()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasIPv4DefaultRoute() {
        Iterator i$ = this.mRoutes.iterator();
        while (i$.hasNext()) {
            if (i$.next().isIPv4Default()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasIPv6DefaultRoute() {
        Iterator i$ = this.mRoutes.iterator();
        while (i$.hasNext()) {
            if (i$.next().isIPv6Default()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasIPv4DnsServer() {
        Iterator i$ = this.mDnses.iterator();
        while (i$.hasNext()) {
            if (i$.next() instanceof Inet4Address) {
                return true;
            }
        }
        return false;
    }

    public boolean hasIPv6DnsServer() {
        Iterator i$ = this.mDnses.iterator();
        while (i$.hasNext()) {
            if (i$.next() instanceof Inet6Address) {
                return true;
            }
        }
        return false;
    }

    public boolean isProvisioned() {
        return hasIPv4Address() || (hasGlobalIPv6Address() && hasIPv6DefaultRoute() && hasIPv6DnsServer());
    }

    public boolean isIdenticalInterfaceName(LinkProperties target) {
        return TextUtils.equals(getInterfaceName(), target.getInterfaceName());
    }

    public boolean isIdenticalAddresses(LinkProperties target) {
        Collection<?> targetAddresses = target.getAddresses();
        Collection<InetAddress> sourceAddresses = getAddresses();
        if (sourceAddresses.size() == targetAddresses.size()) {
            return sourceAddresses.containsAll(targetAddresses);
        }
        return false;
    }

    public boolean isIdenticalDnses(LinkProperties target) {
        Collection<InetAddress> targetDnses = target.getDnsServers();
        String targetDomains = target.getDomains();
        if (this.mDomains == null) {
            if (targetDomains != null) {
                return false;
            }
        } else if (!this.mDomains.equals(targetDomains)) {
            return false;
        }
        if (this.mDnses.size() == targetDnses.size()) {
            return this.mDnses.containsAll(targetDnses);
        }
        return false;
    }

    public boolean isIdenticalRoutes(LinkProperties target) {
        Collection<RouteInfo> targetRoutes = target.getRoutes();
        if (this.mRoutes.size() == targetRoutes.size()) {
            return this.mRoutes.containsAll(targetRoutes);
        }
        return false;
    }

    public boolean isIdenticalHttpProxy(LinkProperties target) {
        if (getHttpProxy() == null) {
            return target.getHttpProxy() == null;
        }
        return getHttpProxy().equals(target.getHttpProxy());
    }

    public boolean isIdenticalStackedLinks(LinkProperties target) {
        if (!this.mStackedLinks.keySet().equals(target.mStackedLinks.keySet())) {
            return false;
        }
        for (LinkProperties stacked : this.mStackedLinks.values()) {
            if (!stacked.equals(target.mStackedLinks.get(stacked.getInterfaceName()))) {
                return false;
            }
        }
        return true;
    }

    public boolean isIdenticalMtu(LinkProperties target) {
        return getMtu() == target.getMtu();
    }

    public boolean isIdenticalTcpBufferSizes(LinkProperties target) {
        return Objects.equals(this.mTcpBufferSizes, target.mTcpBufferSizes);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof LinkProperties)) {
            return false;
        }
        LinkProperties target = (LinkProperties) obj;
        return isIdenticalInterfaceName(target) && isIdenticalAddresses(target) && isIdenticalDnses(target) && isIdenticalRoutes(target) && isIdenticalHttpProxy(target) && isIdenticalStackedLinks(target) && isIdenticalMtu(target) && isIdenticalTcpBufferSizes(target);
    }

    public CompareResult<LinkAddress> compareAddresses(LinkProperties target) {
        CompareResult<LinkAddress> result = new CompareResult<>();
        result.removed = new ArrayList(this.mLinkAddresses);
        result.added.clear();
        if (target != null) {
            for (LinkAddress newAddress : target.getLinkAddresses()) {
                if (!result.removed.remove(newAddress)) {
                    result.added.add(newAddress);
                }
            }
        }
        return result;
    }

    public CompareResult<InetAddress> compareDnses(LinkProperties target) {
        CompareResult<InetAddress> result = new CompareResult<>();
        result.removed = new ArrayList(this.mDnses);
        result.added.clear();
        if (target != null) {
            for (InetAddress newAddress : target.getDnsServers()) {
                if (!result.removed.remove(newAddress)) {
                    result.added.add(newAddress);
                }
            }
        }
        return result;
    }

    public CompareResult<RouteInfo> compareAllRoutes(LinkProperties target) {
        CompareResult<RouteInfo> result = new CompareResult<>();
        result.removed = (List<T>) getAllRoutes();
        result.added.clear();
        if (target != null) {
            for (RouteInfo r : target.getAllRoutes()) {
                if (!result.removed.remove(r)) {
                    result.added.add(r);
                }
            }
        }
        return result;
    }

    public CompareResult<String> compareAllInterfaceNames(LinkProperties target) {
        CompareResult<String> result = new CompareResult<>();
        result.removed = (List<T>) getAllInterfaceNames();
        result.added.clear();
        if (target != null) {
            for (String r : target.getAllInterfaceNames()) {
                if (!result.removed.remove(r)) {
                    result.added.add(r);
                }
            }
        }
        return result;
    }

    public int hashCode() {
        int hashCode;
        int i = 0;
        if (this.mIfaceName == null) {
            hashCode = 0;
        } else {
            hashCode = (this.mHttpProxy == null ? 0 : this.mHttpProxy.hashCode()) + (this.mRoutes.size() * 41) + (this.mDomains == null ? 0 : this.mDomains.hashCode()) + (this.mDnses.size() * 37) + this.mIfaceName.hashCode() + (this.mLinkAddresses.size() * 31) + (this.mStackedLinks.hashCode() * 47);
        }
        int i2 = hashCode + (this.mMtu * 51);
        if (this.mTcpBufferSizes != null) {
            i = this.mTcpBufferSizes.hashCode();
        }
        return i2 + i;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getInterfaceName());
        dest.writeInt(this.mLinkAddresses.size());
        Iterator i$ = this.mLinkAddresses.iterator();
        while (i$.hasNext()) {
            dest.writeParcelable(i$.next(), flags);
        }
        dest.writeInt(this.mDnses.size());
        Iterator i$2 = this.mDnses.iterator();
        while (i$2.hasNext()) {
            dest.writeByteArray(i$2.next().getAddress());
        }
        dest.writeString(this.mDomains);
        dest.writeInt(this.mMtu);
        dest.writeString(this.mTcpBufferSizes);
        dest.writeInt(this.mRoutes.size());
        Iterator i$3 = this.mRoutes.iterator();
        while (i$3.hasNext()) {
            dest.writeParcelable(i$3.next(), flags);
        }
        if (this.mHttpProxy != null) {
            dest.writeByte((byte) 1);
            dest.writeParcelable(this.mHttpProxy, flags);
        } else {
            dest.writeByte((byte) 0);
        }
        dest.writeList(new ArrayList<>(this.mStackedLinks.values()));
    }

    /* JADX WARNING: Removed duplicated region for block: B:8:0x0012 A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean isValidMtu(int r3, boolean r4) {
        /*
            r2 = 10000(0x2710, float:1.4013E-41)
            r0 = 1
            if (r4 == 0) goto L_0x000c
            r1 = 1280(0x500, float:1.794E-42)
            if (r3 < r1) goto L_0x0012
            if (r3 > r2) goto L_0x0012
        L_0x000b:
            return r0
        L_0x000c:
            r1 = 68
            if (r3 < r1) goto L_0x0012
            if (r3 <= r2) goto L_0x000b
        L_0x0012:
            r0 = 0
            goto L_0x000b
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.LinkProperties.isValidMtu(int, boolean):boolean");
    }
}

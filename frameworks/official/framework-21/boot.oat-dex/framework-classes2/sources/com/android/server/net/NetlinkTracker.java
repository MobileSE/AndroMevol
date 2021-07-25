package com.android.server.net;

import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.RouteInfo;
import java.util.Arrays;

public class NetlinkTracker extends BaseNetworkObserver {
    private static final boolean DBG = false;
    private final String TAG;
    private final Callback mCallback;
    private DnsServerRepository mDnsServerRepository;
    private final String mInterfaceName;
    private final LinkProperties mLinkProperties = new LinkProperties();

    public interface Callback {
        void update();
    }

    public NetlinkTracker(String iface, Callback callback) {
        this.TAG = "NetlinkTracker/" + iface;
        this.mInterfaceName = iface;
        this.mCallback = callback;
        this.mLinkProperties.setInterfaceName(this.mInterfaceName);
        this.mDnsServerRepository = new DnsServerRepository();
    }

    private void maybeLog(String operation, String iface, LinkAddress address) {
    }

    private void maybeLog(String operation, Object o) {
    }

    @Override // com.android.server.net.BaseNetworkObserver
    public void addressUpdated(String iface, LinkAddress address) {
        boolean changed;
        if (this.mInterfaceName.equals(iface)) {
            maybeLog("addressUpdated", iface, address);
            synchronized (this) {
                changed = this.mLinkProperties.addLinkAddress(address);
            }
            if (changed) {
                this.mCallback.update();
            }
        }
    }

    @Override // com.android.server.net.BaseNetworkObserver
    public void addressRemoved(String iface, LinkAddress address) {
        boolean changed;
        if (this.mInterfaceName.equals(iface)) {
            maybeLog("addressRemoved", iface, address);
            synchronized (this) {
                changed = this.mLinkProperties.removeLinkAddress(address);
            }
            if (changed) {
                this.mCallback.update();
            }
        }
    }

    @Override // com.android.server.net.BaseNetworkObserver
    public void routeUpdated(RouteInfo route) {
        boolean changed;
        if (this.mInterfaceName.equals(route.getInterface())) {
            maybeLog("routeUpdated", route);
            synchronized (this) {
                changed = this.mLinkProperties.addRoute(route);
            }
            if (changed) {
                this.mCallback.update();
            }
        }
    }

    @Override // com.android.server.net.BaseNetworkObserver
    public void routeRemoved(RouteInfo route) {
        boolean changed;
        if (this.mInterfaceName.equals(route.getInterface())) {
            maybeLog("routeRemoved", route);
            synchronized (this) {
                changed = this.mLinkProperties.removeRoute(route);
            }
            if (changed) {
                this.mCallback.update();
            }
        }
    }

    @Override // com.android.server.net.BaseNetworkObserver
    public void interfaceDnsServerInfo(String iface, long lifetime, String[] addresses) {
        if (this.mInterfaceName.equals(iface)) {
            maybeLog("interfaceDnsServerInfo", Arrays.toString(addresses));
            if (this.mDnsServerRepository.addServers(lifetime, addresses)) {
                synchronized (this) {
                    this.mDnsServerRepository.setDnsServersOn(this.mLinkProperties);
                }
                this.mCallback.update();
            }
        }
    }

    public synchronized LinkProperties getLinkProperties() {
        return new LinkProperties(this.mLinkProperties);
    }

    public synchronized void clearLinkProperties() {
        this.mDnsServerRepository = new DnsServerRepository();
        this.mLinkProperties.clear();
        this.mLinkProperties.setInterfaceName(this.mInterfaceName);
    }
}

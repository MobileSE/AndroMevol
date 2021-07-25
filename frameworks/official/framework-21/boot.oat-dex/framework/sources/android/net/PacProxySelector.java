package android.net;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.android.net.IProxyService;
import com.google.android.collect.Lists;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.List;

public class PacProxySelector extends ProxySelector {
    private static final String PROXY = "PROXY ";
    public static final String PROXY_SERVICE = "com.android.net.IProxyService";
    private static final String SOCKS = "SOCKS ";
    private static final String TAG = "PacProxySelector";
    private final List<Proxy> mDefaultList;
    private IProxyService mProxyService = IProxyService.Stub.asInterface(ServiceManager.getService(PROXY_SERVICE));

    public PacProxySelector() {
        if (this.mProxyService == null) {
            Log.e(TAG, "PacManager: no proxy service");
        }
        this.mDefaultList = Lists.newArrayList(new Proxy[]{Proxy.NO_PROXY});
    }

    @Override // java.net.ProxySelector
    public List<Proxy> select(URI uri) {
        String urlString;
        if (this.mProxyService == null) {
            this.mProxyService = IProxyService.Stub.asInterface(ServiceManager.getService(PROXY_SERVICE));
        }
        if (this.mProxyService == null) {
            Log.e(TAG, "select: no proxy service return NO_PROXY");
            return Lists.newArrayList(new Proxy[]{Proxy.NO_PROXY});
        }
        String response = null;
        try {
            urlString = uri.toURL().toString();
        } catch (MalformedURLException e) {
            urlString = uri.getHost();
        }
        try {
            response = this.mProxyService.resolvePacFile(uri.getHost(), urlString);
        } catch (RemoteException e2) {
            e2.printStackTrace();
        }
        if (response == null) {
            return this.mDefaultList;
        }
        return parseResponse(response);
    }

    private static List<Proxy> parseResponse(String response) {
        Proxy proxy;
        String[] split = response.split(";");
        List<Proxy> ret = Lists.newArrayList();
        for (String s : split) {
            String trimmed = s.trim();
            if (trimmed.equals("DIRECT")) {
                ret.add(Proxy.NO_PROXY);
            } else if (trimmed.startsWith(PROXY)) {
                Proxy proxy2 = proxyFromHostPort(Proxy.Type.HTTP, trimmed.substring(PROXY.length()));
                if (proxy2 != null) {
                    ret.add(proxy2);
                }
            } else if (trimmed.startsWith(SOCKS) && (proxy = proxyFromHostPort(Proxy.Type.SOCKS, trimmed.substring(SOCKS.length()))) != null) {
                ret.add(proxy);
            }
        }
        if (ret.size() == 0) {
            ret.add(Proxy.NO_PROXY);
        }
        return ret;
    }

    private static Proxy proxyFromHostPort(Proxy.Type type, String hostPortString) {
        try {
            String[] hostPort = hostPortString.split(":");
            return new Proxy(type, InetSocketAddress.createUnresolved(hostPort[0], Integer.parseInt(hostPort[1])));
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            Log.d(TAG, "Unable to parse proxy " + hostPortString + " " + e);
            return null;
        }
    }

    public void connectFailed(URI uri, SocketAddress address, IOException failure) {
    }
}

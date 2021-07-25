package android.net;

import android.os.Parcel;
import android.os.Parcelable;
import android.system.ErrnoException;
import com.android.okhttp.ConnectionPool;
import com.android.okhttp.HostResolver;
import com.android.okhttp.HttpHandler;
import com.android.okhttp.HttpsHandler;
import com.android.okhttp.OkHttpClient;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import javax.net.SocketFactory;

public class Network implements Parcelable {
    public static final Parcelable.Creator<Network> CREATOR = new Parcelable.Creator<Network>() {
        /* class android.net.Network.AnonymousClass2 */

        @Override // android.os.Parcelable.Creator
        public Network createFromParcel(Parcel in) {
            return new Network(in.readInt());
        }

        @Override // android.os.Parcelable.Creator
        public Network[] newArray(int size) {
            return new Network[size];
        }
    };
    private static final boolean httpKeepAlive = Boolean.parseBoolean(System.getProperty("http.keepAlive", "true"));
    private static final long httpKeepAliveDurationMs = Long.parseLong(System.getProperty("http.keepAliveDuration", "300000"));
    private static final int httpMaxConnections = (httpKeepAlive ? Integer.parseInt(System.getProperty("http.maxConnections", "5")) : 0);
    private volatile ConnectionPool mConnectionPool = null;
    private volatile HostResolver mHostResolver = null;
    private Object mLock = new Object();
    private volatile NetworkBoundSocketFactory mNetworkBoundSocketFactory = null;
    public final int netId;

    public Network(int netId2) {
        this.netId = netId2;
    }

    public Network(Network that) {
        this.netId = that.netId;
    }

    public InetAddress[] getAllByName(String host) throws UnknownHostException {
        return InetAddress.getAllByNameOnNet(host, this.netId);
    }

    public InetAddress getByName(String host) throws UnknownHostException {
        return InetAddress.getByNameOnNet(host, this.netId);
    }

    /* access modifiers changed from: private */
    public class NetworkBoundSocketFactory extends SocketFactory {
        private final int mNetId;

        public NetworkBoundSocketFactory(int netId) {
            this.mNetId = netId;
        }

        private Socket connectToHost(String host, int port, SocketAddress localAddress) throws IOException {
            InetAddress[] hostAddresses = Network.this.getAllByName(host);
            for (int i = 0; i < hostAddresses.length; i++) {
                try {
                    Socket socket = createSocket();
                    if (localAddress != null) {
                        socket.bind(localAddress);
                    }
                    socket.connect(new InetSocketAddress(hostAddresses[i], port));
                    return socket;
                } catch (IOException e) {
                    if (i == hostAddresses.length - 1) {
                        throw e;
                    }
                }
            }
            throw new UnknownHostException(host);
        }

        @Override // javax.net.SocketFactory
        public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
            return connectToHost(host, port, new InetSocketAddress(localHost, localPort));
        }

        @Override // javax.net.SocketFactory
        public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
            Socket socket = createSocket();
            socket.bind(new InetSocketAddress(localAddress, localPort));
            socket.connect(new InetSocketAddress(address, port));
            return socket;
        }

        @Override // javax.net.SocketFactory
        public Socket createSocket(InetAddress host, int port) throws IOException {
            Socket socket = createSocket();
            socket.connect(new InetSocketAddress(host, port));
            return socket;
        }

        @Override // javax.net.SocketFactory
        public Socket createSocket(String host, int port) throws IOException {
            return connectToHost(host, port, null);
        }

        @Override // javax.net.SocketFactory
        public Socket createSocket() throws IOException {
            Socket socket = new Socket();
            Network.this.bindSocket(socket);
            return socket;
        }
    }

    public SocketFactory getSocketFactory() {
        if (this.mNetworkBoundSocketFactory == null) {
            synchronized (this.mLock) {
                if (this.mNetworkBoundSocketFactory == null) {
                    this.mNetworkBoundSocketFactory = new NetworkBoundSocketFactory(this.netId);
                }
            }
        }
        return this.mNetworkBoundSocketFactory;
    }

    private void maybeInitHttpClient() {
        synchronized (this.mLock) {
            if (this.mHostResolver == null) {
                this.mHostResolver = new HostResolver() {
                    /* class android.net.Network.AnonymousClass1 */

                    public InetAddress[] getAllByName(String host) throws UnknownHostException {
                        return Network.this.getAllByName(host);
                    }
                };
            }
            if (this.mConnectionPool == null) {
                this.mConnectionPool = new ConnectionPool(httpMaxConnections, httpKeepAliveDurationMs);
            }
        }
    }

    public URLConnection openConnection(URL url) throws IOException {
        OkHttpClient client;
        maybeInitHttpClient();
        String protocol = url.getProtocol();
        if (protocol.equals("http")) {
            client = HttpHandler.createHttpOkHttpClient((Proxy) null);
        } else if (protocol.equals("https")) {
            client = HttpsHandler.createHttpsOkHttpClient((Proxy) null);
        } else {
            throw new MalformedURLException("Invalid URL or unrecognized protocol " + protocol);
        }
        return client.setSocketFactory(getSocketFactory()).setHostResolver(this.mHostResolver).setConnectionPool(this.mConnectionPool).open(url);
    }

    public void bindSocket(Socket socket) throws IOException {
        if (socket.isConnected()) {
            throw new SocketException("Socket is connected");
        }
        socket.getReuseAddress();
        int err = NetworkUtils.bindSocketToNetwork(socket.getFileDescriptor$().getInt$(), this.netId);
        if (err != 0) {
            throw new ErrnoException("Binding socket to network " + this.netId, -err).rethrowAsSocketException();
        }
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.netId);
    }

    public boolean equals(Object obj) {
        if ((obj instanceof Network) && this.netId == ((Network) obj).netId) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return this.netId * 11;
    }

    public String toString() {
        return Integer.toString(this.netId);
    }
}

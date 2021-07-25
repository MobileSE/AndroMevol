package android.net.http;

import android.content.Context;
import android.util.Log;
import com.android.org.conscrypt.FileClientSessionCache;
import com.android.org.conscrypt.OpenSSLContextImpl;
import com.android.org.conscrypt.SSLClientSessionCache;
import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.HttpHost;

public class HttpsConnection extends Connection {
    private static SSLSocketFactory mSslSocketFactory = null;
    private boolean mAborted = false;
    private HttpHost mProxyHost;
    private Object mSuspendLock = new Object();
    private boolean mSuspended = false;

    @Override // android.net.http.Connection
    public /* bridge */ /* synthetic */ String toString() {
        return super.toString();
    }

    static {
        initializeEngine(null);
    }

    public static void initializeEngine(File sessionDir) {
        SSLClientSessionCache cache = null;
        if (sessionDir != null) {
            try {
                Log.d("HttpsConnection", "Caching SSL sessions in " + sessionDir + ".");
                cache = FileClientSessionCache.usingDirectory(sessionDir);
            } catch (KeyManagementException e) {
                throw new RuntimeException(e);
            } catch (IOException e2) {
                throw new RuntimeException(e2);
            }
        }
        OpenSSLContextImpl sslContext = new OpenSSLContextImpl();
        sslContext.engineInit((KeyManager[]) null, new TrustManager[]{new X509TrustManager() {
            /* class android.net.http.HttpsConnection.AnonymousClass1 */

            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override // javax.net.ssl.X509TrustManager
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            @Override // javax.net.ssl.X509TrustManager
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }}, (SecureRandom) null);
        sslContext.engineGetClientSessionContext().setPersistentCache(cache);
        synchronized (HttpsConnection.class) {
            mSslSocketFactory = sslContext.engineGetSocketFactory();
        }
    }

    private static synchronized SSLSocketFactory getSocketFactory() {
        SSLSocketFactory sSLSocketFactory;
        synchronized (HttpsConnection.class) {
            sSLSocketFactory = mSslSocketFactory;
        }
        return sSLSocketFactory;
    }

    HttpsConnection(Context context, HttpHost host, HttpHost proxy, RequestFeeder requestFeeder) {
        super(context, host, requestFeeder);
        this.mProxyHost = proxy;
    }

    /* access modifiers changed from: package-private */
    public void setCertificate(SslCertificate certificate) {
        this.mCertificate = certificate;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x00bb  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x00c4  */
    @Override // android.net.http.Connection
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public android.net.http.AndroidHttpClientConnection openConnection(android.net.http.Request r33) throws java.io.IOException {
        /*
        // Method dump skipped, instructions count: 699
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.http.HttpsConnection.openConnection(android.net.http.Request):android.net.http.AndroidHttpClientConnection");
    }

    /* access modifiers changed from: package-private */
    @Override // android.net.http.Connection
    public void closeConnection() {
        if (this.mSuspended) {
            restartConnection(false);
        }
        try {
            if (this.mHttpClientConnection != null && this.mHttpClientConnection.isOpen()) {
                this.mHttpClientConnection.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: package-private */
    public void restartConnection(boolean proceed) {
        boolean z = false;
        synchronized (this.mSuspendLock) {
            if (this.mSuspended) {
                this.mSuspended = false;
                if (!proceed) {
                    z = true;
                }
                this.mAborted = z;
                this.mSuspendLock.notify();
            }
        }
    }

    /* access modifiers changed from: package-private */
    @Override // android.net.http.Connection
    public String getScheme() {
        return "https";
    }
}

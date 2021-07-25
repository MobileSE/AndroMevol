package android.media;

import android.media.IMediaHTTPConnection;
import android.net.NetworkUtils;
import android.net.ProxyInfo;
import android.os.IBinder;
import android.os.StrictMode;
import android.util.Log;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class MediaHTTPConnection extends IMediaHTTPConnection.Stub {
    private static final int HTTP_TEMP_REDIRECT = 307;
    private static final int MAX_REDIRECTS = 20;
    private static final String TAG = "MediaHTTPConnection";
    private static final boolean VERBOSE = false;
    private boolean mAllowCrossDomainRedirect = true;
    private boolean mAllowCrossProtocolRedirect = true;
    private HttpURLConnection mConnection = null;
    private long mCurrentOffset = -1;
    private Map<String, String> mHeaders = null;
    private InputStream mInputStream = null;
    private long mNativeContext;
    private long mTotalSize = -1;
    private URL mURL = null;

    private final native void native_finalize();

    private final native IBinder native_getIMemory();

    private static final native void native_init();

    private final native int native_readAt(long j, int i);

    private final native void native_setup();

    public MediaHTTPConnection() {
        if (CookieHandler.getDefault() == null) {
            CookieHandler.setDefault(new CookieManager());
        }
        native_setup();
    }

    @Override // android.media.IMediaHTTPConnection
    public IBinder connect(String uri, String headers) {
        try {
            disconnect();
            this.mAllowCrossDomainRedirect = true;
            this.mURL = new URL(uri);
            this.mHeaders = convertHeaderStringToMap(headers);
            return native_getIMemory();
        } catch (MalformedURLException e) {
            return null;
        }
    }

    private boolean parseBoolean(String val) {
        boolean z = false;
        try {
            return Long.parseLong(val) != 0;
        } catch (NumberFormatException e) {
            if ("true".equalsIgnoreCase(val) || "yes".equalsIgnoreCase(val)) {
                z = true;
            }
            return z;
        }
    }

    private boolean filterOutInternalHeaders(String key, String val) {
        if (!"android-allow-cross-domain-redirect".equalsIgnoreCase(key)) {
            return false;
        }
        this.mAllowCrossDomainRedirect = parseBoolean(val);
        this.mAllowCrossProtocolRedirect = this.mAllowCrossDomainRedirect;
        return true;
    }

    private Map<String, String> convertHeaderStringToMap(String headers) {
        HashMap<String, String> map = new HashMap<>();
        String[] pairs = headers.split("\r\n");
        for (String pair : pairs) {
            int colonPos = pair.indexOf(":");
            if (colonPos >= 0) {
                String key = pair.substring(0, colonPos);
                String val = pair.substring(colonPos + 1);
                if (!filterOutInternalHeaders(key, val)) {
                    map.put(key, val);
                }
            }
        }
        return map;
    }

    @Override // android.media.IMediaHTTPConnection
    public void disconnect() {
        teardownConnection();
        this.mHeaders = null;
        this.mURL = null;
    }

    private void teardownConnection() {
        if (this.mConnection != null) {
            this.mInputStream = null;
            this.mConnection.disconnect();
            this.mConnection = null;
            this.mCurrentOffset = -1;
        }
    }

    private static final boolean isLocalHost(URL url) {
        String host;
        if (url == null || (host = url.getHost()) == null) {
            return false;
        }
        try {
            if (host.equalsIgnoreCase(ProxyInfo.LOCAL_HOST)) {
                return true;
            }
            return NetworkUtils.numericToInetAddress(host).isLoopbackAddress();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private void seekTo(long offset) throws IOException {
        int lastSlashPos;
        teardownConnection();
        int redirectCount = 0;
        try {
            URL url = this.mURL;
            boolean noProxy = isLocalHost(url);
            while (true) {
                if (noProxy) {
                    this.mConnection = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
                } else {
                    this.mConnection = (HttpURLConnection) url.openConnection();
                }
                this.mConnection.setInstanceFollowRedirects(this.mAllowCrossDomainRedirect);
                if (this.mHeaders != null) {
                    for (Map.Entry<String, String> entry : this.mHeaders.entrySet()) {
                        this.mConnection.setRequestProperty(entry.getKey(), entry.getValue());
                    }
                }
                if (offset > 0) {
                    this.mConnection.setRequestProperty("Range", "bytes=" + offset + "-");
                }
                int response = this.mConnection.getResponseCode();
                if (response == 300 || response == 301 || response == 302 || response == 303 || response == HTTP_TEMP_REDIRECT) {
                    redirectCount++;
                    if (redirectCount > 20) {
                        throw new NoRouteToHostException("Too many redirects: " + redirectCount);
                    }
                    String method = this.mConnection.getRequestMethod();
                    if (response != HTTP_TEMP_REDIRECT || method.equals("GET") || method.equals("HEAD")) {
                        String location = this.mConnection.getHeaderField("Location");
                        if (location == null) {
                            throw new NoRouteToHostException("Invalid redirect");
                        }
                        url = new URL(this.mURL, location);
                        if (url.getProtocol().equals("https") || url.getProtocol().equals("http")) {
                            boolean sameProtocol = this.mURL.getProtocol().equals(url.getProtocol());
                            if (this.mAllowCrossProtocolRedirect || sameProtocol) {
                                boolean sameHost = this.mURL.getHost().equals(url.getHost());
                                if (!this.mAllowCrossDomainRedirect && !sameHost) {
                                    throw new NoRouteToHostException("Cross-domain redirects are disallowed");
                                } else if (response != HTTP_TEMP_REDIRECT) {
                                    this.mURL = url;
                                }
                            } else {
                                throw new NoRouteToHostException("Cross-protocol redirects are disallowed");
                            }
                        } else {
                            throw new NoRouteToHostException("Unsupported protocol redirect");
                        }
                    } else {
                        throw new NoRouteToHostException("Invalid redirect");
                    }
                } else {
                    if (this.mAllowCrossDomainRedirect) {
                        this.mURL = this.mConnection.getURL();
                    }
                    if (response == 206) {
                        String contentRange = this.mConnection.getHeaderField("Content-Range");
                        this.mTotalSize = -1;
                        if (contentRange != null && (lastSlashPos = contentRange.lastIndexOf(47)) >= 0) {
                            try {
                                this.mTotalSize = Long.parseLong(contentRange.substring(lastSlashPos + 1));
                            } catch (NumberFormatException e) {
                            }
                        }
                    } else if (response != 200) {
                        throw new IOException();
                    } else {
                        this.mTotalSize = (long) this.mConnection.getContentLength();
                    }
                    if (offset <= 0 || response == 206) {
                        this.mInputStream = new BufferedInputStream(this.mConnection.getInputStream());
                        this.mCurrentOffset = offset;
                        return;
                    }
                    throw new IOException();
                }
            }
        } catch (IOException e2) {
            this.mTotalSize = -1;
            this.mInputStream = null;
            this.mConnection = null;
            this.mCurrentOffset = -1;
            throw e2;
        }
    }

    @Override // android.media.IMediaHTTPConnection
    public int readAt(long offset, int size) {
        return native_readAt(offset, size);
    }

    private int readAt(long offset, byte[] data, int size) {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        try {
            if (offset != this.mCurrentOffset) {
                seekTo(offset);
            }
            int n = this.mInputStream.read(data, 0, size);
            if (n == -1) {
                n = 0;
            }
            this.mCurrentOffset += (long) n;
            return n;
        } catch (NoRouteToHostException e) {
            Log.w(TAG, "readAt " + offset + " / " + size + " => " + e);
            return MediaPlayer.MEDIA_ERROR_UNSUPPORTED;
        } catch (IOException e2) {
            return -1;
        } catch (Exception e3) {
            return -1;
        }
    }

    @Override // android.media.IMediaHTTPConnection
    public long getSize() {
        if (this.mConnection == null) {
            try {
                seekTo(0);
            } catch (IOException e) {
                return -1;
            }
        }
        return this.mTotalSize;
    }

    @Override // android.media.IMediaHTTPConnection
    public String getMIMEType() {
        if (this.mConnection == null) {
            try {
                seekTo(0);
            } catch (IOException e) {
                return "application/octet-stream";
            }
        }
        return this.mConnection.getContentType();
    }

    @Override // android.media.IMediaHTTPConnection
    public String getUri() {
        return this.mURL.toString();
    }

    /* access modifiers changed from: protected */
    @Override // android.os.Binder
    public void finalize() {
        native_finalize();
    }

    static {
        System.loadLibrary("media_jni");
        native_init();
    }
}

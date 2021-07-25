package android.net.http;

import android.content.Context;
import android.os.SystemClock;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import javax.net.ssl.SSLHandshakeException;
import org.apache.http.HttpConnection;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpVersion;
import org.apache.http.ParseException;
import org.apache.http.ProtocolVersion;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

/* access modifiers changed from: package-private */
public abstract class Connection {
    private static final int DONE = 3;
    private static final int DRAIN = 2;
    private static final String HTTP_CONNECTION = "http.connection";
    private static final int MAX_PIPE = 3;
    private static final int MIN_PIPE = 2;
    private static final int READ = 1;
    private static final int RETRY_REQUEST_LIMIT = 2;
    private static final int SEND = 0;
    static final int SOCKET_TIMEOUT = 60000;
    private static int STATE_CANCEL_REQUESTED = 1;
    private static int STATE_NORMAL = 0;
    private static final String[] states = {"SEND", "READ", "DRAIN", "DONE"};
    private int mActive = STATE_NORMAL;
    private byte[] mBuf;
    private boolean mCanPersist;
    protected SslCertificate mCertificate = null;
    Context mContext;
    HttpHost mHost;
    protected AndroidHttpClientConnection mHttpClientConnection = null;
    private HttpContext mHttpContext;
    RequestFeeder mRequestFeeder;

    /* access modifiers changed from: package-private */
    public abstract void closeConnection();

    /* access modifiers changed from: package-private */
    public abstract String getScheme();

    /* access modifiers changed from: package-private */
    public abstract AndroidHttpClientConnection openConnection(Request request) throws IOException;

    protected Connection(Context context, HttpHost host, RequestFeeder requestFeeder) {
        this.mContext = context;
        this.mHost = host;
        this.mRequestFeeder = requestFeeder;
        this.mCanPersist = false;
        this.mHttpContext = new BasicHttpContext((HttpContext) null);
    }

    /* access modifiers changed from: package-private */
    public HttpHost getHost() {
        return this.mHost;
    }

    static Connection getConnection(Context context, HttpHost host, HttpHost proxy, RequestFeeder requestFeeder) {
        if (host.getSchemeName().equals("http")) {
            return new HttpConnection(context, host, requestFeeder);
        }
        return new HttpsConnection(context, host, proxy, requestFeeder);
    }

    /* access modifiers changed from: package-private */
    public SslCertificate getCertificate() {
        return this.mCertificate;
    }

    /* access modifiers changed from: package-private */
    public void cancel() {
        this.mActive = STATE_CANCEL_REQUESTED;
        closeConnection();
    }

    /* access modifiers changed from: package-private */
    public void processRequests(Request firstRequest) {
        boolean empty;
        Request req;
        int error = 0;
        Exception exception = null;
        LinkedList<Request> pipe = new LinkedList<>();
        int minPipe = 2;
        int maxPipe = 3;
        int state = 0;
        while (state != 3) {
            if (this.mActive == STATE_CANCEL_REQUESTED) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
                this.mActive = STATE_NORMAL;
            }
            switch (state) {
                case 0:
                    if (pipe.size() != maxPipe) {
                        if (firstRequest == null) {
                            req = this.mRequestFeeder.getRequest(this.mHost);
                        } else {
                            req = firstRequest;
                            firstRequest = null;
                        }
                        if (req != null) {
                            req.setConnection(this);
                            if (!req.mCancelled) {
                                if ((this.mHttpClientConnection != null && this.mHttpClientConnection.isOpen()) || openHttpConnection(req)) {
                                    req.mEventHandler.certificate(this.mCertificate);
                                    try {
                                        req.sendRequest(this.mHttpClientConnection);
                                    } catch (HttpException e2) {
                                        exception = e2;
                                        error = -1;
                                    } catch (IOException e3) {
                                        exception = e3;
                                        error = -7;
                                    } catch (IllegalStateException e4) {
                                        exception = e4;
                                        error = -7;
                                    }
                                    if (exception == null) {
                                        pipe.addLast(req);
                                        if (this.mCanPersist) {
                                            break;
                                        } else {
                                            state = 1;
                                            break;
                                        }
                                    } else {
                                        if (httpFailure(req, error, exception) && !req.mCancelled) {
                                            pipe.addLast(req);
                                        }
                                        exception = null;
                                        if (clearPipe(pipe)) {
                                            state = 3;
                                        } else {
                                            state = 0;
                                        }
                                        maxPipe = 1;
                                        minPipe = 1;
                                        break;
                                    }
                                } else {
                                    state = 3;
                                    break;
                                }
                            } else {
                                req.complete();
                                break;
                            }
                        } else {
                            state = 2;
                            break;
                        }
                    } else {
                        state = 1;
                        break;
                    }
                    break;
                case 1:
                case 2:
                    if (!this.mRequestFeeder.haveRequest(this.mHost)) {
                        empty = true;
                    } else {
                        empty = false;
                    }
                    int pipeSize = pipe.size();
                    if (state == 2 || pipeSize >= minPipe || empty || !this.mCanPersist) {
                        if (pipeSize == 0) {
                            if (!empty) {
                                state = 0;
                                break;
                            } else {
                                state = 3;
                                break;
                            }
                        } else {
                            Request req2 = pipe.removeFirst();
                            try {
                                req2.readResponse(this.mHttpClientConnection);
                            } catch (ParseException e5) {
                                exception = e5;
                                error = -7;
                            } catch (IOException e6) {
                                exception = e6;
                                error = -7;
                            } catch (IllegalStateException e7) {
                                exception = e7;
                                error = -7;
                            }
                            if (exception != null) {
                                if (httpFailure(req2, error, exception) && !req2.mCancelled) {
                                    req2.reset();
                                    pipe.addFirst(req2);
                                }
                                exception = null;
                                this.mCanPersist = false;
                            }
                            if (this.mCanPersist) {
                                break;
                            } else {
                                closeConnection();
                                this.mHttpContext.removeAttribute(HTTP_CONNECTION);
                                clearPipe(pipe);
                                maxPipe = 1;
                                minPipe = 1;
                                state = 0;
                                break;
                            }
                        }
                    } else {
                        state = 0;
                        break;
                    }
            }
        }
    }

    private boolean clearPipe(LinkedList<Request> pipe) {
        boolean empty = true;
        synchronized (this.mRequestFeeder) {
            while (!pipe.isEmpty()) {
                this.mRequestFeeder.requeueRequest(pipe.removeLast());
                empty = false;
            }
            if (empty) {
                empty = !this.mRequestFeeder.haveRequest(this.mHost);
            }
        }
        return empty;
    }

    private boolean openHttpConnection(Request req) {
        boolean z = true;
        SystemClock.uptimeMillis();
        int error = 0;
        Exception exception = null;
        try {
            this.mCertificate = null;
            this.mHttpClientConnection = openConnection(req);
            if (this.mHttpClientConnection != null) {
                this.mHttpClientConnection.setSocketTimeout(SOCKET_TIMEOUT);
                this.mHttpContext.setAttribute(HTTP_CONNECTION, this.mHttpClientConnection);
                if (error == 0) {
                    return true;
                }
                if (req.mFailCount < 2) {
                    this.mRequestFeeder.requeueRequest(req);
                    req.mFailCount++;
                } else {
                    httpFailure(req, error, exception);
                }
                if (error != 0) {
                    z = false;
                }
                return z;
            }
            req.mFailCount = 2;
            return false;
        } catch (UnknownHostException e) {
            error = -2;
            exception = e;
        } catch (IllegalArgumentException e2) {
            error = -6;
            req.mFailCount = 2;
            exception = e2;
        } catch (SSLConnectionClosedByUserException e3) {
            req.mFailCount = 2;
            return false;
        } catch (SSLHandshakeException e4) {
            req.mFailCount = 2;
            error = -11;
            exception = e4;
        } catch (IOException e5) {
            error = -6;
            exception = e5;
        }
    }

    private boolean httpFailure(Request req, int errorId, Exception e) {
        String error;
        boolean ret = true;
        int i = req.mFailCount + 1;
        req.mFailCount = i;
        if (i >= 2) {
            ret = false;
            if (errorId < 0) {
                error = ErrorStrings.getString(errorId, this.mContext);
            } else {
                Throwable cause = e.getCause();
                error = cause != null ? cause.toString() : e.getMessage();
            }
            req.mEventHandler.error(errorId, error);
            req.complete();
        }
        closeConnection();
        this.mHttpContext.removeAttribute(HTTP_CONNECTION);
        return ret;
    }

    /* access modifiers changed from: package-private */
    public HttpContext getHttpContext() {
        return this.mHttpContext;
    }

    private boolean keepAlive(HttpEntity entity, ProtocolVersion ver, int connType, HttpContext context) {
        boolean z = true;
        HttpConnection conn = (HttpConnection) context.getAttribute(HTTP_CONNECTION);
        if (conn != null && !conn.isOpen()) {
            return false;
        }
        if ((entity != null && entity.getContentLength() < 0 && (!entity.isChunked() || ver.lessEquals(HttpVersion.HTTP_1_0))) || connType == 1) {
            return false;
        }
        if (connType == 2) {
            return true;
        }
        if (ver.lessEquals(HttpVersion.HTTP_1_0)) {
            z = false;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public void setCanPersist(HttpEntity entity, ProtocolVersion ver, int connType) {
        this.mCanPersist = keepAlive(entity, ver, connType, this.mHttpContext);
    }

    /* access modifiers changed from: package-private */
    public void setCanPersist(boolean canPersist) {
        this.mCanPersist = canPersist;
    }

    /* access modifiers changed from: package-private */
    public boolean getCanPersist() {
        return this.mCanPersist;
    }

    public synchronized String toString() {
        return this.mHost.toString();
    }

    /* access modifiers changed from: package-private */
    public byte[] getBuf() {
        if (this.mBuf == null) {
            this.mBuf = new byte[8192];
        }
        return this.mBuf;
    }
}

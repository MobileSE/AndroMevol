package android.service.media;

import android.app.Service;
import android.content.Intent;
import android.content.pm.ParceledListSlice;
import android.media.browse.MediaBrowser;
import android.media.session.MediaSession;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.service.media.IMediaBrowserService;
import android.util.ArrayMap;
import android.util.Log;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;

public abstract class MediaBrowserService extends Service {
    private static final boolean DBG = false;
    public static final String SERVICE_INTERFACE = "android.media.browse.MediaBrowserService";
    private static final String TAG = "MediaBrowserService";
    private ServiceBinder mBinder;
    private final ArrayMap<IBinder, ConnectionRecord> mConnections = new ArrayMap<>();
    private final Handler mHandler = new Handler();
    MediaSession.Token mSession;

    public abstract BrowserRoot onGetRoot(String str, int i, Bundle bundle);

    public abstract void onLoadChildren(String str, Result<List<MediaBrowser.MediaItem>> result);

    /* access modifiers changed from: private */
    public class ConnectionRecord {
        IMediaBrowserServiceCallbacks callbacks;
        String pkg;
        BrowserRoot root;
        Bundle rootHints;
        HashSet<String> subscriptions;

        private ConnectionRecord() {
            this.subscriptions = new HashSet<>();
        }
    }

    public class Result<T> {
        private Object mDebug;
        private boolean mDetachCalled;
        private boolean mSendResultCalled;

        Result(Object debug) {
            this.mDebug = debug;
        }

        public void sendResult(T result) {
            if (this.mSendResultCalled) {
                throw new IllegalStateException("sendResult() called twice for: " + this.mDebug);
            }
            this.mSendResultCalled = true;
            onResultSent(result);
        }

        public void detach() {
            if (this.mDetachCalled) {
                throw new IllegalStateException("detach() called when detach() had already been called for: " + this.mDebug);
            } else if (this.mSendResultCalled) {
                throw new IllegalStateException("detach() called when sendResult() had already been called for: " + this.mDebug);
            } else {
                this.mDetachCalled = true;
            }
        }

        /* access modifiers changed from: package-private */
        public boolean isDone() {
            return this.mDetachCalled || this.mSendResultCalled;
        }

        /* access modifiers changed from: package-private */
        public void onResultSent(T t) {
        }
    }

    private class ServiceBinder extends IMediaBrowserService.Stub {
        private ServiceBinder() {
        }

        @Override // android.service.media.IMediaBrowserService
        public void connect(final String pkg, final Bundle rootHints, final IMediaBrowserServiceCallbacks callbacks) {
            final int uid = Binder.getCallingUid();
            if (!MediaBrowserService.this.isValidPackage(pkg, uid)) {
                throw new IllegalArgumentException("Package/uid mismatch: uid=" + uid + " package=" + pkg);
            }
            MediaBrowserService.this.mHandler.post(new Runnable() {
                /* class android.service.media.MediaBrowserService.ServiceBinder.AnonymousClass1 */

                public void run() {
                    IBinder b = callbacks.asBinder();
                    MediaBrowserService.this.mConnections.remove(b);
                    ConnectionRecord connection = new ConnectionRecord();
                    connection.pkg = pkg;
                    connection.rootHints = rootHints;
                    connection.callbacks = callbacks;
                    connection.root = MediaBrowserService.this.onGetRoot(pkg, uid, rootHints);
                    if (connection.root == null) {
                        Log.i(MediaBrowserService.TAG, "No root for client " + pkg + " from service " + getClass().getName());
                        try {
                            callbacks.onConnectFailed();
                        } catch (RemoteException e) {
                            Log.w(MediaBrowserService.TAG, "Calling onConnectFailed() failed. Ignoring. pkg=" + pkg);
                        }
                    } else {
                        try {
                            MediaBrowserService.this.mConnections.put(b, connection);
                            callbacks.onConnect(connection.root.getRootId(), MediaBrowserService.this.mSession, connection.root.getExtras());
                        } catch (RemoteException e2) {
                            Log.w(MediaBrowserService.TAG, "Calling onConnect() failed. Dropping client. pkg=" + pkg);
                            MediaBrowserService.this.mConnections.remove(b);
                        }
                    }
                }
            });
        }

        @Override // android.service.media.IMediaBrowserService
        public void disconnect(final IMediaBrowserServiceCallbacks callbacks) {
            MediaBrowserService.this.mHandler.post(new Runnable() {
                /* class android.service.media.MediaBrowserService.ServiceBinder.AnonymousClass2 */

                public void run() {
                    if (((ConnectionRecord) MediaBrowserService.this.mConnections.remove(callbacks.asBinder())) != null) {
                    }
                }
            });
        }

        @Override // android.service.media.IMediaBrowserService
        public void addSubscription(final String id, final IMediaBrowserServiceCallbacks callbacks) {
            MediaBrowserService.this.mHandler.post(new Runnable() {
                /* class android.service.media.MediaBrowserService.ServiceBinder.AnonymousClass3 */

                public void run() {
                    ConnectionRecord connection = (ConnectionRecord) MediaBrowserService.this.mConnections.get(callbacks.asBinder());
                    if (connection == null) {
                        Log.w(MediaBrowserService.TAG, "addSubscription for callback that isn't registered id=" + id);
                    } else {
                        MediaBrowserService.this.addSubscription(id, connection);
                    }
                }
            });
        }

        @Override // android.service.media.IMediaBrowserService
        public void removeSubscription(final String id, final IMediaBrowserServiceCallbacks callbacks) {
            MediaBrowserService.this.mHandler.post(new Runnable() {
                /* class android.service.media.MediaBrowserService.ServiceBinder.AnonymousClass4 */

                public void run() {
                    ConnectionRecord connection = (ConnectionRecord) MediaBrowserService.this.mConnections.get(callbacks.asBinder());
                    if (connection == null) {
                        Log.w(MediaBrowserService.TAG, "removeSubscription for callback that isn't registered id=" + id);
                    } else if (!connection.subscriptions.remove(id)) {
                        Log.w(MediaBrowserService.TAG, "removeSubscription called for " + id + " which is not subscribed");
                    }
                }
            });
        }
    }

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        this.mBinder = new ServiceBinder();
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        if (SERVICE_INTERFACE.equals(intent.getAction())) {
            return this.mBinder;
        }
        return null;
    }

    @Override // android.app.Service
    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
    }

    public void setSessionToken(MediaSession.Token token) {
        if (token == null) {
            throw new IllegalStateException(getClass().getName() + ".onCreateSession() set invalid MediaSession.Token");
        }
        this.mSession = token;
    }

    public MediaSession.Token getSessionToken() {
        return this.mSession;
    }

    public void notifyChildrenChanged(final String parentId) {
        if (parentId == null) {
            throw new IllegalArgumentException("parentId cannot be null in notifyChildrenChanged");
        }
        this.mHandler.post(new Runnable() {
            /* class android.service.media.MediaBrowserService.AnonymousClass1 */

            public void run() {
                for (IBinder binder : MediaBrowserService.this.mConnections.keySet()) {
                    ConnectionRecord connection = (ConnectionRecord) MediaBrowserService.this.mConnections.get(binder);
                    if (connection.subscriptions.contains(parentId)) {
                        MediaBrowserService.this.performLoadChildren(parentId, connection);
                    }
                }
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isValidPackage(String pkg, int uid) {
        if (pkg == null) {
            return false;
        }
        for (String str : getPackageManager().getPackagesForUid(uid)) {
            if (str.equals(pkg)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void addSubscription(String id, ConnectionRecord connection) {
        if (connection.subscriptions.add(id)) {
            performLoadChildren(id, connection);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void performLoadChildren(final String parentId, final ConnectionRecord connection) {
        Result<List<MediaBrowser.MediaItem>> result = new Result<List<MediaBrowser.MediaItem>>(parentId) {
            /* class android.service.media.MediaBrowserService.AnonymousClass2 */

            /* access modifiers changed from: package-private */
            public void onResultSent(List<MediaBrowser.MediaItem> list) {
                if (list == null) {
                    throw new IllegalStateException("onLoadChildren sent null list for id " + parentId);
                } else if (MediaBrowserService.this.mConnections.get(connection.callbacks.asBinder()) == connection) {
                    try {
                        connection.callbacks.onLoadChildren(parentId, new ParceledListSlice<>(list));
                    } catch (RemoteException e) {
                        Log.w(MediaBrowserService.TAG, "Calling onLoadChildren() failed for id=" + parentId + " package=" + connection.pkg);
                    }
                }
            }
        };
        onLoadChildren(parentId, result);
        if (!result.isDone()) {
            throw new IllegalStateException("onLoadChildren must call detach() or sendResult() before returning for package=" + connection.pkg + " id=" + parentId);
        }
    }

    public static final class BrowserRoot {
        private final Bundle mExtras;
        private final String mRootId;

        public BrowserRoot(String rootId, Bundle extras) {
            if (rootId == null) {
                throw new IllegalArgumentException("The root id in BrowserRoot cannot be null. Use null for BrowserRoot instead.");
            }
            this.mRootId = rootId;
            this.mExtras = extras;
        }

        public String getRootId() {
            return this.mRootId;
        }

        public Bundle getExtras() {
            return this.mExtras;
        }
    }
}

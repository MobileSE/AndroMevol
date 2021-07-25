package android.media.browse;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ParceledListSlice;
import android.media.MediaDescription;
import android.media.session.MediaSession;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.service.media.IMediaBrowserService;
import android.service.media.IMediaBrowserServiceCallbacks;
import android.service.media.MediaBrowserService;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

public final class MediaBrowser {
    private static final int CONNECT_STATE_CONNECTED = 2;
    private static final int CONNECT_STATE_CONNECTING = 1;
    private static final int CONNECT_STATE_DISCONNECTED = 0;
    private static final int CONNECT_STATE_SUSPENDED = 3;
    private static final boolean DBG = false;
    private static final String TAG = "MediaBrowser";
    private final ConnectionCallback mCallback;
    private final Context mContext;
    private Bundle mExtras;
    private final Handler mHandler = new Handler();
    private MediaSession.Token mMediaSessionToken;
    private final Bundle mRootHints;
    private String mRootId;
    private IMediaBrowserService mServiceBinder;
    private IMediaBrowserServiceCallbacks mServiceCallbacks;
    private final ComponentName mServiceComponent;
    private MediaServiceConnection mServiceConnection;
    private int mState = 0;
    private final ArrayMap<String, Subscription> mSubscriptions = new ArrayMap<>();

    public MediaBrowser(Context context, ComponentName serviceComponent, ConnectionCallback callback, Bundle rootHints) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null");
        } else if (serviceComponent == null) {
            throw new IllegalArgumentException("service component must not be null");
        } else if (callback == null) {
            throw new IllegalArgumentException("connection callback must not be null");
        } else {
            this.mContext = context;
            this.mServiceComponent = serviceComponent;
            this.mCallback = callback;
            this.mRootHints = rootHints;
        }
    }

    public void connect() {
        if (this.mState != 0) {
            throw new IllegalStateException("connect() called while not disconnected (state=" + getStateLabel(this.mState) + ")");
        } else if (this.mServiceBinder != null) {
            throw new RuntimeException("mServiceBinder should be null. Instead it is " + this.mServiceBinder);
        } else if (this.mServiceCallbacks != null) {
            throw new RuntimeException("mServiceCallbacks should be null. Instead it is " + this.mServiceCallbacks);
        } else {
            this.mState = 1;
            Intent intent = new Intent(MediaBrowserService.SERVICE_INTERFACE);
            intent.setComponent(this.mServiceComponent);
            final MediaServiceConnection mediaServiceConnection = new MediaServiceConnection();
            this.mServiceConnection = mediaServiceConnection;
            boolean bound = false;
            try {
                bound = this.mContext.bindService(intent, this.mServiceConnection, 1);
            } catch (Exception e) {
                Log.e(TAG, "Failed binding to service " + this.mServiceComponent);
            }
            if (!bound) {
                this.mHandler.post(new Runnable() {
                    /* class android.media.browse.MediaBrowser.AnonymousClass1 */

                    public void run() {
                        if (mediaServiceConnection == MediaBrowser.this.mServiceConnection) {
                            MediaBrowser.this.forceCloseConnection();
                            MediaBrowser.this.mCallback.onConnectionFailed();
                        }
                    }
                });
            }
        }
    }

    public void disconnect() {
        if (this.mServiceCallbacks != null) {
            try {
                this.mServiceBinder.disconnect(this.mServiceCallbacks);
            } catch (RemoteException e) {
                Log.w(TAG, "RemoteException during connect for " + this.mServiceComponent);
            }
        }
        forceCloseConnection();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void forceCloseConnection() {
        if (this.mServiceConnection != null) {
            this.mContext.unbindService(this.mServiceConnection);
        }
        this.mState = 0;
        this.mServiceConnection = null;
        this.mServiceBinder = null;
        this.mServiceCallbacks = null;
        this.mRootId = null;
        this.mMediaSessionToken = null;
    }

    public boolean isConnected() {
        return this.mState == 2;
    }

    public ComponentName getServiceComponent() {
        if (isConnected()) {
            return this.mServiceComponent;
        }
        throw new IllegalStateException("getServiceComponent() called while not connected (state=" + this.mState + ")");
    }

    public String getRoot() {
        if (isConnected()) {
            return this.mRootId;
        }
        throw new IllegalStateException("getSessionToken() called while not connected (state=" + getStateLabel(this.mState) + ")");
    }

    public Bundle getExtras() {
        if (isConnected()) {
            return this.mExtras;
        }
        throw new IllegalStateException("getExtras() called while not connected (state=" + getStateLabel(this.mState) + ")");
    }

    public MediaSession.Token getSessionToken() {
        if (isConnected()) {
            return this.mMediaSessionToken;
        }
        throw new IllegalStateException("getSessionToken() called while not connected (state=" + this.mState + ")");
    }

    public void subscribe(String parentId, SubscriptionCallback callback) {
        if (parentId == null) {
            throw new IllegalArgumentException("parentId is null");
        } else if (callback == null) {
            throw new IllegalArgumentException("callback is null");
        } else {
            Subscription sub = this.mSubscriptions.get(parentId);
            boolean newSubscription = sub == null;
            if (newSubscription) {
                sub = new Subscription(parentId);
                this.mSubscriptions.put(parentId, sub);
            }
            sub.callback = callback;
            if (this.mState == 2 && newSubscription) {
                try {
                    this.mServiceBinder.addSubscription(parentId, this.mServiceCallbacks);
                } catch (RemoteException e) {
                    Log.d(TAG, "addSubscription failed with RemoteException parentId=" + parentId);
                }
            }
        }
    }

    public void unsubscribe(String parentId) {
        if (parentId == null) {
            throw new IllegalArgumentException("parentId is null");
        }
        Subscription sub = this.mSubscriptions.remove(parentId);
        if (this.mState == 2 && sub != null) {
            try {
                this.mServiceBinder.removeSubscription(parentId, this.mServiceCallbacks);
            } catch (RemoteException e) {
                Log.d(TAG, "removeSubscription failed with RemoteException parentId=" + parentId);
            }
        }
    }

    /* access modifiers changed from: private */
    public static String getStateLabel(int state) {
        switch (state) {
            case 0:
                return "CONNECT_STATE_DISCONNECTED";
            case 1:
                return "CONNECT_STATE_CONNECTING";
            case 2:
                return "CONNECT_STATE_CONNECTED";
            case 3:
                return "CONNECT_STATE_SUSPENDED";
            default:
                return "UNKNOWN/" + state;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void onServiceConnected(final IMediaBrowserServiceCallbacks callback, final String root, final MediaSession.Token session, final Bundle extra) {
        this.mHandler.post(new Runnable() {
            /* class android.media.browse.MediaBrowser.AnonymousClass2 */

            public void run() {
                if (MediaBrowser.this.isCurrent(callback, "onConnect")) {
                    if (MediaBrowser.this.mState != 1) {
                        Log.w(MediaBrowser.TAG, "onConnect from service while mState=" + MediaBrowser.getStateLabel(MediaBrowser.this.mState) + "... ignoring");
                        return;
                    }
                    MediaBrowser.this.mRootId = root;
                    MediaBrowser.this.mMediaSessionToken = session;
                    MediaBrowser.this.mExtras = extra;
                    MediaBrowser.this.mState = 2;
                    MediaBrowser.this.mCallback.onConnected();
                    for (String id : MediaBrowser.this.mSubscriptions.keySet()) {
                        try {
                            MediaBrowser.this.mServiceBinder.addSubscription(id, MediaBrowser.this.mServiceCallbacks);
                        } catch (RemoteException e) {
                            Log.d(MediaBrowser.TAG, "addSubscription failed with RemoteException parentId=" + id);
                        }
                    }
                }
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void onConnectionFailed(final IMediaBrowserServiceCallbacks callback) {
        this.mHandler.post(new Runnable() {
            /* class android.media.browse.MediaBrowser.AnonymousClass3 */

            public void run() {
                Log.e(MediaBrowser.TAG, "onConnectFailed for " + MediaBrowser.this.mServiceComponent);
                if (MediaBrowser.this.isCurrent(callback, "onConnectFailed")) {
                    if (MediaBrowser.this.mState != 1) {
                        Log.w(MediaBrowser.TAG, "onConnect from service while mState=" + MediaBrowser.getStateLabel(MediaBrowser.this.mState) + "... ignoring");
                        return;
                    }
                    MediaBrowser.this.forceCloseConnection();
                    MediaBrowser.this.mCallback.onConnectionFailed();
                }
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void onLoadChildren(final IMediaBrowserServiceCallbacks callback, final String parentId, final ParceledListSlice list) {
        this.mHandler.post(new Runnable() {
            /* class android.media.browse.MediaBrowser.AnonymousClass4 */

            public void run() {
                if (MediaBrowser.this.isCurrent(callback, "onLoadChildren")) {
                    List<MediaItem> data = list.getList();
                    if (data == null) {
                        data = Collections.emptyList();
                    }
                    Subscription subscription = (Subscription) MediaBrowser.this.mSubscriptions.get(parentId);
                    if (subscription != null) {
                        subscription.callback.onChildrenLoaded(parentId, data);
                    }
                }
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isCurrent(IMediaBrowserServiceCallbacks callback, String funcName) {
        if (this.mServiceCallbacks == callback) {
            return true;
        }
        if (this.mState != 0) {
            Log.i(TAG, funcName + " for " + this.mServiceComponent + " with mServiceConnection=" + this.mServiceCallbacks + " this=" + this);
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private ServiceCallbacks getNewServiceCallbacks() {
        return new ServiceCallbacks(this);
    }

    /* access modifiers changed from: package-private */
    public void dump() {
        Log.d(TAG, "MediaBrowser...");
        Log.d(TAG, "  mServiceComponent=" + this.mServiceComponent);
        Log.d(TAG, "  mCallback=" + this.mCallback);
        Log.d(TAG, "  mRootHints=" + this.mRootHints);
        Log.d(TAG, "  mState=" + getStateLabel(this.mState));
        Log.d(TAG, "  mServiceConnection=" + this.mServiceConnection);
        Log.d(TAG, "  mServiceBinder=" + this.mServiceBinder);
        Log.d(TAG, "  mServiceCallbacks=" + this.mServiceCallbacks);
        Log.d(TAG, "  mRootId=" + this.mRootId);
        Log.d(TAG, "  mMediaSessionToken=" + this.mMediaSessionToken);
    }

    public static class MediaItem implements Parcelable {
        public static final Parcelable.Creator<MediaItem> CREATOR = new Parcelable.Creator<MediaItem>() {
            /* class android.media.browse.MediaBrowser.MediaItem.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public MediaItem createFromParcel(Parcel in) {
                return new MediaItem(in);
            }

            @Override // android.os.Parcelable.Creator
            public MediaItem[] newArray(int size) {
                return new MediaItem[size];
            }
        };
        public static final int FLAG_BROWSABLE = 1;
        public static final int FLAG_PLAYABLE = 2;
        private final MediaDescription mDescription;
        private final int mFlags;

        public MediaItem(MediaDescription description, int flags) {
            if (description == null) {
                throw new IllegalArgumentException("description cannot be null");
            } else if (TextUtils.isEmpty(description.getMediaId())) {
                throw new IllegalArgumentException("description must have a non-empty media id");
            } else {
                this.mFlags = flags;
                this.mDescription = description;
            }
        }

        private MediaItem(Parcel in) {
            this.mFlags = in.readInt();
            this.mDescription = MediaDescription.CREATOR.createFromParcel(in);
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel out, int flags) {
            out.writeInt(this.mFlags);
            this.mDescription.writeToParcel(out, flags);
        }

        public String toString() {
            StringBuilder sb = new StringBuilder("MediaItem{");
            sb.append("mFlags=").append(this.mFlags);
            sb.append(", mDescription=").append(this.mDescription);
            sb.append('}');
            return sb.toString();
        }

        public int getFlags() {
            return this.mFlags;
        }

        public boolean isBrowsable() {
            return (this.mFlags & 1) != 0;
        }

        public boolean isPlayable() {
            return (this.mFlags & 2) != 0;
        }

        public MediaDescription getDescription() {
            return this.mDescription;
        }

        public String getMediaId() {
            return this.mDescription.getMediaId();
        }
    }

    public static class ConnectionCallback {
        public void onConnected() {
        }

        public void onConnectionSuspended() {
        }

        public void onConnectionFailed() {
        }
    }

    public static abstract class SubscriptionCallback {
        public void onChildrenLoaded(String parentId, List<MediaItem> list) {
        }

        public void onError(String id) {
        }
    }

    /* access modifiers changed from: private */
    public class MediaServiceConnection implements ServiceConnection {
        private MediaServiceConnection() {
        }

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder binder) {
            if (isCurrent("onServiceConnected")) {
                MediaBrowser.this.mServiceBinder = IMediaBrowserService.Stub.asInterface(binder);
                MediaBrowser.this.mServiceCallbacks = MediaBrowser.this.getNewServiceCallbacks();
                MediaBrowser.this.mState = 1;
                try {
                    MediaBrowser.this.mServiceBinder.connect(MediaBrowser.this.mContext.getPackageName(), MediaBrowser.this.mRootHints, MediaBrowser.this.mServiceCallbacks);
                } catch (RemoteException e) {
                    Log.w(MediaBrowser.TAG, "RemoteException during connect for " + MediaBrowser.this.mServiceComponent);
                }
            }
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
            if (isCurrent("onServiceDisconnected")) {
                MediaBrowser.this.mServiceBinder = null;
                MediaBrowser.this.mServiceCallbacks = null;
                MediaBrowser.this.mState = 3;
                MediaBrowser.this.mCallback.onConnectionSuspended();
            }
        }

        private boolean isCurrent(String funcName) {
            if (MediaBrowser.this.mServiceConnection == this) {
                return true;
            }
            if (MediaBrowser.this.mState != 0) {
                Log.i(MediaBrowser.TAG, funcName + " for " + MediaBrowser.this.mServiceComponent + " with mServiceConnection=" + MediaBrowser.this.mServiceConnection + " this=" + this);
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    public static class ServiceCallbacks extends IMediaBrowserServiceCallbacks.Stub {
        private WeakReference<MediaBrowser> mMediaBrowser;

        public ServiceCallbacks(MediaBrowser mediaBrowser) {
            this.mMediaBrowser = new WeakReference<>(mediaBrowser);
        }

        @Override // android.service.media.IMediaBrowserServiceCallbacks
        public void onConnect(String root, MediaSession.Token session, Bundle extras) {
            MediaBrowser mediaBrowser = this.mMediaBrowser.get();
            if (mediaBrowser != null) {
                mediaBrowser.onServiceConnected(this, root, session, extras);
            }
        }

        @Override // android.service.media.IMediaBrowserServiceCallbacks
        public void onConnectFailed() {
            MediaBrowser mediaBrowser = this.mMediaBrowser.get();
            if (mediaBrowser != null) {
                mediaBrowser.onConnectionFailed(this);
            }
        }

        @Override // android.service.media.IMediaBrowserServiceCallbacks
        public void onLoadChildren(String parentId, ParceledListSlice list) {
            MediaBrowser mediaBrowser = this.mMediaBrowser.get();
            if (mediaBrowser != null) {
                mediaBrowser.onLoadChildren(this, parentId, list);
            }
        }
    }

    /* access modifiers changed from: private */
    public static class Subscription {
        SubscriptionCallback callback;
        final String id;

        Subscription(String id2) {
            this.id = id2;
        }
    }
}

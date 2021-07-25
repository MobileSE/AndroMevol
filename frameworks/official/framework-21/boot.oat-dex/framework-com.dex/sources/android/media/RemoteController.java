package android.media;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.IRemoteControlDisplay;
import android.media.session.MediaController;
import android.media.session.MediaSessionLegacyHelper;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.net.ProxyInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import java.lang.ref.WeakReference;
import java.util.List;

@Deprecated
public final class RemoteController {
    private static final boolean DEBUG = false;
    private static final int MAX_BITMAP_DIMENSION = 512;
    private static final int MSG_CLIENT_CHANGE = 4;
    private static final int MSG_DISPLAY_ENABLE = 5;
    private static final int MSG_NEW_MEDIA_METADATA = 7;
    private static final int MSG_NEW_METADATA = 3;
    private static final int MSG_NEW_PENDING_INTENT = 0;
    private static final int MSG_NEW_PLAYBACK_INFO = 1;
    private static final int MSG_NEW_PLAYBACK_STATE = 6;
    private static final int MSG_NEW_TRANSPORT_INFO = 2;
    public static final int POSITION_SYNCHRONIZATION_CHECK = 1;
    public static final int POSITION_SYNCHRONIZATION_NONE = 0;
    private static final int SENDMSG_NOOP = 1;
    private static final int SENDMSG_QUEUE = 2;
    private static final int SENDMSG_REPLACE = 0;
    private static final String TAG = "RemoteController";
    private static final int TRANSPORT_UNKNOWN = 0;
    private static final boolean USE_SESSIONS = true;
    private static final Object mGenLock = new Object();
    private static final Object mInfoLock = new Object();
    private int mArtworkHeight;
    private int mArtworkWidth;
    private final AudioManager mAudioManager;
    private int mClientGenerationIdCurrent;
    private PendingIntent mClientPendingIntentCurrent;
    private final Context mContext;
    private MediaController mCurrentSession;
    private boolean mEnabled;
    private final EventHandler mEventHandler;
    private boolean mIsRegistered;
    private PlaybackInfo mLastPlaybackInfo;
    private final int mMaxBitmapDimension;
    private MetadataEditor mMetadataEditor;
    private OnClientUpdateListener mOnClientUpdateListener;
    private final RcDisplay mRcd;
    private MediaController.Callback mSessionCb;
    private MediaSessionManager.OnActiveSessionsChangedListener mSessionListener;
    private MediaSessionManager mSessionManager;

    public interface OnClientUpdateListener {
        void onClientChange(boolean z);

        void onClientMetadataUpdate(MetadataEditor metadataEditor);

        void onClientPlaybackStateUpdate(int i);

        void onClientPlaybackStateUpdate(int i, long j, long j2, float f);

        void onClientTransportControlUpdate(int i);
    }

    public RemoteController(Context context, OnClientUpdateListener updateListener) throws IllegalArgumentException {
        this(context, updateListener, null);
    }

    public RemoteController(Context context, OnClientUpdateListener updateListener, Looper looper) throws IllegalArgumentException {
        this.mSessionCb = new MediaControllerCallback();
        this.mClientGenerationIdCurrent = 0;
        this.mIsRegistered = false;
        this.mArtworkWidth = -1;
        this.mArtworkHeight = -1;
        this.mEnabled = true;
        if (context == null) {
            throw new IllegalArgumentException("Invalid null Context");
        } else if (updateListener == null) {
            throw new IllegalArgumentException("Invalid null OnClientUpdateListener");
        } else {
            if (looper != null) {
                this.mEventHandler = new EventHandler(this, looper);
            } else {
                Looper l = Looper.myLooper();
                if (l != null) {
                    this.mEventHandler = new EventHandler(this, l);
                } else {
                    throw new IllegalArgumentException("Calling thread not associated with a looper");
                }
            }
            this.mOnClientUpdateListener = updateListener;
            this.mContext = context;
            this.mRcd = new RcDisplay(this);
            this.mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            this.mSessionManager = (MediaSessionManager) context.getSystemService(Context.MEDIA_SESSION_SERVICE);
            this.mSessionListener = new TopTransportSessionListener();
            if (ActivityManager.isLowRamDeviceStatic()) {
                this.mMaxBitmapDimension = 512;
                return;
            }
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            this.mMaxBitmapDimension = Math.max(dm.widthPixels, dm.heightPixels);
        }
    }

    public String getRemoteControlClientPackageName() {
        String packageName;
        synchronized (mInfoLock) {
            packageName = this.mCurrentSession != null ? this.mCurrentSession.getPackageName() : null;
        }
        return packageName;
    }

    public long getEstimatedMediaPosition() {
        PlaybackState state;
        synchronized (mInfoLock) {
            if (this.mCurrentSession == null || (state = this.mCurrentSession.getPlaybackState()) == null) {
                return -1;
            }
            return state.getPosition();
        }
    }

    public boolean sendMediaKeyEvent(KeyEvent keyEvent) throws IllegalArgumentException {
        boolean z;
        if (!KeyEvent.isMediaKey(keyEvent.getKeyCode())) {
            throw new IllegalArgumentException("not a media key event");
        }
        synchronized (mInfoLock) {
            if (this.mCurrentSession != null) {
                z = this.mCurrentSession.dispatchMediaButtonEvent(keyEvent);
            } else {
                z = false;
            }
        }
        return z;
    }

    public boolean seekTo(long timeMs) throws IllegalArgumentException {
        if (!this.mEnabled) {
            Log.e(TAG, "Cannot use seekTo() from a disabled RemoteController");
            return false;
        } else if (timeMs < 0) {
            throw new IllegalArgumentException("illegal negative time value");
        } else {
            synchronized (mInfoLock) {
                if (this.mCurrentSession != null) {
                    this.mCurrentSession.getTransportControls().seekTo(timeMs);
                }
            }
            return true;
        }
    }

    public boolean setArtworkConfiguration(boolean wantBitmap, int width, int height) throws IllegalArgumentException {
        synchronized (mInfoLock) {
            if (!wantBitmap) {
                this.mArtworkWidth = -1;
                this.mArtworkHeight = -1;
            } else if (width <= 0 || height <= 0) {
                throw new IllegalArgumentException("Invalid dimensions");
            } else {
                if (width > this.mMaxBitmapDimension) {
                    width = this.mMaxBitmapDimension;
                }
                if (height > this.mMaxBitmapDimension) {
                    height = this.mMaxBitmapDimension;
                }
                this.mArtworkWidth = width;
                this.mArtworkHeight = height;
            }
        }
        return true;
    }

    public boolean setArtworkConfiguration(int width, int height) throws IllegalArgumentException {
        return setArtworkConfiguration(true, width, height);
    }

    public boolean clearArtworkConfiguration() {
        return setArtworkConfiguration(false, -1, -1);
    }

    public boolean setSynchronizationMode(int sync) throws IllegalArgumentException {
        boolean z = false;
        if (sync != 0 && sync != 1) {
            throw new IllegalArgumentException("Unknown synchronization mode " + sync);
        } else if (!this.mIsRegistered) {
            Log.e(TAG, "Cannot set synchronization mode on an unregistered RemoteController");
            return false;
        } else {
            AudioManager audioManager = this.mAudioManager;
            RcDisplay rcDisplay = this.mRcd;
            if (1 == sync) {
                z = true;
            }
            audioManager.remoteControlDisplayWantsPlaybackPositionSync(rcDisplay, z);
            return true;
        }
    }

    public MetadataEditor editMetadata() {
        MetadataEditor editor = new MetadataEditor();
        editor.mEditorMetadata = new Bundle();
        editor.mEditorArtwork = null;
        editor.mMetadataChanged = true;
        editor.mArtworkChanged = true;
        editor.mEditableKeys = 0;
        return editor;
    }

    public class MetadataEditor extends MediaMetadataEditor {
        protected MetadataEditor() {
        }

        protected MetadataEditor(Bundle metadata, long editableKeys) {
            this.mEditorMetadata = metadata;
            this.mEditableKeys = editableKeys;
            this.mEditorArtwork = (Bitmap) metadata.getParcelable(String.valueOf(100));
            if (this.mEditorArtwork != null) {
                cleanupBitmapFromBundle(100);
            }
            this.mMetadataChanged = true;
            this.mArtworkChanged = true;
            this.mApplied = false;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void cleanupBitmapFromBundle(int key) {
            if (METADATA_KEYS_TYPE.get(key, -1) == 2) {
                this.mEditorMetadata.remove(String.valueOf(key));
            }
        }

        @Override // android.media.MediaMetadataEditor
        public synchronized void apply() {
            Rating rating;
            if (this.mMetadataChanged) {
                synchronized (RemoteController.mInfoLock) {
                    if (!(RemoteController.this.mCurrentSession == null || !this.mEditorMetadata.containsKey(String.valueOf((int) MediaMetadataEditor.RATING_KEY_BY_USER)) || (rating = (Rating) getObject(MediaMetadataEditor.RATING_KEY_BY_USER, null)) == null)) {
                        RemoteController.this.mCurrentSession.getTransportControls().setRating(rating);
                    }
                }
                this.mApplied = false;
            }
        }
    }

    private static class RcDisplay extends IRemoteControlDisplay.Stub {
        private final WeakReference<RemoteController> mController;

        RcDisplay(RemoteController rc) {
            this.mController = new WeakReference<>(rc);
        }

        @Override // android.media.IRemoteControlDisplay
        public void setCurrentClientId(int genId, PendingIntent clientMediaIntent, boolean clearing) {
            RemoteController rc = this.mController.get();
            if (rc != null) {
                boolean isNew = false;
                synchronized (RemoteController.mGenLock) {
                    if (rc.mClientGenerationIdCurrent != genId) {
                        rc.mClientGenerationIdCurrent = genId;
                        isNew = true;
                    }
                }
                if (clientMediaIntent != null) {
                    RemoteController.sendMsg(rc.mEventHandler, 0, 0, genId, 0, clientMediaIntent, 0);
                }
                if (isNew || clearing) {
                    RemoteController.sendMsg(rc.mEventHandler, 4, 0, genId, clearing ? 1 : 0, null, 0);
                }
            }
        }

        @Override // android.media.IRemoteControlDisplay
        public void setEnabled(boolean enabled) {
            RemoteController rc = this.mController.get();
            if (rc != null) {
                RemoteController.sendMsg(rc.mEventHandler, 5, 0, enabled ? 1 : 0, 0, null, 0);
            }
        }

        @Override // android.media.IRemoteControlDisplay
        public void setPlaybackState(int genId, int state, long stateChangeTimeMs, long currentPosMs, float speed) {
            RemoteController rc = this.mController.get();
            if (rc != null) {
                synchronized (RemoteController.mGenLock) {
                    if (rc.mClientGenerationIdCurrent == genId) {
                        RemoteController.sendMsg(rc.mEventHandler, 1, 0, genId, 0, new PlaybackInfo(state, stateChangeTimeMs, currentPosMs, speed), 0);
                    }
                }
            }
        }

        @Override // android.media.IRemoteControlDisplay
        public void setTransportControlInfo(int genId, int transportControlFlags, int posCapabilities) {
            RemoteController rc = this.mController.get();
            if (rc != null) {
                synchronized (RemoteController.mGenLock) {
                    if (rc.mClientGenerationIdCurrent == genId) {
                        RemoteController.sendMsg(rc.mEventHandler, 2, 0, genId, transportControlFlags, null, 0);
                    }
                }
            }
        }

        @Override // android.media.IRemoteControlDisplay
        public void setMetadata(int genId, Bundle metadata) {
            RemoteController rc = this.mController.get();
            if (rc != null && metadata != null) {
                synchronized (RemoteController.mGenLock) {
                    if (rc.mClientGenerationIdCurrent == genId) {
                        RemoteController.sendMsg(rc.mEventHandler, 3, 2, genId, 0, metadata, 0);
                    }
                }
            }
        }

        @Override // android.media.IRemoteControlDisplay
        public void setArtwork(int genId, Bitmap artwork) {
            RemoteController rc = this.mController.get();
            if (rc != null) {
                synchronized (RemoteController.mGenLock) {
                    if (rc.mClientGenerationIdCurrent == genId) {
                        Bundle metadata = new Bundle(1);
                        metadata.putParcelable(String.valueOf(100), artwork);
                        RemoteController.sendMsg(rc.mEventHandler, 3, 2, genId, 0, metadata, 0);
                    }
                }
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:15:0x0021, code lost:
            if (r10 != null) goto L_0x0029;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:16:0x0023, code lost:
            r10 = new android.os.Bundle(1);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:17:0x0029, code lost:
            if (r11 == null) goto L_0x0034;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:18:0x002b, code lost:
            r10.putParcelable(java.lang.String.valueOf(100), r11);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:19:0x0034, code lost:
            android.media.RemoteController.sendMsg(r7.mEventHandler, 3, 2, r9, 0, r10, 0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:23:?, code lost:
            return;
         */
        @Override // android.media.IRemoteControlDisplay
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void setAllMetadata(int r9, android.os.Bundle r10, android.graphics.Bitmap r11) {
            /*
                r8 = this;
                r4 = 0
                java.lang.ref.WeakReference<android.media.RemoteController> r0 = r8.mController
                java.lang.Object r7 = r0.get()
                android.media.RemoteController r7 = (android.media.RemoteController) r7
                if (r7 != 0) goto L_0x000c
            L_0x000b:
                return
            L_0x000c:
                if (r10 != 0) goto L_0x0010
                if (r11 == 0) goto L_0x000b
            L_0x0010:
                java.lang.Object r1 = android.media.RemoteController.access$400()
                monitor-enter(r1)
                int r0 = android.media.RemoteController.access$500(r7)     // Catch:{ all -> 0x001d }
                if (r0 == r9) goto L_0x0020
                monitor-exit(r1)     // Catch:{ all -> 0x001d }
                goto L_0x000b
            L_0x001d:
                r0 = move-exception
                monitor-exit(r1)     // Catch:{ all -> 0x001d }
                throw r0
            L_0x0020:
                monitor-exit(r1)
                if (r10 != 0) goto L_0x0029
                android.os.Bundle r10 = new android.os.Bundle
                r0 = 1
                r10.<init>(r0)
            L_0x0029:
                if (r11 == 0) goto L_0x0034
                r0 = 100
                java.lang.String r0 = java.lang.String.valueOf(r0)
                r10.putParcelable(r0, r11)
            L_0x0034:
                android.media.RemoteController$EventHandler r0 = android.media.RemoteController.access$600(r7)
                r1 = 3
                r2 = 2
                r3 = r9
                r5 = r10
                r6 = r4
                android.media.RemoteController.access$700(r0, r1, r2, r3, r4, r5, r6)
                goto L_0x000b
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.RemoteController.RcDisplay.setAllMetadata(int, android.os.Bundle, android.graphics.Bitmap):void");
        }
    }

    private class MediaControllerCallback extends MediaController.Callback {
        private MediaControllerCallback() {
        }

        @Override // android.media.session.MediaController.Callback
        public void onPlaybackStateChanged(PlaybackState state) {
            RemoteController.this.onNewPlaybackState(state);
        }

        @Override // android.media.session.MediaController.Callback
        public void onMetadataChanged(MediaMetadata metadata) {
            RemoteController.this.onNewMediaMetadata(metadata);
        }
    }

    private class TopTransportSessionListener implements MediaSessionManager.OnActiveSessionsChangedListener {
        private TopTransportSessionListener() {
        }

        @Override // android.media.session.MediaSessionManager.OnActiveSessionsChangedListener
        public void onActiveSessionsChanged(List<MediaController> controllers) {
            int size = controllers.size();
            for (int i = 0; i < size; i++) {
                MediaController controller = controllers.get(i);
                if ((2 & controller.getFlags()) != 0) {
                    RemoteController.this.updateController(controller);
                    return;
                }
            }
            RemoteController.this.updateController(null);
        }
    }

    /* access modifiers changed from: private */
    public class EventHandler extends Handler {
        public EventHandler(RemoteController rc, Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            boolean z = true;
            switch (msg.what) {
                case 0:
                    RemoteController.this.onNewPendingIntent(msg.arg1, (PendingIntent) msg.obj);
                    return;
                case 1:
                    RemoteController.this.onNewPlaybackInfo(msg.arg1, (PlaybackInfo) msg.obj);
                    return;
                case 2:
                    RemoteController.this.onNewTransportInfo(msg.arg1, msg.arg2);
                    return;
                case 3:
                    RemoteController.this.onNewMetadata(msg.arg1, (Bundle) msg.obj);
                    return;
                case 4:
                    RemoteController remoteController = RemoteController.this;
                    int i = msg.arg1;
                    if (msg.arg2 != 1) {
                        z = false;
                    }
                    remoteController.onClientChange(i, z);
                    return;
                case 5:
                    RemoteController remoteController2 = RemoteController.this;
                    if (msg.arg1 != 1) {
                        z = false;
                    }
                    remoteController2.onDisplayEnable(z);
                    return;
                case 6:
                    RemoteController.this.onNewPlaybackState((PlaybackState) msg.obj);
                    return;
                case 7:
                    RemoteController.this.onNewMediaMetadata((MediaMetadata) msg.obj);
                    return;
                default:
                    Log.e(RemoteController.TAG, "unknown event " + msg.what);
                    return;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void startListeningToSessions() {
        ComponentName listenerComponent = new ComponentName(this.mContext, this.mOnClientUpdateListener.getClass());
        Handler handler = null;
        if (Looper.myLooper() == null) {
            handler = new Handler(Looper.getMainLooper());
        }
        this.mSessionManager.addOnActiveSessionsChangedListener(this.mSessionListener, listenerComponent, UserHandle.myUserId(), handler);
        this.mSessionListener.onActiveSessionsChanged(this.mSessionManager.getActiveSessions(listenerComponent));
    }

    /* access modifiers changed from: package-private */
    public void stopListeningToSessions() {
        this.mSessionManager.removeOnActiveSessionsChangedListener(this.mSessionListener);
    }

    /* access modifiers changed from: private */
    public static void sendMsg(Handler handler, int msg, int existingMsgPolicy, int arg1, int arg2, Object obj, int delayMs) {
        if (handler == null) {
            Log.e(TAG, "null event handler, will not deliver message " + msg);
            return;
        }
        if (existingMsgPolicy == 0) {
            handler.removeMessages(msg);
        } else if (existingMsgPolicy == 1 && handler.hasMessages(msg)) {
            return;
        }
        handler.sendMessageDelayed(handler.obtainMessage(msg, arg1, arg2, obj), (long) delayMs);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onNewPendingIntent(int genId, PendingIntent pi) {
        synchronized (mGenLock) {
            if (this.mClientGenerationIdCurrent == genId) {
                synchronized (mInfoLock) {
                    this.mClientPendingIntentCurrent = pi;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Code restructure failed: missing block: B:10:?, code lost:
        r0 = r7.mOnClientUpdateListener;
        r7.mLastPlaybackInfo = r9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0011, code lost:
        monitor-exit(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0012, code lost:
        if (r0 == null) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x001d, code lost:
        if (r9.mCurrentPosMs != android.media.RemoteControlClient.PLAYBACK_POSITION_ALWAYS_UNKNOWN) goto L_0x002b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x001f, code lost:
        r0.onClientPlaybackStateUpdate(r9.mState);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x002b, code lost:
        r0.onClientPlaybackStateUpdate(r9.mState, r9.mStateChangeTimeMs, r9.mCurrentPosMs, r9.mSpeed);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:7:0x000a, code lost:
        r2 = android.media.RemoteController.mInfoLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x000c, code lost:
        monitor-enter(r2);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void onNewPlaybackInfo(int r8, android.media.RemoteController.PlaybackInfo r9) {
        /*
            r7 = this;
            java.lang.Object r2 = android.media.RemoteController.mGenLock
            monitor-enter(r2)
            int r1 = r7.mClientGenerationIdCurrent     // Catch:{ all -> 0x0025 }
            if (r1 == r8) goto L_0x0009
            monitor-exit(r2)     // Catch:{ all -> 0x0025 }
        L_0x0008:
            return
        L_0x0009:
            monitor-exit(r2)     // Catch:{ all -> 0x0025 }
            java.lang.Object r2 = android.media.RemoteController.mInfoLock
            monitor-enter(r2)
            android.media.RemoteController$OnClientUpdateListener r0 = r7.mOnClientUpdateListener     // Catch:{ all -> 0x0028 }
            r7.mLastPlaybackInfo = r9     // Catch:{ all -> 0x0028 }
            monitor-exit(r2)     // Catch:{ all -> 0x0028 }
            if (r0 == 0) goto L_0x0008
            long r2 = r9.mCurrentPosMs
            r4 = -9216204211029966080(0x8019771980198300, double:-3.541376495412184E-308)
            int r1 = (r2 > r4 ? 1 : (r2 == r4 ? 0 : -1))
            if (r1 != 0) goto L_0x002b
            int r1 = r9.mState
            r0.onClientPlaybackStateUpdate(r1)
            goto L_0x0008
        L_0x0025:
            r1 = move-exception
            monitor-exit(r2)
            throw r1
        L_0x0028:
            r1 = move-exception
            monitor-exit(r2)
            throw r1
        L_0x002b:
            int r1 = r9.mState
            long r2 = r9.mStateChangeTimeMs
            long r4 = r9.mCurrentPosMs
            float r6 = r9.mSpeed
            r0.onClientPlaybackStateUpdate(r1, r2, r4, r6)
            goto L_0x0008
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.RemoteController.onNewPlaybackInfo(int, android.media.RemoteController$PlaybackInfo):void");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Code restructure failed: missing block: B:10:?, code lost:
        r0 = r3.mOnClientUpdateListener;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x000f, code lost:
        monitor-exit(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0010, code lost:
        if (r0 == null) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0012, code lost:
        r0.onClientTransportControlUpdate(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:7:0x000a, code lost:
        r2 = android.media.RemoteController.mInfoLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x000c, code lost:
        monitor-enter(r2);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void onNewTransportInfo(int r4, int r5) {
        /*
            r3 = this;
            java.lang.Object r2 = android.media.RemoteController.mGenLock
            monitor-enter(r2)
            int r1 = r3.mClientGenerationIdCurrent     // Catch:{ all -> 0x0016 }
            if (r1 == r4) goto L_0x0009
            monitor-exit(r2)     // Catch:{ all -> 0x0016 }
        L_0x0008:
            return
        L_0x0009:
            monitor-exit(r2)     // Catch:{ all -> 0x0016 }
            java.lang.Object r2 = android.media.RemoteController.mInfoLock
            monitor-enter(r2)
            android.media.RemoteController$OnClientUpdateListener r0 = r3.mOnClientUpdateListener     // Catch:{ all -> 0x0019 }
            monitor-exit(r2)     // Catch:{ all -> 0x0019 }
            if (r0 == 0) goto L_0x0008
            r0.onClientTransportControlUpdate(r5)
            goto L_0x0008
        L_0x0016:
            r1 = move-exception
            monitor-exit(r2)
            throw r1
        L_0x0019:
            r1 = move-exception
            monitor-exit(r2)
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.RemoteController.onNewTransportInfo(int, int):void");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0022, code lost:
        r5 = android.media.RemoteController.mInfoLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0024, code lost:
        monitor-enter(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:?, code lost:
        r2 = r10.mOnClientUpdateListener;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0029, code lost:
        if (r10.mMetadataEditor == null) goto L_0x0064;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x002f, code lost:
        if (r10.mMetadataEditor.mEditorMetadata == null) goto L_0x0064;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0035, code lost:
        if (r10.mMetadataEditor.mEditorMetadata == r12) goto L_0x003e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0037, code lost:
        r10.mMetadataEditor.mEditorMetadata.putAll(r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x003e, code lost:
        r10.mMetadataEditor.putBitmap(100, (android.graphics.Bitmap) r12.getParcelable(java.lang.String.valueOf(100)));
        r10.mMetadataEditor.cleanupBitmapFromBundle(100);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0058, code lost:
        r3 = r10.mMetadataEditor;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x005a, code lost:
        monitor-exit(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x005b, code lost:
        if (r2 == null) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x005d, code lost:
        r2.onClientMetadataUpdate(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0064, code lost:
        r10.mMetadataEditor = new android.media.RemoteController.MetadataEditor(r10, r12, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:7:0x000f, code lost:
        r0 = r12.getLong(java.lang.String.valueOf((int) android.media.MediaMetadataEditor.KEY_EDITABLE_MASK), 0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0019, code lost:
        if (r0 == 0) goto L_0x0022;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x001b, code lost:
        r12.remove(java.lang.String.valueOf((int) android.media.MediaMetadataEditor.KEY_EDITABLE_MASK));
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void onNewMetadata(int r11, android.os.Bundle r12) {
        /*
        // Method dump skipped, instructions count: 111
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.RemoteController.onNewMetadata(int, android.os.Bundle):void");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Code restructure failed: missing block: B:10:?, code lost:
        r0 = r3.mOnClientUpdateListener;
        r3.mMetadataEditor = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0012, code lost:
        monitor-exit(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0013, code lost:
        if (r0 == null) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0015, code lost:
        r0.onClientChange(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:7:0x000a, code lost:
        r2 = android.media.RemoteController.mInfoLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x000c, code lost:
        monitor-enter(r2);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void onClientChange(int r4, boolean r5) {
        /*
            r3 = this;
            java.lang.Object r2 = android.media.RemoteController.mGenLock
            monitor-enter(r2)
            int r1 = r3.mClientGenerationIdCurrent     // Catch:{ all -> 0x0019 }
            if (r1 == r4) goto L_0x0009
            monitor-exit(r2)     // Catch:{ all -> 0x0019 }
        L_0x0008:
            return
        L_0x0009:
            monitor-exit(r2)     // Catch:{ all -> 0x0019 }
            java.lang.Object r2 = android.media.RemoteController.mInfoLock
            monitor-enter(r2)
            android.media.RemoteController$OnClientUpdateListener r0 = r3.mOnClientUpdateListener     // Catch:{ all -> 0x001c }
            r1 = 0
            r3.mMetadataEditor = r1     // Catch:{ all -> 0x001c }
            monitor-exit(r2)     // Catch:{ all -> 0x001c }
            if (r0 == 0) goto L_0x0008
            r0.onClientChange(r5)
            goto L_0x0008
        L_0x0019:
            r1 = move-exception
            monitor-exit(r2)
            throw r1
        L_0x001c:
            r1 = move-exception
            monitor-exit(r2)
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.RemoteController.onClientChange(int, boolean):void");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onDisplayEnable(boolean enabled) {
        int genId;
        synchronized (mInfoLock) {
            this.mEnabled = enabled;
            OnClientUpdateListener onClientUpdateListener = this.mOnClientUpdateListener;
        }
        if (!enabled) {
            synchronized (mGenLock) {
                genId = this.mClientGenerationIdCurrent;
            }
            sendMsg(this.mEventHandler, 1, 0, genId, 0, new PlaybackInfo(1, SystemClock.elapsedRealtime(), 0, 0.0f), 0);
            sendMsg(this.mEventHandler, 2, 0, genId, 0, null, 0);
            Bundle metadata = new Bundle(3);
            metadata.putString(String.valueOf(7), ProxyInfo.LOCAL_EXCL_LIST);
            metadata.putString(String.valueOf(2), ProxyInfo.LOCAL_EXCL_LIST);
            metadata.putLong(String.valueOf(9), 0);
            sendMsg(this.mEventHandler, 3, 2, genId, 0, metadata, 0);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateController(MediaController controller) {
        synchronized (mInfoLock) {
            if (controller == null) {
                if (this.mCurrentSession != null) {
                    this.mCurrentSession.unregisterCallback(this.mSessionCb);
                    this.mCurrentSession = null;
                    sendMsg(this.mEventHandler, 4, 0, 0, 1, null, 0);
                }
            } else if (this.mCurrentSession == null || !controller.getSessionToken().equals(this.mCurrentSession.getSessionToken())) {
                if (this.mCurrentSession != null) {
                    this.mCurrentSession.unregisterCallback(this.mSessionCb);
                }
                sendMsg(this.mEventHandler, 4, 0, 0, 0, null, 0);
                this.mCurrentSession = controller;
                this.mCurrentSession.registerCallback(this.mSessionCb, this.mEventHandler);
                sendMsg(this.mEventHandler, 6, 0, 0, 0, controller.getPlaybackState(), 0);
                sendMsg(this.mEventHandler, 7, 0, 0, 0, controller.getMetadata(), 0);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onNewPlaybackState(PlaybackState state) {
        OnClientUpdateListener l;
        synchronized (mInfoLock) {
            l = this.mOnClientUpdateListener;
        }
        if (l != null) {
            int playstate = state == null ? 0 : PlaybackState.getRccStateFromState(state.getState());
            if (state == null || state.getPosition() == -1) {
                l.onClientPlaybackStateUpdate(playstate);
            } else {
                l.onClientPlaybackStateUpdate(playstate, state.getLastPositionUpdateTime(), state.getPosition(), state.getPlaybackSpeed());
            }
            if (state != null) {
                l.onClientTransportControlUpdate(PlaybackState.getRccControlFlagsFromActions(state.getActions()));
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onNewMediaMetadata(MediaMetadata metadata) {
        OnClientUpdateListener l;
        MetadataEditor metadataEditor;
        if (metadata != null) {
            synchronized (mInfoLock) {
                l = this.mOnClientUpdateListener;
                this.mMetadataEditor = new MetadataEditor(MediaSessionLegacyHelper.getOldMetadata(metadata, this.mArtworkWidth, this.mArtworkHeight), this.mCurrentSession != null && this.mCurrentSession.getRatingType() != 0 ? 268435457 : 0);
                metadataEditor = this.mMetadataEditor;
            }
            if (l != null) {
                l.onClientMetadataUpdate(metadataEditor);
            }
        }
    }

    /* access modifiers changed from: private */
    public static class PlaybackInfo {
        long mCurrentPosMs;
        float mSpeed;
        int mState;
        long mStateChangeTimeMs;

        PlaybackInfo(int state, long stateChangeTimeMs, long currentPosMs, float speed) {
            this.mState = state;
            this.mStateChangeTimeMs = stateChangeTimeMs;
            this.mCurrentPosMs = currentPosMs;
            this.mSpeed = speed;
        }
    }

    /* access modifiers changed from: package-private */
    public void setIsRegistered(boolean registered) {
        synchronized (mInfoLock) {
            this.mIsRegistered = registered;
        }
    }

    /* access modifiers changed from: package-private */
    public RcDisplay getRcDisplay() {
        return this.mRcd;
    }

    /* access modifiers changed from: package-private */
    public int[] getArtworkSize() {
        int[] size;
        synchronized (mInfoLock) {
            size = new int[]{this.mArtworkWidth, this.mArtworkHeight};
        }
        return size;
    }

    /* access modifiers changed from: package-private */
    public OnClientUpdateListener getUpdateListener() {
        return this.mOnClientUpdateListener;
    }
}

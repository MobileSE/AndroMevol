package android.media;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import java.io.PrintWriter;
import java.util.NoSuchElementException;

/* access modifiers changed from: package-private */
public class PlayerRecord implements IBinder.DeathRecipient {
    private static final boolean DEBUG = false;
    private static final String TAG = "MediaFocusControl";
    public static MediaFocusControl sController;
    private static int sLastRccId = 0;
    private String mCallingPackageName;
    private int mCallingUid;
    private final PendingIntent mMediaIntent;
    public RccPlaybackState mPlaybackState;
    public int mPlaybackStream;
    public int mPlaybackType;
    public int mPlaybackVolume;
    public int mPlaybackVolumeHandling;
    public int mPlaybackVolumeMax;
    private IRemoteControlClient mRcClient;
    private RcClientDeathHandler mRcClientDeathHandler;
    private int mRccId = -1;
    private final ComponentName mReceiverComponent;
    public IRemoteVolumeObserver mRemoteVolumeObs;
    private IBinder mToken;

    /* access modifiers changed from: protected */
    public static class RccPlaybackState {
        public long mPositionMs;
        public float mSpeed;
        public int mState;

        public RccPlaybackState(int state, long positionMs, float speed) {
            this.mState = state;
            this.mPositionMs = positionMs;
            this.mSpeed = speed;
        }

        public void reset() {
            this.mState = 1;
            this.mPositionMs = -1;
            this.mSpeed = 1.0f;
        }

        public String toString() {
            return stateToString() + ", " + posToString() + ", " + this.mSpeed + "X";
        }

        private String posToString() {
            if (this.mPositionMs == -1) {
                return "PLAYBACK_POSITION_INVALID";
            }
            if (this.mPositionMs == RemoteControlClient.PLAYBACK_POSITION_ALWAYS_UNKNOWN) {
                return "PLAYBACK_POSITION_ALWAYS_UNKNOWN";
            }
            return String.valueOf(this.mPositionMs) + "ms";
        }

        private String stateToString() {
            switch (this.mState) {
                case 0:
                    return "PLAYSTATE_NONE";
                case 1:
                    return "PLAYSTATE_STOPPED";
                case 2:
                    return "PLAYSTATE_PAUSED";
                case 3:
                    return "PLAYSTATE_PLAYING";
                case 4:
                    return "PLAYSTATE_FAST_FORWARDING";
                case 5:
                    return "PLAYSTATE_REWINDING";
                case 6:
                    return "PLAYSTATE_SKIPPING_FORWARDS";
                case 7:
                    return "PLAYSTATE_SKIPPING_BACKWARDS";
                case 8:
                    return "PLAYSTATE_BUFFERING";
                case 9:
                    return "PLAYSTATE_ERROR";
                default:
                    return "[invalid playstate]";
            }
        }
    }

    /* access modifiers changed from: private */
    public class RcClientDeathHandler implements IBinder.DeathRecipient {
        private final IBinder mCb;
        private final PendingIntent mMediaIntent;

        RcClientDeathHandler(IBinder cb, PendingIntent pi) {
            this.mCb = cb;
            this.mMediaIntent = pi;
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            Log.w(PlayerRecord.TAG, "  RemoteControlClient died");
            PlayerRecord.sController.registerRemoteControlClient(this.mMediaIntent, null, null);
            PlayerRecord.sController.postReevaluateRemote();
        }

        public IBinder getBinder() {
            return this.mCb;
        }
    }

    /* access modifiers changed from: protected */
    public static class RemotePlaybackState {
        int mRccId;
        int mVolume;
        int mVolumeHandling = 1;
        int mVolumeMax;

        protected RemotePlaybackState(int id, int vol, int volMax) {
            this.mRccId = id;
            this.mVolume = vol;
            this.mVolumeMax = volMax;
        }
    }

    /* access modifiers changed from: package-private */
    public void dump(PrintWriter pw, boolean registrationInfo) {
        if (registrationInfo) {
            pw.println("  pi: " + this.mMediaIntent + " -- pack: " + this.mCallingPackageName + "  -- ercvr: " + this.mReceiverComponent + "  -- client: " + this.mRcClient + "  -- uid: " + this.mCallingUid + "  -- type: " + this.mPlaybackType + "  state: " + this.mPlaybackState);
        } else {
            pw.println("  uid: " + this.mCallingUid + "  -- id: " + this.mRccId + "  -- type: " + this.mPlaybackType + "  -- state: " + this.mPlaybackState + "  -- vol handling: " + this.mPlaybackVolumeHandling + "  -- vol: " + this.mPlaybackVolume + "  -- volMax: " + this.mPlaybackVolumeMax + "  -- volObs: " + this.mRemoteVolumeObs);
        }
    }

    protected static void setMediaFocusControl(MediaFocusControl mfc) {
        sController = mfc;
    }

    protected PlayerRecord(PendingIntent mediaIntent, ComponentName eventReceiver, IBinder token) {
        this.mMediaIntent = mediaIntent;
        this.mReceiverComponent = eventReceiver;
        this.mToken = token;
        this.mCallingUid = -1;
        this.mRcClient = null;
        int i = sLastRccId + 1;
        sLastRccId = i;
        this.mRccId = i;
        this.mPlaybackState = new RccPlaybackState(1, -1, 1.0f);
        resetPlaybackInfo();
        if (this.mToken != null) {
            try {
                this.mToken.linkToDeath(this, 0);
            } catch (RemoteException e) {
                sController.unregisterMediaButtonIntentAsync(this.mMediaIntent);
            }
        }
    }

    /* access modifiers changed from: protected */
    public int getRccId() {
        return this.mRccId;
    }

    /* access modifiers changed from: protected */
    public IRemoteControlClient getRcc() {
        return this.mRcClient;
    }

    /* access modifiers changed from: protected */
    public ComponentName getMediaButtonReceiver() {
        return this.mReceiverComponent;
    }

    /* access modifiers changed from: protected */
    public PendingIntent getMediaButtonIntent() {
        return this.mMediaIntent;
    }

    /* access modifiers changed from: protected */
    public boolean hasMatchingMediaButtonIntent(PendingIntent pi) {
        if (this.mToken != null) {
            return this.mMediaIntent.equals(pi);
        }
        if (this.mReceiverComponent != null) {
            return this.mReceiverComponent.equals(pi.getIntent().getComponent());
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isPlaybackActive() {
        return MediaFocusControl.isPlaystateActive(this.mPlaybackState.mState);
    }

    /* access modifiers changed from: protected */
    public void resetControllerInfoForRcc(IRemoteControlClient rcClient, String callingPackageName, int uid) {
        if (this.mRcClientDeathHandler != null) {
            unlinkToRcClientDeath();
        }
        this.mRcClient = rcClient;
        this.mCallingPackageName = callingPackageName;
        this.mCallingUid = uid;
        if (rcClient == null) {
            resetPlaybackInfo();
            return;
        }
        IBinder b = this.mRcClient.asBinder();
        RcClientDeathHandler rcdh = new RcClientDeathHandler(b, this.mMediaIntent);
        try {
            b.linkToDeath(rcdh, 0);
        } catch (RemoteException e) {
            Log.w(TAG, "registerRemoteControlClient() has a dead client " + b);
            this.mRcClient = null;
        }
        this.mRcClientDeathHandler = rcdh;
    }

    /* access modifiers changed from: protected */
    public void resetControllerInfoForNoRcc() {
        unlinkToRcClientDeath();
        this.mRcClient = null;
        this.mCallingPackageName = null;
    }

    public void resetPlaybackInfo() {
        this.mPlaybackType = 0;
        this.mPlaybackVolume = 15;
        this.mPlaybackVolumeMax = 15;
        this.mPlaybackVolumeHandling = 1;
        this.mPlaybackStream = 3;
        this.mPlaybackState.reset();
        this.mRemoteVolumeObs = null;
    }

    public void unlinkToRcClientDeath() {
        if (this.mRcClientDeathHandler != null && this.mRcClientDeathHandler.mCb != null) {
            try {
                this.mRcClientDeathHandler.mCb.unlinkToDeath(this.mRcClientDeathHandler, 0);
                this.mRcClientDeathHandler = null;
            } catch (NoSuchElementException e) {
                Log.e(TAG, "Error in unlinkToRcClientDeath()", e);
            }
        }
    }

    public void destroy() {
        unlinkToRcClientDeath();
        if (this.mToken != null) {
            this.mToken.unlinkToDeath(this, 0);
            this.mToken = null;
        }
    }

    @Override // android.os.IBinder.DeathRecipient
    public void binderDied() {
        sController.unregisterMediaButtonIntentAsync(this.mMediaIntent);
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        destroy();
        super.finalize();
    }
}

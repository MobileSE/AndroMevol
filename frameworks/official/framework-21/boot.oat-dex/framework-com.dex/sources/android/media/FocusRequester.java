package android.media;

import android.media.MediaFocusControl;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import java.io.PrintWriter;
import java.util.NoSuchElementException;

/* access modifiers changed from: package-private */
public class FocusRequester {
    private static final boolean DEBUG = false;
    private static final String TAG = "MediaFocusControl";
    private final int mCallingUid;
    private final String mClientId;
    private MediaFocusControl.AudioFocusDeathHandler mDeathHandler;
    private final IAudioFocusDispatcher mFocusDispatcher;
    private final int mFocusGainRequest;
    private int mFocusLossReceived = 0;
    private final String mPackageName;
    private final IBinder mSourceRef;
    private final int mStreamType;

    FocusRequester(int streamType, int focusRequest, IAudioFocusDispatcher afl, IBinder source, String id, MediaFocusControl.AudioFocusDeathHandler hdlr, String pn, int uid) {
        this.mStreamType = streamType;
        this.mFocusDispatcher = afl;
        this.mSourceRef = source;
        this.mClientId = id;
        this.mDeathHandler = hdlr;
        this.mPackageName = pn;
        this.mCallingUid = uid;
        this.mFocusGainRequest = focusRequest;
    }

    /* access modifiers changed from: package-private */
    public boolean hasSameClient(String otherClient) {
        try {
            return this.mClientId.compareTo(otherClient) == 0;
        } catch (NullPointerException e) {
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean hasSameBinder(IBinder ib) {
        return this.mSourceRef != null && this.mSourceRef.equals(ib);
    }

    /* access modifiers changed from: package-private */
    public boolean hasSamePackage(String pack) {
        try {
            return this.mPackageName.compareTo(pack) == 0;
        } catch (NullPointerException e) {
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean hasSameUid(int uid) {
        return this.mCallingUid == uid;
    }

    /* access modifiers changed from: package-private */
    public int getGainRequest() {
        return this.mFocusGainRequest;
    }

    /* access modifiers changed from: package-private */
    public int getStreamType() {
        return this.mStreamType;
    }

    private static String focusChangeToString(int focus) {
        switch (focus) {
            case -3:
                return "LOSS_TRANSIENT_CAN_DUCK";
            case -2:
                return "LOSS_TRANSIENT";
            case -1:
                return "LOSS";
            case 0:
                return "none";
            case 1:
                return "GAIN";
            case 2:
                return "GAIN_TRANSIENT";
            case 3:
                return "GAIN_TRANSIENT_MAY_DUCK";
            case 4:
                return "GAIN_TRANSIENT_EXCLUSIVE";
            default:
                return "[invalid focus change" + focus + "]";
        }
    }

    private String focusGainToString() {
        return focusChangeToString(this.mFocusGainRequest);
    }

    private String focusLossToString() {
        return focusChangeToString(this.mFocusLossReceived);
    }

    /* access modifiers changed from: package-private */
    public void dump(PrintWriter pw) {
        pw.println("  source:" + this.mSourceRef + " -- pack: " + this.mPackageName + " -- client: " + this.mClientId + " -- gain: " + focusGainToString() + " -- loss: " + focusLossToString() + " -- uid: " + this.mCallingUid + " -- stream: " + this.mStreamType);
    }

    /* access modifiers changed from: package-private */
    public void release() {
        try {
            if (this.mSourceRef != null && this.mDeathHandler != null) {
                this.mSourceRef.unlinkToDeath(this.mDeathHandler, 0);
                this.mDeathHandler = null;
            }
        } catch (NoSuchElementException e) {
            Log.e(TAG, "FocusRequester.release() hit ", e);
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        release();
        super.finalize();
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:0x0031  */
    /* JADX WARNING: Removed duplicated region for block: B:11:0x0033  */
    /* JADX WARNING: Removed duplicated region for block: B:14:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:15:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x002f A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int focusLossForGainRequest(int r4) {
        /*
        // Method dump skipped, instructions count: 102
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.FocusRequester.focusLossForGainRequest(int):int");
    }

    /* access modifiers changed from: package-private */
    public void handleExternalFocusGain(int focusGain) {
        handleFocusLoss(focusLossForGainRequest(focusGain));
    }

    /* access modifiers changed from: package-private */
    public void handleFocusGain(int focusGain) {
        try {
            if (this.mFocusDispatcher != null) {
                this.mFocusDispatcher.dispatchAudioFocusChange(focusGain, this.mClientId);
            }
            this.mFocusLossReceived = 0;
        } catch (RemoteException e) {
            Log.e(TAG, "Failure to signal gain of audio focus due to: ", e);
        }
    }

    /* access modifiers changed from: package-private */
    public void handleFocusLoss(int focusLoss) {
        try {
            if (focusLoss != this.mFocusLossReceived) {
                if (this.mFocusDispatcher != null) {
                    this.mFocusDispatcher.dispatchAudioFocusChange(focusLoss, this.mClientId);
                }
                this.mFocusLossReceived = focusLoss;
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Failure to signal loss of audio focus due to:", e);
        }
    }
}

package android.media;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.media.AudioService;
import android.media.PlayerRecord;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Slog;
import android.view.KeyEvent;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Stack;

public class MediaFocusControl implements PendingIntent.OnFinished {
    protected static final boolean DEBUG_RC = false;
    protected static final boolean DEBUG_VOL = false;
    private static final Uri ENABLED_NOTIFICATION_LISTENERS_URI = Settings.Secure.getUriFor(Settings.Secure.ENABLED_NOTIFICATION_LISTENERS);
    private static final String EXTRA_WAKELOCK_ACQUIRED = "android.media.AudioService.WAKELOCK_ACQUIRED";
    protected static final String IN_VOICE_COMM_FOCUS_ID = "AudioFocus_For_Phone_Ring_And_Calls";
    private static final int MSG_RCC_NEW_PLAYBACK_INFO = 4;
    private static final int MSG_RCC_NEW_PLAYBACK_STATE = 6;
    private static final int MSG_RCC_NEW_VOLUME_OBS = 5;
    private static final int MSG_RCC_SEEK_REQUEST = 7;
    private static final int MSG_RCC_UPDATE_METADATA = 8;
    private static final int MSG_RCDISPLAY_CLEAR = 1;
    private static final int MSG_RCDISPLAY_INIT_INFO = 9;
    private static final int MSG_RCDISPLAY_UPDATE = 2;
    private static final int MSG_REEVALUATE_RCD = 10;
    private static final int MSG_REEVALUATE_REMOTE = 3;
    private static final int MSG_UNREGISTER_MEDIABUTTONINTENT = 11;
    private static final int RCD_REG_FAILURE = 0;
    private static final int RCD_REG_SUCCESS_ENABLED_NOTIF = 2;
    private static final int RCD_REG_SUCCESS_PERMISSION = 1;
    private static final int RC_INFO_ALL = 15;
    private static final int RC_INFO_NONE = 0;
    private static final int SENDMSG_NOOP = 1;
    private static final int SENDMSG_QUEUE = 2;
    private static final int SENDMSG_REPLACE = 0;
    private static final String TAG = "MediaFocusControl";
    private static final int VOICEBUTTON_ACTION_DISCARD_CURRENT_KEY_PRESS = 1;
    private static final int VOICEBUTTON_ACTION_SIMULATE_KEY_PRESS = 3;
    private static final int VOICEBUTTON_ACTION_START_VOICE_INPUT = 2;
    private static final int WAKELOCK_RELEASE_ON_FINISHED = 1980;
    private static final Object mAudioFocusLock = new Object();
    private static final Object mRingingLock = new Object();
    private final AppOpsManager mAppOps;
    private final AudioService mAudioService;
    private final ContentResolver mContentResolver;
    private final Context mContext;
    private IRemoteControlClient mCurrentRcClient = null;
    private int mCurrentRcClientGen = 0;
    private PendingIntent mCurrentRcClientIntent = null;
    private final Object mCurrentRcLock = new Object();
    private final MediaEventHandler mEventHandler;
    private final Stack<FocusRequester> mFocusStack = new Stack<>();
    private boolean mHasRemotePlayback;
    private boolean mIsRinging = false;
    BroadcastReceiver mKeyEventDone = new BroadcastReceiver() {
        /* class android.media.MediaFocusControl.AnonymousClass2 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            Bundle extras;
            if (intent != null && (extras = intent.getExtras()) != null && extras.containsKey(MediaFocusControl.EXTRA_WAKELOCK_ACQUIRED)) {
                MediaFocusControl.this.mMediaEventWakeLock.release();
            }
        }
    };
    private final KeyguardManager mKeyguardManager;
    private PlayerRecord.RemotePlaybackState mMainRemote;
    private boolean mMainRemoteIsActive;
    private final PowerManager.WakeLock mMediaEventWakeLock;
    private ComponentName mMediaReceiverForCalls = null;
    private final NotificationListenerObserver mNotifListenerObserver;
    private final Stack<PlayerRecord> mPRStack = new Stack<>();
    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        /* class android.media.MediaFocusControl.AnonymousClass1 */

        @Override // android.telephony.PhoneStateListener
        public void onCallStateChanged(int state, String incomingNumber) {
            if (state == 1) {
                synchronized (MediaFocusControl.mRingingLock) {
                    MediaFocusControl.this.mIsRinging = true;
                }
            } else if (state == 2 || state == 0) {
                synchronized (MediaFocusControl.mRingingLock) {
                    MediaFocusControl.this.mIsRinging = false;
                }
            }
        }
    };
    private ArrayList<DisplayInfoForServer> mRcDisplays = new ArrayList<>(1);
    private boolean mVoiceButtonDown;
    private boolean mVoiceButtonHandled;
    private final Object mVoiceEventLock = new Object();
    private final AudioService.VolumeController mVolumeController;

    protected MediaFocusControl(Looper looper, Context cntxt, AudioService.VolumeController volumeCtrl, AudioService as) {
        this.mEventHandler = new MediaEventHandler(looper);
        this.mContext = cntxt;
        this.mContentResolver = this.mContext.getContentResolver();
        this.mVolumeController = volumeCtrl;
        this.mAudioService = as;
        this.mMediaEventWakeLock = ((PowerManager) this.mContext.getSystemService(Context.POWER_SERVICE)).newWakeLock(1, "handleMediaEvent");
        this.mMainRemote = new PlayerRecord.RemotePlaybackState(-1, AudioService.getMaxStreamVolume(3), AudioService.getMaxStreamVolume(3));
        ((TelephonyManager) this.mContext.getSystemService("phone")).listen(this.mPhoneStateListener, 32);
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService(Context.APP_OPS_SERVICE);
        this.mKeyguardManager = (KeyguardManager) this.mContext.getSystemService(Context.KEYGUARD_SERVICE);
        this.mNotifListenerObserver = new NotificationListenerObserver();
        this.mHasRemotePlayback = false;
        this.mMainRemoteIsActive = false;
        PlayerRecord.setMediaFocusControl(this);
        postReevaluateRemote();
    }

    /* access modifiers changed from: protected */
    public void dump(PrintWriter pw) {
        dumpFocusStack(pw);
        dumpRCStack(pw);
        dumpRCCStack(pw);
        dumpRCDList(pw);
    }

    private class NotificationListenerObserver extends ContentObserver {
        NotificationListenerObserver() {
            super(MediaFocusControl.this.mEventHandler);
            MediaFocusControl.this.mContentResolver.registerContentObserver(Settings.Secure.getUriFor(Settings.Secure.ENABLED_NOTIFICATION_LISTENERS), false, this);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri uri) {
            if (MediaFocusControl.ENABLED_NOTIFICATION_LISTENERS_URI.equals(uri) && !selfChange) {
                MediaFocusControl.this.postReevaluateRemoteControlDisplays();
            }
        }
    }

    private int checkRcdRegistrationAuthorization(ComponentName listenerComp) {
        String[] components;
        if (this.mContext.checkCallingOrSelfPermission(Manifest.permission.MEDIA_CONTENT_CONTROL) == 0) {
            return 1;
        }
        if (listenerComp != null) {
            long ident = Binder.clearCallingIdentity();
            try {
                String enabledNotifListeners = Settings.Secure.getStringForUser(this.mContext.getContentResolver(), Settings.Secure.ENABLED_NOTIFICATION_LISTENERS, ActivityManager.getCurrentUser());
                if (enabledNotifListeners != null) {
                    for (String str : enabledNotifListeners.split(":")) {
                        ComponentName component = ComponentName.unflattenFromString(str);
                        if (component != null && listenerComp.equals(component)) {
                            return 2;
                        }
                    }
                }
                Binder.restoreCallingIdentity(ident);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
        return 0;
    }

    /* access modifiers changed from: protected */
    public boolean registerRemoteController(IRemoteControlDisplay rcd, int w, int h, ComponentName listenerComp) {
        if (checkRcdRegistrationAuthorization(listenerComp) != 0) {
            registerRemoteControlDisplay_int(rcd, w, h, listenerComp);
            return true;
        }
        Slog.w(TAG, "Access denied to process: " + Binder.getCallingPid() + ", must have permission " + Manifest.permission.MEDIA_CONTENT_CONTROL + " or be an enabled NotificationListenerService for registerRemoteController");
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean registerRemoteControlDisplay(IRemoteControlDisplay rcd, int w, int h) {
        if (checkRcdRegistrationAuthorization(null) != 0) {
            registerRemoteControlDisplay_int(rcd, w, h, null);
            return true;
        }
        Slog.w(TAG, "Access denied to process: " + Binder.getCallingPid() + ", must have permission " + Manifest.permission.MEDIA_CONTENT_CONTROL + " to register IRemoteControlDisplay");
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void postReevaluateRemoteControlDisplays() {
        sendMsg(this.mEventHandler, 10, 2, 0, 0, null, 0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onReevaluateRemoteControlDisplays() {
        String[] enabledComponents;
        String enabledNotifListeners = Settings.Secure.getStringForUser(this.mContext.getContentResolver(), Settings.Secure.ENABLED_NOTIFICATION_LISTENERS, ActivityManager.getCurrentUser());
        synchronized (mAudioFocusLock) {
            synchronized (this.mPRStack) {
                if (enabledNotifListeners == null) {
                    enabledComponents = null;
                } else {
                    enabledComponents = enabledNotifListeners.split(":");
                }
                Iterator<DisplayInfoForServer> displayIterator = this.mRcDisplays.iterator();
                while (displayIterator.hasNext()) {
                    DisplayInfoForServer di = displayIterator.next();
                    if (di.mClientNotifListComp != null) {
                        boolean wasEnabled = di.mEnabled;
                        di.mEnabled = isComponentInStringArray(di.mClientNotifListComp, enabledComponents);
                        if (wasEnabled != di.mEnabled) {
                            try {
                                di.mRcDisplay.setEnabled(di.mEnabled);
                                enableRemoteControlDisplayForClient_syncRcStack(di.mRcDisplay, di.mEnabled);
                                if (di.mEnabled) {
                                    sendMsg(this.mEventHandler, 9, 2, di.mArtworkExpectedWidth, di.mArtworkExpectedHeight, di.mRcDisplay, 0);
                                }
                            } catch (RemoteException e) {
                                Log.e(TAG, "Error en/disabling RCD: ", e);
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isComponentInStringArray(ComponentName comp, String[] enabledArray) {
        if (enabledArray == null || enabledArray.length == 0) {
            return false;
        }
        String compString = comp.flattenToString();
        for (String str : enabledArray) {
            if (compString.equals(str)) {
                return true;
            }
        }
        return false;
    }

    private static void sendMsg(Handler handler, int msg, int existingMsgPolicy, int arg1, int arg2, Object obj, int delay) {
        if (existingMsgPolicy == 0) {
            handler.removeMessages(msg);
        } else if (existingMsgPolicy == 1 && handler.hasMessages(msg)) {
            return;
        }
        handler.sendMessageDelayed(handler.obtainMessage(msg, arg1, arg2, obj), (long) delay);
    }

    /* access modifiers changed from: private */
    public class MediaEventHandler extends Handler {
        MediaEventHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    MediaFocusControl.this.onRcDisplayClear();
                    return;
                case 2:
                    MediaFocusControl.this.onRcDisplayUpdate((PlayerRecord) msg.obj, msg.arg1);
                    return;
                case 3:
                    MediaFocusControl.this.onReevaluateRemote();
                    return;
                case 4:
                case 6:
                case 7:
                case 8:
                default:
                    return;
                case 5:
                    MediaFocusControl.this.onRegisterVolumeObserverForRcc(msg.arg1, (IRemoteVolumeObserver) msg.obj);
                    return;
                case 9:
                    MediaFocusControl.this.onRcDisplayInitInfo((IRemoteControlDisplay) msg.obj, msg.arg1, msg.arg2);
                    return;
                case 10:
                    MediaFocusControl.this.onReevaluateRemoteControlDisplays();
                    return;
                case 11:
                    MediaFocusControl.this.unregisterMediaButtonIntent((PendingIntent) msg.obj);
                    return;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void discardAudioFocusOwner() {
        synchronized (mAudioFocusLock) {
            if (!this.mFocusStack.empty()) {
                FocusRequester exFocusOwner = this.mFocusStack.pop();
                exFocusOwner.handleFocusLoss(-1);
                exFocusOwner.release();
            }
        }
    }

    private void notifyTopOfAudioFocusStack() {
        if (!this.mFocusStack.empty() && canReassignAudioFocus()) {
            this.mFocusStack.peek().handleFocusGain(1);
        }
    }

    private void propagateFocusLossFromGain_syncAf(int focusGain) {
        Iterator<FocusRequester> stackIterator = this.mFocusStack.iterator();
        while (stackIterator.hasNext()) {
            stackIterator.next().handleExternalFocusGain(focusGain);
        }
    }

    private void dumpFocusStack(PrintWriter pw) {
        pw.println("\nAudio Focus stack entries (last is top of stack):");
        synchronized (mAudioFocusLock) {
            Iterator<FocusRequester> stackIterator = this.mFocusStack.iterator();
            while (stackIterator.hasNext()) {
                stackIterator.next().dump(pw);
            }
        }
    }

    private void removeFocusStackEntry(String clientToRemove, boolean signal) {
        if (this.mFocusStack.empty() || !this.mFocusStack.peek().hasSameClient(clientToRemove)) {
            Iterator<FocusRequester> stackIterator = this.mFocusStack.iterator();
            while (stackIterator.hasNext()) {
                FocusRequester fr = stackIterator.next();
                if (fr.hasSameClient(clientToRemove)) {
                    Log.i(TAG, "AudioFocus  removeFocusStackEntry(): removing entry for " + clientToRemove);
                    stackIterator.remove();
                    fr.release();
                }
            }
            return;
        }
        this.mFocusStack.pop().release();
        if (signal) {
            notifyTopOfAudioFocusStack();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeFocusStackEntryForClient(IBinder cb) {
        boolean isTopOfStackForClientToRemove = !this.mFocusStack.isEmpty() && this.mFocusStack.peek().hasSameBinder(cb);
        Iterator<FocusRequester> stackIterator = this.mFocusStack.iterator();
        while (stackIterator.hasNext()) {
            if (stackIterator.next().hasSameBinder(cb)) {
                Log.i(TAG, "AudioFocus  removeFocusStackEntry(): removing entry for " + cb);
                stackIterator.remove();
            }
        }
        if (isTopOfStackForClientToRemove) {
            notifyTopOfAudioFocusStack();
        }
    }

    private boolean canReassignAudioFocus() {
        if (this.mFocusStack.isEmpty() || !this.mFocusStack.peek().hasSameClient(IN_VOICE_COMM_FOCUS_ID)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public class AudioFocusDeathHandler implements IBinder.DeathRecipient {
        private IBinder mCb;

        AudioFocusDeathHandler(IBinder cb) {
            this.mCb = cb;
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            synchronized (MediaFocusControl.mAudioFocusLock) {
                Log.w(MediaFocusControl.TAG, "  AudioFocus   audio focus client died");
                MediaFocusControl.this.removeFocusStackEntryForClient(this.mCb);
            }
        }

        public IBinder getBinder() {
            return this.mCb;
        }
    }

    /* access modifiers changed from: protected */
    public int getCurrentAudioFocus() {
        int gainRequest;
        synchronized (mAudioFocusLock) {
            if (this.mFocusStack.empty()) {
                gainRequest = 0;
            } else {
                gainRequest = this.mFocusStack.peek().getGainRequest();
            }
        }
        return gainRequest;
    }

    /* access modifiers changed from: protected */
    public int requestAudioFocus(int mainStreamType, int focusChangeHint, IBinder cb, IAudioFocusDispatcher fd, String clientId, String callingPackageName) {
        Log.i(TAG, " AudioFocus  requestAudioFocus() from " + clientId);
        if (!cb.pingBinder()) {
            Log.e(TAG, " AudioFocus DOA client for requestAudioFocus(), aborting.");
            return 0;
        } else if (this.mAppOps.noteOp(32, Binder.getCallingUid(), callingPackageName) != 0) {
            return 0;
        } else {
            synchronized (mAudioFocusLock) {
                if (!canReassignAudioFocus()) {
                    return 0;
                }
                AudioFocusDeathHandler afdh = new AudioFocusDeathHandler(cb);
                try {
                    cb.linkToDeath(afdh, 0);
                    if (!this.mFocusStack.empty() && this.mFocusStack.peek().hasSameClient(clientId)) {
                        if (this.mFocusStack.peek().getGainRequest() == focusChangeHint) {
                            cb.unlinkToDeath(afdh, 0);
                            return 1;
                        }
                        this.mFocusStack.pop().release();
                    }
                    removeFocusStackEntry(clientId, false);
                    if (!this.mFocusStack.empty()) {
                        propagateFocusLossFromGain_syncAf(focusChangeHint);
                    }
                    this.mFocusStack.push(new FocusRequester(mainStreamType, focusChangeHint, fd, cb, clientId, afdh, callingPackageName, Binder.getCallingUid()));
                    return 1;
                } catch (RemoteException e) {
                    Log.w(TAG, "AudioFocus  requestAudioFocus() could not link to " + cb + " binder death");
                    return 0;
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public int abandonAudioFocus(IAudioFocusDispatcher fl, String clientId) {
        Log.i(TAG, " AudioFocus  abandonAudioFocus() from " + clientId);
        try {
            synchronized (mAudioFocusLock) {
                removeFocusStackEntry(clientId, true);
            }
        } catch (ConcurrentModificationException cme) {
            Log.e(TAG, "FATAL EXCEPTION AudioFocus  abandonAudioFocus() caused " + cme);
            cme.printStackTrace();
        }
        return 1;
    }

    /* access modifiers changed from: protected */
    public void unregisterAudioFocusClient(String clientId) {
        synchronized (mAudioFocusLock) {
            removeFocusStackEntry(clientId, false);
        }
    }

    /* access modifiers changed from: protected */
    public void dispatchMediaKeyEvent(KeyEvent keyEvent) {
        filterMediaKeyEvent(keyEvent, false);
    }

    /* access modifiers changed from: protected */
    public void dispatchMediaKeyEventUnderWakelock(KeyEvent keyEvent) {
        filterMediaKeyEvent(keyEvent, true);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:24:0x004a, code lost:
        if (isValidVoiceInputKeyCode(r5.getKeyCode()) == false) goto L_0x0053;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x004c, code lost:
        filterVoiceInputKeyEvent(r5, r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0053, code lost:
        dispatchMediaKeyEvent(r5, r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:?, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void filterMediaKeyEvent(android.view.KeyEvent r5, boolean r6) {
        /*
            r4 = this;
            boolean r0 = isValidMediaKeyEvent(r5)
            if (r0 != 0) goto L_0x0020
            java.lang.String r0 = "MediaFocusControl"
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "not dispatching invalid media key event "
            java.lang.StringBuilder r1 = r1.append(r2)
            java.lang.StringBuilder r1 = r1.append(r5)
            java.lang.String r1 = r1.toString()
            android.util.Log.e(r0, r1)
        L_0x001f:
            return
        L_0x0020:
            java.lang.Object r1 = android.media.MediaFocusControl.mRingingLock
            monitor-enter(r1)
            java.util.Stack<android.media.PlayerRecord> r2 = r4.mPRStack     // Catch:{ all -> 0x003d }
            monitor-enter(r2)     // Catch:{ all -> 0x003d }
            android.content.ComponentName r0 = r4.mMediaReceiverForCalls     // Catch:{ all -> 0x0050 }
            if (r0 == 0) goto L_0x0040
            boolean r0 = r4.mIsRinging     // Catch:{ all -> 0x0050 }
            if (r0 != 0) goto L_0x0037
            android.media.AudioService r0 = r4.mAudioService     // Catch:{ all -> 0x0050 }
            int r0 = r0.getMode()     // Catch:{ all -> 0x0050 }
            r3 = 2
            if (r0 != r3) goto L_0x0040
        L_0x0037:
            r4.dispatchMediaKeyEventForCalls(r5, r6)     // Catch:{ all -> 0x0050 }
            monitor-exit(r2)     // Catch:{ all -> 0x0050 }
            monitor-exit(r1)
            goto L_0x001f
        L_0x003d:
            r0 = move-exception
            monitor-exit(r1)
            throw r0
        L_0x0040:
            monitor-exit(r2)
            monitor-exit(r1)
            int r0 = r5.getKeyCode()
            boolean r0 = isValidVoiceInputKeyCode(r0)
            if (r0 == 0) goto L_0x0053
            r4.filterVoiceInputKeyEvent(r5, r6)
            goto L_0x001f
        L_0x0050:
            r0 = move-exception
            monitor-exit(r2)
            throw r0
        L_0x0053:
            r4.dispatchMediaKeyEvent(r5, r6)
            goto L_0x001f
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.MediaFocusControl.filterMediaKeyEvent(android.view.KeyEvent, boolean):void");
    }

    private void dispatchMediaKeyEventForCalls(KeyEvent keyEvent, boolean needWakeLock) {
        Intent keyIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, (Uri) null);
        keyIntent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
        keyIntent.setPackage(this.mMediaReceiverForCalls.getPackageName());
        if (needWakeLock) {
            this.mMediaEventWakeLock.acquire();
            keyIntent.putExtra(EXTRA_WAKELOCK_ACQUIRED, WAKELOCK_RELEASE_ON_FINISHED);
        }
        long ident = Binder.clearCallingIdentity();
        try {
            this.mContext.sendOrderedBroadcastAsUser(keyIntent, UserHandle.ALL, null, this.mKeyEventDone, this.mEventHandler, -1, null, null);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private void dispatchMediaKeyEvent(KeyEvent keyEvent, boolean needWakeLock) {
        int i;
        if (needWakeLock) {
            this.mMediaEventWakeLock.acquire();
        }
        Intent keyIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, (Uri) null);
        keyIntent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
        synchronized (this.mPRStack) {
            if (!this.mPRStack.empty()) {
                try {
                    PendingIntent mediaButtonIntent = this.mPRStack.peek().getMediaButtonIntent();
                    Context context = this.mContext;
                    if (needWakeLock) {
                        i = WAKELOCK_RELEASE_ON_FINISHED;
                    } else {
                        i = 0;
                    }
                    mediaButtonIntent.send(context, i, keyIntent, this, this.mEventHandler);
                } catch (PendingIntent.CanceledException e) {
                    Log.e(TAG, "Error sending pending intent " + this.mPRStack.peek());
                    e.printStackTrace();
                }
            } else {
                if (needWakeLock) {
                    keyIntent.putExtra(EXTRA_WAKELOCK_ACQUIRED, WAKELOCK_RELEASE_ON_FINISHED);
                }
                long ident = Binder.clearCallingIdentity();
                try {
                    this.mContext.sendOrderedBroadcastAsUser(keyIntent, UserHandle.ALL, null, this.mKeyEventDone, this.mEventHandler, -1, null, null);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            }
        }
    }

    private void filterVoiceInputKeyEvent(KeyEvent keyEvent, boolean needWakeLock) {
        int voiceButtonAction = 1;
        int keyAction = keyEvent.getAction();
        synchronized (this.mVoiceEventLock) {
            if (keyAction == 0) {
                if (keyEvent.getRepeatCount() == 0) {
                    this.mVoiceButtonDown = true;
                    this.mVoiceButtonHandled = false;
                } else if (this.mVoiceButtonDown && !this.mVoiceButtonHandled && (keyEvent.getFlags() & 128) != 0) {
                    this.mVoiceButtonHandled = true;
                    voiceButtonAction = 2;
                }
            } else if (keyAction == 1 && this.mVoiceButtonDown) {
                this.mVoiceButtonDown = false;
                if (!this.mVoiceButtonHandled && !keyEvent.isCanceled()) {
                    voiceButtonAction = 3;
                }
            }
        }
        switch (voiceButtonAction) {
            case 1:
            default:
                return;
            case 2:
                startVoiceBasedInteractions(needWakeLock);
                return;
            case 3:
                sendSimulatedMediaButtonEvent(keyEvent, needWakeLock);
                return;
        }
    }

    private void sendSimulatedMediaButtonEvent(KeyEvent originalKeyEvent, boolean needWakeLock) {
        dispatchMediaKeyEvent(KeyEvent.changeAction(originalKeyEvent, 0), needWakeLock);
        dispatchMediaKeyEvent(KeyEvent.changeAction(originalKeyEvent, 1), needWakeLock);
    }

    private static boolean isValidMediaKeyEvent(KeyEvent keyEvent) {
        if (keyEvent == null) {
            return false;
        }
        return KeyEvent.isMediaKey(keyEvent.getKeyCode());
    }

    private static boolean isValidVoiceInputKeyCode(int keyCode) {
        if (keyCode == 79) {
            return true;
        }
        return false;
    }

    private void startVoiceBasedInteractions(boolean needWakeLock) {
        boolean isLocked;
        Intent voiceIntent;
        boolean z = true;
        PowerManager pm = (PowerManager) this.mContext.getSystemService(Context.POWER_SERVICE);
        if (this.mKeyguardManager == null || !this.mKeyguardManager.isKeyguardLocked()) {
            isLocked = false;
        } else {
            isLocked = true;
        }
        if (isLocked || !pm.isScreenOn()) {
            voiceIntent = new Intent(RecognizerIntent.ACTION_VOICE_SEARCH_HANDS_FREE);
            if (!isLocked || !this.mKeyguardManager.isKeyguardSecure()) {
                z = false;
            }
            voiceIntent.putExtra(RecognizerIntent.EXTRA_SECURE, z);
            Log.i(TAG, "voice-based interactions: about to use ACTION_VOICE_SEARCH_HANDS_FREE");
        } else {
            voiceIntent = new Intent(RecognizerIntent.ACTION_WEB_SEARCH);
            Log.i(TAG, "voice-based interactions: about to use ACTION_WEB_SEARCH");
        }
        if (needWakeLock) {
            this.mMediaEventWakeLock.acquire();
        }
        long identity = Binder.clearCallingIdentity();
        if (voiceIntent != null) {
            try {
                voiceIntent.setFlags(276824064);
                this.mContext.startActivityAsUser(voiceIntent, UserHandle.CURRENT);
            } catch (ActivityNotFoundException e) {
                Log.w(TAG, "No activity for search: " + e);
                Binder.restoreCallingIdentity(identity);
                if (needWakeLock) {
                    this.mMediaEventWakeLock.release();
                    return;
                }
                return;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                if (needWakeLock) {
                    this.mMediaEventWakeLock.release();
                }
                throw th;
            }
        }
        Binder.restoreCallingIdentity(identity);
        if (needWakeLock) {
            this.mMediaEventWakeLock.release();
        }
    }

    @Override // android.app.PendingIntent.OnFinished
    public void onSendFinished(PendingIntent pendingIntent, Intent intent, int resultCode, String resultData, Bundle resultExtras) {
        if (resultCode == WAKELOCK_RELEASE_ON_FINISHED) {
            this.mMediaEventWakeLock.release();
        }
    }

    private void dumpRCStack(PrintWriter pw) {
        pw.println("\nRemote Control stack entries (last is top of stack):");
        synchronized (this.mPRStack) {
            Iterator<PlayerRecord> stackIterator = this.mPRStack.iterator();
            while (stackIterator.hasNext()) {
                stackIterator.next().dump(pw, true);
            }
        }
    }

    private void dumpRCCStack(PrintWriter pw) {
        pw.println("\nRemote Control Client stack entries (last is top of stack):");
        synchronized (this.mPRStack) {
            Iterator<PlayerRecord> stackIterator = this.mPRStack.iterator();
            while (stackIterator.hasNext()) {
                stackIterator.next().dump(pw, false);
            }
            synchronized (this.mCurrentRcLock) {
                pw.println("\nCurrent remote control generation ID = " + this.mCurrentRcClientGen);
            }
        }
        synchronized (this.mMainRemote) {
            pw.println("\nRemote Volume State:");
            pw.println("  has remote: " + this.mHasRemotePlayback);
            pw.println("  is remote active: " + this.mMainRemoteIsActive);
            pw.println("  rccId: " + this.mMainRemote.mRccId);
            pw.println("  volume handling: " + (this.mMainRemote.mVolumeHandling == 0 ? "PLAYBACK_VOLUME_FIXED(0)" : "PLAYBACK_VOLUME_VARIABLE(1)"));
            pw.println("  volume: " + this.mMainRemote.mVolume);
            pw.println("  volume steps: " + this.mMainRemote.mVolumeMax);
        }
    }

    private void dumpRCDList(PrintWriter pw) {
        pw.println("\nRemote Control Display list entries:");
        synchronized (this.mPRStack) {
            Iterator<DisplayInfoForServer> displayIterator = this.mRcDisplays.iterator();
            while (displayIterator.hasNext()) {
                DisplayInfoForServer di = displayIterator.next();
                pw.println("  IRCD: " + di.mRcDisplay + "  -- w:" + di.mArtworkExpectedWidth + "  -- h:" + di.mArtworkExpectedHeight + "  -- wantsPosSync:" + di.mWantsPositionSync + "  -- " + (di.mEnabled ? "enabled" : "disabled"));
            }
        }
    }

    private boolean pushMediaButtonReceiver_syncPrs(PendingIntent mediaIntent, ComponentName target, IBinder token) {
        ArrayIndexOutOfBoundsException e;
        if (this.mPRStack.empty()) {
            this.mPRStack.push(new PlayerRecord(mediaIntent, target, token));
            return true;
        } else if (this.mPRStack.peek().hasMatchingMediaButtonIntent(mediaIntent) || this.mAppOps.noteOp(31, Binder.getCallingUid(), mediaIntent.getCreatorPackage()) != 0) {
            return false;
        } else {
            this.mPRStack.lastElement();
            int lastPlayingIndex = this.mPRStack.size();
            int inStackIndex = -1;
            try {
                int index = this.mPRStack.size() - 1;
                PlayerRecord prse = null;
                while (index >= 0) {
                    try {
                        PlayerRecord prse2 = this.mPRStack.elementAt(index);
                        if (prse2.isPlaybackActive()) {
                            lastPlayingIndex = index;
                        }
                        if (prse2.hasMatchingMediaButtonIntent(mediaIntent)) {
                            inStackIndex = index;
                        }
                        index--;
                        prse = prse2;
                    } catch (ArrayIndexOutOfBoundsException e2) {
                        e = e2;
                        Log.e(TAG, "Wrong index (inStack=" + inStackIndex + " lastPlaying=" + lastPlayingIndex + " size=" + this.mPRStack.size() + " accessing media button stack", e);
                        return false;
                    }
                }
                if (inStackIndex == -1) {
                    this.mPRStack.add(lastPlayingIndex, new PlayerRecord(mediaIntent, target, token));
                    return false;
                } else if (this.mPRStack.size() <= 1) {
                    return false;
                } else {
                    PlayerRecord prse3 = this.mPRStack.elementAt(inStackIndex);
                    this.mPRStack.removeElementAt(inStackIndex);
                    if (prse3.isPlaybackActive()) {
                        this.mPRStack.push(prse3);
                        return false;
                    } else if (inStackIndex > lastPlayingIndex) {
                        this.mPRStack.add(lastPlayingIndex, prse3);
                        return false;
                    } else {
                        this.mPRStack.add(lastPlayingIndex - 1, prse3);
                        return false;
                    }
                }
            } catch (ArrayIndexOutOfBoundsException e3) {
                e = e3;
                Log.e(TAG, "Wrong index (inStack=" + inStackIndex + " lastPlaying=" + lastPlayingIndex + " size=" + this.mPRStack.size() + " accessing media button stack", e);
                return false;
            }
        }
    }

    private void removeMediaButtonReceiver_syncPrs(PendingIntent pi) {
        try {
            for (int index = this.mPRStack.size() - 1; index >= 0; index--) {
                PlayerRecord prse = this.mPRStack.elementAt(index);
                if (prse.hasMatchingMediaButtonIntent(pi)) {
                    prse.destroy();
                    this.mPRStack.removeElementAt(index);
                    return;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.e(TAG, "Wrong index accessing media button stack, lock error? ", e);
        }
    }

    private boolean isCurrentRcController(PendingIntent pi) {
        if (this.mPRStack.empty() || !this.mPRStack.peek().hasMatchingMediaButtonIntent(pi)) {
            return false;
        }
        return true;
    }

    private void setNewRcClientOnDisplays_syncRcsCurrc(int newClientGeneration, PendingIntent newMediaIntent, boolean clearing) {
        synchronized (this.mPRStack) {
            if (this.mRcDisplays.size() > 0) {
                Iterator<DisplayInfoForServer> displayIterator = this.mRcDisplays.iterator();
                while (displayIterator.hasNext()) {
                    DisplayInfoForServer di = displayIterator.next();
                    try {
                        di.mRcDisplay.setCurrentClientId(newClientGeneration, newMediaIntent, clearing);
                    } catch (RemoteException e) {
                        Log.e(TAG, "Dead display in setNewRcClientOnDisplays_syncRcsCurrc()", e);
                        di.release();
                        displayIterator.remove();
                    }
                }
            }
        }
    }

    private void setNewRcClientGenerationOnClients_syncRcsCurrc(int newClientGeneration) {
        Iterator<PlayerRecord> stackIterator = this.mPRStack.iterator();
        while (stackIterator.hasNext()) {
            PlayerRecord se = stackIterator.next();
            if (!(se == null || se.getRcc() == null)) {
                try {
                    se.getRcc().setCurrentClientGenerationId(newClientGeneration);
                } catch (RemoteException e) {
                    Log.w(TAG, "Dead client in setNewRcClientGenerationOnClients_syncRcsCurrc()", e);
                    stackIterator.remove();
                    se.unlinkToRcClientDeath();
                }
            }
        }
    }

    private void setNewRcClient_syncRcsCurrc(int newClientGeneration, PendingIntent newMediaIntent, boolean clearing) {
        setNewRcClientOnDisplays_syncRcsCurrc(newClientGeneration, newMediaIntent, clearing);
        setNewRcClientGenerationOnClients_syncRcsCurrc(newClientGeneration);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onRcDisplayClear() {
        synchronized (this.mPRStack) {
            synchronized (this.mCurrentRcLock) {
                this.mCurrentRcClientGen++;
                setNewRcClient_syncRcsCurrc(this.mCurrentRcClientGen, null, true);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onRcDisplayUpdate(PlayerRecord prse, int flags) {
        synchronized (this.mPRStack) {
            synchronized (this.mCurrentRcLock) {
                if (this.mCurrentRcClient != null && this.mCurrentRcClient.equals(prse.getRcc())) {
                    this.mCurrentRcClientGen++;
                    setNewRcClient_syncRcsCurrc(this.mCurrentRcClientGen, prse.getMediaButtonIntent(), false);
                    try {
                        this.mCurrentRcClient.onInformationRequested(this.mCurrentRcClientGen, flags);
                    } catch (RemoteException e) {
                        Log.e(TAG, "Current valid remote client is dead: " + e);
                        this.mCurrentRcClient = null;
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onRcDisplayInitInfo(IRemoteControlDisplay newRcd, int w, int h) {
        synchronized (this.mPRStack) {
            synchronized (this.mCurrentRcLock) {
                if (this.mCurrentRcClient != null) {
                    try {
                        newRcd.setCurrentClientId(this.mCurrentRcClientGen, this.mCurrentRcClientIntent, false);
                        try {
                            this.mCurrentRcClient.informationRequestForDisplay(newRcd, w, h);
                        } catch (RemoteException e) {
                            Log.e(TAG, "Current valid remote client is dead: ", e);
                            this.mCurrentRcClient = null;
                        }
                    } catch (RemoteException e2) {
                        Log.e(TAG, "Dead display in onRcDisplayInitInfo()", e2);
                    }
                }
            }
        }
    }

    private void clearRemoteControlDisplay_syncPrs() {
        synchronized (this.mCurrentRcLock) {
            this.mCurrentRcClient = null;
        }
        this.mEventHandler.sendMessage(this.mEventHandler.obtainMessage(1));
    }

    private void updateRemoteControlDisplay_syncPrs(int infoChangedFlags) {
        PlayerRecord prse = this.mPRStack.peek();
        int infoFlagsAboutToBeUsed = infoChangedFlags;
        if (prse.getRcc() == null) {
            clearRemoteControlDisplay_syncPrs();
            return;
        }
        synchronized (this.mCurrentRcLock) {
            if (!prse.getRcc().equals(this.mCurrentRcClient)) {
                infoFlagsAboutToBeUsed = 15;
            }
            this.mCurrentRcClient = prse.getRcc();
            this.mCurrentRcClientIntent = prse.getMediaButtonIntent();
        }
        this.mEventHandler.sendMessage(this.mEventHandler.obtainMessage(2, infoFlagsAboutToBeUsed, 0, prse));
    }

    private void checkUpdateRemoteControlDisplay_syncPrs(int infoChangedFlags) {
        if (this.mPRStack.isEmpty()) {
            clearRemoteControlDisplay_syncPrs();
        } else {
            updateRemoteControlDisplay_syncPrs(infoChangedFlags);
        }
    }

    /* access modifiers changed from: protected */
    public void registerMediaButtonIntent(PendingIntent mediaIntent, ComponentName eventReceiver, IBinder token) {
        Log.i(TAG, "  Remote Control   registerMediaButtonIntent() for " + mediaIntent);
        synchronized (this.mPRStack) {
            if (pushMediaButtonReceiver_syncPrs(mediaIntent, eventReceiver, token)) {
                checkUpdateRemoteControlDisplay_syncPrs(15);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void unregisterMediaButtonIntent(PendingIntent mediaIntent) {
        Log.i(TAG, "  Remote Control   unregisterMediaButtonIntent() for " + mediaIntent);
        synchronized (this.mPRStack) {
            boolean topOfStackWillChange = isCurrentRcController(mediaIntent);
            removeMediaButtonReceiver_syncPrs(mediaIntent);
            if (topOfStackWillChange) {
                checkUpdateRemoteControlDisplay_syncPrs(15);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void unregisterMediaButtonIntentAsync(PendingIntent mediaIntent) {
        this.mEventHandler.sendMessage(this.mEventHandler.obtainMessage(11, 0, 0, mediaIntent));
    }

    /* access modifiers changed from: protected */
    public void registerMediaButtonEventReceiverForCalls(ComponentName c) {
        if (this.mContext.checkCallingPermission(Manifest.permission.MODIFY_PHONE_STATE) != 0) {
            Log.e(TAG, "Invalid permissions to register media button receiver for calls");
            return;
        }
        synchronized (this.mPRStack) {
            this.mMediaReceiverForCalls = c;
        }
    }

    /* access modifiers changed from: protected */
    public void unregisterMediaButtonEventReceiverForCalls() {
        if (this.mContext.checkCallingPermission(Manifest.permission.MODIFY_PHONE_STATE) != 0) {
            Log.e(TAG, "Invalid permissions to unregister media button receiver for calls");
            return;
        }
        synchronized (this.mPRStack) {
            this.mMediaReceiverForCalls = null;
        }
    }

    /* access modifiers changed from: protected */
    public int registerRemoteControlClient(PendingIntent mediaIntent, IRemoteControlClient rcClient, String callingPackageName) {
        int rccId = -1;
        synchronized (this.mPRStack) {
            try {
                int index = this.mPRStack.size() - 1;
                while (true) {
                    if (index >= 0) {
                        PlayerRecord prse = this.mPRStack.elementAt(index);
                        if (prse.hasMatchingMediaButtonIntent(mediaIntent)) {
                            prse.resetControllerInfoForRcc(rcClient, callingPackageName, Binder.getCallingUid());
                            if (rcClient != null) {
                                rccId = prse.getRccId();
                                if (this.mRcDisplays.size() > 0) {
                                    plugRemoteControlDisplaysIntoClient_syncPrs(prse.getRcc());
                                }
                            }
                        } else {
                            index--;
                        }
                    }
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                Log.e(TAG, "Wrong index accessing RC stack, lock error? ", e);
            }
            if (isCurrentRcController(mediaIntent)) {
                checkUpdateRemoteControlDisplay_syncPrs(15);
            }
        }
        return rccId;
    }

    /* access modifiers changed from: protected */
    public void unregisterRemoteControlClient(PendingIntent mediaIntent, IRemoteControlClient rcClient) {
        synchronized (this.mPRStack) {
            boolean topRccChange = false;
            try {
                int index = this.mPRStack.size() - 1;
                while (true) {
                    if (index < 0) {
                        break;
                    }
                    PlayerRecord prse = this.mPRStack.elementAt(index);
                    if (!prse.hasMatchingMediaButtonIntent(mediaIntent) || !rcClient.equals(prse.getRcc())) {
                        index--;
                    } else {
                        prse.resetControllerInfoForNoRcc();
                        topRccChange = index == this.mPRStack.size() + -1;
                    }
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                Log.e(TAG, "Wrong index accessing RC stack, lock error? ", e);
            }
            if (topRccChange) {
                checkUpdateRemoteControlDisplay_syncPrs(15);
            }
        }
    }

    /* access modifiers changed from: private */
    public class DisplayInfoForServer implements IBinder.DeathRecipient {
        private int mArtworkExpectedHeight = -1;
        private int mArtworkExpectedWidth = -1;
        private ComponentName mClientNotifListComp;
        private boolean mEnabled = true;
        private final IRemoteControlDisplay mRcDisplay;
        private final IBinder mRcDisplayBinder;
        private boolean mWantsPositionSync = false;

        public DisplayInfoForServer(IRemoteControlDisplay rcd, int w, int h) {
            this.mRcDisplay = rcd;
            this.mRcDisplayBinder = rcd.asBinder();
            this.mArtworkExpectedWidth = w;
            this.mArtworkExpectedHeight = h;
        }

        public boolean init() {
            try {
                this.mRcDisplayBinder.linkToDeath(this, 0);
                return true;
            } catch (RemoteException e) {
                Log.w(MediaFocusControl.TAG, "registerRemoteControlDisplay() has a dead client " + this.mRcDisplayBinder);
                return false;
            }
        }

        public void release() {
            try {
                this.mRcDisplayBinder.unlinkToDeath(this, 0);
            } catch (NoSuchElementException e) {
                Log.e(MediaFocusControl.TAG, "Error in DisplaInfoForServer.relase()", e);
            }
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            synchronized (MediaFocusControl.this.mPRStack) {
                Log.w(MediaFocusControl.TAG, "RemoteControl: display " + this.mRcDisplay + " died");
                Iterator<DisplayInfoForServer> displayIterator = MediaFocusControl.this.mRcDisplays.iterator();
                while (displayIterator.hasNext()) {
                    if (displayIterator.next().mRcDisplay == this.mRcDisplay) {
                        displayIterator.remove();
                        return;
                    }
                }
            }
        }
    }

    private void plugRemoteControlDisplaysIntoClient_syncPrs(IRemoteControlClient rcc) {
        Iterator<DisplayInfoForServer> displayIterator = this.mRcDisplays.iterator();
        while (displayIterator.hasNext()) {
            DisplayInfoForServer di = displayIterator.next();
            try {
                rcc.plugRemoteControlDisplay(di.mRcDisplay, di.mArtworkExpectedWidth, di.mArtworkExpectedHeight);
                if (di.mWantsPositionSync) {
                    rcc.setWantsSyncForDisplay(di.mRcDisplay, true);
                }
            } catch (RemoteException e) {
                Log.e(TAG, "Error connecting RCD to RCC in RCC registration", e);
            }
        }
    }

    private void enableRemoteControlDisplayForClient_syncRcStack(IRemoteControlDisplay rcd, boolean enabled) {
        Iterator<PlayerRecord> stackIterator = this.mPRStack.iterator();
        while (stackIterator.hasNext()) {
            PlayerRecord prse = stackIterator.next();
            if (prse.getRcc() != null) {
                try {
                    prse.getRcc().enableRemoteControlDisplay(rcd, enabled);
                } catch (RemoteException e) {
                    Log.e(TAG, "Error connecting RCD to client: ", e);
                }
            }
        }
    }

    private boolean rcDisplayIsPluggedIn_syncRcStack(IRemoteControlDisplay rcd) {
        Iterator<DisplayInfoForServer> displayIterator = this.mRcDisplays.iterator();
        while (displayIterator.hasNext()) {
            if (displayIterator.next().mRcDisplay.asBinder().equals(rcd.asBinder())) {
                return true;
            }
        }
        return false;
    }

    private void registerRemoteControlDisplay_int(IRemoteControlDisplay rcd, int w, int h, ComponentName listenerComp) {
        synchronized (mAudioFocusLock) {
            synchronized (this.mPRStack) {
                if (rcd != null) {
                    if (!rcDisplayIsPluggedIn_syncRcStack(rcd)) {
                        DisplayInfoForServer di = new DisplayInfoForServer(rcd, w, h);
                        di.mEnabled = true;
                        di.mClientNotifListComp = listenerComp;
                        if (di.init()) {
                            this.mRcDisplays.add(di);
                            Iterator<PlayerRecord> stackIterator = this.mPRStack.iterator();
                            while (stackIterator.hasNext()) {
                                PlayerRecord prse = stackIterator.next();
                                if (prse.getRcc() != null) {
                                    try {
                                        prse.getRcc().plugRemoteControlDisplay(rcd, w, h);
                                    } catch (RemoteException e) {
                                        Log.e(TAG, "Error connecting RCD to client: ", e);
                                    }
                                }
                            }
                            sendMsg(this.mEventHandler, 9, 2, w, h, rcd, 0);
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void unregisterRemoteControlDisplay(IRemoteControlDisplay rcd) {
        synchronized (this.mPRStack) {
            if (rcd != null) {
                boolean displayWasPluggedIn = false;
                Iterator<DisplayInfoForServer> displayIterator = this.mRcDisplays.iterator();
                while (displayIterator.hasNext() && !displayWasPluggedIn) {
                    DisplayInfoForServer di = displayIterator.next();
                    if (di.mRcDisplay.asBinder().equals(rcd.asBinder())) {
                        displayWasPluggedIn = true;
                        di.release();
                        displayIterator.remove();
                    }
                }
                if (displayWasPluggedIn) {
                    Iterator<PlayerRecord> stackIterator = this.mPRStack.iterator();
                    while (stackIterator.hasNext()) {
                        PlayerRecord prse = stackIterator.next();
                        if (prse.getRcc() != null) {
                            try {
                                prse.getRcc().unplugRemoteControlDisplay(rcd);
                            } catch (RemoteException e) {
                                Log.e(TAG, "Error disconnecting remote control display to client: ", e);
                            }
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void remoteControlDisplayUsesBitmapSize(IRemoteControlDisplay rcd, int w, int h) {
        synchronized (this.mPRStack) {
            Iterator<DisplayInfoForServer> displayIterator = this.mRcDisplays.iterator();
            boolean artworkSizeUpdate = false;
            while (displayIterator.hasNext() && !artworkSizeUpdate) {
                DisplayInfoForServer di = displayIterator.next();
                if (di.mRcDisplay.asBinder().equals(rcd.asBinder()) && !(di.mArtworkExpectedWidth == w && di.mArtworkExpectedHeight == h)) {
                    di.mArtworkExpectedWidth = w;
                    di.mArtworkExpectedHeight = h;
                    artworkSizeUpdate = true;
                }
            }
            if (artworkSizeUpdate) {
                Iterator<PlayerRecord> stackIterator = this.mPRStack.iterator();
                while (stackIterator.hasNext()) {
                    PlayerRecord prse = stackIterator.next();
                    if (prse.getRcc() != null) {
                        try {
                            prse.getRcc().setBitmapSizeForDisplay(rcd, w, h);
                        } catch (RemoteException e) {
                            Log.e(TAG, "Error setting bitmap size for RCD on RCC: ", e);
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void remoteControlDisplayWantsPlaybackPositionSync(IRemoteControlDisplay rcd, boolean wantsSync) {
        synchronized (this.mPRStack) {
            boolean rcdRegistered = false;
            Iterator<DisplayInfoForServer> displayIterator = this.mRcDisplays.iterator();
            while (true) {
                if (!displayIterator.hasNext()) {
                    break;
                }
                DisplayInfoForServer di = displayIterator.next();
                if (di.mRcDisplay.asBinder().equals(rcd.asBinder())) {
                    di.mWantsPositionSync = wantsSync;
                    rcdRegistered = true;
                    break;
                }
            }
            if (rcdRegistered) {
                Iterator<PlayerRecord> stackIterator = this.mPRStack.iterator();
                while (stackIterator.hasNext()) {
                    PlayerRecord prse = stackIterator.next();
                    if (prse.getRcc() != null) {
                        try {
                            prse.getRcc().setWantsSyncForDisplay(rcd, wantsSync);
                        } catch (RemoteException e) {
                            Log.e(TAG, "Error setting position sync flag for RCD on RCC: ", e);
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onRegisterVolumeObserverForRcc(int rccId, IRemoteVolumeObserver rvo) {
        synchronized (this.mPRStack) {
            try {
                int index = this.mPRStack.size() - 1;
                while (true) {
                    if (index >= 0) {
                        PlayerRecord prse = this.mPRStack.elementAt(index);
                        if (prse.getRccId() == rccId) {
                            prse.mRemoteVolumeObs = rvo;
                            break;
                        }
                        index--;
                    }
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                Log.e(TAG, "Wrong index accessing media button stack, lock error? ", e);
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean checkUpdateRemoteStateIfActive(int streamType) {
        synchronized (this.mPRStack) {
            try {
                for (int index = this.mPRStack.size() - 1; index >= 0; index--) {
                    PlayerRecord prse = this.mPRStack.elementAt(index);
                    if (prse.mPlaybackType == 1 && isPlaystateActive(prse.mPlaybackState.mState) && prse.mPlaybackStream == streamType) {
                        synchronized (this.mMainRemote) {
                            this.mMainRemote.mRccId = prse.getRccId();
                            this.mMainRemote.mVolume = prse.mPlaybackVolume;
                            this.mMainRemote.mVolumeMax = prse.mPlaybackVolumeMax;
                            this.mMainRemote.mVolumeHandling = prse.mPlaybackVolumeHandling;
                            this.mMainRemoteIsActive = true;
                        }
                        return true;
                    }
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                Log.e(TAG, "Wrong index accessing RC stack, lock error? ", e);
            }
            synchronized (this.mMainRemote) {
                this.mMainRemoteIsActive = false;
            }
            return false;
        }
    }

    protected static boolean isPlaystateActive(int playState) {
        switch (playState) {
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
                return true;
            default:
                return false;
        }
    }

    private void sendVolumeUpdateToRemote(int rccId, int direction) {
        if (direction != 0) {
            IRemoteVolumeObserver rvo = null;
            synchronized (this.mPRStack) {
                try {
                    int index = this.mPRStack.size() - 1;
                    while (true) {
                        if (index >= 0) {
                            PlayerRecord prse = this.mPRStack.elementAt(index);
                            if (prse.getRccId() == rccId) {
                                rvo = prse.mRemoteVolumeObs;
                                break;
                            }
                            index--;
                        }
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    Log.e(TAG, "Wrong index accessing media button stack, lock error? ", e);
                }
            }
            if (rvo != null) {
                try {
                    rvo.dispatchRemoteVolumeUpdate(direction, -1);
                } catch (RemoteException e2) {
                    Log.e(TAG, "Error dispatching relative volume update", e2);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public int getRemoteStreamMaxVolume() {
        int i;
        synchronized (this.mMainRemote) {
            if (this.mMainRemote.mRccId == -1) {
                i = 0;
            } else {
                i = this.mMainRemote.mVolumeMax;
            }
        }
        return i;
    }

    /* access modifiers changed from: protected */
    public int getRemoteStreamVolume() {
        int i;
        synchronized (this.mMainRemote) {
            if (this.mMainRemote.mRccId == -1) {
                i = 0;
            } else {
                i = this.mMainRemote.mVolume;
            }
        }
        return i;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:11:?, code lost:
        r1 = r8.mPRStack.size() - 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001e, code lost:
        if (r1 < 0) goto L_0x0030;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0020, code lost:
        r2 = r8.mPRStack.elementAt(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x002c, code lost:
        if (r2.getRccId() != r3) goto L_0x0044;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x002e, code lost:
        r4 = r2.mRemoteVolumeObs;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0044, code lost:
        r1 = r1 - 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0047, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0048, code lost:
        android.util.Log.e(android.media.MediaFocusControl.TAG, "Wrong index accessing media button stack, lock error? ", r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0012, code lost:
        r4 = null;
        r6 = r8.mPRStack;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0015, code lost:
        monitor-enter(r6);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setRemoteStreamVolume(int r9) {
        /*
            r8 = this;
            r3 = -1
            android.media.PlayerRecord$RemotePlaybackState r6 = r8.mMainRemote
            monitor-enter(r6)
            android.media.PlayerRecord$RemotePlaybackState r5 = r8.mMainRemote     // Catch:{ all -> 0x0041 }
            int r5 = r5.mRccId     // Catch:{ all -> 0x0041 }
            r7 = -1
            if (r5 != r7) goto L_0x000d
            monitor-exit(r6)     // Catch:{ all -> 0x0041 }
        L_0x000c:
            return
        L_0x000d:
            android.media.PlayerRecord$RemotePlaybackState r5 = r8.mMainRemote     // Catch:{ all -> 0x0041 }
            int r3 = r5.mRccId     // Catch:{ all -> 0x0041 }
            monitor-exit(r6)     // Catch:{ all -> 0x0041 }
            r4 = 0
            java.util.Stack<android.media.PlayerRecord> r6 = r8.mPRStack
            monitor-enter(r6)
            java.util.Stack<android.media.PlayerRecord> r5 = r8.mPRStack     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0047 }
            int r5 = r5.size()     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0047 }
            int r1 = r5 + -1
        L_0x001e:
            if (r1 < 0) goto L_0x0030
            java.util.Stack<android.media.PlayerRecord> r5 = r8.mPRStack     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0047 }
            java.lang.Object r2 = r5.elementAt(r1)     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0047 }
            android.media.PlayerRecord r2 = (android.media.PlayerRecord) r2     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0047 }
            int r5 = r2.getRccId()     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0047 }
            if (r5 != r3) goto L_0x0044
            android.media.IRemoteVolumeObserver r4 = r2.mRemoteVolumeObs     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0047 }
        L_0x0030:
            monitor-exit(r6)     // Catch:{ all -> 0x0050 }
            if (r4 == 0) goto L_0x000c
            r5 = 0
            r4.dispatchRemoteVolumeUpdate(r5, r9)     // Catch:{ RemoteException -> 0x0038 }
            goto L_0x000c
        L_0x0038:
            r0 = move-exception
            java.lang.String r5 = "MediaFocusControl"
            java.lang.String r6 = "Error dispatching absolute volume update"
            android.util.Log.e(r5, r6, r0)
            goto L_0x000c
        L_0x0041:
            r5 = move-exception
            monitor-exit(r6)
            throw r5
        L_0x0044:
            int r1 = r1 + -1
            goto L_0x001e
        L_0x0047:
            r0 = move-exception
            java.lang.String r5 = "MediaFocusControl"
            java.lang.String r7 = "Wrong index accessing media button stack, lock error? "
            android.util.Log.e(r5, r7, r0)
            goto L_0x0030
        L_0x0050:
            r5 = move-exception
            monitor-exit(r6)
            throw r5
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.MediaFocusControl.setRemoteStreamVolume(int):void");
    }

    /* access modifiers changed from: protected */
    public void postReevaluateRemote() {
        sendMsg(this.mEventHandler, 3, 2, 0, 0, null, 0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onReevaluateRemote() {
    }
}

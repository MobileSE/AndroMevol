package android.media.tv;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.hardware.hdmi.HdmiDeviceInfo;
import android.media.tv.ITvInputService;
import android.media.tv.TvInputManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.InputChannel;
import android.view.InputEvent;
import android.view.InputEventReceiver;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import com.android.internal.os.SomeArgs;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class TvInputService extends Service {
    private static final boolean DEBUG = false;
    public static final String SERVICE_INTERFACE = "android.media.tv.TvInputService";
    public static final String SERVICE_META_DATA = "android.media.tv.input";
    private static final String TAG = "TvInputService";
    private final RemoteCallbackList<ITvInputServiceCallback> mCallbacks = new RemoteCallbackList<>();
    private final Handler mServiceHandler = new ServiceHandler();
    private TvInputManager mTvInputManager;

    public abstract Session onCreateSession(String str);

    @Override // android.app.Service
    public final IBinder onBind(Intent intent) {
        return new ITvInputService.Stub() {
            /* class android.media.tv.TvInputService.AnonymousClass1 */

            @Override // android.media.tv.ITvInputService
            public void registerCallback(ITvInputServiceCallback cb) {
                if (cb != null) {
                    TvInputService.this.mCallbacks.register(cb);
                }
            }

            @Override // android.media.tv.ITvInputService
            public void unregisterCallback(ITvInputServiceCallback cb) {
                if (cb != null) {
                    TvInputService.this.mCallbacks.unregister(cb);
                }
            }

            @Override // android.media.tv.ITvInputService
            public void createSession(InputChannel channel, ITvInputSessionCallback cb, String inputId) {
                if (channel == null) {
                    Log.w(TvInputService.TAG, "Creating session without input channel");
                }
                if (cb != null) {
                    SomeArgs args = SomeArgs.obtain();
                    args.arg1 = channel;
                    args.arg2 = cb;
                    args.arg3 = inputId;
                    TvInputService.this.mServiceHandler.obtainMessage(1, args).sendToTarget();
                }
            }

            @Override // android.media.tv.ITvInputService
            public void notifyHardwareAdded(TvInputHardwareInfo hardwareInfo) {
                TvInputService.this.mServiceHandler.obtainMessage(3, hardwareInfo).sendToTarget();
            }

            @Override // android.media.tv.ITvInputService
            public void notifyHardwareRemoved(TvInputHardwareInfo hardwareInfo) {
                TvInputService.this.mServiceHandler.obtainMessage(4, hardwareInfo).sendToTarget();
            }

            @Override // android.media.tv.ITvInputService
            public void notifyHdmiDeviceAdded(HdmiDeviceInfo deviceInfo) {
                TvInputService.this.mServiceHandler.obtainMessage(5, deviceInfo).sendToTarget();
            }

            @Override // android.media.tv.ITvInputService
            public void notifyHdmiDeviceRemoved(HdmiDeviceInfo deviceInfo) {
                TvInputService.this.mServiceHandler.obtainMessage(6, deviceInfo).sendToTarget();
            }
        };
    }

    public final int getRegisteredCallbackCount() {
        return this.mCallbacks.getRegisteredCallbackCount();
    }

    public TvInputInfo onHardwareAdded(TvInputHardwareInfo hardwareInfo) {
        return null;
    }

    public String onHardwareRemoved(TvInputHardwareInfo hardwareInfo) {
        return null;
    }

    public TvInputInfo onHdmiDeviceAdded(HdmiDeviceInfo deviceInfo) {
        return null;
    }

    public String onHdmiDeviceRemoved(HdmiDeviceInfo deviceInfo) {
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isPassthroughInput(String inputId) {
        if (this.mTvInputManager == null) {
            this.mTvInputManager = (TvInputManager) getSystemService(Context.TV_INPUT_SERVICE);
        }
        TvInputInfo info = this.mTvInputManager.getTvInputInfo(inputId);
        if (info == null || !info.isPassthroughInput()) {
            return false;
        }
        return true;
    }

    public static abstract class Session implements KeyEvent.Callback {
        private final KeyEvent.DispatcherState mDispatcherState = new KeyEvent.DispatcherState();
        final Handler mHandler;
        private Rect mOverlayFrame;
        private View mOverlayView;
        private boolean mOverlayViewEnabled;
        private ITvInputSessionCallback mSessionCallback;
        private Surface mSurface;
        private final WindowManager mWindowManager;
        private WindowManager.LayoutParams mWindowParams;
        private IBinder mWindowToken;

        public abstract void onRelease();

        public abstract void onSetCaptionEnabled(boolean z);

        public abstract void onSetStreamVolume(float f);

        public abstract boolean onSetSurface(Surface surface);

        public abstract boolean onTune(Uri uri);

        public Session(Context context) {
            this.mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            this.mHandler = new Handler(context.getMainLooper());
        }

        public void setOverlayViewEnabled(final boolean enable) {
            this.mHandler.post(new Runnable() {
                /* class android.media.tv.TvInputService.Session.AnonymousClass1 */

                public void run() {
                    if (enable != Session.this.mOverlayViewEnabled) {
                        Session.this.mOverlayViewEnabled = enable;
                        if (!enable) {
                            Session.this.removeOverlayView(false);
                        } else if (Session.this.mWindowToken != null) {
                            Session.this.createOverlayView(Session.this.mWindowToken, Session.this.mOverlayFrame);
                        }
                    }
                }
            });
        }

        public void notifySessionEvent(final String eventType, final Bundle eventArgs) {
            if (eventType == null) {
                throw new IllegalArgumentException("eventType should not be null.");
            }
            runOnMainThread(new Runnable() {
                /* class android.media.tv.TvInputService.Session.AnonymousClass2 */

                public void run() {
                    try {
                        Session.this.mSessionCallback.onSessionEvent(eventType, eventArgs);
                    } catch (RemoteException e) {
                        Log.w(TvInputService.TAG, "error in sending event (event=" + eventType + ")");
                    }
                }
            });
        }

        public void notifyChannelRetuned(final Uri channelUri) {
            runOnMainThread(new Runnable() {
                /* class android.media.tv.TvInputService.Session.AnonymousClass3 */

                public void run() {
                    try {
                        Session.this.mSessionCallback.onChannelRetuned(channelUri);
                    } catch (RemoteException e) {
                        Log.w(TvInputService.TAG, "error in notifyChannelRetuned");
                    }
                }
            });
        }

        public void notifyTracksChanged(final List<TvTrackInfo> tracks) {
            Set<String> trackIdSet = new HashSet<>();
            for (TvTrackInfo track : tracks) {
                String trackId = track.getId();
                if (trackIdSet.contains(trackId)) {
                    throw new IllegalArgumentException("redundant track ID: " + trackId);
                }
                trackIdSet.add(trackId);
            }
            trackIdSet.clear();
            runOnMainThread(new Runnable() {
                /* class android.media.tv.TvInputService.Session.AnonymousClass4 */

                public void run() {
                    try {
                        Session.this.mSessionCallback.onTracksChanged(tracks);
                    } catch (RemoteException e) {
                        Log.w(TvInputService.TAG, "error in notifyTracksChanged");
                    }
                }
            });
        }

        public void notifyTrackSelected(final int type, final String trackId) {
            runOnMainThread(new Runnable() {
                /* class android.media.tv.TvInputService.Session.AnonymousClass5 */

                public void run() {
                    try {
                        Session.this.mSessionCallback.onTrackSelected(type, trackId);
                    } catch (RemoteException e) {
                        Log.w(TvInputService.TAG, "error in notifyTrackSelected");
                    }
                }
            });
        }

        public void notifyVideoAvailable() {
            runOnMainThread(new Runnable() {
                /* class android.media.tv.TvInputService.Session.AnonymousClass6 */

                public void run() {
                    try {
                        Session.this.mSessionCallback.onVideoAvailable();
                    } catch (RemoteException e) {
                        Log.w(TvInputService.TAG, "error in notifyVideoAvailable");
                    }
                }
            });
        }

        public void notifyVideoUnavailable(final int reason) {
            if (reason < 0 || reason > 3) {
                throw new IllegalArgumentException("Unknown reason: " + reason);
            }
            runOnMainThread(new Runnable() {
                /* class android.media.tv.TvInputService.Session.AnonymousClass7 */

                public void run() {
                    try {
                        Session.this.mSessionCallback.onVideoUnavailable(reason);
                    } catch (RemoteException e) {
                        Log.w(TvInputService.TAG, "error in notifyVideoUnavailable");
                    }
                }
            });
        }

        public void notifyContentAllowed() {
            runOnMainThread(new Runnable() {
                /* class android.media.tv.TvInputService.Session.AnonymousClass8 */

                public void run() {
                    try {
                        Session.this.mSessionCallback.onContentAllowed();
                    } catch (RemoteException e) {
                        Log.w(TvInputService.TAG, "error in notifyContentAllowed");
                    }
                }
            });
        }

        public void notifyContentBlocked(final TvContentRating rating) {
            runOnMainThread(new Runnable() {
                /* class android.media.tv.TvInputService.Session.AnonymousClass9 */

                public void run() {
                    try {
                        Session.this.mSessionCallback.onContentBlocked(rating.flattenToString());
                    } catch (RemoteException e) {
                        Log.w(TvInputService.TAG, "error in notifyContentBlocked");
                    }
                }
            });
        }

        public void layoutSurface(final int left, final int top, final int right, final int bottm) {
            if (left > right || top > bottm) {
                throw new IllegalArgumentException("Invalid parameter");
            }
            runOnMainThread(new Runnable() {
                /* class android.media.tv.TvInputService.Session.AnonymousClass10 */

                public void run() {
                    try {
                        Session.this.mSessionCallback.onLayoutSurface(left, top, right, bottm);
                    } catch (RemoteException e) {
                        Log.w(TvInputService.TAG, "error in layoutSurface");
                    }
                }
            });
        }

        public void onSetMain(boolean isMain) {
        }

        public void onSurfaceChanged(int format, int width, int height) {
        }

        public void onOverlayViewSizeChanged(int width, int height) {
        }

        public boolean onTune(Uri channelUri, Bundle params) {
            return onTune(channelUri);
        }

        public void onUnblockContent(TvContentRating unblockedRating) {
        }

        public boolean onSelectTrack(int type, String trackId) {
            return false;
        }

        public void onAppPrivateCommand(String action, Bundle data) {
        }

        public View onCreateOverlayView() {
            return null;
        }

        @Override // android.view.KeyEvent.Callback
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            return false;
        }

        @Override // android.view.KeyEvent.Callback
        public boolean onKeyLongPress(int keyCode, KeyEvent event) {
            return false;
        }

        @Override // android.view.KeyEvent.Callback
        public boolean onKeyMultiple(int keyCode, int count, KeyEvent event) {
            return false;
        }

        @Override // android.view.KeyEvent.Callback
        public boolean onKeyUp(int keyCode, KeyEvent event) {
            return false;
        }

        public boolean onTouchEvent(MotionEvent event) {
            return false;
        }

        public boolean onTrackballEvent(MotionEvent event) {
            return false;
        }

        public boolean onGenericMotionEvent(MotionEvent event) {
            return false;
        }

        /* access modifiers changed from: package-private */
        public void release() {
            removeOverlayView(true);
            onRelease();
            if (this.mSurface != null) {
                this.mSurface.release();
                this.mSurface = null;
            }
        }

        /* access modifiers changed from: package-private */
        public void setMain(boolean isMain) {
            onSetMain(isMain);
        }

        /* access modifiers changed from: package-private */
        public void setSurface(Surface surface) {
            onSetSurface(surface);
            if (this.mSurface != null) {
                this.mSurface.release();
            }
            this.mSurface = surface;
        }

        /* access modifiers changed from: package-private */
        public void dispatchSurfaceChanged(int format, int width, int height) {
            onSurfaceChanged(format, width, height);
        }

        /* access modifiers changed from: package-private */
        public void setStreamVolume(float volume) {
            onSetStreamVolume(volume);
        }

        /* access modifiers changed from: package-private */
        public void tune(Uri channelUri, Bundle params) {
            onTune(channelUri, params);
        }

        /* access modifiers changed from: package-private */
        public void setCaptionEnabled(boolean enabled) {
            onSetCaptionEnabled(enabled);
        }

        /* access modifiers changed from: package-private */
        public void selectTrack(int type, String trackId) {
            onSelectTrack(type, trackId);
        }

        /* access modifiers changed from: package-private */
        public void unblockContent(String unblockedRating) {
            onUnblockContent(TvContentRating.unflattenFromString(unblockedRating));
        }

        /* access modifiers changed from: package-private */
        public void appPrivateCommand(String action, Bundle data) {
            onAppPrivateCommand(action, data);
        }

        /* access modifiers changed from: package-private */
        public void createOverlayView(IBinder windowToken, Rect frame) {
            if (this.mOverlayView != null) {
                this.mWindowManager.removeView(this.mOverlayView);
                this.mOverlayView = null;
            }
            this.mWindowToken = windowToken;
            this.mOverlayFrame = frame;
            onOverlayViewSizeChanged(frame.right - frame.left, frame.bottom - frame.top);
            if (this.mOverlayViewEnabled) {
                this.mOverlayView = onCreateOverlayView();
                if (this.mOverlayView != null) {
                    this.mWindowParams = new WindowManager.LayoutParams(frame.right - frame.left, frame.bottom - frame.top, frame.left, frame.top, 1004, 536, -2);
                    this.mWindowParams.privateFlags |= 64;
                    this.mWindowParams.gravity = 8388659;
                    this.mWindowParams.token = windowToken;
                    this.mWindowManager.addView(this.mOverlayView, this.mWindowParams);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void relayoutOverlayView(Rect frame) {
            if (!(this.mOverlayFrame != null && this.mOverlayFrame.width() == frame.width() && this.mOverlayFrame.height() == frame.height())) {
                onOverlayViewSizeChanged(frame.right - frame.left, frame.bottom - frame.top);
            }
            this.mOverlayFrame = frame;
            if (this.mOverlayViewEnabled && this.mOverlayView != null) {
                this.mWindowParams.x = frame.left;
                this.mWindowParams.y = frame.top;
                this.mWindowParams.width = frame.right - frame.left;
                this.mWindowParams.height = frame.bottom - frame.top;
                this.mWindowManager.updateViewLayout(this.mOverlayView, this.mWindowParams);
            }
        }

        /* access modifiers changed from: package-private */
        public void removeOverlayView(boolean clearWindowToken) {
            if (clearWindowToken) {
                this.mWindowToken = null;
                this.mOverlayFrame = null;
            }
            if (this.mOverlayView != null) {
                this.mWindowManager.removeView(this.mOverlayView);
                this.mOverlayView = null;
                this.mWindowParams = null;
            }
        }

        /* access modifiers changed from: package-private */
        public int dispatchInputEvent(InputEvent event, InputEventReceiver receiver) {
            boolean isNavigationKey = false;
            if (event instanceof KeyEvent) {
                KeyEvent keyEvent = (KeyEvent) event;
                isNavigationKey = TvInputService.isNavigationKey(keyEvent.getKeyCode());
                if (keyEvent.dispatch(this, this.mDispatcherState, this)) {
                    return 1;
                }
            } else if (event instanceof MotionEvent) {
                MotionEvent motionEvent = (MotionEvent) event;
                int source = motionEvent.getSource();
                if (motionEvent.isTouchEvent()) {
                    if (onTouchEvent(motionEvent)) {
                        return 1;
                    }
                } else if ((source & 4) != 0) {
                    if (onTrackballEvent(motionEvent)) {
                        return 1;
                    }
                } else if (onGenericMotionEvent(motionEvent)) {
                    return 1;
                }
            }
            if (this.mOverlayView == null || !this.mOverlayView.isAttachedToWindow()) {
                return 0;
            }
            if (!this.mOverlayView.hasWindowFocus()) {
                this.mOverlayView.getViewRootImpl().windowFocusChanged(true, true);
            }
            if (!isNavigationKey || !this.mOverlayView.hasFocusable()) {
                this.mOverlayView.getViewRootImpl().dispatchInputEvent(event, receiver);
                return -1;
            }
            this.mOverlayView.getViewRootImpl().dispatchInputEvent(event);
            return 1;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setSessionCallback(ITvInputSessionCallback callback) {
            this.mSessionCallback = callback;
        }

        private final void runOnMainThread(Runnable action) {
            if (!this.mHandler.getLooper().isCurrentThread() || this.mSessionCallback == null) {
                this.mHandler.post(action);
            } else {
                action.run();
            }
        }
    }

    public static abstract class HardwareSession extends Session {
        private TvInputManager.Session mHardwareSession;
        private final TvInputManager.SessionCallback mHardwareSessionCallback = new TvInputManager.SessionCallback() {
            /* class android.media.tv.TvInputService.HardwareSession.AnonymousClass1 */

            @Override // android.media.tv.TvInputManager.SessionCallback
            public void onSessionCreated(TvInputManager.Session session) {
                HardwareSession.this.mHardwareSession = session;
                SomeArgs args = SomeArgs.obtain();
                if (session != null) {
                    args.arg1 = HardwareSession.this.mProxySession;
                    args.arg2 = HardwareSession.this.mProxySessionCallback;
                    args.arg3 = session.getToken();
                } else {
                    args.arg1 = null;
                    args.arg2 = HardwareSession.this.mProxySessionCallback;
                    args.arg3 = null;
                    HardwareSession.this.onRelease();
                }
                HardwareSession.this.mServiceHandler.obtainMessage(2, args).sendToTarget();
            }

            @Override // android.media.tv.TvInputManager.SessionCallback
            public void onVideoAvailable(TvInputManager.Session session) {
                if (HardwareSession.this.mHardwareSession == session) {
                    HardwareSession.this.onHardwareVideoAvailable();
                }
            }

            @Override // android.media.tv.TvInputManager.SessionCallback
            public void onVideoUnavailable(TvInputManager.Session session, int reason) {
                if (HardwareSession.this.mHardwareSession == session) {
                    HardwareSession.this.onHardwareVideoUnavailable(reason);
                }
            }
        };
        private ITvInputSession mProxySession;
        private ITvInputSessionCallback mProxySessionCallback;
        private Handler mServiceHandler;

        public abstract String getHardwareInputId();

        public HardwareSession(Context context) {
            super(context);
        }

        @Override // android.media.tv.TvInputService.Session
        public final boolean onSetSurface(Surface surface) {
            Log.e(TvInputService.TAG, "onSetSurface() should not be called in HardwareProxySession.");
            return false;
        }

        public void onHardwareVideoAvailable() {
        }

        public void onHardwareVideoUnavailable(int reason) {
        }
    }

    public static boolean isNavigationKey(int keyCode) {
        switch (keyCode) {
            case 19:
            case 20:
            case 21:
            case 22:
            case 23:
            case 61:
            case 62:
            case 66:
            case 92:
            case 93:
            case 122:
            case 123:
                return true;
            default:
                return false;
        }
    }

    @SuppressLint({"HandlerLeak"})
    private final class ServiceHandler extends Handler {
        private static final int DO_ADD_HARDWARE_TV_INPUT = 3;
        private static final int DO_ADD_HDMI_TV_INPUT = 5;
        private static final int DO_CREATE_SESSION = 1;
        private static final int DO_NOTIFY_SESSION_CREATED = 2;
        private static final int DO_REMOVE_HARDWARE_TV_INPUT = 4;
        private static final int DO_REMOVE_HDMI_TV_INPUT = 6;

        private ServiceHandler() {
        }

        private void broadcastAddHardwareTvInput(int deviceId, TvInputInfo inputInfo) {
            int n = TvInputService.this.mCallbacks.beginBroadcast();
            for (int i = 0; i < n; i++) {
                try {
                    ((ITvInputServiceCallback) TvInputService.this.mCallbacks.getBroadcastItem(i)).addHardwareTvInput(deviceId, inputInfo);
                } catch (RemoteException e) {
                    Log.e(TvInputService.TAG, "Error while broadcasting.", e);
                }
            }
            TvInputService.this.mCallbacks.finishBroadcast();
        }

        private void broadcastAddHdmiTvInput(int id, TvInputInfo inputInfo) {
            int n = TvInputService.this.mCallbacks.beginBroadcast();
            for (int i = 0; i < n; i++) {
                try {
                    ((ITvInputServiceCallback) TvInputService.this.mCallbacks.getBroadcastItem(i)).addHdmiTvInput(id, inputInfo);
                } catch (RemoteException e) {
                    Log.e(TvInputService.TAG, "Error while broadcasting.", e);
                }
            }
            TvInputService.this.mCallbacks.finishBroadcast();
        }

        private void broadcastRemoveTvInput(String inputId) {
            int n = TvInputService.this.mCallbacks.beginBroadcast();
            for (int i = 0; i < n; i++) {
                try {
                    ((ITvInputServiceCallback) TvInputService.this.mCallbacks.getBroadcastItem(i)).removeTvInput(inputId);
                } catch (RemoteException e) {
                    Log.e(TvInputService.TAG, "Error while broadcasting.", e);
                }
            }
            TvInputService.this.mCallbacks.finishBroadcast();
        }

        @Override // android.os.Handler
        public final void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    SomeArgs args = (SomeArgs) msg.obj;
                    InputChannel channel = (InputChannel) args.arg1;
                    ITvInputSessionCallback cb = (ITvInputSessionCallback) args.arg2;
                    args.recycle();
                    Session sessionImpl = TvInputService.this.onCreateSession((String) args.arg3);
                    if (sessionImpl == null) {
                        try {
                            cb.onSessionCreated(null, null);
                            return;
                        } catch (RemoteException e) {
                            Log.e(TvInputService.TAG, "error in onSessionCreated");
                            return;
                        }
                    } else {
                        sessionImpl.setSessionCallback(cb);
                        ITvInputSession stub = new ITvInputSessionWrapper(TvInputService.this, sessionImpl, channel);
                        if (sessionImpl instanceof HardwareSession) {
                            HardwareSession proxySession = (HardwareSession) sessionImpl;
                            String harewareInputId = proxySession.getHardwareInputId();
                            if (TextUtils.isEmpty(harewareInputId) || !TvInputService.this.isPassthroughInput(harewareInputId)) {
                                if (TextUtils.isEmpty(harewareInputId)) {
                                    Log.w(TvInputService.TAG, "Hardware input id is not setup yet.");
                                } else {
                                    Log.w(TvInputService.TAG, "Invalid hardware input id : " + harewareInputId);
                                }
                                sessionImpl.onRelease();
                                try {
                                    cb.onSessionCreated(null, null);
                                    return;
                                } catch (RemoteException e2) {
                                    Log.e(TvInputService.TAG, "error in onSessionCreated");
                                    return;
                                }
                            } else {
                                proxySession.mProxySession = stub;
                                proxySession.mProxySessionCallback = cb;
                                proxySession.mServiceHandler = TvInputService.this.mServiceHandler;
                                ((TvInputManager) TvInputService.this.getSystemService(Context.TV_INPUT_SERVICE)).createSession(harewareInputId, proxySession.mHardwareSessionCallback, TvInputService.this.mServiceHandler);
                                return;
                            }
                        } else {
                            SomeArgs someArgs = SomeArgs.obtain();
                            someArgs.arg1 = stub;
                            someArgs.arg2 = cb;
                            someArgs.arg3 = null;
                            TvInputService.this.mServiceHandler.obtainMessage(2, someArgs).sendToTarget();
                            return;
                        }
                    }
                case 2:
                    SomeArgs args2 = (SomeArgs) msg.obj;
                    try {
                        ((ITvInputSessionCallback) args2.arg2).onSessionCreated((ITvInputSession) args2.arg1, (IBinder) args2.arg3);
                    } catch (RemoteException e3) {
                        Log.e(TvInputService.TAG, "error in onSessionCreated");
                    }
                    args2.recycle();
                    return;
                case 3:
                    TvInputHardwareInfo hardwareInfo = (TvInputHardwareInfo) msg.obj;
                    TvInputInfo inputInfo = TvInputService.this.onHardwareAdded(hardwareInfo);
                    if (inputInfo != null) {
                        broadcastAddHardwareTvInput(hardwareInfo.getDeviceId(), inputInfo);
                        return;
                    }
                    return;
                case 4:
                    String inputId = TvInputService.this.onHardwareRemoved((TvInputHardwareInfo) msg.obj);
                    if (inputId != null) {
                        broadcastRemoveTvInput(inputId);
                        return;
                    }
                    return;
                case 5:
                    HdmiDeviceInfo deviceInfo = (HdmiDeviceInfo) msg.obj;
                    TvInputInfo inputInfo2 = TvInputService.this.onHdmiDeviceAdded(deviceInfo);
                    if (inputInfo2 != null) {
                        broadcastAddHdmiTvInput(deviceInfo.getId(), inputInfo2);
                        return;
                    }
                    return;
                case 6:
                    String inputId2 = TvInputService.this.onHdmiDeviceRemoved((HdmiDeviceInfo) msg.obj);
                    if (inputId2 != null) {
                        broadcastRemoveTvInput(inputId2);
                        return;
                    }
                    return;
                default:
                    Log.w(TvInputService.TAG, "Unhandled message code: " + msg.what);
                    return;
            }
        }
    }
}

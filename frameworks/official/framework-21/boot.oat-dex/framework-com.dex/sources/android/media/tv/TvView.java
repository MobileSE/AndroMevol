package android.media.tv;

import android.content.Context;
import android.graphics.Rect;
import android.media.tv.TvInputManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewRootImpl;
import java.lang.ref.WeakReference;
import java.util.List;

public class TvView extends ViewGroup {
    private static final int CAPTION_DEFAULT = 0;
    private static final int CAPTION_DISABLED = 2;
    private static final int CAPTION_ENABLED = 1;
    private static final boolean DEBUG = false;
    private static final WeakReference<TvView> NULL_TV_VIEW = new WeakReference<>(null);
    private static final String TAG = "TvView";
    private static final int VIDEO_SIZE_VALUE_UNKNOWN = 0;
    private static final int ZORDER_MEDIA = 0;
    private static final int ZORDER_MEDIA_OVERLAY = 1;
    private static final int ZORDER_ON_TOP = 2;
    private static WeakReference<TvView> sMainTvView = NULL_TV_VIEW;
    private static final Object sMainTvViewLock = new Object();
    private final AttributeSet mAttrs;
    private TvInputCallback mCallback;
    private int mCaptionEnabled;
    private final int mDefStyleAttr;
    private final TvInputManager.Session.FinishedInputEventCallback mFinishedInputEventCallback;
    private final Handler mHandler;
    private boolean mHasStreamVolume;
    private OnUnhandledInputEventListener mOnUnhandledInputEventListener;
    private boolean mOverlayViewCreated;
    private Rect mOverlayViewFrame;
    private TvInputManager.Session mSession;
    private MySessionCallback mSessionCallback;
    private float mStreamVolume;
    private Surface mSurface;
    private boolean mSurfaceChanged;
    private int mSurfaceFormat;
    private int mSurfaceHeight;
    private final SurfaceHolder.Callback mSurfaceHolderCallback;
    private SurfaceView mSurfaceView;
    private int mSurfaceViewBottom;
    private int mSurfaceViewLeft;
    private int mSurfaceViewRight;
    private int mSurfaceViewTop;
    private int mSurfaceWidth;
    private final TvInputManager mTvInputManager;
    private boolean mUseRequestedSurfaceLayout;
    private int mVideoHeight;
    private int mVideoWidth;
    private int mWindowZOrder;

    public interface OnUnhandledInputEventListener {
        boolean onUnhandledInputEvent(InputEvent inputEvent);
    }

    public TvView(Context context) {
        this(context, null, 0);
    }

    public TvView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TvView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mHandler = new Handler();
        this.mVideoWidth = 0;
        this.mVideoHeight = 0;
        this.mSurfaceHolderCallback = new SurfaceHolder.Callback() {
            /* class android.media.tv.TvView.AnonymousClass1 */

            @Override // android.view.SurfaceHolder.Callback
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                TvView.this.mSurfaceFormat = format;
                TvView.this.mSurfaceWidth = width;
                TvView.this.mSurfaceHeight = height;
                TvView.this.mSurfaceChanged = true;
                TvView.this.dispatchSurfaceChanged(TvView.this.mSurfaceFormat, TvView.this.mSurfaceWidth, TvView.this.mSurfaceHeight);
            }

            @Override // android.view.SurfaceHolder.Callback
            public void surfaceCreated(SurfaceHolder holder) {
                TvView.this.mSurface = holder.getSurface();
                TvView.this.setSessionSurface(TvView.this.mSurface);
            }

            @Override // android.view.SurfaceHolder.Callback
            public void surfaceDestroyed(SurfaceHolder holder) {
                TvView.this.mSurface = null;
                TvView.this.mSurfaceChanged = false;
                TvView.this.setSessionSurface(null);
            }
        };
        this.mFinishedInputEventCallback = new TvInputManager.Session.FinishedInputEventCallback() {
            /* class android.media.tv.TvView.AnonymousClass2 */

            @Override // android.media.tv.TvInputManager.Session.FinishedInputEventCallback
            public void onFinishedInputEvent(Object token, boolean handled) {
                ViewRootImpl viewRootImpl;
                if (!handled) {
                    InputEvent event = (InputEvent) token;
                    if (!TvView.this.dispatchUnhandledInputEvent(event) && (viewRootImpl = TvView.this.getViewRootImpl()) != null) {
                        viewRootImpl.dispatchUnhandledInputEvent(event);
                    }
                }
            }
        };
        this.mAttrs = attrs;
        this.mDefStyleAttr = defStyleAttr;
        resetSurfaceView();
        this.mTvInputManager = (TvInputManager) getContext().getSystemService(Context.TV_INPUT_SERVICE);
    }

    public void setCallback(TvInputCallback callback) {
        this.mCallback = callback;
    }

    public void setMain() {
        synchronized (sMainTvViewLock) {
            sMainTvView = new WeakReference<>(this);
            if (hasWindowFocus() && this.mSession != null) {
                this.mSession.setMain();
            }
        }
    }

    public void setZOrderMediaOverlay(boolean isMediaOverlay) {
        if (isMediaOverlay) {
            this.mWindowZOrder = 1;
            removeSessionOverlayView();
        } else {
            this.mWindowZOrder = 0;
            createSessionOverlayView();
        }
        if (this.mSurfaceView != null) {
            this.mSurfaceView.setZOrderOnTop(false);
            this.mSurfaceView.setZOrderMediaOverlay(isMediaOverlay);
        }
    }

    public void setZOrderOnTop(boolean onTop) {
        if (onTop) {
            this.mWindowZOrder = 2;
            removeSessionOverlayView();
        } else {
            this.mWindowZOrder = 0;
            createSessionOverlayView();
        }
        if (this.mSurfaceView != null) {
            this.mSurfaceView.setZOrderMediaOverlay(false);
            this.mSurfaceView.setZOrderOnTop(onTop);
        }
    }

    public void setStreamVolume(float volume) {
        this.mHasStreamVolume = true;
        this.mStreamVolume = volume;
        if (this.mSession != null) {
            this.mSession.setStreamVolume(volume);
        }
    }

    public void tune(String inputId, Uri channelUri) {
        tune(inputId, channelUri, null);
    }

    public void tune(String inputId, Uri channelUri, Bundle params) {
        if (TextUtils.isEmpty(inputId)) {
            throw new IllegalArgumentException("inputId cannot be null or an empty string");
        }
        synchronized (sMainTvViewLock) {
            if (sMainTvView.get() == null) {
                sMainTvView = new WeakReference<>(this);
            }
        }
        if (this.mSessionCallback == null || !this.mSessionCallback.mInputId.equals(inputId)) {
            resetInternal();
            this.mSessionCallback = new MySessionCallback(inputId, channelUri, params);
            if (this.mTvInputManager != null) {
                this.mTvInputManager.createSession(inputId, this.mSessionCallback, this.mHandler);
            }
        } else if (this.mSession != null) {
            this.mSession.tune(channelUri, params);
        } else {
            this.mSessionCallback.mChannelUri = channelUri;
            this.mSessionCallback.mTuneParams = params;
        }
    }

    public void reset() {
        synchronized (sMainTvViewLock) {
            if (this == sMainTvView.get()) {
                sMainTvView = NULL_TV_VIEW;
            }
        }
        resetInternal();
    }

    private void resetInternal() {
        if (this.mSession != null) {
            release();
            resetSurfaceView();
        }
    }

    public void requestUnblockContent(TvContentRating unblockedRating) {
        if (this.mSession != null) {
            this.mSession.requestUnblockContent(unblockedRating);
        }
    }

    public void setCaptionEnabled(boolean enabled) {
        this.mCaptionEnabled = enabled ? 1 : 2;
        if (this.mSession != null) {
            this.mSession.setCaptionEnabled(enabled);
        }
    }

    public void selectTrack(int type, String trackId) {
        if (this.mSession != null) {
            this.mSession.selectTrack(type, trackId);
        }
    }

    public List<TvTrackInfo> getTracks(int type) {
        if (this.mSession == null) {
            return null;
        }
        return this.mSession.getTracks(type);
    }

    public String getSelectedTrack(int type) {
        if (this.mSession == null) {
            return null;
        }
        return this.mSession.getSelectedTrack(type);
    }

    public void sendAppPrivateCommand(String action, Bundle data) {
        if (TextUtils.isEmpty(action)) {
            throw new IllegalArgumentException("action cannot be null or an empty string");
        } else if (this.mSession != null) {
            this.mSession.sendAppPrivateCommand(action, data);
        }
    }

    public boolean dispatchUnhandledInputEvent(InputEvent event) {
        if (this.mOnUnhandledInputEventListener == null || !this.mOnUnhandledInputEventListener.onUnhandledInputEvent(event)) {
            return onUnhandledInputEvent(event);
        }
        return true;
    }

    public boolean onUnhandledInputEvent(InputEvent event) {
        return false;
    }

    public void setOnUnhandledInputEventListener(OnUnhandledInputEventListener listener) {
        this.mOnUnhandledInputEventListener = listener;
    }

    @Override // android.view.View, android.view.ViewGroup
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (super.dispatchKeyEvent(event)) {
            return true;
        }
        if (this.mSession == null) {
            return false;
        }
        InputEvent copiedEvent = event.copy();
        return this.mSession.dispatchInputEvent(copiedEvent, copiedEvent, this.mFinishedInputEventCallback, this.mHandler) != 0;
    }

    @Override // android.view.View, android.view.ViewGroup
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (super.dispatchTouchEvent(event)) {
            return true;
        }
        if (this.mSession == null) {
            return false;
        }
        InputEvent copiedEvent = event.copy();
        return this.mSession.dispatchInputEvent(copiedEvent, copiedEvent, this.mFinishedInputEventCallback, this.mHandler) != 0;
    }

    @Override // android.view.View, android.view.ViewGroup
    public boolean dispatchTrackballEvent(MotionEvent event) {
        if (super.dispatchTrackballEvent(event)) {
            return true;
        }
        if (this.mSession == null) {
            return false;
        }
        InputEvent copiedEvent = event.copy();
        return this.mSession.dispatchInputEvent(copiedEvent, copiedEvent, this.mFinishedInputEventCallback, this.mHandler) != 0;
    }

    @Override // android.view.View
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        if (super.dispatchGenericMotionEvent(event)) {
            return true;
        }
        if (this.mSession == null) {
            return false;
        }
        InputEvent copiedEvent = event.copy();
        return this.mSession.dispatchInputEvent(copiedEvent, copiedEvent, this.mFinishedInputEventCallback, this.mHandler) != 0;
    }

    @Override // android.view.View, android.view.ViewGroup
    public void dispatchWindowFocusChanged(boolean hasFocus) {
        super.dispatchWindowFocusChanged(hasFocus);
        synchronized (sMainTvViewLock) {
            if (hasFocus) {
                if (this == sMainTvView.get() && this.mSession != null) {
                    this.mSession.setMain();
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        createSessionOverlayView();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onDetachedFromWindow() {
        removeSessionOverlayView();
        super.onDetachedFromWindow();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (this.mUseRequestedSurfaceLayout) {
            this.mSurfaceView.layout(this.mSurfaceViewLeft, this.mSurfaceViewTop, this.mSurfaceViewRight, this.mSurfaceViewBottom);
        } else {
            this.mSurfaceView.layout(0, 0, right - left, bottom - top);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        this.mSurfaceView.measure(widthMeasureSpec, heightMeasureSpec);
        int width = this.mSurfaceView.getMeasuredWidth();
        int height = this.mSurfaceView.getMeasuredHeight();
        int childState = this.mSurfaceView.getMeasuredState();
        setMeasuredDimension(resolveSizeAndState(width, widthMeasureSpec, childState), resolveSizeAndState(height, heightMeasureSpec, childState << 16));
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        this.mSurfaceView.setVisibility(visibility);
        if (visibility == 0) {
            createSessionOverlayView();
        } else {
            removeSessionOverlayView();
        }
    }

    private void resetSurfaceView() {
        if (this.mSurfaceView != null) {
            this.mSurfaceView.getHolder().removeCallback(this.mSurfaceHolderCallback);
            removeView(this.mSurfaceView);
        }
        this.mSurface = null;
        this.mSurfaceView = new SurfaceView(getContext(), this.mAttrs, this.mDefStyleAttr) {
            /* class android.media.tv.TvView.AnonymousClass3 */

            /* access modifiers changed from: protected */
            @Override // android.view.SurfaceView
            public void updateWindow(boolean force, boolean redrawNeeded) {
                super.updateWindow(force, redrawNeeded);
                TvView.this.relayoutSessionOverlayView();
            }
        };
        this.mSurfaceView.getHolder().addCallback(this.mSurfaceHolderCallback);
        if (this.mWindowZOrder == 1) {
            this.mSurfaceView.setZOrderMediaOverlay(true);
        } else if (this.mWindowZOrder == 2) {
            this.mSurfaceView.setZOrderOnTop(true);
        }
        addView(this.mSurfaceView);
    }

    private void release() {
        setSessionSurface(null);
        removeSessionOverlayView();
        this.mUseRequestedSurfaceLayout = false;
        this.mSession.release();
        this.mSession = null;
        this.mSessionCallback = null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setSessionSurface(Surface surface) {
        if (this.mSession != null) {
            this.mSession.setSurface(surface);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dispatchSurfaceChanged(int format, int width, int height) {
        if (this.mSession != null) {
            this.mSession.dispatchSurfaceChanged(format, width, height);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void createSessionOverlayView() {
        if (this.mSession != null && isAttachedToWindow() && !this.mOverlayViewCreated && this.mWindowZOrder == 0) {
            this.mOverlayViewFrame = getViewFrameOnScreen();
            this.mSession.createOverlayView(this, this.mOverlayViewFrame);
            this.mOverlayViewCreated = true;
        }
    }

    private void removeSessionOverlayView() {
        if (this.mSession != null && this.mOverlayViewCreated) {
            this.mSession.removeOverlayView();
            this.mOverlayViewCreated = false;
            this.mOverlayViewFrame = null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void relayoutSessionOverlayView() {
        if (this.mSession != null && isAttachedToWindow() && this.mOverlayViewCreated && this.mWindowZOrder == 0) {
            Rect viewFrame = getViewFrameOnScreen();
            if (!viewFrame.equals(this.mOverlayViewFrame)) {
                this.mSession.relayoutOverlayView(viewFrame);
                this.mOverlayViewFrame = viewFrame;
            }
        }
    }

    private Rect getViewFrameOnScreen() {
        int[] location = new int[2];
        getLocationOnScreen(location);
        return new Rect(location[0], location[1], location[0] + getWidth(), location[1] + getHeight());
    }

    public static abstract class TvInputCallback {
        public void onConnectionFailed(String inputId) {
        }

        public void onDisconnected(String inputId) {
        }

        public void onVideoSizeChanged(String inputId, int width, int height) {
        }

        public void onChannelRetuned(String inputId, Uri channelUri) {
        }

        public void onTracksChanged(String inputId, List<TvTrackInfo> list) {
        }

        public void onTrackSelected(String inputId, int type, String trackId) {
        }

        public void onVideoAvailable(String inputId) {
        }

        public void onVideoUnavailable(String inputId, int reason) {
        }

        public void onContentAllowed(String inputId) {
        }

        public void onContentBlocked(String inputId, TvContentRating rating) {
        }

        public void onEvent(String inputId, String eventType, Bundle eventArgs) {
        }
    }

    /* access modifiers changed from: private */
    public class MySessionCallback extends TvInputManager.SessionCallback {
        Uri mChannelUri;
        final String mInputId;
        Bundle mTuneParams;

        MySessionCallback(String inputId, Uri channelUri, Bundle tuneParams) {
            this.mInputId = inputId;
            this.mChannelUri = channelUri;
            this.mTuneParams = tuneParams;
        }

        @Override // android.media.tv.TvInputManager.SessionCallback
        public void onSessionCreated(TvInputManager.Session session) {
            boolean z = true;
            if (this == TvView.this.mSessionCallback) {
                TvView.this.mSession = session;
                if (session != null) {
                    synchronized (TvView.sMainTvViewLock) {
                        if (TvView.this.hasWindowFocus() && TvView.this == TvView.sMainTvView.get()) {
                            TvView.this.mSession.setMain();
                        }
                    }
                    if (TvView.this.mSurface != null) {
                        TvView.this.setSessionSurface(TvView.this.mSurface);
                        if (TvView.this.mSurfaceChanged) {
                            TvView.this.dispatchSurfaceChanged(TvView.this.mSurfaceFormat, TvView.this.mSurfaceWidth, TvView.this.mSurfaceHeight);
                        }
                    }
                    TvView.this.createSessionOverlayView();
                    if (TvView.this.mCaptionEnabled != 0) {
                        TvInputManager.Session session2 = TvView.this.mSession;
                        if (TvView.this.mCaptionEnabled != 1) {
                            z = false;
                        }
                        session2.setCaptionEnabled(z);
                    }
                    TvView.this.mSession.tune(this.mChannelUri, this.mTuneParams);
                    if (TvView.this.mHasStreamVolume) {
                        TvView.this.mSession.setStreamVolume(TvView.this.mStreamVolume);
                        return;
                    }
                    return;
                }
                TvView.this.mSessionCallback = null;
                if (TvView.this.mCallback != null) {
                    TvView.this.mCallback.onConnectionFailed(this.mInputId);
                }
            } else if (session != null) {
                session.release();
            }
        }

        @Override // android.media.tv.TvInputManager.SessionCallback
        public void onSessionReleased(TvInputManager.Session session) {
            if (this == TvView.this.mSessionCallback) {
                TvView.this.mOverlayViewCreated = false;
                TvView.this.mOverlayViewFrame = null;
                TvView.this.mSessionCallback = null;
                TvView.this.mSession = null;
                if (TvView.this.mCallback != null) {
                    TvView.this.mCallback.onDisconnected(this.mInputId);
                }
            }
        }

        @Override // android.media.tv.TvInputManager.SessionCallback
        public void onChannelRetuned(TvInputManager.Session session, Uri channelUri) {
            if (this == TvView.this.mSessionCallback && TvView.this.mCallback != null) {
                TvView.this.mCallback.onChannelRetuned(this.mInputId, channelUri);
            }
        }

        @Override // android.media.tv.TvInputManager.SessionCallback
        public void onTracksChanged(TvInputManager.Session session, List<TvTrackInfo> tracks) {
            if (this == TvView.this.mSessionCallback && TvView.this.mCallback != null) {
                TvView.this.mCallback.onTracksChanged(this.mInputId, tracks);
            }
        }

        @Override // android.media.tv.TvInputManager.SessionCallback
        public void onTrackSelected(TvInputManager.Session session, int type, String trackId) {
            if (this == TvView.this.mSessionCallback && TvView.this.mCallback != null) {
                TvView.this.mCallback.onTrackSelected(this.mInputId, type, trackId);
            }
        }

        @Override // android.media.tv.TvInputManager.SessionCallback
        public void onVideoAvailable(TvInputManager.Session session) {
            if (this == TvView.this.mSessionCallback && TvView.this.mCallback != null) {
                TvView.this.mCallback.onVideoAvailable(this.mInputId);
            }
        }

        @Override // android.media.tv.TvInputManager.SessionCallback
        public void onVideoUnavailable(TvInputManager.Session session, int reason) {
            if (this == TvView.this.mSessionCallback && TvView.this.mCallback != null) {
                TvView.this.mCallback.onVideoUnavailable(this.mInputId, reason);
            }
        }

        @Override // android.media.tv.TvInputManager.SessionCallback
        public void onContentAllowed(TvInputManager.Session session) {
            if (this == TvView.this.mSessionCallback && TvView.this.mCallback != null) {
                TvView.this.mCallback.onContentAllowed(this.mInputId);
            }
        }

        @Override // android.media.tv.TvInputManager.SessionCallback
        public void onContentBlocked(TvInputManager.Session session, TvContentRating rating) {
            if (this == TvView.this.mSessionCallback && TvView.this.mCallback != null) {
                TvView.this.mCallback.onContentBlocked(this.mInputId, rating);
            }
        }

        @Override // android.media.tv.TvInputManager.SessionCallback
        public void onLayoutSurface(TvInputManager.Session session, int left, int top, int right, int bottom) {
            if (this == TvView.this.mSessionCallback) {
                TvView.this.mSurfaceViewLeft = left;
                TvView.this.mSurfaceViewTop = top;
                TvView.this.mSurfaceViewRight = right;
                TvView.this.mSurfaceViewBottom = bottom;
                TvView.this.mUseRequestedSurfaceLayout = true;
                TvView.this.requestLayout();
            }
        }

        @Override // android.media.tv.TvInputManager.SessionCallback
        public void onSessionEvent(TvInputManager.Session session, String eventType, Bundle eventArgs) {
            if (this == TvView.this.mSessionCallback && TvView.this.mCallback != null) {
                TvView.this.mCallback.onEvent(this.mInputId, eventType, eventArgs);
            }
        }
    }
}

package android.app;

import android.app.IActivityContainerCallback;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.SurfaceTexture;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.InputEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import dalvik.system.CloseGuard;
import java.lang.ref.WeakReference;

public class ActivityView extends ViewGroup {
    private static final boolean DEBUG = false;
    private static final String TAG = "ActivityView";
    private Activity mActivity;
    private ActivityContainerWrapper mActivityContainer;
    private ActivityViewCallback mActivityViewCallback;
    private int mHeight;
    private int mLastVisibility;
    DisplayMetrics mMetrics;
    Intent mQueuedIntent;
    IIntentSender mQueuedPendingIntent;
    private Surface mSurface;
    private final TextureView mTextureView;
    private int mWidth;

    public static abstract class ActivityViewCallback {
        public abstract void onAllActivitiesComplete(ActivityView activityView);
    }

    public ActivityView(Context context) {
        this(context, null);
    }

    public ActivityView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ActivityView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        while (true) {
            if (!(context instanceof ContextWrapper)) {
                break;
            } else if (context instanceof Activity) {
                this.mActivity = (Activity) context;
                break;
            } else {
                context = ((ContextWrapper) context).getBaseContext();
            }
        }
        if (this.mActivity == null) {
            throw new IllegalStateException("The ActivityView's Context is not an Activity.");
        }
        try {
            this.mActivityContainer = new ActivityContainerWrapper(ActivityManagerNative.getDefault().createActivityContainer(this.mActivity.getActivityToken(), new ActivityContainerCallback(this)));
            this.mTextureView = new TextureView(context);
            this.mTextureView.setSurfaceTextureListener(new ActivityViewSurfaceTextureListener());
            addView(this.mTextureView);
            this.mMetrics = new DisplayMetrics();
            ((WindowManager) this.mActivity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(this.mMetrics);
            this.mLastVisibility = getVisibility();
        } catch (RemoteException e) {
            throw new RuntimeException("ActivityView: Unable to create ActivityContainer. " + e);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        this.mTextureView.layout(0, 0, r - l, b - t);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (this.mSurface != null) {
            if (visibility == 8) {
                try {
                    this.mActivityContainer.setSurface(null, this.mWidth, this.mHeight, this.mMetrics.densityDpi);
                } catch (RemoteException e) {
                    throw new RuntimeException("ActivityView: Unable to set surface of ActivityContainer. " + e);
                }
            } else if (this.mLastVisibility == 8) {
                this.mActivityContainer.setSurface(this.mSurface, this.mWidth, this.mHeight, this.mMetrics.densityDpi);
            }
        }
        this.mLastVisibility = visibility;
    }

    private boolean injectInputEvent(InputEvent event) {
        return this.mActivityContainer != null && this.mActivityContainer.injectEvent(event);
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent event) {
        return injectInputEvent(event) || super.onTouchEvent(event);
    }

    @Override // android.view.View
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (!event.isFromSource(2) || !injectInputEvent(event)) {
            return super.onGenericMotionEvent(event);
        }
        return true;
    }

    @Override // android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
    }

    @Override // android.view.View, android.view.ViewGroup
    public void onDetachedFromWindow() {
    }

    public boolean isAttachedToDisplay() {
        return this.mSurface != null;
    }

    public void startActivity(Intent intent) {
        if (this.mActivityContainer == null) {
            throw new IllegalStateException("Attempt to call startActivity after release");
        } else if (this.mSurface != null) {
            this.mActivityContainer.startActivity(intent);
        } else {
            this.mActivityContainer.checkEmbeddedAllowed(intent);
            this.mQueuedIntent = intent;
            this.mQueuedPendingIntent = null;
        }
    }

    public void startActivity(IntentSender intentSender) {
        if (this.mActivityContainer == null) {
            throw new IllegalStateException("Attempt to call startActivity after release");
        }
        IIntentSender iIntentSender = intentSender.getTarget();
        if (this.mSurface != null) {
            this.mActivityContainer.startActivityIntentSender(iIntentSender);
            return;
        }
        this.mActivityContainer.checkEmbeddedAllowedIntentSender(iIntentSender);
        this.mQueuedPendingIntent = iIntentSender;
        this.mQueuedIntent = null;
    }

    public void startActivity(PendingIntent pendingIntent) {
        if (this.mActivityContainer == null) {
            throw new IllegalStateException("Attempt to call startActivity after release");
        }
        IIntentSender iIntentSender = pendingIntent.getTarget();
        if (this.mSurface != null) {
            this.mActivityContainer.startActivityIntentSender(iIntentSender);
            return;
        }
        this.mActivityContainer.checkEmbeddedAllowedIntentSender(iIntentSender);
        this.mQueuedPendingIntent = iIntentSender;
        this.mQueuedIntent = null;
    }

    public void release() {
        if (this.mActivityContainer == null) {
            Log.e(TAG, "Duplicate call to release");
            return;
        }
        this.mActivityContainer.release();
        this.mActivityContainer = null;
        if (this.mSurface != null) {
            this.mSurface.release();
            this.mSurface = null;
        }
        this.mTextureView.setSurfaceTextureListener(null);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void attachToSurfaceWhenReady() {
        SurfaceTexture surfaceTexture = this.mTextureView.getSurfaceTexture();
        if (surfaceTexture != null && this.mSurface == null) {
            this.mSurface = new Surface(surfaceTexture);
            try {
                this.mActivityContainer.setSurface(this.mSurface, this.mWidth, this.mHeight, this.mMetrics.densityDpi);
                if (this.mQueuedIntent != null) {
                    this.mActivityContainer.startActivity(this.mQueuedIntent);
                    this.mQueuedIntent = null;
                } else if (this.mQueuedPendingIntent != null) {
                    this.mActivityContainer.startActivityIntentSender(this.mQueuedPendingIntent);
                    this.mQueuedPendingIntent = null;
                }
            } catch (RemoteException e) {
                this.mSurface.release();
                this.mSurface = null;
                throw new RuntimeException("ActivityView: Unable to create ActivityContainer. " + e);
            }
        }
    }

    public void setCallback(ActivityViewCallback callback) {
        this.mActivityViewCallback = callback;
    }

    private class ActivityViewSurfaceTextureListener implements TextureView.SurfaceTextureListener {
        private ActivityViewSurfaceTextureListener() {
        }

        @Override // android.view.TextureView.SurfaceTextureListener
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            if (ActivityView.this.mActivityContainer != null) {
                ActivityView.this.mWidth = width;
                ActivityView.this.mHeight = height;
                ActivityView.this.attachToSurfaceWhenReady();
            }
        }

        @Override // android.view.TextureView.SurfaceTextureListener
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
            if (ActivityView.this.mActivityContainer == null) {
            }
        }

        @Override // android.view.TextureView.SurfaceTextureListener
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            if (ActivityView.this.mActivityContainer != null) {
                ActivityView.this.mSurface.release();
                ActivityView.this.mSurface = null;
                try {
                    ActivityView.this.mActivityContainer.setSurface(null, ActivityView.this.mWidth, ActivityView.this.mHeight, ActivityView.this.mMetrics.densityDpi);
                } catch (RemoteException e) {
                    throw new RuntimeException("ActivityView: Unable to set surface of ActivityContainer. " + e);
                }
            }
            return true;
        }

        @Override // android.view.TextureView.SurfaceTextureListener
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        }
    }

    private static class ActivityContainerCallback extends IActivityContainerCallback.Stub {
        private final WeakReference<ActivityView> mActivityViewWeakReference;

        ActivityContainerCallback(ActivityView activityView) {
            this.mActivityViewWeakReference = new WeakReference<>(activityView);
        }

        @Override // android.app.IActivityContainerCallback
        public void setVisible(IBinder container, boolean visible) {
        }

        /* JADX WARNING: Code restructure failed: missing block: B:2:0x000a, code lost:
            r1 = r0.mActivityViewCallback;
         */
        @Override // android.app.IActivityContainerCallback
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onAllActivitiesComplete(android.os.IBinder r4) {
            /*
                r3 = this;
                java.lang.ref.WeakReference<android.app.ActivityView> r2 = r3.mActivityViewWeakReference
                java.lang.Object r0 = r2.get()
                android.app.ActivityView r0 = (android.app.ActivityView) r0
                if (r0 == 0) goto L_0x0018
                android.app.ActivityView$ActivityViewCallback r1 = android.app.ActivityView.access$600(r0)
                if (r1 == 0) goto L_0x0018
                android.app.ActivityView$ActivityContainerCallback$1 r2 = new android.app.ActivityView$ActivityContainerCallback$1
                r2.<init>(r1, r0)
                r0.post(r2)
            L_0x0018:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.ActivityView.ActivityContainerCallback.onAllActivitiesComplete(android.os.IBinder):void");
        }
    }

    /* access modifiers changed from: private */
    public static class ActivityContainerWrapper {
        private final CloseGuard mGuard = CloseGuard.get();
        private final IActivityContainer mIActivityContainer;
        boolean mOpened;

        ActivityContainerWrapper(IActivityContainer container) {
            this.mIActivityContainer = container;
            this.mOpened = true;
            this.mGuard.open("release");
        }

        /* access modifiers changed from: package-private */
        public void attachToDisplay(int displayId) {
            try {
                this.mIActivityContainer.attachToDisplay(displayId);
            } catch (RemoteException e) {
            }
        }

        /* access modifiers changed from: package-private */
        public void setSurface(Surface surface, int width, int height, int density) throws RemoteException {
            this.mIActivityContainer.setSurface(surface, width, height, density);
        }

        /* access modifiers changed from: package-private */
        public int startActivity(Intent intent) {
            try {
                return this.mIActivityContainer.startActivity(intent);
            } catch (RemoteException e) {
                throw new RuntimeException("ActivityView: Unable to startActivity. " + e);
            }
        }

        /* access modifiers changed from: package-private */
        public int startActivityIntentSender(IIntentSender intentSender) {
            try {
                return this.mIActivityContainer.startActivityIntentSender(intentSender);
            } catch (RemoteException e) {
                throw new RuntimeException("ActivityView: Unable to startActivity from IntentSender. " + e);
            }
        }

        /* access modifiers changed from: package-private */
        public void checkEmbeddedAllowed(Intent intent) {
            try {
                this.mIActivityContainer.checkEmbeddedAllowed(intent);
            } catch (RemoteException e) {
                throw new RuntimeException("ActivityView: Unable to startActivity from Intent. " + e);
            }
        }

        /* access modifiers changed from: package-private */
        public void checkEmbeddedAllowedIntentSender(IIntentSender intentSender) {
            try {
                this.mIActivityContainer.checkEmbeddedAllowedIntentSender(intentSender);
            } catch (RemoteException e) {
                throw new RuntimeException("ActivityView: Unable to startActivity from IntentSender. " + e);
            }
        }

        /* access modifiers changed from: package-private */
        public int getDisplayId() {
            try {
                return this.mIActivityContainer.getDisplayId();
            } catch (RemoteException e) {
                return -1;
            }
        }

        /* access modifiers changed from: package-private */
        public boolean injectEvent(InputEvent event) {
            try {
                return this.mIActivityContainer.injectEvent(event);
            } catch (RemoteException e) {
                return false;
            }
        }

        /* access modifiers changed from: package-private */
        public void release() {
            synchronized (this.mGuard) {
                if (this.mOpened) {
                    try {
                        this.mIActivityContainer.release();
                        this.mGuard.close();
                    } catch (RemoteException e) {
                    }
                    this.mOpened = false;
                }
            }
        }

        /* access modifiers changed from: protected */
        public void finalize() throws Throwable {
            try {
                if (this.mGuard != null) {
                    this.mGuard.warnIfOpen();
                    release();
                }
            } finally {
                super.finalize();
            }
        }
    }
}

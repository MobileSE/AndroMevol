package android.view;

import android.content.Context;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import com.android.internal.view.BaseIWindow;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class SurfaceView extends View {
    private static final boolean DEBUG = false;
    static final int GET_NEW_SURFACE_MSG = 2;
    static final int KEEP_SCREEN_ON_MSG = 1;
    private static final String TAG = "SurfaceView";
    static final int UPDATE_WINDOW_MSG = 3;
    final ArrayList<SurfaceHolder.Callback> mCallbacks;
    final Configuration mConfiguration;
    final Rect mContentInsets;
    private final ViewTreeObserver.OnPreDrawListener mDrawListener;
    boolean mDrawingStopped;
    int mFormat;
    private boolean mGlobalListenersAdded;
    final Handler mHandler;
    boolean mHaveFrame;
    int mHeight;
    boolean mIsCreating;
    long mLastLockTime;
    int mLastSurfaceHeight;
    int mLastSurfaceWidth;
    final WindowManager.LayoutParams mLayout;
    int mLeft;
    final int[] mLocation;
    final Surface mNewSurface;
    final Rect mOverscanInsets;
    boolean mReportDrawNeeded;
    int mRequestedFormat;
    int mRequestedHeight;
    boolean mRequestedVisible;
    int mRequestedWidth;
    final ViewTreeObserver.OnScrollChangedListener mScrollChangedListener;
    IWindowSession mSession;
    final Rect mStableInsets;
    final Surface mSurface;
    boolean mSurfaceCreated;
    final Rect mSurfaceFrame;
    private final SurfaceHolder mSurfaceHolder;
    final ReentrantLock mSurfaceLock;
    int mTop;
    private CompatibilityInfo.Translator mTranslator;
    boolean mUpdateWindowNeeded;
    boolean mViewVisibility;
    boolean mVisible;
    final Rect mVisibleInsets;
    int mWidth;
    final Rect mWinFrame;
    MyWindow mWindow;
    int mWindowType;
    boolean mWindowVisibility;

    public SurfaceView(Context context) {
        super(context);
        this.mCallbacks = new ArrayList<>();
        this.mLocation = new int[2];
        this.mSurfaceLock = new ReentrantLock();
        this.mSurface = new Surface();
        this.mNewSurface = new Surface();
        this.mDrawingStopped = true;
        this.mLayout = new WindowManager.LayoutParams();
        this.mVisibleInsets = new Rect();
        this.mWinFrame = new Rect();
        this.mOverscanInsets = new Rect();
        this.mContentInsets = new Rect();
        this.mStableInsets = new Rect();
        this.mConfiguration = new Configuration();
        this.mWindowType = 1001;
        this.mIsCreating = false;
        this.mHandler = new Handler() {
            /* class android.view.SurfaceView.AnonymousClass1 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                boolean z = false;
                switch (msg.what) {
                    case 1:
                        SurfaceView surfaceView = SurfaceView.this;
                        if (msg.arg1 != 0) {
                            z = true;
                        }
                        surfaceView.setKeepScreenOn(z);
                        return;
                    case 2:
                        SurfaceView.this.handleGetNewSurface();
                        return;
                    case 3:
                        SurfaceView.this.updateWindow(false, false);
                        return;
                    default:
                        return;
                }
            }
        };
        this.mScrollChangedListener = new ViewTreeObserver.OnScrollChangedListener() {
            /* class android.view.SurfaceView.AnonymousClass2 */

            @Override // android.view.ViewTreeObserver.OnScrollChangedListener
            public void onScrollChanged() {
                SurfaceView.this.updateWindow(false, false);
            }
        };
        this.mRequestedVisible = false;
        this.mWindowVisibility = false;
        this.mViewVisibility = false;
        this.mRequestedWidth = -1;
        this.mRequestedHeight = -1;
        this.mRequestedFormat = 4;
        this.mHaveFrame = false;
        this.mSurfaceCreated = false;
        this.mLastLockTime = 0;
        this.mVisible = false;
        this.mLeft = -1;
        this.mTop = -1;
        this.mWidth = -1;
        this.mHeight = -1;
        this.mFormat = -1;
        this.mSurfaceFrame = new Rect();
        this.mLastSurfaceWidth = -1;
        this.mLastSurfaceHeight = -1;
        this.mDrawListener = new ViewTreeObserver.OnPreDrawListener() {
            /* class android.view.SurfaceView.AnonymousClass3 */

            @Override // android.view.ViewTreeObserver.OnPreDrawListener
            public boolean onPreDraw() {
                boolean z;
                SurfaceView surfaceView = SurfaceView.this;
                if (SurfaceView.this.getWidth() <= 0 || SurfaceView.this.getHeight() <= 0) {
                    z = false;
                } else {
                    z = true;
                }
                surfaceView.mHaveFrame = z;
                SurfaceView.this.updateWindow(false, false);
                return true;
            }
        };
        this.mSurfaceHolder = new SurfaceHolder() {
            /* class android.view.SurfaceView.AnonymousClass4 */
            private static final String LOG_TAG = "SurfaceHolder";

            @Override // android.view.SurfaceHolder
            public boolean isCreating() {
                return SurfaceView.this.mIsCreating;
            }

            @Override // android.view.SurfaceHolder
            public void addCallback(SurfaceHolder.Callback callback) {
                synchronized (SurfaceView.this.mCallbacks) {
                    if (!SurfaceView.this.mCallbacks.contains(callback)) {
                        SurfaceView.this.mCallbacks.add(callback);
                    }
                }
            }

            @Override // android.view.SurfaceHolder
            public void removeCallback(SurfaceHolder.Callback callback) {
                synchronized (SurfaceView.this.mCallbacks) {
                    SurfaceView.this.mCallbacks.remove(callback);
                }
            }

            @Override // android.view.SurfaceHolder
            public void setFixedSize(int width, int height) {
                if (SurfaceView.this.mRequestedWidth != width || SurfaceView.this.mRequestedHeight != height) {
                    SurfaceView.this.mRequestedWidth = width;
                    SurfaceView.this.mRequestedHeight = height;
                    SurfaceView.this.requestLayout();
                }
            }

            @Override // android.view.SurfaceHolder
            public void setSizeFromLayout() {
                if (SurfaceView.this.mRequestedWidth != -1 || SurfaceView.this.mRequestedHeight != -1) {
                    SurfaceView surfaceView = SurfaceView.this;
                    SurfaceView.this.mRequestedHeight = -1;
                    surfaceView.mRequestedWidth = -1;
                    SurfaceView.this.requestLayout();
                }
            }

            @Override // android.view.SurfaceHolder
            public void setFormat(int format) {
                if (format == -1) {
                    format = 4;
                }
                SurfaceView.this.mRequestedFormat = format;
                if (SurfaceView.this.mWindow != null) {
                    SurfaceView.this.updateWindow(false, false);
                }
            }

            @Override // android.view.SurfaceHolder
            @Deprecated
            public void setType(int type) {
            }

            @Override // android.view.SurfaceHolder
            public void setKeepScreenOn(boolean screenOn) {
                int i = 1;
                Message msg = SurfaceView.this.mHandler.obtainMessage(1);
                if (!screenOn) {
                    i = 0;
                }
                msg.arg1 = i;
                SurfaceView.this.mHandler.sendMessage(msg);
            }

            @Override // android.view.SurfaceHolder
            public Canvas lockCanvas() {
                return internalLockCanvas(null);
            }

            @Override // android.view.SurfaceHolder
            public Canvas lockCanvas(Rect inOutDirty) {
                return internalLockCanvas(inOutDirty);
            }

            private final Canvas internalLockCanvas(Rect dirty) {
                SurfaceView.this.mSurfaceLock.lock();
                Canvas c = null;
                if (!SurfaceView.this.mDrawingStopped && SurfaceView.this.mWindow != null) {
                    try {
                        c = SurfaceView.this.mSurface.lockCanvas(dirty);
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Exception locking surface", e);
                    }
                }
                if (c != null) {
                    SurfaceView.this.mLastLockTime = SystemClock.uptimeMillis();
                    return c;
                }
                long now = SystemClock.uptimeMillis();
                long nextTime = SurfaceView.this.mLastLockTime + 100;
                if (nextTime > now) {
                    try {
                        Thread.sleep(nextTime - now);
                    } catch (InterruptedException e2) {
                    }
                    now = SystemClock.uptimeMillis();
                }
                SurfaceView.this.mLastLockTime = now;
                SurfaceView.this.mSurfaceLock.unlock();
                return null;
            }

            @Override // android.view.SurfaceHolder
            public void unlockCanvasAndPost(Canvas canvas) {
                SurfaceView.this.mSurface.unlockCanvasAndPost(canvas);
                SurfaceView.this.mSurfaceLock.unlock();
            }

            @Override // android.view.SurfaceHolder
            public Surface getSurface() {
                return SurfaceView.this.mSurface;
            }

            @Override // android.view.SurfaceHolder
            public Rect getSurfaceFrame() {
                return SurfaceView.this.mSurfaceFrame;
            }
        };
        init();
    }

    public SurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mCallbacks = new ArrayList<>();
        this.mLocation = new int[2];
        this.mSurfaceLock = new ReentrantLock();
        this.mSurface = new Surface();
        this.mNewSurface = new Surface();
        this.mDrawingStopped = true;
        this.mLayout = new WindowManager.LayoutParams();
        this.mVisibleInsets = new Rect();
        this.mWinFrame = new Rect();
        this.mOverscanInsets = new Rect();
        this.mContentInsets = new Rect();
        this.mStableInsets = new Rect();
        this.mConfiguration = new Configuration();
        this.mWindowType = 1001;
        this.mIsCreating = false;
        this.mHandler = new Handler() {
            /* class android.view.SurfaceView.AnonymousClass1 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                boolean z = false;
                switch (msg.what) {
                    case 1:
                        SurfaceView surfaceView = SurfaceView.this;
                        if (msg.arg1 != 0) {
                            z = true;
                        }
                        surfaceView.setKeepScreenOn(z);
                        return;
                    case 2:
                        SurfaceView.this.handleGetNewSurface();
                        return;
                    case 3:
                        SurfaceView.this.updateWindow(false, false);
                        return;
                    default:
                        return;
                }
            }
        };
        this.mScrollChangedListener = new ViewTreeObserver.OnScrollChangedListener() {
            /* class android.view.SurfaceView.AnonymousClass2 */

            @Override // android.view.ViewTreeObserver.OnScrollChangedListener
            public void onScrollChanged() {
                SurfaceView.this.updateWindow(false, false);
            }
        };
        this.mRequestedVisible = false;
        this.mWindowVisibility = false;
        this.mViewVisibility = false;
        this.mRequestedWidth = -1;
        this.mRequestedHeight = -1;
        this.mRequestedFormat = 4;
        this.mHaveFrame = false;
        this.mSurfaceCreated = false;
        this.mLastLockTime = 0;
        this.mVisible = false;
        this.mLeft = -1;
        this.mTop = -1;
        this.mWidth = -1;
        this.mHeight = -1;
        this.mFormat = -1;
        this.mSurfaceFrame = new Rect();
        this.mLastSurfaceWidth = -1;
        this.mLastSurfaceHeight = -1;
        this.mDrawListener = new ViewTreeObserver.OnPreDrawListener() {
            /* class android.view.SurfaceView.AnonymousClass3 */

            @Override // android.view.ViewTreeObserver.OnPreDrawListener
            public boolean onPreDraw() {
                boolean z;
                SurfaceView surfaceView = SurfaceView.this;
                if (SurfaceView.this.getWidth() <= 0 || SurfaceView.this.getHeight() <= 0) {
                    z = false;
                } else {
                    z = true;
                }
                surfaceView.mHaveFrame = z;
                SurfaceView.this.updateWindow(false, false);
                return true;
            }
        };
        this.mSurfaceHolder = new SurfaceHolder() {
            /* class android.view.SurfaceView.AnonymousClass4 */
            private static final String LOG_TAG = "SurfaceHolder";

            @Override // android.view.SurfaceHolder
            public boolean isCreating() {
                return SurfaceView.this.mIsCreating;
            }

            @Override // android.view.SurfaceHolder
            public void addCallback(SurfaceHolder.Callback callback) {
                synchronized (SurfaceView.this.mCallbacks) {
                    if (!SurfaceView.this.mCallbacks.contains(callback)) {
                        SurfaceView.this.mCallbacks.add(callback);
                    }
                }
            }

            @Override // android.view.SurfaceHolder
            public void removeCallback(SurfaceHolder.Callback callback) {
                synchronized (SurfaceView.this.mCallbacks) {
                    SurfaceView.this.mCallbacks.remove(callback);
                }
            }

            @Override // android.view.SurfaceHolder
            public void setFixedSize(int width, int height) {
                if (SurfaceView.this.mRequestedWidth != width || SurfaceView.this.mRequestedHeight != height) {
                    SurfaceView.this.mRequestedWidth = width;
                    SurfaceView.this.mRequestedHeight = height;
                    SurfaceView.this.requestLayout();
                }
            }

            @Override // android.view.SurfaceHolder
            public void setSizeFromLayout() {
                if (SurfaceView.this.mRequestedWidth != -1 || SurfaceView.this.mRequestedHeight != -1) {
                    SurfaceView surfaceView = SurfaceView.this;
                    SurfaceView.this.mRequestedHeight = -1;
                    surfaceView.mRequestedWidth = -1;
                    SurfaceView.this.requestLayout();
                }
            }

            @Override // android.view.SurfaceHolder
            public void setFormat(int format) {
                if (format == -1) {
                    format = 4;
                }
                SurfaceView.this.mRequestedFormat = format;
                if (SurfaceView.this.mWindow != null) {
                    SurfaceView.this.updateWindow(false, false);
                }
            }

            @Override // android.view.SurfaceHolder
            @Deprecated
            public void setType(int type) {
            }

            @Override // android.view.SurfaceHolder
            public void setKeepScreenOn(boolean screenOn) {
                int i = 1;
                Message msg = SurfaceView.this.mHandler.obtainMessage(1);
                if (!screenOn) {
                    i = 0;
                }
                msg.arg1 = i;
                SurfaceView.this.mHandler.sendMessage(msg);
            }

            @Override // android.view.SurfaceHolder
            public Canvas lockCanvas() {
                return internalLockCanvas(null);
            }

            @Override // android.view.SurfaceHolder
            public Canvas lockCanvas(Rect inOutDirty) {
                return internalLockCanvas(inOutDirty);
            }

            private final Canvas internalLockCanvas(Rect dirty) {
                SurfaceView.this.mSurfaceLock.lock();
                Canvas c = null;
                if (!SurfaceView.this.mDrawingStopped && SurfaceView.this.mWindow != null) {
                    try {
                        c = SurfaceView.this.mSurface.lockCanvas(dirty);
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Exception locking surface", e);
                    }
                }
                if (c != null) {
                    SurfaceView.this.mLastLockTime = SystemClock.uptimeMillis();
                    return c;
                }
                long now = SystemClock.uptimeMillis();
                long nextTime = SurfaceView.this.mLastLockTime + 100;
                if (nextTime > now) {
                    try {
                        Thread.sleep(nextTime - now);
                    } catch (InterruptedException e2) {
                    }
                    now = SystemClock.uptimeMillis();
                }
                SurfaceView.this.mLastLockTime = now;
                SurfaceView.this.mSurfaceLock.unlock();
                return null;
            }

            @Override // android.view.SurfaceHolder
            public void unlockCanvasAndPost(Canvas canvas) {
                SurfaceView.this.mSurface.unlockCanvasAndPost(canvas);
                SurfaceView.this.mSurfaceLock.unlock();
            }

            @Override // android.view.SurfaceHolder
            public Surface getSurface() {
                return SurfaceView.this.mSurface;
            }

            @Override // android.view.SurfaceHolder
            public Rect getSurfaceFrame() {
                return SurfaceView.this.mSurfaceFrame;
            }
        };
        init();
    }

    public SurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mCallbacks = new ArrayList<>();
        this.mLocation = new int[2];
        this.mSurfaceLock = new ReentrantLock();
        this.mSurface = new Surface();
        this.mNewSurface = new Surface();
        this.mDrawingStopped = true;
        this.mLayout = new WindowManager.LayoutParams();
        this.mVisibleInsets = new Rect();
        this.mWinFrame = new Rect();
        this.mOverscanInsets = new Rect();
        this.mContentInsets = new Rect();
        this.mStableInsets = new Rect();
        this.mConfiguration = new Configuration();
        this.mWindowType = 1001;
        this.mIsCreating = false;
        this.mHandler = new Handler() {
            /* class android.view.SurfaceView.AnonymousClass1 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                boolean z = false;
                switch (msg.what) {
                    case 1:
                        SurfaceView surfaceView = SurfaceView.this;
                        if (msg.arg1 != 0) {
                            z = true;
                        }
                        surfaceView.setKeepScreenOn(z);
                        return;
                    case 2:
                        SurfaceView.this.handleGetNewSurface();
                        return;
                    case 3:
                        SurfaceView.this.updateWindow(false, false);
                        return;
                    default:
                        return;
                }
            }
        };
        this.mScrollChangedListener = new ViewTreeObserver.OnScrollChangedListener() {
            /* class android.view.SurfaceView.AnonymousClass2 */

            @Override // android.view.ViewTreeObserver.OnScrollChangedListener
            public void onScrollChanged() {
                SurfaceView.this.updateWindow(false, false);
            }
        };
        this.mRequestedVisible = false;
        this.mWindowVisibility = false;
        this.mViewVisibility = false;
        this.mRequestedWidth = -1;
        this.mRequestedHeight = -1;
        this.mRequestedFormat = 4;
        this.mHaveFrame = false;
        this.mSurfaceCreated = false;
        this.mLastLockTime = 0;
        this.mVisible = false;
        this.mLeft = -1;
        this.mTop = -1;
        this.mWidth = -1;
        this.mHeight = -1;
        this.mFormat = -1;
        this.mSurfaceFrame = new Rect();
        this.mLastSurfaceWidth = -1;
        this.mLastSurfaceHeight = -1;
        this.mDrawListener = new ViewTreeObserver.OnPreDrawListener() {
            /* class android.view.SurfaceView.AnonymousClass3 */

            @Override // android.view.ViewTreeObserver.OnPreDrawListener
            public boolean onPreDraw() {
                boolean z;
                SurfaceView surfaceView = SurfaceView.this;
                if (SurfaceView.this.getWidth() <= 0 || SurfaceView.this.getHeight() <= 0) {
                    z = false;
                } else {
                    z = true;
                }
                surfaceView.mHaveFrame = z;
                SurfaceView.this.updateWindow(false, false);
                return true;
            }
        };
        this.mSurfaceHolder = new SurfaceHolder() {
            /* class android.view.SurfaceView.AnonymousClass4 */
            private static final String LOG_TAG = "SurfaceHolder";

            @Override // android.view.SurfaceHolder
            public boolean isCreating() {
                return SurfaceView.this.mIsCreating;
            }

            @Override // android.view.SurfaceHolder
            public void addCallback(SurfaceHolder.Callback callback) {
                synchronized (SurfaceView.this.mCallbacks) {
                    if (!SurfaceView.this.mCallbacks.contains(callback)) {
                        SurfaceView.this.mCallbacks.add(callback);
                    }
                }
            }

            @Override // android.view.SurfaceHolder
            public void removeCallback(SurfaceHolder.Callback callback) {
                synchronized (SurfaceView.this.mCallbacks) {
                    SurfaceView.this.mCallbacks.remove(callback);
                }
            }

            @Override // android.view.SurfaceHolder
            public void setFixedSize(int width, int height) {
                if (SurfaceView.this.mRequestedWidth != width || SurfaceView.this.mRequestedHeight != height) {
                    SurfaceView.this.mRequestedWidth = width;
                    SurfaceView.this.mRequestedHeight = height;
                    SurfaceView.this.requestLayout();
                }
            }

            @Override // android.view.SurfaceHolder
            public void setSizeFromLayout() {
                if (SurfaceView.this.mRequestedWidth != -1 || SurfaceView.this.mRequestedHeight != -1) {
                    SurfaceView surfaceView = SurfaceView.this;
                    SurfaceView.this.mRequestedHeight = -1;
                    surfaceView.mRequestedWidth = -1;
                    SurfaceView.this.requestLayout();
                }
            }

            @Override // android.view.SurfaceHolder
            public void setFormat(int format) {
                if (format == -1) {
                    format = 4;
                }
                SurfaceView.this.mRequestedFormat = format;
                if (SurfaceView.this.mWindow != null) {
                    SurfaceView.this.updateWindow(false, false);
                }
            }

            @Override // android.view.SurfaceHolder
            @Deprecated
            public void setType(int type) {
            }

            @Override // android.view.SurfaceHolder
            public void setKeepScreenOn(boolean screenOn) {
                int i = 1;
                Message msg = SurfaceView.this.mHandler.obtainMessage(1);
                if (!screenOn) {
                    i = 0;
                }
                msg.arg1 = i;
                SurfaceView.this.mHandler.sendMessage(msg);
            }

            @Override // android.view.SurfaceHolder
            public Canvas lockCanvas() {
                return internalLockCanvas(null);
            }

            @Override // android.view.SurfaceHolder
            public Canvas lockCanvas(Rect inOutDirty) {
                return internalLockCanvas(inOutDirty);
            }

            private final Canvas internalLockCanvas(Rect dirty) {
                SurfaceView.this.mSurfaceLock.lock();
                Canvas c = null;
                if (!SurfaceView.this.mDrawingStopped && SurfaceView.this.mWindow != null) {
                    try {
                        c = SurfaceView.this.mSurface.lockCanvas(dirty);
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Exception locking surface", e);
                    }
                }
                if (c != null) {
                    SurfaceView.this.mLastLockTime = SystemClock.uptimeMillis();
                    return c;
                }
                long now = SystemClock.uptimeMillis();
                long nextTime = SurfaceView.this.mLastLockTime + 100;
                if (nextTime > now) {
                    try {
                        Thread.sleep(nextTime - now);
                    } catch (InterruptedException e2) {
                    }
                    now = SystemClock.uptimeMillis();
                }
                SurfaceView.this.mLastLockTime = now;
                SurfaceView.this.mSurfaceLock.unlock();
                return null;
            }

            @Override // android.view.SurfaceHolder
            public void unlockCanvasAndPost(Canvas canvas) {
                SurfaceView.this.mSurface.unlockCanvasAndPost(canvas);
                SurfaceView.this.mSurfaceLock.unlock();
            }

            @Override // android.view.SurfaceHolder
            public Surface getSurface() {
                return SurfaceView.this.mSurface;
            }

            @Override // android.view.SurfaceHolder
            public Rect getSurfaceFrame() {
                return SurfaceView.this.mSurfaceFrame;
            }
        };
        init();
    }

    public SurfaceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mCallbacks = new ArrayList<>();
        this.mLocation = new int[2];
        this.mSurfaceLock = new ReentrantLock();
        this.mSurface = new Surface();
        this.mNewSurface = new Surface();
        this.mDrawingStopped = true;
        this.mLayout = new WindowManager.LayoutParams();
        this.mVisibleInsets = new Rect();
        this.mWinFrame = new Rect();
        this.mOverscanInsets = new Rect();
        this.mContentInsets = new Rect();
        this.mStableInsets = new Rect();
        this.mConfiguration = new Configuration();
        this.mWindowType = 1001;
        this.mIsCreating = false;
        this.mHandler = new Handler() {
            /* class android.view.SurfaceView.AnonymousClass1 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                boolean z = false;
                switch (msg.what) {
                    case 1:
                        SurfaceView surfaceView = SurfaceView.this;
                        if (msg.arg1 != 0) {
                            z = true;
                        }
                        surfaceView.setKeepScreenOn(z);
                        return;
                    case 2:
                        SurfaceView.this.handleGetNewSurface();
                        return;
                    case 3:
                        SurfaceView.this.updateWindow(false, false);
                        return;
                    default:
                        return;
                }
            }
        };
        this.mScrollChangedListener = new ViewTreeObserver.OnScrollChangedListener() {
            /* class android.view.SurfaceView.AnonymousClass2 */

            @Override // android.view.ViewTreeObserver.OnScrollChangedListener
            public void onScrollChanged() {
                SurfaceView.this.updateWindow(false, false);
            }
        };
        this.mRequestedVisible = false;
        this.mWindowVisibility = false;
        this.mViewVisibility = false;
        this.mRequestedWidth = -1;
        this.mRequestedHeight = -1;
        this.mRequestedFormat = 4;
        this.mHaveFrame = false;
        this.mSurfaceCreated = false;
        this.mLastLockTime = 0;
        this.mVisible = false;
        this.mLeft = -1;
        this.mTop = -1;
        this.mWidth = -1;
        this.mHeight = -1;
        this.mFormat = -1;
        this.mSurfaceFrame = new Rect();
        this.mLastSurfaceWidth = -1;
        this.mLastSurfaceHeight = -1;
        this.mDrawListener = new ViewTreeObserver.OnPreDrawListener() {
            /* class android.view.SurfaceView.AnonymousClass3 */

            @Override // android.view.ViewTreeObserver.OnPreDrawListener
            public boolean onPreDraw() {
                boolean z;
                SurfaceView surfaceView = SurfaceView.this;
                if (SurfaceView.this.getWidth() <= 0 || SurfaceView.this.getHeight() <= 0) {
                    z = false;
                } else {
                    z = true;
                }
                surfaceView.mHaveFrame = z;
                SurfaceView.this.updateWindow(false, false);
                return true;
            }
        };
        this.mSurfaceHolder = new SurfaceHolder() {
            /* class android.view.SurfaceView.AnonymousClass4 */
            private static final String LOG_TAG = "SurfaceHolder";

            @Override // android.view.SurfaceHolder
            public boolean isCreating() {
                return SurfaceView.this.mIsCreating;
            }

            @Override // android.view.SurfaceHolder
            public void addCallback(SurfaceHolder.Callback callback) {
                synchronized (SurfaceView.this.mCallbacks) {
                    if (!SurfaceView.this.mCallbacks.contains(callback)) {
                        SurfaceView.this.mCallbacks.add(callback);
                    }
                }
            }

            @Override // android.view.SurfaceHolder
            public void removeCallback(SurfaceHolder.Callback callback) {
                synchronized (SurfaceView.this.mCallbacks) {
                    SurfaceView.this.mCallbacks.remove(callback);
                }
            }

            @Override // android.view.SurfaceHolder
            public void setFixedSize(int width, int height) {
                if (SurfaceView.this.mRequestedWidth != width || SurfaceView.this.mRequestedHeight != height) {
                    SurfaceView.this.mRequestedWidth = width;
                    SurfaceView.this.mRequestedHeight = height;
                    SurfaceView.this.requestLayout();
                }
            }

            @Override // android.view.SurfaceHolder
            public void setSizeFromLayout() {
                if (SurfaceView.this.mRequestedWidth != -1 || SurfaceView.this.mRequestedHeight != -1) {
                    SurfaceView surfaceView = SurfaceView.this;
                    SurfaceView.this.mRequestedHeight = -1;
                    surfaceView.mRequestedWidth = -1;
                    SurfaceView.this.requestLayout();
                }
            }

            @Override // android.view.SurfaceHolder
            public void setFormat(int format) {
                if (format == -1) {
                    format = 4;
                }
                SurfaceView.this.mRequestedFormat = format;
                if (SurfaceView.this.mWindow != null) {
                    SurfaceView.this.updateWindow(false, false);
                }
            }

            @Override // android.view.SurfaceHolder
            @Deprecated
            public void setType(int type) {
            }

            @Override // android.view.SurfaceHolder
            public void setKeepScreenOn(boolean screenOn) {
                int i = 1;
                Message msg = SurfaceView.this.mHandler.obtainMessage(1);
                if (!screenOn) {
                    i = 0;
                }
                msg.arg1 = i;
                SurfaceView.this.mHandler.sendMessage(msg);
            }

            @Override // android.view.SurfaceHolder
            public Canvas lockCanvas() {
                return internalLockCanvas(null);
            }

            @Override // android.view.SurfaceHolder
            public Canvas lockCanvas(Rect inOutDirty) {
                return internalLockCanvas(inOutDirty);
            }

            private final Canvas internalLockCanvas(Rect dirty) {
                SurfaceView.this.mSurfaceLock.lock();
                Canvas c = null;
                if (!SurfaceView.this.mDrawingStopped && SurfaceView.this.mWindow != null) {
                    try {
                        c = SurfaceView.this.mSurface.lockCanvas(dirty);
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Exception locking surface", e);
                    }
                }
                if (c != null) {
                    SurfaceView.this.mLastLockTime = SystemClock.uptimeMillis();
                    return c;
                }
                long now = SystemClock.uptimeMillis();
                long nextTime = SurfaceView.this.mLastLockTime + 100;
                if (nextTime > now) {
                    try {
                        Thread.sleep(nextTime - now);
                    } catch (InterruptedException e2) {
                    }
                    now = SystemClock.uptimeMillis();
                }
                SurfaceView.this.mLastLockTime = now;
                SurfaceView.this.mSurfaceLock.unlock();
                return null;
            }

            @Override // android.view.SurfaceHolder
            public void unlockCanvasAndPost(Canvas canvas) {
                SurfaceView.this.mSurface.unlockCanvasAndPost(canvas);
                SurfaceView.this.mSurfaceLock.unlock();
            }

            @Override // android.view.SurfaceHolder
            public Surface getSurface() {
                return SurfaceView.this.mSurface;
            }

            @Override // android.view.SurfaceHolder
            public Rect getSurfaceFrame() {
                return SurfaceView.this.mSurfaceFrame;
            }
        };
        init();
    }

    private void init() {
        setWillNotDraw(true);
    }

    public SurfaceHolder getHolder() {
        return this.mSurfaceHolder;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mParent.requestTransparentRegion(this);
        this.mSession = getWindowSession();
        this.mLayout.token = getWindowToken();
        this.mLayout.setTitle(TAG);
        this.mViewVisibility = getVisibility() == 0;
        if (!this.mGlobalListenersAdded) {
            ViewTreeObserver observer = getViewTreeObserver();
            observer.addOnScrollChangedListener(this.mScrollChangedListener);
            observer.addOnPreDrawListener(this.mDrawListener);
            this.mGlobalListenersAdded = true;
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onWindowVisibilityChanged(int visibility) {
        boolean z;
        boolean z2 = true;
        super.onWindowVisibilityChanged(visibility);
        if (visibility == 0) {
            z = true;
        } else {
            z = false;
        }
        this.mWindowVisibility = z;
        if (!this.mWindowVisibility || !this.mViewVisibility) {
            z2 = false;
        }
        this.mRequestedVisible = z2;
        updateWindow(false, false);
    }

    @Override // android.view.View
    public void setVisibility(int visibility) {
        boolean z;
        boolean newRequestedVisible;
        super.setVisibility(visibility);
        if (visibility == 0) {
            z = true;
        } else {
            z = false;
        }
        this.mViewVisibility = z;
        if (!this.mWindowVisibility || !this.mViewVisibility) {
            newRequestedVisible = false;
        } else {
            newRequestedVisible = true;
        }
        if (newRequestedVisible != this.mRequestedVisible) {
            requestLayout();
        }
        this.mRequestedVisible = newRequestedVisible;
        updateWindow(false, false);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r1v4, resolved type: android.view.IWindowSession */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r2v1, types: [android.view.SurfaceView$MyWindow, android.view.IWindow] */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Unknown variable types count: 1 */
    @Override // android.view.View
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onDetachedFromWindow() {
        /*
            r4 = this;
            r3 = 0
            r2 = 0
            boolean r1 = r4.mGlobalListenersAdded
            if (r1 == 0) goto L_0x0016
            android.view.ViewTreeObserver r0 = r4.getViewTreeObserver()
            android.view.ViewTreeObserver$OnScrollChangedListener r1 = r4.mScrollChangedListener
            r0.removeOnScrollChangedListener(r1)
            android.view.ViewTreeObserver$OnPreDrawListener r1 = r4.mDrawListener
            r0.removeOnPreDrawListener(r1)
            r4.mGlobalListenersAdded = r2
        L_0x0016:
            r4.mRequestedVisible = r2
            r4.updateWindow(r2, r2)
            r4.mHaveFrame = r2
            android.view.SurfaceView$MyWindow r1 = r4.mWindow
            if (r1 == 0) goto L_0x002a
            android.view.IWindowSession r1 = r4.mSession     // Catch:{ RemoteException -> 0x0034 }
            android.view.SurfaceView$MyWindow r2 = r4.mWindow     // Catch:{ RemoteException -> 0x0034 }
            r1.remove(r2)     // Catch:{ RemoteException -> 0x0034 }
        L_0x0028:
            r4.mWindow = r3
        L_0x002a:
            r4.mSession = r3
            android.view.WindowManager$LayoutParams r1 = r4.mLayout
            r1.token = r3
            super.onDetachedFromWindow()
            return
        L_0x0034:
            r1 = move-exception
            goto L_0x0028
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.SurfaceView.onDetachedFromWindow():void");
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(this.mRequestedWidth >= 0 ? resolveSizeAndState(this.mRequestedWidth, widthMeasureSpec, 0) : getDefaultSize(0, widthMeasureSpec), this.mRequestedHeight >= 0 ? resolveSizeAndState(this.mRequestedHeight, heightMeasureSpec, 0) : getDefaultSize(0, heightMeasureSpec));
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public boolean setFrame(int left, int top, int right, int bottom) {
        boolean result = super.setFrame(left, top, right, bottom);
        updateWindow(false, false);
        return result;
    }

    @Override // android.view.View
    public boolean gatherTransparentRegion(Region region) {
        if (this.mWindowType == 1000) {
            return super.gatherTransparentRegion(region);
        }
        boolean opaque = true;
        if ((this.mPrivateFlags & 128) == 0) {
            opaque = super.gatherTransparentRegion(region);
        } else if (region != null) {
            int w = getWidth();
            int h = getHeight();
            if (w > 0 && h > 0) {
                getLocationInWindow(this.mLocation);
                int l = this.mLocation[0];
                int t = this.mLocation[1];
                region.op(l, t, l + w, t + h, Region.Op.UNION);
            }
        }
        if (PixelFormat.formatHasAlpha(this.mRequestedFormat)) {
            return false;
        }
        return opaque;
    }

    @Override // android.view.View
    public void draw(Canvas canvas) {
        if (this.mWindowType != 1000 && (this.mPrivateFlags & 128) == 0) {
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        }
        super.draw(canvas);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void dispatchDraw(Canvas canvas) {
        if (this.mWindowType != 1000 && (this.mPrivateFlags & 128) == 128) {
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        }
        super.dispatchDraw(canvas);
    }

    public void setZOrderMediaOverlay(boolean isMediaOverlay) {
        this.mWindowType = isMediaOverlay ? 1004 : 1001;
    }

    public void setZOrderOnTop(boolean onTop) {
        if (onTop) {
            this.mWindowType = 1000;
            this.mLayout.flags |= 131072;
            return;
        }
        this.mWindowType = 1001;
        this.mLayout.flags &= -131073;
    }

    public void setSecure(boolean isSecure) {
        if (isSecure) {
            this.mLayout.flags |= 8192;
            return;
        }
        this.mLayout.flags &= -8193;
    }

    public void setWindowType(int type) {
        this.mWindowType = type;
    }

    /* JADX INFO: finally extract failed */
    /* JADX DEBUG: Multi-variable search result rejected for r3v32, resolved type: android.view.IWindowSession */
    /* JADX DEBUG: Multi-variable search result rejected for r4v30, resolved type: android.view.IWindowSession */
    /* JADX DEBUG: Multi-variable search result rejected for r4v31, resolved type: android.view.IWindowSession */
    /* JADX DEBUG: Multi-variable search result rejected for r3v50, resolved type: android.view.IWindowSession */
    /* JADX DEBUG: Multi-variable search result rejected for r3v51, resolved type: android.view.IWindowSession */
    /* JADX DEBUG: Multi-variable search result rejected for r3v73, resolved type: android.view.IWindowSession */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r4v14, types: [android.view.SurfaceView$MyWindow, android.view.IWindow] */
    /* JADX WARN: Type inference failed for: r5v4, types: [android.view.SurfaceView$MyWindow, android.view.IWindow] */
    /* JADX WARN: Type inference failed for: r5v5, types: [android.view.SurfaceView$MyWindow, android.view.IWindow] */
    /* JADX WARN: Type inference failed for: r4v33, types: [android.view.SurfaceView$MyWindow, android.view.IWindow] */
    /* JADX WARN: Type inference failed for: r4v34, types: [android.view.SurfaceView$MyWindow, android.view.IWindow] */
    /* JADX WARN: Type inference failed for: r4v42, types: [android.view.SurfaceView$MyWindow, android.view.IWindow] */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Unknown variable types count: 4 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateWindow(boolean r40, boolean r41) {
        /*
        // Method dump skipped, instructions count: 1031
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.SurfaceView.updateWindow(boolean, boolean):void");
    }

    private SurfaceHolder.Callback[] getSurfaceCallbacks() {
        SurfaceHolder.Callback[] callbacks;
        synchronized (this.mCallbacks) {
            callbacks = new SurfaceHolder.Callback[this.mCallbacks.size()];
            this.mCallbacks.toArray(callbacks);
        }
        return callbacks;
    }

    /* access modifiers changed from: package-private */
    public void handleGetNewSurface() {
        updateWindow(false, false);
    }

    public boolean isFixedSize() {
        return (this.mRequestedWidth == -1 && this.mRequestedHeight == -1) ? false : true;
    }

    /* access modifiers changed from: private */
    public static class MyWindow extends BaseIWindow {
        int mCurHeight = -1;
        int mCurWidth = -1;
        private final WeakReference<SurfaceView> mSurfaceView;

        public MyWindow(SurfaceView surfaceView) {
            this.mSurfaceView = new WeakReference<>(surfaceView);
        }

        public void resized(Rect frame, Rect overscanInsets, Rect contentInsets, Rect visibleInsets, Rect stableInsets, boolean reportDraw, Configuration newConfig) {
            SurfaceView surfaceView = this.mSurfaceView.get();
            if (surfaceView != null) {
                surfaceView.mSurfaceLock.lock();
                if (reportDraw) {
                    try {
                        surfaceView.mUpdateWindowNeeded = true;
                        surfaceView.mReportDrawNeeded = true;
                        surfaceView.mHandler.sendEmptyMessage(3);
                    } catch (Throwable th) {
                        surfaceView.mSurfaceLock.unlock();
                        throw th;
                    }
                } else if (!(surfaceView.mWinFrame.width() == frame.width() && surfaceView.mWinFrame.height() == frame.height())) {
                    surfaceView.mUpdateWindowNeeded = true;
                    surfaceView.mHandler.sendEmptyMessage(3);
                }
                surfaceView.mSurfaceLock.unlock();
            }
        }

        public void dispatchAppVisibility(boolean visible) {
        }

        public void dispatchGetNewSurface() {
            SurfaceView surfaceView = this.mSurfaceView.get();
            if (surfaceView != null) {
                surfaceView.mHandler.sendMessage(surfaceView.mHandler.obtainMessage(2));
            }
        }

        public void windowFocusChanged(boolean hasFocus, boolean touchEnabled) {
            Log.w(SurfaceView.TAG, "Unexpected focus in surface: focus=" + hasFocus + ", touchEnabled=" + touchEnabled);
        }

        public void executeCommand(String command, String parameters, ParcelFileDescriptor out) {
        }
    }
}

package android.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ActivityTransitionCoordinator;
import android.content.Intent;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ResultReceiver;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import java.util.ArrayList;

/* access modifiers changed from: package-private */
public class ExitTransitionCoordinator extends ActivityTransitionCoordinator {
    private static final long MAX_WAIT_MS = 1000;
    private static final String TAG = "ExitTransitionCoordinator";
    private Activity mActivity;
    private ObjectAnimator mBackgroundAnimator;
    private boolean mExitComplete;
    private boolean mExitNotified;
    private Bundle mExitSharedElementBundle;
    private Handler mHandler;
    private boolean mIsBackgroundReady;
    private boolean mIsCanceled;
    private boolean mIsExitStarted;
    private boolean mIsHidden;
    private Bundle mSharedElementBundle;
    private boolean mSharedElementNotified;
    private boolean mSharedElementsHidden;

    public ExitTransitionCoordinator(Activity activity, ArrayList<String> names, ArrayList<String> accepted, ArrayList<View> mapped, boolean isReturning) {
        super(activity.getWindow(), names, getListener(activity, isReturning), isReturning);
        viewsReady(mapSharedElements(accepted, mapped));
        stripOffscreenViews();
        this.mIsBackgroundReady = !isReturning;
        this.mActivity = activity;
    }

    private static SharedElementCallback getListener(Activity activity, boolean isReturning) {
        return isReturning ? activity.mEnterTransitionListener : activity.mExitTransitionListener;
    }

    /* access modifiers changed from: protected */
    @Override // android.os.ResultReceiver
    public void onReceiveResult(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case 100:
                stopCancel();
                this.mResultReceiver = (ResultReceiver) resultData.getParcelable("android:remoteReceiver");
                if (this.mIsCanceled) {
                    this.mResultReceiver.send(106, null);
                    this.mResultReceiver = null;
                    return;
                }
                notifyComplete();
                return;
            case 101:
                stopCancel();
                if (!this.mIsCanceled) {
                    hideSharedElements();
                    return;
                }
                return;
            case 102:
            case 103:
            case 104:
            case 106:
            default:
                return;
            case 105:
                this.mHandler.removeMessages(106);
                startExit();
                return;
            case 107:
                this.mExitSharedElementBundle = resultData;
                sharedElementExitBack();
                return;
        }
    }

    private void stopCancel() {
        if (this.mHandler != null) {
            this.mHandler.removeMessages(106);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void delayCancel() {
        if (this.mHandler != null) {
            this.mHandler.sendEmptyMessageDelayed(106, 1000);
        }
    }

    public void resetViews() {
        if (this.mTransitioningViews != null) {
            showViews(this.mTransitioningViews, true);
        }
        showViews(this.mSharedElements, true);
        this.mIsHidden = true;
        ViewGroup decorView = getDecor();
        if (!this.mIsReturning && decorView != null) {
            decorView.suppressLayout(false);
        }
        moveSharedElementsFromOverlay();
        clearState();
    }

    private void sharedElementExitBack() {
        final ViewGroup decorView = getDecor();
        if (decorView != null) {
            decorView.suppressLayout(true);
        }
        if (decorView == null || this.mExitSharedElementBundle == null || this.mExitSharedElementBundle.isEmpty() || this.mSharedElements.isEmpty() || getSharedElementTransition() == null) {
            sharedElementTransitionComplete();
        } else {
            startTransition(new Runnable() {
                /* class android.app.ExitTransitionCoordinator.AnonymousClass1 */

                public void run() {
                    ExitTransitionCoordinator.this.startSharedElementExit(decorView);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startSharedElementExit(final ViewGroup decorView) {
        Transition transition = getSharedElementExitTransition();
        transition.addListener(new Transition.TransitionListenerAdapter() {
            /* class android.app.ExitTransitionCoordinator.AnonymousClass2 */

            @Override // android.transition.Transition.TransitionListener, android.transition.Transition.TransitionListenerAdapter
            public void onTransitionEnd(Transition transition) {
                transition.removeListener(this);
                if (ExitTransitionCoordinator.this.mExitComplete) {
                    ExitTransitionCoordinator.this.delayCancel();
                }
            }
        });
        final ArrayList<View> sharedElementSnapshots = createSnapshots(this.mExitSharedElementBundle, this.mSharedElementNames);
        decorView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            /* class android.app.ExitTransitionCoordinator.AnonymousClass3 */

            @Override // android.view.ViewTreeObserver.OnPreDrawListener
            public boolean onPreDraw() {
                decorView.getViewTreeObserver().removeOnPreDrawListener(this);
                ExitTransitionCoordinator.this.setSharedElementState(ExitTransitionCoordinator.this.mExitSharedElementBundle, sharedElementSnapshots);
                return true;
            }
        });
        setGhostVisibility(4);
        scheduleGhostVisibilityChange(4);
        if (this.mListener != null) {
            this.mListener.onSharedElementEnd(this.mSharedElementNames, this.mSharedElements, sharedElementSnapshots);
        }
        TransitionManager.beginDelayedTransition(decorView, transition);
        scheduleGhostVisibilityChange(0);
        setGhostVisibility(0);
        decorView.invalidate();
    }

    private void hideSharedElements() {
        moveSharedElementsFromOverlay();
        if (!this.mIsHidden) {
            hideViews(this.mSharedElements);
        }
        this.mSharedElementsHidden = true;
        finishIfNecessary();
    }

    public void startExit() {
        if (!this.mIsExitStarted) {
            this.mIsExitStarted = true;
            ViewGroup decorView = getDecor();
            if (decorView != null) {
                decorView.suppressLayout(true);
            }
            moveSharedElementsToOverlay();
            startTransition(new Runnable() {
                /* class android.app.ExitTransitionCoordinator.AnonymousClass4 */

                public void run() {
                    ExitTransitionCoordinator.this.beginTransitions();
                }
            });
        }
    }

    public void startExit(int resultCode, Intent data) {
        if (!this.mIsExitStarted) {
            this.mIsExitStarted = true;
            ViewGroup decorView = getDecor();
            if (decorView != null) {
                decorView.suppressLayout(true);
            }
            this.mHandler = new Handler() {
                /* class android.app.ExitTransitionCoordinator.AnonymousClass5 */

                @Override // android.os.Handler
                public void handleMessage(Message msg) {
                    ExitTransitionCoordinator.this.mIsCanceled = true;
                    ExitTransitionCoordinator.this.finish();
                }
            };
            delayCancel();
            moveSharedElementsToOverlay();
            if (decorView != null && decorView.getBackground() == null) {
                getWindow().setBackgroundDrawable(new ColorDrawable(-16777216));
            }
            this.mActivity.convertToTranslucent(new Activity.TranslucentConversionListener() {
                /* class android.app.ExitTransitionCoordinator.AnonymousClass6 */

                @Override // android.app.Activity.TranslucentConversionListener
                public void onTranslucentConversionComplete(boolean drawComplete) {
                    if (!ExitTransitionCoordinator.this.mIsCanceled) {
                        ExitTransitionCoordinator.this.fadeOutBackground();
                    }
                }
            }, ActivityOptions.makeSceneTransitionAnimation(this.mActivity, this, this.mAllSharedElementNames, resultCode, data));
            startTransition(new Runnable() {
                /* class android.app.ExitTransitionCoordinator.AnonymousClass7 */

                public void run() {
                    ExitTransitionCoordinator.this.startExitTransition();
                }
            });
        }
    }

    public void stop() {
        if (this.mIsReturning && this.mActivity != null) {
            this.mActivity.convertToTranslucent(null, null);
            finish();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startExitTransition() {
        Transition transition = getExitTransition();
        ViewGroup decorView = getDecor();
        if (transition == null || decorView == null || this.mTransitioningViews == null) {
            transitionStarted();
            return;
        }
        TransitionManager.beginDelayedTransition(decorView, transition);
        ((View) this.mTransitioningViews.get(0)).invalidate();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void fadeOutBackground() {
        Drawable background;
        if (this.mBackgroundAnimator == null) {
            ViewGroup decor = getDecor();
            if (decor == null || (background = decor.getBackground()) == null) {
                this.mIsBackgroundReady = true;
                return;
            }
            Drawable background2 = background.mutate();
            getWindow().setBackgroundDrawable(background2);
            this.mBackgroundAnimator = ObjectAnimator.ofInt(background2, "alpha", 0);
            this.mBackgroundAnimator.addListener(new AnimatorListenerAdapter() {
                /* class android.app.ExitTransitionCoordinator.AnonymousClass8 */

                public void onAnimationEnd(Animator animation) {
                    ExitTransitionCoordinator.this.mBackgroundAnimator = null;
                    if (!ExitTransitionCoordinator.this.mIsCanceled) {
                        ExitTransitionCoordinator.this.mIsBackgroundReady = true;
                        ExitTransitionCoordinator.this.notifyComplete();
                    }
                }
            });
            this.mBackgroundAnimator.setDuration(getFadeDuration());
            this.mBackgroundAnimator.start();
        }
    }

    private Transition getExitTransition() {
        Transition viewsTransition = null;
        if (this.mTransitioningViews != null && !this.mTransitioningViews.isEmpty()) {
            viewsTransition = configureTransition(getViewsTransition(), true);
        }
        if (viewsTransition == null) {
            exitTransitionComplete();
        } else {
            final ArrayList<View> transitioningViews = this.mTransitioningViews;
            viewsTransition.addListener(new ActivityTransitionCoordinator.ContinueTransitionListener() {
                /* class android.app.ExitTransitionCoordinator.AnonymousClass9 */

                @Override // android.transition.Transition.TransitionListener, android.transition.Transition.TransitionListenerAdapter
                public void onTransitionEnd(Transition transition) {
                    transition.removeListener(this);
                    ExitTransitionCoordinator.this.exitTransitionComplete();
                    if (ExitTransitionCoordinator.this.mIsHidden && transitioningViews != null) {
                        ExitTransitionCoordinator.this.showViews(transitioningViews, true);
                    }
                    if (ExitTransitionCoordinator.this.mSharedElementBundle != null) {
                        ExitTransitionCoordinator.this.delayCancel();
                    }
                    super.onTransitionEnd(transition);
                }
            });
            viewsTransition.forceVisibility(4, false);
        }
        return viewsTransition;
    }

    private Transition getSharedElementExitTransition() {
        Transition sharedElementTransition = null;
        if (!this.mSharedElements.isEmpty()) {
            sharedElementTransition = configureTransition(getSharedElementTransition(), false);
        }
        if (sharedElementTransition == null) {
            sharedElementTransitionComplete();
        } else {
            sharedElementTransition.addListener(new ActivityTransitionCoordinator.ContinueTransitionListener() {
                /* class android.app.ExitTransitionCoordinator.AnonymousClass10 */

                @Override // android.transition.Transition.TransitionListener, android.transition.Transition.TransitionListenerAdapter
                public void onTransitionEnd(Transition transition) {
                    transition.removeListener(this);
                    ExitTransitionCoordinator.this.sharedElementTransitionComplete();
                    if (ExitTransitionCoordinator.this.mIsHidden) {
                        ExitTransitionCoordinator.this.showViews(ExitTransitionCoordinator.this.mSharedElements, true);
                    }
                }
            });
            ((View) this.mSharedElements.get(0)).invalidate();
        }
        return sharedElementTransition;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void beginTransitions() {
        Transition transition = mergeTransitions(getSharedElementExitTransition(), getExitTransition());
        ViewGroup decorView = getDecor();
        if (transition == null || decorView == null) {
            transitionStarted();
            return;
        }
        setGhostVisibility(4);
        scheduleGhostVisibilityChange(4);
        TransitionManager.beginDelayedTransition(decorView, transition);
        scheduleGhostVisibilityChange(0);
        setGhostVisibility(0);
        decorView.invalidate();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void exitTransitionComplete() {
        this.mExitComplete = true;
        notifyComplete();
    }

    /* access modifiers changed from: protected */
    public boolean isReadyToNotify() {
        return (this.mSharedElementBundle == null || this.mResultReceiver == null || !this.mIsBackgroundReady) ? false : true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sharedElementTransitionComplete() {
        this.mSharedElementBundle = this.mExitSharedElementBundle == null ? captureSharedElementState() : captureExitSharedElementsState();
        notifyComplete();
    }

    private Bundle captureExitSharedElementsState() {
        Bundle bundle = new Bundle();
        RectF bounds = new RectF();
        Matrix matrix = new Matrix();
        for (int i = 0; i < this.mSharedElements.size(); i++) {
            String name = (String) this.mSharedElementNames.get(i);
            Bundle sharedElementState = this.mExitSharedElementBundle.getBundle(name);
            if (sharedElementState != null) {
                bundle.putBundle(name, sharedElementState);
            } else {
                captureSharedElementState((View) this.mSharedElements.get(i), name, bundle, matrix, bounds);
            }
        }
        return bundle;
    }

    /* access modifiers changed from: protected */
    public void notifyComplete() {
        if (isReadyToNotify()) {
            if (!this.mSharedElementNotified) {
                this.mSharedElementNotified = true;
                delayCancel();
                this.mResultReceiver.send(103, this.mSharedElementBundle);
            }
            if (!this.mExitNotified && this.mExitComplete) {
                this.mExitNotified = true;
                this.mResultReceiver.send(104, null);
                this.mResultReceiver = null;
                ViewGroup decorView = getDecor();
                if (!this.mIsReturning && decorView != null) {
                    decorView.suppressLayout(false);
                }
                finishIfNecessary();
            }
        }
    }

    private void finishIfNecessary() {
        if (this.mIsReturning && this.mExitNotified && this.mActivity != null && (this.mSharedElements.isEmpty() || this.mSharedElementsHidden)) {
            finish();
        }
        if (!this.mIsReturning && this.mExitNotified) {
            this.mActivity = null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void finish() {
        stopCancel();
        if (this.mActivity != null) {
            this.mActivity.mActivityTransitionState.clear();
            this.mActivity.finish();
            this.mActivity.overridePendingTransition(0, 0);
            this.mActivity = null;
        }
        this.mHandler = null;
        this.mSharedElementBundle = null;
        if (this.mBackgroundAnimator != null) {
            this.mBackgroundAnimator.cancel();
            this.mBackgroundAnimator = null;
        }
        this.mExitSharedElementBundle = null;
        clearState();
    }

    /* access modifiers changed from: protected */
    @Override // android.app.ActivityTransitionCoordinator
    public boolean moveSharedElementWithParent() {
        return !this.mIsReturning;
    }

    /* access modifiers changed from: protected */
    @Override // android.app.ActivityTransitionCoordinator
    public Transition getViewsTransition() {
        if (this.mIsReturning) {
            return getWindow().getReturnTransition();
        }
        return getWindow().getExitTransition();
    }

    /* access modifiers changed from: protected */
    public Transition getSharedElementTransition() {
        if (this.mIsReturning) {
            return getWindow().getSharedElementReturnTransition();
        }
        return getWindow().getSharedElementExitTransition();
    }
}

package android.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.ActivityTransitionCoordinator;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.ArrayMap;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import java.util.ArrayList;

/* access modifiers changed from: package-private */
public class EnterTransitionCoordinator extends ActivityTransitionCoordinator {
    private static final int MIN_ANIMATION_FRAMES = 2;
    private static final String TAG = "EnterTransitionCoordinator";
    private Activity mActivity;
    private boolean mAreViewsReady;
    private ObjectAnimator mBackgroundAnimator;
    private Transition mEnterViewsTransition;
    private boolean mHasStopped;
    private boolean mIsCanceled;
    private boolean mIsExitTransitionComplete;
    private boolean mIsReadyForTransition;
    private boolean mIsSharedElementTransitionComplete;
    private boolean mIsViewsTransitionComplete;
    private boolean mIsViewsTransitionStarted;
    private ArrayList<Matrix> mSharedElementParentMatrices;
    private boolean mSharedElementTransitionStarted;
    private Bundle mSharedElementsBundle;
    private boolean mWasOpaque;

    public EnterTransitionCoordinator(Activity activity, ResultReceiver resultReceiver, ArrayList<String> sharedElementNames, boolean isReturning) {
        super(activity.getWindow(), sharedElementNames, getListener(activity, isReturning), isReturning);
        this.mActivity = activity;
        setResultReceiver(resultReceiver);
        prepareEnter();
        Bundle resultReceiverBundle = new Bundle();
        resultReceiverBundle.putParcelable("android:remoteReceiver", this);
        this.mResultReceiver.send(100, resultReceiverBundle);
        final View decorView = getDecor();
        if (decorView != null) {
            decorView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                /* class android.app.EnterTransitionCoordinator.AnonymousClass1 */

                @Override // android.view.ViewTreeObserver.OnPreDrawListener
                public boolean onPreDraw() {
                    if (EnterTransitionCoordinator.this.mIsReadyForTransition) {
                        decorView.getViewTreeObserver().removeOnPreDrawListener(this);
                    }
                    return EnterTransitionCoordinator.this.mIsReadyForTransition;
                }
            });
        }
    }

    public void viewInstancesReady(ArrayList<String> accepted, ArrayList<String> localNames, ArrayList<View> localViews) {
        boolean remap = false;
        int i = 0;
        while (true) {
            if (i >= localViews.size()) {
                break;
            }
            View view = localViews.get(i);
            if (!TextUtils.equals(view.getTransitionName(), localNames.get(i)) || !view.isAttachedToWindow()) {
                remap = true;
            } else {
                i++;
            }
        }
        remap = true;
        if (remap) {
            triggerViewsReady(mapNamedElements(accepted, localNames));
        } else {
            triggerViewsReady(mapSharedElements(accepted, localViews));
        }
    }

    public void namedViewsReady(ArrayList<String> accepted, ArrayList<String> localNames) {
        triggerViewsReady(mapNamedElements(accepted, localNames));
    }

    public Transition getEnterViewsTransition() {
        return this.mEnterViewsTransition;
    }

    /* access modifiers changed from: protected */
    @Override // android.app.ActivityTransitionCoordinator
    public void viewsReady(ArrayMap<String, View> sharedElements) {
        super.viewsReady(sharedElements);
        this.mIsReadyForTransition = true;
        hideViews(this.mSharedElements);
        if (!(getViewsTransition() == null || this.mTransitioningViews == null)) {
            hideViews(this.mTransitioningViews);
        }
        if (this.mIsReturning) {
            sendSharedElementDestination();
        } else {
            setSharedElementMatrices();
            moveSharedElementsToOverlay();
        }
        if (this.mSharedElementsBundle != null) {
            onTakeSharedElements();
        }
    }

    private void triggerViewsReady(final ArrayMap<String, View> sharedElements) {
        if (!this.mAreViewsReady) {
            this.mAreViewsReady = true;
            if (sharedElements.isEmpty() || !sharedElements.valueAt(0).isLayoutRequested()) {
                viewsReady(sharedElements);
                return;
            }
            final View sharedElement = sharedElements.valueAt(0);
            sharedElement.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                /* class android.app.EnterTransitionCoordinator.AnonymousClass2 */

                @Override // android.view.ViewTreeObserver.OnPreDrawListener
                public boolean onPreDraw() {
                    sharedElement.getViewTreeObserver().removeOnPreDrawListener(this);
                    EnterTransitionCoordinator.this.viewsReady(sharedElements);
                    return true;
                }
            });
        }
    }

    private ArrayMap<String, View> mapNamedElements(ArrayList<String> accepted, ArrayList<String> localNames) {
        View view;
        ArrayMap<String, View> sharedElements = new ArrayMap<>();
        ViewGroup decorView = getDecor();
        if (decorView != null) {
            decorView.findNamedViews(sharedElements);
        }
        if (accepted != null) {
            for (int i = 0; i < localNames.size(); i++) {
                String localName = localNames.get(i);
                String acceptedName = accepted.get(i);
                if (!(localName == null || localName.equals(acceptedName) || (view = sharedElements.remove(localName)) == null)) {
                    sharedElements.put(acceptedName, view);
                }
            }
        }
        return sharedElements;
    }

    private void sendSharedElementDestination() {
        boolean allReady;
        final View decorView = getDecor();
        if (allowOverlappingTransitions() && getEnterViewsTransition() != null) {
            allReady = false;
        } else if (decorView == null) {
            allReady = true;
        } else {
            allReady = !decorView.isLayoutRequested();
            if (allReady) {
                int i = 0;
                while (true) {
                    if (i >= this.mSharedElements.size()) {
                        break;
                    } else if (((View) this.mSharedElements.get(i)).isLayoutRequested()) {
                        allReady = false;
                        break;
                    } else {
                        i++;
                    }
                }
            }
        }
        if (allReady) {
            Bundle state = captureSharedElementState();
            setSharedElementMatrices();
            moveSharedElementsToOverlay();
            this.mResultReceiver.send(107, state);
        } else if (decorView != null) {
            decorView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                /* class android.app.EnterTransitionCoordinator.AnonymousClass3 */

                @Override // android.view.ViewTreeObserver.OnPreDrawListener
                public boolean onPreDraw() {
                    decorView.getViewTreeObserver().removeOnPreDrawListener(this);
                    if (EnterTransitionCoordinator.this.mResultReceiver == null) {
                        return true;
                    }
                    Bundle state = EnterTransitionCoordinator.this.captureSharedElementState();
                    EnterTransitionCoordinator.this.setSharedElementMatrices();
                    EnterTransitionCoordinator.this.moveSharedElementsToOverlay();
                    EnterTransitionCoordinator.this.mResultReceiver.send(107, state);
                    return true;
                }
            });
        }
        if (allowOverlappingTransitions()) {
            startEnterTransitionOnly();
        }
    }

    private static SharedElementCallback getListener(Activity activity, boolean isReturning) {
        return isReturning ? activity.mExitTransitionListener : activity.mEnterTransitionListener;
    }

    /* access modifiers changed from: protected */
    @Override // android.os.ResultReceiver
    public void onReceiveResult(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case 103:
                if (!this.mIsCanceled) {
                    this.mSharedElementsBundle = resultData;
                    onTakeSharedElements();
                    return;
                }
                return;
            case 104:
                if (!this.mIsCanceled) {
                    this.mIsExitTransitionComplete = true;
                    if (this.mSharedElementTransitionStarted) {
                        onRemoteExitTransitionComplete();
                        return;
                    }
                    return;
                }
                return;
            case 105:
            default:
                return;
            case 106:
                cancel();
                return;
        }
    }

    private void cancel() {
        if (!this.mIsCanceled) {
            this.mIsCanceled = true;
            if (getViewsTransition() == null || this.mIsViewsTransitionStarted) {
                showViews(this.mSharedElements, true);
            } else if (this.mTransitioningViews != null) {
                this.mTransitioningViews.addAll(this.mSharedElements);
            }
            this.mSharedElementNames.clear();
            this.mSharedElements.clear();
            this.mAllSharedElementNames.clear();
            startSharedElementTransition(null);
            onRemoteExitTransitionComplete();
        }
    }

    public boolean isReturning() {
        return this.mIsReturning;
    }

    /* access modifiers changed from: protected */
    public void prepareEnter() {
        ViewGroup decorView = getDecor();
        if (this.mActivity != null && decorView != null) {
            this.mActivity.overridePendingTransition(0, 0);
            if (!this.mIsReturning) {
                this.mWasOpaque = this.mActivity.convertToTranslucent(null, null);
                Drawable background = decorView.getBackground();
                if (background != null) {
                    getWindow().setBackgroundDrawable(null);
                    Drawable background2 = background.mutate();
                    background2.setAlpha(0);
                    getWindow().setBackgroundDrawable(background2);
                    return;
                }
                return;
            }
            this.mActivity = null;
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.app.ActivityTransitionCoordinator
    public Transition getViewsTransition() {
        Window window = getWindow();
        if (window == null) {
            return null;
        }
        if (this.mIsReturning) {
            return window.getReenterTransition();
        }
        return window.getEnterTransition();
    }

    /* access modifiers changed from: protected */
    public Transition getSharedElementTransition() {
        Window window = getWindow();
        if (window == null) {
            return null;
        }
        if (this.mIsReturning) {
            return window.getSharedElementReenterTransition();
        }
        return window.getSharedElementEnterTransition();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startSharedElementTransition(Bundle sharedElementState) {
        boolean startEnterTransition = true;
        ViewGroup decorView = getDecor();
        if (decorView != null) {
            ArrayList<String> rejectedNames = new ArrayList<>(this.mAllSharedElementNames);
            rejectedNames.removeAll(this.mSharedElementNames);
            ArrayList<View> rejectedSnapshots = createSnapshots(sharedElementState, rejectedNames);
            if (this.mListener != null) {
                this.mListener.onRejectSharedElements(rejectedSnapshots);
            }
            startRejectedAnimations(rejectedSnapshots);
            ArrayList<View> sharedElementSnapshots = createSnapshots(sharedElementState, this.mSharedElementNames);
            showViews(this.mSharedElements, true);
            scheduleSetSharedElementEnd(sharedElementSnapshots);
            ArrayList<ActivityTransitionCoordinator.SharedElementOriginalState> originalImageViewState = setSharedElementState(sharedElementState, sharedElementSnapshots);
            requestLayoutForSharedElements();
            if (!allowOverlappingTransitions() || this.mIsReturning) {
                startEnterTransition = false;
            }
            setGhostVisibility(4);
            scheduleGhostVisibilityChange(4);
            Transition transition = beginTransition(decorView, startEnterTransition, true);
            scheduleGhostVisibilityChange(0);
            setGhostVisibility(0);
            if (startEnterTransition) {
                startEnterTransition(transition);
            }
            setOriginalSharedElementState(this.mSharedElements, originalImageViewState);
            if (this.mResultReceiver != null) {
                decorView.postOnAnimation(new Runnable() {
                    /* class android.app.EnterTransitionCoordinator.AnonymousClass4 */
                    int mAnimations;

                    public void run() {
                        int i = this.mAnimations;
                        this.mAnimations = i + 1;
                        if (i < 2) {
                            View decorView = EnterTransitionCoordinator.this.getDecor();
                            if (decorView != null) {
                                decorView.postOnAnimation(this);
                            }
                        } else if (EnterTransitionCoordinator.this.mResultReceiver != null) {
                            EnterTransitionCoordinator.this.mResultReceiver.send(101, null);
                            EnterTransitionCoordinator.this.mResultReceiver = null;
                        }
                    }
                });
            }
        }
    }

    private void onTakeSharedElements() {
        if (this.mIsReadyForTransition && this.mSharedElementsBundle != null) {
            final Bundle sharedElementState = this.mSharedElementsBundle;
            this.mSharedElementsBundle = null;
            final View decorView = getDecor();
            if (decorView != null) {
                decorView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    /* class android.app.EnterTransitionCoordinator.AnonymousClass5 */

                    @Override // android.view.ViewTreeObserver.OnPreDrawListener
                    public boolean onPreDraw() {
                        decorView.getViewTreeObserver().removeOnPreDrawListener(this);
                        EnterTransitionCoordinator.this.startTransition(new Runnable() {
                            /* class android.app.EnterTransitionCoordinator.AnonymousClass5.AnonymousClass1 */

                            public void run() {
                                EnterTransitionCoordinator.this.startSharedElementTransition(sharedElementState);
                            }
                        });
                        return false;
                    }
                });
                decorView.invalidate();
            }
        }
    }

    private void requestLayoutForSharedElements() {
        int numSharedElements = this.mSharedElements.size();
        for (int i = 0; i < numSharedElements; i++) {
            ((View) this.mSharedElements.get(i)).requestLayout();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Transition beginTransition(ViewGroup decorView, boolean startEnterTransition, boolean startSharedElementTransition) {
        Transition sharedElementTransition = null;
        if (startSharedElementTransition) {
            if (!this.mSharedElementNames.isEmpty()) {
                sharedElementTransition = configureTransition(getSharedElementTransition(), false);
            }
            if (sharedElementTransition == null) {
                sharedElementTransitionStarted();
                sharedElementTransitionComplete();
            } else {
                sharedElementTransition.addListener(new Transition.TransitionListenerAdapter() {
                    /* class android.app.EnterTransitionCoordinator.AnonymousClass6 */

                    @Override // android.transition.Transition.TransitionListener, android.transition.Transition.TransitionListenerAdapter
                    public void onTransitionStart(Transition transition) {
                        EnterTransitionCoordinator.this.sharedElementTransitionStarted();
                    }

                    @Override // android.transition.Transition.TransitionListener, android.transition.Transition.TransitionListenerAdapter
                    public void onTransitionEnd(Transition transition) {
                        transition.removeListener(this);
                        EnterTransitionCoordinator.this.sharedElementTransitionComplete();
                    }
                });
            }
        }
        Transition viewsTransition = null;
        if (startEnterTransition) {
            this.mIsViewsTransitionStarted = true;
            if (this.mTransitioningViews != null && !this.mTransitioningViews.isEmpty() && (viewsTransition = configureTransition(getViewsTransition(), true)) != null && !this.mIsReturning) {
                stripOffscreenViews();
            }
            if (viewsTransition == null) {
                viewTransitionComplete();
            } else {
                viewsTransition.forceVisibility(4, true);
                final ArrayList<View> transitioningViews = this.mTransitioningViews;
                viewsTransition.addListener(new ActivityTransitionCoordinator.ContinueTransitionListener() {
                    /* class android.app.EnterTransitionCoordinator.AnonymousClass7 */

                    @Override // android.app.ActivityTransitionCoordinator.ContinueTransitionListener, android.transition.Transition.TransitionListener, android.transition.Transition.TransitionListenerAdapter
                    public void onTransitionStart(Transition transition) {
                        EnterTransitionCoordinator.this.mEnterViewsTransition = transition;
                        if (transitioningViews != null) {
                            EnterTransitionCoordinator.this.showViews(transitioningViews, false);
                        }
                        super.onTransitionStart(transition);
                    }

                    @Override // android.transition.Transition.TransitionListener, android.transition.Transition.TransitionListenerAdapter
                    public void onTransitionEnd(Transition transition) {
                        EnterTransitionCoordinator.this.mEnterViewsTransition = null;
                        transition.removeListener(this);
                        EnterTransitionCoordinator.this.viewTransitionComplete();
                        super.onTransitionEnd(transition);
                    }
                });
            }
        }
        Transition transition = mergeTransitions(sharedElementTransition, viewsTransition);
        if (transition != null) {
            transition.addListener(new ActivityTransitionCoordinator.ContinueTransitionListener());
            TransitionManager.beginDelayedTransition(decorView, transition);
            if (startSharedElementTransition && !this.mSharedElementNames.isEmpty()) {
                ((View) this.mSharedElements.get(0)).invalidate();
            } else if (startEnterTransition && this.mTransitioningViews != null && !this.mTransitioningViews.isEmpty()) {
                ((View) this.mTransitioningViews.get(0)).invalidate();
            }
        } else {
            transitionStarted();
        }
        return transition;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void viewTransitionComplete() {
        this.mIsViewsTransitionComplete = true;
        if (this.mIsSharedElementTransitionComplete) {
            moveSharedElementsFromOverlay();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sharedElementTransitionComplete() {
        this.mIsSharedElementTransitionComplete = true;
        if (this.mIsViewsTransitionComplete) {
            moveSharedElementsFromOverlay();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sharedElementTransitionStarted() {
        this.mSharedElementTransitionStarted = true;
        if (this.mIsExitTransitionComplete) {
            send(104, null);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startEnterTransition(Transition transition) {
        ViewGroup decorView = getDecor();
        if (!this.mIsReturning && decorView != null) {
            Drawable background = decorView.getBackground();
            if (background != null) {
                Drawable background2 = background.mutate();
                getWindow().setBackgroundDrawable(background2);
                this.mBackgroundAnimator = ObjectAnimator.ofInt(background2, "alpha", 255);
                this.mBackgroundAnimator.setDuration(getFadeDuration());
                this.mBackgroundAnimator.addListener(new AnimatorListenerAdapter() {
                    /* class android.app.EnterTransitionCoordinator.AnonymousClass8 */

                    public void onAnimationEnd(Animator animation) {
                        EnterTransitionCoordinator.this.makeOpaque();
                    }
                });
                this.mBackgroundAnimator.start();
            } else if (transition != null) {
                transition.addListener(new Transition.TransitionListenerAdapter() {
                    /* class android.app.EnterTransitionCoordinator.AnonymousClass9 */

                    @Override // android.transition.Transition.TransitionListener, android.transition.Transition.TransitionListenerAdapter
                    public void onTransitionEnd(Transition transition) {
                        transition.removeListener(this);
                        EnterTransitionCoordinator.this.makeOpaque();
                    }
                });
            } else {
                makeOpaque();
            }
        }
    }

    public void stop() {
        ViewGroup decorView;
        Drawable drawable;
        if (this.mBackgroundAnimator != null) {
            this.mBackgroundAnimator.end();
            this.mBackgroundAnimator = null;
        } else if (!(!this.mWasOpaque || (decorView = getDecor()) == null || (drawable = decorView.getBackground()) == null)) {
            drawable.setAlpha(1);
        }
        makeOpaque();
        this.mIsCanceled = true;
        this.mResultReceiver = null;
        this.mActivity = null;
        moveSharedElementsFromOverlay();
        if (this.mTransitioningViews != null) {
            showViews(this.mTransitioningViews, true);
        }
        showViews(this.mSharedElements, true);
        clearState();
    }

    public void cancelEnter() {
        setGhostVisibility(4);
        this.mHasStopped = true;
        this.mIsCanceled = true;
        this.mResultReceiver = null;
        if (this.mBackgroundAnimator != null) {
            this.mBackgroundAnimator.cancel();
            this.mBackgroundAnimator = null;
        }
        this.mActivity = null;
        clearState();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void makeOpaque() {
        if (!this.mHasStopped && this.mActivity != null) {
            if (this.mWasOpaque) {
                this.mActivity.convertFromTranslucent();
            }
            this.mActivity = null;
        }
    }

    private boolean allowOverlappingTransitions() {
        return this.mIsReturning ? getWindow().getAllowExitTransitionOverlap() : getWindow().getAllowEnterTransitionOverlap();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:3:0x0009, code lost:
        r1 = getDecor();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void startRejectedAnimations(final java.util.ArrayList<android.view.View> r9) {
        /*
            r8 = this;
            if (r9 == 0) goto L_0x0008
            boolean r6 = r9.isEmpty()
            if (r6 == 0) goto L_0x0009
        L_0x0008:
            return
        L_0x0009:
            android.view.ViewGroup r1 = r8.getDecor()
            if (r1 == 0) goto L_0x0008
            android.view.ViewGroupOverlay r4 = r1.getOverlay()
            r0 = 0
            int r3 = r9.size()
            r2 = 0
        L_0x0019:
            if (r2 >= r3) goto L_0x0036
            java.lang.Object r5 = r9.get(r2)
            android.view.View r5 = (android.view.View) r5
            r4.add(r5)
            android.util.Property<android.view.View, java.lang.Float> r6 = android.view.View.ALPHA
            r7 = 2
            float[] r7 = new float[r7]
            r7 = {x0040: FILL_ARRAY_DATA  , data: [1065353216, 0} // fill-array
            android.animation.ObjectAnimator r0 = android.animation.ObjectAnimator.ofFloat(r5, r6, r7)
            r0.start()
            int r2 = r2 + 1
            goto L_0x0019
        L_0x0036:
            android.app.EnterTransitionCoordinator$10 r6 = new android.app.EnterTransitionCoordinator$10
            r6.<init>(r1, r9)
            r0.addListener(r6)
            goto L_0x0008
            fill-array 0x0040: FILL_ARRAY_DATA  , data: [1065353216, 0]
        */
        throw new UnsupportedOperationException("Method not decompiled: android.app.EnterTransitionCoordinator.startRejectedAnimations(java.util.ArrayList):void");
    }

    /* access modifiers changed from: protected */
    public void onRemoteExitTransitionComplete() {
        if (!allowOverlappingTransitions()) {
            startEnterTransitionOnly();
        }
    }

    private void startEnterTransitionOnly() {
        startTransition(new Runnable() {
            /* class android.app.EnterTransitionCoordinator.AnonymousClass11 */

            public void run() {
                ViewGroup decorView = EnterTransitionCoordinator.this.getDecor();
                if (decorView != null) {
                    EnterTransitionCoordinator.this.startEnterTransition(EnterTransitionCoordinator.this.beginTransition(decorView, true, false));
                }
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setSharedElementMatrices() {
        int numSharedElements = this.mSharedElements.size();
        if (numSharedElements > 0) {
            this.mSharedElementParentMatrices = new ArrayList<>(numSharedElements);
        }
        for (int i = 0; i < numSharedElements; i++) {
            Matrix matrix = new Matrix();
            ((ViewGroup) ((View) this.mSharedElements.get(i)).getParent()).transformMatrixToLocal(matrix);
            this.mSharedElementParentMatrices.add(matrix);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.app.ActivityTransitionCoordinator
    public void getSharedElementParentMatrix(View view, Matrix matrix) {
        int index = this.mSharedElementParentMatrices == null ? -1 : this.mSharedElements.indexOf(view);
        if (index < 0) {
            super.getSharedElementParentMatrix(view, matrix);
        } else {
            matrix.set(this.mSharedElementParentMatrices.get(index));
        }
    }
}

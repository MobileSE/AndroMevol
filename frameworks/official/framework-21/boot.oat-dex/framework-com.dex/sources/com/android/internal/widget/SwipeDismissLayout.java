package com.android.internal.widget;

import android.animation.TimeInterpolator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

public class SwipeDismissLayout extends FrameLayout {
    private static final float DISMISS_MIN_DRAG_WIDTH_RATIO = 0.33f;
    private static final String TAG = "SwipeDismissLayout";
    private int mActiveTouchId;
    private long mAnimationTime;
    private TimeInterpolator mCancelInterpolator;
    private boolean mDiscardIntercept;
    private TimeInterpolator mDismissInterpolator;
    private boolean mDismissed;
    private OnDismissedListener mDismissedListener;
    private float mDownX;
    private float mDownY;
    private float mLastX;
    private int mMaxFlingVelocity;
    private int mMinFlingVelocity;
    private OnSwipeProgressChangedListener mProgressListener;
    private int mSlop;
    private boolean mSwiping;
    private float mTranslationX;
    private VelocityTracker mVelocityTracker;

    public interface OnDismissedListener {
        void onDismissed(SwipeDismissLayout swipeDismissLayout);
    }

    public interface OnSwipeProgressChangedListener {
        void onSwipeCancelled(SwipeDismissLayout swipeDismissLayout);

        void onSwipeProgressChanged(SwipeDismissLayout swipeDismissLayout, float f, float f2);
    }

    public SwipeDismissLayout(Context context) {
        super(context);
        init(context);
    }

    public SwipeDismissLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SwipeDismissLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        ViewConfiguration vc = ViewConfiguration.get(getContext());
        this.mSlop = vc.getScaledTouchSlop();
        this.mMinFlingVelocity = vc.getScaledMinimumFlingVelocity();
        this.mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();
        this.mAnimationTime = (long) getContext().getResources().getInteger(17694720);
        this.mCancelInterpolator = new DecelerateInterpolator(1.5f);
        this.mDismissInterpolator = new AccelerateInterpolator(1.5f);
    }

    public void setOnDismissedListener(OnDismissedListener listener) {
        this.mDismissedListener = listener;
    }

    public void setOnSwipeProgressChangedListener(OnSwipeProgressChangedListener listener) {
        this.mProgressListener = listener;
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int newActionIndex;
        ev.offsetLocation(this.mTranslationX, 0.0f);
        switch (ev.getActionMasked()) {
            case 0:
                resetMembers();
                this.mDownX = ev.getRawX();
                this.mDownY = ev.getRawY();
                this.mActiveTouchId = ev.getPointerId(0);
                this.mVelocityTracker = VelocityTracker.obtain();
                this.mVelocityTracker.addMovement(ev);
                break;
            case 1:
            case 3:
                resetMembers();
                break;
            case 2:
                if (this.mVelocityTracker != null && !this.mDiscardIntercept) {
                    int pointerIndex = ev.findPointerIndex(this.mActiveTouchId);
                    if (pointerIndex != -1) {
                        float dx = ev.getRawX() - this.mDownX;
                        float x = ev.getX(pointerIndex);
                        float y = ev.getY(pointerIndex);
                        if (dx != 0.0f && canScroll(this, false, dx, x, y)) {
                            this.mDiscardIntercept = true;
                            break;
                        } else {
                            updateSwiping(ev);
                            break;
                        }
                    } else {
                        Log.e(TAG, "Invalid pointer index: ignoring.");
                        this.mDiscardIntercept = true;
                        break;
                    }
                }
                break;
            case 5:
                this.mActiveTouchId = ev.getPointerId(ev.getActionIndex());
                break;
            case 6:
                int actionIndex = ev.getActionIndex();
                if (ev.getPointerId(actionIndex) == this.mActiveTouchId) {
                    if (actionIndex == 0) {
                        newActionIndex = 1;
                    } else {
                        newActionIndex = 0;
                    }
                    this.mActiveTouchId = ev.getPointerId(newActionIndex);
                    break;
                }
                break;
        }
        return !this.mDiscardIntercept && this.mSwiping;
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent ev) {
        if (this.mVelocityTracker == null) {
            return super.onTouchEvent(ev);
        }
        switch (ev.getActionMasked()) {
            case 1:
                updateDismiss(ev);
                if (this.mDismissed) {
                    dismiss();
                } else if (this.mSwiping) {
                    cancel();
                }
                resetMembers();
                break;
            case 2:
                this.mVelocityTracker.addMovement(ev);
                this.mLastX = ev.getRawX();
                updateSwiping(ev);
                if (this.mSwiping) {
                    setProgress(ev.getRawX() - this.mDownX);
                    break;
                }
                break;
            case 3:
                cancel();
                resetMembers();
                break;
        }
        return true;
    }

    private void setProgress(float deltaX) {
        this.mTranslationX = deltaX;
        if (this.mProgressListener != null && deltaX >= 0.0f) {
            this.mProgressListener.onSwipeProgressChanged(this, deltaX / ((float) getWidth()), deltaX);
        }
    }

    private void dismiss() {
        if (this.mDismissedListener != null) {
            this.mDismissedListener.onDismissed(this);
        }
    }

    /* access modifiers changed from: protected */
    public void cancel() {
        if (this.mProgressListener != null) {
            this.mProgressListener.onSwipeCancelled(this);
        }
    }

    private void resetMembers() {
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.recycle();
        }
        this.mVelocityTracker = null;
        this.mTranslationX = 0.0f;
        this.mDownX = 0.0f;
        this.mDownY = 0.0f;
        this.mSwiping = false;
        this.mDismissed = false;
        this.mDiscardIntercept = false;
    }

    private void updateSwiping(MotionEvent ev) {
        boolean z = false;
        if (!this.mSwiping) {
            float deltaX = ev.getRawX() - this.mDownX;
            float deltaY = ev.getRawY() - this.mDownY;
            if ((deltaX * deltaX) + (deltaY * deltaY) > ((float) (this.mSlop * this.mSlop))) {
                if (deltaX > ((float) (this.mSlop * 2)) && Math.abs(deltaY) < ((float) (this.mSlop * 2))) {
                    z = true;
                }
                this.mSwiping = z;
                return;
            }
            this.mSwiping = false;
        }
    }

    private void updateDismiss(MotionEvent ev) {
        float deltaX = ev.getRawX() - this.mDownX;
        if (!this.mDismissed) {
            this.mVelocityTracker.addMovement(ev);
            this.mVelocityTracker.computeCurrentVelocity(1000);
            if (deltaX > ((float) getWidth()) * DISMISS_MIN_DRAG_WIDTH_RATIO && ev.getRawX() >= this.mLastX) {
                this.mDismissed = true;
            }
        }
        if (this.mDismissed && this.mSwiping && deltaX < ((float) getWidth()) * DISMISS_MIN_DRAG_WIDTH_RATIO) {
            this.mDismissed = false;
        }
    }

    /* access modifiers changed from: protected */
    public boolean canScroll(View v, boolean checkV, float dx, float x, float y) {
        if (v instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) v;
            int scrollX = v.getScrollX();
            int scrollY = v.getScrollY();
            for (int i = group.getChildCount() - 1; i >= 0; i--) {
                View child = group.getChildAt(i);
                if (((float) scrollX) + x >= ((float) child.getLeft()) && ((float) scrollX) + x < ((float) child.getRight()) && ((float) scrollY) + y >= ((float) child.getTop()) && ((float) scrollY) + y < ((float) child.getBottom()) && canScroll(child, true, dx, (((float) scrollX) + x) - ((float) child.getLeft()), (((float) scrollY) + y) - ((float) child.getTop()))) {
                    return true;
                }
            }
        }
        return checkV && v.canScrollHorizontally((int) (-dx));
    }
}

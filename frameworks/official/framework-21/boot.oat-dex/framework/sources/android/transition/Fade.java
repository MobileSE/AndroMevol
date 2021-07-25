package android.transition;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import com.android.internal.R;

public class Fade extends Visibility {
    private static boolean DBG = false;
    public static final int IN = 1;
    private static final String LOG_TAG = "Fade";
    public static final int OUT = 2;

    public Fade() {
    }

    public Fade(int fadingMode) {
        setMode(fadingMode);
    }

    public Fade(Context context, AttributeSet attrs) {
        super(context, attrs);
        setMode(context.obtainStyledAttributes(attrs, R.styleable.Fade).getInt(0, getMode()));
    }

    private Animator createAnimation(View view, float startAlpha, float endAlpha) {
        if (startAlpha == endAlpha) {
            return null;
        }
        view.setTransitionAlpha(startAlpha);
        ObjectAnimator anim = ObjectAnimator.ofFloat(view, "transitionAlpha", endAlpha);
        if (DBG) {
            Log.d(LOG_TAG, "Created animator " + anim);
        }
        FadeAnimatorListener listener = new FadeAnimatorListener(view);
        anim.addListener(listener);
        anim.addPauseListener(listener);
        return anim;
    }

    @Override // android.transition.Visibility
    public Animator onAppear(ViewGroup sceneRoot, View view, TransitionValues startValues, TransitionValues endValues) {
        if (DBG) {
            Log.d(LOG_TAG, "Fade.onAppear: startView, startVis, endView, endVis = " + (startValues != null ? startValues.view : null) + ", " + view);
        }
        return createAnimation(view, 0.0f, 1.0f);
    }

    @Override // android.transition.Visibility
    public Animator onDisappear(ViewGroup sceneRoot, View view, TransitionValues startValues, TransitionValues endValues) {
        return createAnimation(view, 1.0f, 0.0f);
    }

    /* access modifiers changed from: private */
    public static class FadeAnimatorListener extends AnimatorListenerAdapter {
        private boolean mCanceled = false;
        private boolean mLayerTypeChanged = false;
        private float mPausedAlpha = -1.0f;
        private final View mView;

        public FadeAnimatorListener(View view) {
            this.mView = view;
        }

        public void onAnimationStart(Animator animator) {
            if (this.mView.hasOverlappingRendering() && this.mView.getLayerType() == 0) {
                this.mLayerTypeChanged = true;
                this.mView.setLayerType(2, null);
            }
        }

        public void onAnimationCancel(Animator animator) {
            this.mCanceled = true;
            if (this.mPausedAlpha >= 0.0f) {
                this.mView.setTransitionAlpha(this.mPausedAlpha);
            }
        }

        public void onAnimationEnd(Animator animator) {
            if (!this.mCanceled) {
                this.mView.setTransitionAlpha(1.0f);
            }
            if (this.mLayerTypeChanged) {
                this.mView.setLayerType(0, null);
            }
        }

        public void onAnimationPause(Animator animator) {
            this.mPausedAlpha = this.mView.getTransitionAlpha();
            this.mView.setTransitionAlpha(1.0f);
        }

        public void onAnimationResume(Animator animator) {
            this.mView.setTransitionAlpha(this.mPausedAlpha);
        }
    }
}

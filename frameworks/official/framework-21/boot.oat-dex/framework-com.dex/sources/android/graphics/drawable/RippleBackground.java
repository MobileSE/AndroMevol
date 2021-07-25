package android.graphics.drawable;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.graphics.Canvas;
import android.graphics.CanvasProperty;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.MathUtils;
import android.view.HardwareCanvas;
import android.view.RenderNodeAnimator;
import android.view.animation.LinearInterpolator;
import com.android.ims.ImsReasonInfo;
import java.util.ArrayList;

/* access modifiers changed from: package-private */
public class RippleBackground {
    private static final float GLOBAL_SPEED = 1.0f;
    private static final TimeInterpolator LINEAR_INTERPOLATOR = new LinearInterpolator();
    private static final float WAVE_OPACITY_DECAY_VELOCITY = 3.0f;
    private static final float WAVE_OUTER_OPACITY_ENTER_VELOCITY = 10.0f;
    private static final float WAVE_OUTER_OPACITY_EXIT_VELOCITY_MAX = 4.5f;
    private static final float WAVE_OUTER_OPACITY_EXIT_VELOCITY_MIN = 1.5f;
    private static final float WAVE_OUTER_SIZE_INFLUENCE_MAX = 200.0f;
    private static final float WAVE_OUTER_SIZE_INFLUENCE_MIN = 40.0f;
    private ObjectAnimator mAnimOuterOpacity;
    private final AnimatorListenerAdapter mAnimationListener = new AnimatorListenerAdapter() {
        /* class android.graphics.drawable.RippleBackground.AnonymousClass2 */

        @Override // android.animation.Animator.AnimatorListener, android.animation.AnimatorListenerAdapter
        public void onAnimationEnd(Animator animation) {
            RippleBackground.this.mHardwareAnimating = false;
        }
    };
    private final Rect mBounds;
    private boolean mCanUseHardware;
    private int mColorAlpha;
    private int mColorOpaque;
    private float mDensity;
    private boolean mHardwareAnimating;
    private boolean mHasMaxRadius;
    private float mOuterOpacity = 0.0f;
    private float mOuterRadius;
    private float mOuterX;
    private float mOuterY;
    private final RippleDrawable mOwner;
    private final ArrayList<RenderNodeAnimator> mPendingAnimations = new ArrayList<>();
    private CanvasProperty<Paint> mPropOuterPaint;
    private CanvasProperty<Float> mPropOuterRadius;
    private CanvasProperty<Float> mPropOuterX;
    private CanvasProperty<Float> mPropOuterY;
    private final ArrayList<RenderNodeAnimator> mRunningAnimations = new ArrayList<>();
    private Paint mTempPaint;

    public RippleBackground(RippleDrawable owner, Rect bounds) {
        this.mOwner = owner;
        this.mBounds = bounds;
    }

    public void setup(int maxRadius, int color, float density) {
        this.mColorOpaque = -16777216 | color;
        this.mColorAlpha = Color.alpha(color) / 2;
        if (maxRadius != -1) {
            this.mHasMaxRadius = true;
            this.mOuterRadius = (float) maxRadius;
        } else {
            float halfWidth = ((float) this.mBounds.width()) / 2.0f;
            float halfHeight = ((float) this.mBounds.height()) / 2.0f;
            this.mOuterRadius = (float) Math.sqrt((double) ((halfWidth * halfWidth) + (halfHeight * halfHeight)));
        }
        this.mOuterX = 0.0f;
        this.mOuterY = 0.0f;
        this.mDensity = density;
    }

    public boolean isHardwareAnimating() {
        return this.mHardwareAnimating;
    }

    public void onHotspotBoundsChanged() {
        if (!this.mHasMaxRadius) {
            float halfWidth = ((float) this.mBounds.width()) / 2.0f;
            float halfHeight = ((float) this.mBounds.height()) / 2.0f;
            this.mOuterRadius = (float) Math.sqrt((double) ((halfWidth * halfWidth) + (halfHeight * halfHeight)));
        }
    }

    public void setOuterOpacity(float a) {
        this.mOuterOpacity = a;
        invalidateSelf();
    }

    public float getOuterOpacity() {
        return this.mOuterOpacity;
    }

    public boolean draw(Canvas c, Paint p) {
        boolean canUseHardware = c.isHardwareAccelerated();
        if (this.mCanUseHardware != canUseHardware && this.mCanUseHardware) {
            cancelHardwareAnimations(true);
        }
        this.mCanUseHardware = canUseHardware;
        if (!canUseHardware || !this.mHardwareAnimating) {
            return drawSoftware(c, p);
        }
        return drawHardware((HardwareCanvas) c);
    }

    public boolean shouldDraw() {
        return (this.mCanUseHardware && this.mHardwareAnimating) || (((int) ((((float) this.mColorAlpha) * this.mOuterOpacity) + 0.5f)) > 0 && this.mOuterRadius > 0.0f);
    }

    private boolean drawHardware(HardwareCanvas c) {
        ArrayList<RenderNodeAnimator> pendingAnimations = this.mPendingAnimations;
        int N = pendingAnimations.size();
        if (N > 0) {
            cancelHardwareAnimations(false);
            this.mHardwareAnimating = true;
            for (int i = 0; i < N; i++) {
                pendingAnimations.get(i).setTarget((Canvas) c);
                pendingAnimations.get(i).start();
            }
            this.mRunningAnimations.addAll(pendingAnimations);
            pendingAnimations.clear();
        }
        c.drawCircle(this.mPropOuterX, this.mPropOuterY, this.mPropOuterRadius, this.mPropOuterPaint);
        return true;
    }

    private boolean drawSoftware(Canvas c, Paint p) {
        p.setColor(this.mColorOpaque);
        int outerAlpha = (int) ((((float) this.mColorAlpha) * this.mOuterOpacity) + 0.5f);
        if (outerAlpha <= 0 || this.mOuterRadius <= 0.0f) {
            return false;
        }
        p.setAlpha(outerAlpha);
        p.setStyle(Paint.Style.FILL);
        c.drawCircle(this.mOuterX, this.mOuterY, this.mOuterRadius, p);
        return true;
    }

    public void getBounds(Rect bounds) {
        int outerX = (int) this.mOuterX;
        int outerY = (int) this.mOuterY;
        int r = ((int) this.mOuterRadius) + 1;
        bounds.set(outerX - r, outerY - r, outerX + r, outerY + r);
    }

    public void enter() {
        cancel();
        ObjectAnimator outer = ObjectAnimator.ofFloat(this, "outerOpacity", 0.0f, 1.0f);
        outer.setAutoCancel(true);
        outer.setDuration(100L);
        outer.setInterpolator(LINEAR_INTERPOLATOR);
        this.mAnimOuterOpacity = outer;
        outer.start();
    }

    public void exit() {
        cancel();
        float outerSizeInfluence = MathUtils.constrain((this.mOuterRadius - (WAVE_OUTER_SIZE_INFLUENCE_MIN * this.mDensity)) / (WAVE_OUTER_SIZE_INFLUENCE_MAX * this.mDensity), 0.0f, 1.0f);
        float outerOpacityVelocity = MathUtils.lerp(WAVE_OUTER_OPACITY_EXIT_VELOCITY_MIN, WAVE_OUTER_OPACITY_EXIT_VELOCITY_MAX, outerSizeInfluence);
        int inflectionDuration = Math.max(0, (int) ((((1.0f - this.mOuterOpacity) * 1000.0f) / (WAVE_OPACITY_DECAY_VELOCITY + outerOpacityVelocity)) + 0.5f));
        int inflectionOpacity = (int) ((((float) this.mColorAlpha) * (this.mOuterOpacity + (((((float) inflectionDuration) * outerOpacityVelocity) * outerSizeInfluence) / 1000.0f))) + 0.5f);
        if (this.mCanUseHardware) {
            exitHardware(ImsReasonInfo.CODE_SIP_NOT_FOUND, inflectionDuration, inflectionOpacity);
        } else {
            exitSoftware(ImsReasonInfo.CODE_SIP_NOT_FOUND, inflectionDuration, inflectionOpacity);
        }
    }

    private void exitHardware(int opacityDuration, int inflectionDuration, int inflectionOpacity) {
        RenderNodeAnimator outerOpacityAnim;
        this.mPendingAnimations.clear();
        Paint outerPaint = getTempPaint();
        outerPaint.setAntiAlias(true);
        outerPaint.setColor(this.mColorOpaque);
        outerPaint.setAlpha((int) ((((float) this.mColorAlpha) * this.mOuterOpacity) + 0.5f));
        outerPaint.setStyle(Paint.Style.FILL);
        this.mPropOuterPaint = CanvasProperty.createPaint(outerPaint);
        this.mPropOuterRadius = CanvasProperty.createFloat(this.mOuterRadius);
        this.mPropOuterX = CanvasProperty.createFloat(this.mOuterX);
        this.mPropOuterY = CanvasProperty.createFloat(this.mOuterY);
        if (inflectionDuration > 0) {
            outerOpacityAnim = new RenderNodeAnimator(this.mPropOuterPaint, 1, (float) inflectionOpacity);
            outerOpacityAnim.setDuration((long) inflectionDuration);
            outerOpacityAnim.setInterpolator(LINEAR_INTERPOLATOR);
            int outerDuration = opacityDuration - inflectionDuration;
            if (outerDuration > 0) {
                RenderNodeAnimator outerFadeOutAnim = new RenderNodeAnimator(this.mPropOuterPaint, 1, 0.0f);
                outerFadeOutAnim.setDuration((long) outerDuration);
                outerFadeOutAnim.setInterpolator(LINEAR_INTERPOLATOR);
                outerFadeOutAnim.setStartDelay((long) inflectionDuration);
                outerFadeOutAnim.setStartValue((float) inflectionOpacity);
                outerFadeOutAnim.addListener(this.mAnimationListener);
                this.mPendingAnimations.add(outerFadeOutAnim);
            } else {
                outerOpacityAnim.addListener(this.mAnimationListener);
            }
        } else {
            outerOpacityAnim = new RenderNodeAnimator(this.mPropOuterPaint, 1, 0.0f);
            outerOpacityAnim.setInterpolator(LINEAR_INTERPOLATOR);
            outerOpacityAnim.setDuration((long) opacityDuration);
            outerOpacityAnim.addListener(this.mAnimationListener);
        }
        this.mPendingAnimations.add(outerOpacityAnim);
        this.mHardwareAnimating = true;
        this.mOuterOpacity = 0.0f;
        invalidateSelf();
    }

    public void jump() {
        endSoftwareAnimations();
        cancelHardwareAnimations(true);
    }

    private void endSoftwareAnimations() {
        if (this.mAnimOuterOpacity != null) {
            this.mAnimOuterOpacity.end();
            this.mAnimOuterOpacity = null;
        }
    }

    private Paint getTempPaint() {
        if (this.mTempPaint == null) {
            this.mTempPaint = new Paint();
        }
        return this.mTempPaint;
    }

    private void exitSoftware(int opacityDuration, int inflectionDuration, int inflectionOpacity) {
        ObjectAnimator outerOpacityAnim;
        if (inflectionDuration > 0) {
            outerOpacityAnim = ObjectAnimator.ofFloat(this, "outerOpacity", ((float) inflectionOpacity) / 255.0f);
            outerOpacityAnim.setAutoCancel(true);
            outerOpacityAnim.setDuration((long) inflectionDuration);
            outerOpacityAnim.setInterpolator(LINEAR_INTERPOLATOR);
            final int outerDuration = opacityDuration - inflectionDuration;
            if (outerDuration > 0) {
                outerOpacityAnim.addListener(new AnimatorListenerAdapter() {
                    /* class android.graphics.drawable.RippleBackground.AnonymousClass1 */

                    @Override // android.animation.Animator.AnimatorListener, android.animation.AnimatorListenerAdapter
                    public void onAnimationEnd(Animator animation) {
                        ObjectAnimator outerFadeOutAnim = ObjectAnimator.ofFloat(RippleBackground.this, "outerOpacity", 0.0f);
                        outerFadeOutAnim.setAutoCancel(true);
                        outerFadeOutAnim.setDuration((long) outerDuration);
                        outerFadeOutAnim.setInterpolator(RippleBackground.LINEAR_INTERPOLATOR);
                        outerFadeOutAnim.addListener(RippleBackground.this.mAnimationListener);
                        RippleBackground.this.mAnimOuterOpacity = outerFadeOutAnim;
                        outerFadeOutAnim.start();
                    }

                    @Override // android.animation.Animator.AnimatorListener, android.animation.AnimatorListenerAdapter
                    public void onAnimationCancel(Animator animation) {
                        animation.removeListener(this);
                    }
                });
            } else {
                outerOpacityAnim.addListener(this.mAnimationListener);
            }
        } else {
            outerOpacityAnim = ObjectAnimator.ofFloat(this, "outerOpacity", 0.0f);
            outerOpacityAnim.setAutoCancel(true);
            outerOpacityAnim.setDuration((long) opacityDuration);
            outerOpacityAnim.addListener(this.mAnimationListener);
        }
        this.mAnimOuterOpacity = outerOpacityAnim;
        outerOpacityAnim.start();
    }

    public void cancel() {
        cancelSoftwareAnimations();
        cancelHardwareAnimations(true);
    }

    private void cancelSoftwareAnimations() {
        if (this.mAnimOuterOpacity != null) {
            this.mAnimOuterOpacity.cancel();
            this.mAnimOuterOpacity = null;
        }
    }

    private void cancelHardwareAnimations(boolean cancelPending) {
        ArrayList<RenderNodeAnimator> runningAnimations = this.mRunningAnimations;
        int N = runningAnimations.size();
        for (int i = 0; i < N; i++) {
            runningAnimations.get(i).cancel();
        }
        runningAnimations.clear();
        if (cancelPending && !this.mPendingAnimations.isEmpty()) {
            this.mPendingAnimations.clear();
        }
        this.mHardwareAnimating = false;
    }

    private void invalidateSelf() {
        this.mOwner.invalidateSelf();
    }
}

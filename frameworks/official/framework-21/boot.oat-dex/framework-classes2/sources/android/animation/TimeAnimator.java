package android.animation;

public class TimeAnimator extends ValueAnimator {
    private TimeListener mListener;
    private long mPreviousTime = -1;

    public interface TimeListener {
        void onTimeUpdate(TimeAnimator timeAnimator, long j, long j2);
    }

    @Override // android.animation.Animator, android.animation.ValueAnimator
    public void start() {
        this.mPreviousTime = -1;
        super.start();
    }

    /* access modifiers changed from: package-private */
    @Override // android.animation.ValueAnimator
    public boolean animationFrame(long currentTime) {
        long deltaTime = 0;
        if (this.mListener == null) {
            return false;
        }
        long totalTime = currentTime - this.mStartTime;
        if (this.mPreviousTime >= 0) {
            deltaTime = currentTime - this.mPreviousTime;
        }
        this.mPreviousTime = currentTime;
        this.mListener.onTimeUpdate(this, totalTime, deltaTime);
        return false;
    }

    public void setTimeListener(TimeListener listener) {
        this.mListener = listener;
    }

    /* access modifiers changed from: package-private */
    @Override // android.animation.ValueAnimator
    public void animateValue(float fraction) {
    }

    /* access modifiers changed from: package-private */
    @Override // android.animation.ValueAnimator
    public void initAnimation() {
    }
}

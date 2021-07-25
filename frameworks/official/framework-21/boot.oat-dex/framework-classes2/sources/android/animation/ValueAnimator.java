package android.animation;

import android.animation.Animator;
import android.os.Looper;
import android.os.Trace;
import android.util.AndroidRuntimeException;
import android.view.Choreographer;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class ValueAnimator extends Animator {
    public static final int INFINITE = -1;
    public static final int RESTART = 1;
    public static final int REVERSE = 2;
    static final int RUNNING = 1;
    static final int SEEKED = 2;
    static final int STOPPED = 0;
    protected static ThreadLocal<AnimationHandler> sAnimationHandler = new ThreadLocal<>();
    private static final TimeInterpolator sDefaultInterpolator = new AccelerateDecelerateInterpolator();
    private static float sDurationScale = 1.0f;
    private float mCurrentFraction = 0.0f;
    private int mCurrentIteration = 0;
    private long mDelayStartTime;
    private long mDuration = ((long) (300.0f * sDurationScale));
    boolean mInitialized = false;
    private TimeInterpolator mInterpolator = sDefaultInterpolator;
    private long mPauseTime;
    private boolean mPlayingBackwards = false;
    int mPlayingState = 0;
    private int mRepeatCount = 0;
    private int mRepeatMode = 1;
    private boolean mResumed = false;
    private boolean mRunning = false;
    long mSeekTime = -1;
    private long mStartDelay = 0;
    private boolean mStartListenersCalled = false;
    long mStartTime;
    private boolean mStarted = false;
    private boolean mStartedDelay = false;
    private long mUnscaledDuration = 300;
    private long mUnscaledStartDelay = 0;
    ArrayList<AnimatorUpdateListener> mUpdateListeners = null;
    PropertyValuesHolder[] mValues;
    HashMap<String, PropertyValuesHolder> mValuesMap;

    public interface AnimatorUpdateListener {
        void onAnimationUpdate(ValueAnimator valueAnimator);
    }

    public static void setDurationScale(float durationScale) {
        sDurationScale = durationScale;
    }

    public static float getDurationScale() {
        return sDurationScale;
    }

    public static ValueAnimator ofInt(int... values) {
        ValueAnimator anim = new ValueAnimator();
        anim.setIntValues(values);
        return anim;
    }

    public static ValueAnimator ofArgb(int... values) {
        ValueAnimator anim = new ValueAnimator();
        anim.setIntValues(values);
        anim.setEvaluator(ArgbEvaluator.getInstance());
        return anim;
    }

    public static ValueAnimator ofFloat(float... values) {
        ValueAnimator anim = new ValueAnimator();
        anim.setFloatValues(values);
        return anim;
    }

    public static ValueAnimator ofPropertyValuesHolder(PropertyValuesHolder... values) {
        ValueAnimator anim = new ValueAnimator();
        anim.setValues(values);
        return anim;
    }

    public static ValueAnimator ofObject(TypeEvaluator evaluator, Object... values) {
        ValueAnimator anim = new ValueAnimator();
        anim.setObjectValues(values);
        anim.setEvaluator(evaluator);
        return anim;
    }

    public void setIntValues(int... values) {
        if (values != null && values.length != 0) {
            if (this.mValues == null || this.mValues.length == 0) {
                setValues(PropertyValuesHolder.ofInt("", values));
            } else {
                this.mValues[0].setIntValues(values);
            }
            this.mInitialized = false;
        }
    }

    public void setFloatValues(float... values) {
        if (values != null && values.length != 0) {
            if (this.mValues == null || this.mValues.length == 0) {
                setValues(PropertyValuesHolder.ofFloat("", values));
            } else {
                this.mValues[0].setFloatValues(values);
            }
            this.mInitialized = false;
        }
    }

    public void setObjectValues(Object... values) {
        if (values != null && values.length != 0) {
            if (this.mValues == null || this.mValues.length == 0) {
                setValues(PropertyValuesHolder.ofObject("", (TypeEvaluator) null, values));
            } else {
                this.mValues[0].setObjectValues(values);
            }
            this.mInitialized = false;
        }
    }

    public void setValues(PropertyValuesHolder... values) {
        int numValues = values.length;
        this.mValues = values;
        this.mValuesMap = new HashMap<>(numValues);
        for (PropertyValuesHolder valuesHolder : values) {
            this.mValuesMap.put(valuesHolder.getPropertyName(), valuesHolder);
        }
        this.mInitialized = false;
    }

    public PropertyValuesHolder[] getValues() {
        return this.mValues;
    }

    /* access modifiers changed from: package-private */
    public void initAnimation() {
        if (!this.mInitialized) {
            int numValues = this.mValues.length;
            for (int i = 0; i < numValues; i++) {
                this.mValues[i].init();
            }
            this.mInitialized = true;
        }
    }

    @Override // android.animation.Animator
    public ValueAnimator setDuration(long duration) {
        if (duration < 0) {
            throw new IllegalArgumentException("Animators cannot have negative duration: " + duration);
        }
        this.mUnscaledDuration = duration;
        updateScaledDuration();
        return this;
    }

    private void updateScaledDuration() {
        this.mDuration = (long) (((float) this.mUnscaledDuration) * sDurationScale);
    }

    @Override // android.animation.Animator
    public long getDuration() {
        return this.mUnscaledDuration;
    }

    public void setCurrentPlayTime(long playTime) {
        initAnimation();
        long currentTime = AnimationUtils.currentAnimationTimeMillis();
        if (this.mPlayingState != 1) {
            this.mSeekTime = playTime;
            this.mPlayingState = 2;
        }
        this.mStartTime = currentTime - playTime;
        doAnimationFrame(currentTime);
    }

    public long getCurrentPlayTime() {
        if (!this.mInitialized || this.mPlayingState == 0) {
            return 0;
        }
        return AnimationUtils.currentAnimationTimeMillis() - this.mStartTime;
    }

    /* access modifiers changed from: protected */
    public static class AnimationHandler implements Runnable {
        private boolean mAnimationScheduled;
        protected final ArrayList<ValueAnimator> mAnimations;
        private final Choreographer mChoreographer;
        protected final ArrayList<ValueAnimator> mDelayedAnims;
        private final ArrayList<ValueAnimator> mEndingAnims;
        protected final ArrayList<ValueAnimator> mPendingAnimations;
        private final ArrayList<ValueAnimator> mReadyAnims;
        private final ArrayList<ValueAnimator> mTmpAnimations;

        private AnimationHandler() {
            this.mAnimations = new ArrayList<>();
            this.mTmpAnimations = new ArrayList<>();
            this.mPendingAnimations = new ArrayList<>();
            this.mDelayedAnims = new ArrayList<>();
            this.mEndingAnims = new ArrayList<>();
            this.mReadyAnims = new ArrayList<>();
            this.mChoreographer = Choreographer.getInstance();
        }

        public void start() {
            scheduleAnimation();
        }

        private void doAnimationFrame(long frameTime) {
            while (this.mPendingAnimations.size() > 0) {
                ArrayList<ValueAnimator> pendingCopy = (ArrayList) this.mPendingAnimations.clone();
                this.mPendingAnimations.clear();
                int count = pendingCopy.size();
                for (int i = 0; i < count; i++) {
                    ValueAnimator anim = pendingCopy.get(i);
                    if (anim.mStartDelay == 0) {
                        anim.startAnimation(this);
                    } else {
                        this.mDelayedAnims.add(anim);
                    }
                }
            }
            int numDelayedAnims = this.mDelayedAnims.size();
            for (int i2 = 0; i2 < numDelayedAnims; i2++) {
                ValueAnimator anim2 = this.mDelayedAnims.get(i2);
                if (anim2.delayedAnimationFrame(frameTime)) {
                    this.mReadyAnims.add(anim2);
                }
            }
            int numReadyAnims = this.mReadyAnims.size();
            if (numReadyAnims > 0) {
                for (int i3 = 0; i3 < numReadyAnims; i3++) {
                    ValueAnimator anim3 = this.mReadyAnims.get(i3);
                    anim3.startAnimation(this);
                    anim3.mRunning = true;
                    this.mDelayedAnims.remove(anim3);
                }
                this.mReadyAnims.clear();
            }
            int numAnims = this.mAnimations.size();
            for (int i4 = 0; i4 < numAnims; i4++) {
                this.mTmpAnimations.add(this.mAnimations.get(i4));
            }
            for (int i5 = 0; i5 < numAnims; i5++) {
                ValueAnimator anim4 = this.mTmpAnimations.get(i5);
                if (this.mAnimations.contains(anim4) && anim4.doAnimationFrame(frameTime)) {
                    this.mEndingAnims.add(anim4);
                }
            }
            this.mTmpAnimations.clear();
            if (this.mEndingAnims.size() > 0) {
                for (int i6 = 0; i6 < this.mEndingAnims.size(); i6++) {
                    this.mEndingAnims.get(i6).endAnimation(this);
                }
                this.mEndingAnims.clear();
            }
            if (!this.mAnimations.isEmpty() || !this.mDelayedAnims.isEmpty()) {
                scheduleAnimation();
            }
        }

        public void run() {
            this.mAnimationScheduled = false;
            doAnimationFrame(this.mChoreographer.getFrameTime());
        }

        private void scheduleAnimation() {
            if (!this.mAnimationScheduled) {
                this.mChoreographer.postCallback(1, this, null);
                this.mAnimationScheduled = true;
            }
        }
    }

    @Override // android.animation.Animator
    public long getStartDelay() {
        return this.mUnscaledStartDelay;
    }

    @Override // android.animation.Animator
    public void setStartDelay(long startDelay) {
        this.mStartDelay = (long) (((float) startDelay) * sDurationScale);
        this.mUnscaledStartDelay = startDelay;
    }

    public static long getFrameDelay() {
        return Choreographer.getFrameDelay();
    }

    public static void setFrameDelay(long frameDelay) {
        Choreographer.setFrameDelay(frameDelay);
    }

    public Object getAnimatedValue() {
        if (this.mValues == null || this.mValues.length <= 0) {
            return null;
        }
        return this.mValues[0].getAnimatedValue();
    }

    public Object getAnimatedValue(String propertyName) {
        PropertyValuesHolder valuesHolder = this.mValuesMap.get(propertyName);
        if (valuesHolder != null) {
            return valuesHolder.getAnimatedValue();
        }
        return null;
    }

    public void setRepeatCount(int value) {
        this.mRepeatCount = value;
    }

    public int getRepeatCount() {
        return this.mRepeatCount;
    }

    public void setRepeatMode(int value) {
        this.mRepeatMode = value;
    }

    public int getRepeatMode() {
        return this.mRepeatMode;
    }

    public void addUpdateListener(AnimatorUpdateListener listener) {
        if (this.mUpdateListeners == null) {
            this.mUpdateListeners = new ArrayList<>();
        }
        this.mUpdateListeners.add(listener);
    }

    public void removeAllUpdateListeners() {
        if (this.mUpdateListeners != null) {
            this.mUpdateListeners.clear();
            this.mUpdateListeners = null;
        }
    }

    public void removeUpdateListener(AnimatorUpdateListener listener) {
        if (this.mUpdateListeners != null) {
            this.mUpdateListeners.remove(listener);
            if (this.mUpdateListeners.size() == 0) {
                this.mUpdateListeners = null;
            }
        }
    }

    @Override // android.animation.Animator
    public void setInterpolator(TimeInterpolator value) {
        if (value != null) {
            this.mInterpolator = value;
        } else {
            this.mInterpolator = new LinearInterpolator();
        }
    }

    @Override // android.animation.Animator
    public TimeInterpolator getInterpolator() {
        return this.mInterpolator;
    }

    public void setEvaluator(TypeEvaluator value) {
        if (value != null && this.mValues != null && this.mValues.length > 0) {
            this.mValues[0].setEvaluator(value);
        }
    }

    private void notifyStartListeners() {
        if (this.mListeners != null && !this.mStartListenersCalled) {
            ArrayList<Animator.AnimatorListener> tmpListeners = (ArrayList) this.mListeners.clone();
            int numListeners = tmpListeners.size();
            for (int i = 0; i < numListeners; i++) {
                tmpListeners.get(i).onAnimationStart(this);
            }
        }
        this.mStartListenersCalled = true;
    }

    private void start(boolean playBackwards) {
        if (Looper.myLooper() == null) {
            throw new AndroidRuntimeException("Animators may only be run on Looper threads");
        }
        this.mPlayingBackwards = playBackwards;
        this.mCurrentIteration = 0;
        this.mPlayingState = 0;
        this.mStarted = true;
        this.mStartedDelay = false;
        this.mPaused = false;
        updateScaledDuration();
        AnimationHandler animationHandler = getOrCreateAnimationHandler();
        animationHandler.mPendingAnimations.add(this);
        if (this.mStartDelay == 0) {
            setCurrentPlayTime(0);
            this.mPlayingState = 0;
            this.mRunning = true;
            notifyStartListeners();
        }
        animationHandler.start();
    }

    @Override // android.animation.Animator
    public void start() {
        start(false);
    }

    @Override // android.animation.Animator
    public void cancel() {
        AnimationHandler handler = getOrCreateAnimationHandler();
        if (this.mPlayingState != 0 || handler.mPendingAnimations.contains(this) || handler.mDelayedAnims.contains(this)) {
            if ((this.mStarted || this.mRunning) && this.mListeners != null) {
                if (!this.mRunning) {
                    notifyStartListeners();
                }
                Iterator i$ = ((ArrayList) this.mListeners.clone()).iterator();
                while (i$.hasNext()) {
                    i$.next().onAnimationCancel(this);
                }
            }
            endAnimation(handler);
        }
    }

    @Override // android.animation.Animator
    public void end() {
        AnimationHandler handler = getOrCreateAnimationHandler();
        if (!handler.mAnimations.contains(this) && !handler.mPendingAnimations.contains(this)) {
            this.mStartedDelay = false;
            startAnimation(handler);
            this.mStarted = true;
        } else if (!this.mInitialized) {
            initAnimation();
        }
        animateValue(this.mPlayingBackwards ? 0.0f : 1.0f);
        endAnimation(handler);
    }

    @Override // android.animation.Animator
    public void resume() {
        if (this.mPaused) {
            this.mResumed = true;
        }
        super.resume();
    }

    @Override // android.animation.Animator
    public void pause() {
        boolean previouslyPaused = this.mPaused;
        super.pause();
        if (!previouslyPaused && this.mPaused) {
            this.mPauseTime = -1;
            this.mResumed = false;
        }
    }

    @Override // android.animation.Animator
    public boolean isRunning() {
        return this.mPlayingState == 1 || this.mRunning;
    }

    @Override // android.animation.Animator
    public boolean isStarted() {
        return this.mStarted;
    }

    @Override // android.animation.Animator
    public void reverse() {
        this.mPlayingBackwards = !this.mPlayingBackwards;
        if (this.mPlayingState == 1) {
            long currentTime = AnimationUtils.currentAnimationTimeMillis();
            this.mStartTime = currentTime - (this.mDuration - (currentTime - this.mStartTime));
        } else if (this.mStarted) {
            end();
        } else {
            start(true);
        }
    }

    @Override // android.animation.Animator
    public boolean canReverse() {
        return true;
    }

    /* access modifiers changed from: protected */
    public void endAnimation(AnimationHandler handler) {
        handler.mAnimations.remove(this);
        handler.mPendingAnimations.remove(this);
        handler.mDelayedAnims.remove(this);
        this.mPlayingState = 0;
        this.mPaused = false;
        if ((this.mStarted || this.mRunning) && this.mListeners != null) {
            if (!this.mRunning) {
                notifyStartListeners();
            }
            ArrayList<Animator.AnimatorListener> tmpListeners = (ArrayList) this.mListeners.clone();
            int numListeners = tmpListeners.size();
            for (int i = 0; i < numListeners; i++) {
                tmpListeners.get(i).onAnimationEnd(this);
            }
        }
        this.mRunning = false;
        this.mStarted = false;
        this.mStartListenersCalled = false;
        this.mPlayingBackwards = false;
        if (Trace.isTagEnabled(8)) {
            Trace.asyncTraceEnd(8, getNameForTrace(), System.identityHashCode(this));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startAnimation(AnimationHandler handler) {
        if (Trace.isTagEnabled(8)) {
            Trace.asyncTraceBegin(8, getNameForTrace(), System.identityHashCode(this));
        }
        initAnimation();
        handler.mAnimations.add(this);
        if (this.mStartDelay > 0 && this.mListeners != null) {
            notifyStartListeners();
        }
    }

    /* access modifiers changed from: package-private */
    public String getNameForTrace() {
        return "animator";
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean delayedAnimationFrame(long currentTime) {
        if (!this.mStartedDelay) {
            this.mStartedDelay = true;
            this.mDelayStartTime = currentTime;
        }
        if (!this.mPaused) {
            if (this.mResumed) {
                this.mResumed = false;
                if (this.mPauseTime > 0) {
                    this.mDelayStartTime += currentTime - this.mPauseTime;
                }
            }
            long deltaTime = currentTime - this.mDelayStartTime;
            if (deltaTime <= this.mStartDelay) {
                return false;
            }
            this.mStartTime = currentTime - (deltaTime - this.mStartDelay);
            this.mPlayingState = 1;
            return true;
        } else if (this.mPauseTime >= 0) {
            return false;
        } else {
            this.mPauseTime = currentTime;
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean animationFrame(long currentTime) {
        boolean done = false;
        switch (this.mPlayingState) {
            case 1:
            case 2:
                float fraction = this.mDuration > 0 ? ((float) (currentTime - this.mStartTime)) / ((float) this.mDuration) : 1.0f;
                if (fraction >= 1.0f) {
                    if (this.mCurrentIteration < this.mRepeatCount || this.mRepeatCount == -1) {
                        if (this.mListeners != null) {
                            int numListeners = this.mListeners.size();
                            for (int i = 0; i < numListeners; i++) {
                                ((Animator.AnimatorListener) this.mListeners.get(i)).onAnimationRepeat(this);
                            }
                        }
                        if (this.mRepeatMode == 2) {
                            this.mPlayingBackwards = !this.mPlayingBackwards;
                        }
                        this.mCurrentIteration += (int) fraction;
                        fraction %= 1.0f;
                        this.mStartTime += this.mDuration;
                    } else {
                        done = true;
                        fraction = Math.min(fraction, 1.0f);
                    }
                }
                if (this.mPlayingBackwards) {
                    fraction = 1.0f - fraction;
                }
                animateValue(fraction);
                break;
        }
        return done;
    }

    /* access modifiers changed from: package-private */
    public final boolean doAnimationFrame(long frameTime) {
        if (this.mPlayingState == 0) {
            this.mPlayingState = 1;
            if (this.mSeekTime < 0) {
                this.mStartTime = frameTime;
            } else {
                this.mStartTime = frameTime - this.mSeekTime;
                this.mSeekTime = -1;
            }
        }
        if (!this.mPaused) {
            if (this.mResumed) {
                this.mResumed = false;
                if (this.mPauseTime > 0) {
                    this.mStartTime += frameTime - this.mPauseTime;
                }
            }
            return animationFrame(Math.max(frameTime, this.mStartTime));
        } else if (this.mPauseTime >= 0) {
            return false;
        } else {
            this.mPauseTime = frameTime;
            return false;
        }
    }

    public float getAnimatedFraction() {
        return this.mCurrentFraction;
    }

    /* access modifiers changed from: package-private */
    public void animateValue(float fraction) {
        float fraction2 = this.mInterpolator.getInterpolation(fraction);
        this.mCurrentFraction = fraction2;
        int numValues = this.mValues.length;
        for (int i = 0; i < numValues; i++) {
            this.mValues[i].calculateValue(fraction2);
        }
        if (this.mUpdateListeners != null) {
            int numListeners = this.mUpdateListeners.size();
            for (int i2 = 0; i2 < numListeners; i2++) {
                this.mUpdateListeners.get(i2).onAnimationUpdate(this);
            }
        }
    }

    @Override // java.lang.Object, android.animation.Animator, android.animation.Animator
    public ValueAnimator clone() {
        ValueAnimator anim = (ValueAnimator) super.clone();
        if (this.mUpdateListeners != null) {
            ArrayList<AnimatorUpdateListener> oldListeners = this.mUpdateListeners;
            anim.mUpdateListeners = new ArrayList<>();
            int numListeners = oldListeners.size();
            for (int i = 0; i < numListeners; i++) {
                anim.mUpdateListeners.add(oldListeners.get(i));
            }
        }
        anim.mSeekTime = -1;
        anim.mPlayingBackwards = false;
        anim.mCurrentIteration = 0;
        anim.mInitialized = false;
        anim.mPlayingState = 0;
        anim.mStartedDelay = false;
        PropertyValuesHolder[] oldValues = this.mValues;
        if (oldValues != null) {
            int numValues = oldValues.length;
            anim.mValues = new PropertyValuesHolder[numValues];
            anim.mValuesMap = new HashMap<>(numValues);
            for (int i2 = 0; i2 < numValues; i2++) {
                PropertyValuesHolder newValuesHolder = oldValues[i2].clone();
                anim.mValues[i2] = newValuesHolder;
                anim.mValuesMap.put(newValuesHolder.getPropertyName(), newValuesHolder);
            }
        }
        return anim;
    }

    public static int getCurrentAnimationsCount() {
        AnimationHandler handler = sAnimationHandler.get();
        if (handler != null) {
            return handler.mAnimations.size();
        }
        return 0;
    }

    public static void clearAllAnimations() {
        AnimationHandler handler = sAnimationHandler.get();
        if (handler != null) {
            handler.mAnimations.clear();
            handler.mPendingAnimations.clear();
            handler.mDelayedAnims.clear();
        }
    }

    private static AnimationHandler getOrCreateAnimationHandler() {
        AnimationHandler handler = sAnimationHandler.get();
        if (handler != null) {
            return handler;
        }
        AnimationHandler handler2 = new AnimationHandler();
        sAnimationHandler.set(handler2);
        return handler2;
    }

    public String toString() {
        String returnVal = "ValueAnimator@" + Integer.toHexString(hashCode());
        if (this.mValues != null) {
            for (int i = 0; i < this.mValues.length; i++) {
                returnVal = returnVal + "\n    " + this.mValues[i].toString();
            }
        }
        return returnVal;
    }

    @Override // android.animation.Animator
    public void setAllowRunningAsynchronously(boolean mayRunAsync) {
    }
}

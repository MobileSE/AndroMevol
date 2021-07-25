package android.animation;

import java.util.ArrayList;

public abstract class Animator implements Cloneable {
    ArrayList<AnimatorListener> mListeners = null;
    ArrayList<AnimatorPauseListener> mPauseListeners = null;
    boolean mPaused = false;

    public interface AnimatorListener {
        void onAnimationCancel(Animator animator);

        void onAnimationEnd(Animator animator);

        void onAnimationRepeat(Animator animator);

        void onAnimationStart(Animator animator);
    }

    public interface AnimatorPauseListener {
        void onAnimationPause(Animator animator);

        void onAnimationResume(Animator animator);
    }

    public abstract long getDuration();

    public abstract long getStartDelay();

    public abstract boolean isRunning();

    public abstract Animator setDuration(long j);

    public abstract void setInterpolator(TimeInterpolator timeInterpolator);

    public abstract void setStartDelay(long j);

    public void start() {
    }

    public void cancel() {
    }

    public void end() {
    }

    public void pause() {
        if (isStarted() && !this.mPaused) {
            this.mPaused = true;
            if (this.mPauseListeners != null) {
                ArrayList<AnimatorPauseListener> tmpListeners = (ArrayList) this.mPauseListeners.clone();
                int numListeners = tmpListeners.size();
                for (int i = 0; i < numListeners; i++) {
                    tmpListeners.get(i).onAnimationPause(this);
                }
            }
        }
    }

    public void resume() {
        if (this.mPaused) {
            this.mPaused = false;
            if (this.mPauseListeners != null) {
                ArrayList<AnimatorPauseListener> tmpListeners = (ArrayList) this.mPauseListeners.clone();
                int numListeners = tmpListeners.size();
                for (int i = 0; i < numListeners; i++) {
                    tmpListeners.get(i).onAnimationResume(this);
                }
            }
        }
    }

    public boolean isPaused() {
        return this.mPaused;
    }

    public TimeInterpolator getInterpolator() {
        return null;
    }

    public boolean isStarted() {
        return isRunning();
    }

    public void addListener(AnimatorListener listener) {
        if (this.mListeners == null) {
            this.mListeners = new ArrayList<>();
        }
        this.mListeners.add(listener);
    }

    public void removeListener(AnimatorListener listener) {
        if (this.mListeners != null) {
            this.mListeners.remove(listener);
            if (this.mListeners.size() == 0) {
                this.mListeners = null;
            }
        }
    }

    public ArrayList<AnimatorListener> getListeners() {
        return this.mListeners;
    }

    public void addPauseListener(AnimatorPauseListener listener) {
        if (this.mPauseListeners == null) {
            this.mPauseListeners = new ArrayList<>();
        }
        this.mPauseListeners.add(listener);
    }

    public void removePauseListener(AnimatorPauseListener listener) {
        if (this.mPauseListeners != null) {
            this.mPauseListeners.remove(listener);
            if (this.mPauseListeners.size() == 0) {
                this.mPauseListeners = null;
            }
        }
    }

    public void removeAllListeners() {
        if (this.mListeners != null) {
            this.mListeners.clear();
            this.mListeners = null;
        }
        if (this.mPauseListeners != null) {
            this.mPauseListeners.clear();
            this.mPauseListeners = null;
        }
    }

    @Override // java.lang.Object
    public Animator clone() {
        try {
            Animator anim = (Animator) super.clone();
            if (this.mListeners != null) {
                ArrayList<AnimatorListener> oldListeners = this.mListeners;
                anim.mListeners = new ArrayList<>();
                int numListeners = oldListeners.size();
                for (int i = 0; i < numListeners; i++) {
                    anim.mListeners.add(oldListeners.get(i));
                }
            }
            if (this.mPauseListeners != null) {
                ArrayList<AnimatorPauseListener> oldListeners2 = this.mPauseListeners;
                anim.mPauseListeners = new ArrayList<>();
                int numListeners2 = oldListeners2.size();
                for (int i2 = 0; i2 < numListeners2; i2++) {
                    anim.mPauseListeners.add(oldListeners2.get(i2));
                }
            }
            return anim;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public void setupStartValues() {
    }

    public void setupEndValues() {
    }

    public void setTarget(Object target) {
    }

    public boolean canReverse() {
        return false;
    }

    public void reverse() {
        throw new IllegalStateException("Reverse is not supported");
    }

    public void setAllowRunningAsynchronously(boolean mayRunAsync) {
    }
}

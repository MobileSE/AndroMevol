package android.graphics.drawable;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.DrawableContainer;
import android.os.SystemClock;
import android.util.AttributeSet;
import com.android.internal.R;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class AnimationDrawable extends DrawableContainer implements Runnable, Animatable {
    private boolean mAnimating;
    private final AnimationState mAnimationState;
    private int mCurFrame;
    private boolean mMutated;
    private boolean mRunning;

    public AnimationDrawable() {
        this(null, null);
    }

    @Override // android.graphics.drawable.DrawableContainer, android.graphics.drawable.Drawable
    public boolean setVisible(boolean visible, boolean restart) {
        int i = 0;
        boolean changed = super.setVisible(visible, restart);
        if (!visible) {
            unscheduleSelf(this);
        } else if (restart || changed) {
            if (!(restart || this.mCurFrame < 0 || this.mCurFrame >= this.mAnimationState.getChildCount())) {
                i = this.mCurFrame;
            }
            setFrame(i, true, this.mAnimating);
        }
        return changed;
    }

    @Override // android.graphics.drawable.Animatable
    public void start() {
        this.mAnimating = true;
        if (!isRunning()) {
            run();
        }
    }

    @Override // android.graphics.drawable.Animatable
    public void stop() {
        this.mAnimating = false;
        if (isRunning()) {
            unscheduleSelf(this);
        }
    }

    @Override // android.graphics.drawable.Animatable
    public boolean isRunning() {
        return this.mRunning;
    }

    public void run() {
        nextFrame(false);
    }

    @Override // android.graphics.drawable.Drawable
    public void unscheduleSelf(Runnable what) {
        this.mCurFrame = -1;
        this.mRunning = false;
        super.unscheduleSelf(what);
    }

    public int getNumberOfFrames() {
        return this.mAnimationState.getChildCount();
    }

    public Drawable getFrame(int index) {
        return this.mAnimationState.getChild(index);
    }

    public int getDuration(int i) {
        return this.mAnimationState.mDurations[i];
    }

    public boolean isOneShot() {
        return this.mAnimationState.mOneShot;
    }

    public void setOneShot(boolean oneShot) {
        this.mAnimationState.mOneShot = oneShot;
    }

    public void addFrame(Drawable frame, int duration) {
        this.mAnimationState.addFrame(frame, duration);
        if (this.mCurFrame < 0) {
            setFrame(0, true, false);
        }
    }

    private void nextFrame(boolean unschedule) {
        int next = this.mCurFrame + 1;
        int N = this.mAnimationState.getChildCount();
        if (next >= N) {
            next = 0;
        }
        setFrame(next, unschedule, !this.mAnimationState.mOneShot || next < N + -1);
    }

    private void setFrame(int frame, boolean unschedule, boolean animate) {
        if (frame < this.mAnimationState.getChildCount()) {
            this.mAnimating = animate;
            this.mCurFrame = frame;
            selectDrawable(frame);
            if (unschedule || animate) {
                unscheduleSelf(this);
            }
            if (animate) {
                this.mCurFrame = frame;
                this.mRunning = true;
                scheduleSelf(this, SystemClock.uptimeMillis() + ((long) this.mAnimationState.mDurations[frame]));
            }
        }
    }

    @Override // android.graphics.drawable.Drawable
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs, Resources.Theme theme) throws XmlPullParserException, IOException {
        int depth;
        int type;
        Drawable dr;
        TypedArray a = obtainAttributes(r, theme, attrs, R.styleable.AnimationDrawable);
        super.inflateWithAttributes(r, parser, a, 0);
        this.mAnimationState.setVariablePadding(a.getBoolean(1, false));
        this.mAnimationState.mOneShot = a.getBoolean(2, false);
        a.recycle();
        int innerDepth = parser.getDepth() + 1;
        while (true) {
            int type2 = parser.next();
            if (type2 == 1 || ((depth = parser.getDepth()) < innerDepth && type2 == 3)) {
                setFrame(0, true, false);
            } else if (type2 == 2 && depth <= innerDepth && parser.getName().equals("item")) {
                TypedArray a2 = obtainAttributes(r, theme, attrs, R.styleable.AnimationDrawableItem);
                int duration = a2.getInt(0, -1);
                if (duration < 0) {
                    throw new XmlPullParserException(parser.getPositionDescription() + ": <item> tag requires a 'duration' attribute");
                }
                int drawableRes = a2.getResourceId(1, 0);
                a2.recycle();
                if (drawableRes != 0) {
                    dr = r.getDrawable(drawableRes, theme);
                } else {
                    do {
                        type = parser.next();
                    } while (type == 4);
                    if (type != 2) {
                        throw new XmlPullParserException(parser.getPositionDescription() + ": <item> tag requires a 'drawable' attribute or child tag" + " defining a drawable");
                    }
                    dr = Drawable.createFromXmlInner(r, parser, attrs, theme);
                }
                this.mAnimationState.addFrame(dr, duration);
                if (dr != null) {
                    dr.setCallback(this);
                }
            }
        }
        setFrame(0, true, false);
    }

    @Override // android.graphics.drawable.DrawableContainer, android.graphics.drawable.Drawable
    public Drawable mutate() {
        if (!this.mMutated && super.mutate() == this) {
            this.mAnimationState.mDurations = (int[]) this.mAnimationState.mDurations.clone();
            this.mMutated = true;
        }
        return this;
    }

    /* access modifiers changed from: private */
    public static final class AnimationState extends DrawableContainer.DrawableContainerState {
        private int[] mDurations;
        private boolean mOneShot;

        AnimationState(AnimationState orig, AnimationDrawable owner, Resources res) {
            super(orig, owner, res);
            if (orig != null) {
                this.mDurations = orig.mDurations;
                this.mOneShot = orig.mOneShot;
                return;
            }
            this.mDurations = new int[getCapacity()];
            this.mOneShot = true;
        }

        @Override // android.graphics.drawable.Drawable.ConstantState
        public Drawable newDrawable() {
            return new AnimationDrawable(this, null);
        }

        @Override // android.graphics.drawable.Drawable.ConstantState
        public Drawable newDrawable(Resources res) {
            return new AnimationDrawable(this, res);
        }

        public void addFrame(Drawable dr, int dur) {
            this.mDurations[super.addChild(dr)] = dur;
        }

        @Override // android.graphics.drawable.DrawableContainer.DrawableContainerState
        public void growArray(int oldSize, int newSize) {
            super.growArray(oldSize, newSize);
            int[] newDurations = new int[newSize];
            System.arraycopy(this.mDurations, 0, newDurations, 0, oldSize);
            this.mDurations = newDurations;
        }
    }

    private AnimationDrawable(AnimationState state, Resources res) {
        this.mCurFrame = -1;
        AnimationState as = new AnimationState(state, this, res);
        this.mAnimationState = as;
        setConstantState(as);
        if (state != null) {
            setFrame(0, true, false);
        }
    }
}

package android.graphics.drawable;

import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.LongSparseLongArray;
import android.util.SparseIntArray;
import android.util.StateSet;
import com.android.internal.R;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class AnimatedStateListDrawable extends StateListDrawable {
    private static final String ELEMENT_ITEM = "item";
    private static final String ELEMENT_TRANSITION = "transition";
    private static final String LOGTAG = AnimatedStateListDrawable.class.getSimpleName();
    private boolean mMutated;
    private AnimatedStateListState mState;
    private Transition mTransition;
    private int mTransitionFromIndex;
    private int mTransitionToIndex;

    public AnimatedStateListDrawable() {
        this(null, null);
    }

    @Override // android.graphics.drawable.DrawableContainer, android.graphics.drawable.Drawable
    public boolean setVisible(boolean visible, boolean restart) {
        boolean changed = super.setVisible(visible, restart);
        if (this.mTransition != null && (changed || restart)) {
            if (visible) {
                this.mTransition.start();
            } else {
                jumpToCurrentState();
            }
        }
        return changed;
    }

    public void addState(int[] stateSet, Drawable drawable, int id) {
        if (drawable == null) {
            throw new IllegalArgumentException("Drawable must not be null");
        }
        this.mState.addStateSet(stateSet, drawable, id);
        onStateChange(getState());
    }

    public <T extends Drawable & Animatable> void addTransition(int fromId, int toId, T transition, boolean reversible) {
        if (transition == null) {
            throw new IllegalArgumentException("Transition drawable must not be null");
        }
        this.mState.addTransition(fromId, toId, transition, reversible);
    }

    @Override // android.graphics.drawable.DrawableContainer, android.graphics.drawable.StateListDrawable, android.graphics.drawable.Drawable
    public boolean isStateful() {
        return true;
    }

    /* access modifiers changed from: protected */
    @Override // android.graphics.drawable.DrawableContainer, android.graphics.drawable.StateListDrawable, android.graphics.drawable.Drawable
    public boolean onStateChange(int[] stateSet) {
        int keyframeIndex = this.mState.indexOfKeyframe(stateSet);
        if (keyframeIndex == getCurrentIndex()) {
            Drawable current = getCurrent();
            if (current != null) {
                return current.setState(stateSet);
            }
            return false;
        } else if (selectTransition(keyframeIndex) || selectDrawable(keyframeIndex)) {
            return true;
        } else {
            return super.onStateChange(stateSet);
        }
    }

    private boolean selectTransition(int toIndex) {
        int fromIndex;
        Transition transition;
        Transition currentTransition = this.mTransition;
        if (currentTransition == null) {
            fromIndex = getCurrentIndex();
        } else if (toIndex == this.mTransitionToIndex) {
            return true;
        } else {
            if (toIndex != this.mTransitionFromIndex || !currentTransition.canReverse()) {
                fromIndex = this.mTransitionToIndex;
                currentTransition.stop();
            } else {
                currentTransition.reverse();
                this.mTransitionToIndex = this.mTransitionFromIndex;
                this.mTransitionFromIndex = toIndex;
                return true;
            }
        }
        this.mTransition = null;
        this.mTransitionFromIndex = -1;
        this.mTransitionToIndex = -1;
        AnimatedStateListState state = this.mState;
        int fromId = state.getKeyframeIdAt(fromIndex);
        int toId = state.getKeyframeIdAt(toIndex);
        if (toId == 0 || fromId == 0) {
            return false;
        }
        int transitionIndex = state.indexOfTransition(fromId, toId);
        if (transitionIndex < 0) {
            return false;
        }
        selectDrawable(transitionIndex);
        Drawable d = getCurrent();
        if (d instanceof AnimationDrawable) {
            transition = new AnimationDrawableTransition((AnimationDrawable) d, state.isTransitionReversed(fromId, toId));
        } else if (d instanceof AnimatedVectorDrawable) {
            transition = new AnimatedVectorDrawableTransition((AnimatedVectorDrawable) d, state.isTransitionReversed(fromId, toId));
        } else if (!(d instanceof Animatable)) {
            return false;
        } else {
            transition = new AnimatableTransition((Animatable) d);
        }
        transition.start();
        this.mTransition = transition;
        this.mTransitionFromIndex = fromIndex;
        this.mTransitionToIndex = toIndex;
        return true;
    }

    /* access modifiers changed from: private */
    public static abstract class Transition {
        public abstract void start();

        public abstract void stop();

        private Transition() {
        }

        public void reverse() {
        }

        public boolean canReverse() {
            return false;
        }
    }

    /* access modifiers changed from: private */
    public static class AnimatableTransition extends Transition {
        private final Animatable mA;

        public AnimatableTransition(Animatable a) {
            super();
            this.mA = a;
        }

        @Override // android.graphics.drawable.AnimatedStateListDrawable.Transition
        public void start() {
            this.mA.start();
        }

        @Override // android.graphics.drawable.AnimatedStateListDrawable.Transition
        public void stop() {
            this.mA.stop();
        }
    }

    /* access modifiers changed from: private */
    public static class AnimationDrawableTransition extends Transition {
        private final ObjectAnimator mAnim;

        public AnimationDrawableTransition(AnimationDrawable ad, boolean reversed) {
            super();
            int fromFrame;
            int frameCount = ad.getNumberOfFrames();
            if (reversed) {
                fromFrame = frameCount - 1;
            } else {
                fromFrame = 0;
            }
            int toFrame = reversed ? 0 : frameCount - 1;
            FrameInterpolator interp = new FrameInterpolator(ad, reversed);
            ObjectAnimator anim = ObjectAnimator.ofInt(ad, "currentIndex", fromFrame, toFrame);
            anim.setAutoCancel(true);
            anim.setDuration((long) interp.getTotalDuration());
            anim.setInterpolator(interp);
            this.mAnim = anim;
        }

        @Override // android.graphics.drawable.AnimatedStateListDrawable.Transition
        public boolean canReverse() {
            return true;
        }

        @Override // android.graphics.drawable.AnimatedStateListDrawable.Transition
        public void start() {
            this.mAnim.start();
        }

        @Override // android.graphics.drawable.AnimatedStateListDrawable.Transition
        public void reverse() {
            this.mAnim.reverse();
        }

        @Override // android.graphics.drawable.AnimatedStateListDrawable.Transition
        public void stop() {
            this.mAnim.cancel();
        }
    }

    /* access modifiers changed from: private */
    public static class AnimatedVectorDrawableTransition extends Transition {
        private final AnimatedVectorDrawable mAvd;
        private final boolean mReversed;

        public AnimatedVectorDrawableTransition(AnimatedVectorDrawable avd, boolean reversed) {
            super();
            this.mAvd = avd;
            this.mReversed = reversed;
        }

        @Override // android.graphics.drawable.AnimatedStateListDrawable.Transition
        public boolean canReverse() {
            return this.mAvd.canReverse();
        }

        @Override // android.graphics.drawable.AnimatedStateListDrawable.Transition
        public void start() {
            if (this.mReversed) {
                reverse();
            } else {
                this.mAvd.start();
            }
        }

        @Override // android.graphics.drawable.AnimatedStateListDrawable.Transition
        public void reverse() {
            if (canReverse()) {
                this.mAvd.reverse();
            } else {
                Log.w(AnimatedStateListDrawable.LOGTAG, "Reverse() is called on a drawable can't reverse");
            }
        }

        @Override // android.graphics.drawable.AnimatedStateListDrawable.Transition
        public void stop() {
            this.mAvd.stop();
        }
    }

    @Override // android.graphics.drawable.DrawableContainer, android.graphics.drawable.Drawable
    public void jumpToCurrentState() {
        super.jumpToCurrentState();
        if (this.mTransition != null) {
            this.mTransition.stop();
            this.mTransition = null;
            selectDrawable(this.mTransitionToIndex);
            this.mTransitionToIndex = -1;
            this.mTransitionFromIndex = -1;
        }
    }

    @Override // android.graphics.drawable.StateListDrawable, android.graphics.drawable.Drawable
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs, Resources.Theme theme) throws XmlPullParserException, IOException {
        int depth;
        TypedArray a = obtainAttributes(r, theme, attrs, R.styleable.AnimatedStateListDrawable);
        super.inflateWithAttributes(r, parser, a, 1);
        StateListDrawable.StateListState stateListState = getStateListState();
        stateListState.setVariablePadding(a.getBoolean(2, false));
        stateListState.setConstantSize(a.getBoolean(3, false));
        stateListState.setEnterFadeDuration(a.getInt(4, 0));
        stateListState.setExitFadeDuration(a.getInt(5, 0));
        setDither(a.getBoolean(0, true));
        setAutoMirrored(a.getBoolean(6, false));
        a.recycle();
        int innerDepth = parser.getDepth() + 1;
        while (true) {
            int type = parser.next();
            if (type == 1 || ((depth = parser.getDepth()) < innerDepth && type == 3)) {
                onStateChange(getState());
            } else if (type == 2 && depth <= innerDepth) {
                if (parser.getName().equals(ELEMENT_ITEM)) {
                    parseItem(r, parser, attrs, theme);
                } else if (parser.getName().equals(ELEMENT_TRANSITION)) {
                    parseTransition(r, parser, attrs, theme);
                }
            }
        }
        onStateChange(getState());
    }

    private int parseTransition(Resources r, XmlPullParser parser, AttributeSet attrs, Resources.Theme theme) throws XmlPullParserException, IOException {
        int type;
        Drawable dr;
        int drawableRes = 0;
        int fromId = 0;
        int toId = 0;
        boolean reversible = false;
        int numAttrs = attrs.getAttributeCount();
        for (int i = 0; i < numAttrs; i++) {
            switch (attrs.getAttributeNameResource(i)) {
                case 16843161:
                    drawableRes = attrs.getAttributeResourceValue(i, 0);
                    break;
                case 16843849:
                    toId = attrs.getAttributeResourceValue(i, 0);
                    break;
                case 16843850:
                    fromId = attrs.getAttributeResourceValue(i, 0);
                    break;
                case 16843851:
                    reversible = attrs.getAttributeBooleanValue(i, false);
                    break;
            }
        }
        if (drawableRes != 0) {
            dr = r.getDrawable(drawableRes, theme);
        } else {
            do {
                type = parser.next();
            } while (type == 4);
            if (type != 2) {
                throw new XmlPullParserException(parser.getPositionDescription() + ": <item> tag requires a 'drawable' attribute or " + "child tag defining a drawable");
            }
            dr = Drawable.createFromXmlInner(r, parser, attrs, theme);
        }
        return this.mState.addTransition(fromId, toId, dr, reversible);
    }

    private int parseItem(Resources r, XmlPullParser parser, AttributeSet attrs, Resources.Theme theme) throws XmlPullParserException, IOException {
        int type;
        Drawable dr;
        int j;
        int drawableRes = 0;
        int keyframeId = 0;
        int numAttrs = attrs.getAttributeCount();
        int[] states = new int[numAttrs];
        int i = 0;
        int j2 = 0;
        while (i < numAttrs) {
            int stateResId = attrs.getAttributeNameResource(i);
            switch (stateResId) {
                case 0:
                    j = j2;
                    break;
                case 16842960:
                    keyframeId = attrs.getAttributeResourceValue(i, 0);
                    j = j2;
                    break;
                case 16843161:
                    drawableRes = attrs.getAttributeResourceValue(i, 0);
                    j = j2;
                    break;
                default:
                    j = j2 + 1;
                    if (!attrs.getAttributeBooleanValue(i, false)) {
                        stateResId = -stateResId;
                    }
                    states[j2] = stateResId;
                    break;
            }
            i++;
            j2 = j;
        }
        int[] states2 = StateSet.trimStateSet(states, j2);
        if (drawableRes != 0) {
            dr = r.getDrawable(drawableRes, theme);
        } else {
            do {
                type = parser.next();
            } while (type == 4);
            if (type != 2) {
                throw new XmlPullParserException(parser.getPositionDescription() + ": <item> tag requires a 'drawable' attribute or " + "child tag defining a drawable");
            }
            dr = Drawable.createFromXmlInner(r, parser, attrs, theme);
        }
        return this.mState.addStateSet(states2, dr, keyframeId);
    }

    @Override // android.graphics.drawable.DrawableContainer, android.graphics.drawable.StateListDrawable, android.graphics.drawable.Drawable
    public Drawable mutate() {
        if (!this.mMutated && super.mutate() == this) {
            setConstantState(new AnimatedStateListState(this.mState, this, null));
            this.mMutated = true;
        }
        return this;
    }

    /* access modifiers changed from: package-private */
    public static class AnimatedStateListState extends StateListDrawable.StateListState {
        private static final int REVERSE_MASK = 1;
        private static final int REVERSE_SHIFT = 32;
        final SparseIntArray mStateIds;
        final LongSparseLongArray mTransitions;

        AnimatedStateListState(AnimatedStateListState orig, AnimatedStateListDrawable owner, Resources res) {
            super(orig, owner, res);
            if (orig != null) {
                this.mTransitions = orig.mTransitions.clone();
                this.mStateIds = orig.mStateIds.clone();
                return;
            }
            this.mTransitions = new LongSparseLongArray();
            this.mStateIds = new SparseIntArray();
        }

        /* access modifiers changed from: package-private */
        public int addTransition(int fromId, int toId, Drawable anim, boolean reversible) {
            int pos = super.addChild(anim);
            this.mTransitions.append(generateTransitionKey(fromId, toId), (long) pos);
            if (reversible) {
                this.mTransitions.append(generateTransitionKey(toId, fromId), ((long) pos) | 4294967296L);
            }
            return addChild(anim);
        }

        /* access modifiers changed from: package-private */
        public int addStateSet(int[] stateSet, Drawable drawable, int id) {
            int index = super.addStateSet(stateSet, drawable);
            this.mStateIds.put(index, id);
            return index;
        }

        /* access modifiers changed from: package-private */
        public int indexOfKeyframe(int[] stateSet) {
            int index = super.indexOfStateSet(stateSet);
            return index >= 0 ? index : super.indexOfStateSet(StateSet.WILD_CARD);
        }

        /* access modifiers changed from: package-private */
        public int getKeyframeIdAt(int index) {
            if (index < 0) {
                return 0;
            }
            return this.mStateIds.get(index, 0);
        }

        /* access modifiers changed from: package-private */
        public int indexOfTransition(int fromId, int toId) {
            return (int) this.mTransitions.get(generateTransitionKey(fromId, toId), -1);
        }

        /* access modifiers changed from: package-private */
        public boolean isTransitionReversed(int fromId, int toId) {
            return ((this.mTransitions.get(generateTransitionKey(fromId, toId), -1) >> 32) & 1) == 1;
        }

        @Override // android.graphics.drawable.Drawable.ConstantState, android.graphics.drawable.StateListDrawable.StateListState
        public Drawable newDrawable() {
            return new AnimatedStateListDrawable(this, null);
        }

        @Override // android.graphics.drawable.Drawable.ConstantState, android.graphics.drawable.StateListDrawable.StateListState
        public Drawable newDrawable(Resources res) {
            return new AnimatedStateListDrawable(this, res);
        }

        private static long generateTransitionKey(int fromId, int toId) {
            return (((long) fromId) << 32) | ((long) toId);
        }
    }

    /* access modifiers changed from: package-private */
    public void setConstantState(AnimatedStateListState state) {
        super.setConstantState((StateListDrawable.StateListState) state);
        this.mState = state;
    }

    private AnimatedStateListDrawable(AnimatedStateListState state, Resources res) {
        super(null);
        this.mTransitionToIndex = -1;
        this.mTransitionFromIndex = -1;
        setConstantState(new AnimatedStateListState(state, this, res));
        onStateChange(getState());
        jumpToCurrentState();
    }

    private static class FrameInterpolator implements TimeInterpolator {
        private int[] mFrameTimes;
        private int mFrames;
        private int mTotalDuration;

        public FrameInterpolator(AnimationDrawable d, boolean reversed) {
            updateFrames(d, reversed);
        }

        public int updateFrames(AnimationDrawable d, boolean reversed) {
            int N = d.getNumberOfFrames();
            this.mFrames = N;
            if (this.mFrameTimes == null || this.mFrameTimes.length < N) {
                this.mFrameTimes = new int[N];
            }
            int[] frameTimes = this.mFrameTimes;
            int totalDuration = 0;
            for (int i = 0; i < N; i++) {
                int duration = d.getDuration(reversed ? (N - i) - 1 : i);
                frameTimes[i] = duration;
                totalDuration += duration;
            }
            this.mTotalDuration = totalDuration;
            return totalDuration;
        }

        public int getTotalDuration() {
            return this.mTotalDuration;
        }

        public float getInterpolation(float input) {
            float frameElapsed;
            int N = this.mFrames;
            int[] frameTimes = this.mFrameTimes;
            int remaining = (int) ((((float) this.mTotalDuration) * input) + 0.5f);
            int i = 0;
            while (i < N && remaining >= frameTimes[i]) {
                remaining -= frameTimes[i];
                i++;
            }
            if (i < N) {
                frameElapsed = ((float) remaining) / ((float) this.mTotalDuration);
            } else {
                frameElapsed = 0.0f;
            }
            return (((float) i) / ((float) N)) + frameElapsed;
        }
    }
}

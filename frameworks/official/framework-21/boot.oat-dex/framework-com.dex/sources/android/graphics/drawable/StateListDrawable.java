package android.graphics.drawable;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.DrawableContainer;
import android.util.AttributeSet;
import android.util.StateSet;
import com.android.internal.R;
import java.io.IOException;
import java.util.Arrays;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class StateListDrawable extends DrawableContainer {
    private static final boolean DEBUG = false;
    private static final boolean DEFAULT_DITHER = true;
    private static final String TAG = StateListDrawable.class.getSimpleName();
    private boolean mMutated;
    private StateListState mStateListState;

    public StateListDrawable() {
        this(null, null);
    }

    public void addState(int[] stateSet, Drawable drawable) {
        if (drawable != null) {
            this.mStateListState.addStateSet(stateSet, drawable);
            onStateChange(getState());
        }
    }

    @Override // android.graphics.drawable.DrawableContainer, android.graphics.drawable.Drawable
    public boolean isStateful() {
        return true;
    }

    /* access modifiers changed from: protected */
    @Override // android.graphics.drawable.DrawableContainer, android.graphics.drawable.Drawable
    public boolean onStateChange(int[] stateSet) {
        int idx = this.mStateListState.indexOfStateSet(stateSet);
        if (idx < 0) {
            idx = this.mStateListState.indexOfStateSet(StateSet.WILD_CARD);
        }
        if (selectDrawable(idx)) {
            return true;
        }
        return super.onStateChange(stateSet);
    }

    @Override // android.graphics.drawable.Drawable
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs, Resources.Theme theme) throws XmlPullParserException, IOException {
        int depth;
        int type;
        Drawable dr;
        int j;
        TypedArray a = obtainAttributes(r, theme, attrs, R.styleable.StateListDrawable);
        super.inflateWithAttributes(r, parser, a, 1);
        this.mStateListState.setVariablePadding(a.getBoolean(2, false));
        this.mStateListState.setConstantSize(a.getBoolean(3, false));
        this.mStateListState.setEnterFadeDuration(a.getInt(4, 0));
        this.mStateListState.setExitFadeDuration(a.getInt(5, 0));
        setDither(a.getBoolean(0, true));
        setAutoMirrored(a.getBoolean(6, false));
        a.recycle();
        int innerDepth = parser.getDepth() + 1;
        while (true) {
            int type2 = parser.next();
            if (type2 == 1 || ((depth = parser.getDepth()) < innerDepth && type2 == 3)) {
                onStateChange(getState());
            } else if (type2 == 2 && depth <= innerDepth && parser.getName().equals("item")) {
                int drawableRes = 0;
                int numAttrs = attrs.getAttributeCount();
                int[] states = new int[numAttrs];
                int i = 0;
                int j2 = 0;
                while (i < numAttrs) {
                    int stateResId = attrs.getAttributeNameResource(i);
                    if (stateResId == 0) {
                        break;
                    }
                    if (stateResId == 16843161) {
                        drawableRes = attrs.getAttributeResourceValue(i, 0);
                        j = j2;
                    } else {
                        j = j2 + 1;
                        if (!attrs.getAttributeBooleanValue(i, false)) {
                            stateResId = -stateResId;
                        }
                        states[j2] = stateResId;
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
                this.mStateListState.addStateSet(states2, dr);
            }
        }
        onStateChange(getState());
    }

    /* access modifiers changed from: package-private */
    public StateListState getStateListState() {
        return this.mStateListState;
    }

    public int getStateCount() {
        return this.mStateListState.getChildCount();
    }

    public int[] getStateSet(int index) {
        return this.mStateListState.mStateSets[index];
    }

    public Drawable getStateDrawable(int index) {
        return this.mStateListState.getChild(index);
    }

    public int getStateDrawableIndex(int[] stateSet) {
        return this.mStateListState.indexOfStateSet(stateSet);
    }

    @Override // android.graphics.drawable.DrawableContainer, android.graphics.drawable.Drawable
    public Drawable mutate() {
        if (!this.mMutated && super.mutate() == this) {
            int[][] sets = this.mStateListState.mStateSets;
            int count = sets.length;
            this.mStateListState.mStateSets = new int[count][];
            for (int i = 0; i < count; i++) {
                int[] set = sets[i];
                if (set != null) {
                    this.mStateListState.mStateSets[i] = (int[]) set.clone();
                }
            }
            this.mMutated = true;
        }
        return this;
    }

    @Override // android.graphics.drawable.Drawable
    public void setLayoutDirection(int layoutDirection) {
        super.setLayoutDirection(layoutDirection);
        this.mStateListState.setLayoutDirection(layoutDirection);
    }

    /* access modifiers changed from: package-private */
    public static class StateListState extends DrawableContainer.DrawableContainerState {
        int[][] mStateSets;

        StateListState(StateListState orig, StateListDrawable owner, Resources res) {
            super(orig, owner, res);
            if (orig != null) {
                this.mStateSets = (int[][]) Arrays.copyOf(orig.mStateSets, orig.mStateSets.length);
            } else {
                this.mStateSets = new int[getCapacity()][];
            }
        }

        /* access modifiers changed from: package-private */
        public int addStateSet(int[] stateSet, Drawable drawable) {
            int pos = addChild(drawable);
            this.mStateSets[pos] = stateSet;
            return pos;
        }

        /* access modifiers changed from: package-private */
        public int indexOfStateSet(int[] stateSet) {
            int[][] stateSets = this.mStateSets;
            int N = getChildCount();
            for (int i = 0; i < N; i++) {
                if (StateSet.stateSetMatches(stateSets[i], stateSet)) {
                    return i;
                }
            }
            return -1;
        }

        @Override // android.graphics.drawable.Drawable.ConstantState
        public Drawable newDrawable() {
            return new StateListDrawable(this, null);
        }

        @Override // android.graphics.drawable.Drawable.ConstantState
        public Drawable newDrawable(Resources res) {
            return new StateListDrawable(this, res);
        }

        @Override // android.graphics.drawable.DrawableContainer.DrawableContainerState
        public void growArray(int oldSize, int newSize) {
            super.growArray(oldSize, newSize);
            int[][] newStateSets = new int[newSize][];
            System.arraycopy(this.mStateSets, 0, newStateSets, 0, oldSize);
            this.mStateSets = newStateSets;
        }
    }

    /* access modifiers changed from: package-private */
    public void setConstantState(StateListState state) {
        super.setConstantState((DrawableContainer.DrawableContainerState) state);
        this.mStateListState = state;
    }

    private StateListDrawable(StateListState state, Resources res) {
        setConstantState(new StateListState(state, this, res));
        onStateChange(getState());
    }

    StateListDrawable(StateListState state) {
        if (state != null) {
            setConstantState(state);
        }
    }
}

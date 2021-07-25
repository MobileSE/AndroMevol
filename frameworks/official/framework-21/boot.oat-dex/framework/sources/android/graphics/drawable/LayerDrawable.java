package android.graphics.drawable;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import com.android.internal.R;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class LayerDrawable extends Drawable implements Drawable.Callback {
    public static final int PADDING_MODE_NEST = 0;
    public static final int PADDING_MODE_STACK = 1;
    private Rect mHotspotBounds;
    LayerState mLayerState;
    private boolean mMutated;
    private int mOpacityOverride;
    private int[] mPaddingB;
    private int[] mPaddingL;
    private int[] mPaddingR;
    private int[] mPaddingT;
    private final Rect mTmpRect;

    public LayerDrawable(Drawable[] layers) {
        this(layers, null);
    }

    LayerDrawable(Drawable[] layers, LayerState state) {
        this(state, null, null);
        int length = layers.length;
        ChildDrawable[] r = new ChildDrawable[length];
        for (int i = 0; i < length; i++) {
            r[i] = new ChildDrawable();
            r[i].mDrawable = layers[i];
            layers[i].setCallback(this);
            this.mLayerState.mChildrenChangingConfigurations |= layers[i].getChangingConfigurations();
        }
        this.mLayerState.mNum = length;
        this.mLayerState.mChildren = r;
        ensurePadding();
    }

    LayerDrawable() {
        this(null, null, null);
    }

    LayerDrawable(LayerState state, Resources res, Resources.Theme theme) {
        this.mOpacityOverride = 0;
        this.mTmpRect = new Rect();
        LayerState as = createConstantState(state, res);
        this.mLayerState = as;
        if (as.mNum > 0) {
            ensurePadding();
        }
        if (theme != null && canApplyTheme()) {
            applyTheme(theme);
        }
    }

    /* access modifiers changed from: package-private */
    public LayerState createConstantState(LayerState state, Resources res) {
        return new LayerState(state, this, res);
    }

    @Override // android.graphics.drawable.Drawable
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs, Resources.Theme theme) throws XmlPullParserException, IOException {
        super.inflate(r, parser, attrs, theme);
        TypedArray a = obtainAttributes(r, theme, attrs, R.styleable.LayerDrawable);
        updateStateFromTypedArray(a);
        a.recycle();
        inflateLayers(r, parser, attrs, theme);
        ensurePadding();
        onStateChange(getState());
    }

    private void updateStateFromTypedArray(TypedArray a) {
        LayerState state = this.mLayerState;
        state.mChangingConfigurations |= a.getChangingConfigurations();
        state.mThemeAttrs = a.extractThemeAttrs();
        this.mOpacityOverride = a.getInt(0, this.mOpacityOverride);
        state.mAutoMirrored = a.getBoolean(1, state.mAutoMirrored);
        state.mPaddingMode = a.getInteger(2, state.mPaddingMode);
    }

    private void inflateLayers(Resources r, XmlPullParser parser, AttributeSet attrs, Resources.Theme theme) throws XmlPullParserException, IOException {
        int type;
        LayerState state = this.mLayerState;
        int innerDepth = parser.getDepth() + 1;
        while (true) {
            int type2 = parser.next();
            if (type2 != 1) {
                int depth = parser.getDepth();
                if (depth < innerDepth && type2 == 3) {
                    return;
                }
                if (type2 == 2 && depth <= innerDepth && parser.getName().equals("item")) {
                    ChildDrawable layer = new ChildDrawable();
                    TypedArray a = obtainAttributes(r, theme, attrs, R.styleable.LayerDrawableItem);
                    updateLayerFromTypedArray(layer, a);
                    a.recycle();
                    if (layer.mDrawable == null) {
                        do {
                            type = parser.next();
                        } while (type == 4);
                        if (type != 2) {
                            throw new XmlPullParserException(parser.getPositionDescription() + ": <item> tag requires a 'drawable' attribute or " + "child tag defining a drawable");
                        }
                        layer.mDrawable = Drawable.createFromXmlInner(r, parser, attrs, theme);
                    }
                    if (layer.mDrawable != null) {
                        state.mChildrenChangingConfigurations |= layer.mDrawable.getChangingConfigurations();
                        layer.mDrawable.setCallback(this);
                    }
                    addLayer(layer);
                }
            } else {
                return;
            }
        }
    }

    private void updateLayerFromTypedArray(ChildDrawable layer, TypedArray a) {
        this.mLayerState.mChildrenChangingConfigurations |= a.getChangingConfigurations();
        layer.mThemeAttrs = a.extractThemeAttrs();
        layer.mInsetL = a.getDimensionPixelOffset(2, layer.mInsetL);
        layer.mInsetT = a.getDimensionPixelOffset(3, layer.mInsetT);
        layer.mInsetR = a.getDimensionPixelOffset(4, layer.mInsetR);
        layer.mInsetB = a.getDimensionPixelOffset(5, layer.mInsetB);
        layer.mId = a.getResourceId(0, layer.mId);
        Drawable dr = a.getDrawable(1);
        if (dr != null) {
            layer.mDrawable = dr;
        }
    }

    @Override // android.graphics.drawable.Drawable
    public void applyTheme(Resources.Theme t) {
        super.applyTheme(t);
        LayerState state = this.mLayerState;
        if (state != null) {
            if (state.mThemeAttrs != null) {
                TypedArray a = t.resolveAttributes(state.mThemeAttrs, R.styleable.LayerDrawable);
                updateStateFromTypedArray(a);
                a.recycle();
            }
            ChildDrawable[] array = this.mLayerState.mChildren;
            int N = this.mLayerState.mNum;
            for (int i = 0; i < N; i++) {
                ChildDrawable layer = array[i];
                if (layer.mThemeAttrs != null) {
                    TypedArray a2 = t.resolveAttributes(layer.mThemeAttrs, R.styleable.LayerDrawableItem);
                    updateLayerFromTypedArray(layer, a2);
                    a2.recycle();
                }
                Drawable d = layer.mDrawable;
                if (d.canApplyTheme()) {
                    d.applyTheme(t);
                }
            }
            ensurePadding();
            onStateChange(getState());
        }
    }

    @Override // android.graphics.drawable.Drawable
    public boolean canApplyTheme() {
        LayerState state = this.mLayerState;
        if (state == null) {
            return false;
        }
        if (state.mThemeAttrs != null) {
            return true;
        }
        ChildDrawable[] array = state.mChildren;
        int N = state.mNum;
        for (int i = 0; i < N; i++) {
            ChildDrawable layer = array[i];
            if (layer.mThemeAttrs != null || layer.mDrawable.canApplyTheme()) {
                return true;
            }
        }
        return false;
    }

    @Override // android.graphics.drawable.Drawable
    public boolean isProjected() {
        if (super.isProjected()) {
            return true;
        }
        ChildDrawable[] layers = this.mLayerState.mChildren;
        int N = this.mLayerState.mNum;
        for (int i = 0; i < N; i++) {
            if (layers[i].mDrawable.isProjected()) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void addLayer(ChildDrawable layer) {
        int N;
        LayerState st = this.mLayerState;
        if (st.mChildren != null) {
            N = st.mChildren.length;
        } else {
            N = 0;
        }
        int i = st.mNum;
        if (i >= N) {
            ChildDrawable[] nu = new ChildDrawable[(N + 10)];
            if (i > 0) {
                System.arraycopy(st.mChildren, 0, nu, 0, i);
            }
            st.mChildren = nu;
        }
        st.mChildren[i] = layer;
        st.mNum++;
        st.invalidateCache();
    }

    /* access modifiers changed from: package-private */
    public ChildDrawable addLayer(Drawable layer, int[] themeAttrs, int id, int left, int top, int right, int bottom) {
        ChildDrawable childDrawable = new ChildDrawable();
        childDrawable.mId = id;
        childDrawable.mThemeAttrs = themeAttrs;
        childDrawable.mDrawable = layer;
        childDrawable.mDrawable.setAutoMirrored(isAutoMirrored());
        childDrawable.mInsetL = left;
        childDrawable.mInsetT = top;
        childDrawable.mInsetR = right;
        childDrawable.mInsetB = bottom;
        addLayer(childDrawable);
        this.mLayerState.mChildrenChangingConfigurations |= layer.getChangingConfigurations();
        layer.setCallback(this);
        return childDrawable;
    }

    public Drawable findDrawableByLayerId(int id) {
        ChildDrawable[] layers = this.mLayerState.mChildren;
        for (int i = this.mLayerState.mNum - 1; i >= 0; i--) {
            if (layers[i].mId == id) {
                return layers[i].mDrawable;
            }
        }
        return null;
    }

    public void setId(int index, int id) {
        this.mLayerState.mChildren[index].mId = id;
    }

    public int getNumberOfLayers() {
        return this.mLayerState.mNum;
    }

    public Drawable getDrawable(int index) {
        return this.mLayerState.mChildren[index].mDrawable;
    }

    public int getId(int index) {
        return this.mLayerState.mChildren[index].mId;
    }

    public boolean setDrawableByLayerId(int id, Drawable drawable) {
        ChildDrawable[] layers = this.mLayerState.mChildren;
        int N = this.mLayerState.mNum;
        for (int i = 0; i < N; i++) {
            ChildDrawable childDrawable = layers[i];
            if (childDrawable.mId == id) {
                if (childDrawable.mDrawable != null) {
                    if (drawable != null) {
                        drawable.setBounds(childDrawable.mDrawable.getBounds());
                    }
                    childDrawable.mDrawable.setCallback(null);
                }
                if (drawable != null) {
                    drawable.setCallback(this);
                }
                childDrawable.mDrawable = drawable;
                this.mLayerState.invalidateCache();
                return true;
            }
        }
        return false;
    }

    public void setLayerInset(int index, int l, int t, int r, int b) {
        ChildDrawable childDrawable = this.mLayerState.mChildren[index];
        childDrawable.mInsetL = l;
        childDrawable.mInsetT = t;
        childDrawable.mInsetR = r;
        childDrawable.mInsetB = b;
    }

    public void setPaddingMode(int mode) {
        if (this.mLayerState.mPaddingMode != mode) {
            this.mLayerState.mPaddingMode = mode;
        }
    }

    public int getPaddingMode() {
        return this.mLayerState.mPaddingMode;
    }

    @Override // android.graphics.drawable.Drawable.Callback
    public void invalidateDrawable(Drawable who) {
        invalidateSelf();
    }

    @Override // android.graphics.drawable.Drawable.Callback
    public void scheduleDrawable(Drawable who, Runnable what, long when) {
        scheduleSelf(what, when);
    }

    @Override // android.graphics.drawable.Drawable.Callback
    public void unscheduleDrawable(Drawable who, Runnable what) {
        unscheduleSelf(what);
    }

    @Override // android.graphics.drawable.Drawable
    public void draw(Canvas canvas) {
        ChildDrawable[] array = this.mLayerState.mChildren;
        int N = this.mLayerState.mNum;
        for (int i = 0; i < N; i++) {
            array[i].mDrawable.draw(canvas);
        }
    }

    @Override // android.graphics.drawable.Drawable
    public int getChangingConfigurations() {
        return super.getChangingConfigurations() | this.mLayerState.mChangingConfigurations | this.mLayerState.mChildrenChangingConfigurations;
    }

    @Override // android.graphics.drawable.Drawable
    public boolean getPadding(Rect padding) {
        if (this.mLayerState.mPaddingMode == 0) {
            computeNestedPadding(padding);
        } else {
            computeStackedPadding(padding);
        }
        return (padding.left == 0 && padding.top == 0 && padding.right == 0 && padding.bottom == 0) ? false : true;
    }

    private void computeNestedPadding(Rect padding) {
        padding.left = 0;
        padding.top = 0;
        padding.right = 0;
        padding.bottom = 0;
        ChildDrawable[] array = this.mLayerState.mChildren;
        int N = this.mLayerState.mNum;
        for (int i = 0; i < N; i++) {
            refreshChildPadding(i, array[i]);
            padding.left += this.mPaddingL[i];
            padding.top += this.mPaddingT[i];
            padding.right += this.mPaddingR[i];
            padding.bottom += this.mPaddingB[i];
        }
    }

    private void computeStackedPadding(Rect padding) {
        padding.left = 0;
        padding.top = 0;
        padding.right = 0;
        padding.bottom = 0;
        ChildDrawable[] array = this.mLayerState.mChildren;
        int N = this.mLayerState.mNum;
        for (int i = 0; i < N; i++) {
            refreshChildPadding(i, array[i]);
            padding.left = Math.max(padding.left, this.mPaddingL[i]);
            padding.top = Math.max(padding.top, this.mPaddingT[i]);
            padding.right = Math.max(padding.right, this.mPaddingR[i]);
            padding.bottom = Math.max(padding.bottom, this.mPaddingB[i]);
        }
    }

    @Override // android.graphics.drawable.Drawable
    public void getOutline(Outline outline) {
        LayerState state = this.mLayerState;
        ChildDrawable[] children = state.mChildren;
        int N = state.mNum;
        for (int i = 0; i < N; i++) {
            children[i].mDrawable.getOutline(outline);
            if (!outline.isEmpty()) {
                return;
            }
        }
    }

    @Override // android.graphics.drawable.Drawable
    public void setHotspot(float x, float y) {
        ChildDrawable[] array = this.mLayerState.mChildren;
        int N = this.mLayerState.mNum;
        for (int i = 0; i < N; i++) {
            array[i].mDrawable.setHotspot(x, y);
        }
    }

    @Override // android.graphics.drawable.Drawable
    public void setHotspotBounds(int left, int top, int right, int bottom) {
        ChildDrawable[] array = this.mLayerState.mChildren;
        int N = this.mLayerState.mNum;
        for (int i = 0; i < N; i++) {
            array[i].mDrawable.setHotspotBounds(left, top, right, bottom);
        }
        if (this.mHotspotBounds == null) {
            this.mHotspotBounds = new Rect(left, top, right, bottom);
        } else {
            this.mHotspotBounds.set(left, top, right, bottom);
        }
    }

    @Override // android.graphics.drawable.Drawable
    public void getHotspotBounds(Rect outRect) {
        if (this.mHotspotBounds != null) {
            outRect.set(this.mHotspotBounds);
        } else {
            super.getHotspotBounds(outRect);
        }
    }

    @Override // android.graphics.drawable.Drawable
    public boolean setVisible(boolean visible, boolean restart) {
        boolean changed = super.setVisible(visible, restart);
        ChildDrawable[] array = this.mLayerState.mChildren;
        int N = this.mLayerState.mNum;
        for (int i = 0; i < N; i++) {
            array[i].mDrawable.setVisible(visible, restart);
        }
        return changed;
    }

    @Override // android.graphics.drawable.Drawable
    public void setDither(boolean dither) {
        ChildDrawable[] array = this.mLayerState.mChildren;
        int N = this.mLayerState.mNum;
        for (int i = 0; i < N; i++) {
            array[i].mDrawable.setDither(dither);
        }
    }

    @Override // android.graphics.drawable.Drawable
    public void setAlpha(int alpha) {
        ChildDrawable[] array = this.mLayerState.mChildren;
        int N = this.mLayerState.mNum;
        for (int i = 0; i < N; i++) {
            array[i].mDrawable.setAlpha(alpha);
        }
    }

    @Override // android.graphics.drawable.Drawable
    public int getAlpha() {
        ChildDrawable[] array = this.mLayerState.mChildren;
        if (this.mLayerState.mNum > 0) {
            return array[0].mDrawable.getAlpha();
        }
        return super.getAlpha();
    }

    @Override // android.graphics.drawable.Drawable
    public void setColorFilter(ColorFilter cf) {
        ChildDrawable[] array = this.mLayerState.mChildren;
        int N = this.mLayerState.mNum;
        for (int i = 0; i < N; i++) {
            array[i].mDrawable.setColorFilter(cf);
        }
    }

    @Override // android.graphics.drawable.Drawable
    public void setTintList(ColorStateList tint) {
        ChildDrawable[] array = this.mLayerState.mChildren;
        int N = this.mLayerState.mNum;
        for (int i = 0; i < N; i++) {
            array[i].mDrawable.setTintList(tint);
        }
    }

    @Override // android.graphics.drawable.Drawable
    public void setTintMode(PorterDuff.Mode tintMode) {
        ChildDrawable[] array = this.mLayerState.mChildren;
        int N = this.mLayerState.mNum;
        for (int i = 0; i < N; i++) {
            array[i].mDrawable.setTintMode(tintMode);
        }
    }

    public void setOpacity(int opacity) {
        this.mOpacityOverride = opacity;
    }

    @Override // android.graphics.drawable.Drawable
    public int getOpacity() {
        if (this.mOpacityOverride != 0) {
            return this.mOpacityOverride;
        }
        return this.mLayerState.getOpacity();
    }

    @Override // android.graphics.drawable.Drawable
    public void setAutoMirrored(boolean mirrored) {
        this.mLayerState.mAutoMirrored = mirrored;
        ChildDrawable[] array = this.mLayerState.mChildren;
        int N = this.mLayerState.mNum;
        for (int i = 0; i < N; i++) {
            array[i].mDrawable.setAutoMirrored(mirrored);
        }
    }

    @Override // android.graphics.drawable.Drawable
    public boolean isAutoMirrored() {
        return this.mLayerState.mAutoMirrored;
    }

    @Override // android.graphics.drawable.Drawable
    public boolean isStateful() {
        return this.mLayerState.isStateful();
    }

    /* access modifiers changed from: protected */
    @Override // android.graphics.drawable.Drawable
    public boolean onStateChange(int[] state) {
        boolean paddingChanged = false;
        boolean changed = false;
        ChildDrawable[] array = this.mLayerState.mChildren;
        int N = this.mLayerState.mNum;
        for (int i = 0; i < N; i++) {
            ChildDrawable r = array[i];
            if (r.mDrawable.isStateful() && r.mDrawable.setState(state)) {
                changed = true;
            }
            if (refreshChildPadding(i, r)) {
                paddingChanged = true;
            }
        }
        if (paddingChanged) {
            onBoundsChange(getBounds());
        }
        return changed;
    }

    /* access modifiers changed from: protected */
    @Override // android.graphics.drawable.Drawable
    public boolean onLevelChange(int level) {
        boolean paddingChanged = false;
        boolean changed = false;
        ChildDrawable[] array = this.mLayerState.mChildren;
        int N = this.mLayerState.mNum;
        for (int i = 0; i < N; i++) {
            ChildDrawable r = array[i];
            if (r.mDrawable.setLevel(level)) {
                changed = true;
            }
            if (refreshChildPadding(i, r)) {
                paddingChanged = true;
            }
        }
        if (paddingChanged) {
            onBoundsChange(getBounds());
        }
        return changed;
    }

    /* access modifiers changed from: protected */
    @Override // android.graphics.drawable.Drawable
    public void onBoundsChange(Rect bounds) {
        int padL = 0;
        int padT = 0;
        int padR = 0;
        int padB = 0;
        boolean nest = this.mLayerState.mPaddingMode == 0;
        ChildDrawable[] array = this.mLayerState.mChildren;
        int N = this.mLayerState.mNum;
        for (int i = 0; i < N; i++) {
            ChildDrawable r = array[i];
            r.mDrawable.setBounds(bounds.left + r.mInsetL + padL, bounds.top + r.mInsetT + padT, (bounds.right - r.mInsetR) - padR, (bounds.bottom - r.mInsetB) - padB);
            if (nest) {
                padL += this.mPaddingL[i];
                padR += this.mPaddingR[i];
                padT += this.mPaddingT[i];
                padB += this.mPaddingB[i];
            }
        }
    }

    @Override // android.graphics.drawable.Drawable
    public int getIntrinsicWidth() {
        int width = -1;
        int padL = 0;
        int padR = 0;
        boolean nest = this.mLayerState.mPaddingMode == 0;
        ChildDrawable[] array = this.mLayerState.mChildren;
        int N = this.mLayerState.mNum;
        for (int i = 0; i < N; i++) {
            ChildDrawable r = array[i];
            int w = r.mDrawable.getIntrinsicWidth() + r.mInsetL + r.mInsetR + padL + padR;
            if (w > width) {
                width = w;
            }
            if (nest) {
                padL += this.mPaddingL[i];
                padR += this.mPaddingR[i];
            }
        }
        return width;
    }

    @Override // android.graphics.drawable.Drawable
    public int getIntrinsicHeight() {
        int height = -1;
        int padT = 0;
        int padB = 0;
        boolean nest = this.mLayerState.mPaddingMode == 0;
        ChildDrawable[] array = this.mLayerState.mChildren;
        int N = this.mLayerState.mNum;
        for (int i = 0; i < N; i++) {
            ChildDrawable r = array[i];
            int h = r.mDrawable.getIntrinsicHeight() + r.mInsetT + r.mInsetB + padT + padB;
            if (h > height) {
                height = h;
            }
            if (nest) {
                padT += this.mPaddingT[i];
                padB += this.mPaddingB[i];
            }
        }
        return height;
    }

    private boolean refreshChildPadding(int i, ChildDrawable r) {
        Rect rect = this.mTmpRect;
        r.mDrawable.getPadding(rect);
        if (rect.left == this.mPaddingL[i] && rect.top == this.mPaddingT[i] && rect.right == this.mPaddingR[i] && rect.bottom == this.mPaddingB[i]) {
            return false;
        }
        this.mPaddingL[i] = rect.left;
        this.mPaddingT[i] = rect.top;
        this.mPaddingR[i] = rect.right;
        this.mPaddingB[i] = rect.bottom;
        return true;
    }

    /* access modifiers changed from: package-private */
    public void ensurePadding() {
        int N = this.mLayerState.mNum;
        if (this.mPaddingL == null || this.mPaddingL.length < N) {
            this.mPaddingL = new int[N];
            this.mPaddingT = new int[N];
            this.mPaddingR = new int[N];
            this.mPaddingB = new int[N];
        }
    }

    @Override // android.graphics.drawable.Drawable
    public Drawable.ConstantState getConstantState() {
        if (!this.mLayerState.canConstantState()) {
            return null;
        }
        this.mLayerState.mChangingConfigurations = getChangingConfigurations();
        return this.mLayerState;
    }

    @Override // android.graphics.drawable.Drawable
    public Drawable mutate() {
        if (!this.mMutated && super.mutate() == this) {
            this.mLayerState = createConstantState(this.mLayerState, null);
            ChildDrawable[] array = this.mLayerState.mChildren;
            int N = this.mLayerState.mNum;
            for (int i = 0; i < N; i++) {
                array[i].mDrawable.mutate();
            }
            this.mMutated = true;
        }
        return this;
    }

    @Override // android.graphics.drawable.Drawable
    public void setLayoutDirection(int layoutDirection) {
        ChildDrawable[] array = this.mLayerState.mChildren;
        int N = this.mLayerState.mNum;
        for (int i = 0; i < N; i++) {
            array[i].mDrawable.setLayoutDirection(layoutDirection);
        }
        super.setLayoutDirection(layoutDirection);
    }

    /* access modifiers changed from: package-private */
    public static class ChildDrawable {
        public Drawable mDrawable;
        public int mId = -1;
        public int mInsetB;
        public int mInsetL;
        public int mInsetR;
        public int mInsetT;
        public int[] mThemeAttrs;

        ChildDrawable() {
        }

        ChildDrawable(ChildDrawable orig, LayerDrawable owner, Resources res) {
            if (res != null) {
                this.mDrawable = orig.mDrawable.getConstantState().newDrawable(res);
            } else {
                this.mDrawable = orig.mDrawable.getConstantState().newDrawable();
            }
            this.mDrawable.setCallback(owner);
            this.mDrawable.setLayoutDirection(orig.mDrawable.getLayoutDirection());
            this.mDrawable.setBounds(orig.mDrawable.getBounds());
            this.mDrawable.setLevel(orig.mDrawable.getLevel());
            this.mThemeAttrs = orig.mThemeAttrs;
            this.mInsetL = orig.mInsetL;
            this.mInsetT = orig.mInsetT;
            this.mInsetR = orig.mInsetR;
            this.mInsetB = orig.mInsetB;
            this.mId = orig.mId;
        }
    }

    /* access modifiers changed from: package-private */
    public static class LayerState extends Drawable.ConstantState {
        private boolean mAutoMirrored = false;
        int mChangingConfigurations;
        ChildDrawable[] mChildren;
        int mChildrenChangingConfigurations;
        private boolean mHaveIsStateful;
        private boolean mHaveOpacity;
        private boolean mIsStateful;
        int mNum;
        private int mOpacity;
        private int mPaddingMode = 0;
        int[] mThemeAttrs;

        LayerState(LayerState orig, LayerDrawable owner, Resources res) {
            if (orig != null) {
                ChildDrawable[] origChildDrawable = orig.mChildren;
                int N = orig.mNum;
                this.mNum = N;
                this.mChildren = new ChildDrawable[N];
                this.mChangingConfigurations = orig.mChangingConfigurations;
                this.mChildrenChangingConfigurations = orig.mChildrenChangingConfigurations;
                for (int i = 0; i < N; i++) {
                    this.mChildren[i] = new ChildDrawable(origChildDrawable[i], owner, res);
                }
                this.mHaveOpacity = orig.mHaveOpacity;
                this.mOpacity = orig.mOpacity;
                this.mHaveIsStateful = orig.mHaveIsStateful;
                this.mIsStateful = orig.mIsStateful;
                this.mAutoMirrored = orig.mAutoMirrored;
                this.mPaddingMode = orig.mPaddingMode;
                this.mThemeAttrs = orig.mThemeAttrs;
                return;
            }
            this.mNum = 0;
            this.mChildren = null;
        }

        @Override // android.graphics.drawable.Drawable.ConstantState
        public boolean canApplyTheme() {
            return this.mThemeAttrs != null;
        }

        @Override // android.graphics.drawable.Drawable.ConstantState
        public Drawable newDrawable() {
            return new LayerDrawable(this, null, null);
        }

        @Override // android.graphics.drawable.Drawable.ConstantState
        public Drawable newDrawable(Resources res) {
            return new LayerDrawable(this, res, null);
        }

        @Override // android.graphics.drawable.Drawable.ConstantState
        public Drawable newDrawable(Resources res, Resources.Theme theme) {
            return new LayerDrawable(this, res, theme);
        }

        @Override // android.graphics.drawable.Drawable.ConstantState
        public int getChangingConfigurations() {
            return this.mChangingConfigurations;
        }

        public final int getOpacity() {
            if (this.mHaveOpacity) {
                return this.mOpacity;
            }
            ChildDrawable[] array = this.mChildren;
            int N = this.mNum;
            int op = N > 0 ? array[0].mDrawable.getOpacity() : -2;
            for (int i = 1; i < N; i++) {
                op = Drawable.resolveOpacity(op, array[i].mDrawable.getOpacity());
            }
            this.mOpacity = op;
            this.mHaveOpacity = true;
            return op;
        }

        public final boolean isStateful() {
            if (this.mHaveIsStateful) {
                return this.mIsStateful;
            }
            ChildDrawable[] array = this.mChildren;
            int N = this.mNum;
            boolean isStateful = false;
            int i = 0;
            while (true) {
                if (i >= N) {
                    break;
                } else if (array[i].mDrawable.isStateful()) {
                    isStateful = true;
                    break;
                } else {
                    i++;
                }
            }
            this.mIsStateful = isStateful;
            this.mHaveIsStateful = true;
            return isStateful;
        }

        public final boolean canConstantState() {
            ChildDrawable[] array = this.mChildren;
            int N = this.mNum;
            for (int i = 0; i < N; i++) {
                if (array[i].mDrawable.getConstantState() == null) {
                    return false;
                }
            }
            return true;
        }

        public void invalidateCache() {
            this.mHaveOpacity = false;
            this.mHaveIsStateful = false;
        }
    }
}

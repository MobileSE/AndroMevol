package android.graphics.drawable;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Insets;
import android.graphics.Outline;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import com.android.internal.R;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class InsetDrawable extends Drawable implements Drawable.Callback {
    private InsetState mInsetState;
    private boolean mMutated;
    private final Rect mTmpRect;

    InsetDrawable() {
        this((InsetState) null, (Resources) null);
    }

    public InsetDrawable(Drawable drawable, int inset) {
        this(drawable, inset, inset, inset, inset);
    }

    public InsetDrawable(Drawable drawable, int insetLeft, int insetTop, int insetRight, int insetBottom) {
        this((InsetState) null, (Resources) null);
        this.mInsetState.mDrawable = drawable;
        this.mInsetState.mInsetLeft = insetLeft;
        this.mInsetState.mInsetTop = insetTop;
        this.mInsetState.mInsetRight = insetRight;
        this.mInsetState.mInsetBottom = insetBottom;
        if (drawable != null) {
            drawable.setCallback(this);
        }
    }

    @Override // android.graphics.drawable.Drawable
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs, Resources.Theme theme) throws XmlPullParserException, IOException {
        int type;
        TypedArray a = obtainAttributes(r, theme, attrs, R.styleable.InsetDrawable);
        super.inflateWithAttributes(r, parser, a, 0);
        this.mInsetState.mDrawable = null;
        updateStateFromTypedArray(a);
        if (this.mInsetState.mDrawable == null) {
            do {
                type = parser.next();
            } while (type == 4);
            if (type != 2) {
                throw new XmlPullParserException(parser.getPositionDescription() + ": <inset> tag requires a 'drawable' attribute or " + "child tag defining a drawable");
            }
            Drawable dr = Drawable.createFromXmlInner(r, parser, attrs, theme);
            this.mInsetState.mDrawable = dr;
            dr.setCallback(this);
        }
        verifyRequiredAttributes(a);
        a.recycle();
    }

    private void verifyRequiredAttributes(TypedArray a) throws XmlPullParserException {
        if (this.mInsetState.mDrawable != null) {
            return;
        }
        if (this.mInsetState.mThemeAttrs == null || this.mInsetState.mThemeAttrs[1] == 0) {
            throw new XmlPullParserException(a.getPositionDescription() + ": <inset> tag requires a 'drawable' attribute or " + "child tag defining a drawable");
        }
    }

    private void updateStateFromTypedArray(TypedArray a) throws XmlPullParserException {
        InsetState state = this.mInsetState;
        state.mChangingConfigurations |= a.getChangingConfigurations();
        state.mThemeAttrs = a.extractThemeAttrs();
        int N = a.getIndexCount();
        for (int i = 0; i < N; i++) {
            int attr = a.getIndex(i);
            switch (attr) {
                case 1:
                    Drawable dr = a.getDrawable(attr);
                    if (dr != null) {
                        state.mDrawable = dr;
                        dr.setCallback(this);
                        break;
                    } else {
                        break;
                    }
                case 2:
                    state.mInsetLeft = a.getDimensionPixelOffset(attr, state.mInsetLeft);
                    break;
                case 3:
                    state.mInsetRight = a.getDimensionPixelOffset(attr, state.mInsetRight);
                    break;
                case 4:
                    state.mInsetTop = a.getDimensionPixelOffset(attr, state.mInsetTop);
                    break;
                case 5:
                    state.mInsetBottom = a.getDimensionPixelOffset(attr, state.mInsetBottom);
                    break;
                case 6:
                    int inset = a.getDimensionPixelOffset(attr, Integer.MIN_VALUE);
                    if (inset != Integer.MIN_VALUE) {
                        state.mInsetLeft = inset;
                        state.mInsetTop = inset;
                        state.mInsetRight = inset;
                        state.mInsetBottom = inset;
                        break;
                    } else {
                        break;
                    }
            }
        }
    }

    @Override // android.graphics.drawable.Drawable
    public void applyTheme(Resources.Theme t) {
        super.applyTheme(t);
        InsetState state = this.mInsetState;
        if (state != null && state.mThemeAttrs != null) {
            TypedArray a = t.resolveAttributes(state.mThemeAttrs, R.styleable.InsetDrawable);
            try {
                updateStateFromTypedArray(a);
                verifyRequiredAttributes(a);
                a.recycle();
            } catch (XmlPullParserException e) {
                throw new RuntimeException(e);
            } catch (Throwable th) {
                a.recycle();
                throw th;
            }
        }
    }

    @Override // android.graphics.drawable.Drawable
    public boolean canApplyTheme() {
        return (this.mInsetState == null || this.mInsetState.mThemeAttrs == null) ? false : true;
    }

    @Override // android.graphics.drawable.Drawable.Callback
    public void invalidateDrawable(Drawable who) {
        Drawable.Callback callback = getCallback();
        if (callback != null) {
            callback.invalidateDrawable(this);
        }
    }

    @Override // android.graphics.drawable.Drawable.Callback
    public void scheduleDrawable(Drawable who, Runnable what, long when) {
        Drawable.Callback callback = getCallback();
        if (callback != null) {
            callback.scheduleDrawable(this, what, when);
        }
    }

    @Override // android.graphics.drawable.Drawable.Callback
    public void unscheduleDrawable(Drawable who, Runnable what) {
        Drawable.Callback callback = getCallback();
        if (callback != null) {
            callback.unscheduleDrawable(this, what);
        }
    }

    @Override // android.graphics.drawable.Drawable
    public void draw(Canvas canvas) {
        this.mInsetState.mDrawable.draw(canvas);
    }

    @Override // android.graphics.drawable.Drawable
    public int getChangingConfigurations() {
        return super.getChangingConfigurations() | this.mInsetState.mChangingConfigurations | this.mInsetState.mDrawable.getChangingConfigurations();
    }

    @Override // android.graphics.drawable.Drawable
    public boolean getPadding(Rect padding) {
        boolean pad = this.mInsetState.mDrawable.getPadding(padding);
        padding.left += this.mInsetState.mInsetLeft;
        padding.right += this.mInsetState.mInsetRight;
        padding.top += this.mInsetState.mInsetTop;
        padding.bottom += this.mInsetState.mInsetBottom;
        return pad || (((this.mInsetState.mInsetLeft | this.mInsetState.mInsetRight) | this.mInsetState.mInsetTop) | this.mInsetState.mInsetBottom) != 0;
    }

    @Override // android.graphics.drawable.Drawable
    public Insets getOpticalInsets() {
        Insets contentInsets = super.getOpticalInsets();
        return Insets.of(contentInsets.left + this.mInsetState.mInsetLeft, contentInsets.top + this.mInsetState.mInsetTop, contentInsets.right + this.mInsetState.mInsetRight, contentInsets.bottom + this.mInsetState.mInsetBottom);
    }

    @Override // android.graphics.drawable.Drawable
    public void setHotspot(float x, float y) {
        this.mInsetState.mDrawable.setHotspot(x, y);
    }

    @Override // android.graphics.drawable.Drawable
    public void setHotspotBounds(int left, int top, int right, int bottom) {
        this.mInsetState.mDrawable.setHotspotBounds(left, top, right, bottom);
    }

    @Override // android.graphics.drawable.Drawable
    public void getHotspotBounds(Rect outRect) {
        this.mInsetState.mDrawable.getHotspotBounds(outRect);
    }

    @Override // android.graphics.drawable.Drawable
    public boolean setVisible(boolean visible, boolean restart) {
        this.mInsetState.mDrawable.setVisible(visible, restart);
        return super.setVisible(visible, restart);
    }

    @Override // android.graphics.drawable.Drawable
    public void setAlpha(int alpha) {
        this.mInsetState.mDrawable.setAlpha(alpha);
    }

    @Override // android.graphics.drawable.Drawable
    public int getAlpha() {
        return this.mInsetState.mDrawable.getAlpha();
    }

    @Override // android.graphics.drawable.Drawable
    public void setColorFilter(ColorFilter cf) {
        this.mInsetState.mDrawable.setColorFilter(cf);
    }

    @Override // android.graphics.drawable.Drawable
    public void setTintList(ColorStateList tint) {
        this.mInsetState.mDrawable.setTintList(tint);
    }

    @Override // android.graphics.drawable.Drawable
    public void setTintMode(PorterDuff.Mode tintMode) {
        this.mInsetState.mDrawable.setTintMode(tintMode);
    }

    @Override // android.graphics.drawable.Drawable
    public void setLayoutDirection(int layoutDirection) {
        this.mInsetState.mDrawable.setLayoutDirection(layoutDirection);
    }

    @Override // android.graphics.drawable.Drawable
    public int getOpacity() {
        return this.mInsetState.mDrawable.getOpacity();
    }

    @Override // android.graphics.drawable.Drawable
    public boolean isStateful() {
        return this.mInsetState.mDrawable.isStateful();
    }

    /* access modifiers changed from: protected */
    @Override // android.graphics.drawable.Drawable
    public boolean onStateChange(int[] state) {
        boolean changed = this.mInsetState.mDrawable.setState(state);
        onBoundsChange(getBounds());
        return changed;
    }

    /* access modifiers changed from: protected */
    @Override // android.graphics.drawable.Drawable
    public boolean onLevelChange(int level) {
        return this.mInsetState.mDrawable.setLevel(level);
    }

    /* access modifiers changed from: protected */
    @Override // android.graphics.drawable.Drawable
    public void onBoundsChange(Rect bounds) {
        Rect r = this.mTmpRect;
        r.set(bounds);
        r.left += this.mInsetState.mInsetLeft;
        r.top += this.mInsetState.mInsetTop;
        r.right -= this.mInsetState.mInsetRight;
        r.bottom -= this.mInsetState.mInsetBottom;
        this.mInsetState.mDrawable.setBounds(r.left, r.top, r.right, r.bottom);
    }

    @Override // android.graphics.drawable.Drawable
    public int getIntrinsicWidth() {
        return this.mInsetState.mDrawable.getIntrinsicWidth();
    }

    @Override // android.graphics.drawable.Drawable
    public int getIntrinsicHeight() {
        return this.mInsetState.mDrawable.getIntrinsicHeight();
    }

    @Override // android.graphics.drawable.Drawable
    public void getOutline(Outline outline) {
        this.mInsetState.mDrawable.getOutline(outline);
    }

    @Override // android.graphics.drawable.Drawable
    public Drawable.ConstantState getConstantState() {
        if (!this.mInsetState.canConstantState()) {
            return null;
        }
        this.mInsetState.mChangingConfigurations = getChangingConfigurations();
        return this.mInsetState;
    }

    @Override // android.graphics.drawable.Drawable
    public Drawable mutate() {
        if (!this.mMutated && super.mutate() == this) {
            this.mInsetState.mDrawable.mutate();
            this.mMutated = true;
        }
        return this;
    }

    public Drawable getDrawable() {
        return this.mInsetState.mDrawable;
    }

    /* access modifiers changed from: package-private */
    public static final class InsetState extends Drawable.ConstantState {
        boolean mCanConstantState;
        int mChangingConfigurations;
        boolean mCheckedConstantState;
        Drawable mDrawable;
        int mInsetBottom;
        int mInsetLeft;
        int mInsetRight;
        int mInsetTop;
        int[] mThemeAttrs;

        InsetState(InsetState orig, InsetDrawable owner, Resources res) {
            if (orig != null) {
                this.mThemeAttrs = orig.mThemeAttrs;
                this.mChangingConfigurations = orig.mChangingConfigurations;
                if (res != null) {
                    this.mDrawable = orig.mDrawable.getConstantState().newDrawable(res);
                } else {
                    this.mDrawable = orig.mDrawable.getConstantState().newDrawable();
                }
                this.mDrawable.setCallback(owner);
                this.mDrawable.setLayoutDirection(orig.mDrawable.getLayoutDirection());
                this.mDrawable.setBounds(orig.mDrawable.getBounds());
                this.mDrawable.setLevel(orig.mDrawable.getLevel());
                this.mInsetLeft = orig.mInsetLeft;
                this.mInsetTop = orig.mInsetTop;
                this.mInsetRight = orig.mInsetRight;
                this.mInsetBottom = orig.mInsetBottom;
                this.mCanConstantState = true;
                this.mCheckedConstantState = true;
            }
        }

        @Override // android.graphics.drawable.Drawable.ConstantState
        public Drawable newDrawable() {
            return new InsetDrawable(this, null);
        }

        @Override // android.graphics.drawable.Drawable.ConstantState
        public Drawable newDrawable(Resources res) {
            return new InsetDrawable(this, res);
        }

        @Override // android.graphics.drawable.Drawable.ConstantState
        public int getChangingConfigurations() {
            return this.mChangingConfigurations;
        }

        /* access modifiers changed from: package-private */
        public boolean canConstantState() {
            if (!this.mCheckedConstantState) {
                this.mCanConstantState = this.mDrawable.getConstantState() != null;
                this.mCheckedConstantState = true;
            }
            return this.mCanConstantState;
        }
    }

    private InsetDrawable(InsetState state, Resources res) {
        this.mTmpRect = new Rect();
        this.mInsetState = new InsetState(state, this, res);
    }
}

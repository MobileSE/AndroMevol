package android.graphics.drawable;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.Gravity;

public class ClipDrawable extends Drawable implements Drawable.Callback {
    public static final int HORIZONTAL = 1;
    public static final int VERTICAL = 2;
    private ClipState mClipState;
    private final Rect mTmpRect;

    ClipDrawable() {
        this(null, null);
    }

    public ClipDrawable(Drawable drawable, int gravity, int orientation) {
        this(null, null);
        this.mClipState.mDrawable = drawable;
        this.mClipState.mGravity = gravity;
        this.mClipState.mOrientation = orientation;
        if (drawable != null) {
            drawable.setCallback(this);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:11:0x003f  */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x0037  */
    @Override // android.graphics.drawable.Drawable
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void inflate(android.content.res.Resources r11, org.xmlpull.v1.XmlPullParser r12, android.util.AttributeSet r13, android.content.res.Resources.Theme r14) throws org.xmlpull.v1.XmlPullParserException, java.io.IOException {
        /*
            r10 = this;
            r9 = 3
            r8 = 2
            r7 = 1
            super.inflate(r11, r12, r13, r14)
            int[] r6 = com.android.internal.R.styleable.ClipDrawable
            android.content.res.TypedArray r0 = obtainAttributes(r11, r14, r13, r6)
            int r3 = r0.getInt(r8, r7)
            r6 = 0
            int r2 = r0.getInt(r6, r9)
            android.graphics.drawable.Drawable r1 = r0.getDrawable(r7)
            r0.recycle()
            int r4 = r12.getDepth()
        L_0x0020:
            int r5 = r12.next()
            if (r5 == r7) goto L_0x0035
            if (r5 != r9) goto L_0x002e
            int r6 = r12.getDepth()
            if (r6 <= r4) goto L_0x0035
        L_0x002e:
            if (r5 != r8) goto L_0x0020
            android.graphics.drawable.Drawable r1 = android.graphics.drawable.Drawable.createFromXmlInner(r11, r12, r13, r14)
            goto L_0x0020
        L_0x0035:
            if (r1 != 0) goto L_0x003f
            java.lang.IllegalArgumentException r6 = new java.lang.IllegalArgumentException
            java.lang.String r7 = "No drawable specified for <clip>"
            r6.<init>(r7)
            throw r6
        L_0x003f:
            android.graphics.drawable.ClipDrawable$ClipState r6 = r10.mClipState
            r6.mDrawable = r1
            android.graphics.drawable.ClipDrawable$ClipState r6 = r10.mClipState
            r6.mOrientation = r3
            android.graphics.drawable.ClipDrawable$ClipState r6 = r10.mClipState
            r6.mGravity = r2
            r1.setCallback(r10)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: android.graphics.drawable.ClipDrawable.inflate(android.content.res.Resources, org.xmlpull.v1.XmlPullParser, android.util.AttributeSet, android.content.res.Resources$Theme):void");
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
    public int getChangingConfigurations() {
        return super.getChangingConfigurations() | this.mClipState.mChangingConfigurations | this.mClipState.mDrawable.getChangingConfigurations();
    }

    @Override // android.graphics.drawable.Drawable
    public boolean getPadding(Rect padding) {
        return this.mClipState.mDrawable.getPadding(padding);
    }

    @Override // android.graphics.drawable.Drawable
    public boolean setVisible(boolean visible, boolean restart) {
        this.mClipState.mDrawable.setVisible(visible, restart);
        return super.setVisible(visible, restart);
    }

    @Override // android.graphics.drawable.Drawable
    public void setAlpha(int alpha) {
        this.mClipState.mDrawable.setAlpha(alpha);
    }

    @Override // android.graphics.drawable.Drawable
    public int getAlpha() {
        return this.mClipState.mDrawable.getAlpha();
    }

    @Override // android.graphics.drawable.Drawable
    public void setColorFilter(ColorFilter cf) {
        this.mClipState.mDrawable.setColorFilter(cf);
    }

    @Override // android.graphics.drawable.Drawable
    public void setTintList(ColorStateList tint) {
        this.mClipState.mDrawable.setTintList(tint);
    }

    @Override // android.graphics.drawable.Drawable
    public void setTintMode(PorterDuff.Mode tintMode) {
        this.mClipState.mDrawable.setTintMode(tintMode);
    }

    @Override // android.graphics.drawable.Drawable
    public int getOpacity() {
        return this.mClipState.mDrawable.getOpacity();
    }

    @Override // android.graphics.drawable.Drawable
    public boolean isStateful() {
        return this.mClipState.mDrawable.isStateful();
    }

    /* access modifiers changed from: protected */
    @Override // android.graphics.drawable.Drawable
    public boolean onStateChange(int[] state) {
        return this.mClipState.mDrawable.setState(state);
    }

    /* access modifiers changed from: protected */
    @Override // android.graphics.drawable.Drawable
    public boolean onLevelChange(int level) {
        this.mClipState.mDrawable.setLevel(level);
        invalidateSelf();
        return true;
    }

    /* access modifiers changed from: protected */
    @Override // android.graphics.drawable.Drawable
    public void onBoundsChange(Rect bounds) {
        this.mClipState.mDrawable.setBounds(bounds);
    }

    @Override // android.graphics.drawable.Drawable
    public void draw(Canvas canvas) {
        if (this.mClipState.mDrawable.getLevel() != 0) {
            Rect r = this.mTmpRect;
            Rect bounds = getBounds();
            int level = getLevel();
            int w = bounds.width();
            if ((this.mClipState.mOrientation & 1) != 0) {
                w -= ((w + 0) * (10000 - level)) / 10000;
            }
            int h = bounds.height();
            if ((this.mClipState.mOrientation & 2) != 0) {
                h -= ((h + 0) * (10000 - level)) / 10000;
            }
            Gravity.apply(this.mClipState.mGravity, w, h, bounds, r, getLayoutDirection());
            if (w > 0 && h > 0) {
                canvas.save();
                canvas.clipRect(r);
                this.mClipState.mDrawable.draw(canvas);
                canvas.restore();
            }
        }
    }

    @Override // android.graphics.drawable.Drawable
    public int getIntrinsicWidth() {
        return this.mClipState.mDrawable.getIntrinsicWidth();
    }

    @Override // android.graphics.drawable.Drawable
    public int getIntrinsicHeight() {
        return this.mClipState.mDrawable.getIntrinsicHeight();
    }

    @Override // android.graphics.drawable.Drawable
    public Drawable.ConstantState getConstantState() {
        if (!this.mClipState.canConstantState()) {
            return null;
        }
        this.mClipState.mChangingConfigurations = getChangingConfigurations();
        return this.mClipState;
    }

    @Override // android.graphics.drawable.Drawable
    public void setLayoutDirection(int layoutDirection) {
        this.mClipState.mDrawable.setLayoutDirection(layoutDirection);
        super.setLayoutDirection(layoutDirection);
    }

    /* access modifiers changed from: package-private */
    public static final class ClipState extends Drawable.ConstantState {
        private boolean mCanConstantState;
        int mChangingConfigurations;
        private boolean mCheckedConstantState;
        Drawable mDrawable;
        int mGravity;
        int mOrientation;

        ClipState(ClipState orig, ClipDrawable owner, Resources res) {
            if (orig != null) {
                if (res != null) {
                    this.mDrawable = orig.mDrawable.getConstantState().newDrawable(res);
                } else {
                    this.mDrawable = orig.mDrawable.getConstantState().newDrawable();
                }
                this.mDrawable.setCallback(owner);
                this.mDrawable.setLayoutDirection(orig.mDrawable.getLayoutDirection());
                this.mDrawable.setBounds(orig.mDrawable.getBounds());
                this.mDrawable.setLevel(orig.mDrawable.getLevel());
                this.mOrientation = orig.mOrientation;
                this.mGravity = orig.mGravity;
                this.mCanConstantState = true;
                this.mCheckedConstantState = true;
            }
        }

        @Override // android.graphics.drawable.Drawable.ConstantState
        public Drawable newDrawable() {
            return new ClipDrawable(this, (Resources) null);
        }

        @Override // android.graphics.drawable.Drawable.ConstantState
        public Drawable newDrawable(Resources res) {
            return new ClipDrawable(this, res);
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

    private ClipDrawable(ClipState state, Resources res) {
        this.mTmpRect = new Rect();
        this.mClipState = new ClipState(state, this, res);
    }
}

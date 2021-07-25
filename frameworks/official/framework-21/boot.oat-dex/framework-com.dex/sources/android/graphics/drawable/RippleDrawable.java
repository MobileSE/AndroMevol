package android.graphics.drawable;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import com.android.internal.R;
import java.io.IOException;
import java.util.Arrays;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class RippleDrawable extends LayerDrawable {
    private static final PorterDuffXfermode DST_IN = new PorterDuffXfermode(PorterDuff.Mode.DST_IN);
    private static final int MAX_RIPPLES = 10;
    public static final int RADIUS_AUTO = -1;
    private static final PorterDuffXfermode SRC_ATOP = new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP);
    private static final PorterDuffXfermode SRC_OVER = new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER);
    private RippleBackground mBackground;
    private boolean mBackgroundActive;
    private float mDensity;
    private final Rect mDirtyBounds;
    private final Rect mDrawingBounds;
    private Ripple[] mExitingRipples;
    private int mExitingRipplesCount;
    private boolean mHasPending;
    private final Rect mHotspotBounds;
    private Drawable mMask;
    private Paint mMaskingPaint;
    private boolean mNeedsDraw;
    private boolean mOverrideBounds;
    private float mPendingX;
    private float mPendingY;
    private Ripple mRipple;
    private boolean mRippleActive;
    private Paint mRipplePaint;
    private RippleState mState;
    private final Rect mTempRect;

    RippleDrawable() {
        this(new RippleState(null, null, null), (Resources) null, (Resources.Theme) null);
    }

    public RippleDrawable(ColorStateList color, Drawable content, Drawable mask) {
        this(new RippleState(null, null, null), (Resources) null, (Resources.Theme) null);
        if (color == null) {
            throw new IllegalArgumentException("RippleDrawable requires a non-null color");
        }
        if (content != null) {
            addLayer(content, null, 0, 0, 0, 0, 0);
        }
        if (mask != null) {
            addLayer(mask, null, 16908334, 0, 0, 0, 0);
        }
        setColor(color);
        ensurePadding();
        initializeFromState();
    }

    @Override // android.graphics.drawable.Drawable
    public void jumpToCurrentState() {
        super.jumpToCurrentState();
        boolean needsDraw = false;
        if (this.mRipple != null) {
            needsDraw = false | this.mRipple.isHardwareAnimating();
            this.mRipple.jump();
        }
        if (this.mBackground != null) {
            needsDraw |= this.mBackground.isHardwareAnimating();
            this.mBackground.jump();
        }
        this.mNeedsDraw = needsDraw | cancelExitingRipples();
        invalidateSelf();
    }

    private boolean cancelExitingRipples() {
        boolean needsDraw = false;
        int count = this.mExitingRipplesCount;
        Ripple[] ripples = this.mExitingRipples;
        for (int i = 0; i < count; i++) {
            needsDraw |= ripples[i].isHardwareAnimating();
            ripples[i].cancel();
        }
        if (ripples != null) {
            Arrays.fill(ripples, 0, count, (Object) null);
        }
        this.mExitingRipplesCount = 0;
        return needsDraw;
    }

    @Override // android.graphics.drawable.LayerDrawable, android.graphics.drawable.Drawable
    public void setAlpha(int alpha) {
        super.setAlpha(alpha);
    }

    @Override // android.graphics.drawable.LayerDrawable, android.graphics.drawable.Drawable
    public void setColorFilter(ColorFilter cf) {
        super.setColorFilter(cf);
    }

    @Override // android.graphics.drawable.LayerDrawable, android.graphics.drawable.Drawable
    public int getOpacity() {
        return -3;
    }

    /* access modifiers changed from: protected */
    @Override // android.graphics.drawable.LayerDrawable, android.graphics.drawable.Drawable
    public boolean onStateChange(int[] stateSet) {
        boolean z = false;
        boolean changed = super.onStateChange(stateSet);
        boolean enabled = false;
        boolean pressed = false;
        boolean focused = false;
        for (int state : stateSet) {
            if (state == 16842910) {
                enabled = true;
            }
            if (state == 16842908) {
                focused = true;
            }
            if (state == 16842919) {
                pressed = true;
            }
        }
        setRippleActive(enabled && pressed);
        if (focused || (enabled && pressed)) {
            z = true;
        }
        setBackgroundActive(z);
        return changed;
    }

    private void setRippleActive(boolean active) {
        if (this.mRippleActive != active) {
            this.mRippleActive = active;
            if (active) {
                tryRippleEnter();
            } else {
                tryRippleExit();
            }
        }
    }

    private void setBackgroundActive(boolean active) {
        if (this.mBackgroundActive != active) {
            this.mBackgroundActive = active;
            if (active) {
                tryBackgroundEnter();
            } else {
                tryBackgroundExit();
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.graphics.drawable.LayerDrawable, android.graphics.drawable.Drawable
    public void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        if (!this.mOverrideBounds) {
            this.mHotspotBounds.set(bounds);
            onHotspotBoundsChanged();
        }
        invalidateSelf();
    }

    @Override // android.graphics.drawable.LayerDrawable, android.graphics.drawable.Drawable
    public boolean setVisible(boolean visible, boolean restart) {
        boolean changed = super.setVisible(visible, restart);
        if (!visible) {
            clearHotspots();
        } else if (changed) {
            if (this.mRippleActive) {
                tryRippleEnter();
            }
            if (this.mBackgroundActive) {
                tryBackgroundEnter();
            }
        }
        return changed;
    }

    @Override // android.graphics.drawable.LayerDrawable, android.graphics.drawable.Drawable
    public boolean isProjected() {
        return getNumberOfLayers() == 0;
    }

    @Override // android.graphics.drawable.LayerDrawable, android.graphics.drawable.Drawable
    public boolean isStateful() {
        return true;
    }

    public void setColor(ColorStateList color) {
        this.mState.mColor = color;
        invalidateSelf();
    }

    @Override // android.graphics.drawable.LayerDrawable, android.graphics.drawable.Drawable
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs, Resources.Theme theme) throws XmlPullParserException, IOException {
        TypedArray a = obtainAttributes(r, theme, attrs, R.styleable.RippleDrawable);
        updateStateFromTypedArray(a);
        a.recycle();
        setPaddingMode(1);
        super.inflate(r, parser, attrs, theme);
        setTargetDensity(r.getDisplayMetrics());
        initializeFromState();
    }

    @Override // android.graphics.drawable.LayerDrawable
    public boolean setDrawableByLayerId(int id, Drawable drawable) {
        if (!super.setDrawableByLayerId(id, drawable)) {
            return false;
        }
        if (id == 16908334) {
            this.mMask = drawable;
        }
        return true;
    }

    @Override // android.graphics.drawable.LayerDrawable
    public void setPaddingMode(int mode) {
        super.setPaddingMode(mode);
    }

    private void updateStateFromTypedArray(TypedArray a) throws XmlPullParserException {
        RippleState state = this.mState;
        state.mChangingConfigurations |= a.getChangingConfigurations();
        state.mTouchThemeAttrs = a.extractThemeAttrs();
        ColorStateList color = a.getColorStateList(0);
        if (color != null) {
            this.mState.mColor = color;
        }
        verifyRequiredAttributes(a);
    }

    private void verifyRequiredAttributes(TypedArray a) throws XmlPullParserException {
        if (this.mState.mColor != null) {
            return;
        }
        if (this.mState.mTouchThemeAttrs == null || this.mState.mTouchThemeAttrs[0] == 0) {
            throw new XmlPullParserException(a.getPositionDescription() + ": <ripple> requires a valid color attribute");
        }
    }

    private void setTargetDensity(DisplayMetrics metrics) {
        if (this.mDensity != metrics.density) {
            this.mDensity = metrics.density;
            invalidateSelf();
        }
    }

    @Override // android.graphics.drawable.LayerDrawable, android.graphics.drawable.Drawable
    public void applyTheme(Resources.Theme t) {
        super.applyTheme(t);
        RippleState state = this.mState;
        if (state != null && state.mTouchThemeAttrs != null) {
            TypedArray a = t.resolveAttributes(state.mTouchThemeAttrs, R.styleable.RippleDrawable);
            try {
                updateStateFromTypedArray(a);
                a.recycle();
                initializeFromState();
            } catch (XmlPullParserException e) {
                throw new RuntimeException(e);
            } catch (Throwable th) {
                a.recycle();
                throw th;
            }
        }
    }

    @Override // android.graphics.drawable.LayerDrawable, android.graphics.drawable.Drawable
    public boolean canApplyTheme() {
        return super.canApplyTheme() || !(this.mState == null || this.mState.mTouchThemeAttrs == null);
    }

    @Override // android.graphics.drawable.LayerDrawable, android.graphics.drawable.Drawable
    public void setHotspot(float x, float y) {
        if (this.mRipple == null || this.mBackground == null) {
            this.mPendingX = x;
            this.mPendingY = y;
            this.mHasPending = true;
        }
        if (this.mRipple != null) {
            this.mRipple.move(x, y);
        }
    }

    private void tryBackgroundEnter() {
        if (this.mBackground == null) {
            this.mBackground = new RippleBackground(this, this.mHotspotBounds);
        }
        this.mBackground.setup(this.mState.mMaxRadius, this.mState.mColor.getColorForState(getState(), 0), this.mDensity);
        this.mBackground.enter();
    }

    private void tryBackgroundExit() {
        if (this.mBackground != null) {
            this.mBackground.exit();
        }
    }

    private void tryRippleEnter() {
        float x;
        float y;
        if (this.mExitingRipplesCount < 10) {
            if (this.mRipple == null) {
                if (this.mHasPending) {
                    this.mHasPending = false;
                    x = this.mPendingX;
                    y = this.mPendingY;
                } else {
                    x = this.mHotspotBounds.exactCenterX();
                    y = this.mHotspotBounds.exactCenterY();
                }
                this.mRipple = new Ripple(this, this.mHotspotBounds, x, y);
            }
            this.mRipple.setup(this.mState.mMaxRadius, this.mState.mColor.getColorForState(getState(), 0), this.mDensity);
            this.mRipple.enter();
        }
    }

    private void tryRippleExit() {
        if (this.mRipple != null) {
            if (this.mExitingRipples == null) {
                this.mExitingRipples = new Ripple[10];
            }
            Ripple[] rippleArr = this.mExitingRipples;
            int i = this.mExitingRipplesCount;
            this.mExitingRipplesCount = i + 1;
            rippleArr[i] = this.mRipple;
            this.mRipple.exit();
            this.mRipple = null;
        }
    }

    private void clearHotspots() {
        boolean needsDraw = false;
        if (this.mRipple != null) {
            needsDraw = false | this.mRipple.isHardwareAnimating();
            this.mRipple.cancel();
            this.mRipple = null;
        }
        if (this.mBackground != null) {
            needsDraw |= this.mBackground.isHardwareAnimating();
            this.mBackground.cancel();
            this.mBackground = null;
        }
        this.mNeedsDraw = needsDraw | cancelExitingRipples();
        invalidateSelf();
    }

    @Override // android.graphics.drawable.LayerDrawable, android.graphics.drawable.Drawable
    public void setHotspotBounds(int left, int top, int right, int bottom) {
        this.mOverrideBounds = true;
        this.mHotspotBounds.set(left, top, right, bottom);
        onHotspotBoundsChanged();
    }

    @Override // android.graphics.drawable.LayerDrawable, android.graphics.drawable.Drawable
    public void getHotspotBounds(Rect outRect) {
        outRect.set(this.mHotspotBounds);
    }

    private void onHotspotBoundsChanged() {
        int count = this.mExitingRipplesCount;
        Ripple[] ripples = this.mExitingRipples;
        for (int i = 0; i < count; i++) {
            ripples[i].onHotspotBoundsChanged();
        }
        if (this.mRipple != null) {
            this.mRipple.onHotspotBoundsChanged();
        }
        if (this.mBackground != null) {
            this.mBackground.onHotspotBoundsChanged();
        }
    }

    @Override // android.graphics.drawable.LayerDrawable, android.graphics.drawable.Drawable
    public void getOutline(Outline outline) {
        LayerDrawable.LayerState state = this.mLayerState;
        LayerDrawable.ChildDrawable[] children = state.mChildren;
        int N = state.mNum;
        for (int i = 0; i < N; i++) {
            if (children[i].mId != 16908334) {
                children[i].mDrawable.getOutline(outline);
                if (!outline.isEmpty()) {
                    return;
                }
            }
        }
    }

    @Override // android.graphics.drawable.LayerDrawable, android.graphics.drawable.Drawable
    public void draw(Canvas canvas) {
        boolean hasMask;
        int i;
        boolean drawMask;
        int contentLayer;
        if (this.mMask != null) {
            hasMask = true;
        } else {
            hasMask = false;
        }
        int i2 = this.mLayerState.mNum;
        if (hasMask) {
            i = 1;
        } else {
            i = 0;
        }
        boolean drawNonMaskContent = i2 > i;
        if (!hasMask || this.mMask.getOpacity() == -1) {
            drawMask = false;
        } else {
            drawMask = true;
        }
        Rect bounds = getDirtyBounds();
        int saveCount = canvas.save(2);
        canvas.clipRect(bounds);
        if (drawNonMaskContent) {
            contentLayer = drawContentLayer(canvas, bounds, SRC_OVER);
        } else {
            contentLayer = -1;
        }
        PorterDuffXfermode xfermode = (hasMask || !drawNonMaskContent) ? SRC_OVER : SRC_ATOP;
        int backgroundLayer = drawBackgroundLayer(canvas, bounds, xfermode, drawMask);
        if (backgroundLayer >= 0) {
            if (drawMask) {
                drawMaskingLayer(canvas, bounds, DST_IN);
            }
            canvas.restoreToCount(backgroundLayer);
        }
        int rippleLayer = drawRippleLayer(canvas, bounds, xfermode);
        if (rippleLayer >= 0) {
            if (drawMask) {
                drawMaskingLayer(canvas, bounds, DST_IN);
            }
            canvas.restoreToCount(rippleLayer);
        }
        if (contentLayer < 0 && backgroundLayer < 0 && rippleLayer < 0 && this.mNeedsDraw) {
            canvas.drawColor(0);
            invalidateSelf();
        }
        this.mNeedsDraw = false;
        canvas.restoreToCount(saveCount);
    }

    /* access modifiers changed from: package-private */
    public void removeRipple(Ripple ripple) {
        Ripple[] ripples = this.mExitingRipples;
        int count = this.mExitingRipplesCount;
        int index = getRippleIndex(ripple);
        if (index >= 0) {
            System.arraycopy(ripples, index + 1, ripples, index, count - (index + 1));
            ripples[count - 1] = null;
            this.mExitingRipplesCount--;
            invalidateSelf();
        }
    }

    private int getRippleIndex(Ripple ripple) {
        Ripple[] ripples = this.mExitingRipples;
        int count = this.mExitingRipplesCount;
        for (int i = 0; i < count; i++) {
            if (ripples[i] == ripple) {
                return i;
            }
        }
        return -1;
    }

    private int drawContentLayer(Canvas canvas, Rect bounds, PorterDuffXfermode mode) {
        int restoreToCount = -1;
        LayerDrawable.ChildDrawable[] array = this.mLayerState.mChildren;
        int count = this.mLayerState.mNum;
        boolean needsLayer = false;
        if ((this.mExitingRipplesCount > 0 || this.mBackground != null) && this.mMask == null) {
            int i = 0;
            while (true) {
                if (i >= count) {
                    break;
                }
                if (!(array[i].mId == 16908334 || array[i].mDrawable.getOpacity() == -1)) {
                    needsLayer = true;
                    break;
                }
                i++;
            }
        }
        Paint maskingPaint = getMaskingPaint(mode);
        if (needsLayer) {
            restoreToCount = canvas.saveLayer((float) bounds.left, (float) bounds.top, (float) bounds.right, (float) bounds.bottom, maskingPaint);
        }
        for (int i2 = 0; i2 < count; i2++) {
            if (array[i2].mId != 16908334) {
                array[i2].mDrawable.draw(canvas);
            }
        }
        return restoreToCount;
    }

    private int drawBackgroundLayer(Canvas canvas, Rect bounds, PorterDuffXfermode mode, boolean drawMask) {
        int saveCount = -1;
        if (this.mBackground != null && this.mBackground.shouldDraw()) {
            if (drawMask || mode != SRC_OVER) {
                saveCount = canvas.saveLayer((float) bounds.left, (float) bounds.top, (float) bounds.right, (float) bounds.bottom, getMaskingPaint(mode));
            }
            float x = this.mHotspotBounds.exactCenterX();
            float y = this.mHotspotBounds.exactCenterY();
            canvas.translate(x, y);
            this.mBackground.draw(canvas, getRipplePaint());
            canvas.translate(-x, -y);
        }
        return saveCount;
    }

    private int drawRippleLayer(Canvas canvas, Rect bounds, PorterDuffXfermode mode) {
        Ripple ripple;
        boolean drewRipples = false;
        int restoreToCount = -1;
        int restoreTranslate = -1;
        int count = this.mExitingRipplesCount;
        Ripple[] ripples = this.mExitingRipples;
        for (int i = 0; i <= count; i++) {
            if (i < count) {
                ripple = ripples[i];
            } else if (this.mRipple != null) {
                ripple = this.mRipple;
            }
            if (restoreToCount < 0) {
                Paint maskingPaint = getMaskingPaint(mode);
                maskingPaint.setAlpha(Color.alpha(this.mState.mColor.getColorForState(getState(), 0)) / 2);
                restoreToCount = canvas.saveLayer((float) bounds.left, (float) bounds.top, (float) bounds.right, (float) bounds.bottom, maskingPaint);
                restoreTranslate = canvas.save();
                canvas.translate(this.mHotspotBounds.exactCenterX(), this.mHotspotBounds.exactCenterY());
            }
            drewRipples |= ripple.draw(canvas, getRipplePaint());
        }
        if (restoreTranslate >= 0) {
            canvas.restoreToCount(restoreTranslate);
        }
        if (restoreToCount < 0 || drewRipples) {
            return restoreToCount;
        }
        canvas.restoreToCount(restoreToCount);
        return -1;
    }

    private int drawMaskingLayer(Canvas canvas, Rect bounds, PorterDuffXfermode mode) {
        int restoreToCount = canvas.saveLayer((float) bounds.left, (float) bounds.top, (float) bounds.right, (float) bounds.bottom, getMaskingPaint(mode));
        canvas.drawColor(0);
        this.mMask.draw(canvas);
        return restoreToCount;
    }

    private Paint getRipplePaint() {
        if (this.mRipplePaint == null) {
            this.mRipplePaint = new Paint();
            this.mRipplePaint.setAntiAlias(true);
        }
        return this.mRipplePaint;
    }

    private Paint getMaskingPaint(PorterDuffXfermode xfermode) {
        if (this.mMaskingPaint == null) {
            this.mMaskingPaint = new Paint();
        }
        this.mMaskingPaint.setXfermode(xfermode);
        this.mMaskingPaint.setAlpha(255);
        return this.mMaskingPaint;
    }

    @Override // android.graphics.drawable.Drawable
    public Rect getDirtyBounds() {
        if (!isProjected()) {
            return getBounds();
        }
        Rect drawingBounds = this.mDrawingBounds;
        Rect dirtyBounds = this.mDirtyBounds;
        dirtyBounds.set(drawingBounds);
        drawingBounds.setEmpty();
        int cX = (int) this.mHotspotBounds.exactCenterX();
        int cY = (int) this.mHotspotBounds.exactCenterY();
        Rect rippleBounds = this.mTempRect;
        Ripple[] activeRipples = this.mExitingRipples;
        int N = this.mExitingRipplesCount;
        for (int i = 0; i < N; i++) {
            activeRipples[i].getBounds(rippleBounds);
            rippleBounds.offset(cX, cY);
            drawingBounds.union(rippleBounds);
        }
        RippleBackground background = this.mBackground;
        if (background != null) {
            background.getBounds(rippleBounds);
            rippleBounds.offset(cX, cY);
            drawingBounds.union(rippleBounds);
        }
        dirtyBounds.union(drawingBounds);
        dirtyBounds.union(super.getDirtyBounds());
        return dirtyBounds;
    }

    @Override // android.graphics.drawable.LayerDrawable, android.graphics.drawable.Drawable
    public Drawable.ConstantState getConstantState() {
        return this.mState;
    }

    @Override // android.graphics.drawable.LayerDrawable, android.graphics.drawable.Drawable
    public Drawable mutate() {
        super.mutate();
        this.mState = (RippleState) this.mLayerState;
        return this;
    }

    /* access modifiers changed from: package-private */
    @Override // android.graphics.drawable.LayerDrawable
    public RippleState createConstantState(LayerDrawable.LayerState state, Resources res) {
        return new RippleState(state, this, res);
    }

    /* access modifiers changed from: package-private */
    public static class RippleState extends LayerDrawable.LayerState {
        ColorStateList mColor = ColorStateList.valueOf(Color.MAGENTA);
        int mMaxRadius = -1;
        int[] mTouchThemeAttrs;

        public RippleState(LayerDrawable.LayerState orig, RippleDrawable owner, Resources res) {
            super(orig, owner, res);
            if (orig != null && (orig instanceof RippleState)) {
                RippleState origs = (RippleState) orig;
                this.mTouchThemeAttrs = origs.mTouchThemeAttrs;
                this.mColor = origs.mColor;
                this.mMaxRadius = origs.mMaxRadius;
            }
        }

        @Override // android.graphics.drawable.Drawable.ConstantState, android.graphics.drawable.LayerDrawable.LayerState
        public boolean canApplyTheme() {
            return this.mTouchThemeAttrs != null || super.canApplyTheme();
        }

        @Override // android.graphics.drawable.Drawable.ConstantState, android.graphics.drawable.LayerDrawable.LayerState
        public Drawable newDrawable() {
            return new RippleDrawable(this, null, null);
        }

        @Override // android.graphics.drawable.Drawable.ConstantState, android.graphics.drawable.LayerDrawable.LayerState
        public Drawable newDrawable(Resources res) {
            return new RippleDrawable(this, res, null);
        }

        @Override // android.graphics.drawable.Drawable.ConstantState, android.graphics.drawable.LayerDrawable.LayerState
        public Drawable newDrawable(Resources res, Resources.Theme theme) {
            return new RippleDrawable(this, res, theme);
        }
    }

    public void setMaxRadius(int maxRadius) {
        if (maxRadius == -1 || maxRadius >= 0) {
            this.mState.mMaxRadius = maxRadius;
            return;
        }
        throw new IllegalArgumentException("maxRadius must be RADIUS_AUTO or >= 0");
    }

    public int getMaxRadius() {
        return this.mState.mMaxRadius;
    }

    private RippleDrawable(RippleState state, Resources res, Resources.Theme theme) {
        RippleState ns;
        this.mTempRect = new Rect();
        this.mHotspotBounds = new Rect();
        this.mDrawingBounds = new Rect();
        this.mDirtyBounds = new Rect();
        this.mExitingRipplesCount = 0;
        this.mDensity = 1.0f;
        boolean needsTheme = false;
        if (theme != null && state != null && state.canApplyTheme()) {
            ns = new RippleState(state, this, res);
            needsTheme = true;
        } else if (state == null) {
            ns = new RippleState(null, this, res);
        } else {
            ns = new RippleState(state, this, res);
        }
        if (res != null) {
            this.mDensity = res.getDisplayMetrics().density;
        }
        this.mState = ns;
        this.mLayerState = ns;
        if (ns.mNum > 0) {
            ensurePadding();
        }
        if (needsTheme) {
            applyTheme(theme);
        }
        initializeFromState();
    }

    private void initializeFromState() {
        this.mMask = findDrawableByLayerId(16908334);
    }
}

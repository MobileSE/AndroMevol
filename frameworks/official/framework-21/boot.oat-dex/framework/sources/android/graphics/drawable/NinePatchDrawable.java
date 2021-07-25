package android.graphics.drawable;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Insets;
import android.graphics.NinePatch;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import com.android.internal.R;
import java.io.IOException;
import java.io.InputStream;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class NinePatchDrawable extends Drawable {
    private static final boolean DEFAULT_DITHER = false;
    private int mBitmapHeight;
    private int mBitmapWidth;
    private boolean mMutated;
    private NinePatch mNinePatch;
    private NinePatchState mNinePatchState;
    private Insets mOpticalInsets;
    private Rect mPadding;
    private Paint mPaint;
    private int mTargetDensity;
    private PorterDuffColorFilter mTintFilter;

    NinePatchDrawable() {
        this.mOpticalInsets = Insets.NONE;
        this.mTargetDensity = 160;
        this.mBitmapWidth = -1;
        this.mBitmapHeight = -1;
        this.mNinePatchState = new NinePatchState();
    }

    @Deprecated
    public NinePatchDrawable(Bitmap bitmap, byte[] chunk, Rect padding, String srcName) {
        this(new NinePatchState(new NinePatch(bitmap, chunk, srcName), padding), null, null);
    }

    public NinePatchDrawable(Resources res, Bitmap bitmap, byte[] chunk, Rect padding, String srcName) {
        this(new NinePatchState(new NinePatch(bitmap, chunk, srcName), padding), res, null);
        this.mNinePatchState.mTargetDensity = this.mTargetDensity;
    }

    public NinePatchDrawable(Resources res, Bitmap bitmap, byte[] chunk, Rect padding, Rect opticalInsets, String srcName) {
        this(new NinePatchState(new NinePatch(bitmap, chunk, srcName), padding, opticalInsets), res, null);
        this.mNinePatchState.mTargetDensity = this.mTargetDensity;
    }

    @Deprecated
    public NinePatchDrawable(NinePatch patch) {
        this(new NinePatchState(patch, new Rect()), null, null);
    }

    public NinePatchDrawable(Resources res, NinePatch patch) {
        this(new NinePatchState(patch, new Rect()), res, null);
        this.mNinePatchState.mTargetDensity = this.mTargetDensity;
    }

    public void setTargetDensity(Canvas canvas) {
        setTargetDensity(canvas.getDensity());
    }

    public void setTargetDensity(DisplayMetrics metrics) {
        setTargetDensity(metrics.densityDpi);
    }

    public void setTargetDensity(int density) {
        if (density != this.mTargetDensity) {
            if (density == 0) {
                density = 160;
            }
            this.mTargetDensity = density;
            if (this.mNinePatch != null) {
                computeBitmapSize();
            }
            invalidateSelf();
        }
    }

    private static Insets scaleFromDensity(Insets insets, int sdensity, int tdensity) {
        return Insets.of(Bitmap.scaleFromDensity(insets.left, sdensity, tdensity), Bitmap.scaleFromDensity(insets.top, sdensity, tdensity), Bitmap.scaleFromDensity(insets.right, sdensity, tdensity), Bitmap.scaleFromDensity(insets.bottom, sdensity, tdensity));
    }

    private void computeBitmapSize() {
        int sdensity = this.mNinePatch.getDensity();
        int tdensity = this.mTargetDensity;
        if (sdensity == tdensity) {
            this.mBitmapWidth = this.mNinePatch.getWidth();
            this.mBitmapHeight = this.mNinePatch.getHeight();
            this.mOpticalInsets = this.mNinePatchState.mOpticalInsets;
            return;
        }
        this.mBitmapWidth = Bitmap.scaleFromDensity(this.mNinePatch.getWidth(), sdensity, tdensity);
        this.mBitmapHeight = Bitmap.scaleFromDensity(this.mNinePatch.getHeight(), sdensity, tdensity);
        if (!(this.mNinePatchState.mPadding == null || this.mPadding == null)) {
            Rect dest = this.mPadding;
            Rect src = this.mNinePatchState.mPadding;
            if (dest == src) {
                dest = new Rect(src);
                this.mPadding = dest;
            }
            dest.left = Bitmap.scaleFromDensity(src.left, sdensity, tdensity);
            dest.top = Bitmap.scaleFromDensity(src.top, sdensity, tdensity);
            dest.right = Bitmap.scaleFromDensity(src.right, sdensity, tdensity);
            dest.bottom = Bitmap.scaleFromDensity(src.bottom, sdensity, tdensity);
        }
        this.mOpticalInsets = scaleFromDensity(this.mNinePatchState.mOpticalInsets, sdensity, tdensity);
    }

    private void setNinePatch(NinePatch ninePatch) {
        if (this.mNinePatch != ninePatch) {
            this.mNinePatch = ninePatch;
            if (ninePatch != null) {
                computeBitmapSize();
            } else {
                this.mBitmapHeight = -1;
                this.mBitmapWidth = -1;
                this.mOpticalInsets = Insets.NONE;
            }
            invalidateSelf();
        }
    }

    @Override // android.graphics.drawable.Drawable
    public void draw(Canvas canvas) {
        boolean clearColorFilter;
        int restoreAlpha;
        Rect bounds = getBounds();
        if (this.mTintFilter == null || getPaint().getColorFilter() != null) {
            clearColorFilter = false;
        } else {
            this.mPaint.setColorFilter(this.mTintFilter);
            clearColorFilter = true;
        }
        if (needsMirroring()) {
            canvas.translate((float) (bounds.right - bounds.left), 0.0f);
            canvas.scale(-1.0f, 1.0f);
        }
        if (this.mNinePatchState.mBaseAlpha != 1.0f) {
            restoreAlpha = this.mPaint.getAlpha();
            this.mPaint.setAlpha((int) ((((float) restoreAlpha) * this.mNinePatchState.mBaseAlpha) + 0.5f));
        } else {
            restoreAlpha = -1;
        }
        this.mNinePatch.draw(canvas, bounds, this.mPaint);
        if (clearColorFilter) {
            this.mPaint.setColorFilter(null);
        }
        if (restoreAlpha >= 0) {
            this.mPaint.setAlpha(restoreAlpha);
        }
    }

    @Override // android.graphics.drawable.Drawable
    public int getChangingConfigurations() {
        return super.getChangingConfigurations() | this.mNinePatchState.mChangingConfigurations;
    }

    @Override // android.graphics.drawable.Drawable
    public boolean getPadding(Rect padding) {
        Rect scaledPadding = this.mPadding;
        if (scaledPadding == null) {
            return false;
        }
        if (needsMirroring()) {
            padding.set(scaledPadding.right, scaledPadding.top, scaledPadding.left, scaledPadding.bottom);
        } else {
            padding.set(scaledPadding);
        }
        if ((padding.left | padding.top | padding.right | padding.bottom) != 0) {
            return true;
        }
        return false;
    }

    @Override // android.graphics.drawable.Drawable
    public void getOutline(Outline outline) {
        NinePatch.InsetStruct insets;
        Rect bounds = getBounds();
        if (!bounds.isEmpty()) {
            if (this.mNinePatchState == null || (insets = this.mNinePatchState.getBitmap().getNinePatchInsets()) == null) {
                super.getOutline(outline);
                return;
            }
            Rect outlineInsets = insets.outlineRect;
            outline.setRoundRect(outlineInsets.left + bounds.left, outlineInsets.top + bounds.top, bounds.right - outlineInsets.right, bounds.bottom - outlineInsets.bottom, insets.outlineRadius);
            outline.setAlpha(insets.outlineAlpha * (((float) getAlpha()) / 255.0f));
        }
    }

    @Override // android.graphics.drawable.Drawable
    public Insets getOpticalInsets() {
        if (needsMirroring()) {
            return Insets.of(this.mOpticalInsets.right, this.mOpticalInsets.top, this.mOpticalInsets.left, this.mOpticalInsets.bottom);
        }
        return this.mOpticalInsets;
    }

    @Override // android.graphics.drawable.Drawable
    public void setAlpha(int alpha) {
        if (this.mPaint != null || alpha != 255) {
            getPaint().setAlpha(alpha);
            invalidateSelf();
        }
    }

    @Override // android.graphics.drawable.Drawable
    public int getAlpha() {
        if (this.mPaint == null) {
            return 255;
        }
        return getPaint().getAlpha();
    }

    @Override // android.graphics.drawable.Drawable
    public void setColorFilter(ColorFilter cf) {
        if (this.mPaint != null || cf != null) {
            getPaint().setColorFilter(cf);
            invalidateSelf();
        }
    }

    @Override // android.graphics.drawable.Drawable
    public void setTintList(ColorStateList tint) {
        this.mNinePatchState.mTint = tint;
        this.mTintFilter = updateTintFilter(this.mTintFilter, tint, this.mNinePatchState.mTintMode);
        invalidateSelf();
    }

    @Override // android.graphics.drawable.Drawable
    public void setTintMode(PorterDuff.Mode tintMode) {
        this.mNinePatchState.mTintMode = tintMode;
        this.mTintFilter = updateTintFilter(this.mTintFilter, this.mNinePatchState.mTint, tintMode);
        invalidateSelf();
    }

    @Override // android.graphics.drawable.Drawable
    public void setDither(boolean dither) {
        if (this.mPaint != null || dither) {
            getPaint().setDither(dither);
            invalidateSelf();
        }
    }

    @Override // android.graphics.drawable.Drawable
    public void setAutoMirrored(boolean mirrored) {
        this.mNinePatchState.mAutoMirrored = mirrored;
    }

    private boolean needsMirroring() {
        return isAutoMirrored() && getLayoutDirection() == 1;
    }

    @Override // android.graphics.drawable.Drawable
    public boolean isAutoMirrored() {
        return this.mNinePatchState.mAutoMirrored;
    }

    @Override // android.graphics.drawable.Drawable
    public void setFilterBitmap(boolean filter) {
        getPaint().setFilterBitmap(filter);
        invalidateSelf();
    }

    @Override // android.graphics.drawable.Drawable
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs, Resources.Theme theme) throws XmlPullParserException, IOException {
        super.inflate(r, parser, attrs, theme);
        TypedArray a = obtainAttributes(r, theme, attrs, R.styleable.NinePatchDrawable);
        updateStateFromTypedArray(a);
        a.recycle();
    }

    private void updateStateFromTypedArray(TypedArray a) throws XmlPullParserException {
        Resources r = a.getResources();
        NinePatchState state = this.mNinePatchState;
        state.mChangingConfigurations |= a.getChangingConfigurations();
        state.mThemeAttrs = a.extractThemeAttrs();
        state.mDither = a.getBoolean(1, state.mDither);
        int srcResId = a.getResourceId(0, 0);
        if (srcResId != 0) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inDither = !state.mDither;
            options.inScreenDensity = r.getDisplayMetrics().noncompatDensityDpi;
            Rect padding = new Rect();
            Rect opticalInsets = new Rect();
            Bitmap bitmap = null;
            try {
                TypedValue value = new TypedValue();
                InputStream is = r.openRawResource(srcResId, value);
                bitmap = BitmapFactory.decodeResourceStream(r, value, is, padding, options);
                is.close();
            } catch (IOException e) {
            }
            if (bitmap == null) {
                throw new XmlPullParserException(a.getPositionDescription() + ": <nine-patch> requires a valid src attribute");
            } else if (bitmap.getNinePatchChunk() == null) {
                throw new XmlPullParserException(a.getPositionDescription() + ": <nine-patch> requires a valid 9-patch source image");
            } else {
                bitmap.getOpticalInsets(opticalInsets);
                state.mNinePatch = new NinePatch(bitmap, bitmap.getNinePatchChunk());
                state.mPadding = padding;
                state.mOpticalInsets = Insets.of(opticalInsets);
            }
        }
        state.mAutoMirrored = a.getBoolean(4, state.mAutoMirrored);
        state.mBaseAlpha = a.getFloat(3, state.mBaseAlpha);
        int tintMode = a.getInt(5, -1);
        if (tintMode != -1) {
            state.mTintMode = Drawable.parseTintMode(tintMode, PorterDuff.Mode.SRC_IN);
        }
        ColorStateList tint = a.getColorStateList(2);
        if (tint != null) {
            state.mTint = tint;
        }
        initializeWithState(state, r);
        state.mTargetDensity = this.mTargetDensity;
    }

    @Override // android.graphics.drawable.Drawable
    public void applyTheme(Resources.Theme t) {
        super.applyTheme(t);
        NinePatchState state = this.mNinePatchState;
        if (state != null && state.mThemeAttrs != null) {
            TypedArray a = t.resolveAttributes(state.mThemeAttrs, R.styleable.NinePatchDrawable);
            try {
                updateStateFromTypedArray(a);
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
        return (this.mNinePatchState == null || this.mNinePatchState.mThemeAttrs == null) ? false : true;
    }

    public Paint getPaint() {
        if (this.mPaint == null) {
            this.mPaint = new Paint();
            this.mPaint.setDither(false);
        }
        return this.mPaint;
    }

    @Override // android.graphics.drawable.Drawable
    public int getIntrinsicWidth() {
        return this.mBitmapWidth;
    }

    @Override // android.graphics.drawable.Drawable
    public int getIntrinsicHeight() {
        return this.mBitmapHeight;
    }

    @Override // android.graphics.drawable.Drawable
    public int getMinimumWidth() {
        return this.mBitmapWidth;
    }

    @Override // android.graphics.drawable.Drawable
    public int getMinimumHeight() {
        return this.mBitmapHeight;
    }

    @Override // android.graphics.drawable.Drawable
    public int getOpacity() {
        return (this.mNinePatch.hasAlpha() || (this.mPaint != null && this.mPaint.getAlpha() < 255)) ? -3 : -1;
    }

    @Override // android.graphics.drawable.Drawable
    public Region getTransparentRegion() {
        return this.mNinePatch.getTransparentRegion(getBounds());
    }

    @Override // android.graphics.drawable.Drawable
    public Drawable.ConstantState getConstantState() {
        this.mNinePatchState.mChangingConfigurations = getChangingConfigurations();
        return this.mNinePatchState;
    }

    @Override // android.graphics.drawable.Drawable
    public Drawable mutate() {
        if (!this.mMutated && super.mutate() == this) {
            this.mNinePatchState = new NinePatchState(this.mNinePatchState);
            this.mNinePatch = this.mNinePatchState.mNinePatch;
            this.mMutated = true;
        }
        return this;
    }

    /* access modifiers changed from: protected */
    @Override // android.graphics.drawable.Drawable
    public boolean onStateChange(int[] stateSet) {
        NinePatchState state = this.mNinePatchState;
        if (state.mTint == null || state.mTintMode == null) {
            return false;
        }
        this.mTintFilter = updateTintFilter(this.mTintFilter, state.mTint, state.mTintMode);
        return true;
    }

    @Override // android.graphics.drawable.Drawable
    public boolean isStateful() {
        NinePatchState s = this.mNinePatchState;
        return super.isStateful() || (s.mTint != null && s.mTint.isStateful());
    }

    /* access modifiers changed from: package-private */
    public static final class NinePatchState extends Drawable.ConstantState {
        boolean mAutoMirrored;
        float mBaseAlpha;
        int mChangingConfigurations;
        boolean mDither;
        NinePatch mNinePatch;
        Insets mOpticalInsets;
        Rect mPadding;
        int mTargetDensity;
        int[] mThemeAttrs;
        ColorStateList mTint;
        PorterDuff.Mode mTintMode;

        NinePatchState() {
            this.mThemeAttrs = null;
            this.mNinePatch = null;
            this.mTint = null;
            this.mTintMode = Drawable.DEFAULT_TINT_MODE;
            this.mPadding = null;
            this.mOpticalInsets = Insets.NONE;
            this.mBaseAlpha = 1.0f;
            this.mDither = false;
            this.mTargetDensity = 160;
            this.mAutoMirrored = false;
        }

        NinePatchState(NinePatch ninePatch, Rect padding) {
            this(ninePatch, padding, null, false, false);
        }

        NinePatchState(NinePatch ninePatch, Rect padding, Rect opticalInsets) {
            this(ninePatch, padding, opticalInsets, false, false);
        }

        NinePatchState(NinePatch ninePatch, Rect padding, Rect opticalInsets, boolean dither, boolean autoMirror) {
            this.mThemeAttrs = null;
            this.mNinePatch = null;
            this.mTint = null;
            this.mTintMode = Drawable.DEFAULT_TINT_MODE;
            this.mPadding = null;
            this.mOpticalInsets = Insets.NONE;
            this.mBaseAlpha = 1.0f;
            this.mDither = false;
            this.mTargetDensity = 160;
            this.mAutoMirrored = false;
            this.mNinePatch = ninePatch;
            this.mPadding = padding;
            this.mOpticalInsets = Insets.of(opticalInsets);
            this.mDither = dither;
            this.mAutoMirrored = autoMirror;
        }

        NinePatchState(NinePatchState state) {
            this.mThemeAttrs = null;
            this.mNinePatch = null;
            this.mTint = null;
            this.mTintMode = Drawable.DEFAULT_TINT_MODE;
            this.mPadding = null;
            this.mOpticalInsets = Insets.NONE;
            this.mBaseAlpha = 1.0f;
            this.mDither = false;
            this.mTargetDensity = 160;
            this.mAutoMirrored = false;
            this.mNinePatch = state.mNinePatch;
            this.mTint = state.mTint;
            this.mTintMode = state.mTintMode;
            this.mThemeAttrs = state.mThemeAttrs;
            this.mPadding = state.mPadding;
            this.mOpticalInsets = state.mOpticalInsets;
            this.mBaseAlpha = state.mBaseAlpha;
            this.mDither = state.mDither;
            this.mChangingConfigurations = state.mChangingConfigurations;
            this.mTargetDensity = state.mTargetDensity;
            this.mAutoMirrored = state.mAutoMirrored;
        }

        @Override // android.graphics.drawable.Drawable.ConstantState
        public boolean canApplyTheme() {
            return this.mThemeAttrs != null;
        }

        @Override // android.graphics.drawable.Drawable.ConstantState
        public Bitmap getBitmap() {
            return this.mNinePatch.getBitmap();
        }

        @Override // android.graphics.drawable.Drawable.ConstantState
        public Drawable newDrawable() {
            return new NinePatchDrawable(this, (Resources) null, (Resources.Theme) null);
        }

        @Override // android.graphics.drawable.Drawable.ConstantState
        public Drawable newDrawable(Resources res) {
            return new NinePatchDrawable(this, res, (Resources.Theme) null);
        }

        @Override // android.graphics.drawable.Drawable.ConstantState
        public Drawable newDrawable(Resources res, Resources.Theme theme) {
            return new NinePatchDrawable(this, res, theme);
        }

        @Override // android.graphics.drawable.Drawable.ConstantState
        public int getChangingConfigurations() {
            return this.mChangingConfigurations;
        }
    }

    private NinePatchDrawable(NinePatchState state, Resources res, Resources.Theme theme) {
        this.mOpticalInsets = Insets.NONE;
        this.mTargetDensity = 160;
        this.mBitmapWidth = -1;
        this.mBitmapHeight = -1;
        if (theme == null || !state.canApplyTheme()) {
            this.mNinePatchState = state;
        } else {
            this.mNinePatchState = new NinePatchState(state);
            applyTheme(theme);
        }
        initializeWithState(state, res);
    }

    private void initializeWithState(NinePatchState state, Resources res) {
        if (res != null) {
            this.mTargetDensity = res.getDisplayMetrics().densityDpi;
        } else {
            this.mTargetDensity = state.mTargetDensity;
        }
        if (state.mDither) {
            setDither(state.mDither);
        }
        if (state.mPadding != null) {
            this.mPadding = new Rect(state.mPadding);
        }
        this.mTintFilter = updateTintFilter(this.mTintFilter, state.mTint, state.mTintMode);
        setNinePatch(state.mNinePatch);
    }
}

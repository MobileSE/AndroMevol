package android.graphics.drawable;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import com.android.internal.R;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class GradientDrawable extends Drawable {
    private static final float DEFAULT_INNER_RADIUS_RATIO = 3.0f;
    private static final float DEFAULT_THICKNESS_RATIO = 9.0f;
    public static final int LINE = 2;
    public static final int LINEAR_GRADIENT = 0;
    public static final int OVAL = 1;
    public static final int RADIAL_GRADIENT = 1;
    private static final int RADIUS_TYPE_FRACTION = 1;
    private static final int RADIUS_TYPE_FRACTION_PARENT = 2;
    private static final int RADIUS_TYPE_PIXELS = 0;
    public static final int RECTANGLE = 0;
    public static final int RING = 3;
    public static final int SWEEP_GRADIENT = 2;
    private int mAlpha;
    private ColorFilter mColorFilter;
    private final Paint mFillPaint;
    private boolean mGradientIsDirty;
    private float mGradientRadius;
    private GradientState mGradientState;
    private Paint mLayerPaint;
    private boolean mMutated;
    private Rect mPadding;
    private final Path mPath;
    private boolean mPathIsDirty;
    private final RectF mRect;
    private Path mRingPath;
    private Paint mStrokePaint;

    public enum Orientation {
        TOP_BOTTOM,
        TR_BL,
        RIGHT_LEFT,
        BR_TL,
        BOTTOM_TOP,
        BL_TR,
        LEFT_RIGHT,
        TL_BR
    }

    /* synthetic */ GradientDrawable(GradientState x0, Resources.Theme x1, AnonymousClass1 x2) {
        this(x0, x1);
    }

    public GradientDrawable() {
        this(new GradientState(Orientation.TOP_BOTTOM, null), (Resources.Theme) null);
    }

    public GradientDrawable(Orientation orientation, int[] colors) {
        this(new GradientState(orientation, colors), (Resources.Theme) null);
    }

    @Override // android.graphics.drawable.Drawable
    public boolean getPadding(Rect padding) {
        if (this.mPadding == null) {
            return super.getPadding(padding);
        }
        padding.set(this.mPadding);
        return true;
    }

    public void setCornerRadii(float[] radii) {
        this.mGradientState.setCornerRadii(radii);
        this.mPathIsDirty = true;
        invalidateSelf();
    }

    public void setCornerRadius(float radius) {
        this.mGradientState.setCornerRadius(radius);
        this.mPathIsDirty = true;
        invalidateSelf();
    }

    public void setStroke(int width, int color) {
        setStroke(width, color, 0.0f, 0.0f);
    }

    public void setStroke(int width, ColorStateList colorStateList) {
        setStroke(width, colorStateList, 0.0f, 0.0f);
    }

    public void setStroke(int width, int color, float dashWidth, float dashGap) {
        this.mGradientState.setStroke(width, ColorStateList.valueOf(color), dashWidth, dashGap);
        setStrokeInternal(width, color, dashWidth, dashGap);
    }

    public void setStroke(int width, ColorStateList colorStateList, float dashWidth, float dashGap) {
        int color;
        this.mGradientState.setStroke(width, colorStateList, dashWidth, dashGap);
        if (colorStateList == null) {
            color = 0;
        } else {
            color = colorStateList.getColorForState(getState(), 0);
        }
        setStrokeInternal(width, color, dashWidth, dashGap);
    }

    private void setStrokeInternal(int width, int color, float dashWidth, float dashGap) {
        if (this.mStrokePaint == null) {
            this.mStrokePaint = new Paint(1);
            this.mStrokePaint.setStyle(Paint.Style.STROKE);
        }
        this.mStrokePaint.setStrokeWidth((float) width);
        this.mStrokePaint.setColor(color);
        DashPathEffect e = null;
        if (dashWidth > 0.0f) {
            e = new DashPathEffect(new float[]{dashWidth, dashGap}, 0.0f);
        }
        this.mStrokePaint.setPathEffect(e);
        invalidateSelf();
    }

    public void setSize(int width, int height) {
        this.mGradientState.setSize(width, height);
        this.mPathIsDirty = true;
        invalidateSelf();
    }

    public void setShape(int shape) {
        this.mRingPath = null;
        this.mPathIsDirty = true;
        this.mGradientState.setShape(shape);
        invalidateSelf();
    }

    public void setGradientType(int gradient) {
        this.mGradientState.setGradientType(gradient);
        this.mGradientIsDirty = true;
        invalidateSelf();
    }

    public void setGradientCenter(float x, float y) {
        this.mGradientState.setGradientCenter(x, y);
        this.mGradientIsDirty = true;
        invalidateSelf();
    }

    public void setGradientRadius(float gradientRadius) {
        this.mGradientState.setGradientRadius(gradientRadius, 0);
        this.mGradientIsDirty = true;
        invalidateSelf();
    }

    public float getGradientRadius() {
        if (this.mGradientState.mGradient != 1) {
            return 0.0f;
        }
        ensureValidRect();
        return this.mGradientRadius;
    }

    public void setUseLevel(boolean useLevel) {
        this.mGradientState.mUseLevel = useLevel;
        this.mGradientIsDirty = true;
        invalidateSelf();
    }

    private int modulateAlpha(int alpha) {
        return (alpha * (this.mAlpha + (this.mAlpha >> 7))) >> 8;
    }

    public Orientation getOrientation() {
        return this.mGradientState.mOrientation;
    }

    public void setOrientation(Orientation orientation) {
        this.mGradientState.mOrientation = orientation;
        this.mGradientIsDirty = true;
        invalidateSelf();
    }

    public void setColors(int[] colors) {
        this.mGradientState.setColors(colors);
        this.mGradientIsDirty = true;
        invalidateSelf();
    }

    @Override // android.graphics.drawable.Drawable
    public void draw(Canvas canvas) {
        if (ensureValidRect()) {
            int prevFillAlpha = this.mFillPaint.getAlpha();
            int prevStrokeAlpha = this.mStrokePaint != null ? this.mStrokePaint.getAlpha() : 0;
            int currFillAlpha = modulateAlpha(prevFillAlpha);
            int currStrokeAlpha = modulateAlpha(prevStrokeAlpha);
            boolean haveStroke = currStrokeAlpha > 0 && this.mStrokePaint != null && this.mStrokePaint.getStrokeWidth() > 0.0f;
            boolean haveFill = currFillAlpha > 0;
            GradientState st = this.mGradientState;
            boolean useLayer = haveStroke && haveFill && st.mShape != 2 && currStrokeAlpha < 255 && (this.mAlpha < 255 || this.mColorFilter != null);
            if (useLayer) {
                if (this.mLayerPaint == null) {
                    this.mLayerPaint = new Paint();
                }
                this.mLayerPaint.setDither(st.mDither);
                this.mLayerPaint.setAlpha(this.mAlpha);
                this.mLayerPaint.setColorFilter(this.mColorFilter);
                float rad = this.mStrokePaint.getStrokeWidth();
                canvas.saveLayer(this.mRect.left - rad, this.mRect.top - rad, this.mRect.right + rad, this.mRect.bottom + rad, this.mLayerPaint, 4);
                this.mFillPaint.setColorFilter(null);
                this.mStrokePaint.setColorFilter(null);
            } else {
                this.mFillPaint.setAlpha(currFillAlpha);
                this.mFillPaint.setDither(st.mDither);
                this.mFillPaint.setColorFilter(this.mColorFilter);
                if (this.mColorFilter != null && st.mColorStateList == null) {
                    this.mFillPaint.setColor(this.mAlpha << 24);
                }
                if (haveStroke) {
                    this.mStrokePaint.setAlpha(currStrokeAlpha);
                    this.mStrokePaint.setDither(st.mDither);
                    this.mStrokePaint.setColorFilter(this.mColorFilter);
                }
            }
            switch (st.mShape) {
                case 0:
                    if (st.mRadiusArray == null) {
                        if (st.mRadius <= 0.0f) {
                            if (!(this.mFillPaint.getColor() == 0 && this.mColorFilter == null && this.mFillPaint.getShader() == null)) {
                                canvas.drawRect(this.mRect, this.mFillPaint);
                            }
                            if (haveStroke) {
                                canvas.drawRect(this.mRect, this.mStrokePaint);
                                break;
                            }
                        } else {
                            float rad2 = Math.min(st.mRadius, Math.min(this.mRect.width(), this.mRect.height()) * 0.5f);
                            canvas.drawRoundRect(this.mRect, rad2, rad2, this.mFillPaint);
                            if (haveStroke) {
                                canvas.drawRoundRect(this.mRect, rad2, rad2, this.mStrokePaint);
                                break;
                            }
                        }
                    } else {
                        buildPathIfDirty();
                        canvas.drawPath(this.mPath, this.mFillPaint);
                        if (haveStroke) {
                            canvas.drawPath(this.mPath, this.mStrokePaint);
                            break;
                        }
                    }
                    break;
                case 1:
                    canvas.drawOval(this.mRect, this.mFillPaint);
                    if (haveStroke) {
                        canvas.drawOval(this.mRect, this.mStrokePaint);
                        break;
                    }
                    break;
                case 2:
                    RectF r = this.mRect;
                    float y = r.centerY();
                    if (haveStroke) {
                        canvas.drawLine(r.left, y, r.right, y, this.mStrokePaint);
                        break;
                    }
                    break;
                case 3:
                    Path path = buildRing(st);
                    canvas.drawPath(path, this.mFillPaint);
                    if (haveStroke) {
                        canvas.drawPath(path, this.mStrokePaint);
                        break;
                    }
                    break;
            }
            if (useLayer) {
                canvas.restore();
                return;
            }
            this.mFillPaint.setAlpha(prevFillAlpha);
            if (haveStroke) {
                this.mStrokePaint.setAlpha(prevStrokeAlpha);
            }
        }
    }

    private void buildPathIfDirty() {
        GradientState st = this.mGradientState;
        if (this.mPathIsDirty) {
            ensureValidRect();
            this.mPath.reset();
            this.mPath.addRoundRect(this.mRect, st.mRadiusArray, Path.Direction.CW);
            this.mPathIsDirty = false;
        }
    }

    private Path buildRing(GradientState st) {
        float sweep;
        float radius;
        if (this.mRingPath != null && (!st.mUseLevelForShape || !this.mPathIsDirty)) {
            return this.mRingPath;
        }
        this.mPathIsDirty = false;
        if (st.mUseLevelForShape) {
            sweep = (((float) getLevel()) * 360.0f) / 10000.0f;
        } else {
            sweep = 360.0f;
        }
        RectF bounds = new RectF(this.mRect);
        float x = bounds.width() / 2.0f;
        float y = bounds.height() / 2.0f;
        float thickness = st.mThickness != -1 ? (float) st.mThickness : bounds.width() / st.mThicknessRatio;
        if (st.mInnerRadius != -1) {
            radius = (float) st.mInnerRadius;
        } else {
            radius = bounds.width() / st.mInnerRadiusRatio;
        }
        RectF innerBounds = new RectF(bounds);
        innerBounds.inset(x - radius, y - radius);
        RectF bounds2 = new RectF(innerBounds);
        bounds2.inset(-thickness, -thickness);
        if (this.mRingPath == null) {
            this.mRingPath = new Path();
        } else {
            this.mRingPath.reset();
        }
        Path ringPath = this.mRingPath;
        if (sweep >= 360.0f || sweep <= -360.0f) {
            ringPath.addOval(bounds2, Path.Direction.CW);
            ringPath.addOval(innerBounds, Path.Direction.CCW);
            return ringPath;
        }
        ringPath.setFillType(Path.FillType.EVEN_ODD);
        ringPath.moveTo(x + radius, y);
        ringPath.lineTo(x + radius + thickness, y);
        ringPath.arcTo(bounds2, 0.0f, sweep, false);
        ringPath.arcTo(innerBounds, sweep, -sweep, false);
        ringPath.close();
        return ringPath;
    }

    public void setColor(int argb) {
        this.mGradientState.setColorStateList(ColorStateList.valueOf(argb));
        this.mFillPaint.setColor(argb);
        invalidateSelf();
    }

    public void setColor(ColorStateList colorStateList) {
        int color;
        this.mGradientState.setColorStateList(colorStateList);
        if (colorStateList == null) {
            color = 0;
        } else {
            color = colorStateList.getColorForState(getState(), 0);
        }
        this.mFillPaint.setColor(color);
        invalidateSelf();
    }

    /* access modifiers changed from: protected */
    @Override // android.graphics.drawable.Drawable
    public boolean onStateChange(int[] stateSet) {
        ColorStateList strokeStateList;
        int newStrokeColor;
        int newColor;
        boolean invalidateSelf = false;
        GradientState s = this.mGradientState;
        ColorStateList stateList = s.mColorStateList;
        if (!(stateList == null || this.mFillPaint.getColor() == (newColor = stateList.getColorForState(stateSet, 0)))) {
            this.mFillPaint.setColor(newColor);
            invalidateSelf = true;
        }
        Paint strokePaint = this.mStrokePaint;
        if (!(strokePaint == null || (strokeStateList = s.mStrokeColorStateList) == null || strokePaint.getColor() == (newStrokeColor = strokeStateList.getColorForState(stateSet, 0)))) {
            strokePaint.setColor(newStrokeColor);
            invalidateSelf = true;
        }
        if (!invalidateSelf) {
            return false;
        }
        invalidateSelf();
        return true;
    }

    @Override // android.graphics.drawable.Drawable
    public boolean isStateful() {
        GradientState s = this.mGradientState;
        return super.isStateful() || (s.mColorStateList != null && s.mColorStateList.isStateful()) || (s.mStrokeColorStateList != null && s.mStrokeColorStateList.isStateful());
    }

    @Override // android.graphics.drawable.Drawable
    public int getChangingConfigurations() {
        return super.getChangingConfigurations() | this.mGradientState.mChangingConfigurations;
    }

    @Override // android.graphics.drawable.Drawable
    public void setAlpha(int alpha) {
        if (alpha != this.mAlpha) {
            this.mAlpha = alpha;
            invalidateSelf();
        }
    }

    @Override // android.graphics.drawable.Drawable
    public int getAlpha() {
        return this.mAlpha;
    }

    @Override // android.graphics.drawable.Drawable
    public void setDither(boolean dither) {
        if (dither != this.mGradientState.mDither) {
            this.mGradientState.mDither = dither;
            invalidateSelf();
        }
    }

    @Override // android.graphics.drawable.Drawable
    public ColorFilter getColorFilter() {
        return this.mColorFilter;
    }

    @Override // android.graphics.drawable.Drawable
    public void setColorFilter(ColorFilter cf) {
        if (cf != this.mColorFilter) {
            this.mColorFilter = cf;
            invalidateSelf();
        }
    }

    @Override // android.graphics.drawable.Drawable
    public int getOpacity() {
        return (this.mAlpha != 255 || !this.mGradientState.mOpaqueOverBounds || !isOpaqueForState()) ? -3 : -1;
    }

    /* access modifiers changed from: protected */
    @Override // android.graphics.drawable.Drawable
    public void onBoundsChange(Rect r) {
        super.onBoundsChange(r);
        this.mRingPath = null;
        this.mPathIsDirty = true;
        this.mGradientIsDirty = true;
    }

    /* access modifiers changed from: protected */
    @Override // android.graphics.drawable.Drawable
    public boolean onLevelChange(int level) {
        super.onLevelChange(level);
        this.mGradientIsDirty = true;
        this.mPathIsDirty = true;
        invalidateSelf();
        return true;
    }

    private boolean ensureValidRect() {
        float x0;
        float y0;
        float x1;
        float y1;
        if (this.mGradientIsDirty) {
            this.mGradientIsDirty = false;
            Rect bounds = getBounds();
            float inset = 0.0f;
            if (this.mStrokePaint != null) {
                inset = this.mStrokePaint.getStrokeWidth() * 0.5f;
            }
            GradientState st = this.mGradientState;
            this.mRect.set(((float) bounds.left) + inset, ((float) bounds.top) + inset, ((float) bounds.right) - inset, ((float) bounds.bottom) - inset);
            int[] colors = st.mColors;
            if (colors != null) {
                RectF r = this.mRect;
                if (st.mGradient == 0) {
                    float level = st.mUseLevel ? ((float) getLevel()) / 10000.0f : 1.0f;
                    switch (AnonymousClass1.$SwitchMap$android$graphics$drawable$GradientDrawable$Orientation[st.mOrientation.ordinal()]) {
                        case 1:
                            x0 = r.left;
                            y0 = r.top;
                            x1 = x0;
                            y1 = level * r.bottom;
                            break;
                        case 2:
                            x0 = r.right;
                            y0 = r.top;
                            x1 = level * r.left;
                            y1 = level * r.bottom;
                            break;
                        case 3:
                            x0 = r.right;
                            y0 = r.top;
                            x1 = level * r.left;
                            y1 = y0;
                            break;
                        case 4:
                            x0 = r.right;
                            y0 = r.bottom;
                            x1 = level * r.left;
                            y1 = level * r.top;
                            break;
                        case 5:
                            x0 = r.left;
                            y0 = r.bottom;
                            x1 = x0;
                            y1 = level * r.top;
                            break;
                        case 6:
                            x0 = r.left;
                            y0 = r.bottom;
                            x1 = level * r.right;
                            y1 = level * r.top;
                            break;
                        case 7:
                            x0 = r.left;
                            y0 = r.top;
                            x1 = level * r.right;
                            y1 = y0;
                            break;
                        default:
                            x0 = r.left;
                            y0 = r.top;
                            x1 = level * r.right;
                            y1 = level * r.bottom;
                            break;
                    }
                    this.mFillPaint.setShader(new LinearGradient(x0, y0, x1, y1, colors, st.mPositions, Shader.TileMode.CLAMP));
                } else if (st.mGradient == 1) {
                    float x02 = r.left + ((r.right - r.left) * st.mCenterX);
                    float y02 = r.top + ((r.bottom - r.top) * st.mCenterY);
                    float radius = st.mGradientRadius;
                    if (st.mGradientRadiusType == 1) {
                        radius *= (float) Math.min(st.mWidth, st.mHeight);
                    } else if (st.mGradientRadiusType == 2) {
                        radius *= Math.min(r.width(), r.height());
                    }
                    if (st.mUseLevel) {
                        radius *= ((float) getLevel()) / 10000.0f;
                    }
                    this.mGradientRadius = radius;
                    if (radius == 0.0f) {
                        radius = 0.001f;
                    }
                    this.mFillPaint.setShader(new RadialGradient(x02, y02, radius, colors, (float[]) null, Shader.TileMode.CLAMP));
                } else if (st.mGradient == 2) {
                    float x03 = r.left + ((r.right - r.left) * st.mCenterX);
                    float y03 = r.top + ((r.bottom - r.top) * st.mCenterY);
                    int[] tempColors = colors;
                    float[] tempPositions = null;
                    if (st.mUseLevel) {
                        tempColors = st.mTempColors;
                        int length = colors.length;
                        if (tempColors == null || tempColors.length != length + 1) {
                            tempColors = new int[(length + 1)];
                            st.mTempColors = tempColors;
                        }
                        System.arraycopy(colors, 0, tempColors, 0, length);
                        tempColors[length] = colors[length - 1];
                        tempPositions = st.mTempPositions;
                        float fraction = 1.0f / ((float) (length - 1));
                        if (tempPositions == null || tempPositions.length != length + 1) {
                            tempPositions = new float[(length + 1)];
                            st.mTempPositions = tempPositions;
                        }
                        float level2 = ((float) getLevel()) / 10000.0f;
                        for (int i = 0; i < length; i++) {
                            tempPositions[i] = ((float) i) * fraction * level2;
                        }
                        tempPositions[length] = 1.0f;
                    }
                    this.mFillPaint.setShader(new SweepGradient(x03, y03, tempColors, tempPositions));
                }
                if (st.mColorStateList == null) {
                    this.mFillPaint.setColor(-16777216);
                }
            }
        }
        if (!this.mRect.isEmpty()) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    /* renamed from: android.graphics.drawable.GradientDrawable$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$android$graphics$drawable$GradientDrawable$Orientation = new int[Orientation.values().length];

        static {
            try {
                $SwitchMap$android$graphics$drawable$GradientDrawable$Orientation[Orientation.TOP_BOTTOM.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$android$graphics$drawable$GradientDrawable$Orientation[Orientation.TR_BL.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$android$graphics$drawable$GradientDrawable$Orientation[Orientation.RIGHT_LEFT.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$android$graphics$drawable$GradientDrawable$Orientation[Orientation.BR_TL.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$android$graphics$drawable$GradientDrawable$Orientation[Orientation.BOTTOM_TOP.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$android$graphics$drawable$GradientDrawable$Orientation[Orientation.BL_TR.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$android$graphics$drawable$GradientDrawable$Orientation[Orientation.LEFT_RIGHT.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
        }
    }

    @Override // android.graphics.drawable.Drawable
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs, Resources.Theme theme) throws XmlPullParserException, IOException {
        TypedArray a = obtainAttributes(r, theme, attrs, R.styleable.GradientDrawable);
        super.inflateWithAttributes(r, parser, a, 1);
        updateStateFromTypedArray(a);
        a.recycle();
        inflateChildElements(r, parser, attrs, theme);
        this.mGradientState.computeOpacity();
    }

    @Override // android.graphics.drawable.Drawable
    public void applyTheme(Resources.Theme t) {
        super.applyTheme(t);
        GradientState state = this.mGradientState;
        if (state != null && state.mThemeAttrs != null) {
            TypedArray a = t.resolveAttributes(state.mThemeAttrs, R.styleable.GradientDrawable);
            updateStateFromTypedArray(a);
            a.recycle();
            applyThemeChildElements(t);
            state.computeOpacity();
        }
    }

    private void updateStateFromTypedArray(TypedArray a) {
        GradientState state = this.mGradientState;
        state.mChangingConfigurations |= a.getChangingConfigurations();
        state.mThemeAttrs = a.extractThemeAttrs();
        state.mShape = a.getInt(2, state.mShape);
        state.mDither = a.getBoolean(0, state.mDither);
        if (state.mShape == 3) {
            state.mInnerRadius = a.getDimensionPixelSize(6, state.mInnerRadius);
            if (state.mInnerRadius == -1) {
                state.mInnerRadiusRatio = a.getFloat(3, state.mInnerRadiusRatio);
            }
            state.mThickness = a.getDimensionPixelSize(7, state.mThickness);
            if (state.mThickness == -1) {
                state.mThicknessRatio = a.getFloat(4, state.mThicknessRatio);
            }
            state.mUseLevelForShape = a.getBoolean(5, state.mUseLevelForShape);
        }
    }

    @Override // android.graphics.drawable.Drawable
    public boolean canApplyTheme() {
        GradientState st = this.mGradientState;
        return (st == null || (st.mThemeAttrs == null && st.mAttrSize == null && st.mAttrGradient == null && st.mAttrSolid == null && st.mAttrStroke == null && st.mAttrCorners == null && st.mAttrPadding == null)) ? false : true;
    }

    private void applyThemeChildElements(Resources.Theme t) {
        GradientState st = this.mGradientState;
        if (st.mAttrSize != null) {
            TypedArray a = t.resolveAttributes(st.mAttrSize, R.styleable.GradientDrawableSize);
            updateGradientDrawableSize(a);
            a.recycle();
        }
        if (st.mAttrGradient != null) {
            TypedArray a2 = t.resolveAttributes(st.mAttrGradient, R.styleable.GradientDrawableGradient);
            try {
                updateGradientDrawableGradient(t.getResources(), a2);
                a2.recycle();
            } catch (XmlPullParserException e) {
                throw new RuntimeException(e);
            } catch (Throwable th) {
                a2.recycle();
                throw th;
            }
        }
        if (st.mAttrSolid != null) {
            TypedArray a3 = t.resolveAttributes(st.mAttrSolid, R.styleable.GradientDrawableSolid);
            updateGradientDrawableSolid(a3);
            a3.recycle();
        }
        if (st.mAttrStroke != null) {
            TypedArray a4 = t.resolveAttributes(st.mAttrStroke, R.styleable.GradientDrawableStroke);
            updateGradientDrawableStroke(a4);
            a4.recycle();
        }
        if (st.mAttrCorners != null) {
            TypedArray a5 = t.resolveAttributes(st.mAttrCorners, R.styleable.DrawableCorners);
            updateDrawableCorners(a5);
            a5.recycle();
        }
        if (st.mAttrPadding != null) {
            TypedArray a6 = t.resolveAttributes(st.mAttrPadding, R.styleable.GradientDrawablePadding);
            updateGradientDrawablePadding(a6);
            a6.recycle();
        }
    }

    private void inflateChildElements(Resources r, XmlPullParser parser, AttributeSet attrs, Resources.Theme theme) throws XmlPullParserException, IOException {
        int innerDepth = parser.getDepth() + 1;
        while (true) {
            int type = parser.next();
            if (type != 1) {
                int depth = parser.getDepth();
                if (depth < innerDepth && type == 3) {
                    return;
                }
                if (type == 2 && depth <= innerDepth) {
                    String name = parser.getName();
                    if (name.equals("size")) {
                        TypedArray a = obtainAttributes(r, theme, attrs, R.styleable.GradientDrawableSize);
                        updateGradientDrawableSize(a);
                        a.recycle();
                    } else if (name.equals("gradient")) {
                        TypedArray a2 = obtainAttributes(r, theme, attrs, R.styleable.GradientDrawableGradient);
                        updateGradientDrawableGradient(r, a2);
                        a2.recycle();
                    } else if (name.equals("solid")) {
                        TypedArray a3 = obtainAttributes(r, theme, attrs, R.styleable.GradientDrawableSolid);
                        updateGradientDrawableSolid(a3);
                        a3.recycle();
                    } else if (name.equals("stroke")) {
                        TypedArray a4 = obtainAttributes(r, theme, attrs, R.styleable.GradientDrawableStroke);
                        updateGradientDrawableStroke(a4);
                        a4.recycle();
                    } else if (name.equals("corners")) {
                        TypedArray a5 = obtainAttributes(r, theme, attrs, R.styleable.DrawableCorners);
                        updateDrawableCorners(a5);
                        a5.recycle();
                    } else if (name.equals("padding")) {
                        TypedArray a6 = obtainAttributes(r, theme, attrs, R.styleable.GradientDrawablePadding);
                        updateGradientDrawablePadding(a6);
                        a6.recycle();
                    } else {
                        Log.w("drawable", "Bad element under <shape>: " + name);
                    }
                }
            } else {
                return;
            }
        }
    }

    private void updateGradientDrawablePadding(TypedArray a) {
        GradientState st = this.mGradientState;
        st.mChangingConfigurations |= a.getChangingConfigurations();
        st.mAttrPadding = a.extractThemeAttrs();
        if (st.mPadding == null) {
            st.mPadding = new Rect();
        }
        Rect pad = st.mPadding;
        pad.set(a.getDimensionPixelOffset(0, pad.left), a.getDimensionPixelOffset(1, pad.top), a.getDimensionPixelOffset(2, pad.right), a.getDimensionPixelOffset(3, pad.bottom));
        this.mPadding = pad;
    }

    private void updateDrawableCorners(TypedArray a) {
        GradientState st = this.mGradientState;
        st.mChangingConfigurations |= a.getChangingConfigurations();
        st.mAttrCorners = a.extractThemeAttrs();
        int radius = a.getDimensionPixelSize(0, (int) st.mRadius);
        setCornerRadius((float) radius);
        int topLeftRadius = a.getDimensionPixelSize(1, radius);
        int topRightRadius = a.getDimensionPixelSize(2, radius);
        int bottomLeftRadius = a.getDimensionPixelSize(3, radius);
        int bottomRightRadius = a.getDimensionPixelSize(4, radius);
        if (topLeftRadius != radius || topRightRadius != radius || bottomLeftRadius != radius || bottomRightRadius != radius) {
            setCornerRadii(new float[]{(float) topLeftRadius, (float) topLeftRadius, (float) topRightRadius, (float) topRightRadius, (float) bottomRightRadius, (float) bottomRightRadius, (float) bottomLeftRadius, (float) bottomLeftRadius});
        }
    }

    private void updateGradientDrawableStroke(TypedArray a) {
        GradientState st = this.mGradientState;
        st.mChangingConfigurations |= a.getChangingConfigurations();
        st.mAttrStroke = a.extractThemeAttrs();
        int width = a.getDimensionPixelSize(0, Math.max(0, st.mStrokeWidth));
        float dashWidth = a.getDimension(2, st.mStrokeDashWidth);
        ColorStateList colorStateList = a.getColorStateList(1);
        if (colorStateList == null) {
            colorStateList = st.mStrokeColorStateList;
        }
        if (dashWidth != 0.0f) {
            setStroke(width, colorStateList, dashWidth, a.getDimension(3, st.mStrokeDashGap));
        } else {
            setStroke(width, colorStateList);
        }
    }

    private void updateGradientDrawableSolid(TypedArray a) {
        GradientState st = this.mGradientState;
        st.mChangingConfigurations |= a.getChangingConfigurations();
        st.mAttrSolid = a.extractThemeAttrs();
        ColorStateList colorStateList = a.getColorStateList(0);
        if (colorStateList != null) {
            setColor(colorStateList);
        }
    }

    private void updateGradientDrawableGradient(Resources r, TypedArray a) throws XmlPullParserException {
        float radius;
        int radiusType;
        GradientState st = this.mGradientState;
        st.mChangingConfigurations |= a.getChangingConfigurations();
        st.mAttrGradient = a.extractThemeAttrs();
        st.mCenterX = getFloatOrFraction(a, 5, st.mCenterX);
        st.mCenterY = getFloatOrFraction(a, 6, st.mCenterY);
        st.mUseLevel = a.getBoolean(2, st.mUseLevel);
        st.mGradient = a.getInt(4, st.mGradient);
        int startColor = a.getColor(0, 0);
        boolean hasCenterColor = a.hasValue(8);
        int centerColor = a.getColor(8, 0);
        int endColor = a.getColor(1, 0);
        if (hasCenterColor) {
            st.mColors = new int[3];
            st.mColors[0] = startColor;
            st.mColors[1] = centerColor;
            st.mColors[2] = endColor;
            st.mPositions = new float[3];
            st.mPositions[0] = 0.0f;
            st.mPositions[1] = st.mCenterX != 0.5f ? st.mCenterX : st.mCenterY;
            st.mPositions[2] = 1.0f;
        } else {
            st.mColors = new int[2];
            st.mColors[0] = startColor;
            st.mColors[1] = endColor;
        }
        if (st.mGradient == 0) {
            int angle = ((int) a.getFloat(3, (float) st.mAngle)) % 360;
            if (angle % 45 != 0) {
                throw new XmlPullParserException(a.getPositionDescription() + "<gradient> tag requires 'angle' attribute to " + "be a multiple of 45");
            }
            st.mAngle = angle;
            switch (angle) {
                case 0:
                    st.mOrientation = Orientation.LEFT_RIGHT;
                    return;
                case 45:
                    st.mOrientation = Orientation.BL_TR;
                    return;
                case 90:
                    st.mOrientation = Orientation.BOTTOM_TOP;
                    return;
                case 135:
                    st.mOrientation = Orientation.BR_TL;
                    return;
                case 180:
                    st.mOrientation = Orientation.RIGHT_LEFT;
                    return;
                case 225:
                    st.mOrientation = Orientation.TR_BL;
                    return;
                case android.R.styleable.Theme_windowTransitionBackgroundFadeDuration:
                    st.mOrientation = Orientation.TOP_BOTTOM;
                    return;
                case 315:
                    st.mOrientation = Orientation.TL_BR;
                    return;
                default:
                    return;
            }
        } else {
            TypedValue tv = a.peekValue(7);
            if (tv != null) {
                if (tv.type == 6) {
                    radius = tv.getFraction(1.0f, 1.0f);
                    if (((tv.data >> 0) & 15) == 1) {
                        radiusType = 2;
                    } else {
                        radiusType = 1;
                    }
                } else {
                    radius = tv.getDimension(r.getDisplayMetrics());
                    radiusType = 0;
                }
                st.mGradientRadius = radius;
                st.mGradientRadiusType = radiusType;
            } else if (st.mGradient == 1) {
                throw new XmlPullParserException(a.getPositionDescription() + "<gradient> tag requires 'gradientRadius' " + "attribute with radial type");
            }
        }
    }

    private void updateGradientDrawableSize(TypedArray a) {
        GradientState st = this.mGradientState;
        st.mChangingConfigurations |= a.getChangingConfigurations();
        st.mAttrSize = a.extractThemeAttrs();
        st.mWidth = a.getDimensionPixelSize(1, st.mWidth);
        st.mHeight = a.getDimensionPixelSize(0, st.mHeight);
    }

    private static float getFloatOrFraction(TypedArray a, int index, float defaultValue) {
        TypedValue tv = a.peekValue(index);
        if (tv == null) {
            return defaultValue;
        }
        return tv.type == 6 ? tv.getFraction(1.0f, 1.0f) : tv.getFloat();
    }

    @Override // android.graphics.drawable.Drawable
    public int getIntrinsicWidth() {
        return this.mGradientState.mWidth;
    }

    @Override // android.graphics.drawable.Drawable
    public int getIntrinsicHeight() {
        return this.mGradientState.mHeight;
    }

    @Override // android.graphics.drawable.Drawable
    public Drawable.ConstantState getConstantState() {
        this.mGradientState.mChangingConfigurations = getChangingConfigurations();
        return this.mGradientState;
    }

    private boolean isOpaqueForState() {
        if ((this.mGradientState.mStrokeWidth < 0 || this.mStrokePaint == null || isOpaque(this.mStrokePaint.getColor())) && isOpaque(this.mFillPaint.getColor())) {
            return true;
        }
        return false;
    }

    @Override // android.graphics.drawable.Drawable
    public void getOutline(Outline outline) {
        float halfStrokeWidth;
        GradientState st = this.mGradientState;
        Rect bounds = getBounds();
        outline.setAlpha((!st.mOpaqueOverShape || !isOpaqueForState()) ? 0.0f : ((float) this.mAlpha) / 255.0f);
        switch (st.mShape) {
            case 0:
                if (st.mRadiusArray != null) {
                    buildPathIfDirty();
                    outline.setConvexPath(this.mPath);
                    return;
                }
                float rad = 0.0f;
                if (st.mRadius > 0.0f) {
                    rad = Math.min(st.mRadius, ((float) Math.min(bounds.width(), bounds.height())) * 0.5f);
                }
                outline.setRoundRect(bounds, rad);
                return;
            case 1:
                outline.setOval(bounds);
                return;
            case 2:
                if (this.mStrokePaint == null) {
                    halfStrokeWidth = 1.0E-4f;
                } else {
                    halfStrokeWidth = this.mStrokePaint.getStrokeWidth() * 0.5f;
                }
                float centerY = (float) bounds.centerY();
                outline.setRect(bounds.left, (int) Math.floor((double) (centerY - halfStrokeWidth)), bounds.right, (int) Math.ceil((double) (centerY + halfStrokeWidth)));
                return;
            default:
                return;
        }
    }

    @Override // android.graphics.drawable.Drawable
    public Drawable mutate() {
        if (!this.mMutated && super.mutate() == this) {
            this.mGradientState = new GradientState(this.mGradientState);
            initializeWithState(this.mGradientState);
            this.mMutated = true;
        }
        return this;
    }

    /* access modifiers changed from: package-private */
    public static final class GradientState extends Drawable.ConstantState {
        public int mAngle = 0;
        int[] mAttrCorners;
        int[] mAttrGradient;
        int[] mAttrPadding;
        int[] mAttrSize;
        int[] mAttrSolid;
        int[] mAttrStroke;
        private float mCenterX = 0.5f;
        private float mCenterY = 0.5f;
        public int mChangingConfigurations;
        public ColorStateList mColorStateList;
        public int[] mColors;
        public boolean mDither = false;
        public int mGradient = 0;
        private float mGradientRadius = 0.5f;
        private int mGradientRadiusType = 0;
        public int mHeight = -1;
        public int mInnerRadius = -1;
        public float mInnerRadiusRatio = GradientDrawable.DEFAULT_INNER_RADIUS_RATIO;
        private boolean mOpaqueOverBounds;
        private boolean mOpaqueOverShape;
        public Orientation mOrientation;
        public Rect mPadding = null;
        public float[] mPositions;
        public float mRadius = 0.0f;
        public float[] mRadiusArray = null;
        public int mShape = 0;
        public ColorStateList mStrokeColorStateList;
        public float mStrokeDashGap = 0.0f;
        public float mStrokeDashWidth = 0.0f;
        public int mStrokeWidth = -1;
        public int[] mTempColors;
        public float[] mTempPositions;
        int[] mThemeAttrs;
        public int mThickness = -1;
        public float mThicknessRatio = GradientDrawable.DEFAULT_THICKNESS_RATIO;
        private boolean mUseLevel;
        private boolean mUseLevelForShape;
        public int mWidth = -1;

        GradientState(Orientation orientation, int[] colors) {
            this.mOrientation = orientation;
            setColors(colors);
        }

        public GradientState(GradientState state) {
            this.mChangingConfigurations = state.mChangingConfigurations;
            this.mShape = state.mShape;
            this.mGradient = state.mGradient;
            this.mAngle = state.mAngle;
            this.mOrientation = state.mOrientation;
            this.mColorStateList = state.mColorStateList;
            if (state.mColors != null) {
                this.mColors = (int[]) state.mColors.clone();
            }
            if (state.mPositions != null) {
                this.mPositions = (float[]) state.mPositions.clone();
            }
            this.mStrokeColorStateList = state.mStrokeColorStateList;
            this.mStrokeWidth = state.mStrokeWidth;
            this.mStrokeDashWidth = state.mStrokeDashWidth;
            this.mStrokeDashGap = state.mStrokeDashGap;
            this.mRadius = state.mRadius;
            if (state.mRadiusArray != null) {
                this.mRadiusArray = (float[]) state.mRadiusArray.clone();
            }
            if (state.mPadding != null) {
                this.mPadding = new Rect(state.mPadding);
            }
            this.mWidth = state.mWidth;
            this.mHeight = state.mHeight;
            this.mInnerRadiusRatio = state.mInnerRadiusRatio;
            this.mThicknessRatio = state.mThicknessRatio;
            this.mInnerRadius = state.mInnerRadius;
            this.mThickness = state.mThickness;
            this.mDither = state.mDither;
            this.mCenterX = state.mCenterX;
            this.mCenterY = state.mCenterY;
            this.mGradientRadius = state.mGradientRadius;
            this.mGradientRadiusType = state.mGradientRadiusType;
            this.mUseLevel = state.mUseLevel;
            this.mUseLevelForShape = state.mUseLevelForShape;
            this.mOpaqueOverBounds = state.mOpaqueOverBounds;
            this.mOpaqueOverShape = state.mOpaqueOverShape;
            this.mThemeAttrs = state.mThemeAttrs;
            this.mAttrSize = state.mAttrSize;
            this.mAttrGradient = state.mAttrGradient;
            this.mAttrSolid = state.mAttrSolid;
            this.mAttrStroke = state.mAttrStroke;
            this.mAttrCorners = state.mAttrCorners;
            this.mAttrPadding = state.mAttrPadding;
        }

        @Override // android.graphics.drawable.Drawable.ConstantState
        public boolean canApplyTheme() {
            return this.mThemeAttrs != null;
        }

        @Override // android.graphics.drawable.Drawable.ConstantState
        public Drawable newDrawable() {
            return new GradientDrawable(this, null, null);
        }

        @Override // android.graphics.drawable.Drawable.ConstantState
        public Drawable newDrawable(Resources res) {
            return new GradientDrawable(this, null, null);
        }

        @Override // android.graphics.drawable.Drawable.ConstantState
        public Drawable newDrawable(Resources res, Resources.Theme theme) {
            return new GradientDrawable(this, theme, null);
        }

        @Override // android.graphics.drawable.Drawable.ConstantState
        public int getChangingConfigurations() {
            return this.mChangingConfigurations;
        }

        public void setShape(int shape) {
            this.mShape = shape;
            computeOpacity();
        }

        public void setGradientType(int gradient) {
            this.mGradient = gradient;
        }

        public void setGradientCenter(float x, float y) {
            this.mCenterX = x;
            this.mCenterY = y;
        }

        public void setColors(int[] colors) {
            this.mColors = colors;
            this.mColorStateList = null;
            computeOpacity();
        }

        public void setColorStateList(ColorStateList colorStateList) {
            this.mColors = null;
            this.mColorStateList = colorStateList;
            computeOpacity();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void computeOpacity() {
            boolean z = true;
            this.mOpaqueOverBounds = false;
            this.mOpaqueOverShape = false;
            if (this.mColors != null) {
                for (int i = 0; i < this.mColors.length; i++) {
                    if (!GradientDrawable.isOpaque(this.mColors[i])) {
                        return;
                    }
                }
            }
            if (!(this.mColors == null && this.mColorStateList == null)) {
                this.mOpaqueOverShape = true;
                if (!(this.mShape == 0 && this.mRadius <= 0.0f && this.mRadiusArray == null)) {
                    z = false;
                }
                this.mOpaqueOverBounds = z;
            }
        }

        public void setStroke(int width, ColorStateList colorStateList, float dashWidth, float dashGap) {
            this.mStrokeWidth = width;
            this.mStrokeColorStateList = colorStateList;
            this.mStrokeDashWidth = dashWidth;
            this.mStrokeDashGap = dashGap;
            computeOpacity();
        }

        public void setCornerRadius(float radius) {
            if (radius < 0.0f) {
                radius = 0.0f;
            }
            this.mRadius = radius;
            this.mRadiusArray = null;
        }

        public void setCornerRadii(float[] radii) {
            this.mRadiusArray = radii;
            if (radii == null) {
                this.mRadius = 0.0f;
            }
        }

        public void setSize(int width, int height) {
            this.mWidth = width;
            this.mHeight = height;
        }

        public void setGradientRadius(float gradientRadius, int type) {
            this.mGradientRadius = gradientRadius;
            this.mGradientRadiusType = type;
        }
    }

    static boolean isOpaque(int color) {
        return ((color >> 24) & 255) == 255;
    }

    private GradientDrawable(GradientState state, Resources.Theme theme) {
        this.mFillPaint = new Paint(1);
        this.mAlpha = 255;
        this.mPath = new Path();
        this.mRect = new RectF();
        this.mPathIsDirty = true;
        if (theme == null || !state.canApplyTheme()) {
            this.mGradientState = state;
        } else {
            this.mGradientState = new GradientState(state);
            applyTheme(theme);
        }
        initializeWithState(state);
        this.mGradientIsDirty = true;
        this.mMutated = false;
    }

    private void initializeWithState(GradientState state) {
        if (state.mColorStateList != null) {
            this.mFillPaint.setColor(state.mColorStateList.getColorForState(getState(), 0));
        } else if (state.mColors == null) {
            this.mFillPaint.setColor(0);
        } else {
            this.mFillPaint.setColor(-16777216);
        }
        this.mPadding = state.mPadding;
        if (state.mStrokeWidth >= 0) {
            this.mStrokePaint = new Paint(1);
            this.mStrokePaint.setStyle(Paint.Style.STROKE);
            this.mStrokePaint.setStrokeWidth((float) state.mStrokeWidth);
            if (state.mStrokeColorStateList != null) {
                this.mStrokePaint.setColor(state.mStrokeColorStateList.getColorForState(getState(), 0));
            }
            if (state.mStrokeDashWidth != 0.0f) {
                this.mStrokePaint.setPathEffect(new DashPathEffect(new float[]{state.mStrokeDashWidth, state.mStrokeDashGap}, 0.0f));
            }
        }
    }
}

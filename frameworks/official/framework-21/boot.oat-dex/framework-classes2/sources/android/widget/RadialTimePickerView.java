package android.widget;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.mtp.MtpConstants;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import com.android.ims.ImsReasonInfo;
import com.android.internal.R;
import com.android.internal.telephony.PhoneConstants;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class RadialTimePickerView extends View implements View.OnTouchListener {
    private static final int ALPHA_AMPM_PRESSED = 255;
    private static final int ALPHA_AMPM_SELECTED = 60;
    private static final int ALPHA_OPAQUE = 255;
    private static final int ALPHA_SELECTOR = 60;
    private static final int ALPHA_TRANSPARENT = 0;
    private static final int AM = 0;
    private static final int AMPM = 3;
    private static final int CENTER_RADIUS = 2;
    private static final float COSINE_30_DEGREES = (((float) Math.sqrt(3.0d)) * SINE_30_DEGREES);
    private static final boolean DEBUG = false;
    private static final int DEBUG_COLOR = 553582592;
    private static final int DEBUG_STROKE_WIDTH = 2;
    private static final int DEBUG_TEXT_COLOR = 1627324416;
    private static final int DEGREES_FOR_ONE_HOUR = 30;
    private static final int DEGREES_FOR_ONE_MINUTE = 6;
    private static final int HOURS = 0;
    private static final int HOURS_INNER = 2;
    private static final int[] HOURS_NUMBERS = {12, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
    private static final int[] HOURS_NUMBERS_24 = {0, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23};
    private static final int MINUTES = 1;
    private static final int[] MINUTES_NUMBERS = {0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55};
    private static final int PM = 1;
    private static final int SELECTOR_CIRCLE = 0;
    private static final int SELECTOR_DOT = 1;
    private static final int SELECTOR_LINE = 2;
    private static final float SINE_30_DEGREES = 0.5f;
    private static final int[] STATE_SET_SELECTED = {16842913};
    private static final String TAG = "ClockView";
    private static int[] sSnapPrefer30sMap = new int[ImsReasonInfo.CODE_SIP_USER_REJECTED];
    private final IntHolder[] mAlpha;
    private final IntHolder[][] mAlphaSelector;
    private int mAmOrPm;
    private int mAmOrPmPressed;
    private int mAmPmCircleRadius;
    private float mAmPmCircleRadiusMultiplier;
    private int mAmPmSelectedColor;
    private final String[] mAmPmText;
    private int mAmPmTextColor;
    private int mAmPmUnselectedColor;
    private float mAmPmYCenter;
    private float[] mAnimationRadiusMultiplier;
    private float[] mCircleRadius;
    private float[] mCircleRadiusMultiplier;
    private final int[] mColor;
    private final int[][] mColorSelector;
    private int mDisabledAlpha;
    private int mHalfwayHypotenusePoint;
    private final String[] mHours12Texts;
    private final ArrayList<Animator> mHoursToMinutesAnims;
    private final String[] mInnerHours24Texts;
    private float[] mInnerTextGridHeights;
    private float[] mInnerTextGridWidths;
    private String[] mInnerTextHours;
    private float mInnerTextSize;
    private boolean mInputEnabled;
    private InvalidateUpdateListener mInvalidateUpdateListener;
    private boolean mIs24HourMode;
    private boolean mIsOnInnerCircle;
    private float mLeftIndicatorXCenter;
    private int[] mLineLength;
    private OnValueSelectedListener mListener;
    private int mMaxHypotenuseForOuterNumber;
    private int mMinHypotenuseForInnerNumber;
    private final ArrayList<Animator> mMinuteToHoursAnims;
    private final String[] mMinutesTexts;
    private float[] mNumbersRadiusMultiplier;
    private final String[] mOuterHours24Texts;
    private String[] mOuterTextHours;
    private String[] mOuterTextMinutes;
    private final Paint[] mPaint;
    private final Paint[] mPaintAmPmCircle;
    private final Paint mPaintAmPmText;
    private final Paint mPaintBackground;
    private final Paint mPaintCenter;
    private final Paint mPaintDebug;
    private final Paint mPaintDisabled;
    private final Paint[][] mPaintSelector;
    private RectF mRectF;
    private float mRightIndicatorXCenter;
    private int[] mSelectionDegrees;
    private int[] mSelectionRadius;
    private float mSelectionRadiusMultiplier;
    private boolean mShowHours;
    private float[][] mTextGridHeights;
    private float[][] mTextGridWidths;
    private float[] mTextSize;
    private float[] mTextSizeMultiplier;
    private AnimatorSet mTransition;
    private float mTransitionEndRadiusMultiplier;
    private float mTransitionMidRadiusMultiplier;
    private Typeface mTypeface;
    private int mXCenter;
    private int mYCenter;

    public interface OnValueSelectedListener {
        void onValueSelected(int i, int i2, boolean z);
    }

    static {
        preparePrefer30sMap();
    }

    private static void preparePrefer30sMap() {
        int snappedOutputDegrees = 0;
        int count = 1;
        int expectedCount = 8;
        for (int degrees = 0; degrees < 361; degrees++) {
            sSnapPrefer30sMap[degrees] = snappedOutputDegrees;
            if (count == expectedCount) {
                snappedOutputDegrees += 6;
                if (snappedOutputDegrees == 360) {
                    expectedCount = 7;
                } else if (snappedOutputDegrees % 30 == 0) {
                    expectedCount = 14;
                } else {
                    expectedCount = 4;
                }
                count = 1;
            } else {
                count++;
            }
        }
    }

    private static int snapPrefer30s(int degrees) {
        if (sSnapPrefer30sMap == null) {
            return -1;
        }
        return sSnapPrefer30sMap[degrees];
    }

    private static int snapOnly30s(int degrees, int forceHigherOrLower) {
        int floor = (degrees / 30) * 30;
        int ceiling = floor + 30;
        if (forceHigherOrLower == 1) {
            return ceiling;
        }
        if (forceHigherOrLower != -1) {
            return degrees - floor < ceiling - degrees ? floor : ceiling;
        }
        if (degrees == floor) {
            floor -= 30;
        }
        return floor;
    }

    public RadialTimePickerView(Context context, AttributeSet attrs) {
        this(context, attrs, 16843933);
    }

    public RadialTimePickerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        this.mHours12Texts = new String[12];
        this.mOuterHours24Texts = new String[12];
        this.mInnerHours24Texts = new String[12];
        this.mMinutesTexts = new String[12];
        this.mAmPmText = new String[2];
        this.mPaint = new Paint[2];
        this.mColor = new int[2];
        this.mAlpha = new IntHolder[2];
        this.mPaintCenter = new Paint();
        this.mPaintSelector = (Paint[][]) Array.newInstance(Paint.class, 2, 3);
        this.mColorSelector = (int[][]) Array.newInstance(Integer.TYPE, 2, 3);
        this.mAlphaSelector = (IntHolder[][]) Array.newInstance(IntHolder.class, 2, 3);
        this.mPaintAmPmText = new Paint();
        this.mPaintAmPmCircle = new Paint[2];
        this.mPaintBackground = new Paint();
        this.mPaintDisabled = new Paint();
        this.mPaintDebug = new Paint();
        this.mCircleRadius = new float[3];
        this.mTextSize = new float[2];
        this.mTextGridHeights = (float[][]) Array.newInstance(Float.TYPE, 2, 7);
        this.mTextGridWidths = (float[][]) Array.newInstance(Float.TYPE, 2, 7);
        this.mInnerTextGridHeights = new float[7];
        this.mInnerTextGridWidths = new float[7];
        this.mCircleRadiusMultiplier = new float[2];
        this.mNumbersRadiusMultiplier = new float[3];
        this.mTextSizeMultiplier = new float[3];
        this.mAnimationRadiusMultiplier = new float[3];
        this.mInvalidateUpdateListener = new InvalidateUpdateListener(this, (AnonymousClass1) null);
        this.mLineLength = new int[3];
        this.mSelectionRadius = new int[3];
        this.mSelectionDegrees = new int[3];
        this.mRectF = new RectF();
        this.mInputEnabled = true;
        this.mHoursToMinutesAnims = new ArrayList<>();
        this.mMinuteToHoursAnims = new ArrayList<>();
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(16842803, outValue, true);
        this.mDisabledAlpha = (int) ((outValue.getFloat() * 255.0f) + SINE_30_DEGREES);
        Resources res = getResources();
        TypedArray a = this.mContext.obtainStyledAttributes(attrs, R.styleable.TimePicker, defStyle, 0);
        ColorStateList amPmBackgroundColor = a.getColorStateList(7);
        amPmBackgroundColor = amPmBackgroundColor == null ? res.getColorStateList(R.color.timepicker_default_ampm_unselected_background_color_material) : amPmBackgroundColor;
        int amPmSelectedColor = a.getColor(12, res.getColor(R.color.timepicker_default_ampm_selected_background_color_material));
        ColorStateList amPmBackgroundColor2 = ColorStateList.addFirstIfMissing(amPmBackgroundColor, 16842913, amPmSelectedColor);
        this.mAmPmSelectedColor = amPmBackgroundColor2.getColorForState(STATE_SET_SELECTED, amPmSelectedColor);
        this.mAmPmUnselectedColor = amPmBackgroundColor2.getDefaultColor();
        this.mAmPmTextColor = a.getColor(6, res.getColor(R.color.timepicker_default_text_color_material));
        this.mTypeface = Typeface.create("sans-serif", 0);
        for (int i = 0; i < this.mAlpha.length; i++) {
            this.mAlpha[i] = new IntHolder(R.styleable.Theme_actionBarTheme);
        }
        for (int i2 = 0; i2 < this.mAlphaSelector.length; i2++) {
            for (int j = 0; j < this.mAlphaSelector[i2].length; j++) {
                this.mAlphaSelector[i2][j] = new IntHolder(R.styleable.Theme_actionBarTheme);
            }
        }
        int numbersTextColor = a.getColor(3, res.getColor(R.color.timepicker_default_text_color_material));
        this.mPaint[0] = new Paint();
        this.mPaint[0].setAntiAlias(true);
        this.mPaint[0].setTextAlign(Paint.Align.CENTER);
        this.mColor[0] = numbersTextColor;
        this.mPaint[1] = new Paint();
        this.mPaint[1].setAntiAlias(true);
        this.mPaint[1].setTextAlign(Paint.Align.CENTER);
        this.mColor[1] = numbersTextColor;
        this.mPaintCenter.setColor(numbersTextColor);
        this.mPaintCenter.setAntiAlias(true);
        this.mPaintCenter.setTextAlign(Paint.Align.CENTER);
        this.mPaintSelector[0][0] = new Paint();
        this.mPaintSelector[0][0].setAntiAlias(true);
        this.mColorSelector[0][0] = a.getColor(5, R.color.timepicker_default_selector_color_material);
        this.mPaintSelector[0][1] = new Paint();
        this.mPaintSelector[0][1].setAntiAlias(true);
        this.mColorSelector[0][1] = a.getColor(5, R.color.timepicker_default_selector_color_material);
        this.mPaintSelector[0][2] = new Paint();
        this.mPaintSelector[0][2].setAntiAlias(true);
        this.mPaintSelector[0][2].setStrokeWidth(2.0f);
        this.mColorSelector[0][2] = a.getColor(5, R.color.timepicker_default_selector_color_material);
        this.mPaintSelector[1][0] = new Paint();
        this.mPaintSelector[1][0].setAntiAlias(true);
        this.mColorSelector[1][0] = a.getColor(5, R.color.timepicker_default_selector_color_material);
        this.mPaintSelector[1][1] = new Paint();
        this.mPaintSelector[1][1].setAntiAlias(true);
        this.mColorSelector[1][1] = a.getColor(5, R.color.timepicker_default_selector_color_material);
        this.mPaintSelector[1][2] = new Paint();
        this.mPaintSelector[1][2].setAntiAlias(true);
        this.mPaintSelector[1][2].setStrokeWidth(2.0f);
        this.mColorSelector[1][2] = a.getColor(5, R.color.timepicker_default_selector_color_material);
        this.mPaintAmPmText.setColor(this.mAmPmTextColor);
        this.mPaintAmPmText.setTypeface(this.mTypeface);
        this.mPaintAmPmText.setAntiAlias(true);
        this.mPaintAmPmText.setTextAlign(Paint.Align.CENTER);
        this.mPaintAmPmCircle[0] = new Paint();
        this.mPaintAmPmCircle[0].setAntiAlias(true);
        this.mPaintAmPmCircle[1] = new Paint();
        this.mPaintAmPmCircle[1].setAntiAlias(true);
        this.mPaintBackground.setColor(a.getColor(4, res.getColor(R.color.timepicker_default_numbers_background_color_material)));
        this.mPaintBackground.setAntiAlias(true);
        this.mShowHours = true;
        this.mIs24HourMode = false;
        this.mAmOrPm = 0;
        this.mAmOrPmPressed = -1;
        initHoursAndMinutesText();
        initData();
        this.mTransitionMidRadiusMultiplier = Float.parseFloat(res.getString(R.string.timepicker_transition_mid_radius_multiplier));
        this.mTransitionEndRadiusMultiplier = Float.parseFloat(res.getString(R.string.timepicker_transition_end_radius_multiplier));
        this.mTextGridHeights[0] = new float[7];
        this.mTextGridHeights[1] = new float[7];
        this.mSelectionRadiusMultiplier = Float.parseFloat(res.getString(R.string.timepicker_selection_radius_multiplier));
        a.recycle();
        setOnTouchListener(this);
        setClickable(true);
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        int currentHour = calendar.get(11);
        int currentMinute = calendar.get(12);
        setCurrentHour(currentHour);
        setCurrentMinute(currentMinute);
        setHapticFeedbackEnabled(true);
    }

    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth = View.MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
        int measuredHeight = View.MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);
        int minDimension = Math.min(measuredWidth, measuredHeight);
        super.onMeasure(View.MeasureSpec.makeMeasureSpec(minDimension, widthMode), View.MeasureSpec.makeMeasureSpec(minDimension, heightMode));
    }

    public void initialize(int hour, int minute, boolean is24HourMode) {
        this.mIs24HourMode = is24HourMode;
        setCurrentHour(hour);
        setCurrentMinute(minute);
    }

    public void setCurrentItemShowing(int item, boolean animate) {
        switch (item) {
            case 0:
                showHours(animate);
                return;
            case 1:
                showMinutes(animate);
                return;
            default:
                Log.e(TAG, "ClockView does not support showing item " + item);
                return;
        }
    }

    public int getCurrentItemShowing() {
        return this.mShowHours ? 0 : 1;
    }

    public void setOnValueSelectedListener(OnValueSelectedListener listener) {
        this.mListener = listener;
    }

    public void setCurrentHour(int hour) {
        boolean z = true;
        int degrees = (hour % 12) * 30;
        this.mSelectionDegrees[0] = degrees;
        this.mSelectionDegrees[2] = degrees;
        this.mAmOrPm = (hour == 0 || hour % 24 < 12) ? 0 : 1;
        if (this.mIs24HourMode) {
            if (hour < 1 || hour > 12) {
                z = false;
            }
            this.mIsOnInnerCircle = z;
        } else {
            this.mIsOnInnerCircle = false;
        }
        initData();
        updateLayoutData();
        invalidate();
    }

    public int getCurrentHour() {
        int hour = (this.mSelectionDegrees[this.mIsOnInnerCircle ? (char) 2 : 0] / 30) % 12;
        if (this.mIs24HourMode) {
            if (this.mIsOnInnerCircle && hour == 0) {
                return 12;
            }
            if (this.mIsOnInnerCircle || hour == 0) {
                return hour;
            }
            return hour + 12;
        } else if (this.mAmOrPm == 1) {
            return hour + 12;
        } else {
            return hour;
        }
    }

    public void setCurrentMinute(int minute) {
        this.mSelectionDegrees[1] = (minute % 60) * 6;
        invalidate();
    }

    public int getCurrentMinute() {
        return this.mSelectionDegrees[1] / 6;
    }

    public void setAmOrPm(int val) {
        this.mAmOrPm = val % 2;
        invalidate();
    }

    public int getAmOrPm() {
        return this.mAmOrPm;
    }

    public void swapAmPm() {
        this.mAmOrPm = this.mAmOrPm == 0 ? 1 : 0;
        invalidate();
    }

    public void showHours(boolean animate) {
        if (!this.mShowHours) {
            this.mShowHours = true;
            if (animate) {
                startMinutesToHoursAnimation();
            }
            initData();
            updateLayoutData();
            invalidate();
        }
    }

    public void showMinutes(boolean animate) {
        if (this.mShowHours) {
            this.mShowHours = false;
            if (animate) {
                startHoursToMinutesAnimation();
            }
            initData();
            updateLayoutData();
            invalidate();
        }
    }

    private void initHoursAndMinutesText() {
        for (int i = 0; i < 12; i++) {
            this.mHours12Texts[i] = String.format("%d", Integer.valueOf(HOURS_NUMBERS[i]));
            this.mOuterHours24Texts[i] = String.format("%02d", Integer.valueOf(HOURS_NUMBERS_24[i]));
            this.mInnerHours24Texts[i] = String.format("%d", Integer.valueOf(HOURS_NUMBERS[i]));
            this.mMinutesTexts[i] = String.format("%02d", Integer.valueOf(MINUTES_NUMBERS[i]));
        }
        String[] amPmStrings = TimePickerClockDelegate.getAmPmStrings(this.mContext);
        this.mAmPmText[0] = amPmStrings[0];
        this.mAmPmText[1] = amPmStrings[1];
    }

    private void initData() {
        int i;
        int i2;
        int i3;
        int i4;
        int i5;
        int i6 = R.styleable.Theme_actionBarTheme;
        int i7 = 0;
        if (this.mIs24HourMode) {
            this.mOuterTextHours = this.mOuterHours24Texts;
            this.mInnerTextHours = this.mInnerHours24Texts;
        } else {
            this.mOuterTextHours = this.mHours12Texts;
            this.mInnerTextHours = null;
        }
        this.mOuterTextMinutes = this.mMinutesTexts;
        Resources res = getResources();
        if (!this.mShowHours) {
            this.mCircleRadiusMultiplier[1] = Float.parseFloat(res.getString(R.string.timepicker_circle_radius_multiplier));
            this.mNumbersRadiusMultiplier[1] = Float.parseFloat(res.getString(R.string.timepicker_numbers_radius_multiplier_normal));
            this.mTextSizeMultiplier[1] = Float.parseFloat(res.getString(R.string.timepicker_text_size_multiplier_normal));
        } else if (this.mIs24HourMode) {
            this.mCircleRadiusMultiplier[0] = Float.parseFloat(res.getString(R.string.timepicker_circle_radius_multiplier_24HourMode));
            this.mNumbersRadiusMultiplier[0] = Float.parseFloat(res.getString(R.string.timepicker_numbers_radius_multiplier_outer));
            this.mTextSizeMultiplier[0] = Float.parseFloat(res.getString(R.string.timepicker_text_size_multiplier_outer));
            this.mNumbersRadiusMultiplier[2] = Float.parseFloat(res.getString(R.string.timepicker_numbers_radius_multiplier_inner));
            this.mTextSizeMultiplier[2] = Float.parseFloat(res.getString(R.string.timepicker_text_size_multiplier_inner));
        } else {
            this.mCircleRadiusMultiplier[0] = Float.parseFloat(res.getString(R.string.timepicker_circle_radius_multiplier));
            this.mNumbersRadiusMultiplier[0] = Float.parseFloat(res.getString(R.string.timepicker_numbers_radius_multiplier_normal));
            this.mTextSizeMultiplier[0] = Float.parseFloat(res.getString(R.string.timepicker_text_size_multiplier_normal));
        }
        this.mAnimationRadiusMultiplier[0] = 1.0f;
        this.mAnimationRadiusMultiplier[2] = 1.0f;
        this.mAnimationRadiusMultiplier[1] = 1.0f;
        this.mAmPmCircleRadiusMultiplier = Float.parseFloat(res.getString(R.string.timepicker_ampm_circle_radius_multiplier));
        this.mAlpha[0].setValue(this.mShowHours ? 255 : 0);
        IntHolder intHolder = this.mAlpha[1];
        if (this.mShowHours) {
            i = 0;
        } else {
            i = 255;
        }
        intHolder.setValue(i);
        IntHolder intHolder2 = this.mAlphaSelector[0][0];
        if (this.mShowHours) {
            i2 = 60;
        } else {
            i2 = 0;
        }
        intHolder2.setValue(i2);
        IntHolder intHolder3 = this.mAlphaSelector[0][1];
        if (this.mShowHours) {
            i3 = 255;
        } else {
            i3 = 0;
        }
        intHolder3.setValue(i3);
        IntHolder intHolder4 = this.mAlphaSelector[0][2];
        if (this.mShowHours) {
            i4 = 60;
        } else {
            i4 = 0;
        }
        intHolder4.setValue(i4);
        IntHolder intHolder5 = this.mAlphaSelector[1][0];
        if (this.mShowHours) {
            i5 = 0;
        } else {
            i5 = 60;
        }
        intHolder5.setValue(i5);
        IntHolder intHolder6 = this.mAlphaSelector[1][1];
        if (this.mShowHours) {
            i6 = 0;
        }
        intHolder6.setValue(i6);
        IntHolder intHolder7 = this.mAlphaSelector[1][2];
        if (!this.mShowHours) {
            i7 = 60;
        }
        intHolder7.setValue(i7);
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        updateLayoutData();
    }

    private void updateLayoutData() {
        this.mXCenter = getWidth() / 2;
        this.mYCenter = getHeight() / 2;
        int min = Math.min(this.mXCenter, this.mYCenter);
        this.mCircleRadius[0] = ((float) min) * this.mCircleRadiusMultiplier[0];
        this.mCircleRadius[2] = ((float) min) * this.mCircleRadiusMultiplier[0];
        this.mCircleRadius[1] = ((float) min) * this.mCircleRadiusMultiplier[1];
        if (!this.mIs24HourMode) {
            this.mYCenter -= ((int) (this.mCircleRadius[0] * this.mAmPmCircleRadiusMultiplier)) / 2;
        }
        this.mMinHypotenuseForInnerNumber = ((int) (this.mCircleRadius[0] * this.mNumbersRadiusMultiplier[2])) - this.mSelectionRadius[0];
        this.mMaxHypotenuseForOuterNumber = ((int) (this.mCircleRadius[0] * this.mNumbersRadiusMultiplier[0])) + this.mSelectionRadius[0];
        this.mHalfwayHypotenusePoint = (int) (this.mCircleRadius[0] * ((this.mNumbersRadiusMultiplier[0] + this.mNumbersRadiusMultiplier[2]) / 2.0f));
        this.mTextSize[0] = this.mCircleRadius[0] * this.mTextSizeMultiplier[0];
        this.mTextSize[1] = this.mCircleRadius[1] * this.mTextSizeMultiplier[1];
        if (this.mIs24HourMode) {
            this.mInnerTextSize = this.mCircleRadius[0] * this.mTextSizeMultiplier[2];
        }
        calculateGridSizesHours();
        calculateGridSizesMinutes();
        this.mSelectionRadius[0] = (int) (this.mCircleRadius[0] * this.mSelectionRadiusMultiplier);
        this.mSelectionRadius[2] = this.mSelectionRadius[0];
        this.mSelectionRadius[1] = (int) (this.mCircleRadius[1] * this.mSelectionRadiusMultiplier);
        this.mAmPmCircleRadius = (int) (this.mCircleRadius[0] * this.mAmPmCircleRadiusMultiplier);
        this.mPaintAmPmText.setTextSize((float) ((this.mAmPmCircleRadius * 3) / 4));
        this.mAmPmYCenter = ((float) this.mYCenter) + this.mCircleRadius[0];
        this.mLeftIndicatorXCenter = (((float) this.mXCenter) - this.mCircleRadius[0]) + ((float) this.mAmPmCircleRadius);
        this.mRightIndicatorXCenter = (((float) this.mXCenter) + this.mCircleRadius[0]) - ((float) this.mAmPmCircleRadius);
    }

    public void onDraw(Canvas canvas) {
        if (!this.mInputEnabled) {
            canvas.saveLayerAlpha(0.0f, 0.0f, (float) getWidth(), (float) getHeight(), this.mDisabledAlpha);
        } else {
            canvas.save();
        }
        calculateGridSizesHours();
        calculateGridSizesMinutes();
        drawCircleBackground(canvas);
        drawSelector(canvas);
        drawTextElements(canvas, this.mTextSize[0], this.mTypeface, this.mOuterTextHours, this.mTextGridWidths[0], this.mTextGridHeights[0], this.mPaint[0], this.mColor[0], this.mAlpha[0].getValue());
        if (this.mIs24HourMode && this.mInnerTextHours != null) {
            drawTextElements(canvas, this.mInnerTextSize, this.mTypeface, this.mInnerTextHours, this.mInnerTextGridWidths, this.mInnerTextGridHeights, this.mPaint[0], this.mColor[0], this.mAlpha[0].getValue());
        }
        drawTextElements(canvas, this.mTextSize[1], this.mTypeface, this.mOuterTextMinutes, this.mTextGridWidths[1], this.mTextGridHeights[1], this.mPaint[1], this.mColor[1], this.mAlpha[1].getValue());
        drawCenter(canvas);
        if (!this.mIs24HourMode) {
            drawAmPm(canvas);
        }
        canvas.restore();
    }

    private void drawCircleBackground(Canvas canvas) {
        canvas.drawCircle((float) this.mXCenter, (float) this.mYCenter, this.mCircleRadius[0], this.mPaintBackground);
    }

    private void drawCenter(Canvas canvas) {
        canvas.drawCircle((float) this.mXCenter, (float) this.mYCenter, 2.0f, this.mPaintCenter);
    }

    private void drawSelector(Canvas canvas) {
        drawSelector(canvas, this.mIsOnInnerCircle ? 2 : 0);
        drawSelector(canvas, 1);
    }

    private void drawAmPm(Canvas canvas) {
        boolean isLayoutRtl = isLayoutRtl();
        int amColor = this.mAmPmUnselectedColor;
        int amAlpha = R.styleable.Theme_actionBarTheme;
        int pmColor = this.mAmPmUnselectedColor;
        int pmAlpha = R.styleable.Theme_actionBarTheme;
        if (this.mAmOrPm == 0) {
            amColor = this.mAmPmSelectedColor;
            amAlpha = 60;
        } else if (this.mAmOrPm == 1) {
            pmColor = this.mAmPmSelectedColor;
            pmAlpha = 60;
        }
        if (this.mAmOrPmPressed == 0) {
            amColor = this.mAmPmSelectedColor;
            amAlpha = R.styleable.Theme_actionBarTheme;
        } else if (this.mAmOrPmPressed == 1) {
            pmColor = this.mAmPmSelectedColor;
            pmAlpha = R.styleable.Theme_actionBarTheme;
        }
        this.mPaintAmPmCircle[0].setColor(amColor);
        this.mPaintAmPmCircle[0].setAlpha(getMultipliedAlpha(amColor, amAlpha));
        canvas.drawCircle(isLayoutRtl ? this.mRightIndicatorXCenter : this.mLeftIndicatorXCenter, this.mAmPmYCenter, (float) this.mAmPmCircleRadius, this.mPaintAmPmCircle[0]);
        this.mPaintAmPmCircle[1].setColor(pmColor);
        this.mPaintAmPmCircle[1].setAlpha(getMultipliedAlpha(pmColor, pmAlpha));
        canvas.drawCircle(isLayoutRtl ? this.mLeftIndicatorXCenter : this.mRightIndicatorXCenter, this.mAmPmYCenter, (float) this.mAmPmCircleRadius, this.mPaintAmPmCircle[1]);
        this.mPaintAmPmText.setColor(this.mAmPmTextColor);
        float textYCenter = this.mAmPmYCenter - ((float) (((int) (this.mPaintAmPmText.descent() + this.mPaintAmPmText.ascent())) / 2));
        canvas.drawText(isLayoutRtl ? this.mAmPmText[1] : this.mAmPmText[0], this.mLeftIndicatorXCenter, textYCenter, this.mPaintAmPmText);
        canvas.drawText(isLayoutRtl ? this.mAmPmText[0] : this.mAmPmText[1], this.mRightIndicatorXCenter, textYCenter, this.mPaintAmPmText);
    }

    private int getMultipliedAlpha(int argb, int alpha) {
        return (int) ((((double) Color.alpha(argb)) * (((double) alpha) / 255.0d)) + 0.5d);
    }

    private void drawSelector(Canvas canvas, int index) {
        this.mLineLength[index] = (int) (this.mCircleRadius[index] * this.mNumbersRadiusMultiplier[index] * this.mAnimationRadiusMultiplier[index]);
        double selectionRadians = Math.toRadians((double) this.mSelectionDegrees[index]);
        int pointX = this.mXCenter + ((int) (((double) this.mLineLength[index]) * Math.sin(selectionRadians)));
        int pointY = this.mYCenter - ((int) (((double) this.mLineLength[index]) * Math.cos(selectionRadians)));
        int color = this.mColorSelector[index % 2][0];
        int alpha = this.mAlphaSelector[index % 2][0].getValue();
        Paint paint = this.mPaintSelector[index % 2][0];
        paint.setColor(color);
        paint.setAlpha(getMultipliedAlpha(color, alpha));
        canvas.drawCircle((float) pointX, (float) pointY, (float) this.mSelectionRadius[index], paint);
        if (this.mSelectionDegrees[index] % 30 != 0) {
            int color2 = this.mColorSelector[index % 2][1];
            int alpha2 = this.mAlphaSelector[index % 2][1].getValue();
            Paint paint2 = this.mPaintSelector[index % 2][1];
            paint2.setColor(color2);
            paint2.setAlpha(getMultipliedAlpha(color2, alpha2));
            canvas.drawCircle((float) pointX, (float) pointY, (float) ((this.mSelectionRadius[index] * 2) / 7), paint2);
        } else {
            int lineLength = this.mLineLength[index] - this.mSelectionRadius[index];
            pointX = this.mXCenter + ((int) (((double) lineLength) * Math.sin(selectionRadians)));
            pointY = this.mYCenter - ((int) (((double) lineLength) * Math.cos(selectionRadians)));
        }
        int color3 = this.mColorSelector[index % 2][2];
        int alpha3 = this.mAlphaSelector[index % 2][2].getValue();
        Paint paint3 = this.mPaintSelector[index % 2][2];
        paint3.setColor(color3);
        paint3.setAlpha(getMultipliedAlpha(color3, alpha3));
        canvas.drawLine((float) this.mXCenter, (float) this.mYCenter, (float) pointX, (float) pointY, paint3);
    }

    private void drawDebug(Canvas canvas) {
        float outerRadius = this.mCircleRadius[0] * this.mNumbersRadiusMultiplier[0];
        canvas.drawCircle((float) this.mXCenter, (float) this.mYCenter, outerRadius, this.mPaintDebug);
        canvas.drawCircle((float) this.mXCenter, (float) this.mYCenter, this.mCircleRadius[0] * this.mNumbersRadiusMultiplier[2], this.mPaintDebug);
        canvas.drawCircle((float) this.mXCenter, (float) this.mYCenter, this.mCircleRadius[0], this.mPaintDebug);
        this.mRectF = new RectF(((float) this.mXCenter) - outerRadius, ((float) this.mYCenter) - outerRadius, ((float) this.mXCenter) + outerRadius, ((float) this.mYCenter) + outerRadius);
        canvas.drawRect(this.mRectF, this.mPaintDebug);
        this.mRectF.set(((float) this.mXCenter) - this.mCircleRadius[0], ((float) this.mYCenter) - this.mCircleRadius[0], ((float) this.mXCenter) + this.mCircleRadius[0], ((float) this.mYCenter) + this.mCircleRadius[0]);
        canvas.drawRect(this.mRectF, this.mPaintDebug);
        this.mRectF.set(0.0f, 0.0f, (float) getWidth(), (float) getHeight());
        canvas.drawRect(this.mRectF, this.mPaintDebug);
        String selected = String.format("%02d:%02d", Integer.valueOf(getCurrentHour()), Integer.valueOf(getCurrentMinute()));
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(-2, -2);
        TextView tv = new TextView(getContext());
        tv.setLayoutParams(lp);
        tv.setText(selected);
        tv.measure(0, 0);
        Paint paint = tv.getPaint();
        paint.setColor(DEBUG_TEXT_COLOR);
        int width = tv.getMeasuredWidth();
        float height = paint.descent() - paint.ascent();
        canvas.drawText(selected.toString(), (float) (this.mXCenter - (width / 2)), ((float) this.mYCenter) + (1.5f * height), paint);
    }

    private void calculateGridSizesHours() {
        calculateGridSizes(this.mPaint[0], this.mCircleRadius[0] * this.mNumbersRadiusMultiplier[0] * this.mAnimationRadiusMultiplier[0], (float) this.mXCenter, (float) this.mYCenter, this.mTextSize[0], this.mTextGridHeights[0], this.mTextGridWidths[0]);
        if (this.mIs24HourMode) {
            calculateGridSizes(this.mPaint[0], this.mCircleRadius[2] * this.mNumbersRadiusMultiplier[2] * this.mAnimationRadiusMultiplier[2], (float) this.mXCenter, (float) this.mYCenter, this.mInnerTextSize, this.mInnerTextGridHeights, this.mInnerTextGridWidths);
        }
    }

    private void calculateGridSizesMinutes() {
        calculateGridSizes(this.mPaint[1], this.mCircleRadius[1] * this.mNumbersRadiusMultiplier[1] * this.mAnimationRadiusMultiplier[1], (float) this.mXCenter, (float) this.mYCenter, this.mTextSize[1], this.mTextGridHeights[1], this.mTextGridWidths[1]);
    }

    private static void calculateGridSizes(Paint paint, float numbersRadius, float xCenter, float yCenter, float textSize, float[] textGridHeights, float[] textGridWidths) {
        float offset2 = numbersRadius * COSINE_30_DEGREES;
        float offset3 = numbersRadius * SINE_30_DEGREES;
        paint.setTextSize(textSize);
        float yCenter2 = yCenter - ((paint.descent() + paint.ascent()) / 2.0f);
        textGridHeights[0] = yCenter2 - numbersRadius;
        textGridWidths[0] = xCenter - numbersRadius;
        textGridHeights[1] = yCenter2 - offset2;
        textGridWidths[1] = xCenter - offset2;
        textGridHeights[2] = yCenter2 - offset3;
        textGridWidths[2] = xCenter - offset3;
        textGridHeights[3] = yCenter2;
        textGridWidths[3] = xCenter;
        textGridHeights[4] = yCenter2 + offset3;
        textGridWidths[4] = xCenter + offset3;
        textGridHeights[5] = yCenter2 + offset2;
        textGridWidths[5] = xCenter + offset2;
        textGridHeights[6] = yCenter2 + numbersRadius;
        textGridWidths[6] = xCenter + numbersRadius;
    }

    private void drawTextElements(Canvas canvas, float textSize, Typeface typeface, String[] texts, float[] textGridWidths, float[] textGridHeights, Paint paint, int color, int alpha) {
        paint.setTextSize(textSize);
        paint.setTypeface(typeface);
        paint.setColor(color);
        paint.setAlpha(getMultipliedAlpha(color, alpha));
        canvas.drawText(texts[0], textGridWidths[3], textGridHeights[0], paint);
        canvas.drawText(texts[1], textGridWidths[4], textGridHeights[1], paint);
        canvas.drawText(texts[2], textGridWidths[5], textGridHeights[2], paint);
        canvas.drawText(texts[3], textGridWidths[6], textGridHeights[3], paint);
        canvas.drawText(texts[4], textGridWidths[5], textGridHeights[4], paint);
        canvas.drawText(texts[5], textGridWidths[4], textGridHeights[5], paint);
        canvas.drawText(texts[6], textGridWidths[3], textGridHeights[6], paint);
        canvas.drawText(texts[7], textGridWidths[2], textGridHeights[5], paint);
        canvas.drawText(texts[8], textGridWidths[1], textGridHeights[4], paint);
        canvas.drawText(texts[9], textGridWidths[0], textGridHeights[3], paint);
        canvas.drawText(texts[10], textGridWidths[1], textGridHeights[2], paint);
        canvas.drawText(texts[11], textGridWidths[2], textGridHeights[1], paint);
    }

    private void setAnimationRadiusMultiplierHours(float animationRadiusMultiplier) {
        this.mAnimationRadiusMultiplier[0] = animationRadiusMultiplier;
        this.mAnimationRadiusMultiplier[2] = animationRadiusMultiplier;
    }

    private void setAnimationRadiusMultiplierMinutes(float animationRadiusMultiplier) {
        this.mAnimationRadiusMultiplier[1] = animationRadiusMultiplier;
    }

    private static ObjectAnimator getRadiusDisappearAnimator(Object target, String radiusPropertyName, InvalidateUpdateListener updateListener, float midRadiusMultiplier, float endRadiusMultiplier) {
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(target, PropertyValuesHolder.ofKeyframe(radiusPropertyName, Keyframe.ofFloat(0.0f, 1.0f), Keyframe.ofFloat(0.2f, midRadiusMultiplier), Keyframe.ofFloat(1.0f, endRadiusMultiplier))).setDuration((long) PhoneConstants.EVENT_SUBSCRIPTION_ACTIVATED);
        animator.addUpdateListener(updateListener);
        return animator;
    }

    private static ObjectAnimator getRadiusReappearAnimator(Object target, String radiusPropertyName, InvalidateUpdateListener updateListener, float midRadiusMultiplier, float endRadiusMultiplier) {
        int totalDuration = (int) (((float) PhoneConstants.EVENT_SUBSCRIPTION_ACTIVATED) * (1.0f + 0.25f));
        float delayPoint = (((float) PhoneConstants.EVENT_SUBSCRIPTION_ACTIVATED) * 0.25f) / ((float) totalDuration);
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(target, PropertyValuesHolder.ofKeyframe(radiusPropertyName, Keyframe.ofFloat(0.0f, endRadiusMultiplier), Keyframe.ofFloat(delayPoint, endRadiusMultiplier), Keyframe.ofFloat(1.0f - ((1.0f - delayPoint) * 0.2f), midRadiusMultiplier), Keyframe.ofFloat(1.0f, 1.0f))).setDuration((long) totalDuration);
        animator.addUpdateListener(updateListener);
        return animator;
    }

    private static ObjectAnimator getFadeOutAnimator(IntHolder target, int startAlpha, int endAlpha, InvalidateUpdateListener updateListener) {
        ObjectAnimator animator = ObjectAnimator.ofInt(target, "value", startAlpha, endAlpha);
        animator.setDuration((long) PhoneConstants.EVENT_SUBSCRIPTION_ACTIVATED);
        animator.addUpdateListener(updateListener);
        return animator;
    }

    private static ObjectAnimator getFadeInAnimator(IntHolder target, int startAlpha, int endAlpha, InvalidateUpdateListener updateListener) {
        int totalDuration = (int) (((float) PhoneConstants.EVENT_SUBSCRIPTION_ACTIVATED) * (1.0f + 0.25f));
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(target, PropertyValuesHolder.ofKeyframe("value", Keyframe.ofInt(0.0f, startAlpha), Keyframe.ofInt((((float) PhoneConstants.EVENT_SUBSCRIPTION_ACTIVATED) * 0.25f) / ((float) totalDuration), startAlpha), Keyframe.ofInt(1.0f, endAlpha))).setDuration((long) totalDuration);
        animator.addUpdateListener(updateListener);
        return animator;
    }

    private void startHoursToMinutesAnimation() {
        if (this.mHoursToMinutesAnims.size() == 0) {
            this.mHoursToMinutesAnims.add(getRadiusDisappearAnimator(this, "animationRadiusMultiplierHours", this.mInvalidateUpdateListener, this.mTransitionMidRadiusMultiplier, this.mTransitionEndRadiusMultiplier));
            this.mHoursToMinutesAnims.add(getFadeOutAnimator(this.mAlpha[0], R.styleable.Theme_actionBarTheme, 0, this.mInvalidateUpdateListener));
            this.mHoursToMinutesAnims.add(getFadeOutAnimator(this.mAlphaSelector[0][0], 60, 0, this.mInvalidateUpdateListener));
            this.mHoursToMinutesAnims.add(getFadeOutAnimator(this.mAlphaSelector[0][1], R.styleable.Theme_actionBarTheme, 0, this.mInvalidateUpdateListener));
            this.mHoursToMinutesAnims.add(getFadeOutAnimator(this.mAlphaSelector[0][2], 60, 0, this.mInvalidateUpdateListener));
            this.mHoursToMinutesAnims.add(getRadiusReappearAnimator(this, "animationRadiusMultiplierMinutes", this.mInvalidateUpdateListener, this.mTransitionMidRadiusMultiplier, this.mTransitionEndRadiusMultiplier));
            this.mHoursToMinutesAnims.add(getFadeInAnimator(this.mAlpha[1], 0, R.styleable.Theme_actionBarTheme, this.mInvalidateUpdateListener));
            this.mHoursToMinutesAnims.add(getFadeInAnimator(this.mAlphaSelector[1][0], 0, 60, this.mInvalidateUpdateListener));
            this.mHoursToMinutesAnims.add(getFadeInAnimator(this.mAlphaSelector[1][1], 0, R.styleable.Theme_actionBarTheme, this.mInvalidateUpdateListener));
            this.mHoursToMinutesAnims.add(getFadeInAnimator(this.mAlphaSelector[1][2], 0, 60, this.mInvalidateUpdateListener));
        }
        if (this.mTransition != null && this.mTransition.isRunning()) {
            this.mTransition.end();
        }
        this.mTransition = new AnimatorSet();
        this.mTransition.playTogether(this.mHoursToMinutesAnims);
        this.mTransition.start();
    }

    private void startMinutesToHoursAnimation() {
        if (this.mMinuteToHoursAnims.size() == 0) {
            this.mMinuteToHoursAnims.add(getRadiusDisappearAnimator(this, "animationRadiusMultiplierMinutes", this.mInvalidateUpdateListener, this.mTransitionMidRadiusMultiplier, this.mTransitionEndRadiusMultiplier));
            this.mMinuteToHoursAnims.add(getFadeOutAnimator(this.mAlpha[1], R.styleable.Theme_actionBarTheme, 0, this.mInvalidateUpdateListener));
            this.mMinuteToHoursAnims.add(getFadeOutAnimator(this.mAlphaSelector[1][0], 60, 0, this.mInvalidateUpdateListener));
            this.mMinuteToHoursAnims.add(getFadeOutAnimator(this.mAlphaSelector[1][1], R.styleable.Theme_actionBarTheme, 0, this.mInvalidateUpdateListener));
            this.mMinuteToHoursAnims.add(getFadeOutAnimator(this.mAlphaSelector[1][2], 60, 0, this.mInvalidateUpdateListener));
            this.mMinuteToHoursAnims.add(getRadiusReappearAnimator(this, "animationRadiusMultiplierHours", this.mInvalidateUpdateListener, this.mTransitionMidRadiusMultiplier, this.mTransitionEndRadiusMultiplier));
            this.mMinuteToHoursAnims.add(getFadeInAnimator(this.mAlpha[0], 0, R.styleable.Theme_actionBarTheme, this.mInvalidateUpdateListener));
            this.mMinuteToHoursAnims.add(getFadeInAnimator(this.mAlphaSelector[0][0], 0, 60, this.mInvalidateUpdateListener));
            this.mMinuteToHoursAnims.add(getFadeInAnimator(this.mAlphaSelector[0][1], 0, R.styleable.Theme_actionBarTheme, this.mInvalidateUpdateListener));
            this.mMinuteToHoursAnims.add(getFadeInAnimator(this.mAlphaSelector[0][2], 0, 60, this.mInvalidateUpdateListener));
        }
        if (this.mTransition != null && this.mTransition.isRunning()) {
            this.mTransition.end();
        }
        this.mTransition = new AnimatorSet();
        this.mTransition.playTogether(this.mMinuteToHoursAnims);
        this.mTransition.start();
    }

    private int getDegreesFromXY(float x, float y) {
        double hypotenuse = Math.sqrt((double) (((y - ((float) this.mYCenter)) * (y - ((float) this.mYCenter))) + ((x - ((float) this.mXCenter)) * (x - ((float) this.mXCenter)))));
        if (hypotenuse > ((double) this.mCircleRadius[0])) {
            return -1;
        }
        if (!this.mIs24HourMode || !this.mShowHours) {
            int index = this.mShowHours ? 0 : 1;
            if (((int) Math.abs(hypotenuse - ((double) (this.mCircleRadius[index] * this.mNumbersRadiusMultiplier[index])))) > ((int) (this.mCircleRadius[index] * (1.0f - this.mNumbersRadiusMultiplier[index])))) {
                return -1;
            }
        } else if (hypotenuse >= ((double) this.mMinHypotenuseForInnerNumber) && hypotenuse <= ((double) this.mHalfwayHypotenusePoint)) {
            this.mIsOnInnerCircle = true;
        } else if (hypotenuse > ((double) this.mMaxHypotenuseForOuterNumber) || hypotenuse < ((double) this.mHalfwayHypotenusePoint)) {
            return -1;
        } else {
            this.mIsOnInnerCircle = false;
        }
        double degrees = Math.toDegrees(Math.asin(((double) Math.abs(y - ((float) this.mYCenter))) / hypotenuse));
        boolean rightSide = x > ((float) this.mXCenter);
        boolean topSide = y < ((float) this.mYCenter);
        if (rightSide && topSide) {
            degrees = 90.0d - degrees;
        } else if (rightSide && !topSide) {
            degrees += 90.0d;
        } else if (!rightSide && !topSide) {
            degrees = 270.0d - degrees;
        } else if (!rightSide && topSide) {
            degrees += 270.0d;
        }
        return (int) degrees;
    }

    private int getIsTouchingAmOrPm(float x, float y) {
        int i = 0;
        boolean isLayoutRtl = isLayoutRtl();
        int squaredYDistance = (int) ((y - this.mAmPmYCenter) * (y - this.mAmPmYCenter));
        if (((int) Math.sqrt((double) (((x - this.mLeftIndicatorXCenter) * (x - this.mLeftIndicatorXCenter)) + ((float) squaredYDistance)))) <= this.mAmPmCircleRadius) {
            return isLayoutRtl ? 1 : 0;
        }
        if (((int) Math.sqrt((double) (((x - this.mRightIndicatorXCenter) * (x - this.mRightIndicatorXCenter)) + ((float) squaredYDistance)))) > this.mAmPmCircleRadius) {
            return -1;
        }
        if (!isLayoutRtl) {
            i = 1;
        }
        return i;
    }

    public boolean onTouch(View v, MotionEvent event) {
        if (!this.mInputEnabled) {
            return true;
        }
        float eventX = event.getX();
        float eventY = event.getY();
        boolean result = false;
        switch (event.getAction()) {
            case 0:
            case 2:
                this.mAmOrPmPressed = getIsTouchingAmOrPm(eventX, eventY);
                if (this.mAmOrPmPressed != -1) {
                    result = true;
                } else {
                    int degrees = getDegreesFromXY(eventX, eventY);
                    if (degrees != -1) {
                        int snapDegrees = (this.mShowHours ? snapOnly30s(degrees, 0) : snapPrefer30s(degrees)) % 360;
                        if (this.mShowHours) {
                            this.mSelectionDegrees[0] = snapDegrees;
                            this.mSelectionDegrees[2] = snapDegrees;
                        } else {
                            this.mSelectionDegrees[1] = snapDegrees;
                        }
                        performHapticFeedback(4);
                        if (this.mListener != null) {
                            if (this.mShowHours) {
                                this.mListener.onValueSelected(0, getCurrentHour(), false);
                            } else {
                                this.mListener.onValueSelected(1, getCurrentMinute(), false);
                            }
                        }
                        result = true;
                    }
                }
                invalidate();
                return result;
            case 1:
                this.mAmOrPmPressed = getIsTouchingAmOrPm(eventX, eventY);
                if (this.mAmOrPmPressed != -1) {
                    if (this.mAmOrPm != this.mAmOrPmPressed) {
                        swapAmPm();
                    }
                    this.mAmOrPmPressed = -1;
                    if (this.mListener != null) {
                        this.mListener.onValueSelected(3, getCurrentHour(), true);
                    }
                    result = true;
                } else {
                    int degrees2 = getDegreesFromXY(eventX, eventY);
                    if (degrees2 != -1) {
                        int snapDegrees2 = (this.mShowHours ? snapOnly30s(degrees2, 0) : snapPrefer30s(degrees2)) % 360;
                        if (this.mShowHours) {
                            this.mSelectionDegrees[0] = snapDegrees2;
                            this.mSelectionDegrees[2] = snapDegrees2;
                        } else {
                            this.mSelectionDegrees[1] = snapDegrees2;
                        }
                        if (this.mListener != null) {
                            if (this.mShowHours) {
                                this.mListener.onValueSelected(0, getCurrentHour(), true);
                            } else {
                                this.mListener.onValueSelected(1, getCurrentMinute(), true);
                            }
                        }
                        result = true;
                    }
                }
                if (!result) {
                    return result;
                }
                invalidate();
                return result;
            default:
                return false;
        }
    }

    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.addAction(4096);
        info.addAction(MtpConstants.RESPONSE_UNDEFINED);
    }

    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() != 32) {
            return super.dispatchPopulateAccessibilityEvent(event);
        }
        event.getText().clear();
        Time time = new Time();
        time.hour = getCurrentHour();
        time.minute = getCurrentMinute();
        long millis = time.normalize(true);
        int flags = 1;
        if (this.mIs24HourMode) {
            flags = 1 | 128;
        }
        event.getText().add(DateUtils.formatDateTime(getContext(), millis, flags));
        return true;
    }

    @SuppressLint({"NewApi"})
    public boolean performAccessibilityAction(int action, Bundle arguments) {
        int stepSize;
        int value;
        int maxValue;
        if (super.performAccessibilityAction(action, arguments)) {
            return true;
        }
        int changeMultiplier = 0;
        if (action == 4096) {
            changeMultiplier = 1;
        } else if (action == 8192) {
            changeMultiplier = -1;
        }
        if (changeMultiplier == 0) {
            return false;
        }
        if (this.mShowHours) {
            stepSize = 30;
            value = getCurrentHour() % 12;
        } else {
            stepSize = 6;
            value = getCurrentMinute();
        }
        int value2 = snapOnly30s(value * stepSize, changeMultiplier) / stepSize;
        int minValue = 0;
        if (!this.mShowHours) {
            maxValue = 55;
        } else if (this.mIs24HourMode) {
            maxValue = 23;
        } else {
            maxValue = 12;
            minValue = 1;
        }
        if (value2 > maxValue) {
            value2 = minValue;
        } else if (value2 < minValue) {
            value2 = maxValue;
        }
        if (this.mShowHours) {
            setCurrentHour(value2);
            if (this.mListener == null) {
                return true;
            }
            this.mListener.onValueSelected(0, value2, false);
            return true;
        }
        setCurrentMinute(value2);
        if (this.mListener == null) {
            return true;
        }
        this.mListener.onValueSelected(1, value2, false);
        return true;
    }

    public void setInputEnabled(boolean inputEnabled) {
        this.mInputEnabled = inputEnabled;
        invalidate();
    }

    /* access modifiers changed from: private */
    public static class IntHolder {
        private int mValue;

        public IntHolder(int value) {
            this.mValue = value;
        }

        public void setValue(int value) {
            this.mValue = value;
        }

        public int getValue() {
            return this.mValue;
        }
    }
}

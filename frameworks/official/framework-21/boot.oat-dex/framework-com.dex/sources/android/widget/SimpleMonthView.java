package android.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import com.android.internal.R;
import com.android.internal.widget.ExploreByTouchHelper;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

/* access modifiers changed from: package-private */
public class SimpleMonthView extends View {
    private static final int DAY_SEPARATOR_WIDTH = 1;
    private static final int DEFAULT_HEIGHT = 32;
    private static final int DEFAULT_NUM_DAYS = 7;
    private static final int DEFAULT_NUM_ROWS = 6;
    private static final int DEFAULT_SELECTED_DAY = -1;
    private static final int DEFAULT_WEEK_START = 1;
    private static final int MAX_NUM_ROWS = 6;
    private static final int MIN_HEIGHT = 10;
    private static final int SELECTED_CIRCLE_ALPHA = 60;
    private static final String TAG = "SimpleMonthView";
    private static float mScale = 0.0f;
    private final Calendar mCalendar;
    private SimpleDateFormat mDayFormatter;
    private final Calendar mDayLabelCalendar;
    private Paint mDayNumberDisabledPaint;
    private Paint mDayNumberPaint;
    private Paint mDayNumberSelectedPaint;
    private int mDayOfWeekStart;
    private String mDayOfWeekTypeface;
    private final int mDaySelectedCircleSize;
    private int mDisabledTextColor;
    private int mEnabledDayEnd;
    private int mEnabledDayStart;
    private final Formatter mFormatter;
    private boolean mHasToday;
    private boolean mLockAccessibilityDelegate;
    private final int mMiniDayNumberTextSize;
    private int mMonth;
    private Paint mMonthDayLabelPaint;
    private final int mMonthDayLabelTextSize;
    private final int mMonthHeaderSize;
    private final int mMonthLabelTextSize;
    private Paint mMonthTitlePaint;
    private String mMonthTitleTypeface;
    private int mNormalTextColor;
    private int mNumCells;
    private int mNumDays;
    private int mNumRows;
    private OnDayClickListener mOnDayClickListener;
    private int mPadding;
    private int mRowHeight;
    private int mSelectedDay;
    private int mSelectedDayColor;
    private final StringBuilder mStringBuilder;
    private int mToday;
    private final MonthViewTouchHelper mTouchHelper;
    private int mWeekStart;
    private int mWidth;
    private int mYear;

    public interface OnDayClickListener {
        void onDayClick(SimpleMonthView simpleMonthView, Calendar calendar);
    }

    public SimpleMonthView(Context context) {
        this(context, null);
    }

    public SimpleMonthView(Context context, AttributeSet attrs) {
        this(context, attrs, 16843612);
    }

    public SimpleMonthView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        this.mDayFormatter = new SimpleDateFormat("EEEEE", Locale.getDefault());
        this.mPadding = 0;
        this.mRowHeight = 32;
        this.mHasToday = false;
        this.mSelectedDay = -1;
        this.mToday = -1;
        this.mWeekStart = 1;
        this.mNumDays = 7;
        this.mNumCells = this.mNumDays;
        this.mDayOfWeekStart = 0;
        this.mEnabledDayStart = 1;
        this.mEnabledDayEnd = 31;
        this.mCalendar = Calendar.getInstance();
        this.mDayLabelCalendar = Calendar.getInstance();
        this.mNumRows = 6;
        Resources res = context.getResources();
        this.mDayOfWeekTypeface = res.getString(R.string.day_of_week_label_typeface);
        this.mMonthTitleTypeface = res.getString(R.string.sans_serif);
        this.mStringBuilder = new StringBuilder(50);
        this.mFormatter = new Formatter(this.mStringBuilder, Locale.getDefault());
        this.mMiniDayNumberTextSize = res.getDimensionPixelSize(R.dimen.datepicker_day_number_size);
        this.mMonthLabelTextSize = res.getDimensionPixelSize(R.dimen.datepicker_month_label_size);
        this.mMonthDayLabelTextSize = res.getDimensionPixelSize(R.dimen.datepicker_month_day_label_text_size);
        this.mMonthHeaderSize = res.getDimensionPixelOffset(R.dimen.datepicker_month_list_item_header_height);
        this.mDaySelectedCircleSize = res.getDimensionPixelSize(R.dimen.datepicker_day_number_select_circle_radius);
        this.mRowHeight = (res.getDimensionPixelOffset(R.dimen.datepicker_view_animator_height) - this.mMonthHeaderSize) / 6;
        this.mTouchHelper = new MonthViewTouchHelper(this);
        setAccessibilityDelegate(this.mTouchHelper);
        setImportantForAccessibility(1);
        this.mLockAccessibilityDelegate = true;
        initView();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.mDayFormatter = new SimpleDateFormat("EEEEE", newConfig.locale);
    }

    /* access modifiers changed from: package-private */
    public void setTextColor(ColorStateList colors) {
        Resources res = getContext().getResources();
        this.mNormalTextColor = colors.getColorForState(ENABLED_STATE_SET, res.getColor(R.color.datepicker_default_normal_text_color_holo_light));
        this.mMonthTitlePaint.setColor(this.mNormalTextColor);
        this.mMonthDayLabelPaint.setColor(this.mNormalTextColor);
        this.mDisabledTextColor = colors.getColorForState(EMPTY_STATE_SET, res.getColor(R.color.datepicker_default_disabled_text_color_holo_light));
        this.mDayNumberDisabledPaint.setColor(this.mDisabledTextColor);
        this.mSelectedDayColor = colors.getColorForState(ENABLED_SELECTED_STATE_SET, res.getColor(17170450));
        this.mDayNumberSelectedPaint.setColor(this.mSelectedDayColor);
        this.mDayNumberSelectedPaint.setAlpha(60);
    }

    @Override // android.view.View
    public void setAccessibilityDelegate(View.AccessibilityDelegate delegate) {
        if (!this.mLockAccessibilityDelegate) {
            super.setAccessibilityDelegate(delegate);
        }
    }

    public void setOnDayClickListener(OnDayClickListener listener) {
        this.mOnDayClickListener = listener;
    }

    @Override // android.view.View
    public boolean dispatchHoverEvent(MotionEvent event) {
        if (this.mTouchHelper.dispatchHoverEvent(event)) {
            return true;
        }
        return super.dispatchHoverEvent(event);
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case 1:
                int day = getDayFromLocation(event.getX(), event.getY());
                if (day < 0) {
                    return true;
                }
                onDayClick(day);
                return true;
            default:
                return true;
        }
    }

    private void initView() {
        this.mMonthTitlePaint = new Paint();
        this.mMonthTitlePaint.setAntiAlias(true);
        this.mMonthTitlePaint.setColor(this.mNormalTextColor);
        this.mMonthTitlePaint.setTextSize((float) this.mMonthLabelTextSize);
        this.mMonthTitlePaint.setTypeface(Typeface.create(this.mMonthTitleTypeface, 1));
        this.mMonthTitlePaint.setTextAlign(Paint.Align.CENTER);
        this.mMonthTitlePaint.setStyle(Paint.Style.FILL);
        this.mMonthTitlePaint.setFakeBoldText(true);
        this.mMonthDayLabelPaint = new Paint();
        this.mMonthDayLabelPaint.setAntiAlias(true);
        this.mMonthDayLabelPaint.setColor(this.mNormalTextColor);
        this.mMonthDayLabelPaint.setTextSize((float) this.mMonthDayLabelTextSize);
        this.mMonthDayLabelPaint.setTypeface(Typeface.create(this.mDayOfWeekTypeface, 0));
        this.mMonthDayLabelPaint.setTextAlign(Paint.Align.CENTER);
        this.mMonthDayLabelPaint.setStyle(Paint.Style.FILL);
        this.mMonthDayLabelPaint.setFakeBoldText(true);
        this.mDayNumberSelectedPaint = new Paint();
        this.mDayNumberSelectedPaint.setAntiAlias(true);
        this.mDayNumberSelectedPaint.setColor(this.mSelectedDayColor);
        this.mDayNumberSelectedPaint.setAlpha(60);
        this.mDayNumberSelectedPaint.setTextAlign(Paint.Align.CENTER);
        this.mDayNumberSelectedPaint.setStyle(Paint.Style.FILL);
        this.mDayNumberSelectedPaint.setFakeBoldText(true);
        this.mDayNumberPaint = new Paint();
        this.mDayNumberPaint.setAntiAlias(true);
        this.mDayNumberPaint.setTextSize((float) this.mMiniDayNumberTextSize);
        this.mDayNumberPaint.setTextAlign(Paint.Align.CENTER);
        this.mDayNumberPaint.setStyle(Paint.Style.FILL);
        this.mDayNumberPaint.setFakeBoldText(false);
        this.mDayNumberDisabledPaint = new Paint();
        this.mDayNumberDisabledPaint.setAntiAlias(true);
        this.mDayNumberDisabledPaint.setColor(this.mDisabledTextColor);
        this.mDayNumberDisabledPaint.setTextSize((float) this.mMiniDayNumberTextSize);
        this.mDayNumberDisabledPaint.setTextAlign(Paint.Align.CENTER);
        this.mDayNumberDisabledPaint.setStyle(Paint.Style.FILL);
        this.mDayNumberDisabledPaint.setFakeBoldText(false);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onDraw(Canvas canvas) {
        drawMonthTitle(canvas);
        drawWeekDayLabels(canvas);
        drawDays(canvas);
    }

    private static boolean isValidDayOfWeek(int day) {
        return day >= 1 && day <= 7;
    }

    private static boolean isValidMonth(int month) {
        return month >= 0 && month <= 11;
    }

    /* access modifiers changed from: package-private */
    public void setMonthParams(int selectedDay, int month, int year, int weekStart, int enabledDayStart, int enabledDayEnd) {
        if (this.mRowHeight < 10) {
            this.mRowHeight = 10;
        }
        this.mSelectedDay = selectedDay;
        if (isValidMonth(month)) {
            this.mMonth = month;
        }
        this.mYear = year;
        Time today = new Time(Time.getCurrentTimezone());
        today.setToNow();
        this.mHasToday = false;
        this.mToday = -1;
        this.mCalendar.set(2, this.mMonth);
        this.mCalendar.set(1, this.mYear);
        this.mCalendar.set(5, 1);
        this.mDayOfWeekStart = this.mCalendar.get(7);
        if (isValidDayOfWeek(weekStart)) {
            this.mWeekStart = weekStart;
        } else {
            this.mWeekStart = this.mCalendar.getFirstDayOfWeek();
        }
        if (enabledDayStart > 0 && enabledDayEnd < 32) {
            this.mEnabledDayStart = enabledDayStart;
        }
        if (enabledDayEnd > 0 && enabledDayEnd < 32 && enabledDayEnd >= enabledDayStart) {
            this.mEnabledDayEnd = enabledDayEnd;
        }
        this.mNumCells = getDaysInMonth(this.mMonth, this.mYear);
        for (int i = 0; i < this.mNumCells; i++) {
            int day = i + 1;
            if (sameDay(day, today)) {
                this.mHasToday = true;
                this.mToday = day;
            }
        }
        this.mNumRows = calculateNumRows();
        this.mTouchHelper.invalidateRoot();
    }

    private static int getDaysInMonth(int month, int year) {
        switch (month) {
            case 0:
            case 2:
            case 4:
            case 6:
            case 7:
            case 9:
            case 11:
                return 31;
            case 1:
                return year % 4 == 0 ? 29 : 28;
            case 3:
            case 5:
            case 8:
            case 10:
                return 30;
            default:
                throw new IllegalArgumentException("Invalid Month");
        }
    }

    public void reuse() {
        this.mNumRows = 6;
        requestLayout();
    }

    private int calculateNumRows() {
        int offset = findDayOffset();
        return ((this.mNumCells + offset) % this.mNumDays > 0 ? 1 : 0) + ((this.mNumCells + offset) / this.mNumDays);
    }

    private boolean sameDay(int day, Time today) {
        return this.mYear == today.year && this.mMonth == today.month && day == today.monthDay;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(View.MeasureSpec.getSize(widthMeasureSpec), (this.mRowHeight * this.mNumRows) + this.mMonthHeaderSize);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        this.mWidth = w;
        this.mTouchHelper.invalidateRoot();
    }

    private String getMonthAndYearString() {
        this.mStringBuilder.setLength(0);
        long millis = this.mCalendar.getTimeInMillis();
        return DateUtils.formatDateRange(getContext(), this.mFormatter, millis, millis, 52, Time.getCurrentTimezone()).toString();
    }

    private void drawMonthTitle(Canvas canvas) {
        canvas.drawText(getMonthAndYearString(), ((float) (this.mWidth + (this.mPadding * 2))) / 2.0f, ((float) (this.mMonthHeaderSize - this.mMonthDayLabelTextSize)) / 2.0f, this.mMonthTitlePaint);
    }

    private void drawWeekDayLabels(Canvas canvas) {
        int y = this.mMonthHeaderSize - (this.mMonthDayLabelTextSize / 2);
        int dayWidthHalf = (this.mWidth - (this.mPadding * 2)) / (this.mNumDays * 2);
        for (int i = 0; i < this.mNumDays; i++) {
            this.mDayLabelCalendar.set(7, (this.mWeekStart + i) % this.mNumDays);
            canvas.drawText(this.mDayFormatter.format(this.mDayLabelCalendar.getTime()), (float) ((((i * 2) + 1) * dayWidthHalf) + this.mPadding), (float) y, this.mMonthDayLabelPaint);
        }
    }

    private void drawDays(Canvas canvas) {
        int y = (((this.mRowHeight + this.mMiniDayNumberTextSize) / 2) - 1) + this.mMonthHeaderSize;
        int dayWidthHalf = (this.mWidth - (this.mPadding * 2)) / (this.mNumDays * 2);
        int j = findDayOffset();
        int day = 1;
        while (day <= this.mNumCells) {
            int x = (((j * 2) + 1) * dayWidthHalf) + this.mPadding;
            if (this.mSelectedDay == day) {
                canvas.drawCircle((float) x, (float) (y - (this.mMiniDayNumberTextSize / 3)), (float) this.mDaySelectedCircleSize, this.mDayNumberSelectedPaint);
            }
            if (!this.mHasToday || this.mToday != day) {
                this.mDayNumberPaint.setColor(this.mNormalTextColor);
            } else {
                this.mDayNumberPaint.setColor(this.mSelectedDayColor);
            }
            canvas.drawText(String.format("%d", Integer.valueOf(day)), (float) x, (float) y, (day < this.mEnabledDayStart || day > this.mEnabledDayEnd) ? this.mDayNumberDisabledPaint : this.mDayNumberPaint);
            j++;
            if (j == this.mNumDays) {
                j = 0;
                y += this.mRowHeight;
            }
            day++;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int findDayOffset() {
        return (this.mDayOfWeekStart < this.mWeekStart ? this.mDayOfWeekStart + this.mNumDays : this.mDayOfWeekStart) - this.mWeekStart;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getDayFromLocation(float x, float y) {
        int dayStart = this.mPadding;
        if (x < ((float) dayStart) || x > ((float) (this.mWidth - this.mPadding))) {
            return -1;
        }
        int day = (((int) (((x - ((float) dayStart)) * ((float) this.mNumDays)) / ((float) ((this.mWidth - dayStart) - this.mPadding)))) - findDayOffset()) + 1 + (this.mNumDays * (((int) (y - ((float) this.mMonthHeaderSize))) / this.mRowHeight));
        if (day < 1 || day > this.mNumCells) {
            return -1;
        }
        return day;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onDayClick(int day) {
        if (this.mOnDayClickListener != null) {
            Calendar date = Calendar.getInstance();
            date.set(this.mYear, this.mMonth, day);
            this.mOnDayClickListener.onDayClick(this, date);
        }
        this.mTouchHelper.sendEventForVirtualView(day, 1);
    }

    /* access modifiers changed from: package-private */
    public Calendar getAccessibilityFocus() {
        int day = this.mTouchHelper.getFocusedVirtualView();
        if (day < 0) {
            return null;
        }
        Calendar date = Calendar.getInstance();
        date.set(this.mYear, this.mMonth, day);
        return date;
    }

    @Override // android.view.View
    public void clearAccessibilityFocus() {
        this.mTouchHelper.clearFocusedVirtualView();
    }

    /* access modifiers changed from: package-private */
    public boolean restoreAccessibilityFocus(Calendar day) {
        if (day.get(1) != this.mYear || day.get(2) != this.mMonth || day.get(5) > this.mNumCells) {
            return false;
        }
        this.mTouchHelper.setFocusedVirtualView(day.get(5));
        return true;
    }

    /* access modifiers changed from: private */
    public class MonthViewTouchHelper extends ExploreByTouchHelper {
        private static final String DATE_FORMAT = "dd MMMM yyyy";
        private final Calendar mTempCalendar = Calendar.getInstance();
        private final Rect mTempRect = new Rect();

        public MonthViewTouchHelper(View host) {
            super(host);
        }

        public void setFocusedVirtualView(int virtualViewId) {
            getAccessibilityNodeProvider(SimpleMonthView.this).performAction(virtualViewId, 64, null);
        }

        public void clearFocusedVirtualView() {
            int focusedVirtualView = getFocusedVirtualView();
            if (focusedVirtualView != Integer.MIN_VALUE) {
                getAccessibilityNodeProvider(SimpleMonthView.this).performAction(focusedVirtualView, 128, null);
            }
        }

        /* access modifiers changed from: protected */
        @Override // com.android.internal.widget.ExploreByTouchHelper
        public int getVirtualViewAt(float x, float y) {
            int day = SimpleMonthView.this.getDayFromLocation(x, y);
            if (day >= 0) {
                return day;
            }
            return Integer.MIN_VALUE;
        }

        /* access modifiers changed from: protected */
        @Override // com.android.internal.widget.ExploreByTouchHelper
        public void getVisibleVirtualViews(List<Integer> virtualViewIds) {
            for (int day = 1; day <= SimpleMonthView.this.mNumCells; day++) {
                virtualViewIds.add(Integer.valueOf(day));
            }
        }

        /* access modifiers changed from: protected */
        @Override // com.android.internal.widget.ExploreByTouchHelper
        public void onPopulateEventForVirtualView(int virtualViewId, AccessibilityEvent event) {
            event.setContentDescription(getItemDescription(virtualViewId));
        }

        /* access modifiers changed from: protected */
        @Override // com.android.internal.widget.ExploreByTouchHelper
        public void onPopulateNodeForVirtualView(int virtualViewId, AccessibilityNodeInfo node) {
            getItemBounds(virtualViewId, this.mTempRect);
            node.setContentDescription(getItemDescription(virtualViewId));
            node.setBoundsInParent(this.mTempRect);
            node.addAction(16);
            if (virtualViewId == SimpleMonthView.this.mSelectedDay) {
                node.setSelected(true);
            }
        }

        /* access modifiers changed from: protected */
        @Override // com.android.internal.widget.ExploreByTouchHelper
        public boolean onPerformActionForVirtualView(int virtualViewId, int action, Bundle arguments) {
            switch (action) {
                case 16:
                    SimpleMonthView.this.onDayClick(virtualViewId);
                    return true;
                default:
                    return false;
            }
        }

        private void getItemBounds(int day, Rect rect) {
            int offsetX = SimpleMonthView.this.mPadding;
            int offsetY = SimpleMonthView.this.mMonthHeaderSize;
            int cellHeight = SimpleMonthView.this.mRowHeight;
            int cellWidth = (SimpleMonthView.this.mWidth - (SimpleMonthView.this.mPadding * 2)) / SimpleMonthView.this.mNumDays;
            int index = (day - 1) + SimpleMonthView.this.findDayOffset();
            int row = index / SimpleMonthView.this.mNumDays;
            int x = offsetX + ((index % SimpleMonthView.this.mNumDays) * cellWidth);
            int y = offsetY + (row * cellHeight);
            rect.set(x, y, x + cellWidth, y + cellHeight);
        }

        private CharSequence getItemDescription(int day) {
            this.mTempCalendar.set(SimpleMonthView.this.mYear, SimpleMonthView.this.mMonth, day);
            CharSequence date = DateFormat.format(DATE_FORMAT, this.mTempCalendar.getTimeInMillis());
            if (day != SimpleMonthView.this.mSelectedDay) {
                return date;
            }
            return SimpleMonthView.this.getContext().getString(R.string.item_is_selected, date);
        }
    }
}

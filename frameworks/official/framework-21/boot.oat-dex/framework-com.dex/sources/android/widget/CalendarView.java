package android.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.ProxyInfo;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.AbsListView;
import com.android.internal.R;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import libcore.icu.LocaleData;

public class CalendarView extends FrameLayout {
    private static final String LOG_TAG = CalendarView.class.getSimpleName();
    private CalendarViewDelegate mDelegate;

    /* access modifiers changed from: private */
    public interface CalendarViewDelegate {
        long getDate();

        int getDateTextAppearance();

        int getFirstDayOfWeek();

        int getFocusedMonthDateColor();

        long getMaxDate();

        long getMinDate();

        Drawable getSelectedDateVerticalBar();

        int getSelectedWeekBackgroundColor();

        boolean getShowWeekNumber();

        int getShownWeekCount();

        int getUnfocusedMonthDateColor();

        int getWeekDayTextAppearance();

        int getWeekNumberColor();

        int getWeekSeparatorLineColor();

        boolean isEnabled();

        void onConfigurationChanged(Configuration configuration);

        void onInitializeAccessibilityEvent(AccessibilityEvent accessibilityEvent);

        void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo);

        void setDate(long j);

        void setDate(long j, boolean z, boolean z2);

        void setDateTextAppearance(int i);

        void setEnabled(boolean z);

        void setFirstDayOfWeek(int i);

        void setFocusedMonthDateColor(int i);

        void setMaxDate(long j);

        void setMinDate(long j);

        void setOnDateChangeListener(OnDateChangeListener onDateChangeListener);

        void setSelectedDateVerticalBar(int i);

        void setSelectedDateVerticalBar(Drawable drawable);

        void setSelectedWeekBackgroundColor(int i);

        void setShowWeekNumber(boolean z);

        void setShownWeekCount(int i);

        void setUnfocusedMonthDateColor(int i);

        void setWeekDayTextAppearance(int i);

        void setWeekNumberColor(int i);

        void setWeekSeparatorLineColor(int i);
    }

    public interface OnDateChangeListener {
        void onSelectedDayChange(CalendarView calendarView, int i, int i2, int i3);
    }

    public CalendarView(Context context) {
        this(context, null);
    }

    public CalendarView(Context context, AttributeSet attrs) {
        this(context, attrs, 16843613);
    }

    public CalendarView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CalendarView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mDelegate = new LegacyCalendarViewDelegate(this, context, attrs, defStyleAttr, defStyleRes);
    }

    public void setShownWeekCount(int count) {
        this.mDelegate.setShownWeekCount(count);
    }

    public int getShownWeekCount() {
        return this.mDelegate.getShownWeekCount();
    }

    public void setSelectedWeekBackgroundColor(int color) {
        this.mDelegate.setSelectedWeekBackgroundColor(color);
    }

    public int getSelectedWeekBackgroundColor() {
        return this.mDelegate.getSelectedWeekBackgroundColor();
    }

    public void setFocusedMonthDateColor(int color) {
        this.mDelegate.setFocusedMonthDateColor(color);
    }

    public int getFocusedMonthDateColor() {
        return this.mDelegate.getFocusedMonthDateColor();
    }

    public void setUnfocusedMonthDateColor(int color) {
        this.mDelegate.setUnfocusedMonthDateColor(color);
    }

    public int getUnfocusedMonthDateColor() {
        return this.mDelegate.getUnfocusedMonthDateColor();
    }

    public void setWeekNumberColor(int color) {
        this.mDelegate.setWeekNumberColor(color);
    }

    public int getWeekNumberColor() {
        return this.mDelegate.getWeekNumberColor();
    }

    public void setWeekSeparatorLineColor(int color) {
        this.mDelegate.setWeekSeparatorLineColor(color);
    }

    public int getWeekSeparatorLineColor() {
        return this.mDelegate.getWeekSeparatorLineColor();
    }

    public void setSelectedDateVerticalBar(int resourceId) {
        this.mDelegate.setSelectedDateVerticalBar(resourceId);
    }

    public void setSelectedDateVerticalBar(Drawable drawable) {
        this.mDelegate.setSelectedDateVerticalBar(drawable);
    }

    public Drawable getSelectedDateVerticalBar() {
        return this.mDelegate.getSelectedDateVerticalBar();
    }

    public void setWeekDayTextAppearance(int resourceId) {
        this.mDelegate.setWeekDayTextAppearance(resourceId);
    }

    public int getWeekDayTextAppearance() {
        return this.mDelegate.getWeekDayTextAppearance();
    }

    public void setDateTextAppearance(int resourceId) {
        this.mDelegate.setDateTextAppearance(resourceId);
    }

    public int getDateTextAppearance() {
        return this.mDelegate.getDateTextAppearance();
    }

    @Override // android.view.View
    public void setEnabled(boolean enabled) {
        this.mDelegate.setEnabled(enabled);
    }

    @Override // android.view.View
    public boolean isEnabled() {
        return this.mDelegate.isEnabled();
    }

    public long getMinDate() {
        return this.mDelegate.getMinDate();
    }

    public void setMinDate(long minDate) {
        this.mDelegate.setMinDate(minDate);
    }

    public long getMaxDate() {
        return this.mDelegate.getMaxDate();
    }

    public void setMaxDate(long maxDate) {
        this.mDelegate.setMaxDate(maxDate);
    }

    public void setShowWeekNumber(boolean showWeekNumber) {
        this.mDelegate.setShowWeekNumber(showWeekNumber);
    }

    public boolean getShowWeekNumber() {
        return this.mDelegate.getShowWeekNumber();
    }

    public int getFirstDayOfWeek() {
        return this.mDelegate.getFirstDayOfWeek();
    }

    public void setFirstDayOfWeek(int firstDayOfWeek) {
        this.mDelegate.setFirstDayOfWeek(firstDayOfWeek);
    }

    public void setOnDateChangeListener(OnDateChangeListener listener) {
        this.mDelegate.setOnDateChangeListener(listener);
    }

    public long getDate() {
        return this.mDelegate.getDate();
    }

    public void setDate(long date) {
        this.mDelegate.setDate(date);
    }

    public void setDate(long date, boolean animate, boolean center) {
        this.mDelegate.setDate(date, animate, center);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.mDelegate.onConfigurationChanged(newConfig);
    }

    @Override // android.widget.FrameLayout, android.view.View
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        this.mDelegate.onInitializeAccessibilityEvent(event);
    }

    @Override // android.widget.FrameLayout, android.view.View
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        this.mDelegate.onInitializeAccessibilityNodeInfo(info);
    }

    /* access modifiers changed from: package-private */
    public static abstract class AbstractCalendarViewDelegate implements CalendarViewDelegate {
        protected Context mContext;
        protected Locale mCurrentLocale;
        protected CalendarView mDelegator;

        AbstractCalendarViewDelegate(CalendarView delegator, Context context) {
            this.mDelegator = delegator;
            this.mContext = context;
            setCurrentLocale(Locale.getDefault());
        }

        /* access modifiers changed from: protected */
        public void setCurrentLocale(Locale locale) {
            if (!locale.equals(this.mCurrentLocale)) {
                this.mCurrentLocale = locale;
            }
        }
    }

    /* access modifiers changed from: private */
    public static class LegacyCalendarViewDelegate extends AbstractCalendarViewDelegate {
        private static final int ADJUSTMENT_SCROLL_DURATION = 500;
        private static final String DATE_FORMAT = "MM/dd/yyyy";
        private static final int DAYS_PER_WEEK = 7;
        private static final int DEFAULT_DATE_TEXT_SIZE = 14;
        private static final String DEFAULT_MAX_DATE = "01/01/2100";
        private static final String DEFAULT_MIN_DATE = "01/01/1900";
        private static final int DEFAULT_SHOWN_WEEK_COUNT = 6;
        private static final boolean DEFAULT_SHOW_WEEK_NUMBER = true;
        private static final int DEFAULT_WEEK_DAY_TEXT_APPEARANCE_RES_ID = -1;
        private static final int GOTO_SCROLL_DURATION = 1000;
        private static final long MILLIS_IN_DAY = 86400000;
        private static final long MILLIS_IN_WEEK = 604800000;
        private static final int SCROLL_CHANGE_DELAY = 40;
        private static final int SCROLL_HYST_WEEKS = 2;
        private static final int UNSCALED_BOTTOM_BUFFER = 20;
        private static final int UNSCALED_LIST_SCROLL_TOP_OFFSET = 2;
        private static final int UNSCALED_SELECTED_DATE_VERTICAL_BAR_WIDTH = 6;
        private static final int UNSCALED_WEEK_MIN_VISIBLE_HEIGHT = 12;
        private static final int UNSCALED_WEEK_SEPARATOR_LINE_WIDTH = 1;
        private WeeksAdapter mAdapter;
        private int mBottomBuffer = 20;
        private int mCurrentMonthDisplayed = -1;
        private int mCurrentScrollState = 0;
        private final DateFormat mDateFormat = new SimpleDateFormat(DATE_FORMAT);
        private int mDateTextAppearanceResId;
        private int mDateTextSize;
        private String[] mDayLabels;
        private ViewGroup mDayNamesHeader;
        private int mDaysPerWeek = 7;
        private Calendar mFirstDayOfMonth;
        private int mFirstDayOfWeek;
        private int mFocusedMonthDateColor;
        private float mFriction = 0.05f;
        private boolean mIsScrollingUp = false;
        private int mListScrollTopOffset = 2;
        private ListView mListView;
        private Calendar mMaxDate;
        private Calendar mMinDate;
        private TextView mMonthName;
        private OnDateChangeListener mOnDateChangeListener;
        private long mPreviousScrollPosition;
        private int mPreviousScrollState = 0;
        private ScrollStateRunnable mScrollStateChangedRunnable = new ScrollStateRunnable();
        private Drawable mSelectedDateVerticalBar;
        private final int mSelectedDateVerticalBarWidth;
        private int mSelectedWeekBackgroundColor;
        private boolean mShowWeekNumber;
        private int mShownWeekCount;
        private Calendar mTempDate;
        private int mUnfocusedMonthDateColor;
        private float mVelocityScale = 0.333f;
        private int mWeekDayTextAppearanceResId;
        private int mWeekMinVisibleHeight = 12;
        private int mWeekNumberColor;
        private int mWeekSeparatorLineColor;
        private final int mWeekSeperatorLineWidth;

        LegacyCalendarViewDelegate(CalendarView delegator, Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(delegator, context);
            setCurrentLocale(Locale.getDefault());
            TypedArray attributesArray = context.obtainStyledAttributes(attrs, R.styleable.CalendarView, defStyleAttr, defStyleRes);
            this.mShowWeekNumber = attributesArray.getBoolean(1, true);
            this.mFirstDayOfWeek = attributesArray.getInt(0, LocaleData.get(Locale.getDefault()).firstDayOfWeek.intValue());
            String minDate = attributesArray.getString(2);
            if (TextUtils.isEmpty(minDate) || !parseDate(minDate, this.mMinDate)) {
                parseDate(DEFAULT_MIN_DATE, this.mMinDate);
            }
            String maxDate = attributesArray.getString(3);
            if (TextUtils.isEmpty(maxDate) || !parseDate(maxDate, this.mMaxDate)) {
                parseDate(DEFAULT_MAX_DATE, this.mMaxDate);
            }
            if (this.mMaxDate.before(this.mMinDate)) {
                throw new IllegalArgumentException("Max date cannot be before min date.");
            }
            this.mShownWeekCount = attributesArray.getInt(4, 6);
            this.mSelectedWeekBackgroundColor = attributesArray.getColor(5, 0);
            this.mFocusedMonthDateColor = attributesArray.getColor(6, 0);
            this.mUnfocusedMonthDateColor = attributesArray.getColor(7, 0);
            this.mWeekSeparatorLineColor = attributesArray.getColor(9, 0);
            this.mWeekNumberColor = attributesArray.getColor(8, 0);
            this.mSelectedDateVerticalBar = attributesArray.getDrawable(10);
            this.mDateTextAppearanceResId = attributesArray.getResourceId(12, 16973894);
            updateDateTextSize();
            this.mWeekDayTextAppearanceResId = attributesArray.getResourceId(11, -1);
            attributesArray.recycle();
            DisplayMetrics displayMetrics = this.mDelegator.getResources().getDisplayMetrics();
            this.mWeekMinVisibleHeight = (int) TypedValue.applyDimension(1, 12.0f, displayMetrics);
            this.mListScrollTopOffset = (int) TypedValue.applyDimension(1, 2.0f, displayMetrics);
            this.mBottomBuffer = (int) TypedValue.applyDimension(1, 20.0f, displayMetrics);
            this.mSelectedDateVerticalBarWidth = (int) TypedValue.applyDimension(1, 6.0f, displayMetrics);
            this.mWeekSeperatorLineWidth = (int) TypedValue.applyDimension(1, 1.0f, displayMetrics);
            View content = ((LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.calendar_view, (ViewGroup) null, false);
            this.mDelegator.addView(content);
            this.mListView = (ListView) this.mDelegator.findViewById(16908298);
            this.mDayNamesHeader = (ViewGroup) content.findViewById(R.id.day_names);
            this.mMonthName = (TextView) content.findViewById(R.id.month_name);
            setUpHeader();
            setUpListView();
            setUpAdapter();
            this.mTempDate.setTimeInMillis(System.currentTimeMillis());
            if (this.mTempDate.before(this.mMinDate)) {
                goTo(this.mMinDate, false, true, true);
            } else if (this.mMaxDate.before(this.mTempDate)) {
                goTo(this.mMaxDate, false, true, true);
            } else {
                goTo(this.mTempDate, false, true, true);
            }
            this.mDelegator.invalidate();
        }

        @Override // android.widget.CalendarView.CalendarViewDelegate
        public void setShownWeekCount(int count) {
            if (this.mShownWeekCount != count) {
                this.mShownWeekCount = count;
                this.mDelegator.invalidate();
            }
        }

        @Override // android.widget.CalendarView.CalendarViewDelegate
        public int getShownWeekCount() {
            return this.mShownWeekCount;
        }

        @Override // android.widget.CalendarView.CalendarViewDelegate
        public void setSelectedWeekBackgroundColor(int color) {
            if (this.mSelectedWeekBackgroundColor != color) {
                this.mSelectedWeekBackgroundColor = color;
                int childCount = this.mListView.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    WeekView weekView = (WeekView) this.mListView.getChildAt(i);
                    if (weekView.mHasSelectedDay) {
                        weekView.invalidate();
                    }
                }
            }
        }

        @Override // android.widget.CalendarView.CalendarViewDelegate
        public int getSelectedWeekBackgroundColor() {
            return this.mSelectedWeekBackgroundColor;
        }

        @Override // android.widget.CalendarView.CalendarViewDelegate
        public void setFocusedMonthDateColor(int color) {
            if (this.mFocusedMonthDateColor != color) {
                this.mFocusedMonthDateColor = color;
                int childCount = this.mListView.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    WeekView weekView = (WeekView) this.mListView.getChildAt(i);
                    if (weekView.mHasFocusedDay) {
                        weekView.invalidate();
                    }
                }
            }
        }

        @Override // android.widget.CalendarView.CalendarViewDelegate
        public int getFocusedMonthDateColor() {
            return this.mFocusedMonthDateColor;
        }

        @Override // android.widget.CalendarView.CalendarViewDelegate
        public void setUnfocusedMonthDateColor(int color) {
            if (this.mUnfocusedMonthDateColor != color) {
                this.mUnfocusedMonthDateColor = color;
                int childCount = this.mListView.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    WeekView weekView = (WeekView) this.mListView.getChildAt(i);
                    if (weekView.mHasUnfocusedDay) {
                        weekView.invalidate();
                    }
                }
            }
        }

        @Override // android.widget.CalendarView.CalendarViewDelegate
        public int getUnfocusedMonthDateColor() {
            return this.mFocusedMonthDateColor;
        }

        @Override // android.widget.CalendarView.CalendarViewDelegate
        public void setWeekNumberColor(int color) {
            if (this.mWeekNumberColor != color) {
                this.mWeekNumberColor = color;
                if (this.mShowWeekNumber) {
                    invalidateAllWeekViews();
                }
            }
        }

        @Override // android.widget.CalendarView.CalendarViewDelegate
        public int getWeekNumberColor() {
            return this.mWeekNumberColor;
        }

        @Override // android.widget.CalendarView.CalendarViewDelegate
        public void setWeekSeparatorLineColor(int color) {
            if (this.mWeekSeparatorLineColor != color) {
                this.mWeekSeparatorLineColor = color;
                invalidateAllWeekViews();
            }
        }

        @Override // android.widget.CalendarView.CalendarViewDelegate
        public int getWeekSeparatorLineColor() {
            return this.mWeekSeparatorLineColor;
        }

        @Override // android.widget.CalendarView.CalendarViewDelegate
        public void setSelectedDateVerticalBar(int resourceId) {
            setSelectedDateVerticalBar(this.mDelegator.getContext().getDrawable(resourceId));
        }

        @Override // android.widget.CalendarView.CalendarViewDelegate
        public void setSelectedDateVerticalBar(Drawable drawable) {
            if (this.mSelectedDateVerticalBar != drawable) {
                this.mSelectedDateVerticalBar = drawable;
                int childCount = this.mListView.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    WeekView weekView = (WeekView) this.mListView.getChildAt(i);
                    if (weekView.mHasSelectedDay) {
                        weekView.invalidate();
                    }
                }
            }
        }

        @Override // android.widget.CalendarView.CalendarViewDelegate
        public Drawable getSelectedDateVerticalBar() {
            return this.mSelectedDateVerticalBar;
        }

        @Override // android.widget.CalendarView.CalendarViewDelegate
        public void setWeekDayTextAppearance(int resourceId) {
            if (this.mWeekDayTextAppearanceResId != resourceId) {
                this.mWeekDayTextAppearanceResId = resourceId;
                setUpHeader();
            }
        }

        @Override // android.widget.CalendarView.CalendarViewDelegate
        public int getWeekDayTextAppearance() {
            return this.mWeekDayTextAppearanceResId;
        }

        @Override // android.widget.CalendarView.CalendarViewDelegate
        public void setDateTextAppearance(int resourceId) {
            if (this.mDateTextAppearanceResId != resourceId) {
                this.mDateTextAppearanceResId = resourceId;
                updateDateTextSize();
                invalidateAllWeekViews();
            }
        }

        @Override // android.widget.CalendarView.CalendarViewDelegate
        public int getDateTextAppearance() {
            return this.mDateTextAppearanceResId;
        }

        @Override // android.widget.CalendarView.CalendarViewDelegate
        public void setEnabled(boolean enabled) {
            this.mListView.setEnabled(enabled);
        }

        @Override // android.widget.CalendarView.CalendarViewDelegate
        public boolean isEnabled() {
            return this.mListView.isEnabled();
        }

        @Override // android.widget.CalendarView.CalendarViewDelegate
        public void setMinDate(long minDate) {
            this.mTempDate.setTimeInMillis(minDate);
            if (!isSameDate(this.mTempDate, this.mMinDate)) {
                this.mMinDate.setTimeInMillis(minDate);
                Calendar date = this.mAdapter.mSelectedDate;
                if (date.before(this.mMinDate)) {
                    this.mAdapter.setSelectedDay(this.mMinDate);
                }
                this.mAdapter.init();
                if (date.before(this.mMinDate)) {
                    setDate(this.mTempDate.getTimeInMillis());
                } else {
                    goTo(date, false, true, false);
                }
            }
        }

        @Override // android.widget.CalendarView.CalendarViewDelegate
        public long getMinDate() {
            return this.mMinDate.getTimeInMillis();
        }

        @Override // android.widget.CalendarView.CalendarViewDelegate
        public void setMaxDate(long maxDate) {
            this.mTempDate.setTimeInMillis(maxDate);
            if (!isSameDate(this.mTempDate, this.mMaxDate)) {
                this.mMaxDate.setTimeInMillis(maxDate);
                this.mAdapter.init();
                Calendar date = this.mAdapter.mSelectedDate;
                if (date.after(this.mMaxDate)) {
                    setDate(this.mMaxDate.getTimeInMillis());
                } else {
                    goTo(date, false, true, false);
                }
            }
        }

        @Override // android.widget.CalendarView.CalendarViewDelegate
        public long getMaxDate() {
            return this.mMaxDate.getTimeInMillis();
        }

        @Override // android.widget.CalendarView.CalendarViewDelegate
        public void setShowWeekNumber(boolean showWeekNumber) {
            if (this.mShowWeekNumber != showWeekNumber) {
                this.mShowWeekNumber = showWeekNumber;
                this.mAdapter.notifyDataSetChanged();
                setUpHeader();
            }
        }

        @Override // android.widget.CalendarView.CalendarViewDelegate
        public boolean getShowWeekNumber() {
            return this.mShowWeekNumber;
        }

        @Override // android.widget.CalendarView.CalendarViewDelegate
        public void setFirstDayOfWeek(int firstDayOfWeek) {
            if (this.mFirstDayOfWeek != firstDayOfWeek) {
                this.mFirstDayOfWeek = firstDayOfWeek;
                this.mAdapter.init();
                this.mAdapter.notifyDataSetChanged();
                setUpHeader();
            }
        }

        @Override // android.widget.CalendarView.CalendarViewDelegate
        public int getFirstDayOfWeek() {
            return this.mFirstDayOfWeek;
        }

        @Override // android.widget.CalendarView.CalendarViewDelegate
        public void setDate(long date) {
            setDate(date, false, false);
        }

        @Override // android.widget.CalendarView.CalendarViewDelegate
        public void setDate(long date, boolean animate, boolean center) {
            this.mTempDate.setTimeInMillis(date);
            if (!isSameDate(this.mTempDate, this.mAdapter.mSelectedDate)) {
                goTo(this.mTempDate, animate, true, center);
            }
        }

        @Override // android.widget.CalendarView.CalendarViewDelegate
        public long getDate() {
            return this.mAdapter.mSelectedDate.getTimeInMillis();
        }

        @Override // android.widget.CalendarView.CalendarViewDelegate
        public void setOnDateChangeListener(OnDateChangeListener listener) {
            this.mOnDateChangeListener = listener;
        }

        @Override // android.widget.CalendarView.CalendarViewDelegate
        public void onConfigurationChanged(Configuration newConfig) {
            setCurrentLocale(newConfig.locale);
        }

        @Override // android.widget.CalendarView.CalendarViewDelegate
        public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
            event.setClassName(CalendarView.class.getName());
        }

        @Override // android.widget.CalendarView.CalendarViewDelegate
        public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
            info.setClassName(CalendarView.class.getName());
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.CalendarView.AbstractCalendarViewDelegate
        public void setCurrentLocale(Locale locale) {
            super.setCurrentLocale(locale);
            this.mTempDate = getCalendarForLocale(this.mTempDate, locale);
            this.mFirstDayOfMonth = getCalendarForLocale(this.mFirstDayOfMonth, locale);
            this.mMinDate = getCalendarForLocale(this.mMinDate, locale);
            this.mMaxDate = getCalendarForLocale(this.mMaxDate, locale);
        }

        private void updateDateTextSize() {
            TypedArray dateTextAppearance = this.mDelegator.getContext().obtainStyledAttributes(this.mDateTextAppearanceResId, R.styleable.TextAppearance);
            this.mDateTextSize = dateTextAppearance.getDimensionPixelSize(0, 14);
            dateTextAppearance.recycle();
        }

        private void invalidateAllWeekViews() {
            int childCount = this.mListView.getChildCount();
            for (int i = 0; i < childCount; i++) {
                this.mListView.getChildAt(i).invalidate();
            }
        }

        private static Calendar getCalendarForLocale(Calendar oldCalendar, Locale locale) {
            if (oldCalendar == null) {
                return Calendar.getInstance(locale);
            }
            long currentTimeMillis = oldCalendar.getTimeInMillis();
            Calendar newCalendar = Calendar.getInstance(locale);
            newCalendar.setTimeInMillis(currentTimeMillis);
            return newCalendar;
        }

        private static boolean isSameDate(Calendar firstDate, Calendar secondDate) {
            return firstDate.get(6) == secondDate.get(6) && firstDate.get(1) == secondDate.get(1);
        }

        private void setUpAdapter() {
            if (this.mAdapter == null) {
                this.mAdapter = new WeeksAdapter(this.mContext);
                this.mAdapter.registerDataSetObserver(new DataSetObserver() {
                    /* class android.widget.CalendarView.LegacyCalendarViewDelegate.AnonymousClass1 */

                    @Override // android.database.DataSetObserver
                    public void onChanged() {
                        if (LegacyCalendarViewDelegate.this.mOnDateChangeListener != null) {
                            Calendar selectedDay = LegacyCalendarViewDelegate.this.mAdapter.getSelectedDay();
                            LegacyCalendarViewDelegate.this.mOnDateChangeListener.onSelectedDayChange(LegacyCalendarViewDelegate.this.mDelegator, selectedDay.get(1), selectedDay.get(2), selectedDay.get(5));
                        }
                    }
                });
                this.mListView.setAdapter((ListAdapter) this.mAdapter);
            }
            this.mAdapter.notifyDataSetChanged();
        }

        private void setUpHeader() {
            this.mDayLabels = new String[this.mDaysPerWeek];
            int count = this.mFirstDayOfWeek + this.mDaysPerWeek;
            for (int i = this.mFirstDayOfWeek; i < count; i++) {
                this.mDayLabels[i - this.mFirstDayOfWeek] = DateUtils.getDayOfWeekString(i > 7 ? i - 7 : i, 50);
            }
            TextView label = (TextView) this.mDayNamesHeader.getChildAt(0);
            if (this.mShowWeekNumber) {
                label.setVisibility(0);
            } else {
                label.setVisibility(8);
            }
            int count2 = this.mDayNamesHeader.getChildCount();
            for (int i2 = 1; i2 < count2; i2++) {
                TextView label2 = (TextView) this.mDayNamesHeader.getChildAt(i2);
                if (this.mWeekDayTextAppearanceResId > -1) {
                    label2.setTextAppearance(this.mContext, this.mWeekDayTextAppearanceResId);
                }
                if (i2 < this.mDaysPerWeek + 1) {
                    label2.setText(this.mDayLabels[i2 - 1]);
                    label2.setVisibility(0);
                } else {
                    label2.setVisibility(8);
                }
            }
            this.mDayNamesHeader.invalidate();
        }

        private void setUpListView() {
            this.mListView.setDivider(null);
            this.mListView.setItemsCanFocus(true);
            this.mListView.setVerticalScrollBarEnabled(false);
            this.mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
                /* class android.widget.CalendarView.LegacyCalendarViewDelegate.AnonymousClass2 */

                @Override // android.widget.AbsListView.OnScrollListener
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    LegacyCalendarViewDelegate.this.onScrollStateChanged(view, scrollState);
                }

                @Override // android.widget.AbsListView.OnScrollListener
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    LegacyCalendarViewDelegate.this.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
                }
            });
            this.mListView.setFriction(this.mFriction);
            this.mListView.setVelocityScale(this.mVelocityScale);
        }

        private void goTo(Calendar date, boolean animate, boolean setSelected, boolean forceScroll) {
            int position;
            if (date.before(this.mMinDate) || date.after(this.mMaxDate)) {
                throw new IllegalArgumentException("Time not between " + this.mMinDate.getTime() + " and " + this.mMaxDate.getTime());
            }
            int firstFullyVisiblePosition = this.mListView.getFirstVisiblePosition();
            View firstChild = this.mListView.getChildAt(0);
            if (firstChild != null && firstChild.getTop() < 0) {
                firstFullyVisiblePosition++;
            }
            int lastFullyVisiblePosition = (this.mShownWeekCount + firstFullyVisiblePosition) - 1;
            if (firstChild != null && firstChild.getTop() > this.mBottomBuffer) {
                lastFullyVisiblePosition--;
            }
            if (setSelected) {
                this.mAdapter.setSelectedDay(date);
            }
            int position2 = getWeeksSinceMinDate(date);
            if (position2 < firstFullyVisiblePosition || position2 > lastFullyVisiblePosition || forceScroll) {
                this.mFirstDayOfMonth.setTimeInMillis(date.getTimeInMillis());
                this.mFirstDayOfMonth.set(5, 1);
                setMonthDisplayed(this.mFirstDayOfMonth);
                if (this.mFirstDayOfMonth.before(this.mMinDate)) {
                    position = 0;
                } else {
                    position = getWeeksSinceMinDate(this.mFirstDayOfMonth);
                }
                this.mPreviousScrollState = 2;
                if (animate) {
                    this.mListView.smoothScrollToPositionFromTop(position, this.mListScrollTopOffset, 1000);
                    return;
                }
                this.mListView.setSelectionFromTop(position, this.mListScrollTopOffset);
                onScrollStateChanged(this.mListView, 0);
            } else if (setSelected) {
                setMonthDisplayed(date);
            }
        }

        private boolean parseDate(String date, Calendar outDate) {
            try {
                outDate.setTime(this.mDateFormat.parse(date));
                return true;
            } catch (ParseException e) {
                Log.w(CalendarView.LOG_TAG, "Date: " + date + " not in format: " + DATE_FORMAT);
                return false;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void onScrollStateChanged(AbsListView view, int scrollState) {
            this.mScrollStateChangedRunnable.doScrollStateChange(view, scrollState);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            int month;
            int monthDiff;
            WeekView child = (WeekView) view.getChildAt(0);
            if (child != null) {
                long currScroll = (long) ((view.getFirstVisiblePosition() * child.getHeight()) - child.getBottom());
                if (currScroll < this.mPreviousScrollPosition) {
                    this.mIsScrollingUp = true;
                } else if (currScroll > this.mPreviousScrollPosition) {
                    this.mIsScrollingUp = false;
                } else {
                    return;
                }
                int offset = child.getBottom() < this.mWeekMinVisibleHeight ? 1 : 0;
                if (this.mIsScrollingUp) {
                    child = (WeekView) view.getChildAt(offset + 2);
                } else if (offset != 0) {
                    child = (WeekView) view.getChildAt(offset);
                }
                if (child != null) {
                    if (this.mIsScrollingUp) {
                        month = child.getMonthOfFirstWeekDay();
                    } else {
                        month = child.getMonthOfLastWeekDay();
                    }
                    if (this.mCurrentMonthDisplayed == 11 && month == 0) {
                        monthDiff = 1;
                    } else if (this.mCurrentMonthDisplayed == 0 && month == 11) {
                        monthDiff = -1;
                    } else {
                        monthDiff = month - this.mCurrentMonthDisplayed;
                    }
                    if ((!this.mIsScrollingUp && monthDiff > 0) || (this.mIsScrollingUp && monthDiff < 0)) {
                        Calendar firstDay = child.getFirstDay();
                        if (this.mIsScrollingUp) {
                            firstDay.add(5, -7);
                        } else {
                            firstDay.add(5, 7);
                        }
                        setMonthDisplayed(firstDay);
                    }
                }
                this.mPreviousScrollPosition = currScroll;
                this.mPreviousScrollState = this.mCurrentScrollState;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setMonthDisplayed(Calendar calendar) {
            this.mCurrentMonthDisplayed = calendar.get(2);
            this.mAdapter.setFocusMonth(this.mCurrentMonthDisplayed);
            long millis = calendar.getTimeInMillis();
            this.mMonthName.setText(DateUtils.formatDateRange(this.mContext, millis, millis, 52));
            this.mMonthName.invalidate();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int getWeeksSinceMinDate(Calendar date) {
            if (date.before(this.mMinDate)) {
                throw new IllegalArgumentException("fromDate: " + this.mMinDate.getTime() + " does not precede toDate: " + date.getTime());
            }
            long endTimeMillis = date.getTimeInMillis() + ((long) date.getTimeZone().getOffset(date.getTimeInMillis()));
            long startTimeMillis = this.mMinDate.getTimeInMillis() + ((long) this.mMinDate.getTimeZone().getOffset(this.mMinDate.getTimeInMillis()));
            return (int) (((endTimeMillis - startTimeMillis) + (((long) (this.mMinDate.get(7) - this.mFirstDayOfWeek)) * 86400000)) / 604800000);
        }

        /* access modifiers changed from: private */
        public class ScrollStateRunnable implements Runnable {
            private int mNewState;
            private AbsListView mView;

            private ScrollStateRunnable() {
            }

            public void doScrollStateChange(AbsListView view, int scrollState) {
                this.mView = view;
                this.mNewState = scrollState;
                LegacyCalendarViewDelegate.this.mDelegator.removeCallbacks(this);
                LegacyCalendarViewDelegate.this.mDelegator.postDelayed(this, 40);
            }

            public void run() {
                LegacyCalendarViewDelegate.this.mCurrentScrollState = this.mNewState;
                if (this.mNewState == 0 && LegacyCalendarViewDelegate.this.mPreviousScrollState != 0) {
                    View child = this.mView.getChildAt(0);
                    if (child != null) {
                        int dist = child.getBottom() - LegacyCalendarViewDelegate.this.mListScrollTopOffset;
                        if (dist > LegacyCalendarViewDelegate.this.mListScrollTopOffset) {
                            if (LegacyCalendarViewDelegate.this.mIsScrollingUp) {
                                this.mView.smoothScrollBy(dist - child.getHeight(), 500);
                            } else {
                                this.mView.smoothScrollBy(dist, 500);
                            }
                        }
                    } else {
                        return;
                    }
                }
                LegacyCalendarViewDelegate.this.mPreviousScrollState = this.mNewState;
            }
        }

        /* access modifiers changed from: private */
        public class WeeksAdapter extends BaseAdapter implements View.OnTouchListener {
            private int mFocusedMonth;
            private GestureDetector mGestureDetector;
            private final Calendar mSelectedDate = Calendar.getInstance();
            private int mSelectedWeek;
            private int mTotalWeekCount;

            public WeeksAdapter(Context context) {
                LegacyCalendarViewDelegate.this.mContext = context;
                this.mGestureDetector = new GestureDetector(LegacyCalendarViewDelegate.this.mContext, new CalendarGestureListener());
                init();
            }

            /* access modifiers changed from: private */
            /* access modifiers changed from: public */
            private void init() {
                this.mSelectedWeek = LegacyCalendarViewDelegate.this.getWeeksSinceMinDate(this.mSelectedDate);
                this.mTotalWeekCount = LegacyCalendarViewDelegate.this.getWeeksSinceMinDate(LegacyCalendarViewDelegate.this.mMaxDate);
                if (!(LegacyCalendarViewDelegate.this.mMinDate.get(7) == LegacyCalendarViewDelegate.this.mFirstDayOfWeek && LegacyCalendarViewDelegate.this.mMaxDate.get(7) == LegacyCalendarViewDelegate.this.mFirstDayOfWeek)) {
                    this.mTotalWeekCount++;
                }
                notifyDataSetChanged();
            }

            public void setSelectedDay(Calendar selectedDay) {
                if (selectedDay.get(6) != this.mSelectedDate.get(6) || selectedDay.get(1) != this.mSelectedDate.get(1)) {
                    this.mSelectedDate.setTimeInMillis(selectedDay.getTimeInMillis());
                    this.mSelectedWeek = LegacyCalendarViewDelegate.this.getWeeksSinceMinDate(this.mSelectedDate);
                    this.mFocusedMonth = this.mSelectedDate.get(2);
                    notifyDataSetChanged();
                }
            }

            public Calendar getSelectedDay() {
                return this.mSelectedDate;
            }

            @Override // android.widget.Adapter
            public int getCount() {
                return this.mTotalWeekCount;
            }

            @Override // android.widget.Adapter
            public Object getItem(int position) {
                return null;
            }

            @Override // android.widget.Adapter
            public long getItemId(int position) {
                return (long) position;
            }

            @Override // android.widget.Adapter
            public View getView(int position, View convertView, ViewGroup parent) {
                WeekView weekView;
                if (convertView != null) {
                    weekView = (WeekView) convertView;
                } else {
                    weekView = new WeekView(LegacyCalendarViewDelegate.this.mContext);
                    weekView.setLayoutParams(new AbsListView.LayoutParams(-2, -2));
                    weekView.setClickable(true);
                    weekView.setOnTouchListener(this);
                }
                weekView.init(position, this.mSelectedWeek == position ? this.mSelectedDate.get(7) : -1, this.mFocusedMonth);
                return weekView;
            }

            public void setFocusMonth(int month) {
                if (this.mFocusedMonth != month) {
                    this.mFocusedMonth = month;
                    notifyDataSetChanged();
                }
            }

            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View v, MotionEvent event) {
                if (!LegacyCalendarViewDelegate.this.mListView.isEnabled() || !this.mGestureDetector.onTouchEvent(event)) {
                    return false;
                }
                if (!((WeekView) v).getDayFromLocation(event.getX(), LegacyCalendarViewDelegate.this.mTempDate) || LegacyCalendarViewDelegate.this.mTempDate.before(LegacyCalendarViewDelegate.this.mMinDate) || LegacyCalendarViewDelegate.this.mTempDate.after(LegacyCalendarViewDelegate.this.mMaxDate)) {
                    return true;
                }
                onDateTapped(LegacyCalendarViewDelegate.this.mTempDate);
                return true;
            }

            private void onDateTapped(Calendar day) {
                setSelectedDay(day);
                LegacyCalendarViewDelegate.this.setMonthDisplayed(day);
            }

            class CalendarGestureListener extends GestureDetector.SimpleOnGestureListener {
                CalendarGestureListener() {
                }

                @Override // android.view.GestureDetector.SimpleOnGestureListener, android.view.GestureDetector.OnGestureListener
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }
            }
        }

        /* access modifiers changed from: private */
        public class WeekView extends View {
            private String[] mDayNumbers;
            private final Paint mDrawPaint = new Paint();
            private Calendar mFirstDay;
            private boolean[] mFocusDay;
            private boolean mHasFocusedDay;
            private boolean mHasSelectedDay = false;
            private boolean mHasUnfocusedDay;
            private int mHeight;
            private int mLastWeekDayMonth = -1;
            private final Paint mMonthNumDrawPaint = new Paint();
            private int mMonthOfFirstWeekDay = -1;
            private int mNumCells;
            private int mSelectedDay = -1;
            private int mSelectedLeft = -1;
            private int mSelectedRight = -1;
            private final Rect mTempRect = new Rect();
            private int mWeek = -1;
            private int mWidth;

            public WeekView(Context context) {
                super(context);
                initilaizePaints();
            }

            public void init(int weekNumber, int selectedWeekDay, int focusedMonth) {
                int i;
                this.mSelectedDay = selectedWeekDay;
                this.mHasSelectedDay = this.mSelectedDay != -1;
                if (LegacyCalendarViewDelegate.this.mShowWeekNumber) {
                    i = LegacyCalendarViewDelegate.this.mDaysPerWeek + 1;
                } else {
                    i = LegacyCalendarViewDelegate.this.mDaysPerWeek;
                }
                this.mNumCells = i;
                this.mWeek = weekNumber;
                LegacyCalendarViewDelegate.this.mTempDate.setTimeInMillis(LegacyCalendarViewDelegate.this.mMinDate.getTimeInMillis());
                LegacyCalendarViewDelegate.this.mTempDate.add(3, this.mWeek);
                LegacyCalendarViewDelegate.this.mTempDate.setFirstDayOfWeek(LegacyCalendarViewDelegate.this.mFirstDayOfWeek);
                this.mDayNumbers = new String[this.mNumCells];
                this.mFocusDay = new boolean[this.mNumCells];
                int i2 = 0;
                if (LegacyCalendarViewDelegate.this.mShowWeekNumber) {
                    this.mDayNumbers[0] = String.format(Locale.getDefault(), "%d", Integer.valueOf(LegacyCalendarViewDelegate.this.mTempDate.get(3)));
                    i2 = 0 + 1;
                }
                LegacyCalendarViewDelegate.this.mTempDate.add(5, LegacyCalendarViewDelegate.this.mFirstDayOfWeek - LegacyCalendarViewDelegate.this.mTempDate.get(7));
                this.mFirstDay = (Calendar) LegacyCalendarViewDelegate.this.mTempDate.clone();
                this.mMonthOfFirstWeekDay = LegacyCalendarViewDelegate.this.mTempDate.get(2);
                this.mHasUnfocusedDay = true;
                while (i2 < this.mNumCells) {
                    boolean isFocusedDay = LegacyCalendarViewDelegate.this.mTempDate.get(2) == focusedMonth;
                    this.mFocusDay[i2] = isFocusedDay;
                    this.mHasFocusedDay |= isFocusedDay;
                    this.mHasUnfocusedDay = (!isFocusedDay) & this.mHasUnfocusedDay;
                    if (LegacyCalendarViewDelegate.this.mTempDate.before(LegacyCalendarViewDelegate.this.mMinDate) || LegacyCalendarViewDelegate.this.mTempDate.after(LegacyCalendarViewDelegate.this.mMaxDate)) {
                        this.mDayNumbers[i2] = ProxyInfo.LOCAL_EXCL_LIST;
                    } else {
                        this.mDayNumbers[i2] = String.format(Locale.getDefault(), "%d", Integer.valueOf(LegacyCalendarViewDelegate.this.mTempDate.get(5)));
                    }
                    LegacyCalendarViewDelegate.this.mTempDate.add(5, 1);
                    i2++;
                }
                if (LegacyCalendarViewDelegate.this.mTempDate.get(5) == 1) {
                    LegacyCalendarViewDelegate.this.mTempDate.add(5, -1);
                }
                this.mLastWeekDayMonth = LegacyCalendarViewDelegate.this.mTempDate.get(2);
                updateSelectionPositions();
            }

            private void initilaizePaints() {
                this.mDrawPaint.setFakeBoldText(false);
                this.mDrawPaint.setAntiAlias(true);
                this.mDrawPaint.setStyle(Paint.Style.FILL);
                this.mMonthNumDrawPaint.setFakeBoldText(true);
                this.mMonthNumDrawPaint.setAntiAlias(true);
                this.mMonthNumDrawPaint.setStyle(Paint.Style.FILL);
                this.mMonthNumDrawPaint.setTextAlign(Paint.Align.CENTER);
                this.mMonthNumDrawPaint.setTextSize((float) LegacyCalendarViewDelegate.this.mDateTextSize);
            }

            public int getMonthOfFirstWeekDay() {
                return this.mMonthOfFirstWeekDay;
            }

            public int getMonthOfLastWeekDay() {
                return this.mLastWeekDayMonth;
            }

            public Calendar getFirstDay() {
                return this.mFirstDay;
            }

            public boolean getDayFromLocation(float x, Calendar outCalendar) {
                int start;
                int end;
                boolean isLayoutRtl = isLayoutRtl();
                if (isLayoutRtl) {
                    start = 0;
                    end = LegacyCalendarViewDelegate.this.mShowWeekNumber ? this.mWidth - (this.mWidth / this.mNumCells) : this.mWidth;
                } else {
                    if (LegacyCalendarViewDelegate.this.mShowWeekNumber) {
                        start = this.mWidth / this.mNumCells;
                    } else {
                        start = 0;
                    }
                    end = this.mWidth;
                }
                if (x < ((float) start) || x > ((float) end)) {
                    outCalendar.clear();
                    return false;
                }
                int dayPosition = (int) (((x - ((float) start)) * ((float) LegacyCalendarViewDelegate.this.mDaysPerWeek)) / ((float) (end - start)));
                if (isLayoutRtl) {
                    dayPosition = (LegacyCalendarViewDelegate.this.mDaysPerWeek - 1) - dayPosition;
                }
                outCalendar.setTimeInMillis(this.mFirstDay.getTimeInMillis());
                outCalendar.add(5, dayPosition);
                return true;
            }

            /* access modifiers changed from: protected */
            @Override // android.view.View
            public void onDraw(Canvas canvas) {
                drawBackground(canvas);
                drawWeekNumbersAndDates(canvas);
                drawWeekSeparators(canvas);
                drawSelectedDateVerticalBars(canvas);
            }

            private void drawBackground(Canvas canvas) {
                int i = 0;
                if (this.mHasSelectedDay) {
                    this.mDrawPaint.setColor(LegacyCalendarViewDelegate.this.mSelectedWeekBackgroundColor);
                    this.mTempRect.top = LegacyCalendarViewDelegate.this.mWeekSeperatorLineWidth;
                    this.mTempRect.bottom = this.mHeight;
                    boolean isLayoutRtl = isLayoutRtl();
                    if (isLayoutRtl) {
                        this.mTempRect.left = 0;
                        this.mTempRect.right = this.mSelectedLeft - 2;
                    } else {
                        Rect rect = this.mTempRect;
                        if (LegacyCalendarViewDelegate.this.mShowWeekNumber) {
                            i = this.mWidth / this.mNumCells;
                        }
                        rect.left = i;
                        this.mTempRect.right = this.mSelectedLeft - 2;
                    }
                    canvas.drawRect(this.mTempRect, this.mDrawPaint);
                    if (isLayoutRtl) {
                        this.mTempRect.left = this.mSelectedRight + 3;
                        this.mTempRect.right = LegacyCalendarViewDelegate.this.mShowWeekNumber ? this.mWidth - (this.mWidth / this.mNumCells) : this.mWidth;
                    } else {
                        this.mTempRect.left = this.mSelectedRight + 3;
                        this.mTempRect.right = this.mWidth;
                    }
                    canvas.drawRect(this.mTempRect, this.mDrawPaint);
                }
            }

            private void drawWeekNumbersAndDates(Canvas canvas) {
                int y = ((int) ((((float) this.mHeight) + this.mDrawPaint.getTextSize()) / 2.0f)) - LegacyCalendarViewDelegate.this.mWeekSeperatorLineWidth;
                int nDays = this.mNumCells;
                int divisor = nDays * 2;
                this.mDrawPaint.setTextAlign(Paint.Align.CENTER);
                this.mDrawPaint.setTextSize((float) LegacyCalendarViewDelegate.this.mDateTextSize);
                int i = 0;
                if (isLayoutRtl()) {
                    while (i < nDays - 1) {
                        this.mMonthNumDrawPaint.setColor(this.mFocusDay[i] ? LegacyCalendarViewDelegate.this.mFocusedMonthDateColor : LegacyCalendarViewDelegate.this.mUnfocusedMonthDateColor);
                        canvas.drawText(this.mDayNumbers[(nDays - 1) - i], (float) ((((i * 2) + 1) * this.mWidth) / divisor), (float) y, this.mMonthNumDrawPaint);
                        i++;
                    }
                    if (LegacyCalendarViewDelegate.this.mShowWeekNumber) {
                        this.mDrawPaint.setColor(LegacyCalendarViewDelegate.this.mWeekNumberColor);
                        canvas.drawText(this.mDayNumbers[0], (float) (this.mWidth - (this.mWidth / divisor)), (float) y, this.mDrawPaint);
                        return;
                    }
                    return;
                }
                if (LegacyCalendarViewDelegate.this.mShowWeekNumber) {
                    this.mDrawPaint.setColor(LegacyCalendarViewDelegate.this.mWeekNumberColor);
                    canvas.drawText(this.mDayNumbers[0], (float) (this.mWidth / divisor), (float) y, this.mDrawPaint);
                    i = 0 + 1;
                }
                while (i < nDays) {
                    this.mMonthNumDrawPaint.setColor(this.mFocusDay[i] ? LegacyCalendarViewDelegate.this.mFocusedMonthDateColor : LegacyCalendarViewDelegate.this.mUnfocusedMonthDateColor);
                    canvas.drawText(this.mDayNumbers[i], (float) ((((i * 2) + 1) * this.mWidth) / divisor), (float) y, this.mMonthNumDrawPaint);
                    i++;
                }
            }

            private void drawWeekSeparators(Canvas canvas) {
                float startX;
                float stopX;
                int firstFullyVisiblePosition = LegacyCalendarViewDelegate.this.mListView.getFirstVisiblePosition();
                if (LegacyCalendarViewDelegate.this.mListView.getChildAt(0).getTop() < 0) {
                    firstFullyVisiblePosition++;
                }
                if (firstFullyVisiblePosition != this.mWeek) {
                    this.mDrawPaint.setColor(LegacyCalendarViewDelegate.this.mWeekSeparatorLineColor);
                    this.mDrawPaint.setStrokeWidth((float) LegacyCalendarViewDelegate.this.mWeekSeperatorLineWidth);
                    if (isLayoutRtl()) {
                        startX = 0.0f;
                        stopX = LegacyCalendarViewDelegate.this.mShowWeekNumber ? (float) (this.mWidth - (this.mWidth / this.mNumCells)) : (float) this.mWidth;
                    } else {
                        if (LegacyCalendarViewDelegate.this.mShowWeekNumber) {
                            startX = (float) (this.mWidth / this.mNumCells);
                        } else {
                            startX = 0.0f;
                        }
                        stopX = (float) this.mWidth;
                    }
                    canvas.drawLine(startX, 0.0f, stopX, 0.0f, this.mDrawPaint);
                }
            }

            private void drawSelectedDateVerticalBars(Canvas canvas) {
                if (this.mHasSelectedDay) {
                    LegacyCalendarViewDelegate.this.mSelectedDateVerticalBar.setBounds(this.mSelectedLeft - (LegacyCalendarViewDelegate.this.mSelectedDateVerticalBarWidth / 2), LegacyCalendarViewDelegate.this.mWeekSeperatorLineWidth, this.mSelectedLeft + (LegacyCalendarViewDelegate.this.mSelectedDateVerticalBarWidth / 2), this.mHeight);
                    LegacyCalendarViewDelegate.this.mSelectedDateVerticalBar.draw(canvas);
                    LegacyCalendarViewDelegate.this.mSelectedDateVerticalBar.setBounds(this.mSelectedRight - (LegacyCalendarViewDelegate.this.mSelectedDateVerticalBarWidth / 2), LegacyCalendarViewDelegate.this.mWeekSeperatorLineWidth, this.mSelectedRight + (LegacyCalendarViewDelegate.this.mSelectedDateVerticalBarWidth / 2), this.mHeight);
                    LegacyCalendarViewDelegate.this.mSelectedDateVerticalBar.draw(canvas);
                }
            }

            /* access modifiers changed from: protected */
            @Override // android.view.View
            public void onSizeChanged(int w, int h, int oldw, int oldh) {
                this.mWidth = w;
                updateSelectionPositions();
            }

            private void updateSelectionPositions() {
                if (this.mHasSelectedDay) {
                    boolean isLayoutRtl = isLayoutRtl();
                    int selectedPosition = this.mSelectedDay - LegacyCalendarViewDelegate.this.mFirstDayOfWeek;
                    if (selectedPosition < 0) {
                        selectedPosition += 7;
                    }
                    if (LegacyCalendarViewDelegate.this.mShowWeekNumber && !isLayoutRtl) {
                        selectedPosition++;
                    }
                    if (isLayoutRtl) {
                        this.mSelectedLeft = (((LegacyCalendarViewDelegate.this.mDaysPerWeek - 1) - selectedPosition) * this.mWidth) / this.mNumCells;
                    } else {
                        this.mSelectedLeft = (this.mWidth * selectedPosition) / this.mNumCells;
                    }
                    this.mSelectedRight = this.mSelectedLeft + (this.mWidth / this.mNumCells);
                }
            }

            /* access modifiers changed from: protected */
            @Override // android.view.View
            public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                this.mHeight = ((LegacyCalendarViewDelegate.this.mListView.getHeight() - LegacyCalendarViewDelegate.this.mListView.getPaddingTop()) - LegacyCalendarViewDelegate.this.mListView.getPaddingBottom()) / LegacyCalendarViewDelegate.this.mShownWeekCount;
                setMeasuredDimension(View.MeasureSpec.getSize(widthMeasureSpec), this.mHeight);
            }
        }
    }
}

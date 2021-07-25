package android.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
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

    private interface CalendarViewDelegate {
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

    public void setEnabled(boolean enabled) {
        this.mDelegate.setEnabled(enabled);
    }

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
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.mDelegate.onConfigurationChanged(newConfig);
    }

    @Override // android.widget.FrameLayout
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        this.mDelegate.onInitializeAccessibilityEvent(event);
    }

    @Override // android.widget.FrameLayout
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
            this.mDateTextAppearanceResId = attributesArray.getResourceId(12, R.style.TextAppearance_Small);
            updateDateTextSize();
            this.mWeekDayTextAppearanceResId = attributesArray.getResourceId(11, -1);
            attributesArray.recycle();
            DisplayMetrics displayMetrics = this.mDelegator.getResources().getDisplayMetrics();
            this.mWeekMinVisibleHeight = (int) TypedValue.applyDimension(1, 12.0f, displayMetrics);
            this.mListScrollTopOffset = (int) TypedValue.applyDimension(1, 2.0f, displayMetrics);
            this.mBottomBuffer = (int) TypedValue.applyDimension(1, 20.0f, displayMetrics);
            this.mSelectedDateVerticalBarWidth = (int) TypedValue.applyDimension(1, 6.0f, displayMetrics);
            this.mWeekSeperatorLineWidth = (int) TypedValue.applyDimension(1, 1.0f, displayMetrics);
            View content = ((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate(R.layout.calendar_view, (ViewGroup) null, false);
            this.mDelegator.addView(content);
            this.mListView = (ListView) this.mDelegator.findViewById(R.id.list);
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
                    WeekView weekView = this.mListView.getChildAt(i);
                    if (WeekView.access$100(weekView)) {
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
                    WeekView weekView = this.mListView.getChildAt(i);
                    if (WeekView.access$200(weekView)) {
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
                    WeekView weekView = this.mListView.getChildAt(i);
                    if (WeekView.access$300(weekView)) {
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
                    WeekView weekView = this.mListView.getChildAt(i);
                    if (WeekView.access$100(weekView)) {
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
                Calendar date = WeeksAdapter.access$400(this.mAdapter);
                if (date.before(this.mMinDate)) {
                    this.mAdapter.setSelectedDay(this.mMinDate);
                }
                WeeksAdapter.access$500(this.mAdapter);
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
                WeeksAdapter.access$500(this.mAdapter);
                Calendar date = WeeksAdapter.access$400(this.mAdapter);
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
                WeeksAdapter.access$500(this.mAdapter);
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
            if (!isSameDate(this.mTempDate, WeeksAdapter.access$400(this.mAdapter))) {
                goTo(this.mTempDate, animate, true, center);
            }
        }

        @Override // android.widget.CalendarView.CalendarViewDelegate
        public long getDate() {
            return WeeksAdapter.access$400(this.mAdapter).getTimeInMillis();
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
                this.mAdapter = new WeeksAdapter(this, this.mContext);
                this.mAdapter.registerDataSetObserver(new DataSetObserver() {
                    /* class android.widget.CalendarView.LegacyCalendarViewDelegate.AnonymousClass1 */

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
            WeekView child = view.getChildAt(0);
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
                    child = view.getChildAt(offset + 2);
                } else if (offset != 0) {
                    child = view.getChildAt(offset);
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
        public void setMonthDisplayed(Calendar calendar) {
            this.mCurrentMonthDisplayed = calendar.get(2);
            this.mAdapter.setFocusMonth(this.mCurrentMonthDisplayed);
            long millis = calendar.getTimeInMillis();
            this.mMonthName.setText(DateUtils.formatDateRange(this.mContext, millis, millis, 52));
            this.mMonthName.invalidate();
        }

        /* access modifiers changed from: private */
        public int getWeeksSinceMinDate(Calendar date) {
            if (date.before(this.mMinDate)) {
                throw new IllegalArgumentException("fromDate: " + this.mMinDate.getTime() + " does not precede toDate: " + date.getTime());
            }
            long endTimeMillis = date.getTimeInMillis() + ((long) date.getTimeZone().getOffset(date.getTimeInMillis()));
            long startTimeMillis = this.mMinDate.getTimeInMillis() + ((long) this.mMinDate.getTimeZone().getOffset(this.mMinDate.getTimeInMillis()));
            return (int) (((endTimeMillis - startTimeMillis) + (((long) (this.mMinDate.get(7) - this.mFirstDayOfWeek)) * MILLIS_IN_DAY)) / MILLIS_IN_WEEK);
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
    }
}

package android.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.net.ProxyInfo;
import android.os.Parcelable;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.DatePicker;
import com.android.internal.R;
import com.android.internal.widget.AccessibleDateAnimator;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;

/* access modifiers changed from: package-private */
public class DatePickerCalendarDelegate extends DatePicker.AbstractDatePickerDelegate implements View.OnClickListener, DatePickerController {
    private static final int ANIMATION_DURATION = 300;
    private static final int DAY_INDEX = 1;
    private static final int DEFAULT_END_YEAR = 2100;
    private static final int DEFAULT_START_YEAR = 1900;
    private static final int MONTH_AND_DAY_VIEW = 0;
    private static final int MONTH_INDEX = 0;
    private static final int UNINITIALIZED = -1;
    private static final int USE_LOCALE = 0;
    private static final int YEAR_INDEX = 2;
    private static final int YEAR_VIEW = 1;
    private AccessibleDateAnimator mAnimator;
    private Calendar mCurrentDate;
    private int mCurrentView = -1;
    private DatePicker.OnDateChangedListener mDateChangedListener;
    private SimpleDateFormat mDayFormat = new SimpleDateFormat("d", Locale.getDefault());
    private TextView mDayOfWeekView;
    private String mDayPickerDescription;
    private DayPickerView mDayPickerView;
    private int mFirstDayOfWeek = 0;
    private TextView mHeaderDayOfMonthTextView;
    private TextView mHeaderMonthTextView;
    private TextView mHeaderYearTextView;
    private boolean mIsEnabled = true;
    private HashSet<OnDateChangedListener> mListeners = new HashSet<>();
    private Calendar mMaxDate;
    private Calendar mMinDate;
    private LinearLayout mMonthAndDayLayout;
    private LinearLayout mMonthDayYearLayout;
    private String mSelectDay;
    private String mSelectYear;
    private Calendar mTempDate;
    private SimpleDateFormat mYearFormat = new SimpleDateFormat("y", Locale.getDefault());
    private String mYearPickerDescription;
    private YearPickerView mYearPickerView;

    public DatePickerCalendarDelegate(DatePicker delegator, Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(delegator, context);
        Locale locale = Locale.getDefault();
        this.mMinDate = getCalendarForLocale(this.mMinDate, locale);
        this.mMaxDate = getCalendarForLocale(this.mMaxDate, locale);
        this.mTempDate = getCalendarForLocale(this.mMaxDate, locale);
        this.mCurrentDate = getCalendarForLocale(this.mCurrentDate, locale);
        this.mMinDate.set(DEFAULT_START_YEAR, 1, 1);
        this.mMaxDate.set(DEFAULT_END_YEAR, 12, 31);
        Resources res = this.mDelegator.getResources();
        TypedArray a = this.mContext.obtainStyledAttributes(attrs, R.styleable.DatePicker, defStyleAttr, defStyleRes);
        View mainView = ((LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(a.getResourceId(17, 17367105), (ViewGroup) null);
        this.mDelegator.addView(mainView);
        this.mDayOfWeekView = (TextView) mainView.findViewById(16909033);
        LinearLayout dateLayout = (LinearLayout) mainView.findViewById(16909032);
        this.mMonthDayYearLayout = (LinearLayout) mainView.findViewById(16909034);
        this.mMonthAndDayLayout = (LinearLayout) mainView.findViewById(16909035);
        this.mMonthAndDayLayout.setOnClickListener(this);
        this.mHeaderMonthTextView = (TextView) mainView.findViewById(16909036);
        this.mHeaderDayOfMonthTextView = (TextView) mainView.findViewById(16909037);
        this.mHeaderYearTextView = (TextView) mainView.findViewById(16909038);
        this.mHeaderYearTextView.setOnClickListener(this);
        int defaultHighlightColor = this.mHeaderYearTextView.getHighlightColor();
        int dayOfWeekTextAppearanceResId = a.getResourceId(9, -1);
        if (dayOfWeekTextAppearanceResId != -1) {
            this.mDayOfWeekView.setTextAppearance(context, dayOfWeekTextAppearanceResId);
        }
        this.mDayOfWeekView.setBackground(a.getDrawable(8));
        dateLayout.setBackground(a.getDrawable(0));
        int headerSelectedTextColor = a.getColor(20, defaultHighlightColor);
        int monthTextAppearanceResId = a.getResourceId(10, -1);
        if (monthTextAppearanceResId != -1) {
            this.mHeaderMonthTextView.setTextAppearance(context, monthTextAppearanceResId);
        }
        this.mHeaderMonthTextView.setTextColor(ColorStateList.addFirstIfMissing(this.mHeaderMonthTextView.getTextColors(), 16842913, headerSelectedTextColor));
        int dayOfMonthTextAppearanceResId = a.getResourceId(11, -1);
        if (dayOfMonthTextAppearanceResId != -1) {
            this.mHeaderDayOfMonthTextView.setTextAppearance(context, dayOfMonthTextAppearanceResId);
        }
        this.mHeaderDayOfMonthTextView.setTextColor(ColorStateList.addFirstIfMissing(this.mHeaderDayOfMonthTextView.getTextColors(), 16842913, headerSelectedTextColor));
        int yearTextAppearanceResId = a.getResourceId(12, -1);
        if (yearTextAppearanceResId != -1) {
            this.mHeaderYearTextView.setTextAppearance(context, yearTextAppearanceResId);
        }
        this.mHeaderYearTextView.setTextColor(ColorStateList.addFirstIfMissing(this.mHeaderYearTextView.getTextColors(), 16842913, headerSelectedTextColor));
        this.mDayPickerView = new DayPickerView(this.mContext, this);
        this.mYearPickerView = new YearPickerView(this.mContext);
        this.mYearPickerView.init(this);
        this.mYearPickerView.setYearSelectedCircleColor(a.getColor(14, defaultHighlightColor));
        this.mDayPickerView.setCalendarTextColor(ColorStateList.addFirstIfMissing(a.getColorStateList(15), 16842913, a.getColor(18, defaultHighlightColor)));
        this.mDayPickerDescription = res.getString(17040999);
        this.mSelectDay = res.getString(17041001);
        this.mYearPickerDescription = res.getString(17041000);
        this.mSelectYear = res.getString(17041002);
        this.mAnimator = mainView.findViewById(16909039);
        this.mAnimator.addView(this.mDayPickerView);
        this.mAnimator.addView(this.mYearPickerView);
        this.mAnimator.setDateMillis(this.mCurrentDate.getTimeInMillis());
        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(300);
        this.mAnimator.setInAnimation(animation);
        Animation animation2 = new AlphaAnimation(1.0f, 0.0f);
        animation2.setDuration(300);
        this.mAnimator.setOutAnimation(animation2);
        updateDisplay(false);
        setCurrentView(0);
    }

    private Calendar getCalendarForLocale(Calendar oldCalendar, Locale locale) {
        if (oldCalendar == null) {
            return Calendar.getInstance(locale);
        }
        long currentTimeMillis = oldCalendar.getTimeInMillis();
        Calendar newCalendar = Calendar.getInstance(locale);
        newCalendar.setTimeInMillis(currentTimeMillis);
        return newCalendar;
    }

    private int[] getMonthDayYearIndexes(String pattern) {
        int[] result = new int[3];
        String filteredPattern = pattern.replaceAll("'.*?'", ProxyInfo.LOCAL_EXCL_LIST);
        int dayIndex = filteredPattern.indexOf(100);
        int monthMIndex = filteredPattern.indexOf("M");
        int monthIndex = monthMIndex != -1 ? monthMIndex : filteredPattern.indexOf("L");
        if (filteredPattern.indexOf("y") < monthIndex) {
            result[2] = 0;
            if (monthIndex < dayIndex) {
                result[0] = 1;
                result[1] = 2;
            } else {
                result[0] = 2;
                result[1] = 1;
            }
        } else {
            result[2] = 2;
            if (monthIndex < dayIndex) {
                result[0] = 0;
                result[1] = 1;
            } else {
                result[0] = 1;
                result[1] = 0;
            }
        }
        return result;
    }

    private void updateDisplay(boolean announce) {
        if (this.mDayOfWeekView != null) {
            this.mDayOfWeekView.setText(this.mCurrentDate.getDisplayName(7, 2, Locale.getDefault()));
        }
        int[] viewIndices = getMonthDayYearIndexes(DateFormat.getBestDateTimePattern(this.mCurrentLocale, "yMMMd"));
        this.mMonthDayYearLayout.removeAllViews();
        if (viewIndices[2] == 0) {
            this.mMonthDayYearLayout.addView(this.mHeaderYearTextView);
            this.mMonthDayYearLayout.addView(this.mMonthAndDayLayout);
        } else {
            this.mMonthDayYearLayout.addView(this.mMonthAndDayLayout);
            this.mMonthDayYearLayout.addView(this.mHeaderYearTextView);
        }
        this.mMonthAndDayLayout.removeAllViews();
        if (viewIndices[0] > viewIndices[1]) {
            this.mMonthAndDayLayout.addView(this.mHeaderDayOfMonthTextView);
            this.mMonthAndDayLayout.addView(this.mHeaderMonthTextView);
        } else {
            this.mMonthAndDayLayout.addView(this.mHeaderMonthTextView);
            this.mMonthAndDayLayout.addView(this.mHeaderDayOfMonthTextView);
        }
        this.mHeaderMonthTextView.setText(this.mCurrentDate.getDisplayName(2, 1, Locale.getDefault()).toUpperCase(Locale.getDefault()));
        this.mHeaderDayOfMonthTextView.setText(this.mDayFormat.format(this.mCurrentDate.getTime()));
        this.mHeaderYearTextView.setText(this.mYearFormat.format(this.mCurrentDate.getTime()));
        long millis = this.mCurrentDate.getTimeInMillis();
        this.mAnimator.setDateMillis(millis);
        this.mMonthAndDayLayout.setContentDescription(DateUtils.formatDateTime(this.mContext, millis, 24));
        if (announce) {
            this.mAnimator.announceForAccessibility(DateUtils.formatDateTime(this.mContext, millis, 20));
        }
        updatePickers();
    }

    private void setCurrentView(int viewIndex) {
        long millis = this.mCurrentDate.getTimeInMillis();
        switch (viewIndex) {
            case 0:
                this.mDayPickerView.onDateChanged();
                if (this.mCurrentView != viewIndex) {
                    this.mMonthAndDayLayout.setSelected(true);
                    this.mHeaderYearTextView.setSelected(false);
                    this.mAnimator.setDisplayedChild(0);
                    this.mCurrentView = viewIndex;
                }
                this.mAnimator.setContentDescription(this.mDayPickerDescription + ": " + DateUtils.formatDateTime(this.mContext, millis, 16));
                this.mAnimator.announceForAccessibility(this.mSelectDay);
                return;
            case 1:
                this.mYearPickerView.onDateChanged();
                if (this.mCurrentView != viewIndex) {
                    this.mMonthAndDayLayout.setSelected(false);
                    this.mHeaderYearTextView.setSelected(true);
                    this.mAnimator.setDisplayedChild(1);
                    this.mCurrentView = viewIndex;
                }
                this.mAnimator.setContentDescription(this.mYearPickerDescription + ": " + ((Object) this.mYearFormat.format(Long.valueOf(millis))));
                this.mAnimator.announceForAccessibility(this.mSelectYear);
                return;
            default:
                return;
        }
    }

    public void init(int year, int monthOfYear, int dayOfMonth, DatePicker.OnDateChangedListener callBack) {
        this.mDateChangedListener = callBack;
        this.mCurrentDate.set(1, year);
        this.mCurrentDate.set(2, monthOfYear);
        this.mCurrentDate.set(5, dayOfMonth);
        updateDisplay(false);
    }

    public void updateDate(int year, int month, int dayOfMonth) {
        this.mCurrentDate.set(1, year);
        this.mCurrentDate.set(2, month);
        this.mCurrentDate.set(5, dayOfMonth);
        if (this.mDateChangedListener != null) {
            this.mDateChangedListener.onDateChanged(this.mDelegator, year, month, dayOfMonth);
        }
        updateDisplay(false);
    }

    public int getYear() {
        return this.mCurrentDate.get(1);
    }

    public int getMonth() {
        return this.mCurrentDate.get(2);
    }

    public int getDayOfMonth() {
        return this.mCurrentDate.get(5);
    }

    public void setMinDate(long minDate) {
        this.mTempDate.setTimeInMillis(minDate);
        if (this.mTempDate.get(1) != this.mMinDate.get(1) || this.mTempDate.get(6) == this.mMinDate.get(6)) {
            if (this.mCurrentDate.before(this.mTempDate)) {
                this.mCurrentDate.setTimeInMillis(minDate);
                updatePickers();
                updateDisplay(false);
            }
            this.mMinDate.setTimeInMillis(minDate);
            this.mDayPickerView.goTo(getSelectedDay(), false, true, true);
        }
    }

    public Calendar getMinDate() {
        return this.mMinDate;
    }

    public void setMaxDate(long maxDate) {
        this.mTempDate.setTimeInMillis(maxDate);
        if (this.mTempDate.get(1) != this.mMaxDate.get(1) || this.mTempDate.get(6) == this.mMaxDate.get(6)) {
            if (this.mCurrentDate.after(this.mTempDate)) {
                this.mCurrentDate.setTimeInMillis(maxDate);
                updatePickers();
                updateDisplay(false);
            }
            this.mMaxDate.setTimeInMillis(maxDate);
            this.mDayPickerView.goTo(getSelectedDay(), false, true, true);
        }
    }

    public Calendar getMaxDate() {
        return this.mMaxDate;
    }

    public void setFirstDayOfWeek(int firstDayOfWeek) {
        this.mFirstDayOfWeek = firstDayOfWeek;
    }

    public int getFirstDayOfWeek() {
        if (this.mFirstDayOfWeek != 0) {
            return this.mFirstDayOfWeek;
        }
        return this.mCurrentDate.getFirstDayOfWeek();
    }

    public int getMinYear() {
        return this.mMinDate.get(1);
    }

    public int getMaxYear() {
        return this.mMaxDate.get(1);
    }

    public int getMinMonth() {
        return this.mMinDate.get(2);
    }

    public int getMaxMonth() {
        return this.mMaxDate.get(2);
    }

    public int getMinDay() {
        return this.mMinDate.get(5);
    }

    public int getMaxDay() {
        return this.mMaxDate.get(5);
    }

    public void setEnabled(boolean enabled) {
        this.mMonthAndDayLayout.setEnabled(enabled);
        this.mHeaderYearTextView.setEnabled(enabled);
        this.mAnimator.setEnabled(enabled);
        this.mIsEnabled = enabled;
    }

    public boolean isEnabled() {
        return this.mIsEnabled;
    }

    public CalendarView getCalendarView() {
        throw new UnsupportedOperationException("CalendarView does not exists for the new DatePicker");
    }

    public void setCalendarViewShown(boolean shown) {
    }

    public boolean getCalendarViewShown() {
        return false;
    }

    public void setSpinnersShown(boolean shown) {
    }

    public boolean getSpinnersShown() {
        return false;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        this.mYearFormat = new SimpleDateFormat("y", newConfig.locale);
        this.mDayFormat = new SimpleDateFormat("d", newConfig.locale);
    }

    public Parcelable onSaveInstanceState(Parcelable superState) {
        int year = this.mCurrentDate.get(1);
        int month = this.mCurrentDate.get(2);
        int day = this.mCurrentDate.get(5);
        int listPosition = -1;
        int listPositionOffset = -1;
        if (this.mCurrentView == 0) {
            listPosition = this.mDayPickerView.getMostVisiblePosition();
        } else if (this.mCurrentView == 1) {
            listPosition = this.mYearPickerView.getFirstVisiblePosition();
            listPositionOffset = this.mYearPickerView.getFirstPositionOffset();
        }
        return new SavedState(superState, year, month, day, this.mMinDate.getTimeInMillis(), this.mMaxDate.getTimeInMillis(), this.mCurrentView, listPosition, listPositionOffset, (1) null);
    }

    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        this.mCurrentDate.set(ss.getSelectedYear(), ss.getSelectedMonth(), ss.getSelectedDay());
        this.mCurrentView = ss.getCurrentView();
        this.mMinDate.setTimeInMillis(ss.getMinDate());
        this.mMaxDate.setTimeInMillis(ss.getMaxDate());
        updateDisplay(false);
        setCurrentView(this.mCurrentView);
        int listPosition = ss.getListPosition();
        if (listPosition == -1) {
            return;
        }
        if (this.mCurrentView == 0) {
            this.mDayPickerView.postSetSelection(listPosition);
        } else if (this.mCurrentView == 1) {
            this.mYearPickerView.postSetSelectionFromTop(listPosition, ss.getListPositionOffset());
        }
    }

    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        onPopulateAccessibilityEvent(event);
        return true;
    }

    public void onPopulateAccessibilityEvent(AccessibilityEvent event) {
        event.getText().add(this.mCurrentDate.getTime().toString());
    }

    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        event.setClassName(DatePicker.class.getName());
    }

    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        info.setClassName(DatePicker.class.getName());
    }

    public void onYearSelected(int year) {
        adjustDayInMonthIfNeeded(this.mCurrentDate.get(2), year);
        this.mCurrentDate.set(1, year);
        updatePickers();
        setCurrentView(0);
        updateDisplay(true);
    }

    private void adjustDayInMonthIfNeeded(int month, int year) {
        int day = this.mCurrentDate.get(5);
        int daysInMonth = getDaysInMonth(month, year);
        if (day > daysInMonth) {
            this.mCurrentDate.set(5, daysInMonth);
        }
    }

    public static int getDaysInMonth(int month, int year) {
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

    public void onDayOfMonthSelected(int year, int month, int day) {
        this.mCurrentDate.set(1, year);
        this.mCurrentDate.set(2, month);
        this.mCurrentDate.set(5, day);
        updatePickers();
        updateDisplay(true);
    }

    private void updatePickers() {
        Iterator i$ = this.mListeners.iterator();
        while (i$.hasNext()) {
            i$.next().onDateChanged();
        }
    }

    public void registerOnDateChangedListener(OnDateChangedListener listener) {
        this.mListeners.add(listener);
    }

    public void unregisterOnDateChangedListener(OnDateChangedListener listener) {
        this.mListeners.remove(listener);
    }

    public Calendar getSelectedDay() {
        return this.mCurrentDate;
    }

    public void tryVibrate() {
        this.mDelegator.performHapticFeedback(5);
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View v) {
        tryVibrate();
        if (v.getId() == 16909038) {
            setCurrentView(1);
        } else if (v.getId() == 16909035) {
            setCurrentView(0);
        }
    }
}

package android.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Parcelable;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.RadialTimePickerView;
import android.widget.RelativeLayout;
import android.widget.TimePicker;
import com.android.internal.R;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;

/* access modifiers changed from: package-private */
public class TimePickerSpinnerDelegate extends TimePicker.AbstractTimePickerDelegate implements RadialTimePickerView.OnValueSelectedListener {
    private static final int AM = 0;
    private static final int AMPM_INDEX = 2;
    private static final boolean DEFAULT_ENABLED_STATE = true;
    private static final int ENABLE_PICKER_INDEX = 3;
    private static final int HOURS_IN_HALF_DAY = 12;
    private static final int HOUR_INDEX = 0;
    private static final int MINUTE_INDEX = 1;
    private static final int PM = 1;
    private static final String TAG = "TimePickerDelegate";
    private boolean mAllowAutoAdvance;
    private int mAmKeyCode;
    private TextView mAmPmTextView;
    private String mAmText;
    private String mDeletedKeyFormat;
    private String mDoublePlaceholderText;
    private final View.OnFocusChangeListener mFocusListener = new 5(this);
    private View mHeaderView;
    private String mHourPickerDescription;
    private TextView mHourView;
    private boolean mInKbMode;
    private int mInitialHourOfDay;
    private int mInitialMinute;
    private boolean mIs24HourView;
    private boolean mIsEnabled = true;
    private final View.OnKeyListener mKeyListener = new View.OnKeyListener() {
        /* class android.widget.TimePickerSpinnerDelegate.AnonymousClass4 */

        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == 1) {
                return TimePickerSpinnerDelegate.this.processKeyUp(keyCode);
            }
            return false;
        }
    };
    private Node mLegalTimesTree;
    private String mMinutePickerDescription;
    private TextView mMinuteView;
    private char mPlaceholderText;
    private int mPmKeyCode;
    private String mPmText;
    private RadialTimePickerView mRadialTimePickerView;
    private String mSelectHours;
    private String mSelectMinutes;
    private TextView mSeparatorView;
    private Calendar mTempCalendar;
    private ArrayList<Integer> mTypedTimes = new ArrayList<>();

    public TimePickerSpinnerDelegate(TimePicker delegator, Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(delegator, context);
        TypedArray a = this.mContext.obtainStyledAttributes(attrs, R.styleable.TimePicker, defStyleAttr, defStyleRes);
        Resources res = this.mContext.getResources();
        this.mHourPickerDescription = res.getString(R.string.hour_picker_description);
        this.mSelectHours = res.getString(R.string.select_hours);
        this.mMinutePickerDescription = res.getString(R.string.minute_picker_description);
        this.mSelectMinutes = res.getString(R.string.select_minutes);
        String[] amPmStrings = TimePickerClockDelegate.getAmPmStrings(context);
        this.mAmText = amPmStrings[0];
        this.mPmText = amPmStrings[1];
        View mainView = ((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate(a.getResourceId(9, R.layout.time_picker_holo), (ViewGroup) null);
        this.mDelegator.addView(mainView);
        this.mHourView = (TextView) mainView.findViewById(R.id.hours);
        this.mSeparatorView = (TextView) mainView.findViewById(R.id.separator);
        this.mMinuteView = (TextView) mainView.findViewById(R.id.minutes);
        this.mAmPmTextView = (TextView) mainView.findViewById(R.id.ampm_label);
        int headerTimeTextAppearance = a.getResourceId(1, 0);
        if (headerTimeTextAppearance != 0) {
            this.mHourView.setTextAppearance(context, headerTimeTextAppearance);
            this.mSeparatorView.setTextAppearance(context, headerTimeTextAppearance);
            this.mMinuteView.setTextAppearance(context, headerTimeTextAppearance);
        }
        int headerSelectedTextColor = a.getColor(11, res.getColor(R.color.timepicker_default_selector_color_material));
        this.mHourView.setTextColor(ColorStateList.addFirstIfMissing(this.mHourView.getTextColors(), 16842913, headerSelectedTextColor));
        this.mMinuteView.setTextColor(ColorStateList.addFirstIfMissing(this.mMinuteView.getTextColors(), 16842913, headerSelectedTextColor));
        int headerAmPmTextAppearance = a.getResourceId(2, 0);
        if (headerAmPmTextAppearance != 0) {
            this.mAmPmTextView.setTextAppearance(context, headerAmPmTextAppearance);
        }
        this.mHeaderView = mainView.findViewById(R.id.time_header);
        this.mHeaderView.setBackground(a.getDrawable(0));
        a.recycle();
        this.mRadialTimePickerView = (RadialTimePickerView) mainView.findViewById(R.id.radial_picker);
        setupListeners();
        this.mAllowAutoAdvance = true;
        this.mDoublePlaceholderText = res.getString(R.string.time_placeholder);
        this.mDeletedKeyFormat = res.getString(R.string.deleted_key);
        this.mPlaceholderText = this.mDoublePlaceholderText.charAt(0);
        this.mPmKeyCode = -1;
        this.mAmKeyCode = -1;
        generateLegalTimesTree();
        Calendar calendar = Calendar.getInstance(this.mCurrentLocale);
        initialize(calendar.get(11), calendar.get(12), false, 0);
    }

    private void initialize(int hourOfDay, int minute, boolean is24HourView, int index) {
        this.mInitialHourOfDay = hourOfDay;
        this.mInitialMinute = minute;
        this.mIs24HourView = is24HourView;
        this.mInKbMode = false;
        updateUI(index);
    }

    private void setupListeners() {
        this.mHeaderView.setOnKeyListener(this.mKeyListener);
        this.mHeaderView.setOnFocusChangeListener(this.mFocusListener);
        this.mHeaderView.setFocusable(true);
        this.mRadialTimePickerView.setOnValueSelectedListener(this);
        this.mHourView.setOnClickListener(new View.OnClickListener() {
            /* class android.widget.TimePickerSpinnerDelegate.AnonymousClass1 */

            public void onClick(View v) {
                TimePickerSpinnerDelegate.this.setCurrentItemShowing(0, true, true);
                TimePickerSpinnerDelegate.this.tryVibrate();
            }
        });
        this.mMinuteView.setOnClickListener(new 2(this));
    }

    private void updateUI(int index) {
        updateRadialPicker(index);
        updateHeaderAmPm();
        updateHeaderHour(this.mInitialHourOfDay, true);
        updateHeaderSeparator();
        updateHeaderMinute(this.mInitialMinute);
        this.mDelegator.invalidate();
    }

    private void updateRadialPicker(int index) {
        this.mRadialTimePickerView.initialize(this.mInitialHourOfDay, this.mInitialMinute, this.mIs24HourView);
        setCurrentItemShowing(index, false, true);
    }

    private int computeMaxWidthOfNumbers(int max) {
        TextView tempView = new TextView(this.mContext);
        tempView.setTextAppearance(this.mContext, R.style.TextAppearance_Material_TimePicker_TimeLabel);
        tempView.setLayoutParams(new ViewGroup.LayoutParams(-2, -2));
        int maxWidth = 0;
        for (int minutes = 0; minutes < max; minutes++) {
            tempView.setText(String.format("%02d", Integer.valueOf(minutes)));
            tempView.measure(0, 0);
            maxWidth = Math.max(maxWidth, tempView.getMeasuredWidth());
        }
        return maxWidth;
    }

    private void updateHeaderAmPm() {
        int i = 0;
        if (this.mIs24HourView) {
            this.mAmPmTextView.setVisibility(8);
            return;
        }
        this.mAmPmTextView.setVisibility(0);
        boolean amPmOnLeft = DateFormat.getBestDateTimePattern(this.mCurrentLocale, "hm").startsWith("a");
        if (TextUtils.getLayoutDirectionFromLocale(this.mCurrentLocale) == 1) {
            if (!amPmOnLeft) {
                amPmOnLeft = true;
            } else {
                amPmOnLeft = false;
            }
        }
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.mAmPmTextView.getLayoutParams();
        if (amPmOnLeft) {
            layoutParams.rightMargin = computeMaxWidthOfNumbers(12);
            layoutParams.removeRule(1);
            layoutParams.addRule(0, R.id.separator);
        } else {
            layoutParams.leftMargin = computeMaxWidthOfNumbers(60);
            layoutParams.removeRule(0);
            layoutParams.addRule(1, R.id.separator);
        }
        if (this.mInitialHourOfDay >= 12) {
            i = 1;
        }
        updateAmPmDisplay(i);
        this.mAmPmTextView.setOnClickListener(new View.OnClickListener() {
            /* class android.widget.TimePickerSpinnerDelegate.AnonymousClass3 */

            public void onClick(View v) {
                TimePickerSpinnerDelegate.this.tryVibrate();
                int amOrPm = TimePickerSpinnerDelegate.this.mRadialTimePickerView.getAmOrPm();
                if (amOrPm == 0) {
                    amOrPm = 1;
                } else if (amOrPm == 1) {
                    amOrPm = 0;
                }
                TimePickerSpinnerDelegate.this.updateAmPmDisplay(amOrPm);
                TimePickerSpinnerDelegate.this.mRadialTimePickerView.setAmOrPm(amOrPm);
            }
        });
    }

    public void setCurrentHour(Integer currentHour) {
        int i = 1;
        if (this.mInitialHourOfDay != currentHour.intValue()) {
            this.mInitialHourOfDay = currentHour.intValue();
            updateHeaderHour(currentHour.intValue(), true);
            updateHeaderAmPm();
            this.mRadialTimePickerView.setCurrentHour(currentHour.intValue());
            RadialTimePickerView radialTimePickerView = this.mRadialTimePickerView;
            if (this.mInitialHourOfDay < 12) {
                i = 0;
            }
            radialTimePickerView.setAmOrPm(i);
            this.mDelegator.invalidate();
            onTimeChanged();
        }
    }

    public Integer getCurrentHour() {
        int currentHour = this.mRadialTimePickerView.getCurrentHour();
        if (this.mIs24HourView) {
            return Integer.valueOf(currentHour);
        }
        switch (this.mRadialTimePickerView.getAmOrPm()) {
            case 1:
                return Integer.valueOf((currentHour % 12) + 12);
            default:
                return Integer.valueOf(currentHour % 12);
        }
    }

    public void setCurrentMinute(Integer currentMinute) {
        if (this.mInitialMinute != currentMinute.intValue()) {
            this.mInitialMinute = currentMinute.intValue();
            updateHeaderMinute(currentMinute.intValue());
            this.mRadialTimePickerView.setCurrentMinute(currentMinute.intValue());
            this.mDelegator.invalidate();
            onTimeChanged();
        }
    }

    public Integer getCurrentMinute() {
        return Integer.valueOf(this.mRadialTimePickerView.getCurrentMinute());
    }

    public void setIs24HourView(Boolean is24HourView) {
        if (is24HourView.booleanValue() != this.mIs24HourView) {
            this.mIs24HourView = is24HourView.booleanValue();
            generateLegalTimesTree();
            int hour = this.mRadialTimePickerView.getCurrentHour();
            this.mInitialHourOfDay = hour;
            updateHeaderHour(hour, false);
            updateHeaderAmPm();
            updateRadialPicker(this.mRadialTimePickerView.getCurrentItemShowing());
            this.mDelegator.invalidate();
        }
    }

    public boolean is24HourView() {
        return this.mIs24HourView;
    }

    public void setOnTimeChangedListener(TimePicker.OnTimeChangedListener callback) {
        this.mOnTimeChangedListener = callback;
    }

    public void setEnabled(boolean enabled) {
        this.mHourView.setEnabled(enabled);
        this.mMinuteView.setEnabled(enabled);
        this.mAmPmTextView.setEnabled(enabled);
        this.mRadialTimePickerView.setEnabled(enabled);
        this.mIsEnabled = enabled;
    }

    public boolean isEnabled() {
        return this.mIsEnabled;
    }

    public int getBaseline() {
        return -1;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        updateUI(this.mRadialTimePickerView.getCurrentItemShowing());
    }

    public Parcelable onSaveInstanceState(Parcelable superState) {
        return new SavedState(superState, getCurrentHour().intValue(), getCurrentMinute().intValue(), is24HourView(), inKbMode(), getTypedTimes(), getCurrentItemShowing(), (AnonymousClass1) null);
    }

    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        setInKbMode(ss.inKbMode());
        setTypedTimes(ss.getTypesTimes());
        initialize(ss.getHour(), ss.getMinute(), ss.is24HourMode(), ss.getCurrentItemShowing());
        this.mRadialTimePickerView.invalidate();
        if (this.mInKbMode) {
            tryStartingKbMode(-1);
            this.mHourView.invalidate();
        }
    }

    @Override // android.widget.TimePicker.AbstractTimePickerDelegate
    public void setCurrentLocale(Locale locale) {
        super.setCurrentLocale(locale);
        this.mTempCalendar = Calendar.getInstance(locale);
    }

    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        onPopulateAccessibilityEvent(event);
        return true;
    }

    public void onPopulateAccessibilityEvent(AccessibilityEvent event) {
        int flags;
        if (this.mIs24HourView) {
            flags = 1 | 128;
        } else {
            flags = 1 | 64;
        }
        this.mTempCalendar.set(11, getCurrentHour().intValue());
        this.mTempCalendar.set(12, getCurrentMinute().intValue());
        event.getText().add(DateUtils.formatDateTime(this.mContext, this.mTempCalendar.getTimeInMillis(), flags));
    }

    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        event.setClassName(TimePicker.class.getName());
    }

    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        info.setClassName(TimePicker.class.getName());
    }

    private void setInKbMode(boolean inKbMode) {
        this.mInKbMode = inKbMode;
    }

    private boolean inKbMode() {
        return this.mInKbMode;
    }

    private void setTypedTimes(ArrayList<Integer> typeTimes) {
        this.mTypedTimes = typeTimes;
    }

    private ArrayList<Integer> getTypedTimes() {
        return this.mTypedTimes;
    }

    private int getCurrentItemShowing() {
        return this.mRadialTimePickerView.getCurrentItemShowing();
    }

    private void onTimeChanged() {
        this.mDelegator.sendAccessibilityEvent(4);
        if (this.mOnTimeChangedListener != null) {
            this.mOnTimeChangedListener.onTimeChanged(this.mDelegator, getCurrentHour().intValue(), getCurrentMinute().intValue());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void tryVibrate() {
        this.mDelegator.performHapticFeedback(4);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateAmPmDisplay(int amOrPm) {
        if (amOrPm == 0) {
            this.mAmPmTextView.setText(this.mAmText);
            this.mRadialTimePickerView.announceForAccessibility(this.mAmText);
        } else if (amOrPm == 1) {
            this.mAmPmTextView.setText(this.mPmText);
            this.mRadialTimePickerView.announceForAccessibility(this.mPmText);
        } else {
            this.mAmPmTextView.setText(this.mDoublePlaceholderText);
        }
    }

    @Override // android.widget.RadialTimePickerView.OnValueSelectedListener
    public void onValueSelected(int pickerIndex, int newValue, boolean autoAdvance) {
        if (pickerIndex == 0) {
            updateHeaderHour(newValue, false);
            String announcement = String.format("%d", Integer.valueOf(newValue));
            if (!this.mAllowAutoAdvance || !autoAdvance) {
                this.mRadialTimePickerView.setContentDescription(this.mHourPickerDescription + ": " + newValue);
            } else {
                setCurrentItemShowing(1, true, false);
                announcement = announcement + ". " + this.mSelectMinutes;
            }
            this.mRadialTimePickerView.announceForAccessibility(announcement);
        } else if (pickerIndex == 1) {
            updateHeaderMinute(newValue);
            this.mRadialTimePickerView.setContentDescription(this.mMinutePickerDescription + ": " + newValue);
        } else if (pickerIndex == 2) {
            updateAmPmDisplay(newValue);
        } else if (pickerIndex == 3) {
            if (!isTypedTimeFullyLegal()) {
                this.mTypedTimes.clear();
            }
            finishKbMode();
        }
    }

    private void updateHeaderHour(int value, boolean announce) {
        String format;
        char c;
        String bestDateTimePattern = DateFormat.getBestDateTimePattern(this.mCurrentLocale, this.mIs24HourView ? "Hm" : "hm");
        int lengthPattern = bestDateTimePattern.length();
        boolean hourWithTwoDigit = false;
        char hourFormat = 0;
        int i = 0;
        while (true) {
            if (i >= lengthPattern) {
                break;
            }
            c = bestDateTimePattern.charAt(i);
            if (c == 'H' || c == 'h' || c == 'K' || c == 'k') {
                hourFormat = c;
            } else {
                i++;
            }
        }
        hourFormat = c;
        if (i + 1 < lengthPattern && c == bestDateTimePattern.charAt(i + 1)) {
            hourWithTwoDigit = true;
        }
        if (hourWithTwoDigit) {
            format = "%02d";
        } else {
            format = "%d";
        }
        if (!this.mIs24HourView) {
            value = modulo12(value, hourFormat == 'K');
        } else if (hourFormat == 'k' && value == 0) {
            value = 24;
        }
        CharSequence text = String.format(format, Integer.valueOf(value));
        this.mHourView.setText(text);
        if (announce) {
            this.mRadialTimePickerView.announceForAccessibility(text);
        }
    }

    private static int modulo12(int n, boolean startWithZero) {
        int value = n % 12;
        if (value != 0 || startWithZero) {
            return value;
        }
        return 12;
    }

    private void updateHeaderSeparator() {
        String separatorText;
        String bestDateTimePattern = DateFormat.getBestDateTimePattern(this.mCurrentLocale, this.mIs24HourView ? "Hm" : "hm");
        int hIndex = lastIndexOfAny(bestDateTimePattern, new char[]{'H', 'h', 'K', 'k'});
        if (hIndex == -1) {
            separatorText = ":";
        } else {
            separatorText = Character.toString(bestDateTimePattern.charAt(hIndex + 1));
        }
        this.mSeparatorView.setText(separatorText);
    }

    private static int lastIndexOfAny(String str, char[] any) {
        int lengthAny = any.length;
        if (lengthAny > 0) {
            for (int i = str.length() - 1; i >= 0; i--) {
                char c = str.charAt(i);
                for (char c2 : any) {
                    if (c == c2) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    private void updateHeaderMinute(int value) {
        if (value == 60) {
            value = 0;
        }
        CharSequence text = String.format(this.mCurrentLocale, "%02d", Integer.valueOf(value));
        this.mRadialTimePickerView.announceForAccessibility(text);
        this.mMinuteView.setText(text);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setCurrentItemShowing(int index, boolean animateCircle, boolean announce) {
        boolean z;
        boolean z2 = true;
        this.mRadialTimePickerView.setCurrentItemShowing(index, animateCircle);
        if (index == 0) {
            int hours = this.mRadialTimePickerView.getCurrentHour();
            if (!this.mIs24HourView) {
                hours %= 12;
            }
            this.mRadialTimePickerView.setContentDescription(this.mHourPickerDescription + ": " + hours);
            if (announce) {
                this.mRadialTimePickerView.announceForAccessibility(this.mSelectHours);
            }
        } else {
            this.mRadialTimePickerView.setContentDescription(this.mMinutePickerDescription + ": " + this.mRadialTimePickerView.getCurrentMinute());
            if (announce) {
                this.mRadialTimePickerView.announceForAccessibility(this.mSelectMinutes);
            }
        }
        TextView textView = this.mHourView;
        if (index == 0) {
            z = true;
        } else {
            z = false;
        }
        textView.setSelected(z);
        TextView textView2 = this.mMinuteView;
        if (index != 1) {
            z2 = false;
        }
        textView2.setSelected(z2);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean processKeyUp(int keyCode) {
        String deletedKeyStr;
        if (keyCode == 67) {
            if (this.mInKbMode && !this.mTypedTimes.isEmpty()) {
                int deleted = deleteLastTypedKey();
                if (deleted == getAmOrPmKeyCode(0)) {
                    deletedKeyStr = this.mAmText;
                } else if (deleted == getAmOrPmKeyCode(1)) {
                    deletedKeyStr = this.mPmText;
                } else {
                    deletedKeyStr = String.format("%d", Integer.valueOf(getValFromKeyCode(deleted)));
                }
                this.mRadialTimePickerView.announceForAccessibility(String.format(this.mDeletedKeyFormat, deletedKeyStr));
                updateDisplay(true);
            }
        } else if (keyCode == 7 || keyCode == 8 || keyCode == 9 || keyCode == 10 || keyCode == 11 || keyCode == 12 || keyCode == 13 || keyCode == 14 || keyCode == 15 || keyCode == 16 || (!this.mIs24HourView && (keyCode == getAmOrPmKeyCode(0) || keyCode == getAmOrPmKeyCode(1)))) {
            if (!this.mInKbMode) {
                if (this.mRadialTimePickerView == null) {
                    Log.e(TAG, "Unable to initiate keyboard mode, TimePicker was null.");
                    return true;
                }
                this.mTypedTimes.clear();
                tryStartingKbMode(keyCode);
                return true;
            } else if (!addKeyIfLegal(keyCode)) {
                return true;
            } else {
                updateDisplay(false);
                return true;
            }
        }
        return false;
    }

    private void tryStartingKbMode(int keyCode) {
        if (keyCode == -1 || addKeyIfLegal(keyCode)) {
            this.mInKbMode = true;
            onValidationChanged(false);
            updateDisplay(false);
            this.mRadialTimePickerView.setInputEnabled(false);
        }
    }

    private boolean addKeyIfLegal(int keyCode) {
        if (this.mIs24HourView && this.mTypedTimes.size() == 4) {
            return false;
        }
        if (!this.mIs24HourView && isTypedTimeFullyLegal()) {
            return false;
        }
        this.mTypedTimes.add(Integer.valueOf(keyCode));
        if (!isTypedTimeLegalSoFar()) {
            deleteLastTypedKey();
            return false;
        }
        int val = getValFromKeyCode(keyCode);
        this.mRadialTimePickerView.announceForAccessibility(String.format("%d", Integer.valueOf(val)));
        if (isTypedTimeFullyLegal()) {
            if (!this.mIs24HourView && this.mTypedTimes.size() <= 3) {
                this.mTypedTimes.add(this.mTypedTimes.size() - 1, 7);
                this.mTypedTimes.add(this.mTypedTimes.size() - 1, 7);
            }
            onValidationChanged(true);
        }
        return true;
    }

    private boolean isTypedTimeLegalSoFar() {
        Node node = this.mLegalTimesTree;
        Iterator i$ = this.mTypedTimes.iterator();
        while (i$.hasNext()) {
            node = node.canReach(i$.next().intValue());
            if (node == null) {
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: private */
    public boolean isTypedTimeFullyLegal() {
        boolean z = false;
        if (this.mIs24HourView) {
            int[] values = getEnteredTime(null);
            return values[0] >= 0 && values[1] >= 0 && values[1] < 60;
        }
        if (this.mTypedTimes.contains(Integer.valueOf(getAmOrPmKeyCode(0))) || this.mTypedTimes.contains(Integer.valueOf(getAmOrPmKeyCode(1)))) {
            z = true;
        }
        return z;
    }

    private int deleteLastTypedKey() {
        int deleted = this.mTypedTimes.remove(this.mTypedTimes.size() - 1).intValue();
        if (!isTypedTimeFullyLegal()) {
            onValidationChanged(false);
        }
        return deleted;
    }

    /* access modifiers changed from: private */
    public void finishKbMode() {
        this.mInKbMode = false;
        if (!this.mTypedTimes.isEmpty()) {
            int[] values = getEnteredTime(null);
            this.mRadialTimePickerView.setCurrentHour(values[0]);
            this.mRadialTimePickerView.setCurrentMinute(values[1]);
            if (!this.mIs24HourView) {
                this.mRadialTimePickerView.setAmOrPm(values[2]);
            }
            this.mTypedTimes.clear();
        }
        updateDisplay(false);
        this.mRadialTimePickerView.setInputEnabled(true);
    }

    private void updateDisplay(boolean allowEmptyDisplay) {
        String minuteStr;
        if (allowEmptyDisplay || !this.mTypedTimes.isEmpty()) {
            boolean[] enteredZeros = {false, false};
            int[] values = getEnteredTime(enteredZeros);
            String hourFormat = enteredZeros[0] ? "%02d" : "%2d";
            String minuteFormat = enteredZeros[1] ? "%02d" : "%2d";
            String hourStr = values[0] == -1 ? this.mDoublePlaceholderText : String.format(hourFormat, Integer.valueOf(values[0])).replace(' ', this.mPlaceholderText);
            if (values[1] == -1) {
                minuteStr = this.mDoublePlaceholderText;
            } else {
                minuteStr = String.format(minuteFormat, Integer.valueOf(values[1])).replace(' ', this.mPlaceholderText);
            }
            this.mHourView.setText(hourStr);
            this.mHourView.setSelected(false);
            this.mMinuteView.setText(minuteStr);
            this.mMinuteView.setSelected(false);
            if (!this.mIs24HourView) {
                updateAmPmDisplay(values[2]);
                return;
            }
            return;
        }
        int hour = this.mRadialTimePickerView.getCurrentHour();
        int minute = this.mRadialTimePickerView.getCurrentMinute();
        updateHeaderHour(hour, true);
        updateHeaderMinute(minute);
        if (!this.mIs24HourView) {
            updateAmPmDisplay(hour < 12 ? 0 : 1);
        }
        setCurrentItemShowing(this.mRadialTimePickerView.getCurrentItemShowing(), true, true);
        onValidationChanged(true);
    }

    private int getValFromKeyCode(int keyCode) {
        switch (keyCode) {
            case 7:
                return 0;
            case 8:
                return 1;
            case 9:
                return 2;
            case 10:
                return 3;
            case 11:
                return 4;
            case 12:
                return 5;
            case 13:
                return 6;
            case 14:
                return 7;
            case 15:
                return 8;
            case 16:
                return 9;
            default:
                return -1;
        }
    }

    private int[] getEnteredTime(boolean[] enteredZeros) {
        int amOrPm = -1;
        int startIndex = 1;
        if (!this.mIs24HourView && isTypedTimeFullyLegal()) {
            int keyCode = this.mTypedTimes.get(this.mTypedTimes.size() - 1).intValue();
            if (keyCode == getAmOrPmKeyCode(0)) {
                amOrPm = 0;
            } else if (keyCode == getAmOrPmKeyCode(1)) {
                amOrPm = 1;
            }
            startIndex = 2;
        }
        int minute = -1;
        int hour = -1;
        for (int i = startIndex; i <= this.mTypedTimes.size(); i++) {
            int val = getValFromKeyCode(this.mTypedTimes.get(this.mTypedTimes.size() - i).intValue());
            if (i == startIndex) {
                minute = val;
            } else if (i == startIndex + 1) {
                minute += val * 10;
                if (enteredZeros != null && val == 0) {
                    enteredZeros[1] = true;
                }
            } else if (i == startIndex + 2) {
                hour = val;
            } else if (i == startIndex + 3) {
                hour += val * 10;
                if (enteredZeros != null && val == 0) {
                    enteredZeros[0] = true;
                }
            }
        }
        return new int[]{hour, minute, amOrPm};
    }

    private int getAmOrPmKeyCode(int amOrPm) {
        if (this.mAmKeyCode == -1 || this.mPmKeyCode == -1) {
            KeyCharacterMap kcm = KeyCharacterMap.load(-1);
            int i = 0;
            while (true) {
                if (i >= Math.max(this.mAmText.length(), this.mPmText.length())) {
                    break;
                }
                char amChar = this.mAmText.toLowerCase(this.mCurrentLocale).charAt(i);
                char pmChar = this.mPmText.toLowerCase(this.mCurrentLocale).charAt(i);
                if (amChar != pmChar) {
                    KeyEvent[] events = kcm.getEvents(new char[]{amChar, pmChar});
                    if (events == null || events.length != 4) {
                        Log.e(TAG, "Unable to find keycodes for AM and PM.");
                    } else {
                        this.mAmKeyCode = events[0].getKeyCode();
                        this.mPmKeyCode = events[2].getKeyCode();
                    }
                } else {
                    i++;
                }
            }
        }
        if (amOrPm == 0) {
            return this.mAmKeyCode;
        }
        if (amOrPm == 1) {
            return this.mPmKeyCode;
        }
        return -1;
    }

    private void generateLegalTimesTree() {
        this.mLegalTimesTree = new Node(new int[0]);
        if (this.mIs24HourView) {
            Node minuteFirstDigit = new Node(7, 8, 9, 10, 11, 12);
            Node minuteSecondDigit = new Node(7, 8, 9, 10, 11, 12, 13, 14, 15, 16);
            minuteFirstDigit.addChild(minuteSecondDigit);
            Node firstDigit = new Node(7, 8);
            this.mLegalTimesTree.addChild(firstDigit);
            Node secondDigit = new Node(7, 8, 9, 10, 11, 12);
            firstDigit.addChild(secondDigit);
            secondDigit.addChild(minuteFirstDigit);
            secondDigit.addChild(new Node(13, 14, 15, 16));
            Node secondDigit2 = new Node(13, 14, 15, 16);
            firstDigit.addChild(secondDigit2);
            secondDigit2.addChild(minuteFirstDigit);
            Node firstDigit2 = new Node(9);
            this.mLegalTimesTree.addChild(firstDigit2);
            Node secondDigit3 = new Node(7, 8, 9, 10);
            firstDigit2.addChild(secondDigit3);
            secondDigit3.addChild(minuteFirstDigit);
            Node secondDigit4 = new Node(11, 12);
            firstDigit2.addChild(secondDigit4);
            secondDigit4.addChild(minuteSecondDigit);
            Node firstDigit3 = new Node(10, 11, 12, 13, 14, 15, 16);
            this.mLegalTimesTree.addChild(firstDigit3);
            firstDigit3.addChild(minuteFirstDigit);
            return;
        }
        Node ampm = new Node(getAmOrPmKeyCode(0), getAmOrPmKeyCode(1));
        Node firstDigit4 = new Node(8);
        this.mLegalTimesTree.addChild(firstDigit4);
        firstDigit4.addChild(ampm);
        Node secondDigit5 = new Node(7, 8, 9);
        firstDigit4.addChild(secondDigit5);
        secondDigit5.addChild(ampm);
        Node thirdDigit = new Node(7, 8, 9, 10, 11, 12);
        secondDigit5.addChild(thirdDigit);
        thirdDigit.addChild(ampm);
        Node fourthDigit = new Node(7, 8, 9, 10, 11, 12, 13, 14, 15, 16);
        thirdDigit.addChild(fourthDigit);
        fourthDigit.addChild(ampm);
        Node thirdDigit2 = new Node(13, 14, 15, 16);
        secondDigit5.addChild(thirdDigit2);
        thirdDigit2.addChild(ampm);
        Node secondDigit6 = new Node(10, 11, 12);
        firstDigit4.addChild(secondDigit6);
        Node thirdDigit3 = new Node(7, 8, 9, 10, 11, 12, 13, 14, 15, 16);
        secondDigit6.addChild(thirdDigit3);
        thirdDigit3.addChild(ampm);
        Node firstDigit5 = new Node(9, 10, 11, 12, 13, 14, 15, 16);
        this.mLegalTimesTree.addChild(firstDigit5);
        firstDigit5.addChild(ampm);
        Node secondDigit7 = new Node(7, 8, 9, 10, 11, 12);
        firstDigit5.addChild(secondDigit7);
        Node thirdDigit4 = new Node(7, 8, 9, 10, 11, 12, 13, 14, 15, 16);
        secondDigit7.addChild(thirdDigit4);
        thirdDigit4.addChild(ampm);
    }

    /* access modifiers changed from: private */
    public class Node {
        private ArrayList<Node> mChildren = new ArrayList<>();
        private int[] mLegalKeys;

        public Node(int... legalKeys) {
            this.mLegalKeys = legalKeys;
        }

        public void addChild(Node child) {
            this.mChildren.add(child);
        }

        public boolean containsKey(int key) {
            for (int i = 0; i < this.mLegalKeys.length; i++) {
                if (this.mLegalKeys[i] == key) {
                    return true;
                }
            }
            return false;
        }

        public Node canReach(int key) {
            if (this.mChildren == null) {
                return null;
            }
            Iterator i$ = this.mChildren.iterator();
            while (i$.hasNext()) {
                Node child = i$.next();
                if (child.containsKey(key)) {
                    return child;
                }
            }
            return null;
        }
    }
}

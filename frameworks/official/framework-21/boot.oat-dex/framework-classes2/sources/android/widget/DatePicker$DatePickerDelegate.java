package android.widget;

import android.content.res.Configuration;
import android.os.Parcelable;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.Calendar;

interface DatePicker$DatePickerDelegate {
    boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent accessibilityEvent);

    CalendarView getCalendarView();

    boolean getCalendarViewShown();

    int getDayOfMonth();

    int getFirstDayOfWeek();

    Calendar getMaxDate();

    Calendar getMinDate();

    int getMonth();

    boolean getSpinnersShown();

    int getYear();

    void init(int i, int i2, int i3, DatePicker$OnDateChangedListener datePicker$OnDateChangedListener);

    boolean isEnabled();

    void onConfigurationChanged(Configuration configuration);

    void onInitializeAccessibilityEvent(AccessibilityEvent accessibilityEvent);

    void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo);

    void onPopulateAccessibilityEvent(AccessibilityEvent accessibilityEvent);

    void onRestoreInstanceState(Parcelable parcelable);

    Parcelable onSaveInstanceState(Parcelable parcelable);

    void setCalendarViewShown(boolean z);

    void setEnabled(boolean z);

    void setFirstDayOfWeek(int i);

    void setMaxDate(long j);

    void setMinDate(long j);

    void setSpinnersShown(boolean z);

    void setValidationCallback(DatePicker$ValidationCallback datePicker$ValidationCallback);

    void updateDate(int i, int i2, int i3);
}

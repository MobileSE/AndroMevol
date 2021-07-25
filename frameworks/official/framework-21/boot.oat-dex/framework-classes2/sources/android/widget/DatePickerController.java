package android.widget;

import java.util.Calendar;

/* access modifiers changed from: package-private */
public interface DatePickerController {
    int getFirstDayOfWeek();

    Calendar getMaxDate();

    int getMaxDay();

    int getMaxMonth();

    int getMaxYear();

    Calendar getMinDate();

    int getMinDay();

    int getMinMonth();

    int getMinYear();

    Calendar getSelectedDay();

    void onDayOfMonthSelected(int i, int i2, int i3);

    void onYearSelected(int i);

    void registerOnDateChangedListener(OnDateChangedListener onDateChangedListener);

    void setFirstDayOfWeek(int i);

    void setMaxDate(long j);

    void setMinDate(long j);

    void tryVibrate();

    void unregisterOnDateChangedListener(OnDateChangedListener onDateChangedListener);
}

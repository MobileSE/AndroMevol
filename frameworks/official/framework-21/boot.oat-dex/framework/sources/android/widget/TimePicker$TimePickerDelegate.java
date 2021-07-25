package android.widget;

import android.content.res.Configuration;
import android.os.Parcelable;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.TimePicker;

interface TimePicker$TimePickerDelegate {
    boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent accessibilityEvent);

    int getBaseline();

    Integer getCurrentHour();

    Integer getCurrentMinute();

    boolean is24HourView();

    boolean isEnabled();

    void onConfigurationChanged(Configuration configuration);

    void onInitializeAccessibilityEvent(AccessibilityEvent accessibilityEvent);

    void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo);

    void onPopulateAccessibilityEvent(AccessibilityEvent accessibilityEvent);

    void onRestoreInstanceState(Parcelable parcelable);

    Parcelable onSaveInstanceState(Parcelable parcelable);

    void setCurrentHour(Integer num);

    void setCurrentMinute(Integer num);

    void setEnabled(boolean z);

    void setIs24HourView(Boolean bool);

    void setOnTimeChangedListener(TimePicker$OnTimeChangedListener timePicker$OnTimeChangedListener);

    void setValidationCallback(TimePicker.ValidationCallback validationCallback);
}

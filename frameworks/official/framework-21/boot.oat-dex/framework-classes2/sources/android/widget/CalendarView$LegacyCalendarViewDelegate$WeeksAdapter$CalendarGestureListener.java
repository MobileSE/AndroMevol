package android.widget;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.CalendarView;

class CalendarView$LegacyCalendarViewDelegate$WeeksAdapter$CalendarGestureListener extends GestureDetector.SimpleOnGestureListener {
    final /* synthetic */ CalendarView.LegacyCalendarViewDelegate.WeeksAdapter this$1;

    CalendarView$LegacyCalendarViewDelegate$WeeksAdapter$CalendarGestureListener(CalendarView.LegacyCalendarViewDelegate.WeeksAdapter weeksAdapter) {
        this.this$1 = weeksAdapter;
    }

    public boolean onSingleTapUp(MotionEvent e) {
        return true;
    }
}

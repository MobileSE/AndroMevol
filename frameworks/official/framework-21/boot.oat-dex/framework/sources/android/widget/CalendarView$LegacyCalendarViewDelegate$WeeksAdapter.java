package android.widget;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import java.util.Calendar;

class CalendarView$LegacyCalendarViewDelegate$WeeksAdapter extends BaseAdapter implements View.OnTouchListener {
    private int mFocusedMonth;
    private GestureDetector mGestureDetector;
    private final Calendar mSelectedDate = Calendar.getInstance();
    private int mSelectedWeek;
    private int mTotalWeekCount;
    final /* synthetic */ CalendarView.LegacyCalendarViewDelegate this$0;

    public CalendarView$LegacyCalendarViewDelegate$WeeksAdapter(CalendarView.LegacyCalendarViewDelegate legacyCalendarViewDelegate, Context context) {
        this.this$0 = legacyCalendarViewDelegate;
        legacyCalendarViewDelegate.mContext = context;
        this.mGestureDetector = new GestureDetector(legacyCalendarViewDelegate.mContext, (GestureDetector.OnGestureListener) new CalendarGestureListener(this));
        init();
    }

    /* access modifiers changed from: private */
    public void init() {
        this.mSelectedWeek = CalendarView.LegacyCalendarViewDelegate.access$1500(this.this$0, this.mSelectedDate);
        this.mTotalWeekCount = CalendarView.LegacyCalendarViewDelegate.access$1500(this.this$0, CalendarView.LegacyCalendarViewDelegate.access$1600(this.this$0));
        if (!(CalendarView.LegacyCalendarViewDelegate.access$1700(this.this$0).get(7) == CalendarView.LegacyCalendarViewDelegate.access$1800(this.this$0) && CalendarView.LegacyCalendarViewDelegate.access$1600(this.this$0).get(7) == CalendarView.LegacyCalendarViewDelegate.access$1800(this.this$0))) {
            this.mTotalWeekCount++;
        }
        notifyDataSetChanged();
    }

    public void setSelectedDay(Calendar selectedDay) {
        if (selectedDay.get(6) != this.mSelectedDate.get(6) || selectedDay.get(1) != this.mSelectedDate.get(1)) {
            this.mSelectedDate.setTimeInMillis(selectedDay.getTimeInMillis());
            this.mSelectedWeek = CalendarView.LegacyCalendarViewDelegate.access$1500(this.this$0, this.mSelectedDate);
            this.mFocusedMonth = this.mSelectedDate.get(2);
            notifyDataSetChanged();
        }
    }

    public Calendar getSelectedDay() {
        return this.mSelectedDate;
    }

    public int getCount() {
        return this.mTotalWeekCount;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        CalendarView$LegacyCalendarViewDelegate$WeekView weekView;
        if (convertView != null) {
            weekView = (CalendarView$LegacyCalendarViewDelegate$WeekView) convertView;
        } else {
            weekView = new CalendarView$LegacyCalendarViewDelegate$WeekView(this.this$0, this.this$0.mContext);
            weekView.setLayoutParams(new AbsListView$LayoutParams(-2, -2));
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
        if (!CalendarView.LegacyCalendarViewDelegate.access$1900(this.this$0).isEnabled() || !this.mGestureDetector.onTouchEvent(event)) {
            return false;
        }
        if (!((CalendarView$LegacyCalendarViewDelegate$WeekView) v).getDayFromLocation(event.getX(), CalendarView.LegacyCalendarViewDelegate.access$2000(this.this$0)) || CalendarView.LegacyCalendarViewDelegate.access$2000(this.this$0).before(CalendarView.LegacyCalendarViewDelegate.access$1700(this.this$0)) || CalendarView.LegacyCalendarViewDelegate.access$2000(this.this$0).after(CalendarView.LegacyCalendarViewDelegate.access$1600(this.this$0))) {
            return true;
        }
        onDateTapped(CalendarView.LegacyCalendarViewDelegate.access$2000(this.this$0));
        return true;
    }

    private void onDateTapped(Calendar day) {
        setSelectedDay(day);
        CalendarView.LegacyCalendarViewDelegate.access$2100(this.this$0, day);
    }
}

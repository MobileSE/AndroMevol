package android.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.SimpleMonthView;
import java.util.Calendar;
import java.util.HashMap;

class SimpleMonthAdapter extends BaseAdapter implements SimpleMonthView.OnDayClickListener {
    private static final String TAG = "SimpleMonthAdapter";
    private ColorStateList mCalendarTextColors;
    private final Context mContext;
    private final DatePickerController mController;
    private Calendar mSelectedDay;

    public SimpleMonthAdapter(Context context, DatePickerController controller) {
        this.mContext = context;
        this.mController = controller;
        init();
        setSelectedDay(this.mController.getSelectedDay());
    }

    public void setSelectedDay(Calendar day) {
        if (this.mSelectedDay != day) {
            this.mSelectedDay = day;
            notifyDataSetChanged();
        }
    }

    /* access modifiers changed from: package-private */
    public void setCalendarTextColor(ColorStateList colors) {
        this.mCalendarTextColors = colors;
    }

    /* access modifiers changed from: protected */
    public void init() {
        this.mSelectedDay = Calendar.getInstance();
    }

    @Override // android.widget.Adapter
    public int getCount() {
        return ((this.mController.getMaxMonth() + 1) - this.mController.getMinMonth()) + ((this.mController.getMaxYear() - this.mController.getMinYear()) * 12);
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
    public boolean hasStableIds() {
        return true;
    }

    @Override // android.widget.Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        SimpleMonthView v;
        int enabledDayRangeStart;
        int enabledDayRangeEnd;
        HashMap<String, Integer> drawingParams = null;
        if (convertView != null) {
            v = (SimpleMonthView) convertView;
            drawingParams = (HashMap) v.getTag();
        } else {
            v = new SimpleMonthView(this.mContext);
            v.setLayoutParams(new AbsListView.LayoutParams(-1, -1));
            v.setClickable(true);
            v.setOnDayClickListener(this);
            if (this.mCalendarTextColors != null) {
                v.setTextColor(this.mCalendarTextColors);
            }
        }
        if (drawingParams == null) {
            new HashMap<>();
        } else {
            drawingParams.clear();
        }
        int currentMonth = position + this.mController.getMinMonth();
        int month = currentMonth % 12;
        int year = (currentMonth / 12) + this.mController.getMinYear();
        int selectedDay = -1;
        if (isSelectedDayInMonth(year, month)) {
            selectedDay = this.mSelectedDay.get(5);
        }
        v.reuse();
        if (this.mController.getMinMonth() == month && this.mController.getMinYear() == year) {
            enabledDayRangeStart = this.mController.getMinDay();
        } else {
            enabledDayRangeStart = 1;
        }
        if (this.mController.getMaxMonth() == month && this.mController.getMaxYear() == year) {
            enabledDayRangeEnd = this.mController.getMaxDay();
        } else {
            enabledDayRangeEnd = 31;
        }
        v.setMonthParams(selectedDay, month, year, this.mController.getFirstDayOfWeek(), enabledDayRangeStart, enabledDayRangeEnd);
        v.invalidate();
        return v;
    }

    private boolean isSelectedDayInMonth(int year, int month) {
        return this.mSelectedDay.get(1) == year && this.mSelectedDay.get(2) == month;
    }

    @Override // android.widget.SimpleMonthView.OnDayClickListener
    public void onDayClick(SimpleMonthView view, Calendar day) {
        if (day != null) {
            onDayTapped(day);
        }
    }

    /* access modifiers changed from: protected */
    public void onDayTapped(Calendar day) {
        this.mController.tryVibrate();
        this.mController.onDayOfMonthSelected(day.get(1), day.get(2), day.get(5));
        setSelectedDay(day);
    }
}

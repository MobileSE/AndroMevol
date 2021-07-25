package android.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.mtp.MtpConstants;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.AbsListView;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

class DayPickerView extends ListView implements AbsListView.OnScrollListener, OnDateChangedListener {
    private static final int GOTO_SCROLL_DURATION = 250;
    private static int LIST_TOP_OFFSET = -1;
    private static final int SCROLL_CHANGE_DELAY = 40;
    private static final String TAG = "DayPickerView";
    private SimpleMonthAdapter mAdapter;
    private DatePickerController mController;
    private int mCurrentMonthDisplayed;
    private int mCurrentScrollState = 0;
    private float mFriction = 1.0f;
    private boolean mPerformingScroll;
    private int mPreviousScrollState = 0;
    private ScrollStateRunnable mScrollStateChangedRunnable = new ScrollStateRunnable(this);
    private Calendar mSelectedDay = Calendar.getInstance();
    private Calendar mTempDay = Calendar.getInstance();
    private SimpleDateFormat mYearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());

    public DayPickerView(Context context, DatePickerController controller) {
        super(context);
        init();
        setController(controller);
    }

    public void setController(DatePickerController controller) {
        if (this.mController != null) {
            this.mController.unregisterOnDateChangedListener(this);
        }
        this.mController = controller;
        this.mController.registerOnDateChangedListener(this);
        setUpAdapter();
        setAdapter((ListAdapter) this.mAdapter);
        onDateChanged();
    }

    public void init() {
        setLayoutParams(new AbsListView.LayoutParams(-1, -1));
        setDrawSelectorOnTop(false);
        setUpListView();
    }

    public void onChange() {
        setUpAdapter();
        setAdapter((ListAdapter) this.mAdapter);
    }

    /* access modifiers changed from: protected */
    public void setUpAdapter() {
        if (this.mAdapter == null) {
            this.mAdapter = new SimpleMonthAdapter(getContext(), this.mController);
        } else {
            this.mAdapter.setSelectedDay(this.mSelectedDay);
            this.mAdapter.notifyDataSetChanged();
        }
        this.mAdapter.notifyDataSetChanged();
    }

    /* access modifiers changed from: protected */
    public void setUpListView() {
        setCacheColorHint(0);
        setDivider(null);
        setItemsCanFocus(true);
        setFastScrollEnabled(false);
        setVerticalScrollBarEnabled(false);
        setOnScrollListener(this);
        setFadingEdgeLength(0);
        setFriction(ViewConfiguration.getScrollFriction() * this.mFriction);
    }

    private int getDiffMonths(Calendar start, Calendar end) {
        return (end.get(2) - start.get(2)) + ((end.get(1) - start.get(1)) * 12);
    }

    private int getPositionFromDay(Calendar day) {
        int diffMonthMax = getDiffMonths(this.mController.getMinDate(), this.mController.getMaxDate());
        int diffMonth = getDiffMonths(this.mController.getMinDate(), day);
        if (diffMonth < 0) {
            return 0;
        }
        return diffMonth > diffMonthMax ? diffMonthMax : diffMonth;
    }

    public boolean goTo(Calendar day, boolean animate, boolean setSelected, boolean forceScroll) {
        View child;
        int selectedPosition;
        if (setSelected) {
            this.mSelectedDay.setTimeInMillis(day.getTimeInMillis());
        }
        this.mTempDay.setTimeInMillis(day.getTimeInMillis());
        int position = getPositionFromDay(day);
        int i = 0;
        while (true) {
            int i2 = i + 1;
            child = getChildAt(i);
            if (child != null && child.getTop() < 0) {
                i = i2;
            }
        }
        if (child != null) {
            selectedPosition = getPositionForView(child);
        } else {
            selectedPosition = 0;
        }
        if (setSelected) {
            this.mAdapter.setSelectedDay(this.mSelectedDay);
        }
        if (position != selectedPosition || forceScroll) {
            setMonthDisplayed(this.mTempDay);
            this.mPreviousScrollState = 2;
            if (animate) {
                smoothScrollToPositionFromTop(position, LIST_TOP_OFFSET, 250);
                return true;
            }
            postSetSelection(position);
        } else if (setSelected) {
            setMonthDisplayed(this.mSelectedDay);
        }
        return false;
    }

    public void postSetSelection(final int position) {
        clearFocus();
        post(new Runnable() {
            /* class android.widget.DayPickerView.AnonymousClass1 */

            public void run() {
                DayPickerView.this.setSelection(position);
            }
        });
        onScrollStateChanged(this, 0);
    }

    @Override // android.widget.AbsListView.OnScrollListener
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (((SimpleMonthView) view.getChildAt(0)) != null) {
            this.mPreviousScrollState = this.mCurrentScrollState;
        }
    }

    /* access modifiers changed from: protected */
    public void setMonthDisplayed(Calendar date) {
        if (this.mCurrentMonthDisplayed != date.get(2)) {
            this.mCurrentMonthDisplayed = date.get(2);
            invalidateViews();
        }
    }

    @Override // android.widget.AbsListView.OnScrollListener
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        this.mScrollStateChangedRunnable.doScrollStateChange(view, scrollState);
    }

    /* access modifiers changed from: package-private */
    public void setCalendarTextColor(ColorStateList colors) {
        this.mAdapter.setCalendarTextColor(colors);
    }

    /* access modifiers changed from: protected */
    public class ScrollStateRunnable implements Runnable {
        private int mNewState;
        private View mParent;

        ScrollStateRunnable(View view) {
            this.mParent = view;
        }

        public void doScrollStateChange(AbsListView view, int scrollState) {
            this.mParent.removeCallbacks(this);
            this.mNewState = scrollState;
            this.mParent.postDelayed(this, 40);
        }

        public void run() {
            boolean scroll = true;
            DayPickerView.this.mCurrentScrollState = this.mNewState;
            if (Log.isLoggable(DayPickerView.TAG, 3)) {
                Log.d(DayPickerView.TAG, "new scroll state: " + this.mNewState + " old state: " + DayPickerView.this.mPreviousScrollState);
            }
            if (this.mNewState != 0 || DayPickerView.this.mPreviousScrollState == 0 || DayPickerView.this.mPreviousScrollState == 1) {
                DayPickerView.this.mPreviousScrollState = this.mNewState;
                return;
            }
            DayPickerView.this.mPreviousScrollState = this.mNewState;
            int i = 0;
            View child = DayPickerView.this.getChildAt(0);
            while (child != null && child.getBottom() <= 0) {
                i++;
                child = DayPickerView.this.getChildAt(i);
            }
            if (child != null) {
                int firstPosition = DayPickerView.this.getFirstVisiblePosition();
                int lastPosition = DayPickerView.this.getLastVisiblePosition();
                if (firstPosition == 0 || lastPosition == DayPickerView.this.getCount() - 1) {
                    scroll = false;
                }
                int top = child.getTop();
                int bottom = child.getBottom();
                int midpoint = DayPickerView.this.getHeight() / 2;
                if (scroll && top < DayPickerView.LIST_TOP_OFFSET) {
                    if (bottom > midpoint) {
                        DayPickerView.this.smoothScrollBy(top, 250);
                    } else {
                        DayPickerView.this.smoothScrollBy(bottom, 250);
                    }
                }
            }
        }
    }

    public int getMostVisiblePosition() {
        int firstPosition = getFirstVisiblePosition();
        int height = getHeight();
        int maxDisplayedHeight = 0;
        int mostVisibleIndex = 0;
        int i = 0;
        int bottom = 0;
        while (bottom < height) {
            View child = getChildAt(i);
            if (child == null) {
                break;
            }
            bottom = child.getBottom();
            int displayedHeight = Math.min(bottom, height) - Math.max(0, child.getTop());
            if (displayedHeight > maxDisplayedHeight) {
                mostVisibleIndex = i;
                maxDisplayedHeight = displayedHeight;
            }
            i++;
        }
        return firstPosition + mostVisibleIndex;
    }

    @Override // android.widget.OnDateChangedListener
    public void onDateChanged() {
        goTo(this.mController.getSelectedDay(), false, true, true);
    }

    private Calendar findAccessibilityFocus() {
        Calendar focus;
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if ((child instanceof SimpleMonthView) && (focus = ((SimpleMonthView) child).getAccessibilityFocus()) != null) {
                return focus;
            }
        }
        return null;
    }

    private boolean restoreAccessibilityFocus(Calendar day) {
        if (day == null) {
            return false;
        }
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if ((child instanceof SimpleMonthView) && ((SimpleMonthView) child).restoreAccessibilityFocus(day)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.AbsListView
    public void layoutChildren() {
        Calendar focusedDay = findAccessibilityFocus();
        super.layoutChildren();
        if (this.mPerformingScroll) {
            this.mPerformingScroll = false;
        } else {
            restoreAccessibilityFocus(focusedDay);
        }
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration newConfig) {
        this.mYearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
    }

    @Override // android.widget.AbsListView, android.widget.AdapterView
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setItemCount(-1);
    }

    private String getMonthAndYearString(Calendar day) {
        StringBuffer sbuf = new StringBuffer();
        sbuf.append(day.getDisplayName(2, 2, Locale.getDefault()));
        sbuf.append(" ");
        sbuf.append(this.mYearFormat.format(day.getTime()));
        return sbuf.toString();
    }

    @Override // android.widget.AbsListView, android.widget.AdapterView
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.addAction(4096);
        info.addAction(MtpConstants.RESPONSE_UNDEFINED);
    }

    @Override // android.widget.AbsListView
    public boolean performAccessibilityAction(int action, Bundle arguments) {
        View firstVisibleView;
        if (action != 4096 && action != 8192) {
            return super.performAccessibilityAction(action, arguments);
        }
        int firstVisiblePosition = getFirstVisiblePosition();
        int year = (firstVisiblePosition / 12) + this.mController.getMinYear();
        Calendar day = Calendar.getInstance();
        day.set(year, firstVisiblePosition % 12, 1);
        if (action == 4096) {
            day.add(2, 1);
            if (day.get(2) == 12) {
                day.set(2, 0);
                day.add(1, 1);
            }
        } else if (action == 8192 && (firstVisibleView = getChildAt(0)) != null && firstVisibleView.getTop() >= -1) {
            day.add(2, -1);
            if (day.get(2) == -1) {
                day.set(2, 11);
                day.add(1, -1);
            }
        }
        announceForAccessibility(getMonthAndYearString(day));
        goTo(day, true, false, true);
        this.mPerformingScroll = true;
        return true;
    }
}

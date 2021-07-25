package android.widget;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.AbsListView;
import android.widget.AdapterView;
import com.android.internal.R;

/* access modifiers changed from: package-private */
public class YearPickerView extends ListView implements AdapterView.OnItemClickListener, OnDateChangedListener {
    private YearAdapter mAdapter;
    private int mChildSize;
    private DatePickerController mController;
    private int mSelectedPosition;
    private int mViewSize;
    private int mYearSelectedCircleColor;

    public YearPickerView(Context context) {
        this(context, null);
    }

    public YearPickerView(Context context, AttributeSet attrs) {
        this(context, attrs, 16842868);
    }

    public YearPickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public YearPickerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mSelectedPosition = -1;
        setLayoutParams(new AbsListView.LayoutParams(-1, -2));
        Resources res = context.getResources();
        this.mViewSize = res.getDimensionPixelOffset(R.dimen.datepicker_view_animator_height);
        this.mChildSize = res.getDimensionPixelOffset(R.dimen.datepicker_year_label_height);
        setVerticalFadingEdgeEnabled(true);
        setFadingEdgeLength(this.mChildSize / 3);
        setPadding(0, res.getDimensionPixelSize(R.dimen.datepicker_year_picker_padding_top), 0, 0);
        setOnItemClickListener(this);
        setDividerHeight(0);
    }

    public void init(DatePickerController controller) {
        this.mController = controller;
        this.mController.registerOnDateChangedListener(this);
        this.mAdapter = new YearAdapter(getContext(), R.layout.year_label_text_view);
        updateAdapterData();
        setAdapter((ListAdapter) this.mAdapter);
        onDateChanged();
    }

    public void setYearSelectedCircleColor(int color) {
        if (color != this.mYearSelectedCircleColor) {
            this.mYearSelectedCircleColor = color;
        }
        requestLayout();
    }

    public int getYearSelectedCircleColor() {
        return this.mYearSelectedCircleColor;
    }

    private void updateAdapterData() {
        this.mAdapter.clear();
        int maxYear = this.mController.getMaxYear();
        for (int year = this.mController.getMinYear(); year <= maxYear; year++) {
            this.mAdapter.add(Integer.valueOf(year));
        }
    }

    @Override // android.widget.AdapterView.OnItemClickListener
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        this.mController.tryVibrate();
        if (position != this.mSelectedPosition) {
            this.mSelectedPosition = position;
            this.mAdapter.notifyDataSetChanged();
        }
        this.mController.onYearSelected(((Integer) this.mAdapter.getItem(position)).intValue());
    }

    /* access modifiers changed from: package-private */
    public void setItemTextAppearance(int resId) {
        this.mAdapter.setItemTextAppearance(resId);
    }

    /* access modifiers changed from: private */
    public class YearAdapter extends ArrayAdapter<Integer> {
        int mItemTextAppearanceResId;

        public YearAdapter(Context context, int resource) {
            super(context, resource);
        }

        @Override // android.widget.ArrayAdapter, android.widget.Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            boolean selected = true;
            TextViewWithCircularIndicator v = (TextViewWithCircularIndicator) super.getView(position, convertView, parent);
            v.setTextAppearance(getContext(), this.mItemTextAppearanceResId);
            v.requestLayout();
            if (YearPickerView.this.mController.getSelectedDay().get(1) != ((Integer) getItem(position)).intValue()) {
                selected = false;
            }
            v.setDrawIndicator(selected);
            if (selected) {
                v.setCircleColor(YearPickerView.this.mYearSelectedCircleColor);
            }
            return v;
        }

        public void setItemTextAppearance(int resId) {
            this.mItemTextAppearanceResId = resId;
        }
    }

    public void postSetSelectionCentered(int position) {
        postSetSelectionFromTop(position, (this.mViewSize / 2) - (this.mChildSize / 2));
    }

    public void postSetSelectionFromTop(final int position, final int offset) {
        post(new Runnable() {
            /* class android.widget.YearPickerView.AnonymousClass1 */

            public void run() {
                YearPickerView.this.setSelectionFromTop(position, offset);
                YearPickerView.this.requestLayout();
            }
        });
    }

    public int getFirstPositionOffset() {
        View firstChild = getChildAt(0);
        if (firstChild == null) {
            return 0;
        }
        return firstChild.getTop();
    }

    @Override // android.widget.OnDateChangedListener
    public void onDateChanged() {
        updateAdapterData();
        this.mAdapter.notifyDataSetChanged();
        postSetSelectionCentered(this.mController.getSelectedDay().get(1) - this.mController.getMinYear());
    }

    @Override // android.widget.AbsListView, android.view.View, android.widget.ListView, android.widget.AdapterView
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        if (event.getEventType() == 4096) {
            event.setFromIndex(0);
            event.setToIndex(0);
        }
    }
}

package android.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.ProxyInfo;
import android.view.View;
import android.widget.CalendarView;
import java.util.Calendar;
import java.util.Locale;

/* access modifiers changed from: private */
public class CalendarView$LegacyCalendarViewDelegate$WeekView extends View {
    private String[] mDayNumbers;
    private final Paint mDrawPaint = new Paint();
    private Calendar mFirstDay;
    private boolean[] mFocusDay;
    private boolean mHasFocusedDay;
    private boolean mHasSelectedDay = false;
    private boolean mHasUnfocusedDay;
    private int mHeight;
    private int mLastWeekDayMonth = -1;
    private final Paint mMonthNumDrawPaint = new Paint();
    private int mMonthOfFirstWeekDay = -1;
    private int mNumCells;
    private int mSelectedDay = -1;
    private int mSelectedLeft = -1;
    private int mSelectedRight = -1;
    private final Rect mTempRect = new Rect();
    private int mWeek = -1;
    private int mWidth;
    final /* synthetic */ CalendarView.LegacyCalendarViewDelegate this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public CalendarView$LegacyCalendarViewDelegate$WeekView(CalendarView.LegacyCalendarViewDelegate legacyCalendarViewDelegate, Context context) {
        super(context);
        this.this$0 = legacyCalendarViewDelegate;
        initilaizePaints();
    }

    public void init(int weekNumber, int selectedWeekDay, int focusedMonth) {
        int access$2300;
        this.mSelectedDay = selectedWeekDay;
        this.mHasSelectedDay = this.mSelectedDay != -1;
        if (CalendarView.LegacyCalendarViewDelegate.access$2200(this.this$0)) {
            access$2300 = CalendarView.LegacyCalendarViewDelegate.access$2300(this.this$0) + 1;
        } else {
            access$2300 = CalendarView.LegacyCalendarViewDelegate.access$2300(this.this$0);
        }
        this.mNumCells = access$2300;
        this.mWeek = weekNumber;
        CalendarView.LegacyCalendarViewDelegate.access$2000(this.this$0).setTimeInMillis(CalendarView.LegacyCalendarViewDelegate.access$1700(this.this$0).getTimeInMillis());
        CalendarView.LegacyCalendarViewDelegate.access$2000(this.this$0).add(3, this.mWeek);
        CalendarView.LegacyCalendarViewDelegate.access$2000(this.this$0).setFirstDayOfWeek(CalendarView.LegacyCalendarViewDelegate.access$1800(this.this$0));
        this.mDayNumbers = new String[this.mNumCells];
        this.mFocusDay = new boolean[this.mNumCells];
        int i = 0;
        if (CalendarView.LegacyCalendarViewDelegate.access$2200(this.this$0)) {
            this.mDayNumbers[0] = String.format(Locale.getDefault(), "%d", Integer.valueOf(CalendarView.LegacyCalendarViewDelegate.access$2000(this.this$0).get(3)));
            i = 0 + 1;
        }
        CalendarView.LegacyCalendarViewDelegate.access$2000(this.this$0).add(5, CalendarView.LegacyCalendarViewDelegate.access$1800(this.this$0) - CalendarView.LegacyCalendarViewDelegate.access$2000(this.this$0).get(7));
        this.mFirstDay = (Calendar) CalendarView.LegacyCalendarViewDelegate.access$2000(this.this$0).clone();
        this.mMonthOfFirstWeekDay = CalendarView.LegacyCalendarViewDelegate.access$2000(this.this$0).get(2);
        this.mHasUnfocusedDay = true;
        while (i < this.mNumCells) {
            boolean isFocusedDay = CalendarView.LegacyCalendarViewDelegate.access$2000(this.this$0).get(2) == focusedMonth;
            this.mFocusDay[i] = isFocusedDay;
            this.mHasFocusedDay |= isFocusedDay;
            this.mHasUnfocusedDay = (!isFocusedDay) & this.mHasUnfocusedDay;
            if (CalendarView.LegacyCalendarViewDelegate.access$2000(this.this$0).before(CalendarView.LegacyCalendarViewDelegate.access$1700(this.this$0)) || CalendarView.LegacyCalendarViewDelegate.access$2000(this.this$0).after(CalendarView.LegacyCalendarViewDelegate.access$1600(this.this$0))) {
                this.mDayNumbers[i] = ProxyInfo.LOCAL_EXCL_LIST;
            } else {
                this.mDayNumbers[i] = String.format(Locale.getDefault(), "%d", Integer.valueOf(CalendarView.LegacyCalendarViewDelegate.access$2000(this.this$0).get(5)));
            }
            CalendarView.LegacyCalendarViewDelegate.access$2000(this.this$0).add(5, 1);
            i++;
        }
        if (CalendarView.LegacyCalendarViewDelegate.access$2000(this.this$0).get(5) == 1) {
            CalendarView.LegacyCalendarViewDelegate.access$2000(this.this$0).add(5, -1);
        }
        this.mLastWeekDayMonth = CalendarView.LegacyCalendarViewDelegate.access$2000(this.this$0).get(2);
        updateSelectionPositions();
    }

    private void initilaizePaints() {
        this.mDrawPaint.setFakeBoldText(false);
        this.mDrawPaint.setAntiAlias(true);
        this.mDrawPaint.setStyle(Paint.Style.FILL);
        this.mMonthNumDrawPaint.setFakeBoldText(true);
        this.mMonthNumDrawPaint.setAntiAlias(true);
        this.mMonthNumDrawPaint.setStyle(Paint.Style.FILL);
        this.mMonthNumDrawPaint.setTextAlign(Paint.Align.CENTER);
        this.mMonthNumDrawPaint.setTextSize((float) CalendarView.LegacyCalendarViewDelegate.access$2400(this.this$0));
    }

    public int getMonthOfFirstWeekDay() {
        return this.mMonthOfFirstWeekDay;
    }

    public int getMonthOfLastWeekDay() {
        return this.mLastWeekDayMonth;
    }

    public Calendar getFirstDay() {
        return this.mFirstDay;
    }

    public boolean getDayFromLocation(float x, Calendar outCalendar) {
        int start;
        int end;
        boolean isLayoutRtl = isLayoutRtl();
        if (isLayoutRtl) {
            start = 0;
            end = CalendarView.LegacyCalendarViewDelegate.access$2200(this.this$0) ? this.mWidth - (this.mWidth / this.mNumCells) : this.mWidth;
        } else {
            if (CalendarView.LegacyCalendarViewDelegate.access$2200(this.this$0)) {
                start = this.mWidth / this.mNumCells;
            } else {
                start = 0;
            }
            end = this.mWidth;
        }
        if (x < ((float) start) || x > ((float) end)) {
            outCalendar.clear();
            return false;
        }
        int dayPosition = (int) (((x - ((float) start)) * ((float) CalendarView.LegacyCalendarViewDelegate.access$2300(this.this$0))) / ((float) (end - start)));
        if (isLayoutRtl) {
            dayPosition = (CalendarView.LegacyCalendarViewDelegate.access$2300(this.this$0) - 1) - dayPosition;
        }
        outCalendar.setTimeInMillis(this.mFirstDay.getTimeInMillis());
        outCalendar.add(5, dayPosition);
        return true;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onDraw(Canvas canvas) {
        drawBackground(canvas);
        drawWeekNumbersAndDates(canvas);
        drawWeekSeparators(canvas);
        drawSelectedDateVerticalBars(canvas);
    }

    private void drawBackground(Canvas canvas) {
        int i = 0;
        if (this.mHasSelectedDay) {
            this.mDrawPaint.setColor(CalendarView.LegacyCalendarViewDelegate.access$2500(this.this$0));
            this.mTempRect.top = CalendarView.LegacyCalendarViewDelegate.access$2600(this.this$0);
            this.mTempRect.bottom = this.mHeight;
            boolean isLayoutRtl = isLayoutRtl();
            if (isLayoutRtl) {
                this.mTempRect.left = 0;
                this.mTempRect.right = this.mSelectedLeft - 2;
            } else {
                Rect rect = this.mTempRect;
                if (CalendarView.LegacyCalendarViewDelegate.access$2200(this.this$0)) {
                    i = this.mWidth / this.mNumCells;
                }
                rect.left = i;
                this.mTempRect.right = this.mSelectedLeft - 2;
            }
            canvas.drawRect(this.mTempRect, this.mDrawPaint);
            if (isLayoutRtl) {
                this.mTempRect.left = this.mSelectedRight + 3;
                this.mTempRect.right = CalendarView.LegacyCalendarViewDelegate.access$2200(this.this$0) ? this.mWidth - (this.mWidth / this.mNumCells) : this.mWidth;
            } else {
                this.mTempRect.left = this.mSelectedRight + 3;
                this.mTempRect.right = this.mWidth;
            }
            canvas.drawRect(this.mTempRect, this.mDrawPaint);
        }
    }

    private void drawWeekNumbersAndDates(Canvas canvas) {
        int y = ((int) ((((float) this.mHeight) + this.mDrawPaint.getTextSize()) / 2.0f)) - CalendarView.LegacyCalendarViewDelegate.access$2600(this.this$0);
        int nDays = this.mNumCells;
        int divisor = nDays * 2;
        this.mDrawPaint.setTextAlign(Paint.Align.CENTER);
        this.mDrawPaint.setTextSize((float) CalendarView.LegacyCalendarViewDelegate.access$2400(this.this$0));
        int i = 0;
        if (isLayoutRtl()) {
            while (i < nDays - 1) {
                this.mMonthNumDrawPaint.setColor(this.mFocusDay[i] ? CalendarView.LegacyCalendarViewDelegate.access$2700(this.this$0) : CalendarView.LegacyCalendarViewDelegate.access$2800(this.this$0));
                canvas.drawText(this.mDayNumbers[(nDays - 1) - i], (float) ((((i * 2) + 1) * this.mWidth) / divisor), (float) y, this.mMonthNumDrawPaint);
                i++;
            }
            if (CalendarView.LegacyCalendarViewDelegate.access$2200(this.this$0)) {
                this.mDrawPaint.setColor(CalendarView.LegacyCalendarViewDelegate.access$2900(this.this$0));
                canvas.drawText(this.mDayNumbers[0], (float) (this.mWidth - (this.mWidth / divisor)), (float) y, this.mDrawPaint);
                return;
            }
            return;
        }
        if (CalendarView.LegacyCalendarViewDelegate.access$2200(this.this$0)) {
            this.mDrawPaint.setColor(CalendarView.LegacyCalendarViewDelegate.access$2900(this.this$0));
            canvas.drawText(this.mDayNumbers[0], (float) (this.mWidth / divisor), (float) y, this.mDrawPaint);
            i = 0 + 1;
        }
        while (i < nDays) {
            this.mMonthNumDrawPaint.setColor(this.mFocusDay[i] ? CalendarView.LegacyCalendarViewDelegate.access$2700(this.this$0) : CalendarView.LegacyCalendarViewDelegate.access$2800(this.this$0));
            canvas.drawText(this.mDayNumbers[i], (float) ((((i * 2) + 1) * this.mWidth) / divisor), (float) y, this.mMonthNumDrawPaint);
            i++;
        }
    }

    private void drawWeekSeparators(Canvas canvas) {
        float startX;
        float stopX;
        int firstFullyVisiblePosition = CalendarView.LegacyCalendarViewDelegate.access$1900(this.this$0).getFirstVisiblePosition();
        if (CalendarView.LegacyCalendarViewDelegate.access$1900(this.this$0).getChildAt(0).getTop() < 0) {
            firstFullyVisiblePosition++;
        }
        if (firstFullyVisiblePosition != this.mWeek) {
            this.mDrawPaint.setColor(CalendarView.LegacyCalendarViewDelegate.access$3000(this.this$0));
            this.mDrawPaint.setStrokeWidth((float) CalendarView.LegacyCalendarViewDelegate.access$2600(this.this$0));
            if (isLayoutRtl()) {
                startX = 0.0f;
                stopX = CalendarView.LegacyCalendarViewDelegate.access$2200(this.this$0) ? (float) (this.mWidth - (this.mWidth / this.mNumCells)) : (float) this.mWidth;
            } else {
                if (CalendarView.LegacyCalendarViewDelegate.access$2200(this.this$0)) {
                    startX = (float) (this.mWidth / this.mNumCells);
                } else {
                    startX = 0.0f;
                }
                stopX = (float) this.mWidth;
            }
            canvas.drawLine(startX, 0.0f, stopX, 0.0f, this.mDrawPaint);
        }
    }

    private void drawSelectedDateVerticalBars(Canvas canvas) {
        if (this.mHasSelectedDay) {
            CalendarView.LegacyCalendarViewDelegate.access$3200(this.this$0).setBounds(this.mSelectedLeft - (CalendarView.LegacyCalendarViewDelegate.access$3100(this.this$0) / 2), CalendarView.LegacyCalendarViewDelegate.access$2600(this.this$0), this.mSelectedLeft + (CalendarView.LegacyCalendarViewDelegate.access$3100(this.this$0) / 2), this.mHeight);
            CalendarView.LegacyCalendarViewDelegate.access$3200(this.this$0).draw(canvas);
            CalendarView.LegacyCalendarViewDelegate.access$3200(this.this$0).setBounds(this.mSelectedRight - (CalendarView.LegacyCalendarViewDelegate.access$3100(this.this$0) / 2), CalendarView.LegacyCalendarViewDelegate.access$2600(this.this$0), this.mSelectedRight + (CalendarView.LegacyCalendarViewDelegate.access$3100(this.this$0) / 2), this.mHeight);
            CalendarView.LegacyCalendarViewDelegate.access$3200(this.this$0).draw(canvas);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        this.mWidth = w;
        updateSelectionPositions();
    }

    private void updateSelectionPositions() {
        if (this.mHasSelectedDay) {
            boolean isLayoutRtl = isLayoutRtl();
            int selectedPosition = this.mSelectedDay - CalendarView.LegacyCalendarViewDelegate.access$1800(this.this$0);
            if (selectedPosition < 0) {
                selectedPosition += 7;
            }
            if (CalendarView.LegacyCalendarViewDelegate.access$2200(this.this$0) && !isLayoutRtl) {
                selectedPosition++;
            }
            if (isLayoutRtl) {
                this.mSelectedLeft = (((CalendarView.LegacyCalendarViewDelegate.access$2300(this.this$0) - 1) - selectedPosition) * this.mWidth) / this.mNumCells;
            } else {
                this.mSelectedLeft = (this.mWidth * selectedPosition) / this.mNumCells;
            }
            this.mSelectedRight = this.mSelectedLeft + (this.mWidth / this.mNumCells);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        this.mHeight = ((CalendarView.LegacyCalendarViewDelegate.access$1900(this.this$0).getHeight() - CalendarView.LegacyCalendarViewDelegate.access$1900(this.this$0).getPaddingTop()) - CalendarView.LegacyCalendarViewDelegate.access$1900(this.this$0).getPaddingBottom()) / CalendarView.LegacyCalendarViewDelegate.access$3300(this.this$0);
        setMeasuredDimension(View.MeasureSpec.getSize(widthMeasureSpec), this.mHeight);
    }
}

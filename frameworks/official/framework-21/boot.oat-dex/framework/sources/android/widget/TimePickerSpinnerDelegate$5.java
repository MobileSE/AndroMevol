package android.widget;

import android.view.View;

class TimePickerSpinnerDelegate$5 implements View.OnFocusChangeListener {
    final /* synthetic */ TimePickerSpinnerDelegate this$0;

    TimePickerSpinnerDelegate$5(TimePickerSpinnerDelegate timePickerSpinnerDelegate) {
        this.this$0 = timePickerSpinnerDelegate;
    }

    @Override // android.view.View.OnFocusChangeListener
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus && TimePickerSpinnerDelegate.access$700(this.this$0) && TimePickerSpinnerDelegate.access$800(this.this$0)) {
            TimePickerSpinnerDelegate.access$900(this.this$0);
            if (this.this$0.mOnTimeChangedListener != null) {
                this.this$0.mOnTimeChangedListener.onTimeChanged(this.this$0.mDelegator, TimePickerSpinnerDelegate.access$200(this.this$0).getCurrentHour(), TimePickerSpinnerDelegate.access$200(this.this$0).getCurrentMinute());
            }
        }
    }
}

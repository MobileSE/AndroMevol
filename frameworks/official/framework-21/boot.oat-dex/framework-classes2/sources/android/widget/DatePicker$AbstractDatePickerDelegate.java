package android.widget;

import android.content.Context;
import java.util.Locale;

abstract class DatePicker$AbstractDatePickerDelegate implements DatePicker$DatePickerDelegate {
    protected Context mContext;
    protected Locale mCurrentLocale;
    protected DatePicker mDelegator;
    protected DatePicker$OnDateChangedListener mOnDateChangedListener;
    protected DatePicker$ValidationCallback mValidationCallback;

    public DatePicker$AbstractDatePickerDelegate(DatePicker delegator, Context context) {
        this.mDelegator = delegator;
        this.mContext = context;
        setCurrentLocale(Locale.getDefault());
    }

    /* access modifiers changed from: protected */
    public void setCurrentLocale(Locale locale) {
        if (!locale.equals(this.mCurrentLocale)) {
            this.mCurrentLocale = locale;
        }
    }

    @Override // android.widget.DatePicker$DatePickerDelegate
    public void setValidationCallback(DatePicker$ValidationCallback callback) {
        this.mValidationCallback = callback;
    }

    /* access modifiers changed from: protected */
    public void onValidationChanged(boolean valid) {
        if (this.mValidationCallback != null) {
            this.mValidationCallback.onValidationChanged(valid);
        }
    }
}

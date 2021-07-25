package android.widget;

import android.text.Editable;
import android.text.TextWatcher;

class AutoCompleteTextView$MyWatcher implements TextWatcher {
    final /* synthetic */ AutoCompleteTextView this$0;

    private AutoCompleteTextView$MyWatcher(AutoCompleteTextView autoCompleteTextView) {
        this.this$0 = autoCompleteTextView;
    }

    /* synthetic */ AutoCompleteTextView$MyWatcher(AutoCompleteTextView x0, AutoCompleteTextView$1 x1) {
        this(x0);
    }

    public void afterTextChanged(Editable s) {
        this.this$0.doAfterTextChanged();
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        this.this$0.doBeforeTextChanged();
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }
}

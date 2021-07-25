package android.widget;

import android.view.View;

class AutoCompleteTextView$PassThroughClickListener implements View.OnClickListener {
    private View.OnClickListener mWrapped;
    final /* synthetic */ AutoCompleteTextView this$0;

    private AutoCompleteTextView$PassThroughClickListener(AutoCompleteTextView autoCompleteTextView) {
        this.this$0 = autoCompleteTextView;
    }

    /* synthetic */ AutoCompleteTextView$PassThroughClickListener(AutoCompleteTextView x0, AutoCompleteTextView$1 x1) {
        this(x0);
    }

    public void onClick(View v) {
        AutoCompleteTextView.access$600(this.this$0);
        if (this.mWrapped != null) {
            this.mWrapped.onClick(v);
        }
    }
}

package android.widget;

import android.widget.AutoCompleteTextView;
import android.widget.PopupWindow;

class AutoCompleteTextView$1 implements PopupWindow.OnDismissListener {
    final /* synthetic */ AutoCompleteTextView this$0;
    final /* synthetic */ AutoCompleteTextView.OnDismissListener val$dismissListener;

    AutoCompleteTextView$1(AutoCompleteTextView autoCompleteTextView, AutoCompleteTextView.OnDismissListener onDismissListener) {
        this.this$0 = autoCompleteTextView;
        this.val$dismissListener = onDismissListener;
    }

    @Override // android.widget.PopupWindow.OnDismissListener
    public void onDismiss() {
        this.val$dismissListener.onDismiss();
    }
}

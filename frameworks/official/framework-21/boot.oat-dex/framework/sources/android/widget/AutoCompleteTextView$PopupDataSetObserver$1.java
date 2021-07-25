package android.widget;

import android.widget.AutoCompleteTextView;

class AutoCompleteTextView$PopupDataSetObserver$1 implements Runnable {
    final /* synthetic */ AutoCompleteTextView.PopupDataSetObserver this$0;

    AutoCompleteTextView$PopupDataSetObserver$1(AutoCompleteTextView.PopupDataSetObserver popupDataSetObserver) {
        this.this$0 = popupDataSetObserver;
    }

    public void run() {
        ListAdapter adapter;
        AutoCompleteTextView textView = (AutoCompleteTextView) AutoCompleteTextView.PopupDataSetObserver.access$800(this.this$0).get();
        if (textView != null && (adapter = textView.mAdapter) != null) {
            textView.updateDropDownForFilter(adapter.getCount());
        }
    }
}

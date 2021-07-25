package android.widget;

import android.view.View;
import android.widget.AdapterView;

class AutoCompleteTextView$DropDownItemClickListener implements AdapterView.OnItemClickListener {
    final /* synthetic */ AutoCompleteTextView this$0;

    private AutoCompleteTextView$DropDownItemClickListener(AutoCompleteTextView autoCompleteTextView) {
        this.this$0 = autoCompleteTextView;
    }

    /* synthetic */ AutoCompleteTextView$DropDownItemClickListener(AutoCompleteTextView x0, AutoCompleteTextView$1 x1) {
        this(x0);
    }

    @Override // android.widget.AdapterView.OnItemClickListener
    public void onItemClick(AdapterView parent, View v, int position, long id) {
        AutoCompleteTextView.access$500(this.this$0, v, position, id);
    }
}

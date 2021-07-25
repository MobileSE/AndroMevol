package android.widget;

import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

class Spinner$DropdownPopup$1 implements AdapterView.OnItemClickListener {
    final /* synthetic */ Spinner.DropdownPopup this$1;
    final /* synthetic */ Spinner val$this$0;

    Spinner$DropdownPopup$1(Spinner.DropdownPopup dropdownPopup, Spinner spinner) {
        this.this$1 = dropdownPopup;
        this.val$this$0 = spinner;
    }

    @Override // android.widget.AdapterView.OnItemClickListener
    public void onItemClick(AdapterView parent, View v, int position, long id) {
        this.this$1.this$0.setSelection(position);
        if (this.this$1.this$0.mOnItemClickListener != null) {
            this.this$1.this$0.performItemClick(v, position, Spinner.DropdownPopup.access$300(this.this$1).getItemId(position));
        }
        this.this$1.dismiss();
    }
}

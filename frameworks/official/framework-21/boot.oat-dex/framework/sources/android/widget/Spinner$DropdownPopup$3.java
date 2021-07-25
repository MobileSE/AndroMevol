package android.widget;

import android.view.ViewTreeObserver;
import android.widget.PopupWindow;
import android.widget.Spinner;

class Spinner$DropdownPopup$3 implements PopupWindow.OnDismissListener {
    final /* synthetic */ Spinner.DropdownPopup this$1;
    final /* synthetic */ ViewTreeObserver.OnGlobalLayoutListener val$layoutListener;

    Spinner$DropdownPopup$3(Spinner.DropdownPopup dropdownPopup, ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener) {
        this.this$1 = dropdownPopup;
        this.val$layoutListener = onGlobalLayoutListener;
    }

    public void onDismiss() {
        ViewTreeObserver vto = this.this$1.this$0.getViewTreeObserver();
        if (vto != null) {
            vto.removeOnGlobalLayoutListener(this.val$layoutListener);
        }
    }
}

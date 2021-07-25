package android.widget;

import android.view.View;
import android.widget.ActionMenuPresenter;
import android.widget.ListPopupWindow;

class ActionMenuPresenter$OverflowMenuButton$1 extends ListPopupWindow.ForwardingListener {
    final /* synthetic */ ActionMenuPresenter.OverflowMenuButton this$1;
    final /* synthetic */ ActionMenuPresenter val$this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    ActionMenuPresenter$OverflowMenuButton$1(ActionMenuPresenter.OverflowMenuButton overflowMenuButton, View x0, ActionMenuPresenter actionMenuPresenter) {
        super(x0);
        this.this$1 = overflowMenuButton;
        this.val$this$0 = actionMenuPresenter;
    }

    public ListPopupWindow getPopup() {
        if (ActionMenuPresenter.access$200(this.this$1.this$0) == null) {
            return null;
        }
        return ActionMenuPresenter.access$200(this.this$1.this$0).getPopup();
    }

    public boolean onForwardingStarted() {
        this.this$1.this$0.showOverflowMenu();
        return true;
    }

    public boolean onForwardingStopped() {
        if (ActionMenuPresenter.access$300(this.this$1.this$0) != null) {
            return false;
        }
        this.this$1.this$0.hideOverflowMenu();
        return true;
    }
}

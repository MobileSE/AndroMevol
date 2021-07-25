package android.widget;

import android.view.View;
import android.widget.ActionMenuPresenter;

class ActionMenuPresenter$OpenOverflowRunnable implements Runnable {
    private ActionMenuPresenter.OverflowPopup mPopup;
    final /* synthetic */ ActionMenuPresenter this$0;

    public ActionMenuPresenter$OpenOverflowRunnable(ActionMenuPresenter actionMenuPresenter, ActionMenuPresenter.OverflowPopup popup) {
        this.this$0 = actionMenuPresenter;
        this.mPopup = popup;
    }

    public void run() {
        ActionMenuPresenter.access$800(this.this$0).changeMenuMode();
        View menuView = ActionMenuPresenter.access$900(this.this$0);
        if (!(menuView == null || menuView.getWindowToken() == null || !this.mPopup.tryShow())) {
            ActionMenuPresenter.access$202(this.this$0, this.mPopup);
        }
        ActionMenuPresenter.access$302(this.this$0, (ActionMenuPresenter$OpenOverflowRunnable) null);
    }
}

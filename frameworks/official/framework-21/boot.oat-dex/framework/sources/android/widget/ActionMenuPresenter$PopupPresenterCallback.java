package android.widget;

import com.android.internal.view.menu.MenuBuilder;
import com.android.internal.view.menu.MenuPresenter;
import com.android.internal.view.menu.SubMenuBuilder;

class ActionMenuPresenter$PopupPresenterCallback implements MenuPresenter.Callback {
    final /* synthetic */ ActionMenuPresenter this$0;

    private ActionMenuPresenter$PopupPresenterCallback(ActionMenuPresenter actionMenuPresenter) {
        this.this$0 = actionMenuPresenter;
    }

    public boolean onOpenSubMenu(MenuBuilder subMenu) {
        if (subMenu == null) {
            return false;
        }
        this.this$0.mOpenSubMenuId = ((SubMenuBuilder) subMenu).getItem().getItemId();
        MenuPresenter.Callback cb = this.this$0.getCallback();
        return cb != null ? cb.onOpenSubMenu(subMenu) : false;
    }

    public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
        if (menu instanceof SubMenuBuilder) {
            ((SubMenuBuilder) menu).getRootMenu().close(false);
        }
        MenuPresenter.Callback cb = this.this$0.getCallback();
        if (cb != null) {
            cb.onCloseMenu(menu, allMenusAreClosing);
        }
    }
}

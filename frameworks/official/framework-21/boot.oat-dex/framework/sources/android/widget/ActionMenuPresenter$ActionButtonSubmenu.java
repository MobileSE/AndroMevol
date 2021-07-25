package android.widget;

import android.content.Context;
import android.view.MenuItem;
import android.view.View;
import com.android.internal.view.menu.MenuPopupHelper;
import com.android.internal.view.menu.SubMenuBuilder;

class ActionMenuPresenter$ActionButtonSubmenu extends MenuPopupHelper {
    private SubMenuBuilder mSubMenu;
    final /* synthetic */ ActionMenuPresenter this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public ActionMenuPresenter$ActionButtonSubmenu(ActionMenuPresenter actionMenuPresenter, Context context, SubMenuBuilder subMenu) {
        super(context, subMenu, (View) null, false, 16843844);
        this.this$0 = actionMenuPresenter;
        this.mSubMenu = subMenu;
        if (!subMenu.getItem().isActionButton()) {
            setAnchorView(ActionMenuPresenter.access$500(actionMenuPresenter) == null ? (View) ActionMenuPresenter.access$600(actionMenuPresenter) : ActionMenuPresenter.access$500(actionMenuPresenter));
        }
        setCallback(actionMenuPresenter.mPopupPresenterCallback);
        boolean preserveIconSpacing = false;
        int count = subMenu.size();
        int i = 0;
        while (true) {
            if (i >= count) {
                break;
            }
            MenuItem childItem = subMenu.getItem(i);
            if (childItem.isVisible() && childItem.getIcon() != null) {
                preserveIconSpacing = true;
                break;
            }
            i++;
        }
        setForceShowIcon(preserveIconSpacing);
    }

    public void onDismiss() {
        ActionMenuPresenter$ActionButtonSubmenu.super.onDismiss();
        ActionMenuPresenter.access$702(this.this$0, (ActionMenuPresenter$ActionButtonSubmenu) null);
        this.this$0.mOpenSubMenuId = 0;
    }
}

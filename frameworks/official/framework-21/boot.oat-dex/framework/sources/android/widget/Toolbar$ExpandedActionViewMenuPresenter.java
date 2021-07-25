package android.widget;

import android.content.Context;
import android.os.Parcelable;
import android.view.CollapsibleActionView;
import android.view.ViewGroup;
import android.widget.Toolbar;
import com.android.internal.view.menu.MenuBuilder;
import com.android.internal.view.menu.MenuItemImpl;
import com.android.internal.view.menu.MenuPresenter;
import com.android.internal.view.menu.MenuView;
import com.android.internal.view.menu.SubMenuBuilder;

class Toolbar$ExpandedActionViewMenuPresenter implements MenuPresenter {
    MenuItemImpl mCurrentExpandedItem;
    MenuBuilder mMenu;
    final /* synthetic */ Toolbar this$0;

    private Toolbar$ExpandedActionViewMenuPresenter(Toolbar toolbar) {
        this.this$0 = toolbar;
    }

    public void initForMenu(Context context, MenuBuilder menu) {
        if (!(this.mMenu == null || this.mCurrentExpandedItem == null)) {
            this.mMenu.collapseItemActionView(this.mCurrentExpandedItem);
        }
        this.mMenu = menu;
    }

    public MenuView getMenuView(ViewGroup root) {
        return null;
    }

    public void updateMenuView(boolean cleared) {
        if (this.mCurrentExpandedItem != null) {
            boolean found = false;
            if (this.mMenu != null) {
                int count = this.mMenu.size();
                int i = 0;
                while (true) {
                    if (i >= count) {
                        break;
                    } else if (this.mMenu.getItem(i) == this.mCurrentExpandedItem) {
                        found = true;
                        break;
                    } else {
                        i++;
                    }
                }
            }
            if (!found) {
                collapseItemActionView(this.mMenu, this.mCurrentExpandedItem);
            }
        }
    }

    public void setCallback(MenuPresenter.Callback cb) {
    }

    public boolean onSubMenuSelected(SubMenuBuilder subMenu) {
        return false;
    }

    public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
    }

    public boolean flagActionItems() {
        return false;
    }

    public boolean expandItemActionView(MenuBuilder menu, MenuItemImpl item) {
        Toolbar.access$200(this.this$0);
        if (Toolbar.access$300(this.this$0).getParent() != this.this$0) {
            this.this$0.addView(Toolbar.access$300(this.this$0));
        }
        this.this$0.mExpandedActionView = item.getActionView();
        this.mCurrentExpandedItem = item;
        if (this.this$0.mExpandedActionView.getParent() != this.this$0) {
            Toolbar.LayoutParams lp = this.this$0.generateDefaultLayoutParams();
            lp.gravity = 8388611 | (Toolbar.access$400(this.this$0) & 112);
            lp.mViewType = 2;
            this.this$0.mExpandedActionView.setLayoutParams(lp);
            this.this$0.addView(this.this$0.mExpandedActionView);
        }
        Toolbar.access$500(this.this$0, true);
        this.this$0.requestLayout();
        item.setActionViewExpanded(true);
        if (this.this$0.mExpandedActionView instanceof CollapsibleActionView) {
            ((CollapsibleActionView) this.this$0.mExpandedActionView).onActionViewExpanded();
        }
        return true;
    }

    public boolean collapseItemActionView(MenuBuilder menu, MenuItemImpl item) {
        if (this.this$0.mExpandedActionView instanceof CollapsibleActionView) {
            ((CollapsibleActionView) this.this$0.mExpandedActionView).onActionViewCollapsed();
        }
        this.this$0.removeView(this.this$0.mExpandedActionView);
        this.this$0.removeView(Toolbar.access$300(this.this$0));
        this.this$0.mExpandedActionView = null;
        Toolbar.access$500(this.this$0, false);
        this.mCurrentExpandedItem = null;
        this.this$0.requestLayout();
        item.setActionViewExpanded(false);
        return true;
    }

    public int getId() {
        return 0;
    }

    public Parcelable onSaveInstanceState() {
        return null;
    }

    public void onRestoreInstanceState(Parcelable state) {
    }
}

package com.android.internal.app;

import android.app.ActionBar;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowCallbackWrapper;
import android.widget.SpinnerAdapter;
import android.widget.Toolbar;
import com.android.internal.view.menu.MenuBuilder;
import com.android.internal.view.menu.MenuPresenter;
import com.android.internal.widget.DecorToolbar;
import java.util.ArrayList;

public class ToolbarActionBar extends ActionBar {
    private DecorToolbar mDecorToolbar;
    private boolean mLastMenuVisibility;
    private boolean mMenuCallbackSet;
    private final Toolbar.OnMenuItemClickListener mMenuClicker = new Toolbar.OnMenuItemClickListener() {
        /* class com.android.internal.app.ToolbarActionBar.AnonymousClass2 */

        @Override // android.widget.Toolbar.OnMenuItemClickListener
        public boolean onMenuItemClick(MenuItem item) {
            return ToolbarActionBar.this.mWindowCallback.onMenuItemSelected(0, item);
        }
    };
    private final Runnable mMenuInvalidator = new Runnable() {
        /* class com.android.internal.app.ToolbarActionBar.AnonymousClass1 */

        public void run() {
            ToolbarActionBar.this.populateOptionsMenu();
        }
    };
    private ArrayList<ActionBar.OnMenuVisibilityListener> mMenuVisibilityListeners = new ArrayList<>();
    private Toolbar mToolbar;
    private boolean mToolbarMenuPrepared;
    private Window.Callback mWindowCallback;

    /* JADX WARN: Type inference failed for: r0v4, types: [com.android.internal.app.ToolbarActionBar$ToolbarCallbackWrapper, android.view.Window$Callback] */
    /* JADX WARNING: Unknown variable types count: 1 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public ToolbarActionBar(android.widget.Toolbar r3, java.lang.CharSequence r4, android.view.Window.Callback r5) {
        /*
            r2 = this;
            r2.<init>()
            java.util.ArrayList r0 = new java.util.ArrayList
            r0.<init>()
            r2.mMenuVisibilityListeners = r0
            com.android.internal.app.ToolbarActionBar$1 r0 = new com.android.internal.app.ToolbarActionBar$1
            r0.<init>()
            r2.mMenuInvalidator = r0
            com.android.internal.app.ToolbarActionBar$2 r0 = new com.android.internal.app.ToolbarActionBar$2
            r0.<init>()
            r2.mMenuClicker = r0
            r2.mToolbar = r3
            com.android.internal.widget.ToolbarWidgetWrapper r0 = new com.android.internal.widget.ToolbarWidgetWrapper
            r1 = 0
            r0.<init>(r3, r1)
            r2.mDecorToolbar = r0
            com.android.internal.app.ToolbarActionBar$ToolbarCallbackWrapper r0 = new com.android.internal.app.ToolbarActionBar$ToolbarCallbackWrapper
            r0.<init>(r5)
            r2.mWindowCallback = r0
            com.android.internal.widget.DecorToolbar r0 = r2.mDecorToolbar
            android.view.Window$Callback r1 = r2.mWindowCallback
            r0.setWindowCallback(r1)
            android.widget.Toolbar$OnMenuItemClickListener r0 = r2.mMenuClicker
            r3.setOnMenuItemClickListener(r0)
            com.android.internal.widget.DecorToolbar r0 = r2.mDecorToolbar
            r0.setWindowTitle(r4)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.ToolbarActionBar.<init>(android.widget.Toolbar, java.lang.CharSequence, android.view.Window$Callback):void");
    }

    public Window.Callback getWrappedWindowCallback() {
        return this.mWindowCallback;
    }

    @Override // android.app.ActionBar
    public void setCustomView(View view) {
        setCustomView(view, new ActionBar.LayoutParams(-2, -2));
    }

    public void setCustomView(View view, ActionBar.LayoutParams layoutParams) {
        view.setLayoutParams(layoutParams);
        this.mDecorToolbar.setCustomView(view);
    }

    @Override // android.app.ActionBar
    public void setCustomView(int resId) {
        setCustomView(LayoutInflater.from(this.mToolbar.getContext()).inflate(resId, (ViewGroup) this.mToolbar, false));
    }

    @Override // android.app.ActionBar
    public void setIcon(int resId) {
        this.mDecorToolbar.setIcon(resId);
    }

    @Override // android.app.ActionBar
    public void setIcon(Drawable icon) {
        this.mDecorToolbar.setIcon(icon);
    }

    @Override // android.app.ActionBar
    public void setLogo(int resId) {
        this.mDecorToolbar.setLogo(resId);
    }

    @Override // android.app.ActionBar
    public void setLogo(Drawable logo) {
        this.mDecorToolbar.setLogo(logo);
    }

    public void setStackedBackgroundDrawable(Drawable d) {
    }

    public void setSplitBackgroundDrawable(Drawable d) {
    }

    public void setHomeButtonEnabled(boolean enabled) {
    }

    public void setElevation(float elevation) {
        this.mToolbar.setElevation(elevation);
    }

    public float getElevation() {
        return this.mToolbar.getElevation();
    }

    public Context getThemedContext() {
        return this.mToolbar.getContext();
    }

    public boolean isTitleTruncated() {
        return super.isTitleTruncated();
    }

    @Override // android.app.ActionBar
    public void setHomeAsUpIndicator(Drawable indicator) {
        this.mToolbar.setNavigationIcon(indicator);
    }

    @Override // android.app.ActionBar
    public void setHomeAsUpIndicator(int resId) {
        this.mToolbar.setNavigationIcon(resId);
    }

    @Override // android.app.ActionBar
    public void setHomeActionContentDescription(CharSequence description) {
        this.mDecorToolbar.setNavigationContentDescription(description);
    }

    public void setDefaultDisplayHomeAsUpEnabled(boolean enabled) {
    }

    @Override // android.app.ActionBar
    public void setHomeActionContentDescription(int resId) {
        this.mDecorToolbar.setNavigationContentDescription(resId);
    }

    public void setShowHideAnimationEnabled(boolean enabled) {
    }

    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
    }

    public ActionMode startActionMode(ActionMode.Callback callback) {
        return null;
    }

    public void setListNavigationCallbacks(SpinnerAdapter adapter, ActionBar.OnNavigationListener callback) {
        this.mDecorToolbar.setDropdownParams(adapter, new NavItemSelectedListener(callback));
    }

    public void setSelectedNavigationItem(int position) {
        switch (this.mDecorToolbar.getNavigationMode()) {
            case 1:
                this.mDecorToolbar.setDropdownSelectedPosition(position);
                return;
            default:
                throw new IllegalStateException("setSelectedNavigationIndex not valid for current navigation mode");
        }
    }

    public int getSelectedNavigationIndex() {
        return -1;
    }

    public int getNavigationItemCount() {
        return 0;
    }

    @Override // android.app.ActionBar
    public void setTitle(CharSequence title) {
        this.mDecorToolbar.setTitle(title);
    }

    @Override // android.app.ActionBar
    public void setTitle(int resId) {
        this.mDecorToolbar.setTitle(resId != 0 ? this.mDecorToolbar.getContext().getText(resId) : null);
    }

    public void setWindowTitle(CharSequence title) {
        this.mDecorToolbar.setWindowTitle(title);
    }

    @Override // android.app.ActionBar
    public void setSubtitle(CharSequence subtitle) {
        this.mDecorToolbar.setSubtitle(subtitle);
    }

    @Override // android.app.ActionBar
    public void setSubtitle(int resId) {
        this.mDecorToolbar.setSubtitle(resId != 0 ? this.mDecorToolbar.getContext().getText(resId) : null);
    }

    public void setDisplayOptions(int options) {
        setDisplayOptions(options, -1);
    }

    public void setDisplayOptions(int options, int mask) {
        this.mDecorToolbar.setDisplayOptions((options & mask) | ((mask ^ -1) & this.mDecorToolbar.getDisplayOptions()));
    }

    public void setDisplayUseLogoEnabled(boolean useLogo) {
        setDisplayOptions(useLogo ? 1 : 0, 1);
    }

    public void setDisplayShowHomeEnabled(boolean showHome) {
        setDisplayOptions(showHome ? 2 : 0, 2);
    }

    public void setDisplayHomeAsUpEnabled(boolean showHomeAsUp) {
        setDisplayOptions(showHomeAsUp ? 4 : 0, 4);
    }

    public void setDisplayShowTitleEnabled(boolean showTitle) {
        setDisplayOptions(showTitle ? 8 : 0, 8);
    }

    public void setDisplayShowCustomEnabled(boolean showCustom) {
        setDisplayOptions(showCustom ? 16 : 0, 16);
    }

    public void setBackgroundDrawable(Drawable d) {
        this.mToolbar.setBackground(d);
    }

    public View getCustomView() {
        return this.mDecorToolbar.getCustomView();
    }

    public CharSequence getTitle() {
        return this.mToolbar.getTitle();
    }

    public CharSequence getSubtitle() {
        return this.mToolbar.getSubtitle();
    }

    public int getNavigationMode() {
        return 0;
    }

    public void setNavigationMode(int mode) {
        if (mode == 2) {
            throw new IllegalArgumentException("Tabs not supported in this configuration");
        }
        this.mDecorToolbar.setNavigationMode(mode);
    }

    public int getDisplayOptions() {
        return this.mDecorToolbar.getDisplayOptions();
    }

    public ActionBar.Tab newTab() {
        throw new UnsupportedOperationException("Tabs are not supported in toolbar action bars");
    }

    public void addTab(ActionBar.Tab tab) {
        throw new UnsupportedOperationException("Tabs are not supported in toolbar action bars");
    }

    @Override // android.app.ActionBar
    public void addTab(ActionBar.Tab tab, boolean setSelected) {
        throw new UnsupportedOperationException("Tabs are not supported in toolbar action bars");
    }

    @Override // android.app.ActionBar
    public void addTab(ActionBar.Tab tab, int position) {
        throw new UnsupportedOperationException("Tabs are not supported in toolbar action bars");
    }

    public void addTab(ActionBar.Tab tab, int position, boolean setSelected) {
        throw new UnsupportedOperationException("Tabs are not supported in toolbar action bars");
    }

    public void removeTab(ActionBar.Tab tab) {
        throw new UnsupportedOperationException("Tabs are not supported in toolbar action bars");
    }

    public void removeTabAt(int position) {
        throw new UnsupportedOperationException("Tabs are not supported in toolbar action bars");
    }

    public void removeAllTabs() {
        throw new UnsupportedOperationException("Tabs are not supported in toolbar action bars");
    }

    public void selectTab(ActionBar.Tab tab) {
        throw new UnsupportedOperationException("Tabs are not supported in toolbar action bars");
    }

    public ActionBar.Tab getSelectedTab() {
        throw new UnsupportedOperationException("Tabs are not supported in toolbar action bars");
    }

    public ActionBar.Tab getTabAt(int index) {
        throw new UnsupportedOperationException("Tabs are not supported in toolbar action bars");
    }

    public int getTabCount() {
        return 0;
    }

    public int getHeight() {
        return this.mToolbar.getHeight();
    }

    public void show() {
        this.mToolbar.setVisibility(0);
    }

    public void hide() {
        this.mToolbar.setVisibility(8);
    }

    public boolean isShowing() {
        return this.mToolbar.getVisibility() == 0;
    }

    public boolean openOptionsMenu() {
        return this.mToolbar.showOverflowMenu();
    }

    public boolean invalidateOptionsMenu() {
        this.mToolbar.removeCallbacks(this.mMenuInvalidator);
        this.mToolbar.postOnAnimation(this.mMenuInvalidator);
        return true;
    }

    public boolean collapseActionView() {
        if (!this.mToolbar.hasExpandedActionView()) {
            return false;
        }
        this.mToolbar.collapseActionView();
        return true;
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v0, types: [com.android.internal.app.ToolbarActionBar$1] */
    /* JADX WARN: Type inference failed for: r0v1, types: [com.android.internal.view.menu.MenuBuilder] */
    /* JADX WARN: Type inference failed for: r0v2 */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Unknown variable types count: 1 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void populateOptionsMenu() {
        /*
            r5 = this;
            r0 = 0
            boolean r2 = r5.mMenuCallbackSet
            if (r2 != 0) goto L_0x0017
            android.widget.Toolbar r2 = r5.mToolbar
            com.android.internal.app.ToolbarActionBar$ActionMenuPresenterCallback r3 = new com.android.internal.app.ToolbarActionBar$ActionMenuPresenterCallback
            r3.<init>()
            com.android.internal.app.ToolbarActionBar$MenuBuilderCallback r4 = new com.android.internal.app.ToolbarActionBar$MenuBuilderCallback
            r4.<init>()
            r2.setMenuCallbacks(r3, r4)
            r2 = 1
            r5.mMenuCallbackSet = r2
        L_0x0017:
            android.widget.Toolbar r2 = r5.mToolbar
            android.view.Menu r1 = r2.getMenu()
            boolean r2 = r1 instanceof com.android.internal.view.menu.MenuBuilder
            if (r2 == 0) goto L_0x0025
            r2 = r1
            com.android.internal.view.menu.MenuBuilder r2 = (com.android.internal.view.menu.MenuBuilder) r2
            r0 = r2
        L_0x0025:
            if (r0 == 0) goto L_0x002a
            r0.stopDispatchingItemsChanged()
        L_0x002a:
            r1.clear()     // Catch:{ all -> 0x0049 }
            android.view.Window$Callback r2 = r5.mWindowCallback     // Catch:{ all -> 0x0049 }
            r3 = 0
            boolean r2 = r2.onCreatePanelMenu(r3, r1)     // Catch:{ all -> 0x0049 }
            if (r2 == 0) goto L_0x0040
            android.view.Window$Callback r2 = r5.mWindowCallback     // Catch:{ all -> 0x0049 }
            r3 = 0
            r4 = 0
            boolean r2 = r2.onPreparePanel(r3, r4, r1)     // Catch:{ all -> 0x0049 }
            if (r2 != 0) goto L_0x0043
        L_0x0040:
            r1.clear()     // Catch:{ all -> 0x0049 }
        L_0x0043:
            if (r0 == 0) goto L_0x0048
            r0.startDispatchingItemsChanged()
        L_0x0048:
            return
        L_0x0049:
            r2 = move-exception
            if (r0 == 0) goto L_0x004f
            r0.startDispatchingItemsChanged()
        L_0x004f:
            throw r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.ToolbarActionBar.populateOptionsMenu():void");
    }

    public boolean onMenuKeyEvent(KeyEvent event) {
        if (event.getAction() == 1) {
            openOptionsMenu();
        }
        return true;
    }

    public void addOnMenuVisibilityListener(ActionBar.OnMenuVisibilityListener listener) {
        this.mMenuVisibilityListeners.add(listener);
    }

    public void removeOnMenuVisibilityListener(ActionBar.OnMenuVisibilityListener listener) {
        this.mMenuVisibilityListeners.remove(listener);
    }

    public void dispatchMenuVisibilityChanged(boolean isVisible) {
        if (isVisible != this.mLastMenuVisibility) {
            this.mLastMenuVisibility = isVisible;
            int count = this.mMenuVisibilityListeners.size();
            for (int i = 0; i < count; i++) {
                this.mMenuVisibilityListeners.get(i).onMenuVisibilityChanged(isVisible);
            }
        }
    }

    private class ToolbarCallbackWrapper extends WindowCallbackWrapper {
        public ToolbarCallbackWrapper(Window.Callback wrapped) {
            super(wrapped);
        }

        public boolean onPreparePanel(int featureId, View view, Menu menu) {
            boolean result = ToolbarActionBar.super.onPreparePanel(featureId, view, menu);
            if (result && !ToolbarActionBar.this.mToolbarMenuPrepared) {
                ToolbarActionBar.this.mDecorToolbar.setMenuPrepared();
                ToolbarActionBar.this.mToolbarMenuPrepared = true;
            }
            return result;
        }
    }

    /* access modifiers changed from: private */
    public final class ActionMenuPresenterCallback implements MenuPresenter.Callback {
        private boolean mClosingActionMenu;

        private ActionMenuPresenterCallback() {
        }

        @Override // com.android.internal.view.menu.MenuPresenter.Callback
        public boolean onOpenSubMenu(MenuBuilder subMenu) {
            if (ToolbarActionBar.this.mWindowCallback == null) {
                return false;
            }
            ToolbarActionBar.this.mWindowCallback.onMenuOpened(8, subMenu);
            return true;
        }

        @Override // com.android.internal.view.menu.MenuPresenter.Callback
        public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
            if (!this.mClosingActionMenu) {
                this.mClosingActionMenu = true;
                ToolbarActionBar.this.mToolbar.dismissPopupMenus();
                if (ToolbarActionBar.this.mWindowCallback != null) {
                    ToolbarActionBar.this.mWindowCallback.onPanelClosed(8, menu);
                }
                this.mClosingActionMenu = false;
            }
        }
    }

    /* access modifiers changed from: private */
    public final class MenuBuilderCallback implements MenuBuilder.Callback {
        private MenuBuilderCallback() {
        }

        @Override // com.android.internal.view.menu.MenuBuilder.Callback
        public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
            return false;
        }

        @Override // com.android.internal.view.menu.MenuBuilder.Callback
        public void onMenuModeChange(MenuBuilder menu) {
            if (ToolbarActionBar.this.mWindowCallback == null) {
                return;
            }
            if (ToolbarActionBar.this.mToolbar.isOverflowMenuShowing()) {
                ToolbarActionBar.this.mWindowCallback.onPanelClosed(8, menu);
            } else if (ToolbarActionBar.this.mWindowCallback.onPreparePanel(0, null, menu)) {
                ToolbarActionBar.this.mWindowCallback.onMenuOpened(8, menu);
            }
        }
    }
}

package com.android.internal.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.SpinnerAdapter;
import android.widget.Toolbar;
import com.android.internal.R;
import com.android.internal.view.ActionBarPolicy;
import com.android.internal.view.menu.MenuBuilder;
import com.android.internal.view.menu.MenuPopupHelper;
import com.android.internal.view.menu.SubMenuBuilder;
import com.android.internal.widget.ActionBarContainer;
import com.android.internal.widget.ActionBarContextView;
import com.android.internal.widget.ActionBarOverlayLayout;
import com.android.internal.widget.DecorToolbar;
import com.android.internal.widget.ScrollingTabContainerView;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class WindowDecorActionBar extends ActionBar implements ActionBarOverlayLayout.ActionBarVisibilityCallback {
    static final /* synthetic */ boolean $assertionsDisabled = (!WindowDecorActionBar.class.desiredAssertionStatus());
    private static final int CONTEXT_DISPLAY_NORMAL = 0;
    private static final int CONTEXT_DISPLAY_SPLIT = 1;
    private static final int INVALID_POSITION = -1;
    private static final String TAG = "WindowDecorActionBar";
    ActionModeImpl mActionMode;
    private Activity mActivity;
    private ActionBarContainer mContainerView;
    private boolean mContentAnimations = true;
    private View mContentView;
    private Context mContext;
    private int mContextDisplayMode;
    private ActionBarContextView mContextView;
    private int mCurWindowVisibility = 0;
    private Animator mCurrentShowAnim;
    private DecorToolbar mDecorToolbar;
    ActionMode mDeferredDestroyActionMode;
    ActionMode.Callback mDeferredModeDestroyCallback;
    private Dialog mDialog;
    private boolean mDisplayHomeAsUpSet;
    private boolean mHasEmbeddedTabs;
    private boolean mHiddenByApp;
    private boolean mHiddenBySystem;
    final Animator.AnimatorListener mHideListener = new AnimatorListenerAdapter() {
        /* class com.android.internal.app.WindowDecorActionBar.AnonymousClass1 */

        @Override // android.animation.Animator.AnimatorListener, android.animation.AnimatorListenerAdapter
        public void onAnimationEnd(Animator animation) {
            if (WindowDecorActionBar.this.mContentAnimations && WindowDecorActionBar.this.mContentView != null) {
                WindowDecorActionBar.this.mContentView.setTranslationY(0.0f);
                WindowDecorActionBar.this.mContainerView.setTranslationY(0.0f);
            }
            if (WindowDecorActionBar.this.mSplitView != null && WindowDecorActionBar.this.mContextDisplayMode == 1) {
                WindowDecorActionBar.this.mSplitView.setVisibility(8);
            }
            WindowDecorActionBar.this.mContainerView.setVisibility(8);
            WindowDecorActionBar.this.mContainerView.setTransitioning(false);
            WindowDecorActionBar.this.mCurrentShowAnim = null;
            WindowDecorActionBar.this.completeDeferredDestroyActionMode();
            if (WindowDecorActionBar.this.mOverlayLayout != null) {
                WindowDecorActionBar.this.mOverlayLayout.requestApplyInsets();
            }
        }
    };
    boolean mHideOnContentScroll;
    private boolean mLastMenuVisibility;
    private ArrayList<ActionBar.OnMenuVisibilityListener> mMenuVisibilityListeners = new ArrayList<>();
    private boolean mNowShowing = true;
    private ActionBarOverlayLayout mOverlayLayout;
    private int mSavedTabPosition = -1;
    private TabImpl mSelectedTab;
    private boolean mShowHideAnimationEnabled;
    final Animator.AnimatorListener mShowListener = new AnimatorListenerAdapter() {
        /* class com.android.internal.app.WindowDecorActionBar.AnonymousClass2 */

        @Override // android.animation.Animator.AnimatorListener, android.animation.AnimatorListenerAdapter
        public void onAnimationEnd(Animator animation) {
            WindowDecorActionBar.this.mCurrentShowAnim = null;
            WindowDecorActionBar.this.mContainerView.requestLayout();
        }
    };
    private boolean mShowingForMode;
    private ActionBarContainer mSplitView;
    private ScrollingTabContainerView mTabScrollView;
    private ArrayList<TabImpl> mTabs = new ArrayList<>();
    private Context mThemedContext;
    final ValueAnimator.AnimatorUpdateListener mUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        /* class com.android.internal.app.WindowDecorActionBar.AnonymousClass3 */

        @Override // android.animation.ValueAnimator.AnimatorUpdateListener
        public void onAnimationUpdate(ValueAnimator animation) {
            ((View) WindowDecorActionBar.this.mContainerView.getParent()).invalidate();
        }
    };

    public WindowDecorActionBar(Activity activity) {
        this.mActivity = activity;
        View decor = activity.getWindow().getDecorView();
        boolean overlayMode = this.mActivity.getWindow().hasFeature(9);
        init(decor);
        if (!overlayMode) {
            this.mContentView = decor.findViewById(R.id.content);
        }
    }

    public WindowDecorActionBar(Dialog dialog) {
        this.mDialog = dialog;
        init(dialog.getWindow().getDecorView());
    }

    public WindowDecorActionBar(View layout) {
        if ($assertionsDisabled || layout.isInEditMode()) {
            init(layout);
            return;
        }
        throw new AssertionError();
    }

    private void init(View decor) {
        int i;
        boolean homeAsUp;
        boolean z;
        this.mOverlayLayout = (ActionBarOverlayLayout) decor.findViewById(R.id.decor_content_parent);
        if (this.mOverlayLayout != null) {
            this.mOverlayLayout.setActionBarVisibilityCallback(this);
        }
        this.mDecorToolbar = getDecorToolbar(decor.findViewById(R.id.action_bar));
        this.mContextView = (ActionBarContextView) decor.findViewById(R.id.action_context_bar);
        this.mContainerView = (ActionBarContainer) decor.findViewById(R.id.action_bar_container);
        this.mSplitView = (ActionBarContainer) decor.findViewById(R.id.split_action_bar);
        if (this.mDecorToolbar == null || this.mContextView == null || this.mContainerView == null) {
            throw new IllegalStateException(getClass().getSimpleName() + " can only be used " + "with a compatible window decor layout");
        }
        this.mContext = this.mDecorToolbar.getContext();
        if (this.mDecorToolbar.isSplit()) {
            i = 1;
        } else {
            i = 0;
        }
        this.mContextDisplayMode = i;
        if ((this.mDecorToolbar.getDisplayOptions() & 4) != 0) {
            homeAsUp = true;
        } else {
            homeAsUp = false;
        }
        if (homeAsUp) {
            this.mDisplayHomeAsUpSet = true;
        }
        ActionBarPolicy abp = ActionBarPolicy.get(this.mContext);
        if (abp.enableHomeButtonByDefault() || homeAsUp) {
            z = true;
        } else {
            z = false;
        }
        setHomeButtonEnabled(z);
        setHasEmbeddedTabs(abp.hasEmbeddedTabs());
        TypedArray a = this.mContext.obtainStyledAttributes(null, R.styleable.ActionBar, 16843470, 0);
        if (a.getBoolean(21, false)) {
            setHideOnContentScrollEnabled(true);
        }
        int elevation = a.getDimensionPixelSize(20, 0);
        if (elevation != 0) {
            setElevation((float) elevation);
        }
        a.recycle();
    }

    private DecorToolbar getDecorToolbar(View view) {
        if (view instanceof DecorToolbar) {
            return (DecorToolbar) view;
        }
        if (view instanceof Toolbar) {
            return ((Toolbar) view).getWrapper();
        }
        throw new IllegalStateException("Can't make a decor toolbar out of " + view.getClass().getSimpleName());
    }

    public void setElevation(float elevation) {
        this.mContainerView.setElevation(elevation);
        if (this.mSplitView != null) {
            this.mSplitView.setElevation(elevation);
        }
    }

    public float getElevation() {
        return this.mContainerView.getElevation();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        setHasEmbeddedTabs(ActionBarPolicy.get(this.mContext).hasEmbeddedTabs());
    }

    private void setHasEmbeddedTabs(boolean hasEmbeddedTabs) {
        boolean isInTabMode;
        boolean z;
        boolean z2 = true;
        this.mHasEmbeddedTabs = hasEmbeddedTabs;
        if (!this.mHasEmbeddedTabs) {
            this.mDecorToolbar.setEmbeddedTabView(null);
            this.mContainerView.setTabContainer(this.mTabScrollView);
        } else {
            this.mContainerView.setTabContainer(null);
            this.mDecorToolbar.setEmbeddedTabView(this.mTabScrollView);
        }
        if (getNavigationMode() == 2) {
            isInTabMode = true;
        } else {
            isInTabMode = false;
        }
        if (this.mTabScrollView != null) {
            if (isInTabMode) {
                this.mTabScrollView.setVisibility(0);
                if (this.mOverlayLayout != null) {
                    this.mOverlayLayout.requestApplyInsets();
                }
            } else {
                this.mTabScrollView.setVisibility(8);
            }
        }
        DecorToolbar decorToolbar = this.mDecorToolbar;
        if (this.mHasEmbeddedTabs || !isInTabMode) {
            z = false;
        } else {
            z = true;
        }
        decorToolbar.setCollapsible(z);
        ActionBarOverlayLayout actionBarOverlayLayout = this.mOverlayLayout;
        if (this.mHasEmbeddedTabs || !isInTabMode) {
            z2 = false;
        }
        actionBarOverlayLayout.setHasNonEmbeddedTabs(z2);
    }

    private void ensureTabsExist() {
        if (this.mTabScrollView == null) {
            ScrollingTabContainerView tabScroller = new ScrollingTabContainerView(this.mContext);
            if (this.mHasEmbeddedTabs) {
                tabScroller.setVisibility(0);
                this.mDecorToolbar.setEmbeddedTabView(tabScroller);
            } else {
                if (getNavigationMode() == 2) {
                    tabScroller.setVisibility(0);
                    if (this.mOverlayLayout != null) {
                        this.mOverlayLayout.requestApplyInsets();
                    }
                } else {
                    tabScroller.setVisibility(8);
                }
                this.mContainerView.setTabContainer(tabScroller);
            }
            this.mTabScrollView = tabScroller;
        }
    }

    /* access modifiers changed from: package-private */
    public void completeDeferredDestroyActionMode() {
        if (this.mDeferredModeDestroyCallback != null) {
            this.mDeferredModeDestroyCallback.onDestroyActionMode(this.mDeferredDestroyActionMode);
            this.mDeferredDestroyActionMode = null;
            this.mDeferredModeDestroyCallback = null;
        }
    }

    @Override // com.android.internal.widget.ActionBarOverlayLayout.ActionBarVisibilityCallback
    public void onWindowVisibilityChanged(int visibility) {
        this.mCurWindowVisibility = visibility;
    }

    public void setShowHideAnimationEnabled(boolean enabled) {
        this.mShowHideAnimationEnabled = enabled;
        if (!enabled && this.mCurrentShowAnim != null) {
            this.mCurrentShowAnim.end();
        }
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

    @Override // android.app.ActionBar
    public void setCustomView(int resId) {
        setCustomView(LayoutInflater.from(getThemedContext()).inflate(resId, this.mDecorToolbar.getViewGroup(), false));
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

    public void setHomeButtonEnabled(boolean enable) {
        this.mDecorToolbar.setHomeButtonEnabled(enable);
    }

    @Override // android.app.ActionBar
    public void setTitle(int resId) {
        setTitle(this.mContext.getString(resId));
    }

    @Override // android.app.ActionBar
    public void setSubtitle(int resId) {
        setSubtitle(this.mContext.getString(resId));
    }

    public void setSelectedNavigationItem(int position) {
        switch (this.mDecorToolbar.getNavigationMode()) {
            case 1:
                this.mDecorToolbar.setDropdownSelectedPosition(position);
                return;
            case 2:
                selectTab(this.mTabs.get(position));
                return;
            default:
                throw new IllegalStateException("setSelectedNavigationIndex not valid for current navigation mode");
        }
    }

    public void removeAllTabs() {
        cleanupTabs();
    }

    private void cleanupTabs() {
        if (this.mSelectedTab != null) {
            selectTab(null);
        }
        this.mTabs.clear();
        if (this.mTabScrollView != null) {
            this.mTabScrollView.removeAllTabs();
        }
        this.mSavedTabPosition = -1;
    }

    @Override // android.app.ActionBar
    public void setTitle(CharSequence title) {
        this.mDecorToolbar.setTitle(title);
    }

    public void setWindowTitle(CharSequence title) {
        this.mDecorToolbar.setWindowTitle(title);
    }

    @Override // android.app.ActionBar
    public void setSubtitle(CharSequence subtitle) {
        this.mDecorToolbar.setSubtitle(subtitle);
    }

    public void setDisplayOptions(int options) {
        if ((options & 4) != 0) {
            this.mDisplayHomeAsUpSet = true;
        }
        this.mDecorToolbar.setDisplayOptions(options);
    }

    public void setDisplayOptions(int options, int mask) {
        int current = this.mDecorToolbar.getDisplayOptions();
        if ((mask & 4) != 0) {
            this.mDisplayHomeAsUpSet = true;
        }
        this.mDecorToolbar.setDisplayOptions((options & mask) | ((mask ^ -1) & current));
    }

    public void setBackgroundDrawable(Drawable d) {
        this.mContainerView.setPrimaryBackground(d);
    }

    public void setStackedBackgroundDrawable(Drawable d) {
        this.mContainerView.setStackedBackground(d);
    }

    public void setSplitBackgroundDrawable(Drawable d) {
        if (this.mSplitView != null) {
            this.mSplitView.setSplitBackground(d);
        }
    }

    public View getCustomView() {
        return this.mDecorToolbar.getCustomView();
    }

    public CharSequence getTitle() {
        return this.mDecorToolbar.getTitle();
    }

    public CharSequence getSubtitle() {
        return this.mDecorToolbar.getSubtitle();
    }

    public int getNavigationMode() {
        return this.mDecorToolbar.getNavigationMode();
    }

    public int getDisplayOptions() {
        return this.mDecorToolbar.getDisplayOptions();
    }

    public ActionMode startActionMode(ActionMode.Callback callback) {
        if (this.mActionMode != null) {
            this.mActionMode.finish();
        }
        this.mOverlayLayout.setHideOnContentScrollEnabled(false);
        this.mContextView.killMode();
        ActionModeImpl mode = new ActionModeImpl(callback);
        if (!mode.dispatchOnCreate()) {
            return null;
        }
        mode.invalidate();
        this.mContextView.initForMode(mode);
        animateToMode(true);
        if (!(this.mSplitView == null || this.mContextDisplayMode != 1 || this.mSplitView.getVisibility() == 0)) {
            this.mSplitView.setVisibility(0);
            if (this.mOverlayLayout != null) {
                this.mOverlayLayout.requestApplyInsets();
            }
        }
        this.mContextView.sendAccessibilityEvent(32);
        this.mActionMode = mode;
        return mode;
    }

    private void configureTab(ActionBar.Tab tab, int position) {
        TabImpl tabi = (TabImpl) tab;
        if (tabi.getCallback() == null) {
            throw new IllegalStateException("Action Bar Tab must have a Callback");
        }
        tabi.setPosition(position);
        this.mTabs.add(position, tabi);
        int count = this.mTabs.size();
        for (int i = position + 1; i < count; i++) {
            this.mTabs.get(i).setPosition(i);
        }
    }

    public void addTab(ActionBar.Tab tab) {
        addTab(tab, this.mTabs.isEmpty());
    }

    @Override // android.app.ActionBar
    public void addTab(ActionBar.Tab tab, int position) {
        addTab(tab, position, this.mTabs.isEmpty());
    }

    @Override // android.app.ActionBar
    public void addTab(ActionBar.Tab tab, boolean setSelected) {
        ensureTabsExist();
        this.mTabScrollView.addTab(tab, setSelected);
        configureTab(tab, this.mTabs.size());
        if (setSelected) {
            selectTab(tab);
        }
    }

    public void addTab(ActionBar.Tab tab, int position, boolean setSelected) {
        ensureTabsExist();
        this.mTabScrollView.addTab(tab, position, setSelected);
        configureTab(tab, position);
        if (setSelected) {
            selectTab(tab);
        }
    }

    public ActionBar.Tab newTab() {
        return new TabImpl();
    }

    public void removeTab(ActionBar.Tab tab) {
        removeTabAt(tab.getPosition());
    }

    public void removeTabAt(int position) {
        if (this.mTabScrollView != null) {
            int selectedTabPosition = this.mSelectedTab != null ? this.mSelectedTab.getPosition() : this.mSavedTabPosition;
            this.mTabScrollView.removeTabAt(position);
            TabImpl removedTab = this.mTabs.remove(position);
            if (removedTab != null) {
                removedTab.setPosition(-1);
            }
            int newTabCount = this.mTabs.size();
            for (int i = position; i < newTabCount; i++) {
                this.mTabs.get(i).setPosition(i);
            }
            if (selectedTabPosition == position) {
                selectTab(this.mTabs.isEmpty() ? null : this.mTabs.get(Math.max(0, position - 1)));
            }
        }
    }

    public void selectTab(ActionBar.Tab tab) {
        int i = -1;
        if (getNavigationMode() != 2) {
            if (tab != null) {
                i = tab.getPosition();
            }
            this.mSavedTabPosition = i;
            return;
        }
        FragmentTransaction trans = this.mDecorToolbar.getViewGroup().isInEditMode() ? null : this.mActivity.getFragmentManager().beginTransaction().disallowAddToBackStack();
        if (this.mSelectedTab != tab) {
            ScrollingTabContainerView scrollingTabContainerView = this.mTabScrollView;
            if (tab != null) {
                i = tab.getPosition();
            }
            scrollingTabContainerView.setTabSelected(i);
            if (this.mSelectedTab != null) {
                this.mSelectedTab.getCallback().onTabUnselected(this.mSelectedTab, trans);
            }
            this.mSelectedTab = (TabImpl) tab;
            if (this.mSelectedTab != null) {
                this.mSelectedTab.getCallback().onTabSelected(this.mSelectedTab, trans);
            }
        } else if (this.mSelectedTab != null) {
            this.mSelectedTab.getCallback().onTabReselected(this.mSelectedTab, trans);
            this.mTabScrollView.animateToTab(tab.getPosition());
        }
        if (trans != null && !trans.isEmpty()) {
            trans.commit();
        }
    }

    public ActionBar.Tab getSelectedTab() {
        return this.mSelectedTab;
    }

    public int getHeight() {
        return this.mContainerView.getHeight();
    }

    @Override // com.android.internal.widget.ActionBarOverlayLayout.ActionBarVisibilityCallback
    public void enableContentAnimations(boolean enabled) {
        this.mContentAnimations = enabled;
    }

    public void show() {
        if (this.mHiddenByApp) {
            this.mHiddenByApp = false;
            updateVisibility(false);
        }
    }

    private void showForActionMode() {
        if (!this.mShowingForMode) {
            this.mShowingForMode = true;
            if (this.mOverlayLayout != null) {
                this.mOverlayLayout.setShowingForActionMode(true);
            }
            updateVisibility(false);
        }
    }

    @Override // com.android.internal.widget.ActionBarOverlayLayout.ActionBarVisibilityCallback
    public void showForSystem() {
        if (this.mHiddenBySystem) {
            this.mHiddenBySystem = false;
            updateVisibility(true);
        }
    }

    public void hide() {
        if (!this.mHiddenByApp) {
            this.mHiddenByApp = true;
            updateVisibility(false);
        }
    }

    private void hideForActionMode() {
        if (this.mShowingForMode) {
            this.mShowingForMode = false;
            if (this.mOverlayLayout != null) {
                this.mOverlayLayout.setShowingForActionMode(false);
            }
            updateVisibility(false);
        }
    }

    @Override // com.android.internal.widget.ActionBarOverlayLayout.ActionBarVisibilityCallback
    public void hideForSystem() {
        if (!this.mHiddenBySystem) {
            this.mHiddenBySystem = true;
            updateVisibility(true);
        }
    }

    public void setHideOnContentScrollEnabled(boolean hideOnContentScroll) {
        if (!hideOnContentScroll || this.mOverlayLayout.isInOverlayMode()) {
            this.mHideOnContentScroll = hideOnContentScroll;
            this.mOverlayLayout.setHideOnContentScrollEnabled(hideOnContentScroll);
            return;
        }
        throw new IllegalStateException("Action bar must be in overlay mode (Window.FEATURE_OVERLAY_ACTION_BAR) to enable hide on content scroll");
    }

    public boolean isHideOnContentScrollEnabled() {
        return this.mOverlayLayout.isHideOnContentScrollEnabled();
    }

    public int getHideOffset() {
        return this.mOverlayLayout.getActionBarHideOffset();
    }

    public void setHideOffset(int offset) {
        if (offset == 0 || this.mOverlayLayout.isInOverlayMode()) {
            this.mOverlayLayout.setActionBarHideOffset(offset);
            return;
        }
        throw new IllegalStateException("Action bar must be in overlay mode (Window.FEATURE_OVERLAY_ACTION_BAR) to set a non-zero hide offset");
    }

    /* access modifiers changed from: private */
    public static boolean checkShowingFlags(boolean hiddenByApp, boolean hiddenBySystem, boolean showingForMode) {
        if (showingForMode) {
            return true;
        }
        if (hiddenByApp || hiddenBySystem) {
            return false;
        }
        return true;
    }

    private void updateVisibility(boolean fromSystem) {
        if (checkShowingFlags(this.mHiddenByApp, this.mHiddenBySystem, this.mShowingForMode)) {
            if (!this.mNowShowing) {
                this.mNowShowing = true;
                doShow(fromSystem);
            }
        } else if (this.mNowShowing) {
            this.mNowShowing = false;
            doHide(fromSystem);
        }
    }

    public void doShow(boolean fromSystem) {
        if (this.mCurrentShowAnim != null) {
            this.mCurrentShowAnim.end();
        }
        this.mContainerView.setVisibility(0);
        if (this.mCurWindowVisibility != 0 || (!this.mShowHideAnimationEnabled && !fromSystem)) {
            this.mContainerView.setAlpha(1.0f);
            this.mContainerView.setTranslationY(0.0f);
            if (this.mContentAnimations && this.mContentView != null) {
                this.mContentView.setTranslationY(0.0f);
            }
            if (this.mSplitView != null && this.mContextDisplayMode == 1) {
                this.mSplitView.setAlpha(1.0f);
                this.mSplitView.setTranslationY(0.0f);
                this.mSplitView.setVisibility(0);
            }
            this.mShowListener.onAnimationEnd(null);
        } else {
            this.mContainerView.setTranslationY(0.0f);
            float startingY = (float) (-this.mContainerView.getHeight());
            if (fromSystem) {
                int[] topLeft = {0, 0};
                this.mContainerView.getLocationInWindow(topLeft);
                startingY -= (float) topLeft[1];
            }
            this.mContainerView.setTranslationY(startingY);
            AnimatorSet anim = new AnimatorSet();
            ObjectAnimator a = ObjectAnimator.ofFloat(this.mContainerView, View.TRANSLATION_Y, 0.0f);
            a.addUpdateListener(this.mUpdateListener);
            AnimatorSet.Builder b = anim.play(a);
            if (this.mContentAnimations && this.mContentView != null) {
                b.with(ObjectAnimator.ofFloat(this.mContentView, View.TRANSLATION_Y, startingY, 0.0f));
            }
            if (this.mSplitView != null && this.mContextDisplayMode == 1) {
                this.mSplitView.setTranslationY((float) this.mSplitView.getHeight());
                this.mSplitView.setVisibility(0);
                b.with(ObjectAnimator.ofFloat(this.mSplitView, View.TRANSLATION_Y, 0.0f));
            }
            anim.setInterpolator(AnimationUtils.loadInterpolator(this.mContext, R.interpolator.decelerate_cubic));
            anim.setDuration(250L);
            anim.addListener(this.mShowListener);
            this.mCurrentShowAnim = anim;
            anim.start();
        }
        if (this.mOverlayLayout != null) {
            this.mOverlayLayout.requestApplyInsets();
        }
    }

    public void doHide(boolean fromSystem) {
        if (this.mCurrentShowAnim != null) {
            this.mCurrentShowAnim.end();
        }
        if (this.mCurWindowVisibility != 0 || (!this.mShowHideAnimationEnabled && !fromSystem)) {
            this.mHideListener.onAnimationEnd(null);
            return;
        }
        this.mContainerView.setAlpha(1.0f);
        this.mContainerView.setTransitioning(true);
        AnimatorSet anim = new AnimatorSet();
        float endingY = (float) (-this.mContainerView.getHeight());
        if (fromSystem) {
            int[] topLeft = {0, 0};
            this.mContainerView.getLocationInWindow(topLeft);
            endingY -= (float) topLeft[1];
        }
        ObjectAnimator a = ObjectAnimator.ofFloat(this.mContainerView, View.TRANSLATION_Y, endingY);
        a.addUpdateListener(this.mUpdateListener);
        AnimatorSet.Builder b = anim.play(a);
        if (this.mContentAnimations && this.mContentView != null) {
            b.with(ObjectAnimator.ofFloat(this.mContentView, View.TRANSLATION_Y, 0.0f, endingY));
        }
        if (this.mSplitView != null && this.mSplitView.getVisibility() == 0) {
            this.mSplitView.setAlpha(1.0f);
            b.with(ObjectAnimator.ofFloat(this.mSplitView, View.TRANSLATION_Y, (float) this.mSplitView.getHeight()));
        }
        anim.setInterpolator(AnimationUtils.loadInterpolator(this.mContext, R.interpolator.accelerate_cubic));
        anim.setDuration(250L);
        anim.addListener(this.mHideListener);
        this.mCurrentShowAnim = anim;
        anim.start();
    }

    public boolean isShowing() {
        int height = getHeight();
        return this.mNowShowing && (height == 0 || getHideOffset() < height);
    }

    /* access modifiers changed from: package-private */
    public void animateToMode(boolean toActionMode) {
        int i;
        int i2 = 0;
        if (toActionMode) {
            showForActionMode();
        } else {
            hideForActionMode();
        }
        DecorToolbar decorToolbar = this.mDecorToolbar;
        if (toActionMode) {
            i = 8;
        } else {
            i = 0;
        }
        decorToolbar.animateToVisibility(i);
        ActionBarContextView actionBarContextView = this.mContextView;
        if (!toActionMode) {
            i2 = 8;
        }
        actionBarContextView.animateToVisibility(i2);
    }

    public Context getThemedContext() {
        if (this.mThemedContext == null) {
            TypedValue outValue = new TypedValue();
            this.mContext.getTheme().resolveAttribute(16843671, outValue, true);
            int targetThemeRes = outValue.resourceId;
            if (targetThemeRes == 0 || this.mContext.getThemeResId() == targetThemeRes) {
                this.mThemedContext = this.mContext;
            } else {
                this.mThemedContext = new ContextThemeWrapper(this.mContext, targetThemeRes);
            }
        }
        return this.mThemedContext;
    }

    public boolean isTitleTruncated() {
        return this.mDecorToolbar != null && this.mDecorToolbar.isTitleTruncated();
    }

    @Override // android.app.ActionBar
    public void setHomeAsUpIndicator(Drawable indicator) {
        this.mDecorToolbar.setNavigationIcon(indicator);
    }

    @Override // android.app.ActionBar
    public void setHomeAsUpIndicator(int resId) {
        this.mDecorToolbar.setNavigationIcon(resId);
    }

    @Override // android.app.ActionBar
    public void setHomeActionContentDescription(CharSequence description) {
        this.mDecorToolbar.setNavigationContentDescription(description);
    }

    @Override // android.app.ActionBar
    public void setHomeActionContentDescription(int resId) {
        this.mDecorToolbar.setNavigationContentDescription(resId);
    }

    @Override // com.android.internal.widget.ActionBarOverlayLayout.ActionBarVisibilityCallback
    public void onContentScrollStarted() {
        if (this.mCurrentShowAnim != null) {
            this.mCurrentShowAnim.cancel();
            this.mCurrentShowAnim = null;
        }
    }

    @Override // com.android.internal.widget.ActionBarOverlayLayout.ActionBarVisibilityCallback
    public void onContentScrollStopped() {
    }

    public boolean collapseActionView() {
        if (this.mDecorToolbar == null || !this.mDecorToolbar.hasExpandedActionView()) {
            return false;
        }
        this.mDecorToolbar.collapseActionView();
        return true;
    }

    public class ActionModeImpl extends ActionMode implements MenuBuilder.Callback {
        private ActionMode.Callback mCallback;
        private WeakReference<View> mCustomView;
        private MenuBuilder mMenu;

        public ActionModeImpl(ActionMode.Callback callback) {
            this.mCallback = callback;
            this.mMenu = new MenuBuilder(WindowDecorActionBar.this.getThemedContext()).setDefaultShowAsAction(1);
            this.mMenu.setCallback(this);
        }

        public MenuInflater getMenuInflater() {
            return new MenuInflater(WindowDecorActionBar.this.getThemedContext());
        }

        public Menu getMenu() {
            return this.mMenu;
        }

        public void finish() {
            if (WindowDecorActionBar.this.mActionMode == this) {
                if (!WindowDecorActionBar.checkShowingFlags(WindowDecorActionBar.this.mHiddenByApp, WindowDecorActionBar.this.mHiddenBySystem, false)) {
                    WindowDecorActionBar.this.mDeferredDestroyActionMode = this;
                    WindowDecorActionBar.this.mDeferredModeDestroyCallback = this.mCallback;
                } else {
                    this.mCallback.onDestroyActionMode(this);
                }
                this.mCallback = null;
                WindowDecorActionBar.this.animateToMode(false);
                WindowDecorActionBar.this.mContextView.closeMode();
                WindowDecorActionBar.this.mDecorToolbar.getViewGroup().sendAccessibilityEvent(32);
                WindowDecorActionBar.this.mOverlayLayout.setHideOnContentScrollEnabled(WindowDecorActionBar.this.mHideOnContentScroll);
                WindowDecorActionBar.this.mActionMode = null;
            }
        }

        public void invalidate() {
            this.mMenu.stopDispatchingItemsChanged();
            try {
                this.mCallback.onPrepareActionMode(this, this.mMenu);
            } finally {
                this.mMenu.startDispatchingItemsChanged();
            }
        }

        public boolean dispatchOnCreate() {
            this.mMenu.stopDispatchingItemsChanged();
            try {
                return this.mCallback.onCreateActionMode(this, this.mMenu);
            } finally {
                this.mMenu.startDispatchingItemsChanged();
            }
        }

        public void setCustomView(View view) {
            WindowDecorActionBar.this.mContextView.setCustomView(view);
            this.mCustomView = new WeakReference<>(view);
        }

        @Override // android.view.ActionMode
        public void setSubtitle(CharSequence subtitle) {
            WindowDecorActionBar.this.mContextView.setSubtitle(subtitle);
        }

        @Override // android.view.ActionMode
        public void setTitle(CharSequence title) {
            WindowDecorActionBar.this.mContextView.setTitle(title);
        }

        @Override // android.view.ActionMode
        public void setTitle(int resId) {
            setTitle(WindowDecorActionBar.this.mContext.getResources().getString(resId));
        }

        @Override // android.view.ActionMode
        public void setSubtitle(int resId) {
            setSubtitle(WindowDecorActionBar.this.mContext.getResources().getString(resId));
        }

        public CharSequence getTitle() {
            return WindowDecorActionBar.this.mContextView.getTitle();
        }

        public CharSequence getSubtitle() {
            return WindowDecorActionBar.this.mContextView.getSubtitle();
        }

        public void setTitleOptionalHint(boolean titleOptional) {
            super.setTitleOptionalHint(titleOptional);
            WindowDecorActionBar.this.mContextView.setTitleOptional(titleOptional);
        }

        public boolean isTitleOptional() {
            return WindowDecorActionBar.this.mContextView.isTitleOptional();
        }

        public View getCustomView() {
            if (this.mCustomView != null) {
                return this.mCustomView.get();
            }
            return null;
        }

        @Override // com.android.internal.view.menu.MenuBuilder.Callback
        public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
            if (this.mCallback != null) {
                return this.mCallback.onActionItemClicked(this, item);
            }
            return false;
        }

        public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
        }

        public boolean onSubMenuSelected(SubMenuBuilder subMenu) {
            if (this.mCallback == null) {
                return false;
            }
            if (!subMenu.hasVisibleItems()) {
                return true;
            }
            new MenuPopupHelper(WindowDecorActionBar.this.getThemedContext(), subMenu).show();
            return true;
        }

        public void onCloseSubMenu(SubMenuBuilder menu) {
        }

        @Override // com.android.internal.view.menu.MenuBuilder.Callback
        public void onMenuModeChange(MenuBuilder menu) {
            if (this.mCallback != null) {
                invalidate();
                WindowDecorActionBar.this.mContextView.showOverflowMenu();
            }
        }
    }

    public class TabImpl extends ActionBar.Tab {
        private ActionBar.TabListener mCallback;
        private CharSequence mContentDesc;
        private View mCustomView;
        private Drawable mIcon;
        private int mPosition = -1;
        private Object mTag;
        private CharSequence mText;

        public TabImpl() {
        }

        public Object getTag() {
            return this.mTag;
        }

        public ActionBar.Tab setTag(Object tag) {
            this.mTag = tag;
            return this;
        }

        public ActionBar.TabListener getCallback() {
            return this.mCallback;
        }

        public ActionBar.Tab setTabListener(ActionBar.TabListener callback) {
            this.mCallback = callback;
            return this;
        }

        public View getCustomView() {
            return this.mCustomView;
        }

        @Override // android.app.ActionBar.Tab
        public ActionBar.Tab setCustomView(View view) {
            this.mCustomView = view;
            if (this.mPosition >= 0) {
                WindowDecorActionBar.this.mTabScrollView.updateTab(this.mPosition);
            }
            return this;
        }

        @Override // android.app.ActionBar.Tab
        public ActionBar.Tab setCustomView(int layoutResId) {
            return setCustomView(LayoutInflater.from(WindowDecorActionBar.this.getThemedContext()).inflate(layoutResId, (ViewGroup) null));
        }

        public Drawable getIcon() {
            return this.mIcon;
        }

        public int getPosition() {
            return this.mPosition;
        }

        public void setPosition(int position) {
            this.mPosition = position;
        }

        public CharSequence getText() {
            return this.mText;
        }

        @Override // android.app.ActionBar.Tab
        public ActionBar.Tab setIcon(Drawable icon) {
            this.mIcon = icon;
            if (this.mPosition >= 0) {
                WindowDecorActionBar.this.mTabScrollView.updateTab(this.mPosition);
            }
            return this;
        }

        @Override // android.app.ActionBar.Tab
        public ActionBar.Tab setIcon(int resId) {
            return setIcon(WindowDecorActionBar.this.mContext.getDrawable(resId));
        }

        @Override // android.app.ActionBar.Tab
        public ActionBar.Tab setText(CharSequence text) {
            this.mText = text;
            if (this.mPosition >= 0) {
                WindowDecorActionBar.this.mTabScrollView.updateTab(this.mPosition);
            }
            return this;
        }

        @Override // android.app.ActionBar.Tab
        public ActionBar.Tab setText(int resId) {
            return setText(WindowDecorActionBar.this.mContext.getResources().getText(resId));
        }

        public void select() {
            WindowDecorActionBar.this.selectTab(this);
        }

        @Override // android.app.ActionBar.Tab
        public ActionBar.Tab setContentDescription(int resId) {
            return setContentDescription(WindowDecorActionBar.this.mContext.getResources().getText(resId));
        }

        @Override // android.app.ActionBar.Tab
        public ActionBar.Tab setContentDescription(CharSequence contentDesc) {
            this.mContentDesc = contentDesc;
            if (this.mPosition >= 0) {
                WindowDecorActionBar.this.mTabScrollView.updateTab(this.mPosition);
            }
            return this;
        }

        public CharSequence getContentDescription() {
            return this.mContentDesc;
        }
    }

    @Override // android.app.ActionBar
    public void setCustomView(View view) {
        this.mDecorToolbar.setCustomView(view);
    }

    public void setCustomView(View view, ActionBar.LayoutParams layoutParams) {
        view.setLayoutParams(layoutParams);
        this.mDecorToolbar.setCustomView(view);
    }

    public void setListNavigationCallbacks(SpinnerAdapter adapter, ActionBar.OnNavigationListener callback) {
        this.mDecorToolbar.setDropdownParams(adapter, new NavItemSelectedListener(callback));
    }

    public int getSelectedNavigationIndex() {
        switch (this.mDecorToolbar.getNavigationMode()) {
            case 1:
                return this.mDecorToolbar.getDropdownSelectedPosition();
            case 2:
                if (this.mSelectedTab != null) {
                    return this.mSelectedTab.getPosition();
                }
                return -1;
            default:
                return -1;
        }
    }

    public int getNavigationItemCount() {
        switch (this.mDecorToolbar.getNavigationMode()) {
            case 1:
                return this.mDecorToolbar.getDropdownItemCount();
            case 2:
                return this.mTabs.size();
            default:
                return 0;
        }
    }

    public int getTabCount() {
        return this.mTabs.size();
    }

    public void setNavigationMode(int mode) {
        boolean z;
        boolean z2 = true;
        int oldMode = this.mDecorToolbar.getNavigationMode();
        switch (oldMode) {
            case 2:
                this.mSavedTabPosition = getSelectedNavigationIndex();
                selectTab(null);
                this.mTabScrollView.setVisibility(8);
                break;
        }
        if (!(oldMode == mode || this.mHasEmbeddedTabs || this.mOverlayLayout == null)) {
            this.mOverlayLayout.requestFitSystemWindows();
        }
        this.mDecorToolbar.setNavigationMode(mode);
        switch (mode) {
            case 2:
                ensureTabsExist();
                this.mTabScrollView.setVisibility(0);
                if (this.mSavedTabPosition != -1) {
                    setSelectedNavigationItem(this.mSavedTabPosition);
                    this.mSavedTabPosition = -1;
                    break;
                }
                break;
        }
        DecorToolbar decorToolbar = this.mDecorToolbar;
        if (mode != 2 || this.mHasEmbeddedTabs) {
            z = false;
        } else {
            z = true;
        }
        decorToolbar.setCollapsible(z);
        ActionBarOverlayLayout actionBarOverlayLayout = this.mOverlayLayout;
        if (mode != 2 || this.mHasEmbeddedTabs) {
            z2 = false;
        }
        actionBarOverlayLayout.setHasNonEmbeddedTabs(z2);
    }

    public ActionBar.Tab getTabAt(int index) {
        return this.mTabs.get(index);
    }

    @Override // android.app.ActionBar
    public void setIcon(int resId) {
        this.mDecorToolbar.setIcon(resId);
    }

    @Override // android.app.ActionBar
    public void setIcon(Drawable icon) {
        this.mDecorToolbar.setIcon(icon);
    }

    public boolean hasIcon() {
        return this.mDecorToolbar.hasIcon();
    }

    @Override // android.app.ActionBar
    public void setLogo(int resId) {
        this.mDecorToolbar.setLogo(resId);
    }

    @Override // android.app.ActionBar
    public void setLogo(Drawable logo) {
        this.mDecorToolbar.setLogo(logo);
    }

    public boolean hasLogo() {
        return this.mDecorToolbar.hasLogo();
    }

    public void setDefaultDisplayHomeAsUpEnabled(boolean enable) {
        if (!this.mDisplayHomeAsUpSet) {
            setDisplayHomeAsUpEnabled(enable);
        }
    }
}

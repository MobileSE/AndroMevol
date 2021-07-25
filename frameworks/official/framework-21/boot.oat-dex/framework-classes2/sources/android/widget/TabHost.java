package android.widget;

import android.app.LocalActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.TabWidget;
import com.android.internal.R;
import com.android.internal.util.Protocol;
import java.util.ArrayList;
import java.util.List;

public class TabHost extends FrameLayout implements ViewTreeObserver.OnTouchModeChangeListener {
    private static final int TABWIDGET_LOCATION_BOTTOM = 3;
    private static final int TABWIDGET_LOCATION_LEFT = 0;
    private static final int TABWIDGET_LOCATION_RIGHT = 2;
    private static final int TABWIDGET_LOCATION_TOP = 1;
    protected int mCurrentTab;
    private View mCurrentView;
    protected LocalActivityManager mLocalActivityManager;
    private OnTabChangeListener mOnTabChangeListener;
    private FrameLayout mTabContent;
    private View.OnKeyListener mTabKeyListener;
    private int mTabLayoutId;
    private List<TabSpec> mTabSpecs;
    private TabWidget mTabWidget;

    /* access modifiers changed from: private */
    public interface ContentStrategy {
        View getContentView();

        void tabClosed();
    }

    /* access modifiers changed from: private */
    public interface IndicatorStrategy {
        View createIndicatorView();
    }

    public interface OnTabChangeListener {
        void onTabChanged(String str);
    }

    public interface TabContentFactory {
        View createTabContent(String str);
    }

    public TabHost(Context context) {
        super(context);
        this.mTabSpecs = new ArrayList(2);
        this.mCurrentTab = -1;
        this.mCurrentView = null;
        this.mLocalActivityManager = null;
        initTabHost();
    }

    public TabHost(Context context, AttributeSet attrs) {
        this(context, attrs, 16842883);
    }

    public TabHost(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TabHost(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs);
        this.mTabSpecs = new ArrayList(2);
        this.mCurrentTab = -1;
        this.mCurrentView = null;
        this.mLocalActivityManager = null;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TabWidget, defStyleAttr, defStyleRes);
        this.mTabLayoutId = a.getResourceId(4, 0);
        a.recycle();
        if (this.mTabLayoutId == 0) {
            this.mTabLayoutId = R.layout.tab_indicator_holo;
        }
        initTabHost();
    }

    private void initTabHost() {
        setFocusableInTouchMode(true);
        setDescendantFocusability(Protocol.BASE_DATA_CONNECTION);
        this.mCurrentTab = -1;
        this.mCurrentView = null;
    }

    public TabSpec newTabSpec(String tag) {
        return new TabSpec(tag);
    }

    public void setup() {
        this.mTabWidget = (TabWidget) findViewById(R.id.tabs);
        if (this.mTabWidget == null) {
            throw new RuntimeException("Your TabHost must have a TabWidget whose id attribute is 'android.R.id.tabs'");
        }
        this.mTabKeyListener = new View.OnKeyListener() {
            /* class android.widget.TabHost.AnonymousClass1 */

            public boolean onKey(View v, int keyCode, KeyEvent event) {
                switch (keyCode) {
                    case 19:
                    case 20:
                    case 21:
                    case 22:
                    case 23:
                    case 66:
                        return false;
                    default:
                        TabHost.this.mTabContent.requestFocus(2);
                        return TabHost.this.mTabContent.dispatchKeyEvent(event);
                }
            }
        };
        this.mTabWidget.setTabSelectionListener(new TabWidget.OnTabSelectionChanged() {
            /* class android.widget.TabHost.AnonymousClass2 */

            @Override // android.widget.TabWidget.OnTabSelectionChanged
            public void onTabSelectionChanged(int tabIndex, boolean clicked) {
                TabHost.this.setCurrentTab(tabIndex);
                if (clicked) {
                    TabHost.this.mTabContent.requestFocus(2);
                }
            }
        });
        this.mTabContent = (FrameLayout) findViewById(R.id.tabcontent);
        if (this.mTabContent == null) {
            throw new RuntimeException("Your TabHost must have a FrameLayout whose id attribute is 'android.R.id.tabcontent'");
        }
    }

    public void sendAccessibilityEvent(int eventType) {
    }

    public void setup(LocalActivityManager activityGroup) {
        setup();
        this.mLocalActivityManager = activityGroup;
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnTouchModeChangeListener(this);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeOnTouchModeChangeListener(this);
    }

    public void onTouchModeChanged(boolean isInTouchMode) {
        if (!isInTouchMode && this.mCurrentView != null) {
            if (!this.mCurrentView.hasFocus() || this.mCurrentView.isFocused()) {
                this.mTabWidget.getChildTabViewAt(this.mCurrentTab).requestFocus();
            }
        }
    }

    public void addTab(TabSpec tabSpec) {
        if (tabSpec.mIndicatorStrategy == null) {
            throw new IllegalArgumentException("you must specify a way to create the tab indicator.");
        } else if (tabSpec.mContentStrategy == null) {
            throw new IllegalArgumentException("you must specify a way to create the tab content");
        } else {
            View tabIndicator = tabSpec.mIndicatorStrategy.createIndicatorView();
            tabIndicator.setOnKeyListener(this.mTabKeyListener);
            if (tabSpec.mIndicatorStrategy instanceof ViewIndicatorStrategy) {
                this.mTabWidget.setStripEnabled(false);
            }
            this.mTabWidget.addView(tabIndicator);
            this.mTabSpecs.add(tabSpec);
            if (this.mCurrentTab == -1) {
                setCurrentTab(0);
            }
        }
    }

    public void clearAllTabs() {
        this.mTabWidget.removeAllViews();
        initTabHost();
        this.mTabContent.removeAllViews();
        this.mTabSpecs.clear();
        requestLayout();
        invalidate();
    }

    public TabWidget getTabWidget() {
        return this.mTabWidget;
    }

    public int getCurrentTab() {
        return this.mCurrentTab;
    }

    public String getCurrentTabTag() {
        if (this.mCurrentTab < 0 || this.mCurrentTab >= this.mTabSpecs.size()) {
            return null;
        }
        return this.mTabSpecs.get(this.mCurrentTab).getTag();
    }

    public View getCurrentTabView() {
        if (this.mCurrentTab < 0 || this.mCurrentTab >= this.mTabSpecs.size()) {
            return null;
        }
        return this.mTabWidget.getChildTabViewAt(this.mCurrentTab);
    }

    public View getCurrentView() {
        return this.mCurrentView;
    }

    public void setCurrentTabByTag(String tag) {
        for (int i = 0; i < this.mTabSpecs.size(); i++) {
            if (this.mTabSpecs.get(i).getTag().equals(tag)) {
                setCurrentTab(i);
                return;
            }
        }
    }

    public FrameLayout getTabContentView() {
        return this.mTabContent;
    }

    private int getTabWidgetLocation() {
        switch (this.mTabWidget.getOrientation()) {
            case 1:
                return this.mTabContent.getLeft() < this.mTabWidget.getLeft() ? 2 : 0;
            default:
                return this.mTabContent.getTop() < this.mTabWidget.getTop() ? 3 : 1;
        }
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCodeShouldChangeFocus;
        int directionShouldChangeFocus;
        int soundEffect;
        boolean handled = super.dispatchKeyEvent(event);
        if (handled || event.getAction() != 0 || this.mCurrentView == null || !this.mCurrentView.isRootNamespace() || !this.mCurrentView.hasFocus()) {
            return handled;
        }
        switch (getTabWidgetLocation()) {
            case 0:
                keyCodeShouldChangeFocus = 21;
                directionShouldChangeFocus = 17;
                soundEffect = 1;
                break;
            case 1:
            default:
                keyCodeShouldChangeFocus = 19;
                directionShouldChangeFocus = 33;
                soundEffect = 2;
                break;
            case 2:
                keyCodeShouldChangeFocus = 22;
                directionShouldChangeFocus = 66;
                soundEffect = 3;
                break;
            case 3:
                keyCodeShouldChangeFocus = 20;
                directionShouldChangeFocus = R.styleable.Theme_textSelectHandleWindowStyle;
                soundEffect = 4;
                break;
        }
        if (event.getKeyCode() != keyCodeShouldChangeFocus || this.mCurrentView.findFocus().focusSearch(directionShouldChangeFocus) != null) {
            return handled;
        }
        this.mTabWidget.getChildTabViewAt(this.mCurrentTab).requestFocus();
        playSoundEffect(soundEffect);
        return true;
    }

    public void dispatchWindowFocusChanged(boolean hasFocus) {
        if (this.mCurrentView != null) {
            this.mCurrentView.dispatchWindowFocusChanged(hasFocus);
        }
    }

    @Override // android.widget.FrameLayout
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(TabHost.class.getName());
    }

    @Override // android.widget.FrameLayout
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(TabHost.class.getName());
    }

    public void setCurrentTab(int index) {
        if (index >= 0 && index < this.mTabSpecs.size() && index != this.mCurrentTab) {
            if (this.mCurrentTab != -1) {
                this.mTabSpecs.get(this.mCurrentTab).mContentStrategy.tabClosed();
            }
            this.mCurrentTab = index;
            this.mTabWidget.focusCurrentTab(this.mCurrentTab);
            this.mCurrentView = this.mTabSpecs.get(index).mContentStrategy.getContentView();
            if (this.mCurrentView.getParent() == null) {
                this.mTabContent.addView(this.mCurrentView, new ViewGroup.LayoutParams(-1, -1));
            }
            if (!this.mTabWidget.hasFocus()) {
                this.mCurrentView.requestFocus();
            }
            invokeOnTabChangeListener();
        }
    }

    public void setOnTabChangedListener(OnTabChangeListener l) {
        this.mOnTabChangeListener = l;
    }

    private void invokeOnTabChangeListener() {
        if (this.mOnTabChangeListener != null) {
            this.mOnTabChangeListener.onTabChanged(getCurrentTabTag());
        }
    }

    public class TabSpec {
        private ContentStrategy mContentStrategy;
        private IndicatorStrategy mIndicatorStrategy;
        private String mTag;

        private TabSpec(String tag) {
            this.mTag = tag;
        }

        public TabSpec setIndicator(CharSequence label) {
            this.mIndicatorStrategy = new LabelIndicatorStrategy(label);
            return this;
        }

        public TabSpec setIndicator(CharSequence label, Drawable icon) {
            this.mIndicatorStrategy = new LabelAndIconIndicatorStrategy(label, icon);
            return this;
        }

        public TabSpec setIndicator(View view) {
            this.mIndicatorStrategy = new ViewIndicatorStrategy(view);
            return this;
        }

        public TabSpec setContent(int viewId) {
            this.mContentStrategy = new ViewIdContentStrategy(viewId);
            return this;
        }

        public TabSpec setContent(TabContentFactory contentFactory) {
            this.mContentStrategy = new FactoryContentStrategy(TabHost.this, this.mTag, contentFactory);
            return this;
        }

        public TabSpec setContent(Intent intent) {
            this.mContentStrategy = new IntentContentStrategy(this.mTag, intent);
            return this;
        }

        public String getTag() {
            return this.mTag;
        }
    }

    private class LabelIndicatorStrategy implements IndicatorStrategy {
        private final CharSequence mLabel;

        private LabelIndicatorStrategy(CharSequence label) {
            this.mLabel = label;
        }

        @Override // android.widget.TabHost.IndicatorStrategy
        public View createIndicatorView() {
            Context context = TabHost.this.getContext();
            View tabIndicator = ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(TabHost.this.mTabLayoutId, (ViewGroup) TabHost.this.mTabWidget, false);
            TextView tv = (TextView) tabIndicator.findViewById(R.id.title);
            tv.setText(this.mLabel);
            if (context.getApplicationInfo().targetSdkVersion <= 4) {
                tabIndicator.setBackgroundResource(R.drawable.tab_indicator_v4);
                tv.setTextColor(context.getResources().getColorStateList(R.color.tab_indicator_text_v4));
            }
            return tabIndicator;
        }
    }

    private class LabelAndIconIndicatorStrategy implements IndicatorStrategy {
        private final Drawable mIcon;
        private final CharSequence mLabel;

        private LabelAndIconIndicatorStrategy(CharSequence label, Drawable icon) {
            this.mLabel = label;
            this.mIcon = icon;
        }

        @Override // android.widget.TabHost.IndicatorStrategy
        public View createIndicatorView() {
            boolean exclusive;
            boolean bindIcon = true;
            Context context = TabHost.this.getContext();
            View tabIndicator = ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(TabHost.this.mTabLayoutId, (ViewGroup) TabHost.this.mTabWidget, false);
            TextView tv = (TextView) tabIndicator.findViewById(R.id.title);
            ImageView iconView = (ImageView) tabIndicator.findViewById(R.id.icon);
            if (iconView.getVisibility() == 8) {
                exclusive = true;
            } else {
                exclusive = false;
            }
            if (exclusive && !TextUtils.isEmpty(this.mLabel)) {
                bindIcon = false;
            }
            tv.setText(this.mLabel);
            if (bindIcon && this.mIcon != null) {
                iconView.setImageDrawable(this.mIcon);
                iconView.setVisibility(0);
            }
            if (context.getApplicationInfo().targetSdkVersion <= 4) {
                tabIndicator.setBackgroundResource(R.drawable.tab_indicator_v4);
                tv.setTextColor(context.getResources().getColorStateList(R.color.tab_indicator_text_v4));
            }
            return tabIndicator;
        }
    }

    private class ViewIndicatorStrategy implements IndicatorStrategy {
        private final View mView;

        private ViewIndicatorStrategy(View view) {
            this.mView = view;
        }

        @Override // android.widget.TabHost.IndicatorStrategy
        public View createIndicatorView() {
            return this.mView;
        }
    }

    private class ViewIdContentStrategy implements ContentStrategy {
        private final View mView;

        private ViewIdContentStrategy(int viewId) {
            this.mView = TabHost.this.mTabContent.findViewById(viewId);
            if (this.mView != null) {
                this.mView.setVisibility(8);
                return;
            }
            throw new RuntimeException("Could not create tab content because could not find view with id " + viewId);
        }

        @Override // android.widget.TabHost.ContentStrategy
        public View getContentView() {
            this.mView.setVisibility(0);
            return this.mView;
        }

        @Override // android.widget.TabHost.ContentStrategy
        public void tabClosed() {
            this.mView.setVisibility(8);
        }
    }

    private class IntentContentStrategy implements ContentStrategy {
        private final Intent mIntent;
        private View mLaunchedView;
        private final String mTag;

        private IntentContentStrategy(String tag, Intent intent) {
            this.mTag = tag;
            this.mIntent = intent;
        }

        @Override // android.widget.TabHost.ContentStrategy
        public View getContentView() {
            if (TabHost.this.mLocalActivityManager == null) {
                throw new IllegalStateException("Did you forget to call 'public void setup(LocalActivityManager activityGroup)'?");
            }
            Window w = TabHost.this.mLocalActivityManager.startActivity(this.mTag, this.mIntent);
            View wd = w != null ? w.getDecorView() : null;
            if (!(this.mLaunchedView == wd || this.mLaunchedView == null || this.mLaunchedView.getParent() == null)) {
                TabHost.this.mTabContent.removeView(this.mLaunchedView);
            }
            this.mLaunchedView = wd;
            if (this.mLaunchedView != null) {
                this.mLaunchedView.setVisibility(0);
                this.mLaunchedView.setFocusableInTouchMode(true);
                ((ViewGroup) this.mLaunchedView).setDescendantFocusability(Protocol.BASE_DATA_CONNECTION);
            }
            return this.mLaunchedView;
        }

        @Override // android.widget.TabHost.ContentStrategy
        public void tabClosed() {
            if (this.mLaunchedView != null) {
                this.mLaunchedView.setVisibility(8);
            }
        }
    }
}

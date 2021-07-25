package android.widget;

import android.view.View;
import android.widget.TabHost;

class TabHost$FactoryContentStrategy implements TabHost.ContentStrategy {
    private TabHost.TabContentFactory mFactory;
    private View mTabContent;
    private final CharSequence mTag;
    final /* synthetic */ TabHost this$0;

    public TabHost$FactoryContentStrategy(TabHost tabHost, CharSequence tag, TabHost.TabContentFactory factory) {
        this.this$0 = tabHost;
        this.mTag = tag;
        this.mFactory = factory;
    }

    public View getContentView() {
        if (this.mTabContent == null) {
            this.mTabContent = this.mFactory.createTabContent(this.mTag.toString());
        }
        this.mTabContent.setVisibility(0);
        return this.mTabContent;
    }

    public void tabClosed() {
        this.mTabContent.setVisibility(8);
    }
}

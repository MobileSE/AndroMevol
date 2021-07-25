package android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewDebug;
import android.view.ViewGroup;

public class AbsListView$LayoutParams extends ViewGroup.LayoutParams {
    @ViewDebug.ExportedProperty(category = "list")
    boolean forceAdd;
    long itemId = -1;
    @ViewDebug.ExportedProperty(category = "list")
    boolean recycledHeaderFooter;
    int scrappedFromPosition;
    @ViewDebug.ExportedProperty(category = "list", mapping = {@ViewDebug.IntToString(from = -1, to = "ITEM_VIEW_TYPE_IGNORE"), @ViewDebug.IntToString(from = -2, to = "ITEM_VIEW_TYPE_HEADER_OR_FOOTER")})
    int viewType;

    public AbsListView$LayoutParams(Context c, AttributeSet attrs) {
        super(c, attrs);
    }

    public AbsListView$LayoutParams(int w, int h) {
        super(w, h);
    }

    public AbsListView$LayoutParams(int w, int h, int viewType2) {
        super(w, h);
        this.viewType = viewType2;
    }

    public AbsListView$LayoutParams(ViewGroup.LayoutParams source) {
        super(source);
    }
}

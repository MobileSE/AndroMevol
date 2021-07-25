package android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.android.internal.R;

public class TableRow$LayoutParams extends LinearLayout.LayoutParams {
    private static final int LOCATION = 0;
    private static final int LOCATION_NEXT = 1;
    @ViewDebug.ExportedProperty(category = "layout")
    public int column;
    private int[] mOffset;
    @ViewDebug.ExportedProperty(category = "layout")
    public int span;

    public TableRow$LayoutParams(Context c, AttributeSet attrs) {
        super(c, attrs);
        this.mOffset = new int[2];
        TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.TableRow_Cell);
        this.column = a.getInt(0, -1);
        this.span = a.getInt(1, 1);
        if (this.span <= 1) {
            this.span = 1;
        }
        a.recycle();
    }

    public TableRow$LayoutParams(int w, int h) {
        super(w, h);
        this.mOffset = new int[2];
        this.column = -1;
        this.span = 1;
    }

    public TableRow$LayoutParams(int w, int h, float initWeight) {
        super(w, h, initWeight);
        this.mOffset = new int[2];
        this.column = -1;
        this.span = 1;
    }

    public TableRow$LayoutParams() {
        super(-1, -2);
        this.mOffset = new int[2];
        this.column = -1;
        this.span = 1;
    }

    public TableRow$LayoutParams(int column2) {
        this();
        this.column = column2;
    }

    public TableRow$LayoutParams(ViewGroup.LayoutParams p) {
        super(p);
        this.mOffset = new int[2];
    }

    public TableRow$LayoutParams(ViewGroup.MarginLayoutParams source) {
        super(source);
        this.mOffset = new int[2];
    }

    /* access modifiers changed from: protected */
    public void setBaseAttributes(TypedArray a, int widthAttr, int heightAttr) {
        if (a.hasValue(widthAttr)) {
            this.width = a.getLayoutDimension(widthAttr, "layout_width");
        } else {
            this.width = -1;
        }
        if (a.hasValue(heightAttr)) {
            this.height = a.getLayoutDimension(heightAttr, "layout_height");
        } else {
            this.height = -2;
        }
    }
}

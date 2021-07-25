package android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.ViewGroup;
import com.android.internal.R;

public class AbsoluteLayout$LayoutParams extends ViewGroup.LayoutParams {
    public int x;
    public int y;

    public AbsoluteLayout$LayoutParams(int width, int height, int x2, int y2) {
        super(width, height);
        this.x = x2;
        this.y = y2;
    }

    public AbsoluteLayout$LayoutParams(Context c, AttributeSet attrs) {
        super(c, attrs);
        TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.AbsoluteLayout_Layout);
        this.x = a.getDimensionPixelOffset(0, 0);
        this.y = a.getDimensionPixelOffset(1, 0);
        a.recycle();
    }

    public AbsoluteLayout$LayoutParams(ViewGroup.LayoutParams source) {
        super(source);
    }

    @Override // android.view.ViewGroup.LayoutParams
    public String debug(String output) {
        return output + "Absolute.LayoutParams={width=" + sizeToString(this.width) + ", height=" + sizeToString(this.height) + " x=" + this.x + " y=" + this.y + "}";
    }
}

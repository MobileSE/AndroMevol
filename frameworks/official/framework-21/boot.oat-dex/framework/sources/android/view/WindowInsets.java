package android.view;

import android.graphics.Rect;

public final class WindowInsets {
    public static final WindowInsets CONSUMED = new WindowInsets(null, null, null, false);
    private static final Rect EMPTY_RECT = new Rect(0, 0, 0, 0);
    private boolean mIsRound;
    private Rect mStableInsets;
    private boolean mStableInsetsConsumed;
    private Rect mSystemWindowInsets;
    private boolean mSystemWindowInsetsConsumed;
    private Rect mTempRect;
    private Rect mWindowDecorInsets;
    private boolean mWindowDecorInsetsConsumed;

    public WindowInsets(Rect systemWindowInsets, Rect windowDecorInsets, Rect stableInsets, boolean isRound) {
        boolean z;
        boolean z2;
        boolean z3 = true;
        this.mSystemWindowInsetsConsumed = false;
        this.mWindowDecorInsetsConsumed = false;
        this.mStableInsetsConsumed = false;
        if (systemWindowInsets == null) {
            z = true;
        } else {
            z = false;
        }
        this.mSystemWindowInsetsConsumed = z;
        this.mSystemWindowInsets = this.mSystemWindowInsetsConsumed ? EMPTY_RECT : systemWindowInsets;
        if (windowDecorInsets == null) {
            z2 = true;
        } else {
            z2 = false;
        }
        this.mWindowDecorInsetsConsumed = z2;
        this.mWindowDecorInsets = this.mWindowDecorInsetsConsumed ? EMPTY_RECT : windowDecorInsets;
        this.mStableInsetsConsumed = stableInsets != null ? false : z3;
        this.mStableInsets = this.mStableInsetsConsumed ? EMPTY_RECT : stableInsets;
        this.mIsRound = isRound;
    }

    public WindowInsets(WindowInsets src) {
        this.mSystemWindowInsetsConsumed = false;
        this.mWindowDecorInsetsConsumed = false;
        this.mStableInsetsConsumed = false;
        this.mSystemWindowInsets = src.mSystemWindowInsets;
        this.mWindowDecorInsets = src.mWindowDecorInsets;
        this.mStableInsets = src.mStableInsets;
        this.mSystemWindowInsetsConsumed = src.mSystemWindowInsetsConsumed;
        this.mWindowDecorInsetsConsumed = src.mWindowDecorInsetsConsumed;
        this.mStableInsetsConsumed = src.mStableInsetsConsumed;
        this.mIsRound = src.mIsRound;
    }

    public WindowInsets(Rect systemWindowInsets) {
        this(systemWindowInsets, null, null, false);
    }

    public Rect getSystemWindowInsets() {
        if (this.mTempRect == null) {
            this.mTempRect = new Rect();
        }
        if (this.mSystemWindowInsets != null) {
            this.mTempRect.set(this.mSystemWindowInsets);
        } else {
            this.mTempRect.setEmpty();
        }
        return this.mTempRect;
    }

    public int getSystemWindowInsetLeft() {
        return this.mSystemWindowInsets.left;
    }

    public int getSystemWindowInsetTop() {
        return this.mSystemWindowInsets.top;
    }

    public int getSystemWindowInsetRight() {
        return this.mSystemWindowInsets.right;
    }

    public int getSystemWindowInsetBottom() {
        return this.mSystemWindowInsets.bottom;
    }

    public int getWindowDecorInsetLeft() {
        return this.mWindowDecorInsets.left;
    }

    public int getWindowDecorInsetTop() {
        return this.mWindowDecorInsets.top;
    }

    public int getWindowDecorInsetRight() {
        return this.mWindowDecorInsets.right;
    }

    public int getWindowDecorInsetBottom() {
        return this.mWindowDecorInsets.bottom;
    }

    public boolean hasSystemWindowInsets() {
        return (this.mSystemWindowInsets.left == 0 && this.mSystemWindowInsets.top == 0 && this.mSystemWindowInsets.right == 0 && this.mSystemWindowInsets.bottom == 0) ? false : true;
    }

    public boolean hasWindowDecorInsets() {
        return (this.mWindowDecorInsets.left == 0 && this.mWindowDecorInsets.top == 0 && this.mWindowDecorInsets.right == 0 && this.mWindowDecorInsets.bottom == 0) ? false : true;
    }

    public boolean hasInsets() {
        return hasSystemWindowInsets() || hasWindowDecorInsets();
    }

    public boolean isConsumed() {
        return this.mSystemWindowInsetsConsumed && this.mWindowDecorInsetsConsumed && this.mStableInsetsConsumed;
    }

    public boolean isRound() {
        return this.mIsRound;
    }

    public WindowInsets consumeSystemWindowInsets() {
        WindowInsets result = new WindowInsets(this);
        result.mSystemWindowInsets = EMPTY_RECT;
        result.mSystemWindowInsetsConsumed = true;
        return result;
    }

    public WindowInsets consumeSystemWindowInsets(boolean left, boolean top, boolean right, boolean bottom) {
        int i = 0;
        if (!left && !top && !right && !bottom) {
            return this;
        }
        WindowInsets result = new WindowInsets(this);
        int i2 = left ? 0 : this.mSystemWindowInsets.left;
        int i3 = top ? 0 : this.mSystemWindowInsets.top;
        int i4 = right ? 0 : this.mSystemWindowInsets.right;
        if (!bottom) {
            i = this.mSystemWindowInsets.bottom;
        }
        result.mSystemWindowInsets = new Rect(i2, i3, i4, i);
        return result;
    }

    public WindowInsets replaceSystemWindowInsets(int left, int top, int right, int bottom) {
        WindowInsets result = new WindowInsets(this);
        result.mSystemWindowInsets = new Rect(left, top, right, bottom);
        return result;
    }

    public WindowInsets replaceSystemWindowInsets(Rect systemWindowInsets) {
        WindowInsets result = new WindowInsets(this);
        result.mSystemWindowInsets = new Rect(systemWindowInsets);
        return result;
    }

    public WindowInsets consumeWindowDecorInsets() {
        WindowInsets result = new WindowInsets(this);
        result.mWindowDecorInsets.set(0, 0, 0, 0);
        result.mWindowDecorInsetsConsumed = true;
        return result;
    }

    public WindowInsets consumeWindowDecorInsets(boolean left, boolean top, boolean right, boolean bottom) {
        int i = 0;
        if (!left && !top && !right && !bottom) {
            return this;
        }
        WindowInsets result = new WindowInsets(this);
        int i2 = left ? 0 : this.mWindowDecorInsets.left;
        int i3 = top ? 0 : this.mWindowDecorInsets.top;
        int i4 = right ? 0 : this.mWindowDecorInsets.right;
        if (!bottom) {
            i = this.mWindowDecorInsets.bottom;
        }
        result.mWindowDecorInsets = new Rect(i2, i3, i4, i);
        return result;
    }

    public WindowInsets replaceWindowDecorInsets(int left, int top, int right, int bottom) {
        WindowInsets result = new WindowInsets(this);
        result.mWindowDecorInsets = new Rect(left, top, right, bottom);
        return result;
    }

    public int getStableInsetTop() {
        return this.mStableInsets.top;
    }

    public int getStableInsetLeft() {
        return this.mStableInsets.left;
    }

    public int getStableInsetRight() {
        return this.mStableInsets.right;
    }

    public int getStableInsetBottom() {
        return this.mStableInsets.bottom;
    }

    public boolean hasStableInsets() {
        return (this.mStableInsets.top == 0 && this.mStableInsets.left == 0 && this.mStableInsets.right == 0 && this.mStableInsets.bottom == 0) ? false : true;
    }

    public WindowInsets consumeStableInsets() {
        WindowInsets result = new WindowInsets(this);
        result.mStableInsets = EMPTY_RECT;
        result.mStableInsetsConsumed = true;
        return result;
    }

    public String toString() {
        return "WindowInsets{systemWindowInsets=" + this.mSystemWindowInsets + " windowDecorInsets=" + this.mWindowDecorInsets + " stableInsets=" + this.mStableInsets + (isRound() ? " round}" : "}");
    }
}

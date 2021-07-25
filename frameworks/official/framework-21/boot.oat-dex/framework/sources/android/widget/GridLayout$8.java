package android.widget;

import android.view.View;
import android.widget.GridLayout;

class GridLayout$8 extends GridLayout.Alignment {
    GridLayout$8() {
    }

    /* access modifiers changed from: package-private */
    public int getGravityOffset(View view, int cellDelta) {
        return 0;
    }

    public int getAlignmentValue(View view, int viewSize, int mode) {
        return Integer.MIN_VALUE;
    }

    public int getSizeInCell(View view, int viewSize, int cellSize) {
        return cellSize;
    }
}

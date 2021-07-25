package android.widget;

import android.widget.AbsListView;

class AbsListView$PositionScroller$2 implements Runnable {
    final /* synthetic */ AbsListView.PositionScroller this$1;
    final /* synthetic */ int val$boundPosition;
    final /* synthetic */ int val$position;

    AbsListView$PositionScroller$2(AbsListView.PositionScroller positionScroller, int i, int i2) {
        this.this$1 = positionScroller;
        this.val$position = i;
        this.val$boundPosition = i2;
    }

    public void run() {
        this.this$1.start(this.val$position, this.val$boundPosition);
    }
}

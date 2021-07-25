package android.widget;

import android.widget.AbsListView;

class AbsListView$PositionScroller$1 implements Runnable {
    final /* synthetic */ AbsListView.PositionScroller this$1;
    final /* synthetic */ int val$position;

    AbsListView$PositionScroller$1(AbsListView.PositionScroller positionScroller, int i) {
        this.this$1 = positionScroller;
        this.val$position = i;
    }

    public void run() {
        this.this$1.start(this.val$position);
    }
}

package android.widget;

import android.view.View;
import android.widget.AbsListView;

class AbsListView$CheckForLongPress extends AbsListView.WindowRunnnable implements Runnable {
    final /* synthetic */ AbsListView this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    private AbsListView$CheckForLongPress(AbsListView absListView) {
        super(absListView, (AbsListView.1) null);
        this.this$0 = absListView;
    }

    public void run() {
        View child = this.this$0.getChildAt(this.this$0.mMotionPosition - this.this$0.mFirstPosition);
        if (child != null) {
            int longPressPosition = this.this$0.mMotionPosition;
            long longPressId = this.this$0.mAdapter.getItemId(this.this$0.mMotionPosition);
            boolean handled = false;
            if (sameWindow() && !this.this$0.mDataChanged) {
                handled = this.this$0.performLongPress(child, longPressPosition, longPressId);
            }
            if (handled) {
                this.this$0.mTouchMode = -1;
                this.this$0.setPressed(false);
                child.setPressed(false);
                return;
            }
            this.this$0.mTouchMode = 2;
        }
    }
}

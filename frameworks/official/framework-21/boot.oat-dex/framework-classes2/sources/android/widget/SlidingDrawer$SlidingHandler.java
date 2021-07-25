package android.widget;

import android.os.Handler;
import android.os.Message;

class SlidingDrawer$SlidingHandler extends Handler {
    final /* synthetic */ SlidingDrawer this$0;

    private SlidingDrawer$SlidingHandler(SlidingDrawer slidingDrawer) {
        this.this$0 = slidingDrawer;
    }

    public void handleMessage(Message m) {
        switch (m.what) {
            case 1000:
                SlidingDrawer.access$400(this.this$0);
                return;
            default:
                return;
        }
    }
}

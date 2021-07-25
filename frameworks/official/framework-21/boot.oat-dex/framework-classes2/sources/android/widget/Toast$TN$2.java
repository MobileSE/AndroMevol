package android.widget;

import android.widget.Toast;

class Toast$TN$2 implements Runnable {
    final /* synthetic */ Toast.TN this$0;

    Toast$TN$2(Toast.TN tn) {
        this.this$0 = tn;
    }

    public void run() {
        this.this$0.handleHide();
        this.this$0.mNextView = null;
    }
}

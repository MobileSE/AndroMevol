package android.widget;

import android.widget.Toast;

class Toast$TN$1 implements Runnable {
    final /* synthetic */ Toast.TN this$0;

    Toast$TN$1(Toast.TN tn) {
        this.this$0 = tn;
    }

    public void run() {
        this.this$0.handleShow();
    }
}

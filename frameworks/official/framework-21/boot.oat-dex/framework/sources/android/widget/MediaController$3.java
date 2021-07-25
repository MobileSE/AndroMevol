package android.widget;

import android.os.Handler;
import android.os.Message;

class MediaController$3 extends Handler {
    final /* synthetic */ MediaController this$0;

    MediaController$3(MediaController mediaController) {
        this.this$0 = mediaController;
    }

    @Override // android.os.Handler
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                this.this$0.hide();
                return;
            case 2:
                int pos = MediaController.access$500(this.this$0);
                if (!MediaController.access$600(this.this$0) && MediaController.access$100(this.this$0) && MediaController.access$700(this.this$0).isPlaying()) {
                    sendMessageDelayed(obtainMessage(2), (long) (1000 - (pos % 1000)));
                    return;
                }
                return;
            default:
                return;
        }
    }
}

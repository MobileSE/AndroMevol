package android.widget;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

class Chronometer$1 extends Handler {
    final /* synthetic */ Chronometer this$0;

    Chronometer$1(Chronometer chronometer) {
        this.this$0 = chronometer;
    }

    public void handleMessage(Message m) {
        if (Chronometer.access$000(this.this$0)) {
            Chronometer.access$100(this.this$0, SystemClock.elapsedRealtime());
            this.this$0.dispatchChronometerTick();
            sendMessageDelayed(Message.obtain(this, 2), 1000);
        }
    }
}

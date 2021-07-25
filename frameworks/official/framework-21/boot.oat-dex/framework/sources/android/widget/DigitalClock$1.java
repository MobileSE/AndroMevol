package android.widget;

import android.os.SystemClock;
import android.text.format.DateFormat;

class DigitalClock$1 implements Runnable {
    final /* synthetic */ DigitalClock this$0;

    DigitalClock$1(DigitalClock digitalClock) {
        this.this$0 = digitalClock;
    }

    public void run() {
        if (!DigitalClock.access$000(this.this$0)) {
            this.this$0.mCalendar.setTimeInMillis(System.currentTimeMillis());
            this.this$0.setText(DateFormat.format(this.this$0.mFormat, this.this$0.mCalendar));
            this.this$0.invalidate();
            long now = SystemClock.uptimeMillis();
            DigitalClock.access$200(this.this$0).postAtTime(DigitalClock.access$100(this.this$0), now + (1000 - (now % 1000)));
        }
    }
}

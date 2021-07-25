package android.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

class TextClock$2 extends BroadcastReceiver {
    final /* synthetic */ TextClock this$0;

    TextClock$2(TextClock textClock) {
        this.this$0 = textClock;
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        if (TextClock.access$200(this.this$0) == null && Intent.ACTION_TIMEZONE_CHANGED.equals(intent.getAction())) {
            TextClock.access$300(this.this$0, intent.getStringExtra("time-zone"));
        }
        TextClock.access$100(this.this$0);
    }
}

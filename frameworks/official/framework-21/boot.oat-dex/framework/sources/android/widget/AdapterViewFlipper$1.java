package android.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

class AdapterViewFlipper$1 extends BroadcastReceiver {
    final /* synthetic */ AdapterViewFlipper this$0;

    AdapterViewFlipper$1(AdapterViewFlipper adapterViewFlipper) {
        this.this$0 = adapterViewFlipper;
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_SCREEN_OFF.equals(action)) {
            AdapterViewFlipper.access$002(this.this$0, false);
            AdapterViewFlipper.access$100(this.this$0);
        } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
            AdapterViewFlipper.access$002(this.this$0, true);
            AdapterViewFlipper.access$200(this.this$0, false);
        }
    }
}

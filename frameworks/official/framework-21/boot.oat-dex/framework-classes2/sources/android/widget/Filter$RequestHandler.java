package android.widget;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

class Filter$RequestHandler extends Handler {
    final /* synthetic */ Filter this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public Filter$RequestHandler(Filter filter, Looper looper) {
        super(looper);
        this.this$0 = filter;
    }

    public void handleMessage(Message msg) {
        int what = msg.what;
        switch (what) {
            case -791613427:
                Filter$RequestArguments args = (Filter$RequestArguments) msg.obj;
                try {
                    args.results = this.this$0.performFiltering(args.constraint);
                } catch (Exception e) {
                    args.results = new Filter$FilterResults();
                    Log.w("Filter", "An exception occured during performFiltering()!", e);
                } finally {
                    Message message = Filter.access$200(this.this$0).obtainMessage(what);
                    message.obj = args;
                    message.sendToTarget();
                }
                synchronized (Filter.access$300(this.this$0)) {
                    if (Filter.access$400(this.this$0) != null) {
                        Filter.access$400(this.this$0).sendMessageDelayed(Filter.access$400(this.this$0).obtainMessage(-559038737), 3000);
                    }
                }
                return;
            case -559038737:
                synchronized (Filter.access$300(this.this$0)) {
                    if (Filter.access$400(this.this$0) != null) {
                        Filter.access$400(this.this$0).getLooper().quit();
                        Filter.access$402(this.this$0, null);
                    }
                }
                return;
            default:
                return;
        }
    }
}

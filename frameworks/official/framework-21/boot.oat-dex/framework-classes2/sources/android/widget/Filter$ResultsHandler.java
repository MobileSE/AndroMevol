package android.widget;

import android.os.Handler;
import android.os.Message;

class Filter$ResultsHandler extends Handler {
    final /* synthetic */ Filter this$0;

    private Filter$ResultsHandler(Filter filter) {
        this.this$0 = filter;
    }

    public void handleMessage(Message msg) {
        Filter$RequestArguments args = (Filter$RequestArguments) msg.obj;
        this.this$0.publishResults(args.constraint, args.results);
        if (args.listener != null) {
            args.listener.onFilterComplete(args.results != null ? args.results.count : -1);
        }
    }
}

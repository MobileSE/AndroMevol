package android.widget;

import android.net.ProxyInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

public abstract class Filter {
    private static final int FILTER_TOKEN = -791613427;
    private static final int FINISH_TOKEN = -559038737;
    private static final String LOG_TAG = "Filter";
    private static final String THREAD_NAME = "Filter";
    private Delayer mDelayer;
    private final Object mLock = new Object();
    private Handler mResultHandler = new ResultsHandler(this, (1) null);
    private Handler mThreadHandler;

    public interface FilterListener {
        void onFilterComplete(int i);
    }

    /* access modifiers changed from: protected */
    public abstract FilterResults performFiltering(CharSequence charSequence);

    /* access modifiers changed from: protected */
    public abstract void publishResults(CharSequence charSequence, FilterResults filterResults);

    public void setDelayer(Delayer delayer) {
        synchronized (this.mLock) {
            this.mDelayer = delayer;
        }
    }

    public final void filter(CharSequence constraint) {
        filter(constraint, null);
    }

    public final void filter(CharSequence constraint, FilterListener listener) {
        long delay;
        String str = null;
        synchronized (this.mLock) {
            if (this.mThreadHandler == null) {
                HandlerThread thread = new HandlerThread("Filter", 10);
                thread.start();
                this.mThreadHandler = new RequestHandler(this, thread.getLooper());
            }
            if (this.mDelayer == null) {
                delay = 0;
            } else {
                delay = this.mDelayer.getPostingDelay(constraint);
            }
            Message message = this.mThreadHandler.obtainMessage(FILTER_TOKEN);
            RequestArguments args = new RequestArguments((1) null);
            if (constraint != null) {
                str = constraint.toString();
            }
            args.constraint = str;
            args.listener = listener;
            message.obj = args;
            this.mThreadHandler.removeMessages(FILTER_TOKEN);
            this.mThreadHandler.removeMessages(FINISH_TOKEN);
            this.mThreadHandler.sendMessageDelayed(message, delay);
        }
    }

    public CharSequence convertResultToString(Object resultValue) {
        return resultValue == null ? ProxyInfo.LOCAL_EXCL_LIST : resultValue.toString();
    }
}

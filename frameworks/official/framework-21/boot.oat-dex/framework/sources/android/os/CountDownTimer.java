package android.os;

public abstract class CountDownTimer {
    private static final int MSG = 1;
    private boolean mCancelled = false;
    private final long mCountdownInterval;
    private Handler mHandler = new Handler() {
        /* class android.os.CountDownTimer.AnonymousClass1 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            synchronized (CountDownTimer.this) {
                if (!CountDownTimer.this.mCancelled) {
                    long millisLeft = CountDownTimer.this.mStopTimeInFuture - SystemClock.elapsedRealtime();
                    if (millisLeft <= 0) {
                        CountDownTimer.this.onFinish();
                    } else if (millisLeft < CountDownTimer.this.mCountdownInterval) {
                        sendMessageDelayed(obtainMessage(1), millisLeft);
                    } else {
                        long lastTickStart = SystemClock.elapsedRealtime();
                        CountDownTimer.this.onTick(millisLeft);
                        long delay = (CountDownTimer.this.mCountdownInterval + lastTickStart) - SystemClock.elapsedRealtime();
                        while (delay < 0) {
                            delay += CountDownTimer.this.mCountdownInterval;
                        }
                        sendMessageDelayed(obtainMessage(1), delay);
                    }
                }
            }
        }
    };
    private final long mMillisInFuture;
    private long mStopTimeInFuture;

    public abstract void onFinish();

    public abstract void onTick(long j);

    public CountDownTimer(long millisInFuture, long countDownInterval) {
        this.mMillisInFuture = millisInFuture;
        this.mCountdownInterval = countDownInterval;
    }

    public final synchronized void cancel() {
        this.mCancelled = true;
        this.mHandler.removeMessages(1);
    }

    public final synchronized CountDownTimer start() {
        CountDownTimer countDownTimer;
        this.mCancelled = false;
        if (this.mMillisInFuture <= 0) {
            onFinish();
            countDownTimer = this;
        } else {
            this.mStopTimeInFuture = SystemClock.elapsedRealtime() + this.mMillisInFuture;
            this.mHandler.sendMessage(this.mHandler.obtainMessage(1));
            countDownTimer = this;
        }
        return countDownTimer;
    }
}

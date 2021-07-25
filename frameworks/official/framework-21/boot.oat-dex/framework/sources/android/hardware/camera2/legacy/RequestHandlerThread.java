package android.hardware.camera2.legacy;

import android.os.ConditionVariable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.MessageQueue;

public class RequestHandlerThread extends HandlerThread {
    public static final int MSG_POKE_IDLE_HANDLER = -1;
    private Handler.Callback mCallback;
    private volatile Handler mHandler;
    private final ConditionVariable mIdle = new ConditionVariable(true);
    private final MessageQueue.IdleHandler mIdleHandler = new MessageQueue.IdleHandler() {
        /* class android.hardware.camera2.legacy.RequestHandlerThread.AnonymousClass1 */

        @Override // android.os.MessageQueue.IdleHandler
        public boolean queueIdle() {
            RequestHandlerThread.this.mIdle.open();
            return false;
        }
    };
    private final ConditionVariable mStarted = new ConditionVariable(false);

    public RequestHandlerThread(String name, Handler.Callback callback) {
        super(name, 10);
        this.mCallback = callback;
    }

    /* access modifiers changed from: protected */
    @Override // android.os.HandlerThread
    public void onLooperPrepared() {
        this.mHandler = new Handler(getLooper(), this.mCallback);
        this.mStarted.open();
    }

    public void waitUntilStarted() {
        this.mStarted.block();
    }

    public Handler getHandler() {
        return this.mHandler;
    }

    public Handler waitAndGetHandler() {
        waitUntilStarted();
        return getHandler();
    }

    public boolean hasAnyMessages(int[] what) {
        synchronized (this.mHandler.getLooper().getQueue()) {
            for (int i : what) {
                if (this.mHandler.hasMessages(i)) {
                    return true;
                }
            }
            return false;
        }
    }

    public void removeMessages(int[] what) {
        synchronized (this.mHandler.getLooper().getQueue()) {
            for (int i : what) {
                this.mHandler.removeMessages(i);
            }
        }
    }

    public void waitUntilIdle() {
        Handler handler = waitAndGetHandler();
        Looper looper = handler.getLooper();
        if (!looper.isIdling()) {
            this.mIdle.close();
            looper.getQueue().addIdleHandler(this.mIdleHandler);
            handler.sendEmptyMessage(-1);
            if (!looper.isIdling()) {
                this.mIdle.block();
            }
        }
    }
}

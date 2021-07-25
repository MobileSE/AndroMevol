package android.net.http;

import android.os.SystemClock;

class Timer {
    private long mLast;
    private long mStart;

    public Timer() {
        long uptimeMillis = SystemClock.uptimeMillis();
        this.mLast = uptimeMillis;
        this.mStart = uptimeMillis;
    }

    public void mark(String message) {
        this.mLast = SystemClock.uptimeMillis();
    }
}

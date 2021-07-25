package com.android.server;

import android.os.ConditionVariable;
import android.os.SystemClock;

abstract class ResettableTimeout {
    private ConditionVariable mLock = new ConditionVariable();
    private volatile long mOffAt;
    private volatile boolean mOffCalled;
    private Thread mThread;

    public abstract void off();

    public abstract void on(boolean z);

    ResettableTimeout() {
    }

    public void go(long milliseconds) {
        boolean alreadyOn;
        synchronized (this) {
            this.mOffAt = SystemClock.uptimeMillis() + milliseconds;
            if (this.mThread == null) {
                alreadyOn = false;
                this.mLock.close();
                this.mThread = new T();
                this.mThread.start();
                this.mLock.block();
                this.mOffCalled = false;
            } else {
                alreadyOn = true;
                this.mThread.interrupt();
            }
            on(alreadyOn);
        }
    }

    public void cancel() {
        synchronized (this) {
            this.mOffAt = 0;
            if (this.mThread != null) {
                this.mThread.interrupt();
                this.mThread = null;
            }
            if (!this.mOffCalled) {
                this.mOffCalled = true;
                off();
            }
        }
    }

    private class T extends Thread {
        private T() {
        }

        /* JADX WARNING: Code restructure failed: missing block: B:10:?, code lost:
            sleep(r0);
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            /*
                r6 = this;
                com.android.server.ResettableTimeout r2 = com.android.server.ResettableTimeout.this
                android.os.ConditionVariable r2 = com.android.server.ResettableTimeout.access$100(r2)
                r2.open()
            L_0x0009:
                monitor-enter(r6)
                com.android.server.ResettableTimeout r2 = com.android.server.ResettableTimeout.this     // Catch:{ all -> 0x0036 }
                long r2 = com.android.server.ResettableTimeout.access$200(r2)     // Catch:{ all -> 0x0036 }
                long r4 = android.os.SystemClock.uptimeMillis()     // Catch:{ all -> 0x0036 }
                long r0 = r2 - r4
                r2 = 0
                int r2 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1))
                if (r2 > 0) goto L_0x002f
                com.android.server.ResettableTimeout r2 = com.android.server.ResettableTimeout.this     // Catch:{ all -> 0x0036 }
                r3 = 1
                com.android.server.ResettableTimeout.access$302(r2, r3)     // Catch:{ all -> 0x0036 }
                com.android.server.ResettableTimeout r2 = com.android.server.ResettableTimeout.this     // Catch:{ all -> 0x0036 }
                r2.off()     // Catch:{ all -> 0x0036 }
                com.android.server.ResettableTimeout r2 = com.android.server.ResettableTimeout.this     // Catch:{ all -> 0x0036 }
                r3 = 0
                com.android.server.ResettableTimeout.access$402(r2, r3)     // Catch:{ all -> 0x0036 }
                monitor-exit(r6)     // Catch:{ all -> 0x0036 }
                return
            L_0x002f:
                monitor-exit(r6)     // Catch:{ all -> 0x0036 }
                sleep(r0)     // Catch:{ InterruptedException -> 0x0034 }
                goto L_0x0009
            L_0x0034:
                r2 = move-exception
                goto L_0x0009
            L_0x0036:
                r2 = move-exception
                monitor-exit(r6)
                throw r2
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.ResettableTimeout.T.run():void");
        }
    }
}

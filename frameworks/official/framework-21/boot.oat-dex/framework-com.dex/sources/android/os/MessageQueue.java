package android.os;

import android.util.Log;
import android.util.Printer;
import java.util.ArrayList;

public final class MessageQueue {
    private boolean mBlocked;
    private final ArrayList<IdleHandler> mIdleHandlers = new ArrayList<>();
    Message mMessages;
    private int mNextBarrierToken;
    private IdleHandler[] mPendingIdleHandlers;
    private long mPtr;
    private final boolean mQuitAllowed;
    private boolean mQuitting;

    public interface IdleHandler {
        boolean queueIdle();
    }

    private static native void nativeDestroy(long j);

    private static native long nativeInit();

    private static native boolean nativeIsIdling(long j);

    private static native void nativePollOnce(long j, int i);

    private static native void nativeWake(long j);

    public void addIdleHandler(IdleHandler handler) {
        if (handler == null) {
            throw new NullPointerException("Can't add a null IdleHandler");
        }
        synchronized (this) {
            this.mIdleHandlers.add(handler);
        }
    }

    public void removeIdleHandler(IdleHandler handler) {
        synchronized (this) {
            this.mIdleHandlers.remove(handler);
        }
    }

    MessageQueue(boolean quitAllowed) {
        this.mQuitAllowed = quitAllowed;
        this.mPtr = nativeInit();
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            dispose();
        } finally {
            super.finalize();
        }
    }

    private void dispose() {
        if (this.mPtr != 0) {
            nativeDestroy(this.mPtr);
            this.mPtr = 0;
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00b3, code lost:
        r2 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x00b4, code lost:
        if (r2 >= r7) goto L_0x00e2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x00b6, code lost:
        r3 = r18.mPendingIdleHandlers[r2];
        r18.mPendingIdleHandlers[r2] = null;
        r4 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:?, code lost:
        r4 = r3.queueIdle();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x00d6, code lost:
        r11 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x00d7, code lost:
        android.util.Log.wtf("MessageQueue", "IdleHandler threw exception", r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x00e2, code lost:
        r7 = 0;
        r6 = 0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public android.os.Message next() {
        /*
        // Method dump skipped, instructions count: 230
        */
        throw new UnsupportedOperationException("Method not decompiled: android.os.MessageQueue.next():android.os.Message");
    }

    /* access modifiers changed from: package-private */
    public void quit(boolean safe) {
        if (!this.mQuitAllowed) {
            throw new IllegalStateException("Main thread not allowed to quit.");
        }
        synchronized (this) {
            if (!this.mQuitting) {
                this.mQuitting = true;
                if (safe) {
                    removeAllFutureMessagesLocked();
                } else {
                    removeAllMessagesLocked();
                }
                nativeWake(this.mPtr);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public int enqueueSyncBarrier(long when) {
        int token;
        synchronized (this) {
            token = this.mNextBarrierToken;
            this.mNextBarrierToken = token + 1;
            Message msg = Message.obtain();
            msg.markInUse();
            msg.when = when;
            msg.arg1 = token;
            Message prev = null;
            Message p = this.mMessages;
            if (when != 0) {
                while (p != null && p.when <= when) {
                    prev = p;
                    p = p.next;
                }
            }
            if (prev != null) {
                msg.next = p;
                prev.next = msg;
            } else {
                msg.next = p;
                this.mMessages = msg;
            }
        }
        return token;
    }

    /* access modifiers changed from: package-private */
    public void removeSyncBarrier(int token) {
        boolean needWake;
        synchronized (this) {
            Message prev = null;
            Message p = this.mMessages;
            while (p != null && (p.target != null || p.arg1 != token)) {
                prev = p;
                p = p.next;
            }
            if (p == null) {
                throw new IllegalStateException("The specified message queue synchronization  barrier token has not been posted or has already been removed.");
            }
            if (prev != null) {
                prev.next = p.next;
                needWake = false;
            } else {
                this.mMessages = p.next;
                needWake = this.mMessages == null || this.mMessages.target != null;
            }
            p.recycleUnchecked();
            if (needWake && !this.mQuitting) {
                nativeWake(this.mPtr);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean enqueueMessage(Message msg, long when) {
        boolean needWake = false;
        if (msg.target == null) {
            throw new IllegalArgumentException("Message must have a target.");
        } else if (msg.isInUse()) {
            throw new IllegalStateException(msg + " This message is already in use.");
        } else {
            synchronized (this) {
                if (this.mQuitting) {
                    IllegalStateException e = new IllegalStateException(msg.target + " sending message to a Handler on a dead thread");
                    Log.w("MessageQueue", e.getMessage(), e);
                    msg.recycle();
                    return false;
                }
                msg.markInUse();
                msg.when = when;
                Message p = this.mMessages;
                if (p == null || when == 0 || when < p.when) {
                    msg.next = p;
                    this.mMessages = msg;
                    needWake = this.mBlocked;
                } else {
                    if (this.mBlocked && p.target == null && msg.isAsynchronous()) {
                        needWake = true;
                    }
                    while (true) {
                        p = p.next;
                        if (p == null || when < p.when) {
                            msg.next = p;
                            p.next = msg;
                        } else if (needWake && p.isAsynchronous()) {
                            needWake = false;
                        }
                    }
                    msg.next = p;
                    p.next = msg;
                }
                if (needWake) {
                    nativeWake(this.mPtr);
                }
                return true;
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0017, code lost:
        r1 = true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean hasMessages(android.os.Handler r4, int r5, java.lang.Object r6) {
        /*
            r3 = this;
            r1 = 0
            if (r4 != 0) goto L_0x0004
        L_0x0003:
            return r1
        L_0x0004:
            monitor-enter(r3)
            android.os.Message r0 = r3.mMessages     // Catch:{ all -> 0x001a }
        L_0x0007:
            if (r0 == 0) goto L_0x0020
            android.os.Handler r2 = r0.target     // Catch:{ all -> 0x001a }
            if (r2 != r4) goto L_0x001d
            int r2 = r0.what     // Catch:{ all -> 0x001a }
            if (r2 != r5) goto L_0x001d
            if (r6 == 0) goto L_0x0017
            java.lang.Object r2 = r0.obj     // Catch:{ all -> 0x001a }
            if (r2 != r6) goto L_0x001d
        L_0x0017:
            r1 = 1
            monitor-exit(r3)     // Catch:{ all -> 0x001a }
            goto L_0x0003
        L_0x001a:
            r1 = move-exception
            monitor-exit(r3)     // Catch:{ all -> 0x001a }
            throw r1
        L_0x001d:
            android.os.Message r0 = r0.next
            goto L_0x0007
        L_0x0020:
            monitor-exit(r3)
            goto L_0x0003
        */
        throw new UnsupportedOperationException("Method not decompiled: android.os.MessageQueue.hasMessages(android.os.Handler, int, java.lang.Object):boolean");
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0017, code lost:
        r1 = true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean hasMessages(android.os.Handler r4, java.lang.Runnable r5, java.lang.Object r6) {
        /*
            r3 = this;
            r1 = 0
            if (r4 != 0) goto L_0x0004
        L_0x0003:
            return r1
        L_0x0004:
            monitor-enter(r3)
            android.os.Message r0 = r3.mMessages     // Catch:{ all -> 0x001a }
        L_0x0007:
            if (r0 == 0) goto L_0x0020
            android.os.Handler r2 = r0.target     // Catch:{ all -> 0x001a }
            if (r2 != r4) goto L_0x001d
            java.lang.Runnable r2 = r0.callback     // Catch:{ all -> 0x001a }
            if (r2 != r5) goto L_0x001d
            if (r6 == 0) goto L_0x0017
            java.lang.Object r2 = r0.obj     // Catch:{ all -> 0x001a }
            if (r2 != r6) goto L_0x001d
        L_0x0017:
            r1 = 1
            monitor-exit(r3)     // Catch:{ all -> 0x001a }
            goto L_0x0003
        L_0x001a:
            r1 = move-exception
            monitor-exit(r3)     // Catch:{ all -> 0x001a }
            throw r1
        L_0x001d:
            android.os.Message r0 = r0.next
            goto L_0x0007
        L_0x0020:
            monitor-exit(r3)
            goto L_0x0003
        */
        throw new UnsupportedOperationException("Method not decompiled: android.os.MessageQueue.hasMessages(android.os.Handler, java.lang.Runnable, java.lang.Object):boolean");
    }

    /* access modifiers changed from: package-private */
    public boolean isIdling() {
        boolean isIdlingLocked;
        synchronized (this) {
            isIdlingLocked = isIdlingLocked();
        }
        return isIdlingLocked;
    }

    private boolean isIdlingLocked() {
        return !this.mQuitting && nativeIsIdling(this.mPtr);
    }

    /* access modifiers changed from: package-private */
    public void removeMessages(Handler h, int what, Object object) {
        if (h != null) {
            synchronized (this) {
                Message p = this.mMessages;
                while (p != null && p.target == h && p.what == what && (object == null || p.obj == object)) {
                    Message n = p.next;
                    this.mMessages = n;
                    p.recycleUnchecked();
                    p = n;
                }
                while (p != null) {
                    Message n2 = p.next;
                    if (n2 != null && n2.target == h && n2.what == what && (object == null || n2.obj == object)) {
                        Message nn = n2.next;
                        n2.recycleUnchecked();
                        p.next = nn;
                    } else {
                        p = n2;
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void removeMessages(Handler h, Runnable r, Object object) {
        if (h != null && r != null) {
            synchronized (this) {
                Message p = this.mMessages;
                while (p != null && p.target == h && p.callback == r && (object == null || p.obj == object)) {
                    Message n = p.next;
                    this.mMessages = n;
                    p.recycleUnchecked();
                    p = n;
                }
                while (p != null) {
                    Message n2 = p.next;
                    if (n2 != null && n2.target == h && n2.callback == r && (object == null || n2.obj == object)) {
                        Message nn = n2.next;
                        n2.recycleUnchecked();
                        p.next = nn;
                    } else {
                        p = n2;
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void removeCallbacksAndMessages(Handler h, Object object) {
        if (h != null) {
            synchronized (this) {
                Message p = this.mMessages;
                while (p != null && p.target == h && (object == null || p.obj == object)) {
                    Message n = p.next;
                    this.mMessages = n;
                    p.recycleUnchecked();
                    p = n;
                }
                while (p != null) {
                    Message n2 = p.next;
                    if (n2 != null && n2.target == h && (object == null || n2.obj == object)) {
                        Message nn = n2.next;
                        n2.recycleUnchecked();
                        p.next = nn;
                    } else {
                        p = n2;
                    }
                }
            }
        }
    }

    private void removeAllMessagesLocked() {
        Message p = this.mMessages;
        while (p != null) {
            Message n = p.next;
            p.recycleUnchecked();
            p = n;
        }
        this.mMessages = null;
    }

    private void removeAllFutureMessagesLocked() {
        long now = SystemClock.uptimeMillis();
        Message p = this.mMessages;
        if (p == null) {
            return;
        }
        if (p.when > now) {
            removeAllMessagesLocked();
            return;
        }
        while (true) {
            Message n = p.next;
            if (n == null) {
                return;
            }
            if (n.when > now) {
                p.next = null;
                do {
                    n = n.next;
                    n.recycleUnchecked();
                } while (n != null);
                return;
            }
            p = n;
        }
    }

    /* access modifiers changed from: package-private */
    public void dump(Printer pw, String prefix) {
        synchronized (this) {
            long now = SystemClock.uptimeMillis();
            int n = 0;
            for (Message msg = this.mMessages; msg != null; msg = msg.next) {
                pw.println(prefix + "Message " + n + ": " + msg.toString(now));
                n++;
            }
            pw.println(prefix + "(Total messages: " + n + ", idling=" + isIdlingLocked() + ", quitting=" + this.mQuitting + ")");
        }
    }
}

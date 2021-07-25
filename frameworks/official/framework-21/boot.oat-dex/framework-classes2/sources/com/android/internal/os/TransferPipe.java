package com.android.internal.os;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.SystemClock;
import java.io.FileDescriptor;
import java.io.IOException;

public final class TransferPipe implements Runnable {
    static final boolean DEBUG = false;
    static final long DEFAULT_TIMEOUT = 5000;
    static final String TAG = "TransferPipe";
    String mBufferPrefix;
    boolean mComplete;
    long mEndTime;
    String mFailure;
    final ParcelFileDescriptor[] mFds = ParcelFileDescriptor.createPipe();
    FileDescriptor mOutFd;
    final Thread mThread = new Thread(this, TAG);

    /* access modifiers changed from: package-private */
    public interface Caller {
        void go(IInterface iInterface, FileDescriptor fileDescriptor, String str, String[] strArr) throws RemoteException;
    }

    /* access modifiers changed from: package-private */
    public ParcelFileDescriptor getReadFd() {
        return this.mFds[0];
    }

    public ParcelFileDescriptor getWriteFd() {
        return this.mFds[1];
    }

    public void setBufferPrefix(String prefix) {
        this.mBufferPrefix = prefix;
    }

    static void go(Caller caller, IInterface iface, FileDescriptor out, String prefix, String[] args) throws IOException, RemoteException {
        go(caller, iface, out, prefix, args, DEFAULT_TIMEOUT);
    }

    static void go(Caller caller, IInterface iface, FileDescriptor out, String prefix, String[] args, long timeout) throws IOException, RemoteException {
        if (iface.asBinder() instanceof Binder) {
            try {
                caller.go(iface, out, prefix, args);
            } catch (RemoteException e) {
            }
        } else {
            TransferPipe tp = new TransferPipe();
            try {
                caller.go(iface, tp.getWriteFd().getFileDescriptor(), prefix, args);
                tp.go(out, timeout);
            } finally {
                tp.kill();
            }
        }
    }

    static void goDump(IBinder binder, FileDescriptor out, String[] args) throws IOException, RemoteException {
        goDump(binder, out, args, DEFAULT_TIMEOUT);
    }

    static void goDump(IBinder binder, FileDescriptor out, String[] args, long timeout) throws IOException, RemoteException {
        if (binder instanceof Binder) {
            try {
                binder.dump(out, args);
            } catch (RemoteException e) {
            }
        } else {
            TransferPipe tp = new TransferPipe();
            try {
                binder.dumpAsync(tp.getWriteFd().getFileDescriptor(), args);
                tp.go(out, timeout);
            } finally {
                tp.kill();
            }
        }
    }

    public void go(FileDescriptor out) throws IOException {
        go(out, DEFAULT_TIMEOUT);
    }

    public void go(FileDescriptor out, long timeout) throws IOException {
        try {
            synchronized (this) {
                this.mOutFd = out;
                this.mEndTime = SystemClock.uptimeMillis() + timeout;
                closeFd(1);
                this.mThread.start();
                while (this.mFailure == null && !this.mComplete) {
                    long waitTime = this.mEndTime - SystemClock.uptimeMillis();
                    if (waitTime <= 0) {
                        this.mThread.interrupt();
                        throw new IOException("Timeout");
                    }
                    try {
                        wait(waitTime);
                    } catch (InterruptedException e) {
                    }
                }
                if (this.mFailure != null) {
                    throw new IOException(this.mFailure);
                }
            }
        } finally {
            kill();
        }
    }

    /* access modifiers changed from: package-private */
    public void closeFd(int num) {
        if (this.mFds[num] != null) {
            try {
                this.mFds[num].close();
            } catch (IOException e) {
            }
            this.mFds[num] = null;
        }
    }

    public void kill() {
        synchronized (this) {
            closeFd(0);
            closeFd(1);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x002b, code lost:
        if (r12.mBufferPrefix == null) goto L_0x0033;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x002d, code lost:
        r1 = r12.mBufferPrefix.getBytes();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:?, code lost:
        r8 = r3.read(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0037, code lost:
        if (r8 <= 0) goto L_0x007f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0039, code lost:
        if (r1 != null) goto L_0x0053;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x003b, code lost:
        r4.write(r0, 0, r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0040, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0041, code lost:
        monitor-enter(r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:?, code lost:
        r12.mFailure = r2.toString();
        notifyAll();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0053, code lost:
        r9 = 0;
        r5 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0055, code lost:
        if (r5 >= r8) goto L_0x0077;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0059, code lost:
        if (r0[r5] == 10) goto L_0x0074;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x005b, code lost:
        if (r5 <= r9) goto L_0x0062;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x005d, code lost:
        r4.write(r0, r9, r5 - r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0062, code lost:
        r9 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0063, code lost:
        if (r6 == false) goto L_0x0069;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0065, code lost:
        r4.write(r1);
        r6 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0069, code lost:
        r5 = r5 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x006b, code lost:
        if (r5 >= r8) goto L_0x0071;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x006f, code lost:
        if (r0[r5] != 10) goto L_0x0069;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x0071, code lost:
        if (r5 >= r8) goto L_0x0074;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x0073, code lost:
        r6 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x0074, code lost:
        r5 = r5 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x0077, code lost:
        if (r8 <= r9) goto L_0x0033;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x0079, code lost:
        r4.write(r0, r9, r8 - r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x0085, code lost:
        if (r12.mThread.isInterrupted() == false) goto L_0x0087;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x0087, code lost:
        monitor-enter(r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:?, code lost:
        r12.mComplete = true;
        notifyAll();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x008e, code lost:
        monitor-exit(r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0027, code lost:
        r1 = null;
        r6 = true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void run() {
        /*
        // Method dump skipped, instructions count: 147
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.TransferPipe.run():void");
    }
}

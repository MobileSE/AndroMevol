package android.os;

import android.os.ICancellationSignal;

public final class CancellationSignal {
    private boolean mCancelInProgress;
    private boolean mIsCanceled;
    private OnCancelListener mOnCancelListener;
    private ICancellationSignal mRemote;

    public interface OnCancelListener {
        void onCancel();
    }

    public boolean isCanceled() {
        boolean z;
        synchronized (this) {
            z = this.mIsCanceled;
        }
        return z;
    }

    public void throwIfCanceled() {
        if (isCanceled()) {
            throw new OperationCanceledException();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0017, code lost:
        if (r1 == null) goto L_0x001c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:?, code lost:
        r1.cancel();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x002b, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x002c, code lost:
        monitor-enter(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:?, code lost:
        r4.mCancelInProgress = false;
        notifyAll();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0034, code lost:
        throw r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:7:0x0012, code lost:
        if (r0 == null) goto L_0x0017;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:?, code lost:
        r0.onCancel();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void cancel() {
        /*
            r4 = this;
            monitor-enter(r4)
            boolean r2 = r4.mIsCanceled     // Catch:{ all -> 0x0028 }
            if (r2 == 0) goto L_0x0007
            monitor-exit(r4)     // Catch:{ all -> 0x0028 }
        L_0x0006:
            return
        L_0x0007:
            r2 = 1
            r4.mIsCanceled = r2     // Catch:{ all -> 0x0028 }
            r2 = 1
            r4.mCancelInProgress = r2     // Catch:{ all -> 0x0028 }
            android.os.CancellationSignal$OnCancelListener r0 = r4.mOnCancelListener     // Catch:{ all -> 0x0028 }
            android.os.ICancellationSignal r1 = r4.mRemote     // Catch:{ all -> 0x0028 }
            monitor-exit(r4)     // Catch:{ all -> 0x0028 }
            if (r0 == 0) goto L_0x0017
            r0.onCancel()     // Catch:{ all -> 0x002b }
        L_0x0017:
            if (r1 == 0) goto L_0x001c
            r1.cancel()     // Catch:{ RemoteException -> 0x0038 }
        L_0x001c:
            monitor-enter(r4)
            r2 = 0
            r4.mCancelInProgress = r2     // Catch:{ all -> 0x0025 }
            r4.notifyAll()     // Catch:{ all -> 0x0025 }
            monitor-exit(r4)     // Catch:{ all -> 0x0025 }
            goto L_0x0006
        L_0x0025:
            r2 = move-exception
            monitor-exit(r4)     // Catch:{ all -> 0x0025 }
            throw r2
        L_0x0028:
            r2 = move-exception
            monitor-exit(r4)
            throw r2
        L_0x002b:
            r2 = move-exception
            monitor-enter(r4)
            r3 = 0
            r4.mCancelInProgress = r3     // Catch:{ all -> 0x0035 }
            r4.notifyAll()     // Catch:{ all -> 0x0035 }
            monitor-exit(r4)     // Catch:{ all -> 0x0035 }
            throw r2
        L_0x0035:
            r2 = move-exception
            monitor-exit(r4)
            throw r2
        L_0x0038:
            r2 = move-exception
            goto L_0x001c
        */
        throw new UnsupportedOperationException("Method not decompiled: android.os.CancellationSignal.cancel():void");
    }

    public void setOnCancelListener(OnCancelListener listener) {
        synchronized (this) {
            waitForCancelFinishedLocked();
            if (this.mOnCancelListener != listener) {
                this.mOnCancelListener = listener;
                if (this.mIsCanceled && listener != null) {
                    listener.onCancel();
                }
            }
        }
    }

    public void setRemote(ICancellationSignal remote) {
        synchronized (this) {
            waitForCancelFinishedLocked();
            if (this.mRemote != remote) {
                this.mRemote = remote;
                if (this.mIsCanceled && remote != null) {
                    try {
                        remote.cancel();
                    } catch (RemoteException e) {
                    }
                }
            }
        }
    }

    private void waitForCancelFinishedLocked() {
        while (this.mCancelInProgress) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
    }

    public static ICancellationSignal createTransport() {
        return new Transport();
    }

    public static CancellationSignal fromTransport(ICancellationSignal transport) {
        if (transport instanceof Transport) {
            return ((Transport) transport).mCancellationSignal;
        }
        return null;
    }

    /* access modifiers changed from: private */
    public static final class Transport extends ICancellationSignal.Stub {
        final CancellationSignal mCancellationSignal;

        private Transport() {
            this.mCancellationSignal = new CancellationSignal();
        }

        @Override // android.os.ICancellationSignal
        public void cancel() throws RemoteException {
            this.mCancellationSignal.cancel();
        }
    }
}

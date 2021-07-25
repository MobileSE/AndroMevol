package android.os;

public class Broadcaster {
    private Registration mReg;

    /* JADX WARNING: Removed duplicated region for block: B:25:0x006e  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void request(int r12, android.os.Handler r13, int r14) {
        /*
        // Method dump skipped, instructions count: 168
        */
        throw new UnsupportedOperationException("Method not decompiled: android.os.Broadcaster.request(int, android.os.Handler, int):void");
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0008 A[LOOP:0: B:5:0x0008->B:27:0x0057, LOOP_START, PHI: r2 
      PHI: (r2v1 'r' android.os.Broadcaster$Registration) = (r2v0 'r' android.os.Broadcaster$Registration), (r2v2 'r' android.os.Broadcaster$Registration) binds: [B:3:0x0004, B:27:0x0057] A[DONT_GENERATE, DONT_INLINE]] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void cancelRequest(int r11, android.os.Handler r12, int r13) {
        /*
            r10 = this;
            monitor-enter(r10)
            android.os.Broadcaster$Registration r4 = r10.mReg     // Catch:{ all -> 0x0052 }
            r2 = r4
            if (r2 != 0) goto L_0x0008
            monitor-exit(r10)     // Catch:{ all -> 0x0052 }
        L_0x0007:
            return
        L_0x0008:
            int r7 = r2.senderWhat     // Catch:{ all -> 0x0052 }
            if (r7 < r11) goto L_0x0055
        L_0x000c:
            int r7 = r2.senderWhat     // Catch:{ all -> 0x0052 }
            if (r7 != r11) goto L_0x0050
            android.os.Handler[] r5 = r2.targets     // Catch:{ all -> 0x0052 }
            int[] r6 = r2.targetWhats     // Catch:{ all -> 0x0052 }
            int r1 = r5.length     // Catch:{ all -> 0x0052 }
            r0 = 0
        L_0x0016:
            if (r0 >= r1) goto L_0x0050
            r7 = r5[r0]     // Catch:{ all -> 0x0052 }
            if (r7 != r12) goto L_0x005a
            r7 = r6[r0]     // Catch:{ all -> 0x0052 }
            if (r7 != r13) goto L_0x005a
            int r7 = r1 + -1
            android.os.Handler[] r7 = new android.os.Handler[r7]     // Catch:{ all -> 0x0052 }
            r2.targets = r7     // Catch:{ all -> 0x0052 }
            int r7 = r1 + -1
            int[] r7 = new int[r7]     // Catch:{ all -> 0x0052 }
            r2.targetWhats = r7     // Catch:{ all -> 0x0052 }
            if (r0 <= 0) goto L_0x003c
            r7 = 0
            android.os.Handler[] r8 = r2.targets     // Catch:{ all -> 0x0052 }
            r9 = 0
            java.lang.System.arraycopy(r5, r7, r8, r9, r0)     // Catch:{ all -> 0x0052 }
            r7 = 0
            int[] r8 = r2.targetWhats     // Catch:{ all -> 0x0052 }
            r9 = 0
            java.lang.System.arraycopy(r6, r7, r8, r9, r0)     // Catch:{ all -> 0x0052 }
        L_0x003c:
            int r7 = r1 - r0
            int r3 = r7 + -1
            if (r3 == 0) goto L_0x0050
            int r7 = r0 + 1
            android.os.Handler[] r8 = r2.targets     // Catch:{ all -> 0x0052 }
            java.lang.System.arraycopy(r5, r7, r8, r0, r3)     // Catch:{ all -> 0x0052 }
            int r7 = r0 + 1
            int[] r8 = r2.targetWhats     // Catch:{ all -> 0x0052 }
            java.lang.System.arraycopy(r6, r7, r8, r0, r3)     // Catch:{ all -> 0x0052 }
        L_0x0050:
            monitor-exit(r10)     // Catch:{ all -> 0x0052 }
            goto L_0x0007
        L_0x0052:
            r7 = move-exception
            monitor-exit(r10)     // Catch:{ all -> 0x0052 }
            throw r7
        L_0x0055:
            android.os.Broadcaster$Registration r2 = r2.next
            if (r2 != r4) goto L_0x0008
            goto L_0x000c
        L_0x005a:
            int r0 = r0 + 1
            goto L_0x0016
        */
        throw new UnsupportedOperationException("Method not decompiled: android.os.Broadcaster.cancelRequest(int, android.os.Handler, int):void");
    }

    public void dumpRegistrations() {
        synchronized (this) {
            Registration start = this.mReg;
            System.out.println("Broadcaster " + this + " {");
            if (start != null) {
                Registration r = start;
                do {
                    System.out.println("    senderWhat=" + r.senderWhat);
                    int n = r.targets.length;
                    for (int i = 0; i < n; i++) {
                        System.out.println("        [" + r.targetWhats[i] + "] " + r.targets[i]);
                    }
                    r = r.next;
                } while (r != start);
            }
            System.out.println("}");
        }
    }

    public void broadcast(Message msg) {
        synchronized (this) {
            if (this.mReg != null) {
                int senderWhat = msg.what;
                Registration start = this.mReg;
                Registration r = start;
                while (true) {
                    if (r.senderWhat >= senderWhat) {
                        break;
                    }
                    r = r.next;
                    if (r == start) {
                        break;
                    }
                }
                if (r.senderWhat == senderWhat) {
                    Handler[] targets = r.targets;
                    int[] whats = r.targetWhats;
                    int n = targets.length;
                    for (int i = 0; i < n; i++) {
                        Handler target = targets[i];
                        Message m = Message.obtain();
                        m.copyFrom(msg);
                        m.what = whats[i];
                        target.sendMessage(m);
                    }
                }
            }
        }
    }

    private class Registration {
        Registration next;
        Registration prev;
        int senderWhat;
        int[] targetWhats;
        Handler[] targets;

        private Registration() {
        }
    }
}

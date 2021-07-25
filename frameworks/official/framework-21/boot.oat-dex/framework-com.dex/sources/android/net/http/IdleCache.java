package android.net.http;

import android.os.Process;
import android.os.SystemClock;
import org.apache.http.HttpHost;

/* access modifiers changed from: package-private */
public class IdleCache {
    private static final int CHECK_INTERVAL = 2000;
    private static final int EMPTY_CHECK_MAX = 5;
    private static final int IDLE_CACHE_MAX = 8;
    private static final int TIMEOUT = 6000;
    private int mCached = 0;
    private int mCount = 0;
    private Entry[] mEntries = new Entry[8];
    private int mReused = 0;
    private IdleReaper mThread = null;

    /* access modifiers changed from: package-private */
    public class Entry {
        Connection mConnection;
        HttpHost mHost;
        long mTimeout;

        Entry() {
        }
    }

    IdleCache() {
        for (int i = 0; i < 8; i++) {
            this.mEntries[i] = new Entry();
        }
    }

    /* access modifiers changed from: package-private */
    public synchronized boolean cacheConnection(HttpHost host, Connection connection) {
        boolean ret;
        ret = false;
        if (this.mCount < 8) {
            long time = SystemClock.uptimeMillis();
            int i = 0;
            while (true) {
                if (i >= 8) {
                    break;
                }
                Entry entry = this.mEntries[i];
                if (entry.mHost == null) {
                    entry.mHost = host;
                    entry.mConnection = connection;
                    entry.mTimeout = 6000 + time;
                    this.mCount++;
                    ret = true;
                    if (this.mThread == null) {
                        this.mThread = new IdleReaper();
                        this.mThread.start();
                    }
                } else {
                    i++;
                }
            }
        }
        return ret;
    }

    /* access modifiers changed from: package-private */
    public synchronized Connection getConnection(HttpHost host) {
        Connection ret;
        ret = null;
        if (this.mCount > 0) {
            int i = 0;
            while (true) {
                if (i < 8) {
                    Entry entry = this.mEntries[i];
                    HttpHost eHost = entry.mHost;
                    if (eHost != null && eHost.equals(host)) {
                        ret = entry.mConnection;
                        entry.mHost = null;
                        entry.mConnection = null;
                        this.mCount--;
                        break;
                    }
                    i++;
                } else {
                    break;
                }
            }
        }
        return ret;
    }

    /* access modifiers changed from: package-private */
    public synchronized void clear() {
        int i = 0;
        while (this.mCount > 0 && i < 8) {
            Entry entry = this.mEntries[i];
            if (entry.mHost != null) {
                entry.mHost = null;
                entry.mConnection.closeConnection();
                entry.mConnection = null;
                this.mCount--;
            }
            i++;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void clearIdle() {
        if (this.mCount > 0) {
            long time = SystemClock.uptimeMillis();
            for (int i = 0; i < 8; i++) {
                Entry entry = this.mEntries[i];
                if (entry.mHost != null && time > entry.mTimeout) {
                    entry.mHost = null;
                    entry.mConnection.closeConnection();
                    entry.mConnection = null;
                    this.mCount--;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public class IdleReaper extends Thread {
        private IdleReaper() {
        }

        public void run() {
            int check = 0;
            setName("IdleReaper");
            Process.setThreadPriority(10);
            synchronized (IdleCache.this) {
                while (check < 5) {
                    try {
                        IdleCache.this.wait(2000);
                    } catch (InterruptedException e) {
                    }
                    if (IdleCache.this.mCount == 0) {
                        check++;
                    } else {
                        check = 0;
                        IdleCache.this.clearIdle();
                    }
                }
                IdleCache.this.mThread = null;
            }
        }
    }
}

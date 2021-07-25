package android.net.http;

import android.os.SystemClock;
import android.util.Log;

/* access modifiers changed from: package-private */
public class HttpLog {
    private static final boolean DEBUG = false;
    private static final String LOGTAG = "http";
    static final boolean LOGV = false;

    HttpLog() {
    }

    static void v(String logMe) {
        Log.v(LOGTAG, SystemClock.uptimeMillis() + " " + Thread.currentThread().getName() + " " + logMe);
    }

    static void e(String logMe) {
        Log.e(LOGTAG, logMe);
    }
}

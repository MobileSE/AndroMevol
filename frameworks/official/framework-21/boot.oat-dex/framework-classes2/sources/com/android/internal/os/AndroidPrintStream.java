package com.android.internal.os;

import android.util.Log;

class AndroidPrintStream extends LoggingPrintStream {
    private final int priority;
    private final String tag;

    public AndroidPrintStream(int priority2, String tag2) {
        if (tag2 == null) {
            throw new NullPointerException("tag");
        }
        this.priority = priority2;
        this.tag = tag2;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.os.LoggingPrintStream
    public void log(String line) {
        Log.println(this.priority, this.tag, line);
    }
}

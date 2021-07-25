package com.android.internal.util;

import android.os.Debug;
import android.os.StrictMode;

public final class MemInfoReader {
    final long[] mInfos = new long[9];

    public void readMemInfo() {
        StrictMode.ThreadPolicy savedPolicy = StrictMode.allowThreadDiskReads();
        try {
            Debug.getMemInfo(this.mInfos);
        } finally {
            StrictMode.setThreadPolicy(savedPolicy);
        }
    }

    public long getTotalSize() {
        return this.mInfos[0] * 1024;
    }

    public long getFreeSize() {
        return this.mInfos[1] * 1024;
    }

    public long getCachedSize() {
        return this.mInfos[3] * 1024;
    }

    public long getTotalSizeKb() {
        return this.mInfos[0];
    }

    public long getFreeSizeKb() {
        return this.mInfos[1];
    }

    public long getCachedSizeKb() {
        return this.mInfos[3];
    }

    public long getBuffersSizeKb() {
        return this.mInfos[2];
    }

    public long getShmemSizeKb() {
        return this.mInfos[4];
    }

    public long getSlabSizeKb() {
        return this.mInfos[5];
    }

    public long getSwapTotalSizeKb() {
        return this.mInfos[6];
    }

    public long getSwapFreeSizeKb() {
        return this.mInfos[7];
    }

    public long getZramTotalSizeKb() {
        return this.mInfos[8];
    }
}

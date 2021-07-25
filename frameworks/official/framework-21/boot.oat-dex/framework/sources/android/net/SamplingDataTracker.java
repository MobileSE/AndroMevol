package android.net;

public class SamplingDataTracker {
    private static final boolean DBG = false;
    private static final String TAG = "SamplingDataTracker";
    private final int MINIMUM_SAMPLED_PACKETS = 30;
    private final int MINIMUM_SAMPLING_INTERVAL = 15000;
    private SamplingSnapshot mBeginningSample;
    private SamplingSnapshot mEndingSample;
    private SamplingSnapshot mLastSample;
    public final Object mSamplingDataLock = new Object();

    public static class SamplingSnapshot {
        public long mRxByteCount;
        public long mRxPacketCount;
        public long mRxPacketErrorCount;
        public long mTimestamp;
        public long mTxByteCount;
        public long mTxPacketCount;
        public long mTxPacketErrorCount;
    }

    /* JADX WARNING: Removed duplicated region for block: B:25:0x009f A[SYNTHETIC, Splitter:B:25:0x009f] */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x00b6 A[SYNTHETIC, Splitter:B:32:0x00b6] */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x00c6 A[SYNTHETIC, Splitter:B:38:0x00c6] */
    /* JADX WARNING: Removed duplicated region for block: B:57:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:59:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void getSamplingSnapshots(java.util.Map<java.lang.String, android.net.SamplingDataTracker.SamplingSnapshot> r10) {
        /*
        // Method dump skipped, instructions count: 220
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.SamplingDataTracker.getSamplingSnapshots(java.util.Map):void");
    }

    public void startSampling(SamplingSnapshot s) {
        synchronized (this.mSamplingDataLock) {
            this.mLastSample = s;
        }
    }

    public void stopSampling(SamplingSnapshot s) {
        synchronized (this.mSamplingDataLock) {
            if (this.mLastSample != null && s.mTimestamp - this.mLastSample.mTimestamp > 15000 && getSampledPacketCount(this.mLastSample, s) > 30) {
                this.mBeginningSample = this.mLastSample;
                this.mEndingSample = s;
                this.mLastSample = null;
            }
        }
    }

    public void resetSamplingData() {
        synchronized (this.mSamplingDataLock) {
            this.mLastSample = null;
        }
    }

    public long getSampledTxByteCount() {
        long j;
        synchronized (this.mSamplingDataLock) {
            if (this.mBeginningSample == null || this.mEndingSample == null) {
                j = Long.MAX_VALUE;
            } else {
                j = this.mEndingSample.mTxByteCount - this.mBeginningSample.mTxByteCount;
            }
        }
        return j;
    }

    public long getSampledTxPacketCount() {
        long j;
        synchronized (this.mSamplingDataLock) {
            if (this.mBeginningSample == null || this.mEndingSample == null) {
                j = Long.MAX_VALUE;
            } else {
                j = this.mEndingSample.mTxPacketCount - this.mBeginningSample.mTxPacketCount;
            }
        }
        return j;
    }

    public long getSampledTxPacketErrorCount() {
        long j;
        synchronized (this.mSamplingDataLock) {
            if (this.mBeginningSample == null || this.mEndingSample == null) {
                j = Long.MAX_VALUE;
            } else {
                j = this.mEndingSample.mTxPacketErrorCount - this.mBeginningSample.mTxPacketErrorCount;
            }
        }
        return j;
    }

    public long getSampledRxByteCount() {
        long j;
        synchronized (this.mSamplingDataLock) {
            if (this.mBeginningSample == null || this.mEndingSample == null) {
                j = Long.MAX_VALUE;
            } else {
                j = this.mEndingSample.mRxByteCount - this.mBeginningSample.mRxByteCount;
            }
        }
        return j;
    }

    public long getSampledRxPacketCount() {
        long j;
        synchronized (this.mSamplingDataLock) {
            if (this.mBeginningSample == null || this.mEndingSample == null) {
                j = Long.MAX_VALUE;
            } else {
                j = this.mEndingSample.mRxPacketCount - this.mBeginningSample.mRxPacketCount;
            }
        }
        return j;
    }

    public long getSampledPacketCount() {
        return getSampledPacketCount(this.mBeginningSample, this.mEndingSample);
    }

    public long getSampledPacketCount(SamplingSnapshot begin, SamplingSnapshot end) {
        if (begin == null || end == null) {
            return Long.MAX_VALUE;
        }
        return (end.mRxPacketCount - begin.mRxPacketCount) + (end.mTxPacketCount - begin.mTxPacketCount);
    }

    public long getSampledPacketErrorCount() {
        if (this.mBeginningSample == null || this.mEndingSample == null) {
            return Long.MAX_VALUE;
        }
        return getSampledRxPacketErrorCount() + getSampledTxPacketErrorCount();
    }

    public long getSampledRxPacketErrorCount() {
        long j;
        synchronized (this.mSamplingDataLock) {
            if (this.mBeginningSample == null || this.mEndingSample == null) {
                j = Long.MAX_VALUE;
            } else {
                j = this.mEndingSample.mRxPacketErrorCount - this.mBeginningSample.mRxPacketErrorCount;
            }
        }
        return j;
    }

    public long getSampleTimestamp() {
        long j;
        synchronized (this.mSamplingDataLock) {
            if (this.mEndingSample != null) {
                j = this.mEndingSample.mTimestamp;
            } else {
                j = Long.MAX_VALUE;
            }
        }
        return j;
    }

    public int getSampleDuration() {
        int i;
        synchronized (this.mSamplingDataLock) {
            if (this.mBeginningSample == null || this.mEndingSample == null) {
                i = Integer.MAX_VALUE;
            } else {
                i = (int) (this.mEndingSample.mTimestamp - this.mBeginningSample.mTimestamp);
            }
        }
        return i;
    }

    public void setCommonLinkQualityInfoFields(LinkQualityInfo li) {
        synchronized (this.mSamplingDataLock) {
            li.setLastDataSampleTime(getSampleTimestamp());
            li.setDataSampleDuration(getSampleDuration());
            li.setPacketCount(getSampledPacketCount());
            li.setPacketErrorCount(getSampledPacketErrorCount());
        }
    }
}

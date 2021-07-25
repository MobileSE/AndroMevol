package android.media;

import android.media.MediaCodec;
import dalvik.system.CloseGuard;
import java.io.FileDescriptor;
import java.nio.ByteBuffer;
import java.util.Map;

public final class MediaMuxer {
    private static final int MUXER_STATE_INITIALIZED = 0;
    private static final int MUXER_STATE_STARTED = 1;
    private static final int MUXER_STATE_STOPPED = 2;
    private static final int MUXER_STATE_UNINITIALIZED = -1;
    private final CloseGuard mCloseGuard = CloseGuard.get();
    private int mLastTrackIndex = -1;
    private long mNativeObject;
    private int mState = -1;

    private static native int nativeAddTrack(long j, String[] strArr, Object[] objArr);

    private static native void nativeRelease(long j);

    private static native void nativeSetLocation(long j, int i, int i2);

    private static native void nativeSetOrientationHint(long j, int i);

    private static native long nativeSetup(FileDescriptor fileDescriptor, int i);

    private static native void nativeStart(long j);

    private static native void nativeStop(long j);

    private static native void nativeWriteSampleData(long j, int i, ByteBuffer byteBuffer, int i2, int i3, long j2, int i4);

    static {
        System.loadLibrary("media_jni");
    }

    public static final class OutputFormat {
        public static final int MUXER_OUTPUT_MPEG_4 = 0;
        public static final int MUXER_OUTPUT_WEBM = 1;

        private OutputFormat() {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:18:0x004d  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public MediaMuxer(java.lang.String r7, int r8) throws java.io.IOException {
        /*
            r6 = this;
            r4 = -1
            r6.<init>()
            r6.mState = r4
            dalvik.system.CloseGuard r3 = dalvik.system.CloseGuard.get()
            r6.mCloseGuard = r3
            r6.mLastTrackIndex = r4
            if (r7 != 0) goto L_0x0019
            java.lang.IllegalArgumentException r3 = new java.lang.IllegalArgumentException
            java.lang.String r4 = "path must not be null"
            r3.<init>(r4)
            throw r3
        L_0x0019:
            if (r8 == 0) goto L_0x0026
            r3 = 1
            if (r8 == r3) goto L_0x0026
            java.lang.IllegalArgumentException r3 = new java.lang.IllegalArgumentException
            java.lang.String r4 = "format is invalid"
            r3.<init>(r4)
            throw r3
        L_0x0026:
            r1 = 0
            java.io.RandomAccessFile r2 = new java.io.RandomAccessFile     // Catch:{ all -> 0x004a }
            java.lang.String r3 = "rws"
            r2.<init>(r7, r3)     // Catch:{ all -> 0x004a }
            java.io.FileDescriptor r0 = r2.getFD()     // Catch:{ all -> 0x0051 }
            long r4 = nativeSetup(r0, r8)     // Catch:{ all -> 0x0051 }
            r6.mNativeObject = r4     // Catch:{ all -> 0x0051 }
            r3 = 0
            r6.mState = r3     // Catch:{ all -> 0x0051 }
            dalvik.system.CloseGuard r3 = r6.mCloseGuard     // Catch:{ all -> 0x0051 }
            java.lang.String r4 = "release"
            r3.open(r4)     // Catch:{ all -> 0x0051 }
            if (r2 == 0) goto L_0x0049
            r2.close()
        L_0x0049:
            return
        L_0x004a:
            r3 = move-exception
        L_0x004b:
            if (r1 == 0) goto L_0x0050
            r1.close()
        L_0x0050:
            throw r3
        L_0x0051:
            r3 = move-exception
            r1 = r2
            goto L_0x004b
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.MediaMuxer.<init>(java.lang.String, int):void");
    }

    public void setOrientationHint(int degrees) {
        if (degrees != 0 && degrees != 90 && degrees != 180 && degrees != 270) {
            throw new IllegalArgumentException("Unsupported angle: " + degrees);
        } else if (this.mState == 0) {
            nativeSetOrientationHint(this.mNativeObject, degrees);
        } else {
            throw new IllegalStateException("Can't set rotation degrees due to wrong state.");
        }
    }

    public void setLocation(float latitude, float longitude) {
        int latitudex10000 = (int) (((double) (latitude * 10000.0f)) + 0.5d);
        int longitudex10000 = (int) (((double) (longitude * 10000.0f)) + 0.5d);
        if (latitudex10000 > 900000 || latitudex10000 < -900000) {
            throw new IllegalArgumentException("Latitude: " + latitude + " out of range.");
        } else if (longitudex10000 > 1800000 || longitudex10000 < -1800000) {
            throw new IllegalArgumentException("Longitude: " + longitude + " out of range");
        } else if (this.mState != 0 || this.mNativeObject == 0) {
            throw new IllegalStateException("Can't set location due to wrong state.");
        } else {
            nativeSetLocation(this.mNativeObject, latitudex10000, longitudex10000);
        }
    }

    public void start() {
        if (this.mNativeObject == 0) {
            throw new IllegalStateException("Muxer has been released!");
        } else if (this.mState == 0) {
            nativeStart(this.mNativeObject);
            this.mState = 1;
        } else {
            throw new IllegalStateException("Can't start due to wrong state.");
        }
    }

    public void stop() {
        if (this.mState == 1) {
            nativeStop(this.mNativeObject);
            this.mState = 2;
            return;
        }
        throw new IllegalStateException("Can't stop due to wrong state.");
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            if (this.mCloseGuard != null) {
                this.mCloseGuard.warnIfOpen();
            }
            if (this.mNativeObject != 0) {
                nativeRelease(this.mNativeObject);
                this.mNativeObject = 0;
            }
        } finally {
            super.finalize();
        }
    }

    public int addTrack(MediaFormat format) {
        if (format == null) {
            throw new IllegalArgumentException("format must not be null.");
        } else if (this.mState != 0) {
            throw new IllegalStateException("Muxer is not initialized.");
        } else if (this.mNativeObject == 0) {
            throw new IllegalStateException("Muxer has been released!");
        } else {
            Map<String, Object> formatMap = format.getMap();
            int mapSize = formatMap.size();
            if (mapSize > 0) {
                String[] keys = new String[mapSize];
                Object[] values = new Object[mapSize];
                int i = 0;
                for (Map.Entry<String, Object> entry : formatMap.entrySet()) {
                    keys[i] = entry.getKey();
                    values[i] = entry.getValue();
                    i++;
                }
                int trackIndex = nativeAddTrack(this.mNativeObject, keys, values);
                if (this.mLastTrackIndex >= trackIndex) {
                    throw new IllegalArgumentException("Invalid format.");
                }
                this.mLastTrackIndex = trackIndex;
                return trackIndex;
            }
            throw new IllegalArgumentException("format must not be empty.");
        }
    }

    public void writeSampleData(int trackIndex, ByteBuffer byteBuf, MediaCodec.BufferInfo bufferInfo) {
        if (trackIndex < 0 || trackIndex > this.mLastTrackIndex) {
            throw new IllegalArgumentException("trackIndex is invalid");
        } else if (byteBuf == null) {
            throw new IllegalArgumentException("byteBuffer must not be null");
        } else if (bufferInfo == null) {
            throw new IllegalArgumentException("bufferInfo must not be null");
        } else if (bufferInfo.size < 0 || bufferInfo.offset < 0 || bufferInfo.offset + bufferInfo.size > byteBuf.capacity() || bufferInfo.presentationTimeUs < 0) {
            throw new IllegalArgumentException("bufferInfo must specify a valid buffer offset, size and presentation time");
        } else if (this.mNativeObject == 0) {
            throw new IllegalStateException("Muxer has been released!");
        } else if (this.mState != 1) {
            throw new IllegalStateException("Can't write, muxer is not started");
        } else {
            nativeWriteSampleData(this.mNativeObject, trackIndex, byteBuf, bufferInfo.offset, bufferInfo.size, bufferInfo.presentationTimeUs, bufferInfo.flags);
        }
    }

    public void release() {
        if (this.mState == 1) {
            stop();
        }
        if (this.mNativeObject != 0) {
            nativeRelease(this.mNativeObject);
            this.mNativeObject = 0;
            this.mCloseGuard.close();
        }
        this.mState = -1;
    }
}

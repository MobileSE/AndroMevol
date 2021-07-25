package android.speech.srec;

import android.os.BatteryStats;
import java.io.IOException;
import java.io.InputStream;

public final class MicrophoneInputStream extends InputStream {
    private static final String TAG = "MicrophoneInputStream";
    private long mAudioRecord = 0;
    private byte[] mOneByte = new byte[1];

    private static native void AudioRecordDelete(long j) throws IOException;

    private static native long AudioRecordNew(int i, int i2);

    private static native int AudioRecordRead(long j, byte[] bArr, int i, int i2) throws IOException;

    private static native int AudioRecordStart(long j);

    private static native void AudioRecordStop(long j) throws IOException;

    static {
        System.loadLibrary("srec_jni");
    }

    public MicrophoneInputStream(int sampleRate, int fifoDepth) throws IOException {
        this.mAudioRecord = AudioRecordNew(sampleRate, fifoDepth);
        if (this.mAudioRecord == 0) {
            throw new IOException("AudioRecord constructor failed - busy?");
        }
        int status = AudioRecordStart(this.mAudioRecord);
        if (status != 0) {
            close();
            throw new IOException("AudioRecord start failed: " + status);
        }
    }

    @Override // java.io.InputStream
    public int read() throws IOException {
        if (this.mAudioRecord == 0) {
            throw new IllegalStateException("not open");
        } else if (AudioRecordRead(this.mAudioRecord, this.mOneByte, 0, 1) == 1) {
            return this.mOneByte[0] & BatteryStats.HistoryItem.CMD_NULL;
        } else {
            return -1;
        }
    }

    @Override // java.io.InputStream
    public int read(byte[] b) throws IOException {
        if (this.mAudioRecord != 0) {
            return AudioRecordRead(this.mAudioRecord, b, 0, b.length);
        }
        throw new IllegalStateException("not open");
    }

    @Override // java.io.InputStream
    public int read(byte[] b, int offset, int length) throws IOException {
        if (this.mAudioRecord != 0) {
            return AudioRecordRead(this.mAudioRecord, b, offset, length);
        }
        throw new IllegalStateException("not open");
    }

    @Override // java.io.Closeable, java.lang.AutoCloseable, java.io.InputStream
    public void close() throws IOException {
        if (this.mAudioRecord != 0) {
            try {
                AudioRecordStop(this.mAudioRecord);
                try {
                    AudioRecordDelete(this.mAudioRecord);
                } finally {
                    this.mAudioRecord = 0;
                }
            } catch (Throwable th) {
                AudioRecordDelete(this.mAudioRecord);
                throw th;
            } finally {
                this.mAudioRecord = 0;
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public void finalize() throws Throwable {
        if (this.mAudioRecord != 0) {
            close();
            throw new IOException("someone forgot to close MicrophoneInputStream");
        }
    }
}

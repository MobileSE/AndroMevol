package android.speech.tts;

import android.media.AudioFormat;
import android.speech.tts.TextToSpeechService;
import android.util.Log;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

class FileSynthesisCallback extends AbstractSynthesisCallback {
    private static final boolean DBG = false;
    private static final int MAX_AUDIO_BUFFER_SIZE = 8192;
    private static final String TAG = "FileSynthesisRequest";
    private static final short WAV_FORMAT_PCM = 1;
    private static final int WAV_HEADER_LENGTH = 44;
    private int mAudioFormat;
    private final Object mCallerIdentity;
    private int mChannelCount;
    private final TextToSpeechService.UtteranceProgressDispatcher mDispatcher;
    private boolean mDone = false;
    private FileChannel mFileChannel;
    private int mSampleRateInHz;
    private boolean mStarted = false;
    private final Object mStateLock = new Object();
    protected int mStatusCode;

    FileSynthesisCallback(FileChannel fileChannel, TextToSpeechService.UtteranceProgressDispatcher dispatcher, Object callerIdentity, boolean clientIsUsingV2) {
        super(clientIsUsingV2);
        this.mFileChannel = fileChannel;
        this.mDispatcher = dispatcher;
        this.mCallerIdentity = callerIdentity;
        this.mStatusCode = 0;
    }

    /* access modifiers changed from: package-private */
    @Override // android.speech.tts.AbstractSynthesisCallback
    public void stop() {
        synchronized (this.mStateLock) {
            if (!this.mDone) {
                if (this.mStatusCode != -2) {
                    this.mStatusCode = -2;
                    cleanUp();
                    if (this.mDispatcher != null) {
                        this.mDispatcher.dispatchOnStop();
                    }
                }
            }
        }
    }

    private void cleanUp() {
        closeFile();
    }

    private void closeFile() {
        this.mFileChannel = null;
    }

    @Override // android.speech.tts.SynthesisCallback
    public int getMaxBufferSize() {
        return 8192;
    }

    @Override // android.speech.tts.SynthesisCallback
    public int start(int sampleRateInHz, int audioFormat, int channelCount) {
        synchronized (this.mStateLock) {
            if (this.mStatusCode == -2) {
                return errorCodeOnStop();
            } else if (this.mStatusCode != 0) {
                return -1;
            } else {
                if (this.mStarted) {
                    Log.e(TAG, "Start called twice");
                    return -1;
                }
                this.mStarted = true;
                this.mSampleRateInHz = sampleRateInHz;
                this.mAudioFormat = audioFormat;
                this.mChannelCount = channelCount;
                if (this.mDispatcher != null) {
                    this.mDispatcher.dispatchOnStart();
                }
                FileChannel fileChannel = this.mFileChannel;
                try {
                    fileChannel.write(ByteBuffer.allocate(44));
                    return 0;
                } catch (IOException ex) {
                    Log.e(TAG, "Failed to write wav header to output file descriptor", ex);
                    synchronized (this.mStateLock) {
                        cleanUp();
                        this.mStatusCode = -5;
                        return -1;
                    }
                }
            }
        }
    }

    @Override // android.speech.tts.SynthesisCallback
    public int audioAvailable(byte[] buffer, int offset, int length) {
        synchronized (this.mStateLock) {
            if (this.mStatusCode == -2) {
                return errorCodeOnStop();
            } else if (this.mStatusCode != 0) {
                return -1;
            } else {
                if (this.mFileChannel == null) {
                    Log.e(TAG, "File not open");
                    this.mStatusCode = -5;
                    return -1;
                } else if (!this.mStarted) {
                    Log.e(TAG, "Start method was not called");
                    return -1;
                } else {
                    FileChannel fileChannel = this.mFileChannel;
                    try {
                        fileChannel.write(ByteBuffer.wrap(buffer, offset, length));
                        return 0;
                    } catch (IOException ex) {
                        Log.e(TAG, "Failed to write to output file descriptor", ex);
                        synchronized (this.mStateLock) {
                            cleanUp();
                            this.mStatusCode = -5;
                            return -1;
                        }
                    }
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:31:?, code lost:
        r4.position(0L);
        r4.write(makeWavHeader(r5, r0, r1, (int) (r4.size() - 44)));
        r8 = r12.mStateLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0067, code lost:
        monitor-enter(r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:?, code lost:
        closeFile();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x006d, code lost:
        if (r12.mDispatcher == null) goto L_0x0074;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x006f, code lost:
        r12.mDispatcher.dispatchOnSuccess();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0075, code lost:
        monitor-exit(r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x007b, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x007c, code lost:
        android.util.Log.e(android.speech.tts.FileSynthesisCallback.TAG, "Failed to write to output file descriptor", r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x0085, code lost:
        monitor-enter(r12.mStateLock);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:?, code lost:
        cleanUp();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:?, code lost:
        return 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:?, code lost:
        return -1;
     */
    @Override // android.speech.tts.SynthesisCallback
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int done() {
        /*
        // Method dump skipped, instructions count: 142
        */
        throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.FileSynthesisCallback.done():int");
    }

    @Override // android.speech.tts.SynthesisCallback
    public void error() {
        error(-3);
    }

    @Override // android.speech.tts.SynthesisCallback
    public void error(int errorCode) {
        synchronized (this.mStateLock) {
            if (!this.mDone) {
                cleanUp();
                this.mStatusCode = errorCode;
            }
        }
    }

    @Override // android.speech.tts.SynthesisCallback
    public boolean hasStarted() {
        boolean z;
        synchronized (this.mStateLock) {
            z = this.mStarted;
        }
        return z;
    }

    @Override // android.speech.tts.SynthesisCallback
    public boolean hasFinished() {
        boolean z;
        synchronized (this.mStateLock) {
            z = this.mDone;
        }
        return z;
    }

    private ByteBuffer makeWavHeader(int sampleRateInHz, int audioFormat, int channelCount, int dataLength) {
        int sampleSizeInBytes = AudioFormat.getBytesPerSample(audioFormat);
        ByteBuffer header = ByteBuffer.wrap(new byte[44]);
        header.order(ByteOrder.LITTLE_ENDIAN);
        header.put(new byte[]{82, 73, 70, 70});
        header.putInt((dataLength + 44) - 8);
        header.put(new byte[]{87, 65, 86, 69});
        header.put(new byte[]{102, 109, 116, 32});
        header.putInt(16);
        header.putShort(1);
        header.putShort((short) channelCount);
        header.putInt(sampleRateInHz);
        header.putInt(sampleRateInHz * sampleSizeInBytes * channelCount);
        header.putShort((short) (sampleSizeInBytes * channelCount));
        header.putShort((short) (sampleSizeInBytes * 8));
        header.put(new byte[]{100, 97, 116, 97});
        header.putInt(dataLength);
        header.flip();
        return header;
    }
}

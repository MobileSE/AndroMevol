package android.speech.tts;

import android.speech.tts.TextToSpeechService;
import android.util.Log;

/* access modifiers changed from: package-private */
public class PlaybackSynthesisCallback extends AbstractSynthesisCallback {
    private static final boolean DBG = false;
    private static final int MIN_AUDIO_BUFFER_SIZE = 8192;
    private static final String TAG = "PlaybackSynthesisRequest";
    private final TextToSpeechService.AudioOutputParams mAudioParams;
    private final AudioPlaybackHandler mAudioTrackHandler;
    private final Object mCallerIdentity;
    private final TextToSpeechService.UtteranceProgressDispatcher mDispatcher;
    private volatile boolean mDone = false;
    private SynthesisPlaybackQueueItem mItem = null;
    private final AbstractEventLogger mLogger;
    private final Object mStateLock = new Object();
    protected int mStatusCode;

    PlaybackSynthesisCallback(TextToSpeechService.AudioOutputParams audioParams, AudioPlaybackHandler audioTrackHandler, TextToSpeechService.UtteranceProgressDispatcher dispatcher, Object callerIdentity, AbstractEventLogger logger, boolean clientIsUsingV2) {
        super(clientIsUsingV2);
        this.mAudioParams = audioParams;
        this.mAudioTrackHandler = audioTrackHandler;
        this.mDispatcher = dispatcher;
        this.mCallerIdentity = callerIdentity;
        this.mLogger = logger;
        this.mStatusCode = 0;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0021, code lost:
        if (r0 == null) goto L_0x0027;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0023, code lost:
        r0.stop(-2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0027, code lost:
        r4.mLogger.onCompleted(-2);
        r4.mDispatcher.dispatchOnStop();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        return;
     */
    @Override // android.speech.tts.AbstractSynthesisCallback
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void stop() {
        /*
            r4 = this;
            r3 = -2
            java.lang.Object r2 = r4.mStateLock
            monitor-enter(r2)
            boolean r1 = r4.mDone     // Catch:{ all -> 0x0018 }
            if (r1 == 0) goto L_0x000a
            monitor-exit(r2)     // Catch:{ all -> 0x0018 }
        L_0x0009:
            return
        L_0x000a:
            int r1 = r4.mStatusCode     // Catch:{ all -> 0x0018 }
            if (r1 != r3) goto L_0x001b
            java.lang.String r1 = "PlaybackSynthesisRequest"
            java.lang.String r3 = "stop() called twice"
            android.util.Log.w(r1, r3)     // Catch:{ all -> 0x0018 }
            monitor-exit(r2)     // Catch:{ all -> 0x0018 }
            goto L_0x0009
        L_0x0018:
            r1 = move-exception
            monitor-exit(r2)     // Catch:{ all -> 0x0018 }
            throw r1
        L_0x001b:
            android.speech.tts.SynthesisPlaybackQueueItem r0 = r4.mItem
            r1 = -2
            r4.mStatusCode = r1
            monitor-exit(r2)
            if (r0 == 0) goto L_0x0027
            r0.stop(r3)
            goto L_0x0009
        L_0x0027:
            android.speech.tts.AbstractEventLogger r1 = r4.mLogger
            r1.onCompleted(r3)
            android.speech.tts.TextToSpeechService$UtteranceProgressDispatcher r1 = r4.mDispatcher
            r1.dispatchOnStop()
            goto L_0x0009
        */
        throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.PlaybackSynthesisCallback.stop():void");
    }

    @Override // android.speech.tts.SynthesisCallback
    public int getMaxBufferSize() {
        return 8192;
    }

    @Override // android.speech.tts.SynthesisCallback
    public boolean hasStarted() {
        boolean z;
        synchronized (this.mStateLock) {
            z = this.mItem != null;
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

    @Override // android.speech.tts.SynthesisCallback
    public int start(int sampleRateInHz, int audioFormat, int channelCount) {
        int channelConfig = BlockingAudioTrack.getChannelConfig(channelCount);
        synchronized (this.mStateLock) {
            if (channelConfig == 0) {
                Log.e(TAG, "Unsupported number of channels :" + channelCount);
                this.mStatusCode = -5;
                return -1;
            } else if (this.mStatusCode == -2) {
                return errorCodeOnStop();
            } else if (this.mStatusCode != 0) {
                return -1;
            } else {
                if (this.mItem != null) {
                    Log.e(TAG, "Start called twice");
                    return -1;
                }
                SynthesisPlaybackQueueItem item = new SynthesisPlaybackQueueItem(this.mAudioParams, sampleRateInHz, audioFormat, channelCount, this.mDispatcher, this.mCallerIdentity, this.mLogger);
                this.mAudioTrackHandler.enqueue(item);
                this.mItem = item;
                return 0;
            }
        }
    }

    @Override // android.speech.tts.SynthesisCallback
    public int audioAvailable(byte[] buffer, int offset, int length) {
        if (length > getMaxBufferSize() || length <= 0) {
            throw new IllegalArgumentException("buffer is too large or of zero length (" + length + " bytes)");
        }
        synchronized (this.mStateLock) {
            if (this.mItem == null) {
                this.mStatusCode = -5;
                return -1;
            } else if (this.mStatusCode != 0) {
                return -1;
            } else {
                if (this.mStatusCode == -2) {
                    return errorCodeOnStop();
                }
                SynthesisPlaybackQueueItem item = this.mItem;
                byte[] bufferCopy = new byte[length];
                System.arraycopy(buffer, offset, bufferCopy, 0, length);
                try {
                    item.put(bufferCopy);
                    this.mLogger.onEngineDataReceived();
                    return 0;
                } catch (InterruptedException e) {
                    synchronized (this.mStateLock) {
                        this.mStatusCode = -5;
                        return -1;
                    }
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:25:0x004c, code lost:
        if (r1 != 0) goto L_0x0058;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x004e, code lost:
        r0.done();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0051, code lost:
        r6.mLogger.onEngineComplete();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0058, code lost:
        r0.stop(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:?, code lost:
        return 0;
     */
    @Override // android.speech.tts.SynthesisCallback
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int done() {
        /*
            r6 = this;
            r2 = -1
            r1 = 0
            r0 = 0
            java.lang.Object r3 = r6.mStateLock
            monitor-enter(r3)
            boolean r4 = r6.mDone     // Catch:{ all -> 0x001e }
            if (r4 == 0) goto L_0x0013
            java.lang.String r4 = "PlaybackSynthesisRequest"
            java.lang.String r5 = "Duplicate call to done()"
            android.util.Log.w(r4, r5)     // Catch:{ all -> 0x001e }
            monitor-exit(r3)     // Catch:{ all -> 0x001e }
        L_0x0012:
            return r2
        L_0x0013:
            int r4 = r6.mStatusCode     // Catch:{ all -> 0x001e }
            r5 = -2
            if (r4 != r5) goto L_0x0021
            int r2 = r6.errorCodeOnStop()     // Catch:{ all -> 0x001e }
            monitor-exit(r3)     // Catch:{ all -> 0x001e }
            goto L_0x0012
        L_0x001e:
            r2 = move-exception
            monitor-exit(r3)     // Catch:{ all -> 0x001e }
            throw r2
        L_0x0021:
            r4 = 1
            r6.mDone = r4
            android.speech.tts.SynthesisPlaybackQueueItem r4 = r6.mItem
            if (r4 != 0) goto L_0x0047
            java.lang.String r4 = "PlaybackSynthesisRequest"
            java.lang.String r5 = "done() was called before start() call"
            android.util.Log.w(r4, r5)
            int r4 = r6.mStatusCode
            if (r4 != 0) goto L_0x003f
            android.speech.tts.TextToSpeechService$UtteranceProgressDispatcher r4 = r6.mDispatcher
            r4.dispatchOnSuccess()
        L_0x0038:
            android.speech.tts.AbstractEventLogger r4 = r6.mLogger
            r4.onEngineComplete()
            monitor-exit(r3)
            goto L_0x0012
        L_0x003f:
            android.speech.tts.TextToSpeechService$UtteranceProgressDispatcher r4 = r6.mDispatcher
            int r5 = r6.mStatusCode
            r4.dispatchOnError(r5)
            goto L_0x0038
        L_0x0047:
            android.speech.tts.SynthesisPlaybackQueueItem r0 = r6.mItem
            int r1 = r6.mStatusCode
            monitor-exit(r3)
            if (r1 != 0) goto L_0x0058
            r0.done()
        L_0x0051:
            android.speech.tts.AbstractEventLogger r2 = r6.mLogger
            r2.onEngineComplete()
            r2 = 0
            goto L_0x0012
        L_0x0058:
            r0.stop(r1)
            goto L_0x0051
        */
        throw new UnsupportedOperationException("Method not decompiled: android.speech.tts.PlaybackSynthesisCallback.done():int");
    }

    @Override // android.speech.tts.SynthesisCallback
    public void error() {
        error(-3);
    }

    @Override // android.speech.tts.SynthesisCallback
    public void error(int errorCode) {
        synchronized (this.mStateLock) {
            if (!this.mDone) {
                this.mStatusCode = errorCode;
            }
        }
    }
}

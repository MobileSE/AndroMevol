package android.speech.tts;

import android.speech.tts.TextToSpeech;

public abstract class UtteranceProgressListener {
    public abstract void onDone(String str);

    @Deprecated
    public abstract void onError(String str);

    public abstract void onStart(String str);

    public void onError(String utteranceId, int errorCode) {
        onError(utteranceId);
    }

    static UtteranceProgressListener from(final TextToSpeech.OnUtteranceCompletedListener listener) {
        return new UtteranceProgressListener() {
            /* class android.speech.tts.UtteranceProgressListener.AnonymousClass1 */

            @Override // android.speech.tts.UtteranceProgressListener
            public synchronized void onDone(String utteranceId) {
                listener.onUtteranceCompleted(utteranceId);
            }

            @Override // android.speech.tts.UtteranceProgressListener
            public void onError(String utteranceId) {
                listener.onUtteranceCompleted(utteranceId);
            }

            @Override // android.speech.tts.UtteranceProgressListener
            public void onStart(String utteranceId) {
            }
        };
    }
}

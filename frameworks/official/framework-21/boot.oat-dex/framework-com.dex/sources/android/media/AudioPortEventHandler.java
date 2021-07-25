package android.media;

import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

/* access modifiers changed from: package-private */
public class AudioPortEventHandler {
    private static final int AUDIOPORT_EVENT_NEW_LISTENER = 4;
    private static final int AUDIOPORT_EVENT_PATCH_LIST_UPDATED = 2;
    private static final int AUDIOPORT_EVENT_PORT_LIST_UPDATED = 1;
    private static final int AUDIOPORT_EVENT_SERVICE_DIED = 3;
    private static String TAG = "AudioPortEventHandler";
    private AudioManager mAudioManager;
    private final Handler mHandler;
    private ArrayList<AudioManager.OnAudioPortUpdateListener> mListeners = new ArrayList<>();

    private native void native_finalize();

    private native void native_setup(Object obj);

    AudioPortEventHandler(AudioManager audioManager) {
        this.mAudioManager = audioManager;
        Looper looper = Looper.getMainLooper();
        if (looper != null) {
            this.mHandler = new Handler(looper) {
                /* class android.media.AudioPortEventHandler.AnonymousClass1 */

                /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
                @Override // android.os.Handler
                public void handleMessage(Message msg) {
                    ArrayList<AudioManager.OnAudioPortUpdateListener> listeners;
                    synchronized (this) {
                        if (msg.what == 4) {
                            listeners = new ArrayList<>();
                            if (AudioPortEventHandler.this.mListeners.contains(msg.obj)) {
                                listeners.add((AudioManager.OnAudioPortUpdateListener) msg.obj);
                            }
                        } else {
                            listeners = AudioPortEventHandler.this.mListeners;
                        }
                    }
                    if (!listeners.isEmpty()) {
                        if (msg.what == 1 || msg.what == 2 || msg.what == 3) {
                            AudioPortEventHandler.this.mAudioManager.resetAudioPortGeneration();
                        }
                        ArrayList<AudioPort> ports = new ArrayList<>();
                        ArrayList<AudioPatch> patches = new ArrayList<>();
                        if (msg.what == 3 || AudioPortEventHandler.this.mAudioManager.updateAudioPortCache(ports, patches) == 0) {
                            switch (msg.what) {
                                case 1:
                                case 4:
                                    AudioPort[] portList = (AudioPort[]) ports.toArray(new AudioPort[0]);
                                    for (int i = 0; i < listeners.size(); i++) {
                                        listeners.get(i).onAudioPortListUpdate(portList);
                                    }
                                    if (msg.what == 1) {
                                        return;
                                    }
                                    break;
                                case 2:
                                    break;
                                case 3:
                                    for (int i2 = 0; i2 < listeners.size(); i2++) {
                                        listeners.get(i2).onServiceDied();
                                    }
                                    return;
                                default:
                                    return;
                            }
                            AudioPatch[] patchList = (AudioPatch[]) patches.toArray(new AudioPatch[0]);
                            for (int i3 = 0; i3 < listeners.size(); i3++) {
                                listeners.get(i3).onAudioPatchListUpdate(patchList);
                            }
                        }
                    }
                }
            };
        } else {
            this.mHandler = null;
        }
        native_setup(new WeakReference(this));
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        native_finalize();
    }

    /* access modifiers changed from: package-private */
    public void registerListener(AudioManager.OnAudioPortUpdateListener l) {
        synchronized (this) {
            this.mListeners.add(l);
        }
        if (this.mHandler != null) {
            this.mHandler.sendMessage(this.mHandler.obtainMessage(4, 0, 0, l));
        }
    }

    /* access modifiers changed from: package-private */
    public void unregisterListener(AudioManager.OnAudioPortUpdateListener l) {
        synchronized (this) {
            this.mListeners.remove(l);
        }
    }

    /* access modifiers changed from: package-private */
    public Handler handler() {
        return this.mHandler;
    }

    private static void postEventFromNative(Object module_ref, int what, int arg1, int arg2, Object obj) {
        Handler handler;
        AudioPortEventHandler eventHandler = (AudioPortEventHandler) ((WeakReference) module_ref).get();
        if (eventHandler != null && eventHandler != null && (handler = eventHandler.handler()) != null) {
            handler.sendMessage(handler.obtainMessage(what, arg1, arg2, obj));
        }
    }
}

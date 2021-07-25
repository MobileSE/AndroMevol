package android.preference;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.preference.VolumePreference;
import android.provider.Settings;
import android.util.Log;
import android.widget.SeekBar;

public class SeekBarVolumizer implements SeekBar.OnSeekBarChangeListener, Handler.Callback {
    private static final int CHECK_RINGTONE_PLAYBACK_DELAY_MS = 1000;
    private static final int MSG_INIT_SAMPLE = 3;
    private static final int MSG_SET_STREAM_VOLUME = 0;
    private static final int MSG_START_SAMPLE = 1;
    private static final int MSG_STOP_SAMPLE = 2;
    private static final String TAG = "SeekBarVolumizer";
    private final AudioManager mAudioManager;
    private final Callback mCallback;
    private final Context mContext;
    private final Uri mDefaultUri;
    private final Handler mHandler;
    private int mLastProgress = -1;
    private final int mMaxStreamVolume;
    private int mOriginalStreamVolume;
    private final Receiver mReceiver = new Receiver();
    private Ringtone mRingtone;
    private SeekBar mSeekBar;
    private final int mStreamType;
    private final H mUiHandler = new H();
    private int mVolumeBeforeMute = -1;
    private final Observer mVolumeObserver;

    public interface Callback {
        void onSampleStarting(SeekBarVolumizer seekBarVolumizer);
    }

    public SeekBarVolumizer(Context context, int streamType, Uri defaultUri, Callback callback) {
        this.mContext = context;
        this.mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        this.mStreamType = streamType;
        this.mMaxStreamVolume = this.mAudioManager.getStreamMaxVolume(this.mStreamType);
        HandlerThread thread = new HandlerThread("SeekBarVolumizer.CallbackHandler");
        thread.start();
        this.mHandler = new Handler(thread.getLooper(), this);
        this.mCallback = callback;
        this.mOriginalStreamVolume = this.mAudioManager.getStreamVolume(this.mStreamType);
        this.mVolumeObserver = new Observer(this.mHandler);
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(Settings.System.VOLUME_SETTINGS[this.mStreamType]), false, this.mVolumeObserver);
        this.mReceiver.setListening(true);
        if (defaultUri == null) {
            if (this.mStreamType == 2) {
                defaultUri = Settings.System.DEFAULT_RINGTONE_URI;
            } else if (this.mStreamType == 5) {
                defaultUri = Settings.System.DEFAULT_NOTIFICATION_URI;
            } else {
                defaultUri = Settings.System.DEFAULT_ALARM_ALERT_URI;
            }
        }
        this.mDefaultUri = defaultUri;
        this.mHandler.sendEmptyMessage(3);
    }

    public void setSeekBar(SeekBar seekBar) {
        if (this.mSeekBar != null) {
            this.mSeekBar.setOnSeekBarChangeListener(null);
        }
        this.mSeekBar = seekBar;
        this.mSeekBar.setOnSeekBarChangeListener(null);
        this.mSeekBar.setMax(this.mMaxStreamVolume);
        this.mSeekBar.setProgress(this.mLastProgress > -1 ? this.mLastProgress : this.mOriginalStreamVolume);
        this.mSeekBar.setOnSeekBarChangeListener(this);
    }

    @Override // android.os.Handler.Callback
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case 0:
                this.mAudioManager.setStreamVolume(this.mStreamType, this.mLastProgress, 1024);
                return true;
            case 1:
                onStartSample();
                return true;
            case 2:
                onStopSample();
                return true;
            case 3:
                onInitSample();
                return true;
            default:
                Log.e(TAG, "invalid SeekBarVolumizer message: " + msg.what);
                return true;
        }
    }

    private void onInitSample() {
        this.mRingtone = RingtoneManager.getRingtone(this.mContext, this.mDefaultUri);
        if (this.mRingtone != null) {
            this.mRingtone.setStreamType(this.mStreamType);
        }
    }

    private void postStartSample() {
        this.mHandler.removeMessages(1);
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1), isSamplePlaying() ? 1000 : 0);
    }

    private void onStartSample() {
        if (!isSamplePlaying()) {
            if (this.mCallback != null) {
                this.mCallback.onSampleStarting(this);
            }
            if (this.mRingtone != null) {
                try {
                    this.mRingtone.play();
                } catch (Throwable e) {
                    Log.w(TAG, "Error playing ringtone, stream " + this.mStreamType, e);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void postStopSample() {
        this.mHandler.removeMessages(1);
        this.mHandler.removeMessages(2);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(2));
    }

    private void onStopSample() {
        if (this.mRingtone != null) {
            this.mRingtone.stop();
        }
    }

    public void stop() {
        postStopSample();
        this.mContext.getContentResolver().unregisterContentObserver(this.mVolumeObserver);
        this.mSeekBar.setOnSeekBarChangeListener(null);
        this.mReceiver.setListening(false);
        this.mHandler.getLooper().quitSafely();
    }

    public void revertVolume() {
        this.mAudioManager.setStreamVolume(this.mStreamType, this.mOriginalStreamVolume, 0);
    }

    @Override // android.widget.SeekBar.OnSeekBarChangeListener
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
        if (fromTouch) {
            postSetVolume(progress);
        }
    }

    /* access modifiers changed from: package-private */
    public void postSetVolume(int progress) {
        this.mLastProgress = progress;
        this.mHandler.removeMessages(0);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(0));
    }

    @Override // android.widget.SeekBar.OnSeekBarChangeListener
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override // android.widget.SeekBar.OnSeekBarChangeListener
    public void onStopTrackingTouch(SeekBar seekBar) {
        postStartSample();
    }

    public boolean isSamplePlaying() {
        return this.mRingtone != null && this.mRingtone.isPlaying();
    }

    public void startSample() {
        postStartSample();
    }

    public void stopSample() {
        postStopSample();
    }

    public SeekBar getSeekBar() {
        return this.mSeekBar;
    }

    public void changeVolumeBy(int amount) {
        this.mSeekBar.incrementProgressBy(amount);
        postSetVolume(this.mSeekBar.getProgress());
        postStartSample();
        this.mVolumeBeforeMute = -1;
    }

    public void muteVolume() {
        if (this.mVolumeBeforeMute != -1) {
            this.mSeekBar.setProgress(this.mVolumeBeforeMute);
            postSetVolume(this.mVolumeBeforeMute);
            postStartSample();
            this.mVolumeBeforeMute = -1;
            return;
        }
        this.mVolumeBeforeMute = this.mSeekBar.getProgress();
        this.mSeekBar.setProgress(0);
        postStopSample();
        postSetVolume(0);
    }

    public void onSaveInstanceState(VolumePreference.VolumeStore volumeStore) {
        if (this.mLastProgress >= 0) {
            volumeStore.volume = this.mLastProgress;
            volumeStore.originalVolume = this.mOriginalStreamVolume;
        }
    }

    public void onRestoreInstanceState(VolumePreference.VolumeStore volumeStore) {
        if (volumeStore.volume != -1) {
            this.mOriginalStreamVolume = volumeStore.originalVolume;
            this.mLastProgress = volumeStore.volume;
            postSetVolume(this.mLastProgress);
        }
    }

    /* access modifiers changed from: private */
    public final class H extends Handler {
        private static final int UPDATE_SLIDER = 1;

        private H() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == 1 && SeekBarVolumizer.this.mSeekBar != null) {
                SeekBarVolumizer.this.mSeekBar.setProgress(msg.arg1);
                SeekBarVolumizer.this.mLastProgress = SeekBarVolumizer.this.mSeekBar.getProgress();
            }
        }

        public void postUpdateSlider(int volume) {
            obtainMessage(1, volume, 0).sendToTarget();
        }
    }

    /* access modifiers changed from: private */
    public final class Observer extends ContentObserver {
        public Observer(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            if (SeekBarVolumizer.this.mSeekBar != null && SeekBarVolumizer.this.mAudioManager != null) {
                SeekBarVolumizer.this.mUiHandler.postUpdateSlider(SeekBarVolumizer.this.mAudioManager.getStreamVolume(SeekBarVolumizer.this.mStreamType));
            }
        }
    }

    /* access modifiers changed from: private */
    public final class Receiver extends BroadcastReceiver {
        private boolean mListening;

        private Receiver() {
        }

        public void setListening(boolean listening) {
            if (this.mListening != listening) {
                this.mListening = listening;
                if (listening) {
                    SeekBarVolumizer.this.mContext.registerReceiver(this, new IntentFilter(AudioManager.VOLUME_CHANGED_ACTION));
                    return;
                }
                SeekBarVolumizer.this.mContext.unregisterReceiver(this);
            }
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.VOLUME_CHANGED_ACTION.equals(intent.getAction())) {
                int streamType = intent.getIntExtra(AudioManager.EXTRA_VOLUME_STREAM_TYPE, -1);
                int streamValue = intent.getIntExtra(AudioManager.EXTRA_VOLUME_STREAM_VALUE, -1);
                if (SeekBarVolumizer.this.mSeekBar != null && streamType == SeekBarVolumizer.this.mStreamType && streamValue != -1) {
                    SeekBarVolumizer.this.mUiHandler.postUpdateSlider(streamValue);
                }
            }
        }
    }
}

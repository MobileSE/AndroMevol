package android.media;

import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.IAudioFocusDispatcher;
import android.media.IAudioService;
import android.media.audiopolicy.AudioPolicy;
import android.media.session.MediaSessionLegacyHelper;
import android.net.ProxyInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;

public class AudioManager {
    public static final String ACTION_ANALOG_AUDIO_DOCK_PLUG = "android.media.action.ANALOG_AUDIO_DOCK_PLUG";
    public static final String ACTION_AUDIO_BECOMING_NOISY = "android.media.AUDIO_BECOMING_NOISY";
    public static final String ACTION_DIGITAL_AUDIO_DOCK_PLUG = "android.media.action.DIGITAL_AUDIO_DOCK_PLUG";
    public static final String ACTION_HDMI_AUDIO_PLUG = "android.media.action.HDMI_AUDIO_PLUG";
    public static final String ACTION_HEADSET_PLUG = "android.intent.action.HEADSET_PLUG";
    @Deprecated
    public static final String ACTION_SCO_AUDIO_STATE_CHANGED = "android.media.SCO_AUDIO_STATE_CHANGED";
    public static final String ACTION_SCO_AUDIO_STATE_UPDATED = "android.media.ACTION_SCO_AUDIO_STATE_UPDATED";
    public static final String ACTION_USB_AUDIO_ACCESSORY_PLUG = "android.media.action.USB_AUDIO_ACCESSORY_PLUG";
    public static final String ACTION_USB_AUDIO_DEVICE_PLUG = "android.media.action.USB_AUDIO_DEVICE_PLUG";
    public static final int ADJUST_LOWER = -1;
    public static final int ADJUST_RAISE = 1;
    public static final int ADJUST_SAME = 0;
    public static final int AUDIOFOCUS_GAIN = 1;
    public static final int AUDIOFOCUS_GAIN_TRANSIENT = 2;
    public static final int AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE = 4;
    public static final int AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK = 3;
    public static final int AUDIOFOCUS_LOSS = -1;
    public static final int AUDIOFOCUS_LOSS_TRANSIENT = -2;
    public static final int AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK = -3;
    public static final int AUDIOFOCUS_NONE = 0;
    public static final int AUDIOFOCUS_REQUEST_FAILED = 0;
    public static final int AUDIOFOCUS_REQUEST_GRANTED = 1;
    static final int AUDIOPORT_GENERATION_INIT = 0;
    public static final int AUDIO_SESSION_ID_GENERATE = 0;
    public static final int[] DEFAULT_STREAM_VOLUME = {4, 7, 5, 11, 6, 5, 7, 7, 11, 11};
    public static final int DEVICE_IN_ANLG_DOCK_HEADSET = -2147483136;
    public static final int DEVICE_IN_BACK_MIC = -2147483520;
    public static final int DEVICE_IN_BLUETOOTH_SCO_HEADSET = -2147483640;
    public static final int DEVICE_IN_BUILTIN_MIC = -2147483644;
    public static final int DEVICE_IN_DGTL_DOCK_HEADSET = -2147482624;
    public static final int DEVICE_IN_FM_TUNER = -2147475456;
    public static final int DEVICE_IN_HDMI = -2147483616;
    public static final int DEVICE_IN_LINE = -2147450880;
    public static final int DEVICE_IN_LOOPBACK = -2147221504;
    public static final int DEVICE_IN_SPDIF = -2147418112;
    public static final int DEVICE_IN_TELEPHONY_RX = -2147483584;
    public static final int DEVICE_IN_TV_TUNER = -2147467264;
    public static final int DEVICE_IN_USB_ACCESSORY = -2147481600;
    public static final int DEVICE_IN_USB_DEVICE = -2147479552;
    public static final int DEVICE_IN_WIRED_HEADSET = -2147483632;
    public static final int DEVICE_NONE = 0;
    public static final int DEVICE_OUT_ANLG_DOCK_HEADSET = 2048;
    public static final int DEVICE_OUT_AUX_DIGITAL = 1024;
    public static final int DEVICE_OUT_BLUETOOTH_A2DP = 128;
    public static final int DEVICE_OUT_BLUETOOTH_A2DP_HEADPHONES = 256;
    public static final int DEVICE_OUT_BLUETOOTH_A2DP_SPEAKER = 512;
    public static final int DEVICE_OUT_BLUETOOTH_SCO = 16;
    public static final int DEVICE_OUT_BLUETOOTH_SCO_CARKIT = 64;
    public static final int DEVICE_OUT_BLUETOOTH_SCO_HEADSET = 32;
    public static final int DEVICE_OUT_DEFAULT = 1073741824;
    public static final int DEVICE_OUT_DGTL_DOCK_HEADSET = 4096;
    public static final int DEVICE_OUT_EARPIECE = 1;
    public static final int DEVICE_OUT_FM = 1048576;
    public static final int DEVICE_OUT_HDMI = 1024;
    public static final int DEVICE_OUT_HDMI_ARC = 262144;
    public static final int DEVICE_OUT_LINE = 131072;
    public static final int DEVICE_OUT_REMOTE_SUBMIX = 32768;
    public static final int DEVICE_OUT_SPDIF = 524288;
    public static final int DEVICE_OUT_SPEAKER = 2;
    public static final int DEVICE_OUT_TELEPHONY_TX = 65536;
    public static final int DEVICE_OUT_USB_ACCESSORY = 8192;
    public static final int DEVICE_OUT_USB_DEVICE = 16384;
    public static final int DEVICE_OUT_WIRED_HEADPHONE = 8;
    public static final int DEVICE_OUT_WIRED_HEADSET = 4;
    public static final int ERROR = -1;
    public static final int ERROR_BAD_VALUE = -2;
    public static final int ERROR_DEAD_OBJECT = -6;
    public static final int ERROR_INVALID_OPERATION = -3;
    public static final int ERROR_NO_INIT = -5;
    public static final int ERROR_PERMISSION_DENIED = -4;
    public static final String EXTRA_AUDIO_PLUG_STATE = "android.media.extra.AUDIO_PLUG_STATE";
    public static final String EXTRA_ENCODINGS = "android.media.extra.ENCODINGS";
    public static final String EXTRA_MASTER_VOLUME_MUTED = "android.media.EXTRA_MASTER_VOLUME_MUTED";
    public static final String EXTRA_MASTER_VOLUME_VALUE = "android.media.EXTRA_MASTER_VOLUME_VALUE";
    public static final String EXTRA_MAX_CHANNEL_COUNT = "android.media.extra.MAX_CHANNEL_COUNT";
    public static final String EXTRA_PREV_MASTER_VOLUME_VALUE = "android.media.EXTRA_PREV_MASTER_VOLUME_VALUE";
    public static final String EXTRA_PREV_VOLUME_STREAM_VALUE = "android.media.EXTRA_PREV_VOLUME_STREAM_VALUE";
    public static final String EXTRA_RINGER_MODE = "android.media.EXTRA_RINGER_MODE";
    public static final String EXTRA_SCO_AUDIO_PREVIOUS_STATE = "android.media.extra.SCO_AUDIO_PREVIOUS_STATE";
    public static final String EXTRA_SCO_AUDIO_STATE = "android.media.extra.SCO_AUDIO_STATE";
    public static final String EXTRA_VIBRATE_SETTING = "android.media.EXTRA_VIBRATE_SETTING";
    public static final String EXTRA_VIBRATE_TYPE = "android.media.EXTRA_VIBRATE_TYPE";
    public static final String EXTRA_VOLUME_STREAM_TYPE = "android.media.EXTRA_VOLUME_STREAM_TYPE";
    public static final String EXTRA_VOLUME_STREAM_VALUE = "android.media.EXTRA_VOLUME_STREAM_VALUE";
    public static final int FLAG_ACTIVE_MEDIA_ONLY = 512;
    public static final int FLAG_ALLOW_RINGER_MODES = 2;
    public static final int FLAG_BLUETOOTH_ABS_VOLUME = 64;
    public static final int FLAG_FIXED_VOLUME = 32;
    public static final int FLAG_HDMI_SYSTEM_AUDIO_VOLUME = 256;
    public static final int FLAG_PLAY_SOUND = 4;
    public static final int FLAG_REMOVE_SOUND_AND_VIBRATE = 8;
    public static final int FLAG_SHOW_SILENT_HINT = 128;
    public static final int FLAG_SHOW_UI = 1;
    public static final int FLAG_SHOW_UI_WARNINGS = 1024;
    public static final int FLAG_VIBRATE = 16;
    public static final int FX_FOCUS_NAVIGATION_DOWN = 2;
    public static final int FX_FOCUS_NAVIGATION_LEFT = 3;
    public static final int FX_FOCUS_NAVIGATION_RIGHT = 4;
    public static final int FX_FOCUS_NAVIGATION_UP = 1;
    public static final int FX_KEYPRESS_DELETE = 7;
    public static final int FX_KEYPRESS_INVALID = 9;
    public static final int FX_KEYPRESS_RETURN = 8;
    public static final int FX_KEYPRESS_SPACEBAR = 6;
    public static final int FX_KEYPRESS_STANDARD = 5;
    public static final int FX_KEY_CLICK = 0;
    public static final String MASTER_MUTE_CHANGED_ACTION = "android.media.MASTER_MUTE_CHANGED_ACTION";
    public static final String MASTER_VOLUME_CHANGED_ACTION = "android.media.MASTER_VOLUME_CHANGED_ACTION";
    public static final int MODE_CURRENT = -1;
    public static final int MODE_INVALID = -2;
    public static final int MODE_IN_CALL = 2;
    public static final int MODE_IN_COMMUNICATION = 3;
    public static final int MODE_NORMAL = 0;
    public static final int MODE_RINGTONE = 1;
    public static final int NUM_SOUND_EFFECTS = 10;
    @Deprecated
    public static final int NUM_STREAMS = 5;
    public static final String PROPERTY_OUTPUT_FRAMES_PER_BUFFER = "android.media.property.OUTPUT_FRAMES_PER_BUFFER";
    public static final String PROPERTY_OUTPUT_SAMPLE_RATE = "android.media.property.OUTPUT_SAMPLE_RATE";
    public static final String RINGER_MODE_CHANGED_ACTION = "android.media.RINGER_MODE_CHANGED";
    private static final int RINGER_MODE_MAX = 2;
    public static final int RINGER_MODE_NORMAL = 2;
    public static final int RINGER_MODE_SILENT = 0;
    public static final int RINGER_MODE_VIBRATE = 1;
    @Deprecated
    public static final int ROUTE_ALL = -1;
    @Deprecated
    public static final int ROUTE_BLUETOOTH = 4;
    @Deprecated
    public static final int ROUTE_BLUETOOTH_A2DP = 16;
    @Deprecated
    public static final int ROUTE_BLUETOOTH_SCO = 4;
    @Deprecated
    public static final int ROUTE_EARPIECE = 1;
    @Deprecated
    public static final int ROUTE_HEADSET = 8;
    @Deprecated
    public static final int ROUTE_SPEAKER = 2;
    public static final int SCO_AUDIO_STATE_CONNECTED = 1;
    public static final int SCO_AUDIO_STATE_CONNECTING = 2;
    public static final int SCO_AUDIO_STATE_DISCONNECTED = 0;
    public static final int SCO_AUDIO_STATE_ERROR = -1;
    public static final int STREAM_ALARM = 4;
    public static final int STREAM_BLUETOOTH_SCO = 6;
    public static final int STREAM_DTMF = 8;
    public static final int STREAM_MUSIC = 3;
    public static final int STREAM_NOTIFICATION = 5;
    public static final int STREAM_RING = 2;
    public static final int STREAM_SYSTEM = 1;
    public static final int STREAM_SYSTEM_ENFORCED = 7;
    public static final int STREAM_TTS = 9;
    public static final int STREAM_VOICE_CALL = 0;
    public static final int SUCCESS = 0;
    private static String TAG = "AudioManager";
    public static final int USE_DEFAULT_STREAM_TYPE = Integer.MIN_VALUE;
    public static final String VIBRATE_SETTING_CHANGED_ACTION = "android.media.VIBRATE_SETTING_CHANGED";
    public static final int VIBRATE_SETTING_OFF = 0;
    public static final int VIBRATE_SETTING_ON = 1;
    public static final int VIBRATE_SETTING_ONLY_SILENT = 2;
    public static final int VIBRATE_TYPE_NOTIFICATION = 1;
    public static final int VIBRATE_TYPE_RINGER = 0;
    public static final String VOLUME_CHANGED_ACTION = "android.media.VOLUME_CHANGED_ACTION";
    private static IAudioService sService;
    private final IAudioFocusDispatcher mAudioFocusDispatcher = new IAudioFocusDispatcher.Stub() {
        /* class android.media.AudioManager.AnonymousClass1 */

        @Override // android.media.IAudioFocusDispatcher
        public void dispatchAudioFocusChange(int focusChange, String id) {
            AudioManager.this.mAudioFocusEventHandlerDelegate.getHandler().sendMessage(AudioManager.this.mAudioFocusEventHandlerDelegate.getHandler().obtainMessage(focusChange, id));
        }
    };
    private final FocusEventHandlerDelegate mAudioFocusEventHandlerDelegate = new FocusEventHandlerDelegate();
    private final HashMap<String, OnAudioFocusChangeListener> mAudioFocusIdListenerMap = new HashMap<>();
    ArrayList<AudioPatch> mAudioPatchesCached = new ArrayList<>();
    AudioPortEventHandler mAudioPortEventHandler;
    Integer mAudioPortGeneration = new Integer(0);
    ArrayList<AudioPort> mAudioPortsCached = new ArrayList<>();
    private final Context mContext;
    private final Object mFocusListenerLock = new Object();
    private final IBinder mICallBack = new Binder();
    private final Binder mToken = new Binder();
    private final boolean mUseFixedVolume;
    private final boolean mUseMasterVolume;
    private final boolean mUseVolumeKeySounds;
    private long mVolumeKeyUpTime;

    public interface OnAudioFocusChangeListener {
        void onAudioFocusChange(int i);
    }

    public interface OnAudioPortUpdateListener {
        void onAudioPatchListUpdate(AudioPatch[] audioPatchArr);

        void onAudioPortListUpdate(AudioPort[] audioPortArr);

        void onServiceDied();
    }

    public AudioManager(Context context) {
        this.mContext = context;
        this.mUseMasterVolume = this.mContext.getResources().getBoolean(17956880);
        this.mUseVolumeKeySounds = this.mContext.getResources().getBoolean(17956881);
        this.mAudioPortEventHandler = new AudioPortEventHandler(this);
        this.mUseFixedVolume = this.mContext.getResources().getBoolean(17956968);
    }

    private static IAudioService getService() {
        if (sService != null) {
            return sService;
        }
        sService = IAudioService.Stub.asInterface(ServiceManager.getService(Context.AUDIO_SERVICE));
        return sService;
    }

    public void dispatchMediaKeyEvent(KeyEvent keyEvent) {
        MediaSessionLegacyHelper.getHelper(this.mContext).sendMediaButtonEvent(keyEvent, false);
    }

    public void preDispatchKeyEvent(KeyEvent event, int stream) {
        int keyCode = event.getKeyCode();
        if (keyCode != 25 && keyCode != 24 && keyCode != 164 && this.mVolumeKeyUpTime + 300 > SystemClock.uptimeMillis()) {
            if (this.mUseMasterVolume) {
                adjustMasterVolume(0, 8);
            } else {
                adjustSuggestedStreamVolume(0, stream, 8);
            }
        }
    }

    public void handleKeyDown(KeyEvent event, int stream) {
        boolean z = true;
        int i = 1;
        int i2 = 1;
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case 24:
            case 25:
                if (this.mUseMasterVolume) {
                    if (keyCode != 24) {
                        i = -1;
                    }
                    adjustMasterVolume(i, 17);
                    return;
                }
                if (keyCode != 24) {
                    i2 = -1;
                }
                adjustSuggestedStreamVolume(i2, stream, 17);
                return;
            case 164:
                if (event.getRepeatCount() == 0 && this.mUseMasterVolume) {
                    if (isMasterMute()) {
                        z = false;
                    }
                    setMasterMute(z);
                    return;
                }
                return;
            default:
                return;
        }
    }

    public void handleKeyUp(KeyEvent event, int stream) {
        switch (event.getKeyCode()) {
            case 24:
            case 25:
                if (this.mUseVolumeKeySounds) {
                    if (this.mUseMasterVolume) {
                        adjustMasterVolume(0, 4);
                    } else {
                        adjustSuggestedStreamVolume(0, stream, 4);
                    }
                }
                this.mVolumeKeyUpTime = SystemClock.uptimeMillis();
                return;
            default:
                return;
        }
    }

    public boolean isVolumeFixed() {
        return this.mUseFixedVolume;
    }

    public void adjustStreamVolume(int streamType, int direction, int flags) {
        IAudioService service = getService();
        try {
            if (this.mUseMasterVolume) {
                service.adjustMasterVolume(direction, flags, this.mContext.getOpPackageName());
            } else {
                service.adjustStreamVolume(streamType, direction, flags, this.mContext.getOpPackageName());
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in adjustStreamVolume", e);
        }
    }

    public void adjustVolume(int direction, int flags) {
        IAudioService service = getService();
        try {
            if (this.mUseMasterVolume) {
                service.adjustMasterVolume(direction, flags, this.mContext.getOpPackageName());
            } else {
                MediaSessionLegacyHelper.getHelper(this.mContext).sendAdjustVolumeBy(Integer.MIN_VALUE, direction, flags);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in adjustVolume", e);
        }
    }

    public void adjustSuggestedStreamVolume(int direction, int suggestedStreamType, int flags) {
        IAudioService service = getService();
        try {
            if (this.mUseMasterVolume) {
                service.adjustMasterVolume(direction, flags, this.mContext.getOpPackageName());
            } else {
                MediaSessionLegacyHelper.getHelper(this.mContext).sendAdjustVolumeBy(suggestedStreamType, direction, flags);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in adjustSuggestedStreamVolume", e);
        }
    }

    public void adjustMasterVolume(int steps, int flags) {
        try {
            getService().adjustMasterVolume(steps, flags, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in adjustMasterVolume", e);
        }
    }

    public int getRingerMode() {
        try {
            return getService().getRingerMode();
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in getRingerMode", e);
            return 2;
        }
    }

    public static boolean isValidRingerMode(int ringerMode) {
        if (ringerMode < 0 || ringerMode > 2) {
            return false;
        }
        return true;
    }

    public int getStreamMaxVolume(int streamType) {
        IAudioService service = getService();
        try {
            if (this.mUseMasterVolume) {
                return service.getMasterMaxVolume();
            }
            return service.getStreamMaxVolume(streamType);
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in getStreamMaxVolume", e);
            return 0;
        }
    }

    public int getStreamVolume(int streamType) {
        IAudioService service = getService();
        try {
            if (this.mUseMasterVolume) {
                return service.getMasterVolume();
            }
            return service.getStreamVolume(streamType);
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in getStreamVolume", e);
            return 0;
        }
    }

    public int getLastAudibleStreamVolume(int streamType) {
        IAudioService service = getService();
        try {
            if (this.mUseMasterVolume) {
                return service.getLastAudibleMasterVolume();
            }
            return service.getLastAudibleStreamVolume(streamType);
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in getLastAudibleStreamVolume", e);
            return 0;
        }
    }

    public int getMasterStreamType() {
        try {
            return getService().getMasterStreamType();
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in getMasterStreamType", e);
            return 2;
        }
    }

    public void setRingerMode(int ringerMode) {
        setRingerMode(ringerMode, true);
    }

    public void setRingerMode(int ringerMode, boolean checkZen) {
        if (isValidRingerMode(ringerMode)) {
            try {
                getService().setRingerMode(ringerMode, checkZen);
            } catch (RemoteException e) {
                Log.e(TAG, "Dead object in setRingerMode", e);
            }
        }
    }

    public void setStreamVolume(int streamType, int index, int flags) {
        IAudioService service = getService();
        try {
            if (this.mUseMasterVolume) {
                service.setMasterVolume(index, flags, this.mContext.getOpPackageName());
            } else {
                service.setStreamVolume(streamType, index, flags, this.mContext.getOpPackageName());
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in setStreamVolume", e);
        }
    }

    public int getMasterMaxVolume() {
        try {
            return getService().getMasterMaxVolume();
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in getMasterMaxVolume", e);
            return 0;
        }
    }

    public int getMasterVolume() {
        try {
            return getService().getMasterVolume();
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in getMasterVolume", e);
            return 0;
        }
    }

    public int getLastAudibleMasterVolume() {
        try {
            return getService().getLastAudibleMasterVolume();
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in getLastAudibleMasterVolume", e);
            return 0;
        }
    }

    public void setMasterVolume(int index, int flags) {
        try {
            getService().setMasterVolume(index, flags, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in setMasterVolume", e);
        }
    }

    public void setStreamSolo(int streamType, boolean state) {
        try {
            getService().setStreamSolo(streamType, state, this.mICallBack);
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in setStreamSolo", e);
        }
    }

    public void setStreamMute(int streamType, boolean state) {
        try {
            getService().setStreamMute(streamType, state, this.mICallBack);
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in setStreamMute", e);
        }
    }

    public boolean isStreamMute(int streamType) {
        try {
            return getService().isStreamMute(streamType);
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in isStreamMute", e);
            return false;
        }
    }

    public void setMasterMute(boolean state) {
        setMasterMute(state, 1);
    }

    public void setMasterMute(boolean state, int flags) {
        try {
            getService().setMasterMute(state, flags, this.mContext.getOpPackageName(), this.mICallBack);
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in setMasterMute", e);
        }
    }

    public boolean isMasterMute() {
        try {
            return getService().isMasterMute();
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in isMasterMute", e);
            return false;
        }
    }

    public void forceVolumeControlStream(int streamType) {
        try {
            getService().forceVolumeControlStream(streamType, this.mICallBack);
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in forceVolumeControlStream", e);
        }
    }

    public boolean shouldVibrate(int vibrateType) {
        try {
            return getService().shouldVibrate(vibrateType);
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in shouldVibrate", e);
            return false;
        }
    }

    public int getVibrateSetting(int vibrateType) {
        try {
            return getService().getVibrateSetting(vibrateType);
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in getVibrateSetting", e);
            return 0;
        }
    }

    public void setVibrateSetting(int vibrateType, int vibrateSetting) {
        try {
            getService().setVibrateSetting(vibrateType, vibrateSetting);
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in setVibrateSetting", e);
        }
    }

    public void setSpeakerphoneOn(boolean on) {
        try {
            getService().setSpeakerphoneOn(on);
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in setSpeakerphoneOn", e);
        }
    }

    public boolean isSpeakerphoneOn() {
        try {
            return getService().isSpeakerphoneOn();
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in isSpeakerphoneOn", e);
            return false;
        }
    }

    public boolean isBluetoothScoAvailableOffCall() {
        return this.mContext.getResources().getBoolean(17956927);
    }

    public void startBluetoothSco() {
        try {
            getService().startBluetoothSco(this.mICallBack, this.mContext.getApplicationInfo().targetSdkVersion);
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in startBluetoothSco", e);
        }
    }

    public void startBluetoothScoVirtualCall() {
        try {
            getService().startBluetoothScoVirtualCall(this.mICallBack);
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in startBluetoothScoVirtualCall", e);
        }
    }

    public void stopBluetoothSco() {
        try {
            getService().stopBluetoothSco(this.mICallBack);
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in stopBluetoothSco", e);
        }
    }

    public void setBluetoothScoOn(boolean on) {
        try {
            getService().setBluetoothScoOn(on);
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in setBluetoothScoOn", e);
        }
    }

    public boolean isBluetoothScoOn() {
        try {
            return getService().isBluetoothScoOn();
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in isBluetoothScoOn", e);
            return false;
        }
    }

    @Deprecated
    public void setBluetoothA2dpOn(boolean on) {
    }

    public boolean isBluetoothA2dpOn() {
        if (AudioSystem.getDeviceConnectionState(128, ProxyInfo.LOCAL_EXCL_LIST) == 0) {
            return false;
        }
        return true;
    }

    @Deprecated
    public void setWiredHeadsetOn(boolean on) {
    }

    public boolean isWiredHeadsetOn() {
        if (AudioSystem.getDeviceConnectionState(4, ProxyInfo.LOCAL_EXCL_LIST) == 0 && AudioSystem.getDeviceConnectionState(8, ProxyInfo.LOCAL_EXCL_LIST) == 0) {
            return false;
        }
        return true;
    }

    public void setMicrophoneMute(boolean on) {
        try {
            getService().setMicrophoneMute(on, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in setMicrophoneMute", e);
        }
    }

    public boolean isMicrophoneMute() {
        return AudioSystem.isMicrophoneMuted();
    }

    public void setMode(int mode) {
        try {
            getService().setMode(mode, this.mICallBack);
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in setMode", e);
        }
    }

    public int getMode() {
        try {
            return getService().getMode();
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in getMode", e);
            return -2;
        }
    }

    @Deprecated
    public void setRouting(int mode, int routes, int mask) {
    }

    @Deprecated
    public int getRouting(int mode) {
        return -1;
    }

    public boolean isMusicActive() {
        return AudioSystem.isStreamActive(3, 0);
    }

    public boolean isMusicActiveRemotely() {
        return AudioSystem.isStreamActiveRemotely(3, 0);
    }

    public boolean isAudioFocusExclusive() {
        try {
            if (getService().getCurrentAudioFocus() == 4) {
                return true;
            }
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in isAudioFocusExclusive()", e);
            return false;
        }
    }

    public int generateAudioSessionId() {
        int session = AudioSystem.newAudioSessionId();
        if (session > 0) {
            return session;
        }
        Log.e(TAG, "Failure to generate a new audio session ID");
        return -1;
    }

    @Deprecated
    public void setParameter(String key, String value) {
        setParameters(key + "=" + value);
    }

    public void setParameters(String keyValuePairs) {
        AudioSystem.setParameters(keyValuePairs);
    }

    public String getParameters(String keys) {
        return AudioSystem.getParameters(keys);
    }

    public void playSoundEffect(int effectType) {
        if (effectType >= 0 && effectType < 10 && querySoundEffectsEnabled(Process.myUserHandle().getIdentifier())) {
            try {
                getService().playSoundEffect(effectType);
            } catch (RemoteException e) {
                Log.e(TAG, "Dead object in playSoundEffect" + e);
            }
        }
    }

    public void playSoundEffect(int effectType, int userId) {
        if (effectType >= 0 && effectType < 10 && querySoundEffectsEnabled(userId)) {
            try {
                getService().playSoundEffect(effectType);
            } catch (RemoteException e) {
                Log.e(TAG, "Dead object in playSoundEffect" + e);
            }
        }
    }

    public void playSoundEffect(int effectType, float volume) {
        if (effectType >= 0 && effectType < 10) {
            try {
                getService().playSoundEffectVolume(effectType, volume);
            } catch (RemoteException e) {
                Log.e(TAG, "Dead object in playSoundEffect" + e);
            }
        }
    }

    private boolean querySoundEffectsEnabled(int user) {
        return Settings.System.getIntForUser(this.mContext.getContentResolver(), Settings.System.SOUND_EFFECTS_ENABLED, 0, user) != 0;
    }

    public void loadSoundEffects() {
        try {
            getService().loadSoundEffects();
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in loadSoundEffects" + e);
        }
    }

    public void unloadSoundEffects() {
        try {
            getService().unloadSoundEffects();
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in unloadSoundEffects" + e);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private OnAudioFocusChangeListener findFocusListener(String id) {
        return this.mAudioFocusIdListenerMap.get(id);
    }

    /* access modifiers changed from: private */
    public class FocusEventHandlerDelegate {
        private final Handler mHandler;

        FocusEventHandlerDelegate() {
            Looper looper = Looper.myLooper();
            looper = looper == null ? Looper.getMainLooper() : looper;
            if (looper != null) {
                this.mHandler = new Handler(looper, AudioManager.this) {
                    /* class android.media.AudioManager.FocusEventHandlerDelegate.AnonymousClass1 */

                    @Override // android.os.Handler
                    public void handleMessage(Message msg) {
                        OnAudioFocusChangeListener listener;
                        synchronized (AudioManager.this.mFocusListenerLock) {
                            listener = AudioManager.this.findFocusListener((String) msg.obj);
                        }
                        if (listener != null) {
                            listener.onAudioFocusChange(msg.what);
                        }
                    }
                };
            } else {
                this.mHandler = null;
            }
        }

        /* access modifiers changed from: package-private */
        public Handler getHandler() {
            return this.mHandler;
        }
    }

    private String getIdForAudioFocusListener(OnAudioFocusChangeListener l) {
        if (l == null) {
            return new String(toString());
        }
        return new String(toString() + l.toString());
    }

    public void registerAudioFocusListener(OnAudioFocusChangeListener l) {
        synchronized (this.mFocusListenerLock) {
            if (!this.mAudioFocusIdListenerMap.containsKey(getIdForAudioFocusListener(l))) {
                this.mAudioFocusIdListenerMap.put(getIdForAudioFocusListener(l), l);
            }
        }
    }

    public void unregisterAudioFocusListener(OnAudioFocusChangeListener l) {
        synchronized (this.mFocusListenerLock) {
            this.mAudioFocusIdListenerMap.remove(getIdForAudioFocusListener(l));
        }
    }

    public int requestAudioFocus(OnAudioFocusChangeListener l, int streamType, int durationHint) {
        int status = 0;
        if (durationHint < 1 || durationHint > 4) {
            Log.e(TAG, "Invalid duration hint, audio focus request denied");
            return 0;
        }
        registerAudioFocusListener(l);
        try {
            status = getService().requestAudioFocus(streamType, durationHint, this.mICallBack, this.mAudioFocusDispatcher, getIdForAudioFocusListener(l), this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            Log.e(TAG, "Can't call requestAudioFocus() on AudioService due to " + e);
        }
        return status;
    }

    public void requestAudioFocusForCall(int streamType, int durationHint) {
        try {
            getService().requestAudioFocus(streamType, durationHint, this.mICallBack, null, "AudioFocus_For_Phone_Ring_And_Calls", this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            Log.e(TAG, "Can't call requestAudioFocusForCall() on AudioService due to " + e);
        }
    }

    public void abandonAudioFocusForCall() {
        try {
            getService().abandonAudioFocus(null, "AudioFocus_For_Phone_Ring_And_Calls");
        } catch (RemoteException e) {
            Log.e(TAG, "Can't call abandonAudioFocusForCall() on AudioService due to " + e);
        }
    }

    public int abandonAudioFocus(OnAudioFocusChangeListener l) {
        unregisterAudioFocusListener(l);
        try {
            return getService().abandonAudioFocus(this.mAudioFocusDispatcher, getIdForAudioFocusListener(l));
        } catch (RemoteException e) {
            Log.e(TAG, "Can't call abandonAudioFocus() on AudioService due to " + e);
            return 0;
        }
    }

    @Deprecated
    public void registerMediaButtonEventReceiver(ComponentName eventReceiver) {
        if (eventReceiver != null) {
            if (!eventReceiver.getPackageName().equals(this.mContext.getPackageName())) {
                Log.e(TAG, "registerMediaButtonEventReceiver() error: receiver and context package names don't match");
                return;
            }
            Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
            mediaButtonIntent.setComponent(eventReceiver);
            registerMediaButtonIntent(PendingIntent.getBroadcast(this.mContext, 0, mediaButtonIntent, 0), eventReceiver);
        }
    }

    @Deprecated
    public void registerMediaButtonEventReceiver(PendingIntent eventReceiver) {
        if (eventReceiver != null) {
            registerMediaButtonIntent(eventReceiver, null);
        }
    }

    public void registerMediaButtonIntent(PendingIntent pi, ComponentName eventReceiver) {
        if (pi == null) {
            Log.e(TAG, "Cannot call registerMediaButtonIntent() with a null parameter");
        } else {
            MediaSessionLegacyHelper.getHelper(this.mContext).addMediaButtonListener(pi, eventReceiver, this.mContext);
        }
    }

    @Deprecated
    public void unregisterMediaButtonEventReceiver(ComponentName eventReceiver) {
        if (eventReceiver != null) {
            Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
            mediaButtonIntent.setComponent(eventReceiver);
            unregisterMediaButtonIntent(PendingIntent.getBroadcast(this.mContext, 0, mediaButtonIntent, 0));
        }
    }

    @Deprecated
    public void unregisterMediaButtonEventReceiver(PendingIntent eventReceiver) {
        if (eventReceiver != null) {
            unregisterMediaButtonIntent(eventReceiver);
        }
    }

    public void unregisterMediaButtonIntent(PendingIntent pi) {
        MediaSessionLegacyHelper.getHelper(this.mContext).removeMediaButtonListener(pi);
    }

    @Deprecated
    public void registerRemoteControlClient(RemoteControlClient rcClient) {
        if (rcClient != null && rcClient.getRcMediaIntent() != null) {
            rcClient.registerWithSession(MediaSessionLegacyHelper.getHelper(this.mContext));
        }
    }

    @Deprecated
    public void unregisterRemoteControlClient(RemoteControlClient rcClient) {
        if (rcClient != null && rcClient.getRcMediaIntent() != null) {
            rcClient.unregisterWithSession(MediaSessionLegacyHelper.getHelper(this.mContext));
        }
    }

    @Deprecated
    public boolean registerRemoteController(RemoteController rctlr) {
        if (rctlr == null) {
            return false;
        }
        rctlr.startListeningToSessions();
        return true;
    }

    @Deprecated
    public void unregisterRemoteController(RemoteController rctlr) {
        if (rctlr != null) {
            rctlr.stopListeningToSessions();
        }
    }

    public void registerRemoteControlDisplay(IRemoteControlDisplay rcd) {
        registerRemoteControlDisplay(rcd, -1, -1);
    }

    public void registerRemoteControlDisplay(IRemoteControlDisplay rcd, int w, int h) {
        if (rcd != null) {
            try {
                getService().registerRemoteControlDisplay(rcd, w, h);
            } catch (RemoteException e) {
                Log.e(TAG, "Dead object in registerRemoteControlDisplay " + e);
            }
        }
    }

    public void unregisterRemoteControlDisplay(IRemoteControlDisplay rcd) {
        if (rcd != null) {
            try {
                getService().unregisterRemoteControlDisplay(rcd);
            } catch (RemoteException e) {
                Log.e(TAG, "Dead object in unregisterRemoteControlDisplay " + e);
            }
        }
    }

    public void remoteControlDisplayUsesBitmapSize(IRemoteControlDisplay rcd, int w, int h) {
        if (rcd != null) {
            try {
                getService().remoteControlDisplayUsesBitmapSize(rcd, w, h);
            } catch (RemoteException e) {
                Log.e(TAG, "Dead object in remoteControlDisplayUsesBitmapSize " + e);
            }
        }
    }

    public void remoteControlDisplayWantsPlaybackPositionSync(IRemoteControlDisplay rcd, boolean wantsSync) {
        if (rcd != null) {
            try {
                getService().remoteControlDisplayWantsPlaybackPositionSync(rcd, wantsSync);
            } catch (RemoteException e) {
                Log.e(TAG, "Dead object in remoteControlDisplayWantsPlaybackPositionSync " + e);
            }
        }
    }

    public int registerAudioPolicy(AudioPolicy policy) {
        if (policy == null) {
            throw new IllegalArgumentException("Illegal null AudioPolicy argument");
        }
        try {
            if (!getService().registerAudioPolicy(policy.getConfig(), policy.token())) {
                return -1;
            }
            return 0;
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in registerAudioPolicyAsync()", e);
            return -1;
        }
    }

    public void unregisterAudioPolicyAsync(AudioPolicy policy) {
        if (policy == null) {
            throw new IllegalArgumentException("Illegal null AudioPolicy argument");
        }
        try {
            getService().unregisterAudioPolicyAsync(policy.token());
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in unregisterAudioPolicyAsync()", e);
        }
    }

    public void reloadAudioSettings() {
        try {
            getService().reloadAudioSettings();
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in reloadAudioSettings" + e);
        }
    }

    public void avrcpSupportsAbsoluteVolume(String address, boolean support) {
        try {
            getService().avrcpSupportsAbsoluteVolume(address, support);
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in avrcpSupportsAbsoluteVolume", e);
        }
    }

    public boolean isSilentMode() {
        int ringerMode = getRingerMode();
        if (ringerMode == 0 || ringerMode == 1) {
            return true;
        }
        return false;
    }

    public static boolean isOutputDevice(int device) {
        return (Integer.MIN_VALUE & device) == 0;
    }

    public static boolean isInputDevice(int device) {
        return (device & Integer.MIN_VALUE) == Integer.MIN_VALUE;
    }

    public int getDevicesForStream(int streamType) {
        switch (streamType) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 8:
                return AudioSystem.getDevicesForStream(streamType);
            case 6:
            case 7:
            default:
                return 0;
        }
    }

    public void setWiredDeviceConnectionState(int device, int state, String name) {
        try {
            getService().setWiredDeviceConnectionState(device, state, name);
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in setWiredDeviceConnectionState " + e);
        }
    }

    public int setBluetoothA2dpDeviceConnectionState(BluetoothDevice device, int state, int profile) {
        try {
            return getService().setBluetoothA2dpDeviceConnectionState(device, state, profile);
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in setBluetoothA2dpDeviceConnectionState " + e);
            return 0;
        } catch (Throwable th) {
            return 0;
        }
    }

    public IRingtonePlayer getRingtonePlayer() {
        try {
            return getService().getRingtonePlayer();
        } catch (RemoteException e) {
            return null;
        }
    }

    public String getProperty(String key) {
        int outputFramesPerBuffer;
        if (PROPERTY_OUTPUT_SAMPLE_RATE.equals(key)) {
            int outputSampleRate = AudioSystem.getPrimaryOutputSamplingRate();
            if (outputSampleRate > 0) {
                return Integer.toString(outputSampleRate);
            }
            return null;
        } else if (!PROPERTY_OUTPUT_FRAMES_PER_BUFFER.equals(key) || (outputFramesPerBuffer = AudioSystem.getPrimaryOutputFrameCount()) <= 0) {
            return null;
        } else {
            return Integer.toString(outputFramesPerBuffer);
        }
    }

    public int getOutputLatency(int streamType) {
        return AudioSystem.getOutputLatency(streamType);
    }

    public void setVolumeController(IVolumeController controller) {
        try {
            getService().setVolumeController(controller);
        } catch (RemoteException e) {
            Log.w(TAG, "Error setting volume controller", e);
        }
    }

    public void notifyVolumeControllerVisible(IVolumeController controller, boolean visible) {
        try {
            getService().notifyVolumeControllerVisible(controller, visible);
        } catch (RemoteException e) {
            Log.w(TAG, "Error notifying about volume controller visibility", e);
        }
    }

    public boolean isStreamAffectedByRingerMode(int streamType) {
        try {
            return getService().isStreamAffectedByRingerMode(streamType);
        } catch (RemoteException e) {
            Log.w(TAG, "Error calling isStreamAffectedByRingerMode", e);
            return false;
        }
    }

    public void disableSafeMediaVolume() {
        try {
            getService().disableSafeMediaVolume();
        } catch (RemoteException e) {
            Log.w(TAG, "Error disabling safe media volume", e);
        }
    }

    public int setHdmiSystemAudioSupported(boolean on) {
        try {
            return getService().setHdmiSystemAudioSupported(on);
        } catch (RemoteException e) {
            Log.w(TAG, "Error setting system audio mode", e);
            return 0;
        }
    }

    public boolean isHdmiSystemAudioSupported() {
        try {
            return getService().isHdmiSystemAudioSupported();
        } catch (RemoteException e) {
            Log.w(TAG, "Error querying system audio mode", e);
            return false;
        }
    }

    public int listAudioPorts(ArrayList<AudioPort> ports) {
        return updateAudioPortCache(ports, null);
    }

    public int listAudioDevicePorts(ArrayList<AudioPort> devices) {
        ArrayList<AudioPort> ports = new ArrayList<>();
        int status = updateAudioPortCache(ports, null);
        if (status == 0) {
            devices.clear();
            for (int i = 0; i < ports.size(); i++) {
                if (ports.get(i) instanceof AudioDevicePort) {
                    devices.add(ports.get(i));
                }
            }
        }
        return status;
    }

    public int createAudioPatch(AudioPatch[] patch, AudioPortConfig[] sources, AudioPortConfig[] sinks) {
        return AudioSystem.createAudioPatch(patch, sources, sinks);
    }

    public int releaseAudioPatch(AudioPatch patch) {
        return AudioSystem.releaseAudioPatch(patch);
    }

    public int listAudioPatches(ArrayList<AudioPatch> patches) {
        return updateAudioPortCache(null, patches);
    }

    public int setAudioPortGain(AudioPort port, AudioGainConfig gain) {
        if (port == null || gain == null) {
            return -2;
        }
        AudioPortConfig activeConfig = port.activeConfig();
        AudioPortConfig config = new AudioPortConfig(port, activeConfig.samplingRate(), activeConfig.channelMask(), activeConfig.format(), gain);
        config.mConfigMask = 8;
        return AudioSystem.setAudioPortConfig(config);
    }

    public void registerAudioPortUpdateListener(OnAudioPortUpdateListener l) {
        this.mAudioPortEventHandler.registerListener(l);
    }

    public void unregisterAudioPortUpdateListener(OnAudioPortUpdateListener l) {
        this.mAudioPortEventHandler.unregisterListener(l);
    }

    /* access modifiers changed from: package-private */
    public int resetAudioPortGeneration() {
        int generation;
        synchronized (this.mAudioPortGeneration) {
            generation = this.mAudioPortGeneration.intValue();
            this.mAudioPortGeneration = 0;
        }
        return generation;
    }

    /* access modifiers changed from: package-private */
    public int updateAudioPortCache(ArrayList<AudioPort> ports, ArrayList<AudioPatch> patches) {
        synchronized (this.mAudioPortGeneration) {
            if (this.mAudioPortGeneration.intValue() == 0) {
                int[] patchGeneration = new int[1];
                int[] portGeneration = new int[1];
                ArrayList<AudioPort> newPorts = new ArrayList<>();
                ArrayList<AudioPatch> newPatches = new ArrayList<>();
                do {
                    newPorts.clear();
                    int status = AudioSystem.listAudioPorts(newPorts, portGeneration);
                    if (status != 0) {
                        return status;
                    }
                    newPatches.clear();
                    int status2 = AudioSystem.listAudioPatches(newPatches, patchGeneration);
                    if (status2 != 0) {
                        return status2;
                    }
                } while (patchGeneration[0] != portGeneration[0]);
                for (int i = 0; i < newPatches.size(); i++) {
                    for (int j = 0; j < newPatches.get(i).sources().length; j++) {
                        AudioPortConfig portCfg = updatePortConfig(newPatches.get(i).sources()[j], newPorts);
                        if (portCfg == null) {
                            return -1;
                        }
                        newPatches.get(i).sources()[j] = portCfg;
                    }
                    for (int j2 = 0; j2 < newPatches.get(i).sinks().length; j2++) {
                        AudioPortConfig portCfg2 = updatePortConfig(newPatches.get(i).sinks()[j2], newPorts);
                        if (portCfg2 == null) {
                            return -1;
                        }
                        newPatches.get(i).sinks()[j2] = portCfg2;
                    }
                }
                this.mAudioPortsCached = newPorts;
                this.mAudioPatchesCached = newPatches;
                this.mAudioPortGeneration = Integer.valueOf(portGeneration[0]);
            }
            if (ports != null) {
                ports.clear();
                ports.addAll(this.mAudioPortsCached);
            }
            if (patches != null) {
                patches.clear();
                patches.addAll(this.mAudioPatchesCached);
            }
            return 0;
        }
    }

    /* access modifiers changed from: package-private */
    public AudioPortConfig updatePortConfig(AudioPortConfig portCfg, ArrayList<AudioPort> ports) {
        AudioPort port = portCfg.port();
        int k = 0;
        while (true) {
            if (k >= ports.size()) {
                break;
            } else if (ports.get(k).handle().equals(port.handle())) {
                port = ports.get(k);
                break;
            } else {
                k++;
            }
        }
        if (k == ports.size()) {
            Log.e(TAG, "updatePortConfig port not found for handle: " + port.handle().id());
            return null;
        }
        AudioGainConfig gainCfg = portCfg.gain();
        if (gainCfg != null) {
            gainCfg = port.gain(gainCfg.index()).buildConfig(gainCfg.mode(), gainCfg.channelMask(), gainCfg.values(), gainCfg.rampDurationMs());
        }
        return port.buildConfig(portCfg.samplingRate(), portCfg.channelMask(), portCfg.format(), gainCfg);
    }
}

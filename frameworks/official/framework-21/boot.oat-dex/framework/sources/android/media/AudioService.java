package android.media;

import android.Manifest;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.AppOpsManager;
import android.app.KeyguardManager;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.database.ContentObserver;
import android.hardware.hdmi.HdmiControlManager;
import android.hardware.hdmi.HdmiPlaybackClient;
import android.hardware.hdmi.HdmiTvClient;
import android.hardware.usb.UsbManager;
import android.media.AudioSystem;
import android.media.IAudioService;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.media.audiopolicy.AudioPolicyConfig;
import android.net.ProxyInfo;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.Vibrator;
import android.provider.Settings;
import android.telecom.TelecomManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.MathUtils;
import android.util.Slog;
import android.view.OrientationEventListener;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import com.android.internal.util.XmlUtils;
import com.android.server.LocalServices;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.xmlpull.v1.XmlPullParserException;

public class AudioService extends IAudioService.Stub {
    private static final String ASSET_FILE_VERSION = "1.0";
    private static final String ATTR_ASSET_FILE = "file";
    private static final String ATTR_ASSET_ID = "id";
    private static final String ATTR_GROUP_NAME = "name";
    private static final String ATTR_VERSION = "version";
    private static final int BTA2DP_DOCK_TIMEOUT_MILLIS = 8000;
    private static final int BT_HEADSET_CNCT_TIMEOUT_MS = 3000;
    protected static final boolean DEBUG_MODE = Log.isLoggable("AudioService.MOD", 3);
    private static final boolean DEBUG_SESSIONS = Log.isLoggable("AudioService.SESSIONS", 3);
    protected static final boolean DEBUG_VOL = Log.isLoggable("AudioService.VOL", 3);
    private static final int FLAG_ADJUST_VOLUME = 1;
    private static final String GROUP_TOUCH_SOUNDS = "touch_sounds";
    private static final int MAX_BATCH_VOLUME_ADJUST_STEPS = 4;
    private static final int MAX_MASTER_VOLUME = 100;
    private static final int[] MAX_STREAM_VOLUME = {5, 7, 7, 15, 7, 7, 15, 7, 15, 15};
    private static final int MSG_BROADCAST_AUDIO_BECOMING_NOISY = 15;
    private static final int MSG_BROADCAST_BT_CONNECTION_STATE = 19;
    private static final int MSG_BTA2DP_DOCK_TIMEOUT = 6;
    private static final int MSG_BT_HEADSET_CNCT_FAILED = 9;
    private static final int MSG_CHECK_MUSIC_ACTIVE = 14;
    private static final int MSG_CONFIGURE_SAFE_MEDIA_VOLUME = 16;
    private static final int MSG_CONFIGURE_SAFE_MEDIA_VOLUME_FORCED = 17;
    private static final int MSG_LOAD_SOUND_EFFECTS = 7;
    private static final int MSG_MEDIA_SERVER_DIED = 4;
    private static final int MSG_PERSIST_MASTER_VOLUME = 2;
    private static final int MSG_PERSIST_MASTER_VOLUME_MUTE = 11;
    private static final int MSG_PERSIST_MICROPHONE_MUTE = 23;
    private static final int MSG_PERSIST_MUSIC_ACTIVE_MS = 22;
    private static final int MSG_PERSIST_RINGER_MODE = 3;
    private static final int MSG_PERSIST_SAFE_VOLUME_STATE = 18;
    private static final int MSG_PERSIST_VOLUME = 1;
    private static final int MSG_PLAY_SOUND_EFFECT = 5;
    private static final int MSG_REPORT_NEW_ROUTES = 12;
    private static final int MSG_SET_A2DP_SINK_CONNECTION_STATE = 102;
    private static final int MSG_SET_A2DP_SRC_CONNECTION_STATE = 101;
    private static final int MSG_SET_ALL_VOLUMES = 10;
    private static final int MSG_SET_DEVICE_VOLUME = 0;
    private static final int MSG_SET_FORCE_BT_A2DP_USE = 13;
    private static final int MSG_SET_FORCE_USE = 8;
    private static final int MSG_SET_WIRED_DEVICE_CONNECTION_STATE = 100;
    private static final int MSG_SYSTEM_READY = 21;
    private static final int MSG_UNLOAD_SOUND_EFFECTS = 20;
    private static final int MUSIC_ACTIVE_POLL_PERIOD_MS = 60000;
    private static final int NUM_SOUNDPOOL_CHANNELS = 4;
    private static final int PERSIST_DELAY = 500;
    private static final int PLATFORM_DEFAULT = 0;
    private static final int PLATFORM_TELEVISION = 2;
    private static final int PLATFORM_VOICE = 1;
    public static final int PLAY_SOUND_DELAY = 300;
    private static final boolean PREVENT_VOLUME_ADJUSTMENT_IF_SILENT = true;
    private static final String[] RINGER_MODE_NAMES = {"SILENT", "VIBRATE", "NORMAL"};
    private static final int SAFE_MEDIA_VOLUME_ACTIVE = 3;
    private static final int SAFE_MEDIA_VOLUME_DISABLED = 1;
    private static final int SAFE_MEDIA_VOLUME_INACTIVE = 2;
    private static final int SAFE_MEDIA_VOLUME_NOT_CONFIGURED = 0;
    private static final int SAFE_VOLUME_CONFIGURE_TIMEOUT_MS = 30000;
    private static final int SCO_MODE_MAX = 2;
    private static final int SCO_MODE_RAW = 1;
    private static final int SCO_MODE_UNDEFINED = -1;
    private static final int SCO_MODE_VIRTUAL_CALL = 0;
    private static final int SCO_MODE_VR = 2;
    private static final int SCO_STATE_ACTIVATE_REQ = 1;
    private static final int SCO_STATE_ACTIVE_EXTERNAL = 2;
    private static final int SCO_STATE_ACTIVE_INTERNAL = 3;
    private static final int SCO_STATE_DEACTIVATE_EXT_REQ = 4;
    private static final int SCO_STATE_DEACTIVATE_REQ = 5;
    private static final int SCO_STATE_INACTIVE = 0;
    private static final int SENDMSG_NOOP = 1;
    private static final int SENDMSG_QUEUE = 2;
    private static final int SENDMSG_REPLACE = 0;
    private static final int SOUND_EFFECTS_LOAD_TIMEOUT_MS = 5000;
    private static final String SOUND_EFFECTS_PATH = "/media/audio/ui/";
    private static final List<String> SOUND_EFFECT_FILES = new ArrayList();
    private static final int[] STEAM_VOLUME_OPS = {34, 36, 35, 36, 37, 38, 39, 36, 36, 36};
    private static final String[] STREAM_NAMES = {"STREAM_VOICE_CALL", "STREAM_SYSTEM", "STREAM_RING", "STREAM_MUSIC", "STREAM_ALARM", "STREAM_NOTIFICATION", "STREAM_BLUETOOTH_SCO", "STREAM_SYSTEM_ENFORCED", "STREAM_DTMF", "STREAM_TTS"};
    private static final String TAG = "AudioService";
    private static final String TAG_ASSET = "asset";
    private static final String TAG_AUDIO_ASSETS = "audio_assets";
    private static final String TAG_GROUP = "group";
    private static final int UNSAFE_VOLUME_MUSIC_ACTIVE_MS_MAX = 72000000;
    private static final boolean VOLUME_SETS_RINGER_MODE_SILENT = false;
    private static int sSoundEffectVolumeDb;
    private final int[][] SOUND_EFFECT_FILES_MAP = ((int[][]) Array.newInstance(Integer.TYPE, 10, 2));
    private final int[] STREAM_VOLUME_ALIAS_DEFAULT = {0, 2, 2, 3, 4, 2, 6, 2, 2, 3};
    private final int[] STREAM_VOLUME_ALIAS_TELEVISION = {3, 3, 3, 3, 3, 3, 3, 3, 3, 3};
    private final int[] STREAM_VOLUME_ALIAS_VOICE = {0, 2, 2, 3, 4, 2, 6, 2, 2, 3};
    private BluetoothA2dp mA2dp;
    private final Object mA2dpAvrcpLock = new Object();
    private final AppOpsManager mAppOps;
    private PowerManager.WakeLock mAudioEventWakeLock;
    private AudioHandler mAudioHandler;
    private HashMap<IBinder, AudioPolicyProxy> mAudioPolicies = new HashMap<>();
    private final AudioSystem.ErrorCallback mAudioSystemCallback = new AudioSystem.ErrorCallback() {
        /* class android.media.AudioService.AnonymousClass1 */

        @Override // android.media.AudioSystem.ErrorCallback
        public void onError(int error) {
            switch (error) {
                case 100:
                    AudioService.sendMsg(AudioService.this.mAudioHandler, 4, 1, 0, 0, null, 0);
                    return;
                default:
                    return;
            }
        }
    };
    private AudioSystemThread mAudioSystemThread;
    private boolean mAvrcpAbsVolSupported = false;
    int mBecomingNoisyIntentDevices = 163724;
    private boolean mBluetoothA2dpEnabled;
    private final Object mBluetoothA2dpEnabledLock = new Object();
    private BluetoothHeadset mBluetoothHeadset;
    private BluetoothDevice mBluetoothHeadsetDevice;
    private BluetoothProfile.ServiceListener mBluetoothProfileServiceListener = new BluetoothProfile.ServiceListener() {
        /* class android.media.AudioService.AnonymousClass2 */

        @Override // android.bluetooth.BluetoothProfile.ServiceListener
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            switch (profile) {
                case 1:
                    synchronized (AudioService.this.mScoClients) {
                        AudioService.this.mAudioHandler.removeMessages(9);
                        AudioService.this.mBluetoothHeadset = (BluetoothHeadset) proxy;
                        List<BluetoothDevice> deviceList = AudioService.this.mBluetoothHeadset.getConnectedDevices();
                        if (deviceList.size() > 0) {
                            AudioService.this.mBluetoothHeadsetDevice = deviceList.get(0);
                        } else {
                            AudioService.this.mBluetoothHeadsetDevice = null;
                        }
                        AudioService.this.checkScoAudioState();
                        if (AudioService.this.mScoAudioState == 1 || AudioService.this.mScoAudioState == 5 || AudioService.this.mScoAudioState == 4) {
                            boolean status = false;
                            if (AudioService.this.mBluetoothHeadsetDevice != null) {
                                switch (AudioService.this.mScoAudioState) {
                                    case 1:
                                        AudioService.this.mScoAudioState = 3;
                                        if (AudioService.this.mScoAudioMode != 1) {
                                            if (AudioService.this.mScoAudioMode != 0) {
                                                if (AudioService.this.mScoAudioMode == 2) {
                                                    status = AudioService.this.mBluetoothHeadset.startVoiceRecognition(AudioService.this.mBluetoothHeadsetDevice);
                                                    break;
                                                }
                                            } else {
                                                status = AudioService.this.mBluetoothHeadset.startScoUsingVirtualVoiceCall(AudioService.this.mBluetoothHeadsetDevice);
                                                break;
                                            }
                                        } else {
                                            status = AudioService.this.mBluetoothHeadset.connectAudio();
                                            break;
                                        }
                                        break;
                                    case 4:
                                        status = AudioService.this.mBluetoothHeadset.stopVoiceRecognition(AudioService.this.mBluetoothHeadsetDevice);
                                        break;
                                    case 5:
                                        if (AudioService.this.mScoAudioMode != 1) {
                                            if (AudioService.this.mScoAudioMode != 0) {
                                                if (AudioService.this.mScoAudioMode == 2) {
                                                    status = AudioService.this.mBluetoothHeadset.stopVoiceRecognition(AudioService.this.mBluetoothHeadsetDevice);
                                                    break;
                                                }
                                            } else {
                                                status = AudioService.this.mBluetoothHeadset.stopScoUsingVirtualVoiceCall(AudioService.this.mBluetoothHeadsetDevice);
                                                break;
                                            }
                                        } else {
                                            status = AudioService.this.mBluetoothHeadset.disconnectAudio();
                                            break;
                                        }
                                        break;
                                }
                            }
                            if (!status) {
                                AudioService.sendMsg(AudioService.this.mAudioHandler, 9, 0, 0, 0, null, 0);
                            }
                        }
                    }
                    return;
                case 2:
                    synchronized (AudioService.this.mA2dpAvrcpLock) {
                        AudioService.this.mA2dp = (BluetoothA2dp) proxy;
                        List<BluetoothDevice> deviceList2 = AudioService.this.mA2dp.getConnectedDevices();
                        if (deviceList2.size() > 0) {
                            BluetoothDevice btDevice = deviceList2.get(0);
                            synchronized (AudioService.this.mConnectedDevices) {
                                int state = AudioService.this.mA2dp.getConnectionState(btDevice);
                                AudioService.this.queueMsgUnderWakeLock(AudioService.this.mAudioHandler, 102, state, 0, btDevice, AudioService.this.checkSendBecomingNoisyIntent(128, state == 2 ? 1 : 0));
                            }
                        }
                    }
                    return;
                case 10:
                    List<BluetoothDevice> deviceList3 = proxy.getConnectedDevices();
                    if (deviceList3.size() > 0) {
                        BluetoothDevice btDevice2 = deviceList3.get(0);
                        synchronized (AudioService.this.mConnectedDevices) {
                            AudioService.this.queueMsgUnderWakeLock(AudioService.this.mAudioHandler, 101, proxy.getConnectionState(btDevice2), 0, btDevice2, 0);
                        }
                        return;
                    }
                    return;
                default:
                    return;
            }
        }

        @Override // android.bluetooth.BluetoothProfile.ServiceListener
        public void onServiceDisconnected(int profile) {
            switch (profile) {
                case 1:
                    synchronized (AudioService.this.mScoClients) {
                        AudioService.this.mBluetoothHeadset = null;
                    }
                    return;
                case 2:
                    synchronized (AudioService.this.mA2dpAvrcpLock) {
                        AudioService.this.mA2dp = null;
                        synchronized (AudioService.this.mConnectedDevices) {
                            if (AudioService.this.mConnectedDevices.containsKey(128)) {
                                AudioService.this.makeA2dpDeviceUnavailableNow((String) AudioService.this.mConnectedDevices.get(128));
                            }
                        }
                    }
                    return;
                case 10:
                    synchronized (AudioService.this.mConnectedDevices) {
                        if (AudioService.this.mConnectedDevices.containsKey(Integer.valueOf((int) AudioSystem.DEVICE_IN_BLUETOOTH_A2DP))) {
                            AudioService.this.makeA2dpSrcUnavailable((String) AudioService.this.mConnectedDevices.get(Integer.valueOf((int) AudioSystem.DEVICE_IN_BLUETOOTH_A2DP)));
                        }
                    }
                    return;
                default:
                    return;
            }
        }
    };
    private Boolean mCameraSoundForced;
    private final HashMap<Integer, String> mConnectedDevices = new HashMap<>();
    private final ContentResolver mContentResolver;
    private final Context mContext;
    final AudioRoutesInfo mCurAudioRoutes = new AudioRoutesInfo();
    private int mDeviceOrientation = 0;
    private int mDeviceRotation = 0;
    private String mDockAddress;
    private boolean mDockAudioMediaEnabled = true;
    private int mDockState = 0;
    int mFixedVolumeDevices = 2890752;
    private ForceControlStreamClient mForceControlStreamClient = null;
    private final Object mForceControlStreamLock = new Object();
    private int mForcedUseForComm;
    int mFullVolumeDevices = 0;
    private final boolean mHasVibrator;
    private boolean mHdmiCecSink;
    private MyDisplayStatusCallback mHdmiDisplayStatusCallback = new MyDisplayStatusCallback();
    private HdmiControlManager mHdmiManager;
    private HdmiPlaybackClient mHdmiPlaybackClient;
    private boolean mHdmiSystemAudioSupported = false;
    private HdmiTvClient mHdmiTvClient;
    private KeyguardManager mKeyguardManager;
    private final int[] mMasterVolumeRamp;
    private int mMcc = 0;
    private final MediaFocusControl mMediaFocusControl;
    private int mMode = 0;
    private final boolean mMonitorOrientation;
    private final boolean mMonitorRotation;
    private int mMusicActiveMs;
    private int mMuteAffectedStreams;
    private AudioOrientationEventListener mOrientationListener;
    private StreamVolumeCommand mPendingVolumeCommand;
    private final int mPlatformType;
    private int mPrevVolDirection = 0;
    private final BroadcastReceiver mReceiver = new AudioServiceBroadcastReceiver();
    private int mRingerMode;
    private int mRingerModeAffectedStreams = 0;
    private int mRingerModeMutedStreams;
    private volatile IRingtonePlayer mRingtonePlayer;
    private ArrayList<RmtSbmxFullVolDeathHandler> mRmtSbmxFullVolDeathHandlers = new ArrayList<>();
    private int mRmtSbmxFullVolRefCount = 0;
    final RemoteCallbackList<IAudioRoutesObserver> mRoutesObservers = new RemoteCallbackList<>();
    private final int mSafeMediaVolumeDevices = 12;
    private int mSafeMediaVolumeIndex;
    private Integer mSafeMediaVolumeState;
    private int mScoAudioMode;
    private int mScoAudioState;
    private final ArrayList<ScoClient> mScoClients = new ArrayList<>();
    private int mScoConnectionState;
    private final ArrayList<SetModeDeathHandler> mSetModeDeathHandlers = new ArrayList<>();
    private final Object mSettingsLock = new Object();
    private SettingsObserver mSettingsObserver;
    private final Object mSoundEffectsLock = new Object();
    private SoundPool mSoundPool;
    private SoundPoolCallback mSoundPoolCallBack;
    private SoundPoolListenerThread mSoundPoolListenerThread;
    private Looper mSoundPoolLooper = null;
    private VolumeStreamState[] mStreamStates;
    private int[] mStreamVolumeAlias;
    private boolean mSystemReady;
    private final boolean mUseFixedVolume;
    private final boolean mUseMasterVolume;
    private int mVibrateSetting;
    private int mVolumeControlStream = -1;
    private final VolumeController mVolumeController = new VolumeController();

    private boolean isPlatformVoice() {
        return this.mPlatformType == 1;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isPlatformTelevision() {
        return this.mPlatformType == 2;
    }

    public AudioService(Context context) {
        int i;
        this.mContext = context;
        this.mContentResolver = context.getContentResolver();
        this.mAppOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        if (this.mContext.getResources().getBoolean(17956931)) {
            this.mPlatformType = 1;
        } else if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEVISION)) {
            this.mPlatformType = 2;
        } else {
            this.mPlatformType = 0;
        }
        this.mAudioEventWakeLock = ((PowerManager) context.getSystemService(Context.POWER_SERVICE)).newWakeLock(1, "handleAudioEvent");
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        this.mHasVibrator = vibrator == null ? false : vibrator.hasVibrator();
        MAX_STREAM_VOLUME[0] = SystemProperties.getInt("ro.config.vc_call_vol_steps", MAX_STREAM_VOLUME[0]);
        MAX_STREAM_VOLUME[3] = SystemProperties.getInt("ro.config.media_vol_steps", MAX_STREAM_VOLUME[3]);
        sSoundEffectVolumeDb = context.getResources().getInteger(17694724);
        this.mForcedUseForComm = 0;
        createAudioSystemThread();
        this.mMediaFocusControl = new MediaFocusControl(this.mAudioHandler.getLooper(), this.mContext, this.mVolumeController, this);
        AudioSystem.setErrorCallback(this.mAudioSystemCallback);
        boolean cameraSoundForced = this.mContext.getResources().getBoolean(17956963);
        this.mCameraSoundForced = new Boolean(cameraSoundForced);
        AudioHandler audioHandler = this.mAudioHandler;
        if (cameraSoundForced) {
            i = 11;
        } else {
            i = 0;
        }
        sendMsg(audioHandler, 8, 2, 4, i, null, 0);
        this.mSafeMediaVolumeState = new Integer(Settings.Global.getInt(this.mContentResolver, Settings.Global.AUDIO_SAFE_VOLUME_STATE, 0));
        this.mSafeMediaVolumeIndex = this.mContext.getResources().getInteger(17694831) * 10;
        this.mUseFixedVolume = this.mContext.getResources().getBoolean(17956968);
        updateStreamVolumeAlias(false);
        readPersistedSettings();
        this.mSettingsObserver = new SettingsObserver();
        createStreamStates();
        readAndSetLowRamDevice();
        this.mRingerModeMutedStreams = 0;
        setRingerModeInt(getRingerMode(), false);
        IntentFilter intentFilter = new IntentFilter(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED);
        intentFilter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
        intentFilter.addAction(Intent.ACTION_DOCK_EVENT);
        intentFilter.addAction(AudioManager.ACTION_USB_AUDIO_ACCESSORY_PLUG);
        intentFilter.addAction(AudioManager.ACTION_USB_AUDIO_DEVICE_PLUG);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_USER_SWITCHED);
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        intentFilter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
        this.mMonitorOrientation = SystemProperties.getBoolean("ro.audio.monitorOrientation", false);
        if (this.mMonitorOrientation) {
            Log.v(TAG, "monitoring device orientation");
            setOrientationForAudioSystem();
        }
        this.mMonitorRotation = SystemProperties.getBoolean("ro.audio.monitorRotation", false);
        if (this.mMonitorRotation) {
            this.mDeviceRotation = ((WindowManager) this.mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
            Log.v(TAG, "monitoring device rotation, initial=" + this.mDeviceRotation);
            this.mOrientationListener = new AudioOrientationEventListener(this.mContext);
            this.mOrientationListener.enable();
            setRotationForAudioSystem();
        }
        context.registerReceiver(this.mReceiver, intentFilter);
        this.mUseMasterVolume = context.getResources().getBoolean(17956880);
        restoreMasterVolume();
        this.mMasterVolumeRamp = context.getResources().getIntArray(17235979);
        LocalServices.addService(AudioManagerInternal.class, new AudioServiceInternal());
    }

    public void systemReady() {
        sendMsg(this.mAudioHandler, 21, 2, 0, 0, null, 0);
    }

    public void onSystemReady() {
        this.mSystemReady = true;
        sendMsg(this.mAudioHandler, 7, 2, 0, 0, null, 0);
        this.mKeyguardManager = (KeyguardManager) this.mContext.getSystemService(Context.KEYGUARD_SERVICE);
        this.mScoConnectionState = -1;
        resetBluetoothSco();
        getBluetoothHeadset();
        Intent newIntent = new Intent(AudioManager.ACTION_SCO_AUDIO_STATE_CHANGED);
        newIntent.putExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, 0);
        sendStickyBroadcastToAll(newIntent);
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null) {
            adapter.getProfileProxy(this.mContext, this.mBluetoothProfileServiceListener, 2);
        }
        this.mHdmiManager = (HdmiControlManager) this.mContext.getSystemService(Context.HDMI_CONTROL_SERVICE);
        if (this.mHdmiManager != null) {
            synchronized (this.mHdmiManager) {
                this.mHdmiTvClient = this.mHdmiManager.getTvClient();
                if (this.mHdmiTvClient != null) {
                    this.mFixedVolumeDevices &= -2883587;
                }
                this.mHdmiPlaybackClient = this.mHdmiManager.getPlaybackClient();
                this.mHdmiCecSink = false;
            }
        }
        sendMsg(this.mAudioHandler, 17, 0, 0, 0, null, 30000);
        StreamOverride.init(this.mContext);
    }

    private void createAudioSystemThread() {
        this.mAudioSystemThread = new AudioSystemThread();
        this.mAudioSystemThread.start();
        waitForAudioHandlerCreation();
    }

    private void waitForAudioHandlerCreation() {
        synchronized (this) {
            while (this.mAudioHandler == null) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Log.e(TAG, "Interrupted while waiting on volume handler.");
                }
            }
        }
    }

    private void checkAllAliasStreamVolumes() {
        int numStreamTypes = AudioSystem.getNumStreamTypes();
        for (int streamType = 0; streamType < numStreamTypes; streamType++) {
            if (streamType != this.mStreamVolumeAlias[streamType]) {
                this.mStreamStates[streamType].setAllIndexes(this.mStreamStates[this.mStreamVolumeAlias[streamType]]);
            }
            if (!this.mStreamStates[streamType].isMuted()) {
                this.mStreamStates[streamType].applyAllVolumes();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkAllFixedVolumeDevices() {
        int numStreamTypes = AudioSystem.getNumStreamTypes();
        for (int streamType = 0; streamType < numStreamTypes; streamType++) {
            this.mStreamStates[streamType].checkFixedVolumeDevices();
        }
    }

    private void checkAllFixedVolumeDevices(int streamType) {
        this.mStreamStates[streamType].checkFixedVolumeDevices();
    }

    private void createStreamStates() {
        int numStreamTypes = AudioSystem.getNumStreamTypes();
        VolumeStreamState[] streams = new VolumeStreamState[numStreamTypes];
        this.mStreamStates = streams;
        for (int i = 0; i < numStreamTypes; i++) {
            streams[i] = new VolumeStreamState(Settings.System.VOLUME_SETTINGS[this.mStreamVolumeAlias[i]], i);
        }
        checkAllFixedVolumeDevices();
        checkAllAliasStreamVolumes();
    }

    private void dumpStreamStates(PrintWriter pw) {
        pw.println("\nStream volumes (device: index)");
        int numStreamTypes = AudioSystem.getNumStreamTypes();
        for (int i = 0; i < numStreamTypes; i++) {
            pw.println("- " + STREAM_NAMES[i] + ":");
            this.mStreamStates[i].dump(pw);
            pw.println(ProxyInfo.LOCAL_EXCL_LIST);
        }
        pw.print("\n- mute affected streams = 0x");
        pw.println(Integer.toHexString(this.mMuteAffectedStreams));
    }

    public static String streamToString(int stream) {
        if (stream >= 0 && stream < STREAM_NAMES.length) {
            return STREAM_NAMES[stream];
        }
        if (stream == Integer.MIN_VALUE) {
            return "USE_DEFAULT_STREAM_TYPE";
        }
        return "UNKNOWN_STREAM_" + stream;
    }

    private void updateStreamVolumeAlias(boolean updateVolumes) {
        int dtmfStreamAlias;
        switch (this.mPlatformType) {
            case 1:
                this.mStreamVolumeAlias = this.STREAM_VOLUME_ALIAS_VOICE;
                dtmfStreamAlias = 2;
                break;
            case 2:
                this.mStreamVolumeAlias = this.STREAM_VOLUME_ALIAS_TELEVISION;
                dtmfStreamAlias = 3;
                break;
            default:
                this.mStreamVolumeAlias = this.STREAM_VOLUME_ALIAS_DEFAULT;
                dtmfStreamAlias = 3;
                break;
        }
        if (isPlatformTelevision()) {
            this.mRingerModeAffectedStreams = 0;
        } else if (isInCommunication()) {
            dtmfStreamAlias = 0;
            this.mRingerModeAffectedStreams &= -257;
        } else {
            this.mRingerModeAffectedStreams |= 256;
        }
        this.mStreamVolumeAlias[8] = dtmfStreamAlias;
        if (updateVolumes) {
            this.mStreamStates[8].setAllIndexes(this.mStreamStates[dtmfStreamAlias]);
            setRingerModeInt(getRingerMode(), false);
            sendMsg(this.mAudioHandler, 10, 2, 0, 0, this.mStreamStates[8], 0);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void readDockAudioSettings(ContentResolver cr) {
        int i;
        boolean z = true;
        if (Settings.Global.getInt(cr, Settings.Global.DOCK_AUDIO_MEDIA_ENABLED, 0) != 1) {
            z = false;
        }
        this.mDockAudioMediaEnabled = z;
        if (this.mDockAudioMediaEnabled) {
            this.mBecomingNoisyIntentDevices |= 2048;
        } else {
            this.mBecomingNoisyIntentDevices &= -2049;
        }
        AudioHandler audioHandler = this.mAudioHandler;
        if (this.mDockAudioMediaEnabled) {
            i = 8;
        } else {
            i = 0;
        }
        sendMsg(audioHandler, 8, 2, 3, i, null, 0);
    }

    private void readPersistedSettings() {
        int i;
        boolean masterMute;
        boolean microphoneMute;
        int i2 = 2;
        ContentResolver cr = this.mContentResolver;
        int ringerModeFromSettings = Settings.Global.getInt(cr, "mode_ringer", 2);
        int ringerMode = ringerModeFromSettings;
        if (!AudioManager.isValidRingerMode(ringerMode)) {
            ringerMode = 2;
        }
        if (ringerMode == 1 && !this.mHasVibrator) {
            ringerMode = 0;
        }
        if (ringerMode != ringerModeFromSettings) {
            Settings.Global.putInt(cr, "mode_ringer", ringerMode);
        }
        if (this.mUseFixedVolume || isPlatformTelevision()) {
            ringerMode = 2;
        }
        synchronized (this.mSettingsLock) {
            this.mRingerMode = ringerMode;
            if (this.mHasVibrator) {
                i = 2;
            } else {
                i = 0;
            }
            this.mVibrateSetting = getValueForVibrateSetting(0, 1, i);
            int i3 = this.mVibrateSetting;
            if (!this.mHasVibrator) {
                i2 = 0;
            }
            this.mVibrateSetting = getValueForVibrateSetting(i3, 0, i2);
            updateRingerModeAffectedStreams();
            readDockAudioSettings(cr);
        }
        this.mMuteAffectedStreams = Settings.System.getIntForUser(cr, Settings.System.MUTE_STREAMS_AFFECTED, 14, -2);
        if (Settings.System.getIntForUser(cr, Settings.System.VOLUME_MASTER_MUTE, 0, -2) == 1) {
            masterMute = true;
        } else {
            masterMute = false;
        }
        if (this.mUseFixedVolume) {
            masterMute = false;
            AudioSystem.setMasterVolume(1.0f);
        }
        AudioSystem.setMasterMute(masterMute);
        broadcastMasterMuteStatus(masterMute);
        if (Settings.System.getIntForUser(cr, Settings.System.MICROPHONE_MUTE, 0, -2) == 1) {
            microphoneMute = true;
        } else {
            microphoneMute = false;
        }
        AudioSystem.muteMicrophone(microphoneMute);
        broadcastRingerMode(ringerMode);
        broadcastVibrateSetting(0);
        broadcastVibrateSetting(1);
        this.mVolumeController.loadSettings(cr);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int rescaleIndex(int index, int srcStream, int dstStream) {
        return ((this.mStreamStates[dstStream].getMaxIndex() * index) + (this.mStreamStates[srcStream].getMaxIndex() / 2)) / this.mStreamStates[srcStream].getMaxIndex();
    }

    /* access modifiers changed from: private */
    public class AudioOrientationEventListener extends OrientationEventListener {
        public AudioOrientationEventListener(Context context) {
            super(context);
        }

        @Override // android.view.OrientationEventListener
        public void onOrientationChanged(int orientation) {
            int newRotation = ((WindowManager) AudioService.this.mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
            if (newRotation != AudioService.this.mDeviceRotation) {
                AudioService.this.mDeviceRotation = newRotation;
                AudioService.this.setRotationForAudioSystem();
            }
        }
    }

    @Override // android.media.IAudioService
    public void adjustSuggestedStreamVolume(int direction, int suggestedStreamType, int flags, String callingPackage) {
        adjustSuggestedStreamVolume(direction, suggestedStreamType, flags, callingPackage, Binder.getCallingUid());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void adjustSuggestedStreamVolume(int direction, int suggestedStreamType, int flags, String callingPackage, int uid) {
        int streamType;
        if (DEBUG_VOL) {
            Log.d(TAG, "adjustSuggestedStreamVolume() stream=" + suggestedStreamType + ", flags=" + flags);
        }
        if (this.mVolumeControlStream != -1) {
            streamType = this.mVolumeControlStream;
        } else {
            streamType = getActiveStreamType(suggestedStreamType);
        }
        int resolvedStream = this.mStreamVolumeAlias[streamType];
        if (!((flags & 4) == 0 || resolvedStream == 2)) {
            flags &= -5;
        }
        if (this.mVolumeController.suppressAdjustment(resolvedStream, flags)) {
            direction = 0;
            flags = flags & -5 & -17;
            if (DEBUG_VOL) {
                Log.d(TAG, "Volume controller suppressed adjustment");
            }
        }
        adjustStreamVolume(streamType, direction, flags, callingPackage, uid);
    }

    @Override // android.media.IAudioService
    public void adjustStreamVolume(int streamType, int direction, int flags, String callingPackage) {
        adjustStreamVolume(streamType, direction, flags, callingPackage, Binder.getCallingUid());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void adjustStreamVolume(int streamType, int direction, int flags, String callingPackage, int uid) {
        int step;
        if (!this.mUseFixedVolume) {
            if (DEBUG_VOL) {
                Log.d(TAG, "adjustStreamVolume() stream=" + streamType + ", dir=" + direction + ", flags=" + flags);
            }
            ensureValidDirection(direction);
            ensureValidStreamType(streamType);
            int streamTypeAlias = this.mStreamVolumeAlias[streamType];
            VolumeStreamState streamState = this.mStreamStates[streamTypeAlias];
            int device = getDeviceForStream(streamTypeAlias);
            int aliasIndex = streamState.getIndex(device);
            boolean adjustVolume = true;
            if (((device & AudioSystem.DEVICE_OUT_ALL_A2DP) != 0 || (flags & 64) == 0) && this.mAppOps.noteOp(STEAM_VOLUME_OPS[streamTypeAlias], uid, callingPackage) == 0) {
                synchronized (this.mSafeMediaVolumeState) {
                    this.mPendingVolumeCommand = null;
                }
                int flags2 = flags & -33;
                if (streamTypeAlias != 3 || (this.mFixedVolumeDevices & device) == 0) {
                    step = rescaleIndex(10, streamType, streamTypeAlias);
                } else {
                    flags2 |= 32;
                    if (this.mSafeMediaVolumeState.intValue() != 3 || (device & 12) == 0) {
                        step = streamState.getMaxIndex();
                    } else {
                        step = this.mSafeMediaVolumeIndex;
                    }
                    if (aliasIndex != 0) {
                        aliasIndex = step;
                    }
                }
                if ((flags2 & 2) != 0 || streamTypeAlias == getMasterStreamType()) {
                    if (getRingerMode() == 1) {
                        flags2 &= -17;
                    }
                    int result = checkForRingerModeChange(aliasIndex, direction, step);
                    adjustVolume = (result & 1) != 0;
                    if ((result & 128) != 0) {
                        flags2 |= 128;
                    }
                }
                int oldIndex = this.mStreamStates[streamType].getIndex(device);
                if (adjustVolume && direction != 0) {
                    if (streamTypeAlias == 3 && (device & AudioSystem.DEVICE_OUT_ALL_A2DP) != 0 && (flags2 & 64) == 0) {
                        synchronized (this.mA2dpAvrcpLock) {
                            if (this.mA2dp != null && this.mAvrcpAbsVolSupported) {
                                this.mA2dp.adjustAvrcpAbsoluteVolume(direction);
                            }
                        }
                    }
                    if (direction == 1 && !checkSafeMediaVolume(streamTypeAlias, aliasIndex + step, device)) {
                        Log.e(TAG, "adjustStreamVolume() safe volume index = " + oldIndex);
                        this.mVolumeController.postDisplaySafeVolumeWarning(flags2);
                    } else if (streamState.adjustIndex(direction * step, device)) {
                        sendMsg(this.mAudioHandler, 0, 2, device, 0, streamState, 0);
                    }
                    int newIndex = this.mStreamStates[streamType].getIndex(device);
                    if (this.mHdmiManager != null) {
                        synchronized (this.mHdmiManager) {
                            if (this.mHdmiTvClient != null && streamTypeAlias == 3 && (flags2 & 256) == 0 && oldIndex != newIndex) {
                                int maxIndex = getStreamMaxVolume(streamType);
                                synchronized (this.mHdmiTvClient) {
                                    if (this.mHdmiSystemAudioSupported) {
                                        this.mHdmiTvClient.setSystemAudioVolume((oldIndex + 5) / 10, (newIndex + 5) / 10, maxIndex);
                                    }
                                }
                            }
                            if (this.mHdmiCecSink && streamTypeAlias == 3 && oldIndex != newIndex) {
                                synchronized (this.mHdmiPlaybackClient) {
                                    int keyCode = direction == -1 ? 25 : 24;
                                    this.mHdmiPlaybackClient.sendKeyEvent(keyCode, true);
                                    this.mHdmiPlaybackClient.sendKeyEvent(keyCode, false);
                                }
                            }
                        }
                    }
                }
                sendVolumeUpdate(streamType, oldIndex, this.mStreamStates[streamType].getIndex(device), flags2);
            }
        }
    }

    @Override // android.media.IAudioService
    public void adjustMasterVolume(int steps, int flags, String callingPackage) {
        if (!this.mUseFixedVolume) {
            ensureValidSteps(steps);
            int volume = Math.round(AudioSystem.getMasterVolume() * 100.0f);
            int numSteps = Math.abs(steps);
            int direction = steps > 0 ? 1 : -1;
            for (int i = 0; i < numSteps; i++) {
                volume += findVolumeDelta(direction, volume);
            }
            setMasterVolume(volume, flags, callingPackage);
        }
    }

    /* access modifiers changed from: package-private */
    public class StreamVolumeCommand {
        public final int mDevice;
        public final int mFlags;
        public final int mIndex;
        public final int mStreamType;

        StreamVolumeCommand(int streamType, int index, int flags, int device) {
            this.mStreamType = streamType;
            this.mIndex = index;
            this.mFlags = flags;
            this.mDevice = device;
        }

        public String toString() {
            return "{streamType=" + this.mStreamType + ",index=" + this.mIndex + ",flags=" + this.mFlags + ",device=" + this.mDevice + '}';
        }
    }

    private void onSetStreamVolume(int streamType, int index, int flags, int device) {
        int newRingerMode;
        setStreamVolumeInt(this.mStreamVolumeAlias[streamType], index, device, false);
        if ((flags & 2) != 0 || this.mStreamVolumeAlias[streamType] == getMasterStreamType()) {
            if (index == 0) {
                newRingerMode = this.mHasVibrator ? 1 : 2;
            } else {
                newRingerMode = 2;
            }
            setRingerMode(newRingerMode, false);
        }
    }

    @Override // android.media.IAudioService
    public void setStreamVolume(int streamType, int index, int flags, String callingPackage) {
        setStreamVolume(streamType, index, flags, callingPackage, Binder.getCallingUid());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setStreamVolume(int streamType, int index, int flags, String callingPackage, int uid) {
        int oldIndex;
        int index2;
        int flags2;
        if (!this.mUseFixedVolume) {
            ensureValidStreamType(streamType);
            int streamTypeAlias = this.mStreamVolumeAlias[streamType];
            VolumeStreamState streamState = this.mStreamStates[streamTypeAlias];
            int device = getDeviceForStream(streamType);
            if (((device & AudioSystem.DEVICE_OUT_ALL_A2DP) != 0 || (flags & 64) == 0) && this.mAppOps.noteOp(STEAM_VOLUME_OPS[streamTypeAlias], uid, callingPackage) == 0) {
                synchronized (this.mSafeMediaVolumeState) {
                    this.mPendingVolumeCommand = null;
                    oldIndex = streamState.getIndex(device);
                    index2 = rescaleIndex(index * 10, streamType, streamTypeAlias);
                    if (streamTypeAlias == 3 && (device & AudioSystem.DEVICE_OUT_ALL_A2DP) != 0 && (flags & 64) == 0) {
                        synchronized (this.mA2dpAvrcpLock) {
                            if (this.mA2dp != null && this.mAvrcpAbsVolSupported) {
                                this.mA2dp.setAvrcpAbsoluteVolume(index2 / 10);
                            }
                        }
                    }
                    if (this.mHdmiManager != null) {
                        synchronized (this.mHdmiManager) {
                            if (this.mHdmiTvClient != null && streamTypeAlias == 3 && (flags & 256) == 0 && oldIndex != index2) {
                                int maxIndex = getStreamMaxVolume(streamType);
                                synchronized (this.mHdmiTvClient) {
                                    if (this.mHdmiSystemAudioSupported) {
                                        this.mHdmiTvClient.setSystemAudioVolume((oldIndex + 5) / 10, (index2 + 5) / 10, maxIndex);
                                    }
                                }
                            }
                        }
                    }
                    flags2 = flags & -33;
                    if (streamTypeAlias == 3 && (this.mFixedVolumeDevices & device) != 0) {
                        flags2 |= 32;
                        if (index2 != 0) {
                            index2 = (this.mSafeMediaVolumeState.intValue() != 3 || (device & 12) == 0) ? streamState.getMaxIndex() : this.mSafeMediaVolumeIndex;
                        }
                    }
                    if (!checkSafeMediaVolume(streamTypeAlias, index2, device)) {
                        this.mVolumeController.postDisplaySafeVolumeWarning(flags2);
                        this.mPendingVolumeCommand = new StreamVolumeCommand(streamType, index2, flags2, device);
                    } else {
                        onSetStreamVolume(streamType, index2, flags2, device);
                        index2 = this.mStreamStates[streamType].getIndex(device);
                    }
                }
                sendVolumeUpdate(streamType, oldIndex, index2, flags2);
            }
        }
    }

    @Override // android.media.IAudioService
    public void forceVolumeControlStream(int streamType, IBinder cb) {
        synchronized (this.mForceControlStreamLock) {
            this.mVolumeControlStream = streamType;
            if (this.mVolumeControlStream != -1) {
                this.mForceControlStreamClient = new ForceControlStreamClient(cb);
            } else if (this.mForceControlStreamClient != null) {
                this.mForceControlStreamClient.release();
                this.mForceControlStreamClient = null;
            }
        }
    }

    /* access modifiers changed from: private */
    public class ForceControlStreamClient implements IBinder.DeathRecipient {
        private IBinder mCb;

        ForceControlStreamClient(IBinder cb) {
            if (cb != null) {
                try {
                    cb.linkToDeath(this, 0);
                } catch (RemoteException e) {
                    Log.w(AudioService.TAG, "ForceControlStreamClient() could not link to " + cb + " binder death");
                    cb = null;
                }
            }
            this.mCb = cb;
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            synchronized (AudioService.this.mForceControlStreamLock) {
                Log.w(AudioService.TAG, "SCO client died");
                if (AudioService.this.mForceControlStreamClient != this) {
                    Log.w(AudioService.TAG, "unregistered control stream client died");
                } else {
                    AudioService.this.mForceControlStreamClient = null;
                    AudioService.this.mVolumeControlStream = -1;
                }
            }
        }

        public void release() {
            if (this.mCb != null) {
                this.mCb.unlinkToDeath(this, 0);
                this.mCb = null;
            }
        }
    }

    private int findVolumeDelta(int direction, int volume) {
        int delta = 0;
        if (direction == 1) {
            if (volume == 100) {
                return 0;
            }
            delta = this.mMasterVolumeRamp[1];
            int i = this.mMasterVolumeRamp.length - 1;
            while (true) {
                if (i <= 1) {
                    break;
                } else if (volume >= this.mMasterVolumeRamp[i - 1]) {
                    delta = this.mMasterVolumeRamp[i];
                    break;
                } else {
                    i -= 2;
                }
            }
        } else if (direction == -1) {
            if (volume == 0) {
                return 0;
            }
            int length = this.mMasterVolumeRamp.length;
            delta = -this.mMasterVolumeRamp[length - 1];
            int i2 = 2;
            while (true) {
                if (i2 >= length) {
                    break;
                } else if (volume <= this.mMasterVolumeRamp[i2]) {
                    delta = -this.mMasterVolumeRamp[i2 - 1];
                    break;
                } else {
                    i2 += 2;
                }
            }
        }
        return delta;
    }

    private void sendBroadcastToAll(Intent intent) {
        long ident = Binder.clearCallingIdentity();
        try {
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendStickyBroadcastToAll(Intent intent) {
        long ident = Binder.clearCallingIdentity();
        try {
            this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private void sendVolumeUpdate(int streamType, int oldIndex, int index, int flags) {
        if (!isPlatformVoice() && streamType == 2) {
            streamType = 5;
        }
        if (this.mHdmiTvClient != null && streamType == 3) {
            synchronized (this.mHdmiTvClient) {
                if (this.mHdmiSystemAudioSupported && (flags & 256) == 0) {
                    flags &= -2;
                }
            }
        }
        this.mVolumeController.postVolumeChanged(streamType, flags);
        if ((flags & 32) == 0) {
            Intent intent = new Intent(AudioManager.VOLUME_CHANGED_ACTION);
            intent.putExtra(AudioManager.EXTRA_VOLUME_STREAM_TYPE, streamType);
            intent.putExtra(AudioManager.EXTRA_VOLUME_STREAM_VALUE, (index + 5) / 10);
            intent.putExtra(AudioManager.EXTRA_PREV_VOLUME_STREAM_VALUE, (oldIndex + 5) / 10);
            sendBroadcastToAll(intent);
        }
    }

    private void sendMasterVolumeUpdate(int flags, int oldVolume, int newVolume) {
        this.mVolumeController.postMasterVolumeChanged(flags);
        Intent intent = new Intent(AudioManager.MASTER_VOLUME_CHANGED_ACTION);
        intent.putExtra(AudioManager.EXTRA_PREV_MASTER_VOLUME_VALUE, oldVolume);
        intent.putExtra(AudioManager.EXTRA_MASTER_VOLUME_VALUE, newVolume);
        sendBroadcastToAll(intent);
    }

    private void sendMasterMuteUpdate(boolean muted, int flags) {
        this.mVolumeController.postMasterMuteChanged(flags);
        broadcastMasterMuteStatus(muted);
    }

    private void broadcastMasterMuteStatus(boolean muted) {
        Intent intent = new Intent(AudioManager.MASTER_MUTE_CHANGED_ACTION);
        intent.putExtra(AudioManager.EXTRA_MASTER_VOLUME_MUTED, muted);
        intent.addFlags(603979776);
        sendStickyBroadcastToAll(intent);
    }

    private void setStreamVolumeInt(int streamType, int index, int device, boolean force) {
        VolumeStreamState streamState = this.mStreamStates[streamType];
        if (streamState.setIndex(index, device) || force) {
            sendMsg(this.mAudioHandler, 0, 2, device, 0, streamState, 0);
        }
    }

    @Override // android.media.IAudioService
    public void setStreamSolo(int streamType, boolean state, IBinder cb) {
        if (!this.mUseFixedVolume) {
            for (int stream = 0; stream < this.mStreamStates.length; stream++) {
                if (isStreamAffectedByMute(stream) && stream != streamType) {
                    this.mStreamStates[stream].mute(cb, state);
                }
            }
        }
    }

    @Override // android.media.IAudioService
    public void setStreamMute(int streamType, boolean state, IBinder cb) {
        if (!this.mUseFixedVolume && isStreamAffectedByMute(streamType)) {
            if (this.mHdmiManager != null) {
                synchronized (this.mHdmiManager) {
                    if (streamType == 3) {
                        if (this.mHdmiTvClient != null) {
                            synchronized (this.mHdmiTvClient) {
                                if (this.mHdmiSystemAudioSupported) {
                                    this.mHdmiTvClient.setSystemAudioMute(state);
                                }
                            }
                        }
                    }
                }
            }
            this.mStreamStates[streamType].mute(cb, state);
        }
    }

    @Override // android.media.IAudioService
    public boolean isStreamMute(int streamType) {
        return this.mStreamStates[streamType].isMuted();
    }

    /* access modifiers changed from: private */
    public class RmtSbmxFullVolDeathHandler implements IBinder.DeathRecipient {
        private IBinder mICallback;

        RmtSbmxFullVolDeathHandler(IBinder cb) {
            this.mICallback = cb;
            try {
                cb.linkToDeath(this, 0);
            } catch (RemoteException e) {
                Log.e(AudioService.TAG, "can't link to death", e);
            }
        }

        /* access modifiers changed from: package-private */
        public boolean isHandlerFor(IBinder cb) {
            return this.mICallback.equals(cb);
        }

        /* access modifiers changed from: package-private */
        public void forget() {
            try {
                this.mICallback.unlinkToDeath(this, 0);
            } catch (NoSuchElementException e) {
                Log.e(AudioService.TAG, "error unlinking to death", e);
            }
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            Log.w(AudioService.TAG, "Recorder with remote submix at full volume died " + this.mICallback);
            AudioService.this.forceRemoteSubmixFullVolume(false, this.mICallback);
        }
    }

    private boolean discardRmtSbmxFullVolDeathHandlerFor(IBinder cb) {
        Iterator<RmtSbmxFullVolDeathHandler> it = this.mRmtSbmxFullVolDeathHandlers.iterator();
        while (it.hasNext()) {
            RmtSbmxFullVolDeathHandler handler = it.next();
            if (handler.isHandlerFor(cb)) {
                handler.forget();
                this.mRmtSbmxFullVolDeathHandlers.remove(handler);
                return true;
            }
        }
        return false;
    }

    private boolean hasRmtSbmxFullVolDeathHandlerFor(IBinder cb) {
        Iterator<RmtSbmxFullVolDeathHandler> it = this.mRmtSbmxFullVolDeathHandlers.iterator();
        while (it.hasNext()) {
            if (it.next().isHandlerFor(cb)) {
                return true;
            }
        }
        return false;
    }

    @Override // android.media.IAudioService
    public void forceRemoteSubmixFullVolume(boolean startForcing, IBinder cb) {
        if (cb != null) {
            if (this.mContext.checkCallingOrSelfPermission(Manifest.permission.CAPTURE_AUDIO_OUTPUT) != 0) {
                Log.w(TAG, "Trying to call forceRemoteSubmixFullVolume() without CAPTURE_AUDIO_OUTPUT");
                return;
            }
            synchronized (this.mRmtSbmxFullVolDeathHandlers) {
                boolean applyRequired = false;
                if (startForcing) {
                    if (!hasRmtSbmxFullVolDeathHandlerFor(cb)) {
                        this.mRmtSbmxFullVolDeathHandlers.add(new RmtSbmxFullVolDeathHandler(cb));
                        if (this.mRmtSbmxFullVolRefCount == 0) {
                            this.mFullVolumeDevices |= 32768;
                            this.mFixedVolumeDevices |= 32768;
                            applyRequired = true;
                        }
                        this.mRmtSbmxFullVolRefCount++;
                    }
                } else if (discardRmtSbmxFullVolDeathHandlerFor(cb) && this.mRmtSbmxFullVolRefCount > 0) {
                    this.mRmtSbmxFullVolRefCount--;
                    if (this.mRmtSbmxFullVolRefCount == 0) {
                        this.mFullVolumeDevices &= -32769;
                        this.mFixedVolumeDevices &= -32769;
                        applyRequired = true;
                    }
                }
                if (applyRequired) {
                    checkAllFixedVolumeDevices(3);
                    this.mStreamStates[3].applyAllVolumes();
                }
            }
        }
    }

    @Override // android.media.IAudioService
    public void setMasterMute(boolean state, int flags, String callingPackage, IBinder cb) {
        int i;
        if (!this.mUseFixedVolume && this.mAppOps.noteOp(33, Binder.getCallingUid(), callingPackage) == 0 && state != AudioSystem.getMasterMute()) {
            AudioSystem.setMasterMute(state);
            AudioHandler audioHandler = this.mAudioHandler;
            if (state) {
                i = 1;
            } else {
                i = 0;
            }
            sendMsg(audioHandler, 11, 0, i, UserHandle.getCallingUserId(), null, 500);
            sendMasterMuteUpdate(state, flags);
        }
    }

    @Override // android.media.IAudioService
    public boolean isMasterMute() {
        return AudioSystem.getMasterMute();
    }

    protected static int getMaxStreamVolume(int streamType) {
        return MAX_STREAM_VOLUME[streamType];
    }

    @Override // android.media.IAudioService
    public int getStreamVolume(int streamType) {
        ensureValidStreamType(streamType);
        int device = getDeviceForStream(streamType);
        int index = this.mStreamStates[streamType].getIndex(device);
        if (this.mStreamStates[streamType].isMuted()) {
            index = 0;
        }
        if (!(index == 0 || this.mStreamVolumeAlias[streamType] != 3 || (this.mFixedVolumeDevices & device) == 0)) {
            index = this.mStreamStates[streamType].getMaxIndex();
        }
        return (index + 5) / 10;
    }

    @Override // android.media.IAudioService
    public int getMasterVolume() {
        if (isMasterMute()) {
            return 0;
        }
        return getLastAudibleMasterVolume();
    }

    @Override // android.media.IAudioService
    public void setMasterVolume(int volume, int flags, String callingPackage) {
        if (!this.mUseFixedVolume && this.mAppOps.noteOp(33, Binder.getCallingUid(), callingPackage) == 0) {
            if (volume < 0) {
                volume = 0;
            } else if (volume > 100) {
                volume = 100;
            }
            doSetMasterVolume(((float) volume) / 100.0f, flags);
        }
    }

    private void doSetMasterVolume(float volume, int flags) {
        if (!AudioSystem.getMasterMute()) {
            int oldVolume = getMasterVolume();
            AudioSystem.setMasterVolume(volume);
            int newVolume = getMasterVolume();
            if (newVolume != oldVolume) {
                sendMsg(this.mAudioHandler, 2, 0, Math.round(1000.0f * volume), 0, null, 500);
            }
            sendMasterVolumeUpdate(flags, oldVolume, newVolume);
        }
    }

    @Override // android.media.IAudioService
    public int getStreamMaxVolume(int streamType) {
        ensureValidStreamType(streamType);
        return (this.mStreamStates[streamType].getMaxIndex() + 5) / 10;
    }

    @Override // android.media.IAudioService
    public int getMasterMaxVolume() {
        return 100;
    }

    @Override // android.media.IAudioService
    public int getLastAudibleStreamVolume(int streamType) {
        ensureValidStreamType(streamType);
        return (this.mStreamStates[streamType].getIndex(getDeviceForStream(streamType)) + 5) / 10;
    }

    @Override // android.media.IAudioService
    public int getLastAudibleMasterVolume() {
        return Math.round(AudioSystem.getMasterVolume() * 100.0f);
    }

    @Override // android.media.IAudioService
    public int getMasterStreamType() {
        return this.mStreamVolumeAlias[1];
    }

    @Override // android.media.IAudioService
    public void setMicrophoneMute(boolean on, String callingPackage) {
        if (this.mAppOps.noteOp(44, Binder.getCallingUid(), callingPackage) == 0 && checkAudioSettingsPermission("setMicrophoneMute()")) {
            AudioSystem.muteMicrophone(on);
            sendMsg(this.mAudioHandler, 23, 0, on ? 1 : 0, UserHandle.getCallingUserId(), null, 500);
        }
    }

    @Override // android.media.IAudioService
    public int getRingerMode() {
        int i;
        synchronized (this.mSettingsLock) {
            i = this.mRingerMode;
        }
        return i;
    }

    private void ensureValidRingerMode(int ringerMode) {
        if (!AudioManager.isValidRingerMode(ringerMode)) {
            throw new IllegalArgumentException("Bad ringer mode " + ringerMode);
        }
    }

    @Override // android.media.IAudioService
    public void setRingerMode(int ringerMode, boolean checkZen) {
        if (!this.mUseFixedVolume && !isPlatformTelevision()) {
            if (ringerMode == 1 && !this.mHasVibrator) {
                ringerMode = 0;
            }
            if (checkZen) {
                checkZen(ringerMode);
            }
            if (ringerMode != getRingerMode()) {
                setRingerModeInt(ringerMode, true);
                broadcastRingerMode(ringerMode);
            }
        }
    }

    private void checkZen(int ringerMode) {
        int zen = Settings.Global.getInt(this.mContentResolver, Settings.Global.ZEN_MODE, 0);
        if (ringerMode != 0 && zen != 0) {
            long ident = Binder.clearCallingIdentity();
            try {
                Settings.Global.putInt(this.mContentResolver, Settings.Global.ZEN_MODE, 0);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setRingerModeInt(int ringerMode, boolean persist) {
        synchronized (this.mSettingsLock) {
            this.mRingerMode = ringerMode;
        }
        for (int streamType = AudioSystem.getNumStreamTypes() - 1; streamType >= 0; streamType--) {
            if (isStreamMutedByRingerMode(streamType)) {
                if (!isStreamAffectedByRingerMode(streamType) || ringerMode == 2) {
                    if (isPlatformVoice() && this.mStreamVolumeAlias[streamType] == 2) {
                        synchronized (this.mStreamStates[streamType]) {
                            for (Map.Entry entry : this.mStreamStates[streamType].mIndex.entrySet()) {
                                if (((Integer) entry.getValue()).intValue() == 0) {
                                    entry.setValue(10);
                                }
                            }
                        }
                    }
                    this.mStreamStates[streamType].mute(null, false);
                    this.mRingerModeMutedStreams &= (1 << streamType) ^ -1;
                }
            } else if (isStreamAffectedByRingerMode(streamType) && ringerMode != 2) {
                this.mStreamStates[streamType].mute(null, true);
                this.mRingerModeMutedStreams |= 1 << streamType;
            }
        }
        if (persist) {
            sendMsg(this.mAudioHandler, 3, 0, 0, 0, null, 500);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void restoreMasterVolume() {
        if (this.mUseFixedVolume) {
            AudioSystem.setMasterVolume(1.0f);
        } else if (this.mUseMasterVolume) {
            float volume = Settings.System.getFloatForUser(this.mContentResolver, Settings.System.VOLUME_MASTER, -1.0f, -2);
            if (volume >= 0.0f) {
                AudioSystem.setMasterVolume(volume);
            }
        }
    }

    @Override // android.media.IAudioService
    public boolean shouldVibrate(int vibrateType) {
        boolean z = true;
        if (!this.mHasVibrator) {
            return false;
        }
        switch (getVibrateSetting(vibrateType)) {
            case 0:
            default:
                return false;
            case 1:
                if (getRingerMode() == 0) {
                    z = false;
                }
                return z;
            case 2:
                if (getRingerMode() != 1) {
                    z = false;
                }
                return z;
        }
    }

    @Override // android.media.IAudioService
    public int getVibrateSetting(int vibrateType) {
        if (!this.mHasVibrator) {
            return 0;
        }
        return (this.mVibrateSetting >> (vibrateType * 2)) & 3;
    }

    @Override // android.media.IAudioService
    public void setVibrateSetting(int vibrateType, int vibrateSetting) {
        if (this.mHasVibrator) {
            this.mVibrateSetting = getValueForVibrateSetting(this.mVibrateSetting, vibrateType, vibrateSetting);
            broadcastVibrateSetting(vibrateType);
        }
    }

    public static int getValueForVibrateSetting(int existingValue, int vibrateType, int vibrateSetting) {
        return (existingValue & ((3 << (vibrateType * 2)) ^ -1)) | ((vibrateSetting & 3) << (vibrateType * 2));
    }

    /* access modifiers changed from: private */
    public class SetModeDeathHandler implements IBinder.DeathRecipient {
        private IBinder mCb;
        private int mMode = 0;
        private int mPid;

        SetModeDeathHandler(IBinder cb, int pid) {
            this.mCb = cb;
            this.mPid = pid;
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            int newModeOwnerPid = 0;
            synchronized (AudioService.this.mSetModeDeathHandlers) {
                Log.w(AudioService.TAG, "setMode() client died");
                if (AudioService.this.mSetModeDeathHandlers.indexOf(this) < 0) {
                    Log.w(AudioService.TAG, "unregistered setMode() client died");
                } else {
                    newModeOwnerPid = AudioService.this.setModeInt(0, this.mCb, this.mPid);
                }
            }
            if (newModeOwnerPid != 0) {
                long ident = Binder.clearCallingIdentity();
                AudioService.this.disconnectBluetoothSco(newModeOwnerPid);
                Binder.restoreCallingIdentity(ident);
            }
        }

        public int getPid() {
            return this.mPid;
        }

        public void setMode(int mode) {
            this.mMode = mode;
        }

        public int getMode() {
            return this.mMode;
        }

        public IBinder getBinder() {
            return this.mCb;
        }
    }

    @Override // android.media.IAudioService
    public void setMode(int mode, IBinder cb) {
        int newModeOwnerPid;
        if (DEBUG_MODE) {
            Log.v(TAG, "setMode(mode=" + mode + ")");
        }
        if (checkAudioSettingsPermission("setMode()")) {
            if (mode == 2 && this.mContext.checkCallingOrSelfPermission(Manifest.permission.MODIFY_PHONE_STATE) != 0) {
                Log.w(TAG, "MODIFY_PHONE_STATE Permission Denial: setMode(MODE_IN_CALL) from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            } else if (mode >= -1 && mode < 4) {
                synchronized (this.mSetModeDeathHandlers) {
                    if (mode == -1) {
                        mode = this.mMode;
                    }
                    newModeOwnerPid = setModeInt(mode, cb, Binder.getCallingPid());
                }
                if (newModeOwnerPid != 0) {
                    disconnectBluetoothSco(newModeOwnerPid);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int setModeInt(int mode, IBinder cb, int pid) {
        int status;
        if (DEBUG_MODE) {
            Log.v(TAG, "setModeInt(mode=" + mode + ", pid=" + pid + ")");
        }
        int newModeOwnerPid = 0;
        if (cb == null) {
            Log.e(TAG, "setModeInt() called with null binder");
            return 0;
        }
        SetModeDeathHandler hdlr = null;
        Iterator iter = this.mSetModeDeathHandlers.iterator();
        while (true) {
            if (!iter.hasNext()) {
                break;
            }
            SetModeDeathHandler h = iter.next();
            if (h.getPid() == pid) {
                hdlr = h;
                iter.remove();
                hdlr.getBinder().unlinkToDeath(hdlr, 0);
                break;
            }
        }
        do {
            if (mode != 0) {
                if (hdlr == null) {
                    hdlr = new SetModeDeathHandler(cb, pid);
                }
                try {
                    cb.linkToDeath(hdlr, 0);
                } catch (RemoteException e) {
                    Log.w(TAG, "setMode() could not link to " + cb + " binder death");
                }
                this.mSetModeDeathHandlers.add(0, hdlr);
                hdlr.setMode(mode);
            } else if (!this.mSetModeDeathHandlers.isEmpty()) {
                hdlr = this.mSetModeDeathHandlers.get(0);
                cb = hdlr.getBinder();
                mode = hdlr.getMode();
                if (DEBUG_MODE) {
                    Log.w(TAG, " using mode=" + mode + " instead due to death hdlr at pid=" + hdlr.mPid);
                }
            }
            if (mode != this.mMode) {
                status = AudioSystem.setPhoneState(mode);
                if (status == 0) {
                    if (DEBUG_MODE) {
                        Log.v(TAG, " mode successfully set to " + mode);
                    }
                    this.mMode = mode;
                } else {
                    if (hdlr != null) {
                        this.mSetModeDeathHandlers.remove(hdlr);
                        cb.unlinkToDeath(hdlr, 0);
                    }
                    if (DEBUG_MODE) {
                        Log.w(TAG, " mode set to MODE_NORMAL after phoneState pb");
                    }
                    mode = 0;
                }
            } else {
                status = 0;
            }
            if (status == 0) {
                break;
            }
        } while (!this.mSetModeDeathHandlers.isEmpty());
        if (status == 0) {
            if (mode != 0) {
                if (this.mSetModeDeathHandlers.isEmpty()) {
                    Log.e(TAG, "setMode() different from MODE_NORMAL with empty mode client stack");
                } else {
                    newModeOwnerPid = this.mSetModeDeathHandlers.get(0).getPid();
                }
            }
            int streamType = getActiveStreamType(Integer.MIN_VALUE);
            int device = getDeviceForStream(streamType);
            setStreamVolumeInt(this.mStreamVolumeAlias[streamType], this.mStreamStates[this.mStreamVolumeAlias[streamType]].getIndex(device), device, true);
            updateStreamVolumeAlias(true);
        }
        return newModeOwnerPid;
    }

    @Override // android.media.IAudioService
    public int getMode() {
        return this.mMode;
    }

    class LoadSoundEffectReply {
        public int mStatus = 1;

        LoadSoundEffectReply() {
        }
    }

    private void loadTouchSoundAssetDefaults() {
        SOUND_EFFECT_FILES.add("Effect_Tick.ogg");
        for (int i = 0; i < 10; i++) {
            this.SOUND_EFFECT_FILES_MAP[i][0] = 0;
            this.SOUND_EFFECT_FILES_MAP[i][1] = -1;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void loadTouchSoundAssets() {
        XmlResourceParser parser = null;
        if (SOUND_EFFECT_FILES.isEmpty()) {
            loadTouchSoundAssetDefaults();
            try {
                XmlResourceParser parser2 = this.mContext.getResources().getXml(17891329);
                XmlUtils.beginDocument(parser2, TAG_AUDIO_ASSETS);
                boolean inTouchSoundsGroup = false;
                if (ASSET_FILE_VERSION.equals(parser2.getAttributeValue(null, "version"))) {
                    while (true) {
                        XmlUtils.nextElement(parser2);
                        String element = parser2.getName();
                        if (element != null) {
                            if (element.equals("group") && GROUP_TOUCH_SOUNDS.equals(parser2.getAttributeValue(null, "name"))) {
                                inTouchSoundsGroup = true;
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                    while (inTouchSoundsGroup) {
                        XmlUtils.nextElement(parser2);
                        String element2 = parser2.getName();
                        if (element2 == null || !element2.equals(TAG_ASSET)) {
                            break;
                        }
                        String id = parser2.getAttributeValue(null, "id");
                        String file = parser2.getAttributeValue(null, "file");
                        try {
                            int fx = AudioManager.class.getField(id).getInt(null);
                            int i = SOUND_EFFECT_FILES.indexOf(file);
                            if (i == -1) {
                                i = SOUND_EFFECT_FILES.size();
                                SOUND_EFFECT_FILES.add(file);
                            }
                            this.SOUND_EFFECT_FILES_MAP[fx][0] = i;
                        } catch (Exception e) {
                            Log.w(TAG, "Invalid touch sound ID: " + id);
                        }
                    }
                }
                if (parser2 != null) {
                    parser2.close();
                }
            } catch (Resources.NotFoundException e2) {
                Log.w(TAG, "audio assets file not found", e2);
                if (0 != 0) {
                    parser.close();
                }
            } catch (XmlPullParserException e3) {
                Log.w(TAG, "XML parser exception reading touch sound assets", e3);
                if (0 != 0) {
                    parser.close();
                }
            } catch (IOException e4) {
                Log.w(TAG, "I/O exception reading touch sound assets", e4);
                if (0 != 0) {
                    parser.close();
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    parser.close();
                }
                throw th;
            }
        }
    }

    @Override // android.media.IAudioService
    public void playSoundEffect(int effectType) {
        playSoundEffectVolume(effectType, -1.0f);
    }

    @Override // android.media.IAudioService
    public void playSoundEffectVolume(int effectType, float volume) {
        if (effectType >= 10 || effectType < 0) {
            Log.w(TAG, "AudioService effectType value " + effectType + " out of range");
        } else {
            sendMsg(this.mAudioHandler, 5, 2, effectType, (int) (1000.0f * volume), null, 0);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0031, code lost:
        if (r5.mStatus != 0) goto L_0x0038;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:?, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:?, code lost:
        return false;
     */
    @Override // android.media.IAudioService
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean loadSoundEffects() {
        /*
            r12 = this;
            r10 = 1
            r11 = 0
            r7 = 3
            android.media.AudioService$LoadSoundEffectReply r5 = new android.media.AudioService$LoadSoundEffectReply
            r5.<init>()
            monitor-enter(r5)
            android.media.AudioService$AudioHandler r0 = r12.mAudioHandler     // Catch:{ all -> 0x0035 }
            r1 = 7
            r2 = 2
            r3 = 0
            r4 = 0
            r6 = 0
            sendMsg(r0, r1, r2, r3, r4, r5, r6)     // Catch:{ all -> 0x0035 }
            r8 = r7
        L_0x0014:
            int r0 = r5.mStatus     // Catch:{ all -> 0x003a }
            if (r0 != r10) goto L_0x002d
            int r7 = r8 + -1
            if (r8 <= 0) goto L_0x002e
            r0 = 5000(0x1388, double:2.4703E-320)
            r5.wait(r0)     // Catch:{ InterruptedException -> 0x0023 }
            r8 = r7
            goto L_0x0014
        L_0x0023:
            r9 = move-exception
            java.lang.String r0 = "AudioService"
            java.lang.String r1 = "loadSoundEffects Interrupted while waiting sound pool loaded."
            android.util.Log.w(r0, r1)
            r8 = r7
            goto L_0x0014
        L_0x002d:
            r7 = r8
        L_0x002e:
            monitor-exit(r5)
            int r0 = r5.mStatus
            if (r0 != 0) goto L_0x0038
            r0 = r10
        L_0x0034:
            return r0
        L_0x0035:
            r0 = move-exception
        L_0x0036:
            monitor-exit(r5)
            throw r0
        L_0x0038:
            r0 = r11
            goto L_0x0034
        L_0x003a:
            r0 = move-exception
            r7 = r8
            goto L_0x0036
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.AudioService.loadSoundEffects():boolean");
    }

    @Override // android.media.IAudioService
    public void unloadSoundEffects() {
        sendMsg(this.mAudioHandler, 20, 2, 0, 0, null, 0);
    }

    /* access modifiers changed from: package-private */
    public class SoundPoolListenerThread extends Thread {
        public SoundPoolListenerThread() {
            super("SoundPoolListenerThread");
        }

        public void run() {
            Looper.prepare();
            AudioService.this.mSoundPoolLooper = Looper.myLooper();
            synchronized (AudioService.this.mSoundEffectsLock) {
                if (AudioService.this.mSoundPool != null) {
                    AudioService.this.mSoundPoolCallBack = new SoundPoolCallback();
                    AudioService.this.mSoundPool.setOnLoadCompleteListener(AudioService.this.mSoundPoolCallBack);
                }
                AudioService.this.mSoundEffectsLock.notify();
            }
            Looper.loop();
        }
    }

    /* access modifiers changed from: private */
    public final class SoundPoolCallback implements SoundPool.OnLoadCompleteListener {
        List<Integer> mSamples;
        int mStatus;

        private SoundPoolCallback() {
            this.mStatus = 1;
            this.mSamples = new ArrayList();
        }

        public int status() {
            return this.mStatus;
        }

        public void setSamples(int[] samples) {
            for (int i = 0; i < samples.length; i++) {
                if (samples[i] > 0) {
                    this.mSamples.add(Integer.valueOf(samples[i]));
                }
            }
        }

        @Override // android.media.SoundPool.OnLoadCompleteListener
        public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
            synchronized (AudioService.this.mSoundEffectsLock) {
                int i = this.mSamples.indexOf(Integer.valueOf(sampleId));
                if (i >= 0) {
                    this.mSamples.remove(i);
                }
                if (status != 0 || this.mSamples.isEmpty()) {
                    this.mStatus = status;
                    AudioService.this.mSoundEffectsLock.notify();
                }
            }
        }
    }

    @Override // android.media.IAudioService
    public void reloadAudioSettings() {
        readAudioSettings(false);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void readAudioSettings(boolean userSwitch) {
        readPersistedSettings();
        int numStreamTypes = AudioSystem.getNumStreamTypes();
        for (int streamType = 0; streamType < numStreamTypes; streamType++) {
            VolumeStreamState streamState = this.mStreamStates[streamType];
            if (!userSwitch || this.mStreamVolumeAlias[streamType] != 3) {
                synchronized (streamState) {
                    streamState.readSettings();
                    if (streamState.isMuted() && ((!isStreamAffectedByMute(streamType) && !isStreamMutedByRingerMode(streamType)) || this.mUseFixedVolume)) {
                        int size = streamState.mDeathHandlers.size();
                        for (int i = 0; i < size; i++) {
                            ((VolumeStreamState.VolumeDeathHandler) streamState.mDeathHandlers.get(i)).mMuteCount = 1;
                            ((VolumeStreamState.VolumeDeathHandler) streamState.mDeathHandlers.get(i)).mute(false);
                        }
                    }
                }
            }
        }
        setRingerModeInt(getRingerMode(), false);
        checkAllFixedVolumeDevices();
        checkAllAliasStreamVolumes();
        synchronized (this.mSafeMediaVolumeState) {
            this.mMusicActiveMs = MathUtils.constrain(Settings.Secure.getIntForUser(this.mContentResolver, Settings.Secure.UNSAFE_VOLUME_MUSIC_ACTIVE_MS, 0, -2), 0, (int) UNSAFE_VOLUME_MUSIC_ACTIVE_MS_MAX);
            if (this.mSafeMediaVolumeState.intValue() == 3) {
                enforceSafeMediaVolume();
            }
        }
    }

    @Override // android.media.IAudioService
    public void setSpeakerphoneOn(boolean on) {
        if (checkAudioSettingsPermission("setSpeakerphoneOn()")) {
            if (on) {
                if (this.mForcedUseForComm == 3) {
                    sendMsg(this.mAudioHandler, 8, 2, 2, 0, null, 0);
                }
                this.mForcedUseForComm = 1;
            } else if (this.mForcedUseForComm == 1) {
                this.mForcedUseForComm = 0;
            }
            sendMsg(this.mAudioHandler, 8, 2, 0, this.mForcedUseForComm, null, 0);
        }
    }

    @Override // android.media.IAudioService
    public boolean isSpeakerphoneOn() {
        return this.mForcedUseForComm == 1;
    }

    @Override // android.media.IAudioService
    public void setBluetoothScoOn(boolean on) {
        if (checkAudioSettingsPermission("setBluetoothScoOn()")) {
            if (on) {
                this.mForcedUseForComm = 3;
            } else if (this.mForcedUseForComm == 3) {
                this.mForcedUseForComm = 0;
            }
            sendMsg(this.mAudioHandler, 8, 2, 0, this.mForcedUseForComm, null, 0);
            sendMsg(this.mAudioHandler, 8, 2, 2, this.mForcedUseForComm, null, 0);
        }
    }

    @Override // android.media.IAudioService
    public boolean isBluetoothScoOn() {
        return this.mForcedUseForComm == 3;
    }

    @Override // android.media.IAudioService
    public void setBluetoothA2dpOn(boolean on) {
        int i = 0;
        synchronized (this.mBluetoothA2dpEnabledLock) {
            this.mBluetoothA2dpEnabled = on;
            AudioHandler audioHandler = this.mAudioHandler;
            if (!this.mBluetoothA2dpEnabled) {
                i = 10;
            }
            sendMsg(audioHandler, 13, 2, 1, i, null, 0);
        }
    }

    @Override // android.media.IAudioService
    public boolean isBluetoothA2dpOn() {
        boolean z;
        synchronized (this.mBluetoothA2dpEnabledLock) {
            z = this.mBluetoothA2dpEnabled;
        }
        return z;
    }

    @Override // android.media.IAudioService
    public void startBluetoothSco(IBinder cb, int targetSdkVersion) {
        startBluetoothScoInt(cb, targetSdkVersion < 18 ? 0 : -1);
    }

    @Override // android.media.IAudioService
    public void startBluetoothScoVirtualCall(IBinder cb) {
        startBluetoothScoInt(cb, 0);
    }

    /* access modifiers changed from: package-private */
    public void startBluetoothScoInt(IBinder cb, int scoAudioMode) {
        if (checkAudioSettingsPermission("startBluetoothSco()") && this.mSystemReady) {
            ScoClient client = getScoClient(cb, true);
            long ident = Binder.clearCallingIdentity();
            client.incCount(scoAudioMode);
            Binder.restoreCallingIdentity(ident);
        }
    }

    @Override // android.media.IAudioService
    public void stopBluetoothSco(IBinder cb) {
        if (checkAudioSettingsPermission("stopBluetoothSco()") && this.mSystemReady) {
            ScoClient client = getScoClient(cb, false);
            long ident = Binder.clearCallingIdentity();
            if (client != null) {
                client.decCount();
            }
            Binder.restoreCallingIdentity(ident);
        }
    }

    /* access modifiers changed from: private */
    public class ScoClient implements IBinder.DeathRecipient {
        private IBinder mCb;
        private int mCreatorPid = Binder.getCallingPid();
        private int mStartcount = 0;

        ScoClient(IBinder cb) {
            this.mCb = cb;
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            synchronized (AudioService.this.mScoClients) {
                Log.w(AudioService.TAG, "SCO client died");
                if (AudioService.this.mScoClients.indexOf(this) < 0) {
                    Log.w(AudioService.TAG, "unregistered SCO client died");
                } else {
                    clearCount(true);
                    AudioService.this.mScoClients.remove(this);
                }
            }
        }

        public void incCount(int scoAudioMode) {
            synchronized (AudioService.this.mScoClients) {
                requestScoState(12, scoAudioMode);
                if (this.mStartcount == 0) {
                    try {
                        this.mCb.linkToDeath(this, 0);
                    } catch (RemoteException e) {
                        Log.w(AudioService.TAG, "ScoClient  incCount() could not link to " + this.mCb + " binder death");
                    }
                }
                this.mStartcount++;
            }
        }

        public void decCount() {
            synchronized (AudioService.this.mScoClients) {
                if (this.mStartcount == 0) {
                    Log.w(AudioService.TAG, "ScoClient.decCount() already 0");
                } else {
                    this.mStartcount--;
                    if (this.mStartcount == 0) {
                        try {
                            this.mCb.unlinkToDeath(this, 0);
                        } catch (NoSuchElementException e) {
                            Log.w(AudioService.TAG, "decCount() going to 0 but not registered to binder");
                        }
                    }
                    requestScoState(10, 0);
                }
            }
        }

        public void clearCount(boolean stopSco) {
            synchronized (AudioService.this.mScoClients) {
                if (this.mStartcount != 0) {
                    try {
                        this.mCb.unlinkToDeath(this, 0);
                    } catch (NoSuchElementException e) {
                        Log.w(AudioService.TAG, "clearCount() mStartcount: " + this.mStartcount + " != 0 but not registered to binder");
                    }
                }
                this.mStartcount = 0;
                if (stopSco) {
                    requestScoState(10, 0);
                }
            }
        }

        public int getCount() {
            return this.mStartcount;
        }

        public IBinder getBinder() {
            return this.mCb;
        }

        public int getPid() {
            return this.mCreatorPid;
        }

        public int totalCount() {
            int count;
            synchronized (AudioService.this.mScoClients) {
                count = 0;
                int size = AudioService.this.mScoClients.size();
                for (int i = 0; i < size; i++) {
                    count += ((ScoClient) AudioService.this.mScoClients.get(i)).getCount();
                }
            }
            return count;
        }

        private void requestScoState(int state, int scoAudioMode) {
            AudioService.this.checkScoAudioState();
            if (totalCount() != 0) {
                return;
            }
            if (state == 12) {
                AudioService.this.broadcastScoConnectionState(2);
                synchronized (AudioService.this.mSetModeDeathHandlers) {
                    if ((!AudioService.this.mSetModeDeathHandlers.isEmpty() && ((SetModeDeathHandler) AudioService.this.mSetModeDeathHandlers.get(0)).getPid() != this.mCreatorPid) || (AudioService.this.mScoAudioState != 0 && AudioService.this.mScoAudioState != 5)) {
                        AudioService.this.broadcastScoConnectionState(0);
                    } else if (AudioService.this.mScoAudioState == 0) {
                        AudioService.this.mScoAudioMode = scoAudioMode;
                        if (scoAudioMode == -1) {
                            AudioService.this.mScoAudioMode = new Integer(Settings.Global.getInt(AudioService.this.mContentResolver, "bluetooth_sco_channel_" + AudioService.this.mBluetoothHeadsetDevice.getAddress(), 0)).intValue();
                            if (AudioService.this.mScoAudioMode > 2 || AudioService.this.mScoAudioMode < 0) {
                                AudioService.this.mScoAudioMode = 0;
                            }
                        }
                        if (AudioService.this.mBluetoothHeadset != null && AudioService.this.mBluetoothHeadsetDevice != null) {
                            boolean status = false;
                            if (AudioService.this.mScoAudioMode == 1) {
                                status = AudioService.this.mBluetoothHeadset.connectAudio();
                            } else if (AudioService.this.mScoAudioMode == 0) {
                                status = AudioService.this.mBluetoothHeadset.startScoUsingVirtualVoiceCall(AudioService.this.mBluetoothHeadsetDevice);
                            } else if (AudioService.this.mScoAudioMode == 2) {
                                status = AudioService.this.mBluetoothHeadset.startVoiceRecognition(AudioService.this.mBluetoothHeadsetDevice);
                            }
                            if (status) {
                                AudioService.this.mScoAudioState = 3;
                            } else {
                                AudioService.this.broadcastScoConnectionState(0);
                            }
                        } else if (AudioService.this.getBluetoothHeadset()) {
                            AudioService.this.mScoAudioState = 1;
                        }
                    } else {
                        AudioService.this.mScoAudioState = 3;
                        AudioService.this.broadcastScoConnectionState(1);
                    }
                }
            } else if (state != 10) {
            } else {
                if (AudioService.this.mScoAudioState != 3 && AudioService.this.mScoAudioState != 1) {
                    return;
                }
                if (AudioService.this.mScoAudioState != 3) {
                    AudioService.this.mScoAudioState = 0;
                    AudioService.this.broadcastScoConnectionState(0);
                } else if (AudioService.this.mBluetoothHeadset != null && AudioService.this.mBluetoothHeadsetDevice != null) {
                    boolean status2 = false;
                    if (AudioService.this.mScoAudioMode == 1) {
                        status2 = AudioService.this.mBluetoothHeadset.disconnectAudio();
                    } else if (AudioService.this.mScoAudioMode == 0) {
                        status2 = AudioService.this.mBluetoothHeadset.stopScoUsingVirtualVoiceCall(AudioService.this.mBluetoothHeadsetDevice);
                    } else if (AudioService.this.mScoAudioMode == 2) {
                        status2 = AudioService.this.mBluetoothHeadset.stopVoiceRecognition(AudioService.this.mBluetoothHeadsetDevice);
                    }
                    if (!status2) {
                        AudioService.this.mScoAudioState = 0;
                        AudioService.this.broadcastScoConnectionState(0);
                    }
                } else if (AudioService.this.getBluetoothHeadset()) {
                    AudioService.this.mScoAudioState = 5;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkScoAudioState() {
        if (this.mBluetoothHeadset != null && this.mBluetoothHeadsetDevice != null && this.mScoAudioState == 0 && this.mBluetoothHeadset.getAudioState(this.mBluetoothHeadsetDevice) != 10) {
            this.mScoAudioState = 2;
        }
    }

    private ScoClient getScoClient(IBinder cb, boolean create) {
        Throwable th;
        ScoClient client;
        synchronized (this.mScoClients) {
            try {
                int size = this.mScoClients.size();
                int i = 0;
                ScoClient client2 = null;
                while (i < size) {
                    try {
                        ScoClient client3 = this.mScoClients.get(i);
                        if (client3.getBinder() == cb) {
                            return client3;
                        }
                        i++;
                        client2 = client3;
                    } catch (Throwable th2) {
                        th = th2;
                        throw th;
                    }
                }
                if (create) {
                    client = new ScoClient(cb);
                    this.mScoClients.add(client);
                } else {
                    client = client2;
                }
                return client;
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
    }

    public void clearAllScoClients(int exceptPid, boolean stopSco) {
        synchronized (this.mScoClients) {
            ScoClient savedClient = null;
            int size = this.mScoClients.size();
            for (int i = 0; i < size; i++) {
                ScoClient cl = this.mScoClients.get(i);
                if (cl.getPid() != exceptPid) {
                    cl.clearCount(stopSco);
                } else {
                    savedClient = cl;
                }
            }
            this.mScoClients.clear();
            if (savedClient != null) {
                this.mScoClients.add(savedClient);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean getBluetoothHeadset() {
        int i;
        boolean result = false;
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null) {
            result = adapter.getProfileProxy(this.mContext, this.mBluetoothProfileServiceListener, 1);
        }
        AudioHandler audioHandler = this.mAudioHandler;
        if (result) {
            i = 3000;
        } else {
            i = 0;
        }
        sendMsg(audioHandler, 9, 0, 0, 0, null, i);
        return result;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void disconnectBluetoothSco(int exceptPid) {
        synchronized (this.mScoClients) {
            checkScoAudioState();
            if (this.mScoAudioState != 2 && this.mScoAudioState != 4) {
                clearAllScoClients(exceptPid, true);
            } else if (this.mBluetoothHeadsetDevice != null) {
                if (this.mBluetoothHeadset != null) {
                    if (!this.mBluetoothHeadset.stopVoiceRecognition(this.mBluetoothHeadsetDevice)) {
                        sendMsg(this.mAudioHandler, 9, 0, 0, 0, null, 0);
                    }
                } else if (this.mScoAudioState == 2 && getBluetoothHeadset()) {
                    this.mScoAudioState = 4;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resetBluetoothSco() {
        synchronized (this.mScoClients) {
            clearAllScoClients(0, false);
            this.mScoAudioState = 0;
            broadcastScoConnectionState(0);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void broadcastScoConnectionState(int state) {
        sendMsg(this.mAudioHandler, 19, 2, state, 0, null, 0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onBroadcastScoConnectionState(int state) {
        if (state != this.mScoConnectionState) {
            Intent newIntent = new Intent(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED);
            newIntent.putExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, state);
            newIntent.putExtra(AudioManager.EXTRA_SCO_AUDIO_PREVIOUS_STATE, this.mScoConnectionState);
            sendStickyBroadcastToAll(newIntent);
            this.mScoConnectionState = state;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onCheckMusicActive() {
        synchronized (this.mSafeMediaVolumeState) {
            if (this.mSafeMediaVolumeState.intValue() == 2) {
                int device = getDeviceForStream(3);
                if ((device & 12) != 0) {
                    sendMsg(this.mAudioHandler, 14, 0, 0, 0, null, MUSIC_ACTIVE_POLL_PERIOD_MS);
                    int index = this.mStreamStates[3].getIndex(device);
                    if (AudioSystem.isStreamActive(3, 0) && index > this.mSafeMediaVolumeIndex) {
                        this.mMusicActiveMs += MUSIC_ACTIVE_POLL_PERIOD_MS;
                        if (this.mMusicActiveMs > UNSAFE_VOLUME_MUSIC_ACTIVE_MS_MAX) {
                            setSafeMediaVolumeEnabled(true);
                            this.mMusicActiveMs = 0;
                        }
                        saveMusicActiveMs();
                    }
                }
            }
        }
    }

    private void saveMusicActiveMs() {
        this.mAudioHandler.obtainMessage(22, this.mMusicActiveMs, 0).sendToTarget();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onConfigureSafeVolume(boolean force) {
        int persistedState;
        boolean safeMediaVolumeEnabled = false;
        synchronized (this.mSafeMediaVolumeState) {
            int mcc = this.mContext.getResources().getConfiguration().mcc;
            if (this.mMcc != mcc || (this.mMcc == 0 && force)) {
                this.mSafeMediaVolumeIndex = this.mContext.getResources().getInteger(17694831) * 10;
                if (SystemProperties.getBoolean("audio.safemedia.force", false) || this.mContext.getResources().getBoolean(17956961)) {
                    safeMediaVolumeEnabled = true;
                }
                if (safeMediaVolumeEnabled) {
                    persistedState = 3;
                    if (this.mSafeMediaVolumeState.intValue() != 2) {
                        if (this.mMusicActiveMs == 0) {
                            this.mSafeMediaVolumeState = 3;
                            enforceSafeMediaVolume();
                        } else {
                            this.mSafeMediaVolumeState = 2;
                        }
                    }
                } else {
                    persistedState = 1;
                    this.mSafeMediaVolumeState = 1;
                }
                this.mMcc = mcc;
                sendMsg(this.mAudioHandler, 18, 2, persistedState, 0, null, 0);
            }
        }
    }

    private int checkForRingerModeChange(int oldIndex, int direction, int step) {
        int result = 1;
        int ringerMode = getRingerMode();
        switch (ringerMode) {
            case 0:
                if (direction == 1) {
                    result = 1 | 128;
                }
                result &= -2;
                break;
            case 1:
                if (this.mHasVibrator) {
                    if (direction != -1 && direction == 1) {
                        ringerMode = 2;
                    }
                    result = 1 & -2;
                    break;
                } else {
                    Log.e(TAG, "checkForRingerModeChange() current ringer mode is vibratebut no vibrator is present");
                    break;
                }
            case 2:
                if (direction == -1) {
                    if (this.mHasVibrator) {
                        if (step <= oldIndex && oldIndex < step * 2) {
                            ringerMode = 1;
                            break;
                        }
                    } else if (oldIndex < step) {
                    }
                }
                break;
            default:
                Log.e(TAG, "checkForRingerModeChange() wrong ringer mode: " + ringerMode);
                break;
        }
        setRingerMode(ringerMode, false);
        this.mPrevVolDirection = direction;
        return result;
    }

    @Override // android.media.IAudioService
    public boolean isStreamAffectedByRingerMode(int streamType) {
        return (this.mRingerModeAffectedStreams & (1 << streamType)) != 0;
    }

    private boolean isStreamMutedByRingerMode(int streamType) {
        return (this.mRingerModeMutedStreams & (1 << streamType)) != 0;
    }

    /* access modifiers changed from: package-private */
    public boolean updateRingerModeAffectedStreams() {
        int ringerModeAffectedStreams;
        int ringerModeAffectedStreams2;
        int ringerModeAffectedStreams3;
        int ringerModeAffectedStreams4 = Settings.System.getIntForUser(this.mContentResolver, Settings.System.MODE_RINGER_STREAMS_AFFECTED, 166, -2) | 38;
        switch (this.mPlatformType) {
            case 2:
                ringerModeAffectedStreams = 0;
                break;
            default:
                ringerModeAffectedStreams = ringerModeAffectedStreams4 & -9;
                break;
        }
        synchronized (this.mCameraSoundForced) {
            if (this.mCameraSoundForced.booleanValue()) {
                ringerModeAffectedStreams2 = ringerModeAffectedStreams & -129;
            } else {
                ringerModeAffectedStreams2 = ringerModeAffectedStreams | 128;
            }
        }
        if (this.mStreamVolumeAlias[8] == 2) {
            ringerModeAffectedStreams3 = ringerModeAffectedStreams2 | 256;
        } else {
            ringerModeAffectedStreams3 = ringerModeAffectedStreams2 & -257;
        }
        if (ringerModeAffectedStreams3 == this.mRingerModeAffectedStreams) {
            return false;
        }
        Settings.System.putIntForUser(this.mContentResolver, Settings.System.MODE_RINGER_STREAMS_AFFECTED, ringerModeAffectedStreams3, -2);
        this.mRingerModeAffectedStreams = ringerModeAffectedStreams3;
        return true;
    }

    public boolean isStreamAffectedByMute(int streamType) {
        return (this.mMuteAffectedStreams & (1 << streamType)) != 0;
    }

    private void ensureValidDirection(int direction) {
        if (direction < -1 || direction > 1) {
            throw new IllegalArgumentException("Bad direction " + direction);
        }
    }

    private void ensureValidSteps(int steps) {
        if (Math.abs(steps) > 4) {
            throw new IllegalArgumentException("Bad volume adjust steps " + steps);
        }
    }

    private void ensureValidStreamType(int streamType) {
        if (streamType < 0 || streamType >= this.mStreamStates.length) {
            throw new IllegalArgumentException("Bad stream type " + streamType);
        }
    }

    private boolean isInCommunication() {
        return ((TelecomManager) this.mContext.getSystemService(Context.TELECOM_SERVICE)).isInCall() || getMode() == 3;
    }

    private boolean isAfMusicActiveRecently(int delay_ms) {
        return AudioSystem.isStreamActive(3, delay_ms) || AudioSystem.isStreamActiveRemotely(3, delay_ms);
    }

    private int getActiveStreamType(int suggestedStreamType) {
        switch (this.mPlatformType) {
            case 1:
                if (isInCommunication()) {
                    if (AudioSystem.getForceUse(0) == 3) {
                        return 6;
                    }
                    return 0;
                } else if (suggestedStreamType == Integer.MIN_VALUE) {
                    if (isAfMusicActiveRecently(StreamOverride.sDelayMs)) {
                        if (DEBUG_VOL) {
                            Log.v(TAG, "getActiveStreamType: Forcing STREAM_MUSIC stream active");
                        }
                        return 3;
                    }
                    if (DEBUG_VOL) {
                        Log.v(TAG, "getActiveStreamType: Forcing STREAM_RING b/c default");
                    }
                    return 2;
                } else if (isAfMusicActiveRecently(0)) {
                    if (DEBUG_VOL) {
                        Log.v(TAG, "getActiveStreamType: Forcing STREAM_MUSIC stream active");
                    }
                    return 3;
                }
                break;
            case 2:
                if (suggestedStreamType == Integer.MIN_VALUE) {
                    return 3;
                }
                break;
            default:
                if (isInCommunication()) {
                    if (AudioSystem.getForceUse(0) == 3) {
                        if (DEBUG_VOL) {
                            Log.v(TAG, "getActiveStreamType: Forcing STREAM_BLUETOOTH_SCO");
                        }
                        return 6;
                    }
                    if (DEBUG_VOL) {
                        Log.v(TAG, "getActiveStreamType: Forcing STREAM_VOICE_CALL");
                    }
                    return 0;
                } else if (AudioSystem.isStreamActive(5, StreamOverride.sDelayMs) || AudioSystem.isStreamActive(2, StreamOverride.sDelayMs)) {
                    if (DEBUG_VOL) {
                        Log.v(TAG, "getActiveStreamType: Forcing STREAM_NOTIFICATION");
                    }
                    return 5;
                } else if (suggestedStreamType == Integer.MIN_VALUE) {
                    if (isAfMusicActiveRecently(StreamOverride.sDelayMs)) {
                        if (DEBUG_VOL) {
                            Log.v(TAG, "getActiveStreamType: forcing STREAM_MUSIC");
                        }
                        return 3;
                    }
                    if (DEBUG_VOL) {
                        Log.v(TAG, "getActiveStreamType: using STREAM_NOTIFICATION as default");
                    }
                    return 5;
                }
                break;
        }
        if (!DEBUG_VOL) {
            return suggestedStreamType;
        }
        Log.v(TAG, "getActiveStreamType: Returning suggested type " + suggestedStreamType);
        return suggestedStreamType;
    }

    private void broadcastRingerMode(int ringerMode) {
        Intent broadcast = new Intent(AudioManager.RINGER_MODE_CHANGED_ACTION);
        broadcast.putExtra(AudioManager.EXTRA_RINGER_MODE, ringerMode);
        broadcast.addFlags(603979776);
        sendStickyBroadcastToAll(broadcast);
    }

    private void broadcastVibrateSetting(int vibrateType) {
        if (ActivityManagerNative.isSystemReady()) {
            Intent broadcast = new Intent(AudioManager.VIBRATE_SETTING_CHANGED_ACTION);
            broadcast.putExtra(AudioManager.EXTRA_VIBRATE_TYPE, vibrateType);
            broadcast.putExtra(AudioManager.EXTRA_VIBRATE_SETTING, getVibrateSetting(vibrateType));
            sendBroadcastToAll(broadcast);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void queueMsgUnderWakeLock(Handler handler, int msg, int arg1, int arg2, Object obj, int delay) {
        long ident = Binder.clearCallingIdentity();
        this.mAudioEventWakeLock.acquire();
        Binder.restoreCallingIdentity(ident);
        sendMsg(handler, msg, 2, arg1, arg2, obj, delay);
    }

    /* access modifiers changed from: private */
    public static void sendMsg(Handler handler, int msg, int existingMsgPolicy, int arg1, int arg2, Object obj, int delay) {
        if (existingMsgPolicy == 0) {
            handler.removeMessages(msg);
        } else if (existingMsgPolicy == 1 && handler.hasMessages(msg)) {
            return;
        }
        handler.sendMessageDelayed(handler.obtainMessage(msg, arg1, arg2, obj), (long) delay);
    }

    /* access modifiers changed from: package-private */
    public boolean checkAudioSettingsPermission(String method) {
        if (this.mContext.checkCallingOrSelfPermission(Manifest.permission.MODIFY_AUDIO_SETTINGS) == 0) {
            return true;
        }
        Log.w(TAG, "Audio Settings Permission Denial: " + method + " from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getDeviceForStream(int stream) {
        int device = AudioSystem.getDevicesForStream(stream);
        if (((device - 1) & device) == 0) {
            return device;
        }
        if ((device & 2) != 0) {
            return 2;
        }
        if ((262144 & device) != 0) {
            return 262144;
        }
        if ((524288 & device) != 0) {
            return 524288;
        }
        if ((2097152 & device) != 0) {
            return 2097152;
        }
        return device & AudioSystem.DEVICE_OUT_ALL_A2DP;
    }

    @Override // android.media.IAudioService
    public void setWiredDeviceConnectionState(int device, int state, String name) {
        synchronized (this.mConnectedDevices) {
            queueMsgUnderWakeLock(this.mAudioHandler, 100, device, state, name, checkSendBecomingNoisyIntent(device, state));
        }
    }

    @Override // android.media.IAudioService
    public int setBluetoothA2dpDeviceConnectionState(BluetoothDevice device, int state, int profile) {
        int delay;
        int i = 0;
        if (profile == 2 || profile == 10) {
            synchronized (this.mConnectedDevices) {
                if (profile == 2) {
                    if (state == 2) {
                        i = 1;
                    }
                    delay = checkSendBecomingNoisyIntent(128, i);
                } else {
                    delay = 0;
                }
                queueMsgUnderWakeLock(this.mAudioHandler, profile == 2 ? 102 : 101, state, 0, device, delay);
            }
            return delay;
        }
        throw new IllegalArgumentException("invalid profile " + profile);
    }

    public class VolumeStreamState {
        private ArrayList<VolumeDeathHandler> mDeathHandlers;
        private final ConcurrentHashMap<Integer, Integer> mIndex;
        private int mIndexMax;
        private final int mStreamType;
        private String mVolumeIndexSettingName;

        private VolumeStreamState(String settingName, int streamType) {
            this.mIndex = new ConcurrentHashMap<>(8, 0.75f, 4);
            this.mVolumeIndexSettingName = settingName;
            this.mStreamType = streamType;
            this.mIndexMax = AudioService.MAX_STREAM_VOLUME[streamType];
            AudioSystem.initStreamVolume(streamType, 0, this.mIndexMax);
            this.mIndexMax *= 10;
            this.mDeathHandlers = new ArrayList<>();
            readSettings();
        }

        public String getSettingNameForDevice(int device) {
            String name = this.mVolumeIndexSettingName;
            String suffix = AudioSystem.getOutputDeviceName(device);
            return suffix.isEmpty() ? name : name + "_" + suffix;
        }

        public void readSettings() {
            int defaultIndex;
            synchronized (VolumeStreamState.class) {
                if (AudioService.this.mUseFixedVolume) {
                    this.mIndex.put(1073741824, Integer.valueOf(this.mIndexMax));
                } else if (this.mStreamType == 1 || this.mStreamType == 7) {
                    int index = AudioManager.DEFAULT_STREAM_VOLUME[this.mStreamType] * 10;
                    synchronized (AudioService.this.mCameraSoundForced) {
                        if (AudioService.this.mCameraSoundForced.booleanValue()) {
                            index = this.mIndexMax;
                        }
                    }
                    this.mIndex.put(1073741824, Integer.valueOf(index));
                } else {
                    int remainingDevices = AudioSystem.DEVICE_OUT_ALL;
                    int i = 0;
                    while (remainingDevices != 0) {
                        int device = 1 << i;
                        if ((device & remainingDevices) != 0) {
                            remainingDevices &= device ^ -1;
                            String name = getSettingNameForDevice(device);
                            if (device == 1073741824) {
                                defaultIndex = AudioManager.DEFAULT_STREAM_VOLUME[this.mStreamType];
                            } else {
                                defaultIndex = -1;
                            }
                            int index2 = Settings.System.getIntForUser(AudioService.this.mContentResolver, name, defaultIndex, -2);
                            if (index2 != -1) {
                                this.mIndex.put(Integer.valueOf(device), Integer.valueOf(getValidIndex(index2 * 10)));
                            }
                        }
                        i++;
                    }
                }
            }
        }

        public void applyDeviceVolume(int device) {
            int index;
            if (isMuted()) {
                index = 0;
            } else if (((device & AudioSystem.DEVICE_OUT_ALL_A2DP) == 0 || !AudioService.this.mAvrcpAbsVolSupported) && (AudioService.this.mFullVolumeDevices & device) == 0) {
                index = (getIndex(device) + 5) / 10;
            } else {
                index = (this.mIndexMax + 5) / 10;
            }
            AudioSystem.setStreamVolumeIndex(this.mStreamType, index, device);
        }

        public void applyAllVolumes() {
            int index;
            int index2;
            synchronized (VolumeStreamState.class) {
                if (isMuted()) {
                    index = 0;
                } else {
                    index = (getIndex(1073741824) + 5) / 10;
                }
                AudioSystem.setStreamVolumeIndex(this.mStreamType, index, 1073741824);
                for (Map.Entry<Integer, Integer> entry : this.mIndex.entrySet()) {
                    int device = entry.getKey().intValue();
                    if (device != 1073741824) {
                        if (isMuted()) {
                            index2 = 0;
                        } else if (((device & AudioSystem.DEVICE_OUT_ALL_A2DP) == 0 || !AudioService.this.mAvrcpAbsVolSupported) && (AudioService.this.mFullVolumeDevices & device) == 0) {
                            index2 = (entry.getValue().intValue() + 5) / 10;
                        } else {
                            index2 = (this.mIndexMax + 5) / 10;
                        }
                        AudioSystem.setStreamVolumeIndex(this.mStreamType, index2, device);
                    }
                }
            }
        }

        public boolean adjustIndex(int deltaIndex, int device) {
            return setIndex(getIndex(device) + deltaIndex, device);
        }

        public boolean setIndex(int index, int device) {
            synchronized (VolumeStreamState.class) {
                int oldIndex = getIndex(device);
                int index2 = getValidIndex(index);
                synchronized (AudioService.this.mCameraSoundForced) {
                    if (this.mStreamType == 7 && AudioService.this.mCameraSoundForced.booleanValue()) {
                        index2 = this.mIndexMax;
                    }
                }
                this.mIndex.put(Integer.valueOf(device), Integer.valueOf(index2));
                if (oldIndex == index2) {
                    return false;
                }
                boolean currentDevice = device == AudioService.this.getDeviceForStream(this.mStreamType);
                for (int streamType = AudioSystem.getNumStreamTypes() - 1; streamType >= 0; streamType--) {
                    if (streamType != this.mStreamType && AudioService.this.mStreamVolumeAlias[streamType] == this.mStreamType) {
                        int scaledIndex = AudioService.this.rescaleIndex(index2, this.mStreamType, streamType);
                        AudioService.this.mStreamStates[streamType].setIndex(scaledIndex, device);
                        if (currentDevice) {
                            AudioService.this.mStreamStates[streamType].setIndex(scaledIndex, AudioService.this.getDeviceForStream(streamType));
                        }
                    }
                }
                return true;
            }
        }

        public int getIndex(int device) {
            int intValue;
            synchronized (VolumeStreamState.class) {
                Integer index = this.mIndex.get(Integer.valueOf(device));
                if (index == null) {
                    index = this.mIndex.get(1073741824);
                }
                intValue = index.intValue();
            }
            return intValue;
        }

        public int getMaxIndex() {
            return this.mIndexMax;
        }

        public void setAllIndexes(VolumeStreamState srcStream) {
            synchronized (VolumeStreamState.class) {
                int srcStreamType = srcStream.getStreamType();
                int index = AudioService.this.rescaleIndex(srcStream.getIndex(1073741824), srcStreamType, this.mStreamType);
                for (Map.Entry<Integer, Integer> entry : this.mIndex.entrySet()) {
                    entry.setValue(Integer.valueOf(index));
                }
                for (Map.Entry<Integer, Integer> entry2 : srcStream.mIndex.entrySet()) {
                    setIndex(AudioService.this.rescaleIndex(entry2.getValue().intValue(), srcStreamType, this.mStreamType), entry2.getKey().intValue());
                }
            }
        }

        public void setAllIndexesToMax() {
            synchronized (VolumeStreamState.class) {
                for (Map.Entry<Integer, Integer> entry : this.mIndex.entrySet()) {
                    entry.setValue(Integer.valueOf(this.mIndexMax));
                }
            }
        }

        public void mute(IBinder cb, boolean state) {
            synchronized (VolumeStreamState.class) {
                VolumeDeathHandler handler = getDeathHandler(cb, state);
                if (handler == null) {
                    Log.e(AudioService.TAG, "Could not get client death handler for stream: " + this.mStreamType);
                } else {
                    handler.mute(state);
                }
            }
        }

        public int getStreamType() {
            return this.mStreamType;
        }

        public void checkFixedVolumeDevices() {
            synchronized (VolumeStreamState.class) {
                if (AudioService.this.mStreamVolumeAlias[this.mStreamType] == 3) {
                    for (Map.Entry<Integer, Integer> entry : this.mIndex.entrySet()) {
                        int device = entry.getKey().intValue();
                        int index = entry.getValue().intValue();
                        if (!((AudioService.this.mFullVolumeDevices & device) == 0 && ((AudioService.this.mFixedVolumeDevices & device) == 0 || index == 0))) {
                            entry.setValue(Integer.valueOf(this.mIndexMax));
                        }
                        applyDeviceVolume(device);
                    }
                }
            }
        }

        private int getValidIndex(int index) {
            if (index < 0) {
                return 0;
            }
            if (AudioService.this.mUseFixedVolume || index > this.mIndexMax) {
                return this.mIndexMax;
            }
            return index;
        }

        /* access modifiers changed from: private */
        public class VolumeDeathHandler implements IBinder.DeathRecipient {
            private IBinder mICallback;
            private int mMuteCount;

            VolumeDeathHandler(IBinder cb) {
                this.mICallback = cb;
            }

            public void mute(boolean state) {
                boolean updateVolume = false;
                if (state) {
                    if (this.mMuteCount == 0) {
                        try {
                            if (this.mICallback != null) {
                                this.mICallback.linkToDeath(this, 0);
                            }
                            VolumeStreamState.this.mDeathHandlers.add(this);
                            if (!VolumeStreamState.this.isMuted()) {
                                updateVolume = true;
                            }
                        } catch (RemoteException e) {
                            binderDied();
                            return;
                        }
                    } else {
                        Log.w(AudioService.TAG, "stream: " + VolumeStreamState.this.mStreamType + " was already muted by this client");
                    }
                    this.mMuteCount++;
                } else if (this.mMuteCount == 0) {
                    Log.e(AudioService.TAG, "unexpected unmute for stream: " + VolumeStreamState.this.mStreamType);
                } else {
                    this.mMuteCount--;
                    if (this.mMuteCount == 0) {
                        VolumeStreamState.this.mDeathHandlers.remove(this);
                        if (this.mICallback != null) {
                            this.mICallback.unlinkToDeath(this, 0);
                        }
                        if (!VolumeStreamState.this.isMuted()) {
                            updateVolume = true;
                        }
                    }
                }
                if (updateVolume) {
                    AudioService.sendMsg(AudioService.this.mAudioHandler, 10, 2, 0, 0, VolumeStreamState.this, 0);
                }
            }

            @Override // android.os.IBinder.DeathRecipient
            public void binderDied() {
                Log.w(AudioService.TAG, "Volume service client died for stream: " + VolumeStreamState.this.mStreamType);
                if (this.mMuteCount != 0) {
                    this.mMuteCount = 1;
                    mute(false);
                }
            }
        }

        private synchronized int muteCount() {
            int count;
            count = 0;
            int size = this.mDeathHandlers.size();
            for (int i = 0; i < size; i++) {
                count += this.mDeathHandlers.get(i).mMuteCount;
            }
            return count;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private synchronized boolean isMuted() {
            return muteCount() != 0;
        }

        private VolumeDeathHandler getDeathHandler(IBinder cb, boolean state) {
            VolumeDeathHandler handler;
            int size = this.mDeathHandlers.size();
            for (int i = 0; i < size; i++) {
                VolumeDeathHandler handler2 = this.mDeathHandlers.get(i);
                if (cb == handler2.mICallback) {
                    return handler2;
                }
            }
            if (state) {
                handler = new VolumeDeathHandler(cb);
            } else {
                Log.w(AudioService.TAG, "stream was not muted by this client");
                handler = null;
            }
            return handler;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void dump(PrintWriter pw) {
            pw.print("   Mute count: ");
            pw.println(muteCount());
            pw.print("   Max: ");
            pw.println((this.mIndexMax + 5) / 10);
            pw.print("   Current: ");
            Iterator i = this.mIndex.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry<Integer, Integer> entry = i.next();
                int device = entry.getKey().intValue();
                pw.print(Integer.toHexString(device));
                String deviceName = device == 1073741824 ? "default" : AudioSystem.getOutputDeviceName(device);
                if (!deviceName.isEmpty()) {
                    pw.print(" (");
                    pw.print(deviceName);
                    pw.print(")");
                }
                pw.print(": ");
                pw.print((entry.getValue().intValue() + 5) / 10);
                if (i.hasNext()) {
                    pw.print(", ");
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public class AudioSystemThread extends Thread {
        AudioSystemThread() {
            super(AudioService.TAG);
        }

        public void run() {
            Looper.prepare();
            synchronized (AudioService.this) {
                AudioService.this.mAudioHandler = new AudioHandler();
                AudioService.this.notify();
            }
            Looper.loop();
        }
    }

    /* access modifiers changed from: private */
    public class AudioHandler extends Handler {
        private AudioHandler() {
        }

        private void setDeviceVolume(VolumeStreamState streamState, int device) {
            streamState.applyDeviceVolume(device);
            for (int streamType = AudioSystem.getNumStreamTypes() - 1; streamType >= 0; streamType--) {
                if (streamType != streamState.mStreamType && AudioService.this.mStreamVolumeAlias[streamType] == streamState.mStreamType) {
                    int streamDevice = AudioService.this.getDeviceForStream(streamType);
                    if (!(device == streamDevice || !AudioService.this.mAvrcpAbsVolSupported || (device & AudioSystem.DEVICE_OUT_ALL_A2DP) == 0)) {
                        AudioService.this.mStreamStates[streamType].applyDeviceVolume(device);
                    }
                    AudioService.this.mStreamStates[streamType].applyDeviceVolume(streamDevice);
                }
            }
            AudioService.sendMsg(AudioService.this.mAudioHandler, 1, 2, device, 0, streamState, 500);
        }

        private void setAllVolumes(VolumeStreamState streamState) {
            streamState.applyAllVolumes();
            for (int streamType = AudioSystem.getNumStreamTypes() - 1; streamType >= 0; streamType--) {
                if (streamType != streamState.mStreamType && AudioService.this.mStreamVolumeAlias[streamType] == streamState.mStreamType) {
                    AudioService.this.mStreamStates[streamType].applyAllVolumes();
                }
            }
        }

        private void persistVolume(VolumeStreamState streamState, int device) {
            if (!AudioService.this.mUseFixedVolume) {
                if (!AudioService.this.isPlatformTelevision() || streamState.mStreamType == 3) {
                    Settings.System.putIntForUser(AudioService.this.mContentResolver, streamState.getSettingNameForDevice(device), (streamState.getIndex(device) + 5) / 10, -2);
                }
            }
        }

        private void persistRingerMode(int ringerMode) {
            if (!AudioService.this.mUseFixedVolume) {
                Settings.Global.putInt(AudioService.this.mContentResolver, "mode_ringer", ringerMode);
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:76:0x024c, code lost:
            if (r9 != 0) goto L_0x0251;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:77:0x024e, code lost:
            return true;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:78:0x0251, code lost:
            return false;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private boolean onLoadSoundEffects() {
            /*
            // Method dump skipped, instructions count: 598
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.AudioService.AudioHandler.onLoadSoundEffects():boolean");
        }

        private void onUnloadSoundEffects() {
            synchronized (AudioService.this.mSoundEffectsLock) {
                if (AudioService.this.mSoundPool != null) {
                    int[] poolId = new int[AudioService.SOUND_EFFECT_FILES.size()];
                    for (int fileIdx = 0; fileIdx < AudioService.SOUND_EFFECT_FILES.size(); fileIdx++) {
                        poolId[fileIdx] = 0;
                    }
                    for (int effect = 0; effect < 10; effect++) {
                        if (AudioService.this.SOUND_EFFECT_FILES_MAP[effect][1] > 0 && poolId[AudioService.this.SOUND_EFFECT_FILES_MAP[effect][0]] == 0) {
                            AudioService.this.mSoundPool.unload(AudioService.this.SOUND_EFFECT_FILES_MAP[effect][1]);
                            AudioService.this.SOUND_EFFECT_FILES_MAP[effect][1] = -1;
                            poolId[AudioService.this.SOUND_EFFECT_FILES_MAP[effect][0]] = -1;
                        }
                    }
                    AudioService.this.mSoundPool.release();
                    AudioService.this.mSoundPool = null;
                }
            }
        }

        private void onPlaySoundEffect(int effectType, int volume) {
            float volFloat;
            synchronized (AudioService.this.mSoundEffectsLock) {
                onLoadSoundEffects();
                if (AudioService.this.mSoundPool != null) {
                    if (volume < 0) {
                        volFloat = (float) Math.pow(10.0d, (double) (((float) AudioService.sSoundEffectVolumeDb) / 20.0f));
                    } else {
                        volFloat = ((float) volume) / 1000.0f;
                    }
                    if (AudioService.this.SOUND_EFFECT_FILES_MAP[effectType][1] > 0) {
                        AudioService.this.mSoundPool.play(AudioService.this.SOUND_EFFECT_FILES_MAP[effectType][1], volFloat, volFloat, 0, 0, 1.0f);
                    } else {
                        MediaPlayer mediaPlayer = new MediaPlayer();
                        try {
                            mediaPlayer.setDataSource(Environment.getRootDirectory() + AudioService.SOUND_EFFECTS_PATH + ((String) AudioService.SOUND_EFFECT_FILES.get(AudioService.this.SOUND_EFFECT_FILES_MAP[effectType][0])));
                            mediaPlayer.setAudioStreamType(1);
                            mediaPlayer.prepare();
                            mediaPlayer.setVolume(volFloat);
                            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                /* class android.media.AudioService.AudioHandler.AnonymousClass1 */

                                @Override // android.media.MediaPlayer.OnCompletionListener
                                public void onCompletion(MediaPlayer mp) {
                                    AudioHandler.this.cleanupPlayer(mp);
                                }
                            });
                            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                                /* class android.media.AudioService.AudioHandler.AnonymousClass2 */

                                @Override // android.media.MediaPlayer.OnErrorListener
                                public boolean onError(MediaPlayer mp, int what, int extra) {
                                    AudioHandler.this.cleanupPlayer(mp);
                                    return true;
                                }
                            });
                            mediaPlayer.start();
                        } catch (IOException ex) {
                            Log.w(AudioService.TAG, "MediaPlayer IOException: " + ex);
                        } catch (IllegalArgumentException ex2) {
                            Log.w(AudioService.TAG, "MediaPlayer IllegalArgumentException: " + ex2);
                        } catch (IllegalStateException ex3) {
                            Log.w(AudioService.TAG, "MediaPlayer IllegalStateException: " + ex3);
                        }
                    }
                }
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void cleanupPlayer(MediaPlayer mp) {
            if (mp != null) {
                try {
                    mp.stop();
                    mp.release();
                } catch (IllegalStateException ex) {
                    Log.w(AudioService.TAG, "MediaPlayer IllegalStateException: " + ex);
                }
            }
        }

        private void setForceUse(int usage, int config) {
            AudioSystem.setForceUse(usage, config);
        }

        private void onPersistSafeVolumeState(int state) {
            Settings.Global.putInt(AudioService.this.mContentResolver, Settings.Global.AUDIO_SAFE_VOLUME_STATE, state);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            AudioRoutesInfo routes;
            switch (msg.what) {
                case 0:
                    setDeviceVolume((VolumeStreamState) msg.obj, msg.arg1);
                    return;
                case 1:
                    persistVolume((VolumeStreamState) msg.obj, msg.arg1);
                    return;
                case 2:
                    if (!AudioService.this.mUseFixedVolume) {
                        Settings.System.putFloatForUser(AudioService.this.mContentResolver, Settings.System.VOLUME_MASTER, ((float) msg.arg1) / 1000.0f, -2);
                        return;
                    }
                    return;
                case 3:
                    persistRingerMode(AudioService.this.getRingerMode());
                    return;
                case 4:
                    if (!AudioService.this.mSystemReady || AudioSystem.checkAudioFlinger() != 0) {
                        Log.e(AudioService.TAG, "Media server died.");
                        AudioService.sendMsg(AudioService.this.mAudioHandler, 4, 1, 0, 0, null, 500);
                        return;
                    }
                    Log.e(AudioService.TAG, "Media server started.");
                    AudioSystem.setParameters("restarting=true");
                    AudioService.readAndSetLowRamDevice();
                    synchronized (AudioService.this.mConnectedDevices) {
                        for (Map.Entry device : AudioService.this.mConnectedDevices.entrySet()) {
                            AudioSystem.setDeviceConnectionState(((Integer) device.getKey()).intValue(), 1, (String) device.getValue());
                        }
                    }
                    AudioSystem.setPhoneState(AudioService.this.mMode);
                    AudioSystem.setForceUse(0, AudioService.this.mForcedUseForComm);
                    AudioSystem.setForceUse(2, AudioService.this.mForcedUseForComm);
                    AudioSystem.setForceUse(4, AudioService.this.mCameraSoundForced.booleanValue() ? 11 : 0);
                    for (int streamType = AudioSystem.getNumStreamTypes() - 1; streamType >= 0; streamType--) {
                        VolumeStreamState streamState = AudioService.this.mStreamStates[streamType];
                        AudioSystem.initStreamVolume(streamType, 0, (streamState.mIndexMax + 5) / 10);
                        streamState.applyAllVolumes();
                    }
                    AudioService.this.setRingerModeInt(AudioService.this.getRingerMode(), false);
                    AudioService.this.restoreMasterVolume();
                    if (AudioService.this.mMonitorOrientation) {
                        AudioService.this.setOrientationForAudioSystem();
                    }
                    if (AudioService.this.mMonitorRotation) {
                        AudioService.this.setRotationForAudioSystem();
                    }
                    synchronized (AudioService.this.mBluetoothA2dpEnabledLock) {
                        AudioSystem.setForceUse(1, AudioService.this.mBluetoothA2dpEnabled ? 0 : 10);
                    }
                    synchronized (AudioService.this.mSettingsLock) {
                        AudioSystem.setForceUse(3, AudioService.this.mDockAudioMediaEnabled ? 8 : 0);
                    }
                    if (AudioService.this.mHdmiManager != null) {
                        synchronized (AudioService.this.mHdmiManager) {
                            if (AudioService.this.mHdmiTvClient != null) {
                                AudioService.this.setHdmiSystemAudioSupported(AudioService.this.mHdmiSystemAudioSupported);
                            }
                        }
                    }
                    AudioSystem.setParameters("restarting=false");
                    return;
                case 5:
                    onPlaySoundEffect(msg.arg1, msg.arg2);
                    return;
                case 6:
                    synchronized (AudioService.this.mConnectedDevices) {
                        AudioService.this.makeA2dpDeviceUnavailableNow((String) msg.obj);
                    }
                    return;
                case 7:
                    boolean loaded = onLoadSoundEffects();
                    if (msg.obj != null) {
                        LoadSoundEffectReply reply = (LoadSoundEffectReply) msg.obj;
                        synchronized (reply) {
                            reply.mStatus = loaded ? 0 : -1;
                            reply.notify();
                        }
                        return;
                    }
                    return;
                case 8:
                case 13:
                    setForceUse(msg.arg1, msg.arg2);
                    return;
                case 9:
                    AudioService.this.resetBluetoothSco();
                    return;
                case 10:
                    setAllVolumes((VolumeStreamState) msg.obj);
                    return;
                case 11:
                    if (!AudioService.this.mUseFixedVolume) {
                        Settings.System.putIntForUser(AudioService.this.mContentResolver, Settings.System.VOLUME_MASTER_MUTE, msg.arg1, msg.arg2);
                        return;
                    }
                    return;
                case 12:
                    int N = AudioService.this.mRoutesObservers.beginBroadcast();
                    if (N > 0) {
                        synchronized (AudioService.this.mCurAudioRoutes) {
                            routes = new AudioRoutesInfo(AudioService.this.mCurAudioRoutes);
                        }
                        while (N > 0) {
                            N--;
                            try {
                                AudioService.this.mRoutesObservers.getBroadcastItem(N).dispatchAudioRoutesChanged(routes);
                            } catch (RemoteException e) {
                            }
                        }
                    }
                    AudioService.this.mRoutesObservers.finishBroadcast();
                    return;
                case 14:
                    AudioService.this.onCheckMusicActive();
                    return;
                case 15:
                    AudioService.this.onSendBecomingNoisyIntent();
                    return;
                case 16:
                case 17:
                    AudioService.this.onConfigureSafeVolume(msg.what == 17);
                    return;
                case 18:
                    onPersistSafeVolumeState(msg.arg1);
                    return;
                case 19:
                    AudioService.this.onBroadcastScoConnectionState(msg.arg1);
                    return;
                case 20:
                    onUnloadSoundEffects();
                    return;
                case 21:
                    AudioService.this.onSystemReady();
                    return;
                case 22:
                    Settings.Secure.putIntForUser(AudioService.this.mContentResolver, Settings.Secure.UNSAFE_VOLUME_MUSIC_ACTIVE_MS, msg.arg1, -2);
                    return;
                case 23:
                    Settings.System.putIntForUser(AudioService.this.mContentResolver, Settings.System.MICROPHONE_MUTE, msg.arg1, msg.arg2);
                    return;
                case 100:
                    AudioService.this.onSetWiredDeviceConnectionState(msg.arg1, msg.arg2, (String) msg.obj);
                    AudioService.this.mAudioEventWakeLock.release();
                    return;
                case 101:
                    AudioService.this.onSetA2dpSourceConnectionState((BluetoothDevice) msg.obj, msg.arg1);
                    AudioService.this.mAudioEventWakeLock.release();
                    return;
                case 102:
                    AudioService.this.onSetA2dpSinkConnectionState((BluetoothDevice) msg.obj, msg.arg1);
                    AudioService.this.mAudioEventWakeLock.release();
                    return;
                default:
                    return;
            }
        }
    }

    private class SettingsObserver extends ContentObserver {
        SettingsObserver() {
            super(new Handler());
            AudioService.this.mContentResolver.registerContentObserver(Settings.System.getUriFor(Settings.System.MODE_RINGER_STREAMS_AFFECTED), false, this);
            AudioService.this.mContentResolver.registerContentObserver(Settings.Global.getUriFor(Settings.Global.DOCK_AUDIO_MEDIA_ENABLED), false, this);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            synchronized (AudioService.this.mSettingsLock) {
                if (AudioService.this.updateRingerModeAffectedStreams()) {
                    AudioService.this.setRingerModeInt(AudioService.this.getRingerMode(), false);
                }
                AudioService.this.readDockAudioSettings(AudioService.this.mContentResolver);
            }
        }
    }

    private void makeA2dpDeviceAvailable(String address) {
        sendMsg(this.mAudioHandler, 0, 2, 128, 0, this.mStreamStates[3], 0);
        setBluetoothA2dpOnInt(true);
        AudioSystem.setDeviceConnectionState(128, 1, address);
        AudioSystem.setParameters("A2dpSuspended=false");
        this.mConnectedDevices.put(new Integer(128), address);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onSendBecomingNoisyIntent() {
        sendBroadcastToAll(new Intent(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void makeA2dpDeviceUnavailableNow(String address) {
        synchronized (this.mA2dpAvrcpLock) {
            this.mAvrcpAbsVolSupported = false;
        }
        AudioSystem.setDeviceConnectionState(128, 0, address);
        this.mConnectedDevices.remove(128);
        synchronized (this.mCurAudioRoutes) {
            if (this.mCurAudioRoutes.mBluetoothName != null) {
                this.mCurAudioRoutes.mBluetoothName = null;
                sendMsg(this.mAudioHandler, 12, 1, 0, 0, null, 0);
            }
        }
    }

    private void makeA2dpDeviceUnavailableLater(String address) {
        AudioSystem.setParameters("A2dpSuspended=true");
        this.mConnectedDevices.remove(128);
        this.mAudioHandler.sendMessageDelayed(this.mAudioHandler.obtainMessage(6, address), 8000);
    }

    private void makeA2dpSrcAvailable(String address) {
        AudioSystem.setDeviceConnectionState(AudioSystem.DEVICE_IN_BLUETOOTH_A2DP, 1, address);
        this.mConnectedDevices.put(new Integer((int) AudioSystem.DEVICE_IN_BLUETOOTH_A2DP), address);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void makeA2dpSrcUnavailable(String address) {
        AudioSystem.setDeviceConnectionState(AudioSystem.DEVICE_IN_BLUETOOTH_A2DP, 0, address);
        this.mConnectedDevices.remove(Integer.valueOf((int) AudioSystem.DEVICE_IN_BLUETOOTH_A2DP));
    }

    private void cancelA2dpDeviceTimeout() {
        this.mAudioHandler.removeMessages(6);
    }

    private boolean hasScheduledA2dpDockTimeout() {
        return this.mAudioHandler.hasMessages(6);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onSetA2dpSinkConnectionState(BluetoothDevice btDevice, int state) {
        boolean isConnected = true;
        if (DEBUG_VOL) {
            Log.d(TAG, "onSetA2dpSinkConnectionState btDevice=" + btDevice + "state=" + state);
        }
        if (btDevice != null) {
            String address = btDevice.getAddress();
            if (!BluetoothAdapter.checkBluetoothAddress(address)) {
                address = ProxyInfo.LOCAL_EXCL_LIST;
            }
            synchronized (this.mConnectedDevices) {
                if (!this.mConnectedDevices.containsKey(128) || !this.mConnectedDevices.get(128).equals(address)) {
                    isConnected = false;
                }
                if (isConnected && state != 2) {
                    if (!btDevice.isBluetoothDock()) {
                        makeA2dpDeviceUnavailableNow(address);
                    } else if (state == 0) {
                        makeA2dpDeviceUnavailableLater(address);
                    }
                    synchronized (this.mCurAudioRoutes) {
                        if (this.mCurAudioRoutes.mBluetoothName != null) {
                            this.mCurAudioRoutes.mBluetoothName = null;
                            sendMsg(this.mAudioHandler, 12, 1, 0, 0, null, 0);
                        }
                    }
                } else if (!isConnected && state == 2) {
                    if (btDevice.isBluetoothDock()) {
                        cancelA2dpDeviceTimeout();
                        this.mDockAddress = address;
                    } else if (hasScheduledA2dpDockTimeout()) {
                        cancelA2dpDeviceTimeout();
                        makeA2dpDeviceUnavailableNow(this.mDockAddress);
                    }
                    makeA2dpDeviceAvailable(address);
                    synchronized (this.mCurAudioRoutes) {
                        String name = btDevice.getAliasName();
                        if (!TextUtils.equals(this.mCurAudioRoutes.mBluetoothName, name)) {
                            this.mCurAudioRoutes.mBluetoothName = name;
                            sendMsg(this.mAudioHandler, 12, 1, 0, 0, null, 0);
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onSetA2dpSourceConnectionState(BluetoothDevice btDevice, int state) {
        if (DEBUG_VOL) {
            Log.d(TAG, "onSetA2dpSourceConnectionState btDevice=" + btDevice + " state=" + state);
        }
        if (btDevice != null) {
            String address = btDevice.getAddress();
            if (!BluetoothAdapter.checkBluetoothAddress(address)) {
                address = ProxyInfo.LOCAL_EXCL_LIST;
            }
            synchronized (this.mConnectedDevices) {
                boolean isConnected = this.mConnectedDevices.containsKey(Integer.valueOf(AudioSystem.DEVICE_IN_BLUETOOTH_A2DP)) && this.mConnectedDevices.get(Integer.valueOf(AudioSystem.DEVICE_IN_BLUETOOTH_A2DP)).equals(address);
                if (isConnected && state != 2) {
                    makeA2dpSrcUnavailable(address);
                } else if (!isConnected && state == 2) {
                    makeA2dpSrcAvailable(address);
                }
            }
        }
    }

    @Override // android.media.IAudioService
    public void avrcpSupportsAbsoluteVolume(String address, boolean support) {
        synchronized (this.mA2dpAvrcpLock) {
            this.mAvrcpAbsVolSupported = support;
            sendMsg(this.mAudioHandler, 0, 2, 128, 0, this.mStreamStates[3], 0);
            sendMsg(this.mAudioHandler, 0, 2, 128, 0, this.mStreamStates[2], 0);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean handleDeviceConnection(boolean connected, int device, String params) {
        boolean isConnected;
        synchronized (this.mConnectedDevices) {
            if (!this.mConnectedDevices.containsKey(Integer.valueOf(device)) || (!params.isEmpty() && !this.mConnectedDevices.get(Integer.valueOf(device)).equals(params))) {
                isConnected = false;
            } else {
                isConnected = true;
            }
            if (isConnected && !connected) {
                AudioSystem.setDeviceConnectionState(device, 0, this.mConnectedDevices.get(Integer.valueOf(device)));
                this.mConnectedDevices.remove(Integer.valueOf(device));
                return true;
            } else if (isConnected || !connected) {
                return false;
            } else {
                AudioSystem.setDeviceConnectionState(device, 1, params);
                this.mConnectedDevices.put(new Integer(device), params);
                return true;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int checkSendBecomingNoisyIntent(int device, int state) {
        int delay = 0;
        if (state == 0 && (this.mBecomingNoisyIntentDevices & device) != 0) {
            int devices = 0;
            for (Integer num : this.mConnectedDevices.keySet()) {
                int dev = num.intValue();
                if ((Integer.MIN_VALUE & dev) == 0 && (this.mBecomingNoisyIntentDevices & dev) != 0) {
                    devices |= dev;
                }
            }
            if (devices == device) {
                sendMsg(this.mAudioHandler, 15, 0, 0, 0, null, 0);
                delay = 1000;
            }
        }
        if (this.mAudioHandler.hasMessages(101) || this.mAudioHandler.hasMessages(102) || this.mAudioHandler.hasMessages(100)) {
            return 1000;
        }
        return delay;
    }

    private void sendDeviceConnectionIntent(int device, int state, String name) {
        int newConn;
        Intent intent = new Intent();
        intent.putExtra("state", state);
        intent.putExtra("name", name);
        intent.addFlags(1073741824);
        int connType = 0;
        if (device == 4) {
            connType = 1;
            intent.setAction("android.intent.action.HEADSET_PLUG");
            intent.putExtra("microphone", 1);
        } else if (device == 8 || device == 131072) {
            connType = 2;
            intent.setAction("android.intent.action.HEADSET_PLUG");
            intent.putExtra("microphone", 0);
        } else if (device == 2048) {
            connType = 4;
            intent.setAction(AudioManager.ACTION_ANALOG_AUDIO_DOCK_PLUG);
        } else if (device == 4096) {
            connType = 4;
            intent.setAction(AudioManager.ACTION_DIGITAL_AUDIO_DOCK_PLUG);
        } else if (device == 1024) {
            connType = 8;
            configureHdmiPlugIntent(intent, state);
        }
        synchronized (this.mCurAudioRoutes) {
            if (connType != 0) {
                int newConn2 = this.mCurAudioRoutes.mMainType;
                if (state != 0) {
                    newConn = newConn2 | connType;
                } else {
                    newConn = newConn2 & (connType ^ -1);
                }
                if (newConn != this.mCurAudioRoutes.mMainType) {
                    this.mCurAudioRoutes.mMainType = newConn;
                    sendMsg(this.mAudioHandler, 12, 1, 0, 0, null, 0);
                }
            }
        }
        long ident = Binder.clearCallingIdentity();
        try {
            ActivityManagerNative.broadcastStickyIntent(intent, null, -1);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onSetWiredDeviceConnectionState(int device, int state, String name) {
        boolean isUsb;
        boolean z = false;
        synchronized (this.mConnectedDevices) {
            if (state == 0 && (device == 4 || device == 8 || device == 131072)) {
                setBluetoothA2dpOnInt(true);
            }
            if ((device & -24577) == 0 || ((Integer.MIN_VALUE & device) != 0 && (2147477503 & device) == 0)) {
                isUsb = true;
            } else {
                isUsb = false;
            }
            if (state == 1) {
                z = true;
            }
            handleDeviceConnection(z, device, isUsb ? name : ProxyInfo.LOCAL_EXCL_LIST);
            if (state != 0) {
                if (device == 4 || device == 8 || device == 131072) {
                    setBluetoothA2dpOnInt(false);
                }
                if ((device & 12) != 0) {
                    sendMsg(this.mAudioHandler, 14, 0, 0, 0, null, MUSIC_ACTIVE_POLL_PERIOD_MS);
                }
                if (isPlatformTelevision() && (device & 1024) != 0) {
                    this.mFixedVolumeDevices |= 1024;
                    checkAllFixedVolumeDevices();
                    if (this.mHdmiManager != null) {
                        synchronized (this.mHdmiManager) {
                            if (this.mHdmiPlaybackClient != null) {
                                this.mHdmiCecSink = false;
                                this.mHdmiPlaybackClient.queryDisplayStatus(this.mHdmiDisplayStatusCallback);
                            }
                        }
                    }
                }
            } else if (!(!isPlatformTelevision() || (device & 1024) == 0 || this.mHdmiManager == null)) {
                synchronized (this.mHdmiManager) {
                    this.mHdmiCecSink = false;
                }
            }
            if (!isUsb && device != -2147483632) {
                sendDeviceConnectionIntent(device, state, name);
            }
        }
    }

    private void configureHdmiPlugIntent(Intent intent, int state) {
        intent.setAction(AudioManager.ACTION_HDMI_AUDIO_PLUG);
        intent.putExtra(AudioManager.EXTRA_AUDIO_PLUG_STATE, state);
        if (state == 1) {
            ArrayList<AudioPort> ports = new ArrayList<>();
            if (AudioSystem.listAudioPorts(ports, new int[1]) == 0) {
                Iterator<AudioPort> it = ports.iterator();
                while (it.hasNext()) {
                    AudioPort port = it.next();
                    if (port instanceof AudioDevicePort) {
                        AudioDevicePort devicePort = (AudioDevicePort) port;
                        if (devicePort.type() == 1024) {
                            int[] formats = devicePort.formats();
                            if (formats.length > 0) {
                                ArrayList<Integer> encodingList = new ArrayList<>(1);
                                for (int format : formats) {
                                    if (format != 0) {
                                        encodingList.add(Integer.valueOf(format));
                                    }
                                }
                                int[] encodingArray = new int[encodingList.size()];
                                for (int i = 0; i < encodingArray.length; i++) {
                                    encodingArray[i] = encodingList.get(i).intValue();
                                }
                                intent.putExtra(AudioManager.EXTRA_ENCODINGS, encodingArray);
                            }
                            int maxChannels = 0;
                            for (int mask : devicePort.channelMasks()) {
                                int channelCount = AudioFormat.channelCountFromOutChannelMask(mask);
                                if (channelCount > maxChannels) {
                                    maxChannels = channelCount;
                                }
                            }
                            intent.putExtra(AudioManager.EXTRA_MAX_CHANNEL_COUNT, maxChannels);
                        }
                    }
                }
            }
        }
    }

    private class AudioServiceBroadcastReceiver extends BroadcastReceiver {
        private AudioServiceBroadcastReceiver() {
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String params;
            int config;
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_DOCK_EVENT)) {
                int dockState = intent.getIntExtra(Intent.EXTRA_DOCK_STATE, 0);
                switch (dockState) {
                    case 1:
                        config = 7;
                        break;
                    case 2:
                        config = 6;
                        break;
                    case 3:
                        config = 8;
                        break;
                    case 4:
                        config = 9;
                        break;
                    default:
                        config = 0;
                        break;
                }
                if (!(dockState == 3 || (dockState == 0 && AudioService.this.mDockState == 3))) {
                    AudioSystem.setForceUse(3, config);
                }
                AudioService.this.mDockState = dockState;
            } else if (action.equals(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, 0);
                int outDevice = 16;
                BluetoothDevice btDevice = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (btDevice != null) {
                    String address = btDevice.getAddress();
                    BluetoothClass btClass = btDevice.getBluetoothClass();
                    if (btClass != null) {
                        switch (btClass.getDeviceClass()) {
                            case 1028:
                            case 1032:
                                outDevice = 32;
                                break;
                            case BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO:
                                outDevice = 64;
                                break;
                        }
                    }
                    if (!BluetoothAdapter.checkBluetoothAddress(address)) {
                        address = ProxyInfo.LOCAL_EXCL_LIST;
                    }
                    boolean connected = state == 2;
                    if (AudioService.this.handleDeviceConnection(connected, outDevice, address) && AudioService.this.handleDeviceConnection(connected, -2147483640, address)) {
                        synchronized (AudioService.this.mScoClients) {
                            if (connected) {
                                AudioService.this.mBluetoothHeadsetDevice = btDevice;
                            } else {
                                AudioService.this.mBluetoothHeadsetDevice = null;
                                AudioService.this.resetBluetoothSco();
                            }
                        }
                    }
                }
            } else if (action.equals(AudioManager.ACTION_USB_AUDIO_ACCESSORY_PLUG)) {
                int state2 = intent.getIntExtra("state", 0);
                int alsaCard = intent.getIntExtra("card", -1);
                int alsaDevice = intent.getIntExtra(UsbManager.EXTRA_DEVICE, -1);
                AudioService.this.setWiredDeviceConnectionState(8192, state2, (alsaCard == -1 && alsaDevice == -1) ? ProxyInfo.LOCAL_EXCL_LIST : "card=" + alsaCard + ";device=" + alsaDevice);
            } else if (action.equals(AudioManager.ACTION_USB_AUDIO_DEVICE_PLUG)) {
                if (Settings.Secure.getInt(AudioService.this.mContentResolver, Settings.Secure.USB_AUDIO_AUTOMATIC_ROUTING_DISABLED, 0) == 0) {
                    int state3 = intent.getIntExtra("state", 0);
                    int alsaCard2 = intent.getIntExtra("card", -1);
                    int alsaDevice2 = intent.getIntExtra(UsbManager.EXTRA_DEVICE, -1);
                    boolean hasPlayback = intent.getBooleanExtra("hasPlayback", false);
                    boolean hasCapture = intent.getBooleanExtra("hasCapture", false);
                    intent.getBooleanExtra("hasMIDI", false);
                    if (alsaCard2 == -1 && alsaDevice2 == -1) {
                        params = ProxyInfo.LOCAL_EXCL_LIST;
                    } else {
                        params = "card=" + alsaCard2 + ";device=" + alsaDevice2;
                    }
                    if (hasPlayback) {
                        AudioService.this.setWiredDeviceConnectionState(16384, state3, params);
                    }
                    if (hasCapture) {
                        AudioService.this.setWiredDeviceConnectionState(-2147479552, state3, params);
                    }
                }
            } else if (action.equals(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED)) {
                boolean broadcast = false;
                int scoAudioState = -1;
                synchronized (AudioService.this.mScoClients) {
                    int btState = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, -1);
                    if (!AudioService.this.mScoClients.isEmpty() && (AudioService.this.mScoAudioState == 3 || AudioService.this.mScoAudioState == 1 || AudioService.this.mScoAudioState == 5)) {
                        broadcast = true;
                    }
                    switch (btState) {
                        case 10:
                            scoAudioState = 0;
                            AudioService.this.mScoAudioState = 0;
                            AudioService.this.clearAllScoClients(0, false);
                            break;
                        case 11:
                            if (!(AudioService.this.mScoAudioState == 3 || AudioService.this.mScoAudioState == 5 || AudioService.this.mScoAudioState == 4)) {
                                AudioService.this.mScoAudioState = 2;
                            }
                            broadcast = false;
                            break;
                        case 12:
                            scoAudioState = 1;
                            if (!(AudioService.this.mScoAudioState == 3 || AudioService.this.mScoAudioState == 5 || AudioService.this.mScoAudioState == 4)) {
                                AudioService.this.mScoAudioState = 2;
                                break;
                            }
                        default:
                            broadcast = false;
                            break;
                    }
                }
                if (broadcast) {
                    AudioService.this.broadcastScoConnectionState(scoAudioState);
                    Intent newIntent = new Intent(AudioManager.ACTION_SCO_AUDIO_STATE_CHANGED);
                    newIntent.putExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, scoAudioState);
                    AudioService.this.sendStickyBroadcastToAll(newIntent);
                }
            } else if (action.equals(Intent.ACTION_SCREEN_ON)) {
                if (AudioService.this.mMonitorRotation) {
                    AudioService.this.mOrientationListener.onOrientationChanged(0);
                    AudioService.this.mOrientationListener.enable();
                }
                AudioSystem.setParameters("screen_state=on");
            } else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                if (AudioService.this.mMonitorRotation) {
                    AudioService.this.mOrientationListener.disable();
                }
                AudioSystem.setParameters("screen_state=off");
            } else if (action.equals(Intent.ACTION_CONFIGURATION_CHANGED)) {
                AudioService.this.handleConfigurationChanged(context);
            } else if (action.equals(Intent.ACTION_USER_SWITCHED)) {
                AudioService.sendMsg(AudioService.this.mAudioHandler, 15, 0, 0, 0, null, 0);
                AudioService.this.mMediaFocusControl.discardAudioFocusOwner();
                AudioService.this.readAudioSettings(true);
                AudioService.sendMsg(AudioService.this.mAudioHandler, 10, 2, 0, 0, AudioService.this.mStreamStates[3], 0);
            }
        }
    }

    @Override // android.media.IAudioService
    public boolean registerRemoteController(IRemoteControlDisplay rcd, int w, int h, ComponentName listenerComp) {
        return this.mMediaFocusControl.registerRemoteController(rcd, w, h, listenerComp);
    }

    @Override // android.media.IAudioService
    public boolean registerRemoteControlDisplay(IRemoteControlDisplay rcd, int w, int h) {
        return this.mMediaFocusControl.registerRemoteControlDisplay(rcd, w, h);
    }

    @Override // android.media.IAudioService
    public void unregisterRemoteControlDisplay(IRemoteControlDisplay rcd) {
        this.mMediaFocusControl.unregisterRemoteControlDisplay(rcd);
    }

    @Override // android.media.IAudioService
    public void remoteControlDisplayUsesBitmapSize(IRemoteControlDisplay rcd, int w, int h) {
        this.mMediaFocusControl.remoteControlDisplayUsesBitmapSize(rcd, w, h);
    }

    @Override // android.media.IAudioService
    public void remoteControlDisplayWantsPlaybackPositionSync(IRemoteControlDisplay rcd, boolean wantsSync) {
        this.mMediaFocusControl.remoteControlDisplayWantsPlaybackPositionSync(rcd, wantsSync);
    }

    @Override // android.media.IAudioService
    public void setRemoteStreamVolume(int index) {
        enforceSelfOrSystemUI("set the remote stream volume");
        this.mMediaFocusControl.setRemoteStreamVolume(index);
    }

    @Override // android.media.IAudioService
    public int requestAudioFocus(int mainStreamType, int durationHint, IBinder cb, IAudioFocusDispatcher fd, String clientId, String callingPackageName) {
        return this.mMediaFocusControl.requestAudioFocus(mainStreamType, durationHint, cb, fd, clientId, callingPackageName);
    }

    @Override // android.media.IAudioService
    public int abandonAudioFocus(IAudioFocusDispatcher fd, String clientId) {
        return this.mMediaFocusControl.abandonAudioFocus(fd, clientId);
    }

    @Override // android.media.IAudioService
    public void unregisterAudioFocusClient(String clientId) {
        this.mMediaFocusControl.unregisterAudioFocusClient(clientId);
    }

    @Override // android.media.IAudioService
    public int getCurrentAudioFocus() {
        return this.mMediaFocusControl.getCurrentAudioFocus();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleConfigurationChanged(Context context) {
        int newOrientation;
        try {
            Configuration config = context.getResources().getConfiguration();
            if (this.mMonitorOrientation && (newOrientation = config.orientation) != this.mDeviceOrientation) {
                this.mDeviceOrientation = newOrientation;
                setOrientationForAudioSystem();
            }
            sendMsg(this.mAudioHandler, 16, 0, 0, 0, null, 0);
            boolean cameraSoundForced = this.mContext.getResources().getBoolean(17956963);
            synchronized (this.mSettingsLock) {
                synchronized (this.mCameraSoundForced) {
                    if (cameraSoundForced != this.mCameraSoundForced.booleanValue()) {
                        this.mCameraSoundForced = Boolean.valueOf(cameraSoundForced);
                        if (!isPlatformTelevision()) {
                            VolumeStreamState s = this.mStreamStates[7];
                            if (cameraSoundForced) {
                                s.setAllIndexesToMax();
                                this.mRingerModeAffectedStreams &= -129;
                            } else {
                                s.setAllIndexes(this.mStreamStates[1]);
                                this.mRingerModeAffectedStreams |= 128;
                            }
                            setRingerModeInt(getRingerMode(), false);
                        }
                        sendMsg(this.mAudioHandler, 8, 2, 4, cameraSoundForced ? 11 : 0, null, 0);
                        sendMsg(this.mAudioHandler, 10, 2, 0, 0, this.mStreamStates[7], 0);
                    }
                }
            }
            this.mVolumeController.setLayoutDirection(config.getLayoutDirection());
        } catch (Exception e) {
            Log.e(TAG, "Error handling configuration change: ", e);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setOrientationForAudioSystem() {
        switch (this.mDeviceOrientation) {
            case 0:
                AudioSystem.setParameters("orientation=undefined");
                return;
            case 1:
                AudioSystem.setParameters("orientation=portrait");
                return;
            case 2:
                AudioSystem.setParameters("orientation=landscape");
                return;
            case 3:
                AudioSystem.setParameters("orientation=square");
                return;
            default:
                Log.e(TAG, "Unknown orientation");
                return;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setRotationForAudioSystem() {
        switch (this.mDeviceRotation) {
            case 0:
                AudioSystem.setParameters("rotation=0");
                return;
            case 1:
                AudioSystem.setParameters("rotation=90");
                return;
            case 2:
                AudioSystem.setParameters("rotation=180");
                return;
            case 3:
                AudioSystem.setParameters("rotation=270");
                return;
            default:
                Log.e(TAG, "Unknown device rotation");
                return;
        }
    }

    public void setBluetoothA2dpOnInt(boolean on) {
        synchronized (this.mBluetoothA2dpEnabledLock) {
            this.mBluetoothA2dpEnabled = on;
            this.mAudioHandler.removeMessages(13);
            AudioSystem.setForceUse(1, this.mBluetoothA2dpEnabled ? 0 : 10);
        }
    }

    @Override // android.media.IAudioService
    public void setRingtonePlayer(IRingtonePlayer player) {
        this.mContext.enforceCallingOrSelfPermission(Manifest.permission.REMOTE_AUDIO_PLAYBACK, null);
        this.mRingtonePlayer = player;
    }

    @Override // android.media.IAudioService
    public IRingtonePlayer getRingtonePlayer() {
        return this.mRingtonePlayer;
    }

    @Override // android.media.IAudioService
    public AudioRoutesInfo startWatchingRoutes(IAudioRoutesObserver observer) {
        AudioRoutesInfo routes;
        synchronized (this.mCurAudioRoutes) {
            routes = new AudioRoutesInfo(this.mCurAudioRoutes);
            this.mRoutesObservers.register(observer);
        }
        return routes;
    }

    private void setSafeMediaVolumeEnabled(boolean on) {
        synchronized (this.mSafeMediaVolumeState) {
            if (!(this.mSafeMediaVolumeState.intValue() == 0 || this.mSafeMediaVolumeState.intValue() == 1)) {
                if (on && this.mSafeMediaVolumeState.intValue() == 2) {
                    this.mSafeMediaVolumeState = 3;
                    enforceSafeMediaVolume();
                } else if (!on && this.mSafeMediaVolumeState.intValue() == 3) {
                    this.mSafeMediaVolumeState = 2;
                    this.mMusicActiveMs = 1;
                    saveMusicActiveMs();
                    sendMsg(this.mAudioHandler, 14, 0, 0, 0, null, MUSIC_ACTIVE_POLL_PERIOD_MS);
                }
            }
        }
    }

    private void enforceSafeMediaVolume() {
        VolumeStreamState streamState = this.mStreamStates[3];
        int devices = 12;
        int i = 0;
        while (devices != 0) {
            int i2 = i + 1;
            int device = 1 << i;
            if ((device & devices) == 0) {
                i = i2;
            } else {
                if (streamState.getIndex(device) > this.mSafeMediaVolumeIndex) {
                    streamState.setIndex(this.mSafeMediaVolumeIndex, device);
                    sendMsg(this.mAudioHandler, 0, 2, device, 0, streamState, 0);
                }
                devices &= device ^ -1;
                i = i2;
            }
        }
    }

    private boolean checkSafeMediaVolume(int streamType, int index, int device) {
        boolean z;
        synchronized (this.mSafeMediaVolumeState) {
            if (this.mSafeMediaVolumeState.intValue() != 3 || this.mStreamVolumeAlias[streamType] != 3 || (device & 12) == 0 || index <= this.mSafeMediaVolumeIndex) {
                z = true;
            } else {
                z = false;
            }
        }
        return z;
    }

    @Override // android.media.IAudioService
    public void disableSafeMediaVolume() {
        enforceSelfOrSystemUI("disable the safe media volume");
        synchronized (this.mSafeMediaVolumeState) {
            setSafeMediaVolumeEnabled(false);
            if (this.mPendingVolumeCommand != null) {
                onSetStreamVolume(this.mPendingVolumeCommand.mStreamType, this.mPendingVolumeCommand.mIndex, this.mPendingVolumeCommand.mFlags, this.mPendingVolumeCommand.mDevice);
                this.mPendingVolumeCommand = null;
            }
        }
    }

    /* access modifiers changed from: private */
    public class MyDisplayStatusCallback implements HdmiPlaybackClient.DisplayStatusCallback {
        private MyDisplayStatusCallback() {
        }

        @Override // android.hardware.hdmi.HdmiPlaybackClient.DisplayStatusCallback
        public void onComplete(int status) {
            if (AudioService.this.mHdmiManager != null) {
                synchronized (AudioService.this.mHdmiManager) {
                    AudioService.this.mHdmiCecSink = status != -1;
                    if (AudioService.this.isPlatformTelevision() && !AudioService.this.mHdmiCecSink) {
                        AudioService.this.mFixedVolumeDevices &= -1025;
                    }
                    AudioService.this.checkAllFixedVolumeDevices();
                }
            }
        }
    }

    @Override // android.media.IAudioService
    public int setHdmiSystemAudioSupported(boolean on) {
        int device = 0;
        if (this.mHdmiManager != null) {
            synchronized (this.mHdmiManager) {
                if (this.mHdmiTvClient == null) {
                    Log.w(TAG, "Only Hdmi-Cec enabled TV device supports system audio mode.");
                    return 0;
                }
                synchronized (this.mHdmiTvClient) {
                    if (this.mHdmiSystemAudioSupported != on) {
                        this.mHdmiSystemAudioSupported = on;
                        AudioSystem.setForceUse(5, on ? 12 : 0);
                    }
                    device = AudioSystem.getDevicesForStream(3);
                }
            }
        }
        return device;
    }

    @Override // android.media.IAudioService
    public boolean isHdmiSystemAudioSupported() {
        return this.mHdmiSystemAudioSupported;
    }

    /* access modifiers changed from: private */
    public static class StreamOverride implements AccessibilityManager.TouchExplorationStateChangeListener {
        private static final int DEFAULT_STREAM_TYPE_OVERRIDE_DELAY_MS = 5000;
        private static final int TOUCH_EXPLORE_STREAM_TYPE_OVERRIDE_DELAY_MS = 1000;
        static int sDelayMs;

        private StreamOverride() {
        }

        static void init(Context ctxt) {
            AccessibilityManager accessibilityManager = (AccessibilityManager) ctxt.getSystemService(Context.ACCESSIBILITY_SERVICE);
            updateDefaultStreamOverrideDelay(accessibilityManager.isTouchExplorationEnabled());
            accessibilityManager.addTouchExplorationStateChangeListener(new StreamOverride());
        }

        @Override // android.view.accessibility.AccessibilityManager.TouchExplorationStateChangeListener
        public void onTouchExplorationStateChanged(boolean enabled) {
            updateDefaultStreamOverrideDelay(enabled);
        }

        private static void updateDefaultStreamOverrideDelay(boolean touchExploreEnabled) {
            if (touchExploreEnabled) {
                sDelayMs = 1000;
            } else {
                sDelayMs = 5000;
            }
            if (AudioService.DEBUG_VOL) {
                Log.d(AudioService.TAG, "Touch exploration enabled=" + touchExploreEnabled + " stream override delay is now " + sDelayMs + " ms");
            }
        }
    }

    @Override // android.media.IAudioService
    public boolean isCameraSoundForced() {
        boolean booleanValue;
        synchronized (this.mCameraSoundForced) {
            booleanValue = this.mCameraSoundForced.booleanValue();
        }
        return booleanValue;
    }

    private void dumpRingerMode(PrintWriter pw) {
        pw.println("\nRinger mode: ");
        pw.println("- mode: " + RINGER_MODE_NAMES[this.mRingerMode]);
        pw.print("- ringer mode affected streams = 0x");
        pw.println(Integer.toHexString(this.mRingerModeAffectedStreams));
        pw.print("- ringer mode muted streams = 0x");
        pw.println(Integer.toHexString(this.mRingerModeMutedStreams));
    }

    /* access modifiers changed from: protected */
    @Override // android.os.Binder
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        this.mContext.enforceCallingOrSelfPermission(Manifest.permission.DUMP, TAG);
        this.mMediaFocusControl.dump(pw);
        dumpStreamStates(pw);
        dumpRingerMode(pw);
        pw.println("\nAudio routes:");
        pw.print("  mMainType=0x");
        pw.println(Integer.toHexString(this.mCurAudioRoutes.mMainType));
        pw.print("  mBluetoothName=");
        pw.println(this.mCurAudioRoutes.mBluetoothName);
        pw.println("\nOther state:");
        pw.print("  mVolumeController=");
        pw.println(this.mVolumeController);
        pw.print("  mSafeMediaVolumeState=");
        pw.println(safeMediaVolumeStateToString(this.mSafeMediaVolumeState));
        pw.print("  mSafeMediaVolumeIndex=");
        pw.println(this.mSafeMediaVolumeIndex);
        pw.print("  mPendingVolumeCommand=");
        pw.println(this.mPendingVolumeCommand);
        pw.print("  mMusicActiveMs=");
        pw.println(this.mMusicActiveMs);
        pw.print("  mMcc=");
        pw.println(this.mMcc);
    }

    private static String safeMediaVolumeStateToString(Integer state) {
        switch (state.intValue()) {
            case 0:
                return "SAFE_MEDIA_VOLUME_NOT_CONFIGURED";
            case 1:
                return "SAFE_MEDIA_VOLUME_DISABLED";
            case 2:
                return "SAFE_MEDIA_VOLUME_INACTIVE";
            case 3:
                return "SAFE_MEDIA_VOLUME_ACTIVE";
            default:
                return null;
        }
    }

    /* access modifiers changed from: private */
    public static void readAndSetLowRamDevice() {
        int status = AudioSystem.setLowRamDevice(ActivityManager.isLowRamDeviceStatic());
        if (status != 0) {
            Log.w(TAG, "AudioFlinger informed of device's low RAM attribute; status " + status);
        }
    }

    private void enforceSelfOrSystemUI(String action) {
        this.mContext.enforceCallingOrSelfPermission(Manifest.permission.STATUS_BAR_SERVICE, "Only SystemUI can " + action);
    }

    @Override // android.media.IAudioService
    public void setVolumeController(final IVolumeController controller) {
        enforceSelfOrSystemUI("set the volume controller");
        if (!this.mVolumeController.isSameBinder(controller)) {
            this.mVolumeController.postDismiss();
            if (controller != null) {
                try {
                    controller.asBinder().linkToDeath(new IBinder.DeathRecipient() {
                        /* class android.media.AudioService.AnonymousClass3 */

                        @Override // android.os.IBinder.DeathRecipient
                        public void binderDied() {
                            if (AudioService.this.mVolumeController.isSameBinder(controller)) {
                                Log.w(AudioService.TAG, "Current remote volume controller died, unregistering");
                                AudioService.this.setVolumeController(null);
                            }
                        }
                    }, 0);
                } catch (RemoteException e) {
                }
            }
            this.mVolumeController.setController(controller);
            if (DEBUG_VOL) {
                Log.d(TAG, "Volume controller: " + this.mVolumeController);
            }
        }
    }

    @Override // android.media.IAudioService
    public void notifyVolumeControllerVisible(IVolumeController controller, boolean visible) {
        enforceSelfOrSystemUI("notify about volume controller visibility");
        if (this.mVolumeController.isSameBinder(controller)) {
            this.mVolumeController.setVisible(visible);
            if (DEBUG_VOL) {
                Log.d(TAG, "Volume controller visible: " + visible);
            }
        }
    }

    public static class VolumeController {
        private static final String TAG = "VolumeController";
        private IVolumeController mController;
        private int mLongPressTimeout;
        private long mNextLongPress;
        private boolean mVisible;

        public void setController(IVolumeController controller) {
            this.mController = controller;
            this.mVisible = false;
        }

        public void loadSettings(ContentResolver cr) {
            this.mLongPressTimeout = Settings.Secure.getIntForUser(cr, Settings.Secure.LONG_PRESS_TIMEOUT, 500, -2);
        }

        public boolean suppressAdjustment(int resolvedStream, int flags) {
            if (resolvedStream != 2 || this.mController == null) {
                return false;
            }
            long now = SystemClock.uptimeMillis();
            if ((flags & 1) != 0 && !this.mVisible) {
                if (this.mNextLongPress < now) {
                    this.mNextLongPress = ((long) this.mLongPressTimeout) + now;
                }
                return true;
            } else if (this.mNextLongPress <= 0) {
                return false;
            } else {
                if (now <= this.mNextLongPress) {
                    return true;
                }
                this.mNextLongPress = 0;
                return false;
            }
        }

        public void setVisible(boolean visible) {
            this.mVisible = visible;
        }

        public boolean isSameBinder(IVolumeController controller) {
            return Objects.equals(asBinder(), binder(controller));
        }

        public IBinder asBinder() {
            return binder(this.mController);
        }

        private static IBinder binder(IVolumeController controller) {
            if (controller == null) {
                return null;
            }
            return controller.asBinder();
        }

        public String toString() {
            return "VolumeController(" + asBinder() + ",mVisible=" + this.mVisible + ")";
        }

        public void postDisplaySafeVolumeWarning(int flags) {
            if (this.mController != null) {
                try {
                    this.mController.displaySafeVolumeWarning(flags);
                } catch (RemoteException e) {
                    Log.w(TAG, "Error calling displaySafeVolumeWarning", e);
                }
            }
        }

        public void postVolumeChanged(int streamType, int flags) {
            if (this.mController != null) {
                try {
                    this.mController.volumeChanged(streamType, flags);
                } catch (RemoteException e) {
                    Log.w(TAG, "Error calling volumeChanged", e);
                }
            }
        }

        public void postMasterVolumeChanged(int flags) {
            if (this.mController != null) {
                try {
                    this.mController.masterVolumeChanged(flags);
                } catch (RemoteException e) {
                    Log.w(TAG, "Error calling masterVolumeChanged", e);
                }
            }
        }

        public void postMasterMuteChanged(int flags) {
            if (this.mController != null) {
                try {
                    this.mController.masterMuteChanged(flags);
                } catch (RemoteException e) {
                    Log.w(TAG, "Error calling masterMuteChanged", e);
                }
            }
        }

        public void setLayoutDirection(int layoutDirection) {
            if (this.mController != null) {
                try {
                    this.mController.setLayoutDirection(layoutDirection);
                } catch (RemoteException e) {
                    Log.w(TAG, "Error calling setLayoutDirection", e);
                }
            }
        }

        public void postDismiss() {
            if (this.mController != null) {
                try {
                    this.mController.dismiss();
                } catch (RemoteException e) {
                    Log.w(TAG, "Error calling dismiss", e);
                }
            }
        }
    }

    final class AudioServiceInternal extends AudioManagerInternal {
        AudioServiceInternal() {
        }

        @Override // android.media.AudioManagerInternal
        public void adjustSuggestedStreamVolumeForUid(int streamType, int direction, int flags, String callingPackage, int uid) {
            AudioService.this.adjustSuggestedStreamVolume(direction, streamType, flags, callingPackage, uid);
        }

        @Override // android.media.AudioManagerInternal
        public void adjustStreamVolumeForUid(int streamType, int direction, int flags, String callingPackage, int uid) {
            AudioService.this.adjustStreamVolume(streamType, direction, flags, callingPackage, uid);
        }

        @Override // android.media.AudioManagerInternal
        public void setStreamVolumeForUid(int streamType, int direction, int flags, String callingPackage, int uid) {
            AudioService.this.setStreamVolume(streamType, direction, flags, callingPackage, uid);
        }
    }

    @Override // android.media.IAudioService
    public boolean registerAudioPolicy(AudioPolicyConfig policyConfig, IBinder cb) {
        boolean hasPermissionForPolicy;
        if (this.mContext.checkCallingOrSelfPermission(Manifest.permission.MODIFY_AUDIO_ROUTING) == 0) {
            hasPermissionForPolicy = true;
        } else {
            hasPermissionForPolicy = false;
        }
        if (!hasPermissionForPolicy) {
            Slog.w(TAG, "Can't register audio policy for pid " + Binder.getCallingPid() + " / uid " + Binder.getCallingUid() + ", need MODIFY_AUDIO_ROUTING");
            return false;
        }
        synchronized (this.mAudioPolicies) {
            AudioPolicyProxy app = new AudioPolicyProxy(policyConfig, cb);
            try {
                cb.linkToDeath(app, 0);
                this.mAudioPolicies.put(cb, app);
            } catch (RemoteException e) {
                Slog.w(TAG, "Audio policy registration failed, could not link to " + cb + " binder death", e);
                return false;
            }
        }
        return true;
    }

    @Override // android.media.IAudioService
    public void unregisterAudioPolicyAsync(IBinder cb) {
        synchronized (this.mAudioPolicies) {
            AudioPolicyProxy app = this.mAudioPolicies.remove(cb);
            if (app == null) {
                Slog.w(TAG, "Trying to unregister unknown audio policy for pid " + Binder.getCallingPid() + " / uid " + Binder.getCallingUid());
            } else {
                cb.unlinkToDeath(app, 0);
            }
        }
    }

    public class AudioPolicyProxy implements IBinder.DeathRecipient {
        private static final String TAG = "AudioPolicyProxy";
        AudioPolicyConfig mConfig;
        IBinder mToken;

        AudioPolicyProxy(AudioPolicyConfig config, IBinder token) {
            this.mConfig = config;
            this.mToken = token;
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            synchronized (AudioService.this.mAudioPolicies) {
                Log.v(TAG, "audio policy " + this.mToken + " died");
                AudioService.this.mAudioPolicies.remove(this.mToken);
            }
        }
    }
}

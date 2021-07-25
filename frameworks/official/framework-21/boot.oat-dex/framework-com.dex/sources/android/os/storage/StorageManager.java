package android.os.storage;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.storage.IMountService;
import android.os.storage.IMountServiceListener;
import android.os.storage.IObbActionListener;
import android.provider.Settings;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.util.Preconditions;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class StorageManager {
    public static final int CRYPT_TYPE_DEFAULT = 1;
    public static final int CRYPT_TYPE_PASSWORD = 0;
    public static final int CRYPT_TYPE_PATTERN = 2;
    public static final int CRYPT_TYPE_PIN = 3;
    private static final long DEFAULT_FULL_THRESHOLD_BYTES = 1048576;
    private static final long DEFAULT_THRESHOLD_MAX_BYTES = 524288000;
    private static final int DEFAULT_THRESHOLD_PERCENTAGE = 10;
    public static final String OWNER_INFO_KEY = "OwnerInfo";
    public static final String PATTERN_VISIBLE_KEY = "PatternVisible";
    public static final String SYSTEM_LOCALE_KEY = "SystemLocale";
    private static final String TAG = "StorageManager";
    private MountServiceBinderListener mBinderListener;
    private List<ListenerDelegate> mListeners = new ArrayList();
    private final IMountService mMountService;
    private final AtomicInteger mNextNonce = new AtomicInteger(0);
    private final ObbActionListener mObbActionListener = new ObbActionListener();
    private final ContentResolver mResolver;
    private final Looper mTgtLooper;

    private class MountServiceBinderListener extends IMountServiceListener.Stub {
        private MountServiceBinderListener() {
        }

        @Override // android.os.storage.IMountServiceListener
        public void onUsbMassStorageConnectionChanged(boolean available) {
            int size = StorageManager.this.mListeners.size();
            for (int i = 0; i < size; i++) {
                ((ListenerDelegate) StorageManager.this.mListeners.get(i)).sendShareAvailabilityChanged(available);
            }
        }

        @Override // android.os.storage.IMountServiceListener
        public void onStorageStateChanged(String path, String oldState, String newState) {
            int size = StorageManager.this.mListeners.size();
            for (int i = 0; i < size; i++) {
                ((ListenerDelegate) StorageManager.this.mListeners.get(i)).sendStorageStateChanged(path, oldState, newState);
            }
        }
    }

    private class ObbActionListener extends IObbActionListener.Stub {
        private SparseArray<ObbListenerDelegate> mListeners;

        private ObbActionListener() {
            this.mListeners = new SparseArray<>();
        }

        @Override // android.os.storage.IObbActionListener
        public void onObbResult(String filename, int nonce, int status) {
            ObbListenerDelegate delegate;
            synchronized (this.mListeners) {
                delegate = this.mListeners.get(nonce);
                if (delegate != null) {
                    this.mListeners.remove(nonce);
                }
            }
            if (delegate != null) {
                delegate.sendObbStateChanged(filename, status);
            }
        }

        public int addListener(OnObbStateChangeListener listener) {
            ObbListenerDelegate delegate = new ObbListenerDelegate(listener);
            synchronized (this.mListeners) {
                this.mListeners.put(delegate.nonce, delegate);
            }
            return delegate.nonce;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getNextNonce() {
        return this.mNextNonce.getAndIncrement();
    }

    /* access modifiers changed from: private */
    public class ObbListenerDelegate {
        private final Handler mHandler;
        private final WeakReference<OnObbStateChangeListener> mObbEventListenerRef;
        private final int nonce;

        ObbListenerDelegate(OnObbStateChangeListener listener) {
            this.nonce = StorageManager.this.getNextNonce();
            this.mObbEventListenerRef = new WeakReference<>(listener);
            this.mHandler = new Handler(StorageManager.this.mTgtLooper, StorageManager.this) {
                /* class android.os.storage.StorageManager.ObbListenerDelegate.AnonymousClass1 */

                @Override // android.os.Handler
                public void handleMessage(Message msg) {
                    OnObbStateChangeListener changeListener = ObbListenerDelegate.this.getListener();
                    if (changeListener != null) {
                        StorageEvent e = (StorageEvent) msg.obj;
                        if (msg.what == 3) {
                            ObbStateChangedStorageEvent ev = (ObbStateChangedStorageEvent) e;
                            changeListener.onObbStateChange(ev.path, ev.state);
                            return;
                        }
                        Log.e(StorageManager.TAG, "Unsupported event " + msg.what);
                    }
                }
            };
        }

        /* access modifiers changed from: package-private */
        public OnObbStateChangeListener getListener() {
            if (this.mObbEventListenerRef == null) {
                return null;
            }
            return this.mObbEventListenerRef.get();
        }

        /* access modifiers changed from: package-private */
        public void sendObbStateChanged(String path, int state) {
            this.mHandler.sendMessage(new ObbStateChangedStorageEvent(path, state).getMessage());
        }
    }

    /* access modifiers changed from: private */
    public class ObbStateChangedStorageEvent extends StorageEvent {
        public final String path;
        public final int state;

        public ObbStateChangedStorageEvent(String path2, int state2) {
            super(3);
            this.path = path2;
            this.state = state2;
        }
    }

    private class StorageEvent {
        static final int EVENT_OBB_STATE_CHANGED = 3;
        static final int EVENT_STORAGE_STATE_CHANGED = 2;
        static final int EVENT_UMS_CONNECTION_CHANGED = 1;
        private Message mMessage = Message.obtain();

        public StorageEvent(int what) {
            this.mMessage.what = what;
            this.mMessage.obj = this;
        }

        public Message getMessage() {
            return this.mMessage;
        }
    }

    /* access modifiers changed from: private */
    public class UmsConnectionChangedStorageEvent extends StorageEvent {
        public boolean available;

        public UmsConnectionChangedStorageEvent(boolean a) {
            super(1);
            this.available = a;
        }
    }

    /* access modifiers changed from: private */
    public class StorageStateChangedStorageEvent extends StorageEvent {
        public String newState;
        public String oldState;
        public String path;

        public StorageStateChangedStorageEvent(String p, String oldS, String newS) {
            super(2);
            this.path = p;
            this.oldState = oldS;
            this.newState = newS;
        }
    }

    /* access modifiers changed from: private */
    public class ListenerDelegate {
        private final Handler mHandler;
        final StorageEventListener mStorageEventListener;

        ListenerDelegate(StorageEventListener listener) {
            this.mStorageEventListener = listener;
            this.mHandler = new Handler(StorageManager.this.mTgtLooper, StorageManager.this) {
                /* class android.os.storage.StorageManager.ListenerDelegate.AnonymousClass1 */

                @Override // android.os.Handler
                public void handleMessage(Message msg) {
                    StorageEvent e = (StorageEvent) msg.obj;
                    if (msg.what == 1) {
                        ListenerDelegate.this.mStorageEventListener.onUsbMassStorageConnectionChanged(((UmsConnectionChangedStorageEvent) e).available);
                    } else if (msg.what == 2) {
                        StorageStateChangedStorageEvent ev = (StorageStateChangedStorageEvent) e;
                        ListenerDelegate.this.mStorageEventListener.onStorageStateChanged(ev.path, ev.oldState, ev.newState);
                    } else {
                        Log.e(StorageManager.TAG, "Unsupported event " + msg.what);
                    }
                }
            };
        }

        /* access modifiers changed from: package-private */
        public StorageEventListener getListener() {
            return this.mStorageEventListener;
        }

        /* access modifiers changed from: package-private */
        public void sendShareAvailabilityChanged(boolean available) {
            this.mHandler.sendMessage(new UmsConnectionChangedStorageEvent(available).getMessage());
        }

        /* access modifiers changed from: package-private */
        public void sendStorageStateChanged(String path, String oldState, String newState) {
            this.mHandler.sendMessage(new StorageStateChangedStorageEvent(path, oldState, newState).getMessage());
        }
    }

    public static StorageManager from(Context context) {
        return (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
    }

    public StorageManager(ContentResolver resolver, Looper tgtLooper) throws RemoteException {
        this.mResolver = resolver;
        this.mTgtLooper = tgtLooper;
        this.mMountService = IMountService.Stub.asInterface(ServiceManager.getService("mount"));
        if (this.mMountService == null) {
            Log.e(TAG, "Unable to connect to mount service! - is it running yet?");
        }
    }

    public void registerListener(StorageEventListener listener) {
        if (listener != null) {
            synchronized (this.mListeners) {
                if (this.mBinderListener == null) {
                    try {
                        this.mBinderListener = new MountServiceBinderListener();
                        this.mMountService.registerListener(this.mBinderListener);
                    } catch (RemoteException e) {
                        Log.e(TAG, "Register mBinderListener failed");
                        return;
                    }
                }
                this.mListeners.add(new ListenerDelegate(listener));
            }
        }
    }

    public void unregisterListener(StorageEventListener listener) {
        if (listener != null) {
            synchronized (this.mListeners) {
                int size = this.mListeners.size();
                int i = 0;
                while (true) {
                    if (i >= size) {
                        break;
                    } else if (this.mListeners.get(i).getListener() == listener) {
                        this.mListeners.remove(i);
                        break;
                    } else {
                        i++;
                    }
                }
                if (this.mListeners.size() == 0 && this.mBinderListener != null) {
                    try {
                        this.mMountService.unregisterListener(this.mBinderListener);
                    } catch (RemoteException e) {
                        Log.e(TAG, "Unregister mBinderListener failed");
                    }
                }
            }
        }
    }

    public void enableUsbMassStorage() {
        try {
            this.mMountService.setUsbMassStorageEnabled(true);
        } catch (Exception ex) {
            Log.e(TAG, "Failed to enable UMS", ex);
        }
    }

    public void disableUsbMassStorage() {
        try {
            this.mMountService.setUsbMassStorageEnabled(false);
        } catch (Exception ex) {
            Log.e(TAG, "Failed to disable UMS", ex);
        }
    }

    public boolean isUsbMassStorageConnected() {
        try {
            return this.mMountService.isUsbMassStorageConnected();
        } catch (Exception ex) {
            Log.e(TAG, "Failed to get UMS connection state", ex);
            return false;
        }
    }

    public boolean isUsbMassStorageEnabled() {
        try {
            return this.mMountService.isUsbMassStorageEnabled();
        } catch (RemoteException rex) {
            Log.e(TAG, "Failed to get UMS enable state", rex);
            return false;
        }
    }

    public boolean mountObb(String rawPath, String key, OnObbStateChangeListener listener) {
        Preconditions.checkNotNull(rawPath, "rawPath cannot be null");
        Preconditions.checkNotNull(listener, "listener cannot be null");
        try {
            this.mMountService.mountObb(rawPath, new File(rawPath).getCanonicalPath(), key, this.mObbActionListener, this.mObbActionListener.addListener(listener));
            return true;
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to resolve path: " + rawPath, e);
        } catch (RemoteException e2) {
            Log.e(TAG, "Failed to mount OBB", e2);
            return false;
        }
    }

    public boolean unmountObb(String rawPath, boolean force, OnObbStateChangeListener listener) {
        Preconditions.checkNotNull(rawPath, "rawPath cannot be null");
        Preconditions.checkNotNull(listener, "listener cannot be null");
        try {
            this.mMountService.unmountObb(rawPath, force, this.mObbActionListener, this.mObbActionListener.addListener(listener));
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to mount OBB", e);
            return false;
        }
    }

    public boolean isObbMounted(String rawPath) {
        Preconditions.checkNotNull(rawPath, "rawPath cannot be null");
        try {
            return this.mMountService.isObbMounted(rawPath);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to check if OBB is mounted", e);
            return false;
        }
    }

    public String getMountedObbPath(String rawPath) {
        Preconditions.checkNotNull(rawPath, "rawPath cannot be null");
        try {
            return this.mMountService.getMountedObbPath(rawPath);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to find mounted path for OBB", e);
            return null;
        }
    }

    public String getVolumeState(String mountPoint) {
        if (this.mMountService == null) {
            return Environment.MEDIA_REMOVED;
        }
        try {
            return this.mMountService.getVolumeState(mountPoint);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to get volume state", e);
            return null;
        }
    }

    public StorageVolume[] getVolumeList() {
        if (this.mMountService == null) {
            return new StorageVolume[0];
        }
        try {
            Parcelable[] list = this.mMountService.getVolumeList();
            if (list == null) {
                return new StorageVolume[0];
            }
            int length = list.length;
            StorageVolume[] result = new StorageVolume[length];
            for (int i = 0; i < length; i++) {
                result[i] = (StorageVolume) list[i];
            }
            return result;
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to get volume list", e);
            return null;
        }
    }

    public String[] getVolumePaths() {
        StorageVolume[] volumes = getVolumeList();
        if (volumes == null) {
            return null;
        }
        int count = volumes.length;
        String[] paths = new String[count];
        for (int i = 0; i < count; i++) {
            paths[i] = volumes[i].getPath();
        }
        return paths;
    }

    public StorageVolume getPrimaryVolume() {
        return getPrimaryVolume(getVolumeList());
    }

    public static StorageVolume getPrimaryVolume(StorageVolume[] volumes) {
        for (StorageVolume volume : volumes) {
            if (volume.isPrimary()) {
                return volume;
            }
        }
        Log.w(TAG, "No primary storage defined");
        return null;
    }

    public long getStorageBytesUntilLow(File path) {
        return path.getUsableSpace() - getStorageFullBytes(path);
    }

    public long getStorageLowBytes(File path) {
        return Math.min((path.getTotalSpace() * ((long) Settings.Global.getInt(this.mResolver, Settings.Global.SYS_STORAGE_THRESHOLD_PERCENTAGE, 10))) / 100, Settings.Global.getLong(this.mResolver, Settings.Global.SYS_STORAGE_THRESHOLD_MAX_BYTES, DEFAULT_THRESHOLD_MAX_BYTES));
    }

    public long getStorageFullBytes(File path) {
        return Settings.Global.getLong(this.mResolver, Settings.Global.SYS_STORAGE_FULL_THRESHOLD_BYTES, 1048576);
    }
}

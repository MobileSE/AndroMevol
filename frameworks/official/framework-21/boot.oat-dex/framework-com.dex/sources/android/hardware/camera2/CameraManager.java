package android.hardware.camera2;

import android.content.Context;
import android.hardware.CameraInfo;
import android.hardware.ICameraService;
import android.hardware.ICameraServiceListener;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.ICameraDeviceUser;
import android.hardware.camera2.impl.CameraDeviceImpl;
import android.hardware.camera2.impl.CameraMetadataNative;
import android.hardware.camera2.legacy.CameraDeviceUserShim;
import android.hardware.camera2.legacy.LegacyMetadataMapper;
import android.hardware.camera2.utils.BinderHolder;
import android.hardware.camera2.utils.CameraRuntimeException;
import android.hardware.camera2.utils.CameraServiceBinderDecorator;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.ArrayMap;
import android.util.Log;
import java.util.ArrayList;
import java.util.Iterator;

public final class CameraManager {
    private static final int API_VERSION_1 = 1;
    private static final int API_VERSION_2 = 2;
    private static final String CAMERA_SERVICE_BINDER_NAME = "media.camera";
    private static final String TAG = "CameraManager";
    private static final int USE_CALLING_UID = -1;
    private final boolean DEBUG = Log.isLoggable(TAG, 3);
    private final ArrayMap<AvailabilityCallback, Handler> mCallbackMap = new ArrayMap<>();
    private ICameraService mCameraService;
    private final Context mContext;
    private ArrayList<String> mDeviceIdList;
    private final Object mLock = new Object();
    private final CameraServiceListener mServiceListener = new CameraServiceListener();

    public static abstract class AvailabilityListener extends AvailabilityCallback {
    }

    public CameraManager(Context context) {
        synchronized (this.mLock) {
            this.mContext = context;
            connectCameraServiceLocked();
        }
    }

    public String[] getCameraIdList() throws CameraAccessException {
        String[] strArr;
        synchronized (this.mLock) {
            strArr = (String[]) getOrCreateDeviceIdListLocked().toArray(new String[0]);
        }
        return strArr;
    }

    public void registerAvailabilityCallback(AvailabilityCallback callback, Handler handler) {
        if (handler == null) {
            Looper looper = Looper.myLooper();
            if (looper == null) {
                throw new IllegalArgumentException("No handler given, and current thread has no looper!");
            }
            handler = new Handler(looper);
        }
        synchronized (this.mLock) {
            if (this.mCallbackMap.put(callback, handler) == null) {
                this.mServiceListener.updateCallbackLocked(callback, handler);
            }
        }
    }

    public void unregisterAvailabilityCallback(AvailabilityCallback callback) {
        synchronized (this.mLock) {
            this.mCallbackMap.remove(callback);
        }
    }

    public CameraCharacteristics getCameraCharacteristics(String cameraId) throws CameraAccessException {
        CameraCharacteristics characteristics;
        synchronized (this.mLock) {
            if (!getOrCreateDeviceIdListLocked().contains(cameraId)) {
                throw new IllegalArgumentException(String.format("Camera id %s does not match any currently connected camera device", cameraId));
            }
            int id = Integer.valueOf(cameraId).intValue();
            ICameraService cameraService = getCameraServiceLocked();
            if (cameraService == null) {
                throw new CameraAccessException(2, "Camera service is currently unavailable");
            }
            try {
                if (!supportsCamera2ApiLocked(cameraId)) {
                    String[] outParameters = new String[1];
                    cameraService.getLegacyParameters(id, outParameters);
                    String parameters = outParameters[0];
                    CameraInfo info = new CameraInfo();
                    cameraService.getCameraInfo(id, info);
                    characteristics = LegacyMetadataMapper.createCharacteristics(parameters, info);
                } else {
                    CameraMetadataNative info2 = new CameraMetadataNative();
                    cameraService.getCameraCharacteristics(id, info2);
                    characteristics = new CameraCharacteristics(info2);
                }
            } catch (CameraRuntimeException e) {
                throw e.asChecked();
            } catch (RemoteException e2) {
                throw new CameraAccessException(2, "Camera service is currently unavailable", e2);
            }
        }
        return characteristics;
    }

    private CameraDevice openCameraDeviceUserAsync(String cameraId, CameraDevice.StateCallback callback, Handler handler) throws CameraAccessException {
        CameraDeviceImpl deviceImpl;
        CameraCharacteristics characteristics = getCameraCharacteristics(cameraId);
        try {
            synchronized (this.mLock) {
                ICameraDeviceUser cameraUser = null;
                deviceImpl = new CameraDeviceImpl(cameraId, callback, handler, characteristics);
                BinderHolder holder = new BinderHolder();
                ICameraDeviceCallbacks callbacks = deviceImpl.getCallbacks();
                int id = Integer.parseInt(cameraId);
                try {
                    if (supportsCamera2ApiLocked(cameraId)) {
                        ICameraService cameraService = getCameraServiceLocked();
                        if (cameraService == null) {
                            throw new CameraRuntimeException(2, "Camera service is currently unavailable");
                        }
                        cameraService.connectDevice(callbacks, id, this.mContext.getPackageName(), -1, holder);
                        cameraUser = ICameraDeviceUser.Stub.asInterface(holder.getBinder());
                    } else {
                        Log.i(TAG, "Using legacy camera HAL.");
                        cameraUser = CameraDeviceUserShim.connectBinderShim(callbacks, id);
                    }
                } catch (CameraRuntimeException e) {
                    if (e.getReason() == 1000) {
                        throw new AssertionError("Should've gone down the shim path");
                    } else if (e.getReason() == 4 || e.getReason() == 5 || e.getReason() == 1 || e.getReason() == 2 || e.getReason() == 3) {
                        deviceImpl.setRemoteFailure(e);
                        if (e.getReason() == 1 || e.getReason() == 2) {
                            throw e.asChecked();
                        }
                    } else {
                        throw e;
                    }
                } catch (RemoteException e2) {
                    CameraRuntimeException ce = new CameraRuntimeException(2, "Camera service is currently unavailable", e2);
                    deviceImpl.setRemoteFailure(ce);
                    throw ce.asChecked();
                }
                deviceImpl.setRemoteDevice(cameraUser);
            }
            return deviceImpl;
        } catch (NumberFormatException e3) {
            throw new IllegalArgumentException("Expected cameraId to be numeric, but it was: " + cameraId);
        } catch (CameraRuntimeException e4) {
            throw e4.asChecked();
        }
    }

    public void openCamera(String cameraId, CameraDevice.StateCallback callback, Handler handler) throws CameraAccessException {
        if (cameraId == null) {
            throw new IllegalArgumentException("cameraId was null");
        } else if (callback == null) {
            throw new IllegalArgumentException("callback was null");
        } else {
            if (handler == null) {
                if (Looper.myLooper() != null) {
                    handler = new Handler();
                } else {
                    throw new IllegalArgumentException("Looper doesn't exist in the calling thread");
                }
            }
            openCameraDeviceUserAsync(cameraId, callback, handler);
        }
    }

    public static abstract class AvailabilityCallback {
        public void onCameraAvailable(String cameraId) {
        }

        public void onCameraUnavailable(String cameraId) {
        }
    }

    private ArrayList<String> getOrCreateDeviceIdListLocked() throws CameraAccessException {
        if (this.mDeviceIdList == null) {
            ICameraService cameraService = getCameraServiceLocked();
            ArrayList<String> deviceIdList = new ArrayList<>();
            if (cameraService == null) {
                return deviceIdList;
            }
            try {
                int numCameras = cameraService.getNumberOfCameras();
                CameraMetadataNative info = new CameraMetadataNative();
                for (int i = 0; i < numCameras; i++) {
                    boolean isDeviceSupported = false;
                    try {
                        cameraService.getCameraCharacteristics(i, info);
                        if (!info.isEmpty()) {
                            isDeviceSupported = true;
                            if (isDeviceSupported) {
                                deviceIdList.add(String.valueOf(i));
                            } else {
                                Log.w(TAG, "Error querying camera device " + i + " for listing.");
                            }
                        } else {
                            throw new AssertionError("Expected to get non-empty characteristics");
                        }
                    } catch (IllegalArgumentException e) {
                    } catch (CameraRuntimeException e2) {
                        if (e2.getReason() != 2) {
                            throw e2.asChecked();
                        }
                    } catch (RemoteException e3) {
                        deviceIdList.clear();
                        return deviceIdList;
                    }
                }
                this.mDeviceIdList = deviceIdList;
            } catch (CameraRuntimeException e4) {
                throw e4.asChecked();
            } catch (RemoteException e5) {
                return deviceIdList;
            }
        }
        return this.mDeviceIdList;
    }

    private void handleRecoverableSetupErrors(CameraRuntimeException e, String msg) {
        int problem = e.getReason();
        switch (problem) {
            case 2:
                Log.w(TAG, msg + ": " + CameraAccessException.getDefaultMessage(problem));
                return;
            default:
                throw new IllegalStateException(msg, e.asChecked());
        }
    }

    private boolean supportsCamera2ApiLocked(String cameraId) {
        return supportsCameraApiLocked(cameraId, 2);
    }

    private boolean supportsCameraApiLocked(String cameraId, int apiVersion) {
        int id = Integer.parseInt(cameraId);
        try {
            ICameraService cameraService = getCameraServiceLocked();
            if (cameraService == null) {
                return false;
            }
            int res = cameraService.supportsCameraApi(id, apiVersion);
            if (res == 0) {
                return true;
            }
            throw new AssertionError("Unexpected value " + res);
        } catch (CameraRuntimeException e) {
            if (e.getReason() == 1000) {
                return false;
            }
            throw e;
        } catch (RemoteException e2) {
            return false;
        }
    }

    private void connectCameraServiceLocked() {
        this.mCameraService = null;
        IBinder cameraServiceBinder = ServiceManager.getService(CAMERA_SERVICE_BINDER_NAME);
        if (cameraServiceBinder != null) {
            try {
                cameraServiceBinder.linkToDeath(new CameraServiceDeathListener(), 0);
                ICameraService cameraService = (ICameraService) CameraServiceBinderDecorator.newInstance(ICameraService.Stub.asInterface(cameraServiceBinder));
                try {
                    CameraServiceBinderDecorator.throwOnError(CameraMetadataNative.nativeSetupGlobalVendorTagDescriptor());
                } catch (CameraRuntimeException e) {
                    handleRecoverableSetupErrors(e, "Failed to set up vendor tags");
                }
                try {
                    cameraService.addListener(this.mServiceListener);
                    this.mCameraService = cameraService;
                } catch (CameraRuntimeException e2) {
                    throw new IllegalStateException("Failed to register a camera service listener", e2.asChecked());
                } catch (RemoteException e3) {
                }
            } catch (RemoteException e4) {
            }
        }
    }

    private ICameraService getCameraServiceLocked() {
        if (this.mCameraService == null) {
            Log.i(TAG, "getCameraServiceLocked: Reconnecting to camera service");
            connectCameraServiceLocked();
            if (this.mCameraService == null) {
                Log.e(TAG, "Camera service is unavailable");
            }
        }
        return this.mCameraService;
    }

    /* access modifiers changed from: private */
    public class CameraServiceDeathListener implements IBinder.DeathRecipient {
        private CameraServiceDeathListener() {
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            synchronized (CameraManager.this.mLock) {
                CameraManager.this.mCameraService = null;
                Iterator i$ = CameraManager.this.mDeviceIdList.iterator();
                while (i$.hasNext()) {
                    CameraManager.this.mServiceListener.onStatusChangedLocked(1, (String) i$.next());
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public class CameraServiceListener extends ICameraServiceListener.Stub {
        public static final int STATUS_ENUMERATING = 2;
        public static final int STATUS_NOT_AVAILABLE = Integer.MIN_VALUE;
        public static final int STATUS_NOT_PRESENT = 0;
        public static final int STATUS_PRESENT = 1;
        private static final String TAG = "CameraServiceListener";
        private final ArrayMap<String, Integer> mDeviceStatus;

        private CameraServiceListener() {
            this.mDeviceStatus = new ArrayMap<>();
        }

        @Override // android.hardware.ICameraServiceListener.Stub, android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        private boolean isAvailable(int status) {
            switch (status) {
                case 1:
                    return true;
                default:
                    return false;
            }
        }

        private boolean validStatus(int status) {
            switch (status) {
                case Integer.MIN_VALUE:
                case 0:
                case 1:
                case 2:
                    return true;
                default:
                    return false;
            }
        }

        private void postSingleUpdate(final AvailabilityCallback callback, Handler handler, final String id, int status) {
            if (isAvailable(status)) {
                handler.post(new Runnable() {
                    /* class android.hardware.camera2.CameraManager.CameraServiceListener.AnonymousClass1 */

                    public void run() {
                        callback.onCameraAvailable(id);
                    }
                });
            } else {
                handler.post(new Runnable() {
                    /* class android.hardware.camera2.CameraManager.CameraServiceListener.AnonymousClass2 */

                    public void run() {
                        callback.onCameraUnavailable(id);
                    }
                });
            }
        }

        public void updateCallbackLocked(AvailabilityCallback callback, Handler handler) {
            for (int i = 0; i < this.mDeviceStatus.size(); i++) {
                postSingleUpdate(callback, handler, this.mDeviceStatus.keyAt(i), this.mDeviceStatus.valueAt(i).intValue());
            }
        }

        @Override // android.hardware.ICameraServiceListener
        public void onStatusChanged(int status, int cameraId) throws RemoteException {
            synchronized (CameraManager.this.mLock) {
                onStatusChangedLocked(status, String.valueOf(cameraId));
            }
        }

        public void onStatusChangedLocked(int status, String id) {
            if (CameraManager.this.DEBUG) {
                Log.v(TAG, String.format("Camera id %s has status changed to 0x%x", id, Integer.valueOf(status)));
            }
            if (!validStatus(status)) {
                Log.e(TAG, String.format("Ignoring invalid device %s status 0x%x", id, Integer.valueOf(status)));
                return;
            }
            Integer oldStatus = this.mDeviceStatus.put(id, Integer.valueOf(status));
            if (oldStatus == null || oldStatus.intValue() != status) {
                if (oldStatus == null || isAvailable(status) != isAvailable(oldStatus.intValue())) {
                    int callbackCount = CameraManager.this.mCallbackMap.size();
                    for (int i = 0; i < callbackCount; i++) {
                        postSingleUpdate((AvailabilityCallback) CameraManager.this.mCallbackMap.keyAt(i), (Handler) CameraManager.this.mCallbackMap.valueAt(i), id, status);
                    }
                } else if (CameraManager.this.DEBUG) {
                    Log.v(TAG, String.format("Device status was previously available (%d),  and is now again available (%d)so no new client visible update will be sent", Boolean.valueOf(isAvailable(status)), Boolean.valueOf(isAvailable(status))));
                }
            } else if (CameraManager.this.DEBUG) {
                Log.v(TAG, String.format("Device status changed to 0x%x, which is what it already was", Integer.valueOf(status)));
            }
        }
    }
}

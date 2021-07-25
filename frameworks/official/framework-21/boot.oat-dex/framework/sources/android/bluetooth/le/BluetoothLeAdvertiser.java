package android.bluetooth.le;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCallbackWrapper;
import android.bluetooth.BluetoothUuid;
import android.bluetooth.IBluetoothGatt;
import android.bluetooth.IBluetoothManager;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.os.RemoteException;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;

public final class BluetoothLeAdvertiser {
    private static final int FLAGS_FIELD_BYTES = 3;
    private static final int MANUFACTURER_SPECIFIC_DATA_LENGTH = 2;
    private static final int MAX_ADVERTISING_DATA_BYTES = 31;
    private static final int OVERHEAD_BYTES_PER_FIELD = 2;
    private static final int SERVICE_DATA_UUID_LENGTH = 2;
    private static final String TAG = "BluetoothLeAdvertiser";
    private BluetoothAdapter mBluetoothAdapter;
    private final IBluetoothManager mBluetoothManager;
    private final Handler mHandler;
    private final Map<AdvertiseCallback, AdvertiseCallbackWrapper> mLeAdvertisers = new HashMap();

    public BluetoothLeAdvertiser(IBluetoothManager bluetoothManager) {
        this.mBluetoothManager = bluetoothManager;
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mHandler = new Handler(Looper.getMainLooper());
    }

    public void startAdvertising(AdvertiseSettings settings, AdvertiseData advertiseData, AdvertiseCallback callback) {
        startAdvertising(settings, advertiseData, null, callback);
    }

    public void startAdvertising(AdvertiseSettings settings, AdvertiseData advertiseData, AdvertiseData scanResponse, AdvertiseCallback callback) {
        synchronized (this.mLeAdvertisers) {
            BluetoothLeUtils.checkAdapterStateOn(this.mBluetoothAdapter);
            if (callback == null) {
                throw new IllegalArgumentException("callback cannot be null");
            } else if (!this.mBluetoothAdapter.isMultipleAdvertisementSupported()) {
                postStartFailure(callback, 5);
            } else if (totalBytes(advertiseData) > 31 || totalBytes(scanResponse) > 31) {
                postStartFailure(callback, 1);
            } else if (this.mLeAdvertisers.containsKey(callback)) {
                postStartFailure(callback, 3);
            } else {
                try {
                    new AdvertiseCallbackWrapper(callback, advertiseData, scanResponse, settings, this.mBluetoothManager.getBluetoothGatt()).startRegisteration();
                } catch (RemoteException e) {
                    Log.e(TAG, "Failed to get Bluetooth gatt - ", e);
                    postStartFailure(callback, 4);
                }
            }
        }
    }

    public void stopAdvertising(AdvertiseCallback callback) {
        synchronized (this.mLeAdvertisers) {
            BluetoothLeUtils.checkAdapterStateOn(this.mBluetoothAdapter);
            if (callback == null) {
                throw new IllegalArgumentException("callback cannot be null");
            }
            AdvertiseCallbackWrapper wrapper = this.mLeAdvertisers.get(callback);
            if (wrapper != null) {
                wrapper.stopAdvertising();
            }
        }
    }

    public void cleanup() {
        this.mLeAdvertisers.clear();
    }

    private int totalBytes(AdvertiseData data) {
        if (data == null) {
            return 0;
        }
        int size = 3;
        if (data.getServiceUuids() != null) {
            int num16BitUuids = 0;
            int num32BitUuids = 0;
            int num128BitUuids = 0;
            for (ParcelUuid uuid : data.getServiceUuids()) {
                if (BluetoothUuid.is16BitUuid(uuid)) {
                    num16BitUuids++;
                } else if (BluetoothUuid.is32BitUuid(uuid)) {
                    num32BitUuids++;
                } else {
                    num128BitUuids++;
                }
            }
            if (num16BitUuids != 0) {
                size = 3 + (num16BitUuids * 2) + 2;
            }
            if (num32BitUuids != 0) {
                size += (num32BitUuids * 4) + 2;
            }
            if (num128BitUuids != 0) {
                size += (num128BitUuids * 16) + 2;
            }
        }
        for (ParcelUuid uuid2 : data.getServiceData().keySet()) {
            size += byteLength(data.getServiceData().get(uuid2)) + 4;
        }
        for (int i = 0; i < data.getManufacturerSpecificData().size(); i++) {
            size += byteLength(data.getManufacturerSpecificData().valueAt(i)) + 4;
        }
        if (data.getIncludeTxPowerLevel()) {
            size += 3;
        }
        if (!data.getIncludeDeviceName() || this.mBluetoothAdapter.getName() == null) {
            return size;
        }
        return size + this.mBluetoothAdapter.getName().length() + 2;
    }

    private int byteLength(byte[] array) {
        if (array == null) {
            return 0;
        }
        return array.length;
    }

    /* access modifiers changed from: private */
    public class AdvertiseCallbackWrapper extends BluetoothGattCallbackWrapper {
        private static final int LE_CALLBACK_TIMEOUT_MILLIS = 2000;
        private final AdvertiseCallback mAdvertiseCallback;
        private final AdvertiseData mAdvertisement;
        private final IBluetoothGatt mBluetoothGatt;
        private int mClientIf;
        private boolean mIsAdvertising = false;
        private final AdvertiseData mScanResponse;
        private final AdvertiseSettings mSettings;

        public AdvertiseCallbackWrapper(AdvertiseCallback advertiseCallback, AdvertiseData advertiseData, AdvertiseData scanResponse, AdvertiseSettings settings, IBluetoothGatt bluetoothGatt) {
            this.mAdvertiseCallback = advertiseCallback;
            this.mAdvertisement = advertiseData;
            this.mScanResponse = scanResponse;
            this.mSettings = settings;
            this.mBluetoothGatt = bluetoothGatt;
            this.mClientIf = 0;
        }

        /* JADX WARNING: Removed duplicated region for block: B:10:0x001f  */
        /* JADX WARNING: Removed duplicated region for block: B:22:0x0041  */
        /* JADX WARNING: Removed duplicated region for block: B:23:0x004a A[SYNTHETIC, Splitter:B:23:0x004a] */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void startRegisteration() {
            /*
                r5 = this;
                r3 = -1
                monitor-enter(r5)
                int r2 = r5.mClientIf     // Catch:{ all -> 0x0030 }
                if (r2 != r3) goto L_0x0008
                monitor-exit(r5)     // Catch:{ all -> 0x0030 }
            L_0x0007:
                return
            L_0x0008:
                java.util.UUID r1 = java.util.UUID.randomUUID()     // Catch:{ InterruptedException -> 0x0033, RemoteException -> 0x005f }
                android.bluetooth.IBluetoothGatt r2 = r5.mBluetoothGatt     // Catch:{ InterruptedException -> 0x0033, RemoteException -> 0x005f }
                android.os.ParcelUuid r3 = new android.os.ParcelUuid     // Catch:{ InterruptedException -> 0x0033, RemoteException -> 0x005f }
                r3.<init>(r1)     // Catch:{ InterruptedException -> 0x0033, RemoteException -> 0x005f }
                r2.registerClient(r3, r5)     // Catch:{ InterruptedException -> 0x0033, RemoteException -> 0x005f }
                r2 = 2000(0x7d0, double:9.88E-321)
                r5.wait(r2)     // Catch:{ InterruptedException -> 0x0033, RemoteException -> 0x005f }
            L_0x001b:
                int r2 = r5.mClientIf
                if (r2 <= 0) goto L_0x003d
                boolean r2 = r5.mIsAdvertising
                if (r2 == 0) goto L_0x003d
                android.bluetooth.le.BluetoothLeAdvertiser r2 = android.bluetooth.le.BluetoothLeAdvertiser.this
                java.util.Map r2 = android.bluetooth.le.BluetoothLeAdvertiser.access$000(r2)
                android.bluetooth.le.AdvertiseCallback r3 = r5.mAdvertiseCallback
                r2.put(r3, r5)
            L_0x002e:
                monitor-exit(r5)
                goto L_0x0007
            L_0x0030:
                r2 = move-exception
                monitor-exit(r5)
                throw r2
            L_0x0033:
                r2 = move-exception
                r0 = r2
            L_0x0035:
                java.lang.String r2 = "BluetoothLeAdvertiser"
                java.lang.String r3 = "Failed to start registeration"
                android.util.Log.e(r2, r3, r0)
                goto L_0x001b
            L_0x003d:
                int r2 = r5.mClientIf
                if (r2 > 0) goto L_0x004a
                android.bluetooth.le.BluetoothLeAdvertiser r2 = android.bluetooth.le.BluetoothLeAdvertiser.this
                android.bluetooth.le.AdvertiseCallback r3 = r5.mAdvertiseCallback
                r4 = 4
                android.bluetooth.le.BluetoothLeAdvertiser.access$100(r2, r3, r4)
                goto L_0x002e
            L_0x004a:
                android.bluetooth.IBluetoothGatt r2 = r5.mBluetoothGatt     // Catch:{ RemoteException -> 0x0055 }
                int r3 = r5.mClientIf     // Catch:{ RemoteException -> 0x0055 }
                r2.unregisterClient(r3)     // Catch:{ RemoteException -> 0x0055 }
                r2 = -1
                r5.mClientIf = r2     // Catch:{ RemoteException -> 0x0055 }
                goto L_0x002e
            L_0x0055:
                r0 = move-exception
                java.lang.String r2 = "BluetoothLeAdvertiser"
                java.lang.String r3 = "remote exception when unregistering"
                android.util.Log.e(r2, r3, r0)
                goto L_0x002e
            L_0x005f:
                r2 = move-exception
                r0 = r2
                goto L_0x0035
            */
            throw new UnsupportedOperationException("Method not decompiled: android.bluetooth.le.BluetoothLeAdvertiser.AdvertiseCallbackWrapper.startRegisteration():void");
        }

        /* JADX WARNING: Removed duplicated region for block: B:6:0x001b A[Catch:{ InterruptedException -> 0x0028, RemoteException -> 0x0035 }] */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void stopAdvertising() {
            /*
                r4 = this;
                monitor-enter(r4)
                android.bluetooth.IBluetoothGatt r1 = r4.mBluetoothGatt     // Catch:{ InterruptedException -> 0x0028, RemoteException -> 0x0035 }
                int r2 = r4.mClientIf     // Catch:{ InterruptedException -> 0x0028, RemoteException -> 0x0035 }
                r1.stopMultiAdvertising(r2)     // Catch:{ InterruptedException -> 0x0028, RemoteException -> 0x0035 }
                r2 = 2000(0x7d0, double:9.88E-321)
                r4.wait(r2)     // Catch:{ InterruptedException -> 0x0028, RemoteException -> 0x0035 }
            L_0x000d:
                android.bluetooth.le.BluetoothLeAdvertiser r1 = android.bluetooth.le.BluetoothLeAdvertiser.this     // Catch:{ all -> 0x0032 }
                java.util.Map r1 = android.bluetooth.le.BluetoothLeAdvertiser.access$000(r1)     // Catch:{ all -> 0x0032 }
                android.bluetooth.le.AdvertiseCallback r2 = r4.mAdvertiseCallback     // Catch:{ all -> 0x0032 }
                boolean r1 = r1.containsKey(r2)     // Catch:{ all -> 0x0032 }
                if (r1 == 0) goto L_0x0026
                android.bluetooth.le.BluetoothLeAdvertiser r1 = android.bluetooth.le.BluetoothLeAdvertiser.this     // Catch:{ all -> 0x0032 }
                java.util.Map r1 = android.bluetooth.le.BluetoothLeAdvertiser.access$000(r1)     // Catch:{ all -> 0x0032 }
                android.bluetooth.le.AdvertiseCallback r2 = r4.mAdvertiseCallback     // Catch:{ all -> 0x0032 }
                r1.remove(r2)     // Catch:{ all -> 0x0032 }
            L_0x0026:
                monitor-exit(r4)     // Catch:{ all -> 0x0032 }
                return
            L_0x0028:
                r1 = move-exception
                r0 = r1
            L_0x002a:
                java.lang.String r1 = "BluetoothLeAdvertiser"
                java.lang.String r2 = "Failed to stop advertising"
                android.util.Log.e(r1, r2, r0)     // Catch:{ all -> 0x0032 }
                goto L_0x000d
            L_0x0032:
                r1 = move-exception
                monitor-exit(r4)     // Catch:{ all -> 0x0032 }
                throw r1
            L_0x0035:
                r1 = move-exception
                r0 = r1
                goto L_0x002a
            */
            throw new UnsupportedOperationException("Method not decompiled: android.bluetooth.le.BluetoothLeAdvertiser.AdvertiseCallbackWrapper.stopAdvertising():void");
        }

        @Override // android.bluetooth.BluetoothGattCallbackWrapper, android.bluetooth.IBluetoothGattCallback
        public void onClientRegistered(int status, int clientIf) {
            Log.d(BluetoothLeAdvertiser.TAG, "onClientRegistered() - status=" + status + " clientIf=" + clientIf);
            synchronized (this) {
                if (status == 0) {
                    this.mClientIf = clientIf;
                    try {
                        this.mBluetoothGatt.startMultiAdvertising(this.mClientIf, this.mAdvertisement, this.mScanResponse, this.mSettings);
                        return;
                    } catch (RemoteException e) {
                        Log.e(BluetoothLeAdvertiser.TAG, "failed to start advertising", e);
                    }
                }
                this.mClientIf = -1;
                notifyAll();
            }
        }

        @Override // android.bluetooth.BluetoothGattCallbackWrapper, android.bluetooth.IBluetoothGattCallback
        public void onMultiAdvertiseCallback(int status, boolean isStart, AdvertiseSettings settings) {
            synchronized (this) {
                if (!isStart) {
                    try {
                        this.mBluetoothGatt.unregisterClient(this.mClientIf);
                        this.mClientIf = -1;
                        this.mIsAdvertising = false;
                        BluetoothLeAdvertiser.this.mLeAdvertisers.remove(this.mAdvertiseCallback);
                    } catch (RemoteException e) {
                        Log.e(BluetoothLeAdvertiser.TAG, "remote exception when unregistering", e);
                    }
                } else if (status == 0) {
                    this.mIsAdvertising = true;
                    BluetoothLeAdvertiser.this.postStartSuccess(this.mAdvertiseCallback, settings);
                } else {
                    BluetoothLeAdvertiser.this.postStartFailure(this.mAdvertiseCallback, status);
                }
                notifyAll();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void postStartFailure(final AdvertiseCallback callback, final int error) {
        this.mHandler.post(new Runnable() {
            /* class android.bluetooth.le.BluetoothLeAdvertiser.AnonymousClass1 */

            public void run() {
                callback.onStartFailure(error);
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void postStartSuccess(final AdvertiseCallback callback, final AdvertiseSettings settings) {
        this.mHandler.post(new Runnable() {
            /* class android.bluetooth.le.BluetoothLeAdvertiser.AnonymousClass2 */

            public void run() {
                callback.onStartSuccess(settings);
            }
        });
    }
}

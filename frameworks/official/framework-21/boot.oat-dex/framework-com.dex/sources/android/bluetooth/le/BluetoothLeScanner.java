package android.bluetooth.le;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCallbackWrapper;
import android.bluetooth.IBluetoothGatt;
import android.bluetooth.IBluetoothManager;
import android.bluetooth.le.ScanSettings;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BluetoothLeScanner {
    private static final boolean DBG = true;
    private static final String TAG = "BluetoothLeScanner";
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private final IBluetoothManager mBluetoothManager;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final Map<ScanCallback, BleScanCallbackWrapper> mLeScanClients = new HashMap();

    public BluetoothLeScanner(IBluetoothManager bluetoothManager) {
        this.mBluetoothManager = bluetoothManager;
    }

    public void startScan(ScanCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("callback is null");
        }
        startScan(null, new ScanSettings.Builder().build(), callback);
    }

    public void startScan(List<ScanFilter> filters, ScanSettings settings, ScanCallback callback) {
        startScan(filters, settings, callback, null);
    }

    private void startScan(List<ScanFilter> filters, ScanSettings settings, ScanCallback callback, List<List<ResultStorageDescriptor>> resultStorages) {
        IBluetoothGatt gatt;
        BluetoothLeUtils.checkAdapterStateOn(this.mBluetoothAdapter);
        if (settings == null || callback == null) {
            throw new IllegalArgumentException("settings or callback is null");
        }
        synchronized (this.mLeScanClients) {
            if (this.mLeScanClients.containsKey(callback)) {
                postCallbackError(callback, 1);
                return;
            }
            try {
                gatt = this.mBluetoothManager.getBluetoothGatt();
            } catch (RemoteException e) {
                gatt = null;
            }
            if (gatt == null) {
                postCallbackError(callback, 3);
            } else if (!isSettingsConfigAllowedForScan(settings)) {
                postCallbackError(callback, 4);
            } else {
                new BleScanCallbackWrapper(gatt, filters, settings, callback, resultStorages).startRegisteration();
            }
        }
    }

    public void stopScan(ScanCallback callback) {
        BluetoothLeUtils.checkAdapterStateOn(this.mBluetoothAdapter);
        synchronized (this.mLeScanClients) {
            BleScanCallbackWrapper wrapper = this.mLeScanClients.remove(callback);
            if (wrapper == null) {
                Log.d(TAG, "could not find callback wrapper");
            } else {
                wrapper.stopLeScan();
            }
        }
    }

    public void flushPendingScanResults(ScanCallback callback) {
        BluetoothLeUtils.checkAdapterStateOn(this.mBluetoothAdapter);
        if (callback == null) {
            throw new IllegalArgumentException("callback cannot be null!");
        }
        synchronized (this.mLeScanClients) {
            BleScanCallbackWrapper wrapper = this.mLeScanClients.get(callback);
            if (wrapper != null) {
                wrapper.flushPendingBatchResults();
            }
        }
    }

    public void startTruncatedScan(List<TruncatedFilter> truncatedFilters, ScanSettings settings, ScanCallback callback) {
        int filterSize = truncatedFilters.size();
        List<ScanFilter> scanFilters = new ArrayList<>(filterSize);
        List<List<ResultStorageDescriptor>> scanStorages = new ArrayList<>(filterSize);
        for (TruncatedFilter filter : truncatedFilters) {
            scanFilters.add(filter.getFilter());
            scanStorages.add(filter.getStorageDescriptors());
        }
        startScan(scanFilters, settings, callback, scanStorages);
    }

    public void cleanup() {
        this.mLeScanClients.clear();
    }

    /* access modifiers changed from: private */
    public class BleScanCallbackWrapper extends BluetoothGattCallbackWrapper {
        private static final int REGISTRATION_CALLBACK_TIMEOUT_MILLIS = 2000;
        private IBluetoothGatt mBluetoothGatt;
        private int mClientIf = 0;
        private final List<ScanFilter> mFilters;
        private List<List<ResultStorageDescriptor>> mResultStorages;
        private final ScanCallback mScanCallback;
        private ScanSettings mSettings;

        public BleScanCallbackWrapper(IBluetoothGatt bluetoothGatt, List<ScanFilter> filters, ScanSettings settings, ScanCallback scanCallback, List<List<ResultStorageDescriptor>> resultStorages) {
            this.mBluetoothGatt = bluetoothGatt;
            this.mFilters = filters;
            this.mSettings = settings;
            this.mScanCallback = scanCallback;
            this.mResultStorages = resultStorages;
        }

        /* JADX WARNING: Removed duplicated region for block: B:17:0x0041  */
        /* JADX WARNING: Removed duplicated region for block: B:9:0x001f  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void startRegisteration() {
            /*
                r5 = this;
                monitor-enter(r5)
                int r2 = r5.mClientIf     // Catch:{ all -> 0x002c }
                r3 = -1
                if (r2 != r3) goto L_0x0008
                monitor-exit(r5)     // Catch:{ all -> 0x002c }
            L_0x0007:
                return
            L_0x0008:
                java.util.UUID r1 = java.util.UUID.randomUUID()     // Catch:{ InterruptedException -> 0x002f, RemoteException -> 0x004a }
                android.bluetooth.IBluetoothGatt r2 = r5.mBluetoothGatt     // Catch:{ InterruptedException -> 0x002f, RemoteException -> 0x004a }
                android.os.ParcelUuid r3 = new android.os.ParcelUuid     // Catch:{ InterruptedException -> 0x002f, RemoteException -> 0x004a }
                r3.<init>(r1)     // Catch:{ InterruptedException -> 0x002f, RemoteException -> 0x004a }
                r2.registerClient(r3, r5)     // Catch:{ InterruptedException -> 0x002f, RemoteException -> 0x004a }
                r2 = 2000(0x7d0, double:9.88E-321)
                r5.wait(r2)     // Catch:{ InterruptedException -> 0x002f, RemoteException -> 0x004a }
            L_0x001b:
                int r2 = r5.mClientIf
                if (r2 <= 0) goto L_0x0041
                android.bluetooth.le.BluetoothLeScanner r2 = android.bluetooth.le.BluetoothLeScanner.this
                java.util.Map r2 = android.bluetooth.le.BluetoothLeScanner.access$100(r2)
                android.bluetooth.le.ScanCallback r3 = r5.mScanCallback
                r2.put(r3, r5)
            L_0x002a:
                monitor-exit(r5)
                goto L_0x0007
            L_0x002c:
                r2 = move-exception
                monitor-exit(r5)
                throw r2
            L_0x002f:
                r2 = move-exception
                r0 = r2
            L_0x0031:
                java.lang.String r2 = "BluetoothLeScanner"
                java.lang.String r3 = "application registeration exception"
                android.util.Log.e(r2, r3, r0)
                android.bluetooth.le.BluetoothLeScanner r2 = android.bluetooth.le.BluetoothLeScanner.this
                android.bluetooth.le.ScanCallback r3 = r5.mScanCallback
                r4 = 3
                android.bluetooth.le.BluetoothLeScanner.access$000(r2, r3, r4)
                goto L_0x001b
            L_0x0041:
                android.bluetooth.le.BluetoothLeScanner r2 = android.bluetooth.le.BluetoothLeScanner.this
                android.bluetooth.le.ScanCallback r3 = r5.mScanCallback
                r4 = 2
                android.bluetooth.le.BluetoothLeScanner.access$000(r2, r3, r4)
                goto L_0x002a
            L_0x004a:
                r2 = move-exception
                r0 = r2
                goto L_0x0031
            */
            throw new UnsupportedOperationException("Method not decompiled: android.bluetooth.le.BluetoothLeScanner.BleScanCallbackWrapper.startRegisteration():void");
        }

        public void stopLeScan() {
            synchronized (this) {
                if (this.mClientIf <= 0) {
                    Log.e(BluetoothLeScanner.TAG, "Error state, mLeHandle: " + this.mClientIf);
                    return;
                }
                try {
                    this.mBluetoothGatt.stopScan(this.mClientIf, false);
                    this.mBluetoothGatt.unregisterClient(this.mClientIf);
                } catch (RemoteException e) {
                    Log.e(BluetoothLeScanner.TAG, "Failed to stop scan and unregister", e);
                }
                this.mClientIf = -1;
            }
        }

        /* access modifiers changed from: package-private */
        public void flushPendingBatchResults() {
            synchronized (this) {
                if (this.mClientIf <= 0) {
                    Log.e(BluetoothLeScanner.TAG, "Error state, mLeHandle: " + this.mClientIf);
                    return;
                }
                try {
                    this.mBluetoothGatt.flushPendingBatchResults(this.mClientIf, false);
                } catch (RemoteException e) {
                    Log.e(BluetoothLeScanner.TAG, "Failed to get pending scan results", e);
                }
            }
        }

        @Override // android.bluetooth.BluetoothGattCallbackWrapper, android.bluetooth.IBluetoothGattCallback
        public void onClientRegistered(int status, int clientIf) {
            Log.d(BluetoothLeScanner.TAG, "onClientRegistered() - status=" + status + " clientIf=" + clientIf);
            synchronized (this) {
                if (this.mClientIf == -1) {
                    Log.d(BluetoothLeScanner.TAG, "onClientRegistered LE scan canceled");
                }
                if (status == 0) {
                    this.mClientIf = clientIf;
                    try {
                        this.mBluetoothGatt.startScan(this.mClientIf, false, this.mSettings, this.mFilters, this.mResultStorages);
                    } catch (RemoteException e) {
                        Log.e(BluetoothLeScanner.TAG, "fail to start le scan: " + e);
                        this.mClientIf = -1;
                    }
                } else {
                    this.mClientIf = -1;
                }
                notifyAll();
            }
        }

        @Override // android.bluetooth.BluetoothGattCallbackWrapper, android.bluetooth.IBluetoothGattCallback
        public void onScanResult(final ScanResult scanResult) {
            Log.d(BluetoothLeScanner.TAG, "onScanResult() - " + scanResult.toString());
            synchronized (this) {
                if (this.mClientIf > 0) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        /* class android.bluetooth.le.BluetoothLeScanner.BleScanCallbackWrapper.AnonymousClass1 */

                        public void run() {
                            BleScanCallbackWrapper.this.mScanCallback.onScanResult(1, scanResult);
                        }
                    });
                }
            }
        }

        @Override // android.bluetooth.BluetoothGattCallbackWrapper, android.bluetooth.IBluetoothGattCallback
        public void onBatchScanResults(final List<ScanResult> results) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                /* class android.bluetooth.le.BluetoothLeScanner.BleScanCallbackWrapper.AnonymousClass2 */

                public void run() {
                    BleScanCallbackWrapper.this.mScanCallback.onBatchScanResults(results);
                }
            });
        }

        @Override // android.bluetooth.BluetoothGattCallbackWrapper, android.bluetooth.IBluetoothGattCallback
        public void onFoundOrLost(final boolean onFound, final ScanResult scanResult) {
            Log.d(BluetoothLeScanner.TAG, "onFoundOrLost() - onFound = " + onFound + " " + scanResult.toString());
            synchronized (this) {
                if (this.mClientIf > 0) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        /* class android.bluetooth.le.BluetoothLeScanner.BleScanCallbackWrapper.AnonymousClass3 */

                        public void run() {
                            if (onFound) {
                                BleScanCallbackWrapper.this.mScanCallback.onScanResult(2, scanResult);
                            } else {
                                BleScanCallbackWrapper.this.mScanCallback.onScanResult(4, scanResult);
                            }
                        }
                    });
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void postCallbackError(final ScanCallback callback, final int errorCode) {
        this.mHandler.post(new Runnable() {
            /* class android.bluetooth.le.BluetoothLeScanner.AnonymousClass1 */

            public void run() {
                callback.onScanFailed(errorCode);
            }
        });
    }

    private boolean isSettingsConfigAllowedForScan(ScanSettings settings) {
        if (this.mBluetoothAdapter.isOffloadedFilteringSupported()) {
            return true;
        }
        if (settings.getCallbackType() == 1 && settings.getReportDelayMillis() == 0) {
            return true;
        }
        return false;
    }
}

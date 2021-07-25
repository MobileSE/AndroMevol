package android.bluetooth;

import android.bluetooth.IBluetoothGattCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.ScanResult;
import android.os.ParcelUuid;
import android.os.RemoteException;
import java.util.List;

public class BluetoothGattCallbackWrapper extends IBluetoothGattCallback.Stub {
    @Override // android.bluetooth.IBluetoothGattCallback
    public void onClientRegistered(int status, int clientIf) throws RemoteException {
    }

    @Override // android.bluetooth.IBluetoothGattCallback
    public void onClientConnectionState(int status, int clientIf, boolean connected, String address) throws RemoteException {
    }

    @Override // android.bluetooth.IBluetoothGattCallback
    public void onScanResult(ScanResult scanResult) throws RemoteException {
    }

    @Override // android.bluetooth.IBluetoothGattCallback
    public void onBatchScanResults(List<ScanResult> list) throws RemoteException {
    }

    @Override // android.bluetooth.IBluetoothGattCallback
    public void onGetService(String address, int srvcType, int srvcInstId, ParcelUuid srvcUuid) throws RemoteException {
    }

    @Override // android.bluetooth.IBluetoothGattCallback
    public void onGetIncludedService(String address, int srvcType, int srvcInstId, ParcelUuid srvcUuid, int inclSrvcType, int inclSrvcInstId, ParcelUuid inclSrvcUuid) throws RemoteException {
    }

    @Override // android.bluetooth.IBluetoothGattCallback
    public void onGetCharacteristic(String address, int srvcType, int srvcInstId, ParcelUuid srvcUuid, int charInstId, ParcelUuid charUuid, int charProps) throws RemoteException {
    }

    @Override // android.bluetooth.IBluetoothGattCallback
    public void onGetDescriptor(String address, int srvcType, int srvcInstId, ParcelUuid srvcUuid, int charInstId, ParcelUuid charUuid, int descrInstId, ParcelUuid descrUuid) throws RemoteException {
    }

    @Override // android.bluetooth.IBluetoothGattCallback
    public void onSearchComplete(String address, int status) throws RemoteException {
    }

    @Override // android.bluetooth.IBluetoothGattCallback
    public void onCharacteristicRead(String address, int status, int srvcType, int srvcInstId, ParcelUuid srvcUuid, int charInstId, ParcelUuid charUuid, byte[] value) throws RemoteException {
    }

    @Override // android.bluetooth.IBluetoothGattCallback
    public void onCharacteristicWrite(String address, int status, int srvcType, int srvcInstId, ParcelUuid srvcUuid, int charInstId, ParcelUuid charUuid) throws RemoteException {
    }

    @Override // android.bluetooth.IBluetoothGattCallback
    public void onExecuteWrite(String address, int status) throws RemoteException {
    }

    @Override // android.bluetooth.IBluetoothGattCallback
    public void onDescriptorRead(String address, int status, int srvcType, int srvcInstId, ParcelUuid srvcUuid, int charInstId, ParcelUuid charUuid, int descrInstId, ParcelUuid descrUuid, byte[] value) throws RemoteException {
    }

    @Override // android.bluetooth.IBluetoothGattCallback
    public void onDescriptorWrite(String address, int status, int srvcType, int srvcInstId, ParcelUuid srvcUuid, int charInstId, ParcelUuid charUuid, int descrInstId, ParcelUuid descrUuid) throws RemoteException {
    }

    @Override // android.bluetooth.IBluetoothGattCallback
    public void onNotify(String address, int srvcType, int srvcInstId, ParcelUuid srvcUuid, int charInstId, ParcelUuid charUuid, byte[] value) throws RemoteException {
    }

    @Override // android.bluetooth.IBluetoothGattCallback
    public void onReadRemoteRssi(String address, int rssi, int status) throws RemoteException {
    }

    @Override // android.bluetooth.IBluetoothGattCallback
    public void onMultiAdvertiseCallback(int status, boolean isStart, AdvertiseSettings advertiseSettings) throws RemoteException {
    }

    @Override // android.bluetooth.IBluetoothGattCallback
    public void onConfigureMTU(String address, int mtu, int status) throws RemoteException {
    }

    @Override // android.bluetooth.IBluetoothGattCallback
    public void onFoundOrLost(boolean onFound, ScanResult scanResult) throws RemoteException {
    }
}

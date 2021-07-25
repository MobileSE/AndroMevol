package android.hardware.hdmi;

import android.hardware.hdmi.HdmiControlManager;
import android.hardware.hdmi.IHdmiVendorCommandListener;
import android.os.RemoteException;
import android.util.Log;

public abstract class HdmiClient {
    private static final String TAG = "HdmiClient";
    protected final IHdmiControlService mService;

    /* access modifiers changed from: protected */
    public abstract int getDeviceType();

    public HdmiClient(IHdmiControlService service) {
        this.mService = service;
    }

    public HdmiDeviceInfo getActiveSource() {
        try {
            return this.mService.getActiveSource();
        } catch (RemoteException e) {
            Log.e(TAG, "getActiveSource threw exception ", e);
            return null;
        }
    }

    public void sendKeyEvent(int keyCode, boolean isPressed) {
        try {
            this.mService.sendKeyEvent(getDeviceType(), keyCode, isPressed);
        } catch (RemoteException e) {
            Log.e(TAG, "sendKeyEvent threw exception ", e);
        }
    }

    public void sendVendorCommand(int targetAddress, byte[] params, boolean hasVendorId) {
        try {
            this.mService.sendVendorCommand(getDeviceType(), targetAddress, params, hasVendorId);
        } catch (RemoteException e) {
            Log.e(TAG, "failed to send vendor command: ", e);
        }
    }

    public void addVendorCommandListener(HdmiControlManager.VendorCommandListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener cannot be null");
        }
        try {
            this.mService.addVendorCommandListener(getListenerWrapper(listener), getDeviceType());
        } catch (RemoteException e) {
            Log.e(TAG, "failed to add vendor command listener: ", e);
        }
    }

    private static IHdmiVendorCommandListener getListenerWrapper(final HdmiControlManager.VendorCommandListener listener) {
        return new IHdmiVendorCommandListener.Stub() {
            /* class android.hardware.hdmi.HdmiClient.AnonymousClass1 */

            @Override // android.hardware.hdmi.IHdmiVendorCommandListener
            public void onReceived(int srcAddress, byte[] params, boolean hasVendorId) {
                listener.onReceived(srcAddress, params, hasVendorId);
            }
        };
    }
}

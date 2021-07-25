package android.hardware.hdmi;

import android.hardware.hdmi.IHdmiControlCallback;
import android.os.RemoteException;
import android.util.Log;

public final class HdmiPlaybackClient extends HdmiClient {
    private static final String TAG = "HdmiPlaybackClient";

    public interface DisplayStatusCallback {
        void onComplete(int i);
    }

    public interface OneTouchPlayCallback {
        void onComplete(int i);
    }

    HdmiPlaybackClient(IHdmiControlService service) {
        super(service);
    }

    public void oneTouchPlay(OneTouchPlayCallback callback) {
        try {
            this.mService.oneTouchPlay(getCallbackWrapper(callback));
        } catch (RemoteException e) {
            Log.e(TAG, "oneTouchPlay threw exception ", e);
        }
    }

    @Override // android.hardware.hdmi.HdmiClient
    public int getDeviceType() {
        return 4;
    }

    public void queryDisplayStatus(DisplayStatusCallback callback) {
        try {
            this.mService.queryDisplayStatus(getCallbackWrapper(callback));
        } catch (RemoteException e) {
            Log.e(TAG, "queryDisplayStatus threw exception ", e);
        }
    }

    private IHdmiControlCallback getCallbackWrapper(final OneTouchPlayCallback callback) {
        return new IHdmiControlCallback.Stub() {
            /* class android.hardware.hdmi.HdmiPlaybackClient.AnonymousClass1 */

            @Override // android.hardware.hdmi.IHdmiControlCallback
            public void onComplete(int result) {
                callback.onComplete(result);
            }
        };
    }

    private IHdmiControlCallback getCallbackWrapper(final DisplayStatusCallback callback) {
        return new IHdmiControlCallback.Stub() {
            /* class android.hardware.hdmi.HdmiPlaybackClient.AnonymousClass2 */

            @Override // android.hardware.hdmi.IHdmiControlCallback
            public void onComplete(int status) {
                callback.onComplete(status);
            }
        };
    }
}

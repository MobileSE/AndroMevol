package android.telecom;

import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.telecom.InCallService;
import android.view.Surface;
import com.android.internal.os.SomeArgs;
import com.android.internal.telecom.IVideoCallback;
import com.android.internal.telecom.IVideoProvider;

public class VideoCallImpl extends InCallService.VideoCall {
    private static final int MSG_CHANGE_CALL_DATA_USAGE = 5;
    private static final int MSG_CHANGE_CAMERA_CAPABILITIES = 6;
    private static final int MSG_CHANGE_PEER_DIMENSIONS = 4;
    private static final int MSG_HANDLE_CALL_SESSION_EVENT = 3;
    private static final int MSG_RECEIVE_SESSION_MODIFY_REQUEST = 1;
    private static final int MSG_RECEIVE_SESSION_MODIFY_RESPONSE = 2;
    private final VideoCallListenerBinder mBinder;
    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        /* class android.telecom.VideoCallImpl.AnonymousClass1 */

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            VideoCallImpl.this.mVideoProvider.asBinder().unlinkToDeath(this, 0);
        }
    };
    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        /* class android.telecom.VideoCallImpl.AnonymousClass2 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (VideoCallImpl.this.mVideoCallListener != null) {
                switch (msg.what) {
                    case 1:
                        VideoCallImpl.this.mVideoCallListener.onSessionModifyRequestReceived((VideoProfile) msg.obj);
                        return;
                    case 2:
                        SomeArgs args = (SomeArgs) msg.obj;
                        try {
                            VideoCallImpl.this.mVideoCallListener.onSessionModifyResponseReceived(((Integer) args.arg1).intValue(), (VideoProfile) args.arg2, (VideoProfile) args.arg3);
                            return;
                        } finally {
                            args.recycle();
                        }
                    case 3:
                        VideoCallImpl.this.mVideoCallListener.onCallSessionEvent(((Integer) msg.obj).intValue());
                        return;
                    case 4:
                        SomeArgs args2 = (SomeArgs) msg.obj;
                        try {
                            VideoCallImpl.this.mVideoCallListener.onPeerDimensionsChanged(((Integer) args2.arg1).intValue(), ((Integer) args2.arg2).intValue());
                            return;
                        } finally {
                            args2.recycle();
                        }
                    case 5:
                        VideoCallImpl.this.mVideoCallListener.onCallDataUsageChanged(msg.arg1);
                        return;
                    case 6:
                        VideoCallImpl.this.mVideoCallListener.onCameraCapabilitiesChanged((CameraCapabilities) msg.obj);
                        return;
                    default:
                        return;
                }
            }
        }
    };
    private InCallService.VideoCall.Listener mVideoCallListener;
    private final IVideoProvider mVideoProvider;

    private final class VideoCallListenerBinder extends IVideoCallback.Stub {
        private VideoCallListenerBinder() {
        }

        public void receiveSessionModifyRequest(VideoProfile videoProfile) {
            VideoCallImpl.this.mHandler.obtainMessage(1, videoProfile).sendToTarget();
        }

        public void receiveSessionModifyResponse(int status, VideoProfile requestProfile, VideoProfile responseProfile) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = Integer.valueOf(status);
            args.arg2 = requestProfile;
            args.arg3 = responseProfile;
            VideoCallImpl.this.mHandler.obtainMessage(2, args).sendToTarget();
        }

        public void handleCallSessionEvent(int event) {
            VideoCallImpl.this.mHandler.obtainMessage(3, Integer.valueOf(event)).sendToTarget();
        }

        public void changePeerDimensions(int width, int height) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = Integer.valueOf(width);
            args.arg2 = Integer.valueOf(height);
            VideoCallImpl.this.mHandler.obtainMessage(4, args).sendToTarget();
        }

        public void changeCallDataUsage(int dataUsage) {
            VideoCallImpl.this.mHandler.obtainMessage(5, Integer.valueOf(dataUsage)).sendToTarget();
        }

        public void changeCameraCapabilities(CameraCapabilities cameraCapabilities) {
            VideoCallImpl.this.mHandler.obtainMessage(6, cameraCapabilities).sendToTarget();
        }
    }

    /* JADX WARN: Type inference failed for: r1v3, types: [android.telecom.VideoCallImpl$VideoCallListenerBinder, android.os.IBinder] */
    /* JADX WARNING: Unknown variable types count: 1 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    VideoCallImpl(com.android.internal.telecom.IVideoProvider r4) throws android.os.RemoteException {
        /*
            r3 = this;
            r3.<init>()
            android.telecom.VideoCallImpl$1 r0 = new android.telecom.VideoCallImpl$1
            r0.<init>()
            r3.mDeathRecipient = r0
            android.telecom.VideoCallImpl$2 r0 = new android.telecom.VideoCallImpl$2
            android.os.Looper r1 = android.os.Looper.getMainLooper()
            r0.<init>(r1)
            r3.mHandler = r0
            r3.mVideoProvider = r4
            com.android.internal.telecom.IVideoProvider r0 = r3.mVideoProvider
            android.os.IBinder r0 = r0.asBinder()
            android.os.IBinder$DeathRecipient r1 = r3.mDeathRecipient
            r2 = 0
            r0.linkToDeath(r1, r2)
            android.telecom.VideoCallImpl$VideoCallListenerBinder r0 = new android.telecom.VideoCallImpl$VideoCallListenerBinder
            r1 = 0
            r0.<init>()
            r3.mBinder = r0
            com.android.internal.telecom.IVideoProvider r0 = r3.mVideoProvider
            android.telecom.VideoCallImpl$VideoCallListenerBinder r1 = r3.mBinder
            r0.setVideoCallback(r1)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: android.telecom.VideoCallImpl.<init>(com.android.internal.telecom.IVideoProvider):void");
    }

    @Override // android.telecom.InCallService.VideoCall
    public void setVideoCallListener(InCallService.VideoCall.Listener videoCallListener) {
        this.mVideoCallListener = videoCallListener;
    }

    @Override // android.telecom.InCallService.VideoCall
    public void setCamera(String cameraId) {
        try {
            this.mVideoProvider.setCamera(cameraId);
        } catch (RemoteException e) {
        }
    }

    @Override // android.telecom.InCallService.VideoCall
    public void setPreviewSurface(Surface surface) {
        try {
            this.mVideoProvider.setPreviewSurface(surface);
        } catch (RemoteException e) {
        }
    }

    @Override // android.telecom.InCallService.VideoCall
    public void setDisplaySurface(Surface surface) {
        try {
            this.mVideoProvider.setDisplaySurface(surface);
        } catch (RemoteException e) {
        }
    }

    @Override // android.telecom.InCallService.VideoCall
    public void setDeviceOrientation(int rotation) {
        try {
            this.mVideoProvider.setDeviceOrientation(rotation);
        } catch (RemoteException e) {
        }
    }

    @Override // android.telecom.InCallService.VideoCall
    public void setZoom(float value) {
        try {
            this.mVideoProvider.setZoom(value);
        } catch (RemoteException e) {
        }
    }

    @Override // android.telecom.InCallService.VideoCall
    public void sendSessionModifyRequest(VideoProfile requestProfile) {
        try {
            this.mVideoProvider.sendSessionModifyRequest(requestProfile);
        } catch (RemoteException e) {
        }
    }

    @Override // android.telecom.InCallService.VideoCall
    public void sendSessionModifyResponse(VideoProfile responseProfile) {
        try {
            this.mVideoProvider.sendSessionModifyResponse(responseProfile);
        } catch (RemoteException e) {
        }
    }

    @Override // android.telecom.InCallService.VideoCall
    public void requestCameraCapabilities() {
        try {
            this.mVideoProvider.requestCameraCapabilities();
        } catch (RemoteException e) {
        }
    }

    @Override // android.telecom.InCallService.VideoCall
    public void requestCallDataUsage() {
        try {
            this.mVideoProvider.requestCallDataUsage();
        } catch (RemoteException e) {
        }
    }

    @Override // android.telecom.InCallService.VideoCall
    public void setPauseImage(String uri) {
        try {
            this.mVideoProvider.setPauseImage(uri);
        } catch (RemoteException e) {
        }
    }
}

package android.telecom;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Surface;
import com.android.internal.os.SomeArgs;
import com.android.internal.telecom.IInCallAdapter;
import com.android.internal.telecom.IInCallService;

public abstract class InCallService extends Service {
    private static final int MSG_ADD_CALL = 2;
    private static final int MSG_BRING_TO_FOREGROUND = 6;
    private static final int MSG_ON_AUDIO_STATE_CHANGED = 5;
    private static final int MSG_SET_IN_CALL_ADAPTER = 1;
    private static final int MSG_SET_POST_DIAL_WAIT = 4;
    private static final int MSG_UPDATE_CALL = 3;
    public static final String SERVICE_INTERFACE = "android.telecom.InCallService";
    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        /* class android.telecom.InCallService.AnonymousClass1 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            boolean z = true;
            if (InCallService.this.mPhone != null || msg.what == 1) {
                switch (msg.what) {
                    case 1:
                        InCallService.this.mPhone = new Phone(new InCallAdapter((IInCallAdapter) msg.obj));
                        InCallService.this.onPhoneCreated(InCallService.this.mPhone);
                        return;
                    case 2:
                        InCallService.this.mPhone.internalAddCall((ParcelableCall) msg.obj);
                        return;
                    case 3:
                        InCallService.this.mPhone.internalUpdateCall((ParcelableCall) msg.obj);
                        return;
                    case 4:
                        SomeArgs args = (SomeArgs) msg.obj;
                        try {
                            InCallService.this.mPhone.internalSetPostDialWait((String) args.arg1, (String) args.arg2);
                            return;
                        } finally {
                            args.recycle();
                        }
                    case 5:
                        InCallService.this.mPhone.internalAudioStateChanged((AudioState) msg.obj);
                        return;
                    case 6:
                        Phone phone = InCallService.this.mPhone;
                        if (msg.arg1 != 1) {
                            z = false;
                        }
                        phone.internalBringToForeground(z);
                        return;
                    default:
                        return;
                }
            }
        }
    };
    private Phone mPhone;

    public static abstract class VideoCall {

        public static abstract class Listener {
            public abstract void onCallDataUsageChanged(int i);

            public abstract void onCallSessionEvent(int i);

            public abstract void onCameraCapabilitiesChanged(CameraCapabilities cameraCapabilities);

            public abstract void onPeerDimensionsChanged(int i, int i2);

            public abstract void onSessionModifyRequestReceived(VideoProfile videoProfile);

            public abstract void onSessionModifyResponseReceived(int i, VideoProfile videoProfile, VideoProfile videoProfile2);
        }

        public abstract void requestCallDataUsage();

        public abstract void requestCameraCapabilities();

        public abstract void sendSessionModifyRequest(VideoProfile videoProfile);

        public abstract void sendSessionModifyResponse(VideoProfile videoProfile);

        public abstract void setCamera(String str);

        public abstract void setDeviceOrientation(int i);

        public abstract void setDisplaySurface(Surface surface);

        public abstract void setPauseImage(String str);

        public abstract void setPreviewSurface(Surface surface);

        public abstract void setVideoCallListener(Listener listener);

        public abstract void setZoom(float f);
    }

    private final class InCallServiceBinder extends IInCallService.Stub {
        private InCallServiceBinder() {
        }

        public void setInCallAdapter(IInCallAdapter inCallAdapter) {
            InCallService.this.mHandler.obtainMessage(1, inCallAdapter).sendToTarget();
        }

        public void addCall(ParcelableCall call) {
            InCallService.this.mHandler.obtainMessage(2, call).sendToTarget();
        }

        public void updateCall(ParcelableCall call) {
            InCallService.this.mHandler.obtainMessage(3, call).sendToTarget();
        }

        public void setPostDial(String callId, String remaining) {
        }

        public void setPostDialWait(String callId, String remaining) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = callId;
            args.arg2 = remaining;
            InCallService.this.mHandler.obtainMessage(4, args).sendToTarget();
        }

        public void onAudioStateChanged(AudioState audioState) {
            InCallService.this.mHandler.obtainMessage(5, audioState).sendToTarget();
        }

        public void bringToForeground(boolean showDialpad) {
            int i;
            Handler handler = InCallService.this.mHandler;
            if (showDialpad) {
                i = 1;
            } else {
                i = 0;
            }
            handler.obtainMessage(6, i, 0).sendToTarget();
        }
    }

    /* JADX WARN: Type inference failed for: r0v0, types: [android.telecom.InCallService$InCallServiceBinder, android.os.IBinder] */
    /* JADX WARNING: Unknown variable types count: 1 */
    @Override // android.app.Service
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public android.os.IBinder onBind(android.content.Intent r3) {
        /*
            r2 = this;
            android.telecom.InCallService$InCallServiceBinder r0 = new android.telecom.InCallService$InCallServiceBinder
            r1 = 0
            r0.<init>()
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: android.telecom.InCallService.onBind(android.content.Intent):android.os.IBinder");
    }

    @Override // android.app.Service
    public boolean onUnbind(Intent intent) {
        if (this.mPhone == null) {
            return false;
        }
        Phone oldPhone = this.mPhone;
        this.mPhone = null;
        oldPhone.destroy();
        onPhoneDestroyed(oldPhone);
        return false;
    }

    public Phone getPhone() {
        return this.mPhone;
    }

    public void onPhoneCreated(Phone phone) {
    }

    public void onPhoneDestroyed(Phone phone) {
    }
}

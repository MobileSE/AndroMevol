package android.service.voice;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.service.voice.IVoiceInteractionSessionService;
import com.android.internal.app.IVoiceInteractionManagerService;
import com.android.internal.os.HandlerCaller;
import com.android.internal.os.SomeArgs;

public abstract class VoiceInteractionSessionService extends Service {
    static final int MSG_NEW_SESSION = 1;
    HandlerCaller mHandlerCaller;
    final HandlerCaller.Callback mHandlerCallerCallback = new HandlerCaller.Callback() {
        /* class android.service.voice.VoiceInteractionSessionService.AnonymousClass2 */

        public void executeMessage(Message msg) {
            SomeArgs args = (SomeArgs) msg.obj;
            switch (msg.what) {
                case 1:
                    VoiceInteractionSessionService.this.doNewSession((IBinder) args.arg1, (Bundle) args.arg2);
                    return;
                default:
                    return;
            }
        }
    };
    IVoiceInteractionSessionService mInterface = new IVoiceInteractionSessionService.Stub() {
        /* class android.service.voice.VoiceInteractionSessionService.AnonymousClass1 */

        @Override // android.service.voice.IVoiceInteractionSessionService
        public void newSession(IBinder token, Bundle args) {
            VoiceInteractionSessionService.this.mHandlerCaller.sendMessage(VoiceInteractionSessionService.this.mHandlerCaller.obtainMessageOO(1, token, args));
        }
    };
    VoiceInteractionSession mSession;
    IVoiceInteractionManagerService mSystemService;

    public abstract VoiceInteractionSession onNewSession(Bundle bundle);

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        this.mSystemService = IVoiceInteractionManagerService.Stub.asInterface(ServiceManager.getService(Context.VOICE_INTERACTION_MANAGER_SERVICE));
        this.mHandlerCaller = new HandlerCaller(this, Looper.myLooper(), this.mHandlerCallerCallback, true);
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return this.mInterface.asBinder();
    }

    /* access modifiers changed from: package-private */
    public void doNewSession(IBinder token, Bundle args) {
        if (this.mSession != null) {
            this.mSession.doDestroy();
            this.mSession = null;
        }
        this.mSession = onNewSession(args);
        try {
            this.mSystemService.deliverNewSession(token, this.mSession.mSession, this.mSession.mInteractor);
            this.mSession.doCreate(this.mSystemService, token, args);
        } catch (RemoteException e) {
        }
    }
}

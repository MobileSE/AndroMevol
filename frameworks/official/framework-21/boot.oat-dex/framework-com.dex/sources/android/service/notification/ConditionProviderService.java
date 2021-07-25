package android.service.notification;

import android.app.INotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.service.notification.IConditionProvider;
import android.util.Log;

public abstract class ConditionProviderService extends Service {
    public static final String SERVICE_INTERFACE = "android.service.notification.ConditionProviderService";
    private final String TAG = (ConditionProviderService.class.getSimpleName() + "[" + getClass().getSimpleName() + "]");
    private final H mHandler = new H();
    private INotificationManager mNoMan;
    private Provider mProvider;

    public abstract void onConnected();

    public abstract void onRequestConditions(int i);

    public abstract void onSubscribe(Uri uri);

    public abstract void onUnsubscribe(Uri uri);

    private final INotificationManager getNotificationInterface() {
        if (this.mNoMan == null) {
            this.mNoMan = INotificationManager.Stub.asInterface(ServiceManager.getService(Context.NOTIFICATION_SERVICE));
        }
        return this.mNoMan;
    }

    public final void notifyCondition(Condition condition) {
        if (condition != null) {
            notifyConditions(condition);
        }
    }

    public final void notifyConditions(Condition... conditions) {
        if (isBound() && conditions != null) {
            try {
                getNotificationInterface().notifyConditions(getPackageName(), this.mProvider, conditions);
            } catch (RemoteException ex) {
                Log.v(this.TAG, "Unable to contact notification manager", ex);
            }
        }
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        if (this.mProvider == null) {
            this.mProvider = new Provider();
        }
        return this.mProvider;
    }

    private boolean isBound() {
        if (this.mProvider != null) {
            return true;
        }
        Log.w(this.TAG, "Condition provider service not yet bound.");
        return false;
    }

    /* access modifiers changed from: private */
    public final class Provider extends IConditionProvider.Stub {
        private Provider() {
        }

        @Override // android.service.notification.IConditionProvider
        public void onConnected() {
            ConditionProviderService.this.mHandler.obtainMessage(1).sendToTarget();
        }

        @Override // android.service.notification.IConditionProvider
        public void onRequestConditions(int relevance) {
            ConditionProviderService.this.mHandler.obtainMessage(2, relevance, 0).sendToTarget();
        }

        @Override // android.service.notification.IConditionProvider
        public void onSubscribe(Uri conditionId) {
            ConditionProviderService.this.mHandler.obtainMessage(3, conditionId).sendToTarget();
        }

        @Override // android.service.notification.IConditionProvider
        public void onUnsubscribe(Uri conditionId) {
            ConditionProviderService.this.mHandler.obtainMessage(4, conditionId).sendToTarget();
        }
    }

    /* access modifiers changed from: private */
    public final class H extends Handler {
        private static final int ON_CONNECTED = 1;
        private static final int ON_REQUEST_CONDITIONS = 2;
        private static final int ON_SUBSCRIBE = 3;
        private static final int ON_UNSUBSCRIBE = 4;

        private H() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            try {
                switch (msg.what) {
                    case 1:
                        ConditionProviderService.this.onConnected();
                        return;
                    case 2:
                        ConditionProviderService.this.onRequestConditions(msg.arg1);
                        return;
                    case 3:
                        ConditionProviderService.this.onSubscribe((Uri) msg.obj);
                        return;
                    case 4:
                        ConditionProviderService.this.onUnsubscribe((Uri) msg.obj);
                        return;
                    default:
                        return;
                }
            } catch (Throwable t) {
                Log.w(ConditionProviderService.this.TAG, "Error running " + ((String) null), t);
            }
        }
    }
}

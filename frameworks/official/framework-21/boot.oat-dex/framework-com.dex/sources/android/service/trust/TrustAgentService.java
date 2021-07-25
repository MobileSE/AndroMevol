package android.service.trust;

import android.Manifest;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.service.trust.ITrustAgentService;
import android.util.Log;
import android.util.Slog;

public class TrustAgentService extends Service {
    private static final boolean DEBUG = false;
    public static final String KEY_FEATURES = "trust_agent_features";
    private static final int MSG_SET_TRUST_AGENT_FEATURES_ENABLED = 2;
    private static final int MSG_TRUST_TIMEOUT = 3;
    private static final int MSG_UNLOCK_ATTEMPT = 1;
    public static final String SERVICE_INTERFACE = "android.service.trust.TrustAgentService";
    public static final String TRUST_AGENT_META_DATA = "android.service.trust.trustagent";
    private final String TAG = (TrustAgentService.class.getSimpleName() + "[" + getClass().getSimpleName() + "]");
    private ITrustAgentServiceCallback mCallback;
    private Handler mHandler = new Handler() {
        /* class android.service.trust.TrustAgentService.AnonymousClass1 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    TrustAgentService.this.onUnlockAttempt(msg.arg1 != 0);
                    return;
                case 2:
                    Bundle features = msg.peekData();
                    IBinder token = (IBinder) msg.obj;
                    boolean result = TrustAgentService.this.onSetTrustAgentFeaturesEnabled(features);
                    try {
                        synchronized (TrustAgentService.this.mLock) {
                            TrustAgentService.this.mCallback.onSetTrustAgentFeaturesEnabledCompleted(result, token);
                        }
                        return;
                    } catch (RemoteException e) {
                        TrustAgentService.this.onError("calling onSetTrustAgentFeaturesEnabledCompleted()");
                        return;
                    }
                case 3:
                    TrustAgentService.this.onTrustTimeout();
                    return;
                default:
                    return;
            }
        }
    };
    private final Object mLock = new Object();
    private boolean mManagingTrust;
    private Runnable mPendingGrantTrustTask;

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        ComponentName component = new ComponentName(this, getClass());
        try {
            if (!Manifest.permission.BIND_TRUST_AGENT.equals(getPackageManager().getServiceInfo(component, 0).permission)) {
                throw new IllegalStateException(component.flattenToShortString() + " is not declared with the permission " + "\"" + Manifest.permission.BIND_TRUST_AGENT + "\"");
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(this.TAG, "Can't get ServiceInfo for " + component.toShortString());
        }
    }

    public void onUnlockAttempt(boolean successful) {
    }

    public void onTrustTimeout() {
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onError(String msg) {
        Slog.v(this.TAG, "Remote exception while " + msg);
    }

    public boolean onSetTrustAgentFeaturesEnabled(Bundle options) {
        return false;
    }

    public final void grantTrust(final CharSequence message, final long durationMs, final boolean initiatedByUser) {
        synchronized (this.mLock) {
            if (!this.mManagingTrust) {
                throw new IllegalStateException("Cannot grant trust if agent is not managing trust. Call setManagingTrust(true) first.");
            } else if (this.mCallback != null) {
                try {
                    this.mCallback.grantTrust(message.toString(), durationMs, initiatedByUser);
                } catch (RemoteException e) {
                    onError("calling enableTrust()");
                }
            } else {
                this.mPendingGrantTrustTask = new Runnable() {
                    /* class android.service.trust.TrustAgentService.AnonymousClass2 */

                    public void run() {
                        TrustAgentService.this.grantTrust(message, durationMs, initiatedByUser);
                    }
                };
            }
        }
    }

    public final void revokeTrust() {
        synchronized (this.mLock) {
            if (this.mPendingGrantTrustTask != null) {
                this.mPendingGrantTrustTask = null;
            }
            if (this.mCallback != null) {
                try {
                    this.mCallback.revokeTrust();
                } catch (RemoteException e) {
                    onError("calling revokeTrust()");
                }
            }
        }
    }

    public final void setManagingTrust(boolean managingTrust) {
        synchronized (this.mLock) {
            if (this.mManagingTrust != managingTrust) {
                this.mManagingTrust = managingTrust;
                if (this.mCallback != null) {
                    try {
                        this.mCallback.setManagingTrust(managingTrust);
                    } catch (RemoteException e) {
                        onError("calling setManagingTrust()");
                    }
                }
            }
        }
    }

    @Override // android.app.Service
    public final IBinder onBind(Intent intent) {
        return new TrustAgentServiceWrapper();
    }

    private final class TrustAgentServiceWrapper extends ITrustAgentService.Stub {
        private TrustAgentServiceWrapper() {
        }

        @Override // android.service.trust.ITrustAgentService
        public void onUnlockAttempt(boolean successful) {
            int i;
            Handler handler = TrustAgentService.this.mHandler;
            if (successful) {
                i = 1;
            } else {
                i = 0;
            }
            handler.obtainMessage(1, i, 0).sendToTarget();
        }

        @Override // android.service.trust.ITrustAgentService
        public void onTrustTimeout() {
            TrustAgentService.this.mHandler.sendEmptyMessage(3);
        }

        @Override // android.service.trust.ITrustAgentService
        public void setCallback(ITrustAgentServiceCallback callback) {
            synchronized (TrustAgentService.this.mLock) {
                TrustAgentService.this.mCallback = callback;
                if (TrustAgentService.this.mManagingTrust) {
                    try {
                        TrustAgentService.this.mCallback.setManagingTrust(TrustAgentService.this.mManagingTrust);
                    } catch (RemoteException e) {
                        TrustAgentService.this.onError("calling setManagingTrust()");
                    }
                }
                if (TrustAgentService.this.mPendingGrantTrustTask != null) {
                    TrustAgentService.this.mPendingGrantTrustTask.run();
                    TrustAgentService.this.mPendingGrantTrustTask = null;
                }
            }
        }

        @Override // android.service.trust.ITrustAgentService
        public void setTrustAgentFeaturesEnabled(Bundle features, IBinder token) {
            Message msg = TrustAgentService.this.mHandler.obtainMessage(2, token);
            msg.setData(features);
            msg.sendToTarget();
        }
    }
}

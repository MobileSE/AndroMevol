package android.os;

import android.os.IRemoteCallback;
import android.os.Parcelable;

public abstract class RemoteCallback implements Parcelable {
    public static final Parcelable.Creator<RemoteCallback> CREATOR = new Parcelable.Creator<RemoteCallback>() {
        /* class android.os.RemoteCallback.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RemoteCallback createFromParcel(Parcel in) {
            IBinder target = in.readStrongBinder();
            if (target != null) {
                return new RemoteCallbackProxy(IRemoteCallback.Stub.asInterface(target));
            }
            return null;
        }

        @Override // android.os.Parcelable.Creator
        public RemoteCallback[] newArray(int size) {
            return new RemoteCallback[size];
        }
    };
    final Handler mHandler;
    final IRemoteCallback mTarget;

    /* access modifiers changed from: protected */
    public abstract void onResult(Bundle bundle);

    class DeliverResult implements Runnable {
        final Bundle mResult;

        DeliverResult(Bundle result) {
            this.mResult = result;
        }

        public void run() {
            RemoteCallback.this.onResult(this.mResult);
        }
    }

    class LocalCallback extends IRemoteCallback.Stub {
        LocalCallback() {
        }

        @Override // android.os.IRemoteCallback
        public void sendResult(Bundle bundle) {
            RemoteCallback.this.mHandler.post(new DeliverResult(bundle));
        }
    }

    /* access modifiers changed from: package-private */
    public static class RemoteCallbackProxy extends RemoteCallback {
        RemoteCallbackProxy(IRemoteCallback target) {
            super(target);
        }

        /* access modifiers changed from: protected */
        @Override // android.os.RemoteCallback
        public void onResult(Bundle bundle) {
        }
    }

    public RemoteCallback(Handler handler) {
        this.mHandler = handler;
        this.mTarget = new LocalCallback();
    }

    RemoteCallback(IRemoteCallback target) {
        this.mHandler = null;
        this.mTarget = target;
    }

    public void sendResult(Bundle bundle) throws RemoteException {
        this.mTarget.sendResult(bundle);
    }

    public boolean equals(Object otherObj) {
        if (otherObj == null) {
            return false;
        }
        try {
            return this.mTarget.asBinder().equals(((RemoteCallback) otherObj).mTarget.asBinder());
        } catch (ClassCastException e) {
            return false;
        }
    }

    public int hashCode() {
        return this.mTarget.asBinder().hashCode();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeStrongBinder(this.mTarget.asBinder());
    }
}

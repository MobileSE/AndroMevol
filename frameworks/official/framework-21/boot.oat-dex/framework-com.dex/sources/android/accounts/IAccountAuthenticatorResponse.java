package android.accounts;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IAccountAuthenticatorResponse extends IInterface {
    void onError(int i, String str) throws RemoteException;

    void onRequestContinued() throws RemoteException;

    void onResult(Bundle bundle) throws RemoteException;

    public static abstract class Stub extends Binder implements IAccountAuthenticatorResponse {
        private static final String DESCRIPTOR = "android.accounts.IAccountAuthenticatorResponse";
        static final int TRANSACTION_onError = 3;
        static final int TRANSACTION_onRequestContinued = 2;
        static final int TRANSACTION_onResult = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IAccountAuthenticatorResponse asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IAccountAuthenticatorResponse)) {
                return new Proxy(obj);
            }
            return (IAccountAuthenticatorResponse) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle _arg0;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = Bundle.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    onResult(_arg0);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    onRequestContinued();
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    onError(data.readInt(), data.readString());
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*{ENCODED_INT: 1598968902}*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IAccountAuthenticatorResponse {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // android.accounts.IAccountAuthenticatorResponse
            public void onResult(Bundle value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (value != null) {
                        _data.writeInt(1);
                        value.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.accounts.IAccountAuthenticatorResponse
            public void onRequestContinued() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.accounts.IAccountAuthenticatorResponse
            public void onError(int errorCode, String errorMessage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(errorCode);
                    _data.writeString(errorMessage);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }
    }
}

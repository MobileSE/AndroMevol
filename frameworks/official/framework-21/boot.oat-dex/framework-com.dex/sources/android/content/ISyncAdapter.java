package android.content;

import android.accounts.Account;
import android.content.ISyncContext;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ISyncAdapter extends IInterface {
    void cancelSync(ISyncContext iSyncContext) throws RemoteException;

    void initialize(Account account, String str) throws RemoteException;

    void startSync(ISyncContext iSyncContext, String str, Account account, Bundle bundle) throws RemoteException;

    public static abstract class Stub extends Binder implements ISyncAdapter {
        private static final String DESCRIPTOR = "android.content.ISyncAdapter";
        static final int TRANSACTION_cancelSync = 2;
        static final int TRANSACTION_initialize = 3;
        static final int TRANSACTION_startSync = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ISyncAdapter asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ISyncAdapter)) {
                return new Proxy(obj);
            }
            return (ISyncAdapter) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Account _arg0;
            Account _arg2;
            Bundle _arg3;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    ISyncContext _arg02 = ISyncContext.Stub.asInterface(data.readStrongBinder());
                    String _arg1 = data.readString();
                    if (data.readInt() != 0) {
                        _arg2 = Account.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg3 = Bundle.CREATOR.createFromParcel(data);
                    } else {
                        _arg3 = null;
                    }
                    startSync(_arg02, _arg1, _arg2, _arg3);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    cancelSync(ISyncContext.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = Account.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    initialize(_arg0, data.readString());
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*{ENCODED_INT: 1598968902}*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }

        private static class Proxy implements ISyncAdapter {
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

            @Override // android.content.ISyncAdapter
            public void startSync(ISyncContext syncContext, String authority, Account account, Bundle extras) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (syncContext != null) {
                        iBinder = syncContext.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeString(authority);
                    if (account != null) {
                        _data.writeInt(1);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.content.ISyncAdapter
            public void cancelSync(ISyncContext syncContext) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (syncContext != null) {
                        iBinder = syncContext.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.content.ISyncAdapter
            public void initialize(Account account, String authority) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (account != null) {
                        _data.writeInt(1);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(authority);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }
    }
}

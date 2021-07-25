package android.service.fingerprint;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IFingerprintServiceReceiver extends IInterface {
    void onAcquired(int i) throws RemoteException;

    void onEnrollResult(int i, int i2) throws RemoteException;

    void onError(int i) throws RemoteException;

    void onProcessed(int i) throws RemoteException;

    void onRemoved(int i) throws RemoteException;

    public static abstract class Stub extends Binder implements IFingerprintServiceReceiver {
        private static final String DESCRIPTOR = "android.service.fingerprint.IFingerprintServiceReceiver";
        static final int TRANSACTION_onAcquired = 2;
        static final int TRANSACTION_onEnrollResult = 1;
        static final int TRANSACTION_onError = 4;
        static final int TRANSACTION_onProcessed = 3;
        static final int TRANSACTION_onRemoved = 5;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IFingerprintServiceReceiver asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IFingerprintServiceReceiver)) {
                return new Proxy(obj);
            }
            return (IFingerprintServiceReceiver) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    onEnrollResult(data.readInt(), data.readInt());
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    onAcquired(data.readInt());
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    onProcessed(data.readInt());
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    onError(data.readInt());
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    onRemoved(data.readInt());
                    return true;
                case IBinder.INTERFACE_TRANSACTION:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IFingerprintServiceReceiver {
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

            @Override // android.service.fingerprint.IFingerprintServiceReceiver
            public void onEnrollResult(int fingerprintId, int remaining) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(fingerprintId);
                    _data.writeInt(remaining);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.fingerprint.IFingerprintServiceReceiver
            public void onAcquired(int acquiredInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(acquiredInfo);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.fingerprint.IFingerprintServiceReceiver
            public void onProcessed(int fingerprintId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(fingerprintId);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.fingerprint.IFingerprintServiceReceiver
            public void onError(int error) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(error);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.fingerprint.IFingerprintServiceReceiver
            public void onRemoved(int fingerprintId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(fingerprintId);
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }
    }
}

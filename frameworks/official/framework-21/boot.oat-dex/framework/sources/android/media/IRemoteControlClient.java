package android.media;

import android.media.IRemoteControlDisplay;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IRemoteControlClient extends IInterface {
    void enableRemoteControlDisplay(IRemoteControlDisplay iRemoteControlDisplay, boolean z) throws RemoteException;

    void informationRequestForDisplay(IRemoteControlDisplay iRemoteControlDisplay, int i, int i2) throws RemoteException;

    void onInformationRequested(int i, int i2) throws RemoteException;

    void plugRemoteControlDisplay(IRemoteControlDisplay iRemoteControlDisplay, int i, int i2) throws RemoteException;

    void seekTo(int i, long j) throws RemoteException;

    void setBitmapSizeForDisplay(IRemoteControlDisplay iRemoteControlDisplay, int i, int i2) throws RemoteException;

    void setCurrentClientGenerationId(int i) throws RemoteException;

    void setWantsSyncForDisplay(IRemoteControlDisplay iRemoteControlDisplay, boolean z) throws RemoteException;

    void unplugRemoteControlDisplay(IRemoteControlDisplay iRemoteControlDisplay) throws RemoteException;

    void updateMetadata(int i, int i2, Rating rating) throws RemoteException;

    public static abstract class Stub extends Binder implements IRemoteControlClient {
        private static final String DESCRIPTOR = "android.media.IRemoteControlClient";
        static final int TRANSACTION_enableRemoteControlDisplay = 8;
        static final int TRANSACTION_informationRequestForDisplay = 2;
        static final int TRANSACTION_onInformationRequested = 1;
        static final int TRANSACTION_plugRemoteControlDisplay = 4;
        static final int TRANSACTION_seekTo = 9;
        static final int TRANSACTION_setBitmapSizeForDisplay = 6;
        static final int TRANSACTION_setCurrentClientGenerationId = 3;
        static final int TRANSACTION_setWantsSyncForDisplay = 7;
        static final int TRANSACTION_unplugRemoteControlDisplay = 5;
        static final int TRANSACTION_updateMetadata = 10;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IRemoteControlClient asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IRemoteControlClient)) {
                return new Proxy(obj);
            }
            return (IRemoteControlClient) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Rating _arg2;
            boolean _arg1 = false;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    onInformationRequested(data.readInt(), data.readInt());
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    informationRequestForDisplay(IRemoteControlDisplay.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readInt());
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    setCurrentClientGenerationId(data.readInt());
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    plugRemoteControlDisplay(IRemoteControlDisplay.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readInt());
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    unplugRemoteControlDisplay(IRemoteControlDisplay.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    setBitmapSizeForDisplay(IRemoteControlDisplay.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readInt());
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    IRemoteControlDisplay _arg0 = IRemoteControlDisplay.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        _arg1 = true;
                    }
                    setWantsSyncForDisplay(_arg0, _arg1);
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    IRemoteControlDisplay _arg02 = IRemoteControlDisplay.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        _arg1 = true;
                    }
                    enableRemoteControlDisplay(_arg02, _arg1);
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    seekTo(data.readInt(), data.readLong());
                    return true;
                case 10:
                    data.enforceInterface(DESCRIPTOR);
                    int _arg03 = data.readInt();
                    int _arg12 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg2 = Rating.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    updateMetadata(_arg03, _arg12, _arg2);
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*{ENCODED_INT: 1598968902}*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }

        private static class Proxy implements IRemoteControlClient {
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

            @Override // android.media.IRemoteControlClient
            public void onInformationRequested(int generationId, int infoFlags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(generationId);
                    _data.writeInt(infoFlags);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.IRemoteControlClient
            public void informationRequestForDisplay(IRemoteControlDisplay rcd, int w, int h) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (rcd != null) {
                        iBinder = rcd.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(w);
                    _data.writeInt(h);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.IRemoteControlClient
            public void setCurrentClientGenerationId(int clientGeneration) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(clientGeneration);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.IRemoteControlClient
            public void plugRemoteControlDisplay(IRemoteControlDisplay rcd, int w, int h) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (rcd != null) {
                        iBinder = rcd.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(w);
                    _data.writeInt(h);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.IRemoteControlClient
            public void unplugRemoteControlDisplay(IRemoteControlDisplay rcd) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (rcd != null) {
                        iBinder = rcd.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.IRemoteControlClient
            public void setBitmapSizeForDisplay(IRemoteControlDisplay rcd, int w, int h) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (rcd != null) {
                        iBinder = rcd.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(w);
                    _data.writeInt(h);
                    this.mRemote.transact(6, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.IRemoteControlClient
            public void setWantsSyncForDisplay(IRemoteControlDisplay rcd, boolean wantsSync) throws RemoteException {
                IBinder iBinder = null;
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (rcd != null) {
                        iBinder = rcd.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (!wantsSync) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(7, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.IRemoteControlClient
            public void enableRemoteControlDisplay(IRemoteControlDisplay rcd, boolean enabled) throws RemoteException {
                IBinder iBinder = null;
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (rcd != null) {
                        iBinder = rcd.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (!enabled) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(8, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.IRemoteControlClient
            public void seekTo(int clientGeneration, long timeMs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(clientGeneration);
                    _data.writeLong(timeMs);
                    this.mRemote.transact(9, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.IRemoteControlClient
            public void updateMetadata(int clientGeneration, int key, Rating value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(clientGeneration);
                    _data.writeInt(key);
                    if (value != null) {
                        _data.writeInt(1);
                        value.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(10, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }
    }
}

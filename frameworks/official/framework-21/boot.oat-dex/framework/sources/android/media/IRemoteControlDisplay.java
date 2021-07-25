package android.media;

import android.app.PendingIntent;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IRemoteControlDisplay extends IInterface {
    void setAllMetadata(int i, Bundle bundle, Bitmap bitmap) throws RemoteException;

    void setArtwork(int i, Bitmap bitmap) throws RemoteException;

    void setCurrentClientId(int i, PendingIntent pendingIntent, boolean z) throws RemoteException;

    void setEnabled(boolean z) throws RemoteException;

    void setMetadata(int i, Bundle bundle) throws RemoteException;

    void setPlaybackState(int i, int i2, long j, long j2, float f) throws RemoteException;

    void setTransportControlInfo(int i, int i2, int i3) throws RemoteException;

    public static abstract class Stub extends Binder implements IRemoteControlDisplay {
        private static final String DESCRIPTOR = "android.media.IRemoteControlDisplay";
        static final int TRANSACTION_setAllMetadata = 7;
        static final int TRANSACTION_setArtwork = 6;
        static final int TRANSACTION_setCurrentClientId = 1;
        static final int TRANSACTION_setEnabled = 2;
        static final int TRANSACTION_setMetadata = 5;
        static final int TRANSACTION_setPlaybackState = 3;
        static final int TRANSACTION_setTransportControlInfo = 4;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IRemoteControlDisplay asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IRemoteControlDisplay)) {
                return new Proxy(obj);
            }
            return (IRemoteControlDisplay) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle _arg1;
            Bitmap _arg2;
            Bitmap _arg12;
            Bundle _arg13;
            boolean _arg0;
            PendingIntent _arg14;
            boolean _arg22 = false;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    int _arg02 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg14 = PendingIntent.CREATOR.createFromParcel(data);
                    } else {
                        _arg14 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg22 = true;
                    }
                    setCurrentClientId(_arg02, _arg14, _arg22);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = true;
                    } else {
                        _arg0 = false;
                    }
                    setEnabled(_arg0);
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    setPlaybackState(data.readInt(), data.readInt(), data.readLong(), data.readLong(), data.readFloat());
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    setTransportControlInfo(data.readInt(), data.readInt(), data.readInt());
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    int _arg03 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg13 = Bundle.CREATOR.createFromParcel(data);
                    } else {
                        _arg13 = null;
                    }
                    setMetadata(_arg03, _arg13);
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    int _arg04 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg12 = Bitmap.CREATOR.createFromParcel(data);
                    } else {
                        _arg12 = null;
                    }
                    setArtwork(_arg04, _arg12);
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    int _arg05 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg1 = Bundle.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg2 = Bitmap.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    setAllMetadata(_arg05, _arg1, _arg2);
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*{ENCODED_INT: 1598968902}*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IRemoteControlDisplay {
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

            @Override // android.media.IRemoteControlDisplay
            public void setCurrentClientId(int clientGeneration, PendingIntent clientMediaIntent, boolean clearing) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(clientGeneration);
                    if (clientMediaIntent != null) {
                        _data.writeInt(1);
                        clientMediaIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!clearing) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.IRemoteControlDisplay
            public void setEnabled(boolean enabled) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!enabled) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.IRemoteControlDisplay
            public void setPlaybackState(int generationId, int state, long stateChangeTimeMs, long currentPosMs, float speed) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(generationId);
                    _data.writeInt(state);
                    _data.writeLong(stateChangeTimeMs);
                    _data.writeLong(currentPosMs);
                    _data.writeFloat(speed);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.IRemoteControlDisplay
            public void setTransportControlInfo(int generationId, int transportControlFlags, int posCapabilities) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(generationId);
                    _data.writeInt(transportControlFlags);
                    _data.writeInt(posCapabilities);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.IRemoteControlDisplay
            public void setMetadata(int generationId, Bundle metadata) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(generationId);
                    if (metadata != null) {
                        _data.writeInt(1);
                        metadata.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.IRemoteControlDisplay
            public void setArtwork(int generationId, Bitmap artwork) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(generationId);
                    if (artwork != null) {
                        _data.writeInt(1);
                        artwork.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(6, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.IRemoteControlDisplay
            public void setAllMetadata(int generationId, Bundle metadata, Bitmap artwork) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(generationId);
                    if (metadata != null) {
                        _data.writeInt(1);
                        metadata.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (artwork != null) {
                        _data.writeInt(1);
                        artwork.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(7, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }
    }
}

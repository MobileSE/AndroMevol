package android.media.tv;

import android.hardware.hdmi.HdmiDeviceInfo;
import android.media.tv.ITvInputServiceCallback;
import android.media.tv.ITvInputSessionCallback;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.InputChannel;

public interface ITvInputService extends IInterface {
    void createSession(InputChannel inputChannel, ITvInputSessionCallback iTvInputSessionCallback, String str) throws RemoteException;

    void notifyHardwareAdded(TvInputHardwareInfo tvInputHardwareInfo) throws RemoteException;

    void notifyHardwareRemoved(TvInputHardwareInfo tvInputHardwareInfo) throws RemoteException;

    void notifyHdmiDeviceAdded(HdmiDeviceInfo hdmiDeviceInfo) throws RemoteException;

    void notifyHdmiDeviceRemoved(HdmiDeviceInfo hdmiDeviceInfo) throws RemoteException;

    void registerCallback(ITvInputServiceCallback iTvInputServiceCallback) throws RemoteException;

    void unregisterCallback(ITvInputServiceCallback iTvInputServiceCallback) throws RemoteException;

    public static abstract class Stub extends Binder implements ITvInputService {
        private static final String DESCRIPTOR = "android.media.tv.ITvInputService";
        static final int TRANSACTION_createSession = 3;
        static final int TRANSACTION_notifyHardwareAdded = 4;
        static final int TRANSACTION_notifyHardwareRemoved = 5;
        static final int TRANSACTION_notifyHdmiDeviceAdded = 6;
        static final int TRANSACTION_notifyHdmiDeviceRemoved = 7;
        static final int TRANSACTION_registerCallback = 1;
        static final int TRANSACTION_unregisterCallback = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ITvInputService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ITvInputService)) {
                return new Proxy(obj);
            }
            return (ITvInputService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            HdmiDeviceInfo _arg0;
            HdmiDeviceInfo _arg02;
            TvInputHardwareInfo _arg03;
            TvInputHardwareInfo _arg04;
            InputChannel _arg05;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    registerCallback(ITvInputServiceCallback.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    unregisterCallback(ITvInputServiceCallback.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg05 = InputChannel.CREATOR.createFromParcel(data);
                    } else {
                        _arg05 = null;
                    }
                    createSession(_arg05, ITvInputSessionCallback.Stub.asInterface(data.readStrongBinder()), data.readString());
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg04 = TvInputHardwareInfo.CREATOR.createFromParcel(data);
                    } else {
                        _arg04 = null;
                    }
                    notifyHardwareAdded(_arg04);
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg03 = TvInputHardwareInfo.CREATOR.createFromParcel(data);
                    } else {
                        _arg03 = null;
                    }
                    notifyHardwareRemoved(_arg03);
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg02 = HdmiDeviceInfo.CREATOR.createFromParcel(data);
                    } else {
                        _arg02 = null;
                    }
                    notifyHdmiDeviceAdded(_arg02);
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = HdmiDeviceInfo.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    notifyHdmiDeviceRemoved(_arg0);
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*{ENCODED_INT: 1598968902}*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }

        private static class Proxy implements ITvInputService {
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

            @Override // android.media.tv.ITvInputService
            public void registerCallback(ITvInputServiceCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputService
            public void unregisterCallback(ITvInputServiceCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputService
            public void createSession(InputChannel channel, ITvInputSessionCallback callback, String inputId) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (channel != null) {
                        _data.writeInt(1);
                        channel.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeString(inputId);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputService
            public void notifyHardwareAdded(TvInputHardwareInfo hardwareInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (hardwareInfo != null) {
                        _data.writeInt(1);
                        hardwareInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputService
            public void notifyHardwareRemoved(TvInputHardwareInfo hardwareInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (hardwareInfo != null) {
                        _data.writeInt(1);
                        hardwareInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputService
            public void notifyHdmiDeviceAdded(HdmiDeviceInfo deviceInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (deviceInfo != null) {
                        _data.writeInt(1);
                        deviceInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(6, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputService
            public void notifyHdmiDeviceRemoved(HdmiDeviceInfo deviceInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (deviceInfo != null) {
                        _data.writeInt(1);
                        deviceInfo.writeToParcel(_data, 0);
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

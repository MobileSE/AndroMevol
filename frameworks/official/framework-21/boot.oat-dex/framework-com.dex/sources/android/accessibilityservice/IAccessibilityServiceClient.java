package android.accessibilityservice;

import android.accessibilityservice.IAccessibilityServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

public interface IAccessibilityServiceClient extends IInterface {
    void clearAccessibilityCache() throws RemoteException;

    void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) throws RemoteException;

    void onGesture(int i) throws RemoteException;

    void onInterrupt() throws RemoteException;

    void onKeyEvent(KeyEvent keyEvent, int i) throws RemoteException;

    void setConnection(IAccessibilityServiceConnection iAccessibilityServiceConnection, int i) throws RemoteException;

    public static abstract class Stub extends Binder implements IAccessibilityServiceClient {
        private static final String DESCRIPTOR = "android.accessibilityservice.IAccessibilityServiceClient";
        static final int TRANSACTION_clearAccessibilityCache = 5;
        static final int TRANSACTION_onAccessibilityEvent = 2;
        static final int TRANSACTION_onGesture = 4;
        static final int TRANSACTION_onInterrupt = 3;
        static final int TRANSACTION_onKeyEvent = 6;
        static final int TRANSACTION_setConnection = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IAccessibilityServiceClient asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IAccessibilityServiceClient)) {
                return new Proxy(obj);
            }
            return (IAccessibilityServiceClient) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            KeyEvent _arg0;
            AccessibilityEvent _arg02;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    setConnection(IAccessibilityServiceConnection.Stub.asInterface(data.readStrongBinder()), data.readInt());
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg02 = AccessibilityEvent.CREATOR.createFromParcel(data);
                    } else {
                        _arg02 = null;
                    }
                    onAccessibilityEvent(_arg02);
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    onInterrupt();
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    onGesture(data.readInt());
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    clearAccessibilityCache();
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = KeyEvent.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    onKeyEvent(_arg0, data.readInt());
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*{ENCODED_INT: 1598968902}*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }

        private static class Proxy implements IAccessibilityServiceClient {
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

            @Override // android.accessibilityservice.IAccessibilityServiceClient
            public void setConnection(IAccessibilityServiceConnection connection, int connectionId) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (connection != null) {
                        iBinder = connection.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(connectionId);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.accessibilityservice.IAccessibilityServiceClient
            public void onAccessibilityEvent(AccessibilityEvent event) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (event != null) {
                        _data.writeInt(1);
                        event.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.accessibilityservice.IAccessibilityServiceClient
            public void onInterrupt() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.accessibilityservice.IAccessibilityServiceClient
            public void onGesture(int gesture) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(gesture);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.accessibilityservice.IAccessibilityServiceClient
            public void clearAccessibilityCache() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.accessibilityservice.IAccessibilityServiceClient
            public void onKeyEvent(KeyEvent event, int sequence) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (event != null) {
                        _data.writeInt(1);
                        event.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(sequence);
                    this.mRemote.transact(6, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }
    }
}

package android.location;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IGpsMeasurementsListener extends IInterface {
    void onGpsMeasurementsReceived(GpsMeasurementsEvent gpsMeasurementsEvent) throws RemoteException;

    public static abstract class Stub extends Binder implements IGpsMeasurementsListener {
        private static final String DESCRIPTOR = "android.location.IGpsMeasurementsListener";
        static final int TRANSACTION_onGpsMeasurementsReceived = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IGpsMeasurementsListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IGpsMeasurementsListener)) {
                return new Proxy(obj);
            }
            return (IGpsMeasurementsListener) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            GpsMeasurementsEvent _arg0;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = GpsMeasurementsEvent.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    onGpsMeasurementsReceived(_arg0);
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*{ENCODED_INT: 1598968902}*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IGpsMeasurementsListener {
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

            @Override // android.location.IGpsMeasurementsListener
            public void onGpsMeasurementsReceived(GpsMeasurementsEvent event) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (event != null) {
                        _data.writeInt(1);
                        event.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }
    }
}

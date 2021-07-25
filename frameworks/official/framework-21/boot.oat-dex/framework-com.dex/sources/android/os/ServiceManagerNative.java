package android.os;

import android.os.IPermissionController;

public abstract class ServiceManagerNative extends Binder implements IServiceManager {
    public static IServiceManager asInterface(IBinder obj) {
        if (obj == null) {
            return null;
        }
        IServiceManager in = (IServiceManager) obj.queryLocalInterface(IServiceManager.descriptor);
        return in == null ? new ServiceManagerProxy(obj) : in;
    }

    public ServiceManagerNative() {
        attachInterface(this, IServiceManager.descriptor);
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    @Override // android.os.Binder
    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) {
        boolean allowIsolated;
        switch (code) {
            case 1:
                try {
                    data.enforceInterface(IServiceManager.descriptor);
                    reply.writeStrongBinder(getService(data.readString()));
                    return true;
                } catch (RemoteException e) {
                    break;
                }
            case 2:
                data.enforceInterface(IServiceManager.descriptor);
                reply.writeStrongBinder(checkService(data.readString()));
                return true;
            case 3:
                data.enforceInterface(IServiceManager.descriptor);
                String name = data.readString();
                IBinder service = data.readStrongBinder();
                if (data.readInt() != 0) {
                    allowIsolated = true;
                } else {
                    allowIsolated = false;
                }
                addService(name, service, allowIsolated);
                return true;
            case 4:
                data.enforceInterface(IServiceManager.descriptor);
                reply.writeStringArray(listServices());
                return true;
            case 6:
                data.enforceInterface(IServiceManager.descriptor);
                setPermissionController(IPermissionController.Stub.asInterface(data.readStrongBinder()));
                return true;
        }
        return false;
    }

    @Override // android.os.IInterface
    public IBinder asBinder() {
        return this;
    }
}

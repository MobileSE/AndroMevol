package android.hardware.camera2.utils;

import android.hardware.camera2.utils.CameraBinderDecorator;
import android.os.DeadObjectException;
import android.os.RemoteException;
import android.util.Log;
import java.lang.reflect.Method;

public class CameraServiceBinderDecorator extends CameraBinderDecorator {
    private static final String TAG = "CameraServiceBinderDecorator";

    /* access modifiers changed from: package-private */
    public static class CameraServiceBinderDecoratorListener extends CameraBinderDecorator.CameraBinderDecoratorListener {
        CameraServiceBinderDecoratorListener() {
        }

        @Override // android.hardware.camera2.utils.Decorator.DecoratorListener, android.hardware.camera2.utils.CameraBinderDecorator.CameraBinderDecoratorListener
        public boolean onCatchException(Method m, Object[] args, Throwable t) {
            if ((t instanceof DeadObjectException) || !(t instanceof RemoteException)) {
                return false;
            }
            Log.e(CameraServiceBinderDecorator.TAG, "Unexpected RemoteException from camera service call.", t);
            return false;
        }
    }

    public static <T> T newInstance(T obj) {
        return (T) Decorator.newInstance(obj, new CameraServiceBinderDecoratorListener());
    }
}

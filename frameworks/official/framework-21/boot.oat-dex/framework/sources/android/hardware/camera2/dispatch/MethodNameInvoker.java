package android.hardware.camera2.dispatch;

import android.hardware.camera2.utils.UncheckedThrow;
import com.android.internal.util.Preconditions;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

public class MethodNameInvoker<T> {
    private final ConcurrentHashMap<String, Method> mMethods = new ConcurrentHashMap<>();
    private final Dispatchable<T> mTarget;
    private final Class<T> mTargetClass;

    public MethodNameInvoker(Dispatchable<T> target, Class<T> targetClass) {
        this.mTargetClass = targetClass;
        this.mTarget = target;
    }

    public <K> K invoke(String methodName, Object... params) {
        Preconditions.checkNotNull(methodName, "methodName must not be null");
        Method targetMethod = this.mMethods.get(methodName);
        if (targetMethod == null) {
            Method[] arr$ = this.mTargetClass.getMethods();
            int len$ = arr$.length;
            int i$ = 0;
            while (true) {
                if (i$ >= len$) {
                    break;
                }
                Method method = arr$[i$];
                if (method.getName().equals(methodName) && params.length == method.getParameterTypes().length) {
                    targetMethod = method;
                    this.mMethods.put(methodName, targetMethod);
                    break;
                }
                i$++;
            }
            if (targetMethod == null) {
                throw new IllegalArgumentException("Method " + methodName + " does not exist on class " + this.mTargetClass);
            }
        }
        try {
            return (K) this.mTarget.dispatch(targetMethod, params);
        } catch (Throwable e) {
            UncheckedThrow.throwAnyException(e);
            return null;
        }
    }
}

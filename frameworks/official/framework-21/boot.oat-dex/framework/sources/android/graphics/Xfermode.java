package android.graphics;

public class Xfermode {
    long native_instance;

    private static native void finalizer(long j);

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            finalizer(this.native_instance);
        } finally {
            super.finalize();
        }
    }
}

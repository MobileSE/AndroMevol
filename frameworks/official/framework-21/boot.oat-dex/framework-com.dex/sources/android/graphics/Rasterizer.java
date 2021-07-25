package android.graphics;

@Deprecated
public class Rasterizer {
    long native_instance;

    private static native void finalizer(long j);

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        finalizer(this.native_instance);
    }
}

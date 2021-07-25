package android.graphics;

public class DrawFilter {
    long mNativeInt;

    private static native void nativeDestructor(long j);

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            nativeDestructor(this.mNativeInt);
        } finally {
            super.finalize();
        }
    }
}

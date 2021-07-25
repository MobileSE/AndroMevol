package android.graphics;

import android.graphics.PorterDuff;

public class PorterDuffXfermode extends Xfermode {
    public final PorterDuff.Mode mode;

    private static native long nativeCreateXfermode(int i);

    public PorterDuffXfermode(PorterDuff.Mode mode2) {
        this.mode = mode2;
        this.native_instance = nativeCreateXfermode(mode2.nativeInt);
    }
}

package android.graphics;

public class PaintFlagsDrawFilter extends DrawFilter {
    public final int clearBits;
    public final int setBits;

    private static native long nativeConstructor(int i, int i2);

    public PaintFlagsDrawFilter(int clearBits2, int setBits2) {
        this.clearBits = clearBits2;
        this.setBits = setBits2;
        this.mNativeInt = nativeConstructor(clearBits2, setBits2);
    }
}

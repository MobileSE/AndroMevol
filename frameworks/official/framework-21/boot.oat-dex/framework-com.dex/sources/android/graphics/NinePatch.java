package android.graphics;

public class NinePatch {
    private final Bitmap mBitmap;
    public final long mNativeChunk;
    private Paint mPaint;
    private String mSrcName;

    public static native boolean isNinePatchChunk(byte[] bArr);

    private static native void nativeDraw(long j, Rect rect, long j2, long j3, long j4, int i, int i2);

    private static native void nativeDraw(long j, RectF rectF, long j2, long j3, long j4, int i, int i2);

    private static native void nativeFinalize(long j);

    private static native long nativeGetTransparentRegion(long j, long j2, Rect rect);

    private static native long validateNinePatchChunk(long j, byte[] bArr);

    public static class InsetStruct {
        public final Rect opticalRect;
        public final float outlineAlpha;
        public final float outlineRadius;
        public final Rect outlineRect;

        InsetStruct(int opticalLeft, int opticalTop, int opticalRight, int opticalBottom, int outlineLeft, int outlineTop, int outlineRight, int outlineBottom, float outlineRadius2, int outlineAlpha2, float decodeScale) {
            this.opticalRect = new Rect(opticalLeft, opticalTop, opticalRight, opticalBottom);
            this.outlineRect = new Rect(outlineLeft, outlineTop, outlineRight, outlineBottom);
            if (decodeScale != 1.0f) {
                this.opticalRect.scale(decodeScale);
                this.outlineRect.scaleRoundIn(decodeScale);
            }
            this.outlineRadius = outlineRadius2 * decodeScale;
            this.outlineAlpha = ((float) outlineAlpha2) / 255.0f;
        }
    }

    public NinePatch(Bitmap bitmap, byte[] chunk) {
        this(bitmap, chunk, null);
    }

    public NinePatch(Bitmap bitmap, byte[] chunk, String srcName) {
        this.mBitmap = bitmap;
        this.mSrcName = srcName;
        this.mNativeChunk = validateNinePatchChunk(this.mBitmap.ni(), chunk);
    }

    public NinePatch(NinePatch patch) {
        this.mBitmap = patch.mBitmap;
        this.mSrcName = patch.mSrcName;
        if (patch.mPaint != null) {
            this.mPaint = new Paint(patch.mPaint);
        }
        this.mNativeChunk = patch.mNativeChunk;
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            if (this.mNativeChunk != 0) {
                nativeFinalize(this.mNativeChunk);
            }
        } finally {
            super.finalize();
        }
    }

    public String getName() {
        return this.mSrcName;
    }

    public Paint getPaint() {
        return this.mPaint;
    }

    public void setPaint(Paint p) {
        this.mPaint = p;
    }

    public Bitmap getBitmap() {
        return this.mBitmap;
    }

    public void draw(Canvas canvas, RectF location) {
        canvas.drawPatch(this, location, this.mPaint);
    }

    public void draw(Canvas canvas, Rect location) {
        canvas.drawPatch(this, location, this.mPaint);
    }

    public void draw(Canvas canvas, Rect location, Paint paint) {
        canvas.drawPatch(this, location, paint);
    }

    /* access modifiers changed from: package-private */
    public void drawSoftware(Canvas canvas, RectF location, Paint paint) {
        nativeDraw(canvas.getNativeCanvasWrapper(), location, this.mBitmap.ni(), this.mNativeChunk, paint != null ? paint.mNativePaint : 0, canvas.mDensity, this.mBitmap.mDensity);
    }

    /* access modifiers changed from: package-private */
    public void drawSoftware(Canvas canvas, Rect location, Paint paint) {
        nativeDraw(canvas.getNativeCanvasWrapper(), location, this.mBitmap.ni(), this.mNativeChunk, paint != null ? paint.mNativePaint : 0, canvas.mDensity, this.mBitmap.mDensity);
    }

    public int getDensity() {
        return this.mBitmap.mDensity;
    }

    public int getWidth() {
        return this.mBitmap.getWidth();
    }

    public int getHeight() {
        return this.mBitmap.getHeight();
    }

    public final boolean hasAlpha() {
        return this.mBitmap.hasAlpha();
    }

    public final Region getTransparentRegion(Rect bounds) {
        long r = nativeGetTransparentRegion(this.mBitmap.ni(), this.mNativeChunk, bounds);
        if (r != 0) {
            return new Region(r);
        }
        return null;
    }
}

package android.graphics;

import java.io.InputStream;
import java.io.OutputStream;

public class Picture {
    private static final int WORKING_STREAM_STORAGE = 16384;
    private final long mNativePicture;
    private Canvas mRecordingCanvas;

    private static native long nativeBeginRecording(long j, int i, int i2);

    private static native long nativeConstructor(long j);

    private static native long nativeCreateFromStream(InputStream inputStream, byte[] bArr);

    private static native void nativeDestructor(long j);

    private static native void nativeDraw(long j, long j2);

    private static native void nativeEndRecording(long j);

    private static native int nativeGetHeight(long j);

    private static native int nativeGetWidth(long j);

    private static native boolean nativeWriteToStream(long j, OutputStream outputStream, byte[] bArr);

    public Picture() {
        this(nativeConstructor(0));
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public Picture(Picture src) {
        this(nativeConstructor(src != null ? src.mNativePicture : 0));
    }

    private Picture(long nativePicture) {
        if (nativePicture == 0) {
            throw new RuntimeException();
        }
        this.mNativePicture = nativePicture;
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            nativeDestructor(this.mNativePicture);
        } finally {
            super.finalize();
        }
    }

    public Canvas beginRecording(int width, int height) {
        this.mRecordingCanvas = new RecordingCanvas(this, nativeBeginRecording(this.mNativePicture, width, height));
        return this.mRecordingCanvas;
    }

    public void endRecording() {
        if (this.mRecordingCanvas != null) {
            this.mRecordingCanvas = null;
            nativeEndRecording(this.mNativePicture);
        }
    }

    public int getWidth() {
        return nativeGetWidth(this.mNativePicture);
    }

    public int getHeight() {
        return nativeGetHeight(this.mNativePicture);
    }

    public void draw(Canvas canvas) {
        if (canvas.isHardwareAccelerated()) {
            throw new IllegalArgumentException("Picture playback is only supported on software canvas.");
        }
        if (this.mRecordingCanvas != null) {
            endRecording();
        }
        nativeDraw(canvas.getNativeCanvasWrapper(), this.mNativePicture);
    }

    @Deprecated
    public static Picture createFromStream(InputStream stream) {
        return new Picture(nativeCreateFromStream(stream, new byte[16384]));
    }

    @Deprecated
    public void writeToStream(OutputStream stream) {
        if (stream == null) {
            throw new NullPointerException();
        } else if (!nativeWriteToStream(this.mNativePicture, stream, new byte[16384])) {
            throw new RuntimeException();
        }
    }

    private static class RecordingCanvas extends Canvas {
        private final Picture mPicture;

        public RecordingCanvas(Picture pict, long nativeCanvas) {
            super(nativeCanvas);
            this.mPicture = pict;
        }

        @Override // android.graphics.Canvas
        public void setBitmap(Bitmap bitmap) {
            throw new RuntimeException("Cannot call setBitmap on a picture canvas");
        }

        @Override // android.graphics.Canvas
        public void drawPicture(Picture picture) {
            if (this.mPicture == picture) {
                throw new RuntimeException("Cannot draw a picture into its recording canvas");
            }
            super.drawPicture(picture);
        }
    }
}

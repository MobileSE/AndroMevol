package android.media;

import android.graphics.Rect;
import java.nio.ByteBuffer;

public abstract class Image implements AutoCloseable {
    private Rect mCropRect;

    @Override // java.lang.AutoCloseable
    public abstract void close();

    public abstract int getFormat();

    public abstract int getHeight();

    public abstract Plane[] getPlanes();

    public abstract long getTimestamp();

    public abstract int getWidth();

    protected Image() {
    }

    public Rect getCropRect() {
        if (this.mCropRect == null) {
            return new Rect(0, 0, getWidth(), getHeight());
        }
        return new Rect(this.mCropRect);
    }

    public void setCropRect(Rect cropRect) {
        Rect cropRect2 = new Rect(cropRect);
        cropRect2.intersect(0, 0, getWidth(), getHeight());
        this.mCropRect = cropRect2;
    }

    public static abstract class Plane {
        public abstract ByteBuffer getBuffer();

        public abstract int getPixelStride();

        public abstract int getRowStride();

        protected Plane() {
        }
    }
}

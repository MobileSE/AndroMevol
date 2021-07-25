package android.telecom;

import android.os.Parcel;
import android.os.Parcelable;

public final class CameraCapabilities implements Parcelable {
    public static final Parcelable.Creator<CameraCapabilities> CREATOR = new Parcelable.Creator<CameraCapabilities>() {
        /* class android.telecom.CameraCapabilities.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public CameraCapabilities createFromParcel(Parcel source) {
            return new CameraCapabilities(source.readByte() != 0, source.readFloat(), source.readInt(), source.readInt());
        }

        @Override // android.os.Parcelable.Creator
        public CameraCapabilities[] newArray(int size) {
            return new CameraCapabilities[size];
        }
    };
    private final int mHeight;
    private final float mMaxZoom;
    private final int mWidth;
    private final boolean mZoomSupported;

    public CameraCapabilities(boolean zoomSupported, float maxZoom, int width, int height) {
        this.mZoomSupported = zoomSupported;
        this.mMaxZoom = maxZoom;
        this.mWidth = width;
        this.mHeight = height;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (isZoomSupported() ? 1 : 0));
        dest.writeFloat(getMaxZoom());
        dest.writeInt(getWidth());
        dest.writeInt(getHeight());
    }

    public boolean isZoomSupported() {
        return this.mZoomSupported;
    }

    public float getMaxZoom() {
        return this.mMaxZoom;
    }

    public int getWidth() {
        return this.mWidth;
    }

    public int getHeight() {
        return this.mHeight;
    }
}

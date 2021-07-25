package android.graphics;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.FloatMath;

public class PointF implements Parcelable {
    public static final Parcelable.Creator<PointF> CREATOR = new Parcelable.Creator<PointF>() {
        /* class android.graphics.PointF.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public PointF createFromParcel(Parcel in) {
            PointF r = new PointF();
            r.readFromParcel(in);
            return r;
        }

        @Override // android.os.Parcelable.Creator
        public PointF[] newArray(int size) {
            return new PointF[size];
        }
    };
    public float x;
    public float y;

    public PointF() {
    }

    public PointF(float x2, float y2) {
        this.x = x2;
        this.y = y2;
    }

    public PointF(Point p) {
        this.x = (float) p.x;
        this.y = (float) p.y;
    }

    public final void set(float x2, float y2) {
        this.x = x2;
        this.y = y2;
    }

    public final void set(PointF p) {
        this.x = p.x;
        this.y = p.y;
    }

    public final void negate() {
        this.x = -this.x;
        this.y = -this.y;
    }

    public final void offset(float dx, float dy) {
        this.x += dx;
        this.y += dy;
    }

    public final boolean equals(float x2, float y2) {
        return this.x == x2 && this.y == y2;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PointF pointF = (PointF) o;
        if (Float.compare(pointF.x, this.x) != 0) {
            return false;
        }
        return Float.compare(pointF.y, this.y) == 0;
    }

    public int hashCode() {
        int result;
        int i = 0;
        if (this.x != 0.0f) {
            result = Float.floatToIntBits(this.x);
        } else {
            result = 0;
        }
        int i2 = result * 31;
        if (this.y != 0.0f) {
            i = Float.floatToIntBits(this.y);
        }
        return i2 + i;
    }

    public String toString() {
        return "PointF(" + this.x + ", " + this.y + ")";
    }

    public final float length() {
        return length(this.x, this.y);
    }

    public static float length(float x2, float y2) {
        return FloatMath.sqrt((x2 * x2) + (y2 * y2));
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeFloat(this.x);
        out.writeFloat(this.y);
    }

    public void readFromParcel(Parcel in) {
        this.x = in.readFloat();
        this.y = in.readFloat();
    }
}

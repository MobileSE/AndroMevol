package android.util;

import android.app.backup.FullBackup;

public class TypedValue {
    public static final int COMPLEX_MANTISSA_MASK = 16777215;
    public static final int COMPLEX_MANTISSA_SHIFT = 8;
    public static final int COMPLEX_RADIX_0p23 = 3;
    public static final int COMPLEX_RADIX_16p7 = 1;
    public static final int COMPLEX_RADIX_23p0 = 0;
    public static final int COMPLEX_RADIX_8p15 = 2;
    public static final int COMPLEX_RADIX_MASK = 3;
    public static final int COMPLEX_RADIX_SHIFT = 4;
    public static final int COMPLEX_UNIT_DIP = 1;
    public static final int COMPLEX_UNIT_FRACTION = 0;
    public static final int COMPLEX_UNIT_FRACTION_PARENT = 1;
    public static final int COMPLEX_UNIT_IN = 4;
    public static final int COMPLEX_UNIT_MASK = 15;
    public static final int COMPLEX_UNIT_MM = 5;
    public static final int COMPLEX_UNIT_PT = 3;
    public static final int COMPLEX_UNIT_PX = 0;
    public static final int COMPLEX_UNIT_SHIFT = 0;
    public static final int COMPLEX_UNIT_SP = 2;
    public static final int DENSITY_DEFAULT = 0;
    public static final int DENSITY_NONE = 65535;
    private static final String[] DIMENSION_UNIT_STRS = {"px", "dip", FullBackup.SHAREDPREFS_TREE_TOKEN, "pt", "in", "mm"};
    private static final String[] FRACTION_UNIT_STRS = {"%", "%p"};
    private static final float MANTISSA_MULT = 0.00390625f;
    private static final float[] RADIX_MULTS = {MANTISSA_MULT, 3.0517578E-5f, 1.1920929E-7f, 4.656613E-10f};
    public static final int TYPE_ATTRIBUTE = 2;
    public static final int TYPE_DIMENSION = 5;
    public static final int TYPE_FIRST_COLOR_INT = 28;
    public static final int TYPE_FIRST_INT = 16;
    public static final int TYPE_FLOAT = 4;
    public static final int TYPE_FRACTION = 6;
    public static final int TYPE_INT_BOOLEAN = 18;
    public static final int TYPE_INT_COLOR_ARGB4 = 30;
    public static final int TYPE_INT_COLOR_ARGB8 = 28;
    public static final int TYPE_INT_COLOR_RGB4 = 31;
    public static final int TYPE_INT_COLOR_RGB8 = 29;
    public static final int TYPE_INT_DEC = 16;
    public static final int TYPE_INT_HEX = 17;
    public static final int TYPE_LAST_COLOR_INT = 31;
    public static final int TYPE_LAST_INT = 31;
    public static final int TYPE_NULL = 0;
    public static final int TYPE_REFERENCE = 1;
    public static final int TYPE_STRING = 3;
    public int assetCookie;
    public int changingConfigurations = -1;
    public int data;
    public int density;
    public int resourceId;
    public CharSequence string;
    public int type;

    public final float getFloat() {
        return Float.intBitsToFloat(this.data);
    }

    public static float complexToFloat(int complex) {
        return ((float) (complex & -256)) * RADIX_MULTS[(complex >> 4) & 3];
    }

    public static float complexToDimension(int data2, DisplayMetrics metrics) {
        return applyDimension((data2 >> 0) & 15, complexToFloat(data2), metrics);
    }

    public static int complexToDimensionPixelOffset(int data2, DisplayMetrics metrics) {
        return (int) applyDimension((data2 >> 0) & 15, complexToFloat(data2), metrics);
    }

    public static int complexToDimensionPixelSize(int data2, DisplayMetrics metrics) {
        float value = complexToFloat(data2);
        int res = (int) (0.5f + applyDimension((data2 >> 0) & 15, value, metrics));
        if (res != 0) {
            return res;
        }
        if (value == 0.0f) {
            return 0;
        }
        if (value > 0.0f) {
            return 1;
        }
        return -1;
    }

    @Deprecated
    public static float complexToDimensionNoisy(int data2, DisplayMetrics metrics) {
        return complexToDimension(data2, metrics);
    }

    public static float applyDimension(int unit, float value, DisplayMetrics metrics) {
        switch (unit) {
            case 0:
                return value;
            case 1:
                return value * metrics.density;
            case 2:
                return value * metrics.scaledDensity;
            case 3:
                return metrics.xdpi * value * 0.013888889f;
            case 4:
                return value * metrics.xdpi;
            case 5:
                return metrics.xdpi * value * 0.03937008f;
            default:
                return 0.0f;
        }
    }

    public float getDimension(DisplayMetrics metrics) {
        return complexToDimension(this.data, metrics);
    }

    public static float complexToFraction(int data2, float base, float pbase) {
        switch ((data2 >> 0) & 15) {
            case 0:
                return complexToFloat(data2) * base;
            case 1:
                return complexToFloat(data2) * pbase;
            default:
                return 0.0f;
        }
    }

    public float getFraction(float base, float pbase) {
        return complexToFraction(this.data, base, pbase);
    }

    public final CharSequence coerceToString() {
        int t = this.type;
        if (t == 3) {
            return this.string;
        }
        return coerceToString(t, this.data);
    }

    public static final String coerceToString(int type2, int data2) {
        switch (type2) {
            case 0:
                return null;
            case 1:
                return "@" + data2;
            case 2:
                return "?" + data2;
            case 3:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            default:
                if (type2 >= 28 && type2 <= 31) {
                    return "#" + Integer.toHexString(data2);
                }
                if (type2 < 16 || type2 > 31) {
                    return null;
                }
                return Integer.toString(data2);
            case 4:
                return Float.toString(Float.intBitsToFloat(data2));
            case 5:
                return Float.toString(complexToFloat(data2)) + DIMENSION_UNIT_STRS[(data2 >> 0) & 15];
            case 6:
                return Float.toString(complexToFloat(data2) * 100.0f) + FRACTION_UNIT_STRS[(data2 >> 0) & 15];
            case 17:
                return "0x" + Integer.toHexString(data2);
            case 18:
                return data2 != 0 ? "true" : "false";
        }
    }

    public void setTo(TypedValue other) {
        this.type = other.type;
        this.string = other.string;
        this.data = other.data;
        this.assetCookie = other.assetCookie;
        this.resourceId = other.resourceId;
        this.density = other.density;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TypedValue{t=0x").append(Integer.toHexString(this.type));
        sb.append("/d=0x").append(Integer.toHexString(this.data));
        if (this.type == 3) {
            sb.append(" \"").append(this.string != null ? this.string : "<null>").append("\"");
        }
        if (this.assetCookie != 0) {
            sb.append(" a=").append(this.assetCookie);
        }
        if (this.resourceId != 0) {
            sb.append(" r=0x").append(Integer.toHexString(this.resourceId));
        }
        sb.append("}");
        return sb.toString();
    }
}

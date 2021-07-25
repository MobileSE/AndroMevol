package android.graphics;

import android.hardware.Camera;
import android.util.MathUtils;
import com.android.internal.util.XmlUtils;
import java.util.HashMap;
import java.util.Locale;

public class Color {
    public static final int BLACK = -16777216;
    public static final int BLUE = -16776961;
    public static final int CYAN = -16711681;
    public static final int DKGRAY = -12303292;
    public static final int GRAY = -7829368;
    public static final int GREEN = -16711936;
    public static final int LTGRAY = -3355444;
    public static final int MAGENTA = -65281;
    public static final int RED = -65536;
    public static final int TRANSPARENT = 0;
    public static final int WHITE = -1;
    public static final int YELLOW = -256;
    private static final HashMap<String, Integer> sColorNameMap = new HashMap<>();

    private static native int nativeHSVToColor(int i, float[] fArr);

    private static native void nativeRGBToHSV(int i, int i2, int i3, float[] fArr);

    public static int alpha(int color) {
        return color >>> 24;
    }

    public static int red(int color) {
        return (color >> 16) & 255;
    }

    public static int green(int color) {
        return (color >> 8) & 255;
    }

    public static int blue(int color) {
        return color & 255;
    }

    public static int rgb(int red, int green, int blue) {
        return -16777216 | (red << 16) | (green << 8) | blue;
    }

    public static int argb(int alpha, int red, int green, int blue) {
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    public static float hue(int color) {
        float H;
        int r = (color >> 16) & 255;
        int g = (color >> 8) & 255;
        int b = color & 255;
        int V = Math.max(b, Math.max(r, g));
        int temp = Math.min(b, Math.min(r, g));
        if (V == temp) {
            return 0.0f;
        }
        float vtemp = (float) (V - temp);
        float cr = ((float) (V - r)) / vtemp;
        float cg = ((float) (V - g)) / vtemp;
        float cb = ((float) (V - b)) / vtemp;
        if (r == V) {
            H = cb - cg;
        } else if (g == V) {
            H = (2.0f + cr) - cb;
        } else {
            H = (4.0f + cg) - cr;
        }
        float H2 = H / 6.0f;
        if (H2 < 0.0f) {
            return H2 + 1.0f;
        }
        return H2;
    }

    public static float saturation(int color) {
        int r = (color >> 16) & 255;
        int g = (color >> 8) & 255;
        int b = color & 255;
        int V = Math.max(b, Math.max(r, g));
        int temp = Math.min(b, Math.min(r, g));
        if (V == temp) {
            return 0.0f;
        }
        return ((float) (V - temp)) / ((float) V);
    }

    public static float brightness(int color) {
        return ((float) Math.max(color & 255, Math.max((color >> 16) & 255, (color >> 8) & 255))) / 255.0f;
    }

    public static int parseColor(String colorString) {
        if (colorString.charAt(0) == '#') {
            long color = Long.parseLong(colorString.substring(1), 16);
            if (colorString.length() == 7) {
                color |= -16777216;
            } else if (colorString.length() != 9) {
                throw new IllegalArgumentException("Unknown color");
            }
            return (int) color;
        }
        Integer color2 = sColorNameMap.get(colorString.toLowerCase(Locale.ROOT));
        if (color2 != null) {
            return color2.intValue();
        }
        throw new IllegalArgumentException("Unknown color");
    }

    public static int HSBtoColor(float[] hsb) {
        return HSBtoColor(hsb[0], hsb[1], hsb[2]);
    }

    public static int HSBtoColor(float h, float s, float b) {
        float h2 = MathUtils.constrain(h, 0.0f, 1.0f);
        float s2 = MathUtils.constrain(s, 0.0f, 1.0f);
        float b2 = MathUtils.constrain(b, 0.0f, 1.0f);
        float red = 0.0f;
        float green = 0.0f;
        float blue = 0.0f;
        float hf = (h2 - ((float) ((int) h2))) * 6.0f;
        int ihf = (int) hf;
        float f = hf - ((float) ihf);
        float pv = b2 * (1.0f - s2);
        float qv = b2 * (1.0f - (s2 * f));
        float tv = b2 * (1.0f - ((1.0f - f) * s2));
        switch (ihf) {
            case 0:
                red = b2;
                green = tv;
                blue = pv;
                break;
            case 1:
                red = qv;
                green = b2;
                blue = pv;
                break;
            case 2:
                red = pv;
                green = b2;
                blue = tv;
                break;
            case 3:
                red = pv;
                green = qv;
                blue = b2;
                break;
            case 4:
                red = tv;
                green = pv;
                blue = b2;
                break;
            case 5:
                red = b2;
                green = pv;
                blue = qv;
                break;
        }
        return -16777216 | (((int) (red * 255.0f)) << 16) | (((int) (green * 255.0f)) << 8) | ((int) (blue * 255.0f));
    }

    public static void RGBToHSV(int red, int green, int blue, float[] hsv) {
        if (hsv.length < 3) {
            throw new RuntimeException("3 components required for hsv");
        }
        nativeRGBToHSV(red, green, blue, hsv);
    }

    public static void colorToHSV(int color, float[] hsv) {
        RGBToHSV((color >> 16) & 255, (color >> 8) & 255, color & 255, hsv);
    }

    public static int HSVToColor(float[] hsv) {
        return HSVToColor(255, hsv);
    }

    public static int HSVToColor(int alpha, float[] hsv) {
        if (hsv.length >= 3) {
            return nativeHSVToColor(alpha, hsv);
        }
        throw new RuntimeException("3 components required for hsv");
    }

    public static int getHtmlColor(String color) {
        Integer i = sColorNameMap.get(color.toLowerCase(Locale.ROOT));
        if (i != null) {
            return i.intValue();
        }
        try {
            return XmlUtils.convertValueToInt(color, -1);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    static {
        sColorNameMap.put("black", -16777216);
        sColorNameMap.put("darkgray", Integer.valueOf((int) DKGRAY));
        sColorNameMap.put("gray", Integer.valueOf((int) GRAY));
        sColorNameMap.put("lightgray", Integer.valueOf((int) LTGRAY));
        sColorNameMap.put("white", -1);
        sColorNameMap.put("red", -65536);
        sColorNameMap.put("green", Integer.valueOf((int) GREEN));
        sColorNameMap.put("blue", Integer.valueOf((int) BLUE));
        sColorNameMap.put("yellow", -256);
        sColorNameMap.put("cyan", Integer.valueOf((int) CYAN));
        sColorNameMap.put("magenta", Integer.valueOf((int) MAGENTA));
        sColorNameMap.put(Camera.Parameters.EFFECT_AQUA, Integer.valueOf((int) CYAN));
        sColorNameMap.put("fuchsia", Integer.valueOf((int) MAGENTA));
        sColorNameMap.put("darkgrey", Integer.valueOf((int) DKGRAY));
        sColorNameMap.put("grey", Integer.valueOf((int) GRAY));
        sColorNameMap.put("lightgrey", Integer.valueOf((int) LTGRAY));
        sColorNameMap.put("lime", Integer.valueOf((int) GREEN));
        sColorNameMap.put("maroon", -8388608);
        sColorNameMap.put("navy", -16777088);
        sColorNameMap.put("olive", -8355840);
        sColorNameMap.put("purple", -8388480);
        sColorNameMap.put("silver", -4144960);
        sColorNameMap.put("teal", -16744320);
    }
}

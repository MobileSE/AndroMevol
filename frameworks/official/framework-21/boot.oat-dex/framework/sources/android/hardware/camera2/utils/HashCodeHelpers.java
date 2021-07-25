package android.hardware.camera2.utils;

public final class HashCodeHelpers {
    public static int hashCode(int[] array) {
        if (array == null) {
            return 0;
        }
        int h = 1;
        for (int x : array) {
            h = ((h << 5) - h) ^ x;
        }
        return h;
    }

    public static int hashCode(float[] array) {
        if (array == null) {
            return 0;
        }
        int h = 1;
        for (float f : array) {
            h = ((h << 5) - h) ^ Float.floatToIntBits(f);
        }
        return h;
    }

    public static <T> int hashCode(T[] array) {
        if (array == null) {
            return 0;
        }
        int h = 1;
        int len$ = array.length;
        for (int i$ = 0; i$ < len$; i$++) {
            T o = array[i$];
            h = ((h << 5) - h) ^ (o == null ? 0 : o.hashCode());
        }
        return h;
    }

    public static <T> int hashCode(T a) {
        if (a == null) {
            return 0;
        }
        return a.hashCode();
    }

    public static <T> int hashCode(T a, T b) {
        int h = hashCode(a);
        return ((h << 5) - h) ^ (b == null ? 0 : b.hashCode());
    }

    public static <T> int hashCode(T a, T b, T c) {
        int h = hashCode(a, b);
        return ((h << 5) - h) ^ (c == null ? 0 : c.hashCode());
    }

    public static <T> int hashCode(T a, T b, T c, T d) {
        int h = hashCode(a, b, c);
        return ((h << 5) - h) ^ (d == null ? 0 : d.hashCode());
    }

    public static int hashCode(int x) {
        return hashCode(new int[]{x});
    }

    public static int hashCode(int x, int y) {
        return hashCode(new int[]{x, y});
    }

    public static int hashCode(int x, int y, int z) {
        return hashCode(new int[]{x, y, z});
    }

    public static int hashCode(int x, int y, int z, int w) {
        return hashCode(new int[]{x, y, z, w});
    }

    public static int hashCode(int x, int y, int z, int w, int t) {
        return hashCode(new int[]{x, y, z, w, t});
    }
}

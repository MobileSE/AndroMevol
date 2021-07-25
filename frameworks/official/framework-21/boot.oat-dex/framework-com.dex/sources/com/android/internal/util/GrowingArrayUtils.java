package com.android.internal.util;

public final class GrowingArrayUtils {
    static final /* synthetic */ boolean $assertionsDisabled = (!GrowingArrayUtils.class.desiredAssertionStatus());

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v0, types: [java.lang.Object[], java.lang.Object] */
    /* JADX WARNING: Unknown variable types count: 1 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static <T> T[] append(T[] r4, int r5, T r6) {
        /*
            r3 = 0
            boolean r1 = com.android.internal.util.GrowingArrayUtils.$assertionsDisabled
            if (r1 != 0) goto L_0x000e
            int r1 = r4.length
            if (r5 <= r1) goto L_0x000e
            java.lang.AssertionError r1 = new java.lang.AssertionError
            r1.<init>()
            throw r1
        L_0x000e:
            int r1 = r5 + 1
            int r2 = r4.length
            if (r1 <= r2) goto L_0x0027
            java.lang.Class r1 = r4.getClass()
            java.lang.Class r1 = r1.getComponentType()
            int r2 = growSize(r5)
            java.lang.Object[] r0 = com.android.internal.util.ArrayUtils.newUnpaddedArray(r1, r2)
            java.lang.System.arraycopy(r4, r3, r0, r3, r5)
            r4 = r0
        L_0x0027:
            r4[r5] = r6
            return r4
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.util.GrowingArrayUtils.append(java.lang.Object[], int, java.lang.Object):java.lang.Object[]");
    }

    public static int[] append(int[] array, int currentSize, int element) {
        if ($assertionsDisabled || currentSize <= array.length) {
            if (currentSize + 1 > array.length) {
                int[] newArray = ArrayUtils.newUnpaddedIntArray(growSize(currentSize));
                System.arraycopy(array, 0, newArray, 0, currentSize);
                array = newArray;
            }
            array[currentSize] = element;
            return array;
        }
        throw new AssertionError();
    }

    public static long[] append(long[] array, int currentSize, long element) {
        if ($assertionsDisabled || currentSize <= array.length) {
            if (currentSize + 1 > array.length) {
                long[] newArray = ArrayUtils.newUnpaddedLongArray(growSize(currentSize));
                System.arraycopy(array, 0, newArray, 0, currentSize);
                array = newArray;
            }
            array[currentSize] = element;
            return array;
        }
        throw new AssertionError();
    }

    public static boolean[] append(boolean[] array, int currentSize, boolean element) {
        if ($assertionsDisabled || currentSize <= array.length) {
            if (currentSize + 1 > array.length) {
                boolean[] newArray = ArrayUtils.newUnpaddedBooleanArray(growSize(currentSize));
                System.arraycopy(array, 0, newArray, 0, currentSize);
                array = newArray;
            }
            array[currentSize] = element;
            return array;
        }
        throw new AssertionError();
    }

    public static <T> T[] insert(T[] array, int currentSize, int index, T element) {
        if (!$assertionsDisabled && currentSize > array.length) {
            throw new AssertionError();
        } else if (currentSize + 1 <= array.length) {
            System.arraycopy(array, index, array, index + 1, currentSize - index);
            array[index] = element;
            return array;
        } else {
            T[] newArray = (T[]) ArrayUtils.newUnpaddedArray(array.getClass().getComponentType(), growSize(currentSize));
            System.arraycopy(array, 0, newArray, 0, index);
            newArray[index] = element;
            System.arraycopy(array, index, newArray, index + 1, array.length - index);
            return newArray;
        }
    }

    public static int[] insert(int[] array, int currentSize, int index, int element) {
        if (!$assertionsDisabled && currentSize > array.length) {
            throw new AssertionError();
        } else if (currentSize + 1 <= array.length) {
            System.arraycopy(array, index, array, index + 1, currentSize - index);
            array[index] = element;
            return array;
        } else {
            int[] newArray = ArrayUtils.newUnpaddedIntArray(growSize(currentSize));
            System.arraycopy(array, 0, newArray, 0, index);
            newArray[index] = element;
            System.arraycopy(array, index, newArray, index + 1, array.length - index);
            return newArray;
        }
    }

    public static long[] insert(long[] array, int currentSize, int index, long element) {
        if (!$assertionsDisabled && currentSize > array.length) {
            throw new AssertionError();
        } else if (currentSize + 1 <= array.length) {
            System.arraycopy(array, index, array, index + 1, currentSize - index);
            array[index] = element;
            return array;
        } else {
            long[] newArray = ArrayUtils.newUnpaddedLongArray(growSize(currentSize));
            System.arraycopy(array, 0, newArray, 0, index);
            newArray[index] = element;
            System.arraycopy(array, index, newArray, index + 1, array.length - index);
            return newArray;
        }
    }

    public static boolean[] insert(boolean[] array, int currentSize, int index, boolean element) {
        if (!$assertionsDisabled && currentSize > array.length) {
            throw new AssertionError();
        } else if (currentSize + 1 <= array.length) {
            System.arraycopy(array, index, array, index + 1, currentSize - index);
            array[index] = element;
            return array;
        } else {
            boolean[] newArray = ArrayUtils.newUnpaddedBooleanArray(growSize(currentSize));
            System.arraycopy(array, 0, newArray, 0, index);
            newArray[index] = element;
            System.arraycopy(array, index, newArray, index + 1, array.length - index);
            return newArray;
        }
    }

    public static int growSize(int currentSize) {
        if (currentSize <= 4) {
            return 8;
        }
        return currentSize * 2;
    }

    private GrowingArrayUtils() {
    }
}

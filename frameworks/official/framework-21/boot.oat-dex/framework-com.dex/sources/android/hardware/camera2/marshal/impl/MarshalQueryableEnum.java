package android.hardware.camera2.marshal.impl;

import android.hardware.camera2.marshal.MarshalHelpers;
import android.hardware.camera2.marshal.MarshalQueryable;
import android.hardware.camera2.marshal.Marshaler;
import android.hardware.camera2.utils.TypeReference;
import android.os.BatteryStats;
import android.util.Log;
import java.lang.Enum;
import java.nio.ByteBuffer;
import java.util.HashMap;

public class MarshalQueryableEnum<T extends Enum<T>> implements MarshalQueryable<T> {
    private static final String TAG = MarshalQueryableEnum.class.getSimpleName();
    private static final int UINT8_MASK = 255;
    private static final int UINT8_MAX = 255;
    private static final int UINT8_MIN = 0;
    private static final boolean VERBOSE = Log.isLoggable(TAG, 2);
    private static final HashMap<Class<? extends Enum>, int[]> sEnumValues = new HashMap<>();

    private class MarshalerEnum extends Marshaler<T> {
        private final Class<T> mClass;

        /* JADX DEBUG: Type inference failed for r0v0. Raw type applied. Possible types: java.lang.Class<? super T extends java.lang.Enum<T>>, java.lang.Class<T> */
        protected MarshalerEnum(TypeReference<T> typeReference, int nativeType) {
            super(MarshalQueryableEnum.this, typeReference, nativeType);
            this.mClass = (Class<? super T>) typeReference.getRawType();
        }

        public void marshal(T value, ByteBuffer buffer) {
            int enumValue = MarshalQueryableEnum.getEnumValue(value);
            if (this.mNativeType == 1) {
                buffer.putInt(enumValue);
            } else if (this.mNativeType != 0) {
                throw new AssertionError();
            } else if (enumValue < 0 || enumValue > 255) {
                throw new UnsupportedOperationException(String.format("Enum value %x too large to fit into unsigned byte", Integer.valueOf(enumValue)));
            } else {
                buffer.put((byte) enumValue);
            }
        }

        @Override // android.hardware.camera2.marshal.Marshaler
        public T unmarshal(ByteBuffer buffer) {
            int enumValue;
            switch (this.mNativeType) {
                case 0:
                    enumValue = buffer.get() & BatteryStats.HistoryItem.CMD_NULL;
                    break;
                case 1:
                    enumValue = buffer.getInt();
                    break;
                default:
                    throw new AssertionError("Unexpected native type; impossible since its not supported");
            }
            return (T) MarshalQueryableEnum.getEnumFromValue(this.mClass, enumValue);
        }

        @Override // android.hardware.camera2.marshal.Marshaler
        public int getNativeSize() {
            return MarshalHelpers.getPrimitiveTypeSize(this.mNativeType);
        }
    }

    @Override // android.hardware.camera2.marshal.MarshalQueryable
    public Marshaler<T> createMarshaler(TypeReference<T> managedType, int nativeType) {
        return new MarshalerEnum(managedType, nativeType);
    }

    @Override // android.hardware.camera2.marshal.MarshalQueryable
    public boolean isTypeMappingSupported(TypeReference<T> managedType, int nativeType) {
        if ((nativeType == 1 || nativeType == 0) && (managedType.getType() instanceof Class)) {
            Class<?> typeClass = (Class) managedType.getType();
            if (typeClass.isEnum()) {
                if (VERBOSE) {
                    Log.v(TAG, "possible enum detected for " + typeClass);
                }
                try {
                    typeClass.getDeclaredConstructor(String.class, Integer.TYPE);
                    return true;
                } catch (NoSuchMethodException e) {
                    Log.e(TAG, "Can't marshal class " + typeClass + "; no default constructor");
                } catch (SecurityException e2) {
                    Log.e(TAG, "Can't marshal class " + typeClass + "; not accessible");
                }
            }
        }
        return false;
    }

    public static <T extends Enum<T>> void registerEnumValues(Class<T> enumType, int[] values) {
        if (enumType.getEnumConstants().length != values.length) {
            throw new IllegalArgumentException("Expected values array to be the same size as the enumTypes values " + values.length + " for type " + enumType);
        }
        if (VERBOSE) {
            Log.v(TAG, "Registered enum values for type " + enumType + " values");
        }
        sEnumValues.put(enumType, values);
    }

    /* access modifiers changed from: private */
    public static <T extends Enum<T>> int getEnumValue(T enumValue) {
        int[] values = sEnumValues.get(enumValue.getClass());
        int ordinal = enumValue.ordinal();
        if (values != null) {
            return values[ordinal];
        }
        return ordinal;
    }

    /* access modifiers changed from: private */
    public static <T extends Enum<T>> T getEnumFromValue(Class<T> enumType, int value) {
        int ordinal;
        boolean z = true;
        int[] registeredValues = sEnumValues.get(enumType);
        if (registeredValues != null) {
            ordinal = -1;
            int i = 0;
            while (true) {
                if (i >= registeredValues.length) {
                    break;
                } else if (registeredValues[i] == value) {
                    ordinal = i;
                    break;
                } else {
                    i++;
                }
            }
        } else {
            ordinal = value;
        }
        T[] values = enumType.getEnumConstants();
        if (ordinal >= 0 && ordinal < values.length) {
            return values[ordinal];
        }
        Object[] objArr = new Object[3];
        objArr[0] = Integer.valueOf(value);
        objArr[1] = enumType;
        if (registeredValues == null) {
            z = false;
        }
        objArr[2] = Boolean.valueOf(z);
        throw new IllegalArgumentException(String.format("Argument 'value' (%d) was not a valid enum value for type %s (registered? %b)", objArr));
    }
}

package android.hardware.camera2.marshal.impl;

import android.hardware.camera2.marshal.MarshalHelpers;
import android.hardware.camera2.marshal.MarshalQueryable;
import android.hardware.camera2.marshal.Marshaler;
import android.hardware.camera2.utils.TypeReference;
import android.util.Rational;
import java.nio.ByteBuffer;

public final class MarshalQueryablePrimitive<T> implements MarshalQueryable<T> {

    private class MarshalerPrimitive extends Marshaler<T> {
        private final Class<T> mClass;

        protected MarshalerPrimitive(TypeReference<T> typeReference, int nativeType) {
            super(MarshalQueryablePrimitive.this, typeReference, nativeType);
            this.mClass = MarshalHelpers.wrapClassIfPrimitive(typeReference.getRawType());
        }

        @Override // android.hardware.camera2.marshal.Marshaler
        public T unmarshal(ByteBuffer buffer) {
            return this.mClass.cast(unmarshalObject(buffer));
        }

        @Override // android.hardware.camera2.marshal.Marshaler
        public int calculateMarshalSize(T t) {
            return MarshalHelpers.getPrimitiveTypeSize(this.mNativeType);
        }

        @Override // android.hardware.camera2.marshal.Marshaler
        public void marshal(T value, ByteBuffer buffer) {
            if (value instanceof Integer) {
                MarshalHelpers.checkNativeTypeEquals(1, this.mNativeType);
                marshalPrimitive(value.intValue(), buffer);
            } else if (value instanceof Float) {
                MarshalHelpers.checkNativeTypeEquals(2, this.mNativeType);
                marshalPrimitive(value.floatValue(), buffer);
            } else if (value instanceof Long) {
                MarshalHelpers.checkNativeTypeEquals(3, this.mNativeType);
                marshalPrimitive(value.longValue(), buffer);
            } else if (value instanceof Rational) {
                MarshalHelpers.checkNativeTypeEquals(5, this.mNativeType);
                marshalPrimitive(value, buffer);
            } else if (value instanceof Double) {
                MarshalHelpers.checkNativeTypeEquals(4, this.mNativeType);
                marshalPrimitive(value.doubleValue(), buffer);
            } else if (value instanceof Byte) {
                MarshalHelpers.checkNativeTypeEquals(0, this.mNativeType);
                marshalPrimitive(value.byteValue(), buffer);
            } else {
                throw new UnsupportedOperationException("Can't marshal managed type " + this.mTypeReference);
            }
        }

        private void marshalPrimitive(int value, ByteBuffer buffer) {
            buffer.putInt(value);
        }

        private void marshalPrimitive(float value, ByteBuffer buffer) {
            buffer.putFloat(value);
        }

        private void marshalPrimitive(double value, ByteBuffer buffer) {
            buffer.putDouble(value);
        }

        private void marshalPrimitive(long value, ByteBuffer buffer) {
            buffer.putLong(value);
        }

        private void marshalPrimitive(Rational value, ByteBuffer buffer) {
            buffer.putInt(value.getNumerator());
            buffer.putInt(value.getDenominator());
        }

        private void marshalPrimitive(byte value, ByteBuffer buffer) {
            buffer.put(value);
        }

        private Object unmarshalObject(ByteBuffer buffer) {
            switch (this.mNativeType) {
                case 0:
                    return Byte.valueOf(buffer.get());
                case 1:
                    return Integer.valueOf(buffer.getInt());
                case 2:
                    return Float.valueOf(buffer.getFloat());
                case 3:
                    return Long.valueOf(buffer.getLong());
                case 4:
                    return Double.valueOf(buffer.getDouble());
                case 5:
                    return new Rational(buffer.getInt(), buffer.getInt());
                default:
                    throw new UnsupportedOperationException("Can't unmarshal native type " + this.mNativeType);
            }
        }

        @Override // android.hardware.camera2.marshal.Marshaler
        public int getNativeSize() {
            return MarshalHelpers.getPrimitiveTypeSize(this.mNativeType);
        }
    }

    @Override // android.hardware.camera2.marshal.MarshalQueryable
    public Marshaler<T> createMarshaler(TypeReference<T> managedType, int nativeType) {
        return new MarshalerPrimitive(managedType, nativeType);
    }

    @Override // android.hardware.camera2.marshal.MarshalQueryable
    public boolean isTypeMappingSupported(TypeReference<T> managedType, int nativeType) {
        if (managedType.getType() instanceof Class) {
            Class<?> klass = (Class) managedType.getType();
            if (klass == Byte.TYPE || klass == Byte.class) {
                return nativeType == 0;
            }
            if (klass == Integer.TYPE || klass == Integer.class) {
                return nativeType == 1;
            }
            if (klass == Float.TYPE || klass == Float.class) {
                return nativeType == 2;
            }
            if (klass == Long.TYPE || klass == Long.class) {
                return nativeType == 3;
            }
            if (klass == Double.TYPE || klass == Double.class) {
                return nativeType == 4;
            }
            if (klass == Rational.class) {
                return nativeType == 5;
            }
        }
        return false;
    }
}

package android.hardware.camera2.marshal.impl;

import android.hardware.camera2.marshal.MarshalQueryable;
import android.hardware.camera2.marshal.Marshaler;
import android.hardware.camera2.utils.TypeReference;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import java.nio.ByteBuffer;

public class MarshalQueryableParcelable<T extends Parcelable> implements MarshalQueryable<T> {
    private static final String FIELD_CREATOR = "CREATOR";
    private static final String TAG = "MarshalParcelable";
    private static final boolean VERBOSE = Log.isLoggable(TAG, 2);

    private class MarshalerParcelable extends Marshaler<T> {
        private final Class<T> mClass;
        private final Parcelable.Creator<T> mCreator;

        /* JADX DEBUG: Type inference failed for r2v0. Raw type applied. Possible types: java.lang.Class<? super T extends android.os.Parcelable>, java.lang.Class<T> */
        protected MarshalerParcelable(TypeReference<T> typeReference, int nativeType) {
            super(MarshalQueryableParcelable.this, typeReference, nativeType);
            this.mClass = (Class<? super T>) typeReference.getRawType();
            try {
                try {
                    this.mCreator = (Parcelable.Creator) this.mClass.getDeclaredField(MarshalQueryableParcelable.FIELD_CREATOR).get(null);
                } catch (IllegalAccessException e) {
                    throw new AssertionError(e);
                } catch (IllegalArgumentException e2) {
                    throw new AssertionError(e2);
                }
            } catch (NoSuchFieldException e3) {
                throw new AssertionError(e3);
            }
        }

        /* JADX INFO: finally extract failed */
        public void marshal(T value, ByteBuffer buffer) {
            if (MarshalQueryableParcelable.VERBOSE) {
                Log.v(MarshalQueryableParcelable.TAG, "marshal " + value);
            }
            Parcel parcel = Parcel.obtain();
            try {
                value.writeToParcel(parcel, 0);
                if (parcel.hasFileDescriptors()) {
                    throw new UnsupportedOperationException("Parcelable " + value + " must not have file descriptors");
                }
                byte[] parcelContents = parcel.marshall();
                parcel.recycle();
                if (parcelContents.length == 0) {
                    throw new AssertionError("No data marshaled for " + value);
                }
                buffer.put(parcelContents);
            } catch (Throwable th) {
                parcel.recycle();
                throw th;
            }
        }

        /* JADX WARN: Type inference failed for: r5v10, types: [android.os.Parcelable, T extends android.os.Parcelable] */
        @Override // android.hardware.camera2.marshal.Marshaler
        public T unmarshal(ByteBuffer buffer) {
            if (MarshalQueryableParcelable.VERBOSE) {
                Log.v(MarshalQueryableParcelable.TAG, "unmarshal, buffer remaining " + buffer.remaining());
            }
            buffer.mark();
            Parcel parcel = Parcel.obtain();
            try {
                int maxLength = buffer.remaining();
                byte[] remaining = new byte[maxLength];
                buffer.get(remaining);
                parcel.unmarshall(remaining, 0, maxLength);
                parcel.setDataPosition(0);
                T createFromParcel = this.mCreator.createFromParcel(parcel);
                int actualLength = parcel.dataPosition();
                if (actualLength == 0) {
                    throw new AssertionError("No data marshaled for " + ((Object) createFromParcel));
                }
                buffer.reset();
                buffer.position(buffer.position() + actualLength);
                if (MarshalQueryableParcelable.VERBOSE) {
                    Log.v(MarshalQueryableParcelable.TAG, "unmarshal, parcel length was " + actualLength);
                    Log.v(MarshalQueryableParcelable.TAG, "unmarshal, value is " + ((Object) createFromParcel));
                }
                return this.mClass.cast(createFromParcel);
            } finally {
                parcel.recycle();
            }
        }

        @Override // android.hardware.camera2.marshal.Marshaler
        public int getNativeSize() {
            return NATIVE_SIZE_DYNAMIC;
        }

        public int calculateMarshalSize(T value) {
            Parcel parcel = Parcel.obtain();
            try {
                value.writeToParcel(parcel, 0);
                int length = parcel.marshall().length;
                if (MarshalQueryableParcelable.VERBOSE) {
                    Log.v(MarshalQueryableParcelable.TAG, "calculateMarshalSize, length when parceling " + value + " is " + length);
                }
                return length;
            } finally {
                parcel.recycle();
            }
        }
    }

    @Override // android.hardware.camera2.marshal.MarshalQueryable
    public Marshaler<T> createMarshaler(TypeReference<T> managedType, int nativeType) {
        return new MarshalerParcelable(managedType, nativeType);
    }

    @Override // android.hardware.camera2.marshal.MarshalQueryable
    public boolean isTypeMappingSupported(TypeReference<T> managedType, int nativeType) {
        return Parcelable.class.isAssignableFrom(managedType.getRawType());
    }
}

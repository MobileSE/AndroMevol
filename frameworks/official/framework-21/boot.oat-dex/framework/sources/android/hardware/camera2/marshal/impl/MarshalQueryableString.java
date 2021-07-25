package android.hardware.camera2.marshal.impl;

import android.hardware.camera2.marshal.MarshalQueryable;
import android.hardware.camera2.marshal.Marshaler;
import android.hardware.camera2.utils.TypeReference;
import android.util.Log;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class MarshalQueryableString implements MarshalQueryable<String> {
    private static final byte NUL = 0;
    private static final String TAG = MarshalQueryableString.class.getSimpleName();
    private static final Charset UTF8_CHARSET = Charset.forName("UTF-8");
    private static final boolean VERBOSE = Log.isLoggable(TAG, 2);

    private class MarshalerString extends Marshaler<String> {
        protected MarshalerString(TypeReference<String> typeReference, int nativeType) {
            super(MarshalQueryableString.this, typeReference, nativeType);
        }

        public void marshal(String value, ByteBuffer buffer) {
            buffer.put(value.getBytes(MarshalQueryableString.UTF8_CHARSET));
            buffer.put((byte) 0);
        }

        public int calculateMarshalSize(String value) {
            return value.getBytes(MarshalQueryableString.UTF8_CHARSET).length + 1;
        }

        @Override // android.hardware.camera2.marshal.Marshaler
        public String unmarshal(ByteBuffer buffer) {
            buffer.mark();
            boolean foundNull = false;
            int stringLength = 0;
            while (true) {
                if (!buffer.hasRemaining()) {
                    break;
                } else if (buffer.get() == 0) {
                    foundNull = true;
                    break;
                } else {
                    stringLength++;
                }
            }
            if (MarshalQueryableString.VERBOSE) {
                Log.v(MarshalQueryableString.TAG, "unmarshal - scanned " + stringLength + " characters; found null? " + foundNull);
            }
            if (!foundNull) {
                throw new UnsupportedOperationException("Strings must be null-terminated");
            }
            buffer.reset();
            byte[] strBytes = new byte[(stringLength + 1)];
            buffer.get(strBytes, 0, stringLength + 1);
            return new String(strBytes, 0, stringLength, MarshalQueryableString.UTF8_CHARSET);
        }

        @Override // android.hardware.camera2.marshal.Marshaler
        public int getNativeSize() {
            return NATIVE_SIZE_DYNAMIC;
        }
    }

    @Override // android.hardware.camera2.marshal.MarshalQueryable
    public Marshaler<String> createMarshaler(TypeReference<String> managedType, int nativeType) {
        return new MarshalerString(managedType, nativeType);
    }

    @Override // android.hardware.camera2.marshal.MarshalQueryable
    public boolean isTypeMappingSupported(TypeReference<String> managedType, int nativeType) {
        return nativeType == 0 && String.class.equals(managedType.getType());
    }
}

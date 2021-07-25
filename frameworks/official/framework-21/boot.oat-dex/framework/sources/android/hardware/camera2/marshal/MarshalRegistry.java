package android.hardware.camera2.marshal;

import android.hardware.camera2.utils.TypeReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class MarshalRegistry {
    private static HashMap<MarshalToken<?>, Marshaler<?>> sMarshalerMap = new HashMap<>();
    private static List<MarshalQueryable<?>> sRegisteredMarshalQueryables = new ArrayList();

    public static <T> void registerMarshalQueryable(MarshalQueryable<T> queryable) {
        sRegisteredMarshalQueryables.add(queryable);
    }

    public static <T> Marshaler<T> getMarshaler(TypeReference<T> typeToken, int nativeType) {
        MarshalToken<?> marshalToken = new MarshalToken<>(typeToken, nativeType);
        Marshaler<T> marshaler = (Marshaler<T>) sMarshalerMap.get(marshalToken);
        if (sRegisteredMarshalQueryables.size() == 0) {
            throw new AssertionError("No available query marshalers registered");
        }
        if (marshaler == null) {
            Iterator i$ = sRegisteredMarshalQueryables.iterator();
            while (true) {
                if (!i$.hasNext()) {
                    break;
                }
                MarshalQueryable<?> potentialMarshaler = i$.next();
                if (potentialMarshaler.isTypeMappingSupported(typeToken, nativeType)) {
                    marshaler = (Marshaler<T>) potentialMarshaler.createMarshaler(typeToken, nativeType);
                    break;
                }
            }
            if (marshaler == null) {
                throw new UnsupportedOperationException("Could not find marshaler that matches the requested combination of type reference " + typeToken + " and native type " + MarshalHelpers.toStringNativeType(nativeType));
            }
            sMarshalerMap.put(marshalToken, marshaler);
        }
        return marshaler;
    }

    /* access modifiers changed from: private */
    public static class MarshalToken<T> {
        private final int hash;
        final int nativeType;
        final TypeReference<T> typeReference;

        public MarshalToken(TypeReference<T> typeReference2, int nativeType2) {
            this.typeReference = typeReference2;
            this.nativeType = nativeType2;
            this.hash = typeReference2.hashCode() ^ nativeType2;
        }

        public boolean equals(Object other) {
            if (!(other instanceof MarshalToken)) {
                return false;
            }
            MarshalToken<?> otherToken = (MarshalToken) other;
            if (!this.typeReference.equals(otherToken.typeReference) || this.nativeType != otherToken.nativeType) {
                return false;
            }
            return true;
        }

        public int hashCode() {
            return this.hash;
        }
    }

    private MarshalRegistry() {
        throw new AssertionError();
    }
}

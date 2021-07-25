package android.os;

import android.util.ArrayMap;
import android.util.Log;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class BaseBundle {
    static final int BUNDLE_MAGIC = 1279544898;
    static final boolean DEBUG = false;
    static final Parcel EMPTY_PARCEL = Parcel.obtain();
    private static final String TAG = "Bundle";
    private ClassLoader mClassLoader;
    ArrayMap<String, Object> mMap;
    Parcel mParcelledData;

    BaseBundle(ClassLoader loader, int capacity) {
        this.mMap = null;
        this.mParcelledData = null;
        this.mMap = capacity > 0 ? new ArrayMap<>(capacity) : new ArrayMap<>();
        this.mClassLoader = loader == null ? getClass().getClassLoader() : loader;
    }

    BaseBundle() {
        this((ClassLoader) null, 0);
    }

    BaseBundle(Parcel parcelledData) {
        this.mMap = null;
        this.mParcelledData = null;
        readFromParcelInner(parcelledData);
    }

    BaseBundle(Parcel parcelledData, int length) {
        this.mMap = null;
        this.mParcelledData = null;
        readFromParcelInner(parcelledData, length);
    }

    BaseBundle(ClassLoader loader) {
        this(loader, 0);
    }

    BaseBundle(int capacity) {
        this((ClassLoader) null, capacity);
    }

    BaseBundle(BaseBundle b) {
        this.mMap = null;
        this.mParcelledData = null;
        if (b.mParcelledData == null) {
            this.mParcelledData = null;
        } else if (b.mParcelledData == EMPTY_PARCEL) {
            this.mParcelledData = EMPTY_PARCEL;
        } else {
            this.mParcelledData = Parcel.obtain();
            this.mParcelledData.appendFrom(b.mParcelledData, 0, b.mParcelledData.dataSize());
            this.mParcelledData.setDataPosition(0);
        }
        if (b.mMap != null) {
            this.mMap = new ArrayMap<>(b.mMap);
        } else {
            this.mMap = null;
        }
        this.mClassLoader = b.mClassLoader;
    }

    public String getPairValue() {
        unparcel();
        int size = this.mMap.size();
        if (size > 1) {
            Log.w(TAG, "getPairValue() used on Bundle with multiple pairs.");
        }
        if (size == 0) {
            return null;
        }
        Object o = this.mMap.valueAt(0);
        try {
            return (String) o;
        } catch (ClassCastException e) {
            typeWarning("getPairValue()", o, "String", e);
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public void setClassLoader(ClassLoader loader) {
        this.mClassLoader = loader;
    }

    /* access modifiers changed from: package-private */
    public ClassLoader getClassLoader() {
        return this.mClassLoader;
    }

    /* access modifiers changed from: package-private */
    public synchronized void unparcel() {
        if (this.mParcelledData != null) {
            if (this.mParcelledData == EMPTY_PARCEL) {
                if (this.mMap == null) {
                    this.mMap = new ArrayMap<>(1);
                } else {
                    this.mMap.erase();
                }
                this.mParcelledData = null;
            } else {
                int N = this.mParcelledData.readInt();
                if (N >= 0) {
                    if (this.mMap == null) {
                        this.mMap = new ArrayMap<>(N);
                    } else {
                        this.mMap.erase();
                        this.mMap.ensureCapacity(N);
                    }
                    this.mParcelledData.readArrayMapInternal(this.mMap, N, this.mClassLoader);
                    this.mParcelledData.recycle();
                    this.mParcelledData = null;
                }
            }
        }
    }

    public boolean isParcelled() {
        return this.mParcelledData != null;
    }

    public int size() {
        unparcel();
        return this.mMap.size();
    }

    public boolean isEmpty() {
        unparcel();
        return this.mMap.isEmpty();
    }

    public void clear() {
        unparcel();
        this.mMap.clear();
    }

    public boolean containsKey(String key) {
        unparcel();
        return this.mMap.containsKey(key);
    }

    public Object get(String key) {
        unparcel();
        return this.mMap.get(key);
    }

    public void remove(String key) {
        unparcel();
        this.mMap.remove(key);
    }

    public void putAll(PersistableBundle bundle) {
        unparcel();
        bundle.unparcel();
        this.mMap.putAll(bundle.mMap);
    }

    /* access modifiers changed from: package-private */
    public void putAll(Map map) {
        unparcel();
        this.mMap.putAll(map);
    }

    public Set<String> keySet() {
        unparcel();
        return this.mMap.keySet();
    }

    /* access modifiers changed from: package-private */
    public void putBoolean(String key, boolean value) {
        unparcel();
        this.mMap.put(key, Boolean.valueOf(value));
    }

    /* access modifiers changed from: package-private */
    public void putByte(String key, byte value) {
        unparcel();
        this.mMap.put(key, Byte.valueOf(value));
    }

    /* access modifiers changed from: package-private */
    public void putChar(String key, char value) {
        unparcel();
        this.mMap.put(key, Character.valueOf(value));
    }

    /* access modifiers changed from: package-private */
    public void putShort(String key, short value) {
        unparcel();
        this.mMap.put(key, Short.valueOf(value));
    }

    public void putInt(String key, int value) {
        unparcel();
        this.mMap.put(key, Integer.valueOf(value));
    }

    public void putLong(String key, long value) {
        unparcel();
        this.mMap.put(key, Long.valueOf(value));
    }

    /* access modifiers changed from: package-private */
    public void putFloat(String key, float value) {
        unparcel();
        this.mMap.put(key, Float.valueOf(value));
    }

    public void putDouble(String key, double value) {
        unparcel();
        this.mMap.put(key, Double.valueOf(value));
    }

    public void putString(String key, String value) {
        unparcel();
        this.mMap.put(key, value);
    }

    /* access modifiers changed from: package-private */
    public void putCharSequence(String key, CharSequence value) {
        unparcel();
        this.mMap.put(key, value);
    }

    /* access modifiers changed from: package-private */
    public void putIntegerArrayList(String key, ArrayList<Integer> value) {
        unparcel();
        this.mMap.put(key, value);
    }

    /* access modifiers changed from: package-private */
    public void putStringArrayList(String key, ArrayList<String> value) {
        unparcel();
        this.mMap.put(key, value);
    }

    /* access modifiers changed from: package-private */
    public void putCharSequenceArrayList(String key, ArrayList<CharSequence> value) {
        unparcel();
        this.mMap.put(key, value);
    }

    /* access modifiers changed from: package-private */
    public void putSerializable(String key, Serializable value) {
        unparcel();
        this.mMap.put(key, value);
    }

    /* access modifiers changed from: package-private */
    public void putBooleanArray(String key, boolean[] value) {
        unparcel();
        this.mMap.put(key, value);
    }

    /* access modifiers changed from: package-private */
    public void putByteArray(String key, byte[] value) {
        unparcel();
        this.mMap.put(key, value);
    }

    /* access modifiers changed from: package-private */
    public void putShortArray(String key, short[] value) {
        unparcel();
        this.mMap.put(key, value);
    }

    /* access modifiers changed from: package-private */
    public void putCharArray(String key, char[] value) {
        unparcel();
        this.mMap.put(key, value);
    }

    public void putIntArray(String key, int[] value) {
        unparcel();
        this.mMap.put(key, value);
    }

    public void putLongArray(String key, long[] value) {
        unparcel();
        this.mMap.put(key, value);
    }

    /* access modifiers changed from: package-private */
    public void putFloatArray(String key, float[] value) {
        unparcel();
        this.mMap.put(key, value);
    }

    public void putDoubleArray(String key, double[] value) {
        unparcel();
        this.mMap.put(key, value);
    }

    public void putStringArray(String key, String[] value) {
        unparcel();
        this.mMap.put(key, value);
    }

    /* access modifiers changed from: package-private */
    public void putCharSequenceArray(String key, CharSequence[] value) {
        unparcel();
        this.mMap.put(key, value);
    }

    /* access modifiers changed from: package-private */
    public boolean getBoolean(String key) {
        unparcel();
        return getBoolean(key, false);
    }

    /* access modifiers changed from: package-private */
    public void typeWarning(String key, Object value, String className, Object defaultValue, ClassCastException e) {
        Log.w(TAG, "Key " + key + " expected " + className + " but value was a " + value.getClass().getName() + ".  The default value " + defaultValue + " was returned.");
        Log.w(TAG, "Attempt to cast generated internal exception:", e);
    }

    /* access modifiers changed from: package-private */
    public void typeWarning(String key, Object value, String className, ClassCastException e) {
        typeWarning(key, value, className, "<null>", e);
    }

    /* access modifiers changed from: package-private */
    public boolean getBoolean(String key, boolean defaultValue) {
        unparcel();
        Object o = this.mMap.get(key);
        if (o == null) {
            return defaultValue;
        }
        try {
            return ((Boolean) o).booleanValue();
        } catch (ClassCastException e) {
            typeWarning(key, o, "Boolean", Boolean.valueOf(defaultValue), e);
            return defaultValue;
        }
    }

    /* access modifiers changed from: package-private */
    public byte getByte(String key) {
        unparcel();
        return getByte(key, (byte) 0).byteValue();
    }

    /* access modifiers changed from: package-private */
    public Byte getByte(String key, byte defaultValue) {
        unparcel();
        Object o = this.mMap.get(key);
        if (o == null) {
            return Byte.valueOf(defaultValue);
        }
        try {
            return (Byte) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "Byte", Byte.valueOf(defaultValue), e);
            return Byte.valueOf(defaultValue);
        }
    }

    /* access modifiers changed from: package-private */
    public char getChar(String key) {
        unparcel();
        return getChar(key, 0);
    }

    /* access modifiers changed from: package-private */
    public char getChar(String key, char defaultValue) {
        unparcel();
        Object o = this.mMap.get(key);
        if (o == null) {
            return defaultValue;
        }
        try {
            return ((Character) o).charValue();
        } catch (ClassCastException e) {
            typeWarning(key, o, "Character", Character.valueOf(defaultValue), e);
            return defaultValue;
        }
    }

    /* access modifiers changed from: package-private */
    public short getShort(String key) {
        unparcel();
        return getShort(key, 0);
    }

    /* access modifiers changed from: package-private */
    public short getShort(String key, short defaultValue) {
        unparcel();
        Object o = this.mMap.get(key);
        if (o == null) {
            return defaultValue;
        }
        try {
            return ((Short) o).shortValue();
        } catch (ClassCastException e) {
            typeWarning(key, o, "Short", Short.valueOf(defaultValue), e);
            return defaultValue;
        }
    }

    public int getInt(String key) {
        unparcel();
        return getInt(key, 0);
    }

    public int getInt(String key, int defaultValue) {
        unparcel();
        Object o = this.mMap.get(key);
        if (o == null) {
            return defaultValue;
        }
        try {
            return ((Integer) o).intValue();
        } catch (ClassCastException e) {
            typeWarning(key, o, "Integer", Integer.valueOf(defaultValue), e);
            return defaultValue;
        }
    }

    public long getLong(String key) {
        unparcel();
        return getLong(key, 0);
    }

    public long getLong(String key, long defaultValue) {
        unparcel();
        Object o = this.mMap.get(key);
        if (o == null) {
            return defaultValue;
        }
        try {
            return ((Long) o).longValue();
        } catch (ClassCastException e) {
            typeWarning(key, o, "Long", Long.valueOf(defaultValue), e);
            return defaultValue;
        }
    }

    /* access modifiers changed from: package-private */
    public float getFloat(String key) {
        unparcel();
        return getFloat(key, 0.0f);
    }

    /* access modifiers changed from: package-private */
    public float getFloat(String key, float defaultValue) {
        unparcel();
        Object o = this.mMap.get(key);
        if (o == null) {
            return defaultValue;
        }
        try {
            return ((Float) o).floatValue();
        } catch (ClassCastException e) {
            typeWarning(key, o, "Float", Float.valueOf(defaultValue), e);
            return defaultValue;
        }
    }

    public double getDouble(String key) {
        unparcel();
        return getDouble(key, 0.0d);
    }

    public double getDouble(String key, double defaultValue) {
        unparcel();
        Object o = this.mMap.get(key);
        if (o == null) {
            return defaultValue;
        }
        try {
            return ((Double) o).doubleValue();
        } catch (ClassCastException e) {
            typeWarning(key, o, "Double", Double.valueOf(defaultValue), e);
            return defaultValue;
        }
    }

    public String getString(String key) {
        unparcel();
        Object o = this.mMap.get(key);
        try {
            return (String) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "String", e);
            return null;
        }
    }

    public String getString(String key, String defaultValue) {
        String s = getString(key);
        return s == null ? defaultValue : s;
    }

    /* access modifiers changed from: package-private */
    public CharSequence getCharSequence(String key) {
        unparcel();
        Object o = this.mMap.get(key);
        try {
            return (CharSequence) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "CharSequence", e);
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public CharSequence getCharSequence(String key, CharSequence defaultValue) {
        CharSequence cs = getCharSequence(key);
        return cs == null ? defaultValue : cs;
    }

    /* access modifiers changed from: package-private */
    public Serializable getSerializable(String key) {
        unparcel();
        Object o = this.mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (Serializable) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "Serializable", e);
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public ArrayList<Integer> getIntegerArrayList(String key) {
        unparcel();
        Object o = this.mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (ArrayList) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "ArrayList<Integer>", e);
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public ArrayList<String> getStringArrayList(String key) {
        unparcel();
        Object o = this.mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (ArrayList) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "ArrayList<String>", e);
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public ArrayList<CharSequence> getCharSequenceArrayList(String key) {
        unparcel();
        Object o = this.mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (ArrayList) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "ArrayList<CharSequence>", e);
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean[] getBooleanArray(String key) {
        unparcel();
        Object o = this.mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (boolean[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "byte[]", e);
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public byte[] getByteArray(String key) {
        unparcel();
        Object o = this.mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (byte[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "byte[]", e);
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public short[] getShortArray(String key) {
        unparcel();
        Object o = this.mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (short[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "short[]", e);
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public char[] getCharArray(String key) {
        unparcel();
        Object o = this.mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (char[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "char[]", e);
            return null;
        }
    }

    public int[] getIntArray(String key) {
        unparcel();
        Object o = this.mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (int[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "int[]", e);
            return null;
        }
    }

    public long[] getLongArray(String key) {
        unparcel();
        Object o = this.mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (long[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "long[]", e);
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public float[] getFloatArray(String key) {
        unparcel();
        Object o = this.mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (float[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "float[]", e);
            return null;
        }
    }

    public double[] getDoubleArray(String key) {
        unparcel();
        Object o = this.mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (double[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "double[]", e);
            return null;
        }
    }

    public String[] getStringArray(String key) {
        unparcel();
        Object o = this.mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (String[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "String[]", e);
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public CharSequence[] getCharSequenceArray(String key) {
        unparcel();
        Object o = this.mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (CharSequence[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "CharSequence[]", e);
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public void writeToParcelInner(Parcel parcel, int flags) {
        if (this.mParcelledData != null) {
            if (this.mParcelledData == EMPTY_PARCEL) {
                parcel.writeInt(0);
                return;
            }
            int length = this.mParcelledData.dataSize();
            parcel.writeInt(length);
            parcel.writeInt(BUNDLE_MAGIC);
            parcel.appendFrom(this.mParcelledData, 0, length);
        } else if (this.mMap == null || this.mMap.size() <= 0) {
            parcel.writeInt(0);
        } else {
            int lengthPos = parcel.dataPosition();
            parcel.writeInt(-1);
            parcel.writeInt(BUNDLE_MAGIC);
            int startPos = parcel.dataPosition();
            parcel.writeArrayMapInternal(this.mMap);
            int endPos = parcel.dataPosition();
            parcel.setDataPosition(lengthPos);
            parcel.writeInt(endPos - startPos);
            parcel.setDataPosition(endPos);
        }
    }

    /* access modifiers changed from: package-private */
    public void readFromParcelInner(Parcel parcel) {
        int length = parcel.readInt();
        if (length < 0) {
            throw new RuntimeException("Bad length in parcel: " + length);
        }
        readFromParcelInner(parcel, length);
    }

    private void readFromParcelInner(Parcel parcel, int length) {
        if (length == 0) {
            this.mParcelledData = EMPTY_PARCEL;
            return;
        }
        int magic = parcel.readInt();
        if (magic != BUNDLE_MAGIC) {
            throw new IllegalStateException("Bad magic number for Bundle: 0x" + Integer.toHexString(magic));
        }
        int offset = parcel.dataPosition();
        parcel.setDataPosition(offset + length);
        Parcel p = Parcel.obtain();
        p.setDataPosition(0);
        p.appendFrom(parcel, offset, length);
        p.setDataPosition(0);
        this.mParcelledData = p;
    }
}

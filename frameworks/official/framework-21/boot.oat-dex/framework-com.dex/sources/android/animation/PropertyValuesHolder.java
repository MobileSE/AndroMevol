package android.animation;

import android.animation.Keyframes;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.FloatProperty;
import android.util.IntProperty;
import android.util.Log;
import android.util.Property;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PropertyValuesHolder implements Cloneable {
    private static Class[] DOUBLE_VARIANTS = {Double.TYPE, Double.class, Float.TYPE, Integer.TYPE, Float.class, Integer.class};
    private static Class[] FLOAT_VARIANTS = {Float.TYPE, Float.class, Double.TYPE, Integer.TYPE, Double.class, Integer.class};
    private static Class[] INTEGER_VARIANTS = {Integer.TYPE, Integer.class, Float.TYPE, Double.TYPE, Float.class, Double.class};
    private static final TypeEvaluator sFloatEvaluator = new FloatEvaluator();
    private static final HashMap<Class, HashMap<String, Method>> sGetterPropertyMap = new HashMap<>();
    private static final TypeEvaluator sIntEvaluator = new IntEvaluator();
    private static final HashMap<Class, HashMap<String, Method>> sSetterPropertyMap = new HashMap<>();
    private Object mAnimatedValue;
    private TypeConverter mConverter;
    private TypeEvaluator mEvaluator;
    private Method mGetter;
    Keyframes mKeyframes;
    protected Property mProperty;
    final ReentrantReadWriteLock mPropertyMapLock;
    String mPropertyName;
    Method mSetter;
    final Object[] mTmpValueArray;
    Class mValueType;

    /* access modifiers changed from: private */
    public static native void nCallFloatMethod(Object obj, long j, float f);

    /* access modifiers changed from: private */
    public static native void nCallFourFloatMethod(Object obj, long j, float f, float f2, float f3, float f4);

    /* access modifiers changed from: private */
    public static native void nCallFourIntMethod(Object obj, long j, int i, int i2, int i3, int i4);

    /* access modifiers changed from: private */
    public static native void nCallIntMethod(Object obj, long j, int i);

    /* access modifiers changed from: private */
    public static native void nCallMultipleFloatMethod(Object obj, long j, float[] fArr);

    /* access modifiers changed from: private */
    public static native void nCallMultipleIntMethod(Object obj, long j, int[] iArr);

    /* access modifiers changed from: private */
    public static native void nCallTwoFloatMethod(Object obj, long j, float f, float f2);

    /* access modifiers changed from: private */
    public static native void nCallTwoIntMethod(Object obj, long j, int i, int i2);

    /* access modifiers changed from: private */
    public static native long nGetFloatMethod(Class cls, String str);

    /* access modifiers changed from: private */
    public static native long nGetIntMethod(Class cls, String str);

    /* access modifiers changed from: private */
    public static native long nGetMultipleFloatMethod(Class cls, String str, int i);

    /* access modifiers changed from: private */
    public static native long nGetMultipleIntMethod(Class cls, String str, int i);

    private PropertyValuesHolder(String propertyName) {
        this.mSetter = null;
        this.mGetter = null;
        this.mKeyframes = null;
        this.mPropertyMapLock = new ReentrantReadWriteLock();
        this.mTmpValueArray = new Object[1];
        this.mPropertyName = propertyName;
    }

    private PropertyValuesHolder(Property property) {
        this.mSetter = null;
        this.mGetter = null;
        this.mKeyframes = null;
        this.mPropertyMapLock = new ReentrantReadWriteLock();
        this.mTmpValueArray = new Object[1];
        this.mProperty = property;
        if (property != null) {
            this.mPropertyName = property.getName();
        }
    }

    public static PropertyValuesHolder ofInt(String propertyName, int... values) {
        return new IntPropertyValuesHolder(propertyName, values);
    }

    public static PropertyValuesHolder ofInt(Property<?, Integer> property, int... values) {
        return new IntPropertyValuesHolder(property, values);
    }

    public static PropertyValuesHolder ofMultiInt(String propertyName, int[][] values) {
        if (values.length < 2) {
            throw new IllegalArgumentException("At least 2 values must be supplied");
        }
        int numParameters = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i] == null) {
                throw new IllegalArgumentException("values must not be null");
            }
            int length = values[i].length;
            if (i == 0) {
                numParameters = length;
            } else if (length != numParameters) {
                throw new IllegalArgumentException("Values must all have the same length");
            }
        }
        return new MultiIntValuesHolder(propertyName, (TypeConverter) null, new IntArrayEvaluator(new int[numParameters]), values);
    }

    public static PropertyValuesHolder ofMultiInt(String propertyName, Path path) {
        return new MultiIntValuesHolder(propertyName, new PointFToIntArray(), (TypeEvaluator) null, KeyframeSet.ofPath(path));
    }

    public static <V> PropertyValuesHolder ofMultiInt(String propertyName, TypeConverter<V, int[]> converter, TypeEvaluator<V> evaluator, V... values) {
        return new MultiIntValuesHolder(propertyName, converter, evaluator, values);
    }

    public static <T> PropertyValuesHolder ofMultiInt(String propertyName, TypeConverter<T, int[]> converter, TypeEvaluator<T> evaluator, Keyframe... values) {
        return new MultiIntValuesHolder(propertyName, converter, evaluator, KeyframeSet.ofKeyframe(values));
    }

    public static PropertyValuesHolder ofFloat(String propertyName, float... values) {
        return new FloatPropertyValuesHolder(propertyName, values);
    }

    public static PropertyValuesHolder ofFloat(Property<?, Float> property, float... values) {
        return new FloatPropertyValuesHolder(property, values);
    }

    public static PropertyValuesHolder ofMultiFloat(String propertyName, float[][] values) {
        if (values.length < 2) {
            throw new IllegalArgumentException("At least 2 values must be supplied");
        }
        int numParameters = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i] == null) {
                throw new IllegalArgumentException("values must not be null");
            }
            int length = values[i].length;
            if (i == 0) {
                numParameters = length;
            } else if (length != numParameters) {
                throw new IllegalArgumentException("Values must all have the same length");
            }
        }
        return new MultiFloatValuesHolder(propertyName, (TypeConverter) null, new FloatArrayEvaluator(new float[numParameters]), values);
    }

    public static PropertyValuesHolder ofMultiFloat(String propertyName, Path path) {
        return new MultiFloatValuesHolder(propertyName, new PointFToFloatArray(), (TypeEvaluator) null, KeyframeSet.ofPath(path));
    }

    public static <V> PropertyValuesHolder ofMultiFloat(String propertyName, TypeConverter<V, float[]> converter, TypeEvaluator<V> evaluator, V... values) {
        return new MultiFloatValuesHolder(propertyName, converter, evaluator, values);
    }

    public static <T> PropertyValuesHolder ofMultiFloat(String propertyName, TypeConverter<T, float[]> converter, TypeEvaluator<T> evaluator, Keyframe... values) {
        return new MultiFloatValuesHolder(propertyName, converter, evaluator, KeyframeSet.ofKeyframe(values));
    }

    public static PropertyValuesHolder ofObject(String propertyName, TypeEvaluator evaluator, Object... values) {
        PropertyValuesHolder pvh = new PropertyValuesHolder(propertyName);
        pvh.setObjectValues(values);
        pvh.setEvaluator(evaluator);
        return pvh;
    }

    public static PropertyValuesHolder ofObject(String propertyName, TypeConverter<PointF, ?> converter, Path path) {
        PropertyValuesHolder pvh = new PropertyValuesHolder(propertyName);
        pvh.mKeyframes = KeyframeSet.ofPath(path);
        pvh.mValueType = PointF.class;
        pvh.setConverter(converter);
        return pvh;
    }

    public static <V> PropertyValuesHolder ofObject(Property property, TypeEvaluator<V> evaluator, V... values) {
        PropertyValuesHolder pvh = new PropertyValuesHolder(property);
        pvh.setObjectValues(values);
        pvh.setEvaluator(evaluator);
        return pvh;
    }

    public static <T, V> PropertyValuesHolder ofObject(Property<?, V> property, TypeConverter<T, V> converter, TypeEvaluator<T> evaluator, T... values) {
        PropertyValuesHolder pvh = new PropertyValuesHolder(property);
        pvh.setConverter(converter);
        pvh.setObjectValues(values);
        pvh.setEvaluator(evaluator);
        return pvh;
    }

    public static <V> PropertyValuesHolder ofObject(Property<?, V> property, TypeConverter<PointF, V> converter, Path path) {
        PropertyValuesHolder pvh = new PropertyValuesHolder(property);
        pvh.mKeyframes = KeyframeSet.ofPath(path);
        pvh.mValueType = PointF.class;
        pvh.setConverter(converter);
        return pvh;
    }

    public static PropertyValuesHolder ofKeyframe(String propertyName, Keyframe... values) {
        return ofKeyframes(propertyName, KeyframeSet.ofKeyframe(values));
    }

    public static PropertyValuesHolder ofKeyframe(Property property, Keyframe... values) {
        return ofKeyframes(property, KeyframeSet.ofKeyframe(values));
    }

    static PropertyValuesHolder ofKeyframes(String propertyName, Keyframes keyframes) {
        if (keyframes instanceof Keyframes.IntKeyframes) {
            return new IntPropertyValuesHolder(propertyName, (Keyframes.IntKeyframes) keyframes);
        }
        if (keyframes instanceof Keyframes.FloatKeyframes) {
            return new FloatPropertyValuesHolder(propertyName, (Keyframes.FloatKeyframes) keyframes);
        }
        PropertyValuesHolder pvh = new PropertyValuesHolder(propertyName);
        pvh.mKeyframes = keyframes;
        pvh.mValueType = keyframes.getType();
        return pvh;
    }

    static PropertyValuesHolder ofKeyframes(Property property, Keyframes keyframes) {
        if (keyframes instanceof Keyframes.IntKeyframes) {
            return new IntPropertyValuesHolder(property, (Keyframes.IntKeyframes) keyframes);
        }
        if (keyframes instanceof Keyframes.FloatKeyframes) {
            return new FloatPropertyValuesHolder(property, (Keyframes.FloatKeyframes) keyframes);
        }
        PropertyValuesHolder pvh = new PropertyValuesHolder(property);
        pvh.mKeyframes = keyframes;
        pvh.mValueType = keyframes.getType();
        return pvh;
    }

    public void setIntValues(int... values) {
        this.mValueType = Integer.TYPE;
        this.mKeyframes = KeyframeSet.ofInt(values);
    }

    public void setFloatValues(float... values) {
        this.mValueType = Float.TYPE;
        this.mKeyframes = KeyframeSet.ofFloat(values);
    }

    public void setKeyframes(Keyframe... values) {
        int numKeyframes = values.length;
        Keyframe[] keyframes = new Keyframe[Math.max(numKeyframes, 2)];
        this.mValueType = values[0].getType();
        for (int i = 0; i < numKeyframes; i++) {
            keyframes[i] = values[i];
        }
        this.mKeyframes = new KeyframeSet(keyframes);
    }

    public void setObjectValues(Object... values) {
        this.mValueType = values[0].getClass();
        this.mKeyframes = KeyframeSet.ofObject(values);
        if (this.mEvaluator != null) {
            this.mKeyframes.setEvaluator(this.mEvaluator);
        }
    }

    public void setConverter(TypeConverter converter) {
        this.mConverter = converter;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r13v0, resolved type: java.lang.Class */
    /* JADX WARN: Multi-variable type inference failed */
    private Method getPropertyFunction(Class targetClass, String prefix, Class valueType) {
        Class[] typeVariants;
        Method returnVal = null;
        String methodName = getMethodName(prefix, this.mPropertyName);
        if (valueType == null) {
            try {
                returnVal = targetClass.getMethod(methodName, null);
            } catch (NoSuchMethodException e) {
            }
        } else {
            Class[] args = new Class[1];
            if (valueType.equals(Float.class)) {
                typeVariants = FLOAT_VARIANTS;
            } else if (valueType.equals(Integer.class)) {
                typeVariants = INTEGER_VARIANTS;
            } else {
                typeVariants = valueType.equals(Double.class) ? DOUBLE_VARIANTS : new Class[]{valueType};
            }
            for (Class typeVariant : typeVariants) {
                args[0] = typeVariant;
                try {
                    Method returnVal2 = targetClass.getMethod(methodName, args);
                    if (this.mConverter == null) {
                        this.mValueType = typeVariant;
                    }
                    return returnVal2;
                } catch (NoSuchMethodException e2) {
                }
            }
        }
        if (returnVal == null) {
            Log.w("PropertyValuesHolder", "Method " + getMethodName(prefix, this.mPropertyName) + "() with type " + valueType + " not found on target class " + targetClass);
        }
        return returnVal;
    }

    private Method setupSetterOrGetter(Class targetClass, HashMap<Class, HashMap<String, Method>> propertyMapMap, String prefix, Class valueType) {
        Method setterOrGetter = null;
        try {
            this.mPropertyMapLock.writeLock().lock();
            HashMap<String, Method> propertyMap = propertyMapMap.get(targetClass);
            if (propertyMap != null) {
                setterOrGetter = propertyMap.get(this.mPropertyName);
            }
            if (setterOrGetter == null) {
                setterOrGetter = getPropertyFunction(targetClass, prefix, valueType);
                if (propertyMap == null) {
                    propertyMap = new HashMap<>();
                    propertyMapMap.put(targetClass, propertyMap);
                }
                propertyMap.put(this.mPropertyName, setterOrGetter);
            }
            return setterOrGetter;
        } finally {
            this.mPropertyMapLock.writeLock().unlock();
        }
    }

    /* access modifiers changed from: package-private */
    public void setupSetter(Class targetClass) {
        this.mSetter = setupSetterOrGetter(targetClass, sSetterPropertyMap, "set", this.mConverter == null ? this.mValueType : this.mConverter.getTargetType());
    }

    private void setupGetter(Class targetClass) {
        this.mGetter = setupSetterOrGetter(targetClass, sGetterPropertyMap, "get", null);
    }

    /* access modifiers changed from: package-private */
    public void setupSetterAndGetter(Object target) {
        this.mKeyframes.invalidateCache();
        if (this.mProperty != null) {
            try {
                ArrayList<Keyframe> keyframes = this.mKeyframes.getKeyframes();
                int keyframeCount = keyframes == null ? 0 : keyframes.size();
                Object obj = null;
                for (int i = 0; i < keyframeCount; i++) {
                    Keyframe kf = keyframes.get(i);
                    if (!kf.hasValue() || kf.valueWasSetOnStart()) {
                        if (obj == null) {
                            obj = convertBack(this.mProperty.get(target));
                        }
                        kf.setValue(obj);
                        kf.setValueWasSetOnStart(true);
                    }
                }
                return;
            } catch (ClassCastException e) {
                Log.w("PropertyValuesHolder", "No such property (" + this.mProperty.getName() + ") on target object " + target + ". Trying reflection instead");
                this.mProperty = null;
            }
        }
        Class targetClass = target.getClass();
        if (this.mSetter == null) {
            setupSetter(targetClass);
        }
        ArrayList<Keyframe> keyframes2 = this.mKeyframes.getKeyframes();
        int keyframeCount2 = keyframes2 == null ? 0 : keyframes2.size();
        for (int i2 = 0; i2 < keyframeCount2; i2++) {
            Keyframe kf2 = keyframes2.get(i2);
            if (!kf2.hasValue() || kf2.valueWasSetOnStart()) {
                if (this.mGetter == null) {
                    setupGetter(targetClass);
                    if (this.mGetter == null) {
                        return;
                    }
                }
                try {
                    kf2.setValue(convertBack(this.mGetter.invoke(target, new Object[0])));
                    kf2.setValueWasSetOnStart(true);
                } catch (InvocationTargetException e2) {
                    Log.e("PropertyValuesHolder", e2.toString());
                } catch (IllegalAccessException e3) {
                    Log.e("PropertyValuesHolder", e3.toString());
                }
            }
        }
    }

    private Object convertBack(Object value) {
        if (this.mConverter == null) {
            return value;
        }
        if (this.mConverter instanceof BidirectionalTypeConverter) {
            return ((BidirectionalTypeConverter) this.mConverter).convertBack(value);
        }
        throw new IllegalArgumentException("Converter " + this.mConverter.getClass().getName() + " must be a BidirectionalTypeConverter");
    }

    private void setupValue(Object target, Keyframe kf) {
        if (this.mProperty != null) {
            kf.setValue(convertBack(this.mProperty.get(target)));
        }
        try {
            if (this.mGetter == null) {
                setupGetter(target.getClass());
                if (this.mGetter == null) {
                    return;
                }
            }
            kf.setValue(convertBack(this.mGetter.invoke(target, new Object[0])));
        } catch (InvocationTargetException e) {
            Log.e("PropertyValuesHolder", e.toString());
        } catch (IllegalAccessException e2) {
            Log.e("PropertyValuesHolder", e2.toString());
        }
    }

    /* access modifiers changed from: package-private */
    public void setupStartValue(Object target) {
        ArrayList<Keyframe> keyframes = this.mKeyframes.getKeyframes();
        if (!keyframes.isEmpty()) {
            setupValue(target, keyframes.get(0));
        }
    }

    /* access modifiers changed from: package-private */
    public void setupEndValue(Object target) {
        ArrayList<Keyframe> keyframes = this.mKeyframes.getKeyframes();
        if (!keyframes.isEmpty()) {
            setupValue(target, keyframes.get(keyframes.size() - 1));
        }
    }

    @Override // java.lang.Object
    public PropertyValuesHolder clone() {
        try {
            PropertyValuesHolder newPVH = (PropertyValuesHolder) super.clone();
            newPVH.mPropertyName = this.mPropertyName;
            newPVH.mProperty = this.mProperty;
            newPVH.mKeyframes = this.mKeyframes.clone();
            newPVH.mEvaluator = this.mEvaluator;
            return newPVH;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public void setAnimatedValue(Object target) {
        if (this.mProperty != null) {
            this.mProperty.set(target, getAnimatedValue());
        }
        if (this.mSetter != null) {
            try {
                this.mTmpValueArray[0] = getAnimatedValue();
                this.mSetter.invoke(target, this.mTmpValueArray);
            } catch (InvocationTargetException e) {
                Log.e("PropertyValuesHolder", e.toString());
            } catch (IllegalAccessException e2) {
                Log.e("PropertyValuesHolder", e2.toString());
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void init() {
        if (this.mEvaluator == null) {
            this.mEvaluator = this.mValueType == Integer.class ? sIntEvaluator : this.mValueType == Float.class ? sFloatEvaluator : null;
        }
        if (this.mEvaluator != null) {
            this.mKeyframes.setEvaluator(this.mEvaluator);
        }
    }

    public void setEvaluator(TypeEvaluator evaluator) {
        this.mEvaluator = evaluator;
        this.mKeyframes.setEvaluator(evaluator);
    }

    /* access modifiers changed from: package-private */
    public void calculateValue(float fraction) {
        Object value = this.mKeyframes.getValue(fraction);
        if (this.mConverter != null) {
            value = this.mConverter.convert(value);
        }
        this.mAnimatedValue = value;
    }

    public void setPropertyName(String propertyName) {
        this.mPropertyName = propertyName;
    }

    public void setProperty(Property property) {
        this.mProperty = property;
    }

    public String getPropertyName() {
        return this.mPropertyName;
    }

    /* access modifiers changed from: package-private */
    public Object getAnimatedValue() {
        return this.mAnimatedValue;
    }

    public String toString() {
        return this.mPropertyName + ": " + this.mKeyframes.toString();
    }

    static String getMethodName(String prefix, String propertyName) {
        if (propertyName == null || propertyName.length() == 0) {
            return prefix;
        }
        char firstLetter = Character.toUpperCase(propertyName.charAt(0));
        return prefix + firstLetter + propertyName.substring(1);
    }

    /* access modifiers changed from: package-private */
    public static class IntPropertyValuesHolder extends PropertyValuesHolder {
        private static final HashMap<Class, HashMap<String, Long>> sJNISetterPropertyMap = new HashMap<>();
        int mIntAnimatedValue;
        Keyframes.IntKeyframes mIntKeyframes;
        private IntProperty mIntProperty;
        long mJniSetter;

        public IntPropertyValuesHolder(String propertyName, Keyframes.IntKeyframes keyframes) {
            super(propertyName);
            this.mValueType = Integer.TYPE;
            this.mKeyframes = keyframes;
            this.mIntKeyframes = keyframes;
        }

        public IntPropertyValuesHolder(Property property, Keyframes.IntKeyframes keyframes) {
            super(property);
            this.mValueType = Integer.TYPE;
            this.mKeyframes = keyframes;
            this.mIntKeyframes = keyframes;
            if (property instanceof IntProperty) {
                this.mIntProperty = (IntProperty) this.mProperty;
            }
        }

        public IntPropertyValuesHolder(String propertyName, int... values) {
            super(propertyName);
            setIntValues(values);
        }

        public IntPropertyValuesHolder(Property property, int... values) {
            super(property);
            setIntValues(values);
            if (property instanceof IntProperty) {
                this.mIntProperty = (IntProperty) this.mProperty;
            }
        }

        @Override // android.animation.PropertyValuesHolder
        public void setIntValues(int... values) {
            PropertyValuesHolder.super.setIntValues(values);
            this.mIntKeyframes = (Keyframes.IntKeyframes) this.mKeyframes;
        }

        /* access modifiers changed from: package-private */
        @Override // android.animation.PropertyValuesHolder
        public void calculateValue(float fraction) {
            this.mIntAnimatedValue = this.mIntKeyframes.getIntValue(fraction);
        }

        /* access modifiers changed from: package-private */
        @Override // android.animation.PropertyValuesHolder
        public Object getAnimatedValue() {
            return Integer.valueOf(this.mIntAnimatedValue);
        }

        @Override // android.animation.PropertyValuesHolder, android.animation.PropertyValuesHolder, java.lang.Object
        public IntPropertyValuesHolder clone() {
            IntPropertyValuesHolder newPVH = (IntPropertyValuesHolder) PropertyValuesHolder.super.clone();
            newPVH.mIntKeyframes = (Keyframes.IntKeyframes) newPVH.mKeyframes;
            return newPVH;
        }

        /* access modifiers changed from: package-private */
        @Override // android.animation.PropertyValuesHolder
        public void setAnimatedValue(Object target) {
            if (this.mIntProperty != null) {
                this.mIntProperty.setValue(target, this.mIntAnimatedValue);
            } else if (this.mProperty != null) {
                this.mProperty.set(target, Integer.valueOf(this.mIntAnimatedValue));
            } else if (this.mJniSetter != 0) {
                PropertyValuesHolder.nCallIntMethod(target, this.mJniSetter, this.mIntAnimatedValue);
            } else if (this.mSetter != null) {
                try {
                    this.mTmpValueArray[0] = Integer.valueOf(this.mIntAnimatedValue);
                    this.mSetter.invoke(target, this.mTmpValueArray);
                } catch (InvocationTargetException e) {
                    Log.e("PropertyValuesHolder", e.toString());
                } catch (IllegalAccessException e2) {
                    Log.e("PropertyValuesHolder", e2.toString());
                }
            }
        }

        /* access modifiers changed from: package-private */
        @Override // android.animation.PropertyValuesHolder
        public void setupSetter(Class targetClass) {
            Long jniSetter;
            if (this.mProperty == null) {
                try {
                    this.mPropertyMapLock.writeLock().lock();
                    HashMap<String, Long> propertyMap = sJNISetterPropertyMap.get(targetClass);
                    if (!(propertyMap == null || (jniSetter = propertyMap.get(this.mPropertyName)) == null)) {
                        this.mJniSetter = jniSetter.longValue();
                    }
                    if (this.mJniSetter == 0) {
                        this.mJniSetter = PropertyValuesHolder.nGetIntMethod(targetClass, getMethodName("set", this.mPropertyName));
                        if (this.mJniSetter != 0) {
                            if (propertyMap == null) {
                                propertyMap = new HashMap<>();
                                sJNISetterPropertyMap.put(targetClass, propertyMap);
                            }
                            propertyMap.put(this.mPropertyName, Long.valueOf(this.mJniSetter));
                        }
                    }
                } catch (NoSuchMethodError e) {
                } finally {
                    this.mPropertyMapLock.writeLock().unlock();
                }
                if (this.mJniSetter == 0) {
                    PropertyValuesHolder.super.setupSetter(targetClass);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public static class FloatPropertyValuesHolder extends PropertyValuesHolder {
        private static final HashMap<Class, HashMap<String, Long>> sJNISetterPropertyMap = new HashMap<>();
        float mFloatAnimatedValue;
        Keyframes.FloatKeyframes mFloatKeyframes;
        private FloatProperty mFloatProperty;
        long mJniSetter;

        public FloatPropertyValuesHolder(String propertyName, Keyframes.FloatKeyframes keyframes) {
            super(propertyName);
            this.mValueType = Float.TYPE;
            this.mKeyframes = keyframes;
            this.mFloatKeyframes = keyframes;
        }

        public FloatPropertyValuesHolder(Property property, Keyframes.FloatKeyframes keyframes) {
            super(property);
            this.mValueType = Float.TYPE;
            this.mKeyframes = keyframes;
            this.mFloatKeyframes = keyframes;
            if (property instanceof FloatProperty) {
                this.mFloatProperty = (FloatProperty) this.mProperty;
            }
        }

        public FloatPropertyValuesHolder(String propertyName, float... values) {
            super(propertyName);
            setFloatValues(values);
        }

        public FloatPropertyValuesHolder(Property property, float... values) {
            super(property);
            setFloatValues(values);
            if (property instanceof FloatProperty) {
                this.mFloatProperty = (FloatProperty) this.mProperty;
            }
        }

        @Override // android.animation.PropertyValuesHolder
        public void setFloatValues(float... values) {
            PropertyValuesHolder.super.setFloatValues(values);
            this.mFloatKeyframes = (Keyframes.FloatKeyframes) this.mKeyframes;
        }

        /* access modifiers changed from: package-private */
        @Override // android.animation.PropertyValuesHolder
        public void calculateValue(float fraction) {
            this.mFloatAnimatedValue = this.mFloatKeyframes.getFloatValue(fraction);
        }

        /* access modifiers changed from: package-private */
        @Override // android.animation.PropertyValuesHolder
        public Object getAnimatedValue() {
            return Float.valueOf(this.mFloatAnimatedValue);
        }

        @Override // android.animation.PropertyValuesHolder, android.animation.PropertyValuesHolder, java.lang.Object
        public FloatPropertyValuesHolder clone() {
            FloatPropertyValuesHolder newPVH = (FloatPropertyValuesHolder) PropertyValuesHolder.super.clone();
            newPVH.mFloatKeyframes = (Keyframes.FloatKeyframes) newPVH.mKeyframes;
            return newPVH;
        }

        /* access modifiers changed from: package-private */
        @Override // android.animation.PropertyValuesHolder
        public void setAnimatedValue(Object target) {
            if (this.mFloatProperty != null) {
                this.mFloatProperty.setValue(target, this.mFloatAnimatedValue);
            } else if (this.mProperty != null) {
                this.mProperty.set(target, Float.valueOf(this.mFloatAnimatedValue));
            } else if (this.mJniSetter != 0) {
                PropertyValuesHolder.nCallFloatMethod(target, this.mJniSetter, this.mFloatAnimatedValue);
            } else if (this.mSetter != null) {
                try {
                    this.mTmpValueArray[0] = Float.valueOf(this.mFloatAnimatedValue);
                    this.mSetter.invoke(target, this.mTmpValueArray);
                } catch (InvocationTargetException e) {
                    Log.e("PropertyValuesHolder", e.toString());
                } catch (IllegalAccessException e2) {
                    Log.e("PropertyValuesHolder", e2.toString());
                }
            }
        }

        /* access modifiers changed from: package-private */
        @Override // android.animation.PropertyValuesHolder
        public void setupSetter(Class targetClass) {
            Long jniSetter;
            if (this.mProperty == null) {
                try {
                    this.mPropertyMapLock.writeLock().lock();
                    HashMap<String, Long> propertyMap = sJNISetterPropertyMap.get(targetClass);
                    if (!(propertyMap == null || (jniSetter = propertyMap.get(this.mPropertyName)) == null)) {
                        this.mJniSetter = jniSetter.longValue();
                    }
                    if (this.mJniSetter == 0) {
                        this.mJniSetter = PropertyValuesHolder.nGetFloatMethod(targetClass, getMethodName("set", this.mPropertyName));
                        if (this.mJniSetter != 0) {
                            if (propertyMap == null) {
                                propertyMap = new HashMap<>();
                                sJNISetterPropertyMap.put(targetClass, propertyMap);
                            }
                            propertyMap.put(this.mPropertyName, Long.valueOf(this.mJniSetter));
                        }
                    }
                } catch (NoSuchMethodError e) {
                } finally {
                    this.mPropertyMapLock.writeLock().unlock();
                }
                if (this.mJniSetter == 0) {
                    PropertyValuesHolder.super.setupSetter(targetClass);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public static class MultiFloatValuesHolder extends PropertyValuesHolder {
        private static final HashMap<Class, HashMap<String, Long>> sJNISetterPropertyMap = new HashMap<>();
        private long mJniSetter;

        @Override // android.animation.PropertyValuesHolder, android.animation.PropertyValuesHolder, java.lang.Object
        public /* bridge */ /* synthetic */ Object clone() throws CloneNotSupportedException {
            return PropertyValuesHolder.super.clone();
        }

        public MultiFloatValuesHolder(String propertyName, TypeConverter converter, TypeEvaluator evaluator, Object... values) {
            super(propertyName);
            setConverter(converter);
            setObjectValues(values);
            setEvaluator(evaluator);
        }

        public MultiFloatValuesHolder(String propertyName, TypeConverter converter, TypeEvaluator evaluator, Keyframes keyframes) {
            super(propertyName);
            setConverter(converter);
            this.mKeyframes = keyframes;
            setEvaluator(evaluator);
        }

        /* access modifiers changed from: package-private */
        @Override // android.animation.PropertyValuesHolder
        public void setAnimatedValue(Object target) {
            float[] values = (float[]) getAnimatedValue();
            int numParameters = values.length;
            if (this.mJniSetter != 0) {
                switch (numParameters) {
                    case 1:
                        PropertyValuesHolder.nCallFloatMethod(target, this.mJniSetter, values[0]);
                        return;
                    case 2:
                        PropertyValuesHolder.nCallTwoFloatMethod(target, this.mJniSetter, values[0], values[1]);
                        return;
                    case 3:
                    default:
                        PropertyValuesHolder.nCallMultipleFloatMethod(target, this.mJniSetter, values);
                        return;
                    case 4:
                        PropertyValuesHolder.nCallFourFloatMethod(target, this.mJniSetter, values[0], values[1], values[2], values[3]);
                        return;
                }
            }
        }

        /* access modifiers changed from: package-private */
        @Override // android.animation.PropertyValuesHolder
        public void setupSetterAndGetter(Object target) {
            setupSetter(target.getClass());
        }

        /* access modifiers changed from: package-private */
        @Override // android.animation.PropertyValuesHolder
        public void setupSetter(Class targetClass) {
            Long jniSetterLong;
            if (this.mJniSetter == 0) {
                try {
                    this.mPropertyMapLock.writeLock().lock();
                    HashMap<String, Long> propertyMap = sJNISetterPropertyMap.get(targetClass);
                    if (!(propertyMap == null || (jniSetterLong = propertyMap.get(this.mPropertyName)) == null)) {
                        this.mJniSetter = jniSetterLong.longValue();
                    }
                    if (this.mJniSetter == 0) {
                        String methodName = getMethodName("set", this.mPropertyName);
                        calculateValue(0.0f);
                        int numParams = ((float[]) getAnimatedValue()).length;
                        try {
                            this.mJniSetter = PropertyValuesHolder.nGetMultipleFloatMethod(targetClass, methodName, numParams);
                        } catch (NoSuchMethodError e) {
                            this.mJniSetter = PropertyValuesHolder.nGetMultipleFloatMethod(targetClass, this.mPropertyName, numParams);
                        }
                        if (this.mJniSetter != 0) {
                            if (propertyMap == null) {
                                propertyMap = new HashMap<>();
                                sJNISetterPropertyMap.put(targetClass, propertyMap);
                            }
                            propertyMap.put(this.mPropertyName, Long.valueOf(this.mJniSetter));
                        }
                    }
                } finally {
                    this.mPropertyMapLock.writeLock().unlock();
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public static class MultiIntValuesHolder extends PropertyValuesHolder {
        private static final HashMap<Class, HashMap<String, Long>> sJNISetterPropertyMap = new HashMap<>();
        private long mJniSetter;

        @Override // android.animation.PropertyValuesHolder, android.animation.PropertyValuesHolder, java.lang.Object
        public /* bridge */ /* synthetic */ Object clone() throws CloneNotSupportedException {
            return PropertyValuesHolder.super.clone();
        }

        public MultiIntValuesHolder(String propertyName, TypeConverter converter, TypeEvaluator evaluator, Object... values) {
            super(propertyName);
            setConverter(converter);
            setObjectValues(values);
            setEvaluator(evaluator);
        }

        public MultiIntValuesHolder(String propertyName, TypeConverter converter, TypeEvaluator evaluator, Keyframes keyframes) {
            super(propertyName);
            setConverter(converter);
            this.mKeyframes = keyframes;
            setEvaluator(evaluator);
        }

        /* access modifiers changed from: package-private */
        @Override // android.animation.PropertyValuesHolder
        public void setAnimatedValue(Object target) {
            int[] values = (int[]) getAnimatedValue();
            int numParameters = values.length;
            if (this.mJniSetter != 0) {
                switch (numParameters) {
                    case 1:
                        PropertyValuesHolder.nCallIntMethod(target, this.mJniSetter, values[0]);
                        return;
                    case 2:
                        PropertyValuesHolder.nCallTwoIntMethod(target, this.mJniSetter, values[0], values[1]);
                        return;
                    case 3:
                    default:
                        PropertyValuesHolder.nCallMultipleIntMethod(target, this.mJniSetter, values);
                        return;
                    case 4:
                        PropertyValuesHolder.nCallFourIntMethod(target, this.mJniSetter, values[0], values[1], values[2], values[3]);
                        return;
                }
            }
        }

        /* access modifiers changed from: package-private */
        @Override // android.animation.PropertyValuesHolder
        public void setupSetterAndGetter(Object target) {
            setupSetter(target.getClass());
        }

        /* access modifiers changed from: package-private */
        @Override // android.animation.PropertyValuesHolder
        public void setupSetter(Class targetClass) {
            Long jniSetterLong;
            if (this.mJniSetter == 0) {
                try {
                    this.mPropertyMapLock.writeLock().lock();
                    HashMap<String, Long> propertyMap = sJNISetterPropertyMap.get(targetClass);
                    if (!(propertyMap == null || (jniSetterLong = propertyMap.get(this.mPropertyName)) == null)) {
                        this.mJniSetter = jniSetterLong.longValue();
                    }
                    if (this.mJniSetter == 0) {
                        String methodName = getMethodName("set", this.mPropertyName);
                        calculateValue(0.0f);
                        int numParams = ((int[]) getAnimatedValue()).length;
                        try {
                            this.mJniSetter = PropertyValuesHolder.nGetMultipleIntMethod(targetClass, methodName, numParams);
                        } catch (NoSuchMethodError e) {
                            this.mJniSetter = PropertyValuesHolder.nGetMultipleIntMethod(targetClass, this.mPropertyName, numParams);
                        }
                        if (this.mJniSetter != 0) {
                            if (propertyMap == null) {
                                propertyMap = new HashMap<>();
                                sJNISetterPropertyMap.put(targetClass, propertyMap);
                            }
                            propertyMap.put(this.mPropertyName, Long.valueOf(this.mJniSetter));
                        }
                    }
                } finally {
                    this.mPropertyMapLock.writeLock().unlock();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static class PointFToFloatArray extends TypeConverter<PointF, float[]> {
        private float[] mCoordinates = new float[2];

        public PointFToFloatArray() {
            super(PointF.class, float[].class);
        }

        public float[] convert(PointF value) {
            this.mCoordinates[0] = value.x;
            this.mCoordinates[1] = value.y;
            return this.mCoordinates;
        }
    }

    /* access modifiers changed from: private */
    public static class PointFToIntArray extends TypeConverter<PointF, int[]> {
        private int[] mCoordinates = new int[2];

        public PointFToIntArray() {
            super(PointF.class, int[].class);
        }

        public int[] convert(PointF value) {
            this.mCoordinates[0] = Math.round(value.x);
            this.mCoordinates[1] = Math.round(value.y);
            return this.mCoordinates;
        }
    }
}

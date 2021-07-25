package android.animation;

import java.util.ArrayList;

interface Keyframes extends Cloneable {

    public interface FloatKeyframes extends Keyframes {
        float getFloatValue(float f);
    }

    public interface IntKeyframes extends Keyframes {
        int getIntValue(float f);
    }

    @Override // java.lang.Object
    Keyframes clone();

    ArrayList<Keyframe> getKeyframes();

    Class getType();

    Object getValue(float f);

    void invalidateCache();

    void setEvaluator(TypeEvaluator typeEvaluator);
}

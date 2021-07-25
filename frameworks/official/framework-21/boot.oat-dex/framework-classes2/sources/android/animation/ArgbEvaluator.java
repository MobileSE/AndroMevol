package android.animation;

import com.android.internal.R;

public class ArgbEvaluator implements TypeEvaluator {
    private static final ArgbEvaluator sInstance = new ArgbEvaluator();

    public static ArgbEvaluator getInstance() {
        return sInstance;
    }

    @Override // android.animation.TypeEvaluator
    public Object evaluate(float fraction, Object startValue, Object endValue) {
        int startInt = ((Integer) startValue).intValue();
        int startA = (startInt >> 24) & R.styleable.Theme_actionBarTheme;
        int startR = (startInt >> 16) & R.styleable.Theme_actionBarTheme;
        int startG = (startInt >> 8) & R.styleable.Theme_actionBarTheme;
        int startB = startInt & R.styleable.Theme_actionBarTheme;
        int endInt = ((Integer) endValue).intValue();
        return Integer.valueOf(((((int) (((float) (((endInt >> 24) & R.styleable.Theme_actionBarTheme) - startA)) * fraction)) + startA) << 24) | ((((int) (((float) (((endInt >> 16) & R.styleable.Theme_actionBarTheme) - startR)) * fraction)) + startR) << 16) | ((((int) (((float) (((endInt >> 8) & R.styleable.Theme_actionBarTheme) - startG)) * fraction)) + startG) << 8) | (((int) (((float) ((endInt & R.styleable.Theme_actionBarTheme) - startB)) * fraction)) + startB));
    }
}

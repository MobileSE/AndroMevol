package android.widget;

import android.view.animation.Interpolator;

class Scroller$ViscousFluidInterpolator implements Interpolator {
    private static final float VISCOUS_FLUID_NORMALIZE = (1.0f / viscousFluid(1.0f));
    private static final float VISCOUS_FLUID_OFFSET = (1.0f - (VISCOUS_FLUID_NORMALIZE * viscousFluid(1.0f)));
    private static final float VISCOUS_FLUID_SCALE = 8.0f;

    Scroller$ViscousFluidInterpolator() {
    }

    private static float viscousFluid(float x) {
        float x2 = x * VISCOUS_FLUID_SCALE;
        if (x2 < 1.0f) {
            return x2 - (1.0f - ((float) Math.exp((double) (-x2))));
        }
        return 0.36787945f + ((1.0f - 0.36787945f) * (1.0f - ((float) Math.exp((double) (1.0f - x2)))));
    }

    public float getInterpolation(float input) {
        float interpolated = VISCOUS_FLUID_NORMALIZE * viscousFluid(input);
        if (interpolated > 0.0f) {
            return interpolated + VISCOUS_FLUID_OFFSET;
        }
        return interpolated;
    }
}

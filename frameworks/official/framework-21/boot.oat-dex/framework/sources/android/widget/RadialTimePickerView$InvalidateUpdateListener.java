package android.widget;

import android.animation.ValueAnimator;

class RadialTimePickerView$InvalidateUpdateListener implements ValueAnimator.AnimatorUpdateListener {
    final /* synthetic */ RadialTimePickerView this$0;

    private RadialTimePickerView$InvalidateUpdateListener(RadialTimePickerView radialTimePickerView) {
        this.this$0 = radialTimePickerView;
    }

    public void onAnimationUpdate(ValueAnimator animation) {
        this.this$0.invalidate();
    }
}

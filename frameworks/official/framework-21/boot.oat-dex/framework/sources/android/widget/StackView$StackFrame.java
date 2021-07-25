package android.widget;

import android.animation.ObjectAnimator;
import android.content.Context;
import java.lang.ref.WeakReference;

class StackView$StackFrame extends FrameLayout {
    WeakReference<ObjectAnimator> sliderAnimator;
    WeakReference<ObjectAnimator> transformAnimator;

    public StackView$StackFrame(Context context) {
        super(context);
    }

    /* access modifiers changed from: package-private */
    public void setTransformAnimator(ObjectAnimator oa) {
        this.transformAnimator = new WeakReference<>(oa);
    }

    /* access modifiers changed from: package-private */
    public void setSliderAnimator(ObjectAnimator oa) {
        this.sliderAnimator = new WeakReference<>(oa);
    }

    /* access modifiers changed from: package-private */
    public boolean cancelTransformAnimator() {
        ObjectAnimator oa;
        if (this.transformAnimator == null || (oa = this.transformAnimator.get()) == null) {
            return false;
        }
        oa.cancel();
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean cancelSliderAnimator() {
        ObjectAnimator oa;
        if (this.sliderAnimator == null || (oa = this.sliderAnimator.get()) == null) {
            return false;
        }
        oa.cancel();
        return true;
    }
}

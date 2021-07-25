package android.widget;

import android.view.View;
import android.widget.StackView;

class StackView$StackSlider {
    static final int BEGINNING_OF_STACK_MODE = 1;
    static final int END_OF_STACK_MODE = 2;
    static final int NORMAL_MODE = 0;
    int mMode = 0;
    View mView;
    float mXProgress;
    float mYProgress;
    final /* synthetic */ StackView this$0;

    public StackView$StackSlider(StackView stackView) {
        this.this$0 = stackView;
    }

    public StackView$StackSlider(StackView stackView, StackView$StackSlider copy) {
        this.this$0 = stackView;
        this.mView = copy.mView;
        this.mYProgress = copy.mYProgress;
        this.mXProgress = copy.mXProgress;
        this.mMode = copy.mMode;
    }

    private float cubic(float r) {
        return ((float) (Math.pow((double) ((2.0f * r) - 1.0f), 3.0d) + 1.0d)) / 2.0f;
    }

    private float highlightAlphaInterpolator(float r) {
        if (r < 0.4f) {
            return cubic(r / 0.4f) * 0.85f;
        }
        return cubic(1.0f - ((r - 0.4f) / (1.0f - 0.4f))) * 0.85f;
    }

    private float viewAlphaInterpolator(float r) {
        if (r > 0.3f) {
            return (r - 0.3f) / (1.0f - 0.3f);
        }
        return 0.0f;
    }

    private float rotationInterpolator(float r) {
        if (r < 0.2f) {
            return 0.0f;
        }
        return (r - 0.2f) / (1.0f - 0.2f);
    }

    /* access modifiers changed from: package-private */
    public void setView(View v) {
        this.mView = v;
    }

    public void setYProgress(float r) {
        float r2 = Math.max(0.0f, Math.min(1.0f, r));
        this.mYProgress = r2;
        if (this.mView != null) {
            StackView.LayoutParams viewLp = this.mView.getLayoutParams();
            StackView.LayoutParams highlightLp = StackView.access$000(this.this$0).getLayoutParams();
            int stackDirection = StackView.access$100(this.this$0) == 0 ? 1 : -1;
            if (Float.compare(0.0f, this.mYProgress) == 0 || Float.compare(1.0f, this.mYProgress) == 0) {
                if (this.mView.getLayerType() != 0) {
                    this.mView.setLayerType(0, null);
                }
            } else if (this.mView.getLayerType() == 0) {
                this.mView.setLayerType(2, null);
            }
            switch (this.mMode) {
                case 0:
                    viewLp.setVerticalOffset(Math.round((-r2) * ((float) stackDirection) * ((float) StackView.access$200(this.this$0))));
                    highlightLp.setVerticalOffset(Math.round((-r2) * ((float) stackDirection) * ((float) StackView.access$200(this.this$0))));
                    StackView.access$000(this.this$0).setAlpha(highlightAlphaInterpolator(r2));
                    float alpha = viewAlphaInterpolator(1.0f - r2);
                    if (this.mView.getAlpha() == 0.0f && alpha != 0.0f && this.mView.getVisibility() != 0) {
                        this.mView.setVisibility(0);
                    } else if (alpha == 0.0f && this.mView.getAlpha() != 0.0f && this.mView.getVisibility() == 0) {
                        this.mView.setVisibility(4);
                    }
                    this.mView.setAlpha(alpha);
                    this.mView.setRotationX(((float) stackDirection) * 90.0f * rotationInterpolator(r2));
                    StackView.access$000(this.this$0).setRotationX(((float) stackDirection) * 90.0f * rotationInterpolator(r2));
                    return;
                case 1:
                    float r3 = (1.0f - r2) * 0.2f;
                    viewLp.setVerticalOffset(Math.round(((float) stackDirection) * r3 * ((float) StackView.access$200(this.this$0))));
                    highlightLp.setVerticalOffset(Math.round(((float) stackDirection) * r3 * ((float) StackView.access$200(this.this$0))));
                    StackView.access$000(this.this$0).setAlpha(highlightAlphaInterpolator(r3));
                    return;
                case 2:
                    float r4 = r2 * 0.2f;
                    viewLp.setVerticalOffset(Math.round(((float) (-stackDirection)) * r4 * ((float) StackView.access$200(this.this$0))));
                    highlightLp.setVerticalOffset(Math.round(((float) (-stackDirection)) * r4 * ((float) StackView.access$200(this.this$0))));
                    StackView.access$000(this.this$0).setAlpha(highlightAlphaInterpolator(r4));
                    return;
                default:
                    return;
            }
        }
    }

    public void setXProgress(float r) {
        float r2 = Math.max(-2.0f, Math.min(2.0f, r));
        this.mXProgress = r2;
        if (this.mView != null) {
            float r3 = r2 * 0.2f;
            this.mView.getLayoutParams().setHorizontalOffset(Math.round(((float) StackView.access$200(this.this$0)) * r3));
            StackView.access$000(this.this$0).getLayoutParams().setHorizontalOffset(Math.round(((float) StackView.access$200(this.this$0)) * r3));
        }
    }

    /* access modifiers changed from: package-private */
    public void setMode(int mode) {
        this.mMode = mode;
    }

    /* access modifiers changed from: package-private */
    public float getDurationForNeutralPosition() {
        return getDuration(false, 0.0f);
    }

    /* access modifiers changed from: package-private */
    public float getDurationForOffscreenPosition() {
        return getDuration(true, 0.0f);
    }

    /* access modifiers changed from: package-private */
    public float getDurationForNeutralPosition(float velocity) {
        return getDuration(false, velocity);
    }

    /* access modifiers changed from: package-private */
    public float getDurationForOffscreenPosition(float velocity) {
        return getDuration(true, velocity);
    }

    private float getDuration(boolean invert, float velocity) {
        float f;
        if (this.mView == null) {
            return 0.0f;
        }
        StackView.LayoutParams viewLp = this.mView.getLayoutParams();
        float d = (float) Math.sqrt(Math.pow((double) viewLp.horizontalOffset, 2.0d) + Math.pow((double) viewLp.verticalOffset, 2.0d));
        float maxd = (float) Math.sqrt(Math.pow((double) StackView.access$200(this.this$0), 2.0d) + Math.pow((double) (0.4f * ((float) StackView.access$200(this.this$0))), 2.0d));
        if (velocity == 0.0f) {
            if (invert) {
                f = 1.0f - (d / maxd);
            } else {
                f = d / maxd;
            }
            return f * 400.0f;
        }
        float duration = invert ? d / Math.abs(velocity) : (maxd - d) / Math.abs(velocity);
        if (duration < 50.0f || duration > 400.0f) {
            return getDuration(invert, 0.0f);
        }
        return duration;
    }

    public float getYProgress() {
        return this.mYProgress;
    }

    public float getXProgress() {
        return this.mXProgress;
    }
}

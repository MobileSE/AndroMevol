package android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import com.android.internal.R;

public class ToggleButton extends CompoundButton {
    private static final int NO_ALPHA = 255;
    private float mDisabledAlpha;
    private Drawable mIndicatorDrawable;
    private CharSequence mTextOff;
    private CharSequence mTextOn;

    public ToggleButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ToggleButton, defStyleAttr, defStyleRes);
        this.mTextOn = a.getText(1);
        this.mTextOff = a.getText(2);
        this.mDisabledAlpha = a.getFloat(0, 0.5f);
        syncTextState();
        a.recycle();
    }

    public ToggleButton(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ToggleButton(Context context, AttributeSet attrs) {
        this(context, attrs, 16842827);
    }

    public ToggleButton(Context context) {
        this(context, null);
    }

    public void setChecked(boolean checked) {
        super.setChecked(checked);
        syncTextState();
    }

    private void syncTextState() {
        boolean checked = isChecked();
        if (checked && this.mTextOn != null) {
            setText(this.mTextOn);
        } else if (!checked && this.mTextOff != null) {
            setText(this.mTextOff);
        }
    }

    public CharSequence getTextOn() {
        return this.mTextOn;
    }

    public void setTextOn(CharSequence textOn) {
        this.mTextOn = textOn;
    }

    public CharSequence getTextOff() {
        return this.mTextOff;
    }

    public void setTextOff(CharSequence textOff) {
        this.mTextOff = textOff;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        updateReferenceToIndicatorDrawable(getBackground());
    }

    @Override // android.view.View
    public void setBackgroundDrawable(Drawable d) {
        super.setBackgroundDrawable(d);
        updateReferenceToIndicatorDrawable(d);
    }

    private void updateReferenceToIndicatorDrawable(Drawable backgroundDrawable) {
        if (backgroundDrawable instanceof LayerDrawable) {
            this.mIndicatorDrawable = ((LayerDrawable) backgroundDrawable).findDrawableByLayerId(android.R.id.toggle);
        } else {
            this.mIndicatorDrawable = null;
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void drawableStateChanged() {
        super.drawableStateChanged();
        if (this.mIndicatorDrawable != null) {
            this.mIndicatorDrawable.setAlpha(isEnabled() ? 255 : (int) (255.0f * this.mDisabledAlpha));
        }
    }

    @Override // android.widget.Button, android.view.View
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(ToggleButton.class.getName());
    }

    @Override // android.widget.Button, android.view.View
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(ToggleButton.class.getName());
    }
}

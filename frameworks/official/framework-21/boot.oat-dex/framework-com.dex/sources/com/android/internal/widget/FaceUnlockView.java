package com.android.internal.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

public class FaceUnlockView extends RelativeLayout {
    private static final String TAG = "FaceUnlockView";

    public FaceUnlockView(Context context) {
        this(context, null);
    }

    public FaceUnlockView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private int resolveMeasured(int measureSpec, int desired) {
        int specSize = View.MeasureSpec.getSize(measureSpec);
        switch (View.MeasureSpec.getMode(measureSpec)) {
            case Integer.MIN_VALUE:
                return Math.max(specSize, desired);
            case 0:
                return desired;
            default:
                return specSize;
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.RelativeLayout, android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int chosenSize = Math.min(resolveMeasured(widthMeasureSpec, getSuggestedMinimumWidth()), resolveMeasured(heightMeasureSpec, getSuggestedMinimumHeight()));
        super.onMeasure(View.MeasureSpec.makeMeasureSpec(chosenSize, Integer.MIN_VALUE), View.MeasureSpec.makeMeasureSpec(chosenSize, Integer.MIN_VALUE));
    }
}

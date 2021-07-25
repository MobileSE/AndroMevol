package android.widget;

import android.graphics.drawable.Drawable;

interface Spinner$SpinnerPopup {
    void dismiss();

    Drawable getBackground();

    CharSequence getHintText();

    int getHorizontalOffset();

    int getVerticalOffset();

    boolean isShowing();

    void setAdapter(ListAdapter listAdapter);

    void setBackgroundDrawable(Drawable drawable);

    void setHorizontalOffset(int i);

    void setPromptText(CharSequence charSequence);

    void setVerticalOffset(int i);

    void show(int i, int i2);
}

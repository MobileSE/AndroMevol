package android.media;

import android.os.Parcel;
import android.text.ParcelableSpan;
import android.text.TextPaint;
import android.text.style.CharacterStyle;
import android.text.style.UpdateAppearance;

/* compiled from: ClosedCaptionRenderer */
class MutableBackgroundColorSpan extends CharacterStyle implements UpdateAppearance, ParcelableSpan {
    private int mColor;

    public MutableBackgroundColorSpan(int color) {
        this.mColor = color;
    }

    public MutableBackgroundColorSpan(Parcel src) {
        this.mColor = src.readInt();
    }

    public void setBackgroundColor(int color) {
        this.mColor = color;
    }

    public int getBackgroundColor() {
        return this.mColor;
    }

    @Override // android.text.ParcelableSpan
    public int getSpanTypeId() {
        return 12;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mColor);
    }

    @Override // android.text.style.CharacterStyle
    public void updateDrawState(TextPaint ds) {
        ds.bgColor = this.mColor;
    }
}

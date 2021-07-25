package android.text.style;

import android.graphics.Paint;
import android.os.Parcel;
import android.text.ParcelableSpan;
import android.text.TextPaint;
import java.util.Locale;

public class LocaleSpan extends MetricAffectingSpan implements ParcelableSpan {
    private final Locale mLocale;

    public LocaleSpan(Locale locale) {
        this.mLocale = locale;
    }

    public LocaleSpan(Parcel src) {
        this.mLocale = new Locale(src.readString(), src.readString(), src.readString());
    }

    @Override // android.text.ParcelableSpan
    public int getSpanTypeId() {
        return 23;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mLocale.getLanguage());
        dest.writeString(this.mLocale.getCountry());
        dest.writeString(this.mLocale.getVariant());
    }

    public Locale getLocale() {
        return this.mLocale;
    }

    @Override // android.text.style.CharacterStyle
    public void updateDrawState(TextPaint ds) {
        apply(ds, this.mLocale);
    }

    @Override // android.text.style.MetricAffectingSpan
    public void updateMeasureState(TextPaint paint) {
        apply(paint, this.mLocale);
    }

    private static void apply(Paint paint, Locale locale) {
        paint.setTextLocale(locale);
    }
}

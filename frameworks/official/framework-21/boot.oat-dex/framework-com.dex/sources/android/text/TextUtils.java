package android.text;

import android.content.res.Resources;
import android.media.AudioSystem;
import android.net.ProxyInfo;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.AlignmentSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.BulletSpan;
import android.text.style.CharacterStyle;
import android.text.style.EasyEditSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.LocaleSpan;
import android.text.style.MetricAffectingSpan;
import android.text.style.QuoteSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.ReplacementSpan;
import android.text.style.ScaleXSpan;
import android.text.style.SpellCheckSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.SubscriptSpan;
import android.text.style.SuggestionRangeSpan;
import android.text.style.SuggestionSpan;
import android.text.style.SuperscriptSpan;
import android.text.style.TextAppearanceSpan;
import android.text.style.TtsSpan;
import android.text.style.TypefaceSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.util.Printer;
import android.widget.ExpandableListView;
import com.android.internal.R;
import com.android.internal.content.NativeLibraryHelper;
import com.android.internal.util.ArrayUtils;
import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.Locale;
import java.util.regex.Pattern;
import libcore.icu.ICU;

public class TextUtils {
    public static final int ABSOLUTE_SIZE_SPAN = 16;
    public static final int ALIGNMENT_SPAN = 1;
    public static final int ANNOTATION = 18;
    private static String ARAB_SCRIPT_SUBTAG = "Arab";
    public static final int BACKGROUND_COLOR_SPAN = 12;
    public static final int BULLET_SPAN = 8;
    public static final int CAP_MODE_CHARACTERS = 4096;
    public static final int CAP_MODE_SENTENCES = 16384;
    public static final int CAP_MODE_WORDS = 8192;
    public static final Parcelable.Creator<CharSequence> CHAR_SEQUENCE_CREATOR = new Parcelable.Creator<CharSequence>() {
        /* class android.text.TextUtils.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public CharSequence createFromParcel(Parcel p) {
            int kind = p.readInt();
            String string = p.readString();
            if (string == null) {
                return null;
            }
            if (kind == 1) {
                return string;
            }
            SpannableString sp = new SpannableString(string);
            while (true) {
                int kind2 = p.readInt();
                if (kind2 == 0) {
                    return sp;
                }
                switch (kind2) {
                    case 1:
                        TextUtils.readSpan(p, sp, new AlignmentSpan.Standard(p));
                        break;
                    case 2:
                        TextUtils.readSpan(p, sp, new ForegroundColorSpan(p));
                        break;
                    case 3:
                        TextUtils.readSpan(p, sp, new RelativeSizeSpan(p));
                        break;
                    case 4:
                        TextUtils.readSpan(p, sp, new ScaleXSpan(p));
                        break;
                    case 5:
                        TextUtils.readSpan(p, sp, new StrikethroughSpan(p));
                        break;
                    case 6:
                        TextUtils.readSpan(p, sp, new UnderlineSpan(p));
                        break;
                    case 7:
                        TextUtils.readSpan(p, sp, new StyleSpan(p));
                        break;
                    case 8:
                        TextUtils.readSpan(p, sp, new BulletSpan(p));
                        break;
                    case 9:
                        TextUtils.readSpan(p, sp, new QuoteSpan(p));
                        break;
                    case 10:
                        TextUtils.readSpan(p, sp, new LeadingMarginSpan.Standard(p));
                        break;
                    case 11:
                        TextUtils.readSpan(p, sp, new URLSpan(p));
                        break;
                    case 12:
                        TextUtils.readSpan(p, sp, new BackgroundColorSpan(p));
                        break;
                    case 13:
                        TextUtils.readSpan(p, sp, new TypefaceSpan(p));
                        break;
                    case 14:
                        TextUtils.readSpan(p, sp, new SuperscriptSpan(p));
                        break;
                    case 15:
                        TextUtils.readSpan(p, sp, new SubscriptSpan(p));
                        break;
                    case 16:
                        TextUtils.readSpan(p, sp, new AbsoluteSizeSpan(p));
                        break;
                    case 17:
                        TextUtils.readSpan(p, sp, new TextAppearanceSpan(p));
                        break;
                    case 18:
                        TextUtils.readSpan(p, sp, new Annotation(p));
                        break;
                    case 19:
                        TextUtils.readSpan(p, sp, new SuggestionSpan(p));
                        break;
                    case 20:
                        TextUtils.readSpan(p, sp, new SpellCheckSpan(p));
                        break;
                    case 21:
                        TextUtils.readSpan(p, sp, new SuggestionRangeSpan(p));
                        break;
                    case 22:
                        TextUtils.readSpan(p, sp, new EasyEditSpan(p));
                        break;
                    case 23:
                        TextUtils.readSpan(p, sp, new LocaleSpan(p));
                        break;
                    case 24:
                        TextUtils.readSpan(p, sp, new TtsSpan(p));
                        break;
                    default:
                        throw new RuntimeException("bogus span encoding " + kind2);
                }
            }
        }

        @Override // android.os.Parcelable.Creator
        public CharSequence[] newArray(int size) {
            return new CharSequence[size];
        }
    };
    public static final int EASY_EDIT_SPAN = 22;
    private static String[] EMPTY_STRING_ARRAY = new String[0];
    private static final char FIRST_RIGHT_TO_LEFT = 1424;
    public static final int FIRST_SPAN = 1;
    public static final int FOREGROUND_COLOR_SPAN = 2;
    private static String HEBR_SCRIPT_SUBTAG = "Hebr";
    public static final int LAST_SPAN = 24;
    public static final int LEADING_MARGIN_SPAN = 10;
    public static final int LOCALE_SPAN = 23;
    public static final int QUOTE_SPAN = 9;
    public static final int RELATIVE_SIZE_SPAN = 3;
    public static final int SCALE_X_SPAN = 4;
    public static final int SPELL_CHECK_SPAN = 20;
    public static final int STRIKETHROUGH_SPAN = 5;
    public static final int STYLE_SPAN = 7;
    public static final int SUBSCRIPT_SPAN = 15;
    public static final int SUGGESTION_RANGE_SPAN = 21;
    public static final int SUGGESTION_SPAN = 19;
    public static final int SUPERSCRIPT_SPAN = 14;
    private static final String TAG = "TextUtils";
    public static final int TEXT_APPEARANCE_SPAN = 17;
    public static final int TTS_SPAN = 24;
    public static final int TYPEFACE_SPAN = 13;
    public static final int UNDERLINE_SPAN = 6;
    public static final int URL_SPAN = 11;
    private static final char ZWNBS_CHAR = 65279;
    private static Object sLock = new Object();
    private static char[] sTemp = null;

    public interface EllipsizeCallback {
        void ellipsized(int i, int i2);
    }

    public interface StringSplitter extends Iterable<String> {
        void setString(String str);
    }

    public enum TruncateAt {
        START,
        MIDDLE,
        END,
        MARQUEE,
        END_SMALL
    }

    private TextUtils() {
    }

    public static void getChars(CharSequence s, int start, int end, char[] dest, int destoff) {
        Class<?> cls = s.getClass();
        if (cls == String.class) {
            ((String) s).getChars(start, end, dest, destoff);
        } else if (cls == StringBuffer.class) {
            ((StringBuffer) s).getChars(start, end, dest, destoff);
        } else if (cls == StringBuilder.class) {
            ((StringBuilder) s).getChars(start, end, dest, destoff);
        } else if (s instanceof GetChars) {
            ((GetChars) s).getChars(start, end, dest, destoff);
        } else {
            int i = start;
            int destoff2 = destoff;
            while (i < end) {
                dest[destoff2] = s.charAt(i);
                i++;
                destoff2++;
            }
        }
    }

    public static int indexOf(CharSequence s, char ch) {
        return indexOf(s, ch, 0);
    }

    public static int indexOf(CharSequence s, char ch, int start) {
        if (s.getClass() == String.class) {
            return ((String) s).indexOf(ch, start);
        }
        return indexOf(s, ch, start, s.length());
    }

    public static int indexOf(CharSequence s, char ch, int start, int end) {
        Class<?> cls = s.getClass();
        if ((s instanceof GetChars) || cls == StringBuffer.class || cls == StringBuilder.class || cls == String.class) {
            char[] temp = obtain(500);
            while (start < end) {
                int segend = start + 500;
                if (segend > end) {
                    segend = end;
                }
                getChars(s, start, segend, temp, 0);
                int count = segend - start;
                for (int i = 0; i < count; i++) {
                    if (temp[i] == ch) {
                        recycle(temp);
                        return i + start;
                    }
                }
                start = segend;
            }
            recycle(temp);
            return -1;
        }
        for (int i2 = start; i2 < end; i2++) {
            if (s.charAt(i2) == ch) {
                return i2;
            }
        }
        return -1;
    }

    public static int lastIndexOf(CharSequence s, char ch) {
        return lastIndexOf(s, ch, s.length() - 1);
    }

    public static int lastIndexOf(CharSequence s, char ch, int last) {
        if (s.getClass() == String.class) {
            return ((String) s).lastIndexOf(ch, last);
        }
        return lastIndexOf(s, ch, 0, last);
    }

    public static int lastIndexOf(CharSequence s, char ch, int start, int last) {
        if (last < 0) {
            return -1;
        }
        if (last >= s.length()) {
            last = s.length() - 1;
        }
        int end = last + 1;
        Class<?> cls = s.getClass();
        if ((s instanceof GetChars) || cls == StringBuffer.class || cls == StringBuilder.class || cls == String.class) {
            char[] temp = obtain(500);
            while (start < end) {
                int segstart = end - 500;
                if (segstart < start) {
                    segstart = start;
                }
                getChars(s, segstart, end, temp, 0);
                for (int i = (end - segstart) - 1; i >= 0; i--) {
                    if (temp[i] == ch) {
                        recycle(temp);
                        return i + segstart;
                    }
                }
                end = segstart;
            }
            recycle(temp);
            return -1;
        }
        for (int i2 = end - 1; i2 >= start; i2--) {
            if (s.charAt(i2) == ch) {
                return i2;
            }
        }
        return -1;
    }

    public static int indexOf(CharSequence s, CharSequence needle) {
        return indexOf(s, needle, 0, s.length());
    }

    public static int indexOf(CharSequence s, CharSequence needle, int start) {
        return indexOf(s, needle, start, s.length());
    }

    public static int indexOf(CharSequence s, CharSequence needle, int start, int end) {
        int nlen = needle.length();
        if (nlen == 0) {
            return start;
        }
        char c = needle.charAt(0);
        while (true) {
            int start2 = indexOf(s, c, start);
            if (start2 > end - nlen || start2 < 0) {
                return -1;
            }
            if (regionMatches(s, start2, needle, 0, nlen)) {
                return start2;
            }
            start = start2 + 1;
        }
    }

    public static boolean regionMatches(CharSequence one, int toffset, CharSequence two, int ooffset, int len) {
        int tempLen = len * 2;
        if (tempLen < len) {
            throw new IndexOutOfBoundsException();
        }
        char[] temp = obtain(tempLen);
        getChars(one, toffset, toffset + len, temp, 0);
        getChars(two, ooffset, ooffset + len, temp, len);
        boolean match = true;
        int i = 0;
        while (true) {
            if (i >= len) {
                break;
            } else if (temp[i] != temp[i + len]) {
                match = false;
                break;
            } else {
                i++;
            }
        }
        recycle(temp);
        return match;
    }

    public static String substring(CharSequence source, int start, int end) {
        if (source instanceof String) {
            return ((String) source).substring(start, end);
        }
        if (source instanceof StringBuilder) {
            return ((StringBuilder) source).substring(start, end);
        }
        if (source instanceof StringBuffer) {
            return ((StringBuffer) source).substring(start, end);
        }
        char[] temp = obtain(end - start);
        getChars(source, start, end, temp, 0);
        String str = new String(temp, 0, end - start);
        recycle(temp);
        return str;
    }

    public static CharSequence join(Iterable<CharSequence> list) {
        return join(Resources.getSystem().getText(R.string.list_delimeter), list);
    }

    public static String join(CharSequence delimiter, Object[] tokens) {
        StringBuilder sb = new StringBuilder();
        boolean firstTime = true;
        for (Object token : tokens) {
            if (firstTime) {
                firstTime = false;
            } else {
                sb.append(delimiter);
            }
            sb.append(token);
        }
        return sb.toString();
    }

    public static String join(CharSequence delimiter, Iterable tokens) {
        StringBuilder sb = new StringBuilder();
        boolean firstTime = true;
        for (Object token : tokens) {
            if (firstTime) {
                firstTime = false;
            } else {
                sb.append(delimiter);
            }
            sb.append(token);
        }
        return sb.toString();
    }

    public static String[] split(String text, String expression) {
        if (text.length() == 0) {
            return EMPTY_STRING_ARRAY;
        }
        return text.split(expression, -1);
    }

    public static String[] split(String text, Pattern pattern) {
        if (text.length() == 0) {
            return EMPTY_STRING_ARRAY;
        }
        return pattern.split(text, -1);
    }

    public static class SimpleStringSplitter implements StringSplitter, Iterator<String> {
        private char mDelimiter;
        private int mLength;
        private int mPosition;
        private String mString;

        public SimpleStringSplitter(char delimiter) {
            this.mDelimiter = delimiter;
        }

        @Override // android.text.TextUtils.StringSplitter
        public void setString(String string) {
            this.mString = string;
            this.mPosition = 0;
            this.mLength = this.mString.length();
        }

        @Override // java.lang.Iterable
        public Iterator<String> iterator() {
            return this;
        }

        public boolean hasNext() {
            return this.mPosition < this.mLength;
        }

        @Override // java.util.Iterator
        public String next() {
            int end = this.mString.indexOf(this.mDelimiter, this.mPosition);
            if (end == -1) {
                end = this.mLength;
            }
            String nextString = this.mString.substring(this.mPosition, end);
            this.mPosition = end + 1;
            return nextString;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    public static CharSequence stringOrSpannedString(CharSequence source) {
        if (source == null) {
            return null;
        }
        if (source instanceof SpannedString) {
            return source;
        }
        if (source instanceof Spanned) {
            return new SpannedString(source);
        }
        return source.toString();
    }

    public static boolean isEmpty(CharSequence str) {
        if (str == null || str.length() == 0) {
            return true;
        }
        return false;
    }

    public static int getTrimmedLength(CharSequence s) {
        int len = s.length();
        int start = 0;
        while (start < len && s.charAt(start) <= ' ') {
            start++;
        }
        int end = len;
        while (end > start && s.charAt(end - 1) <= ' ') {
            end--;
        }
        return end - start;
    }

    public static boolean equals(CharSequence a, CharSequence b) {
        int length;
        if (a == b) {
            return true;
        }
        if (a == null || b == null || (length = a.length()) != b.length()) {
            return false;
        }
        if ((a instanceof String) && (b instanceof String)) {
            return a.equals(b);
        }
        for (int i = 0; i < length; i++) {
            if (a.charAt(i) != b.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    public static CharSequence getReverse(CharSequence source, int start, int end) {
        return new Reverser(source, start, end);
    }

    private static class Reverser implements CharSequence, GetChars {
        private int mEnd;
        private CharSequence mSource;
        private int mStart;

        public Reverser(CharSequence source, int start, int end) {
            this.mSource = source;
            this.mStart = start;
            this.mEnd = end;
        }

        public int length() {
            return this.mEnd - this.mStart;
        }

        public CharSequence subSequence(int start, int end) {
            char[] buf = new char[(end - start)];
            getChars(start, end, buf, 0);
            return new String(buf);
        }

        public String toString() {
            return subSequence(0, length()).toString();
        }

        public char charAt(int off) {
            return AndroidCharacter.getMirror(this.mSource.charAt((this.mEnd - 1) - off));
        }

        @Override // android.text.GetChars
        public void getChars(int start, int end, char[] dest, int destoff) {
            TextUtils.getChars(this.mSource, this.mStart + start, this.mStart + end, dest, destoff);
            AndroidCharacter.mirror(dest, 0, end - start);
            int len = end - start;
            int n = (end - start) / 2;
            for (int i = 0; i < n; i++) {
                char tmp = dest[destoff + i];
                dest[destoff + i] = dest[((destoff + len) - i) - 1];
                dest[((destoff + len) - i) - 1] = tmp;
            }
        }
    }

    public static void writeToParcel(CharSequence cs, Parcel p, int parcelableFlags) {
        if (cs instanceof Spanned) {
            p.writeInt(0);
            p.writeString(cs.toString());
            Spanned sp = (Spanned) cs;
            Object[] os = sp.getSpans(0, cs.length(), Object.class);
            for (int i = 0; i < os.length; i++) {
                Object o = os[i];
                Object prop = os[i];
                if (prop instanceof CharacterStyle) {
                    prop = ((CharacterStyle) prop).getUnderlying();
                }
                if (prop instanceof ParcelableSpan) {
                    ParcelableSpan ps = (ParcelableSpan) prop;
                    int spanTypeId = ps.getSpanTypeId();
                    if (spanTypeId < 1 || spanTypeId > 24) {
                        Log.e(TAG, "external class \"" + ps.getClass().getSimpleName() + "\" is attempting to use the frameworks-only ParcelableSpan" + " interface");
                    } else {
                        p.writeInt(spanTypeId);
                        ps.writeToParcel(p, parcelableFlags);
                        writeWhere(p, sp, o);
                    }
                }
            }
            p.writeInt(0);
            return;
        }
        p.writeInt(1);
        if (cs != null) {
            p.writeString(cs.toString());
        } else {
            p.writeString(null);
        }
    }

    private static void writeWhere(Parcel p, Spanned sp, Object o) {
        p.writeInt(sp.getSpanStart(o));
        p.writeInt(sp.getSpanEnd(o));
        p.writeInt(sp.getSpanFlags(o));
    }

    public static void dumpSpans(CharSequence cs, Printer printer, String prefix) {
        if (cs instanceof Spanned) {
            Spanned sp = (Spanned) cs;
            Object[] os = sp.getSpans(0, cs.length(), Object.class);
            for (Object o : os) {
                printer.println(prefix + ((Object) cs.subSequence(sp.getSpanStart(o), sp.getSpanEnd(o))) + ": " + Integer.toHexString(System.identityHashCode(o)) + " " + o.getClass().getCanonicalName() + " (" + sp.getSpanStart(o) + NativeLibraryHelper.CLEAR_ABI_OVERRIDE + sp.getSpanEnd(o) + ") fl=#" + sp.getSpanFlags(o));
            }
            return;
        }
        printer.println(prefix + ((Object) cs) + ": (no spans)");
    }

    public static CharSequence replace(CharSequence template, String[] sources, CharSequence[] destinations) {
        SpannableStringBuilder tb = new SpannableStringBuilder(template);
        for (int i = 0; i < sources.length; i++) {
            int where = indexOf(tb, sources[i]);
            if (where >= 0) {
                tb.setSpan(sources[i], where, sources[i].length() + where, 33);
            }
        }
        for (int i2 = 0; i2 < sources.length; i2++) {
            int start = tb.getSpanStart(sources[i2]);
            int end = tb.getSpanEnd(sources[i2]);
            if (start >= 0) {
                tb.replace(start, end, destinations[i2]);
            }
        }
        return tb;
    }

    public static CharSequence expandTemplate(CharSequence template, CharSequence... values) {
        if (values.length > 9) {
            throw new IllegalArgumentException("max of 9 values are supported");
        }
        SpannableStringBuilder ssb = new SpannableStringBuilder(template);
        int i = 0;
        while (i < ssb.length()) {
            try {
                if (ssb.charAt(i) == '^') {
                    char next = ssb.charAt(i + 1);
                    if (next == '^') {
                        ssb.delete(i + 1, i + 2);
                        i++;
                    } else if (Character.isDigit(next)) {
                        int which = Character.getNumericValue(next) - 1;
                        if (which < 0) {
                            throw new IllegalArgumentException("template requests value ^" + (which + 1));
                        } else if (which >= values.length) {
                            throw new IllegalArgumentException("template requests value ^" + (which + 1) + "; only " + values.length + " provided");
                        } else {
                            ssb.replace(i, i + 2, values[which]);
                            i += values[which].length();
                        }
                    }
                }
                i++;
            } catch (IndexOutOfBoundsException e) {
            }
        }
        return ssb;
    }

    public static int getOffsetBefore(CharSequence text, int offset) {
        int offset2;
        if (offset == 0 || offset == 1) {
            return 0;
        }
        char c = text.charAt(offset - 1);
        if (c < 56320 || c > 57343) {
            offset2 = offset - 1;
        } else {
            char c1 = text.charAt(offset - 2);
            offset2 = (c1 < 55296 || c1 > 56319) ? offset - 1 : offset - 2;
        }
        if (text instanceof Spanned) {
            ReplacementSpan[] spans = (ReplacementSpan[]) ((Spanned) text).getSpans(offset2, offset2, ReplacementSpan.class);
            for (int i = 0; i < spans.length; i++) {
                int start = ((Spanned) text).getSpanStart(spans[i]);
                int end = ((Spanned) text).getSpanEnd(spans[i]);
                if (start < offset2 && end > offset2) {
                    offset2 = start;
                }
            }
        }
        return offset2;
    }

    public static int getOffsetAfter(CharSequence text, int offset) {
        int offset2;
        int len = text.length();
        if (offset == len || offset == len - 1) {
            return len;
        }
        char c = text.charAt(offset);
        if (c < 55296 || c > 56319) {
            offset2 = offset + 1;
        } else {
            char c1 = text.charAt(offset + 1);
            if (c1 < 56320 || c1 > 57343) {
                offset2 = offset + 1;
            } else {
                offset2 = offset + 2;
            }
        }
        if (text instanceof Spanned) {
            ReplacementSpan[] spans = (ReplacementSpan[]) ((Spanned) text).getSpans(offset2, offset2, ReplacementSpan.class);
            for (int i = 0; i < spans.length; i++) {
                int start = ((Spanned) text).getSpanStart(spans[i]);
                int end = ((Spanned) text).getSpanEnd(spans[i]);
                if (start < offset2 && end > offset2) {
                    offset2 = end;
                }
            }
        }
        return offset2;
    }

    /* access modifiers changed from: private */
    public static void readSpan(Parcel p, Spannable sp, Object o) {
        sp.setSpan(o, p.readInt(), p.readInt(), p.readInt());
    }

    public static void copySpansFrom(Spanned source, int start, int end, Class kind, Spannable dest, int destoff) {
        if (kind == null) {
            kind = Object.class;
        }
        Object[] spans = source.getSpans(start, end, kind);
        for (int i = 0; i < spans.length; i++) {
            int st = source.getSpanStart(spans[i]);
            int en = source.getSpanEnd(spans[i]);
            int fl = source.getSpanFlags(spans[i]);
            if (st < start) {
                st = start;
            }
            if (en > end) {
                en = end;
            }
            dest.setSpan(spans[i], (st - start) + destoff, (en - start) + destoff, fl);
        }
    }

    public static CharSequence ellipsize(CharSequence text, TextPaint p, float avail, TruncateAt where) {
        return ellipsize(text, p, avail, where, false, null);
    }

    public static CharSequence ellipsize(CharSequence text, TextPaint paint, float avail, TruncateAt where, boolean preserveLength, EllipsizeCallback callback) {
        return ellipsize(text, paint, avail, where, preserveLength, callback, TextDirectionHeuristics.FIRSTSTRONG_LTR, where == TruncateAt.END_SMALL ? Resources.getSystem().getString(R.string.ellipsis_two_dots) : Resources.getSystem().getString(R.string.ellipsis));
    }

    public static CharSequence ellipsize(CharSequence text, TextPaint paint, float avail, TruncateAt where, boolean preserveLength, EllipsizeCallback callback, TextDirectionHeuristic textDir, String ellipsis) {
        int left;
        int left2;
        int len = text.length();
        MeasuredText mt = MeasuredText.obtain();
        try {
            if (setPara(mt, paint, text, 0, text.length(), textDir) <= avail) {
                if (callback != null) {
                    callback.ellipsized(0, 0);
                }
                return text;
            }
            float avail2 = avail - paint.measureText(ellipsis);
            int right = len;
            if (avail2 < 0.0f) {
                left = 0;
            } else if (where == TruncateAt.START) {
                right = len - mt.breakText(len, false, avail2);
                left = 0;
            } else if (where == TruncateAt.END || where == TruncateAt.END_SMALL) {
                left = mt.breakText(len, true, avail2);
            } else {
                right = len - mt.breakText(len, false, avail2 / 2.0f);
                left = mt.breakText(right, true, avail2 - mt.measure(right, len));
            }
            if (callback != null) {
                callback.ellipsized(left, right);
            }
            char[] buf = mt.mChars;
            Spanned sp = text instanceof Spanned ? (Spanned) text : null;
            int remaining = len - (right - left);
            if (preserveLength) {
                if (remaining > 0) {
                    left2 = left + 1;
                    buf[left] = ellipsis.charAt(0);
                } else {
                    left2 = left;
                }
                for (int i = left2; i < right; i++) {
                    buf[i] = ZWNBS_CHAR;
                }
                String s = new String(buf, 0, len);
                if (sp == null) {
                    MeasuredText.recycle(mt);
                    return s;
                }
                SpannableString ss = new SpannableString(s);
                copySpansFrom(sp, 0, len, Object.class, ss, 0);
                MeasuredText.recycle(mt);
                return ss;
            } else if (remaining == 0) {
                MeasuredText.recycle(mt);
                return ProxyInfo.LOCAL_EXCL_LIST;
            } else if (sp == null) {
                StringBuilder sb = new StringBuilder(ellipsis.length() + remaining);
                sb.append(buf, 0, left);
                sb.append(ellipsis);
                sb.append(buf, right, len - right);
                CharSequence text2 = sb.toString();
                MeasuredText.recycle(mt);
                return text2;
            } else {
                SpannableStringBuilder ssb = new SpannableStringBuilder();
                ssb.append(text, 0, left);
                ssb.append((CharSequence) ellipsis);
                ssb.append(text, right, len);
                MeasuredText.recycle(mt);
                return ssb;
            }
        } finally {
            MeasuredText.recycle(mt);
        }
    }

    public static CharSequence commaEllipsize(CharSequence text, TextPaint p, float avail, String oneMore, String more) {
        return commaEllipsize(text, p, avail, oneMore, more, TextDirectionHeuristics.FIRSTSTRONG_LTR);
    }

    public static CharSequence commaEllipsize(CharSequence text, TextPaint p, float avail, String oneMore, String more, TextDirectionHeuristic textDir) {
        MeasuredText mt = MeasuredText.obtain();
        try {
            int len = text.length();
            if (setPara(mt, p, text, 0, len, textDir) <= avail) {
                return text;
            }
            char[] buf = mt.mChars;
            int commaCount = 0;
            for (int i = 0; i < len; i++) {
                if (buf[i] == ',') {
                    commaCount++;
                }
            }
            int remaining = commaCount + 1;
            int ok = 0;
            String okFormat = ProxyInfo.LOCAL_EXCL_LIST;
            int w = 0;
            int count = 0;
            float[] widths = mt.mWidths;
            MeasuredText tempMt = MeasuredText.obtain();
            for (int i2 = 0; i2 < len; i2++) {
                w = (int) (((float) w) + widths[i2]);
                if (buf[i2] == ',') {
                    count++;
                    remaining--;
                    String format = remaining == 1 ? " " + oneMore : " " + String.format(more, Integer.valueOf(remaining));
                    tempMt.setPara(format, 0, format.length(), textDir);
                    if (((float) w) + tempMt.addStyleRun(p, tempMt.mLen, null) <= avail) {
                        ok = i2 + 1;
                        okFormat = format;
                    }
                }
            }
            MeasuredText.recycle(tempMt);
            SpannableStringBuilder out = new SpannableStringBuilder(okFormat);
            out.insert(0, text, 0, ok);
            MeasuredText.recycle(mt);
            return out;
        } finally {
            MeasuredText.recycle(mt);
        }
    }

    private static float setPara(MeasuredText mt, TextPaint paint, CharSequence text, int start, int end, TextDirectionHeuristic textDir) {
        Spanned sp;
        mt.setPara(text, start, end, textDir);
        if (text instanceof Spanned) {
            sp = (Spanned) text;
        } else {
            sp = null;
        }
        int len = end - start;
        if (sp == null) {
            return mt.addStyleRun(paint, len, null);
        }
        float width = 0.0f;
        int spanStart = 0;
        while (spanStart < len) {
            int spanEnd = sp.nextSpanTransition(spanStart, len, MetricAffectingSpan.class);
            width += mt.addStyleRun(paint, (MetricAffectingSpan[]) removeEmptySpans((MetricAffectingSpan[]) sp.getSpans(spanStart, spanEnd, MetricAffectingSpan.class), sp, MetricAffectingSpan.class), spanEnd - spanStart, null);
            spanStart = spanEnd;
        }
        return width;
    }

    static boolean doesNotNeedBidi(CharSequence s, int start, int end) {
        for (int i = start; i < end; i++) {
            if (s.charAt(i) >= 1424) {
                return false;
            }
        }
        return true;
    }

    static boolean doesNotNeedBidi(char[] text, int start, int len) {
        int i = start;
        int e = i + len;
        while (i < e) {
            if (text[i] >= 1424) {
                return false;
            }
            i++;
        }
        return true;
    }

    static char[] obtain(int len) {
        char[] buf;
        synchronized (sLock) {
            buf = sTemp;
            sTemp = null;
        }
        if (buf == null || buf.length < len) {
            return ArrayUtils.newUnpaddedCharArray(len);
        }
        return buf;
    }

    static void recycle(char[] temp) {
        if (temp.length <= 1000) {
            synchronized (sLock) {
                sTemp = temp;
            }
        }
    }

    public static String htmlEncode(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\"':
                    sb.append("&quot;");
                    break;
                case '&':
                    sb.append("&amp;");
                    break;
                case '\'':
                    sb.append("&#39;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                default:
                    sb.append(c);
                    break;
            }
        }
        return sb.toString();
    }

    public static CharSequence concat(CharSequence... text) {
        if (text.length == 0) {
            return ProxyInfo.LOCAL_EXCL_LIST;
        }
        if (text.length == 1) {
            return text[0];
        }
        boolean spanned = false;
        int i = 0;
        while (true) {
            if (i >= text.length) {
                break;
            } else if (text[i] instanceof Spanned) {
                spanned = true;
                break;
            } else {
                i++;
            }
        }
        StringBuilder sb = new StringBuilder();
        for (CharSequence charSequence : text) {
            sb.append(charSequence);
        }
        if (!spanned) {
            return sb.toString();
        }
        SpannableString ss = new SpannableString(sb);
        int off = 0;
        for (int i2 = 0; i2 < text.length; i2++) {
            int len = text[i2].length();
            if (text[i2] instanceof Spanned) {
                copySpansFrom((Spanned) text[i2], 0, len, Object.class, ss, off);
            }
            off += len;
        }
        return new SpannedString(ss);
    }

    public static boolean isGraphic(CharSequence str) {
        int len = str.length();
        for (int i = 0; i < len; i++) {
            int gc = Character.getType(str.charAt(i));
            if (!(gc == 15 || gc == 16 || gc == 19 || gc == 0 || gc == 13 || gc == 14 || gc == 12)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isGraphic(char c) {
        int gc = Character.getType(c);
        return (gc == 15 || gc == 16 || gc == 19 || gc == 0 || gc == 13 || gc == 14 || gc == 12) ? false : true;
    }

    public static boolean isDigitsOnly(CharSequence str) {
        int len = str.length();
        for (int i = 0; i < len; i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isPrintableAscii(char c) {
        return (' ' <= c && c <= '~') || c == '\r' || c == '\n';
    }

    public static boolean isPrintableAsciiOnly(CharSequence str) {
        int len = str.length();
        for (int i = 0; i < len; i++) {
            if (!isPrintableAscii(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static int getCapsMode(CharSequence cs, int off, int reqModes) {
        if (off < 0) {
            return 0;
        }
        int mode = 0;
        if ((reqModes & 4096) != 0) {
            mode = 0 | 4096;
        }
        if ((reqModes & AudioSystem.DEVICE_OUT_ALL_USB) == 0) {
            return mode;
        }
        int i = off;
        while (i > 0) {
            char c = cs.charAt(i - 1);
            if (!(c == '\"' || c == '\'' || Character.getType(c) == 21)) {
                break;
            }
            i--;
        }
        int j = i;
        while (j > 0) {
            char c2 = cs.charAt(j - 1);
            if (!(c2 == ' ' || c2 == '\t')) {
                break;
            }
            j--;
        }
        if (j == 0 || cs.charAt(j - 1) == '\n') {
            return mode | 8192;
        }
        if ((reqModes & 16384) == 0) {
            if (i != j) {
                return mode | 8192;
            }
            return mode;
        } else if (i == j) {
            return mode;
        } else {
            while (j > 0) {
                char c3 = cs.charAt(j - 1);
                if (!(c3 == '\"' || c3 == '\'' || Character.getType(c3) == 22)) {
                    break;
                }
                j--;
            }
            if (j <= 0) {
                return mode;
            }
            char c4 = cs.charAt(j - 1);
            if (!(c4 == '.' || c4 == '?' || c4 == '!')) {
                return mode;
            }
            if (c4 == '.') {
                for (int k = j - 2; k >= 0; k--) {
                    char c5 = cs.charAt(k);
                    if (c5 == '.') {
                        return mode;
                    }
                    if (!Character.isLetter(c5)) {
                        break;
                    }
                }
            }
            return mode | 16384;
        }
    }

    public static boolean delimitedStringContains(String delimitedString, char delimiter, String item) {
        int expectedDelimiterPos;
        if (isEmpty(delimitedString) || isEmpty(item)) {
            return false;
        }
        int pos = -1;
        int length = delimitedString.length();
        while (true) {
            pos = delimitedString.indexOf(item, pos + 1);
            if (pos == -1) {
                return false;
            }
            if ((pos <= 0 || delimitedString.charAt(pos - 1) == delimiter) && ((expectedDelimiterPos = pos + item.length()) == length || delimitedString.charAt(expectedDelimiterPos) == delimiter)) {
                return true;
            }
        }
    }

    public static <T> T[] removeEmptySpans(T[] spans, Spanned spanned, Class<T> klass) {
        Object[] objArr = null;
        int count = 0;
        for (int i = 0; i < spans.length; i++) {
            T span = spans[i];
            if (spanned.getSpanStart(span) == spanned.getSpanEnd(span)) {
                if (objArr == null) {
                    objArr = (Object[]) Array.newInstance((Class<?>) klass, spans.length - 1);
                    System.arraycopy(spans, 0, objArr, 0, i);
                    count = i;
                }
            } else if (objArr != null) {
                objArr[count] = span;
                count++;
            }
        }
        if (objArr == null) {
            return spans;
        }
        T[] result = (T[]) ((Object[]) Array.newInstance((Class<?>) klass, count));
        System.arraycopy(objArr, 0, result, 0, count);
        return result;
    }

    public static long packRangeInLong(int start, int end) {
        return (((long) start) << 32) | ((long) end);
    }

    public static int unpackRangeStartFromLong(long range) {
        return (int) (range >>> 32);
    }

    public static int unpackRangeEndFromLong(long range) {
        return (int) (ExpandableListView.PACKED_POSITION_VALUE_NULL & range);
    }

    public static int getLayoutDirectionFromLocale(Locale locale) {
        if (locale != null && !locale.equals(Locale.ROOT)) {
            String scriptSubtag = ICU.addLikelySubtags(locale).getScript();
            if (scriptSubtag == null) {
                return getLayoutDirectionFromFirstChar(locale);
            }
            if (scriptSubtag.equalsIgnoreCase(ARAB_SCRIPT_SUBTAG) || scriptSubtag.equalsIgnoreCase(HEBR_SCRIPT_SUBTAG)) {
                return 1;
            }
        }
        return !SystemProperties.getBoolean(Settings.Global.DEVELOPMENT_FORCE_RTL, false) ? 0 : 1;
    }

    private static int getLayoutDirectionFromFirstChar(Locale locale) {
        switch (Character.getDirectionality(locale.getDisplayName(locale).charAt(0))) {
            case 1:
            case 2:
                return 1;
            default:
                return 0;
        }
    }
}

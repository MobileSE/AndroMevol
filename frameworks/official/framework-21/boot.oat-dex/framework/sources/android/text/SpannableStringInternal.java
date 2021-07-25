package android.text;

import com.android.internal.util.ArrayUtils;
import com.android.internal.util.GrowingArrayUtils;
import java.lang.reflect.Array;
import libcore.util.EmptyArray;

/* access modifiers changed from: package-private */
public abstract class SpannableStringInternal {
    private static final int COLUMNS = 3;
    static final Object[] EMPTY = new Object[0];
    private static final int END = 1;
    private static final int FLAGS = 2;
    private static final int START = 0;
    private int mSpanCount;
    private int[] mSpanData;
    private Object[] mSpans;
    private String mText;

    SpannableStringInternal(CharSequence source, int start, int end) {
        if (start == 0 && end == source.length()) {
            this.mText = source.toString();
        } else {
            this.mText = source.toString().substring(start, end);
        }
        this.mSpans = EmptyArray.OBJECT;
        this.mSpanData = EmptyArray.INT;
        if (source instanceof Spanned) {
            Spanned sp = (Spanned) source;
            Object[] spans = sp.getSpans(start, end, Object.class);
            for (int i = 0; i < spans.length; i++) {
                int st = sp.getSpanStart(spans[i]);
                int en = sp.getSpanEnd(spans[i]);
                int fl = sp.getSpanFlags(spans[i]);
                st = st < start ? start : st;
                if (en > end) {
                    en = end;
                }
                setSpan(spans[i], st - start, en - start, fl);
            }
        }
    }

    public final int length() {
        return this.mText.length();
    }

    public final char charAt(int i) {
        return this.mText.charAt(i);
    }

    public final String toString() {
        return this.mText;
    }

    public final void getChars(int start, int end, char[] dest, int off) {
        this.mText.getChars(start, end, dest, off);
    }

    /* access modifiers changed from: package-private */
    public void setSpan(Object what, int start, int end, int flags) {
        char c;
        char c2;
        checkRange("setSpan", start, end);
        if ((flags & 51) == 51) {
            if (start != 0 && start != length() && (c2 = charAt(start - 1)) != '\n') {
                throw new RuntimeException("PARAGRAPH span must start at paragraph boundary (" + start + " follows " + c2 + ")");
            } else if (!(end == 0 || end == length() || (c = charAt(end - 1)) == '\n')) {
                throw new RuntimeException("PARAGRAPH span must end at paragraph boundary (" + end + " follows " + c + ")");
            }
        }
        int count = this.mSpanCount;
        Object[] spans = this.mSpans;
        int[] data = this.mSpanData;
        for (int i = 0; i < count; i++) {
            if (spans[i] == what) {
                int ostart = data[(i * 3) + 0];
                int oend = data[(i * 3) + 1];
                data[(i * 3) + 0] = start;
                data[(i * 3) + 1] = end;
                data[(i * 3) + 2] = flags;
                sendSpanChanged(what, ostart, oend, start, end);
                return;
            }
        }
        if (this.mSpanCount + 1 >= this.mSpans.length) {
            Object[] newtags = ArrayUtils.newUnpaddedObjectArray(GrowingArrayUtils.growSize(this.mSpanCount));
            int[] newdata = new int[(newtags.length * 3)];
            System.arraycopy(this.mSpans, 0, newtags, 0, this.mSpanCount);
            System.arraycopy(this.mSpanData, 0, newdata, 0, this.mSpanCount * 3);
            this.mSpans = newtags;
            this.mSpanData = newdata;
        }
        this.mSpans[this.mSpanCount] = what;
        this.mSpanData[(this.mSpanCount * 3) + 0] = start;
        this.mSpanData[(this.mSpanCount * 3) + 1] = end;
        this.mSpanData[(this.mSpanCount * 3) + 2] = flags;
        this.mSpanCount++;
        if (this instanceof Spannable) {
            sendSpanAdded(what, start, end);
        }
    }

    /* access modifiers changed from: package-private */
    public void removeSpan(Object what) {
        int count = this.mSpanCount;
        Object[] spans = this.mSpans;
        int[] data = this.mSpanData;
        for (int i = count - 1; i >= 0; i--) {
            if (spans[i] == what) {
                int ostart = data[(i * 3) + 0];
                int oend = data[(i * 3) + 1];
                int c = count - (i + 1);
                System.arraycopy(spans, i + 1, spans, i, c);
                System.arraycopy(data, (i + 1) * 3, data, i * 3, c * 3);
                this.mSpanCount--;
                sendSpanRemoved(what, ostart, oend);
                return;
            }
        }
    }

    public int getSpanStart(Object what) {
        int count = this.mSpanCount;
        Object[] spans = this.mSpans;
        int[] data = this.mSpanData;
        for (int i = count - 1; i >= 0; i--) {
            if (spans[i] == what) {
                return data[(i * 3) + 0];
            }
        }
        return -1;
    }

    public int getSpanEnd(Object what) {
        int count = this.mSpanCount;
        Object[] spans = this.mSpans;
        int[] data = this.mSpanData;
        for (int i = count - 1; i >= 0; i--) {
            if (spans[i] == what) {
                return data[(i * 3) + 1];
            }
        }
        return -1;
    }

    public int getSpanFlags(Object what) {
        int count = this.mSpanCount;
        Object[] spans = this.mSpans;
        int[] data = this.mSpanData;
        for (int i = count - 1; i >= 0; i--) {
            if (spans[i] == what) {
                return data[(i * 3) + 2];
            }
        }
        return 0;
    }

    public <T> T[] getSpans(int queryStart, int queryEnd, Class<T> kind) {
        int count;
        int spanCount = this.mSpanCount;
        Object[] spans = this.mSpans;
        int[] data = this.mSpanData;
        Object[] ret = null;
        Object ret1 = null;
        int i = 0;
        int count2 = 0;
        while (i < spanCount) {
            if (kind == null || kind.isInstance(spans[i])) {
                int spanStart = data[(i * 3) + 0];
                int spanEnd = data[(i * 3) + 1];
                if (spanStart > queryEnd) {
                    count = count2;
                } else if (spanEnd < queryStart) {
                    count = count2;
                } else {
                    if (!(spanStart == spanEnd || queryStart == queryEnd)) {
                        if (spanStart == queryEnd) {
                            count = count2;
                        } else if (spanEnd == queryStart) {
                            count = count2;
                        }
                    }
                    if (count2 == 0) {
                        ret1 = spans[i];
                        count = count2 + 1;
                    } else {
                        if (count2 == 1) {
                            ret = (Object[]) Array.newInstance((Class<?>) kind, (spanCount - i) + 1);
                            ret[0] = ret1;
                        }
                        int prio = data[(i * 3) + 2] & Spanned.SPAN_PRIORITY;
                        if (prio != 0) {
                            int j = 0;
                            while (j < count2 && prio <= (getSpanFlags(ret[j]) & Spanned.SPAN_PRIORITY)) {
                                j++;
                            }
                            System.arraycopy(ret, j, ret, j + 1, count2 - j);
                            ret[j] = spans[i];
                            count = count2 + 1;
                        } else {
                            count = count2 + 1;
                            ret[count2] = spans[i];
                        }
                    }
                }
            } else {
                count = count2;
            }
            i++;
            count2 = count;
        }
        if (count2 == 0) {
            return (T[]) ArrayUtils.emptyArray(kind);
        }
        if (count2 == 1) {
            Object[] ret2 = (Object[]) Array.newInstance((Class<?>) kind, 1);
            ret2[0] = ret1;
            return (T[]) ret2;
        } else if (count2 == ret.length) {
            return (T[]) ret;
        } else {
            Object[] nret = (Object[]) Array.newInstance((Class<?>) kind, count2);
            System.arraycopy(ret, 0, nret, 0, count2);
            return (T[]) nret;
        }
    }

    public int nextSpanTransition(int start, int limit, Class kind) {
        int count = this.mSpanCount;
        Object[] spans = this.mSpans;
        int[] data = this.mSpanData;
        if (kind == null) {
            kind = Object.class;
        }
        for (int i = 0; i < count; i++) {
            int st = data[(i * 3) + 0];
            int en = data[(i * 3) + 1];
            if (st > start && st < limit && kind.isInstance(spans[i])) {
                limit = st;
            }
            if (en > start && en < limit && kind.isInstance(spans[i])) {
                limit = en;
            }
        }
        return limit;
    }

    private void sendSpanAdded(Object what, int start, int end) {
        for (SpanWatcher spanWatcher : (SpanWatcher[]) getSpans(start, end, SpanWatcher.class)) {
            spanWatcher.onSpanAdded((Spannable) this, what, start, end);
        }
    }

    private void sendSpanRemoved(Object what, int start, int end) {
        for (SpanWatcher spanWatcher : (SpanWatcher[]) getSpans(start, end, SpanWatcher.class)) {
            spanWatcher.onSpanRemoved((Spannable) this, what, start, end);
        }
    }

    private void sendSpanChanged(Object what, int s, int e, int st, int en) {
        for (SpanWatcher spanWatcher : (SpanWatcher[]) getSpans(Math.min(s, st), Math.max(e, en), SpanWatcher.class)) {
            spanWatcher.onSpanChanged((Spannable) this, what, s, e, st, en);
        }
    }

    private static String region(int start, int end) {
        return "(" + start + " ... " + end + ")";
    }

    private void checkRange(String operation, int start, int end) {
        if (end < start) {
            throw new IndexOutOfBoundsException(operation + " " + region(start, end) + " has end before start");
        }
        int len = length();
        if (start > len || end > len) {
            throw new IndexOutOfBoundsException(operation + " " + region(start, end) + " ends beyond length " + len);
        } else if (start < 0 || end < 0) {
            throw new IndexOutOfBoundsException(operation + " " + region(start, end) + " starts before 0");
        }
    }

    public boolean equals(Object o) {
        if (!(o instanceof Spanned) || !toString().equals(o.toString())) {
            return false;
        }
        Spanned other = (Spanned) o;
        Object[] otherSpans = other.getSpans(0, other.length(), Object.class);
        if (this.mSpanCount != otherSpans.length) {
            return false;
        }
        for (int i = 0; i < this.mSpanCount; i++) {
            Object thisSpan = this.mSpans[i];
            Object otherSpan = otherSpans[i];
            if (thisSpan == this) {
                if (!(other == otherSpan && getSpanStart(thisSpan) == other.getSpanStart(otherSpan) && getSpanEnd(thisSpan) == other.getSpanEnd(otherSpan) && getSpanFlags(thisSpan) == other.getSpanFlags(otherSpan))) {
                    return false;
                }
            } else if (!thisSpan.equals(otherSpan) || getSpanStart(thisSpan) != other.getSpanStart(otherSpan) || getSpanEnd(thisSpan) != other.getSpanEnd(otherSpan) || getSpanFlags(thisSpan) != other.getSpanFlags(otherSpan)) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        int hash = (toString().hashCode() * 31) + this.mSpanCount;
        for (int i = 0; i < this.mSpanCount; i++) {
            Object span = this.mSpans[i];
            if (span != this) {
                hash = (hash * 31) + span.hashCode();
            }
            hash = (((((hash * 31) + getSpanStart(span)) * 31) + getSpanEnd(span)) * 31) + getSpanFlags(span);
        }
        return hash;
    }
}

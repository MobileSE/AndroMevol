package android.text;

import java.lang.reflect.Array;

public class SpanSet<E> {
    private final Class<? extends E> classType;
    int numberOfSpans = 0;
    int[] spanEnds;
    int[] spanFlags;
    int[] spanStarts;
    E[] spans;

    SpanSet(Class<? extends E> type) {
        this.classType = type;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r7v2, resolved type: E[] */
    /* JADX WARN: Multi-variable type inference failed */
    public void init(Spanned spanned, int start, int limit) {
        Object[] spans2 = spanned.getSpans(start, limit, this.classType);
        int length = spans2.length;
        if (length > 0 && (this.spans == null || this.spans.length < length)) {
            this.spans = (E[]) ((Object[]) Array.newInstance(this.classType, length));
            this.spanStarts = new int[length];
            this.spanEnds = new int[length];
            this.spanFlags = new int[length];
        }
        this.numberOfSpans = 0;
        for (Object obj : spans2) {
            int spanStart = spanned.getSpanStart(obj);
            int spanEnd = spanned.getSpanEnd(obj);
            if (spanStart != spanEnd) {
                int spanFlag = spanned.getSpanFlags(obj);
                this.spans[this.numberOfSpans] = obj;
                this.spanStarts[this.numberOfSpans] = spanStart;
                this.spanEnds[this.numberOfSpans] = spanEnd;
                this.spanFlags[this.numberOfSpans] = spanFlag;
                this.numberOfSpans++;
            }
        }
    }

    public boolean hasSpansIntersecting(int start, int end) {
        for (int i = 0; i < this.numberOfSpans; i++) {
            if (this.spanStarts[i] < end && this.spanEnds[i] > start) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public int getNextTransition(int start, int limit) {
        for (int i = 0; i < this.numberOfSpans; i++) {
            int spanStart = this.spanStarts[i];
            int spanEnd = this.spanEnds[i];
            if (spanStart > start && spanStart < limit) {
                limit = spanStart;
            }
            if (spanEnd > start && spanEnd < limit) {
                limit = spanEnd;
            }
        }
        return limit;
    }

    public void recycle() {
        for (int i = 0; i < this.numberOfSpans; i++) {
            this.spans[i] = null;
        }
    }
}

package android.text;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.Layout;
import android.text.style.CharacterStyle;
import android.text.style.MetricAffectingSpan;
import android.text.style.ReplacementSpan;
import com.android.internal.util.ArrayUtils;

/* access modifiers changed from: package-private */
public class TextLine {
    private static final boolean DEBUG = false;
    private static final int TAB_INCREMENT = 20;
    private static final TextLine[] sCached = new TextLine[3];
    private final SpanSet<CharacterStyle> mCharacterStyleSpanSet = new SpanSet<>(CharacterStyle.class);
    private char[] mChars;
    private boolean mCharsValid;
    private int mDir;
    private Layout.Directions mDirections;
    private boolean mHasTabs;
    private int mLen;
    private final SpanSet<MetricAffectingSpan> mMetricAffectingSpanSpanSet = new SpanSet<>(MetricAffectingSpan.class);
    private TextPaint mPaint;
    private final SpanSet<ReplacementSpan> mReplacementSpanSpanSet = new SpanSet<>(ReplacementSpan.class);
    private Spanned mSpanned;
    private int mStart;
    private Layout.TabStops mTabs;
    private CharSequence mText;
    private final TextPaint mWorkPaint = new TextPaint();

    TextLine() {
    }

    static TextLine obtain() {
        synchronized (sCached) {
            int i = sCached.length;
            do {
                i--;
                if (i < 0) {
                    return new TextLine();
                }
            } while (sCached[i] == null);
            TextLine tl = sCached[i];
            sCached[i] = null;
            return tl;
        }
    }

    static TextLine recycle(TextLine tl) {
        tl.mText = null;
        tl.mPaint = null;
        tl.mDirections = null;
        tl.mMetricAffectingSpanSpanSet.recycle();
        tl.mCharacterStyleSpanSet.recycle();
        tl.mReplacementSpanSpanSet.recycle();
        synchronized (sCached) {
            int i = 0;
            while (true) {
                if (i >= sCached.length) {
                    break;
                } else if (sCached[i] == null) {
                    sCached[i] = tl;
                    break;
                } else {
                    i++;
                }
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void set(TextPaint paint, CharSequence text, int start, int limit, int dir, Layout.Directions directions, boolean hasTabs, Layout.TabStops tabStops) {
        this.mPaint = paint;
        this.mText = text;
        this.mStart = start;
        this.mLen = limit - start;
        this.mDir = dir;
        this.mDirections = directions;
        if (this.mDirections == null) {
            throw new IllegalArgumentException("Directions cannot be null");
        }
        this.mHasTabs = hasTabs;
        this.mSpanned = null;
        boolean hasReplacement = false;
        if (text instanceof Spanned) {
            this.mSpanned = (Spanned) text;
            this.mReplacementSpanSpanSet.init(this.mSpanned, start, limit);
            hasReplacement = this.mReplacementSpanSpanSet.numberOfSpans > 0;
        }
        this.mCharsValid = hasReplacement || hasTabs || directions != Layout.DIRS_ALL_LEFT_TO_RIGHT;
        if (this.mCharsValid) {
            if (this.mChars == null || this.mChars.length < this.mLen) {
                this.mChars = ArrayUtils.newUnpaddedCharArray(this.mLen);
            }
            TextUtils.getChars(text, start, limit, this.mChars, 0);
            if (hasReplacement) {
                char[] chars = this.mChars;
                int i = start;
                while (i < limit) {
                    int inext = this.mReplacementSpanSpanSet.getNextTransition(i, limit);
                    if (this.mReplacementSpanSpanSet.hasSpansIntersecting(i, inext)) {
                        chars[i - start] = 65532;
                        int e = inext - start;
                        for (int j = (i - start) + 1; j < e; j++) {
                            chars[j] = 65279;
                        }
                    }
                    i = inext;
                }
            }
        }
        this.mTabs = tabStops;
    }

    /* access modifiers changed from: package-private */
    public void draw(Canvas c, float x, int top, int y, int bottom) {
        int j;
        if (!this.mHasTabs) {
            if (this.mDirections == Layout.DIRS_ALL_LEFT_TO_RIGHT) {
                drawRun(c, 0, this.mLen, false, x, top, y, bottom, false);
                return;
            } else if (this.mDirections == Layout.DIRS_ALL_RIGHT_TO_LEFT) {
                drawRun(c, 0, this.mLen, true, x, top, y, bottom, false);
                return;
            }
        }
        float h = 0.0f;
        int[] runs = this.mDirections.mDirections;
        RectF emojiRect = null;
        int lastRunIndex = runs.length - 2;
        int i = 0;
        while (i < runs.length) {
            int runStart = runs[i];
            int runLimit = runStart + (runs[i + 1] & 67108863);
            if (runLimit > this.mLen) {
                runLimit = this.mLen;
            }
            boolean runIsRtl = (runs[i + 1] & 67108864) != 0;
            int segstart = runStart;
            if (this.mHasTabs) {
                j = runStart;
            } else {
                j = runLimit;
            }
            while (j <= runLimit) {
                int codept = 0;
                Bitmap bm = null;
                if (this.mHasTabs && j < runLimit) {
                    char c2 = this.mChars[j];
                    codept = c2;
                    if (c2 >= 55296) {
                        codept = c2;
                        if (c2 < 56320) {
                            codept = c2;
                            if (j + 1 < runLimit) {
                                int codept2 = Character.codePointAt(this.mChars, j);
                                if (codept2 < Layout.MIN_EMOJI || codept2 > Layout.MAX_EMOJI) {
                                    codept = codept2;
                                    if (codept2 > 65535) {
                                        j++;
                                        j++;
                                    }
                                } else {
                                    bm = Layout.EMOJI_FACTORY.getBitmapFromAndroidPua(codept2);
                                    codept = codept2;
                                }
                            }
                        }
                    }
                }
                if (j == runLimit || codept == 9 || bm != null) {
                    h += drawRun(c, segstart, j, runIsRtl, x + h, top, y, bottom, (i == lastRunIndex && j == this.mLen) ? false : true);
                    if (codept == 9) {
                        h = ((float) this.mDir) * nextTab(((float) this.mDir) * h);
                    } else if (bm != null) {
                        float bmAscent = ascent(j);
                        float width = ((float) bm.getWidth()) * ((-bmAscent) / ((float) bm.getHeight()));
                        if (emojiRect == null) {
                            emojiRect = new RectF();
                        }
                        emojiRect.set(x + h, ((float) y) + bmAscent, x + h + width, (float) y);
                        c.drawBitmap(bm, (Rect) null, emojiRect, this.mPaint);
                        h += width;
                        j++;
                    }
                    segstart = j + 1;
                    j++;
                } else {
                    j++;
                }
            }
            i += 2;
        }
    }

    /* access modifiers changed from: package-private */
    public float metrics(Paint.FontMetricsInt fmi) {
        return measure(this.mLen, false, fmi);
    }

    /* access modifiers changed from: package-private */
    public float measure(int offset, boolean trailing, Paint.FontMetricsInt fmi) {
        int j;
        int target = trailing ? offset - 1 : offset;
        if (target < 0) {
            return 0.0f;
        }
        float h = 0.0f;
        if (!this.mHasTabs) {
            if (this.mDirections == Layout.DIRS_ALL_LEFT_TO_RIGHT) {
                return measureRun(0, offset, this.mLen, false, fmi);
            }
            if (this.mDirections == Layout.DIRS_ALL_RIGHT_TO_LEFT) {
                return measureRun(0, offset, this.mLen, true, fmi);
            }
        }
        char[] chars = this.mChars;
        int[] runs = this.mDirections.mDirections;
        for (int i = 0; i < runs.length; i += 2) {
            int runStart = runs[i];
            int runLimit = runStart + (runs[i + 1] & 67108863);
            if (runLimit > this.mLen) {
                runLimit = this.mLen;
            }
            boolean runIsRtl = (runs[i + 1] & 67108864) != 0;
            int segstart = runStart;
            if (this.mHasTabs) {
                j = runStart;
            } else {
                j = runLimit;
            }
            while (j <= runLimit) {
                int codept = 0;
                Bitmap bm = null;
                if (this.mHasTabs && j < runLimit) {
                    char c = chars[j];
                    codept = c;
                    if (c >= 55296) {
                        codept = c;
                        if (c < 56320) {
                            codept = c;
                            if (j + 1 < runLimit) {
                                int codept2 = Character.codePointAt(chars, j);
                                if (codept2 < Layout.MIN_EMOJI || codept2 > Layout.MAX_EMOJI) {
                                    codept = codept2;
                                    if (codept2 > 65535) {
                                        j++;
                                        j++;
                                    }
                                } else {
                                    bm = Layout.EMOJI_FACTORY.getBitmapFromAndroidPua(codept2);
                                    codept = codept2;
                                }
                            }
                        }
                    }
                }
                if (j == runLimit || codept == 9 || bm != null) {
                    boolean inSegment = target >= segstart && target < j;
                    boolean advance = (this.mDir == -1) == runIsRtl;
                    if (inSegment && advance) {
                        return h + measureRun(segstart, offset, j, runIsRtl, fmi);
                    }
                    float w = measureRun(segstart, j, j, runIsRtl, fmi);
                    if (!advance) {
                        w = -w;
                    }
                    h += w;
                    if (inSegment) {
                        return h + measureRun(segstart, offset, j, runIsRtl, null);
                    }
                    if (codept == 9) {
                        if (offset == j) {
                            return h;
                        }
                        h = ((float) this.mDir) * nextTab(((float) this.mDir) * h);
                        if (target == j) {
                            return h;
                        }
                    }
                    if (bm != null) {
                        h += ((float) this.mDir) * ((((float) bm.getWidth()) * (-ascent(j))) / ((float) bm.getHeight()));
                        j++;
                    }
                    segstart = j + 1;
                    j++;
                } else {
                    j++;
                }
            }
        }
        return h;
    }

    private float drawRun(Canvas c, int start, int limit, boolean runIsRtl, float x, int top, int y, int bottom, boolean needWidth) {
        if ((this.mDir == 1) != runIsRtl) {
            return handleRun(start, limit, limit, runIsRtl, c, x, top, y, bottom, null, needWidth);
        }
        float w = -measureRun(start, limit, limit, runIsRtl, null);
        handleRun(start, limit, limit, runIsRtl, c, x + w, top, y, bottom, null, false);
        return w;
    }

    private float measureRun(int start, int offset, int limit, boolean runIsRtl, Paint.FontMetricsInt fmi) {
        return handleRun(start, offset, limit, runIsRtl, null, 0.0f, 0, 0, 0, fmi, true);
    }

    /* access modifiers changed from: package-private */
    public int getOffsetToLeftRightOf(int cursor, boolean toLeft) {
        int runIndex;
        boolean advance;
        int i;
        int i2;
        int i3;
        int lineEnd = this.mLen;
        boolean paraIsRtl = this.mDir == -1;
        int[] runs = this.mDirections.mDirections;
        int runLevel = 0;
        int runStart = 0;
        int runLimit = lineEnd;
        int newCaret = -1;
        boolean trailing = false;
        if (cursor == 0) {
            runIndex = -2;
        } else if (cursor == lineEnd) {
            runIndex = runs.length;
        } else {
            runIndex = 0;
            while (true) {
                if (runIndex >= runs.length) {
                    break;
                }
                runStart = 0 + runs[runIndex];
                if (cursor >= runStart) {
                    runLimit = runStart + (runs[runIndex + 1] & 67108863);
                    if (runLimit > lineEnd) {
                        runLimit = lineEnd;
                    }
                    if (cursor < runLimit) {
                        runLevel = (runs[runIndex + 1] >>> 26) & 63;
                        if (cursor == runStart) {
                            int pos = cursor - 1;
                            int prevRunIndex = 0;
                            while (true) {
                                if (prevRunIndex >= runs.length) {
                                    break;
                                }
                                int prevRunStart = 0 + runs[prevRunIndex];
                                if (pos >= prevRunStart) {
                                    int prevRunLimit = prevRunStart + (runs[prevRunIndex + 1] & 67108863);
                                    if (prevRunLimit > lineEnd) {
                                        prevRunLimit = lineEnd;
                                    }
                                    if (pos < prevRunLimit) {
                                        int prevRunLevel = (runs[prevRunIndex + 1] >>> 26) & 63;
                                        if (prevRunLevel < runLevel) {
                                            runIndex = prevRunIndex;
                                            runLevel = prevRunLevel;
                                            runStart = prevRunStart;
                                            runLimit = prevRunLimit;
                                            trailing = true;
                                            break;
                                        }
                                    } else {
                                        continue;
                                    }
                                }
                                prevRunIndex += 2;
                            }
                        }
                    }
                }
                runIndex += 2;
            }
            if (runIndex != runs.length) {
                boolean runIsRtl = (runLevel & 1) != 0;
                boolean advance2 = toLeft == runIsRtl;
                if (advance2) {
                    i2 = runLimit;
                } else {
                    i2 = runStart;
                }
                if (!(cursor == i2 && advance2 == trailing)) {
                    newCaret = getOffsetBeforeAfter(runIndex, runStart, runLimit, runIsRtl, cursor, advance2);
                    if (advance2) {
                        i3 = runLimit;
                    } else {
                        i3 = runStart;
                    }
                    if (newCaret != i3) {
                        return newCaret;
                    }
                }
            }
        }
        while (true) {
            advance = toLeft == paraIsRtl;
            int otherRunIndex = runIndex + (advance ? 2 : -2);
            if (otherRunIndex >= 0 && otherRunIndex < runs.length) {
                int otherRunStart = 0 + runs[otherRunIndex];
                int otherRunLimit = otherRunStart + (runs[otherRunIndex + 1] & 67108863);
                if (otherRunLimit > lineEnd) {
                    otherRunLimit = lineEnd;
                }
                int otherRunLevel = (runs[otherRunIndex + 1] >>> 26) & 63;
                boolean otherRunIsRtl = (otherRunLevel & 1) != 0;
                boolean advance3 = toLeft == otherRunIsRtl;
                if (newCaret == -1) {
                    if (advance3) {
                        i = otherRunStart;
                    } else {
                        i = otherRunLimit;
                    }
                    newCaret = getOffsetBeforeAfter(otherRunIndex, otherRunStart, otherRunLimit, otherRunIsRtl, i, advance3);
                    if (!advance3) {
                        otherRunLimit = otherRunStart;
                    }
                    if (newCaret != otherRunLimit) {
                        break;
                    }
                    runIndex = otherRunIndex;
                    runLevel = otherRunLevel;
                } else if (otherRunLevel < runLevel) {
                    newCaret = advance3 ? otherRunStart : otherRunLimit;
                }
            }
        }
        if (newCaret == -1) {
            newCaret = advance ? this.mLen + 1 : -1;
        } else if (newCaret <= lineEnd) {
            newCaret = advance ? lineEnd : 0;
        }
        return newCaret;
    }

    private int getOffsetBeforeAfter(int runIndex, int runStart, int runLimit, boolean runIsRtl, int offset, boolean after) {
        int target;
        int spanLimit;
        if (runIndex >= 0) {
            if (offset != (after ? this.mLen : 0)) {
                TextPaint wp = this.mWorkPaint;
                wp.set(this.mPaint);
                int spanStart = runStart;
                if (this.mSpanned == null) {
                    spanLimit = runLimit;
                } else {
                    if (after) {
                        target = offset + 1;
                    } else {
                        target = offset;
                    }
                    int limit = this.mStart + runLimit;
                    while (true) {
                        spanLimit = this.mSpanned.nextSpanTransition(this.mStart + spanStart, limit, MetricAffectingSpan.class) - this.mStart;
                        if (spanLimit >= target) {
                            break;
                        }
                        spanStart = spanLimit;
                    }
                    MetricAffectingSpan[] spans = (MetricAffectingSpan[]) TextUtils.removeEmptySpans((MetricAffectingSpan[]) this.mSpanned.getSpans(this.mStart + spanStart, this.mStart + spanLimit, MetricAffectingSpan.class), this.mSpanned, MetricAffectingSpan.class);
                    if (spans.length > 0) {
                        ReplacementSpan replacement = null;
                        for (int j = 0; j < spans.length; j++) {
                            MetricAffectingSpan span = spans[j];
                            if (span instanceof ReplacementSpan) {
                                replacement = (ReplacementSpan) span;
                            } else {
                                span.updateMeasureState(wp);
                            }
                        }
                        if (replacement != null) {
                            return !after ? spanStart : spanLimit;
                        }
                    }
                }
                int dir = runIsRtl ? 1 : 0;
                int cursorOpt = after ? 0 : 2;
                if (this.mCharsValid) {
                    return wp.getTextRunCursor(this.mChars, spanStart, spanLimit - spanStart, dir, offset, cursorOpt);
                }
                return wp.getTextRunCursor(this.mText, this.mStart + spanStart, this.mStart + spanLimit, dir, this.mStart + offset, cursorOpt) - this.mStart;
            }
        }
        if (after) {
            return TextUtils.getOffsetAfter(this.mText, this.mStart + offset) - this.mStart;
        }
        return TextUtils.getOffsetBefore(this.mText, this.mStart + offset) - this.mStart;
    }

    private static void expandMetricsFromPaint(Paint.FontMetricsInt fmi, TextPaint wp) {
        int previousTop = fmi.top;
        int previousAscent = fmi.ascent;
        int previousDescent = fmi.descent;
        int previousBottom = fmi.bottom;
        int previousLeading = fmi.leading;
        wp.getFontMetricsInt(fmi);
        updateMetrics(fmi, previousTop, previousAscent, previousDescent, previousBottom, previousLeading);
    }

    static void updateMetrics(Paint.FontMetricsInt fmi, int previousTop, int previousAscent, int previousDescent, int previousBottom, int previousLeading) {
        fmi.top = Math.min(fmi.top, previousTop);
        fmi.ascent = Math.min(fmi.ascent, previousAscent);
        fmi.descent = Math.max(fmi.descent, previousDescent);
        fmi.bottom = Math.max(fmi.bottom, previousBottom);
        fmi.leading = Math.max(fmi.leading, previousLeading);
    }

    private float handleText(TextPaint wp, int start, int end, int contextStart, int contextEnd, boolean runIsRtl, Canvas c, float x, int top, int y, int bottom, Paint.FontMetricsInt fmi, boolean needWidth) {
        if (fmi != null) {
            expandMetricsFromPaint(fmi, wp);
        }
        int runLen = end - start;
        if (runLen == 0) {
            return 0.0f;
        }
        float ret = 0.0f;
        int contextLen = contextEnd - contextStart;
        if (needWidth || !(c == null || (wp.bgColor == 0 && wp.underlineColor == 0 && !runIsRtl))) {
            if (this.mCharsValid) {
                ret = wp.getTextRunAdvances(this.mChars, start, runLen, contextStart, contextLen, runIsRtl, (float[]) null, 0);
            } else {
                int delta = this.mStart;
                ret = wp.getTextRunAdvances(this.mText, delta + start, delta + end, delta + contextStart, delta + contextEnd, runIsRtl, (float[]) null, 0);
            }
        }
        if (c != null) {
            if (runIsRtl) {
                x -= ret;
            }
            if (wp.bgColor != 0) {
                int previousColor = wp.getColor();
                Paint.Style previousStyle = wp.getStyle();
                wp.setColor(wp.bgColor);
                wp.setStyle(Paint.Style.FILL);
                c.drawRect(x, (float) top, x + ret, (float) bottom, wp);
                wp.setStyle(previousStyle);
                wp.setColor(previousColor);
            }
            if (wp.underlineColor != 0) {
                float underlineTop = ((float) (wp.baselineShift + y)) + (0.11111111f * wp.getTextSize());
                int previousColor2 = wp.getColor();
                Paint.Style previousStyle2 = wp.getStyle();
                boolean previousAntiAlias = wp.isAntiAlias();
                wp.setStyle(Paint.Style.FILL);
                wp.setAntiAlias(true);
                wp.setColor(wp.underlineColor);
                c.drawRect(x, underlineTop, x + ret, underlineTop + wp.underlineThickness, wp);
                wp.setStyle(previousStyle2);
                wp.setColor(previousColor2);
                wp.setAntiAlias(previousAntiAlias);
            }
            drawTextRun(c, wp, start, end, contextStart, contextEnd, runIsRtl, x, y + wp.baselineShift);
        }
        if (runIsRtl) {
            return -ret;
        }
        return ret;
    }

    private float handleReplacement(ReplacementSpan replacement, TextPaint wp, int start, int limit, boolean runIsRtl, Canvas c, float x, int top, int y, int bottom, Paint.FontMetricsInt fmi, boolean needWidth) {
        float ret = 0.0f;
        int textStart = this.mStart + start;
        int textLimit = this.mStart + limit;
        if (needWidth || (c != null && runIsRtl)) {
            int previousTop = 0;
            int previousAscent = 0;
            int previousDescent = 0;
            int previousBottom = 0;
            int previousLeading = 0;
            boolean needUpdateMetrics = fmi != null;
            if (needUpdateMetrics) {
                previousTop = fmi.top;
                previousAscent = fmi.ascent;
                previousDescent = fmi.descent;
                previousBottom = fmi.bottom;
                previousLeading = fmi.leading;
            }
            ret = (float) replacement.getSize(wp, this.mText, textStart, textLimit, fmi);
            if (needUpdateMetrics) {
                updateMetrics(fmi, previousTop, previousAscent, previousDescent, previousBottom, previousLeading);
            }
        }
        if (c != null) {
            if (runIsRtl) {
                x -= ret;
            }
            replacement.draw(c, this.mText, textStart, textLimit, x, top, y, bottom, wp);
        }
        return runIsRtl ? -ret : ret;
    }

    private float handleRun(int start, int measureLimit, int limit, boolean runIsRtl, Canvas c, float x, int top, int y, int bottom, Paint.FontMetricsInt fmi, boolean needWidth) {
        boolean z;
        boolean z2;
        if (start == measureLimit) {
            TextPaint wp = this.mWorkPaint;
            wp.set(this.mPaint);
            if (fmi != null) {
                expandMetricsFromPaint(fmi, wp);
            }
            return 0.0f;
        } else if (this.mSpanned == null) {
            TextPaint wp2 = this.mWorkPaint;
            wp2.set(this.mPaint);
            return handleText(wp2, start, measureLimit, start, limit, runIsRtl, c, x, top, y, bottom, fmi, needWidth || measureLimit < measureLimit);
        } else {
            this.mMetricAffectingSpanSpanSet.init(this.mSpanned, this.mStart + start, this.mStart + limit);
            this.mCharacterStyleSpanSet.init(this.mSpanned, this.mStart + start, this.mStart + limit);
            int i = start;
            while (i < measureLimit) {
                TextPaint wp3 = this.mWorkPaint;
                wp3.set(this.mPaint);
                int inext = this.mMetricAffectingSpanSpanSet.getNextTransition(this.mStart + i, this.mStart + limit) - this.mStart;
                int mlimit = Math.min(inext, measureLimit);
                ReplacementSpan replacement = null;
                for (int j = 0; j < this.mMetricAffectingSpanSpanSet.numberOfSpans; j++) {
                    if (this.mMetricAffectingSpanSpanSet.spanStarts[j] < this.mStart + mlimit && this.mMetricAffectingSpanSpanSet.spanEnds[j] > this.mStart + i) {
                        MetricAffectingSpan span = this.mMetricAffectingSpanSpanSet.spans[j];
                        if (span instanceof ReplacementSpan) {
                            replacement = (ReplacementSpan) span;
                        } else {
                            span.updateDrawState(wp3);
                        }
                    }
                }
                if (replacement != null) {
                    if (needWidth || mlimit < measureLimit) {
                        z2 = true;
                    } else {
                        z2 = false;
                    }
                    x += handleReplacement(replacement, wp3, i, mlimit, runIsRtl, c, x, top, y, bottom, fmi, z2);
                } else {
                    int j2 = i;
                    while (j2 < mlimit) {
                        int jnext = this.mCharacterStyleSpanSet.getNextTransition(this.mStart + j2, this.mStart + mlimit) - this.mStart;
                        wp3.set(this.mPaint);
                        for (int k = 0; k < this.mCharacterStyleSpanSet.numberOfSpans; k++) {
                            if (this.mCharacterStyleSpanSet.spanStarts[k] < this.mStart + jnext && this.mCharacterStyleSpanSet.spanEnds[k] > this.mStart + j2) {
                                this.mCharacterStyleSpanSet.spans[k].updateDrawState(wp3);
                            }
                        }
                        if (needWidth || jnext < measureLimit) {
                            z = true;
                        } else {
                            z = false;
                        }
                        x += handleText(wp3, j2, jnext, i, inext, runIsRtl, c, x, top, y, bottom, fmi, z);
                        j2 = jnext;
                    }
                }
                i = inext;
            }
            return x - x;
        }
    }

    private void drawTextRun(Canvas c, TextPaint wp, int start, int end, int contextStart, int contextEnd, boolean runIsRtl, float x, int y) {
        if (this.mCharsValid) {
            c.drawTextRun(this.mChars, start, end - start, contextStart, contextEnd - contextStart, x, (float) y, runIsRtl, wp);
            return;
        }
        int delta = this.mStart;
        c.drawTextRun(this.mText, delta + start, delta + end, delta + contextStart, delta + contextEnd, x, (float) y, runIsRtl, wp);
    }

    /* access modifiers changed from: package-private */
    public float ascent(int pos) {
        if (this.mSpanned == null) {
            return this.mPaint.ascent();
        }
        int pos2 = pos + this.mStart;
        MetricAffectingSpan[] spans = (MetricAffectingSpan[]) this.mSpanned.getSpans(pos2, pos2 + 1, MetricAffectingSpan.class);
        if (spans.length == 0) {
            return this.mPaint.ascent();
        }
        TextPaint wp = this.mWorkPaint;
        wp.set(this.mPaint);
        for (MetricAffectingSpan span : spans) {
            span.updateMeasureState(wp);
        }
        return wp.ascent();
    }

    /* access modifiers changed from: package-private */
    public float nextTab(float h) {
        if (this.mTabs != null) {
            return this.mTabs.nextTab(h);
        }
        return Layout.TabStops.nextDefaultStop(h, 20);
    }
}

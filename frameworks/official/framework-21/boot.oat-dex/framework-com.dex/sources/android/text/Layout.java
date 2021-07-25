package android.text;

import android.emoji.EmojiFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.text.TextUtils;
import android.text.method.TextKeyListener;
import android.text.style.AlignmentSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.LineBackgroundSpan;
import android.text.style.ParagraphStyle;
import android.text.style.ReplacementSpan;
import android.text.style.TabStopSpan;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.GrowingArrayUtils;
import java.util.Arrays;

public abstract class Layout {
    static final Directions DIRS_ALL_LEFT_TO_RIGHT = new Directions(new int[]{0, RUN_LENGTH_MASK});
    static final Directions DIRS_ALL_RIGHT_TO_LEFT = new Directions(new int[]{0, 134217727});
    public static final int DIR_LEFT_TO_RIGHT = 1;
    static final int DIR_REQUEST_DEFAULT_LTR = 2;
    static final int DIR_REQUEST_DEFAULT_RTL = -2;
    static final int DIR_REQUEST_LTR = 1;
    static final int DIR_REQUEST_RTL = -1;
    public static final int DIR_RIGHT_TO_LEFT = -1;
    static final char[] ELLIPSIS_NORMAL = {8230};
    static final char[] ELLIPSIS_TWO_DOTS = {8229};
    static final EmojiFactory EMOJI_FACTORY = EmojiFactory.newAvailableInstance();
    static final int MAX_EMOJI;
    static final int MIN_EMOJI;
    private static final ParagraphStyle[] NO_PARA_SPANS = ((ParagraphStyle[]) ArrayUtils.emptyArray(ParagraphStyle.class));
    static final int RUN_LENGTH_MASK = 67108863;
    static final int RUN_LEVEL_MASK = 63;
    static final int RUN_LEVEL_SHIFT = 26;
    static final int RUN_RTL_FLAG = 67108864;
    private static final int TAB_INCREMENT = 20;
    private static final Rect sTempRect = new Rect();
    private Alignment mAlignment;
    private SpanSet<LineBackgroundSpan> mLineBackgroundSpans;
    private TextPaint mPaint;
    private float mSpacingAdd;
    private float mSpacingMult;
    private boolean mSpannedText;
    private CharSequence mText;
    private TextDirectionHeuristic mTextDir;
    private int mWidth;
    TextPaint mWorkPaint;

    public enum Alignment {
        ALIGN_NORMAL,
        ALIGN_OPPOSITE,
        ALIGN_CENTER,
        ALIGN_LEFT,
        ALIGN_RIGHT
    }

    public abstract int getBottomPadding();

    public abstract int getEllipsisCount(int i);

    public abstract int getEllipsisStart(int i);

    public abstract boolean getLineContainsTab(int i);

    public abstract int getLineCount();

    public abstract int getLineDescent(int i);

    public abstract Directions getLineDirections(int i);

    public abstract int getLineStart(int i);

    public abstract int getLineTop(int i);

    public abstract int getParagraphDirection(int i);

    public abstract int getTopPadding();

    static {
        if (EMOJI_FACTORY != null) {
            MIN_EMOJI = EMOJI_FACTORY.getMinimumAndroidPua();
            MAX_EMOJI = EMOJI_FACTORY.getMaximumAndroidPua();
        } else {
            MIN_EMOJI = -1;
            MAX_EMOJI = -1;
        }
    }

    public static float getDesiredWidth(CharSequence source, TextPaint paint) {
        return getDesiredWidth(source, 0, source.length(), paint);
    }

    public static float getDesiredWidth(CharSequence source, int start, int end, TextPaint paint) {
        float need = 0.0f;
        int i = start;
        while (i <= end) {
            int next = TextUtils.indexOf(source, '\n', i, end);
            if (next < 0) {
                next = end;
            }
            float w = measurePara(paint, source, i, next);
            if (w > need) {
                need = w;
            }
            i = next + 1;
        }
        return need;
    }

    protected Layout(CharSequence text, TextPaint paint, int width, Alignment align, float spacingMult, float spacingAdd) {
        this(text, paint, width, align, TextDirectionHeuristics.FIRSTSTRONG_LTR, spacingMult, spacingAdd);
    }

    protected Layout(CharSequence text, TextPaint paint, int width, Alignment align, TextDirectionHeuristic textDir, float spacingMult, float spacingAdd) {
        this.mAlignment = Alignment.ALIGN_NORMAL;
        if (width < 0) {
            throw new IllegalArgumentException("Layout: " + width + " < 0");
        }
        if (paint != null) {
            paint.bgColor = 0;
            paint.baselineShift = 0;
        }
        this.mText = text;
        this.mPaint = paint;
        this.mWorkPaint = new TextPaint();
        this.mWidth = width;
        this.mAlignment = align;
        this.mSpacingMult = spacingMult;
        this.mSpacingAdd = spacingAdd;
        this.mSpannedText = text instanceof Spanned;
        this.mTextDir = textDir;
    }

    /* access modifiers changed from: package-private */
    public void replaceWith(CharSequence text, TextPaint paint, int width, Alignment align, float spacingmult, float spacingadd) {
        if (width < 0) {
            throw new IllegalArgumentException("Layout: " + width + " < 0");
        }
        this.mText = text;
        this.mPaint = paint;
        this.mWidth = width;
        this.mAlignment = align;
        this.mSpacingMult = spacingmult;
        this.mSpacingAdd = spacingadd;
        this.mSpannedText = text instanceof Spanned;
    }

    public void draw(Canvas c) {
        draw(c, null, null, 0);
    }

    public void draw(Canvas canvas, Path highlight, Paint highlightPaint, int cursorOffsetVertical) {
        long lineRange = getLineRangeForDraw(canvas);
        int firstLine = TextUtils.unpackRangeStartFromLong(lineRange);
        int lastLine = TextUtils.unpackRangeEndFromLong(lineRange);
        if (lastLine >= 0) {
            drawBackground(canvas, highlight, highlightPaint, cursorOffsetVertical, firstLine, lastLine);
            drawText(canvas, firstLine, lastLine);
        }
    }

    public void drawText(Canvas canvas, int firstLine, int lastLine) {
        int x;
        boolean isFirstParaLine;
        int previousLineBottom = getLineTop(firstLine);
        int previousLineEnd = getLineStart(firstLine);
        ParagraphStyle[] spans = NO_PARA_SPANS;
        int spanEnd = 0;
        TextPaint paint = this.mPaint;
        CharSequence buf = this.mText;
        Alignment paraAlign = this.mAlignment;
        TabStops tabStops = null;
        boolean tabStopsIsInitialized = false;
        TextLine tl = TextLine.obtain();
        for (int i = firstLine; i <= lastLine; i++) {
            previousLineEnd = getLineStart(i + 1);
            int end = getLineVisibleEnd(i, previousLineEnd, previousLineEnd);
            int lbottom = getLineTop(i + 1);
            previousLineBottom = lbottom;
            int lbaseline = lbottom - getLineDescent(i);
            int dir = getParagraphDirection(i);
            int left = 0;
            int right = this.mWidth;
            if (this.mSpannedText) {
                Spanned sp = (Spanned) buf;
                int textLength = buf.length();
                if (previousLineEnd == 0 || buf.charAt(previousLineEnd - 1) == '\n') {
                    isFirstParaLine = true;
                } else {
                    isFirstParaLine = false;
                }
                if (previousLineEnd >= spanEnd && (i == firstLine || isFirstParaLine)) {
                    spanEnd = sp.nextSpanTransition(previousLineEnd, textLength, ParagraphStyle.class);
                    spans = (ParagraphStyle[]) getParagraphSpans(sp, previousLineEnd, spanEnd, ParagraphStyle.class);
                    paraAlign = this.mAlignment;
                    int n = spans.length - 1;
                    while (true) {
                        if (n < 0) {
                            break;
                        } else if (spans[n] instanceof AlignmentSpan) {
                            paraAlign = ((AlignmentSpan) spans[n]).getAlignment();
                            break;
                        } else {
                            n--;
                        }
                    }
                    tabStopsIsInitialized = false;
                }
                int length = spans.length;
                boolean useFirstLineMargin = isFirstParaLine;
                int n2 = 0;
                while (true) {
                    if (n2 >= length) {
                        break;
                    }
                    if (spans[n2] instanceof LeadingMarginSpan.LeadingMarginSpan2) {
                        if (i < getLineForOffset(sp.getSpanStart(spans[n2])) + ((LeadingMarginSpan.LeadingMarginSpan2) spans[n2]).getLeadingMarginLineCount()) {
                            useFirstLineMargin = true;
                            break;
                        }
                    }
                    n2++;
                }
                for (int n3 = 0; n3 < length; n3++) {
                    if (spans[n3] instanceof LeadingMarginSpan) {
                        LeadingMarginSpan margin = (LeadingMarginSpan) spans[n3];
                        if (dir == -1) {
                            margin.drawLeadingMargin(canvas, paint, right, dir, previousLineBottom, lbaseline, lbottom, buf, previousLineEnd, end, isFirstParaLine, this);
                            right -= margin.getLeadingMargin(useFirstLineMargin);
                        } else {
                            margin.drawLeadingMargin(canvas, paint, left, dir, previousLineBottom, lbaseline, lbottom, buf, previousLineEnd, end, isFirstParaLine, this);
                            left += margin.getLeadingMargin(useFirstLineMargin);
                        }
                    }
                }
            }
            boolean hasTabOrEmoji = getLineContainsTab(i);
            if (!hasTabOrEmoji || tabStopsIsInitialized) {
                tabStops = tabStops;
            } else {
                if (tabStops == null) {
                    tabStops = new TabStops(20, spans);
                } else {
                    tabStops.reset(20, spans);
                    tabStops = tabStops;
                }
                tabStopsIsInitialized = true;
            }
            Alignment align = paraAlign;
            if (align == Alignment.ALIGN_LEFT) {
                align = dir == 1 ? Alignment.ALIGN_NORMAL : Alignment.ALIGN_OPPOSITE;
            } else if (align == Alignment.ALIGN_RIGHT) {
                align = dir == 1 ? Alignment.ALIGN_OPPOSITE : Alignment.ALIGN_NORMAL;
            }
            if (align != Alignment.ALIGN_NORMAL) {
                int max = (int) getLineExtent(i, tabStops, false);
                if (align != Alignment.ALIGN_OPPOSITE) {
                    x = ((right + left) - (max & -2)) >> 1;
                } else if (dir == 1) {
                    x = right - max;
                } else {
                    x = left - max;
                }
            } else if (dir == 1) {
                x = left;
            } else {
                x = right;
            }
            Directions directions = getLineDirections(i);
            if (directions != DIRS_ALL_LEFT_TO_RIGHT || this.mSpannedText || hasTabOrEmoji) {
                tl.set(paint, buf, previousLineEnd, end, dir, directions, hasTabOrEmoji, tabStops);
                tl.draw(canvas, (float) x, previousLineBottom, lbaseline, lbottom);
            } else {
                canvas.drawText(buf, previousLineEnd, end, (float) x, (float) lbaseline, paint);
            }
        }
        TextLine.recycle(tl);
    }

    public void drawBackground(Canvas canvas, Path highlight, Paint highlightPaint, int cursorOffsetVertical, int firstLine, int lastLine) {
        if (this.mSpannedText) {
            if (this.mLineBackgroundSpans == null) {
                this.mLineBackgroundSpans = new SpanSet<>(LineBackgroundSpan.class);
            }
            Spanned buffer = (Spanned) this.mText;
            int textLength = buffer.length();
            this.mLineBackgroundSpans.init(buffer, 0, textLength);
            if (this.mLineBackgroundSpans.numberOfSpans > 0) {
                int previousLineBottom = getLineTop(firstLine);
                int previousLineEnd = getLineStart(firstLine);
                ParagraphStyle[] spans = NO_PARA_SPANS;
                int spansLength = 0;
                TextPaint paint = this.mPaint;
                int spanEnd = 0;
                int width = this.mWidth;
                for (int i = firstLine; i <= lastLine; i++) {
                    int end = getLineStart(i + 1);
                    previousLineEnd = end;
                    int lbottom = getLineTop(i + 1);
                    previousLineBottom = lbottom;
                    int lbaseline = lbottom - getLineDescent(i);
                    if (previousLineEnd >= spanEnd) {
                        spanEnd = this.mLineBackgroundSpans.getNextTransition(previousLineEnd, textLength);
                        spansLength = 0;
                        if (previousLineEnd != end || previousLineEnd == 0) {
                            for (int j = 0; j < this.mLineBackgroundSpans.numberOfSpans; j++) {
                                if (this.mLineBackgroundSpans.spanStarts[j] < end && this.mLineBackgroundSpans.spanEnds[j] > previousLineEnd) {
                                    spans = (ParagraphStyle[]) GrowingArrayUtils.append(spans, spansLength, this.mLineBackgroundSpans.spans[j]);
                                    spansLength++;
                                }
                            }
                        }
                    }
                    for (int n = 0; n < spansLength; n++) {
                        ((LineBackgroundSpan) spans[n]).drawBackground(canvas, paint, 0, width, previousLineBottom, lbaseline, lbottom, buffer, previousLineEnd, end, i);
                    }
                }
            }
            this.mLineBackgroundSpans.recycle();
        }
        if (highlight != null) {
            if (cursorOffsetVertical != 0) {
                canvas.translate(0.0f, (float) cursorOffsetVertical);
            }
            canvas.drawPath(highlight, highlightPaint);
            if (cursorOffsetVertical != 0) {
                canvas.translate(0.0f, (float) (-cursorOffsetVertical));
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x002e, code lost:
        if (r3 < r0) goto L_0x0038;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:?, code lost:
        return android.text.TextUtils.packRangeInLong(0, -1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:?, code lost:
        return android.text.TextUtils.packRangeInLong(getLineForVertical(r3), getLineForVertical(r0));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x001e, code lost:
        r3 = java.lang.Math.max(r2, 0);
        r0 = java.lang.Math.min(getLineTop(getLineCount()), r1);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public long getLineRangeForDraw(android.graphics.Canvas r9) {
        /*
            r8 = this;
            r7 = -1
            r5 = 0
            android.graphics.Rect r6 = android.text.Layout.sTempRect
            monitor-enter(r6)
            android.graphics.Rect r4 = android.text.Layout.sTempRect     // Catch:{ all -> 0x0035 }
            boolean r4 = r9.getClipBounds(r4)     // Catch:{ all -> 0x0035 }
            if (r4 != 0) goto L_0x0015
            r4 = 0
            r5 = -1
            long r4 = android.text.TextUtils.packRangeInLong(r4, r5)     // Catch:{ all -> 0x0035 }
            monitor-exit(r6)     // Catch:{ all -> 0x0035 }
        L_0x0014:
            return r4
        L_0x0015:
            android.graphics.Rect r4 = android.text.Layout.sTempRect     // Catch:{ all -> 0x0035 }
            int r2 = r4.top     // Catch:{ all -> 0x0035 }
            android.graphics.Rect r4 = android.text.Layout.sTempRect     // Catch:{ all -> 0x0035 }
            int r1 = r4.bottom     // Catch:{ all -> 0x0035 }
            monitor-exit(r6)     // Catch:{ all -> 0x0035 }
            int r3 = java.lang.Math.max(r2, r5)
            int r4 = r8.getLineCount()
            int r4 = r8.getLineTop(r4)
            int r0 = java.lang.Math.min(r4, r1)
            if (r3 < r0) goto L_0x0038
            long r4 = android.text.TextUtils.packRangeInLong(r5, r7)
            goto L_0x0014
        L_0x0035:
            r4 = move-exception
            monitor-exit(r6)
            throw r4
        L_0x0038:
            int r4 = r8.getLineForVertical(r3)
            int r5 = r8.getLineForVertical(r0)
            long r4 = android.text.TextUtils.packRangeInLong(r4, r5)
            goto L_0x0014
        */
        throw new UnsupportedOperationException("Method not decompiled: android.text.Layout.getLineRangeForDraw(android.graphics.Canvas):long");
    }

    private int getLineStartPos(int line, int left, int right) {
        Alignment align = getParagraphAlignment(line);
        int dir = getParagraphDirection(line);
        if (align == Alignment.ALIGN_LEFT) {
            align = dir == 1 ? Alignment.ALIGN_NORMAL : Alignment.ALIGN_OPPOSITE;
        } else if (align == Alignment.ALIGN_RIGHT) {
            align = dir == 1 ? Alignment.ALIGN_OPPOSITE : Alignment.ALIGN_NORMAL;
        }
        if (align == Alignment.ALIGN_NORMAL) {
            return dir == 1 ? left : right;
        }
        TabStops tabStops = null;
        if (this.mSpannedText && getLineContainsTab(line)) {
            Spanned spanned = (Spanned) this.mText;
            int start = getLineStart(line);
            TabStopSpan[] tabSpans = (TabStopSpan[]) getParagraphSpans(spanned, start, spanned.nextSpanTransition(start, spanned.length(), TabStopSpan.class), TabStopSpan.class);
            if (tabSpans.length > 0) {
                tabStops = new TabStops(20, tabSpans);
            }
        }
        int max = (int) getLineExtent(line, tabStops, false);
        if (align != Alignment.ALIGN_OPPOSITE) {
            return ((left + right) - (max & -2)) >> 1;
        } else if (dir == 1) {
            return right - max;
        } else {
            return left - max;
        }
    }

    public final CharSequence getText() {
        return this.mText;
    }

    public final TextPaint getPaint() {
        return this.mPaint;
    }

    public final int getWidth() {
        return this.mWidth;
    }

    public int getEllipsizedWidth() {
        return this.mWidth;
    }

    public final void increaseWidthTo(int wid) {
        if (wid < this.mWidth) {
            throw new RuntimeException("attempted to reduce Layout width");
        }
        this.mWidth = wid;
    }

    public int getHeight() {
        return getLineTop(getLineCount());
    }

    public final Alignment getAlignment() {
        return this.mAlignment;
    }

    public final float getSpacingMultiplier() {
        return this.mSpacingMult;
    }

    public final float getSpacingAdd() {
        return this.mSpacingAdd;
    }

    public final TextDirectionHeuristic getTextDirectionHeuristic() {
        return this.mTextDir;
    }

    public int getLineBounds(int line, Rect bounds) {
        if (bounds != null) {
            bounds.left = 0;
            bounds.top = getLineTop(line);
            bounds.right = this.mWidth;
            bounds.bottom = getLineTop(line + 1);
        }
        return getLineBaseline(line);
    }

    public boolean isLevelBoundary(int offset) {
        int line = getLineForOffset(offset);
        Directions dirs = getLineDirections(line);
        if (dirs == DIRS_ALL_LEFT_TO_RIGHT || dirs == DIRS_ALL_RIGHT_TO_LEFT) {
            return false;
        }
        int[] runs = dirs.mDirections;
        int lineStart = getLineStart(line);
        int lineEnd = getLineEnd(line);
        if (offset == lineStart || offset == lineEnd) {
            return ((runs[(offset == lineStart ? 0 : runs.length + -2) + 1] >>> 26) & 63) != (getParagraphDirection(line) == 1 ? 0 : 1);
        }
        int offset2 = offset - lineStart;
        for (int i = 0; i < runs.length; i += 2) {
            if (offset2 == runs[i]) {
                return true;
            }
        }
        return false;
    }

    public boolean isRtlCharAt(int offset) {
        boolean z = true;
        int line = getLineForOffset(offset);
        Directions dirs = getLineDirections(line);
        if (dirs == DIRS_ALL_LEFT_TO_RIGHT) {
            return false;
        }
        if (dirs == DIRS_ALL_RIGHT_TO_LEFT) {
            return true;
        }
        int[] runs = dirs.mDirections;
        int lineStart = getLineStart(line);
        for (int i = 0; i < runs.length; i += 2) {
            int start = lineStart + runs[i];
            int limit = start + (runs[i + 1] & RUN_LENGTH_MASK);
            if (offset >= start && offset < limit) {
                if (((runs[i + 1] >>> 26) & 63 & 1) == 0) {
                    z = false;
                }
                return z;
            }
        }
        return false;
    }

    private boolean primaryIsTrailingPrevious(int offset) {
        boolean z = true;
        int line = getLineForOffset(offset);
        int lineStart = getLineStart(line);
        int lineEnd = getLineEnd(line);
        int[] runs = getLineDirections(line).mDirections;
        int levelAt = -1;
        int i = 0;
        while (true) {
            if (i >= runs.length) {
                break;
            }
            int start = lineStart + runs[i];
            int limit = start + (runs[i + 1] & RUN_LENGTH_MASK);
            if (limit > lineEnd) {
                limit = lineEnd;
            }
            if (offset < start || offset >= limit) {
                i += 2;
            } else if (offset > start) {
                return false;
            } else {
                levelAt = (runs[i + 1] >>> 26) & 63;
            }
        }
        if (levelAt == -1) {
            if (getParagraphDirection(line) == 1) {
                levelAt = 0;
            } else {
                levelAt = 1;
            }
        }
        int levelBefore = -1;
        if (offset != lineStart) {
            int offset2 = offset - 1;
            int i2 = 0;
            while (true) {
                if (i2 < runs.length) {
                    int start2 = lineStart + runs[i2];
                    int limit2 = start2 + (runs[i2 + 1] & RUN_LENGTH_MASK);
                    if (limit2 > lineEnd) {
                        limit2 = lineEnd;
                    }
                    if (offset2 >= start2 && offset2 < limit2) {
                        levelBefore = (runs[i2 + 1] >>> 26) & 63;
                        break;
                    }
                    i2 += 2;
                } else {
                    break;
                }
            }
        } else if (getParagraphDirection(line) == 1) {
            levelBefore = 0;
        } else {
            levelBefore = 1;
        }
        if (levelBefore >= levelAt) {
            z = false;
        }
        return z;
    }

    public float getPrimaryHorizontal(int offset) {
        return getPrimaryHorizontal(offset, false);
    }

    public float getPrimaryHorizontal(int offset, boolean clamped) {
        return getHorizontal(offset, primaryIsTrailingPrevious(offset), clamped);
    }

    public float getSecondaryHorizontal(int offset) {
        return getSecondaryHorizontal(offset, false);
    }

    public float getSecondaryHorizontal(int offset, boolean clamped) {
        return getHorizontal(offset, !primaryIsTrailingPrevious(offset), clamped);
    }

    private float getHorizontal(int offset, boolean trailing, boolean clamped) {
        return getHorizontal(offset, trailing, getLineForOffset(offset), clamped);
    }

    private float getHorizontal(int offset, boolean trailing, int line, boolean clamped) {
        int start = getLineStart(line);
        int end = getLineEnd(line);
        int dir = getParagraphDirection(line);
        boolean hasTabOrEmoji = getLineContainsTab(line);
        Directions directions = getLineDirections(line);
        TabStops tabStops = null;
        if (hasTabOrEmoji && (this.mText instanceof Spanned)) {
            TabStopSpan[] tabs = (TabStopSpan[]) getParagraphSpans((Spanned) this.mText, start, end, TabStopSpan.class);
            if (tabs.length > 0) {
                tabStops = new TabStops(20, tabs);
            }
        }
        TextLine tl = TextLine.obtain();
        tl.set(this.mPaint, this.mText, start, end, dir, directions, hasTabOrEmoji, tabStops);
        float wid = tl.measure(offset - start, trailing, null);
        TextLine.recycle(tl);
        if (clamped && wid > ((float) this.mWidth)) {
            wid = (float) this.mWidth;
        }
        return ((float) getLineStartPos(line, getParagraphLeft(line), getParagraphRight(line))) + wid;
    }

    public float getLineLeft(int line) {
        int dir = getParagraphDirection(line);
        Alignment align = getParagraphAlignment(line);
        if (align == Alignment.ALIGN_LEFT) {
            return 0.0f;
        }
        if (align == Alignment.ALIGN_NORMAL) {
            if (dir == -1) {
                return ((float) getParagraphRight(line)) - getLineMax(line);
            }
            return 0.0f;
        } else if (align == Alignment.ALIGN_RIGHT) {
            return ((float) this.mWidth) - getLineMax(line);
        } else {
            if (align != Alignment.ALIGN_OPPOSITE) {
                int left = getParagraphLeft(line);
                int right = getParagraphRight(line);
                return (float) ((((right - left) - (((int) getLineMax(line)) & -2)) / 2) + left);
            } else if (dir != -1) {
                return ((float) this.mWidth) - getLineMax(line);
            } else {
                return 0.0f;
            }
        }
    }

    public float getLineRight(int line) {
        int dir = getParagraphDirection(line);
        Alignment align = getParagraphAlignment(line);
        if (align == Alignment.ALIGN_LEFT) {
            return ((float) getParagraphLeft(line)) + getLineMax(line);
        }
        if (align == Alignment.ALIGN_NORMAL) {
            if (dir == -1) {
                return (float) this.mWidth;
            }
            return ((float) getParagraphLeft(line)) + getLineMax(line);
        } else if (align == Alignment.ALIGN_RIGHT) {
            return (float) this.mWidth;
        } else {
            if (align != Alignment.ALIGN_OPPOSITE) {
                int left = getParagraphLeft(line);
                int right = getParagraphRight(line);
                return (float) (right - (((right - left) - (((int) getLineMax(line)) & -2)) / 2));
            } else if (dir == -1) {
                return getLineMax(line);
            } else {
                return (float) this.mWidth;
            }
        }
    }

    public float getLineMax(int line) {
        float margin = (float) getParagraphLeadingMargin(line);
        float signedExtent = getLineExtent(line, false);
        if (signedExtent < 0.0f) {
            signedExtent = -signedExtent;
        }
        return margin + signedExtent;
    }

    public float getLineWidth(int line) {
        float margin = (float) getParagraphLeadingMargin(line);
        float signedExtent = getLineExtent(line, true);
        if (signedExtent < 0.0f) {
            signedExtent = -signedExtent;
        }
        return margin + signedExtent;
    }

    private float getLineExtent(int line, boolean full) {
        int start = getLineStart(line);
        int end = full ? getLineEnd(line) : getLineVisibleEnd(line);
        boolean hasTabsOrEmoji = getLineContainsTab(line);
        TabStops tabStops = null;
        if (hasTabsOrEmoji && (this.mText instanceof Spanned)) {
            TabStopSpan[] tabs = (TabStopSpan[]) getParagraphSpans((Spanned) this.mText, start, end, TabStopSpan.class);
            if (tabs.length > 0) {
                tabStops = new TabStops(20, tabs);
            }
        }
        Directions directions = getLineDirections(line);
        if (directions == null) {
            return 0.0f;
        }
        int dir = getParagraphDirection(line);
        TextLine tl = TextLine.obtain();
        tl.set(this.mPaint, this.mText, start, end, dir, directions, hasTabsOrEmoji, tabStops);
        float metrics = tl.metrics(null);
        TextLine.recycle(tl);
        return metrics;
    }

    private float getLineExtent(int line, TabStops tabStops, boolean full) {
        int start = getLineStart(line);
        int end = full ? getLineEnd(line) : getLineVisibleEnd(line);
        boolean hasTabsOrEmoji = getLineContainsTab(line);
        Directions directions = getLineDirections(line);
        int dir = getParagraphDirection(line);
        TextLine tl = TextLine.obtain();
        tl.set(this.mPaint, this.mText, start, end, dir, directions, hasTabsOrEmoji, tabStops);
        float width = tl.metrics(null);
        TextLine.recycle(tl);
        return width;
    }

    public int getLineForVertical(int vertical) {
        int high = getLineCount();
        int low = -1;
        while (high - low > 1) {
            int guess = (high + low) / 2;
            if (getLineTop(guess) > vertical) {
                high = guess;
            } else {
                low = guess;
            }
        }
        if (low < 0) {
            return 0;
        }
        return low;
    }

    public int getLineForOffset(int offset) {
        int high = getLineCount();
        int low = -1;
        while (high - low > 1) {
            int guess = (high + low) / 2;
            if (getLineStart(guess) > offset) {
                high = guess;
            } else {
                low = guess;
            }
        }
        if (low < 0) {
            return 0;
        }
        return low;
    }

    public int getOffsetForHorizontal(int line, float horiz) {
        int max = getLineEnd(line) - 1;
        int min = getLineStart(line);
        Directions dirs = getLineDirections(line);
        if (line == getLineCount() - 1) {
            max++;
        }
        int best = min;
        float bestdist = Math.abs(getPrimaryHorizontal(best) - horiz);
        for (int i = 0; i < dirs.mDirections.length; i += 2) {
            int here = min + dirs.mDirections[i];
            int there = here + (dirs.mDirections[i + 1] & RUN_LENGTH_MASK);
            int swap = (dirs.mDirections[i + 1] & 67108864) != 0 ? -1 : 1;
            if (there > max) {
                there = max;
            }
            int high = (there - 1) + 1;
            int low = (here + 1) - 1;
            while (high - low > 1) {
                int guess = (high + low) / 2;
                if (getPrimaryHorizontal(getOffsetAtStartOf(guess)) * ((float) swap) >= ((float) swap) * horiz) {
                    high = guess;
                } else {
                    low = guess;
                }
            }
            if (low < here + 1) {
                low = here + 1;
            }
            if (low < there) {
                int low2 = getOffsetAtStartOf(low);
                float dist = Math.abs(getPrimaryHorizontal(low2) - horiz);
                int aft = TextUtils.getOffsetAfter(this.mText, low2);
                if (aft < there) {
                    float other = Math.abs(getPrimaryHorizontal(aft) - horiz);
                    if (other < dist) {
                        dist = other;
                        low2 = aft;
                    }
                }
                if (dist < bestdist) {
                    bestdist = dist;
                    best = low2;
                }
            }
            float dist2 = Math.abs(getPrimaryHorizontal(here) - horiz);
            if (dist2 < bestdist) {
                bestdist = dist2;
                best = here;
            }
        }
        return Math.abs(getPrimaryHorizontal(max) - horiz) <= bestdist ? max : best;
    }

    public final int getLineEnd(int line) {
        return getLineStart(line + 1);
    }

    public int getLineVisibleEnd(int line) {
        return getLineVisibleEnd(line, getLineStart(line), getLineStart(line + 1));
    }

    private int getLineVisibleEnd(int line, int start, int end) {
        CharSequence text = this.mText;
        if (line == getLineCount() - 1) {
            return end;
        }
        while (end > start) {
            char ch = text.charAt(end - 1);
            if (ch != '\n') {
                if (ch != ' ' && ch != '\t') {
                    break;
                }
                end--;
            } else {
                return end - 1;
            }
        }
        return end;
    }

    public final int getLineBottom(int line) {
        return getLineTop(line + 1);
    }

    public final int getLineBaseline(int line) {
        return getLineTop(line + 1) - getLineDescent(line);
    }

    public final int getLineAscent(int line) {
        return getLineTop(line) - (getLineTop(line + 1) - getLineDescent(line));
    }

    public int getOffsetToLeftOf(int offset) {
        return getOffsetToLeftRightOf(offset, true);
    }

    public int getOffsetToRightOf(int offset) {
        return getOffsetToLeftRightOf(offset, false);
    }

    private int getOffsetToLeftRightOf(int caret, boolean toLeft) {
        int line = getLineForOffset(caret);
        int lineStart = getLineStart(line);
        int lineEnd = getLineEnd(line);
        int lineDir = getParagraphDirection(line);
        boolean lineChanged = false;
        if (toLeft == (lineDir == -1)) {
            if (caret == lineEnd) {
                if (line >= getLineCount() - 1) {
                    return caret;
                }
                lineChanged = true;
                line++;
            }
        } else if (caret == lineStart) {
            if (line <= 0) {
                return caret;
            }
            lineChanged = true;
            line--;
        }
        if (lineChanged) {
            lineStart = getLineStart(line);
            lineEnd = getLineEnd(line);
            int newDir = getParagraphDirection(line);
            if (newDir != lineDir) {
                toLeft = !toLeft;
                lineDir = newDir;
            }
        }
        Directions directions = getLineDirections(line);
        TextLine tl = TextLine.obtain();
        tl.set(this.mPaint, this.mText, lineStart, lineEnd, lineDir, directions, false, null);
        int caret2 = lineStart + tl.getOffsetToLeftRightOf(caret - lineStart, toLeft);
        TextLine.recycle(tl);
        return caret2;
    }

    private int getOffsetAtStartOf(int offset) {
        char c1;
        if (offset == 0) {
            return 0;
        }
        CharSequence text = this.mText;
        char c = text.charAt(offset);
        if (c >= 56320 && c <= 57343 && (c1 = text.charAt(offset - 1)) >= 55296 && c1 <= 56319) {
            offset--;
        }
        if (this.mSpannedText) {
            ReplacementSpan[] spans = (ReplacementSpan[]) ((Spanned) text).getSpans(offset, offset, ReplacementSpan.class);
            for (int i = 0; i < spans.length; i++) {
                int start = ((Spanned) text).getSpanStart(spans[i]);
                int end = ((Spanned) text).getSpanEnd(spans[i]);
                if (start < offset && end > offset) {
                    offset = start;
                }
            }
        }
        return offset;
    }

    /* access modifiers changed from: package-private */
    /* renamed from: android.text.Layout$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$android$text$Layout$Alignment = new int[Alignment.values().length];

        static {
            try {
                $SwitchMap$android$text$Layout$Alignment[Alignment.ALIGN_LEFT.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$android$text$Layout$Alignment[Alignment.ALIGN_NORMAL.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
        }
    }

    public boolean shouldClampCursor(int line) {
        switch (AnonymousClass1.$SwitchMap$android$text$Layout$Alignment[getParagraphAlignment(line).ordinal()]) {
            case 1:
                return true;
            case 2:
                return getParagraphDirection(line) > 0;
            default:
                return false;
        }
    }

    public void getCursorPath(int point, Path dest, CharSequence editingBuffer) {
        float h2;
        dest.reset();
        int line = getLineForOffset(point);
        int top = getLineTop(line);
        int bottom = getLineTop(line + 1);
        boolean clamped = shouldClampCursor(line);
        float h1 = getPrimaryHorizontal(point, clamped) - 0.5f;
        if (isLevelBoundary(point)) {
            h2 = getSecondaryHorizontal(point, clamped) - 0.5f;
        } else {
            h2 = h1;
        }
        int caps = TextKeyListener.getMetaState(editingBuffer, 1) | TextKeyListener.getMetaState(editingBuffer, 2048);
        int fn = TextKeyListener.getMetaState(editingBuffer, 2);
        int dist = 0;
        if (!(caps == 0 && fn == 0)) {
            dist = (bottom - top) >> 2;
            if (fn != 0) {
                top += dist;
            }
            if (caps != 0) {
                bottom -= dist;
            }
        }
        if (h1 < 0.5f) {
            h1 = 0.5f;
        }
        if (h2 < 0.5f) {
            h2 = 0.5f;
        }
        if (Float.compare(h1, h2) == 0) {
            dest.moveTo(h1, (float) top);
            dest.lineTo(h1, (float) bottom);
        } else {
            dest.moveTo(h1, (float) top);
            dest.lineTo(h1, (float) ((top + bottom) >> 1));
            dest.moveTo(h2, (float) ((top + bottom) >> 1));
            dest.lineTo(h2, (float) bottom);
        }
        if (caps == 2) {
            dest.moveTo(h2, (float) bottom);
            dest.lineTo(h2 - ((float) dist), (float) (bottom + dist));
            dest.lineTo(h2, (float) bottom);
            dest.lineTo(((float) dist) + h2, (float) (bottom + dist));
        } else if (caps == 1) {
            dest.moveTo(h2, (float) bottom);
            dest.lineTo(h2 - ((float) dist), (float) (bottom + dist));
            dest.moveTo(h2 - ((float) dist), ((float) (bottom + dist)) - 0.5f);
            dest.lineTo(((float) dist) + h2, ((float) (bottom + dist)) - 0.5f);
            dest.moveTo(((float) dist) + h2, (float) (bottom + dist));
            dest.lineTo(h2, (float) bottom);
        }
        if (fn == 2) {
            dest.moveTo(h1, (float) top);
            dest.lineTo(h1 - ((float) dist), (float) (top - dist));
            dest.lineTo(h1, (float) top);
            dest.lineTo(((float) dist) + h1, (float) (top - dist));
        } else if (fn == 1) {
            dest.moveTo(h1, (float) top);
            dest.lineTo(h1 - ((float) dist), (float) (top - dist));
            dest.moveTo(h1 - ((float) dist), ((float) (top - dist)) + 0.5f);
            dest.lineTo(((float) dist) + h1, ((float) (top - dist)) + 0.5f);
            dest.moveTo(((float) dist) + h1, (float) (top - dist));
            dest.lineTo(h1, (float) top);
        }
    }

    private void addSelection(int line, int start, int end, int top, int bottom, Path dest) {
        int st;
        int en;
        int linestart = getLineStart(line);
        int lineend = getLineEnd(line);
        Directions dirs = getLineDirections(line);
        if (lineend > linestart && this.mText.charAt(lineend - 1) == '\n') {
            lineend--;
        }
        for (int i = 0; i < dirs.mDirections.length; i += 2) {
            int here = linestart + dirs.mDirections[i];
            int there = here + (dirs.mDirections[i + 1] & RUN_LENGTH_MASK);
            if (there > lineend) {
                there = lineend;
            }
            if (start <= there && end >= here && (st = Math.max(start, here)) != (en = Math.min(end, there))) {
                float h1 = getHorizontal(st, false, line, false);
                float h2 = getHorizontal(en, true, line, false);
                dest.addRect(Math.min(h1, h2), (float) top, Math.max(h1, h2), (float) bottom, Path.Direction.CW);
            }
        }
    }

    public void getSelectionPath(int start, int end, Path dest) {
        dest.reset();
        if (start != end) {
            if (end < start) {
                end = start;
                start = end;
            }
            int startline = getLineForOffset(start);
            int endline = getLineForOffset(end);
            int top = getLineTop(startline);
            int bottom = getLineBottom(endline);
            if (startline == endline) {
                addSelection(startline, start, end, top, bottom, dest);
                return;
            }
            float width = (float) this.mWidth;
            addSelection(startline, start, getLineEnd(startline), top, getLineBottom(startline), dest);
            if (getParagraphDirection(startline) == -1) {
                dest.addRect(getLineLeft(startline), (float) top, 0.0f, (float) getLineBottom(startline), Path.Direction.CW);
            } else {
                dest.addRect(getLineRight(startline), (float) top, width, (float) getLineBottom(startline), Path.Direction.CW);
            }
            for (int i = startline + 1; i < endline; i++) {
                dest.addRect(0.0f, (float) getLineTop(i), width, (float) getLineBottom(i), Path.Direction.CW);
            }
            int top2 = getLineTop(endline);
            int bottom2 = getLineBottom(endline);
            addSelection(endline, getLineStart(endline), end, top2, bottom2, dest);
            if (getParagraphDirection(endline) == -1) {
                dest.addRect(width, (float) top2, getLineRight(endline), (float) bottom2, Path.Direction.CW);
            } else {
                dest.addRect(0.0f, (float) top2, getLineLeft(endline), (float) bottom2, Path.Direction.CW);
            }
        }
    }

    public final Alignment getParagraphAlignment(int line) {
        AlignmentSpan[] spans;
        int spanLength;
        Alignment align = this.mAlignment;
        if (!this.mSpannedText || (spanLength = (spans = (AlignmentSpan[]) getParagraphSpans((Spanned) this.mText, getLineStart(line), getLineEnd(line), AlignmentSpan.class)).length) <= 0) {
            return align;
        }
        return spans[spanLength - 1].getAlignment();
    }

    public final int getParagraphLeft(int line) {
        if (getParagraphDirection(line) == -1 || !this.mSpannedText) {
            return 0;
        }
        return getParagraphLeadingMargin(line);
    }

    public final int getParagraphRight(int line) {
        int right = this.mWidth;
        return (getParagraphDirection(line) == 1 || !this.mSpannedText) ? right : right - getParagraphLeadingMargin(line);
    }

    private int getParagraphLeadingMargin(int line) {
        boolean z;
        if (!this.mSpannedText) {
            return 0;
        }
        Spanned spanned = (Spanned) this.mText;
        int lineStart = getLineStart(line);
        LeadingMarginSpan[] spans = (LeadingMarginSpan[]) getParagraphSpans(spanned, lineStart, spanned.nextSpanTransition(lineStart, getLineEnd(line), LeadingMarginSpan.class), LeadingMarginSpan.class);
        if (spans.length == 0) {
            return 0;
        }
        int margin = 0;
        boolean useFirstLineMargin = lineStart == 0 || spanned.charAt(lineStart + -1) == '\n';
        for (int i = 0; i < spans.length; i++) {
            if (spans[i] instanceof LeadingMarginSpan.LeadingMarginSpan2) {
                if (line < getLineForOffset(spanned.getSpanStart(spans[i])) + ((LeadingMarginSpan.LeadingMarginSpan2) spans[i]).getLeadingMarginLineCount()) {
                    z = true;
                } else {
                    z = false;
                }
                useFirstLineMargin |= z;
            }
        }
        for (LeadingMarginSpan span : spans) {
            margin += span.getLeadingMargin(useFirstLineMargin);
        }
        return margin;
    }

    static float measurePara(TextPaint paint, CharSequence text, int start, int end) {
        Directions directions;
        int dir;
        MeasuredText mt = MeasuredText.obtain();
        TextLine tl = TextLine.obtain();
        try {
            mt.setPara(text, start, end, TextDirectionHeuristics.LTR);
            if (mt.mEasy) {
                directions = DIRS_ALL_LEFT_TO_RIGHT;
                dir = 1;
            } else {
                directions = AndroidBidi.directions(mt.mDir, mt.mLevels, 0, mt.mChars, 0, mt.mLen);
                dir = mt.mDir;
            }
            char[] chars = mt.mChars;
            int len = mt.mLen;
            boolean hasTabs = false;
            TabStops tabStops = null;
            int margin = 0;
            if (text instanceof Spanned) {
                LeadingMarginSpan[] spans = (LeadingMarginSpan[]) getParagraphSpans((Spanned) text, start, end, LeadingMarginSpan.class);
                int len$ = spans.length;
                for (int i$ = 0; i$ < len$; i$++) {
                    margin += spans[i$].getLeadingMargin(true);
                }
            }
            int i = 0;
            while (true) {
                if (i >= len) {
                    break;
                } else if (chars[i] == '\t') {
                    hasTabs = true;
                    if (text instanceof Spanned) {
                        Spanned spanned = (Spanned) text;
                        TabStopSpan[] spans2 = (TabStopSpan[]) getParagraphSpans(spanned, start, spanned.nextSpanTransition(start, end, TabStopSpan.class), TabStopSpan.class);
                        if (spans2.length > 0) {
                            tabStops = new TabStops(20, spans2);
                        }
                    }
                } else {
                    i++;
                }
            }
            tl.set(paint, text, start, end, dir, directions, hasTabs, tabStops);
            return ((float) margin) + tl.metrics(null);
        } finally {
            TextLine.recycle(tl);
            MeasuredText.recycle(mt);
        }
    }

    /* access modifiers changed from: package-private */
    public static class TabStops {
        private int mIncrement;
        private int mNumStops;
        private int[] mStops;

        TabStops(int increment, Object[] spans) {
            reset(increment, spans);
        }

        /* access modifiers changed from: package-private */
        public void reset(int increment, Object[] spans) {
            int ns;
            this.mIncrement = increment;
            int ns2 = 0;
            if (spans != null) {
                int[] stops = this.mStops;
                int len$ = spans.length;
                int i$ = 0;
                int ns3 = 0;
                while (i$ < len$) {
                    Object o = spans[i$];
                    if (o instanceof TabStopSpan) {
                        if (stops == null) {
                            stops = new int[10];
                        } else if (ns3 == stops.length) {
                            int[] nstops = new int[(ns3 * 2)];
                            for (int i = 0; i < ns3; i++) {
                                nstops[i] = stops[i];
                            }
                            stops = nstops;
                        }
                        ns = ns3 + 1;
                        stops[ns3] = ((TabStopSpan) o).getTabStop();
                    } else {
                        ns = ns3;
                    }
                    i$++;
                    ns3 = ns;
                }
                if (ns3 > 1) {
                    Arrays.sort(stops, 0, ns3);
                }
                if (stops != this.mStops) {
                    this.mStops = stops;
                }
                ns2 = ns3;
            }
            this.mNumStops = ns2;
        }

        /* access modifiers changed from: package-private */
        public float nextTab(float h) {
            int ns = this.mNumStops;
            if (ns > 0) {
                int[] stops = this.mStops;
                for (int i = 0; i < ns; i++) {
                    int stop = stops[i];
                    if (((float) stop) > h) {
                        return (float) stop;
                    }
                }
            }
            return nextDefaultStop(h, this.mIncrement);
        }

        public static float nextDefaultStop(float h, int inc) {
            return (float) (((int) ((((float) inc) + h) / ((float) inc))) * inc);
        }
    }

    static float nextTab(CharSequence text, int start, int end, float h, Object[] tabs) {
        float nh = Float.MAX_VALUE;
        boolean alltabs = false;
        if (text instanceof Spanned) {
            if (tabs == null) {
                tabs = getParagraphSpans((Spanned) text, start, end, TabStopSpan.class);
                alltabs = true;
            }
            for (int i = 0; i < tabs.length; i++) {
                if (alltabs || (tabs[i] instanceof TabStopSpan)) {
                    int where = ((TabStopSpan) tabs[i]).getTabStop();
                    if (((float) where) < nh && ((float) where) > h) {
                        nh = (float) where;
                    }
                }
            }
            if (nh != Float.MAX_VALUE) {
                return nh;
            }
        }
        return (float) (((int) ((h + 20.0f) / 20.0f)) * 20);
    }

    /* access modifiers changed from: protected */
    public final boolean isSpanned() {
        return this.mSpannedText;
    }

    static <T> T[] getParagraphSpans(Spanned text, int start, int end, Class<T> type) {
        return (start != end || start <= 0) ? (T[]) text.getSpans(start, end, type) : (T[]) ArrayUtils.emptyArray(type);
    }

    private char getEllipsisChar(TextUtils.TruncateAt method) {
        return method == TextUtils.TruncateAt.END_SMALL ? ELLIPSIS_TWO_DOTS[0] : ELLIPSIS_NORMAL[0];
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void ellipsize(int start, int end, int line, char[] dest, int destoff, TextUtils.TruncateAt method) {
        char c;
        int ellipsisCount = getEllipsisCount(line);
        if (ellipsisCount != 0) {
            int ellipsisStart = getEllipsisStart(line);
            int linestart = getLineStart(line);
            for (int i = ellipsisStart; i < ellipsisStart + ellipsisCount; i++) {
                if (i == ellipsisStart) {
                    c = getEllipsisChar(method);
                } else {
                    c = 65279;
                }
                int a = i + linestart;
                if (a >= start && a < end) {
                    dest[(destoff + a) - start] = c;
                }
            }
        }
    }

    public static class Directions {
        int[] mDirections;

        Directions(int[] dirs) {
            this.mDirections = dirs;
        }
    }

    static class Ellipsizer implements CharSequence, GetChars {
        Layout mLayout;
        TextUtils.TruncateAt mMethod;
        CharSequence mText;
        int mWidth;

        public Ellipsizer(CharSequence s) {
            this.mText = s;
        }

        public char charAt(int off) {
            char[] buf = TextUtils.obtain(1);
            getChars(off, off + 1, buf, 0);
            char ret = buf[0];
            TextUtils.recycle(buf);
            return ret;
        }

        @Override // android.text.GetChars
        public void getChars(int start, int end, char[] dest, int destoff) {
            int line1 = this.mLayout.getLineForOffset(start);
            int line2 = this.mLayout.getLineForOffset(end);
            TextUtils.getChars(this.mText, start, end, dest, destoff);
            for (int i = line1; i <= line2; i++) {
                this.mLayout.ellipsize(start, end, i, dest, destoff, this.mMethod);
            }
        }

        public int length() {
            return this.mText.length();
        }

        public CharSequence subSequence(int start, int end) {
            char[] s = new char[(end - start)];
            getChars(start, end, s, 0);
            return new String(s);
        }

        public String toString() {
            char[] s = new char[length()];
            getChars(0, length(), s, 0);
            return new String(s);
        }
    }

    static class SpannedEllipsizer extends Ellipsizer implements Spanned {
        private Spanned mSpanned;

        public SpannedEllipsizer(CharSequence display) {
            super(display);
            this.mSpanned = (Spanned) display;
        }

        @Override // android.text.Spanned
        public <T> T[] getSpans(int start, int end, Class<T> type) {
            return (T[]) this.mSpanned.getSpans(start, end, type);
        }

        @Override // android.text.Spanned
        public int getSpanStart(Object tag) {
            return this.mSpanned.getSpanStart(tag);
        }

        @Override // android.text.Spanned
        public int getSpanEnd(Object tag) {
            return this.mSpanned.getSpanEnd(tag);
        }

        @Override // android.text.Spanned
        public int getSpanFlags(Object tag) {
            return this.mSpanned.getSpanFlags(tag);
        }

        @Override // android.text.Spanned
        public int nextSpanTransition(int start, int limit, Class type) {
            return this.mSpanned.nextSpanTransition(start, limit, type);
        }

        @Override // android.text.Layout.Ellipsizer
        public CharSequence subSequence(int start, int end) {
            char[] s = new char[(end - start)];
            getChars(start, end, s, 0);
            SpannableString ss = new SpannableString(new String(s));
            TextUtils.copySpansFrom(this.mSpanned, start, end, Object.class, ss, 0);
            return ss;
        }
    }
}

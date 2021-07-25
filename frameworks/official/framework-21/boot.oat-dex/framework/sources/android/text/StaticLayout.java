package android.text;

import android.graphics.Paint;
import android.text.Layout;
import android.text.TextUtils;
import android.text.style.LineHeightSpan;
import android.util.Log;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.GrowingArrayUtils;

public class StaticLayout extends Layout {
    private static final int CHAR_FIRST_HIGH_SURROGATE = 55296;
    private static final int CHAR_LAST_LOW_SURROGATE = 57343;
    private static final char CHAR_NEW_LINE = '\n';
    private static final char CHAR_SPACE = ' ';
    private static final char CHAR_TAB = '\t';
    private static final char CHAR_ZWSP = 8203;
    private static final int COLUMNS_ELLIPSIZE = 5;
    private static final int COLUMNS_NORMAL = 3;
    private static final int DESCENT = 2;
    private static final int DIR = 0;
    private static final int DIR_SHIFT = 30;
    private static final int ELLIPSIS_COUNT = 4;
    private static final int ELLIPSIS_START = 3;
    private static final double EXTRA_ROUNDING = 0.5d;
    private static final int START = 0;
    private static final int START_MASK = 536870911;
    private static final int TAB = 0;
    private static final int TAB_INCREMENT = 20;
    private static final int TAB_MASK = 536870912;
    static final String TAG = "StaticLayout";
    private static final int TOP = 1;
    private int mBottomPadding;
    private int mColumns;
    private int mEllipsizedWidth;
    private Paint.FontMetricsInt mFontMetricsInt;
    private int mLineCount;
    private Layout.Directions[] mLineDirections;
    private int[] mLines;
    private int mMaximumVisibleLineCount;
    private MeasuredText mMeasured;
    private int mTopPadding;

    private static native int[] nLineBreakOpportunities(String str, char[] cArr, int i, int[] iArr);

    public StaticLayout(CharSequence source, TextPaint paint, int width, Layout.Alignment align, float spacingmult, float spacingadd, boolean includepad) {
        this(source, 0, source.length(), paint, width, align, spacingmult, spacingadd, includepad);
    }

    public StaticLayout(CharSequence source, TextPaint paint, int width, Layout.Alignment align, TextDirectionHeuristic textDir, float spacingmult, float spacingadd, boolean includepad) {
        this(source, 0, source.length(), paint, width, align, textDir, spacingmult, spacingadd, includepad);
    }

    public StaticLayout(CharSequence source, int bufstart, int bufend, TextPaint paint, int outerwidth, Layout.Alignment align, float spacingmult, float spacingadd, boolean includepad) {
        this(source, bufstart, bufend, paint, outerwidth, align, spacingmult, spacingadd, includepad, null, 0);
    }

    public StaticLayout(CharSequence source, int bufstart, int bufend, TextPaint paint, int outerwidth, Layout.Alignment align, TextDirectionHeuristic textDir, float spacingmult, float spacingadd, boolean includepad) {
        this(source, bufstart, bufend, paint, outerwidth, align, textDir, spacingmult, spacingadd, includepad, null, 0, Integer.MAX_VALUE);
    }

    public StaticLayout(CharSequence source, int bufstart, int bufend, TextPaint paint, int outerwidth, Layout.Alignment align, float spacingmult, float spacingadd, boolean includepad, TextUtils.TruncateAt ellipsize, int ellipsizedWidth) {
        this(source, bufstart, bufend, paint, outerwidth, align, TextDirectionHeuristics.FIRSTSTRONG_LTR, spacingmult, spacingadd, includepad, ellipsize, ellipsizedWidth, Integer.MAX_VALUE);
    }

    /* JADX WARNING: Illegal instructions before constructor call */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public StaticLayout(java.lang.CharSequence r16, int r17, int r18, android.text.TextPaint r19, int r20, android.text.Layout.Alignment r21, android.text.TextDirectionHeuristic r22, float r23, float r24, boolean r25, android.text.TextUtils.TruncateAt r26, int r27, int r28) {
        /*
        // Method dump skipped, instructions count: 161
        */
        throw new UnsupportedOperationException("Method not decompiled: android.text.StaticLayout.<init>(java.lang.CharSequence, int, int, android.text.TextPaint, int, android.text.Layout$Alignment, android.text.TextDirectionHeuristic, float, float, boolean, android.text.TextUtils$TruncateAt, int, int):void");
    }

    StaticLayout(CharSequence text) {
        super(text, null, 0, null, 0.0f, 0.0f);
        this.mMaximumVisibleLineCount = Integer.MAX_VALUE;
        this.mFontMetricsInt = new Paint.FontMetricsInt();
        this.mColumns = 5;
        this.mLineDirections = (Layout.Directions[]) ArrayUtils.newUnpaddedArray(Layout.Directions.class, this.mColumns * 2);
        this.mLines = new int[this.mLineDirections.length];
        this.mMeasured = MeasuredText.obtain();
    }

    /*  JADX ERROR: IndexOutOfBoundsException in pass: SSATransform
        java.lang.IndexOutOfBoundsException: bitIndex < 0: -127
        	at java.util.BitSet.get(BitSet.java:623)
        	at jadx.core.dex.visitors.ssa.LiveVarAnalysis.fillBasicBlockInfo(LiveVarAnalysis.java:65)
        	at jadx.core.dex.visitors.ssa.LiveVarAnalysis.runAnalysis(LiveVarAnalysis.java:36)
        	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:50)
        	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:41)
        */
    void generate(java.lang.CharSequence r136, int r137, int r138, android.text.TextPaint r139, int r140, android.text.TextDirectionHeuristic r141, float r142, float r143, boolean r144, boolean r145, float r146, android.text.TextUtils.TruncateAt r147) {
        /*
        // Method dump skipped, instructions count: 1256
        */
        throw new UnsupportedOperationException("Method not decompiled: android.text.StaticLayout.generate(java.lang.CharSequence, int, int, android.text.TextPaint, int, android.text.TextDirectionHeuristic, float, float, boolean, boolean, float, android.text.TextUtils$TruncateAt):void");
    }

    private int out(CharSequence text, int start, int end, int above, int below, int top, int bottom, int v, float spacingmult, float spacingadd, LineHeightSpan[] chooseHt, int[] chooseHtv, Paint.FontMetricsInt fm, boolean hasTabOrEmoji, boolean needMultiply, byte[] chdirs, int dir, boolean easy, int bufEnd, boolean includePad, boolean trackPad, char[] chs, float[] widths, int widthStart, TextUtils.TruncateAt ellipsize, float ellipsisWidth, float textWidth, TextPaint paint, boolean moreChars) {
        int extra;
        int j = this.mLineCount;
        int off = j * this.mColumns;
        int want = this.mColumns + off + 1;
        int[] lines = this.mLines;
        if (want >= lines.length) {
            Layout.Directions[] grow2 = (Layout.Directions[]) ArrayUtils.newUnpaddedArray(Layout.Directions.class, GrowingArrayUtils.growSize(want));
            System.arraycopy(this.mLineDirections, 0, grow2, 0, this.mLineDirections.length);
            this.mLineDirections = grow2;
            int[] grow = new int[grow2.length];
            System.arraycopy(lines, 0, grow, 0, lines.length);
            this.mLines = grow;
            lines = grow;
        }
        if (chooseHt != null) {
            fm.ascent = above;
            fm.descent = below;
            fm.top = top;
            fm.bottom = bottom;
            for (int i = 0; i < chooseHt.length; i++) {
                if (chooseHt[i] instanceof LineHeightSpan.WithDensity) {
                    ((LineHeightSpan.WithDensity) chooseHt[i]).chooseHeight(text, start, end, chooseHtv[i], v, fm, paint);
                } else {
                    chooseHt[i].chooseHeight(text, start, end, chooseHtv[i], v, fm);
                }
            }
            above = fm.ascent;
            below = fm.descent;
            top = fm.top;
            bottom = fm.bottom;
        }
        boolean firstLine = j == 0;
        boolean currentLineIsTheLastVisibleOne = j + 1 == this.mMaximumVisibleLineCount;
        boolean lastLine = currentLineIsTheLastVisibleOne || end == bufEnd;
        if (firstLine) {
            if (trackPad) {
                this.mTopPadding = top - above;
            }
            if (includePad) {
                above = top;
            }
        }
        if (lastLine) {
            if (trackPad) {
                this.mBottomPadding = bottom - below;
            }
            if (includePad) {
                below = bottom;
            }
        }
        if (!needMultiply || lastLine) {
            extra = 0;
        } else {
            double ex = (double) ((((float) (below - above)) * (spacingmult - 1.0f)) + spacingadd);
            if (ex >= 0.0d) {
                extra = (int) (EXTRA_ROUNDING + ex);
            } else {
                extra = -((int) ((-ex) + EXTRA_ROUNDING));
            }
        }
        lines[off + 0] = start;
        lines[off + 1] = v;
        lines[off + 2] = below + extra;
        int v2 = v + (below - above) + extra;
        lines[this.mColumns + off + 0] = end;
        lines[this.mColumns + off + 1] = v2;
        if (hasTabOrEmoji) {
            int i2 = off + 0;
            lines[i2] = lines[i2] | 536870912;
        }
        int i3 = off + 0;
        lines[i3] = lines[i3] | (dir << 30);
        Layout.Directions linedirs = DIRS_ALL_LEFT_TO_RIGHT;
        if (easy) {
            this.mLineDirections[j] = linedirs;
        } else {
            this.mLineDirections[j] = AndroidBidi.directions(dir, chdirs, start - widthStart, chs, start - widthStart, end - start);
        }
        if (ellipsize != null) {
            boolean forceEllipsis = moreChars && this.mLineCount + 1 == this.mMaximumVisibleLineCount;
            if ((((this.mMaximumVisibleLineCount == 1 && moreChars) || (firstLine && !moreChars)) && ellipsize != TextUtils.TruncateAt.MARQUEE) || (!firstLine && ((currentLineIsTheLastVisibleOne || !moreChars) && ellipsize == TextUtils.TruncateAt.END))) {
                calculateEllipsis(start, end, widths, widthStart, ellipsisWidth, ellipsize, j, textWidth, paint, forceEllipsis);
            }
        }
        this.mLineCount++;
        return v2;
    }

    private void calculateEllipsis(int lineStart, int lineEnd, float[] widths, int widthStart, float avail, TextUtils.TruncateAt where, int line, float textWidth, TextPaint paint, boolean forceEllipsis) {
        if (textWidth > avail || forceEllipsis) {
            float ellipsisWidth = paint.measureText(where == TextUtils.TruncateAt.END_SMALL ? ELLIPSIS_TWO_DOTS : ELLIPSIS_NORMAL, 0, 1);
            int ellipsisStart = 0;
            int ellipsisCount = 0;
            int len = lineEnd - lineStart;
            if (where == TextUtils.TruncateAt.START) {
                if (this.mMaximumVisibleLineCount == 1) {
                    float sum = 0.0f;
                    int i = len;
                    while (i >= 0) {
                        float w = widths[((i - 1) + lineStart) - widthStart];
                        if (w + sum + ellipsisWidth > avail) {
                            break;
                        }
                        sum += w;
                        i--;
                    }
                    ellipsisStart = 0;
                    ellipsisCount = i;
                } else if (Log.isLoggable(TAG, 5)) {
                    Log.w(TAG, "Start Ellipsis only supported with one line");
                }
            } else if (where == TextUtils.TruncateAt.END || where == TextUtils.TruncateAt.MARQUEE || where == TextUtils.TruncateAt.END_SMALL) {
                float sum2 = 0.0f;
                int i2 = 0;
                while (i2 < len) {
                    float w2 = widths[(i2 + lineStart) - widthStart];
                    if (w2 + sum2 + ellipsisWidth > avail) {
                        break;
                    }
                    sum2 += w2;
                    i2++;
                }
                ellipsisStart = i2;
                ellipsisCount = len - i2;
                if (forceEllipsis && ellipsisCount == 0 && len > 0) {
                    ellipsisStart = len - 1;
                    ellipsisCount = 1;
                }
            } else if (this.mMaximumVisibleLineCount == 1) {
                float lsum = 0.0f;
                float rsum = 0.0f;
                float ravail = (avail - ellipsisWidth) / 2.0f;
                int right = len;
                while (right >= 0) {
                    float w3 = widths[((right - 1) + lineStart) - widthStart];
                    if (w3 + rsum > ravail) {
                        break;
                    }
                    rsum += w3;
                    right--;
                }
                float lavail = (avail - ellipsisWidth) - rsum;
                int left = 0;
                while (left < right) {
                    float w4 = widths[(left + lineStart) - widthStart];
                    if (w4 + lsum > lavail) {
                        break;
                    }
                    lsum += w4;
                    left++;
                }
                ellipsisStart = left;
                ellipsisCount = right - left;
            } else if (Log.isLoggable(TAG, 5)) {
                Log.w(TAG, "Middle Ellipsis only supported with one line");
            }
            this.mLines[(this.mColumns * line) + 3] = ellipsisStart;
            this.mLines[(this.mColumns * line) + 4] = ellipsisCount;
            return;
        }
        this.mLines[(this.mColumns * line) + 3] = 0;
        this.mLines[(this.mColumns * line) + 4] = 0;
    }

    @Override // android.text.Layout
    public int getLineForVertical(int vertical) {
        int high = this.mLineCount;
        int low = -1;
        int[] lines = this.mLines;
        while (high - low > 1) {
            int guess = (high + low) >> 1;
            if (lines[(this.mColumns * guess) + 1] > vertical) {
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

    @Override // android.text.Layout
    public int getLineCount() {
        return this.mLineCount;
    }

    @Override // android.text.Layout
    public int getLineTop(int line) {
        int top = this.mLines[(this.mColumns * line) + 1];
        if (this.mMaximumVisibleLineCount <= 0 || line < this.mMaximumVisibleLineCount || line == this.mLineCount) {
            return top;
        }
        return top + getBottomPadding();
    }

    @Override // android.text.Layout
    public int getLineDescent(int line) {
        int descent = this.mLines[(this.mColumns * line) + 2];
        if (this.mMaximumVisibleLineCount <= 0 || line < this.mMaximumVisibleLineCount - 1 || line == this.mLineCount) {
            return descent;
        }
        return descent + getBottomPadding();
    }

    @Override // android.text.Layout
    public int getLineStart(int line) {
        return this.mLines[(this.mColumns * line) + 0] & 536870911;
    }

    @Override // android.text.Layout
    public int getParagraphDirection(int line) {
        return this.mLines[(this.mColumns * line) + 0] >> 30;
    }

    @Override // android.text.Layout
    public boolean getLineContainsTab(int line) {
        return (this.mLines[(this.mColumns * line) + 0] & 536870912) != 0;
    }

    @Override // android.text.Layout
    public final Layout.Directions getLineDirections(int line) {
        return this.mLineDirections[line];
    }

    @Override // android.text.Layout
    public int getTopPadding() {
        return this.mTopPadding;
    }

    @Override // android.text.Layout
    public int getBottomPadding() {
        return this.mBottomPadding;
    }

    @Override // android.text.Layout
    public int getEllipsisCount(int line) {
        if (this.mColumns < 5) {
            return 0;
        }
        return this.mLines[(this.mColumns * line) + 4];
    }

    @Override // android.text.Layout
    public int getEllipsisStart(int line) {
        if (this.mColumns < 5) {
            return 0;
        }
        return this.mLines[(this.mColumns * line) + 3];
    }

    @Override // android.text.Layout
    public int getEllipsizedWidth() {
        return this.mEllipsizedWidth;
    }

    /* access modifiers changed from: package-private */
    public void prepare() {
        this.mMeasured = MeasuredText.obtain();
    }

    /* access modifiers changed from: package-private */
    public void finish() {
        this.mMeasured = MeasuredText.recycle(this.mMeasured);
    }
}

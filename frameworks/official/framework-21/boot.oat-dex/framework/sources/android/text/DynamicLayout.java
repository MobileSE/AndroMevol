package android.text;

import android.text.Layout;
import android.text.TextUtils;
import android.text.style.UpdateLayout;
import android.text.style.WrapTogetherSpan;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.GrowingArrayUtils;
import java.lang.ref.WeakReference;

public class DynamicLayout extends Layout {
    private static final int BLOCK_MINIMUM_CHARACTER_LENGTH = 400;
    private static final int COLUMNS_ELLIPSIZE = 5;
    private static final int COLUMNS_NORMAL = 3;
    private static final int DESCENT = 2;
    private static final int DIR = 0;
    private static final int DIR_SHIFT = 30;
    private static final int ELLIPSIS_COUNT = 4;
    private static final int ELLIPSIS_START = 3;
    private static final int ELLIPSIS_UNDEFINED = Integer.MIN_VALUE;
    public static final int INVALID_BLOCK_INDEX = -1;
    private static final int PRIORITY = 128;
    private static final int START = 0;
    private static final int START_MASK = 536870911;
    private static final int TAB = 0;
    private static final int TAB_MASK = 536870912;
    private static final int TOP = 1;
    private static final Object[] sLock = new Object[0];
    private static StaticLayout sStaticLayout = new StaticLayout(null);
    private CharSequence mBase;
    private int[] mBlockEndLines;
    private int[] mBlockIndices;
    private int mBottomPadding;
    private CharSequence mDisplay;
    private boolean mEllipsize;
    private TextUtils.TruncateAt mEllipsizeAt;
    private int mEllipsizedWidth;
    private boolean mIncludePad;
    private int mIndexFirstChangedBlock;
    private PackedIntVector mInts;
    private int mNumberOfBlocks;
    private PackedObjectVector<Layout.Directions> mObjects;
    private int mTopPadding;
    private ChangeWatcher mWatcher;

    public DynamicLayout(CharSequence base, TextPaint paint, int width, Layout.Alignment align, float spacingmult, float spacingadd, boolean includepad) {
        this(base, base, paint, width, align, spacingmult, spacingadd, includepad);
    }

    public DynamicLayout(CharSequence base, CharSequence display, TextPaint paint, int width, Layout.Alignment align, float spacingmult, float spacingadd, boolean includepad) {
        this(base, display, paint, width, align, spacingmult, spacingadd, includepad, null, 0);
    }

    public DynamicLayout(CharSequence base, CharSequence display, TextPaint paint, int width, Layout.Alignment align, float spacingmult, float spacingadd, boolean includepad, TextUtils.TruncateAt ellipsize, int ellipsizedWidth) {
        this(base, display, paint, width, align, TextDirectionHeuristics.FIRSTSTRONG_LTR, spacingmult, spacingadd, includepad, ellipsize, ellipsizedWidth);
    }

    /* JADX WARNING: Illegal instructions before constructor call */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public DynamicLayout(java.lang.CharSequence r20, java.lang.CharSequence r21, android.text.TextPaint r22, int r23, android.text.Layout.Alignment r24, android.text.TextDirectionHeuristic r25, float r26, float r27, boolean r28, android.text.TextUtils.TruncateAt r29, int r30) {
        /*
        // Method dump skipped, instructions count: 314
        */
        throw new UnsupportedOperationException("Method not decompiled: android.text.DynamicLayout.<init>(java.lang.CharSequence, java.lang.CharSequence, android.text.TextPaint, int, android.text.Layout$Alignment, android.text.TextDirectionHeuristic, float, float, boolean, android.text.TextUtils$TruncateAt, int):void");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reflow(CharSequence s, int where, int before, int after) {
        int find;
        int look;
        StaticLayout reflowed;
        int[] ints;
        boolean again;
        if (s == this.mBase) {
            CharSequence text = this.mDisplay;
            int len = text.length();
            int find2 = TextUtils.lastIndexOf(text, '\n', where - 1);
            if (find2 < 0) {
                find = 0;
            } else {
                find = find2 + 1;
            }
            int diff = where - find;
            int before2 = before + diff;
            int after2 = after + diff;
            int where2 = where - diff;
            int look2 = TextUtils.indexOf(text, '\n', where2 + after2);
            if (look2 < 0) {
                look = len;
            } else {
                look = look2 + 1;
            }
            int change = look - (where2 + after2);
            int before3 = before2 + change;
            int after3 = after2 + change;
            if (text instanceof Spanned) {
                Spanned sp = (Spanned) text;
                do {
                    again = false;
                    Object[] force = sp.getSpans(where2, where2 + after3, WrapTogetherSpan.class);
                    for (int i = 0; i < force.length; i++) {
                        int st = sp.getSpanStart(force[i]);
                        int en = sp.getSpanEnd(force[i]);
                        if (st < where2) {
                            again = true;
                            int diff2 = where2 - st;
                            before3 += diff2;
                            after3 += diff2;
                            where2 -= diff2;
                        }
                        if (en > where2 + after3) {
                            again = true;
                            int diff3 = en - (where2 + after3);
                            before3 += diff3;
                            after3 += diff3;
                        }
                    }
                } while (again);
            }
            int startline = getLineForOffset(where2);
            int startv = getLineTop(startline);
            int endline = getLineForOffset(where2 + before3);
            if (where2 + after3 == len) {
                endline = getLineCount();
            }
            int endv = getLineTop(endline);
            boolean islast = endline == getLineCount();
            synchronized (sLock) {
                reflowed = sStaticLayout;
                sStaticLayout = null;
            }
            if (reflowed == null) {
                reflowed = new StaticLayout(null);
            } else {
                reflowed.prepare();
            }
            reflowed.generate(text, where2, where2 + after3, getPaint(), getWidth(), getTextDirectionHeuristic(), getSpacingMultiplier(), getSpacingAdd(), false, true, (float) this.mEllipsizedWidth, this.mEllipsizeAt);
            int n = reflowed.getLineCount();
            if (where2 + after3 != len && reflowed.getLineStart(n - 1) == where2 + after3) {
                n--;
            }
            this.mInts.deleteAt(startline, endline - startline);
            this.mObjects.deleteAt(startline, endline - startline);
            int ht = reflowed.getLineTop(n);
            int toppad = 0;
            int botpad = 0;
            if (this.mIncludePad && startline == 0) {
                toppad = reflowed.getTopPadding();
                this.mTopPadding = toppad;
                ht -= toppad;
            }
            if (this.mIncludePad && islast) {
                botpad = reflowed.getBottomPadding();
                this.mBottomPadding = botpad;
                ht += botpad;
            }
            this.mInts.adjustValuesBelow(startline, 0, after3 - before3);
            this.mInts.adjustValuesBelow(startline, 1, (startv - endv) + ht);
            if (this.mEllipsize) {
                ints = new int[5];
                ints[3] = Integer.MIN_VALUE;
            } else {
                ints = new int[3];
            }
            Layout.Directions[] objects = new Layout.Directions[1];
            for (int i2 = 0; i2 < n; i2++) {
                ints[0] = (reflowed.getLineContainsTab(i2) ? 536870912 : 0) | (reflowed.getParagraphDirection(i2) << 30) | reflowed.getLineStart(i2);
                int top = reflowed.getLineTop(i2) + startv;
                if (i2 > 0) {
                    top -= toppad;
                }
                ints[1] = top;
                int desc = reflowed.getLineDescent(i2);
                if (i2 == n - 1) {
                    desc += botpad;
                }
                ints[2] = desc;
                objects[0] = reflowed.getLineDirections(i2);
                if (this.mEllipsize) {
                    ints[3] = reflowed.getEllipsisStart(i2);
                    ints[4] = reflowed.getEllipsisCount(i2);
                }
                this.mInts.insertAt(startline + i2, ints);
                this.mObjects.insertAt(startline + i2, objects);
            }
            updateBlocks(startline, endline - 1, n);
            synchronized (sLock) {
                sStaticLayout = reflowed;
                reflowed.finish();
            }
        }
    }

    private void createBlocks() {
        int offset = 400;
        this.mNumberOfBlocks = 0;
        CharSequence text = this.mDisplay;
        while (true) {
            int offset2 = TextUtils.indexOf(text, '\n', offset);
            if (offset2 < 0) {
                break;
            }
            addBlockAtOffset(offset2);
            offset = offset2 + 400;
        }
        addBlockAtOffset(text.length());
        this.mBlockIndices = new int[this.mBlockEndLines.length];
        for (int i = 0; i < this.mBlockEndLines.length; i++) {
            this.mBlockIndices[i] = -1;
        }
    }

    private void addBlockAtOffset(int offset) {
        int line = getLineForOffset(offset);
        if (this.mBlockEndLines == null) {
            this.mBlockEndLines = ArrayUtils.newUnpaddedIntArray(1);
            this.mBlockEndLines[this.mNumberOfBlocks] = line;
            this.mNumberOfBlocks++;
        } else if (line > this.mBlockEndLines[this.mNumberOfBlocks - 1]) {
            this.mBlockEndLines = GrowingArrayUtils.append(this.mBlockEndLines, this.mNumberOfBlocks, line);
            this.mNumberOfBlocks++;
        }
    }

    /* access modifiers changed from: package-private */
    public void updateBlocks(int startLine, int endLine, int newLineCount) {
        int newFirstChangedBlock;
        if (this.mBlockEndLines == null) {
            createBlocks();
            return;
        }
        int firstBlock = -1;
        int lastBlock = -1;
        int i = 0;
        while (true) {
            if (i >= this.mNumberOfBlocks) {
                break;
            } else if (this.mBlockEndLines[i] >= startLine) {
                firstBlock = i;
                break;
            } else {
                i++;
            }
        }
        int i2 = firstBlock;
        while (true) {
            if (i2 >= this.mNumberOfBlocks) {
                break;
            } else if (this.mBlockEndLines[i2] >= endLine) {
                lastBlock = i2;
                break;
            } else {
                i2++;
            }
        }
        int lastBlockEndLine = this.mBlockEndLines[lastBlock];
        boolean createBlockBefore = startLine > (firstBlock == 0 ? 0 : this.mBlockEndLines[firstBlock + -1] + 1);
        boolean createBlock = newLineCount > 0;
        boolean createBlockAfter = endLine < this.mBlockEndLines[lastBlock];
        int numAddedBlocks = 0;
        if (createBlockBefore) {
            numAddedBlocks = 0 + 1;
        }
        if (createBlock) {
            numAddedBlocks++;
        }
        if (createBlockAfter) {
            numAddedBlocks++;
        }
        int newNumberOfBlocks = (this.mNumberOfBlocks + numAddedBlocks) - ((lastBlock - firstBlock) + 1);
        if (newNumberOfBlocks == 0) {
            this.mBlockEndLines[0] = 0;
            this.mBlockIndices[0] = -1;
            this.mNumberOfBlocks = 1;
            return;
        }
        if (newNumberOfBlocks > this.mBlockEndLines.length) {
            int[] blockEndLines = ArrayUtils.newUnpaddedIntArray(Math.max(this.mBlockEndLines.length * 2, newNumberOfBlocks));
            int[] blockIndices = new int[blockEndLines.length];
            System.arraycopy(this.mBlockEndLines, 0, blockEndLines, 0, firstBlock);
            System.arraycopy(this.mBlockIndices, 0, blockIndices, 0, firstBlock);
            System.arraycopy(this.mBlockEndLines, lastBlock + 1, blockEndLines, firstBlock + numAddedBlocks, (this.mNumberOfBlocks - lastBlock) - 1);
            System.arraycopy(this.mBlockIndices, lastBlock + 1, blockIndices, firstBlock + numAddedBlocks, (this.mNumberOfBlocks - lastBlock) - 1);
            this.mBlockEndLines = blockEndLines;
            this.mBlockIndices = blockIndices;
        } else {
            System.arraycopy(this.mBlockEndLines, lastBlock + 1, this.mBlockEndLines, firstBlock + numAddedBlocks, (this.mNumberOfBlocks - lastBlock) - 1);
            System.arraycopy(this.mBlockIndices, lastBlock + 1, this.mBlockIndices, firstBlock + numAddedBlocks, (this.mNumberOfBlocks - lastBlock) - 1);
        }
        this.mNumberOfBlocks = newNumberOfBlocks;
        int deltaLines = newLineCount - ((endLine - startLine) + 1);
        if (deltaLines != 0) {
            newFirstChangedBlock = firstBlock + numAddedBlocks;
            for (int i3 = newFirstChangedBlock; i3 < this.mNumberOfBlocks; i3++) {
                int[] iArr = this.mBlockEndLines;
                iArr[i3] = iArr[i3] + deltaLines;
            }
        } else {
            newFirstChangedBlock = this.mNumberOfBlocks;
        }
        this.mIndexFirstChangedBlock = Math.min(this.mIndexFirstChangedBlock, newFirstChangedBlock);
        int blockIndex = firstBlock;
        if (createBlockBefore) {
            this.mBlockEndLines[blockIndex] = startLine - 1;
            this.mBlockIndices[blockIndex] = -1;
            blockIndex++;
        }
        if (createBlock) {
            this.mBlockEndLines[blockIndex] = (startLine + newLineCount) - 1;
            this.mBlockIndices[blockIndex] = -1;
            blockIndex++;
        }
        if (createBlockAfter) {
            this.mBlockEndLines[blockIndex] = lastBlockEndLine + deltaLines;
            this.mBlockIndices[blockIndex] = -1;
        }
    }

    /* access modifiers changed from: package-private */
    public void setBlocksDataForTest(int[] blockEndLines, int[] blockIndices, int numberOfBlocks) {
        this.mBlockEndLines = new int[blockEndLines.length];
        this.mBlockIndices = new int[blockIndices.length];
        System.arraycopy(blockEndLines, 0, this.mBlockEndLines, 0, blockEndLines.length);
        System.arraycopy(blockIndices, 0, this.mBlockIndices, 0, blockIndices.length);
        this.mNumberOfBlocks = numberOfBlocks;
    }

    public int[] getBlockEndLines() {
        return this.mBlockEndLines;
    }

    public int[] getBlockIndices() {
        return this.mBlockIndices;
    }

    public int getNumberOfBlocks() {
        return this.mNumberOfBlocks;
    }

    public int getIndexFirstChangedBlock() {
        return this.mIndexFirstChangedBlock;
    }

    public void setIndexFirstChangedBlock(int i) {
        this.mIndexFirstChangedBlock = i;
    }

    @Override // android.text.Layout
    public int getLineCount() {
        return this.mInts.size() - 1;
    }

    @Override // android.text.Layout
    public int getLineTop(int line) {
        return this.mInts.getValue(line, 1);
    }

    @Override // android.text.Layout
    public int getLineDescent(int line) {
        return this.mInts.getValue(line, 2);
    }

    @Override // android.text.Layout
    public int getLineStart(int line) {
        return this.mInts.getValue(line, 0) & 536870911;
    }

    @Override // android.text.Layout
    public boolean getLineContainsTab(int line) {
        return (this.mInts.getValue(line, 0) & 536870912) != 0;
    }

    @Override // android.text.Layout
    public int getParagraphDirection(int line) {
        return this.mInts.getValue(line, 0) >> 30;
    }

    @Override // android.text.Layout
    public final Layout.Directions getLineDirections(int line) {
        return this.mObjects.getValue(line, 0);
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
    public int getEllipsizedWidth() {
        return this.mEllipsizedWidth;
    }

    private static class ChangeWatcher implements TextWatcher, SpanWatcher {
        private WeakReference<DynamicLayout> mLayout;

        public ChangeWatcher(DynamicLayout layout) {
            this.mLayout = new WeakReference<>(layout);
        }

        private void reflow(CharSequence s, int where, int before, int after) {
            DynamicLayout ml = this.mLayout.get();
            if (ml != null) {
                ml.reflow(s, where, before, after);
            } else if (s instanceof Spannable) {
                ((Spannable) s).removeSpan(this);
            }
        }

        @Override // android.text.TextWatcher
        public void beforeTextChanged(CharSequence s, int where, int before, int after) {
        }

        @Override // android.text.TextWatcher
        public void onTextChanged(CharSequence s, int where, int before, int after) {
            reflow(s, where, before, after);
        }

        @Override // android.text.TextWatcher
        public void afterTextChanged(Editable s) {
        }

        @Override // android.text.SpanWatcher
        public void onSpanAdded(Spannable s, Object o, int start, int end) {
            if (o instanceof UpdateLayout) {
                reflow(s, start, end - start, end - start);
            }
        }

        @Override // android.text.SpanWatcher
        public void onSpanRemoved(Spannable s, Object o, int start, int end) {
            if (o instanceof UpdateLayout) {
                reflow(s, start, end - start, end - start);
            }
        }

        @Override // android.text.SpanWatcher
        public void onSpanChanged(Spannable s, Object o, int start, int end, int nstart, int nend) {
            if (o instanceof UpdateLayout) {
                reflow(s, start, end - start, end - start);
                reflow(s, nstart, nend - nstart, nend - nstart);
            }
        }
    }

    @Override // android.text.Layout
    public int getEllipsisStart(int line) {
        if (this.mEllipsizeAt == null) {
            return 0;
        }
        return this.mInts.getValue(line, 3);
    }

    @Override // android.text.Layout
    public int getEllipsisCount(int line) {
        if (this.mEllipsizeAt == null) {
            return 0;
        }
        return this.mInts.getValue(line, 4);
    }
}

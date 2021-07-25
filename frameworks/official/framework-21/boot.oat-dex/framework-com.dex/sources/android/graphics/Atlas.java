package android.graphics;

public class Atlas {
    public static final int FLAG_ADD_PADDING = 2;
    public static final int FLAG_ALLOW_ROTATIONS = 1;
    public static final int FLAG_DEFAULTS = 2;
    private final Policy mPolicy;

    public static class Entry {
        public boolean rotated;
        public int x;
        public int y;
    }

    public enum Type {
        SliceMinArea,
        SliceMaxArea,
        SliceShortAxis,
        SliceLongAxis
    }

    public Atlas(Type type, int width, int height) {
        this(type, width, height, 2);
    }

    public Atlas(Type type, int width, int height, int flags) {
        this.mPolicy = findPolicy(type, width, height, flags);
    }

    public Entry pack(int width, int height) {
        return pack(width, height, null);
    }

    public Entry pack(int width, int height, Entry entry) {
        if (entry == null) {
            entry = new Entry();
        }
        return this.mPolicy.pack(width, height, entry);
    }

    /* access modifiers changed from: package-private */
    /* renamed from: android.graphics.Atlas$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$android$graphics$Atlas$Type = new int[Type.values().length];

        static {
            try {
                $SwitchMap$android$graphics$Atlas$Type[Type.SliceMinArea.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$android$graphics$Atlas$Type[Type.SliceMaxArea.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$android$graphics$Atlas$Type[Type.SliceShortAxis.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$android$graphics$Atlas$Type[Type.SliceLongAxis.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    private static Policy findPolicy(Type type, int width, int height, int flags) {
        switch (AnonymousClass1.$SwitchMap$android$graphics$Atlas$Type[type.ordinal()]) {
            case 1:
                return new SlicePolicy(width, height, flags, new SlicePolicy.MinAreaSplitDecision(null));
            case 2:
                return new SlicePolicy(width, height, flags, new SlicePolicy.MaxAreaSplitDecision(null));
            case 3:
                return new SlicePolicy(width, height, flags, new SlicePolicy.ShorterFreeAxisSplitDecision(null));
            case 4:
                return new SlicePolicy(width, height, flags, new SlicePolicy.LongerFreeAxisSplitDecision(null));
            default:
                return null;
        }
    }

    /* access modifiers changed from: private */
    public static abstract class Policy {
        /* access modifiers changed from: package-private */
        public abstract Entry pack(int i, int i2, Entry entry);

        private Policy() {
        }

        /* synthetic */ Policy(AnonymousClass1 x0) {
            this();
        }
    }

    /* access modifiers changed from: private */
    public static class SlicePolicy extends Policy {
        private final boolean mAllowRotation;
        private final int mPadding;
        private final Cell mRoot = new Cell(null);
        private final SplitDecision mSplitDecision;

        /* access modifiers changed from: private */
        public interface SplitDecision {
            boolean splitHorizontal(int i, int i2, int i3, int i4);
        }

        /* access modifiers changed from: private */
        public static class Cell {
            int height;
            Cell next;
            int width;
            int x;
            int y;

            private Cell() {
            }

            /* synthetic */ Cell(AnonymousClass1 x0) {
                this();
            }

            public String toString() {
                return String.format("cell[x=%d y=%d width=%d height=%d", Integer.valueOf(this.x), Integer.valueOf(this.y), Integer.valueOf(this.width), Integer.valueOf(this.height));
            }
        }

        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        SlicePolicy(int width, int height, int flags, SplitDecision splitDecision) {
            super(null);
            boolean z;
            int i = 1;
            if ((flags & 1) != 0) {
                z = true;
            } else {
                z = false;
            }
            this.mAllowRotation = z;
            this.mPadding = (flags & 2) == 0 ? 0 : i;
            Cell first = new Cell(null);
            int i2 = this.mPadding;
            first.y = i2;
            first.x = i2;
            first.width = width - (this.mPadding * 2);
            first.height = height - (this.mPadding * 2);
            this.mRoot.next = first;
            this.mSplitDecision = splitDecision;
        }

        /* access modifiers changed from: package-private */
        @Override // android.graphics.Atlas.Policy
        public Entry pack(int width, int height, Entry entry) {
            Cell prev = this.mRoot;
            for (Cell cell = this.mRoot.next; cell != null; cell = cell.next) {
                if (insert(cell, prev, width, height, entry)) {
                    return entry;
                }
                prev = cell;
            }
            return null;
        }

        /* access modifiers changed from: private */
        public static class MinAreaSplitDecision implements SplitDecision {
            private MinAreaSplitDecision() {
            }

            /* synthetic */ MinAreaSplitDecision(AnonymousClass1 x0) {
                this();
            }

            @Override // android.graphics.Atlas.SlicePolicy.SplitDecision
            public boolean splitHorizontal(int freeWidth, int freeHeight, int rectWidth, int rectHeight) {
                return rectWidth * freeHeight > freeWidth * rectHeight;
            }
        }

        /* access modifiers changed from: private */
        public static class MaxAreaSplitDecision implements SplitDecision {
            private MaxAreaSplitDecision() {
            }

            /* synthetic */ MaxAreaSplitDecision(AnonymousClass1 x0) {
                this();
            }

            @Override // android.graphics.Atlas.SlicePolicy.SplitDecision
            public boolean splitHorizontal(int freeWidth, int freeHeight, int rectWidth, int rectHeight) {
                return rectWidth * freeHeight <= freeWidth * rectHeight;
            }
        }

        /* access modifiers changed from: private */
        public static class ShorterFreeAxisSplitDecision implements SplitDecision {
            private ShorterFreeAxisSplitDecision() {
            }

            /* synthetic */ ShorterFreeAxisSplitDecision(AnonymousClass1 x0) {
                this();
            }

            @Override // android.graphics.Atlas.SlicePolicy.SplitDecision
            public boolean splitHorizontal(int freeWidth, int freeHeight, int rectWidth, int rectHeight) {
                return freeWidth <= freeHeight;
            }
        }

        /* access modifiers changed from: private */
        public static class LongerFreeAxisSplitDecision implements SplitDecision {
            private LongerFreeAxisSplitDecision() {
            }

            /* synthetic */ LongerFreeAxisSplitDecision(AnonymousClass1 x0) {
                this();
            }

            @Override // android.graphics.Atlas.SlicePolicy.SplitDecision
            public boolean splitHorizontal(int freeWidth, int freeHeight, int rectWidth, int rectHeight) {
                return freeWidth > freeHeight;
            }
        }

        private boolean insert(Cell cell, Cell prev, int width, int height, Entry entry) {
            boolean rotated = false;
            if (cell.width < width || cell.height < height) {
                if (!this.mAllowRotation || cell.width < height || cell.height < width) {
                    return false;
                }
                width = height;
                height = width;
                rotated = true;
            }
            int deltaWidth = cell.width - width;
            int deltaHeight = cell.height - height;
            Cell first = new Cell(null);
            Cell second = new Cell(null);
            first.x = cell.x + width + this.mPadding;
            first.y = cell.y;
            first.width = deltaWidth - this.mPadding;
            second.x = cell.x;
            second.y = cell.y + height + this.mPadding;
            second.height = deltaHeight - this.mPadding;
            if (this.mSplitDecision.splitHorizontal(deltaWidth, deltaHeight, width, height)) {
                first.height = height;
                second.width = cell.width;
            } else {
                first.height = cell.height;
                second.width = width;
                first = second;
                second = first;
            }
            if (first.width > 0 && first.height > 0) {
                prev.next = first;
                prev = first;
            }
            if (second.width <= 0 || second.height <= 0) {
                prev.next = cell.next;
            } else {
                prev.next = second;
                second.next = cell.next;
            }
            cell.next = null;
            entry.x = cell.x;
            entry.y = cell.y;
            entry.rotated = rotated;
            return true;
        }
    }
}

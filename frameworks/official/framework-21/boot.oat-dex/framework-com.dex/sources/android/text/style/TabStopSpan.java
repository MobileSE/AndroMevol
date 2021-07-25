package android.text.style;

public interface TabStopSpan extends ParagraphStyle {
    int getTabStop();

    public static class Standard implements TabStopSpan {
        private int mTab;

        public Standard(int where) {
            this.mTab = where;
        }

        @Override // android.text.style.TabStopSpan
        public int getTabStop() {
            return this.mTab;
        }
    }
}

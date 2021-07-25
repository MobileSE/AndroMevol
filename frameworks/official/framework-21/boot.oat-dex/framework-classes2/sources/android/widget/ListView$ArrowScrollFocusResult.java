package android.widget;

class ListView$ArrowScrollFocusResult {
    private int mAmountToScroll;
    private int mSelectedPosition;

    private ListView$ArrowScrollFocusResult() {
    }

    /* access modifiers changed from: package-private */
    public void populate(int selectedPosition, int amountToScroll) {
        this.mSelectedPosition = selectedPosition;
        this.mAmountToScroll = amountToScroll;
    }

    public int getSelectedPosition() {
        return this.mSelectedPosition;
    }

    public int getAmountToScroll() {
        return this.mAmountToScroll;
    }
}

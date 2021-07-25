package android.widget;

class ListView$FocusSelector implements Runnable {
    private int mPosition;
    private int mPositionTop;
    final /* synthetic */ ListView this$0;

    private ListView$FocusSelector(ListView listView) {
        this.this$0 = listView;
    }

    public ListView$FocusSelector setup(int position, int top) {
        this.mPosition = position;
        this.mPositionTop = top;
        return this;
    }

    public void run() {
        this.this$0.setSelectionFromTop(this.mPosition, this.mPositionTop);
    }
}

package android.widget;

class AbsListView$2 implements Runnable {
    final /* synthetic */ AbsListView this$0;
    final /* synthetic */ boolean val$alwaysShow;

    AbsListView$2(AbsListView absListView, boolean z) {
        this.this$0 = absListView;
        this.val$alwaysShow = z;
    }

    public void run() {
        AbsListView.access$100(this.this$0, this.val$alwaysShow);
    }
}

package android.widget;

class Gallery$1 implements Runnable {
    final /* synthetic */ Gallery this$0;

    Gallery$1(Gallery gallery) {
        this.this$0 = gallery;
    }

    public void run() {
        Gallery.access$002(this.this$0, false);
        this.this$0.selectionChanged();
    }
}

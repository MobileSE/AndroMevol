package android.widget;

class TextView$2 implements Runnable {
    final /* synthetic */ TextView this$0;

    TextView$2(TextView textView) {
        this.this$0 = textView;
    }

    public void run() {
        this.this$0.requestLayout();
    }
}

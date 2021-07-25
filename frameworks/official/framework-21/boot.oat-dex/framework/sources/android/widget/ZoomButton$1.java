package android.widget;

class ZoomButton$1 implements Runnable {
    final /* synthetic */ ZoomButton this$0;

    ZoomButton$1(ZoomButton zoomButton) {
        this.this$0 = zoomButton;
    }

    public void run() {
        if (this.this$0.hasOnClickListeners() && ZoomButton.access$000(this.this$0) && this.this$0.isEnabled()) {
            this.this$0.callOnClick();
            ZoomButton.access$200(this.this$0).postDelayed(this, ZoomButton.access$100(this.this$0));
        }
    }
}

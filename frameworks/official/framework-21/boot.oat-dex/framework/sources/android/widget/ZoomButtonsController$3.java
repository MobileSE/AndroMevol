package android.widget;

import android.view.View;

class ZoomButtonsController$3 implements View.OnClickListener {
    final /* synthetic */ ZoomButtonsController this$0;

    ZoomButtonsController$3(ZoomButtonsController zoomButtonsController) {
        this.this$0 = zoomButtonsController;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View v) {
        ZoomButtonsController.access$500(this.this$0, ZoomButtonsController.access$400());
        if (ZoomButtonsController.access$600(this.this$0) != null) {
            ZoomButtonsController.access$600(this.this$0).onZoom(true);
        }
    }
}

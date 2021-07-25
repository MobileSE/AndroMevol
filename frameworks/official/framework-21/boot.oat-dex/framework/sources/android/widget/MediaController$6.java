package android.widget;

import android.net.ConnectivityManager;
import android.view.View;

class MediaController$6 implements View.OnClickListener {
    final /* synthetic */ MediaController this$0;

    MediaController$6(MediaController mediaController) {
        this.this$0 = mediaController;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View v) {
        MediaController.access$700(this.this$0).seekTo(MediaController.access$700(this.this$0).getCurrentPosition() - 5000);
        MediaController.access$500(this.this$0);
        this.this$0.show(ConnectivityManager.CONNECTIVITY_CHANGE_DELAY_DEFAULT);
    }
}

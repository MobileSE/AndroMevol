package android.widget;

import android.content.Intent;
import android.graphics.Rect;
import android.view.View;
import android.widget.RemoteViews;

class RemoteViews$SetOnClickPendingIntent$1 implements View.OnClickListener {
    final /* synthetic */ RemoteViews.SetOnClickPendingIntent this$1;
    final /* synthetic */ RemoteViews.OnClickHandler val$handler;

    RemoteViews$SetOnClickPendingIntent$1(RemoteViews.SetOnClickPendingIntent setOnClickPendingIntent, RemoteViews.OnClickHandler onClickHandler) {
        this.this$1 = setOnClickPendingIntent;
        this.val$handler = onClickHandler;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View v) {
        Rect rect = RemoteViews.access$200(v);
        Intent intent = new Intent();
        intent.setSourceBounds(rect);
        this.val$handler.onClickHandler(v, this.this$1.pendingIntent, intent);
    }
}

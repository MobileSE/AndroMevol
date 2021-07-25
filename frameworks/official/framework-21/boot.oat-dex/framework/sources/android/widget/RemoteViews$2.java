package android.widget;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;

class RemoteViews$2 extends ContextWrapper {
    final /* synthetic */ RemoteViews this$0;
    final /* synthetic */ Context val$contextForResources;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    RemoteViews$2(RemoteViews remoteViews, Context x0, Context context) {
        super(x0);
        this.this$0 = remoteViews;
        this.val$contextForResources = context;
    }

    @Override // android.content.Context, android.content.ContextWrapper
    public Resources getResources() {
        return this.val$contextForResources.getResources();
    }

    @Override // android.content.Context, android.content.ContextWrapper
    public Resources.Theme getTheme() {
        return this.val$contextForResources.getTheme();
    }
}

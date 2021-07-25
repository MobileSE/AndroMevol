package android.widget;

import android.content.Context;
import android.os.Parcel;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;

class RemoteViews$ViewGroupAction extends RemoteViews.Action {
    public static final int TAG = 4;
    RemoteViews nestedViews;
    final /* synthetic */ RemoteViews this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public RemoteViews$ViewGroupAction(RemoteViews remoteViews, int viewId, RemoteViews nestedViews2) {
        super((RemoteViews$1) null);
        this.this$0 = remoteViews;
        this.viewId = viewId;
        this.nestedViews = nestedViews2;
        if (nestedViews2 != null) {
            RemoteViews.access$600(remoteViews, nestedViews2);
        }
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public RemoteViews$ViewGroupAction(RemoteViews remoteViews, Parcel parcel, RemoteViews$BitmapCache bitmapCache) {
        super((RemoteViews$1) null);
        this.this$0 = remoteViews;
        this.viewId = parcel.readInt();
        if (!(parcel.readInt() == 0)) {
            this.nestedViews = new RemoteViews(parcel, bitmapCache, null);
        } else {
            this.nestedViews = null;
        }
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(4);
        dest.writeInt(this.viewId);
        if (this.nestedViews != null) {
            dest.writeInt(1);
            this.nestedViews.writeToParcel(dest, flags);
            return;
        }
        dest.writeInt(0);
    }

    public void apply(View root, ViewGroup rootParent, RemoteViews.OnClickHandler handler) {
        Context context = root.getContext();
        ViewGroup target = (ViewGroup) root.findViewById(this.viewId);
        if (target != null) {
            if (this.nestedViews != null) {
                target.addView(this.nestedViews.apply(context, target, handler));
            } else {
                target.removeAllViews();
            }
        }
    }

    public void updateMemoryUsageEstimate(RemoteViews.MemoryUsageCounter counter) {
        if (this.nestedViews != null) {
            counter.increment(this.nestedViews.estimateMemoryUsage());
        }
    }

    public void setBitmapCache(RemoteViews$BitmapCache bitmapCache) {
        if (this.nestedViews != null) {
            RemoteViews.access$800(this.nestedViews, bitmapCache);
        }
    }

    public String getActionName() {
        return "ViewGroupAction" + (this.nestedViews == null ? "Remove" : "Add");
    }

    public int mergeBehavior() {
        return 1;
    }
}

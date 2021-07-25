package android.widget;

import android.os.Parcel;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;

class RemoteViews$TextViewSizeAction extends RemoteViews.Action {
    public static final int TAG = 13;
    float size;
    final /* synthetic */ RemoteViews this$0;
    int units;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public RemoteViews$TextViewSizeAction(RemoteViews remoteViews, int viewId, int units2, float size2) {
        super((RemoteViews$1) null);
        this.this$0 = remoteViews;
        this.viewId = viewId;
        this.units = units2;
        this.size = size2;
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public RemoteViews$TextViewSizeAction(RemoteViews remoteViews, Parcel parcel) {
        super((RemoteViews$1) null);
        this.this$0 = remoteViews;
        this.viewId = parcel.readInt();
        this.units = parcel.readInt();
        this.size = parcel.readFloat();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(13);
        dest.writeInt(this.viewId);
        dest.writeInt(this.units);
        dest.writeFloat(this.size);
    }

    public void apply(View root, ViewGroup rootParent, RemoteViews.OnClickHandler handler) {
        TextView target = (TextView) root.findViewById(this.viewId);
        if (target != null) {
            target.setTextSize(this.units, this.size);
        }
    }

    public String getActionName() {
        return "TextViewSizeAction";
    }
}

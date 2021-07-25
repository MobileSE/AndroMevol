package android.widget;

import android.database.DataSetObserver;
import android.widget.CursorTreeAdapter;

class CursorTreeAdapter$MyCursorHelper$MyDataSetObserver extends DataSetObserver {
    final /* synthetic */ CursorTreeAdapter.MyCursorHelper this$1;

    private CursorTreeAdapter$MyCursorHelper$MyDataSetObserver(CursorTreeAdapter.MyCursorHelper myCursorHelper) {
        this.this$1 = myCursorHelper;
    }

    @Override // android.database.DataSetObserver
    public void onChanged() {
        CursorTreeAdapter.MyCursorHelper.access$402(this.this$1, true);
        this.this$1.this$0.notifyDataSetChanged();
    }

    @Override // android.database.DataSetObserver
    public void onInvalidated() {
        CursorTreeAdapter.MyCursorHelper.access$402(this.this$1, false);
        this.this$1.this$0.notifyDataSetInvalidated();
    }
}

package android.widget;

import android.database.ContentObserver;
import android.database.Cursor;

class CursorTreeAdapter$MyCursorHelper {
    private MyContentObserver mContentObserver;
    private Cursor mCursor;
    private MyDataSetObserver mDataSetObserver;
    private boolean mDataValid;
    private int mRowIDColumn;
    final /* synthetic */ CursorTreeAdapter this$0;

    CursorTreeAdapter$MyCursorHelper(CursorTreeAdapter cursorTreeAdapter, Cursor cursor) {
        this.this$0 = cursorTreeAdapter;
        boolean cursorPresent = cursor != null;
        this.mCursor = cursor;
        this.mDataValid = cursorPresent;
        this.mRowIDColumn = cursorPresent ? cursor.getColumnIndex("_id") : -1;
        this.mContentObserver = new MyContentObserver();
        this.mDataSetObserver = new MyDataSetObserver(this, (CursorTreeAdapter$1) null);
        if (cursorPresent) {
            cursor.registerContentObserver(this.mContentObserver);
            cursor.registerDataSetObserver(this.mDataSetObserver);
        }
    }

    /* access modifiers changed from: package-private */
    public Cursor getCursor() {
        return this.mCursor;
    }

    /* access modifiers changed from: package-private */
    public int getCount() {
        if (!this.mDataValid || this.mCursor == null) {
            return 0;
        }
        return this.mCursor.getCount();
    }

    /* access modifiers changed from: package-private */
    public long getId(int position) {
        if (!this.mDataValid || this.mCursor == null || !this.mCursor.moveToPosition(position)) {
            return 0;
        }
        return this.mCursor.getLong(this.mRowIDColumn);
    }

    /* access modifiers changed from: package-private */
    public Cursor moveTo(int position) {
        if (!this.mDataValid || this.mCursor == null || !this.mCursor.moveToPosition(position)) {
            return null;
        }
        return this.mCursor;
    }

    /* access modifiers changed from: package-private */
    public void changeCursor(Cursor cursor, boolean releaseCursors) {
        if (cursor != this.mCursor) {
            deactivate();
            this.mCursor = cursor;
            if (cursor != null) {
                cursor.registerContentObserver(this.mContentObserver);
                cursor.registerDataSetObserver(this.mDataSetObserver);
                this.mRowIDColumn = cursor.getColumnIndex("_id");
                this.mDataValid = true;
                this.this$0.notifyDataSetChanged(releaseCursors);
                return;
            }
            this.mRowIDColumn = -1;
            this.mDataValid = false;
            this.this$0.notifyDataSetInvalidated();
        }
    }

    /* access modifiers changed from: package-private */
    public void deactivate() {
        if (this.mCursor != null) {
            this.mCursor.unregisterContentObserver(this.mContentObserver);
            this.mCursor.unregisterDataSetObserver(this.mDataSetObserver);
            this.mCursor.close();
            this.mCursor = null;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isValid() {
        return this.mDataValid && this.mCursor != null;
    }

    /* access modifiers changed from: private */
    public class MyContentObserver extends ContentObserver {
        public MyContentObserver() {
            super(CursorTreeAdapter.access$100(CursorTreeAdapter$MyCursorHelper.this.this$0));
        }

        public boolean deliverSelfNotifications() {
            return true;
        }

        public void onChange(boolean selfChange) {
            if (CursorTreeAdapter.access$200(CursorTreeAdapter$MyCursorHelper.this.this$0) && CursorTreeAdapter$MyCursorHelper.this.mCursor != null && !CursorTreeAdapter$MyCursorHelper.this.mCursor.isClosed()) {
                CursorTreeAdapter$MyCursorHelper.this.mDataValid = CursorTreeAdapter$MyCursorHelper.this.mCursor.requery();
            }
        }
    }
}

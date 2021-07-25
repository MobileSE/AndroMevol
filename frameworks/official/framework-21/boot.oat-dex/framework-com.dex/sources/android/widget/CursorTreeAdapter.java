package android.widget;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.ProxyInfo;
import android.os.Handler;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorFilter;

public abstract class CursorTreeAdapter extends BaseExpandableListAdapter implements Filterable, CursorFilter.CursorFilterClient {
    private boolean mAutoRequery;
    SparseArray<MyCursorHelper> mChildrenCursorHelpers;
    private Context mContext;
    CursorFilter mCursorFilter;
    FilterQueryProvider mFilterQueryProvider;
    MyCursorHelper mGroupCursorHelper;
    private Handler mHandler;

    /* access modifiers changed from: protected */
    public abstract void bindChildView(View view, Context context, Cursor cursor, boolean z);

    /* access modifiers changed from: protected */
    public abstract void bindGroupView(View view, Context context, Cursor cursor, boolean z);

    /* access modifiers changed from: protected */
    public abstract Cursor getChildrenCursor(Cursor cursor);

    /* access modifiers changed from: protected */
    public abstract View newChildView(Context context, Cursor cursor, boolean z, ViewGroup viewGroup);

    /* access modifiers changed from: protected */
    public abstract View newGroupView(Context context, Cursor cursor, boolean z, ViewGroup viewGroup);

    public CursorTreeAdapter(Cursor cursor, Context context) {
        init(cursor, context, true);
    }

    public CursorTreeAdapter(Cursor cursor, Context context, boolean autoRequery) {
        init(cursor, context, autoRequery);
    }

    private void init(Cursor cursor, Context context, boolean autoRequery) {
        this.mContext = context;
        this.mHandler = new Handler();
        this.mAutoRequery = autoRequery;
        this.mGroupCursorHelper = new MyCursorHelper(cursor);
        this.mChildrenCursorHelpers = new SparseArray<>();
    }

    /* access modifiers changed from: package-private */
    public synchronized MyCursorHelper getChildrenCursorHelper(int groupPosition, boolean requestCursor) {
        MyCursorHelper myCursorHelper;
        MyCursorHelper cursorHelper = this.mChildrenCursorHelpers.get(groupPosition);
        if (cursorHelper == null) {
            if (this.mGroupCursorHelper.moveTo(groupPosition) == null) {
                myCursorHelper = null;
            } else {
                cursorHelper = new MyCursorHelper(getChildrenCursor(this.mGroupCursorHelper.getCursor()));
                this.mChildrenCursorHelpers.put(groupPosition, cursorHelper);
            }
        }
        myCursorHelper = cursorHelper;
        return myCursorHelper;
    }

    public void setGroupCursor(Cursor cursor) {
        this.mGroupCursorHelper.changeCursor(cursor, false);
    }

    public void setChildrenCursor(int groupPosition, Cursor childrenCursor) {
        getChildrenCursorHelper(groupPosition, false).changeCursor(childrenCursor, false);
    }

    @Override // android.widget.ExpandableListAdapter
    public Cursor getChild(int groupPosition, int childPosition) {
        return getChildrenCursorHelper(groupPosition, true).moveTo(childPosition);
    }

    @Override // android.widget.ExpandableListAdapter
    public long getChildId(int groupPosition, int childPosition) {
        return getChildrenCursorHelper(groupPosition, true).getId(childPosition);
    }

    @Override // android.widget.ExpandableListAdapter
    public int getChildrenCount(int groupPosition) {
        MyCursorHelper helper = getChildrenCursorHelper(groupPosition, true);
        if (!this.mGroupCursorHelper.isValid() || helper == null) {
            return 0;
        }
        return helper.getCount();
    }

    @Override // android.widget.ExpandableListAdapter
    public Cursor getGroup(int groupPosition) {
        return this.mGroupCursorHelper.moveTo(groupPosition);
    }

    @Override // android.widget.ExpandableListAdapter
    public int getGroupCount() {
        return this.mGroupCursorHelper.getCount();
    }

    @Override // android.widget.ExpandableListAdapter
    public long getGroupId(int groupPosition) {
        return this.mGroupCursorHelper.getId(groupPosition);
    }

    @Override // android.widget.ExpandableListAdapter
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        View v;
        Cursor cursor = this.mGroupCursorHelper.moveTo(groupPosition);
        if (cursor == null) {
            throw new IllegalStateException("this should only be called when the cursor is valid");
        }
        if (convertView == null) {
            v = newGroupView(this.mContext, cursor, isExpanded, parent);
        } else {
            v = convertView;
        }
        bindGroupView(v, this.mContext, cursor, isExpanded);
        return v;
    }

    @Override // android.widget.ExpandableListAdapter
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        View v;
        Cursor cursor = getChildrenCursorHelper(groupPosition, true).moveTo(childPosition);
        if (cursor == null) {
            throw new IllegalStateException("this should only be called when the cursor is valid");
        }
        if (convertView == null) {
            v = newChildView(this.mContext, cursor, isLastChild, parent);
        } else {
            v = convertView;
        }
        bindChildView(v, this.mContext, cursor, isLastChild);
        return v;
    }

    @Override // android.widget.ExpandableListAdapter
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override // android.widget.ExpandableListAdapter
    public boolean hasStableIds() {
        return true;
    }

    private synchronized void releaseCursorHelpers() {
        for (int pos = this.mChildrenCursorHelpers.size() - 1; pos >= 0; pos--) {
            this.mChildrenCursorHelpers.valueAt(pos).deactivate();
        }
        this.mChildrenCursorHelpers.clear();
    }

    @Override // android.widget.BaseExpandableListAdapter
    public void notifyDataSetChanged() {
        notifyDataSetChanged(true);
    }

    public void notifyDataSetChanged(boolean releaseCursors) {
        if (releaseCursors) {
            releaseCursorHelpers();
        }
        super.notifyDataSetChanged();
    }

    @Override // android.widget.BaseExpandableListAdapter
    public void notifyDataSetInvalidated() {
        releaseCursorHelpers();
        super.notifyDataSetInvalidated();
    }

    @Override // android.widget.ExpandableListAdapter, android.widget.BaseExpandableListAdapter
    public void onGroupCollapsed(int groupPosition) {
        deactivateChildrenCursorHelper(groupPosition);
    }

    /* access modifiers changed from: package-private */
    public synchronized void deactivateChildrenCursorHelper(int groupPosition) {
        MyCursorHelper cursorHelper = getChildrenCursorHelper(groupPosition, true);
        this.mChildrenCursorHelpers.remove(groupPosition);
        cursorHelper.deactivate();
    }

    @Override // android.widget.CursorFilter.CursorFilterClient
    public String convertToString(Cursor cursor) {
        return cursor == null ? ProxyInfo.LOCAL_EXCL_LIST : cursor.toString();
    }

    @Override // android.widget.CursorFilter.CursorFilterClient
    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
        if (this.mFilterQueryProvider != null) {
            return this.mFilterQueryProvider.runQuery(constraint);
        }
        return this.mGroupCursorHelper.getCursor();
    }

    @Override // android.widget.Filterable
    public Filter getFilter() {
        if (this.mCursorFilter == null) {
            this.mCursorFilter = new CursorFilter(this);
        }
        return this.mCursorFilter;
    }

    public FilterQueryProvider getFilterQueryProvider() {
        return this.mFilterQueryProvider;
    }

    public void setFilterQueryProvider(FilterQueryProvider filterQueryProvider) {
        this.mFilterQueryProvider = filterQueryProvider;
    }

    @Override // android.widget.CursorFilter.CursorFilterClient
    public void changeCursor(Cursor cursor) {
        this.mGroupCursorHelper.changeCursor(cursor, true);
    }

    @Override // android.widget.CursorFilter.CursorFilterClient
    public Cursor getCursor() {
        return this.mGroupCursorHelper.getCursor();
    }

    /* access modifiers changed from: package-private */
    public class MyCursorHelper {
        private MyContentObserver mContentObserver;
        private Cursor mCursor;
        private MyDataSetObserver mDataSetObserver;
        private boolean mDataValid;
        private int mRowIDColumn;

        MyCursorHelper(Cursor cursor) {
            boolean cursorPresent = cursor != null;
            this.mCursor = cursor;
            this.mDataValid = cursorPresent;
            this.mRowIDColumn = cursorPresent ? cursor.getColumnIndex("_id") : -1;
            this.mContentObserver = new MyContentObserver();
            this.mDataSetObserver = new MyDataSetObserver();
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
                    CursorTreeAdapter.this.notifyDataSetChanged(releaseCursors);
                    return;
                }
                this.mRowIDColumn = -1;
                this.mDataValid = false;
                CursorTreeAdapter.this.notifyDataSetInvalidated();
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
                super(CursorTreeAdapter.this.mHandler);
            }

            @Override // android.database.ContentObserver
            public boolean deliverSelfNotifications() {
                return true;
            }

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange) {
                if (CursorTreeAdapter.this.mAutoRequery && MyCursorHelper.this.mCursor != null && !MyCursorHelper.this.mCursor.isClosed()) {
                    MyCursorHelper.this.mDataValid = MyCursorHelper.this.mCursor.requery();
                }
            }
        }

        /* access modifiers changed from: private */
        public class MyDataSetObserver extends DataSetObserver {
            private MyDataSetObserver() {
            }

            @Override // android.database.DataSetObserver
            public void onChanged() {
                MyCursorHelper.this.mDataValid = true;
                CursorTreeAdapter.this.notifyDataSetChanged();
            }

            @Override // android.database.DataSetObserver
            public void onInvalidated() {
                MyCursorHelper.this.mDataValid = false;
                CursorTreeAdapter.this.notifyDataSetInvalidated();
            }
        }
    }
}

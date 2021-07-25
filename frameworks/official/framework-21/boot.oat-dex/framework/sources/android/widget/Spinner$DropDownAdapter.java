package android.widget;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;

class Spinner$DropDownAdapter implements ListAdapter, SpinnerAdapter {
    private SpinnerAdapter mAdapter;
    private ListAdapter mListAdapter;

    public Spinner$DropDownAdapter(SpinnerAdapter adapter) {
        this.mAdapter = adapter;
        if (adapter instanceof ListAdapter) {
            this.mListAdapter = (ListAdapter) adapter;
        }
    }

    public int getCount() {
        if (this.mAdapter == null) {
            return 0;
        }
        return this.mAdapter.getCount();
    }

    public Object getItem(int position) {
        if (this.mAdapter == null) {
            return null;
        }
        return this.mAdapter.getItem(position);
    }

    public long getItemId(int position) {
        if (this.mAdapter == null) {
            return -1;
        }
        return this.mAdapter.getItemId(position);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        return getDropDownView(position, convertView, parent);
    }

    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (this.mAdapter == null) {
            return null;
        }
        return this.mAdapter.getDropDownView(position, convertView, parent);
    }

    public boolean hasStableIds() {
        return this.mAdapter != null && this.mAdapter.hasStableIds();
    }

    public void registerDataSetObserver(DataSetObserver observer) {
        if (this.mAdapter != null) {
            this.mAdapter.registerDataSetObserver(observer);
        }
    }

    public void unregisterDataSetObserver(DataSetObserver observer) {
        if (this.mAdapter != null) {
            this.mAdapter.unregisterDataSetObserver(observer);
        }
    }

    public boolean areAllItemsEnabled() {
        ListAdapter adapter = this.mListAdapter;
        if (adapter != null) {
            return adapter.areAllItemsEnabled();
        }
        return true;
    }

    public boolean isEnabled(int position) {
        ListAdapter adapter = this.mListAdapter;
        if (adapter != null) {
            return adapter.isEnabled(position);
        }
        return true;
    }

    public int getItemViewType(int position) {
        return 0;
    }

    public int getViewTypeCount() {
        return 1;
    }

    public boolean isEmpty() {
        return getCount() == 0;
    }
}

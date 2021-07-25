package android.widget;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.Iterator;

public class RemoteViewsListAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<RemoteViews> mRemoteViewsList;
    private int mViewTypeCount;
    private ArrayList<Integer> mViewTypes = new ArrayList<>();

    public RemoteViewsListAdapter(Context context, ArrayList<RemoteViews> remoteViews, int viewTypeCount) {
        this.mContext = context;
        this.mRemoteViewsList = remoteViews;
        this.mViewTypeCount = viewTypeCount;
        init();
    }

    public void setViewsList(ArrayList<RemoteViews> remoteViews) {
        this.mRemoteViewsList = remoteViews;
        init();
        notifyDataSetChanged();
    }

    private void init() {
        if (this.mRemoteViewsList != null) {
            this.mViewTypes.clear();
            Iterator i$ = this.mRemoteViewsList.iterator();
            while (i$.hasNext()) {
                RemoteViews rv = i$.next();
                if (!this.mViewTypes.contains(Integer.valueOf(rv.getLayoutId()))) {
                    this.mViewTypes.add(Integer.valueOf(rv.getLayoutId()));
                }
            }
            if (this.mViewTypes.size() > this.mViewTypeCount || this.mViewTypeCount < 1) {
                throw new RuntimeException("Invalid view type count -- view type count must be >= 1and must be as large as the total number of distinct view types");
            }
        }
    }

    @Override // android.widget.Adapter
    public int getCount() {
        if (this.mRemoteViewsList != null) {
            return this.mRemoteViewsList.size();
        }
        return 0;
    }

    @Override // android.widget.Adapter
    public Object getItem(int position) {
        return null;
    }

    @Override // android.widget.Adapter
    public long getItemId(int position) {
        return (long) position;
    }

    @Override // android.widget.Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position >= getCount()) {
            return null;
        }
        RemoteViews rv = this.mRemoteViewsList.get(position);
        rv.setIsWidgetCollectionChild(true);
        if (convertView == null || rv == null || convertView.getId() != rv.getLayoutId()) {
            return rv.apply(this.mContext, parent);
        }
        rv.reapply(this.mContext, convertView);
        return convertView;
    }

    @Override // android.widget.Adapter, android.widget.BaseAdapter
    public int getItemViewType(int position) {
        if (position >= getCount()) {
            return 0;
        }
        return this.mViewTypes.indexOf(Integer.valueOf(this.mRemoteViewsList.get(position).getLayoutId()));
    }

    @Override // android.widget.Adapter, android.widget.BaseAdapter
    public int getViewTypeCount() {
        return this.mViewTypeCount;
    }

    @Override // android.widget.Adapter, android.widget.BaseAdapter
    public boolean hasStableIds() {
        return false;
    }
}

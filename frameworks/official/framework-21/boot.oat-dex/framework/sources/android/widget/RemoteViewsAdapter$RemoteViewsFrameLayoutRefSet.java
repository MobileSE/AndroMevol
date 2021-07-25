package android.widget;

import android.widget.RemoteViewsAdapter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

class RemoteViewsAdapter$RemoteViewsFrameLayoutRefSet {
    private HashMap<Integer, LinkedList<RemoteViewsAdapter.RemoteViewsFrameLayout>> mReferences = new HashMap<>();
    private HashMap<RemoteViewsAdapter.RemoteViewsFrameLayout, LinkedList<RemoteViewsAdapter.RemoteViewsFrameLayout>> mViewToLinkedList = new HashMap<>();
    final /* synthetic */ RemoteViewsAdapter this$0;

    public RemoteViewsAdapter$RemoteViewsFrameLayoutRefSet(RemoteViewsAdapter remoteViewsAdapter) {
        this.this$0 = remoteViewsAdapter;
    }

    public void add(int position, RemoteViewsAdapter.RemoteViewsFrameLayout layout) {
        LinkedList<RemoteViewsAdapter.RemoteViewsFrameLayout> refs;
        Integer pos = Integer.valueOf(position);
        if (this.mReferences.containsKey(pos)) {
            refs = this.mReferences.get(pos);
        } else {
            refs = new LinkedList<>();
            this.mReferences.put(pos, refs);
        }
        this.mViewToLinkedList.put(layout, refs);
        refs.add(layout);
    }

    public void notifyOnRemoteViewsLoaded(int position, RemoteViews view) {
        if (view != null) {
            Integer pos = Integer.valueOf(position);
            if (this.mReferences.containsKey(pos)) {
                LinkedList<RemoteViewsAdapter.RemoteViewsFrameLayout> refs = this.mReferences.get(pos);
                Iterator i$ = refs.iterator();
                while (i$.hasNext()) {
                    RemoteViewsAdapter.RemoteViewsFrameLayout ref = i$.next();
                    ref.onRemoteViewsLoaded(view, RemoteViewsAdapter.access$1100(this.this$0));
                    if (this.mViewToLinkedList.containsKey(ref)) {
                        this.mViewToLinkedList.remove(ref);
                    }
                }
                refs.clear();
                this.mReferences.remove(pos);
            }
        }
    }

    public void removeView(RemoteViewsAdapter.RemoteViewsFrameLayout rvfl) {
        if (this.mViewToLinkedList.containsKey(rvfl)) {
            this.mViewToLinkedList.get(rvfl).remove(rvfl);
            this.mViewToLinkedList.remove(rvfl);
        }
    }

    public void clear() {
        this.mReferences.clear();
        this.mViewToLinkedList.clear();
    }
}

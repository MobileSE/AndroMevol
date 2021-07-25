package android.widget;

import android.widget.RemoteViewsAdapter;

class RemoteViewsAdapter$1 implements Runnable {
    final /* synthetic */ RemoteViewsAdapter this$0;
    final /* synthetic */ RemoteViewsAdapter.RemoteViewsCacheKey val$key;

    RemoteViewsAdapter$1(RemoteViewsAdapter remoteViewsAdapter, RemoteViewsAdapter.RemoteViewsCacheKey remoteViewsCacheKey) {
        this.this$0 = remoteViewsAdapter;
        this.val$key = remoteViewsCacheKey;
    }

    public void run() {
        synchronized (RemoteViewsAdapter.access$1400()) {
            if (RemoteViewsAdapter.access$1400().containsKey(this.val$key)) {
                RemoteViewsAdapter.access$1400().remove(this.val$key);
            }
            if (RemoteViewsAdapter.access$1500().containsKey(this.val$key)) {
                RemoteViewsAdapter.access$1500().remove(this.val$key);
            }
        }
    }
}

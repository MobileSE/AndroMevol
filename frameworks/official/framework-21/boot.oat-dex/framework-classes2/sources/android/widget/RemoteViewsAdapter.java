package android.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.util.Slog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import com.android.internal.R;
import com.android.internal.widget.IRemoteViewsAdapterConnection;
import com.android.internal.widget.IRemoteViewsFactory;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class RemoteViewsAdapter extends BaseAdapter implements Handler.Callback {
    private static final String MULTI_USER_PERM = "android.permission.INTERACT_ACROSS_USERS_FULL";
    private static final int REMOTE_VIEWS_CACHE_DURATION = 5000;
    private static final String TAG = "RemoteViewsAdapter";
    private static Handler sCacheRemovalQueue = null;
    private static HandlerThread sCacheRemovalThread = null;
    private static final HashMap<RemoteViewsCacheKey, FixedSizeRemoteViewsCache> sCachedRemoteViewsCaches = new HashMap<>();
    private static final int sDefaultCacheSize = 40;
    private static final int sDefaultLoadingViewHeight = 50;
    private static final int sDefaultMessageType = 0;
    private static final HashMap<RemoteViewsCacheKey, Runnable> sRemoteViewsCacheRemoveRunnables = new HashMap<>();
    private static final int sUnbindServiceDelay = 5000;
    private static final int sUnbindServiceMessageType = 1;
    private final int mAppWidgetId;
    private FixedSizeRemoteViewsCache mCache;
    private WeakReference<RemoteAdapterConnectionCallback> mCallback;
    private final Context mContext;
    private boolean mDataReady = false;
    private final Intent mIntent;
    private LayoutInflater mLayoutInflater;
    private Handler mMainQueue;
    private boolean mNotifyDataSetChangedAfterOnServiceConnected = false;
    private RemoteViews.OnClickHandler mRemoteViewsOnClickHandler;
    private RemoteViewsFrameLayoutRefSet mRequestedViews;
    private RemoteViewsAdapterServiceConnection mServiceConnection;
    private int mVisibleWindowLowerBound;
    private int mVisibleWindowUpperBound;
    private Handler mWorkerQueue;
    private HandlerThread mWorkerThread;

    public interface RemoteAdapterConnectionCallback {
        void deferNotifyDataSetChanged();

        boolean onRemoteAdapterConnected();

        void onRemoteAdapterDisconnected();
    }

    /* access modifiers changed from: private */
    public static class RemoteViewsAdapterServiceConnection extends IRemoteViewsAdapterConnection.Stub {
        private WeakReference<RemoteViewsAdapter> mAdapter;
        private boolean mIsConnected;
        private boolean mIsConnecting;
        private IRemoteViewsFactory mRemoteViewsFactory;

        public RemoteViewsAdapterServiceConnection(RemoteViewsAdapter adapter) {
            this.mAdapter = new WeakReference<>(adapter);
        }

        public synchronized void bind(Context context, int appWidgetId, Intent intent) {
            if (!this.mIsConnecting) {
                try {
                    AppWidgetManager mgr = AppWidgetManager.getInstance(context);
                    if (this.mAdapter.get() != null) {
                        mgr.bindRemoteViewsService(context.getOpPackageName(), appWidgetId, intent, asBinder());
                    } else {
                        Slog.w(RemoteViewsAdapter.TAG, "bind: adapter was null");
                    }
                    this.mIsConnecting = true;
                } catch (Exception e) {
                    Log.e("RemoteViewsAdapterServiceConnection", "bind(): " + e.getMessage());
                    this.mIsConnecting = false;
                    this.mIsConnected = false;
                }
            }
        }

        public synchronized void unbind(Context context, int appWidgetId, Intent intent) {
            try {
                AppWidgetManager mgr = AppWidgetManager.getInstance(context);
                if (this.mAdapter.get() != null) {
                    mgr.unbindRemoteViewsService(context.getOpPackageName(), appWidgetId, intent);
                } else {
                    Slog.w(RemoteViewsAdapter.TAG, "unbind: adapter was null");
                }
                this.mIsConnecting = false;
            } catch (Exception e) {
                Log.e("RemoteViewsAdapterServiceConnection", "unbind(): " + e.getMessage());
                this.mIsConnecting = false;
                this.mIsConnected = false;
            }
            return;
        }

        @Override // com.android.internal.widget.IRemoteViewsAdapterConnection
        public synchronized void onServiceConnected(IBinder service) {
            this.mRemoteViewsFactory = IRemoteViewsFactory.Stub.asInterface(service);
            final RemoteViewsAdapter adapter = this.mAdapter.get();
            if (adapter != null) {
                adapter.mWorkerQueue.post(new Runnable() {
                    /* class android.widget.RemoteViewsAdapter.RemoteViewsAdapterServiceConnection.AnonymousClass1 */

                    public void run() {
                        if (adapter.mNotifyDataSetChangedAfterOnServiceConnected) {
                            adapter.onNotifyDataSetChanged();
                        } else {
                            IRemoteViewsFactory factory = adapter.mServiceConnection.getRemoteViewsFactory();
                            try {
                                if (!factory.isCreated()) {
                                    factory.onDataSetChanged();
                                }
                            } catch (RemoteException e) {
                                Log.e(RemoteViewsAdapter.TAG, "Error notifying factory of data set changed in onServiceConnected(): " + e.getMessage());
                                return;
                            } catch (RuntimeException e2) {
                                Log.e(RemoteViewsAdapter.TAG, "Error notifying factory of data set changed in onServiceConnected(): " + e2.getMessage());
                            }
                            adapter.updateTemporaryMetaData();
                            adapter.mMainQueue.post(new Runnable() {
                                /* class android.widget.RemoteViewsAdapter.RemoteViewsAdapterServiceConnection.AnonymousClass1.AnonymousClass1 */

                                public void run() {
                                    synchronized (adapter.mCache) {
                                        adapter.mCache.commitTemporaryMetaData();
                                    }
                                    RemoteAdapterConnectionCallback callback = (RemoteAdapterConnectionCallback) adapter.mCallback.get();
                                    if (callback != null) {
                                        callback.onRemoteAdapterConnected();
                                    }
                                }
                            });
                        }
                        adapter.enqueueDeferredUnbindServiceMessage();
                        RemoteViewsAdapterServiceConnection.this.mIsConnected = true;
                        RemoteViewsAdapterServiceConnection.this.mIsConnecting = false;
                    }
                });
            }
        }

        @Override // com.android.internal.widget.IRemoteViewsAdapterConnection
        public synchronized void onServiceDisconnected() {
            this.mIsConnected = false;
            this.mIsConnecting = false;
            this.mRemoteViewsFactory = null;
            final RemoteViewsAdapter adapter = this.mAdapter.get();
            if (adapter != null) {
                adapter.mMainQueue.post(new Runnable() {
                    /* class android.widget.RemoteViewsAdapter.RemoteViewsAdapterServiceConnection.AnonymousClass2 */

                    public void run() {
                        adapter.mMainQueue.removeMessages(1);
                        RemoteAdapterConnectionCallback callback = (RemoteAdapterConnectionCallback) adapter.mCallback.get();
                        if (callback != null) {
                            callback.onRemoteAdapterDisconnected();
                        }
                    }
                });
            }
        }

        public synchronized IRemoteViewsFactory getRemoteViewsFactory() {
            return this.mRemoteViewsFactory;
        }

        public synchronized boolean isConnected() {
            return this.mIsConnected;
        }
    }

    /* access modifiers changed from: private */
    public static class RemoteViewsFrameLayout extends FrameLayout {
        public RemoteViewsFrameLayout(Context context) {
            super(context);
        }

        public void onRemoteViewsLoaded(RemoteViews view, RemoteViews.OnClickHandler handler) {
            try {
                removeAllViews();
                addView(view.apply(getContext(), this, handler));
            } catch (Exception e) {
                Log.e(RemoteViewsAdapter.TAG, "Failed to apply RemoteViews.");
            }
        }
    }

    /* access modifiers changed from: private */
    public static class RemoteViewsMetaData {
        int count;
        boolean hasStableIds;
        RemoteViews mFirstView;
        int mFirstViewHeight;
        private final HashMap<Integer, Integer> mTypeIdIndexMap = new HashMap<>();
        RemoteViews mUserLoadingView;
        int viewTypeCount;

        public RemoteViewsMetaData() {
            reset();
        }

        public void set(RemoteViewsMetaData d) {
            synchronized (d) {
                this.count = d.count;
                this.viewTypeCount = d.viewTypeCount;
                this.hasStableIds = d.hasStableIds;
                setLoadingViewTemplates(d.mUserLoadingView, d.mFirstView);
            }
        }

        public void reset() {
            this.count = 0;
            this.viewTypeCount = 1;
            this.hasStableIds = true;
            this.mUserLoadingView = null;
            this.mFirstView = null;
            this.mFirstViewHeight = 0;
            this.mTypeIdIndexMap.clear();
        }

        public void setLoadingViewTemplates(RemoteViews loadingView, RemoteViews firstView) {
            this.mUserLoadingView = loadingView;
            if (firstView != null) {
                this.mFirstView = firstView;
                this.mFirstViewHeight = -1;
            }
        }

        public int getMappedViewType(int typeId) {
            if (this.mTypeIdIndexMap.containsKey(Integer.valueOf(typeId))) {
                return this.mTypeIdIndexMap.get(Integer.valueOf(typeId)).intValue();
            }
            int incrementalTypeId = this.mTypeIdIndexMap.size() + 1;
            this.mTypeIdIndexMap.put(Integer.valueOf(typeId), Integer.valueOf(incrementalTypeId));
            return incrementalTypeId;
        }

        public boolean isViewTypeInRange(int typeId) {
            if (getMappedViewType(typeId) >= this.viewTypeCount) {
                return false;
            }
            return true;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private RemoteViewsFrameLayout createLoadingView(int position, View convertView, ViewGroup parent, Object lock, LayoutInflater layoutInflater, RemoteViews.OnClickHandler handler) {
            Context context = parent.getContext();
            RemoteViewsFrameLayout layout = new RemoteViewsFrameLayout(context);
            synchronized (lock) {
                boolean customLoadingViewAvailable = false;
                if (this.mUserLoadingView != null) {
                    try {
                        View loadingView = this.mUserLoadingView.apply(parent.getContext(), parent, handler);
                        loadingView.setTagInternal(R.id.rowTypeId, new Integer(0));
                        layout.addView(loadingView);
                        customLoadingViewAvailable = true;
                    } catch (Exception e) {
                        Log.w(RemoteViewsAdapter.TAG, "Error inflating custom loading view, using default loadingview instead", e);
                    }
                }
                if (!customLoadingViewAvailable) {
                    if (this.mFirstViewHeight < 0) {
                        try {
                            View firstView = this.mFirstView.apply(parent.getContext(), parent, handler);
                            firstView.measure(View.MeasureSpec.makeMeasureSpec(0, 0), View.MeasureSpec.makeMeasureSpec(0, 0));
                            this.mFirstViewHeight = firstView.getMeasuredHeight();
                            this.mFirstView = null;
                        } catch (Exception e2) {
                            this.mFirstViewHeight = Math.round(50.0f * context.getResources().getDisplayMetrics().density);
                            this.mFirstView = null;
                            Log.w(RemoteViewsAdapter.TAG, "Error inflating first RemoteViews" + e2);
                        }
                    }
                    TextView loadingTextView = (TextView) layoutInflater.inflate(R.layout.remote_views_adapter_default_loading_view, (ViewGroup) layout, false);
                    loadingTextView.setHeight(this.mFirstViewHeight);
                    loadingTextView.setTag(new Integer(0));
                    layout.addView(loadingTextView);
                }
            }
            return layout;
        }
    }

    /* access modifiers changed from: private */
    public static class RemoteViewsIndexMetaData {
        long itemId;
        int typeId;

        public RemoteViewsIndexMetaData(RemoteViews v, long itemId2) {
            set(v, itemId2);
        }

        public void set(RemoteViews v, long id) {
            this.itemId = id;
            if (v != null) {
                this.typeId = v.getLayoutId();
            } else {
                this.typeId = 0;
            }
        }
    }

    /* access modifiers changed from: private */
    public static class FixedSizeRemoteViewsCache {
        private static final String TAG = "FixedSizeRemoteViewsCache";
        private static final float sMaxCountSlackPercent = 0.75f;
        private static final int sMaxMemoryLimitInBytes = 2097152;
        private HashMap<Integer, RemoteViewsIndexMetaData> mIndexMetaData = new HashMap<>();
        private HashMap<Integer, RemoteViews> mIndexRemoteViews = new HashMap<>();
        private int mLastRequestedIndex = -1;
        private HashSet<Integer> mLoadIndices = new HashSet<>();
        private int mMaxCount;
        private int mMaxCountSlack = Math.round(sMaxCountSlackPercent * ((float) (this.mMaxCount / 2)));
        private final RemoteViewsMetaData mMetaData = new RemoteViewsMetaData();
        private int mPreloadLowerBound = 0;
        private int mPreloadUpperBound = -1;
        private HashSet<Integer> mRequestedIndices = new HashSet<>();
        private final RemoteViewsMetaData mTemporaryMetaData = new RemoteViewsMetaData();

        public FixedSizeRemoteViewsCache(int maxCacheSize) {
            this.mMaxCount = maxCacheSize;
        }

        public void insert(int position, RemoteViews v, long itemId, ArrayList<Integer> visibleWindow) {
            int pruneFromPosition;
            if (this.mIndexRemoteViews.size() >= this.mMaxCount) {
                this.mIndexRemoteViews.remove(Integer.valueOf(getFarthestPositionFrom(position, visibleWindow)));
            }
            if (this.mLastRequestedIndex > -1) {
                pruneFromPosition = this.mLastRequestedIndex;
            } else {
                pruneFromPosition = position;
            }
            while (getRemoteViewsBitmapMemoryUsage() >= sMaxMemoryLimitInBytes) {
                this.mIndexRemoteViews.remove(Integer.valueOf(getFarthestPositionFrom(pruneFromPosition, visibleWindow)));
            }
            if (this.mIndexMetaData.containsKey(Integer.valueOf(position))) {
                this.mIndexMetaData.get(Integer.valueOf(position)).set(v, itemId);
            } else {
                this.mIndexMetaData.put(Integer.valueOf(position), new RemoteViewsIndexMetaData(v, itemId));
            }
            this.mIndexRemoteViews.put(Integer.valueOf(position), v);
        }

        public RemoteViewsMetaData getMetaData() {
            return this.mMetaData;
        }

        public RemoteViewsMetaData getTemporaryMetaData() {
            return this.mTemporaryMetaData;
        }

        public RemoteViews getRemoteViewsAt(int position) {
            if (this.mIndexRemoteViews.containsKey(Integer.valueOf(position))) {
                return this.mIndexRemoteViews.get(Integer.valueOf(position));
            }
            return null;
        }

        public RemoteViewsIndexMetaData getMetaDataAt(int position) {
            if (this.mIndexMetaData.containsKey(Integer.valueOf(position))) {
                return this.mIndexMetaData.get(Integer.valueOf(position));
            }
            return null;
        }

        public void commitTemporaryMetaData() {
            synchronized (this.mTemporaryMetaData) {
                synchronized (this.mMetaData) {
                    this.mMetaData.set(this.mTemporaryMetaData);
                }
            }
        }

        private int getRemoteViewsBitmapMemoryUsage() {
            int mem = 0;
            for (Integer i : this.mIndexRemoteViews.keySet()) {
                RemoteViews v = this.mIndexRemoteViews.get(i);
                if (v != null) {
                    mem += v.estimateMemoryUsage();
                }
            }
            return mem;
        }

        private int getFarthestPositionFrom(int pos, ArrayList<Integer> visibleWindow) {
            int maxDist = 0;
            int maxDistIndex = -1;
            int maxDistNotVisible = 0;
            int maxDistIndexNotVisible = -1;
            for (Integer num : this.mIndexRemoteViews.keySet()) {
                int i = num.intValue();
                int dist = Math.abs(i - pos);
                if (dist > maxDistNotVisible && !visibleWindow.contains(Integer.valueOf(i))) {
                    maxDistIndexNotVisible = i;
                    maxDistNotVisible = dist;
                }
                if (dist >= maxDist) {
                    maxDistIndex = i;
                    maxDist = dist;
                }
            }
            return maxDistIndexNotVisible > -1 ? maxDistIndexNotVisible : maxDistIndex;
        }

        public void queueRequestedPositionToLoad(int position) {
            this.mLastRequestedIndex = position;
            synchronized (this.mLoadIndices) {
                this.mRequestedIndices.add(Integer.valueOf(position));
                this.mLoadIndices.add(Integer.valueOf(position));
            }
        }

        public boolean queuePositionsToBePreloadedFromRequestedPosition(int position) {
            int count;
            if (this.mPreloadLowerBound <= position && position <= this.mPreloadUpperBound && Math.abs(position - ((this.mPreloadUpperBound + this.mPreloadLowerBound) / 2)) < this.mMaxCountSlack) {
                return false;
            }
            synchronized (this.mMetaData) {
                count = this.mMetaData.count;
            }
            synchronized (this.mLoadIndices) {
                this.mLoadIndices.clear();
                this.mLoadIndices.addAll(this.mRequestedIndices);
                int halfMaxCount = this.mMaxCount / 2;
                this.mPreloadLowerBound = position - halfMaxCount;
                this.mPreloadUpperBound = position + halfMaxCount;
                int effectiveLowerBound = Math.max(0, this.mPreloadLowerBound);
                int effectiveUpperBound = Math.min(this.mPreloadUpperBound, count - 1);
                for (int i = effectiveLowerBound; i <= effectiveUpperBound; i++) {
                    this.mLoadIndices.add(Integer.valueOf(i));
                }
                this.mLoadIndices.removeAll(this.mIndexRemoteViews.keySet());
            }
            return true;
        }

        public int[] getNextIndexToLoad() {
            int[] iArr;
            synchronized (this.mLoadIndices) {
                if (!this.mRequestedIndices.isEmpty()) {
                    Integer i = this.mRequestedIndices.iterator().next();
                    this.mRequestedIndices.remove(i);
                    this.mLoadIndices.remove(i);
                    iArr = new int[]{i.intValue(), 1};
                } else if (!this.mLoadIndices.isEmpty()) {
                    Integer i2 = this.mLoadIndices.iterator().next();
                    this.mLoadIndices.remove(i2);
                    iArr = new int[]{i2.intValue(), 0};
                } else {
                    iArr = new int[]{-1, 0};
                }
            }
            return iArr;
        }

        public boolean containsRemoteViewAt(int position) {
            return this.mIndexRemoteViews.containsKey(Integer.valueOf(position));
        }

        public boolean containsMetaDataAt(int position) {
            return this.mIndexMetaData.containsKey(Integer.valueOf(position));
        }

        public void reset() {
            this.mPreloadLowerBound = 0;
            this.mPreloadUpperBound = -1;
            this.mLastRequestedIndex = -1;
            this.mIndexRemoteViews.clear();
            this.mIndexMetaData.clear();
            synchronized (this.mLoadIndices) {
                this.mRequestedIndices.clear();
                this.mLoadIndices.clear();
            }
        }
    }

    static class RemoteViewsCacheKey {
        final Intent.FilterComparison filter;
        final int widgetId;

        RemoteViewsCacheKey(Intent.FilterComparison filter2, int widgetId2) {
            this.filter = filter2;
            this.widgetId = widgetId2;
        }

        public boolean equals(Object o) {
            if (!(o instanceof RemoteViewsCacheKey)) {
                return false;
            }
            RemoteViewsCacheKey other = (RemoteViewsCacheKey) o;
            if (!other.filter.equals(this.filter) || other.widgetId != this.widgetId) {
                return false;
            }
            return true;
        }

        public int hashCode() {
            return (this.filter == null ? 0 : this.filter.hashCode()) ^ (this.widgetId << 2);
        }
    }

    public RemoteViewsAdapter(Context context, Intent intent, RemoteAdapterConnectionCallback callback) {
        this.mContext = context;
        this.mIntent = intent;
        this.mAppWidgetId = intent.getIntExtra("remoteAdapterAppWidgetId", -1);
        this.mLayoutInflater = LayoutInflater.from(context);
        if (this.mIntent == null) {
            throw new IllegalArgumentException("Non-null Intent must be specified.");
        }
        this.mRequestedViews = new RemoteViewsFrameLayoutRefSet(this);
        if (intent.hasExtra("remoteAdapterAppWidgetId")) {
            intent.removeExtra("remoteAdapterAppWidgetId");
        }
        this.mWorkerThread = new HandlerThread("RemoteViewsCache-loader");
        this.mWorkerThread.start();
        this.mWorkerQueue = new Handler(this.mWorkerThread.getLooper());
        this.mMainQueue = new Handler(Looper.myLooper(), this);
        if (sCacheRemovalThread == null) {
            sCacheRemovalThread = new HandlerThread("RemoteViewsAdapter-cachePruner");
            sCacheRemovalThread.start();
            sCacheRemovalQueue = new Handler(sCacheRemovalThread.getLooper());
        }
        this.mCallback = new WeakReference<>(callback);
        this.mServiceConnection = new RemoteViewsAdapterServiceConnection(this);
        RemoteViewsCacheKey key = new RemoteViewsCacheKey(new Intent.FilterComparison(this.mIntent), this.mAppWidgetId);
        synchronized (sCachedRemoteViewsCaches) {
            if (sCachedRemoteViewsCaches.containsKey(key)) {
                this.mCache = sCachedRemoteViewsCaches.get(key);
                synchronized (this.mCache.mMetaData) {
                    if (this.mCache.mMetaData.count > 0) {
                        this.mDataReady = true;
                    }
                }
            } else {
                this.mCache = new FixedSizeRemoteViewsCache(40);
            }
            if (!this.mDataReady) {
                requestBindService();
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public void finalize() throws Throwable {
        try {
            if (this.mWorkerThread != null) {
                this.mWorkerThread.quit();
            }
        } finally {
            super.finalize();
        }
    }

    public boolean isDataReady() {
        return this.mDataReady;
    }

    public void setRemoteViewsOnClickHandler(RemoteViews.OnClickHandler handler) {
        this.mRemoteViewsOnClickHandler = handler;
    }

    public void saveRemoteViewsCache() {
        int metaDataCount;
        int numRemoteViewsCached;
        RemoteViewsCacheKey key = new RemoteViewsCacheKey(new Intent.FilterComparison(this.mIntent), this.mAppWidgetId);
        synchronized (sCachedRemoteViewsCaches) {
            if (sRemoteViewsCacheRemoveRunnables.containsKey(key)) {
                sCacheRemovalQueue.removeCallbacks(sRemoteViewsCacheRemoveRunnables.get(key));
                sRemoteViewsCacheRemoveRunnables.remove(key);
            }
            synchronized (this.mCache.mMetaData) {
                metaDataCount = this.mCache.mMetaData.count;
            }
            synchronized (this.mCache) {
                numRemoteViewsCached = this.mCache.mIndexRemoteViews.size();
            }
            if (metaDataCount > 0 && numRemoteViewsCached > 0) {
                sCachedRemoteViewsCaches.put(key, this.mCache);
            }
            Runnable r = new 1(this, key);
            sRemoteViewsCacheRemoveRunnables.put(key, r);
            sCacheRemovalQueue.postDelayed(r, 5000);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void loadNextIndexInBackground() {
        this.mWorkerQueue.post(new Runnable() {
            /* class android.widget.RemoteViewsAdapter.AnonymousClass2 */

            public void run() {
                int position;
                if (RemoteViewsAdapter.this.mServiceConnection.isConnected()) {
                    synchronized (RemoteViewsAdapter.this.mCache) {
                        position = RemoteViewsAdapter.this.mCache.getNextIndexToLoad()[0];
                    }
                    if (position > -1) {
                        RemoteViewsAdapter.this.updateRemoteViews(position, true);
                        RemoteViewsAdapter.this.loadNextIndexInBackground();
                        return;
                    }
                    RemoteViewsAdapter.this.enqueueDeferredUnbindServiceMessage();
                }
            }
        });
    }

    private void processException(String method, Exception e) {
        Log.e(TAG, "Error in " + method + ": " + e.getMessage());
        RemoteViewsMetaData metaData = this.mCache.getMetaData();
        synchronized (metaData) {
            metaData.reset();
        }
        synchronized (this.mCache) {
            this.mCache.reset();
        }
        this.mMainQueue.post(new Runnable() {
            /* class android.widget.RemoteViewsAdapter.AnonymousClass3 */

            public void run() {
                RemoteViewsAdapter.this.superNotifyDataSetChanged();
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateTemporaryMetaData() {
        IRemoteViewsFactory factory = this.mServiceConnection.getRemoteViewsFactory();
        try {
            boolean hasStableIds = factory.hasStableIds();
            int viewTypeCount = factory.getViewTypeCount();
            int count = factory.getCount();
            RemoteViews loadingView = factory.getLoadingView();
            RemoteViews firstView = null;
            if (count > 0 && loadingView == null) {
                firstView = factory.getViewAt(0);
            }
            RemoteViewsMetaData tmpMetaData = this.mCache.getTemporaryMetaData();
            synchronized (tmpMetaData) {
                tmpMetaData.hasStableIds = hasStableIds;
                tmpMetaData.viewTypeCount = viewTypeCount + 1;
                tmpMetaData.count = count;
                tmpMetaData.setLoadingViewTemplates(loadingView, firstView);
            }
        } catch (RemoteException e) {
            processException("updateMetaData", e);
        } catch (RuntimeException e2) {
            processException("updateMetaData", e2);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateRemoteViews(final int position, boolean notifyWhenLoaded) {
        boolean viewTypeInRange;
        int cacheCount;
        IRemoteViewsFactory factory = this.mServiceConnection.getRemoteViewsFactory();
        try {
            final RemoteViews remoteViews = factory.getViewAt(position);
            long itemId = factory.getItemId(position);
            if (remoteViews == null) {
                Log.e(TAG, "Error in updateRemoteViews(" + position + "): " + " null RemoteViews " + "returned from RemoteViewsFactory.");
                return;
            }
            int layoutId = remoteViews.getLayoutId();
            RemoteViewsMetaData metaData = this.mCache.getMetaData();
            synchronized (metaData) {
                viewTypeInRange = metaData.isViewTypeInRange(layoutId);
                cacheCount = this.mCache.mMetaData.count;
            }
            synchronized (this.mCache) {
                if (viewTypeInRange) {
                    this.mCache.insert(position, remoteViews, itemId, getVisibleWindow(this.mVisibleWindowLowerBound, this.mVisibleWindowUpperBound, cacheCount));
                    if (notifyWhenLoaded) {
                        this.mMainQueue.post(new Runnable() {
                            /* class android.widget.RemoteViewsAdapter.AnonymousClass4 */

                            public void run() {
                                RemoteViewsAdapter.this.mRequestedViews.notifyOnRemoteViewsLoaded(position, remoteViews);
                            }
                        });
                    }
                } else {
                    Log.e(TAG, "Error: widget's RemoteViewsFactory returns more view types than  indicated by getViewTypeCount() ");
                }
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error in updateRemoteViews(" + position + "): " + e.getMessage());
        } catch (RuntimeException e2) {
            Log.e(TAG, "Error in updateRemoteViews(" + position + "): " + e2.getMessage());
        }
    }

    public Intent getRemoteViewsServiceIntent() {
        return this.mIntent;
    }

    @Override // android.widget.Adapter
    public int getCount() {
        int i;
        RemoteViewsMetaData metaData = this.mCache.getMetaData();
        synchronized (metaData) {
            i = metaData.count;
        }
        return i;
    }

    @Override // android.widget.Adapter
    public Object getItem(int position) {
        return null;
    }

    @Override // android.widget.Adapter
    public long getItemId(int position) {
        long j;
        synchronized (this.mCache) {
            if (this.mCache.containsMetaDataAt(position)) {
                j = this.mCache.getMetaDataAt(position).itemId;
            } else {
                j = 0;
            }
        }
        return j;
    }

    @Override // android.widget.Adapter
    public int getItemViewType(int position) {
        int i;
        synchronized (this.mCache) {
            if (this.mCache.containsMetaDataAt(position)) {
                int typeId = this.mCache.getMetaDataAt(position).typeId;
                RemoteViewsMetaData metaData = this.mCache.getMetaData();
                synchronized (metaData) {
                    i = metaData.getMappedViewType(typeId);
                }
            } else {
                i = 0;
            }
        }
        return i;
    }

    private int getConvertViewTypeId(View convertView) {
        Object tag;
        if (convertView == null || (tag = convertView.getTag(R.id.rowTypeId)) == null) {
            return -1;
        }
        return ((Integer) tag).intValue();
    }

    public void setVisibleRangeHint(int lowerBound, int upperBound) {
        this.mVisibleWindowLowerBound = lowerBound;
        this.mVisibleWindowUpperBound = upperBound;
    }

    /* JADX WARNING: Removed duplicated region for block: B:43:0x0105  */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x012d  */
    @Override // android.widget.Adapter
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public android.view.View getView(int r25, android.view.View r26, android.view.ViewGroup r27) {
        /*
        // Method dump skipped, instructions count: 379
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.RemoteViewsAdapter.getView(int, android.view.View, android.view.ViewGroup):android.view.View");
    }

    @Override // android.widget.Adapter
    public int getViewTypeCount() {
        int i;
        RemoteViewsMetaData metaData = this.mCache.getMetaData();
        synchronized (metaData) {
            i = metaData.viewTypeCount;
        }
        return i;
    }

    @Override // android.widget.Adapter
    public boolean hasStableIds() {
        boolean z;
        RemoteViewsMetaData metaData = this.mCache.getMetaData();
        synchronized (metaData) {
            z = metaData.hasStableIds;
        }
        return z;
    }

    @Override // android.widget.Adapter
    public boolean isEmpty() {
        return getCount() <= 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onNotifyDataSetChanged() {
        int newCount;
        ArrayList<Integer> visibleWindow;
        try {
            this.mServiceConnection.getRemoteViewsFactory().onDataSetChanged();
            synchronized (this.mCache) {
                this.mCache.reset();
            }
            updateTemporaryMetaData();
            synchronized (this.mCache.getTemporaryMetaData()) {
                newCount = this.mCache.getTemporaryMetaData().count;
                visibleWindow = getVisibleWindow(this.mVisibleWindowLowerBound, this.mVisibleWindowUpperBound, newCount);
            }
            Iterator i$ = visibleWindow.iterator();
            while (i$.hasNext()) {
                int i = i$.next().intValue();
                if (i < newCount) {
                    updateRemoteViews(i, false);
                }
            }
            this.mMainQueue.post(new Runnable() {
                /* class android.widget.RemoteViewsAdapter.AnonymousClass5 */

                public void run() {
                    synchronized (RemoteViewsAdapter.this.mCache) {
                        RemoteViewsAdapter.this.mCache.commitTemporaryMetaData();
                    }
                    RemoteViewsAdapter.this.superNotifyDataSetChanged();
                    RemoteViewsAdapter.this.enqueueDeferredUnbindServiceMessage();
                }
            });
            this.mNotifyDataSetChangedAfterOnServiceConnected = false;
        } catch (RemoteException e) {
            Log.e(TAG, "Error in updateNotifyDataSetChanged(): " + e.getMessage());
        } catch (RuntimeException e2) {
            Log.e(TAG, "Error in updateNotifyDataSetChanged(): " + e2.getMessage());
        }
    }

    private ArrayList<Integer> getVisibleWindow(int lower, int upper, int count) {
        ArrayList<Integer> window = new ArrayList<>();
        if (!(lower == 0 && upper == 0) && lower >= 0 && upper >= 0) {
            if (lower <= upper) {
                for (int i = lower; i <= upper; i++) {
                    window.add(Integer.valueOf(i));
                }
            } else {
                for (int i2 = lower; i2 < count; i2++) {
                    window.add(Integer.valueOf(i2));
                }
                for (int i3 = 0; i3 <= upper; i3++) {
                    window.add(Integer.valueOf(i3));
                }
            }
        }
        return window;
    }

    public void notifyDataSetChanged() {
        this.mMainQueue.removeMessages(1);
        if (this.mServiceConnection.isConnected()) {
            this.mWorkerQueue.post(new 6(this));
        } else if (!this.mNotifyDataSetChangedAfterOnServiceConnected) {
            this.mNotifyDataSetChangedAfterOnServiceConnected = true;
            requestBindService();
        }
    }

    /* access modifiers changed from: package-private */
    public void superNotifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                if (this.mServiceConnection.isConnected()) {
                    this.mServiceConnection.unbind(this.mContext, this.mAppWidgetId, this.mIntent);
                }
                return true;
            default:
                return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void enqueueDeferredUnbindServiceMessage() {
        this.mMainQueue.removeMessages(1);
        this.mMainQueue.sendEmptyMessageDelayed(1, 5000);
    }

    private boolean requestBindService() {
        if (!this.mServiceConnection.isConnected()) {
            this.mServiceConnection.bind(this.mContext, this.mAppWidgetId, this.mIntent);
        }
        this.mMainQueue.removeMessages(1);
        return this.mServiceConnection.isConnected();
    }
}

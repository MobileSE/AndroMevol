package android.appwidget;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.IntentSender;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.RemoteViews;
import com.android.internal.appwidget.IAppWidgetHost;
import com.android.internal.appwidget.IAppWidgetService;
import java.util.ArrayList;
import java.util.HashMap;

public class AppWidgetHost {
    static final int HANDLE_PROVIDERS_CHANGED = 3;
    static final int HANDLE_PROVIDER_CHANGED = 2;
    static final int HANDLE_UPDATE = 1;
    static final int HANDLE_VIEW_DATA_CHANGED = 4;
    static IAppWidgetService sService;
    static final Object sServiceLock = new Object();
    Callbacks mCallbacks;
    Context mContext;
    private DisplayMetrics mDisplayMetrics;
    Handler mHandler;
    int mHostId;
    private RemoteViews.OnClickHandler mOnClickHandler;
    final HashMap<Integer, AppWidgetHostView> mViews;

    class Callbacks extends IAppWidgetHost.Stub {
        Callbacks() {
        }

        @Override // com.android.internal.appwidget.IAppWidgetHost
        public void updateAppWidget(int appWidgetId, RemoteViews views) {
            if (AppWidgetHost.this.isLocalBinder() && views != null) {
                views = views.clone();
            }
            AppWidgetHost.this.mHandler.obtainMessage(1, appWidgetId, 0, views).sendToTarget();
        }

        @Override // com.android.internal.appwidget.IAppWidgetHost
        public void providerChanged(int appWidgetId, AppWidgetProviderInfo info) {
            if (AppWidgetHost.this.isLocalBinder() && info != null) {
                info = info.clone();
            }
            AppWidgetHost.this.mHandler.obtainMessage(2, appWidgetId, 0, info).sendToTarget();
        }

        @Override // com.android.internal.appwidget.IAppWidgetHost
        public void providersChanged() {
            AppWidgetHost.this.mHandler.obtainMessage(3).sendToTarget();
        }

        @Override // com.android.internal.appwidget.IAppWidgetHost
        public void viewDataChanged(int appWidgetId, int viewId) {
            AppWidgetHost.this.mHandler.obtainMessage(4, appWidgetId, viewId).sendToTarget();
        }
    }

    class UpdateHandler extends Handler {
        public UpdateHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    AppWidgetHost.this.updateAppWidgetView(msg.arg1, (RemoteViews) msg.obj);
                    return;
                case 2:
                    AppWidgetHost.this.onProviderChanged(msg.arg1, (AppWidgetProviderInfo) msg.obj);
                    return;
                case 3:
                    AppWidgetHost.this.onProvidersChanged();
                    return;
                case 4:
                    AppWidgetHost.this.viewDataChanged(msg.arg1, msg.arg2);
                    return;
                default:
                    return;
            }
        }
    }

    public AppWidgetHost(Context context, int hostId) {
        this(context, hostId, null, context.getMainLooper());
    }

    public AppWidgetHost(Context context, int hostId, RemoteViews.OnClickHandler handler, Looper looper) {
        this.mCallbacks = new Callbacks();
        this.mViews = new HashMap<>();
        this.mContext = context;
        this.mHostId = hostId;
        this.mOnClickHandler = handler;
        this.mHandler = new UpdateHandler(looper);
        this.mDisplayMetrics = context.getResources().getDisplayMetrics();
        bindService();
    }

    private static void bindService() {
        synchronized (sServiceLock) {
            if (sService == null) {
                sService = IAppWidgetService.Stub.asInterface(ServiceManager.getService("appwidget"));
            }
        }
    }

    public void startListening() {
        ArrayList<RemoteViews> updatedViews = new ArrayList<>();
        try {
            int[] updatedIds = sService.startListening(this.mCallbacks, this.mContext.getOpPackageName(), this.mHostId, updatedViews);
            int N = updatedIds.length;
            for (int i = 0; i < N; i++) {
                updateAppWidgetView(updatedIds[i], updatedViews.get(i));
            }
        } catch (RemoteException e) {
            throw new RuntimeException("system server dead?", e);
        }
    }

    public void stopListening() {
        try {
            sService.stopListening(this.mContext.getOpPackageName(), this.mHostId);
            clearViews();
        } catch (RemoteException e) {
            throw new RuntimeException("system server dead?", e);
        }
    }

    public int allocateAppWidgetId() {
        try {
            return sService.allocateAppWidgetId(this.mContext.getOpPackageName(), this.mHostId);
        } catch (RemoteException e) {
            throw new RuntimeException("system server dead?", e);
        }
    }

    public final void startAppWidgetConfigureActivityForResult(Activity activity, int appWidgetId, int intentFlags, int requestCode, Bundle options) {
        try {
            IntentSender intentSender = sService.createAppWidgetConfigIntentSender(this.mContext.getOpPackageName(), appWidgetId, intentFlags);
            if (intentSender != null) {
                activity.startIntentSenderForResult(intentSender, requestCode, null, 0, 0, 0, options);
                return;
            }
            throw new ActivityNotFoundException();
        } catch (IntentSender.SendIntentException e) {
            throw new ActivityNotFoundException();
        } catch (RemoteException e2) {
            throw new RuntimeException("system server dead?", e2);
        }
    }

    public int[] getAppWidgetIds() {
        try {
            if (sService == null) {
                bindService();
            }
            return sService.getAppWidgetIdsForHost(this.mContext.getOpPackageName(), this.mHostId);
        } catch (RemoteException e) {
            throw new RuntimeException("system server dead?", e);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isLocalBinder() {
        return Process.myPid() == Binder.getCallingPid();
    }

    public void deleteAppWidgetId(int appWidgetId) {
        synchronized (this.mViews) {
            this.mViews.remove(Integer.valueOf(appWidgetId));
            try {
                sService.deleteAppWidgetId(this.mContext.getOpPackageName(), appWidgetId);
            } catch (RemoteException e) {
                throw new RuntimeException("system server dead?", e);
            }
        }
    }

    public void deleteHost() {
        try {
            sService.deleteHost(this.mContext.getOpPackageName(), this.mHostId);
        } catch (RemoteException e) {
            throw new RuntimeException("system server dead?", e);
        }
    }

    public static void deleteAllHosts() {
        try {
            sService.deleteAllHosts();
        } catch (RemoteException e) {
            throw new RuntimeException("system server dead?", e);
        }
    }

    public final AppWidgetHostView createView(Context context, int appWidgetId, AppWidgetProviderInfo appWidget) {
        AppWidgetHostView view = onCreateView(this.mContext, appWidgetId, appWidget);
        view.setOnClickHandler(this.mOnClickHandler);
        view.setAppWidget(appWidgetId, appWidget);
        synchronized (this.mViews) {
            this.mViews.put(Integer.valueOf(appWidgetId), view);
        }
        try {
            view.updateAppWidget(sService.getAppWidgetViews(this.mContext.getOpPackageName(), appWidgetId));
            return view;
        } catch (RemoteException e) {
            throw new RuntimeException("system server dead?", e);
        }
    }

    /* access modifiers changed from: protected */
    public AppWidgetHostView onCreateView(Context context, int appWidgetId, AppWidgetProviderInfo appWidget) {
        return new AppWidgetHostView(context, this.mOnClickHandler);
    }

    /* access modifiers changed from: protected */
    public void onProviderChanged(int appWidgetId, AppWidgetProviderInfo appWidget) {
        AppWidgetHostView v;
        appWidget.minWidth = TypedValue.complexToDimensionPixelSize(appWidget.minWidth, this.mDisplayMetrics);
        appWidget.minHeight = TypedValue.complexToDimensionPixelSize(appWidget.minHeight, this.mDisplayMetrics);
        appWidget.minResizeWidth = TypedValue.complexToDimensionPixelSize(appWidget.minResizeWidth, this.mDisplayMetrics);
        appWidget.minResizeHeight = TypedValue.complexToDimensionPixelSize(appWidget.minResizeHeight, this.mDisplayMetrics);
        synchronized (this.mViews) {
            v = this.mViews.get(Integer.valueOf(appWidgetId));
        }
        if (v != null) {
            v.resetAppWidget(appWidget);
        }
    }

    /* access modifiers changed from: protected */
    public void onProvidersChanged() {
    }

    /* access modifiers changed from: package-private */
    public void updateAppWidgetView(int appWidgetId, RemoteViews views) {
        AppWidgetHostView v;
        synchronized (this.mViews) {
            v = this.mViews.get(Integer.valueOf(appWidgetId));
        }
        if (v != null) {
            v.updateAppWidget(views);
        }
    }

    /* access modifiers changed from: package-private */
    public void viewDataChanged(int appWidgetId, int viewId) {
        AppWidgetHostView v;
        synchronized (this.mViews) {
            v = this.mViews.get(Integer.valueOf(appWidgetId));
        }
        if (v != null) {
            v.viewDataChanged(viewId);
        }
    }

    /* access modifiers changed from: protected */
    public void clearViews() {
        this.mViews.clear();
    }
}

package android.widget;

public interface RemoteViewsService$RemoteViewsFactory {
    int getCount();

    long getItemId(int i);

    RemoteViews getLoadingView();

    RemoteViews getViewAt(int i);

    int getViewTypeCount();

    boolean hasStableIds();

    void onCreate();

    void onDataSetChanged();

    void onDestroy();
}

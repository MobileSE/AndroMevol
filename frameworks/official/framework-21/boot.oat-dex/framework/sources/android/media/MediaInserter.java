package android.media;

import android.content.ContentValues;
import android.content.IContentProvider;
import android.net.Uri;
import android.os.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MediaInserter {
    private final int mBufferSizePerUri;
    private final String mPackageName;
    private final HashMap<Uri, List<ContentValues>> mPriorityRowMap = new HashMap<>();
    private final IContentProvider mProvider;
    private final HashMap<Uri, List<ContentValues>> mRowMap = new HashMap<>();

    public MediaInserter(IContentProvider provider, String packageName, int bufferSizePerUri) {
        this.mProvider = provider;
        this.mPackageName = packageName;
        this.mBufferSizePerUri = bufferSizePerUri;
    }

    public void insert(Uri tableUri, ContentValues values) throws RemoteException {
        insert(tableUri, values, false);
    }

    public void insertwithPriority(Uri tableUri, ContentValues values) throws RemoteException {
        insert(tableUri, values, true);
    }

    private void insert(Uri tableUri, ContentValues values, boolean priority) throws RemoteException {
        HashMap<Uri, List<ContentValues>> rowmap = priority ? this.mPriorityRowMap : this.mRowMap;
        List<ContentValues> list = rowmap.get(tableUri);
        if (list == null) {
            list = new ArrayList<>();
            rowmap.put(tableUri, list);
        }
        list.add(new ContentValues(values));
        if (list.size() >= this.mBufferSizePerUri) {
            flushAllPriority();
            flush(tableUri, list);
        }
    }

    public void flushAll() throws RemoteException {
        flushAllPriority();
        for (Uri tableUri : this.mRowMap.keySet()) {
            flush(tableUri, this.mRowMap.get(tableUri));
        }
        this.mRowMap.clear();
    }

    private void flushAllPriority() throws RemoteException {
        for (Uri tableUri : this.mPriorityRowMap.keySet()) {
            flush(tableUri, this.mPriorityRowMap.get(tableUri));
        }
        this.mPriorityRowMap.clear();
    }

    private void flush(Uri tableUri, List<ContentValues> list) throws RemoteException {
        if (!list.isEmpty()) {
            this.mProvider.bulkInsert(this.mPackageName, tableUri, (ContentValues[]) list.toArray(new ContentValues[list.size()]));
            list.clear();
        }
    }
}

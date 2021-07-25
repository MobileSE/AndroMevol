package android.widget;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.widget.RemoteViews;
import java.util.ArrayList;

/* access modifiers changed from: private */
public class RemoteViews$BitmapCache {
    ArrayList<Bitmap> mBitmaps = new ArrayList<>();

    public RemoteViews$BitmapCache() {
    }

    public RemoteViews$BitmapCache(Parcel source) {
        int count = source.readInt();
        for (int i = 0; i < count; i++) {
            this.mBitmaps.add(Bitmap.CREATOR.createFromParcel(source));
        }
    }

    public int getBitmapId(Bitmap b) {
        if (b == null) {
            return -1;
        }
        if (this.mBitmaps.contains(b)) {
            return this.mBitmaps.indexOf(b);
        }
        this.mBitmaps.add(b);
        return this.mBitmaps.size() - 1;
    }

    public Bitmap getBitmapForId(int id) {
        if (id == -1 || id >= this.mBitmaps.size()) {
            return null;
        }
        return this.mBitmaps.get(id);
    }

    public void writeBitmapsToParcel(Parcel dest, int flags) {
        int count = this.mBitmaps.size();
        dest.writeInt(count);
        for (int i = 0; i < count; i++) {
            this.mBitmaps.get(i).writeToParcel(dest, flags);
        }
    }

    public void assimilate(RemoteViews$BitmapCache bitmapCache) {
        ArrayList<Bitmap> bitmapsToBeAdded = bitmapCache.mBitmaps;
        int count = bitmapsToBeAdded.size();
        for (int i = 0; i < count; i++) {
            Bitmap b = bitmapsToBeAdded.get(i);
            if (!this.mBitmaps.contains(b)) {
                this.mBitmaps.add(b);
            }
        }
    }

    public void addBitmapMemory(RemoteViews.MemoryUsageCounter memoryCounter) {
        for (int i = 0; i < this.mBitmaps.size(); i++) {
            memoryCounter.addBitmapMemory(this.mBitmaps.get(i));
        }
    }
}

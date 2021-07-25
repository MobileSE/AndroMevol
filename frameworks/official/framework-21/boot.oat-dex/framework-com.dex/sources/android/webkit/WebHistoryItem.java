package android.webkit;

import android.graphics.Bitmap;

public class WebHistoryItem implements Cloneable {
    @Deprecated
    public int getId() {
        throw new MustOverrideException();
    }

    public String getUrl() {
        throw new MustOverrideException();
    }

    public String getOriginalUrl() {
        throw new MustOverrideException();
    }

    public String getTitle() {
        throw new MustOverrideException();
    }

    public Bitmap getFavicon() {
        throw new MustOverrideException();
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public synchronized WebHistoryItem clone() {
        throw new MustOverrideException();
    }
}

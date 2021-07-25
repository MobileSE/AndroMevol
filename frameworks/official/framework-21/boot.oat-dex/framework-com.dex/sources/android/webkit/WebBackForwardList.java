package android.webkit;

import java.io.Serializable;

public class WebBackForwardList implements Cloneable, Serializable {
    public synchronized WebHistoryItem getCurrentItem() {
        throw new MustOverrideException();
    }

    public synchronized int getCurrentIndex() {
        throw new MustOverrideException();
    }

    public synchronized WebHistoryItem getItemAtIndex(int index) {
        throw new MustOverrideException();
    }

    public synchronized int getSize() {
        throw new MustOverrideException();
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public synchronized WebBackForwardList clone() {
        throw new MustOverrideException();
    }
}

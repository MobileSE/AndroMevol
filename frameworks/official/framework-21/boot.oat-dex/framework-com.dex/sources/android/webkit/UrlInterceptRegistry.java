package android.webkit;

import android.webkit.CacheManager;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

@Deprecated
public final class UrlInterceptRegistry {
    private static final String LOGTAG = "intercept";
    private static boolean mDisabled = false;
    private static LinkedList mHandlerList;

    private static synchronized LinkedList getHandlers() {
        LinkedList linkedList;
        synchronized (UrlInterceptRegistry.class) {
            if (mHandlerList == null) {
                mHandlerList = new LinkedList();
            }
            linkedList = mHandlerList;
        }
        return linkedList;
    }

    @Deprecated
    public static synchronized void setUrlInterceptDisabled(boolean disabled) {
        synchronized (UrlInterceptRegistry.class) {
            mDisabled = disabled;
        }
    }

    @Deprecated
    public static synchronized boolean urlInterceptDisabled() {
        boolean z;
        synchronized (UrlInterceptRegistry.class) {
            z = mDisabled;
        }
        return z;
    }

    @Deprecated
    public static synchronized boolean registerHandler(UrlInterceptHandler handler) {
        boolean z;
        synchronized (UrlInterceptRegistry.class) {
            if (!getHandlers().contains(handler)) {
                getHandlers().addFirst(handler);
                z = true;
            } else {
                z = false;
            }
        }
        return z;
    }

    @Deprecated
    public static synchronized boolean unregisterHandler(UrlInterceptHandler handler) {
        boolean remove;
        synchronized (UrlInterceptRegistry.class) {
            remove = getHandlers().remove(handler);
        }
        return remove;
    }

    @Deprecated
    public static synchronized CacheManager.CacheResult getSurrogate(String url, Map<String, String> headers) {
        CacheManager.CacheResult result;
        synchronized (UrlInterceptRegistry.class) {
            if (!urlInterceptDisabled()) {
                Iterator iter = getHandlers().listIterator();
                while (true) {
                    if (!iter.hasNext()) {
                        result = null;
                        break;
                    }
                    result = ((UrlInterceptHandler) iter.next()).service(url, headers);
                    if (result != null) {
                        break;
                    }
                }
            } else {
                result = null;
            }
        }
        return result;
    }

    @Deprecated
    public static synchronized PluginData getPluginData(String url, Map<String, String> headers) {
        PluginData data;
        synchronized (UrlInterceptRegistry.class) {
            if (!urlInterceptDisabled()) {
                Iterator iter = getHandlers().listIterator();
                while (true) {
                    if (!iter.hasNext()) {
                        data = null;
                        break;
                    }
                    data = ((UrlInterceptHandler) iter.next()).getPluginData(url, headers);
                    if (data != null) {
                        break;
                    }
                }
            } else {
                data = null;
            }
        }
        return data;
    }
}

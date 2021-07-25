package android.net.http;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.CacheRequest;
import java.net.CacheResponse;
import java.net.ResponseCache;
import java.net.URI;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

public final class HttpResponseCache extends ResponseCache implements Closeable {
    private final com.android.okhttp.HttpResponseCache delegate;

    private HttpResponseCache(com.android.okhttp.HttpResponseCache delegate2) {
        this.delegate = delegate2;
    }

    public static HttpResponseCache getInstalled() {
        ResponseCache installed = ResponseCache.getDefault();
        if (installed instanceof com.android.okhttp.HttpResponseCache) {
            return new HttpResponseCache((com.android.okhttp.HttpResponseCache) installed);
        }
        return null;
    }

    public static HttpResponseCache install(File directory, long maxSize) throws IOException {
        ResponseCache installed = ResponseCache.getDefault();
        if (installed instanceof com.android.okhttp.HttpResponseCache) {
            com.android.okhttp.HttpResponseCache installedCache = (com.android.okhttp.HttpResponseCache) installed;
            if (installedCache.getDirectory().equals(directory) && installedCache.getMaxSize() == maxSize && !installedCache.isClosed()) {
                return new HttpResponseCache(installedCache);
            }
            installedCache.close();
        }
        com.android.okhttp.HttpResponseCache responseCache = new com.android.okhttp.HttpResponseCache(directory, maxSize);
        ResponseCache.setDefault(responseCache);
        return new HttpResponseCache(responseCache);
    }

    @Override // java.net.ResponseCache
    public CacheResponse get(URI uri, String requestMethod, Map<String, List<String>> requestHeaders) throws IOException {
        return this.delegate.get(uri, requestMethod, requestHeaders);
    }

    @Override // java.net.ResponseCache
    public CacheRequest put(URI uri, URLConnection urlConnection) throws IOException {
        return this.delegate.put(uri, urlConnection);
    }

    public long size() {
        return this.delegate.getSize();
    }

    public long maxSize() {
        return this.delegate.getMaxSize();
    }

    public void flush() {
        try {
            this.delegate.flush();
        } catch (IOException e) {
        }
    }

    public int getNetworkCount() {
        return this.delegate.getNetworkCount();
    }

    public int getHitCount() {
        return this.delegate.getHitCount();
    }

    public int getRequestCount() {
        return this.delegate.getRequestCount();
    }

    @Override // java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        if (ResponseCache.getDefault() == this.delegate) {
            ResponseCache.setDefault(null);
        }
        this.delegate.close();
    }

    public void delete() throws IOException {
        if (ResponseCache.getDefault() == this.delegate) {
            ResponseCache.setDefault(null);
        }
        this.delegate.delete();
    }
}

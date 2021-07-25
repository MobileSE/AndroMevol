package android.webkit;

import android.content.ContentResolver;
import android.graphics.Bitmap;

@Deprecated
public class WebIconDatabase {

    @Deprecated
    public interface IconListener {
        void onReceivedIcon(String str, Bitmap bitmap);
    }

    public void open(String path) {
        throw new MustOverrideException();
    }

    public void close() {
        throw new MustOverrideException();
    }

    public void removeAllIcons() {
        throw new MustOverrideException();
    }

    public void requestIconForPageUrl(String url, IconListener listener) {
        throw new MustOverrideException();
    }

    public void bulkRequestIconForPageUrl(ContentResolver cr, String where, IconListener listener) {
        throw new MustOverrideException();
    }

    public void retainIconForPageUrl(String url) {
        throw new MustOverrideException();
    }

    public void releaseIconForPageUrl(String url) {
        throw new MustOverrideException();
    }

    public static WebIconDatabase getInstance() {
        return WebViewFactory.getProvider().getWebIconDatabase();
    }

    protected WebIconDatabase() {
    }
}

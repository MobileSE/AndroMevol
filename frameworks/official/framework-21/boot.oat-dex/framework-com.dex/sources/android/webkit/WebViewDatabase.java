package android.webkit;

import android.content.Context;

public class WebViewDatabase {
    protected static final String LOGTAG = "webviewdatabase";

    protected WebViewDatabase() {
    }

    public static WebViewDatabase getInstance(Context context) {
        return WebViewFactory.getProvider().getWebViewDatabase(context);
    }

    @Deprecated
    public boolean hasUsernamePassword() {
        throw new MustOverrideException();
    }

    @Deprecated
    public void clearUsernamePassword() {
        throw new MustOverrideException();
    }

    public boolean hasHttpAuthUsernamePassword() {
        throw new MustOverrideException();
    }

    public void clearHttpAuthUsernamePassword() {
        throw new MustOverrideException();
    }

    public boolean hasFormData() {
        throw new MustOverrideException();
    }

    public void clearFormData() {
        throw new MustOverrideException();
    }
}

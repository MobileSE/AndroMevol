package android.webkit;

import android.net.WebAddress;

public class CookieManager {
    protected CookieManager() {
    }

    /* access modifiers changed from: protected */
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("doesn't implement Cloneable");
    }

    public static synchronized CookieManager getInstance() {
        CookieManager cookieManager;
        synchronized (CookieManager.class) {
            cookieManager = WebViewFactory.getProvider().getCookieManager();
        }
        return cookieManager;
    }

    public synchronized void setAcceptCookie(boolean accept) {
        throw new MustOverrideException();
    }

    public synchronized boolean acceptCookie() {
        throw new MustOverrideException();
    }

    public void setAcceptThirdPartyCookies(WebView webview, boolean accept) {
        throw new MustOverrideException();
    }

    public boolean acceptThirdPartyCookies(WebView webview) {
        throw new MustOverrideException();
    }

    public void setCookie(String url, String value) {
        throw new MustOverrideException();
    }

    public void setCookie(String url, String value, ValueCallback<Boolean> valueCallback) {
        throw new MustOverrideException();
    }

    public String getCookie(String url) {
        throw new MustOverrideException();
    }

    public String getCookie(String url, boolean privateBrowsing) {
        throw new MustOverrideException();
    }

    public synchronized String getCookie(WebAddress uri) {
        throw new MustOverrideException();
    }

    public void removeSessionCookie() {
        throw new MustOverrideException();
    }

    public void removeSessionCookies(ValueCallback<Boolean> valueCallback) {
        throw new MustOverrideException();
    }

    @Deprecated
    public void removeAllCookie() {
        throw new MustOverrideException();
    }

    public void removeAllCookies(ValueCallback<Boolean> valueCallback) {
        throw new MustOverrideException();
    }

    public synchronized boolean hasCookies() {
        throw new MustOverrideException();
    }

    public synchronized boolean hasCookies(boolean privateBrowsing) {
        throw new MustOverrideException();
    }

    @Deprecated
    public void removeExpiredCookie() {
        throw new MustOverrideException();
    }

    public void flush() {
        flushCookieStore();
    }

    /* access modifiers changed from: protected */
    public void flushCookieStore() {
    }

    public static boolean allowFileSchemeCookies() {
        return getInstance().allowFileSchemeCookiesImpl();
    }

    /* access modifiers changed from: protected */
    public boolean allowFileSchemeCookiesImpl() {
        throw new MustOverrideException();
    }

    public static void setAcceptFileSchemeCookies(boolean accept) {
        getInstance().setAcceptFileSchemeCookiesImpl(accept);
    }

    /* access modifiers changed from: protected */
    public void setAcceptFileSchemeCookiesImpl(boolean accept) {
        throw new MustOverrideException();
    }
}

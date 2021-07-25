package android.view;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.content.res.Resources;

public class ContextThemeWrapper extends ContextWrapper {
    private LayoutInflater mInflater;
    private Configuration mOverrideConfiguration;
    private Resources mResources;
    private Resources.Theme mTheme;
    private int mThemeResource;

    public ContextThemeWrapper() {
        super(null);
    }

    public ContextThemeWrapper(Context base, int themeres) {
        super(base);
        this.mThemeResource = themeres;
    }

    /* access modifiers changed from: protected */
    @Override // android.content.ContextWrapper
    public void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
    }

    public void applyOverrideConfiguration(Configuration overrideConfiguration) {
        if (this.mResources != null) {
            throw new IllegalStateException("getResources() has already been called");
        } else if (this.mOverrideConfiguration != null) {
            throw new IllegalStateException("Override configuration has already been set");
        } else {
            this.mOverrideConfiguration = new Configuration(overrideConfiguration);
        }
    }

    @Override // android.content.Context, android.content.ContextWrapper
    public Resources getResources() {
        if (this.mResources != null) {
            return this.mResources;
        }
        if (this.mOverrideConfiguration == null) {
            this.mResources = super.getResources();
            return this.mResources;
        }
        this.mResources = createConfigurationContext(this.mOverrideConfiguration).getResources();
        return this.mResources;
    }

    @Override // android.content.Context, android.content.ContextWrapper
    public void setTheme(int resid) {
        this.mThemeResource = resid;
        initializeTheme();
    }

    @Override // android.content.Context, android.content.ContextWrapper
    public int getThemeResId() {
        return this.mThemeResource;
    }

    @Override // android.content.Context, android.content.ContextWrapper
    public Resources.Theme getTheme() {
        if (this.mTheme != null) {
            return this.mTheme;
        }
        this.mThemeResource = Resources.selectDefaultTheme(this.mThemeResource, getApplicationInfo().targetSdkVersion);
        initializeTheme();
        return this.mTheme;
    }

    @Override // android.content.Context, android.content.ContextWrapper
    public Object getSystemService(String name) {
        if (!Context.LAYOUT_INFLATER_SERVICE.equals(name)) {
            return getBaseContext().getSystemService(name);
        }
        if (this.mInflater == null) {
            this.mInflater = LayoutInflater.from(getBaseContext()).cloneInContext(this);
        }
        return this.mInflater;
    }

    /* access modifiers changed from: protected */
    public void onApplyThemeResource(Resources.Theme theme, int resid, boolean first) {
        theme.applyStyle(resid, true);
    }

    private void initializeTheme() {
        boolean first = this.mTheme == null;
        if (first) {
            this.mTheme = getResources().newTheme();
            Resources.Theme theme = getBaseContext().getTheme();
            if (theme != null) {
                this.mTheme.setTo(theme);
            }
        }
        onApplyThemeResource(this.mTheme, this.mThemeResource, first);
    }
}

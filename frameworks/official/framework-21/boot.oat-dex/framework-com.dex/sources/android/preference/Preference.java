package android.preference;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.AbsSavedState;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.internal.R;
import com.android.internal.util.CharSequences;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Preference implements Comparable<Preference> {
    public static final int DEFAULT_ORDER = Integer.MAX_VALUE;
    private boolean mBaseMethodCalled;
    private boolean mCanRecycleLayout;
    private Context mContext;
    private Object mDefaultValue;
    private String mDependencyKey;
    private boolean mDependencyMet;
    private List<Preference> mDependents;
    private boolean mEnabled;
    private Bundle mExtras;
    private String mFragment;
    private Drawable mIcon;
    private int mIconResId;
    private long mId;
    private Intent mIntent;
    private String mKey;
    private int mLayoutResId;
    private OnPreferenceChangeInternalListener mListener;
    private OnPreferenceChangeListener mOnChangeListener;
    private OnPreferenceClickListener mOnClickListener;
    private int mOrder;
    private boolean mParentDependencyMet;
    private boolean mPersistent;
    private PreferenceManager mPreferenceManager;
    private boolean mRequiresKey;
    private boolean mSelectable;
    private boolean mShouldDisableView;
    private CharSequence mSummary;
    private CharSequence mTitle;
    private int mTitleRes;
    private int mWidgetLayoutResId;

    /* access modifiers changed from: package-private */
    public interface OnPreferenceChangeInternalListener {
        void onPreferenceChange(Preference preference);

        void onPreferenceHierarchyChange(Preference preference);
    }

    public interface OnPreferenceChangeListener {
        boolean onPreferenceChange(Preference preference, Object obj);
    }

    public interface OnPreferenceClickListener {
        boolean onPreferenceClick(Preference preference);
    }

    public Preference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this.mOrder = Integer.MAX_VALUE;
        this.mEnabled = true;
        this.mSelectable = true;
        this.mPersistent = true;
        this.mDependencyMet = true;
        this.mParentDependencyMet = true;
        this.mShouldDisableView = true;
        this.mLayoutResId = R.layout.preference;
        this.mCanRecycleLayout = true;
        this.mContext = context;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Preference, defStyleAttr, defStyleRes);
        for (int i = a.getIndexCount() - 1; i >= 0; i--) {
            int attr = a.getIndex(i);
            switch (attr) {
                case 0:
                    this.mIconResId = a.getResourceId(attr, 0);
                    break;
                case 1:
                    this.mPersistent = a.getBoolean(attr, this.mPersistent);
                    break;
                case 2:
                    this.mEnabled = a.getBoolean(attr, true);
                    break;
                case 3:
                    this.mLayoutResId = a.getResourceId(attr, this.mLayoutResId);
                    break;
                case 4:
                    this.mTitleRes = a.getResourceId(attr, 0);
                    this.mTitle = a.getString(attr);
                    break;
                case 5:
                    this.mSelectable = a.getBoolean(attr, true);
                    break;
                case 6:
                    this.mKey = a.getString(attr);
                    break;
                case 7:
                    this.mSummary = a.getString(attr);
                    break;
                case 8:
                    this.mOrder = a.getInt(attr, this.mOrder);
                    break;
                case 9:
                    this.mWidgetLayoutResId = a.getResourceId(attr, this.mWidgetLayoutResId);
                    break;
                case 10:
                    this.mDependencyKey = a.getString(attr);
                    break;
                case 11:
                    this.mDefaultValue = onGetDefaultValue(a, attr);
                    break;
                case 12:
                    this.mShouldDisableView = a.getBoolean(attr, this.mShouldDisableView);
                    break;
                case 13:
                    this.mFragment = a.getString(attr);
                    break;
            }
        }
        a.recycle();
        if (!(getClass().getName().startsWith(PreferenceManager.METADATA_KEY_PREFERENCES) || getClass().getName().startsWith("com.android"))) {
            this.mCanRecycleLayout = false;
        }
    }

    public Preference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public Preference(Context context, AttributeSet attrs) {
        this(context, attrs, 16842894);
    }

    public Preference(Context context) {
        this(context, null);
    }

    /* access modifiers changed from: protected */
    public Object onGetDefaultValue(TypedArray a, int index) {
        return null;
    }

    public void setIntent(Intent intent) {
        this.mIntent = intent;
    }

    public Intent getIntent() {
        return this.mIntent;
    }

    public void setFragment(String fragment) {
        this.mFragment = fragment;
    }

    public String getFragment() {
        return this.mFragment;
    }

    public Bundle getExtras() {
        if (this.mExtras == null) {
            this.mExtras = new Bundle();
        }
        return this.mExtras;
    }

    public Bundle peekExtras() {
        return this.mExtras;
    }

    public void setLayoutResource(int layoutResId) {
        if (layoutResId != this.mLayoutResId) {
            this.mCanRecycleLayout = false;
        }
        this.mLayoutResId = layoutResId;
    }

    public int getLayoutResource() {
        return this.mLayoutResId;
    }

    public void setWidgetLayoutResource(int widgetLayoutResId) {
        if (widgetLayoutResId != this.mWidgetLayoutResId) {
            this.mCanRecycleLayout = false;
        }
        this.mWidgetLayoutResId = widgetLayoutResId;
    }

    public int getWidgetLayoutResource() {
        return this.mWidgetLayoutResId;
    }

    public View getView(View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = onCreateView(parent);
        }
        onBindView(convertView);
        return convertView;
    }

    /* access modifiers changed from: protected */
    public View onCreateView(ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(this.mLayoutResId, parent, false);
        ViewGroup widgetFrame = (ViewGroup) layout.findViewById(16908312);
        if (widgetFrame != null) {
            if (this.mWidgetLayoutResId != 0) {
                layoutInflater.inflate(this.mWidgetLayoutResId, widgetFrame);
            } else {
                widgetFrame.setVisibility(8);
            }
        }
        return layout;
    }

    /* access modifiers changed from: protected */
    public void onBindView(View view) {
        int i;
        int i2 = 0;
        TextView titleView = (TextView) view.findViewById(16908310);
        if (titleView != null) {
            CharSequence title = getTitle();
            if (!TextUtils.isEmpty(title)) {
                titleView.setText(title);
                titleView.setVisibility(0);
            } else {
                titleView.setVisibility(8);
            }
        }
        TextView summaryView = (TextView) view.findViewById(16908304);
        if (summaryView != null) {
            CharSequence summary = getSummary();
            if (!TextUtils.isEmpty(summary)) {
                summaryView.setText(summary);
                summaryView.setVisibility(0);
            } else {
                summaryView.setVisibility(8);
            }
        }
        ImageView imageView = (ImageView) view.findViewById(16908294);
        if (imageView != null) {
            if (!(this.mIconResId == 0 && this.mIcon == null)) {
                if (this.mIcon == null) {
                    this.mIcon = getContext().getDrawable(this.mIconResId);
                }
                if (this.mIcon != null) {
                    imageView.setImageDrawable(this.mIcon);
                }
            }
            if (this.mIcon != null) {
                i = 0;
            } else {
                i = 8;
            }
            imageView.setVisibility(i);
        }
        View imageFrame = view.findViewById(R.id.icon_frame);
        if (imageFrame != null) {
            if (this.mIcon == null) {
                i2 = 8;
            }
            imageFrame.setVisibility(i2);
        }
        if (this.mShouldDisableView) {
            setEnabledStateOnViews(view, isEnabled());
        }
    }

    private void setEnabledStateOnViews(View v, boolean enabled) {
        v.setEnabled(enabled);
        if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            for (int i = vg.getChildCount() - 1; i >= 0; i--) {
                setEnabledStateOnViews(vg.getChildAt(i), enabled);
            }
        }
    }

    public void setOrder(int order) {
        if (order != this.mOrder) {
            this.mOrder = order;
            notifyHierarchyChanged();
        }
    }

    public int getOrder() {
        return this.mOrder;
    }

    public void setTitle(CharSequence title) {
        if ((title == null && this.mTitle != null) || (title != null && !title.equals(this.mTitle))) {
            this.mTitleRes = 0;
            this.mTitle = title;
            notifyChanged();
        }
    }

    public void setTitle(int titleResId) {
        setTitle(this.mContext.getString(titleResId));
        this.mTitleRes = titleResId;
    }

    public int getTitleRes() {
        return this.mTitleRes;
    }

    public CharSequence getTitle() {
        return this.mTitle;
    }

    public void setIcon(Drawable icon) {
        if ((icon == null && this.mIcon != null) || (icon != null && this.mIcon != icon)) {
            this.mIcon = icon;
            notifyChanged();
        }
    }

    public void setIcon(int iconResId) {
        this.mIconResId = iconResId;
        setIcon(this.mContext.getDrawable(iconResId));
    }

    public Drawable getIcon() {
        return this.mIcon;
    }

    public CharSequence getSummary() {
        return this.mSummary;
    }

    public void setSummary(CharSequence summary) {
        if ((summary == null && this.mSummary != null) || (summary != null && !summary.equals(this.mSummary))) {
            this.mSummary = summary;
            notifyChanged();
        }
    }

    public void setSummary(int summaryResId) {
        setSummary(this.mContext.getString(summaryResId));
    }

    public void setEnabled(boolean enabled) {
        if (this.mEnabled != enabled) {
            this.mEnabled = enabled;
            notifyDependencyChange(shouldDisableDependents());
            notifyChanged();
        }
    }

    public boolean isEnabled() {
        return this.mEnabled && this.mDependencyMet && this.mParentDependencyMet;
    }

    public void setSelectable(boolean selectable) {
        if (this.mSelectable != selectable) {
            this.mSelectable = selectable;
            notifyChanged();
        }
    }

    public boolean isSelectable() {
        return this.mSelectable;
    }

    public void setShouldDisableView(boolean shouldDisableView) {
        this.mShouldDisableView = shouldDisableView;
        notifyChanged();
    }

    public boolean getShouldDisableView() {
        return this.mShouldDisableView;
    }

    /* access modifiers changed from: package-private */
    public long getId() {
        return this.mId;
    }

    /* access modifiers changed from: protected */
    public void onClick() {
    }

    public void setKey(String key) {
        this.mKey = key;
        if (this.mRequiresKey && !hasKey()) {
            requireKey();
        }
    }

    public String getKey() {
        return this.mKey;
    }

    /* access modifiers changed from: package-private */
    public void requireKey() {
        if (this.mKey == null) {
            throw new IllegalStateException("Preference does not have a key assigned.");
        }
        this.mRequiresKey = true;
    }

    public boolean hasKey() {
        return !TextUtils.isEmpty(this.mKey);
    }

    public boolean isPersistent() {
        return this.mPersistent;
    }

    /* access modifiers changed from: protected */
    public boolean shouldPersist() {
        return this.mPreferenceManager != null && isPersistent() && hasKey();
    }

    public void setPersistent(boolean persistent) {
        this.mPersistent = persistent;
    }

    /* access modifiers changed from: protected */
    public boolean callChangeListener(Object newValue) {
        if (this.mOnChangeListener == null) {
            return true;
        }
        return this.mOnChangeListener.onPreferenceChange(this, newValue);
    }

    public void setOnPreferenceChangeListener(OnPreferenceChangeListener onPreferenceChangeListener) {
        this.mOnChangeListener = onPreferenceChangeListener;
    }

    public OnPreferenceChangeListener getOnPreferenceChangeListener() {
        return this.mOnChangeListener;
    }

    public void setOnPreferenceClickListener(OnPreferenceClickListener onPreferenceClickListener) {
        this.mOnClickListener = onPreferenceClickListener;
    }

    public OnPreferenceClickListener getOnPreferenceClickListener() {
        return this.mOnClickListener;
    }

    public void performClick(PreferenceScreen preferenceScreen) {
        if (isEnabled()) {
            onClick();
            if (this.mOnClickListener == null || !this.mOnClickListener.onPreferenceClick(this)) {
                PreferenceManager preferenceManager = getPreferenceManager();
                if (preferenceManager != null) {
                    PreferenceManager.OnPreferenceTreeClickListener listener = preferenceManager.getOnPreferenceTreeClickListener();
                    if (!(preferenceScreen == null || listener == null || !listener.onPreferenceTreeClick(preferenceScreen, this))) {
                        return;
                    }
                }
                if (this.mIntent != null) {
                    getContext().startActivity(this.mIntent);
                }
            }
        }
    }

    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    public Context getContext() {
        return this.mContext;
    }

    public SharedPreferences getSharedPreferences() {
        if (this.mPreferenceManager == null) {
            return null;
        }
        return this.mPreferenceManager.getSharedPreferences();
    }

    public SharedPreferences.Editor getEditor() {
        if (this.mPreferenceManager == null) {
            return null;
        }
        return this.mPreferenceManager.getEditor();
    }

    public boolean shouldCommit() {
        if (this.mPreferenceManager == null) {
            return false;
        }
        return this.mPreferenceManager.shouldCommit();
    }

    public int compareTo(Preference another) {
        if (this.mOrder != another.mOrder) {
            return this.mOrder - another.mOrder;
        }
        if (this.mTitle == another.mTitle) {
            return 0;
        }
        if (this.mTitle == null) {
            return 1;
        }
        if (another.mTitle == null) {
            return -1;
        }
        return CharSequences.compareToIgnoreCase(this.mTitle, another.mTitle);
    }

    /* access modifiers changed from: package-private */
    public final void setOnPreferenceChangeInternalListener(OnPreferenceChangeInternalListener listener) {
        this.mListener = listener;
    }

    /* access modifiers changed from: protected */
    public void notifyChanged() {
        if (this.mListener != null) {
            this.mListener.onPreferenceChange(this);
        }
    }

    /* access modifiers changed from: protected */
    public void notifyHierarchyChanged() {
        if (this.mListener != null) {
            this.mListener.onPreferenceHierarchyChange(this);
        }
    }

    public PreferenceManager getPreferenceManager() {
        return this.mPreferenceManager;
    }

    /* access modifiers changed from: protected */
    public void onAttachedToHierarchy(PreferenceManager preferenceManager) {
        this.mPreferenceManager = preferenceManager;
        this.mId = preferenceManager.getNextId();
        dispatchSetInitialValue();
    }

    /* access modifiers changed from: protected */
    public void onAttachedToActivity() {
        registerDependency();
    }

    private void registerDependency() {
        if (!TextUtils.isEmpty(this.mDependencyKey)) {
            Preference preference = findPreferenceInHierarchy(this.mDependencyKey);
            if (preference != null) {
                preference.registerDependent(this);
                return;
            }
            throw new IllegalStateException("Dependency \"" + this.mDependencyKey + "\" not found for preference \"" + this.mKey + "\" (title: \"" + ((Object) this.mTitle) + "\"");
        }
    }

    private void unregisterDependency() {
        Preference oldDependency;
        if (this.mDependencyKey != null && (oldDependency = findPreferenceInHierarchy(this.mDependencyKey)) != null) {
            oldDependency.unregisterDependent(this);
        }
    }

    /* access modifiers changed from: protected */
    public Preference findPreferenceInHierarchy(String key) {
        if (TextUtils.isEmpty(key) || this.mPreferenceManager == null) {
            return null;
        }
        return this.mPreferenceManager.findPreference(key);
    }

    private void registerDependent(Preference dependent) {
        if (this.mDependents == null) {
            this.mDependents = new ArrayList();
        }
        this.mDependents.add(dependent);
        dependent.onDependencyChanged(this, shouldDisableDependents());
    }

    private void unregisterDependent(Preference dependent) {
        if (this.mDependents != null) {
            this.mDependents.remove(dependent);
        }
    }

    public void notifyDependencyChange(boolean disableDependents) {
        List<Preference> dependents = this.mDependents;
        if (dependents != null) {
            int dependentsCount = dependents.size();
            for (int i = 0; i < dependentsCount; i++) {
                dependents.get(i).onDependencyChanged(this, disableDependents);
            }
        }
    }

    public void onDependencyChanged(Preference dependency, boolean disableDependent) {
        if (this.mDependencyMet == disableDependent) {
            this.mDependencyMet = !disableDependent;
            notifyDependencyChange(shouldDisableDependents());
            notifyChanged();
        }
    }

    public void onParentChanged(Preference parent, boolean disableChild) {
        if (this.mParentDependencyMet == disableChild) {
            this.mParentDependencyMet = !disableChild;
            notifyDependencyChange(shouldDisableDependents());
            notifyChanged();
        }
    }

    public boolean shouldDisableDependents() {
        return !isEnabled();
    }

    public void setDependency(String dependencyKey) {
        unregisterDependency();
        this.mDependencyKey = dependencyKey;
        registerDependency();
    }

    public String getDependency() {
        return this.mDependencyKey;
    }

    /* access modifiers changed from: protected */
    public void onPrepareForRemoval() {
        unregisterDependency();
    }

    public void setDefaultValue(Object defaultValue) {
        this.mDefaultValue = defaultValue;
    }

    private void dispatchSetInitialValue() {
        if (shouldPersist() && getSharedPreferences().contains(this.mKey)) {
            onSetInitialValue(true, null);
        } else if (this.mDefaultValue != null) {
            onSetInitialValue(false, this.mDefaultValue);
        }
    }

    /* access modifiers changed from: protected */
    public void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
    }

    private void tryCommit(SharedPreferences.Editor editor) {
        if (this.mPreferenceManager.shouldCommit()) {
            try {
                editor.apply();
            } catch (AbstractMethodError e) {
                editor.commit();
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean persistString(String value) {
        if (!shouldPersist()) {
            return false;
        }
        if (value == getPersistedString(null)) {
            return true;
        }
        SharedPreferences.Editor editor = this.mPreferenceManager.getEditor();
        editor.putString(this.mKey, value);
        tryCommit(editor);
        return true;
    }

    /* access modifiers changed from: protected */
    public String getPersistedString(String defaultReturnValue) {
        return !shouldPersist() ? defaultReturnValue : this.mPreferenceManager.getSharedPreferences().getString(this.mKey, defaultReturnValue);
    }

    /* access modifiers changed from: protected */
    public boolean persistStringSet(Set<String> values) {
        if (!shouldPersist()) {
            return false;
        }
        if (values.equals(getPersistedStringSet(null))) {
            return true;
        }
        SharedPreferences.Editor editor = this.mPreferenceManager.getEditor();
        editor.putStringSet(this.mKey, values);
        tryCommit(editor);
        return true;
    }

    /* access modifiers changed from: protected */
    public Set<String> getPersistedStringSet(Set<String> defaultReturnValue) {
        return !shouldPersist() ? defaultReturnValue : this.mPreferenceManager.getSharedPreferences().getStringSet(this.mKey, defaultReturnValue);
    }

    /* access modifiers changed from: protected */
    public boolean persistInt(int value) {
        if (!shouldPersist()) {
            return false;
        }
        if (value == getPersistedInt(value ^ -1)) {
            return true;
        }
        SharedPreferences.Editor editor = this.mPreferenceManager.getEditor();
        editor.putInt(this.mKey, value);
        tryCommit(editor);
        return true;
    }

    /* access modifiers changed from: protected */
    public int getPersistedInt(int defaultReturnValue) {
        return !shouldPersist() ? defaultReturnValue : this.mPreferenceManager.getSharedPreferences().getInt(this.mKey, defaultReturnValue);
    }

    /* access modifiers changed from: protected */
    public boolean persistFloat(float value) {
        if (!shouldPersist()) {
            return false;
        }
        if (value == getPersistedFloat(Float.NaN)) {
            return true;
        }
        SharedPreferences.Editor editor = this.mPreferenceManager.getEditor();
        editor.putFloat(this.mKey, value);
        tryCommit(editor);
        return true;
    }

    /* access modifiers changed from: protected */
    public float getPersistedFloat(float defaultReturnValue) {
        return !shouldPersist() ? defaultReturnValue : this.mPreferenceManager.getSharedPreferences().getFloat(this.mKey, defaultReturnValue);
    }

    /* access modifiers changed from: protected */
    public boolean persistLong(long value) {
        if (!shouldPersist()) {
            return false;
        }
        if (value == getPersistedLong(-1 ^ value)) {
            return true;
        }
        SharedPreferences.Editor editor = this.mPreferenceManager.getEditor();
        editor.putLong(this.mKey, value);
        tryCommit(editor);
        return true;
    }

    /* access modifiers changed from: protected */
    public long getPersistedLong(long defaultReturnValue) {
        return !shouldPersist() ? defaultReturnValue : this.mPreferenceManager.getSharedPreferences().getLong(this.mKey, defaultReturnValue);
    }

    /* access modifiers changed from: protected */
    public boolean persistBoolean(boolean value) {
        boolean z = false;
        if (!shouldPersist()) {
            return false;
        }
        if (!value) {
            z = true;
        }
        if (value == getPersistedBoolean(z)) {
            return true;
        }
        SharedPreferences.Editor editor = this.mPreferenceManager.getEditor();
        editor.putBoolean(this.mKey, value);
        tryCommit(editor);
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean getPersistedBoolean(boolean defaultReturnValue) {
        return !shouldPersist() ? defaultReturnValue : this.mPreferenceManager.getSharedPreferences().getBoolean(this.mKey, defaultReturnValue);
    }

    /* access modifiers changed from: package-private */
    public boolean canRecycleLayout() {
        return this.mCanRecycleLayout;
    }

    public String toString() {
        return getFilterableStringBuilder().toString();
    }

    /* access modifiers changed from: package-private */
    public StringBuilder getFilterableStringBuilder() {
        StringBuilder sb = new StringBuilder();
        CharSequence title = getTitle();
        if (!TextUtils.isEmpty(title)) {
            sb.append(title).append(' ');
        }
        CharSequence summary = getSummary();
        if (!TextUtils.isEmpty(summary)) {
            sb.append(summary).append(' ');
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb;
    }

    public void saveHierarchyState(Bundle container) {
        dispatchSaveInstanceState(container);
    }

    /* access modifiers changed from: package-private */
    public void dispatchSaveInstanceState(Bundle container) {
        if (hasKey()) {
            this.mBaseMethodCalled = false;
            Parcelable state = onSaveInstanceState();
            if (!this.mBaseMethodCalled) {
                throw new IllegalStateException("Derived class did not call super.onSaveInstanceState()");
            } else if (state != null) {
                container.putParcelable(this.mKey, state);
            }
        }
    }

    /* access modifiers changed from: protected */
    public Parcelable onSaveInstanceState() {
        this.mBaseMethodCalled = true;
        return BaseSavedState.EMPTY_STATE;
    }

    public void restoreHierarchyState(Bundle container) {
        dispatchRestoreInstanceState(container);
    }

    /* access modifiers changed from: package-private */
    public void dispatchRestoreInstanceState(Bundle container) {
        Parcelable state;
        if (hasKey() && (state = container.getParcelable(this.mKey)) != null) {
            this.mBaseMethodCalled = false;
            onRestoreInstanceState(state);
            if (!this.mBaseMethodCalled) {
                throw new IllegalStateException("Derived class did not call super.onRestoreInstanceState()");
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onRestoreInstanceState(Parcelable state) {
        this.mBaseMethodCalled = true;
        if (state != BaseSavedState.EMPTY_STATE && state != null) {
            throw new IllegalArgumentException("Wrong state class -- expecting Preference State");
        }
    }

    public static class BaseSavedState extends AbsSavedState {
        public static final Parcelable.Creator<BaseSavedState> CREATOR = new Parcelable.Creator<BaseSavedState>() {
            /* class android.preference.Preference.BaseSavedState.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public BaseSavedState createFromParcel(Parcel in) {
                return new BaseSavedState(in);
            }

            @Override // android.os.Parcelable.Creator
            public BaseSavedState[] newArray(int size) {
                return new BaseSavedState[size];
            }
        };

        public BaseSavedState(Parcel source) {
            super(source);
        }

        public BaseSavedState(Parcelable superState) {
            super(superState);
        }
    }
}

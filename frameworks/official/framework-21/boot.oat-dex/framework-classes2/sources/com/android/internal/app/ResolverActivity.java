package com.android.internal.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.ActivityThread;
import android.app.AppGlobals;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PatternMatcher;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.ims.ImsConferenceState;
import com.android.internal.R;
import com.android.internal.content.PackageMonitor;
import com.android.internal.util.Protocol;
import com.android.internal.widget.ResolverDrawerLayout;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ResolverActivity extends Activity implements AdapterView.OnItemClickListener {
    private static final boolean DEBUG = false;
    private static final String TAG = "ResolverActivity";
    private static final long USAGE_STATS_PERIOD = 1209600000;
    private ResolveListAdapter mAdapter;
    private Button mAlwaysButton;
    private boolean mAlwaysUseOption;
    private int mIconDpi;
    private int mIconSize;
    private int mLastSelected = -1;
    private int mLaunchedFromUid;
    private ListView mListView;
    private int mMaxColumns;
    private Button mOnceButton;
    private final PackageMonitor mPackageMonitor = new PackageMonitor() {
        /* class com.android.internal.app.ResolverActivity.AnonymousClass1 */

        @Override // com.android.internal.content.PackageMonitor
        public void onSomePackagesChanged() {
            ResolverActivity.this.mAdapter.handlePackagesChanged();
        }
    };
    private PackageManager mPm;
    private boolean mRegistered;
    private boolean mResolvingHome = false;
    private boolean mSafeForwardingMode;
    private boolean mShowExtended;
    private Map<String, UsageStats> mStats;
    private UsageStatsManager mUsm;

    /* access modifiers changed from: private */
    public enum ActionTitle {
        VIEW("android.intent.action.VIEW", R.string.whichViewApplication, R.string.whichViewApplicationNamed),
        EDIT("android.intent.action.EDIT", R.string.whichEditApplication, R.string.whichEditApplicationNamed),
        SEND("android.intent.action.SEND", R.string.whichSendApplication, R.string.whichSendApplicationNamed),
        SENDTO("android.intent.action.SENDTO", R.string.whichSendApplication, R.string.whichSendApplicationNamed),
        SEND_MULTIPLE("android.intent.action.SEND_MULTIPLE", R.string.whichSendApplication, R.string.whichSendApplicationNamed),
        DEFAULT(null, R.string.whichApplication, R.string.whichApplicationNamed),
        HOME("android.intent.action.MAIN", R.string.whichHomeApplication, R.string.whichHomeApplicationNamed);
        
        public final String action;
        public final int namedTitleRes;
        public final int titleRes;

        private ActionTitle(String action2, int titleRes2, int namedTitleRes2) {
            this.action = action2;
            this.titleRes = titleRes2;
            this.namedTitleRes = namedTitleRes2;
        }

        public static ActionTitle forAction(String action2) {
            ActionTitle[] arr$ = values();
            for (ActionTitle title : arr$) {
                if (!(title == HOME || action2 == null || !action2.equals(title.action))) {
                    return title;
                }
            }
            return DEFAULT;
        }
    }

    private Intent makeMyIntent() {
        Intent intent = new Intent(getIntent());
        intent.setComponent(null);
        intent.setFlags(intent.getFlags() & -8388609);
        return intent;
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        Intent intent = makeMyIntent();
        Set<String> categories = intent.getCategories();
        if ("android.intent.action.MAIN".equals(intent.getAction()) && categories != null && categories.size() == 1 && categories.contains("android.intent.category.HOME")) {
            this.mResolvingHome = true;
        }
        setSafeForwardingMode(true);
        onCreate(savedInstanceState, intent, null, 0, null, null, true);
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState, Intent intent, CharSequence title, Intent[] initialIntents, List<ResolveInfo> rList, boolean alwaysUseOption) {
        onCreate(savedInstanceState, intent, title, 0, initialIntents, rList, alwaysUseOption);
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState, Intent intent, CharSequence title, int defaultTitleRes, Intent[] initialIntents, List<ResolveInfo> rList, boolean alwaysUseOption) {
        boolean useHeader;
        int layoutId;
        setTheme(R.style.Theme_DeviceDefault_Resolver);
        super.onCreate(savedInstanceState);
        try {
            this.mLaunchedFromUid = ActivityManagerNative.getDefault().getLaunchedFromUid(getActivityToken());
        } catch (RemoteException e) {
            this.mLaunchedFromUid = -1;
        }
        this.mPm = getPackageManager();
        this.mUsm = (UsageStatsManager) getSystemService("usagestats");
        long sinceTime = System.currentTimeMillis() - USAGE_STATS_PERIOD;
        this.mStats = this.mUsm.queryAndAggregateUsageStats(sinceTime, System.currentTimeMillis());
        Log.d(TAG, "sinceTime=" + sinceTime);
        this.mMaxColumns = getResources().getInteger(R.integer.config_maxResolverActivityColumns);
        this.mPackageMonitor.register(this, getMainLooper(), false);
        this.mRegistered = true;
        ActivityManager am = (ActivityManager) getSystemService("activity");
        this.mIconDpi = am.getLauncherLargeIconDensity();
        this.mIconSize = am.getLauncherLargeIconSize();
        this.mAdapter = new ResolveListAdapter(this, intent, initialIntents, rList, this.mLaunchedFromUid, alwaysUseOption);
        if (this.mAdapter.hasFilteredItem()) {
            layoutId = R.layout.resolver_list_with_default;
            alwaysUseOption = false;
            useHeader = true;
        } else {
            useHeader = false;
            layoutId = R.layout.resolver_list;
        }
        this.mAlwaysUseOption = alwaysUseOption;
        int count = this.mAdapter.mList.size();
        if (this.mLaunchedFromUid < 0 || UserHandle.isIsolated(this.mLaunchedFromUid)) {
            finish();
            return;
        }
        if (count > 1) {
            setContentView(layoutId);
            this.mListView = (ListView) findViewById(R.id.resolver_list);
            this.mListView.setAdapter((ListAdapter) this.mAdapter);
            this.mListView.setOnItemClickListener(this);
            this.mListView.setOnItemLongClickListener(new ItemLongClickListener());
            if (alwaysUseOption) {
                this.mListView.setChoiceMode(1);
            }
            if (useHeader) {
                this.mListView.addHeaderView(LayoutInflater.from(this).inflate(R.layout.resolver_different_item_header, (ViewGroup) this.mListView, false));
            }
        } else if (count == 1) {
            safelyStartActivity(this.mAdapter.intentForPosition(0, false));
            this.mPackageMonitor.unregister();
            this.mRegistered = false;
            finish();
            return;
        } else {
            setContentView(R.layout.resolver_list);
            ((TextView) findViewById(R.id.empty)).setVisibility(0);
            this.mListView = (ListView) findViewById(R.id.resolver_list);
            this.mListView.setVisibility(8);
        }
        ResolverDrawerLayout rdl = (ResolverDrawerLayout) findViewById(R.id.contentPanel);
        if (rdl != null) {
            rdl.setOnClickOutsideListener(new View.OnClickListener() {
                /* class com.android.internal.app.ResolverActivity.AnonymousClass2 */

                public void onClick(View v) {
                    ResolverActivity.this.finish();
                }
            });
        }
        if (title == null) {
            title = getTitleForAction(intent.getAction(), defaultTitleRes);
        }
        if (!TextUtils.isEmpty(title)) {
            TextView titleView = (TextView) findViewById(R.id.title);
            if (titleView != null) {
                titleView.setText(title);
            }
            setTitle(title);
        }
        ImageView iconView = (ImageView) findViewById(R.id.icon);
        DisplayResolveInfo iconInfo = this.mAdapter.getFilteredItem();
        if (!(iconView == null || iconInfo == null)) {
            new LoadIconIntoViewTask(iconView).execute(iconInfo);
        }
        if (alwaysUseOption || this.mAdapter.hasFilteredItem()) {
            ViewGroup buttonLayout = (ViewGroup) findViewById(R.id.button_bar);
            if (buttonLayout != null) {
                buttonLayout.setVisibility(0);
                this.mAlwaysButton = (Button) buttonLayout.findViewById(R.id.button_always);
                this.mOnceButton = (Button) buttonLayout.findViewById(R.id.button_once);
            } else {
                this.mAlwaysUseOption = false;
            }
        }
        if (this.mAdapter.hasFilteredItem()) {
            setAlwaysButtonEnabled(true, this.mAdapter.getFilteredPosition(), false);
            this.mOnceButton.setEnabled(true);
        }
    }

    public void setSafeForwardingMode(boolean safeForwarding) {
        this.mSafeForwardingMode = safeForwarding;
    }

    /* access modifiers changed from: protected */
    public CharSequence getTitleForAction(String action, int defaultTitleRes) {
        ActionTitle title = this.mResolvingHome ? ActionTitle.HOME : ActionTitle.forAction(action);
        boolean named = this.mAdapter.hasFilteredItem();
        if (title == ActionTitle.DEFAULT && defaultTitleRes != 0) {
            return getString(defaultTitleRes);
        }
        if (!named) {
            return getString(title.titleRes);
        }
        return getString(title.namedTitleRes, new Object[]{this.mAdapter.getFilteredItem().displayLabel});
    }

    /* access modifiers changed from: package-private */
    public void dismiss() {
        if (!isFinishing()) {
            finish();
        }
    }

    /* access modifiers changed from: package-private */
    public Drawable getIcon(Resources res, int resId) {
        try {
            return res.getDrawableForDensity(resId, this.mIconDpi);
        } catch (Resources.NotFoundException e) {
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public Drawable loadIconForResolveInfo(ResolveInfo ri) {
        Drawable dr;
        Drawable dr2;
        try {
            if (ri.resolvePackageName != null && ri.icon != 0 && (dr2 = getIcon(this.mPm.getResourcesForApplication(ri.resolvePackageName), ri.icon)) != null) {
                return dr2;
            }
            int iconRes = ri.getIconResource();
            if (!(iconRes == 0 || (dr = getIcon(this.mPm.getResourcesForApplication(ri.activityInfo.packageName), iconRes)) == null)) {
                return dr;
            }
            return ri.loadIcon(this.mPm);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Couldn't find resources for package", e);
        }
    }

    /* access modifiers changed from: protected */
    public void onRestart() {
        super.onRestart();
        if (!this.mRegistered) {
            this.mPackageMonitor.register(this, getMainLooper(), false);
            this.mRegistered = true;
        }
        this.mAdapter.handlePackagesChanged();
    }

    /* access modifiers changed from: protected */
    public void onStop() {
        super.onStop();
        if (this.mRegistered) {
            this.mPackageMonitor.unregister();
            this.mRegistered = false;
        }
        if ((getIntent().getFlags() & 268435456) != 0 && !isChangingConfigurations()) {
            finish();
        }
    }

    /* access modifiers changed from: protected */
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (this.mAlwaysUseOption) {
            int checkedPos = this.mListView.getCheckedItemPosition();
            boolean hasValidSelection = checkedPos != -1;
            this.mLastSelected = checkedPos;
            setAlwaysButtonEnabled(hasValidSelection, checkedPos, true);
            this.mOnceButton.setEnabled(hasValidSelection);
            if (hasValidSelection) {
                this.mListView.setSelection(checkedPos);
            }
        }
    }

    @Override // android.widget.AdapterView.OnItemClickListener
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        boolean hasValidSelection;
        int position2 = position - this.mListView.getHeaderViewsCount();
        if (position2 >= 0) {
            ResolveInfo resolveInfo = this.mAdapter.resolveInfoForPosition(position2, true);
            if (!this.mResolvingHome || !hasManagedProfile() || supportsManagedProfiles(resolveInfo)) {
                int checkedPos = this.mListView.getCheckedItemPosition();
                if (checkedPos != -1) {
                    hasValidSelection = true;
                } else {
                    hasValidSelection = false;
                }
                if (!this.mAlwaysUseOption || (hasValidSelection && this.mLastSelected == checkedPos)) {
                    startSelected(position2, false, true);
                    return;
                }
                setAlwaysButtonEnabled(hasValidSelection, checkedPos, true);
                this.mOnceButton.setEnabled(hasValidSelection);
                if (hasValidSelection) {
                    this.mListView.smoothScrollToPosition(checkedPos);
                }
                this.mLastSelected = checkedPos;
                return;
            }
            Toast.makeText(this, String.format(getResources().getString(R.string.activity_resolver_work_profiles_support), resolveInfo.activityInfo.loadLabel(getPackageManager()).toString()), 1).show();
        }
    }

    private boolean hasManagedProfile() {
        UserManager userManager = (UserManager) getSystemService(ImsConferenceState.USER);
        if (userManager == null) {
            return false;
        }
        try {
            for (UserInfo userInfo : userManager.getProfiles(getUserId())) {
                if (userInfo != null && userInfo.isManagedProfile()) {
                    return true;
                }
            }
            return false;
        } catch (SecurityException e) {
            return false;
        }
    }

    private boolean supportsManagedProfiles(ResolveInfo resolveInfo) {
        try {
            return versionNumberAtLeastL(getPackageManager().getApplicationInfo(resolveInfo.activityInfo.packageName, 0).targetSdkVersion);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private boolean versionNumberAtLeastL(int versionNumber) {
        return versionNumber >= 21;
    }

    private void setAlwaysButtonEnabled(boolean hasValidSelection, int checkedPos, boolean filtered) {
        boolean enabled = false;
        if (hasValidSelection && this.mAdapter.resolveInfoForPosition(checkedPos, filtered).targetUserId == -2) {
            enabled = true;
        }
        this.mAlwaysButton.setEnabled(enabled);
    }

    public void onButtonClick(View v) {
        startSelected(this.mAlwaysUseOption ? this.mListView.getCheckedItemPosition() : this.mAdapter.getFilteredPosition(), v.getId() == 16909158, this.mAlwaysUseOption);
        dismiss();
    }

    /* access modifiers changed from: package-private */
    public void startSelected(int which, boolean always, boolean filtered) {
        if (!isFinishing()) {
            onIntentSelected(this.mAdapter.resolveInfoForPosition(which, filtered), this.mAdapter.intentForPosition(which, filtered), always);
            finish();
        }
    }

    public Intent getReplacementIntent(String packageName, Intent defIntent) {
        return defIntent;
    }

    /* access modifiers changed from: protected */
    public void onIntentSelected(ResolveInfo ri, Intent intent, boolean alwaysCheck) {
        String mimeType;
        if ((this.mAlwaysUseOption || this.mAdapter.hasFilteredItem()) && this.mAdapter.mOrigResolveList != null) {
            IntentFilter filter = new IntentFilter();
            if (intent.getAction() != null) {
                filter.addAction(intent.getAction());
            }
            Set<String> categories = intent.getCategories();
            if (categories != null) {
                for (String cat : categories) {
                    filter.addCategory(cat);
                }
            }
            filter.addCategory("android.intent.category.DEFAULT");
            int cat2 = ri.match & 268369920;
            Uri data = intent.getData();
            if (cat2 == 6291456 && (mimeType = intent.resolveType(this)) != null) {
                try {
                    filter.addDataType(mimeType);
                } catch (IntentFilter.MalformedMimeTypeException e) {
                    Log.w(TAG, e);
                    filter = null;
                }
            }
            if (data != null && data.getScheme() != null && (cat2 != 6291456 || (!"file".equals(data.getScheme()) && !"content".equals(data.getScheme())))) {
                filter.addDataScheme(data.getScheme());
                Iterator<PatternMatcher> pIt = ri.filter.schemeSpecificPartsIterator();
                if (pIt != null) {
                    String ssp = data.getSchemeSpecificPart();
                    while (true) {
                        if (ssp == null || !pIt.hasNext()) {
                            break;
                        }
                        PatternMatcher p = pIt.next();
                        if (p.match(ssp)) {
                            filter.addDataSchemeSpecificPart(p.getPath(), p.getType());
                            break;
                        }
                    }
                }
                Iterator<IntentFilter.AuthorityEntry> aIt = ri.filter.authoritiesIterator();
                if (aIt != null) {
                    while (true) {
                        if (!aIt.hasNext()) {
                            break;
                        }
                        IntentFilter.AuthorityEntry a = aIt.next();
                        if (a.match(data) >= 0) {
                            int port = a.getPort();
                            filter.addDataAuthority(a.getHost(), port >= 0 ? Integer.toString(port) : null);
                        }
                    }
                }
                Iterator<PatternMatcher> pIt2 = ri.filter.pathsIterator();
                if (pIt2 != null) {
                    String path = data.getPath();
                    while (true) {
                        if (path == null || !pIt2.hasNext()) {
                            break;
                        }
                        PatternMatcher p2 = pIt2.next();
                        if (p2.match(path)) {
                            filter.addDataPath(p2.getPath(), p2.getType());
                            break;
                        }
                    }
                }
            }
            if (filter != null) {
                int N = this.mAdapter.mOrigResolveList.size();
                ComponentName[] set = new ComponentName[N];
                int bestMatch = 0;
                for (int i = 0; i < N; i++) {
                    ResolveInfo r = this.mAdapter.mOrigResolveList.get(i);
                    set[i] = new ComponentName(r.activityInfo.packageName, r.activityInfo.name);
                    if (r.match > bestMatch) {
                        bestMatch = r.match;
                    }
                }
                if (alwaysCheck) {
                    getPackageManager().addPreferredActivity(filter, bestMatch, set, intent.getComponent());
                } else {
                    try {
                        AppGlobals.getPackageManager().setLastChosenActivity(intent, intent.resolveTypeIfNeeded(getContentResolver()), (int) Protocol.BASE_SYSTEM_RESERVED, filter, bestMatch, intent.getComponent());
                    } catch (RemoteException re) {
                        Log.d(TAG, "Error calling setLastChosenActivity\n" + re);
                    }
                }
            }
        }
        if (intent != null) {
            safelyStartActivity(intent);
        }
    }

    public void safelyStartActivity(Intent intent) {
        String launchedFromPackage;
        if (!this.mSafeForwardingMode) {
            startActivity(intent);
            return;
        }
        try {
            startActivityAsCaller(intent, null, -10000);
        } catch (RuntimeException e) {
            try {
                launchedFromPackage = ActivityManagerNative.getDefault().getLaunchedFromPackage(getActivityToken());
            } catch (RemoteException e2) {
                launchedFromPackage = "??";
            }
            Slog.wtf(TAG, "Unable to launch as uid " + this.mLaunchedFromUid + " package " + launchedFromPackage + ", while running in " + ActivityThread.currentProcessName(), e);
        }
    }

    /* access modifiers changed from: package-private */
    public void showAppDetails(ResolveInfo ri) {
        startActivity(new Intent().setAction("android.settings.APPLICATION_DETAILS_SETTINGS").setData(Uri.fromParts("package", ri.activityInfo.packageName, null)).addFlags(Protocol.BASE_CONNECTIVITY_MANAGER));
    }

    /* access modifiers changed from: private */
    public final class DisplayResolveInfo {
        Drawable displayIcon;
        CharSequence displayLabel;
        CharSequence extendedInfo;
        Intent origIntent;
        ResolveInfo ri;

        DisplayResolveInfo(ResolveInfo pri, CharSequence pLabel, CharSequence pInfo, Intent pOrigIntent) {
            this.ri = pri;
            this.displayLabel = pLabel;
            this.extendedInfo = pInfo;
            this.origIntent = pOrigIntent;
        }
    }

    /* access modifiers changed from: private */
    public final class ResolveListAdapter extends BaseAdapter {
        private final List<ResolveInfo> mBaseResolveList;
        private boolean mFilterLastUsed;
        private final LayoutInflater mInflater;
        private final Intent[] mInitialIntents;
        private final Intent mIntent;
        private ResolveInfo mLastChosen;
        private int mLastChosenPosition = -1;
        private final int mLaunchedFromUid;
        List<DisplayResolveInfo> mList;
        List<ResolveInfo> mOrigResolveList;

        public ResolveListAdapter(Context context, Intent intent, Intent[] initialIntents, List<ResolveInfo> rList, int launchedFromUid, boolean filterLastUsed) {
            this.mIntent = new Intent(intent);
            this.mInitialIntents = initialIntents;
            this.mBaseResolveList = rList;
            this.mLaunchedFromUid = launchedFromUid;
            this.mInflater = LayoutInflater.from(context);
            this.mList = new ArrayList();
            this.mFilterLastUsed = filterLastUsed;
            rebuildList();
        }

        public void handlePackagesChanged() {
            getCount();
            rebuildList();
            notifyDataSetChanged();
            if (getCount() == 0) {
                ResolverActivity.this.finish();
            }
        }

        public DisplayResolveInfo getFilteredItem() {
            if (!this.mFilterLastUsed || this.mLastChosenPosition < 0) {
                return null;
            }
            return this.mList.get(this.mLastChosenPosition);
        }

        public int getFilteredPosition() {
            if (!this.mFilterLastUsed || this.mLastChosenPosition < 0) {
                return -1;
            }
            return this.mLastChosenPosition;
        }

        public boolean hasFilteredItem() {
            return this.mFilterLastUsed && this.mLastChosenPosition >= 0;
        }

        private void rebuildList() {
            List<ResolveInfo> currentResolveList;
            int N;
            try {
                this.mLastChosen = AppGlobals.getPackageManager().getLastChosenActivity(this.mIntent, this.mIntent.resolveTypeIfNeeded(ResolverActivity.this.getContentResolver()), (int) Protocol.BASE_SYSTEM_RESERVED);
            } catch (RemoteException re) {
                Log.d(ResolverActivity.TAG, "Error calling setLastChosenActivity\n" + re);
            }
            this.mList.clear();
            if (this.mBaseResolveList != null) {
                currentResolveList = this.mBaseResolveList;
                this.mOrigResolveList = currentResolveList;
            } else {
                currentResolveList = ResolverActivity.this.mPm.queryIntentActivities(this.mIntent, (this.mFilterLastUsed ? 64 : 0) | Protocol.BASE_SYSTEM_RESERVED);
                this.mOrigResolveList = currentResolveList;
                if (currentResolveList != null) {
                    for (int i = currentResolveList.size() - 1; i >= 0; i--) {
                        ActivityInfo ai = currentResolveList.get(i).activityInfo;
                        if (ActivityManager.checkComponentPermission(ai.permission, this.mLaunchedFromUid, ai.applicationInfo.uid, ai.exported) != 0) {
                            if (this.mOrigResolveList == currentResolveList) {
                                this.mOrigResolveList = new ArrayList(this.mOrigResolveList);
                            }
                            currentResolveList.remove(i);
                        }
                    }
                }
            }
            if (currentResolveList != null && (N = currentResolveList.size()) > 0) {
                ResolveInfo r0 = currentResolveList.get(0);
                for (int i2 = 1; i2 < N; i2++) {
                    ResolveInfo ri = currentResolveList.get(i2);
                    if (r0.priority != ri.priority || r0.isDefault != ri.isDefault) {
                        while (i2 < N) {
                            if (this.mOrigResolveList == currentResolveList) {
                                this.mOrigResolveList = new ArrayList(this.mOrigResolveList);
                            }
                            currentResolveList.remove(i2);
                            N--;
                        }
                    }
                }
                if (N > 1) {
                    Collections.sort(currentResolveList, new ResolverComparator(ResolverActivity.this));
                }
                if (this.mInitialIntents != null) {
                    for (int i3 = 0; i3 < this.mInitialIntents.length; i3++) {
                        Intent ii = this.mInitialIntents[i3];
                        if (ii != null) {
                            ActivityInfo ai2 = ii.resolveActivityInfo(ResolverActivity.this.getPackageManager(), 0);
                            if (ai2 == null) {
                                Log.w(ResolverActivity.TAG, "No activity found for " + ii);
                            } else {
                                ResolveInfo ri2 = new ResolveInfo();
                                ri2.activityInfo = ai2;
                                if (ii instanceof LabeledIntent) {
                                    LabeledIntent li = (LabeledIntent) ii;
                                    ri2.resolvePackageName = li.getSourcePackage();
                                    ri2.labelRes = li.getLabelResource();
                                    ri2.nonLocalizedLabel = li.getNonLocalizedLabel();
                                    ri2.icon = li.getIconResource();
                                }
                                this.mList.add(new DisplayResolveInfo(ri2, ri2.loadLabel(ResolverActivity.this.getPackageManager()), null, ii));
                            }
                        }
                    }
                }
                ResolveInfo r02 = currentResolveList.get(0);
                int start = 0;
                CharSequence r0Label = r02.loadLabel(ResolverActivity.this.mPm);
                ResolverActivity.this.mShowExtended = false;
                for (int i4 = 1; i4 < N; i4++) {
                    if (r0Label == null) {
                        r0Label = r02.activityInfo.packageName;
                    }
                    ResolveInfo ri3 = currentResolveList.get(i4);
                    CharSequence riLabel = ri3.loadLabel(ResolverActivity.this.mPm);
                    if (riLabel == null) {
                        riLabel = ri3.activityInfo.packageName;
                    }
                    if (!riLabel.equals(r0Label)) {
                        processGroup(currentResolveList, start, i4 - 1, r02, r0Label);
                        r02 = ri3;
                        r0Label = riLabel;
                        start = i4;
                    }
                }
                processGroup(currentResolveList, start, N - 1, r02, r0Label);
            }
        }

        private void processGroup(List<ResolveInfo> rList, int start, int end, ResolveInfo ro, CharSequence roLabel) {
            if ((end - start) + 1 == 1) {
                if (this.mLastChosen != null && this.mLastChosen.activityInfo.packageName.equals(ro.activityInfo.packageName) && this.mLastChosen.activityInfo.name.equals(ro.activityInfo.name)) {
                    this.mLastChosenPosition = this.mList.size();
                }
                this.mList.add(new DisplayResolveInfo(ro, roLabel, null, null));
                return;
            }
            ResolverActivity.this.mShowExtended = true;
            boolean usePkg = false;
            CharSequence startApp = ro.activityInfo.applicationInfo.loadLabel(ResolverActivity.this.mPm);
            if (startApp == null) {
                usePkg = true;
            }
            if (!usePkg) {
                HashSet<CharSequence> duplicates = new HashSet<>();
                duplicates.add(startApp);
                int j = start + 1;
                while (true) {
                    if (j > end) {
                        break;
                    }
                    CharSequence jApp = rList.get(j).activityInfo.applicationInfo.loadLabel(ResolverActivity.this.mPm);
                    if (jApp == null || duplicates.contains(jApp)) {
                        usePkg = true;
                    } else {
                        duplicates.add(jApp);
                        j++;
                    }
                }
                usePkg = true;
                duplicates.clear();
            }
            for (int k = start; k <= end; k++) {
                ResolveInfo add = rList.get(k);
                if (this.mLastChosen != null && this.mLastChosen.activityInfo.packageName.equals(add.activityInfo.packageName) && this.mLastChosen.activityInfo.name.equals(add.activityInfo.name)) {
                    this.mLastChosenPosition = this.mList.size();
                }
                if (usePkg) {
                    this.mList.add(new DisplayResolveInfo(add, roLabel, add.activityInfo.packageName, null));
                } else {
                    this.mList.add(new DisplayResolveInfo(add, roLabel, add.activityInfo.applicationInfo.loadLabel(ResolverActivity.this.mPm), null));
                }
            }
        }

        public ResolveInfo resolveInfoForPosition(int position, boolean filtered) {
            return (filtered ? getItem(position) : this.mList.get(position)).ri;
        }

        public Intent intentForPosition(int position, boolean filtered) {
            DisplayResolveInfo dri = filtered ? getItem(position) : this.mList.get(position);
            Intent intent = new Intent(dri.origIntent != null ? dri.origIntent : ResolverActivity.this.getReplacementIntent(dri.ri.activityInfo.packageName, this.mIntent));
            intent.addFlags(50331648);
            ActivityInfo ai = dri.ri.activityInfo;
            intent.setComponent(new ComponentName(ai.applicationInfo.packageName, ai.name));
            return intent;
        }

        @Override // android.widget.Adapter
        public int getCount() {
            int result = this.mList.size();
            if (!this.mFilterLastUsed || this.mLastChosenPosition < 0) {
                return result;
            }
            return result - 1;
        }

        @Override // android.widget.Adapter
        public DisplayResolveInfo getItem(int position) {
            if (this.mFilterLastUsed && this.mLastChosenPosition >= 0 && position >= this.mLastChosenPosition) {
                position++;
            }
            return this.mList.get(position);
        }

        @Override // android.widget.Adapter
        public long getItemId(int position) {
            return (long) position;
        }

        @Override // android.widget.Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = this.mInflater.inflate(R.layout.resolve_list_item, parent, false);
                view.setTag(new ViewHolder(view));
            }
            bindView(view, getItem(position));
            return view;
        }

        private final void bindView(View view, DisplayResolveInfo info) {
            ViewHolder holder = (ViewHolder) view.getTag();
            holder.text.setText(info.displayLabel);
            if (ResolverActivity.this.mShowExtended) {
                holder.text2.setVisibility(0);
                holder.text2.setText(info.extendedInfo);
            } else {
                holder.text2.setVisibility(8);
            }
            if (info.displayIcon == null) {
                new LoadIconTask().execute(info);
            }
            holder.icon.setImageDrawable(info.displayIcon);
        }
    }

    /* access modifiers changed from: package-private */
    public static class ViewHolder {
        public ImageView icon;
        public TextView text;
        public TextView text2;

        public ViewHolder(View view) {
            this.text = (TextView) view.findViewById(R.id.text1);
            this.text2 = (TextView) view.findViewById(R.id.text2);
            this.icon = (ImageView) view.findViewById(R.id.icon);
        }
    }

    /* access modifiers changed from: package-private */
    public class ItemLongClickListener implements AdapterView.OnItemLongClickListener {
        ItemLongClickListener() {
        }

        @Override // android.widget.AdapterView.OnItemLongClickListener
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
            int position2 = position - ResolverActivity.this.mListView.getHeaderViewsCount();
            if (position2 < 0) {
                return false;
            }
            ResolverActivity.this.showAppDetails(ResolverActivity.this.mAdapter.resolveInfoForPosition(position2, true));
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public class LoadIconTask extends AsyncTask<DisplayResolveInfo, Void, DisplayResolveInfo> {
        LoadIconTask() {
        }

        /* access modifiers changed from: protected */
        public DisplayResolveInfo doInBackground(DisplayResolveInfo... params) {
            DisplayResolveInfo info = params[0];
            if (info.displayIcon == null) {
                info.displayIcon = ResolverActivity.this.loadIconForResolveInfo(info.ri);
            }
            return info;
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(DisplayResolveInfo info) {
            ResolverActivity.this.mAdapter.notifyDataSetChanged();
        }
    }

    /* access modifiers changed from: package-private */
    public class LoadIconIntoViewTask extends AsyncTask<DisplayResolveInfo, Void, DisplayResolveInfo> {
        final ImageView mTargetView;

        public LoadIconIntoViewTask(ImageView target) {
            this.mTargetView = target;
        }

        /* access modifiers changed from: protected */
        public DisplayResolveInfo doInBackground(DisplayResolveInfo... params) {
            DisplayResolveInfo info = params[0];
            if (info.displayIcon == null) {
                info.displayIcon = ResolverActivity.this.loadIconForResolveInfo(info.ri);
            }
            return info;
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(DisplayResolveInfo info) {
            this.mTargetView.setImageDrawable(info.displayIcon);
        }
    }

    /* access modifiers changed from: package-private */
    public class ResolverComparator implements Comparator<ResolveInfo> {
        private final Collator mCollator;

        public ResolverComparator(Context context) {
            this.mCollator = Collator.getInstance(context.getResources().getConfiguration().locale);
        }

        public int compare(ResolveInfo lhs, ResolveInfo rhs) {
            if (lhs.targetUserId != -2) {
                return 1;
            }
            if (ResolverActivity.this.mStats != null) {
                long timeDiff = getPackageTimeSpent(rhs.activityInfo.packageName) - getPackageTimeSpent(lhs.activityInfo.packageName);
                if (timeDiff != 0) {
                    if (timeDiff <= 0) {
                        return -1;
                    }
                    return 1;
                }
            }
            CharSequence sa = lhs.loadLabel(ResolverActivity.this.mPm);
            if (sa == null) {
                sa = lhs.activityInfo.name;
            }
            CharSequence sb = rhs.loadLabel(ResolverActivity.this.mPm);
            if (sb == null) {
                sb = rhs.activityInfo.name;
            }
            return this.mCollator.compare(sa.toString(), sb.toString());
        }

        private long getPackageTimeSpent(String packageName) {
            UsageStats stats;
            if (ResolverActivity.this.mStats == null || (stats = (UsageStats) ResolverActivity.this.mStats.get(packageName)) == null) {
                return 0;
            }
            return stats.getTotalTimeInForeground();
        }
    }
}

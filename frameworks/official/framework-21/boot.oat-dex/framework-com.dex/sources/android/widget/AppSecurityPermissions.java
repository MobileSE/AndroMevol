package android.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.android.internal.R;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AppSecurityPermissions {
    private static final String TAG = "AppSecurityPermissions";
    public static final int WHICH_ALL = 65535;
    public static final int WHICH_DEVICE = 2;
    public static final int WHICH_NEW = 4;
    public static final int WHICH_PERSONAL = 1;
    private static final boolean localLOGV = false;
    private final Context mContext;
    private final LayoutInflater mInflater;
    private final CharSequence mNewPermPrefix;
    private String mPackageName;
    private final PermissionInfoComparator mPermComparator;
    private final PermissionGroupInfoComparator mPermGroupComparator;
    private final Map<String, MyPermissionGroupInfo> mPermGroups;
    private final List<MyPermissionGroupInfo> mPermGroupsList;
    private final List<MyPermissionInfo> mPermsList;
    private final PackageManager mPm;

    /* access modifiers changed from: package-private */
    public static class MyPermissionGroupInfo extends PermissionGroupInfo {
        final ArrayList<MyPermissionInfo> mAllPermissions = new ArrayList<>();
        final ArrayList<MyPermissionInfo> mDevicePermissions = new ArrayList<>();
        CharSequence mLabel;
        final ArrayList<MyPermissionInfo> mNewPermissions = new ArrayList<>();
        final ArrayList<MyPermissionInfo> mPersonalPermissions = new ArrayList<>();

        MyPermissionGroupInfo(PermissionInfo perm) {
            this.name = perm.packageName;
            this.packageName = perm.packageName;
        }

        MyPermissionGroupInfo(PermissionGroupInfo info) {
            super(info);
        }

        public Drawable loadGroupIcon(PackageManager pm) {
            if (this.icon != 0) {
                return loadIcon(pm);
            }
            try {
                return pm.getApplicationInfo(this.packageName, 0).loadIcon(pm);
            } catch (PackageManager.NameNotFoundException e) {
                return null;
            }
        }
    }

    /* access modifiers changed from: private */
    public static class MyPermissionInfo extends PermissionInfo {
        int mExistingReqFlags;
        CharSequence mLabel;
        boolean mNew;
        int mNewReqFlags;

        MyPermissionInfo(PermissionInfo info) {
            super(info);
        }
    }

    public static class PermissionItemView extends LinearLayout implements View.OnClickListener {
        AlertDialog mDialog;
        MyPermissionGroupInfo mGroup;
        private String mPackageName;
        MyPermissionInfo mPerm;
        private boolean mShowRevokeUI = false;

        public PermissionItemView(Context context, AttributeSet attrs) {
            super(context, attrs);
            setClickable(true);
        }

        public void setPermission(MyPermissionGroupInfo grp, MyPermissionInfo perm, boolean first, CharSequence newPermPrefix, String packageName, boolean showRevokeUI) {
            this.mGroup = grp;
            this.mPerm = perm;
            this.mShowRevokeUI = showRevokeUI;
            this.mPackageName = packageName;
            ImageView permGrpIcon = (ImageView) findViewById(R.id.perm_icon);
            TextView permNameView = (TextView) findViewById(R.id.perm_name);
            PackageManager pm = getContext().getPackageManager();
            Drawable icon = null;
            if (first) {
                icon = grp.loadGroupIcon(pm);
            }
            CharSequence label = perm.mLabel;
            if (perm.mNew && newPermPrefix != null) {
                SpannableStringBuilder builder = new SpannableStringBuilder();
                Parcel parcel = Parcel.obtain();
                TextUtils.writeToParcel(newPermPrefix, parcel, 0);
                parcel.setDataPosition(0);
                parcel.recycle();
                builder.append(TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel));
                builder.append(label);
                label = builder;
            }
            permGrpIcon.setImageDrawable(icon);
            permNameView.setText(label);
            setOnClickListener(this);
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View v) {
            CharSequence appName;
            if (this.mGroup != null && this.mPerm != null) {
                if (this.mDialog != null) {
                    this.mDialog.dismiss();
                }
                PackageManager pm = getContext().getPackageManager();
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(this.mGroup.mLabel);
                if (this.mPerm.descriptionRes != 0) {
                    builder.setMessage(this.mPerm.loadDescription(pm));
                } else {
                    try {
                        appName = pm.getApplicationInfo(this.mPerm.packageName, 0).loadLabel(pm);
                    } catch (PackageManager.NameNotFoundException e) {
                        appName = this.mPerm.packageName;
                    }
                    StringBuilder sbuilder = new StringBuilder(128);
                    sbuilder.append(getContext().getString(R.string.perms_description_app, appName));
                    sbuilder.append("\n\n");
                    sbuilder.append(this.mPerm.name);
                    builder.setMessage(sbuilder.toString());
                }
                builder.setCancelable(true);
                builder.setIcon(this.mGroup.loadGroupIcon(pm));
                addRevokeUIIfNecessary(builder);
                this.mDialog = builder.show();
                this.mDialog.setCanceledOnTouchOutside(true);
            }
        }

        /* access modifiers changed from: protected */
        @Override // android.view.View, android.view.ViewGroup
        public void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            if (this.mDialog != null) {
                this.mDialog.dismiss();
            }
        }

        private void addRevokeUIIfNecessary(AlertDialog.Builder builder) {
            if (this.mShowRevokeUI) {
                if (!((this.mPerm.mExistingReqFlags & 1) != 0)) {
                    builder.setNegativeButton(R.string.revoke, new DialogInterface.OnClickListener() {
                        /* class android.widget.AppSecurityPermissions.PermissionItemView.AnonymousClass1 */

                        @Override // android.content.DialogInterface.OnClickListener
                        public void onClick(DialogInterface dialog, int which) {
                            PermissionItemView.this.getContext().getPackageManager().revokePermission(PermissionItemView.this.mPackageName, PermissionItemView.this.mPerm.name);
                            PermissionItemView.this.setVisibility(8);
                        }
                    });
                    builder.setPositiveButton(17039370, (DialogInterface.OnClickListener) null);
                }
            }
        }
    }

    private AppSecurityPermissions(Context context) {
        this.mPermGroups = new HashMap();
        this.mPermGroupsList = new ArrayList();
        this.mPermGroupComparator = new PermissionGroupInfoComparator();
        this.mPermComparator = new PermissionInfoComparator();
        this.mPermsList = new ArrayList();
        this.mContext = context;
        this.mInflater = (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mPm = this.mContext.getPackageManager();
        this.mNewPermPrefix = this.mContext.getText(R.string.perms_new_perm_prefix);
    }

    public AppSecurityPermissions(Context context, String packageName) {
        this(context);
        this.mPackageName = packageName;
        Set<MyPermissionInfo> permSet = new HashSet<>();
        try {
            PackageInfo pkgInfo = this.mPm.getPackageInfo(packageName, 4096);
            if (!(pkgInfo.applicationInfo == null || pkgInfo.applicationInfo.uid == -1)) {
                getAllUsedPermissions(pkgInfo.applicationInfo.uid, permSet);
            }
            this.mPermsList.addAll(permSet);
            setPermissions(this.mPermsList);
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "Couldn't retrieve permissions for package:" + packageName);
        }
    }

    public AppSecurityPermissions(Context context, PackageInfo info) {
        this(context);
        Set<MyPermissionInfo> permSet = new HashSet<>();
        if (info != null) {
            this.mPackageName = info.packageName;
            PackageInfo installedPkgInfo = null;
            if (info.requestedPermissions != null) {
                try {
                    installedPkgInfo = this.mPm.getPackageInfo(info.packageName, 4096);
                } catch (PackageManager.NameNotFoundException e) {
                }
                extractPerms(info, permSet, installedPkgInfo);
            }
            if (info.sharedUserId != null) {
                try {
                    getAllUsedPermissions(this.mPm.getUidForSharedUser(info.sharedUserId), permSet);
                } catch (PackageManager.NameNotFoundException e2) {
                    Log.w(TAG, "Couldn't retrieve shared user id for: " + info.packageName);
                }
            }
            this.mPermsList.addAll(permSet);
            setPermissions(this.mPermsList);
        }
    }

    public static View getPermissionItemView(Context context, CharSequence grpName, CharSequence description, boolean dangerous) {
        return getPermissionItemViewOld(context, (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE), grpName, description, dangerous, context.getDrawable(dangerous ? R.drawable.ic_bullet_key_permission : R.drawable.ic_text_dot));
    }

    private void getAllUsedPermissions(int sharedUid, Set<MyPermissionInfo> permSet) {
        String[] sharedPkgList = this.mPm.getPackagesForUid(sharedUid);
        if (!(sharedPkgList == null || sharedPkgList.length == 0)) {
            for (String sharedPkg : sharedPkgList) {
                getPermissionsForPackage(sharedPkg, permSet);
            }
        }
    }

    private void getPermissionsForPackage(String packageName, Set<MyPermissionInfo> permSet) {
        try {
            PackageInfo pkgInfo = this.mPm.getPackageInfo(packageName, 4096);
            extractPerms(pkgInfo, permSet, pkgInfo);
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "Couldn't retrieve permissions for package: " + packageName);
        }
    }

    private void extractPerms(PackageInfo info, Set<MyPermissionInfo> permSet, PackageInfo installedPkgInfo) {
        MyPermissionGroupInfo group;
        String[] strList = info.requestedPermissions;
        int[] flagsList = info.requestedPermissionsFlags;
        if (!(strList == null || strList.length == 0)) {
            for (int i = 0; i < strList.length; i++) {
                String permName = strList[i];
                if (installedPkgInfo == null || info != installedPkgInfo || (flagsList[i] & 2) != 0) {
                    try {
                        PermissionInfo tmpPermInfo = this.mPm.getPermissionInfo(permName, 0);
                        if (tmpPermInfo != null) {
                            int existingIndex = -1;
                            if (installedPkgInfo != null && installedPkgInfo.requestedPermissions != null) {
                                int j = 0;
                                while (true) {
                                    if (j >= installedPkgInfo.requestedPermissions.length) {
                                        break;
                                    } else if (permName.equals(installedPkgInfo.requestedPermissions[j])) {
                                        existingIndex = j;
                                        break;
                                    } else {
                                        j++;
                                    }
                                }
                            }
                            int existingFlags = existingIndex >= 0 ? installedPkgInfo.requestedPermissionsFlags[existingIndex] : 0;
                            if (isDisplayablePermission(tmpPermInfo, flagsList[i], existingFlags)) {
                                String origGroupName = tmpPermInfo.group;
                                String groupName = origGroupName;
                                if (groupName == null) {
                                    groupName = tmpPermInfo.packageName;
                                    tmpPermInfo.group = groupName;
                                }
                                if (this.mPermGroups.get(groupName) == null) {
                                    PermissionGroupInfo grp = null;
                                    if (origGroupName != null) {
                                        grp = this.mPm.getPermissionGroupInfo(origGroupName, 0);
                                    }
                                    if (grp != null) {
                                        group = new MyPermissionGroupInfo(grp);
                                    } else {
                                        tmpPermInfo.group = tmpPermInfo.packageName;
                                        if (this.mPermGroups.get(tmpPermInfo.group) == null) {
                                            new MyPermissionGroupInfo(tmpPermInfo);
                                        }
                                        group = new MyPermissionGroupInfo(tmpPermInfo);
                                    }
                                    this.mPermGroups.put(tmpPermInfo.group, group);
                                }
                                boolean newPerm = installedPkgInfo != null && (existingFlags & 2) == 0;
                                MyPermissionInfo myPerm = new MyPermissionInfo(tmpPermInfo);
                                myPerm.mNewReqFlags = flagsList[i];
                                myPerm.mExistingReqFlags = existingFlags;
                                myPerm.mNew = newPerm;
                                permSet.add(myPerm);
                            }
                        }
                    } catch (PackageManager.NameNotFoundException e) {
                        Log.i(TAG, "Ignoring unknown permission:" + permName);
                    }
                }
            }
        }
    }

    public int getPermissionCount() {
        return getPermissionCount(65535);
    }

    private List<MyPermissionInfo> getPermissionList(MyPermissionGroupInfo grp, int which) {
        if (which == 4) {
            return grp.mNewPermissions;
        }
        if (which == 1) {
            return grp.mPersonalPermissions;
        }
        if (which == 2) {
            return grp.mDevicePermissions;
        }
        return grp.mAllPermissions;
    }

    public int getPermissionCount(int which) {
        int N = 0;
        for (int i = 0; i < this.mPermGroupsList.size(); i++) {
            N += getPermissionList(this.mPermGroupsList.get(i), which).size();
        }
        return N;
    }

    public View getPermissionsView() {
        return getPermissionsView(65535, false);
    }

    public View getPermissionsViewWithRevokeButtons() {
        return getPermissionsView(65535, true);
    }

    public View getPermissionsView(int which) {
        return getPermissionsView(which, false);
    }

    private View getPermissionsView(int which, boolean showRevokeUI) {
        LinearLayout permsView = (LinearLayout) this.mInflater.inflate(R.layout.app_perms_summary, (ViewGroup) null);
        LinearLayout displayList = (LinearLayout) permsView.findViewById(R.id.perms_list);
        View noPermsView = permsView.findViewById(R.id.no_permissions);
        displayPermissions(this.mPermGroupsList, displayList, which, showRevokeUI);
        if (displayList.getChildCount() <= 0) {
            noPermsView.setVisibility(0);
        }
        return permsView;
    }

    private void displayPermissions(List<MyPermissionGroupInfo> groups, LinearLayout permListView, int which, boolean showRevokeUI) {
        CharSequence charSequence;
        permListView.removeAllViews();
        int spacing = (int) (8.0f * this.mContext.getResources().getDisplayMetrics().density);
        for (int i = 0; i < groups.size(); i++) {
            MyPermissionGroupInfo grp = groups.get(i);
            List<MyPermissionInfo> perms = getPermissionList(grp, which);
            int j = 0;
            while (j < perms.size()) {
                MyPermissionInfo perm = perms.get(j);
                boolean z = j == 0;
                if (which != 4) {
                    charSequence = this.mNewPermPrefix;
                } else {
                    charSequence = null;
                }
                View view = getPermissionItemView(grp, perm, z, charSequence, showRevokeUI);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
                if (j == 0) {
                    lp.topMargin = spacing;
                }
                if (j == grp.mAllPermissions.size() - 1) {
                    lp.bottomMargin = spacing;
                }
                if (permListView.getChildCount() == 0) {
                    lp.topMargin *= 2;
                }
                permListView.addView(view, lp);
                j++;
            }
        }
    }

    private PermissionItemView getPermissionItemView(MyPermissionGroupInfo grp, MyPermissionInfo perm, boolean first, CharSequence newPermPrefix, boolean showRevokeUI) {
        return getPermissionItemView(this.mContext, this.mInflater, grp, perm, first, newPermPrefix, this.mPackageName, showRevokeUI);
    }

    private static PermissionItemView getPermissionItemView(Context context, LayoutInflater inflater, MyPermissionGroupInfo grp, MyPermissionInfo perm, boolean first, CharSequence newPermPrefix, String packageName, boolean showRevokeUI) {
        PermissionItemView permView = (PermissionItemView) inflater.inflate((perm.flags & 1) != 0 ? R.layout.app_permission_item_money : R.layout.app_permission_item, (ViewGroup) null);
        permView.setPermission(grp, perm, first, newPermPrefix, packageName, showRevokeUI);
        return permView;
    }

    private static View getPermissionItemViewOld(Context context, LayoutInflater inflater, CharSequence grpName, CharSequence permList, boolean dangerous, Drawable icon) {
        View permView = inflater.inflate(R.layout.app_permission_item_old, (ViewGroup) null);
        TextView permGrpView = (TextView) permView.findViewById(R.id.permission_group);
        TextView permDescView = (TextView) permView.findViewById(R.id.permission_list);
        ((ImageView) permView.findViewById(R.id.perm_icon)).setImageDrawable(icon);
        if (grpName != null) {
            permGrpView.setText(grpName);
            permDescView.setText(permList);
        } else {
            permGrpView.setText(permList);
            permDescView.setVisibility(8);
        }
        return permView;
    }

    private boolean isDisplayablePermission(PermissionInfo pInfo, int newReqFlags, int existingReqFlags) {
        boolean isNormal;
        boolean isDangerous;
        boolean isRequired;
        boolean isDevelopment;
        boolean wasGranted;
        boolean isGranted;
        int base = pInfo.protectionLevel & 15;
        if (base == 0) {
            isNormal = true;
        } else {
            isNormal = false;
        }
        if (base == 1) {
            isDangerous = true;
        } else {
            isDangerous = false;
        }
        if ((newReqFlags & 1) != 0) {
            isRequired = true;
        } else {
            isRequired = false;
        }
        if ((pInfo.protectionLevel & 32) != 0) {
            isDevelopment = true;
        } else {
            isDevelopment = false;
        }
        if ((existingReqFlags & 2) != 0) {
            wasGranted = true;
        } else {
            wasGranted = false;
        }
        if ((newReqFlags & 2) != 0) {
            isGranted = true;
        } else {
            isGranted = false;
        }
        if ((isNormal || isDangerous) && (isRequired || wasGranted || isGranted)) {
            return true;
        }
        return isDevelopment && wasGranted;
    }

    /* access modifiers changed from: private */
    public static class PermissionGroupInfoComparator implements Comparator<MyPermissionGroupInfo> {
        private final Collator sCollator = Collator.getInstance();

        PermissionGroupInfoComparator() {
        }

        public final int compare(MyPermissionGroupInfo a, MyPermissionGroupInfo b) {
            if (((a.flags ^ b.flags) & 1) != 0) {
                return (a.flags & 1) != 0 ? -1 : 1;
            }
            if (a.priority != b.priority) {
                return a.priority <= b.priority ? 1 : -1;
            }
            return this.sCollator.compare(a.mLabel, b.mLabel);
        }
    }

    /* access modifiers changed from: private */
    public static class PermissionInfoComparator implements Comparator<MyPermissionInfo> {
        private final Collator sCollator = Collator.getInstance();

        PermissionInfoComparator() {
        }

        public final int compare(MyPermissionInfo a, MyPermissionInfo b) {
            return this.sCollator.compare(a.mLabel, b.mLabel);
        }
    }

    private void addPermToList(List<MyPermissionInfo> permList, MyPermissionInfo pInfo) {
        if (pInfo.mLabel == null) {
            pInfo.mLabel = pInfo.loadLabel(this.mPm);
        }
        int idx = Collections.binarySearch(permList, pInfo, this.mPermComparator);
        if (idx < 0) {
            permList.add((-idx) - 1, pInfo);
        }
    }

    private void setPermissions(List<MyPermissionInfo> permList) {
        MyPermissionGroupInfo group;
        if (permList != null) {
            for (MyPermissionInfo pInfo : permList) {
                if (isDisplayablePermission(pInfo, pInfo.mNewReqFlags, pInfo.mExistingReqFlags) && (group = this.mPermGroups.get(pInfo.group)) != null) {
                    pInfo.mLabel = pInfo.loadLabel(this.mPm);
                    addPermToList(group.mAllPermissions, pInfo);
                    if (pInfo.mNew) {
                        addPermToList(group.mNewPermissions, pInfo);
                    }
                    if ((group.flags & 1) != 0) {
                        addPermToList(group.mPersonalPermissions, pInfo);
                    } else {
                        addPermToList(group.mDevicePermissions, pInfo);
                    }
                }
            }
        }
        for (MyPermissionGroupInfo pgrp : this.mPermGroups.values()) {
            if (pgrp.labelRes == 0 && pgrp.nonLocalizedLabel == null) {
                try {
                    pgrp.mLabel = this.mPm.getApplicationInfo(pgrp.packageName, 0).loadLabel(this.mPm);
                } catch (PackageManager.NameNotFoundException e) {
                    pgrp.mLabel = pgrp.loadLabel(this.mPm);
                }
            } else {
                pgrp.mLabel = pgrp.loadLabel(this.mPm);
            }
            this.mPermGroupsList.add(pgrp);
        }
        Collections.sort(this.mPermGroupsList, this.mPermGroupComparator);
    }
}

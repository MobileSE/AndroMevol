package android.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AppSecurityPermissions;

public class AppSecurityPermissions$PermissionItemView extends LinearLayout implements View.OnClickListener {
    AlertDialog mDialog;
    AppSecurityPermissions.MyPermissionGroupInfo mGroup;
    private String mPackageName;
    AppSecurityPermissions.MyPermissionInfo mPerm;
    private boolean mShowRevokeUI = false;

    public AppSecurityPermissions$PermissionItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setClickable(true);
    }

    public void setPermission(AppSecurityPermissions.MyPermissionGroupInfo grp, AppSecurityPermissions.MyPermissionInfo perm, boolean first, CharSequence newPermPrefix, String packageName, boolean showRevokeUI) {
        this.mGroup = grp;
        this.mPerm = perm;
        this.mShowRevokeUI = showRevokeUI;
        this.mPackageName = packageName;
        ImageView permGrpIcon = (ImageView) findViewById(16909011);
        TextView permNameView = (TextView) findViewById(16909012);
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
                sbuilder.append(getContext().getString(17040594, appName));
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
                builder.setNegativeButton(17040893, new 1(this));
                builder.setPositiveButton(17039370, (DialogInterface.OnClickListener) null);
            }
        }
    }
}

package android.widget;

import android.content.DialogInterface;
import android.widget.AppSecurityPermissions;

class AppSecurityPermissions$PermissionItemView$1 implements DialogInterface.OnClickListener {
    final /* synthetic */ AppSecurityPermissions.PermissionItemView this$0;

    AppSecurityPermissions$PermissionItemView$1(AppSecurityPermissions.PermissionItemView permissionItemView) {
        this.this$0 = permissionItemView;
    }

    public void onClick(DialogInterface dialog, int which) {
        this.this$0.getContext().getPackageManager().revokePermission(AppSecurityPermissions.PermissionItemView.access$000(this.this$0), this.this$0.mPerm.name);
        this.this$0.setVisibility(8);
    }
}

package com.android.internal.app;

import android.app.Activity;
import android.app.ActivityManagerNative;
import android.app.ActivityThread;
import android.app.AppGlobals;
import android.content.Intent;
import android.content.pm.IPackageManager;
import android.content.pm.UserInfo;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Slog;
import android.view.View;
import android.widget.Toast;
import com.android.internal.R;

public class IntentForwarderActivity extends Activity {
    public static String FORWARD_INTENT_TO_MANAGED_PROFILE = "com.android.internal.app.ForwardIntentToManagedProfile";
    public static String FORWARD_INTENT_TO_USER_OWNER = "com.android.internal.app.ForwardIntentToUserOwner";
    public static String TAG = "IntentForwarderActivity";

    /* access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        int userMessageId;
        UserHandle userDest;
        super.onCreate(savedInstanceState);
        Intent intentReceived = getIntent();
        String className = intentReceived.getComponent().getClassName();
        if (className.equals(FORWARD_INTENT_TO_USER_OWNER)) {
            userMessageId = R.string.forward_intent_to_owner;
            userDest = UserHandle.OWNER;
        } else if (className.equals(FORWARD_INTENT_TO_MANAGED_PROFILE)) {
            userMessageId = R.string.forward_intent_to_work;
            userDest = getManagedProfile();
        } else {
            Slog.wtf(TAG, IntentForwarderActivity.class.getName() + " cannot be called directly");
            userMessageId = -1;
            userDest = null;
        }
        if (userDest == null) {
            finish();
            return;
        }
        Intent newIntent = new Intent(intentReceived);
        newIntent.setComponent(null);
        newIntent.setPackage(null);
        newIntent.addFlags(View.SCROLLBARS_OUTSIDE_INSET);
        int callingUserId = getUserId();
        IPackageManager ipm = AppGlobals.getPackageManager();
        String resolvedType = newIntent.resolveTypeIfNeeded(getContentResolver());
        boolean canForward = false;
        Intent selector = newIntent.getSelector();
        if (selector == null) {
            selector = newIntent;
        }
        try {
            canForward = ipm.canForwardTo(selector, resolvedType, callingUserId, userDest.getIdentifier());
        } catch (RemoteException e) {
            Slog.e(TAG, "PackageManagerService is dead?");
        }
        if (canForward) {
            newIntent.setContentUserHint(callingUserId);
            boolean shouldShowDisclosure = !UserHandle.isSameApp(getPackageManager().resolveActivityAsUser(newIntent, 65536, userDest.getIdentifier()).activityInfo.applicationInfo.uid, 1000);
            try {
                startActivityAsCaller(newIntent, null, userDest.getIdentifier());
            } catch (RuntimeException e2) {
                int launchedFromUid = -1;
                String launchedFromPackage = "?";
                try {
                    launchedFromUid = ActivityManagerNative.getDefault().getLaunchedFromUid(getActivityToken());
                    launchedFromPackage = ActivityManagerNative.getDefault().getLaunchedFromPackage(getActivityToken());
                } catch (RemoteException e3) {
                }
                Slog.wtf(TAG, "Unable to launch as UID " + launchedFromUid + " package " + launchedFromPackage + ", while running in " + ActivityThread.currentProcessName(), e2);
            }
            if (shouldShowDisclosure) {
                Toast.makeText(this, getString(userMessageId), 1).show();
            }
        } else {
            Slog.wtf(TAG, "the intent: " + newIntent + "cannot be forwarded from user " + callingUserId + " to user " + userDest.getIdentifier());
        }
        finish();
    }

    private UserHandle getManagedProfile() {
        for (UserInfo userInfo : ((UserManager) getSystemService("user")).getProfiles(0)) {
            if (userInfo.isManagedProfile()) {
                return new UserHandle(userInfo.id);
            }
        }
        Slog.wtf(TAG, FORWARD_INTENT_TO_MANAGED_PROFILE + " has been called, but there is no managed profile");
        return null;
    }
}

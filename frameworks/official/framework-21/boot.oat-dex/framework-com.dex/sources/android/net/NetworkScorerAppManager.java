package android.net;

import android.Manifest;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class NetworkScorerAppManager {
    private static final Intent SCORE_INTENT = new Intent(NetworkScoreManager.ACTION_SCORE_NETWORKS);
    private static final String TAG = "NetworkScorerAppManager";

    private NetworkScorerAppManager() {
    }

    public static class NetworkScorerAppData {
        public final String mConfigurationActivityClassName;
        public final String mPackageName;
        public final CharSequence mScorerName;

        public NetworkScorerAppData(String packageName, CharSequence scorerName, String configurationActivityClassName) {
            this.mScorerName = scorerName;
            this.mPackageName = packageName;
            this.mConfigurationActivityClassName = configurationActivityClassName;
        }
    }

    public static Collection<NetworkScorerAppData> getAllValidScorers(Context context) {
        ActivityInfo activityInfo;
        List<NetworkScorerAppData> scorers = new ArrayList<>();
        PackageManager pm = context.getPackageManager();
        for (ResolveInfo receiver : pm.queryBroadcastReceivers(SCORE_INTENT, 0, 0)) {
            ActivityInfo receiverInfo = receiver.activityInfo;
            if (receiverInfo != null && Manifest.permission.BROADCAST_SCORE_NETWORKS.equals(receiverInfo.permission) && pm.checkPermission(Manifest.permission.SCORE_NETWORKS, receiverInfo.packageName) == 0) {
                String configurationActivityClassName = null;
                Intent intent = new Intent(NetworkScoreManager.ACTION_CUSTOM_ENABLE);
                intent.setPackage(receiverInfo.packageName);
                List<ResolveInfo> configActivities = pm.queryIntentActivities(intent, 0);
                if (!configActivities.isEmpty() && (activityInfo = configActivities.get(0).activityInfo) != null) {
                    configurationActivityClassName = activityInfo.name;
                }
                scorers.add(new NetworkScorerAppData(receiverInfo.packageName, receiverInfo.loadLabel(pm), configurationActivityClassName));
            }
        }
        return scorers;
    }

    public static NetworkScorerAppData getActiveScorer(Context context) {
        return getScorer(context, Settings.Global.getString(context.getContentResolver(), Settings.Global.NETWORK_SCORER_APP));
    }

    public static boolean setActiveScorer(Context context, String packageName) {
        String oldPackageName = Settings.Global.getString(context.getContentResolver(), Settings.Global.NETWORK_SCORER_APP);
        if (TextUtils.equals(oldPackageName, packageName)) {
            return true;
        }
        Log.i(TAG, "Changing network scorer from " + oldPackageName + " to " + packageName);
        if (packageName == null) {
            Settings.Global.putString(context.getContentResolver(), Settings.Global.NETWORK_SCORER_APP, null);
            return true;
        } else if (getScorer(context, packageName) != null) {
            Settings.Global.putString(context.getContentResolver(), Settings.Global.NETWORK_SCORER_APP, packageName);
            return true;
        } else {
            Log.w(TAG, "Requested network scorer is not valid: " + packageName);
            return false;
        }
    }

    public static boolean isCallerActiveScorer(Context context, int callingUid) {
        NetworkScorerAppData defaultApp = getActiveScorer(context);
        if (defaultApp == null) {
            return false;
        }
        try {
            ((AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE)).checkPackage(callingUid, defaultApp.mPackageName);
            if (context.checkCallingPermission(Manifest.permission.SCORE_NETWORKS) == 0) {
                return true;
            }
            return false;
        } catch (SecurityException e) {
            return false;
        }
    }

    public static NetworkScorerAppData getScorer(Context context, String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return null;
        }
        for (NetworkScorerAppData app : getAllValidScorers(context)) {
            if (packageName.equals(app.mPackageName)) {
                return app;
            }
        }
        return null;
    }
}

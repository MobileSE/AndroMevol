package android.app.backup;

import android.app.backup.IBackupManager;
import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

public class BackupManager {
    private static final String TAG = "BackupManager";
    private static IBackupManager sService;
    private Context mContext;

    private static void checkServiceBinder() {
        if (sService == null) {
            sService = IBackupManager.Stub.asInterface(ServiceManager.getService(Context.BACKUP_SERVICE));
        }
    }

    public BackupManager(Context context) {
        this.mContext = context;
    }

    public void dataChanged() {
        checkServiceBinder();
        if (sService != null) {
            try {
                sService.dataChanged(this.mContext.getPackageName());
            } catch (RemoteException e) {
                Log.d(TAG, "dataChanged() couldn't connect");
            }
        }
    }

    public static void dataChanged(String packageName) {
        checkServiceBinder();
        if (sService != null) {
            try {
                sService.dataChanged(packageName);
            } catch (RemoteException e) {
                Log.e(TAG, "dataChanged(pkg) couldn't connect");
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:18:0x003b  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0042  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int requestRestore(android.app.backup.RestoreObserver r9) {
        /*
            r8 = this;
            r2 = -1
            checkServiceBinder()
            android.app.backup.IBackupManager r5 = android.app.backup.BackupManager.sService
            if (r5 == 0) goto L_0x002f
            r3 = 0
            android.app.backup.IBackupManager r5 = android.app.backup.BackupManager.sService     // Catch:{ RemoteException -> 0x0030 }
            android.content.Context r6 = r8.mContext     // Catch:{ RemoteException -> 0x0030 }
            java.lang.String r6 = r6.getPackageName()     // Catch:{ RemoteException -> 0x0030 }
            r7 = 0
            android.app.backup.IRestoreSession r0 = r5.beginRestoreSession(r6, r7)     // Catch:{ RemoteException -> 0x0030 }
            if (r0 == 0) goto L_0x002a
            android.app.backup.RestoreSession r4 = new android.app.backup.RestoreSession     // Catch:{ RemoteException -> 0x0030 }
            android.content.Context r5 = r8.mContext     // Catch:{ RemoteException -> 0x0030 }
            r4.<init>(r5, r0)     // Catch:{ RemoteException -> 0x0030 }
            android.content.Context r5 = r8.mContext     // Catch:{ RemoteException -> 0x0049, all -> 0x0046 }
            java.lang.String r5 = r5.getPackageName()     // Catch:{ RemoteException -> 0x0049, all -> 0x0046 }
            int r2 = r4.restorePackage(r5, r9)     // Catch:{ RemoteException -> 0x0049, all -> 0x0046 }
            r3 = r4
        L_0x002a:
            if (r3 == 0) goto L_0x002f
            r3.endRestoreSession()
        L_0x002f:
            return r2
        L_0x0030:
            r1 = move-exception
        L_0x0031:
            java.lang.String r5 = "BackupManager"
            java.lang.String r6 = "restoreSelf() unable to contact service"
            android.util.Log.e(r5, r6)     // Catch:{ all -> 0x003f }
            if (r3 == 0) goto L_0x002f
            r3.endRestoreSession()
            goto L_0x002f
        L_0x003f:
            r5 = move-exception
        L_0x0040:
            if (r3 == 0) goto L_0x0045
            r3.endRestoreSession()
        L_0x0045:
            throw r5
        L_0x0046:
            r5 = move-exception
            r3 = r4
            goto L_0x0040
        L_0x0049:
            r1 = move-exception
            r3 = r4
            goto L_0x0031
        */
        throw new UnsupportedOperationException("Method not decompiled: android.app.backup.BackupManager.requestRestore(android.app.backup.RestoreObserver):int");
    }

    public RestoreSession beginRestoreSession() {
        checkServiceBinder();
        if (sService == null) {
            return null;
        }
        try {
            IRestoreSession binder = sService.beginRestoreSession(null, null);
            if (binder != null) {
                return new RestoreSession(this.mContext, binder);
            }
            return null;
        } catch (RemoteException e) {
            Log.e(TAG, "beginRestoreSession() couldn't connect");
            return null;
        }
    }

    public void setBackupEnabled(boolean isEnabled) {
        checkServiceBinder();
        if (sService != null) {
            try {
                sService.setBackupEnabled(isEnabled);
            } catch (RemoteException e) {
                Log.e(TAG, "setBackupEnabled() couldn't connect");
            }
        }
    }

    public boolean isBackupEnabled() {
        checkServiceBinder();
        if (sService != null) {
            try {
                return sService.isBackupEnabled();
            } catch (RemoteException e) {
                Log.e(TAG, "isBackupEnabled() couldn't connect");
            }
        }
        return false;
    }

    public void setAutoRestore(boolean isEnabled) {
        checkServiceBinder();
        if (sService != null) {
            try {
                sService.setAutoRestore(isEnabled);
            } catch (RemoteException e) {
                Log.e(TAG, "setAutoRestore() couldn't connect");
            }
        }
    }

    public String getCurrentTransport() {
        checkServiceBinder();
        if (sService != null) {
            try {
                return sService.getCurrentTransport();
            } catch (RemoteException e) {
                Log.e(TAG, "getCurrentTransport() couldn't connect");
            }
        }
        return null;
    }

    public String[] listAllTransports() {
        checkServiceBinder();
        if (sService != null) {
            try {
                return sService.listAllTransports();
            } catch (RemoteException e) {
                Log.e(TAG, "listAllTransports() couldn't connect");
            }
        }
        return null;
    }

    public String selectBackupTransport(String transport) {
        checkServiceBinder();
        if (sService != null) {
            try {
                return sService.selectBackupTransport(transport);
            } catch (RemoteException e) {
                Log.e(TAG, "selectBackupTransport() couldn't connect");
            }
        }
        return null;
    }

    public void backupNow() {
        checkServiceBinder();
        if (sService != null) {
            try {
                sService.backupNow();
            } catch (RemoteException e) {
                Log.e(TAG, "backupNow() couldn't connect");
            }
        }
    }
}

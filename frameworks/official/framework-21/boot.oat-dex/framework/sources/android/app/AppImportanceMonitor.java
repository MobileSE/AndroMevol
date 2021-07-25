package android.app;

import android.app.ActivityManager;
import android.app.IProcessObserver;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.SparseArray;
import java.util.List;

public class AppImportanceMonitor {
    static final int MSG_UPDATE = 1;
    final SparseArray<AppEntry> mApps = new SparseArray<>();
    final Context mContext;
    final Handler mHandler;
    final IProcessObserver mProcessObserver = new IProcessObserver.Stub() {
        /* class android.app.AppImportanceMonitor.AnonymousClass1 */

        @Override // android.app.IProcessObserver
        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
        }

        @Override // android.app.IProcessObserver
        public void onProcessStateChanged(int pid, int uid, int procState) {
            synchronized (AppImportanceMonitor.this.mApps) {
                AppImportanceMonitor.this.updateImportanceLocked(pid, uid, ActivityManager.RunningAppProcessInfo.procStateToImportance(procState), true);
            }
        }

        @Override // android.app.IProcessObserver
        public void onProcessDied(int pid, int uid) {
            synchronized (AppImportanceMonitor.this.mApps) {
                AppImportanceMonitor.this.updateImportanceLocked(pid, uid, 1000, true);
            }
        }
    };

    /* access modifiers changed from: package-private */
    public static class AppEntry {
        int importance = 1000;
        final SparseArray<Integer> procs = new SparseArray<>(1);
        final int uid;

        AppEntry(int _uid) {
            this.uid = _uid;
        }
    }

    public AppImportanceMonitor(Context context, Looper looper) {
        this.mContext = context;
        this.mHandler = new Handler(looper) {
            /* class android.app.AppImportanceMonitor.AnonymousClass2 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        AppImportanceMonitor.this.onImportanceChanged(msg.arg1, msg.arg2 & 65535, msg.arg2 >> 16);
                        return;
                    default:
                        super.handleMessage(msg);
                        return;
                }
            }
        };
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        try {
            ActivityManagerNative.getDefault().registerProcessObserver(this.mProcessObserver);
        } catch (RemoteException e) {
        }
        List<ActivityManager.RunningAppProcessInfo> apps = am.getRunningAppProcesses();
        if (apps != null) {
            for (int i = 0; i < apps.size(); i++) {
                ActivityManager.RunningAppProcessInfo app = apps.get(i);
                updateImportanceLocked(app.uid, app.pid, app.importance, false);
            }
        }
    }

    public int getImportance(int uid) {
        AppEntry ent = this.mApps.get(uid);
        if (ent == null) {
            return 1000;
        }
        return ent.importance;
    }

    public void onImportanceChanged(int uid, int importance, int oldImportance) {
    }

    /* access modifiers changed from: package-private */
    public void updateImportanceLocked(int uid, int pid, int importance, boolean repChange) {
        AppEntry ent = this.mApps.get(uid);
        if (ent == null) {
            ent = new AppEntry(uid);
            this.mApps.put(uid, ent);
        }
        if (importance >= 1000) {
            ent.procs.remove(pid);
        } else {
            ent.procs.put(pid, Integer.valueOf(importance));
        }
        updateImportanceLocked(ent, repChange);
    }

    /* access modifiers changed from: package-private */
    public void updateImportanceLocked(AppEntry ent, boolean repChange) {
        int appImp = 1000;
        for (int i = 0; i < ent.procs.size(); i++) {
            int procImp = ent.procs.valueAt(i).intValue();
            if (procImp < appImp) {
                appImp = procImp;
            }
        }
        if (appImp != ent.importance) {
            int impCode = appImp | (ent.importance << 16);
            ent.importance = appImp;
            if (appImp >= 1000) {
                this.mApps.remove(ent.uid);
            }
            if (repChange) {
                this.mHandler.obtainMessage(1, ent.uid, impCode).sendToTarget();
            }
        }
    }
}

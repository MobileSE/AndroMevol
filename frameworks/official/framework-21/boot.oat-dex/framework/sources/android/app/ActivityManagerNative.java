package android.app;

import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Singleton;

public abstract class ActivityManagerNative extends Binder implements IActivityManager {
    private static final Singleton<IActivityManager> gDefault = new Singleton<IActivityManager>() {
        /* class android.app.ActivityManagerNative.AnonymousClass1 */

        /* access modifiers changed from: protected */
        @Override // android.util.Singleton
        public IActivityManager create() {
            return ActivityManagerNative.asInterface(ServiceManager.getService(Context.ACTIVITY_SERVICE));
        }
    };
    static boolean sSystemReady = false;

    public static IActivityManager asInterface(IBinder obj) {
        if (obj == null) {
            return null;
        }
        IActivityManager in = (IActivityManager) obj.queryLocalInterface(IActivityManager.descriptor);
        return in == null ? new ActivityManagerProxy(obj) : in;
    }

    public static IActivityManager getDefault() {
        return gDefault.get();
    }

    public static boolean isSystemReady() {
        if (!sSystemReady) {
            sSystemReady = getDefault().testIsSystemReady();
        }
        return sSystemReady;
    }

    public static void broadcastStickyIntent(Intent intent, String permission, int userId) {
        try {
            getDefault().broadcastIntent(null, intent, null, null, -1, null, null, null, -1, false, true, userId);
        } catch (RemoteException e) {
        }
    }

    public static void noteWakeupAlarm(PendingIntent ps, int sourceUid, String sourcePkg) {
        try {
            getDefault().noteWakeupAlarm(ps.getTarget(), sourceUid, sourcePkg);
        } catch (RemoteException e) {
        }
    }

    public ActivityManagerNative() {
        attachInterface(this, IActivityManager.descriptor);
    }

    /*  JADX ERROR: IndexOutOfBoundsException in pass: SSATransform
        java.lang.IndexOutOfBoundsException: bitIndex < 0: -35
        	at java.util.BitSet.get(BitSet.java:623)
        	at jadx.core.dex.visitors.ssa.LiveVarAnalysis.fillBasicBlockInfo(LiveVarAnalysis.java:65)
        	at jadx.core.dex.visitors.ssa.LiveVarAnalysis.runAnalysis(LiveVarAnalysis.java:36)
        	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:50)
        	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:41)
        */
    @Override // android.os.Binder
    public boolean onTransact(int r254, android.os.Parcel r255, android.os.Parcel r256, int r257) throws android.os.RemoteException {
        /*
        // Method dump skipped, instructions count: 9010
        */
        throw new UnsupportedOperationException("Method not decompiled: android.app.ActivityManagerNative.onTransact(int, android.os.Parcel, android.os.Parcel, int):boolean");
    }

    @Override // android.os.IInterface
    public IBinder asBinder() {
        return this;
    }
}

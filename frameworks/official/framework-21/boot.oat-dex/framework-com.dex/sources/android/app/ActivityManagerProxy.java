package android.app;

import android.app.ActivityManager;
import android.app.ApplicationErrorReport;
import android.app.IActivityContainer;
import android.app.IActivityManager;
import android.app.IAppTask;
import android.content.ComponentName;
import android.content.IIntentReceiver;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.UriPermission;
import android.content.pm.ApplicationInfo;
import android.content.pm.ConfigurationInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.ParceledListSlice;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.os.IBinder;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.StrictMode;
import android.service.voice.IVoiceInteractionSession;
import android.text.TextUtils;
import com.android.internal.app.IVoiceInteractor;
import java.util.ArrayList;
import java.util.List;

/* access modifiers changed from: package-private */
/* compiled from: ActivityManagerNative */
public class ActivityManagerProxy implements IActivityManager {
    private IBinder mRemote;

    public ActivityManagerProxy(IBinder remote) {
        this.mRemote = remote;
    }

    @Override // android.os.IInterface
    public IBinder asBinder() {
        return this.mRemote;
    }

    @Override // android.app.IActivityManager
    public int startActivity(IApplicationThread caller, String callingPackage, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int startFlags, ProfilerInfo profilerInfo, Bundle options) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(caller != null ? caller.asBinder() : null);
        data.writeString(callingPackage);
        intent.writeToParcel(data, 0);
        data.writeString(resolvedType);
        data.writeStrongBinder(resultTo);
        data.writeString(resultWho);
        data.writeInt(requestCode);
        data.writeInt(startFlags);
        if (profilerInfo != null) {
            data.writeInt(1);
            profilerInfo.writeToParcel(data, 1);
        } else {
            data.writeInt(0);
        }
        if (options != null) {
            data.writeInt(1);
            options.writeToParcel(data, 0);
        } else {
            data.writeInt(0);
        }
        this.mRemote.transact(3, data, reply, 0);
        reply.readException();
        int result = reply.readInt();
        reply.recycle();
        data.recycle();
        return result;
    }

    @Override // android.app.IActivityManager
    public int startActivityAsUser(IApplicationThread caller, String callingPackage, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int startFlags, ProfilerInfo profilerInfo, Bundle options, int userId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(caller != null ? caller.asBinder() : null);
        data.writeString(callingPackage);
        intent.writeToParcel(data, 0);
        data.writeString(resolvedType);
        data.writeStrongBinder(resultTo);
        data.writeString(resultWho);
        data.writeInt(requestCode);
        data.writeInt(startFlags);
        if (profilerInfo != null) {
            data.writeInt(1);
            profilerInfo.writeToParcel(data, 1);
        } else {
            data.writeInt(0);
        }
        if (options != null) {
            data.writeInt(1);
            options.writeToParcel(data, 0);
        } else {
            data.writeInt(0);
        }
        data.writeInt(userId);
        this.mRemote.transact(153, data, reply, 0);
        reply.readException();
        int result = reply.readInt();
        reply.recycle();
        data.recycle();
        return result;
    }

    @Override // android.app.IActivityManager
    public int startActivityAsCaller(IApplicationThread caller, String callingPackage, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int startFlags, ProfilerInfo profilerInfo, Bundle options, int userId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(caller != null ? caller.asBinder() : null);
        data.writeString(callingPackage);
        intent.writeToParcel(data, 0);
        data.writeString(resolvedType);
        data.writeStrongBinder(resultTo);
        data.writeString(resultWho);
        data.writeInt(requestCode);
        data.writeInt(startFlags);
        if (profilerInfo != null) {
            data.writeInt(1);
            profilerInfo.writeToParcel(data, 1);
        } else {
            data.writeInt(0);
        }
        if (options != null) {
            data.writeInt(1);
            options.writeToParcel(data, 0);
        } else {
            data.writeInt(0);
        }
        data.writeInt(userId);
        this.mRemote.transact(233, data, reply, 0);
        reply.readException();
        int result = reply.readInt();
        reply.recycle();
        data.recycle();
        return result;
    }

    @Override // android.app.IActivityManager
    public IActivityManager.WaitResult startActivityAndWait(IApplicationThread caller, String callingPackage, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int startFlags, ProfilerInfo profilerInfo, Bundle options, int userId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(caller != null ? caller.asBinder() : null);
        data.writeString(callingPackage);
        intent.writeToParcel(data, 0);
        data.writeString(resolvedType);
        data.writeStrongBinder(resultTo);
        data.writeString(resultWho);
        data.writeInt(requestCode);
        data.writeInt(startFlags);
        if (profilerInfo != null) {
            data.writeInt(1);
            profilerInfo.writeToParcel(data, 1);
        } else {
            data.writeInt(0);
        }
        if (options != null) {
            data.writeInt(1);
            options.writeToParcel(data, 0);
        } else {
            data.writeInt(0);
        }
        data.writeInt(userId);
        this.mRemote.transact(105, data, reply, 0);
        reply.readException();
        IActivityManager.WaitResult result = IActivityManager.WaitResult.CREATOR.createFromParcel(reply);
        reply.recycle();
        data.recycle();
        return result;
    }

    @Override // android.app.IActivityManager
    public int startActivityWithConfig(IApplicationThread caller, String callingPackage, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int startFlags, Configuration config, Bundle options, int userId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(caller != null ? caller.asBinder() : null);
        data.writeString(callingPackage);
        intent.writeToParcel(data, 0);
        data.writeString(resolvedType);
        data.writeStrongBinder(resultTo);
        data.writeString(resultWho);
        data.writeInt(requestCode);
        data.writeInt(startFlags);
        config.writeToParcel(data, 0);
        if (options != null) {
            data.writeInt(1);
            options.writeToParcel(data, 0);
        } else {
            data.writeInt(0);
        }
        data.writeInt(userId);
        this.mRemote.transact(3, data, reply, 0);
        reply.readException();
        int result = reply.readInt();
        reply.recycle();
        data.recycle();
        return result;
    }

    @Override // android.app.IActivityManager
    public int startActivityIntentSender(IApplicationThread caller, IntentSender intent, Intent fillInIntent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int flagsMask, int flagsValues, Bundle options) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(caller != null ? caller.asBinder() : null);
        intent.writeToParcel(data, 0);
        if (fillInIntent != null) {
            data.writeInt(1);
            fillInIntent.writeToParcel(data, 0);
        } else {
            data.writeInt(0);
        }
        data.writeString(resolvedType);
        data.writeStrongBinder(resultTo);
        data.writeString(resultWho);
        data.writeInt(requestCode);
        data.writeInt(flagsMask);
        data.writeInt(flagsValues);
        if (options != null) {
            data.writeInt(1);
            options.writeToParcel(data, 0);
        } else {
            data.writeInt(0);
        }
        this.mRemote.transact(100, data, reply, 0);
        reply.readException();
        int result = reply.readInt();
        reply.recycle();
        data.recycle();
        return result;
    }

    @Override // android.app.IActivityManager
    public int startVoiceActivity(String callingPackage, int callingPid, int callingUid, Intent intent, String resolvedType, IVoiceInteractionSession session, IVoiceInteractor interactor, int startFlags, ProfilerInfo profilerInfo, Bundle options, int userId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(callingPackage);
        data.writeInt(callingPid);
        data.writeInt(callingUid);
        intent.writeToParcel(data, 0);
        data.writeString(resolvedType);
        data.writeStrongBinder(session.asBinder());
        data.writeStrongBinder(interactor.asBinder());
        data.writeInt(startFlags);
        if (profilerInfo != null) {
            data.writeInt(1);
            profilerInfo.writeToParcel(data, 1);
        } else {
            data.writeInt(0);
        }
        if (options != null) {
            data.writeInt(1);
            options.writeToParcel(data, 0);
        } else {
            data.writeInt(0);
        }
        data.writeInt(userId);
        this.mRemote.transact(219, data, reply, 0);
        reply.readException();
        int result = reply.readInt();
        reply.recycle();
        data.recycle();
        return result;
    }

    @Override // android.app.IActivityManager
    public boolean startNextMatchingActivity(IBinder callingActivity, Intent intent, Bundle options) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(callingActivity);
        intent.writeToParcel(data, 0);
        if (options != null) {
            data.writeInt(1);
            options.writeToParcel(data, 0);
        } else {
            data.writeInt(0);
        }
        this.mRemote.transact(67, data, reply, 0);
        reply.readException();
        int result = reply.readInt();
        reply.recycle();
        data.recycle();
        return result != 0;
    }

    @Override // android.app.IActivityManager
    public int startActivityFromRecents(int taskId, Bundle options) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(taskId);
        if (options == null) {
            data.writeInt(0);
        } else {
            data.writeInt(1);
            options.writeToParcel(data, 0);
        }
        this.mRemote.transact(230, data, reply, 0);
        reply.readException();
        int result = reply.readInt();
        reply.recycle();
        data.recycle();
        return result;
    }

    @Override // android.app.IActivityManager
    public boolean finishActivity(IBinder token, int resultCode, Intent resultData, boolean finishTask) throws RemoteException {
        int i;
        boolean res;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        data.writeInt(resultCode);
        if (resultData != null) {
            data.writeInt(1);
            resultData.writeToParcel(data, 0);
        } else {
            data.writeInt(0);
        }
        if (finishTask) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        this.mRemote.transact(11, data, reply, 0);
        reply.readException();
        if (reply.readInt() != 0) {
            res = true;
        } else {
            res = false;
        }
        data.recycle();
        reply.recycle();
        return res;
    }

    @Override // android.app.IActivityManager
    public void finishSubActivity(IBinder token, String resultWho, int requestCode) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        data.writeString(resultWho);
        data.writeInt(requestCode);
        this.mRemote.transact(32, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public boolean finishActivityAffinity(IBinder token) throws RemoteException {
        boolean res = false;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        this.mRemote.transact(149, data, reply, 0);
        reply.readException();
        if (reply.readInt() != 0) {
            res = true;
        }
        data.recycle();
        reply.recycle();
        return res;
    }

    @Override // android.app.IActivityManager
    public void finishVoiceTask(IVoiceInteractionSession session) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(session.asBinder());
        this.mRemote.transact(224, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public boolean releaseActivityInstance(IBinder token) throws RemoteException {
        boolean res = false;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        this.mRemote.transact(236, data, reply, 0);
        reply.readException();
        if (reply.readInt() != 0) {
            res = true;
        }
        data.recycle();
        reply.recycle();
        return res;
    }

    @Override // android.app.IActivityManager
    public void releaseSomeActivities(IApplicationThread app) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(app.asBinder());
        this.mRemote.transact(237, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public boolean willActivityBeVisible(IBinder token) throws RemoteException {
        boolean res = false;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        this.mRemote.transact(106, data, reply, 0);
        reply.readException();
        if (reply.readInt() != 0) {
            res = true;
        }
        data.recycle();
        reply.recycle();
        return res;
    }

    @Override // android.app.IActivityManager
    public Intent registerReceiver(IApplicationThread caller, String packageName, IIntentReceiver receiver, IntentFilter filter, String perm, int userId) throws RemoteException {
        IBinder iBinder;
        IBinder iBinder2 = null;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        if (caller != null) {
            iBinder = caller.asBinder();
        } else {
            iBinder = null;
        }
        data.writeStrongBinder(iBinder);
        data.writeString(packageName);
        if (receiver != null) {
            iBinder2 = receiver.asBinder();
        }
        data.writeStrongBinder(iBinder2);
        filter.writeToParcel(data, 0);
        data.writeString(perm);
        data.writeInt(userId);
        this.mRemote.transact(12, data, reply, 0);
        reply.readException();
        Intent intent = null;
        if (reply.readInt() != 0) {
            intent = Intent.CREATOR.createFromParcel(reply);
        }
        reply.recycle();
        data.recycle();
        return intent;
    }

    @Override // android.app.IActivityManager
    public void unregisterReceiver(IIntentReceiver receiver) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(receiver.asBinder());
        this.mRemote.transact(13, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public int broadcastIntent(IApplicationThread caller, Intent intent, String resolvedType, IIntentReceiver resultTo, int resultCode, String resultData, Bundle map, String requiredPermission, int appOp, boolean serialized, boolean sticky, int userId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(caller != null ? caller.asBinder() : null);
        intent.writeToParcel(data, 0);
        data.writeString(resolvedType);
        data.writeStrongBinder(resultTo != null ? resultTo.asBinder() : null);
        data.writeInt(resultCode);
        data.writeString(resultData);
        data.writeBundle(map);
        data.writeString(requiredPermission);
        data.writeInt(appOp);
        data.writeInt(serialized ? 1 : 0);
        data.writeInt(sticky ? 1 : 0);
        data.writeInt(userId);
        this.mRemote.transact(14, data, reply, 0);
        reply.readException();
        int res = reply.readInt();
        reply.recycle();
        data.recycle();
        return res;
    }

    @Override // android.app.IActivityManager
    public void unbroadcastIntent(IApplicationThread caller, Intent intent, int userId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(caller != null ? caller.asBinder() : null);
        intent.writeToParcel(data, 0);
        data.writeInt(userId);
        this.mRemote.transact(15, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public void finishReceiver(IBinder who, int resultCode, String resultData, Bundle map, boolean abortBroadcast) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(who);
        data.writeInt(resultCode);
        data.writeString(resultData);
        data.writeBundle(map);
        data.writeInt(abortBroadcast ? 1 : 0);
        this.mRemote.transact(16, data, reply, 1);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public void attachApplication(IApplicationThread app) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(app.asBinder());
        this.mRemote.transact(17, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public void activityIdle(IBinder token, Configuration config, boolean stopProfiling) throws RemoteException {
        int i = 0;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        if (config != null) {
            data.writeInt(1);
            config.writeToParcel(data, 0);
        } else {
            data.writeInt(0);
        }
        if (stopProfiling) {
            i = 1;
        }
        data.writeInt(i);
        this.mRemote.transact(18, data, reply, 1);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public void activityResumed(IBinder token) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        this.mRemote.transact(39, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public void activityPaused(IBinder token) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        this.mRemote.transact(19, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public void activityStopped(IBinder token, Bundle state, PersistableBundle persistentState, CharSequence description) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        data.writeBundle(state);
        data.writePersistableBundle(persistentState);
        TextUtils.writeToParcel(description, data, 0);
        this.mRemote.transact(20, data, reply, 1);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public void activitySlept(IBinder token) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        this.mRemote.transact(123, data, reply, 1);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public void activityDestroyed(IBinder token) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        this.mRemote.transact(62, data, reply, 1);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public String getCallingPackage(IBinder token) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        this.mRemote.transact(21, data, reply, 0);
        reply.readException();
        String res = reply.readString();
        data.recycle();
        reply.recycle();
        return res;
    }

    @Override // android.app.IActivityManager
    public ComponentName getCallingActivity(IBinder token) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        this.mRemote.transact(22, data, reply, 0);
        reply.readException();
        ComponentName res = ComponentName.readFromParcel(reply);
        data.recycle();
        reply.recycle();
        return res;
    }

    @Override // android.app.IActivityManager
    public List<IAppTask> getAppTasks(String callingPackage) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(callingPackage);
        this.mRemote.transact(221, data, reply, 0);
        reply.readException();
        ArrayList<IAppTask> list = null;
        int N = reply.readInt();
        if (N >= 0) {
            list = new ArrayList<>();
            while (N > 0) {
                list.add(IAppTask.Stub.asInterface(reply.readStrongBinder()));
                N--;
            }
        }
        data.recycle();
        reply.recycle();
        return list;
    }

    @Override // android.app.IActivityManager
    public int addAppTask(IBinder activityToken, Intent intent, ActivityManager.TaskDescription description, Bitmap thumbnail) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(activityToken);
        intent.writeToParcel(data, 0);
        description.writeToParcel(data, 0);
        thumbnail.writeToParcel(data, 0);
        this.mRemote.transact(234, data, reply, 0);
        reply.readException();
        int res = reply.readInt();
        data.recycle();
        reply.recycle();
        return res;
    }

    @Override // android.app.IActivityManager
    public Point getAppTaskThumbnailSize() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(235, data, reply, 0);
        reply.readException();
        Point size = Point.CREATOR.createFromParcel(reply);
        data.recycle();
        reply.recycle();
        return size;
    }

    @Override // android.app.IActivityManager
    public List getTasks(int maxNum, int flags) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(maxNum);
        data.writeInt(flags);
        this.mRemote.transact(23, data, reply, 0);
        reply.readException();
        ArrayList list = null;
        int N = reply.readInt();
        if (N >= 0) {
            list = new ArrayList();
            while (N > 0) {
                list.add(ActivityManager.RunningTaskInfo.CREATOR.createFromParcel(reply));
                N--;
            }
        }
        data.recycle();
        reply.recycle();
        return list;
    }

    @Override // android.app.IActivityManager
    public List<ActivityManager.RecentTaskInfo> getRecentTasks(int maxNum, int flags, int userId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(maxNum);
        data.writeInt(flags);
        data.writeInt(userId);
        this.mRemote.transact(60, data, reply, 0);
        reply.readException();
        ArrayList<ActivityManager.RecentTaskInfo> list = reply.createTypedArrayList(ActivityManager.RecentTaskInfo.CREATOR);
        data.recycle();
        reply.recycle();
        return list;
    }

    @Override // android.app.IActivityManager
    public ActivityManager.TaskThumbnail getTaskThumbnail(int id) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(id);
        this.mRemote.transact(82, data, reply, 0);
        reply.readException();
        ActivityManager.TaskThumbnail taskThumbnail = null;
        if (reply.readInt() != 0) {
            taskThumbnail = ActivityManager.TaskThumbnail.CREATOR.createFromParcel(reply);
        }
        data.recycle();
        reply.recycle();
        return taskThumbnail;
    }

    @Override // android.app.IActivityManager
    public List getServices(int maxNum, int flags) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(maxNum);
        data.writeInt(flags);
        this.mRemote.transact(81, data, reply, 0);
        reply.readException();
        ArrayList list = null;
        int N = reply.readInt();
        if (N >= 0) {
            list = new ArrayList();
            while (N > 0) {
                list.add(ActivityManager.RunningServiceInfo.CREATOR.createFromParcel(reply));
                N--;
            }
        }
        data.recycle();
        reply.recycle();
        return list;
    }

    @Override // android.app.IActivityManager
    public List<ActivityManager.ProcessErrorStateInfo> getProcessesInErrorState() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(77, data, reply, 0);
        reply.readException();
        ArrayList<ActivityManager.ProcessErrorStateInfo> list = reply.createTypedArrayList(ActivityManager.ProcessErrorStateInfo.CREATOR);
        data.recycle();
        reply.recycle();
        return list;
    }

    @Override // android.app.IActivityManager
    public List<ActivityManager.RunningAppProcessInfo> getRunningAppProcesses() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(83, data, reply, 0);
        reply.readException();
        ArrayList<ActivityManager.RunningAppProcessInfo> list = reply.createTypedArrayList(ActivityManager.RunningAppProcessInfo.CREATOR);
        data.recycle();
        reply.recycle();
        return list;
    }

    @Override // android.app.IActivityManager
    public List<ApplicationInfo> getRunningExternalApplications() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(108, data, reply, 0);
        reply.readException();
        ArrayList<ApplicationInfo> list = reply.createTypedArrayList(ApplicationInfo.CREATOR);
        data.recycle();
        reply.recycle();
        return list;
    }

    @Override // android.app.IActivityManager
    public void moveTaskToFront(int task, int flags, Bundle options) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(task);
        data.writeInt(flags);
        if (options != null) {
            data.writeInt(1);
            options.writeToParcel(data, 0);
        } else {
            data.writeInt(0);
        }
        this.mRemote.transact(24, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public void moveTaskToBack(int task) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(task);
        this.mRemote.transact(25, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public boolean moveActivityTaskToBack(IBinder token, boolean nonRoot) throws RemoteException {
        int i;
        boolean res;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        if (nonRoot) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        this.mRemote.transact(75, data, reply, 0);
        reply.readException();
        if (reply.readInt() != 0) {
            res = true;
        } else {
            res = false;
        }
        data.recycle();
        reply.recycle();
        return res;
    }

    @Override // android.app.IActivityManager
    public void moveTaskBackwards(int task) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(task);
        this.mRemote.transact(26, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public void moveTaskToStack(int taskId, int stackId, boolean toTop) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(taskId);
        data.writeInt(stackId);
        data.writeInt(toTop ? 1 : 0);
        this.mRemote.transact(169, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public void resizeStack(int stackBoxId, Rect r) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(stackBoxId);
        r.writeToParcel(data, 0);
        this.mRemote.transact(170, data, reply, 1);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public List<ActivityManager.StackInfo> getAllStackInfos() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(171, data, reply, 0);
        reply.readException();
        ArrayList<ActivityManager.StackInfo> list = reply.createTypedArrayList(ActivityManager.StackInfo.CREATOR);
        data.recycle();
        reply.recycle();
        return list;
    }

    @Override // android.app.IActivityManager
    public ActivityManager.StackInfo getStackInfo(int stackId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(stackId);
        this.mRemote.transact(173, data, reply, 0);
        reply.readException();
        ActivityManager.StackInfo info = null;
        if (reply.readInt() != 0) {
            info = ActivityManager.StackInfo.CREATOR.createFromParcel(reply);
        }
        data.recycle();
        reply.recycle();
        return info;
    }

    @Override // android.app.IActivityManager
    public boolean isInHomeStack(int taskId) throws RemoteException {
        boolean isInHomeStack = false;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(taskId);
        this.mRemote.transact(213, data, reply, 0);
        reply.readException();
        if (reply.readInt() > 0) {
            isInHomeStack = true;
        }
        data.recycle();
        reply.recycle();
        return isInHomeStack;
    }

    @Override // android.app.IActivityManager
    public void setFocusedStack(int stackId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(stackId);
        this.mRemote.transact(172, data, reply, 1);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public int getTaskForActivity(IBinder token, boolean onlyRoot) throws RemoteException {
        int i;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        if (onlyRoot) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        this.mRemote.transact(27, data, reply, 0);
        reply.readException();
        int res = reply.readInt();
        data.recycle();
        reply.recycle();
        return res;
    }

    @Override // android.app.IActivityManager
    public IActivityManager.ContentProviderHolder getContentProvider(IApplicationThread caller, String name, int userId, boolean stable) throws RemoteException {
        int i;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(caller != null ? caller.asBinder() : null);
        data.writeString(name);
        data.writeInt(userId);
        if (stable) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        this.mRemote.transact(29, data, reply, 0);
        reply.readException();
        IActivityManager.ContentProviderHolder cph = null;
        if (reply.readInt() != 0) {
            cph = IActivityManager.ContentProviderHolder.CREATOR.createFromParcel(reply);
        }
        data.recycle();
        reply.recycle();
        return cph;
    }

    @Override // android.app.IActivityManager
    public IActivityManager.ContentProviderHolder getContentProviderExternal(String name, int userId, IBinder token) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(name);
        data.writeInt(userId);
        data.writeStrongBinder(token);
        this.mRemote.transact(141, data, reply, 0);
        reply.readException();
        IActivityManager.ContentProviderHolder cph = null;
        if (reply.readInt() != 0) {
            cph = IActivityManager.ContentProviderHolder.CREATOR.createFromParcel(reply);
        }
        data.recycle();
        reply.recycle();
        return cph;
    }

    @Override // android.app.IActivityManager
    public void publishContentProviders(IApplicationThread caller, List<IActivityManager.ContentProviderHolder> providers) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(caller != null ? caller.asBinder() : null);
        data.writeTypedList(providers);
        this.mRemote.transact(30, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public boolean refContentProvider(IBinder connection, int stable, int unstable) throws RemoteException {
        boolean res = false;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(connection);
        data.writeInt(stable);
        data.writeInt(unstable);
        this.mRemote.transact(31, data, reply, 0);
        reply.readException();
        if (reply.readInt() != 0) {
            res = true;
        }
        data.recycle();
        reply.recycle();
        return res;
    }

    @Override // android.app.IActivityManager
    public void unstableProviderDied(IBinder connection) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(connection);
        this.mRemote.transact(151, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public void appNotRespondingViaProvider(IBinder connection) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(connection);
        this.mRemote.transact(183, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public void removeContentProvider(IBinder connection, boolean stable) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(connection);
        data.writeInt(stable ? 1 : 0);
        this.mRemote.transact(69, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public void removeContentProviderExternal(String name, IBinder token) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(name);
        data.writeStrongBinder(token);
        this.mRemote.transact(142, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public PendingIntent getRunningServiceControlPanel(ComponentName service) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        service.writeToParcel(data, 0);
        this.mRemote.transact(33, data, reply, 0);
        reply.readException();
        PendingIntent res = PendingIntent.readPendingIntentOrNullFromParcel(reply);
        data.recycle();
        reply.recycle();
        return res;
    }

    @Override // android.app.IActivityManager
    public ComponentName startService(IApplicationThread caller, Intent service, String resolvedType, int userId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(caller != null ? caller.asBinder() : null);
        service.writeToParcel(data, 0);
        data.writeString(resolvedType);
        data.writeInt(userId);
        this.mRemote.transact(34, data, reply, 0);
        reply.readException();
        ComponentName res = ComponentName.readFromParcel(reply);
        data.recycle();
        reply.recycle();
        return res;
    }

    @Override // android.app.IActivityManager
    public int stopService(IApplicationThread caller, Intent service, String resolvedType, int userId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(caller != null ? caller.asBinder() : null);
        service.writeToParcel(data, 0);
        data.writeString(resolvedType);
        data.writeInt(userId);
        this.mRemote.transact(35, data, reply, 0);
        reply.readException();
        int res = reply.readInt();
        reply.recycle();
        data.recycle();
        return res;
    }

    @Override // android.app.IActivityManager
    public boolean stopServiceToken(ComponentName className, IBinder token, int startId) throws RemoteException {
        boolean res = false;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        ComponentName.writeToParcel(className, data);
        data.writeStrongBinder(token);
        data.writeInt(startId);
        this.mRemote.transact(48, data, reply, 0);
        reply.readException();
        if (reply.readInt() != 0) {
            res = true;
        }
        data.recycle();
        reply.recycle();
        return res;
    }

    @Override // android.app.IActivityManager
    public void setServiceForeground(ComponentName className, IBinder token, int id, Notification notification, boolean removeNotification) throws RemoteException {
        int i = 1;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        ComponentName.writeToParcel(className, data);
        data.writeStrongBinder(token);
        data.writeInt(id);
        if (notification != null) {
            data.writeInt(1);
            notification.writeToParcel(data, 0);
        } else {
            data.writeInt(0);
        }
        if (!removeNotification) {
            i = 0;
        }
        data.writeInt(i);
        this.mRemote.transact(74, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public int bindService(IApplicationThread caller, IBinder token, Intent service, String resolvedType, IServiceConnection connection, int flags, int userId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(caller != null ? caller.asBinder() : null);
        data.writeStrongBinder(token);
        service.writeToParcel(data, 0);
        data.writeString(resolvedType);
        data.writeStrongBinder(connection.asBinder());
        data.writeInt(flags);
        data.writeInt(userId);
        this.mRemote.transact(36, data, reply, 0);
        reply.readException();
        int res = reply.readInt();
        data.recycle();
        reply.recycle();
        return res;
    }

    @Override // android.app.IActivityManager
    public boolean unbindService(IServiceConnection connection) throws RemoteException {
        boolean res = false;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(connection.asBinder());
        this.mRemote.transact(37, data, reply, 0);
        reply.readException();
        if (reply.readInt() != 0) {
            res = true;
        }
        data.recycle();
        reply.recycle();
        return res;
    }

    @Override // android.app.IActivityManager
    public void publishService(IBinder token, Intent intent, IBinder service) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        intent.writeToParcel(data, 0);
        data.writeStrongBinder(service);
        this.mRemote.transact(38, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public void unbindFinished(IBinder token, Intent intent, boolean doRebind) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        intent.writeToParcel(data, 0);
        data.writeInt(doRebind ? 1 : 0);
        this.mRemote.transact(72, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public void serviceDoneExecuting(IBinder token, int type, int startId, int res) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        data.writeInt(type);
        data.writeInt(startId);
        data.writeInt(res);
        this.mRemote.transact(61, data, reply, 1);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public IBinder peekService(Intent service, String resolvedType) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        service.writeToParcel(data, 0);
        data.writeString(resolvedType);
        this.mRemote.transact(85, data, reply, 0);
        reply.readException();
        IBinder binder = reply.readStrongBinder();
        reply.recycle();
        data.recycle();
        return binder;
    }

    @Override // android.app.IActivityManager
    public boolean bindBackupAgent(ApplicationInfo app, int backupRestoreMode) throws RemoteException {
        boolean success = false;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        app.writeToParcel(data, 0);
        data.writeInt(backupRestoreMode);
        this.mRemote.transact(90, data, reply, 0);
        reply.readException();
        if (reply.readInt() != 0) {
            success = true;
        }
        reply.recycle();
        data.recycle();
        return success;
    }

    @Override // android.app.IActivityManager
    public void clearPendingBackup() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(160, data, reply, 0);
        reply.recycle();
        data.recycle();
    }

    @Override // android.app.IActivityManager
    public void backupAgentCreated(String packageName, IBinder agent) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(packageName);
        data.writeStrongBinder(agent);
        this.mRemote.transact(91, data, reply, 0);
        reply.recycle();
        data.recycle();
    }

    @Override // android.app.IActivityManager
    public void unbindBackupAgent(ApplicationInfo app) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        app.writeToParcel(data, 0);
        this.mRemote.transact(92, data, reply, 0);
        reply.readException();
        reply.recycle();
        data.recycle();
    }

    @Override // android.app.IActivityManager
    public boolean startInstrumentation(ComponentName className, String profileFile, int flags, Bundle arguments, IInstrumentationWatcher watcher, IUiAutomationConnection connection, int userId, String instructionSet) throws RemoteException {
        IBinder iBinder;
        IBinder iBinder2 = null;
        boolean res = false;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        ComponentName.writeToParcel(className, data);
        data.writeString(profileFile);
        data.writeInt(flags);
        data.writeBundle(arguments);
        if (watcher != null) {
            iBinder = watcher.asBinder();
        } else {
            iBinder = null;
        }
        data.writeStrongBinder(iBinder);
        if (connection != null) {
            iBinder2 = connection.asBinder();
        }
        data.writeStrongBinder(iBinder2);
        data.writeInt(userId);
        data.writeString(instructionSet);
        this.mRemote.transact(44, data, reply, 0);
        reply.readException();
        if (reply.readInt() != 0) {
            res = true;
        }
        reply.recycle();
        data.recycle();
        return res;
    }

    @Override // android.app.IActivityManager
    public void finishInstrumentation(IApplicationThread target, int resultCode, Bundle results) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(target != null ? target.asBinder() : null);
        data.writeInt(resultCode);
        data.writeBundle(results);
        this.mRemote.transact(45, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public Configuration getConfiguration() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(46, data, reply, 0);
        reply.readException();
        Configuration res = Configuration.CREATOR.createFromParcel(reply);
        reply.recycle();
        data.recycle();
        return res;
    }

    @Override // android.app.IActivityManager
    public void updateConfiguration(Configuration values) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        values.writeToParcel(data, 0);
        this.mRemote.transact(47, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public void setRequestedOrientation(IBinder token, int requestedOrientation) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        data.writeInt(requestedOrientation);
        this.mRemote.transact(70, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public int getRequestedOrientation(IBinder token) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        this.mRemote.transact(71, data, reply, 0);
        reply.readException();
        int res = reply.readInt();
        data.recycle();
        reply.recycle();
        return res;
    }

    @Override // android.app.IActivityManager
    public ComponentName getActivityClassForToken(IBinder token) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        this.mRemote.transact(49, data, reply, 0);
        reply.readException();
        ComponentName res = ComponentName.readFromParcel(reply);
        data.recycle();
        reply.recycle();
        return res;
    }

    @Override // android.app.IActivityManager
    public String getPackageForToken(IBinder token) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        this.mRemote.transact(50, data, reply, 0);
        reply.readException();
        String res = reply.readString();
        data.recycle();
        reply.recycle();
        return res;
    }

    @Override // android.app.IActivityManager
    public IIntentSender getIntentSender(int type, String packageName, IBinder token, String resultWho, int requestCode, Intent[] intents, String[] resolvedTypes, int flags, Bundle options, int userId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(type);
        data.writeString(packageName);
        data.writeStrongBinder(token);
        data.writeString(resultWho);
        data.writeInt(requestCode);
        if (intents != null) {
            data.writeInt(1);
            data.writeTypedArray(intents, 0);
            data.writeStringArray(resolvedTypes);
        } else {
            data.writeInt(0);
        }
        data.writeInt(flags);
        if (options != null) {
            data.writeInt(1);
            options.writeToParcel(data, 0);
        } else {
            data.writeInt(0);
        }
        data.writeInt(userId);
        this.mRemote.transact(63, data, reply, 0);
        reply.readException();
        IIntentSender res = IIntentSender.Stub.asInterface(reply.readStrongBinder());
        data.recycle();
        reply.recycle();
        return res;
    }

    @Override // android.app.IActivityManager
    public void cancelIntentSender(IIntentSender sender) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(sender.asBinder());
        this.mRemote.transact(64, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public String getPackageForIntentSender(IIntentSender sender) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(sender.asBinder());
        this.mRemote.transact(65, data, reply, 0);
        reply.readException();
        String res = reply.readString();
        data.recycle();
        reply.recycle();
        return res;
    }

    @Override // android.app.IActivityManager
    public int getUidForIntentSender(IIntentSender sender) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(sender.asBinder());
        this.mRemote.transact(93, data, reply, 0);
        reply.readException();
        int res = reply.readInt();
        data.recycle();
        reply.recycle();
        return res;
    }

    @Override // android.app.IActivityManager
    public int handleIncomingUser(int callingPid, int callingUid, int userId, boolean allowAll, boolean requireFull, String name, String callerPackage) throws RemoteException {
        int i;
        int i2 = 1;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(callingPid);
        data.writeInt(callingUid);
        data.writeInt(userId);
        if (allowAll) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        if (!requireFull) {
            i2 = 0;
        }
        data.writeInt(i2);
        data.writeString(name);
        data.writeString(callerPackage);
        this.mRemote.transact(94, data, reply, 0);
        reply.readException();
        int res = reply.readInt();
        data.recycle();
        reply.recycle();
        return res;
    }

    @Override // android.app.IActivityManager
    public void setProcessLimit(int max) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(max);
        this.mRemote.transact(51, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public int getProcessLimit() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(52, data, reply, 0);
        reply.readException();
        int res = reply.readInt();
        data.recycle();
        reply.recycle();
        return res;
    }

    @Override // android.app.IActivityManager
    public void setProcessForeground(IBinder token, int pid, boolean isForeground) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        data.writeInt(pid);
        data.writeInt(isForeground ? 1 : 0);
        this.mRemote.transact(73, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public int checkPermission(String permission, int pid, int uid) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(permission);
        data.writeInt(pid);
        data.writeInt(uid);
        this.mRemote.transact(53, data, reply, 0);
        reply.readException();
        int res = reply.readInt();
        data.recycle();
        reply.recycle();
        return res;
    }

    @Override // android.app.IActivityManager
    public boolean clearApplicationUserData(String packageName, IPackageDataObserver observer, int userId) throws RemoteException {
        boolean res = false;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(packageName);
        data.writeStrongBinder(observer != null ? observer.asBinder() : null);
        data.writeInt(userId);
        this.mRemote.transact(78, data, reply, 0);
        reply.readException();
        if (reply.readInt() != 0) {
            res = true;
        }
        data.recycle();
        reply.recycle();
        return res;
    }

    @Override // android.app.IActivityManager
    public int checkUriPermission(Uri uri, int pid, int uid, int mode, int userId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        uri.writeToParcel(data, 0);
        data.writeInt(pid);
        data.writeInt(uid);
        data.writeInt(mode);
        data.writeInt(userId);
        this.mRemote.transact(54, data, reply, 0);
        reply.readException();
        int res = reply.readInt();
        data.recycle();
        reply.recycle();
        return res;
    }

    @Override // android.app.IActivityManager
    public void grantUriPermission(IApplicationThread caller, String targetPkg, Uri uri, int mode, int userId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(caller.asBinder());
        data.writeString(targetPkg);
        uri.writeToParcel(data, 0);
        data.writeInt(mode);
        data.writeInt(userId);
        this.mRemote.transact(55, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public void revokeUriPermission(IApplicationThread caller, Uri uri, int mode, int userId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(caller.asBinder());
        uri.writeToParcel(data, 0);
        data.writeInt(mode);
        data.writeInt(userId);
        this.mRemote.transact(56, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public void takePersistableUriPermission(Uri uri, int mode, int userId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        uri.writeToParcel(data, 0);
        data.writeInt(mode);
        data.writeInt(userId);
        this.mRemote.transact(180, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public void releasePersistableUriPermission(Uri uri, int mode, int userId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        uri.writeToParcel(data, 0);
        data.writeInt(mode);
        data.writeInt(userId);
        this.mRemote.transact(181, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public ParceledListSlice<UriPermission> getPersistedUriPermissions(String packageName, boolean incoming) throws RemoteException {
        int i;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(packageName);
        if (incoming) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        this.mRemote.transact(182, data, reply, 0);
        reply.readException();
        ParceledListSlice<UriPermission> perms = ParceledListSlice.CREATOR.createFromParcel(reply);
        data.recycle();
        reply.recycle();
        return perms;
    }

    @Override // android.app.IActivityManager
    public void showWaitingForDebugger(IApplicationThread who, boolean waiting) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(who.asBinder());
        data.writeInt(waiting ? 1 : 0);
        this.mRemote.transact(58, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public void getMemoryInfo(ActivityManager.MemoryInfo outInfo) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(76, data, reply, 0);
        reply.readException();
        outInfo.readFromParcel(reply);
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public void unhandledBack() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(4, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public ParcelFileDescriptor openContentUri(Uri uri) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(5, data, reply, 0);
        reply.readException();
        ParcelFileDescriptor pfd = null;
        if (reply.readInt() != 0) {
            pfd = ParcelFileDescriptor.CREATOR.createFromParcel(reply);
        }
        data.recycle();
        reply.recycle();
        return pfd;
    }

    @Override // android.app.IActivityManager
    public void setLockScreenShown(boolean shown) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(shown ? 1 : 0);
        this.mRemote.transact(148, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public void setDebugApp(String packageName, boolean waitForDebugger, boolean persistent) throws RemoteException {
        int i = 1;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(packageName);
        data.writeInt(waitForDebugger ? 1 : 0);
        if (!persistent) {
            i = 0;
        }
        data.writeInt(i);
        this.mRemote.transact(42, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public void setAlwaysFinish(boolean enabled) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(enabled ? 1 : 0);
        this.mRemote.transact(43, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public void setActivityController(IActivityController watcher) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(watcher != null ? watcher.asBinder() : null);
        this.mRemote.transact(57, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public void enterSafeMode() throws RemoteException {
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(66, data, null, 0);
        data.recycle();
    }

    @Override // android.app.IActivityManager
    public void noteWakeupAlarm(IIntentSender sender, int sourceUid, String sourcePkg) throws RemoteException {
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(sender.asBinder());
        data.writeInt(sourceUid);
        data.writeString(sourcePkg);
        this.mRemote.transact(68, data, null, 0);
        data.recycle();
    }

    @Override // android.app.IActivityManager
    public boolean killPids(int[] pids, String reason, boolean secure) throws RemoteException {
        int i;
        boolean res;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeIntArray(pids);
        data.writeString(reason);
        if (secure) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        this.mRemote.transact(80, data, reply, 0);
        reply.readException();
        if (reply.readInt() != 0) {
            res = true;
        } else {
            res = false;
        }
        data.recycle();
        reply.recycle();
        return res;
    }

    @Override // android.app.IActivityManager
    public boolean killProcessesBelowForeground(String reason) throws RemoteException {
        boolean res = false;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(reason);
        this.mRemote.transact(144, data, reply, 0);
        if (reply.readInt() != 0) {
            res = true;
        }
        data.recycle();
        reply.recycle();
        return res;
    }

    @Override // android.app.IActivityManager
    public boolean testIsSystemReady() {
        return true;
    }

    @Override // android.app.IActivityManager
    public void handleApplicationCrash(IBinder app, ApplicationErrorReport.CrashInfo crashInfo) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(app);
        crashInfo.writeToParcel(data, 0);
        this.mRemote.transact(2, data, reply, 0);
        reply.readException();
        reply.recycle();
        data.recycle();
    }

    @Override // android.app.IActivityManager
    public boolean handleApplicationWtf(IBinder app, String tag, boolean system, ApplicationErrorReport.CrashInfo crashInfo) throws RemoteException {
        int i;
        boolean res;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(app);
        data.writeString(tag);
        if (system) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        crashInfo.writeToParcel(data, 0);
        this.mRemote.transact(102, data, reply, 0);
        reply.readException();
        if (reply.readInt() != 0) {
            res = true;
        } else {
            res = false;
        }
        reply.recycle();
        data.recycle();
        return res;
    }

    @Override // android.app.IActivityManager
    public void handleApplicationStrictModeViolation(IBinder app, int violationMask, StrictMode.ViolationInfo info) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(app);
        data.writeInt(violationMask);
        info.writeToParcel(data, 0);
        this.mRemote.transact(110, data, reply, 0);
        reply.readException();
        reply.recycle();
        data.recycle();
    }

    @Override // android.app.IActivityManager
    public void signalPersistentProcesses(int sig) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(sig);
        this.mRemote.transact(59, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public void killBackgroundProcesses(String packageName, int userId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(packageName);
        data.writeInt(userId);
        this.mRemote.transact(103, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public void killAllBackgroundProcesses() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(140, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public void forceStopPackage(String packageName, int userId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(packageName);
        data.writeInt(userId);
        this.mRemote.transact(79, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public void getMyMemoryState(ActivityManager.RunningAppProcessInfo outInfo) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(143, data, reply, 0);
        reply.readException();
        outInfo.readFromParcel(reply);
        reply.recycle();
        data.recycle();
    }

    @Override // android.app.IActivityManager
    public ConfigurationInfo getDeviceConfigurationInfo() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(84, data, reply, 0);
        reply.readException();
        ConfigurationInfo res = ConfigurationInfo.CREATOR.createFromParcel(reply);
        reply.recycle();
        data.recycle();
        return res;
    }

    @Override // android.app.IActivityManager
    public boolean profileControl(String process, int userId, boolean start, ProfilerInfo profilerInfo, int profileType) throws RemoteException {
        int i;
        boolean res;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(process);
        data.writeInt(userId);
        if (start) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        data.writeInt(profileType);
        if (profilerInfo != null) {
            data.writeInt(1);
            profilerInfo.writeToParcel(data, 1);
        } else {
            data.writeInt(0);
        }
        this.mRemote.transact(86, data, reply, 0);
        reply.readException();
        if (reply.readInt() != 0) {
            res = true;
        } else {
            res = false;
        }
        reply.recycle();
        data.recycle();
        return res;
    }

    @Override // android.app.IActivityManager
    public boolean shutdown(int timeout) throws RemoteException {
        boolean res = false;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(timeout);
        this.mRemote.transact(87, data, reply, 0);
        reply.readException();
        if (reply.readInt() != 0) {
            res = true;
        }
        reply.recycle();
        data.recycle();
        return res;
    }

    @Override // android.app.IActivityManager
    public void stopAppSwitches() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(88, data, reply, 0);
        reply.readException();
        reply.recycle();
        data.recycle();
    }

    @Override // android.app.IActivityManager
    public void resumeAppSwitches() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(89, data, reply, 0);
        reply.readException();
        reply.recycle();
        data.recycle();
    }

    @Override // android.app.IActivityManager
    public void addPackageDependency(String packageName) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(packageName);
        this.mRemote.transact(95, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public void killApplicationWithAppId(String pkg, int appid, String reason) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(pkg);
        data.writeInt(appid);
        data.writeString(reason);
        this.mRemote.transact(96, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public void closeSystemDialogs(String reason) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(reason);
        this.mRemote.transact(97, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public Debug.MemoryInfo[] getProcessMemoryInfo(int[] pids) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeIntArray(pids);
        this.mRemote.transact(98, data, reply, 0);
        reply.readException();
        Debug.MemoryInfo[] res = (Debug.MemoryInfo[]) reply.createTypedArray(Debug.MemoryInfo.CREATOR);
        data.recycle();
        reply.recycle();
        return res;
    }

    @Override // android.app.IActivityManager
    public void killApplicationProcess(String processName, int uid) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(processName);
        data.writeInt(uid);
        this.mRemote.transact(99, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public void overridePendingTransition(IBinder token, String packageName, int enterAnim, int exitAnim) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        data.writeString(packageName);
        data.writeInt(enterAnim);
        data.writeInt(exitAnim);
        this.mRemote.transact(101, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public boolean isUserAMonkey() throws RemoteException {
        boolean res = false;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(104, data, reply, 0);
        reply.readException();
        if (reply.readInt() != 0) {
            res = true;
        }
        data.recycle();
        reply.recycle();
        return res;
    }

    @Override // android.app.IActivityManager
    public void setUserIsMonkey(boolean monkey) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(monkey ? 1 : 0);
        this.mRemote.transact(166, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public void finishHeavyWeightApp() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(109, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public boolean convertFromTranslucent(IBinder token) throws RemoteException {
        boolean res = false;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        this.mRemote.transact(174, data, reply, 0);
        reply.readException();
        if (reply.readInt() != 0) {
            res = true;
        }
        data.recycle();
        reply.recycle();
        return res;
    }

    @Override // android.app.IActivityManager
    public boolean convertToTranslucent(IBinder token, ActivityOptions options) throws RemoteException {
        boolean res = true;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        if (options == null) {
            data.writeInt(0);
        } else {
            data.writeInt(1);
            data.writeBundle(options.toBundle());
        }
        this.mRemote.transact(175, data, reply, 0);
        reply.readException();
        if (reply.readInt() == 0) {
            res = false;
        }
        data.recycle();
        reply.recycle();
        return res;
    }

    @Override // android.app.IActivityManager
    public ActivityOptions getActivityOptions(IBinder token) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        this.mRemote.transact(220, data, reply, 0);
        reply.readException();
        Bundle bundle = reply.readBundle();
        ActivityOptions options = bundle == null ? null : new ActivityOptions(bundle);
        data.recycle();
        reply.recycle();
        return options;
    }

    @Override // android.app.IActivityManager
    public void setImmersive(IBinder token, boolean immersive) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        data.writeInt(immersive ? 1 : 0);
        this.mRemote.transact(112, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public boolean isImmersive(IBinder token) throws RemoteException {
        boolean res = true;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        this.mRemote.transact(111, data, reply, 0);
        reply.readException();
        if (reply.readInt() != 1) {
            res = false;
        }
        data.recycle();
        reply.recycle();
        return res;
    }

    @Override // android.app.IActivityManager
    public boolean isTopOfTask(IBinder token) throws RemoteException {
        boolean res = true;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        this.mRemote.transact(225, data, reply, 0);
        reply.readException();
        if (reply.readInt() != 1) {
            res = false;
        }
        data.recycle();
        reply.recycle();
        return res;
    }

    @Override // android.app.IActivityManager
    public boolean isTopActivityImmersive() throws RemoteException {
        boolean res = true;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(113, data, reply, 0);
        reply.readException();
        if (reply.readInt() != 1) {
            res = false;
        }
        data.recycle();
        reply.recycle();
        return res;
    }

    @Override // android.app.IActivityManager
    public void crashApplication(int uid, int initialPid, String packageName, String message) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(uid);
        data.writeInt(initialPid);
        data.writeString(packageName);
        data.writeString(message);
        this.mRemote.transact(114, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public String getProviderMimeType(Uri uri, int userId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        uri.writeToParcel(data, 0);
        data.writeInt(userId);
        this.mRemote.transact(115, data, reply, 0);
        reply.readException();
        String res = reply.readString();
        data.recycle();
        reply.recycle();
        return res;
    }

    @Override // android.app.IActivityManager
    public IBinder newUriPermissionOwner(String name) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(name);
        this.mRemote.transact(116, data, reply, 0);
        reply.readException();
        IBinder res = reply.readStrongBinder();
        data.recycle();
        reply.recycle();
        return res;
    }

    @Override // android.app.IActivityManager
    public void grantUriPermissionFromOwner(IBinder owner, int fromUid, String targetPkg, Uri uri, int mode, int sourceUserId, int targetUserId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(owner);
        data.writeInt(fromUid);
        data.writeString(targetPkg);
        uri.writeToParcel(data, 0);
        data.writeInt(mode);
        data.writeInt(sourceUserId);
        data.writeInt(targetUserId);
        this.mRemote.transact(55, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public void revokeUriPermissionFromOwner(IBinder owner, Uri uri, int mode, int userId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(owner);
        if (uri != null) {
            data.writeInt(1);
            uri.writeToParcel(data, 0);
        } else {
            data.writeInt(0);
        }
        data.writeInt(mode);
        data.writeInt(userId);
        this.mRemote.transact(56, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public int checkGrantUriPermission(int callingUid, String targetPkg, Uri uri, int modeFlags, int userId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(callingUid);
        data.writeString(targetPkg);
        uri.writeToParcel(data, 0);
        data.writeInt(modeFlags);
        data.writeInt(userId);
        this.mRemote.transact(119, data, reply, 0);
        reply.readException();
        int res = reply.readInt();
        data.recycle();
        reply.recycle();
        return res;
    }

    @Override // android.app.IActivityManager
    public boolean dumpHeap(String process, int userId, boolean managed, String path, ParcelFileDescriptor fd) throws RemoteException {
        int i;
        boolean res;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(process);
        data.writeInt(userId);
        if (managed) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        data.writeString(path);
        if (fd != null) {
            data.writeInt(1);
            fd.writeToParcel(data, 1);
        } else {
            data.writeInt(0);
        }
        this.mRemote.transact(120, data, reply, 0);
        reply.readException();
        if (reply.readInt() != 0) {
            res = true;
        } else {
            res = false;
        }
        reply.recycle();
        data.recycle();
        return res;
    }

    @Override // android.app.IActivityManager
    public int startActivities(IApplicationThread caller, String callingPackage, Intent[] intents, String[] resolvedTypes, IBinder resultTo, Bundle options, int userId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(caller != null ? caller.asBinder() : null);
        data.writeString(callingPackage);
        data.writeTypedArray(intents, 0);
        data.writeStringArray(resolvedTypes);
        data.writeStrongBinder(resultTo);
        if (options != null) {
            data.writeInt(1);
            options.writeToParcel(data, 0);
        } else {
            data.writeInt(0);
        }
        data.writeInt(userId);
        this.mRemote.transact(121, data, reply, 0);
        reply.readException();
        int result = reply.readInt();
        reply.recycle();
        data.recycle();
        return result;
    }

    @Override // android.app.IActivityManager
    public int getFrontActivityScreenCompatMode() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(124, data, reply, 0);
        reply.readException();
        int mode = reply.readInt();
        reply.recycle();
        data.recycle();
        return mode;
    }

    @Override // android.app.IActivityManager
    public void setFrontActivityScreenCompatMode(int mode) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(mode);
        this.mRemote.transact(125, data, reply, 0);
        reply.readException();
        reply.recycle();
        data.recycle();
    }

    @Override // android.app.IActivityManager
    public int getPackageScreenCompatMode(String packageName) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(packageName);
        this.mRemote.transact(126, data, reply, 0);
        reply.readException();
        int mode = reply.readInt();
        reply.recycle();
        data.recycle();
        return mode;
    }

    @Override // android.app.IActivityManager
    public void setPackageScreenCompatMode(String packageName, int mode) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(packageName);
        data.writeInt(mode);
        this.mRemote.transact(127, data, reply, 0);
        reply.readException();
        reply.recycle();
        data.recycle();
    }

    @Override // android.app.IActivityManager
    public boolean getPackageAskScreenCompat(String packageName) throws RemoteException {
        boolean ask = false;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(packageName);
        this.mRemote.transact(128, data, reply, 0);
        reply.readException();
        if (reply.readInt() != 0) {
            ask = true;
        }
        reply.recycle();
        data.recycle();
        return ask;
    }

    @Override // android.app.IActivityManager
    public void setPackageAskScreenCompat(String packageName, boolean ask) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(packageName);
        data.writeInt(ask ? 1 : 0);
        this.mRemote.transact(129, data, reply, 0);
        reply.readException();
        reply.recycle();
        data.recycle();
    }

    @Override // android.app.IActivityManager
    public boolean switchUser(int userid) throws RemoteException {
        boolean result = false;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(userid);
        this.mRemote.transact(130, data, reply, 0);
        reply.readException();
        if (reply.readInt() != 0) {
            result = true;
        }
        reply.recycle();
        data.recycle();
        return result;
    }

    @Override // android.app.IActivityManager
    public boolean startUserInBackground(int userid) throws RemoteException {
        boolean result = false;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(userid);
        this.mRemote.transact(212, data, reply, 0);
        reply.readException();
        if (reply.readInt() != 0) {
            result = true;
        }
        reply.recycle();
        data.recycle();
        return result;
    }

    @Override // android.app.IActivityManager
    public int stopUser(int userid, IStopUserCallback callback) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(userid);
        data.writeStrongInterface(callback);
        this.mRemote.transact(154, data, reply, 0);
        reply.readException();
        int result = reply.readInt();
        reply.recycle();
        data.recycle();
        return result;
    }

    @Override // android.app.IActivityManager
    public UserInfo getCurrentUser() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(145, data, reply, 0);
        reply.readException();
        UserInfo userInfo = UserInfo.CREATOR.createFromParcel(reply);
        reply.recycle();
        data.recycle();
        return userInfo;
    }

    @Override // android.app.IActivityManager
    public boolean isUserRunning(int userid, boolean orStopping) throws RemoteException {
        int i;
        boolean result;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(userid);
        if (orStopping) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        this.mRemote.transact(122, data, reply, 0);
        reply.readException();
        if (reply.readInt() != 0) {
            result = true;
        } else {
            result = false;
        }
        reply.recycle();
        data.recycle();
        return result;
    }

    @Override // android.app.IActivityManager
    public int[] getRunningUserIds() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(157, data, reply, 0);
        reply.readException();
        int[] result = reply.createIntArray();
        reply.recycle();
        data.recycle();
        return result;
    }

    @Override // android.app.IActivityManager
    public boolean removeTask(int taskId, int flags) throws RemoteException {
        boolean result = false;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(taskId);
        data.writeInt(flags);
        this.mRemote.transact(132, data, reply, 0);
        reply.readException();
        if (reply.readInt() != 0) {
            result = true;
        }
        reply.recycle();
        data.recycle();
        return result;
    }

    @Override // android.app.IActivityManager
    public void registerProcessObserver(IProcessObserver observer) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(observer != null ? observer.asBinder() : null);
        this.mRemote.transact(133, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public void unregisterProcessObserver(IProcessObserver observer) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(observer != null ? observer.asBinder() : null);
        this.mRemote.transact(134, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public boolean isIntentSenderTargetedToPackage(IIntentSender sender) throws RemoteException {
        boolean res = false;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(sender.asBinder());
        this.mRemote.transact(135, data, reply, 0);
        reply.readException();
        if (reply.readInt() != 0) {
            res = true;
        }
        data.recycle();
        reply.recycle();
        return res;
    }

    @Override // android.app.IActivityManager
    public boolean isIntentSenderAnActivity(IIntentSender sender) throws RemoteException {
        boolean res = false;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(sender.asBinder());
        this.mRemote.transact(152, data, reply, 0);
        reply.readException();
        if (reply.readInt() != 0) {
            res = true;
        }
        data.recycle();
        reply.recycle();
        return res;
    }

    @Override // android.app.IActivityManager
    public Intent getIntentForIntentSender(IIntentSender sender) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(sender.asBinder());
        this.mRemote.transact(161, data, reply, 0);
        reply.readException();
        Intent res = reply.readInt() != 0 ? Intent.CREATOR.createFromParcel(reply) : null;
        data.recycle();
        reply.recycle();
        return res;
    }

    @Override // android.app.IActivityManager
    public String getTagForIntentSender(IIntentSender sender, String prefix) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(sender.asBinder());
        data.writeString(prefix);
        this.mRemote.transact(211, data, reply, 0);
        reply.readException();
        String res = reply.readString();
        data.recycle();
        reply.recycle();
        return res;
    }

    @Override // android.app.IActivityManager
    public void updatePersistentConfiguration(Configuration values) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        values.writeToParcel(data, 0);
        this.mRemote.transact(136, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public long[] getProcessPss(int[] pids) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeIntArray(pids);
        this.mRemote.transact(137, data, reply, 0);
        reply.readException();
        long[] res = reply.createLongArray();
        data.recycle();
        reply.recycle();
        return res;
    }

    @Override // android.app.IActivityManager
    public void showBootMessage(CharSequence msg, boolean always) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        TextUtils.writeToParcel(msg, data, 0);
        data.writeInt(always ? 1 : 0);
        this.mRemote.transact(138, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public void keyguardWaitingForActivityDrawn() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(232, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public boolean shouldUpRecreateTask(IBinder token, String destAffinity) throws RemoteException {
        boolean result = false;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        data.writeString(destAffinity);
        this.mRemote.transact(146, data, reply, 0);
        reply.readException();
        if (reply.readInt() != 0) {
            result = true;
        }
        data.recycle();
        reply.recycle();
        return result;
    }

    @Override // android.app.IActivityManager
    public boolean navigateUpTo(IBinder token, Intent target, int resultCode, Intent resultData) throws RemoteException {
        boolean result = true;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        target.writeToParcel(data, 0);
        data.writeInt(resultCode);
        if (resultData != null) {
            data.writeInt(1);
            resultData.writeToParcel(data, 0);
        } else {
            data.writeInt(0);
        }
        this.mRemote.transact(147, data, reply, 0);
        reply.readException();
        if (reply.readInt() == 0) {
            result = false;
        }
        data.recycle();
        reply.recycle();
        return result;
    }

    @Override // android.app.IActivityManager
    public int getLaunchedFromUid(IBinder activityToken) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(activityToken);
        this.mRemote.transact(150, data, reply, 0);
        reply.readException();
        int result = reply.readInt();
        data.recycle();
        reply.recycle();
        return result;
    }

    @Override // android.app.IActivityManager
    public String getLaunchedFromPackage(IBinder activityToken) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(activityToken);
        this.mRemote.transact(164, data, reply, 0);
        reply.readException();
        String result = reply.readString();
        data.recycle();
        reply.recycle();
        return result;
    }

    @Override // android.app.IActivityManager
    public void registerUserSwitchObserver(IUserSwitchObserver observer) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(observer != null ? observer.asBinder() : null);
        this.mRemote.transact(155, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public void unregisterUserSwitchObserver(IUserSwitchObserver observer) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(observer != null ? observer.asBinder() : null);
        this.mRemote.transact(156, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public void requestBugReport() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(158, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public long inputDispatchingTimedOut(int pid, boolean aboveSystem, String reason) throws RemoteException {
        int i;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(pid);
        if (aboveSystem) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        data.writeString(reason);
        this.mRemote.transact(159, data, reply, 0);
        reply.readException();
        long res = (long) reply.readInt();
        data.recycle();
        reply.recycle();
        return res;
    }

    @Override // android.app.IActivityManager
    public Bundle getAssistContextExtras(int requestType) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(requestType);
        this.mRemote.transact(162, data, reply, 0);
        reply.readException();
        Bundle res = reply.readBundle();
        data.recycle();
        reply.recycle();
        return res;
    }

    @Override // android.app.IActivityManager
    public void reportAssistContextExtras(IBinder token, Bundle extras) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        data.writeBundle(extras);
        this.mRemote.transact(163, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public boolean launchAssistIntent(Intent intent, int requestType, String hint, int userHandle) throws RemoteException {
        boolean res = false;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        intent.writeToParcel(data, 0);
        data.writeInt(requestType);
        data.writeString(hint);
        data.writeInt(userHandle);
        this.mRemote.transact(240, data, reply, 0);
        reply.readException();
        if (reply.readInt() != 0) {
            res = true;
        }
        data.recycle();
        reply.recycle();
        return res;
    }

    @Override // android.app.IActivityManager
    public void killUid(int uid, String reason) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(uid);
        data.writeString(reason);
        this.mRemote.transact(165, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public void hang(IBinder who, boolean allowRestart) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(who);
        data.writeInt(allowRestart ? 1 : 0);
        this.mRemote.transact(167, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public void reportActivityFullyDrawn(IBinder token) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        this.mRemote.transact(177, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public void notifyActivityDrawn(IBinder token) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        this.mRemote.transact(176, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public void restart() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(178, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public void performIdleMaintenance() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(179, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public IActivityContainer createActivityContainer(IBinder parentActivityToken, IActivityContainerCallback callback) throws RemoteException {
        IActivityContainer res;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(parentActivityToken);
        data.writeStrongBinder(callback == null ? null : callback.asBinder());
        this.mRemote.transact(168, data, reply, 0);
        reply.readException();
        if (reply.readInt() == 1) {
            res = IActivityContainer.Stub.asInterface(reply.readStrongBinder());
        } else {
            res = null;
        }
        data.recycle();
        reply.recycle();
        return res;
    }

    @Override // android.app.IActivityManager
    public void deleteActivityContainer(IActivityContainer activityContainer) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(activityContainer.asBinder());
        this.mRemote.transact(186, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public IActivityContainer getEnclosingActivityContainer(IBinder activityToken) throws RemoteException {
        IActivityContainer res;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(activityToken);
        this.mRemote.transact(185, data, reply, 0);
        reply.readException();
        if (reply.readInt() == 1) {
            res = IActivityContainer.Stub.asInterface(reply.readStrongBinder());
        } else {
            res = null;
        }
        data.recycle();
        reply.recycle();
        return res;
    }

    @Override // android.app.IActivityManager
    public IBinder getHomeActivityToken() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(184, data, reply, 0);
        reply.readException();
        IBinder res = reply.readStrongBinder();
        data.recycle();
        reply.recycle();
        return res;
    }

    @Override // android.app.IActivityManager
    public void startLockTaskMode(int taskId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeInt(taskId);
        this.mRemote.transact(214, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public void startLockTaskMode(IBinder token) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        this.mRemote.transact(215, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public void startLockTaskModeOnCurrent() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(222, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public void stopLockTaskMode() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(216, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public void stopLockTaskModeOnCurrent() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(223, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public boolean isInLockTaskMode() throws RemoteException {
        boolean isInLockTaskMode = true;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(217, data, reply, 0);
        reply.readException();
        if (reply.readInt() != 1) {
            isInLockTaskMode = false;
        }
        data.recycle();
        reply.recycle();
        return isInLockTaskMode;
    }

    @Override // android.app.IActivityManager
    public void setTaskDescription(IBinder token, ActivityManager.TaskDescription values) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        values.writeToParcel(data, 0);
        this.mRemote.transact(218, data, reply, 1);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public Bitmap getTaskDescriptionIcon(String filename) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeString(filename);
        this.mRemote.transact(239, data, reply, 0);
        reply.readException();
        Bitmap icon = reply.readInt() == 0 ? null : Bitmap.CREATOR.createFromParcel(reply);
        data.recycle();
        reply.recycle();
        return icon;
    }

    @Override // android.app.IActivityManager
    public boolean requestVisibleBehind(IBinder token, boolean visible) throws RemoteException {
        int i;
        boolean success;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        if (visible) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        this.mRemote.transact(226, data, reply, 0);
        reply.readException();
        if (reply.readInt() > 0) {
            success = true;
        } else {
            success = false;
        }
        data.recycle();
        reply.recycle();
        return success;
    }

    @Override // android.app.IActivityManager
    public boolean isBackgroundVisibleBehind(IBinder token) throws RemoteException {
        boolean visible = false;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        this.mRemote.transact(227, data, reply, 0);
        reply.readException();
        if (reply.readInt() > 0) {
            visible = true;
        }
        data.recycle();
        reply.recycle();
        return visible;
    }

    @Override // android.app.IActivityManager
    public void backgroundResourcesReleased(IBinder token) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        this.mRemote.transact(228, data, reply, 1);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public void notifyLaunchTaskBehindComplete(IBinder token) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        this.mRemote.transact(229, data, reply, 1);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public void notifyEnterAnimationComplete(IBinder token) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        data.writeStrongBinder(token);
        this.mRemote.transact(231, data, reply, 1);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    @Override // android.app.IActivityManager
    public void bootAnimationComplete() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IActivityManager.descriptor);
        this.mRemote.transact(238, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }
}

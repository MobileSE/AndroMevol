package android.app;

import android.app.IInstrumentationWatcher;
import android.app.IUiAutomationConnection;
import android.content.ComponentName;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Binder;
import android.os.Debug;
import android.os.IBinder;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import com.android.internal.app.IVoiceInteractor;
import java.io.IOException;
import java.util.List;

public abstract class ApplicationThreadNative extends Binder implements IApplicationThread {
    public static IApplicationThread asInterface(IBinder obj) {
        if (obj == null) {
            return null;
        }
        IApplicationThread in = (IApplicationThread) obj.queryLocalInterface(IApplicationThread.descriptor);
        return in == null ? new ApplicationThreadProxy(obj) : in;
    }

    public ApplicationThreadNative() {
        attachInterface(this, IApplicationThread.descriptor);
    }

    @Override // android.os.Binder
    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        Intent args;
        switch (code) {
            case 1:
                data.enforceInterface(IApplicationThread.descriptor);
                schedulePauseActivity(data.readStrongBinder(), data.readInt() != 0, data.readInt() != 0, data.readInt(), data.readInt() != 0);
                return true;
            case 2:
            case 15:
            default:
                return super.onTransact(code, data, reply, flags);
            case 3:
                data.enforceInterface(IApplicationThread.descriptor);
                scheduleStopActivity(data.readStrongBinder(), data.readInt() != 0, data.readInt());
                return true;
            case 4:
                data.enforceInterface(IApplicationThread.descriptor);
                scheduleWindowVisibility(data.readStrongBinder(), data.readInt() != 0);
                return true;
            case 5:
                data.enforceInterface(IApplicationThread.descriptor);
                scheduleResumeActivity(data.readStrongBinder(), data.readInt(), data.readInt() != 0, data.readBundle());
                return true;
            case 6:
                data.enforceInterface(IApplicationThread.descriptor);
                scheduleSendResult(data.readStrongBinder(), data.createTypedArrayList(ResultInfo.CREATOR));
                return true;
            case 7:
                data.enforceInterface(IApplicationThread.descriptor);
                scheduleLaunchActivity(Intent.CREATOR.createFromParcel(data), data.readStrongBinder(), data.readInt(), ActivityInfo.CREATOR.createFromParcel(data), Configuration.CREATOR.createFromParcel(data), CompatibilityInfo.CREATOR.createFromParcel(data), IVoiceInteractor.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readBundle(), data.readPersistableBundle(), data.createTypedArrayList(ResultInfo.CREATOR), data.createTypedArrayList(Intent.CREATOR), data.readInt() != 0, data.readInt() != 0, data.readInt() != 0 ? ProfilerInfo.CREATOR.createFromParcel(data) : null);
                return true;
            case 8:
                data.enforceInterface(IApplicationThread.descriptor);
                scheduleNewIntent(data.createTypedArrayList(Intent.CREATOR), data.readStrongBinder());
                return true;
            case 9:
                data.enforceInterface(IApplicationThread.descriptor);
                scheduleDestroyActivity(data.readStrongBinder(), data.readInt() != 0, data.readInt());
                return true;
            case 10:
                data.enforceInterface(IApplicationThread.descriptor);
                scheduleReceiver(Intent.CREATOR.createFromParcel(data), ActivityInfo.CREATOR.createFromParcel(data), CompatibilityInfo.CREATOR.createFromParcel(data), data.readInt(), data.readString(), data.readBundle(), data.readInt() != 0, data.readInt(), data.readInt());
                return true;
            case 11:
                data.enforceInterface(IApplicationThread.descriptor);
                scheduleCreateService(data.readStrongBinder(), ServiceInfo.CREATOR.createFromParcel(data), CompatibilityInfo.CREATOR.createFromParcel(data), data.readInt());
                return true;
            case 12:
                data.enforceInterface(IApplicationThread.descriptor);
                scheduleStopService(data.readStrongBinder());
                return true;
            case 13:
                data.enforceInterface(IApplicationThread.descriptor);
                bindApplication(data.readString(), ApplicationInfo.CREATOR.createFromParcel(data), data.createTypedArrayList(ProviderInfo.CREATOR), data.readInt() != 0 ? new ComponentName(data) : null, data.readInt() != 0 ? ProfilerInfo.CREATOR.createFromParcel(data) : null, data.readBundle(), IInstrumentationWatcher.Stub.asInterface(data.readStrongBinder()), IUiAutomationConnection.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readInt() != 0, data.readInt() != 0, data.readInt() != 0, Configuration.CREATOR.createFromParcel(data), CompatibilityInfo.CREATOR.createFromParcel(data), data.readHashMap(null), data.readBundle());
                return true;
            case 14:
                data.enforceInterface(IApplicationThread.descriptor);
                scheduleExit();
                return true;
            case 16:
                data.enforceInterface(IApplicationThread.descriptor);
                scheduleConfigurationChanged(Configuration.CREATOR.createFromParcel(data));
                return true;
            case 17:
                data.enforceInterface(IApplicationThread.descriptor);
                IBinder token = data.readStrongBinder();
                boolean taskRemoved = data.readInt() != 0;
                int startId = data.readInt();
                int fl = data.readInt();
                if (data.readInt() != 0) {
                    args = Intent.CREATOR.createFromParcel(data);
                } else {
                    args = null;
                }
                scheduleServiceArgs(token, taskRemoved, startId, fl, args);
                return true;
            case 18:
                data.enforceInterface(IApplicationThread.descriptor);
                updateTimeZone();
                return true;
            case 19:
                data.enforceInterface(IApplicationThread.descriptor);
                processInBackground();
                return true;
            case 20:
                data.enforceInterface(IApplicationThread.descriptor);
                scheduleBindService(data.readStrongBinder(), Intent.CREATOR.createFromParcel(data), data.readInt() != 0, data.readInt());
                return true;
            case 21:
                data.enforceInterface(IApplicationThread.descriptor);
                scheduleUnbindService(data.readStrongBinder(), Intent.CREATOR.createFromParcel(data));
                return true;
            case 22:
                data.enforceInterface(IApplicationThread.descriptor);
                ParcelFileDescriptor fd = data.readFileDescriptor();
                IBinder service = data.readStrongBinder();
                String[] args2 = data.readStringArray();
                if (fd != null) {
                    dumpService(fd.getFileDescriptor(), service, args2);
                    try {
                        fd.close();
                    } catch (IOException e) {
                    }
                }
                return true;
            case 23:
                data.enforceInterface(IApplicationThread.descriptor);
                scheduleRegisteredReceiver(IIntentReceiver.Stub.asInterface(data.readStrongBinder()), Intent.CREATOR.createFromParcel(data), data.readInt(), data.readString(), data.readBundle(), data.readInt() != 0, data.readInt() != 0, data.readInt(), data.readInt());
                return true;
            case 24:
                data.enforceInterface(IApplicationThread.descriptor);
                scheduleLowMemory();
                return true;
            case 25:
                data.enforceInterface(IApplicationThread.descriptor);
                scheduleActivityConfigurationChanged(data.readStrongBinder());
                return true;
            case 26:
                data.enforceInterface(IApplicationThread.descriptor);
                IBinder b = data.readStrongBinder();
                List<ResultInfo> ri = data.createTypedArrayList(ResultInfo.CREATOR);
                List<Intent> pi = data.createTypedArrayList(Intent.CREATOR);
                int configChanges = data.readInt();
                boolean notResumed = data.readInt() != 0;
                Configuration config = null;
                if (data.readInt() != 0) {
                    config = Configuration.CREATOR.createFromParcel(data);
                }
                scheduleRelaunchActivity(b, ri, pi, configChanges, notResumed, config);
                return true;
            case 27:
                data.enforceInterface(IApplicationThread.descriptor);
                scheduleSleeping(data.readStrongBinder(), data.readInt() != 0);
                return true;
            case 28:
                data.enforceInterface(IApplicationThread.descriptor);
                profilerControl(data.readInt() != 0, data.readInt() != 0 ? ProfilerInfo.CREATOR.createFromParcel(data) : null, data.readInt());
                return true;
            case 29:
                data.enforceInterface(IApplicationThread.descriptor);
                setSchedulingGroup(data.readInt());
                return true;
            case 30:
                data.enforceInterface(IApplicationThread.descriptor);
                scheduleCreateBackupAgent(ApplicationInfo.CREATOR.createFromParcel(data), CompatibilityInfo.CREATOR.createFromParcel(data), data.readInt());
                return true;
            case 31:
                data.enforceInterface(IApplicationThread.descriptor);
                scheduleDestroyBackupAgent(ApplicationInfo.CREATOR.createFromParcel(data), CompatibilityInfo.CREATOR.createFromParcel(data));
                return true;
            case 32:
                data.enforceInterface(IApplicationThread.descriptor);
                scheduleOnNewActivityOptions(data.readStrongBinder(), new ActivityOptions(data.readBundle()));
                reply.writeNoException();
                return true;
            case 33:
                data.enforceInterface(IApplicationThread.descriptor);
                scheduleSuicide();
                return true;
            case 34:
                data.enforceInterface(IApplicationThread.descriptor);
                dispatchPackageBroadcast(data.readInt(), data.readStringArray());
                return true;
            case 35:
                data.enforceInterface(IApplicationThread.descriptor);
                scheduleCrash(data.readString());
                return true;
            case 36:
                data.enforceInterface(IApplicationThread.descriptor);
                dumpHeap(data.readInt() != 0, data.readString(), data.readInt() != 0 ? ParcelFileDescriptor.CREATOR.createFromParcel(data) : null);
                return true;
            case 37:
                data.enforceInterface(IApplicationThread.descriptor);
                ParcelFileDescriptor fd2 = data.readFileDescriptor();
                IBinder activity = data.readStrongBinder();
                String prefix = data.readString();
                String[] args3 = data.readStringArray();
                if (fd2 != null) {
                    dumpActivity(fd2.getFileDescriptor(), activity, prefix, args3);
                    try {
                        fd2.close();
                    } catch (IOException e2) {
                    }
                }
                return true;
            case 38:
                data.enforceInterface(IApplicationThread.descriptor);
                clearDnsCache();
                return true;
            case 39:
                data.enforceInterface(IApplicationThread.descriptor);
                setHttpProxy(data.readString(), data.readString(), data.readString(), Uri.CREATOR.createFromParcel(data));
                return true;
            case 40:
                data.enforceInterface(IApplicationThread.descriptor);
                setCoreSettings(data.readBundle());
                return true;
            case 41:
                data.enforceInterface(IApplicationThread.descriptor);
                updatePackageCompatibilityInfo(data.readString(), CompatibilityInfo.CREATOR.createFromParcel(data));
                return true;
            case 42:
                data.enforceInterface(IApplicationThread.descriptor);
                scheduleTrimMemory(data.readInt());
                return true;
            case 43:
                data.enforceInterface(IApplicationThread.descriptor);
                ParcelFileDescriptor fd3 = data.readFileDescriptor();
                Debug.MemoryInfo mi = Debug.MemoryInfo.CREATOR.createFromParcel(data);
                boolean checkin = data.readInt() != 0;
                boolean dumpInfo = data.readInt() != 0;
                boolean dumpDalvik = data.readInt() != 0;
                String[] args4 = data.readStringArray();
                if (fd3 != null) {
                    try {
                        dumpMemInfo(fd3.getFileDescriptor(), mi, checkin, dumpInfo, dumpDalvik, args4);
                    } finally {
                        try {
                            fd3.close();
                        } catch (IOException e3) {
                        }
                    }
                }
                reply.writeNoException();
                return true;
            case 44:
                data.enforceInterface(IApplicationThread.descriptor);
                ParcelFileDescriptor fd4 = data.readFileDescriptor();
                String[] args5 = data.readStringArray();
                if (fd4 != null) {
                    try {
                        dumpGfxInfo(fd4.getFileDescriptor(), args5);
                    } finally {
                        try {
                            fd4.close();
                        } catch (IOException e4) {
                        }
                    }
                }
                reply.writeNoException();
                return true;
            case 45:
                data.enforceInterface(IApplicationThread.descriptor);
                ParcelFileDescriptor fd5 = data.readFileDescriptor();
                IBinder service2 = data.readStrongBinder();
                String[] args6 = data.readStringArray();
                if (fd5 != null) {
                    dumpProvider(fd5.getFileDescriptor(), service2, args6);
                    try {
                        fd5.close();
                    } catch (IOException e5) {
                    }
                }
                return true;
            case 46:
                data.enforceInterface(IApplicationThread.descriptor);
                ParcelFileDescriptor fd6 = data.readFileDescriptor();
                String[] args7 = data.readStringArray();
                if (fd6 != null) {
                    try {
                        dumpDbInfo(fd6.getFileDescriptor(), args7);
                    } finally {
                        try {
                            fd6.close();
                        } catch (IOException e6) {
                        }
                    }
                }
                reply.writeNoException();
                return true;
            case 47:
                data.enforceInterface(IApplicationThread.descriptor);
                unstableProviderDied(data.readStrongBinder());
                reply.writeNoException();
                return true;
            case 48:
                data.enforceInterface(IApplicationThread.descriptor);
                requestAssistContextExtras(data.readStrongBinder(), data.readStrongBinder(), data.readInt());
                reply.writeNoException();
                return true;
            case 49:
                data.enforceInterface(IApplicationThread.descriptor);
                scheduleTranslucentConversionComplete(data.readStrongBinder(), data.readInt() == 1);
                reply.writeNoException();
                return true;
            case 50:
                data.enforceInterface(IApplicationThread.descriptor);
                setProcessState(data.readInt());
                reply.writeNoException();
                return true;
            case 51:
                data.enforceInterface(IApplicationThread.descriptor);
                scheduleInstallProvider(ProviderInfo.CREATOR.createFromParcel(data));
                reply.writeNoException();
                return true;
            case 52:
                data.enforceInterface(IApplicationThread.descriptor);
                updateTimePrefs(data.readByte() == 1);
                reply.writeNoException();
                return true;
            case 53:
                data.enforceInterface(IApplicationThread.descriptor);
                scheduleCancelVisibleBehind(data.readStrongBinder());
                reply.writeNoException();
                return true;
            case 54:
                data.enforceInterface(IApplicationThread.descriptor);
                scheduleBackgroundVisibleBehindChanged(data.readStrongBinder(), data.readInt() > 0);
                reply.writeNoException();
                return true;
            case 55:
                data.enforceInterface(IApplicationThread.descriptor);
                scheduleEnterAnimationComplete(data.readStrongBinder());
                reply.writeNoException();
                return true;
        }
    }

    @Override // android.os.IInterface
    public IBinder asBinder() {
        return this;
    }
}

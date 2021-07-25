package android.os;

import android.os.IBinder;
import android.util.Log;
import com.android.internal.util.FastPrintWriter;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class Binder implements IBinder {
    private static final boolean CHECK_PARCEL_SIZE = false;
    private static final boolean FIND_POTENTIAL_LEAKS = false;
    static final String TAG = "Binder";
    private static String sDumpDisabled = null;
    private String mDescriptor;
    private long mObject;
    private IInterface mOwner;

    public static final native long clearCallingIdentity();

    private final native void destroy();

    public static final native void flushPendingCommands();

    public static final native int getCallingPid();

    public static final native int getCallingUid();

    public static final native int getThreadStrictModePolicy();

    private final native void init();

    public static final native void joinThreadPool();

    public static final native void restoreCallingIdentity(long j);

    public static final native void setThreadStrictModePolicy(int i);

    public static final UserHandle getCallingUserHandle() {
        return new UserHandle(UserHandle.getUserId(getCallingUid()));
    }

    public static final boolean isProxy(IInterface iface) {
        return iface.asBinder() != iface;
    }

    public Binder() {
        init();
    }

    public void attachInterface(IInterface owner, String descriptor) {
        this.mOwner = owner;
        this.mDescriptor = descriptor;
    }

    @Override // android.os.IBinder
    public String getInterfaceDescriptor() {
        return this.mDescriptor;
    }

    @Override // android.os.IBinder
    public boolean pingBinder() {
        return true;
    }

    @Override // android.os.IBinder
    public boolean isBinderAlive() {
        return true;
    }

    @Override // android.os.IBinder
    public IInterface queryLocalInterface(String descriptor) {
        if (this.mDescriptor.equals(descriptor)) {
            return this.mOwner;
        }
        return null;
    }

    public static void setDumpDisabled(String msg) {
        synchronized (Binder.class) {
            sDumpDisabled = msg;
        }
    }

    /* access modifiers changed from: protected */
    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (code == 1598968902) {
            reply.writeString(getInterfaceDescriptor());
            return true;
        } else if (code != 1598311760) {
            return false;
        } else {
            ParcelFileDescriptor fd = data.readFileDescriptor();
            String[] args = data.readStringArray();
            if (fd != null) {
                try {
                    dump(fd.getFileDescriptor(), args);
                } finally {
                    try {
                        fd.close();
                    } catch (IOException e) {
                    }
                }
            }
            if (reply != null) {
                reply.writeNoException();
                return true;
            }
            StrictMode.clearGatheredViolations();
            return true;
        }
    }

    @Override // android.os.IBinder
    public void dump(FileDescriptor fd, String[] args) {
        String disabled;
        PrintWriter pw = new FastPrintWriter(new FileOutputStream(fd));
        try {
            synchronized (Binder.class) {
                disabled = sDumpDisabled;
            }
            if (disabled == null) {
                try {
                    dump(fd, pw, args);
                } catch (SecurityException e) {
                    pw.println("Security exception: " + e.getMessage());
                    throw e;
                } catch (Throwable e2) {
                    pw.println();
                    pw.println("Exception occurred while dumping:");
                    e2.printStackTrace(pw);
                }
            } else {
                pw.println(sDumpDisabled);
            }
        } finally {
            pw.flush();
        }
    }

    @Override // android.os.IBinder
    public void dumpAsync(final FileDescriptor fd, final String[] args) {
        final PrintWriter pw = new FastPrintWriter(new FileOutputStream(fd));
        new Thread("Binder.dumpAsync") {
            /* class android.os.Binder.AnonymousClass1 */

            public void run() {
                try {
                    Binder.this.dump(fd, pw, args);
                } finally {
                    pw.flush();
                }
            }
        }.start();
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter fout, String[] args) {
    }

    @Override // android.os.IBinder
    public final boolean transact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (data != null) {
            data.setDataPosition(0);
        }
        boolean r = onTransact(code, data, reply, flags);
        if (reply != null) {
            reply.setDataPosition(0);
        }
        return r;
    }

    @Override // android.os.IBinder
    public void linkToDeath(IBinder.DeathRecipient recipient, int flags) {
    }

    @Override // android.os.IBinder
    public boolean unlinkToDeath(IBinder.DeathRecipient recipient, int flags) {
        return true;
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            destroy();
        } finally {
            super.finalize();
        }
    }

    static void checkParcel(IBinder obj, int code, Parcel parcel, String msg) {
    }

    private boolean execTransact(int code, long dataObj, long replyObj, int flags) {
        boolean res;
        Parcel data = Parcel.obtain(dataObj);
        Parcel reply = Parcel.obtain(replyObj);
        try {
            res = onTransact(code, data, reply, flags);
        } catch (RemoteException e) {
            if ((flags & 1) != 0) {
                Log.w(TAG, "Binder call failed.", e);
            } else {
                reply.setDataPosition(0);
                reply.writeException(e);
            }
            res = true;
        } catch (RuntimeException e2) {
            if ((flags & 1) != 0) {
                Log.w(TAG, "Caught a RuntimeException from the binder stub implementation.", e2);
            } else {
                reply.setDataPosition(0);
                reply.writeException(e2);
            }
            res = true;
        } catch (OutOfMemoryError e3) {
            Log.e(TAG, "Caught an OutOfMemoryError from the binder stub implementation.", e3);
            RuntimeException re = new RuntimeException("Out of memory", e3);
            reply.setDataPosition(0);
            reply.writeException(re);
            res = true;
        }
        checkParcel(this, code, reply, "Unreasonably large binder reply buffer");
        reply.recycle();
        data.recycle();
        StrictMode.clearGatheredViolations();
        return res;
    }
}

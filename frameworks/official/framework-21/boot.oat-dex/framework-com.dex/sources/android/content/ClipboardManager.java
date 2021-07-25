package android.content;

import android.content.IClipboard;
import android.content.IOnPrimaryClipChangedListener;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import java.util.ArrayList;

public class ClipboardManager extends android.text.ClipboardManager {
    static final int MSG_REPORT_PRIMARY_CLIP_CHANGED = 1;
    private static IClipboard sService;
    private static final Object sStaticLock = new Object();
    private final Context mContext;
    private final Handler mHandler = new Handler() {
        /* class android.content.ClipboardManager.AnonymousClass2 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    ClipboardManager.this.reportPrimaryClipChanged();
                    return;
                default:
                    return;
            }
        }
    };
    private final ArrayList<OnPrimaryClipChangedListener> mPrimaryClipChangedListeners = new ArrayList<>();
    private final IOnPrimaryClipChangedListener.Stub mPrimaryClipChangedServiceListener = new IOnPrimaryClipChangedListener.Stub() {
        /* class android.content.ClipboardManager.AnonymousClass1 */

        @Override // android.content.IOnPrimaryClipChangedListener
        public void dispatchPrimaryClipChanged() {
            ClipboardManager.this.mHandler.sendEmptyMessage(1);
        }
    };

    public interface OnPrimaryClipChangedListener {
        void onPrimaryClipChanged();
    }

    private static IClipboard getService() {
        IClipboard iClipboard;
        synchronized (sStaticLock) {
            if (sService != null) {
                iClipboard = sService;
            } else {
                sService = IClipboard.Stub.asInterface(ServiceManager.getService(Context.CLIPBOARD_SERVICE));
                iClipboard = sService;
            }
        }
        return iClipboard;
    }

    public ClipboardManager(Context context, Handler handler) {
        this.mContext = context;
    }

    public void setPrimaryClip(ClipData clip) {
        if (clip != null) {
            try {
                clip.prepareToLeaveProcess();
            } catch (RemoteException e) {
                return;
            }
        }
        getService().setPrimaryClip(clip, this.mContext.getOpPackageName());
    }

    public ClipData getPrimaryClip() {
        try {
            return getService().getPrimaryClip(this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            return null;
        }
    }

    public ClipDescription getPrimaryClipDescription() {
        try {
            return getService().getPrimaryClipDescription(this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            return null;
        }
    }

    public boolean hasPrimaryClip() {
        try {
            return getService().hasPrimaryClip(this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            return false;
        }
    }

    public void addPrimaryClipChangedListener(OnPrimaryClipChangedListener what) {
        synchronized (this.mPrimaryClipChangedListeners) {
            if (this.mPrimaryClipChangedListeners.size() == 0) {
                try {
                    getService().addPrimaryClipChangedListener(this.mPrimaryClipChangedServiceListener, this.mContext.getOpPackageName());
                } catch (RemoteException e) {
                }
            }
            this.mPrimaryClipChangedListeners.add(what);
        }
    }

    public void removePrimaryClipChangedListener(OnPrimaryClipChangedListener what) {
        synchronized (this.mPrimaryClipChangedListeners) {
            this.mPrimaryClipChangedListeners.remove(what);
            if (this.mPrimaryClipChangedListeners.size() == 0) {
                try {
                    getService().removePrimaryClipChangedListener(this.mPrimaryClipChangedServiceListener);
                } catch (RemoteException e) {
                }
            }
        }
    }

    @Override // android.text.ClipboardManager
    public CharSequence getText() {
        ClipData clip = getPrimaryClip();
        if (clip == null || clip.getItemCount() <= 0) {
            return null;
        }
        return clip.getItemAt(0).coerceToText(this.mContext);
    }

    @Override // android.text.ClipboardManager
    public void setText(CharSequence text) {
        setPrimaryClip(ClipData.newPlainText(null, text));
    }

    @Override // android.text.ClipboardManager
    public boolean hasText() {
        try {
            return getService().hasClipboardText(this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0016, code lost:
        if (r1 >= r2.length) goto L_0x000c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0018, code lost:
        ((android.content.ClipboardManager.OnPrimaryClipChangedListener) r2[r1]).onPrimaryClipChanged();
        r1 = r1 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0014, code lost:
        r1 = 0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void reportPrimaryClipChanged() {
        /*
            r5 = this;
            java.util.ArrayList<android.content.ClipboardManager$OnPrimaryClipChangedListener> r4 = r5.mPrimaryClipChangedListeners
            monitor-enter(r4)
            java.util.ArrayList<android.content.ClipboardManager$OnPrimaryClipChangedListener> r3 = r5.mPrimaryClipChangedListeners     // Catch:{ all -> 0x0022 }
            int r0 = r3.size()     // Catch:{ all -> 0x0022 }
            if (r0 > 0) goto L_0x000d
            monitor-exit(r4)     // Catch:{ all -> 0x0022 }
        L_0x000c:
            return
        L_0x000d:
            java.util.ArrayList<android.content.ClipboardManager$OnPrimaryClipChangedListener> r3 = r5.mPrimaryClipChangedListeners     // Catch:{ all -> 0x0022 }
            java.lang.Object[] r2 = r3.toArray()     // Catch:{ all -> 0x0022 }
            monitor-exit(r4)     // Catch:{ all -> 0x0022 }
            r1 = 0
        L_0x0015:
            int r3 = r2.length
            if (r1 >= r3) goto L_0x000c
            r3 = r2[r1]
            android.content.ClipboardManager$OnPrimaryClipChangedListener r3 = (android.content.ClipboardManager.OnPrimaryClipChangedListener) r3
            r3.onPrimaryClipChanged()
            int r1 = r1 + 1
            goto L_0x0015
        L_0x0022:
            r3 = move-exception
            monitor-exit(r4)
            throw r3
        */
        throw new UnsupportedOperationException("Method not decompiled: android.content.ClipboardManager.reportPrimaryClipChanged():void");
    }
}

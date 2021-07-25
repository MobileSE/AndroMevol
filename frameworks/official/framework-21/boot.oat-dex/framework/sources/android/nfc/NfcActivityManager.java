package android.nfc;

import android.app.Activity;
import android.app.Application;
import android.net.Uri;
import android.nfc.IAppCallback;
import android.nfc.NfcAdapter;
import android.os.Binder;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public final class NfcActivityManager extends IAppCallback.Stub implements Application.ActivityLifecycleCallbacks {
    static final Boolean DBG = false;
    static final String TAG = "NFC";
    final List<NfcActivityState> mActivities = new LinkedList();
    final NfcAdapter mAdapter;
    final List<NfcApplicationState> mApps = new ArrayList(1);
    final NfcEvent mDefaultEvent = new NfcEvent(this.mAdapter);

    /* access modifiers changed from: package-private */
    public class NfcApplicationState {
        final Application app;
        int refCount = 0;

        public NfcApplicationState(Application app2) {
            this.app = app2;
        }

        public void register() {
            this.refCount++;
            if (this.refCount == 1) {
                this.app.registerActivityLifecycleCallbacks(NfcActivityManager.this);
            }
        }

        public void unregister() {
            this.refCount--;
            if (this.refCount == 0) {
                this.app.unregisterActivityLifecycleCallbacks(NfcActivityManager.this);
            } else if (this.refCount < 0) {
                Log.e(NfcActivityManager.TAG, "-ve refcount for " + this.app);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public NfcApplicationState findAppState(Application app) {
        for (NfcApplicationState appState : this.mApps) {
            if (appState.app == app) {
                return appState;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void registerApplication(Application app) {
        NfcApplicationState appState = findAppState(app);
        if (appState == null) {
            appState = new NfcApplicationState(app);
            this.mApps.add(appState);
        }
        appState.register();
    }

    /* access modifiers changed from: package-private */
    public void unregisterApplication(Application app) {
        NfcApplicationState appState = findAppState(app);
        if (appState == null) {
            Log.e(TAG, "app was not registered " + app);
        } else {
            appState.unregister();
        }
    }

    /* access modifiers changed from: package-private */
    public class NfcActivityState {
        Activity activity;
        int flags = 0;
        NdefMessage ndefMessage = null;
        NfcAdapter.CreateNdefMessageCallback ndefMessageCallback = null;
        NfcAdapter.OnNdefPushCompleteCallback onNdefPushCompleteCallback = null;
        NfcAdapter.ReaderCallback readerCallback = null;
        Bundle readerModeExtras = null;
        int readerModeFlags = 0;
        boolean resumed = false;
        Binder token;
        NfcAdapter.CreateBeamUrisCallback uriCallback = null;
        Uri[] uris = null;

        public NfcActivityState(Activity activity2) {
            if (activity2.getWindow().isDestroyed()) {
                throw new IllegalStateException("activity is already destroyed");
            }
            this.resumed = activity2.isResumed();
            this.activity = activity2;
            this.token = new Binder();
            NfcActivityManager.this.registerApplication(activity2.getApplication());
        }

        public void destroy() {
            NfcActivityManager.this.unregisterApplication(this.activity.getApplication());
            this.resumed = false;
            this.activity = null;
            this.ndefMessage = null;
            this.ndefMessageCallback = null;
            this.onNdefPushCompleteCallback = null;
            this.uriCallback = null;
            this.uris = null;
            this.readerModeFlags = 0;
            this.token = null;
        }

        public String toString() {
            StringBuilder s = new StringBuilder("[").append(" ");
            s.append(this.ndefMessage).append(" ").append(this.ndefMessageCallback).append(" ");
            s.append(this.uriCallback).append(" ");
            if (this.uris != null) {
                for (Uri uri : this.uris) {
                    s.append(this.onNdefPushCompleteCallback).append(" ").append(uri).append("]");
                }
            }
            return s.toString();
        }
    }

    /* access modifiers changed from: package-private */
    public synchronized NfcActivityState findActivityState(Activity activity) {
        NfcActivityState state;
        Iterator i$ = this.mActivities.iterator();
        while (true) {
            if (!i$.hasNext()) {
                state = null;
                break;
            }
            state = i$.next();
            if (state.activity == activity) {
                break;
            }
        }
        return state;
    }

    /* access modifiers changed from: package-private */
    public synchronized NfcActivityState getActivityState(Activity activity) {
        NfcActivityState state;
        state = findActivityState(activity);
        if (state == null) {
            state = new NfcActivityState(activity);
            this.mActivities.add(state);
        }
        return state;
    }

    /* access modifiers changed from: package-private */
    public synchronized NfcActivityState findResumedActivityState() {
        NfcActivityState state;
        Iterator i$ = this.mActivities.iterator();
        while (true) {
            if (!i$.hasNext()) {
                state = null;
                break;
            }
            state = i$.next();
            if (state.resumed) {
                break;
            }
        }
        return state;
    }

    /* access modifiers changed from: package-private */
    public synchronized void destroyActivityState(Activity activity) {
        NfcActivityState activityState = findActivityState(activity);
        if (activityState != null) {
            activityState.destroy();
            this.mActivities.remove(activityState);
        }
    }

    public NfcActivityManager(NfcAdapter adapter) {
        this.mAdapter = adapter;
    }

    public void enableReaderMode(Activity activity, NfcAdapter.ReaderCallback callback, int flags, Bundle extras) {
        Binder token;
        boolean isResumed;
        synchronized (this) {
            NfcActivityState state = getActivityState(activity);
            state.readerCallback = callback;
            state.readerModeFlags = flags;
            state.readerModeExtras = extras;
            token = state.token;
            isResumed = state.resumed;
        }
        if (isResumed) {
            setReaderMode(token, flags, extras);
        }
    }

    public void disableReaderMode(Activity activity) {
        Binder token;
        boolean isResumed;
        synchronized (this) {
            NfcActivityState state = getActivityState(activity);
            state.readerCallback = null;
            state.readerModeFlags = 0;
            state.readerModeExtras = null;
            token = state.token;
            isResumed = state.resumed;
        }
        if (isResumed) {
            setReaderMode(token, 0, null);
        }
    }

    public void setReaderMode(Binder token, int flags, Bundle extras) {
        if (DBG.booleanValue()) {
            Log.d(TAG, "Setting reader mode");
        }
        try {
            NfcAdapter.sService.setReaderMode(token, this, flags, extras);
        } catch (RemoteException e) {
            this.mAdapter.attemptDeadServiceRecovery(e);
        }
    }

    public void setNdefPushContentUri(Activity activity, Uri[] uris) {
        boolean isResumed;
        synchronized (this) {
            NfcActivityState state = getActivityState(activity);
            state.uris = uris;
            isResumed = state.resumed;
        }
        if (isResumed) {
            requestNfcServiceCallback();
        }
    }

    public void setNdefPushContentUriCallback(Activity activity, NfcAdapter.CreateBeamUrisCallback callback) {
        boolean isResumed;
        synchronized (this) {
            NfcActivityState state = getActivityState(activity);
            state.uriCallback = callback;
            isResumed = state.resumed;
        }
        if (isResumed) {
            requestNfcServiceCallback();
        }
    }

    public void setNdefPushMessage(Activity activity, NdefMessage message, int flags) {
        boolean isResumed;
        synchronized (this) {
            NfcActivityState state = getActivityState(activity);
            state.ndefMessage = message;
            state.flags = flags;
            isResumed = state.resumed;
        }
        if (isResumed) {
            requestNfcServiceCallback();
        }
    }

    public void setNdefPushMessageCallback(Activity activity, NfcAdapter.CreateNdefMessageCallback callback, int flags) {
        boolean isResumed;
        synchronized (this) {
            NfcActivityState state = getActivityState(activity);
            state.ndefMessageCallback = callback;
            state.flags = flags;
            isResumed = state.resumed;
        }
        if (isResumed) {
            requestNfcServiceCallback();
        }
    }

    public void setOnNdefPushCompleteCallback(Activity activity, NfcAdapter.OnNdefPushCompleteCallback callback) {
        boolean isResumed;
        synchronized (this) {
            NfcActivityState state = getActivityState(activity);
            state.onNdefPushCompleteCallback = callback;
            isResumed = state.resumed;
        }
        if (isResumed) {
            requestNfcServiceCallback();
        }
    }

    /* access modifiers changed from: package-private */
    public void requestNfcServiceCallback() {
        try {
            NfcAdapter.sService.setAppCallback(this);
        } catch (RemoteException e) {
            this.mAdapter.attemptDeadServiceRecovery(e);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001f, code lost:
        if (r11 == null) goto L_0x005e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0021, code lost:
        r10 = r11.createBeamUris(r15.mDefaultEvent);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0027, code lost:
        if (r10 == null) goto L_0x005e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0029, code lost:
        r4 = r10.length;
        r3 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x002c, code lost:
        if (r3 >= r4) goto L_0x005e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x002e, code lost:
        r9 = r10[r3];
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0030, code lost:
        if (r9 != null) goto L_0x003d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0032, code lost:
        android.util.Log.e(android.nfc.NfcActivityManager.TAG, "Uri not allowed to be null.");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x003d, code lost:
        r7 = r9.getScheme();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0041, code lost:
        if (r7 == null) goto L_0x0053;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0049, code lost:
        if (r7.equalsIgnoreCase(android.content.ContentResolver.SCHEME_FILE) != false) goto L_0x005b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0051, code lost:
        if (r7.equalsIgnoreCase("content") != false) goto L_0x005b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0053, code lost:
        android.util.Log.e(android.nfc.NfcActivityManager.TAG, "Uri needs to have either scheme file or scheme content");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x005b, code lost:
        r3 = r3 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x005e, code lost:
        if (r10 == null) goto L_0x0073;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0061, code lost:
        if (r10.length <= 0) goto L_0x0073;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0063, code lost:
        r4 = r10.length;
        r3 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0066, code lost:
        if (r3 >= r4) goto L_0x0073;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0068, code lost:
        r0.grantUriPermission("com.android.nfc", r10[r3], 1);
        r3 = r3 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:?, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:?, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:?, code lost:
        return new android.nfc.BeamShareData(r5, r10, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0017, code lost:
        if (r6 == null) goto L_0x001f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0019, code lost:
        r5 = r6.createNdefMessage(r15.mDefaultEvent);
     */
    @Override // android.nfc.IAppCallback
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public android.nfc.BeamShareData createBeamShareData() {
        /*
        // Method dump skipped, instructions count: 121
        */
        throw new UnsupportedOperationException("Method not decompiled: android.nfc.NfcActivityManager.createBeamShareData():android.nfc.BeamShareData");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:7:0x000c, code lost:
        if (r0 == null) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x000e, code lost:
        r0.onNdefPushComplete(r3.mDefaultEvent);
     */
    @Override // android.nfc.IAppCallback
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onNdefPushComplete() {
        /*
            r3 = this;
            monitor-enter(r3)
            android.nfc.NfcActivityManager$NfcActivityState r1 = r3.findResumedActivityState()     // Catch:{ all -> 0x0014 }
            if (r1 != 0) goto L_0x0009
            monitor-exit(r3)     // Catch:{ all -> 0x0014 }
        L_0x0008:
            return
        L_0x0009:
            android.nfc.NfcAdapter$OnNdefPushCompleteCallback r0 = r1.onNdefPushCompleteCallback     // Catch:{ all -> 0x0014 }
            monitor-exit(r3)     // Catch:{ all -> 0x0014 }
            if (r0 == 0) goto L_0x0008
            android.nfc.NfcEvent r2 = r3.mDefaultEvent
            r0.onNdefPushComplete(r2)
            goto L_0x0008
        L_0x0014:
            r2 = move-exception
            monitor-exit(r3)
            throw r2
        */
        throw new UnsupportedOperationException("Method not decompiled: android.nfc.NfcActivityManager.onNdefPushComplete():void");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:7:0x000c, code lost:
        if (r0 == null) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x000e, code lost:
        r0.onTagDiscovered(r4);
     */
    @Override // android.nfc.IAppCallback
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onTagDiscovered(android.nfc.Tag r4) throws android.os.RemoteException {
        /*
            r3 = this;
            monitor-enter(r3)
            android.nfc.NfcActivityManager$NfcActivityState r1 = r3.findResumedActivityState()     // Catch:{ all -> 0x0012 }
            if (r1 != 0) goto L_0x0009
            monitor-exit(r3)     // Catch:{ all -> 0x0012 }
        L_0x0008:
            return
        L_0x0009:
            android.nfc.NfcAdapter$ReaderCallback r0 = r1.readerCallback     // Catch:{ all -> 0x0012 }
            monitor-exit(r3)     // Catch:{ all -> 0x0012 }
            if (r0 == 0) goto L_0x0008
            r0.onTagDiscovered(r4)
            goto L_0x0008
        L_0x0012:
            r2 = move-exception
            monitor-exit(r3)
            throw r2
        */
        throw new UnsupportedOperationException("Method not decompiled: android.nfc.NfcActivityManager.onTagDiscovered(android.nfc.Tag):void");
    }

    @Override // android.app.Application.ActivityLifecycleCallbacks
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override // android.app.Application.ActivityLifecycleCallbacks
    public void onActivityStarted(Activity activity) {
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0040, code lost:
        if (r1 == 0) goto L_0x0045;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0042, code lost:
        setReaderMode(r3, r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0045, code lost:
        requestNfcServiceCallback();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:?, code lost:
        return;
     */
    @Override // android.app.Application.ActivityLifecycleCallbacks
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onActivityResumed(android.app.Activity r8) {
        /*
            r7 = this;
            r1 = 0
            r0 = 0
            monitor-enter(r7)
            android.nfc.NfcActivityManager$NfcActivityState r2 = r7.findActivityState(r8)     // Catch:{ all -> 0x0049 }
            java.lang.Boolean r4 = android.nfc.NfcActivityManager.DBG     // Catch:{ all -> 0x0049 }
            boolean r4 = r4.booleanValue()     // Catch:{ all -> 0x0049 }
            if (r4 == 0) goto L_0x0032
            java.lang.String r4 = "NFC"
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x0049 }
            r5.<init>()     // Catch:{ all -> 0x0049 }
            java.lang.String r6 = "onResume() for "
            java.lang.StringBuilder r5 = r5.append(r6)     // Catch:{ all -> 0x0049 }
            java.lang.StringBuilder r5 = r5.append(r8)     // Catch:{ all -> 0x0049 }
            java.lang.String r6 = " "
            java.lang.StringBuilder r5 = r5.append(r6)     // Catch:{ all -> 0x0049 }
            java.lang.StringBuilder r5 = r5.append(r2)     // Catch:{ all -> 0x0049 }
            java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x0049 }
            android.util.Log.d(r4, r5)     // Catch:{ all -> 0x0049 }
        L_0x0032:
            if (r2 != 0) goto L_0x0036
            monitor-exit(r7)     // Catch:{ all -> 0x0049 }
        L_0x0035:
            return
        L_0x0036:
            r4 = 1
            r2.resumed = r4     // Catch:{ all -> 0x0049 }
            android.os.Binder r3 = r2.token     // Catch:{ all -> 0x0049 }
            int r1 = r2.readerModeFlags     // Catch:{ all -> 0x0049 }
            android.os.Bundle r0 = r2.readerModeExtras     // Catch:{ all -> 0x0049 }
            monitor-exit(r7)     // Catch:{ all -> 0x0049 }
            if (r1 == 0) goto L_0x0045
            r7.setReaderMode(r3, r1, r0)
        L_0x0045:
            r7.requestNfcServiceCallback()
            goto L_0x0035
        L_0x0049:
            r4 = move-exception
            monitor-exit(r7)
            throw r4
        */
        throw new UnsupportedOperationException("Method not decompiled: android.nfc.NfcActivityManager.onActivityResumed(android.app.Activity):void");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0040, code lost:
        if (r0 == false) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0042, code lost:
        setReaderMode(r2, 0, null);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:?, code lost:
        return;
     */
    @Override // android.app.Application.ActivityLifecycleCallbacks
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onActivityPaused(android.app.Activity r8) {
        /*
            r7 = this;
            r3 = 0
            monitor-enter(r7)
            android.nfc.NfcActivityManager$NfcActivityState r1 = r7.findActivityState(r8)     // Catch:{ all -> 0x0049 }
            java.lang.Boolean r4 = android.nfc.NfcActivityManager.DBG     // Catch:{ all -> 0x0049 }
            boolean r4 = r4.booleanValue()     // Catch:{ all -> 0x0049 }
            if (r4 == 0) goto L_0x0031
            java.lang.String r4 = "NFC"
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x0049 }
            r5.<init>()     // Catch:{ all -> 0x0049 }
            java.lang.String r6 = "onPause() for "
            java.lang.StringBuilder r5 = r5.append(r6)     // Catch:{ all -> 0x0049 }
            java.lang.StringBuilder r5 = r5.append(r8)     // Catch:{ all -> 0x0049 }
            java.lang.String r6 = " "
            java.lang.StringBuilder r5 = r5.append(r6)     // Catch:{ all -> 0x0049 }
            java.lang.StringBuilder r5 = r5.append(r1)     // Catch:{ all -> 0x0049 }
            java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x0049 }
            android.util.Log.d(r4, r5)     // Catch:{ all -> 0x0049 }
        L_0x0031:
            if (r1 != 0) goto L_0x0035
            monitor-exit(r7)     // Catch:{ all -> 0x0049 }
        L_0x0034:
            return
        L_0x0035:
            r4 = 0
            r1.resumed = r4     // Catch:{ all -> 0x0049 }
            android.os.Binder r2 = r1.token     // Catch:{ all -> 0x0049 }
            int r4 = r1.readerModeFlags     // Catch:{ all -> 0x0049 }
            if (r4 == 0) goto L_0x0047
            r0 = 1
        L_0x003f:
            monitor-exit(r7)     // Catch:{ all -> 0x0049 }
            if (r0 == 0) goto L_0x0034
            r4 = 0
            r7.setReaderMode(r2, r3, r4)
            goto L_0x0034
        L_0x0047:
            r0 = r3
            goto L_0x003f
        L_0x0049:
            r3 = move-exception
            monitor-exit(r7)
            throw r3
        */
        throw new UnsupportedOperationException("Method not decompiled: android.nfc.NfcActivityManager.onActivityPaused(android.app.Activity):void");
    }

    @Override // android.app.Application.ActivityLifecycleCallbacks
    public void onActivityStopped(Activity activity) {
    }

    @Override // android.app.Application.ActivityLifecycleCallbacks
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override // android.app.Application.ActivityLifecycleCallbacks
    public void onActivityDestroyed(Activity activity) {
        synchronized (this) {
            NfcActivityState state = findActivityState(activity);
            if (DBG.booleanValue()) {
                Log.d(TAG, "onDestroy() for " + activity + " " + state);
            }
            if (state != null) {
                destroyActivityState(activity);
            }
        }
    }
}

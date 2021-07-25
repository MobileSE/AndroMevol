package com.android.internal.os.storage;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.storage.IMountService;
import android.os.storage.StorageEventListener;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;
import android.widget.Toast;
import com.android.internal.R;

public class ExternalStorageFormatter extends Service implements DialogInterface.OnCancelListener {
    public static final ComponentName COMPONENT_NAME = new ComponentName("android", ExternalStorageFormatter.class.getName());
    public static final String EXTRA_ALWAYS_RESET = "always_reset";
    public static final String FORMAT_AND_FACTORY_RESET = "com.android.internal.os.storage.FORMAT_AND_FACTORY_RESET";
    public static final String FORMAT_ONLY = "com.android.internal.os.storage.FORMAT_ONLY";
    static final String TAG = "ExternalStorageFormatter";
    private boolean mAlwaysReset = false;
    private boolean mFactoryReset = false;
    private IMountService mMountService = null;
    private ProgressDialog mProgressDialog = null;
    private String mReason = null;
    StorageEventListener mStorageListener = new StorageEventListener() {
        /* class com.android.internal.os.storage.ExternalStorageFormatter.AnonymousClass1 */

        @Override // android.os.storage.StorageEventListener
        public void onStorageStateChanged(String path, String oldState, String newState) {
            Log.i(ExternalStorageFormatter.TAG, "Received storage state changed notification that " + path + " changed state from " + oldState + " to " + newState);
            ExternalStorageFormatter.this.updateProgressState();
        }
    };
    private StorageManager mStorageManager = null;
    private ExternalStorageState mStorageState = ExternalStorageState.INITIAL;
    private StorageVolume mStorageVolume;
    private PowerManager.WakeLock mWakeLock;

    /* access modifiers changed from: private */
    public enum ExternalStorageState {
        INITIAL,
        UNMOUNTING,
        FORMATTING,
        MOUNTING,
        COMPLETED
    }

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        if (this.mStorageManager == null) {
            this.mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
            this.mStorageManager.registerListener(this.mStorageListener);
        }
        this.mWakeLock = ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(1, TAG);
        this.mWakeLock.acquire();
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (FORMAT_AND_FACTORY_RESET.equals(intent.getAction())) {
            this.mFactoryReset = true;
        }
        if (intent.getBooleanExtra(EXTRA_ALWAYS_RESET, false)) {
            this.mAlwaysReset = true;
        }
        this.mReason = intent.getStringExtra(Intent.EXTRA_REASON);
        this.mStorageVolume = (StorageVolume) intent.getParcelableExtra(StorageVolume.EXTRA_STORAGE_VOLUME);
        if (this.mProgressDialog != null) {
            return 3;
        }
        this.mProgressDialog = new ProgressDialog(this);
        this.mProgressDialog.setIndeterminate(true);
        this.mProgressDialog.setCancelable(true);
        this.mProgressDialog.getWindow().setType(2003);
        if (!this.mAlwaysReset) {
            this.mProgressDialog.setOnCancelListener(this);
        }
        updateProgressState();
        this.mProgressDialog.show();
        return 3;
    }

    @Override // android.app.Service
    public void onDestroy() {
        if (this.mStorageManager != null) {
            this.mStorageManager.unregisterListener(this.mStorageListener);
        }
        if (this.mProgressDialog != null) {
            this.mProgressDialog.dismiss();
        }
        this.mWakeLock.release();
        super.onDestroy();
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override // android.content.DialogInterface.OnCancelListener
    public void onCancel(DialogInterface dialog) {
        IMountService mountService = getMountService();
        String extStoragePath = this.mStorageVolume == null ? Environment.getLegacyExternalStorageDirectory().toString() : this.mStorageVolume.getPath();
        try {
            this.mStorageState = ExternalStorageState.MOUNTING;
            mountService.mountVolume(extStoragePath);
        } catch (RemoteException e) {
            Log.w(TAG, "Failed talking with mount service", e);
        }
        finish();
    }

    /* access modifiers changed from: package-private */
    public void finish() {
        this.mStorageState = ExternalStorageState.COMPLETED;
        stopSelf();
    }

    /* access modifiers changed from: package-private */
    public void fail(int msg) {
        Toast.makeText(this, msg, 1).show();
        if (this.mAlwaysReset) {
            Intent intent = new Intent(Intent.ACTION_MASTER_CLEAR);
            intent.addFlags(268435456);
            intent.putExtra(Intent.EXTRA_REASON, this.mReason);
            sendBroadcast(intent);
        }
        finish();
    }

    /* access modifiers changed from: package-private */
    public void updateProgressState() {
        String status;
        final String extStoragePath;
        if (this.mStorageVolume == null) {
            status = Environment.getExternalStorageState();
        } else {
            status = this.mStorageManager.getVolumeState(this.mStorageVolume.getPath());
        }
        if (this.mStorageState == ExternalStorageState.MOUNTING || this.mStorageState == ExternalStorageState.COMPLETED) {
            Log.w(TAG, "Received progress state change: " + status + " while in state: " + this.mStorageState);
        } else if (Environment.MEDIA_MOUNTED.equals(status) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(status)) {
            updateProgressDialog(R.string.progress_unmounting);
            IMountService mountService = getMountService();
            String extStoragePath2 = this.mStorageVolume == null ? Environment.getLegacyExternalStorageDirectory().toString() : this.mStorageVolume.getPath();
            try {
                this.mStorageState = ExternalStorageState.UNMOUNTING;
                mountService.unmountVolume(extStoragePath2, true, this.mFactoryReset);
            } catch (RemoteException e) {
                Log.w(TAG, "Failed talking with mount service", e);
            }
        } else if (Environment.MEDIA_NOFS.equals(status) || Environment.MEDIA_UNMOUNTED.equals(status) || Environment.MEDIA_UNMOUNTABLE.equals(status)) {
            updateProgressDialog(R.string.progress_erasing);
            final IMountService mountService2 = getMountService();
            if (this.mStorageVolume == null) {
                extStoragePath = Environment.getLegacyExternalStorageDirectory().toString();
            } else {
                extStoragePath = this.mStorageVolume.getPath();
            }
            if (mountService2 != null) {
                new Thread() {
                    /* class com.android.internal.os.storage.ExternalStorageFormatter.AnonymousClass2 */

                    public void run() {
                        boolean success = false;
                        try {
                            ExternalStorageFormatter.this.mStorageState = ExternalStorageState.FORMATTING;
                            mountService2.formatVolume(extStoragePath);
                            success = true;
                        } catch (Exception e) {
                            Toast.makeText(ExternalStorageFormatter.this, (int) R.string.format_error, 1).show();
                        }
                        if (!success || !ExternalStorageFormatter.this.mFactoryReset) {
                            if (success || !ExternalStorageFormatter.this.mAlwaysReset) {
                                try {
                                    ExternalStorageFormatter.this.mStorageState = ExternalStorageState.MOUNTING;
                                    mountService2.mountVolume(extStoragePath);
                                } catch (RemoteException e2) {
                                    Log.w(ExternalStorageFormatter.TAG, "Failed talking with mount service", e2);
                                }
                            } else {
                                Intent intent = new Intent(Intent.ACTION_MASTER_CLEAR);
                                intent.addFlags(268435456);
                                intent.putExtra(Intent.EXTRA_REASON, ExternalStorageFormatter.this.mReason);
                                ExternalStorageFormatter.this.sendBroadcast(intent);
                            }
                            ExternalStorageFormatter.this.finish();
                            return;
                        }
                        Intent intent2 = new Intent(Intent.ACTION_MASTER_CLEAR);
                        intent2.addFlags(268435456);
                        intent2.putExtra(Intent.EXTRA_REASON, ExternalStorageFormatter.this.mReason);
                        ExternalStorageFormatter.this.sendBroadcast(intent2);
                        ExternalStorageFormatter.this.finish();
                    }
                }.start();
            } else {
                Log.w(TAG, "Unable to locate IMountService");
            }
        } else if (Environment.MEDIA_BAD_REMOVAL.equals(status)) {
            fail(R.string.media_bad_removal);
        } else if (Environment.MEDIA_CHECKING.equals(status)) {
            fail(R.string.media_checking);
        } else if (Environment.MEDIA_REMOVED.equals(status)) {
            fail(R.string.media_removed);
        } else if ("shared".equals(status)) {
            fail(R.string.media_shared);
        } else {
            fail(R.string.media_unknown_state);
            Log.w(TAG, "Unknown storage state: " + status);
            stopSelf();
        }
    }

    public void updateProgressDialog(int msg) {
        if (this.mProgressDialog == null) {
            this.mProgressDialog = new ProgressDialog(this);
            this.mProgressDialog.setIndeterminate(true);
            this.mProgressDialog.setCancelable(false);
            this.mProgressDialog.getWindow().setType(2003);
            this.mProgressDialog.show();
        }
        this.mProgressDialog.setMessage(getText(msg));
    }

    /* access modifiers changed from: package-private */
    public IMountService getMountService() {
        if (this.mMountService == null) {
            IBinder service = ServiceManager.getService("mount");
            if (service != null) {
                this.mMountService = IMountService.Stub.asInterface(service);
            } else {
                Log.e(TAG, "Can't get mount service");
            }
        }
        return this.mMountService;
    }
}

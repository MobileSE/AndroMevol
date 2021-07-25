package android.content;

import android.Manifest;
import android.app.AppOpsManager;
import android.app.backup.FullBackup;
import android.content.pm.PathPermission;
import android.content.pm.ProviderInfo;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.ProxyInfo;
import android.net.Uri;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ICancellationSignal;
import android.os.ParcelFileDescriptor;
import android.os.Process;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public abstract class ContentProvider implements ComponentCallbacks2 {
    private static final String TAG = "ContentProvider";
    private String[] mAuthorities;
    private String mAuthority;
    private final ThreadLocal<String> mCallingPackage = new ThreadLocal<>();
    private Context mContext = null;
    private boolean mExported;
    private int mMyUid;
    private boolean mNoPerms;
    private PathPermission[] mPathPermissions;
    private String mReadPermission;
    private boolean mSingleUser;
    private Transport mTransport = new Transport();
    private String mWritePermission;

    public interface PipeDataWriter<T> {
        void writeDataToPipe(ParcelFileDescriptor parcelFileDescriptor, Uri uri, String str, Bundle bundle, T t);
    }

    public abstract int delete(Uri uri, String str, String[] strArr);

    public abstract String getType(Uri uri);

    public abstract Uri insert(Uri uri, ContentValues contentValues);

    public abstract boolean onCreate();

    public abstract Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2);

    public abstract int update(Uri uri, ContentValues contentValues, String str, String[] strArr);

    public ContentProvider() {
    }

    public ContentProvider(Context context, String readPermission, String writePermission, PathPermission[] pathPermissions) {
        this.mContext = context;
        this.mReadPermission = readPermission;
        this.mWritePermission = writePermission;
        this.mPathPermissions = pathPermissions;
    }

    public static ContentProvider coerceToLocalContentProvider(IContentProvider abstractInterface) {
        if (abstractInterface instanceof Transport) {
            return ((Transport) abstractInterface).getContentProvider();
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public class Transport extends ContentProviderNative {
        AppOpsManager mAppOpsManager = null;
        int mReadOp = -1;
        int mWriteOp = -1;

        Transport() {
        }

        /* access modifiers changed from: package-private */
        public ContentProvider getContentProvider() {
            return ContentProvider.this;
        }

        @Override // android.content.ContentProviderNative
        public String getProviderName() {
            return getContentProvider().getClass().getName();
        }

        @Override // android.content.IContentProvider
        public Cursor query(String callingPkg, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder, ICancellationSignal cancellationSignal) {
            ContentProvider.this.validateIncomingUri(uri);
            Uri uri2 = ContentProvider.getUriWithoutUserId(uri);
            if (enforceReadPermission(callingPkg, uri2) != 0) {
                return ContentProvider.this.rejectQuery(uri2, projection, selection, selectionArgs, sortOrder, CancellationSignal.fromTransport(cancellationSignal));
            }
            String original = ContentProvider.this.setCallingPackage(callingPkg);
            try {
                return ContentProvider.this.query(uri2, projection, selection, selectionArgs, sortOrder, CancellationSignal.fromTransport(cancellationSignal));
            } finally {
                ContentProvider.this.setCallingPackage(original);
            }
        }

        @Override // android.content.IContentProvider
        public String getType(Uri uri) {
            ContentProvider.this.validateIncomingUri(uri);
            return ContentProvider.this.getType(ContentProvider.getUriWithoutUserId(uri));
        }

        @Override // android.content.IContentProvider
        public Uri insert(String callingPkg, Uri uri, ContentValues initialValues) {
            ContentProvider.this.validateIncomingUri(uri);
            int userId = ContentProvider.getUserIdFromUri(uri);
            Uri uri2 = ContentProvider.getUriWithoutUserId(uri);
            if (enforceWritePermission(callingPkg, uri2) != 0) {
                return ContentProvider.this.rejectInsert(uri2, initialValues);
            }
            String original = ContentProvider.this.setCallingPackage(callingPkg);
            try {
                return ContentProvider.maybeAddUserId(ContentProvider.this.insert(uri2, initialValues), userId);
            } finally {
                ContentProvider.this.setCallingPackage(original);
            }
        }

        @Override // android.content.IContentProvider
        public int bulkInsert(String callingPkg, Uri uri, ContentValues[] initialValues) {
            ContentProvider.this.validateIncomingUri(uri);
            Uri uri2 = ContentProvider.getUriWithoutUserId(uri);
            if (enforceWritePermission(callingPkg, uri2) != 0) {
                return 0;
            }
            String original = ContentProvider.this.setCallingPackage(callingPkg);
            try {
                return ContentProvider.this.bulkInsert(uri2, initialValues);
            } finally {
                ContentProvider.this.setCallingPackage(original);
            }
        }

        @Override // android.content.IContentProvider
        public ContentProviderResult[] applyBatch(String callingPkg, ArrayList<ContentProviderOperation> operations) throws OperationApplicationException {
            int numOperations = operations.size();
            int[] userIds = new int[numOperations];
            for (int i = 0; i < numOperations; i++) {
                ContentProviderOperation operation = operations.get(i);
                Uri uri = operation.getUri();
                ContentProvider.this.validateIncomingUri(uri);
                userIds[i] = ContentProvider.getUserIdFromUri(uri);
                if (userIds[i] != -2) {
                    ContentProviderOperation operation2 = new ContentProviderOperation(operation, true);
                    operations.set(i, operation2);
                    operation = operation2;
                }
                if (operation.isReadOperation() && enforceReadPermission(callingPkg, uri) != 0) {
                    throw new OperationApplicationException("App op not allowed", 0);
                } else if (operation.isWriteOperation() && enforceWritePermission(callingPkg, uri) != 0) {
                    throw new OperationApplicationException("App op not allowed", 0);
                }
            }
            String original = ContentProvider.this.setCallingPackage(callingPkg);
            try {
                ContentProviderResult[] results = ContentProvider.this.applyBatch(operations);
                for (int i2 = 0; i2 < results.length; i2++) {
                    if (userIds[i2] != -2) {
                        results[i2] = new ContentProviderResult(results[i2], userIds[i2]);
                    }
                }
                return results;
            } finally {
                ContentProvider.this.setCallingPackage(original);
            }
        }

        @Override // android.content.IContentProvider
        public int delete(String callingPkg, Uri uri, String selection, String[] selectionArgs) {
            ContentProvider.this.validateIncomingUri(uri);
            Uri uri2 = ContentProvider.getUriWithoutUserId(uri);
            if (enforceWritePermission(callingPkg, uri2) != 0) {
                return 0;
            }
            String original = ContentProvider.this.setCallingPackage(callingPkg);
            try {
                return ContentProvider.this.delete(uri2, selection, selectionArgs);
            } finally {
                ContentProvider.this.setCallingPackage(original);
            }
        }

        @Override // android.content.IContentProvider
        public int update(String callingPkg, Uri uri, ContentValues values, String selection, String[] selectionArgs) {
            ContentProvider.this.validateIncomingUri(uri);
            Uri uri2 = ContentProvider.getUriWithoutUserId(uri);
            if (enforceWritePermission(callingPkg, uri2) != 0) {
                return 0;
            }
            String original = ContentProvider.this.setCallingPackage(callingPkg);
            try {
                return ContentProvider.this.update(uri2, values, selection, selectionArgs);
            } finally {
                ContentProvider.this.setCallingPackage(original);
            }
        }

        @Override // android.content.IContentProvider
        public ParcelFileDescriptor openFile(String callingPkg, Uri uri, String mode, ICancellationSignal cancellationSignal) throws FileNotFoundException {
            ContentProvider.this.validateIncomingUri(uri);
            Uri uri2 = ContentProvider.getUriWithoutUserId(uri);
            enforceFilePermission(callingPkg, uri2, mode);
            String original = ContentProvider.this.setCallingPackage(callingPkg);
            try {
                return ContentProvider.this.openFile(uri2, mode, CancellationSignal.fromTransport(cancellationSignal));
            } finally {
                ContentProvider.this.setCallingPackage(original);
            }
        }

        @Override // android.content.IContentProvider
        public AssetFileDescriptor openAssetFile(String callingPkg, Uri uri, String mode, ICancellationSignal cancellationSignal) throws FileNotFoundException {
            ContentProvider.this.validateIncomingUri(uri);
            Uri uri2 = ContentProvider.getUriWithoutUserId(uri);
            enforceFilePermission(callingPkg, uri2, mode);
            String original = ContentProvider.this.setCallingPackage(callingPkg);
            try {
                return ContentProvider.this.openAssetFile(uri2, mode, CancellationSignal.fromTransport(cancellationSignal));
            } finally {
                ContentProvider.this.setCallingPackage(original);
            }
        }

        @Override // android.content.IContentProvider
        public Bundle call(String callingPkg, String method, String arg, Bundle extras) {
            String original = ContentProvider.this.setCallingPackage(callingPkg);
            try {
                return ContentProvider.this.call(method, arg, extras);
            } finally {
                ContentProvider.this.setCallingPackage(original);
            }
        }

        @Override // android.content.IContentProvider
        public String[] getStreamTypes(Uri uri, String mimeTypeFilter) {
            ContentProvider.this.validateIncomingUri(uri);
            return ContentProvider.this.getStreamTypes(ContentProvider.getUriWithoutUserId(uri), mimeTypeFilter);
        }

        @Override // android.content.IContentProvider
        public AssetFileDescriptor openTypedAssetFile(String callingPkg, Uri uri, String mimeType, Bundle opts, ICancellationSignal cancellationSignal) throws FileNotFoundException {
            ContentProvider.this.validateIncomingUri(uri);
            Uri uri2 = ContentProvider.getUriWithoutUserId(uri);
            enforceFilePermission(callingPkg, uri2, FullBackup.ROOT_TREE_TOKEN);
            String original = ContentProvider.this.setCallingPackage(callingPkg);
            try {
                return ContentProvider.this.openTypedAssetFile(uri2, mimeType, opts, CancellationSignal.fromTransport(cancellationSignal));
            } finally {
                ContentProvider.this.setCallingPackage(original);
            }
        }

        @Override // android.content.IContentProvider
        public ICancellationSignal createCancellationSignal() {
            return CancellationSignal.createTransport();
        }

        @Override // android.content.IContentProvider
        public Uri canonicalize(String callingPkg, Uri uri) {
            ContentProvider.this.validateIncomingUri(uri);
            int userId = ContentProvider.getUserIdFromUri(uri);
            Uri uri2 = ContentProvider.getUriWithoutUserId(uri);
            if (enforceReadPermission(callingPkg, uri2) != 0) {
                return null;
            }
            String original = ContentProvider.this.setCallingPackage(callingPkg);
            try {
                return ContentProvider.maybeAddUserId(ContentProvider.this.canonicalize(uri2), userId);
            } finally {
                ContentProvider.this.setCallingPackage(original);
            }
        }

        @Override // android.content.IContentProvider
        public Uri uncanonicalize(String callingPkg, Uri uri) {
            ContentProvider.this.validateIncomingUri(uri);
            int userId = ContentProvider.getUserIdFromUri(uri);
            Uri uri2 = ContentProvider.getUriWithoutUserId(uri);
            if (enforceReadPermission(callingPkg, uri2) != 0) {
                return null;
            }
            String original = ContentProvider.this.setCallingPackage(callingPkg);
            try {
                return ContentProvider.maybeAddUserId(ContentProvider.this.uncanonicalize(uri2), userId);
            } finally {
                ContentProvider.this.setCallingPackage(original);
            }
        }

        private void enforceFilePermission(String callingPkg, Uri uri, String mode) throws FileNotFoundException, SecurityException {
            if (mode == null || mode.indexOf(119) == -1) {
                if (enforceReadPermission(callingPkg, uri) != 0) {
                    throw new FileNotFoundException("App op not allowed");
                }
            } else if (enforceWritePermission(callingPkg, uri) != 0) {
                throw new FileNotFoundException("App op not allowed");
            }
        }

        private int enforceReadPermission(String callingPkg, Uri uri) throws SecurityException {
            ContentProvider.this.enforceReadPermissionInner(uri);
            if (this.mReadOp != -1) {
                return this.mAppOpsManager.noteOp(this.mReadOp, Binder.getCallingUid(), callingPkg);
            }
            return 0;
        }

        private int enforceWritePermission(String callingPkg, Uri uri) throws SecurityException {
            ContentProvider.this.enforceWritePermissionInner(uri);
            if (this.mWriteOp != -1) {
                return this.mAppOpsManager.noteOp(this.mWriteOp, Binder.getCallingUid(), callingPkg);
            }
            return 0;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean checkUser(int pid, int uid, Context context) {
        return UserHandle.getUserId(uid) == context.getUserId() || this.mSingleUser || context.checkPermission(Manifest.permission.INTERACT_ACROSS_USERS, pid, uid) == 0;
    }

    /* access modifiers changed from: protected */
    public void enforceReadPermissionInner(Uri uri) throws SecurityException {
        Context context = getContext();
        int pid = Binder.getCallingPid();
        int uid = Binder.getCallingUid();
        String missingPerm = null;
        if (!UserHandle.isSameApp(uid, this.mMyUid)) {
            if (this.mExported && checkUser(pid, uid, context)) {
                String componentPerm = getReadPermission();
                if (componentPerm != null) {
                    if (context.checkPermission(componentPerm, pid, uid) != 0) {
                        missingPerm = componentPerm;
                    } else {
                        return;
                    }
                }
                boolean allowDefaultRead = componentPerm == null;
                PathPermission[] pps = getPathPermissions();
                if (pps != null) {
                    String path = uri.getPath();
                    for (PathPermission pp : pps) {
                        String pathPerm = pp.getReadPermission();
                        if (pathPerm != null && pp.match(path)) {
                            if (context.checkPermission(pathPerm, pid, uid) != 0) {
                                allowDefaultRead = false;
                                missingPerm = pathPerm;
                            } else {
                                return;
                            }
                        }
                    }
                }
                if (allowDefaultRead) {
                    return;
                }
            }
            if (context.checkUriPermission(uri, pid, uid, 1) != 0) {
                throw new SecurityException("Permission Denial: reading " + getClass().getName() + " uri " + uri + " from pid=" + pid + ", uid=" + uid + (this.mExported ? " requires " + missingPerm + ", or grantUriPermission()" : " requires the provider be exported, or grantUriPermission()"));
            }
        }
    }

    /* access modifiers changed from: protected */
    public void enforceWritePermissionInner(Uri uri) throws SecurityException {
        Context context = getContext();
        int pid = Binder.getCallingPid();
        int uid = Binder.getCallingUid();
        String missingPerm = null;
        if (!UserHandle.isSameApp(uid, this.mMyUid)) {
            if (this.mExported && checkUser(pid, uid, context)) {
                String componentPerm = getWritePermission();
                if (componentPerm != null) {
                    if (context.checkPermission(componentPerm, pid, uid) != 0) {
                        missingPerm = componentPerm;
                    } else {
                        return;
                    }
                }
                boolean allowDefaultWrite = componentPerm == null;
                PathPermission[] pps = getPathPermissions();
                if (pps != null) {
                    String path = uri.getPath();
                    for (PathPermission pp : pps) {
                        String pathPerm = pp.getWritePermission();
                        if (pathPerm != null && pp.match(path)) {
                            if (context.checkPermission(pathPerm, pid, uid) != 0) {
                                allowDefaultWrite = false;
                                missingPerm = pathPerm;
                            } else {
                                return;
                            }
                        }
                    }
                }
                if (allowDefaultWrite) {
                    return;
                }
            }
            if (context.checkUriPermission(uri, pid, uid, 2) != 0) {
                throw new SecurityException("Permission Denial: writing " + getClass().getName() + " uri " + uri + " from pid=" + pid + ", uid=" + uid + (this.mExported ? " requires " + missingPerm + ", or grantUriPermission()" : " requires the provider be exported, or grantUriPermission()"));
            }
        }
    }

    public final Context getContext() {
        return this.mContext;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String setCallingPackage(String callingPackage) {
        String original = this.mCallingPackage.get();
        this.mCallingPackage.set(callingPackage);
        return original;
    }

    public final String getCallingPackage() {
        String pkg = this.mCallingPackage.get();
        if (pkg != null) {
            this.mTransport.mAppOpsManager.checkPackage(Binder.getCallingUid(), pkg);
        }
        return pkg;
    }

    /* access modifiers changed from: protected */
    public final void setAuthorities(String authorities) {
        if (authorities == null) {
            return;
        }
        if (authorities.indexOf(59) == -1) {
            this.mAuthority = authorities;
            this.mAuthorities = null;
            return;
        }
        this.mAuthority = null;
        this.mAuthorities = authorities.split(";");
    }

    /* access modifiers changed from: protected */
    public final boolean matchesOurAuthorities(String authority) {
        if (this.mAuthority != null) {
            return this.mAuthority.equals(authority);
        }
        if (this.mAuthorities != null) {
            int length = this.mAuthorities.length;
            for (int i = 0; i < length; i++) {
                if (this.mAuthorities[i].equals(authority)) {
                    return true;
                }
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public final void setReadPermission(String permission) {
        this.mReadPermission = permission;
    }

    public final String getReadPermission() {
        return this.mReadPermission;
    }

    /* access modifiers changed from: protected */
    public final void setWritePermission(String permission) {
        this.mWritePermission = permission;
    }

    public final String getWritePermission() {
        return this.mWritePermission;
    }

    /* access modifiers changed from: protected */
    public final void setPathPermissions(PathPermission[] permissions) {
        this.mPathPermissions = permissions;
    }

    public final PathPermission[] getPathPermissions() {
        return this.mPathPermissions;
    }

    public final void setAppOps(int readOp, int writeOp) {
        if (!this.mNoPerms) {
            this.mTransport.mReadOp = readOp;
            this.mTransport.mWriteOp = writeOp;
        }
    }

    public AppOpsManager getAppOpsManager() {
        return this.mTransport.mAppOpsManager;
    }

    @Override // android.content.ComponentCallbacks
    public void onConfigurationChanged(Configuration newConfig) {
    }

    @Override // android.content.ComponentCallbacks
    public void onLowMemory() {
    }

    @Override // android.content.ComponentCallbacks2
    public void onTrimMemory(int level) {
    }

    public Cursor rejectQuery(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder, CancellationSignal cancellationSignal) {
        String selection2;
        if (selection == null || selection.isEmpty()) {
            selection2 = "'A' = 'B'";
        } else {
            selection2 = "'A' = 'B' AND (" + selection + ")";
        }
        return query(uri, projection, selection2, selectionArgs, sortOrder, cancellationSignal);
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder, CancellationSignal cancellationSignal) {
        return query(uri, projection, selection, selectionArgs, sortOrder);
    }

    public Uri canonicalize(Uri url) {
        return null;
    }

    public Uri uncanonicalize(Uri url) {
        return url;
    }

    public Uri rejectInsert(Uri uri, ContentValues values) {
        return uri.buildUpon().appendPath(WifiEnterpriseConfig.ENGINE_DISABLE).build();
    }

    public int bulkInsert(Uri uri, ContentValues[] values) {
        int numValues = values.length;
        for (ContentValues contentValues : values) {
            insert(uri, contentValues);
        }
        return numValues;
    }

    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        throw new FileNotFoundException("No files supported by provider at " + uri);
    }

    public ParcelFileDescriptor openFile(Uri uri, String mode, CancellationSignal signal) throws FileNotFoundException {
        return openFile(uri, mode);
    }

    public AssetFileDescriptor openAssetFile(Uri uri, String mode) throws FileNotFoundException {
        ParcelFileDescriptor fd = openFile(uri, mode);
        if (fd != null) {
            return new AssetFileDescriptor(fd, 0, -1);
        }
        return null;
    }

    public AssetFileDescriptor openAssetFile(Uri uri, String mode, CancellationSignal signal) throws FileNotFoundException {
        return openAssetFile(uri, mode);
    }

    /* access modifiers changed from: protected */
    public final ParcelFileDescriptor openFileHelper(Uri uri, String mode) throws FileNotFoundException {
        String path;
        int count = 0;
        Cursor c = query(uri, new String[]{"_data"}, null, null, null);
        if (c != null) {
            count = c.getCount();
        }
        if (count != 1) {
            if (c != null) {
                c.close();
            }
            if (count == 0) {
                throw new FileNotFoundException("No entry for " + uri);
            }
            throw new FileNotFoundException("Multiple items at " + uri);
        }
        c.moveToFirst();
        int i = c.getColumnIndex("_data");
        if (i >= 0) {
            path = c.getString(i);
        } else {
            path = null;
        }
        c.close();
        if (path == null) {
            throw new FileNotFoundException("Column _data not found.");
        }
        return ParcelFileDescriptor.open(new File(path), ParcelFileDescriptor.parseMode(mode));
    }

    public String[] getStreamTypes(Uri uri, String mimeTypeFilter) {
        return null;
    }

    public AssetFileDescriptor openTypedAssetFile(Uri uri, String mimeTypeFilter, Bundle opts) throws FileNotFoundException {
        if ("*/*".equals(mimeTypeFilter)) {
            return openAssetFile(uri, FullBackup.ROOT_TREE_TOKEN);
        }
        String baseType = getType(uri);
        if (baseType != null && ClipDescription.compareMimeTypes(baseType, mimeTypeFilter)) {
            return openAssetFile(uri, FullBackup.ROOT_TREE_TOKEN);
        }
        throw new FileNotFoundException("Can't open " + uri + " as type " + mimeTypeFilter);
    }

    public AssetFileDescriptor openTypedAssetFile(Uri uri, String mimeTypeFilter, Bundle opts, CancellationSignal signal) throws FileNotFoundException {
        return openTypedAssetFile(uri, mimeTypeFilter, opts);
    }

    public <T> ParcelFileDescriptor openPipeHelper(final Uri uri, final String mimeType, final Bundle opts, final T args, final PipeDataWriter<T> func) throws FileNotFoundException {
        try {
            final ParcelFileDescriptor[] fds = ParcelFileDescriptor.createPipe();
            new AsyncTask<Object, Object, Object>() {
                /* class android.content.ContentProvider.AnonymousClass1 */

                /* access modifiers changed from: protected */
                @Override // android.os.AsyncTask
                public Object doInBackground(Object... params) {
                    func.writeDataToPipe(fds[1], uri, mimeType, opts, args);
                    try {
                        fds[1].close();
                        return null;
                    } catch (IOException e) {
                        Log.w(ContentProvider.TAG, "Failure closing pipe", e);
                        return null;
                    }
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
            return fds[0];
        } catch (IOException e) {
            throw new FileNotFoundException("failure making pipe");
        }
    }

    /* access modifiers changed from: protected */
    public boolean isTemporary() {
        return false;
    }

    public IContentProvider getIContentProvider() {
        return this.mTransport;
    }

    public void attachInfoForTesting(Context context, ProviderInfo info) {
        attachInfo(context, info, true);
    }

    public void attachInfo(Context context, ProviderInfo info) {
        attachInfo(context, info, false);
    }

    private void attachInfo(Context context, ProviderInfo info, boolean testing) {
        AsyncTask.init();
        this.mNoPerms = testing;
        if (this.mContext == null) {
            this.mContext = context;
            if (context != null) {
                this.mTransport.mAppOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            }
            this.mMyUid = Process.myUid();
            if (info != null) {
                setReadPermission(info.readPermission);
                setWritePermission(info.writePermission);
                setPathPermissions(info.pathPermissions);
                this.mExported = info.exported;
                this.mSingleUser = (info.flags & 1073741824) != 0;
                setAuthorities(info.authority);
            }
            onCreate();
        }
    }

    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations) throws OperationApplicationException {
        int numOperations = operations.size();
        ContentProviderResult[] results = new ContentProviderResult[numOperations];
        for (int i = 0; i < numOperations; i++) {
            results[i] = operations.get(i).apply(this, results, i);
        }
        return results;
    }

    public Bundle call(String method, String arg, Bundle extras) {
        return null;
    }

    public void shutdown() {
        Log.w(TAG, "implement ContentProvider shutdown() to make sure all database connections are gracefully shutdown");
    }

    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        writer.println("nothing to dump");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void validateIncomingUri(Uri uri) throws SecurityException {
        String message;
        String auth = uri.getAuthority();
        int userId = getUserIdFromAuthority(auth, -2);
        if (userId != -2 && userId != this.mContext.getUserId()) {
            throw new SecurityException("trying to query a ContentProvider in user " + this.mContext.getUserId() + " with a uri belonging to user " + userId);
        } else if (!matchesOurAuthorities(getAuthorityWithoutUserId(auth))) {
            String message2 = "The authority of the uri " + uri + " does not match the one of the " + "contentProvider: ";
            if (this.mAuthority != null) {
                message = message2 + this.mAuthority;
            } else {
                message = message2 + this.mAuthorities;
            }
            throw new SecurityException(message);
        }
    }

    public static int getUserIdFromAuthority(String auth, int defaultUserId) {
        int end;
        if (auth == null || (end = auth.lastIndexOf(64)) == -1) {
            return defaultUserId;
        }
        try {
            return Integer.parseInt(auth.substring(0, end));
        } catch (NumberFormatException e) {
            Log.w(TAG, "Error parsing userId.", e);
            return -10000;
        }
    }

    public static int getUserIdFromAuthority(String auth) {
        return getUserIdFromAuthority(auth, -2);
    }

    public static int getUserIdFromUri(Uri uri, int defaultUserId) {
        return uri == null ? defaultUserId : getUserIdFromAuthority(uri.getAuthority(), defaultUserId);
    }

    public static int getUserIdFromUri(Uri uri) {
        return getUserIdFromUri(uri, -2);
    }

    public static String getAuthorityWithoutUserId(String auth) {
        if (auth == null) {
            return null;
        }
        return auth.substring(auth.lastIndexOf(64) + 1);
    }

    public static Uri getUriWithoutUserId(Uri uri) {
        if (uri == null) {
            return null;
        }
        Uri.Builder builder = uri.buildUpon();
        builder.authority(getAuthorityWithoutUserId(uri.getAuthority()));
        return builder.build();
    }

    public static boolean uriHasUserId(Uri uri) {
        if (uri != null && !TextUtils.isEmpty(uri.getUserInfo())) {
            return true;
        }
        return false;
    }

    public static Uri maybeAddUserId(Uri uri, int userId) {
        if (uri == null) {
            return null;
        }
        if (userId == -2 || !"content".equals(uri.getScheme()) || uriHasUserId(uri)) {
            return uri;
        }
        Uri.Builder builder = uri.buildUpon();
        builder.encodedAuthority(ProxyInfo.LOCAL_EXCL_LIST + userId + "@" + uri.getEncodedAuthority());
        return builder.build();
    }
}

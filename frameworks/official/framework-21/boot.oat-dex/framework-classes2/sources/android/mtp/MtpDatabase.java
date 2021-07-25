package android.mtp;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.IContentProvider;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaScanner;
import android.net.Uri;
import android.os.Bundle;
import android.os.ICancellationSignal;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ExpandableListView;
import com.android.internal.R;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

public class MtpDatabase {
    static final int[] ALL_PROPERTIES = {MtpConstants.PROPERTY_STORAGE_ID, MtpConstants.PROPERTY_OBJECT_FORMAT, MtpConstants.PROPERTY_PROTECTION_STATUS, MtpConstants.PROPERTY_OBJECT_SIZE, MtpConstants.PROPERTY_OBJECT_FILE_NAME, MtpConstants.PROPERTY_DATE_MODIFIED, MtpConstants.PROPERTY_PARENT_OBJECT, MtpConstants.PROPERTY_PERSISTENT_UID, MtpConstants.PROPERTY_NAME, MtpConstants.PROPERTY_DISPLAY_NAME, MtpConstants.PROPERTY_DATE_ADDED, MtpConstants.PROPERTY_DESCRIPTION, MtpConstants.PROPERTY_ARTIST, MtpConstants.PROPERTY_ALBUM_NAME, MtpConstants.PROPERTY_ALBUM_ARTIST, MtpConstants.PROPERTY_TRACK, MtpConstants.PROPERTY_ORIGINAL_RELEASE_DATE, MtpConstants.PROPERTY_DURATION, MtpConstants.PROPERTY_GENRE, MtpConstants.PROPERTY_COMPOSER, MtpConstants.PROPERTY_ARTIST, MtpConstants.PROPERTY_ALBUM_NAME, MtpConstants.PROPERTY_DURATION, MtpConstants.PROPERTY_DESCRIPTION, MtpConstants.PROPERTY_DESCRIPTION};
    static final int[] AUDIO_PROPERTIES = {MtpConstants.PROPERTY_STORAGE_ID, MtpConstants.PROPERTY_OBJECT_FORMAT, MtpConstants.PROPERTY_PROTECTION_STATUS, MtpConstants.PROPERTY_OBJECT_SIZE, MtpConstants.PROPERTY_OBJECT_FILE_NAME, MtpConstants.PROPERTY_DATE_MODIFIED, MtpConstants.PROPERTY_PARENT_OBJECT, MtpConstants.PROPERTY_PERSISTENT_UID, MtpConstants.PROPERTY_NAME, MtpConstants.PROPERTY_DISPLAY_NAME, MtpConstants.PROPERTY_DATE_ADDED, MtpConstants.PROPERTY_ARTIST, MtpConstants.PROPERTY_ALBUM_NAME, MtpConstants.PROPERTY_ALBUM_ARTIST, MtpConstants.PROPERTY_TRACK, MtpConstants.PROPERTY_ORIGINAL_RELEASE_DATE, MtpConstants.PROPERTY_DURATION, MtpConstants.PROPERTY_GENRE, MtpConstants.PROPERTY_COMPOSER, MtpConstants.PROPERTY_AUDIO_WAVE_CODEC, MtpConstants.PROPERTY_BITRATE_TYPE, MtpConstants.PROPERTY_AUDIO_BITRATE, MtpConstants.PROPERTY_NUMBER_OF_CHANNELS, MtpConstants.PROPERTY_SAMPLE_RATE};
    private static final int DEVICE_PROPERTIES_DATABASE_VERSION = 1;
    static final int[] FILE_PROPERTIES = {MtpConstants.PROPERTY_STORAGE_ID, MtpConstants.PROPERTY_OBJECT_FORMAT, MtpConstants.PROPERTY_PROTECTION_STATUS, MtpConstants.PROPERTY_OBJECT_SIZE, MtpConstants.PROPERTY_OBJECT_FILE_NAME, MtpConstants.PROPERTY_DATE_MODIFIED, MtpConstants.PROPERTY_PARENT_OBJECT, MtpConstants.PROPERTY_PERSISTENT_UID, MtpConstants.PROPERTY_NAME, MtpConstants.PROPERTY_DATE_ADDED};
    private static final String FORMAT_PARENT_WHERE = "format=? AND parent=?";
    private static final String FORMAT_WHERE = "format=?";
    private static final String[] ID_PROJECTION = {"_id"};
    private static final String ID_WHERE = "_id=?";
    static final int[] IMAGE_PROPERTIES = {MtpConstants.PROPERTY_STORAGE_ID, MtpConstants.PROPERTY_OBJECT_FORMAT, MtpConstants.PROPERTY_PROTECTION_STATUS, MtpConstants.PROPERTY_OBJECT_SIZE, MtpConstants.PROPERTY_OBJECT_FILE_NAME, MtpConstants.PROPERTY_DATE_MODIFIED, MtpConstants.PROPERTY_PARENT_OBJECT, MtpConstants.PROPERTY_PERSISTENT_UID, MtpConstants.PROPERTY_NAME, MtpConstants.PROPERTY_DISPLAY_NAME, MtpConstants.PROPERTY_DATE_ADDED, MtpConstants.PROPERTY_DESCRIPTION};
    private static final String[] OBJECT_INFO_PROJECTION = {"_id", "storage_id", "format", "parent", "_data", "date_added", "date_modified"};
    private static final String PARENT_WHERE = "parent=?";
    private static final String[] PATH_FORMAT_PROJECTION = {"_id", "_data", "format"};
    private static final String[] PATH_PROJECTION = {"_id", "_data"};
    private static final String PATH_WHERE = "_data=?";
    private static final String STORAGE_FORMAT_PARENT_WHERE = "storage_id=? AND format=? AND parent=?";
    private static final String STORAGE_FORMAT_WHERE = "storage_id=? AND format=?";
    private static final String STORAGE_PARENT_WHERE = "storage_id=? AND parent=?";
    private static final String STORAGE_WHERE = "storage_id=?";
    private static final String TAG = "MtpDatabase";
    static final int[] VIDEO_PROPERTIES = {MtpConstants.PROPERTY_STORAGE_ID, MtpConstants.PROPERTY_OBJECT_FORMAT, MtpConstants.PROPERTY_PROTECTION_STATUS, MtpConstants.PROPERTY_OBJECT_SIZE, MtpConstants.PROPERTY_OBJECT_FILE_NAME, MtpConstants.PROPERTY_DATE_MODIFIED, MtpConstants.PROPERTY_PARENT_OBJECT, MtpConstants.PROPERTY_PERSISTENT_UID, MtpConstants.PROPERTY_NAME, MtpConstants.PROPERTY_DISPLAY_NAME, MtpConstants.PROPERTY_DATE_ADDED, MtpConstants.PROPERTY_ARTIST, MtpConstants.PROPERTY_ALBUM_NAME, MtpConstants.PROPERTY_DURATION, MtpConstants.PROPERTY_DESCRIPTION};
    private int mBatteryLevel;
    private BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
        /* class android.mtp.MtpDatabase.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.BATTERY_CHANGED")) {
                MtpDatabase.this.mBatteryScale = intent.getIntExtra("scale", 0);
                int newLevel = intent.getIntExtra("level", 0);
                if (newLevel != MtpDatabase.this.mBatteryLevel) {
                    MtpDatabase.this.mBatteryLevel = newLevel;
                    if (MtpDatabase.this.mServer != null) {
                        MtpDatabase.this.mServer.sendDevicePropertyChanged(MtpConstants.DEVICE_PROPERTY_BATTERY_LEVEL);
                    }
                }
            }
        }
    };
    private int mBatteryScale;
    private final Context mContext;
    private boolean mDatabaseModified;
    private SharedPreferences mDeviceProperties;
    private final IContentProvider mMediaProvider;
    private final MediaScanner mMediaScanner;
    private final String mMediaStoragePath;
    private long mNativeContext;
    private final Uri mObjectsUri;
    private final String mPackageName;
    private final HashMap<Integer, MtpPropertyGroup> mPropertyGroupsByFormat = new HashMap<>();
    private final HashMap<Integer, MtpPropertyGroup> mPropertyGroupsByProperty = new HashMap<>();
    private MtpServer mServer;
    private final HashMap<String, MtpStorage> mStorageMap = new HashMap<>();
    private final String[] mSubDirectories;
    private String mSubDirectoriesWhere;
    private String[] mSubDirectoriesWhereArgs;
    private final String mVolumeName;

    private final native void native_finalize();

    private final native void native_setup();

    static {
        System.loadLibrary("media_jni");
    }

    public MtpDatabase(Context context, String volumeName, String storagePath, String[] subDirectories) {
        native_setup();
        this.mContext = context;
        this.mPackageName = context.getPackageName();
        this.mMediaProvider = context.getContentResolver().acquireProvider("media");
        this.mVolumeName = volumeName;
        this.mMediaStoragePath = storagePath;
        this.mObjectsUri = MediaStore.Files.getMtpObjectsUri(volumeName);
        this.mMediaScanner = new MediaScanner(context);
        this.mSubDirectories = subDirectories;
        if (subDirectories != null) {
            StringBuilder builder = new StringBuilder();
            builder.append("(");
            int count = subDirectories.length;
            for (int i = 0; i < count; i++) {
                builder.append("_data=? OR _data LIKE ?");
                if (i != count - 1) {
                    builder.append(" OR ");
                }
            }
            builder.append(")");
            this.mSubDirectoriesWhere = builder.toString();
            this.mSubDirectoriesWhereArgs = new String[(count * 2)];
            int j = 0;
            for (String path : subDirectories) {
                int j2 = j + 1;
                this.mSubDirectoriesWhereArgs[j] = path;
                j = j2 + 1;
                this.mSubDirectoriesWhereArgs[j2] = path + "/%";
            }
        }
        Locale locale = context.getResources().getConfiguration().locale;
        if (locale != null) {
            String language = locale.getLanguage();
            String country = locale.getCountry();
            if (language != null) {
                if (country != null) {
                    this.mMediaScanner.setLocale(language + "_" + country);
                } else {
                    this.mMediaScanner.setLocale(language);
                }
            }
        }
        initDeviceProperties(context);
    }

    public void setServer(MtpServer server) {
        this.mServer = server;
        try {
            this.mContext.unregisterReceiver(this.mBatteryReceiver);
        } catch (IllegalArgumentException e) {
        }
        if (server != null) {
            this.mContext.registerReceiver(this.mBatteryReceiver, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            native_finalize();
        } finally {
            super.finalize();
        }
    }

    public void addStorage(MtpStorage storage) {
        this.mStorageMap.put(storage.getPath(), storage);
    }

    public void removeStorage(MtpStorage storage) {
        this.mStorageMap.remove(storage.getPath());
    }

    private void initDeviceProperties(Context context) {
        this.mDeviceProperties = context.getSharedPreferences("device-properties", 0);
        if (context.getDatabasePath("device-properties").exists()) {
            SQLiteDatabase db = null;
            Cursor c = null;
            try {
                SQLiteDatabase db2 = context.openOrCreateDatabase("device-properties", 0, null);
                if (!(db2 == null || (c = db2.query("properties", new String[]{"_id", "code", "value"}, null, null, null, null, null)) == null)) {
                    SharedPreferences.Editor e = this.mDeviceProperties.edit();
                    while (c.moveToNext()) {
                        e.putString(c.getString(1), c.getString(2));
                    }
                    e.commit();
                }
                if (c != null) {
                    c.close();
                }
                if (db2 != null) {
                    db2.close();
                }
            } catch (Exception e2) {
                Log.e(TAG, "failed to migrate device properties", e2);
                if (0 != 0) {
                    c.close();
                }
                if (0 != 0) {
                    db.close();
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    c.close();
                }
                if (0 != 0) {
                    db.close();
                }
                throw th;
            }
            context.deleteDatabase("device-properties");
        }
    }

    private boolean inStorageSubDirectory(String path) {
        if (this.mSubDirectories == null) {
            return true;
        }
        if (path == null) {
            return false;
        }
        boolean allowed = false;
        int pathLength = path.length();
        for (int i = 0; i < this.mSubDirectories.length && !allowed; i++) {
            String subdir = this.mSubDirectories[i];
            int subdirLength = subdir.length();
            if (subdirLength < pathLength && path.charAt(subdirLength) == '/' && path.startsWith(subdir)) {
                allowed = true;
            }
        }
        return allowed;
    }

    private boolean isStorageSubDirectory(String path) {
        if (this.mSubDirectories == null) {
            return false;
        }
        for (int i = 0; i < this.mSubDirectories.length; i++) {
            if (path.equals(this.mSubDirectories[i])) {
                return true;
            }
        }
        return false;
    }

    private boolean inStorageRoot(String path) {
        try {
            String canonical = new File(path).getCanonicalPath();
            for (String root : this.mStorageMap.keySet()) {
                if (canonical.startsWith(root)) {
                    return true;
                }
            }
        } catch (IOException e) {
        }
        return false;
    }

    private int beginSendObject(String path, int format, int parent, int storageId, long size, long modified) {
        if (!inStorageRoot(path)) {
            Log.e(TAG, "attempt to put file outside of storage area: " + path);
            return -1;
        } else if (!inStorageSubDirectory(path)) {
            return -1;
        } else {
            if (path != null) {
                Cursor c = null;
                try {
                    Cursor c2 = this.mMediaProvider.query(this.mPackageName, this.mObjectsUri, ID_PROJECTION, PATH_WHERE, new String[]{path}, (String) null, (ICancellationSignal) null);
                    if (c2 != null && c2.getCount() > 0) {
                        Log.w(TAG, "file already exists in beginSendObject: " + path);
                        if (c2 == null) {
                            return -1;
                        }
                        c2.close();
                        return -1;
                    } else if (c2 != null) {
                        c2.close();
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException in beginSendObject", e);
                    if (0 != 0) {
                        c.close();
                    }
                } catch (Throwable th) {
                    if (0 != 0) {
                        c.close();
                    }
                    throw th;
                }
            }
            this.mDatabaseModified = true;
            ContentValues values = new ContentValues();
            values.put("_data", path);
            values.put("format", Integer.valueOf(format));
            values.put("parent", Integer.valueOf(parent));
            values.put("storage_id", Integer.valueOf(storageId));
            values.put("_size", Long.valueOf(size));
            values.put("date_modified", Long.valueOf(modified));
            try {
                Uri uri = this.mMediaProvider.insert(this.mPackageName, this.mObjectsUri, values);
                if (uri != null) {
                    return Integer.parseInt(uri.getPathSegments().get(2));
                }
                return -1;
            } catch (RemoteException e2) {
                Log.e(TAG, "RemoteException in beginSendObject", e2);
                return -1;
            }
        }
    }

    private void endSendObject(String path, int handle, int format, boolean succeeded) {
        if (!succeeded) {
            deleteFile(handle);
        } else if (format == 47621) {
            String name = path;
            int lastSlash = name.lastIndexOf(47);
            if (lastSlash >= 0) {
                name = name.substring(lastSlash + 1);
            }
            if (name.endsWith(".pla")) {
                name = name.substring(0, name.length() - 4);
            }
            ContentValues values = new ContentValues(1);
            values.put("_data", path);
            values.put("name", name);
            values.put("format", Integer.valueOf(format));
            values.put("date_modified", Long.valueOf(System.currentTimeMillis() / 1000));
            values.put("media_scanner_new_object_id", Integer.valueOf(handle));
            try {
                this.mMediaProvider.insert(this.mPackageName, MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, values);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException in endSendObject", e);
            }
        } else {
            this.mMediaScanner.scanMtpFile(path, this.mVolumeName, handle, format);
        }
    }

    private Cursor createObjectQuery(int storageID, int format, int parent) throws RemoteException {
        String where;
        String[] whereArgs;
        if (storageID == -1) {
            if (format == 0) {
                if (parent == 0) {
                    where = null;
                    whereArgs = null;
                } else {
                    if (parent == -1) {
                        parent = 0;
                    }
                    where = PARENT_WHERE;
                    whereArgs = new String[]{Integer.toString(parent)};
                }
            } else if (parent == 0) {
                where = FORMAT_WHERE;
                whereArgs = new String[]{Integer.toString(format)};
            } else {
                if (parent == -1) {
                    parent = 0;
                }
                where = FORMAT_PARENT_WHERE;
                whereArgs = new String[]{Integer.toString(format), Integer.toString(parent)};
            }
        } else if (format == 0) {
            if (parent == 0) {
                where = STORAGE_WHERE;
                whereArgs = new String[]{Integer.toString(storageID)};
            } else {
                if (parent == -1) {
                    parent = 0;
                }
                where = STORAGE_PARENT_WHERE;
                whereArgs = new String[]{Integer.toString(storageID), Integer.toString(parent)};
            }
        } else if (parent == 0) {
            where = STORAGE_FORMAT_WHERE;
            whereArgs = new String[]{Integer.toString(storageID), Integer.toString(format)};
        } else {
            if (parent == -1) {
                parent = 0;
            }
            where = STORAGE_FORMAT_PARENT_WHERE;
            whereArgs = new String[]{Integer.toString(storageID), Integer.toString(format), Integer.toString(parent)};
        }
        if (this.mSubDirectoriesWhere != null) {
            if (where == null) {
                where = this.mSubDirectoriesWhere;
                whereArgs = this.mSubDirectoriesWhereArgs;
            } else {
                where = where + " AND " + this.mSubDirectoriesWhere;
                String[] newWhereArgs = new String[(whereArgs.length + this.mSubDirectoriesWhereArgs.length)];
                int i = 0;
                while (i < whereArgs.length) {
                    newWhereArgs[i] = whereArgs[i];
                    i++;
                }
                for (int j = 0; j < this.mSubDirectoriesWhereArgs.length; j++) {
                    newWhereArgs[i] = this.mSubDirectoriesWhereArgs[j];
                    i++;
                }
                whereArgs = newWhereArgs;
            }
        }
        return this.mMediaProvider.query(this.mPackageName, this.mObjectsUri, ID_PROJECTION, where, whereArgs, (String) null, (ICancellationSignal) null);
    }

    private int[] getObjectList(int storageID, int format, int parent) {
        Cursor c = null;
        try {
            Cursor c2 = createObjectQuery(storageID, format, parent);
            if (c2 == null) {
                if (c2 != null) {
                    c2.close();
                }
                return null;
            }
            int count = c2.getCount();
            if (count > 0) {
                int[] result = new int[count];
                for (int i = 0; i < count; i++) {
                    c2.moveToNext();
                    result[i] = c2.getInt(0);
                }
                if (c2 == null) {
                    return result;
                }
                c2.close();
                return result;
            }
            if (c2 != null) {
                c2.close();
            }
            return null;
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in getObjectList", e);
            if (0 != 0) {
                c.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                c.close();
            }
            throw th;
        }
    }

    private int getNumObjects(int storageID, int format, int parent) {
        Cursor c = null;
        try {
            Cursor c2 = createObjectQuery(storageID, format, parent);
            if (c2 != null) {
                int count = c2.getCount();
                if (c2 == null) {
                    return count;
                }
                c2.close();
                return count;
            }
            if (c2 != null) {
                c2.close();
            }
            return -1;
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in getNumObjects", e);
            if (0 != 0) {
                c.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                c.close();
            }
            throw th;
        }
    }

    private int[] getSupportedPlaybackFormats() {
        return new int[]{12288, 12289, 12292, 12293, 12296, 12297, 12299, MtpConstants.FORMAT_EXIF_JPEG, MtpConstants.FORMAT_TIFF_EP, MtpConstants.FORMAT_BMP, MtpConstants.FORMAT_GIF, MtpConstants.FORMAT_JFIF, MtpConstants.FORMAT_PNG, MtpConstants.FORMAT_TIFF, MtpConstants.FORMAT_WMA, MtpConstants.FORMAT_OGG, MtpConstants.FORMAT_AAC, MtpConstants.FORMAT_MP4_CONTAINER, MtpConstants.FORMAT_MP2, MtpConstants.FORMAT_3GP_CONTAINER, MtpConstants.FORMAT_ABSTRACT_AV_PLAYLIST, MtpConstants.FORMAT_WPL_PLAYLIST, MtpConstants.FORMAT_M3U_PLAYLIST, MtpConstants.FORMAT_PLS_PLAYLIST, MtpConstants.FORMAT_XML_DOCUMENT, MtpConstants.FORMAT_FLAC};
    }

    private int[] getSupportedCaptureFormats() {
        return null;
    }

    private int[] getSupportedObjectProperties(int format) {
        switch (format) {
            case 0:
                return ALL_PROPERTIES;
            case 12296:
            case 12297:
            case MtpConstants.FORMAT_WMA:
            case MtpConstants.FORMAT_OGG:
            case MtpConstants.FORMAT_AAC:
                return AUDIO_PROPERTIES;
            case 12299:
            case MtpConstants.FORMAT_WMV:
            case MtpConstants.FORMAT_3GP_CONTAINER:
                return VIDEO_PROPERTIES;
            case MtpConstants.FORMAT_EXIF_JPEG:
            case MtpConstants.FORMAT_BMP:
            case MtpConstants.FORMAT_GIF:
            case MtpConstants.FORMAT_PNG:
                return IMAGE_PROPERTIES;
            default:
                return FILE_PROPERTIES;
        }
    }

    private int[] getSupportedDeviceProperties() {
        return new int[]{MtpConstants.DEVICE_PROPERTY_SYNCHRONIZATION_PARTNER, MtpConstants.DEVICE_PROPERTY_DEVICE_FRIENDLY_NAME, MtpConstants.DEVICE_PROPERTY_IMAGE_SIZE, MtpConstants.DEVICE_PROPERTY_BATTERY_LEVEL};
    }

    private MtpPropertyList getObjectPropertyList(long handle, int format, long property, int groupCode, int depth) {
        MtpPropertyGroup propertyGroup;
        if (groupCode != 0) {
            return new MtpPropertyList(0, MtpConstants.RESPONSE_SPECIFICATION_BY_GROUP_UNSUPPORTED);
        }
        if (property == ExpandableListView.PACKED_POSITION_VALUE_NULL) {
            propertyGroup = this.mPropertyGroupsByFormat.get(Integer.valueOf(format));
            if (propertyGroup == null) {
                propertyGroup = new MtpPropertyGroup(this, this.mMediaProvider, this.mPackageName, this.mVolumeName, getSupportedObjectProperties(format));
                this.mPropertyGroupsByFormat.put(new Integer(format), propertyGroup);
            }
        } else {
            propertyGroup = this.mPropertyGroupsByProperty.get(Long.valueOf(property));
            if (propertyGroup == null) {
                propertyGroup = new MtpPropertyGroup(this, this.mMediaProvider, this.mPackageName, this.mVolumeName, new int[]{(int) property});
                this.mPropertyGroupsByProperty.put(new Integer((int) property), propertyGroup);
            }
        }
        return propertyGroup.getPropertyList((int) handle, format, depth);
    }

    private int renameFile(int handle, String newName) {
        Cursor c = null;
        String path = null;
        String[] whereArgs = {Integer.toString(handle)};
        try {
            Cursor c2 = this.mMediaProvider.query(this.mPackageName, this.mObjectsUri, PATH_PROJECTION, ID_WHERE, whereArgs, (String) null, (ICancellationSignal) null);
            if (c2 != null && c2.moveToNext()) {
                path = c2.getString(1);
            }
            if (c2 != null) {
                c2.close();
            }
            if (path == null) {
                return MtpConstants.RESPONSE_INVALID_OBJECT_HANDLE;
            }
            if (isStorageSubDirectory(path)) {
                return MtpConstants.RESPONSE_OBJECT_WRITE_PROTECTED;
            }
            File oldFile = new File(path);
            int lastSlash = path.lastIndexOf(47);
            if (lastSlash <= 1) {
                return MtpConstants.RESPONSE_GENERAL_ERROR;
            }
            String newPath = path.substring(0, lastSlash + 1) + newName;
            File newFile = new File(newPath);
            if (!oldFile.renameTo(newFile)) {
                Log.w(TAG, "renaming " + path + " to " + newPath + " failed");
                return MtpConstants.RESPONSE_GENERAL_ERROR;
            }
            ContentValues values = new ContentValues();
            values.put("_data", newPath);
            int updated = 0;
            try {
                updated = this.mMediaProvider.update(this.mPackageName, this.mObjectsUri, values, ID_WHERE, whereArgs);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException in mMediaProvider.update", e);
            }
            if (updated == 0) {
                Log.e(TAG, "Unable to update path for " + path + " to " + newPath);
                newFile.renameTo(oldFile);
                return MtpConstants.RESPONSE_GENERAL_ERROR;
            }
            if (newFile.isDirectory()) {
                if (oldFile.getName().startsWith(".") && !newPath.startsWith(".")) {
                    try {
                        this.mMediaProvider.call(this.mPackageName, "unhide", newPath, (Bundle) null);
                    } catch (RemoteException e2) {
                        Log.e(TAG, "failed to unhide/rescan for " + newPath);
                    }
                }
            } else if (oldFile.getName().toLowerCase(Locale.US).equals(".nomedia") && !newPath.toLowerCase(Locale.US).equals(".nomedia")) {
                try {
                    this.mMediaProvider.call(this.mPackageName, "unhide", oldFile.getParent(), (Bundle) null);
                } catch (RemoteException e3) {
                    Log.e(TAG, "failed to unhide/rescan for " + newPath);
                }
            }
            return MtpConstants.RESPONSE_OK;
        } catch (RemoteException e4) {
            Log.e(TAG, "RemoteException in getObjectFilePath", e4);
            if (0 == 0) {
                return MtpConstants.RESPONSE_GENERAL_ERROR;
            }
            c.close();
            return MtpConstants.RESPONSE_GENERAL_ERROR;
        } catch (Throwable th) {
            if (0 != 0) {
                c.close();
            }
            throw th;
        }
    }

    private int setObjectProperty(int handle, int property, long intValue, String stringValue) {
        switch (property) {
            case MtpConstants.PROPERTY_OBJECT_FILE_NAME:
                return renameFile(handle, stringValue);
            default:
                return MtpConstants.RESPONSE_OBJECT_PROP_NOT_SUPPORTED;
        }
    }

    private int getDeviceProperty(int property, long[] outIntValue, char[] outStringValue) {
        switch (property) {
            case MtpConstants.DEVICE_PROPERTY_IMAGE_SIZE:
                Display display = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
                String imageSize = Integer.toString(display.getMaximumSizeDimension()) + "x" + Integer.toString(display.getMaximumSizeDimension());
                imageSize.getChars(0, imageSize.length(), outStringValue, 0);
                outStringValue[imageSize.length()] = 0;
                return MtpConstants.RESPONSE_OK;
            case MtpConstants.DEVICE_PROPERTY_SYNCHRONIZATION_PARTNER:
            case MtpConstants.DEVICE_PROPERTY_DEVICE_FRIENDLY_NAME:
                String value = this.mDeviceProperties.getString(Integer.toString(property), "");
                int length = value.length();
                if (length > 255) {
                    length = R.styleable.Theme_actionBarTheme;
                }
                value.getChars(0, length, outStringValue, 0);
                outStringValue[length] = 0;
                return MtpConstants.RESPONSE_OK;
            default:
                return MtpConstants.RESPONSE_DEVICE_PROP_NOT_SUPPORTED;
        }
    }

    private int setDeviceProperty(int property, long intValue, String stringValue) {
        switch (property) {
            case MtpConstants.DEVICE_PROPERTY_SYNCHRONIZATION_PARTNER:
            case MtpConstants.DEVICE_PROPERTY_DEVICE_FRIENDLY_NAME:
                SharedPreferences.Editor e = this.mDeviceProperties.edit();
                e.putString(Integer.toString(property), stringValue);
                return e.commit() ? MtpConstants.RESPONSE_OK : MtpConstants.RESPONSE_GENERAL_ERROR;
            default:
                return MtpConstants.RESPONSE_DEVICE_PROP_NOT_SUPPORTED;
        }
    }

    private boolean getObjectInfo(int handle, int[] outStorageFormatParent, char[] outName, long[] outCreatedModified) {
        Cursor c = null;
        try {
            Cursor c2 = this.mMediaProvider.query(this.mPackageName, this.mObjectsUri, OBJECT_INFO_PROJECTION, ID_WHERE, new String[]{Integer.toString(handle)}, (String) null, (ICancellationSignal) null);
            if (c2 == null || !c2.moveToNext()) {
                if (c2 != null) {
                    c2.close();
                }
                return false;
            }
            outStorageFormatParent[0] = c2.getInt(1);
            outStorageFormatParent[1] = c2.getInt(2);
            outStorageFormatParent[2] = c2.getInt(3);
            String path = c2.getString(4);
            int lastSlash = path.lastIndexOf(47);
            int start = lastSlash >= 0 ? lastSlash + 1 : 0;
            int end = path.length();
            if (end - start > 255) {
                end = start + R.styleable.Theme_actionBarTheme;
            }
            path.getChars(start, end, outName, 0);
            outName[end - start] = 0;
            outCreatedModified[0] = c2.getLong(5);
            outCreatedModified[1] = c2.getLong(6);
            if (outCreatedModified[0] == 0) {
                outCreatedModified[0] = outCreatedModified[1];
            }
            if (c2 == null) {
                return true;
            }
            c2.close();
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in getObjectInfo", e);
            if (0 != 0) {
                c.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                c.close();
            }
            throw th;
        }
    }

    private int getObjectFilePath(int handle, char[] outFilePath, long[] outFileLengthFormat) {
        if (handle == 0) {
            this.mMediaStoragePath.getChars(0, this.mMediaStoragePath.length(), outFilePath, 0);
            outFilePath[this.mMediaStoragePath.length()] = 0;
            outFileLengthFormat[0] = 0;
            outFileLengthFormat[1] = 12289;
            return MtpConstants.RESPONSE_OK;
        }
        Cursor c = null;
        try {
            Cursor c2 = this.mMediaProvider.query(this.mPackageName, this.mObjectsUri, PATH_FORMAT_PROJECTION, ID_WHERE, new String[]{Integer.toString(handle)}, (String) null, (ICancellationSignal) null);
            if (c2 != null && c2.moveToNext()) {
                String path = c2.getString(1);
                path.getChars(0, path.length(), outFilePath, 0);
                outFilePath[path.length()] = 0;
                outFileLengthFormat[0] = new File(path).length();
                outFileLengthFormat[1] = c2.getLong(2);
                if (c2 != null) {
                    c2.close();
                }
                return MtpConstants.RESPONSE_OK;
            } else if (c2 == null) {
                return MtpConstants.RESPONSE_INVALID_OBJECT_HANDLE;
            } else {
                c2.close();
                return MtpConstants.RESPONSE_INVALID_OBJECT_HANDLE;
            }
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in getObjectFilePath", e);
            if (0 == 0) {
                return MtpConstants.RESPONSE_GENERAL_ERROR;
            }
            c.close();
            return MtpConstants.RESPONSE_GENERAL_ERROR;
        } catch (Throwable th) {
            if (0 != 0) {
                c.close();
            }
            throw th;
        }
    }

    private int deleteFile(int handle) {
        int i;
        this.mDatabaseModified = true;
        Cursor c = null;
        try {
            Cursor c2 = this.mMediaProvider.query(this.mPackageName, this.mObjectsUri, PATH_FORMAT_PROJECTION, ID_WHERE, new String[]{Integer.toString(handle)}, (String) null, (ICancellationSignal) null);
            if (c2 == null || !c2.moveToNext()) {
                i = MtpConstants.RESPONSE_INVALID_OBJECT_HANDLE;
                if (c2 != null) {
                    c2.close();
                }
                return i;
            }
            String path = c2.getString(1);
            int format = c2.getInt(2);
            if (path == null || format == 0) {
                i = MtpConstants.RESPONSE_GENERAL_ERROR;
                if (c2 != null) {
                    c2.close();
                }
                return i;
            }
            if (isStorageSubDirectory(path)) {
                i = MtpConstants.RESPONSE_OBJECT_WRITE_PROTECTED;
                if (c2 != null) {
                    c2.close();
                }
            } else {
                if (format == 12289) {
                    Uri uri = MediaStore.Files.getMtpObjectsUri(this.mVolumeName);
                    this.mMediaProvider.delete(this.mPackageName, uri, "_data LIKE ?1 AND lower(substr(_data,1,?2))=lower(?3)", new String[]{path + "/%", Integer.toString(path.length() + 1), path + "/"});
                }
                if (this.mMediaProvider.delete(this.mPackageName, MediaStore.Files.getMtpObjectsUri(this.mVolumeName, (long) handle), (String) null, (String[]) null) > 0) {
                    if (format != 12289 && path.toLowerCase(Locale.US).endsWith("/.nomedia")) {
                        try {
                            this.mMediaProvider.call(this.mPackageName, "unhide", path.substring(0, path.lastIndexOf("/")), (Bundle) null);
                        } catch (RemoteException e) {
                            Log.e(TAG, "failed to unhide/rescan for " + path);
                        }
                    }
                    i = MtpConstants.RESPONSE_OK;
                    if (c2 != null) {
                        c2.close();
                    }
                } else {
                    i = MtpConstants.RESPONSE_INVALID_OBJECT_HANDLE;
                    if (c2 != null) {
                        c2.close();
                    }
                }
            }
            return i;
        } catch (RemoteException e2) {
            Log.e(TAG, "RemoteException in deleteFile", e2);
            i = MtpConstants.RESPONSE_GENERAL_ERROR;
            if (0 != 0) {
                c.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                c.close();
            }
            throw th;
        }
    }

    private int[] getObjectReferences(int handle) {
        Cursor c = null;
        try {
            Cursor c2 = this.mMediaProvider.query(this.mPackageName, MediaStore.Files.getMtpReferencesUri(this.mVolumeName, (long) handle), ID_PROJECTION, (String) null, (String[]) null, (String) null, (ICancellationSignal) null);
            if (c2 == null) {
                if (c2 != null) {
                    c2.close();
                }
                return null;
            }
            int count = c2.getCount();
            if (count > 0) {
                int[] result = new int[count];
                for (int i = 0; i < count; i++) {
                    c2.moveToNext();
                    result[i] = c2.getInt(0);
                }
                if (c2 == null) {
                    return result;
                }
                c2.close();
                return result;
            }
            if (c2 != null) {
                c2.close();
            }
            return null;
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in getObjectList", e);
            if (0 != 0) {
                c.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                c.close();
            }
            throw th;
        }
    }

    private int setObjectReferences(int handle, int[] references) {
        this.mDatabaseModified = true;
        Uri uri = MediaStore.Files.getMtpReferencesUri(this.mVolumeName, (long) handle);
        int count = references.length;
        ContentValues[] valuesList = new ContentValues[count];
        for (int i = 0; i < count; i++) {
            ContentValues values = new ContentValues();
            values.put("_id", Integer.valueOf(references[i]));
            valuesList[i] = values;
        }
        try {
            if (this.mMediaProvider.bulkInsert(this.mPackageName, uri, valuesList) > 0) {
                return MtpConstants.RESPONSE_OK;
            }
            return MtpConstants.RESPONSE_GENERAL_ERROR;
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in setObjectReferences", e);
        }
    }

    private void sessionStarted() {
        this.mDatabaseModified = false;
    }

    private void sessionEnded() {
        if (this.mDatabaseModified) {
            this.mContext.sendBroadcast(new Intent("android.provider.action.MTP_SESSION_END"));
            this.mDatabaseModified = false;
        }
    }
}

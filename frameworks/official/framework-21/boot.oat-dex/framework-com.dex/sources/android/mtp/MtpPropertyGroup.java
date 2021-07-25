package android.mtp;

import android.content.IContentProvider;
import android.database.Cursor;
import android.net.ProxyInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import java.util.ArrayList;

class MtpPropertyGroup {
    private static final String FORMAT_WHERE = "format=?";
    private static final String ID_FORMAT_WHERE = "_id=? AND format=?";
    private static final String ID_WHERE = "_id=?";
    private static final String PARENT_FORMAT_WHERE = "parent=? AND format=?";
    private static final String PARENT_WHERE = "parent=?";
    private static final String TAG = "MtpPropertyGroup";
    private String[] mColumns;
    private final MtpDatabase mDatabase;
    private final String mPackageName;
    private final Property[] mProperties;
    private final IContentProvider mProvider;
    private final Uri mUri;
    private final String mVolumeName;

    private native String format_date_time(long j);

    /* access modifiers changed from: private */
    public class Property {
        int code;
        int column;
        int type;

        Property(int code2, int type2, int column2) {
            this.code = code2;
            this.type = type2;
            this.column = column2;
        }
    }

    public MtpPropertyGroup(MtpDatabase database, IContentProvider provider, String packageName, String volume, int[] properties) {
        this.mDatabase = database;
        this.mProvider = provider;
        this.mPackageName = packageName;
        this.mVolumeName = volume;
        this.mUri = MediaStore.Files.getMtpObjectsUri(volume);
        int count = properties.length;
        ArrayList<String> columns = new ArrayList<>(count);
        columns.add("_id");
        this.mProperties = new Property[count];
        for (int i = 0; i < count; i++) {
            this.mProperties[i] = createProperty(properties[i], columns);
        }
        int count2 = columns.size();
        this.mColumns = new String[count2];
        for (int i2 = 0; i2 < count2; i2++) {
            this.mColumns[i2] = columns.get(i2);
        }
    }

    private Property createProperty(int code, ArrayList<String> columns) {
        int type;
        String column = null;
        switch (code) {
            case MtpConstants.PROPERTY_STORAGE_ID:
                column = MediaStore.Files.FileColumns.STORAGE_ID;
                type = 6;
                break;
            case MtpConstants.PROPERTY_OBJECT_FORMAT:
                column = MediaStore.Files.FileColumns.FORMAT;
                type = 4;
                break;
            case MtpConstants.PROPERTY_PROTECTION_STATUS:
                type = 4;
                break;
            case MtpConstants.PROPERTY_OBJECT_SIZE:
                column = "_size";
                type = 8;
                break;
            case MtpConstants.PROPERTY_OBJECT_FILE_NAME:
                column = "_data";
                type = 65535;
                break;
            case MtpConstants.PROPERTY_DATE_MODIFIED:
                column = "date_modified";
                type = 65535;
                break;
            case MtpConstants.PROPERTY_PARENT_OBJECT:
                column = "parent";
                type = 6;
                break;
            case MtpConstants.PROPERTY_PERSISTENT_UID:
                column = MediaStore.Files.FileColumns.STORAGE_ID;
                type = 10;
                break;
            case MtpConstants.PROPERTY_NAME:
                column = "title";
                type = 65535;
                break;
            case MtpConstants.PROPERTY_ARTIST:
                type = 65535;
                break;
            case MtpConstants.PROPERTY_DESCRIPTION:
                column = "description";
                type = 65535;
                break;
            case MtpConstants.PROPERTY_DATE_ADDED:
                column = "date_added";
                type = 65535;
                break;
            case MtpConstants.PROPERTY_DURATION:
                column = "duration";
                type = 6;
                break;
            case MtpConstants.PROPERTY_TRACK:
                column = MediaStore.Audio.AudioColumns.TRACK;
                type = 4;
                break;
            case MtpConstants.PROPERTY_GENRE:
                type = 65535;
                break;
            case MtpConstants.PROPERTY_COMPOSER:
                column = MediaStore.Audio.AudioColumns.COMPOSER;
                type = 65535;
                break;
            case MtpConstants.PROPERTY_ORIGINAL_RELEASE_DATE:
                column = MediaStore.Audio.AudioColumns.YEAR;
                type = 65535;
                break;
            case MtpConstants.PROPERTY_ALBUM_NAME:
                type = 65535;
                break;
            case MtpConstants.PROPERTY_ALBUM_ARTIST:
                column = MediaStore.Audio.AudioColumns.ALBUM_ARTIST;
                type = 65535;
                break;
            case MtpConstants.PROPERTY_DISPLAY_NAME:
                column = "_display_name";
                type = 65535;
                break;
            default:
                type = 0;
                Log.e(TAG, "unsupported property " + code);
                break;
        }
        if (column == null) {
            return new Property(code, type, -1);
        }
        columns.add(column);
        return new Property(code, type, columns.size() - 1);
    }

    private String queryString(int id, String column) {
        Cursor c = null;
        try {
            Cursor c2 = this.mProvider.query(this.mPackageName, this.mUri, new String[]{"_id", column}, ID_WHERE, new String[]{Integer.toString(id)}, null, null);
            if (c2 != null && c2.moveToNext()) {
                String string = c2.getString(1);
                if (c2 == null) {
                    return string;
                }
                c2.close();
                return string;
            } else if (c2 == null) {
                return ProxyInfo.LOCAL_EXCL_LIST;
            } else {
                c2.close();
                return ProxyInfo.LOCAL_EXCL_LIST;
            }
        } catch (Exception e) {
            if (0 != 0) {
                c.close();
            }
            return null;
        } catch (Throwable th) {
            if (0 != 0) {
                c.close();
            }
            throw th;
        }
    }

    private String queryAudio(int id, String column) {
        Cursor c = null;
        try {
            Cursor c2 = this.mProvider.query(this.mPackageName, MediaStore.Audio.Media.getContentUri(this.mVolumeName), new String[]{"_id", column}, ID_WHERE, new String[]{Integer.toString(id)}, null, null);
            if (c2 != null && c2.moveToNext()) {
                String string = c2.getString(1);
                if (c2 == null) {
                    return string;
                }
                c2.close();
                return string;
            } else if (c2 == null) {
                return ProxyInfo.LOCAL_EXCL_LIST;
            } else {
                c2.close();
                return ProxyInfo.LOCAL_EXCL_LIST;
            }
        } catch (Exception e) {
            if (0 != 0) {
                c.close();
            }
            return null;
        } catch (Throwable th) {
            if (0 != 0) {
                c.close();
            }
            throw th;
        }
    }

    private String queryGenre(int id) {
        Cursor c = null;
        try {
            Uri uri = MediaStore.Audio.Genres.getContentUriForAudioId(this.mVolumeName, id);
            Cursor c2 = this.mProvider.query(this.mPackageName, uri, new String[]{"_id", "name"}, null, null, null, null);
            if (c2 != null && c2.moveToNext()) {
                String string = c2.getString(1);
                if (c2 == null) {
                    return string;
                }
                c2.close();
                return string;
            } else if (c2 == null) {
                return ProxyInfo.LOCAL_EXCL_LIST;
            } else {
                c2.close();
                return ProxyInfo.LOCAL_EXCL_LIST;
            }
        } catch (Exception e) {
            Log.e(TAG, "queryGenre exception", e);
            if (0 != 0) {
                c.close();
            }
            return null;
        } catch (Throwable th) {
            if (0 != 0) {
                c.close();
            }
            throw th;
        }
    }

    private Long queryLong(int id, String column) {
        Cursor c = null;
        try {
            Cursor c2 = this.mProvider.query(this.mPackageName, this.mUri, new String[]{"_id", column}, ID_WHERE, new String[]{Integer.toString(id)}, null, null);
            if (c2 == null || !c2.moveToNext()) {
                if (c2 != null) {
                    c2.close();
                }
                return null;
            }
            Long l = new Long(c2.getLong(1));
            if (c2 == null) {
                return l;
            }
            c2.close();
            return l;
        } catch (Exception e) {
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

    private static String nameFromPath(String path) {
        int start = 0;
        int lastSlash = path.lastIndexOf(47);
        if (lastSlash >= 0) {
            start = lastSlash + 1;
        }
        int end = path.length();
        if (end - start > 255) {
            end = start + 255;
        }
        return path.substring(start, end);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0027, code lost:
        if (r32.mColumns.length > 1) goto L_0x0029;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public android.mtp.MtpPropertyList getPropertyList(int r33, int r34, int r35) {
        /*
        // Method dump skipped, instructions count: 598
        */
        throw new UnsupportedOperationException("Method not decompiled: android.mtp.MtpPropertyGroup.getPropertyList(int, int, int):android.mtp.MtpPropertyList");
    }
}

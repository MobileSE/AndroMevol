package android.media;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.IContentProvider;
import android.database.Cursor;
import android.database.SQLException;
import android.drm.DrmManagerClient;
import android.graphics.BitmapFactory;
import android.media.MediaFile;
import android.mtp.MtpConstants;
import android.net.ProxyInfo;
import android.net.Uri;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Environment;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.provider.MediaStore;
import android.provider.Settings;
import android.sax.ElementListener;
import android.sax.RootElement;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.text.TextUtils;
import android.util.Log;
import java.io.File;
import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;

public class MediaScanner {
    private static final String ALARMS_DIR = "/alarms/";
    private static final int DATE_MODIFIED_PLAYLISTS_COLUMN_INDEX = 2;
    private static final String DEFAULT_RINGTONE_PROPERTY_PREFIX = "ro.config.";
    private static final boolean ENABLE_BULK_INSERTS = true;
    private static final int FILES_PRESCAN_DATE_MODIFIED_COLUMN_INDEX = 3;
    private static final int FILES_PRESCAN_FORMAT_COLUMN_INDEX = 2;
    private static final int FILES_PRESCAN_ID_COLUMN_INDEX = 0;
    private static final int FILES_PRESCAN_PATH_COLUMN_INDEX = 1;
    private static final String[] FILES_PRESCAN_PROJECTION = {"_id", "_data", MediaStore.Files.FileColumns.FORMAT, "date_modified"};
    private static final String[] ID3_GENRES = {"Blues", "Classic Rock", "Country", "Dance", "Disco", "Funk", "Grunge", "Hip-Hop", "Jazz", "Metal", "New Age", "Oldies", "Other", "Pop", "R&B", "Rap", "Reggae", "Rock", "Techno", "Industrial", "Alternative", "Ska", "Death Metal", "Pranks", "Soundtrack", "Euro-Techno", "Ambient", "Trip-Hop", "Vocal", "Jazz+Funk", "Fusion", "Trance", "Classical", "Instrumental", "Acid", "House", "Game", "Sound Clip", "Gospel", "Noise", "AlternRock", "Bass", "Soul", "Punk", "Space", "Meditative", "Instrumental Pop", "Instrumental Rock", "Ethnic", "Gothic", "Darkwave", "Techno-Industrial", "Electronic", "Pop-Folk", "Eurodance", "Dream", "Southern Rock", "Comedy", "Cult", "Gangsta", "Top 40", "Christian Rap", "Pop/Funk", "Jungle", "Native American", "Cabaret", "New Wave", "Psychadelic", "Rave", "Showtunes", "Trailer", "Lo-Fi", "Tribal", "Acid Punk", "Acid Jazz", "Polka", "Retro", "Musical", "Rock & Roll", "Hard Rock", "Folk", "Folk-Rock", "National Folk", "Swing", "Fast Fusion", "Bebob", "Latin", "Revival", "Celtic", "Bluegrass", "Avantgarde", "Gothic Rock", "Progressive Rock", "Psychedelic Rock", "Symphonic Rock", "Slow Rock", "Big Band", "Chorus", "Easy Listening", "Acoustic", "Humour", "Speech", "Chanson", "Opera", "Chamber Music", "Sonata", "Symphony", "Booty Bass", "Primus", "Porn Groove", "Satire", "Slow Jam", "Club", "Tango", "Samba", "Folklore", "Ballad", "Power Ballad", "Rhythmic Soul", "Freestyle", "Duet", "Punk Rock", "Drum Solo", "A capella", "Euro-House", "Dance Hall", "Goa", "Drum & Bass", "Club-House", "Hardcore", "Terror", "Indie", "Britpop", null, "Polsk Punk", "Beat", "Christian Gangsta", "Heavy Metal", "Black Metal", "Crossover", "Contemporary Christian", "Christian Rock", "Merengue", "Salsa", "Thrash Metal", "Anime", "JPop", "Synthpop"};
    private static final int ID_PLAYLISTS_COLUMN_INDEX = 0;
    private static final String[] ID_PROJECTION = {"_id"};
    private static final String MUSIC_DIR = "/music/";
    private static final String NOTIFICATIONS_DIR = "/notifications/";
    private static final int PATH_PLAYLISTS_COLUMN_INDEX = 1;
    private static final String[] PLAYLIST_MEMBERS_PROJECTION = {MediaStore.Audio.Playlists.Members.PLAYLIST_ID};
    private static final String PODCAST_DIR = "/podcasts/";
    private static final String RINGTONES_DIR = "/ringtones/";
    private static final String TAG = "MediaScanner";
    private static HashMap<String, String> mMediaPaths = new HashMap<>();
    private static HashMap<String, String> mNoMediaPaths = new HashMap<>();
    private Uri mAudioUri;
    private final BitmapFactory.Options mBitmapOptions = new BitmapFactory.Options();
    private boolean mCaseInsensitivePaths;
    private final MyMediaScannerClient mClient = new MyMediaScannerClient();
    private Context mContext;
    private String mDefaultAlarmAlertFilename;
    private boolean mDefaultAlarmSet;
    private String mDefaultNotificationFilename;
    private boolean mDefaultNotificationSet;
    private String mDefaultRingtoneFilename;
    private boolean mDefaultRingtoneSet;
    private DrmManagerClient mDrmManagerClient = null;
    private final boolean mExternalIsEmulated;
    private final String mExternalStoragePath;
    private Uri mFilesUri;
    private Uri mFilesUriNoNotify;
    private Uri mImagesUri;
    private MediaInserter mMediaInserter;
    private IContentProvider mMediaProvider;
    private int mMtpObjectHandle;
    private long mNativeContext;
    private int mOriginalCount;
    private String mPackageName;
    private ArrayList<FileEntry> mPlayLists;
    private ArrayList<PlaylistEntry> mPlaylistEntries = new ArrayList<>();
    private Uri mPlaylistsUri;
    private boolean mProcessGenres;
    private boolean mProcessPlaylists;
    private Uri mThumbsUri;
    private Uri mVideoUri;
    private boolean mWasEmptyPriorToScan = false;

    private final native void native_finalize();

    private static final native void native_init();

    private final native void native_setup();

    private native void processDirectory(String str, MediaScannerClient mediaScannerClient);

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private native void processFile(String str, String str2, MediaScannerClient mediaScannerClient);

    public native byte[] extractAlbumArt(FileDescriptor fileDescriptor);

    public native void setLocale(String str);

    static {
        System.loadLibrary("media_jni");
        native_init();
    }

    /* access modifiers changed from: private */
    public static class FileEntry {
        int mFormat;
        long mLastModified;
        boolean mLastModifiedChanged = false;
        String mPath;
        long mRowId;

        FileEntry(long rowId, String path, long lastModified, int format) {
            this.mRowId = rowId;
            this.mPath = path;
            this.mLastModified = lastModified;
            this.mFormat = format;
        }

        public String toString() {
            return this.mPath + " mRowId: " + this.mRowId;
        }
    }

    /* access modifiers changed from: private */
    public static class PlaylistEntry {
        long bestmatchid;
        int bestmatchlevel;
        String path;

        private PlaylistEntry() {
        }
    }

    public MediaScanner(Context c) {
        native_setup();
        this.mContext = c;
        this.mPackageName = c.getPackageName();
        this.mBitmapOptions.inSampleSize = 1;
        this.mBitmapOptions.inJustDecodeBounds = true;
        setDefaultRingtoneFileNames();
        this.mExternalStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        this.mExternalIsEmulated = Environment.isExternalStorageEmulated();
    }

    private void setDefaultRingtoneFileNames() {
        this.mDefaultRingtoneFilename = SystemProperties.get("ro.config.ringtone");
        this.mDefaultNotificationFilename = SystemProperties.get("ro.config.notification_sound");
        this.mDefaultAlarmAlertFilename = SystemProperties.get("ro.config.alarm_alert");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isDrmEnabled() {
        String prop = SystemProperties.get("drm.service.enabled");
        return prop != null && prop.equals("true");
    }

    private class MyMediaScannerClient implements MediaScannerClient {
        private String mAlbum;
        private String mAlbumArtist;
        private String mArtist;
        private int mCompilation;
        private String mComposer;
        private int mDuration;
        private long mFileSize;
        private int mFileType;
        private String mGenre;
        private int mHeight;
        private boolean mIsDrm;
        private long mLastModified;
        private String mMimeType;
        private boolean mNoMedia;
        private String mPath;
        private String mTitle;
        private int mTrack;
        private int mWidth;
        private String mWriter;
        private int mYear;

        private MyMediaScannerClient() {
        }

        public FileEntry beginFile(String path, String mimeType, long lastModified, long fileSize, boolean isDirectory, boolean noMedia) {
            MediaFile.MediaFileType mediaFileType;
            this.mMimeType = mimeType;
            this.mFileType = 0;
            this.mFileSize = fileSize;
            this.mIsDrm = false;
            if (!isDirectory) {
                if (!noMedia && MediaScanner.isNoMediaFile(path)) {
                    noMedia = true;
                }
                this.mNoMedia = noMedia;
                if (mimeType != null) {
                    this.mFileType = MediaFile.getFileTypeForMimeType(mimeType);
                }
                if (this.mFileType == 0 && (mediaFileType = MediaFile.getFileType(path)) != null) {
                    this.mFileType = mediaFileType.fileType;
                    if (this.mMimeType == null) {
                        this.mMimeType = mediaFileType.mimeType;
                    }
                }
                if (MediaScanner.this.isDrmEnabled() && MediaFile.isDrmFileType(this.mFileType)) {
                    this.mFileType = getFileTypeFromDrm(path);
                }
            }
            FileEntry entry = MediaScanner.this.makeEntryFor(path);
            long delta = entry != null ? lastModified - entry.mLastModified : 0;
            boolean wasModified = delta > 1 || delta < -1;
            if (entry == null || wasModified) {
                if (wasModified) {
                    entry.mLastModified = lastModified;
                } else {
                    entry = new FileEntry(0, path, lastModified, isDirectory ? 12289 : 0);
                }
                entry.mLastModifiedChanged = true;
            }
            if (!MediaScanner.this.mProcessPlaylists || !MediaFile.isPlayListFileType(this.mFileType)) {
                this.mArtist = null;
                this.mAlbumArtist = null;
                this.mAlbum = null;
                this.mTitle = null;
                this.mComposer = null;
                this.mGenre = null;
                this.mTrack = 0;
                this.mYear = 0;
                this.mDuration = 0;
                this.mPath = path;
                this.mLastModified = lastModified;
                this.mWriter = null;
                this.mCompilation = 0;
                this.mWidth = 0;
                this.mHeight = 0;
                return entry;
            }
            MediaScanner.this.mPlayLists.add(entry);
            return null;
        }

        @Override // android.media.MediaScannerClient
        public void scanFile(String path, long lastModified, long fileSize, boolean isDirectory, boolean noMedia) {
            doScanFile(path, null, lastModified, fileSize, isDirectory, false, noMedia);
        }

        public Uri doScanFile(String path, String mimeType, long lastModified, long fileSize, boolean isDirectory, boolean scanAlways, boolean noMedia) {
            try {
                FileEntry entry = beginFile(path, mimeType, lastModified, fileSize, isDirectory, noMedia);
                if (MediaScanner.this.mMtpObjectHandle != 0) {
                    entry.mRowId = 0;
                }
                if (entry == null) {
                    return null;
                }
                if (!entry.mLastModifiedChanged && !scanAlways) {
                    return null;
                }
                if (noMedia) {
                    return endFile(entry, false, false, false, false, false);
                }
                String lowpath = path.toLowerCase(Locale.ROOT);
                boolean ringtones = lowpath.indexOf(MediaScanner.RINGTONES_DIR) > 0;
                boolean notifications = lowpath.indexOf(MediaScanner.NOTIFICATIONS_DIR) > 0;
                boolean alarms = lowpath.indexOf(MediaScanner.ALARMS_DIR) > 0;
                boolean podcasts = lowpath.indexOf(MediaScanner.PODCAST_DIR) > 0;
                boolean music = lowpath.indexOf(MediaScanner.MUSIC_DIR) > 0 || (!ringtones && !notifications && !alarms && !podcasts);
                boolean isaudio = MediaFile.isAudioFileType(this.mFileType);
                boolean isvideo = MediaFile.isVideoFileType(this.mFileType);
                boolean isimage = MediaFile.isImageFileType(this.mFileType);
                if ((isaudio || isvideo || isimage) && MediaScanner.this.mExternalIsEmulated && path.startsWith(MediaScanner.this.mExternalStoragePath)) {
                    String directPath = Environment.getMediaStorageDirectory() + path.substring(MediaScanner.this.mExternalStoragePath.length());
                    if (new File(directPath).exists()) {
                        path = directPath;
                    }
                }
                if (isaudio || isvideo) {
                    MediaScanner.this.processFile(path, mimeType, this);
                }
                if (isimage) {
                    processImageFile(path);
                }
                return endFile(entry, ringtones, notifications, alarms, music, podcasts);
            } catch (RemoteException e) {
                Log.e(MediaScanner.TAG, "RemoteException in MediaScanner.scanFile()", e);
                return null;
            }
        }

        private int parseSubstring(String s, int start, int defaultValue) {
            int length = s.length();
            if (start == length) {
                return defaultValue;
            }
            int start2 = start + 1;
            char ch = s.charAt(start);
            if (ch < '0' || ch > '9') {
                return defaultValue;
            }
            int result = ch - '0';
            while (start2 < length) {
                int start3 = start2 + 1;
                char ch2 = s.charAt(start2);
                if (ch2 < '0' || ch2 > '9') {
                    return result;
                }
                result = (result * 10) + (ch2 - '0');
                start2 = start3;
            }
            return result;
        }

        @Override // android.media.MediaScannerClient
        public void handleStringTag(String name, String value) {
            boolean z = true;
            if (name.equalsIgnoreCase("title") || name.startsWith("title;")) {
                this.mTitle = value;
            } else if (name.equalsIgnoreCase("artist") || name.startsWith("artist;")) {
                this.mArtist = value.trim();
            } else if (name.equalsIgnoreCase("albumartist") || name.startsWith("albumartist;") || name.equalsIgnoreCase("band") || name.startsWith("band;")) {
                this.mAlbumArtist = value.trim();
            } else if (name.equalsIgnoreCase("album") || name.startsWith("album;")) {
                this.mAlbum = value.trim();
            } else if (name.equalsIgnoreCase(MediaStore.Audio.AudioColumns.COMPOSER) || name.startsWith("composer;")) {
                this.mComposer = value.trim();
            } else if (MediaScanner.this.mProcessGenres && (name.equalsIgnoreCase(MediaStore.Audio.AudioColumns.GENRE) || name.startsWith("genre;"))) {
                this.mGenre = getGenreName(value);
            } else if (name.equalsIgnoreCase(MediaStore.Audio.AudioColumns.YEAR) || name.startsWith("year;")) {
                this.mYear = parseSubstring(value, 0, 0);
            } else if (name.equalsIgnoreCase("tracknumber") || name.startsWith("tracknumber;")) {
                this.mTrack = ((this.mTrack / 1000) * 1000) + parseSubstring(value, 0, 0);
            } else if (name.equalsIgnoreCase("discnumber") || name.equals("set") || name.startsWith("set;")) {
                this.mTrack = (parseSubstring(value, 0, 0) * 1000) + (this.mTrack % 1000);
            } else if (name.equalsIgnoreCase("duration")) {
                this.mDuration = parseSubstring(value, 0, 0);
            } else if (name.equalsIgnoreCase("writer") || name.startsWith("writer;")) {
                this.mWriter = value.trim();
            } else if (name.equalsIgnoreCase(MediaStore.Audio.AudioColumns.COMPILATION)) {
                this.mCompilation = parseSubstring(value, 0, 0);
            } else if (name.equalsIgnoreCase("isdrm")) {
                if (parseSubstring(value, 0, 0) != 1) {
                    z = false;
                }
                this.mIsDrm = z;
            } else if (name.equalsIgnoreCase("width")) {
                this.mWidth = parseSubstring(value, 0, 0);
            } else if (name.equalsIgnoreCase("height")) {
                this.mHeight = parseSubstring(value, 0, 0);
            }
        }

        private boolean convertGenreCode(String input, String expected) {
            String output = getGenreName(input);
            if (output.equals(expected)) {
                return true;
            }
            Log.d(MediaScanner.TAG, "'" + input + "' -> '" + output + "', expected '" + expected + "'");
            return false;
        }

        private void testGenreNameConverter() {
            convertGenreCode("2", "Country");
            convertGenreCode("(2)", "Country");
            convertGenreCode("(2", "(2");
            convertGenreCode("2 Foo", "Country");
            convertGenreCode("(2) Foo", "Country");
            convertGenreCode("(2 Foo", "(2 Foo");
            convertGenreCode("2Foo", "2Foo");
            convertGenreCode("(2)Foo", "Country");
            convertGenreCode("200 Foo", "Foo");
            convertGenreCode("(200) Foo", "Foo");
            convertGenreCode("200Foo", "200Foo");
            convertGenreCode("(200)Foo", "Foo");
            convertGenreCode("200)Foo", "200)Foo");
            convertGenreCode("200) Foo", "200) Foo");
        }

        public String getGenreName(String genreTagValue) {
            if (genreTagValue == null) {
                return null;
            }
            int length = genreTagValue.length();
            if (length > 0) {
                boolean parenthesized = false;
                StringBuffer number = new StringBuffer();
                int i = 0;
                while (i < length) {
                    char c = genreTagValue.charAt(i);
                    if (i != 0 || c != '(') {
                        if (!Character.isDigit(c)) {
                            break;
                        }
                        number.append(c);
                    } else {
                        parenthesized = true;
                    }
                    i++;
                }
                char charAfterNumber = i < length ? genreTagValue.charAt(i) : ' ';
                if ((parenthesized && charAfterNumber == ')') || (!parenthesized && Character.isWhitespace(charAfterNumber))) {
                    try {
                        short genreIndex = Short.parseShort(number.toString());
                        if (genreIndex >= 0) {
                            if (genreIndex < MediaScanner.ID3_GENRES.length && MediaScanner.ID3_GENRES[genreIndex] != null) {
                                return MediaScanner.ID3_GENRES[genreIndex];
                            }
                            if (genreIndex == 255) {
                                return null;
                            }
                            if (genreIndex >= 255 || i + 1 >= length) {
                                return number.toString();
                            }
                            if (parenthesized && charAfterNumber == ')') {
                                i++;
                            }
                            String ret = genreTagValue.substring(i).trim();
                            if (ret.length() != 0) {
                                return ret;
                            }
                        }
                    } catch (NumberFormatException e) {
                    }
                }
            }
            return genreTagValue;
        }

        private void processImageFile(String path) {
            try {
                MediaScanner.this.mBitmapOptions.outWidth = 0;
                MediaScanner.this.mBitmapOptions.outHeight = 0;
                BitmapFactory.decodeFile(path, MediaScanner.this.mBitmapOptions);
                this.mWidth = MediaScanner.this.mBitmapOptions.outWidth;
                this.mHeight = MediaScanner.this.mBitmapOptions.outHeight;
            } catch (Throwable th) {
            }
        }

        @Override // android.media.MediaScannerClient
        public void setMimeType(String mimeType) {
            if (!"audio/mp4".equals(this.mMimeType) || !mimeType.startsWith("video")) {
                this.mMimeType = mimeType;
                this.mFileType = MediaFile.getFileTypeForMimeType(mimeType);
            }
        }

        private ContentValues toValues() {
            ContentValues map = new ContentValues();
            map.put("_data", this.mPath);
            map.put("title", this.mTitle);
            map.put("date_modified", Long.valueOf(this.mLastModified));
            map.put("_size", Long.valueOf(this.mFileSize));
            map.put("mime_type", this.mMimeType);
            map.put(MediaStore.MediaColumns.IS_DRM, Boolean.valueOf(this.mIsDrm));
            String resolution = null;
            if (this.mWidth > 0 && this.mHeight > 0) {
                map.put("width", Integer.valueOf(this.mWidth));
                map.put("height", Integer.valueOf(this.mHeight));
                resolution = this.mWidth + "x" + this.mHeight;
            }
            if (!this.mNoMedia) {
                if (MediaFile.isVideoFileType(this.mFileType)) {
                    map.put("artist", (this.mArtist == null || this.mArtist.length() <= 0) ? MediaStore.UNKNOWN_STRING : this.mArtist);
                    map.put("album", (this.mAlbum == null || this.mAlbum.length() <= 0) ? MediaStore.UNKNOWN_STRING : this.mAlbum);
                    map.put("duration", Integer.valueOf(this.mDuration));
                    if (resolution != null) {
                        map.put(MediaStore.Video.VideoColumns.RESOLUTION, resolution);
                    }
                } else if (!MediaFile.isImageFileType(this.mFileType) && MediaFile.isAudioFileType(this.mFileType)) {
                    map.put("artist", (this.mArtist == null || this.mArtist.length() <= 0) ? MediaStore.UNKNOWN_STRING : this.mArtist);
                    map.put(MediaStore.Audio.AudioColumns.ALBUM_ARTIST, (this.mAlbumArtist == null || this.mAlbumArtist.length() <= 0) ? null : this.mAlbumArtist);
                    map.put("album", (this.mAlbum == null || this.mAlbum.length() <= 0) ? MediaStore.UNKNOWN_STRING : this.mAlbum);
                    map.put(MediaStore.Audio.AudioColumns.COMPOSER, this.mComposer);
                    map.put(MediaStore.Audio.AudioColumns.GENRE, this.mGenre);
                    if (this.mYear != 0) {
                        map.put(MediaStore.Audio.AudioColumns.YEAR, Integer.valueOf(this.mYear));
                    }
                    map.put(MediaStore.Audio.AudioColumns.TRACK, Integer.valueOf(this.mTrack));
                    map.put("duration", Integer.valueOf(this.mDuration));
                    map.put(MediaStore.Audio.AudioColumns.COMPILATION, Integer.valueOf(this.mCompilation));
                }
            }
            return map;
        }

        /* JADX WARNING: Removed duplicated region for block: B:20:0x0071  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private android.net.Uri endFile(android.media.MediaScanner.FileEntry r31, boolean r32, boolean r33, boolean r34, boolean r35, boolean r36) throws android.os.RemoteException {
            /*
            // Method dump skipped, instructions count: 942
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MediaScanner.MyMediaScannerClient.endFile(android.media.MediaScanner$FileEntry, boolean, boolean, boolean, boolean, boolean):android.net.Uri");
        }

        private boolean doesPathHaveFilename(String path, String filename) {
            int pathFilenameStart = path.lastIndexOf(File.separatorChar) + 1;
            int filenameLength = filename.length();
            if (!path.regionMatches(pathFilenameStart, filename, 0, filenameLength) || pathFilenameStart + filenameLength != path.length()) {
                return false;
            }
            return true;
        }

        private void setSettingIfNotSet(String settingName, Uri uri, long rowId) {
            if (TextUtils.isEmpty(Settings.System.getString(MediaScanner.this.mContext.getContentResolver(), settingName))) {
                Settings.System.putString(MediaScanner.this.mContext.getContentResolver(), settingName, ContentUris.withAppendedId(uri, rowId).toString());
            }
        }

        private int getFileTypeFromDrm(String path) {
            if (!MediaScanner.this.isDrmEnabled()) {
                return 0;
            }
            if (MediaScanner.this.mDrmManagerClient == null) {
                MediaScanner.this.mDrmManagerClient = new DrmManagerClient(MediaScanner.this.mContext);
            }
            if (!MediaScanner.this.mDrmManagerClient.canHandle(path, (String) null)) {
                return 0;
            }
            this.mIsDrm = true;
            String drmMimetype = MediaScanner.this.mDrmManagerClient.getOriginalMimeType(path);
            if (drmMimetype == null) {
                return 0;
            }
            this.mMimeType = drmMimetype;
            return MediaFile.getFileTypeForMimeType(drmMimetype);
        }
    }

    private void prescan(String filePath, boolean prescanFiles) throws RemoteException {
        String where;
        String[] selectionArgs;
        int fileType;
        Cursor c = null;
        if (this.mPlayLists == null) {
            this.mPlayLists = new ArrayList<>();
        } else {
            this.mPlayLists.clear();
        }
        if (filePath != null) {
            where = "_id>? AND _data=?";
            selectionArgs = new String[]{ProxyInfo.LOCAL_EXCL_LIST, filePath};
        } else {
            where = "_id>?";
            selectionArgs = new String[]{ProxyInfo.LOCAL_EXCL_LIST};
        }
        Uri.Builder builder = this.mFilesUri.buildUpon();
        builder.appendQueryParameter(MediaStore.PARAM_DELETE_DATA, "false");
        MediaBulkDeleter deleter = new MediaBulkDeleter(this.mMediaProvider, this.mPackageName, builder.build());
        if (prescanFiles) {
            long lastId = Long.MIN_VALUE;
            try {
                Uri limitUri = this.mFilesUri.buildUpon().appendQueryParameter("limit", "1000").build();
                this.mWasEmptyPriorToScan = true;
                while (true) {
                    selectionArgs[0] = ProxyInfo.LOCAL_EXCL_LIST + lastId;
                    if (c != null) {
                        c.close();
                    }
                    c = this.mMediaProvider.query(this.mPackageName, limitUri, FILES_PRESCAN_PROJECTION, where, selectionArgs, "_id", null);
                    if (c == null || c.getCount() == 0) {
                        break;
                    }
                    this.mWasEmptyPriorToScan = false;
                    while (c.moveToNext()) {
                        long rowId = c.getLong(0);
                        String path = c.getString(1);
                        int format = c.getInt(2);
                        c.getLong(3);
                        lastId = rowId;
                        if (path != null && path.startsWith("/")) {
                            boolean exists = false;
                            try {
                                exists = Os.access(path, OsConstants.F_OK);
                            } catch (ErrnoException e) {
                            }
                            if (!exists && !MtpConstants.isAbstractObject(format)) {
                                MediaFile.MediaFileType mediaFileType = MediaFile.getFileType(path);
                                if (mediaFileType == null) {
                                    fileType = 0;
                                } else {
                                    fileType = mediaFileType.fileType;
                                }
                                if (!MediaFile.isPlayListFileType(fileType)) {
                                    deleter.delete(rowId);
                                    if (path.toLowerCase(Locale.US).endsWith("/.nomedia")) {
                                        deleter.flush();
                                        this.mMediaProvider.call(this.mPackageName, MediaStore.UNHIDE_CALL, new File(path).getParent(), null);
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    c.close();
                }
                deleter.flush();
                throw th;
            }
        }
        if (c != null) {
            c.close();
        }
        deleter.flush();
        this.mOriginalCount = 0;
        Cursor c2 = this.mMediaProvider.query(this.mPackageName, this.mImagesUri, ID_PROJECTION, null, null, null, null);
        if (c2 != null) {
            this.mOriginalCount = c2.getCount();
            c2.close();
        }
    }

    private boolean inScanDirectory(String path, String[] directories) {
        for (String directory : directories) {
            if (path.startsWith(directory)) {
                return true;
            }
        }
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:18:0x008e A[Catch:{ RemoteException -> 0x00bd, all -> 0x00c4 }] */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x00b9  */
    /* JADX WARNING: Removed duplicated region for block: B:39:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void pruneDeadThumbnailFiles() {
        /*
        // Method dump skipped, instructions count: 203
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.MediaScanner.pruneDeadThumbnailFiles():void");
    }

    /* access modifiers changed from: package-private */
    public static class MediaBulkDeleter {
        final Uri mBaseUri;
        final String mPackageName;
        final IContentProvider mProvider;
        ArrayList<String> whereArgs = new ArrayList<>(100);
        StringBuilder whereClause = new StringBuilder();

        public MediaBulkDeleter(IContentProvider provider, String packageName, Uri baseUri) {
            this.mProvider = provider;
            this.mPackageName = packageName;
            this.mBaseUri = baseUri;
        }

        public void delete(long id) throws RemoteException {
            if (this.whereClause.length() != 0) {
                this.whereClause.append(",");
            }
            this.whereClause.append("?");
            this.whereArgs.add(ProxyInfo.LOCAL_EXCL_LIST + id);
            if (this.whereArgs.size() > 100) {
                flush();
            }
        }

        public void flush() throws RemoteException {
            int size = this.whereArgs.size();
            if (size > 0) {
                ArrayList<String> arrayList = this.whereArgs;
                this.mProvider.delete(this.mPackageName, this.mBaseUri, "_id IN (" + this.whereClause.toString() + ")", (String[]) arrayList.toArray(new String[size]));
                this.whereClause.setLength(0);
                this.whereArgs.clear();
            }
        }
    }

    private void postscan(String[] directories) throws RemoteException {
        if (this.mProcessPlaylists) {
            processPlayLists();
        }
        if (this.mOriginalCount == 0 && this.mImagesUri.equals(MediaStore.Images.Media.getContentUri("external"))) {
            pruneDeadThumbnailFiles();
        }
        this.mPlayLists = null;
        this.mMediaProvider = null;
    }

    private void releaseResources() {
        if (this.mDrmManagerClient != null) {
            this.mDrmManagerClient.release();
            this.mDrmManagerClient = null;
        }
    }

    private void initialize(String volumeName) {
        this.mMediaProvider = this.mContext.getContentResolver().acquireProvider(MediaStore.AUTHORITY);
        this.mAudioUri = MediaStore.Audio.Media.getContentUri(volumeName);
        this.mVideoUri = MediaStore.Video.Media.getContentUri(volumeName);
        this.mImagesUri = MediaStore.Images.Media.getContentUri(volumeName);
        this.mThumbsUri = MediaStore.Images.Thumbnails.getContentUri(volumeName);
        this.mFilesUri = MediaStore.Files.getContentUri(volumeName);
        this.mFilesUriNoNotify = this.mFilesUri.buildUpon().appendQueryParameter("nonotify", WifiEnterpriseConfig.ENGINE_ENABLE).build();
        if (!volumeName.equals("internal")) {
            this.mProcessPlaylists = true;
            this.mProcessGenres = true;
            this.mPlaylistsUri = MediaStore.Audio.Playlists.getContentUri(volumeName);
            this.mCaseInsensitivePaths = true;
        }
    }

    public void scanDirectories(String[] directories, String volumeName) {
        try {
            System.currentTimeMillis();
            initialize(volumeName);
            prescan(null, true);
            System.currentTimeMillis();
            this.mMediaInserter = new MediaInserter(this.mMediaProvider, this.mPackageName, 500);
            for (String str : directories) {
                processDirectory(str, this.mClient);
            }
            this.mMediaInserter.flushAll();
            this.mMediaInserter = null;
            System.currentTimeMillis();
            postscan(directories);
            System.currentTimeMillis();
        } catch (SQLException e) {
            Log.e(TAG, "SQLException in MediaScanner.scan()", e);
        } catch (UnsupportedOperationException e2) {
            Log.e(TAG, "UnsupportedOperationException in MediaScanner.scan()", e2);
        } catch (RemoteException e3) {
            Log.e(TAG, "RemoteException in MediaScanner.scan()", e3);
        } finally {
            releaseResources();
        }
    }

    public Uri scanSingleFile(String path, String volumeName, String mimeType) {
        try {
            initialize(volumeName);
            prescan(path, true);
            File file = new File(path);
            if (!file.exists()) {
                return null;
            }
            Uri doScanFile = this.mClient.doScanFile(path, mimeType, file.lastModified() / 1000, file.length(), false, true, isNoMediaPath(path));
            releaseResources();
            return doScanFile;
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in MediaScanner.scanFile()", e);
            return null;
        } finally {
            releaseResources();
        }
    }

    /* access modifiers changed from: private */
    public static boolean isNoMediaFile(String path) {
        if (new File(path).isDirectory()) {
            return false;
        }
        int lastSlash = path.lastIndexOf(47);
        if (lastSlash >= 0 && lastSlash + 2 < path.length()) {
            if (path.regionMatches(lastSlash + 1, "._", 0, 2)) {
                return true;
            }
            if (path.regionMatches(true, path.length() - 4, ".jpg", 0, 4)) {
                if (path.regionMatches(true, lastSlash + 1, "AlbumArt_{", 0, 10) || path.regionMatches(true, lastSlash + 1, "AlbumArt.", 0, 9)) {
                    return true;
                }
                int length = (path.length() - lastSlash) - 1;
                if (length == 17 && path.regionMatches(true, lastSlash + 1, "AlbumArtSmall", 0, 13)) {
                    return true;
                }
                if (length == 10 && path.regionMatches(true, lastSlash + 1, "Folder", 0, 6)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void clearMediaPathCache(boolean clearMediaPaths, boolean clearNoMediaPaths) {
        synchronized (MediaScanner.class) {
            if (clearMediaPaths) {
                mMediaPaths.clear();
            }
            if (clearNoMediaPaths) {
                mNoMediaPaths.clear();
            }
        }
    }

    public static boolean isNoMediaPath(String path) {
        if (path == null) {
            return false;
        }
        if (path.indexOf("/.") >= 0) {
            return true;
        }
        int firstSlash = path.lastIndexOf(47);
        if (firstSlash <= 0) {
            return false;
        }
        String parent = path.substring(0, firstSlash);
        synchronized (MediaScanner.class) {
            if (mNoMediaPaths.containsKey(parent)) {
                return true;
            }
            if (!mMediaPaths.containsKey(parent)) {
                int offset = 1;
                while (offset >= 0) {
                    int slashIndex = path.indexOf(47, offset);
                    if (slashIndex > offset) {
                        slashIndex++;
                        if (new File(path.substring(0, slashIndex) + MediaStore.MEDIA_IGNORE_FILENAME).exists()) {
                            mNoMediaPaths.put(parent, ProxyInfo.LOCAL_EXCL_LIST);
                            return true;
                        }
                    }
                    offset = slashIndex;
                }
                mMediaPaths.put(parent, ProxyInfo.LOCAL_EXCL_LIST);
            }
            return isNoMediaFile(path);
        }
    }

    public void scanMtpFile(String path, String volumeName, int objectHandle, int format) {
        int fileType;
        initialize(volumeName);
        MediaFile.MediaFileType mediaFileType = MediaFile.getFileType(path);
        if (mediaFileType == null) {
            fileType = 0;
        } else {
            fileType = mediaFileType.fileType;
        }
        File file = new File(path);
        long lastModifiedSeconds = file.lastModified() / 1000;
        if (MediaFile.isAudioFileType(fileType) || MediaFile.isVideoFileType(fileType) || MediaFile.isImageFileType(fileType) || MediaFile.isPlayListFileType(fileType) || MediaFile.isDrmFileType(fileType)) {
            this.mMtpObjectHandle = objectHandle;
            Cursor fileList = null;
            try {
                if (MediaFile.isPlayListFileType(fileType)) {
                    prescan(null, true);
                    FileEntry entry = makeEntryFor(path);
                    if (entry != null) {
                        fileList = this.mMediaProvider.query(this.mPackageName, this.mFilesUri, FILES_PRESCAN_PROJECTION, null, null, null, null);
                        processPlayList(entry, fileList);
                    }
                } else {
                    prescan(path, false);
                    this.mClient.doScanFile(path, mediaFileType.mimeType, lastModifiedSeconds, file.length(), format == 12289, true, isNoMediaPath(path));
                }
                this.mMtpObjectHandle = 0;
                if (fileList != null) {
                    fileList.close();
                }
                releaseResources();
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException in MediaScanner.scanFile()", e);
                this.mMtpObjectHandle = 0;
                if (0 != 0) {
                    fileList.close();
                }
                releaseResources();
            } catch (Throwable th) {
                this.mMtpObjectHandle = 0;
                if (0 != 0) {
                    fileList.close();
                }
                releaseResources();
                throw th;
            }
        } else {
            ContentValues values = new ContentValues();
            values.put("_size", Long.valueOf(file.length()));
            values.put("date_modified", Long.valueOf(lastModifiedSeconds));
            try {
                this.mMediaProvider.update(this.mPackageName, MediaStore.Files.getMtpObjectsUri(volumeName), values, "_id=?", new String[]{Integer.toString(objectHandle)});
            } catch (RemoteException e2) {
                Log.e(TAG, "RemoteException in scanMtpFile", e2);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public FileEntry makeEntryFor(String path) {
        Cursor c = null;
        try {
            Cursor c2 = this.mMediaProvider.query(this.mPackageName, this.mFilesUriNoNotify, FILES_PRESCAN_PROJECTION, "_data=?", new String[]{path}, null, null);
            if (c2.moveToFirst()) {
                FileEntry fileEntry = new FileEntry(c2.getLong(0), path, c2.getLong(3), c2.getInt(2));
                if (c2 == null) {
                    return fileEntry;
                }
                c2.close();
                return fileEntry;
            }
            if (c2 != null) {
                c2.close();
            }
            return null;
        } catch (RemoteException e) {
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

    private int matchPaths(String path1, String path2) {
        int start1;
        int start2;
        int result = 0;
        int end1 = path1.length();
        int end2 = path2.length();
        while (end1 > 0 && end2 > 0) {
            int slash1 = path1.lastIndexOf(47, end1 - 1);
            int slash2 = path2.lastIndexOf(47, end2 - 1);
            int backSlash1 = path1.lastIndexOf(92, end1 - 1);
            int backSlash2 = path2.lastIndexOf(92, end2 - 1);
            if (slash1 > backSlash1) {
                start1 = slash1;
            } else {
                start1 = backSlash1;
            }
            if (slash2 > backSlash2) {
                start2 = slash2;
            } else {
                start2 = backSlash2;
            }
            int start12 = start1 < 0 ? 0 : start1 + 1;
            int start22 = start2 < 0 ? 0 : start2 + 1;
            int length = end1 - start12;
            if (end2 - start22 != length || !path1.regionMatches(true, start12, path2, start22, length)) {
                break;
            }
            result++;
            end1 = start12 - 1;
            end2 = start22 - 1;
        }
        return result;
    }

    private boolean matchEntries(long rowId, String data) {
        int len = this.mPlaylistEntries.size();
        boolean done = true;
        for (int i = 0; i < len; i++) {
            PlaylistEntry entry = this.mPlaylistEntries.get(i);
            if (entry.bestmatchlevel != Integer.MAX_VALUE) {
                done = false;
                if (data.equalsIgnoreCase(entry.path)) {
                    entry.bestmatchid = rowId;
                    entry.bestmatchlevel = Integer.MAX_VALUE;
                } else {
                    int matchLength = matchPaths(data, entry.path);
                    if (matchLength > entry.bestmatchlevel) {
                        entry.bestmatchid = rowId;
                        entry.bestmatchlevel = matchLength;
                    }
                }
            }
        }
        return done;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void cachePlaylistEntry(String line, String playListDirectory) {
        boolean fullPath = false;
        PlaylistEntry entry = new PlaylistEntry();
        int entryLength = line.length();
        while (entryLength > 0 && Character.isWhitespace(line.charAt(entryLength - 1))) {
            entryLength--;
        }
        if (entryLength >= 3) {
            if (entryLength < line.length()) {
                line = line.substring(0, entryLength);
            }
            char ch1 = line.charAt(0);
            if (ch1 == '/' || (Character.isLetter(ch1) && line.charAt(1) == ':' && line.charAt(2) == '\\')) {
                fullPath = true;
            }
            if (!fullPath) {
                line = playListDirectory + line;
            }
            entry.path = line;
            this.mPlaylistEntries.add(entry);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:3:0x000a  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void processCachedPlaylist(android.database.Cursor r13, android.content.ContentValues r14, android.net.Uri r15) {
        /*
            r12 = this;
            r8 = -1
            r13.moveToPosition(r8)
        L_0x0004:
            boolean r8 = r13.moveToNext()
            if (r8 == 0) goto L_0x001a
            r8 = 0
            long r6 = r13.getLong(r8)
            r8 = 1
            java.lang.String r0 = r13.getString(r8)
            boolean r8 = r12.matchEntries(r6, r0)
            if (r8 == 0) goto L_0x0004
        L_0x001a:
            java.util.ArrayList<android.media.MediaScanner$PlaylistEntry> r8 = r12.mPlaylistEntries
            int r5 = r8.size()
            r4 = 0
            r3 = 0
        L_0x0022:
            if (r3 >= r5) goto L_0x005d
            java.util.ArrayList<android.media.MediaScanner$PlaylistEntry> r8 = r12.mPlaylistEntries
            java.lang.Object r2 = r8.get(r3)
            android.media.MediaScanner$PlaylistEntry r2 = (android.media.MediaScanner.PlaylistEntry) r2
            int r8 = r2.bestmatchlevel
            if (r8 <= 0) goto L_0x0051
            r14.clear()     // Catch:{ RemoteException -> 0x0054 }
            java.lang.String r8 = "play_order"
            java.lang.Integer r9 = java.lang.Integer.valueOf(r4)     // Catch:{ RemoteException -> 0x0054 }
            r14.put(r8, r9)     // Catch:{ RemoteException -> 0x0054 }
            java.lang.String r8 = "audio_id"
            long r10 = r2.bestmatchid     // Catch:{ RemoteException -> 0x0054 }
            java.lang.Long r9 = java.lang.Long.valueOf(r10)     // Catch:{ RemoteException -> 0x0054 }
            r14.put(r8, r9)     // Catch:{ RemoteException -> 0x0054 }
            android.content.IContentProvider r8 = r12.mMediaProvider     // Catch:{ RemoteException -> 0x0054 }
            java.lang.String r9 = r12.mPackageName     // Catch:{ RemoteException -> 0x0054 }
            r8.insert(r9, r15, r14)     // Catch:{ RemoteException -> 0x0054 }
            int r4 = r4 + 1
        L_0x0051:
            int r3 = r3 + 1
            goto L_0x0022
        L_0x0054:
            r1 = move-exception
            java.lang.String r8 = "MediaScanner"
            java.lang.String r9 = "RemoteException in MediaScanner.processCachedPlaylist()"
            android.util.Log.e(r8, r9, r1)
        L_0x005c:
            return
        L_0x005d:
            java.util.ArrayList<android.media.MediaScanner$PlaylistEntry> r8 = r12.mPlaylistEntries
            r8.clear()
            goto L_0x005c
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.MediaScanner.processCachedPlaylist(android.database.Cursor, android.content.ContentValues, android.net.Uri):void");
    }

    /* JADX WARNING: Removed duplicated region for block: B:26:0x005c A[SYNTHETIC, Splitter:B:26:0x005c] */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x006c A[SYNTHETIC, Splitter:B:32:0x006c] */
    /* JADX WARNING: Removed duplicated region for block: B:48:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void processM3uPlayList(java.lang.String r9, java.lang.String r10, android.net.Uri r11, android.content.ContentValues r12, android.database.Cursor r13) {
        /*
        // Method dump skipped, instructions count: 127
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.MediaScanner.processM3uPlayList(java.lang.String, java.lang.String, android.net.Uri, android.content.ContentValues, android.database.Cursor):void");
    }

    /* JADX WARNING: Removed duplicated region for block: B:26:0x0063 A[SYNTHETIC, Splitter:B:26:0x0063] */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x0073 A[SYNTHETIC, Splitter:B:32:0x0073] */
    /* JADX WARNING: Removed duplicated region for block: B:48:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void processPlsPlayList(java.lang.String r10, java.lang.String r11, android.net.Uri r12, android.content.ContentValues r13, android.database.Cursor r14) {
        /*
        // Method dump skipped, instructions count: 134
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.MediaScanner.processPlsPlayList(java.lang.String, java.lang.String, android.net.Uri, android.content.ContentValues, android.database.Cursor):void");
    }

    /* access modifiers changed from: package-private */
    public class WplHandler implements ElementListener {
        final ContentHandler handler;
        String playListDirectory;

        public WplHandler(String playListDirectory2, Uri uri, Cursor fileList) {
            this.playListDirectory = playListDirectory2;
            RootElement root = new RootElement("smil");
            root.getChild(TtmlUtils.TAG_BODY).getChild("seq").getChild(MediaStore.AUTHORITY).setElementListener(this);
            this.handler = root.getContentHandler();
        }

        @Override // android.sax.StartElementListener
        public void start(Attributes attributes) {
            String path = attributes.getValue(ProxyInfo.LOCAL_EXCL_LIST, "src");
            if (path != null) {
                MediaScanner.this.cachePlaylistEntry(path, this.playListDirectory);
            }
        }

        @Override // android.sax.EndElementListener
        public void end() {
        }

        /* access modifiers changed from: package-private */
        public ContentHandler getContentHandler() {
            return this.handler;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0041 A[SYNTHETIC, Splitter:B:17:0x0041] */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0054 A[SYNTHETIC, Splitter:B:24:0x0054] */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0064 A[SYNTHETIC, Splitter:B:30:0x0064] */
    /* JADX WARNING: Removed duplicated region for block: B:44:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:46:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void processWplPlayList(java.lang.String r8, java.lang.String r9, android.net.Uri r10, android.content.ContentValues r11, android.database.Cursor r12) {
        /*
        // Method dump skipped, instructions count: 122
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.MediaScanner.processWplPlayList(java.lang.String, java.lang.String, android.net.Uri, android.content.ContentValues, android.database.Cursor):void");
    }

    private void processPlayList(FileEntry entry, Cursor fileList) throws RemoteException {
        Uri membersUri;
        String path = entry.mPath;
        ContentValues values = new ContentValues();
        int lastSlash = path.lastIndexOf(47);
        if (lastSlash < 0) {
            throw new IllegalArgumentException("bad path " + path);
        }
        long rowId = entry.mRowId;
        String name = values.getAsString("name");
        if (name == null && (name = values.getAsString("title")) == null) {
            int lastDot = path.lastIndexOf(46);
            if (lastDot < 0) {
                name = path.substring(lastSlash + 1);
            } else {
                name = path.substring(lastSlash + 1, lastDot);
            }
        }
        values.put("name", name);
        values.put("date_modified", Long.valueOf(entry.mLastModified));
        if (rowId == 0) {
            values.put("_data", path);
            Uri uri = this.mMediaProvider.insert(this.mPackageName, this.mPlaylistsUri, values);
            ContentUris.parseId(uri);
            membersUri = Uri.withAppendedPath(uri, "members");
        } else {
            Uri uri2 = ContentUris.withAppendedId(this.mPlaylistsUri, rowId);
            this.mMediaProvider.update(this.mPackageName, uri2, values, null, null);
            membersUri = Uri.withAppendedPath(uri2, "members");
            this.mMediaProvider.delete(this.mPackageName, membersUri, null, null);
        }
        String playListDirectory = path.substring(0, lastSlash + 1);
        MediaFile.MediaFileType mediaFileType = MediaFile.getFileType(path);
        int fileType = mediaFileType == null ? 0 : mediaFileType.fileType;
        if (fileType == 41) {
            processM3uPlayList(path, playListDirectory, membersUri, values, fileList);
        } else if (fileType == 42) {
            processPlsPlayList(path, playListDirectory, membersUri, values, fileList);
        } else if (fileType == 43) {
            processWplPlayList(path, playListDirectory, membersUri, values, fileList);
        }
    }

    private void processPlayLists() throws RemoteException {
        Iterator<FileEntry> iterator = this.mPlayLists.iterator();
        Cursor fileList = null;
        try {
            Cursor fileList2 = this.mMediaProvider.query(this.mPackageName, this.mFilesUri, FILES_PRESCAN_PROJECTION, "media_type=2", null, null, null);
            while (iterator.hasNext()) {
                FileEntry entry = iterator.next();
                if (entry.mLastModifiedChanged) {
                    processPlayList(entry, fileList2);
                }
            }
            if (fileList2 != null) {
                fileList2.close();
            }
        } catch (RemoteException e) {
            if (0 != 0) {
                fileList.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                fileList.close();
            }
            throw th;
        }
    }

    public void release() {
        native_finalize();
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        this.mContext.getContentResolver().releaseProvider(this.mMediaProvider);
        native_finalize();
    }
}

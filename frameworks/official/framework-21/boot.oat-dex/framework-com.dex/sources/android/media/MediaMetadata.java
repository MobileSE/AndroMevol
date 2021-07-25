package android.media;

import android.graphics.Bitmap;
import android.media.MediaDescription;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.SparseArray;
import java.util.Set;

public final class MediaMetadata implements Parcelable {
    public static final Parcelable.Creator<MediaMetadata> CREATOR = new Parcelable.Creator<MediaMetadata>() {
        /* class android.media.MediaMetadata.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public MediaMetadata createFromParcel(Parcel in) {
            return new MediaMetadata(in);
        }

        @Override // android.os.Parcelable.Creator
        public MediaMetadata[] newArray(int size) {
            return new MediaMetadata[size];
        }
    };
    private static final SparseArray<String> EDITOR_KEY_MAPPING = new SparseArray<>();
    private static final ArrayMap<String, Integer> METADATA_KEYS_TYPE = new ArrayMap<>();
    public static final String METADATA_KEY_ALBUM = "android.media.metadata.ALBUM";
    public static final String METADATA_KEY_ALBUM_ART = "android.media.metadata.ALBUM_ART";
    public static final String METADATA_KEY_ALBUM_ARTIST = "android.media.metadata.ALBUM_ARTIST";
    public static final String METADATA_KEY_ALBUM_ART_URI = "android.media.metadata.ALBUM_ART_URI";
    public static final String METADATA_KEY_ART = "android.media.metadata.ART";
    public static final String METADATA_KEY_ARTIST = "android.media.metadata.ARTIST";
    public static final String METADATA_KEY_ART_URI = "android.media.metadata.ART_URI";
    public static final String METADATA_KEY_AUTHOR = "android.media.metadata.AUTHOR";
    public static final String METADATA_KEY_COMPILATION = "android.media.metadata.COMPILATION";
    public static final String METADATA_KEY_COMPOSER = "android.media.metadata.COMPOSER";
    public static final String METADATA_KEY_DATE = "android.media.metadata.DATE";
    public static final String METADATA_KEY_DISC_NUMBER = "android.media.metadata.DISC_NUMBER";
    public static final String METADATA_KEY_DISPLAY_DESCRIPTION = "android.media.metadata.DISPLAY_DESCRIPTION";
    public static final String METADATA_KEY_DISPLAY_ICON = "android.media.metadata.DISPLAY_ICON";
    public static final String METADATA_KEY_DISPLAY_ICON_URI = "android.media.metadata.DISPLAY_ICON_URI";
    public static final String METADATA_KEY_DISPLAY_SUBTITLE = "android.media.metadata.DISPLAY_SUBTITLE";
    public static final String METADATA_KEY_DISPLAY_TITLE = "android.media.metadata.DISPLAY_TITLE";
    public static final String METADATA_KEY_DURATION = "android.media.metadata.DURATION";
    public static final String METADATA_KEY_GENRE = "android.media.metadata.GENRE";
    public static final String METADATA_KEY_MEDIA_ID = "android.media.metadata.MEDIA_ID";
    public static final String METADATA_KEY_NUM_TRACKS = "android.media.metadata.NUM_TRACKS";
    public static final String METADATA_KEY_RATING = "android.media.metadata.RATING";
    public static final String METADATA_KEY_TITLE = "android.media.metadata.TITLE";
    public static final String METADATA_KEY_TRACK_NUMBER = "android.media.metadata.TRACK_NUMBER";
    public static final String METADATA_KEY_USER_RATING = "android.media.metadata.USER_RATING";
    public static final String METADATA_KEY_WRITER = "android.media.metadata.WRITER";
    public static final String METADATA_KEY_YEAR = "android.media.metadata.YEAR";
    private static final int METADATA_TYPE_BITMAP = 2;
    private static final int METADATA_TYPE_INVALID = -1;
    private static final int METADATA_TYPE_LONG = 0;
    private static final int METADATA_TYPE_RATING = 3;
    private static final int METADATA_TYPE_TEXT = 1;
    private static final String[] PREFERRED_BITMAP_ORDER = {METADATA_KEY_DISPLAY_ICON, METADATA_KEY_ART, METADATA_KEY_ALBUM_ART};
    private static final String[] PREFERRED_DESCRIPTION_ORDER = {METADATA_KEY_TITLE, METADATA_KEY_ARTIST, METADATA_KEY_ALBUM, METADATA_KEY_ALBUM_ARTIST, METADATA_KEY_WRITER, METADATA_KEY_AUTHOR, METADATA_KEY_COMPOSER};
    private static final String[] PREFERRED_URI_ORDER = {METADATA_KEY_DISPLAY_ICON_URI, METADATA_KEY_ART_URI, METADATA_KEY_ALBUM_ART_URI};
    private static final String TAG = "MediaMetadata";
    private final Bundle mBundle;
    private MediaDescription mDescription;

    static {
        METADATA_KEYS_TYPE.put(METADATA_KEY_TITLE, 1);
        METADATA_KEYS_TYPE.put(METADATA_KEY_ARTIST, 1);
        METADATA_KEYS_TYPE.put(METADATA_KEY_DURATION, 0);
        METADATA_KEYS_TYPE.put(METADATA_KEY_ALBUM, 1);
        METADATA_KEYS_TYPE.put(METADATA_KEY_AUTHOR, 1);
        METADATA_KEYS_TYPE.put(METADATA_KEY_WRITER, 1);
        METADATA_KEYS_TYPE.put(METADATA_KEY_COMPOSER, 1);
        METADATA_KEYS_TYPE.put(METADATA_KEY_COMPILATION, 1);
        METADATA_KEYS_TYPE.put(METADATA_KEY_DATE, 1);
        METADATA_KEYS_TYPE.put(METADATA_KEY_YEAR, 0);
        METADATA_KEYS_TYPE.put(METADATA_KEY_GENRE, 1);
        METADATA_KEYS_TYPE.put(METADATA_KEY_TRACK_NUMBER, 0);
        METADATA_KEYS_TYPE.put(METADATA_KEY_NUM_TRACKS, 0);
        METADATA_KEYS_TYPE.put(METADATA_KEY_DISC_NUMBER, 0);
        METADATA_KEYS_TYPE.put(METADATA_KEY_ALBUM_ARTIST, 1);
        METADATA_KEYS_TYPE.put(METADATA_KEY_ART, 2);
        METADATA_KEYS_TYPE.put(METADATA_KEY_ART_URI, 1);
        METADATA_KEYS_TYPE.put(METADATA_KEY_ALBUM_ART, 2);
        METADATA_KEYS_TYPE.put(METADATA_KEY_ALBUM_ART_URI, 1);
        METADATA_KEYS_TYPE.put(METADATA_KEY_USER_RATING, 3);
        METADATA_KEYS_TYPE.put(METADATA_KEY_RATING, 3);
        METADATA_KEYS_TYPE.put(METADATA_KEY_DISPLAY_TITLE, 1);
        METADATA_KEYS_TYPE.put(METADATA_KEY_DISPLAY_SUBTITLE, 1);
        METADATA_KEYS_TYPE.put(METADATA_KEY_DISPLAY_DESCRIPTION, 1);
        METADATA_KEYS_TYPE.put(METADATA_KEY_DISPLAY_ICON, 2);
        METADATA_KEYS_TYPE.put(METADATA_KEY_DISPLAY_ICON_URI, 1);
        EDITOR_KEY_MAPPING.put(100, METADATA_KEY_ART);
        EDITOR_KEY_MAPPING.put(101, METADATA_KEY_RATING);
        EDITOR_KEY_MAPPING.put(MediaMetadataEditor.RATING_KEY_BY_USER, METADATA_KEY_USER_RATING);
        EDITOR_KEY_MAPPING.put(1, METADATA_KEY_ALBUM);
        EDITOR_KEY_MAPPING.put(13, METADATA_KEY_ALBUM_ARTIST);
        EDITOR_KEY_MAPPING.put(2, METADATA_KEY_ARTIST);
        EDITOR_KEY_MAPPING.put(3, METADATA_KEY_AUTHOR);
        EDITOR_KEY_MAPPING.put(0, METADATA_KEY_TRACK_NUMBER);
        EDITOR_KEY_MAPPING.put(4, METADATA_KEY_COMPOSER);
        EDITOR_KEY_MAPPING.put(15, METADATA_KEY_COMPILATION);
        EDITOR_KEY_MAPPING.put(5, METADATA_KEY_DATE);
        EDITOR_KEY_MAPPING.put(14, METADATA_KEY_DISC_NUMBER);
        EDITOR_KEY_MAPPING.put(9, METADATA_KEY_DURATION);
        EDITOR_KEY_MAPPING.put(6, METADATA_KEY_GENRE);
        EDITOR_KEY_MAPPING.put(10, METADATA_KEY_NUM_TRACKS);
        EDITOR_KEY_MAPPING.put(7, METADATA_KEY_TITLE);
        EDITOR_KEY_MAPPING.put(11, METADATA_KEY_WRITER);
        EDITOR_KEY_MAPPING.put(8, METADATA_KEY_YEAR);
    }

    private MediaMetadata(Bundle bundle) {
        this.mBundle = new Bundle(bundle);
    }

    private MediaMetadata(Parcel in) {
        this.mBundle = in.readBundle();
    }

    public boolean containsKey(String key) {
        return this.mBundle.containsKey(key);
    }

    public CharSequence getText(String key) {
        return this.mBundle.getCharSequence(key);
    }

    public String getString(String key) {
        CharSequence text = getText(key);
        if (text != null) {
            return text.toString();
        }
        return null;
    }

    public long getLong(String key) {
        return this.mBundle.getLong(key, 0);
    }

    public Rating getRating(String key) {
        try {
            return (Rating) this.mBundle.getParcelable(key);
        } catch (Exception e) {
            Log.w(TAG, "Failed to retrieve a key as Rating.", e);
            return null;
        }
    }

    public Bitmap getBitmap(String key) {
        try {
            return (Bitmap) this.mBundle.getParcelable(key);
        } catch (Exception e) {
            Log.w(TAG, "Failed to retrieve a key as Bitmap.", e);
            return null;
        }
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeBundle(this.mBundle);
    }

    public int size() {
        return this.mBundle.size();
    }

    public Set<String> keySet() {
        return this.mBundle.keySet();
    }

    public MediaDescription getDescription() {
        if (this.mDescription != null) {
            return this.mDescription;
        }
        String mediaId = getString(METADATA_KEY_MEDIA_ID);
        CharSequence[] text = new CharSequence[3];
        Bitmap icon = null;
        Uri iconUri = null;
        CharSequence displayText = getText(METADATA_KEY_DISPLAY_TITLE);
        if (!TextUtils.isEmpty(displayText)) {
            text[0] = displayText;
            text[1] = getText(METADATA_KEY_DISPLAY_SUBTITLE);
            text[2] = getText(METADATA_KEY_DISPLAY_DESCRIPTION);
        } else {
            int textIndex = 0;
            int keyIndex = 0;
            while (textIndex < text.length && keyIndex < PREFERRED_DESCRIPTION_ORDER.length) {
                int keyIndex2 = keyIndex + 1;
                CharSequence next = getText(PREFERRED_DESCRIPTION_ORDER[keyIndex]);
                if (!TextUtils.isEmpty(next)) {
                    text[textIndex] = next;
                    textIndex++;
                }
                keyIndex = keyIndex2;
            }
        }
        int i = 0;
        while (true) {
            if (i >= PREFERRED_BITMAP_ORDER.length) {
                break;
            }
            Bitmap next2 = getBitmap(PREFERRED_BITMAP_ORDER[i]);
            if (next2 != null) {
                icon = next2;
                break;
            }
            i++;
        }
        int i2 = 0;
        while (true) {
            if (i2 >= PREFERRED_URI_ORDER.length) {
                break;
            }
            String next3 = getString(PREFERRED_URI_ORDER[i2]);
            if (!TextUtils.isEmpty(next3)) {
                iconUri = Uri.parse(next3);
                break;
            }
            i2++;
        }
        MediaDescription.Builder bob = new MediaDescription.Builder();
        bob.setMediaId(mediaId);
        bob.setTitle(text[0]);
        bob.setSubtitle(text[1]);
        bob.setDescription(text[2]);
        bob.setIconBitmap(icon);
        bob.setIconUri(iconUri);
        this.mDescription = bob.build();
        return this.mDescription;
    }

    public static String getKeyFromMetadataEditorKey(int editorKey) {
        return EDITOR_KEY_MAPPING.get(editorKey, null);
    }

    public static final class Builder {
        private final Bundle mBundle;

        public Builder() {
            this.mBundle = new Bundle();
        }

        public Builder(MediaMetadata source) {
            this.mBundle = new Bundle(source.mBundle);
        }

        public Builder putText(String key, CharSequence value) {
            if (!MediaMetadata.METADATA_KEYS_TYPE.containsKey(key) || ((Integer) MediaMetadata.METADATA_KEYS_TYPE.get(key)).intValue() == 1) {
                this.mBundle.putCharSequence(key, value);
                return this;
            }
            throw new IllegalArgumentException("The " + key + " key cannot be used to put a CharSequence");
        }

        public Builder putString(String key, String value) {
            if (!MediaMetadata.METADATA_KEYS_TYPE.containsKey(key) || ((Integer) MediaMetadata.METADATA_KEYS_TYPE.get(key)).intValue() == 1) {
                this.mBundle.putCharSequence(key, value);
                return this;
            }
            throw new IllegalArgumentException("The " + key + " key cannot be used to put a String");
        }

        public Builder putLong(String key, long value) {
            if (!MediaMetadata.METADATA_KEYS_TYPE.containsKey(key) || ((Integer) MediaMetadata.METADATA_KEYS_TYPE.get(key)).intValue() == 0) {
                this.mBundle.putLong(key, value);
                return this;
            }
            throw new IllegalArgumentException("The " + key + " key cannot be used to put a long");
        }

        public Builder putRating(String key, Rating value) {
            if (!MediaMetadata.METADATA_KEYS_TYPE.containsKey(key) || ((Integer) MediaMetadata.METADATA_KEYS_TYPE.get(key)).intValue() == 3) {
                this.mBundle.putParcelable(key, value);
                return this;
            }
            throw new IllegalArgumentException("The " + key + " key cannot be used to put a Rating");
        }

        public Builder putBitmap(String key, Bitmap value) {
            if (!MediaMetadata.METADATA_KEYS_TYPE.containsKey(key) || ((Integer) MediaMetadata.METADATA_KEYS_TYPE.get(key)).intValue() == 2) {
                this.mBundle.putParcelable(key, value);
                return this;
            }
            throw new IllegalArgumentException("The " + key + " key cannot be used to put a Bitmap");
        }

        public MediaMetadata build() {
            return new MediaMetadata(this.mBundle);
        }
    }
}

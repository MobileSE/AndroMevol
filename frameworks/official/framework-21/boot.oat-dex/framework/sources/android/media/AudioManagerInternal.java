package android.media;

public abstract class AudioManagerInternal {
    public abstract void adjustStreamVolumeForUid(int i, int i2, int i3, String str, int i4);

    public abstract void adjustSuggestedStreamVolumeForUid(int i, int i2, int i3, String str, int i4);

    public abstract void setStreamVolumeForUid(int i, int i2, int i3, String str, int i4);
}

package android.hardware.soundtrigger;

import android.media.AudioFormat;
import android.net.ProxyInfo;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public class SoundTrigger {
    public static final int RECOGNITION_MODE_USER_AUTHENTICATION = 4;
    public static final int RECOGNITION_MODE_USER_IDENTIFICATION = 2;
    public static final int RECOGNITION_MODE_VOICE_TRIGGER = 1;
    public static final int RECOGNITION_STATUS_ABORT = 1;
    public static final int RECOGNITION_STATUS_FAILURE = 2;
    public static final int RECOGNITION_STATUS_SUCCESS = 0;
    public static final int SERVICE_STATE_DISABLED = 1;
    public static final int SERVICE_STATE_ENABLED = 0;
    public static final int SOUNDMODEL_STATUS_UPDATED = 0;
    public static final int STATUS_BAD_VALUE = -22;
    public static final int STATUS_DEAD_OBJECT = -32;
    public static final int STATUS_ERROR = Integer.MIN_VALUE;
    public static final int STATUS_INVALID_OPERATION = -38;
    public static final int STATUS_NO_INIT = -19;
    public static final int STATUS_OK = 0;
    public static final int STATUS_PERMISSION_DENIED = -1;

    public interface StatusListener {
        void onRecognition(RecognitionEvent recognitionEvent);

        void onServiceDied();

        void onServiceStateChange(int i);

        void onSoundModelUpdate(SoundModelEvent soundModelEvent);
    }

    public static native int listModules(ArrayList<ModuleProperties> arrayList);

    public static class ModuleProperties implements Parcelable {
        public static final Parcelable.Creator<ModuleProperties> CREATOR = new Parcelable.Creator<ModuleProperties>() {
            /* class android.hardware.soundtrigger.SoundTrigger.ModuleProperties.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public ModuleProperties createFromParcel(Parcel in) {
                return ModuleProperties.fromParcel(in);
            }

            @Override // android.os.Parcelable.Creator
            public ModuleProperties[] newArray(int size) {
                return new ModuleProperties[size];
            }
        };
        public final String description;
        public final int id;
        public final String implementor;
        public final int maxBufferMs;
        public final int maxKeyphrases;
        public final int maxSoundModels;
        public final int maxUsers;
        public final int powerConsumptionMw;
        public final int recognitionModes;
        public final boolean returnsTriggerInEvent;
        public final boolean supportsCaptureTransition;
        public final boolean supportsConcurrentCapture;
        public final UUID uuid;
        public final int version;

        ModuleProperties(int id2, String implementor2, String description2, String uuid2, int version2, int maxSoundModels2, int maxKeyphrases2, int maxUsers2, int recognitionModes2, boolean supportsCaptureTransition2, int maxBufferMs2, boolean supportsConcurrentCapture2, int powerConsumptionMw2, boolean returnsTriggerInEvent2) {
            this.id = id2;
            this.implementor = implementor2;
            this.description = description2;
            this.uuid = UUID.fromString(uuid2);
            this.version = version2;
            this.maxSoundModels = maxSoundModels2;
            this.maxKeyphrases = maxKeyphrases2;
            this.maxUsers = maxUsers2;
            this.recognitionModes = recognitionModes2;
            this.supportsCaptureTransition = supportsCaptureTransition2;
            this.maxBufferMs = maxBufferMs2;
            this.supportsConcurrentCapture = supportsConcurrentCapture2;
            this.powerConsumptionMw = powerConsumptionMw2;
            this.returnsTriggerInEvent = returnsTriggerInEvent2;
        }

        /* access modifiers changed from: private */
        public static ModuleProperties fromParcel(Parcel in) {
            return new ModuleProperties(in.readInt(), in.readString(), in.readString(), in.readString(), in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.readByte() == 1, in.readInt(), in.readByte() == 1, in.readInt(), in.readByte() == 1);
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            int i;
            int i2 = 1;
            dest.writeInt(this.id);
            dest.writeString(this.implementor);
            dest.writeString(this.description);
            dest.writeString(this.uuid.toString());
            dest.writeInt(this.version);
            dest.writeInt(this.maxSoundModels);
            dest.writeInt(this.maxKeyphrases);
            dest.writeInt(this.maxUsers);
            dest.writeInt(this.recognitionModes);
            dest.writeByte((byte) (this.supportsCaptureTransition ? 1 : 0));
            dest.writeInt(this.maxBufferMs);
            if (this.supportsConcurrentCapture) {
                i = 1;
            } else {
                i = 0;
            }
            dest.writeByte((byte) i);
            dest.writeInt(this.powerConsumptionMw);
            if (!this.returnsTriggerInEvent) {
                i2 = 0;
            }
            dest.writeByte((byte) i2);
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        public String toString() {
            return "ModuleProperties [id=" + this.id + ", implementor=" + this.implementor + ", description=" + this.description + ", uuid=" + this.uuid + ", version=" + this.version + ", maxSoundModels=" + this.maxSoundModels + ", maxKeyphrases=" + this.maxKeyphrases + ", maxUsers=" + this.maxUsers + ", recognitionModes=" + this.recognitionModes + ", supportsCaptureTransition=" + this.supportsCaptureTransition + ", maxBufferMs=" + this.maxBufferMs + ", supportsConcurrentCapture=" + this.supportsConcurrentCapture + ", powerConsumptionMw=" + this.powerConsumptionMw + ", returnsTriggerInEvent=" + this.returnsTriggerInEvent + "]";
        }
    }

    public static class SoundModel {
        public static final int TYPE_KEYPHRASE = 0;
        public static final int TYPE_UNKNOWN = -1;
        public final byte[] data;
        public final int type;
        public final UUID uuid;
        public final UUID vendorUuid;

        public SoundModel(UUID uuid2, UUID vendorUuid2, int type2, byte[] data2) {
            this.uuid = uuid2;
            this.vendorUuid = vendorUuid2;
            this.type = type2;
            this.data = data2;
        }
    }

    public static class Keyphrase implements Parcelable {
        public static final Parcelable.Creator<Keyphrase> CREATOR = new Parcelable.Creator<Keyphrase>() {
            /* class android.hardware.soundtrigger.SoundTrigger.Keyphrase.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public Keyphrase createFromParcel(Parcel in) {
                return Keyphrase.fromParcel(in);
            }

            @Override // android.os.Parcelable.Creator
            public Keyphrase[] newArray(int size) {
                return new Keyphrase[size];
            }
        };
        public final int id;
        public final String locale;
        public final int recognitionModes;
        public final String text;
        public final int[] users;

        public Keyphrase(int id2, int recognitionModes2, String locale2, String text2, int[] users2) {
            this.id = id2;
            this.recognitionModes = recognitionModes2;
            this.locale = locale2;
            this.text = text2;
            this.users = users2;
        }

        /* access modifiers changed from: private */
        public static Keyphrase fromParcel(Parcel in) {
            int id2 = in.readInt();
            int recognitionModes2 = in.readInt();
            String locale2 = in.readString();
            String text2 = in.readString();
            int[] users2 = null;
            int numUsers = in.readInt();
            if (numUsers >= 0) {
                users2 = new int[numUsers];
                in.readIntArray(users2);
            }
            return new Keyphrase(id2, recognitionModes2, locale2, text2, users2);
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.id);
            dest.writeInt(this.recognitionModes);
            dest.writeString(this.locale);
            dest.writeString(this.text);
            if (this.users != null) {
                dest.writeInt(this.users.length);
                dest.writeIntArray(this.users);
                return;
            }
            dest.writeInt(-1);
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        public int hashCode() {
            int i = 0;
            int hashCode = ((((this.text == null ? 0 : this.text.hashCode()) + 31) * 31) + this.id) * 31;
            if (this.locale != null) {
                i = this.locale.hashCode();
            }
            return ((((hashCode + i) * 31) + this.recognitionModes) * 31) + Arrays.hashCode(this.users);
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Keyphrase other = (Keyphrase) obj;
            if (this.text == null) {
                if (other.text != null) {
                    return false;
                }
            } else if (!this.text.equals(other.text)) {
                return false;
            }
            if (this.id != other.id) {
                return false;
            }
            if (this.locale == null) {
                if (other.locale != null) {
                    return false;
                }
            } else if (!this.locale.equals(other.locale)) {
                return false;
            }
            if (this.recognitionModes != other.recognitionModes) {
                return false;
            }
            return Arrays.equals(this.users, other.users);
        }

        public String toString() {
            return "Keyphrase [id=" + this.id + ", recognitionModes=" + this.recognitionModes + ", locale=" + this.locale + ", text=" + this.text + ", users=" + Arrays.toString(this.users) + "]";
        }
    }

    public static class KeyphraseSoundModel extends SoundModel implements Parcelable {
        public static final Parcelable.Creator<KeyphraseSoundModel> CREATOR = new Parcelable.Creator<KeyphraseSoundModel>() {
            /* class android.hardware.soundtrigger.SoundTrigger.KeyphraseSoundModel.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public KeyphraseSoundModel createFromParcel(Parcel in) {
                return KeyphraseSoundModel.fromParcel(in);
            }

            @Override // android.os.Parcelable.Creator
            public KeyphraseSoundModel[] newArray(int size) {
                return new KeyphraseSoundModel[size];
            }
        };
        public final Keyphrase[] keyphrases;

        public KeyphraseSoundModel(UUID uuid, UUID vendorUuid, byte[] data, Keyphrase[] keyphrases2) {
            super(uuid, vendorUuid, 0, data);
            this.keyphrases = keyphrases2;
        }

        /* access modifiers changed from: private */
        public static KeyphraseSoundModel fromParcel(Parcel in) {
            UUID uuid = UUID.fromString(in.readString());
            UUID vendorUuid = null;
            if (in.readInt() >= 0) {
                vendorUuid = UUID.fromString(in.readString());
            }
            return new KeyphraseSoundModel(uuid, vendorUuid, in.readBlob(), (Keyphrase[]) in.createTypedArray(Keyphrase.CREATOR));
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.uuid.toString());
            if (this.vendorUuid == null) {
                dest.writeInt(-1);
            } else {
                dest.writeInt(this.vendorUuid.toString().length());
                dest.writeString(this.vendorUuid.toString());
            }
            dest.writeBlob(this.data);
            dest.writeTypedArray(this.keyphrases, flags);
        }

        public String toString() {
            return "KeyphraseSoundModel [keyphrases=" + Arrays.toString(this.keyphrases) + ", uuid=" + this.uuid + ", vendorUuid=" + this.vendorUuid + ", type=" + this.type + ", data=" + (this.data == null ? 0 : this.data.length) + "]";
        }
    }

    public static class RecognitionEvent implements Parcelable {
        public static final Parcelable.Creator<RecognitionEvent> CREATOR = new Parcelable.Creator<RecognitionEvent>() {
            /* class android.hardware.soundtrigger.SoundTrigger.RecognitionEvent.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public RecognitionEvent createFromParcel(Parcel in) {
                return RecognitionEvent.fromParcel(in);
            }

            @Override // android.os.Parcelable.Creator
            public RecognitionEvent[] newArray(int size) {
                return new RecognitionEvent[size];
            }
        };
        public final boolean captureAvailable;
        public final int captureDelayMs;
        public AudioFormat captureFormat;
        public final int capturePreambleMs;
        public final int captureSession;
        public final byte[] data;
        public final int soundModelHandle;
        public final int status;
        public final boolean triggerInData;

        public RecognitionEvent(int status2, int soundModelHandle2, boolean captureAvailable2, int captureSession2, int captureDelayMs2, int capturePreambleMs2, boolean triggerInData2, AudioFormat captureFormat2, byte[] data2) {
            this.status = status2;
            this.soundModelHandle = soundModelHandle2;
            this.captureAvailable = captureAvailable2;
            this.captureSession = captureSession2;
            this.captureDelayMs = captureDelayMs2;
            this.capturePreambleMs = capturePreambleMs2;
            this.triggerInData = triggerInData2;
            this.captureFormat = captureFormat2;
            this.data = data2;
        }

        /* access modifiers changed from: private */
        public static RecognitionEvent fromParcel(Parcel in) {
            boolean captureAvailable2;
            boolean triggerInData2;
            int status2 = in.readInt();
            int soundModelHandle2 = in.readInt();
            if (in.readByte() == 1) {
                captureAvailable2 = true;
            } else {
                captureAvailable2 = false;
            }
            int captureSession2 = in.readInt();
            int captureDelayMs2 = in.readInt();
            int capturePreambleMs2 = in.readInt();
            if (in.readByte() == 1) {
                triggerInData2 = true;
            } else {
                triggerInData2 = false;
            }
            AudioFormat captureFormat2 = null;
            if (in.readByte() == 1) {
                captureFormat2 = new AudioFormat.Builder().setChannelMask(in.readInt()).setEncoding(in.readInt()).setSampleRate(in.readInt()).build();
            }
            return new RecognitionEvent(status2, soundModelHandle2, captureAvailable2, captureSession2, captureDelayMs2, capturePreambleMs2, triggerInData2, captureFormat2, in.readBlob());
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            int i;
            dest.writeInt(this.status);
            dest.writeInt(this.soundModelHandle);
            dest.writeByte((byte) (this.captureAvailable ? 1 : 0));
            dest.writeInt(this.captureSession);
            dest.writeInt(this.captureDelayMs);
            dest.writeInt(this.capturePreambleMs);
            if (this.triggerInData) {
                i = 1;
            } else {
                i = 0;
            }
            dest.writeByte((byte) i);
            if (this.captureFormat != null) {
                dest.writeByte((byte) 1);
                dest.writeInt(this.captureFormat.getSampleRate());
                dest.writeInt(this.captureFormat.getEncoding());
                dest.writeInt(this.captureFormat.getChannelMask());
            } else {
                dest.writeByte((byte) 0);
            }
            dest.writeBlob(this.data);
        }

        public int hashCode() {
            int i = 1231;
            int i2 = ((((((((this.captureAvailable ? 1231 : 1237) + 31) * 31) + this.captureDelayMs) * 31) + this.capturePreambleMs) * 31) + this.captureSession) * 31;
            if (!this.triggerInData) {
                i = 1237;
            }
            int result = i2 + i;
            if (this.captureFormat != null) {
                result = (((((result * 31) + this.captureFormat.getSampleRate()) * 31) + this.captureFormat.getEncoding()) * 31) + this.captureFormat.getChannelMask();
            }
            return (((((result * 31) + Arrays.hashCode(this.data)) * 31) + this.soundModelHandle) * 31) + this.status;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            RecognitionEvent other = (RecognitionEvent) obj;
            if (this.captureAvailable != other.captureAvailable) {
                return false;
            }
            if (this.captureDelayMs != other.captureDelayMs) {
                return false;
            }
            if (this.capturePreambleMs != other.capturePreambleMs) {
                return false;
            }
            if (this.captureSession != other.captureSession) {
                return false;
            }
            if (!Arrays.equals(this.data, other.data)) {
                return false;
            }
            if (this.soundModelHandle != other.soundModelHandle) {
                return false;
            }
            if (this.status != other.status) {
                return false;
            }
            if (this.triggerInData != other.triggerInData) {
                return false;
            }
            if (this.captureFormat.getSampleRate() != other.captureFormat.getSampleRate()) {
                return false;
            }
            if (this.captureFormat.getEncoding() != other.captureFormat.getEncoding()) {
                return false;
            }
            return this.captureFormat.getChannelMask() == other.captureFormat.getChannelMask();
        }

        public String toString() {
            return "RecognitionEvent [status=" + this.status + ", soundModelHandle=" + this.soundModelHandle + ", captureAvailable=" + this.captureAvailable + ", captureSession=" + this.captureSession + ", captureDelayMs=" + this.captureDelayMs + ", capturePreambleMs=" + this.capturePreambleMs + ", triggerInData=" + this.triggerInData + (this.captureFormat == null ? ProxyInfo.LOCAL_EXCL_LIST : ", sampleRate=" + this.captureFormat.getSampleRate()) + (this.captureFormat == null ? ProxyInfo.LOCAL_EXCL_LIST : ", encoding=" + this.captureFormat.getEncoding()) + (this.captureFormat == null ? ProxyInfo.LOCAL_EXCL_LIST : ", channelMask=" + this.captureFormat.getChannelMask()) + ", data=" + (this.data == null ? 0 : this.data.length) + "]";
        }
    }

    public static class RecognitionConfig implements Parcelable {
        public static final Parcelable.Creator<RecognitionConfig> CREATOR = new Parcelable.Creator<RecognitionConfig>() {
            /* class android.hardware.soundtrigger.SoundTrigger.RecognitionConfig.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public RecognitionConfig createFromParcel(Parcel in) {
                return RecognitionConfig.fromParcel(in);
            }

            @Override // android.os.Parcelable.Creator
            public RecognitionConfig[] newArray(int size) {
                return new RecognitionConfig[size];
            }
        };
        public final boolean allowMultipleTriggers;
        public final boolean captureRequested;
        public final byte[] data;
        public final KeyphraseRecognitionExtra[] keyphrases;

        public RecognitionConfig(boolean captureRequested2, boolean allowMultipleTriggers2, KeyphraseRecognitionExtra[] keyphrases2, byte[] data2) {
            this.captureRequested = captureRequested2;
            this.allowMultipleTriggers = allowMultipleTriggers2;
            this.keyphrases = keyphrases2;
            this.data = data2;
        }

        /* access modifiers changed from: private */
        public static RecognitionConfig fromParcel(Parcel in) {
            boolean captureRequested2;
            boolean allowMultipleTriggers2;
            if (in.readByte() == 1) {
                captureRequested2 = true;
            } else {
                captureRequested2 = false;
            }
            if (in.readByte() == 1) {
                allowMultipleTriggers2 = true;
            } else {
                allowMultipleTriggers2 = false;
            }
            return new RecognitionConfig(captureRequested2, allowMultipleTriggers2, (KeyphraseRecognitionExtra[]) in.createTypedArray(KeyphraseRecognitionExtra.CREATOR), in.readBlob());
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            int i;
            int i2 = 1;
            if (this.captureRequested) {
                i = 1;
            } else {
                i = 0;
            }
            dest.writeByte((byte) i);
            if (!this.allowMultipleTriggers) {
                i2 = 0;
            }
            dest.writeByte((byte) i2);
            dest.writeTypedArray(this.keyphrases, flags);
            dest.writeBlob(this.data);
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        public String toString() {
            return "RecognitionConfig [captureRequested=" + this.captureRequested + ", allowMultipleTriggers=" + this.allowMultipleTriggers + ", keyphrases=" + Arrays.toString(this.keyphrases) + ", data=" + Arrays.toString(this.data) + "]";
        }
    }

    public static class ConfidenceLevel implements Parcelable {
        public static final Parcelable.Creator<ConfidenceLevel> CREATOR = new Parcelable.Creator<ConfidenceLevel>() {
            /* class android.hardware.soundtrigger.SoundTrigger.ConfidenceLevel.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public ConfidenceLevel createFromParcel(Parcel in) {
                return ConfidenceLevel.fromParcel(in);
            }

            @Override // android.os.Parcelable.Creator
            public ConfidenceLevel[] newArray(int size) {
                return new ConfidenceLevel[size];
            }
        };
        public final int confidenceLevel;
        public final int userId;

        public ConfidenceLevel(int userId2, int confidenceLevel2) {
            this.userId = userId2;
            this.confidenceLevel = confidenceLevel2;
        }

        /* access modifiers changed from: private */
        public static ConfidenceLevel fromParcel(Parcel in) {
            return new ConfidenceLevel(in.readInt(), in.readInt());
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.userId);
            dest.writeInt(this.confidenceLevel);
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        public int hashCode() {
            return ((this.confidenceLevel + 31) * 31) + this.userId;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            ConfidenceLevel other = (ConfidenceLevel) obj;
            if (this.confidenceLevel != other.confidenceLevel) {
                return false;
            }
            return this.userId == other.userId;
        }

        public String toString() {
            return "ConfidenceLevel [userId=" + this.userId + ", confidenceLevel=" + this.confidenceLevel + "]";
        }
    }

    public static class KeyphraseRecognitionExtra implements Parcelable {
        public static final Parcelable.Creator<KeyphraseRecognitionExtra> CREATOR = new Parcelable.Creator<KeyphraseRecognitionExtra>() {
            /* class android.hardware.soundtrigger.SoundTrigger.KeyphraseRecognitionExtra.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public KeyphraseRecognitionExtra createFromParcel(Parcel in) {
                return KeyphraseRecognitionExtra.fromParcel(in);
            }

            @Override // android.os.Parcelable.Creator
            public KeyphraseRecognitionExtra[] newArray(int size) {
                return new KeyphraseRecognitionExtra[size];
            }
        };
        public final int coarseConfidenceLevel;
        public final ConfidenceLevel[] confidenceLevels;
        public final int id;
        public final int recognitionModes;

        public KeyphraseRecognitionExtra(int id2, int recognitionModes2, int coarseConfidenceLevel2, ConfidenceLevel[] confidenceLevels2) {
            this.id = id2;
            this.recognitionModes = recognitionModes2;
            this.coarseConfidenceLevel = coarseConfidenceLevel2;
            this.confidenceLevels = confidenceLevels2;
        }

        /* access modifiers changed from: private */
        public static KeyphraseRecognitionExtra fromParcel(Parcel in) {
            return new KeyphraseRecognitionExtra(in.readInt(), in.readInt(), in.readInt(), (ConfidenceLevel[]) in.createTypedArray(ConfidenceLevel.CREATOR));
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.id);
            dest.writeInt(this.recognitionModes);
            dest.writeInt(this.coarseConfidenceLevel);
            dest.writeTypedArray(this.confidenceLevels, flags);
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        public int hashCode() {
            return ((((((Arrays.hashCode(this.confidenceLevels) + 31) * 31) + this.id) * 31) + this.recognitionModes) * 31) + this.coarseConfidenceLevel;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            KeyphraseRecognitionExtra other = (KeyphraseRecognitionExtra) obj;
            if (!Arrays.equals(this.confidenceLevels, other.confidenceLevels)) {
                return false;
            }
            if (this.id != other.id) {
                return false;
            }
            if (this.recognitionModes != other.recognitionModes) {
                return false;
            }
            return this.coarseConfidenceLevel == other.coarseConfidenceLevel;
        }

        public String toString() {
            return "KeyphraseRecognitionExtra [id=" + this.id + ", recognitionModes=" + this.recognitionModes + ", coarseConfidenceLevel=" + this.coarseConfidenceLevel + ", confidenceLevels=" + Arrays.toString(this.confidenceLevels) + "]";
        }
    }

    public static class KeyphraseRecognitionEvent extends RecognitionEvent {
        public static final Parcelable.Creator<KeyphraseRecognitionEvent> CREATOR = new Parcelable.Creator<KeyphraseRecognitionEvent>() {
            /* class android.hardware.soundtrigger.SoundTrigger.KeyphraseRecognitionEvent.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public KeyphraseRecognitionEvent createFromParcel(Parcel in) {
                return KeyphraseRecognitionEvent.fromParcel(in);
            }

            @Override // android.os.Parcelable.Creator
            public KeyphraseRecognitionEvent[] newArray(int size) {
                return new KeyphraseRecognitionEvent[size];
            }
        };
        public final KeyphraseRecognitionExtra[] keyphraseExtras;

        public KeyphraseRecognitionEvent(int status, int soundModelHandle, boolean captureAvailable, int captureSession, int captureDelayMs, int capturePreambleMs, boolean triggerInData, AudioFormat captureFormat, byte[] data, KeyphraseRecognitionExtra[] keyphraseExtras2) {
            super(status, soundModelHandle, captureAvailable, captureSession, captureDelayMs, capturePreambleMs, triggerInData, captureFormat, data);
            this.keyphraseExtras = keyphraseExtras2;
        }

        /* access modifiers changed from: private */
        public static KeyphraseRecognitionEvent fromParcel(Parcel in) {
            int status = in.readInt();
            int soundModelHandle = in.readInt();
            boolean captureAvailable = in.readByte() == 1;
            int captureSession = in.readInt();
            int captureDelayMs = in.readInt();
            int capturePreambleMs = in.readInt();
            boolean triggerInData = in.readByte() == 1;
            AudioFormat captureFormat = null;
            if (in.readByte() == 1) {
                captureFormat = new AudioFormat.Builder().setChannelMask(in.readInt()).setEncoding(in.readInt()).setSampleRate(in.readInt()).build();
            }
            return new KeyphraseRecognitionEvent(status, soundModelHandle, captureAvailable, captureSession, captureDelayMs, capturePreambleMs, triggerInData, captureFormat, in.readBlob(), (KeyphraseRecognitionExtra[]) in.createTypedArray(KeyphraseRecognitionExtra.CREATOR));
        }

        @Override // android.os.Parcelable, android.hardware.soundtrigger.SoundTrigger.RecognitionEvent
        public void writeToParcel(Parcel dest, int flags) {
            int i;
            dest.writeInt(this.status);
            dest.writeInt(this.soundModelHandle);
            dest.writeByte((byte) (this.captureAvailable ? 1 : 0));
            dest.writeInt(this.captureSession);
            dest.writeInt(this.captureDelayMs);
            dest.writeInt(this.capturePreambleMs);
            if (this.triggerInData) {
                i = 1;
            } else {
                i = 0;
            }
            dest.writeByte((byte) i);
            if (this.captureFormat != null) {
                dest.writeByte((byte) 1);
                dest.writeInt(this.captureFormat.getSampleRate());
                dest.writeInt(this.captureFormat.getEncoding());
                dest.writeInt(this.captureFormat.getChannelMask());
            } else {
                dest.writeByte((byte) 0);
            }
            dest.writeBlob(this.data);
            dest.writeTypedArray(this.keyphraseExtras, flags);
        }

        @Override // android.os.Parcelable, android.hardware.soundtrigger.SoundTrigger.RecognitionEvent
        public int describeContents() {
            return 0;
        }

        @Override // android.hardware.soundtrigger.SoundTrigger.RecognitionEvent
        public int hashCode() {
            return (super.hashCode() * 31) + Arrays.hashCode(this.keyphraseExtras);
        }

        @Override // android.hardware.soundtrigger.SoundTrigger.RecognitionEvent
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!super.equals(obj)) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            return Arrays.equals(this.keyphraseExtras, ((KeyphraseRecognitionEvent) obj).keyphraseExtras);
        }

        @Override // android.hardware.soundtrigger.SoundTrigger.RecognitionEvent
        public String toString() {
            return "KeyphraseRecognitionEvent [keyphraseExtras=" + Arrays.toString(this.keyphraseExtras) + ", status=" + this.status + ", soundModelHandle=" + this.soundModelHandle + ", captureAvailable=" + this.captureAvailable + ", captureSession=" + this.captureSession + ", captureDelayMs=" + this.captureDelayMs + ", capturePreambleMs=" + this.capturePreambleMs + ", triggerInData=" + this.triggerInData + (this.captureFormat == null ? ProxyInfo.LOCAL_EXCL_LIST : ", sampleRate=" + this.captureFormat.getSampleRate()) + (this.captureFormat == null ? ProxyInfo.LOCAL_EXCL_LIST : ", encoding=" + this.captureFormat.getEncoding()) + (this.captureFormat == null ? ProxyInfo.LOCAL_EXCL_LIST : ", channelMask=" + this.captureFormat.getChannelMask()) + ", data=" + (this.data == null ? 0 : this.data.length) + "]";
        }
    }

    public static class SoundModelEvent implements Parcelable {
        public static final Parcelable.Creator<SoundModelEvent> CREATOR = new Parcelable.Creator<SoundModelEvent>() {
            /* class android.hardware.soundtrigger.SoundTrigger.SoundModelEvent.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public SoundModelEvent createFromParcel(Parcel in) {
                return SoundModelEvent.fromParcel(in);
            }

            @Override // android.os.Parcelable.Creator
            public SoundModelEvent[] newArray(int size) {
                return new SoundModelEvent[size];
            }
        };
        public final byte[] data;
        public final int soundModelHandle;
        public final int status;

        SoundModelEvent(int status2, int soundModelHandle2, byte[] data2) {
            this.status = status2;
            this.soundModelHandle = soundModelHandle2;
            this.data = data2;
        }

        /* access modifiers changed from: private */
        public static SoundModelEvent fromParcel(Parcel in) {
            return new SoundModelEvent(in.readInt(), in.readInt(), in.readBlob());
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.status);
            dest.writeInt(this.soundModelHandle);
            dest.writeBlob(this.data);
        }

        public int hashCode() {
            return ((((Arrays.hashCode(this.data) + 31) * 31) + this.soundModelHandle) * 31) + this.status;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            SoundModelEvent other = (SoundModelEvent) obj;
            if (!Arrays.equals(this.data, other.data)) {
                return false;
            }
            if (this.soundModelHandle != other.soundModelHandle) {
                return false;
            }
            return this.status == other.status;
        }

        public String toString() {
            return "SoundModelEvent [status=" + this.status + ", soundModelHandle=" + this.soundModelHandle + ", data=" + (this.data == null ? 0 : this.data.length) + "]";
        }
    }

    public static SoundTriggerModule attachModule(int moduleId, StatusListener listener, Handler handler) {
        if (listener == null) {
            return null;
        }
        return new SoundTriggerModule(moduleId, listener, handler);
    }
}

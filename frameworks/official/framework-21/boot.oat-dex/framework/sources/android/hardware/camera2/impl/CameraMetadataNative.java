package android.hardware.camera2.impl;

import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.marshal.MarshalQueryable;
import android.hardware.camera2.marshal.MarshalRegistry;
import android.hardware.camera2.marshal.Marshaler;
import android.hardware.camera2.marshal.impl.MarshalQueryableArray;
import android.hardware.camera2.marshal.impl.MarshalQueryableBlackLevelPattern;
import android.hardware.camera2.marshal.impl.MarshalQueryableBoolean;
import android.hardware.camera2.marshal.impl.MarshalQueryableColorSpaceTransform;
import android.hardware.camera2.marshal.impl.MarshalQueryableEnum;
import android.hardware.camera2.marshal.impl.MarshalQueryableHighSpeedVideoConfiguration;
import android.hardware.camera2.marshal.impl.MarshalQueryableMeteringRectangle;
import android.hardware.camera2.marshal.impl.MarshalQueryableNativeByteToInteger;
import android.hardware.camera2.marshal.impl.MarshalQueryablePair;
import android.hardware.camera2.marshal.impl.MarshalQueryableParcelable;
import android.hardware.camera2.marshal.impl.MarshalQueryablePrimitive;
import android.hardware.camera2.marshal.impl.MarshalQueryableRange;
import android.hardware.camera2.marshal.impl.MarshalQueryableRect;
import android.hardware.camera2.marshal.impl.MarshalQueryableReprocessFormatsMap;
import android.hardware.camera2.marshal.impl.MarshalQueryableRggbChannelVector;
import android.hardware.camera2.marshal.impl.MarshalQueryableSize;
import android.hardware.camera2.marshal.impl.MarshalQueryableSizeF;
import android.hardware.camera2.marshal.impl.MarshalQueryableStreamConfiguration;
import android.hardware.camera2.marshal.impl.MarshalQueryableStreamConfigurationDuration;
import android.hardware.camera2.marshal.impl.MarshalQueryableString;
import android.hardware.camera2.params.Face;
import android.hardware.camera2.params.HighSpeedVideoConfiguration;
import android.hardware.camera2.params.LensShadingMap;
import android.hardware.camera2.params.StreamConfiguration;
import android.hardware.camera2.params.StreamConfigurationDuration;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.hardware.camera2.params.TonemapCurve;
import android.hardware.camera2.utils.TypeReference;
import android.location.Location;
import android.location.LocationManager;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.util.Size;
import com.android.internal.util.Preconditions;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;

public class CameraMetadataNative implements Parcelable {
    private static final String CELLID_PROCESS = "CELLID";
    public static final Parcelable.Creator<CameraMetadataNative> CREATOR = new Parcelable.Creator<CameraMetadataNative>() {
        /* class android.hardware.camera2.impl.CameraMetadataNative.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public CameraMetadataNative createFromParcel(Parcel in) {
            CameraMetadataNative metadata = new CameraMetadataNative();
            metadata.readFromParcel(in);
            return metadata;
        }

        @Override // android.os.Parcelable.Creator
        public CameraMetadataNative[] newArray(int size) {
            return new CameraMetadataNative[size];
        }
    };
    private static final int FACE_LANDMARK_SIZE = 6;
    private static final String GPS_PROCESS = "GPS";
    public static final int NATIVE_JPEG_FORMAT = 33;
    public static final int NUM_TYPES = 6;
    private static final String TAG = "CameraMetadataJV";
    public static final int TYPE_BYTE = 0;
    public static final int TYPE_DOUBLE = 4;
    public static final int TYPE_FLOAT = 2;
    public static final int TYPE_INT32 = 1;
    public static final int TYPE_INT64 = 3;
    public static final int TYPE_RATIONAL = 5;
    private static final boolean VERBOSE = Log.isLoggable(TAG, 2);
    private static final HashMap<Key<?>, GetCommand> sGetCommandMap = new HashMap<>();
    private static final HashMap<Key<?>, SetCommand> sSetCommandMap = new HashMap<>();
    private long mMetadataPtr;

    private native long nativeAllocate();

    private native long nativeAllocateCopy(CameraMetadataNative cameraMetadataNative) throws NullPointerException;

    private static native void nativeClassInit();

    private native synchronized void nativeClose();

    private native synchronized void nativeDump() throws IOException;

    private native synchronized int nativeGetEntryCount();

    private static native int nativeGetTagFromKey(String str) throws IllegalArgumentException;

    private static native int nativeGetTypeFromTag(int i) throws IllegalArgumentException;

    private native synchronized boolean nativeIsEmpty();

    private native synchronized void nativeReadFromParcel(Parcel parcel);

    private native synchronized byte[] nativeReadValues(int i);

    public static native int nativeSetupGlobalVendorTagDescriptor();

    private native synchronized void nativeSwap(CameraMetadataNative cameraMetadataNative) throws NullPointerException;

    private native synchronized void nativeWriteToParcel(Parcel parcel);

    private native synchronized void nativeWriteValues(int i, byte[] bArr);

    public static class Key<T> {
        private boolean mHasTag;
        private final int mHash;
        private final String mName;
        private int mTag;
        private final Class<T> mType;
        private final TypeReference<T> mTypeReference;

        public Key(String name, Class<T> type) {
            if (name == null) {
                throw new NullPointerException("Key needs a valid name");
            } else if (type == null) {
                throw new NullPointerException("Type needs to be non-null");
            } else {
                this.mName = name;
                this.mType = type;
                this.mTypeReference = TypeReference.createSpecializedTypeReference((Class) type);
                this.mHash = this.mName.hashCode() ^ this.mTypeReference.hashCode();
            }
        }

        /* JADX DEBUG: Type inference failed for r0v0. Raw type applied. Possible types: java.lang.Class<? super T>, java.lang.Class<T> */
        public Key(String name, TypeReference<T> typeReference) {
            if (name == null) {
                throw new NullPointerException("Key needs a valid name");
            } else if (typeReference == null) {
                throw new NullPointerException("TypeReference needs to be non-null");
            } else {
                this.mName = name;
                this.mType = (Class<? super T>) typeReference.getRawType();
                this.mTypeReference = typeReference;
                this.mHash = this.mName.hashCode() ^ this.mTypeReference.hashCode();
            }
        }

        public final String getName() {
            return this.mName;
        }

        public final int hashCode() {
            return this.mHash;
        }

        public final boolean equals(Object o) {
            Key<?> lhs;
            if (this == o) {
                return true;
            }
            if (o == null || hashCode() != o.hashCode()) {
                return false;
            }
            if (o instanceof CaptureResult.Key) {
                lhs = ((CaptureResult.Key) o).getNativeKey();
            } else if (o instanceof CaptureRequest.Key) {
                lhs = ((CaptureRequest.Key) o).getNativeKey();
            } else if (o instanceof CameraCharacteristics.Key) {
                lhs = ((CameraCharacteristics.Key) o).getNativeKey();
            } else if (!(o instanceof Key)) {
                return false;
            } else {
                lhs = (Key) o;
            }
            return this.mName.equals(lhs.mName) && this.mTypeReference.equals(lhs.mTypeReference);
        }

        public final int getTag() {
            if (!this.mHasTag) {
                this.mTag = CameraMetadataNative.getTag(this.mName);
                this.mHasTag = true;
            }
            return this.mTag;
        }

        public final Class<T> getType() {
            return this.mType;
        }

        public final TypeReference<T> getTypeReference() {
            return this.mTypeReference;
        }
    }

    static {
        sGetCommandMap.put(CameraCharacteristics.SCALER_AVAILABLE_FORMATS.getNativeKey(), new GetCommand() {
            /* class android.hardware.camera2.impl.CameraMetadataNative.AnonymousClass2 */

            @Override // android.hardware.camera2.impl.GetCommand
            public <T> T getValue(CameraMetadataNative metadata, Key<T> key) {
                return (T) metadata.getAvailableFormats();
            }
        });
        sGetCommandMap.put(CaptureResult.STATISTICS_FACES.getNativeKey(), new GetCommand() {
            /* class android.hardware.camera2.impl.CameraMetadataNative.AnonymousClass3 */

            @Override // android.hardware.camera2.impl.GetCommand
            public <T> T getValue(CameraMetadataNative metadata, Key<T> key) {
                return (T) metadata.getFaces();
            }
        });
        sGetCommandMap.put(CaptureResult.STATISTICS_FACE_RECTANGLES.getNativeKey(), new GetCommand() {
            /* class android.hardware.camera2.impl.CameraMetadataNative.AnonymousClass4 */

            @Override // android.hardware.camera2.impl.GetCommand
            public <T> T getValue(CameraMetadataNative metadata, Key<T> key) {
                return (T) metadata.getFaceRectangles();
            }
        });
        sGetCommandMap.put(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP.getNativeKey(), new GetCommand() {
            /* class android.hardware.camera2.impl.CameraMetadataNative.AnonymousClass5 */

            @Override // android.hardware.camera2.impl.GetCommand
            public <T> T getValue(CameraMetadataNative metadata, Key<T> key) {
                return (T) metadata.getStreamConfigurationMap();
            }
        });
        sGetCommandMap.put(CameraCharacteristics.CONTROL_MAX_REGIONS_AE.getNativeKey(), new GetCommand() {
            /* class android.hardware.camera2.impl.CameraMetadataNative.AnonymousClass6 */

            @Override // android.hardware.camera2.impl.GetCommand
            public <T> T getValue(CameraMetadataNative metadata, Key<T> key) {
                return (T) metadata.getMaxRegions(key);
            }
        });
        sGetCommandMap.put(CameraCharacteristics.CONTROL_MAX_REGIONS_AWB.getNativeKey(), new GetCommand() {
            /* class android.hardware.camera2.impl.CameraMetadataNative.AnonymousClass7 */

            @Override // android.hardware.camera2.impl.GetCommand
            public <T> T getValue(CameraMetadataNative metadata, Key<T> key) {
                return (T) metadata.getMaxRegions(key);
            }
        });
        sGetCommandMap.put(CameraCharacteristics.CONTROL_MAX_REGIONS_AF.getNativeKey(), new GetCommand() {
            /* class android.hardware.camera2.impl.CameraMetadataNative.AnonymousClass8 */

            @Override // android.hardware.camera2.impl.GetCommand
            public <T> T getValue(CameraMetadataNative metadata, Key<T> key) {
                return (T) metadata.getMaxRegions(key);
            }
        });
        sGetCommandMap.put(CameraCharacteristics.REQUEST_MAX_NUM_OUTPUT_RAW.getNativeKey(), new GetCommand() {
            /* class android.hardware.camera2.impl.CameraMetadataNative.AnonymousClass9 */

            @Override // android.hardware.camera2.impl.GetCommand
            public <T> T getValue(CameraMetadataNative metadata, Key<T> key) {
                return (T) metadata.getMaxNumOutputs(key);
            }
        });
        sGetCommandMap.put(CameraCharacteristics.REQUEST_MAX_NUM_OUTPUT_PROC.getNativeKey(), new GetCommand() {
            /* class android.hardware.camera2.impl.CameraMetadataNative.AnonymousClass10 */

            @Override // android.hardware.camera2.impl.GetCommand
            public <T> T getValue(CameraMetadataNative metadata, Key<T> key) {
                return (T) metadata.getMaxNumOutputs(key);
            }
        });
        sGetCommandMap.put(CameraCharacteristics.REQUEST_MAX_NUM_OUTPUT_PROC_STALLING.getNativeKey(), new GetCommand() {
            /* class android.hardware.camera2.impl.CameraMetadataNative.AnonymousClass11 */

            @Override // android.hardware.camera2.impl.GetCommand
            public <T> T getValue(CameraMetadataNative metadata, Key<T> key) {
                return (T) metadata.getMaxNumOutputs(key);
            }
        });
        sGetCommandMap.put(CaptureRequest.TONEMAP_CURVE.getNativeKey(), new GetCommand() {
            /* class android.hardware.camera2.impl.CameraMetadataNative.AnonymousClass12 */

            @Override // android.hardware.camera2.impl.GetCommand
            public <T> T getValue(CameraMetadataNative metadata, Key<T> key) {
                return (T) metadata.getTonemapCurve();
            }
        });
        sGetCommandMap.put(CaptureResult.JPEG_GPS_LOCATION.getNativeKey(), new GetCommand() {
            /* class android.hardware.camera2.impl.CameraMetadataNative.AnonymousClass13 */

            @Override // android.hardware.camera2.impl.GetCommand
            public <T> T getValue(CameraMetadataNative metadata, Key<T> key) {
                return (T) metadata.getGpsLocation();
            }
        });
        sGetCommandMap.put(CaptureResult.STATISTICS_LENS_SHADING_CORRECTION_MAP.getNativeKey(), new GetCommand() {
            /* class android.hardware.camera2.impl.CameraMetadataNative.AnonymousClass14 */

            @Override // android.hardware.camera2.impl.GetCommand
            public <T> T getValue(CameraMetadataNative metadata, Key<T> key) {
                return (T) metadata.getLensShadingMap();
            }
        });
        sSetCommandMap.put(CameraCharacteristics.SCALER_AVAILABLE_FORMATS.getNativeKey(), new SetCommand() {
            /* class android.hardware.camera2.impl.CameraMetadataNative.AnonymousClass15 */

            @Override // android.hardware.camera2.impl.SetCommand
            public <T> void setValue(CameraMetadataNative metadata, T value) {
                metadata.setAvailableFormats((int[]) value);
            }
        });
        sSetCommandMap.put(CaptureResult.STATISTICS_FACE_RECTANGLES.getNativeKey(), new SetCommand() {
            /* class android.hardware.camera2.impl.CameraMetadataNative.AnonymousClass16 */

            @Override // android.hardware.camera2.impl.SetCommand
            public <T> void setValue(CameraMetadataNative metadata, T value) {
                metadata.setFaceRectangles((Rect[]) value);
            }
        });
        sSetCommandMap.put(CaptureResult.STATISTICS_FACES.getNativeKey(), new SetCommand() {
            /* class android.hardware.camera2.impl.CameraMetadataNative.AnonymousClass17 */

            @Override // android.hardware.camera2.impl.SetCommand
            public <T> void setValue(CameraMetadataNative metadata, T value) {
                metadata.setFaces((Face[]) value);
            }
        });
        sSetCommandMap.put(CaptureRequest.TONEMAP_CURVE.getNativeKey(), new SetCommand() {
            /* class android.hardware.camera2.impl.CameraMetadataNative.AnonymousClass18 */

            @Override // android.hardware.camera2.impl.SetCommand
            public <T> void setValue(CameraMetadataNative metadata, T value) {
                metadata.setTonemapCurve(value);
            }
        });
        sSetCommandMap.put(CaptureResult.JPEG_GPS_LOCATION.getNativeKey(), new SetCommand() {
            /* class android.hardware.camera2.impl.CameraMetadataNative.AnonymousClass19 */

            @Override // android.hardware.camera2.impl.SetCommand
            public <T> void setValue(CameraMetadataNative metadata, T value) {
                metadata.setGpsLocation(value);
            }
        });
        nativeClassInit();
        registerAllMarshalers();
    }

    private static String translateLocationProviderToProcess(String provider) {
        if (provider == null) {
            return null;
        }
        char c = 65535;
        switch (provider.hashCode()) {
            case 102570:
                if (provider.equals(LocationManager.GPS_PROVIDER)) {
                    c = 0;
                    break;
                }
                break;
            case 1843485230:
                if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
                    c = 1;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                return GPS_PROCESS;
            case 1:
                return CELLID_PROCESS;
            default:
                return null;
        }
    }

    private static String translateProcessToLocationProvider(String process) {
        if (process == null) {
            return null;
        }
        char c = 65535;
        switch (process.hashCode()) {
            case 70794:
                if (process.equals(GPS_PROCESS)) {
                    c = 0;
                    break;
                }
                break;
            case 1984215549:
                if (process.equals(CELLID_PROCESS)) {
                    c = 1;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                return LocationManager.GPS_PROVIDER;
            case 1:
                return LocationManager.NETWORK_PROVIDER;
            default:
                return null;
        }
    }

    public CameraMetadataNative() {
        this.mMetadataPtr = nativeAllocate();
        if (this.mMetadataPtr == 0) {
            throw new OutOfMemoryError("Failed to allocate native CameraMetadata");
        }
    }

    public CameraMetadataNative(CameraMetadataNative other) {
        this.mMetadataPtr = nativeAllocateCopy(other);
        if (this.mMetadataPtr == 0) {
            throw new OutOfMemoryError("Failed to allocate native CameraMetadata");
        }
    }

    public static CameraMetadataNative move(CameraMetadataNative other) {
        CameraMetadataNative newObject = new CameraMetadataNative();
        newObject.swap(other);
        return newObject;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        nativeWriteToParcel(dest);
    }

    public <T> T get(CameraCharacteristics.Key<T> key) {
        return (T) get(key.getNativeKey());
    }

    public <T> T get(CaptureResult.Key<T> key) {
        return (T) get(key.getNativeKey());
    }

    public <T> T get(CaptureRequest.Key<T> key) {
        return (T) get(key.getNativeKey());
    }

    public <T> T get(Key<T> key) {
        Preconditions.checkNotNull(key, "key must not be null");
        GetCommand g = sGetCommandMap.get(key);
        return g != null ? (T) g.getValue(this, key) : (T) getBase(key);
    }

    public void readFromParcel(Parcel in) {
        nativeReadFromParcel(in);
    }

    public <T> void set(Key<T> key, T value) {
        SetCommand s = sSetCommandMap.get(key);
        if (s != null) {
            s.setValue(this, value);
        } else {
            setBase(key, value);
        }
    }

    public <T> void set(CaptureRequest.Key<T> key, T value) {
        set(key.getNativeKey(), value);
    }

    public <T> void set(CaptureResult.Key<T> key, T value) {
        set(key.getNativeKey(), value);
    }

    public <T> void set(CameraCharacteristics.Key<T> key, T value) {
        set(key.getNativeKey(), value);
    }

    private void close() {
        nativeClose();
        this.mMetadataPtr = 0;
    }

    private <T> T getBase(CameraCharacteristics.Key<T> key) {
        return (T) getBase(key.getNativeKey());
    }

    private <T> T getBase(CaptureResult.Key<T> key) {
        return (T) getBase(key.getNativeKey());
    }

    private <T> T getBase(CaptureRequest.Key<T> key) {
        return (T) getBase(key.getNativeKey());
    }

    private <T> T getBase(Key<T> key) {
        byte[] values = readValues(key.getTag());
        if (values == null) {
            return null;
        }
        return getMarshalerForKey(key).unmarshal(ByteBuffer.wrap(values).order(ByteOrder.nativeOrder()));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int[] getAvailableFormats() {
        int[] availableFormats = (int[]) getBase(CameraCharacteristics.SCALER_AVAILABLE_FORMATS);
        if (availableFormats != null) {
            for (int i = 0; i < availableFormats.length; i++) {
                if (availableFormats[i] == 33) {
                    availableFormats[i] = 256;
                }
            }
        }
        return availableFormats;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean setFaces(Face[] faces) {
        if (faces == null) {
            return false;
        }
        int numFaces = faces.length;
        boolean fullMode = true;
        for (Face face : faces) {
            if (face == null) {
                numFaces--;
                Log.w(TAG, "setFaces - null face detected, skipping");
            } else if (face.getId() == -1) {
                fullMode = false;
            }
        }
        Rect[] faceRectangles = new Rect[numFaces];
        byte[] faceScores = new byte[numFaces];
        int[] faceIds = null;
        int[] faceLandmarks = null;
        if (fullMode) {
            faceIds = new int[numFaces];
            faceLandmarks = new int[(numFaces * 6)];
        }
        int i = 0;
        for (Face face2 : faces) {
            if (face2 != null) {
                faceRectangles[i] = face2.getBounds();
                faceScores[i] = (byte) face2.getScore();
                if (fullMode) {
                    faceIds[i] = face2.getId();
                    faceLandmarks[(i * 6) + 0] = face2.getLeftEyePosition().x;
                    faceLandmarks[(i * 6) + 1] = face2.getLeftEyePosition().y;
                    faceLandmarks[(i * 6) + 2] = face2.getRightEyePosition().x;
                    faceLandmarks[(i * 6) + 3] = face2.getRightEyePosition().y;
                    faceLandmarks[(i * 6) + 4] = face2.getMouthPosition().x;
                    int j = 0 + 1 + 1 + 1 + 1 + 1 + 1;
                    faceLandmarks[(i * 6) + 5] = face2.getMouthPosition().y;
                }
                i++;
            }
        }
        set(CaptureResult.STATISTICS_FACE_RECTANGLES, faceRectangles);
        set(CaptureResult.STATISTICS_FACE_IDS, faceIds);
        set(CaptureResult.STATISTICS_FACE_LANDMARKS, faceLandmarks);
        set(CaptureResult.STATISTICS_FACE_SCORES, faceScores);
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Face[] getFaces() {
        Integer faceDetectMode = (Integer) get(CaptureResult.STATISTICS_FACE_DETECT_MODE);
        byte[] faceScores = (byte[]) get(CaptureResult.STATISTICS_FACE_SCORES);
        Rect[] faceRectangles = (Rect[]) get(CaptureResult.STATISTICS_FACE_RECTANGLES);
        int[] faceIds = (int[]) get(CaptureResult.STATISTICS_FACE_IDS);
        int[] faceLandmarks = (int[]) get(CaptureResult.STATISTICS_FACE_LANDMARKS);
        if (areValuesAllNull(faceDetectMode, faceScores, faceRectangles, faceIds, faceLandmarks)) {
            return null;
        }
        if (faceDetectMode == null) {
            Log.w(TAG, "Face detect mode metadata is null, assuming the mode is SIMPLE");
            faceDetectMode = 1;
        } else if (faceDetectMode.intValue() == 0) {
            return new Face[0];
        } else {
            if (!(faceDetectMode.intValue() == 1 || faceDetectMode.intValue() == 2)) {
                Log.w(TAG, "Unknown face detect mode: " + faceDetectMode);
                return new Face[0];
            }
        }
        if (faceScores == null || faceRectangles == null) {
            Log.w(TAG, "Expect face scores and rectangles to be non-null");
            return new Face[0];
        }
        if (faceScores.length != faceRectangles.length) {
            Log.w(TAG, String.format("Face score size(%d) doesn match face rectangle size(%d)!", Integer.valueOf(faceScores.length), Integer.valueOf(faceRectangles.length)));
        }
        int numFaces = Math.min(faceScores.length, faceRectangles.length);
        if (faceDetectMode.intValue() == 2) {
            if (faceIds == null || faceLandmarks == null) {
                Log.w(TAG, "Expect face ids and landmarks to be non-null for FULL mode,fallback to SIMPLE mode");
                faceDetectMode = 1;
            } else {
                if (!(faceIds.length == numFaces && faceLandmarks.length == numFaces * 6)) {
                    Log.w(TAG, String.format("Face id size(%d), or face landmark size(%d) don'tmatch face number(%d)!", Integer.valueOf(faceIds.length), Integer.valueOf(faceLandmarks.length * 6), Integer.valueOf(numFaces)));
                }
                numFaces = Math.min(Math.min(numFaces, faceIds.length), faceLandmarks.length / 6);
            }
        }
        ArrayList<Face> faceList = new ArrayList<>();
        if (faceDetectMode.intValue() == 1) {
            for (int i = 0; i < numFaces; i++) {
                if (faceScores[i] <= 100 && faceScores[i] >= 1) {
                    faceList.add(new Face(faceRectangles[i], faceScores[i]));
                }
            }
        } else {
            for (int i2 = 0; i2 < numFaces; i2++) {
                if (faceScores[i2] <= 100 && faceScores[i2] >= 1 && faceIds[i2] >= 0) {
                    faceList.add(new Face(faceRectangles[i2], faceScores[i2], faceIds[i2], new Point(faceLandmarks[i2 * 6], faceLandmarks[(i2 * 6) + 1]), new Point(faceLandmarks[(i2 * 6) + 2], faceLandmarks[(i2 * 6) + 3]), new Point(faceLandmarks[(i2 * 6) + 4], faceLandmarks[(i2 * 6) + 5])));
                }
            }
        }
        Face[] faces = new Face[faceList.size()];
        faceList.toArray(faces);
        return faces;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Rect[] getFaceRectangles() {
        Rect[] faceRectangles = (Rect[]) getBase(CaptureResult.STATISTICS_FACE_RECTANGLES);
        if (faceRectangles == null) {
            return null;
        }
        Rect[] fixedFaceRectangles = new Rect[faceRectangles.length];
        for (int i = 0; i < faceRectangles.length; i++) {
            fixedFaceRectangles[i] = new Rect(faceRectangles[i].left, faceRectangles[i].top, faceRectangles[i].right - faceRectangles[i].left, faceRectangles[i].bottom - faceRectangles[i].top);
        }
        return fixedFaceRectangles;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private LensShadingMap getLensShadingMap() {
        float[] lsmArray = (float[]) getBase(CaptureResult.STATISTICS_LENS_SHADING_MAP);
        Size s = (Size) get(CameraCharacteristics.LENS_INFO_SHADING_MAP_SIZE);
        if (lsmArray == null) {
            return null;
        }
        if (s != null) {
            return new LensShadingMap(lsmArray, s.getHeight(), s.getWidth());
        }
        Log.w(TAG, "getLensShadingMap - Lens shading map size was null.");
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Location getGpsLocation() {
        String processingMethod = (String) get(CaptureResult.JPEG_GPS_PROCESSING_METHOD);
        double[] coords = (double[]) get(CaptureResult.JPEG_GPS_COORDINATES);
        Long timeStamp = (Long) get(CaptureResult.JPEG_GPS_TIMESTAMP);
        if (areValuesAllNull(processingMethod, coords, timeStamp)) {
            return null;
        }
        Location l = new Location(translateProcessToLocationProvider(processingMethod));
        if (timeStamp != null) {
            l.setTime(timeStamp.longValue());
        } else {
            Log.w(TAG, "getGpsLocation - No timestamp for GPS location.");
        }
        if (coords != null) {
            l.setLatitude(coords[0]);
            l.setLongitude(coords[1]);
            l.setAltitude(coords[2]);
            return l;
        }
        Log.w(TAG, "getGpsLocation - No coordinates for GPS location");
        return l;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean setGpsLocation(Location l) {
        if (l == null) {
            return false;
        }
        double[] coords = {l.getLatitude(), l.getLongitude(), l.getAltitude()};
        String processMethod = translateLocationProviderToProcess(l.getProvider());
        set(CaptureRequest.JPEG_GPS_TIMESTAMP, Long.valueOf(l.getTime()));
        set(CaptureRequest.JPEG_GPS_COORDINATES, coords);
        if (processMethod == null) {
            Log.w(TAG, "setGpsLocation - No process method, Location is not from a GPS or NETWORKprovider");
        } else {
            setBase(CaptureRequest.JPEG_GPS_PROCESSING_METHOD, processMethod);
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private StreamConfigurationMap getStreamConfigurationMap() {
        return new StreamConfigurationMap((StreamConfiguration[]) getBase(CameraCharacteristics.SCALER_AVAILABLE_STREAM_CONFIGURATIONS), (StreamConfigurationDuration[]) getBase(CameraCharacteristics.SCALER_AVAILABLE_MIN_FRAME_DURATIONS), (StreamConfigurationDuration[]) getBase(CameraCharacteristics.SCALER_AVAILABLE_STALL_DURATIONS), (HighSpeedVideoConfiguration[]) getBase(CameraCharacteristics.CONTROL_AVAILABLE_HIGH_SPEED_VIDEO_CONFIGURATIONS));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private <T> Integer getMaxRegions(Key<T> key) {
        int[] maxRegions = (int[]) getBase(CameraCharacteristics.CONTROL_MAX_REGIONS);
        if (maxRegions == null) {
            return null;
        }
        if (key.equals(CameraCharacteristics.CONTROL_MAX_REGIONS_AE)) {
            return Integer.valueOf(maxRegions[0]);
        }
        if (key.equals(CameraCharacteristics.CONTROL_MAX_REGIONS_AWB)) {
            return Integer.valueOf(maxRegions[1]);
        }
        if (key.equals(CameraCharacteristics.CONTROL_MAX_REGIONS_AF)) {
            return Integer.valueOf(maxRegions[2]);
        }
        throw new AssertionError("Invalid key " + key);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private <T> Integer getMaxNumOutputs(Key<T> key) {
        int[] maxNumOutputs = (int[]) getBase(CameraCharacteristics.REQUEST_MAX_NUM_OUTPUT_STREAMS);
        if (maxNumOutputs == null) {
            return null;
        }
        if (key.equals(CameraCharacteristics.REQUEST_MAX_NUM_OUTPUT_RAW)) {
            return Integer.valueOf(maxNumOutputs[0]);
        }
        if (key.equals(CameraCharacteristics.REQUEST_MAX_NUM_OUTPUT_PROC)) {
            return Integer.valueOf(maxNumOutputs[1]);
        }
        if (key.equals(CameraCharacteristics.REQUEST_MAX_NUM_OUTPUT_PROC_STALLING)) {
            return Integer.valueOf(maxNumOutputs[2]);
        }
        throw new AssertionError("Invalid key " + key);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private <T> TonemapCurve getTonemapCurve() {
        float[] red = (float[]) getBase(CaptureRequest.TONEMAP_CURVE_RED);
        float[] green = (float[]) getBase(CaptureRequest.TONEMAP_CURVE_GREEN);
        float[] blue = (float[]) getBase(CaptureRequest.TONEMAP_CURVE_BLUE);
        if (areValuesAllNull(red, green, blue)) {
            return null;
        }
        if (red != null && green != null && blue != null) {
            return new TonemapCurve(red, green, blue);
        }
        Log.w(TAG, "getTonemapCurve - missing tone curve components");
        return null;
    }

    private <T> void setBase(CameraCharacteristics.Key<T> key, T value) {
        setBase(key.getNativeKey(), value);
    }

    private <T> void setBase(CaptureResult.Key<T> key, T value) {
        setBase(key.getNativeKey(), value);
    }

    private <T> void setBase(CaptureRequest.Key<T> key, T value) {
        setBase(key.getNativeKey(), value);
    }

    private <T> void setBase(Key<T> key, T value) {
        int tag = key.getTag();
        if (value == null) {
            writeValues(tag, null);
            return;
        }
        Marshaler<T> marshaler = getMarshalerForKey(key);
        byte[] values = new byte[marshaler.calculateMarshalSize(value)];
        marshaler.marshal(value, ByteBuffer.wrap(values).order(ByteOrder.nativeOrder()));
        writeValues(tag, values);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean setAvailableFormats(int[] value) {
        if (value == null) {
            return false;
        }
        int[] newValues = new int[value.length];
        for (int i = 0; i < value.length; i++) {
            newValues[i] = value[i];
            if (value[i] == 256) {
                newValues[i] = 33;
            }
        }
        setBase(CameraCharacteristics.SCALER_AVAILABLE_FORMATS, newValues);
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean setFaceRectangles(Rect[] faceRects) {
        if (faceRects == null) {
            return false;
        }
        Rect[] newFaceRects = new Rect[faceRects.length];
        for (int i = 0; i < newFaceRects.length; i++) {
            newFaceRects[i] = new Rect(faceRects[i].left, faceRects[i].top, faceRects[i].right + faceRects[i].left, faceRects[i].bottom + faceRects[i].top);
        }
        setBase(CaptureResult.STATISTICS_FACE_RECTANGLES, newFaceRects);
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private <T> boolean setTonemapCurve(TonemapCurve tc) {
        if (tc == null) {
            return false;
        }
        float[][] curve = new float[3][];
        for (int i = 0; i <= 2; i++) {
            curve[i] = new float[(tc.getPointCount(i) * 2)];
            tc.copyColorCurve(i, curve[i], 0);
        }
        setBase(CaptureRequest.TONEMAP_CURVE_RED, curve[0]);
        setBase(CaptureRequest.TONEMAP_CURVE_GREEN, curve[1]);
        setBase(CaptureRequest.TONEMAP_CURVE_BLUE, curve[2]);
        return true;
    }

    public void swap(CameraMetadataNative other) {
        nativeSwap(other);
    }

    public int getEntryCount() {
        return nativeGetEntryCount();
    }

    public boolean isEmpty() {
        return nativeIsEmpty();
    }

    public static int getTag(String key) {
        return nativeGetTagFromKey(key);
    }

    public static int getNativeType(int tag) {
        return nativeGetTypeFromTag(tag);
    }

    public void writeValues(int tag, byte[] src) {
        nativeWriteValues(tag, src);
    }

    public byte[] readValues(int tag) {
        return nativeReadValues(tag);
    }

    public void dumpToLog() {
        try {
            nativeDump();
        } catch (IOException e) {
            Log.wtf(TAG, "Dump logging failed", e);
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }

    private static <T> Marshaler<T> getMarshalerForKey(Key<T> key) {
        return MarshalRegistry.getMarshaler(key.getTypeReference(), getNativeType(key.getTag()));
    }

    private static void registerAllMarshalers() {
        if (VERBOSE) {
            Log.v(TAG, "Shall register metadata marshalers");
        }
        for (MarshalQueryable query : new MarshalQueryable[]{new MarshalQueryablePrimitive(), new MarshalQueryableEnum(), new MarshalQueryableArray(), new MarshalQueryableBoolean(), new MarshalQueryableNativeByteToInteger(), new MarshalQueryableRect(), new MarshalQueryableSize(), new MarshalQueryableSizeF(), new MarshalQueryableString(), new MarshalQueryableReprocessFormatsMap(), new MarshalQueryableRange(), new MarshalQueryablePair(), new MarshalQueryableMeteringRectangle(), new MarshalQueryableColorSpaceTransform(), new MarshalQueryableStreamConfiguration(), new MarshalQueryableStreamConfigurationDuration(), new MarshalQueryableRggbChannelVector(), new MarshalQueryableBlackLevelPattern(), new MarshalQueryableHighSpeedVideoConfiguration(), new MarshalQueryableParcelable()}) {
            MarshalRegistry.registerMarshalQueryable(query);
        }
        if (VERBOSE) {
            Log.v(TAG, "Registered metadata marshalers");
        }
    }

    private static boolean areValuesAllNull(Object... objs) {
        for (Object o : objs) {
            if (o != null) {
                return false;
            }
        }
        return true;
    }
}

package android.gesture;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class GestureStore {
    private static final short FILE_FORMAT_VERSION = 1;
    public static final int ORIENTATION_INVARIANT = 1;
    public static final int ORIENTATION_SENSITIVE = 2;
    static final int ORIENTATION_SENSITIVE_4 = 4;
    static final int ORIENTATION_SENSITIVE_8 = 8;
    private static final boolean PROFILE_LOADING_SAVING = false;
    public static final int SEQUENCE_INVARIANT = 1;
    public static final int SEQUENCE_SENSITIVE = 2;
    private boolean mChanged = false;
    private Learner mClassifier = new InstanceLearner();
    private final HashMap<String, ArrayList<Gesture>> mNamedGestures = new HashMap<>();
    private int mOrientationStyle = 2;
    private int mSequenceType = 2;

    public void setOrientationStyle(int style) {
        this.mOrientationStyle = style;
    }

    public int getOrientationStyle() {
        return this.mOrientationStyle;
    }

    public void setSequenceType(int type) {
        this.mSequenceType = type;
    }

    public int getSequenceType() {
        return this.mSequenceType;
    }

    public Set<String> getGestureEntries() {
        return this.mNamedGestures.keySet();
    }

    public ArrayList<Prediction> recognize(Gesture gesture) {
        return this.mClassifier.classify(this.mSequenceType, this.mOrientationStyle, Instance.createInstance(this.mSequenceType, this.mOrientationStyle, gesture, null).vector);
    }

    public void addGesture(String entryName, Gesture gesture) {
        if (entryName != null && entryName.length() != 0) {
            ArrayList<Gesture> gestures = this.mNamedGestures.get(entryName);
            if (gestures == null) {
                gestures = new ArrayList<>();
                this.mNamedGestures.put(entryName, gestures);
            }
            gestures.add(gesture);
            this.mClassifier.addInstance(Instance.createInstance(this.mSequenceType, this.mOrientationStyle, gesture, entryName));
            this.mChanged = true;
        }
    }

    public void removeGesture(String entryName, Gesture gesture) {
        ArrayList<Gesture> gestures = this.mNamedGestures.get(entryName);
        if (gestures != null) {
            gestures.remove(gesture);
            if (gestures.isEmpty()) {
                this.mNamedGestures.remove(entryName);
            }
            this.mClassifier.removeInstance(gesture.getID());
            this.mChanged = true;
        }
    }

    public void removeEntry(String entryName) {
        this.mNamedGestures.remove(entryName);
        this.mClassifier.removeInstances(entryName);
        this.mChanged = true;
    }

    public ArrayList<Gesture> getGestures(String entryName) {
        ArrayList<Gesture> gestures = this.mNamedGestures.get(entryName);
        if (gestures != null) {
            return new ArrayList<>(gestures);
        }
        return null;
    }

    public boolean hasChanged() {
        return this.mChanged;
    }

    public void save(OutputStream stream) throws IOException {
        save(stream, false);
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x0069  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void save(java.io.OutputStream r12, boolean r13) throws java.io.IOException {
        /*
        // Method dump skipped, instructions count: 112
        */
        throw new UnsupportedOperationException("Method not decompiled: android.gesture.GestureStore.save(java.io.OutputStream, boolean):void");
    }

    public void load(InputStream stream) throws IOException {
        load(stream, false);
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x0029  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void load(java.io.InputStream r6, boolean r7) throws java.io.IOException {
        /*
            r5 = this;
            r0 = 0
            java.io.DataInputStream r1 = new java.io.DataInputStream     // Catch:{ all -> 0x002d }
            boolean r3 = r6 instanceof java.io.BufferedInputStream     // Catch:{ all -> 0x002d }
            if (r3 == 0) goto L_0x0017
        L_0x0007:
            r1.<init>(r6)     // Catch:{ all -> 0x002d }
            short r2 = r1.readShort()     // Catch:{ all -> 0x0025 }
            switch(r2) {
                case 1: goto L_0x0021;
                default: goto L_0x0011;
            }
        L_0x0011:
            if (r7 == 0) goto L_0x0016
            android.gesture.GestureUtils.closeStream(r1)
        L_0x0016:
            return
        L_0x0017:
            java.io.BufferedInputStream r3 = new java.io.BufferedInputStream
            r4 = 32768(0x8000, float:4.5918E-41)
            r3.<init>(r6, r4)
            r6 = r3
            goto L_0x0007
        L_0x0021:
            r5.readFormatV1(r1)
            goto L_0x0011
        L_0x0025:
            r3 = move-exception
            r0 = r1
        L_0x0027:
            if (r7 == 0) goto L_0x002c
            android.gesture.GestureUtils.closeStream(r0)
        L_0x002c:
            throw r3
        L_0x002d:
            r3 = move-exception
            goto L_0x0027
            switch-data {1->0x0021, }
        */
        throw new UnsupportedOperationException("Method not decompiled: android.gesture.GestureStore.load(java.io.InputStream, boolean):void");
    }

    private void readFormatV1(DataInputStream in) throws IOException {
        Learner classifier = this.mClassifier;
        HashMap<String, ArrayList<Gesture>> namedGestures = this.mNamedGestures;
        namedGestures.clear();
        int entriesCount = in.readInt();
        for (int i = 0; i < entriesCount; i++) {
            String name = in.readUTF();
            int gestureCount = in.readInt();
            ArrayList<Gesture> gestures = new ArrayList<>(gestureCount);
            for (int j = 0; j < gestureCount; j++) {
                Gesture gesture = Gesture.deserialize(in);
                gestures.add(gesture);
                classifier.addInstance(Instance.createInstance(this.mSequenceType, this.mOrientationStyle, gesture, name));
            }
            namedGestures.put(name, gestures);
        }
    }

    /* access modifiers changed from: package-private */
    public Learner getLearner() {
        return this.mClassifier;
    }
}

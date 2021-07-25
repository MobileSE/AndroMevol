package android.content.res;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.MathUtils;
import android.util.SparseArray;
import android.util.StateSet;
import android.util.Xml;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.GrowingArrayUtils;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class ColorStateList implements Parcelable {
    public static final Parcelable.Creator<ColorStateList> CREATOR = new Parcelable.Creator<ColorStateList>() {
        /* class android.content.res.ColorStateList.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ColorStateList[] newArray(int size) {
            return new ColorStateList[size];
        }

        @Override // android.os.Parcelable.Creator
        public ColorStateList createFromParcel(Parcel source) {
            int N = source.readInt();
            int[][] stateSpecs = new int[N][];
            for (int i = 0; i < N; i++) {
                stateSpecs[i] = source.createIntArray();
            }
            return new ColorStateList(stateSpecs, source.createIntArray());
        }
    };
    private static final int[][] EMPTY = {new int[0]};
    private static final SparseArray<WeakReference<ColorStateList>> sCache = new SparseArray<>();
    private int[] mColors;
    private int mDefaultColor = -65536;
    private int[][] mStateSpecs;

    private ColorStateList() {
    }

    public ColorStateList(int[][] states, int[] colors) {
        this.mStateSpecs = states;
        this.mColors = colors;
        if (states.length > 0) {
            this.mDefaultColor = colors[0];
            for (int i = 0; i < states.length; i++) {
                if (states[i].length == 0) {
                    this.mDefaultColor = colors[i];
                }
            }
        }
    }

    public static ColorStateList valueOf(int color) {
        synchronized (sCache) {
            WeakReference<ColorStateList> ref = sCache.get(color);
            ColorStateList csl = ref != null ? ref.get() : null;
            if (csl != null) {
                return csl;
            }
            ColorStateList csl2 = new ColorStateList(EMPTY, new int[]{color});
            sCache.put(color, new WeakReference<>(csl2));
            return csl2;
        }
    }

    public static ColorStateList createFromXml(Resources r, XmlPullParser parser) throws XmlPullParserException, IOException {
        int type;
        AttributeSet attrs = Xml.asAttributeSet(parser);
        do {
            type = parser.next();
            if (type == 2) {
                break;
            }
        } while (type != 1);
        if (type == 2) {
            return createFromXmlInner(r, parser, attrs);
        }
        throw new XmlPullParserException("No start tag found");
    }

    private static ColorStateList createFromXmlInner(Resources r, XmlPullParser parser, AttributeSet attrs) throws XmlPullParserException, IOException {
        String name = parser.getName();
        if (name.equals("selector")) {
            ColorStateList colorStateList = new ColorStateList();
            colorStateList.inflate(r, parser, attrs);
            return colorStateList;
        }
        throw new XmlPullParserException(parser.getPositionDescription() + ": invalid drawable tag " + name);
    }

    public ColorStateList withAlpha(int alpha) {
        int[] colors = new int[this.mColors.length];
        int len = colors.length;
        for (int i = 0; i < len; i++) {
            colors[i] = (this.mColors[i] & 16777215) | (alpha << 24);
        }
        return new ColorStateList(this.mStateSpecs, colors);
    }

    private void inflate(Resources r, XmlPullParser parser, AttributeSet attrs) throws XmlPullParserException, IOException {
        int depth;
        int stateResId;
        int innerDepth = parser.getDepth() + 1;
        int[][] stateSpecList = (int[][]) ArrayUtils.newUnpaddedArray(int[].class, 20);
        int[] colorList = new int[stateSpecList.length];
        int listSize = 0;
        while (true) {
            int type = parser.next();
            if (type == 1 || ((depth = parser.getDepth()) < innerDepth && type == 3)) {
                this.mColors = new int[listSize];
                this.mStateSpecs = new int[listSize][];
                System.arraycopy(colorList, 0, this.mColors, 0, listSize);
                System.arraycopy(stateSpecList, 0, this.mStateSpecs, 0, listSize);
            } else if (type == 2 && depth <= innerDepth && parser.getName().equals("item")) {
                int alphaRes = 0;
                float alpha = 1.0f;
                int colorRes = 0;
                int color = -65536;
                boolean haveColor = false;
                int j = 0;
                int numAttrs = attrs.getAttributeCount();
                int[] stateSpec = new int[numAttrs];
                int i = 0;
                while (i < numAttrs && (stateResId = attrs.getAttributeNameResource(i)) != 0) {
                    if (stateResId == 16843551) {
                        alphaRes = attrs.getAttributeResourceValue(i, 0);
                        if (alphaRes == 0) {
                            alpha = attrs.getAttributeFloatValue(i, 1.0f);
                            j = j;
                        }
                        j = j;
                    } else if (stateResId == 16843173) {
                        colorRes = attrs.getAttributeResourceValue(i, 0);
                        if (colorRes == 0) {
                            color = attrs.getAttributeIntValue(i, color);
                            haveColor = true;
                            j = j;
                        }
                        j = j;
                    } else {
                        j++;
                        if (!attrs.getAttributeBooleanValue(i, false)) {
                            stateResId = -stateResId;
                        }
                        stateSpec[j] = stateResId;
                    }
                    i++;
                }
                int[] stateSpec2 = StateSet.trimStateSet(stateSpec, j);
                if (colorRes != 0) {
                    color = r.getColor(colorRes);
                } else if (!haveColor) {
                    throw new XmlPullParserException(parser.getPositionDescription() + ": <item> tag requires a 'android:color' attribute.");
                }
                if (alphaRes != 0) {
                    alpha = r.getFloat(alphaRes);
                }
                int color2 = (16777215 & color) | (MathUtils.constrain((int) (((float) Color.alpha(color)) * alpha), 0, 255) << 24);
                if (listSize == 0 || stateSpec2.length == 0) {
                    this.mDefaultColor = color2;
                }
                colorList = GrowingArrayUtils.append(colorList, listSize, color2);
                stateSpecList = (int[][]) GrowingArrayUtils.append(stateSpecList, listSize, stateSpec2);
                listSize++;
            }
        }
        this.mColors = new int[listSize];
        this.mStateSpecs = new int[listSize][];
        System.arraycopy(colorList, 0, this.mColors, 0, listSize);
        System.arraycopy(stateSpecList, 0, this.mStateSpecs, 0, listSize);
    }

    public boolean isStateful() {
        return this.mStateSpecs.length > 1;
    }

    public boolean isOpaque() {
        int n = this.mColors.length;
        for (int i = 0; i < n; i++) {
            if (Color.alpha(this.mColors[i]) != 255) {
                return false;
            }
        }
        return true;
    }

    public int getColorForState(int[] stateSet, int defaultColor) {
        int setLength = this.mStateSpecs.length;
        for (int i = 0; i < setLength; i++) {
            if (StateSet.stateSetMatches(this.mStateSpecs[i], stateSet)) {
                return this.mColors[i];
            }
        }
        return defaultColor;
    }

    public int getDefaultColor() {
        return this.mDefaultColor;
    }

    public int[][] getStates() {
        return this.mStateSpecs;
    }

    public int[] getColors() {
        return this.mColors;
    }

    public static ColorStateList addFirstIfMissing(ColorStateList colorStateList, int state, int color) {
        int[][] inputStates = colorStateList.getStates();
        for (int i = 0; i < inputStates.length; i++) {
            int[] inputState = inputStates[i];
            for (int j = 0; j < inputState.length; j++) {
                if (inputState[i] == state) {
                    return colorStateList;
                }
            }
        }
        int[][] outputStates = new int[(inputStates.length + 1)][];
        System.arraycopy(inputStates, 0, outputStates, 1, inputStates.length);
        outputStates[0] = new int[]{state};
        int[] inputColors = colorStateList.getColors();
        int[] outputColors = new int[(inputColors.length + 1)];
        System.arraycopy(inputColors, 0, outputColors, 1, inputColors.length);
        outputColors[0] = color;
        return new ColorStateList(outputStates, outputColors);
    }

    public String toString() {
        return "ColorStateList{mStateSpecs=" + Arrays.deepToString(this.mStateSpecs) + "mColors=" + Arrays.toString(this.mColors) + "mDefaultColor=" + this.mDefaultColor + '}';
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        int N = this.mStateSpecs.length;
        dest.writeInt(N);
        for (int i = 0; i < N; i++) {
            dest.writeIntArray(this.mStateSpecs[i]);
        }
        dest.writeIntArray(this.mColors);
    }
}

package android.os;

import android.os.Parcelable;

public class PatternMatcher implements Parcelable {
    public static final Parcelable.Creator<PatternMatcher> CREATOR = new Parcelable.Creator<PatternMatcher>() {
        /* class android.os.PatternMatcher.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public PatternMatcher createFromParcel(Parcel source) {
            return new PatternMatcher(source);
        }

        @Override // android.os.Parcelable.Creator
        public PatternMatcher[] newArray(int size) {
            return new PatternMatcher[size];
        }
    };
    public static final int PATTERN_LITERAL = 0;
    public static final int PATTERN_PREFIX = 1;
    public static final int PATTERN_SIMPLE_GLOB = 2;
    private final String mPattern;
    private final int mType;

    public PatternMatcher(String pattern, int type) {
        this.mPattern = pattern;
        this.mType = type;
    }

    public final String getPath() {
        return this.mPattern;
    }

    public final int getType() {
        return this.mType;
    }

    public boolean match(String str) {
        return matchPattern(this.mPattern, str, this.mType);
    }

    public String toString() {
        String type = "? ";
        switch (this.mType) {
            case 0:
                type = "LITERAL: ";
                break;
            case 1:
                type = "PREFIX: ";
                break;
            case 2:
                type = "GLOB: ";
                break;
        }
        return "PatternMatcher{" + type + this.mPattern + "}";
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mPattern);
        dest.writeInt(this.mType);
    }

    public PatternMatcher(Parcel src) {
        this.mPattern = src.readString();
        this.mType = src.readInt();
    }

    /* JADX WARNING: Removed duplicated region for block: B:48:0x0088  */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x009e  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static boolean matchPattern(java.lang.String r13, java.lang.String r14, int r15) {
        /*
        // Method dump skipped, instructions count: 204
        */
        throw new UnsupportedOperationException("Method not decompiled: android.os.PatternMatcher.matchPattern(java.lang.String, java.lang.String, int):boolean");
    }
}

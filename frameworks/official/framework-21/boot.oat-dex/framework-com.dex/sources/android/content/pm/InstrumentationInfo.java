package android.content.pm;

import android.os.Parcel;
import android.os.Parcelable;

public class InstrumentationInfo extends PackageItemInfo implements Parcelable {
    public static final Parcelable.Creator<InstrumentationInfo> CREATOR = new Parcelable.Creator<InstrumentationInfo>() {
        /* class android.content.pm.InstrumentationInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public InstrumentationInfo createFromParcel(Parcel source) {
            return new InstrumentationInfo(source);
        }

        @Override // android.os.Parcelable.Creator
        public InstrumentationInfo[] newArray(int size) {
            return new InstrumentationInfo[size];
        }
    };
    public String dataDir;
    public boolean functionalTest;
    public boolean handleProfiling;
    public String nativeLibraryDir;
    public String publicSourceDir;
    public String sourceDir;
    public String[] splitPublicSourceDirs;
    public String[] splitSourceDirs;
    public String targetPackage;

    public InstrumentationInfo() {
    }

    public InstrumentationInfo(InstrumentationInfo orig) {
        super(orig);
        this.targetPackage = orig.targetPackage;
        this.sourceDir = orig.sourceDir;
        this.publicSourceDir = orig.publicSourceDir;
        this.dataDir = orig.dataDir;
        this.nativeLibraryDir = orig.nativeLibraryDir;
        this.handleProfiling = orig.handleProfiling;
        this.functionalTest = orig.functionalTest;
    }

    public String toString() {
        return "InstrumentationInfo{" + Integer.toHexString(System.identityHashCode(this)) + " " + this.packageName + "}";
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable, android.content.pm.PackageItemInfo
    public void writeToParcel(Parcel dest, int parcelableFlags) {
        int i;
        int i2 = 0;
        super.writeToParcel(dest, parcelableFlags);
        dest.writeString(this.targetPackage);
        dest.writeString(this.sourceDir);
        dest.writeString(this.publicSourceDir);
        dest.writeString(this.dataDir);
        dest.writeString(this.nativeLibraryDir);
        if (!this.handleProfiling) {
            i = 0;
        } else {
            i = 1;
        }
        dest.writeInt(i);
        if (this.functionalTest) {
            i2 = 1;
        }
        dest.writeInt(i2);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    private InstrumentationInfo(Parcel source) {
        super(source);
        boolean z;
        boolean z2 = true;
        this.targetPackage = source.readString();
        this.sourceDir = source.readString();
        this.publicSourceDir = source.readString();
        this.dataDir = source.readString();
        this.nativeLibraryDir = source.readString();
        if (source.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.handleProfiling = z;
        this.functionalTest = source.readInt() == 0 ? false : z2;
    }
}

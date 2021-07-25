package android.nfc.cardemulation;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import com.android.internal.R;
import com.android.internal.telephony.PhoneConstants;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.xmlpull.v1.XmlPullParserException;

public final class ApduServiceInfo implements Parcelable {
    public static final Parcelable.Creator<ApduServiceInfo> CREATOR = new Parcelable.Creator<ApduServiceInfo>() {
        /* class android.nfc.cardemulation.ApduServiceInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ApduServiceInfo createFromParcel(Parcel source) {
            boolean onHost;
            boolean requiresUnlock;
            ResolveInfo info = ResolveInfo.CREATOR.createFromParcel(source);
            String description = source.readString();
            if (source.readInt() != 0) {
                onHost = true;
            } else {
                onHost = false;
            }
            ArrayList<AidGroup> staticAidGroups = new ArrayList<>();
            if (source.readInt() > 0) {
                source.readTypedList(staticAidGroups, AidGroup.CREATOR);
            }
            ArrayList<AidGroup> dynamicAidGroups = new ArrayList<>();
            if (source.readInt() > 0) {
                source.readTypedList(dynamicAidGroups, AidGroup.CREATOR);
            }
            if (source.readInt() != 0) {
                requiresUnlock = true;
            } else {
                requiresUnlock = false;
            }
            return new ApduServiceInfo(info, onHost, description, staticAidGroups, dynamicAidGroups, requiresUnlock, source.readInt(), source.readInt());
        }

        @Override // android.os.Parcelable.Creator
        public ApduServiceInfo[] newArray(int size) {
            return new ApduServiceInfo[size];
        }
    };
    static final String TAG = "ApduServiceInfo";
    final int mBannerResourceId;
    final String mDescription;
    final HashMap<String, AidGroup> mDynamicAidGroups;
    final boolean mOnHost;
    final boolean mRequiresDeviceUnlock;
    final ResolveInfo mService;
    final HashMap<String, AidGroup> mStaticAidGroups;
    final int mUid;

    public ApduServiceInfo(ResolveInfo info, boolean onHost, String description, ArrayList<AidGroup> staticAidGroups, ArrayList<AidGroup> dynamicAidGroups, boolean requiresUnlock, int bannerResource, int uid) {
        this.mService = info;
        this.mDescription = description;
        this.mStaticAidGroups = new HashMap<>();
        this.mDynamicAidGroups = new HashMap<>();
        this.mOnHost = onHost;
        this.mRequiresDeviceUnlock = requiresUnlock;
        Iterator i$ = staticAidGroups.iterator();
        while (i$.hasNext()) {
            AidGroup aidGroup = i$.next();
            this.mStaticAidGroups.put(aidGroup.category, aidGroup);
        }
        Iterator i$2 = dynamicAidGroups.iterator();
        while (i$2.hasNext()) {
            AidGroup aidGroup2 = i$2.next();
            this.mDynamicAidGroups.put(aidGroup2.category, aidGroup2);
        }
        this.mBannerResourceId = bannerResource;
        this.mUid = uid;
    }

    public ApduServiceInfo(PackageManager pm, ResolveInfo info, boolean onHost) throws XmlPullParserException, IOException {
        XmlResourceParser parser;
        ServiceInfo si = info.serviceInfo;
        XmlResourceParser parser2 = null;
        if (onHost) {
            try {
                parser = si.loadXmlMetaData(pm, HostApduService.SERVICE_META_DATA);
                if (parser == null) {
                    throw new XmlPullParserException("No android.nfc.cardemulation.host_apdu_service meta-data");
                }
            } catch (PackageManager.NameNotFoundException e) {
                throw new XmlPullParserException("Unable to create context for: " + si.packageName);
            } catch (Throwable th) {
                if (0 != 0) {
                    parser2.close();
                }
                throw th;
            }
        } else {
            parser = si.loadXmlMetaData(pm, OffHostApduService.SERVICE_META_DATA);
            if (parser == null) {
                throw new XmlPullParserException("No android.nfc.cardemulation.off_host_apdu_service meta-data");
            }
        }
        int eventType = parser.getEventType();
        while (eventType != 2 && eventType != 1) {
            eventType = parser.next();
        }
        String tagName = parser.getName();
        if (onHost && !"host-apdu-service".equals(tagName)) {
            throw new XmlPullParserException("Meta-data does not start with <host-apdu-service> tag");
        } else if (onHost || "offhost-apdu-service".equals(tagName)) {
            Resources res = pm.getResourcesForApplication(si.applicationInfo);
            AttributeSet attrs = Xml.asAttributeSet(parser);
            if (onHost) {
                TypedArray sa = res.obtainAttributes(attrs, R.styleable.HostApduService);
                this.mService = info;
                this.mDescription = sa.getString(0);
                this.mRequiresDeviceUnlock = sa.getBoolean(1, false);
                this.mBannerResourceId = sa.getResourceId(2, -1);
                sa.recycle();
            } else {
                TypedArray sa2 = res.obtainAttributes(attrs, R.styleable.OffHostApduService);
                this.mService = info;
                this.mDescription = sa2.getString(0);
                this.mRequiresDeviceUnlock = false;
                this.mBannerResourceId = sa2.getResourceId(1, -1);
                sa2.recycle();
            }
            this.mStaticAidGroups = new HashMap<>();
            this.mDynamicAidGroups = new HashMap<>();
            this.mOnHost = onHost;
            int depth = parser.getDepth();
            AidGroup currentGroup = null;
            while (true) {
                int eventType2 = parser.next();
                if ((eventType2 != 3 || parser.getDepth() > depth) && eventType2 != 1) {
                    String tagName2 = parser.getName();
                    if (eventType2 == 2 && "aid-group".equals(tagName2) && currentGroup == null) {
                        TypedArray groupAttrs = res.obtainAttributes(attrs, R.styleable.AidGroup);
                        String groupCategory = groupAttrs.getString(1);
                        String groupDescription = groupAttrs.getString(0);
                        groupCategory = !CardEmulation.CATEGORY_PAYMENT.equals(groupCategory) ? CardEmulation.CATEGORY_OTHER : groupCategory;
                        currentGroup = this.mStaticAidGroups.get(groupCategory);
                        if (currentGroup == null) {
                            currentGroup = new AidGroup(groupCategory, groupDescription);
                        } else if (!CardEmulation.CATEGORY_OTHER.equals(groupCategory)) {
                            Log.e(TAG, "Not allowing multiple aid-groups in the " + groupCategory + " category");
                            currentGroup = null;
                        }
                        groupAttrs.recycle();
                    } else if (eventType2 == 3 && "aid-group".equals(tagName2) && currentGroup != null) {
                        if (currentGroup.aids.size() <= 0) {
                            Log.e(TAG, "Not adding <aid-group> with empty or invalid AIDs");
                        } else if (!this.mStaticAidGroups.containsKey(currentGroup.category)) {
                            this.mStaticAidGroups.put(currentGroup.category, currentGroup);
                        }
                        currentGroup = null;
                    } else if (eventType2 == 2 && "aid-filter".equals(tagName2) && currentGroup != null) {
                        TypedArray a = res.obtainAttributes(attrs, R.styleable.AidFilter);
                        String aid = a.getString(0).toUpperCase();
                        if (!CardEmulation.isValidAid(aid) || currentGroup.aids.contains(aid)) {
                            Log.e(TAG, "Ignoring invalid or duplicate aid: " + aid);
                        } else {
                            currentGroup.aids.add(aid);
                        }
                        a.recycle();
                    } else if (eventType2 == 2 && "aid-prefix-filter".equals(tagName2) && currentGroup != null) {
                        TypedArray a2 = res.obtainAttributes(attrs, R.styleable.AidFilter);
                        String aid2 = a2.getString(0).toUpperCase().concat(PhoneConstants.APN_TYPE_ALL);
                        if (!CardEmulation.isValidAid(aid2) || currentGroup.aids.contains(aid2)) {
                            Log.e(TAG, "Ignoring invalid or duplicate aid: " + aid2);
                        } else {
                            currentGroup.aids.add(aid2);
                        }
                        a2.recycle();
                    }
                }
            }
            if (parser != null) {
                parser.close();
            }
            this.mUid = si.applicationInfo.uid;
        } else {
            throw new XmlPullParserException("Meta-data does not start with <offhost-apdu-service> tag");
        }
    }

    public ComponentName getComponent() {
        return new ComponentName(this.mService.serviceInfo.packageName, this.mService.serviceInfo.name);
    }

    public ArrayList<String> getAids() {
        ArrayList<String> aids = new ArrayList<>();
        Iterator i$ = getAidGroups().iterator();
        while (i$.hasNext()) {
            aids.addAll(i$.next().aids);
        }
        return aids;
    }

    public AidGroup getDynamicAidGroupForCategory(String category) {
        return this.mDynamicAidGroups.get(category);
    }

    public boolean removeDynamicAidGroupForCategory(String category) {
        return this.mDynamicAidGroups.remove(category) != null;
    }

    public ArrayList<AidGroup> getAidGroups() {
        ArrayList<AidGroup> groups = new ArrayList<>();
        for (Map.Entry<String, AidGroup> entry : this.mDynamicAidGroups.entrySet()) {
            groups.add(entry.getValue());
        }
        for (Map.Entry<String, AidGroup> entry2 : this.mStaticAidGroups.entrySet()) {
            if (!this.mDynamicAidGroups.containsKey(entry2.getKey())) {
                groups.add(entry2.getValue());
            }
        }
        return groups;
    }

    public String getCategoryForAid(String aid) {
        Iterator i$ = getAidGroups().iterator();
        while (i$.hasNext()) {
            AidGroup group = i$.next();
            if (group.aids.contains(aid.toUpperCase())) {
                return group.category;
            }
        }
        return null;
    }

    public boolean hasCategory(String category) {
        return this.mStaticAidGroups.containsKey(category) || this.mDynamicAidGroups.containsKey(category);
    }

    public boolean isOnHost() {
        return this.mOnHost;
    }

    public boolean requiresUnlock() {
        return this.mRequiresDeviceUnlock;
    }

    public String getDescription() {
        return this.mDescription;
    }

    public int getUid() {
        return this.mUid;
    }

    public void setOrReplaceDynamicAidGroup(AidGroup aidGroup) {
        this.mDynamicAidGroups.put(aidGroup.getCategory(), aidGroup);
    }

    public CharSequence loadLabel(PackageManager pm) {
        return this.mService.loadLabel(pm);
    }

    public Drawable loadIcon(PackageManager pm) {
        return this.mService.loadIcon(pm);
    }

    public Drawable loadBanner(PackageManager pm) {
        try {
            return pm.getResourcesForApplication(this.mService.serviceInfo.packageName).getDrawable(this.mBannerResourceId);
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Could not load banner.");
            return null;
        } catch (PackageManager.NameNotFoundException e2) {
            Log.e(TAG, "Could not load banner.");
            return null;
        }
    }

    public String toString() {
        StringBuilder out = new StringBuilder("ApduService: ");
        out.append(getComponent());
        out.append(", description: " + this.mDescription);
        out.append(", Static AID Groups: ");
        for (AidGroup aidGroup : this.mStaticAidGroups.values()) {
            out.append(aidGroup.toString());
        }
        out.append(", Dynamic AID Groups: ");
        for (AidGroup aidGroup2 : this.mDynamicAidGroups.values()) {
            out.append(aidGroup2.toString());
        }
        return out.toString();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ApduServiceInfo)) {
            return false;
        }
        return ((ApduServiceInfo) o).getComponent().equals(getComponent());
    }

    public int hashCode() {
        return getComponent().hashCode();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        int i = 1;
        this.mService.writeToParcel(dest, flags);
        dest.writeString(this.mDescription);
        dest.writeInt(this.mOnHost ? 1 : 0);
        dest.writeInt(this.mStaticAidGroups.size());
        if (this.mStaticAidGroups.size() > 0) {
            dest.writeTypedList(new ArrayList(this.mStaticAidGroups.values()));
        }
        dest.writeInt(this.mDynamicAidGroups.size());
        if (this.mDynamicAidGroups.size() > 0) {
            dest.writeTypedList(new ArrayList(this.mDynamicAidGroups.values()));
        }
        if (!this.mRequiresDeviceUnlock) {
            i = 0;
        }
        dest.writeInt(i);
        dest.writeInt(this.mBannerResourceId);
        dest.writeInt(this.mUid);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("    " + getComponent() + " (Description: " + getDescription() + ")");
        pw.println("    Static AID groups:");
        for (AidGroup group : this.mStaticAidGroups.values()) {
            pw.println("        Category: " + group.category);
            Iterator i$ = group.aids.iterator();
            while (i$.hasNext()) {
                pw.println("            AID: " + i$.next());
            }
        }
        pw.println("    Dynamic AID groups:");
        for (AidGroup group2 : this.mDynamicAidGroups.values()) {
            pw.println("        Category: " + group2.category);
            Iterator i$2 = group2.aids.iterator();
            while (i$2.hasNext()) {
                pw.println("            AID: " + i$2.next());
            }
        }
    }
}

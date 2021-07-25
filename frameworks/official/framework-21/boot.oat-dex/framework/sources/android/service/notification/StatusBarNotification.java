package android.service.notification;

import android.app.Notification;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.UserHandle;

public class StatusBarNotification implements Parcelable {
    public static final Parcelable.Creator<StatusBarNotification> CREATOR = new Parcelable.Creator<StatusBarNotification>() {
        /* class android.service.notification.StatusBarNotification.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public StatusBarNotification createFromParcel(Parcel parcel) {
            return new StatusBarNotification(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public StatusBarNotification[] newArray(int size) {
            return new StatusBarNotification[size];
        }
    };
    private final String groupKey;
    private final int id;
    private final int initialPid;
    private final String key;
    private final Notification notification;
    private final String opPkg;
    private final String pkg;
    private final long postTime;
    private final int score;
    private final String tag;
    private final int uid;
    private final UserHandle user;

    public StatusBarNotification(String pkg2, String opPkg2, int id2, String tag2, int uid2, int initialPid2, int score2, Notification notification2, UserHandle user2) {
        this(pkg2, opPkg2, id2, tag2, uid2, initialPid2, score2, notification2, user2, System.currentTimeMillis());
    }

    public StatusBarNotification(String pkg2, String opPkg2, int id2, String tag2, int uid2, int initialPid2, int score2, Notification notification2, UserHandle user2, long postTime2) {
        if (pkg2 == null) {
            throw new NullPointerException();
        } else if (notification2 == null) {
            throw new NullPointerException();
        } else {
            this.pkg = pkg2;
            this.opPkg = opPkg2;
            this.id = id2;
            this.tag = tag2;
            this.uid = uid2;
            this.initialPid = initialPid2;
            this.score = score2;
            this.notification = notification2;
            this.user = user2;
            this.postTime = postTime2;
            this.key = key();
            this.groupKey = groupKey();
        }
    }

    public StatusBarNotification(Parcel in) {
        this.pkg = in.readString();
        this.opPkg = in.readString();
        this.id = in.readInt();
        if (in.readInt() != 0) {
            this.tag = in.readString();
        } else {
            this.tag = null;
        }
        this.uid = in.readInt();
        this.initialPid = in.readInt();
        this.score = in.readInt();
        this.notification = new Notification(in);
        this.user = UserHandle.readFromParcel(in);
        this.postTime = in.readLong();
        this.key = key();
        this.groupKey = groupKey();
    }

    private String key() {
        return this.user.getIdentifier() + "|" + this.pkg + "|" + this.id + "|" + this.tag + "|" + this.uid;
    }

    private String groupKey() {
        String group = getNotification().getGroup();
        String sortKey = getNotification().getSortKey();
        if (group == null && sortKey == null) {
            return this.key;
        }
        return this.user.getIdentifier() + "|" + this.pkg + "|" + (group == null ? "p:" + this.notification.priority : "g:" + group);
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.pkg);
        out.writeString(this.opPkg);
        out.writeInt(this.id);
        if (this.tag != null) {
            out.writeInt(1);
            out.writeString(this.tag);
        } else {
            out.writeInt(0);
        }
        out.writeInt(this.uid);
        out.writeInt(this.initialPid);
        out.writeInt(this.score);
        this.notification.writeToParcel(out, flags);
        this.user.writeToParcel(out, flags);
        out.writeLong(this.postTime);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public StatusBarNotification cloneLight() {
        Notification no = new Notification();
        this.notification.cloneInto(no, false);
        return new StatusBarNotification(this.pkg, this.opPkg, this.id, this.tag, this.uid, this.initialPid, this.score, no, this.user, this.postTime);
    }

    public StatusBarNotification clone() {
        return new StatusBarNotification(this.pkg, this.opPkg, this.id, this.tag, this.uid, this.initialPid, this.score, this.notification.clone(), this.user, this.postTime);
    }

    public String toString() {
        return String.format("StatusBarNotification(pkg=%s user=%s id=%d tag=%s score=%d key=%s: %s)", this.pkg, this.user, Integer.valueOf(this.id), this.tag, Integer.valueOf(this.score), this.key, this.notification);
    }

    public boolean isOngoing() {
        return (this.notification.flags & 2) != 0;
    }

    public boolean isClearable() {
        return (this.notification.flags & 2) == 0 && (this.notification.flags & 32) == 0;
    }

    public int getUserId() {
        return this.user.getIdentifier();
    }

    public String getPackageName() {
        return this.pkg;
    }

    public int getId() {
        return this.id;
    }

    public String getTag() {
        return this.tag;
    }

    public int getUid() {
        return this.uid;
    }

    public String getOpPkg() {
        return this.opPkg;
    }

    public int getInitialPid() {
        return this.initialPid;
    }

    public Notification getNotification() {
        return this.notification;
    }

    public UserHandle getUser() {
        return this.user;
    }

    public long getPostTime() {
        return this.postTime;
    }

    public int getScore() {
        return this.score;
    }

    public String getKey() {
        return this.key;
    }

    public String getGroupKey() {
        return this.groupKey;
    }
}

package android.content;

import android.accounts.Account;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class PeriodicSync implements Parcelable {
    public static final Parcelable.Creator<PeriodicSync> CREATOR = new Parcelable.Creator<PeriodicSync>() {
        /* class android.content.PeriodicSync.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public PeriodicSync createFromParcel(Parcel source) {
            return new PeriodicSync(source);
        }

        @Override // android.os.Parcelable.Creator
        public PeriodicSync[] newArray(int size) {
            return new PeriodicSync[size];
        }
    };
    public final Account account;
    public final String authority;
    public final Bundle extras;
    public final long flexTime;
    public final long period;

    public PeriodicSync(Account account2, String authority2, Bundle extras2, long periodInSeconds) {
        this.account = account2;
        this.authority = authority2;
        if (extras2 == null) {
            this.extras = new Bundle();
        } else {
            this.extras = new Bundle(extras2);
        }
        this.period = periodInSeconds;
        this.flexTime = 0;
    }

    public PeriodicSync(PeriodicSync other) {
        this.account = other.account;
        this.authority = other.authority;
        this.extras = new Bundle(other.extras);
        this.period = other.period;
        this.flexTime = other.flexTime;
    }

    public PeriodicSync(Account account2, String authority2, Bundle extras2, long period2, long flexTime2) {
        this.account = account2;
        this.authority = authority2;
        this.extras = new Bundle(extras2);
        this.period = period2;
        this.flexTime = flexTime2;
    }

    private PeriodicSync(Parcel in) {
        this.account = (Account) in.readParcelable(null);
        this.authority = in.readString();
        this.extras = in.readBundle();
        this.period = in.readLong();
        this.flexTime = in.readLong();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.account, flags);
        dest.writeString(this.authority);
        dest.writeBundle(this.extras);
        dest.writeLong(this.period);
        dest.writeLong(this.flexTime);
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof PeriodicSync)) {
            return false;
        }
        PeriodicSync other = (PeriodicSync) o;
        return this.account.equals(other.account) && this.authority.equals(other.authority) && this.period == other.period && syncExtrasEquals(this.extras, other.extras);
    }

    /* JADX WARNING: Removed duplicated region for block: B:8:0x0023  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean syncExtrasEquals(android.os.Bundle r6, android.os.Bundle r7) {
        /*
            r3 = 1
            r2 = 0
            int r4 = r6.size()
            int r5 = r7.size()
            if (r4 == r5) goto L_0x000d
        L_0x000c:
            return r2
        L_0x000d:
            boolean r4 = r6.isEmpty()
            if (r4 == 0) goto L_0x0015
            r2 = r3
            goto L_0x000c
        L_0x0015:
            java.util.Set r4 = r6.keySet()
            java.util.Iterator r0 = r4.iterator()
        L_0x001d:
            boolean r4 = r0.hasNext()
            if (r4 == 0) goto L_0x003e
            java.lang.Object r1 = r0.next()
            java.lang.String r1 = (java.lang.String) r1
            boolean r4 = r7.containsKey(r1)
            if (r4 == 0) goto L_0x000c
            java.lang.Object r4 = r6.get(r1)
            java.lang.Object r5 = r7.get(r1)
            boolean r4 = r4.equals(r5)
            if (r4 != 0) goto L_0x001d
            goto L_0x000c
        L_0x003e:
            r2 = r3
            goto L_0x000c
        */
        throw new UnsupportedOperationException("Method not decompiled: android.content.PeriodicSync.syncExtrasEquals(android.os.Bundle, android.os.Bundle):boolean");
    }

    public String toString() {
        return "account: " + this.account + ", authority: " + this.authority + ". period: " + this.period + "s " + ", flex: " + this.flexTime;
    }
}

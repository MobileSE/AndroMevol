package android.app.job;

import android.app.job.IJobCallback;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.PersistableBundle;

public class JobParameters implements Parcelable {
    public static final Parcelable.Creator<JobParameters> CREATOR = new Parcelable.Creator<JobParameters>() {
        /* class android.app.job.JobParameters.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public JobParameters createFromParcel(Parcel in) {
            return new JobParameters(in);
        }

        @Override // android.os.Parcelable.Creator
        public JobParameters[] newArray(int size) {
            return new JobParameters[size];
        }
    };
    private final IBinder callback;
    private final PersistableBundle extras;
    private final int jobId;
    private final boolean overrideDeadlineExpired;

    public JobParameters(IBinder callback2, int jobId2, PersistableBundle extras2, boolean overrideDeadlineExpired2) {
        this.jobId = jobId2;
        this.extras = extras2;
        this.callback = callback2;
        this.overrideDeadlineExpired = overrideDeadlineExpired2;
    }

    public int getJobId() {
        return this.jobId;
    }

    public PersistableBundle getExtras() {
        return this.extras;
    }

    public boolean isOverrideDeadlineExpired() {
        return this.overrideDeadlineExpired;
    }

    public IJobCallback getCallback() {
        return IJobCallback.Stub.asInterface(this.callback);
    }

    private JobParameters(Parcel in) {
        boolean z = true;
        this.jobId = in.readInt();
        this.extras = in.readPersistableBundle();
        this.callback = in.readStrongBinder();
        this.overrideDeadlineExpired = in.readInt() != 1 ? false : z;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.jobId);
        dest.writePersistableBundle(this.extras);
        dest.writeStrongBinder(this.callback);
        dest.writeInt(this.overrideDeadlineExpired ? 1 : 0);
    }
}

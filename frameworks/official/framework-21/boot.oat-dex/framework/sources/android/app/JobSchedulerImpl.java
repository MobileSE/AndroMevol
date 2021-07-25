package android.app;

import android.app.job.IJobScheduler;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.os.RemoteException;
import java.util.List;

public class JobSchedulerImpl extends JobScheduler {
    IJobScheduler mBinder;

    JobSchedulerImpl(IJobScheduler binder) {
        this.mBinder = binder;
    }

    @Override // android.app.job.JobScheduler
    public int schedule(JobInfo job) {
        try {
            return this.mBinder.schedule(job);
        } catch (RemoteException e) {
            return 0;
        }
    }

    @Override // android.app.job.JobScheduler
    public void cancel(int jobId) {
        try {
            this.mBinder.cancel(jobId);
        } catch (RemoteException e) {
        }
    }

    @Override // android.app.job.JobScheduler
    public void cancelAll() {
        try {
            this.mBinder.cancelAll();
        } catch (RemoteException e) {
        }
    }

    @Override // android.app.job.JobScheduler
    public List<JobInfo> getAllPendingJobs() {
        try {
            return this.mBinder.getAllPendingJobs();
        } catch (RemoteException e) {
            return null;
        }
    }
}

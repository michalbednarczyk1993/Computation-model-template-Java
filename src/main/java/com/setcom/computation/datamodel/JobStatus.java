package com.setcom.computation.datamodel;

public class JobStatus {
    public Status status = Status.IDLE;
    public long jobProgress  = -1;
    public String jobInstanceUid;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public long getJobProgress() {
        return jobProgress;
    }

    public void setJobProgress(long jobProgress) {
        this.jobProgress = jobProgress;
    }

    public String getJobInstanceUid() {
        return jobInstanceUid;
    }

    public void setJobInstanceUid(String jobInstanceUid) {
        this.jobInstanceUid = jobInstanceUid;
    }
}

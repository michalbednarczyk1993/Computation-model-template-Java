package com.setcom.computation.datamodel;

import lombok.Getter;
import lombok.Setter;

public class JobStatus {

    @Getter @Setter
    public Status status = Status.IDLE;

    @Getter @Setter
    public long jobProgress  = -1;

    @Getter @Setter
    public String jobInstanceUid;
}

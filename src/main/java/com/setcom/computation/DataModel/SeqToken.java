package com.setcom.computation.DataModel;

import lombok.Getter;
import lombok.Setter;

public class SeqToken {

    @Getter @Setter
    public String seqUid;

    @Getter @Setter
    public long no;

    @Getter @Setter
    public boolean isFinal;
}

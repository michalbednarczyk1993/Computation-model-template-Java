package com.setcom.computation.datamodel;

import lombok.Getter;
import lombok.Setter;

import java.util.Enumeration;

public class InputTokenMessage {

    @Getter @Setter
    public String msgUid;

    @Getter @Setter
    public String pinName;

    @Getter @Setter
    public String accessType;

    @Getter @Setter
    public String values;

    @Getter @Setter
    public Enumeration<SeqToken> tokenSeqStack;
}

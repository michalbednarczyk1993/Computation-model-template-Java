package com.setcom.computation.DataModel;

import lombok.Getter;
import lombok.Setter;

import java.util.Enumeration;

public class InputTokenMessage {

    @Getter @Setter
    public String MsgUid;

    @Getter @Setter
    public String PinName;

    @Getter @Setter
    public String AccessType;

    @Getter @Setter
    public String Values;

    @Getter @Setter
    public Enumeration<SeqToken> TokenSeqStack;
}

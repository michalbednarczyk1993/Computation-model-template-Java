package com.setcom.computation.datamodel;

import lombok.Getter;
import lombok.Setter;

public class OutputTokenMessage {
    @Getter @Setter
    public String PinName;

    @Getter @Setter
    public String SenderUid;

    @Getter @Setter
    public String Values;

    @Getter @Setter
    public String BaseMsgUid;

    @Getter @Setter
    public boolean IsFinal;
}

package com.setcom.computation.datamodel;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class TokensAck {
    @Getter @Setter
    public List<String> MsgUids;
    @Getter @Setter
    public String SenderUid;
    @Getter @Setter
    public String Note;
    @Getter @Setter
    public boolean IsFailed;
    @Getter @Setter
    public boolean IsFinal;

}

package com.setcom.computation.datamodel;

import lombok.*;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
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

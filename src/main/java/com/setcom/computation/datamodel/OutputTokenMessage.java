package com.setcom.computation.datamodel;

import lombok.*;

@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class OutputTokenMessage {
    @Getter @Setter
    public String pinName;

    @Getter @Setter
    public String senderUid;

    @Getter @Setter
    public String values;

    @Getter @Setter
    public String baseMsgUid;

    @Getter @Setter
    public boolean isFinal;

}

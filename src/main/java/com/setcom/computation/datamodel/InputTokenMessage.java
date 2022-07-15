package com.setcom.computation.datamodel;

import java.util.ArrayList;

public class InputTokenMessage {
    public String msgUid;
    public String pinName;
    public String accessType;
    public String values;
    public ArrayList<SeqToken> tokenSeqStack;

    public String getMsgUid() {
        return msgUid;
    }

    public void setMsgUid(String msgUid) {
        this.msgUid = msgUid;
    }

    public String getPinName() {
        return pinName;
    }

    public void setPinName(String pinName) {
        this.pinName = pinName;
    }

    public String getAccessType() {
        return accessType;
    }

    public void setAccessType(String accessType) {
        this.accessType = accessType;
    }

    public String getValues() {
        return values;
    }

    public void setValues(String values) {
        this.values = values;
    }

    public ArrayList<SeqToken> getTokenSeqStack() {
        return tokenSeqStack;
    }

    public void setTokenSeqStack(ArrayList<SeqToken> tokenSeqStack) {
        this.tokenSeqStack = tokenSeqStack;
    }
}

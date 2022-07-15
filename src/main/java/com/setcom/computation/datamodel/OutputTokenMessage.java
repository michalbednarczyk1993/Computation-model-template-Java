package com.setcom.computation.datamodel;

public class OutputTokenMessage {
    public String pinName;
    public String senderUid;
    public String values;
    public String baseMsgUid;
    public boolean isFinal;

    public OutputTokenMessage() {
    }

    public OutputTokenMessage(String pinName, String senderUid, String values, String baseMsgUid, boolean isFinal) {
        this.pinName = pinName;
        this.senderUid = senderUid;
        this.values = values;
        this.baseMsgUid = baseMsgUid;
        this.isFinal = isFinal;
    }

    public String getPinName() {
        return pinName;
    }

    public void setPinName(String pinName) {
        this.pinName = pinName;
    }

    public String getSenderUid() {
        return senderUid;
    }

    public void setSenderUid(String senderUid) {
        this.senderUid = senderUid;
    }

    public String getValues() {
        return values;
    }

    public void setValues(String values) {
        this.values = values;
    }

    public String getBaseMsgUid() {
        return baseMsgUid;
    }

    public void setBaseMsgUid(String baseMsgUid) {
        this.baseMsgUid = baseMsgUid;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public void setFinal(boolean aFinal) {
        isFinal = aFinal;
    }
}

package com.setcom.computation.datamodel;

import java.util.List;

public class TokensAck {
    public List<String> MsgUids;
    public String SenderUid;
    public String Note;
    public boolean IsFailed;
    public boolean IsFinal;

    public TokensAck() {
    }

    public TokensAck(List<String> msgUids, String senderUid, String note, boolean isFailed, boolean isFinal) {
        MsgUids = msgUids;
        SenderUid = senderUid;
        Note = note;
        IsFailed = isFailed;
        IsFinal = isFinal;
    }

    public List<String> getMsgUids() {
        return MsgUids;
    }

    public void setMsgUids(List<String> msgUids) {
        MsgUids = msgUids;
    }

    public String getSenderUid() {
        return SenderUid;
    }

    public void setSenderUid(String senderUid) {
        SenderUid = senderUid;
    }

    public String getNote() {
        return Note;
    }

    public void setNote(String note) {
        Note = note;
    }

    public boolean isFailed() {
        return IsFailed;
    }

    public void setFailed(boolean failed) {
        IsFailed = failed;
    }

    public boolean isFinal() {
        return IsFinal;
    }

    public void setFinal(boolean aFinal) {
        IsFinal = aFinal;
    }
}

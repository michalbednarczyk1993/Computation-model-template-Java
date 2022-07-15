package com.setcom.computation.datamodel;

public class SeqToken {
    public String seqUid;
    public long no;
    public boolean isFinal;

    public String getSeqUid() {
        return seqUid;
    }

    public void setSeqUid(String seqUid) {
        this.seqUid = seqUid;
    }

    public long getNo() {
        return no;
    }

    public void setNo(long no) {
        this.no = no;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public void setFinal(boolean aFinal) {
        isFinal = aFinal;
    }
}

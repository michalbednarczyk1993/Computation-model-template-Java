package com.setcom.computation.datamodel;

public enum Status {
    IDLE(0),
    WORKING(1),
    COMPLETED(2),
    FAILED(3);

    private final int value;

    Status(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

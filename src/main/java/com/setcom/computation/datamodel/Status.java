package com.setcom.computation.datamodel;

import java.util.HashMap;
import java.util.Map;

public enum Status {
    IDLE(0),
    WORKING(1),
    COMPLETED(2),
    FAILED(3);

    private final int value;

    private static final Map<Object, Object> map = new HashMap<>();

    Status(int value) {
        this.value = value;
    }

    static {
        for (Status status : Status.values()) {
            map.put(status.value, status);
        }
    }

    public static Status valueOf(int status) {
        return (Status) map.get(status);
    }

    public int getValue() {
        return value;
    }
}

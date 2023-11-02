package com.tictacto.tictacto;

import java.util.EventObject;

public class DataEvent extends EventObject {
    private final String data;

    public DataEvent(Object source, String data) {
        super(source);
        this.data = data;
    }

    public String getData() {
        return data;
    }
}

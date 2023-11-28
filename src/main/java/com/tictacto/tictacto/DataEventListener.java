package com.tictacto.tictacto;

public interface DataEventListener {
    void data(DataEvent event);

    // listening to errors is optional
    default void error(DataEvent event) {
        System.out.println("Error: " + event.getData());
    }
}
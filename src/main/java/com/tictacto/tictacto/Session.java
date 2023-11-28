package com.tictacto.tictacto;

public class Session {
    private static final Session instance = new Session();
    private String username;

    private String command;

    private Session() {
    }

    public static Session getInstance() {
        return instance;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}

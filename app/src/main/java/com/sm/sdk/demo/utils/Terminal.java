package com.sm.sdk.demo.utils;

// Terminal class to match the "Terminal" object in the JSON
public class Terminal {
    private String access_token;
    private String refresh_token;
    private Object user; // Assuming 'user' can be null or any type, you might want to adjust this based on the actual type

    // Getters and setters
    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    public Object getUser() {
        return user;
    }

    public void setUser(Object user) {
        this.user = user;
    }
}
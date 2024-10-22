package com.sm.sdk.demo.utils;

public class LoginRequest {
    private final String terminalID;
    private final String deviceSerialNumber;
    private final String password;

    public LoginRequest(String terminalID, String deviceSerialNumber, String password) {
        this.terminalID = terminalID;
        this.deviceSerialNumber = deviceSerialNumber;
        this.password = password;
    }
}
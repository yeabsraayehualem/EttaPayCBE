package com.sm.sdk.demo.utils;

public class KeyDownloadResponse {
    private String kcv;
    private String responseCode;
    private String sessionKey;

    public KeyDownloadResponse(String kcv, String responseCode, String sessionKey) {
        this.kcv = kcv;
        this.responseCode = responseCode;
        this.sessionKey = sessionKey;
    }

    public String getKcv() {
        return kcv;
    }

    public void setKcv(String kcv) {
        this.kcv = kcv;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }
}

package com.sm.sdk.demo.utils;

import android.support.annotation.NonNull;

import java.security.PublicKey;

public class PurchaseResponse {
    String responseCode;
    String rrn;
    String stan;

    public PurchaseResponse(String responseCode, String rrn, String stan) {
        this.responseCode = responseCode;
        this.rrn = rrn;
        this.stan = stan;
    }

    @NonNull
    @Override
    public String toString() {
        return "\nrespnseCode : "+responseCode +"\n" + "rrn : "+rrn +"\n" + "stan : "+stan;
    }
}

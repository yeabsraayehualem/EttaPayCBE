package com.sm.sdk.demo.utils;

public class PurchaseRequest {
    private final String track2;
    private final String pinBlock;


    private final String smartCardData;
    private final String amount;
    private final String smartCardAdditionalData;
    private final int pinLength;




    public PurchaseRequest(String track2, String pinBlock, String smartCardData,  String smartCardAdditionalData, int pinLength ,String amount) {
        this.track2 = track2;
        this.pinBlock = pinBlock;

        this.smartCardData = smartCardData;
        this.amount = amount;
        this.smartCardAdditionalData = smartCardAdditionalData;
        this.pinLength =pinLength;
    }

    public String toString(){
        return "\n"+track2+"\n"+pinBlock+"\n"+smartCardData+"\n"+smartCardAdditionalData + "\n"+pinLength+"\n"+amount;
    }
}
//;379987218983485=270260124025775000000F?
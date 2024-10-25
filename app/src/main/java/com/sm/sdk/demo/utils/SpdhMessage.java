package com.sm.sdk.demo.utils;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class SpdhMessage {
    private String deviceType = "9.";
    private static int transmissionNumber = 0;
    private String terminalId = "ETTA0001";
    private String employeeId = "";
    private String currentDate = "230419";
    private String currentTime = "110655";
    private String messageType = "F";
    private String messageSubType = "O";
    private String transactionCode = "90";
    private String flag1 = "1";
    private String flag2 = "0";
    private String flag3 = "0";
    private String responseCode = "000";
    private String downloadKey = "123456789ABCDEF123456789ABCDEF";
    private ArrayList<String> tags = new ArrayList<>();
    private String amount = "10";
    private String retailer = "RETAIL";
    private String sequenceNumber = "0123456789";
    private String track2Data = "track2";
    private String emvRequestData = "";
    private String emvAdditionalData = "";
    private String emvSupplementaryData = "";
    private String hostOriginalData; // New field for Host Original Data
    private String vTagData = "01";

    public SpdhMessage() {
    }

    public byte[] packHeader() {
        ByteArrayOutputStream var1 = new ByteArrayOutputStream();
        try {
            var1.write(this.deviceType.getBytes());
            ++transmissionNumber;
            var1.write(b.a("0" + transmissionNumber, 2, true).getBytes());
            var1.write(b.a(this.terminalId, 16, true).getBytes());
            var1.write(b.a(this.employeeId, 6, true).getBytes());
            var1.write(b.a(this.currentDate, 6, true).getBytes()); // Fix for current date
            var1.write(b.a(this.currentTime, 6, true).getBytes()); // Fix for current time
            var1.write(b.a(this.messageType, 1, true).getBytes());

            var1.write(b.a(this.messageSubType, 1, true).getBytes());
            var1.write(b.a(this.transactionCode, 2, true).getBytes());
            var1.write(b.a(this.flag1, 1, true).getBytes());
            var1.write(b.a(this.flag2, 1, true).getBytes());
            var1.write(b.a(this.flag3, 1, true).getBytes());
            var1.write(b.a(this.responseCode, 3, true).getBytes());

        } catch (IOException var3) {
            var3.printStackTrace();
        }
        return var1.toByteArray();
    }

    public byte[] pack() {
        ByteArrayOutputStream var1 = new ByteArrayOutputStream();
        try {
            var1.write(this.packHeader());
            if (this.tags.size() > 0) {
                Iterator<String> var3 = this.tags.iterator();
                while (var3.hasNext()) {
                    String var2 = var3.next();
                    a.a("Sent tag:".concat(String.valueOf(var2)));
                    var1.write(this.packTag(var2));
                }
            }
        } catch (IOException var4) {
            var4.printStackTrace();
        }
        return var1.toByteArray();
    }

    public String getVTagData() {
        return this.vTagData;
    }

    public SpdhMessage unpack(byte[] var1) {
        a.a("R:" + b.b(var1));
        int var2 = var1.length;
        byte[] var3 = new byte[48];
        System.arraycopy(var1, 0, var3, 0, 48);
        a.a("H:" + b.b(var3));
        this.unpackHeader(b.b(var3));
        byte[] var6 = new byte[var2 - 48];
        System.arraycopy(var1, 48, var6, 0, var6.length);
        String var4 = b.b(var6);
        a.a("B:".concat(String.valueOf(var4)));
        ArrayList<byte[]> var5 = new ArrayList<>(b.a(var6, (byte)28));
        if (this.tags.size() > 0) {
            Iterator<byte[]> var7 = var5.iterator();
            while (var7.hasNext()) {
                var1 = var7.next();
                a.a("Received tag:" + b.b(var1));
                this.unpackTag(var1);
            }
        }
        return null;
    }

    private void unpackHeader(String var1) {
        this.setResponseCode(var1.substring(var1.length() - 3));
    }

    private void unpackSubtag_6(byte[] var1) {
        ArrayList<byte[]> var3 = new ArrayList<>(b.a(var1, (byte)30));
        if (this.tags.size() > 0) {
            Iterator<byte[]> var2 = var3.iterator();
            while (var2.hasNext()) {
                var1 = var2.next();
                a.a("Received SubTag: " + b.b(var1));
                this.unpackTag(var1);
            }
        }
    }

    private void unpackTag(byte[] var1) {
        String var2 = b.b(var1);
        if (var2.startsWith("6")) {
            this.unpackSubtag_6(var1);
        }
        if (var2.startsWith("V")) {
            // Store the V tag data
            this.vTagData = var2.substring(1); // Assuming data follows the 'V' character
        }
        if (!var2.startsWith("V") && var2.startsWith("W")) {
            this.setDownloadKey(var2.substring(4));
        }
    }

    private byte[] packTag(String var1) {
        ByteArrayOutputStream var2 = new ByteArrayOutputStream();
        try {
            if (var1.startsWith("V")) {
                var2.write(28);
                var2.write("V005g 0000000000".getBytes());
            } else if (var1.startsWith("B")) {
                var2.write(28);
                var2.write(("B" + b.a("000000000000" + this.amount, 12, true)).getBytes());
            } else if (var1.startsWith("b")) {
                var2.write(28);
                var2.write(("b" + b.a("3651A859F872CAFD", 16, true)).getBytes());
            } else if (var1.startsWith("d")) {
                var2.write(28);
                var2.write(("d" + b.a(this.retailer, 12, true)).getBytes());
            } else if (var1.startsWith("h")) {
                var2.write(28);
                var2.write(("h" + b.a("000000000000" + this.sequenceNumber, 10, true)).getBytes());
            } else if (var1.startsWith("q")) {
                var2.write(28);
                var2.write(("q" + this.track2Data).getBytes());
            } else if (var1.startsWith("6-")) {
                var2.write(28);
                var2.write("6".getBytes());
            } else if (var1.startsWith("6E")) {
                var2.write(30);
                var2.write(("E" + b.a("051", 3, true)).getBytes());
            } else if (var1.startsWith("6I")) {
                var2.write(30);
                var2.write(("I" + b.a("230", 3, true)).getBytes());
            } else if (var1.startsWith("6O")) {
                var2.write(30);
                var2.write(("O" + this.emvRequestData).getBytes());
            } else if (var1.startsWith("6P")) {
                var2.write(30);
                var2.write(("P" + this.emvAdditionalData).getBytes());
            } else if (var1.startsWith("6q")) {
                var2.write(30);
                var2.write(("q" + this.emvSupplementaryData).getBytes());
            } else if (var1.startsWith("A")) { // Handle the A tag
                var2.write(28);
                var2.write(("A" + this.hostOriginalData).getBytes());
            } else {
                a.b("TAG not implemented:" + var1.substring(0, 1));
                var2.write(28);
            }
        } catch (IOException var3) {
            var3.printStackTrace();
        }
        return var2.toByteArray();
    }

    public void setPurchaseTags() {
        this.tags.add("B");
        this.tags.add("b");
        this.tags.add("d");
        this.tags.add("h");
        this.tags.add("q");
        this.tags.add("6-");
        this.tags.add("6E");
        this.tags.add("6I");
        this.tags.add("6O");
        this.tags.add("6P");
        this.tags.add("6q");
        this.tags.add("A");
        this.tags.add("V");
    }

    public void setHostOriginalData(String time, String date) {
        // Format: hhmmssMMDD
        this.hostOriginalData = time + date; // e.g., 110655230419
    }
    public String getDeviceType() {
        return this.deviceType;
    }

    public void setDeviceType(String var1) {
        this.deviceType = var1;
    }

    public int getTransmissionNumber() {
        return transmissionNumber;
    }

    public void setTransmissionNumber(int var1) {
        transmissionNumber = var1;
    }

    public String getTerminalId() {
        return this.terminalId;
    }

    public void setTerminalId(String var1) {
        this.terminalId = var1;
    }

    public String getEmployeeId() {
        return this.employeeId;
    }

    public void setEmployeeId(String var1) {
        this.employeeId = var1;
    }

    public String getCurrentDate() {
        return this.currentDate;
    }

    public void setCurrentDate(String var1) {
        this.currentDate = var1;
    }

    public String getCurrentTime() {
        return this.currentTime;
    }

    public void setCurrentTime(String var1) {
        this.currentTime = var1;
    }

    public String getMessageType() {
        return this.messageType;
    }

    public void setMessageType(String var1) {
        this.messageType = var1;
    }

    public String getMessageSubType() {
        return this.messageSubType;
    }

    public void setMessageSubType(String var1) {
        this.messageSubType = var1;
    }

    public String getTransactionCode() {
        return this.transactionCode;
    }

    public void setTransactionCode(String var1) {
        this.transactionCode = var1;
    }

    public String getFlag1() {
        return this.flag1;
    }

    public void setFlag1(String var1) {
        this.flag1 = var1;
    }

    public String getFlag2() {
        return this.flag2;
    }

    public void setFlag2(String var1) {
        this.flag2 = var1;
    }

    public String getFlag3() {
        return this.flag3;
    }

    public void setFlag3(String var1) {
        this.flag3 = var1;
    }

    public String getResponseCode() {
        return this.responseCode;
    }

    public void setResponseCode(String var1) {
        this.responseCode = var1;
        a.a("Response Code:".concat(String.valueOf(var1)));
    }

    public String getDownloadKey() {
        return null;
    }

    public void setDownloadKey(String var1) {
        this.downloadKey = var1;
        a.a("Downloaded key:".concat(String.valueOf(var1)));
    }

    public void setKeyDownloadTags() {
        this.tags.add("V");
    }

    public String getAmount() {
        return this.amount;
    }

    public void setAmount(String var1) {
        this.amount = var1;
    }

    public String getRetailer() {
        return this.retailer;
    }

    public void setRetailer(String var1) {
        this.retailer = var1;
    }

    public String getSequenceNumber() {
        return this.sequenceNumber;
    }

    public void setSequenceNumber(String var1) {
        this.sequenceNumber = var1;
    }

    public String getTrack2Data() {
        return this.track2Data;
    }

    public void setTrack2Data(String var1) {
        this.track2Data = var1;
    }

    public String getEmvRequestData() {
        return this.emvRequestData;
    }

    public void setEmvRequestData(String var1) {
        this.emvRequestData = var1;
    }

    public String getEmvAdditionalData() {
        return this.emvAdditionalData;
    }

    public void setEmvAdditionalData(String var1) {
        this.emvAdditionalData = var1;
    }

    public String getEmvSupplementaryData() {
        return this.emvSupplementaryData;
    }

    public void setEmvSupplementaryData(String var1) {
        this.emvSupplementaryData = var1;
    }
}

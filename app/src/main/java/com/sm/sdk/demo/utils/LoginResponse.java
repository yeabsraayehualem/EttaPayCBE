package com.sm.sdk.demo.utils;

public class LoginResponse {
    private String LoyalityUser;
    private Terminal Terminal;
    private String bankTerminalId;
    private String merchantId;

    // Getters and setters
    public String getLoyalityUser() {
        return LoyalityUser;
    }

    public void setLoyalityUser(String loyalityUser) {
        LoyalityUser = loyalityUser;
    }

    public Terminal getTerminal() {
        return Terminal;
    }

    public void setTerminal(Terminal terminal) {
        Terminal = terminal;
    }

    public String getBankTerminalId() {
        return bankTerminalId;
    }

    public void setBankTerminalId(String bankTerminalId) {
        this.bankTerminalId = bankTerminalId;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }
}


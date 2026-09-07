package com.example.orderms.dto;

public class EncryptedPaymentData {

    private String encryptedCardData;

    public EncryptedPaymentData() {
    }

    public EncryptedPaymentData(String encryptedCardData) {
        this.encryptedCardData = encryptedCardData;
    }

    public String getEncryptedCardData() {
        return encryptedCardData;
    }

    public void setEncryptedCardData(String encryptedCardData) {
        this.encryptedCardData = encryptedCardData;
    }
}
package com.example.orderms.dto;

import jakarta.validation.constraints.NotBlank;

public class CardDataRequest {

    @NotBlank(message = "Card number is required")
    private String cardNumber;

    @NotBlank(message = "Card holder is required")
    private String cardHolder;

    @NotBlank(message = "Expiration date is required")
    private String expirationDate;

    @NotBlank(message = "CVV is required")
    private String cvv;

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardHolder() {
        return cardHolder;
    }

    public void setCardHolder(String cardHolder) {
        this.cardHolder = cardHolder;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }
}
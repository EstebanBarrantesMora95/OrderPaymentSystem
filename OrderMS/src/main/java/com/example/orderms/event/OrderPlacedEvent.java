package com.example.orderms.event;

import java.math.BigDecimal;

public class OrderPlacedEvent {

    private Long orderId;
    private BigDecimal total;
    private String encryptedCardData;

    public OrderPlacedEvent() {
    }

    public OrderPlacedEvent(
            Long orderId,
            BigDecimal total,
            String encryptedCardData
    ) {
        this.orderId = orderId;
        this.total = total;
        this.encryptedCardData = encryptedCardData;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getEncryptedCardData() {
        return encryptedCardData;
    }

    public void setEncryptedCardData(String encryptedCardData) {
        this.encryptedCardData = encryptedCardData;
    }
}
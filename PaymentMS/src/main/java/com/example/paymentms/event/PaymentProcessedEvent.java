package com.example.paymentms.event;

public class PaymentProcessedEvent {

    private Long orderId;
    private boolean approved;
    private String message;

    public PaymentProcessedEvent() {
    }

    public PaymentProcessedEvent(
            Long orderId,
            boolean approved,
            String message) {

        this.orderId = orderId;
        this.approved = approved;
        this.message = message;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "PaymentProcessedEvent{" +
                "orderId=" + orderId +
                ", approved=" + approved +
                ", message='" + message + '\'' +
                '}';
    }
}
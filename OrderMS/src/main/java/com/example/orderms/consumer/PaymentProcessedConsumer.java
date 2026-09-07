package com.example.orderms.consumer;

import com.example.orderms.entity.Order;
import com.example.orderms.entity.OrderStatus;
import com.example.orderms.event.PaymentProcessedEvent;
import com.example.orderms.repository.OrderRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class PaymentProcessedConsumer {

    private final OrderRepository orderRepository;

    public PaymentProcessedConsumer(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @KafkaListener(
            topics = "payment-processed",
            groupId = "order-service"
    )
    public void consume(PaymentProcessedEvent event) {

        System.out.println(
                "Payment result received for order: " + event.getOrderId()
        );

        Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Order not found: " + event.getOrderId()
                        )
                );

        if (event.isApproved()) {
            order.setStatus(OrderStatus.PAID);
        } else {
            order.setStatus(OrderStatus.PAYMENT_FAILED);
        }

        orderRepository.save(order);

        System.out.println(
                "Order " + order.getId()
                        + " updated to: "
                        + order.getStatus()
        );
    }
}
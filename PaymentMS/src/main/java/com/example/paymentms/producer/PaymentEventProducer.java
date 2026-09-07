package com.example.paymentms.producer;

import com.example.paymentms.event.PaymentProcessedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PaymentEventProducer {

    private static final String TOPIC = "payment-processed";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendPaymentProcessedEvent(PaymentProcessedEvent event) {
        kafkaTemplate.send(TOPIC, event);

        System.out.println(
                "Payment event sent to Kafka -> " + event
        );
    }
}
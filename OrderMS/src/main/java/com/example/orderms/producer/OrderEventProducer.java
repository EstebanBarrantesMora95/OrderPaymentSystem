package com.example.orderms.producer;

import com.example.orderms.event.OrderPlacedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderEventProducer {

    private static final String TOPIC = "order-placed";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendOrderPlacedEvent(OrderPlacedEvent event) {

        kafkaTemplate.send(TOPIC, event);

        System.out.println(
                "Evento enviado a Kafka -> " + event
        );
    }
}
package com.example.paymentms.consumer;

import com.example.paymentms.dto.CardData;
import com.example.paymentms.event.OrderPlacedEvent;
import com.example.paymentms.event.PaymentProcessedEvent;
import com.example.paymentms.producer.PaymentEventProducer;
import com.example.paymentms.security.RsaDecryptionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class OrderPlacedConsumer {

    private static final String DECLINED_CARD = "4000000000000002";

    private final RsaDecryptionService rsaDecryptionService;
    private final PaymentEventProducer paymentEventProducer;
    private final ObjectMapper objectMapper;

    public OrderPlacedConsumer(
            RsaDecryptionService rsaDecryptionService,
            PaymentEventProducer paymentEventProducer,
            ObjectMapper objectMapper) {

        this.rsaDecryptionService = rsaDecryptionService;
        this.paymentEventProducer = paymentEventProducer;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "order-placed",
            groupId = "payment-service"
    )
    public void consume(OrderPlacedEvent event) {

        System.out.println(
                "Order received in PaymentMS: " + event.getOrderId()
        );

        try {

            String decryptedCardData =
                    rsaDecryptionService.decrypt(
                            event.getEncryptedCardData()
                    );

            CardData cardData = objectMapper.readValue(
                    decryptedCardData,
                    CardData.class
            );

            boolean approved =
                    !DECLINED_CARD.equals(cardData.getCardNumber());

            PaymentProcessedEvent paymentResult =
                    new PaymentProcessedEvent(
                            event.getOrderId(),
                            approved,
                            approved
                                    ? "Payment approved"
                                    : "Payment declined"
                    );

            paymentEventProducer
                    .sendPaymentProcessedEvent(paymentResult);

        } catch (Exception e) {

            PaymentProcessedEvent paymentResult =
                    new PaymentProcessedEvent(
                            event.getOrderId(),
                            false,
                            "Payment processing failed"
                    );

            paymentEventProducer
                    .sendPaymentProcessedEvent(paymentResult);
        }
    }
}
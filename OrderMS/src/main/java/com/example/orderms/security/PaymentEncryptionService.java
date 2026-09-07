package com.example.orderms.security;

import com.example.orderms.dto.CardDataRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class PaymentEncryptionService {

    private final RsaEncryptionService rsaEncryptionService;
    private final ObjectMapper objectMapper;

    public PaymentEncryptionService(
            RsaEncryptionService rsaEncryptionService,
            ObjectMapper objectMapper) {

        this.rsaEncryptionService = rsaEncryptionService;
        this.objectMapper = objectMapper;
    }

    public String encrypt(CardDataRequest cardData) {
        try {

            String json = objectMapper.writeValueAsString(cardData);

            return rsaEncryptionService.encrypt(json);

        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Could not serialize card data", e
            );
        }
    }
}
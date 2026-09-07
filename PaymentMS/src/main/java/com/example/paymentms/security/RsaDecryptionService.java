package com.example.paymentms.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Service
public class RsaDecryptionService {

    private final PrivateKey privateKey;

    public RsaDecryptionService(
            @Value("${rsa.private-key}") Resource privateKeyResource) {

        try {
            String privateKeyPem = new String(
                    privateKeyResource.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            );

            String privateKeyContent = privateKeyPem
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] decodedKey = Base64.getDecoder()
                    .decode(privateKeyContent);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            this.privateKey = keyFactory.generatePrivate(
                    new PKCS8EncodedKeySpec(decodedKey)
            );

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Could not load RSA private key", e
            );
        }
    }

    public String decrypt(String encryptedData) {
        try {
            Cipher cipher = Cipher.getInstance(
                    "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"
            );

            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            byte[] decryptedBytes = cipher.doFinal(
                    Base64.getDecoder().decode(encryptedData)
            );

            return new String(
                    decryptedBytes,
                    StandardCharsets.UTF_8
            );

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Could not decrypt data", e
            );
        }
    }
}
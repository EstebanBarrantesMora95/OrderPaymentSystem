package com.example.orderms.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Service
public class RsaEncryptionService {

    private final PublicKey publicKey;

    public RsaEncryptionService(
            @Value("${rsa.public-key}") Resource publicKeyResource) {

        try {
            String publicKeyPem = new String(
                    publicKeyResource.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            );

            String publicKeyContent = publicKeyPem
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] decodedKey = Base64.getDecoder()
                    .decode(publicKeyContent);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            this.publicKey = keyFactory.generatePublic(
                    new X509EncodedKeySpec(decodedKey)
            );

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Could not load RSA public key", e
            );
        }
    }

    public String encrypt(String data) {
        try {
            Cipher cipher = Cipher.getInstance(
                    "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"
            );

            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            byte[] encryptedBytes = cipher.doFinal(
                    data.getBytes(StandardCharsets.UTF_8)
            );

            return Base64.getEncoder()
                    .encodeToString(encryptedBytes);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Could not encrypt data", e
            );
        }
    }
}
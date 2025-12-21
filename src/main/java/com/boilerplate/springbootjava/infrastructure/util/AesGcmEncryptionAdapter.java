package com.boilerplate.springbootjava.infrastructure.util;

import com.boilerplate.springbootjava.application.common.port.out.EncryptionPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-GCM 암호화 구현체
 * Infrastructure Layer에 위치
 */
@Component
public class AesGcmEncryptionAdapter implements EncryptionPort {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    // GCM 권장 IV 길이는 12 byte(96 bit)
    private static final int IV_LENGTH = 12;

    private final String secretKey;

    public AesGcmEncryptionAdapter(@Value("${encryption.secret.key}") String secretKey) {
        if (secretKey == null || secretKey.length() != 32) {
            throw new IllegalArgumentException("Secret key must be 32 characters");
        }
        this.secretKey = secretKey;
    }

    @Override
    public String encrypt(String plainText) {
        if (plainText == null) {
            return null;
        }

        try {
            // IV 생성
            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            // 암호화 설정
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(), ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);

            // 암호화 수행
            byte[] encrypted = cipher.doFinal(plainText.getBytes());

            // IV + 암호화된 데이터 결합
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

            // Base64 인코딩
            return Base64.getEncoder().encodeToString(combined);

        } catch (Exception e) {
            throw new RuntimeException("암호화 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public String decrypt(String encryptedText) {
        if (encryptedText == null) {
            return null;
        }

        try {
            // Base64 디코딩
            byte[] combined = Base64.getDecoder().decode(encryptedText);

            // IV와 암호화된 데이터 분리
            byte[] iv = new byte[IV_LENGTH];
            byte[] encrypted = new byte[combined.length - IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, iv.length);
            System.arraycopy(combined, iv.length, encrypted, 0, encrypted.length);

            // 복호화 설정
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(), ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);

            // 복호화 수행
            byte[] decrypted = cipher.doFinal(encrypted);

            return new String(decrypted);

        } catch (Exception e) {
            throw new RuntimeException("복호화 실패: " + e.getMessage(), e);
        }
    }
}
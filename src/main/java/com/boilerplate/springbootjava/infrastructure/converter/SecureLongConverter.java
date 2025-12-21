package com.boilerplate.springbootjava.infrastructure.converter;

import com.boilerplate.springbootjava.application.common.port.out.EncryptionPort;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.stereotype.Component;

/**
 * Long 타입 암호화 Converter
 * Infrastructure Layer에 위치
 */
@Converter
@Component
public class SecureLongConverter implements AttributeConverter<Long, String> {

    private static EncryptionPort encryptionPort;

    public SecureLongConverter(EncryptionPort encryptionPort) {
        SecureLongConverter.encryptionPort = encryptionPort;
    }

    @Override
    public String convertToDatabaseColumn(Long attribute) {
        if (attribute == null) {
            return null;
        }
        String plainText = String.valueOf(attribute);
        return encryptionPort.encrypt(plainText);
    }

    @Override
    public Long convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            String decrypted = encryptionPort.decrypt(dbData);
            return Long.parseLong(decrypted);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Long 변환 실패: " + e.getMessage(), e);
        }
    }
}
package com.boilerplate.springbootjava.infrastructure.converter;

import com.boilerplate.springbootjava.application.common.port.out.EncryptionPort;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.stereotype.Component;

/**
 * Integer 타입 암호화 Converter
 * Infrastructure Layer에 위치
 */
@Converter
@Component
public class SecureIntegerConverter implements AttributeConverter<Integer, String> {

    private static EncryptionPort encryptionPort;

    public SecureIntegerConverter(EncryptionPort encryptionPort) {
        SecureIntegerConverter.encryptionPort = encryptionPort;
    }

    @Override
    public String convertToDatabaseColumn(Integer attribute) {
        if (attribute == null) {
            return null;
        }
        String plainText = String.valueOf(attribute);
        return encryptionPort.encrypt(plainText);
    }

    @Override
    public Integer convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            String decrypted = encryptionPort.decrypt(dbData);
            return Integer.parseInt(decrypted);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Integer 변환 실패: " + e.getMessage(), e);
        }
    }
}
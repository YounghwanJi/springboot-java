package com.boilerplate.springbootjava.infrastructure.converter;

import com.boilerplate.springbootjava.application.common.port.out.EncryptionPort;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.stereotype.Component;


/**
 * String 타입 암호화 Converter
 * Infrastructure Layer에 위치
 */
@Converter
@Component
public class SecureStringConverter implements AttributeConverter<String, String> {

    private static EncryptionPort encryptionPort;

    public SecureStringConverter(EncryptionPort encryptionPort) {
        SecureStringConverter.encryptionPort = encryptionPort;
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        return encryptionPort.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return encryptionPort.decrypt(dbData);
    }
}
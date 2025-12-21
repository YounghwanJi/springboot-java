package com.boilerplate.springbootjava.application.common.port.out;

/**
 * 암호화/복호화를 위한 Port Interface
 * Domain Layer에 위치
 */
public interface EncryptionPort {

    /**
     * 평문을 암호화
     * @param plainText 암호화할 평문
     * @return 암호화된 문자열 (Base64 인코딩)
     */
    String encrypt(String plainText);

    /**
     * 암호문을 복호화
     * @param encryptedText 복호화할 암호문
     * @return 복호화된 평문
     */
    String decrypt(String encryptedText);
}

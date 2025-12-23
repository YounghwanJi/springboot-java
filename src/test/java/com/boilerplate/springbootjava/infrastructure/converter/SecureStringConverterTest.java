package com.boilerplate.springbootjava.infrastructure.converter;

import com.boilerplate.springbootjava.infrastructure.util.AesGcmEncryptionAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * SecureStringConverter 단위 테스트
 * - JPA 컨버터 동작 검증
 * - 암호화/복호화 연동 검증
 */
class SecureStringConverterTest {

    private SecureStringConverter converter;
    private AesGcmEncryptionAdapter encryptionAdapter;

    private static final String TEST_SECRET_KEY = "test-encryption-key-123456789012";

    @BeforeEach
    void setUp() {
        encryptionAdapter = new AesGcmEncryptionAdapter(TEST_SECRET_KEY);
        converter = new SecureStringConverter(encryptionAdapter);
    }

    @Test
    @DisplayName("데이터베이스 저장 시 암호화")
    void convertToDatabaseColumn_EncryptsData() {
        // given
        String plainText = "민감한 데이터";

        // when
        String encrypted = converter.convertToDatabaseColumn(plainText);

        // then
        assertThat(encrypted).isNotNull();
        assertThat(encrypted).isNotEqualTo(plainText);
        assertThat(encrypted).isBase64();
    }

    @Test
    @DisplayName("데이터베이스 저장 시 Null은 Null로 유지")
    void convertToDatabaseColumn_Null_ReturnsNull() {
        // when
        String encrypted = converter.convertToDatabaseColumn(null);

        // then
        assertThat(encrypted).isNull();
    }

    @Test
    @DisplayName("엔티티 조회 시 복호화")
    void convertToEntityAttribute_DecryptsData() {
        // given
        String plainText = "민감한 데이터";
        String encrypted = converter.convertToDatabaseColumn(plainText);

        // when
        String decrypted = converter.convertToEntityAttribute(encrypted);

        // then
        assertThat(decrypted).isEqualTo(plainText);
    }

    @Test
    @DisplayName("엔티티 조회 시 Null은 Null로 유지")
    void convertToEntityAttribute_Null_ReturnsNull() {
        // when
        String decrypted = converter.convertToEntityAttribute(null);

        // then
        assertThat(decrypted).isNull();
    }

    @Test
    @DisplayName("저장/조회 Round-trip 검증")
    void convertToDatabaseAndBack_RoundTrip() {
        // given
        String original = "개인정보: 홍길동";

        // when
        String encrypted = converter.convertToDatabaseColumn(original);
        String restored = converter.convertToEntityAttribute(encrypted);

        // then
        assertThat(restored).isEqualTo(original);
    }

    @Test
    @DisplayName("빈 문자열 처리")
    void convert_EmptyString() {
        // given
        String emptyString = "";

        // when
        String encrypted = converter.convertToDatabaseColumn(emptyString);
        String decrypted = converter.convertToEntityAttribute(encrypted);

        // then
        assertThat(decrypted).isEqualTo(emptyString);
    }

    @Test
    @DisplayName("다양한 문자열 타입 변환 검증")
    void convert_VariousStrings() {
        // given
        String[] testCases = {
                "English Name",
                "한글 이름",
                "010-1234-5678",
                "test@example.com",
                "Special #$%^&*()",
                "Very Long String ".repeat(50)
        };

        // when & then
        for (String testCase : testCases) {
            String encrypted = converter.convertToDatabaseColumn(testCase);
            String decrypted = converter.convertToEntityAttribute(encrypted);
            assertThat(decrypted).isEqualTo(testCase);
        }
    }
}

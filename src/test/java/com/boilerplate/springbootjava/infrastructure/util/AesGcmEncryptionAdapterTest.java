package com.boilerplate.springbootjava.infrastructure.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * AesGcmEncryptionAdapter 단위 테스트
 * - 암호화/복호화 기능 검증
 * - Null 처리 검증
 * - Round-trip 검증
 */
class AesGcmEncryptionAdapterTest {

    // 테스트용 32자 키 (정확히 32자)
    private static final String TEST_SECRET_KEY = "test-encryption-key-123456789012";

    private final AesGcmEncryptionAdapter encryptionAdapter =
            new AesGcmEncryptionAdapter(TEST_SECRET_KEY);

    @Test
    @DisplayName("암호화 - 정상 동작")
    void encrypt_Success() {
        // given
        String plainText = "Hello World";

        // when
        String encrypted = encryptionAdapter.encrypt(plainText);

        // then
        assertThat(encrypted).isNotNull();
        assertThat(encrypted).isNotEqualTo(plainText);
        assertThat(encrypted).isBase64();  // Base64 인코딩 확인
    }

    @Test
    @DisplayName("암호화 - Null 입력 시 Null 반환")
    void encrypt_Null_ReturnsNull() {
        // when
        String encrypted = encryptionAdapter.encrypt(null);

        // then
        assertThat(encrypted).isNull();
    }

    @Test
    @DisplayName("복호화 - 정상 동작")
    void decrypt_Success() {
        // given
        String plainText = "Hello World";
        String encrypted = encryptionAdapter.encrypt(plainText);

        // when
        String decrypted = encryptionAdapter.decrypt(encrypted);

        // then
        assertThat(decrypted).isEqualTo(plainText);
    }

    @Test
    @DisplayName("복호화 - Null 입력 시 Null 반환")
    void decrypt_Null_ReturnsNull() {
        // when
        String decrypted = encryptionAdapter.decrypt(null);

        // then
        assertThat(decrypted).isNull();
    }

    @Test
    @DisplayName("암호화/복호화 Round-trip 검증 - 원본 복원")
    void encryptDecrypt_RoundTrip_RestoresOriginal() {
        // given
        String original = "민감한 개인정보 데이터";

        // when
        String encrypted = encryptionAdapter.encrypt(original);
        String decrypted = encryptionAdapter.decrypt(encrypted);

        // then
        assertThat(decrypted).isEqualTo(original);
    }

    @Test
    @DisplayName("동일 입력에 대해 다른 암호화 결과 (IV 랜덤)")
    void encrypt_SameText_DifferentOutput() {
        // given
        String plainText = "Same Text";

        // when
        String encrypted1 = encryptionAdapter.encrypt(plainText);
        String encrypted2 = encryptionAdapter.encrypt(plainText);

        // then
        assertThat(encrypted1).isNotEqualTo(encrypted2);  // IV가 다르므로 암호화 결과도 다름

        // 하지만 복호화는 동일한 원본으로 복원됨
        assertThat(encryptionAdapter.decrypt(encrypted1)).isEqualTo(plainText);
        assertThat(encryptionAdapter.decrypt(encrypted2)).isEqualTo(plainText);
    }

    @Test
    @DisplayName("잘못된 암호화 데이터 복호화 - 예외 발생")
    void decrypt_InvalidData_ThrowsException() {
        // given
        String invalidEncrypted = "invalid-base64-data!@#$";

        // when & then
        assertThatThrownBy(() -> encryptionAdapter.decrypt(invalidEncrypted))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("복호화 실패");
    }

    @Test
    @DisplayName("잘못된 키 길이 - 예외 발생")
    void constructor_InvalidKeyLength_ThrowsException() {
        // given
        String shortKey = "short-key";  // 32자 미만

        // when & then
        assertThatThrownBy(() -> new AesGcmEncryptionAdapter(shortKey))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Secret key must be 32 characters");
    }

    @Test
    @DisplayName("Null 키 - 예외 발생")
    void constructor_NullKey_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> new AesGcmEncryptionAdapter(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Secret key must be 32 characters");
    }

    @Test
    @DisplayName("다양한 문자열 암호화/복호화 검증")
    void encryptDecrypt_VariousStrings() {
        // given
        String[] testCases = {
                "English Text",
                "한글 텍스트",
                "特殊字符 !@#$%^&*()",
                "123456789",
                "",  // 빈 문자열
                "Very Long String ".repeat(100)
        };

        // when & then
        for (String testCase : testCases) {
            String encrypted = encryptionAdapter.encrypt(testCase);
            String decrypted = encryptionAdapter.decrypt(encrypted);
            assertThat(decrypted).isEqualTo(testCase);
        }
    }
}

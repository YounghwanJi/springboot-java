package com.boilerplate.springbootjava.util;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * JWT 인증을 시뮬레이션하는 커스텀 어노테이션
 * - @WithMockUser 대신 JWT 기반 인증 컨텍스트 생성
 * - 테스트에서 @WithMockJwtUser를 사용하여 인증된 사용자 시뮬레이션
 *
 * 사용 예:
 * <pre>
 * @Test
 * @WithMockJwtUser(email = "test@test.com", role = "USER")
 * void testWithAuthentication() {
 *     // 인증된 사용자로 테스트
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockJwtUserSecurityContextFactory.class)
public @interface WithMockJwtUser {

    /**
     * 사용자 이메일 (기본값: test@test.com)
     */
    String email() default "test@test.com";

    /**
     * 사용자 역할 (기본값: USER)
     * - USER, MANAGER, ADMIN 중 하나
     */
    String role() default "USER";
}

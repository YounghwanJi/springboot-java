package com.boilerplate.springbootjava.util;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.List;

/**
 * @WithMockJwtUser 어노테이션을 위한 SecurityContext 생성 팩토리
 * - JWT 기반 인증을 시뮬레이션하는 SecurityContext 생성
 */
public class WithMockJwtUserSecurityContextFactory
        implements WithSecurityContextFactory<WithMockJwtUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockJwtUser annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        // 역할에 "ROLE_" 접두사 추가
        List<GrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority("ROLE_" + annotation.role()));

        // Authentication 객체 생성
        Authentication auth = new UsernamePasswordAuthenticationToken(
                annotation.email(),
                null,  // credentials (불필요)
                authorities
        );

        context.setAuthentication(auth);
        return context;
    }
}

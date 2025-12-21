package com.boilerplate.springbootjava.infrastructure.filter.security;

import com.boilerplate.springbootjava.application.user.port.out.UserRepository;
import com.boilerplate.springbootjava.infrastructure.config.security.JwtTokenProvider;
import com.boilerplate.springbootjava.infrastructure.persistence.user.UserEntity;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenResolver tokenResolver;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        log.debug("doFilterInternal - Authorization header: {}", request.getHeader("Authorization"));

        tokenResolver.resolve(request)
                .ifPresent(token -> {
                    try {
                        Claims claims = tokenProvider.parseAccessToken(token);
                        String userEmail = claims.getSubject();
                        String role = claims.get("role", String.class);

                        UserEntity user = userRepository.findByEmail(userEmail)
                                .orElseThrow();

                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(
                                        user.getEmail(),
                                        null,
                                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                                );

                        SecurityContextHolder.getContext()
                                .setAuthentication(auth);

                    } catch (Exception e) {
                        SecurityContextHolder.clearContext();
                    }
                });

        filterChain.doFilter(request, response);
    }
}
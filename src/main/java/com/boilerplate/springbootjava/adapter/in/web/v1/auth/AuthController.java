package com.boilerplate.springbootjava.adapter.in.web.v1.auth;

import com.boilerplate.springbootjava.adapter.in.web.v1.auth.dto.LoginRequestDto;
import com.boilerplate.springbootjava.adapter.in.web.v1.auth.dto.RefreshRequestDto;
import com.boilerplate.springbootjava.adapter.in.web.v1.auth.dto.TokenResponseDto;
import com.boilerplate.springbootjava.application.auth.port.out.RefreshTokenRepository;
import com.boilerplate.springbootjava.infrastructure.config.security.CustomUserDetails;
import com.boilerplate.springbootjava.infrastructure.config.security.JwtTokenProvider;
import com.boilerplate.springbootjava.infrastructure.persistence.auth.RefreshTokenEntity;
import com.boilerplate.springbootjava.infrastructure.persistence.user.UserRole;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @PostMapping("/login")
    public TokenResponseDto login(@RequestBody LoginRequestDto request) {
        Authentication auth =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.email(),
                                request.password()
                        )
                );

        CustomUserDetails principal =
                (CustomUserDetails) auth.getPrincipal();

        String accessToken =
                tokenProvider.createAccessToken(
                        principal.getUsername(),
                        principal.getUserRole()
                );

        String refreshToken =
                tokenProvider.createRefreshToken(principal.getUsername());

        refreshTokenRepository.save(
                new RefreshTokenEntity(
                        principal.getUserId(),
                        refreshToken,
                        Instant.now().plusMillis(1209600000)
                )
        );

        return new TokenResponseDto(accessToken, refreshToken);
    }

    @PostMapping("/refresh")
    public TokenResponseDto refresh(@RequestBody RefreshRequestDto request) {

        Claims claims =
                tokenProvider.parseRefreshToken(request.refreshToken());

        String userEmail = claims.getSubject();

        String newAccessToken =
                tokenProvider.createAccessToken(
                        userEmail,
                        UserRole.USER
                );

        return new TokenResponseDto(newAccessToken, request.refreshToken());
    }
}
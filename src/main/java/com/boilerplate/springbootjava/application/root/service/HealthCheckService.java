package com.boilerplate.springbootjava.application.root.service;

import com.boilerplate.springbootjava.adapter.in.web.v1.root.dto.HealthCheckResponseDto;
import com.boilerplate.springbootjava.application.root.port.in.HealthCheckUseCase;
import org.springframework.stereotype.Service;

@Service
public class HealthCheckService implements HealthCheckUseCase {

    private static final String HEALTH_STATUS_UP = "UP";

    public HealthCheckResponseDto getHealth() {
        return HealthCheckResponseDto.builder()
                .status(HEALTH_STATUS_UP)
                .build();
    }
}

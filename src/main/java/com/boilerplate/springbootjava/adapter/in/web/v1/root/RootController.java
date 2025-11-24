package com.boilerplate.springbootjava.adapter.in.web.v1.root;

import com.boilerplate.springbootjava.adapter.in.web.v1.root.dto.AppInfoResponseDto;
import com.boilerplate.springbootjava.adapter.in.web.v1.root.dto.HealthCheckResponseDto;
import com.boilerplate.springbootjava.application.root.port.in.AppInfoUseCase;
import com.boilerplate.springbootjava.application.root.port.in.HealthCheckUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1")
public class RootController {

    private final HealthCheckUseCase healthCheckUseCase;
    private final AppInfoUseCase appInfoUseCase;

    @GetMapping("/health")
    public ResponseEntity<HealthCheckResponseDto> getHealthcheck() {
        return ResponseEntity.ok(healthCheckUseCase.getHealth());
    }

    @GetMapping("/info")
    public ResponseEntity<AppInfoResponseDto> getApplicationInformation() {
        return ResponseEntity.ok(appInfoUseCase.getApplicationInformation());
    }

}

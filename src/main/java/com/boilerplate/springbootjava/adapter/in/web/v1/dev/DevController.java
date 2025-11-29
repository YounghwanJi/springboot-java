package com.boilerplate.springbootjava.adapter.in.web.v1.dev;

import com.boilerplate.springbootjava.adapter.in.web.v1.dev.dto.DevTestResponseDto;
import com.boilerplate.springbootjava.application.dev.port.in.DevUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/dev")
@Profile({"local", "dev"})
public class DevController {

    private final DevUseCase devUseCase;

    @GetMapping("/test")
    public ResponseEntity<DevTestResponseDto> getDevMessage() {
        return ResponseEntity.ok(devUseCase.getMessage());
    }

}
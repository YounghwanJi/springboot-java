package com.boilerplate.springbootjava.application.dev.service;


import com.boilerplate.springbootjava.adapter.in.web.v1.dev.dto.DevTestResponseDto;
import com.boilerplate.springbootjava.application.dev.port.in.DevUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DevService implements DevUseCase {

    public DevTestResponseDto getMessage() {
        return DevTestResponseDto.builder()
                .message("This is a dev message.")
                .build();

    }
}

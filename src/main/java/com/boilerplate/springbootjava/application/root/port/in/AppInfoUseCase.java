package com.boilerplate.springbootjava.application.root.port.in;

import com.boilerplate.springbootjava.adapter.in.web.v1.root.dto.AppInfoResponseDto;

public interface AppInfoUseCase {
    AppInfoResponseDto getApplicationInformation();
}

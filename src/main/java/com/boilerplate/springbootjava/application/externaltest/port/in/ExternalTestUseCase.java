package com.boilerplate.springbootjava.application.externaltest.port.in;

import com.boilerplate.springbootjava.adapter.in.web.v1.externaltest.dto.ExternalItemCreateRequestDto;
import com.boilerplate.springbootjava.adapter.in.web.v1.externaltest.dto.ExternalItemResponseDto;
import com.boilerplate.springbootjava.adapter.in.web.v1.externaltest.dto.ExternalItemUpdateRequestDto;
import com.boilerplate.springbootjava.common.dto.PageResponseDto;
import org.springframework.data.domain.Pageable;

public interface ExternalTestUseCase {
    ExternalItemResponseDto createExternalItem(ExternalItemCreateRequestDto request);

    ExternalItemResponseDto getExternalItem(Long id) ;

    PageResponseDto<ExternalItemResponseDto> getAllExternalItems(Pageable pageable);

    ExternalItemResponseDto updateExternalItem(Long id, ExternalItemUpdateRequestDto request);

    void deleteExternalItem(Long id);
}

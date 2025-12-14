package com.boilerplate.springbootjava.application.externaltest.service;

import com.boilerplate.springbootjava.adapter.in.web.v1.externaltest.dto.ExternalItemCreateRequestDto;
import com.boilerplate.springbootjava.adapter.in.web.v1.externaltest.dto.ExternalItemPageResponseDto;
import com.boilerplate.springbootjava.adapter.in.web.v1.externaltest.dto.ExternalItemResponseDto;
import com.boilerplate.springbootjava.adapter.in.web.v1.externaltest.dto.ExternalItemUpdateRequestDto;
import com.boilerplate.springbootjava.application.externaltest.port.in.ExternalTestUseCase;
import com.boilerplate.springbootjava.common.dto.PageResponseDto;
import com.boilerplate.springbootjava.infrastructure.external.factory.ExternalRestClientFactory;
import com.boilerplate.springbootjava.infrastructure.external.spec.ExternalRequestSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExternalTestService implements ExternalTestUseCase {

    private final ExternalRestClientFactory externalRestClientFactory;

    @Override
    public ExternalItemResponseDto createExternalItem(ExternalItemCreateRequestDto request) {
        ExternalRequestSpec<ExternalItemCreateRequestDto> spec =
                ExternalRequestSpec.<ExternalItemCreateRequestDto>builder()
                        .baseUrl("http://localhost:8100")
                        .path("/api/items")
                        .method(HttpMethod.POST)
                        .headers(Map.of(
                                HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE,
                                HttpHeaders.AUTHORIZATION, "Bearer xxx"
                        ))
                        .body(request)
                        .build();

        return externalRestClientFactory.execute(spec, ExternalItemResponseDto.class);
    }

    @Override
    public ExternalItemResponseDto getExternalItem(Long id) {
        ExternalRequestSpec<Void> spec =
                ExternalRequestSpec.<Void>builder()
                        .baseUrl("http://localhost:8100")
                        .path("/api/items/" + id)
                        .method(HttpMethod.GET)
                        .headers(Map.of(
                                HttpHeaders.AUTHORIZATION, "Bearer xxx"
                        ))
                        .build();

        return externalRestClientFactory.execute(spec, ExternalItemResponseDto.class);
    }

    @Override
    public PageResponseDto<ExternalItemResponseDto> getAllExternalItems(Pageable pageable) {
        int limit = pageable.getPageSize();
        int offset = pageable.getPageNumber() * pageable.getPageSize();

        ExternalRequestSpec<Void> spec =
                ExternalRequestSpec.<Void>builder()
                        .baseUrl("http://localhost:8100")
                        .path("/api/items")
                        .method(HttpMethod.GET)
                        .queryParams(Map.of(
                                "limit", limit,
                                "offset", offset
                        ))
                        .headers(Map.of(
                                HttpHeaders.AUTHORIZATION, "Bearer xxx"
                        ))
                        .build();

        ExternalItemPageResponseDto response = externalRestClientFactory.execute(spec, ExternalItemPageResponseDto.class);

        return PageResponseDto.of(
                response.content(),
                response.offset(),
                response.limit(),
                response.total()
        );
    }

    @Override
    public ExternalItemResponseDto updateExternalItem(Long id, ExternalItemUpdateRequestDto request) {
        ExternalRequestSpec<ExternalItemUpdateRequestDto> spec =
                ExternalRequestSpec.<ExternalItemUpdateRequestDto>builder()
                        .baseUrl("http://localhost:8100")
                        .path("/api/items/" + id)
                        .method(HttpMethod.PUT)
                        .headers(Map.of(
                                HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE,
                                HttpHeaders.AUTHORIZATION, "Bearer xxx"
                        ))
                        .body(request)
                        .build();

        return externalRestClientFactory.execute(spec, ExternalItemResponseDto.class);
    }

    @Override
    public void deleteExternalItem(Long id) {
        ExternalRequestSpec<Void> spec =
                ExternalRequestSpec.<Void>builder()
                        .baseUrl("http://localhost:8100")
                        .path("/api/items/" + id)
                        .method(HttpMethod.DELETE)
                        .headers(Map.of(
                                HttpHeaders.AUTHORIZATION, "Bearer xxx"
                        ))
                        .build();

        externalRestClientFactory.execute(spec, Void.class);
    }
}

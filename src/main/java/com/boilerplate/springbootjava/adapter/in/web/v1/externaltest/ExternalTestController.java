package com.boilerplate.springbootjava.adapter.in.web.v1.externaltest;

import com.boilerplate.springbootjava.adapter.in.web.v1.externaltest.dto.ExternalItemCreateRequestDto;
import com.boilerplate.springbootjava.adapter.in.web.v1.externaltest.dto.ExternalItemResponseDto;
import com.boilerplate.springbootjava.adapter.in.web.v1.externaltest.dto.ExternalItemUpdateRequestDto;
import com.boilerplate.springbootjava.application.externaltest.port.in.ExternalTestUseCase;
import com.boilerplate.springbootjava.common.dto.PageResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/test/external")
public class ExternalTestController {

    private final ExternalTestUseCase externalTestUseCase;

    /**
     * POST /api/v1/test/external
     */
    @PostMapping
    public ResponseEntity<ExternalItemResponseDto> createExternalItem(@Valid @RequestBody ExternalItemCreateRequestDto request) {
        ExternalItemResponseDto response = externalTestUseCase.createExternalItem(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 사용자 조회 (단건)
     * GET /api/v1/test/external/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ExternalItemResponseDto> getExternalItem(@PathVariable Long id) {
        ExternalItemResponseDto response = externalTestUseCase.getExternalItem(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 모든 사용자 조회 (페이징)
     * GET /api/v1/test/external?page=0&size=10&sort=createdAt,desc
     */
    @GetMapping
    public ResponseEntity<PageResponseDto<ExternalItemResponseDto>> getAllExternalItems(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponseDto<ExternalItemResponseDto> response = externalTestUseCase.getAllExternalItems(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * 사용자 수정
     * PUT /api/v1/test/external/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ExternalItemResponseDto> updateExternalItem(
            @PathVariable Long id,
            @Valid @RequestBody ExternalItemUpdateRequestDto request) {
        ExternalItemResponseDto response = externalTestUseCase.updateExternalItem(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 사용자 삭제
     * DELETE /api/v1/test/external/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExternalItem(@PathVariable Long id) {
        externalTestUseCase.deleteExternalItem(id);
        return ResponseEntity.noContent().build();
    }

}
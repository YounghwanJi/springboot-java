package com.boilerplate.springbootjava.common.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponseDto<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrev
) {
    // from: 보통 변환 대상이 한정적일 때 (ex. JPA Page -> DTO)
    // 여러 개의 from을 생성해야 할 경우, fromPage()나 fromJPA() 등으로 분리.
    public static <T> PageResponseDto<T> from(Page<T> page) {
        return new PageResponseDto<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext(),
                page.hasPrevious()
        );
    }

    // of: 여러 파라미터로 직접 인스턴스를 생성할 때 (ex. 값들 -> DTO)
    public static <T> PageResponseDto<T> of(List<T> content, int page, int size, long totalElements) {
        int totalPages = (int) ((totalElements + size - 1) / size);
        boolean hasNext = page + 1 < totalPages;
        boolean hasPrev = page > 0;

        return new PageResponseDto<>(
                content,
                page,
                size,
                totalElements,
                totalPages,
                hasNext,
                hasPrev
        );
    }
}

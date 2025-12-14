package com.boilerplate.springbootjava.common.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CustomErrorResponseDto(
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        Instant timestamp,
        int status,
        String errorCode,
        String message,
        String path,
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        List<ValidationError> validationErrors
) {
    @Builder
    public record ValidationError(
            String objectName,
            String field,
            String code,
            String message,
            Object rejectedValue
    ) {
    }

    public static CustomErrorResponseDto of(HttpStatus status, String errorCode, String message, String path) {
        return new CustomErrorResponseDto(
                Instant.now(),
                status.value(),
                errorCode,
                message,
                path,
                null
        );
    }
}
package com.boilerplate.springbootjava.adapter.in.web.v1.root.dto;

import lombok.Builder;

@Builder
public record HealthCheckResponseDto(String status) {
}

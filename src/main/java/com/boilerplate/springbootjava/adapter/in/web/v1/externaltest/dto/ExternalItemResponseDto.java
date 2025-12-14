package com.boilerplate.springbootjava.adapter.in.web.v1.externaltest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ExternalItemResponseDto(Long id, String name, String description, Integer price, Boolean created) {
}

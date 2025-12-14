package com.boilerplate.springbootjava.adapter.in.web.v1.externaltest.dto;

import java.util.List;

public record ExternalItemPageResponseDto(int total, int offset, int limit,
                                          List<ExternalItemResponseDto> content) {
}

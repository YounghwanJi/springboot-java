package com.boilerplate.springbootjava.adapter.in.web.v1.externaltest.dto;

import java.util.List;

public record ExternalItemPageResponseDto(Integer total, Integer offset, Integer limit,
                                          List<ExternalItemResponseDto> content) {
}

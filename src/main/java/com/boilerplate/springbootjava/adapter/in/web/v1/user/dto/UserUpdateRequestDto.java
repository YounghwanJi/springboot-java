package com.boilerplate.springbootjava.adapter.in.web.v1.user.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserUpdateRequestDto(
        @Size(max = 50, message = "이름은 50자를 초과할 수 없습니다")
        String name,

        @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "올바른 전화번호 형식이 아닙니다")
        String phoneNumber
) {
}

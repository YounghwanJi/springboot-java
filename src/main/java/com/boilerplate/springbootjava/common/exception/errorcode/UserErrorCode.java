package com.boilerplate.springbootjava.common.exception.errorcode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

    // 400
    EMAIL_TYPE_INVALID("USER_40001", HttpStatus.BAD_REQUEST,
            "EMAIL type is invalid."),

    // 409
    EMAIL_CONFLICT("USER_40901", HttpStatus.CONFLICT,
            "EMAIL is already in use."),

    // 404
    USER_NOT_FOUND("USER_40401", HttpStatus.NOT_FOUND,
            "User not found.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;

}

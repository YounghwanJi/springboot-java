package com.boilerplate.springbootjava.common.exception.errorcode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {

    // 400
    XXX_TYPE_INVALID("XXX_40001", HttpStatus.BAD_REQUEST,
            "XXX is invalid."),

    // 403
    XXX_NOT_ALLOWED("XXX_40301", HttpStatus.FORBIDDEN,
            "Not allowed to XXX."),

    // 500
    XXX_SERVER_ERROR("XXX_50001", HttpStatus.INTERNAL_SERVER_ERROR,
            "Server Error XXX.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;

}
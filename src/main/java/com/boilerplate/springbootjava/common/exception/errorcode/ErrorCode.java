package com.boilerplate.springbootjava.common.exception.errorcode;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
    String getCode();

    String getMessage();

    HttpStatus getHttpStatus();
}
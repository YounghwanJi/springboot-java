package com.boilerplate.springbootjava.common.exception.errorcode.mapper;

import com.boilerplate.springbootjava.common.exception.errorcode.ErrorCode;
import org.springframework.http.HttpStatus;

public interface ExternalApiErrorCodeMapper {
    ErrorCode map(HttpStatus status);
}

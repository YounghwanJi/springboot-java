package com.boilerplate.springbootjava.common.exception.errorcode.mapper;

import com.boilerplate.springbootjava.common.exception.errorcode.ErrorCode;
import com.boilerplate.springbootjava.common.exception.errorcode.ItemExternalApiErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ItemExternalApiErrorCodeMapper implements ExternalApiErrorCodeMapper {

    public ErrorCode map(HttpStatus status) {
        if (status.is4xxClientError()) {
            return switch (status) {
//                추가 처리 필요 시, 분리
//                case BAD_REQUEST -> ExternalApiErrorCode.EXTERNAL_BAD_REQUEST;
                case UNAUTHORIZED -> ItemExternalApiErrorCode.EXTERNAL_UNAUTHORIZED;
                case FORBIDDEN -> ItemExternalApiErrorCode.EXTERNAL_FORBIDDEN;
                case NOT_FOUND -> ItemExternalApiErrorCode.EXTERNAL_NOT_FOUND;
                default -> ItemExternalApiErrorCode.EXTERNAL_BAD_REQUEST;
            };
        }

        if (status.is5xxServerError()) {
            return ItemExternalApiErrorCode.EXTERNAL_SERVER_ERROR;
        }

        return ItemExternalApiErrorCode.EXTERNAL_UNKNOWN_ERROR;
    }
}
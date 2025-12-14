package com.boilerplate.springbootjava.common.exception.errorcode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ItemExternalApiErrorCode implements ErrorCode {

    EXTERNAL_BAD_REQUEST(
            "EXTERNAL_400", HttpStatus.BAD_REQUEST,
            "외부 API 요청이 잘못되었습니다."),

    EXTERNAL_UNAUTHORIZED(
            "EXTERNAL_401", HttpStatus.UNAUTHORIZED,
            "외부 API 인증에 실패했습니다."),

    EXTERNAL_FORBIDDEN(
            "EXTERNAL_403", HttpStatus.FORBIDDEN,
            "외부 API 접근이 거부되었습니다."),

    EXTERNAL_NOT_FOUND(
            "EXTERNAL_404", HttpStatus.NOT_FOUND,
            "외부 API 리소스를 찾을 수 없습니다."),

    EXTERNAL_SERVER_ERROR(
            "EXTERNAL_500", HttpStatus.INTERNAL_SERVER_ERROR,
            "외부 API 서버 오류가 발생했습니다."),

    EXTERNAL_UNKNOWN_ERROR(
            "EXTERNAL_UNKNOWN", HttpStatus.INTERNAL_SERVER_ERROR,
            "외부 API 호출 중 알 수 없는 오류가 발생했습니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;

}

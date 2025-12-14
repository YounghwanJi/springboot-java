package com.boilerplate.springbootjava.common.exception;

import com.boilerplate.springbootjava.common.dto.CustomErrorResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class GlobalExceptionHandler {

    /*
     * TODO: 각 errorCode 항목을 각 앱과 Client 사이에 약속, 정의하여 사용하는 것을 권장.
     *       현재는 단순히 error status의 name을 사용 중.
     */
    // 1. Validation 예외 (MethodArgumentNotValidException)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CustomErrorResponseDto> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        List<CustomErrorResponseDto.ValidationError> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> CustomErrorResponseDto.ValidationError.builder()
                        .field(error.getField())
                        .message(error.getDefaultMessage())
                        .rejectedValue(error.getRejectedValue())
                        .build())
                .collect(Collectors.toList());

        CustomErrorResponseDto response = CustomErrorResponseDto.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .errorCode(HttpStatus.BAD_REQUEST.name())
                .message("입력값 검증에 실패했습니다.")
                .path(request.getRequestURI())
                .validationErrors(errors)
                .build();

        log.error("Validation error: {}", errors);
        return ResponseEntity.badRequest().body(response);
    }

    // 2. 잘못된 요청 파라미터 (@RequestParam, @PathVariable)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<CustomErrorResponseDto> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        String message = String.format("'%s' 파라미터의 값 '%s'은(는) 타입이 올바르지 않습니다.",
                ex.getName(), ex.getValue());

        CustomErrorResponseDto response = CustomErrorResponseDto.of(
                HttpStatus.BAD_REQUEST,
                HttpStatus.BAD_REQUEST.name(),
                message,
                request.getRequestURI()
        );

        log.error("Type mismatch: {}", message);
        return ResponseEntity.badRequest().body(response);
    }

    // 3. HTTP 요청 본문 파싱 실패
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<CustomErrorResponseDto> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        CustomErrorResponseDto response = CustomErrorResponseDto.of(
                HttpStatus.BAD_REQUEST,
                HttpStatus.BAD_REQUEST.name(),
                "요청 본문을 읽을 수 없습니다. JSON 형식을 확인해주세요.",
                request.getRequestURI()
        );

        log.error("Message not readable: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    // 4. 지원하지 않는 HTTP Method
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<CustomErrorResponseDto> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request) {

        String message = String.format("'%s' 메서드는 지원하지 않습니다. 지원 메서드: %s",
                ex.getMethod(), Arrays.toString(ex.getSupportedMethods()));

        CustomErrorResponseDto response = CustomErrorResponseDto.of(
                HttpStatus.METHOD_NOT_ALLOWED,
                HttpStatus.METHOD_NOT_ALLOWED.name(),
                message,
                request.getRequestURI()
        );

        log.error("Method not supported: {}", message);
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    // 5. 잘못된 MediaType
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<CustomErrorResponseDto> handleMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex,
            HttpServletRequest request) {

        String message = String.format("'%s' 미디어 타입은 지원하지 않습니다. 지원 타입: %s",
                ex.getContentType(), ex.getSupportedMediaTypes());

        CustomErrorResponseDto response = CustomErrorResponseDto.of(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                HttpStatus.UNSUPPORTED_MEDIA_TYPE.name(),
                message,
                request.getRequestURI()
        );

        log.error("Media type not supported: {}", message);
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(response);
    }

    // 6. 필수 파라미터 누락
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<CustomErrorResponseDto> handleMissingParameter(
            MissingServletRequestParameterException ex,
            HttpServletRequest request) {

        String message = String.format("필수 파라미터 '%s'이(가) 누락되었습니다.", ex.getParameterName());

        CustomErrorResponseDto response = CustomErrorResponseDto.of(
                HttpStatus.BAD_REQUEST,
                HttpStatus.BAD_REQUEST.name(),
                message,
                request.getRequestURI()
        );

        log.error("Missing parameter: {}", message);
        return ResponseEntity.badRequest().body(response);
    }

    // 7. 비즈니스 로직 예외 (Custom Exception)
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<CustomErrorResponseDto> handleCustomException(
            CustomException ex,
            HttpServletRequest request) {

        CustomErrorResponseDto response = CustomErrorResponseDto.of(
                ex.getErrorCode().getHttpStatus(),
                ex.getErrorCode().getCode(),
                ex.getMessage(),
                request.getRequestURI()
        );

        log.error("Custom exception: {}", ex.getMessage());
        return ResponseEntity.status(ex.getErrorCode().getHttpStatus()).body(response);
    }

    // 8. 리소스를 찾을 수 없음
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<CustomErrorResponseDto> handleNoResourceFound(
            NoResourceFoundException ex,
            HttpServletRequest request) {

        CustomErrorResponseDto response = CustomErrorResponseDto.of(
                HttpStatus.NOT_FOUND,
                HttpStatus.NOT_FOUND.name(),
                "요청한 리소스를 찾을 수 없습니다.",
                request.getRequestURI()
        );

        log.error("Resource not found: {}", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // 9. 데이터베이스 제약조건 위반
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<CustomErrorResponseDto> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {

        CustomErrorResponseDto response = CustomErrorResponseDto.of(
                HttpStatus.CONFLICT,
                HttpStatus.CONFLICT.name(),
                "데이터 무결성 제약조건을 위반했습니다.",
                request.getRequestURI()
        );

        log.error("Data integrity violation: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    // 10. 접근 권한 없음 (Spring Security 사용 시)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<CustomErrorResponseDto> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request) {

        CustomErrorResponseDto response = CustomErrorResponseDto.of(
                HttpStatus.FORBIDDEN,
                HttpStatus.FORBIDDEN.name(),
                "접근 권한이 없습니다.",
                request.getRequestURI()
        );

        log.error("Access denied: {}", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    // 나머지 모든 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomErrorResponseDto> handleException(Exception e, HttpServletRequest request) {
        CustomErrorResponseDto response = CustomErrorResponseDto.of(
                HttpStatus.INTERNAL_SERVER_ERROR,
                HttpStatus.INTERNAL_SERVER_ERROR.name(),
                "Internal Server Error",
                request.getRequestURI()
        );

        log.error("Unexpected error: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
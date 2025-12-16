package com.boilerplate.springbootjava.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LoggingFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Request를 여러 번 읽을 수 있도록 래핑
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            logRequest(requestWrapper);
            logResponse(responseWrapper);
            responseWrapper.copyBodyToResponse(); // 중요: 응답 바디를 실제로 전송
        }
    }

    private void logRequest(ContentCachingRequestWrapper request) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n==== Request ====\n");
        sb.append("URI: ").append(request.getMethod()).append(" ").append(request.getRequestURI()).append("\n");

        // Headers
        sb.append("Headers:\n");
        Collections.list(request.getHeaderNames()).forEach(headerName ->
                sb.append("  ").append(headerName).append(": ").append(request.getHeader(headerName)).append("\n")
        );

        // Body
        byte[] content = request.getContentAsByteArray();
        if (content.length > 0) {
            String body = new String(content, StandardCharsets.UTF_8);
            sb.append("Body: ").append(body).append("\n");
        }

        logger.info(sb.toString());
    }

    private void logResponse(ContentCachingResponseWrapper response) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n==== Response ====\n");
        sb.append("Status: ").append(response.getStatus()).append("\n");

        // Headers
        sb.append("Headers:\n");
        response.getHeaderNames().forEach(headerName ->
                sb.append("  ").append(headerName).append(": ").append(response.getHeader(headerName)).append("\n")
        );

        // Body
        byte[] content = response.getContentAsByteArray();
        if (content.length > 0) {
            String body = new String(content, StandardCharsets.UTF_8);
            sb.append("Body: ").append(formatJson(body)).append("\n");
        }

        logger.info(sb.toString());
    }

    // Body item 사이에 줄바꿈 '\n' 추가.
    private String formatJson(String json) {
        try {
            Object jsonObject = objectMapper.readValue(json, Object.class);
            return "\n" + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
        } catch (Exception e) {
            // JSON이 아닌 경우 원본 그대로 반환
            return json;
        }
    }
}
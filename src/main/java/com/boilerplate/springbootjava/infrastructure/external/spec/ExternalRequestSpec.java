package com.boilerplate.springbootjava.infrastructure.external.spec;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpMethod;

import java.util.Map;

@Getter
@Builder
public class ExternalRequestSpec<T> {

    private String baseUrl;
    private String path;
    private HttpMethod method;
    private Map<String, String> headers;
    private Map<String, Object> queryParams;
    private T body;
}
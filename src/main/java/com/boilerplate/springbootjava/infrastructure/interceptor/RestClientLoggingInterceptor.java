package com.boilerplate.springbootjava.infrastructure.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class RestClientLoggingInterceptor implements ClientHttpRequestInterceptor {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AtomicInteger counter = new AtomicInteger(1);

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {

        int requestNumber = getNextNumber();

        logRequest(requestNumber, request, body);

        // Response를 로깅하기 위해 버퍼링
        ClientHttpResponse response = execution.execute(request, body);
        ClientHttpResponse bufferedResponse = new BufferingClientHttpResponseWrapper(response);

        logResponse(requestNumber, bufferedResponse);

        return bufferedResponse;
    }

    private void logRequest(int number, HttpRequest request, byte[] body) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n").append(number).append(" > ==== Outgoing Request ====\n");
        sb.append(number).append(" > URI: ").append(request.getMethod()).append(" ").append(request.getURI()).append("\n");

        sb.append(number).append(" > Headers:\n");
        request.getHeaders().forEach((name, values) ->
                values.forEach(value ->
                        sb.append(number).append(" >   ").append(name).append(": ").append(value).append("\n")
                )
        );

        if (body.length > 0) {
            String bodyStr = new String(body, StandardCharsets.UTF_8);
            String formattedBody = formatJson(bodyStr);
            // Body의 각 라인에도 prefix 추가
            String[] lines = formattedBody.split("\n");
            sb.append(number).append(" > Body:");
            for (String line : lines) {
                sb.append("\n").append(number).append(" > ").append(line);
            }
            sb.append("\n");
        }

        log.info(sb.toString());
    }

    private void logResponse(int number, ClientHttpResponse response) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("\n").append(number).append(" < ==== Incoming Response ====\n");
        sb.append(number).append(" < Status: ").append(response.getStatusCode().value())
                .append(" ").append(response.getStatusText()).append("\n");

        sb.append(number).append(" < Headers:\n");
        response.getHeaders().forEach((name, values) ->
                values.forEach(value ->
                        sb.append(number).append(" <   ").append(name).append(": ").append(value).append("\n")
                )
        );

        String body = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
        if (!body.isEmpty()) {
            String formattedBody = formatJson(body);
            String[] lines = formattedBody.split("\n");
            sb.append(number).append(" < Body:");
            for (String line : lines) {
                sb.append("\n").append(number).append(" < ").append(line);
            }
            sb.append("\n");
        }

        log.info(sb.toString());
    }

    private String formatJson(String json) {
        try {
            Object jsonObject = objectMapper.readValue(json, Object.class);
            return "\n" + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
        } catch (Exception e) {
            return json;
        }
    }

    // Response Body를 여러 번 읽을 수 있도록 버퍼링하는 래퍼
    private static class BufferingClientHttpResponseWrapper implements ClientHttpResponse {

        private final ClientHttpResponse response;
        private byte[] body;

        public BufferingClientHttpResponseWrapper(ClientHttpResponse response) {
            this.response = response;
        }

        @Override
        public HttpStatusCode getStatusCode() throws IOException {
            return response.getStatusCode();
        }

        @Override
        public String getStatusText() throws IOException {
            return response.getStatusText();
        }

        @Override
        public void close() {
            response.close();
        }

        @Override
        public InputStream getBody() throws IOException {
            if (body == null) {
                body = StreamUtils.copyToByteArray(response.getBody());
            }
            return new ByteArrayInputStream(body);
        }

        @Override
        public HttpHeaders getHeaders() {
            return response.getHeaders();
        }
    }

    private int getNextNumber() {
        int current = counter.getAndIncrement();
        // Integer.MAX_VALUE에 도달하면 1로 리셋
        if (current == Integer.MAX_VALUE) {
            counter.set(1);
        }
        return current;
    }
}
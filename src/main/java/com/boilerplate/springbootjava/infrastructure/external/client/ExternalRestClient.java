package com.boilerplate.springbootjava.infrastructure.external.client;

import com.boilerplate.springbootjava.infrastructure.external.spec.ExternalRequestSpec;
import com.boilerplate.springbootjava.infrastructure.external.spec.ExternalResponseSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class ExternalRestClient {

    private final RestClient.Builder restClientBuilder;

    public <T, R> R call(
            ExternalRequestSpec<T> request,
            ExternalResponseSpec<R> responseSpec
    ) {
        RestClient client = restClientBuilder
                .baseUrl(request.getBaseUrl())
                .build();

        RestClient.RequestBodySpec requestSpec =
                client.method(request.getMethod())
                        .uri(uriBuilder -> {
                            uriBuilder.path(request.getPath());

                            if (request.getQueryParams() != null) {
                                request.getQueryParams()
                                        .forEach(uriBuilder::queryParam);
                            }
                            return uriBuilder.build();
                        });

        if (request.getHeaders() != null) {
            request.getHeaders().forEach(requestSpec::header);
        }

        if (request.getBody() != null) {
            requestSpec.body(request.getBody());
        }

        return requestSpec
                .retrieve()
                .body(responseSpec.getResponseType());
    }
}
package com.boilerplate.springbootjava.infrastructure.external.factory;

import com.boilerplate.springbootjava.infrastructure.external.client.ExternalRestClient;
import com.boilerplate.springbootjava.infrastructure.external.spec.ExternalRequestSpec;
import com.boilerplate.springbootjava.infrastructure.external.spec.ExternalResponseSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExternalRestClientFactory {

    private final ExternalRestClient externalRestClient;

    public <T, R> R execute(
            ExternalRequestSpec<T> request,
            Class<R> responseType
    ) {
        return externalRestClient.call(
                request,
                new ExternalResponseSpec<>(responseType)
        );
    }
}
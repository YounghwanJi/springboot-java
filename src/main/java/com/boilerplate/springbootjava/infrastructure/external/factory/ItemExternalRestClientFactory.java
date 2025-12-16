package com.boilerplate.springbootjava.infrastructure.external.factory;

import com.boilerplate.springbootjava.infrastructure.external.client.ItemExternalRestClient;
import com.boilerplate.springbootjava.infrastructure.external.spec.ExternalRequestSpec;
import com.boilerplate.springbootjava.infrastructure.external.spec.ExternalResponseSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ItemExternalRestClientFactory {

    private final ItemExternalRestClient itemExternalRestClient;

    public <T, R> R execute(
            ExternalRequestSpec<T> request,
            Class<R> responseType
    ) {
        return itemExternalRestClient.call(
                request,
                new ExternalResponseSpec<>(responseType)
        );
    }
}
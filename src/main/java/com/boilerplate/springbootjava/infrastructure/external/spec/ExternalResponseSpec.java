package com.boilerplate.springbootjava.infrastructure.external.spec;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ExternalResponseSpec<R> {

    private final Class<R> responseType;
}

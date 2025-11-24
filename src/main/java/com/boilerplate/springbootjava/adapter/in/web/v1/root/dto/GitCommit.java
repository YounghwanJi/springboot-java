package com.boilerplate.springbootjava.adapter.in.web.v1.root.dto;

import lombok.Builder;

import java.time.Instant;

@Builder
public record GitCommit(String id, Instant time) {
}

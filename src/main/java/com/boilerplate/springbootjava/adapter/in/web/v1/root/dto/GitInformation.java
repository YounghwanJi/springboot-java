package com.boilerplate.springbootjava.adapter.in.web.v1.root.dto;

import lombok.Builder;

@Builder
public record GitInformation(String branch, GitCommit commit) {
}
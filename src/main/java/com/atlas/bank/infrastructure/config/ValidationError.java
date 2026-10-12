package com.atlas.bank.infrastructure.config;

public record ValidationError(
        String field,
        String message
) {}
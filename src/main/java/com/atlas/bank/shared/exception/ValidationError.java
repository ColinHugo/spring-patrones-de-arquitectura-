package com.atlas.bank.shared.exception;

public record ValidationError(
        String field,
        String message
) {}
package com.assignment.gatewayguardrails.api.dto;

import java.time.Instant;

public record PostResponse(
        Long id,
        Long authorId,
        String authorType,
        String content,
        Instant createdAt
) {
}


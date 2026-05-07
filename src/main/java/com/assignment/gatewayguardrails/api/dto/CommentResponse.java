package com.assignment.gatewayguardrails.api.dto;

import java.time.Instant;

public record CommentResponse(
        Long id,
        Long postId,
        Long authorId,
        String authorType,
        String content,
        int depthLevel,
        Instant createdAt
) {
}


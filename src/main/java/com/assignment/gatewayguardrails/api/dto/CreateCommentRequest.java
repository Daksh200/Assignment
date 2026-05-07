package com.assignment.gatewayguardrails.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCommentRequest(
        @NotNull Long authorId,
        @NotBlank String authorType, // "USER" | "BOT"
        @NotBlank @Size(max = 4000) String content,
        int depthLevel
) {
}


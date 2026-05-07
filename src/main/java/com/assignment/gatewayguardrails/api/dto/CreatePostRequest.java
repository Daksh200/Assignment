package com.assignment.gatewayguardrails.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreatePostRequest(
        @NotNull Long authorId,
        @NotBlank String authorType, // "USER" | "BOT"
        @NotBlank @Size(max = 5000) String content
) {
}


package com.assignment.gatewayguardrails.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

public record CreateLikeRequest(
        @NotNull Long authorId,
        @NotBlank String authorType // treated as human like
) {
}


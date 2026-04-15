package org.example.library.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateAuthorRequest(
        @NotBlank
        String name,
        @NotBlank
        String email
) {
}

package org.example.library.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAuthorRequest(
        @NotBlank
        @Size(min = 1,max = 255)
        String name,
        @NotBlank
        @Email
        @Size(max = 255)
        String email
) {
}

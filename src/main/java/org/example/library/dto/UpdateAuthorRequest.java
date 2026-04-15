package org.example.library.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateAuthorRequest(
        @Size(min = 1,max = 255)
        String name,
        @Email
        @Size(max = 255)
        String email
) {
}

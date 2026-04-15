package org.example.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateBookRequest(
        @NotBlank
        String title,

        @NotBlank
        String isbn,

        String genre,

        String[] tags,

        @NotNull
        Long authorId
) {
}

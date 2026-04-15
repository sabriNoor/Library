package org.example.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateBookRequest(
        @NotBlank
        @Size(min=1,max = 255)
        String title,

        @NotBlank
        @Size(min=1,max = 50)
        String isbn,

        @Size(min=1,max = 100)
        String genre,

        String[] tags,

        @NotNull
        Long authorId
) {
}

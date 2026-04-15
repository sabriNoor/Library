package org.example.library.dto;

import jakarta.validation.constraints.Size;

public record UpdateBookRequest(
        @Size(min=1,max = 255)
        String title,
        @Size(min=1,max = 50)
        String isbn,
        @Size(min=1,max = 100)
        String genre,
        String[] tags
) {
}

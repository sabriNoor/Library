package org.example.library.dto;

public record AuthorResponse(
        Long id,
        String email,
        String name
) {
}

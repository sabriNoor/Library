package org.example.library.dto;

public record BookResponse(
        Long id,
        String isbn,
        String title,
        String authorName,
        String genre,
        String[] tags,
        boolean available,
        Long authorId
) {
}

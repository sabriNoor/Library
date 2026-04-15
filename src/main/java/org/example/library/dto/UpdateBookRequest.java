package org.example.library.dto;

public record UpdateBookRequest(
        String title,
        String isbn,
        String genre,
        String[] tags
) {
}

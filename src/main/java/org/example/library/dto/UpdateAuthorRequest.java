package org.example.library.dto;

public record UpdateAuthorRequest(
        String name,
        String email
) {
}

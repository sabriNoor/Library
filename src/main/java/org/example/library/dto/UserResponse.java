package org.example.library.dto;

public record UserResponse(
        Long id,
        String email,
        String name
) {
}

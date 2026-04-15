package org.example.library.dto;

import jakarta.validation.constraints.NotNull;

public record BorrowRequest(
        @NotNull
        Long userId,

        @NotNull
        Long bookId
) {
}

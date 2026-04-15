package org.example.library.dto;

import java.time.LocalDateTime;

public record BorrowResponse(
        Long id,
        Long userId,
        String userName,
        Long bookId,
        String bookTitle,
        LocalDateTime borrowDate,
        LocalDateTime returnDate
) {}
package org.example.library.dto;

public record MostBorrowedBook(
        Long bookId,
        Long borrowCount
) {}
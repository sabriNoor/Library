package org.example.library.service;

import org.example.library.dto.BorrowRequest;
import org.example.library.dto.BorrowResponse;
import org.example.library.dto.MostBorrowedBook;

import java.time.LocalDateTime;
import java.util.List;

public interface BorrowService {

    BorrowResponse borrowBook(BorrowRequest request);

    BorrowResponse returnBook(Long borrowId);

    List<BorrowResponse> getUserActiveBorrows(Long userId);

    List<BorrowResponse> getUserBorrowHistory(Long userId);

    List<MostBorrowedBook> getMostBorrowedBooksSince(LocalDateTime date);

}

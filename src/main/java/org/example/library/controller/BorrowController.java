package org.example.library.controller;

import jakarta.validation.Valid;
import org.example.library.dto.BookIdsRequest;
import org.example.library.dto.BorrowRequest;
import org.example.library.dto.BorrowResponse;
import org.example.library.dto.MostBorrowedBook;
import org.example.library.service.BorrowService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/borrows")
public class BorrowController {

    private final BorrowService borrowService;

    public BorrowController(BorrowService borrowService) {
        this.borrowService = borrowService;
    }

    // ✅ BORROW BOOK
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BorrowResponse borrowBook(@Valid @RequestBody BorrowRequest request) {
        return borrowService.borrowBook(request);
    }

    // ✅ RETURN BOOK
    @PutMapping("/{borrowId}/return")
    public BorrowResponse returnBook(@PathVariable Long borrowId) {
        return borrowService.returnBook(borrowId);
    }

    // ✅ GET ACTIVE BORROWS FOR USER
    @GetMapping("/user/{userId}/active")
    public List<BorrowResponse> getUserActiveBorrows(@PathVariable Long userId) {
        return borrowService.getUserActiveBorrows(userId);
    }

    // ✅ GET BORROW HISTORY FOR USER
    @GetMapping("/user/{userId}/history")
    public List<BorrowResponse> getUserBorrowHistory(@PathVariable Long userId) {
        return borrowService.getUserBorrowHistory(userId);
    }

    @GetMapping("/most-borrowed")
    public List<MostBorrowedBook> getMostBorrowedBooksSince(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date) {
        return borrowService.getMostBorrowedBooksSince(date);
    }

    @PutMapping("/return")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markBooksAsAvailable(@Valid @RequestBody BookIdsRequest request) {
        borrowService.markBorrowsAsReturned(request);
    }
}
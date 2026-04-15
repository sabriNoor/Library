package org.example.library.service.impl;

import jakarta.transaction.Transactional;
import org.example.library.dto.BorrowRequest;
import org.example.library.dto.BorrowResponse;
import org.example.library.dto.MostBorrowedBook;
import org.example.library.entity.Book;
import org.example.library.entity.Borrow;
import org.example.library.entity.User;
import org.example.library.exception.BadRequestException;
import org.example.library.exception.ConflictException;
import org.example.library.exception.OperationNotAllowedException;
import org.example.library.exception.ResourceNotFoundException;
import org.example.library.repository.BookRepository;
import org.example.library.repository.BorrowRepository;
import org.example.library.repository.UserRepository;
import org.example.library.service.BorrowService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
@Service
public class BorrowServiceImpl implements BorrowService {

    private final BorrowRepository borrowRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public BorrowServiceImpl(BorrowRepository borrowRepository,
                             BookRepository bookRepository,
                             UserRepository userRepository) {
        this.borrowRepository = borrowRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public BorrowResponse borrowBook(BorrowRequest request) {

        User user = userRepository.findById(request.userId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with id: " + request.userId()));

        Book book = bookRepository.findById(request.bookId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Book not found with id: " + request.bookId()));

        // ✅ Business rule: book already borrowed
        if (borrowRepository.existsByBookIdAndReturnDateIsNull(book.getId())) {
            throw new ConflictException("Book is already borrowed");
        }

        Borrow borrow = Borrow.builder()
                .user(user)
                .book(book)
                .borrowDate(LocalDateTime.now())
                .build();

        Borrow saved = borrowRepository.save(borrow);

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public BorrowResponse returnBook(Long borrowId) {

        Borrow borrow = borrowRepository.findById(borrowId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Borrow not found with id: " + borrowId));

        // ✅ Business rule: already returned
        if (borrow.getReturnDate() != null) {
            throw new OperationNotAllowedException("Book already returned");
        }

        borrow.setReturnDate(LocalDateTime.now());

        Borrow saved = borrowRepository.save(borrow);

        return mapToResponse(saved);
    }

    @Override
    public List<BorrowResponse> getUserActiveBorrows(Long userId) {
        return borrowRepository.findActiveUserBorrowsWithBooksAndUser(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<BorrowResponse> getUserBorrowHistory(Long userId) {
        return borrowRepository.findBorrowHistoryWithDetails(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<MostBorrowedBook> getMostBorrowedBooksSince(LocalDateTime date) {
        if (date == null) {
            throw new BadRequestException("Date must not be null");
        }

        return borrowRepository.findMostBorrowedBooksSince(date);
    }

    private BorrowResponse mapToResponse(Borrow borrow) {
        return new BorrowResponse(
                borrow.getId(),
                borrow.getUser().getId(),
                borrow.getUser().getName(),
                borrow.getBook().getId(),
                borrow.getBook().getTitle(),
                borrow.getBorrowDate(),
                borrow.getReturnDate()
        );
    }
}
package org.example.library.service;

import org.example.library.dto.*;
import org.example.library.entity.Book;
import org.example.library.entity.Borrow;
import org.example.library.entity.User;
import org.example.library.exception.*;
import org.example.library.repository.BookRepository;
import org.example.library.repository.BorrowRepository;
import org.example.library.repository.UserRepository;
import org.example.library.service.impl.BorrowServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BorrowServiceImplTest {

    @Mock
    private BorrowRepository borrowRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BorrowServiceImpl borrowService;

    // ✅ BORROW BOOK SUCCESS
    @Test
    void shouldBorrowBookSuccessfully() {
        BorrowRequest request = new BorrowRequest(1L, 1L);

        User user = User.builder().id(1L).name("User").build();
        Book book = Book.builder().id(1L).title("Book").available(true).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(borrowRepository.existsByBookIdAndReturnDateIsNull(1L)).thenReturn(false);

        Borrow saved = Borrow.builder()
                .id(1L)
                .user(user)
                .book(book)
                .borrowDate(LocalDateTime.now())
                .build();

        when(borrowRepository.save(any())).thenReturn(saved);

        var response = borrowService.borrowBook(request);

        assertEquals(1L, response.bookId());
        verify(bookRepository).save(book);
    }

    // ❌ USER NOT FOUND
    @Test
    void shouldThrowWhenUserNotFound() {
        BorrowRequest request = new BorrowRequest(1L, 1L);

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                borrowService.borrowBook(request)
        );
    }

    // ❌ BOOK NOT FOUND
    @Test
    void shouldThrowWhenBookNotFound() {
        BorrowRequest request = new BorrowRequest(1L, 1L);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(new User()));
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                borrowService.borrowBook(request)
        );
    }

    // ❌ BOOK ALREADY BORROWED
    @Test
    void shouldThrowWhenBookAlreadyBorrowed() {
        BorrowRequest request = new BorrowRequest(1L, 1L);

        User user = User.builder().id(1L).build();
        Book book = Book.builder().id(1L).build(); // ✅ FIX

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(borrowRepository.existsByBookIdAndReturnDateIsNull(1L)).thenReturn(true);

        assertThrows(ConflictException.class, () ->
                borrowService.borrowBook(request)
        );
    }

    // ✅ RETURN BOOK SUCCESS
    @Test
    void shouldReturnBookSuccessfully() {
        Book book = Book.builder().id(1L).available(false).build();

        Borrow borrow = Borrow.builder()
                .id(1L)
                .book(book)
                .user(new User())
                .borrowDate(LocalDateTime.now())
                .build();

        when(borrowRepository.findById(1L)).thenReturn(Optional.of(borrow));
        when(borrowRepository.save(any())).thenReturn(borrow);

        var response = borrowService.returnBook(1L);

        assertNotNull(response.returnDate());
        verify(bookRepository).save(book);
    }

    // ❌ BORROW NOT FOUND
    @Test
    void shouldThrowWhenBorrowNotFound() {
        when(borrowRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                borrowService.returnBook(1L)
        );
    }

    // ❌ ALREADY RETURNED
    @Test
    void shouldThrowWhenAlreadyReturned() {
        Borrow borrow = Borrow.builder()
                .id(1L)
                .returnDate(LocalDateTime.now())
                .build();

        when(borrowRepository.findById(1L)).thenReturn(Optional.of(borrow));

        assertThrows(OperationNotAllowedException.class, () ->
                borrowService.returnBook(1L)
        );
    }

    // ✅ GET ACTIVE BORROWS
    @Test
    void shouldReturnActiveBorrows() {
        Borrow borrow = Borrow.builder()
                .id(1L)
                .user(User.builder().id(1L).name("User").build())
                .book(Book.builder().id(1L).title("Book").build())
                .borrowDate(LocalDateTime.now())
                .build();

        when(borrowRepository.findActiveUserBorrowsWithBooksAndUser(1L))
                .thenReturn(List.of(borrow));

        var result = borrowService.getUserActiveBorrows(1L);

        assertEquals(1, result.size());
    }

    // ✅ GET BORROW HISTORY
    @Test
    void shouldReturnBorrowHistory() {
        Borrow borrow = Borrow.builder()
                .id(1L)
                .user(User.builder().id(1L).name("User").build())
                .book(Book.builder().id(1L).title("Book").build())
                .borrowDate(LocalDateTime.now())
                .build();

        when(borrowRepository.findBorrowHistoryWithDetails(1L))
                .thenReturn(List.of(borrow));

        var result = borrowService.getUserBorrowHistory(1L);

        assertEquals(1, result.size());
    }

    // ✅ MOST BORROWED SUCCESS
    @Test
    void shouldReturnMostBorrowedBooks() {
        when(borrowRepository.findMostBorrowedBooksSince(any()))
                .thenReturn(List.of(mockMostBorrowed()));

        var result = borrowService.getMostBorrowedBooksSince(LocalDateTime.now());

        assertEquals(1, result.size());
    }

    // ❌ NULL DATE
    @Test
    void shouldThrowWhenDateIsNull() {
        assertThrows(BadRequestException.class, () ->
                borrowService.getMostBorrowedBooksSince(null)
        );
    }

    // 🔹 helper
    private MostBorrowedBook mockMostBorrowed() {
        return new MostBorrowedBook(1L,10L);
    }
}
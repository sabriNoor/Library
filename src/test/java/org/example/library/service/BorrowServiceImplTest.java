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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
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

    private User user;
    private Book book;
    private Borrow borrow;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("User")
                .build();

        book = Book.builder()
                .id(1L)
                .title("Book")
                .available(true)
                .build();

        borrow = Borrow.builder()
                .id(1L)
                .user(user)
                .book(book)
                .borrowDate(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should borrow book successfully")
    void shouldBorrowBookSuccessfully() {
        BorrowRequest request = new BorrowRequest(1L, 1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(borrowRepository.existsByBookIdAndReturnDateIsNull(1L)).thenReturn(false);
        when(borrowRepository.save(any())).thenReturn(borrow);

        var response = borrowService.borrowBook(request);

        assertThat(response.bookId()).isEqualTo(1L);

        verify(bookRepository).save(book);
        verify(borrowRepository).save(any(Borrow.class));
    }

    @Test
    @DisplayName("Should throw when user not found")
    void shouldThrowWhenUserNotFound() {
        BorrowRequest request = new BorrowRequest(1L, 1L);

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> borrowService.borrowBook(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should throw when book not found")
    void shouldThrowWhenBookNotFound() {
        BorrowRequest request = new BorrowRequest(1L, 1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> borrowService.borrowBook(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should throw when book already borrowed")
    void shouldThrowWhenBookAlreadyBorrowed() {
        BorrowRequest request = new BorrowRequest(1L, 1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(borrowRepository.existsByBookIdAndReturnDateIsNull(1L)).thenReturn(true);

        assertThatThrownBy(() -> borrowService.borrowBook(request))
                .isInstanceOf(ConflictException.class);

        verify(borrowRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return book successfully")
    void shouldReturnBookSuccessfully() {
        book.setAvailable(false);

        when(borrowRepository.findById(1L)).thenReturn(Optional.of(borrow));
        when(borrowRepository.save(any())).thenReturn(borrow);

        var response = borrowService.returnBook(1L);

        assertThat(response.returnDate()).isNotNull();

        verify(bookRepository).save(book);
        verify(borrowRepository).save(borrow);
    }

    @Test
    @DisplayName("Should throw when borrow not found")
    void shouldThrowWhenBorrowNotFound() {
        when(borrowRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> borrowService.returnBook(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should throw when book already returned")
    void shouldThrowWhenAlreadyReturned() {
        borrow.setReturnDate(LocalDateTime.now());

        when(borrowRepository.findById(1L)).thenReturn(Optional.of(borrow));

        assertThatThrownBy(() -> borrowService.returnBook(1L))
                .isInstanceOf(OperationNotAllowedException.class);
    }

    @Test
    @DisplayName("Should return active borrows")
    void shouldReturnActiveBorrows() {
        when(borrowRepository.findActiveUserBorrowsWithBooksAndUser(1L))
                .thenReturn(List.of(borrow));

        var result = borrowService.getUserActiveBorrows(1L);

        assertThat(result).hasSize(1);

        verify(borrowRepository).findActiveUserBorrowsWithBooksAndUser(1L);
    }

    @Test
    @DisplayName("Should return borrow history")
    void shouldReturnBorrowHistory() {
        when(borrowRepository.findBorrowHistoryWithDetails(1L))
                .thenReturn(List.of(borrow));

        var result = borrowService.getUserBorrowHistory(1L);

        assertThat(result).hasSize(1);

        verify(borrowRepository).findBorrowHistoryWithDetails(1L);
    }

    @Test
    @DisplayName("Should return most borrowed books")
    void shouldReturnMostBorrowedBooks() {
        when(borrowRepository.findMostBorrowedBooksSince(any()))
                .thenReturn(List.of(new MostBorrowedBook(1L, 10L)));

        var result = borrowService.getMostBorrowedBooksSince(LocalDateTime.now());

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Should throw when date is null")
    void shouldThrowWhenDateIsNull() {
        assertThatThrownBy(() -> borrowService.getMostBorrowedBooksSince(null))
                .isInstanceOf(BadRequestException.class);
    }
}
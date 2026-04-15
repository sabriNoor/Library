package org.example.library.service;

import org.example.library.dto.*;
import org.example.library.entity.Author;
import org.example.library.entity.Book;
import org.example.library.exception.*;
import org.example.library.repository.AuthorRepository;
import org.example.library.repository.BookRepository;
import org.example.library.repository.BorrowRepository;
import org.example.library.service.impl.BookServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.OptimisticLockException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private BorrowRepository borrowRepository;

    @InjectMocks
    private BookServiceImpl bookService;

    private Author author;
    private Book book;

    @BeforeEach
    void setUp() {
        author = Author.builder()
                .id(1L)
                .name("Author")
                .build();

        book = Book.builder()
                .id(1L)
                .title("Title")
                .isbn("isbn")
                .genre("Fiction")
                .author(author)
                .available(true)
                .build();
    }

    @Test
    @DisplayName("Should create book successfully")
    void shouldCreateBookSuccessfully() {
        CreateBookRequest request = new CreateBookRequest(
                "Title", "isbn", "Fiction", new String[]{"tag"}, 1L
        );

        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(bookRepository.existsByIsbn("isbn")).thenReturn(false);
        when(bookRepository.save(any())).thenReturn(book);

        var response = bookService.createBook(request);

        assertThat(response.title()).isEqualTo("Title");

        verify(bookRepository).save(any(Book.class));
    }

    @Test
    @DisplayName("Should throw when author not found")
    void shouldThrowWhenAuthorNotFound() {
        CreateBookRequest request = new CreateBookRequest(
                "Title", "isbn", "Fiction", null, 1L
        );

        when(authorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.createBook(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should throw when ISBN already exists")
    void shouldThrowWhenIsbnExists() {
        CreateBookRequest request = new CreateBookRequest(
                "Title", "isbn", "Fiction", null, 1L
        );

        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(bookRepository.existsByIsbn("isbn")).thenReturn(true);

        assertThatThrownBy(() -> bookService.createBook(request))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("Should return book by id")
    void shouldReturnBook() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        var response = bookService.getBookById(1L);

        assertThat(response.title()).isEqualTo("Title");

        verify(bookRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw when book not found")
    void shouldThrowWhenBookNotFound() {
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getBookById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should update book successfully")
    void shouldUpdateBook() {
        UpdateBookRequest request = new UpdateBookRequest(
                "New", "new-isbn", "NewGenre", new String[]{"tag"}
        );

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.existsByIsbn("new-isbn")).thenReturn(false);

        var response = bookService.updateBook(1L, request);

        assertThat(response.title()).isEqualTo("New");
        assertThat(response.isbn()).isEqualTo("new-isbn");

        verify(bookRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw when updating with existing ISBN")
    void shouldThrowWhenUpdatingWithExistingIsbn() {
        UpdateBookRequest request = new UpdateBookRequest(
                null, "taken-isbn", null, null
        );

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.existsByIsbn("taken-isbn")).thenReturn(true);

        assertThatThrownBy(() -> bookService.updateBook(1L, request))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("Should delete book successfully")
    void shouldDeleteBook() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(borrowRepository.existsByBookId(1L)).thenReturn(false);

        bookService.deleteBook(1L);

        verify(bookRepository).delete(book);
    }

    @Test
    @DisplayName("Should throw when deleting book with active borrows")
    void shouldThrowWhenBookHasBorrows() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(borrowRepository.existsByBookId(1L)).thenReturn(true);

        assertThatThrownBy(() -> bookService.deleteBook(1L))
                .isInstanceOf(OperationNotAllowedException.class);

        verify(bookRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should mark books as available successfully")
    void shouldMarkBooksAsAvailable() {
        BookIdsRequest request = new BookIdsRequest(List.of(1L, 2L));

        when(borrowRepository.markBorrowsAsReturned(request.bookIds())).thenReturn(2);
        when(bookRepository.markBooksAsAvailable(request.bookIds())).thenReturn(2);

        bookService.markBooksAsAvailable(request);

        verify(bookRepository).markBooksAsAvailable(request.bookIds());
        verify(borrowRepository).markBorrowsAsReturned(request.bookIds());
    }

    @Test
    @DisplayName("Should throw when bookIds list is empty")
    void shouldThrowWhenEmptyList() {
        BookIdsRequest request = new BookIdsRequest(List.of());

        assertThatThrownBy(() -> bookService.markBooksAsAvailable(request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("Should throw when not all books are updated")
    void shouldThrowWhenNotAllBooksUpdated() {
        BookIdsRequest request = new BookIdsRequest(List.of(1L, 2L));

        when(borrowRepository.markBorrowsAsReturned(request.bookIds())).thenReturn(2);
        when(bookRepository.markBooksAsAvailable(request.bookIds())).thenReturn(1);

        assertThatThrownBy(() -> bookService.markBooksAsAvailable(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
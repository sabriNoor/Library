package org.example.library.service;

import org.example.library.dto.*;
import org.example.library.entity.Author;
import org.example.library.entity.Book;
import org.example.library.exception.*;
import org.example.library.repository.AuthorRepository;
import org.example.library.repository.BookRepository;
import org.example.library.repository.BorrowRepository;
import org.example.library.service.impl.BookServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.OptimisticLockException;

import java.util.List;
import java.util.Optional;

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

    // ✅ CREATE BOOK SUCCESS
    @Test
    void shouldCreateBookSuccessfully() {
        CreateBookRequest request = new CreateBookRequest(
                "Title", "isbn123", "Fiction",
                new String[]{"tag"}, 1L
        );

        Author author = Author.builder().id(1L).name("Author").build();

        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(bookRepository.existsByIsbn("isbn123")).thenReturn(false);

        Book saved = Book.builder()
                .id(1L)
                .title("Title")
                .isbn("isbn123")
                .genre("Fiction")
                .tags(new String[]{"tag"})
                .author(author)
                .available(true)
                .build();

        when(bookRepository.save(any())).thenReturn(saved);

        var response = bookService.createBook(request);

        assertEquals("Title", response.title());
    }

    // ❌ CREATE BOOK - AUTHOR NOT FOUND
    @Test
    void shouldThrowWhenAuthorNotFound() {
        CreateBookRequest request = new CreateBookRequest(
                "Title", "isbn", "Fiction",
                null, 1L
        );

        when(authorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                bookService.createBook(request)
        );
    }

    // ❌ CREATE BOOK - ISBN EXISTS
    @Test
    void shouldThrowWhenIsbnExists() {
        CreateBookRequest request = new CreateBookRequest(
                "Title", "isbn", "Fiction",
                null, 1L
        );

        when(authorRepository.findById(1L))
                .thenReturn(Optional.of(new Author()));
        when(bookRepository.existsByIsbn("isbn")).thenReturn(true);

        assertThrows(ConflictException.class, () ->
                bookService.createBook(request)
        );
    }

    // ✅ GET BOOK BY ID
    @Test
    void shouldReturnBook() {
        Author author = Author.builder().name("Author").build();

        Book book = Book.builder()
                .id(1L)
                .title("Title")
                .isbn("isbn")
                .genre("Fiction")
                .author(author)
                .available(true)
                .build();

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        var response = bookService.getBookById(1L);

        assertEquals("Title", response.title());
    }

    // ❌ GET BOOK NOT FOUND
    @Test
    void shouldThrowWhenBookNotFound() {
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                bookService.getBookById(1L)
        );
    }

    // ✅ UPDATE BOOK SUCCESS
    @Test
    void shouldUpdateBook() {
        Author author = Author.builder().name("Author").build();

        Book book = Book.builder()
                .id(1L)
                .title("Old")
                .isbn("old")
                .genre("Old")
                .author(author)
                .build();

        UpdateBookRequest request = new UpdateBookRequest(
                "New", "new", "NewGenre", new String[]{"tag"}
        );

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        var response = bookService.updateBook(1L, request);

        assertEquals("New", response.title());
        assertEquals("new", response.isbn());
    }

    // ❌ UPDATE BOOK - OPTIMISTIC LOCK
    @Test
    void shouldThrowConcurrencyException() {
        when(bookRepository.findById(1L))
                .thenThrow(new OptimisticLockException());

        UpdateBookRequest request = new UpdateBookRequest(null, null, null, null);

        assertThrows(ConcurrencyException.class, () ->
                bookService.updateBook(1L, request)
        );
    }

    // ✅ DELETE BOOK SUCCESS
    @Test
    void shouldDeleteBook() {
        Book book = Book.builder().id(1L).build();

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(borrowRepository.existsByBookId(1L)).thenReturn(false);

        bookService.deleteBook(1L);

        verify(bookRepository).delete(book);
    }

    // ❌ DELETE BOOK WITH BORROWS
    @Test
    void shouldThrowWhenBookHasBorrows() {
        Book book = Book.builder().id(1L).build();

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(borrowRepository.existsByBookId(1L)).thenReturn(true);

        assertThrows(OperationNotAllowedException.class, () ->
                bookService.deleteBook(1L)
        );
    }

    // ✅ MARK BOOKS AVAILABLE SUCCESS
    @Test
    void shouldMarkBooksAsAvailable() {
        BookIdsRequest request = new BookIdsRequest(List.of(1L, 2L));

        when(borrowRepository.markBorrowsAsReturned(request.bookIds())).thenReturn(2);
        when(bookRepository.markBooksAsAvailable(request.bookIds())).thenReturn(2);

        bookService.markBooksAsAvailable(request);

        verify(bookRepository).markBooksAsAvailable(request.bookIds());
    }

    // ❌ EMPTY LIST
    @Test
    void shouldThrowWhenEmptyList() {
        BookIdsRequest request = new BookIdsRequest(List.of());

        assertThrows(BadRequestException.class, () ->
                bookService.markBooksAsAvailable(request)
        );
    }

    // ❌ NOT ALL BOOKS FOUND
    @Test
    void shouldThrowWhenNotAllBooksUpdated() {
        BookIdsRequest request = new BookIdsRequest(List.of(1L, 2L));

        when(borrowRepository.markBorrowsAsReturned(request.bookIds())).thenReturn(2);
        when(bookRepository.markBooksAsAvailable(request.bookIds())).thenReturn(1);

        assertThrows(ResourceNotFoundException.class, () ->
                bookService.markBooksAsAvailable(request)
        );
    }
}
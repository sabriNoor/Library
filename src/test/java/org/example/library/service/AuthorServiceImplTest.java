package org.example.library.service;

import org.example.library.dto.BookResponse;
import org.example.library.dto.CreateAuthorRequest;
import org.example.library.dto.UpdateAuthorRequest;
import org.example.library.entity.Author;
import org.example.library.exception.BadRequestException;
import org.example.library.exception.ConflictException;
import org.example.library.exception.ResourceNotFoundException;
import org.example.library.repository.AuthorRepository;
import org.example.library.repository.BookRepository;
import org.example.library.service.impl.AuthorServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorServiceImplTest {

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private AuthorServiceImpl authorService;

    // ✅ CREATE AUTHOR SUCCESS
    @Test
    void shouldCreateAuthorSuccessfully() {
        CreateAuthorRequest request = new CreateAuthorRequest("Noor", "noor@mail.com");

        when(authorRepository.findAuthorByEmail(request.email()))
                .thenReturn(Optional.empty());

        Author saved = Author.builder()
                .id(1L)
                .name("Noor")
                .email("noor@mail.com")
                .build();

        when(authorRepository.save(any(Author.class))).thenReturn(saved);

        var response = authorService.createAuthor(request);

        assertEquals("Noor", response.name());
        assertEquals("noor@mail.com", response.email());
    }

    // ❌ CREATE AUTHOR - EMAIL EXISTS
    @Test
    void shouldThrowConflictWhenEmailExists() {
        CreateAuthorRequest request = new CreateAuthorRequest("Noor", "noor@mail.com");

        when(authorRepository.findAuthorByEmail(request.email()))
                .thenReturn(Optional.of(new Author()));

        assertThrows(ConflictException.class, () ->
                authorService.createAuthor(request)
        );
    }

    // ✅ GET AUTHOR BY ID
    @Test
    void shouldReturnAuthorWhenFound() {
        Author author = Author.builder().id(1L).name("A").email("a@mail.com").build();

        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));

        var response = authorService.getAuthorById(1L);

        assertEquals("A", response.name());
    }

    // ❌ GET AUTHOR NOT FOUND
    @Test
    void shouldThrowWhenAuthorNotFound() {
        when(authorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                authorService.getAuthorById(1L)
        );
    }

    // ✅ GET ALL AUTHORS
    @Test
    void shouldReturnAllAuthors() {
        when(authorRepository.findAll()).thenReturn(List.of(
                Author.builder().id(1L).name("A").email("a@mail.com").build(),
                Author.builder().id(2L).name("B").email("b@mail.com").build()
        ));

        var result = authorService.getAllAuthors();

        assertEquals(2, result.size());
    }

    // ✅ SEARCH BY NAME
    @Test
    void shouldSearchAuthorsByName() {
        when(authorRepository.findByNameContainingIgnoreCase("noor"))
                .thenReturn(List.of(
                        Author.builder().id(1L).name("Noor").email("n@mail.com").build()
                ));

        var result = authorService.searchAuthorsByName("noor");

        assertEquals(1, result.size());
    }

    // ✅ FIND BY EMAIL DOMAIN
    @Test
    void shouldFindAuthorsByDomain() {
        when(authorRepository.findAuthorByEmailDomain("mail.com"))
                .thenReturn(List.of(
                        Author.builder().id(1L).name("A").email("a@mail.com").build()
                ));

        var result = authorService.findAuthorsByEmailDomain("mail.com");

        assertEquals(1, result.size());
    }

    // ✅ UPDATE AUTHOR SUCCESS
    @Test
    void shouldUpdateAuthorSuccessfully() {
        Author existing = Author.builder()
                .id(1L)
                .name("Old")
                .email("old@mail.com")
                .build();

        UpdateAuthorRequest request = new UpdateAuthorRequest("New", "new@mail.com");

        when(authorRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(authorRepository.existsByEmail("new@mail.com")).thenReturn(false);
        when(authorRepository.save(any())).thenReturn(existing);

        var response = authorService.updateAuthor(1L, request);

        assertEquals("New", response.name());
        assertEquals("new@mail.com", response.email());
    }

    // ❌ UPDATE AUTHOR - EMAIL CONFLICT
    @Test
    void shouldThrowWhenUpdatingWithExistingEmail() {
        Author existing = Author.builder()
                .id(1L)
                .name("Old")
                .email("old@mail.com")
                .build();

        UpdateAuthorRequest request = new UpdateAuthorRequest("New", "taken@mail.com");

        when(authorRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(authorRepository.existsByEmail("taken@mail.com")).thenReturn(true);

        assertThrows(ConflictException.class, () ->
                authorService.updateAuthor(1L, request)
        );
    }

    // ✅ DELETE AUTHOR SUCCESS
    @Test
    void shouldDeleteAuthorSuccessfully() {
        Author author = Author.builder().id(1L).build();

        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(bookRepository.findBooksByAuthor(1L)).thenReturn(List.of());

        authorService.deleteAuthor(1L);

        verify(authorRepository).delete(author);
    }

    // ❌ DELETE AUTHOR WITH BOOKS
    @Test
    void shouldThrowWhenAuthorHasBooks() {
        Author author = Author.builder().id(1L).build();

        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(bookRepository.findBooksByAuthor(1L))
                .thenReturn(List.of(
                        new BookResponse(
                                1L,
                                "123456",
                                "Test Book",
                                "Author Name",
                                "Fiction",
                                new String[]{"tag1", "tag2"},
                                true,
                                1L
                        )
                ));

        assertThrows(BadRequestException.class, () ->
                authorService.deleteAuthor(1L)
        );
    }
}
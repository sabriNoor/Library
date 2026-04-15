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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
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

    private Author author;

    @BeforeEach
    void setUp() {
        author = Author.builder()
                .id(1L)
                .name("Noor")
                .email("noor@mail.com")
                .build();
    }

    @Test
    @DisplayName("Should create author successfully")
    void shouldCreateAuthorSuccessfully() {
        CreateAuthorRequest request = new CreateAuthorRequest("Noor", "noor@mail.com");

        when(authorRepository.findAuthorByEmail(request.email()))
                .thenReturn(Optional.empty());
        when(authorRepository.save(any())).thenReturn(author);

        var response = authorService.createAuthor(request);

        assertThat(response.name()).isEqualTo("Noor");
        assertThat(response.email()).isEqualTo("noor@mail.com");

        verify(authorRepository).save(any(Author.class));
    }

    @Test
    @DisplayName("Should throw conflict when email already exists")
    void shouldThrowConflictWhenEmailExists() {
        CreateAuthorRequest request = new CreateAuthorRequest("Noor", "noor@mail.com");

        when(authorRepository.findAuthorByEmail(request.email()))
                .thenReturn(Optional.of(author));

        assertThatThrownBy(() -> authorService.createAuthor(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email already exists");

        verify(authorRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return author by id")
    void shouldReturnAuthorWhenFound() {
        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));

        var response = authorService.getAuthorById(1L);

        assertThat(response.name()).isEqualTo("Noor");

        verify(authorRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw when author not found")
    void shouldThrowWhenAuthorNotFound() {
        when(authorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authorService.getAuthorById(1L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(authorRepository).findById(1L);
    }

    @Test
    @DisplayName("Should return all authors")
    void shouldReturnAllAuthors() {
        when(authorRepository.findAll()).thenReturn(List.of(author));

        var result = authorService.getAllAuthors();

        assertThat(result).hasSize(1);

        verify(authorRepository).findAll();
    }

    @Test
    @DisplayName("Should search authors by name")
    void shouldSearchAuthorsByName() {
        when(authorRepository.findByNameContainingIgnoreCase("noor"))
                .thenReturn(List.of(author));

        var result = authorService.searchAuthorsByName("noor");

        assertThat(result).hasSize(1);

        verify(authorRepository).findByNameContainingIgnoreCase("noor");
    }

    @Test
    @DisplayName("Should find authors by email domain")
    void shouldFindAuthorsByDomain() {
        when(authorRepository.findAuthorByEmailDomain("mail.com"))
                .thenReturn(List.of(author));

        var result = authorService.findAuthorsByEmailDomain("mail.com");

        assertThat(result).hasSize(1);

        verify(authorRepository).findAuthorByEmailDomain("mail.com");
    }

    @Test
    @DisplayName("Should update author successfully")
    void shouldUpdateAuthorSuccessfully() {
        UpdateAuthorRequest request = new UpdateAuthorRequest("Updated", "updated@mail.com");

        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(authorRepository.existsByEmail("updated@mail.com")).thenReturn(false);
        when(authorRepository.save(any())).thenReturn(author);

        var response = authorService.updateAuthor(1L, request);

        assertThat(response.name()).isEqualTo("Updated");
        assertThat(response.email()).isEqualTo("updated@mail.com");

        verify(authorRepository).save(author);
    }

    @Test
    @DisplayName("Should throw conflict when updating with existing email")
    void shouldThrowWhenUpdatingWithExistingEmail() {
        UpdateAuthorRequest request = new UpdateAuthorRequest("Updated", "taken@mail.com");

        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(authorRepository.existsByEmail("taken@mail.com")).thenReturn(true);

        assertThatThrownBy(() -> authorService.updateAuthor(1L, request))
                .isInstanceOf(ConflictException.class);

        verify(authorRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete author successfully when no books exist")
    void shouldDeleteAuthorSuccessfully() {
        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(bookRepository.existsByAuthorId(1L)).thenReturn(false);

        authorService.deleteAuthor(1L);

        verify(authorRepository).delete(author);
    }

    @Test
    @DisplayName("Should throw when deleting author with existing books")
    void shouldThrowWhenAuthorHasBooks() {
        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(bookRepository.existsByAuthorId(1L)).thenReturn(true);

        assertThatThrownBy(() -> authorService.deleteAuthor(1L))
                .isInstanceOf(BadRequestException.class);

        verify(authorRepository, never()).delete(any());
    }
}
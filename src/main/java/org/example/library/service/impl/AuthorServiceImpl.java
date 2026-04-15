package org.example.library.service.impl;

import org.example.library.dto.AuthorResponse;
import org.example.library.dto.CreateAuthorRequest;
import org.example.library.dto.UpdateAuthorRequest;
import org.example.library.entity.Author;
import org.example.library.exception.BadRequestException;
import org.example.library.exception.ConflictException;
import org.example.library.exception.ResourceNotFoundException;
import org.example.library.repository.AuthorRepository;
import org.example.library.repository.BookRepository;
import org.example.library.service.AuthorService;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;

    public AuthorServiceImpl(AuthorRepository authorRepository,
                             BookRepository bookRepository) {
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
    }

    @Override
    public AuthorResponse createAuthor(CreateAuthorRequest author) {

        authorRepository.findAuthorByEmail(author.email())
                .ifPresent(a -> {
                    throw new ConflictException("Email already exists");
                });

        Author newAuthor = Author.builder()
                .email(author.email())
                .name(author.name())
                .build();

        return mapToResponse(authorRepository.save(newAuthor));
    }

    @Override
    public AuthorResponse getAuthorById(Long id) {
        Author author = findAuthorById(id);
        return mapToResponse(author);
    }

    @Override
    public List<AuthorResponse> getAllAuthors() {
        return authorRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<AuthorResponse> searchAuthorsByName(String name) {
        return authorRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<AuthorResponse> findAuthorsByEmailDomain(String domain) {
        return authorRepository.findAuthorByEmailDomain(domain)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public AuthorResponse updateAuthor(Long id, UpdateAuthorRequest author) {

        Author existing = findAuthorById(id);

        if (author.email() != null &&
                authorRepository.existsByEmail(author.email()) &&
                !existing.getEmail().equals(author.email())) {

            throw new ConflictException("Email already in use");
        }

        // ✅ Update fields safely
        if (author.name() != null) {
            existing.setName(author.name());
        }

        if (author.email() != null) {
            existing.setEmail(author.email());
        }

        return mapToResponse(authorRepository.save(existing));
    }

    @Override
    public void deleteAuthor(Long id) {

        Author author = findAuthorById(id);

        if (!bookRepository.findBooksByAuthor(author.getId()).isEmpty()) {
            throw new BadRequestException("Cannot delete author with existing books");
        }

        authorRepository.delete(author);
    }

    // ✅ Helper method
    private Author findAuthorById(Long id) {
        return authorRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Author not found with id: " + id));
    }

    // ✅ Mapper
    private AuthorResponse mapToResponse(Author author) {
        return new AuthorResponse(
                author.getId(),
                author.getEmail(),
                author.getName()
        );
    }
}
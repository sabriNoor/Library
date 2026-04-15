package org.example.library.controller;

import jakarta.validation.Valid;
import org.example.library.dto.AuthorResponse;
import org.example.library.dto.CreateAuthorRequest;
import org.example.library.dto.UpdateAuthorRequest;
import org.example.library.service.AuthorService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/authors")
public class AuthorController {

    private final AuthorService authorService;

    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    // ✅ CREATE AUTHOR
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AuthorResponse createAuthor(@Valid @RequestBody CreateAuthorRequest request) {
        return authorService.createAuthor(request);
    }

    // ✅ GET BY ID
    @GetMapping("/{id}")
    public AuthorResponse getAuthorById(@PathVariable Long id) {
        return authorService.getAuthorById(id);
    }

    // ✅ GET ALL
    @GetMapping
    public List<AuthorResponse> getAllAuthors() {
        return authorService.getAllAuthors();
    }

    // ✅ SEARCH BY NAME
    @GetMapping("/search")
    public List<AuthorResponse> searchAuthors(@RequestParam String name) {
        return authorService.searchAuthorsByName(name);
    }

    // ✅ FILTER BY EMAIL DOMAIN
    @GetMapping("/by-domain")
    public List<AuthorResponse> getByEmailDomain(@RequestParam String domain) {
        return authorService.findAuthorsByEmailDomain(domain);
    }

    // ✅ UPDATE
    @PutMapping("/{id}")
    public AuthorResponse updateAuthor(@PathVariable Long id,
                                       @RequestBody UpdateAuthorRequest request) {
        return authorService.updateAuthor(id, request);
    }

    // ✅ DELETE
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAuthor(@PathVariable Long id) {
        authorService.deleteAuthor(id);
    }
}
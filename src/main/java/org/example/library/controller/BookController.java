package org.example.library.controller;

import jakarta.validation.Valid;
import org.example.library.dto.*;
import org.example.library.service.BookService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    // ✅ CREATE BOOK
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookResponse createBook(@Valid @RequestBody CreateBookRequest request) {
        return bookService.createBook(request);
    }

    // ✅ GET BOOK BY ID
    @GetMapping("/{id}")
    public BookResponse getBookById(@PathVariable Long id) {
        return bookService.getBookById(id);
    }

    // ✅ GET AVAILABLE BOOKS
    @GetMapping("/available")
    public List<BookResponse> getAvailableBooks() {
        return bookService.getAvailableBooks();
    }

    // ✅ GET BOOKS BY AUTHOR
    @GetMapping("/author/{authorId}")
    public List<BookResponse> getBooksByAuthor(@PathVariable Long authorId) {
        return bookService.getBooksByAuthor(authorId);
    }

    // ✅ GET BASIC BOOKS BY AUTHOR (DTO projection)
    @GetMapping("/author/{authorId}/basic")
    public List<BookBasic> getBasicBooksByAuthor(@PathVariable Long authorId) {
        return bookService.getBasicBooksByAuthor(authorId);
    }

    // ✅ GET BOOKS BY TAG
    @GetMapping("/tag")
    public List<BookBasic> getBooksByTag(@RequestParam String tag) {
        return bookService.getBooksByTag(tag);
    }

    // ✅ GET ALL TAGS
    @GetMapping("/tags")
    public List<String> getAllTags() {
        return bookService.getAllTags();
    }

    // ✅ UPDATE BOOK
    @PutMapping("/{id}")
    public BookResponse updateBook(@PathVariable Long id,
                                   @Valid @RequestBody UpdateBookRequest request) {
        return bookService.updateBook(id, request);
    }

    // ✅ DELETE BOOK
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
    }

    @PutMapping("/available")
    public void markBooksAsAvailable(@Valid @RequestBody BookIdsRequest request) {
        bookService.markBooksAsAvailable(request);
    }
}
package org.example.library.service;

import org.example.library.dto.*;

import java.util.List;

public interface BookService {

    BookResponse createBook(CreateBookRequest book);

    BookResponse getBookById(Long id);

    List<BookResponse> getAvailableBooks();

    List<BookResponse> getBooksByAuthor(Long authorId);

    List<BookBasic> getBasicBooksByAuthor(Long authorId);

    List<BookBasic> getBooksByTag(String tag);

    List<String> getAllTags();

    BookResponse updateBook(Long id, UpdateBookRequest book);

    void deleteBook(Long id);

}

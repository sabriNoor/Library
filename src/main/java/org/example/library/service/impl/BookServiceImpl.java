package org.example.library.service.impl;

import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import org.example.library.dto.*;
import org.example.library.entity.Author;
import org.example.library.entity.Book;
import org.example.library.exception.*;
import org.example.library.repository.AuthorRepository;
import org.example.library.repository.BookRepository;
import org.example.library.repository.BorrowRepository;
import org.example.library.service.BookService;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final BorrowRepository borrowRepository;

    public BookServiceImpl(BookRepository bookRepository,
                           AuthorRepository authorRepository,
                           BorrowRepository borrowRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.borrowRepository = borrowRepository;
    }

    @Override
    @Transactional
    public BookResponse createBook(CreateBookRequest request) {

        Author author = authorRepository.findById(request.authorId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Author not found with id: " + request.authorId()));
        if(bookRepository.existsByIsbn(request.isbn())){
            throw new ConflictException("ISBN already in use");
        }

        Book book = Book.builder()
                .title(request.title())
                .isbn(request.isbn())
                .genre(request.genre())
                .tags(request.tags())
                .author(author)
                .build();

        return mapToResponse(bookRepository.save(book));
    }

    @Override
    public BookResponse getBookById(Long id) {
        return bookRepository.findBookById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
    }

    @Override
    public List<BookResponse> getAvailableBooks() {
        return bookRepository.findAvailableBooks();
    }

    @Override
    public List<BookResponse> getBooksByAuthor(Long authorId) {
        return bookRepository.findBooksByAuthor(authorId);
    }

    @Override
    public List<BookBasic> getBasicBooksByAuthor(Long authorId) {
        return bookRepository.findBasicBooksByAuthor(authorId);
    }

    @Override
    public List<BookBasic> getBooksByTag(String tag) {
        return bookRepository.findByTag(tag);
    }

    @Override
    public List<String> getAllTags() {
        return bookRepository.findAllTags();
    }

    @Override
    @Transactional
    public BookResponse updateBook(Long id, UpdateBookRequest request) {

        Book existing = fetchBookById(id);

        // ✅ Partial update
        if (request.title() != null) {
            existing.setTitle(request.title());
        }

        if (request.isbn() != null &&
                !request.isbn().equals(existing.getIsbn()) &&
                bookRepository.existsByIsbn(request.isbn())) {

            throw new ConflictException("ISBN already in use");
        }

        if (request.isbn() != null) {
            existing.setIsbn(request.isbn());
        }

        if (request.genre() != null) {
            existing.setGenre(request.genre());
        }

        if (request.tags() != null) {
            existing.setTags(request.tags());
        }

        return mapToResponse(existing);

    }

    @Override
    @Transactional
    public void deleteBook(Long id) {

        Book book = fetchBookById(id);

        if (borrowRepository.existsByBookId(id)) {
            throw new OperationNotAllowedException(
                    "Cannot delete book with active borrows");
        }

        bookRepository.delete(book);
    }

    @Override
    @Transactional
    public void markBooksAsAvailable(BookIdsRequest request) {

        List<Long> bookIds=request.bookIds();
        if (bookIds == null || bookIds.isEmpty()) {
            throw new BadRequestException("Book IDs list cannot be empty");
        }

        int returnedCount = borrowRepository.markBorrowsAsReturned(bookIds);

        if (returnedCount != bookIds.size()) {
            throw new ResourceNotFoundException("One or more books not found");
        }
    }

    // ✅ Helper
    private Book fetchBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Book not found with id: " + id));
    }

    // ✅ Mapper
    private BookResponse mapToResponse(Book book) {
        boolean available = borrowRepository
                .findByBookIdAndReturnDateIsNull(book.getId())
                .isEmpty();

        return new BookResponse(
                book.getId(),
                book.getIsbn(),
                book.getTitle(),
                book.getAuthor().getName(),
                book.getGenre(),
                book.getTags(),
                available,
                book.getAuthor().getId()
        );
    }
}
package org.example.library.repository;

import org.example.library.dto.BookBasic;
import org.example.library.dto.BookResponse;
import org.example.library.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    // ✅ Get all available books (DTO projection)
    @Query("""
        SELECT b.id AS id,
               b.isbn AS isbn,
               b.title AS title,
               b.author.name AS authorName,
               b.genre AS genre,
               b.tags AS tags,
               b.available AS available,
               b.author.id AS authorId
        FROM Book b
        JOIN b.author a
        WHERE b.available = true
    """)
    List<BookResponse> findAvailableBooks();

    boolean existsByIsbn(String isbn);

    // ✅ Get books by author (DTO projection)
    @Query("""
        SELECT b.id AS id,
               b.isbn AS isbn,
               b.title AS title,
               b.author.name AS authorName,
               b.genre AS genre,
               b.tags AS tags,
               b.available AS available,
               b.author.id AS authorId
        FROM Book b
        JOIN b.author a
        WHERE a.id = :authorId
    """)
    List<BookResponse> findBooksByAuthor(@Param("authorId") Long authorId);

    // ✅ Get single book by ID (DTO projection)
    @Query("""
        SELECT b.id AS id,
               b.isbn AS isbn,
               b.title AS title,
               b.author.name AS authorName,
               b.genre AS genre,
               b.tags AS tags,
               b.available AS available,
               b.author.id AS authorId
        FROM Book b
        JOIN b.author a
        WHERE b.id = :id
    """)
    Optional<BookResponse> findBookById(@Param("id") Long id);

    // ✅ Lightweight DTO (no author join needed)
    @Query("""
        SELECT b.id AS id,
               b.title AS title,
               b.isbn AS isbn,
               b.available AS available
        FROM Book b
        WHERE b.author.id = :authorId
    """)
    List<BookBasic> findBasicBooksByAuthor(@Param("authorId") Long authorId);

    // ✅ Native query for tags
    @Query(value = """
        SELECT b.id AS id,
               b.title AS title,
               b.isbn AS isbn,
               b.available AS available
        FROM books b
        WHERE :tag = ANY(b.tags)
    """, nativeQuery = true)
    List<BookBasic> findByTag(@Param("tag") String tag);

    // ✅ Get all unique tags
    @Query(value = "SELECT DISTINCT unnest(tags) FROM books", nativeQuery = true)
    List<String> findAllTags();

    // ✅ Bulk update availability
    @Modifying
    @Query("UPDATE Book b SET b.available = true WHERE b.id IN :ids ")
    int markBooksAsAvailable(@Param("ids") List<Long> ids);
}
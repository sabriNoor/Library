package org.example.library.repository;

import org.example.library.dto.MostBorrowedBook;
import org.example.library.entity.Borrow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BorrowRepository extends JpaRepository<Borrow,Long> {

    // ✅ Check if a book is currently borrowed (ACTIVE borrow)
    Optional<Borrow> findByBookIdAndReturnDateIsNull(Long bookId);

    // ✅ Faster check (used in validation)
    boolean existsByBookIdAndReturnDateIsNull(Long bookId);
    boolean existsByBookId(Long bookId);
    boolean existsByUserId(Long userId);


    // ✅ Get user's full borrow history
    List<Borrow> findByUserId(Long userId);

    // ✅ JPQL with JOIN FETCH (avoid N+1 problem)
    @Query("SELECT b FROM Borrow b JOIN FETCH b.book WHERE b.user.id = :userId")
    List<Borrow> findUserBorrowsWithBooks(@Param("userId") Long userId);


    @Query("SELECT b FROM Borrow b JOIN FETCH b.book JOIN FETCH b.user WHERE b.user.id = :userId AND b.returnDate IS NULL ")
    List<Borrow> findActiveUserBorrowsWithBooksAndUser(@Param("userId") Long userId);

    @Query("SELECT b FROM Borrow b JOIN FETCH b.book JOIN FETCH b.user WHERE b.user.id = :userId")
    List<Borrow> findBorrowHistoryWithDetails(@Param("userId") Long userId);

    // ✅ Native query: most borrowed books since a date
    @Query(value = """
        SELECT book_id, COUNT(*) as borrow_count
        FROM borrows
        WHERE borrow_date >= :date
        GROUP BY book_id
        ORDER BY borrow_count DESC
        """, nativeQuery = true)
    List<MostBorrowedBook> findMostBorrowedBooksSince(@Param("date") LocalDateTime date);

    @Modifying
    @Query("""
        UPDATE Borrow b
        SET b.returnDate = CURRENT_TIMESTAMP
        WHERE b.book.id IN :bookIds AND b.returnDate IS NULL
    """)
    int markBorrowsAsReturned(@Param("bookIds") List<Long> bookIds);
}

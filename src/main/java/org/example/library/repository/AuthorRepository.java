package org.example.library.repository;

import org.example.library.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthorRepository extends JpaRepository<Author,Long> {
    List<Author> findByNameContainingIgnoreCase(String name);

    Optional<Author> findAuthorByEmail(String email);

    boolean existsByEmail(String email);

    @Query(
            value = "SELECT * FROM authors a WHERE a.email LIKE CONCAT('%@', :domain)",
            nativeQuery = true
    )
    List<Author> findAuthorByEmailDomain(String domain);
}

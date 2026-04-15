# Code Review — Book Library Management System

## Overall Assessment

Good work overall. The project is well-structured, all the required features are implemented, and the code is clean and readable. Below are specific findings organized by area — what was done well and what needs improvement.

---

## 1. Entities

### What's Good
- Clean use of Lombok (`@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`)
- Correct use of `@ManyToOne(fetch = FetchType.LAZY)` on `Book.author` and `Borrow.book`/`Borrow.user` — lazy loading is the right default
- `@Version` on `Book` for optimistic locking — correctly implemented
- PostgreSQL array type handled properly with `@JdbcTypeCode(SqlTypes.ARRAY)` on `Book.tags`

### Issues

**1.1 — `Book.genre` is marked `nullable = false` in the entity but genre was described as optional in the task**
- In `Book.java` line 26: `@Column(nullable = false)` on `genre`
- In `CreateBookRequest.java` line 13: `@NotBlank` on `genre`
- The task says genre is optional. This means a book without a genre would fail both at the DTO validation level and at the database level

**1.2 — `Borrow` entity uses `@ManyToOne` to `User` but the task said borrowing is tracked by user ID**
- The task said "tracked by user ID" — meaning just store a `Long userId` field, not a full relationship to a `User` entity
- You created a whole `User` entity, `UserService`, `UserController`, `UserRepository`, and `CreateUserRequest` DTO — none of which were required by the task
- This is extra work that wasn't asked for. In a real project, the user would already exist in another module. The borrow record just needs to store the user's ID

**1.3 — No `@Table(name = ...)` consistency**
- `Author` uses `@Table(name = "authors")` (plural)
- `Book` uses `@Table(name = "books")` (plural)
- `Borrow` uses `@Table(name = "borrow")` (singular)
- Pick one convention and stick with it. In our project we use singular table names

---

## 2. DTOs

### What's Good
- Good use of Java records for all DTOs — clean and immutable
- Separate request/response DTOs (e.g., `CreateBookRequest` vs `BookResponse`) — this is the right pattern
- Separate `CreateAuthorRequest` and `UpdateAuthorRequest` — good separation
- `BookBasic` as a lightweight DTO — exactly what was asked
- `MostBorrowedBook` as a dedicated DTO for the aggregation query
- Validation annotations on create DTOs (`@NotBlank`, `@NotNull`)

### Issues

**2.1 — `CreateAuthorRequest` is missing `@Email` validation on the email field**
- `CreateUserRequest` has `@Email` on the email field, but `CreateAuthorRequest` does not
- Both should validate email format

**2.2 — `UpdateAuthorRequest` has no validation at all**
- If someone sends an empty string for `name`, it would be accepted and saved
- Consider adding `@Size(min = 1)` or handling blank strings in the service

**2.3 — `UpdateBookRequest` has no validation**
- Same issue — no validation on any field. An empty ISBN string could be saved

**2.4 — `BookIdsRequest` has no validation**
- No `@NotNull` or `@NotEmpty` on `bookIds`. The validation is done manually in the service, but it should be at the DTO level too

**2.5 — `BookResponse` is missing `authorId`**
- The task asked for `authorId` in the book response. Currently only `authorName` is returned. If the frontend needs to link to the author, it needs the ID

**2.6 — `BorrowResponse` field order is inconsistent with the constructor**
- The record has `(id, userId, userName, bookId, bookTitle, ...)` but in `BorrowServiceImpl.mapToResponse()` line 119-127, you pass `user.getId()` then `user.getName()` then `book.getId()` then `book.getTitle()` — this works but the field naming `userId` before `userName` is a bit confusing since the task asked for `(id, bookId, bookTitle, userId, borrowedAt, returnedAt)`

---

## 3. Repositories

### What's Good
- All 3 query types are present:
  - **Derived queries**: `findByNameContainingIgnoreCase`, `existsByIsbn`, `findByUserId`, `existsByBookIdAndReturnDateIsNull` — good
  - **JPQL @Query**: `findAuthorByEmailDomain`, `findAvailableBooks`, `findBooksByAuthor`, `findBasicBooksByAuthor`, `findActiveUserBorrowsWithBooksAndUser`, `findBorrowHistoryWithDetails` — good
  - **Native queries**: `findByTag` (uses `ANY(tags)`), `findAllTags` (uses `UNNEST`), `findMostBorrowedBooksSince` — good
- `@Modifying` queries present: `markBooksAsAvailable`, `markBorrowsAsReturned`
- JOIN FETCH used in `BorrowRepository` to avoid N+1 — good

### Issues

**3.1 — Native queries returning records may have mapping issues**
- The JPQL queries returning records (`findAvailableBooks()`, `findBooksByAuthor()`, `findBookById()`, `findBasicBooksByAuthor()`) use `SELECT b.id AS id, ...` syntax — this works in Spring Boot 4.x because Spring Data can map aliases to record constructor parameters
- However, the **native queries** `findByTag()` returning `List<BookBasic>` and `findMostBorrowedBooksSince()` returning `List<MostBorrowedBook>` may still have mapping issues — native queries have different mapping behavior than JPQL, and record mapping from native queries may require an interface projection or `@SqlResultSetMapping` instead
- Verify these native query endpoints actually work at runtime

**3.2 — `BookRepository.markBooksAsAvailable()` is missing `@Transactional`**
- `@Modifying` queries that change data need `@Transactional`. Without it, you'll get a `TransactionRequiredException` at runtime
- The `BorrowRepository.markBorrowsAsReturned()` has the same issue

**3.3 — `AuthorRepository.findAuthorByEmailDomain()` is JPQL, not native**
- The task asked for a native query for the email domain search. This is JPQL. It works, but you're missing a native query example in `AuthorRepository`

**3.5 — No `countByAuthorId` in `BookRepository`**
- The task required this for checking if an author has books before deletion. Instead, `deleteAuthor()` uses `findBooksByAuthor()` which loads full DTOs just to check if the list is empty — wasteful. A simple `countByAuthorId(Long authorId)` or `existsByAuthorId(Long authorId)` would be much more efficient

---

## 4. Services

### What's Good
- Clean interface + impl separation
- Proper error handling with custom exceptions
- Business logic is correct (borrow/return flow, availability toggling)
- Helper methods (`findAuthorById`, `fetchBookById`, `mapToResponse`) keep code DRY
- `markBooksAsAvailable` handles both borrow records and book availability in one transaction

### Issues

**4.1 — `AuthorServiceImpl` and `BookServiceImpl` are missing `@Transactional`**
- `AuthorServiceImpl` has no `@Transactional` at all — not on the class, not on any method
- `BookServiceImpl` has `@Transactional` only on `updateBook` and `markBooksAsAvailable`, but `createBook` and `deleteBook` also modify data and should be transactional
- `BorrowServiceImpl` correctly has `@Transactional` on `borrowBook` and `returnBook` — good
- Best practice: add `@Transactional` at the class level for service implementations, or at minimum on every method that writes data

**4.2 — `BookServiceImpl.updateBook()` catches `OptimisticLockException` manually**
- The `@Version` mechanism throws `ObjectOptimisticLockingFailureException` (Spring's wrapper), not JPA's `OptimisticLockException`
- The catch block at line 109 catches `jakarta.persistence.OptimisticLockException` which may not be what Spring actually throws
- Better approach: let the exception propagate and handle it in the `GlobalExceptionHandler` with a handler for `ObjectOptimisticLockingFailureException`

**4.3 — `BookServiceImpl.updateBook()` doesn't validate ISBN uniqueness on update**
- If someone updates a book's ISBN to one that already exists, it will fail with a database constraint violation instead of a clean error message
- `AuthorServiceImpl.updateAuthor()` correctly checks email uniqueness — the same pattern should be applied to ISBN

**4.4 — `BorrowServiceImpl.borrowBook()` checks `existsByBookIdAndReturnDateIsNull` but doesn't check `book.isAvailable()`**
- There are two sources of truth for availability: the `available` field on `Book` and the existence of an active borrow record
- The code only checks the borrow record. If somehow the data gets out of sync, the `available` flag would be wrong. Consider checking both, or at least checking `book.isAvailable()` as the primary check

**4.5 — `AuthorServiceImpl.deleteAuthor()` uses `bookRepository.findBooksByAuthor()` to check for books**
- This loads full `BookResponse` DTOs just to check if the list is empty
- Use `bookRepository.countByAuthorId(id) > 0` or add `existsByAuthorId(Long authorId)` — much more efficient

---

## 5. Controllers

### What's Good
- Clean and thin — controllers only delegate to services, no business logic
- `@Valid` used on create endpoints
- Proper HTTP status codes: `@ResponseStatus(HttpStatus.CREATED)` for POST, `HttpStatus.NO_CONTENT` for DELETE
- Constructor injection

### Issues

**5.1 — API paths don't match the task specification**
- Task specified: `/api/v0/library/authors`, `/api/v0/library/books`, `/api/v0/library/borrow`
- Actual: `/api/authors`, `/api/books`, `/api/borrows`
- The paths should match what was specified in the requirements

**5.2 — `BookController.createBook()` is missing `@Valid`**
- Line 24: `@RequestBody CreateBookRequest request` — no `@Valid`
- The `CreateBookRequest` has `@NotBlank` and `@NotNull` annotations, but they won't be enforced without `@Valid`
- `AuthorController.createAuthor()` and `BorrowController.borrowBook()` correctly use `@Valid`

**5.3 — `BookController.getBooksByTag()` uses `@RequestParam` instead of `@PathVariable`**
- Task specified: `GET /tag/{tag}` (path variable)
- Actual: `GET /tag?tag=xxx` (query parameter)
- Minor difference but doesn't match the spec

**5.4 — `BorrowController.getMostBorrowedBooksSince()` parses date manually**
- Line 51: `java.time.LocalDateTime.parse(date)` — manual parsing
- Use `@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)` with `LocalDateTime` parameter type directly, and Spring will parse it for you
- Also, if the date string is malformed, the manual parse will throw an unhandled `DateTimeParseException`

**5.5 — No logging in controllers**
- In our project, we add `log.debug(...)` at the start of controller methods for traceability
- None of the controllers have any logging

---

## 6. Exception Handling

### What's Good
- `GlobalExceptionHandler` with `@RestControllerAdvice` — correct pattern
- Uses `ProblemDetail` (RFC 7807) — modern and correct
- Reusable `build()` helper method — clean
- Catch-all `Exception` handler for unexpected errors — good
- Custom exceptions are simple and focused

### Issues

**6.1 — No handler for `MethodArgumentNotValidException`**
- When `@Valid` fails (e.g., blank author name), Spring throws `MethodArgumentNotValidException`
- Without a handler, the default Spring error response is returned, which is inconsistent with your `ProblemDetail` format
- Add a handler that extracts field errors and returns them in a structured way

**6.2 — No handler for `ObjectOptimisticLockingFailureException`**
- Spring's optimistic locking throws this exception, not `jakarta.persistence.OptimisticLockException`
- The manual catch in `BookServiceImpl.updateBook()` may not catch the right exception
- Add a handler in `GlobalExceptionHandler` for `ObjectOptimisticLockingFailureException` → 409 Conflict

**6.3 — No handler for `HttpMessageNotReadableException`**
- If someone sends malformed JSON, the error response will be Spring's default, not your `ProblemDetail` format

**6.4 — `ConflictException` and `ConcurrencyException` both map to 409 but are separate classes**
- Consider whether you really need both. `ConflictException` is used for business conflicts (duplicate email/ISBN), `ConcurrencyException` for optimistic locking. The distinction is fine, but make sure it's intentional

**6.5 — `OperationNotAllowedException` maps to 400 Bad Request**
- "Operation not allowed" semantically sounds more like 403 Forbidden or 422 Unprocessable Entity
- 400 is for malformed requests. "Cannot delete book with active borrows" is a valid request with a business rule violation — 422 would be more appropriate
- This is a minor point but worth thinking about

---

## 7. Unit Tests

### What's Good
- Good coverage for all three services (Author, Book, Borrow)
- Tests both success and error cases
- Uses Mockito correctly (`@Mock`, `@InjectMocks`, `@ExtendWith(MockitoExtension.class)`)
- Tests verify the right exceptions are thrown
- `BookServiceImplTest` covers: create success, author not found, ISBN exists, get by ID, not found, update, optimistic lock, delete, delete with borrows, mark available, empty list, partial update
- `BorrowServiceImplTest` covers: borrow success, user not found, book not found, already borrowed, return success, borrow not found, already returned, active borrows, history, most borrowed, null date

### Issues

**7.1 — No `@DisplayName` annotations on tests**
- In our project, we use `@DisplayName("Should create book successfully")` on every test
- Makes test output much more readable
- Current test names like `shouldCreateBookSuccessfully` are decent but `@DisplayName` is the standard

**7.2 — Uses JUnit assertions instead of AssertJ**
- Current: `assertEquals("Title", response.title())`, `assertThrows(ResourceNotFoundException.class, ...)`
- In our project we use AssertJ: `assertThat(response.title()).isEqualTo("Title")`, `assertThatThrownBy(() -> ...).isInstanceOf(...).hasMessageContaining(...)`
- AssertJ provides better error messages and more fluent API

**7.3 — No `@BeforeEach` setup method**
- Test data is created inline in each test method
- In our project, we use `@BeforeEach void setUp()` to initialize common test data
- Reduces duplication across tests

**7.4 — No `verify()` calls on most tests**
- Some tests verify repository calls (e.g., `verify(bookRepository).delete(book)`) but most don't
- For example, `shouldBorrowBookSuccessfully` verifies `bookRepository.save(book)` but doesn't verify `borrowRepository.save(any())`
- Verifying that the right repository methods were called with the right arguments is important

**7.5 — `shouldThrowConcurrencyException` test may not reflect real behavior**
- The test mocks `bookRepository.findById(1L)` to throw `OptimisticLockException`
- In reality, `findById` never throws this — it's thrown on `save()` or flush
- The test passes because the catch block catches it, but it doesn't test the real scenario

---

## 8. Flyway Migrations

### What's Good
- Separate migration files per table — clean
- Proper use of `BIGSERIAL`, `VARCHAR`, `TEXT[]`, `TIMESTAMP`
- Foreign key constraints with `ON DELETE RESTRICT` — good, prevents orphaned records
- `DEFAULT TRUE` for `available`, `DEFAULT 0` for `version`

### Issues

**8.1 — Table naming inconsistency**
- `authors` (plural), `books` (plural), `users` (plural), `borrow` (singular)
- Should be consistent — either all plural or all singular

**8.2 — No indexes**
- `books.author_id` should have an index — it's used in `findByAuthorId` and `findBooksByAuthor`
- `borrow.book_id` and `borrow.user_id` should have indexes — used in multiple queries
- `borrow.return_date` could benefit from a partial index for active borrows: `CREATE INDEX idx_borrow_active ON borrow(book_id) WHERE return_date IS NULL`

---

## 9. Bonus Points

- Added a `User` entity with full CRUD — extra work beyond the task, shows initiative
- `markBooksAsAvailable` with batch update + borrow record cleanup — well thought out
- `MostBorrowedBook` as a dedicated DTO — clean approach
- `ON DELETE RESTRICT` on foreign keys — good database design
- `spring.jpa.hibernate.ddl-auto=validate` — correct for Flyway-managed schemas

---

## Summary

| Area | Score | Notes |
|------|-------|-------|
| Entities | 7/10 | Good structure, but genre shouldn't be required, table naming inconsistent |
| DTOs | 7/10 | Good use of records, missing some validations and `authorId` in response |
| Repositories | 7/10 | All 3 query types present, JPQL projections work in Spring Boot 4.x, but native query record mapping should be verified |
| Services | 8/10 | Clean logic, good error handling, missing `@Transactional` in some places |
| Controllers | 6/10 | Clean and thin, but paths don't match spec, missing `@Valid` on create book |
| Exception Handling | 7/10 | Good foundation with ProblemDetail, missing validation error handler |
| Unit Tests | 7/10 | Good coverage, should use AssertJ and `@DisplayName` |
| Flyway | 7/10 | Clean migrations, missing indexes |
| **Overall** | **7/10** | Solid first implementation. Main areas to fix: missing `@Transactional`, API paths, validation gaps, and native query record mapping |

# Task: Book Library Management System

## Overview

Build a backend feature for managing a simple book library. Users can manage **authors**, **books**, and **borrow/return books**.

**Estimated Time:** 2 days

---

## Business Requirements

### Authors

- An author has a **name** and an **email**
- Email must be unique across all authors
- You can create, read, update, and delete authors
- You can search authors by name (case-insensitive)
- You can find authors by email domain (e.g. all authors with `@gmail.com`)
- You cannot delete an author who still has books in the system

### Books

- A book has a **title**, **ISBN**, **genre**, and a list of **tags**
- Each book belongs to one author
- ISBN must be unique across all books
- A book tracks whether it is **available** for borrowing or not
- Books should support **optimistic locking** (concurrent updates should be detected)
- You can create, read, update, and delete books
- You can get all available books
- You can get books by a specific author
- You can get a lightweight list of books by author (only id, title, isbn, availability)
- You can find books by a specific tag
- You can get all distinct tags used across all books
- You can mark a batch of books as available by their IDs

### Borrowing

- A user can borrow a book (tracked by user ID)
- A user can return a borrowed book
- You can see a user's currently active borrows (not yet returned)
- You can see a user's full borrow history
- A book cannot be borrowed if it's already borrowed by someone else
- A book that was already returned cannot be returned again
- When a book is borrowed, it becomes unavailable. When returned, it becomes available again
- You can find the most borrowed books since a given date

---

## Technical Requirements

- Use **Spring Boot** with **Spring Data JPA** and **PostgreSQL**
- Create a Flyway migration for the database tables
- Tags should be stored as a **PostgreSQL text array** (`TEXT[]`)
- Your repositories must include examples of **all 3 types of queries**:
  1. Derived queries (Spring generates SQL from the method name)
  2. JPQL `@Query` (including at least one DTO projection and one JOIN FETCH)
  3. Native SQL `@Query` (including at least one that uses PostgreSQL array functions, and one `@Modifying` query)
- Implement proper **error handling** with a global exception handler (`@ControllerAdvice`)
- Return proper HTTP status codes (404 for not found, 400 for bad requests, 409 for concurrent conflicts)
- Write DTOs should have proper validation annotations
- Controllers should validate incoming request bodies
- Write unit tests for `BookServiceImpl` and `BorrowServiceImpl` (both success and error cases)

---

## API Endpoints

### Authors — `/api/v0/library/authors`

| Method | Path               | Description            |
| ------ | ------------------ | ---------------------- |
| POST   | `/`                | Create an author       |
| GET    | `/{id}`            | Get an author by ID    |
| GET    | `/`                | Get all authors        |
| GET    | `/search?name=xxx` | Search authors by name |
| PUT    | `/{id}`            | Update an author       |
| DELETE | `/{id}`            | Delete an author       |

### Books — `/api/v0/library/books`

| Method | Path                       | Description                         |
| ------ | -------------------------- | ----------------------------------- |
| POST   | `/`                        | Create a book                       |
| GET    | `/{id}`                    | Get a book by ID                    |
| GET    | `/available`               | Get all available books             |
| GET    | `/author/{authorId}`       | Get books by author                 |
| GET    | `/author/{authorId}/basic` | Get lightweight book list by author |
| GET    | `/tag/{tag}`               | Get books by tag                    |
| GET    | `/tags`                    | Get all distinct tags               |
| PUT    | `/{id}`                    | Update a book                       |
| DELETE | `/{id}`                    | Delete a book                       |

### Borrowing — `/api/v0/library/borrow`

| Method | Path                     | Description               |
| ------ | ------------------------ | ------------------------- |
| POST   | `/`                      | Borrow a book             |
| PUT    | `/{id}/return`           | Return a book             |
| GET    | `/user/{userId}/active`  | Get user's active borrows |
| GET    | `/user/{userId}/history` | Get user's borrow history |

---

## Checklist

### Day 1

- [ ] Create Flyway migration
- [ ] Create entities
- [ ] Create DTOs
- [ ] Create repositories (all 3 query types)
- [ ] Create AuthorService + implementation
- [ ] Create BookService + implementation

### Day 2

- [ ] Create BorrowService + implementation
- [ ] Create all 3 controllers
- [ ] Write unit tests for BookServiceImpl
- [ ] Write unit tests for BorrowServiceImpl
- [ ] Test all endpoints manually
- [ ] Verify error handling works correctly

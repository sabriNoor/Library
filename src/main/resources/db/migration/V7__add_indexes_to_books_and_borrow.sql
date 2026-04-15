-- Index for books.author_id
CREATE INDEX idx_books_author_id
    ON books(author_id);

-- Index for borrow.book_id
CREATE INDEX idx_borrow_book_id
    ON borrows(book_id);

-- Index for borrow.user_id
CREATE INDEX idx_borrow_user_id
    ON borrows(user_id);

-- Partial index for active borrows (PostgreSQL)
CREATE INDEX idx_borrow_active
    ON borrows(book_id)
    WHERE return_date IS NULL;
CREATE TABLE books(
    id BIGSERIAL PRIMARY KEY,

    isbn VARCHAR(50) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    genre VARCHAR(100) NOT NULL,
    tags TEXT [],

    author_id BIGINT NOT NULL,
    available BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT fk_books_author FOREIGN KEY (author_id) REFERENCES authors(id) ON DELETE RESTRICT

);
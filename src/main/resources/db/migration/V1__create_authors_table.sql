CREATE TABLE authors(
    id BIGSERIAL PRIMARY KEY,
    name varchar(255) NOT NULL ,
    email varchar(255) NOT NULL UNIQUE
);
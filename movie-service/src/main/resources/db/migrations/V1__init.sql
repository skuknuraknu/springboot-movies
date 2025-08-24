CREATE TABLE tb_movies (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    genre VARCHAR(100),
    release_year INT,
    rating DECIMAL(2,1),
    is_deleted boolean
);

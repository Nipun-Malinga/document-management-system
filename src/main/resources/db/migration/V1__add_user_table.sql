CREATE TABLE users
(
    id        BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    firstname VARCHAR(20)  NOT NULL,
    lastname  VARCHAR(20),
    username  VARCHAR(100) NOT NULL UNIQUE,
    email     VARCHAR(255) NOT NULL UNIQUE,
    password  VARCHAR(255) NOT NULL,
    role      ENUM ('USER', 'ADMIN') DEFAULT 'USER'
);
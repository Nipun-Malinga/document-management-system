CREATE TABLE users
(
    id        BIGINT AUTO_INCREMENT      NOT NULL PRIMARY KEY,
    firstname VARCHAR(20)                NOT NULL,
    lastname  VARCHAR(20),
    username  VARCHAR(100)               NOT NULL UNIQUE,
    email     VARCHAR(255) UNIQUE        NOT NULL,
    password  VARCHAR(255)               NOT NULL,
    role      VARCHAR(20) DEFAULT 'USER' NOT NULL
);
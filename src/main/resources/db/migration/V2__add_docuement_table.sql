CREATE TABLE document_contents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    content LONGTEXT
);

CREATE TABLE documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    public_id BINARY(16) UNIQUE DEFAULT (UUID_TO_BIN(UUID())) NOT NULL,
    title VARCHAR(255) NOT NULL,
    owner_id BIGINT NOT NULL,
    content_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    CONSTRAINT fk_owner_id
        FOREIGN KEY(owner_id) REFERENCES users(id) ON DELETE CASCADE ,
    CONSTRAINT fk_content_id
        FOREIGN KEY(content_id) REFERENCES document_contents(id) ON DELETE CASCADE
);
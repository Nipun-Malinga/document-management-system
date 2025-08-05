CREATE table document_version (
    id BIGINT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    document_id BIGINT NOT NULL,
    version_number BINARY(16) UNIQUE DEFAULT (UUID_TO_BIN(UUID())) NOT NULL,
    content LONGTEXT NOT NULL,
    author BIGINT NOT NULL,
    timestamp DATETIME,
    CONSTRAINT fk_version_document_id
        FOREIGN KEY (document_id) REFERENCES documents(id),
    CONSTRAINT fk_version_user_id
        FOREIGN KEY (author) REFERENCES users(id)
)
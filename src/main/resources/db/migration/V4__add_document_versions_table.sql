CREATE TABLE document_version_content (
  id BIGINT AUTO_INCREMENT PRIMARY KEY NOT NULL,
  content LONGTEXT
);


CREATE TABLE document_version (
    id BIGINT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    document_id BIGINT NOT NULL,
    version_number BINARY(16) UNIQUE DEFAULT (UUID_TO_BIN(UUID())) NOT NULL,
    version_content_id BIGINT NOT NULL,
    author BIGINT NOT NULL,
    timestamp DATETIME,
    CONSTRAINT fk_version_document_id
        FOREIGN KEY (document_id) REFERENCES documents(id),
    CONSTRAINT fk_version_content_id
        FOREIGN KEY (version_content_id) REFERENCES document_version_content(id),
    CONSTRAINT fk_version_user_id
        FOREIGN KEY (author) REFERENCES users(id)
)
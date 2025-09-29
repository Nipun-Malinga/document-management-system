CREATE TABLE document_branches
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    public_id   BINARY(16) UNIQUE DEFAULT (UUID_TO_BIN(UUID())) NOT NULL,
    branch_name VARCHAR(255) NOT NULL,
    content_id  BIGINT       NOT NULL,
    document_id BIGINT       NOT NULL,
    owner_id    BIGINT       NOT NULL,
    created_at  DATETIME              DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    status      VARCHAR(20)  NOT NULL DEFAULT 'PUBLIC',
    CONSTRAINT fk_branches_content_id
        FOREIGN KEY (content_id) REFERENCES document_contents (id),
    CONSTRAINT fk_branches_document_id
        FOREIGN KEY (document_id) REFERENCES documents (id)
);
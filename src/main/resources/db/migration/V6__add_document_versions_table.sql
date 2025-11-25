CREATE TABLE document_versions
(
    id         BIGINT                     NOT NULL AUTO_INCREMENT PRIMARY KEY,
    public_id  BINARY(16)                 NOT NULL DEFAULT (UUID_TO_BIN(UUID())) UNIQUE,
    branch_id  BIGINT                     NOT NULL,
    content_id BIGINT                     NOT NULL,
    author     BIGINT                     NOT NULL,
    name       VARCHAR(255)               NOT NULL,
    created_at DATETIME                   NOT NULL,
    status     ENUM ('PRIVATE', 'PUBLIC') NOT NULL DEFAULT 'PUBLIC',
    CONSTRAINT fk_version_branch_id
        FOREIGN KEY (branch_id) REFERENCES document_branches (id) ON DELETE NO ACTION,
    CONSTRAINT fk_version_content_id
        FOREIGN KEY (content_id) REFERENCES document_contents (id),
    CONSTRAINT fk_version_user_id
        FOREIGN KEY (author) REFERENCES users (id) ON DELETE RESTRICT
);
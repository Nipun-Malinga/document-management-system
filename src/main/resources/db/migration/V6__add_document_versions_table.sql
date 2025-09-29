CREATE TABLE document_versions
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    public_id  BINARY(16) UNIQUE DEFAULT (UUID_TO_BIN(UUID())) NOT NULL,
    branch_id  BIGINT       NOT NULL,
    content_id BIGINT       NOT NULL,
    author     BIGINT       NOT NULL,
    name       VARCHAR(255) NOT NULL,
    created_at DATETIME     NOT NULL,
    status     VARCHAR(20)  NOT NULL DEFAULT 'PUBLIC',
    CONSTRAINT fk_version_branch_id
        FOREIGN KEY (branch_id) REFERENCES document_branches (id) ON DELETE NO ACTION,
    CONSTRAINT fk_version_content_id
        FOREIGN KEY (content_id) REFERENCES document_contents (id),
    CONSTRAINT fk_version_user_id
        FOREIGN KEY (author) REFERENCES users (id) ON DELETE RESTRICT
);
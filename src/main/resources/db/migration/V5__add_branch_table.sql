CREATE TABLE document_branch_content (
    id BIGINT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    content LONGTEXT
);

CREATE TABLE document_branches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    public_id BINARY(16) UNIQUE DEFAULT (UUID_TO_BIN(UUID())) NOT NULL,
    branch_name VARCHAR(255) UNIQUE NOT NULL,
    content_id BIGINT NOT NULL,
    document_id BIGINT NOT NULL,
    version_id BIGINT NOT NULL,
    timestamp DATETIME,
    CONSTRAINT fk_branches_content_id
        FOREIGN KEY(content_id) REFERENCES document_branch_content(id),
    CONSTRAINT fk_branches_document_id
        FOREIGN KEY(document_id) REFERENCES documents(id),
    CONSTRAINT fk_branches_version_id
        FOREIGN KEY(version_id) REFERENCES document_version(id) ON DELETE CASCADE
)
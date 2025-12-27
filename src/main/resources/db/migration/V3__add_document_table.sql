CREATE TABLE documents
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    public_id  BINARY(16) UNIQUE                          DEFAULT (UUID_TO_BIN(UUID())) NOT NULL,
    title      VARCHAR(255)                      NOT NULL,
    owner_id   BIGINT                            NOT NULL,
    created_at DATETIME                                   DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME                                   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    status     ENUM ('PUBLIC', 'PRIVATE')        NOT NULL DEFAULT 'PUBLIC',
    trashed    BOOLEAN                           NOT NULL DEFAULT FALSE,
    favorite   BOOLEAN                           NOT NULL DEFAULT FALSE,
    folder_id  BIGINT,
    CONSTRAINT fk_document_owner_id
        FOREIGN KEY (owner_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_document_folder_id
        FOREIGN KEY (folder_id) REFERENCES folders (id) ON DELETE CASCADE
);
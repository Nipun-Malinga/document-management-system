CREATE TABLE folders
(
    id            BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    public_id     BINARY(16) UNIQUE     DEFAULT (UUID_TO_BIN(UUID())) NOT NULL,
    parent_folder BIGINT,
    name          VARCHAR(255) NOT NULL,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    owner_id      BIGINT       NOT NULL,
    CONSTRAINT fk_folders_owner_id
        FOREIGN KEY (owner_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_folders_parent_folder
        FOREIGN KEY (parent_folder) REFERENCES folders (id) ON DELETE CASCADE
);
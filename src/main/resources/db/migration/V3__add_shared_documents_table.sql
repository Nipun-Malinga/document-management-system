CREATE TABLE shared_documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    document_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    permission VARCHAR(20) DEFAULT 'READ_ONLY',
    CONSTRAINT fk_shared_document_id
        FOREIGN KEY(document_id) REFERENCES documents(id) ON DELETE CASCADE,
    CONSTRAINT fk_shared_user_id
        FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
)
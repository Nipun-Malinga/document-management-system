CREATE TABLE document_comments
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    parent_comment_id BIGINT,
    document_id       BIGINT  NOT NULL,
    user_id           BIGINT  NOT NULL,
    likes             INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT fk_comments_parent_comment_id
        FOREIGN KEY (parent_comment_id) REFERENCES document_comments (id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_document_id
        FOREIGN KEY (document_id) REFERENCES documents (id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_user_id
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE NO ACTION
);
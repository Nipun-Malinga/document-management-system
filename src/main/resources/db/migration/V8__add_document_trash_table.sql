CREATE TABLE document_trash
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    document_id BIGINT,
    branch_id   BIGINT,
    added_date  DATETIME                          NOT NULL DEFAULT current_timestamp,
    CONSTRAINT fk_trash_document_id
        FOREIGN KEY (document_id) REFERENCES documents (id) ON DELETE CASCADE,
    CONSTRAINT fk_trash_branch_id
        FOREIGN KEY (branch_id) REFERENCES document_branches (id) ON DELETE CASCADE
);
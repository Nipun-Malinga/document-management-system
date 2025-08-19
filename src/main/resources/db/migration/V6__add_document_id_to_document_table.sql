ALTER TABLE document_version ADD COLUMN branch_id BIGINT;

Alter Table document_version ADD
    CONSTRAINT fk_version_branch_id
        FOREIGN KEY(branch_id) REFERENCES document_branches(id) ON DELETE CASCADE ;
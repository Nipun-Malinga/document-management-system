CREATE TABLE document_templates
(
    id       BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    title    VARCHAR(255) NOT NULL,
    template LONGTEXT
)
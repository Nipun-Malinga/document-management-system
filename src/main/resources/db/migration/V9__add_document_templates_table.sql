CREATE TABLE document_templates
(
    id       BIGINT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    title    VARCHAR(255)                      NOT NULL,
    template LONGTEXT
)
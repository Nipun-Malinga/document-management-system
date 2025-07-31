package com.nipun.system.document;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findAllByOwnerId(Long ownerId);
    Optional<Document> findByPublicIdAndOwnerId(UUID publicId, Long ownerId);
}

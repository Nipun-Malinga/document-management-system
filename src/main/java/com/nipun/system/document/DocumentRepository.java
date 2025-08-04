package com.nipun.system.document;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    Page<Document> findAllByOwnerId(Long ownerId, Pageable pageable);
    Optional<Document> findByPublicIdAndOwnerId(UUID publicId, Long ownerId);
}

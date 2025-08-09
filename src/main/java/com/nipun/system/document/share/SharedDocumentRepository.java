package com.nipun.system.document.share;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SharedDocumentRepository extends JpaRepository<SharedDocument, Long> {
    Optional<SharedDocument> findByDocumentIdAndSharedUserId(Long documentId, Long userId);
    Optional<SharedDocument> findByDocumentPublicIdAndSharedUserId(UUID documentId, Long userId);
    Optional<SharedDocument> findByDocumentPublicId(UUID documentId);
}

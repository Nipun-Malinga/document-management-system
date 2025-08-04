package com.nipun.system.document;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SharedDocumentRepository extends JpaRepository<SharedDocument, Long> {
    Optional<SharedDocument> findByDocumentPublicIdAndSharedUserId(UUID documentId, Long userId);
}

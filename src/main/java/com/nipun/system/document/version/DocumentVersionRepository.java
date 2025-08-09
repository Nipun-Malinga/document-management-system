package com.nipun.system.document.version;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, Long> {
  Page<DocumentVersion> findAllByDocumentId(Long documentId, Pageable pageable);

  Optional<DocumentVersion> findByVersionNumber(UUID versionNumber);

  Optional<DocumentVersion> findByVersionNumberAndDocumentPublicId(UUID versionNumber, UUID documentId);

  Optional<DocumentVersion> findFirstByDocumentIdOrderByTimestampDesc(Long documentId);

  @Modifying
  @Query("delete from DocumentVersion d where  d.timestamp > :timestamp and d.document.id = :documentId")
  void deleteDocumentVersionsAfter(@Param("documentId") Long documentId,@Param("timestamp") LocalDateTime timestamp);
}
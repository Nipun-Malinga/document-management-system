package com.nipun.system.document.version;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, Long> {
  Page<DocumentVersion> findAllByDocumentId(Long documentId, Pageable pageable);

  Optional<DocumentVersion> findByVersionNumber(UUID versionNumber);
}
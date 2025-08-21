package com.nipun.system.document.version;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, Long> {

    /*
        FIXME: FIX THE CONFLICT WHEN RESTORING BRANCH VERSIONS, SYSTEM MISTAKENLY DELETE MAIN DOCUMENT VERSIONS
    */

  @Query("select v from DocumentVersion v where v.document.id = :documentId and v.branch is null")
  Page<DocumentVersion> findAllByDocumentId(@Param("documentId") Long documentId, Pageable pageable);

  @EntityGraph(attributePaths = {"content", "document", "document.sharedDocuments"})
  Optional<DocumentVersion> findByVersionNumberAndDocumentPublicId(UUID versionNumber, UUID documentId);

  @EntityGraph(attributePaths = {"content", "documentBranches"})
  Optional<DocumentVersion> findFirstByDocumentIdOrderByTimestampDesc(Long documentId);

  @Modifying
  @Query("delete from DocumentVersion d where  d.timestamp > :timestamp and d.document.id = :documentId")
  void deleteDocumentVersionsAfter(@Param("documentId") Long documentId,@Param("timestamp") LocalDateTime timestamp);

  Page<DocumentVersion> findAllByDocumentPublicIdAndBranchPublicId(UUID documentId, UUID branchId, Pageable pageable);
}
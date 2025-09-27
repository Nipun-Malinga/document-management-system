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

public interface VersionRepository extends JpaRepository<Version, Long> {

  @Query("select v from Version v where v.document.id = :documentId and v.branch is null")
  Page<Version> findAllByDocumentId(@Param("documentId") Long documentId, Pageable pageable);

  @EntityGraph(attributePaths = {"content", "document", "document.sharedDocuments"})
  Optional<Version> findByVersionNumberAndDocumentPublicId(UUID versionNumber, UUID documentId);

  @EntityGraph(attributePaths = {"content", "documentBranches"})
  Optional<Version> findFirstByDocumentIdOrderByTimestampDesc(Long documentId);

  @Modifying
  @Query("delete from Version d where  d.timestamp > :timestamp and d.document.id = :documentId and d.branch is null")
  void rollbackMainDocToPreviousVersion(@Param("documentId") Long documentId, @Param("timestamp") LocalDateTime timestamp);

  Page<Version> findAllByDocumentPublicIdAndBranchPublicId(UUID documentId, UUID branchId, Pageable pageable);
}
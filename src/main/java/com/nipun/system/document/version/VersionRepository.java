package com.nipun.system.document.version;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface VersionRepository extends JpaRepository<Version, Long> {

    @EntityGraph(attributePaths = {"branch"})
    @Query("select v from Version v where v.branch.document.id = :documentId")
    Page<Version> findAllByBranchDocumentId(@Param("documentId") Long documentId, Pageable pageable);

    @EntityGraph(attributePaths = {"branch"})
    @Query("select v from Version v where v.branch.document.id = :documentId and v.status = 'PUBLIC'")
    Page<Version> findAllByBranchDocumentIdAndStatusPublic(@Param("documentId") Long documentId, Pageable pageable);

    @Query("select v from Version v where v.publicId = :publicId and v.branch.document.id = :documentId")
    Optional<Version> findDocumentBranchVersion(@Param("publicId") UUID publicId, @Param("documentId") Long documentId);

    Optional<Version> findByPublicIdAndBranchDocumentId(UUID versionId, Long documentId);
}
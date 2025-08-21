package com.nipun.system.document;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    @EntityGraph(attributePaths = {"owner", "sharedDocuments"})
    Page<Document> findAllByOwnerId(Long ownerId, Pageable pageable);

    @EntityGraph(attributePaths = {"owner", "sharedDocuments", "content"})
    Optional<Document> findByPublicIdAndOwnerId(UUID publicId, Long ownerId);

    @EntityGraph(attributePaths = {"sharedDocuments"})
    @Query("select d from Document d join SharedDocument s on d.id = s.document.id where s.sharedUser.id = :userId")
    Page<Document> findAllSharedDocumentsWithUser(@Param("userId") Long ownerId, Pageable pageable);

    @EntityGraph(attributePaths = {"content", "sharedDocuments"})
    Optional<Document> findByPublicId(UUID  publicId);
}

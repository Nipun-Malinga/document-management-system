package com.nipun.system.document;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    Page<Document> findAllByOwnerId(Long ownerId, Pageable pageable);

    Optional<Document> findByPublicIdAndOwnerId(UUID publicId, Long ownerId);

    @Query("select d from Document d join SharedDocument s on d.id = s.document.id where s.sharedUser.id = :userId")
    Page<Document> findAllSharedDocumentsWithUser(@Param("userId") Long ownerId, Pageable pageable);

    Optional<Document> findByPublicId(UUID  publicId);
}

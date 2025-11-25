package com.nipun.system.document.branch;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BranchRepository extends JpaRepository<Branch, Long> {
    @EntityGraph(attributePaths = {"content"})
    Optional<Branch> findByPublicIdAndDocumentId(UUID branchId, Long documentId);

    @EntityGraph(attributePaths = {"content"})
    Optional<Branch> findByPublicIdAndDocumentPublicId(UUID branchId, UUID documentId);

    Boolean existsByBranchNameAndDocumentId(String branchName, Long documentId);

    @EntityGraph(attributePaths = {"document"})
    Page<Branch> findAllByDocumentId(Long documentId, Pageable pageable);

    int countAllByDocumentId(Long documentId);

    Page<Branch> findAllByTrashedIsTrueAndOwnerId(Long ownerId, Pageable pageable);

    int countAllByTrashedIsTrueAndOwnerId(Long ownerId);
}

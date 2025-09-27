package com.nipun.system.document.branch;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BranchRepository extends JpaRepository<Branch, Long> {
    Optional<Branch> findByPublicIdAndVersionDocumentPublicId(UUID documentId, UUID branchId);

    Optional<Branch> findByPublicIdAndDocumentId(UUID branchId, Long documentId);

    Optional<Branch> findByBranchName(String branchName);

    @EntityGraph(attributePaths = {"document", "version"})
    Page<Branch> findAllByDocumentId(Long documentId, Pageable pageable);
}

package com.nipun.system.document.branch;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DocumentBranchRepository extends JpaRepository<DocumentBranch, Long> {
    Optional<DocumentBranch> findByPublicIdAndVersionDocumentPublicId(UUID documentId, UUID branchId);
    Optional<DocumentBranch> findByBranchName(String branchName);

    Page<DocumentBranch> findAllByDocumentId(Long documentId, Pageable pageable);
}

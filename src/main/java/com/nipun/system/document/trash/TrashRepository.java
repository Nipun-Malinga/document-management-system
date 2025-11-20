package com.nipun.system.document.trash;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TrashRepository extends JpaRepository<Trash, Long> {
    Page<Trash> findAllByBranchIsNullAndDocumentOwnerId(Long id, Pageable pageable);

    Page<Trash> findAllByDocumentIsNullAndBranchOwnerId(Long id, Pageable pageable);

    Optional<Trash> findByDocumentIdAndBranchIsNull(Long documentId);

    boolean existsByDocumentIdAndBranchIsNull(Long documentId);

    Optional<Trash> findByBranchIdAndBranchDocumentId(Long branchId, Long documentId);

    boolean existsByBranchIdAndBranchDocumentId(Long branchId, Long documentIs);

    int countAllByDocumentOwnerId(Long ownerId);

    int countAllByBranchDocumentPublicIdAndBranchDocumentOwnerId(UUID documentId, Long ownerId);
}

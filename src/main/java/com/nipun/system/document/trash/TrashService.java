package com.nipun.system.document.trash;

import com.nipun.system.document.base.DocumentRepository;
import com.nipun.system.document.base.exceptions.DocumentNotFoundException;
import com.nipun.system.document.branch.BranchRepository;
import com.nipun.system.document.branch.exceptions.BranchNotFoundException;
import com.nipun.system.document.permission.PermissionUtils;
import com.nipun.system.document.permission.exceptions.UnauthorizedDocumentException;
import com.nipun.system.document.trash.exceptions.TrashNotFoundException;
import com.nipun.system.document.trash.exceptions.UnauthorizedBranchDeletionException;
import com.nipun.system.shared.dtos.PaginatedData;
import com.nipun.system.shared.exceptions.UnauthorizedOperationException;
import com.nipun.system.shared.utils.UserIdUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class TrashService {

    private final TrashRepository trashRepository;
    private final DocumentRepository documentRepository;
    private final BranchRepository branchRepository;
    private final TrashMapper trashMapper;

    @CacheEvict(value = "documents", key = "{#documentId}")
    @Transactional
    public void addDocumentToTrash(UUID documentId) {
        var userId = UserIdUtils.getUserIdFromContext();

        var document = documentRepository
                .findByPublicId(documentId)
                .orElseThrow(DocumentNotFoundException::new);

        if (PermissionUtils.isUnauthorizedUser(userId, document))
            throw new UnauthorizedDocumentException();

        document.setTrashed(true);

        documentRepository.save(document);
        trashRepository.save(TrashFactory.createDocumentTrash(document));
    }

    @CacheEvict(value = "document_branch_contents", key = "{#documentId, #branchId}")
    @Transactional
    public void addBranchToTrash(UUID documentId, UUID branchId) {
        var userId = UserIdUtils.getUserIdFromContext();

        var branch = branchRepository
                .findByPublicIdAndDocumentPublicId(branchId, documentId)
                .orElseThrow(BranchNotFoundException::new);

        if (branch.getBranchName().equals("main"))
            throw new UnauthorizedBranchDeletionException("You cannot delete main branch");

        if (PermissionUtils.isUnauthorizedUser(userId, branch.getDocument()))
            throw new UnauthorizedDocumentException();

        branch.setTrashed(true);

        branchRepository.save(branch);
        trashRepository.save(TrashFactory.createBranchTrash(branch));
    }

    @Transactional(readOnly = true)
    public PaginatedData getAllTrashedDocuments(int pageNumber, int pageSize) {
        var userId = UserIdUtils.getUserIdFromContext();

        PageRequest request = PageRequest.of(pageNumber, pageSize);

        var trashList = trashRepository.findAllByBranchIsNullAndDocumentOwnerId(userId, request);

        var trashDtoList = trashList
                .stream()
                .map(trashMapper::toTrashDocumentDto)
                .toList();

        return new PaginatedData(
                trashDtoList,
                pageNumber,
                pageSize
                ,
                trashList.getTotalPages(),
                trashList.getTotalElements(),
                trashList.hasNext(),
                trashList.hasPrevious()
        );
    }

    @Transactional(readOnly = true)
    public PaginatedData getAllTrashedBranches(int pageNumber, int pageSize) {
        var userId = UserIdUtils.getUserIdFromContext();

        PageRequest request = PageRequest.of(pageNumber, pageSize);

        var trashList = trashRepository.findAllByDocumentIsNullAndDocumentOwnerId(userId, request);

        var trashDtoList = trashList.stream().map(trashMapper::toTrashDocumentDto).toList();

        return new PaginatedData(
                trashDtoList,
                pageNumber,
                pageSize,
                trashList.getTotalPages(),
                trashList.getTotalElements(),
                trashList.hasNext(),
                trashList.hasPrevious()
        );
    }

    @CacheEvict(value = "documents", key = "{#documentId}")
    @Transactional
    public void restoreDocument(UUID documentId) {
        handleDocumentAction(documentId, ActionType.RESTORE);
    }

    @CacheEvict(value = "document_branch_contents", key = "{#documentId, #branchId}")
    @Transactional
    public void restoreBranch(UUID documentId, UUID branchId) {
        handleBranchAction(documentId, branchId, ActionType.RESTORE);
    }

    @CacheEvict(value = "documents", key = "{#documentId}")
    @Transactional
    public void deleteDocument(UUID documentId) {
        handleDocumentAction(documentId, ActionType.DELETE);
    }

    @CacheEvict(value = "document_branch_contents", key = "{#documentId, #branchId}")
    @Transactional
    public void deleteBranch(UUID documentId, UUID branchId) {
        handleBranchAction(documentId, branchId, ActionType.DELETE);
    }

    public int getTrashedDocumentCount() {
        var userId = UserIdUtils.getUserIdFromContext();
        return trashRepository.countAllByDocumentOwnerId(userId);
    }

    public int getTrashedBranchesCount(UUID documentId) {
        var userId = UserIdUtils.getUserIdFromContext();
        return trashRepository.countAllByBranchDocumentPublicId(documentId);
    }

    private void handleDocumentAction(UUID documentId, ActionType action) {
        var userId = UserIdUtils.getUserIdFromContext();

        var document = documentRepository.findByPublicId(documentId)
                .orElseThrow(DocumentNotFoundException::new);

        if (!PermissionUtils.isOwner(userId, document)) {
            throw new UnauthorizedOperationException();
        }

        var trash = trashRepository
                .findByDocumentIdAndBranchIsNull(document.getId())
                .orElseThrow(DocumentNotFoundException::new);

        switch (action) {
            case RESTORE -> {
                document.setTrashed(false);
                documentRepository.save(document);
            }
            case DELETE -> documentRepository.delete(document);
        }

        trashRepository.delete(trash);
    }

    private void handleBranchAction(UUID documentId, UUID branchId, ActionType action) {
        var userId = UserIdUtils.getUserIdFromContext();

        var branch = branchRepository
                .findByPublicIdAndDocumentPublicId(branchId, documentId)
                .orElseThrow(BranchNotFoundException::new);

        if (!PermissionUtils.isOwner(userId, branch.getDocument())) {
            throw new UnauthorizedOperationException();
        }

        var trash = trashRepository
                .findByDocumentIdAndBranchId(branch.getId(), branch.getDocument().getId())
                .orElseThrow(TrashNotFoundException::new);

        switch (action) {
            case RESTORE -> {
                branch.setTrashed(false);
                branchRepository.save(branch);
            }
            case DELETE -> branchRepository.delete(branch);
        }

        trashRepository.delete(trash);
    }

    private enum ActionType {
        RESTORE, DELETE
    }
}

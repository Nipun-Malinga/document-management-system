package com.nipun.system.document.websocket.state;

import com.nipun.system.document.branch.BranchRepository;
import com.nipun.system.document.branch.exceptions.BranchNotFoundException;
import com.nipun.system.document.share.exceptions.ReadOnlyDocumentException;
import com.nipun.system.document.share.exceptions.UnauthorizedDocumentException;
import com.nipun.system.document.websocket.permissions.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class StateServiceImpl implements StateService {
    private final BranchRepository branchRepository;
    private final StateCacheService stateCacheService;
    private final PermissionService permissionService;

    @Override
    public String setDocumentState(UUID documentId, UUID branchId, Long userId, String content) {
        if (permissionService.isUnauthorizedUser(documentId, userId))
            throw new UnauthorizedDocumentException();

        if (permissionService.isReadOnlyUser(documentId, userId))
            throw new ReadOnlyDocumentException();

        stateCacheService.setDocumentState(documentId, branchId, content);

        return content;
    }

    @Override
    public String getDocumentState(UUID documentId, UUID branchId, Long userId) {
        if (permissionService.isUnauthorizedUser(documentId, userId))
            throw new UnauthorizedDocumentException();

        var state = stateCacheService.getDocumentState(documentId, branchId);

        if (state != null)
            return state;

        var branch = branchRepository
                .findByPublicIdAndDocumentPublicId(branchId, documentId)
                .orElseThrow(BranchNotFoundException::new);

        stateCacheService.setDocumentState(documentId, branchId, branch.getBranchContent());

        return branch.getBranchContent();
    }
}

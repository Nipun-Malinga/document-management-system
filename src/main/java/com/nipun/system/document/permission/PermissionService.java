package com.nipun.system.document.permission;

import com.nipun.system.document.branch.BranchRepository;
import com.nipun.system.document.branch.exceptions.BranchNotFoundException;
import com.nipun.system.document.permission.dtos.PermissionResponse;
import com.nipun.system.document.share.Permission;
import com.nipun.system.shared.utils.UserIdUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class PermissionService {
    private final BranchRepository branchRepository;

    public PermissionResponse validateUserPermissions(UUID documentId, UUID branchId) {
        var userId = UserIdUtils.getUserIdFromContext();

        var branch = branchRepository
                .findByPublicIdAndDocumentPublicId(branchId, documentId)
                .orElseThrow(BranchNotFoundException::new);

        var permission = PermissionUtils.isUnauthorizedUser(userId, branch.getDocument())
                ? Permission.UNAUTHORIZED : PermissionUtils.isReadOnlyUser(userId, branch.getDocument())
                ? Permission.READ_ONLY : Permission.READ_WRITE;

        return new PermissionResponse(userId, documentId, branchId, permission);
    }
}

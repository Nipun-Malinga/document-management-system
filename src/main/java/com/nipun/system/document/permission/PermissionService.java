package com.nipun.system.document.permission;

import com.nipun.system.document.permission.dtos.PermissionResponse;

import java.util.UUID;

public interface PermissionService {
    PermissionResponse validateUserPermissions(UUID documentId, UUID branchId);
}

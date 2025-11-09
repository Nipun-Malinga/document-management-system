package com.nipun.system.document.permission.dtos;

import com.nipun.system.document.share.Permission;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@AllArgsConstructor
@Data
public class PermissionResponse {
    long userId;
    UUID documentId;
    UUID branchId;
    Permission permission;
}

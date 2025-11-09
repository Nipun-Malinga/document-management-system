package com.nipun.system.document.permission;

import com.nipun.system.document.permission.dtos.PermissionResponse;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/permissions/documents")
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping("/{documentId}/branches/{branchId}")
    public ResponseEntity<PermissionResponse> validate(
            @PathVariable(name = "documentId")
            @Parameter(description = "The ID of the document", example = "bfb8777b-59bd-422b-8132-d1f64b09590d")
            UUID documentId,
            @PathVariable(name = "branchId")
            @Parameter(description = "Document branch ID", example = "8d5177f7-bc39-42b0-84bc-3a945be383c4")
            UUID branchId
    ) {
        var permissions = permissionService.validateUserPermissions(documentId, branchId);
        return ResponseEntity.ok(permissions);
    }
}

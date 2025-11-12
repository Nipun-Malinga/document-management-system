package com.nipun.system.document.branch.dtos;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class BranchResponse {
    private UUID id;

    private UUID documentId;

    private String branchName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private boolean trashed;
}

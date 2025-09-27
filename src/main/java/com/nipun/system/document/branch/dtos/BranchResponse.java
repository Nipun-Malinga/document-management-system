package com.nipun.system.document.branch.dtos;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class BranchResponse {
    private UUID branchId;

    private UUID documentId;

    private UUID versionNumber;

    private String branchName;

    private LocalDateTime timestamp;
}

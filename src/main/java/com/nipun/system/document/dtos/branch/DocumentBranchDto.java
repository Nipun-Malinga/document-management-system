package com.nipun.system.document.dtos.branch;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class DocumentBranchDto {
    private UUID branchId;

    private UUID versionNumber;

    private String branchName;

    private String content;

    private LocalDateTime timestamp;
}

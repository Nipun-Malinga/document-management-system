package com.nipun.system.document.branch;

import com.nipun.system.document.dtos.ContentDto;
import com.nipun.system.document.dtos.branch.DocumentBranchDto;
import com.nipun.system.document.dtos.common.PaginatedData;

import java.util.UUID;

public  interface DocumentBranchService {
    DocumentBranchDto createBranch(UUID documentId, UUID versionId, String branchName);

    ContentDto getBranchContent(UUID documentId, UUID branchId);

    ContentDto updateBranchContent(UUID documentId, UUID branchId, String content);

    PaginatedData getAllBranches(UUID documentId, int pageNumber, int size);

    void deleteBranch(UUID documentId, UUID branchId);

    PaginatedData getAllBranchVersions(UUID documentId, UUID branchId, int pageNumber, int size);

    void mergeToMainBranch(UUID documentId, UUID branchId);

    void mergeSpecificBranches(UUID documentId, UUID branchId, UUID mergeBranchId) ;
}

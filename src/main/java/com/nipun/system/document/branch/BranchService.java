package com.nipun.system.document.branch;

import com.nipun.system.document.base.dtos.ContentResponse;
import com.nipun.system.document.branch.dtos.BranchResponse;
import com.nipun.system.shared.dtos.PaginatedData;

import java.util.UUID;

public  interface BranchService {
    BranchResponse createBranch(UUID documentId, UUID versionId, String branchName);

    ContentResponse getBranchContent(UUID documentId, UUID branchId);

    ContentResponse updateBranchContent(UUID documentId, UUID branchId, String content);

    PaginatedData getAllBranches(UUID documentId, int pageNumber, int size);

    void deleteBranch(UUID documentId, UUID branchId);

    PaginatedData getAllBranchVersions(UUID documentId, UUID branchId, int pageNumber, int size);

    void mergeToMainBranch(UUID documentId, UUID branchId);

    void mergeSpecificBranches(UUID documentId, UUID branchId, UUID mergeBranchId) ;
}

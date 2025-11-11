package com.nipun.system.document.branch;

import com.nipun.system.document.base.dtos.ContentResponse;
import com.nipun.system.document.branch.dtos.BranchResponse;
import com.nipun.system.document.diff.dtos.DiffResponse;
import com.nipun.system.shared.dtos.PaginatedData;

import java.util.UUID;

public interface BranchService {
    BranchResponse createBranch(UUID documentId, UUID branchId, String branchName);

    PaginatedData getAllBranches(UUID documentId, int pageNumber, int size);

    ContentResponse getBranchContent(UUID documentId, UUID branchId);

    ContentResponse updateBranchContent(UUID documentId, UUID branchId, String content);

    DiffResponse getBranchDiffs(UUID documentId, UUID base, UUID compare);

    void mergeBranches(UUID documentId, UUID branchId, UUID mergeBranchId);
}

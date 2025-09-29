package com.nipun.system.document.version;

import com.nipun.system.document.Status;
import com.nipun.system.document.base.dtos.ContentResponse;
import com.nipun.system.document.diff.dtos.DiffResponse;
import com.nipun.system.document.version.dtos.VersionResponse;
import com.nipun.system.shared.dtos.PaginatedData;

import java.util.UUID;

public interface VersionService {
    VersionResponse createNewVersion(UUID documentId, UUID branchId, String title, Status status);

    PaginatedData getAllDocumentVersions(UUID documentId, int pageNumber, int size);

    ContentResponse getVersionContent(UUID documentId, UUID versionNumber);

    DiffResponse getVersionDiffs(UUID documentId, UUID base, UUID compare);

    void mergeVersionToBranch(UUID documentId, UUID branchId, UUID versionId);
}

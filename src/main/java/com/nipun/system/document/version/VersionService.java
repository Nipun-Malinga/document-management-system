package com.nipun.system.document.version;

import com.nipun.system.document.base.dtos.ContentResponse;
import com.nipun.system.shared.dtos.PaginatedData;
import com.nipun.system.document.diff.dtos.DiffResponse;

import java.util.UUID;

public interface VersionService {
    PaginatedData getAllDocumentVersions(UUID documentId, int pageNumber, int size);

    ContentResponse getVersionContent(UUID versionNumber, UUID documentId);

    DiffResponse getVersionDiffs(UUID documentId, UUID base, UUID compare);

    void restoreToPreviousVersion(UUID documentId);

    void restoreToDocumentSpecificVersion(UUID versionNumber, UUID documentId);
}

package com.nipun.system.document.version;

import com.nipun.system.document.dtos.ContentDto;
import com.nipun.system.document.dtos.common.PaginatedData;
import com.nipun.system.document.dtos.version.DiffResponse;

import java.util.UUID;

public interface DocumentVersionService {
    PaginatedData getAllDocumentVersions(UUID documentId, int pageNumber, int size);

    ContentDto getVersionContent(UUID versionNumber, UUID documentId);

    DiffResponse getVersionDiffs(UUID documentId, UUID base, UUID compare);

    void restoreToPreviousVersion(UUID documentId);

    void restoreToDocumentSpecificVersion(UUID versionNumber, UUID documentId);
}

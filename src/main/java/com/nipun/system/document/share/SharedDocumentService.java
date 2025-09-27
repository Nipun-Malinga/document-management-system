package com.nipun.system.document.share;

import com.nipun.system.shared.dtos.PaginatedData;
import com.nipun.system.document.share.dtos.SharedDocumentResponse;

import java.util.UUID;

public interface SharedDocumentService {
    SharedDocumentResponse shareDocument(Long sharedUserId, UUID documentId, Permission permission);

    PaginatedData getAllSharedDocumentsWithUser(int pageNumber, int size);

    void removeDocumentAccess(UUID documentId);

    void removeDocumentAccess(UUID documentId, Long sharedUerId);
}

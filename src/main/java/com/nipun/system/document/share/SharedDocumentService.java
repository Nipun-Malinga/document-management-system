package com.nipun.system.document.share;

import com.nipun.system.document.share.dtos.SharedDocumentResponse;
import com.nipun.system.shared.dtos.PaginatedData;

import java.util.List;
import java.util.UUID;

public interface SharedDocumentService {
    SharedDocumentResponse shareDocument(Long sharedUserId, UUID documentId, Permission permission);

    List<SharedDocumentResponse> getAllSharedUsers(UUID documentId);

    PaginatedData getAllSharedDocumentsWithUser(int pageNumber, int size);

    void removeDocumentAccess(UUID documentId);

    void removeDocumentAccess(UUID documentId, Long sharedUerId);
}

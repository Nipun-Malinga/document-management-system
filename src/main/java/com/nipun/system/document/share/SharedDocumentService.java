package com.nipun.system.document.share;

import com.nipun.system.document.dtos.common.PaginatedData;
import com.nipun.system.document.dtos.share.SharedDocumentDto;

import java.util.UUID;

public interface SharedDocumentService {
    SharedDocumentDto shareDocument(Long sharedUserId, UUID documentId, Permission permission);

    PaginatedData getAllSharedDocumentsWithUser(int pageNumber, int size);

    void removeDocumentAccess(UUID documentId);

    void removeDocumentAccess(UUID documentId, Long sharedUerId);
}

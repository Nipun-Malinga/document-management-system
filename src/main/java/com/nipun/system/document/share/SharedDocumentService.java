package com.nipun.system.document.share;

import com.nipun.system.document.share.dtos.SharedDocumentDto;
import com.nipun.system.document.share.dtos.SharedDocumentResponse;
import com.nipun.system.shared.dtos.PaginatedData;

import java.util.UUID;

public interface SharedDocumentService {
    SharedDocumentDto shareDocument(Long sharedUserId, UUID documentId, Permission permission);

    SharedDocumentResponse getAllSharedUsers(UUID documentId);

    PaginatedData getAllSharedDocumentsWithUser(int pageNumber, int size);

    void removeDocumentAccess(UUID documentId);

    void removeDocumentAccess(UUID documentId, Long sharedUerId);
}

package com.nipun.system.document.share;

import com.nipun.system.document.share.dtos.ShareDocumentRequest;
import com.nipun.system.document.share.dtos.SharedDocumentDto;
import com.nipun.system.document.share.dtos.SharedDocumentResponse;
import com.nipun.system.shared.dtos.CountResponse;
import com.nipun.system.shared.dtos.PaginatedData;

import java.util.UUID;

public interface SharedDocumentService {
    SharedDocumentDto shareDocument(UUID documentId, ShareDocumentRequest request);

    SharedDocumentResponse getAllSharedUsers(UUID documentId);

    CountResponse getSharedDocumentWithUserCount();

    PaginatedData getAllSharedDocumentsWithUser(int pageNumber, int size);

    void removeDocumentAccess(UUID documentId);

    void removeDocumentAccess(UUID documentId, Long sharedUerId);
}

package com.nipun.system.document.base;

import com.nipun.system.document.base.dtos.*;
import com.nipun.system.shared.dtos.PaginatedData;

import java.util.UUID;

public interface DocumentService {
    DocumentResponse createDocument(CreateDocumentRequest request);

    DocumentResponse getDocument(UUID documentId);

    PaginatedData getAllDocuments(int pageNumber, int size);

    DocumentResponse updateTitle(UUID documentId, UpdateTitleRequest request);

    void deleteDocument(UUID documentId);

    ContentResponse getContent(UUID documentId);

    ContentResponse updateContent(UUID documentId, UpdateContentRequest request);
}

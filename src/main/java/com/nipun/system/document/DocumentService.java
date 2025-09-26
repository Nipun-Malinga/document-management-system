package com.nipun.system.document;

import com.nipun.system.document.dtos.*;
import com.nipun.system.shared.dtos.PaginatedData;

import java.util.UUID;

public interface DocumentService {
    DocumentDto createDocument(CreateDocumentRequest request);

    DocumentDto getDocument(UUID documentId);

    PaginatedData getAllDocuments(int pageNumber, int size);

    DocumentDto updateTitle(UUID documentId, UpdateTitleRequest request);

    void deleteDocument(UUID documentId);

    ContentDto getContent(UUID documentId);

    ContentDto updateContent(UUID documentId, UpdateContentRequest request);
}

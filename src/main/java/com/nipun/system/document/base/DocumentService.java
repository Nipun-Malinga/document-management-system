package com.nipun.system.document.base;

import com.nipun.system.document.base.dtos.CreateDocumentRequest;
import com.nipun.system.document.base.dtos.DocumentResponse;
import com.nipun.system.document.base.dtos.UpdateTitleRequest;
import com.nipun.system.shared.dtos.PaginatedData;

import java.util.UUID;

public interface DocumentService {
    DocumentResponse createDocument(CreateDocumentRequest request);

    DocumentResponse getDocument(UUID documentId);

    PaginatedData getAllDocuments(int pageNumber, int size);

    DocumentResponse updateTitle(UUID documentId, UpdateTitleRequest request);
}

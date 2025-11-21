package com.nipun.system.document.base;

import com.nipun.system.document.base.dtos.CreateDocumentRequest;
import com.nipun.system.document.base.dtos.DocumentResponse;
import com.nipun.system.document.base.dtos.UpdateDocumentRequest;
import com.nipun.system.shared.dtos.CountResponse;
import com.nipun.system.shared.dtos.PaginatedData;

import java.util.UUID;

public interface DocumentService {
    DocumentResponse createDocument(CreateDocumentRequest request);

    DocumentResponse getDocument(UUID documentId);

    CountResponse getDocumentCount();

    PaginatedData getAllDocuments(int pageNumber, int size);

    DocumentResponse updateDocument(UUID documentId, UpdateDocumentRequest request);

    DocumentResponse toggleFavorite(UUID documentId);

    CountResponse getDocumentFavoriteCount();
}

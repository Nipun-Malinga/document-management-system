package com.nipun.system.document.websocket.state;

import com.nipun.system.document.DocumentRepository;
import com.nipun.system.document.cache.DocumentCacheService;
import com.nipun.system.document.exceptions.DocumentNotFoundException;
import com.nipun.system.document.share.SharedDocumentAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class DocumentWebsocketStateServiceImpl implements DocumentWebsocketStateService {

    private final DocumentRepository documentRepository;

    private final DocumentCacheService documentCacheService;
    private final SharedDocumentAuthService sharedDocumentAuthService;

    @Override
    public void setDocumentStatus(UUID documentId, String status) {
        documentCacheService.putDocumentCurrentStatus(documentId, status);
    }

    public String getDocumentStatusFromCache(UUID documentId) {
        String cachedContent = documentCacheService.getDocumentCurrentStatus(documentId);
        if (cachedContent != null) {
                return cachedContent;
        }

        var document = documentRepository.findByPublicId(documentId)
                .orElseThrow(DocumentNotFoundException::new);

        var content = document.getDocumentContent() == null ? "" : document.getDocumentContent();

        documentCacheService.putDocumentCurrentStatus(documentId, content);

        return content;
    }

    @Override
    public void saveDocumentState(UUID documentId) {

        var currentState = documentCacheService.getDocumentCurrentStatus(documentId);

        if(currentState == null) return;

        var document = documentRepository
                .findByPublicId(documentId)
                .orElseThrow(DocumentNotFoundException::new);

        document.addContent(currentState);
        documentRepository.save(document);
    }
}

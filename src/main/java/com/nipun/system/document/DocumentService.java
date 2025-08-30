package com.nipun.system.document;

import com.github.difflib.patch.PatchFailedException;
import com.nipun.system.document.exceptions.DocumentNotFoundException;
import com.nipun.system.document.utils.Utils;
import com.nipun.system.user.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@AllArgsConstructor
@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final CacheManager cacheManager;

    @Transactional
    public Document createDocument(Document document) {
        var userId = Utils.getUserIdFromContext();

        var user = userRepository.findById(userId).orElseThrow();

        documentRepository.save(Document.createDocument(document, user));

        return document;
    }

    public Document getDocument(UUID documentId) {

        var userId = Utils.getUserIdFromContext();

        return documentRepository
                .findByPublicIdAndOwnerId(documentId, userId)
                .orElseThrow(DocumentNotFoundException::new);
    }

    public Page<Document> getAllDocuments(int pageNumber, int size) {
        var userId = Utils.getUserIdFromContext();

        PageRequest pageRequest = PageRequest.of(pageNumber, size);

        return documentRepository.findAllByOwnerId(userId, pageRequest);
    }

    @Transactional
    public Document updateTitle(UUID documentId, Document documentRequest) {

        var userId = Utils.getUserIdFromContext();

        var document = documentRepository
                .findByPublicIdAndOwnerId(documentId, userId)
                .orElseThrow(DocumentNotFoundException::new);

        document = document.updateTitle(documentRequest);

        documentRepository.save(document);

        return document;
    }

    @Transactional
    public void deleteDocument(UUID documentId) {

        var userId = Utils.getUserIdFromContext();

        var document = documentRepository
                .findByPublicIdAndOwnerId(documentId, userId)
                .orElseThrow(DocumentNotFoundException::new);

        documentRepository.delete(document);
    }

    @Transactional
    public Content updateContent(UUID documentId, Content content) throws PatchFailedException {
        var userId = Utils.getUserIdFromContext();

        var user = userRepository.findById(userId).orElseThrow();

        var document = documentRepository
                .findByPublicIdAndOwnerId(documentId, userId)
                .orElseThrow(DocumentNotFoundException::new);

        if(document.isContentNull())
            document.addContent(content.getContent());
        else
            document.addContent(
                    Utils.patchDocument(
                            document.getContent().getContent(), content.getContent()
                    )
            );

        document.addDocumentVersion(document, user);

        documentRepository.save(document);

        return document.getContent();
    }

    public Content getContent(UUID documentId) {
        var userId = Utils.getUserIdFromContext();
        var document = documentRepository
                .findByPublicIdAndOwnerId(documentId, userId)
                .orElseThrow(DocumentNotFoundException::new);

        return document.getContent();
    }

    public Boolean isAuthorizedUser(Long userId, UUID documentId) {
        var cache = cacheManager.getCache("USER_PERMISSION_CACHE");
        String cacheKey = documentId.toString() + ":" + userId;

        if (cache != null && Boolean.TRUE.equals(cache.get(cacheKey, Boolean.class)))
            return true;


        var document = documentRepository.findByPublicId(documentId)
                .orElseThrow(DocumentNotFoundException::new);

        boolean authorized = !document.isUnauthorizedUser(userId)
                && !document.isReadOnlyUser(userId);

        if (cache != null)
            cache.put(cacheKey, authorized);

        return authorized;
    }

    public void setDocumentStatus(UUID documentId, String status) {
        var cache = cacheManager.getCache("DOCUMENT_STATUS_CACHE");

        if (cache != null) {
            cache.put(documentId, status);
        }
    }

    public String getDocumentStatusFromCache(UUID documentId) {
        var cache = cacheManager.getCache("DOCUMENT_STATUS_CACHE");

        if(cache != null && cache.get(documentId) != null) {
            return cache.get(documentId, String.class);
        }

        var document = documentRepository.findByPublicId(documentId)
                .orElseThrow(DocumentNotFoundException::new);

        var content = document.getContent().getContent();

        if(cache != null && content != null) {
            cache.put(documentId, content);
            return content;
        }

        return null;
    }
}

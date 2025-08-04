package com.nipun.system.document;

import com.nipun.system.document.exceptions.DocumentNotFoundException;
import com.nipun.system.user.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@AllArgsConstructor
@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    public Document createDocument(Document document) {
        var userId = getUserIdFromContext();

        var user = userRepository.findById(userId).orElseThrow();

        documentRepository.save(
                        Document.createDocument(document, user));

        return document;
    }

    public Document getDocument(UUID documentId) {

        var userId = getUserIdFromContext();

        var document = documentRepository
                .findByPublicIdAndOwnerId(documentId, userId)
                .orElse(null);

        if(document == null)
            throw new DocumentNotFoundException();

        return document;
    }

    public Page<Document> getAllDocuments(int pageNumber, int size) {
        var userId = getUserIdFromContext();

        PageRequest pageRequest = PageRequest.of(pageNumber, size);

        return documentRepository.findAllByOwnerId(userId, pageRequest);
    }

    public Document updateTitle(UUID documentId, Document documentRequest) {

        var userId = getUserIdFromContext();

        var document = documentRepository
                .findByPublicIdAndOwnerId(documentId, userId)
                .orElse(null);

        if (document == null)
            throw new DocumentNotFoundException();

        document = document.updateTitle(documentRequest);

        documentRepository.save(document);

        return document;
    }

    public void deleteDocument(UUID documentId) {

        var userId = getUserIdFromContext();

        var document = documentRepository
                .findByPublicIdAndOwnerId(documentId, userId)
                .orElse(null);

        if (document == null)
            throw new DocumentNotFoundException();

        documentRepository.deleteById(document.getId());
    }

    public Content updateContent(UUID documentId, Content content) {
        var userId = getUserIdFromContext();

        var document = documentRepository
                .findByPublicIdAndOwnerId(documentId, userId)
                .orElse(null);

        if (document == null)
            throw new DocumentNotFoundException();

        document.getContent().setContent(content.getContent());

        documentRepository.save(document);

        return document.getContent();
    }

    public Content getContent(UUID documentId) {
        var userId = getUserIdFromContext();
        var document = documentRepository
                .findByPublicIdAndOwnerId(documentId, userId)
                .orElse(null);

        if (document == null)
            throw new DocumentNotFoundException();

        return document.getContent();
    }

    private Long getUserIdFromContext() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return (Long) authentication.getPrincipal();
    }
}

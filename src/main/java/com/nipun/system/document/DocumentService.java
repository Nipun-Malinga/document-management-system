package com.nipun.system.document;

import com.nipun.system.document.exceptions.DocumentNotFoundException;
import com.nipun.system.document.exceptions.NoSharedDocumentException;
import com.nipun.system.document.exceptions.ReadOnlyDocumentException;
import com.nipun.system.user.UserRepository;
import com.nipun.system.user.exceptions.UserNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final SharedDocumentRepository sharedDocumentRepository;

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

    public SharedDocument shareDocument(UUID documentId, Long sharedUserId, Permission permission) {
        var userId = getUserIdFromContext();

        var document = documentRepository.findByPublicIdAndOwnerId(documentId, userId).orElse(null);

        if(document == null)
            throw new DocumentNotFoundException();

        var sharedUser = userRepository.findById(sharedUserId).orElse(null);

        if(sharedUser == null)
            throw new UserNotFoundException();

        var sharedDocument = sharedDocumentRepository
                .findByDocumentIdAndSharedUserId(document.getId(), sharedUser.getId())
                .orElse(null);

        if(sharedDocument == null) {
            sharedDocument = new SharedDocument();
            sharedDocument.addSharingData(sharedUser, document, permission);

            document.addSharedDocument(sharedDocument);
        } else
            sharedDocument.setPermission(permission);

        document.setUpdatedAt(LocalDateTime.now());

        documentRepository.save(document);

        return sharedDocument;
    }

    public Page<Document> getAllSharedDocumentsWithUser(int pageNumber, int size) {
        var userId = getUserIdFromContext();

        PageRequest pageRequest = PageRequest.of(pageNumber, size);

        return documentRepository.findAllSharedDocumentsWithUser(userId, pageRequest);
    }

    public Content accessSharedDocument(UUID documentId) {
        var userId = getUserIdFromContext();

        var sharedDocument = sharedDocumentRepository
                .findByDocumentPublicIdAndSharedUserId(documentId, userId)
                .orElse(null);

        if(sharedDocument == null)
            throw new NoSharedDocumentException(
                    "No such document shard with userId: " + userId
            );

        return sharedDocument.getDocument().getContent();
    }

    public Content updateSharedDocument(UUID documentId, Content content) {
        var userId = getUserIdFromContext();

        var sharedDocument =  sharedDocumentRepository
                .findByDocumentPublicIdAndSharedUserId(documentId, userId)
                .orElse(null);

        if(sharedDocument == null)
            throw new NoSharedDocumentException(
                    "No such document shard with userId: " + userId
            );

        if(sharedDocument.getPermission().equals(Permission.READ_ONLY))
            throw new ReadOnlyDocumentException();

        var document = sharedDocument.getDocument();
        document.setContent(content);

        sharedDocumentRepository.save(sharedDocument);

        return document.getContent();
    }

    private Long getUserIdFromContext() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return (Long) authentication.getPrincipal();
    }
}

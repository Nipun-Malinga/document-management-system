package com.nipun.system.document;

import com.nipun.system.document.exceptions.DocumentNotFoundException;
import com.nipun.system.document.exceptions.DocumentVersionNotFoundException;
import com.nipun.system.document.exceptions.NoSharedDocumentException;
import com.nipun.system.document.exceptions.ReadOnlyDocumentException;
import com.nipun.system.user.User;
import com.nipun.system.user.UserRepository;
import com.nipun.system.user.exceptions.UserNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final SharedDocumentRepository sharedDocumentRepository;
    private final DocumentVersionRepository documentVersionRepository;

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

    @Transactional
    public Content updateContent(UUID documentId, Content content) {
        var userId = getUserIdFromContext();

        var user = userRepository.findById(userId).orElseThrow();

        var document = documentRepository
                .findByPublicIdAndOwnerId(documentId, userId)
                .orElse(null);

        if (document == null)
            throw new DocumentNotFoundException();

        var documentVersion = createVersion(document, user);
        document.addDocumentVersion(documentVersion);

        document.getContent().setContent(content.getContent());
        document.setUpdatedAt(LocalDateTime.now());

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

    @Transactional
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

        var user = userRepository.findById(userId).orElseThrow();

        var document = sharedDocument.getDocument();

        var documentVersion = createVersion(document, user);
        document.addDocumentVersion(documentVersion);

        document.setContent(content);

        sharedDocumentRepository.save(sharedDocument);

        return document.getContent();
    }

    public Page<DocumentVersion> getAllDocumentVersions(UUID documentId, int pageNumber, int size) {
        var userId = getUserIdFromContext();

        var document = documentRepository.findByPublicId(documentId).orElse(null);

        if(document == null)
            throw new DocumentNotFoundException();

        if(document.isUnauthorizedUser(userId))
            throw new NoSharedDocumentException(
                    "No such document own or shard with userId: " + userId
            );

        PageRequest pageRequest = PageRequest.of(pageNumber, size);

        return documentVersionRepository.findAllByDocumentId(document.getId(), pageRequest);
    }

    public DocumentVersionContent getVersionContent(UUID versionNumber) {
        var userId = getUserIdFromContext();

        var documentVersion = documentVersionRepository.findByVersionNumber(versionNumber).orElse(null);

        if(documentVersion == null)
            throw new DocumentVersionNotFoundException(
                    ""
            );

        var document = documentVersion.getDocument();

        if(document.isUnauthorizedUser(userId))
            throw new NoSharedDocumentException(
                    "No such document own or shard with userId: " + userId
            );

        return documentVersion.getContent();
    }

    private Long getUserIdFromContext() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return (Long) authentication.getPrincipal();
    }

    private DocumentVersion createVersion(Document document, User user) {
        var versionContent = new DocumentVersionContent();

        if(document.getContent().getContent() != null)
            versionContent.setContent(document.getContent().getContent());

        var version = new DocumentVersion();
        version.addData(document, user, versionContent);

        return version;
    }
}

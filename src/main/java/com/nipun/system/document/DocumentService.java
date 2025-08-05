package com.nipun.system.document;

import com.nipun.system.document.exceptions.DocumentNotFoundException;
import com.nipun.system.document.exceptions.DocumentVersionNotFoundException;
import com.nipun.system.document.exceptions.NoSharedDocumentException;
import com.nipun.system.document.exceptions.ReadOnlyDocumentException;
import com.nipun.system.document.share.Permission;
import com.nipun.system.document.share.SharedDocument;
import com.nipun.system.document.share.SharedDocumentRepository;
import com.nipun.system.document.version.DocumentVersion;
import com.nipun.system.document.version.DocumentVersionContent;
import com.nipun.system.document.version.DocumentVersionRepository;
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

        documentRepository.save(Document.createDocument(document, user));

        return document;
    }

    public Document getDocument(UUID documentId) {

        var userId = getUserIdFromContext();

        return documentRepository
                .findByPublicIdAndOwnerId(documentId, userId)
                .orElseThrow(DocumentNotFoundException::new);
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
                .orElseThrow(DocumentNotFoundException::new);

        document = document.updateTitle(documentRequest);

        documentRepository.save(document);

        return document;
    }

    public void deleteDocument(UUID documentId) {

        var userId = getUserIdFromContext();

        var document = documentRepository
                .findByPublicIdAndOwnerId(documentId, userId)
                .orElseThrow(DocumentNotFoundException::new);

        documentRepository.deleteById(document.getId());
    }

    @Transactional
    public Content updateContent(UUID documentId, Content content) {
        var userId = getUserIdFromContext();

        var user = userRepository.findById(userId).orElseThrow();

        var document = documentRepository
                .findByPublicIdAndOwnerId(documentId, userId)
                .orElseThrow(DocumentNotFoundException::new);

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
                .orElseThrow(DocumentNotFoundException::new);

        return document.getContent();
    }

    public SharedDocument shareDocument(UUID documentId, Long sharedUserId, Permission permission) {
        var userId = getUserIdFromContext();

        var document = documentRepository
                .findByPublicIdAndOwnerId(documentId, userId)
                .orElseThrow(DocumentNotFoundException::new);

        var sharedUser = userRepository
                .findById(sharedUserId)
                .orElseThrow(UserNotFoundException::new);

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
                .orElseThrow(NoSharedDocumentException::new);

        return sharedDocument.getDocument().getContent();
    }

    @Transactional
    public Content updateSharedDocument(UUID documentId, Content content) {
        var userId = getUserIdFromContext();

        var sharedDocument =  sharedDocumentRepository
                .findByDocumentPublicIdAndSharedUserId(documentId, userId)
                .orElseThrow(NoSharedDocumentException::new);

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

        var document = documentRepository
                .findByPublicId(documentId)
                .orElseThrow(DocumentNotFoundException::new);

        if(document.isUnauthorizedUser(userId))
            throw new NoSharedDocumentException();

        PageRequest pageRequest = PageRequest.of(pageNumber, size);

        return documentVersionRepository.findAllByDocumentId(document.getId(), pageRequest);
    }

    public DocumentVersionContent getVersionContent(UUID versionNumber) {
        var userId = getUserIdFromContext();

        var documentVersion = documentVersionRepository
                .findByVersionNumber(versionNumber)
                .orElseThrow(DocumentVersionNotFoundException::new);

        var document = documentVersion.getDocument();

        if(document.isUnauthorizedUser(userId))
            throw new NoSharedDocumentException();

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

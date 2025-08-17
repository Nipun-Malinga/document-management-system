package com.nipun.system.document.share;

import com.nipun.system.document.Content;
import com.nipun.system.document.Document;
import com.nipun.system.document.DocumentRepository;
import com.nipun.system.document.common.Utils;
import com.nipun.system.document.exceptions.DocumentNotFoundException;
import com.nipun.system.document.exceptions.NoSharedDocumentException;
import com.nipun.system.document.exceptions.ReadOnlyDocumentException;
import com.nipun.system.user.UserRepository;
import com.nipun.system.user.exceptions.UserNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@Service
public class SharedDocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final SharedDocumentRepository sharedDocumentRepository;

    public SharedDocument shareDocument(Long sharedUserId, UUID documentId, Permission permission) {
        var userId = Utils.getUserIdFromContext();

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
        var userId = Utils.getUserIdFromContext();

        PageRequest pageRequest = PageRequest.of(pageNumber, size);

        return documentRepository.findAllSharedDocumentsWithUser(userId, pageRequest);
    }

    public Content accessSharedDocument(UUID documentId) {
        var userId = Utils.getUserIdFromContext();

        var sharedDocument = sharedDocumentRepository
                .findByDocumentPublicId(documentId)
                .orElseThrow(NoSharedDocumentException::new);

        var document = sharedDocument.getDocument();

        if(document.isUnauthorizedUser(userId))
            throw new NoSharedDocumentException();

        return document.getContent();
    }

    @Transactional
    public Content updateSharedDocument(UUID documentId, Content content) {
        var userId = Utils.getUserIdFromContext();

        var sharedDocument =  sharedDocumentRepository
                .findByDocumentPublicIdAndSharedUserId(documentId, userId)
                .orElseThrow(NoSharedDocumentException::new);

        if(sharedDocument.getPermission().equals(Permission.READ_ONLY))
            throw new ReadOnlyDocumentException();

        var user = userRepository.findById(userId).orElseThrow();

        var document = sharedDocument.getDocument();

        document.setContent(content);

        sharedDocumentRepository.save(sharedDocument);

        var documentVersion = Utils.createVersion(document, user);
        document.addDocumentVersion(documentVersion);

        return document.getContent();
    }
}

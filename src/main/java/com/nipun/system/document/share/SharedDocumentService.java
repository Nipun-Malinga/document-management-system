package com.nipun.system.document.share;

import com.github.difflib.patch.PatchFailedException;
import com.nipun.system.document.Content;
import com.nipun.system.document.Document;
import com.nipun.system.document.DocumentRepository;
import com.nipun.system.document.utils.Utils;
import com.nipun.system.document.exceptions.DocumentNotFoundException;
import com.nipun.system.document.exceptions.ReadOnlyDocumentException;
import com.nipun.system.document.exceptions.UnauthorizedDocumentException;
import com.nipun.system.document.websocket.AuthorizedOptions;
import com.nipun.system.document.websocket.DocumentWebSocketService;
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
    private final DocumentWebSocketService documentWebSocketService;

    @Transactional
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

        documentWebSocketService.updateDocumentPermissionDetails(
                        documentId,
                        sharedUserId,
                        new AuthorizedOptions(
                                document.isUnauthorizedUser(sharedUserId),
                                document.isReadOnlyUser(sharedUserId))
        );

        return sharedDocument;
    }

    public Page<Document> getAllSharedDocumentsWithUser(int pageNumber, int size) {
        var userId = Utils.getUserIdFromContext();

        PageRequest pageRequest = PageRequest.of(pageNumber, size);

        return documentRepository.findAllSharedDocumentsWithUser(userId, pageRequest);
    }

    public Content accessSharedDocument(UUID documentId) {
        var userId = Utils.getUserIdFromContext();

        var document = documentRepository.findByPublicId(documentId)
                .orElseThrow(UnauthorizedDocumentException::new);

        if(document.isUnauthorizedUser(userId))
            throw new UnauthorizedDocumentException();

        return document.getContent();
    }

    @Transactional
    public Content updateSharedDocument(UUID documentId, Content content) throws PatchFailedException {
        var userId = Utils.getUserIdFromContext();

        var sharedDocument =  sharedDocumentRepository
                .findByDocumentPublicIdAndSharedUserId(documentId, userId)
                .orElseThrow(UnauthorizedDocumentException::new);

        if(sharedDocument.getPermission().equals(Permission.READ_ONLY))
            throw new ReadOnlyDocumentException();

        var user = userRepository.findById(userId).orElseThrow();

        var document = sharedDocument.getDocument();

        if(document.isContentNull())
            document.addContent(content.getContent());
        else
            document.addContent(
                    Utils.patchDocument(
                            document.getContent().getContent(), content.getContent()
                    )
            );

        document.addDocumentVersion(document, user);

        sharedDocumentRepository.save(sharedDocument);

        return document.getContent();
    }


    public void removeDocumentAccess(UUID documentId) {
        var userId = Utils.getUserIdFromContext();

        var sharedDocument = sharedDocumentRepository
                .findByDocumentPublicIdAndSharedUserId(documentId, userId)
                .orElseThrow(UnauthorizedDocumentException::new);

        documentWebSocketService.removeDocumentPermissionDetailsFromCache(documentId, userId);

        sharedDocumentRepository.delete(sharedDocument);
    }

    public void removeDocumentAccess(UUID documentId, Long sharedUerId) {
        var userId = Utils.getUserIdFromContext();

        var document = documentRepository
                .findByPublicIdAndOwnerId(documentId, userId)
                .orElseThrow(DocumentNotFoundException::new);

        document.removeSharedUser(sharedUerId);

        documentWebSocketService.removeDocumentPermissionDetailsFromCache(documentId, sharedUerId);

        documentRepository.save(document);
    }
}

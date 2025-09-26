package com.nipun.system.document.share;

import com.nipun.system.document.ContentMapper;
import com.nipun.system.document.DocumentMapper;
import com.nipun.system.document.DocumentRepository;
import com.nipun.system.document.diff.DiffService;
import com.nipun.system.document.dtos.ContentDto;
import com.nipun.system.document.dtos.UpdateContentRequest;
import com.nipun.system.document.dtos.common.PaginatedData;
import com.nipun.system.document.dtos.share.SharedDocumentDto;
import com.nipun.system.document.exceptions.DocumentNotFoundException;
import com.nipun.system.document.exceptions.UnauthorizedDocumentException;
import com.nipun.system.document.version.DocumentVersionFactory;
import com.nipun.system.document.websocket.AuthorizedOptions;
import com.nipun.system.document.websocket.DocumentWebSocketServiceImpl;
import com.nipun.system.shared.utils.UserIdUtils;
import com.nipun.system.user.UserRepository;
import com.nipun.system.user.exceptions.UserNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@Service
public class SharedDocumentServiceImpl implements SharedDocumentService{

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final SharedDocumentRepository sharedDocumentRepository;

    private final DocumentWebSocketServiceImpl documentWebSocketService;
    private final DiffService diffService;
    private final SharedDocumentAuthService sharedDocumentAuthService;

    private final DocumentMapper documentMapper;
    private final ContentMapper contentMapper;
    private final SharedDocumentMapper sharedDocumentMapper;

    private final DocumentVersionFactory versionFactory;

    @Transactional
    @Override
    public SharedDocumentDto shareDocument(Long sharedUserId, UUID documentId, Permission permission) {
        var userId = UserIdUtils.getUserIdFromContext();

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
                                sharedDocumentAuthService.isUnauthorizedUser(sharedUserId, document),
                                sharedDocumentAuthService.isReadOnlyUser(sharedUserId, document))
        );

        return sharedDocumentMapper.toSharedDocumentDto(sharedDocument);
    }

    @Override
    public PaginatedData getAllSharedDocumentsWithUser(int pageNumber, int size) {
        var userId = UserIdUtils.getUserIdFromContext();

        PageRequest pageRequest = PageRequest.of(pageNumber, size);

        var documentPage =  documentRepository.findAllSharedDocumentsWithUser(userId, pageRequest);

        var documentDtoList = documentPage
                .getContent()
                .stream()
                .map(documentMapper::toDto)
                .toList();

        return new PaginatedData(
                        documentDtoList,
                        pageNumber,
                        size,
                        documentPage.getTotalPages(),
                        documentPage.getTotalElements(),
                        documentPage.hasNext(),
                        documentPage.hasPrevious()
                );
    }

    @Cacheable(value = "shared_document_content", key = "#documentId")
    @Override
    public ContentDto accessSharedDocument(UUID documentId) {
        var userId = UserIdUtils.getUserIdFromContext();

        var document = documentRepository.findByPublicId(documentId)
                .orElseThrow(UnauthorizedDocumentException::new);

        if(sharedDocumentAuthService.isUnauthorizedUser(userId, document))
            throw new UnauthorizedDocumentException();

        return contentMapper.toDto(document.getContent());
    }

    @CachePut(value = "shared_document_content", key = "#documentId")
    @Override
    public ContentDto updateSharedDocument(UUID documentId, UpdateContentRequest request) {
        var userId = UserIdUtils.getUserIdFromContext();

        var document =  documentRepository.findByPublicIdAndOwnerId(documentId, userId)
                .orElseThrow(UnauthorizedDocumentException::new);

        sharedDocumentAuthService.checkUserCanWrite(userId, document);

        var user = userRepository.findById(userId).orElseThrow();

        if(document.getDocumentContent() == null)
            document.addContent(request.getContent());
        else
            document.addContent(diffService.patchDocument(document.getDocumentContent(), request.getContent()));

        var version = versionFactory.createNewVersion(document, user);

        document.addDocumentVersion(version);

        documentRepository.save(document);

        return contentMapper.toDto(document.getContent());
    }


    @Override
    public void removeDocumentAccess(UUID documentId) {
        var userId = UserIdUtils.getUserIdFromContext();

        var sharedDocument = sharedDocumentRepository
                .findByDocumentPublicIdAndSharedUserId(documentId, userId)
                .orElseThrow(UnauthorizedDocumentException::new);

        documentWebSocketService.removeDocumentPermissionDetailsFromCache(documentId, userId);

        sharedDocumentRepository.delete(sharedDocument);
    }

    @Override
    public void removeDocumentAccess(UUID documentId, Long sharedUerId) {
        var userId = UserIdUtils.getUserIdFromContext();

        var document = documentRepository
                .findByPublicIdAndOwnerId(documentId, userId)
                .orElseThrow(DocumentNotFoundException::new);

        document.removeSharedUser(sharedUerId);

        documentWebSocketService.removeDocumentPermissionDetailsFromCache(documentId, sharedUerId);

        documentRepository.save(document);
    }
}

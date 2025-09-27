package com.nipun.system.document.share;

import com.nipun.system.document.base.DocumentMapper;
import com.nipun.system.document.base.DocumentRepository;
import com.nipun.system.document.share.dtos.SharedDocumentResponse;
import com.nipun.system.document.base.exceptions.DocumentNotFoundException;
import com.nipun.system.document.share.exceptions.UnauthorizedDocumentException;
import com.nipun.system.document.websocket.authentication.AuthenticationService;
import com.nipun.system.document.websocket.authentication.AuthorizedOptions;
import com.nipun.system.shared.dtos.PaginatedData;
import com.nipun.system.shared.utils.UserIdUtils;
import com.nipun.system.user.UserRepository;
import com.nipun.system.user.exceptions.UserNotFoundException;
import lombok.AllArgsConstructor;
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

    private final AuthenticationService authenticationService;
    private final SharedDocumentAuthService sharedDocumentAuthService;

    private final DocumentMapper documentMapper;
    private final SharedDocumentMapper sharedDocumentMapper;

    private final SharedDocumentFactory sharedDocumentFactory;

    @Transactional
    @Override
    public SharedDocumentResponse shareDocument(Long sharedUserId, UUID documentId, Permission permission) {
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
            sharedDocument = sharedDocumentFactory.createNewSharedDocument(sharedUser, document);
            document.addSharedDocument(sharedDocument);
        }

        sharedDocument.setPermission(permission);

        document.setUpdatedAt(LocalDateTime.now());

        documentRepository.save(document);

        authenticationService.updateDocumentPermissionDetails(
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

    @Override
    public void removeDocumentAccess(UUID documentId) {
        var userId = UserIdUtils.getUserIdFromContext();

        var sharedDocument = sharedDocumentRepository
                .findByDocumentPublicIdAndSharedUserId(documentId, userId)
                .orElseThrow(UnauthorizedDocumentException::new);

        authenticationService.removeDocumentPermissionDetailsFromCache(documentId, userId);

        sharedDocumentRepository.delete(sharedDocument);
    }

    @Override
    public void removeDocumentAccess(UUID documentId, Long sharedUerId) {
        var userId = UserIdUtils.getUserIdFromContext();

        var document = documentRepository
                .findByPublicIdAndOwnerId(documentId, userId)
                .orElseThrow(DocumentNotFoundException::new);

        document.removeSharedUser(sharedUerId);

        authenticationService.removeDocumentPermissionDetailsFromCache(documentId, sharedUerId);

        documentRepository.save(document);
    }
}

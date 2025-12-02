package com.nipun.system.document.share;

import com.nipun.system.document.base.DocumentMapper;
import com.nipun.system.document.base.DocumentRepository;
import com.nipun.system.document.base.exceptions.DocumentNotFoundException;
import com.nipun.system.document.permission.PermissionUtils;
import com.nipun.system.document.permission.exceptions.UnauthorizedDocumentException;
import com.nipun.system.document.share.dtos.ShareDocumentRequest;
import com.nipun.system.document.share.dtos.SharedDocumentResponse;
import com.nipun.system.shared.dtos.CountResponse;
import com.nipun.system.shared.dtos.PaginatedData;
import com.nipun.system.shared.utils.UserIdUtils;
import com.nipun.system.user.UserRepository;
import com.nipun.system.user.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class SharedDocumentService {
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final SharedDocumentRepository sharedDocumentRepository;
    private final DocumentMapper documentMapper;
    private final SharedDocumentMapper sharedDocumentMapper;
    private final SharedDocumentFactory sharedDocumentFactory;

    @Caching(evict = {
            @CacheEvict(value = "documents", key = "{#documentId}"),
            @CacheEvict(value = "sharedUsers", key = "{#documentId}")
    })
    @Transactional
    public SharedDocumentResponse shareDocument(UUID documentId, ShareDocumentRequest request) {
        var userId = UserIdUtils.getUserIdFromContext();

        var document = documentRepository
                .findByPublicIdAndOwnerId(documentId, userId)
                .orElseThrow(DocumentNotFoundException::new);

        var sharedUser = userRepository
                .findById(request.getUserId())
                .orElseThrow(UserNotFoundException::new);

        var sharedDocument = sharedDocumentRepository
                .findByDocumentIdAndSharedUserId(document.getId(), sharedUser.getId())
                .orElse(null);

        if (sharedDocument == null) {
            sharedDocument = sharedDocumentFactory.createNewSharedDocument(sharedUser, document);
            document.addSharedDocument(sharedDocument);
        }

        sharedDocument.setPermission(request.getPermission());

        document.setUpdatedAt(LocalDateTime.now());

        documentRepository.save(document);

        return sharedDocumentMapper.toSharedDocumentDto(sharedDocument);
    }

    @Cacheable(value = "sharedUsers", key = "{#documentId}")
    public List<SharedDocumentResponse> getAllSharedUsers(UUID documentId) {
        var userId = UserIdUtils.getUserIdFromContext();

        var document = documentRepository.findByPublicId(documentId).orElseThrow(DocumentNotFoundException::new);

        if (PermissionUtils.isUnauthorizedUser(userId, document))
            throw new UnauthorizedDocumentException();

        return document.getSharedUsers().stream()
                .map(sharedDocumentMapper::toSharedDocumentDto)
                .toList();
    }

    public CountResponse getSharedDocumentWithUserCount() {
        var userId = UserIdUtils.getUserIdFromContext();
        return new CountResponse(sharedDocumentRepository.countAllBySharedUserId(userId));
    }

    public PaginatedData getAllSharedDocumentsWithUser(int pageNumber, int size) {
        var userId = UserIdUtils.getUserIdFromContext();

        PageRequest pageRequest = PageRequest.of(pageNumber, size);

        var documentPage = documentRepository.findAllSharedDocumentsWithUser(userId, pageRequest);

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

    @CacheEvict(value = "sharedUsers", key = "{#documentId}")
    public void removeDocumentAccess(UUID documentId) {
        var userId = UserIdUtils.getUserIdFromContext();

        var sharedDocument = sharedDocumentRepository
                .findByDocumentPublicIdAndSharedUserId(documentId, userId)
                .orElseThrow(UnauthorizedDocumentException::new);

        sharedDocumentRepository.delete(sharedDocument);
    }

    @CacheEvict(value = "sharedUsers", key = "{#documentId}")
    public void removeDocumentAccess(UUID documentId, Long sharedUserId) {
        var userId = UserIdUtils.getUserIdFromContext();

        var document = documentRepository
                .findByPublicIdAndOwnerId(documentId, userId)
                .orElseThrow(DocumentNotFoundException::new);

        document.removeSharedUser(sharedUserId);

        documentRepository.save(document);
    }
}

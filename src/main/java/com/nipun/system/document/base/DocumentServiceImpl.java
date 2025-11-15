package com.nipun.system.document.base;

import com.nipun.system.document.base.dtos.CreateDocumentRequest;
import com.nipun.system.document.base.dtos.DocumentResponse;
import com.nipun.system.document.base.dtos.UpdateTitleRequest;
import com.nipun.system.document.base.exceptions.DocumentNotFoundException;
import com.nipun.system.document.permission.PermissionUtils;
import com.nipun.system.document.permission.exceptions.UnauthorizedDocumentException;
import com.nipun.system.shared.dtos.CountResponse;
import com.nipun.system.shared.dtos.PaginatedData;
import com.nipun.system.shared.utils.UserIdUtils;
import com.nipun.system.user.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@AllArgsConstructor
@Service
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    private final DocumentMapper documentMapper;

    @Transactional
    @Override
    public DocumentResponse createDocument(
            CreateDocumentRequest request
    ) {
        var userId = UserIdUtils.getUserIdFromContext();

        var user = userRepository.findById(userId).orElseThrow();

        var document = DocumentFactory
                .createNewDocument(user, request.getTitle(), request.getStatus());

        document = documentRepository.save(document);

        return documentMapper.toDto(document);
    }

    @Cacheable(value = "documents", key = "{#documentId}")
    @Transactional(readOnly = true)
    @Override
    public DocumentResponse getDocument(UUID documentId) {

        var userId = UserIdUtils.getUserIdFromContext();

        var document = documentRepository.findByPublicId(documentId)
                .orElseThrow(DocumentNotFoundException::new);

        if (PermissionUtils.isUnauthorizedUser(userId, document))
            throw new UnauthorizedDocumentException();

        return documentMapper.toDto(document);
    }

    @Override
    public CountResponse getDocumentCount() {
        var userId = UserIdUtils.getUserIdFromContext();
        return new CountResponse(documentRepository.countAllByOwnerIdAndTrashedIsFalse(userId));
    }

    @Transactional(readOnly = true)
    @Override
    public PaginatedData getAllDocuments(int pageNumber, int size) {
        var userId = UserIdUtils.getUserIdFromContext();

        PageRequest pageRequest = PageRequest.of(pageNumber, size);

        var documents = documentRepository.findAllByOwnerId(userId, pageRequest);

        var documentDtoList = documents
                .getContent()
                .stream()
                .map(documentMapper::toDto)
                .toList();

        return new PaginatedData(
                documentDtoList,
                pageNumber,
                size,
                documents.getTotalPages(),
                documents.getTotalElements(),
                documents.hasNext(),
                documents.hasPrevious()
        );
    }

    @CachePut(value = "documents", key = "{#documentId}")
    @Transactional
    @Override
    public DocumentResponse updateTitle(UUID documentId, UpdateTitleRequest request) {

        var userId = UserIdUtils.getUserIdFromContext();

        var document = documentRepository
                .findByPublicIdAndOwnerId(documentId, userId)
                .orElseThrow(DocumentNotFoundException::new);

        document.setTitle(request.getTitle());

        return documentMapper.toDto(documentRepository.save(document));
    }

    @Override
    public DocumentResponse toggleFavorite(UUID documentId) {
        var userId = UserIdUtils.getUserIdFromContext();

        var document = documentRepository.findByPublicIdAndOwnerId(documentId, userId).orElseThrow(DocumentNotFoundException::new);

        document.setFavorite(!document.getFavorite());

        return documentMapper.toDto(documentRepository.save(document));
    }

    @Override
    public CountResponse getDocumentFavoriteCount() {
        var userId = UserIdUtils.getUserIdFromContext();
        return new CountResponse(documentRepository.countAllFavoriteDocumentByUser(userId));
    }
}

package com.nipun.system.document;

import com.nipun.system.document.diff.DiffService;
import com.nipun.system.document.dtos.*;
import com.nipun.system.document.dtos.common.PaginatedData;
import com.nipun.system.document.exceptions.DocumentNotFoundException;
import com.nipun.system.document.exceptions.UnauthorizedDocumentException;
import com.nipun.system.document.share.SharedDocumentAuthService;
import com.nipun.system.document.version.DocumentVersionFactory;
import com.nipun.system.shared.utils.UserIdUtils;
import com.nipun.system.user.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@AllArgsConstructor
@Service
public class DocumentServiceImpl implements DocumentService{

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    private final DocumentMapper documentMapper;
    private final ContentMapper contentMapper;

    private final DiffService diffService;

    private final DocumentVersionFactory versionFactory;
    private final SharedDocumentAuthService sharedDocumentAuthService;

    @Transactional
    @Override
    public DocumentDto createDocument(
            CreateDocumentRequest request
    ) {
        var userId = UserIdUtils.getUserIdFromContext();

        var user = userRepository.findById(userId).orElseThrow();

        var document = DocumentFactory.createNewDocument(user, request.getTitle());

        document = documentRepository.save(document);

        return documentMapper.toDto(document);
    }

    @Cacheable(value = "documents", key = "#documentId")
    @Override
    public DocumentDto getDocument(UUID documentId) {

        var userId = UserIdUtils.getUserIdFromContext();

        var document = documentRepository.findByPublicId(documentId)
                .orElseThrow(DocumentNotFoundException::new);

        if (sharedDocumentAuthService.isUnauthorizedUser(userId, document))
            throw new UnauthorizedDocumentException();

        return documentMapper.toDto(document);
    }

    @Override
    public PaginatedData getAllDocuments(int pageNumber, int size) {
        var userId = UserIdUtils.getUserIdFromContext();

        PageRequest pageRequest = PageRequest.of(pageNumber, size);

        var documents =  documentRepository.findAllByOwnerId(userId, pageRequest);

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

    @CachePut(value = "documents", key = "#documentId")
    @Transactional
    @Override
    public DocumentDto updateTitle(UUID documentId, UpdateTitleRequest request) {

        var userId = UserIdUtils.getUserIdFromContext();

        var document = documentRepository
                .findByPublicIdAndOwnerId(documentId, userId)
                .orElseThrow(DocumentNotFoundException::new);

        document = document.updateTitle(request.getTitle());

        return documentMapper.toDto(documentRepository.save(document));
    }

    @CacheEvict(value = "documents", key = "#documentId")
    @Transactional
    @Override
    public void deleteDocument(UUID documentId) {

        var userId = UserIdUtils.getUserIdFromContext();

        var document = documentRepository
                .findByPublicIdAndOwnerId(documentId, userId)
                .orElseThrow(DocumentNotFoundException::new);

        documentRepository.delete(document);
    }

    @Cacheable(value = "document_contents", key = "#documentId")
    @Override
    public ContentDto getContent(UUID documentId) {
        var userId = UserIdUtils.getUserIdFromContext();

        var document = documentRepository.findByPublicId(documentId)
                .orElseThrow(DocumentNotFoundException::new);

        if (sharedDocumentAuthService.isUnauthorizedUser(userId, document))
            throw new UnauthorizedDocumentException();

        return contentMapper.toDto(document.getContent());
    }

    @CachePut(value = "document_contents", key = "#documentId")
    @Transactional
    @Override
    public ContentDto updateContent(UUID documentId, UpdateContentRequest request) {
        var userId = UserIdUtils.getUserIdFromContext();

        var user = userRepository.findById(userId).orElseThrow();

        var document = documentRepository.findByPublicId(documentId)
                .orElseThrow(DocumentNotFoundException::new);

        sharedDocumentAuthService.checkUserCanWrite(userId, document);

        if(document.getDocumentContent() == null)
            document.addContent(request.getContent());
        else
            document.addContent(diffService.patchDocument(document.getDocumentContent(), request.getContent()));

        var version = versionFactory.createNewVersion(document, user);

        document.addDocumentVersion(version);

        documentRepository.save(document);

        return contentMapper.toDto(document.getContent());
    }
}

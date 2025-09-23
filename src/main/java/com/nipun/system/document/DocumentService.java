package com.nipun.system.document;

import com.nipun.system.document.diff.DiffService;
import com.nipun.system.document.dtos.*;
import com.nipun.system.document.dtos.common.PaginatedData;
import com.nipun.system.document.exceptions.DocumentNotFoundException;
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
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final DocumentMapper documentMapper;
    private final ContentMapper contentMapper;
    private final DiffService diffService;

    @Transactional
    public DocumentDto createDocument(
            CreateDocumentRequest request
    ) {
        var document = documentMapper.toEntity(request);

        var userId = UserIdUtils.getUserIdFromContext();

        var user = userRepository.findById(userId).orElseThrow();

        documentRepository.save(Document.createDocument(document, user));

        return documentMapper.toDto(document);
    }

    @Cacheable(value = "documents", key = "#documentId")
    public DocumentDto getDocument(UUID documentId) {

        var userId = UserIdUtils.getUserIdFromContext();

        var document = documentRepository
                .findByPublicIdAndOwnerId(documentId, userId)
                .orElseThrow(DocumentNotFoundException::new);

        return documentMapper.toDto(document);
    }

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
    public void deleteDocument(UUID documentId) {

        var userId = UserIdUtils.getUserIdFromContext();

        var document = documentRepository
                .findByPublicIdAndOwnerId(documentId, userId)
                .orElseThrow(DocumentNotFoundException::new);

        documentRepository.delete(document);
    }

    @CachePut(value = "document_contents", key = "#documentId")
    @Transactional
    public ContentDto updateContent(UUID documentId, UpdateContentRequest request) {
        var userId = UserIdUtils.getUserIdFromContext();

        var user = userRepository.findById(userId).orElseThrow();

        var document = documentRepository
                .findByPublicIdAndOwnerId(documentId, userId)
                .orElseThrow(DocumentNotFoundException::new);

        if(document.isContentNull())
            document.addContent(request.getContent());
        else
            document.addContent(diffService.patchDocument(document.getContent().getContent(), request.getContent()));

        document.addDocumentVersion(user);

        documentRepository.save(document);

        return contentMapper.toDto(document.getContent());
    }

    @Cacheable(value = "document_contents", key = "#documentId")
    public ContentDto getContent(UUID documentId) {
        var userId = UserIdUtils.getUserIdFromContext();
        var document = documentRepository
                .findByPublicIdAndOwnerId(documentId, userId)
                .orElseThrow(DocumentNotFoundException::new);

        return contentMapper.toDto(document.getContent());
    }
}

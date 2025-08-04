package com.nipun.system.document;

import com.nipun.system.document.dtos.*;
import com.nipun.system.document.exceptions.DocumentNotFoundException;
import com.nipun.system.document.exceptions.NoSharedDocumentException;
import com.nipun.system.shared.dtos.ErrorResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping("/documents")
public class DocumentController {

    private final DocumentService documentService;
    private final DocumentMapper documentMapper;
    private final ContentMapper contentMapper;

    @PostMapping
    public ResponseEntity<DocumentDto> createDocument(
            @RequestBody @Valid CreateDocumentRequest request,
            UriComponentsBuilder uriBuilder
    ) {
        var document = documentService.createDocument(
                documentMapper.toEntity(request)
        );

        var uri = uriBuilder.path("/documents/{documentId}")
                .buildAndExpand(document.getPublicId()).toUri();

        return ResponseEntity
                .created(uri)
                .body(documentMapper.toDto(document));
    }

    @GetMapping("/{documentId}")
    public ResponseEntity<DocumentDto> getDocument(
            @PathVariable(name = "documentId") UUID documentId
    ) {
        var document = documentService.getDocument(documentId);
        return ResponseEntity.ok(documentMapper.toDto(document));
    }

    @GetMapping
    public ResponseEntity<Documents> getAllDocuments(
            @RequestParam(name = "page-number", defaultValue = "0") int pageNumber,
            @RequestParam(name = "page-size", defaultValue = "20") int pageSize
    ) {
        var documents = documentService.getAllDocuments(pageNumber, pageSize);

        var documentDtos = documents
                .getContent()
                .stream()
                .map(documentMapper::toDto)
                .toList();
        return ResponseEntity.ok(
                new Documents(
                        documentDtos,
                        pageNumber,
                        pageSize,
                        documents.getTotalPages(),
                        documents.getTotalElements(),
                        documents.hasNext(),
                        documents.hasPrevious()
                ));
    }

    @PatchMapping("/{documentId}")
    public ResponseEntity<DocumentDto> updateDocumentTitle(
            @PathVariable(name = "documentId") UUID documentId,
            @RequestBody @Valid UpdateTitleRequest request
    ) {
        var document = documentService
                .updateTitle(documentId, documentMapper.toEntity(request));

        return ResponseEntity.ok(documentMapper.toDto(document));
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable(name = "documentId") UUID documentId
    ) {
        documentService.deleteDocument(documentId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{documentId}/content")
    public ResponseEntity<ContentDto> updateDocumentContent(
            @PathVariable UUID documentId,
            @RequestBody @Valid UpdateContentRequest request
    ) {
        System.out.println(request.getContent());
        var content = documentService.updateContent(documentId, contentMapper.toEntity(request));

        return ResponseEntity.ok(contentMapper.toDto(content));
    }

    @GetMapping("/{documentId}/content")
    public ResponseEntity<ContentDto> getDocumentContent(
        @PathVariable(name = "documentId") UUID documentId
    ) {
        var document = documentService.getContent(documentId);
        return ResponseEntity.ok(contentMapper.toDto(document));
    }

    @PostMapping("/share")
    public ResponseEntity<SharedDocumentDto> shareDocument(
            @RequestBody @Valid ShareDocumentRequest request
    ) {
        var sharedDocument = documentService
                .shareDocument(
                        request.getDocumentId(),
                        request.getShareUserId(),
                        request.getPermission()
                );
        return ResponseEntity.ok(documentMapper.toSharedDocumentDto(sharedDocument));
    }

    @GetMapping("/share")
    public ResponseEntity<Documents> getAllSharedDocumentsWithUser(
            @RequestParam(name = "page-number", defaultValue = "0") int pageNumber,
            @RequestParam(name = "page-size", defaultValue = "20") int pageSize
    ) {

        var documents = documentService.getAllSharedDocumentsWithUser(pageNumber, pageSize);

        var documentDtos = documents
                .getContent()
                .stream()
                .map(documentMapper::toDto)
                .toList();
        return ResponseEntity.ok(
                new Documents(
                        documentDtos,
                        pageNumber,
                        pageSize,
                        documents.getTotalPages(),
                        documents.getTotalElements(),
                        documents.hasNext(),
                        documents.hasPrevious()
                ));
    }

    @GetMapping("/share/access")
    public ResponseEntity<ContentDto> accessSharedDocument(
            @RequestBody @Valid AccessSharedDocumentRequest request
    ) {
        return ResponseEntity
                .ok(contentMapper.toDto(
                        documentService.accessSharedDocument(request.getDocumentId())
                ));
    }

    @ExceptionHandler(DocumentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDocumentNotFoundException(
            DocumentNotFoundException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(NoSharedDocumentException.class)
    public ResponseEntity<ErrorResponse> handleNoSharedDocumentException(
            NoSharedDocumentException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(exception.getMessage()));
    }
}

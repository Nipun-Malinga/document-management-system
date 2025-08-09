package com.nipun.system.document;

import com.nipun.system.document.dtos.*;
import com.nipun.system.document.dtos.common.PaginatedData;
import com.nipun.system.document.dtos.share.ShareDocumentRequest;
import com.nipun.system.document.dtos.share.SharedDocumentDto;
import com.nipun.system.document.dtos.version.DiffResponse;
import com.nipun.system.document.exceptions.DocumentNotFoundException;
import com.nipun.system.document.exceptions.DocumentVersionNotFoundException;
import com.nipun.system.document.exceptions.NoSharedDocumentException;
import com.nipun.system.document.exceptions.ReadOnlyDocumentException;
import com.nipun.system.document.version.DocumentVersionMapper;
import com.nipun.system.document.version.SharedDocumentMapper;
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
    private final DocumentVersionMapper documentVersionMapper;
    private final SharedDocumentMapper sharedDocumentMapper;

    @PostMapping
    public ResponseEntity<DocumentDto> createDocument(
            @RequestBody @Valid CreateDocumentRequest request,
            UriComponentsBuilder uriBuilder
    ) {
        var document = documentService.createDocument(
                documentMapper.toEntity(request)
        );

        var uri = uriBuilder.path("/documents/{id}")
                .buildAndExpand(document.getPublicId()).toUri();

        return ResponseEntity
                .created(uri)
                .body(documentMapper.toDto(document));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentDto> getDocument(
            @PathVariable(name = "id") UUID documentId
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

    @PutMapping("/{id}/title")
    public ResponseEntity<DocumentDto> updateDocumentTitle(
            @PathVariable(name = "id") UUID documentId,
            @RequestBody @Valid UpdateTitleRequest request
    ) {
        var document = documentService
                .updateTitle(documentId, documentMapper.toEntity(request));

        return ResponseEntity.ok(documentMapper.toDto(document));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable(name = "id") UUID documentId
    ) {
        documentService.deleteDocument(documentId);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/content")
    public ResponseEntity<ContentDto> updateDocumentContent(
            @PathVariable(name = "id") UUID documentId,
            @RequestBody @Valid UpdateContentRequest request
    ) {
        var content = documentService.updateContent(documentId, contentMapper.toEntity(request));

        return ResponseEntity.ok(contentMapper.toDto(content));
    }

    @GetMapping("/{id}/content")
    public ResponseEntity<ContentDto> getDocumentContent(
        @PathVariable(name = "id") UUID documentId
    ) {
        var document = documentService.getContent(documentId);
        return ResponseEntity.ok(contentMapper.toDto(document));
    }

    @PostMapping("/{id}/share/{userId}")
    public ResponseEntity<SharedDocumentDto> shareDocument(
            @PathVariable(name = "id") UUID documentId,
            @PathVariable(name = "userId") Long shareUserId,
            @RequestBody @Valid ShareDocumentRequest request
    ) {
        var sharedDocument = documentService
                .shareDocument(
                        shareUserId,
                        documentId,
                        request.getPermission()
                );
        return ResponseEntity.ok(sharedDocumentMapper.toSharedDocumentDto(sharedDocument));
    }

    @GetMapping("/share")
    public ResponseEntity<PaginatedData> getAllSharedDocumentsWithUser(
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
                new PaginatedData(
                        documentDtos,
                        pageNumber,
                        pageSize,
                        documents.getTotalPages(),
                        documents.getTotalElements(),
                        documents.hasNext(),
                        documents.hasPrevious()
                ));
    }

    @GetMapping("/{id}/share/access")
    public ResponseEntity<ContentDto> accessSharedDocument(
            @PathVariable(name = "id") UUID documentId
    ) {
        return ResponseEntity
                .ok(contentMapper.toDto(
                        documentService.accessSharedDocument(documentId)
                ));
    }

    @PatchMapping("/share/{id}")
    public ResponseEntity<ContentDto> updateSharedDocument(
            @PathVariable(name = "id") UUID documentId,
            @RequestBody @Valid UpdateContentRequest request
    ) {
        var content = documentService.updateSharedDocument(documentId, contentMapper.toEntity(request));

        return ResponseEntity.ok(contentMapper.toDto(content));
    }

    @GetMapping("/{id}/versions")
    public ResponseEntity<PaginatedData> getAllDocumentVersions(
            @PathVariable(name = "id") UUID documentId,
            @RequestParam(name = "page-number", defaultValue = "0") int pageNumber,
            @RequestParam(name = "page-size", defaultValue = "20") int pageSize
    ) {
        var versions = documentService
                .getAllDocumentVersions(documentId, pageNumber, pageSize);

        var documentDtos = versions.getContent().stream().map(documentVersionMapper::toDto).toList();

        return ResponseEntity.ok(
                new PaginatedData(
                        documentDtos,
                        pageNumber,
                        pageSize,
                        versions.getTotalPages(),
                        versions.getTotalElements(),
                        versions.hasNext(),
                        versions.hasPrevious()
                )
        );
    }

    @GetMapping("/{id}/versions/{versionNumber}")
    public ResponseEntity<ContentDto> getDocumentVersionContent(
            @PathVariable(name = "id") UUID documentId,
            @PathVariable(name = "versionNumber") UUID versionNumber
    ) {
        var versionContent = documentService.getVersionContent(documentId, versionNumber);

        return ResponseEntity.ok(
                new ContentDto(versionContent.getContent()));
    }

    @GetMapping("/{id}/versions/diffs")
    public ResponseEntity<DiffResponse> getVersionDiffs(
            @PathVariable(name = "id") UUID documentId,
            @RequestParam(name = "base-version") UUID base,
            @RequestParam(name = "compare-version") UUID compare
    ) {
        var diffs = documentService.getVersionDiffs(documentId, base, compare);
        return ResponseEntity.ok(new DiffResponse(diffs));
    }

    @PostMapping("/{id}/versions/restore")
    public ResponseEntity<Void> restoreToPreviousVersion(
            @PathVariable(name = "id") UUID documentId
    ) {
        documentService.restoreToPreviousVersion(documentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/versions/restore/{versionNumber}")
    public ResponseEntity<Void> restoreToDocumentSpecificVersion(
            @PathVariable(name = "id") UUID documentId,
            @PathVariable(name = "versionNumber") UUID versionNumber
    ) {
        documentService.restoreToDocumentSpecificVersion(documentId, versionNumber);

        return ResponseEntity.noContent().build();
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

    @ExceptionHandler(ReadOnlyDocumentException.class)
    public ResponseEntity<ErrorResponse> handleReadOnlyDocumentException(
            ReadOnlyDocumentException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(DocumentVersionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDocumentVersionNotFoundException(
            DocumentVersionNotFoundException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(exception.getMessage()));
    }
}

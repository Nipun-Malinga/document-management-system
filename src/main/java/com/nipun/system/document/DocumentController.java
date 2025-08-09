package com.nipun.system.document;

import com.nipun.system.document.dtos.*;
import com.nipun.system.document.dtos.common.PaginatedData;
import com.nipun.system.document.dtos.share.AccessSharedDocumentRequest;
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

    @PutMapping("/{documentId}/content")
    public ResponseEntity<ContentDto> updateDocumentContent(
            @PathVariable UUID documentId,
            @RequestBody @Valid UpdateContentRequest request
    ) {
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

    @GetMapping("/share/access")
    public ResponseEntity<ContentDto> accessSharedDocument(
            @RequestBody @Valid AccessSharedDocumentRequest request
    ) {
        return ResponseEntity
                .ok(contentMapper.toDto(
                        documentService.accessSharedDocument(request.getDocumentId())
                ));
    }

    @PutMapping("/share/{documentId}")
    public ResponseEntity<ContentDto> updateSharedDocument(
            @PathVariable(name = "documentId") UUID documentId,
            @RequestBody @Valid UpdateContentRequest request
    ) {
        var content = documentService.updateSharedDocument(documentId, contentMapper.toEntity(request));

        return ResponseEntity.ok(contentMapper.toDto(content));
    }

    @GetMapping("/{documentId}/versions")
    public ResponseEntity<PaginatedData> getAllDocumentVersions(
            @PathVariable(name = "documentId") UUID documentId,
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

    @GetMapping("/versions/{versionNumber}")
    public ResponseEntity<ContentDto> getDocumentVersionContent(
            @PathVariable(name = "versionNumber") UUID versionNumber
    ) {
        var versionContent = documentService.getVersionContent(versionNumber);

        return ResponseEntity.ok(
                new ContentDto(versionContent.getContent()));
    }

    @GetMapping("/{documentId}/versions/diffs")
    public ResponseEntity<DiffResponse> getVersionDiffs(
            @PathVariable(name = "documentId") UUID documentId,
            @RequestParam(name = "base-version") UUID base,
            @RequestParam(name = "compare-version") UUID compare
    ) {
        var diffs = documentService.getVersionDiffs(documentId, base, compare);
        return ResponseEntity.ok(new DiffResponse(diffs));
    }

    @PostMapping("/{documentId}/versions/restore")
    public ResponseEntity<Void> restoreToPreviousVersion(
            @PathVariable(name = "documentId") UUID documentId
    ) {
        documentService.restoreToPreviousVersion(documentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/versions/restore/{versionNumber}")
    public ResponseEntity<Void> restoreToDocumentSpecificVersion(
            @PathVariable(name = "versionNumber") UUID versionNumber
    ) {
        documentService.restoreToDocumentSpecificVersion(versionNumber);

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

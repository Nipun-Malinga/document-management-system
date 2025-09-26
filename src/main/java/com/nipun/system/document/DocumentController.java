package com.nipun.system.document;

import com.nipun.system.document.dtos.*;
import com.nipun.system.shared.dtos.PaginatedData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/documents")
@Tag(name = "Documents", description = "Manage documents in the system")
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping
    @Operation(summary = "Create document", description = "Creates new document in the system")
    public ResponseEntity<DocumentDto> createDocument(
            @RequestBody @Valid CreateDocumentRequest request,
            UriComponentsBuilder uriBuilder
    ) {
        var document = documentService.createDocument(request);

        var uri = uriBuilder.path("/documents/{id}")
                .buildAndExpand(document.getId()).toUri();

        return ResponseEntity.created(uri).body(document);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get document", description = "Get document by ID")
    public ResponseEntity<DocumentDto> getDocument(
            @PathVariable(name = "id")
            @Parameter(description = "The ID of the document", example = "bfb8777b-59bd-422b-8132-d1f64b09590d")
            UUID documentId
    ) {
        var documentDto = documentService.getDocument(documentId);
        return ResponseEntity.ok(documentDto);
    }

    @GetMapping
    @Operation(summary = "Get all documents", description = "Get all user owned documents")
    public ResponseEntity<PaginatedData> getAllDocuments(
            @RequestParam(name = "page-number", defaultValue = "0")
            @Parameter(description = "Required page number")
            int pageNumber,
            @RequestParam(name = "page-size", defaultValue = "20")
            @Parameter(description = "Required page size")
            int pageSize
    ) {
        var paginatedDocumentDtoList = documentService.getAllDocuments(pageNumber, pageSize);
        return ResponseEntity.ok(paginatedDocumentDtoList);
    }

    @PutMapping("/{id}/title")
    @Operation(summary = "Update document title", description = "Updates the document title")
    public ResponseEntity<DocumentDto> updateDocumentTitle(
            @PathVariable(name = "id")
            @Parameter(description = "The ID of the document", example = "bfb8777b-59bd-422b-8132-d1f64b09590d")
            UUID documentId,
            @RequestBody @Valid UpdateTitleRequest request
    ) {
        var documentDto = documentService.updateTitle(documentId, request);
        return ResponseEntity.ok(documentDto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete document", description = "Deletes document from the system")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable(name = "id")
            @Parameter(description = "The ID of the document", example = "bfb8777b-59bd-422b-8132-d1f64b09590d")
            UUID documentId
    ) {
        documentService.deleteDocument(documentId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/content")
    @Operation(summary = "Update document content", description = "Updates the content in the system")
    public ResponseEntity<ContentDto> updateDocumentContent(
            @PathVariable(name = "id")
            @Parameter(description = "The ID of the document", example = "bfb8777b-59bd-422b-8132-d1f64b09590d")
            UUID documentId,
            @RequestBody @Valid UpdateContentRequest request
    ) {
        var contentDto = documentService.updateContent(documentId, request);
        return ResponseEntity.ok(contentDto);
    }

    @GetMapping("/{id}/content")
    @Operation(summary = "Get document content", description = "Gets document content")
    public ResponseEntity<ContentDto> getDocumentContent(
        @PathVariable(name = "id")
        @Parameter(description = "The ID of the document", example = "bfb8777b-59bd-422b-8132-d1f64b09590d")
        UUID documentId
    ) {
        var contentDto = documentService.getContent(documentId);
        return ResponseEntity.ok(contentDto);
    }
}
